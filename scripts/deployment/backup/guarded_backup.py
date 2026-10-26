"""Guarded cold capture. Default plan is offline; host hooks ship unconfigured."""

from __future__ import annotations

import argparse
import json
import os
import signal
import socket
import sys
import time
from pathlib import Path
from typing import Callable

from backup_protocol import (GateError, backup_snapshot, digest, encoded, exact_id,
                             recurring_approval, rejected_snapshot, require, restore_gate, snapshot_nodes,
                             status_report, timestamp, utc_now, validate_approval,
                             validate_config, verify_nodes)
from backup_runtime import (CommandResult, LinuxMonitor, LinuxRunner, atomic_json, checked_path,
                            create_bytes, hash_file, inventory, operations_lock,
                            read_json, restic_command, restic_environment)

HOOK_CHECKS = {
    "preflight": ("target_matched", "private_repository_acl", "recovery_access_verified",
                  "source_inventory_reviewed", "byte_cost_budget_approved", "no_overlap"),
    "close-admission": ("admission_closed",),
    "quiesce": ("drained_or_aborted", "jvm_stopped", "workers_stopped", "relaunch_disabled"),
    "fence": ("db_writers_fenced", "file_writers_fenced", "schema_rotation_fenced"),
    "assert-quiescent": ("admission_closed", "jvm_stopped", "workers_stopped",
                         "relaunch_disabled", "db_writers_fenced", "file_writers_fenced",
                         "schema_rotation_fenced"),
    "validate-dump": ("schema_complete", "all_application_tables", "rows_complete"),
    "resume": ("matching_tuple", "sole_writer", "restricted_health", "admission_reopened"),
}


class StateStore:
    def __init__(self, directory: Path) -> None:
        self.path = directory / "state.json"

    def load(self) -> dict:
        if not self.path.exists():
            return {"version": 1, "attempt": {}, "last_good": None}
        require(not self.path.is_symlink(), "STATE_SYMLINK")
        state = read_json(self.path)
        require(state.get("version") == 1 and isinstance(state.get("attempt"), dict),
                "STATE_INVALID")
        if state.get("last_good"):
            exact_id(state["last_good"].get("snapshot_id"))
            timestamp(state["last_good"].get("captured_at"))
        return state

    def save(self, state: dict) -> None:
        atomic_json(self.path, state)


class Capture:
    """One lock owner and one immutable dump per run; injected runner enables fake tests."""

    def __init__(self, config: dict, config_path: Path, approval: dict,
                 runner: LinuxRunner, store: StateStore,
                 tick: Callable[[], None]) -> None:
        self.config = config
        self.config_path = config_path
        self.approval = approval
        self.runner = runner
        self.store = store
        self.tick = tick
        self.run_dir = Path(config["work_dir"]) / approval["run_id"]
        self.state = store.load()
        self.snapshot: str | None = None
        self.maintenance = False

    def stage(self, name: str, code: str | None = None) -> None:
        attempt = self.state["attempt"]
        attempt.update(stage=name, maintenance=self.maintenance, updated_at=utc_now())
        if self.snapshot:
            attempt["snapshot_id"] = self.snapshot
        if code:
            attempt["failure_code"] = code
        self.store.save(self.state)

    def hook(self, name: str) -> dict:
        require(name in HOOK_CHECKS, "HOOK_NOT_ALLOWED")
        self.tick()
        reply = self.runner.run([self.config["hook_binary"], name,
                                 str(self.config_path), str(self.run_dir)], 65536)
        require(reply.code == 0, "HOST_HOOK_EXIT")
        try:
            result = json.loads(reply.data)
        except (ValueError, UnicodeError):
            raise GateError("HOST_HOOK_PROTOCOL") from None
        require(isinstance(result, dict) and result.get("configured") is True,
                "HOST_HOOK_NOT_CONFIGURED")
        require(all(result.get(key) is True for key in HOOK_CHECKS[name]), "HOST_HOOK_GATE")
        return result

    def restic(self, arguments: list[str], max_bytes: int,
               *, hash_only: bool = False) -> CommandResult:
        return self.runner.run(restic_command(self.config, arguments), max_bytes,
                               hash_only=hash_only)

    def inventory(self, paths: list[Path]) -> dict[str, dict]:
        limits = self.config["limits"]
        devices = {str(path): path.stat().st_dev for path in paths}
        return inventory(paths, self.tick, limits["max_entries"], limits["source_bytes"],
                         limits["metadata_bytes"], devices)

    def run(self) -> dict:
        require(not self.state["attempt"].get("maintenance"), "PRIOR_MAINTENANCE")
        require(self.state["attempt"].get("stage") in {None, "COMPLETE", "FAILED"},
                "PRIOR_RUN_INCOMPLETE")
        self.run_dir.mkdir(mode=0o700, exist_ok=False)
        old_good = self.state.get("last_good")
        self.state["attempt"] = {"run_id": self.approval["run_id"], "started_at": utc_now(),
                                 "config_sha256": digest(encoded(self.config))}
        limits = self.config["limits"]
        try:
            self.stage("PRECHECK")
            self.hook("preflight")
            reply = self.restic(["cat", "config"], 65536)
            require(reply.code == 0, "REPOSITORY_EXIT")
            repo = json.loads(reply.data)
            require(repo.get("id") == self.config["repository_id"] and
                    repo.get("version") == 2, "REPOSITORY_IDENTITY")
            self.maintenance = True
            self.stage("CLOSE_ADMISSION")
            self.hook("close-admission")
            self.hook("quiesce")
            self.hook("fence")
            self.stage("QUIESCENT")
            self.state["attempt"]["boundary_start"] = utc_now()
            self.hook("assert-quiescent")
            self.stage("DUMP")
            partial = self.run_dir / "database.partial"
            reply = self.runner.run([self.config["hook_binary"], "logical-dump",
                                     str(self.config_path), str(self.run_dir)],
                                    limits["dump_bytes"], partial)
            require(reply.code == 0 and reply.size > 0, "DUMP_EXIT_OR_EMPTY")
            actual_size, actual_sha = hash_file(partial, self.tick, limits["dump_bytes"])
            require((reply.size, reply.sha256) == (actual_size, actual_sha), "DUMP_HASH")
            proof = self.hook("validate-dump")
            require(proof.get("dump_sha256") == actual_sha, "DUMP_INVENTORY_HASH")
            dump = self.run_dir / "database.sql"
            require(not dump.exists(), "DUMP_ALREADY_EXISTS")
            partial.rename(dump)
            self.hook("assert-quiescent")
            sources = [Path(self.config[key]) for key in
                       ("public_root", "private_root", "release_root")]
            sources += [Path(path) for path in self.config["settings"]] + [dump]
            members = self.inventory(sources)
            manifest = {"version": 1, "run_id": self.approval["run_id"],
                        "boundary_start": self.state["attempt"]["boundary_start"],
                        "source_revision": self.config["source_revision"],
                        "dirty_disposition": self.config["dirty_disposition"],
                        "schema_ref": self.config["schema_ref"],
                        "release_link": self.config["release_link"],
                        "release_root": self.config["release_root"], "members": members}
            manifest_bytes = encoded(manifest)
            require(len(manifest_bytes) <= limits["metadata_bytes"], "MANIFEST_LIMIT")
            manifest_path = self.run_dir / "manifest.json"
            create_bytes(manifest_path, manifest_bytes)
            expected = {**members, **self.inventory([manifest_path])}
            retrieval_members = (str(dump), str(manifest_path))
            retrieval_bytes = sum(expected[path]["size"] for path in retrieval_members)
            require(retrieval_bytes <= limits["retrieval_bytes"], "RETRIEVAL_BUDGET")
            self.stage("CAPTURE")
            self.hook("assert-quiescent")
            arguments = ["backup", "--json", "--read-concurrency", "1", "--host",
                         self.config["logical_host"], "--tag",
                         "ats-prod,cold-v1," + self.config["class"], "--force", "--",
                         *map(str, sources), str(manifest_path)]
            reply = self.restic(arguments, min(limits["metadata_bytes"], 8 * 1024**2))
            self.snapshot = rejected_snapshot(reply.data)
            self.snapshot = backup_snapshot(reply.data, reply.code)
            if old_good:
                require(self.snapshot != old_good["snapshot_id"], "SNAPSHOT_NOT_NEW")
            self.stage("VERIFY")
            self.hook("assert-quiescent")
            reply = self.restic(["ls", "--json", self.snapshot], limits["metadata_bytes"])
            require(reply.code == 0, "INVENTORY_EXIT")
            verify_nodes(expected, snapshot_nodes(reply.data, self.snapshot))
            # Only manifest and dump retrieval belong in the cold window.
            for path in retrieval_members:
                row = expected[path]
                reply = self.restic(["dump", self.snapshot, path], row["size"],
                                    hash_only=True)
                require(reply.code == 0 and reply.size == row["size"] and
                        reply.sha256 == row["sha256"], "RETRIEVED_HASH")
            require(members == self.inventory(sources), "CAPTURE_SOURCE_CHANGED")
            require(Path(self.config["release_link"]).resolve(strict=True) ==
                    Path(self.config["release_root"]), "RELEASE_CHANGED")
            self.hook("assert-quiescent")
            require(not self.runner.active, "HELPERS_STILL_RUNNING")
            self.state["attempt"]["verified_at"] = utc_now()
            self.stage("CAPTURE_COMPLETE")
            self.tick()
            require(self.approval["resume_authorized"] is True, "RESUME_APPROVAL")
            self.stage("RESUME")
            self.hook("resume")
            require(not self.runner.active, "HELPERS_STILL_RUNNING")
            self.maintenance = False
            self.state["last_good"] = {
                "snapshot_id": self.snapshot, "repository_id": self.config["repository_id"],
                "captured_at": self.state["attempt"]["boundary_start"],
                "verified_at": self.state["attempt"]["verified_at"],
                "manifest_sha256": digest(manifest_bytes), "dump_sha256": actual_sha,
                "run_id": self.approval["run_id"], "restore_verified": False}
            create_bytes(self.run_dir / "receipt.json", encoded({
                "status": "VERIFIED_AND_RESUMED", "last_good": self.state["last_good"],
                "completed_at": utc_now()}))
            self.stage("COMPLETE")
            return {"status": "COMPLETE", "restore": "NOT_RUN", "copy": "NOT_RUN"}
        except BaseException as error:
            self.state["last_good"] = old_good
            code = str(error) if isinstance(error, GateError) else "CAPTURE_FAILURE"
            try:
                self.runner.stop_owned()
            except BaseException:
                code = "HELPERS_STILL_RUNNING"
            try:
                self.stage("FAILED", code)
                create_bytes(self.run_dir / "failure.json", encoded(self.state["attempt"]))
            except BaseException:
                code = "RECEIPT_WRITE_FAILED"
            raise GateError(code) from None


def execute(config: dict, config_path: Path, approval_path: Path,
            *, recurring: bool = False) -> dict:
    require(sys.platform == "linux" and os.geteuid() == 0, "LINUX_ROOT_REQUIRED")
    require(socket.gethostname() == config["target_host"], "EXACT_HOST_REQUIRED")
    checked_path(config_path, private=True)
    checked_path(approval_path, private=True)
    authorization = read_json(approval_path)
    approval = recurring_approval(config, authorization, time.time()) if recurring else authorization
    validate_approval(config, approval, time.time())
    for key in ("state_dir", "work_dir", "cache_dir", "temp_dir"):
        require(checked_path(config[key], private=True).is_dir(), "RUNTIME_DIRECTORY")
    for key in ("password_file", "aws_credentials_file", "mysql_defaults_file"):
        require(checked_path(config[key], private=True).is_file(), "CREDENTIAL_REFERENCE")
    for key in ("public_root", "private_root", "release_root"):
        require(checked_path(config[key]).is_dir(), "SOURCE_DIRECTORY")
    for path in config["settings"]:
        checked_path(path, trusted=True)
    release_link = Path(config["release_link"])
    checked_path(release_link.parent, trusted=True)
    require(release_link.resolve(strict=True) == Path(config["release_root"]), "RELEASE_TARGET")
    required_mounts = {config[key] for key in ("public_root", "private_root", "release_root",
                                              "state_dir", "work_dir", "cache_dir", "temp_dir")}
    required_mounts.update(config["settings"])
    require(required_mounts <= set(config["expected_filesystems"]), "MOUNT_COVERAGE")
    for key in ("restic", "hook"):
        binary = checked_path(config[key + "_binary"], trusted=True)
        require(binary.is_file() and os.access(binary, os.X_OK), "EXECUTABLE_REQUIRED")
        _, sha = hash_file(binary, lambda: None, 256 * 1024**2)
        require(sha == config[key + "_sha256"], "EXECUTABLE_HASH")
    with operations_lock(Path(config["lock_file"])):
        if recurring:
            approval = recurring_approval(config, authorization, time.time())
        validate_approval(config, approval, time.time())
        monitor = LinuxMonitor(config, timestamp(approval["not_after"]))
        runner = LinuxRunner(monitor, restic_environment(config))
        capture = Capture(config, config_path, approval, runner,
                          StateStore(Path(config["state_dir"])), monitor.tick)
        previous = {}

        def interrupted(signum: int, frame: object) -> None:
            raise GateError("INTERRUPTED")

        try:
            for sig in (signal.SIGINT, signal.SIGTERM):
                previous[sig] = signal.signal(sig, interrupted)
            return capture.run()
        finally:
            runner.stop_owned()
            for sig, handler in previous.items():
                signal.signal(sig, handler)


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("command", nargs="?", default="plan",
                        choices=("plan", "validate", "execute", "scheduled", "status",
                                 "restore-plan", "copy-plan"))
    parser.add_argument("--config", type=Path)
    parser.add_argument("--approval", type=Path)
    parser.add_argument("--state", type=Path)
    parser.add_argument("--copy-receipt", type=Path)
    parser.add_argument("--expected-by")
    parser.add_argument("--expected-since")
    args = parser.parse_args(argv)
    try:
        if args.command == "status":
            state = read_json(args.state) if args.state else {}
            copy = read_json(args.copy_receipt) if args.copy_receipt else None
            result = status_report(state, time.time(), timestamp(args.expected_by)
                                   if args.expected_by else None, copy,
                                   timestamp(args.expected_since) if args.expected_since else None)
            code = 0 if result["status"] == "CURRENT" else 2
        elif args.command == "copy-plan":
            result, code = {"status": "NOT_CONFIGURED", "copy": "FUTURE_GUARDED_OPERATION"}, 2
        elif args.command == "restore-plan":
            require(args.config is not None, "RESTORE_PLAN_REQUIRED")
            restore_gate(read_json(args.config))
            result, code = {"status": "PREPARATORY_ONLY", "restore": "NOT_RUN"}, 0
        elif args.config is None and args.command == "plan":
            result, code = {"status": "NOT_CONFIGURED", "side_effects": False}, 0
        else:
            require(args.config is not None, "CONFIG_REQUIRED")
            config = read_json(args.config)
            validate_config(config)
            if args.command in {"execute", "scheduled"}:
                require(args.approval is not None, "EXACT_APPROVAL_REQUIRED")
                result, code = execute(config, args.config.absolute(), args.approval.absolute(),
                                       recurring=args.command == "scheduled"), 0
            else:
                result, code = {"status": "CONFIG_VALID_ONLY", "target_readiness": "NOT_RUN",
                                "config_sha256": digest(encoded(config)), "side_effects": False}, 0
        print(json.dumps(result, sort_keys=True))
        return code
    except (GateError, OSError, ValueError, TypeError, KeyError, AttributeError) as error:
        # Fixed codes only: never expose operating paths or exception messages.
        code = str(error) if isinstance(error, GateError) else "INVALID_INPUT_OR_IO"
        print(json.dumps({"status": "BLOCKED", "code": code}))
        return 2


if __name__ == "__main__":
    raise SystemExit(main())

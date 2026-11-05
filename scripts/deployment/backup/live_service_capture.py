#!/usr/bin/python3
"""One approved capture attached to existing services; inert unless --execute.

This adapter has no service start/stop API. Native execution is Linux/root in a
separate bounded helper cgroup. Existing online capture retains its own proofs,
operations lock, finite resource limits, helper cleanup and HOLD protocol.
"""
from __future__ import annotations

import argparse
import copy
import os
from pathlib import Path
import re
import socket
import stat
import subprocess
import sys
import time
from typing import Any, Callable
import uuid

from backup_protocol import GateError, digest, encoded, exact_id, linux_path, overlap, require, timestamp
from backup_runtime import checked_path, hash_file
import online_capture as online
import outer_recovery as outer
from scheduled_dispatch import load, safe_path, sync_dir, verify_files, write_new

MODULES = ("live_service_capture.py", "online_capture.py", "backup_protocol.py",
           "backup_runtime.py", "scheduled_dispatch.py")
UNIT_PROPERTIES = ("LoadState", "ActiveState", "SubState", "MainPID", "ControlPID",
                   "ControlGroup", "InvocationID", "FragmentPath", "RuntimeMaxUSec",
                   "PartOf", "BindsTo")
REQUIRED_ASSERTIONS = (
    "sole_app_writer", "no_uncoordinated_file_writers", "no_overlapping_deployment",
    "effective_settings_verified", "catalog_admission_verified",
    "audio_worker_admission_verified", "private_mailbox_verified",
    "catalog_audit_in_dump_verified", "reader_impact_and_freeze_bound_accepted",
)
EFFECTIVE_MODE = {
    "app.listening.enabled": True,
    "app.listening.catalog-source": "DATABASE",
    "app.background-tasks.audio-only": True,
    "app.background-tasks.capture-control-only": False,
    "app.audio.worker-enabled": True,
    "app.storage.capture-control-enabled": True,
    "app.bootstrap.test-users.enabled": False,
}


def subject(attachment: dict) -> str:
    """Hash noncircular attachment inputs for independent runtime evidence."""
    value = copy.deepcopy(attachment)
    value.pop("runtime_proof", None)
    return digest(encoded(value))


def validate_service(service: dict) -> None:
    required = {"unit", "pid", "start_ticks", "invocation_id", "cgroup", "exe",
                "exe_sha256", "cmdline_sha256", "unit_fragment", "unit_sha256",
                "uid", "gid", "netns_inode"}
    require(type(service) is dict and set(service) == required, "SERVICE_SHAPE")
    require(re.fullmatch(r"[A-Za-z0-9][A-Za-z0-9_.@:-]{0,127}\.service",
                         service["unit"]) is not None, "SERVICE_UNIT")
    for key in ("pid", "start_ticks", "uid", "gid", "netns_inode"):
        require(type(service[key]) is int and service[key] > 0, "SERVICE_IDENTITY")
    require(re.fullmatch(r"[a-f0-9]{32}", service["invocation_id"]) is not None,
            "SERVICE_INVOCATION")
    for key in ("cgroup", "exe", "unit_fragment"):
        linux_path(service[key])
    require(service["cgroup"] != "/", "SERVICE_CGROUP")
    for key in ("exe_sha256", "cmdline_sha256", "unit_sha256"):
        exact_id(service[key])


def validate_inputs(config: dict, inner: dict, attachment: dict, authority: dict,
                    proof: dict, pins: dict, now: float) -> None:
    """Validate exact single-attempt authority, not manufacture runtime evidence."""
    online.validate_online(config)
    online.validate_authority(config, inner, now)
    require(attachment.get("version") == 1 and attachment.get("configured") is True,
            "ATTACHMENT_DISABLED")
    require(set(attachment["services"]) == {"app", "db"}, "SERVICE_SET")
    for service in attachment["services"].values():
        validate_service(service)
    app, db = attachment["services"]["app"], attachment["services"]["db"]
    require(app["unit"] != db["unit"] and app["pid"] != db["pid"] and
            not overlap(app["cgroup"], db["cgroup"]), "DISTINCT_SERVICES")
    require((app["uid"], app["gid"]) ==
            (config["online"]["app_uid"], config["online"]["app_gid"]), "APP_IDENTITY")
    require(attachment["online_config_sha256"] == digest(encoded(config)) and
            attachment["online_approval_sha256"] == digest(encoded(inner)), "INNER_BINDING")
    require(attachment["pins_sha256"] == digest(encoded(pins)), "PINS_BINDING")
    linux_path(attachment["app_jar"])
    require(Path(attachment["app_jar"]).is_relative_to(Path(config["release_root"])),
            "JAR_RELEASE_BINDING")
    exact_id(attachment["app_jar_sha256"])
    exact_id(attachment["systemctl_sha256"])
    require({"site_settings", "admin_operation_audit_logs"} <= set(config["online"]["tables"]),
            "CATALOG_TABLES_REQUIRED")
    require(authority.get("version") == 1 and authority.get("operation") == "live-service-capture"
            and type(authority.get("max_attempts")) is int and authority["max_attempts"] == 1
            and authority.get("run_id") == inner["run_id"]
            and authority.get("attachment_sha256") == digest(encoded(attachment))
            and authority.get("service_control_authorized") is False, "ATTACHMENT_AUTHORITY")
    begin, end = timestamp(authority["not_before"]), timestamp(authority["not_after"])
    require(begin == timestamp(inner["not_before"]) and end == timestamp(inner["not_after"])
            and begin <= now < end and end - begin <= 1800, "ATTACHMENT_WINDOW")
    require(proof.get("version") == 1 and proof.get("kind") == "live-service-runtime"
            and proof.get("scope") == "ONE_EXISTING_SERVICE_CAPTURE"
            and proof.get("subject_sha256") == subject(attachment)
            and proof.get("run_id") == inner["run_id"]
            and proof.get("machine_sha256") == config["online"]["machine_sha256"]
            and proof.get("boot_sha256") == config["online"]["boot_sha256"]
            and proof.get("services_sha256") == digest(encoded(attachment["services"])),
            "RUNTIME_PROOF_BINDING")
    exact_id(proof["source_evidence_sha256"])
    observed, expires = timestamp(proof["observed_at"]), timestamp(proof["expires_at"])
    require(begin - 600 <= observed <= now < end <= expires and expires - observed <= 3600,
            "RUNTIME_PROOF_STALE")
    require(proof.get("effective_mode") == EFFECTIVE_MODE and
            all(proof.get(key) is True for key in REQUIRED_ASSERTIONS), "RUNTIME_CONTRACT")
    require(type(attachment.get("runtime_proof")) is dict and
            attachment["runtime_proof"].get("sha256") == digest(encoded(proof)), "PROOF_HASH")
    linux_path(attachment["runtime_proof"]["path"])


def validate_previous(root: Path) -> None:
    """A retained failure, incomplete attempt or corrupt receipt prevents new work."""
    for entry in root.iterdir():
        if entry.name == "attachment.lock":
            continue
        require(re.fullmatch(r"attempt-[a-f0-9]{32}", entry.name) is not None
                and entry.is_dir() and not entry.is_symlink(), "UNEXPECTED_LEDGER_ENTRY")
        claim = load(entry / "claim.json")
        result = load(entry / "result.json")
        require(claim.get("run_id") == entry.name.removeprefix("attempt-") and
                result.get("claim_sha256") == digest(encoded(claim)) and
                result.get("run_id") == claim["run_id"] and
                result.get("capture_id") == str(uuid.UUID(claim["run_id"])) and
                result.get("status") == "COMPLETE" and result.get("restore_verified") is False
                and result.get("service_identity_unchanged") is True, "PRIOR_ATTACHMENT_HOLD")


def attach_once(root: Path, config: dict, inner: dict, attachment: dict,
                authority: dict, proof: dict, pins: dict, backend: Any,
                clock: Callable[[], float] = time.time) -> dict:
    """Consume a durable attempt, observe services, call online capture once.

    backend is injected only for local fixtures. The CLI fixes NativeBackend.
    No retry, automatic reconciliation or service lifecycle operation exists.
    """
    validate_inputs(config, inner, attachment, authority, proof, pins, clock())
    verify_files(pins)
    if outer.enabled(config):
        require(Path(root) == outer.ledger(config), "STABLE_LEDGER_REQUIRED")
        return attach_v2(config, inner, attachment, authority, proof, pins, backend, clock)
    root = safe_path(root)
    require(not (root / "operation.guard").exists(), "LEGACY_GATE_INCOMPATIBLE")
    require(root.is_dir(), "LEDGER_REQUIRED")
    if os.name == "posix":
        info = root.stat()
        require(info.st_uid == os.geteuid() and stat.S_IMODE(info.st_mode) == 0o700,
                "PRIVATE_LEDGER_REQUIRED")
    lock = root / "attachment.lock"
    try:
        lock.mkdir(mode=0o700)
    except FileExistsError:
        raise GateError("ATTACHMENT_LOCKED_OR_HOLD") from None
    sync_dir(root)
    claimed = False
    entry = None
    stage = "PRECHECK"
    try:
        validate_previous(root)
        run_id = inner["run_id"]
        entry = root / ("attempt-" + run_id)
        require(not entry.exists(), "DUPLICATE_ATTACHMENT")
        validate_inputs(config, inner, attachment, authority, proof, pins, clock())
        entry.mkdir(mode=0o700)
        claimed = True
        claim = {"version": 1, "run_id": run_id,
                 "attachment_sha256": digest(encoded(attachment)),
                 "authority_sha256": digest(encoded(authority)),
                 "online_config_sha256": digest(encoded(config)),
                 "online_approval_sha256": digest(encoded(inner)),
                 "proof_sha256": digest(encoded(proof)), "pins_sha256": digest(encoded(pins))}
        write_new(entry / "claim.json", claim)
        sync_dir(root)
        stage = "OBSERVE_BEFORE"
        before = backend.observe()
        require(before == attachment["services"], "SERVICE_IDENTITY_BEFORE")
        write_new(entry / "before.json", before)
        verify_files(pins)
        validate_inputs(config, inner, attachment, authority, proof, pins, clock())
        stage = "CAPTURE"
        value = backend.capture()
        require(value == {"status": "COMPLETE", "restore": "NOT_RUN",
                          "capture_id": str(uuid.UUID(run_id))}, "ONLINE_RECEIPT_REQUIRED")
        stage = "OBSERVE_AFTER"
        after = backend.observe()
        write_new(entry / "after.json", after)
        require(after == before, "SERVICE_IDENTITY_AFTER")
        verify_files(pins)
        require(clock() < timestamp(authority["not_after"]), "ATTACHMENT_WINDOW_EXPIRED")
        result = {"status": "COMPLETE", "run_id": run_id, "capture_id": value["capture_id"],
                  "claim_sha256": digest(encoded(claim)), "restore_verified": False,
                  "service_identity_unchanged": True}
        stage = "CLEANUP"
        backend.cleanup()
        write_new(entry / "result.json", result)
        claimed = False
        return result
    except BaseException:
        if claimed and entry is not None:
            cleanup_confirmed = False
            after_confirmed = False
            try:
                backend.cleanup()
                cleanup_confirmed = True
            except BaseException:
                pass
            try:
                observed = backend.observe()
                write_new(entry / "after-failure.json", observed)
                after_confirmed = observed == attachment["services"]
            except BaseException:
                pass
            # 오류 원문에는 private 경로/명령이 포함될 수 있으므로 고정 상태만 남긴다.
            write_new(entry / "hold.json", {"status": "HOLD", "failed_stage": stage,
                      "adapter_cleanup_confirmed": cleanup_confirmed,
                      "service_identity_observed_unchanged": after_confirmed,
                      "online_outcome": "RECONCILE_SEPARATELY", "restore_verified": False})
        raise
    finally:
        if not claimed:
            lock.rmdir()
            sync_dir(root)


def attach_v2(config, inner, attachment, authority, proof, pins, backend, clock):
    """One gate across observation, entry, inner execution, cleanup and publication."""
    with outer.gate(config) as owner:
        outer.pins(config)
        require(config["recovery_gate"]["source_units"] == {k: v["unit"] for k, v in attachment["services"].items()}, "SOURCE_UNITS_BINDING")
        validate_inputs(config, inner, attachment, authority, proof, pins, clock())
        entry, claim = outer.begin_capture(config, inner, owner, {
            "attachment_sha256": digest(encoded(attachment)), "authority_sha256": digest(encoded(authority)),
            "proof_sha256": digest(encoded(proof)), "pins_sha256": digest(encoded(pins))})
        backend.owner = owner
        stage = "OBSERVE_BEFORE"
        try:
            before = backend.observe()
            require(before == attachment["services"], "SERVICE_IDENTITY_BEFORE")
            outer.durable(entry / "before.json", encoded(before))
            verify_files(pins)
            validate_inputs(config, inner, attachment, authority, proof, pins, clock())
            stage = "CAPTURE"
            outer.call_capture(config, owner)
            value = backend.capture()
            require(value == {"status": "COMPLETE", "restore": "NOT_RUN",
                              "capture_id": str(uuid.UUID(inner["run_id"]))}, "ONLINE_RECEIPT_REQUIRED")
            stage = "OBSERVE_AFTER"
            after = backend.observe()
            outer.durable(entry / "after.json", encoded(after))
            require(after == before, "SERVICE_IDENTITY_AFTER")
            verify_files(pins)
            require(clock() < timestamp(authority["not_after"]), "ATTACHMENT_WINDOW_EXPIRED")
            backend.cleanup()
            return outer.finish_capture(config, owner, {"status": "COMPLETE", "run_id": inner["run_id"],
                "capture_id": value["capture_id"], "claim_sha256": digest(encoded(claim)),
                "restore_verified": False, "service_identity_unchanged": True})
        except BaseException:
            try: backend.cleanup()
            finally:
                outer.durable(entry / "hold.json", encoded({"status": "HOLD", "failed_stage": stage,
                    "online_outcome": "RECONCILE_SEPARATELY", "restore_verified": False}))
            raise
        finally:
            backend.owner = None


def parse_properties(raw: bytes) -> dict[str, str]:
    """Parse only fixed systemctl show fields, rejecting ambiguous output."""
    require(len(raw) <= 32768, "SHOW_BOUND")
    result = {}
    for line in raw.decode("utf-8").splitlines():
        key, separator, value = line.partition("=")
        require(separator == "=" and key in UNIT_PROPERTIES and key not in result,
                "SHOW_PROPERTIES")
        result[key] = value
    require(set(result) == set(UNIT_PROPERTIES), "SHOW_PROPERTIES")
    return result


class NativeBackend:
    """Read-only source observations; online.execute alone owns its helpers."""

    def __init__(self, config: dict, inner: dict, attachment: dict,
                 inputs: dict[str, Path], loaded: dict[str, dict]) -> None:
        self.config = config
        self.inner = inner
        self.attachment = attachment
        self.inputs = inputs
        self.loaded = loaded

    @staticmethod
    def show(unit: str) -> dict[str, str]:
        # service 제어 명령은 존재하지 않는다. timeout은 이 읽기용 자식만 종료한다.
        reply = subprocess.run(["/usr/bin/systemctl", "show", "--no-pager",
                                "--property=" + ",".join(UNIT_PROPERTIES), "--", unit],
                               capture_output=True, check=False, timeout=10,
                               env={"PATH": "/usr/sbin:/usr/bin:/sbin:/bin", "LC_ALL": "C"})
        require(reply.returncode == 0 and len(reply.stderr) <= 32768, "SHOW_FAILED")
        return parse_properties(reply.stdout)

    def observe(self) -> dict:
        require(socket.gethostname() == self.config["target_host"], "EXACT_HOST")
        for field, path in (("machine", "/etc/machine-id"),
                            ("boot", "/proc/sys/kernel/random/boot_id")):
            require(digest(Path(path).read_bytes().strip()) ==
                    self.config["online"][field + "_sha256"], "HOST_IDENTITY")
        systemctl = checked_path("/usr/bin/systemctl", trusted=True)
        require(hash_file(systemctl, lambda: None, 32 * 1024**2)[1] ==
                self.attachment["systemctl_sha256"], "SYSTEMCTL_PIN")
        own_groups = Path("/proc/self/cgroup").read_text().splitlines()
        own = [line[3:] for line in own_groups if line.startswith("0::")]
        require(len(own) == 1 and own[0] != "/", "HELPER_CGROUP_REQUIRED")
        observed = {}
        for name, expected in self.attachment["services"].items():
            row = self.show(expected["unit"])
            require(row["LoadState"] == "loaded" and row["ActiveState"] == "active"
                    and row["SubState"] == "running" and row["ControlPID"] == "0"
                    and row["RuntimeMaxUSec"] == "infinity"
                    and not row["PartOf"] and not row["BindsTo"], "SERVICE_NOT_STABLE")
            pid = int(row["MainPID"])
            require(pid == expected["pid"] and pid != os.getpid(), "SERVICE_PID")
            process = Path("/proc") / str(pid)
            fields = (process / "stat").read_text().rpartition(")")[2].split()
            require(len(fields) > 19 and fields[0] not in {"Z", "X"}, "PROCESS_STATE")
            executable = (process / "exe").resolve(strict=True)
            require(str(executable) == expected["exe"], "PROCESS_EXECUTABLE")
            checked_path(executable, trusted=True)
            fragment = checked_path(row["FragmentPath"], trusted=True)
            groups = [line[3:] for line in (process / "cgroup").read_text().splitlines()
                      if line.startswith("0::")]
            require(groups == [row["ControlGroup"]] and not overlap(own[0], groups[0]),
                    "HELPER_SOURCE_CGROUP_OVERLAP")
            status = dict(line.split(":", 1) for line in (process / "status").read_text().splitlines()
                          if ":" in line)
            uid, gid = list(map(int, status["Uid"].split())), list(map(int, status["Gid"].split()))
            require(uid == [expected["uid"]] * 4 and gid == [expected["gid"]] * 4,
                    "PROCESS_OWNER")
            command = (process / "cmdline").read_bytes()
            require(0 < len(command) <= 65536, "COMMAND_BOUND")
            if name == "app":
                args = command.rstrip(b"\0").split(b"\0")
                require(args.count(b"-jar") == 1 and args.index(b"-jar") + 1 < len(args)
                        and args[args.index(b"-jar") + 1].decode() == self.attachment["app_jar"],
                        "PROCESS_JAR")
            observed[name] = {"unit": expected["unit"], "pid": pid,
                "start_ticks": int(fields[19]), "invocation_id": row["InvocationID"],
                "cgroup": groups[0], "exe": str(executable),
                "exe_sha256": hash_file(executable, lambda: None, 256 * 1024**2)[1],
                "cmdline_sha256": digest(command), "unit_fragment": str(fragment),
                "unit_sha256": hash_file(fragment, lambda: None, 1024**2)[1],
                "uid": uid[0], "gid": gid[0], "netns_inode": (process / "ns/net").stat().st_ino}
        jar = checked_path(self.attachment["app_jar"], trusted=True)
        require(hash_file(jar, lambda: None, 256 * 1024**2)[1] == self.attachment["app_jar_sha256"],
                "APP_JAR_PIN")
        return observed

    def capture(self) -> dict:
        for key, path in self.inputs.items():
            require(load(checked_path(path, private=True)) == self.loaded[key], "INPUT_CHANGED")
        validate_source_custody()
        return online.execute(self.config, self.inputs["config"], self.inputs["online_approval"],
                              expected_approval_sha256=digest(encoded(self.inner)), owner=getattr(self, "owner", None))

    @staticmethod
    def cleanup() -> None:
        # online.execute의 finally가 자신이 만든 reader group만 정리한다.
        # adapter는 동기 read-only show 외 별도 child/service를 소유하지 않는다.
        return None


def validate_source_custody() -> None:
    """Require a frozen source-only bundle, without executable cache ambiguity."""
    directory = Path(__file__).resolve().parent
    for filename in MODULES:
        name = filename.removesuffix(".py")
        module = sys.modules.get(__name__ if name == "live_service_capture" else name)
        expected = directory / filename
        require(Path(getattr(module, "__file__", "")).resolve() == expected
                and expected.is_file(), "MODULE_SOURCE_ORIGIN")
        spec = getattr(module, "__spec__", None)
        require(spec is None or Path(spec.origin).resolve() == expected, "MODULE_SOURCE_ORIGIN")
        cached = getattr(module, "__cached__", None)
        paths = [expected.with_suffix(".pyc")]
        if cached is not None:
            paths.append(Path(cached))
        require(not any(path.exists() or path.is_symlink() for path in paths), "MODULE_CACHE_AMBIGUOUS")


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--execute", action="store_true")
    for name in ("config", "online-approval", "attachment", "authority", "pins"):
        parser.add_argument("--" + name, type=Path)
    args = parser.parse_args(argv)
    if not args.execute:
        print('{"status":"DISABLED","side_effects":false}')
        return 0
    try:
        require(sys.platform == "linux" and os.geteuid() == 0, "LINUX_ROOT_REQUIRED")
        validate_source_custody()
        inputs = {key: getattr(args, key) for key in
                  ("config", "online_approval", "attachment", "authority", "pins")}
        require(all(isinstance(path, Path) for path in inputs.values()), "INPUT_REFERENCES_REQUIRED")
        loaded = {key: load(checked_path(path, private=True)) for key, path in inputs.items()}
        attachment = loaded["attachment"]
        inputs["runtime_proof"] = Path(attachment["runtime_proof"]["path"])
        loaded["runtime_proof"] = load(checked_path(inputs["runtime_proof"], private=True))
        directory = Path(__file__).resolve().parent
        names = outer.MODULES if outer.enabled(loaded["config"]) else MODULES
        required = {str(directory / name) for name in names} | {str(Path(sys.executable).resolve())}
        require(set(loaded["pins"]) == required, "EXACT_CODE_PINS_REQUIRED")
        for name in required:
            checked_path(name, trusted=True)
        config, inner = loaded["config"], loaded["online_approval"]
        ledger = Path(config["state_dir"]) / "live-service-ledger"
        checked_path(ledger, private=True)
        value = attach_once(ledger, config, inner, attachment, loaded["authority"],
                            loaded["runtime_proof"], loaded["pins"],
                            NativeBackend(config, inner, attachment, inputs, loaded))
        print(encoded(value).decode())
        return 0
    except (GateError, OSError, ValueError, TypeError, KeyError, AttributeError, RecursionError,
            subprocess.SubprocessError):
        print('{"status":"HOLD","code":"LIVE_SERVICE_CAPTURE_STOPPED"}')
        return 2


if __name__ == "__main__":
    raise SystemExit(main())

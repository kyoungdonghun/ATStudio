#!/usr/bin/python3
"""Bounded online capture through the disabled-by-default private Java mailbox.

No operation occurs on import or on the default plan command. Native execution is
Linux/root only and requires new exact online approval and independently pinned proofs.
The cold executor and its authority are deliberately unchanged.
"""
from __future__ import annotations

import argparse
import copy
import json
import os
from pathlib import Path
import re
import signal
import socket
import stat
import sys
import time
import uuid
import outer_recovery as outer

from backup_protocol import (GateError, backup_snapshot, digest, encoded, exact_id,
                             linux_path, overlap, require, snapshot_nodes, timestamp,
                             utc_now, validate_config, verify_nodes)
from backup_runtime import (LinuxMonitor, LinuxRunner, atomic_json, checked_path,
                            checked_setting_source,
                            create_bytes, hash_file, inventory, operations_lock,
                            read_json, restic_command, restic_environment)

PROTOCOL = "ats-online-v1"
IDENT = re.compile(r"[A-Za-z_][A-Za-z0-9_]{0,63}\Z")
MAX_JSON = 16 * 1024**2


def uuid_id(value):
    require(isinstance(value, str), "CAPTURE_ID")
    try:
        require(str(uuid.UUID(value)) == value, "CAPTURE_ID")
    except (ValueError, AttributeError):
        raise GateError("CAPTURE_ID") from None
    return value


def config_subject(config):
    value = copy.deepcopy(config)
    value["online"].pop("proofs", None)
    return digest(encoded(value))


def validate_online(config):
    validate_config(config)  # Existing path, resource, repository and cost bounds.
    require(config.get("capture_mode") == PROTOCOL and
            config.get("maintenance_mode") == "live-resume" and
            not config.get("cold_host"), "ONLINE_PROFILE_REQUIRED")
    online = config["online"]
    require(online.get("version") == 1, "ONLINE_VERSION")
    linux_path(online["bridge_dir"])
    for path in [config[k] for k in ("public_root", "private_root", "release_root",
                                    "state_dir", "work_dir", "cache_dir", "temp_dir")] + config["settings"]:
        require(not overlap(path, online["bridge_dir"]), "BRIDGE_OVERLAP")
    for key in ("app_uid", "app_gid"):
        require(type(online.get(key)) is int and online[key] > 0, "APP_IDENTITY")
    require(type(online.get("bridge_seconds")) is int and 0 < online["bridge_seconds"] <= 330,
            "BRIDGE_BOUND")
    require(type(online.get("drain_seconds")) is int and 0 <= online["drain_seconds"] <= 300,
            "DRAIN_BOUND")
    require(online["bridge_seconds"] > online["drain_seconds"], "BRIDGE_DRAIN_BOUND")
    require(IDENT.fullmatch(online.get("schema", "")) is not None, "SCHEMA_IDENTIFIER")
    tables = online.get("tables")
    require(isinstance(tables, list) and bool(tables) and tables == sorted(set(tables))
            and all(isinstance(t, str) and IDENT.fullmatch(t) for t in tables), "TABLE_ALLOWLIST")
    for key in ("machine_sha256", "boot_sha256", "mysql_sha256", "mysqldump_sha256"):
        exact_id(online.get(key))
    for key in ("mysql_binary", "mysqldump_binary"):
        linux_path(online[key])
    require(set(online.get("proofs", {})) == {"target", "repository_acl", "recovery_access", "cost"},
            "ONLINE_PROOFS_REQUIRED")
    for proof in online["proofs"].values():
        linux_path(proof["path"])
        exact_id(proof["sha256"])
    modules = online.get("module_sha256", {})
    expected_modules = outer.MODULES if outer.enabled(config) else {"backup_protocol.py", "backup_runtime.py", "online_capture.py"}
    require(set(modules) == expected_modules, "MODULE_PINS")
    if outer.enabled(config): outer.validate_config(config)
    for value in modules.values(): exact_id(value)
    bindings = online.get('source_ownership', {})
    if bindings:
        require(set(bindings) == {'/var/lib/atstudio/tmp'} and '/var/lib/atstudio/tmp' in config['settings'], 'ONLINE_SOURCE_SCOPE')
        row = bindings['/var/lib/atstudio/tmp']
        require(set(row) == {'uid', 'gid', 'mode', 'device', 'inode'} and
                all(type(v) is int and v >= 0 for v in row.values()) and row['uid'] == online['app_uid'] and
                row['gid'] == online['app_gid'] and row['mode'] == 0o700, 'ONLINE_SOURCE_METADATA')


def validate_authority(config, approval, now):
    require(approval.get("operation") == "online-capture" and
            approval.get("config_sha256") == digest(encoded(config)), "EXACT_ONLINE_APPROVAL")
    require(re.fullmatch(r"[a-f0-9]{32}", approval.get("run_id", "")) is not None, "RUN_ID")
    begin, end = timestamp(approval.get("not_before")), timestamp(approval.get("not_after"))
    require(begin <= now < end and end - begin <= config["limits"]["seconds"], "ONLINE_WINDOW")
    require(approval.get("release_authorized") is True, "RELEASE_APPROVAL")


def verify_proofs(config, now, run_id, prior_state_sha256, prior_updated_at=None):
    """Consume existing reviewed evidence; never issue provider/cost assertions."""
    online, limits = config["online"], config["limits"]
    proofs = {}
    for kind, ref in online["proofs"].items():
        path = checked_path(ref["path"], private=True)
        with path.open("rb") as stream: raw = stream.read(65537)
        require(len(raw) <= 65536 and digest(raw) == ref["sha256"], "PROOF_HASH")
        proof = json.loads(raw)
        require(proof.get("version") == 1 and proof.get("kind") == kind and
                proof.get("scope") == "ONE_ONLINE_CAPTURE" and
                proof.get("subject_sha256") == config_subject(config) and
                proof.get("repository_id") == config["repository_id"] and
                proof.get("machine_sha256") == online["machine_sha256"] and
                proof.get("boot_sha256") == online["boot_sha256"], "PROOF_BINDING")
        exact_id(proof.get("source_evidence_sha256"))
        start, end = timestamp(proof.get("observed_at")), timestamp(proof.get("expires_at"))
        require(start <= now and now + limits["seconds"] <= end and end - start <= 21600,
                "PROOF_STALE")
        proofs[kind] = proof
    require(proofs["repository_acl"].get("access") == "PRIVATE_VERIFIED", "REPOSITORY_ACL")
    recovery = proofs["recovery_access"]
    require(recovery.get("operation") == "EXACT_SNAPSHOT_READ_VERIFIED", "RECOVERY_PROOF")
    exact_id(recovery.get("snapshot_id"))
    target = proofs["target"]
    require(all(target.get(k) is True for k in ("app_tuple_matched", "sole_app_writer",
            "all_media_mutations_fenced", "schema_release_settings_rotation_fenced",
            "no_uncoordinated_file_writers", "no_overlapping_deployment")), "TARGET_SCOPE")
    cost = proofs["cost"]
    require(re.fullmatch(r"[a-f0-9]{32}", run_id) is not None and cost.get("run_id") == run_id and
            cost.get("prior_state_sha256") == exact_id(prior_state_sha256), "COST_ATTEMPT_BINDING")
    require(prior_updated_at is None or timestamp(cost["observed_at"]) > timestamp(prior_updated_at),
            "COST_PREDECESSOR_STALE")
    require(type(cost.get("upper_bound_krw")) is int and type(cost.get("approved_limit_krw")) is int
            and 0 <= cost["upper_bound_krw"] == limits["projected_cost_krw"]
            <= cost["approved_limit_krw"] == limits["cost_limit_krw"]
            and cost.get("unknown_components") == [], "COST_AUTHORITY")
    require(type(cost.get("new_bytes_bound")) is int and cost["new_bytes_bound"] >=
            sum(limits[k] for k in ("source_bytes", "dump_bytes", "metadata_bytes")), "COST_NEW_BYTES")
    require(type(cost.get("retrieval_bytes_bound")) is int and
            cost["retrieval_bytes_bound"] >= limits["retrieval_bytes"], "COST_RETRIEVAL")


def safe_member(root, key):
    require(isinstance(key, str) and key and not any(c in key for c in "\\\x00\r\n:")
            and not key.startswith("/") and all(p not in {"", ".", ".."} for p in key.split("/")),
            "REFERENCE_KEY")
    path = root.joinpath(*key.split("/"))
    require(path.is_relative_to(root), "REFERENCE_ESCAPE")
    current = root
    require(not current.is_symlink(), "REFERENCE_SYMLINK")
    for component in key.split("/"):
        current = current / component
        require(not current.is_symlink(), "REFERENCE_SYMLINK")
    return path


class Mailbox:
    """Fixed private paths only; lost replies are HOLD, never a new mutation retry."""
    def __init__(self, directory, tick, seconds, *, owner=None):
        self.directory, self.tick, self.seconds = Path(directory), tick, seconds
        self.owner = owner

    def publish(self, path, data):
        require(path.parent.is_dir() and not path.exists() and not path.is_symlink(), "MAILBOX_PUBLICATION")
        temporary = path.with_name(path.name + ".pending-" + uuid.uuid4().hex)
        create_bytes(temporary, data)
        try:
            if self.owner is not None:
                os.chown(temporary, *self.owner, follow_symlinks=False)
            os.link(temporary, path)  # Atomic create-if-absent, never replace a raced request.
        finally:
            temporary.unlink()
        if os.name == "posix":
            fd = os.open(path.parent, os.O_RDONLY | os.O_DIRECTORY)
            try: os.fsync(fd)
            finally: os.close(fd)

    def request(self, action, capture_id, *, request_id=None, **fields):
        uuid_id(capture_id)
        request_id = request_id or str(uuid.uuid4())
        uuid_id(request_id)
        request = {"version": 1, "protocol": PROTOCOL, "request_id": request_id,
                   "action": action, **fields}
        if action != "FREEZE": request["capture_id"] = capture_id
        else: require(request_id == capture_id, "FREEZE_REQUEST_ID")
        request_bytes = encoded(request)
        self.publish(self.directory / "requests" / (request_id + ".json"), request_bytes)
        response = self.directory / "responses" / (request_id + ".json")
        deadline = time.monotonic() + self.seconds
        while not response.exists():
            self.tick()
            require(time.monotonic() < deadline, "BRIDGE_REPLY_UNKNOWN")
            time.sleep(.02)
        require(not response.is_symlink(), "BRIDGE_REPLY_SYMLINK")
        reply = read_json(response, MAX_JSON)
        require(reply.get("version") == 1 and reply.get("protocol") == PROTOCOL and
                reply.get("request_id") == request_id and reply.get("capture_id") == capture_id and
                reply.get("request_sha256") == digest(request_bytes),
                "BRIDGE_REPLY_BINDING")
        require(reply.get("status") != "REJECTED", "BRIDGE_REJECTED")
        return reply

    def reference_manifest(self, capture_id, reply):
        relative = "captures/" + uuid_id(capture_id) + "/manifest.json"
        require(reply.get("manifest_file") == relative, "REFERENCE_MANIFEST_PATH")
        path = safe_member(self.directory, relative)
        with path.open("rb") as stream:
            raw = stream.read(MAX_JSON + 1)
        require(len(raw) <= MAX_JSON and digest(raw) == exact_id(reply.get("manifest_sha256")), "REFERENCE_MANIFEST_HASH")
        manifest = json.loads(raw)
        require(manifest.get("version") == 1 and manifest.get("protocol") == PROTOCOL and
                manifest.get("capture_id") == capture_id and isinstance(manifest.get("refs"), list),
                "REFERENCE_MANIFEST_BINDING")
        return raw, manifest

    def publish_receipt(self, capture_id, receipt):
        path = safe_member(self.directory, "captures/" + uuid_id(capture_id) + "/verified-receipt.json")
        data = encoded(receipt)
        self.publish(path, data)
        return digest(data)


class OnlineCapture:
    def __init__(self, config, approval, runner, mailbox, tick):
        self.config, self.approval, self.runner, self.mailbox, self.tick = config, approval, runner, mailbox, tick
        self.directory = Path(config["work_dir"]) / approval["run_id"]
        self.state_path = Path(config["state_dir"]) / "online-state.json"
        self.state = read_json(self.state_path) if self.state_path.exists() else {"version": 1, "last_good": None}
        require(not self.state_path.is_symlink() and self.state.get("version") == 1, "ONLINE_STATE")
        require(self.state.get("stage") in {None, "COMPLETE", "RECONCILED"}, "PRIOR_ONLINE_HOLD")
        self.prior_state = dict(self.state)
        self.state = {"version": 1, "last_good": self.prior_state.get("last_good")}
        self.capture_id = str(uuid.UUID(approval["run_id"]))
        self.snapshot = None

    def stage(self, name, **fields):
        self.state.update(stage=name, run_id=self.approval["run_id"], capture_id=self.capture_id,
                          updated_at=utc_now(), **fields)
        atomic_json(self.state_path, self.state)

    def restic(self, args, cap, **kwargs):
        return self.runner.run(restic_command(self.config, args), cap, **kwargs)

    def query_tables(self):
        online = self.config["online"]
        sql = ("SELECT TABLE_NAME,TABLE_TYPE,COALESCE(ENGINE,'') FROM information_schema.TABLES "
               "WHERE TABLE_SCHEMA='" + online["schema"] + "' ORDER BY TABLE_NAME;")
        reply = self.runner.run([online["mysql_binary"], "--defaults-extra-file=" + self.config["mysql_defaults_file"],
                                 "--batch", "--skip-column-names", "--execute=" + sql], MAX_JSON)
        require(reply.code == 0 and reply.data.decode("ascii").splitlines() ==
                [table + "\tBASE TABLE\tInnoDB" for table in online["tables"]], "ONLINE_TABLE_INVENTORY")

    def dump(self):
        online, limits = self.config["online"], self.config["limits"]
        self.query_tables()
        dump = self.directory / "database.sql"
        argv = [online["mysqldump_binary"], "--defaults-extra-file=" + self.config["mysql_defaults_file"],
                "--single-transaction", "--quick", "--hex-blob", "--routines", "--events", "--triggers",
                "--set-gtid-purged=OFF", "--no-tablespaces", "--skip-extended-insert", "--skip-add-drop-table",
                "--tz-utc", "--default-character-set=utf8mb4", "--databases", online["schema"]]
        reply = self.runner.run(argv, limits["dump_bytes"], dump)
        require(reply.code == 0 and reply.size > 0, "ONLINE_DUMP_EXIT")
        size, sha = hash_file(dump, self.tick, limits["dump_bytes"])
        require((reply.size, reply.sha256) == (size, sha), "ONLINE_DUMP_HASH")
        tables = []
        with dump.open("rb") as stream:
            for line in stream:
                found = re.match(rb"CREATE TABLE `([A-Za-z_][A-Za-z0-9_]*)`", line)
                if found: tables.append(found.group(1).decode("ascii"))
        require(tables == online["tables"], "ONLINE_DUMP_TABLES")
        self.query_tables()  # Schema only; live rows are intentionally allowed to change.
        return dump, sha

    def files(self, references, dump, reference_copy):
        config, limits = self.config, self.config["limits"]
        if config.get('online', {}).get('source_ownership'):
            for source in config['settings']: checked_setting_source(config, source)
        seen, files = set(), []
        require(len(references["refs"]) <= limits["max_entries"], "REFERENCE_LIMIT")
        for reference in references["refs"]:
            require(set(reference) == {"root", "key", "required", "present"} and
                    reference["root"] in {"PUBLIC", "PRIVATE"} and
                    type(reference["required"]) is bool and type(reference["present"]) is bool,
                    "REFERENCE_SHAPE")
            identity = (reference["root"], reference["key"])
            require(identity not in seen, "REFERENCE_DUPLICATE")
            seen.add(identity)
            path = safe_member(Path(config[reference["root"].lower() + "_root"]), reference["key"])
            require(reference["present"] or not reference["required"], "REQUIRED_REFERENCE_MISSING")
            if reference["present"]:
                require(path.is_file(), "REFERENCE_NOT_REGULAR")
                require(path.stat().st_dev == Path(config[reference["root"].lower() + "_root"]).stat().st_dev,
                        "NESTED_MOUNT_REVIEW_REQUIRED")
                files.append(path)
        roots = [Path(config["release_root"]), *map(Path, config["settings"])]
        sources = [*files, *roots, dump, reference_copy]
        members = inventory(sources, self.tick, limits["max_entries"],
                            limits["source_bytes"] + limits["dump_bytes"] + limits["metadata_bytes"],
                            limits["metadata_bytes"], {str(p): p.stat().st_dev for p in sources})
        require(sum(item.get("size", 0) for path, item in members.items()
                    if path not in {str(dump), str(reference_copy)}) <= limits["source_bytes"], "SOURCE_BUDGET")
        return members

    def run(self):
        old_good = self.state.get("last_good")
        self.directory.mkdir(mode=0o700, exist_ok=False)
        try:
            create_bytes(self.directory / "previous-state.json", encoded(self.prior_state))
            self.stage("PRECHECK", config_sha256=digest(encoded(self.config)))
            reply = self.restic(["cat", "config"], 65536)
            require(reply.code == 0, "REPOSITORY_EXIT")
            repository = json.loads(reply.data)
            require(repository.get("id") == self.config["repository_id"] and repository.get("version") == 2,
                    "REPOSITORY_IDENTITY")
            self.stage("FREEZE_REQUESTED")
            frozen = self.mailbox.request("FREEZE", self.capture_id, request_id=self.capture_id,
                                          timeout_seconds=self.config["online"]["drain_seconds"])
            require(frozen.get("status") == "FROZEN", "FREEZE_NOT_CONFIRMED")
            raw, references = self.mailbox.reference_manifest(self.capture_id, frozen)
            ref_sha = digest(raw)
            reference_copy = self.directory / "references.json"
            create_bytes(reference_copy, raw)
            self.stage("DUMP_FROZEN", manifest_sha256=ref_sha)
            dump, dump_sha = self.dump()
            seal = self.mailbox.request("SEAL", self.capture_id, manifest_sha256=ref_sha, dump_sha256=dump_sha)
            require(seal.get("status") == "PINNED" and seal.get("manifest_sha256") == ref_sha and
                    seal.get("dump_sha256") == dump_sha, "SEAL_NOT_CONFIRMED")
            self.stage("PINNED", dump_sha256=dump_sha)
            # Hash potentially large media only after mutations resume under durable pins.
            members = self.files(references, dump, reference_copy)
            manifest = {"version": 1, "protocol": PROTOCOL, "capture_id": self.capture_id,
                        "manifest_sha256": ref_sha, "dump_sha256": dump_sha,
                        "config_sha256": digest(encoded(self.config)), "members": members}
            content = encoded(manifest)
            require(len(content) <= self.config["limits"]["metadata_bytes"], "CONTENT_MANIFEST_LIMIT")
            content_path = self.directory / "manifest.json"
            create_bytes(content_path, content)
            row = inventory([content_path], self.tick, 1, len(content), len(content) + 1024,
                            {str(content_path): content_path.stat().st_dev})
            expected = {**members, **row}
            selected = [path for path, item in expected.items() if item["type"] == "file"]
            require(sum(expected[path]["size"] for path in selected) <= self.config["limits"]["retrieval_bytes"],
                    "FULL_RETRIEVAL_BUDGET")
            source_list = self.directory / "files.txt"
            require(all(not any(c in path for c in "\r\n\x00") for path in selected), "FILE_LIST_PATH")
            create_bytes(source_list, ("\n".join(sorted(selected)) + "\n").encode())
            self.stage("TRANSFER")
            reply = self.restic(["backup", "--json", "--read-concurrency", "1", "--host",
                                 self.config["logical_host"], "--tag", "ats-prod,online-v1," + self.config["class"] +
                                 ",capture:" + self.capture_id + ",run:" + self.approval["run_id"],
                                 "--force", "--files-from-verbatim", str(source_list)],
                                min(self.config["limits"]["metadata_bytes"], 8 * 1024**2))
            self.snapshot = backup_snapshot(reply.data, reply.code)
            require(not old_good or self.snapshot != old_good.get("snapshot_id"), "SNAPSHOT_NOT_NEW")
            self.stage("VERIFY", snapshot_id=self.snapshot)
            reply = self.restic(["ls", "--json", self.snapshot], self.config["limits"]["metadata_bytes"])
            require(reply.code == 0, "SNAPSHOT_INVENTORY_EXIT")
            # Empty directories are not capture members when an exact file list is used.
            verify_nodes({p: expected[p] for p in selected}, snapshot_nodes(reply.data, self.snapshot))
            for path in selected:
                row = expected[path]
                reply = self.restic(["dump", self.snapshot, path], row["size"], hash_only=True)
                require(reply.code == 0 and (reply.size, reply.sha256) == (row["size"], row["sha256"]),
                        "SNAPSHOT_CONTENT_HASH")
            require(not self.runner.active, "READERS_NOT_ENDED")
            require(self.files(references, dump, reference_copy) == members, "PINNED_SOURCE_CHANGED")
            require(Path(self.config["release_link"]).resolve(strict=True) == Path(self.config["release_root"]),
                    "RELEASE_CHANGED")
            verified = {"version": 1, "protocol": PROTOCOL, "status": "VERIFIED",
                        "run_id": self.approval["run_id"], "capture_id": self.capture_id,
                        "manifest_sha256": ref_sha, "dump_sha256": dump_sha,
                        "content_manifest_sha256": digest(content), "snapshot_id": self.snapshot,
                        "repository_id": self.config["repository_id"], "verified_at": utc_now(),
                        "helpers_ended": True, "inventory_verified": True,
                        "content_verified": True, "restore_verified": False}
            create_bytes(self.directory / "verified-receipt.json", encoded(verified))
            create_bytes(self.directory / "restore-preparation.json", encoded({
                "version": 1, "snapshot_id": self.snapshot, "repository_id": self.config["repository_id"],
                "capture_id": self.capture_id, "dump_sha256": dump_sha,
                "content_manifest_sha256": digest(content), "execution_authorized": False,
                "restore_verified": False, "required_controls": ["new_isolated_target", "fresh_database",
                    "new_roots", "egress_denied", "services_disabled", "timers_disabled",
                    "scheduler_suppression_proved", "no_effective_live_credentials", "exact_target_approved"]}))
            receipt_sha = self.mailbox.publish_receipt(self.capture_id, verified)
            self.stage("RELEASE_REQUESTED", receipt_sha256=receipt_sha)
            released = self.mailbox.request("RELEASE", self.capture_id, manifest_sha256=ref_sha,
                                            dump_sha256=dump_sha, receipt_sha256=receipt_sha)
            require(released.get("status") == "RELEASED", "RELEASE_NOT_CONFIRMED")
            self.state["last_good"] = verified
            self.stage("COMPLETE")
            return {"status": "COMPLETE", "restore": "NOT_RUN", "capture_id": self.capture_id}
        except BaseException as error:
            code = str(error) if isinstance(error, GateError) else "ONLINE_CAPTURE_FAILURE"
            try: self.runner.stop_owned()
            except BaseException: code = "READERS_NOT_ENDED"
            self.state["last_good"] = old_good
            self.stage("HOLD", failure_code=code, failed_stage=self.state.get("stage"))
            # Never release on timeout, unknown transfer result, interrupted reader or failed receipt.
            raise GateError(code) from None


def reconcile(config, approval, mailbox):
    """Explicit local evidence reconciliation. Never discovers or retries a transfer."""
    state_path = Path(config["state_dir"]) / "online-state.json"
    with state_path.open("rb") as stream: raw = stream.read(65537)
    require(len(raw) <= 65536 and not state_path.is_symlink(), "RECOVERY_STATE")
    state = json.loads(raw)
    now = time.time()
    require(approval.get("operation") == "online-reconcile" and state.get("stage") == "HOLD" and
            approval.get("config_sha256") == state.get("config_sha256") == digest(encoded(config)) and
            approval.get("state_sha256") == digest(raw) and
            approval.get("run_id") == state.get("run_id") and
            approval.get("capture_id") == state.get("capture_id"), "RECOVERY_BINDING")
    capture = uuid_id(state["capture_id"])
    require(str(uuid.UUID(state["run_id"])) == capture, "RECOVERY_RUN")
    recovery_id = uuid_id(approval.get("recovery_id"))
    begin, end = timestamp(approval.get("not_before")), timestamp(approval.get("not_after"))
    require(begin <= now < end and end - begin <= 600, "RECOVERY_WINDOW")
    proof = approval.get("termination", {})
    require(proof.get("helpers_ended") is True and proof.get("readers_ended") is True and
            proof.get("capture_id") == capture and proof.get("machine_sha256") == config["online"]["machine_sha256"] and
            proof.get("boot_sha256") == config["online"]["boot_sha256"], "RECOVERY_TERMINATION")
    exact_id(proof.get("source_evidence_sha256"))
    require(begin <= timestamp(proof.get("observed_at")) <= now, "RECOVERY_TERMINATION_STALE")
    action = approval.get("resolution")
    require(action in {"ABANDON_BEFORE_SEAL", "CONFIRM_VERIFIED_RELEASE", "CONFIRM_NO_CAPTURE"}, "RECOVERY_RESOLUTION")
    work = Path(config["work_dir"]) / state["run_id"]
    recovery = work / "recovery" / recovery_id
    recovery.mkdir(mode=0o700, parents=True, exist_ok=True)
    intent = encoded({"approval": approval, "prior_state": state})
    if (recovery / "intent.json").exists():
        require((recovery / "intent.json").read_bytes() == intent, "RECOVERY_INTENT_REUSE")
    else: create_bytes(recovery / "intent.json", intent)
    # Match every original mutation claim byte-for-byte. Missing or pending claims cannot be guessed.
    requests = []
    for index, path in enumerate((mailbox.directory / "requests").glob("*.json")):
        mailbox.tick()
        require(index < 10000 and not path.is_symlink(), "RECOVERY_REQUEST_LIMIT")
        request = read_json(path)
        identity = request.get("request_id") if request.get("action") == "FREEZE" else request.get("capture_id")
        if identity != capture or request.get("action") not in {"FREEZE", "SEAL", "RELEASE", "ABORT"}: continue
        claim = mailbox.directory / "claims" / path.name
        require(claim.is_file() and not claim.is_symlink() and claim.read_bytes() == path.read_bytes(), "RECOVERY_CLAIM")
        response = mailbox.directory / "responses" / path.name
        if response.exists():
            row = read_json(response)
            require(not response.is_symlink() and row.get("request_id") == request["request_id"] and
                    row.get("capture_id") == capture and row.get("request_sha256") == digest(path.read_bytes()), "RECOVERY_RESPONSE")
        requests.append(request)
    if action != "CONFIRM_NO_CAPTURE":
        require(any(r["action"] == "FREEZE" and r["request_id"] == capture for r in requests), "RECOVERY_FREEZE_CLAIM")
    status = mailbox.request("STATUS", capture)["status"]
    directory = safe_member(mailbox.directory, "captures/" + capture)
    if action == "CONFIRM_NO_CAPTURE":
        require(status == "EMPTY" and not directory.exists() and not state.get("snapshot_id") and
                not any(r["action"] != "FREEZE" for r in requests), "RECOVERY_NO_EFFECT")
        if state.get("failed_stage") == "PRECHECK":
            require(not requests, "RECOVERY_NO_EFFECT")
        else:
            require(state.get("failed_stage") == "FREEZE_REQUESTED" and len(requests) == 1 and
                    requests[0]["request_id"] == capture, "RECOVERY_NO_EFFECT")
            rejected = read_json(mailbox.directory / "responses" / (capture + ".json"))
            require(rejected.get("status") == "REJECTED", "RECOVERY_NO_EFFECT")
        receipt_sha = None
    elif action == "ABANDON_BEFORE_SEAL":
        require(state.get("failed_stage") in {"FREEZE_REQUESTED", "DUMP_FROZEN"} and
                not any(r["action"] in {"SEAL", "RELEASE"} for r in requests) and
                not state.get("snapshot_id") and not (directory / "seal.json").exists(), "RECOVERY_TRANSFER_UNCERTAIN")
        receipt_path = directory / "abandon-receipt.json"
        if receipt_path.exists():
            receipt = read_json(receipt_path)
            require(receipt.get("capture_id") == capture and receipt.get("run_id") == state["run_id"] and
                    receipt.get("helpers_ended") is True and receipt.get("readers_ended") is True and
                    receipt.get("status") == "ABANDONED", "RECOVERY_ABANDON_RECEIPT")
        else:
            require(status == "FROZEN", "RECOVERY_NOT_FROZEN")
            receipt = {"version": 1, "protocol": PROTOCOL, "capture_id": capture, "run_id": state["run_id"],
                       "status": "ABANDONED", "helpers_ended": True, "readers_ended": True, "reconciled_at": utc_now()}
            mailbox.publish(receipt_path, encoded(receipt))
        receipt_sha = digest(receipt_path.read_bytes())
        if status == "FROZEN":
            reply = mailbox.request("ABORT", capture, receipt_sha256=receipt_sha)
            require(reply.get("status") == "ABORTED", "RECOVERY_ABORT_UNKNOWN")
        else: require(status == "EMPTY", "RECOVERY_NOT_EMPTY")
        marker = read_json(directory / "abort.json")
    else:
        require(state.get("failed_stage") == "RELEASE_REQUESTED", "RECOVERY_TRANSFER_UNCERTAIN")
        receipt_path = work / "verified-receipt.json"
        require(not receipt_path.is_symlink(), "RECOVERY_RECEIPT")
        with receipt_path.open("rb") as stream: receipt_raw = stream.read(65537)
        require(len(receipt_raw) <= 65536, "RECOVERY_RECEIPT")
        receipt = json.loads(receipt_raw)
        require(digest(receipt_raw) == state.get("receipt_sha256") and
                receipt.get("capture_id") == capture and receipt.get("run_id") == state["run_id"] and
                receipt.get("snapshot_id") == state.get("snapshot_id") and
                receipt.get("repository_id") == config["repository_id"] and
                receipt.get("manifest_sha256") == state.get("manifest_sha256") and
                receipt.get("dump_sha256") == state.get("dump_sha256") and
                receipt.get("content_manifest_sha256") == hash_file(work / "manifest.json", mailbox.tick,
                                                                     config["limits"]["metadata_bytes"])[1] and
                all(receipt.get(k) is True for k in ("helpers_ended", "inventory_verified", "content_verified")) and
                receipt.get("restore_verified") is False, "RECOVERY_VERIFIED_RECEIPT")
        receipt_sha = digest(receipt_raw)
        require((directory / "verified-receipt.json").read_bytes() == receipt_raw, "RECOVERY_PUBLISHED_RECEIPT")
        if status == "PINNED":
            reply = mailbox.request("RELEASE", capture, manifest_sha256=state["manifest_sha256"],
                                    dump_sha256=state["dump_sha256"], receipt_sha256=receipt_sha)
            require(reply.get("status") == "RELEASED", "RECOVERY_RELEASE_UNKNOWN")
        else: require(status == "EMPTY", "RECOVERY_NOT_EMPTY")
        marker = read_json(directory / "release.json")
    if action != "CONFIRM_NO_CAPTURE":
        require(marker.get("capture_id") == capture and marker.get("receipt_sha256") == receipt_sha, "RECOVERY_FINAL_BINDING")
    require(mailbox.request("STATUS", capture)["status"] == "EMPTY", "RECOVERY_FINAL_BINDING")
    result = {"status": "RECONCILED", "resolution": action, "capture_id": capture,
              "receipt_sha256": receipt_sha, "recovery_id": recovery_id, "restore_verified": False}
    if (recovery / "result.json").exists():
        require((recovery / "result.json").read_bytes() == encoded(result), "RECOVERY_RESULT_REUSE")
    else: create_bytes(recovery / "result.json", encoded(result))
    updated = {**state, "stage": "RECONCILED", "recovery_id": recovery_id, "updated_at": utc_now()}
    if action == "CONFIRM_VERIFIED_RELEASE": updated["last_good"] = receipt
    atomic_json(state_path, updated)
    return result


def execute_reconcile(config, config_path, approval_path, *, owner=None, inner_approval=None):
    require(sys.platform == "linux" and os.geteuid() == 0, "LINUX_ROOT_REQUIRED")
    validate_online(config)
    checked_path(config_path, private=True)
    checked_path(approval_path, private=True)
    approval = read_json(approval_path)
    if inner_approval is not None:
        outer.require_owner(owner, config)
        require(approval.get("inner_approval") == inner_approval, "EXACT_RECOVERY_HANDOFF")
        approval = inner_approval
    require(socket.gethostname() == config["target_host"], "EXACT_HOST")
    online = config["online"]
    require(digest(Path("/etc/machine-id").read_bytes().strip()) == online["machine_sha256"] and
            digest(Path("/proc/sys/kernel/random/boot_id").read_bytes().strip()) == online["boot_sha256"], "HOST_IDENTITY")
    for key in ("state_dir", "work_dir"): checked_path(config[key], private=True)
    for filename, expected in online["module_sha256"].items():
        path = checked_path(Path(__file__).resolve().with_name(filename), trusted=True)
        require(hash_file(path, lambda: None, 8 * 1024**2)[1] == expected, "MODULE_HASH")
    bridge = checked_path(online["bridge_dir"])
    for directory in (bridge, *(bridge / name for name in ("requests", "responses", "claims", "captures"))):
        info = checked_path(directory).stat()
        require(stat.S_ISDIR(info.st_mode) and info.st_uid == online["app_uid"] and
                info.st_gid == online["app_gid"] and stat.S_IMODE(info.st_mode) == 0o700, "BRIDGE_OWNERSHIP")
    deadline = min(time.monotonic() + 600, time.monotonic() + timestamp(approval["not_after"]) - time.time())
    def tick(): require(time.monotonic() < deadline, "RECOVERY_WINDOW")
    with outer.gate(config, owner) as owned:
        require(read_json(config_path) == config, "CONFIG_CHANGED")
        if outer.enabled(config):
            outer.pins(config)
            # The outer recovery owns custody, approval and immutable evidence.
            require(owner is not None and inner_approval is not None, "OUTER_DISPOSITION_REQUIRED")
        return reconcile(config, approval, Mailbox(bridge, tick, online["bridge_seconds"],
                                                  owner=(online["app_uid"], online["app_gid"])))


def execute(config, config_path, approval_path, *, expected_approval_sha256=None, owner=None):
    require(sys.platform == "linux" and os.geteuid() == 0, "LINUX_ROOT_REQUIRED")
    validate_online(config)
    checked_path(config_path, private=True)
    checked_path(approval_path, private=True)
    approval = read_json(approval_path)
    if expected_approval_sha256 is not None:
        require(digest(encoded(approval)) == exact_id(expected_approval_sha256),
                "EXACT_APPROVAL_HANDOFF")
    validate_authority(config, approval, time.time())
    require(socket.gethostname() == config["target_host"], "EXACT_HOST")
    online = config["online"]
    require(digest(Path("/etc/machine-id").read_bytes().strip()) == online["machine_sha256"] and
            digest(Path("/proc/sys/kernel/random/boot_id").read_bytes().strip()) == online["boot_sha256"],
            "HOST_IDENTITY")
    for key in ("state_dir", "work_dir", "cache_dir", "temp_dir"):
        require(checked_path(config[key], private=True).is_dir(), "RUNTIME_DIRECTORY")
    for key in ("password_file", "aws_credentials_file", "mysql_defaults_file"):
        require(checked_path(config[key], private=True).is_file(), "CREDENTIAL_REFERENCE")
    for key in ("public_root", "private_root", "release_root"):
        require(checked_path(config[key]).is_dir(), "SOURCE_DIRECTORY")
    for source in config["settings"]: checked_setting_source(config, source)
    release = Path(config["release_link"])
    checked_path(release.parent, trusted=True)
    require(release.resolve(strict=True) == Path(config["release_root"]), "RELEASE_TARGET")
    for key, pin_owner in (("restic", config), ("hook", config), ("mysql", online), ("mysqldump", online)):
        binary = checked_path(pin_owner[key + "_binary"], trusted=True)
        require(binary.is_file() and os.access(binary, os.X_OK), "BINARY_REQUIRED")
        require(hash_file(binary, lambda: None, 256 * 1024**2)[1] == pin_owner[key + "_sha256"], "BINARY_HASH")
    require(Path(config["hook_binary"]).resolve() == Path(__file__).resolve(), "ONLINE_EXECUTOR_IDENTITY")
    for filename, expected in online["module_sha256"].items():
        module = checked_path(Path(__file__).resolve().with_name(filename), trusted=True)
        require(hash_file(module, lambda: None, 8 * 1024**2)[1] == expected, "MODULE_HASH")
    bridge = checked_path(online["bridge_dir"])
    for directory in (bridge, bridge / "requests", bridge / "responses", bridge / "captures"):
        checked_path(directory)
        info = directory.stat()
        require(directory.is_dir() and info.st_uid == online["app_uid"] and
                info.st_gid == online["app_gid"] and stat.S_IMODE(info.st_mode) == 0o700, "BRIDGE_OWNERSHIP")
    mounts = {config[k] for k in ("public_root", "private_root", "release_root", "state_dir", "work_dir", "cache_dir", "temp_dir")}
    require(mounts | set(config["settings"]) <= set(config["expected_filesystems"]), "MOUNT_COVERAGE")
    with outer.gate(config, owner) as owned:
        require(read_json(config_path) == config and read_json(approval_path) == approval, "INPUT_CHANGED")
        direct = False
        if outer.enabled(config):
            outer.pins(config)
            context = outer.require_owner(owned, config)
            if context["attempt"] is None:
                entry, claim = outer.begin_capture(config, approval, owned)
                direct = True
            outer.call_capture(config, owned)
        validate_authority(config, approval, time.time())
        prior_path = Path(config["state_dir"]) / "online-state.json"
        if prior_path.exists():
            checked_path(prior_path, private=True)
            with prior_path.open("rb") as stream: prior_raw = stream.read(65537)
            require(len(prior_raw) <= 65536, "ONLINE_STATE")
            prior = json.loads(prior_raw)
        else:
            prior_raw, prior = b"", {}
        # Capture-specific cumulative cost/capacity proof, bound under the same shared lock.
        verify_proofs(config, time.time(), approval["run_id"], digest(prior_raw), prior.get("updated_at"))
        monitor = LinuxMonitor(config, timestamp(approval["not_after"]))
        runner = LinuxRunner(monitor, restic_environment(config))
        mailbox = Mailbox(bridge, monitor.tick, online["bridge_seconds"], owner=(online["app_uid"], online["app_gid"]))
        previous = {}
        def interrupted(signum, frame): raise GateError("INTERRUPTED")
        try:
            for sig in (signal.SIGINT, signal.SIGTERM): previous[sig] = signal.signal(sig, interrupted)
            value = OnlineCapture(config, approval, runner, mailbox, monitor.tick).run()
            if direct:
                runner.stop_owned()
                outer.finish_capture(config, owned, {"status": "COMPLETE", "run_id": approval["run_id"],
                    "capture_id": value["capture_id"], "claim_sha256": digest(encoded(claim)),
                    "kind": "direct-inner", "restore_verified": False})
            return value
        finally:
            runner.stop_owned()
            for sig, handler in previous.items(): signal.signal(sig, handler)


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("command", nargs="?", default="plan", choices=("plan", "validate", "execute", "reconcile"))
    parser.add_argument("--config", type=Path)
    parser.add_argument("--approval", type=Path)
    args = parser.parse_args(argv)
    try:
        if args.config is None and args.command == "plan":
            result = {"status": "NOT_CONFIGURED", "side_effects": False}
        else:
            require(args.config is not None, "CONFIG_REQUIRED")
            config = read_json(args.config)
            validate_online(config)
            if args.command in {"execute", "reconcile"}:
                require(args.approval is not None, "APPROVAL_REQUIRED")
                operation = execute if args.command == "execute" else execute_reconcile
                result = operation(config, args.config.absolute(), args.approval.absolute())
            else:
                result = {"status": "CONFIG_VALID_ONLY", "config_sha256": digest(encoded(config)), "side_effects": False}
        print(json.dumps(result, sort_keys=True))
        return 0
    except (GateError, OSError, ValueError, TypeError, KeyError, AttributeError):
        print('{"status":"BLOCKED","code":"ONLINE_HOLD"}')
        return 2


if __name__ == "__main__":
    raise SystemExit(main())

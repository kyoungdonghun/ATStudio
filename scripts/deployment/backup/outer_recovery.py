"""WI-20261106-ATS-003: v2 gates and append-only outer HOLD dispositions.

The protocol remains ats-online-v1. recovery_gate.version=2 identifies the new
executor bundle, not a new Java mailbox protocol. Import is inert.
"""
from __future__ import annotations

import argparse
from contextlib import contextmanager
import json
import os
from pathlib import Path
import re
import stat
import subprocess
import sys
import threading
import time
import uuid

if __name__ == "__main__":
    # Native CLI and imported inner entrypoints must share the same capability registry.
    sys.modules["outer_recovery"] = sys.modules[__name__]

from backup_protocol import GateError, digest, encoded, exact_id, overlap, require, timestamp
from backup_runtime import checked_path, create_bytes, operations_lock
from scheduled_dispatch import load, read, safe_path, sync_dir

MODULES = {"backup_protocol.py", "backup_runtime.py", "online_capture.py",
           "outer_recovery.py", "scheduled_dispatch.py", "live_service_capture.py",
           "online_restore.py", "online_scheduled_dispatch.py"}
RESOLUTIONS = {"NO_CAPTURE", "INNER_COMPLETE", "INNER_RECONCILED", "ORIGINAL_COMPLETE_RECORDED"}
INNER_RESOLUTIONS = {"CONFIRM_NO_CAPTURE", "ABANDON_BEFORE_SEAL", "CONFIRM_VERIFIED_RELEASE"}
_owners = {}  # Identity, PID and thread bound capabilities, never serialized.


def enabled(config):
    return config.get("recovery_gate", {}).get("version") == 2


def ledger(config):
    return Path(config["state_dir"]) / "live-service-ledger"


def state_path(config):
    return Path(config["state_dir"]) / "online-state.json"


def raw_state(config):
    path = state_path(config)
    return read(path, 65536) if path.exists() else b""


def validate_config(config):
    gate = config.get("recovery_gate", {})
    require(set(gate) == {"version", "owner_unit", "source_units", "systemctl_sha256", "python_sha256"}
            and gate["version"] == 2, "V2_GATE_REQUIRED")
    require(re.fullmatch(r"[A-Za-z0-9][A-Za-z0-9_.@:-]{0,127}\.service", gate["owner_unit"]), "OWNER_UNIT")
    require(type(gate["source_units"]) is dict and set(gate["source_units"]) == {"app", "db"}
            and len({gate["owner_unit"], *gate["source_units"].values()}) == 3
            and all(re.fullmatch(r"[A-Za-z0-9][A-Za-z0-9_.@:-]{0,127}\.service", name)
                    for name in gate["source_units"].values()), "SOURCE_UNITS")
    for name in ("systemctl_sha256", "python_sha256"): exact_id(gate[name])
    require(set(config["online"]["module_sha256"]) == MODULES, "V2_BUNDLE_REQUIRED")


def pins(config):
    validate_config(config)
    directory = Path(__file__).resolve().parent
    for filename, expected in config["online"]["module_sha256"].items():
        path = checked_path(directory / filename, trusted=True)
        require(digest(read(path, 8 * 1024**2)) == expected, "MODULE_HASH")
        require(not path.with_suffix(".pyc").exists(), "MODULE_CACHE_AMBIGUOUS")
        name = filename[:-3]
        module = sys.modules.get(name)
        if module is not None:
            require(Path(module.__file__).resolve() == path, "MODULE_SOURCE_ORIGIN")
            cached = getattr(module, "__cached__", None)
            require(not cached or not Path(cached).exists(), "MODULE_CACHE_AMBIGUOUS")
    for path, expected in ((Path(sys.executable).resolve(), config["recovery_gate"]["python_sha256"]),
                           (Path("/usr/bin/systemctl"), config["recovery_gate"]["systemctl_sha256"])):
        checked_path(path, trusted=True)
        require(digest(read(path, 32 * 1024**2)) == expected, "EXECUTABLE_PIN")


def require_owner(owner, config):
    try: value = _owners.get(owner)
    except TypeError: value = None
    require(value is not None and value["pid"] == os.getpid() and value["thread"] == threading.get_ident()
            and value["config"] == digest(encoded(config)), "OWNED_GATE_REQUIRED")
    require(identity(ledger(config) / "operation.guard") == value["guard"], "GUARD_REPLACED")
    require(identity(ledger(config)) == value["ledger"], "LEDGER_REPLACED")
    require(identity(Path(config["lock_file"])) == value["operations"], "OPERATIONS_LOCK_REPLACED")
    return value


@contextmanager
def gate(config, owner=None):
    """Acquire permanent guard then operations flock exactly once; reuse only live capability."""
    if owner is not None:
        require_owner(owner, config)
        yield owner
        return
    root = ledger(config)
    if not enabled(config):
        require(not (root / "operation.guard").exists(), "LEGACY_GATE_INCOMPATIBLE")
        with operations_lock(Path(config["lock_file"])):
            yield None
        return
    validate_config(config)
    checked_path(root, private=True)
    require(root.is_dir(), "LEDGER_REQUIRED")
    expected_ledger = identity(root)
    guard = root / "operation.guard"
    if not guard.exists():
        require(not entries(root), "MISSING_PERMANENT_GUARD")
        try: create_bytes(guard, b"")
        except FileExistsError: pass
        sync_dir(root)
    expected_guard = identity(guard)
    with operations_lock(guard) as locked_identity:
        require(identity(root) == expected_ledger, "LEDGER_REPLACED")
        require(identity(guard) == expected_guard, "GUARD_REPLACED")
        if locked_identity is not None:  # Synthetic lock fixtures have no native fd.
            require(locked_identity == (expected_guard["device"], expected_guard["inode"]), "GUARD_REPLACED")
        sync_dir(root)
        with operations_lock(Path(config["lock_file"])) as shared_identity:
            operations_identity = identity(Path(config["lock_file"]))
            if shared_identity is not None:
                require(shared_identity == (operations_identity["device"], operations_identity["inode"]), "OPERATIONS_LOCK_REPLACED")
            capability = object()
            _owners[capability] = {"pid": os.getpid(), "thread": threading.get_ident(),
                "config": digest(encoded(config)), "guard": expected_guard,
                "ledger": expected_ledger, "operations": operations_identity, "attempt": None}
            try:
                yield capability
            finally:
                del _owners[capability]


def identity(path):
    info = safe_path(path).lstat()
    require(not stat.S_ISLNK(info.st_mode), "EVIDENCE_SYMLINK")
    return {"device": info.st_dev, "inode": info.st_ino, "mode": stat.S_IMODE(info.st_mode),
            "uid": info.st_uid, "gid": info.st_gid}


def mkdir(path):
    path.mkdir(mode=0o700)
    sync_dir(path)
    sync_dir(path.parent)


def entries(path):
    result = []
    with os.scandir(safe_path(path)) as items:
        for item in items:
            require(len(result) < 10000, "LEDGER_BOUND")
            result.append(Path(item.path))
    return result


def durable(path, raw):
    """Exact create-once retry includes fsync even after an acknowledgment loss."""
    safe_path(path)
    if path.exists():
        require(read(path, max(len(raw), 65536)) == raw, "APPEND_ONLY_CONFLICT")
        with path.open("r+b") as stream: os.fsync(stream.fileno())
    else:
        create_bytes(path, raw)
    sync_dir(path.parent)


def inventory(roots, cap=64 * 1024**2, count=10000):
    """Bound names, bytes, regular-file identity and explicit absence; no following links."""
    rows, raws, total = {}, {}, 0
    pending = list(roots)
    while pending:
        path = pending.pop()
        require(len(rows) < count, "EVIDENCE_COUNT")
        key = str(path)
        safe_path(path)
        if not path.exists():
            rows[key] = {"kind": "missing"}
            continue
        info = path.lstat()
        row = identity(path)
        if stat.S_ISDIR(info.st_mode):
            rows[key] = {"kind": "directory", **row}
            with os.scandir(path) as entries:
                for entry in entries:
                    require(len(pending) + len(rows) < count, "EVIDENCE_COUNT")
                    pending.append(Path(entry.path))
        else:
            require(stat.S_ISREG(info.st_mode) and info.st_nlink == 1, "EVIDENCE_TYPE")
            raw = read(path, cap - total)
            total += len(raw)
            require(total <= cap, "EVIDENCE_BYTES")
            require(row == identity(path), "EVIDENCE_RACE")
            rows[key] = {"kind": "file", **row, "sha256": digest(raw), "size": len(raw)}
            raws[key] = raw
    return rows, raws


def evidence(config, run_id, cap, count):
    require(re.fullmatch(r"[a-f0-9]{32}", run_id or ""), "RUN_ID")
    root = ledger(config)
    attempt = root / ("attempt-" + run_id)
    roots = [attempt, root / "attachment.lock", Path(config["work_dir"]) / run_id,
             Path(config["online"]["bridge_dir"])]
    rows, raws = inventory(roots, cap, count)
    for name in ("claim.json", "hold.json", "result.json", "owner.json", "capture-call.json"):
        key = str(attempt / name)
        if key not in rows: rows[key] = {"kind": "missing"}
    # Mutable state has its own CAS domain; it is never an immutable live-file binding.
    raw = raw_state(config)
    return {"ledger": identity(root), "guard": identity(root / "operation.guard"),
            "immutable": rows, "state_sha256": digest(raw), "state_present": state_path(config).exists()}, raws, raw


def preserve(directory, manifest, raws, state):
    if not directory.exists(): mkdir(directory)
    for name, raw in raws.items():
        durable(directory / (digest(name.encode()) + ".raw"), raw)
    durable(directory / "online-state.raw", state)
    durable(directory / "manifest.json", encoded(manifest))


def check_copies(directory):
    manifest = json.loads(read(directory / "manifest.json", 8 * 1024**2))
    expected = {"manifest.json", "online-state.raw"} | {digest(name.encode()) + ".raw"
                for name, row in manifest["immutable"].items() if row["kind"] == "file"}
    require({p.name for p in entries(directory)} == expected, "RECOVERY_COPY_LAYOUT")
    for name, row in manifest["immutable"].items():
        if row["kind"] == "file":
            require(digest(read(directory / (digest(name.encode()) + ".raw"), row["size"])) == row["sha256"], "RAW_COPY_CHANGED")
    require(digest(read(directory / "online-state.raw", 65536)) == manifest["state_sha256"], "RAW_STATE_CHANGED")
    return manifest


def unchanged(before, after):
    for name, row in before.items():
        require(after.get(name) == row, "IMMUTABLE_EVIDENCE_CHANGED")


def host(config):
    for name, path in (("machine", "/etc/machine-id"), ("boot", "/proc/sys/kernel/random/boot_id")):
        require(digest(Path(path).read_bytes().strip()) == config["online"][name + "_sha256"], "RECOVERY_HOST_BOOT")


OWNER_PROPERTIES = ("InvocationID", "ControlGroup", "MainPID", "ControlPID", "ActiveState", "SubState",
                    "Restart", "TriggeredBy", "Delegate", "ProtectControlGroups", "NoNewPrivileges",
                    "CapabilityBoundingSet", "KillMode")


def show_owner(unit):
    reply = subprocess.run(["/usr/bin/systemctl", "show", "--no-pager", "--property=" + ",".join(OWNER_PROPERTIES),
                            "--", unit], capture_output=True, timeout=10, check=False,
                           env={"PATH": "/usr/sbin:/usr/bin:/sbin:/bin", "LC_ALL": "C"})
    require(reply.returncode == 0 and len(reply.stdout) <= 32768 and len(reply.stderr) <= 32768, "OWNER_SHOW")
    rows = {}
    for line in reply.stdout.decode().splitlines():
        key, sep, value = line.partition("=")
        require(sep and key in OWNER_PROPERTIES and key not in rows, "OWNER_SHOW")
        rows[key] = value
    require(set(rows) == set(OWNER_PROPERTIES), "OWNER_SHOW")
    return rows


def confinement(row):
    require(row["Restart"] == "no" and row["TriggeredBy"] == "" and row["Delegate"] == "no"
            and row["ProtectControlGroups"] == "yes" and row["NoNewPrivileges"] == "yes"
            and set(row["CapabilityBoundingSet"].split()) <= {"cap_chown", "cap_dac_override", "cap_dac_read_search", "cap_fowner"}
            and row["KillMode"] == "control-group", "OWNER_CONFINEMENT")


def proc_identity(pid):
    root = Path("/proc") / str(pid)
    fields = (root / "stat").read_text().rpartition(")")[2].split()
    return {"pid": pid, "start_ticks": int(fields[19]), "exe": str((root / "exe").resolve(strict=True)),
            "exe_sha256": digest(read((root / "exe").resolve(strict=True), 32 * 1024**2))}


def source_observation(config):
    from live_service_capture import NativeBackend
    result = {}
    for role, unit in config["recovery_gate"]["source_units"].items():
        row = NativeBackend.show(unit)
        pid = int(row["MainPID"])
        result[role] = {"unit": unit, "properties": row,
                        "process": proc_identity(pid) if pid else None,
                        "unit_sha256": digest(read(checked_path(row["FragmentPath"], trusted=True), 1024**2))}
    return result


def native_owner(config):
    """Durable pre-effect containment identity; all helper children inherit this cgroup."""
    host(config)
    unit = config["recovery_gate"]["owner_unit"]
    row = show_owner(unit)
    confinement(row)
    require(re.fullmatch(r"[a-f0-9]{32}", row["InvocationID"] or ""), "OWNER_INVOCATION")
    groups = [x[3:] for x in Path("/proc/self/cgroup").read_text().splitlines() if x.startswith("0::")]
    require(groups == [row["ControlGroup"]] and groups[0] != "/" and int(row["MainPID"]) == os.getpid()
            and row["ControlPID"] == "0" and row["ActiveState"] == "active", "OWNER_IDENTITY")
    cgroup = Path("/sys/fs/cgroup") / groups[0].lstrip("/")
    process = proc_identity(os.getpid())
    require(process["exe_sha256"] == config["recovery_gate"]["python_sha256"], "OWNER_EXECUTABLE")
    sources = source_observation(config)
    require(all(source["process"] is not None and source["properties"]["ControlGroup"]
                and not overlap(groups[0], source["properties"]["ControlGroup"]) for source in sources.values()), "HELPER_SOURCE_CGROUP_OVERLAP")
    return {"version": 2, "machine_sha256": config["online"]["machine_sha256"],
            "boot_sha256": config["online"]["boot_sha256"], "unit": unit, "properties": row,
            "cgroup_identity": identity(cgroup), "process": process,
            "source": sources}


def terminated(config, launch):
    """Require same retained, nonescaping containment, no relaunch and an empty hierarchy.

    Missing/unloaded cgroups and changed invocations are deliberately unsupported;
    PID absence alone cannot establish descendant custody.
    """
    host(config)
    require(launch["version"] == 2 and all(launch[k] == config["online"][k]
            for k in ("machine_sha256", "boot_sha256")), "OWNER_BOOT")
    old = launch["properties"]
    confinement(old)
    require(re.fullmatch(r"[a-f0-9]{32}", old["InvocationID"] or ""), "OWNER_INVOCATION")
    require(launch["process"]["exe_sha256"] == config["recovery_gate"]["python_sha256"], "OWNER_EXECUTABLE")
    current = show_owner(launch["unit"])
    confinement(current)
    require(current["InvocationID"] == old["InvocationID"] and current["ControlGroup"] == old["ControlGroup"]
            and current["MainPID"] == "0" and current["ControlPID"] == "0"
            and current["ActiveState"] in {"inactive", "failed"}, "OWNER_NOT_ENDED")
    group = Path("/sys/fs/cgroup") / old["ControlGroup"].lstrip("/")
    require(identity(group) == launch["cgroup_identity"], "OWNER_CGROUP_CHANGED")
    events = dict(line.split() for line in (group / "cgroup.events").read_text().splitlines())
    require(events.get("populated") == "0" and (group / "cgroup.procs").read_bytes().strip() == b"", "READERS_REMAIN")
    process = Path("/proc") / str(launch["process"]["pid"])
    if process.exists():
        require(proc_identity(launch["process"]["pid"])["start_ticks"] != launch["process"]["start_ticks"], "OWNER_PROCESS_REMAINS")
    return {"owner_sha256": digest(encoded(launch)), "properties": current,
            "cgroup_identity": identity(group), "events": events, "source": source_observation(config)}


def validate_authority(config, approval, now):
    require(set(approval) == {"version", "operation", "recovery_id", "run_id", "capture_id", "config_sha256",
        "manifest_sha256", "resolution", "inner_approval", "not_before", "not_after", "max_dispositions",
        "max_bytes", "max_entries", "evidence_owner", "reviewer", "review_evidence", "source_evidence"}, "RECOVERY_AUTHORITY_SHAPE")
    require(approval["version"] == 2 and approval["operation"] == "outer-dispose"
            and approval["config_sha256"] == digest(encoded(config))
            and type(approval["max_dispositions"]) is int and approval["max_dispositions"] == 1, "RECOVERY_AUTHORITY")
    require(str(uuid.UUID(approval["recovery_id"])) == approval["recovery_id"]
            and str(uuid.UUID(approval["run_id"])) == approval["capture_id"], "RECOVERY_ID")
    require(approval["resolution"] in RESOLUTIONS and type(approval["max_bytes"]) is int
            and 0 < approval["max_bytes"] <= 1024**3 and type(approval["max_entries"]) is int
            and 0 < approval["max_entries"] <= 10000, "RECOVERY_BOUNDS")
    begin, end = timestamp(approval["not_before"]), timestamp(approval["not_after"])
    require(begin <= now < end and 0 < end - begin <= 600, "RECOVERY_WINDOW")
    if approval["inner_approval"] is not None:
        inner = approval["inner_approval"]
        require(type(inner) is dict and begin <= timestamp(inner["not_before"]) < timestamp(inner["not_after"]) <= end,
                "INNER_AUTHORITY_WINDOW")
    require(type(approval["evidence_owner"]) is str and 0 < len(approval["evidence_owner"]) <= 128
            and type(approval["reviewer"]) is str and 0 < len(approval["reviewer"]) <= 128
            and approval["evidence_owner"] != approval["reviewer"], "INDEPENDENT_REVIEW_REQUIRED")
    for key in ("review_evidence", "source_evidence"):
        ref = approval[key]
        require(set(ref) == {"path", "sha256"}, "EVIDENCE_REFERENCE")
        require(digest(read(checked_path(ref["path"], private=True), approval["max_bytes"])) == exact_id(ref["sha256"]), "REVIEW_EVIDENCE_CHANGED")


def terminal(config, approval, manifest, raw):
    """Check terminal protocol facts, never manufacture COMPLETE or restore evidence."""
    capture, run = approval["capture_id"], approval["run_id"]
    resolution = approval["resolution"]
    attempt = ledger(config) / ("attempt-" + run)
    call = attempt / "capture-call.json"
    work = Path(config["work_dir"]) / run
    bridge = Path(config["online"]["bridge_dir"])
    if resolution == "NO_CAPTURE":
        # Durable owner/claim barrier before call publication is positive entry custody.
        require(not call.exists() and not work.exists(), "NO_ENTRY_UNPROVEN")
        claim = load(attempt / "claim.json")
        require(claim.get("version") == 2 and claim.get("run_id") == run, "NO_ENTRY_UNPROVEN")
        for path in (bridge / "requests").glob("*.json"):
            req = load(path)
            require(req.get("capture_id") != capture and req.get("request_id") != capture, "PENDING_CAPTURE_EFFECT")
        require(not (bridge / "captures" / capture).exists(), "PENDING_CAPTURE_EFFECT")
        if raw: require(json.loads(raw).get("run_id") != run, "NO_ENTRY_UNPROVEN")
        return
    state = json.loads(raw)
    require(state.get("run_id") == run and state.get("capture_id") == capture
            and state.get("config_sha256") == digest(encoded(config)), "TERMINAL_STATE_BINDING")
    if resolution in {"INNER_COMPLETE", "ORIGINAL_COMPLETE_RECORDED"}:
        require(state.get("stage") == "COMPLETE", "INNER_NOT_COMPLETE")
        receipt_raw = read(work / "verified-receipt.json")
        receipt = json.loads(receipt_raw)
        marker = load(bridge / "captures" / capture / "release.json")
        require(digest(receipt_raw) == state.get("receipt_sha256") == marker.get("receipt_sha256")
                and marker.get("capture_id") == capture and receipt.get("run_id") == run
                and receipt.get("capture_id") == capture and receipt.get("restore_verified") is False
                and read(bridge / "captures" / capture / "verified-receipt.json") == receipt_raw, "TERMINAL_RELEASE_BINDING")
        require(receipt.get("status") == "VERIFIED" and receipt.get("repository_id") == config["repository_id"]
                and receipt.get("snapshot_id") == state.get("snapshot_id")
                and receipt.get("dump_sha256") == state.get("dump_sha256")
                and receipt.get("manifest_sha256") == state.get("manifest_sha256")
                and receipt.get("content_manifest_sha256") == digest(read(work / "manifest.json", config["limits"]["metadata_bytes"]))
                and all(receipt.get(k) is True for k in ("helpers_ended", "inventory_verified", "content_verified"))
                and state.get("last_good") == receipt, "TERMINAL_VERIFIED_RECEIPT")
        if resolution == "ORIGINAL_COMPLETE_RECORDED": validate_complete(attempt)
    else:
        require(state.get("stage") == "RECONCILED", "INNER_NOT_RECONCILED")
        recovery = work / "recovery" / state["recovery_id"]
        intent, result = load(recovery / "intent.json"), load(recovery / "result.json")
        require(result.get("status") == "RECONCILED" and result.get("capture_id") == capture
                and result.get("recovery_id") == state["recovery_id"]
                and result.get("restore_verified") is False and result.get("resolution") in INNER_RESOLUTIONS
                and intent["approval"]["resolution"] == result["resolution"]
                and intent["prior_state"]["stage"] == "HOLD" and intent["prior_state"]["run_id"] == run
                and intent["approval"]["run_id"] == run and intent["approval"]["capture_id"] == capture
                and intent["approval"]["config_sha256"] == digest(encoded(config)), "INNER_RECOVERY_BINDING")
        if result["resolution"] != "CONFIRM_NO_CAPTURE":
            name = "abort.json" if result["resolution"] == "ABANDON_BEFORE_SEAL" else "release.json"
            marker = load(bridge / "captures" / capture / name)
            require(marker.get("capture_id") == capture and marker.get("receipt_sha256") == result["receipt_sha256"], "INNER_FINAL_MARKER")
        else: require(not (bridge / "captures" / capture).exists(), "INNER_NO_CAPTURE")


def validate_complete(attempt):
    claim, result = load(attempt / "claim.json"), load(attempt / "result.json")
    require(claim["run_id"] == attempt.name[8:] and result.get("run_id") == claim["run_id"]
            and result.get("claim_sha256") == digest(encoded(claim)) and result.get("status") == "COMPLETE"
            and result.get("capture_id") == str(uuid.UUID(claim["run_id"])) and result.get("restore_verified") is False
            and (result.get("service_identity_unchanged") is True or result.get("kind") == "direct-inner"), "PRIOR_ATTACHMENT_HOLD")
    return claim, result


def attempt_custody(config, run):
    """Original cross-record bindings cannot be replaced by a newly approved file hash."""
    attempt = ledger(config) / ("attempt-" + run)
    claim_raw = read(attempt / "claim.json")
    claim = json.loads(claim_raw)
    launch_raw = read(attempt / "owner.json")
    require(claim.get("version") == 2, "LEGACY_HOLD_UNSUPPORTED")
    require(claim.get("run_id") == run
            and claim.get("online_config_sha256") == digest(encoded(config))
            and claim.get("owner_sha256") == digest(launch_raw)
            and claim.get("ledger_identity") == identity(ledger(config))
            and claim.get("guard_identity") == identity(ledger(config) / "operation.guard")
            and claim.get("lock_identity") == identity(ledger(config) / "attachment.lock")
            and load(attempt / "config.json") == config, "ORIGINAL_ATTEMPT_BINDING")
    call_path = attempt / "capture-call.json"
    if call_path.exists():
        require(load(call_path) == {"version": 2, "claim_sha256": digest(claim_raw),
                "config_sha256": digest(encoded(config)), "owner_sha256": digest(launch_raw)}, "ORIGINAL_CALL_BINDING")
    return json.loads(launch_raw)


def scan(config, owner):
    """Validate immutable history and a single causal shared-state tip."""
    require_owner(owner, config)
    root = ledger(config)
    children = entries(root)
    dispositions, tips, successors, completed_locks = {}, {}, {}, []
    recovery_root = root / "recovery"
    if recovery_root.exists():
        for recovery in entries(recovery_root):
            require(str(uuid.UUID(recovery.name)) == recovery.name, "RECOVERY_LAYOUT")
            require({p.name for p in entries(recovery)} == {"disposition.json", "intent.json", "approval.raw",
                    "config.raw", "review-evidence.raw", "source-evidence.raw", "before", "after"}, "RECOVERY_LAYOUT")
            value = load(recovery / "disposition.json")
            intent = load(recovery / "intent.json")
            approval_raw = read(recovery / "approval.raw")
            approval = json.loads(approval_raw)
            for key in ("review_evidence", "source_evidence"):
                require(digest(read(recovery / (key.replace("_", "-") + ".raw"), approval["max_bytes"])) ==
                        approval[key]["sha256"], "HISTORICAL_REVIEW_EVIDENCE")
            original_config_raw = read(recovery / "config.raw", 1024**2)
            original_config = json.loads(original_config_raw)
            attempt_custody(original_config, value["run_id"])
            before, after = check_copies(recovery / "before"), check_copies(recovery / "after")
            require(before["ledger"] == after["ledger"] == identity(root)
                    and before["guard"] == after["guard"] == identity(root / "operation.guard"), "HISTORICAL_LEDGER_IDENTITY")
            require(value.get("version") == 2 and value.get("status") == "DISPOSED" and value.get("resolution") in RESOLUTIONS
                    and value.get("recovery_id") == recovery.name == approval.get("recovery_id")
                    and value.get("run_id") == approval.get("run_id") == intent.get("run_id")
                    and value.get("resolution") == approval.get("resolution") == intent.get("resolution")
                    and value.get("approval_sha256") == digest(encoded(approval)) == intent.get("approval_sha256")
                    and approval.get("config_sha256") == digest(encoded(original_config))
                    and intent.get("config_raw_sha256") == digest(original_config_raw)
                    and intent.get("approval_raw_sha256") == digest(approval_raw)
                    and intent.get("manifest_sha256") == digest(encoded(before)) == approval.get("manifest_sha256")
                    and value["intent_sha256"] == digest(encoded(intent)) and value["before_sha256"] == digest(encoded(before))
                    and value["after_sha256"] == digest(encoded(after)) and value["capture_success_asserted"] is False
                    and value["restore_verified"] is False and value["run_id"] not in dispositions, "DISPOSITION_BINDING")
            additions(original_config, approval, before["immutable"], after["immutable"])
            terminal(original_config, approval, after, read(recovery / "after" / "online-state.raw", 65536))
            for name, row in after["immutable"].items():
                # Later capture can add mailbox files but may never rewrite an existing original.
                if row["kind"] == "missing":
                    require(not Path(name).exists(), "HISTORICAL_ABSENCE_CHANGED")
                elif row["kind"] == "file":
                    require(identity(Path(name)) == {k: row[k] for k in ("device", "inode", "mode", "uid", "gid")}
                            and digest(read(Path(name), row["size"])) == row["sha256"], "HISTORICAL_EVIDENCE_CHANGED")
                else: require(identity(Path(name)) == {k: row[k] for k in ("device", "inode", "mode", "uid", "gid")}, "HISTORICAL_DIRECTORY_CHANGED")
            dispositions[value["run_id"]] = value
            tips[digest(read(recovery / "disposition.json"))] = after["state_sha256"]
    for entry in children:
        if entry.name in {"operation.guard", "attachment.lock", "recovery"}: continue
        require(re.fullmatch(r"attempt-[a-f0-9]{32}", entry.name) and entry.is_dir() and not entry.is_symlink(), "UNEXPECTED_LEDGER_ENTRY")
        claim = load(entry / "claim.json")
        if entry.name[8:] not in dispositions:
            claim, result = validate_complete(entry)
            if claim.get("version") == 2:
                attempt_custody(load(entry / "config.json"), claim["run_id"])
                tips[digest(read(entry / "result.json"))] = result["state_sha256"]
                completed_locks.append(claim["lock_identity"])
        if claim.get("version") == 2 and claim.get("predecessor") is not None:
            pred = claim["predecessor"]
            require(pred["record_sha256"] not in successors, "LEDGER_FORK")
            successors[pred["record_sha256"]] = pred["state_sha256"]
    for record, state in successors.items(): require(tips.get(record) == state, "LEDGER_PREDECESSOR")
    remaining = {k: v for k, v in tips.items() if k not in successors}
    require(len(remaining) <= 1, "LEDGER_MULTIPLE_TIPS")
    if (root / "attachment.lock").exists():
        lock_id = identity(root / "attachment.lock")
        require(lock_id in completed_locks or any(v["lock_identity"] == lock_id for v in dispositions.values()), "ATTACHMENT_LOCKED_OR_HOLD")
    if remaining:
        record, state = next(iter(remaining.items()))
        require(digest(raw_state(config)) == state, "SHARED_STATE_UNEXPLAINED")
        return {"record_sha256": record, "state_sha256": state}
    return None


def begin_capture(config, approval, owner, claim_fields=None):
    context = require_owner(owner, config)
    require(context["attempt"] is None, "CAPTURE_ALREADY_ENTERED")
    predecessor = scan(config, owner)
    root, run = ledger(config), approval["run_id"]
    entry = root / ("attempt-" + run)
    require(not entry.exists(), "DUPLICATE_ATTACHMENT")
    launch = native_owner(config)
    if not (root / "attachment.lock").exists(): mkdir(root / "attachment.lock")
    mkdir(entry)
    claim = {"version": 2, "run_id": run, "online_config_sha256": digest(encoded(config)),
             "online_approval_sha256": digest(encoded(approval)), "predecessor": predecessor,
             "owner_sha256": digest(encoded(launch)),
             "ledger_identity": identity(root), "guard_identity": identity(root / "operation.guard"),
             "lock_identity": identity(root / "attachment.lock"),
             **(claim_fields or {})}
    durable(entry / "owner.json", encoded(launch))
    durable(entry / "config.json", encoded(config))
    durable(entry / "claim.json", encoded(claim))
    context["attempt"] = entry
    return entry, claim


def call_capture(config, owner):
    context = require_owner(owner, config)
    entry = context["attempt"]
    require(entry is not None, "CAPTURE_ATTEMPT_REQUIRED")
    durable(entry / "capture-call.json", encoded({"version": 2, "claim_sha256": digest(read(entry / "claim.json")),
            "config_sha256": digest(encoded(config)), "owner_sha256": digest(read(entry / "owner.json"))}))


def finish_capture(config, owner, result):
    context = require_owner(owner, config)
    entry = context["attempt"]
    result = {**result, "state_sha256": digest(raw_state(config))}
    durable(entry / "result.json", encoded(result))
    return result


def additions(config, approval, before, after):
    unchanged(before, {k: after[k] for k in before if k in after})
    capture = approval["capture_id"]
    inner = approval["inner_approval"]
    added = set(after) - set(before)
    require(not added or inner is not None, "UNAUTHORIZED_ADDITION")
    bridge = Path(config["online"]["bridge_dir"])
    work_recovery = Path(config["work_dir"]) / approval["run_id"] / "recovery"
    recovery = work_recovery / inner["recovery_id"] if inner else None
    for name in added:
        path, row = Path(name), after[name]
        if path == work_recovery or path == recovery:
            require(row["kind"] == "directory", "RECOVERY_ADDITION")
        elif recovery is not None and path.parent == recovery and path.name in {"intent.json", "result.json"}:
            value = load(path)
            if path.name == "intent.json": require(value["approval"] == inner, "INNER_INTENT_CHANGED")
            else: require(value["recovery_id"] == inner["recovery_id"] and value["capture_id"] == capture, "INNER_RESULT_CHANGED")
        elif path.parent in {bridge / "requests", bridge / "claims", bridge / "responses"}:
            request_path = bridge / "requests" / path.name
            req = load(request_path)
            require(req.get("capture_id") == capture and req.get("action") in {"STATUS", "ABORT", "RELEASE"}
                    and req.get("request_id") + ".json" == path.name, "MAILBOX_ADDITION")
            permitted = {"STATUS"} | ({"ABORT"} if inner["resolution"] == "ABANDON_BEFORE_SEAL" else
                                     {"RELEASE"} if inner["resolution"] == "CONFIRM_VERIFIED_RELEASE" else set())
            require(req["action"] in permitted, "MAILBOX_ACTION_NOT_APPROVED")
            if path.parent.name == "claims": require(read(path) == read(request_path), "MAILBOX_CLAIM")
            if path.parent.name == "responses":
                reply = load(path)
                require(reply.get("request_sha256") == digest(read(request_path)) and reply.get("capture_id") == capture
                        and reply.get("request_id") == req["request_id"], "MAILBOX_RESPONSE")
        elif path.parent == bridge / "captures" / capture:
            allowed = {"abandon-receipt.json", "abort.json"} if inner["resolution"] == "ABANDON_BEFORE_SEAL" else {"release.json"}
            require(path.name in allowed and load(path).get("capture_id") == capture, "MAILBOX_MARKER")
        else: raise GateError("UNAUTHORIZED_ADDITION")


def mailbox_quiescent(config, approval):
    bridge = Path(config["online"]["bridge_dir"])
    for path in entries(bridge / "requests"):
        require(path.suffix == ".json", "MAILBOX_DEBRIS")
        request = load(path)
        require(request.get("request_id") + ".json" == path.name, "MAILBOX_REQUEST")
        claim = bridge / "claims" / path.name
        require(claim.exists() and read(claim) == read(path), "PENDING_MAILBOX_WORK")
        response = bridge / "responses" / path.name
        if response.exists():
            reply = load(response)
            require(reply.get("request_id") == request["request_id"] and reply.get("request_sha256") == digest(read(path)), "MAILBOX_RESPONSE")
        else:
            # Only already-consumed original FREEZE/RELEASE may have lost replies;
            # the existing inner reconciler must establish the final exact marker.
            capture = request.get("request_id") if request.get("action") == "FREEZE" else request.get("capture_id")
            require(capture == approval["capture_id"], "PENDING_MAILBOX_WORK")
            if request.get("action") == "ABORT":
                inner = approval["inner_approval"]
                require(inner is not None and inner["resolution"] == "ABANDON_BEFORE_SEAL", "PENDING_MAILBOX_WORK")
                intent_path = Path(config["work_dir"]) / approval["run_id"] / "recovery" / inner["recovery_id"] / "intent.json"
                require(load(intent_path)["approval"] == inner, "LOST_ABORT_INTENT")
                directory = bridge / "captures" / capture
                receipt_raw = read(directory / "abandon-receipt.json")
                receipt = json.loads(receipt_raw)
                marker = load(directory / "abort.json")
                require(request.get("receipt_sha256") == digest(receipt_raw) == marker.get("receipt_sha256")
                        and marker.get("capture_id") == receipt.get("capture_id") == capture
                        and receipt.get("run_id") == approval["run_id"] and receipt.get("status") == "ABANDONED",
                        "LOST_ABORT_MARKER")
            else:
                require(request.get("action") in {"FREEZE", "RELEASE"}, "PENDING_MAILBOX_WORK")


def dispose(config, approval, owner, reconcile_call, *, clock=time.time, observe_ended=terminated,
            recheck=lambda: None, input_raw=None):
    """One disposition. Dependencies are injected only by synthetic tests; CLI fixes native implementations."""
    require_owner(owner, config)
    recheck()
    validate_authority(config, approval, clock())
    root = ledger(config)
    recovery_root = root / "recovery"
    rid = approval["recovery_id"]
    recovery = recovery_root / rid
    if recovery_root.exists():
        for other in entries(recovery_root):
            require(other == recovery or (other / "disposition.json").exists(), "RECOVERY_CONFLICT")
            if other != recovery:
                require(load(other / "disposition.json")["run_id"] != approval["run_id"], "RECOVERY_CONFLICT")
    attempt = root / ("attempt-" + approval["run_id"])
    launch = attempt_custody(config, approval["run_id"])
    ended = observe_ended(config, launch)
    mailbox_quiescent(config, approval)
    current, raws, raw = evidence(config, approval["run_id"], approval["max_bytes"], approval["max_entries"])
    if (recovery / "disposition.json").exists():
        value = load(recovery / "disposition.json")
        require(value["approval_sha256"] == digest(encoded(approval)), "RECOVERY_CONFLICT")
        scan(config, owner)
        durable(recovery / "disposition.json", encoded(value))
        return value
    if not recovery_root.exists(): mkdir(recovery_root)
    if not recovery.exists(): mkdir(recovery)
    inputs = input_raw or {"config": encoded(config), "approval": encoded(approval)}
    require(json.loads(inputs["config"]) == config and json.loads(inputs["approval"]) == approval, "INPUT_RAW_BINDING")
    durable(recovery / "config.raw", inputs["config"])
    durable(recovery / "approval.raw", inputs["approval"])
    for key in ("review_evidence", "source_evidence"):
        proof_raw = read(checked_path(approval[key]["path"], private=True), approval["max_bytes"])
        require(digest(proof_raw) == approval[key]["sha256"], "REVIEW_EVIDENCE_CHANGED")
        durable(recovery / (key.replace("_", "-") + ".raw"), proof_raw)
    if (recovery / "intent.json").exists():
        intent = load(recovery / "intent.json")
        require(intent["approval_sha256"] == digest(encoded(approval)), "RECOVERY_INTENT_CONFLICT")
        before = check_copies(recovery / "before")
        additions(config, approval, before["immutable"], current["immutable"])
    else:
        require(digest(encoded(current)) == approval["manifest_sha256"], "EVIDENCE_CAS")
        preserve(recovery / "before", current, raws, raw)
        before = current
        intent = {"version": 2, "approval_sha256": digest(encoded(approval)), "manifest_sha256": approval["manifest_sha256"],
                  "config_raw_sha256": digest(inputs["config"]), "approval_raw_sha256": digest(inputs["approval"]),
                  "run_id": approval["run_id"], "resolution": approval["resolution"], "inner_approval": approval["inner_approval"],
                  "expected_state_sha256": before["state_sha256"]}
        durable(recovery / "intent.json", encoded(intent))
    inner = approval["inner_approval"]
    state = json.loads(raw) if raw else {}
    if inner is not None:
        require(enabled(config) and inner["resolution"] in INNER_RESOLUTIONS and approval["resolution"] == "INNER_RECONCILED"
                and inner["run_id"] == approval["run_id"] and inner["capture_id"] == approval["capture_id"]
                and inner["config_sha256"] == digest(encoded(config)) and inner["state_sha256"] == before["state_sha256"], "V2_INNER_BINDING")
        require(load(attempt / "claim.json").get("version") == 2, "LEGACY_HOLD_UNSUPPORTED")
        if state.get("stage") != "RECONCILED":
            require(digest(raw) == before["state_sha256"], "SHARED_STATE_CAS")
            recheck()
            require_owner(owner, config)
            validate_authority(config, approval, clock())
            observe_ended(config, launch)
            reconcile_call(inner, owner)
        else: require(state.get("recovery_id") == inner["recovery_id"], "INNER_RECOVERY_CHANGED")
    after, after_raws, after_state = evidence(config, approval["run_id"], approval["max_bytes"], approval["max_entries"])
    additions(config, approval, before["immutable"], after["immutable"])
    if inner is None: require(after["state_sha256"] == before["state_sha256"], "SHARED_STATE_CAS")
    else:
        prior = json.loads(read(recovery / "before" / "online-state.raw", 65536))
        changed = json.loads(after_state)
        require(changed.get("stage") == "RECONCILED" and changed.get("recovery_id") == inner["recovery_id"], "INNER_TRANSITION")
        for key, value in prior.items():
            if key not in {"stage", "updated_at", "last_good"}: require(changed.get(key) == value, "INNER_TRANSITION")
        require(set(changed) == set(prior) | {"recovery_id"}, "INNER_TRANSITION")
        if inner["resolution"] != "CONFIRM_VERIFIED_RELEASE": require(changed.get("last_good") == prior.get("last_good"), "INNER_LAST_GOOD")
    terminal(config, approval, after, after_state)
    mailbox_quiescent(config, approval)
    ended = observe_ended(config, launch)
    recheck()
    validate_authority(config, approval, clock())
    final, _, final_raw = evidence(config, approval["run_id"], approval["max_bytes"], approval["max_entries"])
    require(final == after and final_raw == after_state, "COMMIT_CAS")
    preserve(recovery / "after", after, after_raws, after_state)
    # Archival can consume the remaining approval window; no effect follows an old check.
    recheck()
    ended = observe_ended(config, launch)
    final, _, final_raw = evidence(config, approval["run_id"], approval["max_bytes"], approval["max_entries"])
    require(final == after and final_raw == after_state, "COMMIT_CAS")
    require_owner(owner, config)
    validate_authority(config, approval, clock())
    value = {"version": 2, "status": "DISPOSED", "recovery_id": rid, "run_id": approval["run_id"],
             "resolution": approval["resolution"], "approval_sha256": digest(encoded(approval)),
             "intent_sha256": digest(encoded(intent)), "before_sha256": digest(encoded(before)),
             "after_sha256": digest(encoded(after)), "termination": ended,
             "lock_identity": identity(root / "attachment.lock"), "source_evidence": approval["source_evidence"],
             "source_continuity": "UNPROVEN", "capture_success_asserted": False, "restore_verified": False}
    durable(recovery / "disposition.json", encoded(value))
    return value


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("command", choices=("plan", "inspect", "dispose"), nargs="?", default="plan")
    parser.add_argument("--config", type=Path)
    parser.add_argument("--approval", type=Path)
    parser.add_argument("--run-id")
    args = parser.parse_args(argv)
    if args.command == "plan":
        print('{"status":"DISABLED","side_effects":false}')
        return 0
    try:
        require(sys.platform == "linux" and os.geteuid() == 0, "LINUX_ROOT_REQUIRED")
        config_raw = read(checked_path(args.config, private=True), 1024**2)
        config = json.loads(config_raw)
        pins(config)
        import online_capture
        online_capture.validate_online(config)
        host(config)
        if args.command == "inspect":
            # No lock creation, STATUS, service operation or evidence publication.
            value, _, _ = evidence(config, args.run_id, 64 * 1024**2, 10000)
            print(encoded({"status": "PROVISIONAL", "manifest_sha256": digest(encoded(value)), "side_effects": False}).decode())
            return 0
        approval_raw = read(checked_path(args.approval, private=True))
        approval = json.loads(approval_raw)
        with gate(config) as owner:
            pins(config)
            def reconcile_call(inner, capability):
                import online_capture
                # Native checks remain; approval is bound through the already-private outer authority.
                return online_capture.execute_reconcile(config, args.config, args.approval,
                    owner=capability, inner_approval=inner)
            def recheck():
                require(read(checked_path(args.config, private=True), 1024**2) == config_raw
                        and read(checked_path(args.approval, private=True)) == approval_raw, "INPUT_CHANGED")
                pins(config)
                host(config)
            value = dispose(config, approval, owner, reconcile_call, recheck=recheck,
                            input_raw={"config": config_raw, "approval": approval_raw})
        print(encoded({k: value[k] for k in ("status", "recovery_id", "capture_success_asserted", "restore_verified")}).decode())
        return 0
    except (GateError, OSError, ValueError, KeyError, TypeError, AttributeError, subprocess.SubprocessError):
        print('{"status":"HOLD","code":"OUTER_RECOVERY_STOPPED"}')
        return 2


if __name__ == "__main__":
    raise SystemExit(main())

"""Finite online dispatch; inert by default, not an installed scheduler.

Consumes an exact fresh online approval/config supplied by a separate operator.
It does not issue proofs, extend authority, send mail or reconcile HOLD records.
"""
from __future__ import annotations

import argparse
from datetime import datetime, timedelta, timezone
import os
from pathlib import Path
import re
import stat
import sys
import time
import uuid

from backup_protocol import GateError, digest, encoded, require, timestamp
import online_capture as online
import outer_recovery as outer
from scheduled_dispatch import load, number, safe_path, sync_dir, verify_files, write_new


def validate_source_custody():
    """Fail closed on source/cache ambiguity in a root-trusted frozen bundle.

    Imports are side-effect-free by contract. This is not a sandbox for malicious
    import-time code: Python has already imported dependencies before this check.
    """
    directory = Path(__file__).resolve().parent
    modules = {name: name for name in (
        "backup_protocol", "backup_runtime", "scheduled_dispatch", "online_capture")}
    modules[__name__] = "online_scheduled_dispatch"
    for name, filename in modules.items():
        module = sys.modules.get(name)
        expected = directory / (filename + ".py")
        origin = getattr(module, "__file__", None)
        require(type(origin) is str and Path(origin).suffix == ".py" and
                Path(origin).resolve() == expected and expected.is_file(), "MODULE_SOURCE_ORIGIN")
        spec = getattr(module, "__spec__", None)
        if spec is not None:
            require(type(spec.origin) is str and Path(spec.origin).resolve() == expected,
                    "MODULE_SOURCE_ORIGIN")
        cached = getattr(module, "__cached__", None)
        require(cached is None or type(cached) is str, "MODULE_CACHE_AMBIGUOUS")
        paths = [expected.with_suffix(".pyc")]
        if cached is not None:
            paths.append(Path(cached))
        require(not any(path.exists() or path.is_symlink() for path in paths),
                "MODULE_CACHE_AMBIGUOUS")


def window(policy, now):
    require(policy.get("version") == 1 and policy.get("enabled") is True and
            policy.get("operation") == "daily-online-dispatch", "ONLINE_SCHEDULE_DISABLED")
    require(re.fullmatch(r"[A-Za-z0-9_-]{1,64}", policy.get("policy_id", "")) is not None,
            "POLICY_ID")
    require(policy.get("timezone") == "Asia/Seoul", "TIMEZONE")
    clock = policy.get("start_hhmm", "")
    require(re.fullmatch(r"(?:[01][0-9]|2[0-3]):[0-5][0-9]", clock) is not None, "CLOCK")
    grace, seconds = policy.get("start_grace_seconds"), policy.get("window_seconds")
    require(type(grace) is int and type(seconds) is int and 0 < grace <= 60 and
            grace < seconds <= 1800, "WINDOW")
    local = datetime.fromtimestamp(now, timezone(timedelta(hours=9)))
    hour, minute = map(int, clock.split(":"))
    start = local.replace(hour=hour, minute=minute, second=0, microsecond=0)
    end = start + timedelta(seconds=seconds)
    lower, upper = timestamp(policy.get("not_before")), timestamp(policy.get("expires_at"))
    require(lower <= start.timestamp() <= now < end.timestamp() <= upper and
            upper - lower <= 366 * 86400 and start.date() == end.date(), "POLICY_EXPIRED")
    require(now - start.timestamp() < grace, "NO_CATCHUP_WINDOW")
    run_id = digest(encoded([policy["policy_id"], str(start.date()), "daily-online-dispatch"]))[:32]
    return run_id, start.timestamp(), end.timestamp()


def validate_inputs(config, approval, policy, pins, now):
    online.validate_online(config)
    online.validate_authority(config, approval, now)
    run_id, start, end = window(policy, now)
    require(policy.get("config_subject_sha256") == online.config_subject(config), "CONFIG_SUBJECT")
    require(policy.get("pins_sha256") == digest(encoded(pins)), "PINS_AUTHORITY")
    require(approval["run_id"] == run_id and timestamp(approval["not_before"]) == start and
            timestamp(approval["not_after"]) == end, "EXACT_RUN_WINDOW")
    require(policy["window_seconds"] <= config["limits"]["seconds"], "WINDOW_LIMIT")
    return run_id


def validate_previous(entry, claim, result, identity):
    """Reject malformed history without repairing or weakening its bindings."""
    require(type(claim) is dict and set(claim) == {
        "identity", "run_id", "config_sha256", "approval_sha256"}, "CORRUPT_CLAIM")
    require(type(claim["identity"]) is dict and claim["identity"] == identity,
            "CORRUPT_CLAIM")
    run_id = claim["run_id"]
    require(type(run_id) is str and re.fullmatch(r"[a-f0-9]{32}", run_id) is not None and
            entry.name == "attempt-" + run_id, "CORRUPT_CLAIM")
    for key in ("config_sha256", "approval_sha256"):
        value = claim[key]
        require(type(value) is str and re.fullmatch(r"[a-f0-9]{64}", value) is not None,
                "CORRUPT_CLAIM")
    require(type(result) is dict and result == {
        "status": "COMPLETE", "run_id": run_id, "capture_id": str(uuid.UUID(run_id)),
        "restore_verified": False} and result.get("restore_verified") is False,
        "UNCERTAIN_ATTEMPT")


def dispatch(root, config, approval, policy, pins, execute, now):
    if outer.enabled(config):
        with outer.gate(config) as owner:
            outer.pins(config)
            outer.scan(config, owner)
            return _dispatch(root, config, approval, policy, pins, lambda: execute(owner), now)
    require(not (outer.ledger(config) / "operation.guard").exists(), "LEGACY_GATE_INCOMPATIBLE")
    return _dispatch(root, config, approval, policy, pins, execute, now)


def _dispatch(root, config, approval, policy, pins, execute, now):
    """execute is injected only by local tests; native CLI fixes online.execute.

    A durable claim consumes a full worst-case attempt even if execution fails.
    No claim/lock/result is automatically cleared after an uncertain operation.
    """
    run_id = validate_inputs(config, approval, policy, pins, now)
    budget = policy["budget"]
    maximum = number(budget, "max_attempts", 1)
    growth = number(budget, "per_attempt_bytes", 1)
    cost = number(budget, "per_attempt_cost_krw")
    retained = number(budget, "existing_bytes") + number(budget, "reserve_bytes")
    capacity = number(budget, "capacity_bytes", 1)
    cap = number(budget, "cost_cap_krw")
    require(growth >= sum(config["limits"][k] for k in
                          ("source_bytes", "dump_bytes", "metadata_bytes")), "GROWTH_BOUND")
    require(cost >= config["limits"]["projected_cost_krw"] and
            cap <= config["limits"]["cost_limit_krw"], "COST_AUTHORITY")
    verify_files(pins)
    root = safe_path(root)
    require(root.is_dir(), "STATE_ROOT_REQUIRED")
    if os.name == "posix":
        info = root.stat()
        require(info.st_uid == os.geteuid() and stat.S_IMODE(info.st_mode) == 0o700,
                "PRIVATE_STATE_REQUIRED")
    lock = root / "dispatch.lock"
    try:
        lock.mkdir(mode=0o700)
    except FileExistsError:
        raise GateError("LOCKED_OR_UNCERTAIN") from None
    sync_dir(root)
    claimed = False
    try:
        identity = {"policy_sha256": digest(encoded(policy))}
        entries = list(root.iterdir())
        require(all(p.name in {"dispatch.lock", "identity.json"} or p.name.startswith("attempt-")
                    for p in entries), "UNEXPECTED_STATE")
        if (root / "identity.json").exists():
            require(load(root / "identity.json") == identity, "POLICY_IDENTITY_CHANGED")
        else:
            require(len(entries) == 1, "MISSING_STATE_IDENTITY")
            write_new(root / "identity.json", identity)
        attempts = [p for p in entries if p.name.startswith("attempt-")]
        for entry in attempts:
            claim = load(entry / "claim.json")
            result = load(entry / "result.json")
            validate_previous(entry, claim, result, identity)
        require(len(attempts) < maximum, "ATTEMPT_BUDGET_EXHAUSTED")
        require(retained + (len(attempts) + 1) * growth <= capacity, "CUMULATIVE_STORAGE_EXCEEDED")
        require((len(attempts) + 1) * cost <= cap, "CUMULATIVE_COST_EXCEEDED")
        entry = root / ("attempt-" + run_id)
        require(not entry.exists(), "DUPLICATE_ATTEMPT")
        entry.mkdir(mode=0o700)
        claimed = True
        write_new(entry / "claim.json", {"identity": identity, "run_id": run_id,
                  "config_sha256": digest(encoded(config)), "approval_sha256": digest(encoded(approval))})
        sync_dir(root)
        verify_files(pins)
        value = execute()
        require(value == {"status": "COMPLETE", "restore": "NOT_RUN",
                          "capture_id": str(uuid.UUID(run_id))}, "ONLINE_RECEIPT_REQUIRED")
        result = {"status": "COMPLETE", "run_id": run_id, "capture_id": value["capture_id"],
                  "restore_verified": False}
        write_new(entry / "result.json", result)
        claimed = False
        return result
    finally:
        if not claimed:
            lock.rmdir()
            sync_dir(root)


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--execute", action="store_true")
    for name in ("config", "approval", "policy", "pins", "state"):
        parser.add_argument("--" + name)
    args = parser.parse_args(argv)
    if not args.execute:
        print('{"status":"DISABLED","side_effects":false}')
        return 0
    try:
        require(sys.platform == "linux" and os.geteuid() == 0, "LINUX_ROOT_REQUIRED")
        validate_source_custody()
        from backup_runtime import checked_path
        for ref in (args.config, args.approval, args.policy, args.pins, args.state):
            require(isinstance(ref, str), "INPUT_REFERENCE_REQUIRED")
            checked_path(ref, private=True)
        config, approval, policy, pins = map(load, (args.config, args.approval, args.policy, args.pins))
        directory = Path(__file__).resolve().parent
        names = outer.MODULES if outer.enabled(config) else ("online_scheduled_dispatch.py", "scheduled_dispatch.py",
                    "online_capture.py", "backup_runtime.py", "backup_protocol.py")
        required = {str(directory / name) for name in names}
        required.add(str(Path(sys.executable).resolve()))
        require(set(pins) == required, "EXECUTION_PINS_INCOMPLETE")
        for ref in required:
            checked_path(ref, trusted=True)
        require(Path(args.state) == Path(config["state_dir"]) / "online-scheduled-ledger", "STABLE_LEDGER_REQUIRED")
        def execute(owner=None):
            validate_source_custody()
            require(load(args.config) == config and load(args.approval) == approval and
                    load(args.policy) == policy and load(args.pins) == pins, "INPUT_CHANGED")
            validate_inputs(config, approval, policy, pins, time.time())
            # Existing executor owns shared operations lock, proof checks and cleanup.
            return online.execute(config, Path(args.config), Path(args.approval),
                                  expected_approval_sha256=digest(encoded(approval)), **({"owner": owner} if owner is not None else {}))
        value = dispatch(args.state, config, approval, policy, pins, execute, time.time())
        print(encoded(value).decode())
        return 0
    except (GateError, OSError, ValueError, TypeError, KeyError, AttributeError, RecursionError):
        print('{"status":"HOLD","code":"ONLINE_DISPATCH_STOPPED"}')
        return 2


if __name__ == "__main__":
    raise SystemExit(main())

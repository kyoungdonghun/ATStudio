"""WI016 durable finite dispatcher. Native CLI is Linux/root only; default inert."""
from __future__ import annotations

import argparse
import json
import os
from pathlib import Path
import stat
import sys
import time

from backup_protocol import GateError, digest, encoded, recurring_approval, recurring_subject, require, timestamp, validate_config


def safe_path(path):
    path = Path(path).absolute()
    for part in (path, *path.parents):
        if part.exists() or part.is_symlink():
            require(not part.is_symlink(), "SYMLINK_REJECTED")
    return path


def read(path, limit=131072):
    path = safe_path(path)
    fd = os.open(path, os.O_RDONLY | getattr(os, "O_NOFOLLOW", 0))
    with os.fdopen(fd, "rb") as stream:
        info = os.fstat(stream.fileno())
        require(stat.S_ISREG(info.st_mode) and info.st_nlink == 1, "REGULAR_SINGLE_LINK_REQUIRED")
        data = stream.read(limit + 1)
        require(len(data) <= limit, "INPUT_LIMIT")
        after = os.fstat(stream.fileno())
        identity = lambda value: (value.st_dev, value.st_ino, value.st_size,
                                  value.st_mtime_ns, value.st_ctime_ns, value.st_nlink)
        require(identity(info) == identity(after) and len(data) == after.st_size,
                "FILE_CHANGED_DURING_READ")
        return data


def sync_dir(path):
    if os.name == "posix":
        fd = os.open(path, os.O_RDONLY | os.O_DIRECTORY | os.O_NOFOLLOW)
        try:
            os.fsync(fd)
        finally:
            os.close(fd)


def write_new(path, value):
    safe_path(path)
    fd = os.open(path, os.O_WRONLY | os.O_CREAT | os.O_EXCL | getattr(os, "O_NOFOLLOW", 0), 0o600)
    with os.fdopen(fd, "wb") as stream:
        stream.write(encoded(value))
        stream.flush()
        os.fsync(stream.fileno())
    sync_dir(path.parent)


def load(path):
    try:
        value = json.loads(read(path))
        require(isinstance(value, dict), "OBJECT_REQUIRED")
        return value
    except (ValueError, RecursionError):
        raise GateError("CORRUPT_STATE") from None


def number(obj, name, minimum=0):
    value = obj.get(name)
    require(type(value) is int and value >= minimum, "FINITE_BUDGET_REQUIRED")
    return value


def verify_files(pins):
    require(isinstance(pins, dict) and bool(pins), "PINS_REQUIRED")
    for name, sha in pins.items():
        require(digest(read(Path(name), 32 * 1024 * 1024)) == sha, "PIN_MISMATCH")


def ledger_failure_code(root):
    """Read-only fixed-code alert bridge; cannot clear locks or infer freshness."""
    try:
        root = safe_path(root)
        require(root.is_dir(), "STATE_ROOT_REQUIRED")
        if (root / "dispatch.lock").exists():
            return "DISPATCH_UNCERTAIN"
        identity = load(root / "identity.json")
        for entry in root.iterdir():
            if entry.name == "identity.json":
                continue
            require(entry.name.startswith("attempt-"), "UNEXPECTED_STATE")
            claim = load(entry / "claim.json")
            require(claim.get("identity") == identity and entry.name == "attempt-" + claim.get("run_id", ""), "CORRUPT_CLAIM")
            result = load(entry / "result.json")
            if result == {"status": "FAILED"}:
                return "DISPATCH_FAILED"
            require(result == {"status": "COMPLETE"}, "CORRUPT_RESULT")
        # Recheck lock after reads: don't report clean during a concurrent claim.
        return "DISPATCH_UNCERTAIN" if (root / "dispatch.lock").exists() else None
    except (GateError, OSError, ValueError, TypeError, AttributeError):
        return "DISPATCH_UNCERTAIN"


def dispatch(root, config, policy, budget, argv, pins, runner, now, *, manifest=None):
    """runner.run is the existing bounded LinuxRunner interface; fakes only in tests.

    Root must be dedicated and trusted. Crash locks and any incomplete attempt are
    HOLD, never automatically reclaimed. An operator must reconcile outside here.
    """
    validate_config(config)
    require(not config.get("recovery_gate") and not
            (Path(config["state_dir"]) / "live-service-ledger" / "operation.guard").exists(),
            "LEGACY_GATE_INCOMPATIBLE")
    approval = recurring_approval(config, policy, now)
    require(isinstance(argv, list) and argv and all(isinstance(x, str) and x for x in argv), "ARGV_REQUIRED")
    if policy.get("version", 1) == 2:
        require(budget.get("config_subject_sha256") == recurring_subject(config) and
                budget.get("policy_sha256") == digest(encoded(policy)) and
                budget.get("policy_id") == policy["policy_id"], "DISPATCH_IDENTITY")
        code_pins = policy.get("code_pins")
        require(isinstance(code_pins, dict) and code_pins and
                all(pins.get(path) == sha for path, sha in code_pins.items()) and
                argv[0] in code_pins, "CODE_AUTHORITY")
        require(isinstance(manifest, dict) and manifest == {
            "version": 1, "policy_id": policy["policy_id"], "run_id": approval["run_id"],
            "config_sha256": digest(encoded(config)), "policy_sha256": digest(encoded(policy)),
            "argv_sha256": digest(encoded(argv)), "pins_sha256": digest(encoded(pins))}, "RUN_MANIFEST_BINDING")
    else:
        require(budget.get("config_sha256") == digest(encoded(config)) and
                budget.get("policy_sha256") == digest(encoded(policy)) and
                budget.get("argv_sha256") == digest(encoded(argv)) and
                budget.get("pins_sha256") == digest(encoded(pins)), "DISPATCH_IDENTITY")
    require(isinstance(argv, list) and argv and all(isinstance(x, str) and x for x in argv), "ARGV_REQUIRED")
    require(argv[0] in pins, "EXECUTABLE_PIN_REQUIRED")
    lower, upper = timestamp(budget.get("not_before")), timestamp(budget.get("expires_at"))
    require(lower <= now < upper and timestamp(policy["not_before"]) >= lower and
            timestamp(policy["expires_at"]) <= upper and upper - lower <= 366 * 86400,
            "BUDGET_EXPIRED")
    maximum = number(budget, "max_attempts", 1)
    growth = number(budget, "per_attempt_bytes", 1)
    require(growth >= sum(config["limits"][key] for key in
                          ("source_bytes", "dump_bytes", "metadata_bytes")), "GROWTH_BOUND_UNDERSIZED")
    cost = number(budget, "per_attempt_cost_krw")
    retained = number(budget, "existing_bytes") + number(budget, "reserve_bytes")
    capacity = number(budget, "capacity_bytes", 1)
    cost_cap = number(budget, "cost_cap_krw")
    require(cost >= config["limits"]["projected_cost_krw"] and cost_cap <= config["limits"]["cost_limit_krw"], "COST_AUTHORITY_MISMATCH")
    number(budget, "max_output_bytes", 1)
    require(budget["max_output_bytes"] <= 1024 * 1024, "OUTPUT_CAP")
    verify_files(pins)
    root = safe_path(root)
    require(root.is_dir(), "STATE_ROOT_REQUIRED")
    if os.name == "posix":
        info = root.stat()
        require(info.st_uid == os.geteuid() and stat.S_IMODE(info.st_mode) == 0o700, "PRIVATE_STATE_REQUIRED")
    lock = root / "dispatch.lock"
    try:
        lock.mkdir(mode=0o700)
    except FileExistsError:
        raise GateError("LOCKED_OR_UNCERTAIN") from None
    sync_dir(root)
    uncertain = False
    try:
        identity = {"budget_sha256": digest(encoded(budget))}
        entries = list(root.iterdir())
        require(all(p.name in {"dispatch.lock", "identity.json"} or p.name.startswith("attempt-") for p in entries), "UNEXPECTED_STATE")
        if (root / "identity.json").exists():
            require(load(root / "identity.json") == identity, "BUDGET_IDENTITY_CHANGED")
        else:
            require(len(entries) == 1, "MISSING_STATE_IDENTITY")
            write_new(root / "identity.json", identity)
        attempts = [p for p in root.iterdir() if p.name.startswith("attempt-")]
        for entry in attempts:
            safe_path(entry)
            claim = load(entry / "claim.json")
            require(claim.get("identity") == identity and entry.name == "attempt-" + claim.get("run_id", ""), "CORRUPT_CLAIM")
            require((entry / "result.json").exists(), "UNCERTAIN_ATTEMPT")
            result = load(entry / "result.json")
            require(result in ({"status": "COMPLETE"}, {"status": "FAILED"}), "CORRUPT_RESULT")
        require(len(attempts) < maximum, "ATTEMPT_BUDGET_EXHAUSTED")
        require(retained + (len(attempts) + 1) * growth <= capacity, "CUMULATIVE_STORAGE_EXCEEDED")
        require((len(attempts) + 1) * cost <= cost_cap, "CUMULATIVE_COST_EXCEEDED")
        entry = root / ("attempt-" + approval["run_id"])
        require(not entry.exists(), "DUPLICATE_ATTEMPT")
        entry.mkdir(mode=0o700)
        uncertain = True
        write_new(entry / "claim.json", {"identity": identity, "run_id": approval["run_id"]})
        sync_dir(root)
        verify_files(pins)  # recheck immediately before bounded dispatch
        response = runner.run(argv, budget["max_output_bytes"])
        require(type(response.code) is int and response.size <= budget["max_output_bytes"], "INVALID_RUNNER_RESULT")
        require(response.code == 0, "CAPTURE_OUTCOME_UNCERTAIN")
        if response.code == 0:
            try:
                receipt = json.loads(response.data)
                require(receipt == {"status": "COMPLETE", "restore": "NOT_RUN", "copy": "NOT_RUN"}, "CAPTURE_RECEIPT_REQUIRED")
            except (ValueError, RecursionError):
                raise GateError("CAPTURE_RECEIPT_REQUIRED") from None
        result = {"status": "COMPLETE"}
        write_new(entry / "result.json", result)
        uncertain = False
        return result
    finally:
        # On exception after claim, preserve lock: no blind replay even next day.
        if not uncertain:
            lock.rmdir()
            sync_dir(root)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--execute", action="store_true")
    parser.add_argument("--config")
    parser.add_argument("--policy")
    parser.add_argument("--budget")
    parser.add_argument("--pins")
    parser.add_argument("--state")
    parser.add_argument("--manifest")
    args = parser.parse_args()
    if not args.execute:
        print('{"status":"DISABLED","side_effects":false}')
        return 0
    try:
        require(sys.platform == "linux" and os.geteuid() == 0, "LINUX_ROOT_REQUIRED")
        from backup_runtime import checked_path
        for path in (args.config, args.policy, args.budget, args.pins, args.state):
            require(isinstance(path, str), "INPUT_REFERENCE_REQUIRED")
            checked_path(path, private=True)
        config, policy, budget, pins = map(load, (args.config, args.policy, args.budget, args.pins))
        manifest = None
        if policy.get("version", 1) == 2:
            require(isinstance(args.manifest, str), "RUN_MANIFEST_REQUIRED")
            checked_path(args.manifest, private=True)
            manifest = load(args.manifest)
        require(Path(args.state) == Path(config["state_dir"]) / "scheduled-ledger", "STABLE_LEDGER_REQUIRED")
        directory = Path(__file__).resolve().parent
        script = directory / "guarded_backup.py"
        executable = str(Path(sys.executable).resolve())
        argv = [executable, "-B", str(script), "scheduled", "--config", args.config, "--approval", args.policy]
        required = [executable, str(script), str(directory / "backup_protocol.py"),
                    str(directory / "backup_runtime.py"), str(Path(__file__).resolve()), args.config, args.policy]
        require(all(p in pins for p in required), "EXECUTION_PINS_INCOMPLETE")
        if policy.get("version", 1) == 2:
            require(set(required[:-2]) <= set(policy.get("code_pins", {})), "CODE_AUTHORITY_INCOMPLETE")
        for path in required:
            checked_path(path, trusted=True)
        # Do not spawn a second supervisor: guarded execute owns the only helper
        # groups, deadline and signal cleanup. Nested setsid runners can orphan
        # grandchildren when the outer supervisor escalates to SIGKILL.
        class InProcessGuardedRunner:
            def run(self, actual_argv, max_bytes):
                require(actual_argv == argv and load(args.config) == config and
                        load(args.policy) == policy, "DISPATCH_IDENTITY")
                import guarded_backup
                from types import SimpleNamespace
                value = guarded_backup.execute(config, Path(args.config), Path(args.policy), recurring=True)
                data = encoded(value)
                require(len(data) <= max_bytes, "COMMAND_OUTPUT_LIMIT")
                return SimpleNamespace(code=0, data=data, size=len(data))
        result = dispatch(args.state, config, policy, budget, argv, pins, InProcessGuardedRunner(), time.time(), manifest=manifest)
        print(json.dumps(result))
        return 0 if result["status"] == "COMPLETE" else 2
    except (GateError, OSError, ValueError, TypeError, AttributeError, RecursionError):
        print('{"status":"HOLD","code":"DISPATCH_STOPPED"}')
        return 2


if __name__ == "__main__":
    raise SystemExit(main())

"""Explicit protected file-handoff runner; default inert, no network transport."""
from __future__ import annotations

import argparse
from datetime import datetime, timedelta, timezone
import hashlib
import json
from pathlib import Path, PurePosixPath
import re
import time

from backup_protocol import GateError, encoded, require, timestamp
import status_transfer as transfer


def layout(config):
    require(config.get("version") == 1 and config.get("enabled") is True, "MONITOR_DISABLED")
    mode = config.get("mode")
    require(mode in {"production", "test"}, "MODE")
    root = PurePosixPath(config["root"])
    require(root.is_absolute() and ".." not in root.parts and str(root) != "/", "ROOT")
    test_id = config.get("test_id")
    if mode == "test":
        require(type(test_id) is str and re.fullmatch(r"[A-Za-z0-9_-]{1,40}", test_id), "TEST_ID")
        require("capture_directory" not in config and "dispatch_directory" not in config, "TEST_PRODUCTION_COLLISION")
        base = root / "test" / test_id
    else:
        require(test_id is None, "TEST_PRODUCTION_COLLISION")
        base = root / "production"
    paths = {name: str(base / name) for name in ("journal", "inbox", "outbox", "alerts", "capture", "dispatch")}
    if mode == "production":
        for field, name in (("capture_directory", "capture"), ("dispatch_directory", "dispatch")):
            path = PurePosixPath(config[field])
            require(path.is_absolute() and ".." not in path.parts and not path.is_relative_to(root), "SOURCE_PATH")
            paths[name] = str(path)
    source, target = config["source_machine_sha256"], config["monitor_machine_sha256"]
    require(all(type(x) is str and re.fullmatch(r"[a-f0-9]{64}", x) for x in (source, target)) and source != target, "INDEPENDENT_TARGET_REQUIRED")
    return paths


def window(config, now):
    require(config.get("timezone") == "Asia/Seoul", "TIMEZONE")
    clock = config.get("start_hhmm")
    require(type(clock) is str and re.fullmatch(r"(?:[01][0-9]|2[0-3]):[0-5][0-9]", clock), "CLOCK")
    seconds = config.get("window_seconds")
    require(type(seconds) is int and 60 <= seconds <= 1800, "WINDOW")
    start = datetime.fromtimestamp(now, timezone(timedelta(hours=9))).replace(
        hour=int(clock[:2]), minute=int(clock[3:]), second=0, microsecond=0)
    if now < start.timestamp():
        start -= timedelta(days=1)
    prefix = "production" if config["mode"] == "production" else "test-" + config["test_id"]
    return prefix + "-" + start.strftime("%Y%m%d"), start.timestamp(), start.timestamp() + seconds


def run(config, action, now, machine_sha256, *, store_factory=transfer.NativeStore, smtp_factory=None):
    paths = layout(config)
    require(action in {"export", "monitor"}, "ACTION")
    expected = config["source_machine_sha256"] if action == "export" else config["monitor_machine_sha256"]
    require(machine_sha256 == expected, "HOST_BINDING")
    require(timestamp(config["not_before"]) <= now < timestamp(config["expires_at"]), "CONFIG_EXPIRED")
    window_id, since, by = window(config, now)
    namespace = "production" if config["mode"] == "production" else "test-" + config["test_id"]
    source, target = namespace + "-source", namespace + "-monitor"
    stores = []
    def open_store(name):
        value = store_factory(paths[name]); stores.append(value); return value
    try:
        journal = open_store("journal")
        binding = encoded({"namespace": namespace, "source": config["source_machine_sha256"], "target": config["monitor_machine_sha256"]})
        with journal.lock():
            previous = journal.read("runner-binding", missing=True)
            require(previous is None or previous == binding, "STORE_NAMESPACE_COLLISION")
            if previous is None:
                journal.write("runner-binding", binding)
        if action == "export":
            data = transfer.export(journal, config["key_ref"], source, target,
                                   datetime.fromtimestamp(now, timezone.utc).isoformat(),
                                   dispatch_root=paths["dispatch"], source_store=open_store("capture"))
            open_store("outbox").write("handoff.json", data)
            return {"status": "EXPORTED", "network_transfer": "NOT_RUN"}
        smtp = dict(config["smtp"])
        require(smtp.get("test_id") is None, "TEST_CONFIG_COLLISION")
        if config["mode"] == "test":
            smtp["test_id"] = config["test_id"]
        require(config.get("send_authorized") is True, "SEND_NOT_AUTHORIZED")
        data = open_store("inbox").read("handoff.json", missing=True)
        return transfer.monitor(journal, data, config["key_ref"], source, target, now,
                                window_id, paths["alerts"], smtp,
                                expected_since=since, expected_by=by, smtp_factory=smtp_factory,
                                max_export_age_seconds=config["max_export_age_seconds"])
    finally:
        for store in reversed(stores):
            store.close()


def supervisor_exit(result):
    return 0 if result.get("status") in {"EXPORTED", "CURRENT"} and result.get("delivery") not in {"OUTCOME_UNKNOWN_HOLD", "RESERVED_HOLD"} else 2


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--execute", action="store_true")
    parser.add_argument("--action", choices=("export", "monitor"))
    parser.add_argument("--config")
    args = parser.parse_args(argv)
    if not args.execute:
        print('{"status":"DISABLED","side_effects":false}')
        return 0
    try:
        from backup_runtime import checked_path
        path = checked_path(args.config, private=True)
        config_store = transfer.NativeStore(str(path.parent))
        try:
            config = transfer.decode(config_store.read(path.name))
        finally:
            config_store.close()
        machine = hashlib.sha256(Path("/etc/machine-id").read_bytes()).hexdigest()
        result = run(config, args.action, time.time(), machine)
        print(json.dumps(result, sort_keys=True))
        return supervisor_exit(result)
    except (GateError, OSError, ValueError, TypeError, KeyError, AttributeError):
        print('{"status":"HOLD","code":"MONITOR_RUNNER_FAILED","supervisor_attention":true}')
        return 2


if __name__ == "__main__":
    raise SystemExit(main())

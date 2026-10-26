"""Pure validation and privacy-safe restic protocol for WI-20261010-ATS-013."""

from __future__ import annotations

import hashlib
import json
import re
from datetime import datetime, timedelta, timezone
from pathlib import PurePosixPath
from typing import Any
from urllib.parse import urlsplit

GIB = 1024**3
HEX = re.compile(r"[0-9a-f]{64}\Z")
STAGES = ("PRECHECK", "CLOSE_ADMISSION", "QUIESCENT", "DUMP", "CAPTURE",
          "VERIFY", "CAPTURE_COMPLETE", "RESUME", "COMPLETE", "FAILED")


class GateError(Exception):
    """Expose a fixed code only; never include a path or subprocess output."""


def require(condition: bool, code: str) -> None:
    if not condition:
        raise GateError(code)


def encoded(value: Any) -> bytes:
    return json.dumps(value, sort_keys=True, separators=(",", ":"),
                      allow_nan=False).encode("utf-8")


def digest(value: bytes) -> str:
    return hashlib.sha256(value).hexdigest()


def timestamp(value: str) -> float:
    try:
        parsed = datetime.fromisoformat(value.replace("Z", "+00:00"))
        require(parsed.tzinfo is not None, "TIMEZONE_REQUIRED")
        return parsed.timestamp()
    except (ValueError, TypeError, AttributeError):
        raise GateError("INVALID_TIME") from None


def utc_now() -> str:
    return datetime.now(timezone.utc).isoformat()


def exact_id(value: Any) -> str:
    require(isinstance(value, str) and bool(HEX.fullmatch(value)), "EXACT_ID_REQUIRED")
    return value


def documents(data: bytes) -> list[dict]:
    try:
        rows = [json.loads(line) for line in data.splitlines() if line.strip()]
        require(bool(rows) and all(isinstance(row, dict) for row in rows),
                "INVALID_JSON_LINES")
        return rows
    except (ValueError, UnicodeError):
        raise GateError("INVALID_JSON_LINES") from None


def backup_snapshot(data: bytes, exit_code: int) -> str:
    require(exit_code == 0, "BACKUP_EXIT")
    rows = documents(data)
    summaries = [row for row in rows if row.get("message_type") == "summary"]
    require(len(summaries) == 1 and rows[-1] is summaries[0], "SUMMARY_REQUIRED")
    for row in rows:
        require(row.get("message_type") in {"status", "summary", "verbose_status"},
                "UNKNOWN_BACKUP_MESSAGE")
        require(row.get("error_count", 0) == 0, "READ_ERRORS")
    require(not summaries[0].get("dry_run", False), "DRY_RUN_REJECTED")
    return exact_id(summaries[0].get("snapshot_id"))


def rejected_snapshot(data: bytes) -> str | None:
    """Record a partial snapshot identity privately without accepting its data."""
    try:
        rows = documents(data)
        ids = [exact_id(row.get("snapshot_id")) for row in rows
               if row.get("message_type") == "summary"]
        return ids[0] if len(ids) == 1 else None
    except GateError:
        return None


def snapshot_nodes(data: bytes, snapshot: str) -> dict[str, dict]:
    nodes: dict[str, dict] = {}
    snapshots = 0
    for row in documents(data):
        kind = row.get("message_type", row.get("struct_type"))
        if kind == "snapshot":
            require(row.get("id") == exact_id(snapshot), "SNAPSHOT_MISMATCH")
            snapshots += 1
        elif kind == "node":
            path = row.get("path")
            require(isinstance(path, str) and path.startswith("/") and
                    path not in nodes, "INVALID_NODE")
            nodes[path] = row
        else:
            raise GateError("UNKNOWN_LS_MESSAGE")
    require(snapshots == 1 and bool(nodes), "INVENTORY_REQUIRED")
    return nodes


def verify_nodes(expected: dict[str, dict], actual: dict[str, dict]) -> None:
    # Absolute source paths can also produce ancestor directory entries.
    parents = {str(parent) for path in expected for parent in PurePosixPath(path).parents}
    require(set(expected) <= set(actual), "MISSING_MEMBER")
    require(all(path in expected or (path in parents and node.get("type") == "dir")
                for path, node in actual.items()), "UNEXPECTED_MEMBER")
    for path, item in expected.items():
        node = actual[path]
        require(item["type"] in {"file", "dir"}, "SOURCE_SYMLINK_REVIEW_REQUIRED")
        require(node.get("type") == item["type"], "MEMBER_TYPE")
        mode = node.get("mode", 0)
        require(type(mode) is int and mode >= 0 and
                not mode & ((1 << 23) | (1 << 22) | (1 << 20)) and
                not item["mode"] & 0o7000, "SPECIAL_MODE_REVIEW_REQUIRED")
        require(mode & 0o777 == item["mode"], "MEMBER_MODE")
        require(node.get("uid", 0) == item["uid"] and
                node.get("gid", 0) == item["gid"], "MEMBER_OWNER")
        if item["type"] == "file":
            require(node.get("size", 0) == item["size"], "MEMBER_SIZE")


def linux_path(value: Any) -> str:
    require(isinstance(value, str) and value.startswith("/") and
            not any(c in value for c in "\x00\r\n") and
            ".." not in PurePosixPath(value).parts and
            str(PurePosixPath(value)) == value and value != "/", "INVALID_PATH")
    return value


def overlap(left: str, right: str) -> bool:
    a, b = PurePosixPath(left), PurePosixPath(right)
    return a == b or a in b.parents or b in a.parents


def validate_config(config: dict) -> None:
    require(config.get("configured") is True, "NOT_CONFIGURED")
    require(config.get("version") == 1, "CONFIG_VERSION")
    for key in ("target_host", "logical_host", "region", "owner_ref", "alert_ref",
                "recovery_ref", "source_revision", "dirty_disposition", "schema_ref"):
        value = config.get(key)
        require(isinstance(value, str) and bool(value.strip()) and
                "__" not in value and len(value) <= 256, "CONFIG_IDENTITY")
    require(config.get("class") in {"routine", "prechange"}, "CAPTURE_CLASS")
    for key in ("repository_id", "restic_sha256", "hook_sha256"):
        exact_id(config.get(key))
    uri = config.get("repository", "")
    require(isinstance(uri, str) and uri.startswith("s3:https://"), "PRIVATE_REPOSITORY")
    parsed = urlsplit(uri[3:])
    require(bool(parsed.hostname) and not parsed.username and not parsed.password
            and not parsed.query and not parsed.fragment and "__" not in uri
            and len([p for p in parsed.path.split("/") if p]) >= 2,
            "PRIVATE_REPOSITORY")
    source_keys = ("public_root", "private_root", "release_root")
    runtime_keys = ("state_dir", "work_dir", "cache_dir", "temp_dir")
    for key in source_keys + runtime_keys + ("release_link", "lock_file",
                                           "password_file", "aws_credentials_file", "mysql_defaults_file",
                                           "restic_binary", "hook_binary"):
        linux_path(config.get(key))
    require(config["lock_file"] == "/run/atstudio-operations/operations.lock", "SHARED_LOCK")
    settings = config.get("settings")
    require(isinstance(settings, list) and bool(settings), "SETTINGS_REQUIRED")
    for path in settings:
        linux_path(path)
        require(path != "/etc", "SETTINGS_ALLOWLIST")
    sources = [config[key] for key in source_keys] + settings
    require(len(sources) == len(set(sources)), "SOURCE_OVERLAP")
    for index, path in enumerate(sources):
        require(not any(overlap(path, other) for other in sources[index + 1:]),
                "SOURCE_OVERLAP")
    runtime = [config[key] for key in runtime_keys]
    for index, path in enumerate(runtime):
        require(not any(overlap(path, other) for other in runtime[index + 1:] + sources),
                "RUNTIME_SOURCE_OVERLAP")
    for key in ("password_file", "aws_credentials_file", "mysql_defaults_file",
                "hook_binary", "restic_binary"):
        require(not any(overlap(config[key], path) for path in runtime + sources),
                "CONTROL_SOURCE_OVERLAP")
    limits = config.get("limits", {})
    caps = {"dump_bytes": 2 * GIB, "metadata_bytes": GIB // 4,
            "cache_bytes": GIB, "temp_bytes": GIB}
    for key in (*caps, "other_growth_bytes", "upload_kib", "download_kib",
                "seconds", "source_bytes", "retrieval_bytes", "cost_limit_krw",
                "projected_cost_krw", "max_entries"):
        value = limits.get(key)
        require(type(value) is int and value > 0, "BUDGET_REQUIRED")
        if key in caps:
            require(value <= caps[key], "LOCAL_BUDGET_CAP")
    require(limits["projected_cost_krw"] <= limits["cost_limit_krw"], "COST_BUDGET")
    require(config.get("expected_filesystems") and
            isinstance(config["expected_filesystems"], dict), "MOUNT_IDENTITY_REQUIRED")
    for path, device in config["expected_filesystems"].items():
        linux_path(path)
        require(type(device) is int and device >= 0, "MOUNT_IDENTITY_REQUIRED")


def validate_approval(config: dict, approval: dict, now: float) -> None:
    require(approval.get("operation") == "cold-capture" and
            approval.get("config_sha256") == digest(encoded(config)), "EXACT_APPROVAL")
    require(bool(re.fullmatch(r"[a-f0-9]{32}", approval.get("run_id", ""))), "RUN_ID")
    start, end = timestamp(approval.get("not_before")), timestamp(approval.get("not_after"))
    require(start <= now < end and end - start <= config["limits"]["seconds"],
            "MAINTENANCE_WINDOW")
    require(approval.get("resume_authorized") is True, "RESUME_APPROVAL")


def recurring_approval(config: dict, policy: dict, now: float) -> dict:
    """Derive one daily attempt only inside an explicit short start window."""
    require(policy.get("enabled") is True, "RECURRING_DISABLED")
    require(policy.get("operation") == "daily-cold-capture" and
            policy.get("config_sha256") == digest(encoded(config)) and
            config["class"] == "routine", "EXACT_RECURRING_APPROVAL")
    require(policy.get("timezone") == "Asia/Seoul", "RECURRING_TIMEZONE")
    clock = policy.get("start_hhmm", "")
    require(isinstance(clock, str) and bool(re.fullmatch(r"(?:[01][0-9]|2[0-3]):[0-5][0-9]", clock)),
            "RECURRING_CLOCK")
    grace, seconds = policy.get("start_grace_seconds"), policy.get("window_seconds")
    require(type(grace) is int and 0 < grace <= 60 and type(seconds) is int and
            grace < seconds <= config["limits"]["seconds"], "RECURRING_WINDOW")
    lower, upper = timestamp(policy.get("not_before")), timestamp(policy.get("expires_at"))
    require(lower <= now < upper, "RECURRING_EXPIRED")
    local = datetime.fromtimestamp(now, timezone(timedelta(hours=9)))
    hour, minute = map(int, clock.split(":"))
    start = local.replace(hour=hour, minute=minute, second=0, microsecond=0)
    end = start + timedelta(seconds=seconds)
    require(start.date() == end.date(), "RECURRING_CROSSES_DAY")
    require(start.timestamp() >= lower and end.timestamp() <= upper,
            "RECURRING_EXPIRED")
    require(0 <= now - start.timestamp() < grace, "NO_CATCHUP_WINDOW")
    require(policy.get("resume_authorized") is True, "RESUME_APPROVAL")
    approval = {"operation": "cold-capture", "config_sha256": policy["config_sha256"],
                "run_id": digest(encoded([policy["config_sha256"], str(start.date()),
                                           "daily-cold-capture"]))[:32],
                "not_before": start.isoformat(), "not_after": end.isoformat(),
                "resume_authorized": True}
    validate_approval(config, approval, now)
    return approval


def restore_gate(plan: dict) -> None:
    exact_id(plan.get("snapshot_id"))
    for key in ("new_isolated_target", "fresh_database", "new_roots", "egress_denied",
                "services_disabled", "timers_disabled", "scheduler_suppression_proved",
                "no_effective_live_credentials", "exact_target_approved"):
        require(plan.get(key) is True, "RESTORE_CONTAINMENT_REQUIRED")
    linux_path(plan.get("target"))
    require(type(plan.get("byte_budget")) is int and plan["byte_budget"] > 0,
            "RESTORE_BUDGET_REQUIRED")


def status_report(state: dict, now: float, expected_by: float | None,
                  copy: dict | None, expected_since: float | None = None) -> dict:
    alerts: list[str] = []
    attempt = state.get("attempt", {})
    good = state.get("last_good")
    if attempt.get("stage") == "FAILED":
        alerts.append("CAPTURE_FAILED")
    elif attempt.get("stage") not in {None, "COMPLETE"}:
        alerts.append("CAPTURE_INCOMPLETE")
    if attempt.get("maintenance"):
        alerts.append("MAINTENANCE_REQUIRES_OPERATOR")
    if good:
        exact_id(good.get("snapshot_id"))
    good_time = timestamp(good["captured_at"]) if good else None
    require(good_time is None or good_time <= now, "FUTURE_RECEIPT")
    if good_time is None or now - good_time > 26 * 3600:
        alerts.append("CAPTURE_26H_STALE")
    if expected_by is not None:
        require(expected_since is not None and expected_since < expected_by,
                "SCHEDULE_WINDOW_REQUIRED")
        if now > expected_by and (good_time is None or good_time < expected_since):
            alerts.append("CAPTURE_MISSED")
    if expected_by is None:
        alerts.append("SCHEDULE_NOT_CONFIGURED")
    copy_time = None
    if copy:
        require(copy.get("independently_verified") is True, "COPY_NOT_VERIFIED")
        exact_id(copy.get("source_snapshot_id"))
        exact_id(copy.get("destination_snapshot_id"))
        copy_time = timestamp(copy.get("captured_at"))
        require(copy_time <= now, "FUTURE_RECEIPT")
    if copy_time is None or now - copy_time > 8 * 86400:
        alerts.append("INDEPENDENT_COPY_8D_STALE")
    return {"status": "ATTENTION" if alerts else "CURRENT", "alerts": alerts,
            "external_send": "NOT_IMPLEMENTED"}

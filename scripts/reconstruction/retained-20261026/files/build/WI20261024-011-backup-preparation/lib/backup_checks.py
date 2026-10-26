"""WI069 pure preparation checks, not a native capture/restore dispatcher."""

from __future__ import annotations

import json
import math
from pathlib import Path, PurePosixPath
import re
import sys
from typing import Any


CORE = Path(__file__).resolve().parents[2] / "scripts/deployment/backup"
LOCAL = Path(__file__).resolve().parent
sys.path.insert(0, str(LOCAL if (LOCAL / "backup_protocol.py").is_file() else CORE))
from backup_protocol import (GateError, backup_snapshot, digest, encoded, exact_id,
                             linux_path, overlap, require, snapshot_nodes, verify_nodes)


ROLES = {"public", "private", "backend", "frontend"}
IDENTIFIER = re.compile(r"[a-z][a-z0-9_]{0,63}\Z")
MAX_METADATA = 16 * 1024**2
INITIAL_REPOSITORY_TARGET = 1_000_000_000


def strict_json(raw: bytes) -> dict:
    """Reject duplicate keys and nonfinite numbers in private receipts."""
    require(len(raw) <= MAX_METADATA, "JSON_LIMIT")

    def unique(pairs: list[tuple[str, Any]]) -> dict:
        value = {}
        for key, item in pairs:
            require(key not in value, "DUPLICATE_JSON")
            value[key] = item
        return value

    def invalid_constant(value: str) -> None:
        raise GateError("NONFINITE_JSON")

    def finite_float(value: str) -> float:
        parsed = float(value)
        require(math.isfinite(parsed), "NONFINITE_JSON")
        return parsed

    try:
        value = json.loads(raw, object_pairs_hook=unique, parse_constant=invalid_constant,
                           parse_float=finite_float)
    except (ValueError, UnicodeError):
        raise GateError("INVALID_JSON") from None
    require(isinstance(value, dict), "JSON_OBJECT_REQUIRED")
    return value


def identifier(value: str) -> str:
    """Accept the existing simple schema/table naming contract only."""
    require(type(value) is str and bool(IDENTIFIER.fullmatch(value)), "SQL_IDENTIFIER")
    return value


def validate_layout(roots: dict[str, str], controls: list[str],
                    restore_parent: str) -> None:
    """Keep complete roots disjoint from credentials and temporary outputs."""
    require(set(roots) == ROLES, "ALL_ROOTS_REQUIRED")
    require(type(controls) is list and len(controls) >= 5, "CONTROL_PATHS_REQUIRED")
    sources = [linux_path(value) for value in roots.values()]
    protected = [linux_path(value) for value in controls] + [linux_path(restore_parent)]
    all_paths = sources + protected
    for index, value in enumerate(all_paths):
        require(not any(overlap(value, other) for other in all_paths[index + 1:]),
                "PATH_OVERLAP")


def validate_members(members: dict[str, dict], roots: dict[str, str],
                     max_entries: int, max_bytes: int) -> int:
    """Check a complete manifest shape; traversal/completeness is the collector's job."""
    require(type(max_entries) is int and 0 < max_entries <= 100000
            and type(max_bytes) is int and 0 < max_bytes < INITIAL_REPOSITORY_TARGET,
            "MEASURED_BUDGET_REQUIRED")
    require(type(members) is dict and 0 < len(members) <= max_entries, "ENTRY_LIMIT")
    require(len(encoded(members)) <= MAX_METADATA, "METADATA_LIMIT")
    require(set(roots) == ROLES, "ALL_ROOTS_REQUIRED")
    for root in roots.values():
        linux_path(root)
        require(root in members and members[root].get("type") == "dir", "MISSING_ROOT")
    for role in ("public", "private"):
        staging = roots[role] + "/.staging"
        require(staging in members and members[staging].get("type") == "dir",
                "MISSING_STAGING")
    total = 0
    for path, row in members.items():
        linux_path(path)
        require(sum(PurePosixPath(root) == PurePosixPath(path)
                    or PurePosixPath(root) in PurePosixPath(path).parents
                    for root in roots.values()) == 1, "UNEXPECTED_MEMBER")
        expected = {"type", "mode", "uid", "gid"}
        if row.get("type") == "file":
            expected |= {"size", "sha256"}
            require(type(row.get("size")) is int and row["size"] >= 0, "MEMBER_SIZE")
            exact_id(row.get("sha256"))
            total += row["size"]
        else:
            require(row.get("type") == "dir", "SPECIAL_MEMBER")
        require(set(row) == expected, "MEMBER_SHAPE")
        require(type(row["mode"]) is int and 0 <= row["mode"] <= 0o777, "MEMBER_MODE")
        for key in ("uid", "gid"):
            require(type(row[key]) is int and row[key] >= 0, "MEMBER_OWNER")
        if path not in roots.values():
            parent = str(PurePosixPath(path).parent)
            require(parent in members and members[parent]["type"] == "dir", "MISSING_PARENT")
    require(total <= max_bytes, "SOURCE_BYTE_LIMIT")
    return total


def prepare_manifest(roots: dict[str, str], members: dict[str, dict],
                     reviewed_inventory_sha256: str, release_pins: dict[str, str],
                     max_entries: int, max_bytes: int) -> dict:
    """Bind a reviewed no-runtime-key inventory without claiming host readiness."""
    size = validate_members(members, roots, max_entries, max_bytes)
    require(digest(encoded(members)) == exact_id(reviewed_inventory_sha256),
            "REVIEWED_INVENTORY_DRIFT")
    require(type(release_pins) is dict and bool(release_pins), "RELEASE_PINS_REQUIRED")
    covered = set()
    for path, sha in release_pins.items():
        require(path in members and members[path]["type"] == "file"
                and members[path]["sha256"] == exact_id(sha), "RELEASE_PIN_DRIFT")
        for role in ("backend", "frontend"):
            if PurePosixPath(roots[role]) in PurePosixPath(path).parents:
                covered.add(role)
    require(covered == {"backend", "frontend"}, "RELEASE_COVERAGE")
    return {
        "version": 1, "scope": "pre-feature-no-runtime-keys", "roots": roots,
        "members": members, "logical_source_bytes": size,
        "reviewed_inventory_sha256": reviewed_inventory_sha256,
        "release_pins": release_pins, "full_disaster_recovery": False,
        "excluded_key_classes": ["smtp", "jwt", "billing", "repository-access"],
        "capture": "NOT_RUN", "restore": "NOT_RUN",
    }


def strict_backup_snapshot(data: bytes, exit_code: int) -> str:
    """Validate the exact ID before even requesting a listing from the repository."""
    require(type(exit_code) is int and len(data) <= MAX_METADATA, "BACKUP_WIRE_LIMIT")
    for line in data.splitlines():
        if not line.strip():
            continue
        row = strict_json(line)
        if "error_count" in row:
            require(type(row["error_count"]) is int, "WIRE_ERROR_COUNT")
        if "dry_run" in row:
            require(type(row["dry_run"]) is bool, "WIRE_DRY_RUN")
    return backup_snapshot(data, exit_code)


def verify_snapshot(expected: dict[str, dict], backup_output: bytes, exit_code: int,
                    listing: bytes, previous: set[str]) -> str:
    """Strictly validate wire types before using the unchanged exact-snapshot protocol."""
    require(type(exit_code) is int, "BACKUP_EXIT_TYPE")
    for data, source in ((backup_output, "backup"), (listing, "listing")):
        require(len(data) <= MAX_METADATA, "JSON_LIMIT")
        rows = [strict_json(line) for line in data.splitlines() if line.strip()]
        require(bool(rows), "INVALID_JSON_LINES")
        for row in rows:
            for key in ("message_type", "struct_type"):
                if key in row:
                    require(type(row[key]) is str, "WIRE_MESSAGE_TYPE")
            if "message_type" in row and "struct_type" in row:
                require(row["message_type"] == row["struct_type"], "WIRE_MESSAGE_CONFLICT")
            if source == "backup":
                if "error_count" in row:
                    require(type(row["error_count"]) is int and row["error_count"] >= 0,
                            "WIRE_ERROR_COUNT")
                if "dry_run" in row:
                    require(type(row["dry_run"]) is bool, "WIRE_DRY_RUN")
            else:
                for key in ("uid", "gid", "size", "mode"):
                    if key in row:
                        require(type(row[key]) is int and row[key] >= 0, "WIRE_NODE_INTEGER")
                if row.get("message_type", row.get("struct_type")) == "node":
                    linux_path(row.get("path"))
                    require(type(row.get("type")) is str, "WIRE_NODE_TYPE")
    snapshot = strict_backup_snapshot(backup_output, exit_code)
    require(snapshot not in previous, "SNAPSHOT_NOT_NEW")
    verify_nodes(expected, snapshot_nodes(listing, snapshot))
    return snapshot


def verify_restored(expected: dict[str, dict], actual: dict[str, dict],
                    destination: str) -> None:
    """Compare every restored file hash and ordinary mode/owner, including empty roots."""
    root = PurePosixPath(linux_path(destination))
    require(bool(expected) and bool(actual), "RESTORE_INVENTORY_REQUIRED")
    remapped = {}
    for path, row in expected.items():
        linux_path(path)
        require(not overlap(path, str(root)), "RESTORE_SOURCE_OVERLAP")
        target = str(root / path.lstrip("/"))
        require(target not in remapped, "DUPLICATE_MEMBER")
        remapped[target] = row
    parents = {str(parent) for value in remapped for parent in PurePosixPath(value).parents
               if parent == root or root in parent.parents}
    require(set(remapped) <= set(actual), "RESTORE_MISSING_MEMBER")
    for path, row in actual.items():
        linux_path(path)
        require(root == PurePosixPath(path) or root in PurePosixPath(path).parents,
                "RESTORE_PATH_ESCAPE")
        require(path in remapped or (path in parents and row.get("type") == "dir"),
                "RESTORE_UNEXPECTED_MEMBER")
        if path in remapped:
            require(row == remapped[path], "RESTORE_CONTENT_OR_METADATA")
        else:
            require(set(row) == {"type", "mode", "uid", "gid"}
                    and type(row["mode"]) is int and 0 <= row["mode"] <= 0o777,
                    "RESTORE_ANCESTOR_MODE")


def mysql_inventory_query(schema: str) -> bytes:
    """Return read-only SQL for all table objects, including unsupported ones."""
    name = identifier(schema)
    return ("SELECT TABLE_NAME,TABLE_TYPE,COALESCE(ENGINE,'') "
            "FROM information_schema.TABLES WHERE TABLE_SCHEMA='" + name
            + "' ORDER BY TABLE_NAME;\n").encode("ascii")


def parse_tables(raw: bytes) -> tuple[str, ...]:
    """Require complete base-table/InnoDB inventory; views need separate target review."""
    require(len(raw) <= 65536, "TABLE_OUTPUT_LIMIT")
    try:
        rows = [line.split("\t") for line in raw.decode("ascii").splitlines()]
    except UnicodeError:
        raise GateError("TABLE_ENCODING") from None
    require(0 < len(rows) <= 256, "TABLE_COUNT")
    tables = []
    for row in rows:
        require(len(row) == 3 and row[1:] == ["BASE TABLE", "InnoDB"],
                "TABLE_ENGINE_OR_TYPE_REVIEW")
        tables.append(identifier(row[0]))
    require(tables == sorted(set(tables)), "TABLE_DUPLICATE_OR_ORDER")
    return tuple(tables)


def row_count_query(schema: str, tables: tuple[str, ...]) -> bytes:
    """Count every observed table without using table_rows estimates."""
    name = identifier(schema)
    require(0 < len(tables) <= 256 and len(set(tables)) == len(tables), "TABLE_COUNT")
    query = " UNION ALL ".join(
        f"SELECT '{identifier(table)}',COUNT(*) FROM `{name}`.`{table}`" for table in tables)
    return (query + ";\n").encode("ascii")


def parse_counts(raw: bytes, tables: tuple[str, ...]) -> dict[str, int]:
    """Require exactly one nonnegative count for every enumerated table."""
    require(0 < len(tables) <= 256 and len(set(tables)) == len(tables), "TABLE_COUNT")
    require(len(raw) <= 65536, "COUNT_OUTPUT_LIMIT")
    try:
        rows = [line.split("\t") for line in raw.decode("ascii").splitlines()]
    except UnicodeError:
        raise GateError("COUNT_ENCODING") from None
    require(len(rows) == len(tables) and all(len(row) == 2 for row in rows), "COUNT_SHAPE")
    result = {}
    for row, table in zip(rows, tables):
        require(row[0] == identifier(table) and bool(re.fullmatch(r"[0-9]{1,20}", row[1])),
                "COUNT_IDENTITY")
        result[table] = int(row[1])
    return result


def dump_arguments(binary: str, defaults_file: str, unix_socket: str, schema: str,
                   mysql_version: str, *, schema_only: bool = False) -> list[str]:
    """Prepare MySQL8.0 arguments only; no process or credential file is accessed."""
    require(type(mysql_version) is str and bool(re.fullmatch(r"8\.0\.[0-9]+", mysql_version)),
            "MYSQL_VERSION_REVIEW")
    for value in (binary, defaults_file, unix_socket):
        linux_path(value)
    require(type(schema_only) is bool, "DUMP_MODE")
    args = [binary, "--defaults-file=" + defaults_file, "--no-login-paths",
            "--protocol=socket", "--socket=" + unix_socket, "--single-transaction",
            "--quick", "--hex-blob", "--routines", "--events", "--triggers",
            "--set-gtid-purged=OFF", "--no-tablespaces", "--skip-comments",
            "--skip-dump-date", "--skip-extended-insert", "--order-by-primary",
            "--skip-add-drop-table", "--tz-utc"]
    if schema_only:
        args.append("--no-data")
    return [*args, identifier(schema)]


def verify_sql_roundtrip(source_counts: dict[str, int], restored_counts: dict[str, int],
                         source_schema_sha256: str, restored_schema_sha256: str,
                         source_dump_sha256: str, restored_dump_sha256: str) -> None:
    """Require all-table counts, deterministic schema and full logical re-dump equality."""
    require(bool(source_counts) and source_counts == restored_counts, "SQL_ROW_DRIFT")
    for table, count in source_counts.items():
        identifier(table)
        require(type(count) is int and count >= 0, "SQL_COUNT_INVALID")
    require(exact_id(source_schema_sha256) == exact_id(restored_schema_sha256),
            "SQL_SCHEMA_DRIFT")
    require(exact_id(source_dump_sha256) == exact_id(restored_dump_sha256), "SQL_DATA_DRIFT")


def budget_projection(source_bytes: int, dump_bytes: int, metadata_bytes: int,
                      restic_overhead_allowance: int) -> dict:
    """Escalate the approved initial target; never assume deduplication savings."""
    values = (source_bytes, dump_bytes, metadata_bytes, restic_overhead_allowance)
    require(all(type(value) is int and value >= 0 for value in values)
            and dump_bytes > 0 and metadata_bytes > 0 and restic_overhead_allowance > 0,
            "MEASURED_BUDGET_REQUIRED")
    total = sum(values)
    require(total < INITIAL_REPOSITORY_TARGET, "INITIAL_TARGET_EXCEEDED")
    return {"projected_bytes": total, "storage_krw_per_month_ex_vat": total * 20 / 1e9,
            "price_source": "MA_QUOTED_NOT_REVERIFIED", "billing_hard_cap": False,
            "provider_metering": "NOT_OBSERVED"}


def main() -> int:
    """Advertise preparation state only; no execution operation exists yet."""
    print(json.dumps({
        "status": "PREPARATION_ONLY", "native_adapter": "PENDING_TARGET_FACTS_AND_REVIEW",
        "backup": "NOT_RUN", "restore": "NOT_RUN", "side_effects": False,
    }, sort_keys=True))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

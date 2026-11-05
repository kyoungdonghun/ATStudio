"""Pinned current-state admission for a retained, separately authorized backup.

No network or database lifecycle on import. Cell hashing deliberately matches
the retained stage-two fixture snapshot representation; hashes remain private.
"""
from __future__ import annotations

import hashlib
import json
from pathlib import Path
import re
import time

SCHEMA = None
MAX_RESPONSE = 4 * 1024 * 1024
ROLES = {1: "ADMIN", 2: "ADMIN", 3: "USER"}
GRANT = dict(id=1, user_id=3, subscription_id=1, status="CANCELLED",
             billing_cycle="MONTHLY", started_at="2026-10-24", expires_at="2026-11-24",
             pending_subscription_id=None, pending_billing_cycle=None)
PLANS = (
    (1, "INDIVIDUAL", 9900, 99000, 5, 1, 3),
    (2, "INDIVIDUAL", 19900, 199000, 20, 2, 10),
    (3, "INDIVIDUAL", 29900, 299000, -1, 2, 10),
    (4, "BUSINESS", 19900, 199000, 10, 1, 3),
    (5, "BUSINESS", 49900, 499000, 50, 2, 10),
    (6, "BUSINESS", 99900, 999000, -1, 2, 10),
)


def need(ok, code):
    if not ok:
        raise ValueError(code)


def canonical(value):
    return json.dumps(value, sort_keys=True, separators=(",", ":"),
                      ensure_ascii=True, allow_nan=False).encode()


def digest(value):
    return hashlib.sha256(canonical(value)).hexdigest()


def pin(value):
    return type(value) is str and re.fullmatch("[a-f0-9]{64}", value) is not None


def cell(value):
    return None if value is None else hashlib.sha256(str(value).encode()).hexdigest()


def identifier(value):
    need(type(value) is str and re.fullmatch("[a-z_]+", value), "STATE_IDENTIFIER")
    return "`" + value + "`"


def unique(pairs):
    result = {}
    for key, value in pairs:
        need(key not in result, "STATE_DUPLICATE_KEY")
        result[key] = value
    return result


def schema():
    value = SCHEMA
    if value is None:
        value = json.loads((Path(__file__).parent / "state-schema.json").read_bytes(),
                           object_pairs_hook=unique)
    need(type(value) is dict and len(value) == 43, "STATE_SCHEMA")
    for table, spec in value.items():
        identifier(table)
        need(type(spec) is dict and set(spec) == {"columns", "primary"}, "STATE_SCHEMA")
        for key in ("columns", "primary"):
            need(type(spec[key]) is list and 0 < len(spec[key]) <= 100
                 and len(set(spec[key])) == len(spec[key]), "STATE_SCHEMA")
            for name in spec[key]:
                identifier(name)
        need(set(spec["primary"]) <= set(spec["columns"]), "STATE_SCHEMA")
    return value


def validate_grant(value):
    need(type(value) is dict and value == GRANT
         and all(type(value[k]) is int for k in ("id", "user_id", "subscription_id")),
         "CURRENT_GRANT")


def validate_anchor(approval, mode, now=None):
    """The external decision must pin this historical anchor independently.

    It is not produced from a fresh observation. No function here creates a
    native anchor or turns an unknown observed state into approved authority.
    """
    anchor = approval["approved_state_anchor"]
    anchor_pin = approval["approved_state_anchor_sha256"]
    if mode == "inventory":
        need(anchor is None and anchor_pin is None, "INVENTORY_NO_STATE_ANCHOR")
        return None
    now = int(time.time()) if now is None else now
    need(type(approval["issued_at"]) is int and type(approval["expires_at"]) is int
         and approval["issued_at"] <= now < approval["expires_at"], "STATE_APPROVAL_WINDOW")
    need(type(anchor) is dict and set(anchor) == {
        "schema", "evidence_kind", "source_wi", "source_receipt_sha256",
        "machine_id_sha256", "schema_sha256", "snapshot_sha256", "table_counts"},
        "STATE_ANCHOR_SHAPE")
    need(anchor["schema"] == "ats-approved-state-v1"
         and anchor["evidence_kind"] == "NATIVE_POST_STAGE_TWO_RECONCILIATION"
         and anchor["source_wi"] == "WI-20261025-ATS-003", "STATE_ANCHOR_ORIGIN")
    need(pin(anchor_pin) and digest(anchor) == anchor_pin, "STATE_ANCHOR_PIN")
    for key in ("source_receipt_sha256", "machine_id_sha256", "schema_sha256", "snapshot_sha256"):
        need(pin(anchor[key]), "STATE_ANCHOR_VALUE")
    need(anchor["machine_id_sha256"] == approval["machine_id_sha256"], "STATE_ANCHOR_TARGET")
    spec = schema()
    need(anchor["schema_sha256"] == digest(spec), "STATE_ANCHOR_SCHEMA")
    counts = anchor["table_counts"]
    need(type(counts) is dict and set(counts) == set(spec)
         and all(type(n) is int and 0 <= n <= 256 for n in counts.values()), "STATE_TABLES")
    need(counts["users"] == 3 and counts["subscriptions"] == 6
         and counts["user_subscriptions"] == 1
         and counts["admin_operation_audit_logs"] >= 3, "STATE_FIXED_COUNTS")
    need(approval["expected_fixture_counts"] == {key: counts[key] for key in
         ("users", "user_subscriptions", "admin_operation_audit_logs")}, "STATE_COUNT_BINDING")
    validate_grant(approval["expected_grant"])
    return anchor


def query_bytes(spec):
    """One read-only consistent transaction; no cleartext private cells returned."""
    statements = ["USE `atstudio_prod`;",
                  "SET SESSION max_execution_time=3000;",
                  "SET SESSION TRANSACTION ISOLATION LEVEL REPEATABLE READ;",
                  "START TRANSACTION WITH CONSISTENT SNAPSHOT, READ ONLY;",
                  "SELECT JSON_OBJECT('kind','schema','table',TABLE_NAME,'column',COLUMN_NAME,"
                  "'ordinal',ORDINAL_POSITION,'primary',IF(COLUMN_KEY='PRI',1,0)) "
                  "FROM information_schema.COLUMNS WHERE TABLE_SCHEMA='atstudio_prod' "
                  "ORDER BY TABLE_NAME,ORDINAL_POSITION;"]
    for table, item in sorted(spec.items()):
        columns = ",".join("'" + name + "',IF(" + identifier(name) +
            " IS NULL,NULL,SHA2(CAST(" + identifier(name) + " AS CHAR CHARACTER SET utf8mb4),256))"
            for name in item["columns"])
        key = ",".join(identifier(name) for name in item["primary"])
        statements.append("SELECT JSON_OBJECT('kind','row','table','" + table +
            "','key',JSON_ARRAY(" + key + "),'cells',JSON_OBJECT(" + columns +
            ")) FROM " + identifier(table) + " ORDER BY " + key + " LIMIT 257;")
    return ("\n".join(statements + ["ROLLBACK;"]) + "\n").encode("ascii")


def parse_snapshot(raw, spec):
    need(type(raw) is bytes and 0 < len(raw) <= MAX_RESPONSE, "STATE_RESPONSE_BOUND")
    rows = [json.loads(line, object_pairs_hook=unique) for line in raw.splitlines()]
    result = {name: {} for name in spec}
    columns = {name: [] for name in spec}
    primaries = {name: [] for name in spec}
    data_started = False
    for row in rows:
        need(type(row) is dict and row.get("table") in spec, "STATE_ROW_TABLE")
        table = row["table"]
        if row.get("kind") == "schema":
            need(not data_started and set(row) == {"kind", "table", "column", "ordinal", "primary"}
                 and type(row["ordinal"]) is int and row["ordinal"] == len(columns[table]) + 1
                 and type(row["primary"]) is int and row["primary"] in (0, 1), "STATE_COLUMN")
            columns[table].append(row["column"])
            if row["primary"]:
                primaries[table].append(row["column"])
        else:
            data_started = True
            need(row.get("kind") == "row" and set(row) == {"kind", "table", "key", "cells"}, "STATE_ROW")
            need(type(row["key"]) is list and len(row["key"]) == len(spec[table]["primary"])
                 and all(type(k) is int and 0 < k < 2**63 for k in row["key"]), "STATE_PRIMARY")
            need(type(row["cells"]) is dict and set(row["cells"]) == set(spec[table]["columns"])
                 and all(v is None or pin(v) for v in row["cells"].values()), "STATE_CELLS")
            key = json.dumps(row["key"], separators=(",", ":"))
            need(key not in result[table] and len(result[table]) < 256, "STATE_ROW_LIMIT")
            need(all(row["cells"][name] == cell(value) for name, value in
                     zip(spec[table]["primary"], row["key"])), "STATE_PRIMARY_CELL")
            result[table][key] = row["cells"]
    need(all(columns[t] == spec[t]["columns"] and primaries[t] == spec[t]["primary"]
             for t in spec), "STATE_SCHEMA_DRIFT")
    return result


def validate_snapshot(snapshot, anchor):
    def match(table, key, values):
        row = snapshot[table].get(key)
        need(row is not None and all(row.get(k) == cell(v) for k, v in values.items()), "CURRENT_STATE")
    need(set(snapshot["users"]) == {"[1]", "[2]", "[3]"}, "CURRENT_ACTORS")
    for uid, role in ROLES.items():
        match("users", f"[{uid}]", dict(id=uid, role=role, is_deleted=0, is_verified=1))
    need(set(snapshot["user_subscriptions"]) == {"[1]"}, "CURRENT_GRANTS")
    match("user_subscriptions", "[1]", GRANT)
    need(set(snapshot["subscriptions"]) == {f"[{i}]" for i in range(1, 7)}, "CURRENT_PLANS")
    for uid, kind, monthly, yearly, daily, channels, playlists in PLANS:
        match("subscriptions", f"[{uid}]", dict(id=uid, user_type=kind, is_active=1,
              price_monthly=f"{monthly}.00", price_yearly=f"{yearly}.00", download_per_day=daily,
              max_whitelist_channels=channels, max_playlists=playlists))
    counts = {name: len(rows) for name, rows in snapshot.items()}
    need(counts == anchor["table_counts"], "CURRENT_TABLE_COUNTS")
    need(digest(snapshot) == anchor["snapshot_sha256"], "APPROVED_STATE_DRIFT")
    return counts


def observe(capture):
    """Called inside the existing source-DB window, before any new backup write."""
    capture.guard()
    anchor = validate_anchor(capture.approval, "capture")
    spec = schema()
    capture.counter += 1
    path = capture.work / ("current-state-query-" + str(capture.counter) + ".sql")
    # Reuse the retained command/file/timeout/diagnostic machinery, not a new DB lifecycle.
    import bootstrap as boot
    import cold_capture as cold
    boot.exclusive(path, query_bytes(spec))
    raw = capture.command(["/usr/bin/mysql", "--no-defaults", "--protocol=socket",
                           "--socket=" + cold.SOCKET, "--user=root", "--batch",
                           "--skip-column-names", "--connect-timeout=3",
                           "--default-character-set=utf8mb4"],
                          label="MYSQL_QUERY", input_file=path, cap=MAX_RESPONSE)
    snapshot = parse_snapshot(raw, spec)
    counts = validate_snapshot(snapshot, anchor)
    capture.guard()
    validate_anchor(capture.approval, "capture")
    proof = dict(status="APPROVED_STATE_MATCH", observed_at=int(time.time()),
                 schema_sha256=digest(spec), snapshot_sha256=digest(snapshot), table_counts=counts,
                 approved_state_anchor_sha256=capture.approval["approved_state_anchor_sha256"])
    boot.exclusive(capture.work / "current-state-proof.json", canonical(proof))
    return proof

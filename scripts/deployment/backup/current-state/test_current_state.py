"""Synthetic local checks only. No record produced here is native authority."""
import ast
import base64
import copy
import importlib
import io
import json
from pathlib import Path
import subprocess
import sys
import tempfile
import time
import unittest
from types import SimpleNamespace
from unittest.mock import Mock, patch

import current_state as guard
import prepare_successor as builder

ROOT = builder.REPO
SCHEMA = json.loads((ROOT / builder.SCHEMA_SOURCE).read_bytes())
guard.SCHEMA = SCHEMA


def synthetic_snapshot():
    result = {name: {} for name in SCHEMA}

    def row(table, key, values):
        data = dict.fromkeys(SCHEMA[table]["columns"])
        data.update({name: guard.cell(value) for name, value in values.items()})
        result[table][key] = data

    for uid, role in guard.ROLES.items():
        row("users", f"[{uid}]", dict(id=uid, role=role, is_deleted=0, is_verified=1))
    row("user_subscriptions", "[1]", guard.GRANT)
    for uid, kind, monthly, yearly, daily, channels, playlists in guard.PLANS:
        row("subscriptions", f"[{uid}]", dict(id=uid, user_type=kind, is_active=1,
            price_monthly=f"{monthly}.00", price_yearly=f"{yearly}.00", download_per_day=daily,
            max_whitelist_channels=channels, max_playlists=playlists))
    for uid in (1, 2, 3):
        row("admin_operation_audit_logs", f"[{uid}]", dict(id=uid, action="SYNTHETIC"))
    return result


def protocol(snapshot):
    rows = []
    for table, spec in sorted(SCHEMA.items()):
        rows += [dict(kind="schema", table=table, column=column, ordinal=i + 1,
                      primary=int(column in spec["primary"]))
                 for i, column in enumerate(spec["columns"])]
    for table, values in sorted(snapshot.items()):
        rows += [dict(kind="row", table=table, key=json.loads(key), cells=cells)
                 for key, cells in values.items()]
    return b"\n".join(guard.canonical(row) for row in rows) + b"\n"


def anchor(snapshot):
    # Native-shaped synthetic fixture only, never persisted as an execution input.
    return dict(schema="ats-approved-state-v1", evidence_kind="NATIVE_POST_STAGE_TWO_RECONCILIATION",
                source_wi="WI-20261025-ATS-003", source_receipt_sha256="a" * 64,
                machine_id_sha256="b" * 64, schema_sha256=guard.digest(SCHEMA),
                snapshot_sha256=guard.digest(snapshot),
                table_counts={name: len(rows) for name, rows in snapshot.items()})


def approval(c, candidate, mode="capture"):
    now = int(time.time())
    snap = synthetic_snapshot()
    a = dict(wi=c.WI, scope=c.SCOPE, mode=mode, allow_native=True,
             candidate_sha256=candidate, decision_sha256=None, cost_sha256=None,
             run_id="1" * 32, issued_at=now - 1, expires_at=now + 1200,
             boot_id="11111111-1111-1111-1111-111111111111", machine_id_sha256="b" * 64,
             repository_id=c.REPOSITORY_ID, bootstrap_run_id=c.BOOTSTRAP_RUN,
             recovery_password_sha256="c" * 64 if mode == "capture" else None,
             inventory_sha256="d" * 64 if mode == "capture" else None,
             units_sha256="e" * 64 if mode == "capture" else None,
             expected_fixture_counts=dict(users=3, user_subscriptions=1, admin_operation_audit_logs=3),
             expected_grant=dict(guard.GRANT),
             approved_state_anchor=anchor(snap) if mode == "capture" else None,
             approved_state_anchor_sha256=guard.digest(anchor(snap)) if mode == "capture" else None)
    source = dict(repository_id=c.REPOSITORY_ID, observed_repository_bytes=64000000,
                  max_capture_bytes=900000000, scope="METADATA_LIST_NEW_BACKUP_EXACT_SNAPSHOT_RESTORE",
                  request_and_egress_bound_verified=True, retained_storage_bound_verified=True,
                  tax_included=True, no_deletion_assumed=True, continuing_storage_after_horizon_accounted=True,
                  evidence_references=["f" * 64])
    cost = dict(status="ZERO_ADDITIONAL_PAID_COST_VERIFIED_BY_MA", observed_at=now,
                expires_at=now + 1800, source_sha256=c.sha(c.canonical(source)),
                account_allowance_verified=True, rates_requests_egress_tax_verified=True,
                retention_days=30, additional_paid_upper_bound_minor=0, currency="KRW", unknowns=[],
                billing_bound_method="INDEPENDENTLY_VERIFIED_PROVIDER_OR_ALLOWANCE_BOUND",
                max_source_bytes=900000000, max_repository_bytes=1000000000)
    if mode == "capture":
        a["cost_sha256"] = c.sha(c.canonical(cost))
    decision = {key: value for key, value in a.items() if key not in
                ("decision_sha256", "recovery_password_sha256")}
    decision.update(source="CURRENT_EXPLICIT_USER_DECISION_RECORDED_BY_MA",
                    no_repository_initialization=True, no_original_database_restore=True,
                    no_business_mutation=True)
    raw = c.canonical(decision)
    a["decision_sha256"] = c.sha(raw)
    return a, raw, c.sha(raw), c.canonical(cost) if mode == "capture" else b"", \
        c.canonical(source) if mode == "capture" else b""


class CurrentStateTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        parent = ROOT / "build/WI-20261026-ATS-008"
        parent.mkdir(exist_ok=True)
        cls.temp = tempfile.TemporaryDirectory(prefix="synthetic-", dir=parent)
        cls.output = Path(cls.temp.name) / "candidate"
        cls.result = builder.prepare(cls.output)
        cls.before_path = list(sys.path)
        sys.path[:0] = [str(cls.output / "stage"), str(cls.output / "backup"),
                       str(cls.output / "backup/lib")]
        cls.stage = importlib.import_module("stage_one")
        cls.c = importlib.import_module("backup_plan")
        cls.adapter = importlib.import_module("adapter")
        cls.entry = importlib.import_module("entry")
        cls.serializer = importlib.import_module("make_payload")
        cls.metadata = importlib.import_module("cost_metadata")

    @classmethod
    def tearDownClass(cls):
        sys.path[:] = cls.before_path
        # Temporary target is resolved inside the dedicated WI directory before removal.
        assert Path(cls.temp.name).resolve().is_relative_to(ROOT / "build/WI-20261026-ATS-008")
        cls.temp.cleanup()

    def test_originals_and_connected_candidate_tree(self):
        self.assertEqual(len(builder.source_bundle(ROOT / builder.BACKUP_SOURCE, builder.BACKUP_PIN)), 19)
        self.assertEqual(len(builder.source_bundle(ROOT / builder.STAGE_SOURCE, builder.STAGE_PIN)), 8)
        self.assertEqual(self.stage.BACKUP.resolve(), self.output / "backup")
        self.assertEqual(self.stage.BACKUP_PIN, self.result["backup_candidate_sha256"])
        self.stage.verify_tree(self.output / "stage", self.result["stage_candidate_sha256"])
        self.entry.verify_candidate(self.result["backup_candidate_sha256"])

    def test_default_entry_and_builder_are_inert(self):
        with patch.object(self.entry, "verify_candidate", side_effect=AssertionError), \
                patch("sys.stdout", new_callable=io.StringIO) as out:
            self.assertEqual(self.entry.main([]), 0)
            self.assertFalse(json.loads(out.getvalue())["allow_native"])
        with patch.object(builder, "prepare", side_effect=AssertionError), \
                patch.object(sys, "argv", ["prepare_successor.py"]), \
                patch("sys.stdout", new_callable=io.StringIO) as out:
            self.assertEqual(builder.main(), 0)
            self.assertEqual(json.loads(out.getvalue())["native_authority"], "NOT_CREATED")

    def test_builder_rejects_existing_or_outside_target(self):
        for path in (self.output, ROOT / "scripts/new-copy"):
            with self.assertRaises(ValueError):
                builder.prepare(path)

    def test_parser_and_hash_match_stage_two_representation(self):
        snap = synthetic_snapshot()
        self.assertEqual(guard.parse_snapshot(protocol(snap), SCHEMA), snap)
        self.assertEqual(guard.validate_snapshot(snap, anchor(snap)), anchor(snap)["table_counts"])
        old = ROOT / "build/WI20261023-005-account-fixture/fixture_core.py"
        tree = ast.parse(old.read_text())
        only = ast.Module(body=[node for node in tree.body if isinstance(node, (ast.Import, ast.ImportFrom))
                     or isinstance(node, ast.FunctionDef) and node.name in ("canonical", "digest", "cell")],
                     type_ignores=[])
        scope = {}
        exec(compile(only, "<retained-pure-hashing>", "exec"), scope)
        self.assertEqual(guard.digest(snap), scope["digest"](snap))
        for value in (None, 1, "9900.00", "CANCELLED"):
            self.assertEqual(guard.cell(value), scope["cell"](value))

    def test_wrong_roles_plans_and_grant_reject_even_rehashed_anchor(self):
        for table, key, column, value in (
            ("users", "[1]", "role", "USER"), ("users", "[2]", "is_verified", 0),
            ("subscriptions", "[6]", "price_yearly", "1.00"),
            ("subscriptions", "[1]", "download_per_day", 20),
            ("user_subscriptions", "[1]", "expires_at", "2026-10-24"),
            ("user_subscriptions", "[1]", "status", "ACTIVE"),
            ("user_subscriptions", "[1]", "pending_subscription_id", 2),
        ):
            snap = synthetic_snapshot()
            snap[table][key][column] = guard.cell(value)
            with self.assertRaisesRegex(ValueError, "CURRENT_STATE"):
                guard.validate_snapshot(snap, anchor(snap))

    def test_equal_count_audit_and_arbitrary_cell_drift_reject(self):
        snap = synthetic_snapshot()
        approved = anchor(snap)
        for table, key, column in (("admin_operation_audit_logs", "[1]", "action"),
                                   ("users", "[3]", "refresh_token")):
            changed = copy.deepcopy(snap)
            changed[table][key][column] = guard.cell("SYNTHETIC_DIFFERENT")
            with self.assertRaisesRegex(ValueError, "APPROVED_STATE_DRIFT"):
                guard.validate_snapshot(changed, approved)

    def test_table_names_extra_actor_and_missing_rows_reject(self):
        snap = synthetic_snapshot()
        for mutation in (lambda s: s.pop("tracks"),
                         lambda s: s["users"].update({"[4]": dict(s["users"]["[3]"])}),
                         lambda s: s["admin_operation_audit_logs"].pop("[1]")):
            altered = copy.deepcopy(snap)
            mutation(altered)
            with self.assertRaises(ValueError):
                guard.validate_snapshot(altered, anchor(snap))

    def test_schema_and_protocol_drift_reject(self):
        raw = protocol(synthetic_snapshot())
        rows = [json.loads(line) for line in raw.splitlines()]
        for mutation in (lambda r: r.pop(0), lambda r: r.insert(1, dict(r[0])),
                         lambda r: r[0].update(column="unapproved"),
                         lambda r: r[-1].update(key=[False]),
                         lambda r: r.append(dict(r[-1]))):
            altered = copy.deepcopy(rows)
            mutation(altered)
            with self.assertRaises(ValueError):
                guard.parse_snapshot(b"\n".join(guard.canonical(row) for row in altered), SCHEMA)
        with self.assertRaises(ValueError):
            guard.parse_snapshot(b"x" * (guard.MAX_RESPONSE + 1), SCHEMA)

    def test_read_only_query_is_schema_explicit_and_complete(self):
        sql = guard.query_bytes(SCHEMA)
        self.assertTrue(sql.startswith(b"USE `atstudio_prod`;"))
        self.assertIn(b"START TRANSACTION WITH CONSISTENT SNAPSHOT, READ ONLY", sql)
        self.assertTrue(sql.endswith(b"ROLLBACK;\n"))
        self.assertEqual(sql.count(b" LIMIT 257;"), 43)
        for keyword in (b"UPDATE ", b"DELETE ", b"INSERT ", b"ALTER ", b"DROP "):
            self.assertNotIn(keyword, sql)
        self.assertIn(b"SHA2(CAST(", sql)

    def test_current_authority_and_exact_anchor_binding(self):
        args = approval(self.c, self.result["backup_candidate_sha256"])
        self.assertEqual(self.c.authority(*args, self.result["backup_candidate_sha256"], "capture")
                         ["additional_paid_upper_bound_minor"], 0)
        for key, value in (("snapshot_sha256", "f" * 64), ("schema_sha256", "f" * 64),
                           ("source_receipt_sha256", "f" * 64)):
            changed = copy.deepcopy(args)
            changed[0]["approved_state_anchor"][key] = value
            with self.assertRaises(ValueError):
                self.c.authority(*changed, self.result["backup_candidate_sha256"], "capture")

    def test_synthetic_origin_stale_wrong_target_and_count_rejected(self):
        args = approval(self.c, self.result["backup_candidate_sha256"])
        for mutation in (
            lambda a: a["approved_state_anchor"].update(evidence_kind="SYNTHETIC"),
            lambda a: a["approved_state_anchor"].update(source_wi="UNRECONCILED"),
            lambda a: a["approved_state_anchor"].update(machine_id_sha256="f" * 64),
            lambda a: a.update(expires_at=0),
            lambda a: a["expected_fixture_counts"].update(admin_operation_audit_logs=4),
        ):
            changed = copy.deepcopy(args[0])
            mutation(changed)
            changed["approved_state_anchor_sha256"] = guard.digest(changed["approved_state_anchor"])
            with self.assertRaises(ValueError):
                guard.validate_anchor(changed, "capture")

    def test_inventory_has_no_state_or_cost_side_effect(self):
        args = approval(self.c, self.result["backup_candidate_sha256"], "inventory")
        self.assertIsNone(self.c.authority(*args, self.result["backup_candidate_sha256"], "inventory"))
        args[0]["approved_state_anchor"] = anchor(synthetic_snapshot())
        with self.assertRaises(ValueError):
            guard.validate_anchor(args[0], "inventory")

    def test_serializer_preserves_pinned_anchor_without_native_execution(self):
        args = approval(self.c, self.result["backup_candidate_sha256"])
        with patch.object(subprocess, "run", side_effect=AssertionError), \
                patch.object(subprocess, "Popen", side_effect=AssertionError):
            raw = self.serializer.payload("capture", self.result["backup_candidate_sha256"],
                    self.c.canonical(args[0]), args[1], args[2], args[3], args[4])
        compile(raw, "<synthetic-payload-not-executed>", "exec")
        nodes = ast.parse(raw).body
        private = ast.literal_eval(nodes[1].value)
        parsed = json.loads(base64.b64decode(private["approval.json"]))
        self.assertEqual(parsed["approved_state_anchor_sha256"], args[0]["approved_state_anchor_sha256"])
        self.assertEqual(base64.b64decode(private["decision.json"]), args[1])
        self.assertIn(b"state.SCHEMA=json.loads(decoded['state-schema.json'])", raw)
        self.assertIn(b"EXPLICIT_NATIVE_DISPATCH_REQUIRED", base64.b64decode(ast.literal_eval(nodes[0].value)["entry.py"]))

    def test_stage_serializer_connects_new_bundle_and_original_cost_fixes(self):
        args = approval(self.c, self.result["backup_candidate_sha256"], "inventory")
        scope = dict(wi="WI-20261026-ATS-008", source="CURRENT_EXPLICIT_USER_DECISION_RECORDED_BY_MA",
                     scope="CURRENT_STATE_CAPTURE_COST_METADATA", action="inventory",
                     candidate_sha256=self.result["stage_candidate_sha256"],
                     backup_candidate_sha256=self.result["backup_candidate_sha256"],
                     approval_sha256=self.c.sha(self.c.canonical(args[0])),
                     issued_at=args[0]["issued_at"], expires_at=args[0]["expires_at"],
                     cost_input_sha256=None, no_business_mutation=True, no_app=True,
                     no_additional_provider_operation=True)
        scope_raw = self.c.canonical(scope)
        with patch.object(subprocess, "run", side_effect=AssertionError), \
                patch.object(subprocess, "Popen", side_effect=AssertionError):
            raw = self.stage.payload("inventory", self.result["stage_candidate_sha256"],
                    self.c.canonical(args[0]), args[1], args[2], scope_raw, self.c.sha(scope_raw))
        compile(raw, "<synthetic-stage-payload-not-executed>", "exec")
        self.assertIn(b"costmeta.install_release_inventory(adapter)", raw)
        self.assertIn(b"costmeta.install_account_query_context(adapter)", raw)
        self.assertIn(b"CURRENT_STATE_CAPTURE_COST_METADATA", raw)
        changed = dict(scope, backup_candidate_sha256=builder.BACKUP_PIN)
        changed_raw = self.c.canonical(changed)
        with self.assertRaises(ValueError):
            self.stage.payload("inventory", self.result["stage_candidate_sha256"],
                self.c.canonical(args[0]), args[1], args[2], changed_raw, self.c.sha(changed_raw))

    def test_native_dispatch_requires_explicit_gate(self):
        for invoke in (False, True):
            args = SimpleNamespace(action="capture", invoke=invoke)
            with patch.object(self.entry.os, "name", "nt"), self.assertRaises(ValueError):
                self.entry.dispatch(args)

    def test_fresh_observer_executes_only_through_injected_seams(self):
        args = approval(self.c, self.result["backup_candidate_sha256"])
        capture = SimpleNamespace(approval=args[0], guard=Mock(), counter=0,
                                  work=Path("/synthetic"), command=Mock(return_value=protocol(synthetic_snapshot())))
        with patch.object(self.adapter.boot, "exclusive") as output, \
                patch.object(subprocess, "run", side_effect=AssertionError), \
                patch.object(subprocess, "Popen", side_effect=AssertionError):
            result = guard.observe(capture)
        self.assertEqual(result["status"], "APPROVED_STATE_MATCH")
        self.assertEqual(capture.guard.call_count, 2)
        self.assertEqual(capture.command.call_args.kwargs["cap"], guard.MAX_RESPONSE)
        self.assertEqual(output.call_count, 2)
        self.assertNotIn("users", json.dumps({k: v for k, v in result.items() if k != "table_counts"}))

    def test_observer_rejects_changed_content_without_writing_proof(self):
        args = approval(self.c, self.result["backup_candidate_sha256"])
        snap = synthetic_snapshot()
        snap["admin_operation_audit_logs"]["[1]"]["action"] = guard.cell("DIFFERENT")
        capture = SimpleNamespace(approval=args[0], guard=Mock(), counter=0, work=Path("/synthetic"),
                                  command=Mock(return_value=protocol(snap)))
        with patch.object(self.adapter.boot, "exclusive") as output, self.assertRaisesRegex(ValueError, "APPROVED_STATE_DRIFT"):
            guard.observe(capture)
        self.assertEqual(output.call_count, 1)

    def test_retained_cleanup_reports_uncertain_snapshot_and_all_checks(self):
        a = self.adapter
        obj = a.ExistingCapture.__new__(a.ExistingCapture)
        obj.work = Path("/synthetic"); obj.stage = "BACKUP"; obj.snapshot_state = "POSSIBLE"
        obj.sql_proof = None; obj.ctx = (object(), None, None, None)
        obj.owned_units = {"synthetic.service"}; obj.seen_units = {"synthetic.service"}
        obj.stop_history = {}; obj.sql_state = "synthetic.service"
        obj.guard = Mock(); obj.stages = Mock(side_effect=ValueError("PRIVATE_SYNTHETIC"))
        obj.stop_mysql = Mock(side_effect=RuntimeError("PRIVATE_SYNTHETIC")); obj.verify_fence = Mock()
        with patch.object(a.cold, "directory"), patch.object(a.boot, "exclusive"), \
                patch.object(a.os.path, "lexists", return_value=False), \
                patch.object(a.guard, "properties", side_effect=ValueError("PRIVATE_SYNTHETIC")):
            result = obj.execute()
        self.assertEqual(result["status"], "HOLD")
        self.assertEqual(result["snapshot_state"], "POSSIBLE")
        self.assertFalse(result["automatic_retry"])
        self.assertEqual(result["cleanup"], dict(STOP="FAILED", PID_ZERO="FAILED", SOCKET_GONE="PASS", COLD_BASELINE="PASS"))
        self.assertNotIn("PRIVATE_SYNTHETIC", json.dumps(result))
        obj.verify_fence.assert_called_once()

    def test_connected_adapter_state_guard_occurs_before_dump(self):
        a = self.adapter
        args = approval(self.c, self.result["backup_candidate_sha256"])
        obj = a.ExistingCapture.__new__(a.ExistingCapture)
        obj.work = Path("/synthetic"); obj.approval = args[0]
        obj.restic = Mock(side_effect=[self.c.canonical(dict(id=self.c.REPOSITORY_ID)),
                                      self.c.canonical([dict(id=a.restore.SNAPSHOT_ID)])])
        obj.start_mysql = Mock(return_value="synthetic.service")
        obj.query = Mock(return_value=b"synthetic")
        obj.sql_capture = Mock(side_effect=AssertionError("DUMP_MUST_NOT_RUN"))
        observed = dict(expected_actor_roles_match=True, existing_work_quiet=True,
                        accounts=[dict(deleted=False, email_verified=True)], grants=[guard.GRANT],
                        plans=[dict(id=1, user_type="INDIVIDUAL", is_active=True, download_per_day=5)])
        with patch.object(a.cold, "directory"), patch.object(a.accounts, "parse", return_value=observed), \
                patch.object(a.current_state, "observe", side_effect=ValueError("APPROVED_STATE_DRIFT")):
            with self.assertRaisesRegex(ValueError, "APPROVED_STATE_DRIFT"):
                obj.stages()
        obj.sql_capture.assert_not_called()
        self.assertEqual(obj.restic.call_count, 2)

    def test_existing_release_and_schema_fixes_remain_wired(self):
        self.assertIn("costmeta.install_release_inventory(adapter)", self.stage.DISPATCH)
        self.assertIn("costmeta.install_account_query_context(adapter)", self.stage.DISPATCH)
        raw_query = self.adapter.accounts.queries
        try:
            self.metadata.install_account_query_context(self.adapter)
            self.assertTrue(self.adapter.accounts.queries("2026-10-26").startswith(b"USE `atstudio_prod`;\n"))
        finally:
            self.adapter.accounts.queries = raw_query
        for name in ("cost_metadata.py", "cost_bound.py"):
            self.assertEqual((self.output / "stage" / name).read_bytes(), (ROOT / builder.STAGE_SOURCE / name).read_bytes())


if __name__ == "__main__":
    unittest.main(verbosity=2)

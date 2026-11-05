"""Local author tests: synthetic executor only, never network or services."""
import contextlib
from datetime import datetime, timezone
import io
import json
from pathlib import Path
import tempfile
from types import SimpleNamespace
import unittest
from unittest.mock import patch
import uuid

import online_scheduled_dispatch as app
from backup_protocol import GateError, digest, encoded
from test_guarded_backup import config_fixture


class DispatchTests(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory()
        self.addCleanup(self.tmp.cleanup)
        self.root = Path(self.tmp.name) / "ledger"
        self.root.mkdir(mode=0o700)
        source = Path(self.tmp.name) / "synthetic-code"
        source.write_bytes(b"synthetic")
        self.pins = {str(source): digest(source.read_bytes())}
        self.config = config_fixture()
        self.config.update(capture_mode=app.online.PROTOCOL, maintenance_mode="live-resume")
        self.config.pop("cold_host", None)
        self.config["online"] = {
            "version": 1, "bridge_dir": "/run/ats-test-mailbox", "app_uid": 1000, "app_gid": 1000,
            "bridge_seconds": 10, "drain_seconds": 5, "schema": "fixture", "tables": ["tracks"],
            "machine_sha256": "d" * 64, "boot_sha256": "e" * 64,
            "mysql_sha256": "a" * 64, "mysqldump_sha256": "b" * 64,
            "mysql_binary": "/usr/bin/mysql", "mysqldump_binary": "/usr/bin/mysqldump",
            "proofs": {k: {"path": "/proofs/" + k, "sha256": "a" * 64}
                       for k in ("target", "repository_acl", "recovery_access", "cost")},
            "module_sha256": {k: "a" * 64 for k in ("backup_protocol.py", "backup_runtime.py", "online_capture.py")}}
        self.now = datetime(2026, 10, 30, 18, 0, 1, tzinfo=timezone.utc).timestamp()
        self.policy = {"version": 1, "enabled": True, "operation": "daily-online-dispatch",
                       "policy_id": "synthetic-01", "timezone": "Asia/Seoul", "start_hhmm": "03:00",
                       "start_grace_seconds": 30, "window_seconds": 120,
                       "not_before": "2026-10-30T00:00:00Z", "expires_at": "2026-11-04T00:00:00Z",
                       "config_subject_sha256": app.online.config_subject(self.config),
                       "pins_sha256": digest(encoded(self.pins)),
                       "budget": {"max_attempts": 3, "per_attempt_bytes": 10**10,
                                  "per_attempt_cost_krw": 1, "existing_bytes": 0, "reserve_bytes": 1,
                                  "capacity_bytes": 4 * 10**10, "cost_cap_krw": 3}}
        self.set_approval(self.now)
        self.calls = 0

    def set_approval(self, now):
        run_id, start, end = app.window(self.policy, now)
        stamp = lambda value: datetime.fromtimestamp(value, timezone.utc).isoformat()
        self.approval = {"operation": "online-capture", "config_sha256": digest(encoded(self.config)),
                         "run_id": run_id, "not_before": stamp(start), "not_after": stamp(end),
                         "release_authorized": True}

    def execute(self):
        self.calls += 1
        return {"status": "COMPLETE", "restore": "NOT_RUN",
                "capture_id": str(uuid.UUID(self.approval["run_id"]))}

    def run_dispatch(self, execute=None, now=None):
        return app.dispatch(self.root, self.config, self.approval, self.policy, self.pins,
                            execute or self.execute, self.now if now is None else now)

    def test_success_has_separate_online_receipt_and_no_restore_claim(self):
        result = self.run_dispatch()
        self.assertEqual(result["status"], "COMPLETE")
        self.assertFalse(result["restore_verified"])
        self.assertFalse((self.root / "dispatch.lock").exists())

    def test_duplicate_does_not_execute(self):
        self.run_dispatch()
        with self.assertRaisesRegex(GateError, "DUPLICATE_ATTEMPT"): self.run_dispatch()
        self.assertEqual(self.calls, 1)

    def test_expired_approval(self):
        with self.assertRaisesRegex(GateError, "ONLINE_WINDOW"): self.run_dispatch(now=self.now + 200)
        self.assertEqual(self.calls, 0)

    def test_no_catchup(self):
        with self.assertRaisesRegex(GateError, "NO_CATCHUP_WINDOW"): self.run_dispatch(now=self.now + 40)

    def test_wrong_run_or_config_cannot_execute(self):
        self.approval["run_id"] = "f" * 32
        with self.assertRaisesRegex(GateError, "EXACT_RUN_WINDOW"): self.run_dispatch()
        self.set_approval(self.now)
        self.config["owner_ref"] = "different"
        with self.assertRaisesRegex(GateError, "EXACT_ONLINE_APPROVAL"): self.run_dispatch()

    def test_budget_storage_and_cost_reject_before_call(self):
        for key in ("capacity_bytes", "cost_cap_krw"):
            with self.subTest(key=key):
                original = self.policy["budget"][key]
                self.policy["budget"][key] = 1 if key == "capacity_bytes" else 0
                with self.assertRaisesRegex(GateError, "CUMULATIVE_"): self.run_dispatch()
                # Independent scenario gets a fresh local fixture, not a native ledger reset.
                self.policy["budget"][key] = original
                (self.root / "identity.json").unlink()
        self.assertEqual(self.calls, 0)

    def test_exception_preserves_claim_lock_and_blocks_retry(self):
        def unknown(): raise TimeoutError("synthetic unknown")
        with self.assertRaises(TimeoutError): self.run_dispatch(unknown)
        self.assertTrue((self.root / "dispatch.lock").exists())
        with self.assertRaisesRegex(GateError, "LOCKED_OR_UNCERTAIN"): self.run_dispatch()
        self.assertEqual(self.calls, 0)

    def test_cold_receipt_is_not_online_success(self):
        with self.assertRaisesRegex(GateError, "ONLINE_RECEIPT_REQUIRED"):
            self.run_dispatch(lambda: {"status": "COMPLETE", "restore": "NOT_RUN", "copy": "NOT_RUN"})
        self.assertTrue((self.root / "dispatch.lock").exists())

    def test_executor_proof_rejection_is_preserved_not_reissued(self):
        before = encoded(self.config)
        def stale(): raise GateError("PROOF_STALE")
        with self.assertRaisesRegex(GateError, "PROOF_STALE"): self.run_dispatch(stale)
        self.assertEqual(before, encoded(self.config))
        self.assertTrue((self.root / "dispatch.lock").exists())

    def test_concurrent_lock_rejects_before_execution(self):
        (self.root / "dispatch.lock").mkdir()
        with self.assertRaisesRegex(GateError, "LOCKED_OR_UNCERTAIN"): self.run_dispatch()
        self.assertEqual(self.calls, 0)

    def test_pin_change_blocks_without_claim(self):
        Path(next(iter(self.pins))).write_bytes(b"changed")
        with self.assertRaisesRegex(GateError, "PIN_MISMATCH"): self.run_dispatch()
        self.assertEqual(list(self.root.iterdir()), [])

    def test_next_day_consumes_finite_budget_and_preserves_prior_result(self):
        first = self.run_dispatch()
        saved = (self.root / ("attempt-" + first["run_id"]) / "result.json").read_bytes()
        for day in (1, 2):
            now = self.now + day * 86400
            self.set_approval(now)
            self.run_dispatch(now=now)
        self.set_approval(self.now + 3 * 86400)
        with self.assertRaisesRegex(GateError, "ATTEMPT_BUDGET_EXHAUSTED"):
            self.run_dispatch(now=self.now + 3 * 86400)
        self.assertEqual(saved, (self.root / ("attempt-" + first["run_id"]) / "result.json").read_bytes())

    def test_policy_change_cannot_reset_ledger(self):
        self.run_dispatch()
        self.policy["budget"]["max_attempts"] += 1
        with self.assertRaisesRegex(GateError, "POLICY_IDENTITY_CHANGED"): self.run_dispatch()

    def test_corrupt_previous_result_blocks_next_day_without_rewriting(self):
        first = self.run_dispatch()
        result_path = self.root / ("attempt-" + first["run_id"]) / "result.json"
        broken = json.loads(result_path.read_bytes())
        broken["capture_id"] = str(uuid.UUID("f" * 32))
        result_path.write_bytes(encoded(broken))
        before = result_path.read_bytes()
        self.set_approval(self.now + 86400)
        with self.assertRaisesRegex(GateError, "UNCERTAIN_ATTEMPT"):
            self.run_dispatch(now=self.now + 86400)
        self.assertEqual(self.calls, 1)
        self.assertEqual(before, result_path.read_bytes())
        self.assertEqual(len(list(self.root.glob("attempt-*"))), 1)

    def test_previous_claim_and_result_require_exact_schema_and_bindings(self):
        first = self.run_dispatch()
        entry = self.root / ("attempt-" + first["run_id"])
        claim = json.loads((entry / "claim.json").read_bytes())
        identity = json.loads((self.root / "identity.json").read_bytes())
        for key in ("config_sha256", "approval_sha256"):
            for value in (None, 123, "g" * 64, "A" * 64, "a" * 63):
                with self.subTest(key=key, value=value), self.assertRaisesRegex(GateError, "CORRUPT_CLAIM"):
                    app.validate_previous(entry, {**claim, key: value}, first, identity)
            missing = dict(claim)
            del missing[key]
            with self.assertRaisesRegex(GateError, "CORRUPT_CLAIM"):
                app.validate_previous(entry, missing, first, identity)
        for broken in ({**claim, "extra": True}, {**claim, "run_id": first["run_id"].upper()},
                       {**claim, "identity": {**identity, "extra": True}}):
            with self.assertRaisesRegex(GateError, "CORRUPT_CLAIM"):
                app.validate_previous(entry, broken, first, identity)
        for broken in ({**first, "capture_id": first["capture_id"].upper()},
                       {**first, "capture_id": first["run_id"]}, {**first, "extra": True},
                       {**first, "restore_verified": 0}):
            with self.assertRaisesRegex(GateError, "UNCERTAIN_ATTEMPT"):
                app.validate_previous(entry, claim, broken, identity)

    def test_default_cli_inert(self):
        with patch.object(app.online, "execute") as execute, contextlib.redirect_stdout(io.StringIO()) as out:
            self.assertEqual(app.main([]), 0)
            execute.assert_not_called()
            self.assertIn("DISABLED", out.getvalue())

    def executor_before_host(self, approval, **kwargs):
        with contextlib.ExitStack() as stack:
            stack.enter_context(patch.object(app.online.sys, "platform", "linux"))
            stack.enter_context(patch.object(app.online.os, "geteuid", return_value=0, create=True))
            stack.enter_context(patch.object(app.online, "checked_path", side_effect=lambda p, **kw: Path(p)))
            stack.enter_context(patch.object(app.online, "read_json", return_value=approval))
            stack.enter_context(patch.object(app.online.time, "time", return_value=self.now))
            host = stack.enter_context(patch.object(app.online.socket, "gethostname",
                                      side_effect=GateError("TEST_STOP_BEFORE_HOST")))
            try:
                app.online.execute(self.config, Path("synthetic-config"), Path("synthetic-approval"), **kwargs)
            except GateError as error:
                return str(error), host.call_count
        self.fail("Expected synthetic stop before native host operations")

    def test_executor_rejects_changed_approval_before_host_access(self):
        expected = digest(encoded(self.approval))
        changed = dict(self.approval)
        changed["not_after"] = datetime.fromtimestamp(self.now + 239, timezone.utc).isoformat()
        self.assertEqual(self.executor_before_host(changed, expected_approval_sha256=expected),
                         ("EXACT_APPROVAL_HANDOFF", 0))

    def test_executor_matching_handoff_and_standalone_remain_compatible(self):
        self.assertEqual(self.executor_before_host(self.approval), ("TEST_STOP_BEFORE_HOST", 1))
        self.assertEqual(self.executor_before_host(self.approval,
                         expected_approval_sha256=digest(encoded(self.approval))),
                         ("TEST_STOP_BEFORE_HOST", 1))

    def test_source_custody_rejects_actual_cache_prefix_and_wrong_origin(self):
        directory = Path(self.tmp.name) / "code"
        directory.mkdir()
        names = ("backup_protocol", "backup_runtime", "scheduled_dispatch", "online_capture",
                 "online_scheduled_dispatch")
        modules = {}
        for name in names:
            source = directory / (name + ".py")
            source.write_text("# synthetic\n")
            modules[name] = SimpleNamespace(__file__=str(source), __cached__=None,
                                           __spec__=SimpleNamespace(origin=str(source)))
        with patch.object(app, "__file__", str(directory / "online_scheduled_dispatch.py")), \
                patch.dict(app.sys.modules, modules):
            app.validate_source_custody()
            prefix = Path(self.tmp.name) / "external-cache.pyc"
            prefix.write_bytes(b"synthetic cache")
            modules["online_capture"].__cached__ = str(prefix)
            with self.assertRaisesRegex(GateError, "MODULE_CACHE_AMBIGUOUS"):
                app.validate_source_custody()
            self.assertTrue(prefix.exists())
            modules["online_capture"].__cached__ = None
            modules["online_capture"].__file__ = str(Path(self.tmp.name) / "other.py")
            with self.assertRaisesRegex(GateError, "MODULE_SOURCE_ORIGIN"):
                app.validate_source_custody()

    def test_source_custody_rejects_legacy_adjacent_cache(self):
        directory = Path(self.tmp.name) / "code"
        directory.mkdir()
        modules = {}
        for name in ("backup_protocol", "backup_runtime", "scheduled_dispatch", "online_capture",
                     "online_scheduled_dispatch"):
            source = directory / (name + ".py")
            source.write_text("# synthetic\n")
            modules[name] = SimpleNamespace(__file__=str(source), __cached__=None, __spec__=None)
        legacy = directory / "online_capture.pyc"
        legacy.write_bytes(b"synthetic legacy")
        with patch.object(app, "__file__", str(directory / "online_scheduled_dispatch.py")), \
                patch.dict(app.sys.modules, modules), self.assertRaisesRegex(GateError, "MODULE_CACHE_AMBIGUOUS"):
            app.validate_source_custody()
        self.assertTrue(legacy.exists())


if __name__ == "__main__":
    unittest.main()

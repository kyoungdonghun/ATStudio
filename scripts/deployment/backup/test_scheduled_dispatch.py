"""WI016 real durable files, fake bounded runner; no native/server execution."""
import json
from pathlib import Path
import tempfile
from types import SimpleNamespace
import unittest
from unittest.mock import patch

from backup_protocol import GateError, digest, encoded, timestamp, validate_config, recurring_approval, recurring_subject
from scheduled_dispatch import dispatch, load
from test_guarded_backup import config_fixture


class DispatchTests(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory()
        self.addCleanup(self.tmp.cleanup)
        self.base = Path(self.tmp.name)
        self.root = self.base / "ledger"
        self.root.mkdir(mode=0o700)
        self.binary = self.base / "fake-command"
        self.binary.write_bytes(b"synthetic pinned executor")
        self.config = config_fixture()
        self.config["limits"].update(cost_limit_krw=0, projected_cost_krw=0,
                                      source_bytes=100, dump_bytes=100, metadata_bytes=100)
        self.now = timestamp("2026-10-26T03:00:15+09:00")
        self.policy = {"enabled": True, "operation": "daily-cold-capture",
                       "config_sha256": digest(encoded(self.config)), "timezone": "Asia/Seoul",
                       "start_hhmm": "03:00", "start_grace_seconds": 30, "window_seconds": 240,
                       "not_before": "2026-10-26T00:00:00+09:00", "expires_at": "2026-10-29T00:00:00+09:00",
                       "resume_authorized": True}
        self.argv = [str(self.binary), "scheduled"]
        self.pins = {str(self.binary): digest(self.binary.read_bytes())}
        self.budget = {"config_sha256": digest(encoded(self.config)), "policy_sha256": digest(encoded(self.policy)),
                       "argv_sha256": digest(encoded(self.argv)), "pins_sha256": digest(encoded(self.pins)),
                       "not_before": self.policy["not_before"], "expires_at": self.policy["expires_at"],
                       "max_attempts": 2, "per_attempt_bytes": 300, "per_attempt_cost_krw": 0,
                       "existing_bytes": 100, "reserve_bytes": 100, "capacity_bytes": 800,
                       "cost_cap_krw": 0, "max_output_bytes": 1024}
        self.calls = 0
        self.runner = SimpleNamespace(run=self.run_fake)

    def run_fake(self, argv, cap):
        self.calls += 1
        self.assertEqual(argv, self.argv)
        self.assertTrue((self.root / "dispatch.lock").is_dir())
        self.assertEqual(len(list(self.root.glob("attempt-*/claim.json"))), self.calls)
        data = encoded({"status": "COMPLETE", "restore": "NOT_RUN", "copy": "NOT_RUN"})
        return SimpleNamespace(code=0, data=data, size=len(data))

    def call(self, delta=0):
        return dispatch(self.root, self.config, self.policy, self.budget, self.argv, self.pins, self.runner, self.now + delta)

    def test_connected_success_and_persistent_duplicate(self):
        self.assertEqual(self.call(), {"status": "COMPLETE"})
        self.assertEqual(load(next(self.root.glob("attempt-*/result.json"))), {"status": "COMPLETE"})
        with self.assertRaisesRegex(GateError, "DUPLICATE_ATTEMPT"):
            self.call()
        self.assertEqual(self.calls, 1)

    def test_exclusive_claim_reentrant_concurrency(self):
        def concurrent(argv, cap):
            with self.assertRaisesRegex(GateError, "LOCKED_OR_UNCERTAIN"):
                self.call()
            return self.run_fake(argv, cap)
        self.runner.run = concurrent
        self.call()
        self.assertEqual(self.calls, 1)

    def test_crash_after_claim_no_automatic_retry(self):
        def crash(*args):
            raise RuntimeError("synthetic crash")
        self.runner.run = crash
        with self.assertRaises(RuntimeError):
            self.call()
        with self.assertRaisesRegex(GateError, "LOCKED_OR_UNCERTAIN"):
            self.call(86400)
        self.assertEqual(len(list(self.root.glob("attempt-*/claim.json"))), 1)

    def test_failed_attempt_consumes_capacity(self):
        self.runner.run = lambda *args: SimpleNamespace(code=2, data=b"", size=0)
        with self.assertRaisesRegex(GateError, "CAPTURE_OUTCOME_UNCERTAIN"):
            self.call()
        with self.assertRaisesRegex(GateError, "LOCKED_OR_UNCERTAIN"):
            self.call(86400)
        self.assertEqual(len(list(self.root.glob("attempt-*/claim.json"))), 1)

    def test_changed_policy_cannot_reset_consumption(self):
        self.call()
        self.policy["start_grace_seconds"] = 29
        self.budget["policy_sha256"] = digest(encoded(self.policy))
        with self.assertRaisesRegex(GateError, "BUDGET_IDENTITY_CHANGED"):
            self.call(86400)

    def test_storage_and_cost_limits(self):
        self.budget["capacity_bytes"] = 499
        with self.assertRaisesRegex(GateError, "CUMULATIVE_STORAGE_EXCEEDED"):
            self.call()
        # New isolated fixture for another policy; never reset a real ledger.
        self.root = self.base / "other-ledger"
        self.root.mkdir(mode=0o700)
        self.budget["capacity_bytes"] = 800
        self.budget["per_attempt_cost_krw"] = 1
        with self.assertRaisesRegex(GateError, "CUMULATIVE_COST_EXCEEDED"):
            self.call()

    def test_expiry_and_no_catchup(self):
        for delta in (60, 3 * 86400):
            with self.assertRaises(GateError):
                self.call(delta)
        self.assertEqual(self.calls, 0)

    def test_command_pin_tamper(self):
        self.binary.write_bytes(b"tampered")
        with self.assertRaisesRegex(GateError, "PIN_MISMATCH"):
            self.call()

    def test_corruption_does_not_reset_ledger(self):
        self.call()
        next(self.root.glob("attempt-*/result.json")).write_text("invalid")
        with self.assertRaisesRegex(GateError, "CORRUPT_STATE"):
            self.call(86400)

    def test_zero_success_requires_capture_receipt(self):
        self.runner.run = lambda *args: SimpleNamespace(code=0, data=b"{}", size=2)
        with self.assertRaisesRegex(GateError, "CAPTURE_RECEIPT_REQUIRED"):
            self.call()
        self.assertTrue((self.root / "dispatch.lock").exists())

    def test_zero_cost_numeric_only(self):
        validate_config(self.config)
        for key in ("cost_limit_krw", "projected_cost_krw"):
            for invalid in (None, -1, False, "0"):
                self.config["limits"][key] = invalid
                with self.assertRaises(GateError):
                    validate_config(self.config)
            self.config["limits"][key] = 0

    def test_symlink_rejected_before_dispatch(self):
        with patch.object(Path, "is_symlink", return_value=True):
            with self.assertRaisesRegex(GateError, "SYMLINK_REJECTED"):
                self.call()
        self.assertEqual(self.calls, 0)

    def test_output_limit_uncertain(self):
        self.runner.run = lambda *args: SimpleNamespace(code=0, data=b"", size=1025)
        with self.assertRaisesRegex(GateError, "INVALID_RUNNER_RESULT"):
            self.call()
        self.assertTrue((self.root / "dispatch.lock").exists())


class RenewedEvidenceTests(unittest.TestCase):
    """Real synthetic ledger files, fake capture; never authorize observed facts."""
    def setUp(self):
        self.f = DispatchTests()
        self.f.setUp()
        self.addCleanup(self.f.doCleanups)
        f = self.f
        f.config.update(maintenance_mode="cold-preserve", cold_host={
            "boot_sha256": "a" * 64, "evidence": {"source_inventory": {"path": "/synthetic/day1", "sha256": "b" * 64}}})
        f.policy.pop("config_sha256")
        f.policy.update(version=2, policy_id="synthetic-policy", code_pins=dict(f.pins),
                        config_subject_sha256=recurring_subject(f.config))
        for key in ("config_sha256", "argv_sha256", "pins_sha256"):
            f.budget.pop(key)
        f.budget.update(policy_id=f.policy["policy_id"], config_subject_sha256=recurring_subject(f.config),
                        policy_sha256=digest(encoded(f.policy)))

    def manifest(self, delta=0):
        f = self.f
        return {"version": 1, "policy_id": f.policy["policy_id"],
                "run_id": recurring_approval(f.config, f.policy, f.now + delta)["run_id"],
                "config_sha256": digest(encoded(f.config)), "policy_sha256": digest(encoded(f.policy)),
                "argv_sha256": digest(encoded(f.argv)), "pins_sha256": digest(encoded(f.pins))}

    def call(self, delta=0, manifest=None):
        f = self.f
        return dispatch(f.root, f.config, f.policy, f.budget, f.argv, f.pins, f.runner,
                        f.now + delta, manifest=self.manifest(delta) if manifest is None else manifest)

    def renew(self):
        self.f.config["cold_host"]["evidence"] = {"source_inventory": {"path": "/synthetic/day2", "sha256": "c" * 64}}

    def test_two_days_renew_exact_config_keep_budget(self):
        first = self.manifest()
        self.call()
        identity = load(self.f.root / "identity.json")
        self.renew()
        second = self.manifest(86400)
        self.assertNotEqual(first["config_sha256"], second["config_sha256"])
        self.call(86400)
        self.assertEqual(load(self.f.root / "identity.json"), identity)
        self.assertEqual(len(list(self.f.root.glob("attempt-*/claim.json"))), 2)
        with self.assertRaisesRegex(GateError, "ATTEMPT_BUDGET_EXHAUSTED"):
            self.call(2 * 86400)

    def test_same_day_renewal_cannot_repeat(self):
        self.call()
        self.renew()
        with self.assertRaisesRegex(GateError, "DUPLICATE_ATTEMPT"):
            self.call()

    def test_old_manifest_and_changed_config_rejected(self):
        old = self.manifest()
        self.renew()
        with self.assertRaisesRegex(GateError, "RUN_MANIFEST_BINDING"):
            self.call(manifest=old)
        with self.assertRaisesRegex(GateError, "RUN_MANIFEST_BINDING"):
            self.call(86400, self.manifest())
        self.f.config["cold_host"]["boot_sha256"] = "d" * 64
        with self.assertRaisesRegex(GateError, "EXACT_RECURRING_APPROVAL"):
            self.call(86400)

    def test_manifest_does_not_relax_subject(self):
        self.f.config["password_file"] = "/different/secret-reference"
        with self.assertRaisesRegex(GateError, "EXACT_RECURRING_APPROVAL"):
            self.call()

    def test_policy_or_budget_edit_cannot_reset_ledger(self):
        self.call()
        self.f.policy["policy_id"] = "changed-policy"
        self.f.budget.update(policy_id="changed-policy", policy_sha256=digest(encoded(self.f.policy)))
        with self.assertRaisesRegex(GateError, "BUDGET_IDENTITY_CHANGED"):
            self.call(86400)

    def test_unknown_outcome_blocks_next_day_even_fresh_evidence(self):
        self.f.runner.run = lambda *args: SimpleNamespace(code=2, data=b"", size=0)
        with self.assertRaisesRegex(GateError, "CAPTURE_OUTCOME_UNCERTAIN"):
            self.call()
        self.renew()
        with self.assertRaisesRegex(GateError, "LOCKED_OR_UNCERTAIN"):
            self.call(86400)
        self.assertEqual(len(list(self.f.root.glob("attempt-*/claim.json"))), 1)

    def test_code_pin_change_rejected_even_rebound_manifest(self):
        self.f.binary.write_bytes(b"changed")
        self.f.pins[str(self.f.binary)] = digest(self.f.binary.read_bytes())
        with self.assertRaisesRegex(GateError, "CODE_AUTHORITY"):
            self.call()

    def test_late_or_expired_never_dispatches(self):
        for delta in (60, 3 * 86400):
            with self.assertRaises(GateError):
                self.call(delta)
        self.assertEqual(self.f.calls, 0)


if __name__ == "__main__":
    unittest.main()

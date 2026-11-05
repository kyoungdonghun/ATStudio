"""Synthetic offline readiness negatives; no native executors imported."""
import copy
import unittest

from backup_protocol import GateError, timestamp
from readiness_plan import REQUIRED_RECOVERY, PROOFS, evaluate

NOW = timestamp("2026-10-26T00:00:00Z")


def fixture():
    return {"budget": {"max_attempts": 1, "existing_bytes": 100,
            "reserve_bytes": 100, "per_attempt_new_bytes": 100,
            "capacity_bytes": 1000, "storage_krw": 0, "requests_krw": 0,
            "retrieval_krw": 0, "transfer_krw": 0, "tax_krw": 0,
            "approved_cost_krw": 0, "cost_evidence_ref": "synthetic-cost",
            "not_before": "2026-10-25T00:00:00Z", "expires_at": "2026-11-25T00:00:00Z"},
            "primary_custody_ref": "primary", "repository_custody_ref": "repository",
            "isolated_recovery_destination_ref": "synthetic-isolation",
            "recovery": {kind: {"artifact_ref": "synthetic", "custody_ref": "escrow"}
                         for kind in REQUIRED_RECOVERY},
            "proofs": {kind: {"receipt_ref": "synthetic", "observed_at": "2026-10-26T00:00:00Z"}
                       for kind in PROOFS},
            "alert": {"monitor_custody_ref": "other-host", "recipient_ref": "existing-ID1-mailbox"}}


class ReadinessTests(unittest.TestCase):
    def test_synthetic_never_authority_and_no_private_references_echoed(self):
        plan = fixture()
        result = evaluate(plan, NOW)
        self.assertEqual(result["status"], "ACTIVATION_HOLD")
        self.assertEqual(result["authority"], "NOT_ISSUED")
        self.assertNotIn("synthetic", str(result))
        self.assertEqual(result["existing_status_protocol"]["external_send"], "NOT_IMPLEMENTED")

    def test_no_mutation(self):
        plan = fixture()
        before = copy.deepcopy(plan)
        evaluate(plan, NOW)
        self.assertEqual(plan, before)

    def test_cumulative_storage_not_single_run(self):
        plan = fixture()
        plan["budget"]["max_attempts"] = 30
        self.assertIn("CUMULATIVE_STORAGE_EXCEEDED", evaluate(plan, NOW)["blockers"])

    def test_unknown_cost_not_zero(self):
        plan = fixture()
        plan["budget"]["transfer_krw"] = None
        with self.assertRaisesRegex(GateError, "FINITE_BUDGET_REQUIRED"):
            evaluate(plan, NOW)

    def test_nonzero_cost_exceeds_zero_authorization(self):
        plan = fixture()
        plan["budget"]["requests_krw"] = 1
        self.assertIn("CUMULATIVE_COST_EXCEEDED", evaluate(plan, NOW)["blockers"])

    def test_expired(self):
        plan = fixture()
        plan["budget"]["expires_at"] = "2026-10-26T00:00:00Z"
        with self.assertRaisesRegex(GateError, "BUDGET_EXPIRED_OR_UNBOUNDED"):
            evaluate(plan, NOW)

    def test_missing_proof(self):
        plan = fixture()
        del plan["proofs"]["alert_ack"]
        self.assertIn("PROOF_MISSING:alert_ack", evaluate(plan, NOW)["blockers"])

    def test_stale_and_future_proof(self):
        for date in ("2026-10-01T00:00:00Z", "2026-10-27T00:00:00Z"):
            plan = fixture()
            plan["proofs"]["full_recovery"]["observed_at"] = date
            self.assertIn("PROOF_STALE_OR_FUTURE:full_recovery", evaluate(plan, NOW)["blockers"])

    def test_secret_colocation(self):
        plan = fixture()
        plan["recovery"]["repository_password"]["custody_ref"] = "repository"
        plan["recovery"]["runtime_config"]["custody_ref"] = "primary"
        blocks = evaluate(plan, NOW)["blockers"]
        self.assertIn("KEY_REPOSITORY_COLOCATION:repository_password", blocks)
        self.assertIn("RECOVERY_PRIMARY_COLOCATION:runtime_config", blocks)

    def test_missing_destination_and_independent_monitor(self):
        plan = fixture()
        del plan["isolated_recovery_destination_ref"]
        plan["alert"]["monitor_custody_ref"] = "primary"
        blocks = evaluate(plan, NOW)["blockers"]
        self.assertIn("RECOVERY_DESTINATION_MISSING", blocks)
        self.assertIn("ALERT_NOT_INDEPENDENT", blocks)

    def test_boolean_is_not_numeric_budget(self):
        plan = fixture()
        plan["budget"]["max_attempts"] = True
        with self.assertRaises(GateError):
            evaluate(plan, NOW)


if __name__ == "__main__":
    unittest.main()

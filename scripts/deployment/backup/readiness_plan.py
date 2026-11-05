"""WI-20261026-ATS-013: offline declarations, never execution authority.

Input is JSON on stdin only with --stdin. No reference is dereferenced.
All evidence supplied here remains unverified, even if labelled observed.
"""
from __future__ import annotations

import argparse
import json
import re
import sys
import time

from backup_protocol import (GateError, recurring_approval, require,
                             status_report, timestamp, validate_config)

REQUIRED_RECOVERY = ("runtime_config", "database_access", "jwt_keys",
                     "billing_keyring", "repository_password", "repository_access")
PROOFS = ("full_recovery", "independent_copy", "alert_delivery", "alert_ack")
REF = re.compile(r"[A-Za-z0-9][A-Za-z0-9_.:-]{0,127}\Z")


def reference(value):
    return isinstance(value, str) and REF.fullmatch(value) is not None


def integer(data, key, positive=False):
    value = data.get(key)
    require(type(value) is int and value >= (1 if positive else 0), "FINITE_BUDGET_REQUIRED")
    return value


def evaluate(plan: dict, now: float) -> dict:
    """Checks plan consistency only. Input evidence does not become observed proof."""
    require(isinstance(plan, dict), "PLAN_OBJECT_REQUIRED")
    blockers = ["LIVE_EVIDENCE_NOT_AUTHENTICATED", "HOST_ADAPTER_NOT_VERIFIED",
                "ALERT_TRANSPORT_NOT_IMPLEMENTED"]
    budget = plan.get("budget", {})
    require(isinstance(budget, dict), "FINITE_BUDGET_REQUIRED")
    runs = integer(budget, "max_attempts", True)
    # Includes failures/retries: no dedup, compression, pruning or retention credit.
    retained = integer(budget, "existing_bytes") + integer(budget, "reserve_bytes")
    bound = retained + runs * integer(budget, "per_attempt_new_bytes", True)
    cap = integer(budget, "capacity_bytes", True)
    if bound > cap:
        blockers.append("CUMULATIVE_STORAGE_EXCEEDED")
    cost_keys = ("storage_krw", "requests_krw", "retrieval_krw", "transfer_krw", "tax_krw")
    # These are horizon totals, not per-run rates; null/unknown is never zero.
    total_cost = sum(integer(budget, key) for key in cost_keys)
    if total_cost > integer(budget, "approved_cost_krw"):
        blockers.append("CUMULATIVE_COST_EXCEEDED")
    start, end = timestamp(budget.get("not_before")), timestamp(budget.get("expires_at"))
    require(start <= now < end and end - start <= 366 * 86400, "BUDGET_EXPIRED_OR_UNBOUNDED")
    if not reference(budget.get("cost_evidence_ref")):
        blockers.append("COST_EVIDENCE_MISSING")

    # Reuse protocol validators, not a second schedule/expiry implementation.
    schedule = plan.get("schedule")
    if not isinstance(schedule, dict):
        blockers.append("SCHEDULE_NOT_CONFIGURED")
    else:
        config, policy = schedule.get("config", {}), schedule.get("policy", {})
        validate_config(config)
        probe = timestamp(schedule.get("planned_start"))
        require(now <= probe < end, "PLANNED_START_OUTSIDE_BUDGET")
        recurring_approval(config, policy, probe)  # Result deliberately discarded.
        policy_end = timestamp(policy.get("expires_at"))
        require(policy_end <= end, "SCHEDULE_OUTLIVES_BUDGET")
        # Conservative daily slots, plus explicit retry allowance.
        daily_slots = int((policy_end - probe + 86399) // 86400)
        if daily_slots + integer(schedule, "retry_allowance") > runs:
            blockers.append("ATTEMPT_BUDGET_INSUFFICIENT")

    primary = plan.get("primary_custody_ref")
    repository = plan.get("repository_custody_ref")
    require(reference(primary) and reference(repository), "CUSTODY_REFERENCE_REQUIRED")
    recovery = plan.get("recovery", {})
    require(isinstance(recovery, dict), "RECOVERY_OBJECT_REQUIRED")
    for kind in REQUIRED_RECOVERY:
        item = recovery.get(kind, {})
        if not isinstance(item, dict) or not reference(item.get("artifact_ref")) or not reference(item.get("custody_ref")):
            blockers.append("RECOVERY_REFERENCE_MISSING:" + kind)
            continue
        if item["custody_ref"] == primary:
            blockers.append("RECOVERY_PRIMARY_COLOCATION:" + kind)
        if kind in {"billing_keyring", "repository_password", "repository_access"} and item["custody_ref"] == repository:
            blockers.append("KEY_REPOSITORY_COLOCATION:" + kind)
    if not reference(plan.get("isolated_recovery_destination_ref")):
        blockers.append("RECOVERY_DESTINATION_MISSING")
    proofs = plan.get("proofs", {})
    require(isinstance(proofs, dict), "PROOFS_OBJECT_REQUIRED")
    for kind in PROOFS:
        proof = proofs.get(kind, {})
        if not isinstance(proof, dict) or not reference(proof.get("receipt_ref")):
            blockers.append("PROOF_MISSING:" + kind)
            continue
        age = now - timestamp(proof.get("observed_at"))
        max_age = 26 * 3600 if kind.startswith("alert") else 8 * 86400
        if not 0 <= age <= max_age:
            blockers.append("PROOF_STALE_OR_FUTURE:" + kind)
    alert = plan.get("alert", {})
    require(isinstance(alert, dict), "ALERT_OBJECT_REQUIRED")
    if not reference(alert.get("monitor_custody_ref")) or alert.get("monitor_custody_ref") == primary:
        blockers.append("ALERT_NOT_INDEPENDENT")
    if alert.get("recipient_ref") != "existing-ID1-mailbox":
        blockers.append("ALERT_RECIPIENT_NOT_BOUND")
    # Existing status protocol is reused; empty state intentionally asserts no live receipt.
    status = status_report({}, now, None, None)
    return {"status": "ACTIVATION_HOLD", "side_effects": False,
            "authority": "NOT_ISSUED", "input_evidence": "UNVERIFIED_DECLARATIONS",
            "cumulative_bytes_bound": bound, "horizon_cost_krw": total_cost,
            "blockers": blockers, "existing_status_protocol": status}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--stdin", action="store_true", help="Read a declaration-only plan; never secrets")
    args = parser.parse_args()
    if not args.stdin:
        result = {"status": "NOT_CONFIGURED", "side_effects": False, "authority": "NOT_ISSUED"}
    else:
        try:
            raw = sys.stdin.buffer.read(131073)
            require(len(raw) <= 131072, "INPUT_TOO_LARGE")
            result = evaluate(json.loads(raw), time.time())
        except GateError as error:
            result = {"status": "ACTIVATION_HOLD", "code": str(error), "authority": "NOT_ISSUED"}
        except (ValueError, TypeError, KeyError, AttributeError, OverflowError, RecursionError):
            result = {"status": "ACTIVATION_HOLD", "code": "INVALID_PLAN", "authority": "NOT_ISSUED"}
    print(json.dumps(result, sort_keys=True))
    return 0 if result["status"] == "NOT_CONFIGURED" else 2


if __name__ == "__main__":
    raise SystemExit(main())

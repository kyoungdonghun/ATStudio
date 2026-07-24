---
version: 1.0
last_updated: 2026-07-25
project: ATS
owner: qa-integ
category: evidence-pack
status: confirmed
dependencies:
  - path: WI-20260724-ATS-026-handoff.md
    reason: Approved Work Item scope and output contract
  - path: WI-20260724-ATS-015-evidence-pack.md
    reason: Blocked payment-rehearsal baseline and resume contract
  - path: WI-20260724-ATS-024-evidence-pack.md
    reason: Corrected disposable acceptance-runtime baseline
---

# Evidence Pack: WI-20260724-ATS-026

## Summary

- **Verdict: PASS with cleanup pending**
- Resumed the Toss test-only recurring-payment rehearsal through an owned
  temporary HTTPS origin and completed the approved payment, plan-change,
  cancellation, reactivation, refund, correction, receipt, audit, and
  reconciliation matrix.
- Restored the final user entitlement to `STANDARD`, `MONTHLY`, `ACTIVE`, with
  the Billing Agreement `ACTIVE` and the next billing date set to 2026-08-24.
- Runtime, tunnel, and disposable-database cleanup remains owned by
  `WI-20260724-ATS-017`; this Work Item is not a production-release verdict.
- No product code or configuration was changed by this Work Item.

## Scope / DoD Check

- [x] The Toss client and secret keys classified as test-only without value
  disclosure or persistence.
- [x] The owned temporary HTTPS origin exposed only the isolated frontend.
- [x] The backend and MySQL were not directly exposed.
- [x] Backend callbacks and CORS used the temporary HTTPS origin.
- [x] Local frontend/API and public frontend/API readiness returned `200`.
- [x] A new Billing Auth and first `STANDARD` monthly recurring charge
  completed.
- [x] Existing incomplete orders reached `EXPIRED` through the supported
  expiration path, without direct database deletion.
- [x] `STANDARD` to `DELUXE` and `DELUXE` to `PREMIUM` immediate upgrades
  completed.
- [x] Cancellation to the grace period and reactivation completed.
- [x] Only the two upgrade payments created in this Work Item were fully
  refunded.
- [x] One Entitlement Correction restored the intended final state.
- [x] Receipt, audit, reconciliation, and local/Provider parity checks passed.
- [x] Bounded readiness and reconciliation operations completed without
  unbounded retry.
- [x] Restricted-value log scans returned zero matches.
- [x] Documentation validation and diff checks passed.
- [ ] Human typed-prompt UI acceptance remains required.
- [ ] Owned runtime, tunnel, and disposable-database cleanup remains assigned
  to `WI-20260724-ATS-017`.

## Reference Documents

| Tier | Document | Reason |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Approval, isolation, and traceability |
| 0 | `docs/standards/development-standards.md` | Verification and evidence standards |
| 0 | `docs/standards/documentation-standards.md` | Deliverable structure and safe links |
| 0 | `docs/standards/glossary.md` | Canonical Work Item and payment terminology |
| 1 | `docs/policies/security-policy.md` | Secret and Provider-identifier handling |
| 1 | `docs/policies/quality-gates.md` | Acceptance and independent quality gates |
| 1 | `docs/policies/access-control-policy.md` | ADMIN execution and separation of duties |
| 2 | `docs/standards/evidence-pack-standard.md` | Evidence and rollback format |
| 2 | `docs/payment/acceptance-test-checklist.md` | Payment acceptance matrix |
| 2 | `docs/payment/user-flows.md` | Subscription and plan-change flows |
| 2 | `docs/payment/admin-operations-guide.md` | Refund and correction operations |
| 2 | `docs/design/payment-operations-runbook.md` | Reconciliation and recovery procedure |
| 2 | `docs/SR/SR-42.md` | Temporary external-access operating boundary |
| 2 | `docs/SR/SR-93.md` | Payment production-readiness boundary |
| Context | `deliverables/user/REQ-20260724-ATS-002.md` | Approved release rehearsal |
| Context | `deliverables/agent/WI-20260724-ATS-015-evidence-pack.md` | Original blocked attempt and resume contract |
| Context | `deliverables/agent/WI-20260724-ATS-024-evidence-pack.md` | Corrected acceptance-runtime baseline |

Injection order followed the required Tier 0, Tier 1, Tier 2, and Work Item
context sequence. The assigned role was `qa-integ` and the task type was
integration testing.

## Evidence Pointers

### Repository State

| Check | Result |
|---|---|
| Branch | `codex/v1-release-rehearsal-fixes` |
| Local HEAD | `5d08598` |
| Remote-tracking HEAD | `5d08598` |
| Client branch changes | 0 |
| Product code/configuration changes | 0 |

The owned runtime evidence remains outside the repository under the
release-rehearsal workspace and is reserved for `WI-20260724-ATS-017`
cleanup. This pack intentionally omits the exact tunnel URL, opaque tunnel
identifier, process identifiers, account identifiers, payment order
identifiers, and Provider references.

### Safe Evidence Classes

| Evidence class | Retained result |
|---|---|
| Key gate | Test/live/unknown classification only |
| Tunnel ownership | Frontend-only exposure and cleanup ownership |
| Readiness | Local/public frontend and API status |
| Payment matrix | Purpose, plan/cycle, transition, and terminal status |
| Ledger summary | Aggregate receipt, refund, audit, and correction counts |
| Reconciliation | Aggregate checked/skipped/mismatch/issue counts |
| Log scan | Restricted marker labels and zero-match counts |
| UI result | Final plan, cycle, entitlement, and Billing Agreement state |

No raw password, bearer token, key, card value, exact Provider identifier, or
exact payment order identifier is retained in these documents.

## Runtime and Boundary Verification

| Boundary | Result |
|---|---|
| Public exposure | Owned temporary HTTPS origin |
| Exposed service | Frontend only |
| API path | Frontend `/api` proxy |
| Backend direct exposure | No |
| MySQL direct exposure | No |
| Local frontend | `200` |
| Local API | `200` |
| Public frontend | `200` |
| Public API | `200` |
| Live keys | Not used |
| Real money | Not used |
| Production database | Not used |

The HTTPS origin was temporary and owned by this rehearsal. Its exact URL and
opaque tunnel identifier are deliberately omitted.

## Secret-Safe Key Gate

| Check | Result |
|---|---|
| Toss client key classification | Test |
| Toss secret key classification | Test |
| Gate | `TEST_ONLY` |
| Unknown/live key behavior | Fail closed |
| Raw value emitted or persisted | No |

## Payment and Subscription Matrix

| Step | Expected behavior | Result |
|---|---|---|
| Previous incomplete orders | Supported expiration | `EXPIRED` |
| New Billing Auth | Register recurring-payment method | PASS |
| Initial charge | `STANDARD` / `MONTHLY` recurring charge | PASS |
| Billing Agreement | Active masked-card record | `ACTIVE` |
| First upgrade | `STANDARD` to `DELUXE`, immediate | PASS |
| Second upgrade | `DELUXE` to `PREMIUM`, immediate | PASS |
| Cancellation | Enter grace period | PASS |
| Reactivation | Restore recurring renewal | PASS |
| Upgrade refund 1 | Full refund of rehearsal upgrade payment | `SUCCEEDED` |
| Upgrade refund 2 | Full refund of rehearsal upgrade payment | `SUCCEEDED` |
| Entitlement Correction | Restore intended subscription state | `SUCCEEDED` |

The two refunds apply only to the two upgrade payments created during this
Work Item. The initial recurring subscription payment was not refunded.
Existing incomplete orders were not removed through direct database mutation.

### Final User State

| Field | Final value |
|---|---|
| Plan | `STANDARD` |
| Billing Cycle | `MONTHLY` |
| Subscription status | `ACTIVE` |
| Next billing date | 2026-08-24 |
| Billing Agreement | `ACTIVE` |
| Payment method evidence | Masked card only |

The final user-facing UI displayed the same restored state.

## Ledger and Audit Evidence

| Ledger | Result |
|---|---:|
| Payment receipts with `ISSUED` status | 3 |
| Refunds with `SUCCEEDED` status | 2 |
| Payment operation audit logs | 15 |
| Entitlement Corrections with `SUCCEEDED` status | 1 |
| Reconciliation Incidents | 0 |

Observed audit action classes:

- Refund requested
- Refund approved
- Refund processing
- Refund succeeded
- Entitlement Correction requested
- Entitlement Correction approved
- Entitlement Correction processing
- Entitlement Correction succeeded
- Receipt evidence created

Exact entity identifiers and Provider references are excluded.

## Reconciliation Evidence

### Local Reconciliation

| Metric | Result |
|---|---:|
| Orders checked | 3 |
| Billing Agreements checked | 1 |
| Mismatches | 0 |
| Issues | 0 |

### Provider Reconciliation

| Metric | Result |
|---|---:|
| Eligible payments checked | 1 |
| Fully refunded payments skipped | 2 |
| Lookup failures | 0 |
| Provider not found | 0 |
| Finalization mismatches | 0 |
| Amount mismatches | 0 |
| Issues | 0 |

The two skipped records are completed full refunds and are legitimate
reconciliation skips, not missing coverage.

## ADMIN Execution and UI Boundary

Browser automation verified the ADMIN request and approval layers. The native
typed confirmation prompt was automatically cancelled by the automation
surface, so it could not complete that final UI interaction. The execute
endpoints were invoked through the authenticated local ADMIN API.

This limitation is classified as an automation-surface limitation, not a
product failure. Human acceptance must still verify:

1. The operator can enter the required typed confirmation.
2. The UI sends the execute request only after the confirmation matches.
3. The UI renders the resulting refund or Entitlement Correction state.

No claim is made that the human typed-prompt gate has passed.

## Restricted-Value and Provider Scan

| Marker | Matches |
|---|---:|
| Bearer token | 0 |
| Auth Key | 0 |
| Billing Key | 0 |
| Customer Key | 0 |
| Raw card value | 0 |
| Secret labels | 0 |

Exact Provider transaction identifiers, refund references, payment order
identifiers, tunnel details, process identifiers, account identifiers, and QA
credentials do not appear in this pack.

## Commands and Results

The executed acceptance sequence used bounded local/public readiness probes,
the supported application payment APIs, authenticated ADMIN operation APIs,
supported order expiration, and both local and Provider reconciliation.

Final documentation gates:

```powershell
python .agents/skills/validate-docs/scripts/validate_docs.py
git diff --check
```

- Documentation validation: PASS.
- Diff check: PASS.
- Raw restricted values added to the two WI-026 deliverables: 0.

## Risks / Rollback

### Residual Risks

- The human typed-prompt interaction remains an explicit acceptance gate.
- External live-key behavior, real-money settlement, and production database
  behavior were intentionally excluded.
- External SMTP authentication and real-inbox delivery remain the separate
  operations gate from `WI-20260724-ATS-025`.
- The owned runtime, temporary tunnel, and disposable database remain active
  until `WI-20260724-ATS-017` performs guarded cleanup.

### Rollback and Cleanup

No product-code rollback is required because this Work Item changed no product
code or configuration.

`WI-20260724-ATS-017` must:

1. Re-read the latest runtime and tunnel ownership evidence.
2. Stop only the owned frontend, backend, and tunnel process trees.
3. Remove only the regex-guarded disposable database.
4. Remove restricted and temporary rehearsal artifacts according to the
   approved cleanup contract.
5. Re-run the final repository and protected-database safety checks.

## Follow-ups

- Execute `WI-20260724-ATS-017` for final audit and guarded cleanup.
- Complete the human typed-prompt acceptance gate.
- Complete the external SMTP/real-inbox operations gate before production
  release.

## Related Documents

- [WI-026 Handoff](WI-20260724-ATS-026-handoff.md)
- [WI-015 Evidence Pack](WI-20260724-ATS-015-evidence-pack.md)
- [WI-024 Evidence Pack](WI-20260724-ATS-024-evidence-pack.md)
- [WI-025 Evidence Pack](WI-20260724-ATS-025-evidence-pack.md)
- [WI-026 User Summary](../user/WI-20260724-ATS-026-summary.md)

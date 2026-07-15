# Evidence Pack: WI-20260714-ATS-036

## Summary (one-liner)

- Completed a code-addressable remediation design for WI-023 findings F-01 through F-05, including executable migration ordering, disjoint implementation packages A-G, verification contracts, rollback, and residual-risk boundaries.

## Review Decision

- Decision: **DESIGN COMPLETE / IMPLEMENTATION STILL BLOCKED**
- The design closes the architecture and implementation-planning gaps for F-01 through F-05.
- This WI does not claim that any finding is closed in production code.
- Packages A-G, independent verification, and the required MySQL proof must complete before downstream payment approval is unblocked.

## Scope / DoD Check

- [x] One logical renewal period retains one order and command while deterministic retry scheduling remains separate and bounded.
- [x] Cancellation, withdrawal cleanup, charged upgrade, and scheduled reconciliation use short local phases with provider calls under `Propagation.NEVER`.
- [x] Refund `PROCESSING` recovery has a 15-minute lease, stale-claim fencing, and exact same-key lookup/replay rules.
- [x] Provider `DONE` recovery has strict evidence gates and purpose-specific finalize-only dispatch for `SUBSCRIBE`, `UPGRADE`, and `RENEWAL`.
- [x] One canonical lock order and required disposable MySQL 8/InnoDB races are specified.
- [x] Every F-01 through F-05 maps to exact design sections, implementation packages, tests, and a closure criterion.
- [x] The existing-DB patch order is executable: preflight/abort, columns, repair/backfill, indexes, then post-validation.
- [x] Package B owns the `BillingAgreementRepository` cleanup/stale contract; package C depends on B and consumes it without repository edits.
- [x] Rollout, rollback, and residual risks are explicit.
- [x] No product code, provider call, schema execution, or retained database mutation was performed.

## Reference Documents (Tier 0-2)

**Injected Context** (from the WI handoff packet):

| Tier | Document | Reason |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Platform-integrity and financial traceability constitution |
| 0 | `docs/standards/development-standards.md` | Transaction, MySQL test, evidence, and traceability standards |
| 0 | `docs/standards/documentation-standards.md` | Documentation structure and dependency rules |
| 0 | `docs/standards/glossary.md` | Canonical WI, SA, and architecture terminology |
| 1 | `docs/policies/security-policy.md` | Secret and provider-evidence handling boundary |
| 1 | `docs/policies/quality-gates.md` | High-criticality validation and rollback expectations |
| Context | `deliverables/user/REQ-20260714-ATS-001.md` | Approved P1 scope and constraints |
| Context | `docs/design/p1-payment-db-integrity-design.md` | Existing command, transaction, DDL, and lock baseline |
| Evidence | `deliverables/agent/WI-20260714-ATS-007-evidence-pack.md` | Renewal implementation baseline |
| Evidence | `deliverables/agent/WI-20260714-ATS-008-evidence-pack.md` | Refund reservation-lock baseline |
| Evidence | `deliverables/agent/WI-20260714-ATS-018-evidence-pack.md` | Provider-boundary and concurrency-test baseline |
| Evidence | `deliverables/agent/WI-20260714-ATS-023-evidence-pack.md` | Source findings F-01 through F-05 |
| Evidence | `deliverables/agent/WI-20260714-ATS-035-evidence-pack.md` | Disposable-MySQL and manual-patch precedent |

**Injection Rules Applied**:

- Handoff: `deliverables/agent/WI-20260714-ATS-036-handoff.md`
- Assignee: `sa`
- Task type: payment-integrity design
- Required context: explicit Tier 0, Tier 1, predecessor design, implementation evidence, and independent-review evidence from the handoff packet

## Finding-to-Contract Closure

| Finding | Design evidence | Implementation package | Required closure proof |
|---|---|---|---|
| F-01 | `docs/design/p1-payment-integrity-remediation-design.md:65-178` | B, founded on A | Two-day retry retains one order/command/period, increments the attempt, uses a distinct persisted attempt key, and remains inside grace |
| F-02 | `docs/design/p1-payment-integrity-remediation-design.md:79-87`, `180-273` | C for cancellation/withdrawal, D for charged upgrade, F for reconciliation | Provider fakes observe no active or suspended transaction; claim and result states survive injected failures |
| F-03 | `docs/design/p1-payment-integrity-remediation-design.md:275-345` | E, founded on A | Crash, stale reclaim, same-key replay/lookup, delayed-result fencing, and concurrent-reclaimer cases converge without a replacement refund/key |
| F-04 | `docs/design/p1-payment-integrity-remediation-design.md:347-417` | F, dependent on B | Exact provider evidence persists `PROVIDER_SUCCEEDED`, dispatches one purpose finalizer, and leaves mismatches Incident-only |
| F-05 | `docs/design/p1-payment-integrity-remediation-design.md:419-460`, `554-586` | B-F plus independent G | Disposable MySQL/InnoDB races produce exact business losers with no accepted deadlock, timeout, or wildcard exception |

## Evidence Pointers

- Design decisions and non-goals: `docs/design/p1-payment-integrity-remediation-design.md:23-51`
- Finding closure matrix: `docs/design/p1-payment-integrity-remediation-design.md:53-61`
- Renewal identity and retry selection: `docs/design/p1-payment-integrity-remediation-design.md:65-178`
- Claim/provider/result transaction phases: `docs/design/p1-payment-integrity-remediation-design.md:180-273`
- Refund lease and crash recovery: `docs/design/p1-payment-integrity-remediation-design.md:275-345`
- Provider-DONE evidence gate and purpose dispatch: `docs/design/p1-payment-integrity-remediation-design.md:347-417`
- Canonical lock order and current-code corrections: `docs/design/p1-payment-integrity-remediation-design.md:419-460`
- Additive schema and executable existing-DB patch order: `docs/design/p1-payment-integrity-remediation-design.md:462-533`
- Focused behavior and MySQL proof contract: `docs/design/p1-payment-integrity-remediation-design.md:535-586`
- Exact implementation impact, including B-owned/C-consumed repository contract: `docs/design/p1-payment-integrity-remediation-design.md:588-614`
- Disjoint implementation packages A-G and dependency chain: `docs/design/p1-payment-integrity-remediation-design.md:615-636`
- Rollout, rollback, and residual risks: `docs/design/p1-payment-integrity-remediation-design.md:638-675`

## Implementation Packages A-G

| Package | Ownership and dependency | Completion boundary |
|---|---|---|
| A | Entity/schema/manual-patch foundation | Additive static contract and copied-DB preflight rehearsal; no retained DB apply |
| B | Payment command core and payment/agreement/subscription repositories; depends on A | F-01, canonical command locks, reconciliation-safe finalizers, and the cleanup/stale repository projections consumed by C |
| C | Cancellation and withdrawal cleanup services; depends on A and B | F-02 cancellation/withdrawal boundaries while consuming B's repository contract without editing it |
| D | Charged-upgrade orchestration; depends on B | F-02 upgrade boundary and finalize-only failure behavior |
| E | Refund lease recovery and refund repository; depends on A | F-03 crash recovery and stale-result fencing |
| F | Reconciliation/finalize-only orchestration and provider lookup evidence; depends on B | Scheduled F-02 plus F-04 evidence and purpose-finalization behavior |
| G | MySQL-only independent proof; depends on B through F | F-05 production-engine concurrency evidence; no production-code ownership |

## Commands & Outputs

- Inspected the WI handoff, Tier 0 standards, source review, predecessor design/evidence, and the complete remediation design with line-numbered PowerShell reads and focused `rg -n` searches.
- `git diff -- docs/design/p1-payment-integrity-remediation-design.md`
  - Result: only the two approved design corrections were present before completion-document creation.
- `git diff --check -- docs/design/p1-payment-integrity-remediation-design.md`
  - Result: PASS for the tracked design change.
- `git diff --no-index --check -- NUL <new-completion-document>` for each new WI-036 completion document
  - Result: PASS with no whitespace diagnostics; exit code `1` is the expected no-index content-difference result.

## Test Evidence

- Product tests: not run; this WI changes design and completion documents only.
- Static design verification: PASS.
  - F-01 through F-05 each have a contract, implementation package, test requirement, and closure criterion.
  - Existing-DB steps no longer reference new columns before column creation.
  - Package C's dependency and repository-consumption contract agree between Exact Implementation Impact and the A-G table.
- Implementation tests remain mandatory under Sections 10 and 12 of the design and are not satisfied by this design review.

## Risks / Rollback

Risks:

- F-01 through F-05 remain open in product code until packages A-G and independent review complete.
- Same-key refund replay is allowed only inside a verified provider idempotency-retention contract; otherwise recovery remains lookup-only and Incident-backed.
- Billing-key deletion has no money-command idempotency key, so ambiguous stale cleanup remains detect-only.
- The orchestration contract assumes one server; multi-server ownership requires a separate design.
- Applicability to a retained database is unknown until an approved copied-DB inventory and rehearsal complete.

Rollback:

- This WI applied no DDL and changed no product state; the design and its two completion documents can be reverted independently.
- After implementation begins, application behavior must be paused and rolled back before any code downgrade; additive columns, indexes, ENUM members, command keys, ledgers, and audit/Incident rows must remain.
- Never drop or contract MySQL schema during an incident; a failed copied-DB rehearsal is discarded or restored from its approved copy.

## Follow-ups

1. Allocate concrete WI IDs for packages A-G through the required handoff skill.
2. Execute A before B/C/E, B before C/D/F, and B-F before G.
3. Run the focused fake-provider transaction/crash tests and the independent disposable MySQL 8/InnoDB suite.
4. Require independent payment/integration review before unblocking downstream WI-025, WI-026, WI-028, or WI-034.

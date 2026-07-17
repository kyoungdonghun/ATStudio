# Evidence Pack: WI-20260717-ATS-017

## Summary (one-liner)
- Closed the active V1 current-state documentation against the official branch, exact database manifest, final quality metrics, and removed SQL/API/configuration paths while preserving genuine production gates in SR-93.

## Scope / DoD Check
- DoD items:
  - [x] Removed active operator instructions for deleted manual SQL files.
  - [x] Replaced legacy direct/one-time subscription API descriptions with the current absence contract.
  - [x] Removed active claims that `codex/client-demo-stable` still exists.
  - [x] Established `codex/p1-acceptance-hardening` as the official V1 baseline branch rather than a candidate.
  - [x] Updated the Standards count to 13 and the total Markdown count to 194.
  - [x] Recorded the exact 39-table/449-column/153-index/80-FK database manifest and supplied final backend/frontend quality metrics.
  - [x] Preserved SR-92 as rejected/retired and SR-93 as OPEN only for genuine production gates.
  - [x] Corrected the review-discovered `app.payment.provider` misstatement: the property, environment alias, and legacy one-time provider path are absent from V1.
  - [x] Passed documentation validation, active stale-reference searches, and `git diff --check`.

## Reference Documents (Tier 0-2)

**Injected Context** (from WI Handoff Packet):

| Tier | Document | Reason |
|------|----------|--------|
| 0 | `docs/standards/core-principles.md` | Constitution and language/traceability rules |
| 0 | `docs/standards/documentation-standards.md` | Current-state documentation, index, and historical-record rules |
| 0 | `docs/standards/glossary.md` | Canonical terminology |
| 1 | `docs/policies/versioning-policy.md` | Stable/deprecated/archived lifecycle rules |
| 1 | `docs/policies/quality-gates.md` | Required documentation and verification gates |
| 1 | `docs/policies/execution-policy.md` | Approved WI execution boundary |
| 2 | `deliverables/user/REQ-20260716-ATS-004.md` | Approved V1 consolidation requirement |
| 2 | `deliverables/agent/WI-20260717-ATS-015-evidence-pack.md` | Cross-layer documentation drift and baseline findings |
| 2 | `deliverables/agent/WI-20260717-ATS-016-evidence-pack.md` | Exact recreated database manifest |
| 2 | `docs/design/api-spec.md` | Current 137-mapping API and removed-contract source |
| 2 | `docs/design/db-schema.md` | Current 39-table/entity and fresh-only schema source |
| 2 | `docs/payment/known-limits-and-next-steps.md` | Current payment delivery and production boundary |

**Injection Rules Applied**:
- Rule source: `.claude/config/context-injection-rules.json`
- Assignee: `docops`
- Task type: documentation closeout
- agent_required_tiers: `[0, 1]`

## Evidence Pointers
- Handoff:
  - `deliverables/agent/WI-20260717-ATS-017-handoff.md`
- Current-state documentation changed:
  - `docs/index.md` - synchronized Standards/total counts and official baseline wording.
  - `docs/SR/SR-42.md` - removed the deleted client-demo branch claim.
  - `docs/SR/SR-92.md` - retained rejected/retired status and documented legacy path absence.
  - `docs/SR/SR-93.md` - published the local V1 closeout, absence contracts, exact metrics, and remaining production gates.
  - `docs/design/db-schema.md` - recorded the exact verified manifest.
  - `docs/design/payment-operations-runbook.md` - removed legacy key/endpoint assumptions and retained the separate-migration boundary.
  - `docs/client/_internal-feature-map.md` - updated final quality metrics and branch boundary.
  - `docs/client/testing-guide.md` - updated the acceptance source branch and removed client-demo usage.
  - `docs/payment/index.md` - updated payment closure and production-open boundary.
  - `docs/payment/feature-inventory.md` - recorded exact DB and quality verification results.
  - `docs/payment/acceptance-test-checklist.md` - made the official baseline branch the only remote acceptance source.
  - `docs/payment/known-limits-and-next-steps.md` - separated closed local gates from open production gates.
  - `docs/registry/project-registry.md` - registered the official branch and exact DB manifest.
- Closeout artifacts created:
  - `deliverables/agent/WI-20260717-ATS-017-evidence-pack.md`
  - `deliverables/user/WI-20260717-ATS-017-summary.md`
- Key locations:
  - `docs/index.md:22,34,73` - 13 Standards, 194 total Markdown files, official V1 baseline branch.
  - `docs/SR/SR-92.md:3,9` - rejected/retired status and removed-path contract.
  - `docs/SR/SR-93.md:24-46` - V1 baseline closeout, final metrics, and remaining production gates.
  - `docs/SR/SR-93.md:116` - corrected `app.payment.provider` absence statement.
  - `docs/SR/SR-93.md:165-172` - fresh-only DB procedure and deleted manual SQL prohibition.
  - `docs/design/db-schema.md:45-58` - exact manifest counts and SHA-256.
  - `docs/client/_internal-feature-map.md:33-41` - final backend/frontend quality baseline.
  - `docs/payment/feature-inventory.md:149-154` - exact DB, test, coverage, tooling, and branch evidence.
  - `docs/registry/project-registry.md:29-41` - official branch and active inventory.

## Commands & Outputs
- Documentation validation:
  - Command: `python .agents/skills/validate-docs/scripts/validate_docs.py`
  - Output: PASS; all Tier 0 documents exist, no broken internal links, 443 traceability IDs matched, and all documents are indexed.
- Active stale-reference search:
  - Command: `rg -n --glob '*.md' --glob '!docs/design/p1-payment-db-integrity-design.md' --glob '!docs/design/remaining-remediation-design-20260716.md' "branch candidate|codex/client-demo-stable|59\.05|34\.49|34\.00|27\.82|35\.43|db/manual|SUBSCRIPTION_CHECKOUT_REQUIRED|/api/payments/subscriptions/prepare|/api/payments/confirm|/subscriptions/payment/|Legacy endpoint removal|PAYMENT_BILLING_KEY_ENCRYPTION_SECRET" docs`
  - Output: 0 active current-state matches. Archived design references were intentionally preserved as historical evidence.
- Provider-setting review search:
  - Command: `rg -n --hidden --glob '!frontend/node_modules/**' --glob '!frontend/dist/**' --glob '!build/**' "app\.payment\.provider|APP_PAYMENT_PROVIDER|legacy/non-subscription one-time provider setting" docs src frontend`
  - Output: only `docs/SR/SR-93.md:116`, which explicitly documents absence, and `V1BackendBaselineContractTest.java:134`, which asserts `APP_PAYMENT_PROVIDER` is forbidden.
  - Runtime-only follow-up over `src/main` and `frontend`: 0 matches.
- Document count comparison:
  - Output: all 14 category counts matched the filesystem; Standards 13 and total 194.
- Diff validation:
  - Command: `git diff --check`
  - Output: PASS with exit code 0; only Windows LF-to-CRLF normalization notices were emitted.

## Tests
- Documentation validation: PASS.
- Active stale-reference and runtime negative searches: PASS.
- `git diff --check`: PASS.
- Backend/frontend suites were not rerun by this documentation WI. The final verified results supplied by MA and recorded in current-state docs are:
  - Backend: 1,208 tests, 0 failures/errors, 9 environment-dependent skips.
  - JaCoCo: instruction 85.673%, branch 71.682%, line 85.726%, method 82.931%.
  - Frontend: 468 tests, 0 failures.
  - Frontend coverage: statements 86.73%, branches 76.98%, functions 85.41%, lines 88.75%.
  - Typecheck, ESLint, Prettier, and build: PASS.

## Risks / Rollback
- Risks:
  - This documentation closeout does not prove retained-data migration, live Toss behavior, production deployment, monitoring, client acceptance, or final release approval.
  - Historical REQ/WI and archived design records intentionally retain time-bound references and must not be interpreted as current operator instructions.
  - Other workers' existing source, test, evidence, runtime-output, and untracked changes remain outside WI-017 ownership.
- Rollback:
  - Apply an inverse patch or revert the eventual WI-017 documentation commit only for the 13 current-state documents listed above.
  - Remove only the two newly created WI-017 closeout artifacts if the entire WI is rolled back.
  - Preserve historical REQ/WI evidence and archived design records.
  - Do not revert source code, tests, database state, local configuration, generated PDFs, Git refs/index, or unrelated working-tree changes.

## Follow-ups
- Remaining production gates:
  - Confirm the production data strategy: verified-empty V1 initialization, or a separately approved retained-data migration and rehearsal.
  - Configure production secrets and validate live Toss charge, renewal, reconciliation, refund, and billing-key cleanup behavior.
  - Verify production HTTPS/proxy/CORS, secret management, backup/restore, scheduler ownership, logging, alerts, and incident response.
  - Complete client acceptance in a newly verified operator-controlled environment.
  - Record explicit production and final release approval before closing SR-93.
- Next blocked activity after WI-017 closeout: unified-branch acceptance testing.

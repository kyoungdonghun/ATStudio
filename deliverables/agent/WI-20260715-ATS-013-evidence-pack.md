# Evidence Pack: WI-20260715-ATS-013

## Summary

- Aligned current payment-integrity, SR, operations, acceptance, and client documentation with the independently reviewed repository state while preserving historical FAIL evidence and keeping production readiness open.

## Scope / DoD Check

- [x] Created a dated closure report mapping F-01 through F-05 and the WI-009/WI-010 follow-ups to WIs, commits, tests, and disposable MySQL proof.
- [x] Marked the remediation design implemented/current and mapped Packages A-G plus corrections to WI-001 through WI-012.
- [x] Marked the earlier DB-integrity design archived/superseded while preserving its baseline and migration cautions.
- [x] Preserved the 2026-07-14 trace baseline and appended current payment-row closure for `ATS020-P1-05` through `ATS020-P1-10`.
- [x] Aligned current payment/SR documents with stable command identity, strict provider boundaries, retry-gate consumption, refund lease fencing, finalize-only reconciliation, payment-key minimization, and disposable MySQL 7/7 proof.
- [x] Limited acceptance/client actions to observable checks and pointed implementation-only checks to agent evidence.
- [x] Kept retained-DB, live Toss, deployment, client acceptance, non-payment, and overall production-readiness gates open.
- [x] Synchronized audit/design/root indexes.
- [x] Final `validate-docs` and `git diff --check` rerun including WI-013 outputs.

## Reference Documents

### Tier 0 and Tier 1

| Tier | Document | Use |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Approval, language, transparency, and payment integrity |
| 0 | `docs/standards/documentation-standards.md` | Metadata, links, indexes, and historical-document rules |
| 0 | `docs/standards/glossary.md` | Canonical lifecycle and ATStudio terms |
| 1 | `docs/policies/quality-gates.md` | Traceability, validation, rollback, and Evidence Pack gate |
| 1 | `docs/policies/archive-policy.md` | Live SoT, historical record, and archived-reference mutation rules |

### REQ and Current Documentation Context

- `deliverables/user/REQ-20260714-ATS-001.md`
- `docs/design/p1-payment-integrity-remediation-design.md`
- `docs/design/p1-payment-db-integrity-design.md`
- `docs/audit/p1-remediation-trace-matrix-20260714.md`
- `docs/SR/SR-93.md`
- `docs/payment/index.md`
- `docs/payment/system-overview.md`
- `docs/payment/feature-inventory.md`
- `docs/payment/admin-operations-guide.md`
- `docs/payment/known-limits-and-next-steps.md`
- `docs/payment/acceptance-test-checklist.md`
- `docs/client/index.md`
- `docs/client/1-quick-checklist.md`
- `docs/client/2-full-feature-checklist.md`
- `docs/client/3-admin-checklist.md`

### Evidence and Commits

- Required handoff evidence: `deliverables/agent/WI-20260715-ATS-007-evidence-pack.md` through `WI-20260715-ATS-012-evidence-pack.md`.
- Package implementation evidence additionally read for accurate A-G mapping: `deliverables/agent/WI-20260715-ATS-001-evidence-pack.md` through `WI-20260715-ATS-006-evidence-pack.md`.
- Source finding/design evidence: `deliverables/agent/WI-20260714-ATS-023-evidence-pack.md` and `WI-20260714-ATS-036-evidence-pack.md`.
- Required commit pointers inspected: `1ecfe5c`, `830c8dd`, `3f18fed`, `46edd88`, `14053e6`.
- Package commits inspected: `103fdf4`, `77c2ebd`, `f5bbd7b`, `49e8774`, `45daf18`, `d0bc21b`.

Injection order: Tier 0 -> Tier 1 -> handoff/context -> evidence snapshots. Assignee: `docops`. Task type: documentation closure and current-state alignment.

## Evidence Pointers

### Closure and Historical Preservation

- `docs/audit/p1-payment-integrity-closure-20260715.md` - current F-01 through F-05 decision, package/review chain, exact MySQL artifacts, open gates, and rollback.
- `docs/audit/p1-remediation-trace-matrix-20260714.md` - original baseline retained; current payment closure added only as a dated appendix.
- `docs/design/p1-payment-integrity-remediation-design.md` - `stable` current design with Package A-G and WI-008 through WI-012 mapping.
- `docs/design/p1-payment-db-integrity-design.md` - `archived` superseded design with replacement path; original gaps and migration cautions retained.

### Current Operations and Acceptance

- `docs/SR/SR-93.md` - production readiness remains OPEN and names every unverified environment gate.
- `docs/payment/index.md`, `system-overview.md`, `feature-inventory.md`, `admin-operations-guide.md`, `known-limits-and-next-steps.md` - current code/test capabilities and operational boundaries.
- `docs/payment/acceptance-test-checklist.md` - observable test actions plus direct technical evidence pointers.
- `docs/client/index.md`, `1-quick-checklist.md`, `2-full-feature-checklist.md`, `3-admin-checklist.md` - Korean test-only boundaries and observable payment checks.

### Indexes and WI Outputs

- `docs/audit/index.md` - trace matrix and closure report registered.
- `docs/design/index.md` - three P1 designs registered with current lifecycle status.
- `docs/index.md` - Design `28`, Audit `6`, total `192`.
- `deliverables/user/WI-20260715-ATS-013-summary.md` - user-facing decision, paths, results, and risks.
- `deliverables/agent/WI-20260715-ATS-013-evidence-pack.md` - this reproducibility record.

## Current Decision Evidence

- Package G final runner: `deliverables/agent/WI-20260715-ATS-007/run-package-g-mysql-proof.ps1`.
- Authoritative final result: `deliverables/agent/WI-20260715-ATS-007/run-summary.log` records schema PASS, Hibernate validate PASS, MySQL races PASS, drop PASS, and cleanup database count `0`.
- Race suite: 7 tests, 0 failures, 0 errors, 0 skipped; exact outcomes are listed in the WI-007 Evidence Pack.
- Historical 5/7 diagnostics remain in the WI-007 directory and are explicitly labeled as earlier-run evidence.
- WI-009/WI-010 FAIL at `830c8dd` remains historical evidence.
- WI-011 commit `46edd88` corrected refund `NEVER`, SUBSCRIBE gates, retry-gate consumption, and payment-key minimization.
- WI-012 commit `14053e6` returned PASS with no P0/P1 in that follow-up scope and one non-blocking P3 test gap.

## Commands and Results

- `git show --no-ext-diff --format=fuller --name-status <commit>` for the required and package commits: PASS; commit scopes and identities recorded above.
- PowerShell markdown reads for every Tier 0/Tier 1, handoff, context, and evidence pointer: PASS; no pointer was missing.
- Documentation index count comparison: PASS; Design `28`, Audit `6`, total `192` after adding the closure report.
- Initial `python .agents/skills/validate-docs/scripts/validate_docs.py`: warning-only exit `2` for three design documents missing from `docs/design/index.md`; links, Tier 0, and trace IDs passed.
- `docs/design/index.md` was synchronized with those three existing design files.
- Final `python .agents/skills/validate-docs/scripts/validate_docs.py`: PASS, exit `0`; Tier 0, internal links, supported trace IDs, and document indexing all passed with no warnings.
- Final `git diff --check`: PASS, exit `0`; Git emitted only existing LF-to-CRLF working-copy notices.
- `git diff --no-index --check -- NUL <new-file>` for the closure report and both WI-013 outputs: PASS; exit `1` was the expected content-difference result and no whitespace diagnostics were emitted.

## Tests

- Product code/tests: not run; WI-013 owns documentation only and relies on the cited independent implementation/test evidence.
- No database, provider, server, preview, tunnel, or runtime operation was executed.

## Intentionally Unchanged

- Historical WI-007 through WI-012 Evidence Packs and their PASS/FAIL chronology.
- Product code, tests, schema, manual SQL, runtime logs, secrets, provider configuration, and deployment state.
- Untracked Cloudflare/Vite runtime logs and unrelated user/concurrent changes.
- `deliverables/agent/WI-20260715-ATS-013-handoff.md` content.

## Risks / Rollback

Risks:

- Fresh disposable MySQL proof does not establish retained-database applicability or migration safety for a specific environment.
- Live Toss, real-money payment, production deployment/configuration, production monitoring, and client acceptance remain unverified.
- WI-012 leaves a non-blocking P3 gap for a dedicated rendered unknown-cancel log assertion.
- Refund same-key recovery remains bounded by the verified provider idempotency-retention contract; otherwise it must remain lookup-only and Incident-backed.

Rollback:

- Revert only the documentation and WI-013 output paths listed in this Evidence Pack.
- Preserve all historical audits, evidence packs, payment ledgers, audit/Incident evidence, schema, runtime logs, and unrelated changes.
- Reverting documentation does not require a product, database, provider, or environment rollback.

## Follow-up

- `WI-20260715-ATS-014` final quality gate remains responsible for the broader release/production-readiness decision.

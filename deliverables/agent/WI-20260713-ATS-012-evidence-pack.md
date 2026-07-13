# Evidence Pack: WI-20260713-ATS-012

## Summary

- Aligned active documentation with implementation commit `d11c62d` for protected Track media, secret-free mail logging, and local-first withdrawal billing stop.

## Scope / DoD Check

- [x] API, DB, and Track/mail/withdrawal use cases match current code.
- [x] Payment design, runbook, payment guide pack, and SR-93 describe cleanup, Incident, retry, convergence, and no-auto-refund.
- [x] Security policy defines public media and delivery-log boundaries.
- [x] Client checklist adds a simple withdrawal renewal-stop check.
- [x] Historical audit findings are preserved and linked to a dated closure report.
- [x] Index versions/counts, dates, links, and traceability IDs validate.

## Reference Documents

| Tier | Document | Reason |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Current-state and evidence rules |
| 0 | `docs/standards/documentation-standards.md` | Documentation and index rules |
| 0 | `docs/standards/glossary.md` | Canonical domain wording |
| 1 | `docs/policies/security-policy.md` | Media and mail security boundary |
| 1 | `docs/policies/quality-gates.md` | Validation evidence |
| 2 | `docs/design/p0-release-blocker-remediation-design.md` | Approved contract |
| 3 | `deliverables/agent/WI-20260713-ATS-009-evidence-pack.md` | Security review |
| 3 | `deliverables/agent/WI-20260713-ATS-010-evidence-pack.md` | Payment transaction review |
| 3 | `deliverables/agent/WI-20260713-ATS-011-evidence-pack.md` | Cross-layer matrix |

## Changed-Document Matrix

| Area | Files | Current-state correction |
|---|---|---|
| API/DB | `docs/design/api-spec.md`, `docs/design/db-schema.md`, `src/main/resources/schema.sql` | Public/admin Track split, bounded stream, withdrawal lifecycle, COMMENT-only schema wording |
| Use cases | `docs/design/usecase/index.md`, `sound-track.md`, `util.md`, `user-info.md` | No async generator, no full-original fallback, delivery metadata only, local-first withdrawal |
| Security | `docs/policies/security-policy.md`, `docs/standards/glossary.md` | Protected original route, safe mail logs, corrected upload definition |
| Payment | `docs/design/payment-integration-design.md`, `payment-operations-runbook.md`, `docs/payment/*.md`, `docs/SR/SR-93.md` | Cleanup event/transaction, Incident, 01:15 retry, already-removed convergence, no automatic refund |
| Audit | `docs/audit/full-system-audit-20260713.md`, `p0-release-blocker-closure-20260713.md`, `docs/audit/index.md` | Historical findings preserved; implementation commit `d11c62d` linked |
| Client | `docs/client/2-full-feature-checklist.md`, `docs/client/index.md` | Simple acceptance wording for withdrawal renewal stop |
| Indexes | `docs/index.md`, `docs/design/index.md` | API v19, DB v14, Standards 12, Audit 4, total 187 |

## Corrected False Claims

- Removed active claims that Track upload enqueues asynchronous preview generation.
- Replaced complete-original `audio_file` fallback with the bounded controller-mediated prefix contract.
- Replaced console email payload fallback with random delivery-ID/outcome/exception-class-only logs.
- Added the previously undocumented withdrawal charge-stop and Provider-cleanup compensation flow.
- Corrected accidental future date `2026-07-14` to KST work date `2026-07-13`.

Historical v3 statements remain only in explicitly labelled change-history sections and are marked superseded by current behavior.

## Commands and Results

- `python .agents/skills/validate-docs/scripts/validate_docs.py`
  - exit 0
  - Tier 0 present
  - no broken internal links
  - 314 supported traceability IDs
  - all documents indexed
- `git diff --check`
  - exit 0; no whitespace errors
- Date scan: no `2026-07-14`, `26-07-14`, or `20260714` residue in active docs/deliverables except the WI-016 handoff's literal negative-test pattern.
- Stale-claim scan: remaining async-preview matches are current negations or explicitly superseded history.
- Count contract: root overview uses direct non-index Markdown files per category; nested `docs/standards/public_data/standard_glossary/README.md` remains indexed as a reference asset but is not added to the Standards direct-file count. Result: Standards 12, Audit 4, total 187.

## Risks / Rollback

- Risks:
  - Dedicated preview generation and physical original-file migration remain unimplemented.
  - Documentation cannot prove live Provider, SMTP, or production MySQL behavior.
- Rollback:
  - Revert the WI-012 documentation commit only.
  - `schema.sql` changes are COMMENT text only; no schema/data rollback is required.

## Follow-ups

- WI-013 through WI-016 final quality gates.
- WI-017 final P0 closure decision; broader audit release verdict remains separate.

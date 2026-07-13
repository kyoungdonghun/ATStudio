[WI HEADER]
WI ID: WI-20260713-ATS-012
REQ: REQ-20260713-ATS-001
Agent: docops
Depends On: WI-20260713-ATS-009, WI-20260713-ATS-010, WI-20260713-ATS-011
Blocks: WI-20260713-ATS-013, WI-20260713-ATS-016

[WI SUMMARY]
Why: Align all active current-state documents with the implemented and verified P0 behavior.
Scope (in/out): Correct false async preview-generation/full-original-fallback statements; document public/admin Track contract and bounded fallback; document secret-free mail logging; document withdrawal local-first cancellation, after-commit cleanup, Incident, daily retry, already-removed convergence, and no-auto-refund. Update API/use-case/schema comments, security/payment/runbook/SR/client acceptance references, indexes/counts, and create a dated P0 closure report. Preserve the historical audit findings while adding remediation status. No product-source change except non-structural `schema.sql` COMMENT wording when required.
DoD: Active docs describe only current code; closure report maps each P0 finding to commits/evidence/tests; indexes/counts validate.
Constraints/Forbidden: English docs except user-facing WI summary. No schema structure/data change, no new feature promise, no claim that a dedicated preview generator exists, no rewriting historical audit evidence as if it never occurred.

[ACCEPTANCE CRITERIA]
- [ ] `api-spec.md`, `db-schema.md`, sound-track/util/user-info use cases match current behavior.
- [ ] payment integration/runbook/payment pack/SR-93 describe withdrawal cleanup and retry.
- [ ] security policy describes delivery-ID-only mail logging.
- [ ] client checklist uses simple Korean and includes withdrawal renewal-stop check where appropriate.
- [ ] audit closure report and indexes/counts are current.
- [ ] docs validator and diff check pass.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/documentation-standards.md
- docs/standards/glossary.md
Tier 1:
- docs/policies/security-policy.md
- docs/policies/quality-gates.md
Tier 2:
- docs/design/p0-release-blocker-remediation-design.md
- docs/audit/full-system-audit-20260713.md
- docs/design/api-spec.md
- docs/design/db-schema.md
- docs/design/usecase/sound-track.md
- docs/design/usecase/util.md
- docs/design/usecase/user-info.md
- docs/design/payment-integration-design.md
- docs/design/payment-operations-runbook.md
- docs/payment/
- docs/SR/SR-93.md
- docs/client/2-full-feature-checklist.md
REQ/Context Docs:
- deliverables/agent/WI-20260713-ATS-009-evidence-pack.md
- deliverables/agent/WI-20260713-ATS-010-evidence-pack.md
- deliverables/agent/WI-20260713-ATS-011-evidence-pack.md
Files:
- Active docs and their category indexes
- src/main/resources/schema.sql comment text only if needed

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260713-ATS-012-summary.md
Agent-facing -> deliverables/agent/WI-20260713-ATS-012-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260713-ATS-012-handoff.md

[TRACEABILITY REQUIREMENTS]
Changed-doc matrix, before/after false claims, validator result, counts, and rollback: Required

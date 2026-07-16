[WI HEADER]
WI ID: WI-20260716-ATS-030
REQ: REQ-20260716-ATS-002
Agent: qa-integ
Depends On: WI-20260716-ATS-028, WI-20260716-ATS-029
Blocks: WI-20260716-ATS-031

[WI SUMMARY]
Why: Independently verify that the final remediation closes every WI-025~027 finding without introducing new security, state, API, UI, or documentation regressions.
Scope (in): Integrated WI-028/WI-029 code/tests/docs; F-025-01..05, F-026-01..03, F-027-01..05; lock/fence semantics, read-only reconciliation, clock/zone, privacy, request races, CORS/export, modal behavior, wire examples, and commit hygiene.
Scope (out): New implementation, client branch/runtime mutation, retained/live DB/provider execution, staging/commit/push, generated-output deletion.
DoD: Each finding receives CLOSED, REOPENED, or ENVIRONMENT-CONDITIONAL disposition with exact evidence. New findings are severity ordered. Product invariants, schema stability, client isolation, and current automated gate results are explicitly checked.
Constraints/Forbidden: Read-only integration review. Do not edit product code/docs, stage, commit, push, restart, mutate DB/provider, delete files, or touch the client worktree. The only writable files are the WI-030 summary and evidence pack. Preserve concurrent worktree changes.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Verify withdrawal/renewal and correction/payment fences under all source-reasonable interleavings.
- [ ] Prove ADMIN reconciliation GET is side-effect free while scheduled recovery still mutates through the intended path.
- [ ] Verify configured zone controls all relevant scheduled business dates and privacy sanitization does not leak raw fragments.
- [ ] Verify frontend latest-request/coalesced-refresh/detail-close behaviors and separate-origin export contract/tests.
- [ ] Reconcile code, DTO, API, operations, glossary/use-case, and WI evidence.
Quality:
- [ ] Findings first, severity ordered, with tight file:line pointers.
- [ ] Review current full-gate evidence supplied by MA and identify any uncovered regression gap.
- [ ] Confirm no schema change and no client worktree modification.
- [ ] Produce both deliverables.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
- docs/standards/documentation-standards.md
- docs/standards/glossary.md
Tier 1:
- docs/policies/security-policy.md
- docs/policies/quality-gates.md
Tier 2:
- docs/design/remaining-remediation-design-20260716.md
- docs/design/api-spec.md
- docs/design/payment-integration-design.md
- docs/design/payment-operations-runbook.md
- docs/ui/screen-flow.md
REQ/Findings/Implementation:
- deliverables/user/REQ-20260716-ATS-002.md
- deliverables/user/WI-20260716-ATS-025-summary.md
- deliverables/user/WI-20260716-ATS-026-summary.md
- deliverables/user/WI-20260716-ATS-027-summary.md
- deliverables/user/WI-20260716-ATS-028-summary.md
- deliverables/user/WI-20260716-ATS-029-summary.md
- corresponding evidence packs
Files/Repro:
- git diff for WI-028/WI-029 changed paths
- current backend/frontend test reports supplied by MA
- git status --short --branch

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260716-ATS-030-summary.md
Agent-facing -> deliverables/agent/WI-20260716-ATS-030-evidence-pack.md
Handoff -> deliverables/agent/WI-20260716-ATS-030-handoff.md

[TRACEABILITY REQUIREMENTS]
- Provide a disposition matrix for every F-025, F-026, and F-027 item.
- Record reviewed files/lines, test evidence, residual environment gates, and rollback implications.
- If any P0/P1/P2 is found or reopened, set the decision to CHANGES_REQUIRED and specify the smallest follow-up WI scope.

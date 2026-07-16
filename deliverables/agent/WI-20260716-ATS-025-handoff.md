[WI HEADER]
WI ID: WI-20260716-ATS-025
REQ: REQ-20260716-ATS-002
Agent: cr
Depends On: WI-20260716-ATS-022
Blocks: WI-20260716-ATS-028

[WI SUMMARY]
Why: Independently re-audit the cumulative backend and security-sensitive remediation diff before it is committed.
Scope (in): Changed backend production/test code, security boundaries, payment/provider privacy, transaction and locking invariants, schema/config changes, and approved product invariants.
Scope (out): Client-demo worktree changes, runtime/database/provider mutation, new product behavior, broad cleanup unrelated to the cumulative diff.
DoD: Findings are ordered by severity with exact file/line evidence; false positives and environment-conditional items are separated; P0/P1/P2 disposition and affected tests/docs are identified.
Constraints/Forbidden: Read-only review. Do not edit product files, stage, commit, push, restart runtimes, call live providers, or modify either database. Treat other worktree edits as concurrent work and never revert them.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Reconcile backend changes against REQ-20260716-ATS-002 and the four product invariants.
- [ ] Review authentication/authorization/rate limits, payment identifiers and receipts, billing-key handling, whitelist/company certification, storage compensation, downloads, albums/playlists, and schema/config changes.
- [ ] Check transaction boundaries, lock ordering, idempotency, stale state, exception mapping, logging, and secret/PII exposure.
Quality:
- [ ] Every actionable finding includes severity, reproduction/reasoning, and tight file/line pointers.
- [ ] If no finding exists, state that explicitly and name residual test/environment risks.
- [ ] Produce both required deliverables.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
Tier 1:
- docs/policies/security-policy.md
- docs/policies/quality-gates.md
Tier 2:
- docs/design/remaining-remediation-design-20260716.md
- docs/design/api-spec.md
- docs/design/db-schema.md
- docs/design/payment-integration-design.md
REQ/Context:
- deliverables/user/REQ-20260716-ATS-002.md
- deliverables/user/WI-20260716-ATS-017-summary.md
- deliverables/user/WI-20260716-ATS-022-summary.md
Files/Repro:
- git diff -- src/main src/test build.gradle application-local.example.yml
- git status --short --branch

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260716-ATS-025-summary.md
Agent-facing -> deliverables/agent/WI-20260716-ATS-025-evidence-pack.md
Handoff -> deliverables/agent/WI-20260716-ATS-025-handoff.md

[TRACEABILITY REQUIREMENTS]
- Use file:line pointers and exact commands; do not paste large source blocks.
- Record reviewed surfaces, exclusions, findings, residuals, and recommended verification.
- Include rollback guidance only for proposed fixes; this WI is review-only.

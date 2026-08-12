[WI HEADER]
WI ID: WI-20260809-ATS-040
REQ: REQ-20260809-ATS-001
Agent: se
Depends On: WI-20260809-ATS-031
Blocks: WI-20260809-ATS-051, WI-20260809-ATS-063

[WI SUMMARY]
Why: `CR-031-075`, `CR-031-076`, and `CR-031-112` prove that the ADMIN Whitelist export UI can misstate the applied scope/status mutation, leave stale actionable rows after a failed latest reload, and lose the only usable batch identity when a committed export response is not received.
Scope (in/out):
- In: Build export confirmation copy from the applied status and applied keyword, never the draft keyword input. State that every matching status is included when status is ALL and that every matching `PENDING` row becomes `EXPORTED`; other statuses remain unchanged.
- In: On a latest list-load failure, clear channels, pagination, and row edits before showing the error so stale rows cannot be operated. Preserve latest-request ownership and ignore stale success/failure/finally paths.
- In: Add a bounded ADMIN recent-export lookup using existing durable `whitelist_export_batches` fields. Scope results to the authenticated ADMIN, exact normalized status/keyword scope, newest `createdAt`/ID order, and at most 10 summaries.
- In: Expose batch ID, filename, item count, recorded status/keyword, and created time. Keep byte-stable `GET /exports/{batchID}` replay as the only download path.
- In: When POST export has an ambiguous response (no definitive 4xx result), do not auto-retry. Query recent exact-scope batches, show that the outcome is unknown, present candidate batch identities/times/counts, and permit explicit replay. A definitive 4xx remains a normal failure without claiming a commit.
- In: Add focused backend, frontend API, and page tests for applied/draft scope, ALL+keyword confirmation, PENDING mutation disclosure, failed reload quarantine, response-loss recovery, actor/scope isolation, bounded ordering, and no second export mutation.
- In: Update current Whitelist use case, API, security/operations documentation, and WI evidence.
- Out: Schema/DDL/data migration, client operation-key/idempotency column, automatic export retry, external CSV handoff, live export, production data, workflow-policy changes, or revision/requeue decision WI-063.
DoD:
- Confirmation text exactly represents the request body and durable status effects for explicit-status and keyword-only scopes.
- A failed current list request leaves zero actionable stale rows; stale requests cannot erase or replace a newer result.
- An ambiguous POST outcome is recoverable through actor-owned recent batch identities without issuing another POST.
- Recent history is bounded, exact-scope, actor-isolated, and does not expose another ADMIN's export metadata.
- Known batch replay remains byte-stable and no existing CSV row/header contract changes.
- Focused and adjacent tests, full frontend/backend quality gates, docs validation, and diff check pass.
Constraints/Forbidden:
- Do not inspect protected output artifacts or ignored secrets.
- Do not access a live/local persistent DB; use mocks/H2/test fixtures only.
- Do not add or modify schema, dependencies, CSV columns, status transitions, export maximum, or business policy.
- Do not perform a real export or external handoff.
- Do not silently auto-retry an ambiguous mutation.
- Do not implement WI-051 or WI-063 behavior.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Confirmation names the applied status and keyword and excludes unapplied draft text.
- [ ] ALL plus applied keyword states that all matching statuses are exported and matching `PENDING` rows transition to `EXPORTED`.
- [ ] Explicit `PENDING` scope states that matching rows transition to `EXPORTED`; non-PENDING scope states that status does not change.
- [ ] Latest list failure clears rows, page information, and edits and leaves no row mutation controls.
- [ ] Stale list success/failure/finally cannot override the newest request state.
- [ ] ADMIN can list at most 10 own recent batches for an exact normalized status/keyword scope in newest-first order.
- [ ] Recent-batch summaries expose only batch ID, filename, item count, recorded scope, and created time.
- [ ] Ambiguous POST failure triggers recovery lookup and no second export POST.
- [ ] Definite 4xx failure does not claim a committed export.
- [ ] A recovered batch can be replayed through the existing batch download endpoint with matching response identity.
Performance:
- [ ] Recent lookup is limited in the repository query and does not load batch items or CSV bytes.
- [ ] Export and replay retain the existing configured maximum and immutable snapshot behavior.
Quality:
- [ ] Focused backend service/controller/repository tests pass.
- [ ] Focused frontend API/page tests pass, including request races and response-loss recovery.
- [ ] Full backend test/JaCoCo/assemble and frontend test/coverage/typecheck/ESLint/Prettier/build pass.
- [ ] Current Whitelist/API/security documentation matches implementation.
- [ ] Documentation validation and `git diff --check` pass.
- [ ] Independent QA-INTEG review confirms UI -> API -> durable batch -> replay consistency.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 1 (Policies - Inferred from REQ/WI content):
- docs/policies/security-policy.md
- docs/policies/quality-gates.md

Tier 2 (Current contracts):
- docs/standards/frontend-standards.md
- docs/design/api-spec.md
- docs/design/db-schema.md
- docs/design/usecase/whitelist.md
- .agents/skills/react-best-practices/SKILL.md

REQ/Context Docs:
- deliverables/user/REQ-20260809-ATS-001.md
- deliverables/agent/WI-20260809-ATS-029-findings.md:251
- deliverables/agent/WI-20260809-ATS-031-consolidated-findings.md:639
- deliverables/agent/WI-20260809-ATS-031-consolidated-findings.md:676

Files:
- frontend/src/pages/admin/WhitelistChannelManagePage.tsx
- frontend/src/pages/admin/WhitelistChannelManagePage.module.css
- frontend/src/pages/admin/WhitelistChannelManagePage.render.test.tsx
- frontend/src/api/admin.ts
- frontend/src/api/adminWhitelistChannels.test.ts
- src/main/java/com/atstudio/atstudio/controller/AdminWhitelistChannelController.java
- src/main/java/com/atstudio/atstudio/service/AdminWhitelistChannelService.java
- src/main/java/com/atstudio/atstudio/entity/WhitelistExportBatch.java
- src/main/java/com/atstudio/atstudio/repository/WhitelistExportBatchRepository.java
- src/main/java/com/atstudio/atstudio/dto/whitelist/
- src/test/java/com/atstudio/atstudio/controller/AdminWhitelistChannelControllerTest.java
- src/test/java/com/atstudio/atstudio/service/AdminWhitelistChannelServiceTest.java

Repro/Logs:
- Use mocked frontend requests and backend mocks/H2 only.
- Simulate an ambiguous POST rejection followed by a successful exact-scope history lookup and explicit known-batch replay.
- Do not invoke actual external export consumers.

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260809-ATS-040-summary.md:
- Korean summary of applied-scope confirmation, stale-row quarantine, recovery workflow, validation, and residual risk.
Agent-facing -> deliverables/agent/WI-20260809-ATS-040-evidence-pack.md:
- Evidence pointers, red/green proof, actor/scope/time recovery contract, tests, rollback, and next chain.
Handoff Packet -> deliverables/agent/WI-20260809-ATS-040-handoff.md:
- This packet.

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required
Tests: Record focused frontend/backend commands and results, then full gate results.
Rollback: Revert the recent-history endpoint/DTO/query, recovery UI, confirmation/list-state changes, tests, docs, and WI deliverables as one patch. No data rollback is needed because no schema or runtime data migration is performed.

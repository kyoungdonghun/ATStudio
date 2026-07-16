[WI HEADER]
WI ID: WI-20260716-ATS-007
REQ: REQ-20260716-ATS-002
Agent: se
Depends On: WI-20260716-ATS-004
Blocks: WI-20260716-ATS-010, WI-20260716-ATS-012, WI-20260716-ATS-013, WI-20260716-ATS-015, WI-20260716-ATS-016

[WI SUMMARY]
Why: Close the remaining whitelist state, concurrency, plan-limit, primary-channel, and export reproducibility gaps without changing the approved subscription policy.
Scope (in):
- Make the existing `CANCELLED` state the explicit terminal state for completed external whitelist removal; distinguish it from `REMOVAL_REQUESTED`, document the contract, and make repeated completion/removal handling idempotent.
- Serialize user-scoped whitelist mutations so create, delete/removal request, primary selection, submission, and plan-limit checks cannot violate limits under concurrent requests.
- Enforce exactly zero-or-one primary channel per user and preserve a valid primary channel when channels are removed or become ineligible.
- Add optimistic/version or database-backed invariants where they materially protect concurrent writes, with fresh-schema and additive manual-patch alignment if schema changes are required.
- Replace unbounded/implicit CSV export with an explicit, bounded selection contract: recorded filters and/or selected channel IDs, deterministic ordering, a configurable hard maximum, immutable export batches/items, and byte-stable re-download from the stored batch selection.
- Keep `userEmail` in the CSV as the user-approved operational identifier; otherwise minimize PII and do not add secrets or unrelated profile fields.
- Align backend APIs, React admin/subscriber UX where affected, automated tests, canonical design/current-state docs, and API/schema/UI descriptions.
Scope (out):
- Actual YouTube or agency registration integration, asynchronous job infrastructure, multi-server locks, provider-side automation, live DB migration execution, production data mutation, and client-demo branch propagation.
- Subscription plan/limit policy changes, removal of userEmail from CSV, or new product states beyond the approved `CANCELLED` contract.
DoD:
- Status transitions, idempotency, concurrent plan-limit/primary invariants, bounded export, immutable batch re-download, and PII shape are covered by automated tests.
- Existing whitelist flows remain functional, documentation reflects implementation, and relevant backend/frontend quality gates pass.
Constraints/Forbidden:
- Work only in `codex/p1-acceptance-hardening` under `C:\Users\jm991\Desktop\project\ATStudio`.
- Do not modify, switch, merge, or restart `codex/client-demo-stable` or its Cloudflare-backed runtime.
- Do not execute DDL against any local/retained/production database and do not delete or mutate real data.
- Do not expose channel/user PII in logs, error messages, batch metadata, or evidence beyond the approved CSV fields.
- Do not weaken plan limits, authorization, subscription/download policy, public full-track listening, or single-server assumptions.
- Preserve unrelated user and prior-WI changes in the dirty worktree.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] `REMOVAL_REQUESTED -> CANCELLED` represents completed external removal, is permission-checked, and repeated completion is idempotent.
- [ ] Illegal status transitions fail with a stable business error and do not partially mutate channel, primary, or export state.
- [ ] Concurrent creates/submissions cannot exceed the active plan's whitelist limit.
- [ ] At most one primary channel exists per user; removing or invalidating the primary leaves a deterministic valid state.
- [ ] Export requires explicit recorded scope, deterministic order, and a hard maximum; an oversized request fails without a partial batch.
- [ ] Re-downloading an export batch uses immutable stored items and produces the same logical CSV rows without re-query drift.
- [ ] CSV includes `userEmail` and only the operational channel/subscription fields justified by the current contract.
- [ ] Subscriber and admin UI/API behavior remains coherent with the revised state and export contracts.
Performance:
- [ ] User mutation paths use bounded queries/locks and do not load all users or all whitelist rows.
- [ ] Export selection and download are bounded by the configured maximum; no unbounded repository list is introduced.
Quality:
- [ ] Focused whitelist service/controller/repository tests pass, including concurrency or deterministic lock-oriented proofs.
- [ ] Affected frontend typecheck, ESLint, Vitest, build, and changed-file Prettier checks pass when frontend changes are made.
- [ ] Documentation validation and `git diff --check` pass.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md
- docs/standards/development-standards.md
- docs/standards/documentation-standards.md
- docs/standards/glossary.md

Tier 1 (Policies):
- docs/policies/security-policy.md
- docs/policies/quality-gates.md
- docs/policies/access-control-policy.md

Tier 2 (Domain / Approved Design):
- docs/design/remaining-remediation-design-20260716.md
- docs/design/usecase/whitelist.md
- docs/design/api-spec.md
- docs/design/db-schema.md
- docs/ui/screen-flow.md
- deliverables/user/REQ-20260716-ATS-002.md
- deliverables/user/REQ-20260615-ATS-001.md
- deliverables/user/WI-20260615-ATS-001-summary.md
- deliverables/agent/WI-20260615-ATS-001-evidence-pack.md

Files:
- src/main/java/com/atstudio/atstudio/entity/WhitelistChannel.java
- src/main/java/com/atstudio/atstudio/entity/WhitelistExportBatch.java
- src/main/java/com/atstudio/atstudio/entity/WhitelistExportItem.java
- src/main/java/com/atstudio/atstudio/entity/enums/WhitelistChannelStatus.java
- src/main/java/com/atstudio/atstudio/repository/WhitelistChannelRepository.java
- src/main/java/com/atstudio/atstudio/repository/WhitelistExportBatchRepository.java
- src/main/java/com/atstudio/atstudio/repository/WhitelistExportItemRepository.java
- src/main/java/com/atstudio/atstudio/service/WhitelistChannelService.java
- src/main/java/com/atstudio/atstudio/service/AdminWhitelistChannelService.java
- src/main/java/com/atstudio/atstudio/controller/WhitelistChannelController.java
- src/main/java/com/atstudio/atstudio/controller/AdminWhitelistChannelController.java
- src/main/java/com/atstudio/atstudio/dto/whitelist/
- frontend/src/api/whitelistChannels.ts
- frontend/src/pages/subscriber/WhitelistChannelPage.tsx
- frontend/src/pages/admin/WhitelistChannelManagePage.tsx
- src/main/resources/schema.sql
- src/main/resources/db/manual/
- src/test/java/com/atstudio/atstudio/service/WhitelistChannelServiceTest.java
- src/test/java/com/atstudio/atstudio/service/AdminWhitelistChannelServiceTest.java
- src/test/java/com/atstudio/atstudio/controller/WhitelistChannelControllerTest.java

Repro / Inspection:
- `rg -n "WhitelistChannelStatus|countByUser|findByUser|export|CANCELLED|REMOVAL_REQUESTED|primary" src/main/java frontend/src docs`
- Inspect the current schema/manual-patch chain before adding any additive DDL source file.

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260716-ATS-007-summary.md:
- Behavior changes, operator/user-visible effects, tests, risks, and environment-only follow-ups.
Agent-facing -> deliverables/agent/WI-20260716-ATS-007-evidence-pack.md:
- Exact evidence pointers, transition/invariant table, schema/API/UI patch notes, reproducible commands/results, rollback, and follow-up findings.
Handoff Packet -> deliverables/agent/WI-20260716-ATS-007-handoff.md:
- This packet.

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required.
Tests: Include exact focused backend/frontend commands and result counts; do not claim real-DB concurrency or DDL proof without an approved environment run.
Rollback: Describe source/schema/manual-patch rollback without deleting ledger/export history or user channel data.
Environment boundary: Mark retained-DB DDL/constraint validation as `ENVIRONMENT-CONDITIONAL` when it was not actually executed.

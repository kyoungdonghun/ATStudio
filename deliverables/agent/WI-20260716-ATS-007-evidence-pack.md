# Evidence Pack: WI-20260716-ATS-007

## Summary (one-liner)

- Formalized the whitelist removal lifecycle and concurrency contract, preserved withdrawal evidence, added bounded immutable exports with replay, aligned the React workflow and schema sources, and retained real-MySQL proof as environment-conditional.

## Scope / DoD Check

- [x] `CANCELLED` is the terminal completed-removal state; repeated current status and repeated removal requests are idempotent.
- [x] Illegal transitions return `INVALID_STATE_TRANSITION` before status, primary, or export mutation.
- [x] User-scoped create, update/requeue, request, primary, removal/delete, admin status mutation, export status mutation, and withdrawal use the owning user-row lock contract.
- [x] Plan-limit and primary mutations are serialized; `@Version` supplies an optimistic channel fence.
- [x] Primary-ineligible removal states are cleared and deterministic eligible replacement promotion is implemented.
- [x] Withdrawal preserves `EXPORTED`, `REGISTERED`, and `REMOVAL_REQUESTED` evidence under the removal lifecycle.
- [x] Member edits cannot mutate `REMOVAL_REQUESTED` or `CANCELLED` metadata; subscriber UI does not offer those edits.
- [x] ATS007-F05 saved-row growth is bounded by the separate configurable `APP_WHITELIST_MAX_SAVED_CHANNELS` technical cap (default 100); creation acquires the existing user row lock, counts all saved rows, and rejects at the cap before save without changing plan registration limits.
- [x] User list reads use the same cap and deterministic primary-first/newest-first order; retained over-cap behavior is explicit and tested without changing the response shape.
- [x] Export requires recorded scope, uses deterministic bounded selection and stable lock order, and fails oversized requests before partial batch/status writes.
- [x] Batch/item filters and ordered operational snapshots support byte-stable bounded re-download without current-data queries.
- [x] CSV retains `userEmail`, omits new user-ID/nickname snapshots, and formula-neutralizes text cells.
- [x] Subscriber/admin UI, API, schema/manual source, tests, and canonical docs are aligned.
- [x] Focused backend/frontend checks pass; no DDL or data mutation was executed.

## Reference Documents (Tier 0-2)

| Tier | Document | Reason |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Constitution and approval/execution boundary |
| 0 | `docs/standards/development-standards.md` | Java, transaction, testing, and schema standards |
| 0 | `docs/standards/documentation-standards.md` | Canonical documentation and traceability rules |
| 0 | `docs/standards/glossary.md` | Domain terminology |
| 1 | `docs/policies/security-policy.md` | PII, logging, and environment-secret boundaries |
| 1 | `docs/policies/quality-gates.md` | Test, diff, rollback, and environment-proof gates |
| 1 | `docs/policies/access-control-policy.md` | USER/ADMIN endpoint boundaries |
| 2 | `docs/design/remaining-remediation-design-20260716.md` | Approved P1/P2 whitelist decisions and residual proof boundary |
| 2 | `docs/design/usecase/whitelist.md` | Whitelist lifecycle and operator workflow |
| 2 | `docs/design/api-spec.md` | API and CSV contract |
| 2 | `docs/design/db-schema.md` | Fresh/retained schema contract |
| 2 | `docs/ui/screen-flow.md` | Subscriber/admin screen behavior |
| Context | `deliverables/user/REQ-20260716-ATS-002.md` | Approved requirement and acceptance criteria |
| Context | `deliverables/agent/WI-20260716-ATS-007-handoff.md` | Delegated scope, ownership, and output contract |

The handoff and its Tier/context pointers were read before implementation. Shared WI-005/WI-006 edits were preserved and the client-demo/runtime boundary was not touched.

## Transition and Invariant Contract

| Source | Allowed target |
|---|---|
| `DRAFT` | none |
| `PENDING` | `REGISTERED`, `REVISION_REQUESTED`, `REJECTED` |
| `EXPORTED` | `REGISTERED`, `REVISION_REQUESTED`, `REJECTED`, `REMOVAL_REQUESTED` |
| `REGISTERED` | `REVISION_REQUESTED`, `REMOVAL_REQUESTED` |
| `REVISION_REQUESTED` | `REGISTERED`, `REJECTED` |
| `REJECTED` | none |
| `REMOVAL_REQUESTED` | `CANCELLED` |
| `CANCELLED` | none; terminal |

| Invariant | Source proof |
|---|---|
| Current-status and repeated removal calls are idempotent | `AdminWhitelistChannelService.java:116-123`; `WhitelistChannelService.java:174-188` |
| Owning user lock precedes channel/count mutation | `WhitelistChannelService.java:53-181`; `AdminWhitelistChannelService.java:100-170`; `UserService.java:125-160` |
| Zero-or-one primary under cooperating writes | `WhitelistChannelService.java:144-160`; `AdminWhitelistChannelService.java:239-250` |
| Removal states are primary-ineligible | `WhitelistChannel.java:133-136` |
| Plan-counting states remain approved policy | `WhitelistChannelService.java:27-33` |
| Saved-row safety cap is separate and lock-protected | `WhitelistChannelService.java:49-56`; `WhitelistChannelProperties.java:14-20` |
| User list is bounded to the saved-row cap | `WhitelistChannelService.java:73-80`; `WhitelistChannelRepository.java:24` |
| Removal target metadata is member-immutable | `WhitelistChannelService.java:88-99` |
| Export selection and replay are bounded | `AdminWhitelistChannelService.java:138-226` |
| New export snapshots omit user ID/nickname | `AdminWhitelistChannelService.java:187-207` |

## Evidence Pointers

- Formal transition matrix, lock-driven admin status mutation, bounded export, stable user/channel lock order, immutable replay, and CSV shaping: `src/main/java/com/atstudio/atstudio/service/AdminWhitelistChannelService.java:46-335`.
- User create/update/request/primary/delete lock, technical saved-row cap, bounded list, and plan-limit contract: `src/main/java/com/atstudio/atstudio/service/WhitelistChannelService.java:44-249`; cap configuration: `src/main/java/com/atstudio/atstudio/config/WhitelistChannelProperties.java:1-20`.
- Withdrawal evidence preservation: `src/main/java/com/atstudio/atstudio/service/UserService.java:125-162`; bulk repository operations: `src/main/java/com/atstudio/atstudio/repository/WhitelistChannelRepository.java:103-116`.
- Pessimistic user/channel/export queries and deterministic replacement/export order: `src/main/java/com/atstudio/atstudio/repository/UserRepository.java:16-19`, `src/main/java/com/atstudio/atstudio/repository/WhitelistChannelRepository.java:34-101`.
- Optimistic version and terminal/primary eligibility helpers: `src/main/java/com/atstudio/atstudio/entity/WhitelistChannel.java:22-25`, `src/main/java/com/atstudio/atstudio/entity/WhitelistChannel.java:101-136`.
- Recorded batch scope and immutable item fields: `src/main/java/com/atstudio/atstudio/entity/WhitelistExportBatch.java:29-38`, `src/main/java/com/atstudio/atstudio/entity/WhitelistExportItem.java:32-86`.
- Explicit export request/replay API and batch response header: `src/main/java/com/atstudio/atstudio/controller/AdminWhitelistChannelController.java:62-87`; request/config DTOs: `src/main/java/com/atstudio/atstudio/dto/whitelist/AdminWhitelistExportRequest.java`, `src/main/java/com/atstudio/atstudio/config/WhitelistExportProperties.java`.
- Fresh schema/config: `src/main/resources/schema.sql:231-301`, `src/main/resources/application.yml:107-111`; retained-DB source patch and inspection queries: `src/main/resources/db/manual/20260716_whitelist_integrity_and_exports.sql:1-110`.
- Frontend explicit export/replay client: `frontend/src/api/admin.ts:176-213`; transition controls and batch re-download: `frontend/src/pages/admin/WhitelistChannelManagePage.tsx:131-344`, `frontend/src/pages/admin/whitelistStatusTransitions.ts:1-12`; subscriber removal immutability: `frontend/src/pages/subscriber/WhitelistChannelPage.tsx:23-48`, `frontend/src/pages/subscriber/WhitelistChannelPage.tsx:364-368`, `frontend/src/pages/subscriber/whitelistChannelPolicy.ts:1-7`.
- Backend lifecycle/export/withdrawal/schema tests: `src/test/java/com/atstudio/atstudio/service/AdminWhitelistChannelServiceTest.java`, `src/test/java/com/atstudio/atstudio/service/WhitelistChannelServiceTest.java`, `src/test/java/com/atstudio/atstudio/service/UserServiceTest.java`, `src/test/java/com/atstudio/atstudio/service/WhitelistConcurrencyContractTest.java`, `src/test/java/com/atstudio/atstudio/controller/AdminWhitelistChannelControllerTest.java`.
- Frontend request/header, admin transition, and subscriber edit-policy tests: `frontend/src/api/adminWhitelistChannels.test.ts`, `frontend/src/pages/admin/WhitelistChannelManagePage.test.ts`, `frontend/src/pages/subscriber/whitelistChannelPolicy.test.ts`.
- Canonical contracts: `docs/design/api-spec.md:9-18`, `docs/design/api-spec.md:3147-3205`, `docs/design/db-schema.md:799-865`, `docs/design/usecase/whitelist.md:15-20`, `docs/design/usecase/whitelist.md:120-134`, `docs/design/usecase/whitelist.md:211-260`, `docs/policies/security-policy.md:196-203`, `docs/ui/screen-flow.md:271-279`, `docs/ui/screen-flow.md:367-377`.

## Commands & Outputs

- MA forced focused Gradle rerun -> PASS, 12 suites / 86 tests / 0 failures / 0 errors / 0 skipped; BUILD SUCCESSFUL. Covered whitelist services, controllers, withdrawal preservation, and concurrency contracts.
- `npm test -- --run src/api/adminWhitelistChannels.test.ts src/pages/admin/WhitelistChannelManagePage.test.ts src/pages/subscriber/whitelistChannelPolicy.test.ts` -> PASS, 3 files / 7 tests.
- `npm run typecheck` -> PASS.
- `npm run lint` -> PASS with zero warnings.
- `npm run build` -> PASS, 261 modules transformed.
- `npx prettier --check <nine affected whitelist frontend files>` -> PASS.
- `python .agents/skills/validate-docs/scripts/validate_docs.py` -> PASS: Tier 0, internal links, 401 traceability IDs, and document index.
- `git diff --check` -> exit 0; output contained repository line-ending conversion warnings only.
- `$env:PYTHONIOENCODING='utf-8'; python .agents/skills/lint/scripts/lint_all.py` -> TOOLING-CONDITIONAL: helper started, but `markdownlint`, `jq`, and `ruff` are not installed. No dependency was installed; required docs validation and frontend ESLint passed independently.
- No database command, manual SQL execution, live/test data mutation, client-demo operation, or Cloudflare action was performed.

## Risks / Rollback

- Risk: retained MySQL DDL runtime, index shape, pessimistic lock behavior, Hibernate validation, and existing duplicate-primary inventory are not proven in this source-only environment. They remain `ENVIRONMENT-CONDITIONAL`.
- Risk: MySQL lacks a partial unique index for the approved eligible-primary predicate. The WI intentionally uses user-row serialization plus `@Version`; database-only uniqueness remains residual proof instead of adding a generated column.
- Risk: a retained batch whose `itemCount` exceeds the configured maximum is rejected before item loading. This preserves bounded behavior but requires an operator decision before replaying an unusually large legacy batch.
- Risk: retained users above `APP_WHITELIST_MAX_SAVED_CHANNELS` receive only the deterministic leading window because the existing response has no total/truncation field. Inventory and cleanup remain an operator concern; the cap should stay well above normal plan limits.
- Rollback: revert only WI-007 whitelist code, tests, frontend files, configuration key, canonical whitelist doc sections, and deliverables listed here. Preserve WI-005/WI-006 and unrelated shared-file hunks.
- Rollback before deployment: omit `20260716_whitelist_integrity_and_exports.sql` and revert fresh-schema/JPA fields together.
- Rollback after deployment: leave additive snapshot/version columns and export history in place unless an approved DBA change proves safe. Do not delete export ledger rows, retained removal evidence, or user channel data; application code can ignore additive columns.

## Follow-ups

- WI-007 unblocks WI-010, WI-012, WI-013, WI-015, and WI-016 subject to their remaining dependencies.
- A named environment owner must rehearse the manual patch on an approved copied MySQL database, inventory duplicate primaries, inspect the export index, exercise concurrent user/primary writes, and run Hibernate validation before closing the residual DB proof.
- ATS007-F05 is closed in source: draft creation and user list reads now share the bounded technical cap, with retained over-cap behavior explicit in code, tests, and canonical docs.
- No product ambiguity or implementation blocker remains.

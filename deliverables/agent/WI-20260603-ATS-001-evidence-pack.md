# Evidence Pack: WI-20260603-ATS-001

## Summary (one-liner)
- Expanded whitelist backend into saved channel drafts, registration requests, admin status workflow, and CSV export ledger.

## Scope / DoD Check
- [x] Extended `whitelist_channels` schema/entity/DTO/repository/service/controller.
- [x] Added whitelist status enum and plan-limit counted status policy.
- [x] Added representative channel API.
- [x] Added admin list/status update/CSV export APIs.
- [x] Persisted CSV export batch/item snapshots including `userEmail`.
- [x] Kept non-`PENDING` export from overwriting workflow status.
- [x] Allowed export snapshots to survive later source channel deletion through nullable source-channel FK.
- [x] Re-request from `REVISION_REQUESTED` reuses the channel's own counted slot and returns to `PENDING`.
- [x] Updates that requeue `REGISTERED`/`EXPORTED`/`REVISION_REQUESTED` channels use the same subscription and limit gate.
- [x] Deleting the primary local-only channel promotes another saved channel when one remains.
- [x] Added focused backend tests and passed full backend test suite.

## Reference Documents (Tier 0-2)

| Tier | Document | Reason |
|------|----------|--------|
| 0 | docs/standards/core-principles.md | Constitution |
| 0 | docs/standards/development-standards.md | Java/Spring implementation standards |
| 1 | docs/policies/security-policy.md | Admin/CSV information exposure boundary |
| 2 | docs/design/api-spec.md | API contract |
| 2 | docs/design/db-schema.md | DB schema contract |
| 2 | docs/design/usecase/whitelist.md | Whitelist workflow |
| 2 | docs/ui/screen-flow.md | UI flow dependency |

## Evidence Pointers
- `src/main/java/com/atstudio/atstudio/entity/enums/WhitelistChannelStatus.java:3` — status enum.
- `src/main/java/com/atstudio/atstudio/entity/WhitelistChannel.java:16` — expanded entity fields and workflow methods.
- `src/main/java/com/atstudio/atstudio/service/WhitelistChannelService.java:99` — registration request with subscription/limit checks.
- `src/main/java/com/atstudio/atstudio/service/WhitelistChannelService.java:136` — primary channel update.
- `src/main/java/com/atstudio/atstudio/service/WhitelistChannelService.java:151` — delete/removal request behavior.
- `src/main/java/com/atstudio/atstudio/service/WhitelistChannelService.java:119` — counted correction states exclude their own slot before plan-limit comparison.
- `src/main/java/com/atstudio/atstudio/service/WhitelistChannelService.java:85` — processed-channel updates must pass the same reprocessing eligibility gate.
- `src/main/java/com/atstudio/atstudio/service/WhitelistChannelService.java:195` — primary replacement promotion after local deletion.
- `src/main/java/com/atstudio/atstudio/service/AdminWhitelistChannelService.java:41` — admin-mutable status allowlist.
- `src/main/java/com/atstudio/atstudio/service/AdminWhitelistChannelService.java:105` — CSV export and export ledger creation.
- `src/main/java/com/atstudio/atstudio/service/AdminWhitelistChannelService.java:153` — only `PENDING` exports transition to `EXPORTED`.
- `src/main/java/com/atstudio/atstudio/entity/WhitelistExportItem.java:67` — exported-at snapshot.
- `src/main/resources/schema.sql:208` — expanded `whitelist_channels`.
- `src/main/resources/schema.sql:235` — `whitelist_export_batches`.
- `src/main/resources/schema.sql:250` — `whitelist_export_items`.
- `src/test/java/com/atstudio/atstudio/service/AdminWhitelistChannelServiceTest.java:68` — CSV export test includes `userEmail` and marks EXPORTED.
- `src/test/java/com/atstudio/atstudio/service/AdminWhitelistChannelServiceTest.java:114` — non-`PENDING` export keeps original workflow status.
- `src/test/java/com/atstudio/atstudio/service/WhitelistChannelServiceTest.java:172` — `REVISION_REQUESTED` can re-request without consuming a new slot.
- `src/test/java/com/atstudio/atstudio/service/WhitelistChannelServiceTest.java:262` — processed-channel update requeues with the existing counted slot.
- `src/test/java/com/atstudio/atstudio/service/WhitelistChannelServiceTest.java:286` — processed-channel update without active subscription is blocked.
- `src/test/java/com/atstudio/atstudio/service/WhitelistChannelServiceTest.java:357` — primary channel deletion promotes another saved channel.
- `src/test/java/com/atstudio/atstudio/controller/WhitelistChannelControllerTest.java:140` — new request/primary endpoint authorization tests.

## Commands & Outputs
- `gradlew.bat test --tests "com.atstudio.atstudio.service.WhitelistChannelServiceTest" --tests "com.atstudio.atstudio.controller.WhitelistChannelControllerTest" --tests "com.atstudio.atstudio.service.AdminWhitelistChannelServiceTest"` → PASS.
- `gradlew.bat test` → PASS.
- `git diff --check` → PASS.

## Risks / Rollback
- Risk: Existing local/production DBs using `ddl-auto=validate` need manual DDL application before server startup.
- Risk: Exported CSV contains user email by approved requirement; operational handling should keep the CSV distribution narrow.
- Rollback: Revert whitelist status/export entities, schema additions, controllers/services, DTO/repository changes, and related tests.

## Follow-ups
- Optional: add a first-class DB migration framework later if manual schema updates become frequent.

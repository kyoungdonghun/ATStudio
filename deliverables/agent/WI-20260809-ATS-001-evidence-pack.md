# Evidence Pack: WI-20260809-ATS-001

## Summary

- Status: COMPLETE.
- Completed WI-001: retired route-policy removal, safe manifest observation, verified constants/hash refresh, and isolated disposable-MySQL cleanup proof.

## Scope / DoD Check

- [x] Removed only the retired direct ADMIN `PUT`/`DELETE /api/user-subscriptions/*` matchers.
- [x] Kept `/me` authentication and ADMIN collection `GET` policy.
- [x] Emitted six non-sensitive manifest fields before the unchanged fail-closed comparison.
- [x] Added source-level ordering and redaction guards while retaining manual-migration and unrelated-database-enumeration guards.
- [x] Ran the direct mapping test and bootstrap guard script.
- [x] `[redacted probe]` failed closed with `V1_MANIFEST_MISMATCH`, emitted the six allowed manifest fields, and returned `cleanupAfterFailure=PASS`.
- [x] Updated only the manifest constants/hash after the observed values matched the expected source baseline.
- [x] `[redacted proof]` passed Create, standalone Validate, and two exact Drops; each scoped Drop absence check returned zero.
- [x] Updated README and WI-022 output status without recording target names or bundle content.

## Reference Documents

| Tier | Document                                            | Reason                                         |
| ---- | --------------------------------------------------- | ---------------------------------------------- |
| 0    | `docs/standards/core-principles.md`                 | Constitution                                   |
| 0    | `docs/standards/development-standards.md`           | Implementation and evidence standards          |
| 1    | `docs/policies/security-policy.md`                  | Authorization and secret-redaction constraints |
| 1    | `docs/policies/access-control-policy.md`            | Retired subscription route policy              |
| 1    | `docs/policies/quality-gates.md`                    | Focused verification                           |
| 2    | `deliverables/user/REQ-20260808-ATS-004.md`         | Approved requirement source                    |
| 2    | `deliverables/agent/WI-20260809-ATS-001-handoff.md` | Scope and output contract                      |

## Evidence Pointers

- `src/main/java/com/atstudio/atstudio/config/SecurityConfig.java:131` keeps authenticated `/me` routes; the collection ADMIN `GET` remains immediately after them and the retired wildcard mutation matchers are absent.
- `scripts/database/DisposableMysqlBootstrap.java:323` emits `tables`, `columns`, `indexes`, `foreignKeys`, `plans`, and `sha256`; `scripts/database/DisposableMysqlBootstrap.java:329` keeps the existing `V1_MANIFEST_MISMATCH` fail-closed throw after those emissions.
- `scripts/database/test-bootstrap-guards.ps1:201` lists the allowed source-observed manifest keys; `scripts/database/test-bootstrap-guards.ps1:209` asserts comparison ordering; `scripts/database/test-bootstrap-guards.ps1:226` through `:232` retain manual-migration, `SHOW DATABASES`, and manifest database-name guards.
- `src/test/java/com/atstudio/atstudio/controller/UserSubscriptionControllerTest.java:138` verifies that direct administrator PUT and DELETE mappings are retired. This test was already present in the shared worktree and was executed unchanged.
- `scripts/database/DisposableMysqlBootstrap.java:47-52` now expects `41/493/168/89`, six plans, and SHA-256 `c581bef61cfba143744882b0674daf8d8fe742d82adbbf66d6b61699f5b86333`.

## Commands and Results

- `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/database/test-bootstrap-guards.ps1`
  - PASS: parser, preflight-only safety checks, target-name redaction, manifest ordering/redaction, current SQL input, manual-migration absence, and unrelated database-enumeration absence.
  - No MySQL connection, Create, Validate, or Drop action occurred.
- `.\\gradlew.bat test --tests "com.atstudio.atstudio.controller.UserSubscriptionControllerTest"`
  - PASS: `BUILD SUCCESSFUL` in 42 seconds.
  - The requested `@SpringBootTest` initialized JPA/Hikari. No direct database command was issued, but this test run is not evidence of zero datasource connections.
- `git diff --check -- src/main/java/com/atstudio/atstudio/config/SecurityConfig.java scripts/database/DisposableMysqlBootstrap.java scripts/database/test-bootstrap-guards.ps1 src/test/java/com/atstudio/atstudio/controller/UserSubscriptionControllerTest.java`
  - PASS: no whitespace errors.
- `[redacted probe]` Create
  - Expected nonzero exit with `schema.apply=PASS`, `seed.apply=PASS`, all six manifest values, `reason=V1_MANIFEST_MISMATCH`, and `cleanupAfterFailure=PASS`.
  - Exact Drop passed and its scoped absence count was zero.
- `[redacted proof]` Create and standalone Validate
  - PASS: both observed `41/493/168/89`, six plans, SHA-256 `c581bef61cfba143744882b0674daf8d8fe742d82adbbf66d6b61699f5b86333`, and `manifest=PASS`.
  - First exact Drop and idempotent second exact Drop both passed with scoped absence count zero.
- Frontend Prettier `--check` passed for both new WI-001 outputs. Six existing changed documents retain their historical formatting differences; this baseline style finding is non-blocking and those documents were not rewritten.

## Risks / Rollback

- Limitation: The requested controller test uses `@SpringBootTest`; its JPA/Hikari initialization means the test cannot certify that no test datasource connection occurred.
- Boundary: The isolated proof does not certify retained data, production migration, deployment, providers, or runtime behavior.
- Rollback: Revert WI-001 source and documentation changes. Both isolated targets were removed, so no database rollback is required.

## Follow-up

- The WI-022 blocker disposition is updated. Successor WIs may proceed under their own approved scopes and gates.

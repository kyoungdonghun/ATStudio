# Work Item Summary: WI-20260809-ATS-001

## Status

COMPLETE (verified).

## Delivered

- Removed the retired direct `PUT` and `DELETE /api/user-subscriptions/*` ADMIN matchers from `SecurityConfig`.
- Preserved the authenticated `/api/user-subscriptions/me` routes and the ADMIN `GET /api/user-subscriptions` rule.
- Changed disposable-MySQL validation to emit only `tables`, `columns`, `indexes`, `foreignKeys`, `plans`, and `sha256` manifest observations before the existing fail-closed `matchesExpected()` comparison.
- Added source-level guards that require every permitted manifest observation to occur before the comparison and retain database-name redaction, retired-manual-migration absence, and `SHOW DATABASES` absence checks.
- Verified the existing direct controller-mapping regression test for retired administrator mutation routes.
- Observed `41/493/168/89`, six plans, and SHA-256 `c581bef61cfba143744882b0674daf8d8fe742d82adbbf66d6b61699f5b86333` from `[redacted probe]`, then updated only the corresponding validator constants.
- `[redacted probe]` failed closed with `V1_MANIFEST_MISMATCH` and `cleanupAfterFailure=PASS`; its exact Drop passed with scoped residual count zero.
- `[redacted proof]` passed Create, standalone Validate, and two exact Drops; both Drops confirmed scoped residual count zero.

## Verification

- `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/database/test-bootstrap-guards.ps1` passed.
- `.\\gradlew.bat test --tests "com.atstudio.atstudio.controller.UserSubscriptionControllerTest"` passed.
- Scoped `git diff --check` passed with no whitespace errors.
- The isolated bootstrap operations emitted no target names or bundle contents.

## Scope Boundary

The proof covers only fresh isolated disposable databases. It does not verify retained data, production migration, deployment, external providers, or application runtime behavior. No protected database, other database enumeration, schema/seed source, bundle content, or external action was used.

## Rollback

Revert the WI-001 source and documentation changes if required. Both isolated targets were removed, so no database rollback remains.

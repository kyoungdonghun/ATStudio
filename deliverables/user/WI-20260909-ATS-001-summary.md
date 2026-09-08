---
version: 1.1
last_updated: 2026-09-09
project: ATS
owner: PG
category: reference
status: active
---

# WI-20260909-ATS-001: Authentication Corrections

## Delivered
- SEC-01/03: strict signed access/refresh purposes, unique refresh issuance IDs, no typeless fallback. Wrong-purpose refresh is rejected before account lookup; old stored-hash replay cannot revoke the replacement session.
- SEC-02: validation diagnostics contain only bounded field/category/count information. Rejected inputs and original/cause/suppressed throwables no longer reach global-handler log events.
- SEC-05: reset-token issuance and consumption use User -> PasswordResetToken locks; token consumption, password update and refresh revocation share one transaction.
- AUTH-06: existing passwordless social accounts can refresh before/after profile completion without falsely verifying provider email. Password accounts retain verification requirements; providers remain unchanged.

## Verification Status
MA's initial focused compilation/run passed in 1m 7s. Its preserved JSON records nine authentication-related suites and 84 tests with zero failures, errors or skips. This verifies the selected tests at that compilation checkpoint, including real-token and H2 reset scenarios; PG started no runner.

`SensitiveValidationLogTest` and `SecurityFilterChainTest` are absent from the initial JSON and still require MA execution. The new captured-log assertions are not covered by the `GlobalExceptionHandler*Test` selection. Existing 100% critical-class LINE/METHOD gates remain untouched; final full/coverage gates and independent review have not passed yet. MySQL, external mail/OAuth, production logs and live runtime remain unverified.

## Applying The Fix
Old typeless sessions must log in again after deployment. This is an intentional security correction, not a compatibility option. New access-token TTLs and current DB-role authorization are unchanged; logout/reset continue to revoke refresh capability without introducing a new access-revocation policy.

No live account/mail/payment/provider action, runtime restart, Git operation, DDL or retained-data cleanup was performed. No product-policy choice remains open within this WI.

## Evidence / Next Step
The [Evidence Pack](../agent/WI-20260909-ATS-001-evidence-pack.md) lists all six product files, eight test files, exact initial JSON counts, the original 10-class request and remaining gates. Source results: `output/release-remediation-20260909/focused-initial.log` and `focused-initial-results.json`.

PG implementation handback is complete; MA can free the slot for WI004, include the two missing suites in serialized verification, and schedule WI010 after WI004. This is not production approval or parent REQ closure. Revision 1.1 adds MA's initial results to the prior unexecuted-request report.

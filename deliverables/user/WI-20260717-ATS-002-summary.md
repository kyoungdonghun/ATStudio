# WI-20260717-ATS-002 Summary

## Status

Completed the approved backend cleanup within backend/test ownership. No frontend, schema/manual SQL, application/provider configuration, active documentation, Git reference, runtime process, secret, or `frontend/tsconfig.tsbuildinfo` change was made.

## Implemented

- `INT-R01`, `INT-R02`: removed the server Play History and Download Queue contracts, persistence models, DTOs, repositories, services, controllers, cleanup consumers, and obsolete tests. Browser-local play history, Public Listening, Official Download authorization/accounting/licensing/history, and Track download counts remain.
- `INT-V01`, `INT-V02`: removed the legacy one-time payment surface and blocked direct subscription-creation endpoint. Recurring billing agreement checkout, renewal, upgrade, cancellation, reconciliation, refund, audit, lease/fence, and recovery paths remain.
- `INT-V03`, `INT-V04`, `INT-V05`: removed subscription-status and user-type utility endpoints and the unused ADMIN subscription detail endpoint. ADMIN subscription update/cancel remains as the approved emergency operation.
- `INT-R03`, `INT-R04`, `INT-R05`, `INT-R07`: removed the deprecated four-argument upgrade finalizer, preview Java consumer, whitelist user-ID/nickname snapshot consumers, and stale `PUT /api/settings/*` matcher. Real `/api/admin/settings/*` authorization is covered by USER/ADMIN tests.

## Validation

- Exact negative searches: 11/11 passed for removed Java symbols, routes, fields, and matchers.
- `gradlew.bat compileJava compileTestJava --console=plain`: passed after removing five approved whitelist legacy-field assertions found by the first test compile.
- Focused Gradle tests: 19 suites, 146 tests, 0 failures, 0 errors, 0 skipped.
- `git diff --check -- src/main/java src/test/java`: passed.

## Inventory and Follow-up

- Product/test diff: 60 files total, with 25 modified and 35 deleted; the complete path list is in `deliverables/agent/WI-20260717-ATS-002-evidence-pack.md`.
- WI-004 owns schema column/table removal, provider enum/config normalization, and application profile/bootstrap changes. WI-003 owns frontend consumers; WI-005 owns active documentation; WI-006 owns full cross-layer and runtime verification.

---
version: 1.0
last_updated: 2026-07-24
project: ATS
owner: se
category: evidence-pack
status: confirmed
related_wi: WI-20260724-ATS-023
---

# Evidence Pack: WI-20260724-ATS-023

## Change Summary

- Added the established BUSINESS member eligibility check to
  `CompanyCertificationService.getMyStatus` before certification persistence
  access.
- Added service, controller, and independent security regressions for
  non-BUSINESS, ADMIN, and changed-account historical-record access.
- Kept the patch within the approved company-certification service and directly
  related tests.

## Scope

### In

- BUSINESS eligibility guard for the self-status read path.
- Existing `RESOURCE_NOT_ACCESS` error semantics.
- Repository non-invocation and HTTP contract regression tests.

### Out

- Schema and data changes.
- Frontend changes.
- Route, request, response, and DTO changes.
- Company certification workflow redesign.
- Provider, payment, or mail behavior.

## Pointers

### Changed Product Code

- `src/main/java/com/atstudio/atstudio/service/CompanyCertificationService.java`
  - `getMyStatus`: checks `user.getUserType() == UserType.BUSINESS` immediately
    after resolving the current user and before
    `findTopByUserOrderByCreatedAtDescIdDesc`.

### Changed Tests

- `src/test/java/com/atstudio/atstudio/service/CompanyCertificationServiceTest.java`
  - `getMyStatus_individualRejectedBeforeCertificationLookup`
  - `getMyStatus_adminRejectedBeforeCertificationLookup`
  - Both use `verifyNoInteractions(certificationRepository)`.
- `src/test/java/com/atstudio/atstudio/service/CompanyCertificationSecurityVerificationTest.java`
  - `getMyStatus_changedAccountCannotReadHistoricalCertification`
  - Configures a hypothetical retained historical record, then proves the
    current INDIVIDUAL account is rejected without repository interaction.
- `src/test/java/com/atstudio/atstudio/controller/CompanyCertificationControllerTest.java`
  - `getMyStatus_nonBusiness_returns403`
  - Existing `getMyStatus_adminRole_returns403BeforeService` continues to prove
    ADMIN rejection at the security boundary.
  - Existing `getMyStatus_success` continues to prove the BUSINESS success
    response contract.

### Consulted Inputs

- `docs/standards/core-principles.md`
- `docs/standards/development-standards.md`
- `docs/policies/security-policy.md`
- `docs/policies/access-control-policy.md`
- `docs/policies/quality-gates.md`
- `docs/design/usecase/company-certification.md`
- `docs/design/api-spec.md`
- `docs/standards/evidence-pack-standard.md`
- `deliverables/user/REQ-20260724-ATS-002.md`
- `deliverables/agent/WI-20260724-ATS-014-evidence-pack.md`
- `deliverables/agent/WI-20260724-ATS-023-handoff.md`

## Behavior Matrix

| Current actor | Certification record | Result | Certification repository |
|---|---|---|---|
| USER / BUSINESS | Exists | Existing `200` response | Queried |
| USER / BUSINESS | Missing | Existing `RESOURCE_NOT_FOUND` (`404`) | Queried |
| USER / INDIVIDUAL | Historical record may exist | `RESOURCE_NOT_ACCESS` (`403`) | Not invoked |
| ADMIN / INDIVIDUAL | Irrelevant | `403` | Not invoked; security also blocks before service |

## Reproduction and Verification

### TDD RED

```powershell
.\gradlew.bat test `
  --tests "com.atstudio.atstudio.service.CompanyCertificationServiceTest" `
  --tests "com.atstudio.atstudio.service.CompanyCertificationSecurityVerificationTest" `
  --tests "com.atstudio.atstudio.controller.CompanyCertificationControllerTest"
```

Result before the product-code change:

- 69 tests executed.
- 3 expected failures:
  - INDIVIDUAL repository-prevention test.
  - ADMIN repository-prevention test.
  - Changed-account historical-record test.

### Focused GREEN

```powershell
.\gradlew.bat test `
  --tests "com.atstudio.atstudio.service.CompanyCertificationServiceTest" `
  --tests "com.atstudio.atstudio.service.CompanyCertificationSecurityVerificationTest" `
  --tests "com.atstudio.atstudio.controller.CompanyCertificationControllerTest"
```

Result:

- 69 tests passed.
- 0 failures, 0 errors, 0 skipped.

### Related Backend Slice

```powershell
.\gradlew.bat test `
  --tests "com.atstudio.atstudio.bootstrap.TestUserBootstrapRunnerTest" `
  --tests "com.atstudio.atstudio.controller.CompanyCertificationControllerTest" `
  --tests "com.atstudio.atstudio.entity.EntityDefaultValueTest" `
  --tests "com.atstudio.atstudio.entity.CompanyCertificationSchemaContractTest" `
  --tests "com.atstudio.atstudio.entity.CompanyCertificationTest" `
  --tests "com.atstudio.atstudio.service.BillingAgreementApplicationServiceTest" `
  --tests "com.atstudio.atstudio.service.CompanyCertificationSecurityVerificationTest" `
  --tests "com.atstudio.atstudio.service.CompanyCertificationServiceTest" `
  --tests "com.atstudio.atstudio.service.PaymentCommandTransactionFenceTest" `
  --tests "com.atstudio.atstudio.service.storage.StorageReferenceCheckerBranchCoverageTest"
```

Result:

- 118 tests passed.
- 0 failures, 0 errors, 0 skipped.

### Compile and Diff

```powershell
.\gradlew.bat compileJava compileTestJava
python .agents/skills/validate-docs/scripts/validate_docs.py
git diff --check
```

Result:

- Main and test compilation passed.
- Documentation validation passed: Tier 0 present, 0 broken links, 471 valid
  traceability references, and no index omissions.
- Diff whitespace check passed.

## Acceptance Criteria

- [x] INDIVIDUAL member receives the established forbidden response.
- [x] Certification repository is not invoked for INDIVIDUAL or ADMIN callers.
- [x] BUSINESS member with no record retains `RESOURCE_NOT_FOUND`.
- [x] BUSINESS member with a record retains the existing response.
- [x] Changed-account historical-record risk has a direct regression test.
- [x] Focused and related backend tests pass.
- [x] Compilation and diff checks pass.

## Risk and Rollback

### Residual Risk

- This WI changes only the service authorization order. WI-024 must rerun the
  runtime role/API check before WI-015 and WI-016 resume.
- ADMIN is blocked by `SecurityConfig` before controller invocation; the direct
  service test also uses the project's established ADMIN/INDIVIDUAL account
  convention.

### Rollback

1. Remove the four-line BUSINESS guard from `getMyStatus`.
2. Remove the four WI-023 regression tests and the test-only helper.
3. Rerun the focused command above.

Rollback would intentionally restore the WI-014 defect and is not recommended.

## Change Control

- Commit status: verifiable from Git history.
- Push status: verifiable from Git history and remote-tracking refs.

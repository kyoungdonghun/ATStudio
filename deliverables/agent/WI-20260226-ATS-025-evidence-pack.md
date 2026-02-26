# WI-20260226-ATS-025 Evidence Pack

## File Change Inventory

### Deleted Files
| File | Reason |
|------|--------|
| `src/main/java/.../entity/BusinessLicenseRequest.java` | Replaced by CompanyCertification |
| `src/main/java/.../entity/enums/BusinessLicenseStatus.java` | Replaced by CompanyCertificationStatus |
| `src/main/java/.../repository/BusinessLicenseRequestRepository.java` | Replaced by CompanyCertificationRepository |

### New Files (Source)
| File | Type |
|------|------|
| `src/main/java/.../entity/CompanyCertification.java` | Entity |
| `src/main/java/.../entity/enums/CompanyCertificationStatus.java` | Enum |
| `src/main/java/.../repository/CompanyCertificationRepository.java` | Repository |
| `src/main/java/.../dto/certification/CompanyCertificationReviewRequest.java` | DTO |
| `src/main/java/.../dto/certification/CompanyCertificationResponse.java` | DTO |
| `src/main/java/.../dto/certification/CompanyCertificationSummaryResponse.java` | DTO |
| `src/main/java/.../service/CompanyCertificationService.java` | Service |
| `src/main/java/.../controller/CompanyCertificationController.java` | Controller |

### New Files (Test)
| File | Test Count |
|------|------------|
| `src/test/java/.../service/CompanyCertificationServiceTest.java` | 14 |
| `src/test/java/.../controller/CompanyCertificationControllerTest.java` | 16 |

### Modified Files (Source)
| File | Change |
|------|--------|
| `src/main/java/.../config/SecurityConfig.java` | URL rename + /me auth rule |
| `src/main/java/.../common/exception/BUSINESS_ERROR.java` | BUSINESS_LICENSE_REQUIRED -> COMPANY_CERTIFICATION_REQUIRED |
| `src/main/resources/schema.sql` | Table rename |

### Modified Files (Test)
| File | Change |
|------|--------|
| `src/test/java/.../entity/EntityDefaultValueTest.java` | BusinessLicenseRequest -> CompanyCertification |

### Modified Files (Documentation)
| File | Change |
|------|--------|
| `docs/design/usecase/company-certification.md` | New (content from business-license.md) |
| `docs/design/usecase/business-license.md` | Replaced with redirect |
| `docs/design/usecase/index.md` | BL -> CC codes |
| `docs/design/usecase/user-subscription.md` | References updated |
| `docs/design/usecase/user-license.md` | References updated |
| `docs/design/usecase/util.md` | BL-001 -> CC-001 |
| `docs/design/api-spec.md` | Section 13 fully renamed |
| `docs/design/db-schema.md` | Table/column/ER diagram renamed |
| `docs/standards/exception-handling.md` | Error code renamed |
| `docs/standards/glossary.md` | Entry renamed |

## Test Case Inventory

### CompanyCertificationServiceTest (14 tests)
| # | Method | Description |
|---|--------|-------------|
| 1 | apply_success | BUSINESS member normal apply |
| 2 | apply_notBusinessMember | INDIVIDUAL -> RESOURCE_NOT_ACCESS |
| 3 | apply_duplicatePending | Existing PENDING -> RESOURCE_DUPLICATE |
| 4 | apply_duplicateApproved | Existing APPROVED -> RESOURCE_DUPLICATE |
| 5 | getMyStatus_exists | Application exists -> Response |
| 6 | getMyStatus_notFound | No application -> null |
| 7 | listAll_noFilter | All certifications |
| 8 | listAll_withStatusFilter | Status filter |
| 9 | getDetail_success | Detail retrieval |
| 10 | getDetail_notFound | RESOURCE_NOT_FOUND |
| 11 | processReview_approve | APPROVED + certificationCode |
| 12 | processReview_revisionRequested | REVISION_REQUESTED + adminNote |
| 13 | processReview_reject | REJECTED |
| 14 | processReview_notFound | RESOURCE_NOT_FOUND |

### CompanyCertificationControllerTest (16 tests)
| # | Method | Description |
|---|--------|-------------|
| 1 | apply_unauthenticated_returns401 | POST 401 |
| 2 | apply_success | POST 201 |
| 3 | apply_forbidden_nonBusiness | POST 403 |
| 4 | apply_conflict | POST 400 (RESOURCE_DUPLICATE) |
| 5 | getMyStatus_unauthenticated_returns401 | GET /me 401 |
| 6 | getMyStatus_success | GET /me 200 |
| 7 | listAll_unauthenticated_returns401 | GET 401 |
| 8 | listAll_forbidden_user | GET 403 |
| 9 | listAll_success | GET 200 (admin) |
| 10 | getDetail_unauthenticated_returns401 | GET /{id} 401 |
| 11 | getDetail_forbidden_user | GET /{id} 403 |
| 12 | getDetail_success | GET /{id} 200 |
| 13 | processReview_unauthenticated_returns401 | PUT 401 |
| 14 | processReview_forbidden | PUT 403 |
| 15 | processReview_approve | PUT 200 |
| 16 | processReview_notFound | PUT 404 |

## Build Evidence

- Build: `BUILD SUCCESSFUL in 39s`
- Total tests: **414 tests, 0 failures, 0 skipped**
- Previous: 384 tests -> New: 414 tests (+30)
- Regression: 0 existing test failures

## Rollback Plan

- Phase 1: Reverse rename all files and references
- Phase 2: Delete new files (dto/certification/*, Service, Controller, Tests)

## Next WI

- **WI-20260226-ATS-026**: Build + full test regression verification (re agent)

# WI-20260226-ATS-025 Summary

## What Changed

### Phase 1: Business License -> Company Certification Renaming

All references to "Business License" have been renamed to "Company Certification" across the entire codebase and documentation.

**Source Files Changed:**
- Entity: `BusinessLicenseRequest.java` deleted, `CompanyCertification.java` created
- Enum: `BusinessLicenseStatus.java` deleted, `CompanyCertificationStatus.java` created
- Repository: `BusinessLicenseRequestRepository.java` deleted, `CompanyCertificationRepository.java` created
- SecurityConfig: URL patterns `/api/business-licenses` -> `/api/company-certifications` + `/me` auth rule added
- BUSINESS_ERROR: `BUSINESS_LICENSE_REQUIRED` -> `COMPANY_CERTIFICATION_REQUIRED`
- schema.sql: `business_license_requests` table -> `company_certifications` table
- EntityDefaultValueTest: Updated to use CompanyCertification

**Documentation Files Changed:**
- `docs/design/usecase/company-certification.md` (new, replaces business-license.md)
- `docs/design/usecase/business-license.md` (redirect to new file)
- `docs/design/usecase/index.md` (BL-001~005 -> CC-001~005)
- `docs/design/usecase/user-subscription.md` (references updated)
- `docs/design/usecase/user-license.md` (references updated)
- `docs/design/usecase/util.md` (BL-001 -> CC-001)
- `docs/design/api-spec.md` (Section 13 fully renamed)
- `docs/design/db-schema.md` (table/column names updated)
- `docs/standards/exception-handling.md` (error code updated)
- `docs/standards/glossary.md` (entry updated)

### Phase 2: Company Certification 5 APIs Implemented

| API | Method | Path | Auth | Status |
|-----|--------|------|------|--------|
| 13.1 | POST | /api/company-certifications | auth (BUSINESS) | 201 |
| 13.2 | GET | /api/company-certifications/me | auth | 200 |
| 13.3 | GET | /api/company-certifications | ADMIN | 200 |
| 13.4 | GET | /api/company-certifications/{id} | ADMIN | 200 |
| 13.5 | PUT | /api/company-certifications/{id} | ADMIN | 200 |

**New Files Created:**
- DTOs: `CompanyCertificationReviewRequest`, `CompanyCertificationResponse`, `CompanyCertificationSummaryResponse`
- Service: `CompanyCertificationService`
- Controller: `CompanyCertificationController`
- Tests: `CompanyCertificationServiceTest` (14 tests), `CompanyCertificationControllerTest` (16 tests)

## Key Decisions

1. **getMyStatus null return**: When no certification application exists, returns `null` data (not RESOURCE_NOT_FOUND). Better UX for status check.
2. **SecurityConfig /me rule**: Added `GET /api/company-certifications/me` as `authenticated()` before ADMIN wildcard rule to prevent `/me` from being blocked for regular users.
3. **certificationCode format**: `"BIZ-" + UUID.randomUUID()` generated on APPROVED status only.
4. **RESOURCE_DUPLICATE for duplicate check**: Uses `existsByUserAndStatusIn(user, [PENDING, APPROVED])`. Returns 400 (RESOURCE_DUPLICATE).

## Verification

- Build: `BUILD SUCCESSFUL`
- Tests: **414 tests, 0 failures** (384 existing + 30 new, all passing)

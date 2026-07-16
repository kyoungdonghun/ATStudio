# Evidence Pack: WI-20260716-ATS-008

## Summary (one-liner)

- Integrated Company Certification hardening across backend, frontend, schema sources, and canonical documentation: deterministic cooperating-write locks, versioned and audited review/access grants, strict file validation, BUSINESS-only UI routing, private response minimization, and a MySQL 8.x-compatible source-only retained-DB patch.

## Scope / DoD Check

- [x] Apply/resubmit/review use the owning-user lock before the affected certification lock where one exists; tests verify the cooperating-write call order.
- [x] `@Version`, fresh schema, and dated additive retained-DB patch align without executing DDL.
- [x] Review reasons are trimmed/bounded and required for revision/rejection before status or audit mutation.
- [x] Review and guarded document access grants persist minimum actor/target/status evidence with no copied path, filename, note, profile, token, request, or content fields.
- [x] Null/empty multipart parts reject the entire request; count, per-file, aggregate, filename, extension, signature, and MIME checks remain backend-enforced.
- [x] API responses omit `documentPath`; guarded attachments retain existing private/no-store/nosniff/sandbox/no-Range delivery headers.
- [x] Admin list page size is bounded to 100.
- [x] React apply/status routes require an authenticated USER with `userType=BUSINESS`; INDIVIDUAL and ADMIN users receive the existing access-denied redirect UX.
- [x] Frontend selection validation matches PDF/JPG/JPEG/PNG, 10 files, 20 MiB per file, 50 MiB aggregate, 255-character filenames, empty-file rejection, and consecutive-selection accumulation.
- [x] Frontend certification APIs/pages use the canonical `CompanyCertification` type with no `documentPath` field.
- [x] Admin review UX requires a trimmed reason for revision/rejection, permits an optional approval note, and bounds both at 500 characters.
- [x] API, DB, use-case, security, and screen-flow documentation state the implemented boundary and the retained-DB/scanner/retention conditions.

## Superseded Correction

- The WI handoff phrase "PNG stored MIME `image/png`" is superseded for the current approved architecture. `CanonicalImageService` verifies PNG input as `image/png`, decodes it, and re-encodes canonical private output as JPEG with `image/jpeg`.
- No PNG byte-preserving storage path was added. `VerifiedFormat` now labels this value as a verified input MIME rather than a stored-output MIME.
- `DOCUMENT_ACCESS_GRANTED` records authorization plus private-resource resolution before controller byte streaming. It is an access-grant audit event, not proof of completed byte delivery.

## Reference Documents (Tier 0-2)

| Tier | Document | Reason |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Constitution and traceability rules |
| 0 | `docs/standards/development-standards.md` | Java/Spring implementation standard |
| 0 | `docs/standards/documentation-standards.md` | Documentation standard |
| 0 | `docs/standards/glossary.md` | Canonical terminology |
| 1 | `docs/policies/security-policy.md` | Sensitive-document and audit minimization policy |
| 1 | `docs/policies/quality-gates.md` | Focused verification gates |
| 1 | `docs/policies/access-control-policy.md` | Least-privilege access policy |
| 2 | `docs/design/remaining-remediation-design-20260716.md` | WI-008 remediation boundary |
| 2 | `docs/design/api-spec.md` | Company Certification contract |
| 2 | `docs/design/db-schema.md` | Fresh/retained schema contract |
| 2 | `docs/design/usecase/company-certification.md` | Apply/resubmit/review/access flows |
| 2 | `deliverables/user/REQ-20260716-ATS-002.md` | Approved requirement scope |
| 2 | `deliverables/agent/WI-20260716-ATS-008-handoff.md` | WI output and traceability contract |

## Evidence Pointers

- `src/main/java/com/atstudio/atstudio/service/CompanyCertificationService.java:71-137` locks the authenticated owner for apply/resubmit; `:237-269` locks user then certification for review and writes the review audit event.
- `src/main/java/com/atstudio/atstudio/repository/CompanyCertificationRepository.java:18-26` defines pessimistic certification lock queries; `UserRepository.findByIdForUpdate` is the first lock in each mutation path.
- `src/main/java/com/atstudio/atstudio/entity/CompanyCertification.java:26-28` adds optimistic versioning; `CompanyCertificationAuditLog.java:41-75` contains the minimum audit fields only.
- `src/main/java/com/atstudio/atstudio/service/CompanyCertificationService.java:209-233` writes `DOCUMENT_ACCESS_GRANTED` after authorization and private-resource resolution, before controller byte streaming.
- `src/main/java/com/atstudio/atstudio/controller/CompanyCertificationController.java:100-135` passes the authenticated ADMIN actor to download/review while retaining attachment headers.
- `src/main/java/com/atstudio/atstudio/service/CompanyCertificationService.java:323-371,484-521` rejects every empty part, bounds filenames/reasons, validates input formats, and distinguishes verified PNG input MIME from canonical output MIME.
- `src/main/resources/schema.sql:144-203` contains fresh version/index/audit schema. `src/main/resources/db/manual/20260716_company_certification_integrity_and_audit.sql:14-44` performs MySQL 8.x metadata checks and dynamic `PREPARE`/`EXECUTE` for the column/index delta; it does not use unsupported ALTER `IF NOT EXISTS` syntax.
- `src/test/java/com/atstudio/atstudio/service/CompanyCertificationServiceTest.java:128-130,266-268,623-625` proves user-before-certification service call order; `:710-737` proves narrow access-grant evidence. `CompanyCertificationSchemaContractTest.java:22-45` guards fresh/manual schema grammar and audit-field minimization.
- `frontend/src/router/ProtectedRoute.tsx:13-58` implements role/type denial; `frontend/src/router/index.tsx:131-136,203-204` applies the BUSINESS gate to both certification routes.
- `frontend/src/utils/validation.ts:39-47,94-125` defines and enforces the shared file/review bounds; apply and resubmit pages reuse that validator.
- `frontend/src/types/index.ts:243-267` is the canonical certification response type and contains no persistence-path field. `frontend/src/api/companyCerts.ts` and `frontend/src/api/admin.ts` reuse it.
- `frontend/src/pages/admin/CompanyCertManagePage.tsx:148-173,388-412` implements conditional reason validation, a 500-character bound, and applicant-visible guidance before the backend remains authoritative.
- `docs/design/api-spec.md` Section 13, `docs/design/db-schema.md` Section 3, `docs/design/usecase/company-certification.md`, `docs/policies/security-policy.md` Section 6.8, and `docs/ui/screen-flow.md` record route, review, access-grant, PNG canonicalization, and policy/environment boundaries.

## Contract Matrix

| Area | Implemented contract | Primary evidence |
|---|---|---|
| Mutation order | Cooperating writes acquire owning user before certification when a certification row exists; `@Version` is an additional stale-write fence. | `CompanyCertificationService`, `CompanyCertificationRepository`, `CompanyCertification` |
| Review transition | Only a valid PENDING review transition mutates state. Revision/rejection require a trimmed reason; approval note is optional; all notes are at most 500 characters. | `CompanyCertificationService.normalizeReviewNote`, entity transition tests, admin review page |
| Review audit | Successful review stores actor reference, action, certification ID, from/to statuses, and timestamp only. | `CompanyCertificationAuditLog`, service tests |
| Document access audit | `DOCUMENT_ACCESS_GRANTED` records authorization plus private-resource resolution with an opaque document ID; it is not byte-delivery completion evidence. | download service/controller tests and security policy |
| Upload contract | PDF/JPG/JPEG/PNG; nonempty parts; maximum 10 files, 20 MiB each, 50 MiB aggregate, 255-character filename; extension/signature/compatible MIME checks are authoritative on backend. | `ValidationConstants`, service validation, frontend shared validator |
| Image storage | JPEG/PNG inputs are decoded through `CanonicalImageService` and stored as canonical JPEG with `image/jpeg`; PDF stays `application/pdf`. | `CompanyCertificationService.verifyDocument` |
| Response minimization | Certification responses expose document metadata and opaque IDs, never `documentPath` or stored paths. | response DTO and central frontend type |
| UI authorization | Apply/status routes admit only authenticated USER + BUSINESS. Frontend denial is UX guidance; backend authorization is authoritative. | `ProtectedRoute`, router tests, controller/service tests |
| Persistence rollout | Fresh schema and a dated additive manual source are aligned; retained-DB execution requires preflight/backup/rehearsal. | `schema.sql`, dated manual patch, schema contract test |

## Commands & Outputs

- `.\gradlew.bat test --rerun-tasks --tests "com.atstudio.atstudio.service.CompanyCertificationServiceTest" --tests "com.atstudio.atstudio.service.CompanyCertificationSecurityVerificationTest" --tests "com.atstudio.atstudio.controller.CompanyCertificationControllerTest" --tests "com.atstudio.atstudio.entity.CompanyCertificationTest" --tests "com.atstudio.atstudio.entity.CompanyCertificationSchemaContractTest"`
  - PASS. `BUILD SUCCESSFUL`; XML totals: 12 suites, 73 tests, 0 failures, 0 errors, 0 skipped.
- `npm test -- src/pages/admin/CompanyCertManagePage.test.tsx src/pages/subscriber/CompanyCertApplyPage.test.tsx src/pages/subscriber/CompanyCertStatusPage.test.tsx src/router/index.test.tsx src/router/ProtectedRoute.test.tsx src/utils/validation.test.ts`
  - PASS. 6 files, 30 tests.
- `npm run typecheck`
  - PASS.
- `npx eslint src/api/admin.ts src/api/companyCerts.ts src/pages/admin/CompanyCertManagePage.tsx src/pages/admin/CompanyCertManagePage.test.tsx src/pages/subscriber/CompanyCertApplyPage.tsx src/pages/subscriber/CompanyCertApplyPage.test.tsx src/pages/subscriber/CompanyCertStatusPage.tsx src/pages/subscriber/CompanyCertStatusPage.test.tsx src/router/ProtectedRoute.tsx src/router/ProtectedRoute.test.tsx src/router/index.tsx src/router/index.test.tsx src/types/index.ts src/utils/validation.ts src/utils/validation.test.ts --max-warnings 0`
  - PASS.
- `npm run build`
  - PASS. Vite production build transformed 261 modules.
- `npx prettier --check src/api/admin.ts src/api/companyCerts.ts src/pages/admin/CompanyCertManagePage.module.css src/pages/admin/CompanyCertManagePage.tsx src/pages/admin/CompanyCertManagePage.test.tsx src/pages/subscriber/CompanyCertApplyPage.tsx src/pages/subscriber/CompanyCertApplyPage.test.tsx src/pages/subscriber/CompanyCertStatusPage.tsx src/pages/subscriber/CompanyCertStatusPage.test.tsx src/router/ProtectedRoute.tsx src/router/ProtectedRoute.test.tsx src/router/index.tsx src/router/index.test.tsx src/types/index.ts src/utils/validation.ts src/utils/validation.test.ts`
  - PASS. All listed files matched Prettier style.
- `python .agents/skills/validate-docs/scripts/validate_docs.py`
  - PASS. Tier 0 documents, internal links, 401 traceability IDs, and document index coverage passed.
- `git diff --check`
  - PASS. No whitespace errors; repository line-ending conversion warnings only.
- No DDL, retained-DB inspection/mutation, private-file inspection, user-data mutation, server/public-demo operation, or scanner dependency action was performed.

## Risks / Rollback

- Retained MySQL: `ENVIRONMENT-CONDITIONAL`. A copied-DB backup/rehearsal and backfill assessment remain required before applying the dated patch and starting with Hibernate validation.
- Retention: `POLICY-PENDING`. No purge, withdrawal deletion, duration, owner, or failed-delete path is implemented.
- Scanner: `POLICY-PENDING`. Signature/MIME/canonicalization checks are not malware scanning; no external scanner dependency was selected.
- Access audit: An access grant means authorization plus private-resource resolution succeeded; a later controller stream failure is not separately recorded as delivery success/failure.
- Rollback: revert the WI-008 code/schema/manual-patch/docs together. Do not delete existing certification, audit, document metadata, or private-file rows as part of rollback.

## Follow-ups

- WI-010/WI-012: company-certification route/file-selection parity is complete; remaining shared frontend-state/accessibility and canonical-document cleanup stay in their assigned scopes.
- WI-013/WI-015/WI-016: run broader backend, integration, and security verification before any release claim.

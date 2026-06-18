# Evidence Pack: WI-20260618-ATS-001

## Summary (one-liner)

- Implemented the company certification operating workflow: document metadata, revision resubmission, protected admin download, admin detail review, frontend status UX, and current-state documentation.

## Scope / DoD Check

- [x] BUSINESS users can submit company certification documents.
- [x] `PENDING`, `APPROVED`, and `REVISION_REQUESTED` block duplicate new applications.
- [x] `REVISION_REQUESTED` supports same-application document replacement and returns to `PENDING`.
- [x] `REJECTED` preserves history and allows a new application.
- [x] Admins can list, inspect, download documents, and process review actions.
- [x] Company certification documents are not reviewed through public static links.
- [x] API spec, DB schema, use cases, UI flow, and client testing docs were updated.
- [x] Backend, frontend, docs, and diff hygiene checks were run.

## Reference Documents (Tier 0-2)

**Injected Context** (from WI Handoff Packet):

| Tier | Document | Reason |
|------|----------|--------|
| 0 | `docs/standards/core-principles.md` | Project constitution |
| 0 | `docs/standards/development-standards.md` | Backend/frontend implementation standards |
| 1 | `docs/policies/security-policy.md` | Sensitive document access control |
| 1 | `docs/policies/quality-gates.md` | Verification gates |
| 2 | `deliverables/user/REQ-20260618-ATS-001.md` | Approved requirement |
| 2 | `docs/design/usecase/company-certification.md` | Certification use case source |
| 2 | `docs/design/api-spec.md` | API contract |
| 2 | `docs/design/db-schema.md` | Schema contract |
| 2 | `docs/ui/screen-flow.md` | UI flow |
| 2 | `docs/ui/modal-list.md` | UI modal/component inventory |
| 2 | `docs/client/1-scenarios.md` | Client acceptance scenarios |
| 2 | `docs/client/2-test-cases.md` | Client test cases |

**Injection Rules Applied**:

- Rule source: `.claude/config/context-injection-rules.json`
- Assignee: MA/local implementation
- Task type: backend/frontend/docs/security
- Agent required tiers: Tier 0 plus relevant Tier 1/2 pointers from WI

## Evidence Pointers

### Backend

- `src/main/java/com/atstudio/atstudio/entity/CompanyCertificationDocument.java` — new per-file metadata entity.
- `src/main/java/com/atstudio/atstudio/repository/CompanyCertificationDocumentRepository.java` — certification-scoped document lookup.
- `src/main/java/com/atstudio/atstudio/entity/CompanyCertification.java` — one-to-many documents, document replacement helpers, document path update.
- `src/main/java/com/atstudio/atstudio/service/CompanyCertificationService.java` — duplicate gate, revision resubmission, document storage metadata, protected download service.
- `src/main/java/com/atstudio/atstudio/controller/CompanyCertificationController.java` — resubmit endpoint and admin document download endpoint.
- `src/main/java/com/atstudio/atstudio/config/SecurityConfig.java` — admin-only `/uploads/company-docs/**` and admin document download security rules.
- `src/main/java/com/atstudio/atstudio/dto/certification/CompanyCertificationResponse.java` — applicant and document metadata response.
- `src/main/java/com/atstudio/atstudio/dto/certification/CompanyCertificationSummaryResponse.java` — admin list applicant/company fields.
- `src/main/java/com/atstudio/atstudio/dto/certification/CompanyCertificationDocumentResponse.java` — document metadata response DTO.
- `src/main/java/com/atstudio/atstudio/dto/certification/CompanyCertificationDocumentDownload.java` — document download carrier DTO.
- `src/main/resources/schema.sql` — latest schema includes `company_certification_documents`.
- `src/main/resources/db/manual/20260618_company_certification_documents.sql` — manual DDL patch for existing DBs.

### Frontend

- `frontend/src/types/index.ts` — certification document and enriched certification types.
- `frontend/src/api/companyCerts.ts` — `resubmitCompanyCert`.
- `frontend/src/api/admin.ts` — `downloadCompanyCertDocument`.
- `frontend/src/pages/subscriber/CompanyCertApplyPage.tsx` — rejected-state reapply support.
- `frontend/src/pages/subscriber/CompanyCertStatusPage.tsx` — status-specific CTA and revision resubmission UI.
- `frontend/src/pages/admin/CompanyCertManagePage.tsx` — admin detail modal, document list/download, review modal.
- `frontend/src/pages/subscriber/SubscriptionPaymentPage.tsx` — company certification required message and navigation.

### Tests

- `src/test/java/com/atstudio/atstudio/service/CompanyCertificationServiceTest.java` — duplicate state, revision resubmit, document metadata tests.
- `src/test/java/com/atstudio/atstudio/controller/CompanyCertificationControllerTest.java` — resubmit endpoint and admin-only document download tests.

### Documentation

- `docs/design/api-spec.md` — v18, company certification APIs 13.1-13.7, total 147 APIs.
- `docs/design/db-schema.md` — v13, `company_certification_documents`, total 39 tables.
- `docs/design/usecase/company-certification.md` — CC-001 through CC-007.
- `docs/design/usecase/index.md` — use case count 109.
- `docs/design/index.md`, `docs/index.md`, `docs/registry/project-registry.md` — API/table count current-state updates.
- `docs/ui/atstudio-front-list.md`, `docs/ui/screen-flow.md`, `docs/ui/modal-list.md` — frontend flow and modal inventory updates.
- `docs/client/1-scenarios.md`, `docs/client/2-test-cases.md`, `docs/client/3-test-methodology.md` — acceptance scenario/test updates.

## Commands & Outputs

- `gradlew.bat test` → PASS, 743 tests completed.
- `cd frontend; npm run typecheck` → PASS.
- `cd frontend; npm run lint` → PASS.
- `cd frontend; npm run test` → PASS, 14 files / 51 tests.
- `cd frontend; npm run build` → PASS.
- `cd frontend; npx prettier --check <changed frontend files>` → PASS.
- `python .agents/skills/validate-docs/scripts/validate_docs.py` → PASS.
- `git diff --check` → PASS, only CRLF normalization warnings.

## Known Validation Notes

- `cd frontend; npm run format` over the entire frontend still fails because 153 existing files are not aligned with the current Prettier baseline. This WI formatted and checked only the changed frontend files to avoid unrelated churn.
- `frontend/tsconfig.tsbuildinfo` was modified by TypeScript/build checks and restored because it is a generated cache artifact.

## Risks / Rollback

- Risk: Existing DBs need the manual DDL patch before running with schema validation against the new entity/table.
- Risk: Historical certification rows cannot automatically reconstruct per-file metadata if only legacy `document_path` exists.
- Rollback:
  - Revert this WI commit.
  - If the manual DDL was applied later, drop `company_certification_documents` only after confirming no newer data depends on it.
  - Restore the previous API spec / DB schema / UI docs from the reverted commit.

## Follow-ups

- Apply `src/main/resources/db/manual/20260618_company_certification_documents.sql` to the local test DB only after explicit approval.
- Consider future policies for approval revocation, certification expiry, external business registry verification, and automated notification.

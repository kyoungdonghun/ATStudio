# Evidence Pack: WI-20260714-ATS-010

## Summary (one-liner)
- Implemented enterprise certification PDF/JPEG/PNG verification, private quarantine storage, attachment-only ADMIN download, response path redaction, and focused tests.

## Scope / DoD Check
- [x] PDF must start with `%PDF-` at byte zero and end with `%%EOF` after whitespace only.
- [x] JPEG/PNG certification images reuse the WI-009 canonical image pipeline and store canonical JPEG bytes.
- [x] HWP/HWPX/DOC/DOCX and extension/MIME/signature mismatches are rejected in this baseline.
- [x] Per-file, count, path-like filename, and 50 MiB aggregate bounds fail before DB mutation.
- [x] New certification documents are stored through `StorageMutationCoordinator` under `StorageRoot.PRIVATE`.
- [x] ADMIN document download loads only the parent/child-owned private key and returns attachment-only safe headers.
- [x] API response redacts `documentPath`; static `/uploads/company-docs/**` is denied.
- [x] Legacy rows/files were not migrated, deleted, or backfilled.

## Reference Documents (Tier 0-2)

**Injected Context**:

| Tier | Document | Reason |
|------|----------|--------|
| 0 | `docs/standards/core-principles.md` | Constitution and traceability rules |
| 0 | `docs/standards/development-standards.md` | Java/Spring implementation and testing standards |
| 1 | `docs/policies/security-policy.md` | Sensitive data, protected resource, and logging boundaries |
| 1 | `docs/policies/access-control-policy.md` | Least privilege and default deny policy |
| 2 | `deliverables/user/REQ-20260714-ATS-001.md` | Approved P1 remediation scope |
| 2 | `docs/design/p1-security-acceptance-hardening-design.md` | Section 4 certification quarantine contract |
| 2 | `deliverables/agent/WI-20260714-ATS-012-evidence-pack.md` | Storage coordinator/private root dependency |
| 2 | `deliverables/agent/WI-20260714-ATS-009-evidence-pack.md` | Canonical image pipeline dependency |

**Injection Rules Applied**:
- Rule source: `deliverables/agent/WI-20260714-ATS-010-handoff.md`
- Assignee: `se`
- Task type: implementation/security
- agent_required_tiers: `[0, 1]`

## Evidence Pointers
- Files changed:
  - `src/main/java/com/atstudio/atstudio/service/CompanyCertificationService.java` - certification validation, PRIVATE storage, private load, PDF/image verification, aggregate bound.
  - `src/main/java/com/atstudio/atstudio/controller/CompanyCertificationController.java` - attachment-only byte response and safe headers.
  - `src/main/java/com/atstudio/atstudio/dto/certification/CompanyCertificationResponse.java` - `documentPath` redaction.
  - `src/main/java/com/atstudio/atstudio/dto/certification/CompanyCertificationDocumentDownload.java` - removed stored MIME from download contract.
  - `src/main/java/com/atstudio/atstudio/config/SecurityConfig.java` - deny static `/uploads/company-docs/**`.
  - `src/main/java/com/atstudio/atstudio/common/validation/ValidationConstants.java` - PDF/JPEG/PNG-only baseline and 50 MiB aggregate limit.
  - `src/test/java/com/atstudio/atstudio/service/CompanyCertificationServiceTest.java` - focused service validation/private-storage tests.
  - `src/test/java/com/atstudio/atstudio/controller/CompanyCertificationControllerTest.java` - attachment header/controller security test update.
  - `deliverables/user/WI-20260714-ATS-010-summary.md` - user-facing summary.
  - `deliverables/agent/WI-20260714-ATS-010-evidence-pack.md` - this evidence pack.

- Key locations:
  - `src/main/java/com/atstudio/atstudio/service/CompanyCertificationService.java:242` - document list/count/aggregate validation entry.
  - `src/main/java/com/atstudio/atstudio/service/CompanyCertificationService.java:306` - per-document verification before storage.
  - `src/main/java/com/atstudio/atstudio/service/CompanyCertificationService.java:335` - image canonicalizer reuse.
  - `src/main/java/com/atstudio/atstudio/service/CompanyCertificationService.java:359` - signature-based format detection.
  - `src/main/java/com/atstudio/atstudio/service/CompanyCertificationService.java:379` - PDF `%%EOF` trailing-payload rejection.
  - `src/main/java/com/atstudio/atstudio/service/CompanyCertificationService.java:271` - `StorageRoot.PRIVATE` coordinator store.
  - `src/main/java/com/atstudio/atstudio/service/CompanyCertificationService.java:206` - PRIVATE document load after parent/child lookup.
  - `src/main/java/com/atstudio/atstudio/controller/CompanyCertificationController.java:101` - ADMIN attachment-only download endpoint.
  - `src/main/java/com/atstudio/atstudio/config/SecurityConfig.java:84` - static company-doc upload path denial.
  - `src/main/java/com/atstudio/atstudio/dto/certification/CompanyCertificationResponse.java:34` - response path redaction.

## Commands & Outputs
- Commands executed:
  - `.\gradlew.bat compileTestJava`
  - `.\gradlew.bat test --tests "com.atstudio.atstudio.service.CompanyCertificationServiceTest" --tests "com.atstudio.atstudio.controller.CompanyCertificationControllerTest"`
  - `.\gradlew.bat test`
  - `git diff --check -- src/main/java/com/atstudio/atstudio/common/validation/ValidationConstants.java src/main/java/com/atstudio/atstudio/config/SecurityConfig.java src/main/java/com/atstudio/atstudio/controller/CompanyCertificationController.java src/main/java/com/atstudio/atstudio/dto/certification/CompanyCertificationDocumentDownload.java src/main/java/com/atstudio/atstudio/dto/certification/CompanyCertificationResponse.java src/main/java/com/atstudio/atstudio/service/CompanyCertificationService.java src/test/java/com/atstudio/atstudio/controller/CompanyCertificationControllerTest.java src/test/java/com/atstudio/atstudio/service/CompanyCertificationServiceTest.java`
- Outputs:
  - `compileTestJava`: PASS, `BUILD SUCCESSFUL`.
  - Focused service/controller tests: PASS, `BUILD SUCCESSFUL`.
  - Full backend tests: PASS, `BUILD SUCCESSFUL`.
  - `git diff --check`: PASS; only CRLF normalization warnings for tracked files.

## Tests
- `src/test/java/com/atstudio/atstudio/service/CompanyCertificationServiceTest.java:96` - response DTO `documentPath` is null.
- `src/test/java/com/atstudio/atstudio/service/CompanyCertificationServiceTest.java:234` - PNG certification image uses canonicalizer.
- `src/test/java/com/atstudio/atstudio/service/CompanyCertificationServiceTest.java:258` - PDF trailing payload rejected before storage/save.
- `src/test/java/com/atstudio/atstudio/service/CompanyCertificationServiceTest.java:277` - path-like filename rejected before storage/save.
- `src/test/java/com/atstudio/atstudio/service/CompanyCertificationServiceTest.java:296` - HWP/HWPX/DOC/DOCX rejected.
- `src/test/java/com/atstudio/atstudio/service/CompanyCertificationServiceTest.java:316` - aggregate overflow rejected before storage/save.
- `src/test/java/com/atstudio/atstudio/controller/CompanyCertificationControllerTest.java:209` - ADMIN download returns attachment-only safe headers.

## Risks / Rollback
- Risks:
  - Existing legacy public-root certification files are not migrated by this WI. They are not exposed through the new PRIVATE download boundary and require a separately approved operator/backfill plan.
  - PDF validation establishes format plausibility only; it is not a malware-clean verdict.
  - Images are canonicalized to JPEG, so stored image MIME becomes `image/jpeg` even when the submitted format was PNG.
- Rollback:
  - Revert the WI-010 changes in the service/controller/DTO/security/constants/tests and the two WI-010 deliverables.
  - Disable new certification uploads before rollback if PRIVATE quarantine behavior has been exposed.
  - Do not delete private/public certification files or mutate existing DB rows during rollback without separate approval.

## Follow-ups
- WI-019/WI-024 should independently review security file/session tests and confirm no legacy public-file exposure path remains.
- Legacy certification migration/backfill remains out of scope and requires separate approval.

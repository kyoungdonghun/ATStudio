# Evidence Pack: WI-20260714-ATS-037

## Summary (one-liner)

- Moved new Question attachment lifecycle operations to PRIVATE storage, denied legacy static paths, hardened authorized downloads, and removed account identifiers from bootstrap logs.

## Scope / DoD Check

- [x] `saveAttachments`, authorized load, and after-commit deletion use `StorageRoot.PRIVATE`.
- [x] `/uploads/questions/**` is denied for anonymous, USER, ADMIN, and an encoded traversal variant.
- [x] Existing public-question and private owner/admin visibility decisions remain unchanged.
- [x] Authorized downloads are attachment-only, octet-stream, no-store, nosniff, sandboxed, and non-range responses.
- [x] CR/LF and punctuation in an original filename cannot inject response headers.
- [x] Question response DTOs do not expose `filePath`.
- [x] Bootstrap success and skip logs contain no email/account identifier.
- [x] Focused service/controller/security/bootstrap tests and scoped diff checks pass.
- [x] No legacy migration/deletion, DB access, schema change, or private-content inspection occurred.

## Reference Documents (Tier 0-2)

**Injected Context**:

| Tier | Document | Reason |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Constitution, private track, approval, traceability |
| 0 | `docs/standards/development-standards.md` | Java/Spring implementation and focused testing |
| 0 | `docs/standards/documentation-standards.md` | Deliverable structure and pointer requirements |
| 0 | `docs/standards/glossary.md` | Canonical WI and security terminology |
| 1 | `docs/policies/security-policy.md` | PII logging minimization and protected-resource controls |
| 1 | `docs/policies/access-control-policy.md` | Least privilege and default deny |
| 1 | `docs/policies/quality-gates.md` | Focused regression and evidence requirements |
| REQ | `deliverables/user/REQ-20260714-ATS-001.md` | Approved P1 remediation scope and constraints |
| Design | `docs/design/p1-security-acceptance-hardening-design.md` | Private storage, read boundary, and download headers |
| Prior WI | `deliverables/agent/WI-20260714-ATS-010-evidence-pack.md` | Company Certification PRIVATE/header pattern |
| Prior WI | `deliverables/agent/WI-20260714-ATS-012-evidence-pack.md` | Storage mutation coordinator/journal dependency |
| Prior WI | `deliverables/agent/WI-20260714-ATS-019-evidence-pack.md` | Focused security verification pattern |
| Review | `deliverables/agent/WI-20260714-ATS-024-evidence-pack.md` | PG-024-01 and PG-024-02 findings |

**Injection Rules Applied**:

- Rule source: `deliverables/agent/WI-20260714-ATS-037-handoff.md`
- Assignee: `se`
- Task type: implementation/security
- Required context: Tier 0 + security/access/quality policies + listed prior evidence

## Finding-to-Code Mapping

| Finding | Remediation | Evidence |
|---|---|---|
| `PG-024-01` public active-content exposure | New write/load/delete operations use PRIVATE storage. | `QuestionService.java:154-163,190-195,222-229` |
| `PG-024-01` legacy same-origin path | Deny the complete legacy prefix before static fallback. | `SecurityConfig.java:85` |
| `PG-024-01` inline/header bypass | Byte response with attachment disposition, encoded filename, octet-stream, no-store, nosniff, sandbox, and no range. | `QuestionController.java:88-113` |
| `PG-024-01` stored path disclosure | Dedicated download DTO carries only resource and original filename; response DTO test proves no `filePath`. | `QuestionAttachmentDownload.java:5-8`; `QuestionControllerTest.java:72-88` |
| `PG-024-02` fixture email logging | Replace email fields with count/status and bounded reason codes. | `TestUserBootstrapRunner.java:138,166-196` |

## Legacy / New-File Behavior

| Case | Behavior |
|---|---|
| New Question attachment | Stored through `StorageMutationCoordinator` under `StorageRoot.PRIVATE` and `questions/attachments`. |
| Authorized new attachment API load | Parent Question access is checked first; the DB-owned key is loaded only from PRIVATE storage. |
| Question deletion | DB-owned attachment keys are scheduled for PRIVATE after-commit deletion through the existing journal pattern. |
| Legacy `/uploads/questions/**` request | Denied before static resource resolution for every role. |
| Legacy DB row pointing to a PUBLIC file | PRIVATE API load fails closed; no fallback to PUBLIC and no migration/backfill is attempted. |

## Authorization / Header Matrix

| Request | Expected result | Focused evidence |
|---|---|---|
| Anonymous API download | `401` | `QuestionControllerTest.java:154-159` |
| Authenticated user, public Question | Allowed by unchanged `checkReadAccess` policy | `QuestionServiceTest.java:352-378` |
| Owner, private Question | Allowed | `QuestionServiceTest.java:381-404` |
| ADMIN, private Question | Allowed | `QuestionServiceTest.java:406-429` |
| Other USER, private Question | `RESOURCE_NOT_ACCESS` | Existing focused test in `QuestionServiceTest` |
| Any role, `/uploads/questions/**` | `401` for anonymous, `403` for USER/ADMIN | `QuestionControllerTest.java:211-248` |

| Download control | Value |
|---|---|
| `Content-Type` | `application/octet-stream` |
| `Content-Disposition` | `attachment; filename*=UTF-8''<percent-encoded>` |
| `Cache-Control` | `no-store, private` |
| `Pragma` | `no-cache` |
| `X-Content-Type-Options` | `nosniff` |
| `Content-Security-Policy` | `default-src 'none'; sandbox` |
| `Accept-Ranges` | `none` |
| Incoming `Range` | Ignored; full body returns `200` |

## Evidence Pointers

- Production files:
  - `src/main/java/com/atstudio/atstudio/service/QuestionService.java:154-163` - unchanged authorization followed by PRIVATE load and safe download metadata.
  - `src/main/java/com/atstudio/atstudio/service/QuestionService.java:190-195` - PRIVATE after-commit delete.
  - `src/main/java/com/atstudio/atstudio/service/QuestionService.java:222-229` - PRIVATE coordinated writes.
  - `src/main/java/com/atstudio/atstudio/controller/QuestionController.java:88-113` - hardened byte response.
  - `src/main/java/com/atstudio/atstudio/dto/question/QuestionAttachmentDownload.java:5-8` - resource/original-filename-only contract.
  - `src/main/java/com/atstudio/atstudio/config/SecurityConfig.java:85` - legacy static deny.
  - `src/main/java/com/atstudio/atstudio/bootstrap/TestUserBootstrapRunner.java:138,166-196` - de-identified logs.
- Focused tests:
  - `src/test/java/com/atstudio/atstudio/service/QuestionServiceTest.java:98-128,352-429,557-582` - PRIVATE write/load/delete and unchanged public/owner/admin access.
  - `src/test/java/com/atstudio/atstudio/controller/QuestionControllerTest.java:72-88,154-248` - path redaction, safe active-content download, injected filename, Range, and static denial.
  - `src/test/java/com/atstudio/atstudio/bootstrap/TestUserBootstrapRunnerTest.java:57-95,149-207` - count/reason logs and email absence.
- Deliverables:
  - `deliverables/user/WI-20260714-ATS-037-summary.md`
  - `deliverables/agent/WI-20260714-ATS-037-evidence-pack.md`

## Commands & Outputs

- Focused tests:
  - `.\gradlew.bat test --tests "com.atstudio.atstudio.service.QuestionServiceTest" --tests "com.atstudio.atstudio.controller.QuestionControllerTest" --tests "com.atstudio.atstudio.bootstrap.TestUserBootstrapRunnerTest" --console=plain`
  - PASS: `BUILD SUCCESSFUL in 22s`.
  - Result files: 51 tests, 0 failures, 0 errors, 0 skipped.
- Scoped tracked-file whitespace check:
  - `git diff --check -- <seven tracked WI-037 source/test paths>`
  - PASS: no whitespace errors; only existing Windows LF/CRLF normalization warnings.
- New DTO whitespace check:
  - `Select-String -Path ...QuestionAttachmentDownload.java -Pattern '[ \t]+$'`
  - PASS: no matches.
- Scoped content check:
  - `git diff --unified=2 -- <seven tracked WI-037 source/test paths>`
  - PASS: WI-037 hunks are limited to the requested behavior. Pre-existing acceptance, logout, Company Certification, storage-journal, and bootstrap-order changes in shared files were preserved.
- Test count extraction:
  - Strict PowerShell XML parsing was not usable because pre-existing corrupted `@DisplayName` text makes the generated XML attributes malformed.
  - Regex extraction of the numeric suite attributes succeeded and reported 51/0/0/0.

## No-DB / No-Migration Proof

- No database command, SQL script, schema change, application server, or live endpoint was invoked.
- No upload/private directory was enumerated and no private file body was read.
- No file move, copy, migration, backfill, or deletion command was executed.
- Storage deletion was verified only through Mockito interaction; no real storage service was invoked.
- Existing payment, design, acceptance, runtime-log, and unrelated worktree changes were neither reverted nor reformatted.

## Risks / Rollback

- Risks:
  - Legacy PUBLIC Question attachments are intentionally unavailable through both static and new PRIVATE API paths until a separately approved migration exists.
  - Malware scanning and attachment format restrictions remain out of scope. The implemented boundary prevents same-origin inline rendering but does not classify file contents as clean.
  - The hardened controller buffers the authorized attachment into memory, matching the approved Company Certification response pattern.
  - Full-suite coverage was intentionally not run per handoff/user instruction.
- Rollback:
  - Disable new Question attachment upload/download before reverting the private-storage and response protections.
  - Revert only WI-037 hunks in shared files; do not restore entire files because they contain concurrent acceptance, session, storage-journal, and bootstrap changes.
  - Remove `QuestionAttachmentDownload.java`, the WI-037 test additions, and the two WI-037 deliverables.
  - Do not move/delete legacy files or mutate DB rows during rollback.

## Follow-ups

- WI-025/WI-027/WI-034 can consume this packet for integration review, client checklist updates, and final evidence aggregation.
- Any legacy Question attachment migration or malware-scanning work requires a separate approved REQ/WI.

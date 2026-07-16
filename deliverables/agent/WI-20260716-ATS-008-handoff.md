[WI HEADER]
WI ID: WI-20260716-ATS-008
REQ: REQ-20260716-ATS-002
Agent: se
Depends On: WI-20260716-ATS-004
Blocks: WI-20260716-ATS-010, WI-20260716-ATS-012, WI-20260716-ATS-013, WI-20260716-ATS-015, WI-20260716-ATS-016

[WI SUMMARY]
Why: Close the remaining company-certification validation, concurrency, accountability, and sensitive-metadata gaps without inventing an unapproved retention duration or external scanning architecture.
Scope (in):
- Serialize application, resubmission, and review mutations with a deterministic owning-user then certification aggregate lock contract; prevent duplicate open applications and last-writer-wins review/resubmission outcomes under cooperating writes.
- Add optimistic versioning where it complements row locks and align fresh schema plus an additive retained-DB manual patch source. Do not execute DDL.
- Require a trimmed, bounded applicant-visible reason for `REVISION_REQUESTED` and `REJECTED`; keep approval note optional but bounded. Reject invalid transitions before partial status/audit mutation.
- Persist minimum certification audit evidence for reviewer identity, review timestamp/from-to status, and guarded document-download access without storing file contents, document bytes, tokens, or raw request data in audit rows/logs.
- Reject null/empty mixed multipart parts instead of silently dropping them; align per-file, aggregate, count, original-filename length, extension, signature, canonical-image MIME, and frontend request-size guidance. Correct the confirmed PNG stored-MIME defect (`image/png`, not `image/jpeg`).
- Keep certification documents in private storage and guarded attachment delivery. Remove the legacy directory hint field from API/frontend response contracts while preserving opaque document IDs and legacy DB compatibility.
- Add a frontend BUSINESS user-type route/page gate for apply/status while retaining backend authorization as the source of truth. Align frontend allowed extensions with the backend's actual PDF/JPG/JPEG/PNG contract and enforce the 50 MB aggregate selection limit.
- Align backend/frontend tests, schema/manual patch sources, and canonical API/DB/use-case/security/UI documentation.
- Record retention as `POLICY-PENDING`: no automatic purge, duration, withdrawal deletion, or scheduler is authorized by this WI. Existing data is preserved until a separate approved policy names duration, owner, legal/operational basis, and failed-delete handling.
Scope (out):
- OCR, automatic approval, business-registry integration, live DB migration/backfill, production data mutation, provider calls, multi-server locks, client-demo propagation, or external malware-scanner/library selection.
- Deleting existing certification records/documents, choosing a legal retention period, claiming malware-free PDFs from signature checks alone, or reconstructing legacy per-file metadata that does not exist.
DoD:
- Review reasons, transition legality, duplicate-open prevention, review/resubmit concurrency, audit evidence, file contract, PNG MIME, response minimization, and BUSINESS UI gate are covered by focused automated tests.
- Canonical docs distinguish implemented controls from retention/scanner/retained-DB environment conditions.
- Relevant backend/frontend quality gates, docs validation, and diff check pass.
Constraints/Forbidden:
- Work only in `codex/p1-acceptance-hardening` under `C:\Users\jm991\Desktop\project\ATStudio`.
- Do not modify, switch, merge, restart, or propagate to `codex/client-demo-stable` or Cloudflare runtime.
- Do not execute DDL, inspect/mutate real certification files or users, expose filenames/paths/PII in logs/evidence, or add a new scanner dependency without approval.
- Preserve WI-005 through WI-009 and unrelated dirty-worktree edits; shared docs must be merged, not overwritten.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Concurrent applications for one user cannot create two open certifications under the cooperating-write contract.
- [ ] Resubmission and review acquire deterministic locks and stale/concurrent decisions cannot silently overwrite one another.
- [ ] `REVISION_REQUESTED`/`REJECTED` require a nonblank bounded reason; `APPROVED` may omit a note; invalid state transitions leave status/audit evidence unchanged.
- [ ] Review and document-download access record actor/time/action/minimum target evidence without document contents or storage paths.
- [ ] Any null/empty multipart part rejects the request; total count and aggregate size are enforced identically on backend and frontend.
- [ ] Backend/frontend accept only PDF/JPG/JPEG/PNG; signature/extension/MIME validation remains coherent and PNG canonical metadata is `image/png`.
- [ ] API responses omit the legacy `documentPath` directory hint and continue to use opaque document IDs for guarded downloads.
- [ ] INDIVIDUAL/ADMIN users cannot use BUSINESS apply/status UI routes; backend still rejects non-BUSINESS writes.
- [ ] No automatic retention deletion is introduced; docs clearly mark the policy and scanner dependencies pending.
Performance:
- [ ] Admin list page size is bounded and audit writes are narrow/indexed; no full-table or file-content audit storage is introduced.
- [ ] Lock order is documented and consistent to avoid user/certification deadlocks.
Quality:
- [ ] Focused certification service/controller/repository/schema/security tests pass, including concurrent/lock contract proofs.
- [ ] Affected frontend Vitest, typecheck, ESLint, build, and changed-file Prettier checks pass.
- [ ] Documentation validation and `git diff --check` pass.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md
- docs/standards/development-standards.md
- docs/standards/documentation-standards.md
- docs/standards/glossary.md

Tier 1 (Policies):
- docs/policies/security-policy.md
- docs/policies/quality-gates.md
- docs/policies/access-control-policy.md

Tier 2 (Domain / Approved Design):
- docs/design/remaining-remediation-design-20260716.md
- docs/design/api-spec.md
- docs/design/db-schema.md
- docs/design/usecase/company-certification.md
- docs/ui/screen-flow.md
- deliverables/user/REQ-20260716-ATS-002.md
- deliverables/agent/WI-20260711-ATS-007-evidence-pack.md
- deliverables/user/WI-20260618-ATS-001-summary.md
- deliverables/agent/WI-20260618-ATS-001-evidence-pack.md

Files:
- src/main/java/com/atstudio/atstudio/entity/CompanyCertification.java
- src/main/java/com/atstudio/atstudio/entity/CompanyCertificationDocument.java
- src/main/java/com/atstudio/atstudio/entity/enums/CompanyCertificationStatus.java
- src/main/java/com/atstudio/atstudio/repository/CompanyCertificationRepository.java
- src/main/java/com/atstudio/atstudio/repository/CompanyCertificationDocumentRepository.java
- src/main/java/com/atstudio/atstudio/service/CompanyCertificationService.java
- src/main/java/com/atstudio/atstudio/controller/CompanyCertificationController.java
- src/main/java/com/atstudio/atstudio/dto/certification/
- src/main/java/com/atstudio/atstudio/common/validation/ValidationConstants.java
- src/main/java/com/atstudio/atstudio/config/SecurityConfig.java
- frontend/src/router/
- frontend/src/pages/subscriber/CompanyCertApplyPage.tsx
- frontend/src/pages/subscriber/CompanyCertStatusPage.tsx
- frontend/src/pages/admin/CompanyCertManagePage.tsx
- frontend/src/utils/validation.ts
- frontend/src/api/companyCerts.ts
- frontend/src/api/admin.ts
- src/main/resources/schema.sql
- src/main/resources/db/manual/
- src/test/java/com/atstudio/atstudio/service/CompanyCertificationServiceTest.java
- src/test/java/com/atstudio/atstudio/controller/CompanyCertificationControllerTest.java

Repro / Inspection:
- `rg -n "CompanyCertification|adminNote|documentPath|review|downloadDocument|CERT_DOC|PNG|findByIdForUpdate|@Version" src/main/java src/test/java frontend/src docs`
- Inspect existing `20260618_company_certification_documents.sql`, fresh schema, audit-ledger naming patterns, and current security route guards before adding new source-only schema.

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260716-ATS-008-summary.md:
- Behavior changes, user/admin effects, unchanged retention/scanner boundary, tests, risks, and environment follow-ups.
Agent-facing -> deliverables/agent/WI-20260716-ATS-008-evidence-pack.md:
- Transition/lock/audit/file-contract tables, exact pointers, commands/results, schema/manual-patch notes, rollback, and policy/environment conditions.
Handoff Packet -> deliverables/agent/WI-20260716-ATS-008-handoff.md:
- This packet.

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required.
Tests: Include exact backend/frontend commands and result counts. Static lock annotations alone are not proof of the cooperating-write contract.
Rollback: Revert code/schema/manual-patch/docs together without deleting existing certification/audit/document rows or private files.
Environment boundary: Retained-MySQL migration/backfill, actual scanner coverage, and retention execution remain `ENVIRONMENT-CONDITIONAL` or `POLICY-PENDING` unless separately approved and proven.

[SUPERSEDED CORRECTION - 2026-07-16]
- The earlier scope/acceptance wording that describes PNG stored MIME as `image/png` is superseded. PNG is accepted and verified as input `image/png`; the existing `CanonicalImageService` decodes it and stores canonical JPEG output with `image/jpeg`.
- `DOCUMENT_ACCESS_GRANTED` means ADMIN authorization and private-resource resolution succeeded. It is not evidence that controller byte streaming completed.

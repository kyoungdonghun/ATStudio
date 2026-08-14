---
version: 1.0
last_updated: 2026-08-14
project: ATS
owner: ma
category: wi-handoff
status: active
dependencies:
  - path: ../user/REQ-20260809-ATS-001.md
    reason: Approved correction authority and autonomous execution gate
  - path: WI-20260809-ATS-031-consolidated-findings.md
    reason: Canonical findings, bounds, dependencies, and sequencing
  - path: WI-20260809-ATS-029-findings.md
    reason: Binary envelope, duplicate-request, and private-streaming evidence
  - path: WI-20260809-ATS-030-findings.md
    reason: Blob-aware Track error-normalization evidence
  - path: WI-20260809-ATS-039-evidence-pack.md
    reason: Existing storage-serving and upload-containment constraints
---

# WI Handoff: WI-20260809-ATS-055

## WI Header

- **WI ID:** `WI-20260809-ATS-055`
- **REQ:** `REQ-20260809-ATS-001`
- **Agent:** `se`
- **Depends On:** `WI-20260809-ATS-039`
- **Blocks:** `WI-20260809-ATS-065`, `WI-20260809-ATS-071`
- **Canonical findings:** `CR-031-107`, `CR-031-108`, `CR-031-110`,
  `CR-031-127`

## WI Summary

### Why

Make the existing binary-download contract consistent and recoverable across
clients, prevent duplicate requests from the same visible download action, and
deliver private Question and Company Certification files without buffering the
entire payload in heap.

### Scope In

- Introduce one shared frontend binary response shape containing the non-empty
  Blob, sanitized filename, and response content type.
- Parse both RFC 5987 `filename*` and basic `filename` Content-Disposition
  values. Prefer a safe response filename; use a deterministic bounded fallback
  derived from stable Track/attachment identity and validated metadata.
- Reject zero-byte payloads as download failures before creating an object URL.
- Preserve server content type when it is a valid binary type and do not force
  every Track filename to `.mp3` when the response proves another extension.
- Route Track-download failures through the existing Blob-aware API error-code
  normalization rather than caller-specific Blob parsing or generic messages.
- Add same-action pending fences to named Track download entry points that can
  currently issue duplicate parallel requests: Track list, License list, Like
  list, Playlist detail, and any other inspected Track caller lacking an
  equivalent synchronous/per-identity fence. Preserve existing PlayerBar,
  Track-detail, and Download-history ownership where already sufficient.
- Return private Question attachment and Company Certification document
  resources as streaming `Resource` responses while retaining authorization,
  no-store/private caching, nosniff, sandbox CSP, disabled ranges, encoded
  original filename, and existing audit timing.
- Add backend controller and frontend API/page tests for headers, fallback,
  sanitization, nonempty bytes, zero-byte rejection, Blob error extraction,
  duplicate clicks, resource streaming, and existing security headers.
- Synchronize binary/download contract documentation with verified behavior.

### Scope Out

- `CR-031-106`: whether durable entitlement grant or completed byte transfer is
  the product definition of successful first download.
- `CR-031-111`: server/client bulk download ceiling.
- Route-outliving operation ownership or cancellation policy gated by
  `UG-031-019`; this WI only fences duplicate invocation while the owning action
  remains mounted.
- Range support, resumable downloads, storage journal recovery, MIME allowlist
  policy, upload limits, provider/payment/mail/export effects, schema/data,
  dependencies, deployment, or private production/user files.
- Any new Track entitlement, License, count, or audit semantics.

### Definition of Done

- All in-scope frontend binary callers consume one canonical validated result;
  filenames and content types no longer depend on inconsistent caller guesses.
- Empty binary responses never produce a browser download or success state.
- Blob JSON failures expose the canonical API error code/message path.
- Repeated activation of one pending Track download action issues at most one
  request for that identity.
- Question and Company Certification private files are returned as `Resource`
  responses without `copyToByteArray`, while all existing authorization and
  hardened response headers remain covered.
- Focused RED/GREEN tests, independent PG and QA-INTEG reviews, full frontend
  and backend quality gates, docs validation, diff check, Evidence Pack, and
  user summary pass.

## Constraints / Forbidden

- Do not inspect, open, hash, modify, stage, or delete
  `output/client-demo-screenshots-20260716-140514.zip` or `output/ui-ux-audit/`.
- Do not inspect ignored secrets or local environment values.
- Do not use real private/user files; tests must use synthetic in-memory or
  temporary safe resources only.
- Do not execute real browser downloads, provider/payment/refund/mail/export,
  database-data, or other external effects.
- Do not decide held success/bulk/route-lifetime policy, change authorization,
  add dependencies, change schema/data, merge/delete branches, or deploy.
- Preserve Question ownership visibility and Company Certification ADMIN-only
  authorization. Preserve existing Company Certification access-grant audit
  semantics; do not claim it proves byte completion.
- Use existing Spring `Resource` response patterns and existing frontend API
  error normalization; avoid a second binary/download abstraction.

## Acceptance Criteria

### Functional

- [ ] A canonical binary helper returns non-empty Blob, sanitized filename, and
      content type using response headers with deterministic safe fallbacks.
- [ ] RFC 5987 and quoted/basic disposition forms are decoded safely; path
      separators, control characters, blank names, and unsafe traversal tokens
      cannot become the browser download name.
- [ ] Zero-byte payloads reject and create no object URL/anchor action.
- [ ] Track, Notice, Question, and Company Certification download clients use
      the canonical binary contract where applicable without losing abort
      support or existing latest-request ownership.
- [ ] Track download callers present Blob JSON error codes through the canonical
      normalization and do not keep per-caller Blob parsing.
- [ ] Repeated activation while a same-identity download is pending issues one
      frontend request and restores availability after success or failure.
- [ ] Question and Company Certification private responses stream a `Resource`
      and retain filename, cache, nosniff, CSP, and no-range headers.

### Security and Durable-State Safety

- [ ] Existing backend authorization and ownership checks remain authoritative.
- [ ] No entire private file is copied into an intermediate controller byte array.
- [ ] No private bytes, unsafe original filenames, or local paths enter logs,
      fixtures, evidence, or output artifacts.
- [ ] Existing download License/count/history and certification audit mutation
      timing remains unchanged.
- [ ] No external side effect is run during verification.

### Quality

- [ ] Focused frontend and backend tests cover all named behaviors and callers.
- [ ] Independent PG review reports no open P0-P3 security/privacy finding.
- [ ] Independent QA-INTEG review reports no open P0-P3 integration finding.
- [ ] Full backend test/coverage/build and frontend coverage/typecheck/ESLint/
      Prettier/build pass.
- [ ] Documentation validation and `git diff --check` pass.

## Input Pointers

### Tier 0

- `docs/standards/core-principles.md`
- `docs/standards/development-standards.md`

### Tier 1

- `docs/policies/security-policy.md`
- `docs/policies/access-control-policy.md`
- `docs/policies/quality-gates.md`

### Tier 2

- `docs/standards/frontend-standards.md`
- `.agents/skills/react-best-practices/AGENTS.md`
- `docs/design/api-spec.md`
- `docs/design/usecase/user-license.md`
- `docs/design/usecase/user-question.md`
- `docs/design/usecase/company-certification.md`
- `docs/design/usecase/download-queue.md`

### REQ and Evidence

- `deliverables/user/REQ-20260809-ATS-001.md`
- `deliverables/agent/WI-20260809-ATS-031-consolidated-findings.md`
- `deliverables/agent/WI-20260809-ATS-029-findings.md`
- `deliverables/agent/WI-20260809-ATS-030-findings.md`
- `deliverables/agent/WI-20260809-ATS-039-evidence-pack.md`
- `deliverables/agent/WI-20260809-ATS-047-evidence-pack.md`
- `deliverables/agent/WI-20260809-ATS-050-evidence-pack.md`

### Primary Backend Files

- `src/main/java/com/atstudio/atstudio/controller/QuestionController.java`
- `src/main/java/com/atstudio/atstudio/controller/CompanyCertificationController.java`
- `src/main/java/com/atstudio/atstudio/controller/TrackController.java`
- `src/main/java/com/atstudio/atstudio/dto/question/QuestionAttachmentDownload.java`
- `src/main/java/com/atstudio/atstudio/dto/certification/CompanyCertificationDocumentDownload.java`
- `src/test/java/com/atstudio/atstudio/controller/QuestionControllerTest.java`
- `src/test/java/com/atstudio/atstudio/controller/CompanyCertificationControllerTest.java`

### Primary Frontend Files

- `frontend/src/api/client.ts`
- `frontend/src/api/downloads.ts`
- `frontend/src/api/notices.ts`
- `frontend/src/api/questions.ts`
- `frontend/src/api/admin.ts`
- `frontend/src/layouts/PlayerBar.tsx`
- `frontend/src/pages/public/TrackDetailPage.tsx`
- `frontend/src/pages/public/TrackListPage.tsx`
- `frontend/src/pages/subscriber/LicenseListPage.tsx`
- `frontend/src/pages/subscriber/LikeListPage.tsx`
- `frontend/src/pages/subscriber/PlaylistDetailPage.tsx`
- `frontend/src/pages/subscriber/DownloadHistoryPage.tsx`
- Corresponding focused API/page/controller tests.

## Output Contract

- User-facing: `deliverables/user/WI-20260809-ATS-055-summary.md`
- Agent-facing: `deliverables/agent/WI-20260809-ATS-055-evidence-pack.md`
- Handoff: this file
- Independent security result: `deliverables/agent/WI-20260809-ATS-055-pg-result.md`
- Independent integration result: `deliverables/agent/WI-20260809-ATS-055-qa-integ-result.md`

## Traceability Requirements

- Separate visible UI state, frontend invocation, HTTP headers/body, backend
  authorization, and durable-state implications.
- Record exact filename/content-type/byte/error cases and same-action invocation
  counts.
- Record commands and pass/fail counts, including any remediation run.
- State explicitly that held completion/bulk/route-lifetime policies were not
  decided and no real private file or external effect was used.
- Document rollback as file/commit reversion with no provider/data rollback.

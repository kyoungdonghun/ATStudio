# Evidence Pack: WI-20260809-ATS-005

## Summary (one-liner)

- Completed a frontend/API-contract independent review of administrator role controls and local subscription correction, finding two MAJOR correctness defects and two MINOR contract/transparency defects.

## Findings

### MAJOR-001 - Ambiguous mutation outcomes are not reconciled with server state

- **Evidence pointers:**
  - `frontend/src/pages/admin/UserSubscriptionCorrectionModal.tsx:408-435` - request failure leaves `correction` unset and the prior preview/request action available.
  - `frontend/src/pages/admin/UserSubscriptionCorrectionModal.tsx:437-463` - approval failure leaves the local correction at `REQUESTED` without a detail refresh.
  - `frontend/src/pages/admin/UserSubscriptionCorrectionModal.tsx:465-501` - execution failure says to check status but leaves the local correction at `APPROVED` and exposes no status-check action.
  - `frontend/src/api/userSubscriptions.ts:219-241` - detail and open-state reads already exist and can support reconciliation.
  - `frontend/src/pages/admin/UserSubscriptionManagePage.test.tsx:598-686` - failure tests assert local preservation only; they do not model commit-before-response-loss or a reconciliation read.
- **Exact impact:** The server can advance or finish a correction while the UI remains one stage behind. A stale request/approval can be retried, and a terminal execution outcome is not recoverable through the non-terminal open lookup after close/reopen.
- **Reasoning:** Transport rejection is an unknown outcome, not proof of rollback. Backend duplicate/idempotency controls reduce mutation risk but do not make the displayed stage truthful.
- **Missing test:** Mutation commits, response rejects, then open/detail returns `REQUESTED`, `APPROVED`, `PROCESSING`, `SUCCEEDED`, or `FAILED`; UI reconciles without a duplicate transition.
- **Recommended repair:** Re-read open state after ambiguous request failures and correction detail after ambiguous approval/execution failures. Preserve ID/draft/notes and add a retryable status gate when the follow-up read also fails.

### MAJOR-002 - Browser time blocks requests governed by the server business date

- **Evidence pointers:**
  - `frontend/src/pages/admin/UserSubscriptionCorrectionModal.tsx:94-103` - browser `new Date()` supplies Seoul `today`.
  - `frontend/src/pages/admin/UserSubscriptionCorrectionModal.tsx:349-377` - browser-derived date validation blocks request construction.
  - `frontend/src/pages/admin/UserSubscriptionCorrectionModal.tsx:654-667` - HTML `min`/`max` bounds enforce the browser-derived date.
  - `docs/SR/SR-97.md:31` and `deliverables/user/REQ-20260808-ATS-004.md:37` - approved date authority is the Asia/Seoul server Clock; frontend validation is advisory.
- **Exact impact:** Client clock skew or a date-boundary session can prevent a server-valid correction from reaching preview.
- **Reasoning:** The backend cannot remain the source of truth when the frontend suppresses the request before server validation.
- **Missing test:** Fake browser time differs from the server business date and a boundary-date payload is still submitted to preview.
- **Recommended repair:** Remove blocking client-clock validation or obtain authoritative server business-date metadata; keep local messaging advisory and server preview decisive.

### MINOR-001 - User-management TypeScript types do not match the controller DTOs

- **Evidence pointers:**
  - `frontend/src/api/admin.ts:36-52` - list and update functions use shared `User`/`UserRole` contracts.
  - `frontend/src/types/index.ts:31-53` - `UserRole` includes frontend-only `GUEST`; `User` requires full profile fields.
  - `src/main/java/com/atstudio/atstudio/dto/user/UserListItemResponse.java:9-16` - list rows omit phone/job/company profile fields.
  - `src/main/java/com/atstudio/atstudio/dto/user/UserAdminUpdateRequest.java:12-18` - the backend accepts its `USER`/`ADMIN` enum, not frontend `GUEST`.
  - `frontend/src/pages/admin/UserManagePage.test.tsx:46-74` - list mocks use a full `User`, masking the narrower backend list shape.
- **Exact impact:** Callers can compile invalid role payloads and assume absent list fields exist. Current rendering uses only common fields and was not observed to fail.
- **Reasoning:** Session-domain and wire-contract types have different valid values and field sets.
- **Missing test:** Backend-realistic list fixture plus a type-level exclusion of `GUEST` from admin mutation bodies.
- **Recommended repair:** Define endpoint-specific list/detail/update types and an `AdminAssignableRole` union.

### MINOR-002 - Normalized text fields are sent without an exact visible confirmation

- **Evidence pointers:**
  - `frontend/src/pages/admin/UserSubscriptionCorrectionModal.tsx:365-376` - `reasonNote` is trimmed for preview/request.
  - `frontend/src/pages/admin/UserSubscriptionCorrectionModal.tsx:437-475` - approval/execution notes are trimmed before sending.
  - `frontend/src/pages/admin/UserSubscriptionCorrectionModal.tsx:707-780` - raw controlled reason remains visible, while the preview omits the normalized reason.
  - `frontend/src/pages/admin/UserSubscriptionCorrectionModal.tsx:788-819,893-900` - raw note inputs remain in state and confirmation text omits their normalized payload values.
  - `frontend/src/pages/admin/UserSubscriptionManagePage.test.tsx:206-222,535-590` - tests prove trimmed API values but not visible equality.
- **Exact impact:** The operator cannot confirm the exact text persisted in correction/audit records. Entitlement-state preview values remain aligned.
- **Reasoning:** Free text is approved; only the concrete raw-display versus trimmed-payload difference is at issue.
- **Missing test:** Exact normalized reason/approval/execution note is visible immediately before each call.
- **Recommended repair:** Normalize controlled state before the preview/confirmation step or render normalized values and correction identity in those surfaces.

## Scope / DoD Check

- [x] Reviewed the WI-005 targeted diff/current code and only related wire types/controller DTO signatures.
- [x] Compared ADMIN authorization, self/last-admin representation, correction preview/request/approve/execute/open contracts, failure/stale behavior, and safe error rendering.
- [x] Used WI-004 only as an upstream risk input; backend state-machine internals were not re-reviewed.
- [x] Classified every confirmed finding with severity, file/line evidence, impact, reasoning, missing test, and recommended repair.
- [x] Recorded confirmed controls, residual risks, rollback implications, and WI-028 block status.
- [x] Ran only the two focused frontend test files.

## Reference Documents (Tier 0-2)

| Tier | Document | Reason |
|------|----------|--------|
| 0 | `docs/standards/core-principles.md` | Constitution, language, traceability, and active SPA baseline |
| 0 | `docs/standards/development-standards.md` | API/DTO, security, testing, and evidence standards |
| 0 | `docs/standards/documentation-standards.md` | English deliverable and evidence format |
| 0 | `docs/standards/glossary.md` | Local Subscription Correction canonical boundary |
| 1 | `docs/policies/security-policy.md` | Secrets/PII and safe administrator output constraints |
| 1 | `docs/policies/access-control-policy.md` | Least privilege and default-deny baseline |
| Context | `deliverables/user/REQ-20260808-ATS-004.md` | Approved policy and downstream work plan |
| Context | `docs/SR/SR-96.md` | Role restriction and session synchronization contract |
| Context | `docs/SR/SR-97.md` | Local correction, server date, preview, and payment boundary contract |
| Risk input | `deliverables/user/WI-20260809-ATS-004-summary.md` | Upstream backend review status only |
| Risk input | `deliverables/agent/WI-20260809-ATS-004-evidence-pack.md` | Backend actor-authorization residual risk only |
| Handoff | `deliverables/agent/WI-20260809-ATS-005-handoff.md` | Authoritative scope and output contract |

**Injection rules applied:** assignee `cr`; task type frontend/API-contract independent review; no backend state-machine re-review; no external or destructive action.

## Reviewed Components And Contracts

| Surface | Reviewed scope |
|---------|----------------|
| `frontend/src/api/admin.ts` | User list/update request typing and separation from payment-operation APIs |
| `frontend/src/api/userSubscriptions.ts` | Correction request, preview, response, note, open/detail, and mutation wire contracts |
| `frontend/src/types/index.ts` | Shared `User` and `UserRole` types used by the admin API |
| `frontend/src/pages/admin/UserManagePage.tsx` | Self-demotion representation, stable error mapping, confirmation payload, and role snapshot refresh calls |
| `frontend/src/pages/admin/UserManagePage.test.tsx` | Focused role-control, error, success refresh, mismatch, and request-fencing evidence |
| `frontend/src/pages/admin/UserSubscriptionManagePage.tsx` | List preservation, success/list-refresh distinction, and modal integration |
| `frontend/src/pages/admin/UserSubscriptionCorrectionModal.tsx` | Open-state gate, preview identity/state, request/approve/execute transitions, stale fencing, failure handling, and safe output |
| `frontend/src/pages/admin/UserSubscriptionManagePage.test.tsx` | Focused open/resume/stale/preview/failure/success contract evidence |
| Backend controller/DTO signatures | `AdminUserSubscriptionCorrectionController`, correction request/preview/response/approve/execute DTOs, `UserController`, `UserAdminUpdateRequest`, `UserListItemResponse`, and `UserDetailResponse` |

## Confirmed Controls / No Confirmed Finding

- **ADMIN boundary:** `AdminUserSubscriptionCorrectionController.java:24-27` and `UserController.java:76-101` apply ADMIN authorization at the server boundary. No frontend path was treated as the final authorization control.
- **Self/last-admin UX:** `UserManagePage.tsx:124-134,228-265` disables self-demotion and keeps stable backend restriction messages local to the row/modal. No last-admin policy was duplicated as an unreliable client count.
- **Local/payment separation:** `UserSubscriptionCorrectionModal.tsx:552-562,691-704,762-779,893-900` repeatedly identifies local entitlement/billing-agreement state and states that Toss charge/refund, provider billing-key deletion, and email are not executed. The modal imports only the user-subscription correction API.
- **Exact entitlement-state preview:** `UserSubscriptionCorrectionModal.tsx:324-377` invalidates previews on every draft change and builds preview/request bodies from the same draft; `:727-780` displays target plan, cycle, status, expiry, pending state, local agreement state, and external-payment flag.
- **Explicit workflow/resume:** `UserSubscriptionCorrectionModal.tsx:248-302,537-584,783-840` gates new work on open lookup and exposes `REQUESTED`, `APPROVED`, `PROCESSING`, and `SUCCEEDED` stages explicitly.
- **Stale response fencing:** `UserSubscriptionCorrectionModal.tsx:248-347` and focused tests at `UserSubscriptionManagePage.test.tsx:250-313,414-468,688-719` abort and ignore obsolete list/open/preview responses.
- **No false success on known response:** `UserSubscriptionCorrectionModal.tsx:465-501` calls success handling only for `SUCCEEDED`; `UserSubscriptionManagePage.tsx:98-105` distinguishes successful execution from list-refresh failure.
- **Safe rendering:** Fixed messages replace raw request errors; `failureMessage` is not rendered (`UserSubscriptionCorrectionModal.tsx:842-849`); displayed user/free-text values use React interpolation. No raw secret, token, provider identifier, or PII was found in error output.
- **Correction DTO alignment:** Fields and nullable response values at `frontend/src/api/userSubscriptions.ts:59-137` align with `AdminSubscriptionCorrectionRequest.java:11-20`, `AdminSubscriptionCorrectionPreviewResponse.java:12-35`, `AdminSubscriptionCorrectionResponse.java:13-49`, and note DTOs `:5-7`.

## Commands & Outputs

- Targeted read-only inspection used `rg`, `Get-Content`, `git status`, and scoped `git diff` against the handoff files and related wire/controller DTO signatures.
- Focused test command:
  - `npm test -- src/pages/admin/UserManagePage.test.tsx src/pages/admin/UserSubscriptionManagePage.test.tsx`
- Test output:
  - Test files: `2 passed (2)`
  - Tests: `23 passed (23)`
  - Duration: `6.27s`
- No full suite, coverage, typecheck, ESLint, Prettier, build, external request, secret inspection, ZIP access, database/data/schema action, commit, or push was performed.

## Residual Risks

- The scoped user page refreshes `/users/me` after successful role mutation and current-row mismatch (`UserManagePage.tsx:59-89,136-162`). Its `ADMIN_ROLE_REQUIRED` test mocks the API and expects no page-level refresh (`UserManagePage.test.tsx:177-220`); any runtime Axios/auth-store 403 synchronization was outside the authorized file set and is not independently confirmed by this WI.
- Production router configuration was outside scope. The backend ADMIN guards are confirmed; `UserManagePage.test.tsx:272-284` proves route reevaluation only with a test-supplied `ProtectedRoute` wrapper.
- Component mocks do not establish browser/network behavior for commit-before-response-loss or divergent client/server dates.
- WI-004's actor authorization race remains an upstream risk input. No backend lock, transaction, audit, preview-token, or free-text policy analysis was repeated.

## Risks / Rollback

- **Review risk:** Confirmed findings are source/contract-backed. Runtime route/interceptor behavior remains explicitly residual because inspecting those files was forbidden by scope.
- **Product changes:** None. Code, tests, data, schema, provider state, secrets, intentional ZIP, and Git history were untouched.
- **Review deliverables created:**
  - `deliverables/user/WI-20260809-ATS-005-summary.md`
  - `deliverables/agent/WI-20260809-ATS-005-evidence-pack.md`
- **Rollback:** Revert only these two review documents to undo WI-005's filesystem effect. Repair or containment of product behavior requires a separately authorized implementation WI.

## Downstream Status

- `WI-20260808-ATS-028` **remains blocked**.
- Unblock conditions from WI-005: repair MAJOR-001 and MAJOR-002, add the focused tests described above, and produce current passing evidence.
- WI-004's backend BLOCKER remains an independent prerequisite. This review does not promote a server preview receipt/token or structured-only operator notes into approved requirements.

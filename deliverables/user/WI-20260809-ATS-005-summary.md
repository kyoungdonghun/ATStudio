# WI-20260809-ATS-005 Frontend/API Contract Independent Review

## Findings

### MAJOR-001 - Ambiguous mutation outcomes are not reconciled with server state

- **Evidence:** `frontend/src/pages/admin/UserSubscriptionCorrectionModal.tsx:408-435`, `:437-463`, and `:465-501` treat request, approval, and execution rejections as local stage failures without reloading the correction. The client already exposes detail and open-state reads at `frontend/src/api/userSubscriptions.ts:219-241`. Focused tests at `frontend/src/pages/admin/UserSubscriptionManagePage.test.tsx:598-686` preserve the local row/stage but do not model a server commit followed by a lost response.
- **Impact:** A timeout or lost response can leave the server at `REQUESTED`, `APPROVED`, `PROCESSING`, `SUCCEEDED`, or `FAILED` while the UI remains one stage behind. The operator can retry a stale transition, and a terminal execution result can disappear after close/reopen because the open-state endpoint returns only non-terminal work.
- **Reasoning:** A rejected HTTP promise proves that the response is unknown; it does not prove that the server transaction did not commit. The execute message asks the operator to check status, but the modal provides no status-refresh action.
- **Missing test:** Request/approve/execute cases where the mutation commits, the response is lost, and a follow-up open/detail read returns the advanced or terminal state.
- **Recommended fix:** Reconcile request failures through the open-state endpoint and approval/execution failures through the correction detail endpoint. Keep the correction ID, draft, and notes visible; disable duplicate transitions while outcome is unknown; provide an explicit status-retry action if reconciliation also fails.

### MAJOR-002 - Browser time blocks requests governed by the server business date

- **Evidence:** `frontend/src/pages/admin/UserSubscriptionCorrectionModal.tsx:94-103` derives Seoul `today` from the browser clock. Lines `:349-377` make that value a blocking validation prerequisite, and lines `:654-667` enforce it through input bounds. The approved contract states that `today` comes from the server Clock and frontend checks are advisory (`docs/SR/SR-97.md:31`, `deliverables/user/REQ-20260808-ATS-004.md:37`).
- **Impact:** A skewed client clock or a boundary-time session can block a correction that the authoritative server would accept, so the preview endpoint never receives the request.
- **Reasoning:** Backend validation remains authoritative only if the UI allows the payload to reach preview. The current validation disables preview before the server can decide.
- **Missing test:** A client/server date-boundary case where browser `today` differs from the server business date.
- **Recommended fix:** Make browser date checks advisory, or obtain an authoritative server business date before applying blocking bounds. Keep the server preview response as the final validation result.

### MINOR-001 - User-management TypeScript types do not match the controller DTOs

- **Evidence:** `frontend/src/api/admin.ts:36-52` types list rows and update results as the shared `User` and accepts `UserRole`. `frontend/src/types/index.ts:31-53` includes frontend-only `GUEST` and requires profile fields. The backend list DTO omits those profile fields (`src/main/java/com/atstudio/atstudio/dto/user/UserListItemResponse.java:9-16`), while the update DTO accepts only the backend role enum (`src/main/java/com/atstudio/atstudio/dto/user/UserAdminUpdateRequest.java:12-18`).
- **Impact:** TypeScript callers can compile an invalid `GUEST` role payload or read list-row fields that are absent at runtime. The current page uses only common fields and offers only `USER`/`ADMIN`, so no current rendering failure was confirmed.
- **Reasoning:** The shared session/user model is broader than both admin list and update wire contracts.
- **Missing test:** A realistic list fixture matching `UserListItemResponse` and a type-level check that admin mutation roles exclude `GUEST`.
- **Recommended fix:** Introduce exact `AdminUserListItem`, `AdminUserDetail`, and `AdminAssignableRole` wire types and use them in `fetchUsers`/`updateUserAdmin`.

### MINOR-002 - Normalized text fields are sent without an exact visible confirmation

- **Evidence:** The modal trims `reasonNote`, approval note, and execution note before sending at `frontend/src/pages/admin/UserSubscriptionCorrectionModal.tsx:365-376`, `:437-447`, and `:465-475`. The visible textareas retain raw input at `:707-720`, `:788-819`; the preview and confirmation content at `:727-780` and `:893-900` does not show the normalized strings. Tests deliberately enter surrounding whitespace and verify trimmed API values at `frontend/src/pages/admin/UserSubscriptionManagePage.test.tsx:206-222` and `:535-590`, but do not verify the exact sent text is displayed.
- **Impact:** The operator can confirm text that differs from the value persisted in correction/audit records. Entitlement target fields are displayed correctly; the mismatch is limited to normalized text fields.
- **Reasoning:** Free text is permitted. The defect is only the definite display-versus-payload mismatch, not the content policy.
- **Missing test:** Visible normalized reason/note assertions immediately before request, approval, and execution.
- **Recommended fix:** Normalize the controlled value before preview/confirmation or render the normalized reason/note in the preview and confirmation dialog with the correction ID.

## Confirmed Controls

- The backend correction controller is class-level ADMIN-only, and user list/update endpoints are ADMIN-only (`AdminUserSubscriptionCorrectionController.java:24-27`; `UserController.java:76-101`).
- Self-demotion is disabled in the page, stable last-admin/self-demotion errors remain attached to the row/modal, and the backend remains the final restriction source.
- The correction UI explicitly identifies a local entitlement correction and states that Toss charge/refund, provider billing-key deletion, and email are not executed.
- The preview shows plan, cycle, status, expiry, pending-change handling, local billing-agreement handling, and external-payment status. Draft edits invalidate stale previews.
- Open `REQUESTED`/`APPROVED`/`PROCESSING` work is loaded before new work and resumed explicitly. Request fencing aborts and ignores stale row/preview responses.
- Success is displayed only for a `SUCCEEDED` execution response; a failed list refresh is distinguished from execution success.
- Error rendering uses fixed UI messages. Raw backend failure details, secrets, tokens, provider identifiers, and PII were not found in error output; React text interpolation safely escapes displayed free text.
- Correction request/preview/response TypeScript fields align with the reviewed backend controller DTO signatures.

## Verification

- Focused command: `npm test -- src/pages/admin/UserManagePage.test.tsx src/pages/admin/UserSubscriptionManagePage.test.tsx`
- Result: **2 test files passed, 23 tests passed**.
- No full suite, coverage, typecheck, lint, build, external call, secret inspection, ZIP access, data/schema action, commit, or push was performed.

## Residual Risks

- Scoped source confirms current-user refresh after a successful role mutation and after a list-row role mismatch. The `ADMIN_ROLE_REQUIRED` page test mocks the API layer and explicitly expects no page-level `/users/me` call, so runtime 403 synchronization through the out-of-scope client/auth layer was not independently re-proven here.
- Production router wiring was outside the authorized file set. Backend ADMIN guards were confirmed; the focused user-page test proves reevaluation only when the page is manually wrapped in `ProtectedRoute`.
- WI-004's backend actor-authorization race remains an upstream risk input and was not re-reviewed.
- Mocked component tests do not provide browser/network evidence for ambiguous committed mutations or client/server clock disagreement.

## Decision And Downstream Status

- **Result:** `0 BLOCKER / 2 MAJOR / 2 MINOR`.
- **WI-20260808-ATS-028:** **REMAINS BLOCKED** until MAJOR-001 and MAJOR-002 are repaired with focused tests. WI-004's backend BLOCKER also remains an independent prerequisite; this review does not carry forward its server-preview-token or free-text policy findings.
- **Rollback:** Product code, tests, data, schema, provider state, and Git history were unchanged. Rolling back this WI means reverting only this summary and its paired evidence pack. Product rollback is not required for the review itself.

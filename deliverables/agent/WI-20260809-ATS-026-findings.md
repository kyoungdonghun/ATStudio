# WI-20260809-ATS-026 Findings

## Evidence Boundary

- Baseline: `e343c20`.
- Scope: source audit of the paths named in `WI-20260809-ATS-026-handoff.md`, plus the supplied targeted-check results. No product, test, runtime, browser, database, storage, CSV, secret, ZIP, stage, or commit operation was performed.
- Anonymous route checks: `PASS` for five guarded routes; each redirected to `/login?returnTo=` with the local route encoded. No authenticated session was available.
- Authenticated role behavior, responsive behavior, live mutation behavior, and durable DB/storage/export verification: `BLOCKED` because no authorized session and safe fixture were available. These lanes are not inferred from source or UI copy.
- Targeted checks supplied for this report: frontend 11 files / 57 tests `PASS`; backend 27 XML suites / 176 tests `PASS`; typecheck `PASS`; targeted ESLint `PASS`. Command logs, exit codes, and durations were not supplied, so they are not reconstructed here.

## Findings

### ATS-026-F01 — REMOVAL_REQUESTED is presented as delete/deleted although the backend is a no-op

- Severity: P2
- Row: `MEM-12` / `WL-004`; route `/whitelist-channels`.
- Evidence: `WhitelistChannelPage.tsx:253-270,453-461,470-482` presents `REMOVAL_REQUESTED` with delete/deleted UI wording and a success message after `deleteChannel`; `WhitelistChannelService.java:187-189` returns without changing state when the current status is `REMOVAL_REQUESTED`.
- UI: `REMOVAL_REQUESTED` is presented as delete/deleted and the flow can display success.
- Request: `DELETE /api/whitelist-channels/{id}` is invoked by the page; authenticated invocation/response was not run (`BLOCKED`).
- Server: source shows a no-op for an already `REMOVAL_REQUESTED` row; no live response was observed (`NOT INSPECTED`).
- Durable state: no DB state was inspected (`BLOCKED`).
- Impact: the UI can imply deletion when the server preserves the row and timestamp.
- Follow-up: make the preserved-row/idempotent result explicit and use status-accurate delete wording.

### ATS-026-F02 — Primary action is exposed for states rejected by the backend

- Severity: P2
- Row: `MEM-12` / `WL-006`; route `/whitelist-channels`.
- Evidence: `WhitelistChannelPage.tsx:423-432` renders the primary action for every non-primary channel; `WhitelistChannelService.java:160-162` rejects `REMOVAL_REQUESTED` and `CANCELLED` through `isPrimaryEligible`; `WhitelistChannel.java:133-135` defines those states as ineligible.
- UI: the action remains visible for non-primary `REMOVAL_REQUESTED` and `CANCELLED` rows.
- Request: clicking invokes `PUT /api/whitelist-channels/{id}/primary`; live invocation was not run (`BLOCKED`).
- Server: source rejects the two states with `INVALID_STATE_TRANSITION`; live response not observed (`NOT INSPECTED`).
- Durable state: not inspected (`BLOCKED`).
- Impact: users can initiate an action that the contract guarantees will fail.
- Follow-up: apply the same eligibility predicate before rendering or disabling the action.

### ATS-026-F03 — Frontend URL validation accepts non-YouTube and HTTP URLs rejected by the backend

- Severity: P2
- Row: `MEM-12` / `WL-001`, `WL-003`; route `/whitelist-channels`.
- Evidence: `validation.ts:52-56` uses `^https?://.+`; `WhitelistChannelPage.tsx:153-166` applies that pattern; `WhitelistChannelService.java:205-221` requires HTTPS and a `youtube.com` host (with no non-standard port or user info); `docs/design/usecase/whitelist.md:38-50,94-98` documents the backend contract.
- UI: the form accepts any syntactically non-empty HTTP(S) URL matching the broad pattern.
- Request: register/update can invoke the API with such a value; live invocation was not run (`BLOCKED`).
- Server: source rejects values outside the HTTPS YouTube contract; live response not observed (`NOT INSPECTED`).
- Durable state: no persisted value was inspected (`BLOCKED`).
- Impact: users receive late server rejection for values the form presents as valid.
- Follow-up: share or mirror the authoritative HTTPS YouTube predicate in frontend validation.

### ATS-026-F04 — Whitelist URL length has no UI limit

- Severity: P3
- Row: `MEM-12` / `WL-001`, `WL-003`; route `/whitelist-channels`.
- Evidence: `WhitelistChannelPage.tsx:356-362` renders the URL field without `maxLength`; `validation.ts:52-56` defines no URL maximum; `src/main/java/com/atstudio/atstudio/dto/whitelist/WhitelistChannelRequest.java:6-10` applies `@Size(max = 255)` to `channelUrl`.
- UI: URL input has no client-side maximum or length guidance.
- Request: a long value may be sent after the pattern check; no live request was made (`BLOCKED`).
- Server: the request DTO source establishes rejection of a URL longer than 255 characters; no live response was observed (`NOT INSPECTED`).
- Durable state: not inspected (`BLOCKED`).
- Impact: input size is unconstrained at the UI boundary and users can reach a late server validation failure.
- Follow-up: enforce the 255-character contract in the field and validation helper.

### ATS-026-F05 — Editing processed or revision-requested channels silently requeues them with generic save copy

- Severity: P2
- Row: `MEM-12` / `WL-003`; route `/whitelist-channels`.
- Evidence: `WhitelistChannelPage.tsx:183-199` uses the same generic update success message for all edits; `WhitelistChannelPage.tsx:448-451` uses the shared edit predicate; `WhitelistChannelService.java:102-104,265-269` treats `EXPORTED`, `REGISTERED`, and `REVISION_REQUESTED` edits as reprocessing candidates, then `WhitelistChannel.java:87-93` moves the status to `PENDING`; `docs/design/usecase/whitelist.md:94-108` documents this behavior.
- UI: edit and success copy does not identify that an eligible processed/revision edit causes reprocessing.
- Request: `PUT /api/whitelist-channels/{id}` is invoked on save; live request/response was not run (`BLOCKED`).
- Server: source requeues the listed statuses after subscription/limit checks; live response not observed (`NOT INSPECTED`).
- Durable state: no status transition was inspected (`BLOCKED`).
- Impact: a user may not understand that editing changes the review workflow and can consume processing capacity.
- Follow-up: make the requeue consequence explicit and confirm it for statuses where the transition is material.

### ATS-026-F06 — “수정 후 재요청” is ambiguous and redundant for REVISION_REQUESTED

- Severity: P2
- Row: `MEM-12` / `WL-003`, `WL-005`; route `/whitelist-channels`.
- Evidence: `WhitelistChannelPage.tsx:51-55,434-446` exposes a direct “수정 후 재요청” action for `REVISION_REQUESTED`; `WhitelistChannelService.java:121-135` allows that status through `ensureCanEnterPending` and `requestRegistration`; `WhitelistChannelService.java:102-104,265-269` also requeues the status when it is edited.
- UI: the direct “수정 후 재요청” action is rendered without demonstrated correction, while editing already triggers requeue; no authenticated interaction was executed (`BLOCKED`).
- Request: the direct action would invoke `POST /api/whitelist-channels/{id}/request`; live invocation/response was not observed (`NOT INSPECTED`).
- Server: source permits direct requeue through the generic request path and separately requeues on edit; no live contract result was captured.
- Durable state: not inspected (`BLOCKED`).
- Impact: the correction requirement and action semantics are ambiguous/redundant, and the “수정 후” label is not enforced by the request path.
- Follow-up: choose and document one correction/requeue contract, then enforce it in both the UI action and backend transition.

### ATS-026-F07 — Export can use the applied keyword while the visible draft keyword is different, and confirmation omits exact scope

- Severity: P1
- Row: `ADM-11` / `WL-007`, `WL-008`; route `/admin/whitelist-channels`.
- Evidence: `WhitelistChannelManagePage.tsx:70-73,122-125` separates `keywordInput` from applied `keyword`; `:176-202` builds the export request from applied `statusFilter`/`keyword`, while confirmation text names only the status and generic CSV scope.
- UI: the draft text and applied filter can differ; export confirmation does not state the exact applied keyword or expected count.
- Request: export sends only the applied `keyword` and `status` (`:182-185,198`); no live export was executed (`BLOCKED`).
- Server: server selection and transition result were not observed (`NOT INSPECTED`).
- Durable export state: CSV bytes, batch items, and status transitions were not inspected (`BLOCKED`).
- Impact: an operator can believe the typed draft is the export scope or fail to notice that the applied scope is broader/different.
- Follow-up: show applied filters and an exact bounded scope summary in the confirmation, and make draft-versus-applied state visually explicit.

### ATS-026-F08 — Failed admin whitelist reload leaves old actionable rows under the new controls

- Severity: P1
- Row: `ADM-11`; route `/admin/whitelist-channels`.
- Evidence: `WhitelistChannelManagePage.tsx:83-113` sets `error` on load failure but does not clear `channels` or `edits`; `:302-418` renders the existing table whenever `loading` is false, even while `error` is shown.
- UI: after a failed reload, prior rows and their status controls can remain displayed beside the new filter state and error.
- Request: the failed list request is invoked; live failure behavior was not executed (`BLOCKED`).
- Server: no live error response was observed (`NOT INSPECTED`).
- Durable state: no mutation or persisted state was inspected (`BLOCKED`).
- Impact: an operator may act on stale rows believing they belong to the newly selected filter.
- Follow-up: clear or quarantine old rows while the new query is failed, and provide an explicit retry state before enabling actions.

### ATS-026-F09 — Admin whitelist note input lacks the 500-character limit and guidance

- Severity: P2
- Row: `ADM-11` / `WL-007`; route `/admin/whitelist-channels`.
- Evidence: `WhitelistChannelManagePage.tsx:390-402` renders the admin note textarea without `maxLength`, counter, or length guidance; `src/main/java/com/atstudio/atstudio/dto/whitelist/AdminWhitelistChannelStatusRequest.java:7-10` applies `@Size(max = 500)` to `adminNote`.
- UI: note length is not bounded or explained in the admin row editor.
- Request: status update trims and sends the entire note (`:150-164`); no live request was made (`BLOCKED`).
- Server: the request DTO source establishes rejection of notes over 500 characters; no live response was observed (`NOT INSPECTED`).
- Durable state: no persisted note was inspected (`BLOCKED`).
- Impact: client input and server validation contract diverge, producing a late server rejection.
- Follow-up: enforce the 500-character contract in the textarea and request validation, with a visible counter/guidance.

### ATS-026-F10 — Existing-status lookup failure other than 403/404 leaves the company-certification application form active

- Severity: P2
- Row: `G-BUS` / `CC-001`; route `/company-certification/apply`.
- Evidence: `CompanyCertApplyPage.tsx:58-91` navigates away for any successful non-`REJECTED` status; in the catch path it sets `accessDenied` only for 403 and sets `error`, but the form is not suppressed by the generic error state. The form is rendered below the check state (`:190` onward in the same page).
- UI: a non-403/non-404 existing-status lookup error can leave the active submission form available with an error message.
- Request: `fetchMyCompanyCert` is invoked; live response was not observed (`BLOCKED`).
- Server: no live error response was observed (`NOT INSPECTED`).
- Durable state: no application attempt or durable result was inspected (`BLOCKED`).
- Impact: the user can attempt a new submission without a confirmed open-application check.
- Follow-up: gate the form on a successful 404/no-existing result or an explicitly allowed `REJECTED` result; render a blocking retry/error state otherwise.

### ATS-026-F11 — Company-certification status and admin list/detail load errors have no retry action

- Severity: P2
- Row: `G-BUS` / `CC-003`; `ADM-06` / `CC-004`, `CC-005`.
- Evidence: `CompanyCertStatusPage.tsx:48-72,125-133` sets an error view without retry; `CompanyCertManagePage.tsx:100-129,138-155,229-243` sets list/detail errors without a retry control or retry handler.
- UI: status-page and admin list/detail load failures terminate in an error message with no retry action.
- Request: load requests are part of the page flow; live failures were not executed (`BLOCKED`).
- Server: no live error responses were observed (`NOT INSPECTED`).
- Durable state: not applicable to read failure and not inspected (`NOT INSPECTED`).
- Impact: transient failures require manual navigation/reload and can strand the workflow.
- Follow-up: provide scoped retry actions that preserve the current route/filter/detail context.

### ATS-026-F12 — English loading text and stale admin legacy wording remain in scoped screens

- Severity: P3
- Row: applicable loading/status screens in `G-BUS`, `ADM-06`, and `ADM-11`.
- Evidence: `CompanyCertStatusPage.tsx:117-121`, `CompanyCertManagePage.tsx:229-233`, and `WhitelistChannelManagePage.tsx:302-304` render `Loading...`; `CompanyCertManagePage.tsx:350-355` contains the stale legacy wording `이전 방식으로 저장된 신청`.
- UI: English loading text and stale admin legacy wording are visible in the scoped flows.
- Request/server/durable state: no independent request, response, or durable-state defect is claimed; these are UI-copy observations only (`NOT INSPECTED` for other lanes).
- Impact: minor localization and workflow comprehension friction.
- Follow-up: replace the three loading copies with consistent Korean text and remove or clarify the stale legacy wording.

## Coverage Gaps

- Authenticated USER/BUSINESS/ADMIN execution, role matrix behavior, responsive checks at `1440x900`, `1024x768`, `390x844`, and `360x800`, live server responses, DB/storage state, export batch/CSV bytes, audit records, stale/race behavior in runtime, keyboard/focus behavior, and browser restoration were `BLOCKED` or `NOT INSPECTED` under the handoff constraints.
- The supplied quality baseline covers the named targeted checks only: frontend 11 files / 57 tests, backend 27 XML suites / 176 tests, typecheck, and targeted ESLint. It does not establish the blocked runtime or durable-state lanes.

## Report

- Output: `deliverables/agent/WI-20260809-ATS-026-findings.md`
- Finding count: 12 independent findings (`ATS-026-F01` through `ATS-026-F12`).

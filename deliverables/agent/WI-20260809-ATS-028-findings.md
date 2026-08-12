# WI-20260809-ATS-028 Integration Audit Findings

## 1. Checkpoint Scope

- WI: `WI-20260809-ATS-028`
- Assignee role: `qa-integ`
- Audit boundary: bounded source, existing assertion, and targeted automated-test integration audit only.
- Audited rows: `ADM-01` through `ADM-11`, `ADM-14`, and `SH-07`.
- Audited non-navigation boundaries: assigned support APIs, payment reconciliation scheduling, and withdrawn-user billing cleanup.
- Anonymous ADMIN-route guard and browser-restoration evidence was collected separately by main. Authenticated ADMIN/browser variants remain blocked and were not collected by this assignee.
- No live database, Provider, mail, private file, CSV/import/export/download, ADMIN mutation, ignored secret, or browser/session data was accessed.
- `output/client-demo-screenshots-20260716-140514.zip` was not opened, read, hashed, touched, or otherwise inspected.
- `COMPLETE` below means the assigned bounded source/test audit is complete. It does not mean that the row passed acceptance or that browser/live evidence exists.

## 2. Coverage Status

| Item                                                   | Status              | Audit disposition                                                                                                                                                                                                                   |
| ------------------------------------------------------ | ------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `ADM-01` `/admin/dashboard`                            | COMPLETE            | Latest-request and retry controls are covered; the executable matrix's fourth total conflicts with the three-field source/test contract. See F-13.                                                                                  |
| `ADM-02` `/admin/users`                                | COMPLETE            | Backend authorization, locking, audit, rejection, and frontend mutation paths were audited. Stale-authority refresh and missing detail UI remain. See F-04 and F-05.                                                                |
| `ADM-03` `/admin/subscriptions`                        | COMPLETE            | Read-only mapping is active, but two contract fields are omitted from the table. See F-08.                                                                                                                                          |
| `ADM-04` `/admin/licenses`                             | COMPLETE            | ADMIN list/detail ownership was audited; the active list surface lacks request ownership and stable selected-user identity. See F-06.                                                                                               |
| `ADM-05` `/admin/questions`                            | COMPLETE            | Mapping, entity transition rules, list/mutation lifecycle, and assertions were audited. See F-06 and F-07.                                                                                                                          |
| `ADM-06` `/admin/company-certifications`               | COMPLETE            | List/detail fencing, private metadata, review service/audit, and assertions were audited. Pending review ownership remains unsafe. See F-09. Binary download remains WI-029.                                                        |
| `ADM-07` `/admin/tags`                                 | COMPLETE            | Duplicate handling and CRUD assertions were audited. Pending modal ownership and destructive dependency copy remain. See F-09 and F-10.                                                                                             |
| `ADM-08` `/admin/track-manage`                         | COMPLETE            | List/delete source and assertions plus audio-analysis dry-run were audited. Durable deletion semantics and list request ownership remain. See F-02, F-06, and F-09.                                                                 |
| `ADM-09` `/admin/user-subscriptions`                   | COMPLETE            | Preview/request/approve/execute/recovery source and assertions were audited. Unknown-outcome recovery is implemented; typed execution confirmation is not. See F-11.                                                                |
| `ADM-10` `/admin/payments`                             | COMPLETE            | All nine tab call sites, refund/correction stages, settlement/incident controls, backend ledgers/audits, scheduler, and assertions were audited. See F-01 and F-14. Binary settlement evidence remains WI-029.                      |
| `ADM-11` `/admin/whitelist-channels`                   | COMPLETE            | Status transitions, latest-request fencing, immutable export snapshots, formula neutralization, locking, and assertions were audited. No additional material source/test integration defect was confirmed; CSV bytes remain WI-029. |
| `ADM-14` `/admin/settings`                             | COMPLETE            | Public read, ADMIN upsert, active consumer, and frontend assertions were audited. Save ownership/canonical reload and backend coverage remain defective. See F-12.                                                                  |
| `SH-07` ADMIN confirmations                            | COMPLETE            | Shared focus/Escape/pending behavior and every assigned ADMIN occurrence were reconciled. See F-09 and F-11.                                                                                                                        |
| Support: ADMIN reconciliation GET                      | COMPLETE            | Active ADMIN-only, no SPA caller, observation-only source/test contract. Matrix conflict is unresolved. See F-14.                                                                                                                   |
| Support: audio-analysis dry-run GET                    | COMPLETE            | Intentional API-only operator support; ADMIN-only, bounded, deterministic, read-only, and no-save assertions confirmed.                                                                                                             |
| Support: refund and entitlement-correction detail GETs | COMPLETE            | Active ADMIN support APIs; no frontend wrapper/caller. Their absence becomes a product recovery defect for ambiguous executions. See F-01.                                                                                          |
| Support: ADMIN License detail GET                      | COMPLETE            | Intentional API-only read support for owner-scoped detail; current ADMIN product surface is list-by-user.                                                                                                                           |
| Support: ADMIN User detail GET                         | COMPLETE            | Active minimized detail API, but `ADM-02` explicitly requires a detail modal and has no caller. See F-05.                                                                                                                           |
| Support: subscription-correction history               | COMPLETE            | Backend history API is active; `fetchAdminSubscriptionCorrections` has no non-test importer. Treat the backend as API-only operational history and the frontend wrapper as non-product/dead call-site evidence.                     |
| Payment reconciliation scheduler                       | COMPLETE            | Daily zone-bound scheduler, bounded keyset/lookback/caps, sanitized aggregate logging, durable Incident handling, and no automatic money mutation were confirmed in source/assertions. Test evidence is isolated, not live proof.   |
| Withdrawn-user billing cleanup                         | COMPLETE            | AFTER_COMMIT plus daily 01:15 ownership, eligibility, active-user exclusion, claim/result transactions, observability, and assertions were audited. Failed cleanup retryability conflicts with the runbook/matrix. See F-03.        |
| Anonymous ADMIN-route guards/restoration               | COMPLETE            | Main re-ran all 12 assigned anonymous routes, confirmed exact encoded local `returnTo` redirects, restored `/`, and confirmed a neutral browser state without an authenticated API call or mutation.                                |
| Authenticated ADMIN/browser variants                   | REMAINING (BLOCKED) | No authenticated ADMIN page, API, Provider, durable-state, file, or mutation evidence was collected in this bounded audit.                                                                                                          |

No assigned source/test row or support/scheduler audit item is `REMAINING` at this checkpoint. Anonymous guard/restoration evidence is complete through main's supplemental run; authenticated ADMIN browser, live-state, Provider, DB, and binary variants remain blocked or outside this assignee's bounded audit.

## 3. Findings

### F-01 [P1] Payment execution response loss is reported as failure without authoritative recovery

- Rows/routes: `ADM-10`, `SH-07`, `/admin/payments`; support `GET /api/admin/payments/refunds/{refundId}` and `GET /api/admin/payments/entitlement-corrections/{correctionId}`.
- Independent cause/contract: an external refund or durable local correction may commit before the browser loses the response. Matrix mutation policy requires an authoritative read before retry.
- UI lane: typed prompts name the refund/correction and distinguish Provider refund from local entitlement correction, but either catch path only displays failure.
- Frontend invocation lane: `PaymentOperationsPage.tsx:617-635` and `:704-725` clear busy state after any rejection and do not perform a detail/list reconciliation read. `frontend/src/api/admin.ts:741-782` and `:806-847` expose list and mutation wrappers but no detail wrappers.
- Server/test-Provider lane: ADMIN detail mappings exist at `AdminPaymentController.java:160-164` and `:214-218`. Refund execution claims before the Provider call and records the result through a separate transaction at `AdminPaymentRefundService.java:149-204`; the persisted idempotency key is asserted at `AdminPaymentRefundServiceTest.java:260-308`. These are test-double/H2 facts, not live Provider proof.
- Durable-state lane: refund/correction/audit rows may already be committed even when the UI says execution failed. Immediate retry is therefore not a proven failure retry.
- Test boundary: `PaymentOperationsPage.test.tsx:462-610` proves request creation and refresh, but contains no execute-response-loss/detail-read assertion. The backend resilience tests do not repair the missing frontend lane.
- Impact: an operator can see a false failure after money movement or local entitlement mutation and can retry without first learning the canonical state.
- Bounded follow-up: add both detail wrappers and one bounded detail read for network/no-response/5xx; preserve an unknown state and block duplicate execution until the read resolves. Add committed-response-loss tests for refund and correction separately.

### F-02 [P1][BLOCKED-CONTRACT] Track "soft delete" removes durable business history

- Rows/routes: `ADM-08`, `/admin/track-manage`.
- Independent cause/contract: UI copy and source naming describe deactivation, while the service physically removes multiple durable relationships first.
- UI lane: `TrackManagePage.tsx:227-245` describes the operation as soft deletion/deactivation.
- Frontend invocation lane: the page invokes the DELETE wrapper once and then reloads the list.
- Server lane: `TrackService.java:217-228` deletes Likes, Track Downloads, Licenses, Playlist membership, Album membership, and Track Tags before `track.deactivate()`.
- Test lane: `TrackServiceTest.java:614-623` asserts only Track Tag deletion before deactivation; it does not assert or constrain the other history purges.
- Durable-state lane: purchase/license/download and user-curated relationship rows are deleted, not merely hidden by Track inactivity.
- Impact: an ADMIN action presented as reversible visibility removal can erase historical entitlement and usage evidence.
- Bounded follow-up: main must obtain an explicit retention contract before implementation. Then either retain durable ledgers and remove only allowed associations, or change the product/acceptance copy and add exhaustive deletion/audit assertions for every approved cascade.

### F-03 [P1][BLOCKED-POLICY] Deterministic withdrawal cleanup failures cannot reach the documented daily retry

- Boundary: withdrawn-user billing cleanup scheduler.
- Independent cause/contract: the runbook and matrix require failed cleanup to remain observable and retryable, but the repository excludes the state produced for deterministic failures.
- UI lane: no manual cleanup endpoint exists; operators can only inspect Incidents (`payment-operations-runbook.md:308-314`).
- Invocation/scheduler lane: `WithdrawalBillingCleanupCoordinator.java:31-63` runs daily at 01:15 and only processes IDs returned by the service.
- Server lane: missing Provider configuration and decryption failures return `FAILED` at `BillingAgreementCleanupProviderExecutor.java:42-56`; `BillingAgreementCleanupTransactionService.java:192-197` persists `BillingKeyCleanupStatus.FAILED`, and `:178` classifies both `PENDING_PROVIDER_CONFIRMATION` and `FAILED` as stable failure.
- Durable-state lane: candidate queries include only `NONE` and `REQUIRED` at `BillingAgreementRepository.java:85-99`. The encrypted key remains retained but the agreement is no longer selectable.
- Test boundary: `BillingAgreementRepositoryTest.java:189-198` explicitly asserts that a failed agreement is excluded. This proves current behavior; it does not prove the runbook's retry promise.
- Contract conflict: the matrix requires failures to remain retriable (`WI-20260809-ATS-020-acceptance-matrix.md:563`), while the current runbook says configuration/decryption/ordinary failures retain the key and are retried by the daily job (`payment-operations-runbook.md:296-304`).
- Impact: a withdrawn user's Provider billing key can remain indefinitely uncleared after a recoverable configuration/decryption failure.
- Bounded follow-up: approve one retry/disposition policy before changing code. Define which definite failures may return to `REQUIRED`, which unknown outcomes stay non-replayable, and how an operator safely requeues a stable failure; then add repository-to-scheduler convergence tests.

### F-04 [P2] Stale ADMIN rejection does not refresh the session role

- Rows/routes: `ADM-02`, `/admin/users`.
- Independent cause/contract: the UI copy claims the current role is being refreshed, but the rejection path never invokes that refresh.
- UI lane: `UserManagePage.tsx:24-29` maps `ADMIN_ROLE_REQUIRED` to “Your current role is being refreshed.”
- Frontend invocation lane: successful mutations call `refreshRoleSnapshot()` at `UserManagePage.tsx:150-167`; the catch at `:168-172` only sets feedback.
- Server lane: server authorization still rejects stale ADMIN authority, so this is not a server-side privilege bypass.
- Test lane: the parameterized rejection test explicitly asserts `fetchMe` was not called at `UserManagePage.test.tsx:195-237`, including `ADMIN_ROLE_REQUIRED`. Success refresh is separately asserted at `:244-263`.
- Durable-state lane: the server role may already be canonical while the Zustand/session shell remains stale.
- Impact: a demoted operator can remain in ADMIN navigation and repeatedly receive 403s until a later list load or sign-in refreshes identity.
- Bounded follow-up: refresh identity on stale-authority/403 rejection, route through the canonical guard result, and change the test to require exactly one refresh without retrying the mutation.

### F-05 [P2] The required ADMIN User detail product surface is missing

- Rows/routes: `ADM-02`, `/admin/users`; support `GET /api/users/{userId}`.
- Independent cause/contract: the executable matrix requires a detail modal, not merely an API-only support endpoint.
- UI lane: `UserManagePage.tsx` renders list rows and role controls only; there is no detail modal or detail invocation.
- Frontend invocation lane: `frontend/src/api/admin.ts:31-77` defines `AdminUserDetail` only as the PUT result and has no GET-detail wrapper.
- Server lane: ADMIN-only GET detail is mapped at `UserController.java:86-91`; `UserDetailResponse.java:9-33` exposes bounded profile fields and no credential secret.
- Test lane: controller role/shape tests use `UserDetailResponse` at `UserControllerTest.java:88-118`; no frontend detail interaction test exists.
- Durable-state lane: read-only; no mutation is implied.
- Impact: operators cannot inspect the contracted detail fields before role decisions, despite the backend support surface being active.
- Bounded follow-up: add a dedicated GET wrapper and accessible read-only modal with latest-request ownership, minimized PII, explicit error/close behavior, and ADMIN/USER controller plus frontend tests.

### F-06 [P2] Three ADMIN collections have no latest-request ownership

- Rows/routes: `ADM-04` `/admin/licenses`, `ADM-05` `/admin/questions`, `ADM-08` `/admin/track-manage`.
- Independent cause/contract: asynchronous list responses commit directly without AbortController or generation/key checks.
- UI/frontend lane: License responses write unconditionally at `LicenseManagePage.tsx:26-40`; search does the same at `:49-60`, and a `userId` deep link loads rows while `selectedUser` remains null at `:13-20` and `:43-47`. Questions write unconditionally at `QuestionManagePage.tsx:65-82`. Tracks do the same at `TrackManagePage.tsx:34-55`.
- Server lane: each request is independently valid; the defect is client ownership of multiple valid responses.
- Test lane: no dedicated License/Question/Track latest-wins assertion exists. The broad tests exercise normal requests but do not race old versus new responses.
- Durable-state lane: unchanged, but rendered rows can be attributed to the wrong selected User/filter/page.
- Impact: an older License response can overwrite a newer User selection, and older Question/Track requests can overwrite current filters or pagination.
- Bounded follow-up: add AbortController plus monotonic request key per collection; derive selected-user identity canonically from the URL/detail read; add deferred-response race tests for User A/User B and old/new filter/page.

### F-07 [P2] Question status controls offer transitions the entity rejects

- Rows/routes: `ADM-05`, `/admin/questions`.
- Independent cause/contract: frontend option generation ignores the backend state machine.
- UI/frontend lane: `QuestionManagePage.tsx:39` defines all statuses and `:191-203` presents all of them for every row. One scalar `updatingId` at `:63` and `:100-111` also permits a second row mutation while the first is pending.
- Server lane: `Question.java:46-59` permits only `OPEN -> IN_PROGRESS/CLOSED`, `IN_PROGRESS -> RESOLVED/CLOSED`, `RESOLVED -> CLOSED`, and no transition from `CLOSED`.
- Test lane: `adminSubscriberPages.coverage.test.tsx:537-563` changes an `OPEN` fixture directly to `RESOLVED` and mocks success, encoding an impossible server contract. `QuestionServiceTest.java:465-488` asserts only a valid `OPEN -> IN_PROGRESS` path.
- Durable-state lane: rejected transitions do not persist, while the page-level catch replaces stable row context with a generic error branch.
- Impact: the UI invites predictable server errors and the existing frontend test masks the mismatch.
- Bounded follow-up: share or map legal transitions by current status, disable all status mutations while one is owned or use per-row operation tokens, consume the canonical response, and add rejection plus valid-transition assertions.

### F-08 [P2] Subscription rows omit fields needed to identify plan policy

- Rows/routes: `ADM-03`, `/admin/subscriptions`.
- Independent cause/contract: the API supplies audience and Playlist limits, but the read-only table drops them.
- UI lane: `SubscriptionManagePage.tsx:64-75` renders name, monthly/yearly price, daily download, whitelist channels, and active state only.
- Frontend/server lane: `SubscriptionResponse.java:8-17` includes `userType` and `maxPlaylists`; the frontend plan type carries the same fields.
- Test lane: `adminSubscriberPages.coverage.test.tsx:343-367` fixtures include both fields but only assert active/inactive/unlimited values, so omission passes.
- Durable-state lane: read-only.
- Impact: plans with the same name across INDIVIDUAL/BUSINESS audiences are visually indistinguishable and one contracted limit is hidden.
- Bounded follow-up: add audience and Playlist-limit columns/labels and assert duplicate-name cross-audience fixtures render distinguishably.

### F-09 [P2] Raw ADMIN modals do not own pending mutations

- Rows/routes: `ADM-02`, `ADM-06`, `ADM-07`, `ADM-08`, and `SH-07`.
- Independent cause/contract: shared `Modal` always closes on Escape, backdrop, and header close (`Modal.tsx:26-32`, `:109-138`), while several owner pages leave `onClose` and cancel active during pending work.
- UI/frontend lane: User role modal close/cancel is unguarded at `UserManagePage.tsx:286-346`; Tag form/delete close is unguarded at `TagManagePage.tsx:258-344`; Track disables its cancel button but not Modal close/Escape/backdrop at `TrackManagePage.tsx:227-245`; Company review close/cancel is unguarded at `CompanyCertManagePage.tsx:402-466`.
- Invocation lane: late success commonly clears current modal state; Company review then performs an unfenced old-detail refresh at `CompanyCertManagePage.tsx:175-207`.
- Server lane: the original request may commit correctly; this finding concerns ownership of its late result.
- Test lane: `ConfirmDialog.test.tsx` covers busy cancellation for the shared wrapper, but these raw Modal occurrences do not inherit it and lack close/retarget-during-pending assertions.
- Durable-state lane: the first target can commit while the visible modal has been closed or retargeted.
- Impact: a late operation can close a newly opened target, attach an error to the wrong context, or overwrite a newer certification detail.
- Bounded follow-up: bind each mutation to an immutable target/generation, block all close mechanisms while pending or deliberately detach the result, and add Escape/backdrop/header/retarget race tests per owner pattern.

### F-10 [P2] Tag deletion copy omits its destructive dependency effect

- Rows/routes: `ADM-07`, `/admin/tags`, `SH-07`.
- Independent cause/contract: the confirmation names only the Tag, while the service deletes every Track association first.
- UI lane: `TagManagePage.tsx:321-342` asks only whether to delete the named Tag.
- Frontend invocation lane: one DELETE request is made after confirmation.
- Server/durable-state lane: `TagService.java:163-167` calls `trackTagRepository.deleteAllByTag(tag)` and then deletes the Tag.
- Test lane: `TagServiceTest.java:241-245` explicitly asserts both deletions; the frontend tests do not assert association-impact copy.
- Impact: operators cannot distinguish deletion of an unused Tag from removal of a taxonomy value already attached to Tracks.
- Bounded follow-up: expose a bounded usage count or explicit association-removal consequence before confirmation and assert the copy for used/unused fixtures.

### F-11 [P2] Local subscription correction execution lacks the required typed phrase

- Rows/routes: `ADM-09`, `/admin/user-subscriptions`, `SH-07`.
- Independent cause/contract: the matrix explicitly assigns typed confirmation to execution.
- UI/frontend lane: `UserSubscriptionCorrectionModal.tsx:1094-1109` uses `ConfirmDialog` with a normal confirm button; `ConfirmDialog.tsx:5-15` has no typed-value contract.
- Server lane: request/approve/execute remains ADMIN-only, revalidates locked snapshots, writes audit, and does not call Toss. Unknown execution recovery is implemented separately and is not disputed here.
- Test lane: the test titled “explicit cancellable confirmations” at `UserSubscriptionManagePage.test.tsx:548-630` cancels and clicks a plain button; it never enters or validates a phrase. Response-loss reconciliation is positively asserted at `:791-868`.
- Durable-state lane: execution changes local Subscription/Billing Agreement state.
- Impact: the highest-impact local correction step does not meet its deliberate operator-friction contract.
- Bounded follow-up: require exact normalized phrase input for execute only, retain current pending/Escape/focus behavior, and add wrong/correct phrase plus duplicate-pending tests.

### F-12 [P2] Settings save can claim success for text that was never sent

- Rows/routes: `ADM-14`, `/admin/settings`, public Company Certification guide.
- Independent cause/contract: the textarea remains editable while save owns an older value, and success does not reload canonical state.
- UI/frontend lane: `SiteSettingsPage.tsx:34-43` sends the current render's value and only toasts success; `:73-83` leaves the textarea enabled while the button is loading.
- Server lane: public missing-key read returns an editable empty string at `SettingController.java:20-26`; ADMIN PUT is role-protected at `AdminSettingController.java:19-25`; `SiteSettingService.java:29-38` updates or inserts.
- Consumer lane: `CompanyCertApplyPage.tsx:40-45` reads the same `COMPANY_CERT_GUIDE` key.
- Test lane: `adminSubscriberPages.coverage.test.tsx:324-340` asserts one mocked save and a manual reset, not edit-during-save or public-consumer canonical reload. `:566-578` covers generic failures. No backend Setting controller/service test exists.
- Durable-state lane: the database may contain value A while the success screen displays unsaved value B.
- Impact: an operator can reasonably believe the visible guidance was published when only an earlier value persisted.
- Bounded follow-up: freeze the field or version the draft during save, consume/refresh canonical data after success, and add backend upsert plus ADMIN/public cross-surface tests.

### F-13 [P2][BLOCKED-CONTRACT] Dashboard total count is undefined

- Rows/routes: `ADM-01`, `/admin/dashboard`.
- Independent cause/contract: the executable matrix requires four totals, while all active layers define three and do not name a fourth.
- UI/frontend lane: `DashboardPage.tsx:79-90` renders Users, Tracks, and Subscribers; `frontend/src/api/admin.ts:13-18` defines those three fields.
- Server lane: `AdminStatsService.java:25-35` computes those three repositories, and `DashboardStatsResponse.java:7-12` contains only those fields plus recent Users.
- Test lane: `DashboardPage.test.tsx:11-58` fixtures and assertions use the same three totals and cover retry/empty behavior.
- Durable-state lane: read-only aggregates.
- Contract conflict: `WI-20260809-ATS-020-acceptance-matrix.md:199` says “four totals” without defining the fourth.
- Impact: acceptance cannot determine whether implementation or matrix is incomplete.
- Bounded follow-up: main must identify the intended fourth aggregate or correct the matrix to three before any implementation change.

### F-14 [P2][BLOCKED-CONTRACT] ADMIN reconciliation GET has contradictory Incident semantics

- Rows/routes: `ADM-10`; support `GET /api/admin/payments/reconciliation`; payment reconciliation scheduler.
- Independent cause/contract: the matrix calls the endpoint a trigger and requires a durable Incident result, while the newer runbook, implementation, and test make it observation-only.
- UI lane: there is no current SPA control; the Incident tab does not invoke this endpoint.
- Server lane: `AdminPaymentController.java:253-257` delegates to `AdminPaymentReadService`; `AdminPaymentReadService.java:74-82` uses non-transactional local observation and `diagnoseProviderLedger()`.
- Test lane: `AdminPaymentReadServiceTest.java:39-56` verifies observations and explicitly verifies `reconcileProviderLedger()` is never called.
- Durable-state lane: the endpoint does not create, update, resolve, or reopen Incidents. Scheduled reconciliation owns those writes.
- Contract conflict: `payment-operations-runbook.md:72-78` and `:139-143` reserve Incident persistence for the scheduler; `WI-20260809-ATS-020-acceptance-matrix.md:539` requires a durable Incident result from the GET.
- Impact: one endpoint cannot satisfy both read-only safety and durable-trigger acceptance simultaneously.
- Bounded follow-up: main must choose the authoritative contract. The bounded recommendation is to preserve the current read-only GET and correct the matrix wording, unless an explicitly approved POST/operator mutation with audit and idempotency is introduced separately.

## 4. Confirmed Controls and API-only Classification

- ADMIN routing is centralized through `ProtectedRoute` with `minRole="ADMIN"` at `frontend/src/router/index.tsx:118-120`; assigned routes are registered at `:211-236`. Anonymous return targets remain local path/search at `ProtectedRoute.tsx:54-60`. `AdminLayout.tsx:13-27` exposes the assigned navigation entries.
- Dashboard load ownership and retry are generation-safe (`DashboardPage.tsx:13-44`; `DashboardPage.test.tsx:31-58`).
- User role service locks/revalidates the actor and target, rejects self/last/stale ADMIN mutations, and writes success/rejection audit; the defect is the frontend stale-session lane, not the server authority lane.
- Company Certification list/detail requests use request IDs, backend review locks state, document DTOs omit storage paths, and source tests assert audit/privacy. Private document bytes were not accessed.
- ADMIN local subscription correction has persisted resumable state, stale snapshot revalidation, separate approval/execution, no Toss call, idempotent terminal behavior, and bounded ambiguous-response recovery. F-11 is limited to typed operator confirmation.
- Payment tab loads use AbortController/generation ownership, receipt links reject unsafe protocols, and backend refund/correction/incident/settlement services use dedicated ledgers and audit. Test Provider/H2 evidence is isolated and never represented as live proof.
- Whitelist source/tests use legal transition mapping, stable lock order, immutable export snapshots, byte-stable replay, and spreadsheet-formula neutralization. CSV bytes/download behavior was not executed.
- `GET /api/admin/tracks/audio-analysis/dry-run` is intentional API-only operator support: ADMIN-only, page/size bounded before repository/file access, deterministic report-only behavior, active/inactive reporting, and no entity save/update assertion.
- `GET /api/users/{userId}/licenses/{licenseId}` is intentional API-only ADMIN detail support for owner-scoped lookup. Its absence from the current list-by-User UI is not, by itself, a missing product surface.
- Refund/correction detail APIs are not merely optional API-only support because they are the available authoritative reads for F-01's committed-response-loss boundary.
- `fetchAdminSubscriptionCorrections` has no non-test importer. It is not evidence of a visible history product surface.

## 5. Automated Evidence Obtained Before Checkpoint

### Frontend targeted tests

Exact command:

```powershell
npm test -- src/router/ProtectedRoute.test.tsx src/router/index.test.tsx src/components/ui/Modal.test.tsx src/components/ui/ConfirmDialog.test.tsx src/api/adminContracts.test.ts src/api/adminWhitelistChannels.test.ts src/pages/admin/DashboardPage.test.tsx src/pages/admin/UserManagePage.test.tsx src/pages/admin/TagManagePage.test.tsx src/pages/admin/CompanyCertManagePage.test.tsx src/pages/admin/PaymentOperationsPage.test.tsx src/pages/admin/UserSubscriptionManagePage.test.tsx src/pages/admin/WhitelistChannelManagePage.test.ts src/pages/admin/WhitelistChannelManagePage.render.test.tsx src/pages/subscriber/CompanyCertApplyPage.test.tsx src/test/coverage/adminSubscriberPages.coverage.test.tsx src/test/coverage/adminSubscriberGaps.coverage.test.tsx src/test/coverage/shellCatalogRouterGaps.coverage.test.tsx
```

- Result: PASS, exit `0`.
- Count: 18 test files passed; 181 tests passed; 0 failed/skipped reported.
- Vitest duration: `10.75s`; wrapper wall duration: `11.887s`.
- Warning: none reported.
- Assertion caveat: passing tests also preserve defects documented in F-04, F-07, F-08, and F-11; count alone is not acceptance proof.

### Frontend typecheck

Exact command: `npm run typecheck`

- Result: PASS, exit `0`; `tsc --noEmit` emitted no diagnostics.
- Wrapper wall duration: `6.374s`.
- Warning: none reported.

### Targeted ESLint

Exact command:

```powershell
npx eslint --max-warnings 0 src/router/index.tsx src/router/ProtectedRoute.tsx src/layouts/AdminLayout.tsx src/components/ui/Modal.tsx src/components/ui/ConfirmDialog.tsx src/api/admin.ts src/api/settings.ts src/api/questions.ts src/api/licenses.ts src/api/tags.ts src/api/tracks.ts src/api/subscriptions.ts src/api/userSubscriptions.ts "src/pages/admin/**/*.{ts,tsx}" src/pages/subscriber/CompanyCertApplyPage.tsx
```

- Result: PASS, exit `0`; 0 warnings because `--max-warnings 0` completed successfully.
- Wrapper wall duration: `3.236s`.

### Backend targeted tests

Exact command:

```powershell
.\gradlew.bat test --rerun-tasks --tests "com.atstudio.atstudio.controller.AdminPaymentControllerTest" --tests "com.atstudio.atstudio.controller.AdminTrackAudioAnalysisControllerTest" --tests "com.atstudio.atstudio.controller.AdminUserSubscriptionCorrectionControllerTest" --tests "com.atstudio.atstudio.controller.AdminWhitelistChannelControllerTest" --tests "com.atstudio.atstudio.controller.UserControllerTest" --tests "com.atstudio.atstudio.controller.SubscriptionControllerTest" --tests "com.atstudio.atstudio.controller.LicenseControllerTest" --tests "com.atstudio.atstudio.controller.QuestionControllerTest" --tests "com.atstudio.atstudio.controller.CompanyCertificationControllerTest" --tests "com.atstudio.atstudio.controller.TagControllerTest" --tests "com.atstudio.atstudio.controller.TrackControllerTest" --tests "com.atstudio.atstudio.service.AdminPaymentReadServiceTest" --tests "com.atstudio.atstudio.service.AdminPaymentIncidentServiceTest" --tests "com.atstudio.atstudio.service.AdminPaymentRefundServiceTest" --tests "com.atstudio.atstudio.service.AdminPaymentEntitlementCorrectionServiceTest" --tests "com.atstudio.atstudio.service.AdminPaymentSettlementServiceTest" --tests "com.atstudio.atstudio.service.AdminSubscriptionCorrectionServiceTest" --tests "com.atstudio.atstudio.service.AdminWhitelistChannelServiceTest" --tests "com.atstudio.atstudio.service.AdminTrackAudioAnalysisServiceTest" --tests "com.atstudio.atstudio.service.AdminOperationAuditTransactionIntegrationTest" --tests "com.atstudio.atstudio.service.PaymentReconciliationServiceTest" --tests "com.atstudio.atstudio.service.PaymentReconciliationServiceEdgeCaseTest" --tests "com.atstudio.atstudio.service.PaymentReconciliationTransactionServiceTest" --tests "com.atstudio.atstudio.service.PaymentReconciliationRecoveryIntegrationTest" --tests "com.atstudio.atstudio.service.PaymentReconciliationIncidentServiceTest" --tests "com.atstudio.atstudio.service.PaymentRefundResilienceIntegrationTest" --tests "com.atstudio.atstudio.service.PaymentReceiptEvidenceServiceTest" --tests "com.atstudio.atstudio.service.PaymentOperationAuditLogServiceTest" --tests "com.atstudio.atstudio.service.WithdrawalBillingCleanupServiceTest" --tests "com.atstudio.atstudio.service.WithdrawalBillingCleanupCoordinatorTest" --tests "com.atstudio.atstudio.service.WithdrawalBillingCleanupTransactionIntegrationTest" --tests "com.atstudio.atstudio.service.BillingAgreementCleanupProviderExecutorTest" --tests "com.atstudio.atstudio.service.BillingAgreementFailurePersistenceIntegrationTest" --tests "com.atstudio.atstudio.service.BillingAgreementCancellationTransactionIntegrationTest" --tests "com.atstudio.atstudio.repository.BillingAgreementRepositoryTest" --tests "com.atstudio.atstudio.service.CompanyCertificationServiceTest" --tests "com.atstudio.atstudio.service.CompanyCertificationSecurityVerificationTest" --tests "com.atstudio.atstudio.service.UserServiceTest" --tests "com.atstudio.atstudio.service.SubscriptionServiceTest" --tests "com.atstudio.atstudio.service.LicenseServiceTest" --tests "com.atstudio.atstudio.service.QuestionServiceTest" --tests "com.atstudio.atstudio.service.TagServiceTest" --tests "com.atstudio.atstudio.service.TrackServiceTest" --tests "com.atstudio.atstudio.service.WhitelistChannelServiceTest"
```

- Result: PASS, exit `0`; `BUILD SUCCESSFUL in 1m 17s`.
- Selection count: 44 explicit test-class filters; 5 actionable Gradle tasks, all executed because `--rerun-tasks` was used.
- Test-case count: 63 `TEST-*.xml` suites; 554 tests, 0 failures, 0 errors, and 0 skipped.
- Aggregate method: main read the generated `build/test-results/test` files and regexed the `testsuite` count attributes read-only. This method was used because encoding-sensitive/malformed suite-name text invalidated PowerShell XML parsing; only the successful attribute aggregate is retained here.
- Wrapper wall duration: `77.361s`.
- Environment boundary: local isolated test contexts/H2 and test doubles only; no live database or Provider evidence.
- Warnings: Java compiler reported unchecked/unsafe operations and suggested `-Xlint:unchecked`; JVM reported CDS sharing limited to boot-loader classes because the bootstrap classpath was appended; Gradle suggested configuration cache and emitted an incubating problems-report location.

### Main supplemental anonymous browser guards

- Result: PASS for all 12 assigned anonymous ADMIN routes.
- `/admin/dashboard?from=audit` -> `/login?returnTo=%2Fadmin%2Fdashboard%3Ffrom%3Daudit`
- `/admin/users?from=audit` -> `/login?returnTo=%2Fadmin%2Fusers%3Ffrom%3Daudit`
- `/admin/subscriptions?from=audit` -> `/login?returnTo=%2Fadmin%2Fsubscriptions%3Ffrom%3Daudit`
- `/admin/licenses?from=audit` -> `/login?returnTo=%2Fadmin%2Flicenses%3Ffrom%3Daudit`
- `/admin/questions?from=audit` -> `/login?returnTo=%2Fadmin%2Fquestions%3Ffrom%3Daudit`
- `/admin/company-certifications?from=audit` -> `/login?returnTo=%2Fadmin%2Fcompany-certifications%3Ffrom%3Daudit`
- `/admin/tags?from=audit` -> `/login?returnTo=%2Fadmin%2Ftags%3Ffrom%3Daudit`
- `/admin/track-manage?from=audit` -> `/login?returnTo=%2Fadmin%2Ftrack-manage%3Ffrom%3Daudit`
- `/admin/user-subscriptions?from=audit` -> `/login?returnTo=%2Fadmin%2Fuser-subscriptions%3Ffrom%3Daudit`
- `/admin/payments?from=audit` -> `/login?returnTo=%2Fadmin%2Fpayments%3Ffrom%3Daudit`
- `/admin/whitelist-channels?from=audit` -> `/login?returnTo=%2Fadmin%2Fwhitelist-channels%3Ffrom%3Daudit`
- `/admin/settings?from=audit` -> `/login?returnTo=%2Fadmin%2Fsettings%3Ffrom%3Daudit`
- Restoration: browser returned to `http://127.0.0.1:5173/`.
- Neutral state: 0 dialogs, 0 file inputs, `BODY` active element, and no horizontal overflow.
- Boundary: no authenticated/ADMIN API call or mutation was made.

## 6. Checkpoint Conclusion

- Assigned bounded source/test coverage is complete for every requested row and support/scheduler item.
- Confirmed findings: 3 P1, 11 P2; F-02, F-03, F-13, and F-14 require contract or policy resolution before implementation.
- Anonymous route-guard and neutral restoration evidence is complete through main. Authenticated ADMIN browser, live Provider/database, mutation, and binary acceptance claims remain blocked or out of scope.
- The assignee did not re-explore source or rerun tests for this supplement. Main's read-only XML aggregation and anonymous browser evidence were incorporated as provided; no document validation, formatting check, or broader exploration was started.

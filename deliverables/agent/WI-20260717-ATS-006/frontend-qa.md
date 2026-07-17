# Frontend QA Report: WI-20260717-ATS-006

## Decision

**FAIL** - 0 P1, 6 P2, and 4 P3 findings.

The frontend compiles, lints, formats, tests, and builds successfully. However, the
repository coverage gate is not met, and independently reproduced defects remain in
download selection, refund targeting, authentication persistence, player rehydration,
and recurring-payment callback handling. WI-20260717-ATS-006 should not close while
the P2 findings remain unresolved.

## Scope and Inputs

This was an independent, read-only review of the frontend implementation. The only
created file is this report.

Reviewed inputs included:

- `deliverables/agent/WI-20260717-ATS-006-handoff.md`
- `deliverables/user/REQ-20260716-ATS-004.md`
- WI-20260717-ATS-001 through WI-20260717-ATS-005 evidence packs
- Tier 0 standards and frontend/security/quality/payment policies
- Current React source and tests for routing, authentication, OAuth/PKCE, storage,
  playback, history, checkout, downloads, whitelist workflows, and admin payments
- The current working-tree diff relevant to the frontend track

## Findings

### P2-01 - Selected re-download sends download IDs as track IDs

**Pointers:**

- `frontend/src/pages/download/DownloadHistoryPage.tsx:51`
- `frontend/src/pages/download/DownloadHistoryPage.tsx:193-205`
- `frontend/src/pages/download/DownloadHistoryPage.tsx:226-229`
- `frontend/src/pages/download/DownloadHistoryPage.tsx:252-265`
- `frontend/src/pages/download/DownloadHistoryPage.test.tsx:154-175`

`selectedIds` is populated with `item.downloadId`, but `handleDownloadSelected`
passes those values to `downloadByTrackIds`, which treats each value as a `trackId`
and calls `downloadTrack(trackId)`.

**Reproduction:** Render a history item with `downloadId: 1` and `trackId: 101`,
select it, and click the selected re-download action. The client requests
`GET /api/tracks/1/download` instead of `GET /api/tracks/101/download`.

The existing selection test checks only the deduplicated label/count and never
executes the selected download action. Store track IDs for this action, or map the
selected download IDs back to their corresponding track IDs, then add a request-level
regression test.

### P2-02 - A stale refund preview can create a request for the wrong payment

**Pointers:**

- `frontend/src/pages/admin/PaymentOperationsPage.tsx:526-557`
- `frontend/src/pages/admin/PaymentOperationsPage.tsx:862-864`
- `frontend/src/pages/admin/PaymentOperationsPage.test.tsx:462-500`

Changing the refund payment ID does not invalidate `refundPreview`. A subsequent
create action is gated by the old preview and submits
`refundPreview.subscriptionPaymentId`, not the payment ID currently displayed in the
input. Preview failure also leaves the prior successful preview active.

**Reproduction:** Preview payment `41`, change the payment ID input to `42`, then
create the refund request without previewing again. The request still targets payment
`41` while the UI displays `42`.

This is a wrong-target financial operation. Clear the preview whenever any preview
input changes or a preview request fails, and require an exact input-to-preview match
before creation. Add edit-after-preview and failed-repreview tests.

### P2-03 - Password login reports success when credential persistence fails

**Pointers:**

- `frontend/src/utils/safeStorage.ts:1-22`
- `frontend/src/stores/authStore.ts:52-62`
- `frontend/src/api/client.ts:16-19`
- `frontend/src/pages/auth/LoginPage.tsx:132-153`

`safeStorage.setItem` reports failure with `false`, but password login ignores both
write results and installs an authenticated in-memory state. The Axios interceptor
reads its bearer token only from storage, so the next API request has no
`Authorization` header.

**Reproduction:** Make `Storage.prototype.setItem` throw (for example, disabled
storage or quota failure), then complete a successful password login and `fetchMe`.
The page navigates as authenticated, but the next protected request is sent without a
token and receives a 401.

Treat token persistence failure as an authentication failure or provide a coherent
in-memory token path for the API client. Add a password-login storage-failure test.

### P2-04 - Persisted player state is not rehydrated into the audio element

**Pointers:**

- `frontend/src/stores/playerStore.ts:41-50`
- `frontend/src/stores/playerStore.ts:222-238`
- `frontend/src/stores/playerStore.ts:269-271`

The store restores `currentTrack`, `currentTime`, and queue state from storage, but it
does not restore `audio.src` or `audio.currentTime`. `resume` only calls
`audio.play()`. After reload, the visible player can therefore show a track and saved
progress that do not exist in the actual audio element.

**Reproduction:** Play a track, seek forward, reload, and press play on the restored
player. The newly constructed audio element has no corresponding source/progress, so
playback fails or restarts incoherently while the UI presents the persisted state.

Either rehydrate the audio source and time before allowing resume, or clear the
non-resumable visible playback state. Add a reload/rehydration test covering source,
progress, and first resume.

### P2-05 - Successful recurring checkout callbacks can be replayed with browser Back

**Pointers:**

- `frontend/src/pages/subscription/SubscriptionPaymentPage.tsx:34`
- `frontend/src/pages/subscription/SubscriptionPaymentPage.tsx:71-84`
- `frontend/src/pages/subscription/SubscriptionPaymentPage.test.tsx:222-252`

The in-component ref prevents duplicate confirmation only within one mounted
instance. Successful confirmation navigates away with a push, leaving the callback
URL in browser history. Going Back remounts the page, resets the ref, and submits the
same recurring confirmation again.

**Reproduction:** Complete a successful billing callback, wait for navigation to the
subscription page, then use browser Back. The callback route mounts again and calls
the confirmation API a second time.

Replace the callback history entry after terminal handling and add a back-navigation
replay test. Backend idempotency remains necessary, but it does not remove the
frontend single-submit requirement.

### P2-06 - Vitest coverage is below the repository quality gate

**Pointers:**

- `docs/standards/development-standards.md:580-598`
- `docs/standards/development-standards.md:819-829`
- `frontend/vitest.config.ts`

The coverage command passes because no enforcing threshold is configured, but the
measured totals are below the repository minimum of 80% lines/statements/functions
and 70% branches:

| Metric     |           Measured | Required |
| ---------- | -----------------: | -------: |
| Statements | 40.51% (2824/6970) |      80% |
| Branches   | 40.29% (1867/4633) |      70% |
| Functions  |  34.23% (643/1878) |      80% |
| Lines      | 41.84% (2619/6259) |      80% |

Critical gaps include 0% line coverage for `WaveformCanvas.tsx`, `HistoryModal.tsx`,
and `PlayHistoryPage.tsx`. Authentication, payment, download, and player files also
remain below the critical-path expectations. Configure executable thresholds and add
focused behavior tests before treating coverage as a passed release gate.

### P3-01 - Valid JSON with an invalid persisted shape can crash history/player views

**Pointers:**

- `frontend/src/stores/playerStore.ts:41-47`
- `frontend/src/stores/playerStore.ts:82-88`
- `frontend/src/pages/history/PlayHistoryPage.tsx:37-42`
- `frontend/src/components/player/HistoryModal.tsx:24-25`
- `frontend/src/components/player/HistoryModal.tsx:61-71`

The load fallback handles JSON parse failures but casts parsed values without shape
validation. For example, setting `playHistory` to `{}` is valid JSON but causes
`.slice`, `.length`, or `.map` failures when the history page/modal renders. Invalid
queue/current-track shapes can similarly poison player state.

Validate persisted schemas and fall back field-by-field to known defaults. Add tests
for valid JSON containing wrong types, missing fields, and obsolete versions.

### P3-02 - A recurring success callback missing `amount` is accepted as amount zero

**Pointer:** `frontend/src/pages/subscription/SubscriptionPaymentPage.tsx:62-63`

`Number(searchParams.get('amount'))` converts a missing query parameter (`null`) to
`0`, and the finite-number check accepts it. A malformed success callback therefore
reaches confirmation with amount zero instead of failing closed.

Require the raw parameter to be present and validate its expected numeric domain
before conversion. Add missing, empty, negative, and non-integer amount tests.

### P3-03 - A transient Toss SDK load failure cannot be retried without reloading

**Pointers:**

- `frontend/src/utils/tossPayments.ts:41`
- `frontend/src/utils/tossPayments.ts:50-85`

The module memoizes `sdkPromise`, but a rejected promise is never cleared and a failed
script element is not reset. Every subsequent checkout attempt receives the same
rejection until the whole page reloads.

Clear the memoized promise and failed script state on rejection, then add a
fail-once/succeed-on-retry loader test.

### P3-04 - Download-history selection and thumbnail playback are not keyboard accessible

**Pointers:**

- `frontend/src/pages/download/DownloadHistoryPage.tsx:388-417`
- `frontend/src/pages/download/DownloadHistoryPage.test.tsx:154-175`

The playable thumbnail is a click-only `div` without button semantics, keyboard
handling, or focusability. The select-all and row checkboxes also have no accessible
name; tests locate them by ordinal position rather than role and name.

Use a semantic button for playback and provide associated labels or `aria-label`
values for selection controls. Add keyboard and accessible-name assertions.

## Quality Gate Results

| Gate                      | Result   | Evidence                                                                   |
| ------------------------- | -------- | -------------------------------------------------------------------------- |
| TypeScript                | PASS     | `npm run typecheck`; 0 errors                                              |
| ESLint                    | PASS     | `npm run lint`; 0 errors, 0 warnings                                       |
| Prettier                  | PASS     | `npm run format`; all matched files formatted                              |
| Vitest coverage execution | PASS     | 45 files, 255 tests; report generated in a temporary directory and removed |
| Coverage policy           | **FAIL** | Totals below repository thresholds; see P2-06                              |
| Full Vitest               | PASS     | `npm test`; 45 files, 255 tests                                            |
| Production build          | PASS     | `npm run build -- --outDir <temporary-directory>`; 266 modules transformed |

The production bundle completed with Vite 6.4.3. The main JS output was 341.10 kB
(111.15 kB gzip). Temporary coverage/build directories were removed after inspection.

## Negative Search Results

Exact searches over `frontend/src` returned no matches for:

- Removed server play-history API clients, endpoints, and types
- Removed download-queue API clients, endpoints, page identity, and symbols
- `DataTable`
- Removed exports: `fetchUser`, `addTracksToPlaylistBatch`,
  `fetchSubscriptionPlanDetail`, `fetchAdminUserSubscriptionDetail`,
  `cancelMyBillingAgreement`, and `PaymentProvider`
- Removed playlist route `/playlists/new`
- Old payment aliases `/subscriptions/payment` and
  `/subscriptions/billing/(success|fail)`
- Removed page identities `PaymentReadOnlyPage` and `DownloadQueuePage`
- Native `confirm` calls in production code
- Mock-payment CSS identities
- Production `@ts-ignore`, `@ts-expect-error`, and explicit `any`

The only native prompt retained is the intentional typed emergency-execution prompt
in the admin payment page.

## Independently Verified Behaviors

No additional findings were identified in these areas:

- `ProtectedRoute` role/user-type enforcement, safe return target, and maximum-role
  behavior
- `SubscriberRoute` subscription enforcement, load error/retry, and stale-request
  handling; `/downloads` is subscriber guarded and checkout is USER-only
- OAuth/PKCE secure state, S256 challenge, one-time attempt consumption, expiration,
  safe return URLs, staged social tokens, cleanup, and failure handling
- Initial full-track stream playback, progress/seek, waveform mapping, local-history
  recording, stalled playback, and media-error handling, subject to P2-04/P3-01
- Standard recurring prepare/auth/success/failure and re-registration paths, subject
  to P2-05/P3-02/P3-03
- Download-history loading, latest-request behavior, and all-download confirmation,
  subject to P2-01/P3-04
- Subscriber/admin whitelist confirmations, pending-state single-submit behavior,
  failure handling, safe URLs, and modal focus trap/restore
- Admin payment latest-request behavior, masked/safe receipt references, settlement,
  correction, and typed execution confirmation, subject to P2-02

## Workspace Integrity

- No product, documentation, configuration, database, or Git operation was performed.
- No file was reverted.
- `frontend/tsconfig.tsbuildinfo` remains ignored and absent from the index; its local
  cache content may have been refreshed by `tsc -b` as permitted by the handoff.
- This report is the only QA-owned deliverable created by this review.

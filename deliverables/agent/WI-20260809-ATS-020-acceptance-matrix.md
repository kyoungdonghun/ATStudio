# WI-20260809-ATS-020 Master Acceptance Matrix

## 1. Baseline and Coverage Contract

This matrix is the executable test design for the UI/UX audit approved by
`REQ-20260809-ATS-001`. It is based on commit `e343c20` on
`codex/v1-release-rehearsal-fixes` and the declaration inventory in
`WI-20260809-ATS-019`.

| Counted unit                           |       Required | Matrix location                               |
| -------------------------------------- | -------------: | --------------------------------------------- |
| Distinct visual page UIs               |             53 | Sections 5 through 10                         |
| Path-bearing routes                    |             56 | Page rows plus callback variants in Section 7 |
| Admin index redirect                   |              1 | `G-ADM-INDEX` in Section 4                    |
| Shared shell/player/dialog surfaces    |       7 groups | Section 11                                    |
| Current Modal render occurrences       | 22 in 17 files | Section 12                                    |
| Same-behavior/different-entry families |              8 | Section 13                                    |
| High-risk state-machine families       |              8 | Section 14                                    |

The matrix does not mark a screen as passing. It defines what must be observed
before a downstream browser WI may mark it `PASS`, `FAIL`, `BLOCKED`, or `N/A`.

## 2. Fixture Vocabulary

### 2.1 Roles and Account States

| Code   | Fixture                                                                     |
| ------ | --------------------------------------------------------------------------- |
| `G`    | Anonymous browser with no AT.M session                                      |
| `U-I0` | Individual USER with no active Subscription                                 |
| `U-IA` | Individual USER with active, auto-renewing Subscription                     |
| `U-IC` | Individual USER cancelled during paid access/grace period                   |
| `U-IP` | Individual USER with pending downgrade or cycle change                      |
| `U-B0` | BUSINESS USER without approved Company Certification or active Subscription |
| `U-BA` | Approved BUSINESS USER with active Subscription                             |
| `A`    | ADMIN                                                                       |

Real credentials and secrets must not be stored in this matrix. A browser WI
records only a fixture alias and sanitized User ID.

### 2.2 Data States

| Code | State                                                                                |
| ---- | ------------------------------------------------------------------------------------ |
| `D0` | Empty collection or absent optional record                                           |
| `D1` | One valid item with all optional media/metadata                                      |
| `D2` | Multiple items spanning pagination, ordering, and limits                             |
| `D3` | Valid item with nullable thumbnail/waveform/attachment fields omitted                |
| `D4` | Inactive, deleted, foreign-owned, or missing identifier                              |
| `D5` | Boundary input: minimum, maximum, Unicode/Korean, whitespace, and long unbroken text |

### 2.3 Viewports

| Code | Viewport   | Primary purpose                                           |
| ---- | ---------- | --------------------------------------------------------- |
| `VD` | 1440 x 900 | Normal desktop layout and dense admin tables              |
| `VN` | 1024 x 768 | Narrow desktop/tablet transition and horizontal overflow  |
| `VM` | 390 x 844  | Common mobile layout, fixed PlayerBar, dialogs, and menus |
| `VS` | 360 x 800  | Long Korean labels and smallest supported practical width |

Every visual row receives `VD`, `VM`, and keyboard coverage. `VN` is mandatory
for tables, grids, sidebars, multi-column forms, and PlayerBar. `VS` is mandatory
for long labels, confirmation copy, cards, and fixed controls.

## 3. Reusable State and Evidence Packs

### 3.1 State Packs

| Pack                       | Required checks and expected result                                                                                                                                                                                                                                                                                      |
| -------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| `L` Load                   | Stable loading state; success replaces it; `D0` has a deliberate empty state; a failed request leaves a readable error and a bounded retry or safe navigation path; stale earlier responses cannot overwrite a newer selection/filter/page.                                                                              |
| `G` Guard                  | Authorized role renders the target; anonymous users go to Login when required; insufficient USER/ADMIN/type/subscription states go to the documented safe target; return targets cannot become external or protocol-relative redirects.                                                                                  |
| `F` Form                   | Required, format, length, duplicate, file type/size, and cross-field checks occur before mutation where locally decidable; field errors stay near the field; entered values survive server rejection; submit is disabled while pending.                                                                                  |
| `C` Collection             | `D0`, `D1`, `D2`, pagination/filter/sort, stable row identity, and latest-request-wins behavior; no duplicated, missing, or stale rows after mutation.                                                                                                                                                                   |
| `M` Mutation               | The action name and confirmation describe the business effect; one user action creates at most one request; success refreshes canonical state; HTTP 4xx is a definite rejection; timeout/no-response/HTTP 5xx is treated as unknown when the server may have committed and is reconciled by a read before retry.         |
| `X` Destructive            | Consequence and target are explicit; cancel is safe; focus returns to the trigger; success removes only the target; failure preserves context; irreversible or external actions use typed confirmation where the current contract requires it.                                                                           |
| `P` Playback               | Play/pause, seek, waveform progress, duration, previous/next, queue, shuffle/repeat, volume/mute, buffering after two seconds, failure/retry, source switch, persisted ID hydration, nullable media, and mobile expansion remain coherent. Full Track playback is the product policy; no preview truncation is expected. |
| `B` Binary/file            | Download/export/import yields the expected status, filename/type, and non-empty bytes; parsed content matches API/DB state; formula-risk CSV cells are neutralized; private files do not expose storage paths; cancellation/error leaves no false success.                                                               |
| `R` Reload/interruption    | Refresh, back/forward, route departure during load, reopen, and resume produce canonical state without duplicate mutation, stale banners, trapped dialogs, or lost recoverable drafts.                                                                                                                                   |
| `K` Keyboard/accessibility | Logical tab order, visible focus, Enter/Space activation, Escape/close behavior, dialog focus containment/restoration, labels/names, status/error announcement, and no keyboard-only dead end.                                                                                                                           |
| `V` Visual/responsive      | No overlap, clipping, unintended horizontal page scroll, hidden action, unreadable contrast, or fixed Header/PlayerBar occlusion across assigned viewports; long text and empty/error/loading content do not resize fixed-format controls incoherently.                                                                  |

### 3.2 Evidence Levels

| Code | Evidence                                                                    |
| ---- | --------------------------------------------------------------------------- |
| `E1` | Screenshot/DOM assertion plus browser console state                         |
| `E2` | Sanitized network request, response status, and response shape              |
| `E3` | Independent API read or page reload proving canonical server state          |
| `E4` | Sanitized DB row/count/state-transition evidence                            |
| `E5` | Downloaded artifact parsed and compared with source rows or stored snapshot |
| `E6` | Toss test-provider or approved mail-receiver evidence, with secrets removed |

### 3.3 Side-Effect Classes

| Code | Meaning                                                                                                                                            | Execution rule                                                                           |
| ---- | -------------------------------------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------- |
| `S0` | Read-only navigation/render/request                                                                                                                | May execute automatically.                                                               |
| `S1` | Browser-local reversible state                                                                                                                     | May execute; restore local state after the scenario.                                     |
| `S2` | Reversible mutation in dedicated local/UAT test data                                                                                               | May execute under the approved REQ; record cleanup and verify it.                        |
| `S3` | Sandbox provider, generated email, file upload/download, or controlled external test receiver                                                      | Execute only with test mode and approved fixture; never use a real charge/refund/cancel. |
| `S4` | Real provider money movement, production/external user impact, destructive schema/data operation, secret change, deployment, or branch destruction | Stop and request explicit user approval.                                                 |

### 3.4 Required Evidence by Action

- Pure display/navigation: `E1`; add `E2` when data-backed.
- Reversible application mutation: `E1 + E2 + E3`; add `E4` for critical
  ownership, entitlement, ordering, audit, payment, or certification state.
- Browser-local history/player state: `E1` plus a sanitized storage assertion.
- File/CSV import/export/download: `E1 + E2 + E5`; add `E3/E4` for persisted
  imports or immutable snapshots.
- Toss test billing and callback: `E1 + E2 + E3 + E4 + E6`.
- Ambiguous mutation result: capture the failed response boundary and the
  reconciliation read; never infer failure from a missing UI success message.

## 4. Global Guard and Redirect Scenarios

| ID            | Target                                                                                            | Fixtures                                 | Expected result                                                                                                                                             | Packs       | Evidence   | WI      |
| ------------- | ------------------------------------------------------------------------------------------------- | ---------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------- | ----------- | ---------- | ------- |
| `G-PUBLIC`    | All nine public routes and two error routes                                                       | `G`, USER variants, `A`                  | Direct URLs render without authentication loops; role-aware controls may differ but public content remains reachable.                                       | `L,G,R,K,V` | `E1,E2`    | 021     |
| `G-AUTH`      | `/complete-profile`, Profile, Likes, History, Licenses, Questions, Subscription Manage, Whitelist | `G` then USER/A                          | `G` is sent to Login with a safe internal return; USER/A access follows the route-specific contract.                                                        | `G,R`       | `E1,E2`    | 022/024 |
| `G-SUB`       | Playlist list/detail/edit and Downloads                                                           | `G`, `U-I0`, `U-IA`, `U-IC`, `U-IP`, `A` | Anonymous goes to Login; no-service USER goes to Plans; service-enabled paid/grace state follows documented entitlement; ADMIN denial/target is consistent. | `G,R`       | `E1,E2,E3` | 024     |
| `G-PAY`       | Three Subscription Payment paths                                                                  | `G`, USER variants, `A`                  | Only exact USER role enters; anonymous goes to Login; ADMIN goes to `/admin/payments`; malformed plan/cycle/callback parameters fail safely.                | `G,F,R`     | `E1,E2`    | 027     |
| `G-BUS`       | Company Certification apply/status                                                                | `G`, individual USER, BUSINESS USER, `A` | Only exact BUSINESS USER enters; all other fixtures use the documented safe redirect without content flash.                                                 | `G,R`       | `E1,E2`    | 026     |
| `G-ADMIN`     | `/admin/*`                                                                                        | `G`, USER variants, `A`                  | Only ADMIN renders AdminLayout; all others use the documented safe route and cannot invoke admin APIs from visible controls.                                | `G,R`       | `E1,E2`    | 028     |
| `G-ADM-INDEX` | `/admin`                                                                                          | `A`                                      | Redirects once to `/admin/dashboard` with replace semantics and no blank intermediate screen.                                                               | `G,R,V`     | `E1`       | 028     |
| `G-QUESTION`  | `/questions`                                                                                      | USER variants, `A`                       | USER sees own list; ADMIN loader redirects to `/admin/questions`; corrupt local user storage cannot create an external redirect or crash.                   | `G,R`       | `E1,E2`    | 024/028 |

## 5. Public Discovery and Notice Pages: 9 UIs

| ID       | Route / UI                         | Role and data coverage                                                 | Primary actions and contract                                                                                                                                                                                                     | Packs / viewports          | Evidence / effect   | WI      |
| -------- | ---------------------------------- | ---------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | -------------------------- | ------------------- | ------- |
| `PUB-01` | `/` Home                           | All roles; Tags/Tracks/Albums `D0-D3`                                  | `fetchTags`, `fetchAvailableTags`, `fetchTracks`, `fetchAlbums`; choose Usage/Genre/Mood/Instrument; open Track/Album; start playback. Usage remains visible and first result-bearing taxonomy becomes active.                   | `L,C,P,K,V`; `VD,VN,VM,VS` | `E1,E2`; `S0/S1`    | 023     |
| `PUB-02` | `/tracks` Track list               | All roles; `D0-D5`; repeated query values and invalid page/query       | Header keyword plus title/Usage search, four Tag taxonomies with AND semantics, removable fallback chips, pagination, playback, like/add/download entitlement paths. `fetchTracks`, Tags, download count/download.               | `L,C,F,P,R,K,V`; all       | `E1,E2,E3`; `S0-S2` | 023     |
| `PUB-03` | `/tracks/:trackId` Track detail    | All roles; `D1,D3,D4`; subscribed and unsubscribed actions             | `fetchTrackDetail`; play, like, add to Playlist, download, license text, breadcrumb/back. Missing/inactive ID has deliberate not-found/error state; duration/waveform and PlayerBar agree.                                       | `L,G,P,B,R,K,V`; all       | `E1-E3`; `S0-S2`    | 023     |
| `PUB-04` | `/albums` image Album list         | All roles; `D0-D3`; pagination                                         | `fetchAlbums`; card image fallback, like, open detail, switch to list view.                                                                                                                                                      | `L,C,R,K,V`; all           | `E1,E2`; `S0-S2`    | 023     |
| `PUB-05` | `/albums/list` tabular Album list  | All roles; `D0-D3`; pagination                                         | Same Album source as `PUB-04`; rows, thumbnail fallback, like/detail navigation, view switch produce the same canonical Album identity/counts.                                                                                   | `L,C,R,K,V`; all           | `E1,E2`; `S0-S2`    | 023     |
| `PUB-06` | `/albums/:albumId` Album detail    | All roles; `D0-D4`; Track nullable media                               | `fetchAlbumDetail`; Album like, ordered Track playback, per-Track like/add/download paths. Track projection obeys shared PlayableTrack fields.                                                                                   | `L,C,P,R,K,V`; all         | `E1-E3`; `S0-S2`    | 023     |
| `PUB-07` | `/subscriptions` Plans             | All roles; individual/business plans `D0-D2`; USER subscription states | `fetchSubscriptionPlans`, optional `fetchMySubscription`; switch audience/cycle, FAQ, choose plan. Guest goes to Login, audience mismatch stays put with clear warning, existing paid access goes to Manage, otherwise Checkout. | `L,G,C,F,R,K,V`; all       | `E1,E2`; `S0`       | 027     |
| `PUB-08` | `/notices` Notice list             | All roles; `D0-D2`; search/filter/page if present                      | `fetchNotices`; latest-request-wins list, stable empty/error/retry, open detail.                                                                                                                                                 | `L,C,R,K,V`; all           | `E1,E2`; `S0`       | 021     |
| `PUB-09` | `/notices/:noticeId` Notice detail | All roles; `D1,D3,D4`; attachment absent/present                       | `fetchNotice`, `downloadNoticeAttachment`; body/metadata, safe attachment name/download, missing ID, back navigation.                                                                                                            | `L,B,R,K,V`; all           | `E1,E2,E5`; `S0/S3` | 021/029 |

## 6. Authentication and Account Entry Pages: 6 UIs

| ID        | Route / UI                                    | Role and data coverage                                                    | Primary actions and contract                                                                                                                                                                      | Packs / viewports    | Evidence / effect | WI  |
| --------- | --------------------------------------------- | ------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | -------------------- | ----------------- | --- |
| `AUTH-01` | `/login` Login                                | `G`, already authenticated USER/A; valid/invalid/locked/unverified inputs | Public capabilities, `login`, `fetchMe`; password/social entry, safe return target, inline error, pending fence, authenticated redirect, no token/secret in UI/log.                               | `L,G,F,M,R,K,V`; all | `E1-E3`; `S2`     | 022 |
| `AUTH-02` | `/signup` Signup                              | `G`; individual/business; duplicate and `D5` inputs                       | `checkEmailAvailability`, nickname/phone checks, `register`; validation and availability race handling, consent, optional channel fields if exposed, one account/email attempt.                   | `L,F,M,R,K,V`; all   | `E1-E4`; `S2/S3`  | 022 |
| `AUTH-03` | `/email-verify` Email callback                | Missing, invalid, expired, reused, valid token; refresh/back              | `verifyEmail`; no-token guidance, invalid/expired distinction where server supports it, idempotent completion, Login target uses current safe origin. Token never appears in visible diagnostics. | `L,F,M,R,K,V`; all   | `E1-E4`; `S2/S3`  | 022 |
| `AUTH-04` | `/password-reset` Request/reset               | Missing/invalid/expired/valid token; duplicate email; `D5` password       | `requestPasswordReset`, `resetPassword`; request response avoids account enumeration, new password validation, one submit, used-token behavior, safe Login return.                                | `L,F,M,R,K,V`; all   | `E1-E4`; `S2/S3`  | 022 |
| `AUTH-05` | `/social-login/:provider` Callback            | enabled/disabled/unknown provider; missing/error/success parameters       | `socialLogin`, `fetchMe`; explicit progress/error, safe redirect, new-profile routing, no auth material leakage, refresh/back is safe.                                                            | `L,G,F,R,K,V`; all   | `E1-E3,E6`; `S3`  | 022 |
| `AUTH-06` | `/complete-profile` Social profile completion | `G`, incomplete USER, complete USER, `A`; duplicate/`D5` fields           | nickname/phone availability, `PUT /users/me/complete-profile`, `fetchMe`; guard, validation, pending fence, canonical session refresh, safe next route.                                           | `L,G,F,M,R,K,V`; all | `E1-E4`; `S2`     | 022 |

## 7. Member and Subscriber Pages: 17 UIs / 19 Routes

| ID        | Route / UI                                         | Role and data coverage                                                        | Primary actions and contract                                                                                                                                                                                                                    | Packs / viewports        | Evidence / effect       | WI      |
| --------- | -------------------------------------------------- | ----------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------ | ----------------------- | ------- |
| `MEM-01`  | `/playlists` Playlist list                         | `U-IA/U-IC/U-IP`; denied fixtures; `D0-D2`; at/beyond plan limit              | fetch/create/delete Playlist plus subscription limit; create modal/card, open, delete confirmation. Reopen refreshes limits and clears stale errors.                                                                                            | `L,G,C,F,M,X,R,K,V`; all | `E1-E4`; `S2`           | 024     |
| `MEM-02`  | `/playlists/:playlistId` Playlist detail           | Owner/non-owner/missing; `D0-D3`; subscription states                         | fetch detail, ordered playback, remove Track, add/download actions; ownership and service guard; removal updates Player projection and reload order.                                                                                            | `L,G,C,M,X,P,R,K,V`; all | `E1-E4`; `S1/S2`        | 024     |
| `MEM-03`  | `/playlists/:playlistId/edit` Playlist edit        | Owner/non-owner/missing; `D0-D2`; boundary title/order                        | fetch/update/delete Playlist; remove/reorder Tracks; optimistic reorder must reconcile or roll back; two owned dialogs reset on close.                                                                                                          | `L,G,C,F,M,X,R,K,V`; all | `E1-E4`; `S2`           | 024     |
| `MEM-04`  | `/profile` Profile                                 | USER variants and `A`; account/subscription `D0-D1`; `D5` fields              | tabs/shortcuts, `fetchMe`, availability checks, profile update, password update, subscription summary. Values survive rejection; session reflects canonical update.                                                                             | `L,G,F,M,R,K,V`; all     | `E1-E4`; `S2`           | 022     |
| `MEM-05`  | `/likes` Likes                                     | USER/A; Track and Album `D0-D3`                                               | fetch/remove Track and Album likes; tabs, playback, download, detail links. Removal affects all like entry points after reload.                                                                                                                 | `L,G,C,M,P,B,R,K,V`; all | `E1-E3`; `S1/S2`        | 024     |
| `MEM-06`  | `/play-history` local History page                 | USER/A; storage `D0-D4`, duplicates, over 100 IDs                             | hydrate IDs through public batch, omit stale/inactive Tracks, play/delete/clear if exposed. An older response cannot overwrite newer storage.                                                                                                   | `L,G,C,P,R,K,V`; all     | `E1` plus storage; `S1` | 024     |
| `MEM-07`  | `/licenses` License list                           | USER/A; `D0-D2`; modal/attachment states                                      | `fetchMyLicenses`; open detail/modal if present, download licensed Track, pagination/empty/error.                                                                                                                                               | `L,G,C,B,R,K,V`; all     | `E1-E3,E5`; `S0/S3`     | 024/029 |
| `MEM-08`  | `/licenses/:licenseId` License detail              | Owner/non-owner/missing; `D1,D4`                                              | `fetchLicenseDetail`; identity, terms, linked Track/Subscription facts, safe missing/forbidden handling.                                                                                                                                        | `L,G,R,K,V`; all         | `E1-E3`; `S0`           | 024     |
| `MEM-09`  | `/downloads` Download history                      | service-enabled/denied fixtures; `D0-D3`; daily limit boundary                | fetch count/history/Track IDs, replay and redownload; Blob result and count/history update agree; nullable media renders.                                                                                                                       | `L,G,C,P,B,R,K,V`; all   | `E1-E5`; `S2/S3`        | 024/029 |
| `MEM-10`  | `/subscriptions/checkout` Subscription Payment     | USER only; valid/invalid/missing plan/cycle; no/old billing method            | fetch plans, `prepareBillingAgreement`; show exact first-period or zero re-registration amount/purpose. Start Toss test billing auth once; leaving and returning remains recoverable.                                                           | `L,G,F,M,R,K,V`; all     | `E1-E4,E6`; `S3`        | 027     |
| `MEM-10S` | `/subscriptions/checkout/success` callback variant | Prepared, already-confirmed, missing, mismatched, expired callback parameters | `confirmBillingAgreement`; verify callback contract server-side, idempotent confirmation, clear success/failure, canonical Subscription/Billing Agreement reload.                                                                               | `L,G,F,M,R,K,V`; all     | `E1-E4,E6`; `S3`        | 027     |
| `MEM-10F` | `/subscriptions/checkout/fail` callback variant    | Provider cancel/error/missing parameters; refresh/back                        | No false activation; human-readable failure/cancel state; retry returns to a valid plan/method registration path without stale prepared order.                                                                                                  | `L,G,F,R,K,V`; all       | `E1-E4,E6`; `S3`        | 027     |
| `MEM-11`  | `/subscriptions/manage` Subscription Manage        | `U-I0/U-IA/U-IC/U-IP`, business variants; invalid query                       | fetch Subscription, Plans, Billing Agreement, change preview; upgrade, downgrade/cycle scheduling/replacement/reversal, cancel/reactivate, payment-method re-registration. UI, charge timing, pending state, entitlement and reload must agree. | `L,G,F,M,X,R,K,V`; all   | `E1-E4,E6`; `S2/S3`     | 027     |
| `MEM-12`  | `/whitelist-channels` Whitelist                    | USER variants; subscription/cert states; `D0-D2`; plan and 100-profile limits | list/register/update/delete/set primary/request registration; draft vs registration-relevant limit, status transitions, immutable submitted values, deletion rules, stale review state.                                                         | `L,G,C,F,M,X,R,K,V`; all | `E1-E4`; `S2`           | 026     |
| `MEM-13`  | `/company-certification/apply` Apply               | `U-B0`; existing pending/approved/rejected; file/type/size/content boundaries | fetch current, guide Setting, apply; PDF/JPG/JPEG/PNG validation, image canonicalization contract, one submission, private metadata, status-aware redirect.                                                                                     | `L,G,F,M,B,R,K,V`; all   | `E1-E4,E5`; `S2/S3`     | 026/029 |
| `MEM-14`  | `/company-certification/status` Status/resubmit    | BUSINESS pending/approved/rejected/revision; missing record                   | fetch current, resubmit replacement; status/reason/timestamps, allowed replacement only, values survive rejection, reload canonical state.                                                                                                      | `L,G,F,M,B,R,K,V`; all   | `E1-E4,E5`; `S2/S3`     | 026/029 |
| `MEM-15`  | `/questions` Question list                         | USER variants; ADMIN redirect; `D0-D2`                                        | `fetchQuestions`; own list, status/filter/page, create/detail navigation, latest-request-wins.                                                                                                                                                  | `L,G,C,R,K,V`; all       | `E1-E3`; `S0`           | 024     |
| `MEM-16`  | `/questions/new` Question create                   | USER variants; `D5`; attachments absent/present                               | `createQuestion`; required/length/file checks, one submit, success target, rejection preserves input.                                                                                                                                           | `L,G,F,M,B,R,K,V`; all   | `E1-E4,E5`; `S2/S3`     | 024/029 |
| `MEM-17`  | `/questions/:questionId` Question detail           | owner/non-owner/ADMIN/missing; answered/unanswered; attachments               | fetch/delete Question, download attachment, ADMIN answer when route contract allows; ownership, status, confirmation, reload, private file boundary.                                                                                            | `L,G,F,M,X,B,R,K,V`; all | `E1-E4,E5`; `S2/S3`     | 024/029 |

## 8. Error Pages: 2 UIs

| ID       | Route / UI            | Coverage                                           | Expected result                                                                                                  | Packs / viewports | Evidence / effect | WI  |
| -------- | --------------------- | -------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------- | ----------------- | ----------------- | --- |
| `ERR-01` | `/error` Server Error | All roles; direct entry and routed failure context | Clear non-technical message, safe Home/retry path, no stale business success, no stack/secret leakage.           | `G,R,K,V`; all    | `E1`; `S0`        | 021 |
| `ERR-02` | `*` Not Found         | All roles; unknown path, encoded path, refresh     | 404-specific message and safe navigation; Header/Player shell remains coherent where intended; no redirect loop. | `G,R,K,V`; all    | `E1`; `S0`        | 021 |

## 9. Creator/Admin Content Pages: 5 UIs

| ID       | Route / UI                               | Role and data coverage                                                  | Primary actions and contract                                                                                                                                       | Packs / viewports          | Evidence / effect   | WI      |
| -------- | ---------------------------------------- | ----------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------ | -------------------------- | ------------------- | ------- |
| `CRT-01` | `/admin/tracks/upload` Track upload      | `A`; denied roles; `D5`; media/tag/thumbnail variants                   | fetch Tags, `createTrack`; audio and square JPEG/PNG validation, Usage/search index, analysis metadata, one multipart submit, rendered detail/playback after save. | `L,G,F,M,B,R,K,V`; all     | `E1-E5`; `S2/S3`    | 025/029 |
| `CRT-02` | `/admin/tracks/:trackId/edit` Track edit | `A`; valid/missing Track; existing non-square and replacement thumbnail | fetch admin detail/Tags, `updateTrack`; preserve existing media unless replaced, validate new files, update metadata/Tags, reload all playback entry projections.  | `L,G,F,M,B,R,K,V`; all     | `E1-E5`; `S2/S3`    | 025/029 |
| `CRT-03` | `/admin/albums` Album manage             | `A`; `D0-D2`; two dialogs                                               | fetch/create/update/delete/detail Album; search/page if present, square image behavior, modal reset, confirmation, no stale list after mutation.                   | `L,G,C,F,M,X,R,K,V`; all   | `E1-E4`; `S2/S3`    | 025     |
| `CRT-04` | `/admin/albums/new` Album create         | `A`; `D5`; image absent/present                                         | `createAlbum`; validation, one submit, canonical detail/manage result, image preview and error.                                                                    | `L,G,F,M,B,R,K,V`; all     | `E1-E4,E5`; `S2/S3` | 025/029 |
| `CRT-05` | `/admin/albums/:albumId/edit` Album edit | `A`; missing/valid Album; `D0-D2` Tracks/order                          | fetch/update Album; add/remove/reorder Tracks; optimistic order reconciliation; image preserve/replace; public detail mirrors persisted order.                     | `L,G,C,F,M,X,B,R,K,V`; all | `E1-E5`; `S2/S3`    | 025/029 |

## 10. Admin Operation Pages: 14 UIs

| ID       | Route / UI                      | Role and data coverage                                           | Primary actions and contract                                                                                                                                                                                                                                 | Packs / viewports          | Evidence / effect | WI          |
| -------- | ------------------------------- | ---------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ | -------------------------- | ----------------- | ----------- |
| `ADM-01` | `/admin/dashboard`              | `A`; totals/recent users `D0-D2`                                 | `fetchDashboardStats`; four totals and five recent users agree with API; loading/error/empty; dense cards do not overflow.                                                                                                                                   | `L,G,V,K`; `VD,VN,VM`      | `E1-E3`; `S0`     | 028         |
| `ADM-02` | `/admin/users`                  | `A`; `D1-D2`; self/last-admin/stale authority                    | fetch/update User role; search/page, detail modal, reason, self-demotion and last-admin rejection, session role refresh after mutation/403, stable row/error.                                                                                                | `L,G,C,F,M,X,R,K,V`; all   | `E1-E4`; `S2`     | 028         |
| `ADM-03` | `/admin/subscriptions`          | `A`; plan `D0-D2`                                                | read-only `fetchAdminSubscriptionPlans`; prices, cycles, limits and active badges; no misleading edit affordance.                                                                                                                                            | `L,G,C,R,K,V`; all         | `E1-E3`; `S0`     | 028         |
| `ADM-04` | `/admin/licenses`               | `A`; users/licenses `D0-D2`                                      | fetch Users then selected User licenses; search/Enter/page, selection replacement, stale response fence, empty/error.                                                                                                                                        | `L,G,C,R,K,V`; all         | `E1-E3`; `S0`     | 028         |
| `ADM-05` | `/admin/questions`              | `A`; `D0-D2`; status transitions                                 | fetch Questions, update status; filter/page/detail/answer ownership, one mutation, list/detail agreement.                                                                                                                                                    | `L,G,C,F,M,R,K,V`; all     | `E1-E4`; `S2`     | 028         |
| `ADM-06` | `/admin/company-certifications` | `A`; pending/approved/rejected/revision; two dialogs             | fetch list/detail, private document download, process review; bounded reason, stale response fence, status transition/audit, no storage-path leak.                                                                                                           | `L,G,C,F,M,X,B,R,K,V`; all | `E1-E5`; `S2/S3`  | 026/028/029 |
| `ADM-07` | `/admin/tags`                   | `A`; all taxonomies `D0-D2`; duplicate/used Tag; two dialogs     | fetch/create/update/delete Tag; client-decidable duplicate stays in modal, server conflict is readable, filters/input survive error, destructive dependency handling.                                                                                        | `L,G,C,F,M,X,R,K,V`; all   | `E1-E4`; `S2`     | 025/028     |
| `ADM-08` | `/admin/track-manage`           | `A`; `D0-D3`; missing/used Track                                 | fetch/delete Track; search/filter/page, image fallback, edit navigation, delete confirmation/dependency error/list refresh.                                                                                                                                  | `L,G,C,M,X,R,K,V`; all     | `E1-E4`; `S2`     | 025/028     |
| `ADM-09` | `/admin/user-subscriptions`     | `A`; none/active/cancelled/expired/pending; stale correction     | fetch User Subscriptions/Plans; open correction modal; preview/request/approve/execute/recover unknown outcome; audit and entitlement agreement. This is local correction, not Toss charge/refund.                                                           | `L,G,C,F,M,X,R,K,V`; all   | `E1-E4`; `S2`     | 028         |
| `ADM-10` | `/admin/payments`               | `A`; all nine tabs; `D0-D2`; lifecycle states                    | orders, billing agreements, payments, incidents, receipts, audits, settlements, refunds, corrections. Reads, previews, requests, approvals, executes, import/reconcile/ignore and status changes must use exact business copy and persistent audit evidence. | `L,G,C,F,M,X,B,R,K,V`; all | `E1-E5`; `S0-S3`  | 028/029     |
| `ADM-11` | `/admin/whitelist-channels`     | `A`; statuses and batches `D0-D2`                                | fetch/filter channels, status transition, immutable CSV export batch and download; request row and exported snapshot agree after later user edits/deletes.                                                                                                   | `L,G,C,F,M,X,B,R,K,V`; all | `E1-E5`; `S2/S3`  | 026/028/029 |
| `ADM-12` | `/admin/notices/new`            | `A`; `D5`; attachments                                           | `createNotice`; validation, file handling, one submit, list/detail visibility after save.                                                                                                                                                                    | `L,G,F,M,B,R,K,V`; all     | `E1-E5`; `S2/S3`  | 025/029     |
| `ADM-13` | `/admin/notices/:noticeId/edit` | `A`; valid/missing Notice; attachment replace/remove; one dialog | fetch/update/delete Notice; preserve existing attachment unless explicit change, confirmation, public detail agreement.                                                                                                                                      | `L,G,F,M,X,B,R,K,V`; all   | `E1-E5`; `S2/S3`  | 025/029     |
| `ADM-14` | `/admin/settings`               | `A`; Setting absent/present; `D5`                                | read public `COMPANY_CERT_GUIDE`, admin upsert; missing key shows editable empty value, one save, public Company Certification guide reloads canonical text.                                                                                                 | `L,G,F,M,R,K,V`; all       | `E1-E4`; `S2`     | 028         |

## 11. Shared Shell, Player, and Dialog Surfaces

| ID      | Surface / owners                      | Required variants and invariant                                                                                                                                                                                                                                       | Packs / viewports          | Evidence / effect       | WI          |
| ------- | ------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | -------------------------- | ----------------------- | ----------- |
| `SH-01` | Header in MainLayout                  | Public/USER/ADMIN nav sets; active state; keyword submission preserves title/Usage search contract; theme; login/logout; mobile menu/overlay; safe internal navigation and fixed-height layout.                                                                       | `G,F,R,K,V`; all           | `E1,E2`; `S0-S2`        | 021/022/030 |
| `SH-02` | PlayerBar in MainLayout               | No Track/current Track; every playback entry; full duration and waveform progress; seek pointer/keyboard; queue/previous/next/shuffle/repeat/volume/mute; buffering/error/retry; like/add/download entitlement; desktop/mobile expanded states; no content occlusion. | `L,G,P,B,R,K,V`; all       | `E1-E3`; `S1-S3`        | 023/030     |
| `SH-03` | HistoryModal                          | `D0-D4`, up to 100 hydrated IDs; play/delete/clear; close/reopen; focus restoration; same Track projection as History page.                                                                                                                                           | `L,C,P,X,R,K,V`; all       | `E1` plus storage; `S1` | 024/030     |
| `SH-04` | PlaylistDrawer                        | guest/no subscription/subscriber; Playlists/Likes tabs; create/open/delete/remove/reorder/play; plan limit; drag, touch, and keyboard alternatives; close/reopen and stale-request fencing.                                                                           | `L,G,C,F,M,X,P,R,K,V`; all | `E1-E4`; `S1/S2`        | 024/030     |
| `SH-05` | AddToPlaylistModal                    | denied/no Playlist/at limit/normal; latest Playlist list, duplicate Track, one add, close timer cleanup, reopen reset, focus restoration.                                                                                                                             | `L,G,C,F,M,R,K,V`; all     | `E1-E4`; `S2`           | 024/030     |
| `SH-06` | TagFilterModal                        | each taxonomy `D0-D2`; search, multi-select, apply/clear/remove; URL raw values and visible Usage `#`; close without apply; reopen current selection; mobile overflow/focus.                                                                                          | `L,C,F,R,K,V`; all         | `E1,E2`; `S0/S1`        | 023/030     |
| `SH-07` | ConfirmDialog and typed confirmations | cancel/confirm, pending, error, destructive target copy, typed phrase validation, focus trap/return, Escape policy, no duplicate request.                                                                                                                             | `F,M,X,R,K,V`; all         | `E1-E3`; `S2-S4`        | 028/030     |

## 12. Modal Occurrence Reconciliation

Each of the 22 current `<Modal>` occurrences must be exercised through its
owning workflow. A shared wrapper occurrence is not evidence for page-owned
dialogs.

| Owner file                                        | Occurrences | Matrix owner   | Required special check                                   |
| ------------------------------------------------- | ----------: | -------------- | -------------------------------------------------------- |
| `components/playlist/AddToPlaylistModal.tsx`      |           1 | `SH-05`        | Subscription/limit/duplicate add and reopen reset        |
| `components/filter/TagFilterModal.tsx`            |           1 | `SH-06`        | Apply versus close-without-apply and focus               |
| `components/ui/ConfirmDialog.tsx`                 |           1 | `SH-07`        | Shared confirmation semantics                            |
| `components/player/HistoryModal.tsx`              |           1 | `SH-03`        | Browser-local deletion/clear and hydration               |
| `pages/admin/CompanyCertManagePage.tsx`           |           2 | `ADM-06`       | Detail/document and review-state separation              |
| `pages/creator/AlbumManagePage.tsx`               |           2 | `CRT-03`       | Create/edit state and delete confirmation reset          |
| `pages/admin/NoticeEditPage.tsx`                  |           1 | `ADM-13`       | Delete confirmation and attachment preservation          |
| `pages/subscriber/LicenseListPage.tsx`            |           1 | `MEM-07`       | Selected record and close/reopen state                   |
| `pages/subscriber/PlaylistDetailPage.tsx`         |           1 | `MEM-02`       | Remove confirmation and current Playlist identity        |
| `pages/subscriber/PlaylistEditPage.tsx`           |           2 | `MEM-03`       | Track removal and Playlist deletion targets              |
| `pages/subscriber/PlaylistListPage.tsx`           |           2 | `MEM-01`       | Create and delete; plan limit refresh                    |
| `pages/admin/TagManagePage.tsx`                   |           2 | `ADM-07`       | Edit/create and delete conflict errors remain local      |
| `pages/admin/TrackManagePage.tsx`                 |           1 | `ADM-08`       | Delete dependency error preserves filters/page           |
| `pages/admin/UserManagePage.tsx`                  |           1 | `ADM-02`       | Role reason, self/last-admin, stale authority            |
| `pages/subscriber/QuestionDetailPage.tsx`         |           1 | `MEM-17`       | Delete target/ownership and attachment context           |
| `pages/admin/UserSubscriptionCorrectionModal.tsx` |           1 | `ADM-09`       | Persisted resumable state plus nested typed confirmation |
| `pages/subscriber/SubscriptionManagePage.tsx`     |           1 | `MEM-11`       | Cancel/reactivate/change confirmation state              |
| **Total**                                         |      **22** | 17 owner files | Must reconcile to `docs/ui/modal-list.md`                |

## 13. Same Behavior, Different Entry-Point Invariants

| ID             | Variants that must each run                                                                                                             | Shared invariant and adjacent regression                                                                                                                                                                                            |
| -------------- | --------------------------------------------------------------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `INV-PLAY`     | Track list, Track detail, Album detail, Likes, Playlist detail, Download history, Player queue/drawer, History page/modal               | Same Track ID selects the same title, duration, nullable thumbnail/waveform, stream source, and full-length playback. Selection atomically resets current time and waveform; source changes cannot retain stale progress or errors. |
| `INV-TRACK`    | Public list/detail, Album Track, Playlist Track, Like item, Download item, browser-local history hydration                              | `PlayableTrack` normalization yields explicit `null` for omitted optional media and never exposes storage keys. A shared DTO/store change receives all-entry regression.                                                            |
| `INV-SEARCH`   | Header search, Track-list keyword, visible Tag chips, Tag modal, ADMIN Tag mutation                                                     | Keyword searches title and Usage only; repeated taxonomy values preserve AND semantics; Usage adds `#` only to display; URLs/API use raw values; deleted/renamed Tags do not leave an unusable hidden filter.                       |
| `INV-IMAGE`    | Home, Track list/detail, Album image/list/detail, Likes, Playlists, PlayerBar, admin upload/edit/manage                                 | One canonical URL/fallback behavior; square new Track thumbnail validation; existing non-square thumbnail remains unchanged until replacement; missing images cannot shift controls.                                                |
| `INV-SUB`      | Plans, three Checkout paths, Manage, SubscriberRoute, Playlist/Download actions, Profile summary, ADMIN user-subscription/payment views | UI copy, Subscription status, Billing Agreement, payment/order state, entitlement and next billing date remain mutually consistent after first purchase, upgrade, scheduled change, cancel, reactivate, failure, and reload.        |
| `INV-QUESTION` | USER list/create/detail and ADMIN list/status/answer                                                                                    | One Question identity/status/attachment set; ownership and ADMIN capabilities differ deliberately; mutation is visible in both sides after reload.                                                                                  |
| `INV-WHITE`    | USER draft/edit/delete/primary/request and ADMIN review/export/download                                                                 | Plan limit counts only registration-relevant states; primary channel remains valid; review acts on immutable submitted data; export snapshot remains reproducible after later user mutation.                                        |
| `INV-CERT`     | BUSINESS apply/status/resubmit and ADMIN list/detail/document/review                                                                    | One certification identity and private document boundary; review status/reason and replacement rules agree; approved status gates BUSINESS Subscription behavior without exposing storage paths.                                    |

## 14. High-Risk State-Machine Scenarios

### 14.1 Subscription and Billing

| Scenario                       | Preconditions                                                    | Action and expected transition                                                                                                                                                                                | Required proof                                                                            | Effect |
| ------------------------------ | ---------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------- | ------ |
| First recurring purchase       | `U-I0` or eligible `U-B0/U-BA`; Toss test mode; valid plan/cycle | Prepare billing auth -> test card registration -> callback confirmation -> charge first period -> ACTIVE Subscription and active Billing Agreement. No activation before server confirmation.                 | `E1-E4,E6`, reload Plans/Manage/Profile/admin ledgers                                     | `S3`   |
| Payment-method re-registration | Paid Subscription with absent/removed Billing Agreement          | `purpose=BILLING_AGREEMENT`, `amount=0`; register method without charge, plan, period, pending-change, or entitlement mutation.                                                                               | `E1-E4,E6`                                                                                | `S3`   |
| Upgrade                        | Active lower plan and valid Billing Agreement                    | Preview prorated remaining-period difference -> confirm -> immediate test charge -> immediate higher plan; existing next billing date retained and chosen future cycle applied according to current contract. | UI copy, request, provider test result, order/payment/agreement/subscription rows, reload | `S3`   |
| Downgrade/cycle-only           | Active plan                                                      | Preview shows no immediate charge -> create/replace pending target -> current entitlement retained until successful renewal -> scheduled target becomes canonical only at boundary.                           | `E1-E4`; do not wait for real scheduler without controlled clock/fixture                  | `S2`   |
| Cancel/reactivate              | Active or paid cancelled-grace Subscription                      | Cancel stops next renewal but preserves paid access to `expiresAt`; reactivate valid grace state; route guards and download/Playlist entitlement agree.                                                       | `E1-E4`; reload all `INV-SUB` surfaces                                                    | `S2`   |
| Provider/local failure         | Prepared or charging test order                                  | Definite provider rejection stays failed; provider success with local uncertainty is not blindly retried and is surfaced through reconciliation/audit evidence.                                               | `E2-E4,E6`; no real money                                                                 | `S3`   |

### 14.2 ADMIN Payment Operations

| Scenario                | State sequence                                                           | Required assertions                                                                                                                                                                               | Effect  |
| ----------------------- | ------------------------------------------------------------------------ | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------- |
| Refund                  | preview -> request -> approve -> execute                                 | Each stage has explicit amount/payment target and operator note; duplicate stage is blocked; provider-test/local result and audit agree; failure such as missing payment is not shown as success. | `S2/S3` |
| Entitlement correction  | preview -> request -> approve -> execute                                 | Copy states that this is local entitlement correction, not Toss refund/charge; revalidation catches stale Subscription; audit and final entitlement agree.                                        | `S2`    |
| Reconciliation incident | compare -> Incident open -> status update                                | Provider/local difference is durable, list filters/reload preserve it, status update is audited, no automated money mutation follows.                                                             | `S0/S2` |
| Settlement import       | CSV select -> validate/preview -> import -> reconcile -> optional ignore | Duplicate file/row is idempotent, malformed/formula content is rejected or neutralized, fees/net totals reconcile to orders/refunds, ignored row requires reason and audit.                       | `S2/S3` |
| Receipt/audit reads     | Ledger tabs and cross-links                                              | Receipt URL/reference is sanitized; raw provider/card/secret data is absent; operation audit identifies actor/action/target/result without excessive PII.                                         | `S0`    |

### 14.3 ADMIN Local Subscription Correction

1. Preview validates User, target plan/status/dates, reason, and Seoul business
   dates without mutating state.
2. Request persists a resumable operation and normalized reason.
3. Approval revalidates actor/target and requires explicit note.
4. Execution uses typed confirmation, revalidates stale state, writes audit and
   canonical entitlement/billing-agreement state, and does not call Toss.
5. HTTP 4xx is definite rejection. Network/no-response/HTTP 5xx triggers one
   bounded detail/open-state read. `204` or failed reconciliation remains
   unknown, preserves the draft, blocks duplicate mutation, and exposes only a
   status retry until state becomes known.

Required proof: `E1-E4`; effect `S2`; owners `ADM-09` and `SH-07`.

### 14.4 Whitelist

1. Draft creation/update/delete and primary selection operate below the
   100-profile safety cap.
2. Registration request enforces current Subscription plan limit against
   registration-relevant states, not all drafts.
3. ADMIN transitions through current statuses, including revision, without
   silently accepting stale user edits.
4. User deletion or edit after submission cannot rewrite an already created
   export batch.
5. Export CSV includes the current required User email and channel fields,
   neutralizes spreadsheet formulas, excludes secret/internal persistence data,
   and parses back to the immutable batch rows.

Required proof: `E1-E5`; effects `S2/S3`; owners `MEM-12`, `ADM-11`.

### 14.5 Company Certification

1. BUSINESS USER submits an allowed private document and sees pending status.
2. Replacement is available only in the current allowed state and preserves a
   readable reason/draft when rejected.
3. ADMIN downloads the private document through authorization, then approves,
   rejects, or requests revision with bounded reason.
4. USER status and BUSINESS Subscription eligibility reflect the same durable
   review state after reload.
5. Metadata, UI, logs, API, and downloaded filename never expose the physical
   storage path. Unsupported/corrupt files do not create a false application.

Required proof: `E1-E5`; effects `S2/S3`; owners `MEM-13/14`, `ADM-06`.

### 14.6 Upload and Media Analysis

Track create/edit must prove local field/file checks, multipart request shape,
server audio/image analysis, durable Track/Tag relations, and playback from all
`INV-PLAY` entry points. The audio is full-length product content; no preview
truncation may be introduced. A new/replacement Track thumbnail must be square
JPEG/PNG, while an existing non-square thumbnail remains unchanged without an
explicit replacement. Failed upload/analysis cannot leave a false-success UI or
unreferenced durable metadata; recovery behavior must follow current storage
mutation contracts.

Required proof: `E1-E5`; effect `S2/S3`; owners `CRT-01/02` and `INV-PLAY`.

### 14.7 Ordered Membership

Album and Playlist Track add/remove/reorder scenarios must cover first/last,
duplicate, missing, foreign-owned, and concurrent/stale order. Optimistic UI
must either match the canonical reload order or visibly roll back. Public Album,
Playlist detail, Player queue, previous/next, and persisted Track identity must
all use that order.

Required proof: `E1-E4`; effect `S2`; owners `CRT-05`, `MEM-02/03`, `SH-04`.

### 14.8 CSV and Private Binary Evidence

For every generated/downloaded file, a click is insufficient evidence. Record
status, content type, sanitized filename, byte length, parser result, row count,
headers, representative escaped values, and comparison with the API/DB snapshot.
For imports, also verify invalid file, duplicate import, partial-row failure,
transactional outcome, audit row, and reload. Never open untrusted spreadsheets
with formula execution enabled during automated inspection.

## 15. Cross-Cutting Heuristic Checks

These are audit heuristics unless a current project contract states otherwise.
A heuristic conflict is classified as a policy question, not auto-fixed.

| Area               | Checks                                                                                                                                                                         |
| ------------------ | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| Feedback           | Every action has pending, success, and failure feedback proportional to its impact; no raw backend/provider wording or implementation jargon appears to end users.             |
| Validation         | Locally decidable invalid/duplicate input is stopped before mutation; server remains authoritative; errors are attached to a stable page/modal/field context.                  |
| State preservation | Search/filter/page/form context survives recoverable errors; closing/reopening clears only transient state; refresh shows canonical durable state.                             |
| Concurrency        | Double-click, Enter plus click, rapid filters/pages, route departure, late response, and duplicate callback do not create duplicate or stale outcomes.                         |
| Navigation         | Back/forward/deep link/refresh works; destructive success does not leave a dead detail URL; external redirect injection is rejected.                                           |
| Accessibility      | Semantic headings/landmarks, accessible names, focus visibility/order, dialog focus, status/error announcements, keyboard alternatives, and sufficient contrast.               |
| Responsive layout  | Long Korean text, prices, dates, filenames, tables, cards, buttons, and fixed shell controls fit without overlap or hidden primary actions.                                    |
| Security/privacy   | No secrets, tokens, raw card values, storage paths, excessive PII, stack traces, or provider payloads appear in DOM, URL, console, logs, files, or generic errors.             |
| Consistency        | Same action uses the same term, icon meaning, confirmation severity, money/date/time zone formatting, empty/error pattern, and resulting canonical object across entry points. |

## 16. Browser WI Assignment and Exit Gates

| WI       | Bounded execution scope                                                                       | Exit gate                                                                                          |
| -------- | --------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------- |
| `WI-021` | Public shell, Notices, error routes, guard smoke                                              | All owned rows classified; no uncaught console errors or broken deep links.                        |
| `WI-022` | Login, Signup, email/password/social flows, Profile                                           | Account mutations and external-mail boundaries have evidence and cleanup.                          |
| `WI-023` | Home, Track/Album catalog, search/Tags, playback and shared PlayerBar                         | Every `INV-PLAY/SEARCH/IMAGE/TRACK` variant runs on desktop/mobile with waveform/time evidence.    |
| `WI-024` | Playlists, Likes, History, Licenses, Downloads, Questions                                     | Ownership, entitlement, local storage, ordering, file and modal flows have canonical reload proof. |
| `WI-025` | Track/Album/Tag/Notice content administration                                                 | CRUD/upload/file/reorder paths and public projections agree.                                       |
| `WI-026` | Whitelist and Company Certification USER/ADMIN flows                                          | All statuses, limits, review/revision/deletion and privacy boundaries are classified.              |
| `WI-027` | User Subscription, Toss test billing, plan change/cancel/reactivate                           | UI/request/provider-test/local DB/reload chain is complete; no real money action runs.             |
| `WI-028` | ADMIN Dashboard/User/Plans/License/Question/Subscription/Payment/Settings operations          | Every tab and operation stage has allow/deny, audit, error, and reload proof.                      |
| `WI-029` | CSV/import/export, attachments, private files and binary downloads                            | Every artifact is parsed and compared; no click-only pass.                                         |
| `WI-030` | Cross-entry, interruption, stale response, keyboard, responsive and adjacent regression sweep | Shared invariants and all assigned viewports pass or have explicit findings.                       |

No browser WI may silently skip a row because fixture data is absent. It must
record `BLOCKED`, name the missing fixture, and either create reversible test
data under `S2/S3` or escalate when doing so would cross `S4`.

## 17. Finding Record Schema

Each failed scenario records:

1. Finding ID, matrix row/invariant, severity, role fixture, data state,
   viewport, URL, and exact precondition.
2. Expected result with document/code source and whether it is a product
   contract or generic audit heuristic.
3. Actual visible behavior, console output, sanitized request/response, and
   canonical API/DB state where relevant.
4. Reproduction steps, frequency, interruption timing, and adjacent entry
   points checked.
5. Classification: implementation defect, documentation drift, test/fixture
   defect, environment limitation, or product-policy decision.
6. Proposed bounded fix and regression radius. Product policy, schema/data
   destruction, real external effects, architecture/dependency, feature
   removal, branch/destructive Git, and deployment remain user gates.

## 18. API and Persistence Traceability

### 18.1 Exact Route-Level Frontend Consumers

The map below is mechanically derived from current non-test TSX imports. A
shared Store/component call is named where a page delegates the request.
Import presence identifies a candidate consumer; browser evidence must prove
that the corresponding control and state are actually reachable.

| Matrix row       | Current frontend consumer boundary                                                                                                                                                                                  |
| ---------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `PUB-01`         | `fetchAlbums`, `fetchTracks`, `fetchAvailableTags`, `fetchTags`                                                                                                                                                     |
| `PUB-02`         | `fetchTracks`, `fetchTags`, `fetchAvailableTags`, `fetchDownloadCount`, `downloadTrack`, like/Playlist shared controls                                                                                              |
| `PUB-03`         | `fetchTrackDetail`, `fetchDownloadCount`, `downloadTrack`, like/Playlist shared controls                                                                                                                            |
| `PUB-04`         | `fetchAlbums`, `albumLikeStore`                                                                                                                                                                                     |
| `PUB-05`         | `fetchAlbums`, `albumLikeStore`                                                                                                                                                                                     |
| `PUB-06`         | `fetchAlbumDetail`, shared Track/Album like and Player controls                                                                                                                                                     |
| `PUB-07`         | `fetchSubscriptionPlans`, `fetchMySubscription`                                                                                                                                                                     |
| `PUB-08`         | `fetchNotices`                                                                                                                                                                                                      |
| `PUB-09`         | `fetchNotice`, `downloadNoticeAttachment`                                                                                                                                                                           |
| `AUTH-01`        | `login`, `fetchMe`, public capability state                                                                                                                                                                         |
| `AUTH-02`        | `register`, `checkEmailAvailability`, `checkNicknameAvailability`, `checkPhoneAvailability`                                                                                                                         |
| `AUTH-03`        | `verifyEmail`                                                                                                                                                                                                       |
| `AUTH-04`        | `requestPasswordReset`, `resetPassword`                                                                                                                                                                             |
| `AUTH-05`        | `socialLogin`, `fetchMe`                                                                                                                                                                                            |
| `AUTH-06`        | `checkNicknameAvailability`, `checkPhoneAvailability`, direct `PUT /users/me/complete-profile`, `fetchMe`                                                                                                           |
| `MEM-01`         | `fetchMyPlaylists`, `createPlaylist`, `deletePlaylist`, `fetchMySubscription`                                                                                                                                       |
| `MEM-02`         | `fetchPlaylistDetail`, `removeTrackFromPlaylist`, `downloadTrack`, shared Player/like/Playlist controls                                                                                                             |
| `MEM-03`         | `fetchPlaylistDetail`, `updatePlaylist`, `deletePlaylist`, `removeTrackFromPlaylist`, `reorderTracks`                                                                                                               |
| `MEM-04`         | `fetchMe`, `checkNicknameAvailability`, `checkPhoneAvailability`, `fetchMySubscription`, direct profile/password updates                                                                                            |
| `MEM-05`         | `fetchLikes`, `removeLike`, `fetchAlbumLikes`, `removeAlbumLike`, `downloadTrack`, shared Player controls                                                                                                           |
| `MEM-06`         | local History utilities and `playerStore.fetchPlayableTracks` batch hydration                                                                                                                                       |
| `MEM-07`         | `fetchMyLicenses`, `downloadTrack`                                                                                                                                                                                  |
| `MEM-08`         | `fetchLicenseDetail`                                                                                                                                                                                                |
| `MEM-09`         | `fetchDownloadCount`, `fetchDownloadHistory`, `fetchDownloadHistoryTrackIds`, `downloadTrack`                                                                                                                       |
| `MEM-10/10S/10F` | `fetchSubscriptionPlans`, `prepareBillingAgreement`, `confirmBillingAgreement` selected by route/callback state                                                                                                     |
| `MEM-11`         | `fetchMySubscription`, `cancelMySubscription`, `reactivateMySubscription`, `changeMySubscription`, `fetchSubscriptionChangePreview`, `fetchSubscriptionPlans`, `fetchMyBillingAgreement`                            |
| `MEM-12`         | `fetchWhitelistChannels`, `registerChannel`, `updateChannel`, `deleteChannel`, `setPrimaryWhitelistChannel`, `requestWhitelistRegistration`, `fetchMySubscription`                                                  |
| `MEM-13`         | `fetchMyCompanyCert`, `applyCompanyCert`, `getSetting`                                                                                                                                                              |
| `MEM-14`         | `fetchMyCompanyCert`, `resubmitCompanyCert`                                                                                                                                                                         |
| `MEM-15`         | `fetchQuestions`                                                                                                                                                                                                    |
| `MEM-16`         | `createQuestion`                                                                                                                                                                                                    |
| `MEM-17`         | `fetchQuestionDetail`, `deleteQuestion`, `downloadAttachment`, `createAnswer`                                                                                                                                       |
| `ERR-01/02`      | No business API consumer                                                                                                                                                                                            |
| `CRT-01`         | `createTrack`, `fetchTags`                                                                                                                                                                                          |
| `CRT-02`         | `fetchTrackDetailForAdmin`, `updateTrack`, `fetchTags`                                                                                                                                                              |
| `CRT-03`         | `fetchAlbums`, `fetchAlbumDetail`, `createAlbum`, `updateAlbum`, `deleteAlbum`                                                                                                                                      |
| `CRT-04`         | `createAlbum`                                                                                                                                                                                                       |
| `CRT-05`         | `fetchAlbumDetail`, `updateAlbum`, `addTrackToAlbum`, `removeTrackFromAlbum`, `reorderAlbumTracks`, `fetchTracks`                                                                                                   |
| `ADM-01`         | `fetchDashboardStats`                                                                                                                                                                                               |
| `ADM-02`         | `fetchUsers`, `updateUserAdmin`                                                                                                                                                                                     |
| `ADM-03`         | `fetchAdminSubscriptionPlans`                                                                                                                                                                                       |
| `ADM-04`         | `fetchUsers`, `fetchUserLicenses`                                                                                                                                                                                   |
| `ADM-05`         | `fetchQuestions`, `updateQuestionStatus`                                                                                                                                                                            |
| `ADM-06`         | `fetchCompanyCerts`, `fetchCompanyCert`, `downloadCompanyCertDocument`, `processCompanyCert`                                                                                                                        |
| `ADM-07`         | `fetchTags`, `createTag`, `updateTag`, `deleteTag`                                                                                                                                                                  |
| `ADM-08`         | `fetchAdminTracks`, `deleteTrack`                                                                                                                                                                                   |
| `ADM-09`         | `fetchAdminUserSubscriptions`, `fetchAdminSubscriptionPlans`; correction modal uses preview/open/detail/create/approve/execute functions                                                                            |
| `ADM-10`         | admin payment order/agreement/payment/incident/receipt/audit/settlement/refund/correction reads; refund and entitlement preview/request/approve/execute; settlement import/reconcile/ignore; incident status update |
| `ADM-11`         | `fetchAdminWhitelistChannels`, `updateAdminWhitelistChannelStatus`, `exportAdminWhitelistChannels`, `downloadAdminWhitelistExportBatch`                                                                             |
| `ADM-12`         | `createNotice`                                                                                                                                                                                                      |
| `ADM-13`         | `fetchNotice`, `updateNotice`, `deleteNotice`                                                                                                                                                                       |
| `ADM-14`         | `getSetting`, `updateSetting`                                                                                                                                                                                       |

Shared consumer boundaries:

- `SH-01`: Header navigation/search and auth Store logout/session actions.
- `SH-02`: `playerStore`, `fetchMySubscription`, `downloadTrack`.
- `SH-03`: browser-local History utilities and Player Store.
- `SH-04`: Playlist CRUD/detail/remove/reorder, Subscription limit read, Likes
  read, and Player Store.
- `SH-05`: `fetchMyPlaylists`, `addTrackToPlaylist`.
- `SH-06`: local Tag selection applied to Track-list URL/request state.
- `SH-07`: owning workflow mutation; ConfirmDialog itself makes no request.

### 18.2 Backend and State Owners

This table supplies the backend and state-owner reference used by the page rows.
Controllers and tables are current declarations, not proof that an individual
scenario passes. A browser WI must still capture the actual request and reload.

| Matrix rows / surfaces                                                        | Frontend boundary                                                                                          | Backend boundary                                                                                                                                   | Canonical state owner                                                                                                                                                                                                                                                                                                                       |
| ----------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `SH-01`, `AUTH-01/02/05/06`, `MEM-04`                                         | `api/auth.ts`, direct `/users/me` updates, `authStore`, `safeStorage`                                      | `AuthController` `/api/auth`, `UserController` `/api/users`                                                                                        | `users`, `social_accounts`; browser session snapshot is a cache, not authority                                                                                                                                                                                                                                                              |
| `AUTH-03`                                                                     | `verifyEmail`                                                                                              | `AuthController`                                                                                                                                   | `email_verification_tokens`, `users.is_verified`                                                                                                                                                                                                                                                                                            |
| `AUTH-04`                                                                     | password reset functions                                                                                   | `AuthController`                                                                                                                                   | `password_reset_tokens`, `users`                                                                                                                                                                                                                                                                                                            |
| `PUB-01/02/03`, `CRT-01/02`, `ADM-08`, `SH-02`, `INV-PLAY/TRACK/SEARCH/IMAGE` | `api/tracks.ts`, `api/tags.ts`, `playerStore`, like/download helpers                                       | `TrackController`, `TagController`, `LikeController`, `DownloadController`, admin audio-analysis boundary                                          | `tracks`, `tags`, `track_tags`, `likes`, `track_downloads`, `storage_mutations`; Player queue/time are browser-local                                                                                                                                                                                                                        |
| `PUB-04/05/06`, `CRT-03/04/05`                                                | `api/albums.ts`, `albumLikeStore`                                                                          | `AlbumController`, `LikeController`                                                                                                                | `albums`, `album_tracks`, `album_likes`, `storage_mutations`                                                                                                                                                                                                                                                                                |
| `PUB-07`, `MEM-10/10S/10F/11`, `ADM-03/09/10`, `INV-SUB`                      | `api/subscriptions.ts`, `api/userSubscriptions.ts`, `api/payments.ts`, payment functions in `api/admin.ts` | `SubscriptionController`, `UserSubscriptionController`, `PaymentController`, `AdminPaymentController`, `AdminUserSubscriptionCorrectionController` | `subscriptions`, `user_subscriptions`, `billing_agreements`, `payment_orders`, `subscription_payments`, `payment_refunds`, `payment_entitlement_corrections`, `payment_reconciliation_incidents`, `payment_receipts`, `payment_settlements`, `payment_operation_audit_logs`, `admin_subscription_corrections`, `admin_operation_audit_logs` |
| `PUB-08/09`, `ADM-12/13`                                                      | `api/notices.ts`                                                                                           | `NoticeController`                                                                                                                                 | `notices`, `notice_attachments`, `storage_mutations`                                                                                                                                                                                                                                                                                        |
| `MEM-01/02/03`, `SH-04/05`                                                    | `api/playlists.ts`, subscription limit read                                                                | `PlaylistController`, `UserSubscriptionController`                                                                                                 | `playlists`, `playlist_tracks`, `user_subscriptions`                                                                                                                                                                                                                                                                                        |
| `MEM-05` and like controls in catalog/player                                  | `api/likes.ts`, `likeStore`, `albumLikeStore`                                                              | `LikeController`                                                                                                                                   | `likes`, `album_likes`                                                                                                                                                                                                                                                                                                                      |
| `MEM-06`, `SH-03`                                                             | local history utilities, `playerStore.fetchPlayableTracks`                                                 | `TrackController` public batch read                                                                                                                | Browser `playHistory` ID list; current active Track projection comes from `tracks`                                                                                                                                                                                                                                                          |
| `MEM-07/08`, `ADM-04`                                                         | `api/licenses.ts`, download helper                                                                         | `LicenseController`                                                                                                                                | `licenses`, related `tracks` and `user_subscriptions`                                                                                                                                                                                                                                                                                       |
| `MEM-09` and download controls                                                | `api/downloads.ts`                                                                                         | `DownloadController`                                                                                                                               | `track_downloads`, `licenses`, related Subscription entitlement                                                                                                                                                                                                                                                                             |
| `MEM-12`, `ADM-11`, `INV-WHITE`                                               | `api/whitelistChannels.ts`, whitelist functions in `api/admin.ts`                                          | `WhitelistChannelController`, `AdminWhitelistChannelController`                                                                                    | `whitelist_channels`, `whitelist_export_batches`, `whitelist_export_items`, related `user_subscriptions`                                                                                                                                                                                                                                    |
| `MEM-13/14`, `ADM-06`, `INV-CERT`                                             | `api/companyCerts.ts`, certification functions in `api/admin.ts`, `api/settings.ts`                        | `CompanyCertificationController`, admin certification endpoints, `SettingController`                                                               | `company_certifications`, `company_certification_documents`, `company_certification_audit_logs`, `site_settings`, `storage_mutations`                                                                                                                                                                                                       |
| `MEM-15/16/17`, `ADM-05`, `INV-QUESTION`                                      | `api/questions.ts`                                                                                         | `QuestionController`                                                                                                                               | `questions`, `answers`, `question_attachments`, `storage_mutations`                                                                                                                                                                                                                                                                         |
| `ADM-01`                                                                      | `fetchDashboardStats`                                                                                      | `AdminStatsController`                                                                                                                             | Aggregates from `users`, `tracks`, `user_subscriptions`                                                                                                                                                                                                                                                                                     |
| `ADM-02`                                                                      | User functions in `api/admin.ts`                                                                           | `UserController` admin mappings                                                                                                                    | `users`, `admin_operation_audit_logs`                                                                                                                                                                                                                                                                                                       |
| `ADM-07`                                                                      | `api/tags.ts`                                                                                              | `TagController`                                                                                                                                    | `tags`, `track_tags`                                                                                                                                                                                                                                                                                                                        |
| `ADM-14`, certification guide reads                                           | `api/settings.ts`                                                                                          | `SettingController`, `AdminSettingController`                                                                                                      | `site_settings`                                                                                                                                                                                                                                                                                                                             |
| `ERR-01/02`                                                                   | Router/error components                                                                                    | No business mutation boundary                                                                                                                      | None                                                                                                                                                                                                                                                                                                                                        |

The current V1 baseline has 41 tables. `docs/design/db-schema.md` is the table
inventory source; `schema.sql` and JPA mappings remain the executable structure.
No schema mutation is part of this WI.

### 18.3 Direct Frontend-to-Backend Contract Reconciliation

A current-source AST/annotation comparison found 131 distinct direct Axios
method/path contracts. Every one normalized to a current backend mapping. The
backend has 13 mappings without a direct `client.get/post/put/delete` match:

| Backend mapping without direct Axios match                       | Current interpretation                                                                                               | Downstream check                                                                                                                                    |
| ---------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------- |
| `POST /api/auth/refresh`                                         | Active infrastructure path called with bare Axios inside the response interceptor, not the shared `client` instance. | Expired-access-token concurrency and logout fallback in WI-022/030.                                                                                 |
| `GET /api/tracks/{trackId}/stream`                               | Active media path assigned directly to `HTMLAudioElement.src`.                                                       | Full playback, seek, error, and access/storage-key boundary in WI-023/030.                                                                          |
| SPA deep-link forward mapping                                    | Active server navigation infrastructure, not an Axios request.                                                       | Refresh every route family and unknown path in WI-021/030.                                                                                          |
| `GET /api/admin/payments/reconciliation`                         | Documented operator/provider reconciliation trigger with no current SPA control.                                     | ADMIN authorization, read/side-effect contract, durable Incident result in WI-028; do not pretend the Incident list invokes it.                     |
| `GET /api/admin/tracks/audio-analysis/dry-run`                   | Documented read-only operator dry-run with no current SPA control.                                                   | ADMIN role, pagination/bounds, report-only/no-update proof in WI-028.                                                                               |
| `GET /api/admin/payments/refunds/{refundId}`                     | Detail support API; current Payment UI operates from list rows.                                                      | API role/shape and list/detail consistency in WI-028; classify UI need separately.                                                                  |
| `GET /api/admin/payments/entitlement-corrections/{correctionId}` | Detail support API; current Payment UI operates from list rows.                                                      | API role/shape and list/detail consistency in WI-028.                                                                                               |
| `GET /api/users/{userId}/licenses/{licenseId}`                   | ADMIN License detail support API; current Admin License UI lists by User.                                            | Ownership/ADMIN role and list/detail consistency in WI-028.                                                                                         |
| `DELETE /api/payments/billing-agreements/me`                     | USER billing-method cancellation boundary with no current SPA wrapper/control.                                       | Determine whether it is intentionally operational/API-only or an unreachable product path during WI-027/031; do not invoke against a real provider. |
| `POST /api/playlists/{playlistId}/tracks/batch`                  | Batch membership API with no current SPA wrapper/control.                                                            | Subscriber ownership, atomicity, limit, duplicate and order contract at API level in WI-024/030.                                                    |
| `GET /api/subscriptions/{subscriptionId}`                        | Public plan-detail support API; current UI consumes list responses.                                                  | Active/inactive visibility and list/detail consistency at API level in WI-027/030.                                                                  |
| `DELETE /api/users/me`                                           | Account-withdrawal API with no current Profile control; social-only withdrawal policy is documented as pending.      | Security/side effects and product-policy classification in WI-022/031; no automatic browser mutation.                                               |
| `GET /api/users/{userId}`                                        | ADMIN User detail support API; current User UI uses list rows and PUT results.                                       | ADMIN role, PII minimization, and list/detail consistency in WI-028.                                                                                |

In addition, `fetchAdminSubscriptionCorrections` is a current frontend API
wrapper with no non-test UI importer. The backend correction-history endpoint
is active, while the modal uses open/detail reads for one selected workflow.
WI-028/031 must classify whether history remains intentionally API-only, should
gain a UI consumer, or is dead wrapper code. No deletion is allowed during the
frozen audit.

### 18.4 Scheduled and Non-Navigation Operations

| Surface                         | Current schedule/entry                                  | Required evidence                                                                                                                                                                       | Effect / WI             |
| ------------------------------- | ------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----------------------- |
| Payment provider reconciliation | One scheduled method plus ADMIN reconciliation endpoint | Test-provider/local comparison, Incident create/update idempotency, bounded window, sanitized warning, no automatic money mutation.                                                     | `S0/S2/S3`; WI-028/030  |
| Subscription lifecycle          | Three scheduled methods                                 | Due renewal, retry/failure/grace/expiry and pending-plan application use a controlled clock/test fixture; one due period cannot charge twice; entitlement and audit/payment rows agree. | `S2/S3`; WI-027/030     |
| Withdrawn-user billing cleanup  | One scheduled coordinator                               | Only eligible withdrawn users, idempotent local/provider-test cleanup, failures remain observable/retriable, no active user is affected.                                                | `S2/S3`; WI-028/030     |
| Storage mutation recovery       | One scheduled service                                   | Pending/failed upload/delete journal entries reconcile storage and DB references without deleting a currently referenced file; retry is idempotent and observable.                      | `S2/S3`; WI-025/029/030 |

Scheduler verification must use existing automated tests or a controlled local
fixture. Wall-clock waiting and production-provider execution are not required
for acceptance, and multi-server locking remains outside the single-server V1
policy.

## 19. Matrix Completion Criteria

- Sections 5 through 10 contain exactly 53 distinct UI rows when callback
  variants `MEM-10S/F` are excluded from the distinct-component count and
  included in the 56 path count.
- All 57 routable declarations are covered by those rows plus `G-ADM-INDEX`.
- All 22 Modal occurrences reconcile through Section 12.
- Every page row names roles/data, action/API contract, state packs/viewports,
  evidence/effect, and a downstream WI.
- Every high-risk mutation has `E3` reload proof; critical durable state adds
  `E4`; binary flows add `E5`; Toss/mail sandbox flows add `E6`.
- No `S4` action is executable without a new explicit user approval.

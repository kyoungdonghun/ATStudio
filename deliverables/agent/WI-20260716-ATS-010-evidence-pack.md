# Evidence Pack: WI-20260716-ATS-010

## Summary (one-liner)

- Integrated the WI-010 frontend slices, fixed four cross-slice stale/lifecycle/hydration defects including the final SubscriberRoute generation fence and Profile form race, aligned directly affected documents, and passed the frontend quality gates without changing product policy.

## Scope / DoD Check

- [x] Track list/filter and Admin Payment Operations reads are latest-request-wins; superseded results cannot commit data, pagination, loading, or errors.
- [x] Track available-filter tags use cancellation and a monotonic generation fence.
- [x] SubscriberRoute uses AbortController plus a monotonic generation fence, treats only structured `403 + NO_ACTIVE_SUBSCRIPTION` as the no-subscription domain outcome, and keeps infrastructure failures visible and retryable.
- [x] Password login preserves only safe internal pathname/query return targets.
- [x] Profile save synchronizes the rendered profile and active/persisted auth user.
- [x] Profile nickname, phone, job, and company fields hydrate in the same initial/save state batch; tests wait for the complete initial form state before editing.
- [x] AddToPlaylistModal invalidates stale work on close/reopen/unmount while remaining stable across parent callback rerenders.
- [x] Player error/stalled/retry and keyboard semantics are covered without adding a preview/listening gate.
- [x] Toast, Pagination, Header search, Modal, and PlayerBar accessibility contracts have focused automated coverage.
- [x] Core retry controls are guarded, idempotent, and distinct from legitimate empty states.
- [x] `/playlists/new` is proven to reuse the existing playlist creation modal.
- [x] Directly affected UI/design documentation matches the implementation.
- [x] Focused/full Vitest, TypeScript, affected ESLint, Vite build, changed-file Prettier, documentation validation, and diff checks pass.

## Reference Documents (Tier 0-2)

**Injected Context** (from WI Handoff Packet):

| Tier | Document | Reason |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Constitution and approved product boundaries |
| 0 | `docs/standards/development-standards.md` | Frontend implementation and verification standards |
| 0 | `docs/standards/documentation-standards.md` | Canonical-document and traceability rules |
| 0 | `docs/standards/glossary.md` | Canonical domain terminology |
| 1 | `docs/policies/quality-gates.md` | Required verification gates |
| 1 | `docs/policies/security-policy.md` | Authentication and sensitive-data boundaries |
| 1 | `docs/policies/access-control-policy.md` | Route/role and execution boundaries |
| 2 | `.agents/skills/react-best-practices/AGENTS.md` | React implementation review guidance |
| 2 | `docs/standards/frontend-standards.md` | React/TypeScript UI conventions |
| 2 | `docs/design/remaining-remediation-design-20260716.md` | Approved remediation scope and invariants |
| 2 | `docs/ui/screen-flow.md` | Route, state, and screen-flow contract |
| 2 | `docs/ui/atstudio-front-list.md` | Frontend/API coverage registry |
| 2 | `docs/design/usecase/sound-playlist.md` | Playlist create/modal lifecycle contract |
| 2 | `deliverables/user/REQ-20260716-ATS-002.md` | Approved requirement and execution scope |
| 2 | `deliverables/agent/WI-20260716-ATS-005-evidence-pack.md` | Preserved payment/security work |
| 2 | `deliverables/agent/WI-20260716-ATS-007-evidence-pack.md` | Preserved whitelist/certification work |
| 2 | `deliverables/agent/WI-20260716-ATS-008-evidence-pack.md` | Preserved cross-layer integrity work |

**Injection Rules Applied**:

- Rule source: `AGENTS.md` and `deliverables/agent/WI-20260716-ATS-010-handoff.md`.
- Assignee context: frontend integration verifier and documentation owner.
- Task type: React integration, regression testing, accessibility, and canonical UI/design alignment.
- Branch boundary: `codex/p1-acceptance-hardening` only; frozen client-demo worktree/runtime excluded.

## State Taxonomy

| State | Commit/render rule | UI treatment |
|---|---|---|
| Initial loading | Current generation only | Loading indication; retry disabled; no stale error |
| Successful data | Current generation and matching view key only | Render current data and pagination |
| Legitimate empty/inactive | Successful domain response | Empty/inactive copy; never rendered as infrastructure failure |
| Authorization/not-found/validation failure | Classified current request | Visible classified failure; no subscriber privilege is granted |
| Infrastructure failure | Current request only | Visible error with one guarded idempotent retry |
| Superseded/cancelled | Never commits | No user-visible error and no overwrite of current state |

## Request-Generation Table

| Surface | Identity / stale fence | Retry/lifecycle rule |
|---|---|---|
| Track list/search/filter/page | AbortController + monotonic generation + current parameters | Guarded idempotent retry; stale success/failure ignored |
| Track available-filter tags | AbortSignal + monotonic generation | Supplementary failure is silent; older tag sets cannot replace current filters |
| Admin Payment Operations | AbortController + monotonic generation + serialized view key | Tab/filter/page read is latest-wins; mutations do not auto-retry and reload the latest view once |
| SubscriberRoute | AbortController + monotonic generation + strict error classifier | Every `then`/`catch`/`finally` commit requires the current generation and a non-aborted signal; retry, unauthenticated transition, and cleanup invalidate prior work |
| Dashboard / Notice / Whitelist loads | Current request fence | One guarded retry; empty state remains distinct |
| AddToPlaylistModal | Open-session lifecycle generation + latest callback refs | Close/reopen/unmount invalidates stale load/add/timer work; parent rerenders do not refetch |

## Route / Role Matrix

| Route/surface | Required access | Verified outcome |
|---|---|---|
| Track list/detail and `/api/tracks/{id}/stream` | Public | Full-track listening preserved; no preview gate |
| Subscriber playlist/download flows | Authenticated active subscriber, plus existing quota/license rules | Only `403 + NO_ACTIVE_SUBSCRIPTION` maps to subscription selection; other failures remain blocked and retryable |
| `/playlists/new` | Active subscriber | Replaces to `/playlists` and opens the existing create modal |
| `/whitelist-channels` | Authenticated USER | Page access remains auth-only; registration action retains existing subscription/plan-limit validation |
| Payment callbacks/manage | USER only | ADMIN is redirected to `/admin/payments`; existing payment gates unchanged |
| `/admin/payments` | ADMIN | Payment Operations route remains admin-only |
| Company certification apply/status | USER + BUSINESS | Non-business and ADMIN boundaries remain unchanged |
| Protected deep link through login | Existing target route role | Safe internal pathname/query restored; external/protocol-relative/malformed/privileged targets rejected |

## Accessibility / Focus Matrix

| Surface | Verified contract | Automated evidence |
|---|---|---|
| ToastContainer | Error uses assertive alert; other toast uses polite status; close has an accessible name | `ToastContainer.test.tsx` |
| Pagination | Labeled navigation/buttons and `aria-current="page"` | `Pagination.test.tsx` |
| Header search | Distinct desktop/mobile search landmarks and accessible input names | `Header.test.tsx` |
| Modal | Only top modal handles Escape/Tab; focus is trapped and restored deterministically | `Modal.test.tsx` |
| PlayerBar | Semantic buttons/sliders, keyboard seek/volume, live failure/stalled feedback, retry | `PlayerBar.test.tsx`, `playerStore.test.ts` |
| AddToPlaylistModal | Closed/reopened sessions cannot receive stale state; callback identity changes do not restart loading | `AddToPlaylistModal.test.tsx` |

## Evidence Pointers (required)

- Integration defects fixed:
  - `frontend/src/api/tags.ts:14` accepts an optional `AbortSignal` for available-tag reads.
  - `frontend/src/pages/public/TrackListPage.tsx:262` through `:294` aborts/fences available-tag requests; `frontend/src/pages/public/TrackListPage.test.tsx:324` proves the newest delayed request wins.
  - `frontend/src/components/playlist/AddToPlaylistModal.tsx:27` through `:34` separates lifecycle generation from callback identity; `:45` through `:114` prevents stale loads/adds/timers from committing.
  - `frontend/src/components/playlist/AddToPlaylistModal.test.tsx:88` proves parent callback identity changes do not restart the playlist load.
- Latest-request-wins and state handling:
  - `frontend/src/pages/public/TrackListPage.tsx:178` through `:246` fences main Track reads; `:250` through `:258` guards retry.
  - `frontend/src/pages/admin/PaymentReadOnlyPage.tsx:188` through `:319` combines cancellation, generation, and current view-key checks.
  - `frontend/src/api/userSubscriptions.ts:58` through `:61` recognizes only `403 + NO_ACTIVE_SUBSCRIPTION`.
  - `frontend/src/router/SubscriberRoute.tsx:23` through `:74` combines AbortController with a monotonic generation fence. Auth transitions and cleanup invalidate the previous generation, and `then`/`catch`/`finally` all require the current generation plus a non-aborted signal.
  - `frontend/src/router/SubscriberRoute.test.tsx:147` and `:176` prove that signal-ignoring stale deferred success and failure cannot replace the latest failure/success state.
- Authentication/profile/navigation:
  - `frontend/src/router/ProtectedRoute.tsx:54` through `:56` preserves pathname + query.
  - `frontend/src/pages/auth/LoginPage.tsx:74` and `:137` validate and apply the safe return target.
  - `frontend/src/pages/subscriber/ProfilePage.tsx:85` and `:228` synchronize the saved user through the auth store.
  - `frontend/src/pages/subscriber/ProfilePage.tsx:123` through `:129` hydrates all initial form fields in the same fetch-result batch; `:234` through `:240` applies the same rule after save instead of relying on a later `[profile]` effect.
  - `frontend/src/pages/subscriber/ProfilePage.test.tsx:91` through `:96` waits for `creator01`, `EDITOR`, and `010-1111-2222` before editing, closing the full-suite hydration race.
  - `frontend/src/store/authStore.ts:65` applies the active/persisted user update.
  - `frontend/src/router/index.tsx:167`, `frontend/src/pages/subscriber/PlaylistCreatePage.tsx:4`, and `frontend/src/router/index.test.tsx:88` prove `/playlists/new` reuses the existing flow.
- Player and accessibility:
  - `frontend/src/store/playerStore.ts:241` uses `/api/tracks/{id}/stream`; `:151` through `:216` handles play failure and stalled/recovered states without a preview timer.
  - `frontend/src/layouts/PlayerBar.tsx:546` through `:565` renders retryable live feedback; `:661`, `:791`, and `:932` expose semantic sliders.
  - `frontend/src/components/ui/Modal.tsx:17` through `:103` implements Escape/Tab handling and focus restoration.
  - `frontend/src/components/ui/ToastContainer.tsx:24` and `:25`, `frontend/src/components/ui/Pagination.tsx:31` through `:67`, and `frontend/src/layouts/Header.tsx:135` through `:259` implement the shared semantics.
- Documentation aligned:
  - `docs/ui/screen-flow.md:43` through `:44`, `:79`, `:116`, and `:134` document safe login return, strict subscriber classification, title + USAGE search, and `/playlists/new`.
  - `docs/ui/atstudio-front-list.md:28`, `:54`, `:89`, and `:163` align the screen/API registry and latest-request behavior.
  - `docs/design/usecase/sound-playlist.md:21` and `:101` document create-flow reuse and stable modal lifecycle.
  - `docs/design/remaining-remediation-design-20260716.md:11` through `:13` records implementation status and unchanged invariants.
- Deliverables:
  - `deliverables/user/WI-20260716-ATS-010-summary.md` is the user-facing completion report.
  - `deliverables/agent/WI-20260716-ATS-010-evidence-pack.md` is this reproducibility record.

## Commands & Outputs

- Profile hydration flaky regression:
  - `for($i=1;$i -le 10;$i++){ npx vitest run src/pages/subscriber/ProfilePage.test.tsx --reporter=dot; if($LASTEXITCODE -ne 0){ exit $LASTEXITCODE } }`
  - Result: PASS, 10/10 runs; 1 file / 5 tests per run; 50/50 tests total.
  - `npx eslint src/pages/subscriber/ProfilePage.tsx src/pages/subscriber/ProfilePage.test.tsx --max-warnings 0`
  - Result: PASS, 2 files, 0 warnings/errors.
  - `npx prettier --check src/pages/subscriber/ProfilePage.tsx src/pages/subscriber/ProfilePage.test.tsx`
  - Result: PASS, 2 files.
- Final MA cross-review correction:
  - `npx vitest run src/router/SubscriberRoute.test.tsx`
  - Result: PASS, 1 test file / 9 tests, including two signal-ignoring stale deferred regressions.
  - `npm run typecheck`
  - Result: PASS; `frontend/tsconfig.tsbuildinfo` SHA-256 remained `B6A42AD2CD32A5AD04D06C55B8B1B26DD9B1894AFDAF7B508DFF16EF0C60F22A` before/after.
  - `npx eslint src/router/SubscriberRoute.tsx src/router/SubscriberRoute.test.tsx --max-warnings 0`
  - Result: PASS, 2 files, 0 warnings/errors.
  - `npx prettier --check src/router/SubscriberRoute.tsx src/router/SubscriberRoute.test.tsx`
  - Result: PASS, 2 files.
- Focused integration regression:
  - `npx vitest run src/components/playlist/AddToPlaylistModal.test.tsx src/pages/public/TrackListPage.test.tsx`
  - Result: PASS, 2 test files / 13 tests.
- Full frontend regression:
  - `npx vitest run`
  - Final result after the SubscriberRoute and Profile hydration corrections: PASS, 38 test files / 180 tests.
- Type safety:
  - `npm run typecheck`
  - Result: PASS. The SHA-256 of the pre-existing dirty `frontend/tsconfig.tsbuildinfo` remained `B6A42AD2CD32A5AD04D06C55B8B1B26DD9B1894AFDAF7B508DFF16EF0C60F22A` before/after the verification rerun.
- Affected lint:
  - `$lintFiles = @(<46 paths in the verification manifest below>); npx eslint @lintFiles --max-warnings 0`
  - Result: PASS, 46 files, 0 warnings/errors.
- Changed-file formatting:
  - `$formatFiles = $lintFiles + @(<10 CSS paths in the verification manifest below>); npx prettier --check @formatFiles`
  - Initial result: four affected files required formatting (`src/api/tags.ts`, `src/components/playlist/AddToPlaylistModal.test.tsx`, `src/components/playlist/AddToPlaylistModal.module.css`, `src/pages/public/TrackListPage.test.tsx`).
  - `npx prettier --write src/api/tags.ts src/components/playlist/AddToPlaylistModal.test.tsx src/components/playlist/AddToPlaylistModal.module.css src/pages/public/TrackListPage.test.tsx`, followed by the same 56-file check.
  - Final result: PASS, 56 files.
- Production bundle:
  - `npx vite build`
  - Result: PASS, 262 modules transformed.
- Documentation:
  - `python .agents/skills/validate-docs/scripts/validate_docs.py`
  - Result: PASS; Tier 0, internal links, traceability IDs, and document-index checks valid.
- Diff integrity:
  - `git diff --check`
  - Result: PASS, exit 0.

### Verification File Manifest

The affected lint and formatting commands above used this exact PowerShell manifest from `frontend/`:

```powershell
$lintFiles = @(
  'src/api/admin.ts',
  'src/api/tags.ts',
  'src/api/tracks.ts',
  'src/api/userSubscriptions.ts',
  'src/api/loadError.ts',
  'src/api/loadError.test.ts',
  'src/components/playlist/AddToPlaylistModal.tsx',
  'src/components/playlist/AddToPlaylistModal.test.tsx',
  'src/components/ui/Modal.tsx',
  'src/components/ui/Modal.test.tsx',
  'src/components/ui/Pagination.tsx',
  'src/components/ui/Pagination.test.tsx',
  'src/components/ui/ToastContainer.tsx',
  'src/components/ui/ToastContainer.test.tsx',
  'src/layouts/Header.tsx',
  'src/layouts/Header.test.tsx',
  'src/layouts/PlayerBar.tsx',
  'src/layouts/PlayerBar.test.tsx',
  'src/pages/admin/DashboardPage.tsx',
  'src/pages/admin/DashboardPage.test.tsx',
  'src/pages/admin/PaymentReadOnlyPage.tsx',
  'src/pages/admin/PaymentReadOnlyPage.test.tsx',
  'src/pages/auth/LoginPage.tsx',
  'src/pages/auth/LoginPage.test.tsx',
  'src/pages/public/NoticeListPage.tsx',
  'src/pages/public/NoticeListPage.test.tsx',
  'src/pages/public/TrackListPage.tsx',
  'src/pages/public/TrackListPage.test.tsx',
  'src/pages/subscriber/PlaylistCreatePage.tsx',
  'src/pages/subscriber/PlaylistListPage.tsx',
  'src/pages/subscriber/PlaylistListPage.test.tsx',
  'src/pages/subscriber/ProfilePage.tsx',
  'src/pages/subscriber/ProfilePage.test.tsx',
  'src/pages/subscriber/WhitelistChannelPage.tsx',
  'src/pages/subscriber/WhitelistChannelPage.test.tsx',
  'src/router/ProtectedRoute.tsx',
  'src/router/ProtectedRoute.test.tsx',
  'src/router/SubscriberRoute.tsx',
  'src/router/SubscriberRoute.test.tsx',
  'src/router/index.tsx',
  'src/router/index.test.tsx',
  'src/store/authStore.ts',
  'src/store/authStore.test.ts',
  'src/store/playerStore.ts',
  'src/store/playerStore.test.ts',
  'src/types/index.ts'
)

$formatFiles = $lintFiles + @(
  'src/components/playlist/AddToPlaylistModal.module.css',
  'src/components/ui/Modal.module.css',
  'src/components/ui/Pagination.module.css',
  'src/components/ui/ToastContainer.module.css',
  'src/layouts/Header.module.css',
  'src/layouts/PlayerBar.module.css',
  'src/pages/admin/DashboardPage.module.css',
  'src/pages/public/NoticeListPage.module.css',
  'src/pages/public/TrackListPage.module.css',
  'src/pages/subscriber/WhitelistChannelPage.module.css'
)
```

## Tests

- ProfilePage hydration suite: 10 consecutive runs, 5 tests per run, 50/50 passed. The previously intermittent individual-profile save now waits for complete nickname/job/phone hydration before editing.
- Final SubscriberRoute suite: 9 tests across 1 file, all passed. This includes two new tests where the stale Promise ignores `AbortSignal`: older success cannot replace the current failure, and older failure cannot replace the current success.
- Focused delayed-response/lifecycle regression: 13 tests across 2 files, all passed.
- Final full Vitest suite: 180 tests across 38 files, all passed.
- Key focused suites include Track stale success/failure/filter tags, Payment stale view results, SubscriberRoute error classification/retry, safe login return, profile-store synchronization, playlist route/modal lifecycle, Player failure/stall/keyboard behavior, and shared accessibility semantics.
- No backend, database, real payment provider, or data-mutating test was run for this frontend-only WI.

## Risks / Rollback

- Risks:
  - `MANUAL-BROWSER`: actual Axios cancellation during throttled navigation has automated deferred-response coverage but no real-network browser observation in this WI.
  - `MANUAL-BROWSER`: nested modal focus return, mobile player expansion/focus, slider keyboard interaction, and media `stalled`/`waiting` behavior need a real-browser pass.
  - `MANUAL-ASSISTIVE-TECH`: semantic assertions do not prove final screen-reader announcement quality.
  - `OUT-OF-SCOPE`: social OAuth callback does not share the password-login `returnTo` flow.
  - `FOLLOW-UP`: repository-wide Prettier and executable coverage baselines belong to WI-011; broad browser regression remains WI-014.
- Rollback:
  - Revert only the WI-010 frontend code/tests and the four directly affected UI/design documents plus these two deliverables.
  - Preserve WI-005 through WI-009 changes, backend/schema/manual patches, runtime logs, `frontend/tsconfig.tsbuildinfo`, and all client-demo branch/runtime state.
  - No data rollback, secret rotation, or Cloudflare action is required.

## Unresolved Conditions / Follow-ups

- Execute the documented manual browser/accessibility matrix in the later broad regression WI.
- Establish repository-wide formatting and coverage gates in the separately approved tooling WI.
- Reconcile broader canonical document counts and stale cross-project references in the documentation WI rather than expanding WI-010.

## Branch / Runtime Integrity

- Development branch: `codex/p1-acceptance-hardening`.
- Frozen client-demo branch was not switched, merged, restarted, or modified; its checkpoint remained `cd876fcf84b3cb2490c27420c6c53a87a35b982d` during integration verification.
- Cloudflare runtime/logs, backend/DB, secrets, and `frontend/tsconfig.tsbuildinfo` were not edited by this integration pass.
- No files were staged or committed.

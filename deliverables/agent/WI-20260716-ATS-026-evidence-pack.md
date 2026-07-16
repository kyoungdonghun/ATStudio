---
version: 1.0
last_updated: 2026-07-16
project: ATS
owner: qa-fe
category: evidence
status: complete
related_wi: WI-20260716-ATS-026
---

# Evidence Pack: WI-20260716-ATS-026

## Summary

- Independent read-only QA found one P1 and two P2 findings in the cumulative frontend remediation state.
- Judgment: `CHANGES_REQUIRED_BEFORE_WI-028`.
- No product invariant, access-boundary, dependency-audit, type, lint, formatting, or existing-test failure was found.

## Findings

### F-026-01 [P1] Admin whitelist and certification lists lack latest-request-wins fencing

**Evidence**

- `frontend/src/pages/admin/WhitelistChannelManagePage.tsx:71-96` issues a list request and unconditionally commits rows, pagination, and editable status state.
- `frontend/src/pages/admin/WhitelistChannelManagePage.tsx:207-212,373` can issue new requests from status and page changes without invalidating the prior request.
- `frontend/src/pages/admin/CompanyCertManagePage.tsx:97-113` unconditionally commits certification rows and pagination for each status/page request.
- `docs/ui/screen-flow.md:55-68` explicitly states that whitelist and certification list screens use latest-request-wins behavior.

**Impact**

- A slower old response can replace the new filter/page result. The displayed control state and visible/editable records can disagree, exposing administrators to wrong-scope review or status actions.

**Focused reproduction/test**

1. Mock two deferred `fetchAdminWhitelistChannels` calls for `PENDING` and `REGISTERED`, or page 1 and page 2.
2. Resolve the newer call, verify its rows, then resolve or reject the older call.
3. Assert that rows, `pageInfo`, `edits`, loading, and error still belong to the newer request.
4. Repeat with deferred `fetchCompanyCerts` calls in `CompanyCertManagePage.test.tsx`.
5. Run `npx vitest run src/pages/admin/WhitelistChannelManagePage.render.test.tsx src/pages/admin/CompanyCertManagePage.test.tsx`.

**Classification**

- Residual behavior defect, not a Prettier change. The loader pattern predates this diff, but both files are changed acceptance surfaces and the accepted current-state document asserts the missing behavior.

### F-026-02 [P2] The new subscriber whitelist load guard drops mutation refreshes

**Evidence**

- `frontend/src/pages/subscriber/WhitelistChannelPage.tsx:80-87` adds `requestId` plus a global `loadBlocked` guard that returns without scheduling another load.
- `frontend/src/pages/subscriber/WhitelistChannelPage.tsx:111-115` releases the guard only when the active load settles.
- Successful save, request, primary, and delete paths rely on `await load()` at `frontend/src/pages/subscriber/WhitelistChannelPage.tsx:181,212,227,250`.
- Per-action busy state at `frontend/src/pages/subscriber/WhitelistChannelPage.tsx:351,407,416-421,435` does not prevent a different action from overlapping.

**Impact**

- A second successful mutation can call `load()` while the first refresh is active and receive an immediate resolved return. If the first refresh captured pre-second-mutation data, the final UI is stale until manual reload.

**Focused reproduction/test**

1. Render two actionable channels and defer the first post-mutation `fetchWhitelistChannels` call.
2. Complete a different mutation while that refresh remains pending.
3. Resolve the first refresh with state captured before the second mutation.
4. Assert that another list request is queued and that final status, slot count, and primary state match the server.
5. Run `npx vitest run src/pages/subscriber/WhitelistChannelPage.test.tsx`.

**Classification**

- Behavior regression introduced by the new `loadBlocked` branch; not formatting-only.

### F-026-03 [P2] Company-certification detail close is not authoritative during loading

**Evidence**

- `frontend/src/pages/admin/CompanyCertManagePage.tsx:115-126` has no request generation, abort, or close-state check around detail loading.
- `frontend/src/pages/admin/CompanyCertManagePage.tsx:129-135` clears detail content but not `detailLoading` and does not invalidate the pending request.
- `frontend/src/pages/admin/CompanyCertManagePage.tsx:284-290` derives dialog openness from `detailLoading || detail || detailError`.
- `frontend/src/components/ui/Modal.tsx:26-32,130-138` correctly forwards Escape and close-button actions; the caller immediately reasserts `open` while loading.

**Impact**

- The dialog cannot be dismissed during a pending detail request, and a late completion can restore content after the user tried to close it. This breaks keyboard/pointer close behavior and focus expectations.

**Focused reproduction/test**

1. Defer `fetchCompanyCert`, open a detail, then press Escape before resolution.
2. Assert immediate dialog removal and focus restoration; resolve the promise and assert it stays closed.
3. Repeat with the close button and a late rejection.
4. Run `npx vitest run src/pages/admin/CompanyCertManagePage.test.tsx src/components/ui/Modal.test.tsx`.

**Classification**

- Residual async/accessibility defect, not introduced by Prettier and not created by the shared modal change.

## Scope / DoD Check

- [x] Reviewed stale-response fencing, cancellation/finally paths, retries, state taxonomy, auth return targets, route access, error visibility, and mutation refresh behavior.
- [x] Checked public listening, download entitlement, recurring card billing, and single-server assumptions.
- [x] Reviewed keyboard, focus, labels, live regions, and responsive overflow/text patterns in changed shared and workflow components.
- [x] Classified formatting-only noise separately from behavior-bearing changes.
- [x] Recorded severity, file/line evidence, impact, and focused test recommendation for every finding.
- [x] Produced both required WI deliverables and made no other worktree change.

## Reference Documents

| Tier    | Document                                               | Reason                                                            |
| ------- | ------------------------------------------------------ | ----------------------------------------------------------------- |
| 0       | `docs/standards/core-principles.md`                    | Product invariants and execution boundaries                       |
| 0       | `docs/standards/development-standards.md`              | Frontend engineering and test expectations                        |
| 0       | `docs/standards/documentation-standards.md`            | Evidence and closure structure                                    |
| 0       | `docs/standards/glossary.md`                           | Canonical role and subscription terms                             |
| 1       | `docs/policies/quality-gates.md`                       | Regression evidence and traceability gates                        |
| 2       | `.agents/skills/react-best-practices/AGENTS.md`        | React effect and data-flow review guidance                        |
| 2       | `docs/standards/frontend-standards.md`                 | Active SPA, route, async, and accessibility standards             |
| 2       | `docs/ui/atstudio-front-list.md`                       | Current frontend inventory                                        |
| 2       | `docs/ui/screen-flow.md`                               | Current role and latest-request-wins contract                     |
| 2       | `docs/design/remaining-remediation-design-20260716.md` | Accepted remediation closure targets                              |
| 2       | `docs/design/api-spec.md`                              | Stream, download, payment, whitelist, and certification contracts |
| Context | `deliverables/user/REQ-20260716-ATS-002.md`            | Approved cumulative remediation scope                             |
| Context | `deliverables/user/WI-20260716-ATS-010-summary.md`     | Shared frontend hardening baseline                                |
| Context | `deliverables/user/WI-20260716-ATS-017-summary.md`     | Claimed remediation and gate baseline                             |
| Context | `deliverables/user/WI-20260716-ATS-022-summary.md`     | Prior readiness boundary                                          |

**Injection rules applied**

- Source: `deliverables/agent/WI-20260716-ATS-026-handoff.md`
- Assignee: `qa-fe`
- Task type: independent cumulative frontend diff audit

## Reviewed Inventory

- Repository identity: branch `codex/p1-acceptance-hardening`, HEAD `cd876fcf84b3cb2490c27420c6c53a87a35b982d`.
- Tracked frontend diff: 158 files, 5,239 insertions, 2,726 deletions.
- Untracked frontend review input: 30 source/test files. Excluded without touching: `frontend/vite.err.log` and `frontend/vite.out.log`.
- API and state: auth/client/admin/company-certification/download/subscription/whitelist adapters, load-error taxonomy, auth/player stores, safe URL and OAuth utilities.
- Routes and access: `ProtectedRoute`, `SubscriberRoute`, router composition, login/social-login continuity, USER-only payment and BUSINESS-only certification routes.
- Workflows: catalog/track detail, public notice, player, download history, payment admin, user admin, whitelist, certification, profile, subscription checkout.
- Shared UI: modal focus stack, pagination, toast live regions, header controls, buttons, filters, responsive table wrappers.
- Config/dependencies: package manifests, Vite proxy/test config, ESLint, Prettier, TypeScript configuration.

## Formatting-Only Classification

A read-only formatted-HEAD byte comparison proved these 24 tracked CSS modules are exact Prettier-only changes:

- `frontend/src/components/album/AlbumCard.module.css`
- `frontend/src/components/filter/TagFilterModal.module.css`
- `frontend/src/components/player/PlaylistDrawer.module.css`
- `frontend/src/components/playlist/AddToPlaylistModal.module.css`
- `frontend/src/components/track/TrackRow.module.css`
- `frontend/src/components/ui/FilterChip.module.css`
- `frontend/src/components/ui/Tag.module.css`
- `frontend/src/layouts/AdminLayout.module.css`
- `frontend/src/pages/admin/NoticeCreatePage.module.css`
- `frontend/src/pages/admin/NoticeEditPage.module.css`
- `frontend/src/pages/admin/TagManagePage.module.css`
- `frontend/src/pages/admin/TrackManagePage.module.css`
- `frontend/src/pages/admin/UserManagePage.module.css`
- `frontend/src/pages/auth/SignupPage.module.css`
- `frontend/src/pages/public/AlbumListImagePage.module.css`
- `frontend/src/pages/public/AlbumListPage.module.css`
- `frontend/src/pages/public/SubscriptionPlanPage.module.css`
- `frontend/src/pages/public/TrackDetailPage.module.css`
- `frontend/src/pages/subscriber/LikeListPage.module.css`
- `frontend/src/pages/subscriber/PlaylistDetailPage.module.css`
- `frontend/src/pages/subscriber/PlaylistEditPage.module.css`
- `frontend/src/pages/subscriber/PlaylistListPage.module.css`
- `frontend/src/pages/subscriber/ProfilePage.module.css`
- `frontend/src/pages/subscriber/QuestionListPage.module.css`

`git diff --ignore-all-space` additionally confirmed format-only TSX examples including `LikeListPage.tsx`, `SubscriptionPlanPage.tsx`, `AlbumDetailPage.tsx`, `PlaylistDetailPage.tsx`, and `TrackUploadPage.tsx`. Mixed files were treated as behavior-bearing and reviewed semantically; raw line counts were not treated as behavioral evidence.

## Product Invariant and Boundary Evidence

- Public full-track playback: `frontend/src/store/playerStore.ts:6,241` still assigns `/api/tracks/{id}/stream` to the audio element without a preview limit.
- Download entitlement: `frontend/src/layouts/PlayerBar.tsx:119,330,809,1033` distinguishes active subscription state and only renders official download actions for ADMIN or authenticated active subscribers.
- Recurring billing card: `frontend/src/pages/subscriber/SubscriptionPaymentPage.tsx:7-10,85,145-150,182-183` still prepares and confirms a billing agreement through the Toss payment SDK; legacy redirect handling remains explicit.
- Route access: `frontend/src/router/index.tsx:123-136,175-204` preserves USER-only payment and USER+BUSINESS certification boundaries.
- Single server: `frontend/vite.config.ts:106-116,131-139` keeps the frontend on 5173 and proxies `/api` and `/uploads` to `127.0.0.1:8080`; `frontend/src/api/client.ts:8,167-172` uses same-origin paths.
- Fenced examples: track list `frontend/src/pages/public/TrackListPage.tsx:69,178-184`; payment admin `frontend/src/pages/admin/PaymentReadOnlyPage.tsx:190,208-215`; download history `frontend/src/pages/subscriber/DownloadQueuePage.tsx:73-115`; user admin `frontend/src/pages/admin/UserManagePage.tsx:33-59`.
- Auth continuity: `frontend/src/pages/auth/LoginPage.tsx:75`, `frontend/src/router/ProtectedRoute.tsx:54-55`, and `frontend/src/utils/oauthAttempt.ts:38-77,90-140` keep internal-only, bounded, per-attempt return targets.
- Accessibility: `frontend/src/components/ui/Modal.tsx:26-105,115-138` supplies topmost Escape handling, focus trap/restore, dialog semantics, and a named close button; `frontend/src/components/ui/ToastContainer.tsx:25` supplies severity-sensitive live regions. F-026-03 is a caller-state failure, not a shared-modal failure.

## Verification Commands and Results

All commands were run from `frontend/` unless a repository-root command is shown.

```powershell
npx vitest run src/router/ProtectedRoute.test.tsx src/router/SubscriberRoute.test.tsx src/router/index.test.tsx src/layouts/PlayerBar.test.tsx src/store/playerStore.test.ts src/components/ui/Modal.test.tsx src/components/ui/Pagination.test.tsx src/components/ui/ToastContainer.test.tsx src/layouts/Header.test.tsx src/pages/public/TrackListPage.test.tsx src/pages/public/TrackDetailPage.test.tsx src/pages/public/SubscriptionPlanPage.test.tsx src/pages/subscriber/DownloadQueuePage.test.tsx src/pages/subscriber/WhitelistChannelPage.test.tsx src/pages/admin/PaymentReadOnlyPage.test.tsx src/pages/admin/CompanyCertManagePage.test.tsx src/pages/admin/WhitelistChannelManagePage.render.test.tsx src/pages/admin/WhitelistChannelManagePage.test.ts src/api/adminWhitelistChannels.test.ts src/utils/oauthAttempt.test.ts src/utils/safeReceiptUrl.test.ts src/utils/safeYoutubeUrl.test.ts
```

- PASS: 22 test files, 133 tests.

```powershell
npx vitest run
npx tsc --noEmit --incremental false
npx eslint src/router/ProtectedRoute.tsx src/router/SubscriberRoute.tsx src/router/index.tsx src/layouts/PlayerBar.tsx src/store/playerStore.ts src/components/ui/Modal.tsx src/pages/public/TrackListPage.tsx src/pages/public/TrackDetailPage.tsx src/pages/subscriber/DownloadQueuePage.tsx src/pages/subscriber/WhitelistChannelPage.tsx src/pages/admin/PaymentReadOnlyPage.tsx src/pages/admin/CompanyCertManagePage.tsx src/pages/admin/WhitelistChannelManagePage.tsx src/api/admin.ts src/api/loadError.ts src/utils/oauthAttempt.ts src/utils/safeReceiptUrl.ts src/utils/safeYoutubeUrl.ts
npx prettier --check . --ignore-unknown
npm audit --omit=dev --audit-level=low
npm audit --audit-level=low
```

- PASS: full Vitest 44 files / 242 tests; typecheck; focused ESLint; full-tree Prettier; both audits with 0 vulnerabilities.

```powershell
git diff --check -- frontend
```

- PASS from repository root; output contained LF-to-CRLF warnings only.
- `frontend/tsconfig.tsbuildinfo` remained 5,421 bytes with SHA-256 `B6A42AD2CD32A5AD04D06C55B8B1B26DD9B1894AFDAF7B508DFF16EF0C60F22A`.

## Residual Coverage and Exclusions

- Existing tests do not schedule the reverse-order list responses, overlapping whitelist mutations, or close-during-detail-load cases described in the findings; therefore the passing 242-test suite does not refute them.
- No live browser or responsive screenshot run was performed because this WI forbids touching or restarting the shared runtime. Responsive review was static plus jsdom component coverage.
- No build command was run because it writes generated output and this WI permits changes only to the two deliverables.
- No backend, DB, provider, client worktree, runtime log, Git index, commit, branch, remote, or server state was changed.

## Rollback

- This audit changes only the two WI-026 deliverables. Removing those two files is the complete rollback for this WI.
- Implementation follow-up should preserve current product invariants and add the focused tests before rerunning WI-028 acceptance.

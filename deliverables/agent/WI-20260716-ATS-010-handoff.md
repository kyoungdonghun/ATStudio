[WI HEADER]
WI ID: WI-20260716-ATS-010
REQ: REQ-20260716-ATS-002
Agent: se
Depends On: WI-20260716-ATS-005, WI-20260716-ATS-007, WI-20260716-ATS-008
Blocks: WI-20260716-ATS-011, WI-20260716-ATS-012, WI-20260716-ATS-014, WI-20260716-ATS-015, WI-20260716-ATS-016

[WI SUMMARY]
Why: Close the confirmed frontend stale-response, error-classification, retry, lifecycle, keyboard, and focus gaps without changing ATStudio product policy or propagating work to the frozen client-demo branch.
Scope (in):
- Establish one explicit async view-state vocabulary for affected screens: initial loading, successful data, legitimate empty/inactive state, authorization/not-found/validation failure, infrastructure failure, and superseded/cancelled request.
- Make Track list/filter requests and Admin Payment Operations tab/filter/page requests latest-request-wins using AbortController/Axios signal or a monotonic generation fence. Superseded requests must not overwrite data, pageInfo, loading, or errors.
- Make SubscriberRoute distinguish the approved no-subscription domain outcome from authentication, timeout, network, and server failures; infrastructure failures remain visible and retryable instead of being reduced to no subscription.
- Preserve protected pathname and query through login with a same-origin internal-route representation and an open-redirect-safe fallback. Existing ADMIN payment redirect and BUSINESS certification route gates remain intact.
- After profile update, update the active auth-store user and persisted bootstrap atomically so Header and later route decisions see the saved profile without reload.
- Finish lifecycle cleanup for AddToPlaylistModal: stale load responses and old success timers cannot affect a closed or reopened modal.
- Preserve and verify the existing Player store contract: playing becomes true only after play succeeds; error/stalled states are visible and retryable; no preview limit or listening restriction is introduced. Add the missing keyboard/focus behavior around PlayerBar controls where confirmed.
- Close shared accessibility findings in ToastContainer, Pagination, Header search, Modal focus return, and PlayerBar controls using native semantics, accessible names, live regions, aria-current, keyboard activation, and deterministic focus restoration.
- Add in-screen retry to the confirmed core load-failure surfaces (TrackListPage, DashboardPage, NoticeListPage, WhitelistChannelPage) while preserving legitimate empty states and preventing duplicate retry requests.
- Verify the existing `/playlists/new` route renders the create workflow; do not reimplement it when current code/tests already satisfy the contract.
- Add focused deferred-response, cancellation, retry, keyboard, focus, and state-synchronization tests, then align directly affected UI/design documentation.
Scope (out):
- Product redesign, visual restyling, new marketing copy, new backend endpoints, database/schema changes, live provider calls, new preview behavior, listening/download/payment policy changes, multi-server work, or client-demo propagation.
- Introducing an accessibility/runtime/query-state library without separate approval. Use the existing React, React Router, Axios, Zustand, RTL, and Vitest stack.
- Treating cancellation as a user-visible failure, retrying non-idempotent mutations automatically, or globally rewriting every page in the SPA.
DoD:
- Confirmed stale responses cannot commit after a newer request, and deferred-response tests prove the ordering contract.
- Infrastructure errors, authorization failures, legitimate empty states, and superseded requests are rendered differently on the affected paths.
- Deep links, profile state, modal lifecycle, retry controls, Player feedback, keyboard interaction, and focus restoration have focused automated coverage.
- Existing payment, whitelist, certification, full-track listening, and download contracts remain unchanged.
- Affected Vitest, TypeScript, ESLint, Vite build, changed-file Prettier, docs validation, and diff checks pass.
Constraints/Forbidden:
- Work only in `codex/p1-acceptance-hardening` under `C:\Users\jm991\Desktop\project\ATStudio`.
- Do not modify, switch, merge, restart, or propagate to `codex/client-demo-stable`, its worktree, or the Cloudflare runtime.
- Do not execute DDL, mutate real data, inspect secrets, change backend behavior, or stage runtime log/PID/build-info artifacts.
- Preserve WI-005 through WI-009 and unrelated dirty-worktree edits. Shared router, payment, whitelist, and docs files must be merged rather than replaced.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Track filters and payment admin tab/page/filter changes are latest-request-wins; an older success or failure cannot overwrite newer state.
- [ ] SubscriberRoute renders no-subscription only for the approved domain outcome and provides retry for network/timeout/5xx failures without granting subscriber access.
- [ ] Login returns to the original internal pathname and query; external, protocol-relative, malformed, and privileged fallback targets are rejected safely.
- [ ] Profile save updates both page data and the global/persisted auth user in one successful flow.
- [ ] AddToPlaylistModal aborts/ignores stale loads and clears delayed-close timers on close, reopen, and unmount.
- [ ] PlayerBar exposes recoverable play failure/stalled feedback, semantic keyboard controls, and no regression to full public listening.
- [ ] Toast, Pagination, Header search, Modal, and PlayerBar satisfy the specified live-region, name, current-state, keyboard, and focus-return contracts.
- [ ] Retry controls rerun failed idempotent loads once, remain disabled while loading, and do not replace legitimate empty states.
- [ ] `/playlists/new` is proven to render the current create workflow; no duplicate route behavior is added.
Performance:
- [ ] Superseded requests are aborted where supported or prevented from committing by a monotonic fence; no unbounded timers, listeners, or duplicate retry loops remain.
- [ ] Shared helpers remove real duplication without forcing a broad component rewrite or new dependency.
Quality:
- [ ] Deferred promises prove stale success and stale failure ordering for TrackListPage and PaymentReadOnlyPage.
- [ ] Focused tests cover SubscriberRoute outcomes, safe deep-link return, profile synchronization, modal lifecycle, retry UI, Player failure/stalled/retry, and keyboard/focus behavior.
- [ ] Affected Vitest, `npm run typecheck`, affected ESLint, `npm run build`, changed-file Prettier, docs validation, and `git diff --check` pass.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 1 (Policies):
- docs/policies/quality-gates.md
- docs/policies/security-policy.md
- docs/policies/access-control-policy.md

Tier 2 (React / UI / Approved Design):
- .agents/skills/react-best-practices/AGENTS.md
- docs/standards/frontend-standards.md
- docs/design/remaining-remediation-design-20260716.md
- docs/ui/screen-flow.md
- docs/ui/atstudio-front-list.md
- deliverables/user/REQ-20260716-ATS-002.md
- deliverables/agent/WI-20260711-ATS-003-evidence-pack.md
- deliverables/agent/WI-20260711-ATS-006-evidence-pack.md
- deliverables/agent/WI-20260711-ATS-008-evidence-pack.md
- deliverables/agent/WI-20260716-ATS-005-evidence-pack.md
- deliverables/agent/WI-20260716-ATS-007-evidence-pack.md
- deliverables/agent/WI-20260716-ATS-008-evidence-pack.md

Files:
- frontend/src/router/ProtectedRoute.tsx
- frontend/src/router/SubscriberRoute.tsx
- frontend/src/router/index.tsx
- frontend/src/pages/auth/LoginPage.tsx
- frontend/src/pages/public/TrackListPage.tsx
- frontend/src/pages/public/NoticeListPage.tsx
- frontend/src/pages/admin/DashboardPage.tsx
- frontend/src/pages/admin/PaymentReadOnlyPage.tsx
- frontend/src/pages/subscriber/ProfilePage.tsx
- frontend/src/pages/subscriber/WhitelistChannelPage.tsx
- frontend/src/components/playlist/AddToPlaylistModal.tsx
- frontend/src/components/ui/ToastContainer.tsx
- frontend/src/components/ui/Pagination.tsx
- frontend/src/components/ui/Modal.tsx
- frontend/src/layouts/Header.tsx
- frontend/src/layouts/PlayerBar.tsx
- frontend/src/store/authStore.ts
- frontend/src/store/playerStore.ts
- frontend/src/api/client.ts
- matching `*.test.ts` / `*.test.tsx` / CSS module files

Repro / Inspection:
- `rg -n "loadTracks|loadData|setLoading|setError|AbortController|signal|requestId|fetchMySubscription|setTimeout|audio.play|stalled|aria-live|aria-current|focus\\(" frontend/src`
- Inspect existing tests before editing; Player and `/playlists/new` may already contain positive controls that must be verified and preserved.
- Use deferred promises and fake timers. Do not rely on arbitrary sleeps.

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260716-ATS-010-summary.md:
- Behavior changes, unchanged product policy, test results, residual risks, and browser/manual follow-ups.
Agent-facing -> deliverables/agent/WI-20260716-ATS-010-evidence-pack.md:
- State taxonomy, request-generation table, route/role matrix, accessibility/focus matrix, exact pointers, commands/results, rollback, and unresolved conditions.
Handoff Packet -> deliverables/agent/WI-20260716-ATS-010-handoff.md:
- This packet.

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required.
Tests: Include exact focused and combined commands with file/test counts. A visual claim requires DOM/keyboard/focus assertions or later browser evidence; source inspection alone is not enough.
Rollback: Revert WI-010 code/tests/docs together without reverting WI-005 through WI-009 or touching the frozen client-demo branch.
Product boundary: Full public Track listening, subscription-gated/quota-limited downloads, recurring card billing, single-server operation, whitelist state, and company certification behavior remain unchanged.

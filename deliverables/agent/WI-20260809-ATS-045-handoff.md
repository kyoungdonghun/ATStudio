[WI HEADER]
WI ID: WI-20260809-ATS-045
REQ: REQ-20260809-ATS-001
Agent: se
Depends On: WI-20260809-ATS-037
Blocks: WI-20260809-ATS-046, WI-20260809-ATS-047, WI-20260809-ATS-059, WI-20260809-ATS-069

[WI SUMMARY]
Why: `CR-031-042`, `CR-031-045`, and `CR-031-049` identify one bounded member-state group: late list/detail responses can overwrite newer route, tab, page, or reopened-drawer state; malformed or nonpositive detail IDs can leave loading active forever; and Playlist creation can be enabled from a silent default or stale subscription capacity.
Scope (in/out):
- In: Give member Playlist, Like, License, Question, and adjacent Download load effects cancellation or generation ownership so only the latest mounted route/query/tab/dialog owner may commit data, error, empty, or loading completion.
- In: Terminate malformed, noninteger, zero, and negative Playlist/License/Question detail IDs in a localized product-owned error or not-found state with a safe recovery action; never issue a request for an invalid ID.
- In: Represent Playlist plan capacity as loading, known, or error rather than silently substituting a creation limit when the subscription request fails.
- In: Prevent Playlist creation while capacity is unknown or failed, expose bounded retry, and clear stale capacity when the authenticated/open owner changes.
- In: Add focused StrictMode/deferred-response tests plus adjacent member-route regressions for stale success/failure/loading suppression, invalid IDs, failed/retried capacity loads, and drawer close/reopen ownership.
- In: Update current member loading/error-state documentation and WI evidence when implementation changes the documented contract.
- Out: Playlist destructive confirmation, mutation retry, and preview URL cleanup owned by WI-046; Question owner/status/attachment behavior owned by WI-047; keyboard semantics and broken-image fallback owned by WI-059; backend/API schema/data changes; live authenticated mutation/download/export/provider/mail/payment effects.
DoD: All three canonical roots are corrected without changing product capacity rules or mutation semantics; stale/aborted loads cannot commit any state; invalid detail routes always terminate; Playlist creation requires a known current capacity; focused, adjacent, and full quality gates pass; current docs and evidence match implementation.
Constraints/Forbidden:
- Do not infer a Playlist limit from a hard-coded default when the subscription request fails or is still pending.
- Preserve the backend as the final authority for creation limits and preserve the currently returned positive `subscription.maxPlaylists` contract.
- Do not allow an aborted or stale request to commit data, error, empty state, selected detail, capacity, or loading completion.
- Do not change delete, reorder, add/remove Track, Question status, answer, attachment, download, or other mutation semantics owned by later WIs.
- Do not add dependencies, change backend/API/schema/data, perform authenticated mutation/download/export/provider/mail/payment operations, or inspect ignored local configuration.
- Do not touch, inspect, stage, or delete `output/client-demo-screenshots-20260716-140514.zip` or `output/ui-ux-audit/`.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Rapid Playlist/Like/License/Question/Download route, query, tab, and drawer-owner changes cannot let an older success or failure overwrite the latest state.
- [ ] Closing and reopening Playlist Drawer starts a new ownership generation; an earlier list/detail/likes response cannot populate the reopened drawer.
- [ ] Invalid Playlist, License, and Question detail IDs issue no request, leave no permanent loading state, and render localized recovery/navigation.
- [ ] Playlist list and Drawer distinguish capacity loading, known, and failed states; creation is available only when the latest capacity is known and the current count is below it.
- [ ] Capacity failure never falls back to an arbitrary limit or preserves a prior authenticated/open owner's value; retry has one in-flight request and can recover.
Performance:
- [ ] Request ownership uses AbortSignal or constant-time generation checks without polling, duplicate loops, or new global listeners.
- [ ] Retry and drawer reopen produce at most one current request per owned resource.
Quality:
- [ ] Focused RED/GREEN tests cover stale success/failure/loading, StrictMode mount cleanup, invalid/nonpositive IDs, capacity failure/retry, and drawer close/reopen.
- [ ] Adjacent Playlist, Like, License, Question, Download, router, player, and subscription tests pass.
- [ ] Frontend full tests, coverage, typecheck, ESLint, Prettier, and build pass.
- [ ] Backend full tests/build remain green because shared contracts must not regress.
- [ ] Current member loading/error-state documentation matches implementation; docs validation and `git diff --check` pass.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 1 (Policies - Inferred from quality/access scope):
- docs/policies/quality-gates.md
- docs/policies/access-control-policy.md
- docs/standards/evidence-pack-standard.md

Tier 2 (React and current contracts):
- docs/standards/frontend-standards.md
- .agents/skills/react-best-practices/AGENTS.md
- docs/ui/screen-flow.md
- docs/ui/atstudio-front-list.md
- docs/design/usecase/sound-playlist.md
- docs/design/usecase/user-license.md
- docs/design/usecase/user-question.md
- docs/design/usecase/download-queue.md
- docs/design/usecase/user-subscription.md
- docs/design/api-spec.md

REQ/Context Docs:
- deliverables/user/REQ-20260809-ATS-001.md
- deliverables/agent/WI-20260809-ATS-024-findings.md
- deliverables/agent/WI-20260809-ATS-024-evidence-pack.md
- deliverables/agent/WI-20260809-ATS-031-consolidated-findings.md
- deliverables/agent/WI-20260809-ATS-037-evidence-pack.md

Files:
- frontend/src/pages/subscriber/PlaylistListPage.tsx
- frontend/src/components/player/PlaylistDrawer.tsx
- frontend/src/pages/subscriber/LikeListPage.tsx
- frontend/src/pages/subscriber/LicenseListPage.tsx
- frontend/src/pages/subscriber/QuestionListPage.tsx
- frontend/src/pages/subscriber/PlaylistDetailPage.tsx
- frontend/src/pages/subscriber/PlaylistEditPage.tsx
- frontend/src/pages/subscriber/LicenseDetailPage.tsx
- frontend/src/pages/subscriber/QuestionDetailPage.tsx
- frontend/src/pages/subscriber/DownloadHistoryPage.tsx
- frontend/src/api/playlists.ts
- frontend/src/api/likes.ts
- frontend/src/api/licenses.ts
- frontend/src/api/questions.ts
- frontend/src/api/userSubscriptions.ts
- corresponding focused and adjacent test/style files

Repro/Logs:
- Use Vitest deferred promises, StrictMode, MemoryRouter/createMemoryRouter, mocked APIs with AbortSignal observation, and drawer close/reopen state only.
- Exercise malformed, decimal, zero, and negative route IDs; rapid route/page/filter/tab changes; unmount/close before resolution; capacity success then failure; failure then retry; stale capacity from an earlier owner.
- Do not invoke real authenticated mutations, downloads, attachments, exports, provider, mail, payment, or local secret configuration.

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260809-ATS-045-summary.md:
- Korean summary of latest-request ownership, terminal invalid-ID recovery, explicit Playlist capacity states, unchanged mutation/product policy, verification, and residual boundaries.
Agent-facing -> deliverables/agent/WI-20260809-ATS-045-evidence-pack.md:
- Root-to-code/test evidence, RED/GREEN proof, commands, rollback, and follow-up chain.
Handoff Packet -> deliverables/agent/WI-20260809-ATS-045-handoff.md:
- This packet.

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required for `CR-031-042`, `CR-031-045`, and `CR-031-049`.
Tests: Record focused deferred-response and invalid-ID cases, adjacent member/router/player/subscription suites, full quality gates, and explicit proof that mutation semantics remain unchanged.
Rollback: Revert member load ownership, invalid-ID terminal states, capacity-state presentation, API AbortSignal options, tests, docs, and WI deliverables as one patch. No data rollback is permitted or expected.

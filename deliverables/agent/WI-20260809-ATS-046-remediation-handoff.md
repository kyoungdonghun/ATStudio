[WI HEADER]
WI ID: WI-20260809-ATS-046-REMEDIATION
REQ: REQ-20260809-ATS-001
Agent: se
Depends On: WI-20260809-ATS-046-QA-FE-REVIEW
Blocks: WI-20260809-ATS-046 closure

[WI SUMMARY]
Why: Independent QA found a passive-effect lifecycle gap and an incomplete stale-boundary regression matrix.
Scope (in): Synchronize AddToPlaylistModal open/Track lifecycle retirement; fence detached controls before passive effects; add Track replacement/list/add/timer tests; add in-flight Drawer remove/delete retirement tests for back/tab/close/detail/session boundaries as practical; reduce PlaylistDrawer formatting-only churn without changing behavior.
Scope (out): WI-057 focus/keyboard/dialog semantics, WI-059 card/image/keyboard semantics, backend/schema/data/product policy, real authenticated mutations or external effects.
DoD: The reported passive-effect race is impossible; stale controls cannot start or commit an add; old list/add/timer completions cannot affect a replacement Track; in-flight Drawer destructive completions are ignored after retirement; focused tests pass; existing behavior remains unchanged.
Constraints/Forbidden: Preserve WI-037 zero-based reorder, WI-045 owner/read projection, and all existing WI-046 behavior. Use the smallest safe state-machine change. Do not edit summary/evidence files. Do not inspect ignored secrets or protected output artifacts.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] `(open, trackId)` replacement is synchronously visible to render-time guards before passive effects.
- [ ] Detached Add controls cannot start a request after close or Track replacement commit.
- [ ] Stale list/add/success timer completions cannot mutate the replacement lifecycle.
- [ ] Drawer mutation completion cannot update UI or refresh after its detail/session is retired.
- [ ] PlaylistDrawer formatting churn is minimized where this does not introduce structural risk.
Quality:
- [ ] Focused tests reproduce the QA finding before the fix and pass afterward.
- [ ] All existing focused tests remain green; typecheck and scoped format/diff checks pass.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md

Tier 0 (Standards - Based on Assignee):
- docs/standards/development-standards.md

Tier 1 (Policies - Inferred):
- docs/policies/quality-gates.md

Tier 2 (Frontend):
- .agents/skills/react-best-practices/AGENTS.md
- docs/standards/frontend-standards.md

REQ/Context Docs:
- deliverables/user/REQ-20260809-ATS-001.md
- deliverables/agent/WI-20260809-ATS-046-handoff.md
- deliverables/agent/WI-20260809-ATS-046-qa-fe-review-handoff.md

Files:
- frontend/src/components/playlist/AddToPlaylistModal.tsx
- frontend/src/components/playlist/AddToPlaylistModal.test.tsx
- frontend/src/components/player/PlaylistDrawer.tsx
- frontend/src/components/player/playerComponents.test.tsx
- frontend/src/components/ui/Modal.tsx (read only)

Repro/Logs:
- QA P2: AddToPlaylistModal lifecycle refs retire only in passive effect and handleAdd does not validate current open/Track render lifecycle.
- QA P3: missing Track replacement and in-flight Drawer retirement tests.

[OUTPUT CONTRACT]
Chat-only implementation report:
- Exact files changed, RED/GREEN test evidence, and any residual limitation.
- Do not create final WI summary/evidence files.

[TRACEABILITY REQUIREMENTS]
Evidence pointers: Required for lifecycle key/guard and each added regression.
Tests: Exact commands and counts.
Rollback: Revert remediation changes together with the WI-046 implementation.

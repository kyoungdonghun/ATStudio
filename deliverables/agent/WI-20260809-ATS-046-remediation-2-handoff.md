[WI HEADER]
WI ID: WI-20260809-ATS-046-REMEDIATION-2
REQ: REQ-20260809-ATS-001
Agent: se
Depends On: WI-20260809-ATS-046-QA-FE-REREVIEW
Blocks: WI-20260809-ATS-046 closure

[WI SUMMARY]
Why: Rereview found pending-state leakage across Playlist detail replacement and a one-frame stale Add-to-Playlist projection after Track replacement.
Scope (in): Bind Drawer pending ownership to a unique operation token and initiating detail; retire visible pending immediately on detail retirement without allowing an old completion to clear a newer operation; bind Add-to-Playlist rendered state and handlers to a render-time `(open, trackId)` lifecycle key so replacement immediately shows loading rather than prior selection/result; add faithful regressions.
Scope (out): Later accessibility WIs, backend/schema/data/product policy, real effects.
DoD: An unresolved retired mutation never disables replacement-detail controls; old and new same-target operations cannot impersonate each other; Track replacement render exposes no prior playlist selection/result and stale controls cannot start work; focused tests pass.
Constraints/Forbidden: Preserve every prior WI-046 behavior, WI-037 zero-based reorder, and WI-045 owner/read projection. Use unique monotonic operation identity rather than mutation key alone. Do not edit final summary/evidence or protected outputs.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Back/detail retirement clears visible pending synchronously while old request remains unresolved.
- [ ] A newer operation stays pending when an older operation settles, even for the same target key.
- [ ] Replacement detail delete/remove controls are usable before the old request settles.
- [ ] Add modal derives current display from a render-time lifecycle key; stale selection/result is hidden immediately on Track replacement.
- [ ] Old control/list/add/timer cannot start or commit against the replacement lifecycle.
Quality:
- [ ] RED tests reproduce both rereview findings.
- [ ] Focused 4-file test suite, typecheck, scoped format, and diff check pass.

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
- deliverables/agent/WI-20260809-ATS-046-qa-fe-rereview-handoff.md

Files:
- frontend/src/components/player/PlaylistDrawer.tsx
- frontend/src/components/player/playerComponents.test.tsx
- frontend/src/components/playlist/AddToPlaylistModal.tsx
- frontend/src/components/playlist/AddToPlaylistModal.test.tsx

Repro/Logs:
- QA P2: retired in-flight Drawer mutation leaves replacement detail controls disabled until old settlement.
- QA P3: Track replacement hides actions but renders previous projection until passive reset.

[OUTPUT CONTRACT]
Chat-only implementation report with exact files, RED/GREEN evidence, and residual limits. Do not create final WI summary/evidence.

[TRACEABILITY REQUIREMENTS]
Evidence pointers: Required for unique operation identity and render lifecycle projection.
Tests: Exact commands and counts.
Rollback: Revert remediation-2 with the WI-046 patch.

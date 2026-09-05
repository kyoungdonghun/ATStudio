# WI Handoff Packet: WI-20260823-ATS-004

[WI HEADER]
WI ID: WI-20260823-ATS-004
REQ: REQ-20260823-ATS-001
Agent: se
Depends On: WI-20260823-ATS-002
Blocks: WI-20260823-ATS-003, WI-20260823-ATS-005

[WI SUMMARY]
Why: Correct the three confirmed P2 regressions found by independent verification of WI-001 before documentation and final review.

Scope (in):
- Preserve a user-selected Likes tab on generic PlaylistDrawer reopen while allowing an explicit PlayerBar Likes action to request the Likes tab and an explicit Playlist action to request the Playlists tab.
- Align the stale broad frontend coverage test with the approved BUSINESS label `회사명 또는 업종`; do not restore the former label.
- Apply Prettier only to the four WI-owned files reported by the scoped quality check.
- Extend/fix focused tests to prove the reopen/stale-response behavior.

Scope (out):
- No product-policy, DB/data/storage, provider/mail/payment, HomePage, client-worktree, or unrelated formatting change.

DoD:
- The targeted drawer reopen test passes.
- The broad coverage expectation is current.
- Scoped Prettier passes for the four reported files.
- Rerun `npm run test` and record exact result, keeping documented pre-existing HomePage changes out of scope.

[ACCEPTANCE CRITERIA]
- [ ] Generic drawer reopen retains its manually selected Likes tab.
- [ ] Explicit PlayerBar action opens the requested drawer tab.
- [ ] `publicAuthShell.coverage.test.tsx` expects only approved business wording.
- [ ] Prettier and focused frontend tests pass.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 1:
- docs/policies/quality-gates.md

Tier 2:
- .agents/skills/react-best-practices/AGENTS.md
- docs/standards/frontend-standards.md

REQ/Context Docs:
- deliverables/user/REQ-20260823-ATS-001.md
- deliverables/agent/WI-20260823-ATS-001-evidence-pack.md
- deliverables/agent/WI-20260823-ATS-002-evidence-pack.md

Files:
- frontend/src/components/player/PlaylistDrawer.tsx
- frontend/src/components/player/playerComponents.test.tsx
- frontend/src/layouts/PlayerBar.tsx
- frontend/src/layouts/PlayerBar.test.tsx
- frontend/src/test/coverage/publicAuthShell.coverage.test.tsx
- frontend/src/pages/auth/SignupPage.test.tsx
- frontend/src/pages/auth/SocialCompleteProfilePage.test.tsx
- frontend/src/pages/public/TrackListPage.tsx
- frontend/src/pages/subscriber/ProfilePage.test.tsx

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260823-ATS-004-summary.md
Agent-facing -> deliverables/agent/WI-20260823-ATS-004-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260823-ATS-004-handoff.md

[TRACEABILITY REQUIREMENTS]
- Use apply_patch only.
- Do not use a broad formatter that touches unrelated files.
- Record the exact test and formatting results.

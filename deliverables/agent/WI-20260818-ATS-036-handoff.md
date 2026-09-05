[WI HEADER]
WI ID: WI-20260818-ATS-036
REQ: REQ-20260818-ATS-002
Agent: se
Depends On: -
Blocks: -

[WI SUMMARY]
Why: Align the public home hero and footer audience wording with the user's requested Korean term `창작자`.
Scope (in/out): Change only the two underlined hero strings and the HomePage footer audience string in the active React `HomePage`, then add/update focused assertions. Do not change the TrackDetail content-type wording, inactive HTML mockups, other routes, layout, styles, or APIs.
DoD: The rendered hero title is `창작자를 위한 최고의 음악`; the hero subtitle is `창작자를 위한 고품질 라이선스 음악.`; the footer reads `창작자를 위한 음악 라이선스 플랫폼`; focused tests and frontend static checks pass; unrelated dirty-worktree files remain untouched.
Constraints/Forbidden: Preserve all unrelated user files and untracked deliverables. Use `apply_patch`. Do not modify docs mockups, footer wording, backend, DB, services, or branches. Do not commit.

[ACCEPTANCE CRITERIA]
Functional:

- [ ] The hero title renders `창작자를 위한` before `최고의 음악`.
- [ ] The hero subtitle renders `창작자를 위한 고품질 라이선스 음악.`.
- [ ] The footer renders `창작자를 위한` before `음악 라이선스 플랫폼`.
- [ ] TrackDetail's distinct content-type phrase containing `쇼츠` remains unchanged.
      Performance:
- [ ] Text-only change adds no render work, request, dependency, or bundle behavior.
      Quality:
- [ ] Focused HomePage Vitest passes.
- [ ] TypeScript typecheck passes.
- [ ] ESLint passes.
- [ ] Changed files pass Prettier check.
- [ ] `git diff --check` passes.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):

- docs/standards/core-principles.md

Tier 0 (Standards - Based on Assignee):

- docs/standards/development-standards.md

Tier 1 (Policies - Inferred from REQ/WI content):

- docs/policies/quality-gates.md
- docs/standards/evidence-pack-standard.md

Tier 2 (Tech Stack - React):

- .agents/skills/react-best-practices/AGENTS.md
- docs/standards/frontend-standards.md

REQ/Context Docs:

- deliverables/user/REQ-20260818-ATS-002.md
- user-provided annotated main-screen screenshot

Files:

- frontend/src/pages/public/HomePage.tsx:291-306
- frontend/src/pages/public/HomePage.test.tsx

Repro/Logs:

- `rg -n "쇼츠|크리에이터" frontend/src/pages/public/HomePage.tsx frontend/src/pages/public/TrackDetailPage.tsx docs/ui/mockup/main.html`

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260818-ATS-036-summary.md :

- Korean summary of exact copy, scope, checks, risks, and rollback.
  Agent-facing -> deliverables/agent/WI-20260818-ATS-036-evidence-pack.md :
- Evidence pointers, exact commands/results, dirty-worktree isolation, and rollback.
  Handoff Packet -> deliverables/agent/WI-20260818-ATS-036-handoff.md :
- This packet.

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required
Tests: Focused HomePage test plus typecheck, ESLint, changed-file Prettier, and diff check.
Rollback: Restore only the two original hero strings, the original footer audience string, and their focused assertions.

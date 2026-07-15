[WI HEADER]
WI ID: WI-20260715-ATS-023
REQ: REQ-20260715-ATS-001
Agent: qa
Depends On: WI-20260715-ATS-022
Blocks: -

[WI SUMMARY]
Why: Freeze the verified full-listening checkpoint in an isolated demo branch/worktree and restore client access without changing the retained acceptance database or uploaded media.
Scope (in/out): In: explicit staging/commit of authorized files, stable branch/worktree creation, frontend dependency install if needed, owned acceptance lifecycle stop/start, local/public status and full-stream probes. Out: DB/schema/data mutation, provider/email/OAuth calls, product changes, runtime-log commits, broad audit remediation.
DoD: Authorized files are committed on the development branch; `codex/client-demo-stable` points to that exact commit in its own clean worktree; old owned preview processes stop; new worktree serves local and public SPA/API/full Track stream; runtime logs remain untracked; URL and evidence are recorded.
Constraints/Forbidden: Never stage `cloudflared*.log`, `frontend/vite*.log`, PID files, runtime roots, credentials, or ignored dependencies. Do not drop/migrate/reset DB or delete uploaded media. Do not force-update, reset, or modify unrelated branches/worktrees. You are not alone in the codebase; preserve unrelated changes.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Commit includes only authorized product/docs/tests and WI/REQ artifacts.
- [ ] Stable branch/worktree is clean at the verified commit.
- [ ] Existing acceptance database and uploaded Track remain available after restart.
- [ ] Local and public `/`, `/api/tracks`, and selected full Track stream return success.
- [ ] Stream no-Range `Content-Length` equals the stored full resource length and a valid Range returns `206` against that length.
- [ ] Anonymous direct static original path remains denied.
Quality:
- [ ] Lifecycle status reports one owned backend, frontend, and tunnel.
- [ ] No live Provider/email/OAuth or DB mutation is performed.
- [ ] Exact commit, branch, worktree, URL, status-only probes, and rollback/stop procedure are recorded.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md
- docs/standards/development-standards.md
Tier 1 (Policies):
- docs/policies/security-policy.md
- docs/policies/quality-gates.md
Tier 2 (Context):
- deliverables/user/REQ-20260715-ATS-001.md
- deliverables/agent/WI-20260715-ATS-022-evidence-pack.md
- deliverables/agent/WI-20260715-ATS-017-evidence-pack.md
- docs/SR/SR-42.md
- scripts/acceptance/start.ps1
- scripts/acceptance/status.ps1
- scripts/acceptance/stop.ps1
Runtime pointers:
- Current preview worktree: `C:/Users/jm991/Desktop/project/ATStudio-acceptance-preview`
- Existing owned runtime root: `C:/Users/jm991/AppData/Local/ATStudio/acceptance-preview-64db91c`
- Existing external environment bundle: `C:/Users/jm991/AppData/Local/ATStudio/acceptance-preview-64db91c/backend-environment-credentials.json`
- New stable worktree: `C:/Users/jm991/Desktop/project/ATStudio-client-demo-stable`

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260715-ATS-023-summary.md
Agent-facing -> deliverables/agent/WI-20260715-ATS-023-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260715-ATS-023-handoff.md

[TRACEABILITY REQUIREMENTS]
Evidence pointers: Required for staged paths, commit, branch/worktree, process lifecycle, URL, and HTTP headers/statuses.
Tests: Reuse WI-022 for full gates; run only deployment/API smoke here.
Rollback: Record owned lifecycle stop and branch/worktree removal conceptually; do not execute DB cleanup.

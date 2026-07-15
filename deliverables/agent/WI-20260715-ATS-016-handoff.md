[WI HEADER]
WI ID: WI-20260715-ATS-016
REQ: REQ-20260714-ATS-001
Agent: qa-integ
Depends On: WI-20260715-ATS-015
Blocks: WI-20260715-ATS-017 remaining-finding reassessment

[WI SUMMARY]
Why: Refresh the isolated client-acceptance preview from frozen checkpoint `b217234` to the fully verified development result `64db91c`, then prove local/public availability before the remaining audit continues.
Scope (in): Capture pre-state; stop only lifecycle-owned acceptance services; fast-forward `codex/acceptance-preview` to `codex/p1-acceptance-hardening@64db91c`; restart through the repository acceptance lifecycle with the existing repo-external backend environment; verify branch/worktree/runtime/local/public smoke and protected attachment denial; create WI outputs.
Scope (out): Development-branch mutation, merge commit, force/reset, product/docs/schema edits, database cleanup or migration, live Toss/payment/refund/email/OAuth mutation, client test-data mutation, credential content disclosure, tunnel provider changes, and push.
DoD: Preview branch is clean at exact commit `64db91c`; lifecycle is ready/running; local and public SPA, public track API, admin shell, and protected attachment boundary pass; temporary URL and limitations are recorded; no secret/data body enters evidence.
Constraints/Forbidden: Use only `--ff-only` branch update. Do not force checkout/reset or delete files. Use `scripts/acceptance/stop.ps1`, `start.ps1`, and `status.ps1` from the acceptance worktree with `%LOCALAPPDATA%\ATStudio\acceptance-preview` and its existing `backend-environment-credentials.json`. Never print, hash, copy, or inspect credential values, JDBC URL, database name, tokens, keys, passwords, payment bodies, or response bodies. Preserve the development worktree's four untracked runtime logs. You are not alone in the repository; do not revert concurrent changes.

[ACCEPTANCE CRITERIA]
- [ ] Record development branch/HEAD and clean tracked state; expected untracked runtime logs are unchanged.
- [ ] Record preview branch `codex/acceptance-preview`, clean pre-state, and old commit `b217234`.
- [ ] Stop only acceptance lifecycle-owned services; verify owned local listeners are released before update.
- [ ] Fast-forward preview with `git merge --ff-only codex/p1-acceptance-hardening`; resulting commit is exactly `64db91c` and worktree is clean.
- [ ] Restart with existing repo-external backend environment and installed cloudflared through `scripts/acceptance/start.ps1`; do not reveal environment values.
- [ ] `scripts/acceptance/status.ps1` reports ready/running and owned tunnel/backend/frontend services.
- [ ] Local `/`, `/api/tracks`, `/admin/dashboard` return HTTP 200.
- [ ] Public `/`, `/api/tracks`, `/admin/dashboard` return HTTP 200 through the active temporary URL.
- [ ] A direct protected Question attachment probe returns HTTP 401 or 403 locally and publicly; do not read the body.
- [ ] No data mutation, provider call, schema change, credential disclosure, or tracked Git change occurs.
- [ ] `validate-docs` and `git diff --check` pass for WI outputs.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 1:
- docs/policies/security-policy.md
- docs/policies/quality-gates.md

REQ/Context:
- deliverables/user/REQ-20260714-ATS-001.md
- deliverables/agent/WI-20260714-ATS-043-evidence-pack.md
- deliverables/agent/WI-20260715-ATS-014-evidence-pack.md
- deliverables/agent/WI-20260715-ATS-015-evidence-pack.md
- docs/SR/SR-42.md
- docs/client/testing-guide.md

Runtime/Files:
- C:\Users\jm991\Desktop\project\ATStudio-acceptance-preview
- C:\Users\jm991\AppData\Local\ATStudio\acceptance-preview
- scripts/acceptance/start.ps1
- scripts/acceptance/stop.ps1
- scripts/acceptance/status.ps1

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260715-ATS-016-summary.md
Agent-facing -> deliverables/agent/WI-20260715-ATS-016-evidence-pack.md
Handoff -> deliverables/agent/WI-20260715-ATS-016-handoff.md

[TRACEABILITY REQUIREMENTS]
Record exact pre/post commits, lifecycle commands and exit codes, sanitized runtime state, local/public status codes, active public URL, protected-boundary result, worktree states, intentionally unverified operations, rollback to the old commit by a separately approved branch update, and shutdown command. Never record secret/data values or response bodies.

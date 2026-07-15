[WI HEADER]
WI ID: WI-20260714-ATS-043
REQ: REQ-20260714-ATS-001
Agent: qa-integ
Depends On: WI-20260714-ATS-042, checkpoint b217234
Blocks: WI-20260714-ATS-044

[WI SUMMARY]
Why: Start and prove a frozen client acceptance preview from the dedicated worktree while leaving the development worktree free for continued remediation.
Scope (in):
- Verify `C:/Users/jm991/Desktop/project/ATStudio-acceptance-preview` is on `codex/acceptance-preview` at exact commit `b217234` and clean.
- Use `C:/Users/jm991/Desktop/project/ATStudio` only as the approved source of the ignored `application-local.yml` and disposable-MySQL runner; never modify or print the local file.
- Create one new disposable MySQL database with the approved `ats_wi021_YYYYMMDD_xxxxxxxx` naming contract and keep it for the running preview.
- Create an ACL-restricted backend environment JSON under a repo-external `%LOCALAPPDATA%/ATStudio/acceptance-preview` runtime root. Parse local YAML structurally; never copy the YAML wholesale.
- Put only allowlisted variables in the JSON. Replace the JDBC database path with the disposable database. Force QA bootstrap true and use a newly generated strong temporary bootstrap password.
- Set public/private storage paths to separate repo-external directories under the preview runtime root.
- If Toss is enabled, require configured TEST client/secret key forms; refuse live-looking credentials. No provider mutation is allowed during smoke.
- Install frontend dependencies in the preview worktree if absent, then start its acceptance lifecycle with the external JSON.
- Smoke local/public SPA and `/api`, Host/static-file denial, ADMIN login/stats/logout-refresh-replay, active subscriber login/subscription/playlist authorization, plan list, callback SPA shells, and Question/company/audio static denials. Do not call Toss charge/refund/billing issue/delete or send email.
- Leave tunnel/backend/frontend and disposable DB running after PASS. Verify manifest ownership and ports.
- Write redacted WI-043 summary/evidence in the development worktree. Return the public URL to MA only; store temporary account password only in the ACL-restricted runtime credential file, never in repo evidence or logs.
Scope (out):
- No production/retained DB, live Toss, real payment, SMTP, OAuth provider exchange, data import, branch change, commit/push, product edit, or teardown after PASS.
DoD:
- Frozen preview is externally reachable and core ADMIN/active-subscriber paths pass.
- Public origin exposes only Vite, with same-origin `/api` and `/uploads` proxying.
- Preview remains running and the development worktree remains on `codex/p1-acceptance-hardening`.
Constraints/Forbidden:
- Never print or persist raw secret values, JDBC URL, DB password, JWT, Toss key, bootstrap password, token, card/billing key, or exact DB name in repository artifacts.
- No payment/provider mutation. A client may later exercise only Toss TEST normal flow under the disclosed acceptance limitation.
- On any startup/smoke failure, stop owned processes and drop only the newly created disposable DB; preserve failure logs repo-externally and report BLOCK.

[ACCEPTANCE CRITERIA]
- [ ] Preview worktree/branch/commit is exact and clean before runtime outputs.
- [ ] Disposable schema and Hibernate validate pass; canonical plans and QA subscriptions are created.
- [ ] Secret file is outside repositories, ACL-restricted, allowlisted, and not inherited by tunnel/frontend.
- [ ] Local and public SPA/API return success.
- [ ] ADMIN and active subscriber role paths pass; subscriber APIs no longer return the prior no-plan 403.
- [ ] Protected static source paths deny anonymous and authenticated direct access as designed.
- [ ] Logout invalidates refresh replay.
- [ ] No provider/email/live mutation occurs.
- [ ] Services remain owned/running after PASS.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
Tier 1:
- docs/policies/security-policy.md
- docs/policies/quality-gates.md
Context in development worktree:
- deliverables/user/REQ-20260714-ATS-001.md
- deliverables/agent/WI-20260714-ATS-021/run-disposable-mysql-rehearsal.ps1
- deliverables/agent/WI-20260714-ATS-022-evidence-pack.md
- deliverables/agent/WI-20260714-ATS-038-evidence-pack.md
- deliverables/agent/WI-20260714-ATS-040-evidence-pack.md
- scripts/acceptance/start.ps1
- scripts/acceptance/status.ps1
- scripts/acceptance/stop.ps1
Runtime worktree:
- C:/Users/jm991/Desktop/project/ATStudio-acceptance-preview

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260714-ATS-043-summary.md
Agent-facing -> deliverables/agent/WI-20260714-ATS-043-evidence-pack.md
Runtime -> repo-external manifest/logs/secret/credential/database cleanup metadata only

[TRACEABILITY REQUIREMENTS]
- Repository evidence contains hashes/counts/status codes only for public host, DB, credentials, PIDs, and tokens.
- Record exact smoke categories and pass/fail without response bodies containing user data.
- Record rollback commands conceptually, not secret-bearing arguments.
- State current running/owned status at completion.

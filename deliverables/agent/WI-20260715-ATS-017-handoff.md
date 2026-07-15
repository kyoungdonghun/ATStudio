[WI HEADER]
WI ID: WI-20260715-ATS-017
REQ: REQ-20260714-ATS-001
Agent: qa-integ
Depends On: WI-20260715-ATS-016, checkpoint 64db91c
Blocks: -

[WI SUMMARY]
Why: Reopen the latest frozen client acceptance preview after WI-016 proved that the retained older disposable database no longer satisfies the current Hibernate schema contract.
Scope (in):
- Keep `C:/Users/jm991/Desktop/project/ATStudio-acceptance-preview` on clean `codex/acceptance-preview` at exact commit `64db91c`.
- Preserve the previous acceptance runtime root, its credential/cleanup metadata, and its retained disposable database unchanged.
- Create a separate repo-external runtime root for checkpoint `64db91c`.
- Create one newly generated disposable MySQL database with the previously approved constrained naming contract, apply the current fresh `src/main/resources/schema.sql`, and prove Hibernate `ddl-auto=validate` through the acceptance backend.
- Derive a new ACL-restricted backend environment JSON outside all repositories from the existing approved external environment source. Parse it without emitting values; keep only the established allowlist; replace only the JDBC database target; use acceptance-safe QA bootstrap and test-provider settings already established by the approved flow.
- Start backend, Vite frontend, and Cloudflare Quick Tunnel with `scripts/acceptance/start.ps1` from the acceptance worktree.
- Verify lifecycle ownership/readiness; local and public SPA root, track API, and admin SPA shell; and anonymous direct Question attachment denial using status/header-only probes.
- Leave the new runtime services and new disposable database running after PASS for the user's rough acceptance check.
- Write redacted completion deliverables in the development worktree.
Scope (out):
- No product/source/document remediation beyond WI handoff/evidence/summary.
- No full audit follow-up, deep authenticated acceptance journey, build suite, live Toss mutation, SMTP, OAuth exchange, data import, commit, push, or production/retained database migration.
- No deletion, drop, migration, or mutation of the previous acceptance database or its runtime metadata.
DoD:
- Latest checkpoint is reachable through a new temporary public URL and local origin.
- The backend validates and runs against a newly created disposable database based on the current canonical schema.
- Basic public/local smoke and protected attachment boundary pass.
- All secret-bearing artifacts remain outside Git with restricted ACLs; evidence is redacted.
Constraints/Forbidden:
- User explicitly approved creation and retention of one new disposable acceptance database for this server reopening only.
- Resolve and validate the absolute target and constrained generated database name before any CREATE/DROP action. Never target a configured retained/local/production database.
- Do not drop the new database after PASS; do not drop the previous database under any outcome.
- On failure, stop only processes owned by the new runtime root. Preserve redacted failure evidence and do not widen scope into product fixes.
- Never print, return, hash into repository evidence, or commit raw credentials, JDBC URLs, database names, JWTs, provider keys, temporary passwords, tokens, response bodies, card data, or billing keys.
- Expose only Vite port 5173 publicly; `/api` and `/uploads` remain same-origin Vite proxies to backend 8080.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Preview worktree is clean on `codex/acceptance-preview` at exact `64db91c` before startup.
- [ ] Previous runtime metadata and previous disposable database are preserved untouched.
- [ ] A separate repo-external runtime root and newly generated disposable database are used.
- [ ] Current `schema.sql` applies and acceptance backend reaches ready state with Hibernate validation.
- [ ] Lifecycle reports one owned backend, frontend, and tunnel process with ports 8080 and 5173 listening.
- [ ] Local SPA root, `/api/tracks`, and `/admin/dashboard` return HTTP 200.
- [ ] Public SPA root, `/api/tracks`, and `/admin/dashboard` return HTTP 200.
- [ ] Anonymous direct Question attachment access is denied locally and publicly with HTTP 401 or 403.
- [ ] New services and disposable database remain running after PASS.
Performance:
- [ ] Startup and smoke complete within the existing lifecycle timeout budget unless a bounded retry is documented.
Quality:
- [ ] No product file is modified.
- [ ] No secret or response body enters repository artifacts or command output captured in evidence.
- [ ] Development worktree's four pre-existing untracked runtime logs remain untouched.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md

Tier 0 (Standards - Based on Assignee):
- docs/standards/development-standards.md

Tier 1 (Policies - Inferred from REQ/WI content):
- docs/policies/security-policy.md
- docs/policies/quality-gates.md

REQ/Context Docs:
- deliverables/user/REQ-20260714-ATS-001.md
- deliverables/agent/WI-20260715-ATS-016-handoff.md
- deliverables/agent/WI-20260715-ATS-016-evidence-pack.md
- deliverables/agent/WI-20260714-ATS-043-handoff.md
- deliverables/agent/WI-20260714-ATS-043-evidence-pack.md
- deliverables/agent/WI-20260714-ATS-021-evidence-pack.md
- deliverables/agent/WI-20260715-ATS-007-evidence-pack.md

Files:
- C:/Users/jm991/Desktop/project/ATStudio-acceptance-preview/src/main/resources/schema.sql
- C:/Users/jm991/Desktop/project/ATStudio-acceptance-preview/scripts/acceptance/start.ps1
- C:/Users/jm991/Desktop/project/ATStudio-acceptance-preview/scripts/acceptance/status.ps1
- C:/Users/jm991/Desktop/project/ATStudio-acceptance-preview/scripts/acceptance/stop.ps1
- C:/Users/jm991/Desktop/project/ATStudio/deliverables/agent/WI-20260714-ATS-021/DisposableMysqlRehearsal.java
- C:/Users/jm991/Desktop/project/ATStudio/deliverables/agent/WI-20260714-ATS-021/run-disposable-mysql-rehearsal.ps1

Runtime pointers (contents are sensitive and must not be emitted):
- `%LOCALAPPDATA%/ATStudio/acceptance-preview`
- New checkpoint-specific runtime root selected by the assignee

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260715-ATS-017-summary.md:
- Korean summary with PASS/BLOCK, public URL if PASS, branch/commit, high-level smoke statuses, preservation statement, and residual temporary-tunnel limitation.
Agent-facing -> deliverables/agent/WI-20260715-ATS-017-evidence-pack.md:
- Redacted evidence pointers, exact command categories, lifecycle/HTTP statuses, secret/DB non-disclosure proof, old-runtime preservation, rollback/shutdown procedure, and running-state conclusion.
Handoff Packet -> deliverables/agent/WI-20260715-ATS-017-handoff.md:
- This packet.

[TRACEABILITY REQUIREMENTS]
- Evidence pointers (files/commands/statuses): Required.
- Tests: Record only bounded startup and header/status smoke; no response bodies.
- Rollback: Document how to stop only the new runtime and later drop only the new disposable database using external cleanup metadata.
- Historical continuity: Preserve WI-016 as the failed old-database startup attempt and identify WI-017 as the isolated fresh-database recovery.


> Historical public derivative, published in this working tree on 2026-09-09 by WI-20260909-ATS-018. Original dates, language, status, decisions and reported outcomes below are historical, not current approval or reverified recovery/production evidence. The original remains unchanged in private archive `v1-artifacts-20260909-084405`. See [A047: original identity, public SHA-256 and review](../../docs/registry/development-history-recovery-20260909.md#a047). Raw/local-only evidence pointers are lookup keys, not files supplied by a source checkout.

[WI HEADER]
WI ID: WI-20260817-ATS-021
REQ: REQ-20260817-ATS-010
Agent: qa-integ
Depends On: WI-20260817-ATS-019
Blocks: WI-20260817-ATS-023

[WI SUMMARY]
Why: Materialize a client-facing acceptance environment from the current release candidate without letting later operational rehearsal work alter it.
Scope (in): Create a dedicated client snapshot branch from exact commit `13fc37e8c74d3c17c8759ef7e65932b2db731f50`, create a separate worktree and repo-external runtime root, run documented acceptance preflights, stop only the explicitly approved existing Vite process `PID 24452` after re-checking its identity, and start the documented frontend/backend/Cloudflare acceptance lifecycle if all guards pass.
Scope (out): Do not delete any branch/worktree/file/data. Do not modify application source/configuration. Do not print secrets or environment bundle content. Do not execute Toss payment/refund mutations or send SMTP mail. Do not touch a database except normal application read/write activity required by the already approved acceptance runtime; abort if startup would create, drop, migrate, restore, or target a non-acceptance database.
DoD: A client acceptance runtime is either live with local/public frontend and API health evidence from the frozen snapshot, or it is stopped/cleanly rolled back with the precise blocker recorded.
Constraints/Forbidden: Use `scripts/acceptance/README.md` lifecycle only. The public URL is temporary. Never kill any process other than the re-verified Vite process on `127.0.0.1:5173` with PID 24452. Do not start a scheduler in more than one runtime.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Dedicated branch `codex/v1-client-acceptance-20260817` points to exact `13fc37e`.
- [ ] Dedicated source worktree is distinct from the current development worktree.
- [ ] Existing Vite process identity/port is re-verified before only that process is stopped.
- [ ] Acceptance preflight passes without exposing bundle values.
- [ ] Launcher reports local frontend, local API, public frontend, and public API health, or returns a documented no-change blocker.
Quality:
- [ ] Runtime manifest is outside repository and secret-free.
- [ ] `status.ps1` confirms owned process state after launch.
- [ ] Evidence states whether the acceptance database was merely used or any unexpected DB operation was requested.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 1:
- docs/policies/quality-gates.md
- docs/policies/security-policy.md

Tier 2:
- scripts/acceptance/README.md
- scripts/database/README.md
- docs/SR/SR-93.md

REQ/Context Docs:
- deliverables/user/REQ-20260817-ATS-010.md
- deliverables/agent/WI-20260817-ATS-019-evidence-pack.md
- deliverables/user/WI-20260817-ATS-019-summary.md
- src/main/resources/application-acceptance.yml

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260817-ATS-021-summary.md:
- Snapshot identifier, local/public reachability result, and only the public URL if live.
Agent-facing -> deliverables/agent/WI-20260817-ATS-021-evidence-pack.md:
- Redacted lifecycle commands/results, process ownership evidence, health status, no-secret/no-external-financial-action assertion, and stop/rollback instructions.

[TRACEABILITY REQUIREMENTS]
Evidence pointers: required.
Tests: documented preflight and lifecycle readiness outcomes.
Rollback: use only `scripts/acceptance/stop.ps1 -RuntimeRoot <exact runtime>` if the launcher started owned processes.

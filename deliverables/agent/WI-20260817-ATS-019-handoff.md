
> Historical public derivative, published in this working tree on 2026-09-09 by WI-20260909-ATS-018. Original dates, language, status, decisions and reported outcomes below are historical, not current approval or reverified recovery/production evidence. The original remains unchanged in private archive `v1-artifacts-20260909-084405`. See [A045: original identity, public SHA-256 and review](../../docs/registry/development-history-recovery-20260909.md#a045). Raw/local-only evidence pointers are lookup keys, not files supplied by a source checkout.

[WI HEADER]
WI ID: WI-20260817-ATS-019
REQ: REQ-20260817-ATS-010
Agent: qa-integ
Depends On: -
Blocks: WI-20260817-ATS-021, WI-20260817-ATS-022

[WI SUMMARY]
Why: Establish evidence for safely running client acceptance and operational rehearsal in parallel without deleting historical branches or disturbing existing processes.
Scope (in/out): Inspect current Git topology, worktrees, untracked scope, possible runtime manifests, local/public health endpoints, and environment separation requirements. Do not create/delete branches or worktrees; do not start, stop, restart, or reconfigure processes/tunnels; do not mutate databases; do not invoke SMTP/Toss/payment/refund operations; do not expose secrets.
DoD: Produce a disposition inventory with current branch/HEAD, branch unique-commit relationships, preservation candidates, runtime evidence, and a precise recommendation for separate client and rehearsal boundaries.
Constraints/Forbidden: Read-only investigation only. Treat public tunnel URLs as ephemeral. Never print credentials, JDBC URLs, environment bundles, raw rows, or provider data. Do not inspect excluded `output/` contents.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Current local/remote branch topology and worktree state are recorded.
- [ ] Potential branch deletion candidates are distinguished from branches with unique commits.
- [ ] Existing client runtime/public health is verified if a manifest or live endpoint exists; absence is reported rather than guessed.
- [ ] Required separation for code/runtime/DB/origin/scheduler/provider/mail is explicit.
Quality:
- [ ] No destructive Git, runtime, database, provider, or email action occurred.
- [ ] Evidence contains reproducible command classes and redacted result summaries.

[INPUT POINTERS]
Tier 0 (Constitution - Required):
- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 1 (Policies):
- docs/policies/quality-gates.md
- docs/policies/security-policy.md

Tier 2 (Context):
- docs/design/api-spec.md
- docs/guides/ (only directly relevant runtime/release guides)

REQ/Context Docs:
- deliverables/user/REQ-20260817-ATS-010.md
- AGENTS.md

Files:
- Repository Git refs/worktree metadata (read-only)
- Runtime manifests only if present outside the repository; never reveal secret values

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260817-ATS-019-summary.md:
- Concise branch/runtime inventory, candidate disposition categories, and next approval gates.
Agent-facing -> deliverables/agent/WI-20260817-ATS-019-evidence-pack.md:
- Commands/classes used, redacted evidence, current state, risks, and rollback/no-change assertion.
Handoff Packet -> deliverables/agent/WI-20260817-ATS-019-handoff.md:
- This packet.

[TRACEABILITY REQUIREMENTS]
Evidence pointers: required.
Tests: health checks only if non-mutating.
Rollback: no changes; state that explicitly.

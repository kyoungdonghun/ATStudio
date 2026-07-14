[WI HEADER]
WI ID: WI-20260714-ATS-017
REQ: REQ-20260714-ATS-001
Agent: se
Depends On: WI-20260714-ATS-003, WI-20260714-ATS-015, WI-20260714-ATS-016
Blocks: WI-20260714-ATS-022, WI-20260714-ATS-025, WI-20260714-ATS-027

[WI SUMMARY]
Why: Make the ephemeral Cloudflare acceptance environment repeatable, ownership-safe, and easy to verify/tear down.
Scope: Windows PowerShell start/status/stop scripts and operator-facing machine-readable runtime manifest outside Git; local/public readiness checks; no client sharing.
Out: Installing cloudflared, persistent tunnels/services, production deployment, live Toss payment, or creating tracked runtime logs/PIDs.
DoD: Launcher discovers one valid quick-tunnel URL, injects acceptance base/Host, starts services, reports readiness, and tears down only owned processes on failure/stop.
Constraints: Runtime state/logs must be outside repository; existing four untracked logs remain untouched. Bind backend/frontend/tunnel origin to loopback. Do not share URL with client.

[ACCEPTANCE CRITERIA]
- [ ] Start/status/stop are idempotent and verify PID/start-time/command ownership.
- [ ] Failure and Ctrl+C stop tunnel, Vite, then Spring and verify ports/public URL are closed.
- [ ] URL is emitted only after local frontend/API and public frontend/API checks pass.
- [ ] Scripts contain no secrets and use environment-variable names only.
- [ ] PSScriptAnalyzer if available or parser check, dry-run tests, and diff check pass.

[INPUT POINTERS]
Tier 0: docs/standards/core-principles.md; docs/standards/development-standards.md
Tier 1: docs/policies/security-policy.md
Context: deliverables/user/REQ-20260714-ATS-001.md; docs/design/p1-security-acceptance-hardening-design.md; docs/SR/SR-42.md; WI-015/WI-016 evidence packs
Files: new scripts under scripts/acceptance/ or established repo script path; no runtime files in repo

[OUTPUT CONTRACT]
User summary: deliverables/user/WI-20260714-ATS-017-summary.md (Korean)
Evidence Pack: deliverables/agent/WI-20260714-ATS-017-evidence-pack.md
Implementation ownership: acceptance PowerShell lifecycle scripts and focused dry-run/parser tests.

[TRACEABILITY REQUIREMENTS]
Evidence/commands/tests/rollback required; external URL sharing remains separate approval.

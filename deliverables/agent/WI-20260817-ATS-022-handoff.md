
> Historical public derivative, published in this working tree on 2026-09-09 by WI-20260909-ATS-018. Original dates, language, status, decisions and reported outcomes below are historical, not current approval or reverified recovery/production evidence. The original remains unchanged in private archive `v1-artifacts-20260909-084405`. See [A049: original identity, public SHA-256 and review](../../docs/registry/development-history-recovery-20260909.md#a049). Raw/local-only evidence pointers are lookup keys, not files supplied by a source checkout.

[WI HEADER]
WI ID: WI-20260817-ATS-022
REQ: REQ-20260817-ATS-010
Agent: se
Depends On: WI-20260817-ATS-021
Blocks: WI-20260817-ATS-023

[WI SUMMARY]
Why: Repair the acceptance lifecycle's failing child-process environment-isolation preflight so the documented client acceptance runtime can be started without weakening secret boundaries.
Scope (in): Diagnose and minimally correct only `scripts/acceptance/AcceptanceLifecycle.psm1` and/or `scripts/acceptance/test-backend-environment.ps1`, plus focused tests/evidence. Prove tunnel/frontend child processes receive none of the backend bundle variable names, backend receives exactly the synthetic bundle values, and the parent environment is restored.
Scope (out): No application feature/configuration changes, no acceptance launch, no actual bundle read, no provider/email/database action, no branch/worktree/file deletion, no secret output.
DoD: `test-backend-environment.ps1` passes with a test method that exercises the real child-environment boundary rather than relying on a scope-dependent false observation; existing dry-run test remains passing.
Constraints/Forbidden: Do not remove or weaken excluded-environment behavior. Do not hard-code current optional names in production logic. Do not hide the failure by skipping assertions. Temporary fixtures must use synthetic values and be removed.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Root cause is documented with source-level evidence.
- [ ] Tunnel and frontend are proven free of every backend-only variable name.
- [ ] Backend is proven to receive each supplied synthetic backend variable value.
- [ ] Parent process values are restored after every child spawn.
Quality:
- [ ] `scripts/acceptance/test-backend-environment.ps1` passes.
- [ ] `scripts/acceptance/test-dry-run.ps1` passes.
- [ ] `git diff --check` passes for the focused patch.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 1:
- docs/policies/quality-gates.md
- docs/policies/security-policy.md

Tier 2:
- scripts/acceptance/README.md

REQ/Context Docs:
- deliverables/user/REQ-20260817-ATS-010.md
- deliverables/agent/WI-20260817-ATS-021-evidence-pack.md
- src/main/resources/application-acceptance.yml

Files:
- scripts/acceptance/AcceptanceLifecycle.psm1
- scripts/acceptance/test-backend-environment.ps1
- scripts/acceptance/test-dry-run.ps1

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260817-ATS-022-summary.md:
- Root cause, security property preserved, and exact verification result.
Agent-facing -> deliverables/agent/WI-20260817-ATS-022-evidence-pack.md:
- Changed paths, synthetic/repro-only test method, command results, rollback, and no-secret assertion.

[TRACEABILITY REQUIREMENTS]
Evidence pointers: required.
Tests: both documented acceptance preflight scripts and diff check.
Rollback: revert only the focused acceptance script/test patch.

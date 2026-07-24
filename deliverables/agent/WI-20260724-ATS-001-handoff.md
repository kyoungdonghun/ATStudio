[WI HEADER]
WI ID: WI-20260724-ATS-001
REQ: REQ-20260724-ATS-001
Agent: se
Depends On: -
Blocks: WI-20260724-ATS-004, WI-20260724-ATS-006

[WI SUMMARY]
Why: Remove obsolete one-time payment environment aliases from the acceptance launcher contract.
Scope (in/out): Edit only acceptance lifecycle, its focused tests, and directly affected current-state documentation. Do not modify application payment behavior, DB, secrets, or external bundles.
DoD: `APP_PAYMENT_PROVIDER` and `TOSS_CONFIRM_URL` are rejected as non-allowlisted; valid recurring V2 variables still pass; focused tests pass.
Constraints/Forbidden: Never read or print external bundle values. Do not reintroduce provider switching or one-time payment compatibility.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Obsolete names are absent from the launcher allowlist.
- [ ] Focused tests prove obsolete-name rejection and current-name acceptance.
Quality:
- [ ] Acceptance PowerShell tests pass.
- [ ] `git diff --check` passes for the owned files.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
Tier 1:
- docs/policies/security-policy.md
REQ/Context:
- deliverables/user/REQ-20260724-ATS-001.md
- docs/SR/SR-93.md
- deliverables/user/WI-20260717-ATS-019-summary.md
Files:
- scripts/acceptance/AcceptanceLifecycle.psm1
- scripts/acceptance/test-backend-environment.ps1
- src/test/java/com/atstudio/atstudio/config/V1BackendBaselineContractTest.java

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260724-ATS-001-summary.md
Agent-facing -> deliverables/agent/WI-20260724-ATS-001-evidence-pack.md

[TRACEABILITY REQUIREMENTS]
List changed files and exact tests. Record rollback and whether current external bundles require operator regeneration.

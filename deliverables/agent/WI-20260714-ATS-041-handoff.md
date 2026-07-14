[WI HEADER]
WI ID: WI-20260714-ATS-041
REQ: REQ-20260714-ATS-001
Agent: qa-integ
Depends On: WI-20260714-ATS-040
Blocks: WI-20260714-ATS-042

[WI SUMMARY]
Why: Before creating the client-preview checkpoint, independently verify the external backend environment isolation contract and audit the exact Git scope for secret/runtime/generated artifacts.
Scope (in):
- Independently rerun `scripts/acceptance/test-dry-run.ps1` and `scripts/acceptance/test-backend-environment.ps1`.
- Review start/lifecycle code for tunnel-before-load, backend-only injection, restoration-before-frontend, fail-cleanup, path validation, and non-persistence.
- Scan intended changed/untracked repository files for likely raw secret/JDBC/token/card/billing-key values and exact public URLs using patterns, while avoiding printing matched values.
- Inventory checkpoint candidates and explicitly classify the four runtime logs and `frontend/tsconfig.tsbuildinfo` as excluded.
- Verify `git diff --check` and no staged changes.
- Produce user summary and Evidence Pack with a PASS/BLOCK verdict.
Scope (out):
- No file edit except WI-041 summary/evidence; no staging/commit/branch/worktree, server/tunnel, external DB, provider/email, or secret bundle creation/read.
DoD:
- Independent checks pass and checkpoint scope has no confirmed secret-bearing artifact, or exact blockers are documented.
Constraints/Forbidden:
- Do not open/read `application-local.yml` or any repo-external secret file.
- Never output matched secret values, URLs, credentials, DB names, tokens, PIDs, or card data.
- Preserve all shared worktree changes.

[ACCEPTANCE CRITERIA]
- [ ] Both PowerShell test suites pass independently.
- [ ] Lifecycle order and process-environment isolation are code-confirmed.
- [ ] Secret-pattern scan reports counts/paths only and zero confirmed raw-secret artifacts in intended scope.
- [ ] Runtime logs and `frontend/tsconfig.tsbuildinfo` are excluded from checkpoint.
- [ ] No staged changes exist and `git diff --check` passes.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
Tier 1:
- docs/policies/security-policy.md
- docs/policies/quality-gates.md
Context:
- deliverables/user/REQ-20260714-ATS-001.md
- deliverables/agent/WI-20260714-ATS-039-evidence-pack.md
- deliverables/agent/WI-20260714-ATS-040-evidence-pack.md
- scripts/acceptance/start.ps1
- scripts/acceptance/AcceptanceLifecycle.psm1
- scripts/acceptance/test-dry-run.ps1
- scripts/acceptance/test-backend-environment.ps1

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260714-ATS-041-summary.md
Agent-facing -> deliverables/agent/WI-20260714-ATS-041-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260714-ATS-041-handoff.md

[TRACEABILITY REQUIREMENTS]
- Record commands, result counts, changed-file classification, and verdict without sensitive values.
- Report candidate paths only when a pattern is confirmed safe or blocking; never copy the matched line.
- State no external state or Git mutation occurred.

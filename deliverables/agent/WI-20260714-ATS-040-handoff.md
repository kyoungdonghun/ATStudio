[WI HEADER]
WI ID: WI-20260714-ATS-040
REQ: REQ-20260714-ATS-001
Agent: se
Depends On: WI-20260714-ATS-039
Blocks: WI-20260714-ATS-041

[WI SUMMARY]
Why: The acceptance launcher currently relies on backend secrets already existing in its parent process. That lets the earlier-started Cloudflare process and later frontend process inherit unrelated DB/JWT/Toss/bootstrap secrets. The separated client preview must inject an external secret bundle only into the backend process.
Scope (in):
- Extend `scripts/acceptance/start.ps1` and `AcceptanceLifecycle.psm1` with a required repo-external backend environment JSON path for non-dry-run starts.
- Validate the path is a regular file outside the repository and its JSON is a flat object of allowlisted environment-variable names with nonblank string values.
- Require datasource URL/username/password, JWT secret, QA bootstrap enabled/password; accept explicitly allowlisted Toss/payment/OAuth/mail/storage values when present.
- Load secret values only after the tunnel process is started, merge them only into the backend child environment, and restore the launcher environment immediately after backend spawn.
- Ensure tunnel and frontend launch records/environment do not receive backend-only names or values.
- Never persist or print secret values, the secret file body, JDBC URL, bootstrap password, or tokens in manifest/status/dry-run/logs/errors.
- Update lifecycle dry-run tests and add focused tests for missing/in-repo/malformed/unknown/blank bundles and child-process isolation.
- Add a placeholder-only example JSON if useful; no real values.
- Produce WI-040 summary and Evidence Pack.
Scope (out):
- No real backend/tunnel/frontend start, DB operation, provider/email call, branch/worktree, staging, commit, product Java/TS change, or runtime-secret creation.
DoD:
- A non-dry-run start cannot proceed without a valid repo-external backend environment bundle.
- Backend receives required values; tunnel/frontend do not receive them.
- Existing lifecycle ownership, fail-cleanup, manifest redaction, and dry-run contracts remain passing.
- Focused PowerShell tests and scoped diff checks pass.
Constraints/Forbidden:
- Shared dirty worktree; do not revert other edits.
- Never echo fixture secret values in test output or repository evidence.
- Use only synthetic sentinel values in temporary directories outside the repo and remove them after tests.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Valid external flat JSON is accepted and merged only into backend environment.
- [ ] Missing, repo-internal, non-file, malformed, nested, unknown-key, and blank-value inputs fail closed.
- [ ] Required variables are enforced without exposing values.
- [ ] Tunnel starts before the secret bundle is loaded.
- [ ] Frontend starts after backend environment restoration.
Security:
- [ ] Manifest/status/dry-run/evidence contain no synthetic sentinel secret.
- [ ] Secret path/body/value is not forwarded to tunnel or frontend.
- [ ] Repository receives no real secret file.
Quality:
- [ ] `scripts/acceptance/test-dry-run.ps1` and new focused tests pass.
- [ ] Scoped `git diff --check` passes.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
Tier 1:
- docs/policies/security-policy.md
- docs/policies/quality-gates.md
Context:
- deliverables/user/REQ-20260714-ATS-001.md
- deliverables/agent/WI-20260714-ATS-015-evidence-pack.md
- deliverables/agent/WI-20260714-ATS-017-evidence-pack.md
- deliverables/agent/WI-20260714-ATS-022-evidence-pack.md
- deliverables/agent/WI-20260714-ATS-039-evidence-pack.md
Files:
- scripts/acceptance/start.ps1
- scripts/acceptance/AcceptanceLifecycle.psm1
- scripts/acceptance/test-dry-run.ps1
- src/main/resources/application-acceptance.yml
- src/main/java/com/atstudio/atstudio/config/AcceptanceStartupGuard.java

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260714-ATS-040-summary.md
Agent-facing -> deliverables/agent/WI-20260714-ATS-040-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260714-ATS-040-handoff.md

[TRACEABILITY REQUIREMENTS]
- Document the exact allowed/required variable names, load order, process isolation proof, test command/result, and rollback.
- Record only variable names/counts, never values.
- State that no live process, DB, provider, email, tunnel, staging, or commit occurred.

[WI HEADER]
WI ID: WI-20260817-ATS-033
REQ: REQ-20260817-ATS-010
Agent: qa-integ
Depends On: WI-20260817-ATS-022, WI-20260817-ATS-032
Blocks: WI-20260817-ATS-023, WI-20260817-ATS-024

[WI SUMMARY]
Why: Correct an acceptance-lifecycle status false-positive discovered during an interrupted client-runtime check. A stale manifest can report `ready` even when its frontend/backend are absent and a recorded PID has been reused by an unrelated process.
Scope (in/out): Inspect and correct only the acceptance status/health classification and its regression coverage. Do not start, stop, restart, or mutate any existing client, development, rehearsal, database, Cloudflare, SMTP, or payment environment. Do not disclose secrets.
DoD: A stale `ready` manifest cannot be reported as healthy; the status result distinguishes an active owned runtime from stale/degraded state using safe process identity and/or local/public HTTP readiness; regression tests cover the observed stale-manifest/PID-reuse case; user and evidence deliverables record the result.
Constraints/Forbidden: No external network side effect beyond existing local/public health GETs if tests require them. No source changes outside `scripts/acceptance/**`, directly related acceptance tests, and this WI's two deliverables. Do not touch the currently live client runtime on ports 5173/8080 or its runtime root. Do not run payment/refund/mail/backup/restore/Cloudflare actions.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] A manifest marked `ready` with no valid owned frontend/backend services returns a non-healthy state rather than `ready`.
- [ ] PID reuse by an unrelated process cannot produce a healthy runtime verdict.
- [ ] A valid owned runtime is not falsely marked stale solely because a public tunnel is temporarily unavailable; local process/HTTP evidence is represented separately and honestly.
- [ ] Existing lifecycle status tests and a regression test pass.
Quality:
- [ ] PowerShell scripts parse successfully.
- [ ] Relevant acceptance test scripts pass.
- [ ] `git diff --check` passes.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md

Tier 0 (Standards - Based on Assignee):
- docs/standards/development-standards.md

Tier 1 (Policies - Inferred from REQ/WI content):
- docs/policies/quality-gates.md
- docs/policies/security-policy.md

REQ/Context Docs:
- deliverables/user/REQ-20260817-ATS-010.md
- deliverables/user/WI-20260817-ATS-021-summary.md
- deliverables/user/WI-20260817-ATS-022-summary.md
- deliverables/user/WI-20260817-ATS-032-summary.md
- deliverables/agent/WI-20260817-ATS-032-evidence-pack.md

Files:
- scripts/acceptance/status.ps1
- scripts/acceptance/AcceptanceLifecycle.psm1:986-1027
- scripts/acceptance/README.md
- scripts/acceptance/test-*.ps1

Repro/Logs:
- A recorded manifest reported `state: ready` while ports 5173 and 8080 rejected connections and the public URL returned HTTP 502. The stored frontend PID was missing; the stored backend PID resolved to an unrelated Chrome process.

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260817-ATS-033-summary.md :
- Corrected behavior, tests, live-runtime non-interference statement, risks, and next approval gates.
Agent-facing -> deliverables/agent/WI-20260817-ATS-033-evidence-pack.md :
- Evidence pointers, patch notes, reproduction and test results, rollback and follow-up WI.
Handoff Packet -> deliverables/agent/WI-20260817-ATS-033-handoff.md :
- This packet.

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required
Tests: Include exact relevant PowerShell test commands and results
Rollback (if needed): Revert only the WI-033 commit/files; no runtime rollback is necessary because no runtime may be mutated

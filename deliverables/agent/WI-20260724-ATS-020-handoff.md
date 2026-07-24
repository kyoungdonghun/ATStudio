[WI HEADER]
WI ID: WI-20260724-ATS-020
REQ: REQ-20260724-ATS-002
Agent: qa
Depends On: WI-20260724-ATS-018, WI-20260724-ATS-019, WI-20260724-ATS-021, WI-20260724-ATS-022
Blocks: WI-20260724-ATS-014

[WI SUMMARY]
Why: Corrective changes are not accepted until a new clone from the pushed corrective branch proves the original failures are gone and the setup helpers are safe.
Scope (in/out): Independently review the corrective diff, recreate a clean remote clone, rerun frontend Prettier and all frontend gates, replay/verify the client PDF, exercise DB-bootstrap refusal cases and a disposable proof DB, and verify no unrelated product behavior changed. No runtime API/UI smoke or external Provider mutations.
DoD: Original WI-011/WI-012 failures reproduce as fixed from a remote fresh clone; DB bootstrap safety is proven; no generated or tracked drift remains.
Constraints/Forbidden: Do not reuse the earlier failed clone as the final proof. Do not access protected DBs, output secrets, commit changes, or waive a failing gate.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Fresh clone HEAD equals the pushed corrective branch HEAD.
- [ ] Frontend test, coverage, typecheck, ESLint, Prettier, and build all pass.
- [ ] Client-PDF replay and verification pass with clean PDF and manifest diffs.
- [ ] DB bootstrap rejects protected/malformed targets and proves create/schema/seed/validate/drop on one disposable DB.
- [ ] Corrective diff contains no product feature changes.
Performance:
- [ ] No material regression in existing build/replay times.
Quality:
- [ ] Backend focused/full gates required by changed files pass.
- [ ] Docs validation and `git diff --check` pass.
- [ ] Fresh clone tracked/staged diff is zero after verification.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md
- docs/standards/development-standards.md
Tier 1 (Policies - Inferred):
- docs/policies/security-policy.md
- docs/policies/quality-gates.md
Tier 2 (Task Context):
- docs/standards/evidence-pack-standard.md
REQ/Context Docs:
- deliverables/user/REQ-20260724-ATS-002.md
- deliverables/agent/WI-20260724-ATS-018-evidence-pack.md
- deliverables/agent/WI-20260724-ATS-019-evidence-pack.md
- deliverables/agent/WI-20260724-ATS-011-evidence-pack.md
- deliverables/agent/WI-20260724-ATS-012-evidence-pack.md
Files:
- .gitattributes
- scripts/docs/
- scripts/db/
- scripts/acceptance/

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260724-ATS-020-summary.md :
- Independent PASS/FAIL, original-defect disposition, residual risks
Agent-facing -> deliverables/agent/WI-20260724-ATS-020-evidence-pack.md :
- Clone/commit evidence, commands, test results, diffs, cleanup proof
Handoff Packet -> deliverables/agent/WI-20260724-ATS-020-handoff.md :
- This packet

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required
Tests: Record all commands, versions, counts, hashes, refusal cases, and clone status
Rollback: Verification-only; record corrective rollback pointer

[WI HEADER]
WI ID: WI-20260818-ATS-002
REQ: REQ-20260817-ATS-010
Agent: docops
Depends On: WI-20260817-ATS-034
Blocks: -

[WI SUMMARY]
Why: `future-policy-stubs.md` is a general system-policy draft whose backup wording can be misread as permission to treat Git history as an AT.M application database backup.
Scope (in/out): Clarify the scope boundary in `docs/policies/future-policy-stubs.md` and point AT.M runtime-data operations to SR-93. Do not claim that a production backup implementation now exists. Do not change source code, scripts, runtime, databases, secrets, external services, or historical findings.
DoD: The policy no longer creates an apparent contradiction with the V1 production-readiness gate; document validation and diff checks pass.
Constraints/Forbidden: Keep documentation in English. Preserve the draft/system-policy context. Do not alter the release verdict, downgrade external gates, or write credentials/identifiers.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] The policy explicitly excludes AT.M application runtime-data backup from the Git-snapshot statement.
- [ ] SR-93 is the explicit pointer for AT.M production backup/restore readiness.
- [ ] No claim is made that production backup/restore has been performed.
Quality:
- [ ] Documentation validator passes.
- [ ] `git diff --check` passes.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md

Tier 0 (Standards - Based on Assignee):
- docs/standards/documentation-standards.md
- docs/standards/glossary.md

Tier 1 (Policies - Inferred from REQ/WI content):
- docs/policies/quality-gates.md

REQ/Context Docs:
- deliverables/user/REQ-20260817-ATS-010.md
- docs/SR/SR-93.md
- docs/design/payment-operations-runbook.md

Files:
- docs/policies/future-policy-stubs.md

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260818-ATS-002-summary.md:
- Korean summary, exact wording boundary, validation result, and no runtime effect.
Agent-facing -> deliverables/agent/WI-20260818-ATS-002-evidence-pack.md:
- Changed document evidence, validation command/result, and rollback guidance.
Handoff Packet -> deliverables/agent/WI-20260818-ATS-002-handoff.md:
- This packet.

[TRACEABILITY REQUIREMENTS]
Evidence pointers: Required.
Tests: Include documentation validation and diff check.
Rollback: Revert only this WI's documentation and deliverables.

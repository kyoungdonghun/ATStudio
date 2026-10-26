---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: MA
category: handoff
status: active
dependencies:
  - path: ../user/REQ-20261011-ATS-001.md
    reason: Approved bounded preparation and read-only check
  - path: WI-20261011-ATS-007-handoff.md
    reason: Independent review dependency
---

# WI-20261011-ATS-008: Release Verification Integration

[WI HEADER]
WI ID: WI-20261011-ATS-008
REQ: REQ-20261011-ATS-001
Agent: docops
Depends On: WI006 / WI007 history, WI009 / WI010 historical review, MA native ENV_KEYS HOLD and cleanup receipts, user-approved WI011 correction / WI012 local re-review
Blocks: REQ closure only when bounded DoD handled

[WI SUMMARY]
Why: Keep Current Decision Entry accurate after the release probe, preserving historical evidence.
Scope: Update this REQ progress/DoD and docs/index.md Current Decision Entry; own summary/evidence.
DoD: Exact reviewed candidate, local vs native results, cleanup, remaining runtime/startup gates and no-publication boundary recorded with owner/date.
Constraints: No scripts/application/source/old evidence edits, remote/browser/network/DB/secret work, descendants, commit/push/reset/deletion or memory edits. Preserve unrelated dirty changes. Do not infer missing receipts.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Integrate WI006 artifact counts, exact coverage exclusions and new candidate identity.
- [ ] Integrate WI007 actual verdict and exact reviewed revisions; distinguish local synthetic/mapping proof from native.
- [ ] Attribute MA native pre/post data, errors and cleanup separately; no secret values, private config bodies, management addresses or console token.
- [ ] Hibernate PASS does not mean canonical fresh manifest recorded or full application runnable.
- [ ] Preserve TLS boundary, unresolved NS, monitoring/renewal and runtime activation gates.
- [ ] Include precise next step or blocker/approval, never erase earlier evidence.
Quality:
- [ ] Use validate-docs skill for scoped modified/new documents with fresh iterators.
- [ ] Direct whitespace checks include new untracked deliverables.
- [ ] Report known whole-validator false positive separately, no broad PASS.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/documentation-standards.md
- docs/standards/development-standards.md
- docs/standards/glossary.md
Tier 1:
- .claude/agents/docops.md
- docs/policies/security-policy.md
Tier 2:
- .agents/skills/create-wi-evidence-pack/SKILL.md
- .agents/skills/validate-docs/SKILL.md
- docs/index.md#current-decision-entry
- deliverables/user/REQ-20261011-ATS-001.md
- deliverables/agent/WI-20261011-ATS-006-evidence-pack.md
- deliverables/agent/WI-20261011-ATS-007-evidence-pack.md
- deliverables/agent/WI-20261011-ATS-005-evidence-pack.md
- deploy/single-vps/runbooks/release-readiness.md
- MA structured current-window receipts supplied at delegation

[OUTPUT CONTRACT]
Write only:
- docs/index.md (current checkpoint, preserve history/unrelated changes)
- deliverables/user/REQ-20261011-ATS-001.md (progress and accurate bounded status)
- deliverables/user/WI-20261011-ATS-008-summary.md
- deliverables/agent/WI-20261011-ATS-008-evidence-pack.md
Use create-wi-evidence-pack; English technical docs, Korean REQ. Include verification date/environment/owner, source HEAD, exact file/candidate pins, test results, rollback/follow-up and no-overclaim limits.

[TRACEABILITY REQUIREMENTS]
All four Tier0 texts injected before role/security/context. Generated using create-wi-handoff-packet from approved REQ and next available ATS sequence. No further WI blocked after this integration; MA verifies bounded DoD before REQ closure. If native check is HOLD, record actual reason and cleanup, never alter DB to force success.


---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: MA
category: reference
status: active
dependencies:
  - path: ../user/REQ-20261011-ATS-007.md
    reason: Approved bounded stages and latest autonomous repair instruction
---
# WI-20261011-ATS-061
[WI HEADER]
WI ID: WI-20261011-ATS-061
REQ: REQ-20261011-ATS-007
Agent: pg
Depends On: WI060 candidate
Blocks: MA native trial

[WI SUMMARY]
Why: Independent review of diagnostics and reload ordering, not a cosmetic PASS fix.
Scope (in/out): Write only build/WI061-review and own reports; inspect fresh WI060. Review exact admission/probe/baseline/reload timing and BPF identity preservation; independent fault/regression tests. No native/credentials/source changes.
DoD: Report concrete findings then exact final bundle/freeze-bound ALLOW or HOLD after fixes. Preserve prior review authority.
Constraints/Forbidden: No purchase, general public access, secret/PII output, destructive DB changes, commit/push. Preserve WI055 and all historical evidence. Native actions remain MA-only after independent exact-byte review. Existing secret use limited to previously approved purpose. Latest human authorization permits bounded fixes/reviews/new one-use trials without repeated minor approvals; major security/design/destructive/cost decisions require escalation.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Report concrete findings then exact final bundle/freeze-bound ALLOW or HOLD after fixes. Preserve prior review authority.
Performance:
- [ ] Existing bounded runtime/cleanup limits retained; no capacity claims from local tests.
Quality:
- [ ] Scoped tests and evidence honest, independent review required for runtime changes.
- [ ] Exact changed paths, hashes and unresolved limits reported.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/documentation-standards.md
- docs/standards/development-standards.md
- docs/standards/glossary.md
Tier 1:
- docs/policies/security-policy.md
- docs/policies/access-control-policy.md
- docs/policies/quality-gates.md
- docs/standards/evidence-pack-standard.md
Tier 2:
- .agents/skills/create-wi-evidence-pack/SKILL.md
- .agents/skills/test/SKILL.md
- .agents/skills/validate-docs/SKILL.md
- docs/templates/eval-report-template.md
- docs/templates/wi-subagent-handoff-template.md
- docs/index.md
- .agents/skills/react-best-practices/AGENTS.md (conditional only; no frontend changes)
- docs/standards/frontend-standards.md (conditional only)
Context:
- deliverables/user/REQ-20261011-ATS-007.md
- deliverables/agent/WI-20261011-ATS-058-evidence-pack.md
- deliverables/agent/WI-20261011-ATS-059-evidence-pack.md
Files:
- build/WI060-restricted-web/
- build/WI055-restricted-web/

[OUTPUT CONTRACT]
User-facing: deliverables/user/WI-20261011-ATS-061-summary.md
Agent-facing: deliverables/agent/WI-20261011-ATS-061-evidence-pack.md
Handoff: deliverables/agent/WI-20261011-ATS-061-handoff.md
Use create-wi-evidence-pack skill. No subdelegation. Shared workspace: preserve unrelated modifications.

[TRACEABILITY REQUIREMENTS]
Evidence pointers, test commands/results, identity pins, rollback boundaries and next WI trigger mandatory.
Rule source .claude/config/context-injection-rules.json; project ATS per workspace.json; skill-generated with Tier0-first minimal injection.


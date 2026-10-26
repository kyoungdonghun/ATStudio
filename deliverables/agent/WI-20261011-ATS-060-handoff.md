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
# WI-20261011-ATS-060
[WI HEADER]
WI ID: WI-20261011-ATS-060
REQ: REQ-20261011-ATS-007
Agent: se
Depends On: WI058 completed HOLD
Blocks: WI061

[WI SUMMARY]
Why: Resolve BPF identity HOLD without weakening security, enable useful native diagnosis.
Scope (in/out): Clone WI055 into fresh build/WI060-restricted-web; local code/tests/recipe only. Add phase-specific exact BPF snapshots and independent DB final comparison. Avoid daemon-reload after admitted BPF baseline by installing required overlays before baseline where safe; do not accept new IDs or tag-only equivalence. Preserve original all comparisons and cleanup. No app source changes yet.
DoD: Ready-to-review bounded trial with precise timing/delta diagnostics and fail-closed security, native sequence free of self-induced post-baseline global reload if justified; targeted plus inherited regression tests pass.
Constraints/Forbidden: No purchase, general public access, secret/PII output, destructive DB changes, commit/push. Preserve WI055 and all historical evidence. Native actions remain MA-only after independent exact-byte review. Existing secret use limited to previously approved purpose. Latest human authorization permits bounded fixes/reviews/new one-use trials without repeated minor approvals; major security/design/destructive/cost decisions require escalation.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Ready-to-review bounded trial with precise timing/delta diagnostics and fail-closed security, native sequence free of self-induced post-baseline global reload if justified; targeted plus inherited regression tests pass.
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
- build/WI055-restricted-web/
- build/WI036-web-contract/
- build/WI052-state-diagnostics/v2/

[OUTPUT CONTRACT]
User-facing: deliverables/user/WI-20261011-ATS-060-summary.md
Agent-facing: deliverables/agent/WI-20261011-ATS-060-evidence-pack.md
Handoff: deliverables/agent/WI-20261011-ATS-060-handoff.md
Use create-wi-evidence-pack skill. No subdelegation. Shared workspace: preserve unrelated modifications.

[TRACEABILITY REQUIREMENTS]
Evidence pointers, test commands/results, identity pins, rollback boundaries and next WI trigger mandatory.
Rule source .claude/config/context-injection-rules.json; project ATS per workspace.json; skill-generated with Tier0-first minimal injection.


[WI HEADER]
WI ID: WI-20260714-ATS-033
REQ: REQ-20260714-ATS-001
Agent: docops
Depends On: WI-20260714-ATS-026, WI-20260714-ATS-027, WI-20260714-ATS-031
Blocks: WI-20260714-ATS-034

[WI SUMMARY]
Why: Run the authoritative documentation integrity and count validation after all document changes are complete.
Scope: project docs validator, index/count synchronization checks, internal links, Tier references, traceability IDs, and documentation diff check.
Out: New documentation scope, code changes, or silently ignoring validator failures.
DoD: Documentation validation passes with exact command evidence and no stale counts/links.
Constraints: Fix only confirmed owned documentation defects. Preserve historical records and approved REQ content.

[ACCEPTANCE CRITERIA]
- [ ] Internal links and Tier references validate.
- [ ] REQ/WI/SR/ADR traceability IDs resolve.
- [ ] Documentation index/registry counts match current files/code where asserted.
- [ ] Documentation diff has no whitespace errors.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/documentation-standards.md
- docs/standards/glossary.md
Tier 1:
- docs/policies/quality-gates.md
Context:
- deliverables/agent/WI-20260714-ATS-026-evidence-pack.md
- deliverables/agent/WI-20260714-ATS-027-evidence-pack.md
- .agents/skills/validate-docs/SKILL.md
- .agents/skills/sync-docs-index/SKILL.md

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260714-ATS-033-summary.md (Korean)
Agent-facing -> deliverables/agent/WI-20260714-ATS-033-evidence-pack.md
Handoff -> deliverables/agent/WI-20260714-ATS-033-handoff.md

[TRACEABILITY REQUIREMENTS]
Commands, validator totals, corrected links/counts, exclusions, rollback, and residual risk are required.

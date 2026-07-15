[WI HEADER]
WI ID: WI-20260715-ATS-021
REQ: REQ-20260715-ATS-001
Agent: docops
Depends On: WI-20260715-ATS-018, WI-20260715-ATS-019
Blocks: WI-20260715-ATS-022

[WI SUMMARY]
Why: Align active documentation with the approved public full-track listening policy and separate listening from entitled downloads.
Scope (in/out): In: current API/use-case/design/glossary/client/audit-closure wording directly affected by bounded preview removal. Out: rewriting immutable historical REQ/WI evidence, unrelated audit findings, new preview/transcoding plans.
DoD: Active documentation says public full-track streaming through the controller; original keys/static URLs remain private; download requires active subscription and plan limit; prior bounded-preview design/closure is explicitly superseded rather than silently left current.
Constraints/Forbidden: Documents are English except the approved Korean REQ. Preserve historical evidence as history; add supersession notes where required. Do not change product code. You are not alone in the codebase; do not revert concurrent backend/frontend/audit work.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] API and Track use case describe full-length public listening and Range behavior.
- [ ] Security boundary clearly distinguishes listening from official download/license entitlement.
- [ ] Active docs no longer prescribe 30 seconds, 50 percent, 25 percent, bounded prefix, or a dedicated preview generator as current policy.
- [ ] Historical P0 design/closure identifies the new REQ as superseding only the listening-length decision while retaining original-path protection.
- [ ] Client-facing terminology uses listening/playback rather than implying a truncated preview.
Quality:
- [ ] `validate-docs` and `git diff --check` pass for affected docs.
- [ ] Repository-wide claim scan reports remaining historical-only occurrences explicitly.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md
- docs/standards/documentation-standards.md
- docs/standards/glossary.md
Tier 1 (Policies):
- docs/policies/security-policy.md
- docs/policies/quality-gates.md
Tier 2 (Context):
- deliverables/user/REQ-20260715-ATS-001.md
- deliverables/agent/WI-20260715-ATS-018-evidence-pack.md
- deliverables/agent/WI-20260715-ATS-019-evidence-pack.md
- docs/design/api-spec.md
- docs/design/usecase/sound-track.md
- docs/design/p0-release-blocker-remediation-design.md
- docs/audit/p0-release-blocker-closure-20260713.md
- docs/audit/full-system-audit-20260713.md
- docs/client/
Files:
- docs/design/api-spec.md
- docs/design/usecase/sound-track.md
- docs/design/p0-release-blocker-remediation-design.md
- docs/audit/p0-release-blocker-closure-20260713.md
- docs/standards/glossary.md
- docs/client/

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260715-ATS-021-summary.md
Agent-facing -> deliverables/agent/WI-20260715-ATS-021-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260715-ATS-021-handoff.md

[TRACEABILITY REQUIREMENTS]
Evidence pointers: Required for every current-state claim changed.
Tests: Record docs validation and exact claim-scan output summary.
Rollback: Document a docs-only rollback that preserves code and historical evidence.

---
version: 1.0
last_updated: 2026-10-25
project: ATS
owner: MA
category: handoff
status: active
---
# WI-20261025-ATS-004: Latest official provider preparation
[WI HEADER]
WI ID: WI-20261025-ATS-004
REQ: REQ-20261024-ATS-001
Agent: tr
Depends On: WI-20261024-ATS-024
Blocks: WI-20261025-ATS-006

[WI SUMMARY]
Why: Finish authorized stage-two preparation without repeated permission gates.
Scope: Verify official NCP SENS MAIL product/API/pricing and minimal account steps; Toss automatic billing enrollment and required operator materials. Reuse WI024. Supply exact verified API contract and corrected assumptions, no made-up fees. Produce actionable human handoff, not repeated generic plan.
DoD: Concrete reusable artifacts and source/official evidence; exact remaining boundary.
Constraints: No server/provider mutations, secret-body reads, paid calls, payment, new account, contract acceptance, DNS edits, commit/push. Local changes only in owned build/WI-20261025-ATS-004 folder and two deliverables. DOC may update REQ/index after review. Preserve prior artifacts and JAR. Report gaps instead of asserting activation.

[ACCEPTANCE CRITERIA]
- Reuse original six plans, recurring policy, IDs1/2 ADMIN and ID3 buyer, grant through Nov24.
- NCP absent and Toss contract unrequested already confirmed; ID1 mailbox chosen. Do not reask.
- Preserve Gmail and billing key ring. TEST keys are existing but not LIVE readiness.
- Document concrete account/contract/cost handoff; no unnecessary input forms.
- Focused local validation only; no production PASS from local evidence.

[INPUT POINTERS]
Tier 0: docs/standards/core-principles.md; docs/standards/development-standards.md; docs/standards/documentation-standards.md; docs/standards/glossary.md.
Tier 1: docs/policies/security-policy.md; AGENTS.md.
Context: docs/index.md Current Decision Entry; deliverables/user/REQ-20261024-ATS-001.md; deliverables/agent/WI-20261024-ATS-024-evidence-pack.md; build/WI20261024-024-provider-preparation/provider-preparation.md; deliverables/user/WI-20261025-ATS-003-summary.md.
Source: src/main/resources/application.yml; EmailService.java; scripts/deployment; docs/payment/index.md. Discover exact existing deployment paths with rg, do not invent.

[OUTPUT CONTRACT]
User-facing: deliverables/user/WI-20261025-ATS-004-summary.md
Agent-facing: deliverables/agent/WI-20261025-ATS-004-evidence-pack.md using create-wi-evidence-pack skill.
Owned artifacts: build/WI-20261025-ATS-004/

[TRACEABILITY REQUIREMENTS]
Record source lines, official URLs/date, exact local tests, changed paths and limitations. Preserve prior evidence. MA injects Tier0 excerpts: Korean communication, English docs/Korean REQ; smallest approved change, traceable WI, secrets environment-only, reuse canonical terms, do not claim unobserved results. Pointers are for focused task source reads, no full-repo sweep.

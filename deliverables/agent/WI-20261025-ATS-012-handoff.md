---
version: 1.0
last_updated: 2026-10-25
project: ATS
owner: MA
category: handoff
status: active
---
# WI-20261025-ATS-012 Explained client policy review PDF
[WI HEADER]
WI ID: WI-20261025-ATS-012
REQ: REQ-20261024-ATS-001
Agent: docops
Depends On: WI-20261025-ATS-011
Blocks: -

[WI SUMMARY]
Why: User found prior compressed comparison hard to understand and explicitly requested re-review and rewrite including concrete explanations.
Scope: New Korean client-facing PDF superseding WI011 as preferred handoff while preserving prior PDF. Reuse WI010 facts and WI011 renderer; make actual situations and ownership of decisions clear. No new policy decisions.
DoD: Readable explanation-first PDF, concrete examples, client decisions versus developer remediation and legal/provider verification separated; exact source facts maintained; all pages reviewed by author and MA.
Constraints: No source-code, original HWP, operational policy change, server, provider, publication, message, secret reads, cost, commit/push. No implying existing software is fully wrong or all gaps require client implementation. No invented policy options as legally approved. Avoid private business values.

[ACCEPTANCE CRITERIA]
Functional:
- Explain member type (individual/business) separately from six existing plans, versus rights-oriented Basic/Pro/Business/Custom; no automatic mapping. Preserve six existing prices/limits.
- Explain Toss developed/TEST-verified versus KG Inicis wording: typo needs wording correction; actual vendor change needs approved integration/refund/recurring revalidation. LIVE/contract still pending.
- Explain cancellation with 1-year/3-month example: future renewal stops but paid 9 months remain. Refund is separate and full versus partial entitlement handling may differ. Existing refund and access correction are separate operations. No statement that all refunds automatically end access.
- Explain annual refund monthlyprice99900 versus annual999000/12=83250, both arithmetic examples, existing formula not approved, not legal endorsement. No new refund policy selection.
- Explain already-published content example and minimum evidence after withdrawal, not mandatory collection of every video. Existing certificate info vs richer promised fields. CID internal management vs actual external action; three business days means check and request/release, NOT external completion guarantee.
- Explain actual collected fields, missing disclosures, soft deletion versus loss of license/download evidence, undefined retention/providers, age/marketing processes and browser storage in familiar language. Mark changes requiring developer work and those needing operator/legal/provider confirmation separately.
- Preserve supplied business information as reusable pending accuracy check, no re-entry request.
- Clear ending answer sheet with 4 core groups and additional questions only when needed. Explain client need not know technical solutions. No default selected choices.
Quality: PDF skill marker before authoring. Reuse bundled runtime+embedded Korean fonts+Poppler; actual allpage render/inspect. Narrative paragraphs and simple examples rather than dense tables. About 6-7 pages acceptable; no arbitrary squeezing. Validate current output, preserve prior artifacts.
Performance: Focused source reuse, no broad retesting.

[INPUT POINTERS]
Tier0: docs/standards/core-principles.md; docs/standards/development-standards.md; docs/standards/documentation-standards.md; docs/standards/glossary.md.
Tier1: AGENTS.md.
Context: deliverables/user/REQ-20261024-ATS-001.md; deliverables/user/WI-20261025-ATS-010-summary.md; deliverables/agent/WI-20261025-ATS-010-evidence-pack.md; build/WI-20261025-ATS-011/create_policy_pdf.py.
Original bodies are available locally under build/WI-20261025-ATS-010/ for focused fact checks only.

[OUTPUT CONTRACT]
Final: deliverables/user/client/ATStudio_policy_decision_review_explained.pdf.
Build/render: build/WI-20261025-ATS-012/.
User summary: deliverables/user/WI-20261025-ATS-012-summary.md.
Evidence: deliverables/agent/WI-20261025-ATS-012-evidence-pack.md via create-wi-evidence-pack skill.
REQ: add approval/assignment/completion pointers; overall stage2 stays open. Prior PDF preserved.

[TRACEABILITY REQUIREMENTS]
Record source/review corrections/outputhash/pagecount/render paths/visual and scoped checks. MA injects Tier0: Korean client document/English internal evidence, smallest approved change, scoped evidence-backed claims, preserve all prior artifacts, no runtime assertions from local review. Use concrete human examples, distinguish contract promise from software automation. No downstream WI.

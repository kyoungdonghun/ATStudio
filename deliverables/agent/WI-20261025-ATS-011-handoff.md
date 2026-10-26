---
version: 1.0
last_updated: 2026-10-25
project: ATS
owner: MA
category: handoff
status: active
---
# WI-20261025-ATS-011 Client policy decision PDF
[WI HEADER]
WI ID: WI-20261025-ATS-011
REQ: REQ-20261024-ATS-001
Agent: docops
Depends On: WI-20261025-ATS-010
Blocks: -

[WI SUMMARY]
Why: User approved turning the latest client-facing comparison and decision request into PDF.
Scope: Korean PDF explaining existing development versus supplied HWP policies and exact client decisions. Reuse WI010 evidence and latest chat comparison. Author local PDF only, plus traceable summary/evidence and REQ checkpoint.
DoD: Readable client-ready PDF, every page visually reviewed, official/payment scope and uncertainties preserved.
Constraints: No code/HWP/policy edits, server, provider, upload/send/publication, secrets, costs, commit or push. Avoid private business details and internal work identifiers in client PDF. Do not claim LIVE tests or contracts completed.

[ACCEPTANCE CRITERIA]
Functional: Cover 6-plan versus Basic/Pro/Business/Custom, user types, use rights, Custom, Toss versus KG Inicis (오기 versus actualmigration), cardonly, upgrade/downgrade, annualrefund arithmetic, ongoing content rights, certificate fields, CID3businessdays, privacy mismatches and four decision questions. Existing source facts unchanged. Include accessible response checklist/space and no invented decision defaults.
Quality: Read PDF skill; operation marker before authoring; embedded Korean fonts; readable table widths or prose alternatives, no cropped rows; actual allpage render and visual inspection. Reuse existing WI009 ReportLab/font/Poppler setup. No new legal research necessary; this is sourced comparison repackaging, not legal advice or new policy.
Performance: Keep client PDF concise, about 3-4 pages.

[INPUT POINTERS]
Tier0: docs/standards/core-principles.md; docs/standards/development-standards.md; docs/standards/documentation-standards.md; docs/standards/glossary.md.
Tier1: AGENTS.md.
Context: deliverables/user/REQ-20261024-ATS-001.md; deliverables/user/WI-20261025-ATS-010-summary.md; latest chat client-facing comparison.
Reusable: build/WI-20261025-ATS-009/create_client_docs.py and renderer commands in its evidence pack.

[OUTPUT CONTRACT]
Final: deliverables/user/client/ATStudio_policy_decision_review.pdf.
Build/render: build/WI-20261025-ATS-011/.
User summary: deliverables/user/WI-20261025-ATS-011-summary.md.
Evidence: deliverables/agent/WI-20261025-ATS-011-evidence-pack.md using create-wi-evidence-pack skill.
REQ: append explicit user PDF approval, assignment and completion, overall stage2 remains open.

[TRACEABILITY REQUIREMENTS]
Record source pointers, PDF hash/page count/render paths/visual results and scoped diffcheck. Rollback only own artifacts. No downstream WI. MA Tier0 injection: Korean client prose, English internal metadata, scoped evidence-led action, preserve originals and historical records, canonical names and factual uncertainty. File modification delegated to docops under approved scope.

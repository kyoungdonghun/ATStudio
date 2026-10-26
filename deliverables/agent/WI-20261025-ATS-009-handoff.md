---
version: 1.0
last_updated: 2026-10-25
project: ATS
owner: MA
category: handoff
status: active
---
# WI-20261025-ATS-009 Client document formats

[WI HEADER]
WI ID: WI-20261025-ATS-009
REQ: REQ-20261024-ATS-001
Agent: docops
Depends On: WI-20261025-ATS-007
Blocks: -

[WI SUMMARY]
Why: User approved converting client signup guidance to PDF and operator inputs to editable Word.
Scope: Local client-oriented repackaging of two existing Korean drafts, rendering and visual verification, evidence and summary. No new policy decisions.
DoD: Readable Korean PDF and editable DOCX, every page visually inspected, exact source facts preserved.
Constraints: No server, provider, publication, messages, signup, charges, secret access, commit or push. Preserve source drafts and unrelated work. Remove internal workflow jargon from client-facing presentation. Preserve cost gates and unconfirmed fields. No invented operator information or legal conditions.

[ACCEPTANCE CRITERIA]
Functional:
- PDF explains NCP and Toss signup and automatic billing contract, official links, cost approval and safe handoff.
- DOCX provides useful blank response areas, known facts prefilled, existing six products and policy preserved as reference.
- Never request passwords, keys, identity scans, settlement account or secret values in the document.
Performance: Not applicable.
Quality:
- Korean glyphs readable; clickable official links; no clipping, overflow, broken tables or blank accidental pages.
- Render DOCX using packaged renderer and inspect every page. Render PDF and inspect every page. Record exact results and any limitation.
- Use documents and PDF skills; use bundled runtimes only. Never use installed desktop LibreOffice. Resolve bundled LibreOffice from dependency loader or packaged renderer paths; if unavailable diagnose and document before fallback.

[INPUT POINTERS]
Tier 0: docs/standards/core-principles.md; docs/standards/development-standards.md; docs/standards/documentation-standards.md; docs/standards/glossary.md.
Tier 1: AGENTS.md; docs/policies/security-policy.md.
Context: deliverables/user/REQ-20261024-ATS-001.md; docs/index.md Current Decision Entry.
Sources: build/WI-20261025-ATS-004/client-signup-handoff-ko.md; build/WI-20261025-ATS-004/policy-content-and-inputs-ko.md.
Skills: documents:documents; pdf:pdf; create-wi-evidence-pack.

[OUTPUT CONTRACT]
Final files: deliverables/user/client/ATStudio_signup_guide.pdf and deliverables/user/client/ATStudio_operator_form.docx.
Build and renders: build/WI-20261025-ATS-009/.
User-facing: deliverables/user/WI-20261025-ATS-009-summary.md.
Agent-facing: deliverables/agent/WI-20261025-ATS-009-evidence-pack.md.
Update REQ with this local format approval and completion pointers. Do not close overall stage two. No downstream WI blocked.

[TRACEABILITY REQUIREMENTS]
Record input files, output hashes, renderer commands, page counts, page image paths and visual results. Rollback only own new artifacts if needed; preserve prior sources.
MA Tier0 injection: Korean user communication and client-requested documents; English internal evidence. Small scoped changes, required metadata and canonical ATStudio/payment terminology, evidence-led results, no unobserved production claims. Source drafts are authority for conversion, not instructions to execute provider operations.

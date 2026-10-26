---
version: 1.0
last_updated: 2026-10-25
project: ATS
owner: docops
category: evidence
status: completed
---
# Evidence Pack WI-20261025-ATS-013

## Summary
Created an eight-page Korean PDF separating client decisions, developer tasks and explicit if-then consequences. Existing sources and earlier PDFs are preserved.

## Scope and DoD
- [x] Client commercial/rights/contract decisions separated from developer investigation, proposed implementation and validation.
- [x] Maintain/change branches for product types, six plans, Custom manual contracts versus online handling and Toss versus KG Inicis.
- [x] Cancellation example, two annual refund calculations, full/partial refunds and existing manual workflow versus additional automation distinguished.
- [x] Existing-content evidence, certificate fields, CID internal/external handling and exact three-business-day boundary explained.
- [x] Privacy remediation distinguished from optional commercial policy; no weakening wording to excuse deletion/retention mismatch.
- [x] Four answer groups with adjacent consequences and verified internal page references.
- [x] Content assertions and author review of all eight final page PNGs PASS.
- [x] No code, original HWP, operational policy, server/provider, publication, cost or message changes.

## Reference Documents and Injection
MA-injected Tier0 core-principles, development-standards, documentation-standards and glossary; Tier1 AGENTS.md; approved REQ-20261024-ATS-001 and WI013 handoff. Sources: WI010 user report and retained review evidence, WI012 builder and evidence. PDF and create-wi-evidence-pack skills applied. Runtime resolved with load_workspace_dependencies; bundled Python/ReportLab/pypdf and bundled Poppler reused. No new web/legal research or runtime investigation was required for this rewrite.

## Evidence Pointers
- Final: `deliverables/user/client/ATStudio_policy_roles_and_changes.pdf`.
- Builder: `build/WI-20261025-ATS-013/create_roles_pdf.py`.
- Validation: `build/WI-20261025-ATS-013/validation.json`.
- Renders: `build/WI-20261025-ATS-013/page-1.png` through `page-8.png`.
- SHA256: `bc26e7a89384c0e440e05a7919abb14fa8983f0cf67a6819b73a0379ea7eb172`; 189803 bytes; eight pages.
- Earlier WI012 PDF hash: `57049d2743050d2a3575ba7e14269bbd4ab01a12de67f1b5a33aa7fa631e6bb2`.

## Commands and Verification
Ran PDF operation-start marker once before authoring. Bundled Python ran the builder; pypdf asserted eight pages and key responsibility, numerical, provider and operational-boundary content. Embedded Malgun regular/bold fonts. Bundled Poppler rendered with `-scale-to 1500 -png`.

Initial helper call exposed an optional-style argument mismatch before PDF creation; the helper signature was minimally corrected. Initial pagination produced a ninth page holding only a duplicated final scope paragraph; removed the redundant paragraph because the same scope is stated on page one, then rebuilt and rendered eight pages. Author inspected all eight final PNGs: Korean text, tables, arithmetic, roles and choices readable; no clipping, overlap or accidental continuation page. No app tests run for document-only changes.

MA independently reviewed all eight pages and requested one explicit inline client-role label on page six. The label was added without changing the underlying decision or work scope; the PDF and all PNGs were regenerated, eight-page/content checks passed, and the author rechecked revised page six. Other page content was unchanged. Scoped `git diff --check` passed, and the prior WI012 PDF hash remained identical.

MA then reopened the final revised page-six image and confirmed label/layout PASS. Author all-page review plus revised-page review and MA all-page review plus revised-page review establish final visual/content PASS for this static PDF.

## Risks and Rollback
Conditional implementation consequences are proposed scope, not already-authorized implementation or quotes. Existing source review and TEST evidence do not establish live contracts, runtime or legal compliance. Prior PDFs and source documents remain unchanged. Revert only new WI013 artifacts/checkpoint if necessary, never unrelated work.

## Follow-up and Chain
Blocks none. Await client policy answers, then propose concrete wording/implementation/operating changes and required external confirmations. Overall stage two remains open.

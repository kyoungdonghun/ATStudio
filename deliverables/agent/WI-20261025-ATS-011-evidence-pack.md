---
version: 1.0
last_updated: 2026-10-25
project: ATS
owner: docops
category: evidence
status: completed
---
# Evidence Pack WI-20261025-ATS-011

## Summary
Created a four-page Korean client decision PDF from WI010 review and the latest MA-provided four decision questions. No policy or implementation changes.

## Scope and DoD
- [x] Existing six products versus Basic/Pro/Business/Custom, user types, rights and Custom explained.
- [x] Toss TEST evidence versus KG Inicis wording and actual migration distinguished; no LIVE/contract completion claim.
- [x] Card-only recurring scope, upgrade/downgrade timing, two annual refund calculations preserved without selecting a policy.
- [x] Existing content rights, certificate fields, CID three-business-day promise and privacy mismatches summarized.
- [x] Four decision questions with response space and separate choices for launch features.
- [x] All final four page images reviewed by docops and independently by MA; PASS.
- [x] No business identifying values, server/provider activity, code/HWP edits, costs, sending or publication.

## References and Injection
Tier0: MA-injected core-principles, development-standards, documentation-standards, glossary. Tier1 AGENTS.md. Approved REQ-20261024-ATS-001 and WI011 handoff. Role docops, task client document conversion. Source: deliverables/user/WI-20261025-ATS-010-summary.md and its evidence pack; focused terms paragraphs P082-089 to confirm Custom context. MA supplied exact four question groups via agent message. Existing WI009 ReportLab/font/Poppler workflow reused. Skills pdf and create-wi-evidence-pack; PDF operation-start marker succeeded once before authoring.

## Artifacts
- Final: deliverables/user/client/ATStudio_policy_decision_review.pdf.
- Builder: build/WI-20261025-ATS-011/create_policy_pdf.py.
- Result: build/WI-20261025-ATS-011/validation.json.
- Renders: build/WI-20261025-ATS-011/page-1.png through page-4.png.
- SHA256: ed5928b0a254a65e18dc3e82372af915fa8f58d29f912d6d55d627506a423fba.
- Size: 136865 bytes. Pages: 4.

## Commands and Verification
Bundled Python dependencies/python/python.exe ran the builder with ReportLab and pypdf. Bundled Poppler native/poppler/Library/bin/pdftoppm.exe rendered final PDF using `-scale-to 1500 -png`. Embedded Malgun regular/bold TTF. Text/page assertions PASS for four pages and key provider, price, refund, storage and feature terms; no internal WI labels in PDF.
Initial Unicode minus did not display clearly in the refund table. Replaced with ASCII hyphen, regenerated and re-rendered; all final images inspected, arithmetic visible, no clipping, broken tables, unreadable Korean or accidental page breaks. MA independently inspected all final pages and confirmed PASS. This is a static printable/reply-by-number document, not an interactive AcroForm.

## Risks and Rollback
The comparison reuses dated local review and prior TEST evidence; it does not newly verify runtime, provider contract or legal compliance. Unanswered decisions remain unselected. Rollback only new WI011 artifacts; original HWP, code and earlier evidence preserved.

## Follow-up and Chain
Blocks none. After client answers, distinguish wording alignment from additional implementation/operating scope. Overall stage two remains open. No current approval to change the underlying policies is inferred from this PDF request.

---
version: 1.0
last_updated: 2026-10-25
project: ATS
owner: docops
category: evidence
status: completed
---
# Evidence Pack WI-20261025-ATS-012

## Summary
Created the seven-page explanation-first Korean client policy review PDF; recovered its final completion records after the user's PC shutdown without regenerating the artifact.

## Scope and DoD
- [x] Existing six products and member types versus the supplied Basic/Pro/Business/Custom framework explained without choosing a new policy.
- [x] Toss TEST evidence versus KG Inicis wording and actual vendor migration distinguished; LIVE and contract remain pending.
- [x] Cancellation, refund and access adjustment explained using one-year/three-month examples; full and partial refunds distinguished.
- [x] Both annual refund arithmetic examples shown without approving either formula or implying legal endorsement.
- [x] Existing-video rights, minimum evidence, richer certificate promises and the precise three-business-day CID response boundary explained.
- [x] Privacy fields, soft deletion, deleted rights records, retention, providers, age/marketing processes and browser storage explained with developer/operator/provider responsibilities.
- [x] Four client answer groups; no secret values or duplicate business-information request.
- [x] Author and MA independently reviewed all seven final page images; content and visual PASS.
- [x] Existing four-page PDF preserved; no code, HWP, actual policy, server, provider, publication or sending operation.

## Reference Documents and Injection
Role: docops. Tier 0 principles injected by MA: core-principles, development-standards, documentation-standards and glossary. Tier 1: AGENTS.md. Approved REQ-20261024-ATS-001 and WI012 handoff govern local PDF work only. Sources: WI010 user summary and evidence pack, WI011 builder and the user's requested plain-language explanations. Skill: create-wi-evidence-pack for this completion record; prior author reused the PDF workflow and bundled runtime.

## Evidence Pointers
- Final: `deliverables/user/client/ATStudio_policy_decision_review_explained.pdf`.
- Builder: `build/WI-20261025-ATS-012/create_explained_pdf.py`.
- Validation: `build/WI-20261025-ATS-012/validation.json`.
- Final renders: `build/WI-20261025-ATS-012/page-1.png` through `page-7.png`.
- PDF SHA256: `57049d2743050d2a3575ba7e14269bbd4ab01a12de67f1b5a33aa7fa631e6bb2`; 188444 bytes; seven pages.
- Preserved WI011 PDF SHA256: `ed5928b0a254a65e18dc3e82372af915fa8f58d29f912d6d55d627506a423fba`.
- User summary: `deliverables/user/WI-20261025-ATS-012-summary.md`.

## Commands and Verification
The original author generated and rendered the seven-page PDF using the existing ReportLab/Korean-font/Poppler workflow. Its validation records content checks PASS. The author reported full-page visual PASS, and MA then independently opened all seven final PNGs and confirmed readability, no clipping and factual explanation PASS before shutdown.

On resume, MA reopened the final PDF with bundled pypdf and observed seven readable pages; character counts were 1033, 1007, 1034, 1232, 1104, 1152 and 1087. MA and resumed docops independently ran `Get-FileHash ... -Algorithm SHA256`, matching both final and preserved PDF hashes above. Existing PNGs remained present. The generator's `visual_check: PENDING` placeholder was updated to record the already-completed author/MA reviews, not a newly claimed review. No PDF regeneration or new remote verification occurred.

Scoped `git diff --check` passed. JSON/record checks verified the matching PDF hash, PASS review status, seven PNGs, existing deliverables and no trailing whitespace in new reports: `RESUME_RECORDS_AND_PDF_INTEGRITY_PASS`. No application tests were needed for completion-record changes.

## Risks and Rollback
This document explains dated code/review evidence, not current production observation or legal approval. Original documents and policies remain unchanged. Rollback is limited to this WI's new documents and checkpoint; do not delete or overwrite prior PDFs or unrelated work.

## Follow-up and Chain
WI012 blocks none. The explanation-first PDF is the preferred client handoff; WI011 remains historical. Client decisions remain unselected. Stage two stays open pending the previously identified policy/provider decisions and authorized implementation.

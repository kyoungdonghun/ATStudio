---
version: 1.0
last_updated: 2026-10-10
project: ATS
owner: docops
category: evidence-pack
status: stable
dependencies:
  - path: WI-20261010-ATS-002-handoff.md
    reason: Approved WI execution contract
---
# Evidence Pack: WI-20261010-ATS-002

## Summary

Two-page Korean domain-purchase guide created from three actual privacy-cropped screenshots and fresh MA-supplied official facts. PDF structure, preservation, independent MA visual review and docops documentation checks PASS. WI complete.

## Scope / DoD Check

- [x] Approved REQ and fresh WI handoff confirmed before source authoring.
- [x] Exact `atmusic.co.kr` spelling and actual menu/public search screenshots.
- [x] Current fee and selectable public result dated 2026-10-10; estimate separated from final checkout, with recheck/no-reservation warning.
- [x] Owner/contact, period/VAT/final total, renewal/expiry and final payment boundary.
- [x] Authenticated post-prerequisite steps clearly separated from official-manual evidence.
- [x] Two A4 pages, Korean font families embedded, four canonical HTTPS links and valid Korean text.
- [x] All captures/renders visually inspected for PII and clipping; source JPEGs preserved.
- [x] Nine earlier guide artifacts hash-preserved, additive README only.
- [x] Independent MA verification for the exact final PDF hash: PASS.
- [x] Final repository documentation and whitespace checks.

## Reference Documents (Tier 0-2)

| Tier | Document | Reason |
|---|---|---|
| 0 | `docs/standards/core-principles.md` (STD-001) | Approval, privacy and traceability |
| 0 | `docs/standards/documentation-standards.md` (STD-004) | Metadata, links and English technical records |
| 0 | `docs/standards/development-standards.md` (STD-002) | Scoped generator and validation |
| 0 | `docs/standards/glossary.md` (STD-005) | AT.M client brand and ATStudio project identity |
| 1 | `docs/policies/security-policy.md` | Exclude account, payment and token information |
| 2 | `docs/index.md#current-decision-entry` | Distinguish dated evidence from live verification |
| 2 | `.agents/skills/create-wi-evidence-pack/SKILL.md` | Evidence Pack contract |
| 2 | `.agents/skills/validate-docs/SKILL.md` | Repository document checks |
| 2 | Bundled PDF skill `26.1007.11041` | Authoring marker, rendering and visual inspection |
| Task | `deliverables/user/REQ-20261010-ATS-002.md` | Explicitly approved domain-guide scope |
| Prior layout | `docs/client-guides/iwinv/build_guide.py` | Read-only visual/layout reference |

All required Tier 0 content was confirmed through current handoff pointers. Role: docops; task type: documentation. MA owns browser observation/capture and independent review; docops made no new delegation or browser actions.

## Evidence Pointers

- [PDF](../../docs/client-guides/iwinv/atmusic-domain-purchase-guide.pdf)
- [Generator](../../docs/client-guides/iwinv/build_domain_guide.py)
- [Additive README section](../../docs/client-guides/iwinv/README.md#domain-purchase-guide-2026-10-10)
- [Validation receipt](../../docs/client-guides/iwinv/qa/domain-validation.json)
- Final renders: [page 1](../../docs/client-guides/iwinv/qa/domain-guide-1.png), [page 2](../../docs/client-guides/iwinv/qa/domain-guide-2.png)
- [User summary](../user/WI-20261010-ATS-002-summary.md), [REQ](../user/REQ-20261010-ATS-002.md)

Docops changed paths: new `build_domain_guide.py`, `atmusic-domain-purchase-guide.pdf`, `qa/domain-guide-1.png`, `qa/domain-guide-2.png`, `qa/domain-validation.json`; additive `README.md`; this Evidence Pack, corresponding user summary and current REQ progress. MA separately saved three `screenshots/domain-*.jpg` files. Earlier guide source/PDF/QA and all unrelated tracked/untracked changes were preserved.

## Source and Capture Provenance

- Date: 2026-10-10 KST. Collector: MA, authenticated owner-controlled console and public iwinv pages in the in-app browser. Docops inspected only saved privacy-cropped files.
- `domain-01-register-menu.jpg`: 1300x380, top-left crop of current `/domain`; Register target `(330,116,102,40)`. Fresh path: main -> Domain -> Domain -> Register. Register then displayed the prerequisite notice requiring identity verification and card registration and led to `/member/join`. No sensitive form was submitted; downstream authenticated UI was inaccessible.
- `domain-02-public-search.jpg`: final 1300x350 crop, public input `atmusic`. MA replaced an earlier crop before final handoff; only the final hash is used. Full-hostname input had been rejected as invalid; `atmusic` succeeded. Highlight `(1115,203,159,69)` indicates Search.
- `domain-03-search-result.jpg`: 1300x530. Row highlight `(87,148,1201,68)` identifies `atmusic.co.kr`; the visible one-year price is KRW 17,000. Other results remain intact. Bottom count is zero with inactive application button: no selection/order was submitted.
- Raw cropped JPEG bytes are retained; all highlights are PDF-only vectors. No account IDs, names, email addresses, phone numbers, card values, passwords or query tokens appear in the saved screenshots. A query containing a security token was deliberately excluded from all stored URLs.
- [Official public price/search page](https://www.iwinv.kr/account/domain): MA reported current new-registration, renewal and transfer `.co.kr` fees as KRW 17,000/year excluding VAT. The guide covers the relevant new/renew fee and calculates KRW 18,700 including VAT for one prepaid year. It does not claim an account-specific checkout result or reservation.
- [Official registration guide](https://docs.iwinv.kr/service/domain/domain/domain_guide/domain_create/) version 2025.02.12: search, period, domain information, owner/contact, name servers, final application. The final application submits registration plus prepaid payment. These details are manual-derived only. Current console navigation differs from the manual's older Domain & DNS label, so the PDF uses the fresh observed menu.
- [Official renewal guide](https://docs.iwinv.kr/service/domain/domain/domain_guide/domain_period/): supporting renewal reference. Published automatic-card-renewal copy does not prove the current account has enabled it or has a working card. Actual renewal/expiry configuration remains uninspected.
- MA restored the browser to `https://console.iwinv.kr/main` and marked handoff. No external settings or transactions were changed.

## Commands & Outputs

- Source identity: existing dirty `main...origin/main`, HEAD `58b1011dad796017e47dbcc82a4225ca449bcdf6`.
- Bundled Node ran `mark_artifact_operation_started.mjs --operation-kind create --expected-output-count 1 --output-format pdf` successfully once immediately before first source authoring. No repeated marker.
- All handwritten source/document/receipt edits used `apply_patch`.
- Reproduction: [README command](../../docs/client-guides/iwinv/README.md#domain-purchase-guide-2026-10-10), bundled Python with `-B`, ReportLab, pypdf and Windows Malgun fonts; Poppler `pdftoppm.exe -r 144 -png`.
- The first PDF structural check rejected three font subsets because the inherited check expected two font objects. ReportLab legitimately split the regular Korean font into two subsets. The new validator checks the two expected embedded font families instead; rerun PASS. No historical generator was changed.
- Current PDF/renders were not regenerated after MA approval, preserving the independently reviewed hash.

## Test Evidence

| Check | Result |
|---|---|
| Generation and structural validation | PASS, exit 0 |
| Page format | Exactly 2 A4 pages |
| File size | 222,310 bytes |
| Fonts | Malgun Gothic regular and bold embedded, 3 valid subsets |
| Text | Required Korean/domain/price/prerequisite terms present, zero U+FFFD |
| Links | 4 canonical official HTTPS URLs, no query strings or tokens |
| Source screenshots | 3 JPEG hashes unchanged before/after generation |
| Existing artifacts | 9 SHA-256 baselines unchanged, including both earlier PDFs |
| Docops visual/privacy review | PASS, all 3 JPEGs and both final rendered pages inspected |
| MA independent review | PASS, both final PNGs inspected; pypdf independently checked A4/text/links; prior PDF hashes independently matched |
| MA findings | Clear text, no overlaps, correct domain/fee/input instructions, no PII; no purchase |
| Application tests | Not run; document-only scope, no application changes |
| Repository docs / whitespace | PASS: validator exit 0, 739 IDs, all Tier 0/link/index checks; `git diff --check` exit 0, pre-existing CRLF warnings only |

Final PDF SHA-256: `ecfc15a274d8d6d6b205cdc770745b473143d2c23474fb3694fd0e2a7413b9af`.

Preserved console PDF: `216eee0f6a6eff946e0b90e849b750fe84f16340f4104cde0d7475833e75f0a3`. Preserved earlier cost PDF: `33ceb77131d05f1abe8997e03b09ecee37cf3a4c1f8cc2f115abd45a9dd592fe`. Remaining hashes: validation receipt and generator `PRESERVED` mapping.

## Risks / Rollback

- Availability, pricing and UI are dated evidence and may change before purchase. No domain reservation, registration or account-specific renewal/checkout validation is implied.
- No authentication/card input, agreements, purchase, order, DNS/renewal/notification changes, application/runtime/DB work, external sending, commits or pushes.
- Rollback only the new domain-guide/WI artifacts and additive README block/current REQ progress, with explicit approval before deletion. Preserve all earlier guides and MA captures, and never reset unrelated worktree changes.

## Follow-ups

- WI Blocks: none. No downstream WI. Exact-hash MA visual approval and docops documentation checks passed; REQ/WI closed. MA may rerun the final validator after this status-only closeout.
- Actual domain registration/payment remains owner-performed future work. Server connection is separate later work.

## Related Documents

- [WI Handoff](WI-20261010-ATS-002-handoff.md)
- [Approved REQ](../user/REQ-20261010-ATS-002.md)
- [User summary](../user/WI-20261010-ATS-002-summary.md)

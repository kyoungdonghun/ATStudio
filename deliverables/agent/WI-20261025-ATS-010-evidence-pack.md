---
version: 1.0
last_updated: 2026-10-25
project: ATS
owner: docops
category: evidence
status: completed
---
# Evidence Pack WI-20261025-ATS-010

## Summary
Read both supplied HWP5 BodyText streams and compared the terms/privacy policy against current local code, existing payment policy and retained stage-two evidence. Produced a Korean clause-level review; originals, application code, server and public policy were not changed.

## Scope and DoD
- [x] Full BodyText, not preview-only extraction, both documents.
- [x] Terms product/payment/license and privacy collection/retention/deletion/provider/consent comparison.
- [x] Reusable principles, wording corrections, operator decisions and implementation/operating gaps separated.
- [x] Source versus deployed/configured/externally verified limits recorded.
- [x] Existing supplied operator facts acknowledged without repeating their identifying values in report.
- [x] No publication, server access, database/data mutation, external document upload, new dependency installation or embedded-script execution.

## Injected References
Tier 0 pointers and MA excerpts: core-principles, development-standards, documentation-standards, glossary. Tier 1: AGENTS.md and security-policy. Current authority: docs/index.md Current Decision Entry and approved REQ-20261024-ATS-001. Domain: docs/design/payment-refund-receipt-settlement-policy.md and WI004 draft. Assignment: WI010 handoff. Standards: scoped evidence-led work, canonical terms, Korean requested review, English internal evidence, preserve historical artifacts and report unverified boundaries.

## Artifacts
- `build/WI-20261025-ATS-010/extract_hwp.py`: minimal read-only CFB/FAT/miniFAT reader, HWP5 decompression and tag67 paragraph extraction. Uses Python standard library only.
- `build/WI-20261025-ATS-010/extraction.json`: original hashes, OLE stream inventory and record consumption.
- `build/WI-20261025-ATS-010/terms-body.txt`, `privacy-body.txt`: numbered UTF-8 body text kept in ignored build, not copied into tracked report in full. Contains supplied operator information; do not publish these scratch files.
- `deliverables/user/WI-20261025-ATS-010-summary.md`: full clause comparison, existing six products, exact annual example distinction, correction/decision list and official law pointers supplied by MA.
- `deliverables/user/REQ-20261024-ATS-001.md`: current local review approval/completion addendum. Overall stage two remains open.

## Extraction and Integrity
| Original | SHA256 | Section | Records | Text records | Nonempty paragraphs | Consumed and total decompressed bytes |
|---|---|---|---:|---:|---:|---:|
| AT.M terms HWP | 61e88cf33b76f68bb351402570919b0b89c699c9352b1f2542bbb695cb5f9d68 | BodyText/Section0 | 1264 | 303 | 302 | 68961 / 68961 |
| Privacy draft HWP | 4ae793c30091c19865e5cf9786c7a90bc340696c64e263274b3d65a782fe9faa | BodyText/Section0 | 1547 | 325 | 323 | 54952 / 54952 |

Both flags=1 (compressed; no password flag). All BodyText sections were enumerated; each document has one. Tag headers/bounds and CFB chain cycles are checked, and all bytes were consumed. Table cell text is included in record order. This is full body text review, not visual HWP page/layout verification; it does not validate layout or embedded image-only text. Stream inventory contains no BinData image/content streams. Scripts streams were inventoried but never executed/read as instructions. Original files were read only.

## Key Evidence
- HWP terms P084-089: Basic/Pro/Business/Custom versus canonical six existing plans (`AcceptanceSubscriptionPlanBootstrapRunner.java:31`). Existing values were retained on server in prior WI003, not re-queried here.
- Terms P172-204: payment methods/renewal; P215-219 annual refund example. 83,250 is 999,000/12; prior monthly business PREMIUM=99,900. Existing policy 1/4/5 separates cancellation, refund and entitlement correction.
- Terms P025/P282: richer certificate fields. `License.java:20`, `LicenseResponse.java:9` and `LicenseDetailPage.tsx:126` only current code/date/user/track presentation. No claim that license functionality is absent.
- Terms P055/P095/P253-257 and privacy P145/P163/P192-198: preservation/deletion promises conflict with `UserService.withdraw:155-214`, `User.withdraw:83-86`. Current withdrawal deletes licenses/downloads, soft-deletes user and clears refresh token; does not establish user PII erasure or playlist/social unlink deletion. Do not repair this by merely legitimizing unrestricted retention in wording.
- Privacy P065-073 omits required phone/profile inputs and certification evidence; `RegisterRequest:22-66`, `UserService.register:83-114`, `CompanyCertificationService:73-103,219-236` supply actual request/storage paths.
- Privacy P187/P190/P225 names KG Inicis; Toss implementation and source policy/current stage-two evidence disagree. NCP mail code exists but new JAR is not deployed; retained Gmail runtime evidence is current confirmed mail path, not newly inspected.
- Privacy P075-082/P256-264 social/profile claims: OAuth2Service65-108,293-337 handles id/email/name transiently and persists provider/id plus email/generated nickname; no profile image stored in this path. Enabling a provider is a separate effective-configuration question.
- Age assertions: terms P048/P184, privacy P267-272; RegisterRequest and SignupPage contain no age/BOD/guardian input. Do not infer legal compliance from missing fields or prescribe a specific identity provider.
- Marketing: UserService458-478 and immutable UserConsent capture affirmative consent/version/time; inspected controllers/service/UI did not establish revocation/suppression workflow. This is a verification gap, not proof of unlawful marketing.
- Privacy P303-306 versus authStore67-100/safeStorage12-34/playerStore: actual browser storage includes localStorage. No claim that no provider cookie exists.
- Whitelist request/admin CSV exists (`WhitelistChannelService:50`, `AdminWhitelistChannelService:148-223`). External CID provider and terms P145 three-business-day promise are not proven by internal records.
- Privacy P008-010 already supplies business facts. Report references presence without reproducing values; only currentness/public use confirmation needed, not new entry.

## Commands and Tests
Bundled Python executed `build/WI-20261025-ATS-010/extract_hwp.py` successfully. No pip install or new dependency. Parent independently read extracted text and material source paths. Focused Get-Content/rg inspections; no app/build/test run warranted for documentation-only review. HEAD observed `91a16e72693d7cd6ed04e0d36a5bd160af96f6d8`; worktree has existing unrelated changes and was not reset.

Official legal references were retrieved by MA, not by this child: Personal Information Protection Act Articles30,17 and28-8, ecommerce enforcement decree Article6. Report paraphrases narrow relevant requirements; no full legal-compliance determination. Country/transfer facts, consumer refund formula and actual contract remain pending.

Scoped `git diff --check` passed. Final original-file SHA256 values match extraction hashes. `git check-ignore` confirms both full extracted body files are ignored under build.

## Risks and Rollback
This is code/source review with retained operational evidence, not a fresh runtime privacy inventory. Current source cannot establish every actual log, external processor location, retention job or contract. HWP typography and page layout were not rendered. Do not publish report as final terms or privacy notice. Correct findings by superseding own report; preserve originals and prior evidence. Do not delete production data based on this review.

## Follow-up and Chain
No blocked downstream WI. Candidate work after user's direction: fact-preserving clause revisions, explicit licensing/refund decisions, scoped retention/withdrawal/certificate/consent design, operator/provider confirmation, then UI and publication approval. Overall stage two remains open.

---
version: 1.0
last_updated: 2026-10-25
project: ATS
owner: tr
category: evidence-pack
status: completed
---
# Evidence Pack: WI-20261025-ATS-004

## Summary

Completed public official provider research and a concrete account/contract handoff, reusing WI024. Provider activation remains pending; NCP MAIL pricing remains unverified.

## Scope / DoD Check

- [x] Exact current MAIL send/signature/result contract supplied to MA and WI008.
- [x] Toss automatic-billing contract and published standard pricing verified.
- [x] Existing account choices, sole-proprietor registration and private references reused without requesting duplicate inputs.
- [x] Missing provider price/identity/contract gates and minimal human steps distinguished.
- [x] No provider/server/private-body mutation or call; no actual cost incurred by task actions.

## Reference Documents (Tier 0-2)

Injected by MA via handoff: `docs/standards/core-principles.md`, `docs/standards/development-standards.md`, `docs/standards/documentation-standards.md`, `docs/standards/glossary.md`; Korean communication, English documents, minimum approved changes, secrets external, canonical terms and evidence honesty. Tier1: AGENTS.md approval boundaries and security policy pointers. Task context: approved REQ-20261024-ATS-001, WI024 evidence/preparation, WI003 completion and this WI handoff. Role: tr, official provider research. No claim of loading unrelated source trees.

## Evidence Pointers

- `build/WI-20261025-ATS-004/provider-readiness.md`: actual API, source URLs/check date, pricing limits and minimal handoff.
- `deliverables/user/WI-20261025-ATS-004-summary.md`: user-facing completion boundary.
- Reused `build/WI20261024-024-provider-preparation/provider-preparation.md`: existing protected-reference map and operating decisions.
- `src/main/java/com/atstudio/atstudio/service/EmailService.java:31`: inspected SMTP transport baseline before parallel WI008 changes.
- MA supplied public-DNS evidence pointer; this WI does not claim DNS ownership/readiness.

## Commands & Outputs

- Read handoff, evidence-pack skill and WI024 preparation with PowerShell Get-Content.
- `rg -n 'class EmailService|JavaMailSender|sendEmail' src/main/java/com/atstudio/atstudio/service/EmailService.java`: SMTP baseline.
- `git rev-parse HEAD`: `91a16e72693d7cd6ed04e0d36a5bd160af96f6d8`.
- Public web official-page retrieval: URLs/results recorded in provider-readiness; SENS product pricing timed out and static endpoint404. Unknown numeric MAIL price preserved as unknown.
- Focused local validation: file presence/nonempty and `git diff --no-index --check NUL <file>` for the three new artifacts; results reported to MA after execution.

## Tests

No app/runtime tests: documentation research only. No network send/signature integration test or production validity claim. Focused file checks only.

## Risks / Rollback

Public prices are not the actual merchant proposal. Missing NCP numerical price blocks paid activation, not local adapter preparation. Provider202 is not inbox delivery. Never retry an unknown send via SMTP. Remove/supersede only owned new artifacts to correct documentation; no remote state changed.

## Follow-ups

WI008 consumes exact API findings; WI006 independent review is the next blocked WI. MA should advance that chain after parallel operating/adapter work is ready. User's direct NCP signup and Toss billing contract are unavoidable; new costs and LIVE payment need exact approval. Stage2 is not fully activated.

## Superseding client-handoff decision

User subsequently deferred NCP signup and Toss enrollment to a combined client request after all pre-signup preparation. The earlier immediate signup handoff is withdrawn, not an unanswered blocker. `build/WI-20261025-ATS-004/client-signup-handoff-ko.md` is an unsent Korean user-requested client draft: registered sole proprietor, existing controlled email, actual automatic-billing contract, actual cost approval, provider-only identity documents, later private key input and nonsecret status return. Its internal checklist separates remaining public-policy/site review readiness from completed research. No outgoing message or enrollment was performed. The same local nonempty/whitespace checks cover this fourth artifact and updated deliverables.

Policy preparation supplement: `build/WI-20261025-ATS-004/policy-content-and-inputs-ko.md` supplies existing six-plan table, factual recurring/cancellation/refund copy from `docs/design/payment-refund-receipt-settlement-policy.md` Sections1,4,5, verified entity/provider data categories and a one-time operator input table. Unknown retention, processor/legal classification, domain ownership and policy decisions remain explicit; no new refund deadline, blanket nonrefund rule or license was invented. Code routes/publication and card review are not completed by the draft.

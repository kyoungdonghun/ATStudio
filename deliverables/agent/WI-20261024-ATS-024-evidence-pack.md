---
version: 1.0
last_updated: 2026-10-24
project: ATS
owner: TR
category: evidence-pack
status: completed
---
# Evidence Pack: WI-20261024-ATS-024

## Summary
Local provider and operating preparation completed without provider/server access or secret-body inspection; external readiness remains unverified.

## Scope / DoD Check
- [x] Existing evidence and exact environment reference reused before requesting human input.
- [x] Configuration, mail integration, contract/MID, cost, scheduler and rollback plan prepared.
- [x] Existing six plans/recurring policy preserved; optional webhook correctly classified.
- [x] Prepared-versus-applied and historical-versus-current boundaries recorded.

## Reference Documents (Tier 0-2)
MA injected Tier0 language, evidence-preservation, minimal-scope and approved-repair principles from core-principles/development-standards/documentation-standards/glossary. Full documents were not independently reread. Tier1 security constraints arrived through the approved handoff; no full security-policy read claimed. Tier2 actually inspected: current docs/index.md, approved REQ-20261024-ATS-001, WI024 handoff, WI019 evidence, WI058 evidence, WI20260908-011/012 evidence, payment feature inventory, acceptance README, application.yml, EmailService, bootstrap and scheduler configuration source. Evidence Pack skill read and applied; handoff existed before work. Workspace configuration confirms ATS tag.

## Evidence Pointers
- [Detailed preparation](../../build/WI20261024-024-provider-preparation/provider-preparation.md)
- [Nonsecret operating config proposal](../../build/WI20261024-024-provider-preparation/operating-config-proposal.json): valid existing environment names, false test/acceptance/background/notification flags, KST, symbolic existing references only; not an importable env bundle or applied state.
- [User summary](../user/WI-20261024-ATS-024-summary.md)
- `src/main/java/com/atstudio/atstudio/service/EmailService.java`: JavaMailSender, unchanged templates/token semantics.
- `src/main/resources/application.yml:119`: test bootstrap defaults false; mail/payment config names.
- `docs/payment/feature-inventory.md:150`: webhook planned, not implemented.
- `scripts/acceptance/README.md:124`: external bundle reference; never wholesale-import acceptance flags into production.
- `docs/index.md:1627`: historical NCP account absence, not current absence evidence.

## Commands & Outputs
- `git rev-parse HEAD` -> `91a16e72693d7cd6ed04e0d36a5bd160af96f6d8`; branch `main`.
- Bounded Get-Item check of documented environment reference -> exists=true, regular_file=true, reparse_point=false. No body read or value hashing.
- Focused rg/Get-Content source and public evidence reads; official web searches. First PowerShell glob arguments were rejected, then corrected to parent directory with rg -g. No consequential operation failed or was retried.
- Official sources and pricing uncertainty are recorded in the preparation plan. No numeric cost guarantee is made.

## Tests
`python build/WI20261024-024-provider-preparation/test_reference_presence.py` -> 4 tests PASS: no value/unknown-key leakage, mixed-mode detection, missing/invalid fields, bounded file/structure rejection. Only synthetic files read. No private bundle parsed by TR. No build, application or provider tests run. Owned-file whitespace and file hash checks recorded in `checks.json` after creation. No stage-one replay needed.

## User update
MA relayed explicit Oct24 user answers: NCP account absent, Toss merchant/billing contract not applied, existing ID1 mailbox selected for reply/operations alerts. The plan now contains exact human account/contract/fee boundaries and no repeated status/recipient question. These are user-confirmed facts, not live provider-console observation.

MA-owned later Oct24 presence check confirms MAIL_HOST/PORT/USERNAME/PASSWORD/FROM, both TOSS keys in TEST mode, and billing encryption ACTIVE_KEY_ID/0_ID/0_SECRET present. No values printed or provider call. TR did not repeat this check. Validity/MID/target/LIVE proof remains absent; these existing references should be reused without user re-entry.

## Risks / Rollback
Source defaults do not prove deployed config. Historical TEST does not establish LIVE contract. The provider's public pricing extraction did not expose a usable MAIL rate. Mail API acceptance is not final delivery. Enabled background jobs can mutate/call providers. Preserve current Gmail and billing key ring. Rollback affects only these newly owned documents, with unrelated work untouched; no operational rollback was required.

## Follow-ups
MA continues approved account/grant work independently. For provider preparation follow the detailed plan: presence/mode-only private-reference check, existing official provider sessions, price/contract/DNS metadata, then only necessary human inputs. No shell command should start the acceptance runtime or import its whole environment. WI024 Blocks:-; no automatic dependent WI exists. Provider implementation/activation remains NOT RUN, so completed WI024 means local research/preparation only.

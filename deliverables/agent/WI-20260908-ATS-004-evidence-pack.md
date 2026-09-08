---
version: 1.0
last_updated: 2026-09-08
project: ATS
owner: cr
category: evidence-pack
status: stable
dependencies:
  - path: WI-20260908-ATS-004-handoff.md
    reason: Approved independent review bounds and write ownership
  - path: ../user/REQ-20260908-ATS-001.md
    reason: Approved scope and WI005 dependency chain
  - path: WI-20260908-ATS-001-evidence-pack.md
    reason: Terminal renewal and grace evidence
  - path: WI-20260908-ATS-002-evidence-pack.md
    reason: Frontend implementation and focused verification
  - path: WI-20260908-ATS-003-evidence-pack.md
    reason: Monetary timestamp implementation and focused verification
---

# Evidence Pack: WI-20260908-ATS-004

> Purpose: Independently review the bounded WI001-003 product/test changes without executing financial operations or duplicating MA verification.

## Summary (one-liner)

No actionable findings in the reviewed snapshot. Independent static review is complete; MA can trigger WI005 documentation closeout while retaining separate aggregate-test, browser, runtime and production gates.

## Findings

| Severity | Open findings |
| --- | ---: |
| CRITICAL / P0-P1 | 0 |
| MAJOR / P2 | 0 |
| MINOR / P3 | 0 |

No product fix was requested or applied, and no fixed-delta review was necessary. This finding-free result applies only to the 16 files below and their relevant unchanged contracts. It is not a whole-product security audit or a production approval.

## Scope / DoD Check

- [x] Verified the main checkout and branch `codex/v1-release-rehearsal-fixes`; HEAD `2f2e9eccadd9ae9626fe8273bc635068d42b09b0`.
- [x] Reviewed all 15 current tracked WI001-003 product/test diff files plus the new BillingAgreementChargeTimestampIntegrationTest.java. Tracked diff: 816 insertions, 25 deletions. The new test adds 238 lines separately.
- [x] Callback query data supplies only a bounded reported hint, never financial authority or a mutation trigger.
- [x] Paid-upgrade confirmation binds to the current preview, source subscription, plan, cycle and request generation; existing mutation and UNKNOWN recovery guards remain.
- [x] Correction expiry requires explicit input, and target/effect details are present at request, approval and execution. Late/failed previews cannot restore a retired executable preview.
- [x] Registration and cleanup preserve previous or null charge history; positive SUBSCRIBE, UPGRADE and RENEWAL finalization retains charge recording and DONE replay exits.
- [x] Terminal-failure tests exercise actual H2 repositories and DownloadService, with explicit dates and fake provider boundaries rather than claimed live/timer proof.
- [x] Created only this Evidence Pack and the WI004 User Summary with apply_patch, following create-wi-evidence-pack.
- [x] No product/test/config/data edits, provider calls, server operations, other-worktree changes, agents, staging or commits.

## Reference Documents (Tier 0-2)

| Tier | Document | Use |
| --- | --- | --- |
| Entry | AGENTS.md; WI004 handoff; approved REQ | Role, approval, bounded ownership and chain |
| 0 | docs/standards/core-principles.md (STD-001) | MA-injected constitution and truthful evidence boundaries |
| 0 | docs/standards/documentation-standards.md (STD-004) | Injected context supplemented by exact local text after truncation |
| 0 | docs/standards/development-standards.md (STD-002) | Injected context supplemented by exact local testing-standard text |
| 0 | docs/standards/glossary.md (STD-005) | MA-injected canonical domain terminology |
| 1 | .claude/agents/cr.md | Independent evidence-based review and severity reporting |
| 1 | docs/policies/security-policy.md | Query trust, financial state, key/PII handling and local correction boundaries |
| 1 | docs/policies/quality-gates.md | Scoped verification, reproducibility and rollback |
| 2 | WI001-003 handoffs and Evidence Packs | Ownership, implemented behavior and previously executed checks |
| 2 | docs/payment/acceptance-test-checklist.md, sections 3-7, 10-13 | Registration, upgrade, renewal, correction and acceptance limits |
| 2 | Relevant unchanged API, entity, service, repository and test-support contracts listed below | Call-path and test-quality cross-checks |

Injection rule source: `.claude/config/context-injection-rules.json`; `cr` required tiers `[0, 1]` were confirmed. ATS tag was confirmed in `.claude/config/workspace.json`. Tier 0 was supplied by MA; truncated portions were consulted at the supplied pointers. No additional delegation was performed.

Skills: `.agents/skills/react-best-practices/SKILL.md`, its effect-dependency and event-handler-ref rules, then `.agents/skills/create-wi-evidence-pack/SKILL.md`. Object identity is intentionally part of the confirmation safety contract, so it was not treated as an automatic performance-refactoring request.

## Evidence Pointers

| Examined changed file | Review anchors and result |
| --- | --- |
| [SubscriptionPaymentPage.tsx](../../frontend/src/pages/subscriber/SubscriptionPaymentPage.tsx) | 89-92, 205-222, 264-272, 525-530, 720-723: exact single-code allowlist; fail callback performs status reads only; matching-purpose DONE suppresses hint even if canonical reload fails; no raw provider text interpolation |
| [SubscriptionPaymentPage.test.tsx](../../frontend/src/pages/subscriber/SubscriptionPaymentPage.test.tsx) | 918-995: READY/PROCESSING/FAILED/unreadable/no-order cases, malformed codes, no prepare/confirm/SDK calls or attempt-storage creation, safe navigation and DONE precedence |
| [SubscriptionManagePage.tsx](../../frontend/src/pages/subscriber/SubscriptionManagePage.tsx) | 580-585, 631-677, 734-791, 1254-1283, 1408-1452: preview retirement, exact confirmation snapshot, synchronous mutation lock, retained-period versus next-cycle presentation; zero-charge and canonical recovery routes retained |
| [SubscriptionManagePage.test.tsx](../../frontend/src/pages/subscriber/SubscriptionManagePage.test.tsx) | 1028-1153 and existing recovery tests updated with confirmPaidUpgrade: monthly/yearly retained period, cancel without write, one mutation after repeated clicks, context retirement and no-charge route |
| [SubscriptionManagePage.module.css](../../frontend/src/pages/subscriber/SubscriptionManagePage.module.css) | 404-416, 433-435: scoped footer wrapping and mobile stacking; no independent visual claim |
| [PaymentOperationsPage.tsx](../../frontend/src/pages/admin/PaymentOperationsPage.tsx) | 338, 651-655, 716-721, 1227-1281, 1289, 1383, 1495-1507, 2133, 2996-3012: explicit expiry, request-generation invalidation, preview clearing before retries, target/effect summaries; existing server approval/execution authority unchanged |
| [PaymentOperationsPage.test.tsx](../../frontend/src/pages/admin/PaymentOperationsPage.test.tsx) | 1261-1457: blank expiry rejection, edits retire preview/dialog, late and failed preview handling, target/effect confirmation, existing duplicate-confirm assertions retained |
| [BillingAgreement.java](../../src/main/java/com/atstudio/atstudio/entity/BillingAgreement.java) | 100-102, 119-158, 178-206: only two history resets removed; activate already resets all other state formerly shared with recordSuccessfulCharge |
| [PaymentCommandTransactionService.java](../../src/main/java/com/atstudio/atstudio/service/PaymentCommandTransactionService.java) | 532-560, 598-600, 640-663, 687-719, 748-765, 1142-1149: both SUBSCRIBE branches explicitly retain charge recording; UPGRADE/RENEWAL unchanged; registration activation no longer fabricates a charge timestamp |
| [schema.sql](../../src/main/resources/schema.sql) | 138: COMMENT-only correction to normal next-billing date; no type, column, key or constraint change, and no SQL execution |
| [BillingAgreementStateMachineTest.java](../../src/test/java/com/atstudio/atstudio/entity/BillingAgreementStateMachineTest.java) | 23-85: old/null history, repeated preparation and cleanup, activation and unchanged key/status semantics |
| [BillingAgreementChargeTimestampIntegrationTest.java](../../src/test/java/com/atstudio/atstudio/service/BillingAgreementChargeTimestampIntegrationTest.java) | Entire new file; 51-80 isolation, 86-127 registration/failure/retry, 133-195 monetary success and DONE replay; persisted reloads, zero/one paid ledger and confirm-only versus confirm+charge assertions |
| [PaymentCommandIndependentVerificationIntegrationTest.java](../../src/test/java/com/atstudio/atstudio/service/PaymentCommandIndependentVerificationIntegrationTest.java) | 219-250: renewal advances prior time and completed finalization replay preserves it |
| [SubscriptionUpgradeCommandIntegrationTest.java](../../src/test/java/com/atstudio/atstudio/service/SubscriptionUpgradeCommandIntegrationTest.java) | 121-179: provider-success/local-finalization rollback preserves history, retry advances it, DONE replay preserves timestamp and next billing date |
| [RecurringRenewalCommandIntegrationTest.java](../../src/test/java/com/atstudio/atstudio/service/RecurringRenewalCommandIntegrationTest.java) | 115, 265-401, 486-491: forced in-memory H2, three committed retry attempts on one logical order, terminal suspension, inclusive grace, denied first download and licensed repeats; only LocalDate.now() is pinned for downloads |
| [DownloadServiceTest.java](../../src/test/java/com/atstudio/atstudio/service/DownloadServiceTest.java) | 101-117, 238-252: existing-license resource identity and no quota/history interaction; denied first download has no persistence/storage effects |

Relevant unchanged contracts cross-checked:

- `BillingAgreementPrepareTransactionService.java:86-90,135-137,266-268`: purpose derives from current service-enabled subscription; registration amount is zero and preparation calls the history-preserving entity method.
- `BillingAgreementApplicationService.java:197-205,273-289,336-346`: registration returns through finalization before charge; initial paid path records provider success before finalization; completed claims do not replay provider work.
- `BillingAgreementCommandIntegrationTestSupport.java:67-80,149-151,251-287`: mocked crypto/mail/cleanup, fresh entity reads, fake provider and no-transaction assertion. The new pure-prepare spy is test-local.
- `DownloadService.java:43-94`; `UserSubscriptionRepository.java:26-30`: licensed repeats precede entitlement/quota checks; new downloads require ACTIVE/CANCELLED with inclusive expiresAt >= today.
- `frontend/src/api/userSubscriptions.ts:23-58`: preview contains immediate amount and next-cycle fields, while mutation accepts plan/cycle, not a server-locked quote token.
- `frontend/src/api/admin.ts:529-588,899-970` and existing correction request validation: preview/record target fields support the confirmation summary; client-selected target still requires server authorization and execution checks.

## Commands & Outputs

All commands ran read-only in `C:/Users/jm991/Desktop/project/ATStudio`, except apply_patch for the two owned documents.

- `git status --short --branch`, `git rev-parse HEAD`: correct branch/HEAD; existing unrelated untracked artifacts preserved.
- `git diff --stat`, path-scoped `git diff`, `rg -n`, numbered UTF-8 Get-Content reads: reviewed current diff and relevant contracts.
- `git diff --check`: exit 0; CRLF-to-LF notices were informational, not whitespace errors.
- PowerShell XML parsing of `build/test-results/test/TEST-*.xml`: independently read the existing 10-suite WI003 result, 83 tests, zero failures/errors/skips. No test was rerun by CR.
- `Get-FileHash -Algorithm SHA256`: the timestamp-suite and state-machine XML hashes match WI003's recorded values; all four frontend product/CSS freeze hashes match WI002's recorded values.
- Final scoped document check: 2 ASCII documents, 28 relative Markdown links, zero missing targets; required metadata inspected. All 16 source/test hashes remained identical before and after document creation. `git diff --check` again exited 0. Aggregate documentation validation remains MA/WI005-owned.

Two initial path searches included nonexistent optional filenames and returned rg exit 2; their real paths were located and read afterward. A hash-inventory JSON parse initially encountered Git stderr warnings; it was repeated with stderr separated. Neither event modified files or affected the findings.

## Tests

| Evidence | Result | CR verification mode |
| --- | --- | --- |
| WI001 focused JUnit | 18 passed; 0 failed/errors/skipped | Owner Evidence Pack plus all 18 included in the independently parsed WI003 XML |
| WI003 focused JUnit | 83 passed across 10 suites; 0 failed/errors/skipped | Existing XML parsed by CR, not rerun |
| New charge-timestamp suite | 6 passed, included in 83 | Source review and exact XML hash match |
| WI002 focused Vitest | 301 passed across 3 files | Owner-reported execution; test source reviewed, not rerun |
| WI002 TypeScript / ESLint / changed-file Prettier | PASS | Owner-reported execution only |
| MA isolated aggregate backend build | PASS; 1,700 total / 1,681 passed / 19 skipped / 0 failures | MA-reported completion, not executed by CR |
| MA aggregate frontend tests/format/build | PASS; 1,487 tests; all-format and build PASS | MA-reported completion, not executed by CR |
| MA isolated fixture browser checks | PASS at 390 and 1440 widths | MA-reported completion, not independently observed by CR |

The 18-test count is included in 83, not additional. XML timestamps span `2026-09-08T09:05:28.633Z` through `2026-09-08T09:06:00.207Z`.

Verified XML SHA-256:

- Charge timestamp: `22F4530B62DF84462EE2068A57B6F8F3A110DD6777A6E5A9DEFAC86C40D169D5`.
- State machine: `63D62F65DC77243CBAD2BBDCF68A0A98BFB22DB339AD170057DE645A291A70C0`.

Generated XML can be replaced by later runs. The hashes identify the reports actually inspected here.

MA supplied its completed aggregate results during review closeout. Its browser fixtures covered the upgrade dialog, card hint, required ADMIN correction expiry and create confirmation, with zero actual writes, page errors or overflow. MA reported that the ADMIN select's accessible name includes option text; adjusting its automation locator resolved that test issue without a product change. This was not a CR product finding or a fixed product delta. Exact aggregate logs and browser artifact paths remain MA/WI005-owned.

## Test Gaps And Boundaries

- No new test execution, coverage measurement, real browser inspection or aggregate build was performed by CR. Owner-reported frontend checks and MA-supplied screenshots are not independent CR execution evidence.
- UI retirement tests assert dialog removal and no writes, including clicks on saved detached DOM nodes. Those clicks do not independently invoke a captured React handler; the snapshot/generation/ref guard assessment additionally relies on source review. This is a proof boundary, not a demonstrated product defect.
- The new positive SUBSCRIBE test exercises ordinary success and completed replay; it does not independently cover the existing-payment/PROVIDER_SUCCEEDED recovery branch at PaymentCommandTransactionService.java:555. That branch was statically checked: the charge-recording call was moved from its shared helper without changing its behavior.
- A confirmation binds the displayed client snapshot, not a server-locked price. Server-side changes between preview and mutation remain governed by the existing API. No policy expansion or new quote-lock mechanism is requested.
- H2 and fake provider assertions do not prove live Toss, operating MySQL locks, parallel/multi-server races, SMTP, actual elapsed days or midnight scheduling. The grace test pins only the download business-date lookup; it does not validate timestamp clocks, daily-quota reset or the separate scheduler's EXPIRED state transition.
- Timestamp preservation is prospective. Existing lost/overwritten historical values are not repaired; timestamps remain local successful-finalization times, not provider-authoritative charge timestamps.
- No runtime deployment status was independently inspected by CR. WI003 reports that the runtime still uses the old JAR; MA must preserve the distinction between source verification and deployed code.

## Reviewed Source Snapshot

Full SHA-256 values at review closeout; these identify the reviewed dirty-worktree content, not just HEAD.

| Path | SHA-256 |
| --- | --- |
| frontend/src/pages/admin/PaymentOperationsPage.test.tsx | 6C0B8506F94CB9FC41135FEFD44FFF8E0651216014E2C9595E65DB1F13126FDC |
| frontend/src/pages/admin/PaymentOperationsPage.tsx | A08FDCCA4B6AD21C7CF1A7093995092B3D68ABFFCE2005DA78A6D556CEC44395 |
| frontend/src/pages/subscriber/SubscriptionManagePage.module.css | 929BA56EE932649B9DDFFFA43055285CD37DCE02AD5FD7ADCAD4753C977D98EE |
| frontend/src/pages/subscriber/SubscriptionManagePage.test.tsx | E3D9859F333CB85EFD1C365FA34F914FA204CCD2A262CA5904A0D37F99EE5996 |
| frontend/src/pages/subscriber/SubscriptionManagePage.tsx | 8E0C8B02011BB9183530A1512002EB4A54445B7AC08683B2FAF9CC8520E6169C |
| frontend/src/pages/subscriber/SubscriptionPaymentPage.test.tsx | A927D05B55F3CD212BF372FFEE3BB634E57AEE1B177165DF27D7718CA94488D0 |
| frontend/src/pages/subscriber/SubscriptionPaymentPage.tsx | 49F959725A34B79F51534EF723941A7C535EB7981807EF9ECAE44E98B31CD36F |
| src/main/java/com/atstudio/atstudio/entity/BillingAgreement.java | 77098F79B38F94CC5196EBE8BE21709DDDEDCED66FE441837C8BFB0771482C95 |
| src/main/java/com/atstudio/atstudio/service/PaymentCommandTransactionService.java | E995009FE16243F27388007181EF94DD948115B5F9D1C4E150361DCDCAB79D16 |
| src/main/resources/schema.sql | 56C54247F9F5C595CA4A178FC99C24793A39488CF9213C4F4662EBD9A3D1C3CA |
| src/test/java/com/atstudio/atstudio/entity/BillingAgreementStateMachineTest.java | D2DFBB2E9D6F28D80921A90C00122685C2D8E4BCE05A85A895C2914AA7A67F14 |
| src/test/java/com/atstudio/atstudio/service/BillingAgreementChargeTimestampIntegrationTest.java | 3237F3DA4D74A6218CE884AE1DC3D10809023C630001CFD440B71E1D2FC5F88B |
| src/test/java/com/atstudio/atstudio/service/DownloadServiceTest.java | 31F5B2D62F264041209BBE8D113F5AC2D7D87ED7CEE8EC2AB1005CB45C308125 |
| src/test/java/com/atstudio/atstudio/service/PaymentCommandIndependentVerificationIntegrationTest.java | 8E5799030E7D1119AE62C4A7A98E4BD99739F14FF74883B633426BBFD46BB405 |
| src/test/java/com/atstudio/atstudio/service/RecurringRenewalCommandIntegrationTest.java | DFDC0149346D0C42091B4CDFF8AB6BC625591B534EFFCAA9614863AD607BB40C |
| src/test/java/com/atstudio/atstudio/service/SubscriptionUpgradeCommandIntegrationTest.java | C567AED60C0DEA96275DA2DE0D043AC03EDFF2820F2F2913581AB185A348622B |

## Risks / Rollback

CR introduced documentation only. No product, schema, DB, provider, server or client-worktree rollback is applicable. Any retirement/correction should target only this WI's two documents and preserve every other contributor's changes; no reset, checkout or deletion was performed.

## Follow-ups

WI004 independent review is complete and releases **WI-20260908-ATS-005**. MA has reported completion of the aggregate backend/frontend/browser gates. MA should immediately activate the existing WI005 handoff through the approved delegation flow, attach the exact aggregate/browser/operational evidence paths, and record remaining runtime/production limits. CR did not spawn an agent, activate WI005 itself or close the overall REQ. Any subsequent product/test delta needs owner verification and a bounded re-review before reusing this conclusion.

## Related Documents

- [WI004 Handoff](WI-20260908-ATS-004-handoff.md)
- [WI004 User Summary](../user/WI-20260908-ATS-004-summary.md)
- [Approved REQ](../user/REQ-20260908-ATS-001.md)
- [WI001 Evidence](WI-20260908-ATS-001-evidence-pack.md)
- [WI002 Evidence](WI-20260908-ATS-002-evidence-pack.md)
- [WI003 Evidence](WI-20260908-ATS-003-evidence-pack.md)
- [WI005 Handoff](WI-20260908-ATS-005-handoff.md)
- [Payment Acceptance Checklist](../../docs/payment/acceptance-test-checklist.md)

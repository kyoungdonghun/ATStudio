# Evidence Pack: WI-20260716-ATS-015

## Summary (one-liner)

- Completed the independent approved-intent/backend/frontend/API/docs/client three-way review and produced severity-ranked WI-017 dispositions without modifying product code, current documentation, runtime, DB, providers, secrets, or Git state.

## Scope / DoD Check

- [x] Reconciled the high-risk WI-005 through WI-014 domains.
- [x] Reviewed payment, whitelist, certification, auth/OAuth, download/license, catalog/playlist, playback, role, and client/PDF contracts.
- [x] Carried forward, refined, or closed every WI-013/WI-014 residual.
- [x] Recorded exact source/doc pointers, impact, severity, and WI-017 action.
- [x] Kept the review source/report-only and development-branch-only.

## Reference Documents (Tier 0-2)

| Tier | Document | Reason |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Product invariants and evidence policy |
| 0 | `docs/standards/development-standards.md` | Backend/frontend contract and test standards |
| 1 | `docs/policies/security-policy.md` | Security, payment, media, whitelist, and certification boundaries |
| 1 | `docs/policies/quality-gates.md` | Regression and release-evidence boundaries |
| 2 | `docs/design/api-spec.md` | API contract SoT |
| 2 | `docs/design/db-schema.md` | Schema/entity contract |
| 2 | `docs/design/remaining-remediation-design-20260716.md` | Approved residual ownership |
| 2 | `docs/design/usecase/`, `docs/ui/`, `docs/payment/`, `docs/client/`, `docs/SR/`, `docs/registry/` | Cross-layer and client contract set |
| Context | `deliverables/user/REQ-20260716-ATS-002.md` | Approved scope and fixed product invariants |
| Evidence | `deliverables/agent/WI-20260716-ATS-005-evidence-pack.md` through `WI-20260716-ATS-014-evidence-pack.md` | Prior implementation and independent backend/frontend evidence |

## Findings

No P0 or P1 finding was identified.

| ID | Severity | Contract mismatch / residual | Exact evidence | WI-017 disposition |
|---|---|---|---|---|
| F-015-01 | P2 Medium | Certification self-service is USER+BUSINESS in routes/docs but authenticated+BUSINESS in backend | `frontend/src/router/index.tsx:131-137,203-204`; `docs/design/usecase/company-certification.md:12`; `docs/design/api-spec.md:3330`; `SecurityConfig.java:120-126`; `CompanyCertificationService.java:72-77,112-117,275-288` | Enforce USER role on self-service endpoints; add BUSINESS ADMIN denial tests |
| F-015-02 | P2 Medium | Client prohibits payment-key display while admin API/UI exposes provider payment identifiers | `docs/client/0-site-policy.md:46`; `docs/client/testing-guide.md:66`; `PaymentReadOnlyPage.tsx:1469,1577,1918`; `frontend/src/api/admin.ts:276,321,379,397-399`; payment response DTO fields | Select one protected-reference policy; mask/remove default UI/API exposure or explicitly authorize and document it; preserve server-only refund identifier |
| F-015-03 | P2 Medium | Player subscription errors collapse to inactive | `PlayerBar.tsx:86-94,277-350,760-779,993-1010`; `SubscriberRoute.tsx:45-63`; `docs/design/usecase/user-subscription.md:163` | Explicit state model, strict no-active classification, abort/generation fence, retry and race tests |
| F-015-04 | P2 Medium | Four current loaders can commit stale responses | `TrackDetailPage.tsx:36-45`; `UserManagePage.tsx:29-38`; `UserSubscriptionManagePage.tsx:50-59`; `DownloadQueuePage.tsx:69-90`; `docs/ui/screen-flow.md:68` | Add cancellation/generation fences and out-of-order tests, or narrow documentation where intentionally unsupported |
| F-015-05 | P2 Medium | Admin financial mutation/read HTTP wiring lacks focused regression proof | `PaymentReadOnlyPage.tsx:403-432,481-641,2014-2016`; `PaymentReadOnlyPage.test.tsx:195-291`; `WI-20260716-ATS-013-evidence-pack.md:143-149` | Test settlement/refund/entitlement mutation UI and backend Controller/read wiring |
| F-015-06 | P2 Medium | Storage compensation and selected audit/read/transport branches remain thin | `WI-20260716-ATS-013-evidence-pack.md:143-150` | Risk-based rollback/after-commit/retry/idempotency and operations-contract tests |
| F-015-07 | P2 Medium | Eight retained-MySQL proof tests were skipped | `WI-20260716-ATS-013-evidence-pack.md:112-116,169-170` | Run on approved disposable/copied MySQL; do not mutate production/current data |
| F-015-08 | P3 Low | OAuth login loses validated protected-route return target | `LoginPage.tsx:137,228-262`; `SocialLoginPage.tsx:72-77`; `SocialCompleteProfilePage.tsx:140` | Per-attempt safe session target, consume once, add rejection/return tests |
| F-015-09 | P3 Low | Playlist use case says physical playlist deletion; implementation and approved REQ use soft delete | `PlaylistService.java:254-262`; `Playlist.java:42-44`; `PlaylistServiceTest.java:465-492`; `deliverables/user/REQ-20260221-ATS-004.md:25`; `docs/design/usecase/sound-playlist.md:178-181` | Correct use case to junction hard delete plus playlist soft delete |
| F-015-10 | P3 Low | Active frontend standard/router comment retain stale screen counts, and docs index undercounts Standards/total by one | `docs/standards/frontend-standards.md:305-313`; `frontend/src/router/index.tsx:139`; `docs/ui/atstudio-front-list.md:27-30,46`; `docs/index.md:21-40`; actual Standards 13 / managed docs 194 | Replace screen counts with current unit or UI-inventory pointer; synchronize docs index to Standards 13 / total 194 |
| F-015-11 | P3 Low | Symlink rejection branch remains host-conditional | `LocalStorageServiceTest.java:68-88`; `WI-20260716-ATS-013-evidence-pack.md:112-116` | Add symlink-capable host evidence |
| F-015-12 | P3 Low | Backend complete gate is heap/timeout sensitive | `build.gradle:67-83`; `WI-20260716-ATS-013-evidence-pack.md:174-175` | Record >=1 GiB / sufficient timeout runner budget |
| F-015-13 | P3 Low | Unchecked compiler warning has no file-level owner | `WI-20260716-ATS-013-evidence-pack.md:174-176` | Identify source and disposition separately |

## Verified High-Risk Matrix

| Domain | Result | Backend / frontend / documentation pointers |
|---|---|---|
| Recurring payment and zero-amount re-registration | VERIFIED except F-015-02/F-015-05 | `BillingAgreementApplicationService.java:107-157,243-255`; `PaymentCommandTransactionService.java:493-523`; `frontend/src/api/payments.ts:25-68`; `docs/design/api-spec.md:1439-1523`; `docs/client/2-full-feature-checklist.md:70` |
| Refund, entitlement correction, settlement, reconciliation | VERIFIED behavior; proof debt remains | `AdminPaymentController.java:55-280`; `frontend/src/api/admin.ts:216-801`; `docs/design/api-spec.md:1583-2304`; F-015-05 |
| Whitelist lifecycle and immutable CSV export | VERIFIED | `AdminWhitelistChannelService.java:48-65`; `WhitelistChannelService.java:85-200`; `AdminWhitelistChannelController.java:62-83`; `whitelistStatusTransitions.ts:3-12`; `docs/design/api-spec.md:3248-3289` |
| Company certification | VERIFIED shapes/workflow; role gap found | `CompanyCertificationResponse.java:8-20`; `frontend/src/types/index.ts:243-277`; certification API/use case; F-015-01 |
| Auth/protected routes/OAuth | VERIFIED password guard; OAuth return gap found | `ProtectedRoute.tsx:53-56`; `LoginPage.tsx:61-121,137,215`; `SubscriberRoute.tsx:25-76`; F-015-08 |
| Official download and license | VERIFIED; retained-MySQL proof conditional | `TrackController.java:82-91`; `DownloadService.java:40-91`; `frontend/src/api/downloads.ts:13-18`; `docs/design/api-spec.md:503-515,2456-2509`; F-015-07 |
| Catalog and playlists | VERIFIED ownership/limit/ordering behavior; doc error found | `PlaylistService.java:46-74,103-183,219-260,282-345`; `frontend/src/api/playlists.ts:33-101`; `docs/design/usecase/sound-playlist.md`; F-015-09 |
| Public full-track playback | VERIFIED invariant | `SecurityConfig.java:73-76,87`; `TrackController.java:94-152`; `TrackService.java:151-160`; `docs/design/api-spec.md:488-501`; `docs/client/0-site-policy.md:19,25-27` |
| Frontend error/race state | NOT CLOSED | F-015-03/F-015-04 |
| Client/PDF acceptance wording | VERIFIED with one contradiction | Full listening, download, payment environment caveats are accurate; F-015-02 is inherited by the generated client guide/PDF |

## Count Reconciliation

| Unit | Result |
|---|---:|
| Method-level REST mappings excluding `SpaForwardController` | 149 |
| `CREATE TABLE` / JPA `@Entity` | 41 / 41 |
| Router paths / index redirects | 62 / 1 |
| Lazy page components / distinct visual page UIs | 54 / 53 |
| `<Modal` call sites / non-test TSX files | 23 / 17 |
| `.claude/agents/*.md` | 13 |
| SR total and statuses | 92 = 82 DONE + 7 OPEN + 2 NOT CONFIRMED + 1 DROPPED |
| Managed documentation excluding index files | 194; published index says 193 |

API/DB/UI/SR units match their current inventories. The managed-document recount found the one-file `docs/index.md` drift recorded in F-015-10.

## WI-013 / WI-014 Disposition

| Prior item | Disposition |
|---|---|
| WI-013 risk coverage debt | Carried and refined as F-015-05/F-015-06 |
| WI-013 eight skipped MySQL proofs | Carried as F-015-07; environment-conditional |
| WI-013 symlink/heap/warning | Carried as F-015-11/F-015-12/F-015-13 |
| F-014-01 | Confirmed as F-015-03 |
| F-014-02 | Confirmed as F-015-04 |
| F-014-03 | Confirmed and strengthened with backend coverage evidence as F-015-05 |
| F-014-04 | Confirmed as F-015-08 |
| F-014-05 | Confirmed and expanded to active frontend standard as F-015-10 |
| New in WI-015 | F-015-01, F-015-02, F-015-09 |

## Commands & Outputs

- Source/report inspection only: targeted `Get-Content`, `Select-String`, and `rg` over handoff pointers and current source.
- Branch/status inspection: development branch `codex/p1-acceptance-hardening`; existing edits preserved.
- Count methods: annotation/source counts for REST/entities/routes/pages/modals/agents; SQL `CREATE TABLE`; SR main-table statuses; managed docs category rules.
- No application process, DB, provider, secret, client runtime, staging, commit, or push operation was used.

## Tests

- No new application test suite was run by design; WI-015 is an independent source/report contract review.
- Backend baseline carried from WI-013: 146 suites, 1,046 tests, 1,037 passed, 0 failures/errors, 9 skipped; JaCoCo lines 76.81%, branches 59.05%, methods 76.57%.
- Frontend baseline carried from WI-014: 38 test files, 180 tests passing; V8 statements 34.49%, branches 34.00%, functions 27.82%, lines 35.43%; typecheck/lint/Prettier/build passed.

## Risks / Rollback

- Risks: this WI deliberately does not close environment-only MySQL, live provider, browser/media, proxy/CORS, or retained-data evidence.
- Release gate: WI-017 must disposition all P2 findings and rerun the complete backend/frontend/document gates before release readiness is reconsidered.
- Rollback: remove only `deliverables/user/WI-20260716-ATS-015-summary.md` and this Evidence Pack; no product state changed.

## Follow-ups

- `WI-20260716-ATS-017`: implement the ranked dispositions in the user summary, then rerun complete backend/frontend/docs/PDF verification.

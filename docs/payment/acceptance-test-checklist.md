---
version: 1.10
last_updated: 2026-09-08
project: ATS
owner: qa
category: guide
status: stable
dependencies:
  - path: index.md
    reason: Payment documentation navigation
  - path: user-flows.md
    reason: User-facing flow definitions
  - path: admin-operations-guide.md
    reason: Admin flow definitions
---

# Payment Acceptance Test Checklist

> Purpose: Provide the current payment-system acceptance checklist for local verification, staging rehearsal, and client-adjacent review.

---

## 2026-09-08 Acceptance Record

MA supplied the results below. Earlier joint user acceptance involved real
Toss **TEST** and Gmail; subsequent source-closeout checks made no new external
financial or mail calls. Generic checklist boxes below remain reusable and do
not imply that every scenario was exercised. IDs, logs and detailed proof limits
are in [WI005 evidence](../../deliverables/agent/WI-20260908-ATS-005-evidence-pack.md).

| Earlier joint acceptance | Result and boundary |
| :-- | :-- |
| Email verification / password reset | User confirmed real Gmail delivery and completion. |
| Initial subscription / upgrade | STANDARD YEARLY 99,000 KRW plus DELUXE upgrade 100,000 KRW succeeded. Paid YEARLY period stayed 2026-09-08 through 2027-09-08; selected MONTHLY was next-renewal only, not a new annual term or refund. |
| Reservations / cancellation | Pending downgrade/cadence reservation and cancellation passed; cancellation/reactivation preserved paid access. |
| Card re-registration | Amount-0 order completed at 16:39:45 with the period preserved. The unusable agreement was an authorized local `SUSPENDED` fixture, not provider-real removed-key detection. |
| Renewal failure / retry | Controlled Toss `REJECT_CARD_PAYMENT`, then a logical-next-day retry succeeded for 199,000 KRW on the same order with two attempts. No clock change or midnight observation. Failure mail reached Gmail spam in English, not the inbox. |
| Refund / correction | Only the extra 199,000 KRW renewal was refunded: provider `CANCELED`, balance 0, one cancel. The original 99,000 + 100,000 remained. Corrected local `CANCELLED` access through 2027-09-08 succeeded at 17:26:28 after explicit approval. |
| Official Download | Repeating Track 4 added no quota/history/License. New Track 5 at 17:43:47 created download and License; daily count 2/20, remaining 18. |

| Subsequent verification | Result and evidence boundary |
| :-- | :-- |
| Full backend | Isolated build exit 0, 3m27s; JUnit 1,700 total / 1,681 passed / 19 skipped / 0 failures / 0 errors. Skips: 18 gated MySQL cases and 1 platform-dependent LocalStorageServiceTest. JaCoCo lines 87.67%, methods 85.40%, branches 73.03%; threshold PASS. |
| Full frontend | 112 files / 1,487 tests passed / 0 skipped, 53.32s. Coverage statements 90.22%, lines 92.8%, functions 91.18%, branches 82.74%. Build, full Prettier and final frozen-source typecheck/ESLint PASS. |
| Focused evidence / independent review | WI001: 18 JUnit; WI002: 301 Vitest; WI003: 83 JUnit including WI001's 18. These overlap the full suites and are not additive. WI004 reviewed 16 files with no actionable findings. |
| Real Edge browser, synthetic APIs | Playwright at 1440x900 and 390x900 passed upgrade confirmation/cancel, allowlisted card hint/status recovery, and required ADMIN expiry/target confirmation/cancel. All API calls were intercepted, external access blocked, zero actual writes/page errors/overflow; one stubbed preview POST per ADMIN viewport is not real API proof. |

The [central source/runtime snapshot](index.md#2026-09-08-source-and-runtime)
and [production gates](../SR/SR-93.md#remaining-production-gates) govern reuse.
Completed acceptance cases are not automatically reset. Changed paths require
risk-based regression before deployment; unlisted cases and target-production
acceptance are not silently passed.

### REQ002 Copy Follow-up

The tables above preserve the earlier acceptance and WI005 source snapshot.
Subsequent [WI006](../../deliverables/agent/WI-20260908-ATS-006-evidence-pack.md)
passed 38 focused mail tests, including Korean UTF-8 MIME readback and escaping;
[WI007](../../deliverables/agent/WI-20260908-ATS-007-evidence-pack.md) passed 139
focused frontend tests plus TypeScript, ESLint and Prettier for the two admin
entry points, strict new confirmation wording and stored receipt evidence.
These focused results must not be added to aggregate suite counts.

MA's first frontend aggregate was 1,493 total / 1,490 passed / 3 failed from
stale UI expectations in the existing coverage test. The authorized test-only
extension passed 24 focused tests and was refrozen; the aggregate rerun is
recorded separately: **r2 exit 0, 112 files / 1,493 passed / 0 failures**, 45.27s,
with the fixture-file hash stable during the run. Coverage: statements 90.22%,
lines 92.8%, functions 91.18%, branches 82.75%. Full TypeScript, ESLint,
frontend build and formatting passed. The initial failed run remains history.

MA's final isolated backend build passed in 3m45s: 191 JUnit XML suites,
1,708 tests / 1,689 passed / 19 skipped / 0 failures / 0 errors. JaCoCo gate
passed (lines 87.69%, methods 85.40%, branches 73.07%). Outputs stayed in
`build-copy-polish/`; temporary H2 schemas were used, not actual MySQL or
live provider/mail calls. During that WI008 source-only run, the running JAR
and original process stayed unchanged; WI009 adoption is recorded below.

MA's synthetic-browser checks passed at widths 1440 and 390: new admin labels,
receipt header/status, mobile horizontal table scrolling without page overflow,
and confirmation text fitting at 390. API responses were stubbed; screenshots
were visually inspected. Final aggregate results and artifact pointers belong to
[WI008](../../deliverables/agent/WI-20260908-ATS-008-evidence-pack.md).
REQ002's WI006-008 source work is complete; WI008 document validation
passed with 675 traceability IDs, valid links and index coverage.

The subsequent approved [WI009 restart](../../deliverables/agent/WI-20260908-ATS-009-evidence-pack.md)
completed development-runtime adoption of the unchanged WI008-tested JAR.
MA's 20:11+ KST record includes five successful local/public GETs, exact-origin
backend OPTIONS, delivery of the changed Vite source and basic unauthenticated
HomePage rendering inspected through CUA AX/screenshot. Artifact identity,
processes, URL and preserved settings are in the
[central snapshot](index.md#2026-09-08-source-and-runtime). Earlier failed restart
attempts remain historical in WI009, not the current runtime status.

In WI010, MA rechecked unchanged backend/frontend/tunnel command-line and
artifact ownership and three local/public HTTP 200 responses. After the user
supplied login through CUA, the real public `/admin/payments` rendered actual
orders and all nine tabs, including `결제 점검 이슈` and `구독 이용권 조정`.
This current browser uses real APIs, not the earlier stubs/fixtures. The
receipts tab displayed four actual `ISSUED` rows as `발급 기록`, the heading
`증빙 상태` and caption `원결제 영수증 · 환불 상태와 별도`. MA inspected the
desktop screenshot: horizontal scrolling stayed inside the table container
and labels did not overlap. External receipt links were not opened. The issue
tab loaded its normal OPEN empty state without saving state. The correction
tab had blank `targetExpiresAt`, disabled request creation, existing
`SUCCEEDED`/`CANCELLED` rows and disabled execution buttons with the new
typed-confirmation tooltip. No mutations or new requests were executed.
At the second entry, `/admin/user-subscriptions` displayed five actual rows.
The `구독 이용권 조정` button opened subscription 5's modal with new title,
stage names and warnings. Reads completed normally; the existing
DELUXE/YEARLY/CANCELLED target and 2027-09-08 expiry were preserved. An empty
reason kept preview/create disabled. MA inspected the screenshot with no
overlap, closed without editing/saving and observed the same unchanged row.
These are bounded actual desktop checks, not fresh subscriber upgrade or
failure-callback mutation acceptance; those paths retain component-test and
previous synthetic-browser evidence. Scoped commit/push remains pending.
MA also reports fresh focused Vitest 5 files / 354 passed in 11.42s, typecheck,
lint and full Prettier PASS; prior backend/full frontend results were not rerun.
MA independently recounted the prior 191 backend XML reports: 1,708 total /
1,689 passed / 19 skipped / 0 failures/errors. Artifact hash still equals the
tested build; a recount and hash match are not a new backend test run.
[WI010 evidence](../../deliverables/agent/WI-20260908-ATS-010-evidence-pack.md)
holds the received results and remaining limits. No fresh updated-mail SMTP
receipt, inbox/spam improvement, refund aggregate, new API or complete
role/financial acceptance is claimed.
The observed cover fallback is undiagnosed; neither all-media health nor a
causal link to the startup audit's 10 missing references is established.
No full suite was rerun in WI009; external/production gates remain OPEN.

## 1. Test Preparation

| Check | Expected Result | Done |
| :-- | :-- | :-- |
| The environment is an approved local/test/staging environment using Toss test configuration. | No live key, real-money payment, retained production DB, or production deployment is used by this checklist. | [ ] |
| Any remotely shared frontend uses the explicitly approved release checkout and revision through the operator-controlled acceptance lifecycle. | Record checkout, branch, revision and exact browser origin; local page and API proxy pass before a newly issued public URL is shared. The 2026-09-05 local closeout uses `codex/v1-release-rehearsal-fixes`, not an instruction to switch or modify a separate client worktree. Historical URLs are not reused. | [ ] |
| Backend and frontend are running against the intended local or staging environment. | User can open `/subscriptions` and admin can open `/admin/payments`. | [ ] |
| The external acceptance backend-environment bundle uses only current allowlisted names. | Obsolete `APP_PAYMENT_PROVIDER`, `TOSS_CONFIRM_URL`, and `PAYMENT_BILLING_KEY_ENCRYPTION_SECRET` entries are absent; a bundle containing one of them is rejected instead of being treated as a compatibility configuration. | [ ] |
| Toss test client key and secret key are configured for recurring billing. | Checkout opens Toss billing auth instead of provider-not-configured error. | [ ] |
| Billing-key active ID and V2 key ring are configured outside the repository. | Billing-key confirmation does not fail due to missing or invalid key-ring configuration. | [ ] |
| Test user has no active subscription for new-subscription tests. | New checkout starts from a clean state. | [ ] |
| Admin account can access `/admin/payments`. | Payment operations tabs are visible. | [ ] |

## 2. New Subscription

| Check | Expected Result | Done |
| :-- | :-- | :-- |
| Select a plan and billing cycle on `/subscriptions`. | User is routed to `/subscriptions/checkout`. | [ ] |
| Start Toss card registration. | Toss billing auth opens and returns to success/fail callback. | [ ] |
| Complete Toss test billing auth. | Backend confirms billing key and charges first period. | [ ] |
| Return to `/subscriptions/manage`. | Current plan, cycle, start date, expiration, payment method, and next billing date are visible. | [ ] |
| Inspect admin payment orders and subscription payments. | `payment_orders` and `subscription_payments` show the completed charge. | [ ] |

## 3. Billing Method Re-registration

| Check | Expected Result | Done |
| :-- | :-- | :-- |
| Use an active subscription with missing/expired/unusable billing agreement. | Manage page shows payment method re-registration guidance. | [ ] |
| Click payment method registration. | Checkout opens with `purpose=BILLING_AGREEMENT` and the prepared amount is `0`. | [ ] |
| Complete billing auth. | Billing agreement becomes usable without charging the card or changing the current plan/period in this step. | [ ] |
| Return to manage page. | User can retry upgrade or wait for renewal using the new billing method. | [ ] |

## 4. Upgrade

| Check | Expected Result | Done |
| :-- | :-- | :-- |
| Select a higher-tier plan in manage page. | Preview shows upgrade and immediate charge amount. | [ ] |
| Confirm upgrade with reusable billing method. | Remaining-period difference is charged through the billing agreement. | [ ] |
| Verify active plan. | Higher-tier plan is active immediately. | [ ] |
| Upgrade while selecting a different billing cycle. | Higher plan applies now; next renewal cycle is shown as pending. | [ ] |
| Inspect admin payments. | Upgrade order/payment and receipt evidence appear without raw sensitive fields. | [ ] |

## 5. Downgrade and Billing Cycle Change

| Check | Expected Result | Done |
| :-- | :-- | :-- |
| Select a lower-tier plan. | Preview shows scheduled change and no immediate payment. | [ ] |
| Confirm downgrade. | Current plan remains active until expiration; pending plan is shown. | [ ] |
| Select a different cycle for the same plan. | Cycle-only change is scheduled for next renewal. | [ ] |
| Select current plan/cycle while pending change exists. | Pending change is cleared. | [ ] |

## 6. Cancellation and Reactivation

| Check | Expected Result | Done |
| :-- | :-- | :-- |
| Cancel subscription. | Status becomes cancelled, future renewal stops, access remains until `expiresAt`. | [ ] |
| Confirm subscriber-only access before expiration. | Cancelled grace-period subscriber can still use subscription-gated features. | [ ] |
| Reactivate before expiration. | Status returns to active and renewal can continue if billing method is reusable. | [ ] |
| Let subscription pass expiration. | Subscription becomes expired and subscriber-only access is blocked. | [ ] |

## 7. Renewal and Failure

| Check | Expected Result | Done |
| :-- | :-- | :-- |
| Trigger or wait for due renewal. | Scheduler charges through billing agreement and extends subscription on success. | [ ] |
| Use an operator-prepared provider failure scenario. | The user sees a safe failure; the order remains visible and a retry is scheduled within the grace period without creating a second completed payment. | [ ] |
| Use an operator-prepared repeated-failure or grace-expiration scenario. | The billing agreement becomes suspended and subscription access expires after the grace period as appropriate. | [ ] |
| Confirm failure email behavior. | Korean payment guidance distinguishes retry from suspended renewal; dynamic values are escaped and failures are logged without secrets. Source tests do not prove delivery or inbox placement. | [ ] |

## 8. Account Withdrawal Safety

| Check | Expected Result | Done |
| :-- | :-- | :-- |
| Withdraw a password account with an ACTIVE subscription and billing agreement in a safe test environment. | User becomes deleted; subscription and agreement are locally `CANCELLED`; no refund row is created. | [ ] |
| After the next prepared renewal run, review the withdrawn account in admin payment screens. | No new renewal order or finalized payment appears for the deleted user. | [ ] |
| Use an operator-prepared Provider cleanup failure. | Withdrawal remains complete and one agreement-scoped `WARNING` Incident is visible. | [ ] |
| After the prepared cleanup retry succeeds, review the Incident. | The matching Incident becomes `RESOLVED`; the user is not reactivated and no refund is created automatically. | [ ] |

## 9. Admin Payment Operations

| Check | Expected Result | Done |
| :-- | :-- | :-- |
| Open orders tab. | Payment orders are paginated and support-safe. | [ ] |
| Open automatic payment tab. | Billing agreements show masked method only. | [ ] |
| Open payments tab. | Finalized charges are visible and refund preview can be started. | [ ] |
| Open incidents tab. | Reconciliation incidents can be listed and status can be updated. | [ ] |
| Open receipts tab. | `증빙 상태` shows stored evidence, including `ISSUED` as `발급 기록`, separate from refund status. Unknown values and safe URL/reference fallback remain; no refund aggregate is inferred. | [ ] |
| Open audit tab. | Operation audit events appear for incident, receipt, refund, correction, and settlement actions. | [ ] |
| Import a valid strict UTF-8 settlement CSV at normal size. | `payment_settlements` rows are created and classified without changing payment/subscription/provider state; counts conserve and `omittedErrorCount` is `0`. | [ ] |
| Import a file with a quoted comma/newline and doubled quote using LF or CRLF. | The logical row imports with its starting physical line number; no value is split or silently rewritten. | [ ] |
| Try a missing/wrong extension, disallowed MIME, empty file, or file over 5 MiB. | The request is rejected before an import attempt, Settlement, or row audit is created. | [ ] |
| Try malformed UTF-8, malformed/unbalanced CSV, duplicate/unknown headers, or 1,001 nonblank logical data rows. | The claimed attempt becomes bounded `FAILED`; no Settlement row from the file is created. | [ ] |
| Import rows with exact-width/field errors and valid rows together. | The UI reports partial completion, shows every returned row error with zero omitted count, retains file/note correction context, and persists only valid rows/audits. | [ ] |
| Inspect an import with an operator note in browser request details. | `file` and trimmed nonblank `note` are multipart parts, the note is absent from the query string, and `Idempotency-Key` is header-only. | [ ] |
| Run settlement missing-provider scan with omitted dates and with a 90-day inclusive range. | The omitted range uses 30 inclusive days; both accepted requests process at most 5,000 selected payments and preserve count conservation. | [ ] |
| Run a 91-day range or prepared 5,001-payment selection. | The whole reconcile request is rejected before Settlement or audit mutation. | [ ] |
| Review a prepared reconciliation result with more than 200 row failures. | The UI shows the first 200 details, the exact omitted count, and partial-warning feedback rather than success. | [ ] |
| Ignore a settlement row. | Row becomes `IGNORED` with operator note. | [ ] |

## 10. Refund and Entitlement Correction

| Check | Expected Result | Done |
| :-- | :-- | :-- |
| Preview a refundable successful payment. | Refundable amount and reason are shown. | [ ] |
| Create refund request. | `payment_refunds` row is created; provider is not called yet. | [ ] |
| Approve refund request. | Status moves to approved. | [ ] |
| Execute refund with required confirmation text. | Toss cancel/refund is called with persisted idempotency key and result is recorded. | [ ] |
| Repeat while the same refund is already processing or awaiting Provider confirmation. | The existing refund remains the only request; the UI does not bypass it with a replacement refund. | [ ] |
| Confirm subscription access after refund. | Access is unchanged until entitlement correction is executed. | [ ] |
| Preview entitlement correction from succeeded refund. | Target local subscription state is shown. | [ ] |
| Execute entitlement correction in `/admin/payments` with `구독 이용권 조정 실행`. | Only the exact phrase is accepted; old wording and added whitespace are rejected. Approved local subscription state changes and audit log is recorded. | [ ] |
| Confirm general local correction in `/admin/user-subscriptions`. | Execute requires `구독 이용권 조정 실행` after trim; old wording is rejected. Approval still has no typed phrase and does not execute the correction. | [ ] |

## 11. Security and Data Boundary

| Check | Expected Result | Done |
| :-- | :-- | :-- |
| Inspect user checkout/manage screens. | Raw billing key, `authKey`, `customerKey`, Toss secret, and raw card number are not visible. | [ ] |
| Inspect admin payment screens. | Only order IDs, masked payment methods, and deterministic `REF-*` support references appear; exact provider identifiers do not. | [ ] |
| Review the operator-provided sensitive-data verification result. | The result confirms that secrets, raw card data, billing keys, and exact provider payment/refund/receipt/settlement identifiers are absent from user/admin output and Incident/audit free text. | [ ] |
| Review settlement rows after CSV import. | The UI shows only the fields needed for matching and operations; no secret or raw card field is visible. | [ ] |

## 12. Technical Evidence - No Client Action

The checks below are implementation-only. Client and ordinary operators should
not inspect transaction internals, run concurrency tests, or connect to a
database to reproduce them.

Checked technical rows retain their stated verification date and source
snapshot. They do not check any manual or client-acceptance row in Sections
1-11 or 13, and do not establish a current production database or deployment.

| Evidence | Authoritative pointer | Done |
| :-- | :-- | :-- |
| Stable command identity, strict Provider boundaries, refund lease fencing, finalize-only reconciliation, and payment-key minimization | [P1 Payment Integrity Closure](../audit/p1-payment-integrity-closure-20260715.md) and [WI-012 Evidence Pack](../../deliverables/agent/WI-20260715-ATS-012-evidence-pack.md) | [x] |
| Historical predecessor 41-table disposable MySQL 8/InnoDB schema validation and 7/7 race proof | [WI-007 Evidence Pack](../../deliverables/agent/WI-20260715-ATS-007-evidence-pack.md); historical only, not the current baseline | [x] |
| WI-067 strict settlement import/reconciliation repository and embedded-H2 review | [QA-INTEG v1.2](../../deliverables/agent/WI-20260809-ATS-067-qa-integ-review.md) and [PG v1.1](../../deliverables/agent/WI-20260809-ATS-067-pg-review.md) accepted DG-067-01..09A | [x] |
| Historical WI-067 42-table fresh-MySQL manifest and concurrency proof (2026-08-13) | [WI-067 Evidence Pack](../../deliverables/agent/WI-20260809-ATS-067-evidence-pack.md): recorded 42/506/173/90/6 manifest, independent Validate, 3/3 MySQL tests, exact cleanup; superseded source snapshot, not the current manifest expectation | [x] |
| Current recorded 43-table manifest contract, proven on a disposable database on 2026-08-17 | [WI-016 Evidence Pack](../../deliverables/agent/WI-20260817-ATS-016-evidence-pack.md): 43/511/175/91/6/6/0/0, SHA-256 `b177b34780fabc75ea8b4608a0d210167a81d414d2778cc1d1dc5c0e39c8fea4`; Create, independent Validate, Hibernate validate, exact Drop and final zero-orphan Inventory. This is dated proof, not a new database operation. | [x] |
| WI-067 final backend/frontend quality gates | [WI-067 Evidence Pack](../../deliverables/agent/WI-20260809-ATS-067-evidence-pack.md): backend 1,542 tests and frontend 827 tests with zero failures; coverage/build/static gates passed | [x] |
| Target-production approval and acceptance not evidenced by the dated record above | OPEN in [SR-93](../SR/SR-93.md#remaining-production-gates); retained migration is conditional on the chosen data strategy | [ ] |

The [2026-09-05 local evidence update](../SR/SR-93.md#2026-09-05-local-verification)
separates initial and final MA quality results: final JUnit 1,689 total / 1,670
executed / 19 skipped with zero failures/errors; frontend 112 files / 1,458
tests, coverage/static/build gates PASS. It also records rebuilt-backend
startup, storage access and canonical-origin browser results. Only
DB Preflight was run through the bootstrap helper; no new DB or restore was
performed. Natural visible-list next-track progression passed; repeat-all at
the visible-list end paused under the existing policy, not a wrap PASS. Queue
repeat outside the visible list passed after navigating Home. The WI-004
source-patch full rerun and [WI-002 runtime evidence](../../deliverables/agent/WI-20260905-ATS-002-evidence-pack.md)
are complete. On 2026-09-05, MA reported document validation PASS (665 IDs,
links and index) and `git diff --check` PASS. MA verified the owned backend
30612 and frontend 28724 stopped and their ports released. Only scoped staging
and commit remain MA-owned for this local closeout; these results do not
complete payment acceptance or production gates.

## 13. Final Acceptance Gate

| Check | Expected Result | Done |
| :-- | :-- | :-- |
| User subscription flows passed. | New subscription, re-registration, upgrade, downgrade, cancel, reactivate, and renewal scenarios are accepted. | [ ] |
| Admin operation flows passed. | Incidents, receipts, audits, refund, correction, and settlement operations are accepted. | [ ] |
| Payment-integrity evidence is linked. | Technical proof points to the closure report and WI evidence; the client is not asked to inspect code, transactions, or database internals. | [ ] |
| Production boundary is understood. | Passing this checklist does not close retained-DB migration, live Toss, production deployment, or overall production readiness. | [ ] |
| Deferred scope is understood. | Tax invoice workflow is on hold under the current card-only recurring subscription scope. Toss Settlement API adapter, webhook, multi-PG, and cash receipt mutation are not treated as current defects. | [ ] |

## Related Documents

### Required References

- [User Flows](user-flows.md): User-facing flow definitions.
- [Admin Operations Guide](admin-operations-guide.md): Admin operation flow definitions.

### Reference Documents

- [Original Final Acceptance Checklist](../../deliverables/user/PAYMENT-FINAL-ACCEPTANCE-CHECKLIST-20260525.md): Historical acceptance source.
- [Known Limits and Next Steps](known-limits-and-next-steps.md): Deferred scope list.
- [P1 Payment Integrity Closure](../audit/p1-payment-integrity-closure-20260715.md): Current technical closure and remaining gates.

---
id: WI-20260716-ATS-030
req: REQ-20260716-ATS-002
agent: qa-integ
date: 2026-07-16
decision: CHANGES_REQUIRED
mode: read-only-integration-review
---

# WI-20260716-ATS-030 Evidence Pack

## 1. Findings First

### [P1] F-025-03 - Agreement before-state is not a revision fence

**Evidence**

- The correction entity persists only agreement status as before/after state:
  `src/main/java/com/atstudio/atstudio/entity/PaymentEntitlementCorrection.java:129-135`.
- Creation captures only `agreement.getStatus()`:
  `src/main/java/com/atstudio/atstudio/service/AdminPaymentEntitlementCorrectionService.java:135-158`.
- Execution accepts the agreement whenever its current status equals the captured enum:
  `src/main/java/com/atstudio/atstudio/service/AdminPaymentEntitlementCorrectionService.java:209-217,346-350`.
- `BillingAgreement.activate()` and `storeIssuedKey()` replace ciphertext, fingerprint, payment
  method, masked method, dates, and cleanup state:
  `src/main/java/com/atstudio/atstudio/entity/BillingAgreement.java:118-176`.
- Active-subscription registration uses `PaymentPurpose.BILLING_AGREEMENT`, resets the existing
  agreement for registration, stores the issued key, and finalizes it back to `ACTIVE`:
  `src/main/java/com/atstudio/atstudio/service/BillingAgreementApplicationService.java:108-156,222-231,354-368`;
  `src/main/java/com/atstudio/atstudio/service/PaymentCommandTransactionService.java:355-372,753-770`.
- The correction pending-outcome fence includes only `SUBSCRIBE`, `UPGRADE`, and `RENEWAL`, not
  `BILLING_AGREEMENT`:
  `src/main/java/com/atstudio/atstudio/service/AdminPaymentEntitlementCorrectionService.java:56-63,360-365`.
- The new agreement-drift test changes status from `ACTIVE` to `CANCELLED`; there is no same-status
  field-replacement case:
  `src/test/java/com/atstudio/atstudio/service/AdminPaymentEntitlementCorrectionServiceTest.java:276-298`.

**Source-reasonable interleaving**

1. Correction C is created while agreement A is `ACTIVE`; C records only `ACTIVE`.
2. The user completes payment-method re-registration. A transitions through `READY`, receives a new
   billing key/fingerprint/method, and returns to `ACTIVE`.
3. No Provider charge order is pending because the registration order has completed and its purpose
   is `BILLING_AGREEMENT` in any event.
4. C executes. Subscription fields and agreement status still match the old snapshot, so the stale
   fence passes and C cancels the newly registered agreement at
   `AdminPaymentEntitlementCorrectionService.java:237-244`.

This violates the WI-028 claim that execution compares the current agreement against its recorded
before-state. Locking prevents simultaneous writes inside the execute transaction, but it does not
detect completed intervening writes.

**Disposition:** `REOPENED`.

### [P2] F-025-05 - Retained colon-labelled Provider IDs bypass sanitization

**Evidence**

- The sanitizer pattern accepts only an equals separator:
  `src/main/java/com/atstudio/atstudio/service/payment/ProviderSupportReference.java:14-16`.
- Unmatched text is appended unchanged:
  `src/main/java/com/atstudio/atstudio/service/payment/ProviderSupportReference.java:39-53`.
- ADMIN audit and Incident DTOs serialize retained free text after this sanitizer:
  `src/main/java/com/atstudio/atstudio/dto/payment/AdminPaymentOperationAuditLogResponse.java:49-53`;
  `src/main/java/com/atstudio/atstudio/dto/payment/AdminPaymentReconciliationIncidentResponse.java:62-70`.
- Policy requires labelled retained Provider identifiers to be replaced before serialization:
  `docs/policies/security-policy.md:231-244`.
- Tests use only `transactionId=...` and `paymentKey=...`:
  `src/test/java/com/atstudio/atstudio/dto/payment/ProviderSupportReferenceTest.java:37-47`;
  `src/test/java/com/atstudio/atstudio/dto/payment/AdminProviderIdentifierContractTest.java:168-203`.

Static reproduction: `sanitizeFreeText("transactionId: pay_0123456789_abcdef")` has no regex match
and returns the raw string. This is a response-boundary privacy leak for retained audit/Incident data.

**Disposition:** `REOPENED`.

### [P3] F-027-03 - Documentation names the wrong expiry field

- Correct implementation wording: `frontend/src/router/SubscriberRoute.tsx:12-15`.
- Stale field name: `docs/standards/glossary.md:93` and
  `docs/design/usecase/sound-track.md:208-216`.
- Implemented wire field: `frontend/src/api/userSubscriptions.ts:10-20` (`expiresAt`).

**Disposition:** `REOPENED`.

## 2. Work Item And Boundaries

| Field | Value |
|---|---|
| WI | `WI-20260716-ATS-030` |
| Depends on | `WI-20260716-ATS-028`, `WI-20260716-ATS-029` |
| Blocks | `WI-20260716-ATS-031` |
| Branch verified | `codex/p1-acceptance-hardening` |
| Review mode | Read-only integration review |
| Product/doc writes | None |
| Allowed writes | This evidence pack and the paired user summary only |
| Decision | `CHANGES_REQUIRED` |

The review obeyed `deliverables/agent/WI-20260716-ATS-030-handoff.md:1-64`. No product source,
product documentation, generated output, Git index, commit, branch, remote, retained/live database,
Provider state, client worktree, or runtime was mutated.

## 3. Complete Finding Disposition

| Finding | Disposition | Exact evidence and rationale |
|---|---|---|
| F-025-01 | `ENVIRONMENT-CONDITIONAL` | Withdrawal locks/fences at `UserService.java:135-182`; renewal claim and Provider/finalization fences at `PaymentCommandTransactionService.java:231-400,676-729` and `RecurringRenewalService.java:170-215`. Retained MySQL proof is gated by `ATSTUDIO_MYSQL_PROOF_ENABLED=true` at `PaymentMysqlConcurrencyIntegrationTest.java:100-119`; its seven tests at lines 230-594 do not include withdrawal/correction interleavings. |
| F-025-02 | `CLOSED` | ADMIN GET is `NOT_SUPPORTED` and calls diagnostic paths only at `AdminPaymentReadService.java:74-82`. Provider diagnosis uses `Propagation.NEVER` at `PaymentReconciliationService.java:161-182`; scheduled recovery remains the separate mutating entry point at `PaymentReconciliationService.java:81-85`. API contract is explicit at `docs/design/api-spec.md:2198-2209`. |
| F-025-03 | `REOPENED` | Status-only agreement snapshot and same-status re-registration interleaving described in Finding 1. |
| F-025-04 | `CLOSED` | Production clock comes from `PaymentProperties.schedulerZoneId()` at `RecurringRenewalService.java:48-83`; scheduled zone is configured at `src/main/resources/application.yml:114` and used by renewal/expiration/reconciliation schedulers. WI-028 reports zone-focused tests passing at `WI-20260716-ATS-028-evidence-pack.md:119-131,148-202`. |
| F-025-05 | `REOPENED` | Equals-only free-text pattern leaves colon-labelled Provider IDs raw, as described in Finding 2. |
| F-026-01 | `CLOSED` | ADMIN whitelist list owns success/failure/loading by generation at `WhitelistChannelManagePage.tsx:70-109`; certification list applies the same rule at `CompanyCertManagePage.tsx:100-129`. Focused reverse-order/stale-failure tests are identified at `WI-20260716-ATS-029-evidence-pack.md:97-103`. |
| F-026-02 | `CLOSED` | `loadQueued` is set before the active-load guard and the loop drains queued work at `WhitelistChannelPage.tsx:80-125`; focused overlapping-mutation test pointer is `WI-20260716-ATS-029-evidence-pack.md:104-108`. |
| F-026-03 | `CLOSED` | Detail opens explicitly, request generations own completion, and close invalidates/clears loading at `CompanyCertManagePage.tsx:86-166`; modal authority is `detailOpen` at line 316. |
| F-027-01 | `CLOSED` | CORS exposes `Content-Disposition` and `X-Whitelist-Export-Batch-Id` at `CorsConfig.java:41-48`; adapter validates positive safe integers, replay fallback, and mismatch at `frontend/src/api/admin.ts:203-239`; contract text is `docs/design/api-spec.md:3279-3289`. Separate-origin deployed smoke remains a residual environment gate. |
| F-027-02 | `CLOSED` | Certification envelope examples and generic binary media wording are aligned at `docs/design/api-spec.md:3325-3502`; focused controller contract evidence is recorded at `WI-20260716-ATS-029-evidence-pack.md:123-130`. |
| F-027-03 | `REOPENED` | Correct service-enabled semantics use a non-existent `currentPeriodEnd` documentation identifier instead of implemented `expiresAt`. |
| F-027-04 | `CLOSED` | ADMIN diagnostic contract includes `localCurrency` and `providerCurrency` at `docs/design/api-spec.md:2204-2209`; WI-028 records the corresponding operations-runbook update at `WI-20260716-ATS-028-evidence-pack.md:85-89,136-142`. |
| F-027-05 | `ENVIRONMENT-CONDITIONAL` | `git diff --numstat -- frontend/tsconfig.tsbuildinfo` returned `1 1`; worktree blob is `6be701894fbe754e13e596499503ec3e8aa98c50`, HEAD blob is `3c8b761d34328de7e52933adbfa3944603c94a32`. WI-029 requires allowlist exclusion at `WI-20260716-ATS-029-summary.md:62-69`, but staging was forbidden, so index closure cannot yet be proved. |

Disposition totals: 8 `CLOSED`, 3 `REOPENED`, 2 `ENVIRONMENT-CONDITIONAL`.

## 4. Product Invariants

| Invariant | Current evidence | Result |
|---|---|---|
| Public full-track listening | Player source remains `/tracks/{id}/stream` at `frontend/src/store/playerStore.ts:241`; integrated remediation did not alter the policy. | Preserved |
| Subscriber-only downloads | Download remains the separate authenticated service path at `DownloadService.java:41-64` and frontend download action at `PlayerBar.tsx:175-199`. | Preserved |
| Recurring billing | Subscription payment remains the billing-key flow; one-time subscription scope remains blocked at `docs/design/payment-integration-design.md:124-125,198`. | Preserved |
| Single-server deployment | No distributed scheduler lock is introduced; current policy remains explicit at `docs/design/payment-integration-design.md:758,795`. | Preserved |

## 5. Schema And Client Isolation

### Schema

- WI-028 explicitly records no schema/migration/entity-field addition at
  `deliverables/agent/WI-20260716-ATS-028-evidence-pack.md:204-220`.
- WI-029's changed-file inventory at
  `deliverables/agent/WI-20260716-ATS-029-evidence-pack.md:221-240` contains frontend, CORS,
  test, and documentation paths only.
- The shared worktree already contains cumulative modifications including `schema.sql` and entities.
  This review does not attribute those unrelated/concurrent changes to WI-028/WI-029 and makes no
  clean-tree assertion.

### Client isolation

- No client worktree path or runtime command was issued during WI-030.
- WI-028 states no client worktree/runtime mutation at
  `WI-20260716-ATS-028-evidence-pack.md:30-43`.
- WI-029 records the same boundary at `WI-20260716-ATS-029-evidence-pack.md:13-36`.
- Independent client inspection was intentionally not performed because the WI-030 handoff forbids
  touching that worktree/runtime. Isolation for WI-030 is directly verified; prior-WI isolation is
  evidence-based.

## 6. Automated Gate Evidence

### Backend

WI-028 evidence records:

- 156 focused tests, zero failures/errors/skips:
  `WI-20260716-ATS-028-evidence-pack.md:148-162`.
- The sliced renewal context root cause and test-only `PaymentProperties` repair:
  `WI-20260716-ATS-028-evidence-pack.md:177-202`.
- Class-only rerun: 6/6 pass; related rerun: 48/48 pass.

At review start, the MA-supplied full-backend XML set was inspected before concurrent cleanup of
ignored generated output. It contained 154 suites, 1,125 tests, 6 failures, 0 errors, and 9 skipped.
All six failures were the earlier missing-`PaymentProperties` ApplicationContext root cause in
`build/test-results/test/TEST-com.atstudio.atstudio.service.RecurringRenewalCommandIntegrationTest.xml:2,28`.
Current source includes the repair at
`src/test/java/com/atstudio/atstudio/service/RecurringRenewalCommandIntegrationTest.java:42-64`, and
the focused reruns pass, so the old XML is not evidence of a current source defect. It is also not a
post-fix full-suite pass. The complete backend gate remains **OPEN / NOT CURRENT**.

### Frontend

WI-029 evidence records:

- Focused Vitest: 4 files, 25 tests passed.
- Typecheck, targeted ESLint, targeted Prettier/full format, focused CORS test passed.
- Exact pointers: `WI-20260716-ATS-029-evidence-pack.md:152-216`.

The MA-supplied Vitest cache inspected at review start listed 44 test files and marked all as not
failed. The ignored cache and `frontend/dist` were subsequently absent after concurrent activity;
WI-030 did not delete them. The observed production build artifact also predated the final WI-029
source/test updates, so a post-integration frontend production build is not proved. Frontend focused
behavior is **PASS**; current production build evidence is **OPEN**.

### WI-030 execution constraint

No Gradle, npm, Vitest, ESLint, Prettier, typecheck, build, or docs-validation command was run by
WI-030. Those commands generate files outside the explicit two-file write allowlist. This is a
constraint-driven evidence gap, not a claimed pass.

## 7. Reviewed Commands

Read-only inspection included:

```powershell
git branch --show-current
git status --short
git diff -- <target paths>
git diff --numstat -- frontend/tsconfig.tsbuildinfo
git hash-object frontend/tsconfig.tsbuildinfo
git rev-parse HEAD:frontend/tsconfig.tsbuildinfo
rg --files <scoped paths>
rg -n <scoped patterns> <scoped paths>
Get-Content -LiteralPath <pointer>
Select-String -LiteralPath <pointer> -Pattern <pattern>
Get-FileHash -Algorithm SHA256 frontend/tsconfig.tsbuildinfo
```

No command staged, committed, pushed, restarted, deleted, restored, or mutated application state.

## 8. Smallest Follow-up WI Scope

1. **F-025-03:** use an existing no-schema revision signal, or another approved snapshot mechanism,
   to reject agreement mutations completed after correction creation. Add a regression test that
   completes `ACTIVE -> billing-agreement re-registration -> ACTIVE` between create and execute and
   proves execution rejects without cancelling the new agreement.
2. **F-025-05:** expand retained free-text sanitization to colon-labelled/equivalent Provider ID
   forms and add audit/Incident DTO sentinels for full and partial raw identifiers.
3. **F-027-03:** replace `currentPeriodEnd` with `expiresAt` in the glossary and track use case.
4. Rerun the complete backend suite and complete frontend test/typecheck/lint/build gates. At the
   eventual commit gate, use an explicit path allowlist and prove the staged diff excludes
   `frontend/tsconfig.tsbuildinfo`.

No schema, Provider, retained DB, client worktree, runtime, or architecture change is required for
this follow-up. Retained MySQL interleavings and deployed separate-origin smoke remain separately
approved environment gates.

## 9. Rollback

WI-030 changed only these two deliverables. Rollback is limited to removing or reverting:

- `deliverables/user/WI-20260716-ATS-030-summary.md`
- `deliverables/agent/WI-20260716-ATS-030-evidence-pack.md`

No product, schema, data, Provider, runtime, Git-index, or client rollback is required.

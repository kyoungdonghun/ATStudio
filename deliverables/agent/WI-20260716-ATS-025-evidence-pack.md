---
version: 1.0
last_updated: 2026-07-16
project: ATS
owner: cr
category: evidence-pack
status: findings-recorded
related_wi: WI-20260716-ATS-025
dependencies:
  - path: ../user/REQ-20260716-ATS-002.md
    reason: Approved acceptance-hardening scope
  - path: WI-20260716-ATS-025-handoff.md
    reason: Read-only review execution contract
  - path: ../user/WI-20260716-ATS-022-summary.md
    reason: Supplied integration verification baseline
---

# Evidence Pack: WI-20260716-ATS-025

## Summary

Completed an independent findings-first, read-only audit of the cumulative uncommitted backend and security diff on `codex/p1-acceptance-hardening`. Result: P0: 0, P1: 3, P2: 2. No application, test, runtime, DB, Provider, client, or Git state was mutated.

## Scope / DoD Check

- [x] Read `WI-20260716-ATS-025-handoff.md` and every listed input pointer.
- [x] Reconciled the changed backend against the four approved product invariants.
- [x] Reviewed auth/authz/rate limits, OAuth, billing-key crypto, payment identifiers/receipts, reconciliation, withdrawal, entitlement correction, whitelist, certification, storage, download, album/playlist, schema, and configuration.
- [x] Checked transaction boundaries, lock order, Provider-call boundaries, idempotency, stale state, exception/log safety, and PII/secret exposure.
- [x] Ranked actionable findings with reachable paths and tight file/line evidence.
- [x] Separated code findings from environment and test-evidence limits.
- [x] Created only the two output-contract deliverables.

## Reference Documents

| Tier | Document | Review use |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Financial traceability, privacy, and product invariants |
| 0 | `docs/standards/development-standards.md` | Service/transaction/lock/DTO/test standards |
| 1 | `docs/policies/security-policy.md` | Role, file, secret, receipt, and Provider-reference boundaries |
| 1 | `docs/policies/quality-gates.md` | Evidence and environment classification |
| 2 | `docs/design/remaining-remediation-design-20260716.md` | Intended closure state and preserved invariants |
| 2 | `docs/design/api-spec.md` | ADMIN read-only, withdrawal, whitelist, and role contracts |
| 2 | `docs/design/db-schema.md` | Fresh/retained schema and lock assumptions |
| 2 | `docs/design/payment-integration-design.md` | Recurring payment, Provider boundary, and reconciliation design |
| Context | `deliverables/user/REQ-20260716-ATS-002.md` | Approved remediation scope |
| Context | `deliverables/user/WI-20260716-ATS-017-summary.md` | Claimed finding closure and residuals |
| Context | `deliverables/user/WI-20260716-ATS-022-summary.md` | Supplied integration judgment and test result |

## Branch and Diff Scope

| Item | Evidence |
|---|---|
| Worktree | `C:/Users/jm991/Desktop/project/ATStudio` |
| Branch | `codex/p1-acceptance-hardening` |
| HEAD | `cd876fcf84b3cb2490c27420c6c53a87a35b982d` |
| Tracked review diff | 84 files, 4,099 insertions, 544 deletions under the handoff's backend/config/test pathspec |
| Untracked scope | Included relevant new Java DTO/config/entity/repository/test/manual-SQL files identified by `git status` |
| Excluded | Frontend/client worktree behavior, live runtime, DB/data, Provider calls, secrets, staging, commits, and remotes |

## Severity Summary

| Severity | Count | IDs |
|---|---:|---|
| P0 Critical | 0 | None |
| P1 High | 3 | F-025-01, F-025-02, F-025-03 |
| P2 Medium | 2 | F-025-04, F-025-05 |

## Findings

### F-025-01 - P1 High - Withdrawal does not fence an escaped renewal claim

**Class:** financial race, cancellation fence failure, and deleted-account state restoration.

**Reachable interleaving:**

1. `claimRenewal()` locks agreement then subscription, sees a non-deleted user, marks/returns a Provider claim, and commits.
2. `withdraw()` locks only the user row, uses unlocked agreement/subscription reads, cancels them, publishes cleanup, and marks the user deleted.
3. `RecurringRenewalService` decrypts the claim and calls the Provider without another persisted-state check.
4. Provider success is recorded, and `finalizeRenewal()` accepts the relationship/status evidence without rejecting deletion/cancellation; it starts a new subscription period and marks the agreement ACTIVE.

**Evidence:**

- `src/main/java/com/atstudio/atstudio/service/UserService.java:124-162`: user lock only; agreement/subscription cancellation through unlocked reads.
- `src/main/java/com/atstudio/atstudio/service/PaymentCommandTransactionService.java:234-258,327-336`: deletion check occurs only while creating the claim; ciphertext escapes the transaction.
- `src/main/java/com/atstudio/atstudio/service/RecurringRenewalService.java:102-109,147-158`: claim and Provider call are separate; no cancellation/deletion recheck.
- `src/main/java/com/atstudio/atstudio/service/PaymentCommandTransactionService.java:358-379,650-701`: Provider success and finalization do not reject deleted/cancelled state; finalization restores subscription/agreement state.
- `docs/design/api-spec.md:3168-3171`: contract requires local-first stop and deleted-user exclusion before charge.
- `src/test/java/com/atstudio/atstudio/service/UserServiceTest.java:67-123` and `RecurringRenewalServiceTest.java:91-107`: isolated happy paths only; no cross-service interleaving.

**Required implementation boundary:** use one agreement-first fence across renewal, withdrawal, Provider-success application, and cleanup. A claimed renewal must be durably completed/reconciled or explicitly deferred before withdrawal is finalized; no raw timing window may authorize a post-withdrawal charge.

**Verification:** deterministic concurrency harness plus retained-MySQL interleavings; assert zero post-fence Provider calls, no deleted-user entitlement restoration, and convergent cleanup after every ordering.

### F-025-02 - P1 High - Read-only reconciliation GET invokes recovery mutations

**Class:** HTTP/contract violation and unexpected financial state mutation.

**Reachable path:** ADMIN GET -> `AdminPaymentReadService.reconcilePayments()` -> `reconcileProviderLedger()` -> record Incident -> apply Provider success -> purpose-specific finalizer -> resolve Incident.

**Evidence:**

- `src/main/java/com/atstudio/atstudio/controller/AdminPaymentController.java:253-256`: GET route.
- `src/main/java/com/atstudio/atstudio/service/AdminPaymentReadService.java:73-80`: GET read service calls both reconciliation paths.
- `src/main/java/com/atstudio/atstudio/service/PaymentReconciliationService.java:339-370,499-514`: Incident persistence and payment/subscription finalizer dispatch.
- `src/main/java/com/atstudio/atstudio/service/PaymentReconciliationTransactionService.java:223-247`: Provider-success application is transactional mutation.
- `src/test/java/com/atstudio/atstudio/service/PaymentReconciliationServiceTest.java:146-180`: test explicitly expects the mutations.
- `docs/design/api-spec.md:1592-1621,2198-2204`: GET is diagnostic-only and must not persist/finalize.

**Required implementation boundary:** separate a pure diagnostic scan from an explicitly mutating recovery command/scheduler. Do not hide a mutation flag inside the GET call chain.

**Verification:** controller-to-service integration test with spies/fakes proving zero calls to Incident/audit repositories, Provider-success application, or finalizers; separate recovery tests retain exact-evidence safeguards.

### F-025-03 - P1 High - Entitlement correction violates payment lock order and does not fence agreement state

**Class:** financial deadlock/lost-update race and stale command.

**Reachable interleaving:** correction holds the subscription lock and later writes an unlocked agreement while renewal/finalization holds the agreement lock and then requests the subscription lock. This is a direct lock-order cycle. Separately, a renewal claim can commit before correction cancels the agreement, allowing a Provider charge to continue after the refund-linked correction.

**Evidence:**

- `src/main/java/com/atstudio/atstudio/service/AdminPaymentEntitlementCorrectionService.java:105-145`: subscription-first lock, unlocked agreement snapshot.
- `src/main/java/com/atstudio/atstudio/service/AdminPaymentEntitlementCorrectionService.java:184-235`: subscription-only stale check, then unlocked agreement mutation.
- `src/main/java/com/atstudio/atstudio/repository/BillingAgreementRepository.java:44-45,67-75`: unlocked finder used despite available pessimistic lock method.
- `src/main/java/com/atstudio/atstudio/service/PaymentCommandTransactionService.java:238-252,650-660` and `PaymentReconciliationTransactionService.java:144-150`: canonical payment paths acquire agreement before subscription.
- `src/main/java/com/atstudio/atstudio/entity/BillingAgreement.java:30-64`: no optimistic `@Version` fallback.
- `src/test/java/com/atstudio/atstudio/service/AdminPaymentEntitlementCorrectionServiceTest.java:195-260`: covers subscription drift only and stubs the unlocked agreement read.

**Required implementation boundary:** lock agreement before subscription, include agreement state in execute-time stale validation, and refuse/defer execution when a non-terminal order can still produce a Provider outcome.

**Verification:** retained-MySQL deadlock/lost-update tests and a provider-boundary test where a renewal claim competes with correction execution.

### F-025-04 - P2 Medium - Scheduler zone and business date can diverge

**Class:** environment-dependent payment/date correctness defect.

**Evidence:**

- `src/main/resources/application.yml:112-114` and `PaymentProperties.java:14-22`: configurable zone exists.
- `src/main/java/com/atstudio/atstudio/service/SubscriptionScheduler.java:32-34,58-62`: cron uses configured zone, expiry date uses JVM-default `LocalDate.now()`.
- `src/main/java/com/atstudio/atstudio/service/RecurringRenewalService.java:58-64`: no-argument entry also uses JVM-default date.
- `src/test/java/com/atstudio/atstudio/service/SubscriptionSchedulerTest.java:43-70,96-143`: annotation and same-default-zone behavior only.

**Impact:** on UTC JVMs, the default Seoul midnight/00:30 runs can process the previous date and delay renewal/expiry by one day.

**Required implementation boundary:** inject a configured `Clock`/`ZoneId` and derive every date/time used by payment jobs from it.

**Verification:** force JVM UTC while configuring Seoul; test immediately before/after Seoul midnight and a second configured zone.

### F-025-05 - P2 Medium - Audit free text contains raw Provider ID fragments

**Class:** payment support-reference privacy contract violation.

**Evidence:**

- `src/main/java/com/atstudio/atstudio/service/PaymentReconciliationIncidentService.java:210-229,311-340`: stores first four and last four raw identifier characters in structured Incident evidence and audit note.
- `src/main/java/com/atstudio/atstudio/dto/payment/AdminPaymentOperationAuditLogResponse.java:23-28,47-51`: structured field becomes `REF-*`, but `note` is returned verbatim.
- `docs/policies/security-policy.md:231-239`: only deterministic support references are allowed; free text cannot be a fallback.
- `src/test/java/com/atstudio/atstudio/service/PaymentReconciliationIncidentServiceTest.java:99-143`: assertion rejects only the complete raw string, not fragments.

**Required implementation boundary:** centralize support-reference generation outside DTO-only code and remove raw fragments from persisted/free-text evidence.

**Verification:** persistence and serialization sentinel tests for full value, prefixes, suffixes, and short identifiers.

## Reviewed Surfaces Without Additional Findings

| Area | Locally observed protection | Remaining classification |
|---|---|---|
| Auth/authz | ADMIN routes and USER-only payment/certification boundaries; BUSINESS service qualification | No concrete bypass found |
| Rate limits | Endpoint-specific process-local windows, trusted client resolver, salted fingerprints, bounded cleanup | Single-server/process-local boundary accepted |
| OAuth | Typed Provider DTOs, mandatory fields, internal return target rules, safe exceptions | Live Provider payloads remain conditional |
| Billing crypto | AES-GCM v2 key-ID/AAD envelope, legacy read path, startup fail-closed validation | Live key rotation remains conditional |
| Receipt/log safety | HTTPS URL policy and bounded exception-class logging | Live Provider/host evidence remains conditional |
| Whitelist | HTTPS YouTube validation, user/channel locks, bounded immutable export, CSV neutralization | Retained-row and MySQL proof remain conditional |
| Certification | USER+BUSINESS self-service, private storage, canonical images, PDF/signature checks, audit, DTO path omission | Malware/retention policy remains outside scope |
| Storage | Durable PREPARED journal, commit/rollback callbacks, reference-aware retry recovery | Long-transaction and real filesystem proof remain conditional |
| Downloads | User lock, entitlement/quota/license checks, unique license contract, atomic count | Retained unique-index application remains conditional |
| Album/playlist | Parent locks cover update/delete/membership/reorder; exact reorder membership checks | Real MySQL race proof remains conditional |
| Schema/config | Fresh schema and source-only retained patches align for reviewed additions | DDL, duplicate prechecks, and EXPLAIN not executed |

## Commands and Outputs

| Command/evidence action | Result |
|---|---|
| `git branch --show-current` | `codex/p1-acceptance-hardening` |
| `git rev-parse --short HEAD` | `cd876fc` |
| `git status --short --branch` | Large concurrent dirty tree preserved; no cleanup or mutation |
| `git diff -- src/main src/test build.gradle application-local.example.yml` | Reviewed cumulative tracked diff and relevant untracked files from status |
| `git diff --stat -- ...` | 84 tracked files; 4,099 insertions and 544 deletions |
| `git diff --check -- ...` | Clean after excluding line-ending conversion warnings |
| Line-numbered PowerShell reads and focused `rg -n -C` searches | Confirmed call chains, contracts, locks, DTO exposure, and test coverage |
| Manual SQL/source schema inspection | No DDL executed; fresh and retained paths compared statically |

## Tests and Evidence Limits

No Gradle build or test task was run by WI-025. The user restricted file creation/update to the two WI-025 deliverables, while Gradle would write build outputs. WI-022's supplied result is therefore recorded but not re-executed: 1,106 backend tests, zero failures, nine skipped (`deliverables/user/WI-20260716-ATS-022-summary.md:17-35`).

Required follow-up verification:

1. Focused unit/integration tests for diagnostic-only reconciliation and support-reference notes.
2. Deterministic concurrency tests for withdrawal/correction versus renewal Provider claims.
3. Retained-MySQL lock/deadlock, migration, duplicate-precheck, and index-plan validation.
4. Scheduler tests under a JVM zone different from `app.payment.scheduler-zone`.
5. Full backend build/test/coverage and all existing acceptance gates after fixes.

## Risks / Proposed-Fix Rollback

- **Withdrawal/renewal fence:** implement and deploy as one bounded state-machine/lock-order change with focused tests. If rollback is required, revert that future change as a unit before enabling live renewal; do not leave half of the fence active.
- **Reconciliation split:** retain the existing mutating recovery service behind scheduler/explicit-command wiring while introducing a separate read-only diagnostic service. Roll back only new route wiring if needed; never route GET back to a mutating implementation in an acceptance environment.
- **Correction lock/fence:** keep the change code-only if possible. Roll back the future service/repository/test commit together; no schema rollback should be necessary.
- **Scheduler clock:** inject through one configuration point. Roll back the future clock wiring and tests together if configuration parsing fails; keep the prior zone value documented during rollback.
- **Support references:** changing notes to `REF-*` needs no schema migration. Roll back only display wording if necessary, not the raw-fragment removal.

This review itself made no product change, so it has no product rollback action.

## Follow-up Chain

WI-025 blocks WI-028. Before WI-028 acceptance judgment, fix and independently verify F-025-01 through F-025-05, then rerun the complete backend/security quality gates without mutating the frozen client worktree or live Provider/DB state unless separately approved.

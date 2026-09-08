---
version: 1.2
last_updated: 2026-09-09
project: ATS
owner: qa-integ
category: evidence-pack
status: active
---

# Evidence Pack: WI-20260909-ATS-011

> Current disposition, 2026-09-09 executed-evidence update: F1 CLOSED for the bounded local correction after independent source re-review and inspection of MA's passing H2/fake-provider results (new regression 10/10; selected related classes 132/132). No new payment blockers found. The full backend build remains FAILED on unrelated multipart fixtures, and full-build/coverage acceptance is not granted. The original finding and earlier checkpoints below are preserved; the final dated evidence update supplies the current disposition.

## Summary

Independent, product-read-only PAY-01/02 review completed against WI002's four product files and five test files. The prospective returning-purchase and competing-upgrade protections have concrete source/test support. One P1 residual interaction remains: an overlapping unlocked local self-cancellation can overwrite an already-finalized upgrade with its stale subscription state. This is a source-grounded counterexample, NOT an executed failure. Payment acceptance should remain open for MA's focused reproduction/disposition.

## Scope / DoD Check

- [x] Read the assigned WI011 handoff before product inspection; confirmed approved REQ and HEAD `8161f0a`.
- [x] Independently inspected WI002's manifest, concrete product diff, five test files, and directly interacting payment paths.
- [x] Reviewed expired/active histories, source replacement, UNKNOWN, durable success, source/price checks, locks, recovery identity, DONE replay, and old-format rollout boundaries.
- [x] Used create-wi-evidence-pack after verifying the existing handoff; authored only this pack and the assigned user summary.
- [x] Preserved product code, policies, unrelated dirty files, historical data and reports. No subdelegation or Git writes.
- [x] Separated MA's existing test evidence from this reviewer's static inspection and unexecuted scenarios.
- [ ] Residual F1 reproduced/disposed by MA and implementation owner; no unconditional payment closure here.

## Reference Documents (Tier 0-2)

| Tier | Document | Use |
| --- | --- | --- |
| Entry | AGENTS.md | Assigned ownership, no subdelegation, two-set deliverables |
| 0 | docs/standards/core-principles.md | Approved autonomy, traceable financial state, preserved policies/data |
| 0 | docs/standards/development-standards.md | Java 17, service transactions, independent focused validation |
| 0 | docs/standards/documentation-standards.md | English technical reports, metadata and evidence boundaries |
| 0 | docs/standards/glossary.md | Subscription and local correction terminology |
| 1 | docs/policies/security-policy.md | No secrets, real Provider, mail, DB or runtime actions |
| 1 | docs/policies/quality-gates.md | Scope-specific review and explicit validation limitations |
| 2 | deliverables/user/REQ-20260909-ATS-001.md | Approved PAY-01/02 remediation; no new policy or architecture |
| 2 | deliverables/agent/WI-20260909-ATS-011-handoff.md | Assignee qa-integ, owned reports, dependencies |
| 2 | deliverables/agent/WI-20260909-ATS-002-evidence-pack.md | Implementation manifest, test readiness and rollout claims to verify |
| 2 | deliverables/agent/WI-20260908-ATS-018-findings.md:31 | Original PAY-01/02 defects and required counterexamples |
| Skill | .agents/skills/create-wi-evidence-pack/SKILL.md | Evidence structure and immediate two-set report generation |
| Config | .claude/config/workspace.json | ATS project tag |

Injected context: user's Tier-0 summary and the handoff's pointers; live reads narrowed to the assigned review. No claim that every linked policy or the entire repository was reviewed. No additional context was delegated.

## Findings

### F1: P1 - Unlocked self-cancellation can restore the pre-upgrade tier after successful finalization

**Primary location:** `src/main/java/com/atstudio/atstudio/service/UserSubscriptionService.java:199-206`.

**Supporting locations:** `repository/UserSubscriptionRepository.java:28-33` (ordinary nonlocking query); `entity/UserSubscription.java:11-16,27-29,90-92` and `common/entity/BaseEntity.java:13-15` (no version or dynamic-update protection); `service/PaymentCommandTransactionService.java:628-648,666-668` (locked validation/finalization, then read-only DONE replay); `controller/UserSubscriptionController.java:62-64` (live caller).

Service/repository/entity paths in this paragraph are under `src/main/java/com/atstudio/atstudio/`.

This is an existing writer interacting with the changed PAY-02 finalizer, not an allegation that WI002 introduced the writer. The new source check protects against a change committed BEFORE finalization. It does not protect against an earlier nonlocking reader committing its stale entity AFTER finalization. `selfCancel` reads the active subscription without a pessimistic lock, changes its status, and commits a managed entity with no optimistic version. The ordinary entity update can write the old plan and period back as well as CANCELLED. The payment order and payment remain DONE, while service access uses the pre-upgrade tier. Existing DONE replay deliberately does not apply the upgrade again.

**Deterministic reproduction for MA, not executed here:**

1. Create a fake-provider/H2 fixture with active Basic, an active agreement and a future expiry. Claim a paid Premium upgrade and durably record fake Provider success, leaving the order PROVIDER_SUCCEEDED.
2. In independent transaction C, enter the actual `selfCancel` route and let `findActiveByUser` return Basic. Pause C before its flush/commit, for example at a thread-specific test spy on the subsequent `findByUserAndProvider` call. Do not run the provider-cancellation route, which is a different operation.
3. In transaction U, call `finalizeUpgrade` on the succeeded order and commit. The uncommitted in-memory cancellation is invisible; U validates the unchanged source and persists Premium plus exactly one DONE payment/order.
4. Release C and commit cancellation. Reload in a fresh persistence context. Assert that CANCELLED retains Premium and the paid period. The source-grounded failure expectation is CANCELLED with Basic restored, despite one DONE Premium upgrade payment.
5. Replay `finalizeUpgrade`; assert no new charge/payment and verify that this does not repair a tier already overwritten after DONE.

An equivalent two-TransactionTemplate entity-level reproducer may first isolate the persistence behavior; the acceptance regression should exercise the public `selfCancel` service path. Capture the executed update and transaction order before describing this as runtime-proven.

**Bounded corrective direction:** synchronize this local read-modify-write with the payment agreement/subscription lock protocol and read the subscription only after the appropriate locks. Preserve cancellation's current paid-access and no-refund semantics. No DDL, version column, new cancellation policy or Provider call is required by this finding. Audit the same local planning transaction's unlocked pending-change writes (`UserSubscriptionService.java:129-142,182-183`) only for this identical stale-write mechanism if fixing the shared entry point; this is not a separate broad audit request.

**Evidence boundary:** static call/transaction/entity inspection only; no SQL trace, H2 reproducer or MySQL interleaving was executed by WI011. Existing PAY tests mutate the source and COMMIT before finalization (`SubscriptionUpgradeCommandIntegrationTest.java:403-413`), so their PASS does not exercise this opposite commit order.

## Concrete Review Matrix

All Java paths below use the same source root. Test paths use `src/test/java/com/atstudio/atstudio/service/`.

| Check | Independent observation | Evidence / boundary |
| --- | --- | --- |
| Returning expired history | Prepare locks and links the retained row; SUBSCRIBE binds its source; finalization reuses that row and adds a new payment rather than dropping historical ledgers. | `BillingAgreementPrepareTransactionService.java:114-149`; `PaymentCommandTransactionService.java:533-606`; returning test `:59-79,232-249`. Tests cover expired dates with ACTIVE, CANCELLED and EXPIRED statuses. |
| Active versus expired | Service-enabled ACTIVE/CANCELLED routes to zero-charge registration; SUBSCRIBE guards reject active replacements and mismatched retained snapshots. Existing active-agreement restriction is preserved. | Prepare `:115-118,249-295,304-308`; command `:1349-1359,1439-1450`; returning test `:120-149`. No claim that an ACTIVE agreement is newly eligible for fresh checkout. |
| Same versus different source | ID, owner, plan, cycle, status, start/end and normalized source-plan prices are bound. Matching DB decimal scales compare equal; a genuinely different source fails closed. | `PaymentCommandKeyFactory.java:53-89`; command `:984-1007,1035-1044`; key test `:39-61`; upgrade test `:398-423`. This is a logical snapshot, not a monotonic generation counter. |
| Price and cycle | Persisted quote/charged cycle remain authoritative after durable success; finalization does not recalculate today's proration. Current YEARLY period and pending MONTHLY choice remain separate. | Claim `:102-114`; order `:843-845`; finalization `:641-668`; upgrade test `:362-395`. Source tariff changes are guarded. Target tariff is not part of the source hash; the persisted amount remains the accepted quote, not an automatic repricing feature. |
| Competing upgrades | Agreement then current-subscription locks precede locking unresolved-UPGRADE and family lookups. Different target/cycle keys are rejected while another intent is unresolved. | Command `:78-110`; `PaymentOrderRepository.java:77-108`. Unresolved states include READY, IN_PROGRESS, PROCESSING, PENDING_PROVIDER_CONFIRMATION, PROVIDER_SUCCEEDED, including older periods for this retained row. |
| Independent fake-provider concurrency | New test uses independent calls while the first Provider call is outside its local transaction. Transaction IDs derive from distinct order IDs, so uniqueness cannot hide a second accepted charge. | Upgrade test `:289-334`; support `:168-180,283-297`. Only observed MA H2 result, not MySQL lock proof. |
| UNKNOWN and durable success | UNKNOWN cannot be directly retried as a new Provider attempt. Same durable-success upgrade uses finalize-only; competing keys remain fenced. | Command `:103-155`; upgrade test `:337-395`; returning test `:82-117`. Returning/upgrade test recovery invokes transaction services directly; separate reconciliation integration tests exercise the orchestration route. |
| Recovery lookup identity | Exact legacy key OR escaped literal base-plus-SOURCE lookup is user-scoped on reads, capped at two and rejects ambiguity explicitly. Normal writers serialize by agreement. | Repository `:46-61,77-97`; `PaymentRecoveryReadService.java:43-75`; returning test `:70-72,180-203`. No single-result query or arbitrary candidate is used. A manually introduced second family member is an integrity error, not permission to create another order. |
| Changed period recovery | User upgrade recovery derives the current active period; after period replacement it may no longer find the old order by that endpoint. Old unresolved orders still fence competitors; reconciliation works from order identity and refuses a mismatched source. | Recovery read `:43-61`; command `:97-105,1311-1327`; `PaymentReconciliationService.java:560-616,750-762`. This is retained/manual-review behavior, not a promised automatic old-period recovery UX. |
| DONE replay | Monetary finalizers return without changing a later lifecycle or charging again. Upgrade DONE skips source matching; existing ledger ownership checks still execute. | Command `:538-542,646-648,1021-1045,1212-1234`; upgrade test `:426-440`. Prepare reuse itself is NOT a DONE recovery API; it still rejects completed attempts. |
| Real callers, no dead-code bypass | Charged upgrade goes through `processChargedUpgrade` -> claim -> executor -> durable success -> finalization. | `UserSubscriptionService.java:106-122,324-398`; repository-wide `rg` finds `createUpgradeOrder` and `chargeUpgrade` only at their private declarations, `:287,311`. No bypass finding for those unused methods. |
| Confirm/reconciliation routes | Initial confirmation charges only from the claimed immutable DTO and persists success before local finalization. Reconciliation requires exact Provider evidence and calls the same guarded finalizers. | `BillingAgreementApplicationService.java:273-346`; reconciliation `:585-616,750-762`; command `:418-444,1311-1327`. No actual Provider operation performed. |
| Cancellation interaction | A cancellation committed before finalization changes status/agreement and causes guarded failure, preserving durable success for review. The separate billing-key cleanup route locks subscription before changing it. Local `selfCancel` has the F1 stale-write residual. | Returning test `:152-163`; upgrade test STATUS case `:410`; `BillingAgreementCleanupTransactionService.java:61-82`; F1 above. Do not conflate key removal with local cancellation. |
| Renewal interaction | Renewal uses agreement/subscription locks, but is not automatically blocked by the new upgrade-only unresolved query. A committed renewal or grace-period change changes the source and prevents an old upgrade from overwriting it; unresolved upgrade disposition remains manual review. | Command `:247-286,510-518,677-700,1035-1044`. No new renewal admission policy requested; scheduler/real midnight behavior unverified. |
| Admin interaction | Explicit correction uses agreement/subscription locks, checks its before-state and blocks PROCESSING/PROVIDER_SUCCEEDED/UNKNOWN monetary orders. Other source changes are rejected by the new guards. | `AdminSubscriptionCorrectionService.java:62-65,272-302,436-446`; upgrade source-change tests. No ADMIN/API or real tariff mutation performed. |
| Deployment compatibility | Legacy unbound unfinished monetary upgrades cannot satisfy the new source check; DONE remains replayable. Mixed writers and blind rollback cannot understand each other's identity semantics. | Upgrade test `:426-470`; WI002 rollout section. Admission pause/inventory/drain or approved reconciliation/remediation is required before rollout, and before reverting with bound commands in flight. No key backfill, data rewrite or actual drain performed. |

## Evidence Pointers

### Exact Owned Outputs

- `deliverables/agent/WI-20260909-ATS-011-evidence-pack.md`: this independent review.
- `deliverables/user/WI-20260909-ATS-011-summary.md`: concise findings and acceptance boundary.

No product/test files were authored or edited by WI011.

### Reviewed Product Snapshot

SHA-256 of current bytes at the end of product inspection, before report authoring:

| File under src/main/java/com/atstudio/atstudio/ | SHA-256 |
| --- | --- |
| service/PaymentCommandKeyFactory.java | C431917532E1AF468BD765A14B6310C90AC1A5097C07CCA0CAC6E9008F320E42 |
| service/BillingAgreementPrepareTransactionService.java | E4F20E106A5BAFDB87EF3A4083D2C30DD86DF70F3FA282068AE76D261391997B |
| service/PaymentCommandTransactionService.java | 74E197C70002AFC01B97CBD4F5C8C6B265F4B25C9707D9504E19390AEB39D7C3 |
| repository/PaymentOrderRepository.java | E3E0E948EC08B4468C68B0730A8D97D64F930F5CD3B216EAC080BF650C9EAA8F |

The shared workspace has unrelated concurrent changes. These hashes identify reviewed bytes, not a clean checkout or all-worktree attestation.

## Commands And Outputs

Executed by WI011 from `C:/Users/jm991/Desktop/project/ATStudio`:

- `rg --files -g '*WI-20260909-ATS-011*' -g '*WI-20260909-ATS-002*' -g '*REQ-20260909-ATS-001*'`: found assigned handoff, approval and implementation reports.
- `Get-Content` and line-numbered PowerShell reads of the explicitly listed guidance/source/tests/reports.
- `git diff --stat`, then `git diff -- <four WI002 product paths>` and focused test diffs: read-only inspection of current changes.
- `git rev-parse --short HEAD`: `8161f0a`.
- `rg -n 'createUpgradeOrder\(|chargeUpgrade\(' src/main src/test`: exactly the two private declarations, no callers.
- `Get-Content output/release-remediation-20260909/payment-initial-results.json` and `Get-Content .../payment-initial.log -Tail 45`: existing MA summary and `BUILD SUCCESSFUL in 58s`.
- `Get-FileHash -Algorithm SHA256 -LiteralPath <four product paths>`: hashes above.
- `apply_patch`: created the two assigned reports only.
- Post-authoring PowerShell checks: both owned reports have metadata, zero trailing-whitespace lines and ASCII-only content; the summary's evidence-pack target exists. Structured `ConvertFrom-Json`/`Measure-Object` recalculation confirms 7 suites, 75 tests, 3 skips and zero failures/errors. All four reviewed product hashes remained unchanged. These are narrow artifact checks, not the full documentation validator or a new payment test run.

Some initial discovery reads named nonexistent `PaymentCommandService.java` / `BillingAgreementCancellationTransactionService.java`, and a wildcard path supplied directly to rg was rejected on Windows. Corrected with bounded service-name/caller searches; no command with those names executed product behavior. An rg absence result for `Version|DynamicUpdate` is not a passing test.

NOT executed: Gradle, npm, JUnit/H2, MySQL, test authoring, original-code red run, Provider/mail calls, browser/runtime checks, restarts, DDL, media/data/log mutations, Git mutations or deployment. No new raw test log was created by this reviewer.

## Tests And Ready List

### Existing MA Evidence Inspected

Source: `output/release-remediation-20260909/payment-initial-results.json`; corresponding `payment-initial.log` ends in successful build. This reviewer did not launch that run, and its original exact command is not asserted here. Current `build/test-results` was already populated by a different MA run, so the saved initial JSON is the per-suite source inspected, not independently re-parsed original payment XML.

| Suite | Tests | Passed | Skipped | Failures / Errors |
| --- | ---: | ---: | ---: | ---: |
| BillingAgreementPrepareIdempotencyIntegrationTest | 20 | 20 | 0 | 0 / 0 |
| BillingAgreementPrepareMysqlConcurrencyIntegrationTest | 3 | 0 | 3 | 0 / 0 |
| BillingAgreementPrepareToConfirmIntegrationTest | 3 | 3 | 0 | 0 / 0 |
| PaymentCommandKeyFactoryTest | 7 | 7 | 0 | 0 / 0 |
| PaymentReconciliationRecoveryIntegrationTest | 9 | 9 | 0 | 0 / 0 |
| PaymentReturningSubscriberIntegrationTest | 12 | 12 | 0 | 0 / 0 |
| SubscriptionUpgradeCommandIntegrationTest | 21 | 21 | 0 | 0 / 0 |
| Total | 75 | 72 | 3 | 0 / 0 |

The three guarded MySQL skips are NOT MySQL concurrency proof. The remaining execution is local fake-provider/H2 plus unit evidence, not production payment or real Provider acceptance.

### MA-Only Next Verification

Readiness was sent in commentary before writing these reports. No runner was started by WI011.

1. Author and run F1's cancellation-read / upgrade-finalize / cancellation-commit regression with independent H2 transactions, fake Provider and a fresh-context final read. Scenario specified, NOT yet authored or executed by WI011. Include same-period paid-tier preservation and one-payment/DONE-replay assertions.
2. After the implementation owner resolves/disposes F1, serialize the four existing core suites: `PaymentReturningSubscriberIntegrationTest`, `SubscriptionUpgradeCommandIntegrationTest`, `PaymentCommandKeyFactoryTest`, `PaymentReconciliationRecoveryIntegrationTest`.
3. Preserve WI002's compatibility queue: prepare idempotency/to-confirm, durable Provider-success recovery, independent command verification, recovery reads, recurring renewal, billing-agreement cancellation and charge timestamps. Do not claim these all ran from the initial seven-suite result.
4. Do not opt into real-MySQL profiles. Any future MySQL interleaving or old-format deployment drain requires its own authorized execution scope.

## Risks / Rollback

- F1 is an actionable source-grounded residual, not a fabricated red test. Resolve the targeted evidence before treating PAY-02 as fully closed.
- Source-bound failure after cancellation, tariff/period replacement or renewal may require reviewed disposition of already-paid commands. This review does not authorize auto-refund, historical source reconstruction or changing cancellation/renewal policy.
- Logical source hashes cannot identify an actor restoring exactly identical source fields. No broad monotonic-generation redesign is requested.
- Real MySQL lock scope/interleavings, query-plan performance, deployment drain and actual Provider behavior remain unverified. No full release approval is made.
- Report rollback is document-only through MA's normal correction process; no product rollback performed. Product rollback must respect WI002's drain boundary and must not strip command suffixes, rewrite ledgers, delete history or revert unrelated shared files.

## Follow-ups / WI Chain

WI011's review/report work is complete with F1 OPEN. Return F1 to MA/WI002's implementation owner for bounded reproduction and disposition; this reviewer does not subdelegate or modify product code. WI011 blocks WI013 and WI014: MA should propagate this finding to the documentation and final integration gates and require the follow-up evidence before unconditional payment closure. REQ remains open; this report does not close other WIs.

## Dated Re-review: 2026-09-09 F1 Correction

### Disposition And Scope

Reviewed current source/test bytes on 2026-09-09, with the verification checkpoint at 04:37:47 +09:00. **F1 is addressed at the code-review level. No new blockers found in this bounded re-review.** Final executed-regression acceptance remains pending MA evidence. This is not an H2 PASS, MySQL proof, whole-application deadlock guarantee, full-build approval or deployment approval.

User explicitly assigned this follow-up after WI002 added agreement-before-subscription locks and pre-mutation unresolved-command fencing. Read the assigned handoff, this original F1, updated WI002 evidence, current `UserSubscriptionService`, relevant repository methods, the unchanged payment claim/finalizer, the new mutation regression and the changed service unit tests. The previous Tier-0/security/quality constraints and create-wi-evidence-pack structure remain in force. No product writes, subdelegation or heavy runners.

### Current Source And Race Review

Paths in this section are under `src/main/java/com/atstudio/atstudio/`.

| Boundary | Current evidence and conclusion |
| --- | --- |
| First subscription read | `service/UserSubscriptionService.java:128-132,202-207,218-223` now locks the authenticated user's TOSS agreement before loading the mutable subscription with `findActiveByUserForUpdate`. `findUser` does not preload a subscription association: the inspected `entity/User.java` has no such mapping. The original stale first-level subscription instance is therefore not carried across the agreement wait by these entry points. |
| Owner/access predicates | `repository/BillingAgreementRepository.java:46-53` filters user ID and Provider under PESSIMISTIC_WRITE. `repository/UserSubscriptionRepository.java:34-40` retains the original ACTIVE/CANCELLED and `expiresAt >= today` access predicate, adds the pessimistic write lock and fetches current user/plan state. It does not broaden expired access or switch cancellation to a different lifecycle. |
| Mutation ordering | Pending clearing (`:141-144`), zero-amount upgrade (`:169-172`), scheduling (`:185-187`), cancellation (`:203-211`) and reactivation (`:219-224`) all validate the unresolved-command fence before changing entity state. The agreement is reused from the lock, not reloaded through an earlier unlocked query. |
| Unresolved predicate | `repository/PaymentOrderRepository.java:109-117` is a locking query for this agreement, purposes SUBSCRIBE/UPGRADE/RENEWAL and states READY, IN_PROGRESS, PROCESSING, PENDING_PROVIDER_CONFIRMATION, PROVIDER_SUCCEEDED, ordered by ID. `UserSubscriptionService.java:239-243` requests one row because existence alone rejects the mutation. It is not an ordinary snapshot `exists` query; it cannot discard an unresolved older period merely because the current period changed. Zero-charge BILLING_AGREEMENT and terminal DONE/FAILED/CANCELLED/EXPIRED orders are excluded. |
| Finalizer wins first | Finalization locks agreement, subscription and order at `service/PaymentCommandTransactionService.java:628-636`, then commits the paid tier. A local mutation waiting before its agreement acquisition subsequently loads the new subscription state. DONE no longer matches the fence, so cancellation can preserve the paid tier/period while setting CANCELLED. This addresses the original F1 commit order. |
| Local mutation wins first, command already unresolved | It holds agreement/subscription, sees the unresolved monetary order and throws PAYMENT_ORDER_INVALID_STATE before source changes. Transaction rollback releases locks; the waiting finalizer can then validate the unchanged source and complete. This addresses MA's opposite-order/stranded-success counterexample rather than relying on locks alone. |
| Provider outside transaction / UNKNOWN | Claim and local mutation serialize on the same agreement row. After an accepted claim commits, PROCESSING remains a fence across the external-call gap; UNKNOWN and PROVIDER_SUCCEEDED stay fenced. Recording Provider outcome also takes the agreement lock. There is no read-check/mutate gap after the guard within the local transaction. A definitive FAILED result releases this local fence. |
| Paid retry and lock release | `UserSubscriptionService.java:159-166` performs no local mutation and deliberately routes a charged upgrade to the existing claim/finalizer. Its planning TransactionTemplate ends at `:110-122` before the Provider executor, whose `Propagation.NEVER` contract is unchanged. `PaymentCommandTransactionService.java:102-110` retains exact durable-success finalize-only recovery; a blanket planning guard was not added in front of that path. |
| DONE policy and replay | `selfCancel` still makes no Provider cancel/refund call, preserves plan/start/end/pending data and allows paid access to expiry via the unchanged access predicate. Reactivation retains the existing key/agreement eligibility checks. `PaymentCommandTransactionService.java:646-648` preserves read-only DONE replay, so replay cannot undo a later legitimate cancellation/pending choice. |
| Deadlock/race assessment | Within the inspected same-user local-mutation/payment paths, the order is agreement -> subscription (including joined user/plan locks) -> order. No new subscription-before-agreement inversion or held-lock Provider call was found. An existing agreement also serializes insertion of a new command against the one-row predicate; a missing agreement does not permit a normal paid claim without first creating/locking one. Locking joins can cover more rows than the root entity, and cross-user MySQL lock plans/timeouts were not executed or exhaustively audited. |

The guard is intentionally state-based: an overdue READY order still needs a legitimate terminal disposition; this review does not introduce automatic expiry, force-failure or historical cleanup. Existing old-format drain and out-of-band source-change limitations from the initial review remain applicable. The new fence is scoped to the named local mutation entry points, not a new platform-wide cancellation/renewal policy.

### Regression Source Review

`src/test/java/com/atstudio/atstudio/service/SubscriptionMutationFenceIntegrationTest.java` contains six test methods expanding to ten cases (four enum mutations and two boolean Provider outcomes). These are authored assertions inspected by WI011, not executed results at this checkpoint.

| Test pointer | What it actually establishes when executed |
| --- | --- |
| `:148-164` | A test-only legacy cancellation control intentionally restores Basic, captures ordered UPGRADE then MUTATION SQL with plan/cycle/expiry columns, and shows DONE replay does not repair the stale overwrite. This is a valid unsafe-pattern control, not a claimed checkout of the entire original implementation. |
| `:169-194,326-355` | Actual cancel/reactivate/schedule/clear service paths start before finalization; the gate pauses before the first new agreement lock, while the legacy control pauses after its stale subscription read. Distinct transaction resources, actual service-return commit order, fresh-context snapshots, paid tier/period and idempotent DONE replay are asserted. |
| `:198-223` | Cancellation pauses after acquiring the source lock; another thread starts finalization and attempts its agreement lock. Cancellation rejects the unresolved success and releases locks; finalization must finish with paid access intact. The immediate `isDone` check alone is not a latency/deadlock proof, but bounded completion, real locks and final state are also asserted. |
| `:228-282` | A held fake Provider call allows concurrent busy checks; all four local mutations must reject without changing the snapshot/order count. The UNKNOWN branch checks the persisted UNKNOWN state, repeats the guards, supplies synthetic authoritative recovery and finalizes once. After DONE, ordinary cancellation/reactivation preserves the paid plan and period. |
| `:286-294` | A busy cancellation does not block the exact charged upgrade's finalize-only retry; no second Provider charge occurs and cancellation works after DONE. |
| `:298-309` | Definitive FAILED permits local cancellation, preserves the period, writes no successful payment and performs no refund/provider cancellation. |
| `:420-438,462-489` | Fresh TransactionTemplate snapshots, one payment/order/charge assertions, bounded latches and SQL inspection support the interleavings. SQL is captured by the test inspector, not executed or collected by WI011. |

`UserSubscriptionServiceTest.java:808-815` adds ordered agreement/subscription acquisition and forbids the former unlocked repository reads; its changed fixtures use the locking methods. This complements, rather than substitutes for, the independent H2 transactions.

Residual coverage limits, not new blocker findings: the new integration enum does not explicitly exercise the zero-amount branch, and the state matrix directly exercises PROCESSING/UNKNOWN/PROVIDER_SUCCEEDED/DONE/FAILED rather than every included/excluded purpose/status. The zero-amount branch and remaining predicates were checked statically. No MySQL execution is inferred from H2-oriented test code.

### Execution Boundary And Ready Queue

- WI011 executed only read-only `Get-Content`, scoped `git diff`, `rg`, `Get-Item`/directory inventory, `Get-Date`, and SHA-256 inspection, followed by updates to the two assigned reports using apply_patch.
- MA announced a full Spring Boot 4.0.8 build at follow-up assignment. At the inspected checkpoint, the exact mutation-suite and service-suite XML files were not available in `build/test-results/test`; no F1 PASS count can be claimed from them.
- A bounded tail read of `output/release-remediation-20260909/backend-full-second.log` showed partial OPS-01 `AppConfigMultipartBoundaryTest` failure entries and no terminal build outcome in that tail. They were not investigated because they are outside this review; they prevent calling the observed log a full PASS. MA owns the runner, final outcome and other-domain disposition.
- The initial 75/72/3 result predates this F1 correction and must not be reused as follow-up acceptance. Neither the unsafe control nor a full original-code red run was executed by this reviewer.
- MA-only ready queue: complete the current serialized build; collect `SubscriptionMutationFenceIntegrationTest` and `UserSubscriptionServiceTest` results; preserve the existing four core payment suites and WI002's compatibility queue. Do not start a second concurrent runner or enable real-MySQL profiles for this review.

### Reviewed Follow-up Snapshot

SHA-256 at 04:37:47 +09:00, relative to the repository root:

| File | SHA-256 |
| --- | --- |
| src/main/java/com/atstudio/atstudio/service/UserSubscriptionService.java | 0A12C17AD466D9FEF6C7A6969E29D46C2BD53DBAAC40BCA1182C1176DEBD21D3 |
| src/main/java/com/atstudio/atstudio/repository/PaymentOrderRepository.java | 5F7C83E53D27537A4704B058C772DF5FF7550188710148C8F90221488CB55193 |
| src/main/java/com/atstudio/atstudio/service/PaymentCommandTransactionService.java | 74E197C70002AFC01B97CBD4F5C8C6B265F4B25C9707D9504E19390AEB39D7C3 |
| src/test/java/com/atstudio/atstudio/service/SubscriptionMutationFenceIntegrationTest.java | 5036F5C9D91812B297471AF618D0B9B8A8F81DF7C6652A5CD2D96E50F6E055F9 |
| src/test/java/com/atstudio/atstudio/service/UserSubscriptionServiceTest.java | ED9381AFD68FD5A7DA51F23D908FD06C6BF16BBBFCEB26B099B873E150475926 |

### Updated Handoff

The original F1 and its initial OPEN disposition above remain historical evidence. Current disposition: **implementation addressed; bounded independent code re-review complete, no new blockers; executed verification pending MA**. Propagate this dated disposition to WI013/WI014 and attach the new regression/build evidence before unconditional closure. No new product feature, DDL, refund/cancellation policy change or additional full audit is requested. Rollback/drain boundaries remain unchanged; no rollback or product edit was performed by WI011.

## Executed Evidence Update: 2026-09-09 Backend Second Run

This later checkpoint supersedes the 04:37 verification-pending disposition, while preserving the original F1 and the intervening source review. MA reported completion, and WI011 then independently read the saved JSON, terminal log and exact payment XML results. **F1 CLOSED within the approved local-code/fake-provider/H2 scope. Bounded review found no new payment blockers.**

### Exact Evidence Inspected

- `output/release-remediation-20260909/backend-full-second-results.json`: parsed with ConvertFrom-Json and aggregate counts recomputed. 200 suites, 1,825 tests, 9 failures, 0 errors, 19 skips; 1,797 passed. Its only failing suite is `OPS-01: real Tomcat multipart parsing without a running server` (10 cases, 9 failures).
- `output/release-remediation-20260909/backend-full-second.log`: terminal `1825 tests completed, 9 failed, 19 skipped`, `Task :test FAILED` and `BUILD FAILED in 2m 38s`. Earlier entries identify AppConfigMultipartBoundaryTest fixture NullPointerExceptions. WI011 did not investigate or modify that unrelated fixture.
- `build/test-results/test/TEST-com.atstudio.atstudio.service.SubscriptionMutationFenceIntegrationTest.xml`: 10 tests, 0 failures/errors/skips, duration 1.498 seconds, suite timestamp `2026-09-08T19:37:26.586Z`. All ten named testcases were inspected, including the legacy overwrite control, four actual mutations, two Provider outcome variants, opposite lock order, exact finalize-only retry and definitive failure.
- The other class XML files below were parsed using `[xml]` and their suite attributes summed. UserSubscriptionServiceTest has six XML files including nested suites. These are existing MA output artifacts; WI011 did not execute the runner.

| Verified class | Tests / Passed | Failures / Errors / Skips |
| --- | ---: | ---: |
| SubscriptionMutationFenceIntegrationTest | 10 / 10 | 0 / 0 / 0 |
| UserSubscriptionServiceTest (six XML suites) | 21 / 21 | 0 / 0 / 0 |
| PaymentReturningSubscriberIntegrationTest | 12 / 12 | 0 / 0 / 0 |
| SubscriptionUpgradeCommandIntegrationTest | 21 / 21 | 0 / 0 / 0 |
| PaymentCommandKeyFactoryTest | 7 / 7 | 0 / 0 / 0 |
| PaymentReconciliationRecoveryIntegrationTest | 9 / 9 | 0 / 0 / 0 |
| BillingAgreementPrepareIdempotencyIntegrationTest | 20 / 20 | 0 / 0 / 0 |
| BillingAgreementPrepareToConfirmIntegrationTest | 3 / 3 | 0 / 0 / 0 |
| PaymentProviderSuccessRecoveryIntegrationTest | 2 / 2 | 0 / 0 / 0 |
| PaymentCommandIndependentVerificationIntegrationTest | 7 / 7 | 0 / 0 / 0 |
| PaymentRecoveryReadIntegrationTest | 2 / 2 | 0 / 0 / 0 |
| RecurringRenewalCommandIntegrationTest | 8 / 8 | 0 / 0 / 0 |
| BillingAgreementCancellationTransactionIntegrationTest | 4 / 4 | 0 / 0 / 0 |
| BillingAgreementChargeTimestampIntegrationTest | 6 / 6 | 0 / 0 / 0 |
| Selected related total: 14 classes, 19 XML suites | 132 / 132 | 0 / 0 / 0 |

These 132 cases are a selected payment/F1 acceptance set, not a claim that they are every payment test in the full suite. The separately guarded MySQL prepare suite remains 3/3 SKIPPED in the saved JSON and is not included in the 132. No MySQL runtime proof is claimed.

### Closure Rationale And Remaining Gates

The original F1 now has executed synthetic evidence: the legacy control's assertions confirm the stale Basic overwrite and ordered full-row UPDATE shape, and the corrected actual service interleavings retain Premium, period, expected cancellation/pending state and a single successful payment. The new opposite-order/held-Provider/UNKNOWN assertions also passed, demonstrating that the busy fence prevents ordinary local cancellation from stranding paid finalization. Exact paid recovery and cancellation after DONE/FAILED remain compatible in the executed scenarios. This is an executed test-only unsafe-pattern control plus corrected-code regression, not a reverted-original-tree red run or actual Provider transaction.

The reviewed lock/read predicates, no-lock-held Provider boundary and no-new-blocker conclusion in the dated source review remain unchanged. Static-only zero-amount branch and unexecuted MySQL/cross-user lock-plan limitations remain explicit; they do not reopen F1 on the supplied evidence.

MA should propagate **F1 closed, payment subset passed, full build failed** to WI013/WI014. The unrelated multipart fixture correction, successful subsequent whole-build verification and coverage gate still belong to MA's existing quality chain. Do not mark the full backend build, overall REQ or release complete from this payment acceptance. No extra runner, product edit, database/provider action or new full audit was performed by WI011.

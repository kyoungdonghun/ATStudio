---
version: 1.0
last_updated: 2026-07-16
project: ATS
owner: cr
category: evidence-pack
status: findings-recorded
related_wi: WI-20260716-ATS-016
dependencies:
  - path: ../user/REQ-20260716-ATS-002.md
    reason: Approved development-branch remediation scope
  - path: WI-20260716-ATS-016-handoff.md
    reason: Review execution contract
  - path: WI-20260716-ATS-013-evidence-pack.md
    reason: Backend verification baseline
  - path: WI-20260716-ATS-014-evidence-pack.md
    reason: Frontend verification baseline
---

# Evidence Pack: WI-20260716-ATS-016

## Summary

Completed a findings-first adversarial review of the WI-005 through WI-014 remediation state on `codex/p1-acceptance-hardening`. The review found P0: 0, P1: 1, P2: 6, and P3: 2. No source, configuration, schema, current-state/design document, runtime, DB, provider, secret, client branch, Git index, commit, or remote was changed.

## Scope / DoD Check

- [x] Reviewed authorization, redirects/OAuth, file/privacy, payment/reconciliation/refund/entitlement, crypto, transaction/lock/idempotency/compensation, whitelist/certification, download/catalog, and frontend asynchronous/mutation risks.
- [x] Ranked concrete findings P0-P3 with reachable code paths and exact pointers.
- [x] Separated exploitable defects, reliability races, test gaps, policy-pending decisions, and environment-only proof limits.
- [x] Explicitly accepted or bounded every WI-013/WI-014 residual.
- [x] Mapped every actionable finding to a minimal WI-017 fix and focused regression tests.
- [x] Preserved product policy: public full-track listening, gated downloads, recurring card billing, and single-server topology.
- [x] Created only the required WI-016 summary and Evidence Pack.

## Reference Documents

| Tier | Document | Review use |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Financial auditability and approved product invariants |
| 0 | `docs/standards/development-standards.md` | Service, transaction, DTO, and test standards |
| 1 | `docs/policies/security-policy.md` | Role, file, secret, stream, download, and provider boundaries |
| 1 | `docs/policies/quality-gates.md` | Evidence and verification classification |
| 2 | `deliverables/user/REQ-20260716-ATS-002.md` | Approved remediation scope |
| 2 | `docs/design/remaining-remediation-design-20260716.md` | Intended remediation contracts |
| 2 | `docs/design/api-spec.md` | API and role contract |
| 2 | `docs/design/payment-integration-design.md` | Recurring payment architecture |
| 2 | `docs/design/payment-operations-runbook.md` | Reconciliation and operator contract |
| 2 | `deliverables/agent/WI-20260716-ATS-005-evidence-pack.md` through `WI-20260716-ATS-014-evidence-pack.md` | Implemented protections, tests, and retained limits |

## Branch and Diff Scope

| Item | Evidence |
|---|---|
| Worktree | `C:/Users/jm991/Desktop/project/ATStudio` |
| Branch | `codex/p1-acceptance-hardening` |
| Baseline | Current HEAD `cd876fc`; reviewed the current WI-005 through WI-014 working-tree remediation state |
| Client branch/runtime | Not inspected, modified, or restarted |
| Live DB/provider/secrets | Not accessed |
| Git mutation | No stage, commit, push, branch change, reset, restore, or clean |

## Severity Summary

| Severity | Count | IDs |
|---|---:|---|
| P0 Critical | 0 | None |
| P1 High | 1 | F-016-01 |
| P2 Medium | 6 | F-016-02 through F-016-07 |
| P3 Low | 2 | F-016-08, F-016-09 |

## Findings

### F-016-01 - P1 High - Stored unsafe whitelist scheme reaches ADMIN navigation

**Class:** exploitable input/output validation defect.

**Reachable path:** authenticated subscriber creates/updates a channel -> backend accepts a URI whose host is `youtube.com` regardless of scheme -> persisted `channelUrl` is returned -> admin whitelist table renders it directly in an anchor.

**Evidence:**

- `src/main/java/com/atstudio/atstudio/service/WhitelistChannelService.java:204-215`: checks `getHost()` only; no scheme or credentials restriction.
- `frontend/src/pages/admin/WhitelistChannelManagePage.tsx:288-292`: direct admin `href={channel.channelUrl}`.
- `frontend/src/pages/subscriber/WhitelistChannelPage.tsx:376-380`: direct subscriber `href`.
- Pure parser proof used during review: Java `URI.create("javascript://youtube.com/%0Aalert(document.domain)")` reports scheme `javascript` and host `youtube.com`, satisfying the current backend condition.

**Impact:** stored unsafe navigation is presented to an operator. `noopener noreferrer` mitigates opener/reference leakage but is not a protocol allowlist.

**Minimal WI-017 fix:** enforce `https` and normalized exact/subdomain YouTube hosts at the backend; reject user-info; audit/quarantine retained invalid rows; render plain text rather than a link unless a shared frontend safe-URL predicate passes.

**Focused tests:** service/controller rejection matrix for scriptable/non-web schemes and host-confusion values; valid `https://youtube.com`, `https://www.youtube.com`, and channel URLs; existing-row frontend rendering test.

### F-016-02 - P2 Medium - Stale entitlement correction can overwrite a newer result

**Class:** financial/entitlement state race and stale-command defect.

**Reachable path:** operator creates correction A and B for the same successful refund/subscription -> both capture the same before-state -> both are approved -> A executes -> B executes later and applies its stored target without comparing the current state to B's before-state.

**Evidence:**

- `AdminPaymentEntitlementCorrectionService.java:100-137`: no subscription lock and no existing non-terminal correction guard at creation.
- `AdminPaymentEntitlementCorrectionService.java:173-218`: locks correction and subscription, then applies target without revalidating `before_*`.
- `AdminPaymentEntitlementCorrectionService.java:246-290`: validation occurs only against the state visible at create/preview time.
- `src/main/resources/schema.sql:708-746`: no uniqueness or active-command key for refund/subscription corrections.

**Minimal WI-017 fix:** lock the user subscription during create; reject another REQUESTED/APPROVED/PROCESSING correction for the same refund/subscription; at execute, compare all before-state fields after acquiring the subscription lock and fail stale commands without mutation.

**Focused tests:** concurrent/successive duplicate creation, reverse-order execution, intervening plan/status/expiry/pending change, and idempotent terminal retry.

### F-016-03 - P2 Medium - Completed provider payments are outside provider reconciliation

**Class:** financial reconciliation coverage defect.

**Reachable path:** a provider payment is locally finalized `DONE` -> provider status later becomes not found/non-DONE outside this application -> scheduled provider reconciliation never selects the order -> no payment mismatch incident is generated and local access remains unchanged.

**Evidence:**

- `PaymentOrderRepository.java:68-79`: provider candidates exclude `DONE`.
- `PaymentReconciliationTransactionService.java:46-63`: local `DONE` pass checks only missing local payment rows.
- `PaymentReconciliationTransactionService.java:89-98,222-225`: provider claim path is limited to pending/provider-succeeded/stale-processing.
- `PaymentReconciliationService.java:109-175`: exposes `localDoneButProviderNotDone`, but its input set cannot contain `DONE` orders.
- `docs/design/payment-operations-runbook.md:120-124`: documents local-DONE/provider mismatch issue types for payments.

**Minimal WI-017 fix:** add a bounded cursor/checkpoint for eligible recent provider-backed `DONE` orders. Treat known app-recorded refunds/cancellations as expected evidence, and keep detection separate from automatic entitlement mutation.

**Focused tests:** DONE/DONE, DONE/not-found, DONE/non-DONE, lookup failure, amount/currency/transaction mismatch, known refund exclusion/classification, cursor pagination, repeated incident dedupe/reopen.

### F-016-04 - P2 Medium - Album/playlist mutation locks do not cover update/delete

**Class:** catalog reliability race.

**Reachable path:** update and delete load the same active entity without locking -> delete marks inactive while update retains a stale active snapshot -> commit order can lose fields or restore active state. Membership operations use the new pessimistic lock, so they can also interleave with unlocked update/delete.

**Evidence:**

- `AlbumService.java:112-143` uses unlocked `getActiveAlbum`; `:148-192` uses locked `getActiveAlbumForUpdate` for membership changes.
- `AlbumRepository.java:30-32` already provides the required lock query.
- `PlaylistService.java:192-214,253-263` uses unlocked `getOwnedPlaylist`; `:218-248` uses the locked variant for membership changes.
- `PlaylistRepository.java:20-22` provides the lock query.
- `Album.java:16-55` and `Playlist.java:13-44` have no `@Version` fallback.

**Minimal WI-017 fix:** use the existing pessimistic lock helper for every metadata update/delete and preserve deterministic parent-before-child lock order.

**Focused tests:** update/delete, add/delete, reorder/delete, and update/update in the MySQL concurrency profile; unit tests may verify repository selection but must not be presented as MySQL lock proof.

### F-016-05 - P2 Medium - Player subscription state collapses errors into inactive

**Class:** frontend state-classification and stale-response defect; accepted WI-014 F-014-01.

**Evidence:** `frontend/src/layouts/PlayerBar.tsx:86-94` maps every rejection to false and has no cancellation/generation control.

**Minimal WI-017 fix:** explicit loading/active/inactive/error state; only structured `NO_ACTIVE_SUBSCRIPTION` maps to inactive; AbortController plus generation fence.

**Focused tests:** active, structured inactive, 5xx/offline, logout/login transition, and out-of-order resolution.

### F-016-06 - P2 Medium - Four loaders can commit stale results

**Class:** frontend latest-request-wins defect; accepted WI-014 F-014-02.

**Evidence:**

- `TrackDetailPage.tsx:36-45`.
- `UserManagePage.tsx:29-43`.
- `UserSubscriptionManagePage.tsx:50-70`.
- `DownloadQueuePage.tsx:69-95`.

**Minimal WI-017 fix:** reuse the generation/AbortController pattern already present in remediated list/payment pages; ignore cancellation and gate success/error/finally writes.

**Focused tests:** deferred old success and old failure after current success for route/page/filter/sort transitions.

### F-016-07 - P2 Medium - Admin financial mutations lack focused UI tests

**Class:** high-risk verification gap, not a proven production defect; accepted WI-014 F-014-03.

**Evidence:**

- `PaymentReadOnlyPage.tsx:393-644`: settlement, refund, and correction handlers.
- `PaymentReadOnlyPage.tsx:979-1111,1272-1284,1696-1841`: busy/disabled/approve/execute controls.
- `PaymentReadOnlyPage.test.tsx:185-292`: four tests cover read fencing/failure and incident reload only.

**Minimal WI-017 fix:** tests only unless a test exposes a defect; retain existing API and UX contract.

**Focused tests:** exact payload/ID/note, typed confirmation rejection/acceptance, one-call busy behavior, failure feedback, status-gated buttons, and one refresh after success.

### F-016-08 - P3 Low - OAuth completion discards safe return target

**Class:** navigation regression; accepted WI-014 F-014-04.

**Evidence:** `LoginPage.tsx:228-262` stores OAuth state/verifier but not the validated return target; `SocialLoginPage.tsx:72-77` always navigates to `/` after a complete profile.

**Minimal WI-017 fix:** bind a validated internal target to the OAuth attempt in session storage and consume once; never accept raw callback/query targets.

**Focused tests:** protected deep link, external/protocol-relative rejection, missing/stale OAuth state, and complete-profile continuation.

### F-016-09 - P3 Low - Router count comment is obsolete

**Class:** maintenance/documentation drift only; accepted WI-014 F-014-05.

**Evidence:** `frontend/src/router/index.tsx:139` retains the obsolete fixed count; WI-014 independently derived current route/visual counts.

**Minimal WI-017 fix:** remove the fixed number or point to `docs/ui/atstudio-front-list.md`.

**Focused test:** none required; source/docs count validation is sufficient.

## Threat and Race Matrix

| Area | Locally verified protection | Residual classification |
|---|---|---|
| Auth/roles | `/api/admin/**` ADMIN; payment USER-only; certification USER+BUSINESS; private upload paths denied | No concrete bypass found |
| OAuth/redirect | State and PKCE verifier are browser-checked; password return target is validated | P3 safe target continuity gap only |
| File/privacy | Company documents use private storage, admin-only controller download, canonical image handling, and path omission from DTO | Malware scanning/retention remain policy or operational boundaries |
| Whitelist | User ownership, plan/status transitions, CSV formula neutralization, deterministic export locking | P1 scheme allowlist defect |
| Billing crypto | AES-GCM envelope with key ID/AAD; HMAC fingerprint; no raw key/card logging found | Live key rotation remains environment evidence |
| Refund | Persisted idempotency key, claim lease, provider call outside transaction, fenced result application | No additional defect found; UI mutation proof gap remains |
| Entitlement correction | Explicit preview/request/approve/execute ledger and subscription row lock at execute | P2 stale command/duplicate non-terminal gap |
| Reconciliation | Exact provider DONE/amount/currency/transaction evidence before local recovery; incident persistence | P2 completed-order comparison gap |
| Withdrawal/scheduler | After-commit provider cleanup and retry; single-server schedule matches topology | Social-only withdrawal policy pending; no multi-server finding |
| Downloads/licenses | Server-side subscription/quota/license checks, user lock, atomic count, unique user/track contract | Real MySQL and symlink-host proof remain conditional |
| Album/playlist | Parent locks protect membership add/remove/reorder | P2 update/delete lock gap |
| Frontend async | Several remediated screens use generation/abort fencing | P2 player/four-loader gaps |

## WI-013 / WI-014 Disposition

### WI-013

- Accepted as current automated backend evidence: 146 suites, 1,046 tests, 1,037 passed, zero failures/errors, nine skipped.
- Accepted coverage observation: 76.81% lines, 59.05% branches, 76.57% methods.
- Eight MySQL-specific skips and one symlink-capable-host skip remain environment-only evidence gaps. They are not converted into generic findings.
- No P0/P1 defect was hidden by a known test failure because the suite had none. This review nevertheless found independent code-path issues F-016-01 through F-016-04.

### WI-014

| WI-014 item | Disposition |
|---|---|
| F-014-01 player subscription classification | Accepted as F-016-05 P2 |
| F-014-02 incomplete latest-request-wins | Accepted as F-016-06 P2 |
| F-014-03 payment mutation test gap | Accepted as F-016-07 P2 verification gap |
| F-014-04 OAuth return target | Accepted as F-016-08 P3 |
| F-014-05 router count | Accepted as F-016-09 P3 maintenance |

Dependency audit, typecheck, lint, 38-file/180-test Vitest run, coverage generation, build, Prettier, full-track playback, and download-policy evidence are accepted for the development branch. They are not claims about the frozen client runtime or production environment.

## False-Positive and Environment Boundary

- **Not findings:** approved public full-track streaming, documented ADMIN download behavior, card recurring subscription policy, and single-server scheduler/process-local controls.
- **Policy pending:** social-only withdrawal; company-document malware scanning and retention. No implementation behavior was invented.
- **Environment-only:** retained MySQL DDL/index/lock proof, live Toss responses, production proxy/CORS/callbacks, real secrets, key rotation, symlink-capable storage host, and public/browser runtime behavior.
- **Defense in depth, not standalone findings:** frontend URL filtering after the required backend URL fix; operator alerts beyond current logs/incidents/email; multi-server locking; multi-PG expansion.
- **No generic claims:** no finding was raised solely because coverage is below 100%, an endpoint lacks method security in addition to route security, or a provider/network operation can fail.

## Commands and Outputs

| Command/evidence action | Result |
|---|---|
| `git branch --show-current` | `codex/p1-acceptance-hardening` |
| `git status --short` | Existing large WI remediation tree preserved; no cleanup or restore |
| `git diff -- <high-risk paths>` and line-numbered PowerShell reads | Reviewed exact current changes and reachable paths |
| Focused `Select-String` searches for roles, locks, fingerprints, reconciliation candidates, correction constraints, and frontend handlers/tests | Used to confirm or reject candidate findings |
| Java `URI` parser proof for unsafe scheme with YouTube host | Current backend host-only predicate is satisfiable by a scriptable scheme |
| WI-013/WI-014 Evidence Packs | Reused full-suite results; no redundant runtime/provider/DB process started |

## Tests and Evidence Gaps

No new automated suite was run by WI-016. This was a verification-only source review and relied on the independently reproduced WI-013/WI-014 full-suite evidence. Missing tests are attached to concrete findings above rather than converted into a generic coverage complaint.

WI-017 should run:

1. Focused backend tests for whitelist URL validation, correction fencing, DONE-order reconciliation, and album/playlist races.
2. The retained MySQL concurrency profile for lock-dependent findings.
3. Focused frontend tests for player state, stale loaders, payment mutations, and OAuth return target.
4. Complete backend test/build and frontend audit/typecheck/lint/test/coverage/build/Prettier checks.
5. `git diff --check` and the established `frontend/tsconfig.tsbuildinfo` hash-preservation procedure.

## WI-017 Minimal Work Plan

| Order | Finding | Minimal code/test surface |
|---:|---|---|
| 1 | F-016-01 | Backend URI allowlist, retained-row audit, frontend safe-link fallback, focused tests |
| 2 | F-016-02 | Subscription lock + non-terminal guard + before-state compare, concurrency/state tests |
| 3 | F-016-03 | Bounded DONE-order provider comparison, refund-aware classification, incident tests |
| 4 | F-016-04 | Reuse parent lock helpers in update/delete, MySQL race tests |
| 5 | F-016-05/F-016-06 | Shared latest-request/error-classification pattern and deferred tests |
| 6 | F-016-07 | Payment mutation UI tests; code changes only if tests expose a defect |
| 7 | F-016-08/F-016-09 | OAuth return continuity and stale comment cleanup |

The work plan deliberately excludes redesigns, new payment policy, multi-server topology, multi-PG work, provider/DB operations, and unrelated formatting/refactoring.

## Risks / Rollback

**Review risk:** exact MySQL lock behavior and live provider state were not executed in this WI. Findings that depend on those environments explicitly require WI-017 proof and are not represented as already reproduced production incidents.

**No-change statement:** WI-016 modified only:

- `deliverables/user/WI-20260716-ATS-016-summary.md`
- `deliverables/agent/WI-20260716-ATS-016-evidence-pack.md`

**Rollback:** remove only those two WI-016 deliverables. Do not revert any existing remediation, generated baseline, runtime log, client-demo state, DB/data, secret, stage, commit, or remote state.

## Follow-up Chain

WI-016 blocks WI-017. WI-017 should implement and independently verify only the bounded fixes/tests listed above, then reassess acceptance-test readiness on the development branch before any client-branch promotion decision.

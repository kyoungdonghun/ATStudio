---
version: 1.0
last_updated: 2026-07-16
project: ATS
owner: cr
category: audit
status: findings-recorded
dependencies:
  - path: REQ-20260716-ATS-002.md
    reason: Approved remediation scope
  - path: ../agent/WI-20260716-ATS-016-handoff.md
    reason: Code review execution contract
  - path: ../agent/WI-20260716-ATS-013-evidence-pack.md
    reason: Backend verification baseline
  - path: ../agent/WI-20260716-ATS-014-evidence-pack.md
    reason: Frontend verification baseline
---

# WI-20260716-ATS-016 Summary

## Outcome

The adversarial review of the development-branch remediation found no P0 issue, one reachable P1 security issue, six P2 correctness or verification risks, and two P3 maintenance or navigation issues. WI-017 should fix the P1 and P2 items before this branch is promoted for acceptance testing. The P3 items are bounded and may be completed in the same WI without broadening product policy.

The strongest protections were retained: role boundaries are enforced server-side, private uploads are not publicly served, downloads remain subscription/quota/license controlled, billing keys are encrypted and not logged, provider calls are outside local database transactions, refunds use a persisted idempotency key and lease fencing, and payment recovery requires exact provider evidence before local finalization.

## Findings First

### F-016-01 - P1 - Unsafe whitelist URL scheme reaches an ADMIN-clickable link

`WhitelistChannelService.validateChannelUrl()` checks only whether `URI.getHost()` is `youtube.com` or a subdomain. A value such as `javascript://youtube.com/%0Aalert(document.domain)` has a YouTube host and passes that check. The stored value is later rendered directly as `href` in both subscriber and admin pages.

- Evidence: `WhitelistChannelService.java:204-215`, `WhitelistChannelManagePage.tsx:288-292`, `WhitelistChannelPage.tsx:376-380`.
- Impact: an authenticated user can persist an unsafe navigation scheme that an operator may click. `target="_blank"` and `noopener` reduce opener access but do not make a scriptable URL safe.
- WI-017 minimum: allow only `https`, validate the normalized YouTube host case-insensitively, reject credentials, audit existing rows, and add a frontend safe-link guard.
- Tests: backend rejection tests for `javascript:`, `data:`, `file:`, `ftp:`, user-info and lookalike hosts; acceptance tests for valid HTTPS YouTube URLs; frontend tests that unsafe persisted values are not rendered as links.

### F-016-02 - P2 - Entitlement corrections can execute from stale snapshots

Correction creation reads a refund and current subscription without locking the subscription or rejecting another non-terminal correction. The schema has only ordinary indexes. Execution later locks the correction and subscription but applies the stored target without revalidating the stored `before_*` snapshot. Two corrections for one refund/subscription can therefore be approved and executed sequentially, allowing the later stale request to overwrite the first result.

- Evidence: `AdminPaymentEntitlementCorrectionService.java:100-137,173-218,246-290`; `schema.sql:708-746`.
- WI-017 minimum: serialize creation on the user subscription, reject an existing non-terminal correction for the same refund/subscription, and compare the current state with the recorded `before_*` state immediately before execution.
- Tests: competing create requests, two approved corrections executed in reverse order, changed subscription before execution, and terminal correction retry.

### F-016-03 - P2 - Provider reconciliation never checks completed payment orders

The provider-candidate query includes pending provider confirmation, provider success, and stale processing orders, but excludes `DONE`. Local reconciliation checks only that a `DONE` order has a local payment row. Consequently, the advertised `LOCAL_DONE_PROVIDER_NOT_DONE` and `LOCAL_DONE_PROVIDER_NOT_FOUND` payment cases and the `localDoneButProviderNotDone` counter cannot be produced for completed payment orders.

- Evidence: `PaymentOrderRepository.java:68-79`; `PaymentReconciliationTransactionService.java:46-63,89-98,222-225`; `PaymentReconciliationService.java:109-175`; `payment-operations-runbook.md:120-124`.
- Impact: a payment cancelled or otherwise changed directly at the provider can remain locally `DONE` with entitlement still active and no scheduled incident.
- WI-017 minimum: add a bounded, cursor-based reconciliation path for eligible recent `DONE` provider orders, with explicit treatment of app-recorded refunds/cancellations to prevent false incidents.
- Tests: provider `DONE`, not-found, non-DONE, lookup failure, amount mismatch, locally recorded refund, pagination, and repeat-run incident deduplication.

### F-016-04 - P2 - Album and playlist mutation locking is internally inconsistent

Track membership mutations now take pessimistic locks, but album and playlist update/delete paths still use unlocked reads. Neither entity has optimistic versioning. Concurrent update/delete operations can therefore lose updates; under Hibernate's normal entity update behavior, a stale update can also restore an `isActive=true` value after a soft delete.

- Evidence: `AlbumService.java:112-143,148-192`; `AlbumRepository.java:30-32`; `PlaylistService.java:192-263`; `PlaylistRepository.java:20-22`; `Album.java:16-55`; `Playlist.java:13-44`.
- WI-017 minimum: use the existing `findByIdForUpdate` path for every album/playlist mutation, including metadata update and delete, and preserve a single lock order.
- Tests: update versus delete, add/reorder versus delete, and two updates. Keep H2 unit tests, but prove lock behavior with the retained MySQL concurrency profile.

### F-016-05 - P2 - Player subscription lookup misclassifies infrastructure failure

`PlayerBar` maps every subscription lookup rejection to `hasSubscription=false` and has no abort or generation fence. Timeouts, 5xx responses, or an older request can therefore hide valid subscriber controls and present a subscribe action.

- Evidence: `PlayerBar.tsx:86-94`.
- WI-017 minimum: model loading/active/inactive/error states, classify only the structured no-active-subscription response as inactive, and add request cancellation plus a generation fence.
- Tests: active, domain-inactive, 5xx/offline, authentication change, and older-response-after-newer-response cases.

### F-016-06 - P2 - Latest-request-wins remains incomplete on four reachable screens

Track detail, admin user list, admin user-subscription list, and download history commit asynchronous results without an abort/generation guard. Older route, page, filter, or sort responses can overwrite newer state.

- Evidence: `TrackDetailPage.tsx:36-45`; `UserManagePage.tsx:29-43`; `UserSubscriptionManagePage.tsx:50-70`; `DownloadQueuePage.tsx:69-95`.
- WI-017 minimum: apply the existing request-generation/AbortController pattern to these four loaders and ignore cancellation as an error.
- Tests: deferred old success and old failure resolving after the current request for each screen.

### F-016-07 - P2 - Admin financial mutation UI lacks focused regression proof

The admin payment screen implements settlement import/reconciliation and refund/entitlement preview, request, approve, and execute flows. Its current tests cover read-request fencing and one incident status update, but not those financial mutations.

- Evidence: `PaymentReadOnlyPage.tsx:393-644,979-1111,1272-1284,1696-1841`; `PaymentReadOnlyPage.test.tsx:185-292`.
- WI-017 minimum: do not redesign the page; add focused interaction tests for existing contracts.
- Tests: payload and ID wiring, confirmation rejection/acceptance, busy/disabled single-submit behavior, failure feedback, and exactly one current-view refresh after success.

### F-016-08 - P3 - Social login loses the validated return target

Password login uses the validated internal `returnTo` target, but OAuth start does not persist it and the successful callback always navigates to `/`.

- Evidence: `LoginPage.tsx:228-262`; `SocialLoginPage.tsx:72-77`.
- WI-017 minimum: store the already validated target with the OAuth attempt, consume it once after profile completion, and retain the current external/protocol-relative rejection rules.
- Tests: valid deep link, unsafe target, missing/stale state, and incomplete-profile flow.

### F-016-09 - P3 - Router count comment is stale

`router/index.tsx:139` still states `49 screens + 2 error pages`, while WI-014 source-derived evidence and current UI documentation use the newer route/visual-screen counts. This has no runtime impact.

- WI-017 minimum: replace the fixed number with a pointer to the UI inventory or remove the count comment.

## WI-013 / WI-014 Disposition

- WI-013 is accepted as the backend automated baseline: 1,037 passing tests, no failure/error, nine conditional skips, and 76.81% line coverage. Its real-MySQL, live-provider, proxy, secret-rotation, and symlink-host gaps remain environment evidence limits, not newly asserted code defects.
- WI-014 findings F-014-01 through F-014-05 are accepted. They map to F-016-05 through F-016-09. No severity was inflated.
- WI-014's dependency, type, lint, test, build, formatting, full-track playback, and download-policy evidence remains valid for the reviewed development branch.

## False Positives and Boundaries

- Social-only account withdrawal remains policy-pending; no policy was invented and it is not reported as a defect.
- Single-process rate limits and a single-server scheduler match the approved topology. Multi-server coordination is not a finding.
- Company-document malware scanning and retention are approved policy/operations boundaries, not silently reclassified as implementation vulnerabilities.
- Skipped MySQL lock tests, real provider responses, deployment proxy/CORS, live secrets, and crypto-key rotation are environment-only proof gaps. Passing H2/unit tests do not prove those environments.
- Public full-track streaming and the documented ADMIN download bypass are approved product behavior, not vulnerabilities.

## WI-017 Priority

1. Block unsafe whitelist URL schemes and inspect retained rows.
2. Fence entitlement corrections and complete provider reconciliation for eligible `DONE` orders.
3. Make album/playlist mutation locking consistent.
4. Fix the five accepted WI-014 residuals, prioritizing player state, stale reads, and admin payment mutation tests.
5. Run focused tests plus the complete backend/frontend quality suites and the retained MySQL concurrency profile where available.

## Scope Preservation

WI-016 changed only this summary and its Evidence Pack. It did not modify application code, design/current-state documentation, DB/data, provider state, secrets, runtime processes, client-demo worktree, staging area, commits, or remotes.

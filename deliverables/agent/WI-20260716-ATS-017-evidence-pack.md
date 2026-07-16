---
version: 1.0
last_updated: 2026-07-16
project: ATS
owner: se
category: evidence-pack
status: complete
related_wi: WI-20260716-ATS-017
dependencies:
  - path: ../user/REQ-20260716-ATS-002.md
    reason: Approved remediation scope
  - path: WI-20260716-ATS-017-handoff.md
    reason: Execution and output contract
  - path: WI-20260716-ATS-015-evidence-pack.md
    reason: Cross-layer findings
  - path: WI-20260716-ATS-016-evidence-pack.md
    reason: Security and release findings
---

# Evidence Pack: WI-20260716-ATS-017

## Summary

Implemented and independently reverified every accepted WI-015/WI-016 finding on `codex/p1-acceptance-hardening`, synchronized current-state documentation and the deterministic client PDF, and preserved all product, environment, client-branch, data, provider, secret, and Git-mutation boundaries.

## Scope / DoD Check

- [x] Every accepted finding has a closure status and direct code/test/document evidence.
- [x] Unsafe whitelist storage and rendering paths are blocked without mutating retained rows.
- [x] Financial and catalog fixes are bounded, idempotent or lock-fenced, and preserve approved subscription/payment policy.
- [x] Frontend inactive/error/cancel/stale states and high-risk admin mutations have focused tests.
- [x] Current code, API/design/security/payment/operations/client documentation, and generated PDF describe one system.
- [x] Focused checks ran before complete backend/frontend gates.
- [x] Complete backend, frontend, documentation, PDF, diff, and generated-file integrity gates pass.
- [x] Client worktree/runtime, DB/data, provider/secrets, staging, commits, and remotes were not mutated.

## Reference Documents

| Tier | Document | Use |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Product invariants and evidence rules |
| 0 | `docs/standards/development-standards.md` | Java/Spring/transaction/test standards |
| 0 | `docs/standards/documentation-standards.md` | Current-state documentation contract |
| 0 | `docs/standards/glossary.md` | Domain terminology |
| 1 | `docs/policies/security-policy.md` | URL, role, secret, provider-ID boundaries |
| 1 | `docs/policies/quality-gates.md` | Closure and environment-evidence classification |
| 2 | `docs/design/remaining-remediation-design-20260716.md` | Accepted remediation contract and closure |
| 2 | `docs/design/api-spec.md` | API v25 and admin support-reference contract |
| 2 | `docs/design/payment-operations-runbook.md` | Bounded reconciliation and operator behavior |
| 2 | `docs/ui/atstudio-front-list.md` | Screen-count source of truth |
| 2 | `docs/client/testing-guide.md` and included client sources | Client acceptance contract |

## Branch and Safety Boundaries

| Item | Evidence |
|---|---|
| Worktree | `C:/Users/jm991/Desktop/project/ATStudio` |
| Branch | `codex/p1-acceptance-hardening` |
| Client worktree | `C:/Users/jm991/Desktop/project/ATStudio-client-demo-stable`; read-only integrity check only |
| DB/provider/secrets | Not accessed or mutated |
| Runtime | Client branch/runtime not modified or restarted |
| Git mutation | No stage, commit, push, merge, reset, restore, clean, or branch switch |
| Product invariants | Full public playback; gated download; recurring billing-key card payment; single-server topology preserved |

## Closure Matrix

| Finding | Before risk | Changed contract | Code/test/document pointers | Status |
|---|---|---|---|---|
| F-016-01 | Host-only URI check accepted unsafe schemes and raw persisted links reached users/admins. | HTTPS only; no userInfo; only default/443 port; normalized exact/subdomain `youtube.com`; retained unsafe values are text-only. | `WhitelistChannelService.java:205`; `safeYoutubeUrl.ts`; `WhitelistChannelServiceTest`; `WhitelistChannelManagePage.render.test.tsx`; `docs/design/usecase/whitelist.md`; `security-policy.md` | CLOSED |
| F-016-02 | Multiple correction commands could capture and later apply stale state. | Lock subscription at create, reject matching non-terminal correction, compare captured state at execute, retain terminal idempotency. | `AdminPaymentEntitlementCorrectionService.java:112,199`; `PaymentEntitlementCorrectionRepository.java`; `AdminPaymentEntitlementCorrectionServiceTest` | CLOSED |
| F-016-03 | Provider reconciliation did not inspect recent locally DONE orders. | Cursor/age/run-bounded recent DONE lookup, explicit mismatch classes, locally completed refund/cancel exclusion, incident deduplication, no double provider check in one run. | `PaymentOrderRepository.java:83`; `PaymentReconciliationService.java:131-188,385-430`; `PaymentProperties.java`; `PaymentReconciliationRecoveryIntegrationTest`; payment runbook/config docs | CLOSED; live provider evidence ENVIRONMENT-CONDITIONAL |
| F-016-04 | Album/playlist metadata and delete paths could bypass mutation locks. | All mutations use pessimistic parent locks and one parent-before-membership order. | `AlbumService.java:220`; `PlaylistService.java:289,313`; repositories; service tests; `AlbumPlaylistMutationLockContractTest` | CLOSED; retained-MySQL concurrent proof ENVIRONMENT-CONDITIONAL |
| F-015-01 | BUSINESS ADMIN could reach subscriber certification self-service. | Self-service requires USER plus BUSINESS; review endpoints remain ADMIN. | `SecurityConfig.java`; `CompanyCertificationControllerTest`; `CompanyCertificationSecurityVerificationTest` | CLOSED |
| F-016-05 | Any subscription lookup failure became inactive and stale results could win. | Loading/active/inactive/error taxonomy; only structured no-active response means inactive; abort/generation fence. | `PlayerBar.tsx:98`; `PlayerBar.test.tsx` | CLOSED |
| F-016-06 | Four route/page loaders allowed old success/failure to overwrite current state. | Abort plus generation fencing; cancellation is not rendered as an error. | `TrackDetailPage.tsx:42`; `UserManagePage.tsx:59`; `UserSubscriptionManagePage.tsx:80`; `DownloadQueuePage.tsx:115`; corresponding tests | CLOSED |
| F-016-07 / F-015-05 | Existing admin financial operations lacked focused interaction/controller proof. | Local-ID payload wiring, explicit confirmation, busy single-submit, visible failure, and one current-view refresh are tested. | `PaymentReadOnlyPage.test.tsx`; `AdminPaymentControllerTest`; payment read-only focused backend tests | CLOSED |
| WI-013 storage risk | Journal completion/compensation/retry paths lacked direct reachable-risk tests. | Completion, compensation retry, and idempotency are explicitly covered. | `StorageMutationJournalServiceTest` | CLOSED |
| F-015-02 | Admin DTO/UI exposed exact provider payment/refund/settlement identifiers despite existing masked-reference policy. | Raw keys remain entity/server-operation fields; responses and UI expose deterministic `REF-` SHA-256-derived support references only; mutations use local IDs. | `ProviderSupportReference.java`; all `AdminPayment*Response` DTOs; `AdminProviderIdentifierContractTest`; `ProviderSupportReferenceTest`; `admin.ts`; `PaymentReadOnlyPage.tsx`; API/security/payment/client/PDF docs | CLOSED |
| F-016-08 | OAuth lost safe deep-link return state and could not prove replay/stale rejection. | Per-state attempt record, ten-minute TTL, callback consume-once, one-time profile continuation, safe internal fallback. | `oauthAttempt.ts`; `oauthAttempt.test.ts`; `LoginPage.tsx:186`; `SocialLoginPage.tsx:33`; `SocialCompleteProfilePage.tsx:141`; OAuth page tests; user-info/API docs | CLOSED |
| F-016-09 / F-015-P3-03 | Router/frontend standard owned stale fixed screen numbers. | Runtime comments and standards defer to the live UI counting inventory; managed count remains 193. | `router/index.tsx:139`; `frontend-standards.md`; `atstudio-front-list.md`; `docs/index.md` | CLOSED |
| F-015-P3-02 | Playlist deletion wording implied the wrong persistence behavior. | Membership rows are physically deleted; the parent playlist is soft-deleted. | `docs/design/usecase/sound-playlist.md:178-184` | CLOSED |

## Regression Found During Full Verification

The first full backend run exposed four failures in `PaymentReconciliationRecoveryIntegrationTest`: a non-terminal order finalized to DONE was immediately selected again by the new recent-DONE phase in the same run, causing duplicate provider lookups. `PaymentReconciliationService` now records provider-checked order IDs and excludes them from the same run's DONE phase. The focused reconciliation tests and a second clean full backend run passed.

## Focused Verification

| Command / group | Result |
|---|---|
| Selected backend security, financial, lock, provider-ID, controller, and storage-journal tests | PASS |
| `gradlew.bat test --tests PaymentReconciliationRecoveryIntegrationTest --tests PaymentReconciliationServiceTest --tests PaymentReconciliationTransactionServiceTest --console=plain` | PASS after duplicate same-run lookup fix |
| Frontend accepted-finding group | PASS, 13 files / 84 tests |
| Admin payment read/mutation focused group | PASS, 8 tests |
| OAuth focused group | PASS, 4 files / 33 tests |
| Player and stale-loader focused group | PASS, 5 files / 25 tests |

## Complete Quality Gates

| Gate | Final result |
|---|---|
| `gradlew.bat clean test jacocoTestReport --console=plain` | PASS; 151 XML suites, 1,079 tests, 0 failures, 0 errors, 9 skipped |
| Backend JaCoCo | Instruction 76.74%; branch 59.85%; line 77.47%; method 77.41%; class 88.49% |
| `gradlew.bat build --console=plain` | PASS |
| `npm audit --omit=dev --json` | PASS; 0 vulnerabilities, 40 production dependencies |
| `npm audit --json` | PASS; 0 vulnerabilities, 370 total dependencies |
| `npm run typecheck` | PASS |
| `npm run lint` | PASS |
| `npm test -- --run` | PASS; 43 files / 230 tests |
| `npm run test:coverage` | PASS; statements 38.64%, branches 38.64%, functions 32.35%, lines 39.90% |
| `npm run build` | PASS; Vite 6.4.3, 266 modules, main JS 341.42 kB / gzip 111.16 kB |
| `npx prettier --check . --ignore-unknown` | PASS; complete frontend tree |
| Documentation managed count | PASS; 193 documents excluding category index files |
| `validate_docs.py` | PASS |
| Deterministic PDF verifier and 12-page render QA | PASS; 275/275 source segments, title `AT.M 클라이언트 테스트 가이드`, SHA-256 `dfdfc587168aaa45786dc15e2fbf4eb9afb4c07f8d371e2a1a73679ad56e8369` |
| `git diff --check` | PASS; line-ending notices only |
| `frontend/tsconfig.tsbuildinfo` | Restored from WI-014 byte baseline; 5,421 bytes; SHA-256 `B6A42AD2CD32A5AD04D06C55B8B1B26DD9B1894AFDAF7B508DFF16EF0C60F22A` |

## Compiler Warning Disposition

`compileJava --rerun-tasks --warning-mode all` is clean. `compileTestJava` emits 24 unchecked warnings from test harness generics: raw Spring `RestClient` Mockito stubs in `OAuth2ServiceTest`, raw `Specification` matchers in `QuestionServiceTest`, and a generic `ArgumentCaptor` in `StorageMutationCoordinatorTest`. The source is identified and bounded to tests; broad test-harness refactoring is outside this risk-targeted WI.

## Documentation and PDF Provenance

- API specification is v25 and uses `providerReference`, `receiptReference`, `providerSettlementReference`, and `providerRefundReference` rather than raw operation identifiers.
- Payment overview/feature inventory/design/runbook/config document bounded recent-DONE reconciliation, local refund/cancel exclusion, and same-run behavior.
- Security, whitelist, OAuth/user-info, playlist, UI counting, SR/client acceptance, registry, and remediation documents match the final code.
- `output/pdf/atstudio-client-testing-guide.manifest.json` records the seven ordered sources, source hashes, deterministic generator/runtime, verifier result, title, 12 pages, and final PDF hash.
- All 12 rendered pages under `tmp/pdfs/wi017-client-guide/` were visually checked for clipping, overlap, broken Korean, and source omission.

## Environment / Policy Boundaries

| Boundary | Status | Required evidence before unconditional production release |
|---|---|---|
| Retained MySQL migration, lock races, and EXPLAIN/index behavior | ENVIRONMENT-CONDITIONAL | Apply/rehearse approved migrations against a disposable retained-DB copy and run the MySQL profile |
| Live Toss/provider reconciliation, refund, and callback behavior | ENVIRONMENT-CONDITIONAL | Controlled sandbox/live-provider evidence with non-secret logs |
| Trusted proxy/CORS/external callback and secret rotation | ENVIRONMENT-CONDITIONAL | Deployment configuration inspection and acceptance smoke evidence |
| Symlink/canonical-path host behavior | ENVIRONMENT-CONDITIONAL | Target-host filesystem proof |
| Frozen client branch dependency state | ENVIRONMENT-CONDITIONAL | Explicit promotion/update decision; this WI did not alter the client worktree |
| Social-only withdrawal | POLICY-PENDING | Product/identity policy approval and a separate REQ |

## Release-Readiness Judgment

All accepted P1/P2 code findings in WI-017 are closed and the development branch passes its complete local automated gates. The branch is ready for controlled environment acceptance once the named environment evidence is supplied. It is not claimed as unconditionally production-ready or promoted to the frozen client branch.

## Risks / Rollback

- Roll back by finding and file group only; do not revert prior WI-004 through WI-016 work or unrelated dirty files.
- URL rollback: whitelist validator, safe-link utility, and their focused tests/docs.
- Financial rollback: entitlement-correction/reconciliation service, repository/config, DTO reference mapper, and their focused tests/docs as coherent groups.
- Frontend rollback: each player/loader/OAuth change with its associated tests; do not restore generated `tsbuildinfo` from Git because the required baseline is the verified WI-014 byte copy.
- Documentation/PDF rollback: current-state Markdown sources plus deterministic PDF/manifest together.

## Final Integrity Statement

No files were staged or committed. No branch was switched or merged. No DB/data/provider/secret was accessed. The frozen client worktree/runtime was not modified or restarted. Runtime logs and unrelated accumulated changes were preserved.

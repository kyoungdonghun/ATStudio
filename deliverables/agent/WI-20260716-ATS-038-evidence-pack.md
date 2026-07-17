# Evidence Pack: WI-20260716-ATS-038

## Summary

WI-034 through WI-037 were reconciled into one non-mutating V1 residual-code disposition and destructive-approval manifest. The four audits contributed 101 normalized source decision units, which were deduplicated into 56 integrated rows: 13 `KEEP`, 16 `REMOVE`, 12 `REPLACE`, 3 `ARCHIVE`, and 12 `REVIEW`.

No product code, configuration, SQL, existing document, Git reference, database, generated artifact, worktree, branch, tag, process, or runtime was changed. Execution stopped before the destructive approval gate.

## Scope / DoD Check

- [x] Read the approved `REQ-20260716-ATS-004` and WI-038 handoff.
- [x] Reconciled all disposition units from WI-034, WI-035, WI-036, and WI-037.
- [x] Mapped every normalized source unit to one integrated row.
- [x] Resolved cross-layer play-history, download-queue, payment-route, acceptance, seed, SQL, and generated-artifact overlaps.
- [x] Protected financial/concurrency controls and non-production acceptance safety boundaries from blanket deletion.
- [x] Produced six separately approvable execution bundles.
- [x] Recorded proof-before-change and proof-after-change gates.
- [x] Preserved unresolved external-traffic, profile, DB, and branch-history questions as `REVIEW`.
- [x] Stopped before any deletion, replacement, archive mutation, DB operation, Git mutation, or runtime action.

## Reference Documents

| Tier | Document | Use |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Approval, traceability, simplicity, financial integrity |
| 0 | `docs/standards/development-standards.md` | Evidence, testing, DB and frontend verification standards |
| 1 | `docs/policies/security-policy.md` | Secret/PII and financial-support data boundaries |
| 1 | `docs/policies/versioning-policy.md` | Breaking-change, deprecation, and archive requirements |
| 1 | `docs/policies/archive-policy.md` | Live SoT versus historical-record treatment |
| 1 | `docs/policies/execution-policy.md` | Destructive-operation approval gate |
| 1 | `docs/policies/quality-gates.md` | Regression, rollback, and independent-review gates |
| 2 | `docs/design/api-spec.md` | Current API contracts and legacy endpoint lifecycle |
| 2 | `docs/design/db-schema.md` | Current DB claims and manual-patch chain |
| 2 | `docs/design/remaining-remediation-design-20260716.md` | Completed remediation context |
| 2 | `docs/standards/frontend-standards.md` | Active SPA and confirmation-dialog standards |
| 2 | `docs/ui/screen-flow.md` | Current playback, playlist, payment, and environment flows |
| REQ | `deliverables/user/REQ-20260716-ATS-004.md` | Approved V1 baseline direction and stop gate |
| WI | `deliverables/agent/WI-20260716-ATS-034-evidence-pack.md` | Backend/security audit |
| WI | `deliverables/agent/WI-20260716-ATS-035-evidence-pack.md` | Frontend audit |
| WI | `deliverables/agent/WI-20260716-ATS-036-evidence-pack.md` | DB/configuration audit |
| WI | `deliverables/agent/WI-20260716-ATS-037-evidence-pack.md` | Documentation/repository audit |

## Integration Rules

1. One integrated row represents one independently approvable runtime or repository decision, not one file.
2. A current integrity control remains `KEEP` even when its name contains terms such as fallback, recovery, fence, lease, or reconciliation.
3. A repository-internal negative search proves only the absence of checked-in callers. It does not prove that public API traffic, bookmarks, provider callbacks, or external scripts are absent.
4. Conflicting source classifications default to `REVIEW` unless a current product policy and concrete consumer evidence resolve the conflict.
5. `REMOVE` with a dependency is not permission to delete early. Its proof dependency must pass first.
6. Historical REQ/WI/SR/audit/retrospective records remain immutable current-history evidence. Live SoT documents are updated in place.
7. No row in this document is executable until the user approves its bundle and any linked `REVIEW` decisions.

## Integrated Count

| Disposition | Integrated rows | Meaning |
|---|---:|---|
| `KEEP` | 13 | Current behavior, safety boundary, or historical evidence to protect |
| `REMOVE` | 16 | Obsolete source/artifact/reference, subject to its proof gate |
| `REPLACE` | 12 | Current path remains, but implementation or identity must be consolidated |
| `ARCHIVE` | 3 | Preserve evidence outside active SoT or mark it archived in place |
| `REVIEW` | 12 | Evidence or policy decision is still insufficient for destructive action |
| **Total** | **56** | Deduplicated execution units |

## KEEP Manifest

| ID | Exact target / consumer | Rationale and protected invariant | Confidence | Verification gate | Source |
|---|---|---|---|---|---|
| INT-K01 | Payment idempotency, provider-attempt claims, local finalization, reconciliation/incidents, operation audit logs, refund and billing-key cleanup lease/fence, pessimistic/optimistic locks, state machines, storage mutation recovery | These prevent duplicate provider operations, stale commits, lost updates, unaudited operator mutations, and storage drift. Refactoring must preserve equivalent invariants. | High | Existing idempotency, concurrency, lease-expiry, reconciliation, audit, state-transition, optimistic-lock, and recovery suites | ATS-BE-017; WI036 current integrity fields |
| INT-K02 | `AcceptanceStartupGuard`, `AcceptanceProperties`, `AcceptancePublicUrls`, `AcceptanceHostFilter`, acceptance CORS/trusted-client controls, `frontend/vite.config.ts` acceptance ingress, `scripts/acceptance/**` | Non-production public testing safety boundary, not an authentication bypass. Production refusal, exact host/origin handling, forwarding-header stripping, and secret validation must remain. | High | Guard tests; production-profile refusal; allowed-host/CORS tests; lifecycle dry run and isolated acceptance smoke | ATS-BE-015; F035-K01; WI036-K04; WI037-TOOL-01 |
| INT-K03 | `frontend/src/utils/oauthAttempt.ts` and Login/Social Login consumers | Current OAuth state, PKCE lifetime, and safe return-path validation. | High | OAuth success, state mismatch, expiry, and unsafe-return tests | F035-K02 |
| INT-K04 | `safeStorage`, `loadError`, AbortController/generation fences, company-guide and valid missing-data fallbacks | Handles browser quota/access failure, stale responses, cancellation, network failure, and unavailable optional data. | High | Storage-denied, request-order, cancellation, and error-render tests | F035-K03; F035-K05 |
| INT-K05 | `ProtectedRoute`, `SubscriberRoute`, `usePublicCapabilities` | Current authentication, role, business-member, subscription, OAuth, and email-capability boundaries. | High | Role-by-route and capability tests | F035-K04 |
| INT-K06 | `/subscriptions/checkout`, checkout success/fail, billing-agreement APIs and recurring charge flow | Current card recurring-subscription path. It is the replacement target for all one-time subscription compatibility surfaces. | High | Checkout, billing auth, first charge, re-registration, upgrade, renewal, cancellation tests | F035-K06 |
| INT-K07 | Whitelist/company-certification status, audit, export, plan-limit, and private-document flows; current `company_certifications.document_path` consumer | Current product workflow and concurrency/audit boundary. `document_path` remains actively written and is not part of this cleanup. | High | Transition, limit, export, private-download, optimistic-lock, and audit tests | F035-K07; WI036 current-field guard |
| INT-K08 | Player store, browser-local `playHistory`, full-track playback, waveform flat-line fallback, Zustand stores | Current public listening policy and browser-local history remain. Only the obsolete server history stack is removed. | High | Full-duration playback, progress/waveform, local-history cap/dedup, autoplay/network fallback tests | F035-K08 |
| INT-K09 | `src/main/resources/application.yml` path, `src/test/resources/application.yml`, JPA/MySQL/H2 Gradle dependencies | Base/test configuration and DB stack remain. Targeted settings inside the base path are handled separately by `INT-V07` and `INT-V08`. | High | Backend test/build; production-shaped startup against disposable MySQL | WI036-K01; WI036-K02; WI036-K03 |
| INT-K10 | `deliverables/**`, `docs/SR/**`, `docs/audit/**`, `docs/retrospective/**`, dated SR addenda | Historical records preserve their original context and must not be rewritten as current SoT. | High | Archive-policy review; docs validation; no historical normalization diff | DOC-13 and WI037 historical KEEP inventory |
| INT-K11 | `output/pdf/atstudio-client-testing-guide.pdf`, its manifest, `scripts/docs/generate_client_testing_pdf.py`, `scripts/docs/verify_client_testing_pdf.py` | Tracked current client deliverable with reproducible generator/verifier. | High | PDF manifest/hash and visual verification | ART-09 |
| INT-K12 | `codex/p1-acceptance-hardening`; tags `v1-pre-consolidation-dev-20260716`, `v1-pre-consolidation-client-20260716` | Official V1 branch candidate and rollback anchors. | High | Commit/tag reachability before and after branch cleanup | REP-01; REP-06 |
| INT-K13 | `scripts/demo/seed-client-demo.mjs` and explicit manifest-scoped demo workflow | Client-demo population is an active, explicit non-baseline workflow. Generated WAV/PNG output is not retained. The PowerShell credential path defect is replaced under `INT-P12`. | Medium-High | Seed/verify/cleanup on disposable acceptance data; zero baseline invocation | WI036-V03 resolved by active workflow evidence; WI037-TOOL-02 |

## REMOVE Manifest

| ID | Exact target | Caller/consumer evidence | Dependency and deletion impact | Proof before / after | Confidence | Bundle | Source |
|---|---|---|---|---|---|---|---|
| INT-R01 | `PlayHistoryController`, service, repository, entity, DTOs/tests, `play_histories`; `frontend/src/api/playHistory.ts`; `PlayHistory` type; cleanup references; current API/schema docs | Active SPA writes `localStorage.playHistory`; no frontend server-history importer. Current product policy is browser-local history. | Remove backend, frontend, table, tests, cleanup references, and live docs atomically. Preserve `INT-K08`. | Before: exact external-client waiver/approval and table/storage irrelevance. After: negative `/play-histories` search, full player/history tests, fresh-schema validation. | High for checked-in runtime; medium external-call risk | B | ATS-BE-003; F035-R02; DOC-10 |
| INT-R02 | `DownloadQueueController`, service, repository, entity/ID/DTO/tests, `download_queue`; `frontend/src/api/downloadQueue.ts`; `DownloadQueueItem`; cleanup references | Wrapper has no importer; active page uses `/downloads/history`. | Remove only the obsolete queue contract/model. Do not remove the active history screen; its route/name is `INT-P06`. | Before: external-client waiver/inventory. After: negative old API/model search, download authorization/accounting/history tests, fresh-schema validation. | High checked-in; medium external-call risk | B | ATS-BE-004; F035-R01 |
| INT-R03 | Deprecated four-argument `PaymentCommandTransactionService.finalizeUpgrade` overload | All checked-in callers use the active three-argument method. | Remove only the overload; preserve claim/fence/state validation. | Exact symbol search before/after; compile; payment upgrade/reconciliation tests. | High | A | ATS-BE-005 |
| INT-R04 | `Track.previewFile`, `tracks.preview_file`, `StorageReferenceChecker` preview reference, fixtures/tests/current docs | Current policy and tests use the original full track; no preview path consumer remains. | Entity, schema, storage checker, tests, and docs change atomically. Existing storage inventory is unnecessary after approved fresh DB, but full-play behavior must remain. | Full playback/progress/waveform/storage tests; negative field/column search; fresh-schema validate. | High | B | ATS-BE-007; WI036-R10; DOC-12 component |
| INT-R05 | `WhitelistExportItem.userIdSnapshot`, `userNicknameSnapshot`; matching DB columns and legacy documentation | Current export builder/CSV omits both and retains email/channel/plan evidence. | Remove entity/schema/test/docs fields without weakening immutable export evidence. | Export byte-stability, deletion-after-export, CSV, schema and negative-field tests. | High | B | ATS-BE-008; WI036-R11; DOC-12 component |
| INT-R06 | Thymeleaf settings in `application.yml`; active docs that claim a runtime compatibility layer | No Thymeleaf dependency, templates, or SSR controller. SPA forward is separate and must remain. | Remove only inert settings and stale live wording. | Dependency report, backend startup, direct SPA deep-link smoke. | High | A | ATS-BE-009; DOC-09 resolved by backend evidence |
| INT-R07 | `SecurityConfig` matcher for `PUT /api/settings/*` | Actual mutation is `/api/admin/settings/*`, already protected by `/api/admin/**`; no old endpoint exists. | Remove only the stale matcher; keep public GET and admin catch-all. | Anonymous/user/admin authorization tests on real routes; negative matcher search. | High | A | ATS-BE-010 |
| INT-R08 | `frontend/src/components/ui/DataTable.tsx`, CSS, stale standards example | No runtime/test import. | Remove component/CSS and update the example. | Negative import search; typecheck, ESLint, Prettier, Vitest, build. | High | A | F035-R03 |
| INT-R09 | Unused frontend exports: `fetchUser`, `fetchSubscriptionPlanDetail`, `fetchAdminUserSubscriptionDetail`, `addTracksToPlaylistBatch`, `cancelMyBillingAgreement`, frontend-only `PaymentProvider` type | Exact searches find definitions only. Corresponding backend endpoints are not authorized for removal by this row. | Remove exports/imports/types only; backend decisions stay under `INT-V05` and `INT-V07`. | Negative symbol search; full frontend gates. | High | A | F035-R04 |
| INT-R10 | `PlaylistCreatePage`, `/playlists/new` adapter/lazy import/test/live docs | No checked-in route producer; normal creation uses the list modal. | Old bookmarks stop resolving. Approved no-legacy V1 policy is the basis; preserve modal creation. | Explicit bookmark-compatibility approval; playlist modal/navigation tests; negative route search. | Medium-High | B | F035-R05 |
| INT-R11 | `frontend/src/features/.gitkeep`, `frontend/src/hooks/.gitkeep`, `frontend/public/.gitkeep` | Empty placeholders; `hooks` already has a real file. | Remove placeholders and update stale directory-structure prose. | File inventory and docs validation. | High | A | F035-R06 |
| INT-R12 | All nine `src/main/resources/db/manual/*.sql` files listed in WI-036 | Each upgrades a retained DB; final DDL shape exists in current `schema.sql`. No DB data is to be preserved. | Remove only after `INT-P01` through `INT-P05` pass and active tests/live docs stop naming patches. Git history remains rollback evidence. | Disposable fresh-DB proof, exact index/ENUM/FK manifest, full MySQL race tests, zero active filename references. | High conditional | C | WI036-R01 through WI036-R09 |
| INT-R13 | `output/demo-seed/`, `tmp/`, expanded `output/client-demo-screenshots-20260716-140514/`, worktree copy `.codex-remote-attachments/` | Generated/reproducible or duplicate workspace output; tracked source/ZIP or original attachment exists. | Confirm ZIP/original/source ownership immediately before deletion. Do not delete tracked PDF assets. | File counts/hashes before; targeted paths absent after; Git status/ignore checks. | High conditional | D | ART-01; ART-02; ART-03; ART-05 |
| INT-R14 | 35 prunable `.claude/worktrees/*` registrations and corresponding merged `claude/*` local branches listed in WI-037 | Gitdir paths do not exist; all point to merged commit `fec16f1`. | Preserve `INT-K12` tags; prune metadata before branch deletion. | `git worktree prune --dry-run`; branch merge/reachability; final worktree/branch inventory. | High | E | REP-02 |
| INT-R15 | Merged local branches `codex/p0-release-blockers`, `codex/payment-integration-clean`, `dev/kyoung` | Fully merged; no required independent history identified. | Local deletion only; remote deletion remains out of scope. | Full commit reachability and tag check before/after. | High | E | REP-04 |
| INT-R16 | Worktrees/branches `codex/acceptance-preview` and `codex/client-demo-stable` | Both fully merged; client-demo currently owns the public runtime. | First cut over and verify runtime on `INT-K12`; then stop owned old processes and remove auxiliary worktrees/branches. | Local/public page and API 200 on official runtime; process paths; worktree lock check; branch reachability. | High conditional | E | REP-03 |

## REPLACE Manifest

| ID | Current target | Replacement target | Dependency / impact | Verification | Confidence | Bundle | Source |
|---|---|---|---|---|---|---|---|
| INT-P01 | Billing-key V1 envelope and legacy `billing.encryption-secret` property | V2 key-ID/key-ring envelope only | Requires fresh official DB proof with no retained `v1:` ciphertext. Never weaken V2 authenticated encryption, key selection, fingerprinting, or negative startup. | Fresh-DB data assertion; V2 encrypt/decrypt/rotation; missing-key and wrong-key startup/decrypt tests; security review. | High conditional | C | ATS-BE-006; WI036-R12; DOC-12 component |
| INT-P02 | `schema.sql` with `IF NOT EXISTS`, stale v13/38-table comments, hidden compatibility behavior | Fail-closed V1 fresh-only schema at the same path with correct 41-table metadata | Preserve dependency order and explicit external application. Second application must fail rather than masquerade as migration. | Apply once to verified-empty MySQL 8; information-schema manifest; second apply fails; `ddl-auto=validate`. | High | C | WI036-P01 |
| INT-P03 | Mixed `seed.sql` plus duplicate `AcceptanceSubscriptionPlanBootstrapRunner` ownership | One minimal deterministic baseline-data owner for six plans; demo/QA fixtures remain explicit and separate | Must not create demo users/tracks/albums/tags/notices in production baseline. Runner may become validator or be removed after ownership transfer. | Exactly six plans; no fixture rows; acceptance bootstrap proved separately. | High | C | WI036-P02; WI036-P06 |
| INT-P04 | `application-acceptance.yml`, `application-local.example.yml`, startup billing checks, acceptance lifecycle inheritance/allowlist | Isolated acceptance/local profiles with `validate`, bootstrap off by default, complete V2 key-ring inputs, actual recurring-provider checks, and no ignored-local inheritance | Preserve `INT-K02`; do not copy secrets. Existing public demo startup behavior changes and needs controlled rehearsal. | Config binding tests; production refusal; isolated start/status/stop/test; CORS/host/callback smoke; secret scan. | High | C | WI036-P03 through WI036-P05; WI036-P07 |
| INT-P05 | WI-specific `PaymentMysqlSchemaValidationTest` and concurrency integration setup | Guarded, reusable V1 MySQL proof suite | Preserve validated race semantics while removing WI-specific DB names/helper assumptions. | Disposable-name guard, structural manifest, seven payment races plus certification/download/whitelist/storage/waveform contracts. | High | C | WI036-P08 |
| INT-P06 | `/download-queue`, `DownloadQueuePage.*`, lazy symbol and active Header/Profile producers | `/downloads`, `DownloadHistoryPage.*` with no legacy redirect unless policy changes | Coordinate after obsolete queue removal; bookmark path changes. | Header/Profile navigation, pagination/back-forward, bulk download, negative old-route references. | Medium-High | B | F035-P02 |
| INT-P07 | `PaymentReadOnlyPage.*` source/test identity | `PaymentOperationsPage.*` or approved canonical name; keep `/admin/payments` | Source identity only; no behavior change. | Typecheck, page tests, admin payment smoke. | High | B | F035-P03 |
| INT-P08 | Ten production `window.confirm` calls in payment and whitelist pages | Controlled `ConfirmDialog` with pending, cancel, focus, and double-submit handling | Preserve warning text and exact mutation count. | Focus/escape/cancel/confirm tests; call-count tests; admin/user smoke. | High | B | F035-P04 |
| INT-P09 | `mock*` CSS classes/comments in recurring Toss checkout | Provider-neutral checkout/status names | Presentation-name cleanup only. | Typecheck, payment tests, visual smoke. | High | B | F035-P05 |
| INT-P10 | Tracked `frontend/tsconfig.tsbuildinfo`; incomplete targeted ignores | Stop Git tracking while preserving ignored local cache; add narrow ignore rules for approved generated paths/logs | Do not ignore all `output/`; tracked PDF/manifest remain visible. Preserve current file hash until approved cleanup. | `git ls-files`, `git check-ignore`, two consecutive build/status checks, PDF tracked check. | High | D | F035-P06; ART-07; ART-08 |
| INT-P11 | Fourteen live documents identified by DOC-01 through DOC-06, plus cross-WI compatibility statements when their owning row executes | Current V1 SoT: single front matter, verified lifecycle status, official branch/runtime, fresh-only DB and current API/UI facts | Update in place; preserve paths and all historical REQ/WI/SR/audit snapshots. This row includes the nine files grouped by DOC-06. | `validate-docs`, index/status checks, API/DB/screen counts, negative stale-branch/version/manual-SQL wording search. | High conditional | D | DOC-01 through DOC-06; DOC-09 and DOC-12 live-document consequences |
| INT-P12 | `scripts/demo/seed-client-demo.ps1` hard-coded acceptance-preview credentials path | Explicit credentials parameter or official acceptance runtime-root resolution | Keep explicit demo workflow (`INT-K13`); never commit credentials. | Dry run without default secret path; explicit-path seed/verify/cleanup; secret scan. | High | D | WI037-TOOL-03 |

## ARCHIVE Manifest

| ID | Target | Archive action | Verification | Confidence | Bundle | Source |
|---|---|---|---|---|---|---|
| INT-A01 | `docs/design/remaining-remediation-design-20260716.md`; `docs/design/p1-security-acceptance-hardening-design.md` | Archive in place with date, reason, notice, replacement path, and index update. Do not move; 79 reference files depend on these paths. | `validate-docs`; reference count; archived metadata and replacement-path check. | High | D | DOC-07; DOC-08 |
| INT-A02 | Historical disposable MySQL helper/log bundles under WI-021 and WI-007 evidence | Preserve as historical evidence only; do not use as V1 runtime/bootstrap source. | Archive-policy classification and live-reference negative search. | High | D | WI036-A01 |
| INT-A03 | `output/client-demo-screenshots-20260716-140514.zip` | Retain outside active SoT as preliminary historical capture with recorded SHA-256; do not present the ephemeral URL as current. | ZIP 52-entry integrity and SHA-256 `6C56C9139616A7936BA596DED6A2F35A8A29D6762DC210497EECDAE3437B09E8`. | High | D | ART-04 |

## REVIEW Manifest

`REVIEW` rows are not executable. A user decision or missing evidence must first produce a new approved disposition.

| ID | Target / unresolved question | Why evidence is insufficient | Decision or proof required | Default safe state | Bundle | Source |
|---|---|---|---|---|---|---|
| INT-V01 | Legacy one-time payment controller/service/provider/DTO surface and five frontend payment/billing aliases/callbacks | SPA has no producer, but API spec requires supported-client inventory and callback/bookmark telemetry or an explicit waiver. FE says replace; backend/docs say review. | Observe traffic or explicitly waive telemetry, then approve atomic API/route/config/test/doc removal while retaining `INT-K06`. | Keep blocked paths rejecting subscription mutation. | F | ATS-BE-001; F035-P01; DOC-11 |
| INT-V02 | `POST /api/user-subscriptions` blocked direct creation endpoint, DTO/service/test/docs | No SPA caller, but unknown external clients may depend on the explicit `410` contract. | Traffic/client inventory or explicit waiver; OpenAPI removal review. | Keep explicit checkout-required rejection. | F | ATS-BE-002 |
| INT-V03 | `GET /api/utils/subscription-status` | No SPA caller; repository search cannot disprove external/manual consumers. | API owner and traffic decision. | Keep read-only endpoint. | F | ATS-BE-011 |
| INT-V04 | `GET /api/utils/user-type` | Same external-consumer uncertainty as `INT-V03`. | API owner and traffic decision. | Keep read-only endpoint. | F | ATS-BE-012 |
| INT-V05 | `GET /api/user-subscriptions/{id}` admin detail endpoint | Frontend wrapper is unused, but dynamic/external admin clients are unobserved. | Admin workflow inventory and traffic evidence. | Remove only frontend wrapper under `INT-R09`; keep endpoint. | F | ATS-BE-013 |
| INT-V06 | `TestUserBootstrapRunner` and `AcceptanceSubscriptionPlanBootstrapRunner` location inside application artifact | DB audit says protected QA bootstrap is useful; backend audit questions in-artifact mutation. No external provisioning replacement exists. | Choose: retain guarded in-app fixture provisioning, or design external provisioning first. | Keep disabled-by-default, non-production guarded provisioning. | F | ATS-BE-016; WI036-K05 conflict resolved to REVIEW |
| INT-V07 | `PaymentProviderType` values, generic `app.payment.provider`, `subscription_payments.provider` nullability, future `KAKAOPAY`, stale one-time `MOCK`/`TOSS` meaning | Persisted identity, recurring-provider selection, future multi-PG intent, and legacy endpoint ownership are conflated. DB contents were not inspected. | Approve a canonical provider matrix after `INT-V01`; prove fresh-DB producers; decide enum values and NOT NULL together. | Preserve persisted enum compatibility and current Toss recurring path. | F | ATS-BE-018; WI036-V01; WI036-V04; DOC-12 component |
| INT-V08 | Ignored root `application-local.yml` and optional import from base config | Key-only audit found stale modes; secret values were intentionally not read. It may still influence local/public startup. | User-approved local-profile replacement/retirement plan without copying secrets; isolated acceptance proof. | Do not edit/read secret values; isolate via `INT-P04`. | F | WI036-V02; targeted aspect of WI036-K01 |
| INT-V09 | `frontend/package.json` and lockfile version `0.1.0` versus official V1 | Runtime unaffected; release identity policy is undecided. | Choose `1.0.0` or retain private-package `0.1.0`, then keep package/lock parity. | Keep current version. | F | F035-V01 |
| INT-V10 | Branch-only tips: `codex-payment-integration-design` (10), `codex-sr-91-tag-taxonomy-layout` (3), `master` (3) commits | Unique commits have not been semantically reviewed; deleting tips could lose independent history. | Inspect unique commits; tag/preserve needed tips; then approve local deletion. | Keep branches. | F | REP-05 |
| INT-V11 | `cloudflared.err.log`, `cloudflared.out.log`, `frontend/vite.err.log`, `frontend/vite.out.log` | FE audit calls them generated; repository audit confirms processes are active, so immediate deletion may lose current evidence or surprise operators. | After official runtime cutover, confirm process ownership and retention need; then approve deletion/ignore. | Keep while current runtime owns them. | F | F035-R07 conflict with ART-06 |
| INT-V12 | Direct admin subscription update/cancel endpoints and active `UserSubscriptionManagePage` calls | Active emergency control is not dead. Replacing it with an audited adjustment workflow changes operator policy, which REQ-004 excludes from silent cleanup. | Decide whether to retain as documented emergency control or design an audited general adjustment workflow in a separate REQ. | Keep current authorized behavior. | F | ATS-BE-014 |

## Conflict Resolution Ledger

| Conflict | Integrated result | Reason |
|---|---|---|
| Server play history: backend/frontend `REMOVE`, docs `REVIEW` | `INT-R01 REMOVE` | Current API and screen-flow documents explicitly establish browser-local SoT; checked-in consumers agree. External-call risk remains a proof-before-delete gate. |
| Thymeleaf: backend `REMOVE`, live docs `REVIEW` | `INT-R06 REMOVE` | Dependency, template, controller, and runtime evidence all show inert settings; SPA forwarding is separately protected. |
| Payment aliases: frontend `REPLACE`, backend/docs `REVIEW` | `INT-V01 REVIEW` | External callback/bookmark/traffic evidence is absent; no stronger evidence resolves the contract risk. |
| QA bootstrap: DB `KEEP`, backend `REVIEW` | `INT-V06 REVIEW` | It is safely guarded but its ownership location remains an architecture decision. |
| Demo seed: DB `REVIEW`, repository `KEEP` | `INT-K13 KEEP` plus `INT-P12 REPLACE` | Client-demo capture remains an active workflow. Generated data is separate, and only the hard-coded wrapper path requires replacement. |
| Vite/Cloudflare logs: frontend `REMOVE`, repository `REVIEW` | `INT-V11 REVIEW` | Processes are currently active; ownership/retention must be checked at cutover. |
| Billing crypto: backend `REPLACE`, DB `REMOVE` | `INT-P01 REPLACE` | The end state removes V1, but only after a V2-only fresh-DB proof; replacement captures the safety dependency. |
| Admin subscription mutation: backend `REPLACE`, active UI caller | `INT-V12 REVIEW` | It is not dead code, and workflow replacement is a product/operations decision outside silent cleanup. |

## Source Reconciliation

### WI-034 Backend (18 of 18)

| Source ID | Integrated row |
|---|---|
| ATS-BE-001 | INT-V01 |
| ATS-BE-002 | INT-V02 |
| ATS-BE-003 | INT-R01 |
| ATS-BE-004 | INT-R02 |
| ATS-BE-005 | INT-R03 |
| ATS-BE-006 | INT-P01 |
| ATS-BE-007 | INT-R04 |
| ATS-BE-008 | INT-R05 |
| ATS-BE-009 | INT-R06 |
| ATS-BE-010 | INT-R07 |
| ATS-BE-011 | INT-V03 |
| ATS-BE-012 | INT-V04 |
| ATS-BE-013 | INT-V05 |
| ATS-BE-014 | INT-V12 |
| ATS-BE-015 | INT-K02 |
| ATS-BE-016 | INT-V06 |
| ATS-BE-017 | INT-K01 |
| ATS-BE-018 | INT-V07 |

### WI-035 Frontend (22 of 22)

| Source ID | Integrated row |
|---|---|
| F035-R01 | INT-R02 |
| F035-R02 | INT-R01 |
| F035-R03 | INT-R08 |
| F035-R04 | INT-R09 |
| F035-R05 | INT-R10 |
| F035-R06 | INT-R11 |
| F035-R07 | INT-V11 |
| F035-P01 | INT-V01 |
| F035-P02 | INT-P06 |
| F035-P03 | INT-P07 |
| F035-P04 | INT-P08 |
| F035-P05 | INT-P09 |
| F035-P06 | INT-P10 |
| F035-V01 | INT-V09 |
| F035-K01 | INT-K02 |
| F035-K02 | INT-K03 |
| F035-K03 | INT-K04 |
| F035-K04 | INT-K05 |
| F035-K05 | INT-K04 |
| F035-K06 | INT-K06 |
| F035-K07 | INT-K07 |
| F035-K08 | INT-K08 |

### WI-036 DB/Configuration (30 of 30)

The WI-036 ledger did not assign per-unit IDs. The following normalized IDs preserve its exact `5 KEEP / 12 REMOVE / 8 REPLACE / 1 ARCHIVE / 4 REVIEW` count.

| Normalized source unit | Original unit | Integrated row |
|---|---|---|
| WI036-K01 | Base application path | INT-K09; targeted local-import aspect INT-V08 |
| WI036-K02 | Test application path | INT-K09 |
| WI036-K03 | Gradle DB stack | INT-K09 |
| WI036-K04 | Acceptance URL/host/CORS support | INT-K02 |
| WI036-K05 | Explicit non-production QA bootstrap | INT-V06 |
| WI036-R01 | `20260615_align_payment_whitelist_schema.sql` | INT-R12 |
| WI036-R02 | `20260618_company_certification_documents.sql` | INT-R12 |
| WI036-R03 | `20260714_payment_db_integrity.sql` | INT-R12 |
| WI036-R04 | `20260714_storage_mutations_journal.sql` | INT-R12 |
| WI036-R05 | `20260715_track_waveform_data.sql` | INT-R12 |
| WI036-R06 | `20260716_company_certification_integrity_and_audit.sql` | INT-R12 |
| WI036-R07 | `20260716_download_atomicity.sql` | INT-R12 |
| WI036-R08 | `20260716_payment_reconciliation_indexes.sql` | INT-R12 |
| WI036-R09 | `20260716_whitelist_integrity_and_exports.sql` | INT-R12 |
| WI036-R10 | Preview-file compatibility | INT-R04 |
| WI036-R11 | Whitelist legacy snapshots | INT-R05 |
| WI036-R12 | Billing crypto V1/legacy secret | INT-P01 |
| WI036-P01 | Fresh schema | INT-P02 |
| WI036-P02 | Mixed seed | INT-P03 |
| WI036-P03 | Acceptance profile | INT-P04 |
| WI036-P04 | Local example | INT-P04 |
| WI036-P05 | Startup guard recurring-provider validation | INT-P04 |
| WI036-P06 | Plan bootstrap duplicate owner | INT-P03 |
| WI036-P07 | Acceptance lifecycle | INT-P04 |
| WI036-P08 | WI-specific MySQL proof tests | INT-P05 |
| WI036-A01 | Historical disposable MySQL helpers/logs | INT-A02 |
| WI036-V01 | Generic provider selector | INT-V07 |
| WI036-V02 | Ignored local config | INT-V08 |
| WI036-V03 | Demo seed tooling ownership | INT-K13 |
| WI036-V04 | Nullable subscription-payment provider | INT-V07 |

The split notation for WI036-K01 is not double counting: the file path is retained by `INT-K09`, while its optional ignored-local import setting is the independently unresolved configuration decision `INT-V08` already counted as WI036-V02.

### WI-037 Documentation/Repository (31 of 31)

| Source ID | Integrated row |
|---|---|
| DOC-01 | INT-P11 |
| DOC-02 | INT-P11 |
| DOC-03 | INT-P11 |
| DOC-04 | INT-P11 |
| DOC-05 | INT-P11 |
| DOC-06 | INT-P11 |
| DOC-07 | INT-A01 |
| DOC-08 | INT-A01 |
| DOC-09 | INT-P11, gated by INT-R06 |
| DOC-10 | INT-R01 |
| DOC-11 | INT-V01 |
| DOC-12 | INT-P11, content gated by INT-R04, INT-R05, INT-P01, and INT-V07 |
| DOC-13 | INT-K10 |
| REP-01 | INT-K12 |
| REP-02 | INT-R14 |
| REP-03 | INT-R16 |
| REP-04 | INT-R15 |
| REP-05 | INT-V10 |
| REP-06 | INT-K12 |
| ART-01 | INT-R13 |
| ART-02 | INT-R13 |
| ART-03 | INT-R13 |
| ART-04 | INT-A03 |
| ART-05 | INT-R13 |
| ART-06 | INT-V11 |
| ART-07 | INT-P10 |
| ART-08 | INT-P10 |
| ART-09 | INT-K11 |
| WI037-TOOL-01 (`scripts/acceptance/**`) | INT-K02 |
| WI037-TOOL-02 (`seed-client-demo.mjs`) | INT-K13 |
| WI037-TOOL-03 (`seed-client-demo.ps1`) | INT-P12 |

## Approval Bundles

Approval is intentionally separable. Approving one bundle does not approve another, and no bundle is approved by this Evidence Pack.

### Bundle A - Safe Internal Dead Code

- Rows: `INT-R03`, `INT-R06`, `INT-R07`, `INT-R08`, `INT-R09`, `INT-R11`.
- Scope: internal overload, inert configuration, stale matcher, unreachable frontend component/exports, and placeholder files.
- Excludes: public endpoint removal, DB DDL, branches, generated runtime logs.
- Gate: exact negative symbol/import search; backend compile/test/startup; frontend typecheck/ESLint/Prettier/Vitest/build; SPA deep-link and settings authorization smoke.

### Bundle B - Coordinated Cross-Layer Replacement

- Rows: `INT-R01`, `INT-R02`, `INT-R04`, `INT-R05`, `INT-R10`, `INT-P06`, `INT-P07`, `INT-P08`, `INT-P09`.
- Scope: browser-local play history, download-history contract/name, superseded preview and snapshot fields, playlist adapter, and active UI naming/dialog cleanup.
- Gate: approval of bookmark/external-client risk; atomic backend/frontend/schema/test/doc edit; fresh-schema validation; role-based browser smoke.
- Dependency: DB portions of this bundle must be represented in the same fresh baseline proved by Bundle C.

### Bundle C - Fresh V1 DB Baseline

- Rows: `INT-R12`, `INT-P01`, `INT-P02`, `INT-P03`, `INT-P04`, `INT-P05`.
- Scope: one fail-closed schema, one minimal baseline-data owner, V2-only billing crypto, isolated profiles, generalized MySQL proof, and retirement of nine retained-DB patches.
- Blocked by: explicit decisions for `INT-V07` and `INT-V08`; separate approval immediately before creating/applying/dropping disposable MySQL databases.
- Gate: verified-empty unique DB; schema applied once; exact information-schema manifest; six plans only; second application fails; `ddl-auto=validate`; acceptance proof on a separate DB; full MySQL concurrency/contract suite.

### Bundle D - Documents, Tooling, and Generated Artifacts

- Rows: `INT-R13`, `INT-P10`, `INT-P11`, `INT-P12`, `INT-A01`, `INT-A02`, `INT-A03`.
- Scope: live SoT refresh, in-place design archives, demo wrapper path, tracked build cache, targeted ignores, generated workspace cleanup, and screenshot archive handling.
- Blocked by: `INT-V11` for active logs and completion facts from Bundles B/C.
- Gate: docs validation, index/status check, PDF/ZIP integrity, secret scan, exact path status, and two consecutive build/status checks.

### Bundle E - Branch and Worktree Consolidation

- Rows: `INT-R14`, `INT-R15`, `INT-R16`; protect `INT-K12`.
- Scope: 35 prunable registrations/branches, three merged local branches, and two auxiliary worktrees/branches after runtime cutover.
- Blocked by: `INT-V10`; official runtime must be verified before removing client-demo/acceptance worktrees.
- Gate: tag and commit reachability, `git worktree prune --dry-run`, local/public UI and API 200 on official branch, process-path check, final one-worktree/one-official-branch inventory. Remote deletion/push remains out of scope.

### Bundle F - Unresolved Decisions

- Rows: `INT-V01` through `INT-V12`.
- Scope: external API/callback evidence, bootstrap ownership, provider model, ignored local config, package version, unique branch tips, active logs, and admin mutation policy.
- Action: decisions only. Each accepted answer must be converted into an approved implementation disposition before destructive execution.

## Required User Decisions

1. Waive external telemetry for the blocked one-time payment/direct-subscription endpoints, or retain them for an observation window (`INT-V01`, `INT-V02`).
2. Remove or retain the dormant utility/admin detail APIs after an external-consumer decision (`INT-V03` through `INT-V05`).
3. Keep guarded in-application acceptance fixture provisioning, or commission an external provisioning replacement (`INT-V06`).
4. Approve a canonical Toss-recurring-only provider matrix for V1, including enum values, selector ownership, and provider nullability (`INT-V07`).
5. Authorize isolated replacement of ignored local configuration without reading/copying secrets (`INT-V08`).
6. Choose frontend release identity `0.1.0` or `1.0.0` (`INT-V09`).
7. Inspect and preserve/discard the three unique branch tips (`INT-V10`).
8. Retain current runtime logs until cutover, then decide deletion/retention (`INT-V11`).
9. Retain direct admin subscription mutation as a documented emergency control, or design an audited replacement in a separate REQ (`INT-V12`).

## Proof Commands for Later Execution

These are plans, not commands executed by WI-038.

| Area | Proof-before / proof-after command or test |
|---|---|
| Symbol reachability | `rg` exact class, route, endpoint, config key, column, and manual-SQL filename searches before and after |
| Backend | `gradlew.bat clean test jacocoTestReport build --console=plain` |
| Frontend | `npm run typecheck`; `npm run lint`; `npm test`; `npm run build`; Prettier check |
| Docs | `python .agents/skills/validate-docs/scripts/validate_docs.py`; index/status and negative-currentness searches |
| MySQL | Unique disposable DB guard; `information_schema` manifest; apply schema once; reject second apply; `ddl-auto=validate`; MySQL concurrency contracts |
| Git/worktrees | `git worktree prune --dry-run`; `git branch --merged`; `git rev-list --left-right --count`; tag/commit reachability |
| Runtime | Official-branch process path; local/public `/` and `/api/tracks` 200; role-based UI smoke; callback/CORS/host checks |
| Secrets | Scan staged diff and generated logs for credentials, private keys, tokens, raw provider/card/billing-key material |

## Coverage Limits

- No remote fetch was performed; branch conclusions use local and local remote-tracking state only.
- No live API traffic, provider callback traffic, bookmark telemetry, or external-client inventory was observed.
- No MySQL schema/data, V1 ciphertext, provider enum data, or nullable provider rows were inspected.
- No secret values in ignored `application-local.yml` were read or recorded.
- No runtime/profile combination, browser flow, Toss callback, or OAuth callback was executed.
- Historical deliverables were preserved and were not sentence-by-sentence normalized; their role is historical evidence, not current SoT.
- These limits are why `INT-V01` through `INT-V12` remain non-executable.

## Validation Performed by WI-038

- Reconciled source counts: WI-034 `18/18`; WI-035 `22/22`; WI-036 `30/30`; WI-037 normalized `31/31`; total `101/101`.
- Deduplicated count: `56` integrated rows.
- Checked that every integrated ID appears in exactly one primary disposition section.
- Checked that every destructive integrated row belongs to one primary approval bundle or is blocked in Bundle F.
- Read-only Git baseline: branch `codex/p1-acceptance-hardening`, HEAD `a96d2e0c5d249723bbf449b6834299a04cf2ad30`.
- Product/runtime tests were intentionally not run because WI-038 integrates existing evidence and forbids shared runtime/DB inspection.

## Risks / Rollback

### Risks

- Treating repository-negative search as proof of no external callers can break public contracts; those cases remain `REVIEW` or carry an explicit waiver gate.
- Removing resilience fields or locks as legacy residue can cause financial/concurrency corruption; `INT-K01` is a hard protection boundary.
- Applying fresh schema to a non-empty/shared DB would be destructive; Bundle C requires exact disposable-DB guards and separate approval.
- Removing active client-demo worktrees/logs before cutover can break client access; Bundle E requires runtime migration first.
- Rewriting historical evidence as current prose would destroy audit context; `INT-K10` prevents it.

### Rollback

WI-038 changed no product state. Rollback is limited to deleting:

- `deliverables/user/WI-20260716-ATS-038-summary.md`
- `deliverables/agent/WI-20260716-ATS-038-evidence-pack.md`

No code, config, SQL, DB, branch, worktree, tag, generated artifact, process, or runtime rollback is required.

## Stop Gate

**STOPPED BEFORE DESTRUCTIVE ACTION.**

No removal, replacement, archive metadata change, DB initialization/migration, branch/worktree cleanup, staging, commit, push, or runtime restart is authorized or performed by WI-038. The next action is user review of Bundles A through F.

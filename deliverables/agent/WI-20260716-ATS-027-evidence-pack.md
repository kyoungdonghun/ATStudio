---
version: 1.0
last_updated: 2026-07-16
project: ATS
owner: qa-integ
category: evidence
status: complete
related_wi: WI-20260716-ATS-027
---

# WI-20260716-ATS-027 Evidence Pack

## 1. Work Item

| Field | Value |
|---|---|
| WI | `WI-20260716-ATS-027` |
| REQ | `REQ-20260716-ATS-002` |
| Agent | `qa-integ` |
| Branch | `codex/p1-acceptance-hardening` |
| Mode | Independent read-only integration audit |
| Depends on | `WI-20260716-ATS-022` |
| Blocks | `WI-20260716-ATS-028` |
| Verdict | `NEEDS_WI_028_REMEDIATION` |

The audit reconciled the cumulative code, API, schema, UI, operations, client, WI, and worktree surfaces. No product, runtime, database, provider, client-branch, or Git state was mutated. Only the two output-contract files for WI-027 were created. WI-026 completed concurrently; its three findings were independently checked against current source before inclusion below.

## 2. Findings First

### [P1] F-026-01 - Admin list completions are not fenced by request scope

**Impact:** A slow response for an old admin filter or page can replace the current rows and pagination while the controls show the newer scope. An operator can then review or mutate records believing they belong to the selected filter/page.

**Evidence:**

- `frontend/src/pages/admin/WhitelistChannelManagePage.tsx:71-96` commits rows, pagination, editable status state, error state, and loading state for every completion.
- `frontend/src/pages/admin/WhitelistChannelManagePage.tsx:207-212` changes status/page without invalidating the prior request.
- `frontend/src/pages/admin/CompanyCertManagePage.tsx:97-113` has the same unrestricted list completion path.
- `docs/ui/screen-flow.md:62-68` requires list screens to use latest-request-wins behavior.
- `deliverables/user/WI-20260716-ATS-026-summary.md` supplied the concurrent QA-FE finding; WI-027 independently re-read the cited source and confirmed the race.

**Boundary:** This requires overlapping requests that complete out of order. Existing in-order tests and ordinary low-latency use do not refute it.

**WI-028 disposition:** Fence all list side effects by request generation/key or abort the prior request. Add reverse-completion tests for rows, pagination, editable state, error, and loading ownership.

### [P2] F-026-02 - Subscriber whitelist load guard drops overlapping mutation refreshes

**Impact:** A second mutation can succeed while the first post-mutation load is active, have its own `load()` return without scheduling work, and leave stale status, slot usage, or primary-channel state on screen.

**Evidence:**

- `frontend/src/pages/subscriber/WhitelistChannelPage.tsx:80-117` returns immediately when `loadBlocked.current` is true and does not queue a later load.
- `frontend/src/pages/subscriber/WhitelistChannelPage.tsx:174-185` and `:201-250` make every successful mutation rely on `await load()`.
- Per-action busy state does not establish one global mutation/refresh sequence.
- `deliverables/user/WI-20260716-ATS-026-summary.md` supplied the concurrent QA-FE finding; WI-027 independently confirmed the control flow.

**Boundary:** The failure requires overlapping actions and a first refresh that captures state before the second commit. Sequential mutations remain unaffected.

**WI-028 disposition:** Coalesce/queue a final refresh or serialize the mutation-plus-refresh sequence. Add a focused deferred-promise test that proves final server state wins.

### [P2] F-026-03 - Certification detail close is not authoritative during loading

**Impact:** Keyboard and pointer users cannot close the detail modal while its request is pending, and late success can restore content after a close attempt.

**Evidence:**

- `frontend/src/pages/admin/CompanyCertManagePage.tsx:115-126` keeps `detailLoading` true until the request settles and has no generation or abort boundary.
- `frontend/src/pages/admin/CompanyCertManagePage.tsx:129-135` does not invalidate the request or clear `detailLoading`.
- `frontend/src/pages/admin/CompanyCertManagePage.tsx:284-290` derives modal openness from `detailLoading || detail || detailError`.
- `frontend/src/components/ui/Modal.tsx:26-32` and `:130-138` correctly dispatch Escape/close; the caller state immediately keeps the modal open.
- `deliverables/user/WI-20260716-ATS-026-summary.md` supplied the concurrent QA-FE finding; WI-027 independently confirmed the state derivation.

**Boundary:** The defect is visible only while detail loading is pending. Already-settled dialogs close normally.

**WI-028 disposition:** Make close invalidate the pending request and make explicit user-open state authoritative. Test Escape/close before late success and late failure.

### [P2] F-027-01 - CORS does not expose whitelist-export response headers

**Impact:** An allowed cross-origin admin frontend cannot read the export filename or replay batch ID. `Number(undefined)` produces `NaN`, which can enter the UI state and makes the exported batch non-replayable through the displayed control.

**Evidence:**

- `src/main/java/com/atstudio/atstudio/controller/AdminWhitelistChannelController.java:77-85` emits `Content-Disposition` and `X-Whitelist-Export-Batch-Id`.
- `src/main/java/com/atstudio/atstudio/config/CorsConfig.java:41-46` configures origins, methods, request headers, and credentials, but no exposed response headers.
- `frontend/src/api/admin.ts:203-216` reads both response headers and applies `Number(...)` to the batch header without rejecting a missing/non-finite value.
- `frontend/src/pages/admin/WhitelistChannelManagePage.tsx:149-155` writes the returned ID into replay state; `:165-167` only validates the later replay input.
- `docs/design/api-spec.md:3276-3280` and `:3294-3300` make the headers part of the export/replay contract.
- `src/test/java/com/atstudio/atstudio/controller/AdminWhitelistChannelControllerTest.java:51-66` checks raw response headers, while `src/test/java/com/atstudio/atstudio/config/CorsConfigTest.java:12-35` checks configured origins but not exposed headers.
- `frontend/src/api/adminWhitelistChannels.test.ts:23-63` supplies readable headers directly and therefore does not model browser CORS filtering.

**Environment boundary:** Same-origin delivery and the repository's Vite proxy path do not require CORS exposure and remain unaffected. The defect applies when the allowed frontend origin differs from the API origin. A same-origin smoke test cannot close this finding.

**WI-028 disposition:** Add both headers to the exposed-header configuration. Add a backend CORS contract assertion and a frontend adapter assertion that rejects or safely handles absent/invalid batch metadata.

### [P3] F-027-02 - Company-certification API examples are not wire-accurate

**Impact:** The in-repo React UI follows the implementation, but external clients or operators using the examples can parse the wrong shape or assume a PDF-only response for evidence that may be PDF or JPEG.

**Evidence:**

- `docs/design/api-spec.md:322-330` defines the standard single-resource envelope.
- `docs/design/api-spec.md:3317-3337`, `:3390-3412`, and `:3472-3479` show unwrapped certification objects.
- `src/main/java/com/atstudio/atstudio/controller/CompanyCertificationController.java:41-45`, `:67-70`, and `:133-136` return the standard response wrapper.
- `docs/design/api-spec.md:3447-3450` promises `application/pdf`.
- `src/main/java/com/atstudio/atstudio/controller/CompanyCertificationController.java:112-120` returns `application/octet-stream` for the stored evidence.
- `frontend/src/api/companyCerts.ts:29-62` and `frontend/src/api/admin.ts:67-124` correctly unwrap the implemented envelope and consume a generic `Blob`.

**Boundary:** No current in-repo UI failure was identified. This is a published-contract defect.

**WI-028 disposition:** Make all certification examples use the standard envelope and document the binary response as generic evidence content or return a verified stored media type in code and tests.

### [P3] F-027-03 - Service-enabled subscription terminology is inconsistent

**Impact:** Future route or authorization work can regress the intended cancellation grace period if it follows the stale wording.

**Evidence:**

- `src/main/java/com/atstudio/atstudio/repository/UserSubscriptionRepository.java:23-35` accepts unexpired `ACTIVE` or `CANCELLED` subscriptions.
- `frontend/src/router/SubscriberRoute.tsx:13-18` describes active-only access, while `:45-50` accepts the current-subscription response regardless of those two service-enabled states.
- `docs/standards/glossary.md:93` defines subscriber access as active-only.
- `docs/design/usecase/sound-track.md:208-224` cross-references subscriber access with `status=ACTIVE` only.
- `docs/design/usecase/sound-playlist.md` is the aligned canonical example and describes the cancellation grace period.

**Boundary:** Runtime behavior is aligned; this is documentation and code-comment drift.

**WI-028 disposition:** Use one `service-enabled subscription` definition everywhere: `ACTIVE`, or `CANCELLED` before `currentPeriodEnd`.

### [P3] F-027-04 - Reconciliation contract and runbook omit currencies

**Impact:** Local and provider amounts can be interpreted in the wrong currency during manual reconciliation, especially outside the current KRW-dominant path.

**Evidence:**

- `src/main/java/com/atstudio/atstudio/dto/payment/AdminPaymentReconciliationResponse.java:70-106` exposes `localCurrency`, `providerCurrency`, and a masked provider reference.
- `docs/design/api-spec.md:2232-2247` omits both currency fields from the on-demand response example.
- `docs/design/payment-operations-runbook.md:76-89` lists safe local/provider amount fields without their currencies.
- `frontend/src/api/admin.ts:494-559` models persisted incidents, not the on-demand reconciliation response; no current UI consumer closes the documentation gap.

**Boundary:** This is not a demonstrated current UI defect. The persisted incident DTO and on-demand response are distinct shapes and must remain explicitly distinguished.

**WI-028 disposition:** Add both currency fields to the API example and runbook field list, and label the on-demand versus persisted-incident shapes.

### [P3] F-027-05 - Generated TypeScript build information is tracked and dirty

**Impact:** `git add -A` or similarly broad staging includes machine-generated compiler state in the remediation commit.

**Evidence:**

- `frontend/.gitignore:1-8` now includes `*.tsbuildinfo`.
- `frontend/tsconfig.tsbuildinfo` remains tracked and differs from `HEAD` (`HEAD` blob `3c8b761d...`; worktree blob `6be701894...`). Ignore rules do not affect already tracked files.

**Boundary:** This is commit hygiene, not a runtime defect.

**WI-028 disposition:** Exclude the current generated delta. Restore the tracked artifact or intentionally remove it from tracking only as an explicit, reviewed repository-hygiene decision.

## 3. Three-Way Reconciliation

| Surface | Implementation evidence | Contract / operations evidence | UI / WI evidence | Result |
|---|---|---|---|---|
| Admin list request ownership | List loaders commit every completion | UI contract requires latest request to win | WI-026 plus independent WI-027 source check | **Mismatch: F-026-01** |
| Whitelist mutation refresh | Loader drops calls while blocked | Final server state must remain authoritative | Mutation handlers depend on dropped refresh | **Mismatch: F-026-02** |
| Certification modal close | Pending request owns `detailLoading` | Modal close must be authoritative | Caller reasserts open state | **Mismatch: F-026-03** |
| Public full-track listening | `SecurityConfig.java:73-87`; `TrackController.java:82-100`; `TrackService.java:151-158` | `docs/design/api-spec.md:487-507`; `docs/standards/glossary.md:79-80` | `frontend/src/store/playerStore.ts:241`; REQ invariant | Aligned |
| Subscriber download limits | Download controller/service/repository and schema reviewed | API, use-case, client checklist, and REQ invariant reviewed | Download queue and license pages/tests reviewed | Aligned; retained-DB concurrency conditional |
| Recurring billing-key payment | Billing agreement, encrypted key, provider, scheduler, receipt and reconciliation code reviewed | Payment design, policy, runbook, API, and acceptance checklist reviewed | Subscription payment and admin payment surfaces reviewed | Aligned; live provider conditional |
| Single application server | Security/startup/config and storage mutation paths reviewed | Architecture and acceptance documentation reviewed | Vite proxy/client guide and WI evidence reviewed | Aligned; deployment topology conditional |
| Whitelist export/replay | Endpoint and service emit batch metadata | API requires exposed filename and batch ID | UI consumes response headers | **Mismatch: F-027-01** |
| Company certification | Wrapped JSON and generic binary response | Examples show raw JSON and PDF-only type | React follows implementation | **Mismatch: F-027-02** |
| Subscription grace access | Repository accepts `ACTIVE` and unexpired `CANCELLED` | Some references say active-only | Route behavior works; comment is stale | **Mismatch: F-027-03** |
| Payment reconciliation | DTO carries amount/currency/reference | API/runbook omit currencies | No on-demand UI consumer | **Mismatch: F-027-04** |
| Provider support references | Raw provider fields are masked before response | API and security policy describe masked reference | Admin UI displays support-safe fields | Aligned |
| Schema/entity inventory | 41 `CREATE TABLE` declarations; 41 `@Entity` types; exact table-name set equality | DB schema index publishes 41 | WI-022 reports clean validation | Aligned statically; live DB conditional |
| UI inventory | Router has 62 paths, one index route, 54 lazy page modules; 53 visual pages after excluding the modal route adapter | UI index publishes 53 | Route source and UI registry agree | Aligned |
| Project inventory | 24 controllers / 149 mappings; 13 agents; 92 SR entries; 193 managed Markdown files | Published indexes use the same values | WI evidence and direct counts agree | Aligned |

## 4. Worktree Artifact Classification

### 4.1 Snapshot totals

The final audit snapshot has 292 tracked modifications and 296 untracked files. The untracked total includes the two WI-027 outputs and the two WI-026 outputs that completed concurrently during final verification.

### 4.2 Include in the REQ-20260716-ATS-002 candidate set

| Class | Count | Notes |
|---|---:|---|
| Tracked implementation/config/docs/PDF | 291 | All tracked modifications except `frontend/tsconfig.tsbuildinfo`; includes the tracked client testing PDF and its reviewed source changes |
| Untracked product/test/manual SQL | 52 | Backend source/tests/migrations and frontend source/tests |
| PDF tooling/manifest | 3 | `scripts/docs/*` and `output/pdf/atstudio-client-testing-guide.manifest.json` |
| REQ-002 WI/design artifacts | 66 | REQ-002, WI-004 through WI-022 sets, the completed WI-026 set, WI-025/WI-027 handoffs/outputs as currently present, and `docs/design/remaining-remediation-design-20260716.md` |

These are **candidate** counts, not a staging instruction. WI-025 outputs were not present at this snapshot and will increase the REQ-002 artifact count when completed.

### 4.3 Preserve but exclude from the REQ-002 commit

| Class | Count | Examples / reason |
|---|---:|---|
| Separate REQ-003 deliverables | 7 | `REQ-20260716-ATS-003` and WI-023/WI-024 handoff, summary, and evidence files |
| Separate REQ-003 source | 2 | `scripts/demo/*` |
| Demo generated assets | 73 | `output/demo-seed/*` |
| Client screenshots/archive | 53 | `output/client-demo-screenshots-20260716-140514*` |
| Temporary PDF render output | 35 | `tmp/*` |
| Runtime logs | 4 | Cloudflare and Vite stdout/stderr logs |
| Private attachment | 1 | `.codex-remote-attachments/*` |
| Tracked generated artifact | 1 | `frontend/tsconfig.tsbuildinfo` |

Ignored local files such as `application-local.yml`, `frontend/.env.local`, `.gradle/`, `.idea/`, `build/`, `frontend/coverage/`, `frontend/dist/`, `frontend/node_modules/`, `uploads/`, and server logs must remain unstaged.

## 5. Environment-Conditional Boundaries

| Boundary | Current evidence | Required closure |
|---|---|---|
| Fresh MySQL schema/migrations | Static table/entity parity and manual SQL review | Apply to a disposable MySQL 8 database and record migration results |
| Retained DB integrity/concurrency | Source locks, constraints, and prior WI evidence | Execute retained-data migration, concurrent mutation tests, and `EXPLAIN` for new reconciliation indexes |
| Toss billing/refund/callback | Provider adapter, scheduler, masking, and tests reviewed | Use approved sandbox credentials and record callback, renewal, refund, and replay evidence |
| Cross-origin CORS | Static config found F-027-01 | Browser/API test with separate allowed frontend and API origins |
| Trusted proxy/public callbacks | Static security/config review only | Verify forwarded headers, canonical callback URL, and public ingress behavior |
| Secrets and encryption | Environment-variable/startup-guard contracts reviewed | Verify deployment injection and rotation without recording secret values |
| Canonical path/symlink host | Static path controls reviewed | Run deployment-host filesystem checks if symlinks or alternate roots are used |
| Frozen client branch | WI-022 supplied historical evidence for `codex/client-demo-stable` | Revalidate dependency lock and client acceptance state without mutating the client worktree |

WI-023/WI-024 public-demo evidence belongs to REQ-20260716-ATS-003. It is useful operational context but cannot close REQ-002 production or provider gates.

## 6. Commands and Evidence

All commands in this WI were read-only or used Python `-B` to suppress bytecode creation.

| Command / check | Result |
|---|---|
| `git branch --show-current` | `codex/p1-acceptance-hardening` |
| `git status --short --branch` | Correct branch; large shared dirty worktree classified above |
| Controller/mapping source count | 24 controllers, 149 method mappings |
| Schema/entity comparison | 41 tables, 41 entities, exact table-name set equality |
| Router/UI source count | 62 paths, one index route, 54 lazy page modules, 53 visual pages |
| SR index count | 92 entries: 82 DONE, 7 OPEN, 2 NOT CONFIRMED, 1 DROPPED |
| Managed Markdown count | 193 |
| `python -B .agents\skills\validate-docs\scripts\validate_docs.py` | PASS: Tier 0 present, no broken links, 415 supported traceability IDs, all managed docs indexed |
| `git diff --check` | Exit 0; line-ending warnings only, no whitespace error |
| `...python.exe -B scripts\docs\verify_client_testing_pdf.py` | PASS: 12 pages, title verified, 278/278 checks, SHA-256 `afba32cce2460d5d38b80f4a88278e31d1f7344a2258e240bfd61df74f4c6095` |
| PDF manifest/source hash comparison | PASS: 7 source hashes match; PDF hash and 172009-byte size match |

### Supplied or concurrent evidence, not rerun in WI-027

WI-022 reports 1,106 backend tests with 0 failures and 9 skipped, a passing backend build, 44 frontend test files with 242 passing tests, and passing typecheck, ESLint, build, and Prettier checks. Concurrent WI-026 also reports 44 frontend files / 242 tests, 22 focused files / 133 tests, typecheck with incremental output disabled, focused ESLint, full Prettier, and dependency audits passing. WI-027 did not rerun these commands because its no-mutation constraint excludes commands that may write to `build/`, `coverage/`, `dist/`, or tracked `frontend/tsconfig.tsbuildinfo`. Treat those numbers as supplied WI evidence, not WI-027 execution evidence.

## 7. Prescribed WI-028 Validation

Run these only after F-026-01 through F-026-03 and F-027-01 through F-027-05 are dispositioned and the shared worktree is stable:

```powershell
git status --short --branch
git diff --check

gradlew.bat clean test jacocoTestReport
gradlew.bat build

Push-Location frontend
npm run typecheck
npm run lint
npm run test
npm run test:coverage
npm run build
npm run format
Pop-Location

python -B .agents\skills\validate-docs\scripts\validate_docs.py
C:\Users\jm991\.cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe -B scripts\docs\verify_client_testing_pdf.py
```

Then perform the environment-conditional database, provider, CORS, ingress, secret, filesystem, and frozen-client checks listed in Section 5.

Before committing, stage by an explicit allowlist and inspect the staged set:

```powershell
git diff --cached --name-status
git diff --cached --check
git diff --cached -- frontend/tsconfig.tsbuildinfo tmp output/demo-seed output/client-demo-screenshots-20260716-140514 scripts/demo deliverables/user/REQ-20260716-ATS-003.md
```

The last command should show no staged REQ-003/generated/runtime/private artifact. Re-run `git status --short --branch` immediately before commit because the worktree is shared.

## 8. DoD and Handoff

| Requirement | Status | Evidence |
|---|---|---|
| Reconcile code/schema/API/UI/operations/client/WI | Complete | Sections 2-5 |
| Exact pointers and severity ordering | Complete | Section 2 |
| Environment-conditional boundaries | Complete | Sections 2 and 5 |
| Commit/generated/runtime/unrelated classification | Complete | Section 4 |
| Prescribe final validation | Complete | Section 7 |
| Produce both deliverables | Complete | This file and `deliverables/user/WI-20260716-ATS-027-summary.md` |

### WI-028 inputs

1. Ingest the pending WI-025 backend/security review, completed WI-026 frontend review, and this integration review before final disposition.
2. Resolve or explicitly accept F-026-01 through F-026-03 and F-027-01 through F-027-05 with owner and evidence.
3. Preserve REQ-003, generated, runtime, private, ignored, and local artifacts outside the REQ-002 candidate commit.
4. Execute current clean validation and all applicable environment-conditional checks.
5. Do not claim production closure from same-origin demo evidence or supplied historical test results alone.

## 9. Rollback / Recovery

WI-027 changed no product or runtime state. Recovery for these deliverables is limited to reverting these two files. Product remediation proposed for WI-028 should retain focused tests so any CORS or documentation correction can be independently reverted without changing runtime data.

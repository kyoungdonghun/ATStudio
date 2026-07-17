# Frontend QA Re-audit: WI-20260717-ATS-008

## Decision

**BLOCK** - The inspected WI-007 repairs close nine of the ten prior frontend
findings at source/test level, but current executable quality-gate evidence was not
completed in this re-audit. Two additional P3 defects remain in auth refresh
persistence and download-history accessibility, and one P3 API type contract is
narrower than the backend contract.

## Scope and Evidence Boundary

This report reviews the current dirty working tree. No product code, active docs,
database state, Git refs/index, or another report was modified. The user stopped
further investigation before the requested frontend commands were run, so this
report does not reuse WI-006 metrics as current metrics and does not infer a pass
from configuration or test source alone.

Consulted inputs:

- `deliverables/agent/WI-20260717-ATS-008-handoff.md`
- `deliverables/agent/WI-20260717-ATS-007-handoff.md`
- `deliverables/agent/WI-20260717-ATS-006/frontend-qa.md`
- `deliverables/user/REQ-20260716-ATS-004.md`
- `docs/standards/core-principles.md`
- Relevant sections of `docs/standards/development-standards.md`,
  `documentation-standards.md`, `glossary.md`, and `frontend-standards.md`
- Relevant sections of `docs/policies/quality-gates.md`, `security-policy.md`, and
  `access-control-policy.md`
- Relevant contracts in `docs/design/api-spec.md`, `docs/payment/`, and
  `docs/client/testing-guide.md`
- `.agents/skills/react-best-practices/AGENTS.md` and the frontend verification
  skill instructions
- Current frontend source/tests and the corresponding backend payment enums/DTOs

Read-only commands used included `git status --short`, targeted `rg -n` searches,
and line-numbered `Get-Content` inspection. `application-local.yml`, `.env.local`,
and secret values were not read or printed.

## Prior Finding Closure Matrix

| WI-006 finding | Severity | Status | Current evidence |
|---|---|---|---|
| P2-01 selected re-download used download IDs | P2 | CLOSED | `DownloadHistoryPage.tsx:226-234` maps selected `downloadId` values back to unique `trackId` values. `DownloadHistoryPage.test.tsx:154-178` executes the action, expects `101`, and rejects IDs `1` and `2`. |
| P2-02 stale refund preview targeted the wrong payment | P2 | CLOSED | `PaymentOperationsPage.tsx:533-542` clears preview before/following failure; `:549-567` requires input ID to equal preview ID; `:590-593` invalidates on edit. Tests at `PaymentOperationsPage.test.tsx:502-543` cover edit and failed re-preview. |
| P2-03 password login succeeded when storage failed | P2 | CLOSED | `authStore.ts:52-67` throws and clears the session if token/user persistence fails; `LoginPage.tsx:132-159` catches it and does not navigate. Tests: `authStore.test.ts:80-97`, `LoginPage.test.tsx:248-272`. |
| P2-04 player UI rehydrated without audio | P2 | CLOSED | `playerStore.ts:105-129` validates persisted state and restores stream source/time before resume. `playerPersistence.test.ts:52-77` verifies source, time, and first resume. |
| P2-05 successful recurring callback replayed on Back | P2 | CLOSED | `SubscriptionPaymentPage.tsx:71-86` confirms once per mount and navigates with `replace: true`. `SubscriptionPaymentReplay.test.tsx:30-50` verifies Back reaches the origin without a second confirmation. |
| P2-06 coverage below/enforcement absent | P2 | DEFERRED | `vite.config.ts:144-157` now configures V8 reports and thresholds of 80% statements/lines/functions and 70% branches. The full coverage command was not run in this re-audit, so current metrics and enforcement success are unverified. |
| P3-01 malformed persisted player/history shapes crashed views | P3 | CLOSED | `playerStore.ts:43-112` validates version, Track fields, queue, time, and history entries. `playerPersistence.test.ts:79-122` covers obsolete/wrong shapes and malformed history entries. |
| P3-02 missing callback amount became zero | P3 | CLOSED | `SubscriptionPaymentPage.tsx:345-354` requires digit-only input and a safe integer; subscribe/upgrade requires a positive amount. `SubscriptionPaymentPage.test.tsx:256-268` covers missing, empty, negative, and fractional input. |
| P3-03 Toss SDK failure was permanently memoized | P3 | CLOSED | `tossPayments.ts:85-90` clears the promise and removes the failed script. `tossPayments.test.ts:12-31` verifies fail-once/succeed-on-retry behavior. |
| P3-04 selection/playback controls were inaccessible | P3 | CLOSED | `DownloadHistoryPage.tsx:392-434` provides named checkboxes and a semantic named play button. `DownloadHistoryPage.test.tsx:181-200` verifies roles/names and focusable button behavior. |

## Current Findings

### P3-NEW-01 - Refreshed credentials can be used without being persisted

**Evidence:** `frontend/src/utils/safeStorage.ts:17-23` reports `setItem` failure as
`false`, but `frontend/src/api/client.ts:95-108` ignores both token write results,
resolves queued requests, and retries with the transient token. No inspected client
test covers refresh-token persistence failure.

**Impact:** When storage becomes write-restricted after login, one retry can appear
successful while later requests continue using stale credentials. With rotating
refresh tokens this can force an avoidable logout; without rotation it can produce a
401/refresh cycle on subsequent requests.

**Smallest safe remediation:** Require successful durable writes before resolving
the queue, synchronize the auth store on success, and fail coherently by clearing the
session when either write fails. Add access-token and rotated-refresh-token storage
failure tests.

### P3-NEW-02 - Remaining download-history controls lack stable accessible names

**Evidence:** `frontend/src/pages/subscriber/DownloadHistoryPage.tsx:339-354` has an
unlabelled search input and sort select. The row re-download control at `:459-466`
uses only an arrow glyph as button content and has no track-specific `aria-label`;
its `title` is not a stable, track-specific control name. The inspected accessibility
test covers only selection and playback (`DownloadHistoryPage.test.tsx:181-200`).

**Impact:** Screen-reader users cannot reliably identify the sort control or which
Track an icon-only re-download action affects.

**Smallest safe remediation:** Add visible or `aria-label` names for search/sort and
`aria-label={`Re-download ${item.title}`}` plus `type="button"` for the row action;
extend role/name assertions.

### P3-NEW-03 - Frontend payment status type is narrower than the backend contract

**Evidence:** `frontend/src/api/payments.ts:5-11` omits `PROCESSING`,
`PROVIDER_SUCCEEDED`, and `PENDING_PROVIDER_CONFIRMATION`. The authoritative backend
enum `src/main/java/com/atstudio/atstudio/entity/enums/PaymentOrderStatus.java:3-13`
contains all three, and `BillingAgreementConfirmResponse` serializes that enum.
Provider identity itself is aligned as `TOSS` in frontend response types and the
backend `PaymentProviderType` enum; no exact active `TOSS_BILLING`, `MOCK`, or
`PaymentProvider` production meaning was found in the targeted frontend search.

**Impact:** TypeScript consumers cannot model every backend response state and may
write falsely exhaustive status handling.

**Smallest safe remediation:** Align the frontend union with the backend enum and
add a contract test covering every serialized status.

## Quality Gates and Metrics

| Gate | Result | Current evidence |
|---|---|---|
| TypeScript (`npm run typecheck`) | NOT RUN | No current error count |
| ESLint (`npm run lint`) | NOT RUN | No current error/warning count |
| Prettier (`npm run format`) | NOT RUN | No current formatted-file result |
| Full Vitest with coverage (`npm run test:coverage`) | NOT RUN | No current suite/test totals or coverage counters |
| Coverage enforcement | NOT VERIFIED | Thresholds are configured, but execution was stopped |
| Production build (`npm run build`) | NOT RUN | No current module, bundle-size, warning, or duration metrics |

The last measured WI-006 coverage values (40.51% statements, 40.29% branches,
34.23% functions, 41.84% lines) are historical baseline values only and are not
reported as current WI-008 metrics.

## Residual Risk and Recommendation

WI-009 must remain blocked until the three current P3 findings are repaired or
disproved and all five frontend commands above are rerun successfully with current
test totals, coverage counters, build output, and threshold enforcement evidence.

**BLOCK**

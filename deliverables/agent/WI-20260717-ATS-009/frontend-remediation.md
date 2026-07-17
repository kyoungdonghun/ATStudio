---
version: 1.0
last_updated: 2026-07-17
project: ATS
owner: qa-fe
category: evidence
status: complete
dependencies:
  - path: ../WI-20260717-ATS-009-handoff.md
    reason: Approved scope and frontend output contract
  - path: ../WI-20260717-ATS-008/frontend-qa.md
    reason: Frontend findings under remediation
  - path: ../WI-20260717-ATS-008/integration-review.md
    reason: Route-count integration finding under remediation
---

# WI-20260717-ATS-009 Frontend Remediation

> Purpose: Record the implementation and executable evidence that closes the four WI-008 frontend and integration findings owned by the frontend track.

## 1. Decision

**PASS for the frontend track.** All four assigned findings are `FIXED`, focused
regressions pass, and the requested TypeScript, ESLint, Prettier, coverage, and
production-build gates pass. No backend, active documentation, database, Git
refs/index, or secret-bearing local configuration was modified or inspected.

## 2. Finding Closure

| Finding | Status | Implementation evidence | Regression evidence |
|---|---|---|---|
| WI-008 frontend `P3-NEW-01`: refreshed credentials used after persistence failure | **FIXED** | `frontend/src/api/client.ts:95-119` requires successful access-token and rotated-refresh-token writes before updating Zustand, releasing the failed-request queue, or retrying. Any write failure enters the existing coherent session-clear, toast, and login-redirect path. | `frontend/src/api/client.test.ts:138-219` verifies successful store synchronization plus independent access-token and rotated-refresh-token write failures. Both failure tests assert no request adapter retry, no auth-store update, token removal, session clearing, toast, and login navigation. |
| WI-008 frontend `P3-NEW-02`: unnamed download-history controls | **FIXED** | `frontend/src/pages/subscriber/DownloadHistoryPage.tsx:339-360,465-474` names the search and sort controls and gives each row re-download button a stable Track-specific name and explicit `type="button"`. | `frontend/src/pages/subscriber/DownloadHistoryPage.test.tsx:181-207` locates all three controls by semantic role and accessible name and verifies the row action button type. |
| WI-008 frontend `P3-NEW-03`: incomplete payment status type | **FIXED** | `frontend/src/api/payments.ts:5-17` exports the complete serialized status tuple and derives `PaymentOrderStatus` from it. | `frontend/src/api/domainApis.test.ts:32-42,263-268` compares the runtime tuple with the backend contract snapshot and proves exact TypeScript type equality. |
| WI-008 integration `P3-01`: ADMIN route comment said 9 | **FIXED** | `frontend/src/router/index.tsx:222` now identifies the following group as `Admin (14 routes)`, matching the 14 declarations at lines 223-236. | Typecheck, lint, Prettier, coverage Vitest, and production build all include the router source and pass. |

## 3. Contract Details

### 3.1 Backend PaymentOrderStatus Values

The frontend contract contains every value from backend
`PaymentOrderStatus`, in backend declaration order:

1. `READY`
2. `IN_PROGRESS`
3. `PROCESSING`
4. `PROVIDER_SUCCEEDED`
5. `PENDING_PROVIDER_CONFIRMATION`
6. `DONE`
7. `FAILED`
8. `CANCELLED`
9. `EXPIRED`

### 3.2 Accessibility Assertions

| Role | Accessible name asserted |
|---|---|
| `textbox` | `다운로드 기록 검색` |
| `combobox` | `다운로드 기록 정렬` |
| `button` | `${track.title} 재다운로드`; fixture assertion: `Accessible track 재다운로드` |

The row re-download assertion also verifies `type="button"`, preventing an
accidental submit action if the table is later placed inside a form.

## 4. Verification Evidence

| Command | Result | Evidence |
|---|---|---|
| `npm run test -- src/api/client.test.ts src/pages/subscriber/DownloadHistoryPage.test.tsx src/api/domainApis.test.ts` before production changes | Expected **FAIL** | 3 files, 32 tests: 28 passed and the 4 new regressions failed, reproducing both persistence failures, missing accessible names, and the absent status contract. Wall time 8.1 seconds. |
| Same focused command after production changes | **PASS** | 3 files, 32 tests passed. Vitest duration 3.87 seconds; command wall time 7.0 seconds. |
| `npm run typecheck` | **PASS** | `tsc --noEmit`, no errors. Wall time 11.0 seconds. |
| `npm run lint` | **PASS** | ESLint completed with zero warnings under `--max-warnings 0`. Wall time 7.7 seconds. |
| `npm run format` | **PASS after scoped correction** | Initial check identified only `src/api/client.ts`. `npx prettier --write src/api/client.ts` changed only that owned file; the repeated full check reported all matched files formatted. |
| `npm run test:coverage` | **PASS** | 63 files and 468 tests passed. Statements 86.73% (6095/7027), branches 76.98% (3626/4710), functions 85.41% (1616/1892), lines 88.75% (5597/6306). All 80/70/80/80 thresholds passed. Command wall time 61.4 seconds. |
| `npm run build` | **PASS** | `tsc -b && vite build`; Vite 6.4.3 transformed 266 modules and built in 4.21 seconds. Command wall time 17.6 seconds. |
| Owned-file whitespace/conflict/high-confidence secret count scan | **PASS** | 8 owned files scanned; 0 trailing-whitespace candidates, 0 conflict markers, and 0 high-confidence secret candidates. Values were not printed. |

The Git-based `git diff --check` was intentionally not run because this track
was explicitly prohibited from touching Git refs/index. Prettier plus the
owned-file whitespace and conflict-marker scan provides the scoped source check;
the final integration owner retains any authorized repository-wide Git gate.

## 5. Files Modified

- `frontend/src/api/client.ts`
- `frontend/src/api/client.test.ts`
- `frontend/src/api/domainApis.test.ts`
- `frontend/src/api/payments.ts`
- `frontend/src/pages/subscriber/DownloadHistoryPage.tsx`
- `frontend/src/pages/subscriber/DownloadHistoryPage.test.tsx`
- `frontend/src/router/index.tsx`
- `deliverables/agent/WI-20260717-ATS-009/frontend-remediation.md`

## 6. Time and Blockers

The largest reasoning share went to reading the handoff and WI-008 evidence,
reconciling the frontend/backend contracts, and designing failure-path tests
that prove no retry or authentication state update occurs. The longest
executable step was full coverage at 61.4 seconds, followed by the production
build at 17.6 seconds and typecheck at 11.0 seconds. The initial format check and
its one-file correction/recheck took about 21.4 seconds combined.

**Blockers: none.** The only interruption was a non-blocking Prettier finding in
the edited `client.ts`; it was corrected in that file alone and the full format
gate then passed. The Git-wide diff check remains an explicit integration-track
constraint, not a product or test blocker for this frontend result.

## 7. Rollback

Rollback is limited to restoring the prior content of the seven frontend files
listed above and removing this report. No backend, active documentation,
database, Git index/ref, secret, dependency, or configuration rollback applies.

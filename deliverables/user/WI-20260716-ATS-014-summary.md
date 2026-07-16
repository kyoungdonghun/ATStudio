---
version: 1.0
last_updated: 2026-07-16
project: ATS
owner: qa-fe
category: audit
status: stable
dependencies:
  - path: REQ-20260716-ATS-002.md
    reason: Approved remediation scope
  - path: ../agent/WI-20260716-ATS-014-handoff.md
    reason: Frontend QA execution contract
  - path: ../agent/WI-20260716-ATS-011-evidence-pack.md
    reason: Dependency and coverage configuration baseline
  - path: ../agent/WI-20260716-ATS-012-evidence-pack.md
    reason: Current UI and client documentation baseline
---

# WI-20260716-ATS-014 Summary

## Outcome

The complete frontend quality toolchain passes on development branch `codex/p1-acceptance-hardening`. Dependency audits are clean, all 180 automated tests pass, the production build succeeds, and the full frontend tree is formatted. Public full-track playback remains present and no preview cutoff was reintroduced.

This result is sufficient to proceed to WI-015 cross-layer review and WI-016 code/security review. It is not a release-readiness approval: five medium/low residual findings require WI-017 disposition, and real-browser, provider, retained-DB, production-proxy, and frozen client-demo evidence remain outside this WI.

## Exact Verification

| Check | Result |
|---|---|
| `npm audit --omit=dev --json` | PASS, 0 vulnerabilities: info 0, low 0, moderate 0, high 0, critical 0 |
| `npm audit --json` | PASS, 0 vulnerabilities: info 0, low 0, moderate 0, high 0, critical 0 |
| `npm run typecheck` | PASS |
| `npm run lint` | PASS, zero warnings/errors |
| `npm test -- --run` | PASS, 38 files / 180 tests |
| `npm run test:coverage` | PASS, 38 files / 180 tests; V8 HTML and JSON generated |
| `npm run build` | PASS, Vite 6.4.3, 264 modules transformed |
| `npx prettier --check . --ignore-unknown` | PASS, all matched files use Prettier style |
| `frontend/tsconfig.tsbuildinfo` | Restored byte-for-byte; final SHA-256 equals baseline |

Build output had no oversized-chunk warning. The largest initial JavaScript chunk was 341.04 kB, 110.97 kB gzip.

## Resolved Dependency State

- Direct/runtime: Axios 1.18.1, React Router DOM 6.30.4, React Router 6.30.4, follow-redirects 1.16.0, form-data 4.0.6.
- Toolchain: Vite 6.4.3, Vitest 4.1.4, `@vitest/coverage-v8` 4.1.4, Babel Core 7.29.7, PostCSS 8.5.19, picomatch 4.0.5, undici 7.28.0.
- `package.json` has no `overrides` or `resolutions`. WI-014 did not run an audit fix, force an install, update a package, or change a lockfile.

## Coverage Observation

Coverage is an observed risk baseline, not an acceptance threshold.

| Metric | Covered / Total | Result |
|---|---:|---:|
| Statements | 2,347 / 6,803 | 34.49% |
| Branches | 1,532 / 4,505 | 34.00% |
| Functions | 515 / 1,851 | 27.82% |
| Lines | 2,168 / 6,119 | 35.43% |

Focused route guards, request fencing, modal focus, toast semantics, pagination, player state, and selected high-value pages have useful coverage. Material gaps remain in admin payment mutations, admin user/subscription lists, detail-load races, and many creator/admin pages.

## Residual Findings

1. **Medium - Player subscription state collapses infrastructure failure into non-subscriber UI.** `PlayerBar` maps every subscription lookup error to `hasSubscription=false`, has no cancellation/generation fence, then hides subscriber controls and shows “구독하기.” Only structured `NO_ACTIVE_SUBSCRIPTION` should represent the inactive domain state.
2. **Medium - Latest-request-wins is incomplete outside the remediated screens.** Track detail, admin user list, admin user-subscription list, and download history have asynchronous reads without a request generation or abort fence. Older responses can overwrite newer route/filter/page state.
3. **Medium - High-risk admin payment mutation UI lacks focused regression coverage.** Existing tests cover stale reads, active failure, and one incident reload, but not settlement import/reconcile or refund/entitlement preview, request, approval, typed confirmation, and execution flows.
4. **Low - Safe post-login return works for password login but is lost through social login.** The login screen validates `returnTo`, but the OAuth start/callback flow does not preserve it and completed social login always navigates to `/`.
5. **Low - Router count comment is stale.** The code comment says 49 screens plus 2 errors while the declarations produce 62 paths, 1 index redirect, 54 lazy pages, and the documented 53 visual UIs.

## Product Policy Review

- Player source remains `GET /api/tracks/{id}/stream` and there is no preview/cutoff term or timer in the player path.
- Playback metadata/time progression, stalled recovery, retry, and keyboard seek tests pass.
- Official download still uses `GET /api/tracks/{id}/download`; subscription/quota/license decisions remain server-authoritative. The existing ADMIN bypass is explicitly retained by the current API design and was not changed by this WI.
- Eight payment callback/compatibility routes remain USER-only and redirect ADMIN to `/admin/payments`.
- BUSINESS certification routes remain USER plus BUSINESS-only.

## Scope Preservation

- Verification-only except this summary and the Evidence Pack.
- No application source, package, lockfile, DB/data, secret, runtime, client-demo worktree, stage, commit, push, or public process was modified.
- Real-browser accessibility/responsive behavior and live network cancellation were not claimed from jsdom or build success.
- The frozen client-demo branch was not inspected or changed; all results above belong only to the development branch.

## Related Documents

- [Evidence Pack](../agent/WI-20260716-ATS-014-evidence-pack.md): Commands, metrics, review matrix, findings, and hash proof.
- [Handoff](../agent/WI-20260716-ATS-014-handoff.md): Approved QA contract.
- [Approved REQ](REQ-20260716-ATS-002.md): Development-branch-only remediation scope.

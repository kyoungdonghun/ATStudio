---
version: 1.0
last_updated: 2026-07-15
project: ATS
owner: qa-fe
category: evidence-pack
status: stable
related_wi: WI-20260714-ATS-020
---

# Evidence Pack: WI-20260714-ATS-020

## Summary (one-liner)

- Verified the frontend social-callback, logout, refresh-failure, and Vite Host/proxy contracts, fixed one logout-refresh mismatch in frontend code only, and closed the QA path with focused tests plus typecheck/lint/build evidence.

## Scope / DoD Check

- [x] Social callback fetches the user with staged credentials before committing login/navigation.
- [x] Callback failure after token staging performs best-effort server logout and always clears dependent client state.
- [x] Strict Mode does not duplicate the social callback exchange or navigation.
- [x] Logout 401 is excluded from refresh recursion.
- [x] Refresh failure on a protected request clears tokens/session, surfaces user feedback, and redirects to `/login`.
- [x] Vite Host allowlist covers only local hosts plus the exact host derived from `APP_PUBLIC_BASE_URL`.
- [x] `/api` and `/uploads` preserve the same loopback proxy boundary and header-sanitizing contract.
- [x] Focused Vitest, typecheck, lint, build, and scoped diff check passed.
- [x] Backend production code was not modified.
- [x] No live OAuth provider, Toss, Cloudflare tunnel, or public runtime was invoked.

## Reference Documents (Tier 0-2)

| Tier | Document | Reason |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Approved execution, testing, and traceability baseline |
| 0 | `docs/standards/development-standards.md` | Frontend QA/test expectations and evidence discipline |
| 0 | `docs/standards/documentation-standards.md` | Deliverable structure and naming rules |
| 0 | `docs/standards/glossary.md` | Canonical terminology |
| 1 | `docs/standards/frontend-standards.md` | React, Zustand, Axios, and Vite runtime contract |
| 2 | `.agents/skills/react-best-practices/AGENTS.md` | React-side verification lens for stable effects and state flow |
| 2 | `deliverables/user/REQ-20260714-ATS-001.md` | Approved P1 scope and QA ownership boundary |
| 2 | `deliverables/agent/WI-20260714-ATS-014-evidence-pack.md` | Social callback staging/order contract |
| 2 | `deliverables/agent/WI-20260714-ATS-015-evidence-pack.md` | `APP_PUBLIC_BASE_URL` acceptance contract consumed by Vite |
| 2 | `deliverables/agent/WI-20260714-ATS-016-evidence-pack.md` | Host/proxy/client-identity boundary already implemented |
| WI | `deliverables/agent/WI-20260714-ATS-020-handoff.md` | Scope, DoD, ownership limits, and output contract |

## Evidence Pointers

### Frontend code and tests

- `frontend/src/api/client.ts:36-45` - refresh exclusion list now includes `/auth/logout`, aligning runtime behavior with the no-recursive-logout contract.
- `frontend/src/api/client.ts:73-115` - refresh-failure branch removes local tokens, clears session, shows toast feedback, and redirects to `/login`.
- `frontend/src/api/client.test.ts:28-39` - direct access to the registered Axios response interceptor for QA-only refresh failure assertions.
- `frontend/src/api/client.test.ts:51-55` - verifies login, logout, and refresh endpoints are excluded from refresh recursion.
- `frontend/src/api/client.test.ts:68-93` - verifies refresh failure on a protected request removes tokens, calls `clearSession`, emits toast feedback, and routes to `/login`.
- `frontend/vite.config.test.ts:36-53` - verifies `/api` and `/uploads` share the same loopback proxy target with `xfwd: false`.
- `frontend/vite.config.test.ts:66-120` - verifies forwarding-header removal, exact internal IP rewrite, local socket fallback, and invalid/list-valued header rejection.
- `frontend/src/pages/auth/SocialLoginPage.test.tsx:71-112` - verifies `exchange -> stage -> fetchMe -> login -> home` ordering under React Strict Mode with a single callback exchange.
- `frontend/src/pages/auth/SocialLoginPage.test.tsx:114-133` - verifies the profile-incomplete route transition to `/complete-profile` after committing auth state.
- `frontend/src/pages/auth/SocialLoginPage.test.tsx:135-175` - verifies callback failure cleanup invokes server logout while staged tokens exist and always clears auth/player/like state.
- `frontend/src/store/authStore.test.ts:79-137` - verifies server-first logout ordering and unconditional local cleanup on network failure.
- `frontend/src/api/auth.test.ts:20-44` - verifies bodyless logout request shape, confirmed-401 tolerance, and propagation of transient logout failures back to the store.

### Contract mismatch found

- **Mismatch**: `frontend/src/api/client.ts` previously excluded login, refresh, and social endpoints but not `/auth/logout`, which meant a logout 401 could attempt token refresh before `logoutSession()` handled it.
- **Resolution**: Added `/auth/logout` to the frontend exclusion list only; no backend change was required or made.

## Commands & Outputs

- `npm test -- src/pages/auth/SocialLoginPage.test.tsx src/store/authStore.test.ts src/api/auth.test.ts src/api/client.test.ts vite.config.test.ts`
  - Passed: 5 files, 23 tests, 0 failures.
- `npm run typecheck`
  - Passed: `tsc --noEmit`.
- `npm run lint`
  - Passed: ESLint completed with zero errors/warnings.
- `npm run build`
  - Passed: `tsc -b && vite build`.
- `git diff --check -- frontend/src/api/client.ts frontend/src/api/client.test.ts frontend/vite.config.test.ts`
  - Passed: no whitespace errors; working-copy LF-to-CRLF warnings only.

## Tests

### Focused Vitest totals

- `frontend/src/pages/auth/SocialLoginPage.test.tsx`: 3 passed
- `frontend/src/store/authStore.test.ts`: 3 passed
- `frontend/src/api/auth.test.ts`: 3 passed
- `frontend/src/api/client.test.ts`: 4 passed
- `frontend/vite.config.test.ts`: 10 passed
- **Total**: 23 passed, 0 failed

### State-transition assertions covered

1. Social callback success:
   - `socialLogin` exchanges the provider code.
   - issued access/refresh tokens are staged first.
   - `fetchMe(accessToken)` runs before store commit.
   - `login()` commits user/role/access token.
   - route ends at `/`.
2. Social callback incomplete profile:
   - same staged-auth sequence as above.
   - final route ends at `/complete-profile`.
3. Social callback failure after token staging:
   - `logoutSession()` is attempted while the staged access token is still available.
   - local auth, like, album-like, and player state are cleared regardless of server confirmation.
4. Manual logout:
   - server logout is attempted before local cleanup.
   - network failure still clears local auth state and dependent stores.
5. Refresh failure on a protected request:
   - access/refresh tokens are removed.
   - `clearSession()` runs.
   - one toast is shown.
   - router redirects to `/login`.
6. Logout 401:
   - `/auth/logout` is excluded from refresh recursion by policy and test.

### Route / proxy assertions covered

- Route:
  - `/social-login/:provider` completes only one exchange in React Strict Mode.
  - callback success routes to `/`.
  - profile-incomplete callback routes to `/complete-profile`.
- Proxy:
  - `allowedHosts` is never `true`.
  - only `localhost`, `127.0.0.1`, and the exact normalized acceptance host are allowed.
  - `/api` and `/uploads` both proxy to `http://127.0.0.1:8080`.
  - `xfwd` stays `false` on both proxies.
  - inbound forwarded headers are removed before proxying.
  - exactly one validated internal client-IP header is re-written.

## Browser-smoke prerequisites

- Keep the frontend same-origin topology: Vite on `127.0.0.1:5173`, backend on `127.0.0.1:8080`, and relative `/api` plus `/uploads`.
- Supply one HTTPS `APP_PUBLIC_BASE_URL` root origin with no path, query, fragment, or trailing slash.
- Use WI-015/WI-016 acceptance contracts when WI-022 performs public runtime verification.
- Do not run live OAuth providers, Toss, or Cloudflare tunnel from this WI.

## Risks / Rollback

- Risks:
  - QA evidence proves the frontend contract, not the live Cloudflare runtime rewrite behavior; that remains an operational/public-runtime check.
  - The refresh-failure test uses a mocked `axios.post('/api/auth/refresh')`; provider/network behavior remains intentionally out of scope.
  - This WI does not validate multi-client rate-limit separation over a real tunnel.
- Rollback:
  - Remove the `/auth/logout` entry from `AUTH_REFRESH_EXCLUDED_PATHS` only if the logout recursion contract is explicitly changed.
  - Remove the QA-only tests added in `frontend/src/api/client.test.ts` and `frontend/vite.config.test.ts`.
  - Do not revert unrelated backend, payment, storage, MySQL, runtime-log, or tunnel artifacts.

## Follow-ups

- WI-020 no longer blocks the frontend QA portion of `WI-20260714-ATS-022`, `WI-20260714-ATS-025`, `WI-20260714-ATS-029`, and `WI-20260714-ATS-034`.
- WI-022 should reuse the route/proxy prerequisites above for public runtime smoke verification.
- WI-025 can treat the logout-refresh mismatch as closed on the frontend side and focus on independent security confirmation.

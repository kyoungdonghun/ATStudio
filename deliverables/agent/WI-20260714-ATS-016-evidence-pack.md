---
version: 1.0
last_updated: 2026-07-14
project: ATS
category: evidence-pack
status: stable
related_wi: WI-20260714-ATS-016
---

# Evidence Pack: WI-20260714-ATS-016

## Summary

- Replaced wildcard acceptance ingress with exact Vite Hosts, sanitized proxy identity headers, a loopback-only Spring client identity resolver, rate-limit integration, exact CORS origins, and an acceptance-only backend Host boundary.

## Scope / DoD Check

- [x] Removed `allowedHosts: true`; allowed only `localhost`, `127.0.0.1`, and the host derived from `APP_PUBLIC_BASE_URL`.
- [x] Removed inbound `Forwarded`, every `X-Forwarded-*`, `CF-Connecting-IP`, and `X-ATStudio-Client-IP` before proxying.
- [x] Rewrote exactly one validated internal client-IP header from Cloudflare input in acceptance or socket peer in local mode.
- [x] Trusted that internal header only from an explicitly configured loopback peer and rejected list, port, zone, whitespace, duplicate, and oversized forms by falling back to the direct peer.
- [x] Connected effective client identity to the existing method + route + IP auth rate-limit key without changing limit values.
- [x] Removed wildcard trycloudflare CORS and added only WI-015's validated acceptance origin.
- [x] Added an acceptance-only backend Host filter for loopback proxy Hosts.
- [x] Did not modify WI-015's acceptance secret/public URL contract and did not start a server, tunnel, or live service.

## Reference Documents

| Tier | Pointer | Applied contract |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Approved execution, security, and traceability baseline |
| 0 | `docs/standards/development-standards.md` | Spring/Java implementation and focused verification |
| 0 | `docs/standards/documentation-standards.md` | Two-set deliverables and evidence format |
| 0 | `docs/standards/glossary.md` | Canonical terminology |
| 1 | `docs/policies/security-policy.md` | Default-deny proxy/CORS and secret-safe environment behavior |
| 1 | `docs/policies/access-control-policy.md` | Least privilege and network boundary |
| 2 | `docs/standards/frontend-standards.md` | Vite/TypeScript quality baseline |
| 2 | `deliverables/user/REQ-20260714-ATS-001.md` | Approved P1 acceptance-hardening scope |
| 2 | `docs/design/p1-security-acceptance-hardening-design.md` | Section 9.1 trusted client identity and Host contract |
| 2 | `docs/SR/SR-42.md` | Single frontend tunnel and same-origin proxy topology |
| WI | `deliverables/agent/WI-20260714-ATS-016-handoff.md` | Scope, ownership, DoD, and output contract |
| WI | `deliverables/agent/WI-20260714-ATS-015-evidence-pack.md` | Consumed `APP_PUBLIC_BASE_URL` and `AcceptanceProperties` contract |

## Evidence Pointers

### Implementation files

- `frontend/vite.config.ts`
  - Exact Host allowlist derived from `APP_PUBLIC_BASE_URL`.
  - Loopback-only Vite binding and backend proxy target.
  - Forwarding-header removal and one validated `X-ATStudio-Client-IP` rewrite.
- `src/main/java/com/atstudio/atstudio/config/TrustedClientIdentityProperties.java`
  - Disabled-by-default trusted proxy configuration and exact proxy address list.
- `src/main/java/com/atstudio/atstudio/security/TrustedClientIdentityResolver.java`
  - Maximum 64-byte single IPv4/IPv6 literal parser and configured-loopback trust enforcement.
- `src/main/java/com/atstudio/atstudio/security/AuthRateLimitFilter.java`
  - Existing rate-limit key now consumes the bounded effective client identity.
- `src/main/java/com/atstudio/atstudio/security/AcceptanceHostFilter.java`
  - Acceptance-only exact backend Host enforcement.
- `src/main/java/com/atstudio/atstudio/config/SecurityConfig.java`
  - Host filter inserted before the auth rate-limit filter.
- `src/main/java/com/atstudio/atstudio/config/CorsConfig.java`
  - Explicit trimmed origins and exact WI-015 acceptance origin; no wildcard pattern.
- `src/main/resources/application.yml`
  - `server.forward-headers-strategy: none` and trusted client identity settings.

### Focused tests

- `frontend/vite.config.test.ts`: exact Host derivation, invalid public base refusal, forwarding-header removal, acceptance/local IP source, invalid list refusal.
- `src/test/java/com/atstudio/atstudio/security/TrustedClientIdentityResolverTest.java`: trusted loopback, direct spoof, disabled mode, acceptance mode, malformed and duplicate values.
- `src/test/java/com/atstudio/atstudio/security/AuthRateLimitFilterTest.java`: distinct proxied clients and direct spoof key collapse.
- `src/test/java/com/atstudio/atstudio/security/AcceptanceHostFilterTest.java`: loopback acceptance, unknown Host refusal, non-acceptance pass-through.
- `src/test/java/com/atstudio/atstudio/config/CorsConfigTest.java`: explicit origins, no wildcard pattern, exact acceptance origin.

## Commands and Results

1. `gradlew.bat compileJava`
   - Passed: `BUILD SUCCESSFUL`.
2. `gradlew.bat test --tests "com.atstudio.atstudio.security.TrustedClientIdentityResolverTest" --tests "com.atstudio.atstudio.security.AuthRateLimitFilterTest" --tests "com.atstudio.atstudio.security.AcceptanceHostFilterTest" --tests "com.atstudio.atstudio.config.CorsConfigTest"`
   - Passed: 4 classes, 12 tests, 0 failures/errors.
3. MA independent rerun of `TrustedClientIdentityResolverTest` and `AuthRateLimitFilterTest`
   - Passed: 2 classes, 7 tests, 0 failures/errors.
4. `npm test -- vite.config.test.ts`
   - Passed: 1 file, 9 tests.
5. `npm run typecheck`
   - Passed.
6. `npm run lint`
   - Passed with zero warnings.
7. `npm run build`
   - Passed: TypeScript project build and Vite production build.
8. WI-016-scoped `git diff --check`
   - Passed; only working-copy LF-to-CRLF notices were emitted.

### Non-assertion interruption

- One attempted combined `SecurityFilterChainTest` + `AuthControllerTest` run was interrupted by `NoSuchFileException` for a Gradle `build/test-results/test/binary/in-progress-results-*.bin` file while concurrent workers shared the build directory.
- No application assertion or WI-016 test failed in that attempt. The focused backend suite was rerun successfully afterward.

## Security Boundary

1. Vite accepts only local Hosts plus the one host derived from WI-015's `APP_PUBLIC_BASE_URL`.
2. Vite removes any client-supplied forwarding/internal identity headers.
3. Vite writes one validated IP literal into `X-ATStudio-Client-IP`.
4. Spring does not apply generic forwarded-header processing.
5. Spring consumes the internal header only when acceptance or explicit trusted identity mode is active and the immediate peer is both loopback and present in the configured allowlist.
6. Any invalid or untrusted assertion falls back to the direct peer and cannot create a new rate-limit identity.

## Risks / Limitations

- Repository tests cannot prove Cloudflare's runtime overwrite behavior for `CF-Connecting-IP`; WI-022 must verify this through the public tunnel.
- WI-022 must also prove two external clients receive separate effective identities and that direct public spoof attempts do not change them.
- The trusted boundary assumes local same-host processes are controlled; no shared secret is added between `cloudflared` and Vite.
- The limiter remains in-memory and single-server by approved scope. Rate values and multi-server coordination were not changed.
- Public URL reachability, live callbacks, Toss, SMTP, DB, and tunnel behavior were not invoked.

## Rollback

- Revert only the WI-016 implementation and test files listed above.
- Restore the previous `AuthRateLimitFilter` key source and explicit CORS behavior only if the acceptance ingress is disabled first.
- Do not revert WI-015 acceptance configuration or unrelated payment, storage, auth, schema, and runtime-log changes.

## Follow-up

- WI-017 may consume this Evidence Pack for launcher environment injection and lifecycle automation.
- WI-020/WI-022/WI-024 own integration, public runtime, and independent security verification respectively.

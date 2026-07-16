# Evidence Pack: WI-20260716-ATS-005

## Summary (one-liner)

- Added dual PII-safe availability abuse budgets and enforced USER-only payment boundaries in backend and frontend while preserving existing USER billing behavior.

## Scope / DoD Check

- [x] Independent registration, email, phone, and nickname budgets return 429 with `Retry-After` when exceeded.
- [x] Every availability request consumes an endpoint + trusted-client budget and an endpoint + trusted-client + normalized-identifier fingerprint budget.
- [x] Rotating identifiers cannot bypass the endpoint-wide client budget.
- [x] Registration body is not read or parsed by the rate-limit filter.
- [x] Availability identifiers are normalized and used only through process-local salted SHA-256 fingerprints; raw client and identifier values are absent from keys and logs.
- [x] A 429 warning contains only a fixed endpoint scope and retry seconds; tests exclude raw identifier and direct IP values.
- [x] Trusted distinct clients and identifiers separate; spoofed, malformed, and duplicate internal headers converge on the direct peer.
- [x] Idle key lifetime is bounded by the configured maximum window.
- [x] ADMIN checkout routes redirect to `/admin/payments`; ADMIN `/api/payments/**` requests return 403 before controller invocation.
- [x] Existing USER recurring billing controller behavior remains green.
- [x] Focused tests, typecheck, ESLint, changed-file Prettier, docs validation, and diff check pass.
- [x] X-02 and X-03 remain environment-conditional.

## Reference Documents (Tier 0-2)

| Tier | Document | Reason |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Constitution and approved execution boundary |
| 0/1 | `docs/standards/development-standards.md` | Java, security, and test standards |
| 0/1 | `docs/policies/security-policy.md` | PII, JWT, logging, and Spring Security policy |
| 1 | `docs/policies/access-control-policy.md` | Least privilege and default deny |
| 1 | `docs/policies/quality-gates.md` | Verification and rollback requirements |
| 2 | `docs/design/remaining-remediation-design-20260716.md` | P2-01/P2-02 and X-02/X-03 boundary |
| 2 | `docs/design/api-spec.md` | Public account and payment API contract |
| 2 | `docs/ui/screen-flow.md` | Checkout route contract |
| 2 | `docs/design/payment-integration-design.md` | USER recurring billing invariant |
| Context | `deliverables/user/REQ-20260716-ATS-002.md` | Approved scope and product invariants |
| Context | `deliverables/user/WI-20260716-ATS-004-summary.md` | Predecessor design decision |
| Context | `deliverables/user/WI-20260715-ATS-020-summary.md` | Source findings and environment gaps |

The handoff source/test pointers were read first. Its absent `frontend/src/components/auth/ProtectedRoute.tsx`, `SecurityConfigTest.java`, and `frontend/src/router/index.test.tsx` pointers were reconciled to the actual `frontend/src/router/ProtectedRoute.tsx`, existing `SecurityFilterChainTest.java`, and a newly created route wiring test.

## Evidence Pointers

- Dual-bucket acquisition, body-free registration path, normalized fingerprints, PII-free 429 observability, and cleanup: `src/main/java/com/atstudio/atstudio/security/AuthRateLimitFilter.java:64-208`.
- Endpoint defaults and maximum-window calculation: `src/main/java/com/atstudio/atstudio/config/AuthRateLimitProperties.java:14-67`; committed configuration: `src/main/resources/application.yml:136-176`, `application-local.example.yml:84-126`.
- Explicit USER-and-not-ADMIN backend matcher: `src/main/java/com/atstudio/atstudio/config/SecurityConfig.java:30-31`, `src/main/java/com/atstudio/atstudio/config/SecurityConfig.java:127-129`.
- Frontend exact-role guard and route wiring: `frontend/src/router/ProtectedRoute.tsx:13-55`, `frontend/src/router/index.tsx:123-191`.
- Unit/integration evidence: `src/test/java/com/atstudio/atstudio/security/AuthRateLimitFilterTest.java:47-259`, `src/test/java/com/atstudio/atstudio/controller/SecurityFilterChainTest.java:141-258`, `src/test/java/com/atstudio/atstudio/controller/PaymentControllerTest.java:47-202`.
- Frontend evidence: `frontend/src/router/ProtectedRoute.test.tsx:46-106`, `frontend/src/router/index.test.tsx:1-33`.
- Contract updates: `docs/policies/security-policy.md:119-176`, `docs/design/api-spec.md:9-20`, `docs/design/api-spec.md:895-931`, `docs/design/api-spec.md:1253-1459`, `docs/design/api-spec.md:3367-3470`, `docs/ui/screen-flow.md:195-217`, `docs/design/payment-integration-design.md:322-330`, `docs/design/payment-integration-design.md:581-584`.

## Commands & Outputs

- `.\gradlew.bat compileJava` -> PASS.
- `.\gradlew.bat test --tests "*AuthRateLimit*" --tests "*TrustedClientIdentityResolver*" --tests "*SecurityFilterChain*" --tests "*PaymentController*"` -> PASS, 43 tests, 0 failures/errors/skips. This includes identifier-rotation client-budget and PII-free warning evidence.
- `npm run typecheck` in `frontend/` -> PASS.
- `npm test -- --run src/router/ProtectedRoute.test.tsx src/router/index.test.tsx` in `frontend/` -> PASS, 2 files / 13 tests.
- `npx eslint src/router/ProtectedRoute.tsx src/router/ProtectedRoute.test.tsx src/router/index.tsx src/router/index.test.tsx` -> PASS, zero errors/warnings.
- `npx prettier --check src/router/ProtectedRoute.tsx src/router/ProtectedRoute.test.tsx src/router/index.tsx src/router/index.test.tsx` -> PASS.
- `python .agents/skills/validate-docs/scripts/validate_docs.py` -> PASS: Tier 0, internal links, 398 traceability IDs, and document index.
- `git diff --check` -> exit 0; output contained line-ending conversion warnings only.

Transient evidence: one initial focused-backend invocation was terminated by its 5-second command timeout before a result. The identical command was rerun with a 120-second timeout and completed successfully.

## Risks / Rollback

- Risks: rate-limit state and fingerprint salt are process-local and intentionally match the current single-server deployment invariant. Multi-instance shared budgets would require a separately approved architecture change.
- Risks: X-02 trusted proxy CIDRs/multi-egress behavior and X-03 JWT rotation/session expiry are not proven by repository tests.
- Rollback: revert only WI-005 source, test, configuration, and documentation files listed above. Preserve existing trusted-client resolution, USER payment service behavior, `docs/design/index.md`, other WI deliverables, client worktrees, and runtime logs.

## Follow-ups

- WI-005's dependency edge is complete for WI-010, WI-011, and WI-012. Those WIs must still satisfy their other REQ dependencies before execution.
- MA should retain X-02/X-03 as environment evidence gates; no source-only closure is authorized.

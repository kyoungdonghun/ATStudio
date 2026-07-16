---
version: 1.0
last_updated: 2026-07-16
project: ATS
owner: qa-integ
category: evidence
status: complete
related_wi: WI-20260716-ATS-022
---

# Evidence Pack: WI-20260716-ATS-022

## Summary

- Final development-branch re-verification found no blocker in the WI-021 response-boundary scope or supplied repository gates. Judgment: `READY_FOR_USER_DEV_ACCEPTANCE`.

## Scope / DoD Check

- [x] F-020-01 is confirmed closed at the ADMIN reconciliation response boundary.
- [x] ADMIN reconciliation JSON exposes `providerReference` and omits the raw `providerTransactionId` field and sentinel value.
- [x] Internal reconciliation evidence and Incident/provider-operation boundaries remain unchanged by WI-021.
- [x] Product policy and client-branch isolation remain intact; no client propagation was performed.
- [x] Final deterministic backend, frontend, documentation, PDF, integrity, client-branch, and runtime gate results supplied by MA pass.
- [x] Environment-conditional residuals, coverage debt, and untracked runtime artifacts remain explicit.

## Reference Documents (Tier 0-2)

| Tier | Document | Reason |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Constitution and payment traceability baseline |
| 0 | `docs/standards/development-standards.md` | Java DTO and testing standards |
| 0 | `docs/standards/documentation-standards.md` | Closure document standards |
| 0 | `docs/standards/glossary.md` | Canonical terminology |
| 1 | `docs/policies/security-policy.md` | ADMIN payment support-reference boundary |
| 1 | `docs/policies/quality-gates.md` | Gate, traceability, and closure requirements |
| 2 | `docs/design/api-spec.md` | Authoritative reconciliation response contract |
| 2 | `docs/design/remaining-remediation-design-20260716.md` | Current remediation and residual baseline |
| 2 | `docs/client/testing-guide.md` | Client verification and PDF evidence context |
| Context | `deliverables/user/REQ-20260716-ATS-002.md` | Approved remediation scope |
| Context | `deliverables/agent/WI-20260716-ATS-020-evidence-pack.md` | Original F-020-01 finding and affected path |
| Context | `deliverables/agent/WI-20260716-ATS-021-evidence-pack.md` | WI-021 implementation and focused evidence |

**Injection rules applied:**

- Source: `deliverables/agent/WI-20260716-ATS-022-handoff.md`
- Assignee: `qa-integ`
- Task type: final integration re-verification and closure

## Independent Evidence Pointers

- `src/main/java/com/atstudio/atstudio/dto/payment/AdminPaymentReconciliationResponse.java:16-42` maps provider issues through the ADMIN-safe `ProviderIssue` record and preserves local/provider aggregate and truncation fields.
- `src/main/java/com/atstudio/atstudio/dto/payment/AdminPaymentReconciliationResponse.java:56-106` defines the nested response record with `providerReference` and converts the internal transaction identifier through `ProviderSupportReference.from(...)`.
- `src/main/java/com/atstudio/atstudio/dto/payment/ProviderSupportReference.java:15-23` produces deterministic uppercase `REF-*` values from a one-way SHA-256 digest.
- `src/main/java/com/atstudio/atstudio/service/AdminPaymentReadService.java:73-80` maps the service reconciliation results through `AdminPaymentReconciliationResponse.from(...)`.
- `src/main/java/com/atstudio/atstudio/controller/AdminPaymentController.java:253-256` exposes the ADMIN reconciliation endpoint and delegates to the read service.
- `src/main/java/com/atstudio/atstudio/service/PaymentReconciliationService.java:524-546,568-613` retains the service-internal exact provider evidence record; the response boundary performs the safe conversion.
- `src/test/java/com/atstudio/atstudio/dto/payment/AdminProviderIdentifierContractTest.java:57-135` verifies the nested record contract, deterministic reference, aggregate preservation, and absence of the raw field name and sentinel value in direct JSON.
- `src/test/java/com/atstudio/atstudio/controller/AdminPaymentControllerTest.java:86-138` verifies serialized `/api/admin/payments/reconciliation` JSON contains the safe reference and no raw field name/value.
- `docs/policies/security-policy.md:231-239` requires deterministic masked `REF-*` references in ADMIN responses while retaining exact identifiers only in protected server/entity fields.
- `docs/design/api-spec.md:2198-2247` defines the read-only reconciliation response and the `providerReference` issue field.
- `deliverables/agent/WI-20260716-ATS-020-evidence-pack.md:19-45` records the original F-020-01 finding and its response-contract gap.
- `deliverables/agent/WI-20260716-ATS-021-evidence-pack.md:48-83` records the WI-021 implementation evidence and focused regression results.

## Final Gate Results Supplied by MA

### Repository identity

- Development branch: `codex/p1-acceptance-hardening`
- HEAD: `cd876fcf84b3cb2490c27420c6c53a87a35b982d`

### Backend

- Clean test and JaCoCo gate: 1106 tests, 0 failures, 9 skipped, `1m37.53s`.
- JaCoCo instruction coverage: `77.34%` (`35847/46352`).
- JaCoCo branch coverage: `60.11%` (`2342/3896`).
- JaCoCo line coverage: `78.01%` (`7919/10151`).
- JaCoCo method coverage: `77.81%` (`1490/1915`).
- JaCoCo class coverage: `89.57%` (`352/393`).
- `gradlew build`: PASS.

### Frontend

- Production dependency audit: 0 vulnerabilities.
- Full dependency audit: 0 vulnerabilities.
- Typecheck: PASS.
- ESLint: PASS.
- Vitest: 44 files / 242 tests PASS.
- Coverage: statements `38.82%` (`2690/6929`), branches `38.96%` (`1800/4619`), functions `32.54%` (`608/1868`), lines `40.10%` (`2498/6228`).
- Vite build: PASS, 267 modules.
- Full Prettier: PASS.
- `frontend/tsconfig.tsbuildinfo` restored exactly: SHA256 `B6A42AD2CD32A5AD04D06C55B8B1B26DD9B1894AFDAF7B508DFF16EF0C60F22A`, size 5421.

### Documentation, PDF, and integrity

- `validate-docs`: PASS; Tier 0, links, 408 traceability IDs, and all indexed documents verified.
- Client PDF verification: PASS; 12 pages, title `AT.M 클라이언트 테스트 가이드`, 278/278 source segments, SHA256 `afba32cce2460d5d38b80f4a88278e31d1f7344a2258e240bfd61df74f4c6095`.
- PDF content is unchanged from the prior rendered 12-page visual QA.
- `git diff --check`: exit 0; LF-to-CRLF warnings only.

### Client, runtime, and repository controls

- Client worktree `codex/client-demo-stable` is clean at the same HEAD; no propagation or restart occurred.
- Local and public `/` and `/api/tracks` checks returned HTTP 200 with bytes `822/3078`.
- No stage, commit, or push occurred.

## Final Judgment

`READY_FOR_USER_DEV_ACCEPTANCE`

No blocker was found in the independent WI-021 response-boundary inspection, and the final gate results supplied by MA passed. F-020-01 is closed without client propagation or a production-readiness claim beyond the stated development-branch acceptance boundary.

## Residuals / Follow-up Classification

These items remain explicit follow-up debt and are not hidden by the readiness judgment:

### Environment and policy follow-up

- Retained MySQL migration, concurrency, and EXPLAIN verification.
- Live Toss/provider/refund/callback verification.
- Trusted proxy, CORS, external callback, and secret configuration validation.
- Canonical path/symlink host behavior.
- Social-only withdrawal policy.
- Frozen client branch dependency state.

### Coverage follow-up

- Overall backend/frontend coverage remains below project standards thresholds, despite the reported passing test/build gates and recorded coverage metrics above.

### Workspace state

- Runtime logs/tmp remain untracked and were not deleted, consistent with the WI-022 constraints.

## Rollback

- WI-022 introduced no implementation or runtime change, so no code rollback is required.
- If the WI-021 response-boundary fix is later reverted, repeat the DTO and controller sentinel checks and the final readiness gates before accepting the development branch.

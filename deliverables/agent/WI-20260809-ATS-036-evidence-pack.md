---
version: 1.0
last_updated: 2026-08-13
project: ATS
owner: se
category: evidence-pack
status: active
related_wi: WI-20260809-ATS-036
dependencies:
  - path: WI-20260809-ATS-036-handoff.md
    reason: Approved scope, acceptance criteria, and output contract
  - path: ../../docs/policies/security-policy.md
    reason: Current authentication replay security contract
---

# Evidence Pack: WI-20260809-ATS-036

## Summary

- Closed the `CR-031-121` implementation defect by marking every eligible
  protected request before refresh ownership or queue entry, so each request is
  replayed at most once and a replayed second `401` fails closed.

## Scope / DoD Check

- [x] The refresh-leading request is marked before refresh starts.
- [x] Every request queued behind the in-flight refresh is marked before queue
      entry and replay.
- [x] A queued replay that receives a second `401` preserves that failure and
      performs no second refresh, queue entry, or request replay.
- [x] Concurrent first `401` responses share one refresh and replay each
      protected request at most once.
- [x] Existing `skipAuthReplay`, auth endpoint exclusions, missing-refresh-token,
      storage fail-closed, token rotation, ADMIN `403` synchronization, and
      navigation tests remain green.
- [x] Queue processing remains bounded by the existing `failedQueue`; no polling,
      timer, or retry loop was introduced.
- [x] Focused and full frontend tests, configured coverage thresholds,
      typecheck, ESLint, Prettier, and production build pass.
- [x] Current frontend, security, and API contracts describe exactly-once replay
      and fail-closed second rejection.
- [x] SE static security review confirms the bounded implementation contract.
- [x] Independent mandatory PG review confirms exactly-once replay, single
      refresh ownership, and fail-closed second rejection (`PASS`, 2026-08-13).

## Reference Documents (Tier 0-2)

| Tier | Document | Use |
| --- | --- | --- |
| 0 | `docs/standards/core-principles.md` | Approval, security, simplicity, and traceability |
| 0 | `docs/standards/development-standards.md` | TDD, coverage, evidence, and rollback |
| 0 | `docs/standards/documentation-standards.md` | Metadata, versioning, language, and links |
| 0 | `docs/standards/glossary.md` | Canonical Authentication/Authorization and WI terms |
| 1 | `docs/standards/frontend-standards.md` | Axios refresh queue and frontend contract |
| 1 | `docs/policies/security-policy.md` | Token storage and fail-closed replay policy |
| 1 | `docs/policies/quality-gates.md` | Test, review, and traceability gates |
| 2 | `docs/design/api-spec.md` | Current `skipAuthReplay` and normal replay contract |
| 2 | `docs/design/p1-security-acceptance-hardening-design.md` | Existing refresh-session and auth ordering constraints |
| REQ | `deliverables/user/REQ-20260809-ATS-001.md` | Approved parent request |
| WI | `deliverables/agent/WI-20260809-ATS-031-consolidated-findings.md:685,965` | Canonical root and bounded remediation portfolio |
| WI | `deliverables/agent/WI-20260809-ATS-036-handoff.md` | Approved implementation and output contract |

## Evidence Pointers

### Implementation

- `frontend/src/api/client.ts:102-118` rejects ineligible, already-retried,
  opted-out, excluded, and missing-refresh-token requests before replay, then
  sets `_retry` for every remaining eligible request.
- `frontend/src/api/client.ts:120-149` queues an already-marked request behind
  the one in-flight refresh or starts that refresh, then calls `client(...)`
  once for each successful replay path.
- The implementation change is one assignment moved before the `isRefreshing`
  branch. Queue shape, token rotation, storage handling, session clearing,
  ADMIN role synchronization, and navigation code are unchanged.

### Focused Tests

- `frontend/src/api/client.test.ts:318-391` uses one deferred refresh and three
  adapters to prove leader/queued marker timing, one refresh owner, and one
  replay per request.
- `frontend/src/api/client.test.ts:393-446` makes a queued adapter return a
  second `401` and proves the exact second failure is preserved with one refresh
  and one adapter call.
- `frontend/src/api/client.test.ts:91-316,448-533` retains auth exclusions,
  request setup, missing token, `skipAuthReplay`, ADMIN `403`, rotation, storage
  failure, refresh failure, and navigation regression coverage.

### Current Documentation

- `docs/standards/frontend-standards.md:200-209` defines marker timing,
  at-most-once replay, second-`401` rejection, and exclusions.
- `docs/policies/security-policy.md:139-147` records the security invariant next
  to the current token-storage contract.
- `docs/design/api-spec.md:326-341` keeps the execute opt-out contract and adds
  the normal protected-request replay contract.
- `docs/design/p1-security-acceptance-hardening-design.md` remains unchanged as
  a historical design record.

## Red / Green Reproduction

1. Red command: `cd frontend; npm test -- --run src/api/client.test.ts`
   - Before the implementation move: 1 file, 23 tests, 21 passed, 2 failed.
   - Failure 1: queued marker values were `[undefined, undefined]` instead of
     `[true, true]`.
   - Failure 2: the queued second `401` triggered `unexpected second refresh`
     instead of preserving the original second failure.
2. Green command: `cd frontend; npm test -- --run src/api/client.test.ts`
   - 1 file passed; 23 tests passed; 0 failed; exit 0.

## Commands and Outputs

| Lane | Exact command | Result |
| --- | --- | --- |
| Focused | `cd frontend; npm test -- --run src/api/client.test.ts` | PASS: 1 file, 23 tests |
| Independent PG focused rerun | `cd frontend; npm test -- --run src/api/client.test.ts` | PASS: 1 file, 23 tests; exit 0 |
| Frontend full | `cd frontend; npm test` | PASS: 73 files, 829 tests |
| Frontend coverage | `cd frontend; npm run test:coverage` | PASS: 73 files, 829 tests; statements 88.36%, branches 79.58%, functions 88.02%, lines 90.58% |
| `client.ts` coverage | Same coverage command | statements 98.94%, branches 93.15%, functions 100%, lines 98.88% |
| TypeScript | `cd frontend; npm run typecheck` | PASS: exit 0 |
| ESLint | `cd frontend; npm run lint` | PASS: 0 errors, 0 warnings, exit 0 |
| Prettier | `cd frontend; npm run format` | PASS: all matched files formatted, exit 0 |
| Production build | `cd frontend; npm run build` | PASS: 274 modules transformed, exit 0 |
| Documentation | `python .agents/skills/validate-docs/scripts/validate_docs.py` | PASS: Tier 0, internal links, 562 traceability IDs, and document index; exit 0 |
| Whitespace | `git diff --check -- <WI-scoped paths>` | PASS: exit 0; one non-blocking existing CRLF-to-LF warning for `security-policy.md` |

## Security Review

SE static review result: `PASS` for the bounded code change.

- The existing early guard rejects `_retry`, `skipAuthReplay`, auth-excluded,
  non-`401`, and configless failures before refresh token access.
- `_retry` is assigned once after a refresh token is confirmed and before both
  the queue and refresh-leading branches.
- Replayed requests traverse the same response interceptor. A second `401`
  observes `_retry === true` and returns the original rejection before refresh
  or queue processing.
- One boolean owner and the existing finite `failedQueue` remain the only
  concurrency mechanism. No timer, polling, recursion, or unbounded loop was
  added.
- The change does not alter token storage architecture, session lifetime,
  redirect policy, authentication endpoints, Provider behavior, or backend
  state.

Independent PG verdict: `PASS` (2026-08-13).

- Exactly-once replay: `_retry` is set before either refresh ownership or queue
  entry and survives Axios replay for the leader and queued requests.
- Concurrent ownership: synchronous `isRefreshing` ownership plus the finite
  `failedQueue` yields one refresh call for concurrent first `401` responses;
  each queued continuation performs one `client(originalRequest)` call.
- Fail-closed second `401`: the `_retry` guard rejects the exact replay failure
  before refresh-token access, queue entry, session clearing, or navigation.
- Exclusions and failure handling: auth paths and `skipAuthReplay` remain ahead
  of refresh processing; missing-token and both token-persistence failure paths
  retain their tested fail-closed behavior.
- Boundedness and exposure: the WI diff adds no timer, polling, recursion, log,
  credential output, or new storage surface. Test tokens are fixed dummy data.
- Independent focused rerun passed: 1 file, 23 tests, exit 0.

## Risks / Rollback

- Residual verification risk: deterministic Vitest adapter promises prove the
  interceptor race locally, but no deployed-browser timing or live backend was
  exercised. No such external run is required or permitted by this WI.
- Coverage passes configured thresholds, but `client.ts` is not 100% covered
  overall because pre-existing adjacent branches remain uncovered. The changed
  marker and both replay ownership paths are directly exercised.
- Independent PG review closes the final WI-036 acceptance item. The existing
  local deterministic-test limitation remains the only review residual noted
  here; external execution was outside this WI and was not performed.
- Rollback: revert the WI-036 marker-position hunk, the two focused tests, the
  three current-document contract additions, the handoff checkbox updates, and
  both WI deliverables as one scoped patch. No data, schema, Provider, mail, or
  environment rollback is required.

## Side-Effect and Git Record

- No DB, external service, OAuth Provider, payment/refund Provider, mail,
  ignored local secret, protected output artifact, deployment, branch state,
  commit, stage, or push was inspected or changed.
- Verification used only local Vitest, TypeScript, ESLint, Prettier, Vite, the
  repository documentation validator, and a scoped whitespace check.
- The scoped whitespace check did not modify line endings or any file.

## Follow-Up Chain

- Immediate next blocked WI: `WI-20260809-ATS-042`.
- Other handoff-blocked WIs: `WI-20260809-ATS-043`,
  `WI-20260809-ATS-053`, `WI-20260809-ATS-057`, and
  `WI-20260809-ATS-060`.
- The mandatory PG review now closes WI-036 acceptance and releases these WIs
  for the MA's normal orchestration. No subsequent WI was created or delegated
  within this independent review scope.

## Related Documents

- [WI-036 Handoff](WI-20260809-ATS-036-handoff.md)
- [WI-036 User Summary](../user/WI-20260809-ATS-036-summary.md)
- [Frontend Standards](../../docs/standards/frontend-standards.md)
- [Security Policy](../../docs/policies/security-policy.md)
- [API Specification](../../docs/design/api-spec.md)

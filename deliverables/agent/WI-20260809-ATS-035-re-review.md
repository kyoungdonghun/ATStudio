# WI-20260809-ATS-035 Final Independent Reliability Engineer Re-review

## Findings

No blocking or non-blocking reliability findings remain within the requested remediation scope.

## Remediation Evidence

### RESOLVED - Execute owns status reads from preflight through POST recovery

Refund and correction now maintain execute-pending state in both synchronous refs and render state
(`frontend/src/pages/admin/PaymentOperationsPage.tsx:284-304`,
`frontend/src/pages/admin/PaymentOperationsPage.tsx:390-409`). Each execute action acquires that
owner before publishing its `UNKNOWN` intent or starting preflight
(`frontend/src/pages/admin/PaymentOperationsPage.tsx:930-952`,
`frontend/src/pages/admin/PaymentOperationsPage.tsx:1172-1200`) and releases it only on a terminal
preflight exit or after the POST/recovery sequence
(`frontend/src/pages/admin/PaymentOperationsPage.tsx:954-1027`,
`frontend/src/pages/admin/PaymentOperationsPage.tsx:1201-1278`).

The imperative status handlers reject calls while the exact durable ID has an execute owner
(`frontend/src/pages/admin/PaymentOperationsPage.tsx:922-927`,
`frontend/src/pages/admin/PaymentOperationsPage.tsx:1164-1169`). The matching status buttons are
also disabled by execute-pending render state, independently of ordinary read-pending state
(`frontend/src/pages/admin/PaymentOperationsPage.tsx:2361-2376`,
`frontend/src/pages/admin/PaymentOperationsPage.tsx:2499-2519`). This prevents a status GET during
both the preflight window and the pending POST/post-execute recovery window.

The new tests exercise both former race orderings for both domains. They remove the rendered
`disabled` attribute and click again to prove the ref guard, not only the DOM guard, prevents an
extra detail GET. The preflight-owner cases also prove a fresh terminal preflight causes zero POSTs
and keeps the linked domain locked until release
(`frontend/src/pages/admin/PaymentOperationsPage.test.tsx:1704-1779`). The pending-POST cases prove
one POST, no competing status GET, retained linked-domain locks, the required post-execute detail
read, and final `COMMITTED` ownership by the eventual execute result
(`frontend/src/pages/admin/PaymentOperationsPage.test.tsx:1781-1869`).

### RESOLVED - An active status read prevents execute and preserves domain isolation

Manual status reads remain deduplicated by exact domain and durable ID while pending
(`frontend/src/pages/admin/PaymentOperationsPage.tsx:863-873`,
`frontend/src/pages/admin/PaymentOperationsPage.tsx:1105-1115`). Until that read resolves, its
intent remains `UNKNOWN` or `RELOAD_FAILED`; the refund and correction execute entry points first
apply the ambiguity locks
(`frontend/src/pages/admin/PaymentOperationsPage.tsx:364-381`,
`frontend/src/pages/admin/PaymentOperationsPage.tsx:930-932`,
`frontend/src/pages/admin/PaymentOperationsPage.tsx:1172-1177`). The same predicates block the
linked refund/correction target. A successful manual `REQUESTED` or `APPROVED` read can unlock only
after the authoritative detail has returned; execution still requires a later typed operator
action and fresh preflight.

Existing pending-read tests independently prove one read owner and authoritative detail retention
for refund and correction (`frontend/src/pages/admin/PaymentOperationsPage.test.tsx:1646-1702`).
The remediated race tests additionally keep linked-domain execute controls disabled throughout
preflight and POST ownership (`frontend/src/pages/admin/PaymentOperationsPage.test.tsx:1725-1737`,
`frontend/src/pages/admin/PaymentOperationsPage.test.tsx:1763-1778`,
`frontend/src/pages/admin/PaymentOperationsPage.test.tsx:1809-1820`,
`frontend/src/pages/admin/PaymentOperationsPage.test.tsx:1855-1868`).

### RESOLVED - Remaining outcome and authentication gaps

Manual authoritative `SUCCEEDED` reads now have explicit refund and correction coverage proving
`RELOAD_FAILED` converges to `COMMITTED`, updates the exact row, and does not repeat execute
(`frontend/src/pages/admin/PaymentOperationsPage.test.tsx:1212-1278`).

The client test starts a normal protected 401 refresh, then delivers an opted-out execute 401 while
that refresh is in progress. It proves the execute neither enters the queue nor triggers refresh,
adapter replay, or session clearing, while the normal request completes its refresh and replay
(`frontend/src/api/client.test.ts:184-230`). The two execute wrappers remain the only product calls
setting `skipAuthReplay` (`frontend/src/api/admin.ts:801-809`,
`frontend/src/api/admin.ts:878-886`), with API contract assertions at
`frontend/src/api/adminContracts.test.ts:270-281` and
`frontend/src/api/adminContracts.test.ts:312-327`. Other protected 401s retain the existing refresh
path in `frontend/src/api/client.ts:102-148`.

The adjacent positive regression suite still covers refund request/approve/typed execute and
correction preview/request/approve/typed execute
(`frontend/src/test/coverage/adminSubscriberGaps.coverage.test.tsx:1161-1322`).

## Verification

Command run independently from `frontend/`:

```text
npm test -- src/pages/admin/PaymentOperationsPage.test.tsx src/api/client.test.ts src/api/adminContracts.test.ts src/test/coverage/adminSubscriberGaps.coverage.test.tsx
```

Result: **PASS** - 4 test files, 132 tests, 0 failures; exit code 0.

## Verdict

**APPROVE**

The prior RE blocker is remediated. Execute-pending ownership now excludes status recovery across
preflight and POST in both domains, active status reads retain ambiguity ownership, linked-domain
locks remain intact, and the eventual authoritative execute/detail result owns final state. All
previously listed focused residual gaps have direct coverage and the focused plus adjacent run is
green.

## Residual Test Gaps

- The race coverage is React component/interceptor integration coverage with deferred promises,
  not browser E2E timing against a running backend. The paired render and imperative ref guards,
  forced-click assertions, exact call counts, and independent focused pass make this non-blocking
  for WI-035.
- No backend suites were rerun in this narrow re-review because the remediation and prior blocker
  are frontend-only; backend exact-detail and zero-Provider-mutation behavior was not changed.

No product code, output/ZIP artifact, secret, Git index, commit, branch, remote, deployment, or
external service was modified or accessed by this re-review.

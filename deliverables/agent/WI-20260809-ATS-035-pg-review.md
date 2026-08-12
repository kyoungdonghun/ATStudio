# WI-20260809-ATS-035 Security and Privacy Re-review

**Final verdict: APPROVE**

## Remediation Evidence

### RESOLVED - Execute POST auth replay

Only the refund and entitlement-correction execute wrappers set `skipAuthReplay: true`
(`frontend/src/api/admin.ts:801-809`, `frontend/src/api/admin.ts:878-886`). The response interceptor
rejects an opted-out 401 before reading a refresh token, entering the active-refresh queue, or
replaying through `client(originalRequest)` (`frontend/src/api/client.ts:102-126`). This ordering
covers both a direct 401 and an execute 401 received while another request is refreshing: the latter
cannot enter the queue at lines 118-126. No other product request sets this option
(`frontend/src/api/client.ts:7-10`, `frontend/src/api/client.ts:102-109`). Contract tests pin the flag
to the two execute POSTs (`frontend/src/api/adminContracts.test.ts:270-281`,
`frontend/src/api/adminContracts.test.ts:312-327`), and the interceptor test proves no refresh,
adapter replay, or session clearing for the opted-out 401
(`frontend/src/api/client.test.ts:162-181`).

### RESOLVED - Reload hydration and ambiguity locks

An authoritative refund list now hydrates `PROCESSING` and `PENDING_PROVIDER_CONFIRMATION` rows as
exact-ID `UNKNOWN` intents; a correction list does the same for `PROCESSING`
(`frontend/src/pages/admin/PaymentOperationsPage.tsx:418-480`). Hydrated intents render a read-only
status action (`frontend/src/pages/admin/PaymentOperationsPage.tsx:2307-2321`,
`frontend/src/pages/admin/PaymentOperationsPage.tsx:2441-2459`) and block their own and linked
mutations (`frontend/src/pages/admin/PaymentOperationsPage.tsx:322-340`). Refund execute is now
eligible only for `APPROVED`, so `PENDING_PROVIDER_CONFIRMATION` cannot execute after reload
(`frontend/src/pages/admin/PaymentOperationsPage.tsx:2358-2364`). Tests cover reloaded pending
refund, processing correction, linked-domain locks, and read-only status controls
(`frontend/src/pages/admin/PaymentOperationsPage.test.tsx:761-799`); the processing-refund hydration
path is exercised at lines 820-838.

### RESOLVED - Manual unlock and fresh preflight

A manual read can clear a lock only when the current intent is `UNKNOWN` and the exact detail is
`REQUESTED` or `APPROVED` (`frontend/src/pages/admin/PaymentOperationsPage.tsx:818-835`,
`frontend/src/pages/admin/PaymentOperationsPage.tsx:1067-1084`). `RELOAD_FAILED` is preserved before
that unlock branch for every non-committed result, and in-flight statuses remain `UNKNOWN`
(`frontend/src/pages/admin/PaymentOperationsPage.tsx:822-836`,
`frontend/src/pages/admin/PaymentOperationsPage.tsx:1071-1085`). Manual status actions call only the
detail-read functions (`frontend/src/pages/admin/PaymentOperationsPage.tsx:862-867`,
`frontend/src/pages/admin/PaymentOperationsPage.tsx:1111-1116`). Tests cover `APPROVED` unlock,
`REQUESTED` approve-only unlock, in-flight retention, and `RELOAD_FAILED` retention
(`frontend/src/pages/admin/PaymentOperationsPage.test.tsx:820-907`,
`frontend/src/pages/admin/PaymentOperationsPage.test.tsx:1150-1209`).

After an unlock, execution still requires a later typed operator action and a new exact-ID detail
preflight. Only fresh `APPROVED` reaches one POST; unreadable, mismatched, terminal, or in-flight
preflight state returns without mutation (`frontend/src/pages/admin/PaymentOperationsPage.tsx:869-939`,
`frontend/src/pages/admin/PaymentOperationsPage.tsx:1118-1195`). Focused tests prove preflight order,
one explicit POST, and all blocking outcomes
(`frontend/src/pages/admin/PaymentOperationsPage.test.tsx:801-862`,
`frontend/src/pages/admin/PaymentOperationsPage.test.tsx:910-1013`).

### RESOLVED - Stale list ownership

Recovery intents capture the owning view request generation and key
(`frontend/src/pages/admin/PaymentOperationsPage.tsx:146-166`); normal list requests require the
current generation/key (`frontend/src/pages/admin/PaymentOperationsPage.tsx:348-358`), and recovery
list success or failure is applied only while both intent and view ownership remain current
(`frontend/src/pages/admin/PaymentOperationsPage.tsx:841-849`,
`frontend/src/pages/admin/PaymentOperationsPage.tsx:1090-1098`). Tests prove later tab/list ownership
wins over stale success and failure, while exact detail remains authoritative
(`frontend/src/pages/admin/PaymentOperationsPage.test.tsx:1212-1335`,
`frontend/src/pages/admin/PaymentOperationsPage.test.tsx:1338-1394`).

### RESOLVED - Zero automatic execute and Provider retry

Each explicit action performs one preflight GET and at most one execute POST. An execute rejection
performs one detail GET, not another execute call
(`frontend/src/pages/admin/PaymentOperationsPage.tsx:934-970`,
`frontend/src/pages/admin/PaymentOperationsPage.tsx:1190-1228`), and `skipAuthReplay` closes the
shared-interceptor retry route. Response-loss tests assert one execute invocation and only the
preflight plus bounded recovery reads (`frontend/src/pages/admin/PaymentOperationsPage.test.tsx:1015-1100`).
The exact refund detail service test proves both present and missing reads make zero Provider cancel
calls (`src/test/java/com/atstudio/atstudio/service/AdminPaymentRefundServiceTest.java:90-111`). The
unchanged correction detail path remains a local exact-ID repository read and has no Provider call.
Automatic refund execute retries: **0**. Automatic correction execute retries: **0**. Recovery GET
mutation count: **0**. Recovery GET Provider call count: **0**.

## Residual Contract Debt

**MEDIUM, non-blocking for WI-035.** The full refund DTO still contains the raw `idempotencyKey`,
actor emails, and `failureMessage`; the correction DTO still contains actor emails and
`failureMessage` (`src/main/java/com/atstudio/atstudio/dto/payment/AdminPaymentRefundResponse.java:26-36`,
`src/main/java/com/atstudio/atstudio/dto/payment/AdminPaymentEntitlementCorrectionResponse.java:44-51`).
This is pre-existing ADMIN list/detail contract debt, not an incremental WI-035 field,
authorization, recipient, or UI exposure: list and detail already share the same ADMIN-only DTOs,
and WI-035 added callers rather than an endpoint or schema. The extra exact-record reads transport
the same contract to the same authorized ADMIN screen but render none of these fields. Track DTO
minimization separately: omit or mask the idempotency key, retain actor email only with an approved
operational need, and sanitize or omit failure text. This debt does **not** block the bounded
response-loss recovery remediation.

## Verification

- `npm test -- src/api/client.test.ts src/api/adminContracts.test.ts src/pages/admin/PaymentOperationsPage.test.tsx`
  - PASS: 3 files, 87 tests, 0 failures.
- `.\gradlew.bat test --tests "com.atstudio.atstudio.controller.AdminPaymentControllerTest" --tests "com.atstudio.atstudio.service.AdminPaymentRefundServiceTest"`
  - PASS: 23 tests, 0 failures, 0 errors, 0 skipped; Gradle build successful.

No product code was modified by this review. No output/ZIP/ignored-secret inspection, staging,
commit, push, branch, deployment, or destructive action was performed.

# Schema/API Decision Required: WI-20260809-ATS-056

## Status

- Verdict: `APPROVED_WITH_REVISIONS`
- Approval date: `2026-08-12`
- Product, schema, and API changes may proceed only under the approved revised minimal contract below.
- Scope remains limited to `CR-031-117` / `F-INTEG-029-B07` and `CR-031-119` / `F-INTEG-029-B09`.

## Approved Revised Minimal Contract

Resume WI-20260809-ATS-056 under this revised minimal contract:

1. Add a dedicated CSV-import attempt ledger only.
2. Use standard Idempotency-Key with canonical lowercase UUIDv4. Never persist or log the raw key; store an owner-scoped opaque digest/hash using an existing suitable key factory/pattern where possible.
3. Add ADMIN-only attempt list, numeric detail, and operation-key recovery outcome. Prefer recovery key in the Idempotency-Key header, not URL/query.
4. Never retain file bytes, raw CSV rows, raw provider payloads, secrets, or credentials. Per-row validation errors remain response-only.
5. Fresh-only DB baseline: update entity/schema.sql/current docs later; no historical backfill or retained-DB migration and no destructive DB action.
6. Reconciliation gets no new operation-key/attempt recovery API in this WI. It only receives atomic duplicate handling where applicable, orderless/unusable classification, and total = imported + duplicate + failed/status count conservation. Date/range/ceiling/retry policy remains WI-067.
7. Same-key POST must never process the file again. It may return explicit conflict/in-progress/completed recovery guidance; no request fingerprint/file normalization policy is approved. UI must recover by the same key through read-only GET and create a new key only for a new explicit operator action.
8. No Ultra unless parent explicitly says so.

## Feasibility Gate Result

The complete durable, queryable file-attempt evidence contract cannot be met cleanly with the current schema and API.

| Required property | Current structure | Gap |
|---|---|---|
| One durable record for every accepted file attempt | `payment_settlements` stores only successfully inserted rows | An all-duplicate attempt creates no row carrying its generated batch key. |
| Stable operation/batch identity after response loss | `AdminPaymentSettlementService.importSettlements` creates a server UUID and returns it | The UUID is durable only when at least one Settlement is inserted; it is neither a durable attempt identity nor independently recoverable. |
| Structured actor, source, and four aggregate counts | `payment_operation_audit_logs` has row-target fields and one free-text `note` | No batch key or total/imported/duplicate/failed columns exist. Encoding these values in `note`, `order_id`, status fields, or `provider_transaction_id` would semantically overload protected fields. |
| Direct query by attempt identity | Settlement list filters by status/source/date; operation-audit list is only a latest-first page | Neither API can retrieve an all-duplicate attempt or resolve an operation key. |
| Atomic race classification | Import performs `existsByDeduplicationKey` and then `save` in one outer transaction | The unique constraint is the final fence, but a losing constraint exception escapes as an unclassified failure and can roll back unrelated rows and their audits. |
| Exact response-loss retry correlation | Import accepts only file and note | A server-only key cannot identify the same client operation before the response arrives. |

Evidence pointers:

- `src/main/java/com/atstudio/atstudio/service/AdminPaymentSettlementService.java:80-129`
- `src/main/java/com/atstudio/atstudio/service/AdminPaymentSettlementService.java:149-237`
- `src/main/java/com/atstudio/atstudio/entity/PaymentSettlement.java:32-79`
- `src/main/java/com/atstudio/atstudio/entity/PaymentOperationAuditLog.java:25-95`
- `src/main/java/com/atstudio/atstudio/repository/PaymentOperationAuditLogRepository.java:9-18`
- `src/main/java/com/atstudio/atstudio/service/AdminPaymentReadService.java:66-71`
- `src/main/resources/schema.sql:559-601`
- `src/main/resources/schema.sql:813-864`
- `frontend/src/api/admin.ts:705-727`

The orderless reconciliation counter defect is independently fixable with the current response shape. It is not being patched alone because the handoff requires this WI to stop and create only this decision packet when the complete file-attempt contract needs schema or API architecture.

## Alternatives

### Alternative A - Dedicated Attempt Ledger and Client Operation Key (Recommended)

Create a `payment_settlement_attempts` table with structured identity, actor, operation/source, lifecycle state, aggregate counts, and bounded operator note. Require a client-generated operation key for import and reconciliation requests, persist it before row processing, and expose ADMIN-only attempt lookup.

Benefits:

- Represents successful, partial, all-duplicate, and interrupted attempts without fabricating a Settlement row.
- Makes response-loss recovery exact without retaining file bytes, raw rows, Provider payloads, or a file fingerprint.
- Allows one unique operation key to prevent a retry from starting a second processing pass.
- Keeps row-level settlement audit semantics unchanged.

Costs:

- Adds a table, indexes, enums/entity/repository/service, request header, and read API.
- Requires coordinated backend/frontend rollout.
- Requires a separately approved retained-data migration strategy if the target database is not rebuilt from the current fresh-only baseline.

### Alternative B - Dedicated Attempt Ledger with Server-Generated Keys Only

Create the same table but keep the current request contract and generate the key on the server.

This is smaller at the API boundary and makes all-duplicate attempts durable. It does not provide exact correlation when the initial response is lost: an operator can search recent attempts, but a retry cannot prove that it refers to the same operation. This does not fully satisfy the requested response-loss and retry contract.

### Alternative C - Extend `payment_operation_audit_logs`

Add a file-attempt action/target type plus batch key, operation/source, lifecycle state, and four count columns to the existing audit table.

This can satisfy the data contract, but it adds settlement-import-specific nullable columns to a cross-operation row-audit ledger and requires its response/query API to branch by target semantics. It is less cohesive than Alternative A and raises regression risk for refund, receipt, entitlement-correction, incident, and Settlement-row audit consumers.

### Alternative D - Deterministic File Fingerprint as Attempt Identity

Hash normalized file content and use the fingerprint as the idempotency key.

This is not recommended in WI-056. It selects canonical byte/encoding/CSV normalization and retention behavior owned by WI-067 (`CR-031-115`, `CR-031-116`, and `CR-031-118`). It also conflates a file's identity with an operator's distinct attempts.

### Rejected Existing-Structure Workarounds

- Do not insert a synthetic `PaymentSettlement` for a file attempt; it would inflate persisted Settlement and status counts.
- Do not store aggregate JSON or delimited counts in `PaymentOperationAuditLog.note`; that is not a structured, safely queryable contract.
- Do not place a batch key in `provider_transaction_id` or `order_id`; those fields have protected payment/provider semantics.
- Do not treat the current response-only UUID as durable evidence.

## Minimal Recommended Contract

### Data Model

Proposed fresh-baseline DDL, placed before `payment_settlements`:

```sql
CREATE TABLE payment_settlement_attempts
(
    id                     BIGINT NOT NULL AUTO_INCREMENT,
    operation_key          VARCHAR(64) NOT NULL,
    operation_type         ENUM ('CSV_IMPORT', 'MISSING_SETTLEMENT_RECONCILIATION') NOT NULL,
    source                 ENUM ('CSV_MANUAL', 'TOSS_API', 'SYSTEM_RECONCILIATION') NOT NULL,
    actor_user_id          BIGINT NOT NULL,
    state                  ENUM ('PROCESSING', 'COMPLETED', 'FAILED') NOT NULL,
    total_rows             INT UNSIGNED NOT NULL DEFAULT 0,
    imported_rows          INT UNSIGNED NOT NULL DEFAULT 0,
    skipped_duplicate_rows INT UNSIGNED NOT NULL DEFAULT 0,
    failed_rows            INT UNSIGNED NOT NULL DEFAULT 0,
    operator_note          VARCHAR(500) NULL,
    failure_code           VARCHAR(100) NULL,
    completed_at           DATETIME NULL,
    created_at             DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at             DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_payment_settlement_attempts_operation_key (operation_key),
    KEY idx_payment_settlement_attempts_actor_created (actor_user_id, created_at),
    KEY idx_payment_settlement_attempts_operation_created (operation_type, created_at),
    CONSTRAINT fk_payment_settlement_attempts_actor
        FOREIGN KEY (actor_user_id) REFERENCES users (id),
    CONSTRAINT chk_payment_settlement_attempts_completed_counts
        CHECK (state <> 'COMPLETED'
            OR total_rows = imported_rows + skipped_duplicate_rows + failed_rows)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

ALTER TABLE payment_settlements
    ADD KEY idx_payment_settlements_import_batch_key (import_batch_key);
```

Contract rules:

- `operation_key` is opaque operation identity, not a file fingerprint or Provider identifier.
- `operator_note` uses the existing trimmed 500-character maximum and must exclude raw row/file/Provider payload, identifiers, PII, credentials, and secrets.
- `COMPLETED` includes full, partial, and all-duplicate outcomes; row-level failures are represented by `failed_rows`, not by attempt state.
- `FAILED` means orchestration did not produce a normal aggregate response. `failure_code` is a bounded internal enum/code, never a raw exception message.
- The completed-count check is the database backstop for response conservation.
- Settlement rows continue to carry `import_batch_key`; the new index supports exact linkage and recovery queries.
- Do not add a foreign key from existing `payment_settlements.import_batch_key` until retained-data handling is approved. Historical batch rows cannot be truthfully backfilled with actor, duplicate, failed, or note evidence.

### API Diff

```http
POST /api/admin/payments/settlements/import
X-ATStudio-Operation-Key: <client-generated UUID, maximum 64 characters>
Content-Type: multipart/form-data

POST /api/admin/payments/settlements/reconcile
X-ATStudio-Operation-Key: <client-generated UUID, maximum 64 characters>
Content-Type: application/json
```

- The header is required for both ADMIN operations after coordinated rollout.
- The existing `AdminPaymentSettlementImportResponse` shape remains unchanged.
- `importBatchKey` echoes the persisted `operation_key`.
- A newly inserted operation key owns processing. A concurrent or repeated POST with the same key does not process rows again:
  - `PROCESSING`: return a stable conflict/in-progress response and query location.
  - `COMPLETED`: return a stable conflict/already-completed response and query location.
  - `FAILED`: return a stable failed-operation response; a deliberate retry uses a new key.
- The frontend generates one key per explicit operator action. It must not automatically submit a second import. On an uncertain response, it queries the same key.

Proposed ADMIN-only recovery endpoints:

```http
GET /api/admin/payments/settlement-attempts/{operationKey}
GET /api/admin/payments/settlement-attempts?operationType=&source=&state=&page=&size=
```

The attempt response contains only `operationKey`, operation/source/state, actor user ID, the four counts, bounded note/failure code, and timestamps. Original file bytes, raw rows, Provider payload/identifier, and per-row error text are not retained in the attempt ledger. The original POST continues to return its bounded row errors.

### Transaction Boundary

1. Controller remains thin and passes the authenticated ADMIN, operation key, file/request, and note to the service.
2. Parse and validate the synthetic CSV using the current bounded parser contract. No new CSV policy is selected here.
3. Insert the attempt in a short `REQUIRES_NEW` transaction. Only the caller that inserts the unique operation key may process rows.
4. Process each usable row through a separate transaction service using `REQUIRES_NEW`:
   - reconcile the candidate;
   - `saveAndFlush` the Settlement so the unique constraint is evaluated;
   - create the one row-level import/reconcile audit in the same successful transaction.
5. Let a constraint exception escape the row transaction. The non-transactional orchestrator catches it only after rollback, then uses a new read transaction to confirm the exact deduplication key exists before classifying `DUPLICATE`. Any other constraint failure remains failed/unclassified and is not swallowed.
6. Finalize the attempt once in `REQUIRES_NEW` with the four counters and a conditional `state = PROCESSING` update. Exactly one finalization may succeed.
7. Build the HTTP response from the finalized aggregate. Persisted Settlement status counts are derived only from rows imported by this operation and must sum to `importedRows`.

This boundary prevents catch-and-continue work in a rollback-only transaction and ensures a duplicate collision cannot roll back an unrelated valid row from the same file.

## Reconciliation Impact

After approval, orderless finalized payments can be classified without another schema change:

- Increment `failedRows` exactly once.
- Add one bounded error using the existing error DTO and a row/index reference that does not expose a raw Provider identifier.
- Do not create a Settlement or row-level Settlement audit for the unusable payment.
- Finalize the reconciliation attempt with `totalRows == importedRows + skippedDuplicateRows + failedRows`.
- Keep `statusCounts.values().sum() == importedRows`.

## Migration Impact

- Current repository policy describes a fresh-only MySQL baseline. For an empty database, create the attempt table before `payment_settlements`, add the batch-key index, and start with `ddl-auto=validate`.
- A retained database cannot reconstruct truthful historical attempt actor, duplicate count, failed count, or operator note from Settlement rows. Do not synthesize those values.
- If retained data exists, a separate approved migration must choose one of:
  - cut over without historical attempt rows and leave the Settlement batch key without a foreign key; or
  - rebuild from a verified-empty database under the current fresh-baseline procedure.
- Adding a future foreign key requires a separate completeness audit and approved migration/backfill policy.
- Backend and frontend must deploy together when the operation header becomes required. A short compatibility window may accept a server-generated key, but that path provides weaker response-loss correlation and must be explicitly time-bounded if approved.

## Rollback Impact

- Before deployment: revert only the approved WI-056 code/schema/API hunks; no data action is needed.
- After deployment: stopping use of the new endpoint/header is non-destructive, but dropping the attempt table or index is destructive and requires separate approval, count verification, backup, and rollback rehearsal.
- Existing Settlement rows and row-level audits must not be deleted or rewritten as part of rollback.
- A client rollback may continue sending the extra header harmlessly only while the backend accepts it; removing the recovery UI does not remove durable attempt records.

## Test Impact

Focused implementation evidence required after approval:

- Unit tests: all-valid, mixed-invalid, all-duplicate, sequential replay, orderless reconciliation, count conservation, status-count conservation, bounded note, duplicate operation key, and stable attempt state transitions.
- Deterministic H2 concurrency integration: two independent import transactions for one deduplication key produce one Settlement, one row audit, and complementary imported/duplicate attempt aggregates.
- Multi-row H2 concurrency integration: the losing duplicate row does not erase an unrelated valid row from the same operation.
- Durable recovery integration: response-equivalent attempt data remains queryable for all-duplicate and response-loss scenarios.
- Controller/security tests: missing/invalid operation key, ADMIN-only POST/GET, same-key retry behavior, and unchanged response shape.
- Frontend tests: one key per explicit action, same-key recovery query after an uncertain response, no automatic second import, all returned errors preserved, and WI-041 partial/IGNORE regressions.
- No-external-effect assertions: zero Provider, payment, refund, subscription, billing-agreement, receipt, and mail mutations/invocations.
- Full `Q-ALL` runs only after the bounded patch is stable and independent QA-INTEG review is scheduled.

H2 can prove Spring transaction separation, unique-constraint race handling, rows/audits/counts, and deterministic application outcomes. It does not prove MySQL InnoDB lock timing, deadlock behavior, isolation semantics, online DDL behavior, or retained-data migration. Those remain explicit MySQL rehearsal risks.

## Approval Questions

1. Approve Alternative A with a dedicated attempt ledger and required `X-ATStudio-Operation-Key` for import and reconciliation?
2. Confirm the first approved rollout targets the documented fresh-only database baseline, with retained-data migration explicitly out of scope?
3. Approve ADMIN-only detail and paged-list attempt recovery endpoints while keeping per-row errors response-only and minimized?

Until these decisions are approved, WI-056 implementation and tests must not proceed.

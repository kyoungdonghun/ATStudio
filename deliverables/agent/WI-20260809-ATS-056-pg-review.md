---
version: 1.0
last_updated: 2026-08-12
project: ATS
owner: PG
category: audit
status: accepted
dependencies:
  - path: WI-20260809-ATS-056-handoff.md
    reason: Approved scope and security boundaries
  - path: WI-20260809-ATS-056-schema-api-decision.md
    reason: Approved attempt-ledger and recovery contract
---

# PG Review: WI-20260809-ATS-056

## Final Decision

**APPROVE**

The implemented repository/H2 contract satisfies the WI-056 privacy and
security requirements. Approval is limited to source and test evidence; it does
not attest to production infrastructure logging configuration or a MySQL
rehearsal.

## Finding History

| Priority | Initial concern | Correction and evidence | Status |
|---|---|---|---|
| P1 | A recoverable operation key could leak through persistence, URLs, or application logs. | Recovery accepts `Idempotency-Key` only in the header. Persistence stores a SHA-256 digest scoped by operation namespace and ADMIN owner; controller tests assert that captured application output excludes the raw key and note. | RESOLVED |
| P1 | A global digest could let one ADMIN recover another ADMIN's attempt by raw key. | Digest input includes the authenticated ADMIN ID, so header recovery resolves only that ADMIN's digest. Same raw UUID used by two ADMINs produces separate attempts. The ADMIN audit list and numeric detail are intentionally global and expose the recorded actor; they are not owner-scoped. | RESOLVED |
| P2 | Operator note guidance could overstate that free text never contains secrets. | The note is optional and bounded to 500 characters, the UI warns against sensitive input, and policy now states that users can still paste sensitive data. Request-target/query logging must omit or redact the note. | RESOLVED |
| P2 | Recovery might retain raw CSV or row/provider evidence. | The attempt ledger retains only aggregate counts, state, owner, opaque digest, bounded note/failure code, and timestamps. CSV bytes, raw rows, provider payloads, per-row errors, and raw keys are not retained in the attempt. | RESOLVED |

## Security Evidence

- `AdminPaymentController.java:126-169`: ADMIN-only import/list/detail/recovery;
  import and recovery consume the key from `Idempotency-Key`. List and numeric
  detail are global ADMIN audit views with actor attribution; only header
  recovery is isolated by the current ADMIN's digest.
- `PaymentCommandKeyFactory.java:27-44`: canonical lowercase UUIDv4 validation
  and owner-scoped SHA-256 digest generation.
- `PaymentSettlementImportAttempt.java:25-108` and `schema.sql:557-583`:
  minimized durable attempt fields and bounded note/failure code.
- `AdminPaymentControllerTest.java:99-184`: authorization/header contract and
  captured application-output exclusion for raw key and note.
- `AdminPaymentSettlementImportIntegrationTest.java:209-340`: owner isolation
  and absence of payment/refund/subscription/billing-agreement/receipt,
  Provider, or mail mutation.
- `PaymentOperationsPage.tsx:710-830,1707-1832`: session-scoped pending key,
  manual read-only recovery, and the operator-note warning.
- `docs/policies/security-policy.md`: canonical application and operations
  responsibilities for header capture and free-text handling.

## Required Operational Boundary

- Raw `Idempotency-Key` values must not be put in URL/query, database fields,
  or application logs.
- Access logs, reverse proxies, tracing, and APM must be configured not to
  collect or record this header. That infrastructure control is an operator
  responsibility and was not verified by repository tests.
- Operator note is optional free text with a 500-character limit. The system
  does not derive or intentionally copy a secret into it, but it has no DLP
  guarantee and an operator can enter sensitive data. UI warning, operational
  prohibition, access control, retention, and request-target redaction remain
  required.

## Residual Risk

- No live infrastructure inspection proved header/query redaction.
- No current MySQL race, driver-message, retained-data, or migration rehearsal
  was performed.
- The browser keeps a pending raw operation key in `sessionStorage` for manual
  recovery. This is intentionally session-scoped, but browser/XSS and shared
  workstation controls remain part of the existing frontend security boundary.
- WI-067 CSV parser hardening remains held and out of scope.

## Related Documents

- [WI-056 Handoff](WI-20260809-ATS-056-handoff.md)
- [WI-056 Schema/API Decision](WI-20260809-ATS-056-schema-api-decision.md)
- [WI-056 QA-INTEG Review](WI-20260809-ATS-056-qa-integ-review.md)
- [Security Policy](../../docs/policies/security-policy.md)

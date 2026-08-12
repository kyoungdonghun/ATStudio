---
version: 1.1
last_updated: 2026-08-13
project: ATS
owner: PG
category: audit
status: accepted
dependencies:
  - path: WI-20260809-ATS-067-handoff.md
    reason: Approved implementation and review contract
  - path: WI-20260809-ATS-067-decision-register.md
    reason: Approved DG-067 security and evidence decisions
  - path: WI-20260809-ATS-056-pg-review.md
    reason: Preserved idempotency, ownership, and minimization boundary
  - path: WI-20260809-ATS-067-qa-integ-review.md
    reason: Current independent integration review v1.2
---

# PG Review: WI-20260809-ATS-067

## Current Findings

No open P1 or P2 finding.

## Finding History

### P2 - Unicode line separators bypassed identifier newline rejection

**Paths:** `PaymentSettlementCsvParser.java:105-190`,
`AdminPaymentSettlementService.java:388-447,547-556,564-581`,
`AdminPaymentSettlementRowTransactionService.java:42-55`, and
`AdminPaymentSettlementServiceTest.java:725-761`.

**Status: RESOLVED**

`AdminPaymentSettlementService.java:459-567` now routes Provider and all four
identifier fields through one evidence-character predicate. The predicate
rejects only ISO controls plus Unicode `LINE_SEPARATOR` and
`PARAGRAPH_SEPARATOR`; it does not reject letters, marks, digits, punctuation,
symbols, or ordinary internal whitespace beyond the previously approved edge
whitespace rule.

`AdminPaymentSettlementServiceTest.java:763-808` covers `U+2028` and `U+2029`
in Provider, `provider_payment_key`, `provider_settlement_id`, `order_id`, and
`provider_status`. The same request accepts and preserves Korean text in all
four identifier fields, proving that the correction is not an ASCII-only or
overbroad Unicode ban.

The test produces ten fixed row errors, one valid import, and exactly one
Settlement save. Source inspection confirms each rejected row exits
`toSettlement` before `persistImported`; the only import audit call is inside
that row transaction after the save. Invalid evidence therefore reaches
neither Settlement nor audit mutation, deduplication, or ADMIN evidence.

## Decision

**ACCEPT**

No blocking security or privacy finding remains at the reviewed repository and
non-database evidence boundary. The original P2 is resolved in current source
and focused executable tests. Acceptance excludes infrastructure logging and
DG-067-09B MySQL evidence.

## Passing Security Evidence

- `AdminPaymentController.java:126-170` and `SecurityConfig.java:26,136` keep
  import, recovery, and reconciliation ADMIN-only. Import note binds only as a
  multipart part; the operation key remains header-only.
- `frontend/src/api/admin.ts:724-774` sends FormData without query parameters,
  disables auth replay for the POST, and performs recovery by GET only.
- `PaymentCommandKeyFactory.java:27-44` validates canonical lowercase UUIDv4
  keys and derives an owner-scoped SHA-256 digest. Persistence and responses do
  not expose the raw key or digest.
- `AdminPaymentSettlementService.java:90-180,348-379` validates the filename,
  MIME family, declared and actual 5 MiB limits before attempt claim. Strict
  UTF-8 and parser failures produce fixed messages without raw row values.
- `PaymentSettlementCsvParser.java:20-80,83-239` strictly decodes UTF-8, bounds
  retained nonblank records to the header plus 1,000 rows, and stops at the
  1,001st row. The 5 MiB byte envelope bounds a single large cell or blank
  suffix.
- Import errors are response-only and fixed-format. Identifier validation now
  rejects ISO controls and both Unicode newline-separator categories without
  copying field values into errors. Attempt persistence retains
  aggregate counts, actor, digest, bounded note/failure code, and timestamps;
  it does not retain file bytes, raw rows, Provider payloads, or per-row errors.
- Reconciliation uses a 90-day range, a 5,001-row probe, a 5,000-row execution
  ceiling, deterministic ID order, and at most 200 error details with an exact
  omitted count. The reviewed dependency graph contains no Provider, mail,
  receipt, billing, refund-command, or subscription-command collaborator.
- Browser persistence in `settlementImportAttempt.ts:3-94` contains only the
  operation key and fixed scope metadata. File bytes and operator note remain
  transient React/FormData state.
- Bootstrap tooling validates the exact disposable-name and loopback target,
  keeps credentials in process environment rather than command arguments,
  emits bounded non-secret fields, and checks the unrecorded manifest before
  credential loading. `Observe` retains exact-created-target cleanup.

## Residual Risks

- Endpoint-specific file and note limits are enforced after Spring multipart
  resolution. The shared configuration permits a 30 MB part and 60 MB request
  (`application.yml:33-37`), while the service later limits the file to 5 MiB
  and truncates note persistence to 500 characters. This is bounded and
  ADMIN-only, but retains authenticated multipart parsing amplification.
- Formula-leading identifier text is not executed by the importer or React UI,
  and WI-067 adds no Settlement CSV export. Any future spreadsheet/export lane
  must neutralize formula cells before release.
- Application tests cannot prove proxy, access-log, tracing, or APM suppression
  of `Idempotency-Key` or unsolicited query text. Operational redaction remains
  required.
- Working-tree files were inspected directly under the no-Git restriction; no
  HEAD-relative diff inventory is claimed.

## Verification

- Exact remediation test: **1 passed**.
- Pure backend unit scope: `PaymentSettlementCsvParserTest` 12 tests plus
  `AdminPaymentSettlementServiceTest` 48 tests -> **60 passed**. These are
  parser/Mockito tests and load no database.
- Focused frontend contracts: `adminContracts.test.ts`,
  `settlementImportAttempt.test.ts`, and `PaymentOperationsPage.test.tsx` ->
  **3 files, 120 tests passed**.
- Non-database bootstrap guard suite -> **18 checks passed**, including pending
  manifest refusal before credentials, command-line/output credential absence,
  target redaction, exact-target cleanup, and no unrelated enumeration.
- Static rescans confirmed multipart-only note transport, header-only
  idempotency, strict UTF-8, the 1,001 retained-record bound, the 5 MiB+1 read
  guard, owner-scoped digest recovery, no automatic second POST, bounded
  reconciliation/error memory, and operation-key-only session storage.
- No database, external system, secret, repository `output/` artifact, or Git
  command was accessed.

## Residual DG-067-09B Gate

**HELD: `NOT RUN - NOT APPROVED`.** No MySQL connection or disposable database
action was performed. H2, source counts, and static guard evidence do not prove
the MySQL manifest or concurrency behavior. A new immediate destructive/test
approval remains mandatory before exact-name create/apply/observe/validate/drop
execution.

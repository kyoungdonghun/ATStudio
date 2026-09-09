---
version: 1.2.0
last_updated: 2026-08-17
project: ATS
owner: qa-integ
category: work-summary
status: blocked
dependencies:
  - path: ../agent/WI-20260816-ATS-001-evidence-pack.md
    reason: Sanitized fail-closed execution evidence
---

> Historical public derivative, published in this working tree on 2026-09-09 by WI-20260909-ATS-018. Original dates, language, status, decisions and reported outcomes below are historical, not current approval or reverified recovery/production evidence. The original remains unchanged in private archive `v1-artifacts-20260909-084405`. See [A074: original identity, public SHA-256 and review](../../docs/registry/development-history-recovery-20260909.md#a074). Raw/local-only evidence pointers are lookup keys, not files supplied by a source checkout.

# WI-20260816-ATS-001 Summary

## Execution Status

**BLOCKED.** The authorized current rerun reached preflight and stopped with
`PREFLIGHT_BUNDLE_INVALID`. This is an actual fail-closed result, not a
historical state. No connection, metadata access, or SQL operation was
attempted.

## Current Rerun Result

- `PATCH_TEXT_AUDIT=PASS`: the approved source was confirmed as one
  `CREATE TABLE user_consents` statement with no DML, `DROP`, or `ALTER`.
- `CONNECTOR_J_DISCOVERY=PASS`: an existing Connector/J candidate was located
  without recording its location.
- `PREFLIGHT_BUNDLE_INVALID`: the private input was parsed only in memory but
  did not establish every required form, completeness, and scope condition.
  No input value, diagnostic, or identity was retained.
- `DB_CONNECTION=NOT_RUN`, `PRE_STRUCTURE=NOT_RUN`,
  `PATCH_APPLICATION=NOT_RUN`, and `POST_STRUCTURE=NOT_RUN`.
- `DATA_ROWS_QUERIED=0`; no retry, repair, rollback, drop, or compensating
  action is authorized.

## Attempt History

### 1. Fixed-Path Attempt (Historical)

- `PATCH_TEXT_AUDIT=PASS`: the approved patch was confirmed as one
  `CREATE TABLE user_consents` statement with no DML, `DROP`, or `ALTER`.
- `CONNECTOR_J=PASS`: an existing Connector/J candidate was available.
- `EXPECTED_BUNDLE=FAIL`: the required opaque credential transport was absent.
- `URL_SCOPE=NOT_RUN`, `DB_CONNECTION=NOT_RUN`, `PRE_STRUCTURE=NOT_RUN`, and
  `POST_STRUCTURE=NOT_RUN`.

### 2. Amended Bounded Recovery Attempt

- `DISCOVERY=PASS`: regular-file name/path metadata was inspected only in the
  approved bounded root.
- `OPAQUE_BUNDLE_VALIDATION=FAIL_ZERO_VALID`: no candidate both passed the
  existing allowlisted bundle parser and internally classified as the one
  approved local database scope.
- `TARGET_SCOPE=NOT_RUN`, `PATCH_TEXT_AUDIT=NOT_RUN`, `DB_CONNECTION=NOT_RUN`,
  `PRE_STRUCTURE=NOT_RUN`, `PATCH_APPLICATION=NOT_RUN`, and
  `POST_STRUCTURE=NOT_RUN`.
- No fallback, recency-based selection, inherited environment credential, or
  retry was used.

### 3. Authorized Opaque Recovery Path (Current Result)

- The current single-use path began only after source audit and Connector/J
  discovery passed, then stopped at `PREFLIGHT_BUNDLE_INVALID`.
- The in-memory preflight did not establish permission to connect. It therefore
  did not perform the absence precheck, execute the approved SQL, or inspect
  post-structure state.
- The failure consumes this current rerun. It does not permit another input,
  fallback, rerun, repair, rollback, or scope expansion.

## Database Outcome

- `PATCH_APPLIED_ONCE=NO`: no DDL attempt occurred in the current rerun.
- No table, data, schema object, application process, browser, provider, mail,
  OAuth, payment/refund flow, Git state, or product source was changed by the
  current rerun.
- No rollback was performed or authorized; no database object was created,
  deleted, repaired, or dropped.

## Dependency Outcome

`WI-20260816-ATS-002` **REMAINS BLOCKED**. Its predecessor did not reach a
successful precheck, permitted patch application, or postcondition evidence.
This WI does not start WI-002.

## Residual Risk

The approved patch remains unapplied and structural state was not observed in
this WI. Any future recovery requires separate approval and must not reuse this
single-use execution boundary.

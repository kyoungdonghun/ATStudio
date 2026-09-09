
> Historical public derivative, published in this working tree on 2026-09-09 by WI-20260909-ATS-018. Original dates, language, status, decisions and reported outcomes below are historical, not current approval or reverified recovery/production evidence. The original remains unchanged in private archive `v1-artifacts-20260909-084405`. See [A093: original identity, public SHA-256 and review](../../docs/registry/development-history-recovery-20260909.md#a093). Raw/local-only evidence pointers are lookup keys, not files supplied by a source checkout.

# WI-20260817-ATS-027 Summary

## Result

Completed. The development database and the active client acceptance database now contain the same scoped administrator and playable dummy catalog.

## Seeded Scope Per Environment

- One verified `ADMIN` account with the shared protected credential.
- 20 dummy tags: five each for `USAGE`, `GENRE`, `MOOD`, and `INSTRUMENT`.
- Ten active `AT.M Demo` tracks with stored audio, positive duration, waveform data, and four tag relations each.
- One active `AT.M Demo Collection - 10 Tracks` album containing exactly those ten tracks.

## Development Schema Alignment

- The development database was one table behind the current 43-table schema.
- Only the missing `payment_settlement_import_attempts` table was added.
- No existing table, user data, media, payment data, or unrelated catalog data was deleted or modified.
- A temporary development runtime started with `ddl-auto=validate` and returned API HTTP 200 before catalog seeding.

## Verification

- Both environments: verified admin 1, dummy tags 20, active dummy tracks 10, media-and-waveform-ready tracks 10, tag relations 40, active dummy album 1, album membership 10, distinct membership 10.
- Each of the ten tracks passed HTTP media retrieval with a Range request.
- The active public client endpoint returned HTTP 200 and exposed all ten dummy tracks with duration and waveform metadata.

## Not Performed

- No existing unrelated data cleanup or deletion.
- No payment, refund, email delivery, provider, or remote database action.
- No application source or configuration change.

## Credential Handling

- The shared credential was generated with high entropy and stored only in a current-user-protected local file.
- It is not recorded in this document, source, Git, logs, or evidence.

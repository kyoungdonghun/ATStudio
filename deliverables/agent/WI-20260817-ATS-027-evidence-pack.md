
> Historical public derivative, published in this working tree on 2026-09-09 by WI-20260909-ATS-018. Original dates, language, status, decisions and reported outcomes below are historical, not current approval or reverified recovery/production evidence. The original remains unchanged in private archive `v1-artifacts-20260909-084405`. See [A052: original identity, public SHA-256 and review](../../docs/registry/development-history-recovery-20260909.md#a052). Raw/local-only evidence pointers are lookup keys, not files supplied by a source checkout.

# Evidence Pack: WI-20260817-ATS-027

## Scope And Isolation

- Targets: exactly one guarded local development database and one guarded local client acceptance disposable database.
- Development guard accepted only loopback MySQL database `atstudio`.
- Client guard accepted only a loopback disposable database matching the approved acceptance naming rule.
- No remote, production, stage, payment, refund, or email provider connection was made.

## Schema Evidence

- Current source schema: 43 tables.
- Development preflight: 42 tables with exactly one missing table, `payment_settlement_import_attempts`.
- Additive alignment created only that table.
- Post-alignment report: all 43 source tables present.
- Temporary development runtime: `ddl-auto=validate` startup passed and `GET /api/tracks?page=1&size=1` returned HTTP 200.

## Supported Workflow Evidence

- ADMIN account: direct JDBC provisioning only, because registration would dispatch verification email. Provisioning enforced loopback and target guards, used BCrypt, verified persisted role and email verification state, and emitted no credential or datasource values.
- Tags: `POST /api/tags` via authenticated ADMIN API.
- Tracks: `POST /api/tracks` multipart upload, audio analysis/storage/waveform generation, then `PUT /api/tracks/{id}` activation and tag attachment.
- Album: `POST /api/albums` multipart upload, then `POST /api/albums/{id}/tracks` for each track.
- Playback: `GET /api/tracks/{id}/stream` with `Range: bytes=0-1023` returned HTTP 200 or 206 for every scoped track.

## Exact Persisted-State Parity

| Metric | Development | Client acceptance |
|---|---:|---:|
| Verified scoped ADMIN | 1 | 1 |
| Scoped tags | 20 | 20 |
| USAGE / GENRE / MOOD / INSTRUMENT | 5 / 5 / 5 / 5 | 5 / 5 / 5 / 5 |
| Active scoped tracks | 10 | 10 |
| Tracks with media, duration, waveform | 10 | 10 |
| Scoped track-tag relations | 40 | 40 |
| Active scoped albums | 1 | 1 |
| Scoped album memberships | 10 | 10 |
| Distinct scoped album tracks | 10 | 10 |

## Public Observation

- The active public client catalog endpoint returned HTTP 200.
- Its scoped catalog query returned ten `AT.M Demo` tracks; all ten had positive duration and non-empty waveform metadata.

## Tools And Reproducibility

- Temporary local tools live under the current user's external local application directory, not the repository: guarded ADMIN provisioner, guarded schema alignment reporter, catalog seed/verify tool, and persisted-state proof tool.
- All tools accept protected external bundles internally and emit only aggregate safe statuses/counts.
- The temporary development runtime was stopped after verification; the client acceptance runtime remained unchanged and running.

## Rollback Boundary

- No cleanup was performed. Removing the scoped administrator, tags, tracks, media, and album requires a new explicit approval because it is destructive.
- Credential material remains only in a current-user-protected local file until a later approved cleanup decision.


> Historical public derivative, published in this working tree on 2026-09-09 by WI-20260909-ATS-018. Original dates, language, status, decisions and reported outcomes below are historical, not current approval or reverified recovery/production evidence. The original remains unchanged in private archive `v1-artifacts-20260909-084405`. See [A054: original identity, public SHA-256 and review](../../docs/registry/development-history-recovery-20260909.md#a054). Raw/local-only evidence pointers are lookup keys, not files supplied by a source checkout.

# Evidence Pack: WI-20260817-ATS-028

## Summary

- Read-only independent review of WI-027 seed evidence and current code/API contracts: **FAIL** due to a documented direct-JDBC account bypass and insufficient canonical/isolation proof.

## Scope / DoD Check

- [ ] Only the two approved local target classes were independently confirmed. WI-027 states this, but provides no independently reproducible non-secret target-binding artifact.
- [ ] Exact scoped account, tag, track, media/waveform, album, and parity claims were independently confirmed. Aggregate counts are present, but identity/value/mapping/order comparisons are absent.
- [ ] Development schema change was independently confirmed as additive and bounded. Current source has the stated table; no live pre/post delta evidence was supplied.
- [x] Relevant current application contracts were checked read-only.
- [x] Evidence pointers and unresolved risks are recorded without credentials, datasource values, protected bundle contents, or live target access.

## Reference Documents

| Tier | Document | Review use |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Read-only review and language/traceability baseline |
| 0 | `docs/standards/development-standards.md` | Code contract review baseline |
| 1 | `docs/policies/security-policy.md` | Credential-safe evidence boundary |
| 1 | `docs/policies/quality-gates.md` | Evidence and risk review baseline |
| REQ | `deliverables/user/REQ-20260817-ATS-011.md` | Approved targets, workflow constraint, strict proof, and parity DoD |
| Context | `deliverables/user/WI-20260817-ATS-027-summary.md` | Submitted outcome claim |
| Context | `deliverables/agent/WI-20260817-ATS-027-evidence-pack.md` | Submitted method and aggregate proof claim |
| Handoff | `deliverables/agent/WI-20260817-ATS-028-handoff.md` | Review scope, constraints, and output contract |

## Evidence Pointers

- `deliverables/user/REQ-20260817-ATS-011.md` - Scope requires supported service/API/storage creation for the account and catalog; it requires strict per-environment UI, API, storage, persisted-state, and canonical parity proof.
- `deliverables/agent/WI-20260817-ATS-027-evidence-pack.md` - Explicitly records direct JDBC provisioning for the ADMIN account and provides only aggregate count parity; it does not include a safe canonical manifest/hash or target-binding proof output.
- `src/main/java/com/atstudio/atstudio/controller/TagController.java` - ADMIN-protected `POST /api/tags` contract exists.
- `src/main/java/com/atstudio/atstudio/service/TrackService.java:64-100` - Multipart track creation analyzes audio, writes public storage, persists duration and waveform, and saves supplied tag relations.
- `src/main/java/com/atstudio/atstudio/service/TrackService.java:154-164` - Public streaming resolves an active track's stored public audio resource.
- `src/main/java/com/atstudio/atstudio/service/TrackService.java:168-213` - ADMIN update supports activation and replacement of track tags.
- `src/main/java/com/atstudio/atstudio/service/AlbumService.java:151-173` - ADMIN album membership accepts active tracks only, rejects duplicates, and assigns order.
- `src/main/resources/schema.sql:574-600` - Current source definition for `payment_settlement_import_attempts`.
- `src/main/resources/schema.sql:993-1003` - Composite primary key prevents duplicate album-track membership in the source schema.

## Read-Only Checks And Outputs

- `rg -n "^CREATE TABLE|payment_settlement_import_attempts" src/main/resources/schema.sql` -> 43 source `CREATE TABLE` definitions, including `payment_settlement_import_attempts`.
- `git diff --name-only` -> no tracked-file diff at review time.
- `git status --short` -> pre-existing untracked deliverables and unrelated workspace artifacts were present; none was modified or used as proof.
- Code inspection of the controllers, services, DTOs, and schema paths cited above -> catalog workflow contracts exist, but they do not cure the documented JDBC account-creation deviation or establish live-state claims.

## Tests

- No build or test was run: a read-only review must not create build outputs.
- No database query, HTTP request, runtime restart, credential/bundle read, or external interaction was performed.

## Risks / Rollback

- Risks: Live target isolation, persisted contents, strict UI observation, full media retrieval, and exact canonical parity remain unverified. The active public client claim is not independently reproduced.
- Rollback: None. This review made no runtime, database, source, configuration, or deletion change.

## Required Follow-Up

- Under a new approved WI, use a supported non-email ADMIN provisioning path or amend the requirement before any further seed action. Then produce credential-safe, reproducible target guards and canonical per-environment manifests/hashes covering account state, tag values, track IDs, tag mappings, media/waveform status, and ordered album membership, with UI/API/storage/persistence observations separated.

## Changed Files

- `deliverables/user/WI-20260817-ATS-028-summary.md`
- `deliverables/agent/WI-20260817-ATS-028-evidence-pack.md`

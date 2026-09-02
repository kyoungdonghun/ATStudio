# Evidence Pack: WI-20260902-ATS-001

## Summary

- Established the current runtime/storage mismatch, historical asset location, and all persistent file-reference domains without mutating data.

## Scope / DoD Check

- [x] Identified current backend branch/profile/config selection.
- [x] Compared local configuration before/after this session without revealing values.
- [x] Checked historical Track keys against the current and historical runtime roots.
- [x] Mapped public/private persistent asset domains and current missing-reference counts.
- [x] Identified acceptance lifecycle validation/readiness gaps.

## Reference Documents

| Tier | Document | Reason |
|---|---|---|
| 0 | docs/standards/core-principles.md | Project constitution |
| 0 | docs/standards/development-standards.md | Runtime implementation standard |
| 1 | docs/policies/security-policy.md | Storage key and private-resource handling |
| 1 | docs/policies/quality-gates.md | Verification scope |
| 2 | scripts/acceptance/README.md | Existing lifecycle contract |
| 2 | scripts/acceptance/AcceptanceLifecycle.psm1 | Bundle validation and readiness behavior |
| 2 | src/main/resources/application.yml | Storage fallback chain |
| 2 | src/main/resources/application-acceptance.yml | Acceptance profile overlay |

## Evidence Pointers

- `src/main/resources/application.yml:83-91` defines the default storage fallback and storage-recovery properties.
- `src/main/resources/application-acceptance.yml:1-43` fixes acceptance schema/profile behavior but does not override storage roots.
- `scripts/acceptance/AcceptanceLifecycle.psm1:15-54` lists storage paths as optional bundle values.
- `scripts/acceptance/AcceptanceLifecycle.psm1:1144-1242` starts the tunnel/backend/frontend and declares HTTP-only readiness.
- `src/main/java/com/atstudio/atstudio/service/storage/StorageReferenceChecker.java` checks reference ownership only for cleanup/recovery; it does not enumerate missing physical files.
- `src/main/java/com/atstudio/atstudio/service/storage/StorageMutationRecoveryService.java` handles incomplete journal mutations; it is not a whole-storage integrity audit.
- `src/main/resources/schema.sql:175-190,219-240,246-260,935-967,973-988` identifies persistent file-reference tables.
- Development DB read-only inventory: active Track audio 3 missing, active Track thumbnails 3 missing, active Album thumbnails 3 missing, Notice attachment 1 missing; other inspected domains had no missing references.
- Historical acceptance public root contains the three historical Track audio/thumbnail keys; their `storage_mutations` rows are `CREATE` and `DONE` from 2026-08-08, with no later delete/replace entry.

## Commands & Outputs

- Read-only local API Range requests: Track 3 stream returned HTTP 500; a current-root Track stream returned HTTP 206.
- Read-only database inventory used the configured local datasource internally and emitted only counts, record IDs, and opaque generated keys; no datasource or credential value was printed.
- Exact-key search across project, local ATStudio runtime, and user workspace roots located historical files under the previous acceptance runtime root.

## Risks / Rollback

- Risk: current development data contains legacy references outside its configured storage root.
- Risk: acceptance may silently start against fallback storage if bundle paths are omitted.
- Rollback: no mutation occurred. Future code/doc changes can be reverted by the associated commit; future asset repair requires a separately approved source/target inventory.

## Follow-ups

- WI-20260902-ATS-002: explicit runtime storage contract and integrity reporting.
- WI-20260902-ATS-003: operational documentation and asset-repair decision record.

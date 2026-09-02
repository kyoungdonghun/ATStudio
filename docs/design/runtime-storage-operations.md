---
version: 1.0
last_updated: 2026-09-02
project: ATS
owner: SA
category: design
status: stable
dependencies:
  - path: ../../src/main/java/com/atstudio/atstudio/service/storage/StorageIntegrityService.java
    reason: Read-only persisted-reference audit implementation
  - path: ../../scripts/acceptance/AcceptanceLifecycle.psm1
    reason: Acceptance runtime bundle enforcement
  - path: ../../docs/policies/security-policy.md
    reason: Public/private storage and key-exposure constraints
---

# Runtime Storage Operations

## Purpose

A runnable ATStudio environment is one explicit tuple:

```text
database + public storage root + private storage root
```

The tuple is an operational contract. Restarting an application against the
same database but different roots is an environment change, not an equivalent
restart. Persisted object references can otherwise remain valid database text
while the corresponding file is unavailable.

## Root Rules

- Public and private roots must be distinct, non-nested real directories.
- Acceptance requires explicit absolute `APP_STORAGE_PUBLIC_PATH` and
  `APP_STORAGE_PRIVATE_PATH` values. It never falls back to repository-relative
  `uploads` or `private-uploads` paths.
- The acceptance backend environment bundle treats both root variables as
  required, validates them before any tunnel/backend/frontend child process is
  launched, and injects them only into the backend process.
- Explicit local configuration remains untracked. It may use local roots, but
  an operator must keep them paired with the database that owns their file
  references.

## Integrity Audit

`StorageIntegrityService` is read-only. It checks whether each persisted
reference resolves through `StorageService` without changing data or files.

| Domain | Root | Checked reference |
|---|---|---|
| Track | PUBLIC | audio, optional thumbnail |
| Album | PUBLIC | optional thumbnail |
| Playlist | PUBLIC | optional thumbnail |
| Company Certification Document | PRIVATE | document |
| Notice Attachment | PRIVATE | attachment |
| Question Attachment | PRIVATE | attachment |

The report contains aggregate checked/available/missing counts and at most 100
opaque issues. An issue includes only `domain`, `storageRoot`, `recordId`, and
`referenceType`. It never serializes a storage key, original filename, file
bytes, credential, or repair instruction.

### Startup Behavior

| Runtime | Audit | Result on missing reference |
|---|---|---|
| Base/local default | enabled, non-strict | safe aggregate warning; no automatic data/file mutation |
| Explicit runtime with `APP_STORAGE_INTEGRITY_AUDIT_ON_STARTUP=false` | disabled | operator must invoke the ADMIN inspection explicitly |
| Any explicit runtime with `APP_STORAGE_INTEGRITY_AUDIT_ON_STARTUP=true` and strict mode disabled | enabled | warning; no automatic data/file mutation |
| `acceptance` | enabled and strict | startup fails before readiness |
| `prod`/`production` deployment | enabled and strict required | startup refuses a missing audit/strict setting or a missing reference |

Production profiles also require explicit absolute roots before the local
storage service initializes. These guards turn an omitted deployment storage
contract into a failed startup, rather than a server that silently points at a
new repository-relative directory.

The ADMIN endpoint is:

```text
GET /api/admin/storage-integrity
```

It is ADMIN-only and returns a report, not a repair operation.

## Recovery Boundary

The audit does not copy, relocate, delete, regenerate, or rewrite database
references. An identified mismatch requires an approved recovery decision:

1. identify the owning environment tuple and a verified source object;
2. decide whether to restore the object or re-upload/recreate it;
3. take a backup or reversible snapshot before a data/file mutation;
4. apply the approved repair to the exact target only;
5. repeat the integrity inspection and the affected user flow.

This is deliberately separate from the storage mutation journal. The journal
recovers an interrupted staged mutation; it cannot infer which external root
should own an already-committed historical reference.

## Backup and Restore Boundary

Any environment backup/recovery exercise treats the tuple as one unit:

1. take a database backup and public/private root snapshots from the same
   declared recovery point;
2. record only the environment label, backup time, and non-secret storage
   locations in the operator evidence, never credentials or object keys;
3. restore into a separate isolated database and separate public/private roots;
4. start with schema validation and the strict integrity audit enabled;
5. smoke-test the affected file flow before a release or cutover decision.

The repository does not yet provide a production backup service, retention
system, or retained-data migration tool. This section defines the minimum
operator boundary for a future approved backup/recovery rehearsal; it does not
claim production recovery is complete.

## Acceptance Readiness

The acceptance lifecycle is ready only after its strict backend startup audit
passes, in addition to its existing local/public HTTP probes. A status `ready`
therefore proves the configured acceptance DB and both configured roots agree
for all supported persisted file-reference domains. It does not prove a
production backup, restore, retained-data migration, or production deployment
process; those remain separate release gates.

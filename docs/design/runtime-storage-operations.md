---
version: 1.2
last_updated: 2026-09-05
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

## Browser-Origin Preflight

Before diagnosing missing player state as missing storage, record the exact
browser origin (scheme, host and port) and confirm that it is explicitly
allowed by the running backend's effective `cors.allowed-origins`. Record only
non-secret origin values; do not print local configuration or environment bundles.

For the existing local configuration, use `http://localhost:5173` throughout
the browser scenario. The [local configuration example](../../application-local.example.yml)
already warns against `http://127.0.0.1:5173` unless explicitly allowed. A Vite
listener bound to 127.0.0.1 does not authorize that browser Origin. The two
origins also have separate browser storage; do not copy, clear or migrate
history/tokens to make a test appear to pass.

1. Confirm the owned development runtime and an existing public Track ID.
2. Check the public read-only lookup `POST /api/tracks/batch`, JSON body
   `{"ids":[4]}`, with `Content-Type: application/json` and the **actual browser
   Origin**. ID 4 is the recorded demo fixture; use an already-confirmed public
   ID for another environment, without creating test data for this probe.
3. Require the allowed-Origin request to succeed and an untrusted Origin to
   remain rejected. Record status and a safe result summary only. A GET health
   response or a POST without Origin is insufficient to validate browser CORS.
4. On that same browser origin, play, pause, seek and reload through the actual
   UI. Record restored Track/time/paused state separately from the controlled
   request comparison. Do not infer the browser's network trace from a shell probe.

[WI-005](../../deliverables/agent/WI-20260905-ATS-005-evidence-pack.md)
records MA's 2026-09-05 comparison against the same local batch target:
127.0.0.1 Origin returned 403 `Invalid CORS request`; localhost Origin and an
omitted Origin returned 200. The actual localhost browser then restored Track 4
at 5 seconds, paused, without a player fix or CORS allowlist expansion. This
single-track test does not establish list or queue repeat behavior; subsequent
MA observations are complete in
[WI-002](../../deliverables/agent/WI-20260905-ATS-002-evidence-pack.md) and
[SR-93](../SR/SR-93.md#2026-09-05-local-verification). On 2026-09-05, MA reported
document validation PASS (665 IDs, links and index) and `git diff --check` PASS.
MA verified the owned backend 30612 and frontend 28724 stopped and their ports
released. These are dated local observations, not current service availability
or production GO; only scoped staging and commit remain MA-owned for this
local closeout.

For deployment, explicitly configure and verify the real approved origin and
callbacks. Do not introduce wildcard origins or production localhost aliases
to work around a test-harness mismatch. Origin correctness does not replace
the storage integrity, backup/restore or release gates above.

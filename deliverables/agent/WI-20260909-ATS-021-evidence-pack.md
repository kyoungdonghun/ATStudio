---
version: 1.0
last_updated: 2026-09-09
project: ATS
owner: se
category: reference
status: active
dependencies:
  - path: WI-20260909-ATS-021-handoff.md
    reason: Approved local-only private staging boundary
  - path: ../user/REQ-20260909-ATS-004.md
    reason: Approved amendment and partial overall recovery status
---

# Evidence Pack: WI-20260909-ATS-021

## Summary

Prepared and locally verified one restricted sibling folder, including a native
logical DB dump. No new aggregate archive, portable encryption or cloud upload.
Independent WI022 review remains the parent's next action.

## Scope / DoD Check

- [x] New sibling directory only; checked ancestors and source entries for reparse points, exact runtime roots, destination containment and existing-target rejection.
- [x] User/SYSTEM-only inherited FullControl applied before private writes; root inheritance disabled. File and directory ACLs checked again at finalization.
- [x] 93 original copies, 277,824,496 bytes, matched source/copy SHA-256. Source lengths, modification instants, hashes and both media file lists remained stable.
- [x] Public storage: 78 files, 276,116,083 bytes. Private storage: present, empty, preserved as a directory. No missing historical object was repaired.
- [x] Original copied ZIP: 205 entries, 205 manifest items, 3,082,533 uncompressed bytes; zero missing/hash/length mismatches. No new ZIP was generated.
- [x] Native MySQL 8.0.45 dump: exit 0, 122,628 bytes; all 43 base tables included, all InnoDB; views/routines/events/triggers each 0. Completion marker present.
- [x] Before/after table definitions and DDL counters identical; 40 DDL instruments enabled, no active DDL seen at endpoints. This observation is not a DDL prevention lock or absolute concurrency guarantee.
- [x] Two referenced key IDs across two retained rows found across both raw sources; one in local YAML, the other in the historical environment bundle. No remaining ID omission across those sources. No decryption test.
- [x] Existing Java, Node and tunnel process identities/creation times unchanged. No source setting, running service or retained record was modified by this work.
- [x] Korean private README prominently requires portable encryption before upload and forbids raw YAML/full-bundle replay.

## Reference Documents (Tier 0-2)

| Tier | Injected pointer | Application |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Consent, sustainable bounded scope, encrypted off-device boundary |
| 0 | `docs/standards/development-standards.md` | Small external helpers, scoped work and evidence |
| 0 | `docs/standards/documentation-standards.md` | English tracked metadata and two deliverable sets |
| 0 | `docs/standards/glossary.md` | Local staging versus backup, restore and approval |
| 1 | `docs/policies/security-policy.md` | No private values in Git/chat, restricted copies |
| 2 | `scripts/reconstruction/README.md` | No automatic import; retained-data quarantine |
| 2 | `docs/design/runtime-storage-operations.md` | Verified DB/public/private tuple and non-atomic boundary |
| 2 | `docs/registry/development-history-recovery-20260909.md` | Original archive and receipt preservation |
| 2 | `scripts/database/README.md` | No fresh-schema/disposable/restore actions |

Injection: explicit WI021 packet and parent-supplied current runtime observations;
assignee SE, local preparation only. No additional subagents were called.

## Evidence Pointers

Private root: `<project-parent>/ATStudio-private-handoff-20260909-105744/`.
This is a local sibling path, not a remote backup location.

| Private relative pointer | Evidence |
|---|---|
| `source-copy-inventory.json` | Original/copy mapping, per-file bytes/time/SHA-256 |
| `SHA256-inventory.json` | 111 files, 278,026,952 bytes; excludes this manifest and its final receipt to avoid self-reference |
| `verification-receipt.json` | Final local status at 2026-09-09 11:06:40 +09:00, manifest hash, ACL and preservation checks |
| `payload/database/dump-verification.json` | Native exit/options, schema coverage, DDL observation and timing |
| `payload/database/export-failure.json` | Preserved first-attempt option error; no dump was created on that attempt |
| `payload/config/current-runtime-overrides.json` | Current local profile, explicit roots and bootstrap-off CLI observation |
| `payload/config/selection-receipt.json` | Actual YAML/CLI selection, JDBC query parameters not forwarded; inherited Java environment limitation |
| `payload/config/keyring-preservation-verification.json` | Final cross-source ID presence; local-only missing count resolved by preserved historical bundle |
| `payload/original/copied-archive-verification.json` | Reopened copied ZIP, all 205 members verified |
| `process-before.json`, `process-after.json` | Same three process identities and creation times |
| `README-먼저읽기.md` | Short private operator handoff and exclusions |

Tracked changes are limited to this evidence, the WI021 user summary, the REQ004
local-staging receipt and the reconstruction guide's private-staging paragraph.
Existing parent changes to REQ004 and the WI021 handoff were preserved.

## Commands & Outputs

Executed from the private sibling folder, using existing PowerShell 7, Node 24,
the repository's installed `js-yaml`, native MySQL tools and .NET ZIP support:

```powershell
pwsh -NoProfile -File ./prepare.ps1 -Phase Copy
node --check ./export-db.cjs
node ./export-db.cjs
node ./verify-keyring.cjs
pwsh -NoProfile -File ./prepare.ps1 -Phase Finalize
```

These are execution evidence, not rerun instructions: outputs are protected
against overwrite and source access would require a new reviewed capture.
The password existed only in the scoped native child environment, not argv or
console logs. Client defaults and the usual login file were bypassed; no browser
or Codex account/session material was read or copied.

The first dump attempt rejected a mysql-only timeout option (native exit 7).
Moving that option to mysql only produced the successful dump above; prior
receipts were retained, not overwritten. The first finalization encountered
PowerShell's JSON-to-DateTime conversion; comparison was corrected to UTC
instants. Independent hash/length/time comparisons were all zero mismatches,
then finalization passed. No originals were changed to obtain PASS.

## Tests

Verification is limited to this local handoff: file hashes and stability,
ACLs, original ZIP members, native export/schema coverage, key ID presence and
process preservation. No product build/test, DB restore or new runtime was run.
The parent also independently reported 205/205 source archive member matches
and independently read the successful 43-table native dump receipt.

## Risks / Rollback

- Plaintext local staging is not encryption or off-device backup. The operator must encrypt portably, retain independent recovery access, upload and verify a downloaded copy.
- DB transaction and filesystem capture are not one atomic snapshot. Existing historical missing references (10 in the earlier audit) were preserved, not repaired or reaudited.
- The running Java process's inherited environment was not read. CLI roots/profile/selected overrides are directly observed; native DB target and local YAML selection are verified without claiming complete Spring Environment attestation.
- Both raw key sources must be retained and manually assembled into a reviewed complete keyring for restoration. ID presence alone proves neither secret correctness nor cryptographic recovery.
- Do not replay YAML or import the whole acceptance bundle: raw YAML enables test-user bootstrap while the observed current CLI disables it. No restored runtime startup or production approval is granted.
- No deletion, movement or rollback was performed. Any later removal of this private staging directory or reversal of these exact document additions requires separate authorization; preserve source assets and parent edits.

## Follow-ups

WI021 unblocks WI-20260909-ATS-022. Return the verified folder and receipts to
the parent for its independent read-only review. REQ004 remains partial until
the separately governed off-device encrypted backup and recovery gates are met.

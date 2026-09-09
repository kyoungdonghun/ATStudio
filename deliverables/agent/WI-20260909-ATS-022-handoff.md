---
version: 1.0
last_updated: 2026-09-09
project: ATS
owner: qa
category: reference
status: active
dependencies:
  - path: ../user/REQ-20260909-ATS-004.md
    reason: Approved local private staging amendment
  - path: WI-20260909-ATS-021-handoff.md
    reason: Local handoff preparation to verify
---

# WI-20260909-ATS-022 Handoff

[WI HEADER]
REQ: REQ-20260909-ATS-004
Agent: qa
Depends On: WI-20260909-ATS-021
Blocks: none

[WI SUMMARY]
Why: Independently verify the private staging folder before the user's manual encryption and cloud backup.
Scope: Read-only inspection of the new sibling folder and bounded public documentation changes. Independently compare inventory hashes/coverage, source copy hashes, ACLs, original 205 archive, native dump metadata/coverage and truthful boundaries. Do not execute preparation scripts or repeat database connections.
DoD: Exact counts/status verified, any findings reported with paths, no false full-restore/off-device/production claim. No product build or broad audit needed.
Forbidden: Read/output of secret values or dump rows; any existing data/file mutation; restore, new database, server lifecycle, cloud upload, encryption configuration, provider/mail operations; copying account sessions; Git commit/push. Private source files may be hashed as bytes, never printed.

[ACCEPTANCE CRITERIA]
- [ ] Destination is outside repository, is not a reparse path and grants only current user and SYSTEM; inspect descendants as well as root.
- [ ] SHA256-inventory.json and verification-receipt.json agree; exact file coverage except their declared self-reference exclusions; rehash every member and the 93 original-to-copy entries.
- [ ] Original ZIP remains the 205-member source with exact bytes/hashes; empty current private-root directory retained.
- [ ] atstudio.sql has successful native dump evidence, 43 InnoDB table definitions and completion marker; inspect no row values. Dump hash matches receipt. SQL was exported, never applied.
- [ ] Key ID coverage receipt combines preserved local YAML and external bundle: 2 required IDs available across 2 sources; local-only missing-one receipt is explicitly historical/limited, not silently overwritten. Do not claim decryption tested.
- [ ] README clearly says plaintext local staging, portable encryption before any cloud upload, separate decryption access, NOT AUTO-IMPORT raw configs, CLI bootstrap false overriding YAML true, DB/filesystem not atomic, historical missing references unrepaired, no actual restore/production GO.
- [ ] No new unencrypted aggregate ZIP or regenable caches; all source/product/runtime preserved. Parent supplied current four HTTP GETs=200 and same PID identities; distinguish independent inspection from parent evidence.

[INPUT POINTERS]
Tier 0: docs/standards/core-principles.md, docs/standards/development-standards.md, docs/standards/documentation-standards.md, docs/standards/glossary.md.
Tier 1: docs/policies/security-policy.md.
Tier 2: scripts/reconstruction/README.md, docs/design/runtime-storage-operations.md.
Context: REQ004 local staging amendment, WI021 evidence/user summary when finalized. Local folder `C:/Users/jm991/Desktop/project/ATStudio-private-handoff-20260909-105744/`.
Private evidence: SHA256-inventory.json, verification-receipt.json, source-copy-inventory.json, payload/database/dump-verification.json, payload/config/keyring-preservation-verification.json, original manifest/ZIP. No raw private values in output.

[OUTPUT CONTRACT]
Own only WI022 evidence pack and user summary, using create-wi-evidence-pack skill. If a private QA receipt is needed, place it outside the finalized handoff folder and record a nonsecret public summary so the frozen handoff inventory is not invalidated. Do not alter WI021-owned files. Run focused hash/ACL checks and documentation validation once. Return compact PASS/findings, counts and scope. No successor WI; full REQ004 remains partial until off-device encrypted backup/restore is separately verified.

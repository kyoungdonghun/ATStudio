---
version: 1.0
last_updated: 2026-09-09
project: ATS
owner: se
category: reference
status: active
dependencies:
  - path: REQ-20260909-ATS-004.md
    reason: Approved local private staging amendment
  - path: ../agent/WI-20260909-ATS-021-evidence-pack.md
    reason: Exact local verification scope and evidence
---

# WI-20260909-ATS-021 Summary

Local preparation is complete in sibling folder
`<project-parent>/ATStudio-private-handoff-20260909-105744/`.
Independent WI022 review is next; no commit or push was performed.

| Included / checked | Result |
|---|---|
| Raw originals | 93 copies, 277,824,496 bytes; source/copy hashes and capture stability passed |
| Media | 78 public files, 276,116,083 bytes; private root exists and is empty |
| Database export | MySQL 8.0.45, exit 0; 43/43 InnoDB tables, 122,628 bytes; completion and schema/DDL observations passed |
| Billing key material | Both referenced IDs present across local YAML and historical environment bundle; actual decryption not tested |
| Original historical ZIP | 205/205 members, 3,082,533 uncompressed bytes; zero mismatches |
| Local handoff inventory | 111 hashed files, 278,026,952 bytes, plus inventory and final receipt |
| Access / preservation | User and SYSTEM only; three existing process identities unchanged |

Read the Korean private `README-먼저읽기.md` before using the materials.
**The folder is plaintext. Apply portable encryption before uploading; preserve
decryption access separately and verify the downloaded backup.** No new ZIP,
encryption, cloud upload, real restore or production approval was performed.

Do not replay raw YAML or import the entire acceptance bundle. Both raw key
sources require deliberate selection; the observed CLI disables bootstrap
that raw YAML enables. Java's inherited environment was not read. The DB/file
capture is non-atomic; the earlier 10 historical missing references were not
repaired or reaudited. REQ004 remains partial.

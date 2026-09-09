---
version: 1.0
last_updated: 2026-09-09
project: ATS
owner: qa
category: reference
status: active
dependencies:
  - path: WI-20260909-ATS-022-handoff.md
    reason: Approved independent local packaging QA boundary
  - path: WI-20260909-ATS-021-evidence-pack.md
    reason: Preparation receipts independently checked
  - path: ../user/REQ-20260909-ATS-004.md
    reason: Local staging amendment; overall recovery remains partial
---

# Evidence Pack: WI-20260909-ATS-022

## Summary

PASS: independent, read-only local packaging QA; zero unresolved findings.
Private payload inspection completed on 2026-09-09 at 11:13 +09:00, followed
by an exact UTC timestamp comparison. This is not an off-device backup,
actual restore, cryptographic recovery test or production approval.

## Scope / DoD Check

| Independent check | Result |
|---|---|
| Frozen tree | 113 files rehashed; exactly 111 inventory entries plus the declared inventory/receipt exclusions; no missing, duplicate, unlisted, size or hash mismatches |
| Inventory agreement | 278,026,952 inventoried bytes; final receipt's inventory SHA-256 and counts match |
| Original-to-copy mapping | 93/93 current sources and payload copies match recorded SHA-256 and lengths; 277,824,496 bytes; all 93 source modification timestamps match UTC ticks |
| Destination and ACL | Sibling outside repository; six ancestor paths and all descendants have no reparse point; 134 ACL objects (113 files, 21 directories including root) grant exactly current user and SYSTEM FullControl; root inheritance disabled, descendants inherit both entries |
| Storage | Public: 78 files, 276,116,083 bytes; private directory exists and is empty |
| Historical ZIP | Original source and copied ZIP have identical bytes/hash; independently opened copied ZIP has 205 unique entries matching all 205 manifest items by length/hash, 3,082,533 uncompressed bytes |
| Native dump | 122,628 bytes; receipt hash matches actual file; MySQL 8.0.45 header, 43 distinct CREATE TABLE definitions, 43/43 InnoDB engine clauses and completion marker confirmed using string checks only |
| Export receipt | Successful native exit 0, 43/43 table coverage, completion and native version agree with file inspection; original export/DDL observations are WI021 evidence, not rerun DB queries |
| Key ID receipts | Two required IDs covered across two raw sources; zero missing across both; historical local-only missing-one receipts remain intact and explicitly limited; no secret-value inspection or decryption |
| Packaging exclusions | Only the original ZIP exists; no new aggregate archive or named regenerable cache directories in the frozen tree |
| README and scope | Plaintext/ACL distinction, portable encryption before upload, separate decryption access, no raw-config auto-import, bootstrap CLI-off/YAML-on warning, non-atomic capture, historical missing references and no actual restore/production GO are explicit |

## Reference Documents (Tier 0-2)

WI022 handoff and parent injection: QA, tiers 0-2. Tier 0 pointers are
`docs/standards/{core-principles,development-standards,documentation-standards,glossary}.md`;
Tier 1 is `docs/policies/security-policy.md`; Tier 2 is
`scripts/reconstruction/README.md` and `docs/design/runtime-storage-operations.md`.
Applied create-wi-evidence-pack and validate-docs skills; no new subagents.

## Evidence Pointers

Private root:
`C:/Users/jm991/Desktop/project/ATStudio-private-handoff-20260909-105744/`.
This folder remains frozen; no QA receipt was added inside or outside it.

Checked private pointers: root `SHA256-inventory.json`, `verification-receipt.json`,
`source-copy-inventory.json`, `README-먼저읽기.md`; `payload/original/` manifest,
ZIP and copied-archive receipt; `payload/database/atstudio.sql` and
`dump-verification.json`; `payload/config/keyring-preservation-verification.json`
and historical `keyring-coverage.json`.

Nonsecret verification anchors:

```text
Inventory SHA256: 5B22DF62B515AA80C3124010FBF95E03D683F04607491F721903D9C00680BE67
Receipt SHA256:   6142CD7BEC26032692585678FE9A7C02CAA9041B4FFC47C2C0FAEDE6CE47313B
Original ZIP:     486744B423DDDDD4DAFDB96224F5612EBCB858DF2FB128417AC4CFD5C52E4163
Dump SHA256:      C814E53BF0FCAE0E1BBE7D913A35167167B50AC5FD523E295BC2E56C985734CA
Tree fingerprint: 7CEE5195EC06B94D5FFE519EB31E4E2515E9A2A59798F7C1E03F96A8B507118A
ACL fingerprint:  C4D7529D3E6E4D47FAAA393C7594B023113A222357B42E362442D206DC33BDDD
```

Writes: this evidence, WI022 summary and only the authorized WI022-pending
status replacements in REQ004's local staging receipt and reconstruction
README, linked to this PASS. Other text and parent edits were preserved.

## Commands & Outputs

In-memory PowerShell: `Get-ChildItem -Force`, `Get-FileHash -Algorithm SHA256`,
parsed JSON coverage/length comparisons, `Get-Acl` SID/inheritance checks,
.NET `ZipFile.OpenRead` member-stream hashes and anchored SQL string checks.
No helper script was executed or created; no rows or secrets were printed.

Initial exit 1 was solely a checker error: JSON DateTime-to-string conversion
lost timestamp precision. The corrected UTC-tick comparison returned exit 0,
93/93 matching; all other checks already passed. No source/receipt changed.

Fingerprints use SHA-256 of UTF-8, LF-joined, PowerShell-sorted lines without
a trailing LF: tree lines are `relative/path|bytes|uppercaseSHA256`; ACL lines
are `root-relative-Windows-path|SDDL`, with an empty path for the root.

## Tests

- Local packaging checks: PASS after the timestamp checker correction above.
- `python .agents/skills/validate-docs/scripts/validate_docs.py`: run exactly
  once, exit 0; Tier 0, internal links, 713 traceability IDs and document
  index PASS. Subsequent edits only compacted these WI022 reports and recorded
  this result; the validator was not repeated.
- Parent-supplied evidence only: original-source ZIP member inspection,
  four live GET responses = HTTP 200, unchanged existing PID identities.
  WI022 did not repeat HTTP probes, process inspection or DB connections.
- Not run: preparation scripts, database/export/restore operations, product
  build/tests, server lifecycle, encryption, external upload or Git publication.

## Risks / Rollback

- Portable encryption, separate recovery access, off-device upload/download
  verification and actual isolated restore remain outstanding; REQ004 partial.
- Key ID presence is not decryption proof; never auto-import either raw source.
- DB/filesystem capture is non-atomic; ten earlier missing references remain
  unrepaired/not reaudited. No production GO or new runtime audit.
- No rollback performed; reversing scoped docs requires authorization while
  preserving parent edits and private assets.

## Follow-ups

No successor WI. Local packaging QA complete, full REQ004 partial.
Git publication intentionally unperformed for this local-only request.

## Related Documents

- [WI022 handoff](WI-20260909-ATS-022-handoff.md)
- [WI021 evidence](WI-20260909-ATS-021-evidence-pack.md)
- [WI022 summary](../user/WI-20260909-ATS-022-summary.md)
- [REQ004](../user/REQ-20260909-ATS-004.md)

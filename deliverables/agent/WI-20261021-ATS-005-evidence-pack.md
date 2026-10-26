---
version: 1.0
last_updated: 2026-10-21
project: ATS
owner: PG
category: reference
status: complete
---
# Evidence Pack: WI-20261021-ATS-005

## Summary
Exact-byte preservation-only allowance; no runtime execution acceptance.

## Scope / DoD
- Reviewed new supersede.py and invoke.ps1, retained host primitives, generated candidate and focused tests.
- Verified expired receipt pins, exact metadata shape, cold state, mask retirement, callback unloading, no-replace moves and durable volatile copies.
- Original WINDOW policy/BPF/ledger success and deferred MAIL remain distinct from aggregate cleanup failure.
- Existing v7 tools, JAR and SPA remain in place and are checked for drift. No usable grant remains at the live path on success.
- No blocking finding in reviewed scope. SE completion declaration remains a prerequisite to invocation.

## Reference Documents
Tier 0 retained fork context: core-principles, development-standards, documentation-standards and glossary. Tier 1 security-policy read. WI004/WI005 handoffs and create-wi-evidence-pack skill applied. MA-reported native facts 478f29 and 9ac91c are not PG native observations.

## Evidence Pointers
- Exact allowance: `build/WI071-review/pg-evening-resume-20261021-final.json`.
- Initial design: `build/WI071-review/pg-evening-resume-20261021-design.json` (superseded by final receipt).
- Candidate: `build/WI067-evening-resume-20261021/`.
- Final freeze SHA256: `b60ea47109a60599c8ac8d10d14abca69f118be816516c21844d5e0bde9999cd`.
- Final delta review: MA reports SE final. Operator, invocation and Python tests remain byte-identical to reviewed versions. README, generator and PowerShell test additions reviewed; generator execution is excluded. This final freeze supersedes the earlier e4ea9b receipt.

## Commands / Tests
`python -B -m unittest discover -s build/WI067-evening-resume-20261021 -p test_cold.py` independently passed 12 tests (final run 0.088 seconds). PowerShell AST parser reported zero errors. All manifest member hashes matched; final freeze hash remained unchanged after repeat tests.

## Risks / Recovery
Additional independent command `pwsh -NoProfile -File build/WI067-evening-resume-20261021/test_pc.ps1` passed 5 tests, zero skipped; native NOT_RUN, secrets NOT_READ. Final manifest members verified before receipt update.

Linux renameat2 and systemd behavior are mocked locally, not natively proven by this review. Native gate mismatch must HOLD. Partial moves preserve evidence but require exact state inspection; never automatically rerun or reverse moves. No extra long test campaign or full runtime re-review was performed. PG did not connect remotely, read private settings, modify candidate bytes, or commit files.

## Follow-up
MA checks SE final freeze, invokes the pinned preservation entry once, verifies native postconditions and only then prepares a fresh bounded release under existing authorization. This receipt does not itself authorize app start or mail.

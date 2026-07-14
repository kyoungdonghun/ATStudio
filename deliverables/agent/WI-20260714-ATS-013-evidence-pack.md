# Evidence Pack: WI-20260714-ATS-013

## Summary

- Neutralized formula-leading user-controlled whitelist CSV cells at serialization time without changing entities, export snapshots, dependencies, BOM, or column order.

## Scope / DoD Check

- [x] User-controlled text beginning effectively with `=`, `+`, `-`, or `@` receives one apostrophe before existing CSV quote escaping.
- [x] Leading ASCII space/tab, in-cell BOM, and cell-leading tab/CR/LF variants are covered.
- [x] Embedded quote/newline, null, empty, apostrophe-prefixed, negative-looking, and Korean values retain their original business data.
- [x] Export snapshots and source entities remain unchanged.
- [x] No dependency or unrelated whitelist workflow change was introduced.
- [x] Focused tests, Java compilation, and owned-file whitespace checks pass.

## Reference Documents (Tier 0-2)

| Tier | Document | Use |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Approval, traceability, and security baseline |
| 0 | `docs/standards/development-standards.md` | Java service and evidence conventions |
| 0 | `docs/standards/documentation-standards.md` | Deliverable structure |
| 0 | `docs/standards/glossary.md` | Canonical whitelist-channel terminology |
| 1 | `docs/policies/security-policy.md` | Untrusted-output security context |
| REQ | `deliverables/user/REQ-20260714-ATS-001.md` | Approved P1 scope and WI chain |
| Audit | `docs/audit/p1-remediation-trace-matrix-20260714.md` | `ATS020-P1-11` exit evidence |
| Design | `docs/design/p1-security-acceptance-hardening-design.md` | CSV neutralization contract, Section 7 |

## Evidence Pointers

- `src/main/java/com/atstudio/atstudio/service/AdminWhitelistChannelService.java:171-230`
  - Routes only the six user-controlled text columns through output-only neutralization before the existing quote/double-quote serializer.
- `src/test/java/com/atstudio/atstudio/service/AdminWhitelistChannelServiceTest.java:113-215`
  - Covers direct formula prefixes, whitespace/control/BOM variants, quoting, null/empty, apostrophe, Korean text, and original snapshot/entity values.
- `src/test/java/com/atstudio/atstudio/service/AdminWhitelistChannelServiceTest.java:263-280`
  - Captures persisted export items for snapshot immutability assertions.

## Exact Edge Cases

| Category | Inputs / assertion |
|---|---|
| Direct prefixes | `=2+3`, `+nickname`, `-legitimate`, `@shorts` become apostrophe-prefixed CSV cells |
| Leading bypasses | leading space, multiple spaces, tab, and in-cell BOM before a formula prefix are neutralized |
| Leading controls | cell-leading tab, CR, and LF are neutralized; tab-leading ordinary text is also covered |
| CSV quoting | embedded `"` is doubled and embedded newline remains inside the quoted cell |
| Preserved values | null, empty, already-apostrophe-prefixed, ordinary Korean, and negative-looking source values retain original entity/snapshot data |
| File contract | UTF-8 BOM and existing column order remain unchanged |

## Commands and Results

- `.\gradlew.bat test --tests "com.atstudio.atstudio.service.AdminWhitelistChannelServiceTest"`
  - PASS: 5 tests, 0 failures, 0 errors, 0 skipped.
- `.\gradlew.bat compileJava`
  - PASS: `BUILD SUCCESSFUL`.
- `git diff --check -- <owned paths>`
  - PASS: no whitespace errors in tracked owned changes.
- `git diff --no-index --check -- /dev/null <new deliverable>` for each untracked summary/Evidence Pack
  - PASS: no whitespace-error output; exit 1 is the expected new-file diff status.

## Risks / Rollback

- Risk: raw CSV consumers that do not implement spreadsheet apostrophe semantics will observe the leading apostrophe.
- Residual verification: no live Excel or Google Sheets UI smoke was run; focused tests verify emitted UTF-8 CSV bytes and snapshot immutability.
- Boundary: numeric IDs, server timestamps, plan name, and billing cycle remain on the existing serializer because they are not user-controlled cells under this WI contract.
- Rollback: revert only the owned service serializer changes, focused tests, summary, and Evidence Pack. Stored records require no migration because they were never rewritten.

## WI-Chain Triggers

- Completion immediately unblocks `WI-20260714-ATS-019`, `WI-20260714-ATS-024`, and `WI-20260714-ATS-025` for MA handoff and delegation.

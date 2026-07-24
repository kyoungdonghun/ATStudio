---
version: 1.0
last_updated: 2026-07-24
project: ATS
owner: qa
category: work-summary
status: confirmed
dependencies:
  - path: REQ-20260724-ATS-002.md
    reason: Approved release rehearsal scope
  - path: ../agent/WI-20260724-ATS-020-handoff.md
    reason: Independent QA execution contract
  - path: ../agent/WI-20260724-ATS-020-evidence-pack.md
    reason: Reproducible verification evidence
---

# WI-20260724-ATS-020 Summary

## Verdict

**PASS** - A brand-new Windows clone of the pushed corrective branch reproduced
the frontend, PDF, documentation, backend, and disposable MySQL gates without
tracked, staged, or untracked repository drift.

## Independent Scope

- Cloned only remote branch `codex/v1-release-rehearsal-fixes`.
- Verified exact commit
  `df35f9fe45146ffdeb64a3fac2730c5c24c6b644`.
- Did not reuse or modify the earlier `release-rehearsal-3147873-20260724`
  clone.
- Used the official workspace only to write this summary and the Evidence Pack.
- Did not commit, push, run runtime UI/API smoke, or invoke payment/provider
  mutations.

## Original Defect Disposition

| Original defect | Independent result |
|---|---|
| Windows checkout changed frontend files to CRLF and failed Prettier | FIXED - all 2,421 tracked EOL records were inspected; unexpected worktree CRLF count was 0 |
| PDF replay changed the manifest source hashes on Windows | FIXED - replayed raw manifest SHA-256 matched the committed SHA-256 |
| Manifest output was not guaranteed to use raw LF bytes | FIXED - CRLF count 0, lone CR count 0, and final byte LF |
| PDF font prerequisite was implicit | FIXED - focused preflight tests passed with the approved Malgun font hashes |
| No current guarded fresh-DB operator path | FIXED - refusal suite and real disposable create/validate/drop proof passed |
| Acceptance operator setup was not documented | FIXED - current database and acceptance runbooks were present and documentation validation passed |

## Verification Results

| Gate | Result |
|---|---|
| Fresh remote clone / exact commit | PASS |
| Windows checkout attributes | PASS |
| Frontend dependency install | PASS, 321 packages |
| Frontend tests | PASS, 63 files / 468 tests |
| Frontend coverage | PASS, 86.73% statements / 76.98% branches / 85.41% functions / 88.75% lines |
| Typecheck / ESLint / Prettier | PASS / PASS / PASS |
| Frontend production build | PASS, 266 modules / 133 files |
| Backend `clean check` | PASS, 1,208 tests / 0 failures / 9 conditional skips |
| Backend coverage thresholds | PASS |
| PDF provenance focused tests | PASS, 5 tests |
| Python runtime | PASS, exact Python 3.12.13 |
| PDF replay and verifier | PASS, 12 pages / 295 of 295 source segments |
| PDF SHA-256 | `d7ad6184acba26b9bea2ef5a1c3d2735d7047b06c8390bb32931a46a9e2332f4` |
| Raw LF manifest SHA-256 | `f05cace32f363b4cd97ebfce0b86d1c33094bf31103d266b3b1cd5d97cb916fb` |
| Documentation validation | PASS, 0 broken links / 0 orphaned documents |
| DB refusal suite | PASS, 9 safety groups |
| Disposable MySQL proof | PASS, create / validate / drop |
| V1 DB manifest | 39 tables / 449 columns / 153 indexes / 80 foreign keys / 6 plans |
| Product-code paths in corrective diff | 0 |
| Final clone status | PASS, tracked 0 / staged 0 / untracked 0 |

## Safety Result

- The credential bundle remained outside the repository, with inheritance
  disabled and one ACL identity.
- No credential value, JDBC URL, username, password, or exact disposable
  database name was printed.
- Protected and malformed names and non-loopback hosts were refused before
  credential or connector access.
- The proof database was dropped successfully.
- The protected `atstudio` database was not selected, mutated, or enumerated.

## Residual Risks

- The installed frontend still contains the already-reviewed React Router
  `6.30.4` moderate advisories. Current ATStudio routes were judged
  non-reachable for the reviewed exploits, but a controlled React Router
  `7.18.1` migration remains required before production.
- The WI-020 handoff path was corrected after independent review from
  `scripts/db/` to the implemented `scripts/database/` path.
- Nine backend tests remain intentionally skipped in this non-MySQL full gate:
  eight environment-conditional MySQL tests and one Windows symlink test. The
  active DB helper received its own real MySQL proof in this WI.

## Next Gate

WI-20260724-ATS-014 may proceed to isolated runtime API/UI smoke. This PASS does
not itself approve production deployment or external payment mutation.

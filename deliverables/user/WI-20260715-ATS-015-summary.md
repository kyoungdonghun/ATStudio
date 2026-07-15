# WI-20260715-ATS-015 Follow-up QA Summary

## Verdict

**FOLLOW-UP PASS**

WI-014 remains an unchanged historical **FAIL**. WI-015 applies the approved
changed-file Prettier gate, confirms that the approved remediation changed no
frontend source file, and confirms that the first WI-014 Gradle executor failure
does not reproduce under the required stacktrace rerun.

This follow-up does not declare production readiness. That decision remains open.

## New Verification Results

| Gate | Command / basis | Result |
|---|---|---|
| Baseline-to-current frontend delta | `git diff --name-status b217234..HEAD -- frontend` | PASS; exit 0; 52 ms; 0 paths |
| Approved changed frontend source set | `git diff --name-only --diff-filter=ACMRT b217234..HEAD -- frontend/src` | PASS; exit 0; 54 ms; 0 paths |
| Current frontend tracked diff | `git diff --name-only -- frontend` | PASS; exit 0; 57 ms; 0 paths |
| Current frontend cached diff | `git diff --cached --name-only -- frontend` | PASS; exit 0; 57 ms; 0 paths |
| Frontend Prettier config delta | Package manifest, lockfile, and `.prettierrc` diff | PASS; exit 0; 59 ms; 0 paths |
| Current full-tree Prettier observation | `npm.cmd run format` in current `frontend/` | Observed exit 1; 2.890 s; 143 files |
| Frozen full-tree Prettier observation | Same command at frozen `b217234` preview | Observed exit 1; 2.985 s; 199 files |
| Scoped Prettier gate | Changed frontend source set | N/A / PASS; exact set `[]` |
| Backend rerun | `.\gradlew.bat test --rerun-tasks --stacktrace` | PASS; exit 0; 90.352 s; 986 tests, 0 failed, 0 errors, 9 skipped |

The current 143-file full-tree Prettier failure set is a strict subset of the
frozen baseline's 199-file set. There are 0 current-only failures and 56
baseline-only failures. All source files are content-identical after CRLF-to-LF
normalization; the 56 baseline-only results are caused by checkout line endings.
Therefore all 143 current failures are pre-existing formatting debt and are not a
blocking gate for this scoped remediation.

The required backend rerun completed successfully. The prior `Gradle Test
Executor 13` completion failure did not recur. The nine skips are the seven
opt-in disposable-MySQL concurrency tests, one opt-in MySQL schema-validation
test, and one symbolic-link-dependent local-storage test.

## Reused WI-014 PASS Results And Reviewed Evidence

The following WI-014 PASS results and reviewed evidence were reused without
rerunning their underlying gates:

| Gate | WI-014 result |
|---|---|
| Frontend typecheck | Exit 0; 4.960 s |
| Frontend ESLint | Exit 0; 3.318 s; 0 warnings |
| Frontend tests | Exit 0; 7.409 s; 17 files, 69 tests |
| Frontend build | Exit 0; 7.887 s; 259 modules |
| Documentation validation | Final WI-014 validation: exit 0; 373 IDs; 0 warnings |
| Whitespace validation | Exit 0; 0.094 s |
| Disposable MySQL evidence | WI-007 executed and passed schema, validate, 7 races, drop, and cleanup; WI-014 reviewed that authoritative evidence |
| Public preview smoke | PASS; public root and `/api/tracks` returned HTTP 200 |
| Independent review | PASS; no P0/P1 findings in WI-012 scope |

## Scope And Preservation

No frontend, product, documentation corpus, schema, preview, or data file was
formatted or written. Live Toss was not used. The frozen preview remains clean at
`b2172346f9c8202abe56ec44b458cd0a493fa232`.

Only these WI-015 output paths were created:

- `deliverables/user/WI-20260715-ATS-015-summary.md`
- `deliverables/agent/WI-20260715-ATS-015-evidence-pack.md`

WI-014's handoff, evidence pack, summary, and the four expected runtime logs were
preserved byte-for-byte. Nothing was staged or committed.

## Residual Risks

1. The 143-file full-tree Prettier debt remains. It is outside WI-015 scope and
   should be handled by a separately approved formatting work item.
2. Nine backend tests remain skipped in the default suite. WI-007 executed and
   passed the eight MySQL-gated tests, and WI-014 reviewed that authoritative
   evidence; the symbolic-link test remains environment-dependent.
3. The original WI-014 Gradle executor completion failure is non-reproducing, not
   root-caused. A single successful stacktrace rerun lowers but does not eliminate
   recurrence risk.
4. WI-012 retains one non-blocking P3 observability gap for the unknown-cancel
   rendered-log path.
5. Public preview evidence and the authoritative WI-007 disposable-MySQL evidence
   reviewed by WI-014 are point-in-time results; they were not rerun by WI-015.
6. Production readiness remains open and requires explicit owner decision.

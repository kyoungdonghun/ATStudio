---
version: 1.0
last_updated: 2026-09-09
project: ATS
owner: qa-fe
category: evidence-pack
status: stable
related_wi: WI-20260909-ATS-007
dependencies:
  - path: WI-20260909-ATS-007-handoff.md
    reason: Assigned evidence-only scope
  - path: ../user/REQ-20260909-ATS-001.md
    reason: Approved bounded remediation and MA-serialized validation
  - path: WI-20260909-ATS-010-evidence-pack.md
    reason: Independent F1 and R1 review disposition
---

# Evidence Pack: WI-20260909-ATS-007

## Summary

Post-R1 frontend regression and unchanged coverage gates PASS: MA's completed Vitest run reports 112/112 files and 1,563/1,563 tests passed in 79.28s, with no reported failures or skips. QA reconciled the coverage artifact and all 334 manifested inputs with both the shared source and isolated tested copy. This is artifact review of MA executions, not a QA rerun or browser/production acceptance.

## Scope / DoD Check

- [x] Assigned handoff read first; approved REQ and owned two-set outputs verified.
- [x] Final MA completion received before acceptance; expected counts were not substituted for actual results.
- [x] Original-source R1 RED, corrected focused GREEN and final full run distinguished from earlier incomplete/pre-F1/pre-R1 checkpoints.
- [x] Exact coverage counts, exclusions, thresholds, warnings and skip meanings reported.
- [x] Only this report and `deliverables/user/WI-20260909-ATS-007-summary.md` created for WI007 with `apply_patch`.
- [x] No product/test/config changes, heavy runner, subdelegation, Git writes or WI006 edits.
- [x] Evidence returned to MA for WI013/WI014, without closing the parent REQ.

## Reference Documents (Tier 0-2)

| Tier | Document | Use |
|---|---|---|
| Entry | `AGENTS.md` | Owned scope, Korean conversation and English two-set reports |
| 0 | `docs/standards/core-principles.md`; `development-standards.md`; `documentation-standards.md`; `glossary.md` in the same directory | Approved execution, unchanged gates, metadata, preservation and terminology; loaded in this QA task |
| 1 | `docs/policies/security-policy.md`; `docs/policies/quality-gates.md` | No sensitive/runtime effects; exact verification boundaries |
| 2 | `deliverables/user/REQ-20260909-ATS-001.md`; assigned WI007 handoff | Scope and chain |
| 2 | `deliverables/agent/WI-20260908-ATS-018-findings.md`; `WI-20260909-ATS-004-evidence-pack.md`; `WI-20260909-ATS-005-evidence-pack.md`; `WI-20260909-ATS-010-evidence-pack.md` | SEC-04, dependency installation boundary, F1/R1 history and independent closure |
| 2 | `docs/standards/frontend-standards.md`; `frontend/vite.config.ts`; `frontend/package.json` | Relevant frontend contracts and actual test/coverage configuration |
| Skills | `.agents/skills/create-wi-evidence-pack/SKILL.md`; `test/SKILL.md`; `test-coverage/SKILL.md`; `react-best-practices/SKILL.md` under `.agents/skills/` | Required report structure, count/coverage reporting and scoped frontend context |

Injection source: `.claude/config/context-injection-rules.json`; assignee `qa-fe`, task type `testing`, required tiers `[0]`, plus explicitly assigned security/quality policies. `.claude/config/workspace.json` confirms `ATS`. No subagent was invoked. Existing skill/report patterns were reused.

## Evidence Pointers

Repository: `C:/Users/jm991/Desktop/project/ATStudio`, `main` at `8161f0a` plus approved uncommitted remediation. Unless otherwise qualified, artifact names below are under `output/release-remediation-20260909/`.

| Artifact | Evidence |
|---|---|
| `frontend-r1-full-final-coverage.log:11` | 112 passed files, 1,563 passed tests, start 05:01:28 KST, duration 79.28s |
| `frontend-r1-full-final-coverage.log:20` | Final aggregate coverage table |
| `frontend-coverage-final.json` | 135 source rows and aggregate covered/total/skipped counters |
| `frontend-final-snapshot.json` | 334 exact source paths/hashes; recorded `2026-09-09T05:01:02.4007414+09:00` |
| `frontend-r1-red-snapshot.json`; `frontend-r1-red.log` | Original store hash and five selected failing R1 cases |
| `frontend-r1-green-focused.log` | Corrected five-file/163-test selection |
| `tested-source-final-check.json` | MA final check: 620 backend and 334 frontend entries, both with empty issue lists; backend not re-audited by this assignment |
| `command-exit-status.json`, `frontendCoverage` | MA's persisted tool-result transcription: `npm run test:coverage`, exit 0, 112 files, 1,563 tests |
| `frontend/src/store/authStore.ts:71` | R1 expected successor captured before internal clear; guard at line 75 prevents stale persistence |
| `frontend/src/store/authStore.test.ts:468`; `frontend/src/pages/auth/SocialLoginPage.test.tsx:445` | Two storage-failure cases plus three intermediate-notification cases |

### Exact Tested Snapshot

Isolated root: `C:/Users/jm991/AppData/Local/ATStudio/validation/release-remediation-20260909-frontend-5c1364c2`. Manifest entries start with `frontend/`; remove that prefix when resolving them inside the isolated root. QA verified all 334 hashes against both roots, with zero missing/mismatched inputs. The manifest includes 112 test files and all four restored `src/test/coverage/*.coverage.test.tsx` files:

- `adminSubscriberGaps.coverage.test.tsx`
- `adminSubscriberPages.coverage.test.tsx`
- `publicAuthShell.coverage.test.tsx`
- `shellCatalogRouterGaps.coverage.test.tsx`

MA reports fresh `npm ci` installation of the corrected lock in this isolated tree; the installation log is a separate earlier dependency checkpoint, not evidence that npm was reinstalled for every source refresh. The final R1 source snapshot is dated above. No `.env*` file exists at the isolated root; no environment-file contents, account or browser session were read. Coverage JSON from the isolated root's `coverage/coverage-summary.json` is byte-identical to the retained final JSON. No manifest claim covers the entire live `node_modules` tree or production runtime.

| Snapshot / artifact | SHA-256 |
|---|---|
| `frontend-final-snapshot.json` | `F182B3E3921F692BED260166C08BAE036BBD5792D47E1E860AA0D3490353C530` |
| R1 original `frontend/src/store/authStore.ts`, recorded in RED snapshot | `D2784A7F7C2481D7E5E34113E55F085D7CB3FCF0EE7C12946C60692AD10E2760` |
| Corrected current `frontend/src/store/authStore.ts`, matches final manifest | `FF1AC52AC54EBD20DB44B479E7E1185F91FDBFF22E740D1C30A84CE49274FA1E` |
| `frontend-r1-red.log` | `BEB4124090E7DBC31A97BDDC1C138DF4CC0F123655F84ABEA0C6AE3610134D50` |
| `frontend-r1-green-focused.log` | `7A96459F7C4697C4BB85E8B5082886E5F274A367A8DB0D69780A86B66DA65642` |
| `frontend-r1-full-final-coverage.log` | `0120E0D4156E342A9E12A95131F0105DEC60E11CC5E1F38FE13594E1AD79359D` |
| `frontend-coverage-final.json` | `55AB39BBEC25BB9C3DE1056325981D6517D761C0B8D38BA2673908228E87B410` |

## Commands & Outputs

MA executed these package scripts in the isolated root; QA read their completed logs only:

```powershell
npm run test -- src/store/authStore.test.ts src/pages/auth/SocialLoginPage.test.tsx -t WI010-F1-R1
npm run test -- src/api/client.test.ts src/store/authStore.test.ts src/pages/auth/LoginPage.test.tsx src/pages/auth/SocialLoginPage.test.tsx src/test/coverage/publicAuthShell.coverage.test.tsx
npm run test:coverage
```

The first command belongs only to MA's original-store RED snapshot, not current source. Logs expose the corresponding `vitest run` commands. The full final log contains `vitest run --coverage`, without the earlier `--maxWorkers=2` suffix. MA explicitly confirmed final gate completion/PASS; process exit attribution belongs to MA, because the text log itself does not encode a shell exit field.

MA subsequently persisted the actual tool-result exits in `command-exit-status.json`: `frontendCoverage.exitCode = 0`. This is explicitly a **MA transcription of tool results**, not an exit inferred from quiet log text, a raw shell transcript or an independent QA execution. Its command, log name and 112/1,563 counts agree with the reviewed final log. The same artifact records the four final frontend quality gates and npm audit at exit 0.

QA executed scoped document/log reads, `Get-FileHash`, structured JSON analysis and read-only configuration comparison. Summing the 135 source rows exactly reproduces all four aggregate total/covered counters. An initial package-lock parse rejected npm's empty root-property name; the corrected `ConvertFrom-Json -AsHashtable` parse succeeded. No failed/null lookup was treated as dependency mismatch. Conventional config filenames absent in this repository were resolved to the actual `.eslintrc.cjs` and single `tsconfig.json`; these read-command corrections are not gate failures.

Read-only `git --no-optional-locks diff 8161f0a -- frontend/vite.config.ts frontend/tsconfig.json frontend/package.json frontend/.eslintrc.cjs frontend/.prettierrc frontend/.gitignore docs/standards/frontend-standards.md docs/policies/quality-gates.md` produced no diff at the gate review checkpoint. No Git mutation occurred.

## Tests And Coverage

### Final Results

| Metric | Actual final value |
|---|---:|
| Vitest version | 4.1.4 |
| Test files | 112 passed / 112 total |
| Tests | 1,563 passed / 1,563 total |
| Failed / skipped tests | 0 / 0 reported by the final summary |
| Duration | 79.28s |
| Warning / stderr / unhandled-error diagnostics | None reported in the retained final coverage log |

### Coverage Counters

Percentages are the reporter's two-decimal values; counts are exact. Coverage-counter `skipped: 0` is distinct from test selection/skipping.

| Metric | Covered | Total | Uncovered | Reporter coverage | Unchanged gate |
|---|---:|---:|---:|---:|---|
| Statements | 10,934 | 12,113 | 1,179 | 90.26% | >=80% PASS |
| Lines | 10,049 | 10,823 | 774 | 92.84% | >=80% PASS |
| Functions | 2,420 | 2,653 | 233 | 91.21% | >=80% PASS |
| Branches | 7,247 | 8,749 | 1,502 | 82.83% | >=70% PASS |

`frontend/vite.config.ts:146-157` retains V8 coverage, `include: ['src/**/*.{ts,tsx}']`, and exclusions `src/**/*.d.ts`, `src/**/*.test.{ts,tsx}`, `src/test/**`, `src/main.tsx`. These exclude coverage instrumentation, not execution of source test files. The earlier overbroad copy omission was a snapshot defect, not an approved test/coverage exclusion. Thresholds remain aggregate; no per-file 100% gate was added or removed.

| Changed surface | Statements | Lines | Functions | Branches |
|---|---:|---:|---:|---:|
| `frontend/src/api/client.ts` | 99.02% (102/103) | 100% (94/94) | 100% (19/19) | 96.42% (81/84) |
| `frontend/src/store/authStore.ts` | 97.89% (93/95) | 97.75% (87/89) | 100% (17/17) | 94.33% (50/53) |
| `frontend/src/pages/auth/LoginPage.tsx` | 74.61% (97/130) | 74.56% (85/114) | 77.27% (17/22) | 70.21% (66/94) |
| `frontend/src/pages/auth/SocialLoginPage.tsx` | 93.25% (83/89) | 93.5% (72/77) | 91.66% (11/12) | 89.58% (43/48) |

The configured aggregate gates pass; this is not 100% coverage of every authentication path. `LoginPage` has 29 uncovered lines and five uncovered functions. The report's below-50% line entries are `src/App.tsx` (0/1 line, 0/1 function) and `src/types/index.ts` (0 executable counters, reporter 0%). These are disclosed coverage limits, not newly assigned implementation work or grounds to lower gates. Function identities cannot be recovered from the retained summary JSON alone.

### Preserved Checkpoint History

| Checkpoint / artifact | Observed result | Meaning |
|---|---|---|
| `frontend-focused-initial.log` | 2 files, 63 passed, 3.53s | Initial installed-dependency focus; not final source/coverage |
| `frontend-incomplete-snapshot-coverage.log` | 108 files, 1,399 passed, 202.12s | Incomplete snapshot: four source coverage-test files omitted. Never a full PASS claim |
| `frontend-full-coverage.log` | 112 files, 1,516 passed, 198.38s | Corrected complete snapshot before F1 |
| `frontend-entrypoint-focused.log` | 5 files, 156 passed / 2 failed, 8.80s | New owned-storage-failure copy expectations were wrong; preserve failed checkpoint |
| `frontend-entrypoint-focused-final.log` | 5 files, 158 passed, 9.01s | Corrected expectations and retained product behavior, before R1 |
| `frontend-full-final-coverage.log` | 112 files, 1,558 passed, 78.36s | Pre-R1 checkpoint despite its filename; not current final evidence |
| `frontend-r1-red.log` | 2 files; 5 selected failures, 57 deselected by `-t`; 3.57s | Executed counterexample against the recorded old store hash |
| `frontend-r1-green-focused.log` | 5 files, 163 passed, 8.43s | Corrected R1, including retained normal/failure compatibility |
| `frontend-r1-full-final-coverage.log` | 112 files, 1,563 passed, 79.28s | Actual current final suite |

The restored test files add 117 cases (1,399 to 1,516), F1 adds 42 (1,516 to 1,558), and R1 adds five (1,558 to 1,563). These differences describe the observed checkpoints, not an inferred substitute for final execution. RED's 57 entries are name-filter deselections, **not environment skips**, failures or permanent exclusions. Focused/RED counts are not added to the full-run total.

R1's two store tests prevent stale access/refresh writes and stale failure cleanup after an internal-clear subscriber logs in a newer session. Its three StrictMode social-entrypoint tests preserve replacement login, same-user re-login and logout through that exact notification. Existing current-owner staging failure, final-notification reentrancy and normal complete/incomplete StrictMode tests remain. WI010's updated independent review explicitly closes F1 including R1, with a 05:05 KST final-gate addendum; QA does not substitute its aggregate report for that review.

## Risks / Rollback

- Actual validation is isolated Vitest/jsdom with real local store logic and fake APIs. No real account, OAuth provider, public browser, mobile, SMTP/payment/refund, DB/DDL, media or production/runtime action was taken by this QA package.
- In-memory generation protection is not evidence for cross-tab synchronization, cancellation of already-dispatched server writes or deployed access-token revocation. No product policy was changed by QA.
- No new backend investigation or WI006 edit occurred. Existing backend environment/compile limits remain in WI006. No secret/environment value or historical application log was inspected.
- Rollback concerns only the two WI007 reports: use scoped correction patches; deletion requires approval. Preserve shared code, SR-93, untracked artifacts, tests and historical evidence. Do not reset the workspace or remove failing history.
- Repository-wide documentation validation is outside WI007. MA's reported preclosure 702-ID PASS predates WI013 edits and these reports; it is not post-edit document validation.

## Follow-ups / Chain

Report-only verification of all six WI007/WI008/WI009 files found zero missing required metadata fields, broken Markdown links, missing dependency targets, trailing-whitespace lines or UTF-8 replacement characters. A local path-list construction error in the first check was corrected before the successful six-file check; it changed no files. This is not the full repository documentation validator.

Ready test list supplied to MA: final coverage, typecheck, lint, format and build, all now completed by MA; **no additional heavy tests requested**. WI007 is complete for its evidence-only scope. Return this packet with WI008/WI009 to WI013 for documentation and WI014 for integration. Independent F1/R1 review is closed in WI010; parent REQ, deployment and remaining documentation closure are MA-owned. No subdelegation or new audit is requested.

## Related Documents

- [WI007 handoff](WI-20260909-ATS-007-handoff.md)
- [WI007 user summary](../user/WI-20260909-ATS-007-summary.md)
- [Independent authentication review](WI-20260909-ATS-010-evidence-pack.md)
- [WI008 type evidence](WI-20260909-ATS-008-evidence-pack.md)
- [WI009 quality evidence](WI-20260909-ATS-009-evidence-pack.md)

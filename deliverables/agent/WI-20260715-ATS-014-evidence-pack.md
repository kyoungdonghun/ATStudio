# Evidence Pack: WI-20260715-ATS-014

## Summary

- **FAIL:** all required gates were executed, but the frontend Prettier gate failed on 143 files. The backend clean build passed on retry after an initial executor-level failure.

## Scope / DoD Check

- [x] Read the WI-014 handoff and every Tier, policy, REQ/context, evidence, and verification-skill pointer before execution.
- [x] Ran the complete backend clean build and recorded both the initial failure and one identical retry.
- [x] Ran frontend typecheck, lint, Prettier check, Vitest, and production build.
- [x] Ran documentation validation and Git whitespace checks.
- [x] Reviewed authoritative WI-007 MySQL evidence and WI-012 independent closure.
- [x] Verified the frozen preview branch/commit and performed read-only public SPA/API GET smoke checks.
- [x] Audited tracked, cached, untracked, generated-metadata, runtime-log, and preview-worktree state.
- [x] Created only the two authorized WI-014 output documents.
- [ ] PASS is not available because `npm run format` exited `1` with 143 files requiring formatting.

## Reference Documents

| Tier | Document | Verification use |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Approval, transparency, financial traceability, and execution boundaries |
| 0 | `docs/standards/development-standards.md` | Backend/frontend build, test, warning, and evidence requirements |
| 0 | `docs/standards/documentation-standards.md` | Output language, structure, links, and reproducibility |
| 0 | `docs/standards/glossary.md` | Canonical ATStudio and WI terminology |
| 1 | `docs/policies/quality-gates.md` | Final quality-gate and Evidence Pack criteria |
| 1 | `docs/policies/security-policy.md` | Secret, database, provider, log, and public-boundary constraints |
| REQ | `deliverables/user/REQ-20260714-ATS-001.md` | Approved scope and prohibited production/live-provider actions |
| Context | `docs/audit/p1-payment-integrity-closure-20260715.md` | Current payment-integrity closure and open boundaries |
| Context | `docs/design/p1-payment-integrity-remediation-design.md` | F-01 through F-05 invariants and MySQL proof contract |
| Context | `docs/SR/SR-93.md` | Production-readiness boundary and remaining gates |
| Context | `docs/payment/acceptance-test-checklist.md` | Client-observable versus implementation-only checks |
| Evidence | `deliverables/agent/WI-20260715-ATS-007-evidence-pack.md` | Authoritative disposable MySQL 7/7 proof |
| Evidence | `deliverables/agent/WI-20260715-ATS-012-evidence-pack.md` | Independent follow-up review PASS and P3 gap |
| Evidence | `deliverables/agent/WI-20260715-ATS-013-evidence-pack.md` | Documentation alignment and final-QA handoff |
| Handoff | `deliverables/agent/WI-20260715-ATS-014-handoff.md` | Scope, acceptance criteria, constraints, and output contract |

Verification skill pointers read and applied:

- `.agents/skills/build-check/SKILL.md`
- `.agents/skills/test/SKILL.md`
- `.agents/skills/typecheck/SKILL.md`
- `.agents/skills/eslint/SKILL.md`
- `.agents/skills/prettier/SKILL.md`
- `.agents/skills/validate-docs/SKILL.md`
- `.agents/skills/create-wi-evidence-pack/SKILL.md`

Injection order: Tier 0 -> Tier 1 -> REQ/context -> evidence and handoff snapshots. Assignee: `qa`. Task type: final read-only quality verification.

## Baseline and Scope Audit

Initial branch and HEAD:

- Branch: `codex/p1-acceptance-hardening`
- HEAD after WI-013 documentation alignment: `08e081fd8a0944b0e30ebe03d0163ca57bf9b70a`
- Initial tracked diff: none.
- Initial cached diff: none.
- Initial untracked paths: the four runtime logs and `deliverables/agent/WI-20260715-ATS-014-handoff.md`.

Pre-existing runtime logs preserved:

| Path | Final bytes | Final SHA-256 |
|---|---:|---|
| `cloudflared.err.log` | 3953 | `A68249173CE7757C2F35764150D7B65B01830EE1DA1A8417532E57AA0289A7C1` |
| `cloudflared.out.log` | 0 | `E3B0C44298FC1C149AFBF4C8996FB92427AE41E4649B934CA495991B7852B855` |
| `frontend/vite.err.log` | 0 | `E3B0C44298FC1C149AFBF4C8996FB92427AE41E4649B934CA495991B7852B855` |
| `frontend/vite.out.log` | 296 | `E5EAB8DD1F7E6EDBE624E0B30F3B1A7C055A5B8B41DF31D8C75A679C444FE96A` |

## Commands and Results

### Backend clean build

Required command, first run:

```powershell
.\gradlew.bat clean build
```

- Exit: `1`.
- Wall time: `80.705s`.
- Result: Gradle reported `Could not complete execution for Gradle Test Executor 13` after the test process encountered an unexpected problem.
- Partial generated report: 559 tests, 0 failures, 1 skipped, report duration 65.58s.
- This was an executor-level failure, not a reported JUnit assertion failure.

Identical bounded retry:

```powershell
.\gradlew.bat clean build
```

- Exit: `0`.
- Wall time: `94.588s`; Gradle reported `BUILD SUCCESSFUL in 1m 34s`.
- Final report: 138 XML suites, 986 tests, 0 failures, 0 errors, 9 skipped; aggregate XML time 76.671s and HTML report duration 80.07s.
- Warnings: Java unchecked/unsafe operations and the JVM class-data-sharing warning.

Skipped tests in the successful run:

- 7 `PaymentMysqlConcurrencyIntegrationTest` races: skipped because the ordinary build does not activate the separately authorized disposable MySQL environment.
- 1 `PaymentMysqlSchemaValidationTest`: same disposable-MySQL boundary.
- 1 `LocalStorageServiceTest.rejectsDirectoriesAndSymbolicLinks()`: `Symbolic links are unavailable in this environment`.

### Frontend

Commands were run from `frontend/`.

| Command | Exit | Wall time | Result |
|---|---:|---:|---|
| `npm run typecheck` | 0 | 4.960s | PASS |
| `npm run lint` | 0 | 3.318s | PASS; `--max-warnings 0` |
| `npm run format` | 1 | 3.644s | **FAIL**; Prettier listed 143 files |
| `npm run test` | 0 | 7.409s | PASS; 17 files, 69 tests, Vitest 6.33s |
| `npm run build` | 0 | 7.887s | PASS; 259 modules, Vite 2.30s |

`npm run build` changed the tracked generated file `frontend/tsconfig.tsbuildinfo`. It was confirmed clean before execution: working blob and HEAD blob were both `3c8b761d34328de7e52933adbfa3944603c94a32`. The generated change was restored with:

```powershell
git restore --worktree -- frontend/tsconfig.tsbuildinfo
```

- Exit: `0`.
- Post-restore working and HEAD blob: `3c8b761d34328de7e52933adbfa3944603c94a32`.

### Documentation and Git

```powershell
python .agents/skills/validate-docs/scripts/validate_docs.py
git diff --check
```

- Documentation validation: exit `0`, 1.906s, all Tier 0 files present, no broken links, 373 supported traceability IDs, all documents indexed, no warnings.
- `git diff --check`: exit `0`, 0.070s.
- No cached changes were created.

After both WI-014 outputs were created, the final checks were repeated:

- `python .agents/skills/validate-docs/scripts/validate_docs.py`: exit `0`, 1.919s, no warnings.
- `git diff --check`: exit `0`, 0.083s.
- `git diff --no-index --check -- NUL deliverables/user/WI-20260715-ATS-014-summary.md`: raw exit `1`, the expected content-difference result, with no whitespace diagnostics.
- `git diff --no-index --check -- NUL deliverables/agent/WI-20260715-ATS-014-evidence-pack.md`: raw exit `1`, the expected content-difference result, with no whitespace diagnostics.
- Git emitted LF-to-CRLF working-copy notices for the two new files; these are line-ending notices, not whitespace errors.

### MySQL evidence review

Read-only review commands included:

```powershell
Get-Content deliverables/agent/WI-20260715-ATS-007/run-summary.log
Get-Content deliverables/agent/WI-20260715-ATS-007/hibernate-validate.log
Get-Content deliverables/agent/WI-20260715-ATS-007/mysql-races.log
Get-Content deliverables/agent/WI-20260715-ATS-007/database-drop.log
Get-Content deliverables/agent/WI-20260715-ATS-007/database-absent.log
Get-FileHash -Algorithm SHA256 <each-authoritative-log>
```

Reviewed authoritative result:

```text
schemaCreate=PASS
hibernateValidate=PASS
mysqlRaces=PASS
diagnostics=NOT_REQUIRED
drop=PASS
cleanupDatabaseExists=0
result=PASS
```

- WI-007 Evidence Pack: runner exit `0`, 97.6s; generated race suite 7 tests, 0 failures/errors/skips, 17.051s.
- `hibernate-validate.log` and `mysql-races.log` each end with `BUILD SUCCESSFUL`.
- `database-drop.log` records `drop.database=OK` and cleanup `0`; independent `database-absent.log` also records cleanup `0`.
- No JDBC URL, credential, generated database name, or secret was printed.
- The MySQL suite was not rerun in WI-014 because the handoff requires authoritative evidence review and forbids database mutation.

Commit ancestry review:

```powershell
git merge-base --is-ancestor 830c8dd HEAD
git merge-base --is-ancestor 46edd88 HEAD
git merge-base --is-ancestor 14053e6 HEAD
```

- All three commands exited `0`; the MySQL proof, follow-up correction, and independent closure are ancestors of the verified HEAD.
- WI-012 remains PASS with no P0/P1 in its four-finding scope and one non-blocking P3 rendered-log test-appender gap.

### Frozen preview and public smoke

Preview worktree read-only checks:

```powershell
git -C C:\Users\jm991\Desktop\project\ATStudio-acceptance-preview status --short
git -C C:\Users\jm991\Desktop\project\ATStudio-acceptance-preview branch --show-current
git -C C:\Users\jm991\Desktop\project\ATStudio-acceptance-preview rev-parse HEAD
```

- Status: clean.
- Branch: `codex/acceptance-preview`.
- Commit: `b2172346f9c8202abe56ec44b458cd0a493fa232` (`b217234`).

Read-only public GET smoke:

```powershell
curl.exe --silent --show-error --location --max-time 30 --output NUL --write-out ... https://sara-edit-seeker-receiving.trycloudflare.com/
curl.exe --silent --show-error --location --max-time 30 --output NUL --write-out ... https://sara-edit-seeker-receiving.trycloudflare.com/api/tracks
```

| URL | Curl exit | HTTP | Type | Transfer time |
|---|---:|---:|---|---:|
| `/` | 0 | 200 | `text/html` | 0.870s |
| `/api/tracks` | 0 | 200 | `application/json` | 0.371s |

Additional in-memory response parsing confirmed HTML content at `/` and a valid `dataList,message,pageInfo` API object with one public item. No response body was written to disk and no mutation endpoint was called.

## Changed Paths and Final State

QA-created paths:

- `deliverables/user/WI-20260715-ATS-014-summary.md`
- `deliverables/agent/WI-20260715-ATS-014-evidence-pack.md`

Pre-existing untracked paths preserved:

- `deliverables/agent/WI-20260715-ATS-014-handoff.md`
- `cloudflared.err.log`
- `cloudflared.out.log`
- `frontend/vite.err.log`
- `frontend/vite.out.log`

No product, existing documentation, test, schema, SQL, preview, provider, server, tunnel, or data path was modified. Build outputs remained ignored. No file was staged or committed.

## Verdict and Residual Risks

- **Verdict: FAIL.** WI-014 requires every executable gate to pass; `npm run format` failed on 143 files.
- No new P0/P1 payment-scope defect surfaced in the executed and reviewed scope.
- The first backend executor-level failure is a reliability risk despite the successful identical retry.
- Eight MySQL tests are intentionally skipped by the ordinary build; their engine proof is supplied by the reviewed WI-007 authorized run, not by this build.
- One symlink test is environment-skipped on this Windows host.
- Retained-database migration, live Toss, real money, production SMTP, production deployment/configuration, scheduler ownership, monitoring, and client acceptance remain unverified.
- Quick Tunnel availability is temporary and depends on local processes and the host PC.
- WI-012's P3 rendered unknown-cancel log-appender test gap remains open.
- Refund same-key recovery remains conditional on provider idempotency retention; otherwise lookup-only/Incident handling is required.

## Rollback

- Remove only the two WI-014 output documents if this QA report must be rolled back.
- Preserve the WI-014 handoff, four runtime logs, all historical evidence, payment/Incident/audit data, schema, preview worktree, and unrelated concurrent changes.
- No product, database, provider, server, tunnel, preview, or Git-history rollback is required.

## Follow-up

- Route the 143-file Prettier remediation through an approved implementation WI, then rerun WI-014 from a clean tracked tree.
- Treat recurrence of the Gradle test-executor completion failure as a release-blocking test-infrastructure defect and capture `--stacktrace` evidence in the follow-up WI.

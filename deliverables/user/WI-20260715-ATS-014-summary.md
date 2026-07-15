# WI-20260715-ATS-014 Final QA Summary

## Verdict

- **FAIL**
- Blocking gate: `npm run format` exited `1` after reporting formatting differences in 143 frontend files.
- No new P0/P1 payment-integrity finding surfaced in the required build, test, evidence, and smoke scope.
- The backend clean build passed on the required retry, but the first identical run ended with an unexpected Gradle test-executor failure. This remains a reliability concern even though no JUnit assertion failed in that run.

## Gate Results

| Area | Command or evidence | Result |
|---|---|---|
| Backend | `.\gradlew.bat clean build` | First run FAIL: exit `1`, 80.705s, unexpected `Gradle Test Executor 13` completion failure. Retry PASS: exit `0`, 94.588s. Final report: 986 tests, 0 failures, 0 errors, 9 skipped. |
| Frontend typecheck | `npm run typecheck` | PASS: exit `0`, 4.960s. |
| Frontend lint | `npm run lint` | PASS: exit `0`, 3.318s, zero warnings allowed by `--max-warnings 0`. |
| Frontend format | `npm run format` | **FAIL**: exit `1`, 3.644s, 143 files reported by Prettier. |
| Frontend tests | `npm run test` | PASS: exit `0`, 7.409s; 17 files and 69 tests passed. Vitest duration 6.33s. |
| Frontend build | `npm run build` | PASS: exit `0`, 7.887s; 259 modules transformed, Vite build 2.30s. |
| Documentation | `python .agents/skills/validate-docs/scripts/validate_docs.py` | PASS: exit `0`, 1.906s, no warnings before WI output creation. |
| Git whitespace | `git diff --check` | PASS: exit `0`, 0.070s before WI output creation. |
| MySQL evidence | WI-007 final summary and logs | PASS evidence retained: schema create, Hibernate validate, seven races, drop, and cleanup count `0`. The authoritative race suite records 7 tests, 0 failures/errors/skips, 17.051s. |
| Follow-up review | WI-012 Evidence Pack | PASS retained: no P0/P1 in the four reviewed findings; one non-blocking P3 log-appender gap remains. |
| Preview worktree | `git -C C:\Users\jm991\Desktop\project\ATStudio-acceptance-preview ...` | PASS: clean `codex/acceptance-preview` at `b2172346f9c8202abe56ec44b458cd0a493fa232`. |
| Public SPA | `curl.exe ... https://sara-edit-seeker-receiving.trycloudflare.com/` | PASS: exit `0`, HTTP 200, `text/html`, 0.870s transfer. |
| Public API | `curl.exe ... https://sara-edit-seeker-receiving.trycloudflare.com/api/tracks` | PASS: exit `0`, HTTP 200, `application/json`, 0.371s transfer; response shape parsed successfully. |

Final output validation also passed: the documentation validator exited `0` in 1.919s, `git diff --check` exited `0` in 0.083s, and both untracked WI documents had no whitespace errors under `git diff --no-index --check`. Git emitted only LF-to-CRLF working-copy notices for the new documents.

## Backend Skips and Warnings

- Seven skipped tests are the WI-007 disposable MySQL race suite, and one is its Hibernate schema-validation test. They require the separately authorized disposable MySQL runner and are covered by the reviewed authoritative WI-007 evidence.
- One `LocalStorageServiceTest.rejectsDirectoriesAndSymbolicLinks()` test was skipped because symbolic links are unavailable in this environment.
- Gradle reported unchecked/unsafe Java operations and the JVM class-data-sharing warning. Neither warning failed the successful retry.

## Changed Paths

Only these WI-014 output documents were created by QA:

- `deliverables/user/WI-20260715-ATS-014-summary.md`
- `deliverables/agent/WI-20260715-ATS-014-evidence-pack.md`

The tracked `frontend/tsconfig.tsbuildinfo` changed during `npm run build` and was restored to its verified pre-run Git blob. No product, existing documentation, schema, preview, or data path was changed. Nothing was staged or committed. The four pre-existing Cloudflare/Vite logs were preserved.

## Residual Risks

- The frontend formatting gate is currently red across 143 files and blocks WI-014 PASS.
- The first backend clean build's executor-level failure did not recur, but a single successful retry does not prove the suite is free from shutdown or resource flakiness.
- Retained-database inventory and copied-database migration rehearsal remain unverified; WI-007 proves only a fresh disposable MySQL 8/InnoDB schema.
- Live Toss, real-money movement, production SMTP, production deployment/configuration, scheduler ownership, monitoring, and client acceptance were intentionally not exercised.
- The Quick Tunnel URL is temporary and depends on the local Vite/backend/cloudflared processes and the host PC remaining available.
- WI-012's P3 rendered unknown-cancel log-appender assertion gap remains open.
- Refund same-key recovery remains bounded by the verified provider idempotency-retention contract; otherwise it must remain lookup-only and Incident-backed.

## Next Required Action

Correct the approved frontend Prettier scope in a separate implementation WI, then rerun WI-014 from a clean tracked tree. The backend executor flake should also be observed in that rerun before release approval.

---
version: 1.0
last_updated: 2026-07-15
project: ATS
owner: qa
category: agent
status: stable
dependencies:
  - path: WI-20260714-ATS-039-handoff.md
    reason: Approved independent read-only QA scope
  - path: WI-20260714-ATS-037-evidence-pack.md
    reason: Question attachment and bootstrap-log remediation evidence
  - path: WI-20260714-ATS-038-evidence-pack.md
    reason: Acceptance subscription-plan bootstrap evidence
---

# Evidence Pack: WI-20260714-ATS-039

## Summary (one-liner)

- Independently passed the bounded backend and frontend preview-safe quality gate with 72 backend and 23 frontend tests, while preserving the shared dirty worktree.

## Scope / DoD Check

- [x] Backend `compileJava` passed.
- [x] Seven required backend test classes ran in separate serial Gradle invocations and passed.
- [x] Frontend typecheck passed.
- [x] Five focused auth/logout/social/proxy test files passed.
- [x] Frontend production build passed.
- [x] Scoped and full tracked `git diff --check` returned exit 0 with no whitespace errors.
- [x] Relevant untracked files had no trailing whitespace.
- [x] Runtime logs and `frontend/tsconfig.tsbuildinfo` were inventoried without cleanup or restoration.
- [x] No product code, product documentation, runtime log, external DB, provider, email, tunnel, staging, or commit operation was performed.

## Reference Documents (Tier 0-2)

| Tier | Document | Reason |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Approval, language, security, and traceability baseline |
| 0 | `docs/standards/development-standards.md` | Java/React verification and evidence requirements |
| 0 | `docs/standards/documentation-standards.md` | Deliverable structure |
| 0 | `docs/standards/glossary.md` | Canonical terminology |
| 1 | `docs/policies/quality-gates.md` | Regression and Evidence Pack gate |
| 1 | `docs/policies/security-policy.md` | Secret minimization and protected-resource expectations |
| REQ | `deliverables/user/REQ-20260714-ATS-001.md` | Approved P1 acceptance-hardening scope |
| WI | `deliverables/agent/WI-20260714-ATS-037-evidence-pack.md` | Question/private attachment and bootstrap-log changes |
| WI | `deliverables/agent/WI-20260714-ATS-038-evidence-pack.md` | Canonical subscription-plan bootstrap changes |
| Context | `src/main/resources/application-acceptance.yml` | Acceptance profile external-placeholder contract |
| Context | `scripts/acceptance/AcceptanceLifecycle.psm1` | Runtime ownership and launcher boundary; inspected only |

**Injection rules applied**:

- Rule source: `AGENTS.md` and `deliverables/agent/WI-20260714-ATS-039-handoff.md`.
- Assignee: `qa`.
- Task type: independent read-only verification.
- Required command order: backend serially, then frontend.

## Evidence Pointers

- Backend acceptance/bootstrap tests:
  - `src/test/java/com/atstudio/atstudio/bootstrap/AcceptanceSubscriptionPlanBootstrapRunnerTest.java:42`
  - `src/test/java/com/atstudio/atstudio/bootstrap/AcceptanceSubscriptionPlanBootstrapConfigurationTest.java:20`
  - `src/test/java/com/atstudio/atstudio/bootstrap/TestUserBootstrapRunnerTest.java:62`
  - `src/test/java/com/atstudio/atstudio/config/AcceptanceProfileConfigurationTest.java:18`
  - `src/test/java/com/atstudio/atstudio/config/AcceptanceStartupGuardTest.java:20`
- Backend Question/security tests:
  - `src/test/java/com/atstudio/atstudio/controller/QuestionControllerTest.java:59`
  - `src/test/java/com/atstudio/atstudio/service/QuestionServiceTest.java:65`
- Frontend focused tests:
  - `frontend/src/api/auth.test.ts:15`
  - `frontend/src/api/client.test.ts:42`
  - `frontend/src/store/authStore.test.ts:51`
  - `frontend/src/pages/auth/SocialLoginPage.test.tsx:61`
  - `frontend/vite.config.test.ts:20`
- Generated verification outputs:
  - `build/test-results/test/`
  - `build/reports/tests/test/index.html`
  - `build/reports/problems/problems-report.html`
  - `frontend/dist/`
  - `frontend/tsconfig.tsbuildinfo`
- Deliverables created by this WI:
  - `deliverables/user/WI-20260714-ATS-039-summary.md`
  - `deliverables/agent/WI-20260714-ATS-039-evidence-pack.md`

## Commands & Outputs

### Backend compile

- `.\gradlew.bat compileJava --rerun-tasks --console=plain`
  - PASS: `BUILD SUCCESSFUL in 13s`; 1 task executed.

### Backend focused tests

Every class ran in its own Gradle process with `--rerun-tasks`; no test processes were parallelized.

| Command filter | Gradle result | Tests | Failed | Errors | Skipped | XML time |
|---|---:|---:|---:|---:|---:|---:|
| `AcceptanceSubscriptionPlanBootstrapRunnerTest` | PASS, 19s | 6 | 0 | 0 | 0 | 2.074s |
| `AcceptanceSubscriptionPlanBootstrapConfigurationTest` | PASS, 18s | 1 | 0 | 0 | 0 | 2.391s |
| `TestUserBootstrapRunnerTest` | PASS, 18s | 3 | 0 | 0 | 0 | 2.991s |
| `AcceptanceProfileConfigurationTest` | PASS, 14s | 2 | 0 | 0 | 0 | 0.352s |
| `AcceptanceStartupGuardTest` | PASS, 15s | 12 | 0 | 0 | 0 | 0.294s |
| `QuestionControllerTest` | PASS, 34s | 20 | 0 | 0 | 0 | 18.918s |
| `QuestionServiceTest` | PASS, 17s | 28 | 0 | 0 | 0 | 3.223s |
| **Total** | **PASS** | **72** | **0** | **0** | **0** | **30.243s** |

Command form used for each class:

- `.\gradlew.bat test --tests "<fully-qualified-class>" --rerun-tasks --console=plain`

`QuestionServiceTest` is emitted as seven nested-suite XML files. Their counts were summed from the `<testsuite>` headers because no top-level XML file exists.

### Frontend

- `npm.cmd run typecheck`
  - PASS in 6.6s; `tsc --noEmit` returned exit 0.
- `npm.cmd run test -- src/api/auth.test.ts src/api/client.test.ts src/store/authStore.test.ts src/pages/auth/SocialLoginPage.test.tsx vite.config.test.ts`
  - PASS in 5.1s; Vitest reported 5/5 files and 23/23 tests passed, duration 3.60s.
- `npm.cmd run build`
  - PASS in 9.7s; `tsc -b && vite build` transformed 259 modules and Vite completed in 2.60s.

### Diff and untracked-file checks

- `git diff --check -- <15 tracked verification paths>`
  - PASS: exit 0, 0 whitespace errors, 15 LF-to-CRLF warnings.
- `git diff --check`
  - PASS: exit 0, 0 whitespace errors, 70 LF-to-CRLF warnings across the shared tracked working tree.
- `Select-String -LiteralPath <14 relevant untracked paths> -Pattern '[ \t]+$'`
  - PASS: 0 matches, 0 missing paths.
- `git diff --cached --name-only`
  - Empty; no staged changes.

## Result Classification

| Classification | Count | Notes |
|---|---:|---|
| Product failures | 0 | All bounded checks passed. |
| Environment failures | 0 | No command stalled or exceeded the five-minute threshold. |
| Blocking findings | 0 | WI-040 may consume this packet. |
| Non-blocking warnings | 3 categories | Gradle unchecked operations, JVM class-sharing, and Git LF-to-CRLF notices. |
| Intentionally deferred | 1 | Full backend suite remains reserved for WI-028. |

## Generated / Excluded Artifacts

- `build/` is ignored by `.gitignore`; 686 files had verification-window timestamps after the serial Gradle reruns. The final test-results directory contains the last class run, while class counts above were captured immediately after each command.
- `frontend/dist/` is ignored by `frontend/.gitignore`; the build produced 131 files, of which 130 received verification-window timestamps.
- `frontend/tsconfig.tsbuildinfo` was already tracked-modified before verification. `npm run build` updated its timestamp from `2026-07-14T15:52:30.8910243Z` to `2026-07-14T23:15:49.8758731Z`, but length remained 4,826 bytes and SHA-256 content was unchanged. It was not restored.
- Pre-existing excluded runtime logs were byte-for-byte and timestamp unchanged:
  - `cloudflared.err.log` (3,953 bytes)
  - `cloudflared.out.log` (0 bytes)
  - `frontend/vite.err.log` (0 bytes)
  - `frontend/vite.out.log` (296 bytes)
- No verification log file was created outside the standard ignored Gradle/Vite outputs.

## State-Safety Evidence

- No server, Vite dev process, Spring Boot runtime, Cloudflare tunnel, provider, payment, or email command was invoked.
- No external or retained database and no MySQL command was used. The required `QuestionControllerTest` used the repository test profile's embedded in-memory H2/JPA context and shut it down with the test JVM.
- No schema script, acceptance lifecycle start/stop/status command, or public URL was invoked.
- No product code/document/runtime log was edited, reverted, cleaned, staged, or committed.
- No secret-bearing environment output, JDBC URL, token, credential, or public URL is recorded here.
- The longest command was `QuestionControllerTest` at 35.1s wall time; the user-specified five-minute no-output termination rule was not triggered.

## Risks / Rollback

- Risks:
  - This is a bounded code-level gate. It does not prove a live disposable MySQL fresh-schema boot, public tunnel behavior, or client workflow.
  - Full backend-suite regressions remain intentionally outside this WI.
  - Existing line-ending warnings and excluded runtime logs remain for MA cleanup decisions.
- Rollback:
  - Product rollback is not applicable because this WI made no product changes.
  - Remove only the two WI-039 deliverables if this QA record must be withdrawn.
  - Do not restore `frontend/tsconfig.tsbuildinfo` or clean shared generated outputs as part of this WI.

## Follow-ups

- `WI-20260714-ATS-040` can consume this packet for the next chain step.
- Any live disposable-DB, server, tunnel, provider, or client smoke work requires its separately authorized WI boundary.

---
version: 1.0
last_updated: 2026-08-17
project: ATS
owner: re
category: evidence-pack
status: complete
related_wi: WI-20260817-ATS-013
dependencies:
  - path: WI-20260817-ATS-013-handoff.md
    reason: Approved test-only scope, source pointers, and DoD
  - path: ../user/REQ-20260817-ATS-009.md
    reason: Approved release quality-gate request
  - path: ../../docs/SR/SR-93.md
    reason: Current contract context
---

# Evidence Pack: WI-20260817-ATS-013

## Summary

- Restored the release test gate by aligning only the cited stale test fixture,
  assertion, and test mock with the already-implemented consent,
  authentication-error, and duplicate-logout contracts.

## Scope / DoD Check

- [x] The registration-rate-limit fixture submits required consent values and
  proves five accepted public registrations followed by a `429` response.
- [x] The login test separately proves `INVALID_CREDENTIALS` and the safe
  generic fallback for an unclassified `401`.
- [x] The Header test mocks every imported `authStore`/`toastStore` dependency
  it uses and proves same-tick duplicate logout suppression.
- [x] Focused and full backend/frontend suites passed.
- [x] Frontend typecheck, lint, and formatting checks passed.
- [x] No runtime application source, dependency, configuration, database,
  output asset, or source-document change was made by this WI.

## Reference Documents (Tier 0-2)

| Tier | Document | Use |
| --- | --- | --- |
| 0 | `docs/standards/core-principles.md` | Approved-scope and traceability control |
| 0 | `docs/standards/development-standards.md` | Test verification discipline |
| 0 | `docs/standards/documentation-standards.md` | Deliverable structure |
| 0 | `docs/standards/glossary.md` | WI terminology |
| 1 | `docs/policies/quality-gates.md` | Required quality checks |
| 1 | `docs/policies/security-policy.md` | Safe public test-output boundary |
| 2 | `.agents/skills/react-best-practices/AGENTS.md` | Frontend test context |
| 2 | `docs/design/api-spec.md` | Consent and logout outcome contract |
| Context | `deliverables/user/REQ-20260817-ATS-009.md` | Approved request |
| Context | `docs/SR/SR-93.md` | Current contract context |

## Evidence Pointers

- `src/main/java/com/atstudio/atstudio/dto/user/RegisterRequest.java:51-62`
  requires affirmative Terms and Privacy consent; it was inspected and not
  changed.
- `src/test/java/com/atstudio/atstudio/controller/SecurityFilterChainTest.java:216-244`
  supplies the consent fixture and preserves the six-request rate-limit proof.
- `frontend/src/api/authError.ts:34-45` maps only known login error codes and
  otherwise uses the safe generic fallback; it was inspected and not changed.
- `frontend/src/test/coverage/publicAuthShell.coverage.test.tsx:541-572`
  provides code-bearing `INVALID_CREDENTIALS` and code-less `401` coverage.
- `frontend/src/layouts/Header.tsx:194-207` suppresses pending duplicate logout
  and was inspected without modification.
- `frontend/src/test/coverage/shellCatalogRouterGaps.coverage.test.tsx:132-159`
  mocks the required store exports; `:605-628` verifies pending suppression.
- `frontend/src/store/authStore.ts:32-51` defines the logout warning and result
  type; it was inspected and not changed.

## Reproduction And Results

| Command | Result |
| --- | --- |
| `.\gradlew.bat test --tests "com.atstudio.atstudio.controller.SecurityFilterChainTest"` before repair | Failed: 23 tests completed, 1 failed at `SecurityFilterChainTest.java:232` because the stale registration body omitted required consent. |
| `npm test -- --run src/test/coverage/publicAuthShell.coverage.test.tsx src/test/coverage/shellCatalogRouterGaps.coverage.test.tsx` before repair | Failed: 2 test failures and 1 unhandled Vitest missing-export error. |
| `.\gradlew.bat test --tests "com.atstudio.atstudio.controller.SecurityFilterChainTest"` after repair | PASS: `BUILD SUCCESSFUL` in 29s. |
| `npm test -- --run src/test/coverage/publicAuthShell.coverage.test.tsx src/test/coverage/shellCatalogRouterGaps.coverage.test.tsx` after repair | PASS: 2 files, 54 tests. |
| `.\gradlew.bat test` | PASS: `BUILD SUCCESSFUL` in 2m 28s; 186 XML suites, 1,614 tests, 0 failures, 0 errors, 19 skipped. |
| `npm test -- --run` | PASS: 111 files, 1,440 tests. |
| `npm run typecheck` | PASS. |
| `npm run lint` | PASS with `--max-warnings 0`. |
| `npm run format` | PASS: all matched files use Prettier code style. |
| `git diff --check -- <five WI files>` | PASS: no whitespace errors. |

## Risks / Rollback

- Risk: The worktree already contained unrelated runtime and documentation
  modifications. This WI neither reviewed them as its own changes nor altered
  them; the full-suite outcome reflects the current combined worktree.
- Rollback: Revert only the three test-file hunks for this WI and the two
  deliverables. No runtime, dependency, configuration, database, or asset
  rollback is required.

## Follow-up

- The handoff identifies `WI-20260817-ATS-014` as the next blocked WI. This
  evidence pack makes no changes for that WI.

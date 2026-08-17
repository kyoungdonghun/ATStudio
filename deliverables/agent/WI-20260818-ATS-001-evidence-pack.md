---
version: 1.0
last_updated: 2026-08-18
project: ATS
owner: se
category: evidence-pack
status: complete
related_wi: WI-20260818-ATS-001
dependencies:
  - path: WI-20260818-ATS-001-handoff.md
    reason: Approved implementation instructions and output contract
  - path: ../user/REQ-20260818-ATS-001.md
    reason: Approved scope and success criteria
---

# Evidence Pack: WI-20260818-ATS-001

## Summary

- Classified Spring MVC `NoResourceFoundException` as `BUSINESS_ERROR.RESOURCE_NOT_FOUND` and added a focused regression test for the HTTP 404 JSON error contract.

## Scope / DoD Check

- [x] `NoResourceFoundException` maps to HTTP 404 and `RESOURCE_NOT_FOUND`.
- [x] Existing generic unexpected exception handling remains unchanged.
- [x] Focused `GlobalExceptionHandlerTest` passes.
- [x] `git diff --check` completed with no whitespace error.
- [x] No runtime, database, external integration, configuration, or secret operation occurred.

## Reference Documents (Tier 0-2)

| Tier | Document | Reason |
| --- | --- | --- |
| 0 | `docs/standards/core-principles.md` | Constitution and approved-execution boundary |
| 0 | `docs/standards/development-standards.md` | Java implementation and traceability standards |
| 1 | `docs/policies/quality-gates.md` | Focused regression and diff verification requirements |
| Context | `deliverables/user/REQ-20260818-ATS-001.md` | Approved scope and success criteria |
| Context | `deliverables/agent/WI-20260818-ATS-001-handoff.md` | WI output contract and constraints |

## Evidence Pointers

- `src/main/java/com/atstudio/atstudio/common/exception/GlobalExceptionHandler.java:23`
  imports `NoResourceFoundException`.
- `src/main/java/com/atstudio/atstudio/common/exception/GlobalExceptionHandler.java:98`
  maps that exception to `BUSINESS_ERROR.RESOURCE_NOT_FOUND` before the generic unexpected-error fallback.
- `src/test/java/com/atstudio/atstudio/common/exception/GlobalExceptionHandlerTest.java:63`
  creates the Spring 7 checked exception through the supported `(HttpMethod, String, String)` constructor.
- `src/test/java/com/atstudio/atstudio/common/exception/GlobalExceptionHandlerTest.java:123`
  verifies HTTP 404, response status `404`, and `RESOURCE_NOT_FOUND`.

## Commands & Outputs

- `./gradlew.bat test --tests "com.atstudio.atstudio.common.exception.GlobalExceptionHandlerTest"`
  -> `BUILD SUCCESSFUL in 8s`; 5 actionable tasks (2 executed, 3 up-to-date).
- `build/test-results/test/TEST-com.atstudio.atstudio.common.exception.GlobalExceptionHandlerTest.xml`
  -> 9 tests, 0 failures, 0 errors, 0 skipped.
- `git diff --check`
  -> exit code 0; no whitespace error. Git printed two CRLF advisory warnings for the two Java working-tree files.

## Risks / Rollback

- Risk: only Spring MVC's missing-static-resource exception classification changes; all other fallback branches are unchanged.
- Rollback: revert the two Java-file changes and remove this evidence pack plus
  `deliverables/user/WI-20260818-ATS-001-summary.md`. No runtime or data rollback is needed.

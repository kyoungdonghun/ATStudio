# Evidence Pack: WI-20260713-ATS-009

## Summary

- Security-reviewed protected media and mail logging, retained dedicated-preview path validation, and verified the merged P0 behavior.

## Review Outcome

- P0 findings: none remaining in reviewed media/mail scope.
- Corrective hardening retained from the interrupted PG review:
  - `TrackService.isDedicatedPreviewFile` accepts only a normalized `tracks/preview/` path distinct from the original path.
  - encoded and traversal variants of the original static route are rejected before resource resolution.
- The assigned review agents were stopped after repeated timeouts without final outputs. MA inspected the partial corrective diff, ran the combined suite, and completed this Evidence Pack.

## Evidence Pointers

- `src/main/java/com/atstudio/atstudio/config/SecurityConfig.java`: deny-all matcher precedes static fallback.
- `src/main/java/com/atstudio/atstudio/dto/track/TrackResponse.java`: explicit public/admin factories.
- `src/main/java/com/atstudio/atstudio/service/TrackService.java`: normalized dedicated-preview validation and bounded original fallback.
- `src/main/java/com/atstudio/atstudio/controller/TrackController.java`: public-length Range enforcement and `416` behavior.
- `src/main/java/com/atstudio/atstudio/service/EmailService.java`: delivery-ID-only logs.
- `src/test/java/com/atstudio/atstudio/controller/SecurityFilterChainTest.java`: anonymous/USER/ADMIN plus encoded/traversal route cases.
- `src/test/java/com/atstudio/atstudio/controller/TrackControllerTest.java`: malformed, multiple, suffix, repeated, and out-of-bound Range cases.
- `src/test/java/com/atstudio/atstudio/service/TrackServiceTest.java`: dedicated-preview path and 1-byte fail-closed cases.
- `src/test/java/com/atstudio/atstudio/service/EmailServiceTest.java`: captured success/failure log assertions.

## Commands and Results

- Combined focused Gradle command covering 11 suites: exit 0.
- XML header aggregation: 11 suites, 133 tests, 0 failures, 0 errors, 0 skipped.
- `rg` scan for mail fallback/body/token logging patterns: 0 matches.
- `git diff --check`: exit 0; line-ending notices only.

## Residual Risk / Rollback

- Dedicated low-quality preview generation remains future work; bounded fallback is the current control.
- Physical storage migration remains separately approved work.
- Rollback uses normal source/test revert; no DB or file-data rollback applies.

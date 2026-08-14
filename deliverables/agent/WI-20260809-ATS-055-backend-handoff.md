---
version: 1.0
last_updated: 2026-08-14
project: ATS
owner: ma
category: wi-implementation-handoff
status: active
dependencies:
  - path: WI-20260809-ATS-055-handoff.md
    reason: Canonical approved WI scope and constraints
---

# Backend Implementation Handoff: WI-20260809-ATS-055

## Assignment

- **Agent:** `se`
- **Purpose:** complete only the private-document streaming backend slice of
  WI-055.

## Scope

- Change Question attachment and Company Certification document controller
  responses from controller-buffered `byte[]` to standard Spring streaming
  responses backed by the service `Resource`.
- Remove `StreamUtils.copyToByteArray` and now-unused imports.
- Preserve existing service authorization/ownership invocation, encoded
  original filename, octet-stream content type, private/no-store cache headers,
  pragma, nosniff, sandbox CSP, disabled ranges, and certification access audit
  timing.
- Update focused controller tests to prove synthetic Resource bytes are streamed
  without a controller-sized intermediate array and all existing
  headers/security responses remain intact.

## Write Scope

- `src/main/java/com/atstudio/atstudio/controller/QuestionController.java`
- `src/main/java/com/atstudio/atstudio/controller/CompanyCertificationController.java`
- `src/test/java/com/atstudio/atstudio/controller/QuestionControllerTest.java`
- `src/test/java/com/atstudio/atstudio/controller/CompanyCertificationControllerTest.java`

## Verification

- Run focused RED/GREEN controller tests.
- Run backend compile/test command sufficient to catch response conversion.
- Run formatting or relevant style check and `git diff --check`.

## Constraints

- Read the canonical WI-055 handoff and required Tier 0/security context first.
- Touch only the four write-scope files.
- Use only synthetic in-memory resources; do not inspect private files,
  protected outputs, ignored secrets, or external effects.
- Do not change authorization, services, DTOs, audit timing, Range policy,
  dependencies, schema/data, docs, branches, commit, or push.
- Do not add a servlet filter, interceptor, or cross-controller registration to
  suppress Spring's automatic Resource Range handling. Keep the no-range
  contract local to each response, for example with `StreamingResponseBody`.

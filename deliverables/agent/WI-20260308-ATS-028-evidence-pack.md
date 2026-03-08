# WI-20260308-ATS-028 Evidence Pack

## WI Metadata

| Field | Value |
|-------|-------|
| WI ID | WI-20260308-ATS-028 |
| REQ | REQ-20260307-ATS-009 |
| Agent | se |
| Status | DONE (test pending manual run) |

## Patch Rationale

Three MAJOR fixes from Phase 1 cross-validation (REQ-009):

1. **M-3**: `getTrack()` was the only single-resource GET endpoint in TrackController missing the `.message()` call in `ResponseDTO` builder. All other mutation endpoints (`createTrack`, `updateTrack`) already included it. This inconsistency was flagged during audit.

2. **M-4**: `createTrack()`, `updateTrack()`, `deleteTrack()` lacked `@PreAuthorize("hasRole('ADMIN')")`. The `getTracksForAdmin()` endpoint already had it, but mutation endpoints were unprotected at the controller level. Spring Security `requestMatchers` may provide partial coverage, but explicit method-level security is the project standard.

3. **M-5**: `@RequestParam(required = false) Boolean isActive` relied on implicit parameter name binding (`isActive` -> `isActive`). The API spec defines the query parameter as `is_active` (snake_case). Without `name = "is_active"`, Spring would not bind `?is_active=true` to the `isActive` parameter.

## File:Line Pointers

| Change | File | Line(s) | Description |
|--------|------|---------|-------------|
| M-3 | `src/main/java/.../controller/TrackController.java` | 59 | Added `.message("Track retrieved")` |
| M-4a | `src/main/java/.../controller/TrackController.java` | 28 | Added `@PreAuthorize("hasRole('ADMIN')")` on `createTrack()` |
| M-4b | `src/main/java/.../controller/TrackController.java` | 86 | Added `@PreAuthorize("hasRole('ADMIN')")` on `updateTrack()` |
| M-4c | `src/main/java/.../controller/TrackController.java` | 97 | Added `@PreAuthorize("hasRole('ADMIN')")` on `deleteTrack()` |
| M-5 | `src/main/java/.../controller/TrackController.java` | 49 | Changed `@RequestParam(required = false)` to `@RequestParam(name = "is_active", required = false)` |

## Acceptance Criteria Verification

| Criterion | Status |
|-----------|--------|
| `getTrack()` includes `.message("Track retrieved")` | PASS (line 59) |
| `createTrack()` has `@PreAuthorize("hasRole('ADMIN')")` | PASS (line 28) |
| `updateTrack()` has `@PreAuthorize("hasRole('ADMIN')")` | PASS (line 86) |
| `deleteTrack()` has `@PreAuthorize("hasRole('ADMIN')")` | PASS (line 97) |
| `getTracksForAdmin()` param: `name = "is_active"` | PASS (line 49) |
| `./gradlew test` all pass | PENDING -- manual run required |

## Test Results

**Status**: PENDING -- Bash execution was denied during agent session.
**Action Required**: Run `gradlew.bat test` manually to confirm 0 failures.

## Follow-up WI

- None (WI-028 has no downstream blockers per handoff).

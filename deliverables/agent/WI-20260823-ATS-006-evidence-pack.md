# Evidence Pack: WI-20260823-ATS-006

## Work Item

- WI: `WI-20260823-ATS-006`
- REQ: `REQ-20260823-ATS-001`
- Agent: `se`
- Branch: `codex/v1-release-rehearsal-fixes`
- Depends on: `WI-20260823-ATS-005`
- Blocks: `WI-20260823-ATS-007`
- Result: **Completed**

## Summary

BUSINESS requests containing a non-null `job` are rejected before a User can
be created or mutated, while the existing BUSINESS `job=null` and INDIVIDUAL
flows remain valid.

## Scope / DoD Check

- [x] Registration rejects BUSINESS plus non-null `job` before persistence.
- [x] Complete profile rejects BUSINESS plus non-null `job` before mutation.
- [x] Profile update rejects a direct BUSINESS `job` payload before mutation.
- [x] Valid BUSINESS `job=null` and INDIVIDUAL paths remain covered by the
      focused `UserServiceTest` suite.
- [x] No schema/data migration, historical-data cleanup, client-worktree,
      business-UI, HomePage, secret/configuration, provider, mail, or payment
      change was made.

## Reference Documents

| Tier | Document | Purpose |
| --- | --- | --- |
| 0 | `docs/standards/core-principles.md` | Project constitution and scope controls. |
| 0 | `docs/standards/development-standards.md` | Service validation and test conventions. |
| 1 | `docs/policies/security-policy.md` | Auth/profile mutation boundary context. |
| 1 | `docs/policies/quality-gates.md` | Test and diff validation requirements. |
| 2 | `docs/design/api-spec.md` | `job` is INDIVIDUAL-only; BUSINESS uses `companyName`. |
| 2 | `docs/design/usecase/user-info.md` | Registration and profile mutation behavior. |
| REQ | `deliverables/user/REQ-20260823-ATS-001.md` | Approved scope and constraints. |
| Prior WI | `deliverables/agent/WI-20260823-ATS-005-evidence-pack.md` | Confirmed direct-payload defect. |

## Evidence Pointers

- `src/main/java/com/atstudio/atstudio/common/validation/RegisterProfileValidator.java:26`
  adds a `job` property violation for BUSINESS registration requests.
- `src/main/java/com/atstudio/atstudio/common/validation/CompleteProfileValidator.java:26`
  adds the matching BUSINESS rejection for profile completion requests.
- `src/main/java/com/atstudio/atstudio/service/UserService.java:122` rejects a
  direct update payload with `job` for a persisted BUSINESS user.
- `src/main/java/com/atstudio/atstudio/service/UserService.java:451` and `:495`
  retain equivalent service-boundary checks for registration and completion.
- `src/test/java/com/atstudio/atstudio/service/UserServiceTest.java:515`, `:701`,
  and `:791` cover the new register, complete-profile, and update-profile
  rejection paths.
- `src/test/java/com/atstudio/atstudio/service/UserServiceTest.java:483`, `:681`,
  and `:834` retain BUSINESS `job=null` coverage; existing INDIVIDUAL tests
  pass in the same focused suite.

## Commands and Results

| Command | Result |
| --- | --- |
| `git diff --check` | PASS: no whitespace errors. Git emitted existing CRLF-to-LF warnings for already modified files. |
| `gradlew.bat test --tests "com.atstudio.atstudio.service.UserServiceTest"` | PASS: `BUILD SUCCESSFUL` in 15s; 50 tests, 0 failures/errors. |
| `gradlew.bat test` | PASS: `BUILD SUCCESSFUL` in 2m 25s; 186 XML suites, 1,622 tests, 0 failures, 0 errors, 19 skipped. |

## Risk / Rollback

- Risk: Historical BUSINESS rows that already contain a `job` are intentionally
  not migrated or cleaned up. The update rejection is limited to a newly
  supplied direct `job` payload so this WI does not alter historical records.
- Rollback: Revert the two validators, the three `UserService` guard clauses,
  and the three corresponding `UserServiceTest` cases. No schema, data,
  external-provider, or secret rollback is required.

## Follow-up

- `WI-20260823-ATS-007`: independently verify the final contract and
  regression boundary.

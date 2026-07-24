# Evidence Pack: WI-20260724-ATS-001

## Summary (one-liner)

- Removed obsolete one-time payment aliases from the acceptance backend environment allowlist and locked the current V2 contract with focused regression tests.

## Scope / DoD Check

- [x] Removed `APP_PAYMENT_PROVIDER` from the acceptance launcher allowlist.
- [x] Removed `TOSS_CONFIRM_URL` from the acceptance launcher allowlist.
- [x] Proved that both retired names and the retired billing-key secret name are rejected.
- [x] Proved that current V2 billing-key and scheduler names remain accepted.
- [x] Updated the directly affected current-state SR wording.
- [x] Left application payment behavior, DB, external bundles, and secrets unchanged.
- [x] Ran focused PowerShell and Java tests.
- [x] Ran `git diff --check` for all owned files.

## Reference Documents (Tier 0-2)

**Injected Context**:

| Tier | Document | Reason |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | System constitution and execution boundaries |
| 0 | `docs/standards/development-standards.md` | Implementation and test standards for `se` |
| 1 | `docs/policies/security-policy.md` | Secret isolation and fail-closed handling |
| 1 | `docs/SR/SR-93.md` | Current payment operations and production-readiness boundary |
| Context | `deliverables/user/REQ-20260724-ATS-001.md` | Approved scope and success criteria |
| Context | `deliverables/agent/WI-20260724-ATS-001-handoff.md` | Mandatory WI scope and output contract |
| Context | `deliverables/user/WI-20260717-ATS-019-summary.md` | Previous V2 launcher contract correction |

**Injection Rules Applied**:

- Assignee: `se`
- Task type: implementation, security-sensitive environment contract
- Only the acceptance allowlist slice, focused tests, and directly affected current-state documentation were modified.
- No external bundle value was read, printed, or changed.

## Evidence Pointers

- `scripts/acceptance/AcceptanceLifecycle.psm1:17-30`
  - The optional backend allowlist retains current Toss recurring, V2 billing-key, and scheduler names without `APP_PAYMENT_PROVIDER` or `TOSS_CONFIRM_URL`.
- `scripts/acceptance/test-backend-environment.ps1:123-215`
  - Current V2 names are accepted; each obsolete name is absent from the allowlist and rejected through a synthetic bundle fixture.
- `scripts/acceptance/test-backend-environment.ps1:521-534`
  - The reported contract checks include obsolete payment-name rejection.
- `src/test/java/com/atstudio/atstudio/config/V1BackendBaselineContractTest.java:27-31`
  - The launcher module is an explicit V1 contract input.
- `src/test/java/com/atstudio/atstudio/config/V1BackendBaselineContractTest.java:124-165`
  - The V1 baseline test asserts current launcher names and rejects all three obsolete names.
- `docs/SR/SR-93.md:108-116`
  - Current-state documentation records that retired payment aliases are absent and rejected by the acceptance launcher.
- `deliverables/user/WI-20260724-ATS-001-summary.md`
  - User-facing result, external bundle impact, risk, and follow-up.

## Commands & Outputs

- `powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\acceptance\test-backend-environment.ps1`
  - PASS; reported all nine acceptance environment checks, including `obsolete-payment-name-rejection`.
- `powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\acceptance\test-dry-run.ps1`
  - PASS; reported all ten dry-run and lifecycle safety checks.
- `.\gradlew.bat test --tests "com.atstudio.atstudio.config.V1BackendBaselineContractTest"`
  - PASS; Gradle build successful.
- `git diff --check -- scripts/acceptance/AcceptanceLifecycle.psm1 scripts/acceptance/test-backend-environment.ps1 src/test/java/com/atstudio/atstudio/config/V1BackendBaselineContractTest.java docs/SR/SR-93.md`
  - PASS; no whitespace errors. Git emitted only the repository's existing LF-to-CRLF conversion warnings.

## Tests

| Test | Result |
|---|---|
| Acceptance backend environment contract | PASS |
| Acceptance dry-run contract | PASS |
| `V1BackendBaselineContractTest` | PASS: 6 tests, 0 failures, 0 errors, 0 skipped |
| Owned-file diff check | PASS |

## External Bundle Decision

- The repository did not read or mutate any external bundle.
- Bundles that already contain only current allowlisted names require no regeneration.
- An operator must remove the retired properties or regenerate any bundle containing `APP_PAYMENT_PROVIDER` or `TOSS_CONFIRM_URL` before its next acceptance launch.
- Such a stale bundle is expected to fail closed with the generic non-allowlisted-name error.

## Risks / Rollback

- Risk:
  - A stale external bundle containing a retired alias will no longer launch. This is the approved V1 behavior, not a compatibility regression.
- Rollback:
  - Revert only this WI's hunks in the launcher module, focused PowerShell test, V1 Java contract test, and SR-93.
  - Reverting would restore obsolete one-time payment aliases to an active acceptance path and is not recommended.

## Follow-ups

- Unblocks `WI-20260724-ATS-004` for full backend and acceptance verification.
- Unblocks `WI-20260724-ATS-006` for documentation consistency verification.
- This WI did not commit or push changes, as required by the handoff.

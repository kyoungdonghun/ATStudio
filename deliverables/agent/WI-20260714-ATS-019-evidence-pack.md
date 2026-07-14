# Evidence Pack: WI-20260714-ATS-019

## Summary (one-liner)
- Independently re-verified the WI-009~013 security chain with fixture-only focused tests and found no reproduced production defect; only WI-019 verification tests and deliverables were added.

## Scope / DoD Check
- [x] `/uploads/company-docs/**` static denial was re-verified for anonymous, USER, ADMIN, and encoded traversal variants.
- [x] ADMIN certification download headers stayed attachment-only and ignored `Range` as a partial-content bypass.
- [x] Certification image/document boundaries re-verified malformed/polyglot bytes: PNG trailing payload canonicalization, forged PDF extension/bytes mismatch rejection, and truncated PNG rejection.
- [x] Storage rollback/recovery re-verified stale `REPLACE` cleanup behavior without touching shared old references.
- [x] Refresh-session replay re-verified post-rotation stale token rejection and post-logout replay rejection.
- [x] CSV formula neutralization, password-change/reset revocation, and shared-reference cleanup stayed green through focused reruns.
- [x] `compileTestJava`, focused WI-019 classes, and owned-file `git diff --check` passed.
- [x] Full `gradlew test` was intentionally not run, per user instruction to reserve Phase 7 as the single full-suite execution point.

## Reference Documents (Tier 0-2)

**Injected Context**:

| Tier | Document | Reason |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Constitution, approval, traceability |
| 0 | `docs/standards/development-standards.md` | Java/Spring test and evidence standards |
| 0 | `docs/standards/documentation-standards.md` | Deliverable structure |
| 0 | `docs/standards/glossary.md` | Canonical WI/RE/security terminology |
| 1 | `docs/policies/security-policy.md` | Sensitive-file, token, and protected-resource rules |
| 1 | `docs/policies/access-control-policy.md` | Default-deny and least-privilege review lens |
| 1 | `docs/policies/quality-gates.md` | Focused verification and evidence gate |
| 2 | `deliverables/user/REQ-20260714-ATS-001.md` | Approved P1 scope and execution constraints |
| 2 | `docs/design/p1-security-acceptance-hardening-design.md` | Sections 3, 4, 5, 6, 7 security contracts |
| 2 | `deliverables/agent/WI-20260714-ATS-009-evidence-pack.md` | Canonical image prior implementation evidence |
| 2 | `deliverables/agent/WI-20260714-ATS-010-evidence-pack.md` | Certification quarantine prior implementation evidence |
| 2 | `deliverables/agent/WI-20260714-ATS-011-evidence-pack.md` | Refresh revocation prior implementation evidence |
| 2 | `deliverables/agent/WI-20260714-ATS-012-evidence-pack.md` | Storage journal/recovery prior implementation evidence |
| 2 | `deliverables/agent/WI-20260714-ATS-013-evidence-pack.md` | CSV neutralization prior implementation evidence |

**Injection Rules Applied**:
- Rule source: `deliverables/agent/WI-20260714-ATS-019-handoff.md`
- Assignee: `re`
- Task type: independent verification / security / testing
- Required context: Tier 0 + security/access/quality policies + WI-009~013 evidence

## Evidence Pointers

- Production code under verification:
  - `src/main/java/com/atstudio/atstudio/config/SecurityConfig.java:84` - static `/uploads/company-docs/**` deny rule.
  - `src/main/java/com/atstudio/atstudio/controller/CompanyCertificationController.java:101` - ADMIN attachment-only download endpoint.
  - `src/main/java/com/atstudio/atstudio/controller/CompanyCertificationController.java:117` - `Accept-Ranges: none` contract.
  - `src/main/java/com/atstudio/atstudio/service/CompanyCertificationService.java:128` - resubmit path defers previous document deletion to after-commit storage lifecycle.
  - `src/main/java/com/atstudio/atstudio/service/CompanyCertificationService.java:200` - private-root document load for ADMIN download.
  - `src/main/java/com/atstudio/atstudio/service/CompanyCertificationService.java:335` - certification image path reuses canonical image pipeline.
  - `src/main/java/com/atstudio/atstudio/service/auth/AuthService.java:94` - stale refresh hash mismatch rejection before rotation.
  - `src/main/java/com/atstudio/atstudio/service/auth/AuthService.java:115` - logout clears stored refresh capability.
  - `src/main/java/com/atstudio/atstudio/service/UserService.java:193` - password change clears refresh capability.
  - `src/main/java/com/atstudio/atstudio/service/EmailService.java:160` - password reset clears refresh capability.
  - `src/main/java/com/atstudio/atstudio/service/storage/StorageMutationCoordinator.java:100-110` - delete-after-commit journal path.
  - `src/main/java/com/atstudio/atstudio/service/storage/StorageMutationCoordinator.java:267` - failed cleanup becomes durable retry.
  - `src/main/java/com/atstudio/atstudio/service/storage/StorageMutationRecoveryService.java:151` - retry scheduling for stale journal recovery.
  - `src/main/java/com/atstudio/atstudio/service/AdminWhitelistChannelService.java:193-196` - output-only CSV formula neutralization.

- Verification tests added/extended:
  - `src/test/java/com/atstudio/atstudio/controller/CompanyCertificationControllerTest.java:232-291` - `Range` attachment behavior plus static deny chain for `/uploads/company-docs/**`.
  - `src/test/java/com/atstudio/atstudio/service/CompanyCertificationSecurityVerificationTest.java:58-153` - fixture-only certification polyglot/malformed boundary verification using the real canonical image service.
  - `src/test/java/com/atstudio/atstudio/service/auth/AuthServiceTest.java:179-205` - stale refresh replay after a successful rotation preserves the newer session.
  - `src/test/java/com/atstudio/atstudio/service/auth/AuthServiceTest.java:254-273` - logout clears state and blocks refresh replay.
  - `src/test/java/com/atstudio/atstudio/service/storage/StorageMutationRecoveryVerificationTest.java:39-71` - stale `REPLACE` without live DB reference cleans only the new file path.

- Deliverables:
  - `deliverables/user/WI-20260714-ATS-019-summary.md`
  - `deliverables/agent/WI-20260714-ATS-019-evidence-pack.md`

## Commands & Outputs

- `.\gradlew.bat compileTestJava --console=plain`
  - PASS: `BUILD SUCCESSFUL`.
- `.\gradlew.bat test --tests "com.atstudio.atstudio.controller.CompanyCertificationControllerTest" --tests "com.atstudio.atstudio.service.CompanyCertificationServiceTest" --tests "com.atstudio.atstudio.service.CompanyCertificationSecurityVerificationTest" --tests "com.atstudio.atstudio.service.image.CanonicalImageServiceTest" --tests "com.atstudio.atstudio.service.storage.StorageMutationCoordinatorTest" --tests "com.atstudio.atstudio.service.storage.StorageMutationRecoveryServiceTest" --tests "com.atstudio.atstudio.service.storage.StorageMutationRecoveryVerificationTest" --tests "com.atstudio.atstudio.service.auth.AuthServiceTest" --tests "com.atstudio.atstudio.service.UserServiceTest" --tests "com.atstudio.atstudio.service.EmailServiceTest" --tests "com.atstudio.atstudio.service.AdminWhitelistChannelServiceTest" --tests "com.atstudio.atstudio.repository.UserRepositoryTest" --console=plain`
  - PASS: `BUILD SUCCESSFUL`.
  - Console showed the focused WI-019 classes only; no full-suite run was invoked.
- `git diff --check -- src/test/java/com/atstudio/atstudio/controller/CompanyCertificationControllerTest.java src/test/java/com/atstudio/atstudio/service/auth/AuthServiceTest.java src/test/java/com/atstudio/atstudio/service/CompanyCertificationSecurityVerificationTest.java src/test/java/com/atstudio/atstudio/service/storage/StorageMutationRecoveryVerificationTest.java`
  - PASS: no whitespace errors; only LF/CRLF worktree warnings were emitted.

## Tests

- `CompanyCertificationControllerTest`
  - static `/uploads/company-docs/**` deny re-verified for anonymous, USER, ADMIN, and encoded traversal.
  - ADMIN download preserved attachment-only headers and ignored `Range`.
- `CompanyCertificationSecurityVerificationTest`
  - PNG trailing payload was re-encoded to canonical JPEG and sent only to `StorageRoot.PRIVATE`.
  - forged `.pdf` + PNG bytes was rejected before storage/DB mutation.
  - truncated PNG signature was rejected before storage/DB mutation.
- `AuthServiceTest`
  - stale replay after successful rotation failed with `REFRESH_TOKEN_INVALID` and preserved the newer refresh hash.
  - logout cleared refresh state and rejected replay of the pre-logout token.
- `StorageMutationRecoveryVerificationTest`
  - stale `REPLACE` with no live DB reference cleaned the new key only and did not invoke old-key cleanup.
- Focused rerun coverage retained green status for:
  - `CompanyCertificationServiceTest`
  - `CanonicalImageServiceTest`
  - `StorageMutationCoordinatorTest`
  - `StorageMutationRecoveryServiceTest`
  - `UserServiceTest`
  - `EmailServiceTest`
  - `AdminWhitelistChannelServiceTest`
  - `UserRepositoryTest`

## Risks / Rollback

- Risks:
  - `build/test-results` is a shared directory in this worktree; by user instruction, absence or churn there must not be interpreted as WI-019 failure when the focused console rerun is green.
  - This WI intentionally did not run the full backend suite; Phase 7 remains the single full-suite execution point.
  - Windows symbolic-link privilege remains environment-dependent; no new symlink assertion was added beyond the existing guarded storage tests.
- Rollback:
  - Revert only the WI-019 verification tests and the two WI-019 deliverables.
  - No production code rollback is required because no production file was changed.

## Follow-ups

- `WI-20260714-ATS-024` and `WI-20260714-ATS-025` can cite this packet as independent verification evidence for file/session/storage/CSV security boundaries.
- Phase 7 should execute the single authorized full-suite pass and capture any shared-result-directory anomalies separately from WI-019's focused rerun evidence.

# Evidence Pack: WI-20260902-ATS-002

## Implementation Evidence

| Concern | Evidence |
|---|---|
| Explicit roots | `LocalStorageService` validates absolute roots when required and always rejects equal/nested roots. |
| Production fail-closed | `LocalStorageService` treats `prod`, `production`, and their hyphenated variants as explicit-root runtimes; `StorageIntegrityStartupGuard` requires enabled strict audit. |
| Acceptance fail-closed | `application-acceptance.yml` enables required roots plus strict audit; `AcceptanceLifecycle.psm1` validates bundle roots before spawning children. |
| Safe report | `StorageIntegrityService` audits all persisted file-reference domains; response DTOs omit paths, keys, filenames, and bytes. |
| Authorization | `AdminStorageIntegrityController` requires `ROLE_ADMIN`; MockMvc tests cover anonymous, USER, and ADMIN calls. |

## Commands and Results

```powershell
.\gradlew.bat test
# BUILD SUCCESSFUL; XML aggregate: tests=1632 failures=0 errors=0 skipped=19

.\gradlew.bat build
# BUILD SUCCESSFUL

powershell -NoProfile -ExecutionPolicy Bypass -File scripts/acceptance/test-backend-environment.ps1
# passed

powershell -NoProfile -ExecutionPolicy Bypass -File scripts/acceptance/test-dry-run.ps1
# passed
```

## Rollback and Data Boundary

- Source/config rollback is commit-level only.
- No DB mutation and no object-store mutation occurred.
- Existing missing references are detected but not repaired; repair needs an approved source/target inventory.

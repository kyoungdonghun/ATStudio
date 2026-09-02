# Evidence Pack: WI-20260902-ATS-004

## Review Matrix

| Review area | Result | Evidence |
|---|---|---|
| Root explicitness and separation | Pass | `LocalStorageService` validates explicit absolute roots in required runtimes and rejects equality/nesting. |
| Production behavior | Pass | Both root explicitness and strict startup audit are enforced for production-like profiles. |
| Acceptance lifecycle order | Pass | Existing ready runtime is observed first; any new runtime validates the bundle before tunnel/backend/frontend spawn. |
| Storage disclosure | Pass | DTOs contain only domain, root classification, record ID, and reference type. |
| Authorization | Pass | Controller `@PreAuthorize` and anonymous/USER/ADMIN MockMvc tests. |
| Domain coverage | Pass | Track audio/thumbnails, album/playlist thumbnails, company documents, notice/question attachments. |
| Regression gate | Pass | Full backend: 1,632 tests, 0 failures/errors; Gradle build passed. |
| Script and document gate | Pass | Both acceptance PowerShell tests, document validation, and diff check passed. |
| Normal local restart | Pass with expected warning | Local frontend/backend restarted from the target worktree; startup audit inspected 30 references and warned about 10 existing missing references. |

## Classified Findings

- **No blocking code finding.**
- **Separate approved-data operation:** legacy referenced assets are not in the currently configured roots. The implementation detects this; it does not infer a correct copy source or mutate data.
- **Maintenance:** repository-wide `findAll()` scanning should be replaced with paged/batched inspection if operational data grows materially.
- **Not claimed:** production backup automation, retained-data migration, or deployment provisioning.

## Restart Probe

- The restarted local frontend and track-list API both returned HTTP `200`.
- A current-root Track Range request returned HTTP `206`.
- A historical missing-reference Track Range request returned HTTP `500`.
- The new ADMIN integrity route returned HTTP `401` without credentials, proving its live mapping while preserving ADMIN-only access.

## Reproducible Commands

- `.\gradlew.bat test`
- `.\gradlew.bat build`
- `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/acceptance/test-backend-environment.ps1`
- `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/acceptance/test-dry-run.ps1`
- `python .agents/skills/validate-docs/scripts/validate_docs.py`
- `git diff --check`

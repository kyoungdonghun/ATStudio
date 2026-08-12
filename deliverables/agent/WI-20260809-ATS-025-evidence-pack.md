# Evidence Pack: WI-20260809-ATS-025

## Summary

- Completed the creator/admin catalog-management audit closeout as a documentation-only, read-only work item.
- In-scope row outcome: `FAIL 9`. `G-ADMIN` sublane outcome: `PASS 1` anonymous and `BLOCKED 2` authenticated wrong-role/ADMIN.
- A `FAIL` means at least one required state has a confirmed defect; it does not mean every behavior in the row failed.
- Audit branch: `codex/v1-release-rehearsal-fixes`. Tracked audit baseline: `e343c20`.
- No tracked product diff, product/runtime mutation, database/storage/provider/mail/payment/secret operation, stage, or commit was produced.

## Scope and DoD Check

- [x] Classified `CRT-01` through `CRT-05`, `ADM-07`, `ADM-08`, `ADM-12`, and `ADM-13`.
- [x] Classified anonymous, authenticated wrong-role, and authenticated ADMIN `G-ADMIN` sublanes without substituting source for blocked live evidence.
- [x] Separated UI observation, frontend API invocation, server response/source contract, and canonical/durable-state evidence.
- [x] Recorded one finding per independent cause, including false-positive and missing-test contracts.
- [x] Recorded frontend/backend regression results and stated that passing tests do not prove invalid or missing contracts.
- [x] Recorded browser restoration and an explicit empty WI-025 screenshot inventory.
- [x] Preserved and did not inspect `output/client-demo-screenshots-20260716-140514.zip`.
- [x] Performed no prohibited operation and no stage or commit.

## Reference Documents

**Handoff-declared context:**

| Tier | Document                                        | Reason                                                |
| ---- | ----------------------------------------------- | ----------------------------------------------------- |
| 0    | `docs/standards/core-principles.md`             | Constitution and evidence boundaries                  |
| 0    | `docs/standards/development-standards.md`       | Frontend/backend implementation standards             |
| 1    | `docs/policies/security-policy.md`              | Authorization, secret, and public-resource boundaries |
| 1    | `docs/policies/quality-gates.md`                | Audit and validation gates                            |
| 2    | `docs/standards/frontend-standards.md`          | React form, state, and accessibility contract         |
| 2    | `docs/ui/screen-flow.md`                        | Cross-screen behavior and projection flow             |
| 2    | `docs/ui/atstudio-front-list.md`                | Route/screen ownership                                |
| 2    | `docs/ui/modal-list.md`                         | Modal ownership and state expectations                |
| 2    | `docs/design/api-spec.md`                       | API and pagination/reorder contracts                  |
| 2    | `docs/design/usecase/sound-track.md`            | Track create/edit/delete/search contract              |
| 2    | `docs/design/usecase/sound-album.md`            | Album membership and reorder contract                 |
| 2    | `docs/design/usecase/sound-tag.md`              | Tag taxonomy and mutation contract                    |
| 2    | `docs/design/usecase/user-notice.md`            | Notice and attachment contract                        |
| 2    | `.agents/skills/react-best-practices/AGENTS.md` | React review guidance                                 |

**Injection rules applied:**

- Rule source: `.claude/config/context-injection-rules.json`.
- Assignee: `qa-fe`.
- Task type: frontend/integration read-only audit.
- Primary execution inputs: `deliverables/agent/WI-20260809-ATS-025-handoff.md`, `deliverables/agent/WI-20260809-ATS-020-acceptance-matrix.md`, and `deliverables/agent/WI-20260809-ATS-025-findings.md`.
- Cross-entry context: WI-021 through WI-024 findings/evidence, as declared by the handoff.

## Row Outcome

| Group   | Rows or sublane                                  | Result    | Basis                                                                                                     |
| ------- | ------------------------------------------------ | --------- | --------------------------------------------------------------------------------------------------------- |
| Guard   | `G-ADMIN` anonymous                              | `PASS`    | Nine local ADMIN routes redirected to Login with encoded local `returnTo`; no open redirect was observed. |
| Guard   | `G-ADMIN` authenticated wrong-role               | `BLOCKED` | No approved authenticated non-ADMIN fixture/session.                                                      |
| Guard   | `G-ADMIN` authenticated ADMIN                    | `BLOCKED` | No approved ADMIN fixture/session; UI, mutation, API response, and durable projection were not run.       |
| Creator | `CRT-01`, `CRT-02`, `CRT-03`, `CRT-04`, `CRT-05` | `FAIL`    | At least one confirmed source/contract defect affects every row.                                          |
| Admin   | `ADM-07`, `ADM-08`, `ADM-12`, `ADM-13`           | `FAIL`    | At least one confirmed source/contract defect affects every row.                                          |

Totals: in-scope rows `FAIL 9`; guard sublanes `PASS 1`, `BLOCKED 2`.

## Evidence Pointers

### Anonymous G-ADMIN Routes

`frontend/src/router/index.tsx:118-120,209-235` assigns all nine routes to the shared ADMIN guard. `frontend/src/router/ProtectedRoute.tsx:34-60` constructs the Login target from the local `pathname + search` through `URLSearchParams`.

| Route under audit                    | Anonymous result | Login return target                                              |
| ------------------------------------ | ---------------- | ---------------------------------------------------------------- |
| `/admin/tracks/upload?from=audit`    | `PASS`           | `/login?returnTo=%2Fadmin%2Ftracks%2Fupload%3Ffrom%3Daudit`      |
| `/admin/tracks/999/edit?from=audit`  | `PASS`           | `/login?returnTo=%2Fadmin%2Ftracks%2F999%2Fedit%3Ffrom%3Daudit`  |
| `/admin/albums?from=audit`           | `PASS`           | `/login?returnTo=%2Fadmin%2Falbums%3Ffrom%3Daudit`               |
| `/admin/albums/new?from=audit`       | `PASS`           | `/login?returnTo=%2Fadmin%2Falbums%2Fnew%3Ffrom%3Daudit`         |
| `/admin/albums/999/edit?from=audit`  | `PASS`           | `/login?returnTo=%2Fadmin%2Falbums%2F999%2Fedit%3Ffrom%3Daudit`  |
| `/admin/tags?from=audit`             | `PASS`           | `/login?returnTo=%2Fadmin%2Ftags%3Ffrom%3Daudit`                 |
| `/admin/track-manage?from=audit`     | `PASS`           | `/login?returnTo=%2Fadmin%2Ftrack-manage%3Ffrom%3Daudit`         |
| `/admin/notices/new?from=audit`      | `PASS`           | `/login?returnTo=%2Fadmin%2Fnotices%2Fnew%3Ffrom%3Daudit`        |
| `/admin/notices/999/edit?from=audit` | `PASS`           | `/login?returnTo=%2Fadmin%2Fnotices%2F999%2Fedit%3Ffrom%3Daudit` |

No external destination or open redirect was observed. Authenticated wrong-role behavior was not inferred from this anonymous result.

### Key Source and Contract Evidence

- Track deletion contradiction: `frontend/src/pages/admin/TrackManagePage.tsx:229-243`, `src/main/java/com/atstudio/atstudio/service/TrackService.java:217-228`, `docs/retrospective/domain-design.md:38-46`, and `docs/design/usecase/sound-track.md:287-313`.
- Album reorder defect: `frontend/src/pages/creator/AlbumEditPage.tsx:164-184`; exact backend `0..n-1` validation at `src/main/java/com/atstudio/atstudio/service/AlbumService.java:238-263`; broad false-positive assertion at `frontend/src/test/coverage/publicAuthShell.coverage.test.tsx:937-943`.
- Audio-format mismatch: `frontend/src/utils/validation.ts:142-154` versus `src/main/java/com/atstudio/atstudio/service/audio/AudioAnalysisFormat.java:10-21`.
- Track explicit-clear gap: `frontend/src/pages/creator/TrackEditPage.tsx:152-182` and null-preserve handling at `src/main/java/com/atstudio/atstudio/service/TrackService.java:200-210`.
- Album modal/search/race paths: `frontend/src/pages/creator/AlbumManagePage.tsx:37-115,137-140,203-246` and `frontend/src/pages/creator/AlbumEditPage.tsx:97-122,190-246,327-379`.
- Tag deletion impact: `frontend/src/pages/admin/TagManagePage.tsx:320-344` and `src/main/java/com/atstudio/atstudio/service/TagService.java:162-168`.
- Notice UI/read side effect: `frontend/src/pages/admin/NoticeEditPage.tsx:23-128`, `frontend/src/pages/public/NoticeDetailPage.tsx:81-96`, `frontend/src/api/notices.ts:76-91`, and `src/main/java/com/atstudio/atstudio/service/NoticeService.java:84-93`.
- Public-root mechanism: `src/main/java/com/atstudio/atstudio/service/AlbumService.java:48-61,112-128`, `src/main/java/com/atstudio/atstudio/service/NoticeService.java:167-189`, `src/main/java/com/atstudio/atstudio/service/storage/LocalStorageService.java:27,58-73,201-212`, `src/main/java/com/atstudio/atstudio/config/WebConfig.java:20-25`, and `src/main/java/com/atstudio/atstudio/config/SecurityConfig.java:83-89`.
- Test boundary: dedicated tests exist for Track upload/edit and Tag management; they are absent for the six Album management/create/edit, Track management, and Notice create/edit pages. The one-based pass-through example is at `frontend/src/api/domainApis.test.ts:82-85`.

### Findings Index

| ID             | Severity      | Finding                                                                                      |
| -------------- | ------------- | -------------------------------------------------------------------------------------------- |
| `F-UI-025-001` | `P1`          | Track soft delete destroys history and relationship records.                                 |
| `F-UI-025-002` | `P1`          | Album reorder sends a one-based contract rejected by the backend.                            |
| `F-UI-025-003` | `P2`          | Track forms advertise audio formats the backend rejects.                                     |
| `F-UI-025-004` | `P2`          | Track edit cannot clear all Tags and silently omits blank required-looking metadata.         |
| `F-UI-025-005` | `P2`          | AlbumManage has clearing, stale modal, pagination, and thumbnail-contract defects.           |
| `F-UI-025-006` | `P2`          | Album thumbnail validation lacks selection/submission race fencing and complete URL cleanup. |
| `F-UI-025-007` | `P2`          | Album Track search misstates its contract and lacks request/combobox controls.               |
| `F-UI-025-008` | `P2`          | Edit routes do not validate IDs before loading or mutation.                                  |
| `F-UI-025-009` | `P2`          | Track upload/edit have accessibility and retry-recovery gaps.                                |
| `F-UI-025-010` | `P2`          | TrackManage has stale request, URL, semantic, action, and recovery gaps.                     |
| `F-UI-025-011` | `P2`          | TagManage obscures recovery and destructive association impact.                              |
| `F-UI-025-012` | `P2`          | Notice create, edit, and public download states are incomplete.                              |
| `F-UI-025-013` | `P1`          | ADMIN uploads can place unvalidated active file types under a public root.                   |
| `F-UI-025-014` | `P2`          | Six dedicated page tests are missing and the broad reorder test is a false positive.         |
| `F-UI-025-015` | `P3 / REVIEW` | Authenticated responsive behavior remains unknown, not a proven visual defect.               |

Full detail is in `deliverables/agent/WI-20260809-ATS-025-findings.md`.

## UI, API, Server, and Durable-State Separation

- UI: anonymous redirects and encoded local return targets passed. Authenticated wrong-role/ADMIN page states, mutations, and all four required authenticated viewports were `BLOCKED`.
- Frontend API invocation: request shapes and missing controls were source-confirmed only. No authenticated mutation or download request was invoked.
- Server response: backend validation and storage mechanisms were source/test-confirmed. No authenticated live response is claimed.
- Canonical/durable state: database, storage, public projection after mutation, provider, mail, payment, and secret evidence remained `BLOCKED` or `NOT RUN`.
- Responsive evidence: `1440x900`, `1024x768`, `390x844`, and `360x800` authenticated checks were `BLOCKED`; source alone was not used to claim clipping.

## Screenshot Inventory

| Inventory          | Result | Reason                                                                                                                     |
| ------------------ | ------ | -------------------------------------------------------------------------------------------------------------------------- |
| WI-025 screenshots | `NONE` | No WI-025 screenshot was captured because authenticated ADMIN UI was unavailable. No screenshot path is invented or cited. |

## Test and Build Results

- Frontend targeted command recorded by the audit handoff: seven selected Vitest files.
- Frontend result: `7 files`, `86 tests`, all passed.
- Backend targeted Gradle run: `11 suites`, `145 tests`, failures `0`, errors `0`, skipped `0`; `BUILD SUCCESSFUL in 34s`.
- These are regression signals. They do not prove contracts that are invalid, asserted too broadly, or not covered, including Album reorder payload shape and the six missing dedicated page suites.
- The frontend/backend suites were audit inputs and were not rerun during this documentation packaging step.

## Browser Restoration and Operational Boundaries

- Restored URL: `http://127.0.0.1:5173/`.
- Viewport: `1280x720`.
- Scroll position: `0`.
- Open dialog count: `0`.
- File input count: `0`.
- The intentional demo ZIP was preserved and uninspected.
- No product/runtime mutation, download, upload, database, storage, provider, mail, payment, secret, or environment operation was performed.
- No stage or commit was performed.

## Commands and Outputs

- Frontend working directory: `C:\Users\jm991\Desktop\project\ATStudio\frontend`.
- Prettier write command:
  - `npx prettier --write ../deliverables/agent/WI-20260809-ATS-025-findings.md ../deliverables/agent/WI-20260809-ATS-025-evidence-pack.md ../deliverables/user/WI-20260809-ATS-025-summary.md`
  - Exit `0`; findings `73ms`, evidence pack `25ms`, summary `10ms (unchanged)`.
- Prettier check command:
  - `npx prettier --check ../deliverables/agent/WI-20260809-ATS-025-handoff.md ../deliverables/agent/WI-20260809-ATS-025-findings.md ../deliverables/agent/WI-20260809-ATS-025-evidence-pack.md ../deliverables/user/WI-20260809-ATS-025-summary.md`
  - Exit `0`; `All matched files use Prettier code style!`.
- Repository working directory: `C:\Users\jm991\Desktop\project\ATStudio`.
- Documentation validator command: `python .agents/skills/validate-docs/scripts/validate_docs.py`.
- Diff check command: `git diff --check`.
- Status command: `git status --short`.

## Documentation Closeout Validation

- Prettier write: exit `0` for findings, evidence pack, and summary.
- Prettier check: exit `0` for all four WI-025 documents, including the unchanged handoff.
- Documentation validator: exit `0`; all Tier 0 documents exist, no broken internal links, `537` traceability IDs matched supported formats, all documents were listed in the index, and all validations passed.
- `git diff --check`: exit `0` with no output.
- `git status --short`: exit `0`; output contained `31` untracked (`??`) audit deliverable/output entries, including the four WI-025 documents, and no tracked-modification or staged entry. This confirms no tracked product diff and no staging.

## Risks and Rollback

### Risks

- Three `P1` findings affect Track durability, Album reorder correctness, and ADMIN-to-public file storage policy.
- Eleven `P2` findings affect request correctness, recovery, accessibility, stale state, and test ownership.
- Authenticated role, mutation, server-response, durable-projection, and responsive acceptance remain unknown until approved sessions and fixtures exist.
- Passing tests must not be treated as proof of missing or invalid contracts.

### Rollback

- No product/runtime rollback is required because this WI changed documentation only.
- If explicitly approved, remove only:
  - `deliverables/agent/WI-20260809-ATS-025-findings.md`
  - `deliverables/agent/WI-20260809-ATS-025-evidence-pack.md`
  - `deliverables/user/WI-20260809-ATS-025-summary.md`
- Remove `deliverables/agent/WI-20260809-ATS-025-handoff.md` only with separate explicit approval.
- Do not remove or alter product code, tests, configuration, database/storage state, prior WI evidence, or `output/client-demo-screenshots-20260716-140514.zip`.

## Follow-ups

- WI-025 blocks WI-030 according to the handoff. Remediation or acceptance planning should preserve the P1-first order in the findings document.
- Authenticated browser and durable mutation coverage require a separately approved session, fixtures, and operation scope.

---
version: 1.0
last_updated: 2026-09-05
project: ATS
owner: qa-integ
category: evidence-pack
status: draft
dependencies:
  - path: WI-20260905-ATS-001-handoff.md
    reason: Assigned scope and output contract
  - path: ../user/REQ-20260905-ATS-001.md
    reason: Approved release-verification closeout
  - path: ../user/REQ-20260823-ATS-001.md
    reason: Approved client-feedback behavior
  - path: ../user/REQ-20260818-ATS-002.md
    reason: Separate approval for the three HomePage strings
---

# Evidence Pack: WI-20260905-ATS-001

## Summary

TL;DR: All 40 tracked modifications are scope-eligible, but two P2 defects
prevent an unconditional client-change closeout. This is a current static
review, not a new application test or browser PASS. No product file was edited.

## Scope / DoD Check

- [x] Read every hunk of the 40 tracked-file diffs, with bounded caller/callee inspection.
- [x] Map the changes to both prior approved REQs and distinguish dated QA evidence.
- [x] Identify concrete defects, exact candidate paths, and an explicit exclusion rule.
- [x] Write only this evidence pack and the corresponding user summary.
- [ ] MA closes the defects and completes current application/browser verification.

Snapshot: `C:/Users/jm991/Desktop/project/ATStudio`, branch
`codex/v1-release-rehearsal-fixes`, HEAD
`69d0226a2656c82c8ecde4b6577c642dc42e12b2`. At
`2026-09-05T16:25:45+09:00`, there were 40 tracked modifications, 165 untracked
leaf paths, and zero staged paths. Peer-owned evidence may change concurrently.
The reviewed baseline is HEAD, not the older `3ea2781` QA baseline.

## Findings

### P2 F1: Likes action can close the drawer while Playlists is visible

- Parent: `frontend/src/layouts/PlayerBar.tsx:93-98` compares the requested
  target with `drawerTab`, which records the last parent request.
- Child: `frontend/src/components/player/PlaylistDrawer.tsx:69-77` owns a
  separate current tab; manual switches at `:724-738` update only that local
  state. There is no child-to-parent tab-change notification.
- Contract: `docs/ui/screen-flow.md:123-125` and
  `docs/ui/atstudio-front-list.md:157-160` say an explicit Likes action opens
  Likes. Backend counterpart: not applicable; the failure precedes an API call.

Static reproduction:

1. Open the drawer using PlayerBar's Likes action. Parent and child both hold `likes`.
2. Select Playlists inside the drawer. The child holds `playlists`; the parent still holds `likes`.
3. Click PlayerBar's Likes action again. `!(true && 'likes' === 'likes')`
   sets `playlistOpen` to false instead of displaying Likes. The reverse sequence
   affects the Playlists action too. This applies to the shared desktop/mobile handler.

The new request-ID effect cannot repair a drawer that the parent closes.
`PlayerBar.test.tsx:150-153` replaces the real drawer with a prop-only stub;
the added test at `:512-532` never exercises manual child-tab changes.
The separate drawer test covers generic reopen, not this parent/child path.
Keep same-visible-tab toggle behavior, but compare against the actual tab or
synchronize the tab state. MA should verify with the real drawer component.

### P2 F2: Frontend and backend trim different nickname whitespace

- Frontend: `frontend/src/utils/validation.ts:84-96` uses ECMAScript `trim()`
  before the nickname pattern/length check; `frontend/src/api/auth.ts:100-130`
  submits/checks that normalized value.
- Backend: `RegisterRequest.java:29-30`, `CompleteProfileRequest.java:27-28`,
  and `UpdateProfileRequest.java:21-22` under
  `src/main/java/com/atstudio/atstudio/dto/user/` use Java `strip()`.
  `src/main/java/com/atstudio/atstudio/service/UserService.java:425-426` repeats it.
- Contract: `docs/design/api-spec.md:581-586` requires consistent edge trimming
  before client/server validation, availability lookup, and persistence.

For `"\u00a0AT_M\u00a0"` (NBSP at both edges), the frontend normalizes to
`AT_M` and accepts it. Java `strip()` retains both NBSP characters; the DTO
`NICKNAME_PATTERN` rejects the same direct JSON input at the `@Valid` controller
boundary (`UserController.java:23-25,42-45,67-70`). The availability service also
receives a differently normalized lookup argument; no database collation outcome
is asserted here. Normal SPA submission already strips the input, so the defect
is API/client equivalence, not a claim that every browser signup fails.

Evidence is current source plus a tool-isolate language-primitive check, not an
HTTP test. Local JDK 17 source was read without extraction from
`C:/Program Files/Java/jdk-17/lib/src.zip`: `java.base/java/lang/String.java:3548-3569`
defines `strip()` through `Character.isWhitespace`; the latter's source at
`java.base/java/lang/Character.java:10874-10877` explicitly excludes U+00A0,
U+2007, and U+202F. The isolate evaluated NBSP-wrapped `AT_M` with `trim()` and
the current regex: result `AT_M`, accepted `true`.

Existing added tests cover ordinary U+0020 edges, not this boundary. Align the
accepted edge-whitespace set at both layers and test DTO deserialization/
validation plus availability normalization. Do not migrate historical records.

## Contract Review

| Area                      | Current static result        | Evidence and remaining limit                                                                                                                                                                                                                                                                                                                                        |
| ------------------------- | ---------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| BUSINESS descriptor / job | No remaining defect found    | Signup, social completion, and profile editing submit BUSINESS `job=null` and existing `companyName`; admin detail renders the same descriptor. Service guards at `UserService.java:122,451,495` reject newly supplied non-null BUSINESS jobs. Historical rows are intentionally not cleaned.                                                                       |
| Nickname                  | F2                           | All three DTO write paths, service checks, entity updates, and frontend input paths normalize ordinary spaces and preserve accepted internal spaces. Unicode edge equivalence is incomplete.                                                                                                                                                                        |
| Multiple Moods            | No defect found in diff      | `TrackListPage.tsx:707-723` renders registered/fallback Mood chips without result-derived pruning. The added test preserves repeated `mood` URL values. Other taxonomy availability logic is unchanged.                                                                                                                                                             |
| Playlist Play all         | No defect found in diff      | `PlaylistDetailPage.tsx:241-243` uses the existing store action; `playerStore.ts:699-705` replaces the queue and starts its first item. Backend `PlaylistService.java:108-114` uses ordered playable tracks. Existing page-context precedence, shuffle, repeat, and queue de-duplication policies remain unchanged. No new sequential navigation test was executed. |
| Likes entry               | F1                           | Both layouts expose the action, but parent/child manual-tab integration is missing.                                                                                                                                                                                                                                                                                 |
| HomePage copy / test      | No defect found in diff      | Three strings at `HomePage.tsx:298-306,511` match REQ-20260818-ATS-002. `HomePage.test.tsx:68-82` keeps full copy assertions with whitespace-tolerant matching; WI-008/009 supersede the older exact-text failure.                                                                                                                                                  |
| Question FAB              | No static defect established | Dedicated link, safe-area/player clearance, and expanded-player selector are present at `QuestionListPage.module.css:223-280`. The test only asserts link destination, not geometry. Actual desktop/mobile overlap and reachability remain MA browser checks.                                                                                                       |
| Safe configuration        | No defect found in diff      | `application-local.example.yml:69` changes only the explanatory keyring comment. No real local configuration or secret was read or changed.                                                                                                                                                                                                                         |

No changed hunk alters schema, data, plan capacities, default-playlist timing,
the three-state repeat contract, external billing/mail execution, or storage.
The existing visible-list-first next/previous policy is not reported as a new
Play-all defect and must not be silently redesigned in this closeout.

## Exact Stage Candidates

Paths below are relative to the verified workspace root. These are candidates
for MA's explicit final review, not instructions already executed and not a
claim that current tests passed. Keep the client-change set pending F1/F2
resolution; do not stage a broken partial dependency set merely to bypass them.

### A1: All 40 tracked candidates

The first 38 paths belong to REQ-20260823-ATS-001. The final two are the
separately approved HomePage copy, with its test repaired under WI-008.

```text
application-local.example.yml
docs/design/api-spec.md
docs/design/usecase/sound-playlist.md
docs/design/usecase/user-info.md
docs/ui/atstudio-front-list.md
docs/ui/screen-flow.md
frontend/src/api/auth.ts
frontend/src/components/player/PlaylistDrawer.tsx
frontend/src/components/player/playerComponents.test.tsx
frontend/src/layouts/PlayerBar.test.tsx
frontend/src/layouts/PlayerBar.tsx
frontend/src/pages/admin/UserManagePage.test.tsx
frontend/src/pages/admin/UserManagePage.tsx
frontend/src/pages/auth/SignupPage.test.tsx
frontend/src/pages/auth/SignupPage.tsx
frontend/src/pages/auth/SocialCompleteProfilePage.test.tsx
frontend/src/pages/auth/SocialCompleteProfilePage.tsx
frontend/src/pages/public/TrackListPage.test.tsx
frontend/src/pages/public/TrackListPage.tsx
frontend/src/pages/subscriber/PlaylistDetailPage.test.tsx
frontend/src/pages/subscriber/PlaylistDetailPage.tsx
frontend/src/pages/subscriber/ProfilePage.test.tsx
frontend/src/pages/subscriber/ProfilePage.tsx
frontend/src/pages/subscriber/QuestionListPage.module.css
frontend/src/pages/subscriber/QuestionListPage.test.tsx
frontend/src/pages/subscriber/QuestionListPage.tsx
frontend/src/test/coverage/publicAuthShell.coverage.test.tsx
frontend/src/utils/validation.ts
frontend/src/utils/validationHelpers.test.ts
src/main/java/com/atstudio/atstudio/common/validation/CompleteProfileValidator.java
src/main/java/com/atstudio/atstudio/common/validation/RegisterProfileValidator.java
src/main/java/com/atstudio/atstudio/common/validation/ValidationConstants.java
src/main/java/com/atstudio/atstudio/dto/user/CompleteProfileRequest.java
src/main/java/com/atstudio/atstudio/dto/user/RegisterRequest.java
src/main/java/com/atstudio/atstudio/dto/user/UpdateProfileRequest.java
src/main/java/com/atstudio/atstudio/entity/User.java
src/main/java/com/atstudio/atstudio/service/UserService.java
src/test/java/com/atstudio/atstudio/service/UserServiceTest.java
frontend/src/pages/public/HomePage.tsx
frontend/src/pages/public/HomePage.test.tsx
```

### A2: Prior provenance candidates, 32 paths

Keep these as dated history, including failures and superseded exclusions.
They are not current release evidence. Do not bulk-stage other old deliverables.
MA still owns final content/secret review of candidate artifacts.

```text
deliverables/user/REQ-20260818-ATS-002.md
deliverables/agent/WI-20260818-ATS-036-handoff.md
deliverables/agent/WI-20260818-ATS-036-evidence-pack.md
deliverables/user/WI-20260818-ATS-036-summary.md
deliverables/user/REQ-20260823-ATS-001.md
deliverables/agent/WI-20260823-ATS-001-handoff.md
deliverables/agent/WI-20260823-ATS-001-evidence-pack.md
deliverables/user/WI-20260823-ATS-001-summary.md
deliverables/agent/WI-20260823-ATS-002-handoff.md
deliverables/agent/WI-20260823-ATS-002-evidence-pack.md
deliverables/user/WI-20260823-ATS-002-summary.md
deliverables/agent/WI-20260823-ATS-003-handoff.md
deliverables/agent/WI-20260823-ATS-003-evidence-pack.md
deliverables/user/WI-20260823-ATS-003-summary.md
deliverables/agent/WI-20260823-ATS-004-handoff.md
deliverables/agent/WI-20260823-ATS-004-evidence-pack.md
deliverables/user/WI-20260823-ATS-004-summary.md
deliverables/agent/WI-20260823-ATS-005-handoff.md
deliverables/agent/WI-20260823-ATS-005-evidence-pack.md
deliverables/user/WI-20260823-ATS-005-summary.md
deliverables/agent/WI-20260823-ATS-006-handoff.md
deliverables/agent/WI-20260823-ATS-006-evidence-pack.md
deliverables/user/WI-20260823-ATS-006-summary.md
deliverables/agent/WI-20260823-ATS-007-handoff.md
deliverables/agent/WI-20260823-ATS-007-evidence-pack.md
deliverables/user/WI-20260823-ATS-007-summary.md
deliverables/agent/WI-20260823-ATS-008-handoff.md
deliverables/agent/WI-20260823-ATS-008-evidence-pack.md
deliverables/user/WI-20260823-ATS-008-summary.md
deliverables/agent/WI-20260823-ATS-009-handoff.md
deliverables/agent/WI-20260823-ATS-009-evidence-pack.md
deliverables/user/WI-20260823-ATS-009-summary.md
```

### A3: This review's provenance candidates, 4 paths

```text
deliverables/user/REQ-20260905-ATS-001.md
deliverables/agent/WI-20260905-ATS-001-handoff.md
deliverables/agent/WI-20260905-ATS-001-evidence-pack.md
deliverables/user/WI-20260905-ATS-001-summary.md
```

### Exclusions

- No tracked path in the 40-file snapshot is unrelated to the two approved
  prior REQs. Scope eligibility does not waive the findings or quality gates.
- Exact default exclusion: every path returned by
  `git ls-files --others --exclude-standard` that is absent from A2/A3.
  This deliberately excludes all other pending 20260809/16/17 records and all
  current peer artifacts until their owner reviews them. Do not use recursive
  staging of `deliverables/` or `git add .`.
- In particular, exclude `output/client-demo-screenshots-20260716-140514.zip`,
  every path under `output/ui-ux-audit/`, and every path under
  `scripts/database/patches/`. Their presence is not approval to ship them.
- `deliverables/agent/WI-20260905-ATS-002-handoff.md` and
  `deliverables/agent/WI-20260905-ATS-003-handoff.md`, plus their eventual
  outputs, belong to MA/peer closeout and are not certified by this WI.
- Ignored local configuration, credentials, logs, build output, storage/media
  fixtures, and every client-acceptance worktree path remain excluded even if
  a later command can force-add them. No historical media repair is authorized.

## Dated Evidence Versus Current Work

| Evidence                | What it establishes                                                                                                                | What it does not establish                                                                                                     |
| ----------------------- | ---------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------ |
| WI-20260818-ATS-036     | Approved HomePage copy and old static checks; focused Vitest blocked by `spawn EPERM`                                              | A current failure or a current test PASS                                                                                       |
| WI-20260823-ATS-004/005 | Historical exact-text HomePage failure; WI-005 also found BUSINESS-job acceptance                                                  | An unresolved current BUSINESS-job defect after WI-006                                                                         |
| WI-20260823-ATS-006/007 | Reported backend suite: 1,622 tests, zero failures/errors, 19 skipped; WI-007 independently ran three BUSINESS-job rejection tests | A fresh suite on HEAD `69d0226`; WI-007's HomePage/media exclusions were later superseded                                      |
| WI-20260823-ATS-008/009 | Repaired HomePage matcher; reported full frontend PASS: 111 files, 1,447 tests; ten scoped local media sets returned 200/206/200   | Current media availability, decoded playback/seek/reload, or production provisioning                                           |
| WI-20260905-ATS-001     | Full tracked diff review, current source tracing, and isolated trim primitive/source evidence                                      | Backend/frontend suite, application startup, HTTP, authenticated workflow, browser geometry, playback, or production readiness |

Dates for WI-20260823 evidence are its work-item date; no fresh execution is
implied by rereading the files. Older PASS verdicts do not cover F1/F2.

## Commands & Outputs

- Read-only commands: `git status --short --branch`, `git rev-parse HEAD`,
  `git diff --numstat`, `git diff -- <explicit path groups>`,
  `git diff --cached --name-only`, `git ls-files --others --exclude-standard`,
  and bounded `rg` / UTF-8 file reads. Every changed hunk was inspected.
- Local JDK source was read in-memory using `ZipFile.OpenRead`; no extraction,
  Java application, service, database, or browser was started.
- `python .agents/skills/validate-docs/scripts/validate_docs.py`: PASS, exit 0;
  required Tier 0 documents, internal links, 663 traceability IDs, and index.
- Scoped Prettier check initially requested table formatting in this evidence
  pack; only the owned report was formatted. Recheck of both reports: PASS,
  exit 0 (`All matched files use Prettier code style`).
- `git diff --no-index --check -- NUL <path>` for each owned report: no
  whitespace diagnostics; exit 1 denotes the new-file difference against NUL.
  Ordinary `git diff --check -- <path>` also exited 0 but skips untracked files.
- Parsed candidate manifests: 40 tracked + 32 prior provenance + 4 current
  provenance paths; zero differences from `git diff --name-only`, zero missing
  paths, and zero staged paths. Other untracked files remain excluded.

## Tests / MA Follow-up

No application tests, builds, runtime requests, or browser actions were run by
this WI. Recommended narrow checks, owned by MA:

1. Render PlayerBar with the real PlaylistDrawer and mocked read-only APIs.
   Exercise Likes -> manual Playlists -> Likes and the reverse sequence,
   preserving same-visible-tab close behavior and generic reopen retention.
2. Parameterize nickname edge normalization for U+0020, tabs/newlines, U+00A0,
   U+2007, and U+202F. Cover DTO setters/validation, service lookup arguments,
   canonical response/storage, and internal-space preservation with mocks.
   Existing `UserServiceTest` and frontend `validationHelpers.test.ts` are
   suitable narrow targets; no live signup or profile mutation is needed.
3. Use the real player store for playlist Play all followed by next/previous
   and non-starting Add all; retain existing list-context/shuffle/repeat policy.
   Existing `PlaylistDetailPage.test.tsx` currently checks a mocked call only.
4. In MA's browser pass, check the Question FAB at desktop/mobile sizes with
   the player collapsed/expanded and empty/loaded. Its current link test cannot
   establish CSS clearance. These visual limits are not additional defects.

## Reference Documents (Tier 0-2)

| Tier  | Document                                                                                               | Reason                                                   |
| ----- | ------------------------------------------------------------------------------------------------------ | -------------------------------------------------------- |
| 0     | `docs/standards/core-principles.md` (STD-001)                                                          | Injected constitution and approved boundaries            |
| 0     | `docs/standards/documentation-standards.md` (STD-004)                                                  | Injected metadata and two-set output rules               |
| 0     | `docs/standards/development-standards.md` (STD-002)                                                    | Injected backend/frontend and evidence rules             |
| 0     | `docs/standards/glossary.md` (STD-005)                                                                 | Injected canonical terminology                           |
| 1     | `docs/policies/security-policy.md`                                                                     | No secrets, mutation, or external actions                |
| 1     | `docs/policies/quality-gates.md`                                                                       | Evidence and closeout gates                              |
| 1     | `.claude/agents/qa-integ.md`                                                                           | Assigned integration-review role; no broad audit         |
| 2     | Both prior REQs and REQ-20260905-ATS-001                                                               | Approval, exact scope, and exclusions                    |
| 2     | WI-20260905-ATS-001 handoff; WI-20260823-ATS-007/009 summaries and evidence                            | Assigned output contract and dated final QA              |
| 2     | `docs/design/api-spec.md`, `docs/design/usecase/user-info.md`, `docs/design/usecase/sound-playlist.md` | API/profile/playlist behavior                            |
| 2     | `docs/ui/atstudio-front-list.md`, `docs/ui/screen-flow.md`                                             | Changed interaction contracts                            |
| Skill | `.agents/skills/create-wi-evidence-pack/SKILL.md`                                                      | Handoff existence checked; this two-set output generated |
| Skill | `.agents/skills/react-best-practices/SKILL.md`                                                         | Bounded React review guidance                            |
| Skill | `.agents/skills/lint/SKILL.md`, `.agents/skills/validate-docs/SKILL.md`                                | Documentation checks only                                |

Injection order: user-supplied Tier 0 -> assigned qa-integ role -> handoff/task
context -> current diff snapshot. No new subagent or handoff was created.

## Risks / Rollback

- Findings are source-level defects; no current end-to-end PASS is claimed.
- Only the two new WI-001 reports were written. Product changes, client
  worktree, database/schema, storage, processes, secrets, and Git index were
  not modified. Do not roll back any pre-existing dirty file.
- If this report needs correction, amend only these owned reports with
  `apply_patch`; no product rollback is necessary.
- Chain: this WI unblocks scope-aware work in WI-20260905-ATS-002. MA may
  continue non-mutating runtime/browser verification, but F1/F2 remain open
  closeout findings. WI-003 owns operational readiness; this WI does not close
  the overall REQ, delegate work, stage, commit, or approve release.

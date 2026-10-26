---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: QA-INTEG
category: evidence-pack
status: confirmed
dependencies:
  - path: WI-20261011-ATS-036-handoff.md
    reason: Approved local identity and integration-contract scope
  - path: ../user/REQ-20261011-ATS-006.md
    reason: Management-only trial approval and explicit exclusions
  - path: WI-20261011-ATS-020-artifact.json
    reason: Current backend source and executable pins
  - path: WI-20261010-ATS-026-artifact.json
    reason: Reusable env-independent frontend source and output pins
  - path: ../../build/WI036-web-contract/allowlist.json
    reason: Exact minimal API and SPA request contract
---

# Evidence Pack: WI-20261011-ATS-036

> Purpose: Independently match current local artifacts and source, then constrain the first anonymous browser trial without starting any application or modifying source.

## Summary

**TL;DR: PASS_LOCAL_IDENTITY_ONLY; six exact GET paths / 17 query sets derived; browser and target acceptance NOT RUN.** `GET /api/tags/available` with **no query** is required by Home and must be added to a five-path candidate allowlist. No `/api/tags/*` wildcard is needed. Operational prerequisites remain WI035/WI037/MA-owned. One existing P2 popular-sort mismatch is outside the empty-catalog acceptance claim.

Verification owner QA-INTEG, Windows/PowerShell local checkout, 2026-10-11. Initial identity assertion completed at **14:30:22 KST**; independent in-memory JAR resource comparison at **14:32:58 KST**. The [final identity receipt](../../build/WI036-web-contract/identity-result.json) records the later complete rerun time. These are observation times, distinct from document edit date. No remote state was read.

## Scope / DoD Check

- [x] Injected STD-001, STD-004, STD-002, STD-005, security policy, role, REQ006 and actual WI036 handoff checked.
- [x] Branch/HEAD, dirty worktree and exact backend input set/hash checked independently.
- [x] 641 backend inputs, one 70,306,351-byte JAR and both 641-record source receipts match.
- [x] 336 frontend inputs and all 143 output files match both applicable manifests; no extra/missing files in the reviewed enumeration.
- [x] Four named historical coverage exclusions match separately; they are not counted as historical build inputs.
- [x] Actual router/layout/home/catalog/plans/login-display calls traced through adapters, controllers and read paths.
- [x] Minimal method/path/query list, same-origin static surface, page matrix and denied-operation boundaries prepared.
- [x] Early results shared through normal commentary, including mandatory available-tags GET, exact raw queries, browser-context limitations and external-font prerequisite. No other chat messaged or spawned.
- [x] Both deliverable sets and reproducer created only in WI036-approved paths with apply_patch.
- [x] No build/install, regression suite, app/browser, DB/network/service, remote/private environment or source modification.

This is a bounded contract audit of the proposed surface, not an all-endpoint audit. WI035 owner implementation/regression work was neither run nor duplicated. REQ006 is not closed by this local result.

## Exact Identity

Branch `main`; HEAD `58b1011dad796017e47dbcc82a4225ca449bcdf6`. The dirty checkout is intentional and was preserved; HEAD alone does not describe packaged input bytes.

| Object | Bytes / count | SHA-256 |
|---|---:|---|
| `build/WI020-release-v3/build/libs/ATStudio-0.0.1-SNAPSHOT.jar` | 70,306,351 | `01d5623972e17d0b6e1d3e532096239cd87e9995a08771c59292c94bf3699dcb` |
| `WI-20261011-ATS-020-artifact.json` | 494,584 | `8a74e29f15708dad5a8278dffc3e8add1eb7b8dbcd6ddb18045ec85c32c63fe6` |
| `WI-20261010-ATS-026-artifact.json` | 483,979 | `fbeb74425ade0fe3882a9dddc4c97f7b67b75dda711ffa37acbd755a02fa4d9c` |
| Backend `source-before.json` and `source-after.json`, each | 132,201 / 641 | `d6e3db1888be322be314b1a351933df1130c441098e1e764042b5e9aa654dade` |
| `build/WI-20261010-ATS-026-frontend-dist`, file-set identity | 1,194,351 / 143 | `43a0384c5f48d100e4af6b20d989a2186b34e6900cf4e69c94b010e9671c8647` |
| Same SPA `index.html` | 613 | `4277c2c58975bcc4a2542fe9c40910e6e732e6fff2a5c9eee4c089a83c3d116b` |

Frontend aggregate algorithm: sort manifest records by path; concatenate `path + TAB + uppercase SHA256 + LF`; SHA-256 the UTF-8 bytes. This is a **file-set hash**, not an archive or single-file hash. Every recorded output size/hash and the actual path set were also compared. Backend source enumeration uses all files under `src` and `gradle` plus build.gradle/settings.gradle/gradlew/gradlew.bat, rejecting private filenames and reparse points. Frontend enumeration uses source/public trees and historical public root configuration inputs; private `.env` files are not opened or hashed. The four exact historical coverage-test exclusions are checked against WI020's separate pins.

The executable was opened as a ZIP **read-only in memory**, not run or extracted. Four packaged public resources match current source exactly; duplicate entries and private-named entries both zero. Resource hashes are in the receipt; packaged application.yml is `c3bd0e1571ffbe9e35ec5b77e6197fb91ca9baa602fb952609de4c07efd4bf55`. Filename checks are not general secret detection. Historical compilation provenance belongs to WI020, not a new reproducible-build claim.

Initial worktree snapshot: **360 dirty paths**: deliverables250, deploy42, docs20, scripts27, src15, output3, and root AGENTS/CLAUDE/README one each. Twelve tracked files were modified; five were backend inputs (AtStudioApplication, SecurityConfig, TrackAudioProcessingWorker, StorageMutationRecoveryService, application.yml). Ten untracked backend source/test files were already included in the matched 641-input inventory. Frontend dirty paths: zero. The receipt lists all tracked/backend dirty paths and grouped totals; unrelated paths were not edited. Concurrent WI035 changes may alter unrelated counts, so pin rechecks remain mandatory before staging.

## Minimal Cross-Layer Contract

[Machine-readable allowlist](../../build/WI036-web-contract/allowlist.json) contains **actual raw query strings**, parsed equivalents, six SPA base routes and the narrowly enumerated SPA query cases. Exact API paths only; deny every other path and method, including HEAD/OPTIONS. No query on available-tags or capabilities. An empty query on tracks/albums is not necessary and is not included.

| API | Frontend evidence | Backend evidence / effects |
|---|---|---|
| GET `/api/tracks` | `HomePage.tsx:86`; `TrackListPage.tsx:360-381`; `api/tracks.ts:65-83` | `TrackController.java:49-52`; `TrackService.java:108-147`: validate 1-based page, SELECT active Tracks through Specification, map DTO and PageInfo. Class readOnly at45. No save, counter, License or external-call path. |
| GET `/api/albums` | `HomePage.tsx:84-85`; `AlbumListImagePage.tsx:70-74`; `AlbumListPage.tsx:72-76`; `api/albums.ts:41-51` | `AlbumController.java:44-49`; `AlbumService.java:78-95`: active-album SELECT plus active membership counts and DTO projection. `AlbumRepository.java:16-28` and `AlbumTrackRepository.java:39-45,55-60`: SELECT-only paths; write-lock helpers are not called here. |
| GET `/api/tags` | `HomePage.tsx:113`; `TrackListPage.tsx:305,325-328`; `api/tags.ts:5-10` | `TagController.java:53-60`; `TagService.java:50-54`: findAll/findAllByType and response mapping. No mutation method called. |
| GET `/api/tags/available` | `HomePage.tsx:113` unconditionally calls `fetchAvailableTags({})`; `api/tags.ts:25-36` | `TagController.java:37-50`; `TagService.java:95-139`: parameterized SELECT DISTINCT on active Tracks/TrackTags/Tags. No query is needed for the Home request. |
| GET `/api/subscriptions` | `SubscriptionPlanPage.tsx:143,159-167,189,326-336`; `api/subscriptions.ts:21-30` | `SubscriptionController.java:22-28`; `SubscriptionService.java:23-37`: active plan reads, optional enum type filtering and DTO mapping. No payment or member subscription mutation. |
| GET `/api/utils/public-capabilities` | `LoginPage.tsx:92,114-117`; `hooks/usePublicCapabilities.ts:32-38`; `api/auth.ts:183-187` | `UtilController.java:27-32`; `UtilService.java:83-102,226-237`: project existing configuration into capability fields, no Provider/mail/network request. Store only boolean observation, not full config-derived payload. |

Paths above are relative to `frontend/src/` or `src/main/java/com/atstudio/atstudio/`. `frontend/src/api/client.ts:14-15` fixes API base `/api`. `SecurityConfig.java:70-81,116` permits these GET paths anonymously, but the trial edge must be stricter than the application's ordinary public surface. Anonymous JWT filtering passes without user lookup (`JwtAuthenticationFilter.java:27-31`); these six paths are not AuthRateLimitFilter's auth/availability targets (`AuthRateLimitFilter.java:109-154`). This is source-path evidence, not a DB write log or runtime isolation test.

Current API spec at `docs/design/api-spec.md:1028-1049` agrees with list envelopes and 1-based pagination. `common/dto/ResponseDTO.java:12-20` uses `dataList` or `data`; `common/dto/PageInfo.java:12-18,24-38` has no totalPages property. Frontend list pages read `dataList` and `pageInfo.total` and derive total pages. On an empty page1 the service produces start1/end0/prevfalse/nextfalse. Do not assert a fictitious raw Tag array or totalPages field.

## Findings and Trial Conditions

### F036-01 / P2: Popular Album Request Uses Latest Ordering

**Frontend:** `frontend/src/pages/public/HomePage.tsx:85,386` sends `sort=popular` for a section labelled popular; `frontend/src/api/albums.ts:31,45` accepts and forwards it. **Backend:** `src/main/java/com/atstudio/atstudio/service/AlbumService.java:82-84` handles only `trackCount` specially and routes every other value, including popular, to newest-first. The repository orders createdAt/id descending, not popularity (`repository/AlbumRepository.java:16-20`). A populated Home can label newest albums as popular. This pre-existing mismatch is observable statically but cannot be demonstrated with empty data. Preserve the request for Home compatibility; explicitly exclude popular-order acceptance. Source repair/definition requires a separate approved follow-up and is not WI035 work.

### F036-02 / P3: Injected Tag Exception Is Stale

Injected development-standards section2A.4 describes a raw-array Tag exception. Actual backend `TagController.java:54-60` returns `ResponseDTO.dataList`, and frontend `api/tags.ts:9-10` consumes that envelope; current `api-spec.md:1028-1030` also documents dataList. This is a governing-document discrepancy, not a broken FE/BE contract. Use the aligned current implementation/spec for trial assertions and defer standard reconciliation; no document outside WI036 outputs was modified.

### Conditions Required Before Browser Navigation

- **Context capability and conditional alternative:** MA subsequently reported existing in-app browser/new-tab capability only, without supported isolated/incognito creation. This is attributed operator context, not a WI036 browser observation. A new tab is not a fresh profile; pristine-profile acceptance is **HOLD**. The plan now permits only a WI037-reviewed retained-context fail-closed observation: edge rejects Cookie/Authorization before upstream, omits credential logging and blocks every excluded path. Do not inspect/clear hidden storage, log out or recover/strip credentials. `layouts/PlayerBar.tsx:112-114` / `store/playerStore.ts:471-498` may issue POST batch from stored queues; `api/tracks.ts:95-97` reaches `TrackController.java:55-62` / read-only `PlayableTrackService.java:20,29-44`, but hydration remains excluded. Stored accessToken also enables likes/member reads (`TrackListPage.tsx:290-294`, `PlayerBar.tsx:128-144`, `SubscriptionPlanPage.tsx:162-167`) and refresh (`api/client.ts:93-105,166-177`). Signed-in UI, credential denial or any auth/member/batch/media attempt means stop/HOLD. Even successful restricted reads prove only the observed existing-context behavior, not pristine storage. Unsupported request observability remains NOT VERIFIED.
- **Browser egress:** unchanged `frontend/index.html:6-9` / pinned dist index requests jsDelivr CSS plus preconnect. Server egress isolation does not prevent browser-origin requests. WI035/WI037 must prevent external traffic before parsing or HOLD browser execution. Fallback-font appearance is not a same-font visual proof.
- **GET is not a write-safety rule:** `frontend/src/api/notices.ts:18-19` reaches `NoticeService.java:86-91`, which increments viewCount. `frontend/src/api/downloads.ts:162-167` reaches `TrackController.java:109-113` / `DownloadService.java:43,73-84`, which can save download/License records and increment counts. Both are excluded, together with all detail/stream/attachment/admin/PII/auth/payment paths. Backend admission's generic safe-method set is not a substitute for this allowlist.
- **Source is not effective environment:** private config was not read. OFF login/social/bootstrap values, six actual plan rows, target hashes, management-line enforcement, deadline and cleanup must be confirmed by MA under reviewed execution scope. `seed.sql:17-22` is an expectation, not target DB evidence.

## Commands and Test Evidence

- `git --no-optional-locks status --short --branch`, `git rev-parse HEAD`, scoped `rg` and `Get-Content`: read-only source/manifest/contract inspection. Some guessed filenames were absent; actual paths were located via `rg --files`. No speculative files were created.
- `./build/WI036-web-contract/verify-identity.ps1`: local hash/set assertions, both manifest pins, backend inputs, frontend inputs/exclusions/output, exact artifact and public archive-resource comparisons. PASS, exit0. It writes no files, loads no environment file, invokes no build and outputs JSON only.
- [Identity receipt](../../build/WI036-web-contract/identity-result.json): captured stdout persisted with apply_patch. Counts for the two manifests overlap; do not sum them into unique file counts.
- [Output validation](../../build/WI036-web-contract/validation-result.json): PowerShell parser, JSON/raw-query consistency, required Markdown metadata/link targets/whitespace, and post-write identity rerun. These are static checks, not browser or application regression tests.
- Regression tests, coverage, typecheck/build, HTTP calls, DB queries, server services, browser render and real-device checks: **NOT RUN**. No logs from an earlier run are relabelled as new tests.

Minimal reproduction: from the repository root, run only the read-only identity script with PowerShell7; compare its output to the pinned receipt. Read [browser plan](../../build/WI036-web-contract/browser-plan.md) and [allowlist](../../build/WI036-web-contract/allowlist.json) without executing the trial. Any current pin or enumeration drift is a HOLD, not permission to rebuild or rewrite a manifest.

## Reference Documents (Tier 0-2)

| Tier | Reference | Use |
|---|---|---|
| 0 | STD-001 core-principles; STD-004 documentation-standards; STD-002 development-standards; STD-005 glossary | Fully injected, ordered governing context |
| 1 | security-policy; injected QA-INTEG role | Privacy, cross-layer pointers and two-set output |
| 2 | REQ006 and WI036 handoff | Narrow local authority and successor WI037 |
| 2 | WI020 artifact/evidence, WI026 frontend manifest, docs/index current entry, WI034 latest completion | Current identity and preservation of dated native success |
| 2 | Current router/layout/API/service/repository/DTO, api-spec, public seed | Actual request and nonmutating response paths |
| Skill | create-wi-evidence-pack; React best-practices entry | Evidence structure; bounded component/data-fetch reading without refactor |

Injection source: MA-provided Tier0 -> security -> persona -> REQ/handoff; reference rules at `.claude/config/context-injection-rules.json`; `.claude/config/workspace.json` confirms ATS. A quick memory keyword lookup yielded no task-specific evidence; all conclusions above come from current local files and supplied context.

## Risks / Rollback and Chain

Only five WI036-local support artifacts and these two reports were created. No runtime/source rollback is necessary. Preserve outputs and old evidence; withholding the plan/artifacts from trial is the rollback. Do not delete files, restore unrelated worktree changes, stage, commit or push.

Historical MA WI034 native BOUNDED_PASS/startup and cleanup remain dated evidence, not a fresh browser pass and not invalidated by this local audit. WI036 does not certify network enforcement, no-write behavior under arbitrary input, populated catalog UX, safe shutdown or production release.

**WI036 complete; Blocks WI037. MA should combine these outputs with WI035 completion and immediately prepare/delegate WI037 through the existing handoff skill.** Do not launch merely because identity matched. No child agent or chat was spawned from WI036.

## Related Documents

- [REQ006](../user/REQ-20261011-ATS-006.md): approved scope.
- [WI036 handoff](WI-20261011-ATS-036-handoff.md): delegated contract.
- [User summary](../user/WI-20261011-ATS-036-summary.md): concise result.
- [WI020 evidence](WI-20261011-ATS-020-evidence-pack.md): historical build provenance.
- [WI020 manifest](WI-20261011-ATS-020-artifact.json): current backend pins.
- [WI026 manifest](WI-20261010-ATS-026-artifact.json): retained frontend pins.
- [WI034 evidence](WI-20261011-ATS-034-evidence-pack.md): latest dated native result.
- [Browser plan](../../build/WI036-web-contract/browser-plan.md): unexecuted matrix.
- [Allowlist](../../build/WI036-web-contract/allowlist.json): exact requests.

# WI-20260715-ATS-015 Evidence Pack

## 1. Identification

| Field | Value |
|---|---|
| Work Item | `WI-20260715-ATS-015` |
| Role | ATStudio Quality Assurance |
| Date | 2026-07-15 |
| Current workspace | `C:\Users\jm991\Desktop\project\ATStudio` |
| Frozen baseline workspace | `C:\Users\jm991\Desktop\project\ATStudio-acceptance-preview` |
| Current branch | `codex/p1-acceptance-hardening` |
| Current HEAD | `08e081fd8a0944b0e30ebe03d0163ca57bf9b70a` |
| Frozen branch | `codex/acceptance-preview` |
| Frozen baseline | `b2172346f9c8202abe56ec44b458cd0a493fa232` |
| Follow-up verdict | **PASS** |
| WI-014 historical verdict | **FAIL, preserved unchanged** |
| Production readiness | **OPEN** |

## 2. Decision

WI-015 passes its approved follow-up acceptance contract:

1. The approved remediation contains no frontend delta from frozen baseline
   `b217234`, so the exact changed frontend source set is empty.
2. The changed-file Prettier gate is therefore N/A / PASS.
3. Both current and frozen full-tree Prettier checks fail on pre-existing files.
   The current 143-file set is a strict subset of the frozen 199-file set, with
   no current-only failure.
4. The required backend rerun succeeds with 986 tests and does not reproduce the
   first WI-014 Gradle executor completion failure.
5. WI-014's already-passing gates are reusable, and no new P0/P1 defect was found.

This verdict does not rewrite WI-014. Its historical FAIL remains valid because
the full-tree Prettier command used there exited 1 and its first backend attempt
ended with an executor completion failure. WI-015 is a separate scoped
adjudication authorized by its handoff.

## 3. Inputs And Policy Compliance

The following required pointers were read before execution:

- `AGENTS.md`
- `docs/standards/core-principles.md`
- `docs/standards/development-standards.md`
- `docs/standards/documentation-standards.md`
- `docs/standards/glossary.md`
- `docs/policies/quality-gates.md`
- `deliverables/user/REQ-20260714-ATS-001.md`
- `deliverables/agent/WI-20260715-ATS-015-handoff.md`
- `deliverables/agent/WI-20260715-ATS-014-handoff.md`
- `deliverables/agent/WI-20260715-ATS-014-evidence-pack.md`
- `deliverables/user/WI-20260715-ATS-014-summary.md`
- `deliverables/agent/WI-20260715-ATS-012-evidence-pack.md`
- `.agents/skills/prettier/SKILL.md`
- `.agents/skills/test/SKILL.md`
- `.agents/skills/create-wi-evidence-pack/SKILL.md`

Execution remained read-only except for the two authorized WI-015 output
documents. No frontend formatter was run in write mode. No product, existing
documentation corpus, schema, preview, or data file was changed. Live Toss was
not used. No file was staged or committed.

## 4. Frontend Delta And Scoped Gate

### 4.1 Required Git Comparisons

| Command | Exit | Duration | Count | Result |
|---|---:|---:|---:|---|
| `git diff --name-status b217234..HEAD -- frontend` | 0 | 52 ms | 0 | No baseline-to-current frontend delta |
| `git diff --name-only --diff-filter=ACMRT b217234..HEAD -- frontend/src` | 0 | 54 ms | 0 | Exact approved changed source set is empty |
| `git diff --name-only -- frontend` | 0 | 57 ms | 0 | No current tracked frontend diff |
| `git diff --cached --name-only -- frontend` | 0 | 57 ms | 0 | No current cached frontend diff |
| `git diff --name-status b217234..HEAD -- frontend/package.json frontend/package-lock.json frontend/.prettierrc` | 0 | 59 ms | 0 | No gate configuration delta |

Exact scoped frontend source set:

```text
[]
```

The scoped Prettier command was intentionally not broadened to the whole tree
when this list was empty. Result: **N/A / PASS**.

## 5. Full-Tree Prettier Adjudication

### 5.1 Same Command In Both Worktrees

Command:

```powershell
npm.cmd run format
```

The package script is identical in both worktrees:

```text
prettier --check "src/**/*.{ts,tsx,css}"
```

| Workspace | Exit | Duration | Failing files | Sorted set SHA-256 |
|---|---:|---:|---:|---|
| Current HEAD | 1 | 2.890 s | 143 | `E40837C3FE5B556BE8D062A870C3EE62184356236965FAF7ABA0FD2B4B1BFB9B` |
| Frozen `b217234` | 1 | 2.985 s | 199 | `BAEB1D7551AC83D28B892125C0B2BD72D3BB61BDF1A001DABAC9F43665AE7208` |

Set comparison:

| Relation | Count |
|---|---:|
| Current only | 0 |
| Frozen baseline only | 56 |
| Current set contained in baseline | 143 |

### 5.2 Configuration And Content Controls

- Installed Prettier version in both worktrees: `3.8.1`.
- `frontend/package-lock.json` SHA-256 is identical:
  `2704FD97C5993BAFD3D72BDCA55216BFAD7822F6558F10D9DC739D98F9D424D0`.
- `frontend/.prettierrc` SHA-256 is identical:
  `36A87144F4F00D8D5B009C4F0953E85AB4EA12FEF83EFE00D1E32FC772D8BFE0`.
- Both worktrees contain 203 matched frontend source files.
- 70 files are byte-identical.
- 133 files differ only by line endings.
- 0 files differ after CRLF-to-LF normalization.
- 106 files are frozen-CRLF/current-LF; the remaining EOL-only differences have
  mixed line-ending distributions.
- All 56 frozen-only Prettier failures are content-identical after normalization
  and are specifically frozen-CRLF/current-LF.

Raw source manifest hashes differ only because the checkouts have different line
endings:

- Current: `74D67646142B0822142658B94BDE0A98B8BE9B81D387D2F500E5B56E4AF344DE`
- Frozen: `ECD68C353AB29FF37A66E18DB6C0E1377319D3370E5E342894C382EEE871D462`

Conclusion: all 143 current full-tree failures existed at the frozen baseline.
The 143-file debt is non-blocking under WI-015's approved changed-file gate.

### 5.3 Exact Current 143-File Set

```text
src/api/albums.ts
src/api/auth.ts
src/api/client.ts
src/api/downloadQueue.ts
src/api/downloads.ts
src/api/licenses.ts
src/api/likes.ts
src/api/notices.ts
src/api/playHistory.ts
src/api/playlists.ts
src/api/questions.ts
src/api/settings.ts
src/api/subscriptions.ts
src/api/tags.ts
src/api/whitelistChannels.ts
src/App.tsx
src/components/album/AlbumCard.module.css
src/components/album/AlbumCard.tsx
src/components/filter/TagFilterModal.module.css
src/components/filter/TagFilterModal.tsx
src/components/player/HistoryModal.tsx
src/components/player/PlaylistDrawer.module.css
src/components/player/PlaylistDrawer.tsx
src/components/playlist/AddToPlaylistModal.module.css
src/components/playlist/AddToPlaylistModal.test.tsx
src/components/playlist/AddToPlaylistModal.tsx
src/components/track/TrackRow.module.css
src/components/track/TrackRow.tsx
src/components/ui/Badge.tsx
src/components/ui/Button.module.css
src/components/ui/Button.tsx
src/components/ui/DataTable.tsx
src/components/ui/FilterChip.module.css
src/components/ui/FilterChip.tsx
src/components/ui/Modal.module.css
src/components/ui/Modal.tsx
src/components/ui/Pagination.module.css
src/components/ui/Pagination.tsx
src/components/ui/Tag.module.css
src/components/ui/Tag.tsx
src/hooks/usePublicCapabilities.ts
src/layouts/AdminLayout.module.css
src/layouts/AdminLayout.tsx
src/layouts/Header.module.css
src/layouts/Header.tsx
src/layouts/MainLayout.tsx
src/layouts/PlayerBar.module.css
src/layouts/PlayerBar.tsx
src/main.tsx
src/pages/admin/DashboardPage.tsx
src/pages/admin/LicenseManagePage.tsx
src/pages/admin/NoticeCreatePage.module.css
src/pages/admin/NoticeCreatePage.tsx
src/pages/admin/NoticeEditPage.module.css
src/pages/admin/NoticeEditPage.tsx
src/pages/admin/QuestionManagePage.tsx
src/pages/admin/SiteSettingsPage.module.css
src/pages/admin/SubscriptionManagePage.tsx
src/pages/admin/TagManagePage.module.css
src/pages/admin/TagManagePage.tsx
src/pages/admin/TrackManagePage.module.css
src/pages/admin/TrackManagePage.tsx
src/pages/admin/UserManagePage.module.css
src/pages/admin/UserManagePage.tsx
src/pages/admin/UserSubscriptionManagePage.tsx
src/pages/admin/WhitelistChannelManagePage.tsx
src/pages/auth/EmailVerifyPage.tsx
src/pages/auth/LoginPage.module.css
src/pages/auth/LoginPage.test.tsx
src/pages/auth/LoginPage.tsx
src/pages/auth/PasswordResetPage.tsx
src/pages/auth/SignupPage.module.css
src/pages/auth/SignupPage.test.tsx
src/pages/auth/SignupPage.tsx
src/pages/auth/SocialCompleteProfilePage.test.tsx
src/pages/auth/SocialCompleteProfilePage.tsx
src/pages/auth/SocialLoginPage.test.tsx
src/pages/auth/SocialLoginPage.tsx
src/pages/creator/AlbumCreatePage.module.css
src/pages/creator/AlbumCreatePage.tsx
src/pages/creator/AlbumEditPage.module.css
src/pages/creator/AlbumEditPage.tsx
src/pages/creator/AlbumManagePage.module.css
src/pages/creator/AlbumManagePage.tsx
src/pages/creator/TrackEditPage.module.css
src/pages/creator/TrackEditPage.tsx
src/pages/creator/TrackUploadPage.module.css
src/pages/creator/TrackUploadPage.tsx
src/pages/error/ErrorPage.module.css
src/pages/error/NotFoundPage.tsx
src/pages/error/ServerErrorPage.tsx
src/pages/public/AlbumDetailPage.module.css
src/pages/public/AlbumDetailPage.tsx
src/pages/public/AlbumListImagePage.module.css
src/pages/public/AlbumListImagePage.tsx
src/pages/public/AlbumListPage.module.css
src/pages/public/AlbumListPage.tsx
src/pages/public/HomePage.module.css
src/pages/public/HomePage.tsx
src/pages/public/NoticeDetailPage.tsx
src/pages/public/NoticeListPage.module.css
src/pages/public/NoticeListPage.tsx
src/pages/public/SubscriptionPlanPage.module.css
src/pages/public/SubscriptionPlanPage.test.tsx
src/pages/public/SubscriptionPlanPage.tsx
src/pages/public/TrackDetailPage.module.css
src/pages/public/TrackDetailPage.tsx
src/pages/public/TrackListPage.module.css
src/pages/public/TrackListPage.tsx
src/pages/subscriber/DownloadQueuePage.test.tsx
src/pages/subscriber/DownloadQueuePage.tsx
src/pages/subscriber/LicenseDetailPage.tsx
src/pages/subscriber/LicenseListPage.tsx
src/pages/subscriber/LikeListPage.module.css
src/pages/subscriber/LikeListPage.tsx
src/pages/subscriber/PlayHistoryPage.module.css
src/pages/subscriber/PlayHistoryPage.tsx
src/pages/subscriber/PlaylistCreatePage.tsx
src/pages/subscriber/PlaylistDetailPage.module.css
src/pages/subscriber/PlaylistDetailPage.tsx
src/pages/subscriber/PlaylistEditPage.module.css
src/pages/subscriber/PlaylistEditPage.tsx
src/pages/subscriber/PlaylistListPage.module.css
src/pages/subscriber/PlaylistListPage.tsx
src/pages/subscriber/ProfilePage.module.css
src/pages/subscriber/ProfilePage.test.tsx
src/pages/subscriber/ProfilePage.tsx
src/pages/subscriber/QuestionCreatePage.tsx
src/pages/subscriber/QuestionDetailPage.tsx
src/pages/subscriber/QuestionListPage.module.css
src/pages/subscriber/QuestionListPage.tsx
src/pages/subscriber/SubscriptionPaymentPage.module.css
src/pages/subscriber/WhitelistChannelPage.tsx
src/router/index.tsx
src/router/SubscriberRoute.tsx
src/store/authStore.ts
src/store/likeStore.ts
src/store/playerStore.ts
src/store/themeStore.ts
src/styles/tokens.css
src/utils/format.ts
src/utils/tossPayments.ts
src/utils/validation.ts
```

### 5.4 Exact Frozen-Only 56-File Difference

```text
src/api/admin.ts
src/api/auth.test.ts
src/api/client.test.ts
src/api/companyCerts.ts
src/api/payments.ts
src/api/tracks.ts
src/api/userSubscriptions.ts
src/components/player/HistoryModal.module.css
src/components/player/WaveformCanvas.module.css
src/components/player/WaveformCanvas.tsx
src/components/ui/Badge.module.css
src/components/ui/ConfirmDialog.module.css
src/components/ui/ConfirmDialog.tsx
src/components/ui/DataTable.module.css
src/components/ui/ToastContainer.module.css
src/components/ui/ToastContainer.tsx
src/layouts/MainLayout.module.css
src/pages/admin/CompanyCertManagePage.module.css
src/pages/admin/CompanyCertManagePage.tsx
src/pages/admin/DashboardPage.module.css
src/pages/admin/LicenseManagePage.module.css
src/pages/admin/PaymentReadOnlyPage.module.css
src/pages/admin/PaymentReadOnlyPage.tsx
src/pages/admin/QuestionManagePage.module.css
src/pages/admin/SiteSettingsPage.tsx
src/pages/admin/SubscriptionManagePage.module.css
src/pages/admin/UserSubscriptionManagePage.module.css
src/pages/admin/WhitelistChannelManagePage.module.css
src/pages/auth/EmailVerifyPage.module.css
src/pages/auth/PasswordResetPage.module.css
src/pages/public/NoticeDetailPage.module.css
src/pages/subscriber/CompanyCertApplyPage.module.css
src/pages/subscriber/CompanyCertApplyPage.tsx
src/pages/subscriber/CompanyCertStatusPage.module.css
src/pages/subscriber/CompanyCertStatusPage.tsx
src/pages/subscriber/DownloadQueuePage.module.css
src/pages/subscriber/LicenseDetailPage.module.css
src/pages/subscriber/LicenseListPage.module.css
src/pages/subscriber/PlaylistListPage.test.tsx
src/pages/subscriber/QuestionCreatePage.module.css
src/pages/subscriber/QuestionDetailPage.module.css
src/pages/subscriber/SubscriptionManagePage.module.css
src/pages/subscriber/SubscriptionManagePage.test.tsx
src/pages/subscriber/SubscriptionManagePage.tsx
src/pages/subscriber/SubscriptionPaymentPage.test.tsx
src/pages/subscriber/SubscriptionPaymentPage.tsx
src/pages/subscriber/WhitelistChannelPage.module.css
src/router/ProtectedRoute.test.tsx
src/router/ProtectedRoute.tsx
src/router/SubscriberRoute.test.tsx
src/store/albumLikeStore.ts
src/store/authStore.test.ts
src/store/toastStore.ts
src/test/setup.ts
src/types/index.ts
src/utils/safeStorage.ts
```

## 6. Backend Stacktrace Rerun

Precondition immediately before execution:

- Current tracked diff count: 0.
- Current cached diff count: 0.

Required command, executed exactly once:

```powershell
.\gradlew.bat test --rerun-tasks --stacktrace
```

| Metric | Result |
|---|---|
| Process exit | 0 |
| Wall duration | 90.352 s |
| Gradle result | `BUILD SUCCESSFUL in 1m 30s` |
| Tasks | 5 actionable, 5 executed |
| XML suites | 138 |
| Tests | 986 |
| Failures | 0 |
| Errors | 0 |
| Skipped | 9 |
| Summed suite time | 74.641 s |
| `Gradle Test Executor 13` recurrence | No |

Skipped suites:

| Suite | Tests | Skipped | Reason |
|---|---:|---:|---|
| `PaymentMysqlConcurrencyIntegrationTest` | 7 | 7 | Opt-in disposable-MySQL proof; executed and passed by WI-007, with its authoritative evidence reviewed by WI-014 |
| `PaymentMysqlSchemaValidationTest` | 1 | 1 | Opt-in MySQL validate gate; executed and passed by WI-007, with its authoritative evidence reviewed by WI-014 |
| `LocalStorageServiceTest` | 4 | 1 | Symbolic-link capability unavailable in this Windows environment |

Compiler output retained warnings for unchecked/unsafe Java operations, and the
test JVM retained its class-data-sharing warning. Neither warning failed the
build. No executor completion exception or actionable stacktrace was emitted.

## 7. Reused WI-014 PASS Results And Reviewed Evidence

Per the WI-015 handoff, these WI-014 PASS results and reviewed evidence were
reused without rerunning their underlying gates:

| Gate | Command / evidence | Exit | Duration / count | WI-014 result |
|---|---|---:|---|---|
| Frontend typecheck | `npm.cmd run typecheck` | 0 | 4.960 s | PASS |
| Frontend ESLint | `npm.cmd run lint` | 0 | 3.318 s; 0 warnings | PASS |
| Frontend tests | `npm.cmd test -- --run` | 0 | 7.409 s; 17 files, 69 tests | PASS |
| Frontend build | `npm.cmd run build` | 0 | 7.887 s; 259 modules | PASS |
| Documentation validation | project docs validator | 0 | Final WI-014 validation; 373 IDs, 0 warnings | PASS |
| Whitespace validation | `git diff --check` | 0 | 0.094 s | PASS |
| MySQL schema/validate/races/drop | WI-007 execution evidence reviewed by WI-014 | 0 | WI-007: 97.6 s runner; 7 races, 0 fail/error/skip, 17.051 s suite | PASS |
| Public preview root | frozen public preview smoke | 0 | HTTP 200 | PASS |
| Public preview API | frozen public `/api/tracks` smoke | 0 | HTTP 200 | PASS |
| Independent review | WI-012 evidence | N/A | No P0/P1 | PASS |

WI-007 executed and passed schema setup, Hibernate validation, seven race
scenarios, schema drop, and cleanup count 0; WI-014 reviewed that authoritative
evidence. The public preview remained the clean frozen baseline and did not use
live Toss.

## 8. Preservation And Changed Paths

Before creating WI-015 outputs:

- Current tracked diff count: 0.
- Current cached diff count: 0.
- Current frontend tracked/cached diff counts: 0 / 0.
- Frozen preview status count: 0.
- Frozen preview HEAD:
  `b2172346f9c8202abe56ec44b458cd0a493fa232`.

Preserved hashes:

| Path | Bytes | SHA-256 |
|---|---:|---|
| `deliverables/agent/WI-20260715-ATS-014-handoff.md` | 4,508 | `224727A00C02D103FD66EB3A31987495D952BD2C04350247CA227100971C9DBA` |
| `deliverables/agent/WI-20260715-ATS-014-evidence-pack.md` | 12,333 | `C05D362A1B22D0664087B71DEE9A90D1C4644647FE163757B4B3120D13B0DF2E` |
| `deliverables/user/WI-20260715-ATS-014-summary.md` | 4,995 | `430DD167F4936844FF0D7B8610BE6F670569ECADE206DDECE56C6610CF3CA141` |
| `cloudflared.err.log` | 3,953 | `A68249173CE7757C2F35764150D7B65B01830EE1DA1A8417532E57AA0289A7C1` |
| `cloudflared.out.log` | 0 | `E3B0C44298FC1C149AFBF4C8996FB92427AE41E4649B934CA495991B7852B855` |
| `frontend/vite.err.log` | 0 | `E3B0C44298FC1C149AFBF4C8996FB92427AE41E4649B934CA495991B7852B855` |
| `frontend/vite.out.log` | 296 | `E5EAB8DD1F7E6EDBE624E0B30F3B1A7C055A5B8B41DF31D8C75A679C444FE96A` |

Only these paths were created by WI-015:

```text
deliverables/agent/WI-20260715-ATS-015-evidence-pack.md
deliverables/user/WI-20260715-ATS-015-summary.md
```

No tracked or cached path was changed. No frontend file was formatted or written.

## 9. Residual Risks

| Risk | Severity | Disposition |
|---|---|---|
| 143 current frontend source files fail the full-tree Prettier check | Known debt | Non-blocking for WI-015; requires separately approved cleanup |
| Original WI-014 Gradle executor completion failure is not root-caused | Low / intermittent | Did not recur in the one mandated stacktrace rerun; monitor future full-suite runs |
| Nine tests are skipped in the default backend suite | Known limitation | WI-007 executed and passed the eight MySQL-gated tests, and WI-014 reviewed that authoritative evidence; one test depends on symbolic-link support |
| Unknown-cancel rendered-log path lacks a test appender assertion | P3 | Retained from WI-012; non-blocking |
| Public preview evidence and WI-007 disposable-MySQL execution evidence reviewed by WI-014 are point-in-time results | Operational | Not rerun under WI-015 by instruction |
| Production readiness has no final owner decision | Open decision | Keep readiness open |

## 10. Reproduction Commands

```powershell
# Frontend scope
git diff --name-status b217234..HEAD -- frontend
git diff --name-only --diff-filter=ACMRT b217234..HEAD -- frontend/src
git diff --name-only -- frontend
git diff --cached --name-only -- frontend

# Same full-tree observation in each worktree
Set-Location C:\Users\jm991\Desktop\project\ATStudio\frontend
npm.cmd run format
Set-Location C:\Users\jm991\Desktop\project\ATStudio-acceptance-preview\frontend
npm.cmd run format

# Backend stacktrace rerun
Set-Location C:\Users\jm991\Desktop\project\ATStudio
.\gradlew.bat test --rerun-tasks --stacktrace

# Final read-only integrity checks
git diff --check
git status --short
git -C C:\Users\jm991\Desktop\project\ATStudio-acceptance-preview status --short
```

## 11. Rollback

No product rollback is applicable because WI-015 made no product change. If the
two report artifacts must be withdrawn, remove only the two WI-015 output files
after obtaining the required approval for deletion. Do not alter WI-014 artifacts
or the four preserved runtime logs.

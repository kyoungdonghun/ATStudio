---
version: 1.0
last_updated: 2026-08-13
project: ATS
owner: qa-integ
category: audit
status: confirmed
dependencies:
  - path: WI-20260809-ATS-049-qa-integ-review-handoff.md
    reason: Independent review scope and output contract
  - path: WI-20260809-ATS-049-handoff.md
    reason: Approved implementation contract
---

# Independent QA-INTEG Review: WI-20260809-ATS-049

## Findings

No P0 or P1 finding was identified. Four P2 findings block acceptance. No P3
finding is needed to determine the verdict.

### QA-049-001 - Reorder recovery retry can falsely claim a rejected mutation committed

- Severity: `P2`
- Contract: Failure before commit must remain safely recoverable, and UI text
  must distinguish a committed mutation from an unconfirmed or rejected one.
- Evidence: `frontend/src/pages/creator/AlbumEditPage.tsx:323-328` correctly
  calls `refetchTracks(false)` after reorder rejection, but the shared retry at
  `frontend/src/pages/creator/AlbumEditPage.tsx:557-563` always calls
  `refetchTracks(true)`.
- Exact counterexample: make `reorderAlbumTracks(11, ...)` reject, make its
  recovery `fetchAlbumDetail(11)` reject, click `트랙 목록 다시 불러오기`, and
  make that read reject again. The first read displays the neutral
  `변경 결과와 최신 트랙 목록을 확인하지 못했습니다.` state. The retry changes
  it to `변경은 완료되었지만 최신 트랙 목록을 불러오지 못했습니다.`, even though
  the mutation returned rejection and no commit was established.
- Remediation expectation: retain recovery provenance (`committed` versus
  `unknown/rejected`) across read retries and never upgrade an unproved result
  to committed solely because another read failed. Add the exact
  rejection -> recovery failure -> retry failure test.

### QA-049-002 - The shared thumbnail field invents a filename-extension policy

- Severity: `P2`
- Contract: Album thumbnail entry points must match the existing backend
  JPEG/PNG input contract and must not invent a new file-format policy.
- Evidence: `frontend/src/pages/creator/AlbumThumbnailField.tsx:27-44` rejects
  unless the filename ends in `.jpg`, `.jpeg`, or `.png`. The authoritative
  server determines JPEG/PNG from byte signature and checks an optional MIME at
  `src/main/java/com/atstudio/atstudio/service/image/CanonicalImageService.java:75-98`;
  it does not inspect the filename extension. The current-state claim in
  `docs/design/usecase/sound-album.md:29-35` says all three entry points use the
  backend JPEG/PNG contract.
- Exact counterexample: select valid JPEG bytes as
  `new File([validJpegBytes], "cover", { type: "image/jpeg" })`. The browser can
  decode the JPEG and the backend signature/MIME contract accepts it, but
  `getFormatError` rejects it before decode or API invocation because the name
  has no extension.
- Remediation expectation: keep the picker hint advisory and validate only
  constraints supported by the current contract, leaving signature authority
  to the backend. Add extensionless valid JPEG/PNG and exact MIME/decode
  counterexamples.

### QA-049-003 - Combobox keyboard and blur behavior is incomplete

- Severity: `P2`
- Contract: The mandatory attack matrix requires Arrow, Home, End, Enter,
  Escape, and blur behavior with correct combobox/listbox/option ownership.
- Evidence: `frontend/src/pages/creator/AlbumEditPage.tsx:229-261` handles
  Arrow Up/Down, Enter, Escape, and Tab but has no `Home` or `End` branch.
  `frontend/src/pages/creator/AlbumEditPage.tsx:128-144` closes only for an
  outside `mousedown`; the combobox has no focus-out/blur ownership.
- Exact counterexample: open three results, activate the second result, then
  press `Home` or `End`. `aria-activedescendant` remains on the second result
  instead of moving to the first or last option. Blurring without an outside
  mouse event also leaves `aria-expanded=true` and the popup rendered.
- Remediation expectation: implement Home/End navigation and a focus-aware
  blur/focus-out dismissal that still permits pointer option selection. Add
  assertions for active option, popup state, and selection after each key.

### QA-049-004 - Current members are rendered as results and become re-addable after refresh failure

- Severity: `P2`
- Contract: The mandatory search matrix requires duplicate/current-member
  exclusion, and post-mutation refresh recovery must not expose a duplicate
  mutation path from stale membership state.
- Evidence: `frontend/src/pages/creator/AlbumEditPage.tsx:503-530` maps every
  API result and merely sets current members to `aria-disabled`; it does not
  exclude them. Availability is derived only from the possibly stale `tracks`
  array at lines 504 and 516-518.
- Exact counterexample: return Track `21`, already present in the loaded Album,
  from `fetchTracks`; it remains a visible `role=option` instead of being
  excluded. More materially, let add of Track `42` commit and let the following
  Album-detail refresh fail. Because `tracks` never acquires `42`, searching
  again renders `42` enabled and permits a second `addTrackToAlbum(11, 42)`.
- Remediation expectation: filter current members from results and preserve a
  local committed-membership fence, or disable further membership mutations
  until the authoritative refresh succeeds. Prove both initial-member and
  committed-but-refresh-failed duplicate exclusion.

## Verdict

`FAIL`

The four P2 counterexamples contradict mandatory WI-049 behavior despite the
green focused suite. The patch should not proceed to final full gates or commit
until they are corrected and independently retested.

## Independent Test Results

| Command | Result |
|---|---|
| `npm test -- --run src/pages/creator/AlbumCreatePage.test.tsx src/pages/creator/AlbumEditPage.test.tsx src/pages/creator/AlbumManagePage.test.tsx src/pages/creator/AlbumThumbnailField.test.tsx src/pages/public/AlbumDetailPage.test.tsx src/pages/public/AlbumListPages.test.tsx src/api/domainApis.test.ts src/test/coverage/publicAuthShell.coverage.test.tsx` | PASS: 8 files, 81 tests, 0 failed; 10.82 s |
| `.\gradlew.bat test --tests "com.atstudio.atstudio.service.AlbumServiceTest" --rerun-tasks` | PASS: 20 tests, 0 skipped, 0 failures, 0 errors; forced execution, Gradle build successful |
| `npm run typecheck` | PASS |
| `npm run lint` | PASS with `--max-warnings 0` |
| `npm run build` | PASS: Vite 6.4.3, 289 modules transformed |

The first backend invocation was `UP-TO-DATE`; it was not used as independent
execution authority. The `--rerun-tasks` result and generated XML at
`build/test-results/test/TEST-com.atstudio.atstudio.service.AlbumServiceTest.xml`
are the authoritative backend result.

## Residual Risks And Deferrals

- The green frontend tests do not exercise the four counterexamples above;
  therefore their pass result does not establish the mandatory contract.
- No live ADMIN browser, real media upload, live backend, database, storage,
  Provider, mail, payment, download, or other external effect was invoked.
- No full repository suite, coverage run, Prettier check, or documentation
  validator was independently started in this review checkpoint. The recorded
  evidence-pack claims for those gates were not treated as authority.
- Browser image validation remains advisory and does not prove signature,
  APNG, canonical JPEG, storage, or durable-state behavior.
- WI-059 public Album semantics and WI-070 broad screen inventory remain
  intentional deferrals and were not promoted to WI-049 defects.
- Protected output paths and ignored secret/local-environment values were not
  inspected. No production, test, or current-state document was modified, and
  no Git staging, commit, push, or branch action was performed.

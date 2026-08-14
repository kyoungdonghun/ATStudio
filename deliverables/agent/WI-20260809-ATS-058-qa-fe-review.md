---
version: 1.0
last_updated: 2026-08-14
project: ATS
owner: qa-fe
category: wi-review
status: fail
wi: WI-20260809-ATS-058
---

# WI-20260809-ATS-058 Independent QA-FE Review

## Verdict

**FAIL** - one open P2 test-evidence gap remains. No P0, P1, or P3 finding
was identified in the reviewed unstaged WI-058 diff.

## Finding

### P2 - FAIL: Focus-trap and focus-return claims are not covered by the new test

- **Evidence:** `frontend/src/components/player/playerComponents.test.tsx:759`
  dispatches only `Escape`. The test at
  `frontend/src/components/player/playerComponents.test.tsx:750` has no
  assertion for `Tab`, `Shift+Tab`, focus containment, or focus restoration.
- **Affected implementation:** `frontend/src/components/player/PlaylistDrawer.tsx:180`
  through `frontend/src/components/player/PlaylistDrawer.tsx:220` implements
  those paths, while `docs/standards/frontend-standards.md:611` states that
  they are supported and non-mutating.
- **Impact:** The passing Escape assertion proves only that one dismissal path
  does not invoke playlist mutation APIs. It does not prove the claimed
  Tab/Shift+Tab trap or connected-opener return behavior, nor that those
  keyboard paths remain free of create, delete, remove, and reorder dispatch.
- **Required resolution:** Add focused tests for forward Tab wrapping, reverse
  Shift+Tab wrapping, and valid-opener restoration after close. Each keyboard
  path must assert that `createPlaylist`, `deletePlaylist`,
  `removeTrackFromPlaylist`, and `reorderTracks` were not called.

## PASS Controls

| Review area | Result | Evidence |
| --- | --- | --- |
| CR-031-040 Escape dismissal and selected-tab semantics | PASS, limited to the tested Escape path | `frontend/src/components/player/playerComponents.test.tsx:754`, `frontend/src/components/player/playerComponents.test.tsx:759` |
| CR-031-062 native controls, labels, selected state, and tag retry | PASS | `frontend/src/pages/creator/TrackUploadPage.tsx:365`, `frontend/src/pages/creator/TrackUploadPage.tsx:526`, `frontend/src/pages/creator/TrackUploadPage.test.tsx:114` |
| API request shapes and business-state transitions | PASS by unstaged diff inspection | Changed retry controls update local retry state only; no changed API argument or mutation transition was found. See `frontend/src/pages/public/TrackListPage.tsx:657` and `frontend/src/pages/creator/TrackUploadPage.tsx:529`. |
| CR-031-080 Korean loading and operational wording | PASS | Changed strings describe local load/status surfaces only, including `frontend/src/pages/admin/CompanyCertManagePage.tsx:270` and `frontend/src/pages/admin/WhitelistChannelManagePage.tsx:390`. |
| CR-031-090 subscription selection/status copy | PASS | Selection state is semantic-only and payment-status wording introduces no billing promise: `frontend/src/pages/public/SubscriptionPlanPage.tsx:324`, `frontend/src/pages/public/SubscriptionPlanPage.tsx:354`, `frontend/src/pages/subscriber/SubscriptionPaymentPage.tsx:641`. |
| Executed focused regression | PASS | `npm test -- src/components/basicComponents.test.tsx src/components/catalogComponents.test.tsx src/components/player/playerComponents.test.tsx src/pages/public/TrackListPage.test.tsx src/pages/auth/SignupPage.test.tsx src/pages/creator/TrackUploadPage.test.tsx src/pages/public/SubscriptionPlanPage.test.tsx src/pages/admin/CompanyCertManagePage.test.tsx src/pages/admin/WhitelistChannelManagePage.render.test.tsx src/pages/subscriber/SubscriptionPaymentPage.test.tsx --reporter=dot`: 10 files, 195 tests passed. |
| Type and whitespace checks | PASS | `npm run typecheck` passed; `git diff --check` passed. |

## Review Boundary

- Reviewed the requested handoff, implementation result, consolidated
  findings, Tier 0/quality/frontend standards, React guidance, and all
  unstaged WI-058 changes.
- Did not inspect `output/client-demo-screenshots-20260716-140514.zip` or
  `output/ui-ux-audit/`.
- No API, Provider, authentication, payment, mail, download/export, database,
  stage, commit, push, or source-code action was performed.

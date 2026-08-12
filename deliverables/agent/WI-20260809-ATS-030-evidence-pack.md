# Evidence Pack: WI-20260809-ATS-030

## Summary (one-liner)

- Completed the bounded cross-entry frontend audit for shared playback, Track/image/search, subscription, Question, Playlist/download, auth, dialog, routing, keyboard, and responsive behavior; recorded 12 independent defects (`P1` 1, `P2` 11), passing scoped automation, a limited safe anonymous `1280x720` browser pass, and blocked/unproven lanes without product or state mutation.

## Scope / DoD Check

- DoD items:
  - [x] Reconciled `INV-PLAY`, `INV-TRACK`, `INV-IMAGE`, `INV-SEARCH`, `INV-SUB`, `INV-QUESTION`, and the named shared auth/Playlist/download/dialog/routing roots across their inventoried entry points.
  - [x] Classified base, `R`, `K`, and `V` outcomes separately and retained `PASS`, `FAIL`, `BLOCKED`, and `NOT RUN` boundaries.
  - [x] Kept UI/DOM, frontend invocation, server response, and durable-state evidence separate; no browser or mock result was promoted to server/durable proof.
  - [x] Recorded 12 independent findings: `P1` 1, `P2` 11; `NEW` 11 and `ADJACENT-REGRESSION` 1.
  - [x] Reconciled prior WI-021 through WI-029 owners without reissuing their causes under new WI-030 IDs.
  - [x] Recorded main-supplied scoped Vitest, typecheck, ESLint, and build results with their reproducibility and isolation limits.
  - [x] Recorded the safe anonymous `1280x720` routes, redirects, reload/Back/Forward, recovery behavior, DOM defects, and exact cleanup state.
  - [x] Kept live `1440x900`, `1024x768`, `390x844`, and `360x800` evidence `BLOCKED`; console evidence remains unavailable/non-authoritative.
  - [x] Preserved the no-auth/private/media/file/mutation/direct-API/DB/Git-state-change boundary and left the intentional ZIP untouched and uninspected.
  - [x] Extracted seven unresolved policy questions for WI-031 without choosing outcomes.
  - [x] Created the required Evidence Pack and Korean user summary at the exact output paths.
  - [x] Completed final output-document Prettier write/check, applicable docs validation, and post-output `git diff --check` with the exact results and tracked/untracked boundary recorded below.

## Reference Documents (Tier 0-2)

**Injected Context** (from WI Handoff Packet):

| Tier | Document                                        | Reason                                                                     |
| ---- | ----------------------------------------------- | -------------------------------------------------------------------------- |
| 0    | `docs/standards/core-principles.md`             | Constitution and evidence-over-inference boundary required by the handoff. |
| 0    | `docs/standards/development-standards.md`       | Frontend source/test and frozen-product verification boundary for qa-fe.   |
| 1    | `docs/policies/security-policy.md`              | Auth, secret, private-state, and prohibited-inspection boundary.           |
| 1    | `docs/policies/access-control-policy.md`        | Anonymous, USER, subscriber, creator, and ADMIN route/capability boundary. |
| 1    | `docs/policies/quality-gates.md`                | Evidence quality and result-classification gates.                          |
| 2    | `docs/standards/frontend-standards.md`          | React interaction, accessibility, request-state, and responsive contract.  |
| 2    | `.agents/skills/react-best-practices/AGENTS.md` | React review context explicitly named by the handoff.                      |
| 2    | `docs/design/api-spec.md`                       | Frontend/API route and response contract.                                  |
| 2    | `docs/ui/screen-flow.md`                        | Named public, member, creator, and ADMIN entry-point context.              |

**Injection Rules Applied**:

- Rule source pointer: `.claude/config/context-injection-rules.json` (project-governance pointer; not re-read during this closeout).
- Assignee: `qa-fe`.
- Task type: cross-entry frontend source/assertion/browser audit.
- `agent_required_tiers`: `[0, 1]`; Tier 2 frontend/API/UI contracts were explicitly injected by the handoff.
- Handoff/REQ context: `deliverables/agent/WI-20260809-ATS-030-handoff.md:1-90`, `deliverables/agent/WI-20260809-ATS-030-handoff.md:175-256`, `deliverables/user/REQ-20260809-ATS-001.md:1-120`, and the acceptance-matrix ranges listed at handoff lines 197-204.
- WI-029 Evidence Pack and summary were read only as formatting examples for this closeout, not as substitute WI-030 evidence.

## Evidence Pointers (required)

- Files changed:
  - `deliverables/agent/WI-20260809-ATS-030-evidence-pack.md` - reproducible agent-facing closeout evidence.
  - `deliverables/user/WI-20260809-ATS-030-summary.md` - Korean user-facing outcome, boundaries, decisions, and next-WI gate.
- Primary evidence remains unchanged:
  - `deliverables/agent/WI-20260809-ATS-030-findings.md:3-17` - execution, frozen-state, browser, and completion boundaries.
  - `deliverables/agent/WI-20260809-ATS-030-findings.md:19-35` - actual entry-point and shared-root inventory.
  - `deliverables/agent/WI-20260809-ATS-030-findings.md:39-171` - 12 independent findings with source/assertion/browser lane analysis.
  - `deliverables/agent/WI-20260809-ATS-030-findings.md:173-196` - prior-owner reconciliation.
  - `deliverables/agent/WI-20260809-ATS-030-findings.md:248-287` - `R`/`K`/`V` and four-lane status matrices.
  - `deliverables/agent/WI-20260809-ATS-030-findings.md:289-305` - existing assertion audit and uncovered cases.
  - `deliverables/agent/WI-20260809-ATS-030-findings.md:307-315` - seven unresolved policy questions.
  - `deliverables/agent/WI-20260809-ATS-030-findings.md:317-372` - supplied automated verification and scoped command inventory.
  - `deliverables/agent/WI-20260809-ATS-030-findings.md:375-386` - safe anonymous browser evidence and blocked-width/console boundaries.
  - `deliverables/agent/WI-20260809-ATS-030-findings.md:388-406` - final source-audit boundary, counts, and verification summary.

### Finding Register

| ID               | Severity / classification    | Independent cause                                                                                           | Primary source / browser pointer                                                                                                                                                           |
| ---------------- | ---------------------------- | ----------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| `F-QAFE-030-001` | `P2` / `NEW`                 | Global playback shortcuts intercept focused controls outside text inputs.                                   | `frontend/src/layouts/MainLayout.tsx:13-49`; findings lines 39-49.                                                                                                                         |
| `F-QAFE-030-002` | `P1` / `NEW`                 | Queued concurrent `401` replays are not marked retried and can refresh again.                               | `frontend/src/api/client.ts:100-149`; findings lines 51-61.                                                                                                                                |
| `F-QAFE-030-003` | `P2` / `NEW`                 | Central `401` fallback loses or inconsistently navigates to a validated origin.                             | `frontend/src/api/client.ts:100-149`; `frontend/src/utils/oauthAttempt.ts:38-83`; findings lines 63-72.                                                                                    |
| `F-QAFE-030-004` | `P2` / `NEW`                 | Header and ADMIN logout callers navigate before the logout transaction settles.                             | `frontend/src/layouts/Header.tsx:189-192,300-303`; `frontend/src/layouts/AdminLayout.tsx:51-59`; findings lines 74-83.                                                                     |
| `F-QAFE-030-005` | `P2` / `NEW`                 | Shared image renderers have no failed-load fallback for nonempty broken URLs.                               | `frontend/src/api/client.ts:202-205`; representative renderers listed at findings lines 85-95.                                                                                             |
| `F-QAFE-030-006` | `P2` / `ADJACENT-REGRESSION` | ADMIN mobile navigation lacks equivalent Escape, focus, and hidden-tree ownership.                          | `frontend/src/layouts/AdminLayout.tsx:51-59,95-119`; `frontend/src/layouts/AdminLayout.module.css:190-227`; findings lines 97-105.                                                         |
| `F-QAFE-030-007` | `P2` / `NEW`                 | User and ADMIN Question list navigation is exposed through mouse-only rows/cells.                           | `frontend/src/pages/subscriber/QuestionListPage.tsx:170-189`; `frontend/src/pages/admin/QuestionManagePage.tsx:172-180`.                                                                   |
| `F-QAFE-030-008` | `P2` / `NEW`                 | Shared Track-download callers bypass the canonical Blob-aware error normalizer and diverge in action scope. | `frontend/src/api/client.ts:158-195`; cross-entry callers at findings lines 117-128.                                                                                                       |
| `F-QAFE-030-009` | `P2` / `NEW`                 | Shared Modal focus restoration has no fallback when the opener disappears or is disabled.                   | `frontend/src/components/ui/Modal.tsx:80-98`; findings lines 130-139.                                                                                                                      |
| `F-QAFE-030-010` | `P2` / `NEW`                 | Shared lazy routes have no application-owned rejected-import recovery or bounded retry.                     | `frontend/src/App.tsx:5-6`; `frontend/src/router/index.tsx:11-106`; findings lines 141-151.                                                                                                |
| `F-QAFE-030-011` | `P2` / `NEW`                 | Public Track and Album list titles are visual `div`s rather than semantic headings.                         | `frontend/src/pages/public/TrackListPage.tsx:563-572`; `frontend/src/pages/public/AlbumListImagePage.tsx:77-86`; `frontend/src/pages/public/AlbumListPage.tsx:74-83`; live `1280x720` DOM. |
| `F-QAFE-030-012` | `P2` / `NEW`                 | Desktop Header nests `Button` inside `Link`, creating duplicate focusable controls.                         | `frontend/src/layouts/Header.tsx:181-185,199-208`; live `1280x720` DOM.                                                                                                                    |

### R / K / V Reconciliation

| Scope                            | Base   | R      | K      | V         | Evidence boundary                                                                                                                  |
| -------------------------------- | ------ | ------ | ------ | --------- | ---------------------------------------------------------------------------------------------------------------------------------- |
| `INV-PLAY`                       | `FAIL` | `FAIL` | `FAIL` | `FAIL`    | F-QAFE-030-001 plus WI-023 Player-context/closed-tree owners; no media action occurred.                                            |
| `INV-TRACK`                      | `FAIL` | `FAIL` | `FAIL` | `FAIL`    | Unreleased/unfenced contexts, hidden mobile play owner, shortcut collision, and F-QAFE-030-011.                                    |
| `INV-IMAGE`                      | `FAIL` | `FAIL` | `PASS` | `BLOCKED` | Static alt/geometry controls pass; failed-load transition is absent; live error delivery and four widths are unproven.             |
| `INV-SEARCH`                     | `FAIL` | `PASS` | `FAIL` | `FAIL`    | Exact repeated query/chips survived reload and Back/Forward at `1280x720`; keyboard/naming and static closed-tree failures remain. |
| Shared Playlist/download         | `FAIL` | `FAIL` | `FAIL` | `BLOCKED` | WI-024/WI-029 owners plus F-QAFE-030-008; no mutation, file, bytes, or durable result was exercised.                               |
| Shared dialog/ADMIN confirmation | `FAIL` | `FAIL` | `FAIL` | `BLOCKED` | Connected focus/busy controls pass in tests; removed opener and owner pending/typed-confirmation gaps remain.                      |
| Shared auth                      | `FAIL` | `FAIL` | `FAIL` | `FAIL`    | F-QAFE-030-002/003/004/012 plus WI-021/WI-023 owners; only anonymous redirects were observed.                                      |
| `INV-SUB`                        | `FAIL` | `FAIL` | `FAIL` | `BLOCKED` | WI-027/WI-028 projection owners remain; no payment/subscription response or durable transition occurred.                           |
| `INV-QUESTION`                   | `FAIL` | `FAIL` | `FAIL` | `BLOCKED` | F-QAFE-030-007 plus WI-024/WI-028 stale/deep-link/status owners; no authenticated/admin mutation occurred.                         |
| Shared routing/error recovery    | `FAIL` | `FAIL` | `PASS` | `BLOCKED` | Valid Korean 404/500 recovery passed at `1280x720`; rejected imports and four other widths remain unproven/blocked.                |

`V=FAIL` above may be established by direct static source failure at a responsive root. It does not mean every live viewport was executed. Live `1440x900`, `1024x768`, `390x844`, and `360x800` remain `BLOCKED` throughout.

### Evidence-Lane Boundaries

| Lane                | Result                              | Evidence                                                                                                                                                       | Explicit non-inference                                                                                         |
| ------------------- | ----------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------- |
| UI / DOM            | `FAIL` with bounded `PASS` controls | Source semantics, existing jsdom assertions, and anonymous `1280x720` DOM/navigation observations. F-QAFE-030-011/012 were directly confirmed in the live DOM. | Visible UI, URL, or focus state does not prove request acceptance or durable state.                            |
| Frontend invocation | `FAIL` / `PASS` by cited root       | Exact component/store/client call paths and passing scoped assertions; duplicate/stale/error gaps remain as finding causes.                                    | Mocked calls and source branches do not prove live server behavior.                                            |
| Server response     | `NOT RUN` or `BLOCKED`              | No direct API probe, authenticated response, route-chunk failure, binary response, Provider response, or mutation response was exercised.                      | Browser navigation and mocked Axios calls are not server acceptance evidence.                                  |
| Durable state       | `NOT RUN` or `BLOCKED`              | No production DB, audit, payment, subscription, Playlist, Question, License, storage, or Provider state was inspected or changed.                              | Reload/history/UI state is not production persistence or audit proof.                                          |
| Existing assertions | `PASS` for executed inventory       | Scoped Vitest: 44 files and 426 tests passed; no failures or skips shown.                                                                                      | Passing existing tests preserve every missing-assertion and defect finding.                                    |
| Live browser        | `PARTIAL`                           | Safe anonymous fixed `1280x720` routes, redirects, history, recovery, current-width layout, and cleanup only.                                                  | No all-invariant, authenticated, media, file, server, durable, console-clean, or other-width acceptance claim. |

### Prior-Owner Reconciliation

| Prior owner                                                                               | Reconciled scope                                                                                                     | WI-030 disposition                                                                                         |
| ----------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------- |
| `deliverables/agent/WI-20260809-ATS-021-findings.md` - `F-UI-021-001/002/003/004`         | Notice recovery, bare `SubscriberRoute` Login redirect, Header Escape, and localized theme naming.                   | `OWNER-REFERENCE`; `/playlists?from=audit -> /login` remains `F-UI-021-002`.                               |
| `deliverables/agent/WI-20260809-ATS-022-findings.md` - `F-UI-022-001` through `008`       | Auth/account submit, capability, tab, recovery, guard, guidance, and accessibility causes.                           | `OWNER-REFERENCE`; no auth flow was reissued.                                                              |
| `deliverables/agent/WI-20260809-ATS-023-findings.md` - `F-UI-023-001/006/007/008/009/010` | Track/Album recovery, direct play, closed trees, search controls, stale requests, and Player context.                | `OWNER-REFERENCE`/`SHARED-ROOT`; F-QAFE-030-001/011 are independent causes.                                |
| `deliverables/agent/WI-20260809-ATS-024-findings.md` - `F-UI-024-001/003-009/011-016`     | Playlist/Drawer/Question stale, keyboard, loading, mutation, attachment, and object-URL causes.                      | `OWNER-REFERENCE`/`SHARED-ROOT`; F-QAFE-030-007/008/009 are bounded independent causes only.               |
| `deliverables/agent/WI-20260809-ATS-025-findings.md` - `F-UI-025-008-012/014`             | Creator/ADMIN malformed IDs, retry/accessibility, Track/Tag, Notice, and test gaps.                                  | `OWNER-REFERENCE`; no CRUD/media/upload path was reopened.                                                 |
| `deliverables/agent/WI-20260809-ATS-027-findings.md` - `ATS-027-F01` through `F10`        | Subscription/payment purpose, duplicate, unknown outcome, callback, projection, and accessibility causes.            | `OWNER-REFERENCE`/`SHARED-ROOT`; no payment flow was executed.                                             |
| `deliverables/agent/WI-20260809-ATS-028-findings.md` - `F-04/F-06-F12`                    | ADMIN stale state, Question transition, plan projection, raw pending modals, copy, typed confirmation, and settings. | `OWNER-REFERENCE`/`SHARED-ROOT`; F-QAFE-030-006/009 remain limited to their separate roots.                |
| `deliverables/agent/WI-20260809-ATS-029-findings.md` - `F-INTEG-029-A02/A03/A04/A05/A08`  | Question authorization control; download completion, binary envelope, duplicate fence, and bulk bound.               | `NON-DEFECT CONTROL`/`SHARED-ROOT`; F-QAFE-030-008 adds only typed frontend error/action-scope divergence. |

## Safe Anonymous Browser Evidence

- Viewport: fixed `1280x720`; anonymous local application only.
- Public/error routes visited: `/`, `/tracks`, `/tracks/3`, `/albums`, `/subscriptions`, `/notices`, `/login`, `/signup`, `/missing-screen?from=audit`, and `/error`.
- Protected redirects:
  - `/profile?from=audit` -> `/login?returnTo=%2Fprofile%3Ffrom%3Daudit`.
  - `/playlists?from=audit` -> bare `/login` (existing owner `F-UI-021-002`).
  - `/subscriptions/manage?plan=STANDARD&cycle=MONTHLY` -> `/login?returnTo=%2Fsubscriptions%2Fmanage%3Fplan%3DSTANDARD%26cycle%3DMONTHLY`.
- Search/history: `/tracks?keyword=genre01&genre=genre01&genre=genre02&page=1` preserved the exact URL, visible keyword, and both chips across reload; navigation to `/albums`, then Back/Forward, restored the search route/state and `/albums` respectively.
- Recovery: `/missing-screen?from=audit` and `/error` rendered valid Korean 404/500 recovery pages; each Home link returned to `/`.
- Current-width layout/safety: no horizontal overflow, dialogs, or file inputs on the listed route pass.
- Direct DOM defects: visible `/tracks` and `/albums` titles had no `h1`/`h2`/`h3`; desktop Login/subscription controls exposed independently focusable nested `A` and `BUTTON` nodes with the same bounding box.
- Other live widths: `1440x900`, `1024x768`, `390x844`, and `360x800` are `BLOCKED`; the `1280x720` result is not promoted.
- Console: unavailable/non-authoritative; no zero-console-error claim.
- Final cleanup: local Home `/`; viewport `1280x720`; active element `BODY`; dialogs `0`; file inputs `0`; horizontal overflow `false`; media elements `0`; playing media `0`.

## Commands & Outputs (if any)

Execution results were supplied by main for the inspected frontend scope. They were not rerun while creating this Evidence Pack.

**Scoped Vitest** (`frontend`):

- Scope inventory: 44 unique test files listed at `deliverables/agent/WI-20260809-ATS-030-findings.md:327-336`.
- Output: `PASS`; 44 files and 426 tests passed; 0 failures; no skips shown.
- Reproducibility boundary: the single aggregate command text and aggregate duration were not supplied. The retained command inventory defines scope but does not prove every listed line was executed verbatim.

**Frontend typecheck** (`frontend`):

```powershell
npm run typecheck
```

- Output: `PASS`; no additional duration/diagnostic count was supplied.

**Scoped ESLint** (`frontend`):

- Scope inventory: inspected source files and `--max-warnings 0` commands at `deliverables/agent/WI-20260809-ATS-030-findings.md:339-343`.
- Output: `PASS`; 0 warnings.
- Reproducibility boundary: the single aggregate ESLint invocation text and duration were not supplied.

**Frontend build** (`frontend`):

```powershell
npm run build
```

- Output: `PASS`; Vite `6.4.3`; 272 modules transformed; built in `2.17s`.

**Final output-document checks**:

- Initial Prettier check over handoff, findings, Evidence Pack, and summary: exit 1; all 4 files needed formatting.
- Prettier `--write` over the same four files: exit 0; handoff 69ms, findings 129ms, Evidence Pack 37ms, summary 22ms.
- Initial final Prettier check over the same four files: exit 0; all matched files use Prettier code style.
- Docs validation: exit 0; Tier 0 documents, internal links, 542 traceability IDs, document index, and all validations passed.
- `git diff --check`: exit 0; no output.
- Post-closeout patch Prettier check: exit 1; evidence-pack only needed formatting.
- Prettier `--write` over evidence-pack only: exit 0; 65ms.
- Final recheck of all 4 WI-030 documents: exit 0; all matched files use Prettier code style.
- Repeated docs validation: exit 0; 542 traceability IDs and all validations passed.
- Repeated `git diff --check`: exit 0; no output.
- Boundary: `git diff --check` covers tracked diff only while the four outputs are untracked. Prettier and docs validation directly checked the four outputs.

## Tests (if any)

| Test / check lane      | Result                                                  | Evidence boundary                                                                                                                                                           |
| ---------------------- | ------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Scoped Vitest          | `PASS` - 44 files/426 tests; 0 failures; no skips shown | Existing jsdom/mock assertions only; missing cases and findings remain open.                                                                                                |
| TypeScript typecheck   | `PASS`                                                  | Compile/type boundary only; supplied duration unavailable.                                                                                                                  |
| Scoped ESLint          | `PASS` - 0 warnings                                     | Inspected-file scope; exact single aggregate invocation/duration unavailable.                                                                                               |
| Vite build             | `PASS` - Vite 6.4.3; 272 modules; 2.17s                 | Bundle construction only; not live/browser/server acceptance.                                                                                                               |
| Anonymous browser      | `PARTIAL` - fixed `1280x720`                            | Safe public route/DOM/history/cleanup evidence only.                                                                                                                        |
| Other four live widths | `BLOCKED`                                               | Browser resize/device emulation unavailable and forbidden for this WI.                                                                                                      |
| Browser console        | `BLOCKED`                                               | Console unavailable/non-authoritative; no clean-console claim.                                                                                                              |
| Final document checks  | `PASS`                                                  | Post-patch evidence-pack formatting was corrected; the final 4-document Prettier recheck and repeated docs validation passed. `git diff --check` covered tracked diff only. |

No authentication, private/ADMIN content, form submission, media playback/download, file chooser, upload/import/export, payment, subscription, mutation, direct API probe, production DB/storage/Provider/audit inspection, or Git state-changing action occurred. Page-owned anonymous read requests caused by navigation were not inspected or treated as server-response evidence.

The intentional `output/client-demo-screenshots-20260716-140514.zip` remained untouched and uninspected: it was not opened, read, hashed, metadata-probed, moved, replaced, deleted, staged, or used as a fixture.

## Policy Questions for WI-031

1. Does download success mean durable entitlement/resource grant or completed client byte delivery? Owner: `F-INTEG-029-A03`.
2. After central refresh failure, which authenticated origins may be retained as a safe `returnTo`, and may an ADMIN origin be retained?
3. With no refresh token, should the interceptor navigate immediately or only clear identity while route guards own navigation?
4. Should logout navigation await server revocation, or should the product define immediate local logout with bounded background revocation feedback?
5. May an ADMIN using the public shell download an official Track without an active subscription/license, or must shared Player actions use USER entitlement presentation?
6. What canonical fallback asset and alt policy applies when a nonempty image URL fails to load?
7. When an initiated Track download or non-cancelable Playlist mutation outlives its route/modal, should it continue with global feedback, detach silently, or be canceled before invocation?

## Risks / Rollback

- Risks:
  - All 12 independent defects remain open despite passing automation and bounded browser controls.
  - Concurrent replay, authenticated/private roles, mutation outcomes, binary delivery, server responses, durable state/audit, route-chunk rejection, assistive-technology behavior, and four required live widths remain unproven or blocked.
  - Exact aggregate Vitest/ESLint invocation text and durations were not supplied; the documented scope inventory is not a verbatim execution transcript.
  - Repeated `git diff --check` passed but covers tracked diff only because the four outputs are untracked; direct output coverage comes from the passing final 4-document Prettier recheck and repeated docs validation.
- Frozen-state record:
  - No product/runtime/backend/test/config/schema/fixture/current-product-document, DB/storage/Provider/audit/secret/session, or Git state was changed by this documentation closeout.
  - No persistent screenshot, trace, download, or browser artifact was created.
  - The intentional ZIP remained untouched and uninspected under the exact boundary above.
- Rollback:
  - Documentation-only closeout. If withdrawn, remove only `deliverables/agent/WI-20260809-ATS-030-evidence-pack.md` and `deliverables/user/WI-20260809-ATS-030-summary.md`; no product, runtime, database, storage, browser-state, or data cleanup is required.

## Follow-ups (optional)

- Direct next WI: `WI-20260809-ATS-031` (WI-030 handoff lines 5-7).
- WI-031 must consolidate and deduplicate WI-021 through WI-030 findings, group shared root causes, preserve owner IDs and evidence lanes, and place the seven unresolved decisions behind explicit decision gates before any product fix is selected or implemented.
- Main corrected the post-closeout evidence-pack formatting failure, then completed the final 4-document Prettier recheck, repeated docs validation, and repeated `git diff --check`; preserve the recorded tracked/untracked boundary without treating document checks as product/browser reruns.
- Any remediation, new assertion, resizable live-browser pass, authenticated acceptance, or server/durable verification requires its own approved WI/scope; WI-030 made no product change.

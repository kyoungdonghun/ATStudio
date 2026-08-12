[WI HEADER]
WI ID: WI-20260809-ATS-030
REQ: REQ-20260809-ATS-001
Session ID: none
Agent: qa-fe
Depends On: WI-20260809-ATS-021 through WI-20260809-ATS-029
Blocks: WI-20260809-ATS-031
Baseline: codex/v1-release-rehearsal-fixes @ e343c2085fbc82c66b44fb8e5edde35bf920980f

[WI SUMMARY]

Why:

- Audit integrated frontend behavior that can fail only when the same object, action, route, or shared component is exercised through multiple entry points.
- Reconcile interruption, stale-response, accessibility, responsive, and adjacent regression evidence after the owning-flow audits in WI-021 through WI-029.
- Consolidate shared root causes without copying or reclassifying an owning WI's finding merely because another page exhibits the same symptom.

Scope (in):

- Cross-entry invariants `INV-PLAY`, `INV-TRACK`, `INV-IMAGE`, `INV-SEARCH`, `INV-SUB`, and `INV-QUESTION` from matrix section 13.
- Shared Playlist, Like, History, License, and download controls where one shared component/store/API behavior crosses multiple pages or route families.
- Shared auth guards, anonymous redirects, role fallbacks, and safe internal `returnTo`/callback/query parameter handling.
- Shared ADMIN confirmation behavior only through source and isolated tests; no ADMIN browser session or mutation is permitted.
- Reload, refresh, back, forward, deep link, route departure during load, reopen, and resume behavior under the matrix `R` pack.
- Double click, Enter plus click, concurrent 401 refresh, rapid filter/page/search, late response, duplicate callback, and stale selection behavior.
- Shared error normalization from Axios/interceptors through page, modal, toast, inline error, retry, and logout fallback surfaces.
- `Header`, `PlayerBar`, `playerStore`, waveform, cards, lists, modals, `ConfirmDialog`, pagination, filters, search, loading, empty, error, API client, guards, and public/admin/main layouts.
- Keyboard and accessibility behavior under the matrix `K` pack: tab order, focus visibility, semantic names, activation, Escape policy, dialog focus, restoration, status/error announcement, and keyboard-only completion.
- Static responsive review for assigned shared surfaces and one current-width anonymous browser pass at exactly 1280x720.
- Adjacent regression checks required by shared DTO, store, API, layout, or component roots discovered during the audit.
- Direct-contract follow-ups from matrix section 18.3 that affect shared frontend behavior: auth refresh, Track stream source, SPA deep-link forwarding, Playlist batch membership, Subscription detail consistency, and safe classification of API-only paths.

Scope (out):

- Re-running complete owning flows from WI-021 through WI-029.
- Treating `INV-WHITE` or `INV-CERT` as new full-flow audits; inspect them only when a shared root or adjacent regression directly reaches their surfaces.
- Product code, runtime, backend, tests, configuration, schema, fixtures, current product documentation, database, storage, Provider, mail, or secret changes.
- Authenticated USER, BUSINESS, CREATOR, or ADMIN browser sessions.
- ADMIN operations, private pages/files, user mutation, payment, refund, entitlement, certification, Whitelist, Settlement, or Provider actions.
- Upload, download, import, export, browser file chooser, clipboard file action, or any inspection of a generated/private/user artifact.
- Browser storage, cookies, session, IndexedDB, token, or ignored configuration inspection, including using such state as acceptance evidence.
- Starting, stopping, restarting, or reconfiguring the application runtime.
- Live checks at 1440x900, 1024x768, 390x844, or 360x800; browser resize is not available for this WI.
- Real database/storage/audit verification or destructive cleanup.
- Git stage, commit, push, branch, reset, checkout, stash, or any other mutation.

DoD:

- [ ] Every in-scope invariant has one reconciliation row with all applicable entry points and links to its owning WI evidence.
- [ ] Every invariant has separate `R`, `K`, and `V` classifications as `PASS`, `FAIL`, `BLOCKED`, or `NOT RUN`.
- [ ] UI/DOM, frontend invocation, server response, and durable-state lanes are classified independently; a UI result never proves another lane.
- [ ] Shared shell/component/store/API roots are mapped to all affected entry points before a finding is raised.
- [ ] Reload/back/forward/deep-link/route-departure outcomes are explicit.
- [ ] Duplicate and stale-response cases distinguish definite rejection, unknown mutation outcome, latest-request-wins, and canonical reload.
- [ ] Shared error normalization and 401 refresh concurrency/logout fallback are traced from source and isolated assertions without using a live session.
- [ ] Safe `returnTo` and callback parameters reject external, protocol-relative, malformed, or stale targets in source/tests; no real auth callback runs.
- [ ] Keyboard checks name the trigger, focus sequence, close behavior, and final focus target; unsupported browser evidence is not inferred.
- [ ] Static CSS findings and the 1280x720 live result are reported separately.
- [ ] The four unresizable viewports are recorded `BLOCKED`, never `PASS` from static CSS alone.
- [ ] Prior findings are referenced by path/ID and reconciled by shared root; owning causes are not duplicated as WI-030 findings.
- [ ] New findings include severity, exact source/test/browser pointers, affected entry points, lane classification, impact, and bounded follow-up.
- [ ] Existing isolated tests are inspected before citation and executed only in their configured mock/jsdom/H2/temp boundaries.
- [ ] Exact commands, case counts, duration, warnings, skips, and evidence boundaries are recorded; cached or unexecuted results are not called PASS.
- [ ] Anonymous browser cleanup ends on local Home at 1280x720 with dialogs 0, file inputs 0, `BODY` active, and no current-width horizontal overflow.
- [ ] Browser console evidence limitations are stated; unavailable console access is `BLOCKED`, not silently clean.
- [ ] Findings, Evidence Pack, and user summary are written to the exact output paths in this packet; no other file is changed.
- [ ] Policy/security/product questions are extracted for WI-031 and not decided.

Constraints / Forbidden:

- Write only:
  - `deliverables/agent/WI-20260809-ATS-030-handoff.md`
  - `deliverables/agent/WI-20260809-ATS-030-findings.md`
  - `deliverables/agent/WI-20260809-ATS-030-evidence-pack.md`
  - `deliverables/user/WI-20260809-ATS-030-summary.md`
- Product/runtime/DB/schema/config/test/fixture/current-doc state is frozen.
- Existing isolated tests may run without fixture/config edits or retained output.
- Browser use is anonymous and limited to an already-running local application.
- If no safe local anonymous Home is available, classify live browser lanes `BLOCKED`; do not start or repair the runtime.
- Browser viewport is fixed at 1280x720. Do not attempt resize or device emulation.
- Browser actions are limited to safe public display, navigation, and keyboard.
- Do not submit a form, change a preference, log in/out, like, add, remove, create, delete, download, upload, import, export, subscribe, pay, refund, or confirm.
- Do not inspect browser storage/cookies/session or infer role state from them.
- Do not access authenticated/private content even if an existing session appears.
- Do not invoke ADMIN or API-only mutation/support endpoints from matrix 18.3.
- Do not inspect real Provider, payment, refund, reconciliation, audit, mail, database, storage, user data, private file, or secret state.
- Never open, read, hash, metadata-probe, move, replace, delete, stage, or use `output/client-demo-screenshots-20260716-140514.zip` as a fixture.
- Never inspect ignored secrets, `application-local.yml`, `.env`, credentials, tokens, keys, cookies, sessions, or environment secret values.
- Do not create persistent screenshots, traces, downloads, or browser artifacts.
- An ephemeral in-tool screenshot/DOM observation at 1280x720 is allowed only if it creates no repository/output artifact and contains public anonymous content.
- No git mutation. Read-only branch/HEAD/status evidence may use the supplied baseline pointer; do not touch the intentional ZIP while checking state.
- Stop and report any unexpected product/runtime/data/file/git mutation rather than attempting cleanup outside the approved output paths.

[ACCEPTANCE CRITERIA]

Functional:

- [ ] `INV-PLAY`: every named playback entry point projects the same Track, resets source/time/error atomically, and shares coherent PlayerBar state.
- [ ] `INV-TRACK`: PlayableTrack normalization produces stable explicit nullable media fields and never leaks storage keys across cards/lists/history.
- [ ] `INV-IMAGE`: canonical URL/fallback behavior preserves control geometry and existing-image rules across public, subscriber, PlayerBar, and admin views.
- [ ] `INV-SEARCH`: Header, Track list, chips, modal, and admin Tag semantics agree on keyword, raw URL/API values, display `#`, and taxonomy AND behavior.
- [ ] `INV-SUB`: plans, guards, actions, profile, and admin projections use mutually consistent status, entitlement, billing, and reload semantics.
- [ ] `INV-QUESTION`: user/admin projections preserve one identity, status, attachment set, role capability boundary, and canonical reload result.
- [ ] Shared Playlist/download paths have one duplicate/pending/error/unknown- outcome contract and do not diverge by card, modal, drawer, or page.
- [ ] Header/MainLayout/AdminLayout guard and route behavior has no redirect loop, content flash, unsafe external target, or blank intermediate route.
- [ ] Auth refresh source/tests prove one bounded refresh for concurrent 401s and a deterministic logout/safe-navigation fallback without token disclosure.
- [ ] Back/forward/deep-link/refresh and route departure cannot reapply stale data, retain trapped dialogs, lose recoverable state, or repeat mutation.
- [ ] Rapid filter/search/page requests are latest-request-wins with stable row identity, loading ownership, empty state, error state, and retry.
- [ ] Shared error normalization preserves actionable context and does not expose raw backend/provider/stack/credential wording.
- [ ] ConfirmDialog cancel/confirm/pending/error/focus/Escape/typed-confirmation behavior is consistent; ADMIN mutation execution remains forbidden.
- [ ] Cards, lists, pagination, filters, loading, empty, and error surfaces cannot resize fixed controls or hide the primary action at current width.
- [ ] Keyboard-only public navigation has logical order, visible focus, semantic names, Enter/Space activation, Escape policy, and no dead end.
- [ ] Adjacent regressions are tested wherever a shared root affects an entry point outside the initially observed page.
- [ ] Each shared defect is assigned to one root cause and links every prior owner finding that remains valid; no duplicate independent cause is created.

Performance:

- [ ] Record source/test facts for unbounded collections, repeated rendering, duplicate requests, listener/timer cleanup, and stale async work.
- [ ] Do not invent response-time, memory, or accessibility SLOs absent a current project contract; classify heuristic conflicts as policy questions.
- [ ] At 1280x720, Header/PlayerBar/fixed overlays do not occlude primary public content and `scrollWidth <= clientWidth` at cleanup.
- [ ] Static review covers fixed dimensions, long content, breakpoint behavior, overflow, focus visibility, and responsive control reachability.
- [ ] 1440x900, 1024x768, 390x844, and 360x800 live results are `BLOCKED` because resize is unavailable; static review may produce `FAIL`, never live `PASS`.

Quality (REQ gates G1-G8):

- [ ] `G1` Active asset coverage: cross-list matrix rows, routes, shared wrappers, shared components/stores, and visible entry points; report omissions.
- [ ] `G2` Classification completeness: zero unclassified in-scope invariants or `R/K/V` lanes; every skip is `BLOCKED` or `NOT RUN` with reason.
- [ ] `G3` Evidence quality: link required UI/DOM, invocation, response, and durable lanes without substituting one lane for another.
- [ ] `G4` Complex-flow consistency: reconcile shared subscription/admin/CSV/file projections from prior evidence without performing forbidden mutations.
- [ ] `G5` Adjacent impact: run focused and adjacent shared-root regressions and identify broader regression coverage that remains blocked.
- [ ] `G6` Automated quality: record exact targeted Vitest, typecheck, ESLint, Prettier, build/other applicable commands actually run and their boundaries.
- [ ] `G7` Docs/code consistency: semantic cross-check and final document checks are recorded; do not claim validate-docs or diff checks until executed.
- [ ] `G8` Independent result: classify findings by severity and report residual risk, blocked lanes, and policy questions for WI-031.

[EXECUTION / CLASSIFICATION PROTOCOL]

Required status vocabulary:

- `PASS`: direct evidence satisfies the invariant in that exact lane.
- `FAIL`: direct source/test/DOM evidence contradicts the invariant.
- `BLOCKED`: safety rule, browser capability, missing fixture, unavailable console, or prohibited auth/live state prevents valid evidence.
- `NOT RUN`: in-scope check was not executed within the bounded pass; state why and identify whether a safe targeted test remains available.

Required evidence lanes for every invariant:

| Lane                | Required evidence                                                                        | Prohibited inference                                      |
| ------------------- | ---------------------------------------------------------------------------------------- | --------------------------------------------------------- |
| UI / DOM            | Visible state, semantics, focus, layout, public browser or DOM assertion                 | A click/toast does not prove a request or durable result. |
| Frontend invocation | Exact shared function/store/guard, method/path/body/params, duplicate and abort behavior | A mocked call does not prove server acceptance.           |
| Server response     | Existing isolated contract assertion for status/shape/error/headers where relevant       | No live authenticated/API call in WI-030.                 |
| Durable state       | Prior owning evidence or isolated persistence assertion with explicit boundary           | UI reload/mock state is not production DB/audit proof.    |

Required matrix per invariant:

- Inventory all entry points before assigning the shared root.
- Classify base invariant plus `R`, `K`, and `V` separately.
- Record current-width browser, static CSS, and isolated tests as separate evidence.
- Use `OWNER-REFERENCE` when a prior finding fully owns the root cause.
- Use `SHARED-ROOT` when WI-030 adds cross-entry impact to one existing cause.
- Use `ADJACENT-REGRESSION` when a shared fix/test must cover another entry point.
- Use `NEW` only when the cause is independent of WI-021 through WI-029.
- Do not change prior finding severity; report disagreement as a reconciliation question for WI-031 with both evidence pointers.
- Extract policy questions without selecting behavior, copy, limits, breakpoints, authorization, persistence, or recovery semantics.

Browser protocol:

- Use only the existing anonymous local Home at 1280x720.
- Safe actions: public navigation, back/forward, refresh, deep link, tab, Shift+Tab, Enter/Space on non-mutating navigation, and Escape for a public transient UI.
- Do not trigger media download, file controls, auth, mutation, or external target.
- If a safe action could mutate browser-local state, do not execute it; use static source/tests and classify the live lane `BLOCKED`.
- Capture only sanitized public DOM/screenshot evidence; no persistent files.
- Record console access capability. If full console is unavailable, state the limitation and do not claim zero console errors.
- Cleanup: navigate to Home; close every dialog; confirm file inputs 0; move focus to `BODY`; assert no current-width horizontal overflow; leave browser neutral.

[INPUT POINTERS]

Tier 0 (Constitution - required):

- `docs/standards/core-principles.md`
- `docs/standards/development-standards.md`

Tier 1 (Policies - inferred for auth/access/quality):

- `docs/policies/security-policy.md`
- `docs/policies/access-control-policy.md`
- `docs/policies/quality-gates.md`

Tier 2 (Frontend and API contracts):

- `docs/standards/frontend-standards.md`
- `.agents/skills/react-best-practices/AGENTS.md`
- `docs/design/api-spec.md`
- `docs/ui/screen-flow.md`

REQ / matrix context:

- `deliverables/user/REQ-20260809-ATS-001.md:1-120` (approved; WI-030 qa-fe; quality gates G1-G8; do not duplicate the REQ skill chain).
- `deliverables/agent/WI-20260809-ATS-020-acceptance-matrix.md:65-114` (section 3: state packs, evidence levels, side-effect classes).
- `deliverables/agent/WI-20260809-ATS-020-acceptance-matrix.md:116-128` (section 4: global guards and redirects).
- `deliverables/agent/WI-20260809-ATS-020-acceptance-matrix.md:214-225` (section 11: shared shell/player/dialog surfaces).
- `deliverables/agent/WI-20260809-ATS-020-acceptance-matrix.md:253-265` (section 13: same behavior across entry points).
- `deliverables/agent/WI-20260809-ATS-020-acceptance-matrix.md:366-381` (section 15: cross-cutting heuristics).
- `deliverables/agent/WI-20260809-ATS-020-acceptance-matrix.md:383-400` (section 16: WI assignment and exit gate).
- `deliverables/agent/WI-20260809-ATS-020-acceptance-matrix.md:528-555` (section 18.3: direct frontend/backend reconciliation).

Prior owning findings/evidence (reference by path and finding ID; do not read or
paste entire documents):

- `deliverables/agent/WI-20260809-ATS-021-findings.md`
- `deliverables/agent/WI-20260809-ATS-021-evidence-pack.md`
- `deliverables/agent/WI-20260809-ATS-022-findings.md`
- `deliverables/agent/WI-20260809-ATS-022-evidence-pack.md`
- `deliverables/agent/WI-20260809-ATS-023-findings.md`
- `deliverables/agent/WI-20260809-ATS-023-evidence-pack.md`
- `deliverables/agent/WI-20260809-ATS-024-findings.md`
- `deliverables/agent/WI-20260809-ATS-024-evidence-pack.md`
- `deliverables/agent/WI-20260809-ATS-025-findings.md`
- `deliverables/agent/WI-20260809-ATS-025-evidence-pack.md`
- `deliverables/agent/WI-20260809-ATS-026-findings.md`
- `deliverables/agent/WI-20260809-ATS-026-evidence-pack.md`
- `deliverables/agent/WI-20260809-ATS-027-findings.md`
- `deliverables/agent/WI-20260809-ATS-027-evidence-pack.md`
- `deliverables/agent/WI-20260809-ATS-028-findings.md`
- `deliverables/agent/WI-20260809-ATS-028-evidence-pack.md`
- `deliverables/agent/WI-20260809-ATS-029-findings.md`
- `deliverables/agent/WI-20260809-ATS-029-evidence-pack.md`

Bounded frontend source discovery pointers:

- Router/entry: `frontend/src/main.tsx`, `frontend/src/App.tsx`, `frontend/src/**/router*.tsx`, `frontend/src/**/routes*.tsx`.
- Guards/layouts: `frontend/src/**/Guard*.tsx`, `frontend/src/**/Route*.tsx`, `frontend/src/layouts/**`, `frontend/src/**/Header*.tsx`.
- Player: `frontend/src/layouts/PlayerBar.tsx`, `frontend/src/stores/playerStore.ts`, `frontend/src/**/Waveform*.tsx`.
- Shared UI: `frontend/src/components/**`, bounded to cards, lists, modals, ConfirmDialog, pagination, filter/search, loading/empty/error components.
- APIs: `frontend/src/api/client.ts` and only wrappers reached by the in-scope invariant entry points.
- Stores/hooks: `frontend/src/stores/**`, `frontend/src/store/**`, and shared hooks reached by router/player/search/list/modal behavior.
- Styles: only CSS/module files imported by an in-scope shared surface; static responsive review does not authorize a whole-style-tree scan.
- Pages: only entry points listed by matrix section 13 plus directly adjacent shared-root consumers; do not re-audit owning pages end to end.

Bounded automated-test pointers:

- `frontend/src/**/*.test.ts`
- `frontend/src/**/*.test.tsx`
- `frontend/src/test/coverage/**`
- Resolve and run only tests that directly cover router/guard, Header, PlayerBar, playerStore, waveform, shared cards/lists/modals/dialog, API client, stale request, error normalization, responsive DOM, or keyboard behavior.
- Prior backend test results are evidence pointers only unless one isolated controller/contract test is necessary for a direct shared response claim.
- Do not use live DB, Provider, private fixture, browser session, or file artifact.

Repro / logs:

- Record every exact test command before execution and its cwd.
- Record process exit, files/suites/tests, failures/errors/skips, duration, and warnings; name every skipped case and reason.
- Identify jsdom/mock/fake-timer/H2/temp/provider-test boundaries per suite.
- Run `npm run typecheck` if applicable and record exact output.
- Run targeted ESLint over only inspected frontend files with zero-warning policy; record the exact command, not merely `PASS`.
- Run output-document Prettier/check and applicable docs validation only after findings/evidence/summary exist; record actual results.
- Do not run browser resize automation, authenticated tests, file tests, or any command requiring ignored local configuration/secrets.

[OUTPUT CONTRACT]

User-facing -> `deliverables/user/WI-20260809-ATS-030-summary.md`:

- English concise summary of classified invariants, new/shared-root findings, blocked live lanes, browser cleanup, test results, residual risk, and policy questions for WI-031.

Agent-facing -> `deliverables/agent/WI-20260809-ATS-030-evidence-pack.md`:

- Reproducible R/K/V matrix, entry-point/root-cause crosswalk, four evidence lanes, source/test/browser pointers, commands/results/warnings, isolation boundaries, frozen-state statement, and rollback/no-product-change note.

Findings -> `deliverables/agent/WI-20260809-ATS-030-findings.md`:

- Independent shared causes only, with severity, exact pointers, affected entry points, prior owner IDs, R/K/V and evidence-lane classification, impact, bounded follow-up, and policy-question flags.

Handoff Packet -> `deliverables/agent/WI-20260809-ATS-030-handoff.md`:

- This packet only.

[TRACEABILITY REQUIREMENTS]

- Cite exact source/test path and line or test name for every PASS/FAIL claim.
- Cite prior WI path and finding ID when using owner evidence; do not summarize a prior document without the precise pointer needed for the shared-root claim.
- Record every invariant entry point before claiming cross-entry consistency.
- Keep UI/DOM, frontend invocation, server response, and durable-state evidence separate in findings, matrix, Evidence Pack, and summary.
- A browser-visible result is not a server response; a mocked request is not a durable-state result; a reload is not production DB/audit proof.
- For `R`, name initial route/state, interruption, late response, final route, final UI, invocation count, and canonical-read evidence.
- For `K`, name focus origin/order, accessible role/name, activation key, dialog containment/Escape rule, and restored focus target.
- For `V`, name evidence type (`STATIC` or `LIVE-1280x720`), measured overflow or occlusion fact, content variant, and why other live viewports are blocked.
- For auth refresh/return targets, redact tokens and never inspect browser state; rely on source and isolated tests only.
- For unknown mutation outcomes, require reconciliation-read semantics; do not infer rollback from missing success UI.
- Preserve exact test commands/results and distinguish main-supplied evidence from commands executed by qa-fe.
- Record anonymous browser cleanup: Home, 1280x720, dialogs 0, file inputs 0, `BODY` active, no current-width horizontal overflow, console limitation.
- Record that 1440x900, 1024x768, 390x844, and 360x800 live checks are `BLOCKED`.
- Record no auth/admin/private/mutation/file/payment/provider/live-DB/storage action.
- Record no product/runtime/test/config/schema/fixture/current-doc/git mutation.
- Record that the intentional ZIP was preserved and never opened, read, hashed, metadata-probed, moved, replaced, deleted, staged, or used.
- Classify policy ambiguity for WI-031 without choosing the outcome.
- Rollback: documentation-only outputs may be removed/restored; no product/data rollback is expected because product and runtime state must remain frozen.

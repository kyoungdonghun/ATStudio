---
version: 1.2
last_updated: 2026-09-09
project: ATS
owner: PG
category: evidence-pack
status: active
dependencies:
  - path: WI-20260909-ATS-010-handoff.md
    reason: Read-only review assignment and report ownership
  - path: ../user/REQ-20260909-ATS-001.md
    reason: Approved bounded remediation
  - path: WI-20260909-ATS-001-evidence-pack.md
    reason: Backend implementation and fixture boundaries
  - path: WI-20260909-ATS-004-evidence-pack.md
    reason: Frontend implementation and fixture boundaries
---

# Evidence Pack: WI-20260909-ATS-010

## Summary

Independent source/diff/test review completed. One P2 residual session-ownership
path remains in the existing social callback caller. No additional concrete
backend defect was established within SEC-01/02/03/05 and AUTH-06. This is not a
full-test, production, or enabled-OAuth approval.

## Findings

### F1 [P2]: An Older Social Callback Can Replace or Log Out the Newer Session

- Traceability: SEC-04, adjacent existing AUTH-06 lifecycle caller. This is an
  unresolved integration path, not a claim that WI004 introduced the caller bug.
- Primary location: `frontend/src/pages/auth/SocialLoginPage.tsx:85-89`.
  Success location: the same file at `48-66`; initial exchange at `44-45`.
- Trigger: a valid synthetic OAuth callback for A stages its tokens and waits
  for `fetchMe(A)`. Before that promise settles, the SPA clears/logs out A and
  establishes B, including a new session for the same user. Let A's request fail.
  The callback invokes the captured `authLogout()` function without checking the
  initiating generation. `authStore.ts:165-183` captures the generation at this
  *new invocation*, so it treats B as the logout owner, sends B's logout request,
  and clears B. The guards inside `logout()` only protect changes occurring
  after that invocation; they cannot identify the stale caller.
- Success variant: resolving the old `fetchMe(A)` instead runs `authLogin` with
  A's retained access/refresh tokens and profile. B is replaced, or a completed
  local logout is undone. `client.ts:131` passes successful responses through;
  `login()` establishes a fresh generation rather than rejecting an old caller.
  A callback whose initial exchange fails before staging also calls
  `clearSession()` on whatever session is current.
- Counter-defense checked: `client.ts:133-138` correctly rejects A's stale 401
  before refresh/replay; that rejection still enters this caller's catch. OAuth
  attempt validation and `processed.current` prevent invalid/repeated initial
  exchange, not later completion after navigation or session replacement. The
  effect at `SocialLoginPage.tsx:26-95` has no cleanup/generation guard.
- Existing test gap: `SocialLoginPage.test.tsx:229` exercises failure while the
  staged session is still current. WI004's real-Axios/real-store tests reject the
  old client promise, but do not compose it with this callback's cleanup.
- Evidence level: source-confirmed control flow; the deferred component
  reproduction has **not been executed**. This requires an actual accepted
  social exchange, or a fake fixture. No real OAuth provider was enabled or
  tested. Disabled providers remain disabled; this is not an observed incident.

## Scope / DoD Check

- [x] Read the assigned handoff first; reviewed WI001/WI004 product diffs and
  corresponding test changes without editing them.
- [x] Checked real signed purpose boundaries, refresh issuance uniqueness,
  stored-hash revocation, current-role source, social verification semantics,
  reset consumption/issuance lock order, and application exception logging.
- [x] Traced dispatch ownership, replay, coalescing, success/failure,
  logout/relogin, lazy imports, and existing callback callers.
- [x] Sent the MA-owned focused test selection and concrete residual path.
- [x] Authored only the two assigned reports using create-wi-evidence-pack.
- [ ] F1 closed and its deferred caller regression executed.
- [ ] Final full backend/frontend verification accepted by MA.

## Reference Documents (Tier 0-2)

| Tier | Document | Use |
|---|---|---|
| Entry | `AGENTS.md` | Ownership, approved REQ/WI, no subdelegation |
| 0 | `docs/standards/core-principles.md` | Bounded work, preserved policies/data |
| 0 | `docs/standards/development-standards.md` | Java17 transactions/DTOs, test evidence |
| 0 | `docs/standards/documentation-standards.md` | Metadata and two-set reports |
| 0 | `docs/standards/glossary.md` | Existing authentication/domain terminology |
| 1 | `docs/policies/security-policy.md` | Token/log privacy, replay and ADMIN sync contracts |
| 1 | `docs/policies/quality-gates.md` | Independent review and truthful gate boundaries |
| 2 | `deliverables/user/REQ-20260909-ATS-001.md` | Approved scope and serialized MA runners |
| 2 | `deliverables/agent/WI-20260909-ATS-010-handoff.md` | Review inputs and exact outputs |
| 2 | `deliverables/agent/WI-20260908-ATS-018-findings.md` | SEC/AUTH finding definitions, not current execution evidence |
| 2 | `deliverables/agent/WI-20260909-ATS-001-evidence-pack.md` | Backend changes and fixture limitations |
| 2 | `deliverables/agent/WI-20260909-ATS-004-evidence-pack.md` | Frontend changes and initial focused checkpoint |
| Skill | `.agents/skills/react-best-practices/SKILL.md` | Existing asynchronous/store patterns; no framework expansion |
| Skill | `.agents/skills/create-wi-evidence-pack/SKILL.md` | Handoff precondition, evidence and report structure |

MA supplied Tier-0 rules and PG review scope. Relevant linked documents were
read/consulted, with targeted follow-up after truncated aggregate output. No
subagent was invoked and no automatic context-injection run is claimed. The
workspace configuration's domain project tag was confirmed as `ATS`.

## Evidence Pointers

All paths are relative to `C:/Users/jm991/Desktop/project/ATStudio`. Read-only
`git --no-optional-locks rev-parse HEAD` returned
`8161f0a00b088001a37962da719c3ace8fea44ad`; review covers its shared uncommitted
remediation diff. Locations describe the review snapshot, not a later deploy.

| Boundary | Source and test evidence | Review result / limit |
|---|---|---|
| SEC-01 purpose | `security/JwtTokenProvider.java:90-120`, `security/JwtAuthenticationFilter.java:34-41`; `JwtTokenPurposeTest.java:45`, `AuthTokenSecurityIntegrationTest.java:80` | API uses access validation; refresh validates refresh purpose before repository lookup. Wrong/absent purpose fails even when expired; no typeless fallback. Real signed fixtures cover both surfaces. |
| SEC-03 issuance | `security/JwtTokenProvider.java:51-62`; `JwtTokenPurposeTest.java:66` | UUIDv4 refresh jti; 100 distinct fixed-instant refresh JWTs/IDs asserted. Validator requires nonblank issuance identity; normal TTLs preserved. |
| Stored-hash revocation | `service/auth/AuthService.java:81-121`; `AuthTokenSecurityIntegrationTest.java:80-150` | User lock and exact stored SHA-256 comparison precede rotation; stale token rejection preserves replacement hash. Logout/reset clear refresh capability; ordinary access TTL is intentionally retained. MVC lifecycle repository/service dependencies are mocked. |
| Current DB role | `security/JwtAuthenticationFilter.java:38-41`, `security/CustomUserDetailsService.java:27-33` | Authorities still come from the current user lookup, not JWT role claim. MVC role test mocks UserDetailsService, so it does not itself execute a live DB role lookup. |
| AUTH-06 | `service/auth/AuthService.java:104-108`, `service/auth/OAuth2Service.java:73-104`; `AuthTokenSecurityIntegrationTest.java:148-175` | Existing passwordless OAuth representation may refresh with a valid stored capability; password-bearing accounts still require verification. No new verify/auto-link/provider-enable operation. OAuth exchange is mocked. |
| SEC-05 consume | `service/EmailService.java:149-165`, `repository/PasswordResetTokenRepository.java:17-23`; `PasswordResetConsumptionIntegrationTest.java:61-137` | Scalar owner lookup avoids a managed stale token; User then token write locks precede used/expiry validation. Password/token/session mutation is transactional. Separate H2 contexts test one winner and rollback/retry, not MySQL isolation. |
| SEC-05 issuance | `service/EmailService.java:93-107` | User lock precedes reset-token deletion/creation, matching consumption order. No deterministic issuance-vs-consume H2 schedule is present; order was inspected statically. |
| SEC-02 logs | `common/exception/GlobalExceptionHandler.java:34-38,76-86,133-142`; `SensitiveValidationLogTest.java:62-165` | Allowlisted/bounded field diagnostics and enum/class names only; original messages, values, causes, suppressed exceptions and throwable arguments are omitted. Tests invoke the handler directly and inspect Logback events, not infrastructure/APM/access logs. |
| SEC-04 dispatch/replay | `frontend/src/api/client.ts:33-49,92-126,133-179`; `frontend/src/api/client.test.ts:609-854` | Synchronous request interceptor captures/preserves generation; stale replay throws; current generation and logout state checked before token persistence and replay. Flights are generation-scoped and identity-cleared. Normal one-replay, exclusions, skipAuthReplay and ADMIN sync tests remain. |
| SEC-04 store/reentrancy | `frontend/src/store/authStore.ts:34-40,84-100,112-191` | Login advances before storage; logout invalidates immediately and guards lazy dispatch/finalization. Refresh checks again after synchronous Zustand notification, so a newer login there prevents replay/old cleanup. Existing test at `client.test.ts:780` uses queued microtask replacement, not a direct synchronous listener. This distinction is not claimed as extra executed coverage. |

Java product paths in the table start at `src/main/java/com/atstudio/atstudio/`;
Java test names resolve under `src/test/java/com/atstudio/atstudio/` in their
corresponding `security`, `service`, or `common/exception` package.

Only reports changed by this WI:
- `deliverables/agent/WI-20260909-ATS-010-evidence-pack.md`.
- `deliverables/user/WI-20260909-ATS-010-summary.md`.

## Commands & Outputs

- Executed only scoped `Get-Content`, `rg`, `Get-ChildItem`, structured config
  parsing, read-only Git diff/HEAD inspection, report `apply_patch`, and report
  static checks. One speculative `frontend/src/hooks/useAuth.ts` lookup did not
  exist; `rg` located actual store/page callers instead.
- Reviewed diffs of WI001's six product files/four existing test files and
  WI004's client/store files; read the newly added signed-token, reset-lock,
  captured-log and session-ownership tests directly.
- Selected preserved MA artifact rows were read from
  `output/release-remediation-20260909/`; these are synthetic verification
  artifacts, not historical application/provider/mail logs.
- No Gradle, npm, compiler, test runner, server, browser, provider, mail, real
  DB/DDL, media, secret, Git write, or subdelegation action was performed by PG.
- Scoped `git --no-optional-locks diff --check -- ...` on the six backend
  product files plus four WI004 files exited 0. Git emitted only CRLF-to-LF
  advisory warnings for five backend files; no whitespace error was reported.
- Report-only PowerShell checks found, for each of the two reports: missing
  required metadata 0, trailing-whitespace lines 0, broken Markdown links 0.
  These simple static checks are not the full repository documentation gate.

## Tests

### Existing MA Checkpoints, Not PG Executions

| Checkpoint | Result and provenance | Boundary |
|---|---|---|
| Initial backend authentication selection | 84 tests, zero failure/error/skip; `focused-initial-results.json`, `focused-initial.log:154` reports success in 1m 7s | Count is auth-related subtotal, including JwtConfig; not full backend pass |
| Second backend selection | `SensitiveValidationLogTest`: 4; `SecurityFilterChainTest`: 23; all pass, within 158-test second selection; `storage-auth-second-results.json`, `storage-auth-second.log:151` | Closes the initial missing-suite execution gap, not final aggregate/coverage |
| Initial frontend focus | 2 files / 63 tests pass in 3.53s; `frontend-focused-initial.log` | Existing installed-dependency checkpoint; no F1 caller race assertion |
| Isolated frontend first coverage | 108 files / 1,399 tests pass; `frontend-incomplete-snapshot-coverage.log:11-14` | **INCOMPLETE SNAPSHOT, NOT FULL PASS**: MA caught four omitted `src/test/coverage` source test files caused by an overbroad exclusion |
| Restored frontend coverage snapshot | MA reports four files restored, 334 inputs, expected 112 test files; complete rerun in progress at latest instruction | No completion/coverage result claimed; first coverage-summary artifact is not accepted as final evidence |
| Isolated frontend typecheck | MA reports pass; `frontend-typecheck.log` contains `tsc --noEmit` | Separate checkpoint; log itself does not encode process exit status |
| First full backend attempt | `backend-full-first.log:12-15,47`: compileTestJava fails because InputBuffer is not a functional interface | MA reports WI005 corrected the fixture; full rerun not accepted here. No auth runtime failure is inferred from this compile error |

Artifact paths in the table start at `output/release-remediation-20260909/`.
No baseline mutation, browser, live OAuth, MySQL concurrency, deployed log
collector, final bundle coverage, or final build validation was executed by PG.

### Ready Selection for MA Only

Existing backend selection, to be scheduled only by MA:

```powershell
.\gradlew.bat test --tests "*.JwtTokenProviderTest" --tests "*.JwtTokenPurposeTest" --tests "*.JwtAuthenticationFilterTest" --tests "*.AuthServiceTest" --tests "*.AuthTokenSecurityIntegrationTest" --tests "*.EmailServiceTest" --tests "*.PasswordResetConsumptionIntegrationTest" --tests "*.SensitiveValidationLogTest" --tests "*.GlobalExceptionHandlerTest" --tests "*.SecurityFilterChainTest"
```

Existing frontend selection, from MA's approved isolated frontend snapshot:

```powershell
npm run test -- src/api/client.test.ts src/store/authStore.test.ts src/pages/auth/SocialLoginPage.test.tsx --maxWorkers=2
```

F1 regression scenarios ready for the assigned implementer, **not authored or
run by this read-only WI**: defer A's `fetchMe`, establish B, then resolve/reject
A; assert B tokens/user/continuation unchanged and zero B logout dispatch.
Repeat after local logout and after same-user re-login. Keep normal current
staged-session failure cleanup and StrictMode single-exchange tests. Use fake
OAuth/API promises and a real store, without enabling any provider.

## Risks / Rollback

- F1 prevents an unconditional SEC-04 end-to-end closure. It is conditional on
  a social callback lifecycle, not evidence that deployed OAuth is enabled.
- No further confirmed backend regression found in this bounded review does
  not imply a complete application security audit or production approval.
- Typed-token rollout invalidates old typeless sessions; re-login is an
  intended compatibility impact. Retained access TTL, single-session policy,
  public playback and product entitlements were not redefined.
- H2/MVC/client fake checks do not prove actual MySQL, OAuth, SMTP, browser,
  infrastructure logging or already-dispatched server writes. No cross-tab
  synchronization/cancellation guarantee is added by the in-memory generation.
- This WI has no product/runtime rollback. If directed, revise only its two
  reports. Do not reset the shared worktree, delete data, edit others' evidence,
  or change validation thresholds to close the finding.

## Follow-ups / WI Chain

Return F1 and the test selection to MA for bounded WI004/caller disposition.
WI010 review/report delivery is complete; F1 remediation and final quality
gates are not. Handoff Blocks WI013 and WI014: MA must carry this result and the
corrected snapshot provenance into documentation/integration closure. PG did
not subdelegate, close either downstream WI, or close the approved REQ.

## Related Documents

- [Handoff](WI-20260909-ATS-010-handoff.md)
- [User summary](../user/WI-20260909-ATS-010-summary.md)
- [Approved REQ](../user/REQ-20260909-ATS-001.md)

## 2026-09-09 Re-Review: F1 Entrypoint Fix

Checkpoint: 04:52 KST; MA identified WI004 product stability at 04:45:20.
The preceding review/finding is retained as history, with its original line
numbers and execution bounds. This section supplies the newer disposition.

### Disposition

**F1 remains open for one concrete scoped counterexample, F1-R1 [P2].** The
original deferred exchange/profile success/failure schedules are now guarded;
the residual is the same newer-session preservation contract during staging's
intermediate synchronous notification. It is not a new OAuth feature request
or an unrelated application audit.

### F1-R1 [P2]: Staging Overwrites a Session Created During Its Internal Clear

- Primary pointer: `frontend/src/store/authStore.ts:71-81`, especially `72`
  followed by `74-81`; notification source: the same file at `197-206`.
- Caller pointer: `frontend/src/pages/auth/SocialLoginPage.tsx:69-84` predicts
  the successor generation and checks it only after `stageTokens` returns.
- Trigger: A's still-owned OAuth exchange succeeds. Install a one-shot
  synchronous authStore subscriber that logs in B when `clearSession()`
  publishes `{accessToken: null, user: null}`. A's `stageTokens()` calls that
  clear, the subscriber completes B's login and advances the generation, then
  A's still-running `stageTokens()` resumes writing A's access/refresh tokens
  and `set({accessToken: A})`. The final state is B's user/role/persisted user
  with A's tokens, under B's newer generation. The caller rejects further
  profile/navigation work at line 84, but the credential overwrite has already
  happened. A subscriber that clears again instead similarly has its logout
  followed by A's token write.
- Synchronous dispatch was checked in the installed Zustand implementation:
  `frontend/node_modules/zustand/vanilla.js:6-11` and
  `frontend/node_modules/zustand/esm/vanilla.mjs:4-9` call listeners inline.
- Current test limitation: `SocialLoginPage.test.tsx`'s
  `does not adopt a newer generation triggered synchronously during its own`
  scenario fires only when `state.accessToken === issuedTokens.accessToken`.
  That exercises the final staging/commit notification, not the earlier null
  notification inside the same staging operation. Rejecting a late callback
  after B already existed before staging does not cover this schedule either.
- Evidence level: deterministic source-level counterexample; **not executed
  by PG**. It uses the existing real store and the same synthetic synchronous
  subscriber model as the new tests. No real subscriber-triggered incident,
  enabled OAuth provider, external account, or browser behavior is claimed.

Minimal fixture for MA/WI004, to add to the existing fake-callback/real-store
test setup rather than run against a user session:

```typescript
let armed = true;
const unsubscribe = useAuthStore.subscribe((state) => {
  if (armed && state.accessToken === null && state.user === null) {
    armed = false;
    useAuthStore.getState().login('B-access', replacementUser, 'B-refresh');
  }
});
// Resolve A's fake exchange so the callback calls real stageTokens().
// Expect B user, B access token and B refresh token to remain, and no fetchMe,
// logout or continuation/navigation from A. Release the subscriber in finally.
```

This fixture is a ready reproduction request, not an authored/running test.
The existing final-notification reentrancy tests must remain intact.

### Actual Delta Reviewed

| Boundary | Current source/test evidence | Re-review result |
|---|---|---|
| Old social exchange/profile outcomes | `SocialLoginPage.tsx:42-45,67,83-88,119-120,139-154`; deferred matrix in `SocialLoginPage.test.tsx` | Current run identity, active lifetime and generation checked before staging, commit and cleanup. B login, identical same-user login, local logout and unmount invalidate old continuations. Original asynchronous F1 schedule is blocked. |
| Owned failure cleanup | `SocialLoginPage.tsx:139-154`; deferred logout/new-login scenario | Stale caller cannot begin logout; after owned logout begins, only an expected empty successor session gets error guidance. No further cleanup is applied to a replacement. |
| StrictMode / changed callback | `SocialLoginPage.tsx:33-49,56-61`; deferred complete/incomplete StrictMode and replacement-callback tests | Same-key effect reattaches the existing run without consuming/exchanging twice. New key changes run identity; cleanup makes old continuations inactive. |
| Stage/commit reentrancy | `SocialLoginPage.tsx:69-84,90-120`; synchronous listener tests | Exact expected successor prevents adopting a later generation after final store notification. Intermediate clear notification still has F1-R1. |
| Password entrypoint | `LoginPage.tsx:102-111,152-164,167-213,233-246`; deferred login/profile and overlapping-attempt tests | Mounted lifetime, local attempt sequence and generation guard old commits/errors/finally. PKCE continuation is also guarded. No additional concrete counterexample found in this bounded entrypoint delta. |
| Coverage fixture | `frontend/src/test/coverage/publicAuthShell.coverage.test.tsx:159-162` | Added real generation exports using importOriginal; existing shell-selector mock retained. No assertion or threshold removal in this adaptation. |

Product SHA-256 values read for this counterexample snapshot:

| Path | SHA-256 |
|---|---|
| `frontend/src/pages/auth/SocialLoginPage.tsx` | `E482216F94020510EFF74198AA9799A2E1474B4A75E5531EBD9C553C31BF8E01` |
| `frontend/src/pages/auth/LoginPage.tsx` | `0C3C846E53E84A5407333D01D5173EE7D0658BEF925EF23B07C028FEC224FA36` |
| `frontend/src/store/authStore.ts` | `D2784A7F7C2481D7E5E34113E55F085D7CB3FCF0EE7C12946C60692AD10E2760` |

### Updated Execution Bounds

- PG started no runner and changed no source/test/config. Read WI004 evidence
  v1.2, scoped product/test diffs, installed Zustand notification code and MA
  verification artifacts; appended only the two WI010 reports.
- `frontend-entrypoint-focused.log:108-111`, start 04:49:31: **5 files,
  156 passed / 2 failed / 158 total**, duration 8.80s. Both failures were the
  new owned-storage-failure expected-copy assertions (old snapshot line 493);
  the log displays the existing network guidance instead of the generic
  expected message. This run is not PASS. MA assigned only fixture/expectation
  correction and reported products unchanged; corrected focused run is pending
  at this checkpoint. F1-R1 is not exercised by those two failing assertions.
- MA says the isolated 334-input snapshot was refreshed after product stability.
  Full post-F1 frontend coverage and final gates remain pending. The older
  complete 112-file/1,516-test result in WI004 evidence predates F1 changes;
  the 108-file/1,399-test result remains an incomplete snapshot. Neither is a
  post-F1 final PASS.
- Backend final JSON is a summary object, not the earlier array format:
  `backend-full-final-results.json` reports **1,825 total, 1,806 passed,
  19 skipped, 0 failed, 200 suites**. `backend-full-final.log:1139,1143`
  records `jacocoTestCoverageVerification` and `BUILD SUCCESSFUL in 2m 20s`.
  `backend-coverage-final.json`: LINE 88.5599%, BRANCH 74.4033%, METHOD 86.2736%.
  These are MA executions, not a PG rerun. Per MA, WI001 backend source is
  unchanged since the initial review; no backend re-audit was initiated.

All artifact filenames above resolve under
`output/release-remediation-20260909/`. Existing fake/H2/MVC and no-live-provider,
no-browser validation limits remain unchanged.

### Re-Review Handback

Return F1-R1 to MA/WI004 before closing F1. After its bounded correction and
MA's focused result, re-review only that staging boundary and retain both the
original finding and this dated checkpoint. WI013/WI014 still belong to MA;
this report does not authorize provider enablement or close the REQ.

### 04:54 KST Addendum: Final Focused Execution Confirmed

Read the actual `frontend-entrypoint-focused-final.log`: Vitest 4.1.4,
start **04:53:51**, **5/5 files and 158/158 tests PASS**, duration **9.01s**.
MA executed this against the refreshed isolated 334-input snapshot recorded
in `frontend-final-snapshot.json`, after expected-copy correction and Prettier.
The exact command shown in the log is:

```powershell
vitest run src/api/client.test.ts src/store/authStore.test.ts src/pages/auth/LoginPage.test.tsx src/pages/auth/SocialLoginPage.test.tsx src/test/coverage/publicAuthShell.coverage.test.tsx
```

The earlier 156-pass/2-failure checkpoint remains above as history; its two
copy-expectation failures are resolved in this focused run. The existing
deferred F1 and final-notification reentrancy tests now have executed PASS
evidence, not merely source inspection. PG re-read all three listed product
SHA-256 values at 04:54:46 and they are unchanged from the counterexample
snapshot. **F1-R1 is still open:** this run does not contain the intermediate
null-notification schedule described above. Post-F1 full frontend coverage is
now running under MA, not accepted as complete here.

Report-only checks returned missing metadata 0, trailing whitespace 0 and
broken Markdown links 0 for both WI010 reports; original F1 and dated re-review
sections remain present. Scoped `git diff --check` returned exit 0. These are
static checks, not another compiler/test/documentation-suite execution.

## 2026-09-09 Closure Re-Review: F1-R1, 05:03 KST

**F1-R1 CLOSED for the reviewed source and MA-executed focused regressions.**
The original asynchronous F1 path was guarded in the prior entrypoint fix;
its remaining intermediate-notification counterexample is now closed. No
further concrete counterexample was found at this assigned boundary. Earlier
F1/R1 findings and intermediate execution results above remain historical
evidence; this dated disposition supersedes their open status, not their facts.

### Exact Correction and Identity

`frontend/src/store/authStore.ts:72-75` captures the expected successor before
`clearSession()` and checks it immediately after synchronous subscribers return,
**before either storage write, failure cleanup, or staging commit**. A reentrant
B login, same-user login, or second clear advances past that successor, so the
old stage returns without touching B's credentials. The caller's existing
generation check then suppresses A's profile load, cleanup and navigation.

The R1 product delta is exactly three added lines: expected-generation capture,
an orienting comment and the early return. PG removed those three known lines
only in an in-memory comparison and obtained the exact earlier source SHA-256
`D2784A7F7C2481D7E5E34113E55F085D7CB3FCF0EE7C12946C60692AD10E2760`.
No source file was rewritten by that comparison.

`frontend-final-snapshot.json`, recorded **05:01:02.4007414+09:00**, contains
334 inputs. Its following four hashes exactly match the shared files PG read:

| File under `frontend/src/` | SHA-256 |
|---|---|
| `store/authStore.ts` | `FF1AC52AC54EBD20DB44B479E7E1185F91FDBFF22E740D1C30A84CE49274FA1E` |
| `store/authStore.test.ts` | `1D28E811E139338D45D0791950D680AFD2F41E831DD3EDC7A3D75713DB8072D2` |
| `pages/auth/SocialLoginPage.tsx` | `E482216F94020510EFF74198AA9799A2E1474B4A75E5531EBD9C553C31BF8E01` |
| `pages/auth/SocialLoginPage.test.tsx` | `270EAE635EDA8DEA5119B39004043870176008693333584BD6C43993BAE43262` |

The social entrypoint product remains unchanged from the preceding re-review.
MA reported source stable at 04:58:53; the tested snapshot was recorded later.

### Five New Assertions and Preserved Behavior

- `authStore.test.ts:467-501`: two parameterized cases install the one-shot B
  login on the internal null-state notification and arrange failure of either
  stale access-token or refresh-token persistence. They assert no throw, B's
  token/profile/role persistence intact, and **neither stale write attempted**.
  This also prevents stale storage-failure cleanup from clearing B.
- `SocialLoginPage.test.tsx:444-493`: three cases replace the user, re-login the
  same user, or clear again during that exact intermediate notification. Under
  StrictMode they retain the exact successor store object, all localStorage
  values and the successor return continuation. They assert one exchange,
  zero fetchMe/logout dispatch, and no stale home/profile/complete-profile
  destination. These are the previously missing R1 schedules.
- `authStore.test.ts:451-464` still verifies normal staging and current-owner
  persistence failure clearing/throwing. `SocialLoginPage.test.tsx:495-534`
  retains normal complete/incomplete StrictMode completion; `536-559` retains
  existing owned stage/commit persistence-failure guidance and cleanup. The
  earlier final-notification reentrancy cases remain at `414-442`. The added
  guard does not bypass a failure while staging still owns the generation.

### Executed RED / GREEN, Attributed to MA

| Run | Exact observed result | Scope |
|---|---|---|
| `frontend-r1-red.log`, start 05:00:30 | 2 files, **all 5 selected tests FAIL**, 57 excluded by name filter, 3.57s | Command selects `src/store/authStore.test.ts src/pages/auth/SocialLoginPage.test.tsx -t WI010-F1-R1`. `frontend-r1-red-snapshot.json` records the original D2784A7... source. Failures show A token replacing B/null in the three component cases and stale staging throwing in both store cases. |
| `frontend-r1-green-focused.log`, start 05:01:04 | **5/5 files, 163/163 tests PASS**, 8.43s | Full focused client/store/LoginPage/SocialLoginPage/publicAuthShell coverage selection, no name filter, on the refreshed fixed-source snapshot. Includes the five new cases and retained normal/failure regressions. |

All artifact names in this section resolve under
`output/release-remediation-20260909/`. RED's 57 exclusions are not 57 failing
tests or accepted permanent suite skips. PG read both actual logs and snapshot
metadata; PG did not execute either run or mutate the isolated/shared sources.

### Closure Boundary and Chain

This closes F1 including R1 as an independent scoped source/test review.
Post-R1 full frontend coverage and final quality gates remain with MA and are
not claimed complete here. The earlier 1,516-test pre-F1 and 1,558-test pre-R1
full-suite checkpoints do not substitute for post-R1 gates. The already
recorded backend 1,825 total / 1,806 passed / 19 skipped / 0 failed and coverage
PASS remain separate evidence; no backend work was repeated.

Only WI010's two reports were appended, with no product/test changes, heavy
runner, browser, live OAuth/provider/mail, DB/DDL, secret, runtime or Git write.
Real-store/fake-API and isolated-run scope remains intact. No provider was
enabled, and browser or production behavior is not inferred from Vitest.
Return the closed review to MA for WI013/WI014; parent REQ/release closure still
requires MA's remaining aggregate evidence and explicit disposition.

### 05:05 KST Addendum: Post-R1 Aggregate Evidence Received

R1 remains **CLOSED**, based on the exact failing-original/passing-fixed
counterexample evidence above, now supplemented by the post-R1 aggregate
results. This addendum supersedes the preceding pending-FE-gates statements.
MA reports source unchanged since the 04:58:53 guard checkpoint.

| MA-owned final check | Evidence read by PG |
|---|---|
| Full frontend coverage suite | `frontend-r1-full-final-coverage.log:11-14`: **112/112 files, 1,563/1,563 tests PASS**, start 05:01:28, duration **79.28s**. MA confirms coverage gate PASS. |
| Coverage values | Same log: statements 90.26%, branches 82.83%, functions 91.21%, lines 92.84%. No threshold changes were requested by this review. |
| Typecheck | `frontend-final-typecheck.log` records `tsc --noEmit`; MA confirms PASS. The quiet log does not independently encode exit status. |
| Lint | `frontend-final-lint.log` records ESLint with `--max-warnings 0`; MA confirms PASS. The quiet log does not independently encode exit status. |
| Format | `frontend-final-format.log` reports all matched files use Prettier code style; MA confirms PASS. |
| Build | `frontend-final-build.log` reports built in 2.91s; MA confirms PASS. |

Artifacts resolve under `output/release-remediation-20260909/`. Earlier
incomplete/pre-F1/pre-R1 runs remain historical checkpoints, not replacements
for these current logs. Backend full/coverage evidence remains as recorded;
PG did not rerun it. This completes WI010's bounded F1/R1 review handback with
no open finding at the reviewed boundary. MA retains WI013/WI014 and parent
REQ/release decisions; real OAuth/browser/production validation is not inferred.

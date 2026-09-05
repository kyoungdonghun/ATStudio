# Evidence Pack: WI-20260823-ATS-005

## Work Item

- WI: `WI-20260823-ATS-005`
- REQ: `REQ-20260823-ATS-001`
- Agent: `cr`
- Branch reviewed: `codex/v1-release-rehearsal-fixes`
- Baseline reviewed: `3ea2781`
- Result: **FAIL - remediation required**

## Review Boundary

- Read-only review of the WI-owned code and current-state documentation.
- No source, test, configuration, schema, data, storage, client-worktree, or
  HomePage change was made. The only created files are this evidence pack and
  `deliverables/user/WI-20260823-ATS-005-summary.md`.
- No login, signup submission, protected mutation, payment/refund, mail,
  provider, external call, or media playback was invoked.

## Confirmed Finding

### P2 - BUSINESS requests accept and persist a non-null `job`

**Classification:** Release-blocking for REQ-20260823-ATS-001. The approved
contract specifies `job` as INDIVIDUAL-only; the API must not depend on the
SPA to preserve that invariant.

**Evidence:**

- `src/main/java/com/atstudio/atstudio/common/validation/RegisterProfileValidator.java:19-32`
  and `CompleteProfileValidator.java:19-32` reject a missing `job` for
  `INDIVIDUAL` and a missing `companyName` for `BUSINESS`, but include no
  `BUSINESS && job != null` rejection.
- `src/main/java/com/atstudio/atstudio/service/UserService.java:80-100` calls
  the incomplete registration check then passes `request.getJob()` to the User
  builder. `UserService.java:235-244` follows the same pattern for profile
  completion.
- `UserService.java:136-143,491-503` resolves and validates the supplied job
  for profile update, then applies `request.getJob()` without a BUSINESS-only
  rejection.
- `docs/design/api-spec.md:582-585` defines `job` for INDIVIDUAL and existing
  `companyName` for BUSINESS, making the present server acceptance a contract
  mismatch.
- `src/test/java/com/atstudio/atstudio/service/UserServiceTest.java:347-366,
  685-703,798-824` adds nickname-normalization coverage but no regression case
  for rejecting a BUSINESS `job` in registration, completion, or update.

**Static reproduction:**

1. Construct a valid `RegisterRequest` with `userType=BUSINESS`, a nonblank
   `companyName`, and `job=EDITOR`.
2. `RegisterProfileValidator` returns valid because only an absent BUSINESS
   `companyName` is rejected.
3. `UserService.register` writes `request.getJob()` into the builder.

The same reasoning applies to `CompleteProfileRequest`; an existing BUSINESS
user can also submit `job=EDITOR` through `PUT /api/users/me` and have it
applied. Remediation must choose and document a single behavior: reject
non-null BUSINESS jobs, or normalize them to null consistently before
validation and persistence. Add focused tests for all three write paths.

## Scope And Policy Review

- The working tree is on the required branch. The baseline diff contains the
  WI-owned paths described in WI-001/WI-004 evidence plus separately modified
  `frontend/src/pages/public/HomePage.tsx` and its test. HomePage remains
  excluded from this review.
- No reviewed diff changes schema, data, storage, a real local config file,
  secret, plan capacity, default-playlist timing, or repeat behavior.
- The Drawer request-ID flow, multi-Mood rendering, Play all queue call,
  Like-list entry, and Question FAB changes were reviewed against the REQ and
  current-state documents. No additional concrete defect was found in those
  areas.

## Supplementary Checks Run

| Command | Result |
| --- | --- |
| `git branch --show-current` | PASS: `codex/v1-release-rehearsal-fixes`. |
| `git status --short`; `git diff --name-status 3ea2781`; `git diff --stat 3ea2781` | Reviewed scope; WI paths align with prior evidence and HomePage was identified separately. |
| `git diff --check 3ea2781` | PASS: no whitespace diagnostics. Existing CRLF-to-LF warnings only. |
| `npm run typecheck` | PASS. |
| `npm run lint` | PASS: zero permitted warnings. |
| Scoped `npx prettier --check` over all 23 WI-owned changed frontend files, excluding HomePage | PASS: all matched files formatted. |
| `npm run test -- --reporter=dot` | 110 files / 1,446 tests passed; the only failure is the explicitly excluded HomePage exact-text matcher (1 file / 1 test). |
| `npm run build` | PASS: TypeScript project build and Vite production build completed. |
| `.\gradlew.bat test --rerun-tasks --console=plain` | No result: started, then stopped at the user's instruction not to run a long suite. Only the reviewer-started `test --rerun-tasks` processes were stopped; existing `bootRun` processes were left intact. |

## Residual Validation Limits

- No final backend-suite result and no rerun of documentation validation are
  available in this WI because long suites were explicitly stopped.
- No authenticated browser verification was performed, so visual clearance of
  the protected Question FAB and authenticated player interactions remain
  evidence limits rather than source findings.
- The known development media/storage mismatch and excluded HomePage test are
  outside this REQ.

## Follow-Up

Create a remediation WI that enforces the BUSINESS/INDIVIDUAL job invariant at
the server boundary, adds focused coverage for registration, completion, and
update, then reruns the deferred backend and documentation checks.

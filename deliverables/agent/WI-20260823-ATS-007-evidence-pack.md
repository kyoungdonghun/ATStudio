# Evidence Pack: WI-20260823-ATS-007

## Work Item

- WI: `WI-20260823-ATS-007`
- REQ: `REQ-20260823-ATS-001`
- Agent: `qa-integ`
- Branch: `codex/v1-release-rehearsal-fixes`
- Depends on: `WI-20260823-ATS-006`
- Result: **PASS - REQ remediation is complete.**

## Review Boundary

- Read-only final verification. The only files created by this WI are this
  evidence pack and `deliverables/user/WI-20260823-ATS-007-summary.md`.
- No product source, current-state documentation, configuration, schema, data,
  storage, HomePage file, or client-acceptance worktree was edited.
- No HTTP/browser/API call, login/signup/profile mutation, payment/refund/mail/
  provider action, or media playback was invoked. The focused service tests use
  repository mocks and do not write application data.
- Long full-suite commands were not rerun. Completed full-suite/build evidence
  is cited from WI-004 and WI-006; the newly executed commands below are
  bounded checks.

## Final DoD Matrix

| Requirement | Verdict | Evidence |
| --- | --- | --- |
| BUSINESS direct `job` is rejected on registration | PASS | Request validator: `RegisterProfileValidator.java:26-30`; service boundary: `UserService.java:451-453`; independently rerun test: `UserServiceTest.java:516-528`. |
| BUSINESS direct `job` is rejected on complete-profile | PASS | Request validator: `CompleteProfileValidator.java:26-30`; service boundary: `UserService.java:495-497`; independently rerun test: `UserServiceTest.java:702-714`. |
| BUSINESS direct `job` is rejected on profile update | PASS | Service guard runs before nickname/phone/profile mutation: `UserService.java:122-124`; independently rerun test: `UserServiceTest.java:792-813`. |
| BUSINESS `job=null` and INDIVIDUAL controls remain valid | PASS | BUSINESS controls: `UserServiceTest.java:483-512`, `:681-698`, `:835-866`; INDIVIDUAL required-job controls: `:457-465`, `:819-830`; service validation retains the corresponding rules at `UserService.java:447-457`, `:491-500`, and `:510-516`. |
| Multiple visible Moods and repeated query values persist | PASS | Independent review and public browser/focused-test evidence in `WI-20260823-ATS-002-evidence-pack.md`; current contract remains in `TrackListPage.tsx:232-243,711-719` and `screen-flow.md:103-109`. |
| BUSINESS descriptor and nickname normalization remain aligned | PASS | Current implementation/docs review: `auth.ts:100-130`, `UserService.java:76-90,118-148,231-251`, `api-spec.md:574-585`, and `user-info.md:20-35,151-169,278-292`; prior focused tests passed in WI-002/WI-004. |
| Playlist `Play all`, Likes direct entry/reopen, and Question FAB meet the approved contract | PASS | Focused regression evidence: `WI-20260823-ATS-004-evidence-pack.md` (Drawer/PlayerBar: 87 passed); prior focused implementation/browser evidence: `WI-20260823-ATS-002-evidence-pack.md`; current behavior is documented in `sound-playlist.md:91-97` and `screen-flow.md:123-125,176-179,212-214`. |
| Safe billing-key keyring documentation has no secret or contract drift | PASS | Approved placeholder/keyring-only diff in `application-local.example.yml`; existing V2 documentation review recorded in `WI-20260823-ATS-003-evidence-pack.md`. |
| Scope/policy integrity | PASS | Required branch confirmed; tracked REQ diff contains no schema/data/storage path. `git diff --check 3ea2781` had no whitespace errors. The two HomePage files remain separately excluded. |

## Direct-Path Test Evidence

Executed without external calls:

```text
.\\gradlew.bat test \
  --tests "com.atstudio.atstudio.service.UserServiceTest.register_businessWithJob_throwsInvalidArgument" \
  --tests "com.atstudio.atstudio.service.UserServiceTest.completeProfile_businessWithJob_throwsInvalidArgument" \
  --tests "com.atstudio.atstudio.service.UserServiceTest.updateMyProfile_businessWithJob_throwsInvalidArgument" \
  --rerun-tasks --console=plain
```

- Result: `BUILD SUCCESSFUL` in 27 seconds.
- Parsed JUnit XML: 3 tests, 0 failures, 0 errors, 0 skipped.
- Executed test cases: registration, complete-profile, and update-profile BUSINESS direct payload rejections.

## Quality Evidence

| Check | Result |
| --- | --- |
| `npm run typecheck` | PASS (executed in WI-007). |
| `npm run lint` | PASS with `--max-warnings 0` (executed in WI-007). |
| `npx prettier --check` over the 23 REQ-owned frontend files, excluding both HomePage files | PASS (executed in WI-007). |
| `python .agents/skills/validate-docs/scripts/validate_docs.py` | PASS: Tier 0 files, internal links, 649 traceability IDs, and document index (executed in WI-007). |
| `git diff --check 3ea2781` | PASS: no whitespace diagnostics; existing CRLF-to-LF warnings only. |
| Full backend suite | PASS from WI-006: 1,622 tests, 0 failures/errors, 19 skipped. Not rerun here by instruction. |
| Backend build | PASS from WI-006 after the forced full suite. Not rerun here by instruction. |
| Full frontend suite | PASS except one excluded HomePage assertion from WI-004: 1,446 passed / 1 failed test. The WI-006 change is backend-only; no frontend source changed after that result. |
| Frontend build | PASS from WI-004. Not rerun here by instruction. |

## Excluded Conditions (Not REQ Defects)

### HomePage exact-text test mismatch

- Status: one broad Vitest failure in `frontend/src/pages/public/HomePage.test.tsx`, recorded in WI-004.
- Cause: the test's exact-text matcher does not accept the line-broken rendered
  hero subtitle `창작자를 위한 고품질 라이선스 음악.`
- Classification: excluded HomePage/client-worktree concern, not a defect in
  `REQ-20260823-ATS-001`; neither HomePage file was changed by this REQ chain.

### Development media/storage mismatch

- Status: public browser evidence in WI-002 observed cover fallback; real media
  seek/persistence verification remains unavailable.
- Classification: known development environment/storage condition explicitly
  outside this REQ. No media playback was attempted in WI-007, and no source
  regression is established.

## Reference Documents

| Tier | Document | Purpose |
| --- | --- | --- |
| 0 | `docs/standards/core-principles.md` | Constitution and scope controls. |
| 0 | `docs/standards/development-standards.md` | Backend validation/test conventions. |
| 0 | `docs/standards/documentation-standards.md` | Evidence/document format. |
| 0 | `docs/standards/glossary.md` | Canonical terminology. |
| 1 | `docs/policies/quality-gates.md` | Quality-gate review. |
| 1 | `docs/policies/security-policy.md` | Auth/profile boundary context. |
| 2 | `.agents/skills/react-best-practices/AGENTS.md` | Frontend review guidance. |
| 2 | `docs/standards/frontend-standards.md` | Frontend contract review. |
| 2 | `docs/design/api-spec.md` | User-write contract. |
| 2 | `docs/design/usecase/user-info.md` | Registration/profile semantics. |
| 2 | `docs/design/usecase/sound-playlist.md` | Playlist semantics. |
| 2 | `docs/ui/atstudio-front-list.md` | Current UI inventory. |
| 2 | `docs/ui/screen-flow.md` | Current interaction flows. |
| Prior WI | `WI-20260823-ATS-002` through `WI-20260823-ATS-006` evidence packs | Verified implementation, remediation, documentation, review, and full-suite evidence. |

## Risks / Rollback

- Residual verification limit: authenticated visual workflows were not run under
  the no-login/no-mutation constraint. Their focused tests and prior evidence
  passed; this is not a source finding.
- Rollback: not applicable to WI-007, which made no product changes. Revert
  only the two WI-007 deliverables if their report must be withdrawn.

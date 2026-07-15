# Evidence Pack: WI-20260715-ATS-022

## Summary (one-liner)

- Independent cross-layer verification and all final quality gates pass after targeted Prettier revalidation; the initial formatter failure remains recorded below.

## Scope / DoD Check

- [x] Independently reviewed backend, frontend, security/static-media, Official Download, test, and current-document contracts.
- [x] Verified complete-resource no-Range and full-length Range behavior against API Section 1 and SOUND-010.
- [x] Verified public DTO storage-key redaction and `/uploads/tracks/audio/**` denial coverage.
- [x] Verified first-download Subscription/quota/ledger/License behavior and existing-License re-download behavior.
- [x] Verified Promise-based playback state, fatal media error handling, user-visible feedback, and non-fatal transient buffering.
- [x] Verified active documents contain no stale bounded-preview current prescription.
- [x] Executed every quality command listed by the handoff.
- [x] All required gates pass after targeted revalidation closed the initial changed-file Prettier failure.
- [x] Wrote only the required WI summary and Evidence Pack.

## Reference Documents (Tier 0-2)

**Injected Context** (read in handoff Tier order):

| Tier | Document | Reason |
|------|----------|--------|
| 0 | `docs/standards/core-principles.md` | Constitution and execution authority |
| 0 | `docs/standards/development-standards.md` | Cross-layer review and test evidence standards |
| 1 | `docs/policies/security-policy.md` | Protected Track media and Official Download boundary |
| 1 | `docs/policies/quality-gates.md` | Required validation and evidence rules |
| 2 | `deliverables/user/REQ-20260715-ATS-001.md` | Approved complete-listening restoration scope |
| 2 | `deliverables/agent/WI-20260715-ATS-018-evidence-pack.md` | Backend implementation claims for independent verification |
| 2 | `deliverables/agent/WI-20260715-ATS-019-evidence-pack.md` | Frontend implementation claims for independent verification |
| 2 | `deliverables/agent/WI-20260715-ATS-021-evidence-pack.md` | Current-document alignment claims for independent verification |
| 2 | `docs/design/api-spec.md` | Current Track API contract |
| 2 | `docs/design/usecase/sound-track.md` | SOUND-010 and SOUND-011 contracts |
| 2 | `docs/audit/p0-release-blocker-closure-20260713.md` | Historical protected-media closure and supersession boundary |

Additional mandatory Tier 0 context read: `docs/standards/documentation-standards.md` and `docs/standards/glossary.md`.

**Injection rules applied:** Handoff `deliverables/agent/WI-20260715-ATS-022-handoff.md`; assignee `qa-integ`; task type integration QA; read order Tier 0 -> Tier 1 -> Tier 2 -> source/test files.

## Evidence Pointers

### Complete Public Listening

- `src/main/java/com/atstudio/atstudio/service/TrackService.java:141-161` - active-only lookup, public DTO mapping, original-resource load, and complete resource length.
- `src/main/java/com/atstudio/atstudio/controller/TrackController.java:94-154` - complete no-Range response; one Range resolved against full length; `416` with `bytes */{fullLength}`.
- `src/test/java/com/atstudio/atstudio/service/TrackServiceTest.java:287-333` - original selection regardless of preview metadata or duration, including a one-byte resource.
- `src/test/java/com/atstudio/atstudio/controller/TrackControllerTest.java:165-295` - no-Range, start/end, overlong end, suffix, malformed, multiple, unsupported, unsatisfiable, and open-ended Range coverage.
- `docs/design/api-spec.md:9-17`, `docs/design/api-spec.md:378-432` and `docs/design/usecase/sound-track.md:114-175` - matching current Public Listening and Official Download contracts.

### Redaction and Static Original Denial

- `src/main/java/com/atstudio/atstudio/dto/track/TrackResponse.java:29-35` - public factory forces `audioFile` to `null`; admin factory retains the key.
- `src/main/java/com/atstudio/atstudio/config/SecurityConfig.java:69-83` - public detail/stream routing and explicit static original `denyAll`.
- `src/test/java/com/atstudio/atstudio/controller/TrackControllerTest.java:141-160` - public/admin DTO separation.
- `src/test/java/com/atstudio/atstudio/controller/SecurityFilterChainTest.java:92-130` - anonymous, USER, ADMIN, encoded, and traversal static-path rejection.

### Official Download

- `src/main/java/com/atstudio/atstudio/service/DownloadService.java:40-87` - active Track, existing-License branch, first-download Subscription/quota checks, ledger, License, count, and original load.
- `src/test/java/com/atstudio/atstudio/service/DownloadServiceTest.java:49-239` - first download, License re-download, bounded/unlimited quota, inactive Track, no Subscription, and exceeded-limit coverage.
- `src/test/java/com/atstudio/atstudio/controller/TrackControllerTest.java:297-315` - anonymous download rejection and authenticated controller access.

### Player Outcome State

- `frontend/src/store/playerStore.ts:142-198` - attempt invalidation, Promise outcome state, fatal media error handling, and no fatal `stalled` listener.
- `frontend/src/store/playerStore.ts:217-295` - pending play state, successful-play history, resume, and stop branches.
- `frontend/src/store/playerStore.ts:411-423` - queue clear invalidates pending playback and clears error state.
- `frontend/src/layouts/PlayerBar.tsx:23-67` - playback error subscription and existing toast path.
- `frontend/src/store/playerStore.test.ts:146-307` - Promise resolution/rejection, stale attempts, metadata/time/seek, stalled/error/retry, next-stop, and repeat-one coverage.
- `frontend/src/layouts/PlayerBar.test.tsx:66-83` - user-visible playback error toast.

### Current Documentation

- `docs/design/api-spec.md:9-17` and `docs/design/usecase/sound-track.md:114-175` state complete Public Listening and protected Official Download.
- `docs/policies/security-policy.md:152-159` preserves DTO redaction, static denial, full-length Range semantics, and entitlement separation.
- `docs/design/p0-release-blocker-remediation-design.md:23-68` and `docs/audit/p0-release-blocker-closure-20260713.md:29-62` explicitly label bounded-preview details as historical and superseded.
- Repository Markdown claim scan found bounded-preview matches only in supersession statements, historical records, immutable older WI/REQ evidence, or negative current-contract statements.

## Commands & Outputs

| Command | Exit | Result |
|---------|------|--------|
| `.\gradlew.bat test` | 0 | PASS; Gradle `BUILD SUCCESSFUL`; report: 981 tests, 0 failures, 9 skipped, 1m40.15s |
| `npm run typecheck` in `frontend/` | 0 | PASS; `tsc --noEmit`, no TypeScript errors |
| `npm run lint` in `frontend/` | 0 | PASS; ESLint over `src`, `--max-warnings 0` |
| `npm test` in `frontend/` | 0 | PASS; Vitest 19 files and 79 tests passed; 8.76s |
| `npm run build` in `frontend/` | 0 | PASS; `tsc -b && vite build`; 259 modules transformed; Vite build 3.06s |
| `npx prettier --check src/store/playerStore.ts src/layouts/PlayerBar.tsx src/store/playerStore.test.ts src/layouts/PlayerBar.test.tsx` | 1 initial; 0 revalidation | Initial **FAIL** for `playerStore.ts` and `PlayerBar.tsx`; revalidation PASS for all four files |
| `python .agents/skills/validate-docs/scripts/validate_docs.py` | 0 | PASS; all Tier 0 files present, no broken links, 383 traceability IDs, all documents indexed |
| `git diff --check` | 0 initial; 0 revalidation | PASS both times; no whitespace errors; LF-to-CRLF working-copy notices only |

## Targeted Revalidation

### WI-019 Formatting Evidence Reviewed

- `deliverables/agent/WI-20260715-ATS-019-evidence-pack.md` records Prettier 3.8.1 formatting of exactly `frontend/src/store/playerStore.ts` and `frontend/src/layouts/PlayerBar.tsx`.
- WI-019 records matching pre-write formatter-output and post-write Git-blob hashes, demonstrating that the applied changes were formatting-only:
  - `playerStore.ts`: `8daeff4d228f75f3849846279d664bf300964615`
  - `PlayerBar.tsx`: `529df23c7b7cea4646c2362e4691c321975e0140`
- WI-019 also records a post-format focused Vitest pass of 2 files / 10 tests and scoped ESLint with no warnings or errors. WI-022 inspected this evidence and did not rerun those commands.

### WI-022 Revalidation Commands

- `npx prettier --check src/store/playerStore.ts src/layouts/PlayerBar.tsx src/store/playerStore.test.ts src/layouts/PlayerBar.test.tsx`
  - Exit `0`; output: `All matched files use Prettier code style!`
- `git diff --check`
  - Exit `0`; no whitespace errors; only LF-to-CRLF working-copy notices.
- `git status --short` inspection found the same product/current-document path set as the initial WI-022 run. The only source updates were formatting within the two already-modified frontend files; no new product diff path appeared.
- No full backend suite, frontend typecheck, ESLint, test suite, build, or documentation validation was rerun during revalidation.

### Backend Test Exclusions

- 981 tests were discovered; 972 executed without failure and 9 were skipped.
- `PaymentMysqlConcurrencyIntegrationTest`: 7 skipped because `ATSTUDIO_MYSQL_PROOF_ENABLED=true` was not set.
- `PaymentMysqlSchemaValidationTest`: 1 skipped under the same MySQL proof gate.
- `LocalStorageServiceTest`: 1 skipped because symbolic-link capability was unavailable in the environment.
- Provider behavior remained test-double based and database-backed tests used ephemeral H2. No live Provider or real MySQL call was made.

### Unexecuted Gates

- None of the handoff-listed quality commands were unexecuted.
- Revalidation intentionally did not rerun full suites or documentation validation. It ran only the changed-file Prettier check and `git diff --check`, as requested.

## Exact Decision

- Functional acceptance: **PASS** (5 of 5 functional criteria).
- Initial quality acceptance: **FAIL** (7 of 8 command gates passed; changed-file Prettier exit 1 for `frontend/src/store/playerStore.ts` and `frontend/src/layouts/PlayerBar.tsx`).
- Revalidation quality acceptance: **PASS** (8 of 8 final command gates pass; changed-file Prettier and `git diff --check` both exit 0).
- Overall WI-20260715-ATS-022: **PASS**.
- WI chain: WI-20260715-ATS-023 is unblocked.

## Risks / Rollback

### Risks

- The initial formatter blocker is resolved. Residual confidence relies on WI-019's formatting-only hash guard plus its focused test/ESLint rerun and on WI-022's preserved full-suite results.
- Public complete Track responses can be retained from network traffic; this is the explicit approved REQ boundary and remains separate from Official Download records and License entitlement.

### Rollback

- WI-022 changed only `deliverables/user/WI-20260715-ATS-022-summary.md` and this Evidence Pack. Rollback is deletion of those two QA deliverables only.
- `npm run build` updated tracked `frontend/tsconfig.tsbuildinfo`; it was restored to its clean pre-command baseline. No product rollback is permitted or required.
- Preserve all concurrent source, test, documentation, runtime-log, and unrelated deliverable changes.

## Follow-ups

- WI-20260715-ATS-023 may proceed using this PASS Evidence Pack and the preserved initial/full-gate evidence above.

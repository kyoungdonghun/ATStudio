---
version: 1.0
last_updated: 2026-07-17
project: ATS
owner: qa-integ / cr
category: audit
status: blocked
dependencies:
  - path: ../WI-20260717-ATS-010-handoff.md
    reason: Scope, prohibitions, and output contract
  - path: ../WI-20260717-ATS-001-evidence-pack.md
    reason: Approved 56-item execution ledger
  - path: ../WI-20260717-ATS-008/integration-review.md
    reason: Prior repository-readiness blockers
  - path: ../WI-20260717-ATS-009/frontend-remediation.md
    reason: Latest frontend remediation evidence
  - path: ../WI-20260717-ATS-009/backend-remediation.md
    reason: Latest backend remediation evidence
---

# WI-20260717-ATS-010 Repository Readiness

## Verdict

**BLOCK aggregation. BLOCK cleanup. BLOCK final staging.**

The static repository baseline is internally consistent, all 56 ledger rows are
accounted for, WI-009 generated quality evidence is current relative to its input
files, and no new code/document/schema/route P1/P2/P3 was found. The final gate
cannot pass because current runtime/API/UI smoke is absent, the value-suppressing
secret scan still has unclassified candidate events, and the destructive ref
inventory was not freshly re-enumerated to exact current names immediately before
this report. These are incomplete gates, not inferred passes.

No product source, active documentation, database, Git index/ref, worktree,
runtime process, or prohibited local-secret file was modified or inspected. This
report is the only authorized write.

## Blocking Gates

### GATE-01 - No current runtime/API/UI smoke

- WI-008 already recorded that no current runtime, API, or browser smoke had
  completed (`WI-20260717-ATS-008/integration-review.md:59-69,192-193`).
- The current read-only process check found zero listeners on ports 5173 and 8080
  and zero `cloudflared` processes. There was therefore no prepared runtime to
  inspect without changing state.
- WI-009 changed runtime frontend behavior in `frontend/src/api/client.ts` and
  `DownloadHistoryPage.tsx`; it also changed the payment type contract and router
  comment. Focused/full automated tests cover those changes, but no earlier
  current-tree smoke exists that can be declared still applicable.

**Disposition: BLOCK.** A separately authorized runtime must start from the final
official working-tree/commit, then pass local and public `/`, `/api/tracks`, SPA
deep-link, auth, subscriber download-history, and ADMIN UI/API smoke before
aggregation or auxiliary-worktree retirement.

### GATE-02 - Secret candidates remain unclassified

The scan suppressed all values and reported counts and paths only. The prohibited
ignored local-secret file was excluded before file reads.

| Surface | Scope | Candidate result |
|---|---|---|
| Tracked added lines | 222 diff files; 3,849 added lines | 3 events: 1 classified fixture, 2 unresolved events in `application-local.example.yml` and `src/test/java/com/atstudio/atstudio/security/JwtTokenProviderTest.java` |
| Nonignored untracked text | 113 files inventoried; 103 UTF-8 text files / 986,606 bytes scanned; 10 binary or non-UTF-8 files skipped | 11 events: 1 classified fixture, 10 unresolved events in four paths |

Untracked candidate paths:

- `deliverables/agent/WI-20260717-ATS-004/run-v1-mysql-proof.ps1`
- `deliverables/agent/WI-20260717-ATS-004/V1MysqlProofManager.java`
- `frontend/src/api/authContracts.test.ts`
- `frontend/src/test/coverage/publicAuthShell.coverage.test.tsx`

Patterns covered private-key markers, well-known cloud/provider/token formats,
credential-bearing URIs, credential assignments, and Luhn-valid PAN-like values.
The interrupted classification did not establish whether every remaining event is
only a placeholder, scanner expression, or test fixture.

**Disposition: BLOCK.** Classify these path/line candidates without emitting
values, then require unresolved candidate count `0`. Binary artifacts remain
outside text-scan coverage and must be governed by their hash/ownership evidence.

### GATE-03 - Exact current ref inventory was not refreshed

The current branch and HEAD were verified as
`codex/p1-acceptance-hardening` at
`a96d2e0c5d249723bbf449b6834299a04cf2ad30`. Prior WI-006 evidence recorded
35 prunable registrations, merged cleanup branches, clean auxiliary worktrees,
two rollback tags, and archive tags for three unique tips
(`WI-20260717-ATS-006/integration-review.md:307-317`). This audit stopped before
freshly enumerating every current worktree/ref/tag mapping, so drift cannot be
excluded for destructive authorization.

**Disposition: BLOCK.** Ref deletion requires a same-operation preflight with
the exact inventory and reachability results recorded before mutation.

## Static Verification

### Documentation and parity

| Gate | Current result |
|---|---|
| Documentation validation | PASS: Tier 0 present, zero broken links, 437 supported traceability IDs, complete document index |
| Java/API documentation | PASS: 23 controllers; 137 source mappings and 137 expanded API-spec routes; exact set equality; GET 65, POST 36, PUT 21, DELETE 15, PATCH 0 |
| Schema/JPA/DB documentation | PASS: 39 schema tables, 39 JPA table mappings, 39 documented inventory rows; zero set differences and zero `CREATE TABLE IF NOT EXISTS` |
| Router/UI documentation | PASS: 53 lazy pages, 56 path entries, 1 index redirect; ADMIN comment and child group both 14 |
| WI-009 payment status contract | PASS: backend and frontend expose the same nine `PaymentOrderStatus` values |
| Diff checks | PASS: `git -c core.safecrlf=false diff --check HEAD` and cached diff check both exit 0 |

Focused active-surface residual searches returned zero candidates for the removed
playlist-create route/page, active manual-patch instructions, deleted frontend
standard examples, Thymeleaf base configuration, removed Play History/Download
Queue contracts, preview persistence, and retired one-time/direct-subscription
types. Exact removed provider meanings also returned zero; the retained
`TOSS_BILLING_AUTH` checkout identity is a distinct current contract.

### 56-item ledger

Status is **52 SATISFIED, 4 CLEANUP-READY/PENDING = 56**:

- SATISFIED: `INT-K01` through `INT-K13`; `INT-R01` through `INT-R13`;
  `INT-P01` through `INT-P12`; `INT-A01` through `INT-A03`; `INT-V01` through
  `INT-V09`; `INT-V11`; `INT-V12`.
- CLEANUP-READY/PENDING: `INT-R14`, `INT-R15`, `INT-R16`, `INT-V10`.

The pending classification records ownership only. It does not authorize deletion
while GATE-01 through GATE-03 remain blocked.

### Generated quality evidence

- Backend generated evidence is current: no backend source/test/resource or
  `build.gradle` input is newer than
  `build/reports/jacoco/test/jacocoTestReport.xml`.
- Backend XML aggregation: 158 suites, 1,206 tests, 0 failures, 0 errors, 9
  environment skips. Global coverage is 85.73% lines, 82.93% methods, and 71.68%
  branches against enforced 80/80/70 thresholds.
- All seven governed classes are exactly 100% line/method: `JwtConfig`,
  `CustomUserDetailsService`, `JwtTokenProvider`, `JwtAuthenticationFilter`,
  `AuthRateLimitFilter`, `AuthService`, and `BillingKeyCrypto`. The exact CLASS
  rule is enforced in `build.gradle:76-84,120-134`.
- Frontend generated coverage is current: no frontend source/config/package input
  is newer than the coverage summary or production build. Counters are 86.73%
  statements (6095/7027), 76.98% branches (3626/4710), 85.41% functions
  (1616/1892), and 88.75% lines (5597/6306), above 80/70/80/80. WI-009 records
  63 files and 468 passing tests plus a passing production build
  (`frontend-remediation.md:69-75`).

The fresh heavy suites were not rerun because their generated artifacts are
consistent and no input timestamp is newer. This does not substitute for the
missing runtime smoke.

### Generated and preserved artifacts

- `frontend/tsconfig.tsbuildinfo` exists locally, has zero index entries, is
  narrowly ignored by `frontend/.gitignore:5`, and has SHA-256
  `41F2BDBBF084859EE83F7F00E806340C05CEC6E0EB9778A565060A73AE612EA1`.
  Its sole staged deletion remains intentional.
- Approved generated/temp/runtime directories and four stopped runtime logs are
  absent.
- Historical screenshot ZIP: 52 entries; SHA-256
  `6C56C9139616A7936BA596DED6A2F35A8A29D6762DC210497EECDAE3437B09E8`;
  preserved as nonignored untracked history.
- Client PDF remains tracked with SHA-256
  `AFBA32CCE2460D5D38B80F4A88278E31D1F7344A2258E240BFD61DF74F4C6095`.
- Client PDF manifest remains tracked with SHA-256
  `11A1C91AF1EBF77FBB5CE6B913D3EB197B3AC68D29F2E62B31231C553E0E398D`.
- Historical zones contain 1,127 tracked files and zero tracked modifications or
  deletions. New REQ/WI evidence is additive.

## Bounded Cleanup Targets

Only these target sets may enter a later destructive cleanup packet:

1. `INT-R14`: the 35 prunable `.claude/worktrees/*` registrations and their 35
   corresponding merged `claude/*` local branches from the WI-037 manifest.
2. `INT-R15`: local branches `codex/p0-release-blockers`,
   `codex/payment-integration-clean`, and `dev/kyoung`.
3. `INT-R16`: worktree/branch pairs `codex/acceptance-preview` and
   `codex/client-demo-stable` after verified official-runtime cutover.
4. `INT-V10`: local branch tips `codex-payment-integration-design`,
   `codex-sr-91-tag-taxonomy-layout`, and `master`, only after exact archive-tag
   equality is freshly proven.

The official branch `codex/p1-acceptance-hardening` and rollback tags
`v1-pre-consolidation-dev-20260716` and
`v1-pre-consolidation-client-20260716` are preservation targets, never cleanup
targets. Remote deletion and push remain out of scope.

## Exact Cleanup Preconditions

All conditions must pass in one fresh preflight before any destructive command:

1. Aggregation is PASS, the user has reapproved destructive cleanup, and the V1
   diff has been staged by an explicit allowlist, index-reviewed, and committed on
   the official branch. No broad add is permitted.
2. Record exact official branch, HEAD, status, worktree porcelain inventory,
   local branches, tags, and target commit IDs. The target set must equal the four
   bounded groups above and contain no unrelated ref/path.
3. Both rollback tags resolve and remain ancestors of the official V1 commit.
   Each of the three unique tips must resolve exactly to its named
   `archive/pre-v1-*` tag; do not infer the mapping from a wildcard.
4. `git worktree prune --dry-run` must identify exactly the approved 35 stale
   registrations. Prune metadata before deleting their corresponding branches.
5. Every `claude/*` and ordinary cleanup branch must have zero unique commits
   versus the official branch or an exact preservation tag.
6. Both auxiliary worktrees must be clean, unlocked, fully merged, and absent
   from all process executable/working paths before removal.
7. The official-branch runtime must pass local and public UI/API smoke from the
   final commit. Process ownership and ports 5173/8080 must point only to the
   official runtime before old runtime/worktree removal.
8. After each bounded batch, rerun worktree/ref inventory, tag reachability,
   branch unique-commit checks, docs/residual checks, diff/status checks, and the
   applicable runtime smoke. Stop on any count, hash, path, or reachability drift.

## Repository Snapshot

Pre-report snapshot: 222 tracked diff paths, 3,849 insertions, 17,626 deletions;
one staged deletion, 221 unstaged entries, 113 nonignored untracked files, and 335
status entries after excluding the prohibited local-secret path from displayed
results. The assigned report adds one allowed untracked path after this snapshot.

## Final Authorization

- **WI-011 evidence aggregation: BLOCK.** Resolve GATE-01 through GATE-03 and
  rerun the affected final checks.
- **Branch/tag/worktree cleanup: BLOCK.** The bounded targets are identified, but
  destructive preconditions are not currently complete.
- **Final staging: BLOCK.** Do not broaden the existing index. After all gates
  pass, stage only an reviewed explicit path allowlist and verify the cached diff
  and value-suppressing secret scan before commit.

## Rollback

This WI changed only this report. Rollback is deletion of
`deliverables/agent/WI-20260717-ATS-010/repository-readiness.md`. No product,
documentation, configuration, database, index, ref, tag, worktree, or runtime
rollback applies.

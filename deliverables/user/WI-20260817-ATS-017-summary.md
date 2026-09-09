---
version: 1.0
last_updated: 2026-08-17
project: ATS
owner: cr
category: work-summary
status: complete
related_wi: WI-20260817-ATS-017
dependencies:
  - path: REQ-20260817-ATS-009.md
    reason: Approved release-candidate scope
  - path: WI-20260817-ATS-016-summary.md
    reason: Required guarded MySQL proof predecessor
---

# WI-20260817-ATS-017 Summary

## Review Findings

1. No blocking implementation defect was identified in the independently
   reviewed WI-013 through WI-016 scope. The current source, direct installed
   frontend dependencies, schema/guard contracts, and current-state documents
   agree on the reviewed facts.
2. Release assembly is not yet an atomic staging operation. The working tree
   has 96 tracked changed files and 78 non-output untracked paths, including
   work from REQs and WIs outside REQ-20260817-ATS-009. A named candidate list
   is provided below, but no file was staged, committed, or pushed.
3. This result is repository-release-candidate verification only. It does not
   establish production readiness or authorize any production action.

## Independent Reconciliation

- WI-013: the current registration contract requires affirmative Terms and
  Privacy consent; the frontend transmits all three consent booleans, the
  backend records immutable affirmative consent, and tests cover required and
  optional cases. Current login error handling uses allowlisted messages, and
  logout coalesces calls while distinguishing confirmed `204` revocation from
  local-only cleanup.
- WI-014: the installed dependency tree resolves React 18.3.1,
  ReactDOM 18.3.1, and React Router DOM 7.18.2. No active V6 future prop,
  V7 future flag, or `react-router-dom/server` import remains under
  `frontend/src`.
- WI-015: active current-state documents record 43 source tables/entities and
  the recorded guarded manifest. Historical 42-table material remains labelled
  historical, and the documents retain the non-production boundary.
- WI-016: its completion evidence records accepted initial and final Inventory
  as `count=0` / `NO_POSSIBLE_ORPHAN`. This audit did not rerun Inventory or
  any guarded disposable MySQL lifecycle. Source guards now enforce the 43-table count,
  recorded 43/511/175/91/6/6/0/0 manifest, all-field comparison including the
  recorded SHA-256, and Observe refusal after recording. The current guard
  suite passed all 26 checks.

## Current Verification

| Gate | Current result |
| --- | --- |
| Backend `gradlew.bat --no-daemon check` | PASS; `BUILD SUCCESSFUL` |
| Backend `gradlew.bat --no-daemon test --rerun-tasks` | PASS; 186 suites, 1,614 tests, 0 failures, 0 errors, 19 skipped |
| Frontend `npm run typecheck` | PASS |
| Frontend `npm run lint` | PASS with `--max-warnings 0` |
| Frontend `npm run format` | PASS |
| Frontend `npm test -- --run` | PASS; 111 files, 1,440 tests |
| Frontend `npm run build` | PASS |
| Frontend `npm audit --omit=dev` | PASS; 0 vulnerabilities |
| Bootstrap source guard suite | PASS; 26 checks |
| Documentation validation | PASS; Tier 0 present, internal links valid, 616 supported traceability IDs, index valid |
| `git diff --check` | PASS; 11 CRLF advisory warnings, no whitespace error |

## Operational Release Checklist

### Repository Release Candidate Readiness

- [x] Current code, dependency, schema, guard, and current-state documentation
  claims were reconciled.
- [x] Required local backend, frontend, audit, documentation, and whitespace
  gates passed.
- [x] Accepted WI-016 proof is retained as sanitized evidence; no new guarded
  disposable, retained, remote, provider, refund, or email action was run.
- [ ] Stage only the approved named candidate groups after separating unrelated
  worktree changes and resolving the intended line-ending policy.
- [ ] Create the release-candidate commit and record its resulting commit ID.

### Acceptance Environment Checklist

- [ ] Use a separately controlled acceptance environment with test-only
  accounts and data.
- [ ] Verify frontend/backend health and role-specific signup, verified login,
  unverified-login guidance, confirmed and unconfirmed logout, guarded download,
  and payment-admin non-execution paths.
- [ ] Confirm capability settings and callback origins without printing or
  committing secret values.
- [ ] Treat the completed WI-016 disposable proof as historical evidence only;
  do not rerun a database lifecycle for acceptance without a new explicit
  approval and exact target scope.
- [ ] Record acceptance outcomes separately from repository quality-gate
  outcomes.

### Unclosed External Production Gates

- [ ] Approve a retained-data strategy and any migration/rollback design.
- [ ] Perform an explicitly approved live Toss rehearsal and reconcile the
  persisted local state separately from provider results.
- [ ] Prove SMTP delivery in the intended production configuration.
- [ ] Validate HTTPS, proxy, CORS, and all payment/auth callback origins.
- [ ] Establish and rehearse backups, restore, logging, monitoring, alerting,
  scheduler ownership, and incident procedures.
- [ ] Obtain client acceptance and explicit release approval.

## Unstaged Commit Candidate List

Named release candidate: `RC-20260817-ATS-009-verified-baseline`.

1. `feat(auth): record consent and enforce verified password sessions`
   - Consent model, fresh-schema baseline, registration/login/refresh behavior,
     corresponding SPA flows, API/UI documentation, and focused tests.
   - The untracked `scripts/database/patches/WI-20260809-ATS-068-user-consents.sql`
     is an existing-development-database patch. It is outside this REQ's
     retained-data scope and requires separate traceability and approval before
     inclusion.
2. `fix(frontend): make logout confirmation explicit`
   - `auth` API/store, Header/Admin logout ownership, affected tests, and the
     current UI/API contract text.
3. `chore(frontend): upgrade React Router to 7.18.2`
   - `package.json`, lockfile, router bootstrap, removed future-flag module,
     and the router-harness compatibility hunks. Overlapping test files require
     hunk-level staging.
4. `chore(database): record guarded 43-table disposable manifest`
   - Bootstrap utility, wrapper, guard tests, validate-only proof test, source
     schema baseline, and current database documentation.
5. `docs(release): synchronize V1 current-state boundaries`
   - Current payment, SR, API, UI, registry, and index documentation that
     distinguishes recorded disposable evidence from retained-data and
     production claims.
6. `docs(work): record REQ-20260817-ATS-009 WI evidence`
   - REQ-009 and WI-013 through WI-017 user/agent deliverables only.

These are review candidates, not staging commands. No `git add`, commit,
push, reset, checkout, deletion, provider action, email delivery, or guarded
disposable MySQL lifecycle was performed by WI-017.

## Recommended Next Actions

1. Have the release owner approve the candidate grouping and explicitly decide
   which unrelated untracked work remains out of the release candidate.
2. Execute and document the separate acceptance-environment checklist.
3. Open separately approved REQ/SR work for each external production gate
   before any production release decision.

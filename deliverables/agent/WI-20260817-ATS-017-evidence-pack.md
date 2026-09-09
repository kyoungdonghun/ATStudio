---
version: 1.0
last_updated: 2026-08-17
project: ATS
owner: cr
category: evidence-pack
status: complete
related_wi: WI-20260817-ATS-017
dependencies:
  - path: WI-20260817-ATS-017-handoff.md
    reason: Approved audit contract and prohibitions
  - path: ../user/REQ-20260817-ATS-009.md
    reason: Approved release-candidate scope
  - path: WI-20260817-ATS-016-evidence-pack.md
    reason: Accepted guarded MySQL proof evidence
---

# Evidence Pack: WI-20260817-ATS-017

## Review Findings

- No blocking implementation defect was found in the WI-013 through WI-016
  audit scope.
- Release-process finding: the working tree is not safe to stage as one
  unreviewed unit. It contains 96 tracked changes (1,536 insertions and 561
  deletions) and 78 non-output untracked paths. The untracked set includes
  non-REQ-009 work; this audit defined candidates only and did not stage them.
- Production readiness remains unproven. The retained-data, live-provider,
  SMTP, deployment, backup/monitoring, acceptance, and release-approval gates
  are intentionally open.

## Current-State Reconciliation

| Area | Independent evidence |
| --- | --- |
| Consent and password-session contract | `RegisterRequest.java:50-62`, `UserService.java:75-103,441-469`, `AuthService.java:45-57,99-124`, `UserConsent.java:25-72`, `SignupPage.tsx:56-174`, and current tests align on affirmative Terms/Privacy, optional Marketing, immutable records, and verified-only password login/refresh. |
| Logout/error contract | `frontend/src/api/auth.ts:163-172`, `frontend/src/store/authStore.ts:146-174`, `frontend/src/layouts/Header.tsx:191-211`, `frontend/src/layouts/AdminLayout.tsx:256-276`, and their tests distinguish only `204` as confirmed while clearing the local session on any outcome. |
| Router remediation | `frontend/package.json` pins `react-router-dom` to `7.18.2`; current `npm ls` resolved React 18.3.1, ReactDOM 18.3.1, and React Router DOM 7.18.2. The focused obsolete-pattern search returned 0 matches. |
| Source baseline | Current commands counted 43 `CREATE TABLE` statements in `schema.sql` and 43 direct `@Entity` source files. |
| Recorded manifest guard | `DisposableMysqlBootstrap.java:48-64` records 43/511/175/91/6/6/0/0 and the current SHA-256; `:151-170` enforces the source count and rejects Observe once recorded; `:634-643` compares every field, including SHA-256. |
| Default proof boundary | `PaymentMysqlSchemaValidationTest.java:25-64` requires explicit opt-in plus a loopback disposable target; ordinary runs do not enable it. |
| Current documentation | `docs/design/db-schema.md:19-47`, `docs/payment/system-overview.md:84-95`, `docs/payment/known-limits-and-next-steps.md:45-54`, and `docs/SR/SR-93.md:279-282` agree on the recorded fresh-only 43-table manifest and retain non-production boundaries. |

## WI-016 Evidence Boundary

- WI-016 summary/evidence status is `complete` and records accepted initial and
  final Inventory evidence as `count=0` / `NO_POSSIBLE_ORPHAN`.
- This audit did not execute Inventory, Observe, Create, Validate, Drop, or
  HibernateValidate for the guarded disposable MySQL utility. It accepted the
  sanitized WI-016 evidence and independently reran only the source guard
  suite.
- `scripts/database/test-bootstrap-guards.ps1` passed 26 current checks. Its
  checked contracts include recorded-manifest values, Observe refusal,
  Inventory's fixed single aggregate query/output boundary, loopback/name
  guards, exact-target cleanup, and absence of unrelated database enumeration.

## Quality Gates

| Command | Current result |
| --- | --- |
| `gradlew.bat --no-daemon check` | PASS; `BUILD SUCCESSFUL` (7 actionable tasks: 2 executed, 5 up-to-date) |
| `gradlew.bat --no-daemon test --rerun-tasks` | PASS; 186 XML suites, 1,614 tests, 0 failures, 0 errors, 19 skipped |
| `npm run typecheck` | PASS |
| `npm run lint` | PASS; `--max-warnings 0` |
| `npm run format` | PASS; all matched files formatted |
| `npm test -- --run` | PASS; 111 files, 1,440 tests |
| `npm run build` | PASS |
| `npm audit --omit=dev` | PASS; 0 vulnerabilities |
| `scripts/database/test-bootstrap-guards.ps1` | PASS; 26 checks |
| `python .agents/skills/validate-docs/scripts/validate_docs.py` | PASS; Tier 0 present, links valid, 616 supported traceability IDs, index valid |
| `git diff --check` | PASS; no whitespace error; 11 CRLF advisory warnings |

The forced backend run emitted an unchecked-operations compiler note and a JVM
sharing warning, but its exit code was zero and no test failure/error occurred.

## Release Checklist And Candidate Boundary

The user-facing summary contains the operational checklist separated into
repository release-candidate readiness, acceptance-environment verification,
and external production gates. It is the approval-facing source for this WI.

Unstaged candidate `RC-20260817-ATS-009-verified-baseline` is split into six
named commit candidates: auth/consent, logout ownership, Router 7 upgrade,
recorded disposable-manifest guard, current-state docs, and REQ/WI evidence.
Router compatibility and some coverage files overlap other concerns, so the
candidate requires hunk-level review. The existing-development-only
`WI-20260809-ATS-068-user-consents.sql` patch is excluded from REQ-009 unless
its own approval/traceability is selected.

## Prohibitions And Rollback

- No staged index change, commit, push, reset, checkout, file deletion, branch
  deletion, provider payment/refund, email delivery, external database action,
  or secret/bundle-path inspection occurred.
- This WI changed only its three traceability artifacts. Roll them back by
  removing `WI-20260817-ATS-017-handoff.md`, this evidence pack, and the
  corresponding user summary; no implementation rollback is associated with
  this audit.

## Recommended Follow-Up

1. Approve the hunk/file membership of the named release candidate and record
   the resulting commit ID after a separate staging operation.
2. Run the acceptance-environment checklist with separately controlled test
   data and record outcomes apart from local quality gates.
3. Track retained-data strategy, live Toss, SMTP, HTTPS/CORS/callbacks,
   backup/restore/monitoring, and explicit release approval as separate
   production-readiness work.

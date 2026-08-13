---
version: 1.3
last_updated: 2026-08-14
project: ATS
owner: se
category: evidence-pack
status: complete
dependencies:
  - path: WI-20260809-ATS-053-qa-r2-result.md
    reason: Independent QA R2 PASS closing all three initial QA P2 findings
  - path: WI-20260809-ATS-053-remediation-r2-handoff.md
    reason: Approved QA R2 remediation scope, acceptance criteria, and constraints
  - path: WI-20260809-ATS-053-qa-result.md
    reason: Initial independent QA FAIL and three P2 findings under remediation
  - path: WI-20260809-ATS-053-pg-r2-result.md
    reason: Independent PG R2 PASS closing the session and PII gate
  - path: WI-20260809-ATS-053-remediation-handoff.md
    reason: Approved PG remediation scope, acceptance criteria, and constraints
  - path: WI-20260809-ATS-053-pg-result.md
    reason: Initial independent PG FAIL and PG-053-001 finding authority
  - path: WI-20260809-ATS-053-handoff.md
    reason: Approved scope, DoD, constraints, and traceability contract
  - path: WI-20260809-ATS-031-consolidated-findings.md
    reason: Canonical finding ownership and sequencing
  - path: WI-20260809-ATS-028-findings.md
    reason: Detailed reproduction evidence for the five findings
  - path: ../user/REQ-20260809-ATS-001.md
    reason: Approved correction authority
---

# Evidence Pack: WI-20260809-ATS-053

## Summary (one-liner)

Five ADMIN surface corrections and the three independent QA P2 remediations
have implementation, regression, independent review, and final full-gate
evidence. The initial PG and QA FAIL results remain unchanged as reviewer-owned
history. Independent PG R2 and QA R2 both passed, and WI-053 has no open P0-P3
finding.

## Scope / DoD Check

- [x] `CR-031-094`: `updateUserAdmin` explicitly opts out of centralized ADMIN
      403 synchronization; the page owns the exact typed failure, refreshes identity
      once, applies the canonical route guard, and invokes no mutation retry.
- [x] `CR-031-095`: bounded, accessible, read-only User detail uses only the
      existing DTO and owns loading, error, retry, close, and stale responses.
- [x] `CR-031-096`: License, Question, and Track product code and tests enforce
      latest-request list ownership; License URL identity is canonical.
- [x] `CR-031-098`: ADMIN plan rows show audience and Playlist limits with
      same-name cross-audience evidence and unlimited semantics.
- [x] `CR-031-101`: settings input is frozen during save and success waits for
      the canonical public read consumed by company certification.
- [x] Original focused frontend/backend tests, full frontend coverage,
      typecheck, ESLint, Prettier, production build, documentation validation, and
      diff check passed before independent review.
- [x] PG remediation integration/unit tests, typecheck, ESLint, Prettier,
      documentation validation, and scoped diff check pass.
- [x] `QA-FE-053-001`: License User search is retired on input, selection,
      URL/User context, and unmount changes; publication matches generation,
      normalized keyword, current input, and URL context.
- [x] `QA-FE-053-002`: mobile User rows keep the read-only detail action while
      explicit mobile hiding remains limited to ID, role mutation, and Joined.
- [x] `QA-FE-053-003`: the mobile plan table retains all eight contracted
      fields through horizontal scroll and preserves unlimited Playlist semantics.
- [x] QA R2 focused race/responsive tests, expanded ADMIN regression suite,
      typecheck, ESLint, Prettier, documentation validation, and scoped diff check pass.
- [x] No prohibited schema/data/dependency/policy/branch/deployment or real
      Provider/payment/refund/mail/export/download effect changed or ran.
- [x] Initial independent PG review recorded `FAIL` with open P2
      `PG-053-001`; SE preserved that reviewer-owned result unchanged.
- [x] Independent PG R2 review recorded `PASS` with no open P0-P3 finding.
- [x] Initial independent QA recorded `FAIL` with three open P2 findings; SE
      preserved that reviewer-owned result unchanged.
- [x] Independent QA R2 recorded `PASS`, closed `QA-FE-053-001`,
      `QA-FE-053-002`, and `QA-FE-053-003`, and found no open P0-P3 finding.
- [x] Final MA full gates passed with the exact frontend, backend, coverage,
      build, documentation, and diff results recorded below.
- [x] WI-053 has no open P0-P3 finding and releases WI-054. WI-057 remains
      dependent on its other prerequisites.

## Reference Documents (Tier 0-2)

| Tier | Document                                        | Reason                                                  |
| ---- | ----------------------------------------------- | ------------------------------------------------------- |
| 0    | `docs/standards/core-principles.md`             | Approval, authority, privacy, and traceability baseline |
| 0    | `docs/standards/development-standards.md`       | SE implementation and test baseline                     |
| 0    | `docs/standards/documentation-standards.md`     | Evidence and current-document update baseline           |
| 0    | `docs/standards/glossary.md`                    | Canonical terminology baseline                          |
| 1    | `docs/policies/security-policy.md`              | Secret and server-authorization boundary                |
| 1    | `docs/policies/access-control-policy.md`        | USER/ADMIN authority baseline                           |
| 1    | `docs/policies/quality-gates.md`                | Required verification gates                             |
| 2    | `docs/standards/frontend-standards.md`          | React async ownership and UI contract                   |
| 2    | `.agents/skills/react-best-practices/AGENTS.md` | React implementation guidance                           |
| 2    | `docs/design/api-spec.md`                       | User, settings, and ADMIN API contract                  |
| 2    | `docs/design/usecase/user-info.md`              | User list/detail and role use cases                     |
| 2    | `docs/design/usecase/user-license.md`           | ADMIN License identity/list contract                    |
| 2    | `docs/design/usecase/user-question.md`          | ADMIN Question list contract                            |
| 2    | `docs/design/usecase/user-subscription.md`      | ADMIN plan presentation contract                        |
| 2    | `docs/design/usecase/company-certification.md`  | Public guide consumer contract                          |
| 2    | `docs/payment/admin-operations-guide.md`        | Payment versus local ADMIN operations boundary          |

**Injection Rules Applied**:

- Assignee: `se`
- Task type: frontend/backend correction with security and privacy constraints
- Authority order: approved REQ and handoff, canonical findings, Tier 0/1
  policies, current API/use-case contracts, then implementation.

## Evidence Pointers

| Finding                     | Implementation authority                                                                                                                                                                                                                                    | Regression authority                                                                                                                                                                                                                                          |
| --------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `CR-031-094` / `PG-053-001` | `frontend/src/api/client.ts:7-11,91-102`; `frontend/src/api/admin.ts:82-90`; existing page-owned typed handling in `frontend/src/pages/admin/UserManagePage.tsx`, canonical `frontend/src/store/authStore.ts`, and `frontend/src/router/ProtectedRoute.tsx` | Real interceptor/store/wrapper/page/guard regression at `frontend/src/pages/admin/UserManagePage.integration.test.tsx:111-160`; unit and wrapper contracts at `frontend/src/api/client.test.ts:245-301` and `frontend/src/api/adminContracts.test.ts:122-141` |
| `CR-031-095`                | `frontend/src/api/admin.ts:66-75`; `frontend/src/pages/admin/UserManagePage.tsx:156-204,362-413`; `frontend/src/pages/admin/UserManagePage.module.css`                                                                                                      | `frontend/src/pages/admin/UserManagePage.test.tsx:253-312`; `src/test/java/com/atstudio/atstudio/controller/UserControllerTest.java:87-117`                                                                                                                   |
| `CR-031-096`                | `frontend/src/pages/admin/LicenseManagePage.tsx:22-75`; `frontend/src/pages/admin/QuestionManagePage.tsx:83-178`; `frontend/src/pages/admin/TrackManagePage.tsx:55-147`; `frontend/src/api/tracks.ts:121-137`                                               | ADMIN page tests for License at line 85, Question at line 57, and Track at line 130; API contract tests                                                                                                                                                       |
| `CR-031-098`                | `frontend/src/pages/admin/SubscriptionManagePage.tsx:6-82`                                                                                                                                                                                                  | `frontend/src/test/coverage/adminSubscriberPages.coverage.test.tsx` same-name INDIVIDUAL/BUSINESS fixture                                                                                                                                                     |
| `CR-031-101`                | `frontend/src/pages/admin/SiteSettingsPage.tsx:35-57,87-99`                                                                                                                                                                                                 | `frontend/src/pages/admin/SiteSettingsPage.test.tsx:31-76`; backend Setting controller/service tests                                                                                                                                                          |
| `QA-FE-053-001`             | `frontend/src/pages/admin/LicenseManagePage.tsx` search retirement and publication fence                                                                                                                                                                    | `frontend/src/pages/admin/LicenseManagePage.test.tsx` deferred keyword-edit and canonical-selection races                                                                                                                                                     |
| `QA-FE-053-002`             | `frontend/src/pages/admin/UserManagePage.tsx`; `UserManagePage.module.css` explicit mobile-hidden columns                                                                                                                                                   | `frontend/src/pages/admin/UserManagePage.test.tsx` 767 px open, retry, and close contract                                                                                                                                                                     |
| `QA-FE-053-003`             | `frontend/src/pages/admin/SubscriptionManagePage.module.css` stable table minimum width and horizontal scroll                                                                                                                                               | `frontend/src/test/coverage/adminSubscriberPages.coverage.test.tsx` CSS-source and eight-column mobile contract                                                                                                                                               |

Current contract updates are in `docs/design/api-spec.md:425-435,527-533` and
the finding-specific User Info, License, Question, Subscription, and Company
Certification use-case sections.

QA R2 contract updates are in `docs/design/usecase/user-license.md` for search
retirement, `docs/design/usecase/user-info.md` for the mobile detail action,
and `docs/design/usecase/user-subscription.md` for eight-field mobile scrolling.

The PG remediation ownership boundary is documented in
`docs/policies/security-policy.md:150-159`,
`docs/standards/frontend-standards.md:217-229`,
`docs/design/api-spec.md:533-540`, and
`docs/design/usecase/user-info.md:313-321`.

### Changed Files

- Frontend API/product: `frontend/src/api/admin.ts`, `tracks.ts`;
  `frontend/src/pages/admin/UserManagePage.tsx`, `UserManagePage.module.css`,
  `LicenseManagePage.tsx`, `QuestionManagePage.tsx`, `TrackManagePage.tsx`,
  `SubscriptionManagePage.tsx`, and `SiteSettingsPage.tsx`.
- Frontend tests: `frontend/src/api/adminContracts.test.ts`,
  `domainApis.test.ts`; ADMIN page tests for User, License, Question, Track, and
  Site Settings; `frontend/src/test/coverage/adminSubscriberPages.coverage.test.tsx`.
- Backend tests: `src/test/java/com/atstudio/atstudio/controller/UserControllerTest.java`,
  `SettingControllerTest.java`, and
  `src/test/java/com/atstudio/atstudio/service/SiteSettingServiceTest.java`.
- Current contracts: `docs/design/api-spec.md` and use cases
  `company-certification.md`, `user-info.md`, `user-license.md`,
  `user-question.md`, and `user-subscription.md`.
- WI records: this Evidence Pack and
  `deliverables/user/WI-20260809-ATS-053-summary.md`.

### PG-053-001 Remediation Changed Files

- Frontend implementation: `frontend/src/api/client.ts` and
  `frontend/src/api/admin.ts`.
- Frontend regression: `frontend/src/api/client.test.ts`,
  `frontend/src/api/adminContracts.test.ts`, and new
  `frontend/src/pages/admin/UserManagePage.integration.test.tsx`.
- Current contracts: `docs/policies/security-policy.md`,
  `docs/standards/frontend-standards.md`, `docs/design/api-spec.md`, and
  `docs/design/usecase/user-info.md`.
- WI records: this Evidence Pack and
  `deliverables/user/WI-20260809-ATS-053-summary.md`.

### QA R2 Remediation Changed Files

- Frontend implementation: `frontend/src/pages/admin/LicenseManagePage.tsx`,
  `UserManagePage.tsx`, `UserManagePage.module.css`, and
  `SubscriptionManagePage.module.css`.
- Frontend regression: `frontend/src/pages/admin/LicenseManagePage.test.tsx`,
  `UserManagePage.test.tsx`, and
  `frontend/src/test/coverage/adminSubscriberPages.coverage.test.tsx`.
- Current contracts: `docs/design/usecase/user-license.md`, `user-info.md`, and
  `user-subscription.md`.
- WI records: this Evidence Pack and
  `deliverables/user/WI-20260809-ATS-053-summary.md`.

## Original WI Commands & Outputs

| Gate                   | Command                                                                                                                                                                                                                                                                                         | Result                                                                   |
| ---------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------ |
| Initial RED            | `npm test -- --run src/pages/admin/UserManagePage.test.tsx src/pages/admin/LicenseManagePage.test.tsx src/pages/admin/QuestionManagePage.test.tsx src/pages/admin/TrackManagePage.test.tsx src/pages/admin/SiteSettingsPage.test.tsx src/api/adminContracts.test.ts src/api/domainApis.test.ts` | Expected pre-implementation RED: 7 files; 33 passed, 11 failed; no flake |
| Final focused frontend | Same command                                                                                                                                                                                                                                                                                    | `PASS`: 7 files; 47/47 tests                                             |
| Relevant backend       | `.\gradlew.bat test --tests "com.atstudio.atstudio.controller.UserControllerTest" --tests "com.atstudio.atstudio.controller.SettingControllerTest" --tests "com.atstudio.atstudio.service.SiteSettingServiceTest"`                                                                              | `BUILD SUCCESSFUL`; 3 suites; 16 tests; 0 failures/errors/skipped        |
| Full frontend coverage | `npm run test:coverage`                                                                                                                                                                                                                                                                         | `PASS`: 103 files; 1,329/1,329 tests                                     |
| Coverage thresholds    | Full coverage run                                                                                                                                                                                                                                                                               | Statements 89.74%; branches 82.20%; functions 90.35%; lines 92.27%       |
| Typecheck              | `npm run typecheck`                                                                                                                                                                                                                                                                             | `PASS`                                                                   |
| ESLint                 | `npm run lint`                                                                                                                                                                                                                                                                                  | `PASS`; zero warnings allowed                                            |
| Prettier initial       | `npm run format`                                                                                                                                                                                                                                                                                | `FAIL`: three WI files required formatting; not flaky                    |
| Prettier correction    | `npx prettier --write` on only the three reported files, then `npm run format`                                                                                                                                                                                                                  | `PASS`: all matched files formatted                                      |
| Production build       | `npm run build`                                                                                                                                                                                                                                                                                 | `PASS`; 292 modules transformed                                          |
| Documentation          | `python .agents/skills/validate-docs/scripts/validate_docs.py`                                                                                                                                                                                                                                  | `PASS`: Tier 0, links, 585 IDs, and index                                |
| Diff                   | `git diff --check`                                                                                                                                                                                                                                                                              | `PASS`                                                                   |

The full coverage run emitted the non-failing jsdom message `Not implemented:
navigation to another Document`; all 1,329 tests and coverage thresholds still
passed on that same run.

These original-WI results preceded the initial PG review and were not rerun as
part of the bounded remediation.

## PG-053-001 Remediation Commands & Outputs

| Gate                       | Command                                                                                                                                                                                                                              | Result                                                                                                                                              |
| -------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ | --------------------------------------------------------------------------------------------------------------------------------------------------- |
| Remediation RED            | `npm test -- --run src/api/client.test.ts src/api/adminContracts.test.ts src/pages/admin/UserManagePage.test.tsx src/pages/admin/UserManagePage.integration.test.tsx`                                                                | Expected `FAIL`: 4 files; 42 passed, 3 failed; failures identified missing wrapper option, ignored interceptor opt-out, and missing real PUT config |
| Focused frontend           | `npm test -- --run src/pages/admin/UserManagePage.integration.test.tsx src/pages/admin/UserManagePage.test.tsx src/api/client.test.ts src/api/adminContracts.test.ts src/store/authStore.test.ts src/router/ProtectedRoute.test.tsx` | `PASS`: 6 files; 66/66 tests                                                                                                                        |
| Typecheck                  | `npm run typecheck`                                                                                                                                                                                                                  | `PASS`                                                                                                                                              |
| ESLint                     | `npm run lint`                                                                                                                                                                                                                       | `PASS`; zero warnings allowed                                                                                                                       |
| Prettier initial           | `npm run format`                                                                                                                                                                                                                     | `FAIL`: new integration test required formatting                                                                                                    |
| Prettier correction        | `npx prettier --write src/pages/admin/UserManagePage.integration.test.tsx`, then `npm run format`                                                                                                                                    | `PASS`: all matched frontend files formatted                                                                                                        |
| Markdown format diagnostic | `frontend/node_modules/.bin/prettier --check` with only the 11 remediation implementation, test, doc, and WI paths                                                                                                                   | `FAIL`: four modified Markdown files used the repository's existing non-Prettier Markdown layout                                                    |
| Markdown churn correction  | `frontend/node_modules/.bin/prettier --write` on only the four reported Markdown files, followed by final diff review                                                                                                                | Broad unrelated table/code-example reflow was reverted with `apply_patch`; final Markdown authority is docs validation plus scoped diff check       |
| Documentation              | `python .agents/skills/validate-docs/scripts/validate_docs.py`                                                                                                                                                                       | `PASS`: Tier 0, links, 585 IDs, and index                                                                                                           |
| Scoped diff                | `git diff --check --` with only remediation implementation, tests, docs, and WI records as pathspecs                                                                                                                                 | `PASS`                                                                                                                                              |

## QA R2 Remediation Commands & Outputs

| Gate                    | Command / scope                                                                                                                                                                           | Result                                                                                                                                        |
| ----------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------- |
| QA R2 RED               | `npm test -- --run` with License, User, and ADMIN subscriber coverage tests                                                                                                               | Expected `FAIL`: 3 files; 51 passed, 4 failed; both missing search aborts, hidden mobile detail contract, and missing plan min-width contract |
| First product GREEN     | Same 3-file command after implementation                                                                                                                                                  | `FAIL`: 2 files and 54 tests passed; one coverage test queried the async plan table before loading completed; no product assertion failed     |
| Focused QA R2           | Same 3-file command after correcting the test wait                                                                                                                                        | `PASS`: 3 files; 55/55 tests; 8.64 s                                                                                                          |
| Expanded ADMIN frontend | Independent QA's 12-file focused command covering User integration/unit, License, Question, Track, Settings, API client/contracts, auth store, route guard, and ADMIN subscriber coverage | `PASS`: 12 files; 135/135 tests; 11.92 s                                                                                                      |
| Typecheck               | `npm run typecheck` from `frontend/`                                                                                                                                                      | `PASS`: `tsc --noEmit`                                                                                                                        |
| ESLint                  | `npm run lint` from `frontend/`                                                                                                                                                           | `PASS`: zero warnings allowed                                                                                                                 |
| Prettier                | `npm run format` from `frontend/`                                                                                                                                                         | `PASS`: all matched frontend files use Prettier style                                                                                         |
| Documentation           | `python .agents/skills/validate-docs/scripts/validate_docs.py`                                                                                                                            | `PASS`: Tier 0, no broken links, 585 IDs, and index                                                                                           |
| Scoped diff             | `git diff --check --` with only QA R2 paths, plus `git diff --no-index --check -- NUL <path>` for the three untracked scoped files                                                        | `PASS`: no whitespace errors in tracked or untracked scoped files                                                                             |

The initial RED and the one intermediate test-timing failure were deterministic,
not flaky. The timing correction changed only the assertion from an immediate
table query to `findByRole`; the product implementation was unchanged.

Focused QA R2 command:

```text
npm test -- --run src/pages/admin/LicenseManagePage.test.tsx src/pages/admin/UserManagePage.test.tsx src/test/coverage/adminSubscriberPages.coverage.test.tsx
```

Expanded ADMIN frontend command:

```text
npm test -- --run src/pages/admin/UserManagePage.integration.test.tsx src/pages/admin/UserManagePage.test.tsx src/pages/admin/LicenseManagePage.test.tsx src/pages/admin/QuestionManagePage.test.tsx src/pages/admin/TrackManagePage.test.tsx src/pages/admin/SiteSettingsPage.test.tsx src/api/client.test.ts src/api/adminContracts.test.ts src/api/domainApis.test.ts src/store/authStore.test.ts src/router/ProtectedRoute.test.tsx src/test/coverage/adminSubscriberPages.coverage.test.tsx
```

## Evidence Separation

- **UI behavior:** Vitest/Testing Library verifies modal states, canonical
  route-guard redirection after server-returned `USER`, latest list projection,
  mobile detail open/retry/close, all eight plan cells, horizontal-scroll CSS
  contract, and settings freeze/copy.
- **Request invocation:** the PG remediation integration uses the real Axios
  response interceptor, auth store, ADMIN wrapper, page, and route guard with a
  local adapter. It records one PUT, one `/users/me` GET, zero PUT replays, and
  `skipAdminRoleSync` on the original PUT. Existing mocked API assertions cover
  detail/list signals and settings PUT followed by public GET.
- **Server authorization/response:** MockMvc verifies public Setting GET,
  ADMIN-only Setting PUT, non-ADMIN 403, and the bounded User detail response.
- **Canonical client state:** the integration returns `USER` from the local
  `/users/me` fixture and verifies the auth store and persisted browser snapshot
  both adopt that exact profile before `ProtectedRoute` redirects.
- **Request retirement:** deferred License search tests record abort signals and
  prove that edited-keyword and selected-User responses publish no stale rows or
  dropdown after their ownership is retired.
- **Durable state:** service unit tests verify exact existing-setting update and
  missing-setting insert behavior. No real database or persisted runtime state
  was read or changed; durable production behavior is not claimed.

## Final MA Full Gates

| Gate                      | Final result                                                                                             |
| ------------------------- | -------------------------------------------------------------------------------------------------------- |
| Frontend coverage         | `PASS`: 104 files; 1,334/1,334 tests; statements 89.76%, branches 82.25%, functions 90.36%, lines 92.28% |
| Frontend static gates     | Typecheck, ESLint, and Prettier `PASS`                                                                   |
| Frontend production build | `PASS`: 292 modules transformed                                                                          |
| Backend tests             | `PASS`: 186 suites; 1,606 tests; 0 failures, 0 errors, 19 skipped                                        |
| Backend coverage          | JaCoCo line 87.447%, method 85.088%, branch 72.358%; coverage verification `PASS`                        |
| Backend build             | `PASS`                                                                                                   |
| Documentation             | `PASS`: 585 IDs                                                                                          |
| Diff whitespace           | `git diff --check` `PASS`                                                                                |

The frontend coverage run emitted the non-failing jsdom message
`Not implemented: navigation to another Document`; the run still passed all
1,334 tests and the recorded coverage thresholds.

The backend count was derived from all 186 suite header records because some
stdout content makes full XML-body parsing unreliable. Gradle itself passed.

These final full gates supersede stale statements that full reruns remained
pending. Earlier RED, FAIL, and baseline gate records above remain historical
evidence and are not rewritten as final-run results.

## Risks / Rollback

- The initial PG and QA results remain immutable historical `FAIL` records.
  PG R2 closed `PG-053-001`; QA R2 closed `QA-FE-053-001`,
  `QA-FE-053-002`, and `QA-FE-053-003`. No open P0-P3 finding remains.
- Automated evidence does not establish live-browser visual acceptance or a
  production database result. No live provider or other external effect is
  relevant to this implementation.
- Protected output paths and ignored secret/local environment values were not
  inspected, opened, hashed, modified, staged, or deleted. No external effect
  was executed.
- Rollback is file-level reversion of the changed files listed above. There is
  no schema, data, dependency, Provider, payment, refund, mail, export/download,
  or deployment rollback.

## Follow-up

- WI-053 is complete and releases `WI-20260809-ATS-054`.
- `WI-20260809-ATS-057` remains dependent on its other prerequisites and is not
  released by WI-053 completion alone.

# Evidence Pack: WI-20260716-ATS-003

## Summary (one-liner)

- Independently verified the current-worktree `AT.M` branding and compatibility boundaries, repaired one backend test coverage regression, and identified the reachable stable demo checkpoint as pending runtime refresh.

## Scope / DoD Check

- [x] Classified exact `ATStudio` and case-insensitive variants in active frontend, backend, and seed sources.
- [x] Confirmed current-worktree user-visible branding uses `AT.M` across the approved SPA, email, Toss, and fresh-seed surfaces.
- [x] Confirmed URLs, domains, email addresses, package/class names, Spring/npm names, DB/schema names, environment variables, internal header, and cryptographic associated data are preserved.
- [x] Confirmed seed-source changes do not update existing database rows.
- [x] Passed frontend typecheck, ESLint, focused Vitest, focused backend tests, frontend build, backend build, and diff integrity checks.
- [x] Restored custom Toss refund-reason coverage and added a separate default-reason test.
- [ ] Public client URL displays `AT.M`: the active URL returns HTTP 200 but still serves the pre-refresh stable checkpoint with `<title>ATStudio</title>`.

**Implementation verification:** `PASS`. Source, compatibility, focused regression, and build gates pass.

**Runtime refresh:** `PENDING`. The active public runtime is reachable but has not yet received the verified worktree.

## Reference Documents (Tier 0-2)

| Tier | Document | Reason |
|------|----------|--------|
| 0 | `docs/standards/core-principles.md` | Constitution and approved-scope boundary |
| 0 | `docs/standards/development-standards.md` | Java, React, test, and traceability standards |
| 0 | `docs/standards/documentation-standards.md` | Evidence and documentation rules |
| 0 | `docs/standards/glossary.md` | Canonical WI and domain terminology |
| 1 | `docs/policies/quality-gates.md` | Regression, rollback, and traceability gates |
| 2 | `.agents/skills/react-best-practices/AGENTS.md` | React review guidance |
| 2 | `docs/standards/frontend-standards.md` | Active SPA architecture and validation commands |
| 2 | `docs/design/api-spec.md` | API-contract context |
| 2 | `deliverables/user/REQ-20260716-ATS-001.md` | Approved branding scope and exclusions |
| 2 | `deliverables/agent/WI-20260716-ATS-002-handoff.md` | Implementation contract |
| 2 | `deliverables/agent/WI-20260716-ATS-002-evidence-pack.md` | Implementation evidence and full-suite baseline |
| 2 | `deliverables/agent/WI-20260716-ATS-003-handoff.md` | Independent verification contract |

**Injection order applied:** Tier 0 -> Tier 1 -> Tier 2 -> changed source/tests and live runtime observations; assignee `qa-integ`; task type `integration verification`.

## Findings

### F1 - Reachable client-facing runtime is pending checkpoint refresh

**Status:** Runtime follow-up required

- `Get-NetTCPConnection -State Listen` found active listeners on 5173 and 8080.
- `Get-CimInstance Win32_Process` showed both processes loading from `C:\Users\jm991\Desktop\project\ATStudio-client-demo-stable`.
- `Invoke-WebRequest http://localhost:5173/` returned HTTP 200 with title `ATStudio`.
- Served Vite modules for `Header.tsx` and `HomePage.tsx` reported `old=True`, `new=False`, and file paths under `ATStudio-client-demo-stable`.
- Browser DOM inspection showed `ATStudio` in the header, footer brand, and copyright.
- `Invoke-WebRequest http://localhost:5173/api/tracks?page=0&size=1` returned HTTP 200 JSON, confirming the stale demo's API proxy still works.
- User-browser and direct checks confirmed `https://challenged-efficiently-void-jonathan.trycloudflare.com/` returns HTTP 200 and exposes `<title>ATStudio</title>` from the stable checkpoint.

**Conclusion:** The implementation is verified. The active tunnel is healthy, while deployment of the verified worktree to the stable checkpoint remains pending.

### F2 - Custom Toss refund-reason coverage was lost

**Severity:** Medium test regression, fixed in WI-003

- Before correction, `cancelPaymentSuccess` supplied a blank reason and asserted the new default, replacing its prior custom-reason branch coverage.
- Restored custom pass-through at `src/test/java/com/atstudio/atstudio/service/payment/provider/recurring/TossBillingProviderTest.java:351` and `:398`.
- Added the distinct blank/default case at the same file `:408` and `:439`.
- Focused backend verification passed 60 tests, including both branches.

No production file was modified for this correction.

## Branding Evidence Pointers

### Current user-visible brand

- `frontend/index.html:14` - browser title `AT.M`.
- `frontend/src/layouts/Header.tsx:119` - public header.
- `frontend/src/layouts/AdminLayout.tsx:71` - admin header.
- `frontend/src/pages/auth/LoginPage.tsx:193`, `SignupPage.tsx:175`, `SocialLoginPage.tsx:92` - auth surfaces.
- `frontend/src/pages/public/HomePage.tsx:395` and `:416` - footer and copyright.
- `frontend/src/pages/subscriber/WhitelistChannelPage.tsx:266` - channel-name example.
- `src/main/java/com/atstudio/atstudio/service/EmailService.java:62`, `:105`, `:120`, `:136`, `:236` - email subjects and fallback recipient label.
- `src/main/java/com/atstudio/atstudio/service/PaymentCommandTransactionService.java:157`, `UserSubscriptionService.java:484`, and `service/payment/provider/TossPaymentProvider.java:166` - Toss order display names.
- `src/main/java/com/atstudio/atstudio/service/payment/provider/recurring/TossBillingProvider.java:302` - default refund reason.
- `src/main/resources/seed.sql:492` and `:500` - future initialization notice copy.

### Residual `ATStudio` classification

| Classification | Evidence | Verdict |
|----------------|----------|---------|
| Unintended active display string | Scoped exact/case-insensitive search | None in the current worktree |
| Visible URL/handle examples | `WhitelistChannelPage.tsx:114`, `:274`, `:282` (`@atstudio` and YouTube URL) | Preserve as URL/handle identifiers |
| Internal frontend comment | `frontend/src/types/index.ts:2` | Non-runtime documentation comment |
| Spring application name | `src/main/resources/application.yml:3` | Preserve internal application identity |
| Java class | `src/main/java/com/atstudio/atstudio/AtStudioApplication.java:9`, `:12` | Preserve class identity |
| Java package/import namespace | 400 package declarations and 1,231 imports under `com.atstudio.atstudio` | Preserve compatibility namespace |
| npm package | `frontend/package.json:2`, `frontend/package-lock.json:2`, `:8` | Preserve package identity |
| DB/schema name | `application.yml:11`; `seed.sql:5` | Preserve `atstudio` schema name |
| Environment variables | 73 `${...}` placeholders in `application.yml`, including lines 11-13 and 103-155 | No environment variable rename diff |
| Domain/email identities | `application.yml:106`, `EmailService.java:38`, `seed.sql:113`, `:115`, login redirect fixtures | Preserve addresses and domains |
| Internal HTTP header | `TrustedClientIdentityResolver.java:19` | Preserve `X-ATStudio-Client-IP` |
| Encryption associated data | `BillingKeyCrypto.java:99` | Preserve `ATStudio:` for ciphertext compatibility |
| Fixture/company data | `TestUserBootstrapRunner.java:60-131` and test company/artist fixtures | Not product branding |
| Internal comments | `seed.sql:2`, `BUSINESS_ERROR.java:67` | Non-user-visible comments |

`git diff` contained no changed line for the preserved URL/domain/package/class/Spring/npm/DB/environment/header/associated-data identifiers.

## Seed and Existing-Data Boundary

- The only `seed.sql` product changes are notice title/body defaults at lines 492 and 500.
- Existing rows were not updated; no MySQL, migration, schema, or data-mutation command was run.
- Focused Gradle tests used isolated test schemas and did not access the existing MySQL database.
- Historical notices and Toss orders may retain `ATStudio` by design.

## Commands & Outputs

| Command | Exit | Result |
|---------|------|--------|
| `npm run typecheck` | 0 | PASS |
| `npm run lint` | 0 | PASS, zero warnings |
| `npm test -- src/pages/auth/LoginPage.test.tsx src/pages/auth/SignupPage.test.tsx src/pages/auth/SocialLoginPage.test.tsx` | 0 | PASS, 3 files / 12 tests |
| Focused Gradle test command for the five affected backend classes | 0 | PASS, 60 tests / 0 failed / 0 skipped |
| `npm run build` | 0 | PASS, 259 modules transformed |
| `gradlew.bat build -x test` | 0 | PASS |
| Scoped exact and case-insensitive `rg` searches | 0 | Residuals classified above |
| Preserved-identifier diff search | 0 | No preserved-identifier diff lines |
| `git diff --check` | 0 | No whitespace errors; LF/CRLF notices only |
| Local root, served-module, process, and API checks | mixed gate | Runtime works but serves stale checkpoint branding |
| User-browser and direct active public URL check | current runtime | HTTP 200 with `<title>ATStudio</title>`; refresh pending |

The implementation evidence already records `npm test` as 20 files / 83 tests and `gradlew.bat test` as 984 tests / 9 skipped / 0 failed. Those full suites were not duplicated per the WI-003 instruction.

## Files Changed by WI-003

- `src/test/java/com/atstudio/atstudio/service/payment/provider/recurring/TossBillingProviderTest.java` - restored custom reason coverage and added separate default reason coverage.
- `deliverables/user/WI-20260716-ATS-003-summary.md` - user-facing verdict.
- `deliverables/agent/WI-20260716-ATS-003-evidence-pack.md` - reproducible verification evidence.

## Risks / Rollback

### Risks

- The active public demo will continue to expose stale `ATStudio` branding until the stable checkpoint is refreshed.
- Existing historical DB rows can retain the old brand by design; changing them requires separate approval.
- LF/CRLF conversion notices remain a repository/worktree characteristic, but `git diff --check` found no whitespace error.

### Rollback

1. Revert only the two test additions/restorations in `TossBillingProviderTest.java` if the regression-test correction is withdrawn.
2. Remove the WI-003 summary/evidence with that withdrawal.
3. No product-code, database, schema, URL, ciphertext, or runtime-log rollback is required from WI-003.

## Follow-up

- Refresh `ATStudio-client-demo-stable` from the verified worktree under a separate runtime WI.
- Restart the stable checkpoint frontend/backend only as required for the refresh; retain the active public tunnel unless runtime operations require otherwise.
- Reverify rendered `AT.M` branding and `/api/tracks` through `https://challenged-efficiently-void-jonathan.trycloudflare.com/` after refresh.

## Execution Boundaries

- Product code remained read-only during WI-003.
- Existing database data and runtime logs were not modified.
- Generated `frontend/tsconfig.tsbuildinfo` was restored after the production build.
- No unrelated change was reverted; no file was staged or committed.

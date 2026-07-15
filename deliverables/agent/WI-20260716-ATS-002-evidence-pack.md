# Evidence Pack: WI-20260716-ATS-002

## Summary (one-liner)

- Replaced approved user-visible `ATStudio` branding with `AT.M` across the active SPA, email, Toss, and fresh-seed surfaces without renaming internal identifiers or mutating existing database data.

## Scope / DoD Check

- [x] Public header, admin header, auth screens, home footer, whitelist example, and browser title use `AT.M`.
- [x] Email subjects/default recipient text and new Toss order/refund display strings use `AT.M`.
- [x] Fresh database initialization notice copy uses `AT.M`.
- [x] URLs, domains, email addresses, packages/classes, npm/Spring names, DB/schema identifiers, environment variables, internal headers, and encryption associated data are preserved.
- [x] Existing database rows were not updated.
- [x] Focused and full frontend/backend verification passed, except the documented pre-existing full-tree Prettier baseline.

## Reference Documents (Tier 0-2)

| Tier | Document | Reason |
|------|----------|--------|
| 0 | `docs/standards/core-principles.md` | Constitution and approved-scope boundary |
| 0 | `docs/standards/development-standards.md` | Java, React, testing, and traceability standards |
| 0 | `docs/standards/documentation-standards.md` | Evidence format and documentation language |
| 0 | `docs/standards/glossary.md` | Canonical WI terminology |
| 1 | `docs/policies/quality-gates.md` | Regression, rollback, and traceability gates |
| 2 | `.agents/skills/react-best-practices/AGENTS.md` | React maintenance guidance |
| 2 | `docs/standards/frontend-standards.md` | Active SPA architecture and validation commands |
| 2 | `deliverables/user/REQ-20260716-ATS-001.md` | Approved branding scope and exclusions |
| 2 | `deliverables/agent/WI-20260716-ATS-002-handoff.md` | WI contract, pointers, and output requirements |

**Injection order applied:** Tier 0 -> Tier 1 -> Tier 2 -> source and test files; assignee `se`; task type `implementation`.

## Evidence Pointers

### User-visible SPA branding

- `frontend/index.html:14` - browser title.
- `frontend/src/layouts/Header.tsx:119` and `frontend/src/layouts/AdminLayout.tsx:71` - public and admin headers.
- `frontend/src/pages/auth/LoginPage.tsx:193`, `SocialLoginPage.tsx:92`, and `SignupPage.tsx:175` - auth surfaces.
- `frontend/src/pages/public/HomePage.tsx:395` and `HomePage.tsx:416` - footer brand and copyright.
- `frontend/src/pages/subscriber/WhitelistChannelPage.tsx:266` - channel-name example.

### Email, payment, and seed branding

- `src/main/java/com/atstudio/atstudio/service/EmailService.java:62`, `:105`, `:120`, `:136`, `:236` - subjects and fallback recipient label.
- `src/main/java/com/atstudio/atstudio/service/PaymentCommandTransactionService.java:157` and `UserSubscriptionService.java:484` - upgrade order names.
- `src/main/java/com/atstudio/atstudio/service/payment/provider/TossPaymentProvider.java:166` - new subscription order name.
- `src/main/java/com/atstudio/atstudio/service/payment/provider/recurring/TossBillingProvider.java:302` - default admin refund reason.
- `src/main/resources/seed.sql:492` and `:500` - future initialization notice copy only.

### Regression coverage

- `frontend/src/pages/auth/LoginPage.test.tsx:104`, `SignupPage.test.tsx:114`, `SocialLoginPage.test.tsx:71` - auth branding assertions.
- `src/test/java/com/atstudio/atstudio/service/EmailServiceTest.java:131-210` - all changed email subjects and fallback recipient text.
- `src/test/java/com/atstudio/atstudio/service/payment/provider/TossPaymentProviderTest.java:40-49` - subscription order name.
- `src/test/java/com/atstudio/atstudio/service/payment/provider/recurring/TossBillingProviderTest.java:188-203` and `:349-398` - billing order name and default refund reason.
- `src/test/java/com/atstudio/atstudio/service/SubscriptionUpgradeCommandIntegrationTest.java:142-144` - generated upgrade order name.

## Intentionally Preserved Strings

- `frontend/package.json:2` - npm package `atstudio-frontend`.
- `src/main/resources/application.yml:3` - Spring application name `ATStudio`.
- `src/main/java/com/atstudio/atstudio/security/TrustedClientIdentityResolver.java:19` - `X-ATStudio-Client-IP`.
- `src/main/java/com/atstudio/atstudio/service/payment/billing/BillingKeyCrypto.java:99` - `ATStudio:` associated data.
- `frontend/src/pages/auth/LoginPage.test.tsx:44` - `app.atstudio.com` URL remains unchanged.
- `src/test/java/com/atstudio/atstudio/service/EmailServiceTest.java:46` - `noreply@atstudio.test` address remains unchanged.
- `src/main/java/com/atstudio/atstudio/bootstrap/TestUserBootstrapRunner.java:131` and company/artist test fixtures retain arbitrary `ATStudio` names as required.
- Java package/class names, DB/schema names, environment variables, and internal identifiers have no rename diff.

## Commands & Outputs

| Command | Exit | Result |
|---------|------|--------|
| `npm test -- src/pages/auth/LoginPage.test.tsx src/pages/auth/SignupPage.test.tsx src/pages/auth/SocialLoginPage.test.tsx` before implementation | 1 | Expected red: 3 brand assertions failed, 9 tests passed |
| Focused Gradle command for five affected backend test classes before implementation | 1 | Expected red: 59 tests, 7 brand assertions failed |
| Same focused frontend command after implementation | 0 | PASS, 3 files / 12 tests |
| Same focused backend command after implementation | 0 | PASS, 59 tests |
| `npm run typecheck` | 0 | PASS |
| `npm run lint` | 0 | PASS, zero warnings |
| `npm test` | 0 | PASS, 20 files / 83 tests |
| `npm run build` | 0 | PASS, 259 modules transformed |
| `gradlew.bat test` | 0 | PASS, 984 tests / 9 skipped / 0 failed |
| `npm run format` | 1 | Existing full-tree baseline: 140 files reported; no `--write` executed |
| Scoped `rg` searches for `ATStudio`, preserved identifiers, and `AT.M` | 0 | Remaining old-name matches classified as approved identifiers/comments/fixtures |
| `git diff --check` | 0 | No whitespace errors; Git emitted line-ending notices only |

## Risks / Rollback

### Risks

- Existing notice rows and historical Toss orders can still contain the old brand by design; this WI changes only source defaults for future records and orders.
- Full-tree Prettier remains a repository baseline issue and was not bulk-fixed because that would exceed the approved scope.
- Public client URL confirmation is intentionally assigned to `WI-20260716-ATS-003`.

### Rollback

1. Revert only the listed product and regression-test diffs if the display-brand change is withdrawn.
2. Revert the two WI-002 deliverables with the implementation.
3. No database rollback, schema rollback, key migration, URL rollback, or ciphertext migration is required because none was performed.

## Execution Boundaries

- No database update/migration command was run; Gradle tests used test-isolated schemas only.
- Generated `frontend/tsconfig.tsbuildinfo` was restored to its pre-work state.
- Pre-existing Cloudflare/Vite logs, REQ/WI handoffs, and unrelated working-tree files were left untouched.
- No file was staged or committed.

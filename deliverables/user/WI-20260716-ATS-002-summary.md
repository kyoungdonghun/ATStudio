# WI-20260716-ATS-002 Summary

## Decision

**PASS** - User-visible product branding in the approved React, email, Toss, and fresh-seed surfaces now uses exactly `AT.M` while internal identifiers and existing database data remain unchanged.

## Changes

- Updated the browser title, public/admin headers, login/social-login/signup copy, home footer, and whitelist example to `AT.M`.
- Updated email subjects and the subscription-email fallback recipient label to `AT.M`.
- Updated new Toss subscription/upgrade order names and the default admin refund reason to `AT.M`.
- Updated only the notice text in `seed.sql` for future database initialization; no database command or existing-row update was executed.
- Added or strengthened focused frontend and backend regression coverage for the changed branding contracts.

## Verification

| Check | Result |
|------|--------|
| Focused frontend Vitest | PASS, 3 files / 12 tests |
| Focused backend Gradle tests | PASS, 59 tests |
| Frontend typecheck | PASS |
| Frontend ESLint | PASS |
| Full frontend Vitest | PASS, 20 files / 83 tests |
| Frontend production build | PASS, 259 modules transformed |
| Full backend Gradle test | PASS, 984 tests / 9 skipped / 0 failed |
| Scoped `ATStudio` classification | PASS; remaining matches are approved identifiers, comments, or company/artist fixtures |
| Diff integrity | PASS |
| Full frontend Prettier check | Existing baseline issue: 140 files reported; no formatting write was performed |

## Boundaries Preserved

- Preserved URLs, domains, email addresses, Java packages/classes, the npm package name, Spring application name, database/schema identifiers, environment variables, and internal HTTP headers.
- Preserved `BillingKeyCrypto` associated data beginning with `ATStudio:` for ciphertext compatibility.
- Preserved arbitrary `ATStudio Biz`, `ATStudio QA Biz`, and `ATStudio` company/artist test fixtures.
- Preserved existing database rows. Only the future initialization source in `seed.sql` changed.
- Restored generated `frontend/tsconfig.tsbuildinfo` to its pre-work state.
- Runtime logs and unrelated REQ/WI files were not modified, staged, committed, or reverted.

## Changed Files

- Frontend product: `frontend/index.html`, `frontend/src/layouts/AdminLayout.tsx`, `frontend/src/layouts/Header.tsx`, `frontend/src/pages/auth/LoginPage.tsx`, `frontend/src/pages/auth/SignupPage.tsx`, `frontend/src/pages/auth/SocialLoginPage.tsx`, `frontend/src/pages/public/HomePage.tsx`, `frontend/src/pages/subscriber/WhitelistChannelPage.tsx`
- Frontend tests: `frontend/src/pages/auth/LoginPage.test.tsx`, `frontend/src/pages/auth/SignupPage.test.tsx`, `frontend/src/pages/auth/SocialLoginPage.test.tsx`
- Backend product: `src/main/java/com/atstudio/atstudio/service/EmailService.java`, `src/main/java/com/atstudio/atstudio/service/PaymentCommandTransactionService.java`, `src/main/java/com/atstudio/atstudio/service/UserSubscriptionService.java`, `src/main/java/com/atstudio/atstudio/service/payment/provider/TossPaymentProvider.java`, `src/main/java/com/atstudio/atstudio/service/payment/provider/recurring/TossBillingProvider.java`, `src/main/resources/seed.sql`
- Backend tests: `src/test/java/com/atstudio/atstudio/service/EmailServiceTest.java`, `src/test/java/com/atstudio/atstudio/service/SubscriptionUpgradeCommandIntegrationTest.java`, `src/test/java/com/atstudio/atstudio/service/UserSubscriptionServiceTest.java`, `src/test/java/com/atstudio/atstudio/service/payment/provider/TossPaymentProviderTest.java`, `src/test/java/com/atstudio/atstudio/service/payment/provider/recurring/TossBillingProviderTest.java`
- Deliverables: `deliverables/user/WI-20260716-ATS-002-summary.md`, `deliverables/agent/WI-20260716-ATS-002-evidence-pack.md`

Public client URL verification remains the responsibility of the blocked follow-up `WI-20260716-ATS-003`. No file was staged or committed.

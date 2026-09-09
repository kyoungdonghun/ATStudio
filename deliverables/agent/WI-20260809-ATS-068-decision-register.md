---
version: 1.0.1
last_updated: 2026-08-16
project: ATS
owner: DocOps
category: agent
status: accepted
dependencies:
  - path: deliverables/user/REQ-20260809-ATS-001.md
    reason: Approved parent requirement and execution boundary
  - path: deliverables/agent/WI-20260809-ATS-068-handoff.md
    reason: Approved WI scope, acceptance criteria, and non-execution boundary
  - path: deliverables/agent/WI-20260809-ATS-068-qa-integ-r2-review.md
    reason: R2 finding that required this missing traceability input
---

> Historical public derivative, published in this working tree on 2026-09-09 by WI-20260909-ATS-018. Original dates, language, status, decisions and reported outcomes below are historical, not current approval or reverified recovery/production evidence. The original remains unchanged in private archive `v1-artifacts-20260909-084405`. See [A001: original identity, public SHA-256 and review](../../docs/registry/development-history-recovery-20260909.md#a001). Raw/local-only evidence pointers are lookup keys, not files supplied by a source checkout.

# WI-20260809-ATS-068 Decision Register

> Purpose: Restore the accepted consent, password-session, logout,
> database-evidence, and social-scope decisions required to reconcile WI-068
> evidence. This record introduces no new product decision or runtime change.

## 1. Authority And Status

- WI: `WI-20260809-ATS-068`
- Parent REQ: `REQ-20260809-ATS-001`, approved.
- Decision status: accepted transcription of the approved WI contract and its
  verified repository implementation evidence.
- QA status: the 2026-08-16 R2 review remains `CONDITIONAL PASS`. It identified
  this missing file as a required input; this restoration does not assert a
  later QA verdict.
- Scope: documentation traceability only. No product source, SQL, schema,
  configuration, test, database, external service, browser, or Git state is
  changed by this record.

## 2. Accepted Decisions

| ID | Decision | Required behavior and boundary |
| --- | --- | --- |
| DG-068-01 | Separate signup consent | Terms of Service and Privacy Collection/Use are separate required affirmative consents. Marketing is optional and independent; declining it does not block password signup. |
| DG-068-02 | Minimal durable consent ledger | Each affirmative record stores the durable user ID, consent category, server-owned policy version, and server-generated agreement timestamp. The ledger deliberately excludes IP addresses and device fingerprints. Legal policy text, URLs, and their governance are separate future legal/product work and are not invented here. |
| DG-068-03 | Signup payload and persistence | The signup client always submits `termsAgreed`, `privacyAgreed`, and `marketingAgreed`. The backend records Terms and Privacy only after their required validation succeeds, and records Marketing only when its submitted boolean is `true`. |
| DG-068-04 | Verified password session | Password login and refresh reject an unverified account before access or refresh token issuance or rotation. Successful password signup creates no SPA password session and routes to email-verification guidance. |
| DG-068-06 | Logout outcome | Logout uses one coalesced `POST /api/auth/logout`. Only `204` confirms server revocation. A bare `401` or any other failure still clears local state and produces the fixed explicit unconfirmed-revocation warning. |
| DG-068-07 | Database-evidence and social boundary | `user_consents` DDL and its WI patch are source/patch evidence only; the patch is unapplied. The current source baseline is 43 tables and 43 direct `@Entity` types, while the live/disposable MySQL manifest is `UNRECORDED`; no real MySQL evidence was performed under WI-068. Social/OAuth lifecycle remains explicitly outside WI-068 and unchanged. |

## 3. Repository Reconciliation

### 3.1 Consent

- `RegisterRequest` requires affirmative `termsAgreed` and `privacyAgreed`, and
  exposes optional `marketingAgreed`.
- `UserService.register` validates required consent before persisting the user,
  then records the accepted consent records in the same transaction.
- `UserConsent` is immutable and stores only user association, consent type,
  policy version, and agreement time beyond its technical primary key.
- `ConsentPolicyProperties` selects the version from server configuration for
  each consent type. The record time is server-generated.

### 3.2 Password Session

- `AuthService.login` verifies the password user before generating access or
  refresh tokens. `AuthService.refresh` repeats that check before rotation.
- `SignupPage` sends all three consent booleans, then routes successful
  registration to `/email-verify` without calling the auth-store login path.

### 3.3 Logout

- `logoutSession` maps only an HTTP `204` response to `confirmed`; all thrown
  requests, including a bare `401`, are `unconfirmed`.
- `authStore.logout` coalesces calls through one in-flight promise, clears local
  session state in `finally`, and returns the confirmation outcome to callers.
- Header and Admin layout callers await that outcome, suppress duplicate clicks,
  and display the fixed warning only when revocation is unconfirmed.

### 3.4 Database And Social Scope

- `schema.sql` contains 43 `CREATE TABLE` statements; source has 43 direct
  `@Entity` declarations. This is static repository evidence, not MySQL
  execution evidence.
- The fresh schema and
  `WI-20260809-ATS-068-user-consents.sql` define the same `user_consents`
  contract. The patch was not applied under this WI.
- `DisposableMysqlBootstrap` expects 43 source tables and keeps its MySQL
  manifest expectation `UNRECORDED`, refusing Create and Validate before a
  connection. No live or disposable MySQL observation, validation, or patch
  application was performed.
- `AuthService.socialLogin` and the social/OAuth lifecycle were outside the
  approved WI scope and remain unchanged.

## 4. Evidence Pointers

- `src/main/java/com/atstudio/atstudio/config/ConsentPolicyProperties.java:12-27`:
  server-owned consent policy versions.
- `src/main/java/com/atstudio/atstudio/entity/UserConsent.java:26-72` and
  `src/main/java/com/atstudio/atstudio/entity/enums/UserConsentType.java:3-7`:
  immutable consent-ledger shape and categories.
- `src/main/java/com/atstudio/atstudio/dto/user/RegisterRequest.java:59-67` and
  `src/main/java/com/atstudio/atstudio/service/UserService.java:76-107,444-464`:
  required-consent validation, transactional registration, and affirmative-only
  Marketing persistence.
- `src/main/java/com/atstudio/atstudio/service/auth/AuthService.java:39-56,78-123`
  and `src/main/java/com/atstudio/atstudio/common/exception/BUSINESS_ERROR.java:229-232`:
  verified-password-session gate before token issuance or rotation.
- `frontend/src/pages/auth/SignupPage.tsx:163-177` and
  `frontend/src/pages/auth/EmailVerifyPage.tsx:19-119`: all-three payload and
  verification guidance without a password session.
- `frontend/src/api/auth.ts:163-171`,
  `frontend/src/store/authStore.ts:149-170`,
  `frontend/src/layouts/Header.tsx:194-207`, and
  `frontend/src/layouts/AdminLayout.tsx:259-272`: coalesced server-first logout,
  local cleanup, and fixed unconfirmed warning.
- `src/main/resources/schema.sql:113-125` and
  `scripts/database/patches/WI-20260809-ATS-068-user-consents.sql:1-17`:
  source-only `user_consents` schema and unapplied patch.
- `scripts/database/DisposableMysqlBootstrap.java:49-51,146-150,551-566`:
  43-table source expectation and `UNRECORDED` manifest guard.
- `deliverables/agent/WI-20260809-ATS-068-qa-integ-r2-review.md:9-20,56-62,105-112`:
  independent R2 source/manifest boundary and the missing-register finding.

## 5. Non-Execution, Rollback, And Follow-Up

- Not run: patch application; real or disposable MySQL activity; database
  bootstrap action; external mail; OAuth/social interaction; Provider, payment,
  or refund calls; browser tests; full suite; and Git stage, commit, push, or
  history mutation.
- Rollback: this is a documentation-only restoration. Correct a decision only
  through a superseding approved REQ/WI decision; no database rollback exists
  because no patch was applied.
- Follow-up: a separately approved database-evidence WI is required before
  recording a current MySQL manifest or executing the existing patch. Legal
  owners must separately provide authoritative policy text, URLs, and
  governance decisions.

## 6. Related Documents

- [Approved REQ](../user/REQ-20260809-ATS-001.md)
- [WI-068 Handoff](WI-20260809-ATS-068-handoff.md)
- [WI-068 Evidence Pack](WI-20260809-ATS-068-evidence-pack.md)
- [WI-068 User Summary](../user/WI-20260809-ATS-068-summary.md)
- [WI-068 QA Integration Review](WI-20260809-ATS-068-qa-integ-review.md)
- [WI-068 QA Integration R2 Review](WI-20260809-ATS-068-qa-integ-r2-review.md)

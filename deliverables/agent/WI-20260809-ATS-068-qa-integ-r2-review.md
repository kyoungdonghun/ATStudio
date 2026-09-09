
> Historical public derivative, published in this working tree on 2026-09-09 by WI-20260909-ATS-018. Original dates, language, status, decisions and reported outcomes below are historical, not current approval or reverified recovery/production evidence. The original remains unchanged in private archive `v1-artifacts-20260909-084405`. See [A005: original identity, public SHA-256 and review](../../docs/registry/development-history-recovery-20260909.md#a005). Raw/local-only evidence pointers are lookup keys, not files supplied by a source checkout.

# QA Integration R2 Review: WI-20260809-ATS-068

**Reviewer:** QA Integration (independent R2 review)<br>
**Review date:** 2026-08-16<br>
**Verdict:** CONDITIONAL PASS

## Verdict Basis

The prior P2 documentation corrections are present and agree with the current
repository source. The current source baseline is 43 `CREATE TABLE`
statements and 43 `@Entity` types; the active live/disposable MySQL manifest
expectation is `UNRECORDED`; and the WI-067 42/506/173/90/6/hash values are
consistently labelled historical evidence for the superseded 42-table source
snapshot only. The two UI documents now accurately distinguish always sending
the boolean `marketingAgreed` from persisting a Marketing-consent record only
when the value is `true`.

A full PASS is not available because the required input
`deliverables/agent/WI-20260809-ATS-068-decision-register.md` is absent in the
current working tree. Its required R2 review evidence could therefore not be
read or independently reconciled.

## Findings

### P0

None.

### P1

None.

### P2

1. **Required R2 decision-register input is missing.**
   `deliverables/agent/WI-20260809-ATS-068-decision-register.md` did not
   exist at review time. This is a traceability gap in the explicitly required
   review input, not evidence that its undocumented decisions were correct.
   Add or restore the decision register, then perform a focused evidence
   reconciliation before upgrading this verdict to PASS.

### P3 / Residual Boundaries

1. **No actual 43-table MySQL manifest is recorded.** This is intentional:
   `DisposableMysqlBootstrap` requires 43 source tables and has an active
   `UnrecordedMysqlManifestExpectation`. The current repository therefore
   does not claim a current MySQL DDL application, observation, or validation.

2. **Focused test results were not rerun in R2.** The prior evidence pack's
   focused backend/frontend test results are historical evidence. This R2
   review used current test source as contract evidence and ran only the
   permitted documentation, diff, and static bootstrap-guard checks.

## Cross-Check Evidence

### Current Baseline And Historical Manifest Boundary

- Static source count: `schema.sql` has 43 `CREATE TABLE` statements and the
  entity source has 43 `@Entity` types. The active bootstrap constants agree
  (`scripts/database/DisposableMysqlBootstrap.java:49-51`).
- The bootstrap's `UNRECORDED` expectation is explicit and cannot match a
  manifest (`scripts/database/DisposableMysqlBootstrap.java:551-567`). The
  passing static guard confirms Create/Validate preflight refusal before
  credentials or a connection.
- Current-state documentation agrees: `docs/index.md:71`,
  `docs/registry/project-registry.md:42-43`, `docs/design/index.md:29`,
  `docs/design/db-schema.md:25,50-60`, and `scripts/database/README.md:10-11,
  53-64`.
- The historical labels correctly preserve, but do not reactivate, WI-067
  42/506/173/90/6/hash evidence in
  `docs/design/payment-settlement-import-design.md:386-402`,
  `docs/payment/admin-operations-guide.md:264-273`, and
  `docs/design/payment-refund-receipt-settlement-policy.md:492-500`. The
  original WI-067 evidence records those values and its exhausted
  `RUN-PASS-CLEANED` scope (`deliverables/agent/WI-20260809-ATS-067-evidence-pack.md:146-151,181-186`).

### Consent And Password-Session Contract

- The client always sends `termsAgreed`, `privacyAgreed`, and
  `marketingAgreed` (`frontend/src/pages/auth/SignupPage.tsx:163-175`). Server
  validation requires affirmative Terms and Privacy
  (`src/main/java/com/atstudio/atstudio/service/UserService.java:444-449`),
  then records Marketing only for `Boolean.TRUE`
  (`src/main/java/com/atstudio/atstudio/service/UserService.java:451-464`).
  Current UI documentation matches this at
  `docs/ui/atstudio-front-list.md:57-63` and
  `docs/ui/screen-flow.md:62-70`.
- The fresh-schema `user_consents` DDL and unapplied WI patch are byte-equal;
  the schema source is at `src/main/resources/schema.sql:113-125` and the
  patch is `scripts/database/patches/WI-20260809-ATS-068-user-consents.sql:4-16`.
- Current tests cover required-consent rejection and affirmative-record
  creation (`UserServiceTest.java:311-373`) and UI separate-consent submission
  (`SignupPage.test.tsx:226-275`).
- Password login checks verification before access/refresh-token generation
  (`AuthService.java:48-56`); refresh checks it before rotation
  (`AuthService.java:91-112`). Focused tests assert no token generation for
  unverified login or refresh (`AuthServiceTest.java:78-99,296-315`). The SPA
  test confirms `EMAIL_VERIFICATION_REQUIRED` invokes neither `fetchMe` nor
  auth-store login (`LoginPage.test.tsx:266-285`).

## Commands And Outcomes

| Command | Outcome |
| --- | --- |
| `python .agents/skills/validate-docs/scripts/validate_docs.py` | PASS - Tier 0, internal links, 587 traceability IDs, and index. |
| `git diff --check` | PASS (exit 0). Existing CRLF-to-LF advisory warnings only. |
| `powershell -NoProfile -ExecutionPolicy Bypass -File .\\scripts\\database\\test-bootstrap-guards.ps1` | PASS - 20 static/preflight checks; no MySQL connection or credential load. |
| Static source count and DDL equality review | PASS - 43 tables, 43 entities, and byte-equal `user_consents` DDL. |

## Non-Execution Boundary

Not run: SQL patch application; database bootstrap `Create`, `Observe`,
`Validate`, or `Drop`; real MySQL; external mail; OAuth/social interaction;
Provider, payment, or refund calls; browser tests; full suite; and Git
stage/commit/push or history operations. No database credentials or secrets
were inspected. No product source, SQL, configuration, database, or test file
was modified by this review.

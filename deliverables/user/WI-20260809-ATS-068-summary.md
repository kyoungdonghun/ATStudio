---
version: 1.1.0
last_updated: 2026-08-16
project: ATS
owner: docops
category: work-summary
status: complete
dependencies:
  - path: ../agent/WI-20260809-ATS-068-evidence-pack.md
    reason: Final evidence chain and current technical boundaries
  - path: ../agent/WI-20260809-ATS-068-decision-register.md
    reason: Accepted in-scope decision transcription
  - path: ../agent/WI-20260809-ATS-068-qa-integ-r4-review.md
    reason: Independent final closure PASS
---

> Historical public derivative, published in this working tree on 2026-09-09 by WI-20260909-ATS-018. Original dates, language, status, decisions and reported outcomes below are historical, not current approval or reverified recovery/production evidence. The original remains unchanged in private archive `v1-artifacts-20260909-084405`. See [A073: original identity, public SHA-256 and review](../../docs/registry/development-history-recovery-20260909.md#a073). Raw/local-only evidence pointers are lookup keys, not files supplied by a source checkout.

# WI-20260809-ATS-068 Summary

## Execution Status

**Complete** at focused repository scope. The independent QA Integration R4
closure is `PASS`, with no P0, P1, or P2 finding in WI-068 scope. This is not a
deployment or production-readiness claim.

The `user_consents` patch was created but not applied. No real database,
external mail, OAuth/social interaction, Provider/payment/refund call, browser
run, full suite, Git stage, commit, or push was performed in the WI-068 closure.
No current fresh MySQL proof was obtained.

## Completed Repository Changes

- Added minimal append-only consent persistence:
  `UserConsent`, `UserConsentType`, `UserConsentRepository`, server-owned
  consent-version properties, fresh-schema DDL, and an unapplied existing-
  development-DB patch.
- Extended password registration input with required `termsAgreed` and
  `privacyAgreed` booleans plus optional `marketingAgreed`; the service records
  only affirmative optional marketing consent, with server-generated time and
  server-owned policy versions. Each consent record contains only user, type,
  version, and server-generated agreement time beyond its technical key.
- Added `EMAIL_VERIFICATION_REQUIRED` and placed verified-account checks before
  password login token issuance and before refresh-token rotation.
- Changed the disposable-MySQL source-table-count expectation to 43 and reset
  its MySQL manifest to `UNRECORDED`, so future Create/Validate work fails
  closed until separately approved evidence is recorded. The prior 42-table,
  506-column, 173-index-row, and 90-foreign-key hash is preserved only as
  WI-067 historical evidence, not as the current result.

## Review Closure Chronology (2026-08-16)

- The original review corrected the current 43-table/entity source boundary,
  the `UNRECORDED` live/disposable MySQL manifest, historical-only WI-067
  42/506/173/90/6/hash values, and the distinction between always transmitting
  `marketingAgreed` and persisting Marketing only when it is `true`.
- R2 found the missing [decision register](../agent/WI-20260809-ATS-068-decision-register.md)
  as a P2 traceability gap. The register was restored after R2; the historical
  R2 verdict remains `CONDITIONAL PASS`.
- R3 found a P2 because safe-return behavior was attributed to WI-068 although
  it is outside the approved WI scope. The `DG-068-05` attribution and its
  reconciliation material were removed; the historical R3 verdict remains
  `CONDITIONAL PASS`.
- R4 then passed independently. It reports no P0, P1, or P2 finding in scope
  and confirms that safe-return behavior is not an approved or implemented
  WI-068 claim.

## Scope Boundary

- The Signup UI validates and submits the consent contract, then directs the
  password user to email verification without persisting a session.
- Login and refresh reject an unverified password account before issuing or
  rotating tokens. The SPA gives fixed email-verification guidance.
- Logout callers coalesce and await server revocation. Only `204` is confirmed;
  a bare `401` or other failure still performs local logout with a fixed warning.
- Social login and social profile-completion onboarding are unchanged.
- The 43 source tables/entities are repository evidence only. The current live/
  disposable MySQL manifest is `UNRECORDED`; WI-067 values are historical only.

## Test Status

| Command | Status |
| --- | --- |
| Focused backend tests | PASS - 3 test classes; implementation evidence, not rerun by R4 |
| Focused frontend tests | PASS - 8 files, 69 tests; implementation evidence, not rerun by R4 |
| Frontend typecheck, lint, and format | PASS - implementation evidence, not rerun by R4 |
| R2/R3 static bootstrap guard | PASS - static/preflight only; no MySQL connection and not rerun by R4 |
| `python .agents\\skills\\validate-docs\\scripts\\validate_docs.py` | PASS, exit 0 in R4 |
| `git diff --check` | PASS, exit 0 in R4 |
| SQL patch, real DB/email/OAuth/Provider/payment/refund, browser, full suite, stage/commit/push | NOT RUN in WI-068 closure |

## Residual Risk And Follow-Up

The focused contract is complete, but the actual 43-table MySQL manifest and
external boundaries remain unverified. Do not apply the SQL patch or treat the
historical WI-067 manifest as current. A separate approved database-evidence WI
is required before real database execution. Legal policy documents or URLs were
not supplied to this WI; this summary makes no claim that they are available.

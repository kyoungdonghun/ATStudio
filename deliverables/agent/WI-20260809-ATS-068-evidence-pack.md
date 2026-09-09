---
version: 1.1.0
last_updated: 2026-08-16
project: ATS
owner: docops
category: evidence-pack
status: complete
dependencies:
  - path: ../user/REQ-20260809-ATS-001.md
    reason: Approved parent requirement and execution boundary
  - path: WI-20260809-ATS-068-handoff.md
    reason: Approved WI scope, acceptance criteria, and output contract
  - path: WI-20260809-ATS-068-decision-register.md
    reason: Accepted decision transcription reconciled by the closure reviews
  - path: WI-20260809-ATS-068-qa-integ-review.md
    reason: Original P2 documentation findings and implementation evidence
  - path: WI-20260809-ATS-068-qa-integ-r2-review.md
    reason: Historical missing-decision-register P2 finding
  - path: WI-20260809-ATS-068-qa-integ-r3-review.md
    reason: Historical out-of-scope safe-return-attribution P2 finding
  - path: WI-20260809-ATS-068-qa-integ-r4-review.md
    reason: Independent final closure PASS
---

> Historical public derivative, published in this working tree on 2026-09-09 by WI-20260909-ATS-018. Original dates, language, status, decisions and reported outcomes below are historical, not current approval or reverified recovery/production evidence. The original remains unchanged in private archive `v1-artifacts-20260909-084405`. See [A002: original identity, public SHA-256 and review](../../docs/registry/development-history-recovery-20260909.md#a002). Raw/local-only evidence pointers are lookup keys, not files supplied by a source checkout.

# Evidence Pack: WI-20260809-ATS-068

## Summary

- Complete at focused repository scope. Independent QA Integration R4 closure
  is `PASS` with no P0, P1, or P2 finding in WI-068 scope.
- This status closes the approved WI evidence chain only; it is not a deployment
  or production-readiness claim.

## Review Closure Chronology (2026-08-16)

- The original QA Integration review was `CONDITIONAL PASS` with P2
  current-source/manifest and signup-wire documentation findings. The corrected
  boundary is 43 source tables/entities, `UNRECORDED` live/disposable MySQL
  manifest, historical-only WI-067 42/506/173/90/6/hash values, and an
  always-transmitted `marketingAgreed` boolean with affirmative-only Marketing
  persistence.
- R2 remained `CONDITIONAL PASS`: it found the missing decision register as a
  P2 traceability input. The register was restored after R2; R2's historical
  verdict is not rewritten.
- R3 remained `CONDITIONAL PASS`: it found the restored safe-return attribution
  as an out-of-scope P2. `DG-068-05` and its safe-return reconciliation material
  were removed; R3's historical verdict is not rewritten.
- R4 is the independent final closure: `PASS`, with no P0, P1, or P2 finding in
  scope. It confirms the register contains only the approved consent,
  password-session, email-verification signup, logout, database-evidence, and
  social-scope entries.

## Scope / DoD Check

- [x] `user_consents` persists append-only password-signup evidence containing
  only user, type, server-owned version, and server-generated agreement time.
- [x] Registration requires Terms and Privacy consent; signup always transmits
  optional `marketingAgreed`, and Marketing is persisted only when affirmative.
- [x] Password login and refresh reject an unverified account before token
  issuance or rotation.
- [x] Successful password registration leads to SPA email-verification guidance
  and creates no password session.
- [x] Logout has an explicit caller-safe outcome: only `204` confirms server
  revocation; bare `401` and all failure outcomes still clear local session and
  present the fixed warning.
- [x] Social login and social profile-completion onboarding remain unchanged.

## Reference Documents

| Tier | Document | Use |
| --- | --- | --- |
| 0 | `docs/standards/core-principles.md` | Approved execution and traceability boundary |
| 0 | `docs/standards/development-standards.md` | Java/Spring implementation and test requirements |
| 1 | `docs/policies/security-policy.md` | Secret, token, local-configuration, and fixed-message boundary |
| 1 | `docs/policies/access-control-policy.md` | Least-privilege execution boundary |
| 1 | `docs/policies/quality-gates.md` | Required verification evidence |
| 2 | `.agents/skills/react-best-practices/AGENTS.md` | Frontend contract context |
| 2 | `docs/standards/frontend-standards.md` | SPA/auth-store contract context |
| 2 | `docs/design/api-spec.md` | Authentication API contract context |
| 2 | `docs/design/db-schema.md` | Fresh-schema and existing-DB boundary context |
| Context | `deliverables/user/REQ-20260809-ATS-001.md` | Approved parent requirement |
| Context | `deliverables/agent/WI-20260809-ATS-068-handoff.md` | Approved implementation handoff |
| Context | [WI-068 Decision Register](WI-20260809-ATS-068-decision-register.md) | Accepted in-scope decision transcription after the R2 restoration and R3 scope correction |
| Review | [Initial QA Integration Review](WI-20260809-ATS-068-qa-integ-review.md) | Historical `CONDITIONAL PASS` and original P2 documentation findings |
| Review | [QA Integration R2 Review](WI-20260809-ATS-068-qa-integ-r2-review.md) | Historical `CONDITIONAL PASS` and missing-decision-register P2 finding |
| Review | [QA Integration R3 Review](WI-20260809-ATS-068-qa-integ-r3-review.md) | Historical `CONDITIONAL PASS` and out-of-scope safe-return-attribution P2 finding |
| Review | [QA Integration R4 Review](WI-20260809-ATS-068-qa-integ-r4-review.md) | Independent final `PASS`; no P0/P1/P2 in scope |

## Evidence Pointers

- `src/main/java/com/atstudio/atstudio/config/ConsentPolicyProperties.java`:
  server-owned version values for the three consent categories.
- `src/main/java/com/atstudio/atstudio/entity/UserConsent.java` and
  `src/main/java/com/atstudio/atstudio/entity/enums/UserConsentType.java`:
  append-only record shape with only user relation, type, version, and agreed
  time beyond its technical primary key.
- `src/main/java/com/atstudio/atstudio/repository/UserConsentRepository.java`:
  save-only repository surface.
- `src/main/resources/schema.sql`: fresh baseline `user_consents` DDL and a
  43-table comment.
- `scripts/database/patches/WI-20260809-ATS-068-user-consents.sql`: created,
  unapplied existing-development-DB patch.
- `src/main/java/com/atstudio/atstudio/dto/user/RegisterRequest.java` and
  `src/main/java/com/atstudio/atstudio/service/UserService.java`: required
  Terms/Privacy validation and affirmative Marketing persistence.
- `src/main/java/com/atstudio/atstudio/common/exception/BUSINESS_ERROR.java`
  and `src/main/java/com/atstudio/atstudio/service/auth/AuthService.java`:
  named unverified-password-session rejection before token material is created.
- `frontend/src/api/auth.ts`, `frontend/src/store/authStore.ts`,
  `frontend/src/pages/auth/SignupPage.tsx`,
  `frontend/src/pages/auth/EmailVerifyPage.tsx`,
  `frontend/src/pages/auth/LoginPage.tsx`, `frontend/src/layouts/Header.tsx`,
  and `frontend/src/layouts/AdminLayout.tsx`: consent submission,
  email-verification guidance, and safe logout callers.
- `scripts/database/DisposableMysqlBootstrap.java`: source count changed to 43
  and stale MySQL manifest reset to `UNRECORDED`.
- `scripts/database/README.md`, `docs/index.md`,
  `docs/registry/project-registry.md`, `docs/design/index.md`,
  `docs/design/payment-settlement-import-design.md`,
  `docs/payment/admin-operations-guide.md`, and
  `docs/design/payment-refund-receipt-settlement-policy.md`: P2 current-source
  and historical-WI-067 manifest correction.
- `docs/ui/atstudio-front-list.md` and `docs/ui/screen-flow.md`: P2 signup wire
  behavior correction.

## Commands And Results

| Command | Result |
| --- | --- |
| Input-pointer reads from WI handoff | Completed before the focused implementation work |
| Focused backend tests | PASS - three classes: `AuthControllerTest`, `AuthServiceTest`, and `UserServiceTest`; implementation evidence, not rerun by R4 |
| Focused frontend tests | PASS - eight files and 69 tests; implementation evidence, not rerun by R4 |
| Frontend typecheck, lint, and format | PASS - implementation evidence, not rerun by R4 |
| R2/R3 static bootstrap guard | PASS - static/preflight only, with no MySQL connection; not rerun by R4 |
| `git diff --check` | PASS, exit 0 in the independent R4 closure |
| `python .agents\\skills\\validate-docs\\scripts\\validate_docs.py` | PASS, exit 0 in the independent R4 closure |

## External Side-Effect Boundary

- The `user_consents` patch was created but was not applied. No real or
  disposable MySQL execution, observation, Create/Validate action, or current
  fresh MySQL manifest proof exists for this WI.
- No real database, external mail, OAuth/social interaction, Provider/payment/
  refund call, browser run, or full suite was executed in the WI-068 closure.
- The R2/R3 bootstrap guard was a static/preflight check only; it did not open a
  MySQL connection. R4 did not rerun it.
- Ignored/local configuration and secrets: NOT INSPECTED.
- Git stage, commit, push, branch, merge, rebase, tag, and stash: NOT RUN in
  the WI-068 closure.

## Risks / Rollback

- Risk: the 43-table source schema and entity count are static repository
  evidence only. The live/disposable MySQL manifest remains `UNRECORDED`, so
  current MySQL DDL, patch, and manifest proof is absent. External account/
  provider behavior, browser coverage, and full-suite coverage remain
  unverified.
- Rollback: revert only the WI-068 repository hunks. No database rollback
  applies because no patch was run.

## Follow-Up

- Create a separately approved database-evidence WI before recording a 43-table
  MySQL manifest or executing the existing patch.
- This WI was not supplied legal policy documents or URLs and makes no claim
  that they exist. No legal body or URL is invented in this evidence.

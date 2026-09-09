
> Historical public derivative, published in this working tree on 2026-09-09 by WI-20260909-ATS-018. Original dates, language, status, decisions and reported outcomes below are historical, not current approval or reverified recovery/production evidence. The original remains unchanged in private archive `v1-artifacts-20260909-084405`. See [A007: original identity, public SHA-256 and review](../../docs/registry/development-history-recovery-20260909.md#a007). Raw/local-only evidence pointers are lookup keys, not files supplied by a source checkout.

# QA Integration R4 Closure Review: WI-20260809-ATS-068

**Reviewer:** QA Integration (independent focused R4 review)<br>
**Review date:** 2026-08-16<br>
**Verdict:** PASS

## Verdict Basis

The R3 P2 is closed. The current WI-068 decision register contains no
`DG-068-05`, safe-return, or return-target decision or reconciliation material.
Its remaining entries (`DG-068-01` through `DG-068-04`, `DG-068-06`, and
`DG-068-07`) accurately map to the approved WI-068 handoff: separate consent
and persistence, verified password-session gating, email-verification signup,
logout outcome, and the database-evidence/social-scope boundary. They do not
represent a post-login return behavior as approved or implemented WI-068 scope.

The R2 corrections remain accurate against current source and documentation:

- `schema.sql` has 43 `CREATE TABLE` statements and the entity package has 43
  direct `@Entity` declarations. `DisposableMysqlBootstrap` requires 43 and
  keeps the current MySQL manifest expectation `UNRECORDED`; its Create and
  Validate paths refuse before credentials or a database connection.
- Current documentation identifies the WI-067 42/506/173/90/6/hash material as
  historical evidence for the superseded 42-table source snapshot only.
- Signup always sends `termsAgreed`, `privacyAgreed`, and `marketingAgreed`.
  Required Terms and Privacy must be `true`; Marketing consent is persisted
  only when `marketingAgreed` is `true`.
- Password login and refresh check verification before token generation or
  rotation. Successful signup navigates to email verification without a
  password SPA session. Only logout HTTP `204` confirms revocation; every other
  outcome clears local state and is surfaced as unconfirmed to the allowed
  callers.

No new P0, P1, or P2 was introduced by the correction.

## Findings

### P0

None.

### P1

None.

### P2

None.

## Commands And Results

| Command | Result |
| --- | --- |
| `python .agents/skills/validate-docs/scripts/validate_docs.py` | PASS, exit 0. |
| `git diff --check` | PASS, exit 0. |

Static repository reads and source/document comparisons were used for this
review. No test, database, browser, external-service, bootstrap, patch,
credential, or Git mutation was performed.

## Residual Boundaries

- The 43-table source and entity counts are repository evidence only. The
  current MySQL manifest remains `UNRECORDED`; no database patch application,
  MySQL observation, Create/Validate action, or current database manifest was
  obtained.
- Browser, real email, OAuth/social interaction, Provider/payment/refund calls,
  full-suite execution, and focused test reruns remain outside this review.
- Safe Login return behavior remains general current application behavior in
  shared authentication documentation, but it is neither an approved nor an
  implemented WI-068 scope claim and requires no DG-068-05 restoration.

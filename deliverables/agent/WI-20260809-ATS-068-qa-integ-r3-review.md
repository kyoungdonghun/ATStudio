
> Historical public derivative, published in this working tree on 2026-09-09 by WI-20260909-ATS-018. Original dates, language, status, decisions and reported outcomes below are historical, not current approval or reverified recovery/production evidence. The original remains unchanged in private archive `v1-artifacts-20260909-084405`. See [A006: original identity, public SHA-256 and review](../../docs/registry/development-history-recovery-20260909.md#a006). Raw/local-only evidence pointers are lookup keys, not files supplied by a source checkout.

# QA Integration R3 Closure Review: WI-20260809-ATS-068

**Reviewer:** QA Integration (independent focused R3 review)<br>
**Review date:** 2026-08-16<br>
**Verdict:** CONDITIONAL PASS

## Verdict Basis

R2's missing-input P2 is closed: the decision register now exists, and its
consent, password-session, logout, database-evidence, and social-scope entries
reconcile with the approved WI contract and current repository source. Current
documentation consistently identifies 43 source tables and 43 direct JPA
entities, with the live/disposable MySQL manifest `UNRECORDED`; it does not
present the WI-067 42/506/173/90/6/hash evidence as current proof. The UI
documents and implementation also agree that `marketingAgreed` is always
transmitted, Marketing is stored only for `true`, Terms/Privacy are required,
unverified password login and refresh issue no tokens, signup persists no
password SPA session, and only logout `204` is confirmed.

However, the restored register includes a safe post-login-return decision as
an accepted WI-068 decision although that behavior is absent from the approved
WI-068 handoff's scope, DoD, acceptance criteria, and output contract. It is
implemented current behavior, but implementation alone does not establish that
it is an approved WI-068 decision. This is a remaining P2 scope/traceability
defect, so PASS is not available.

No database, browser, external service, full suite, targeted test, database
bootstrap action, patch application, secret inspection, or Git mutation was
performed in R3.

## Findings

### P0

None.

### P1

None.

### P2

1. **The decision register records a pre-existing safe-return behavior as an
   accepted WI-068 decision without WI-068 approval evidence.**
   `deliverables/agent/WI-20260809-ATS-068-decision-register.md:27-28,44,61-69`
   describes `DG-068-05` as an accepted transcription of the approved WI
   contract. The approved handoff limits the contract to consent persistence,
   password verification/session gating, email-verification signup, logout,
   and associated boundaries (`WI-20260809-ATS-068-handoff.md:23-50,52-107,
   169-204`); it explicitly excludes unrelated authentication behavior. The
   return-target implementation exists in
   `frontend/src/pages/auth/LoginPage.tsx:81-96,170-172` and
   `frontend/src/utils/loginReturn.ts:36-110`, but the WI-068 approval artifacts
   do not make it an accepted decision for this WI. Remove `DG-068-05` and its
   reconciliation material, or relabel it as pre-existing out-of-scope behavior
   with an authoritative approved predecessor pointer. Do not represent it as
   an accepted WI-068 decision.

### P3 / Residual Boundaries

1. **Current MySQL execution evidence remains intentionally absent.** Static
   source evidence is 43 `CREATE TABLE` statements and 43 direct `@Entity`
   types. The active bootstrap expectation is `UNRECORDED`, and current
   `Create`/`Validate` fail before credentials or a connection
   (`scripts/database/DisposableMysqlBootstrap.java:49-51,127-150,551-567`).
   The `user_consents` patch remains unapplied. No fresh 43-table MySQL
   manifest, DDL application, bootstrap action, or patch proof was obtained.

2. **External and operational boundaries remain unverified by design.** No
   real email, OAuth/social interaction, Provider/payment/refund request,
   browser run, or full suite was executed. Social/OAuth lifecycle is unchanged
   and outside WI-068; this review makes no claim that those external paths
   were verified.

## R2 Closure Cross-Check

| R2 item | R3 result | Evidence |
| --- | --- | --- |
| Missing decision register | Closed, with the P2 decision-scope defect above | `WI-20260809-ATS-068-decision-register.md:1-149` exists and preserves R2's `CONDITIONAL PASS` status rather than claiming a later verdict (`:27-31`). |
| Current source and manifest boundary | PASS | `docs/index.md:71`, `docs/registry/project-registry.md:42-43`, `docs/design/index.md:29`, `docs/design/db-schema.md:49-60,326-330`, and `scripts/database/README.md:27-33,53-58` state 43/43 and `UNRECORDED`. The WI-067 42/506/173/90/6/hash material is labelled historical in `docs/design/db-schema.md:59-90`, `scripts/database/README.md:150-230`, and the payment documents. |
| Always-transmitted Marketing and affirmative-only storage | PASS | `SignupPage.tsx:163-175` always sends all three booleans; `UserService.java:444-464` requires Terms/Privacy and records Marketing only for `Boolean.TRUE`. `docs/ui/atstudio-front-list.md:57-60` and `docs/ui/screen-flow.md:62-66` match. |
| Unverified password session and signup boundary | PASS | `AuthService.java:48-56,100-123` checks verification before login or refresh token generation. `SignupPage.tsx:163-177` registers then navigates to email verification without auth-store login. |
| Logout outcome boundary | PASS | `frontend/src/api/auth.ts:163-170` maps only `204` to `confirmed`; `authStore.ts:149-163` coalesces and clears local state in `finally`; `Header.tsx:194-207` and `AdminLayout.tsx:259-272` await the outcome and warn when unconfirmed. |

## Commands And Results

| Command | Result |
| --- | --- |
| `python .agents/skills/validate-docs/scripts/validate_docs.py` | PASS, exit 0: Tier 0 documents, internal links, 587 supported traceability IDs, and document index. |
| `git diff --check` | PASS, exit 0. Existing working-copy CRLF-to-LF advisories were emitted; no whitespace error was reported. |
| `powershell -NoProfile -ExecutionPolicy Bypass -File .\\scripts\\database\\test-bootstrap-guards.ps1` | PASS: 20 static/preflight guards, including the 43-table count and unrecorded-manifest `Create`/`Validate` refusal. The script's checks completed without MySQL connection activity. |

## Explicit Verdict

**CONDITIONAL PASS.** R2's missing-register P2 is closed, but the remaining
P2 decision-scope misrepresentation in `DG-068-05` must be corrected before
this WI can receive PASS. The unapplied patch, absent fresh 43-table MySQL
proof, and unverified external-service paths are residual P3 boundaries only
and are not blockers unless represented as executed or current proof.

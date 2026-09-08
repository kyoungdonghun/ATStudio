---
version: 1.0
last_updated: 2026-09-08
project: ATS
owner: se
category: evidence-pack
status: stable
dependencies:
  - path: WI-20260908-ATS-002-handoff.md
    reason: Approved scope and file ownership
  - path: ../user/REQ-20260908-ATS-001.md
    reason: Approved release safety changes
---

# Evidence Pack: WI-20260908-ATS-002

> Purpose: Record bounded frontend payment-confirmation fixes and reproducible verification.

## Summary (one-liner)

PASS: three payment screens now separate reported card errors from authoritative outcomes, require explicit paid-upgrade confirmation, and require an entered correction expiry; 301 focused tests pass, up from 272.

## Scope / DoD Check

- [x] Only a single exact `INVALID_CARD_NUMBER` value supplies the fail-route hint. Unknown, blank, duplicated, whitespace-altered, null-byte and malformed code values supply no hint. Provider message text is never rendered.
- [x] The hint remains separate from financial outcome state. Matching-purpose DONE suppresses it even if canonical reload fails. UNKNOWN protection and read-only status recovery remain intact; URL content creates no prepare, confirm, SDK call or replacement-attempt storage entry.
- [x] Upgrade preview distinguishes the retained current cycle and period from the next cycle. Existing Modal and Button components show target plan, immediate server-preview amount, retained expiry, next billing date and amount before a paid mutation.
- [x] The paid confirmation is bound to preview identity, request generation, source subscription, selected plan and cycle. Selection/context changes retire it. Repeated mutation clicks, ambiguous recovery and existing no-charge paths retain their guards.
- [x] Refund-linked correction expiry starts empty and is required. Request, approval and typed execution confirmations show user/subscription/refund IDs, target plan/cycle/status/expiry, pending-change effect and local billing cancellation effect. Provider refund and billing-key deletion remain separate.
- [x] Input edits invalidate correction preview and open confirmation. Preview generations ignore late results after edits or unmount; a failed re-preview cannot preserve an executable prior preview.
- [x] Mobile modal footer stacks buttons, and button text can wrap within its container. No redesign, new library, route/API/name or billing-policy change.
- [x] Focused Vitest, frontend TypeScript, full frontend ESLint, changed-file Prettier and scoped whitespace checks pass.

## Reference Documents (Tier 0-2)

| Tier | Document                                                                          | Use                                                               |
| ---- | --------------------------------------------------------------------------------- | ----------------------------------------------------------------- |
| 0    | docs/standards/core-principles.md (STD-001)                                       | MA-supplied approved execution and truthful evidence boundaries   |
| 0    | docs/standards/documentation-standards.md (STD-004)                               | Supplied context; local metadata section checked after truncation |
| 0    | docs/standards/development-standards.md (STD-002)                                 | Supplied implementation, scope, testing and traceability rules    |
| 0    | docs/standards/glossary.md (STD-005)                                              | Supplied Subscription and correction terminology                  |
| 1    | docs/policies/security-policy.md                                                  | Secrets/PII isolation and no raw provider output                  |
| 1    | docs/policies/quality-gates.md                                                    | Reuse, focused verification and rollback                          |
| 2    | docs/design/payment-integration-design.md, Upgrade and Outcome Recovery sections  | Existing financial authority and UNKNOWN semantics                |
| 2    | docs/payment/admin-operations-guide.md, sections 11-12                            | Separate local correction, approval, typed execution and recovery |
| 2    | docs/payment/acceptance-test-checklist.md, sections 4 and 10                      | Existing upgrade and correction acceptance pointers               |
| 2    | frontend/src/api/userSubscriptions.ts; components/ui/Modal.tsx; Button.module.css | Existing response fields and confirmation controls, unchanged     |

Injection rule source: `.claude/config/context-injection-rules.json`, as specified by the MA handoff. Assignee `se`, implementation task, Tier 0 supplied by MA. Project tag ATS was checked in `.claude/config/workspace.json`. No nested delegation was performed.

Skills used: `.agents/skills/react-best-practices/SKILL.md`, `test`, `typecheck`, `eslint`, `prettier`, and `create-wi-evidence-pack`. The approved REQ and MA-created handoff existed before edits. React work reused existing controls, primitive request generations and existing independent canonical reads without adding dependencies.

## Evidence Pointers

| Owned file                                                                                                 | Key locations and changes                                                                                                                          |
| ---------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------- |
| [SubscriptionPaymentPage.tsx](../../frontend/src/pages/subscriber/SubscriptionPaymentPage.tsx)             | Lines 89, 220, 525: single-code reported hint, DONE precedence and separate rendering                                                              |
| [SubscriptionPaymentPage.test.tsx](../../frontend/src/pages/subscriber/SubscriptionPaymentPage.test.tsx)   | Lines 918, 963, 978: state/read failures, safe route exit, malformed code and DONE tests                                                           |
| [SubscriptionManagePage.tsx](../../frontend/src/pages/subscriber/SubscriptionManagePage.tsx)               | Lines 361, 734, 748, 1265, 1408: snapshot confirmation, mutation guard and current/next period presentation                                        |
| [SubscriptionManagePage.test.tsx](../../frontend/src/pages/subscriber/SubscriptionManagePage.test.tsx)     | Lines 1028, 1083, 1124: retained monthly/yearly periods, no write before confirmation/cancel, duplicate click, stale context and zero-charge tests |
| [SubscriptionManagePage.module.css](../../frontend/src/pages/subscriber/SubscriptionManagePage.module.css) | Lines 404 and 433: footer wrapping, contained button text and mobile stacking                                                                      |
| [PaymentOperationsPage.tsx](../../frontend/src/pages/admin/PaymentOperationsPage.tsx)                      | Lines 338, 345, 717, 1228, 1264, 1289, 1383, 2996: explicit expiry, preview invalidation and consistent target summaries                           |
| [PaymentOperationsPage.test.tsx](../../frontend/src/pages/admin/PaymentOperationsPage.test.tsx)            | Lines 1261, 1282, 1317, 1353: empty expiry, edited/late preview, approval and typed-execution target/effect checks                                 |

The two documents owned by this WI are this Evidence Pack and the linked User Summary. Seven frontend files plus these two documents comprise SE ownership. Existing backend tests and concurrently appearing backend/schema changes belong to other work and were neither edited nor reverted by SE.

## Commands & Outputs

Workspace: `C:/Users/jm991/Desktop/project/ATStudio`.
Branch verified before and after implementation: `codex/v1-release-rehearsal-fixes`.
HEAD at final verification: `2f2e9eccadd9ae9626fe8273bc635068d42b09b0`.

Commands below ran in `frontend/` unless stated otherwise:

```powershell
npm test -- src/pages/subscriber/SubscriptionPaymentPage.test.tsx src/pages/subscriber/SubscriptionManagePage.test.tsx src/pages/admin/PaymentOperationsPage.test.tsx
npm run typecheck
npm run lint
npx --no-install prettier --check src/pages/subscriber/SubscriptionPaymentPage.tsx src/pages/subscriber/SubscriptionPaymentPage.test.tsx src/pages/subscriber/SubscriptionManagePage.tsx src/pages/subscriber/SubscriptionManagePage.test.tsx src/pages/subscriber/SubscriptionManagePage.module.css src/pages/admin/PaymentOperationsPage.tsx src/pages/admin/PaymentOperationsPage.test.tsx
```

Prettier formatting was applied only to these seven owned paths before the final check. `git diff --check --` followed by the seven repository-relative paths exited 0 from the repository root. No dependency install, server startup/restart, commit or push occurred.

| Check                                        | Final result                                           |
| -------------------------------------------- | ------------------------------------------------------ |
| Vitest 4.1.4                                 | Exit 0; 3 files, 301 tests passed; 0 failed, 0 skipped |
| TypeScript `tsc --noEmit`                    | Exit 0                                                 |
| ESLint `src --ext .ts,.tsx --max-warnings 0` | Exit 0; no errors or warnings                          |
| Changed frontend Prettier                    | Exit 0; all 7 files formatted                          |
| Scoped diff whitespace                       | Exit 0                                                 |

Final deliverable verification also passed: Prettier checked all 9 owned files (7 frontend files and 2 documents); a read-only link check resolved 16 relative Markdown links with zero missing targets. These document checks did not rerun or claim the repository-wide documentation validator.

## Tests

| Suite                   | Baseline | Final passed | Added | Failed | Skipped |
| ----------------------- | -------: | -----------: | ----: | -----: | ------: |
| SubscriptionPaymentPage |       95 |          111 |    16 |      0 |       0 |
| SubscriptionManagePage  |       74 |           80 |     6 |      0 |       0 |
| PaymentOperationsPage   |      103 |          110 |     7 |      0 |       0 |
| Total                   |      272 |          301 |    29 |      0 |       0 |

Baseline run: 2026-09-08 17:59:29 local start, 8.79s. Red-phase selection at 18:01:12 produced 9 expected failures for missing hint, confirmation detail and explicit expiry behavior, before implementation. Later test-only selector, detached-row and mock-reference issues were corrected; TypeScript also identified an unsupported test `Array.at` call and the obsolete date-default helper. Both were removed without changing compiler settings. No existing test was removed.

Final post-format and post-compatibility-fix run: 2026-09-08 18:06:49 local start, 8.58s, exit 0:

```text
Test Files  3 passed (3)
     Tests  301 passed (301)
  Duration  8.58s (transform 1.60s, setup 755ms, import 2.34s, tests 14.96s, environment 3.19s)
```

All component API functions were mocked. The 301 include existing checkout identity, prepare idempotency, response-loss, canonical identity, cross-mutation, status-read and ADMIN recovery regressions. Tests asserting mutations invoke mocks, not a backend or Provider.

## MA-Supplied Browser Evidence

MA reported isolated Playwright PASS at 1440x900 and 390x900 during this WI. This is supplied evidence, not a browser session executed or screenshots independently inspected by SE.

- Upgrade confirmation showed 100,000 KRW now, the retained YEARLY period/dates, and next MONTHLY billing of 19,900 KRW. Cancel produced zero write calls.
- Allowlisted card-error guidance was present; the raw provider message was absent; zero JavaScript errors were reported.
- Final mobile screenshot showed no overflow and stacked footer buttons. Screenshots used `animations: 'disabled'` for the existing finite modal animation; this required no product animation change.
- All `/api` requests were intercepted with synthetic user 999001 and mutation requests blocked. No real account, Provider, card, charge or refund evidence is claimed.
- MA artifact basenames: `upgrade-confirmation-1440.png`, `upgrade-confirmation-390.png`, `card-failure-1440.png`, `card-failure-390.png`. MA described their location as its runtime folder; the absolute directory was not supplied to SE. MA owns the durable path handoff.

The final source retains MA's requested plain-language guidance and mobile footer behavior. Subsequent changes were formatting, test compatibility and removal of the unused date helper. Final source freeze hashes (SHA-256):

| File                              | SHA-256                                                          |
| --------------------------------- | ---------------------------------------------------------------- |
| SubscriptionPaymentPage.tsx       | 49F959725A34B79F51534EF723941A7C535EB7981807EF9ECAE44E98B31CD36F |
| SubscriptionManagePage.tsx        | 8E0C8B02011BB9183530A1512002EB4A54445B7AC08683B2FAF9CC8520E6169C |
| SubscriptionManagePage.module.css | 929BA56EE932649B9DDFFFA43055285CD37DCE02AD5FD7ADCAD4753C977D98EE |
| PaymentOperationsPage.tsx         | A08FDCCA4B6AD21C7CF1A7093995092B3D68ABFFCE2005DA78A6D556CEC44395 |

## Risks / Rollback

- No backend/API, DB, source policy, ignored secret, other worktree, live account or external Provider was edited or exercised by SE. Public Vite may have HMR-applied the scoped source edits; SE did not restart or stop any server.
- Backend proration, cadence, expiry and correction policies remain unchanged by this WI. Preview is not a server-locked quote: a later server-side change can alter the authoritative mutation result under the existing API. No new price-lock guarantee is claimed.
- Full frontend suite, coverage measurement, build, backend tests, real Toss TEST/LIVE, operational MySQL, real financial operations and production acceptance were not executed by SE for WI-002.
- Browser evidence covers the two reported customer flows with intercepted APIs. ADMIN visual/mobile confirmation and actual correction execution were not browser-verified in the supplied result.
- Rollback is file-only: apply a reviewed reverse patch to this WI's seven frontend paths, preserving all concurrent edits. Retire the two owned documents separately only if approved. No blanket checkout/reset, DB action or runtime rollback is required.

## Follow-ups

WI-002 is complete and releases its dependency on **WI-20260908-ATS-004**. MA must join WI-003 completion, generate the WI-004 handoff with `create-wi-handoff-packet`, and delegate independent regression/review. SE performed no nested delegation, made no commit and did not close the overall REQ. MA should attach the absolute browser artifact directory during that handoff.

## Related Documents

- [WI-002 Handoff](WI-20260908-ATS-002-handoff.md)
- [Approved REQ](../user/REQ-20260908-ATS-001.md)
- [WI-002 User Summary](../user/WI-20260908-ATS-002-summary.md)
- [WI-001 Evidence](WI-20260908-ATS-001-evidence-pack.md)
- [Payment Integration Design](../../docs/design/payment-integration-design.md)
- [Admin Payment Operations Guide](../../docs/payment/admin-operations-guide.md)

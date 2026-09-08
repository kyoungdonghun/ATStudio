---
version: 1.0
last_updated: 2026-09-08
project: ATS
owner: se
category: reference
status: stable
dependencies:
  - path: WI-20260908-ATS-007-handoff.md
    reason: Approved ownership and acceptance criteria
  - path: ../user/REQ-20260908-ATS-002.md
    reason: Approved presentation-only scope
---

# Evidence Pack: WI-20260908-ATS-007

## Summary

Completed the scoped admin terminology and stored receipt evidence display. Implementation is frozen for MA integration and browser verification.

## Scope / DoD Check

- [x] Both admin entry points use `구독 이용권 조정`; the payment incident tab and feedback use `결제 점검 이슈`.
- [x] Both execute gates require `구독 이용권 조정 실행`; the old phrase is rejected. Existing validation remains unchanged: PaymentOperationsPage compares the exact prompt value; UserSubscriptionCorrectionModal compares after trim.
- [x] Standalone visible `보정` was changed to `조정` in the three owned source files. English keys, functions, enums, requests and mutation behavior remain unchanged.
- [x] Receipt header: `증빙 상태`; caption: `원결제 영수증 · 환불 상태와 별도`. Mapping: `ISSUED` = `발급 기록`, `CANCELLED` = `취소 기록`, `PARTIAL_CANCELLED` = `부분 취소 기록`, `FAILED` = `발급 실패`.
- [x] Unknown status/type values remain raw; HTTPS URL validation and non-clickable reference/review fallback remain intact. No refund state is inferred and no extra API request was added.
- [x] Focused tests, TypeScript, ESLint, Prettier and scoped diff checks pass.

## Reference Documents

- Tier 0 injection: `docs/standards/core-principles.md` (STD-001), `documentation-standards.md` (STD-004), `development-standards.md` (STD-002), `glossary.md` (STD-005).
- Tier 1: `.claude/agents/se.md`; `docs/policies/security-policy.md`, particularly receipt URL/reference handling.
- Tier 2: approved REQ and handoff; receipt/confirmation sections in `docs/payment/admin-operations-guide.md`, `known-limits-and-next-steps.md`, `user-flows.md`, and `docs/ui/`.
- Skills: supplied `create-wi-handoff-packet` contract, `react-best-practices`, `test`, `typecheck`, `eslint`, `prettier`, `create-wi-evidence-pack`. Handoff injection source: `.claude/config/context-injection-rules.json`; assignee SE; frontend implementation/testing.

## Evidence Pointers

- `frontend/src/pages/admin/PaymentOperationsPage.tsx:84`: four-status display map with raw fallback at line 2403; caption/header at lines 2375/2383; exact execute phrase at line 124; incident label at line 1539.
- `frontend/src/pages/admin/UserSubscriptionManagePage.tsx:109`: success feedback; entry button at line 189.
- `frontend/src/pages/admin/UserSubscriptionCorrectionModal.tsx:76`: new phrase; existing trimmed gates at lines 686/734; workflow title at line 770; confirmation labels at line 1172.
- `frontend/src/pages/admin/PaymentOperationsPage.module.css:90`: small semantic caption styling only.
- `frontend/src/pages/admin/PaymentOperationsPage.test.tsx:704`: all four known statuses, future/prototype-like unknown status, raw unknown type, unsafe URL/reference handling, issued evidence not relabeled as refund, no refund fetch. Line 1305 rejects six invalid prompt values, including the old phrase.
- `frontend/src/pages/admin/UserSubscriptionManagePage.test.tsx:718`: actual preview/request/approve/execute workflow; old phrase, padded old phrase, partial and suffixed phrases rejected at line 751; correct trimmed phrase accepted; existing one-execution and pending-state coverage retained.
- Read-only backend contract: `src/main/java/com/atstudio/atstudio/entity/enums/PaymentReceiptStatus.java` has exactly the four mapped values. `dto/payment/AdminPaymentReceiptResponse.java` has status/type/issuedAt/cancelledAt, but no refund aggregate. Backend files were not changed by this WI.

## Commands and Results

Commands below run from `frontend/` unless noted. All six edited frontend files are the three source components, their two existing test files, and PaymentOperationsPage.module.css.

- `npm test -- src/pages/admin/PaymentOperationsPage.test.tsx src/pages/admin/UserSubscriptionManagePage.test.tsx`: 2 files, **139 passed**, 0 failed, 0 skipped; final run 8.50 seconds. The preceding targeted red run had 8 expected failures before implementation.
- `npm run typecheck`: exit 0.
- `npx eslint src/pages/admin/PaymentOperationsPage.tsx src/pages/admin/PaymentOperationsPage.test.tsx src/pages/admin/UserSubscriptionManagePage.tsx src/pages/admin/UserSubscriptionManagePage.test.tsx src/pages/admin/UserSubscriptionCorrectionModal.tsx --max-warnings 0`: exit 0, no warnings.
- `npx prettier --check` with the same five TSX paths plus `src/pages/admin/PaymentOperationsPage.module.css`: exit 0 after applying format-only patches.
- `git diff --check --` with those six repository-relative frontend paths, from repository root: exit 0.
- `rg -n "권한 보정|보정|대사 Incident|Incident 상태|구독 구독"` against the three owned source components: no matches (exit 1).
- In-memory pre-edit snapshot comparison: all three components match only the approved label substitutions, receipt map/caption/header/fallback additions and formatting. Previous WI002 expiry, preview-generation and target-summary changes remain present; its existing tests also pass. The total Git diff includes that pre-existing work and is not the WI007-only patch size.

## Risks / Rollback

Initial changes were limited to six owned frontend files and this WI's two deliverables in `C:/Users/jm991/Desktop/project/ATStudio`, branch `codex/v1-release-rehearsal-fixes`; the approved test-only extension below adds one file and the handoff. No other worktree, commit, nested agent, backend/API/DB mutation, provider/SMTP call, build or server restart was performed. All manual edits and formatting patches used apply_patch.

Browser fixtures, responsive visual verification, full regression and current documentation integration belong to MA; this pack claims local component/static verification only. A stored `ISSUED` receipt can still be original-charge evidence after a refund. Rollback must remove only WI007 label/display/test additions while preserving prior dirty WI002 changes; do not revert entire files.

## Follow-up

### Approved Test-only Extension

MA reported the first aggregate as **1,493 total / 1,490 passed / 3 failed** in `copy-polish-frontend-test.log` under the MA runtime directory. The three failures used stale UI expectations in `frontend/src/test/coverage/adminSubscriberGaps.coverage.test.tsx`; no product failure was established. MA extended the existing handoff ownership to this test and the WI007 artifacts under the user's already-approved corresponding-test scope, not a new direct user approval.

Only 16 actual UI expectation lines (button/title/input/prompt, including escaped strings) and their formatting changed. No production code, generic `ConfirmDialog.test` arbitrary-phrase fixture or existing old-phrase rejection test was edited. `npm test -- src/test/coverage/adminSubscriberGaps.coverage.test.tsx` passed **24/24**, 0 skipped, 6.09 seconds; focused ESLint passed. The file is refrozen after its focused Prettier/diff check. MA owns the new aggregate/coverage run; the initial aggregate FAIL is not relabeled as PASS.

WI007's dependency for **WI-20260908-ATS-008** is satisfied. MA should continue the existing WI008 handoff once WI006 is also ready. No nested delegation or REQ closure was performed by this assignee.

## Related Documents

- [Approved REQ](../user/REQ-20260908-ATS-002.md)
- [WI007 Handoff](WI-20260908-ATS-007-handoff.md)
- [WI007 Summary](../user/WI-20260908-ATS-007-summary.md)
- [WI008 Handoff](WI-20260908-ATS-008-handoff.md)

---
version: 1.0
last_updated: 2026-09-08
project: ATS
owner: se
category: reference
status: active
dependencies:
  - path: ../user/REQ-20260908-ATS-002.md
    reason: Approved small presentation work
---

# WI-20260908-ATS-007: Admin display terminology and receipt clarity

[WI HEADER]
REQ: REQ-20260908-ATS-002 (approved)
Agent: se
Depends On: -
Blocks: WI-20260908-ATS-008

[WI SUMMARY / DoD]
Rename visible 권한 보정 -> 구독 이용권 조정 and 대사 Incident -> 결제 점검 이슈 across PaymentOperationsPage and UserSubscriptionManagePage/UserSubscriptionCorrectionModal, tests. Exact execute typed phrase becomes 구독 이용권 조정 실행 in BOTH paths; don't accept old phrase as alias. Internal identifiers/API/canonical enums remain. ReceiptTable label 증빙 상태, issued evidence e.g. 발급 기록, supported statuses localized with honest unknown fallback; short semantic note 원결제 영수증 · 환불 상태와 별도 (no fabricated refund status). Preserve safe URL/reference fallback and no extra requests. Existing 3 pages/ tests may already be dirty from WI002; preserve them.
Tests cover both entry points/exact confirmation safety, receipt status semantics including unknown statuses and unsafe URL regression; typecheck/ESLint/Prettier focused. Keep compact responsive UI, no new decorative sections.

[INPUT POINTERS]
Tier 0: docs/standards/core-principles.md; documentation-standards.md; development-standards.md; glossary.md (all docs/standards).
Tier 1: docs/policies/security-policy.md; .claude/agents/se.md.
Tier 2: approved REQ; docs/payment/known-limits-and-next-steps.md; admin-operations-guide.md; payment/user-flows.md; corresponding current code/test files.
Skills: test, react-best-practices for frontend; create-wi-evidence-pack. Handoff generated using create-wi-handoff-packet; injection rules .claude/config/context-injection-rules.json, ATS workspace config.

[WRITE OWNERSHIP]
frontend/src/pages/admin/PaymentOperationsPage.tsx/.test.tsx, UserSubscriptionManagePage.tsx/.test.tsx, UserSubscriptionCorrectionModal.tsx and corresponding actual test file, existing related CSS only if necessary. Do not edit other frontend or backend files.
Own deliverables/agent/WI-20260908-ATS-007-evidence-pack.md and deliverables/user/WI-20260908-ATS-007-summary.md.
No other worktree, product policy, API/DB, secrets, real provider/mail actions, server changes, commits or nested agents. Preserve all preceding uncommitted work; apply_patch only for manual edits.

[OUTPUT CONTRACT]
Use create-wi-evidence-pack; concise evidence with actual commands/results/diff boundaries and next Blocks trigger. No duplicate exhaustive release audit.
Main worktree C:/Users/jm991/Desktop/project/ATStudio, branch codex/v1-release-rehearsal-fixes. MA performs aggregate verification; do not run full build overwriting active JAR.

[2026-09-08 APPROVED OWNERSHIP EXTENSION]
MA aggregate found three stale UI-expectation failures in the existing
frontend/src/test/coverage/adminSubscriberGaps.coverage.test.tsx. MA extends
WI007 ownership to this file and this handoff/evidence/summary under the user's
already-approved corresponding-test scope; this is not a new direct user approval.
Change only actual UI button/title/input/prompt expectations to the implemented
new labels, including escaped old strings. Preserve arbitrary generic
ConfirmDialog fixtures, the old-phrase rejection tests, and all production code.
Run only focused tests/static checks, report refreeze immediately, and return to
WI008. Initial aggregate remains 1,493 total / 1,490 passed / 3 failed until MA
reruns it; this extension does not claim full-suite PASS.

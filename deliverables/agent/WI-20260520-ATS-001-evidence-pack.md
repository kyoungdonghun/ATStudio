# Evidence Pack: WI-20260520-ATS-001

## Summary (one-liner)
- Fixed post-review payment edge cases around whole-KRW upgrade charges, zero-amount upgrades, and next-billing preview clarity.

## Scope / DoD Check
- DoD items:
  - [x] Upgrade prorated charge rounds to whole KRW.
  - [x] Zero-amount upgrade skips provider charge while still requiring an active billing agreement.
  - [x] Preview DTO/API/frontend expose next billing date and next billing amount.
  - [x] Tests and docs were updated.
  - [x] Generated `frontend/tsconfig.tsbuildinfo` change was reverted after build.

## Reference Documents (Tier 0-2)

**Injected Context** (from WI Handoff Packet):

| Tier | Document | Reason |
|------|----------|--------|
| 0 | `docs/standards/core-principles.md` | Constitution |
| 0 | `docs/standards/development-standards.md` | Implementation standard |
| 0 | `docs/standards/documentation-standards.md` | Documentation standard |
| REQ | `deliverables/user/REQ-20260519-ATS-001.md` | Approved payment behavior |
| WI | `deliverables/agent/WI-20260519-ATS-005-evidence-pack.md` | Prior verification baseline |
| Design | `docs/design/payment-integration-design.md` | Payment architecture |
| Design | `docs/design/api-spec.md` | API contract |
| Design | `docs/design/usecase/user-subscription.md` | Subscription use case |
| Design | `docs/design/usecase/util.md` | Preview use case |
| UI | `docs/ui/screen-flow.md` | Screen flow |
| UI | `docs/ui/modal-list.md` | Modal/preview flow |

**Injection Rules Applied**:
- Rule source: `AGENTS.md` and WI handoff packet.
- Assignee: `qa-integ`.
- Task type: review fix and verification.

## Evidence Pointers
- Files changed:
  - `src/main/java/com/atstudio/atstudio/service/UserSubscriptionService.java` - whole-KRW upgrade amount and zero-amount charge skip.
  - `src/main/java/com/atstudio/atstudio/service/UtilService.java` - preview amount rounding plus next billing fields.
  - `src/main/java/com/atstudio/atstudio/dto/util/SubscriptionChangePreviewResponse.java` - added `nextBillingDate`, `nextBillingAmount`.
  - `frontend/src/api/userSubscriptions.ts` - preview type updated.
  - `frontend/src/pages/subscriber/SubscriptionManagePage.tsx` - preview UI and zero-amount upgrade success copy.
  - `src/test/java/com/atstudio/atstudio/service/UserSubscriptionServiceTest.java` - rounding and zero-amount tests.
  - `src/test/java/com/atstudio/atstudio/service/UtilServiceTest.java` - next billing and rounding preview tests.
  - `frontend/src/pages/subscriber/SubscriptionManagePage.test.tsx` - preview UI assertion.
  - `docs/design/api-spec.md`, `docs/design/payment-integration-design.md`, `docs/design/usecase/user-subscription.md`, `docs/design/usecase/util.md`, `docs/ui/screen-flow.md`, `docs/ui/modal-list.md` - policy/API/UI documentation updates.

## Commands & Outputs
- `.\gradlew.bat test --tests "com.atstudio.atstudio.service.UserSubscriptionServiceTest" --tests "com.atstudio.atstudio.service.UtilServiceTest" --tests "com.atstudio.atstudio.controller.UserSubscriptionControllerTest"` - passed.
- `npm test -- SubscriptionManagePage.test.tsx` - passed.
- `npm run typecheck` - passed.
- `.\gradlew.bat test` - passed.
- `npm test` - passed, 14 files / 45 tests.
- `npm run lint` - passed.
- `npm run build` - passed.
- `python .agents\skills\validate-docs\scripts\validate_docs.py` - passed.
- `git diff --check origin/dev/kyoung` - passed with CRLF conversion warnings only.

## Tests
- Backend focused tests: passed.
- Backend full tests: passed.
- Frontend focused test: passed.
- Frontend full tests: passed.
- Frontend typecheck/lint/build: passed.
- Docs validation: passed.
- Diff whitespace check against `origin/dev/kyoung`: passed.

## Risks / Rollback
- Risks:
  - The chosen rounding policy is half-up to whole KRW; future product/legal review can change rounding mode if needed.
  - Live Toss behavior still depends on SR-93 operating-server key, recovery, logging, and reconciliation work.
- Rollback:
  - Revert the follow-up commit that contains WI-20260520-ATS-001.

## Follow-ups
- Keep SR-93 as the operating-server hardening backlog for live deployment readiness.

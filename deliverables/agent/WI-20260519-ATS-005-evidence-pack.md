# Evidence Pack: WI-20260519-ATS-005

## Summary (one-liner)
- Verified backend, frontend, and documentation gates and prepared explicit-file commit scope.

## Scope / DoD Check
- DoD items:
  - [x] Backend focused tests passed.
  - [x] Full backend tests passed.
  - [x] Frontend focused tests passed.
  - [x] Frontend typecheck passed.
  - [x] Frontend lint passed.
  - [x] Docs validation passed.
  - [x] `git diff --check` passed with CRLF warnings only.
  - [x] Unrelated untracked files identified and excluded from commit scope.
  - [x] Evidence packs and user summaries created.

## Reference Documents (Tier 0-2)

**Injected Context** (from WI Handoff Packet):

| Tier | Document | Reason |
|------|----------|--------|
| 0 | `docs/standards/core-principles.md` | Constitution |
| 0 | `docs/standards/development-standards.md` | Development rules |
| 1 | `docs/policies/quality-gates.md` | Verification gate |
| REQ | `deliverables/user/REQ-20260519-ATS-001.md` | Approved behavior |
| WI | `deliverables/agent/WI-20260519-ATS-002-handoff.md` | Backend scope |
| WI | `deliverables/agent/WI-20260519-ATS-003-handoff.md` | Frontend scope |
| WI | `deliverables/agent/WI-20260519-ATS-004-handoff.md` | Documentation scope |

**Injection Rules Applied**:
- Rule source: `AGENTS.md` and WI handoff packet.
- Assignee: `qa-integ`.
- Task type: integrated verification and commit preparation.

## Evidence Pointers
- Files changed:
  - `deliverables/user/REQ-20260519-ATS-001.md`
  - `deliverables/user/WI-20260519-ATS-001-summary.md` through `WI-20260519-ATS-005-summary.md`
  - `deliverables/agent/WI-20260519-ATS-001-handoff.md` through `WI-20260519-ATS-005-handoff.md`
  - `deliverables/agent/WI-20260519-ATS-001-evidence-pack.md` through `WI-20260519-ATS-005-evidence-pack.md`
  - `src/main/java/com/atstudio/atstudio/entity/UserSubscription.java`
  - `src/main/java/com/atstudio/atstudio/service/UserSubscriptionService.java`
  - `src/main/java/com/atstudio/atstudio/service/UtilService.java`
  - `src/test/java/com/atstudio/atstudio/controller/PaymentControllerTest.java`
  - `src/test/java/com/atstudio/atstudio/service/BillingAgreementApplicationServiceTest.java`
  - `src/test/java/com/atstudio/atstudio/service/UserSubscriptionServiceTest.java`
  - `src/test/java/com/atstudio/atstudio/service/UtilServiceTest.java`
  - `frontend/src/pages/subscriber/SubscriptionManagePage.tsx`
  - `frontend/src/pages/subscriber/SubscriptionPaymentPage.tsx`
  - `frontend/src/pages/subscriber/SubscriptionManagePage.test.tsx`
  - `frontend/src/pages/subscriber/SubscriptionPaymentPage.test.tsx`
  - `docs/design/payment-integration-design.md`
  - `docs/design/api-spec.md`
  - `docs/design/db-schema.md`
  - `docs/design/usecase/user-subscription.md`
  - `docs/design/index.md`
  - `docs/ui/screen-flow.md`
  - `docs/ui/modal-list.md`
  - `docs/ui/atstudio-front-list.md`
  - `docs/SR/SR-92.md`
  - `docs/SR/SR-93.md`
  - `docs/SR/index.md`
  - `docs/index.md`
- Excluded from commit:
  - `deliverables/agent/WI-20260420-ATS-*`
  - `deliverables/user/REQ-20260420-ATS-001.md`
  - `frontend/vite.err.log`
  - `frontend/vite.out.log`
  - `frontend/vite.pid`
  - `server.pid`

## Commands & Outputs
- `.\gradlew.bat test --tests "com.atstudio.atstudio.service.UserSubscriptionServiceTest" --tests "com.atstudio.atstudio.service.UtilServiceTest" --tests "com.atstudio.atstudio.service.BillingAgreementApplicationServiceTest" --tests "com.atstudio.atstudio.controller.PaymentControllerTest"` - passed.
- `.\gradlew.bat test` - passed.
- `npm test -- SubscriptionManagePage.test.tsx SubscriptionPaymentPage.test.tsx` - passed.
- `npm run typecheck` - passed.
- `npm run lint` - passed.
- `python .agents\skills\validate-docs\scripts\validate_docs.py` - passed.
- `git diff --check` - passed; reported CRLF conversion warnings only.

## Tests
- Backend focused tests: passed.
- Backend full tests: passed.
- Frontend focused tests: passed, 2 files / 12 tests.
- Frontend typecheck: passed.
- Frontend lint: passed.
- Docs validation: passed.

## Risks / Rollback
- Risks:
  - Live Toss operation still requires SR-93 environment, recovery, logging, reconciliation, and operator workflow checks.
  - Existing users without a billing agreement need a payment method registration/recovery path before upgrade can proceed.
- Rollback:
  - Before commit: `git restore --staged <listed files>` and `git restore <listed tracked files>`, then remove the new 20260519 deliverables/docs if needed.
  - After commit: `git revert <commit>`.

## Follow-ups
- Convert SR-93 into implementation REQ/WI when moving from local acceptance to operating-server hardening.


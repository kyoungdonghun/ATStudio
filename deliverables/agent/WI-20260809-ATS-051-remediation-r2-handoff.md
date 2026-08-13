# WI Remediation R2 Handoff: WI-20260809-ATS-051

[WI HEADER]

- WI ID: `WI-20260809-ATS-051-REMEDIATION-R2`
- REQ: `REQ-20260809-ATS-001` (`approved`)
- Agent: `se`
- Depends On: `WI-20260809-ATS-051-QA-CONCLUSIVE`
- Blocks: final conclusive PASS and gates

[SCOPE]

1. Fix `ATS-051-QI-06`: capture immutable review target ownership when review begins. After `processCompanyCert` resolves or rejects, touch review/detail state and start A refresh only while the open detail still owns A. A stale mutation must not increment the detail generation, set B loading/error/detail, close or rewrite B review state, or otherwise affect B.
2. Add exact component tests for schedule (b): A mutation pending, B opens and its detail read is pending, A resolves; cover A success/failure and B success/failure. B must either render its result/error normally and loading must terminate.
3. Close `ATS-051-QI-07`: add a raw URL within 255 characters whose canonical href grows beyond 255 and prove local rejection plus zero register/update calls.

[CONSTRAINTS]

- Modify only `CompanyCertManagePage.tsx`, its test, `WhitelistChannelPage.test.tsx`, and if strictly needed the existing safe URL helper/test. No docs/backend/other production edits.
- No policy/schema/dependency/external/DB effect, protected-output/ignored-secret access, commit, or push.
- Run focused tests, typecheck, ESLint, changed-file Prettier check, diff check.

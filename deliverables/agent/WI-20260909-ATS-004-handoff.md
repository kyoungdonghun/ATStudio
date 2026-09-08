---
version: 1.0
last_updated: 2026-09-09
project: ATS
owner: MA
category: reference
status: active
---

# WI-20260909-ATS-004: Frontend Refresh Session Ownership

[WI HEADER]
WI ID: WI-20260909-ATS-004
REQ: REQ-20260909-ATS-001
Agent: se
Depends On: first implementation slot available
Blocks: 010, 007, 008, 009, 014

[WI SUMMARY]
Scope: SEC-04 only. Bind refresh persistence, queued replay and failure cleanup to the initiating auth session; delayed response must not restore a logged-out session, overwrite A->B or same-user relogin, clear a newer session, or replay old writes as a new user. Preserve coalescing and skipAuthReplay financial protection. Add deferred success/failure/queue regressions.
Write ownership: frontend/src/api/client.ts, client.test.ts; frontend/src/store/authStore.ts, authStore.test.ts; safeStorage or related auth helpers only if indispensable. No UI design/wording, backend/build/package-lock changes.
Constraints: Use existing session-generation guards/patterns; avoid circular imports. Test actual ownership invariants rather than only mocks returning arbitrary different tokens. Read react-best-practices. MA manages test processes; no authenticated live browser actions.
Forbidden: touching unrelated changes, DDL/real DB/media deletion or repair, credential inspection, external payments/refunds/mail, runtime restart, Git writes, subdelegation. Product edits via apply_patch only; existing fake-provider/H2/temp-file tests only. MA serializes Gradle/npm runs; send requested command/test list, do not start heavy runners independently.

[ACCEPTANCE CRITERIA]
- [ ] Concrete regression scenarios fail for the original defect (when executed) and pass after the minimal fix; distinguish unrun from passed.
- [ ] Existing policy/authorization/idempotency/strict integrity guards retained; no schema or broad abstraction churn.
- [ ] Shared-file boundaries respected; list exact changed paths and test candidates/risks.
- [ ] Evidence pack and user summary created; hand back to downstream WI rather than declare production GO.

[INPUT POINTERS]
Tier 0: docs/standards/core-principles.md; docs/standards/documentation-standards.md; docs/standards/development-standards.md; docs/standards/glossary.md.
Tier 1: docs/policies/security-policy.md; docs/policies/quality-gates.md.
Tier 2: deliverables/user/REQ-20260909-ATS-001.md; deliverables/agent/WI-20260908-ATS-018-findings.md; WI-20260908-ATS-015/016/017-evidence-pack.md (only relevant scope); docs/design/api-spec.md; current related usecase/design docs. Frontend work: .agents/skills/react-best-practices/SKILL.md.
Snapshot: main 8161f0a; prior SR-93/audit documents already dirty/untracked and protected. No secrets/data reads needed.

[OUTPUT CONTRACT]
Implement directly in the shared workspace, only owned paths. User-facing: deliverables/user/WI-20260909-ATS-004-summary.md.
Agent-facing: deliverables/agent/WI-20260909-ATS-004-evidence-pack.md using create-wi-evidence-pack.
Report exact files, root cause, tests run/not run, deployment impact and remaining dependency. Do not edit this handoff or other agents' reports.

[TRACEABILITY REQUIREMENTS]
MA review return (2026-09-09): WI010 F1 identifies an equivalent stale authentication response at SocialLoginPage. Ownership extends to SocialLoginPage.tsx and its tests; compare LoginPage only for this same authentication-entrypoint response race and correct it if confirmed. Preserve return targets, profile continuation, ordinary success/failure and StrictMode consume-once behavior. No UI copy/redesign or OAuth provider activation. WI010 re-review and refreshed isolated test evidence are required; the earlier 1516-test run is a checkpoint before this follow-up.

Every relevant code/test change maps to a finding ID. Source pointers and reproducible safe tests required. No test weakening, no historical-data cleanup. English technical reports; Korean concise completion message.

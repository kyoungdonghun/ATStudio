[WI HEADER]
WI ID: WI-20260809-ATS-036
REQ: REQ-20260809-ATS-001
Agent: se
Depends On: WI-20260809-ATS-031
Blocks: WI-20260809-ATS-042, WI-20260809-ATS-043, WI-20260809-ATS-053, WI-20260809-ATS-057, WI-20260809-ATS-060

[WI SUMMARY]
Why: `CR-031-121` proves that requests queued behind an in-progress token refresh are replayed without being marked as already retried. If such a replay receives another 401, it can start another refresh cycle instead of failing closed.
Scope (in/out):
- In: Mark both the refresh-leading request and every queued request exactly once before replay; reject a second 401 without refresh, queueing, or another replay; preserve auth exclusions, `skipAuthReplay`, token rotation, storage failure handling, ADMIN 403 role synchronization, and existing navigation behavior.
- In: Add focused deterministic tests for concurrent delayed 401s, a queued replay that receives a second 401, one-refresh ownership, and unchanged exclusion behavior.
- In: Update current auth/security documentation only when the verified implementation contract is missing or stale.
- Out: Authentication policy changes, token-storage architecture changes, OAuth/provider calls, real mail, backend API changes, schema/data changes, dependency changes, and unrelated frontend refactoring.
DoD:
- Every replayed request carries one internal retry marker before `client(...)` is called.
- A replayed request that receives a second 401 rejects the original second failure and does not refresh or replay again.
- Concurrent first 401s share exactly one refresh request and each protected request is replayed at most once.
- Focused tests plus frontend full tests, coverage, typecheck, ESLint, Prettier check, and build pass.
- Evidence Pack and user summary record exact files, commands, results, rollback, and any residual risk.
Constraints/Forbidden:
- Do not read or expose ignored local secrets or environment values.
- Do not execute OAuth, mail, payment, refund, Provider, schema, or data side effects.
- Do not inspect, alter, stage, or remove `output/client-demo-screenshots-20260716-140514.zip` or `output/ui-ux-audit/`.
- Do not alter user-visible authentication policy, redirect policy, or session lifetime.
- Keep changes limited to the auth replay owner, focused tests, directly stale documentation, and WI deliverables.

[ACCEPTANCE CRITERIA]
Functional:
- [x] The request that initiates refresh is marked retried before refresh starts.
- [x] Requests queued behind refresh are marked retried before replay.
- [x] A queued replay returning 401 is rejected without a second refresh, requeue, or request replay.
- [x] Concurrent first 401s issue one refresh and replay each eligible request no more than once.
- [x] `skipAuthReplay`, auth endpoint exclusions, missing-refresh-token handling, storage fail-closed behavior, and ADMIN 403 synchronization retain their current behavior.
Performance:
- [x] Queue processing remains bounded to the number of requests waiting on the single in-flight refresh.
- [x] No polling, timer, or unbounded retry mechanism is introduced.
Quality:
- [x] Focused `frontend/src/api/client.test.ts` suite passes.
- [x] Frontend full tests and configured coverage thresholds pass.
- [x] Typecheck, ESLint, Prettier check, and production build pass.
- [x] Documentation validation and `git diff --check` pass if documentation is changed.
- [x] Independent PG security review confirms exactly-once replay and fail-closed second rejection (`PASS`, 2026-08-13).

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md

Tier 0 (Standards - Based on Assignee):
- docs/standards/development-standards.md
- docs/standards/frontend-standards.md

Tier 1 (Policies - Inferred from REQ/WI content):
- docs/policies/security-policy.md
- docs/policies/quality-gates.md

Tier 2 (Current contracts):
- docs/design/api-spec.md
- docs/design/p1-security-acceptance-hardening-design.md

REQ/Context Docs:
- deliverables/user/REQ-20260809-ATS-001.md
- deliverables/agent/WI-20260809-ATS-031-consolidated-findings.md:685
- deliverables/agent/WI-20260809-ATS-031-consolidated-findings.md:965

Files:
- frontend/src/api/client.ts
- frontend/src/api/client.test.ts
- frontend/src/store/authStore.ts
- frontend/src/store/authStore.test.ts
- frontend/package.json

Repro/Logs:
- `cd frontend; npm test -- --run src/api/client.test.ts`
- Use deterministic deferred refresh and adapter promises; no external request is permitted.

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260809-ATS-036-summary.md:
- Korean summary, fixed behavior, validation, risks, and approval points.
Agent-facing -> deliverables/agent/WI-20260809-ATS-036-evidence-pack.md:
- Evidence pointers, patch notes, exact reproduction/tests, security review outcome, rollback, and follow-up WI chain.
Handoff Packet -> deliverables/agent/WI-20260809-ATS-036-handoff.md:
- This packet.

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required
Tests: Include exact focused and full frontend commands with counts/results.
Rollback: Revert the retry-marker and focused test/doc changes as one WI-scoped patch; no data rollback is required.

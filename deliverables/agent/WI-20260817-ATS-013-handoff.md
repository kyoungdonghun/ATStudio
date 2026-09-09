[WI HEADER]
WI ID: WI-20260817-ATS-013
REQ: REQ-20260817-ATS-009
Agent: re
Depends On: -
Blocks: WI-20260817-ATS-014

[WI SUMMARY]
Why: Restore the release quality gate by aligning stale test fixtures and assertions with the already-approved consent, deterministic authentication-error, and duplicate-logout contracts.

Scope (in/out):
- In: Only test fixtures/assertions and their test-only mocks necessary to express the current implementation contract.
- Out: Runtime application behavior, API contracts, production configuration, dependency versions, database changes, documentation changes.

DoD:
- The focused backend registration-rate-limit test sends required consent values and still proves the intended rate-limit behavior.
- The frontend login test separately proves code-bearing invalid credentials and safe generic fallback behavior.
- The Header coverage test mocks all imported test dependencies and proves same-tick duplicate logout suppression.
- Focused tests and the full backend/frontend suites are green, or any remaining failure is isolated with exact evidence.

Constraints/Forbidden:
- Do not change runtime Java or TypeScript application source to satisfy a stale test.
- If an assertion contradicts documented current behavior, stop and report rather than changing product behavior.
- Preserve safe user-facing Korean copy and do not expose secrets.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Consent-required registration remains public and rate-limited after five accepted registrations.
- [ ] `INVALID_CREDENTIALS` produces the credential message; an unclassified 401 produces the safe generic message.
- [ ] A pending logout accepts only one same-tick logout command and does not produce an unhandled Vitest mock export error.
Quality:
- [ ] `gradlew.bat test` passes.
- [ ] `npm test -- --run` passes.
- [ ] `npm run typecheck`, `npm run lint`, and `npm run format` pass.

[INPUT POINTERS]
Tier 0 (Constitution - Required):
- docs/standards/core-principles.md

Tier 1 (Policies):
- docs/policies/quality-gates.md
- docs/policies/security-policy.md

Tier 2 (Frontend):
- .agents/skills/react-best-practices/AGENTS.md
- docs/design/api-spec.md

REQ/Context Docs:
- deliverables/user/REQ-20260817-ATS-009.md
- docs/SR/SR-93.md

Files:
- src/test/java/com/atstudio/atstudio/controller/SecurityFilterChainTest.java:216-241
- src/main/java/com/atstudio/atstudio/dto/user/RegisterRequest.java:51-62
- frontend/src/test/coverage/publicAuthShell.coverage.test.tsx:541-570
- frontend/src/api/authError.ts:34-45
- frontend/src/test/coverage/shellCatalogRouterGaps.coverage.test.tsx:132-137
- frontend/src/test/coverage/shellCatalogRouterGaps.coverage.test.tsx:605-622
- frontend/src/layouts/Header.tsx:194-207
- frontend/src/store/authStore.ts:32-51

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260817-ATS-013-summary.md:
- Summary, changed test contracts, test results, risks.
Agent-facing -> deliverables/agent/WI-20260817-ATS-013-evidence-pack.md:
- Evidence pointers, changed files, commands, results, rollback.
Handoff Packet -> deliverables/agent/WI-20260817-ATS-013-handoff.md:
- This packet.

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required.
Tests: Record focused and full suite commands/results.
Rollback: Revert only the test-fixture commit; no runtime behavior is changed.

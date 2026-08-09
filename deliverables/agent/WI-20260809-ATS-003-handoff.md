[WI HEADER]
WI ID: WI-20260809-ATS-003
REQ: REQ-20260808-ATS-004
Agent: qa-fe
Depends On: WI-20260808-ATS-024 verification failure
Blocks: WI-20260808-ATS-024

[WI SUMMARY]
Why: The full frontend suite found stale coverage-test display expectations after Usage tags became explicitly hash-prefixed in the admin UI.
Scope (in/out): Update only the stale Usage-tag display assertions in adminSubscriberPages.coverage.test.tsx and verify the focused behavior. Stored/API names remain unprefixed. Product code, UI policy, DB state, and unrelated tests are out of scope.
DoD: The test expects the approved hash-prefixed display contract while preserving unprefixed request payloads; the focused test passes; no unrelated file is changed.
Constraints/Forbidden: Work only on the current development branch. Do not touch secrets, DB data/schema, external providers, the intentional screenshot ZIP, or unrelated dirty-worktree changes. Do not commit or push.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Usage-tag display assertions match the approved hash-prefixed display contract while API payload assertions remain unprefixed.
- [ ] The focused test file passes with zero failures.
Performance:
- [ ] No production runtime path is changed.
Quality:
- [ ] The patch is limited to the stale test expectation.
- [ ] Evidence records the original failure and focused rerun command.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md

Tier 0 (Standards - Based on Assignee):
- docs/standards/development-standards.md
- docs/standards/frontend-standards.md

Tier 1 (Policies - Inferred from REQ/WI content):
- docs/policies/quality-gates.md

REQ/Context Docs:
- deliverables/user/REQ-20260808-ATS-004.md
- deliverables/agent/WI-20260808-ATS-024-handoff.md

Files:
- frontend/src/test/coverage/adminSubscriberPages.coverage.test.tsx
- frontend/src/pages/admin/TagManagePage.tsx

Repro/Logs:
- npm run test -- --reporter=json --outputFile=<temporary file>

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260809-ATS-003-summary.md
Agent-facing -> deliverables/agent/WI-20260809-ATS-003-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260809-ATS-003-handoff.md

[TRACEABILITY REQUIREMENTS]
Evidence pointers, executed commands, focused test result, changed files, risk, rollback, and WI-024 unblock status are required.

[WI HEADER]
WI ID: WI-20260526-ATS-004
REQ: REQ-20260526-ATS-001
Agent: se/qa-fe
Depends On: WI-20260526-ATS-002
Blocks: WI-20260526-ATS-005

[WI SUMMARY]
Why: Admin operators need to import and inspect settlement mismatches without direct API calls.
Scope (in/out): In scope is frontend admin API client additions and `/admin/payments` settlement tab with upload/list/filter/status UI. Out of scope is backend policy changes, Toss API integration, tax invoice UI, and payout UI.
DoD: Admin can upload a settlement CSV file, view import results, filter settlement rows by status, and inspect mismatch information.
Constraints/Forbidden: Do not show raw secrets or raw provider payload. Keep admin UI dense and operational. Dangerous actions require clear confirmation.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Settlement tab is visible in `/admin/payments`.
- [ ] Import form accepts configured file type and shows result counts.
- [ ] Settlement list shows status, amounts, dates, matched local references, and mismatch reason.
Performance:
- [ ] List uses backend pagination.
Quality:
- [ ] `npm run typecheck`, `npm run lint`, and `npm run build` pass.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 2 (Tech Stack):
- .agents/skills/react-best-practices/AGENTS.md
- docs/standards/frontend-standards.md

Tier 2 (UI/API):
- docs/design/api-spec.md
- docs/ui/atstudio-front-list.md
- docs/ui/screen-flow.md

REQ/Context Docs:
- deliverables/user/REQ-20260526-ATS-001.md
- deliverables/agent/WI-20260526-ATS-002-evidence-pack.md

Files:
- frontend/src/api/admin.ts
- frontend/src/pages/admin/PaymentReadOnlyPage.tsx
- frontend/src/pages/admin/PaymentReadOnlyPage.module.css

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260526-ATS-004-summary.md :
- UI summary, manual acceptance points, risks.
Agent-facing -> deliverables/agent/WI-20260526-ATS-004-evidence-pack.md :
- Files changed, commands, screenshots/manual notes if available.
Handoff Packet -> deliverables/agent/WI-20260526-ATS-004-handoff.md :
- This packet.

[TRACEABILITY REQUIREMENTS]
Evidence pointers: Required.
Tests: frontend typecheck/lint/build.
Rollback: Revert frontend settlement tab/client additions.

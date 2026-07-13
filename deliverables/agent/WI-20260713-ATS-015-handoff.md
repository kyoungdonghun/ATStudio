[WI HEADER]
WI ID: WI-20260713-ATS-015
REQ: REQ-20260713-ATS-001
Agent: qa
Depends On: WI-20260713-ATS-012
Blocks: WI-20260713-ATS-017

[WI SUMMARY]
Why: Verify production builds for both backend and frontend.
Scope (in/out): Run full Gradle build and Vite production build. Do not deploy or call external systems.
DoD: Both builds exit 0 and generated tracked metadata is cleaned.
Constraints/Forbidden: No product edits, deployment, live DB, Toss, or SMTP calls.

[ACCEPTANCE CRITERIA]
- [ ] `.\gradlew.bat build` exits 0.
- [ ] `npm run build` exits 0.
- [ ] No generated tracked file remains modified.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
Tier 1:
- docs/policies/quality-gates.md
Tier 2:
- docs/standards/frontend-standards.md
REQ/Context Docs:
- deliverables/user/REQ-20260713-ATS-001.md
Files:
- build.gradle
- frontend/package.json

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260713-ATS-015-summary.md
Agent-facing -> deliverables/agent/WI-20260713-ATS-015-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260713-ATS-015-handoff.md

[TRACEABILITY REQUIREMENTS]
Commands, exits, duration, generated-file cleanup, risks, and rollback: Required

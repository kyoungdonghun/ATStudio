[WI HEADER]
WI ID: WI-20260711-ATS-008
REQ: REQ-20260711-ATS-001
Agent: cr
Depends On: WI-20260711-ATS-001, WI-20260711-ATS-002, WI-20260711-ATS-003, WI-20260711-ATS-004, WI-20260711-ATS-005
Blocks: WI-20260711-ATS-009, WI-20260711-ATS-018

[WI SUMMARY]
Why: Reconcile all non-payment/non-whitelist/certification domains and independently challenge release-blocking findings.
Scope (in/out): Cover auth/social/email, user/profile/withdrawal, roles/routes, tracks/search/stream/download/license, playlists/albums/likes/history, inquiries/notices, admin/settings/stats, storage and shared frontend behavior. Read-only except WI outputs.
DoD: Produce domain matrices, verified P0/P1 findings, doc drift, test gaps, and explicitly rejected false positives.
Constraints/Forbidden: Do not upload active content, retrieve raw media, send email, mutate data, or print secrets. Static proof only for exploit chains.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Verify auth/social token order, role separation, withdrawal/session lifecycle, and email failure behavior.
- [ ] Verify original-audio/static-upload access and stored-active-content chain without executing it.
- [ ] Reconcile search, playback, download/license, playlist/album, history, inquiry, notice, settings, and admin contracts.
- [ ] Validate frontend findings against backend and docs; reject overstatements explicitly.
Performance:
- [ ] Assess pagination, unbounded list, request race, and large-component risks with evidence.
Quality:
- [ ] Final findings use one severity scale and include exploit/impact prerequisites.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 1:
- docs/policies/security-policy.md
- docs/policies/access-control-policy.md
- docs/policies/quality-gates.md

Tier 2:
- docs/design/api-spec.md
- docs/design/usecase/
- docs/ui/
- docs/client/
- docs/audit/backend-audit-report.md
- docs/standards/frontend-standards.md
- .agents/skills/react-best-practices/AGENTS.md

REQ/Context Docs:
- deliverables/user/REQ-20260711-ATS-001.md
- deliverables/agent/WI-20260711-ATS-001-evidence-pack.md
- deliverables/agent/WI-20260711-ATS-002-evidence-pack.md
- deliverables/agent/WI-20260711-ATS-003-evidence-pack.md
- deliverables/agent/WI-20260711-ATS-004-evidence-pack.md
- deliverables/agent/WI-20260711-ATS-005-evidence-pack.md

Files:
- src/main/java/com/atstudio/atstudio/config/
- src/main/java/com/atstudio/atstudio/security/
- src/main/java/com/atstudio/atstudio/controller/
- src/main/java/com/atstudio/atstudio/service/
- src/main/java/com/atstudio/atstudio/dto/
- src/main/java/com/atstudio/atstudio/entity/
- frontend/src/

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260711-ATS-008-summary.md : Korean cross-domain verdict
Agent-facing -> deliverables/agent/WI-20260711-ATS-008-evidence-pack.md : matrices, adjudicated findings, evidence, tests, follow-ups
Handoff Packet -> deliverables/agent/WI-20260711-ATS-008-handoff.md : this packet

[TRACEABILITY REQUIREMENTS]
Evidence pointers: Required
Tests: Static/focused non-destructive inputs only; no exploit execution
Rollback: Remove only this WI's two owned outputs if explicitly requested

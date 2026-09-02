[WI HEADER]
WI ID: WI-20260902-ATS-001
REQ: REQ-20260902-ATS-001
Agent: se
Depends On: -
Blocks: WI-20260902-ATS-002, WI-20260902-ATS-003

[WI SUMMARY]
Why: Establish exact Runtime Environment Contract ownership before changing runtime guards or data.
Scope (in/out): Inventory active backend startup paths, acceptance lifecycle bundle requirements, DB-to-storage references across all persistent asset domains, and the current development missing-media state. Do not alter source, configuration, DB, media, process ownership, or client worktree.
DoD: Produce a secret-safe contract matrix and a file-level implementation plan that identifies reusable lifecycle components, missing validation points, all storage domains, test seams, and the exact data-mutation boundary.
Constraints/Forbidden: Do not read or emit secret values; do not copy, move, delete, or hash private media; do not stop/restart processes; do not modify ignored `application-local.yml`; do not touch the client worktree.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Development, acceptance, and production-rehearsal runtime contracts are distinguished with explicit DB/public/private storage ownership.
- [ ] Every persistent asset domain is mapped to its storage root and reference table/entity.
- [ ] Current historical Track mismatch is classified with supported evidence, not inferred deletion.
Quality:
- [ ] No source/config/data mutations.
- [ ] Evidence is secret-safe and reproducible.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 1 (Policies - Inferred):
- docs/policies/security-policy.md
- docs/policies/quality-gates.md

Tier 2 (Runtime and product context):
- scripts/acceptance/README.md
- scripts/acceptance/AcceptanceLifecycle.psm1
- src/main/resources/application.yml
- src/main/resources/application-acceptance.yml
- src/main/java/com/atstudio/atstudio/service/storage/LocalStorageService.java
- src/main/java/com/atstudio/atstudio/service/storage/StorageReferenceChecker.java
- deliverables/user/REQ-20260902-ATS-001.md

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260902-ATS-001-summary.md:
- Runtime ownership findings, missing-media classification, risks, and implementation boundary.
Agent-facing -> deliverables/agent/WI-20260902-ATS-001-evidence-pack.md:
- Commands, source pointers, inventory evidence, and follow-up WI readiness.
Handoff Packet -> deliverables/agent/WI-20260902-ATS-001-handoff.md:
- This packet.

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required.
Tests: Read-only endpoint and source/config inspection only.
Rollback: Not applicable; no mutation is allowed.

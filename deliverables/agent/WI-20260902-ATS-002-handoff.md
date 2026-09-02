[WI HEADER]
WI ID: WI-20260902-ATS-002
REQ: REQ-20260902-ATS-001
Agent: se
Depends On: WI-20260902-ATS-001
Blocks: WI-20260902-ATS-004

[WI SUMMARY]
Why: Prevent acceptance/production-like runtimes from silently using fallback storage and let ADMIN operators detect DB-to-storage reference gaps without exposing storage keys.
Scope (in/out): Implement explicit storage-root validation, acceptance startup enforcement, non-secret integrity reporting, and focused backend/script tests. Do not change runtime DB/media data, copy files, change client worktrees, or call external providers.
DoD: Acceptance startup rejects missing/relative/overlapping storage roots before ready; integrity reporting covers all persistent storage domains without returning raw paths; strict acceptance readiness rejects missing references; development records a safe warning/report instead of stopping unrelated service.
Constraints/Forbidden: Never expose DB credentials, tokens, private storage keys, original filenames, or file bytes in response/log evidence. Preserve direct local development fallback but surface its integrity state. Do not stop existing services or mutate data.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Acceptance bundle requires both storage root variables.
- [ ] Explicit-root guard rejects blank, relative, same, or nested public/private roots in strict environments.
- [ ] Admin-only integrity endpoint returns aggregate/count/opaque record ID information only.
- [ ] Acceptance readiness includes integrity verification and does not mark an inconsistent runtime ready.
- [ ] Startup integrity audit distinguishes strict failure from development warning.
Quality:
- [ ] Focused Java and PowerShell tests cover happy and failure paths.
- [ ] Existing backend tests/build pass.
- [ ] API/design documentation is updated by the dependent documentation WI.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 1:
- docs/policies/security-policy.md
- docs/policies/quality-gates.md

Tier 2:
- deliverables/user/REQ-20260902-ATS-001.md
- deliverables/agent/WI-20260902-ATS-001-evidence-pack.md
- scripts/acceptance/AcceptanceLifecycle.psm1
- scripts/acceptance/test-backend-environment.ps1
- scripts/acceptance/test-dry-run.ps1
- src/main/resources/application.yml
- src/main/resources/application-acceptance.yml
- src/main/java/com/atstudio/atstudio/service/storage/LocalStorageService.java
- src/main/java/com/atstudio/atstudio/service/storage/StorageReferenceChecker.java
- src/main/java/com/atstudio/atstudio/service/storage/StorageMutationRecoveryService.java

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260902-ATS-002-summary.md.
Agent-facing -> deliverables/agent/WI-20260902-ATS-002-evidence-pack.md.
Handoff Packet -> deliverables/agent/WI-20260902-ATS-002-handoff.md.

[TRACEABILITY REQUIREMENTS]
Evidence pointers, focused tests, non-secret runtime output, and rollback notes are required.

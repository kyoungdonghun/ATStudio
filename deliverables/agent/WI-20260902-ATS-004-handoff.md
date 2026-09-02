[WI HEADER]
WI ID: WI-20260902-ATS-004
REQ: REQ-20260902-ATS-001
Agent: qa-integ
Depends On: WI-20260902-ATS-002, WI-20260902-ATS-003
Blocks: -

[WI SUMMARY]
Why: Independently verify that the runtime storage contract detects mismatches without creating a new disclosure, availability, or lifecycle regression.
Scope: Review storage guard, startup audit, ADMIN endpoint, acceptance lifecycle order, tests, and documents against the approved REQ. Classify findings as blocking, maintenance, or separate data-operation items. Do not alter code, processes, DB rows, media, ignored configuration, or client worktrees.
DoD: Produce a three-way code/config/document review with test evidence and an explicit statement of what remains outside this REQ.

[ACCEPTANCE CRITERIA]
- [ ] Acceptance cannot spawn a new runtime with absent, relative, equal, or nested roots.
- [ ] Production-like profiles cannot use implicit roots or a non-strict audit.
- [ ] ADMIN response and logs avoid storage keys, filenames, bytes, and repair actions.
- [ ] Every persistent file-reference domain is included in inspection.
- [ ] Documentation matches code and does not overclaim production readiness.
- [ ] Findings distinguish existing data remediation from code defects.

[INPUT POINTERS]
- docs/standards/core-principles.md
- docs/standards/development-standards.md
- docs/policies/security-policy.md
- deliverables/user/REQ-20260902-ATS-001.md
- deliverables/agent/WI-20260902-ATS-002-evidence-pack.md
- deliverables/agent/WI-20260902-ATS-003-evidence-pack.md
- src/main/java/com/atstudio/atstudio/service/storage/LocalStorageService.java
- src/main/java/com/atstudio/atstudio/service/storage/StorageIntegrityService.java
- src/main/java/com/atstudio/atstudio/config/StorageIntegrityStartupGuard.java
- scripts/acceptance/AcceptanceLifecycle.psm1
- docs/design/runtime-storage-operations.md

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260902-ATS-004-summary.md
Agent-facing -> deliverables/agent/WI-20260902-ATS-004-evidence-pack.md

[TRACEABILITY REQUIREMENTS]
Record tests and exact review findings. No mutation is allowed.

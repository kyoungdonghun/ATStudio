[WI HEADER]
WI ID: WI-20260713-ATS-002
REQ: REQ-20260713-ATS-001
Agent: sa
Depends On: WI-20260713-ATS-001
Blocks: WI-20260713-ATS-003, WI-20260713-ATS-004, WI-20260713-ATS-005

[WI SUMMARY]
Why: Define exact contracts, transaction boundaries, compatibility strategy, and test seams for the three confirmed P0 findings before implementation.
Scope (in/out): Inspect current source/docs/tests and create one canonical English remediation design. No product-source edits, DB/file moves, provider/email calls, or data changes.
DoD: Design covers protected-media routing and legacy compatibility, secret-free mail failure behavior, withdrawal/local-renewal/provider-cleanup ordering, API compatibility, migration/rollback, and focused acceptance tests.
Constraints/Forbidden: Preserve public preview and subscriber download behavior. Do not automate refunds. Local renewal blocking must not depend on successful Provider billing-key deletion. Actual existing-file movement requires later approval.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Public DTO/static resource contract no longer requires an original storage key.
- [ ] Preview/original storage and legacy-path handling are defined without a fail-open path.
- [ ] External generic mail responses and internal non-sensitive failure outcomes are separated.
- [ ] Withdrawal is idempotent and locally non-renewable before Provider cleanup.
- [ ] Provider cleanup failure produces a retryable, non-sensitive operational record.
Performance:
- [ ] Resource routing and renewal filtering avoid unbounded per-request work.
Quality:
- [ ] Each design decision maps to current source, tests, and an explicit acceptance case.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
Tier 1:
- docs/policies/security-policy.md
- docs/policies/quality-gates.md
Tier 2:
- docs/design/api-spec.md
- docs/design/codex-payment-integration-design.md
- docs/audit/full-system-audit-20260713.md
REQ/Context Docs:
- deliverables/user/REQ-20260713-ATS-001.md
- deliverables/agent/WI-20260711-ATS-016-evidence-pack.md
- deliverables/agent/WI-20260711-ATS-017-evidence-pack.md
Files:
- src/main/java/com/atstudio/atstudio/config/SecurityConfig.java
- src/main/java/com/atstudio/atstudio/config/WebConfig.java
- src/main/java/com/atstudio/atstudio/dto/track/TrackResponse.java
- src/main/java/com/atstudio/atstudio/service/TrackService.java
- src/main/java/com/atstudio/atstudio/service/DownloadService.java
- src/main/java/com/atstudio/atstudio/service/EmailService.java
- src/main/java/com/atstudio/atstudio/service/UserService.java
- src/main/java/com/atstudio/atstudio/service/UserSubscriptionService.java
- src/main/java/com/atstudio/atstudio/repository/BillingAgreementRepository.java
- src/main/java/com/atstudio/atstudio/service/payment/
- src/test/

[OUTPUT CONTRACT]
Design -> docs/design/p0-release-blocker-remediation-design.md
User-facing -> deliverables/user/WI-20260713-ATS-002-summary.md
Agent-facing -> deliverables/agent/WI-20260713-ATS-002-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260713-ATS-002-handoff.md

[TRACEABILITY REQUIREMENTS]
Evidence pointers and decision-to-test matrix: Required
Tests: Static inspection only in this WI
Rollback: Remove only the design and this WI's two owned outputs if explicitly requested

---
version: 1.0
last_updated: 2026-09-09
project: ATS
owner: MA
category: reference
status: active
---

# WI-20260909-ATS-001: Backend Authentication and Sensitive Validation Logs

[WI HEADER]
WI ID: WI-20260909-ATS-001
REQ: REQ-20260909-ATS-001
Agent: pg
Depends On: none
Blocks: 010, 006, 013, 014

[WI SUMMARY]
Scope: SEC-01/02/03/05 and AUTH-06 from WI-20260908-ATS-018. Implement typed access/refresh JWT validation and per-issuance refresh identity; prevent refresh-as-API-Bearer and wrong-type refresh; sanitized validation diagnostics including throwable leakage; atomic password-reset consume; distinguish password-user verification from existing social login without enabling providers or trusting arbitrary emails. Preserve current roles and normal access TTL. Add real-token, fixed-time/uniqueness, captured-log and transactional concurrency tests.
Write ownership: security/JwtTokenProvider.java, JwtAuthenticationFilter.java; service/auth/AuthService.java; service/EmailService.java; common/exception/GlobalExceptionHandler.java; repository/PasswordResetTokenRepository.java if needed; corresponding/new auth/security/log tests. Do not edit UserRepository, frontend, payment, storage, build/schema/config or docs outside this WI reports.
Constraints: Do not keep typeless JWT acceptance as a fallback that reopens SEC-01. Explain existing-session re-login impact as a security correction. Use current entity fields and locks; no DDL or actual reset/mail. Pair negative JWT tests with existing 100% critical-class coverage gates. Coordinate any shared test-file writes with MA.
Forbidden: touching unrelated changes, DDL/real DB/media deletion or repair, credential inspection, external payments/refunds/mail, runtime restart, Git writes, subdelegation. Product edits via apply_patch only; existing fake-provider/H2/temp-file tests only. MA serializes Gradle/npm runs; send requested command/test list, do not start heavy runners independently.

[ACCEPTANCE CRITERIA]
- [ ] Concrete regression scenarios fail for the original defect (when executed) and pass after the minimal fix; distinguish unrun from passed.
- [ ] Existing policy/authorization/idempotency/strict integrity guards retained; no schema or broad abstraction churn.
- [ ] Shared-file boundaries respected; list exact changed paths and test candidates/risks.
- [ ] Evidence pack and user summary created; hand back to downstream WI rather than declare production GO.

[INPUT POINTERS]
Tier 0: docs/standards/core-principles.md; docs/standards/documentation-standards.md; docs/standards/development-standards.md; docs/standards/glossary.md.
Tier 1: docs/policies/security-policy.md; docs/policies/quality-gates.md.
Tier 2: deliverables/user/REQ-20260909-ATS-001.md; deliverables/agent/WI-20260908-ATS-018-findings.md; WI-20260908-ATS-015/016/017-evidence-pack.md (only relevant scope); docs/design/api-spec.md; current related usecase/design docs. Frontend work: .agents/skills/react-best-practices/SKILL.md.
Snapshot: main 8161f0a; prior SR-93/audit documents already dirty/untracked and protected. No secrets/data reads needed.

[OUTPUT CONTRACT]
Implement directly in the shared workspace, only owned paths. User-facing: deliverables/user/WI-20260909-ATS-001-summary.md.
Agent-facing: deliverables/agent/WI-20260909-ATS-001-evidence-pack.md using create-wi-evidence-pack.
Report exact files, root cause, tests run/not run, deployment impact and remaining dependency. Do not edit this handoff or other agents' reports.

[TRACEABILITY REQUIREMENTS]
Every relevant code/test change maps to a finding ID. Source pointers and reproducible safe tests required. No test weakening, no historical-data cleanup. English technical reports; Korean concise completion message.

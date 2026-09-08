---
version: 1.0
last_updated: 2026-09-09
project: ATS
owner: MA
category: reference
status: active
---

# WI-20260909-ATS-005: Compatible Dependency and Multipart Boundary Corrections

[WI HEADER]
WI ID: WI-20260909-ATS-005
REQ: REQ-20260909-ATS-001
Agent: se
Depends On: MA official-version assessment; first slot available
Blocks: 012, 006, 007, 008, 009, 014

[WI SUMMARY]
Scope: DEP-01/02 and OPS-01 bounded correction. After MA supplies verified available versions, use a compatible Spring Boot 4.0.x security patch and existing framework APIs. Bound Tomcat multipart count consistent with valid endpoint forms and test connector setting/overflow. Dev-only npm advisory patch updates may be mechanical within existing ranges if test-compatible. No new library, major-version migration or blanket force update.
Write ownership: build.gradle, frontend/package-lock.json (package.json only compatible patch constraints necessary), src/main/java/com/atstudio/atstudio/config/AppConfig.java and its focused/new tests. No auth/payment/storage/main application settings. Dependency resolution coordinated with MA before touching installed/runtime tooling.
Constraints: Wait for version details/approval through MA within approved compatible patch scope. No actual runtime restart, real DB/provider/mail. Avoid guessed non-existent versions and arbitrary transitive overrides. If patch unavailable/incompatible, document exact gate and leave broader migration unperformed.
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

MA verified 2026-09-09: Maven Central metadata lists 4.0.8 as the latest released 4.0.x; Spring official docs list 4.0.8 stable and 4.0.9-SNAPSHOT separately. Apply 4.0.8, not a guessed 4.0.9/4.0.10 or a 4.1 migration. The published 4.0.8 BOM manages Tomcat 11.0.24, Framework 7.0.9 and Security 7.0.7. Evidence: https://docs.spring.io/spring-boot/index.html and https://repo.maven.apache.org/maven2/org/springframework/boot/spring-boot-dependencies/4.0.8/spring-boot-dependencies-4.0.8.pom . npm corrections must use package-lock-only first; do not modify the live installed frontend dependencies without MA coordination.

[OUTPUT CONTRACT]
Implement directly in the shared workspace, only owned paths. User-facing: deliverables/user/WI-20260909-ATS-005-summary.md.
Agent-facing: deliverables/agent/WI-20260909-ATS-005-evidence-pack.md using create-wi-evidence-pack.
Report exact files, root cause, tests run/not run, deployment impact and remaining dependency. Do not edit this handoff or other agents' reports.

[TRACEABILITY REQUIREMENTS]
Every relevant code/test change maps to a finding ID. Source pointers and reproducible safe tests required. No test weakening, no historical-data cleanup. English technical reports; Korean concise completion message.

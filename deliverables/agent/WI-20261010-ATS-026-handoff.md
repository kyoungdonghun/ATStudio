---
version: 1.0
last_updated: 2026-10-10
project: ATS
owner: MA
category: guide
status: active
dependencies:
  - path: ../user/REQ-20261010-ATS-008.md
    reason: Approved scope and sensitive-operation gates
---
# Current Backend Artifact Verification
[WI HEADER]
WI ID: WI-20261010-ATS-026
REQ: REQ-20261010-ATS-008
Agent: qa
Depends On: WI024
Blocks: WI027
Session: current

[WI SUMMARY]
Why: Prepare the approved existing target without exposing secrets or conflating local checks with operation.
Scope: Read approved changed-source identity then use existing Gradle wrapper OFFLINE first, bootJar plus focused storage-mutation admission tests as appropriate. Do not clean/delete old artifacts; preserve existing jar outside build only if necessary under new untracked evidence output path. Verify jar has reviewed admission classes/current application.yml and excludes local config/secrets. Hash full source inputs plus jar with exact versions. Frontend builds/tests are not assigned. No MySQL proof or startup, no remote work, no install/download without explaining missing cached dependencies.
Write set: build-generated outputs, a new non-secret artifact manifest under deliverables/agent/WI-20261010-ATS-026-artifact.json if useful, and this WI's two outputs only

Approved-scope handoff addendum (2026-10-10): REQ008 also includes React artifact preparation. After the backend checks, use the already installed frontend toolchain to run typecheck and a production build in a new WI026-specific output directory, without deleting/replacing previous dist. Record source/output hashes and tool versions. Inspect environment provenance by names only and fail closed on unexpected VITE-exposed keys; do not print private env values, embed acceptance tunnel origins, install dependencies, or start a server. This addendum supersedes the earlier sentence excluding frontend builds/tests. Runtime, browser and production integration acceptance remain unverified. Frontend source edits remain prohibited; report any blocker.

User-approved blocker resolution (2026-10-10): User explicitly answered "격리 빌드 진행 승인" after the names-only VITE_GOOGLE_CLIENT_ID gate. Produce an isolated env-free frontend build using the existing installed toolchain. Do not read/copy/edit .env.local or other private configuration; no inherited external integration values. Preserve existing source and dist. Use the actual public Vite production settings (React plugin, @ alias and default base) with configFile/envDir disabled via the installed Vite API if required, documenting the bounded equivalence and exact invocation. Use no network, dependency installation, development server, browser, provider credentials or activation. Record successful artifact hashes and append a superseding completion addendum without erasing the historical HOLD. Update this WI manifest and two reports only, apart from new generated build outputs. Final PG review must see the exact resulting identities.
Constraints: No remote commands, provider actions, credentials/secret reads or generation, SQL execution, app startup, local retained DB mutation, package installation, arbitrary deletion, new dependencies, commit/push. Preserve unrelated dirty files.
Implementation must use apply_patch. Do not delegate descendants.

[ACCEPTANCE CRITERIA]
- [ ] Named approved scope only, fresh-only fail-closed behavior with bounded evidence.
- [ ] No secret-bearing arguments/stdout/stderr/logs, raw endpoints/tokens or private configuration in reports.
- [ ] Reproducible checks; report failures accurately; no claim of production capacity or server execution.
- [ ] Two deliverables and explicit next WI.
Performance: No capacity claim; preserve 2vCPU/8GB/100GB baseline and bounded resource assumptions.

[INPUT POINTERS]
Tier 0 (injected in full):
- docs/standards/core-principles.md
- docs/standards/documentation-standards.md
- docs/standards/development-standards.md
- docs/standards/glossary.md
Tier 1:
- .claude/agents/qa.md
- docs/policies/security-policy.md
- docs/policies/access-control-policy.md
- docs/policies/quality-gates.md
Tier 2:
- .agents/skills/create-wi-evidence-pack/SKILL.md
- .agents/skills/test/SKILL.md
- .agents/skills/build-check/SKILL.md
- .agents/skills/react-best-practices/AGENTS.md (conditional tech-stack pointer; no frontend code change here)
- docs/standards/frontend-standards.md (same bound)
- docs/standards/evidence-pack-standard.md
- docs/templates/eval-report-template.md
- docs/index.md#current-decision-entry
- deploy/single-vps/CONFIGURATION.md
- deploy/single-vps/runbooks/deployment.md
- scripts/reconstruction/README.md
- docs/design/db-schema.md
Context:
- deliverables/user/REQ-20261010-ATS-008.md
- deliverables/agent/WI-20261010-ATS-024-evidence-pack.md
- deliverables/agent/WI-20261010-ATS-014-evidence-pack.md
Source:
- src/main/resources/schema.sql
- src/main/resources/seed.sql
- src/main/resources/application.yml
- deploy/single-vps/env/atstudio.env.example
- deploy/single-vps/env/contract.json
- deploy/single-vps/systemd/atstudio.service
- scripts/deployment/validate_bundle.py
- build.gradle

[OUTPUT CONTRACT]
User-facing: deliverables/user/WI-20261010-ATS-026-summary.md
Agent-facing: deliverables/agent/WI-20261010-ATS-026-evidence-pack.md
Use create-wi-evidence-pack skill. Record pointers, tool versions, hashes, exact check commands/results, risks, non-destructive rollback and next WI. English docs; concise user summary.

[TRACEABILITY REQUIREMENTS]
Generated by create-wi-handoff-packet after approved REQ and maximum WI024 checked.
ATS tag from workspace.json; tier ordering and role/security/testing requirements from context-injection-rules.json. Meta-only architecture inference excluded from product bootstrap scope. Do not modify handoff.


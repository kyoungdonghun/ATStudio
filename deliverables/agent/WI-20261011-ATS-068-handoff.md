---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: MA
category: reference
status: active
dependencies:
  - path: ../user/REQ-20261011-ATS-007.md
    reason: Approved fresh functional release preparation
  - path: WI-20261011-ATS-064-handoff.md
    reason: Audio-only source owner
  - path: WI-20261011-ATS-065-handoff.md
    reason: Independent source review gate
---
# WI-20261011-ATS-068
[WI HEADER]
WI ID: WI-20261011-ATS-068
REQ: REQ-20261011-ATS-007
Agent: qa
Depends On: WI064 final source/tests and WI065 exact-source ALLOW before build
Blocks: WI067 final artifact pins and MA functional release

[WI SUMMARY]
Why: Produce a fresh audited JAR from the reviewed current source, preserving the working web artifact.
Scope (in/out): Own build/WI068-release-* helpers/outputs, deliverables/agent/WI-20261011-ATS-068-artifact.json and two reports. Initially prepare build script only; no Gradle build before MA conveys WI064/065 final source/test gate. Adapt old WI020 release audit pattern, not old approvals/pins. Reuse unchanged frontend SPA only after actual source/output byte match.
DoD: Offline isolated Gradle compileJava/bootJar with sanitized child environment, no app start/tests reading secrets, no install or auto JDK download, no clean/overwrite. Full source/build-input before/after hashes and packaged resources, archive duplicate/private filename check, fresh artifact digest/size/manifest. Preserve WI020 JAR and older manifests/frontend. Record focused test evidence and review provenance separately; no claim of full test suite or server acceptance.
Constraints/Forbidden: No src/frontend edits, private .env/application-local/key files, server/network/DB, credentials, deletion, commit/push or subdelegation. Do not hash unrelated actively-changing build trees; pin exact preserved historical artifacts and immutable references. No old build helper execution with hardcoded old output/approval paths.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Reviewed source preimages/current bytes and fresh immutable source manifest agree before/after build.
- [ ] New JAR contains correct audio-only public config/class changes and no private resource/duplicate entry.
- [ ] Current frontend336/source and143/output unchanged or HOLD; no silent rebuild/private overrides.
Quality:
- [ ] Actual exit status, tools/commands/duration/warnings and manifest hashes.
- [ ] Existing artifacts preserved; failures retained with no automatic overwrite/retry.
- [ ] Build gate applies only after explicit MA final inputs; initial result PREPARED_ONLY.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/documentation-standards.md
- docs/standards/development-standards.md
- docs/standards/glossary.md
Tier 1:
- docs/policies/security-policy.md
- docs/policies/quality-gates.md
- docs/standards/evidence-pack-standard.md
Tier 2:
- .agents/skills/build-check/SKILL.md
- .agents/skills/test/SKILL.md
- .agents/skills/create-wi-evidence-pack/SKILL.md
- docs/templates/wi-subagent-handoff-template.md
Context:
- deliverables/user/REQ-20261011-ATS-007.md
- deliverables/agent/WI-20261011-ATS-064-evidence-pack.md
- deliverables/agent/WI-20261011-ATS-065-handoff.md
- build/WI020-release-audit.ps1
- build/WI020-release.init.gradle
- deliverables/agent/WI-20261011-ATS-020-artifact.json
- deliverables/agent/WI-20261010-ATS-026-artifact.json
- build.gradle

[OUTPUT CONTRACT]
User: deliverables/user/WI-20261011-ATS-068-summary.md
Agent: deliverables/agent/WI-20261011-ATS-068-evidence-pack.md
Artifact: deliverables/agent/WI-20261011-ATS-068-artifact.json
Skill-generated handoff: this file. No subdelegation.

[TRACEABILITY REQUIREMENTS]
Exact source/review/test/artifact provenance and bounded acceptance. ATS; rule source .claude/config/context-injection-rules.json; Tier0 first. Never call old JAR current after source changes.

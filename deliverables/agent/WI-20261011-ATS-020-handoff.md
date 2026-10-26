---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: MA
category: work-summary
status: active
dependencies:
  - path: ../user/REQ-20261011-ATS-002.md
    reason: Approved local new-artifact preparation
  - path: WI-20261011-ATS-018-evidence-pack.md
    reason: Latest repair retest supersedes historical HOLD
  - path: WI-20261011-ATS-019-evidence-pack.md
    reason: Independent87-case regression
---

# WI-20261011-ATS-020 Handoff

[WI HEADER]
WI ID: WI-20261011-ATS-020
REQ: REQ-20261011-ATS-002
Agent: qa
Depends On: WI018 latest LOCAL PASS, WI01987/87
Blocks: WI021

[WI SUMMARY]
Why: provide a traceable fresh executable after background-safety changes.
Scope: Build the reviewed new backend JAR without overwriting ANY retained JAR/manifest/SPA or old class/test receipt directory. Read build-check skill. Own only build/WI020-release/ (or fresh WI020-prefixed isolated output), a WI-local init/audit script under build/WI020-*, and deliverables/agent/WI-20261011-ATS-020-artifact.json plus own evidence/summary. Never modify build.gradle, app source, existing manifests/checkers or deploy candidates.
Wait prerequisite independent WI018 and WI019 PASS and bind exact source hashes to their receipts. Use cached-only Gradle --offline --no-daemon, no clean, no full test/build (tests separately WI019), compileJava and bootJar with new buildDirectory via apply_patch init script; no app launch/remote/DB/install. Env must not import private config into build or output inherited secrets.
Capture public backend source files (src,gradle,build.gradle,settings.gradle,gradlew*) before/after and prove no drift. Verify new executable JAR includes background switch classes and new YAML; Java17 classmajor61; resources equal current public source; reject private-config/key file names, duplicate archive entries, devtools/test deps additions. Compare dependency set and ENTITY/schema/seed identity to old WI026 JAR to describe schema revalidation boundary precisely, not claim native validation of new runtime. Old jar SHA517b745479792a7eae6ce75357c7d1299ab644b50d5bc5b331fea384ccb10e0a and WI026 manifestfbeb74425ade0fe3882a9dddc4c97f7b67b75dda711ffa37acbd755a02fa4d9c must remain.
Frontend not changed: verify current previously recorded frontend336 source/SPA143 file sets with four exact historical coverage exclusions; no frontend rebuild/private env read. Record existing SPA path as separate reusable artifact.
New manifest should contain relative exact paths, bytecounts/SHA256, HEAD and dirty status, source list, included class/library/resource identity, timestamps/toolversions, approved scope, prereq receipts/source hashes, limitations not deployed; no private data/hashes.
Do NOT invoke pinned old release-readiness.py check after source changes as a fresh-release validator, patch old script/manifest, or imply old native PASS automatically covers new app. Explain intentional supersession and artifact filename users should deploy next. If discovered code/config defect, report HOLD, don't edit app.
Use evidence-pack skill, concise scoped counts and warnings; old files preserved, app/DB not started, target Linux proof pending.

[ACCEPTANCE CRITERIA]
Functional: new current-source JAR with explicit output path and immutable previous release.
Performance: build only; no production capacity claim.
Quality: offline compilation/packaging, source and archive identity, prior pins preserved.

[INPUT POINTERS]
Tier 0 (fully injected by MA):
- docs/standards/core-principles.md
- docs/standards/documentation-standards.md
- docs/standards/development-standards.md
- docs/standards/glossary.md
Tier 1:
- docs/policies/security-policy.md
- docs/policies/quality-gates.md
Tier 2:
- docs/index.md (latest Current Decision Entry only)
- deploy/single-vps/CONFIGURATION.md
- deploy/single-vps/runbooks/production-readiness.md (dated; latest index supersedes)
- deliverables/user/REQ-20261011-ATS-002.md
- deliverables/agent/WI-20261011-ATS-015-evidence-pack.md
- deliverables/agent/WI-20261010-ATS-026-artifact.json

[CONSTRAINTS]
Local-only. No SSH, real secrets, private env reads, DB service, remote API,
full application start, package installation, purchase, commit/push or deletion.
Preserve existing dirty files and immutable prior artifacts. No sub-delegation.
Use apply_patch for manual edits. Report new blocker/scope change to MA.
Tests may use isolated synthetic configurations/mocks, not retained application
environments. No dependency upgrade. No runtime Spring guard bypass.
Document target tests as NOT RUN, not implied by local checks.

[OUTPUT CONTRACT]
Use build-check and evidence-pack skills. Write new WI-20261011-ATS-020-artifact.json and
WI-20261011-ATS-020-evidence-pack.md under deliverables/agent/ and WI-20261011-ATS-020-summary.md under
deliverables/user/. Include exact artifact path/hash, dated execution environment,
full command receipts, packaging inventory and local rollback/target limitations.


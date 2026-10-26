---
version: 1.0
last_updated: 2026-10-10
project: ATS
owner: MA
category: handoff
status: active
---
# WI-20261010-ATS-004 Handoff

[WI HEADER]
WI ID: WI-20261010-ATS-004
REQ: REQ-20261010-ATS-004
Agent: sa
Depends On: -
Blocks: WI-20261010-ATS-007

[WI SUMMARY]
Why: Audit current source resource requirements before buying a small-production VPS.
Scope: Runtime stack, dependencies, WAV/MP3 pipeline, concurrency, heap/process/DB pools, stream/download implementation, storage/temp duplication and growth. Report source/code mismatches and dated TEST evidence. Optionally inspect file metadata/aggregate sizes in already identified media/build directories, never private contents. Review actual source, not assumptions from AGENTS tables.
DoD: File/line-backed sizing findings and formulas, measured versus assumed values, realistic workload scenarios, no guaranteed user capacity. Assess 2 vCPU/8GB/NVMe100GB single-host candidate.
Constraints: Read-only source and environment. No database connections, secrets/key reads, running apps/builds/load tests, installations, network/cloud mutations, price research, commits, deletions. Write only the two assigned new deliverables with apply_patch.

[ACCEPTANCE CRITERIA]
- Current branch/HEAD and latest relevant completion evidence identified.
- Java/Spring/MySQL/frontend/FFmpeg runtime dependencies and versions distinguished.
- 128kbps implementation, original/derivative paths and worker bounds verified.
- CPU/RAM/disk/transfer budget assumptions and growth examples reproducible.
- Findings divided into purchase blocker, pre-invitation task, deferred scaling.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
- docs/standards/documentation-standards.md
- docs/standards/glossary.md
Tier 1:
- docs/policies/security-policy.md
Tier 2:
- docs/index.md#current-decision-entry
- docs/design/runtime-storage-operations.md
- deploy/single-vps/README.md
- deploy/single-vps/runbooks/acceptance-capacity.md
Context:
- deliverables/user/REQ-20261010-ATS-004.md
- deliverables/agent/WI-20260909-ATS-028-evidence-pack.md
- deliverables/agent/WI-20261003-ATS-005-evidence-pack.md
- build.gradle
- frontend/package.json
- src/main/resources/application.yml
- src/main/java/com/atstudio/atstudio/

[OUTPUT CONTRACT]
- deliverables/user/WI-20261010-ATS-004-summary.md
- deliverables/agent/WI-20261010-ATS-004-evidence-pack.md
- Use create-wi-evidence-pack. English documents; Korean final digest. Report actual commands and verification limits. No unverified benchmark claims.

[TRACEABILITY REQUIREMENTS]
Exact source pointers and reproducible calculations required. Operational target NOT RUN. Blocks 007 when complete; return to MA for chain coordination. No rollback of unrelated files.

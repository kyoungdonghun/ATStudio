---
version: 1.0
last_updated: 2026-09-29
project: ATS
owner: docops
category: reference
status: active
dependencies:
  - path: ../user/REQ-20260929-ATS-001.md
    reason: Approved documentation-only refresh
---

# WI-20260929-ATS-001 Handoff

## [WI HEADER]

- WI ID: WI-20260929-ATS-001
- REQ: REQ-20260929-ATS-001
- Agent: docops
- Depends On: -
- Blocks: WI-20260929-ATS-002
- Skill: create-wi-handoff-packet; CE projection; PE scope enforcement; create-wi-evidence-pack

## [WI SUMMARY]

- Why: Make later hosting judgments follow the latest bounded completion evidence, not isolated default configuration or superseded pending notes.
- Scope: Update the seven existing entry/operating documents below; create this WI's two deliverables. Add a compact current decision table and latest-first dated checkpoints to docs/index.md. Link other entry points to it rather than duplicating large status narratives.
- DoD: Latest verification dates/environments/evidence/remaining target gates are explicit; Gmail and WAV facts match evidence and source; historical artifacts remain unchanged.
- Constraints/Forbidden: No product/config/private/global-memory changes, secrets, mail sends, provider/DB actions, restarts, commits, pushes, deletion or hosting-price evaluation. Do not rewrite historical WI/SR records, especially preserved/hash-registered artifacts. Explain supersession in current entry documents. Retain production HOLD, fresh-manifest UNRECORDED and historical missing-reference limits.

## [ACCEPTANCE CRITERIA]

Functional:
- [ ] Gmail WI011 three received/readable Korean inbox messages and WI012 website-request SUCCESS plus user receipt/reset-page opening are recorded with 2026-09-08 dates and TEST/development boundaries. No password-change submission or WI012 inbox-placement proof is invented.
- [ ] A default localhost:1025 setting is not treated as proof of effective runtime settings or absence of prior verification; external child-process overrides are explained without secret contents.
- [ ] 2026-09-09 WAV/MP3 and user-assisted phone results retain their bounded claims; FFmpeg is correctly required for new WAV-derived 128kbps playback, not falsely required for all legacy MP3 or all source checks.
- [ ] Reconstruction no longer presents completed WI020 source/history delivery as pending; private off-device backup and actual restored runtime remain distinct/open.
- [ ] AGENTS.md and CLAUDE.md gain matching evidence-reading instructions; root README and kickoff template point to the same current entry. Check actual git/source identity, latest closeout, effective configuration provenance and test environment before negative claims.
- [ ] Future sizing/cost decisions must separate measured values, dated assumptions and unverified provider terms; do not refresh old forecasts/prices here.

Performance:
- [ ] No runtime performance claim; documentation-only work.

Quality:
- [ ] Use relative links, valid metadata where existing, and original verification dates. Increment dates only for edited live documents.
- [ ] Run git diff --check for your scope; no unrelated formatting churn. WI002 performs independent documentation validation.

## [INPUT POINTERS]

Tier 0 (MA supplies relevant projections):
- docs/standards/core-principles.md
- docs/standards/documentation-standards.md
- docs/standards/development-standards.md
- docs/standards/glossary.md

Tier 1:
- .claude/agents/docops.md
- docs/policies/archive-policy.md
- docs/policies/security-policy.md
- docs/policies/quality-gates.md

Tier 2:
- docs/index.md
- docs/design/runtime-storage-operations.md
- docs/templates/ma-session-kickoff-prompt.md
- .agents/skills/create-wi-evidence-pack/SKILL.md
- .agents/skills/validate-docs/SKILL.md

REQ / Snapshot / Historical Evidence:
- deliverables/user/REQ-20260929-ATS-001.md
- deliverables/agent/WI-20260908-ATS-011-evidence-pack.md
- deliverables/agent/WI-20260908-ATS-012-evidence-pack.md
- deliverables/agent/WI-20260909-ATS-028-evidence-pack.md
- deliverables/agent/WI-20260909-ATS-029-evidence-pack.md
- deliverables/user/REQ-20260909-ATS-004.md
- docs/registry/v1-artifact-retention-20260909.md
- docs/SR/SR-93.md

Write ownership (existing files only):
- AGENTS.md
- CLAUDE.md
- README.md
- docs/index.md
- docs/payment/index.md
- scripts/reconstruction/README.md
- docs/templates/ma-session-kickoff-prompt.md

Read-only source:
- src/main/java/com/atstudio/atstudio/service/audio/FfmpegAudioEncoder.java
- src/main/java/com/atstudio/atstudio/service/audio/TrackAudioProcessingWorker.java
- src/main/resources/application.yml
- scripts/reconstruction/Test-DevelopmentRecovery.ps1

## [OUTPUT CONTRACT]

- User-facing: deliverables/user/WI-20260929-ATS-001-summary.md
- Agent-facing: deliverables/agent/WI-20260929-ATS-001-evidence-pack.md (create-wi-evidence-pack format)
- Handoff: this file, owned by MA. Do not modify it or the REQ.
- Return changed file paths, drift findings, checks and limits; do not claim final REQ closure before WI002.

## [TRACEABILITY REQUIREMENTS]

- Evidence pointers: mandatory for every status/verification assertion.
- Tests: distinguish source/doc checks from original dated runtime evidence; no new runtime test.
- Rollback: reverse only this WI's documentation hunks after review; preserve original historical artifacts.
- On completion MA must dispatch blocked WI-20260929-ATS-002 immediately.

## Related Documents

- [Approved REQ](../user/REQ-20260929-ATS-001.md)
- [Documentation Index](../../docs/index.md)

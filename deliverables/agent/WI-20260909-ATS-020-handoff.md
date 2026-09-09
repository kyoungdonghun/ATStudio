---
version: 1.0
last_updated: 2026-09-09
project: ATS
owner: qa
category: reference
status: active
dependencies:
  - path: ../user/REQ-20260909-ATS-004.md
    reason: Approved reconstruction verification
  - path: WI-20260909-ATS-018-handoff.md
    reason: Historical recovery scope
  - path: WI-20260909-ATS-019-handoff.md
    reason: Reconstruction scope
---

# WI-20260909-ATS-020 Handoff

[WI HEADER]
REQ: REQ-20260909-ATS-004
Agent: qa
Depends On: WI-20260909-ATS-018, WI-20260909-ATS-019
Blocks: none

[WI SUMMARY]
Why: Independently check whether the published working context survives a clean checkout without overstating private restoration.
Scope: Review restored historical documents/provenance and their privacy boundary; check reconstruction guide, preflight and tests against actual source. Validate exact allowed diff and documentation integrity. Parent supplies source-only export/remote-clone execution and preserved-runtime results.
DoD: Findings resolved or accurately reported; Git material versus private backup versus production gates separated. Original 205 count reconciles with prior 13/192 and current recovery publication. No unique approval/rule is silently discarded or promoted into an unsupported current claim.
Forbidden: Existing DB/media/runtime changes, external financial/mail calls, private configuration value output, original archive deletion/changes, Git commit/push, whole-product audit. Do not rerun heavy test suites unrelated to the change.

[ACCEPTANCE CRITERIA]
- [ ] Original ZIP/manifest/205 member identities remain preserved; sanitized derivatives clearly distinguish original and published hashes.
- [ ] Inspect preservation mapping and actual restored content for real Secrets/PII; do not print sensitive matches. Check whether unique decisions remain accessible.
- [ ] README reaches active rules/config/project skills, current API/DB/UI and history. No dependency on chat-only instructions or unexplained local launcher.
- [ ] Guide matches actual keyring, roots, configuration loading and scheduler behavior; no fabricated kill switch or automatic DB restore/start.
- [ ] Preflight failure cases do not read private input or run processes/services. Tests run in a private synthetic location only.
- [ ] Source-only validation is not described as a new OS install, real MySQL recovery, off-device backup or production acceptance.
- [ ] Current doc index counts/links pass, and exact staged changes contain no product/secret/runtime assets.

[INPUT POINTERS]
Tier 0: docs/standards/core-principles.md, docs/standards/development-standards.md, docs/standards/documentation-standards.md, docs/standards/glossary.md.
Tier 1: docs/policies/security-policy.md, docs/policies/archive-policy.md.
Tier 2: docs/design/runtime-storage-operations.md, docs/SR/SR-93.md, docs/registry/v1-artifact-retention-20260909.md, scripts/reconstruction/README.md.
Context: REQ004 and WI018/WI019 handoffs/evidence; README.md; scripts/reconstruction/ tests; current Git diff.
Private evidence root: %LOCALAPPDATA%/ATStudio/archives/development-recovery-20260909-094234/.
Original archive root: %LOCALAPPDATA%/ATStudio/archives/v1-artifacts-20260909-084405/ (read-only).

[OUTPUT CONTRACT]
Own files: WI020 evidence pack and user summary only, using create-wi-evidence-pack skill. Findings to parent with exact paths and actionable reasons; do not edit implementation owned by other agents. Record tested commands/outcomes, source-copy limitations, unresolved off-device backup destination, rollback boundaries and no further WI dependency.

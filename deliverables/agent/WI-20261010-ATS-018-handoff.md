---
version: 1.0
last_updated: 2026-10-10
project: ATS
owner: MA
category: handoff
status: active
dependencies:
  - path: ../user/REQ-20261010-ATS-007.md
    reason: Current approved host work and rebuild exception
  - path: WI-20261010-ATS-017-evidence-pack.md
    reason: Independent procedure review completed
---
# WI-20261010-ATS-018 Handoff

[WI HEADER]
WI ID: WI-20261010-ATS-018
REQ: REQ-20261010-ATS-007
Agent: docops
Depends On: WI-20261010-ATS-017
Blocks: -

[WI SUMMARY]
Why: Prevent stale pre-purchase/local-only records from being mistaken for latest host status.
Scope (in): Add dated first-host checkpoint to docs/index.md, related deploy/single-vps/README.md or existing runbook navigation only if needed; update REQ007 progress, own summary and Evidence Pack.
Scope (out): Historical evidence/body/hash rewriting, source/runbook implementation, secrets/endpoints/fingerprints, remote actions, purchase, commit.
DoD: Record MA-attributed live evidence separately from SE/PG local evidence; keep unmet target gates visible and REQ007 active unless truly complete.
Constraints: Use apply_patch. Preserve unrelated dirty work and historical outcomes. WAIT for MA final snapshot before finalizing dynamic progress; prepare static links/review context meanwhile. No descendants.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Current entry links REQ007, WI016/017/018 and first-host runbook.
- [ ] New checkpoint separates purchased/rebuilt/authenticated state from installed/deployed state.
- [ ] Domain purchase remains pending before DNS/HTTPS.
Performance:
- [ ] Narrow documentation work only.
Quality:
- [ ] Load validate-docs skill; scoped validation and diff whitespace check.
- [ ] No secret, account/contact/address, token or raw host pin in repository.
- [ ] Preserve older completion evidence and registered hashes.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/documentation-standards.md
- docs/standards/development-standards.md
- docs/standards/glossary.md
Tier 1:
- .claude/agents/docops.md
- docs/policies/security-policy.md
Tier 2:
- docs/index.md
- deploy/single-vps/README.md
- deliverables/user/REQ-20261010-ATS-007.md
- deliverables/agent/WI-20261010-ATS-016-evidence-pack.md
- deliverables/agent/WI-20261010-ATS-017-evidence-pack.md
- deploy/single-vps/runbooks/first-host-setup.md
- .agents/skills/create-wi-evidence-pack/SKILL.md
- .agents/skills/validate-docs/SKILL.md

[MA SNAPSHOT, PRELIMINARY]
One existing server purchased, then user changed account email and exactly approved no-data in-place Ubuntu24.04 rebuild at displayed0KRW. MA executed19:42KST once; provider operating afterward, same firewallON. User reports new console login; MA confirmed root prompt. Independent post-rebuild Ed25519 fingerprint matches native network observation; public key hex transcription rehashed PASS. Windows agent still Disabled/Stopped, temporary activation approved not yet executed. No native SSH auth or packages yet. Likely initial password entered into username field before login, MA warned user-only rotation needed, never document raw value. Keep all endpoint/host/account details private. Initial server53,020KRW/month inclVAT, monthly70,000 budget not cap, no new purchases/domain/storage.

[OUTPUT CONTRACT]
User-facing: deliverables/user/WI-20261010-ATS-018-summary.md
Agent-facing: deliverables/agent/WI-20261010-ATS-018-evidence-pack.md
Use create-wi-evidence-pack and await MA final operational snapshot.

[TRACEABILITY REQUIREMENTS]
Date/environment/evidence owner, target/local limits, exact edited paths and validation. Preserve prior records. No claim of production readiness or completed REQ from docs-only work.


---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: MA
category: handoff
status: confirmed
dependencies:
  - path: ../user/REQ-20261011-ATS-003.md
    reason: Approved native preparation and bounded operation scope
  - path: WI-20261011-ATS-020-evidence-pack.md
    reason: Exact new JAR identity
---

# Handoff: WI-20261011-ATS-022

[WI HEADER]
WI ID: WI-20261011-ATS-022
REQ: REQ-20261011-ATS-003
Agent: se
Depends On: REQ003 approval, WI016-021 completed local preparation
Blocks: WI-20261011-ATS-023

[WI SUMMARY]
Why: Turn the approved candidate into a minimal safe target procedure, without inventing runtime proof.
Scope: Own only new build/WI022-first-start/ helpers/tests and WI022 evidence/summary. No source or existing candidate changes. MA owns SSH and native operation; you perform no remote operation.
DoD: First deliver a small read-only target inspection helper; then identify concrete feasible next steps before writing a large operator. Exact native inputs will be supplied by MA. Minimal tested staging/prestart/start-stop helper only if the existing approved gate is achievable.
Forbidden: No private data, SSH, service operation, socket/connect calls in tests, new dependencies, artifact rebuild, deleting/overwriting old files, commit/push, weakening gates, new namespace/firewall architecture or endpoint guessing.
Constraints: Stop and report missing external controlled IPv4/IPv6 positive controls/receiver observations, relay audit limitations or any target prerequisite. Do not hide architectural gap in an enormous checker. Mock tests cannot establish actual network denial.
MA will concurrently prepare SSH and inspect existing sanitized baseline, while you prepare a safe native inspection recipe.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Inspect current runbook, exact JAR manifest, older schema preflight/operator for bounded reuse.
- [ ] Produce one secret-safe read-only host inspection helper (no private file body/hash) with bounded subprocess outputs/return-code checks and fixed error reporting. Include unit/slice controls, listener/route/IP-family availability, relays/Unix sockets metadata, app/mysql state, TLS pins, resources/storage metadata, required tools and recovery presence.
- [ ] Before any operator, report which existing candidate network proofs are possible under actual target and exactly which endpoint/approval information is missing.
- [ ] No executable can start Java/DB or stage files merely by import/test/inspection.
- [ ] If runtime operator is feasible, exact hashes, trusted root paths and no overwrite; one attempt; same-unit mandatory proof; original guards; resource/runtime deadline; finally stop/remask even on failure; sanitized receipts only.
Performance:
- [ ] Finite command/time/output bounds; no whole-repo historical audit.
Quality:
- [ ] Inert unit tests for inspection parsing/error/fail-closed boundary, evidence-pack and explicit native NOT_RUN.
- [ ] Report scope blocker before changing architecture; PG must review exact helper before MA runs new executable.

[INPUT POINTERS]
Tier0 (fully injected by MA):
- docs/standards/core-principles.md
- docs/standards/documentation-standards.md
- docs/standards/development-standards.md
- docs/standards/glossary.md
Tier1:
- .claude/agents/se.md
- docs/policies/security-policy.md
Tier2:
- deploy/single-vps/first-start/README.md
- deploy/single-vps/first-start/inventory.md
- deploy/single-vps/first-start/atstudio-first-start.service.candidate
- deploy/single-vps/first-start/first-start-public.env.example
- deploy/single-vps/first-start/test_candidate.py
- deliverables/agent/WI-20261011-ATS-020-artifact.json
- deliverables/agent/WI-20261011-ATS-020-evidence-pack.md
- deliverables/agent/WI-20261011-ATS-018-evidence-pack.md (superseding repair PASS)
- deliverables/agent/WI-20261011-ATS-015-evidence-pack.md
- build/WI014-final-xbind-preflight-80d4a7.py (historical read-only predicates, boot may differ)
- build/WI013-native-v5/ma-native-operator-v5.py (historical patterns; no execution or silent repurposing)
- deploy/single-vps/runbooks/first-host-setup.md
- deliverables/user/REQ-20261011-ATS-003.md

[OUTPUT CONTRACT]
New helpers/tests: build/WI022-first-start/
User-facing: deliverables/user/WI-20261011-ATS-022-summary.md
Agent-facing: deliverables/agent/WI-20261011-ATS-022-evidence-pack.md
Use .agents/skills/test and create-wi-evidence-pack. Report exact hashes, tests, limitations, modified paths and WI023 chain. No sub-delegation.

[TRACEABILITY]
Original JAR: build/WI020-release-v3/build/libs/ATStudio-0.0.1-SNAPSHOT.jar
SHA256 01d5623972e17d0b6e1d3e532096239cd87e9995a08771c59292c94bf3699dcb.
Existing MySQL stopped/masked/app absent is historical only. All native evidence must be fresh.

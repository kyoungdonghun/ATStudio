---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: MA
category: handoff
status: confirmed
dependencies:
  - path: ../user/REQ-20261011-ATS-003.md
    reason: Approved bounded private first-start and IPv4-only proof variant
  - path: WI-20261011-ATS-024-handoff.md
    reason: Implementation ownership and target boundaries
---

# Handoff: WI-20261011-ATS-025

[WI HEADER]
WI ID: WI-20261011-ATS-025
REQ: REQ-20261011-ATS-003
Agent: pg
Depends On: WI024 partial artifacts for preliminary review; completed exact local evidence for final gate
Blocks: MA native proof and one bounded private start; WI026 evidence integration

[WI SUMMARY]
Independently review the exact WI024 separate IPv4-only runtime variant, controlled receiver proof, secret-safe configuration handling and cleanup. No native activity by PG. Keep the review limited to credible risks for this explicitly approved single-host test; do not invent new architecture or a generic audit framework.

Preliminary review may start on completed unit/public overlay/env shape while the owner finishes the operator. This is not execution approval. Final review must pin the complete owner output, completed tests and exact current bytes before any MA native operation.

[ACCEPTANCE CRITERIA]
- [ ] Inspect all executable/configuration bytes delivered by WI024; record hashes and exact invocations eligible for MA execution.
- [ ] Inert independent negative tests cover unsafe config, false network positives, secret leakage, uncontrolled process lifetime, accidental external calls or publication, cleanup failure and hidden retry.
- [ ] Verify effective AF_INET-only same-unit probe and Java IPv4 option; exact loopback allowance, controlled TCP/UDP positive controls and negative receiver observations, AF_INET6/AF_UNIX create denial, bounded inherited-FD and local relay assumptions.
- [ ] Clearly separate on-host cgroup IP-filter evidence from unperformed external Internet endpoint proof. Absence of IPv6 route or timeout alone is not PASS.
- [ ] Existing private env remains unchanged and unprinted, no secrets on CLI or public logs; approved existing keyring retained; DB targets exact; no unknown injected settings.
- [ ] Preserve strict startup guards, scheduler/audio/bootstrap/mail/OAuth/payment OFF, SQL never/DDL validate, resource limits, exact JAR hash, one attempt maximum180s.
- [ ] No existing-file overwrite/deletion, root/current switch, package install, firewall/DNS/TLS change, schema/business DML, enable/restart, commit/push.
- [ ] App/DB/receivers stop in all outcomes; MySQL remasked, no DB/app listeners/jobs remain; failures cannot falsely report cleanup PASS.
- [ ] Distinguish local review PASS, native prerequisites, native NOT_RUN. Findings ordered by severity with precise references; no helper repairs by reviewer.

[INPUT POINTERS]
Tier0, fully injected: docs/standards/core-principles.md; docs/standards/documentation-standards.md; docs/standards/development-standards.md; docs/standards/glossary.md.
Tier1, fully injected: .claude/agents/pg.md; docs/policies/security-policy.md.
Tier2: deliverables/user/REQ-20261011-ATS-003.md; deliverables/agent/WI-20261011-ATS-024-evidence-pack.md; build/WI024-private-start/; deliverables/agent/WI-20261011-ATS-020-artifact.json; deploy/single-vps/first-start/README.md; inventory.md; original candidate.
Fresh MA native inspector observations will be supplied separately with attribution. Historical records are not authority to mutate.

[OUTPUT CONTRACT]
New only: deliverables/agent/WI-20261011-ATS-025-evidence-pack.md; deliverables/user/WI-20261011-ATS-025-summary.md; optional inert tests build/WI025-review/.
Use test and create-wi-evidence-pack skills. No source/helper repair, sub-delegation, SSH, native/socket/DB/service operation, private file read or destructive command.
If substantive blocker exists, report it promptly rather than broadening approved work.

[TRACEABILITY REQUIREMENTS]
Exact public hashes, file/line references, reproducible inert test command/counts, native NOT_RUN, bounded safety assumptions, failure/rollback requirements and next MA step.


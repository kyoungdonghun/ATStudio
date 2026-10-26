---
version: 1.0
last_updated: 2026-10-10
project: ATS
owner: MA
category: agent
status: active
dependencies:
  - path: ../user/REQ-20261010-ATS-005.md
    reason: Approved offline design work
---

# WI-20261010-ATS-008: Initial Resource Baseline

[WI HEADER]
WI ID: WI-20261010-ATS-008
REQ: REQ-20261010-ATS-005
Agent: sa
Depends On: -
Blocks: WI-20261010-ATS-010

[WI SUMMARY]
Why: Turn the completed source audit into bounded, concrete starting settings for the 8GB candidate.
Scope: Write deploy/single-vps/runbooks/resource-baseline.md and this WI's two deliverables only. No executable/template/source changes.
DoD: Current and selected candidate values, memory envelope including native FFmpeg and backup, connection/admission reasoning, rate usability, target tests and implementation map.
Constraints: No purchase, installation, network target test, DB operation, private settings, builds, commits or historical-record changes. Preserve all existing work. Reuse your WI004 audit; do not redo the broad audit.

[ACCEPTANCE CRITERIA]
- [ ] Verify only relevant current settings and nested connection/concurrency source paths; choose practical initial values, not an arbitrary large pool.
- [ ] Explain whether REQUIRES_NEW risk is bounded; do not claim increasing pool alone fixes unbounded callers. If ingress/application admission work is needed, specify exactly and mark pre-invitation implementation gate.
- [ ] Include image/attachment memory, one encoder, JVM nonheap/native, MySQL process beyond buffer pool, restic resource allowance and OS headroom. Actual host RAM must be measured; GB/GiB distinguished.
- [ ] Rate/concurrency settings must preserve protected paths, avoid claiming a cost cap or user count, and give 50MB transfer arithmetic.
- [ ] SMTP finite timeouts and web/DB exposure, timezone/worker ownership, disk/log triggers included as concise target settings or links.
- [ ] Candidate settings are selected for initial target tests only; current templates remain unchanged until a subsequent implementation WI.
- [ ] Provide named mixed-work acceptance tests and clear failure/escalation thresholds, not performance guarantees.

[INPUT POINTERS]
Tier 0: docs/standards/core-principles.md; docs/standards/development-standards.md; docs/standards/documentation-standards.md; docs/standards/glossary.md.
Tier 1: docs/policies/security-policy.md.
Tier 2: deploy/single-vps/CONFIGURATION.md; deploy/single-vps/runbooks/acceptance-capacity.md; deploy/single-vps/runbooks/backup-restore.md.
REQ/context: deliverables/user/REQ-20261010-ATS-005.md; deliverables/user/WI-20261010-ATS-004-summary.md; deliverables/agent/WI-20261010-ATS-004-evidence-pack.md; deliverables/user/WI-20261010-ATS-007-summary.md.
Files: deploy/single-vps/systemd/atstudio.service; deploy/single-vps/mysql/atstudio.cnf; deploy/single-vps/env/atstudio.env.example; deploy/single-vps/nginx/; relevant Java transaction/worker/controller paths from audit.

[OUTPUT CONTRACT]
- deploy/single-vps/runbooks/resource-baseline.md: concise English decision plan, preferably <=230 lines.
- deliverables/user/WI-20261010-ATS-008-summary.md.
- deliverables/agent/WI-20261010-ATS-008-evidence-pack.md, using create-wi-evidence-pack skill.

[TRACEABILITY REQUIREMENTS]
Evidence pointers and date/environment/owner; distinguish source inference from target NOT RUN. Rollback is ignoring this unapplied plan, not reverting others' files. Report completion so MA immediately triggers WI010 when WI009 is also done.

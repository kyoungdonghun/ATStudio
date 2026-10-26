---
version: 1.0
last_updated: 2026-10-10
project: ATS
owner: MA
category: handoff
status: active
---
# WI-20261010-ATS-005 Handoff

[WI HEADER]
WI ID: WI-20261010-ATS-005
REQ: REQ-20261010-ATS-004
Agent: pg
Depends On: -
Blocks: WI-20261010-ATS-007

[WI SUMMARY]
Why: Find overlooked security, operational and recovery prerequisites before single-VPS purchase.
Scope: Actual production config and prepared Nginx/systemd/MySQL templates, secret gates, bootstrap/fresh DB, mail/OAuth/payment external prerequisites, storage access, IP/abuse limiting, backup consistency/offsite/restore, scheduler isolation, restart/monitoring/logs/timezone. Separate existing tested controls from templates and unimplemented automation. Recent SSH registration and firewall facts use latest WI003 closeout.
DoD: Prioritized, source-backed gaps with purchase versus deployment versus public-launch classification; identify concrete causes and mitigations. No code fixes.
Constraints: No secret/key/private config bodies, DB connections, external send, browser/cloud mutations, process changes, actual backup/restore, load tests, deletion, commits. No new pricing research. Write only assigned summary/evidence with apply_patch.

[ACCEPTANCE CRITERIA]
- Recognize Gmail TEST and WAV TEST success; new host remains unverified.
- Distinguish single-host failure risk, backup files versus automated restored backups, and private/public key registration versus SSH login.
- Check SMTP egress compatibility, HTTPS/80/443 rollout, provider recovery path, untrusted file/static aliases, stream/download abuse gaps.
- No unsupported assertion of missing features or safe production.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
- docs/standards/documentation-standards.md
- docs/standards/glossary.md
Tier 1:
- docs/policies/security-policy.md
- docs/policies/quality-gates.md
Tier 2:
- docs/index.md#current-decision-entry
- deploy/single-vps/README.md
- deploy/single-vps/runbooks/
- scripts/reconstruction/README.md
Context:
- deliverables/user/REQ-20261010-ATS-004.md
- deliverables/agent/WI-20261010-ATS-003-evidence-pack.md#public-key-registration-closeout-2026-10-10
- deliverables/agent/WI-20261003-ATS-004-evidence-pack.md
- deliverables/agent/WI-20261003-ATS-005-evidence-pack.md
- src/main/resources/application.yml
- src/main/java/com/atstudio/atstudio/config/

[OUTPUT CONTRACT]
- deliverables/user/WI-20261010-ATS-005-summary.md
- deliverables/agent/WI-20261010-ATS-005-evidence-pack.md
- Use create-wi-evidence-pack; English docs and Korean final digest.

[TRACEABILITY REQUIREMENTS]
Exact file/line evidence, inspection date/owner/environment and NOT RUN boundaries required. Do not rewrite historical evidence. Blocks 007; MA triggers downstream task. No unrelated edits.

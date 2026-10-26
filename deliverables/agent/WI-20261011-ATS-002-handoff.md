---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: pg
category: handoff
status: draft
---

# WI-20261011-ATS-002: Independent Quarantine TLS Review

[WI HEADER]
WI ID: WI-20261011-ATS-002
REQ: REQ-20261010-ATS-010 (approved / in progress)
Agent: pg
Depends On: WI-20261011-ATS-001
Blocks: WI-20261011-ATS-003

[WI SUMMARY]
Why: Independently review the bounded domain/TLS candidate before any provider or server mutation.
Scope (in): Review WI001 files, independently run focused local tests, inspect activation and recovery procedures, identify actionable security or correctness defects.
Scope (out): No remote connections, browser actions, package installation, certificate requests, private files, cloud changes, application or DB operations, commits, or pushes.
DoD: Evidence-backed PASS or HOLD for the exact candidate with public source hashes and precise native gates. PASS is not native execution success or certificate-issuance consent.
Constraints: Preserve unrelated dirty files. Edit only this WI's summary and evidence pack using apply_patch. Report implementation fixes to MA; do not silently fix implementation. No descendants.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Independently test renderer validation and hook guards, including hostile inputs and failure paths.
- [ ] Review ACME-only HTTP, exact domain/Host/SNI, management-only HTTPS, access-phase ordering, no application/storage publication, and IPv4-only listeners.
- [ ] Verify renewal lineage, key permissions and pair checks, active configuration validation, reload semantics, and failure behavior.
- [ ] Review Ubuntu 24.04 / Nginx 1.24.0 / Certbot 2.9.0 compatibility and package auto-start fencing.
- [ ] Explicitly separate local tests from native nginx syntax, network reachability, certificate trust and renewal tests.
Performance:
- [ ] No broad framework or unrelated workload is introduced; tests remain bounded and offline.
Quality:
- [ ] Focused tests pass, or report exact failures without weakening guards.
- [ ] No secrets, exact management IP, host pin or private host identifiers appear in tracked evidence.
- [ ] Provide prioritized findings and exact candidate hashes; no speculative PASS.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/documentation-standards.md
- docs/standards/development-standards.md
- docs/standards/glossary.md
Tier 1:
- .claude/agents/pg.md
- docs/policies/security-policy.md
- docs/policies/access-control-policy.md
- docs/policies/quality-gates.md
Tier 2:
- .agents/skills/test/SKILL.md
- .agents/skills/create-wi-evidence-pack/SKILL.md
- docs/standards/evidence-pack-standard.md
- docs/templates/eval-report-template.md
REQ / context:
- deliverables/user/REQ-20261010-ATS-010.md
- deliverables/agent/WI-20261011-ATS-001-handoff.md
- deliverables/agent/WI-20261011-ATS-001-evidence-pack.md
Files:
- deploy/single-vps/tls-quarantine/
- scripts/deployment/tls-quarantine.py
- scripts/deployment/test_tls_quarantine.py
- deploy/single-vps/runbooks/domain-tls.md
Native observations (MA, read-only, 2026-10-11 KST):
- Approved management source, pinned atadmin SSH and sudo verified; root recovery tty login verified separately.
- Nginx/MySQL masked and inactive; app service not found/inactive; no Java/FFmpeg processes. Only SSH and loopback DNS listen.
- No host UFW or nft rules; bound provider ELCAP profile currently permits SSH from the management source only. Do not introduce global firewall changes.
- Existing signed Ubuntu sources offer certbot 2.9.0-1; simulated install adds seven packages, removes/upgrades zero. No package installed yet.
- No existing certificate/account or Certbot installation. DNS zone does not yet exist; a draft was reached but not submitted.
- Existing nginx.service uses /etc/nginx/nginx.conf. Native nginx -t on the original distro configuration passes.
- Official current ACME directory names Subscriber Agreement v1.8 (2026-07-06). Terms/contact approval is still absent. Let's Encrypt expiration email service ended; do not claim it provides monitoring.

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20261011-ATS-002-summary.md: verdict, findings, residual native gates.
Agent-facing -> deliverables/agent/WI-20261011-ATS-002-evidence-pack.md: reviewed hashes, commands/results, references, prioritized findings, rollback and WI003 follow-up.
Handoff -> this file, generated through create-wi-handoff-packet skill.

[TRACEABILITY REQUIREMENTS]
Evidence: exact files/lines and public hashes; report actual executed tests versus NOT RUN checks.
Tests: use focused unittest suite, no full app execution or external access.
Rollback: no runtime mutation by PG; inspect whether MA's proposed rollback preserves existing files and restores closure.
Next: on completion MA immediately triggers WI003 handoff; a HOLD routes implementation fixes to WI001 before any native change.

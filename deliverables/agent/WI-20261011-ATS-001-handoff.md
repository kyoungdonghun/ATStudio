---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: MA
category: handoff
status: draft
---

# WI-20261011-ATS-001: Restricted TLS Preparation

[WI HEADER]
WI ID: WI-20261011-ATS-001
REQ: REQ-20261010-ATS-010 (approved)
Agent: se
Depends On: approved REQ
Blocks: WI-20261011-ATS-002

[WI SUMMARY]
Why: Prepare minimal reviewed DNS/ACME/quarantine-HTTPS setup for the existing VPS without activating the app.
Scope: New dedicated quarantine templates, safe local rendering/validation tests, and exact gated operator runbook. Reuse existing conventions but do not install the full application proxy.
DoD: Local tests PASS, public file hashes, reproducible MA commands, boundaries/rollback and two-set deliverables.
Forbidden: No remote/network-account access, SSH/agent operations, installation, ACME registration, private files/credentials, DNS/firewall changes, browser, app/DB startup, descendants, commit/push. Do not change previous artifacts or product source. Use apply_patch.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Exact apex domain only; no arbitrary directives injected through domain/IP inputs. Reject malformed, multi-address or wide client network input.
- [ ] HTTP ACME-only isolated webroot; HTTPS safe synthetic marker only for exact approved source, all app/media paths unavailable, unknown Host/SNI rejected. Do not use early return success that bypasses access-phase ACLs.
- [ ] No symlink/read escape to source, private storage or secrets; bounded no-query/no-client-secret logging.
- [ ] Explicit bootstrap -> staging challenge test -> user terms/contact gate -> production issuance -> restricted TLS -> renewal dry-run and reload verification sequence. No automatic ToS acceptance, no contact invented, no fresh runtime secrets read.
- [ ] Guarded reload hook fails on wrong domain/lineage, untrusted permissions, missing pair or failed nginx -t. Prefer standard protected Certbot lineage directly with tested permissions rather than unsafe private-key copies.
- [ ] MA preflight checks strict-pinned SSH/admin/recovery, absent app/masked DB, public config backup without overwrites, native nginx version/features, official apt simulation/sources/no unexpected service start. Changes scoped to new quarantine paths and approved nginx service.
- [ ] Provider/host firewall changes preserve SSH exact source and egress; HTTP80 public token path only, HTTPS443 same approved /32. IPv6 must not silently expose service. No DNS AAAA added.
Performance: No load/capacity claims. Small bounded synthetic HTTP tests only.
Quality: Automated positive/negative fixture tests, syntax where available, safe documented HOLD/restore-to-masked-state procedure; do not claim native Linux proof from Windows tests.

[INPUT POINTERS]
Tier0: docs/standards/core-principles.md, documentation-standards.md, development-standards.md, glossary.md (MA injects full contents).
Tier1: .claude/agents/se.md; docs/policies/security-policy.md; access-control-policy.md; quality-gates.md.
Tier2: .agents/skills/test/SKILL.md; create-wi-evidence-pack/SKILL.md; deploy/single-vps/runbooks/first-host-setup.md; host-hardening.md; jdbc-readiness.md; production-readiness.md; deployment.md.
Context: deliverables/user/REQ-20261010-ATS-010.md; docs/index.md current entry; WI-20261010-ATS-032-evidence-pack.md.
Files: existing deploy/single-vps/nginx/*.template; restricted-access.conf.
Official facts already browsed by MA: https://letsencrypt.org/getting-started/ recommends Certbot and staging; https://letsencrypt.org/docs/challenge-types/ HTTP-01 requires80. https://www.iwinv.kr/account/domain offers free DNS. Use official sources only for further technical verification.

[OUTPUT CONTRACT]
Own only new deploy/single-vps/tls-quarantine/ files, scripts/deployment/tls-quarantine.py, scripts/deployment/test_tls_quarantine.py, deploy/single-vps/runbooks/domain-tls.md, deliverables/user/WI-20261011-ATS-001-summary.md, deliverables/agent/WI-20261011-ATS-001-evidence-pack.md. No edits outside these paths. Prefer 5-8 focused implementation files over a large framework.
User-facing: precise local result, risk, needed MA actions.
Agent-facing: evidence-pack skill format, exact hashes/test command/results, actual limits, next WI002.

[TRACEABILITY REQUIREMENTS]
Record current branch/HEAD/dirty identity without private values, source anchors, tests/rollback. Keep exact target/client IP out of tracked candidates; use reviewed placeholders and MA-local rendering. No claim of TLS/native success before MA executes it.

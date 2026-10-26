---
version: 1.0
last_updated: 2026-10-10
project: ATS
owner: MA
category: handoff
status: active
---
# WI-20261010-ATS-006 Handoff

[WI HEADER]
WI ID: WI-20261010-ATS-006
REQ: REQ-20261010-ATS-004
Agent: tr
Depends On: -
Blocks: WI-20261010-ATS-007

[WI SUMMARY]
Why: Compare a small music storefront with authoritative production architecture practices, without pretending there is a universal industry server size.
Scope: Primary official documentation for single-host app+DB feasibility/tradeoffs, backups, protected media and later CDN/object storage, CPU-heavy media conversion and queues, MySQL RAM on shared host, monitoring/security, software lifecycle compatibility. Planned stack: Nginx React static, Java17/SpringBoot4, MySQL8, FFmpeg 128kbps, 2vCPU8GB100GB. MA owns iwinv prices/terms; do not duplicate that research. Source sizing/PG code audit handled separately.
DoD: 5-8 useful primary references with URLs, check date and narrow claims; comparison matrix of now/later/not required; concise potential compatibility concern (Ubuntu24.04 packaged Java/MySQL/FFmpeg) if material, verify official source.
Constraints: Read-only research. No purchase, provider contact, account use, secrets, code changes, installs, heavy tests. Do not invent competitor private infrastructure or performance. Write only two assigned deliverables using apply_patch.

[ACCEPTANCE CRITERIA]
- No categorical claim that all production systems need managed DB, CDN, load balancer or Kubernetes.
- Explicit distinction: small production possible versus HA and guaranteed capacity.
- Source-based security/backup/monitoring requirements and later scaling triggers.
- No exact software compatibility assertions from unsupported memory.

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
- deploy/single-vps/README.md
Context:
- deliverables/user/REQ-20261010-ATS-004.md
- Authoritative public sources discovered through web search; exact URLs in evidence.

[OUTPUT CONTRACT]
- deliverables/user/WI-20261010-ATS-006-summary.md
- deliverables/agent/WI-20261010-ATS-006-evidence-pack.md
- Use create-wi-evidence-pack; English docs, Korean final digest. Cite source URLs adjacent to supported claims.

[TRACEABILITY REQUIREMENTS]
Verification date, primary sources, uncertainty and no benchmark distinction required. Blocks 007; MA triggers synthesis. No unrelated changes.

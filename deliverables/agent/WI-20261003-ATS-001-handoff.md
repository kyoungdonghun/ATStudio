---
version: 1.0
last_updated: 2026-10-03
project: ATS
owner: MA
category: handoff
status: complete
related_req: REQ-20261003-ATS-001
---
# WI-20261003-ATS-001 Handoff

[WI HEADER]
WI ID: WI-20261003-ATS-001
REQ: REQ-20261003-ATS-001
Agent: se
Depends On: -
Blocks: 003,004

[WI SUMMARY]
Why: Implement the approved pre-purchase preparation, not a production deployment.
Scope (in/out):
- OWN ONLY deploy/single-vps/nginx/, deploy/single-vps/systemd/, deploy/single-vps/env/, deploy/single-vps/mysql/, deploy/single-vps/journald/, deploy/single-vps/CONFIGURATION.md and your two deliverables.
- Create minimal reviewable single-host Linux templates: Nginx HTTP/TLS host + reusable proxy/restricted-access snippets, systemd service, external placeholder-only env example, loopback MySQL example and bounded journal configuration. No installers.
- Nginx serves built SPA, forwards /api and /uploads through Java; NEVER alias public storage root. Preserve API URI and Range/206, overwrite X-ATStudio-Client-IP with direct peer, clear spoofable forwarding headers, fixed intended host. Support deny-all-by-default restricted access; empty approval allowlist must not become public.
- Full private-path deny defense and connection/request limits for media, upload body/time limits aligned with 100MiB file / 120MB request. Avoid logging query tokens, request headers or bodies. No globally sensitive query-string access logs.
- Backend loopback binding, explicit absolute public/private roots, production profile + strict integrity, no seed test users, no local.yml import, Swagger off, existing trusted-proxy settings enabled. systemd one JVM, environment secret file, Java17/FFmpeg executable, modest starting memory, write permissions for media/temp but read-only releases.
- Billing and OAuth remain unconfigured, no dummy real-looking secrets. Distinguish disabled notification from active schedulers. MAIL providers unselected; no false Gmail missing assertion.
- Document intended Linux paths, placeholders, reload validation, ACME/HTTPS bootstrap limitations and SSH/firewall not applied. Retain normal source architecture. No Docker/CDN/new dependencies.
DoD:
- Templates and source/env mappings cross-checked; single-vps/CONFIGURATION.md documents exact filenames and deploy order with no production PASS.
- Emit machine-readable JSON contract (deploy/single-vps/env/contract.json) listing required template paths, required env names and safe literals, placeholder obligations and fixed deployment roots for WI003 offline validation; avoid secret values.
- Shared interface: product /opt/atstudio/current/{app.jar,frontend/}; data /srv/atstudio/{public,private}; config /etc/atstudio/; temporary /var/lib/atstudio/tmp. Nginx upstream 127.0.0.1:8080. You may refine names while keeping these roots; report refinements.
- CWD existing Windows; don't claim nginx -t or systemd execution without actual tools.
Constraints/Forbidden:
- Use apply_patch for edits. Preserve all other work and dirty files.
- No purchase, remote host operation, DB access/DDL/data changes, app/server restart, secret inspection, network email/payment calls, public tunnel load, package installation, commit or push.
- No src/ or frontend/ product changes under this WI.
- English operational docs and evidence; Korean user-facing brief allowed only as conversation, REQ Korean.
- Static/offline checks never prove actual Linux/service/capacity readiness.
- Existing Gmail and WAV/128kbps MP3/phone TEST evidence remains valid in its original bounds.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Deliver scoped files with precise installation/validation and HOLD gates.
Performance:
- [ ] No target capacity claim. Identify values as starting settings, not measured guarantees.
Quality:
- [ ] Bounded checks run, paths recorded, no secrets or unrelated changes.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/documentation-standards.md
- docs/standards/development-standards.md
- docs/standards/glossary.md
Tier 1:
- docs/policies/security-policy.md
- docs/policies/quality-gates.md
Tier 2:
- docs/index.md#current-decision-entry
- docs/design/runtime-storage-operations.md
- docs/SR/SR-93.md#remaining-production-gates
- .agents/skills/create-wi-evidence-pack/SKILL.md
- .agents/skills/validate-docs/SKILL.md
REQ:
- deliverables/user/REQ-20261003-ATS-001.md
Files:
- src/main/resources/application.yml
- src/main/java/com/atstudio/atstudio/config/{SecurityConfig,WebConfig,StorageIntegrityStartupGuard,JwtConfig}.java
- src/main/java/com/atstudio/atstudio/security/TrustedClientIdentityResolver.java
- src/main/java/com/atstudio/atstudio/controller/TrackController.java
- frontend/src (API URL env references, read-only)
- frontend/vite.config.ts

[OUTPUT CONTRACT]
- User-facing: deliverables/user/WI-20261003-ATS-001-summary.md
- Agent-facing: deliverables/agent/WI-20261003-ATS-001-evidence-pack.md (use create-wi-evidence-pack)
- Include changed files, reproducible checks, risks, rollback by reverting ONLY owned additions, downstream WI.
- Do not edit other WI/REQ files.
- Return concise findings and paths; report template/file interface decisions promptly.

[TRACEABILITY REQUIREMENTS]
- Injected Tier 0/security content is supplied in the agent prompt in addition to pointers.
- Evidence date/environment/owner/result and unverified boundary must be separate.

## WI004 Review Follow-up (2026-10-03)

Reopened within the same approved template scope. Resolve WI004 P2 findings:
1. Media rate/concurrency rules must cover every ID spelling accepted by Spring, not only decimal digits; include segment/matrix-parameter variants in focused fixtures where applicable.
2. Add frame-embedding protection to Nginx-served SPA responses without breaking application scripts or framing permitted child integrations.
Update CONFIGURATION, contract if needed, own evidence/summary with the correction and static regression results. No product code, runtime, broader policies or other agent files. Immediately return changed paths for WI003 regression and WI004 re-review.

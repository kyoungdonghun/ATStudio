---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: MA
category: handoff
status: approved
dependencies:
  - path: ../user/REQ-20261011-ATS-006.md
    reason: Approved restricted web test
---
# WI-20261011-ATS-035 Handoff

[WI HEADER]
WI ID: WI-20261011-ATS-035
REQ: REQ-20261011-ATS-006
Agent: se
Depends On: approved REQ006
Blocks: WI037

[WI SUMMARY]
Why: Prepare one bounded management-line-only real SPA/read API test with proven restoration.
Scope: New local candidate under build/WI035-restricted-web-check only, preserving all earlier candidates/source.
DoD: Exact reviewed native recipe, narrow tests and frozen pins; independent PG review before MA execution.
Forbidden: Remote/browser/secrets access, installing, actual service/DB/network effects, application-source edits, data fixtures, deletion, commit/push, subdelegation.
Reuse existing WI032 containment/config/23 diagnostics where appropriate without modifying old files. Need explicit completion handoff (start/session/finish or bounded operator wait) for MA browser actions, hard <=1800s app AND DB lifetime with restoration recovery design, no orphan services on lost client.
If a safe bounded design requires new permissions/architecture or unapproved effects, HOLD and explain; do not silently broaden.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Fresh WI035 identifiers/claim, unchanged JAR and OFF flags/secret handling.
- [ ] Existing management IPv4 only in nginx + unchanged provider firewall; reject spoofed headers, bad Host/SNI, raw protected files, unknown API and write methods.
- [ ] Exact allowlist only reviewed side-effect-free endpoints. Coordinate WI036 through MA.
- [ ] SPA static artifacts only from verified manifest; protect traversal/symlinks/unknown files; no env/source-map exposure.
- [ ] Use actual TLS quarantine templates/hooks as baseline, preserve/restore exact bytes and effective nginx service behavior. Never change cert contents.
- [ ] Maximum30 minute test; test ends with app/DB stopped+masked and exact previous HTTPS config; detect restoration failures without deleting evidence or retry.
- [ ] Safe public result includes admission/start/view interval/cleanup states; no raw private logs/env.
Quality:
- [ ] Target-free mock tests for method/path/IP fences, activation failures, expiry/client loss, all restoration steps.
- [ ] Freeze exact candidate files, bundle and each native recipe block for WI037.
- [ ] Notify MA on design blockers before implementing an alternative.

[INPUT POINTERS]
Tier0:
- docs/standards/core-principles.md
- docs/standards/documentation-standards.md
- docs/standards/development-standards.md
- docs/standards/glossary.md
Tier1:
- docs/policies/security-policy.md
Context:
- deliverables/user/REQ-20261011-ATS-006.md
- deliverables/agent/WI-20261011-ATS-034-evidence-pack.md
- build/WI032-private-diagnostic-start/runtime.py
- build/WI032-private-diagnostic-start/atstudio-first-start-wi032.service
- build/WI032-private-diagnostic-start/public.env
- build/WI032-private-diagnostic-start/README.md
- deploy/single-vps/tls-quarantine/
- deploy/single-vps/nginx/
- scripts/deployment/tls-quarantine.py
- deliverables/agent/WI-20261011-ATS-036-evidence-pack.md (parallel, wait for MA result)

[OUTPUT CONTRACT]
Write: build/WI035-restricted-web-check/**, deliverables/agent/WI-20261011-ATS-035-evidence-pack.md, deliverables/user/WI-20261011-ATS-035-summary.md.
Do not edit shared docs/REQ or old candidates.
Use test and create-wi-evidence-pack skills; report exact frozen recipe/pins and limitations.
[TRACEABILITY REQUIREMENTS]
All tests local mocked, no native claim. Current prior native BOUNDED_PASS only proves short private backend run. Old failure unexplained remains preserved.

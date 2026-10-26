---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: MA
category: work-summary
status: active
dependencies:
  - path: ../user/REQ-20261011-ATS-002.md
    reason: Approved local-only startup preparation
---

# WI-20261011-ATS-017 Handoff

[WI HEADER]
WI ID: WI-20261011-ATS-017
REQ: REQ-20261011-ATS-002
Agent: sa
Depends On: approved REQ
Blocks: WI018

[WI SUMMARY]
Why: Prepare an opt-in first private startup configuration and finite runbook, with external communication fenced independently of credentials.
Scope:
Own a new deploy/single-vps/first-start/ directory only and your WI two-set docs. Do not edit existing template/public contract, app source, historical scripts or manifests.
Use master flag APP_BACKGROUND_TASKS_ENABLED=false (SE WI016 implementing) AND APP_AUDIO_WORKER_ENABLED=false, password login/test seeds/acceptance off, no external Provider credentials. Avoid weakening existing strict integrity/JWT/keyring/DDL validate.
Create a separate not-installed/not-enabled candidate unit/overlay and public config shape; keep host 80/443/TLS unchanged, no Nginx app upstream. Prefer existing systemd controls: loopback-only listener, Restart=no, finite runtime, least privilege/resource limits from existing unit, external IPv4/IPv6 network denial with narrow loopback allowance. Recognize loopback relays and unsupported cgroup/BPF fail-open limitations: actual deny/loopback tests must pass BEFORE Java launch in a later approved window. No blanket host firewall changes. No secrets embedded or rendered here. CLI/overlay precedence must prevent inherited env from silently re-enabling work.
Read actual scheduled/startup and outbound source to produce precise inventory (mail, OAuth, Toss, default provider URLs, startup guards/local mkdir). APP_AUTH_PASSWORD_LOGIN_ENABLED=false is not a universal mail switch, notifications=false not a scheduler switch. Use public examples only.
Document bounded future readiness GET routes, no mutating/download/notice detail probes, manual stop/cleanup and previous MySQL masked state restoration, unchanged TLS. First startup not read-only guarantee. Later approval must include exact existing secrets/internal DB use and allowable local writes; no full start in this WI.
Provide local static/synthetic validation with no extra installs. Verify technical systemd claims with official docs where needed. Mark Linux runtime/network proof pending. Use evidence-pack skill. Do not create runnable unapproved remote operator.

[ACCEPTANCE CRITERIA]
Functional: explicit off mode covers automatic entries; existing production defaults and safety guards preserved.
Performance: no production sizing claims; no performance tuning.
Quality: focused local proof with exact bounds; no external side effects.

[INPUT POINTERS]
Tier 0 (fully injected by MA):
- docs/standards/core-principles.md
- docs/standards/documentation-standards.md
- docs/standards/development-standards.md
- docs/standards/glossary.md
Tier 1:
- docs/policies/security-policy.md
- docs/policies/quality-gates.md
Tier 2:
- docs/index.md (latest Current Decision Entry only)
- deploy/single-vps/CONFIGURATION.md
- deploy/single-vps/runbooks/production-readiness.md (dated; latest index supersedes)
- deliverables/user/REQ-20261011-ATS-002.md
- deliverables/agent/WI-20261011-ATS-015-evidence-pack.md
- deliverables/agent/WI-20261010-ATS-026-artifact.json

[CONSTRAINTS]
Local-only. No SSH, real secrets, private env reads, DB service, remote API,
full application start, package installation, purchase, commit/push or deletion.
Preserve existing dirty files and immutable prior artifacts. No sub-delegation.
Use apply_patch for manual edits. Report new blocker/scope change to MA.
Tests may use isolated synthetic configurations/mocks, not retained application
environments. No dependency upgrade. No runtime Spring guard bypass.
Document target tests as NOT RUN, not implied by local checks.

[OUTPUT CONTRACT]
User-facing: deliverables/user/WI-20261011-ATS-017-summary.md
Agent-facing: deliverables/agent/WI-20261011-ATS-017-evidence-pack.md
Use create-wi-evidence-pack skill. Include injected context, commands/results,
changed paths, rollback (local proposed reversal only), limitations and next WI.

## Approved Validation Repair Addendum

MA reuses this skill-created WI packet for the same candidate-validation scope.
User approved "검사 보완·재검토 후 계속 승인" after PG018-01/02 findings.
Prior completion and original12-test results remain historical; append a repair
addendum and updated result to own evidence/summary, do not erase prior hashes.
Ownership for this repair: test_candidate.py plus own two deliverables only.
Do not change unit, env, README/inventory, Java or existing deployment files.
Preserve ordered repeated directives (two exact EnvironmentFiles); reject
unapproved duplicates/command hooks. Require exact unprefixed Java arguments,
numeric SMTP host/port and fixed public/private/temp paths. Use a narrowly
bounded subset parser, not a new full systemd library or remote operator.
Run existing tests and all27 PG mutations from build/WI018-review/test_redteam.py
unchanged, add focused regressions. Report exact new test hash to MA/PG.
Next: WI018 independent retest, then WI020 if also WI019 PASS. No new remote gate.


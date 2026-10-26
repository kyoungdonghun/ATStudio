---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: MA
category: work-summary
status: active
dependencies:
  - path: ../user/REQ-20261011-ATS-002.md
    reason: Approved local preparation
---

# WI-20261011-ATS-018 Handoff

[WI HEADER]
WI ID: WI-20261011-ATS-018
REQ: REQ-20261011-ATS-002
Agent: pg
Depends On: WI-20261011-ATS-016, WI-20261011-ATS-017
Blocks: WI-20261011-ATS-020

[WI SUMMARY]
Why: independent evidence before new release packaging.
Scope: Independently review exact completed WI016 code and WI017 first-start candidates, read-only on application/config. You may write only own PG evidence/summary and synthetic negative tests under build/WI018-review/. No server/local whole app/real DB/private secrets/network probes. Verify all automatic job pathways suppressed without weakening strict roots/integrity/keyring validation; manual methods remain sensible. Evaluate candidate unit config precedence, env sanitization, no shell/source, off flags, no external DNS/provider/SMTP/IPv6, loopback relay limitations and real Linux fail-closed gate, finite runtime/stop cleanup. Red-team static parser with mutated off flags/restart/allowlist/paths etc; do not accept grep-only proof as runtime safety. Preserve no-run/no-install boundary. Coordinate scoped source findings to MA immediately; do NOT patch SE/SA sources. Check residual risks clearly (future target cgroup/BPF proof, internal writes, baseline recovery). Conclude scope PASS or exact blockers with file/line references. Do not demand whole unrelated deployment gates be completed for LOCAL candidate PASS; distinguish pending runtime. Use test and create-wi-evidence-pack skills.

[ACCEPTANCE CRITERIA]
Functional: match changed behavior and exact candidate; preserve defaults and mandatory guards.
Performance: not measured; no supported-user or capacity claim.
Quality: recorded independent commands/results, clear coverage limits, immutable old artifacts.

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
Use evidence-pack skill; deliverables/agent/WI-20261011-ATS-018-evidence-pack.md and
 deliverables/user/WI-20261011-ATS-018-summary.md. Include exact tested hashes, commands, test
counts, findings/risks, no runtime claim and local rollback guidance.

## Approved Retest Addendum

User approved the local candidate-validator repair after PG018-01/02 HOLD.
Wait for corrected WI017 validator, then independently rerun unchanged PG
27-case harness plus the owner suite; verify exact unit/env/Java remain unchanged.
Append dated superseding result/hashes to own existing evidence and summary;
retain original HOLD and19/27 historical result. No source patch or wider gate.
Successful scoped retest plus WI01987/87 unblocks WI020 packaging.


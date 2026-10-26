---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: MA
category: handoff
status: active
dependencies:
  - path: ../user/REQ-20261011-ATS-001.md
    reason: Fresh native window approval
  - path: WI-20261011-ATS-013-evidence-pack.md
    reason: Exact new operator and offline tests
  - path: WI-20261011-ATS-012-evidence-pack.md
    reason: Unchanged candidate review binding
---

# WI-20261011-ATS-014: Independent v5 Operator Review

[WI HEADER]
WI ID: WI-20261011-ATS-014
REQ: REQ-20261011-ATS-001
Agent: pg
Depends On: WI-20261011-ATS-013
Blocks: MA gated native operation, WI-20261011-ATS-015

[WI SUMMARY]
Why: Bind safety review to the exact v5 lifecycle procedure, not historical v4 review.
Scope: Independently review the WI013 operator narrow delta, run offline mocked tests and check supplied MA staging/preflight procedure. Do not modify operator, v5 or historical evidence.
DoD: Exact SHA-256 verdict, independent tests, required target gates and two-set evidence. No native PASS inferred.
Constraints: Local-only; no SSH, remote, credentials, browser, service/agent change, DB, installation, app startup, deletion, commit/push or subdelegation. MA owns native/cleanup. Report material blocker immediately.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Independently verify exact v5 inputs, WI012 approval binding, /root/wi011 and boot pin.
- [ ] Inspect secret non-disclosure, historical identity provenance, loopback activation, signal/process cleanup and independent bounded stop/mask with fail-closed report.
- [ ] Review unchanged quarantine TLS, fresh capture/access checks and trusted public staging.
Performance:
- [ ] Retain child/parent/cleanup timeout and bounded output.
Quality:
- [ ] Run offline operator tests without native execution; explain OS/signal limitations.
- [ ] Exact file/hash verdict and follow-up WI015 needed after MA result.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/documentation-standards.md
- docs/standards/development-standards.md
- docs/standards/glossary.md
Tier 1:
- docs/policies/security-policy.md
- docs/policies/quality-gates.md
- docs/policies/access-control-policy.md
Tier 2:
- deploy/single-vps/runbooks/release-readiness.md
- .agents/skills/test/SKILL.md
- .agents/skills/create-wi-evidence-pack/SKILL.md
Context:
- deliverables/user/REQ-20261011-ATS-001.md
- deliverables/agent/WI-20261011-ATS-010-evidence-pack.md
- deliverables/agent/WI-20261011-ATS-012-evidence-pack.md
- deliverables/agent/WI-20261011-ATS-013-evidence-pack.md
- build/WI013-native-v5/ma-native-operator-v5.py
- build/WI013-native-v5/test_native_operator.py

[OUTPUT CONTRACT]
User-facing: deliverables/user/WI-20261011-ATS-014-summary.md
Agent-facing: deliverables/agent/WI-20261011-ATS-014-evidence-pack.md
Optional isolated synthetic review outputs: fresh build/WI014-* paths only.
Handoff: This packet created with create-wi-handoff-packet.

[TRACEABILITY REQUIREMENTS]
Exact source hashes, findings, independent commands/results and limits. Rollback is withholding execution, preserving files and requesting bounded correction. Return to MA for immediate native gates then WI015 integration, never start app or mark REQ complete from local review.

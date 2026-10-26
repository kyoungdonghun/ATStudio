---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: MA
category: handoff
status: approved
dependencies:
  - path: ../user/REQ-20261011-ATS-004.md
    reason: Explicit local diagnostics authorization
---

# WI-20261011-ATS-029 Handoff

[WI HEADER]
WI ID: WI-20261011-ATS-029
REQ: REQ-20261011-ATS-004
Agent: se
Depends On: WI-20261011-ATS-028
Blocks: WI-20261011-ATS-030

[WI SUMMARY]
Why: Original native cleanup returned only FAILED, losing the failing step.
Scope: Separate local diagnostic candidate and focused mock regression tests.
DoD: Named allowlisted outcomes for each cleanup action/postcondition/final baseline comparison, safe command exit/timeout categories, best-effort continuation and sticky HOLD.
Constraints: No SSH, service operations, network, secrets, native startup, upload, install, purchases, commit/push. Preserve every frozen WI024/WI026 and JAR byte. No inferred retrospective root cause. No safety-check weakening or automatic retries.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] App stop/mask, MySQL stop/mask, receiver close, state/mask/listener/process/job checks and baseline comparisons are distinguishable.
- [ ] Safe bounded output excludes command text, stdout/stderr, exception messages, secret values and raw environment/logs.
- [ ] Failure at any step retains HOLD and does not skip remaining best-effort steps.
- [ ] Unknown failures, simultaneous failures, timeouts and accepted masked/failed app state are tested.
Performance:
- [ ] Existing bounded execution deadlines and no-retry semantics retained.
Quality:
- [ ] Focused tests plus relevant existing regression suites pass.
- [ ] Original frozen candidate identity preserved and new candidate identity recorded.
- [ ] Candidate is explicitly local-only; fresh native attempt needs new approval and current exact-byte review.

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
- .agents/skills/test/SKILL.md
- .agents/skills/create-wi-evidence-pack/SKILL.md
Context:
- deliverables/user/REQ-20261011-ATS-004.md
- deliverables/agent/WI-20261011-ATS-028-evidence-pack.md
- deliverables/agent/WI-20261011-ATS-028-native-result.json
Files:
- build/WI026-private-start/runtime.py
- build/WI026-private-start/test_runtime.py
- build/WI026-private-start/test_runtime_preliminary.py
- build/WI026-private-start/test_http_lifetime.py
- build/WI026-private-start/test_log_parent.py

[OUTPUT CONTRACT]
Write scope: build/WI029-cleanup-diagnostics/ and WI029 two-set deliverables only.
User-facing: deliverables/user/WI-20261011-ATS-029-summary.md
Agent-facing: deliverables/agent/WI-20261011-ATS-029-evidence-pack.md
Do not modify docs/index or REQ; DocOps owns integration later.
Prefer a minimal separate candidate, no runnable server recipe or fabricated approval.

[TRACEABILITY REQUIREMENTS]
Report paths/lines, reproducible local tests, exact candidate and preservation hashes, limitations and rollback by withholding candidate. Follow-up WI030 must start immediately after completion.

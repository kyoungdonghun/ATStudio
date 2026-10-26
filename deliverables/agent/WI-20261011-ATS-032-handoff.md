---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: MA
category: handoff
status: approved
dependencies:
  - path: ../user/REQ-20261011-ATS-005.md
    reason: Fresh single native attempt approval
---

# WI-20261011-ATS-032 Handoff

[WI HEADER]
WI ID: WI-20261011-ATS-032
REQ: REQ-20261011-ATS-005
Agent: se
Depends On: WI-20261011-ATS-030
Blocks: WI-20261011-ATS-033

[WI SUMMARY]
Why: Convert reviewed local diagnostic candidate into a fresh one-attempt native operator.
Scope: New build/WI032-private-diagnostic-start/ from exact WI029 diagnostics and WI026 unit/public files/three-block recipe.
DoD: Narrow namespace/approval retarget, remove LOCAL_ONLY only in new reviewed candidate, preserve all diagnostics and containment, exact pins and inert regression tests.
Constraints: No actual SSH/native/service/DB/secret/network operations by worker. No old bundle modifications or JAR changes. No architecture redesign, packages, deletion, retry, public exposure. MA owns actual execution after PG033.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Fresh WI032 tools/upload/run/log/release/unit/approval/claim, REQ005 and PG033 checks throughout.
- [ ] Same JAR pin, existing-secret internal use and 23-step diagnostic logic retained.
- [ ] Existing 180s app/330s operator/per-command bounds, no retries, same IPv4-only on-host proof.
- [ ] Four public-file staging, exclusive fresh approval, once-only invocation recipe with SHA pins.
- [ ] Preserve old failed WI026 masked unit without deleting/resetting it.
Quality:
- [ ] Relevant local inherited and diagnostic regression tests pass using synthetic mocks only.
- [ ] Delta comparison shows no predicate weakening; exact bundle and recipe freeze.
Performance:
- [ ] No expanded application load or network scope.

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
- deliverables/user/REQ-20261011-ATS-005.md
- deliverables/agent/WI-20261011-ATS-028-evidence-pack.md
- deliverables/agent/WI-20261011-ATS-030-evidence-pack.md
Files:
- build/WI029-cleanup-diagnostics/runtime.py
- build/WI026-private-start/README.md
- build/WI026-private-start/atstudio-first-start-wi026.service
- build/WI026-private-start/public.env
- build/WI026-private-start/env-shape.json
- build/WI030-review/test_diagnostics.py

[OUTPUT CONTRACT]
Write only build/WI032-private-diagnostic-start/, deliverables/agent/WI-20261011-ATS-032-evidence-pack.md and deliverables/user/WI-20261011-ATS-032-summary.md.
Use apply_patch, keep prior evidence bytes. Notify MA when code/recipe frozen so PG can start before report finalization.

[TRACEABILITY REQUIREMENTS]
Exact hashes/delta/test commands/results, source-preservation, no native claims, fallback HOLD. New WI033 immediately follows; runtime execution belongs only to MA.

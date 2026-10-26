---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: MA
category: guide
status: active
dependencies:
  - path: ../user/REQ-20261011-ATS-006.md
    reason: Approved HTTPS-only continuation
  - path: WI-20261011-ATS-048-handoff.md
    reason: Exact implementation boundary
---

# WI-20261011-ATS-049 Handoff

> Purpose: Independently review the narrow After-order correction and original HTTPS continuation.

[WI HEADER]
WI ID: WI-20261011-ATS-049
REQ: REQ-20261011-ATS-006
Agent: pg
Depends On: WI048 frozen candidate; preparation may overlap
Blocks: MA one-time recovery continuation -> WI045 final integration

[WI SUMMARY]
Why: WI046 was consumed at ORIGINAL_SERVICES before Nginx start. MA has proven nginx.After token multiset equal/count7; other Nginx/MySQL baseline keys equal. User approved correction/review and only HTTPS continuation.
Scope: Read/review exact new WI048 candidate and recipes; focused independent mocked tests and two reports under own WI049 paths. Preserve all WI046/047 results. Review only delta plus inherited safety boundaries, not an unrelated audit.
DoD: Conditional verdict binds frozen script/helper/recipe hashes, strict After token-multiset semantics only, exact ordered Execs, no repeat detachment, no app/DB start/unmask/private-env/SQL reads. Fresh claim, one Nginx start, bounded failure stop/receipt behavior. Native proof explicitly NOT_RUN by PG.
Constraints: No native/SSH/UI/network or candidate edits, secret reads, purchases, package installs, prior-record changes, commit/push. Route blockers through MA. Use apply_patch for own tests/reports. Avoid broad new frameworks.

[ACCEPTANCE CRITERIA]
- [ ] Exact-match/reordered After accepted; different/missing/duplicate tokens and Exec order differences rejected.
- [ ] Verify old consumed recovery receipt and BOTH preserved copies/retained originals; no old state mutation or repeat moves.
- [ ] Current original service/TLS checks precede sole start; postcheck original process/listeners and app/DB masks/PID0.
- [ ] Partial/collision/ambiguous result/HOLD stay fail-closed, including receipt errors; no raw secrets/config output.
- [ ] Tests and pins verified locally; final CONDITIONAL_ALLOW or HOLD, required MA gates, preserved historical limitations.

[INPUT POINTERS]
Tier0 retained: docs/standards/core-principles.md; documentation-standards.md; development-standards.md; glossary.md.
Tier1 retained: docs/policies/security-policy.md.
Tier2: REQ006 and exact new approval in WI048 handoff; WI046/047 EP; new WI048 source/recipe/freeze; existing exact helper tests. Skills: test; create-wi-evidence-pack.

[OUTPUT CONTRACT]
Only build/WI049-continuation-review/**, deliverables/agent/WI-20261011-ATS-049-evidence-pack.md and deliverables/user/WI-20261011-ATS-049-summary.md. Preliminary blockers may be routed promptly; final verdict only on frozen bytes. MA alone runs native operations after gates; DocOps WI045 integrates actual outcomes.

[TRACEABILITY REQUIREMENTS]
No final success inference from local tests. Prior WI046 remains HOLD, original web trial remains HOLD/POST_STATE_CHANGED cause NOT_DETERMINED. Current proven After-only mismatch is a different diagnostic. New user approval is scoped HTTPS only and no extra cost/public access.

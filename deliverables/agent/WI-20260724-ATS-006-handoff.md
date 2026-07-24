[WI HEADER]
WI ID: WI-20260724-ATS-006
REQ: REQ-20260724-ATS-001
Agent: docops
Depends On: WI-20260724-ATS-001, WI-20260724-ATS-002, WI-20260724-ATS-003
Blocks: WI-20260724-ATS-007

[WI SUMMARY]
Why: Close documentation and portability semantics after the residual fixes.
Scope (in/out): Update only current-state documents directly affected by the three changes, then validate all docs and scan for stale machine/runtime/payment-alias claims.
DoD: Current docs match code; archived history remains clearly historical; validation and diff checks pass.
Constraints/Forbidden: Do not rewrite historical audit verdicts or change product policy.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Current acceptance and PDF instructions match implementation.
- [ ] Active current-state docs contain no obsolete alias or personal path.
Quality:
- [ ] `validate_docs.py` passes.
- [ ] Semantic residual scans and `git diff --check` pass.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/documentation-standards.md
- docs/standards/glossary.md
Tier 1:
- docs/policies/archive-policy.md
REQ/Context:
- deliverables/user/REQ-20260724-ATS-001.md
- docs/index.md
- docs/SR/SR-93.md
- docs/payment/acceptance-test-checklist.md

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260724-ATS-006-summary.md
Agent-facing -> deliverables/agent/WI-20260724-ATS-006-evidence-pack.md

[TRACEABILITY REQUIREMENTS]
Record changed docs, validation counts, claim scans, and rollback.

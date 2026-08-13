[WI HEADER]
WI ID: WI-20260809-ATS-048-QA-INTEG-REREVIEW
REQ: REQ-20260809-ATS-001
Agent: qa-integ
Depends On: WI-20260809-ATS-048-REMEDIATION
Blocks: WI-20260809-ATS-048 final gates/finalization

[WI SUMMARY]
Why: Verify that all four findings from the first independent review are actually closed and that remediation introduced no new P0-P2 defect.
Scope (in): Read-only diff review of the four findings, affected contracts, focused tests, and documentation consistency.
Scope (out): Any file edit except required result, broad new audit, full suite, schema/data/Git/external side effects, WI-053 latest-request work.
DoD: PASS only when F-QAI-048-001 through 004 are closed and no new P0-P2 exists; otherwise provide exact blockers.
Constraints/Forbidden: Read only. Do not touch/open/hash protected output or ignored secrets. No implementation/doc/test edits, formatter writes, reporter output, real deletion, or Git mutation.

[ACCEPTANCE CRITERIA]
- [ ] iOS omits native audio `accept`; desktop retains MP3/WAV hints; JS rejects/resets M4A; tests prove both.
- [ ] SOUND-021 exactly uses `is_active`, lists all response fields and `List<TagResponse>`; API spec consistently states 151/GET 76.
- [ ] Existing Korean UI copy is restored without losing WI-048 state hardening.
- [ ] Explicit false Tag intent and stale A/B impact response have direct tests.
- [ ] Original WI-048 backend/frontend contracts remain intact and WI-053 is still deferred.
- [ ] Focused tests may be run only without external/destructive effects; report actual counts.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
Tier 1:
- docs/policies/quality-gates.md
- docs/policies/access-control-policy.md
Tier 2:
- docs/standards/frontend-standards.md
- docs/design/usecase/sound-track.md
- docs/design/usecase/sound-tag.md
- docs/design/api-spec.md
REQ/WI:
- deliverables/user/REQ-20260809-ATS-001.md
- deliverables/agent/WI-20260809-ATS-048-handoff.md
- deliverables/agent/WI-20260809-ATS-048-qa-integ-review-result.md
- deliverables/agent/WI-20260809-ATS-048-remediation-handoff.md
- deliverables/agent/WI-20260809-ATS-048-evidence-pack.md
- deliverables/user/WI-20260809-ATS-048-summary.md
Files:
- Every changed WI-048 implementation/test/doc file, with special attention to files named in the remediation handoff.

[OUTPUT CONTRACT]
Agent-facing -> deliverables/agent/WI-20260809-ATS-048-qa-integ-rereview-result.md
- PASS/FAIL, closure table for four prior findings, any new findings, commands/results, safety statement.

[TRACEABILITY REQUIREMENTS]
- Cite exact file/line evidence for every closed or open finding.
- Separate intentional WI-053 deferral from defects.
- State no protected output, secrets, real data, external effect, or Git mutation was touched.

[WI HEADER]
WI ID: WI-20260724-ATS-024
REQ: REQ-20260724-ATS-002
Agent: qa-integ
Depends On: WI-20260724-ATS-023
Blocks: WI-20260724-ATS-015, WI-20260724-ATS-016

[WI SUMMARY]
Why: Independently prove the certification authorization correction in code and in the disposable runtime before external payment or mail tests resume.
Scope (in/out): Review the corrective diff, rerun focused/full applicable backend gates, push/clone only after MA integration, restart the owned disposable runtime on the corrected commit, and rerun the failed authorization case plus the representative WI-014 API/UI/security smoke. No Toss mutation or email.
DoD: PERSONAL and ADMIN access are forbidden before lookup, BUSINESS behavior is intact, the runtime API matrix passes, UI smoke remains healthy, and no secret/provider side effect occurs.
Constraints/Forbidden: Do not reuse old backend process after code correction. Do not touch protected DB, external Providers, mail, or unrelated processes.

[ACCEPTANCE CRITERIA]
- [ ] Corrective diff is minimal and matches policy.
- [ ] Focused and required backend gates pass.
- [ ] Corrected backend process runs the new commit.
- [ ] PERSONAL/ADMIN/BUSINESS runtime matrix passes.
- [ ] Representative WI-014 API/UI/media/payment-read smoke remains passing.
- [ ] Logs expose no secret and show no Toss/mail call.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
Tier 1:
- docs/policies/security-policy.md
- docs/policies/access-control-policy.md
- docs/policies/quality-gates.md
Tier 2:
- docs/design/api-spec.md
- docs/client/2-full-feature-checklist.md
REQ/Context:
- deliverables/user/REQ-20260724-ATS-002.md
- deliverables/agent/WI-20260724-ATS-014-evidence-pack.md
- deliverables/agent/WI-20260724-ATS-023-evidence-pack.md

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260724-ATS-024-summary.md
Agent-facing -> deliverables/agent/WI-20260724-ATS-024-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260724-ATS-024-handoff.md

[TRACEABILITY REQUIREMENTS]
Record commit/process ownership, role matrix, API/UI regression results, log scans, and runtime handoff.

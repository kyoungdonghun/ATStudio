[WI HEADER]
WI ID: WI-20260713-ATS-006
REQ: REQ-20260713-ATS-001
Agent: re
Depends On: WI-20260713-ATS-003
Blocks: WI-20260713-ATS-009, WI-20260713-ATS-013

[WI SUMMARY]
Why: Independently verify that the media P0 is closed without breaking preview or subscriber download behavior.
Scope (in/out): Review WI-003 implementation and tests, run focused integration tests, add or correct only tests/defects necessary to satisfy MEDIA-01 through MEDIA-05. No file move, DB mutation, transcoder dependency, or unrelated frontend cleanup.
DoD: Static path, DTO, preview-file Range, bounded fallback Range, malformed/suffix/multiple range handling, and subscriber download are independently verified.
Constraints/Forbidden: Preserve other WIs. Do not edit mail or billing files. Do not touch runtime logs.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Full original bytes cannot be reconstructed through public metadata, static route, no-Range stream, or repeated Range requests beyond the boundary.
- [ ] Admin metadata and subscriber download remain functional.
Quality:
- [ ] Focused backend tests and frontend type/test checks pass.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
Tier 1:
- docs/policies/security-policy.md
- docs/policies/quality-gates.md
Tier 2:
- docs/design/p0-release-blocker-remediation-design.md
REQ/Context Docs:
- deliverables/agent/WI-20260713-ATS-003-evidence-pack.md
Files:
- WI-003 owned source and tests

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260713-ATS-006-summary.md
Agent-facing -> deliverables/agent/WI-20260713-ATS-006-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260713-ATS-006-handoff.md

[TRACEABILITY REQUIREMENTS]
Independent findings, test commands/results, and any corrective diff: Required
Rollback: Revert only WI-006 corrective edits and outputs.

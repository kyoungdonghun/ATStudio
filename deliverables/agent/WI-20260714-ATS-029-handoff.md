[WI HEADER]
WI ID: WI-20260714-ATS-029
REQ: REQ-20260714-ATS-001
Agent: qa-fe
Depends On: WI-20260714-ATS-026, WI-20260714-ATS-027
Blocks: WI-20260714-ATS-030, WI-20260714-ATS-032, WI-20260714-ATS-034

[WI SUMMARY]
Why: Run the authoritative frontend unit/integration suite after cross-layer review and client-document alignment.
Scope: `npm test -- --run` using the repository script, result parsing, and focused triage.
Out: Browser acceptance repetition, snapshot churn, unrelated UI redesign, or generated metadata commits.
DoD: Frontend suite passes with exact file/test totals or a confirmed blocker is documented and narrowly corrected.
Constraints: Do not overwrite unrelated edits. Restore generated `frontend/tsconfig.tsbuildinfo` before final commit if verification changes it.

[ACCEPTANCE CRITERIA]
- [ ] All Vitest suites pass with parsed totals.
- [ ] Auth refresh/logout/social, Vite Host/proxy, and affected UI/API tests are included.
- [ ] Warnings and residual untested browser behavior are recorded.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
Tier 1:
- docs/policies/quality-gates.md
Context:
- deliverables/user/REQ-20260714-ATS-001.md
- deliverables/agent/WI-20260714-ATS-020-evidence-pack.md
- deliverables/agent/WI-20260714-ATS-025-evidence-pack.md
Files:
- frontend/package.json
- frontend/src/
- frontend/vite.config.ts

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260714-ATS-029-summary.md (Korean)
Agent-facing -> deliverables/agent/WI-20260714-ATS-029-evidence-pack.md
Handoff -> deliverables/agent/WI-20260714-ATS-029-handoff.md

[TRACEABILITY REQUIREMENTS]
Command, test-file/test totals, warnings/failures, fixes/reruns, generated-artifact handling, rollback, and residual risk are required.

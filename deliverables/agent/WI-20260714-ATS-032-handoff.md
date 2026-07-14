[WI HEADER]
WI ID: WI-20260714-ATS-032
REQ: REQ-20260714-ATS-001
Agent: qa
Depends On: WI-20260714-ATS-028, WI-20260714-ATS-029, WI-20260714-ATS-030, WI-20260714-ATS-031
Blocks: WI-20260714-ATS-034

[WI SUMMARY]
Why: Prove final backend and frontend production artifacts build after tests, types, and lint have passed.
Scope: backend `gradlew.bat build -x test` and frontend `npm run build`, run serially.
Out: Re-running the full backend suite, deployment, publishing, or committing build output.
DoD: Both builds pass and produced artifacts are identified but excluded from Git.
Constraints: No concurrent Gradle task. Backend build must skip tests because WI-028 owns the single full-suite execution.

[ACCEPTANCE CRITERIA]
- [ ] Backend artifact builds with tests excluded only because WI-028 already passed.
- [ ] Frontend production bundle builds.
- [ ] Build warnings, sizes where available, and generated artifacts are recorded.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
Tier 1:
- docs/policies/quality-gates.md
Context:
- deliverables/agent/WI-20260714-ATS-028-evidence-pack.md
- deliverables/agent/WI-20260714-ATS-029-evidence-pack.md
- deliverables/agent/WI-20260714-ATS-030-evidence-pack.md
- deliverables/agent/WI-20260714-ATS-031-evidence-pack.md

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260714-ATS-032-summary.md (Korean)
Agent-facing -> deliverables/agent/WI-20260714-ATS-032-evidence-pack.md
Handoff -> deliverables/agent/WI-20260714-ATS-032-handoff.md

[TRACEABILITY REQUIREMENTS]
Commands, durations, warnings, artifact pointers, Git exclusions, rollback, and residual risk are required.

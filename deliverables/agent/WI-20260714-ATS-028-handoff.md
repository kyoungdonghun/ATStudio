[WI HEADER]
WI ID: WI-20260714-ATS-028
REQ: REQ-20260714-ATS-001
Agent: qa
Depends On: WI-20260714-ATS-026, WI-20260714-ATS-027
Blocks: WI-20260714-ATS-030, WI-20260714-ATS-032, WI-20260714-ATS-034

[WI SUMMARY]
Why: Run the single authoritative full backend test suite after all implementation/review/document edits are stable.
Scope: `gradlew.bat test`, result parsing, failure triage, and exact test-count evidence.
Out: Concurrent Gradle tasks, live providers, database mutation, unrelated fixes, or retrying failures without diagnosis.
DoD: Full backend test result is unambiguous; any failure is traced and fixed under an approved in-scope follow-up before rerun.
Constraints: Own `build/test-results` exclusively while running. No other Gradle task may run concurrently. Run the full suite once; rerun only after a confirmed fix.

[ACCEPTANCE CRITERIA]
- [ ] Full JUnit suite completes with parsed totals and zero failures/errors, or exact blocker evidence is reported.
- [ ] No live Toss/SMTP/production DB path is invoked.
- [ ] Skips/assumptions and residual environment limitations are listed.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
Tier 1:
- docs/policies/quality-gates.md
Context:
- deliverables/user/REQ-20260714-ATS-001.md
- deliverables/agent/WI-20260714-ATS-025-evidence-pack.md
- deliverables/agent/WI-20260714-ATS-026-evidence-pack.md

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260714-ATS-028-summary.md (Korean)
Agent-facing -> deliverables/agent/WI-20260714-ATS-028-evidence-pack.md
Handoff -> deliverables/agent/WI-20260714-ATS-028-handoff.md

[TRACEABILITY REQUIREMENTS]
Exact command, timestamps, XML totals, failed/skipped tests, environment, fixes/reruns, rollback, and residual risk are required.

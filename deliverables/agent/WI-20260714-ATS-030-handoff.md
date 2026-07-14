[WI HEADER]
WI ID: WI-20260714-ATS-030
REQ: REQ-20260714-ATS-001
Agent: qa
Depends On: WI-20260714-ATS-028, WI-20260714-ATS-029
Blocks: WI-20260714-ATS-032, WI-20260714-ATS-034

[WI SUMMARY]
Why: Prove backend compilation and frontend type safety independently of runtime tests.
Scope: `compileJava`, `compileTestJava`, and frontend `npm run typecheck` in serial.
Out: Full tests/builds, formatting changes, or generated metadata staging.
DoD: All compile/typecheck commands pass and generated-file side effects are identified for cleanup.
Constraints: Run serially; do not overlap Gradle tasks. Do not stage `frontend/tsconfig.tsbuildinfo`.

[ACCEPTANCE CRITERIA]
- [ ] Backend main and test sources compile.
- [ ] Frontend TypeScript typecheck passes.
- [ ] Warnings and generated metadata changes are recorded.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
Tier 1:
- docs/policies/quality-gates.md
Context:
- deliverables/agent/WI-20260714-ATS-028-evidence-pack.md
- deliverables/agent/WI-20260714-ATS-029-evidence-pack.md

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260714-ATS-030-summary.md (Korean)
Agent-facing -> deliverables/agent/WI-20260714-ATS-030-evidence-pack.md
Handoff -> deliverables/agent/WI-20260714-ATS-030-handoff.md

[TRACEABILITY REQUIREMENTS]
Commands, exit results, compiler warnings, generated files, cleanup requirement, and residual risk are required.

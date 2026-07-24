[WI HEADER]
WI ID: WI-20260724-ATS-010
REQ: REQ-20260724-ATS-002
Agent: qa
Depends On: -
Blocks: WI-20260724-ATS-011, WI-20260724-ATS-012, WI-20260724-ATS-013

[WI SUMMARY]
Why: Prove that the pushed backend baseline is reproducible without the current worktree.
Scope (in/out): Fresh-clone the remote official branch into `C:\Users\jm991\AppData\Local\ATStudio\release-rehearsal-3147873-20260724`, verify exact remote commit, inspect toolchain, and run clean backend tests, coverage thresholds, build, and executable JAR checks. No DB mutation or external Provider call.
DoD: Clone is independent and at `3147873c42bfd7883fdaa92922c0485e5fc72621`; backend clean gates pass or a reproducible failure is recorded.
Constraints/Forbidden: Do not copy source/build/config from the current repo. Do not read ignored secret files. Do not edit runtime source or commit/push. Write only the contracted evidence in the official workspace.

[ACCEPTANCE CRITERIA]
- [ ] Remote and clone commit IDs match.
- [ ] Java/Gradle versions satisfy the project contract.
- [ ] `gradlew clean check bootJar` completes with exact test/coverage results.
- [ ] Environment-dependent skips are classified, not hidden.
- [ ] Clone status after verification is recorded.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
Tier 1:
- docs/policies/quality-gates.md
REQ/Context:
- deliverables/user/REQ-20260724-ATS-002.md
- docs/SR/SR-93.md
Files:
- build.gradle
- gradlew.bat
- settings.gradle

[OUTPUT CONTRACT]
User-facing -> `deliverables/user/WI-20260724-ATS-010-summary.md`
Agent-facing -> `deliverables/agent/WI-20260724-ATS-010-evidence-pack.md`

[TRACEABILITY REQUIREMENTS]
Record commands, commit hashes, versions, test/coverage/build results, skips, clone path, and rollback/cleanup ownership without secrets.

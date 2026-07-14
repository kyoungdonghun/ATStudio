[WI HEADER]
WI ID: WI-20260714-ATS-013
REQ: REQ-20260714-ATS-001
Agent: se
Depends On: WI-20260714-ATS-001, WI-20260714-ATS-003
Blocks: WI-20260714-ATS-019, WI-20260714-ATS-024, WI-20260714-ATS-025

[WI SUMMARY]
Why: Prevent spreadsheet formula execution when administrators open whitelist CSV exports.
Scope (in/out):
- In: Output-only cell neutralization before existing CSV quoting, focused edge-case tests, and no stored-value mutation.
- Out: UI redesign, export filtering changes, spreadsheet library introduction, and unrelated CSVs.
DoD:
- Every user-controlled whitelist CSV cell is neutralized when its first effective character can start a formula.
- Existing BOM, column order, quoting, status snapshot, and Korean text behavior remain intact.
Constraints/Forbidden:
- Do not mutate DB values or trim/normalize business data.
- Do not add a CSV/spreadsheet dependency.
- Do not edit unrelated whitelist workflow behavior.
- You are not alone in the codebase; never revert concurrent edits.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] `=`, `+`, `-`, `@`, leading space/tab variants, and leading CR/LF are neutralized.
- [ ] Embedded quote/newline, null/empty, apostrophe-prefixed, ordinary Korean, and legitimate values remain correctly quoted.
- [ ] Snapshot data remains original; neutralization occurs only at file serialization.
Quality:
- [ ] Focused service tests pass.
- [ ] `gradlew.bat compileJava` and `git diff --check` pass.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
Tier 1:
- docs/policies/security-policy.md
Context:
- deliverables/user/REQ-20260714-ATS-001.md
- docs/audit/p1-remediation-trace-matrix-20260714.md
- docs/design/p1-security-acceptance-hardening-design.md
- src/main/java/com/atstudio/atstudio/service/AdminWhitelistChannelService.java
- src/test/java/com/atstudio/atstudio/service/AdminWhitelistChannelServiceTest.java

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260714-ATS-013-summary.md (Korean)
Agent-facing -> deliverables/agent/WI-20260714-ATS-013-evidence-pack.md
Handoff -> deliverables/agent/WI-20260714-ATS-013-handoff.md
Implementation ownership -> whitelist CSV serializer and focused tests only.

[TRACEABILITY REQUIREMENTS]
Evidence pointers and exact commands: Required
Tests: focused Gradle whitelist tests
Rollback: revert owned serializer/test changes; stored records remain untouched

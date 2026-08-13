[WI HEADER]
WI ID: WI-20260809-ATS-044-QA-REMEDIATION
REQ: REQ-20260809-ATS-001
Agent: se
Depends On: WI-20260809-ATS-044-QA-FE-REVIEW
Blocks: WI-20260809-ATS-044 completion

[WI SUMMARY]
Why: Independent QA found four stale assertions in the full frontend regression suite and a missing explicit StrictMode ownership regression; core WI-044 behavior passed review.
Scope (in/out):
- In: Update only the stale `publicAuthShell.coverage.test.tsx` expectations for Album AbortSignal arguments, fixed localized recovery, and download-failure toast ownership.
- In: Add focused StrictMode regression proof that catalog request/context cleanup does not leak stale state or corrupt the latest owner.
- In: Run the formerly failing suite, focused WI-044 suites, and full frontend test suite.
- Out: Product behavior changes unless a corrected test proves a real defect; backend/schema/data/docs policy changes; external browser/provider effects.
DoD: Four stale tests match the implemented contract, StrictMode ownership is covered, focused and full frontend tests pass, and no production change is made unless required by a reproduced defect.
Constraints/Forbidden:
- Do not weaken assertions merely to make tests pass; assert AbortSignal, localized non-raw recovery, exact toast ownership, and bounded request/context effects.
- Do not modify unrelated coverage tests or production behavior without reporting a confirmed defect.
- Do not stage, commit, push, or touch/inspect protected `output` assets.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Existing Album API assertions include the request signal and preserve exact request parameters.
- [ ] Missing/error detail assertions verify localized recovery and absence of raw transport text.
- [ ] Download failure assertion verifies the error toast, not obsolete page-state text.
- [ ] StrictMode mount/cleanup proves no stale request/context overwrite or durable playback clearing.
Quality:
- [ ] Formerly failing file passes.
- [ ] WI-044 focused tests pass.
- [ ] Full `npm test` passes with zero failures.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
Tier 1:
- docs/policies/quality-gates.md
Tier 2:
- docs/standards/frontend-standards.md
- .agents/skills/react-best-practices/AGENTS.md
Context:
- deliverables/agent/WI-20260809-ATS-044-handoff.md
- deliverables/agent/WI-20260809-ATS-044-qa-fe-review-handoff.md
Files:
- frontend/src/test/coverage/publicAuthShell.coverage.test.tsx
- WI-044 focused catalog/player test files

[OUTPUT CONTRACT]
- Final response: changed files, exact stale assertions corrected, StrictMode proof, commands/results, and any remaining failure. No additional deliverable file.

[TRACEABILITY REQUIREMENTS]
Evidence: Map all four prior failure lines and the StrictMode case to passing assertions.
Tests: Record focused file, WI-044 focused group, and full frontend counts.
Rollback: Revert only QA remediation test changes unless a real product fix becomes necessary.

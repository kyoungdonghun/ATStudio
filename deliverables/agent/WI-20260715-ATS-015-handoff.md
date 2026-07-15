[WI HEADER]
WI ID: WI-20260715-ATS-015
REQ: REQ-20260714-ATS-001
Agent: qa
Depends On: WI-20260715-ATS-014
Blocks: final scope audit and completion report

[WI SUMMARY]
Why: Adjudicate WI-014's full-tree Prettier failure against the approved scoped-Prettier gate and determine whether the first Gradle executor failure is reproducible, without hiding or rewriting the historical FAIL.
Scope (in): Compare frontend changes from frozen baseline `b217234` to current HEAD; compare full-tree Prettier results at the frozen baseline and current tree; run Prettier only on frontend source files changed by this remediation; rerun the backend test suite once with `--rerun-tasks --stacktrace`; review and reference the remaining WI-014 PASS gates; issue a follow-up PASS/FAIL decision.
Scope (out): Formatting 143 baseline files, product/docs/schema edits, WI-014 evidence edits, live Toss, data/DB mutation, preview mutation, feature work, and non-payment remediation.
DoD: Preserve WI-014 FAIL as historical evidence; prove whether full-tree formatting debt predates this work; apply the approved scoped gate exactly; record whether the executor failure recurs; issue a current follow-up verdict with no suppression of residual risks.
Constraints/Forbidden: Read-only except WI-015 summary/evidence outputs. Do not run Prettier write/fix mode. Do not restore/revert product or user files. Do not modify the preview worktree. Preserve all runtime logs and WI-014 outputs. You are not alone in the repository; do not revert concurrent changes.

[ACCEPTANCE CRITERIA]
- [ ] Record `git diff --name-status b217234..HEAD -- frontend` and current tracked/cached frontend diff.
- [ ] Run the same `npm run format` check at current HEAD and frozen preview `b217234`; compare failing-file sets exactly or explain any difference.
- [ ] Derive the scoped frontend source file list changed by this approved remediation. If empty, record the scoped Prettier gate as N/A/PASS rather than changing unrelated files; if non-empty, run `npx prettier --check` only on that explicit list.
- [ ] Run `gradlew.bat test --rerun-tasks --stacktrace` once from a clean tracked tree and record exit, duration, test counts/skips, and any executor recurrence.
- [ ] Reuse only verified WI-014 PASS results for typecheck, lint, frontend tests/build, docs, whitespace, MySQL evidence, and preview smoke.
- [ ] Final Git state contains no tracked/cached change and only expected runtime logs plus WI-014/WI-015 outputs.
- [ ] Produce a follow-up PASS only if no changed frontend file violates Prettier, the backend rerun passes, and no new P0/P1 emerges; keep full-tree formatting debt as a separate non-blocking baseline issue for this REQ.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 1:
- docs/policies/quality-gates.md

REQ/Context:
- deliverables/user/REQ-20260714-ATS-001.md
- deliverables/agent/WI-20260715-ATS-014-handoff.md
- deliverables/agent/WI-20260715-ATS-014-evidence-pack.md
- deliverables/user/WI-20260715-ATS-014-summary.md
- deliverables/agent/WI-20260715-ATS-012-evidence-pack.md
- commit `b217234`
- current HEAD

Verification Skills:
- .agents/skills/prettier/SKILL.md
- .agents/skills/test/SKILL.md
- .agents/skills/create-wi-evidence-pack/SKILL.md

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260715-ATS-015-summary.md
Agent-facing -> deliverables/agent/WI-20260715-ATS-015-evidence-pack.md
Handoff -> deliverables/agent/WI-20260715-ATS-015-handoff.md

[TRACEABILITY REQUIREMENTS]
Record exact commands, exit codes, durations, normalized Prettier failing-file sets and comparison, backend test counts/skips, Git state before/after, and a clear distinction among historical WI-014 FAIL, approved scoped-gate verdict, baseline formatting debt, and production readiness.

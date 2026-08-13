# Independent QA Rereview Handoff: WI-20260809-ATS-050

[WI HEADER]

- WI ID: `WI-20260809-ATS-050-QA-INTEG-REREVIEW`
- REQ: `REQ-20260809-ATS-001` (`approved`)
- Agent: `qa-integ`
- Depends On: WI-050 remediation
- Blocks: WI-050 finalization, full gates, and commit

[WI SUMMARY]

## Why

Independently determine whether `F-QA-INTEG-050-001`, `-002`, and `-003` are closed without introducing navigation deadlocks, false success/failure, stale operation ownership, or regressions outside Notice scope.

## Scope

- Review the remediation diff and original QA result against the approved WI-050 contract.
- Run focused tests and inspect exact counterexamples; do not rely on the remediation report.
- Review the shared Modal optional contract for backwards compatibility.
- Write only `deliverables/agent/WI-20260809-ATS-050-qa-integ-rereview-result.md`.

## Mandatory Counterexamples

- Click sidebar/link/history while mutation pending, then authoritative success, authoritative 4xx failure, ambiguous network failure, and component unmount. Verify request abort and mutation call counts exactly.
- Ensure programmatic success navigation is not blocked and a previously blocked transition is reset rather than unexpectedly resumed.
- Verify `beforeunload` is installed only while operation ownership exists and released for all terminal outcomes.
- After ambiguous create/update/delete: identical mutation remains impossible until an observation-only path is taken; observation performs only GET and cannot claim success/failure; successful observation unlocks future deliberate edit where appropriate.
- Direct route/unmount cannot leave an unbounded global operation owner or update unmounted UI.
- Busy Modal: header close disabled and out of actionable focus, Escape/backdrop suppressed, `aria-busy` exposed, failure recovery restores close behavior. Non-busy existing Modal behavior remains unchanged.
- Public 5xx, valid-to-invalid, download completion after unmount, and destination GET failure are asserted independently with exact calls.
- No schema/dependency/new endpoint/attachment-limit expansion; WI-039 storage/security remains unchanged.

## Constraints

- No edits except the rereview result file.
- No actual browser mutation, live DB/storage/file/download/external effect, secret/protected-output access, staging, commit, push, or branch action.
- Findings first, P0-P3, with exact pointers and reproducible schedule. PASS only when all P0-P2 are closed.

[INPUT POINTERS]

- `deliverables/agent/WI-20260809-ATS-050-handoff.md`
- `deliverables/agent/WI-20260809-ATS-050-qa-integ-review-result.md`
- `deliverables/agent/WI-20260809-ATS-050-remediation-handoff.md`
- `docs/standards/core-principles.md`
- `docs/standards/development-standards.md`
- `docs/policies/quality-gates.md`
- Current WI-050 diff excluding `output/**`.

[OUTPUT CONTRACT]

- `deliverables/agent/WI-20260809-ATS-050-qa-integ-rereview-result.md`
- Closure table for each original finding, any new findings, exact independent commands/results, verdict PASS/FAIL, and residual deferrals.

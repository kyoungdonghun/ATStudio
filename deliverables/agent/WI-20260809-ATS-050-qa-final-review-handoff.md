# Independent Final QA Handoff: WI-20260809-ATS-050

[WI HEADER]

- WI ID: `WI-20260809-ATS-050-QA-FINAL`
- REQ: `REQ-20260809-ATS-001` (`approved`)
- Agent: `qa-integ`
- Depends On: WI-050 remediation R2
- Blocks: WI-050 finalization, full gates, commit, and push

[WI SUMMARY]

## Why

Perform the final independent attack review after two remediation rounds. PASS only if every P0-P2 finding from both prior reviews is closed and no new P0-P2 regression exists.

## Scope

- Recheck `F-QA-INTEG-050-001` through `-005`, all mandatory WI-050 contracts, and current tests/docs.
- Inspect session observation-fence behavior, storage failure fallback, pending/unload ownership, Modal focus, ADMIN/public reads, attachment downloads, and regression boundaries.
- Run focused and adjacent verification as needed; write only the final result file.

## Mandatory Attack Cases

- Ambiguous create -> unrelated route -> remount -> submit: one POST, zero unlock; successful Notice-list GET then deliberate create is permitted; failed/cancelled/stale list GET does not clear.
- New browser session/tab semantics are session-scoped; no title/content/file/user data stored. `sessionStorage` get/set/remove exceptions do not crash or silently weaken the conservative fence during the current tab lifetime.
- Public Notice list still loads normally for users with no fence; clearing an absent fence is harmless and stale list responses cannot clear a newer fence.
- `beforeunload` listener add/remove exactly follows pending ownership across idle, success, 4xx, ambiguous, unmount; same-tick navigation remains blocked by the ref predicate.
- Success navigation is not deadlocked and an attempted blocked transition is reset, not unexpectedly resumed.
- Busy Modal with zero enabled descendants keeps Tab and Shift+Tab on the dialog; close/Escape/backdrop remain suppressed; failure restores ordinary behavior; non-busy Modal callers are unchanged.
- Create/update/delete destination GET failure/retry executes only GET after authoritative mutation success; exact POST/PUT/DELETE counts remain one.
- ADMIN projection is ADMIN-only and non-counting; public read remains counting; WI-039 PRIVATE/safe download behavior remains intact.
- No schema, dependency, extra endpoint beyond approved ADMIN projection, attachment-policy expansion, real effect, or undocumented current-state claim.

## Constraints

- No edits except `deliverables/agent/WI-20260809-ATS-050-qa-final-review-result.md`.
- No actual browser mutation, live DB/storage/file/download/external effect, secret/protected-output access, Git mutation, branch action, or deploy.
- Findings first with exact pointers and reproducible schedules. PASS requires zero open/new P0-P2 findings.

[INPUT POINTERS]

- `deliverables/agent/WI-20260809-ATS-050-handoff.md`
- `deliverables/agent/WI-20260809-ATS-050-qa-integ-review-result.md`
- `deliverables/agent/WI-20260809-ATS-050-qa-integ-rereview-result.md`
- `deliverables/agent/WI-20260809-ATS-050-remediation-r2-handoff.md`
- `docs/standards/core-principles.md`
- `docs/standards/development-standards.md`
- `docs/policies/quality-gates.md`
- Current WI-050 diff excluding `output/**`.

[OUTPUT CONTRACT]

- `deliverables/agent/WI-20260809-ATS-050-qa-final-review-result.md`
- Closure table for all prior findings, new findings, exact commands/results, PASS/FAIL, residual P3/deferrals.

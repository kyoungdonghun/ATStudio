# Independent QA Re-review Handoff: WI-20260809-ATS-049

[WI HEADER]

- WI ID: `WI-20260809-ATS-049-QA-INTEG-R2`
- REQ: `REQ-20260809-ATS-001` (`approved`)
- Agent: `qa-integ`
- Depends On: `WI-20260809-ATS-049-REM`
- Blocks: WI-049 final full gates and commit

[WI SUMMARY]

## Why

Independently verify that `QA-049-001` through `QA-049-004` are actually closed after remediation and that the fixes introduced no new P0-P2 defect.

## Scope

- Review only current WI-049 changed production/tests/docs plus original QA result and remediation handoff.
- Reproduce/inspect all four exact counterexamples.
- Verify new tests are meaningful, fail against the pre-remediation logic in principle, and assert API call counts/state ownership rather than copy alone.
- Run the two remediated suites and, if practical, the eight-file focused/adjacent command.
- Write only `deliverables/agent/WI-20260809-ATS-049-qa-integ-rereview-result.md`.

## Required Closure Checks

- Rejected reorder provenance remains unconfirmed through repeated failed reads; successful retry performs read only.
- Extensionless valid JPEG/PNG is accepted client-side while incompatible supplied MIME and corrupt decode are rejected; no filename authority remains.
- Home/End/focus-out and pointer option selection coexist with valid combobox ownership.
- Existing and locally committed members are absent/disabled safely and repeated add mutation count remains one after refresh failure.
- WI-038 zero-based reorder remains intact.
- Current docs/evidence/summary describe remediation without rewriting the historical FAIL result.

## Constraints

- No product/test/doc edits except the single rereview result file.
- No live/external/data/secret/protected-output/Git actions.
- Findings first, P0-P3 with exact counterexample. Verdict PASS only when all four are closed and no new P0-P2 exists.

[INPUT POINTERS]

- `deliverables/agent/WI-20260809-ATS-049-qa-integ-review-result.md`
- `deliverables/agent/WI-20260809-ATS-049-remediation-handoff.md`
- `deliverables/agent/WI-20260809-ATS-049-evidence-pack.md`
- `deliverables/user/WI-20260809-ATS-049-summary.md`
- Current WI-049 diff excluding `output/**`
- `docs/standards/core-principles.md`
- `docs/standards/development-standards.md`
- `docs/policies/quality-gates.md`

[OUTPUT CONTRACT]

- `deliverables/agent/WI-20260809-ATS-049-qa-integ-rereview-result.md`
- Per-finding closure matrix, verdict, independent tests, new findings, residual risks.

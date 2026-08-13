# Independent QA Handoff: WI-20260809-ATS-050

[WI HEADER]

- WI ID: `WI-20260809-ATS-050-QA-INTEG`
- REQ: `REQ-20260809-ATS-001` (`approved`)
- Agent: `qa-integ`
- Depends On: WI-050 implementation
- Blocks: WI-050 remediation, final documentation, full gates, and commit

[WI SUMMARY]

## Why

Independently review the complete uncommitted WI-050 Notice diff. Treat the implementer's passing tests as claims to verify, not authority. Find stale ownership, duplicate mutation, authorization, view-count, download recovery, accessibility, contract, or documentation defects before finalization.

## Scope

- Review every current WI-050 source, style, test, and document change plus the primary handoff.
- Trace public Notice detail state, ADMIN edit state, request shape, security rule, repository/service behavior, and response projection.
- Verify WI-039 PRIVATE attachment storage and safe download response behavior remain intact.
- Inspect focused tests for meaningful counterexamples and false-positive assertions; run narrow frontend/backend tests where practical.
- Write only `deliverables/agent/WI-20260809-ATS-050-qa-integ-review-result.md`; do not edit production, tests, docs, summaries, handoffs, or Git state.

## Mandatory Attack Cases

- Public detail load: localized 404 versus network/5xx, retry behavior, route A to B with A completing late, route becoming invalid, and unmount before completion.
- ADMIN edit load: missing, non-numeric, non-positive, and unsafe integer IDs cause zero Notice, attachment, and mutation calls and provide safe navigation.
- View counts: ADMIN detail read performs zero increment/write operations; public detail retains the existing single-increment contract.
- Authorization: the new ADMIN read path is explicitly ADMIN-only and cannot weaken public attachment/download authorization.
- Create/edit coordination: save, Notice delete, file add, file remove, close/navigation, and duplicate submit cannot conflict while an owned operation is pending.
- Commit ambiguity: success followed by refresh failure exposes recovery and retry repeats only the read, never save/delete/file mutation.
- Attachment download: same-file duplicate fence, independent other-file availability, immutable target ownership, local error association, retry, stale completion after route change/unmount, safe filename/error normalization, and no unbounded state.
- Accessibility/localization: labels remain associated, pending/error text is localized, disabled state is programmatically clear, and recovery actions retain keyboard access.
- Policy boundary: no new type/count/size attachment limit or content policy is invented; current canonical content maximum is consistently enforced where documented.
- Regression: WI-039 PRIVATE storage and encoded safe response headers remain green; no real download, durable file write, or DB mutation occurs during verification.
- Docs: describe only current implemented API/UI behavior, distinguish public and ADMIN read semantics, and do not claim browser/live acceptance.

## Constraints

- No production/test/doc modifications except the one QA result file.
- No real ADMIN browser mutation, live DB/storage/file operation, attachment download, external effect, secret inspection, protected output access, commit/push, or branch action.
- Do not treat WI-055/WI-059/WI-066/WI-070 deferred work as WI-050 defects unless this patch newly regresses it.
- Report findings first, ordered P0-P3, with exact file/line and a reproducible counterexample. If no P0-P2 exists, state that explicitly.

[INPUT POINTERS]

## Tier 0

- `docs/standards/core-principles.md`
- `docs/standards/development-standards.md`

## Tier 1

- `docs/policies/security-policy.md`
- `docs/policies/quality-gates.md`
- `docs/policies/access-control-policy.md`

## Tier 2

- `docs/standards/frontend-standards.md`
- `docs/design/api-spec.md`
- `docs/design/usecase/user-notice.md`
- `docs/ui/screen-flow.md`
- `docs/ui/atstudio-front-list.md`
- `docs/ui/modal-list.md`
- `.agents/skills/react-best-practices/AGENTS.md`

## REQ / Audit / Dependency

- `deliverables/user/REQ-20260809-ATS-001.md`
- `deliverables/agent/WI-20260809-ATS-050-handoff.md`
- `deliverables/agent/WI-20260809-ATS-031-consolidated-findings.md`
- `deliverables/agent/WI-20260809-ATS-021-findings.md`
- `deliverables/agent/WI-20260809-ATS-025-findings.md`
- `deliverables/agent/WI-20260809-ATS-039-evidence-pack.md`
- Current `git diff` limited to WI-050 paths, excluding all `output/**`.

[OUTPUT CONTRACT]

- `deliverables/agent/WI-20260809-ATS-050-qa-integ-review-result.md`
- Verdict: PASS or FAIL.
- Findings: ID, severity, contract/counterexample, exact pointers, remediation expectation.
- Tests independently run and exact results.
- Residual risks and intentional deferrals.

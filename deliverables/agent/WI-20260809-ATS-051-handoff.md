# WI Handoff Packet: WI-20260809-ATS-051

[WI HEADER]

- WI ID: `WI-20260809-ATS-051`
- REQ: `REQ-20260809-ATS-001` (`approved`)
- Agent: `se`
- Depends On: `WI-20260809-ATS-040`
- Blocks: `WI-20260809-ATS-058`, `WI-20260809-ATS-063`

[WI SUMMARY]

## Why

Align the whitelist and company-certification user interfaces with the existing backend state, validation, and recovery contracts. Prevent actions that the backend will reject, disclose edits that requeue externally processed channels, and keep certification submission closed until the existing-application lookup has a definitive result.

## Scope

### In

- `CR-031-069`: render `REMOVAL_REQUESTED` as an already-requested, idempotent state instead of offering a misleading delete action or reporting that the row was deleted.
- `CR-031-070`: expose primary-channel action only for statuses accepted by the backend entity predicate.
- `CR-031-071`, `CR-031-072`: mirror the backend HTTPS YouTube-host URL contract and the 255-character channel URL bound in the client form, with focused boundary tests.
- `CR-031-073`: before editing `EXPORTED`, `REGISTERED`, or `REVISION_REQUESTED`, disclose that saving returns the channel to `PENDING`; require an explicit confirmation for the externally processed statuses without changing the workflow policy.
- `CR-031-077`: enforce and explain the existing 500-character ADMIN Whitelist review-note contract in the admin UI and request boundary.
- `CR-031-078`: gate company-certification application on a definitive 404/no-existing or existing `REJECTED` result; generic lookup failure is blocking and retryable.
- `CR-031-079`: add scoped retries for user certification status and admin certification list/detail load failures while preserving current route, filters, pagination, and selected ID.
- Focused tests and current-state Whitelist/Company Certification documentation synchronization.

### Out

- `CR-031-074`: changing the `REVISION_REQUESTED` workflow or deciding whether edits alone should replace the explicit re-request action.
- `CR-031-080`: broad localization/copy cleanup reserved for WI-058, except wording directly required to make the scoped states truthful.
- WI-040 CSV/export behavior, status-transition policy, plan-limit policy, schema/data migration, dependencies, real export/download, or external side effects.

## Definition of Done

- Whitelist actions exactly match backend state predicates; `REMOVAL_REQUESTED` is visibly pending removal and cannot issue another destructive-looking call.
- Frontend URL acceptance matches the backend scheme/host/user-info/port contract and enforces 255 characters before API invocation.
- Editing a processed channel clearly discloses and confirms the existing `PENDING` requeue consequence; no policy transition is added.
- ADMIN Whitelist note length is bounded and tested at 500/501 characters.
- Certification application form renders only after a definitive allowed lookup result; transient failure exposes retry and cannot submit.
- User status and admin list/detail failures expose owned retries and stale responses cannot overwrite newer context.
- Focused frontend/backend/API tests, typecheck, ESLint, changed-file Prettier, production build, docs validation, and diff check pass.
- Evidence pack and Korean user summary record exact results, residual boundaries, and rollback.

## Constraints / Forbidden

- Derive every status predicate, URL rule, note bound, and response shape from current backend code and docs; do not invent policy.
- Preserve WI-040 export recovery and filtering behavior.
- Do not execute live CSV/export, document download, provider/mail/payment calls, or persistent database writes.
- Do not inspect ignored secrets or touch `output/client-demo-screenshots-20260716-140514.zip` / `output/ui-ux-audit/`.
- Do not change schema/dependencies, commit/push, merge/delete branches, or deploy.
- Escalate only a genuine product/security/architecture/schema/destructive/external-effect decision.

[ACCEPTANCE CRITERIA]

## Functional

- [ ] Every whitelist status has exact edit/request/primary/delete-or-removal action coverage and API call-count assertions.
- [ ] YouTube URL tests cover HTTP, user-info, foreign/lookalike host, non-443 port, valid YouTube subdomain, whitespace normalization, and 255/256-character boundaries.
- [ ] Processed-channel edits disclose requeue behavior and do not duplicate update calls.
- [ ] Company-certification apply lookup 404, REJECTED, 403, 5xx/network, retry, stale completion, and submit-gating paths are separately proven.
- [ ] Status-page and admin list/detail retry behavior preserve current context and reject stale completions.
- [ ] ADMIN Whitelist note 500/501-character behavior and exact request payload are proven.

## Performance

- [ ] Retry/request ownership is bounded with no polling or unbounded retained state.
- [ ] No extra backend read/write is introduced for already-known status predicates.

## Quality

- [ ] Focused frontend and backend suites pass.
- [ ] Typecheck, ESLint, changed-file Prettier, production build pass.
- [ ] Documentation validation and `git diff --check` pass.

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
- `docs/design/usecase/whitelist.md`
- `docs/design/usecase/company-certification.md`
- `.agents/skills/react-best-practices/AGENTS.md`

## REQ / Audit / Dependency

- `deliverables/user/REQ-20260809-ATS-001.md`
- `deliverables/agent/WI-20260809-ATS-026-findings.md` (`ATS-026-F01` through `F05`, `F09` through `F11`)
- `deliverables/agent/WI-20260809-ATS-031-consolidated-findings.md` (`CR-031-069` through `073`, `077` through `079`, WI-051 row)
- `deliverables/agent/WI-20260809-ATS-040-handoff.md`
- `deliverables/agent/WI-20260809-ATS-040-evidence-pack.md`

## Primary Files

- `frontend/src/pages/subscriber/WhitelistChannelPage.tsx`
- `frontend/src/pages/subscriber/whitelistChannelPolicy.ts`
- `frontend/src/pages/subscriber/CompanyCertApplyPage.tsx`
- `frontend/src/pages/subscriber/CompanyCertStatusPage.tsx`
- `frontend/src/pages/admin/CompanyCertManagePage.tsx`
- Related CSS/API/validation helpers and focused tests
- Existing whitelist/company-certification backend DTO/entity/service tests where contract proof is needed

[OUTPUT CONTRACT]

- User-facing: `deliverables/user/WI-20260809-ATS-051-summary.md`
- Agent-facing: `deliverables/agent/WI-20260809-ATS-051-evidence-pack.md`
- Handoff: this file
- Scoped implementation/tests/current-state docs in shared workspace; list every changed file.

[TRACEABILITY REQUIREMENTS]

- Map every DoD item to exact source/test/doc pointers.
- Separate UI visibility, API invocation count, backend predicate, durable-state boundary, and unexecuted live effects.
- Record red and green evidence for URL/status/requeue/note/retry boundaries.
- State that CR-074 workflow policy and WI-040 export behavior remain unchanged.
- Document scoped rollback and all intentional WI-058/WI-063 deferrals.

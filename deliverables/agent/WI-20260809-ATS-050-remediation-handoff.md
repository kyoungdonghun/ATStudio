# Remediation Handoff: WI-20260809-ATS-050

[WI HEADER]

- WI ID: `WI-20260809-ATS-050-REMEDIATION`
- REQ: `REQ-20260809-ATS-001` (`approved`)
- Agent: `se`
- Depends On: `WI-20260809-ATS-050-QA-INTEG`
- Blocks: WI-050 independent rereview, finalization, full gates, and commit

[WI SUMMARY]

## Why

Close the two P2 findings and the P3 lifecycle evidence gap in the independent WI-050 review without adding schema, dependencies, attachment policy, or live effects.

## Scope

- Remediate `F-QA-INTEG-050-001`: an accepted Notice mutation may not be abandoned by in-app navigation or silently made retryable after an ambiguous network/server outcome.
- Remediate `F-QA-INTEG-050-002`: pending destructive Modal state must expose an explicit busy/non-closable contract across header close, Escape, backdrop, focus, and accessibility state.
- Fill `F-QA-INTEG-050-003` focused lifecycle proof: public 5xx, valid-to-invalid route, direct unmount during download, and completed mutation followed by destination read failure with exact call counts where the current routing boundary permits it.
- Update focused tests and current-state docs only where behavior changes.

## Required Behavioral Direction

- Use React Router's supported blocking mechanism plus a `beforeunload` guard while a Notice mutation is genuinely pending. The blocker predicate must read immutable/current operation ownership, not stale render state.
- Do not abort an accepted create/update/delete mutation merely because the page cleanup runs. Retire UI writes after unmount, but preserve the request outcome owner until it settles.
- Release the blocker before programmatic navigation after authoritative success.
- Classify authoritative response failures separately from ambiguous `network`, `server`, or `unknown` outcomes. Validation/auth/permission/not-found responses may remain locally retryable when semantically valid.
- After an ambiguous create or attachment-bearing update/delete result, do not immediately re-enable the identical mutation. Present a localized, accessible `처리 결과 확인 필요` recovery state and direct the operator to an observation-only Notice list/detail/edit read. Do not claim success or failure. A later deliberate edit after observation is allowed.
- Do not introduce persistent idempotency, schema, new endpoints, or dependencies in this WI. If a sound bounded recovery cannot be achieved under this rule, stop and report the exact architecture decision rather than approximating silently.
- Extend shared `Modal` with an optional backwards-compatible busy/non-closable contract. While busy: header close is disabled and removed from actionable focus, Escape/backdrop do not close, dialog exposes `aria-busy`, and content can explain the pending action. Existing callers remain unchanged by default.

## Acceptance Criteria

- [ ] Link/sidebar/history navigation is blocked while create/update/delete is pending; accepted mutation is not aborted on component cleanup.
- [ ] Browser unload receives a pending-change guard while the mutation is active.
- [ ] Authoritative failure permits bounded retry; ambiguous outcome does not issue a second POST/PUT/DELETE/file mutation.
- [ ] Success navigation works after the operation fence is released.
- [ ] Pending delete modal close control is programmatically disabled; Escape and backdrop are suppressed by the same contract and recover after failure.
- [ ] Public 5xx, valid-to-invalid route, download-unmount, and destination-read-failure schedules have exact read/mutation/effect call assertions.
- [ ] Existing WI-050 focused/adjacent tests, typecheck, ESLint, changed-file Prettier, and focused backend tests pass.
- [ ] No new attachment limits, schema, endpoint, dependency, actual browser mutation, live file/DB/external effect, or protected-output access.

[INPUT POINTERS]

- `deliverables/agent/WI-20260809-ATS-050-handoff.md`
- `deliverables/agent/WI-20260809-ATS-050-qa-integ-review-result.md`
- `docs/standards/core-principles.md`
- `docs/standards/development-standards.md`
- `docs/policies/quality-gates.md`
- `docs/standards/frontend-standards.md`
- `frontend/src/components/ui/Modal.tsx`
- `frontend/src/pages/admin/NoticeCreatePage.tsx`
- `frontend/src/pages/admin/NoticeEditPage.tsx`
- `frontend/src/pages/public/NoticeDetailPage.tsx`
- `frontend/src/pages/admin/NoticeAdminPages.test.tsx`
- `frontend/src/pages/public/NoticeDetailPage.test.tsx`
- `frontend/src/api/loadError.ts`
- `frontend/package.json` (`react-router-dom` 6.30.x)

[OUTPUT CONTRACT]

- Implement directly in the shared workspace and list every changed file.
- Add red/green focused tests for each finding and exact mutation/read call counts.
- Do not create the final WI evidence pack or summary; docops owns finalization after rereview.
- Do not commit, push, stage, merge, deploy, inspect secrets, or access protected `output/**` artifacts.

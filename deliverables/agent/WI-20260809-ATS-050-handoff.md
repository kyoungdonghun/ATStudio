# WI Handoff Packet: WI-20260809-ATS-050

[WI HEADER]

- WI ID: `WI-20260809-ATS-050`
- REQ: `REQ-20260809-ATS-001` (`approved`)
- Agent: `se`
- Depends On: `WI-20260809-ATS-039`
- Blocks: `WI-20260809-ATS-059`, `WI-20260809-ATS-066`, `WI-20260809-ATS-070`

[WI SUMMARY]

## Why

Provide bounded, localized, recoverable Notice detail/create/edit/download behavior and a non-counting ADMIN read contract. Eliminate duplicate/conflicting actions while preserving WI-039 PRIVATE attachment storage and safe download serving.

## Scope

### In

- `CR-031-001`: public missing Notice distinguishes localized not-found from transient/network failure, exposes safe navigation, and provides retry only where meaningful.
- `CR-031-065`: Notice create/edit form labels, current documented content maximum, validation/recovery, and one coordinated operation state across save, attachment changes, attachment deletion, Notice deletion, close/navigation, and duplicate submit.
- Per-attachment public download ownership: pending/duplicate fence, local bounded failure, same-file retry, safe filename/error normalization using the existing binary helper contract.
- A minimized ADMIN Notice detail read API/service mode that returns the edit projection without incrementing public view count. Update frontend API/edit load to use it.
- Notice edit route canonical positive safe integer validation: malformed IDs issue zero Notice/attachment/mutation calls and render safe recovery. This is the Notice-owned portion of `CR-031-061` left after WI-048/WI-049.
- Latest-request/target ownership for Notice edit/detail loads and route transitions.
- Focused frontend/backend/API/document tests and current-state Notice/API/UI documentation synchronization.

### Out

- New attachment type/count/size policy. `CR-031-104` and its exact limits remain held for WI-066; preserve current accepted behavior.
- Changes to WI-039 PRIVATE storage, safe controller download headers, encoded filenames, or public visibility policy.
- Bulk download, delivery-completion semantics, or streaming changes owned by WI-055/WI-065.
- Public catalog keyboard/headings/fallback work reserved for WI-059.
- Schema migration, dependency/library introduction, live data/storage mutation, real download, external effect, or product-policy change.

## Definition of Done

- Public Notice 404 uses localized missing-state copy and a safe list/back action; transient failure has retry and does not masquerade as 404.
- Public Notice load uses latest target ownership and a retired response cannot repopulate another route or unmounted page.
- Attachment download has immutable per-file ownership, prevents duplicate same-file invocation while pending, keeps other attachments independently available where safe, reports bounded local failure, and permits retry without stale feedback.
- ADMIN create/edit use localized, associated controls and the existing canonical content maximum at both client and server boundaries where not already enforced.
- Save/delete/file-add/file-remove/close/navigation cannot conflict or duplicate while an owned operation is pending; failure remains associated with its action and retry does not repeat a committed mutation.
- ADMIN edit reads through a dedicated ADMIN path/mode and provably does not increment view count; public read still increments exactly according to the existing contract.
- Invalid/missing/non-positive/unsafe Notice edit IDs cause zero Notice/attachment/mutation API calls and show safe navigation.
- WI-039 PRIVATE storage and safe download tests remain green; no new attachment policy is introduced.
- Focused frontend/backend tests, typecheck, ESLint, changed-file Prettier, production build, docs validation, and diff check pass.
- Evidence pack and Korean user summary record exact commands, results, residual boundaries, and rollback.

## Constraints / Forbidden

- Derive content maximum, DTO shape, response envelope, error normalization, and permission style from current code/docs; do not invent values.
- Keep Controller thin, ADMIN authorization explicit, DTO minimized, and public/admin view-count semantics separately tested.
- Do not execute a real attachment download or write Notice rows/files. Use mocks, temp fixtures, MockMvc, and H2/test context only.
- Do not inspect ignored secrets or touch `output/client-demo-screenshots-20260716-140514.zip` / `output/ui-ux-audit/`.
- Do not commit/push, merge/delete branches, deploy, or change schema/dependencies.
- Escalate only a genuine product/security/architecture/schema/destructive/external-effect decision.

[ACCEPTANCE CRITERIA]

## Functional

- [ ] Public 404, 5xx/network, retry, route switch, and unmount schedules are separately proven.
- [ ] Each attachment's pending/failure/retry/duplicate/target-switch behavior is proven with exact download call counts.
- [ ] Create/edit labels, content bounds, submit fence, file state, and recovery are proven.
- [ ] Edit load, save, attachment delete, Notice delete, and route change cannot conflict or commit stale UI.
- [ ] ADMIN detail read increments view count zero times; public detail read retains existing increment behavior.
- [ ] Malformed edit IDs produce zero API calls.
- [ ] WI-039 storage/header/security regressions remain green.

## Performance

- [ ] Request ownership is bounded; no polling/timer loop or unbounded download state accumulation.
- [ ] ADMIN read uses one detail query/projection and no extra metric write.

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
- `docs/design/usecase/user-notice.md`
- `docs/ui/screen-flow.md`
- `docs/ui/atstudio-front-list.md`
- `docs/ui/modal-list.md`
- `.agents/skills/react-best-practices/AGENTS.md`

## REQ / Audit / Dependency

- `deliverables/user/REQ-20260809-ATS-001.md`
- `deliverables/agent/WI-20260809-ATS-031-consolidated-findings.md` (`CR-031-001`, `CR-031-061` Notice portion, `CR-031-065`, WI-050 row)
- `deliverables/agent/WI-20260809-ATS-021-findings.md` (`F-UI-021-001`)
- `deliverables/agent/WI-20260809-ATS-025-findings.md` (`F-UI-025-012`, held `F-UI-025-013` boundary)
- `deliverables/agent/WI-20260809-ATS-039-handoff.md`
- `deliverables/agent/WI-20260809-ATS-039-evidence-pack.md`

## Primary Files

- `frontend/src/pages/public/NoticeDetailPage.tsx`
- `frontend/src/pages/admin/NoticeCreatePage.tsx`
- `frontend/src/pages/admin/NoticeEditPage.tsx`
- Adjacent focused tests
- `frontend/src/api/notices.ts`
- Existing binary error/download helpers
- `src/main/java/com/atstudio/atstudio/controller/NoticeController.java`
- `src/main/java/com/atstudio/atstudio/service/NoticeService.java`
- Notice DTO/repository paths required by the existing projection
- Focused `NoticeControllerTest` / `NoticeServiceTest`

[OUTPUT CONTRACT]

- User-facing: `deliverables/user/WI-20260809-ATS-050-summary.md`
- Agent-facing: `deliverables/agent/WI-20260809-ATS-050-evidence-pack.md`
- Handoff: this file
- Scoped implementation/tests/current-state docs in shared workspace; list every changed file.

[TRACEABILITY REQUIREMENTS]

- Map every DoD item to exact source/test/doc pointers.
- Separate UI behavior, API invocation, authorization, public/admin view-count effect, storage/download boundary, and unexecuted durable/live effects.
- Record red and green evidence and exact mutation/download call counts.
- State that attachment-limit policy remains held and no real file/DB/external effect occurred.
- Document scoped rollback and all intentional WI-055/WI-059/WI-066/WI-070 deferrals.

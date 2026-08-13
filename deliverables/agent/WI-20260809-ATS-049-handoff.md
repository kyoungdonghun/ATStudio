# WI Handoff Packet: WI-20260809-ATS-049

[WI HEADER]

- WI ID: `WI-20260809-ATS-049`
- REQ: `REQ-20260809-ATS-001` (`approved`)
- Agent: `se`
- Depends On: `WI-20260809-ATS-038`
- Blocks: `WI-20260809-ATS-059`, `WI-20260809-ATS-070`

[WI SUMMARY]

## Why

Correct the bounded Album ADMIN create/edit/manage defects established by the approved UI/UX audit: explicit clearing, modal request ownership, complete pagination, thumbnail-selection races, Track search contract and keyboard operation, and invalid Album edit route handling. Preserve the zero-based reorder contract already completed by WI-038.

## Scope

### In

- `CR-031-058`: Album management clear semantics, modal target ownership, pagination, validation consistency, and recoverable list/detail failures.
- `CR-031-059`: thumbnail validation generation ownership, pending submission fence, and object-URL lifecycle across create, edit, and manage entry points.
- `CR-031-060`: Track search copy aligned to title plus `USAGE`, latest-request ownership, keyboard-operable combobox/listbox behavior, and recoverable post-mutation membership refresh.
- Album-only portion of `CR-031-061`: parse the edit route once as a finite positive safe integer; malformed IDs must issue zero Album or membership requests and provide safe recovery navigation. Track was completed in WI-048 and Notice remains WI-050.
- Focused frontend tests for every state transition and contract above, plus current Album API/document synchronization required by the implementation.

### Out

- Reorder payload or policy changes owned and completed by WI-038.
- Public Album catalog keyboard semantics, image fallback, or heading work reserved for WI-059.
- New Album product policy, schema/data migration, dependency/library introduction, or backend API expansion unless a confirmed current-contract defect makes it unavoidable; escalate first.
- Real Album/media mutation against a live DB, external storage/provider/mail/payment/download side effects, protected output inspection, branch merge/delete, or deployment.

## Definition of Done

- Blank Album description is an explicit clear and does not silently preserve stale content.
- Opening/switching/closing the manage edit modal cannot expose or submit another Album's detail; stale responses lose ownership.
- Album management pagination exposes all pages, canonicalizes invalid/beyond-last states, and provides bounded retry without request loops.
- All Album thumbnail entry points share equivalent accepted-file/size/dimension behavior; selection generations prevent stale completion and submit is blocked while validation is pending.
- Every created object URL is revoked on replacement, rejection, clear, and unmount.
- Track search describes title plus Usage, applies latest-request ownership, and provides correct combobox/listbox roles, active option, keyboard navigation, selection, and dismissal.
- Add/remove/reorder success followed by membership refresh failure is represented as committed-but-refresh-failed; retry refresh must not repeat the mutation.
- Malformed Album edit IDs cause zero Album/membership API calls and show bounded recovery.
- WI-038 zero-based reorder behavior remains unchanged and its exact tests continue to pass.
- Focused tests, frontend typecheck, ESLint, changed-file Prettier, production build, documentation validation, and diff check pass.
- User summary and evidence pack are created with exact files, commands, results, residual risks, and rollback.

## Constraints / Forbidden

- Preserve current Korean product copy unless correcting the audited false search contract or adding necessary recovery/status text.
- Do not invent a new file-format, dimension, page-size, or search policy; derive exact values from current backend contracts and canonical docs.
- Do not weaken WI-038 reorder tests or rewrite its payload contract.
- Do not use real ADMIN credentials, mutate a running DB, upload real/private media, or invoke external services.
- Do not inspect, hash, move, stage, delete, or otherwise touch `output/client-demo-screenshots-20260716-140514.zip` or `output/ui-ux-audit/`.
- Do not inspect ignored secret/local-environment values. Do not commit or push.
- If an architecture, dependency, schema, security, destructive-data, or product-policy decision is required, stop and report it.

[ACCEPTANCE CRITERIA]

## Functional

- [ ] Explicit description clearing is proven at request shape and UI state.
- [ ] Modal load ownership, close/reopen, A-to-B switching, failed load, and retry are proven.
- [ ] Management pagination, URL normalization, beyond-last recovery, and retry are proven without loops.
- [ ] Thumbnail accept/reject/pending/stale-generation/same-file retry/object-URL cleanup are proven for all three entry points.
- [ ] Track search copy, latest-wins behavior, empty/error/retry states, and full keyboard combobox operation are proven.
- [ ] Post-mutation refresh failure and refresh-only retry are proven without duplicate mutation.
- [ ] Invalid/missing/non-positive/unsafe Album IDs issue zero protected requests.
- [ ] WI-038 reorder payload and recovery behavior remain green.

## Performance

- [ ] Request ownership uses bounded generation/abort logic with no polling or timer loop.
- [ ] Pagination and search do not introduce unbounded client accumulation.

## Quality

- [ ] Focused Vitest suites pass.
- [ ] `npm run typecheck`, `npm run lint`, changed-file Prettier, and `npm run build` pass.
- [ ] `python .agents/skills/validate-docs/scripts/validate_docs.py` passes.
- [ ] `git diff --check` passes, excluding only the protected output boundary where needed.

[INPUT POINTERS]

## Tier 0

- `docs/standards/core-principles.md`
- `docs/standards/development-standards.md`

## Tier 1

- `docs/policies/quality-gates.md`

## Tier 2

- `docs/standards/frontend-standards.md`
- `docs/ui/screen-flow.md`
- `docs/ui/atstudio-front-list.md`
- `docs/ui/modal-list.md`
- `docs/design/api-spec.md`
- `docs/design/usecase/sound-album.md`
- `docs/design/usecase/sound-track.md`
- `.agents/skills/react-best-practices/AGENTS.md`

## REQ / Audit / Dependency

- `deliverables/user/REQ-20260809-ATS-001.md`
- `deliverables/agent/WI-20260809-ATS-031-consolidated-findings.md` (`CR-031-058` through `CR-031-061`, WI-049 row)
- `deliverables/agent/WI-20260809-ATS-025-findings.md` (`F-UI-025-005` through `F-UI-025-008`)
- `deliverables/agent/WI-20260809-ATS-038-handoff.md`
- `deliverables/agent/WI-20260809-ATS-038-evidence-pack.md`

## Primary Files

- `frontend/src/pages/creator/AlbumManagePage.tsx`
- `frontend/src/pages/creator/AlbumCreatePage.tsx`
- `frontend/src/pages/creator/AlbumEditPage.tsx`
- Existing/new focused tests adjacent to those pages
- `frontend/src/api/albums.ts`
- `frontend/src/api/tracks.ts`
- `frontend/src/utils/validation.ts`
- `src/main/java/com/atstudio/atstudio/controller/AlbumController.java`
- `src/main/java/com/atstudio/atstudio/service/AlbumService.java`
- Album request/response DTOs and focused backend tests only if needed to verify the existing contract

[OUTPUT CONTRACT]

- User-facing: `deliverables/user/WI-20260809-ATS-049-summary.md`
- Agent-facing: `deliverables/agent/WI-20260809-ATS-049-evidence-pack.md`
- Handoff: `deliverables/agent/WI-20260809-ATS-049-handoff.md`
- Implementation: edit the bounded source, test, and current-state documentation files directly in the shared workspace; list every changed file.

[TRACEABILITY REQUIREMENTS]

- Map every acceptance criterion to exact source/test/document pointers.
- Record red-state evidence from the current source and green-state evidence after the patch.
- Record exact commands and results; do not claim unrun tests.
- Separate UI state, request shape, server contract, and durable/external effects.
- Document rollback as a scoped code/test/doc revert; no data rollback should be required.
- Record residual work for WI-059/WI-070 and any newly discovered out-of-scope issue without silently expanding scope.

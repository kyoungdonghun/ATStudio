# Evidence Pack: WI-20260809-ATS-016

## Summary

- Audited WI-008 through WI-015 current-state documentation against final
  WI-028/WI-029 dispositions and current code, then corrected four narrow
  stale/contradictory statements.

## Scope / DoD Check

- [x] Compared WI-028/WI-029 final dispositions and WI-008 through WI-015
      summaries/Evidence Packs with the handoff's current-state documents.
- [x] Confirmed admin correction rejection/ambiguity, audit minimization, actor
      locking, and accepted V1 residual wording.
- [x] Confirmed media/catalog/playback nullable fields, duration transition,
      taxonomy isolation, active memberships, and zero-based Playlist reorder.
- [x] Confirmed SR-93 through SR-101 index status and acceptance boundaries.
- [x] Confirmed documentation, SR, API, DB, page, and modal counts.
- [x] Ran documentation validation, repository `git diff --check`, and scoped
      WI-016 deliverable Prettier.
- [x] Preserved historical records and avoided whole-file formatting churn.

## Reference Documents

| Tier    | Documents                                                                                                      | Reason                                                   |
| ------- | -------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------- |
| 0       | `docs/standards/core-principles.md`, `docs/standards/documentation-standards.md`, `docs/standards/glossary.md` | Constitution, documentation, and canonical terminology   |
| 1       | `docs/policies/quality-gates.md`, `docs/policies/security-policy.md`                                           | Quality and security boundaries                          |
| REQ     | `deliverables/user/REQ-20260808-ATS-004.md`                                                                    | Approved V1 review/repair scope                          |
| Review  | WI-028/WI-029 summaries and Evidence Packs                                                                     | Authoritative final dispositions and accepted residuals  |
| Repair  | WI-008 through WI-015 summaries and Evidence Packs                                                             | Implemented repair and independent verification evidence |
| Handoff | `deliverables/agent/WI-20260809-ATS-016-handoff.md`                                                            | Scope, input pointers, DoD, and constraints              |

All handoff-listed current-state docs and implementation evidence were audited.
No external source or unstated behavior was used.

## Corrected Statement Ledger

| Document pointer                           | Prior contradiction                                                                                       | Correct current statement                                                                                                                                                   | Code/test evidence                                                                                                                                                                     |
| ------------------------------------------ | --------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `docs/SR/SR-97.md:119`                     | WI-009 wording implied every rejected mutation performed reconciliation and omitted request-204 handling. | HTTP 4xx is definite with no read; no-response/network/timeout/5xx is ambiguous; a failed read or request 204 keeps the fence and read-only retry does not replay mutation. | `frontend/src/pages/admin/UserSubscriptionCorrectionModal.tsx:200`, `:419`, `:484`, `:507`; `frontend/src/pages/admin/UserSubscriptionManagePage.test.tsx:643`, `:681`, `:709`, `:748` |
| `docs/ui/atstudio-front-list.md:99`        | "Browser Seoul-date checks are advisory" implied a browser business-date comparison still existed.        | Browser validates required/calendar-valid input only; server preview and injected business clock own Seoul date rules.                                                      | `frontend/src/pages/admin/UserSubscriptionCorrectionModal.tsx:386`; `src/main/java/com/atstudio/atstudio/service/AdminSubscriptionCorrectionService.java:355`                          |
| `docs/design/usecase/sound-album.md:92`    | Claimed every aggregate row always included waveform and never normalized `waveformData=null`.            | `@JsonInclude(NON_NULL)` may omit nullable media members; the shared mapper normalizes omission/null to explicit `null` and keeps declared duration.                        | `src/main/java/com/atstudio/atstudio/dto/album/AlbumTrackItemResponse.java:6`; `frontend/src/utils/playableTrack.ts:13`; `frontend/src/utils/playableTrack.test.ts:46`                 |
| `docs/design/usecase/sound-playlist.md:71` | Described waveform as unconditionally present.                                                            | Active detail rows always carry order/duration; nullable media members may be omitted and are normalized to explicit `null`.                                                | `src/main/java/com/atstudio/atstudio/dto/playlist/PlaylistTrackItemResponse.java:6`; `frontend/src/utils/playableTrack.ts:13`; `frontend/src/utils/playableTrack.test.ts:46`           |

## Confirmed Without Change

- `docs/design/api-spec.md:268-284` matches Track page/size bounds, active
  Playlist/Album membership projections, and zero-based Playlist reorder.
- `docs/design/usecase/sound-playlist.md:133-145` matches the active membership
  and inactive-row retention postcondition.
- `frontend/src/pages/subscriber/PlaylistEditPage.tsx:115-124` emits
  `trackOrder: i`; `src/main/java/com/atstudio/atstudio/service/PlaylistService.java:334-363`
  requires every integer in `0..n-1`.
- `frontend/src/pages/subscriber/PlaylistEditPage.test.tsx:83-99` and
  `frontend/src/test/coverage/adminSubscriberPages.coverage.test.tsx:953-978`
  retain exact zero-based frontend regressions.
- `docs/design/api-spec.md:63-71`, `docs/design/db-schema.md:171-185`,
  `docs/design/usecase/user-subscription.md:61-119`, and
  `docs/policies/security-policy.md:266-275` agree on actor recheck, null
  rejection `reasonNote`, authoritative operator-text locations, and zero
  provider side effects.
- WI-028 remains PASS with accepted V1 residuals: no preview receipt/token, no
  free-text DLP, no active-ADMIN composite index, no live MySQL timing/deployed
  network-loss proof, and no polling/backend correlation protocol.
- WI-029 remains PASS with zero-based Playlist reorder independently verified.

## Index And Registry Evidence

- SR inventory: 100 files and 100 indexed rows; 82 DONE, 15 OPEN, 2 NOT
  CONFIRMED, 1 DROPPED.
- Documentation category check: all category counts match `docs/index.md`; total
  is 201. Only `docs/design/` is recursively counted under the documented rule.
- Current source recount: 25 controller files / 144 method mappings; 41
  `CREATE TABLE` statements / 41 JPA entities; 53 `*Page.tsx` page files; 22
  `<Modal>` occurrences across 17 non-test TSX files.
- `docs/index.md`, `docs/design/index.md`, `docs/SR/index.md`, and
  `docs/registry/project-registry.md` required no WI-016 change.

## Commands And Outputs

1. `python .agents/skills/validate-docs/scripts/validate_docs.py`
   - PASS: Tier 0 documents, internal links, 526 traceability IDs, and document
     index validation.
2. `git diff --check`
   - PASS: exit code 0, no whitespace errors. Existing CRLF-to-LF warnings are
     informational and were not normalized.
3. PowerShell source recounts for controllers/mappings, tables/entities,
   pages/modals, SR statuses, and documentation categories.
   - PASS: all values match current indexes and registries.
4. `npx prettier --check "../deliverables/user/WI-20260809-ATS-016-summary.md" "../deliverables/agent/WI-20260809-ATS-016-evidence-pack.md"`
   - PASS after formatting only these two new WI-016 deliverables.

## Accepted Format Debt

- Shared current-state Markdown files already contained whole-file Prettier
  differences disclosed by predecessor WIs. WI-016 changed only evidenced
  lines and did not rewrite those files for formatting.
- Scoped Prettier ownership is limited to the two new WI-016 deliverables.
- `git diff --check` proves the repository diff has no whitespace errors; line
  ending warnings are not formatting failures.

## Constraints And External Effects

- No implementation, test, schema, retained data, dependency, secret, ZIP,
  external call, branch, staging, commit, push, or client-branch action.
- No whole-file historical rewrite and no change to historical finding text.
- Existing user/predecessor dirty work was preserved.

## Risks / Rollback

- Risk: this is a static documentation audit. It relies on current source and
  focused predecessor test evidence and does not replace full browser, full
  suite, MySQL timing, external provider, or production validation.
- Rollback: inverse only the WI-016 hunks in the four current-state documents
  and remove the two WI-016 deliverables. Preserve every other dirty or
  untracked path.

## WI-030 Unblock Status

- WI-028 final disposition: PASS.
- WI-029 final disposition: PASS.
- WI-016 documentation consistency: PASS.
- `WI-20260808-ATS-030` is unblocked from the WI-016 documentation perspective,
  without claiming production readiness or performing release/Git actions.

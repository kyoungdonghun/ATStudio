[WI HEADER]
WI ID: WI-20260809-ATS-045-OWNER-PROJECTION-REMEDIATION
REQ: REQ-20260809-ATS-001
Agent: se
Depends On: WI-20260809-ATS-045-QA-FE-VERIFICATION
Blocks: WI-20260809-ATS-045 completion

[WI SUMMARY]
Why: Final independent verification found that first-generation request fencing still permits a brief prior-owner projection before passive cleanup, and Download History bulk preparation/count/download continuation is not bound to the initiating owner.
Scope (in/out):
- In: Define a dependency-free sanitized auth-owner key helper if useful; never display/log the key.
- In: For Playlist list/detail/edit/Drawer, Like list, License list/detail, Question list/detail, and Download History, tag successful projections with the full current read key: authenticated owner plus route ID, page/filter/tab, and drawer owner/session where applicable.
- In: Derive renderable data, selected targets, dialogs, player context, and existing mutation controls synchronously from equality between the current key and committed projection key. Prior-owner/route/query/tab data must be absent and controls inert before passive effects run.
- In: Keep request abort/generation ownership so stale success, failure, empty, and finally remain blocked.
- In: For PlaylistList and PlaylistDrawer, owner-tag create/delete/detail state and revalidate the current auth owner plus projection key immediately before existing mutations. Do not change their confirmation or result policy.
- In: For Download History, owner-tag selected/all confirmation targets and fence single/selected/all download flows, Track-ID preparation, count refresh, success/failure/finally, and each loop iteration to the initiating owner. Add optional AbortSignal to read/download wrappers only as needed to retire requests.
- In: Clear or hide page-owned player context synchronously when its projection key no longer matches.
- In: Add tests using a layout-effect observation/probe where appropriate so prior data/actionability is checked before passive effects; cover same-user token replacement, valid-to-valid route transition, drawer close/reopen, open modals, delayed Track-ID/count/download completions, and original stale-response cases.
- In: Update WI-045 current-state docs only where this stronger contract changes wording.
- Out: New user-facing mutation policy, new confirmations, Playlist mutation retry/preview behavior owned by WI-046, Question status/attachment behavior owned by WI-047, keyboard semantics owned by WI-059, backend/schema/data changes, and real side effects.
DoD: No prior-owner or retired read projection/control can render or execute before/after effects; Download History bulk and count work cannot cross auth-owner boundaries; original WI-045 roots and strict route-ID behavior remain green; focused/full gates pass.
Constraints/Forbidden:
- Do not store or expose raw tokens in DOM, docs, logs, errors, test snapshots, or persisted storage. An in-memory opaque equality key may include the already-present token only if never emitted.
- Avoid broad architecture or dependency changes; a small pure key helper is allowed only to remove genuine duplication.
- Preserve existing mutation confirmation/result semantics except for fail-closed owner validation and cancellation.
- Tests must mock every download/provider-like effect; invoke no real authenticated mutation/download/export/provider/mail/payment/DB action.
- Do not stage, commit, push, create final WI evidence/summary, touch protected output, or inspect ignored configuration.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Same-role user or token-only replacement synchronously hides all prior member rows/details/modals/player context and makes detached controls inert before passive effects.
- [ ] Valid-to-valid detail route and list query/tab transitions synchronously hide the retired projection and ignore every stale completion.
- [ ] Drawer close/reopen and owner change cannot display or act on an earlier session's list/detail/create/delete state.
- [ ] Download History selected/all preparation, confirmation, download loop, count refresh, messages, and finally state are bound to the initiating current owner; replacement aborts or suppresses all retired work.
- [ ] Current-owner normal flows and strict positive decimal route IDs continue to work.
Performance:
- [ ] Equality checks are constant-time; no polling, render loops, duplicated fetch loops, or new global listeners.
Quality:
- [ ] RED/GREEN tests prove pre-passive-effect projection blocking, token-only replacement, valid-to-valid transition, and delayed bulk/count completion.
- [ ] Focused, full frontend, coverage, typecheck, ESLint, Prettier, build, backend regression, docs validation, and diff check pass.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 1:
- docs/policies/quality-gates.md
- docs/policies/access-control-policy.md
- docs/standards/evidence-pack-standard.md

Tier 2:
- docs/standards/frontend-standards.md
- .agents/skills/react-best-practices/AGENTS.md
- docs/design/usecase/sound-playlist.md
- docs/design/usecase/user-license.md
- docs/design/usecase/user-question.md
- docs/design/usecase/download-queue.md
- docs/ui/screen-flow.md
- docs/ui/atstudio-front-list.md

REQ/Context Docs:
- deliverables/user/REQ-20260809-ATS-001.md
- deliverables/agent/WI-20260809-ATS-045-handoff.md
- deliverables/agent/WI-20260809-ATS-045-qa-fe-review-handoff.md
- deliverables/agent/WI-20260809-ATS-045-qa-remediation-handoff.md
- deliverables/agent/WI-20260809-ATS-045-qa-fe-verification-handoff.md

Files:
- Current complete uncommitted WI-045 source/tests/docs diff.
- frontend/src/api/downloads.ts and its API tests if signal support is required.
- Existing auth/load key helpers and test utilities before adding a new helper.

[OUTPUT CONTRACT]
- Final response only: changed files, root/finding evidence, RED/GREEN and validation results, and blockers. No final deliverable files.

[TRACEABILITY REQUIREMENTS]
Evidence pointers: Required for both verification P2s and test-gap P3.
Tests: Exact pre-effect/token/route/drawer/bulk/count cases plus all focused non-live gates.
Rollback: Revert this owner-projection layer without altering earlier WI-045 route-ID or response-fencing changes; no data rollback.

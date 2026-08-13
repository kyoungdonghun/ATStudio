[WI HEADER]
WI ID: WI-20260809-ATS-048-REMEDIATION
REQ: REQ-20260809-ATS-001
Agent: se
Depends On: WI-20260809-ATS-048-QA-INTEG-REVIEW
Blocks: WI-20260809-ATS-048 final QA/finalization

[WI SUMMARY]
Why: Close all two P2 and two P3 findings from the independent WI-048 integration review without expanding scope.
Scope (in): Restore the deliberate iOS audio-picker omission while retaining MP3/WAV JS validation; revert unrelated mixed `Track` UI copy to established Korean; correct Track ADMIN API current-state docs and mapping count; add explicit false Tag-intent and stale Tag-impact response counterexample tests; update WI-048 evidence/summary only as needed after focused verification.
Scope (out): New behavior, generic latest-request ownership, Album/Notice handling, schema/data mutation, real deletion, external side effects, dependency or architecture changes.
DoD: F-QAI-048-001 through 004 are all closed with focused green tests and exact doc-code agreement. No unrelated source churn.
Constraints/Forbidden: Do not touch/open/hash protected output ZIP/folder or ignored secrets. No Git. No real Track/Tag deletion. Preserve all successful WI-048 implementation contracts.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] `AUDIO_ACCEPT` omits the native attribute on iOS while desktop advertises only MP3/WAV hints; JavaScript still accepts only MP3/WAV and rejected inputs reset.
- [ ] Tests prove both iOS omission and desktop hints without relying on module-cache leakage.
- [ ] Track management/recovery UI restores established Korean labels (`음원 관리`, `+ 새 음원`, `곡 제목 검색`, empty/delete copy) and Track edit invalid-ID recovery uses the matching Korean destination label.
- [ ] Backend test directly proves `replaceTags=false` plus nonempty IDs preserves existing associations.
- [ ] Frontend deferred A/B Tag impact test proves A's late response cannot replace B or expose A deletion confirmation.
Documentation:
- [ ] SOUND-021 uses exact query name `is_active`, distinguishes response `isActive`, and lists every `AdminTrackListItemResponse` field with `tags: List<TagResponse>`.
- [ ] `docs/design/api-spec.md` contains no stale current/recount value of 150 and consistently states 151 with GET 76.
- [ ] iOS picker exception is documented alongside MP3/WAV contract.
- [ ] Evidence pack and user summary record remediation and final focused commands without claiming pending independent/final gates as complete.
Quality:
- [ ] Focused frontend/backend tests pass, typecheck/lint/Prettier pass, docs validation and diff check pass.
- [ ] No existing test is removed or weakened.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
- docs/standards/documentation-standards.md
- docs/standards/glossary.md

Tier 1:
- docs/policies/quality-gates.md
- docs/policies/access-control-policy.md

Tier 2:
- docs/standards/frontend-standards.md
- docs/design/usecase/sound-track.md
- docs/design/api-spec.md

REQ/WI/QA:
- deliverables/user/REQ-20260809-ATS-001.md
- deliverables/agent/WI-20260809-ATS-048-handoff.md
- deliverables/agent/WI-20260809-ATS-048-evidence-pack.md
- deliverables/user/WI-20260809-ATS-048-summary.md
- deliverables/agent/WI-20260809-ATS-048-qa-integ-review-result.md

Primary files:
- frontend/src/utils/validation.ts
- frontend/src/utils/validationHelpers.test.ts
- frontend/src/pages/creator/TrackUploadPage.test.tsx
- frontend/src/pages/creator/TrackEditPage.tsx
- frontend/src/pages/creator/TrackEditPage.test.tsx
- frontend/src/pages/admin/TrackManagePage.tsx
- frontend/src/pages/admin/TrackManagePage.test.tsx
- frontend/src/test/coverage/adminSubscriberPages.coverage.test.tsx
- frontend/src/pages/admin/TagManagePage.test.tsx
- src/test/java/com/atstudio/atstudio/service/TrackServiceTest.java
- docs/design/usecase/sound-track.md
- docs/design/api-spec.md

[OUTPUT CONTRACT]
Agent-facing: amend deliverables/agent/WI-20260809-ATS-048-evidence-pack.md with remediation evidence.
User-facing: amend deliverables/user/WI-20260809-ATS-048-summary.md only where the corrected behavior or counts matter.
Report exact files and commands to parent; do not commit.

[TRACEABILITY REQUIREMENTS]
- Explicitly close F-QAI-048-001, 002, 003, and 004 one by one.
- Preserve WI-053 deferral and CR-031-054 behavior.
- State no protected output/external effect/real deletion occurred.

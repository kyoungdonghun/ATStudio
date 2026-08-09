[WI HEADER]
WI ID: WI-20260809-ATS-016
REQ: REQ-20260808-ATS-004
Agent: docops
Depends On: WI-20260809-ATS-008~015
Blocks: WI-20260808-ATS-030

[WI SUMMARY]
Why: Close current-state documentation drift after the independent reviews and supplemental repairs, without turning the V1 work into a repository-wide historical reformat.
Scope (in/out): Audit the current modified `docs/` files against the implemented code and final WI-028/WI-029 dispositions. Correct stale or contradictory behavior, API, data, UI, and operating statements caused by WI-008~015. Review the latest WI outputs for internal agreement. Keep historical SR/REQ/WI evidence as history. Do not mass-format untouched historical files or rewrite entire documents solely to satisfy a pre-existing Prettier baseline.
DoD: Current-state docs accurately describe admin subscription correction, media/catalog/playback, Playlist zero-based reorder, and accepted V1 residuals; indexes/counts remain correct; documentation validation and `git diff --check` pass; any pre-existing whole-file Markdown Prettier debt is explicitly separated from new content.
Constraints/Forbidden: Documentation and WI deliverables only. No implementation, schema, data, dependency, secret, ZIP, external call, commit, push, branch, or client-branch changes. Preserve historical records and existing user-authored content. Do not infer behavior without code or test evidence.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Compare final WI-028/WI-029 dispositions and WI-008~015 evidence with affected current-state docs.
- [ ] Correct stale or contradictory statements, including zero-based Playlist reorder and admin correction ambiguity/rejection behavior.
- [ ] Confirm SR-93~101 status and acceptance wording remain aligned with implementation.
- [ ] Confirm document indexes and registry counts remain accurate.
Performance:
- [ ] Not applicable; documentation-only work adds no runtime behavior.
Quality:
- [ ] `python .agents/skills/validate-docs/scripts/validate_docs.py` passes.
- [ ] `git diff --check` passes.
- [ ] New/edited WI-016 deliverables pass scoped Prettier.
- [ ] Existing whole-file Markdown format debt is not expanded into unrelated churn.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/documentation-standards.md
- docs/standards/glossary.md
Tier 1:
- docs/policies/quality-gates.md
- docs/policies/security-policy.md
Context:
- deliverables/user/REQ-20260808-ATS-004.md
- deliverables/user/WI-20260808-ATS-028-summary.md
- deliverables/agent/WI-20260808-ATS-028-evidence-pack.md
- deliverables/user/WI-20260808-ATS-029-summary.md
- deliverables/agent/WI-20260808-ATS-029-evidence-pack.md
- deliverables/user/WI-20260809-ATS-008-summary.md~WI-20260809-ATS-015-summary.md
- deliverables/agent/WI-20260809-ATS-008-evidence-pack.md~WI-20260809-ATS-015-evidence-pack.md
Current-state docs:
- docs/SR/SR-93.md~docs/SR/SR-101.md
- docs/SR/index.md
- docs/design/api-spec.md
- docs/design/db-schema.md
- docs/design/usecase/user-info.md
- docs/design/usecase/user-subscription.md
- docs/design/usecase/sound-album.md
- docs/design/usecase/sound-playlist.md
- docs/design/usecase/sound-track.md
- docs/ui/atstudio-front-list.md
- docs/ui/modal-list.md
- docs/ui/screen-flow.md
- docs/policies/security-policy.md
- docs/index.md
- docs/design/index.md
- docs/registry/project-registry.md
Implementation evidence:
- frontend/src/pages/admin/UserSubscriptionCorrectionModal.tsx
- frontend/src/pages/admin/UserSubscriptionManagePage.tsx
- frontend/src/pages/subscriber/PlaylistEditPage.tsx
- frontend/src/api/admin.ts
- src/main/java/com/atstudio/atstudio/service/AdminSubscriptionCorrectionService.java
- src/main/java/com/atstudio/atstudio/service/AdminOperationRejectionAuditService.java
- src/main/java/com/atstudio/atstudio/service/PlaylistService.java

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260809-ATS-016-summary.md
Agent-facing -> deliverables/agent/WI-20260809-ATS-016-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260809-ATS-016-handoff.md

[TRACEABILITY REQUIREMENTS]
List every corrected statement with document and code/test evidence, validation commands, accepted residual format debt, rollback notes, and WI-030 unblock status.

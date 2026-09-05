# WI-20260905-ATS-004 Handoff

[WI HEADER]
WI ID: WI-20260905-ATS-004
REQ: REQ-20260905-ATS-001
Agent: se
Depends On: WI-20260905-ATS-001
Blocks: WI-20260905-ATS-002 final regression; WI-20260905-ATS-003 closeout

[WI SUMMARY]
Why: Correct only the two evidenced P2 defects in the approved client-feedback diff.
Scope: Synchronize actual PlaylistDrawer tab with PlayerBar toggle; align nickname edge trim with ECMAScript trim at all Java DTO/service/entity entry paths. Add focused regressions. No storage/player persistence change without a separate proved cause.
DoD: Actual child-tab then parent Likes/Playlists transition regression passes. Edge U+00A0/U+2007/U+202F/U+FEFF normalization is consistent before validation/lookup/persistence; internal whitespace policy remains unchanged.
Constraints: Preserve all existing work, no DB/data/client-worktree/secrets/provider changes. No extra dependencies, broad refactor, git stage or commit. Do not run full suites; MA owns them. Use apply_patch.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Same visible drawer tab toggles closed; different visible tab switches without closing.
- [ ] Java edge trim matches the frontend's existing normalization; add DTO/lookup and frontend edge tests.
Performance:
- [ ] Changes stay within the two affected behaviors.
Quality:
- [ ] Focused tests pass; no full suite duplication.
- [ ] Evidence records exact touched paths and tests.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
- docs/standards/documentation-standards.md
- docs/standards/glossary.md
Tier 1:
- docs/policies/security-policy.md
- docs/policies/quality-gates.md
Tier 2:
- .agents/skills/react-best-practices/SKILL.md
- docs/design/api-spec.md
- docs/ui/screen-flow.md
- deliverables/agent/WI-20260905-ATS-001-evidence-pack.md
- deliverables/user/REQ-20260823-ATS-001.md
- frontend/src/layouts/PlayerBar.tsx
- frontend/src/components/player/PlaylistDrawer.tsx
- frontend/src/utils/validation.ts
- src/main/java/com/atstudio/atstudio/common/validation/ValidationConstants.java
- src/main/java/com/atstudio/atstudio/dto/user/RegisterRequest.java
- src/main/java/com/atstudio/atstudio/dto/user/CompleteProfileRequest.java
- src/main/java/com/atstudio/atstudio/dto/user/UpdateProfileRequest.java
- src/main/java/com/atstudio/atstudio/service/UserService.java
- src/main/java/com/atstudio/atstudio/entity/User.java

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260905-ATS-004-summary.md
Agent-facing -> deliverables/agent/WI-20260905-ATS-004-evidence-pack.md
Use create-wi-evidence-pack skill. List changed files and reproducible focused test results.

[TRACEABILITY REQUIREMENTS]
Evidence: Exact code/test paths and command results.
Rollback: Revert only this WI's patch, never pre-existing client-feedback changes.

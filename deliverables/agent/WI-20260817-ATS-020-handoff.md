[WI HEADER]
WI ID: WI-20260817-ATS-020
REQ: REQ-20260817-ATS-001
Agent: qa-integ
Depends On: WI-20260817-ATS-019
Blocks: -

[WI SUMMARY]
Why: Independently verify that the new Track-thumbnail frontend resolution guard matches the existing backend contract, blocks the reported 6048x4032 image before submission, preserves bounded non-square acceptance, and is reflected by the running client-review frontend without touching backend or development work.
Scope (in/out): In scope is read-only diff review, focused frontend regression tests and static checks, local/public served-module inspection, process/worktree isolation, and two verification deliverables. Out of scope is product/test code modification, authenticated data submission unless a session is already safely available, backend restart/change, DB/storage mutation, tunnel restart, and the development worktree.
DoD:
- Independently confirm exact 4096px and 16,777,216-pixel comparisons and the reported Korean field message.
- Confirm 6048x4032 is invalid, 4096x4096 is accepted, and bounded non-square images remain valid.
- Confirm dimension and square policies are separate so restoring the square flag later preserves both checks.
- Confirm Upload/Edit block invalid resolution and allow a valid replacement.
- Confirm no additional decode/request/dependency and no backend/Album/Playlist behavioral change.
- Re-run focused Vitest, typecheck, ESLint, changed-file Prettier, and diff check.
- Confirm running Vite/public module reflects the new limits/message and frontend/API health remains 200.
- Confirm the development branch/worktree and backend source/process are not changed by WI-019/020.
Constraints/Forbidden:
- Do not modify product or test code.
- Only create `deliverables/user/WI-20260817-ATS-020-summary.md` and `deliverables/agent/WI-20260817-ATS-020-evidence-pack.md`.
- Do not stop/restart any service or tunnel, submit public data, inspect secrets, or modify the development worktree.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] 6048x4032 produces the precise frontend resolution error and disabled submit behavior in focused tests.
- [ ] Exact boundary and bounded non-square behavior match the backend contract.
- [ ] Public/local served module includes the same constants, guard, and message.
Performance:
- [ ] Validation reuses the existing image load dimensions and adds no decode, request, or dependency.
Quality:
- [ ] Vitest, typecheck, ESLint, Prettier, and diff check independently pass.
- [ ] Findings are classified BLOCKER/MAJOR/MINOR with evidence pointers.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md

Tier 0 (Standards - Based on Assignee):
- docs/standards/development-standards.md

Tier 1 (Policies - Inferred from REQ/WI content):
- docs/policies/quality-gates.md
- docs/standards/evidence-pack-standard.md

Tier 2 (Tech Stack - React):
- .agents/skills/react-best-practices/AGENTS.md
- docs/standards/frontend-standards.md
- docs/design/api-spec.md

REQ/Context Docs:
- deliverables/user/REQ-20260817-ATS-001.md
- deliverables/agent/WI-20260817-ATS-019-handoff.md
- deliverables/user/WI-20260817-ATS-019-summary.md
- deliverables/agent/WI-20260817-ATS-019-evidence-pack.md
- docs/SR/SR-98.md

Files:
- frontend/src/utils/validation.ts
- frontend/src/pages/creator/TrackThumbnailField.tsx
- frontend/src/pages/creator/TrackThumbnailField.test.tsx
- frontend/src/pages/creator/TrackUploadPage.test.tsx
- frontend/src/pages/creator/TrackEditPage.test.tsx
- src/main/java/com/atstudio/atstudio/service/image/CanonicalImageService.java:35-36

Repro/Logs:
- `DSC_2446.JPG`: 6048x4032, 24,385,536 pixels, 7.815 MiB.
- Local frontend: http://127.0.0.1:5173/
- Local API: http://127.0.0.1:8080/api/tracks
- Public base: https://cleaners-capital-spirit-commerce.trycloudflare.com

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260817-ATS-020-summary.md :
- Korean PASS/FAIL verdict, findings, behavior, runtime reflection, tests, and limitations.
Agent-facing -> deliverables/agent/WI-20260817-ATS-020-evidence-pack.md :
- Independent evidence pointers, commands/results, process and diff isolation, findings, and rollback note.
Handoff Packet -> deliverables/agent/WI-20260817-ATS-020-handoff.md :
- This packet.

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required
Tests: Include actual counts and exit outcomes for all specified checks.
Rollback: Verify WI-019 rollback removes only the frontend resolution guard while preserving the square-policy switch.

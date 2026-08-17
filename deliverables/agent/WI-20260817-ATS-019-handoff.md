[WI HEADER]
WI ID: WI-20260817-ATS-019
REQ: REQ-20260817-ATS-001
Agent: se
Depends On: WI-20260817-ATS-002
Blocks: WI-20260817-ATS-020

[WI SUMMARY]
Why: The Track thumbnail frontend currently accepts a decoded high-resolution image and submits it even though the existing backend rejects dimensions above 4096px or 16,777,216 total pixels with a generic HTTP 400 response. Add matching client-side validation so the operator sees the actual reason before submission.
Scope (in/out): In scope is the client-acceptance worktree only, Track create/edit thumbnail selection, shared frontend validation constants only when needed, focused tests, and guidance that remains compatible with temporarily disabled square validation. Out of scope is backend code, Album/Playlist behavior, database/storage data, automatic resizing, image editing, and the development worktree.
DoD:
- A decoded Track thumbnail is invalid when either natural dimension exceeds 4096px or total pixels exceed 16,777,216.
- `DSC_2446.JPG` dimensions 6048x4032 map to a clear field error before submit.
- A non-square image within the bounds remains valid while `TRACK_THUMBNAIL_SQUARE_REQUIRED` is false.
- The size check remains active if the square flag is later restored to true; do not couple the two policies.
- JPEG/PNG, 10MB, decode failure, stale load protection, object URL cleanup, current-image warning, and square cover preview behavior remain intact.
- Guidance does not imply that 1:1 is currently required and does not describe 2048x2048 as the only recommended shape.
- Focused tests, TypeScript typecheck, ESLint, Prettier, and diff check pass.
Constraints/Forbidden:
- Modify only `C:\Users\jm991\AppData\Local\ATStudio\worktrees\v1-client-acceptance-20260817` on `codex/v1-client-acceptance-20260817`.
- Do not modify or run services from `C:\Users\jm991\Desktop\project\ATStudio`.
- Do not change backend limits or error codes, restart the backend, mutate DB/storage/public data, or restart frontend/tunnel processes.
- Do not remove the temporary square-policy flag; it must remain an independent restoration switch.
- Preserve the existing callback/ref and object-URL lifecycle pattern; add no extra decode, request, or dependency.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] 6048x4032 and any image above either backend bound display a specific Korean resolution error and keep submit disabled.
- [ ] Images at the exact accepted boundary and ordinary non-square images remain valid.
- [ ] The resolution check executes independently of the square check so both can be active in the future.
- [ ] Create and edit pages inherit the shared field behavior without backend changes.
Performance:
- [ ] Reuse the dimensions from the existing image load event; add no second decode or network request.
Quality:
- [ ] Focused Vitest suites pass.
- [ ] TypeScript typecheck passes.
- [ ] ESLint passes with no warnings or errors.
- [ ] Changed frontend files pass Prettier check.
- [ ] `git diff --check` passes.

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

REQ/Context Docs:
- deliverables/user/REQ-20260817-ATS-001.md
- deliverables/user/WI-20260817-ATS-002-summary.md
- deliverables/agent/WI-20260817-ATS-002-evidence-pack.md
- docs/SR/SR-98.md

Files:
- frontend/src/pages/creator/TrackThumbnailField.tsx
- frontend/src/pages/creator/TrackThumbnailField.test.tsx
- frontend/src/pages/creator/TrackUploadPage.test.tsx
- frontend/src/pages/creator/TrackEditPage.test.tsx
- frontend/src/utils/validation.ts
- src/main/java/com/atstudio/atstudio/service/image/CanonicalImageService.java:35-36

Repro/Logs:
- Current public failure: backend `CanonicalImageService.validateBounds` rejects `DSC_2446.JPG` before square validation.
- Attached source dimensions: 6048x4032, 24,385,536 pixels, 7.815 MiB.

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260817-ATS-019-summary.md :
- Korean completion summary, exact message/limits, changed files, test results, risks, rollback, and next WI.
Agent-facing -> deliverables/agent/WI-20260817-ATS-019-evidence-pack.md :
- Evidence pointers, exact test commands/results, diff scope, reproducibility, performance check, and rollback.
Handoff Packet -> deliverables/agent/WI-20260817-ATS-019-handoff.md :
- This packet.

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required
Tests: Include focused component/page Vitest results plus typecheck, ESLint, Prettier, and `git diff --check`.
Rollback: Document how to remove only the frontend dimension guard/guidance while preserving the independent temporary square flag.

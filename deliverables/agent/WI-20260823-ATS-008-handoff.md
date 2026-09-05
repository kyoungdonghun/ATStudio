[WI HEADER]
WI ID: WI-20260823-ATS-008
REQ: REQ-20260823-ATS-001
Agent: se
Depends On: WI-20260823-ATS-007
Blocks: WI-20260823-ATS-009

[WI SUMMARY]
Why: Close the two residual conditions that prevent a clean frontend gate and real player acceptance evidence.
Scope (in/out): In: repair only the existing HomePage line-break-sensitive assertion; restore only currently missing files referenced by the ten scoped `AT.M Demo` tracks from `C:\Users\jm991\AppData\Local\ATStudio\development-catalog-20260817\public` to the current configured `uploads` root; verify exact filename parity before and stream/thumbnail success after. Out: source HomePage copy, database schema/row mutation/deletion, reseeding, client worktree, provider/mail/payment actions, and overwriting an existing target asset.
DoD: The intended hero copy remains asserted with whitespace-tolerant matching; all ten scoped tracks have HTTP-successful full/range streams and thumbnails; copied files are exact-key fixtures only; two-set evidence documents are created.
Constraints/Forbidden: Do not read or print secrets. Stop and report if a fixture key is missing, a target file already exists with a different hash, a track is outside the scoped `AT.M Demo` set, or runtime path cannot be proven. Do not restart a process unless required and ownership is verified.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] The HomePage hero subtitle test tolerates rendered line breaks but still rejects the old copy.
- [ ] The ten `AT.M Demo` DB track storage keys are enumerated using the local runtime or guarded local DB tooling.
- [ ] Only missing matching assets are copied from retained fixture storage.
- [ ] Each scoped stream accepts a byte-range request and each scoped thumbnail returns HTTP 200.
Quality:
- [ ] Focused HomePage test passes.
- [ ] `git diff --check` has no diagnostics.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 1 (Policies):
- docs/policies/quality-gates.md

Tier 2 (Tech Stack):
- .agents/skills/react-best-practices/AGENTS.md
- docs/standards/frontend-standards.md

REQ/Context Docs:
- deliverables/user/REQ-20260823-ATS-001.md
- deliverables/agent/WI-20260823-ATS-007-evidence-pack.md
- deliverables/user/WI-20260817-ATS-027-summary.md

Files:
- frontend/src/pages/public/HomePage.tsx
- frontend/src/pages/public/HomePage.test.tsx
- src/main/resources/application.yml
- src/main/java/com/atstudio/atstudio/controller/TrackController.java
- C:\Users\jm991\AppData\Local\ATStudio\development-catalog-20260817\public

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260823-ATS-008-summary.md:
- Summary, exact restored asset count, tests, risks
Agent-facing -> deliverables/agent/WI-20260823-ATS-008-evidence-pack.md:
- Key matching evidence, copied paths without secrets, HTTP/status verification, reproducible commands, rollback
Handoff Packet -> deliverables/agent/WI-20260823-ATS-008-handoff.md:
- This packet

[TRACEABILITY REQUIREMENTS]
Evidence pointers: Required
Tests: Focused HomePage test; track full/range stream and thumbnail HTTP checks
Rollback: Remove only the files copied in this WI after confirming no current DB row was changed; do not remove any pre-existing asset.

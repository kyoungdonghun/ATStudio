[WI HEADER]
WI ID: WI-20260715-ATS-024
REQ: REQ-20260715-ATS-001
Agent: se
Depends On: WI-20260715-ATS-018, WI-20260715-ATS-019, WI-20260715-ATS-022
Blocks: WI-20260715-ATS-023 final browser evidence

[WI SUMMARY]
Why: Close the browser-only regression found after the first stable checkpoint: duplicate `Content-Range` headers prevent HTML audio playback, and the Track detail action still calls full listening a preview.
Scope (in/out): In: Track stream partial-response headers, focused controller regression tests, Track detail listening label, focused frontend tests if needed. Out: download/subscription policy, audio duration metadata correction, transcoding, preview generation, player redesign, database/data changes, broad audit remediation.
DoD: A valid Range response contains exactly one standards-compliant `Content-Range`; full audio starts in a real browser; current time/progress advances; the Track detail action says play/pause without preview terminology; official download protections remain unchanged.
Constraints/Forbidden: Preserve public full-length listening and protected official download as approved. Do not add playback limits, preview semantics, new media libraries, schema changes, or unrelated refactors. You are not alone in the codebase; do not revert unrelated edits and accommodate concurrent runtime work.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] `GET /api/tracks/{id}/stream` with one valid Range returns `206` with exactly one `Content-Range` header.
- [ ] No-Range and open-ended Range still address the complete stored resource.
- [ ] The real browser can start playback and current time advances.
- [ ] Track detail uses `재생` / `일시정지`, never `미리 듣기`.
- [ ] Download authorization, quota, history, and license paths are untouched.
Quality:
- [ ] Focused backend controller tests cover header multiplicity and existing Range behavior.
- [ ] Focused frontend tests or a stable source assertion cover the label.
- [ ] Backend focused tests, frontend typecheck/ESLint/tests, and diff check pass for changed files.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md
- docs/standards/development-standards.md
Tier 1 (Policies):
- docs/policies/quality-gates.md
Tier 2 (Tech Stack / Context):
- .agents/skills/react-best-practices/AGENTS.md
- deliverables/user/REQ-20260715-ATS-001.md
- deliverables/agent/WI-20260715-ATS-018-evidence-pack.md
- deliverables/agent/WI-20260715-ATS-019-evidence-pack.md
- deliverables/agent/WI-20260715-ATS-022-evidence-pack.md
- docs/design/api-spec.md
- docs/design/usecase/sound-track.md
Files:
- src/main/java/com/atstudio/atstudio/controller/TrackController.java
- src/test/java/com/atstudio/atstudio/controller/TrackControllerTest.java
- frontend/src/pages/public/TrackDetailPage.tsx
- matching focused frontend test file, only if required
Repro/Logs:
- `curl.exe -sS -D - -o NUL -r 0-1023 http://127.0.0.1:8080/api/tracks/2/stream` showed duplicate `Content-Range` lines.
- Browser Track detail loaded the full resource metadata but `audio.play()` rejected and the player remained at `0:00`.

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260715-ATS-024-summary.md
Agent-facing -> deliverables/agent/WI-20260715-ATS-024-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260715-ATS-024-handoff.md

[TRACEABILITY REQUIREMENTS]
Evidence pointers: Required for response header construction, UI label, and unchanged download boundary.
Tests: Record exact Gradle/npm/curl/browser commands and outcomes.
Rollback: Describe the minimal controller/UI rollback without changing data or storage.

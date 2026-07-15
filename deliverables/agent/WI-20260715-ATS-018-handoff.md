[WI HEADER]
WI ID: WI-20260715-ATS-018
REQ: REQ-20260715-ATS-001
Agent: se
Depends On: -
Blocks: WI-20260715-ATS-021, WI-20260715-ATS-022

[WI SUMMARY]
Why: Restore the approved public full-track listening policy without reopening direct original-file exposure or weakening protected downloads.
Scope (in/out): In: Track stream service/controller behavior, focused backend tests, Range semantics. Out: frontend, docs, transcoding, storage migration, download policy changes.
DoD: Full original resource is streamed through the controller with valid Range handling; bounded-prefix logic is removed; static original paths and public storage keys stay denied; download entitlement and daily limits regressions pass.
Constraints/Forbidden: Do not add preview generation, FFmpeg, schema/data changes, public original paths, or relax download authorization. You are not alone in the codebase; do not revert unrelated edits and accommodate concurrent changes.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Public stream exposes the full valid audio resource through the controller.
- [ ] Range start/end/open-ended/suffix requests use the full resource length.
- [ ] Direct static original retrieval and public DTO storage-key exposure remain denied.
- [ ] Subscriber-only download, daily limit, ledger, and license behavior are unchanged.
Quality:
- [ ] Focused Track controller/service/security/download tests pass.
- [ ] Java compile succeeds.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md
- docs/standards/development-standards.md
Tier 1 (Policies):
- docs/policies/security-policy.md
- docs/policies/quality-gates.md
Tier 2 (Context):
- deliverables/user/REQ-20260715-ATS-001.md
- docs/design/api-spec.md
- docs/design/usecase/sound-track.md
- docs/design/p0-release-blocker-remediation-design.md
- docs/audit/p0-release-blocker-closure-20260713.md
Files:
- src/main/java/com/atstudio/atstudio/service/TrackService.java
- src/main/java/com/atstudio/atstudio/controller/TrackController.java
- src/main/java/com/atstudio/atstudio/config/SecurityConfig.java
- src/test/java/com/atstudio/atstudio/service/TrackServiceTest.java
- src/test/java/com/atstudio/atstudio/controller/TrackControllerTest.java
- src/test/java/com/atstudio/atstudio/config/SecurityFilterChainTest.java
- src/test/java/com/atstudio/atstudio/service/DownloadServiceTest.java
Repro/Logs:
- Browser observation: an 18:10 MP3 was exposed as a byte prefix interpreted as 0:12 and did not advance playback.

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260715-ATS-018-summary.md
Agent-facing -> deliverables/agent/WI-20260715-ATS-018-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260715-ATS-018-handoff.md

[TRACEABILITY REQUIREMENTS]
Evidence pointers: Required, including exact stream and test locations.
Tests: Record exact Gradle commands and outcomes.
Rollback: Explain how to restore the prior stream behavior without reverting unrelated work.

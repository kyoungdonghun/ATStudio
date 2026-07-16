[WI HEADER]
WI ID: WI-20260716-ATS-023
REQ: REQ-20260716-ATS-003
Agent: se
Depends On: -
Blocks: WI-20260716-ATS-024

[WI SUMMARY]
Why: Populate the current client demo with realistic, playable catalog density for screenshots and feature demonstrations.
Scope (in/out): Implement an idempotent seed and cleanup tool for QA Demo tags, tracks, audio files, and playlists; apply it only to the current client-demo runtime. Do not change product behavior, merge branches, or mutate unrelated data.
DoD: At least 30 active tracks, 30 tags, and 8-12 playlists are present; generated audio streams successfully; reruns are duplicate-safe; cleanup is scoped to QA Demo identifiers.
Constraints/Forbidden: Never print or persist credentials. Never delete existing non-demo data. Do not edit existing dirty files outside the assigned new seed-tool and deliverable paths. Do not commit or push.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Generate valid, varied sine-wave WAV files.
- [ ] Create or reuse 30+ uniquely identified demo tags.
- [ ] Create or reuse 30+ active demo tracks and associate multiple tags.
- [ ] Create or reuse 8-12 demo playlists with 5-12 tracks each.
- [ ] Provide idempotent seed and scoped cleanup modes.
Performance:
- [ ] Seed completes without interrupting the existing frontend, backend, or Cloudflare tunnel.
- [ ] Generated audio remains reasonably small for local demo storage.
Quality:
- [ ] Secrets are loaded only from the local runtime credential file and never emitted.
- [ ] Existing record counts and identifiers are captured before and after application.
- [ ] Script syntax and dry-run behavior are verified.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 1 (Policies - Inferred):
- docs/policies/security-policy.md
- docs/policies/quality-gates.md

REQ/Context Docs:
- deliverables/user/REQ-20260716-ATS-003.md
- docs/design/api-spec.md
- docs/design/db-schema.md

Files:
- C:/Users/jm991/Desktop/project/ATStudio-client-demo-stable/src/main/java/com/atstudio/atstudio/controller/TrackController.java
- C:/Users/jm991/Desktop/project/ATStudio-client-demo-stable/src/main/java/com/atstudio/atstudio/controller/TagController.java
- C:/Users/jm991/Desktop/project/ATStudio-client-demo-stable/src/main/java/com/atstudio/atstudio/controller/PlaylistController.java
- C:/Users/jm991/Desktop/project/ATStudio-client-demo-stable/src/main/java/com/atstudio/atstudio/bootstrap/TestUserBootstrapRunner.java
- C:/Users/jm991/AppData/Local/ATStudio/acceptance-preview-64db91c/backend-environment-credentials.json

Repro/Logs:
- GET http://127.0.0.1:8080/api/tracks?page=0&size=100
- GET http://127.0.0.1:8080/api/tags

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260716-ATS-023-summary.md:
- Summary, applied counts, risks, and rollback command.
Agent-facing -> deliverables/agent/WI-20260716-ATS-023-evidence-pack.md:
- Evidence pointers, patch notes, API results, idempotency evidence, and follow-up WI.
Handoff Packet -> deliverables/agent/WI-20260716-ATS-023-handoff.md:
- This packet.

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required
Tests: PowerShell syntax/dry-run plus live API verification
Rollback: Document the scoped cleanup command and data identifier convention

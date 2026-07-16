[WI HEADER]
WI ID: WI-20260716-ATS-009
REQ: REQ-20260716-ATS-002
Agent: se
Depends On: WI-20260716-ATS-004
Blocks: WI-20260716-ATS-012, WI-20260716-ATS-013, WI-20260716-ATS-015, WI-20260716-ATS-016

[WI SUMMARY]
Why: Close the remaining typed-OAuth, social-account consistency, album sorting, playlist mutation, and download quota race defects without changing ATStudio product policy.
Scope (in):
- Replace raw/unvalidated OAuth provider token and user-info response handling with typed provider-specific DTOs or equivalent strict parsers. Validate all required fields as non-null/non-blank and map malformed/provider-error payloads to stable business errors without logging tokens, authorization codes, verifier values, provider IDs, or raw response bodies.
- Preserve current Google/Kakao/Naver login, PKCE, profile-completion, and token issuance behavior while adding focused malformed-response and happy-path tests.
- Correct album `trackCount` sorting so it is global across the full filtered result rather than sorting only one already-paged slice. Keep pagination bounded and deterministic.
- Serialize playlist create-limit checks and playlist/album track order mutations under cooperating writes. Prevent duplicate order positions, duplicate membership, or lost updates from concurrent create/add/batch/reorder requests using the least intrusive repository/entity/schema invariant consistent with MySQL 8 and current architecture.
- Serialize first-time downloads per user and enforce the existing daily subscription quota atomically. Concurrent requests must not create duplicate licenses, bypass daily limits, or increment download count more than once. Re-download behavior and ADMIN policy remain unchanged.
- Align affected backend tests, fresh schema/manual additive patch sources if required, and canonical API/database/use-case documents with the implemented contract.
- Social-only account withdrawal is a conditional sub-scope: do not implement or document a new withdrawal authentication mechanism until the user approves provider reauthentication and linked-provider-ID matching. Existing password-account withdrawal must remain unchanged.
Scope (out):
- Public listening restrictions, preview-only playback, subscription/download quota policy changes, ADMIN policy changes, payment behavior, provider additions, multi-server locking, distributed queues, PostgreSQL, or client-demo propagation.
- Calling live OAuth providers, mutating real users/downloads/playlists/albums, executing DDL, rotating secrets, or reading/logging secret values.
- Implementing social-only withdrawal by skipping reauthentication, trusting an email alone, or silently assigning a password.
DoD:
- OAuth malformed-response handling, global album sorting, playlist/album mutation serialization, and first-download quota/license atomicity are covered by focused automated tests.
- Existing product behavior remains intact; affected canonical docs match code and validation gates pass.
- Social-only withdrawal is either implemented after explicit approval with focused security tests or reported as a named unresolved policy blocker without weakening current authentication.
Constraints/Forbidden:
- Work only in `codex/p1-acceptance-hardening` under `C:\Users\jm991\Desktop\project\ATStudio`.
- Do not modify, switch, merge, restart, or propagate to `codex/client-demo-stable` or its Cloudflare-backed runtime.
- Preserve WI-005 through WI-007 and unrelated dirty-worktree changes; do not revert others' edits.
- Use stable business errors and DTO/entity separation; avoid raw provider payloads in exceptions, logs, tests, and evidence.
- Any new DDL must be source-only, additive, aligned in `schema.sql` and a dated manual patch, and explicitly marked not executed.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] OAuth token/user-info payloads are typed or strictly parsed and reject missing, blank, wrong-type, or provider-error required fields without issuing a local session.
- [ ] Existing Google/Kakao/Naver happy paths and PKCE behavior remain compatible.
- [ ] `trackCount` sort produces the same global order regardless of requested page and uses a deterministic secondary key.
- [ ] Concurrent playlist creation cannot exceed the active plan limit.
- [ ] Concurrent playlist/album track mutations preserve unique membership and deterministic, collision-free order.
- [ ] Concurrent first downloads cannot exceed the daily quota, create duplicate user-track licenses, or increment the track download count more than once.
- [ ] Existing licensed re-download remains quota-free and public full-track listening remains unchanged.
- [ ] Social-only withdrawal remains unchanged until explicit approval; if approved during this WI, fresh provider reauthentication must match the linked provider identity before withdrawal.
Performance:
- [ ] Album sorting/pagination is bounded and does not load an unbounded catalog into memory.
- [ ] Lock acquisition uses deterministic narrow scopes and does not introduce full-table locks or multi-server infrastructure.
Quality:
- [ ] Focused OAuth, album, playlist, download, repository/schema contract, and withdrawal tests pass.
- [ ] Relevant full backend regression subset, documentation validation, and `git diff --check` pass.
- [ ] Real-provider and retained-MySQL proof is not claimed without an approved environment run.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md
- docs/standards/development-standards.md
- docs/standards/documentation-standards.md
- docs/standards/glossary.md

Tier 1 (Policies):
- docs/policies/security-policy.md
- docs/policies/quality-gates.md
- docs/policies/access-control-policy.md

Tier 2 (Domain / Approved Design):
- docs/design/remaining-remediation-design-20260716.md
- docs/design/api-spec.md
- docs/design/db-schema.md
- docs/design/usecase/user-info.md
- docs/design/usecase/sound-album.md
- docs/design/usecase/sound-playlist.md
- docs/design/usecase/download-queue.md
- docs/ui/screen-flow.md
- deliverables/user/REQ-20260716-ATS-002.md
- deliverables/agent/WI-20260711-ATS-008-evidence-pack.md

Files:
- src/main/java/com/atstudio/atstudio/service/auth/OAuth2Service.java
- src/main/java/com/atstudio/atstudio/service/auth/AuthService.java
- src/main/java/com/atstudio/atstudio/dto/auth/
- src/main/java/com/atstudio/atstudio/service/UserService.java
- src/main/java/com/atstudio/atstudio/controller/UserController.java
- src/main/java/com/atstudio/atstudio/dto/user/WithdrawRequest.java
- src/main/java/com/atstudio/atstudio/entity/SocialAccount.java
- src/main/java/com/atstudio/atstudio/repository/SocialAccountRepository.java
- src/main/java/com/atstudio/atstudio/service/AlbumService.java
- src/main/java/com/atstudio/atstudio/repository/AlbumRepository.java
- src/main/java/com/atstudio/atstudio/repository/AlbumTrackRepository.java
- src/main/java/com/atstudio/atstudio/service/PlaylistService.java
- src/main/java/com/atstudio/atstudio/repository/PlaylistRepository.java
- src/main/java/com/atstudio/atstudio/repository/PlaylistTrackRepository.java
- src/main/java/com/atstudio/atstudio/service/DownloadService.java
- src/main/java/com/atstudio/atstudio/repository/TrackDownloadRepository.java
- src/main/java/com/atstudio/atstudio/repository/LicenseRepository.java
- src/main/resources/schema.sql
- src/main/resources/db/manual/
- src/test/java/com/atstudio/atstudio/service/

Repro / Inspection:
- `rg -n "Map<String, Object>|access_token|providerId|trackCount|countByUserAndIsActiveTrue|countByAlbum|countByIdPlaylistId|countByUserAndDownloadedAtBetween|findByUserAndTrack" src/main/java src/test/java docs`
- Inspect existing unique keys, composite IDs, row-lock helpers, and current source-only manual-patch chain before choosing a concurrency fence.

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260716-ATS-009-summary.md:
- Behavior changes, unchanged product policy, approval-dependent social withdrawal status, tests, risks, and environment-only follow-ups.
Agent-facing -> deliverables/agent/WI-20260716-ATS-009-evidence-pack.md:
- Typed provider contracts, transaction/lock invariants, exact evidence pointers, schema/API/doc patch notes, reproducible commands/results, rollback, and unresolved policy/environment items.
Handoff Packet -> deliverables/agent/WI-20260716-ATS-009-handoff.md:
- This packet.

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required.
Tests: Include exact commands and result counts. Concurrency tests must prove the chosen lock/constraint contract, not just sequential success.
Rollback: Revert source/schema/manual-patch/docs together without deleting existing user, playlist, album, license, or download records.
Environment boundary: Mark real OAuth-provider payload compatibility and retained-MySQL DDL/lock behavior as `ENVIRONMENT-CONDITIONAL` unless actually proven in an approved environment.

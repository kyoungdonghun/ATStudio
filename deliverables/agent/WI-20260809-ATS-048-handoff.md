[WI HEADER]
WI ID: WI-20260809-ATS-048
REQ: REQ-20260809-ATS-001
Agent: se
Depends On: WI-20260809-ATS-039, WI-20260809-ATS-047
Blocks: WI-20260809-ATS-049 and WI-20260809-ATS-053

[WI SUMMARY]
Why: Close the confirmed Track form/route/management defects and let an ADMIN understand the Track-association impact before deleting a Tag.
Scope (in): Track upload/edit accepted audio contract; Track edit required metadata and explicit empty-Tag intent; Track edit canonical route ID; Track management canonical URL/search/delete/retry state excluding generic latest-request ownership; ADMIN-only Tag deletion-impact read contract and used/unused confirmation; focused backend/frontend tests; Track/Tag use-case and API current-state documentation.
Scope (out): CR-031-054 Track retention/deletion policy; Album/Notice edit-ID slices; Track public catalog behavior; audio analyzer format expansion; TrackManage generic latest-request ownership (Track slice of CR-031-096 remains WI-053); accessibility/keyboard/localization-only work assigned to WI-058/059; real data deletion/UAT; schema change; provider/mail/download effects.
DoD: MP3/WAV is the only advertised and client-accepted audio replacement/upload contract; Track edit cannot submit blank required metadata and can intentionally replace all Tags with an empty set without changing omitted/preserve semantics for other callers; malformed Track edit IDs issue no API calls and render bounded recovery; Track management normalizes URL state, synchronizes draft/applied search, offers retry, keeps delete errors with the target, blocks conflicting delete controls, awaits authoritative refresh, and preserves modal recovery; Tag deletion opens only after an ADMIN-only impact read and distinguishes zero versus N Track associations with explicit removal wording; docs and focused/full gates pass.
Constraints/Forbidden: Preserve existing backend MP3/WAV analyzer, Track public projection, filter semantics, and CR-031-054 deletion behavior. Prefer an explicit multipart intent flag over relying on ambiguous empty-string collection binding. The deletion-impact response must be bounded and contain no Track titles/paths/PII. No schema/data mutation, destructive fixture, external side effect, branch operation, protected-output access, secret inspection, dependency/library change, or architecture expansion.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Shared frontend audio validation and both Track forms accept/name only `.mp3` and `.wav` plus the matching MIME hints; rejected selections reset the input for same-file retry.
- [ ] Track edit validates nonblank title, positive in-range BPM, and nonblank tonality before request construction; description remains explicitly clearable.
- [ ] Track edit always sends explicit Tag replacement intent. An empty selection removes all TrackTag associations; omitted intent still preserves associations for non-UI partial callers.
- [ ] Backend tests prove omitted/preserve, explicit empty/clear, and explicit nonempty/replace Tag semantics.
- [ ] Track edit parses one canonical finite positive safe integer ID and reuses it for read/update; invalid or missing IDs make zero Track/Tag API calls and show safe navigation.
- [ ] Track management canonicalizes invalid/beyond-basic URL inputs without emitting malformed requests, keeps applied URL keyword and draft input synchronized across browser navigation, and owns load versus delete errors separately.
- [ ] Track delete failure keeps the target modal and retry available; pending blocks close/retarget/duplicate execution; success awaits a canonical list refresh before clearing the target.
- [ ] The WI does not implement Track list latest-request ownership; its overlap with CR-031-096 is explicitly retained for WI-053.
- [ ] ADMIN Tag deletion impact returns only Tag identity plus nonnegative association count from an authoritative repository count.
- [ ] Tag delete confirmation says an unused Tag has no Track association to remove, while a used Tag states the exact count and that all those associations will be removed before the Tag is deleted.
- [ ] Impact read failure does not expose an uninformed destructive confirm; retry or safe close remains available.
- [ ] Existing Tag canonicalization/duplicate/conflict behavior and actual delete ordering are preserved.
Quality:
- [ ] Dedicated or focused frontend tests cover MP3/WAV, rejected formats, empty Tags, blank required metadata, invalid IDs, URL/back navigation, load retry, delete failure/retry/pending/success refresh, and used/unused/impact-failure Tag deletion.
- [ ] Backend focused tests cover Track Tag intent and Tag impact count/controller authorization/response without performing real deletion fixtures.
- [ ] Typecheck, ESLint zero warnings, Prettier, production build, backend tests/JaCoCo verification, docs validation, and diff checks pass before closure.
- [ ] Independent QA inspects counterexamples and returns PASS with no P0-P2 before commit.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md

Tier 0 (Standards - Assignee se):
- docs/standards/development-standards.md

Tier 1 (Policies):
- docs/policies/quality-gates.md
- docs/policies/access-control-policy.md
- docs/policies/security-policy.md

Tier 2 (Frontend and domain contracts):
- docs/standards/frontend-standards.md
- docs/design/usecase/sound-track.md
- docs/design/usecase/sound-tag.md
- docs/design/api-spec.md

REQ and finding context:
- deliverables/user/REQ-20260809-ATS-001.md
- deliverables/agent/WI-20260809-ATS-031-consolidated-findings.md:620-628
- deliverables/agent/WI-20260809-ATS-031-consolidated-findings.md:982-987
- deliverables/agent/WI-20260809-ATS-025-findings.md:55-73
- deliverables/agent/WI-20260809-ATS-025-findings.md:105-143
- deliverables/agent/WI-20260809-ATS-028-findings.md:152-161
- deliverables/agent/WI-20260809-ATS-039-evidence-pack.md

Frontend files:
- frontend/src/utils/validation.ts
- frontend/src/pages/creator/TrackUploadPage.tsx
- frontend/src/pages/creator/TrackUploadPage.test.tsx
- frontend/src/pages/creator/TrackEditPage.tsx
- frontend/src/pages/creator/TrackEditPage.test.tsx
- frontend/src/pages/admin/TrackManagePage.tsx
- frontend/src/pages/admin/TrackManagePage.module.css
- frontend/src/pages/admin/TagManagePage.tsx
- frontend/src/pages/admin/TagManagePage.test.tsx
- frontend/src/api/tracks.ts
- frontend/src/api/tags.ts
- frontend/src/api/domainApis.test.ts
- frontend/src/types/index.ts
- frontend/src/utils/routeId.ts
- frontend/src/test/coverage/adminSubscriberPages.coverage.test.tsx

Backend files:
- src/main/java/com/atstudio/atstudio/controller/TrackController.java
- src/main/java/com/atstudio/atstudio/controller/TagController.java
- src/main/java/com/atstudio/atstudio/dto/track/TrackUpdateRequest.java
- src/main/java/com/atstudio/atstudio/dto/tag/TagResponse.java
- src/main/java/com/atstudio/atstudio/entity/Track.java
- src/main/java/com/atstudio/atstudio/service/TrackService.java
- src/main/java/com/atstudio/atstudio/service/TagService.java
- src/main/java/com/atstudio/atstudio/repository/TrackTagRepository.java
- src/test/java/com/atstudio/atstudio/controller/TrackControllerTest.java
- src/test/java/com/atstudio/atstudio/controller/TagControllerTest.java
- src/test/java/com/atstudio/atstudio/service/TrackServiceTest.java
- src/test/java/com/atstudio/atstudio/service/TagServiceTest.java
- src/test/java/com/atstudio/atstudio/service/TagServiceBranchCoverageTest.java

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260809-ATS-048-summary.md after QA/gates:
- Korean summary, behavior, test evidence, residual/deferred boundaries, rollback.
Agent-facing -> deliverables/agent/WI-20260809-ATS-048-evidence-pack.md after QA/gates:
- Exact evidence pointers, response/request contracts, red/green tests, commands/results, risks, rollback, CR disposition.
Handoff Packet -> deliverables/agent/WI-20260809-ATS-048-handoff.md:
- This packet.

[TRACEABILITY REQUIREMENTS]
- Map CR-031-056, CR-031-057, Track slice of CR-031-061, bounded non-latest-request portion of CR-031-063, and CR-031-064 separately.
- State explicitly that Album/Notice ID slices and Track latest-request ownership remain in their assigned later WIs; do not over-close their canonical roots.
- Preserve red/green evidence for explicit empty Tags, malformed Track ID, delete retry/refresh, and used/unused Tag impact.
- Document exact new/changed API fields and why no schema migration is required.
- Record that no real Track or Tag was deleted and protected output remained untouched.

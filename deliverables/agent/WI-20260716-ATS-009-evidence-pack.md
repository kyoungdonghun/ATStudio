# Evidence Pack: WI-20260716-ATS-009

## Summary (one-liner)

- Reviewed the OAuth/catalog/download integration contracts, corrected the remaining canonical documentation, and verified the focused backend suite without expanding behavior.

## Scope / DoD Check

- [x] Typed provider token/user-info contracts reject null, missing, blank, wrong-type, and provider-error required values as `SOCIAL_AUTH_FAILED` without issuing a local session.
- [x] Album `trackCount` uses database aggregate ordering before bounded pagination with deterministic secondary keys.
- [x] Playlist quota checks are serialized by the owning user lock; playlist and album membership/order mutations are serialized by their owning aggregate lock and validate full zero-based reorder membership.
- [x] First-download decisions use a user lock, `uq_licenses_user_track`, and one atomic cross-user Track count increment; licensed re-download behavior is unchanged.
- [x] Fresh-schema and retained-DB manual-patch sources declare the license uniqueness invariant; no patch was executed.
- [x] OAuth canonical documents now describe typed parsing, required values, `SOCIAL_AUTH_FAILED`, secret/raw-payload exclusion, and the real-provider environment boundary.
- [x] Social-only withdrawal remains an explicit `POLICY-PENDING` blocker; no authentication behavior was invented or implemented.
- [x] Focused tests, documentation validation, and `git diff --check` pass.

## Reference Documents (Tier 0-2)

**Injected Context** (from WI Handoff Packet):

| Tier | Document | Reason |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Constitution and communication/approval rules |
| 0 | `docs/standards/development-standards.md` | Backend transaction and test standards |
| 0 | `docs/standards/documentation-standards.md` | Canonical-document and traceability standards |
| 0 | `docs/standards/glossary.md` | Canonical domain terminology |
| 1 | `docs/policies/security-policy.md` | OAuth secret and provider-response handling boundary |
| 1 | `docs/policies/quality-gates.md` | Verification gates |
| 1 | `docs/policies/access-control-policy.md` | Approved execution boundary |
| 2 | `docs/design/remaining-remediation-design-20260716.md` | Approved remaining-remediation constraints |
| 2 | `docs/design/api-spec.md` | OAuth, playlist, album, and download API contract |
| 2 | `docs/design/db-schema.md` | License invariant and retained-DB boundary |
| 2 | `docs/design/usecase/user-info.md` | Social login and withdrawal use cases |
| 2 | `docs/design/usecase/sound-album.md` | Album ordering use cases |
| 2 | `docs/design/usecase/sound-playlist.md` | Playlist serialization and reorder use cases |
| 2 | `docs/design/usecase/download-queue.md` | Download-queue and official-download boundary |
| 2 | `docs/ui/screen-flow.md` | Existing UI contract boundary |
| 2 | `deliverables/user/REQ-20260716-ATS-002.md` | Approved requirement scope |
| 2 | `deliverables/agent/WI-20260711-ATS-008-evidence-pack.md` | Related prior evidence |

**Injection Rules Applied**:

- Rule source: `AGENTS.md` and `deliverables/agent/WI-20260716-ATS-009-handoff.md`.
- Assignee context: integration review / QA integration.
- Task type: review, testing, documentation, security-sensitive OAuth contract review.

## Evidence Pointers (required)

- Reviewed implementation (not modified by this integration pass):
  - `src/main/java/com/atstudio/atstudio/service/auth/OAuth2Service.java:218` maps provider transport and strict-parser failures to `SOCIAL_AUTH_FAILED`; typed provider records start at `:279`.
  - `src/test/java/com/atstudio/atstudio/service/auth/OAuth2ServiceTest.java:283` through `:314` covers blank token, provider error, wrong-type identity, and malformed Naver response cases.
  - `src/main/java/com/atstudio/atstudio/service/AlbumService.java:76` through `:93` selects the database aggregate page before response mapping; `:149`, `:177`, and `:191` acquire the album lock for membership/order writes.
  - `src/main/java/com/atstudio/atstudio/repository/AlbumRepository.java:23` through `:28` orders active albums by `COUNT(album_tracks) DESC, createdAt DESC, id DESC` before pagination.
  - `src/main/java/com/atstudio/atstudio/service/PlaylistService.java:49` and `:288` through `:292` lock the user for plan-limit creation; `:124`, `:154`, `:223`, and `:241` lock the target playlist for writes; `:324` through `:350` validates complete membership and contiguous orders.
  - `src/main/java/com/atstudio/atstudio/service/DownloadService.java:42` through `:80` locks the user before first-download checks and executes the atomic Track increment after successful ledger/license writes.
  - `src/main/java/com/atstudio/atstudio/repository/TrackRepository.java:33` through `:35` defines `downloadCount = downloadCount + 1` as a direct modifying query.
  - `src/main/resources/schema.sql:455` declares `uq_licenses_user_track`; `src/main/resources/db/manual/20260716_download_atomicity.sql:6` through `:61` preflights duplicates and conditionally adds the same unique key.
- Integration review conclusion:
  - The WI-009 write paths do not acquire user and playlist/album pessimistic locks in inverse order. Playlist creation uses only the user lock; playlist and album mutation paths use only their aggregate lock. Download locks the user and does not acquire a Track row lock before its atomic update.
- Files changed by this integration pass:
  - `docs/design/api-spec.md`: OAuth typed-response/failure and social-withdrawal policy boundary.
  - `docs/design/usecase/user-info.md`: OAuth flow and `POLICY-PENDING` withdrawal alignment.
  - `docs/design/usecase/sound-album.md`: corrected zero-based add-order wording.
  - `deliverables/user/WI-20260716-ATS-009-summary.md`: user-facing closure report.
  - `deliverables/agent/WI-20260716-ATS-009-evidence-pack.md`: this evidence pack.

## Commands & Outputs

- Commands executed:
  - `gradlew.bat test --rerun-tasks --tests "com.atstudio.atstudio.service.auth.OAuth2ServiceTest" --tests "com.atstudio.atstudio.service.AlbumServiceTest" --tests "com.atstudio.atstudio.service.PlaylistServiceTest" --tests "com.atstudio.atstudio.service.DownloadServiceTest" --tests "com.atstudio.atstudio.service.DownloadConcurrencyContractTest" --tests "com.atstudio.atstudio.service.LicenseServiceTest"`
  - `python .agents/skills/validate-docs/scripts/validate_docs.py`
  - `git diff --check`
- Outputs:
  - Gradle: `BUILD SUCCESSFUL` in 17 seconds; compilation and test tasks executed.
  - Documentation validator: all Tier 0 documents, internal links, 401 supported traceability IDs, and document-index checks passed.
  - Diff check: exit 0; no whitespace errors.

## Tests

- Focused forced backend suite: 68 tests, 0 failures, 0 errors, 0 skipped.
  - `OAuth2ServiceTest`: 14 tests, including nested malformed-response cases.
  - `AlbumServiceTest`: 12 tests.
  - `PlaylistServiceTest`: 23 tests.
  - `DownloadServiceTest`: 10 tests.
  - `DownloadConcurrencyContractTest`: 3 tests.
  - `LicenseServiceTest`: 6 tests.

## Risks / Rollback

- Risks:
  - `POLICY-PENDING`: social-only withdrawal is not implemented. Existing password-only withdrawal remains. User approval is required for fresh provider reauthentication plus linked provider-ID matching.
  - `ENVIRONMENT-CONDITIONAL`: real Google/Kakao/Naver response compatibility and retained-MySQL lock behavior are not proven by mocked/H2-focused tests.
  - `ENVIRONMENT-CONDITIONAL`: `20260716_download_atomicity.sql` has not been rehearsed on copied retained data; duplicate rows must be resolved before its unique-key DDL can be applied.
- Rollback:
  - Revert the WI-009 OAuth/catalog/download source, test, schema/manual-patch, and canonical-document changes together; do not delete existing users, playlists, albums, licenses, or download records.
  - Reverting this integration pass alone means reverting the three documentation files and the two WI-009 deliverables above; no data or runtime action is required.

## Follow-ups

- Run approved real-provider compatibility checks without recording secrets or raw payloads.
- Rehearse the retained-MySQL manual patch and lock/duplicate preflight on a copied environment.
- Do not implement social-only withdrawal until explicit user approval defines provider reauthentication and linked provider-ID matching.

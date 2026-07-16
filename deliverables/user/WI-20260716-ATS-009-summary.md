# WI-20260716-ATS-009 Summary

## Outcome

Completed the focused integration review and canonical-document alignment for typed OAuth handling, album catalog ordering, playlist mutation serialization, and first-download atomicity. No product policy was broadened and no integration code change was required beyond documentation alignment.

## Verified Contract

- OAuth provider token and user-info handling uses typed provider-specific responses rather than a raw map. Required values fail closed as `SOCIAL_AUTH_FAILED`; no session is issued for malformed or provider-error responses.
- Album `trackCount` sorting is globally ordered before bounded pagination, with deterministic `createdAt DESC, id DESC` secondary ordering.
- Playlist creation locks the owning user row for quota evaluation. Playlist membership, batch-add, remove, and reorder lock the target playlist row; reorder requests require every current member exactly once and contiguous zero-based orders.
- Album add, remove, and reorder lock the target album row. Album reorder requires complete membership and contiguous zero-based orders; add uses the current zero-based count as the next order.
- First downloads lock the owning user row, retain the `licenses(user_id, track_id)` unique invariant, and use one atomic Track download-count update. Licensed re-downloads, ADMIN behavior, and public full-track listening remain unchanged.

## Canonical Documents Updated

- `docs/design/api-spec.md`: documented typed OAuth response rules, `SOCIAL_AUTH_FAILED`, synthetic-only evidence, environment boundary, and the pending social-only withdrawal decision.
- `docs/design/usecase/user-info.md`: aligned the social login and withdrawal flows with the strict provider contract and `POLICY-PENDING` boundary.
- `docs/design/usecase/sound-album.md`: corrected album-add order from `current track count + 1` to the current zero-based count.

## Verification

- `gradlew.bat test --rerun-tasks --tests "com.atstudio.atstudio.service.auth.OAuth2ServiceTest" --tests "com.atstudio.atstudio.service.AlbumServiceTest" --tests "com.atstudio.atstudio.service.PlaylistServiceTest" --tests "com.atstudio.atstudio.service.DownloadServiceTest" --tests "com.atstudio.atstudio.service.DownloadConcurrencyContractTest" --tests "com.atstudio.atstudio.service.LicenseServiceTest"`: passed, 68 tests, 0 failures, 0 errors, 0 skipped.
- `python .agents/skills/validate-docs/scripts/validate_docs.py`: passed; Tier 0, links, 401 traceability IDs, and document index valid.
- `git diff --check`: passed.

## Risks and Approval Boundary

- `POLICY-PENDING`: social-only withdrawal is not implemented. The existing password-only withdrawal flow remains unchanged. A future path requires explicit user approval of fresh provider reauthentication and linked provider-ID matching.
- `ENVIRONMENT-CONDITIONAL`: real Google/Kakao/Naver payload compatibility was not exercised; no live provider call was made.
- `ENVIRONMENT-CONDITIONAL`: retained-MySQL execution of `20260716_download_atomicity.sql`, duplicate cleanup, and real lock behavior were not performed. The patch remains source-only and no DDL or data mutation was run.

## Scope Preservation

All unrelated dirty WI changes were preserved. No client-demo branch/runtime action, DDL execution, data mutation, secret inspection, or real provider call occurred.

[EVIDENCE PACK -- WI-20260302-ATS-013]

## WI Metadata
- **WI ID**: WI-20260302-ATS-013
- **REQ**: REQ-20260302-ATS-012
- **Agent**: re
- **Commit Verified**: e7c6d7a (HEAD, master)

---

## Test Execution

| Item | Detail |
|------|--------|
| Command | `./gradlew clean test` |
| Result | BUILD SUCCESSFUL |
| Total Tests | 534 |
| Failures | 0 |
| Errors | 0 |
| Skipped | 0 |
| Timestamp | 2026-03-03T09:37:15 ~ 09:37:46 UTC |
| Duration | ~38s |
| XML Report Location | `build/test-results/test/TEST-*.xml` (71 files) |

---

## Per-WI Verification

| WI | Issue | 대상 테스트 클래스 / 메서드 | Tests | Result |
|----|-------|---------------------------|-------|--------|
| WI-007 | C-1 unlimited plan guard | DownloadServiceTest: `download() 성공 - 무제한 플랜(downloadPerDay=-1) todayCount=0`, `todayCount=1000`, `제한 플랜(downloadPerDay=5) todayCount=4` | 3 new + 6 existing = 9 total | PASS |
| WI-008 | M-9 OAuth2 null guard | OAuth2ServiceTest$TokenExchangeNullResponse: Google/Kakao/Naver 토큰 null | 3 | PASS |
| WI-008 | M-9 OAuth2 null guard | OAuth2ServiceTest$UserInfoNullResponse: Google/Kakao/Naver userInfo null, Kakao account null, Naver response null | 5 | PASS |
| WI-008 | C-2 soft-delete filter | UserServiceTest: `getUsers_excludesDeletedUsers()` | included in 13 total | PASS |
| WI-009 | M-3 RESOURCE_DUPLICATE 409 / INVALID_STATE_TRANSITION enum | ExceptionTest (parameterized) | included in 96 total | PASS |
| WI-010 | M-2 LicenseRepository @EntityGraph / M-4 PlaylistService batch count | LicenseServiceTest, PlaylistServiceTest | included in totals | PASS |
| WI-011 | M-5 TrackService.deleteTrack() junction cleanup | TrackServiceTest: `deleteTrack() - track_tags 레코드를 deactivate 전에 삭제` | 1 new + 12 existing = 13 total | PASS |
| WI-011 | M-10 PlaylistService.deletePlaylist() junction cleanup | PlaylistServiceTest: `deletePlaylist() - playlist_tracks 레코드를 deactivate 전에 삭제` | 1 new + 16 existing = 17 total | PASS |
| WI-012 | M-7 CompanyCertification state transition guard | entity.CompanyCertificationTest$ValidTransitions (4), $InvalidTransitions (5) | 9 | PASS |
| WI-012 | M-8 Question state transition guard | entity.QuestionTest$ValidTransitions (5), $InvalidTransitions (5) | 10 | PASS |
| WI-012 | M-6 findTopByUserOrderByCreatedAtDesc | CompanyCertificationServiceTest$GetMyStatus (2) | 2 | PASS |

---

## Full Test Class Breakdown (534 total)

| Test Class | Tests | Failures |
|------------|-------|----------|
| AtStudioApplicationTests | 1 | 0 |
| PageInfoTest | 5 | 0 |
| ResponseDTOTest | 6 | 0 |
| ExceptionTest (parameterized) | 96 | 0 |
| GlobalExceptionHandlerTest | 8 | 0 |
| CompanyCertificationControllerTest | 16 | 0 |
| DownloadQueueControllerTest | 6 | 0 |
| LicenseControllerTest | 5 | 0 |
| LikeControllerTest | 6 | 0 |
| NoticeControllerTest | 11 | 0 |
| PlayHistoryControllerTest | 6 | 0 |
| PlaylistControllerTest | 16 | 0 |
| QuestionControllerTest | 15 | 0 |
| SecurityFilterChainTest | 10 | 0 |
| SubscriptionControllerTest | 4 | 0 |
| TagControllerTest | 10 | 0 |
| TrackControllerTest | 11 | 0 |
| UserControllerTest | 9 | 0 |
| UserSubscriptionControllerTest | 20 | 0 |
| WhitelistChannelControllerTest | 12 | 0 |
| entity.CompanyCertificationTest$InvalidTransitions | 5 | 0 |
| entity.CompanyCertificationTest$ValidTransitions | 4 | 0 |
| EntityDefaultValueTest | 9 | 0 |
| entity.QuestionTest$InvalidTransitions | 5 | 0 |
| entity.QuestionTest$ValidTransitions | 5 | 0 |
| CompositeKeyEqualityTest | 7 | 0 |
| LikeRepositoryTest | 4 | 0 |
| TrackTagRepositoryTest | 6 | 0 |
| UserRepositoryTest | 4 | 0 |
| JwtTokenProviderTest | 7 | 0 |
| CompanyCertificationServiceTest$Apply | 5 | 0 |
| CompanyCertificationServiceTest$GetDetail | 2 | 0 |
| CompanyCertificationServiceTest$GetMyStatus | 2 | 0 |
| CompanyCertificationServiceTest$ListAll | 3 | 0 |
| CompanyCertificationServiceTest$ProcessReview | 4 | 0 |
| DownloadQueueServiceTest | 9 | 0 |
| DownloadServiceTest | 9 | 0 |
| LicenseServiceTest | 6 | 0 |
| LikeServiceTest | 6 | 0 |
| NoticeServiceTest | 12 | 0 |
| PlayHistoryServiceTest | 7 | 0 |
| PlaylistServiceTest | 17 | 0 |
| QuestionServiceTest$CreateAnswer | 4 | 0 |
| QuestionServiceTest$CreateQuestion | 2 | 0 |
| QuestionServiceTest$DeleteQuestion | 7 | 0 |
| QuestionServiceTest$DownloadAttachment | 3 | 0 |
| QuestionServiceTest$GetQuestion | 5 | 0 |
| QuestionServiceTest$GetQuestions | 3 | 0 |
| QuestionServiceTest$UpdateQuestionStatus | 2 | 0 |
| SubscriptionServiceTest$GetActiveSubscriptions | 4 | 0 |
| SubscriptionServiceTest$GetSubscription | 2 | 0 |
| TagServiceTest | 9 | 0 |
| TrackServiceTest | 13 | 0 |
| UserServiceTest | 13 | 0 |
| UserSubscriptionServiceTest$AdminCancel | 2 | 0 |
| UserSubscriptionServiceTest$AdminUpdate | 3 | 0 |
| UserSubscriptionServiceTest$ChangeSubscription | 3 | 0 |
| UserSubscriptionServiceTest$GetDetail | 2 | 0 |
| UserSubscriptionServiceTest$GetMySubscription | 2 | 0 |
| UserSubscriptionServiceTest$ListAll | 1 | 0 |
| UserSubscriptionServiceTest$SelfCancel | 2 | 0 |
| UserSubscriptionServiceTest$Subscribe | 5 | 0 |
| UtilServiceTest | 7 | 0 |
| WhitelistChannelServiceTest$DeleteChannel | 2 | 0 |
| WhitelistChannelServiceTest$GetMyChannels | 1 | 0 |
| WhitelistChannelServiceTest$RegisterChannel | 7 | 0 |
| WhitelistChannelServiceTest$UpdateChannel | 3 | 0 |
| AuthServiceTest | 7 | 0 |
| OAuth2ServiceTest$TokenExchangeNullResponse | 3 | 0 |
| OAuth2ServiceTest$UserInfoNullResponse | 5 | 0 |
| OAuth2ServiceTest | 1 | 0 |

---

## Issues Found

None.

---

## Acceptance Criteria Verification

| Criteria | Status |
|----------|--------|
| BUILD SUCCESSFUL | PASS |
| 0 failures, 0 errors | PASS |
| DownloadServiceTest: unlimitedPlan 신규 테스트 3건 PASS | PASS |
| OAuth2ServiceTest: null guard 테스트 8건 PASS | PASS |
| entity.CompanyCertificationTest: 상태전이 유효/무효 9건 PASS | PASS |
| entity.QuestionTest: 상태전이 유효/무효 10건 PASS | PASS |
| TrackServiceTest: deleteTrack track_tags 삭제 검증 PASS | PASS |
| PlaylistServiceTest: deletePlaylist playlist_tracks 삭제 검증 PASS | PASS |
| Total tests >= 530 (actual: 534) | PASS |

---

## Reproduction Steps

```bash
# 1. Verify commit
git log --oneline -1
# Expected: e7c6d7a fix: Backend 3차 수정 CRITICAL 2건 + MAJOR 11건 (REQ-20260302-ATS-012)

# 2. Run full regression
./gradlew clean test

# 3. Verify result
# Expected: BUILD SUCCESSFUL in ~38s, 534 tests, 0 failures

# 4. Check specific WI test XMLs
# build/test-results/test/TEST-com.atstudio.atstudio.service.DownloadServiceTest.xml (tests=9)
# build/test-results/test/TEST-com.atstudio.atstudio.service.auth.OAuth2ServiceTest$TokenExchangeNullResponse.xml (tests=3)
# build/test-results/test/TEST-com.atstudio.atstudio.service.auth.OAuth2ServiceTest$UserInfoNullResponse.xml (tests=5)
# build/test-results/test/TEST-com.atstudio.atstudio.entity.CompanyCertificationTest$ValidTransitions.xml (tests=4)
# build/test-results/test/TEST-com.atstudio.atstudio.entity.CompanyCertificationTest$InvalidTransitions.xml (tests=5)
# build/test-results/test/TEST-com.atstudio.atstudio.entity.QuestionTest$ValidTransitions.xml (tests=5)
# build/test-results/test/TEST-com.atstudio.atstudio.entity.QuestionTest$InvalidTransitions.xml (tests=5)
# build/test-results/test/TEST-com.atstudio.atstudio.service.TrackServiceTest.xml (tests=13)
# build/test-results/test/TEST-com.atstudio.atstudio.service.PlaylistServiceTest.xml (tests=17)
```

---

## Rollback

N/A (verification-only WI; no code changes made)

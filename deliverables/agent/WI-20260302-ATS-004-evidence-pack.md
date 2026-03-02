# WI-20260302-ATS-004 Evidence Pack — 전체 회귀 테스트

## 실행 명령
```
cd C:\Users\jm991\Desktop\project\ATStudio
gradlew.bat test --rerun-tasks
```

## 결과
- **BUILD SUCCESSFUL** | 494 tests | 0 failures | 0 errors | 34s

## 전체 테스트 클래스 결과 (64개, 모두 failures=0)

| 클래스 | tests | failures |
|--------|-------|----------|
| ExceptionTest | 94 | 0 |
| UserSubscriptionControllerTest | 20 | 0 |
| CompanyCertificationControllerTest | 16 | 0 |
| PlaylistControllerTest | 16 | 0 |
| QuestionControllerTest | 15 | 0 |
| NoticeServiceTest | 12 | 0 |
| TrackServiceTest | 12 | 0 |
| UserServiceTest | 12 | 0 |
| WhitelistChannelControllerTest | 12 | 0 |
| TrackControllerTest | 11 | 0 |
| NoticeControllerTest | 11 | 0 |
| SecurityFilterChainTest | 10 | 0 |
| DownloadQueueServiceTest | 9 | 0 |
| UserControllerTest | 9 | 0 |
| TagServiceTest | 9 | 0 |
| GlobalExceptionHandlerTest | 8 | 0 |
| PlayHistoryServiceTest | 7 | 0 |
| AuthServiceTest | 7 | 0 |
| JwtTokenProviderTest | 7 | 0 |
| QuestionServiceTest$DeleteQuestion | 7 | 0 |
| WhitelistChannelServiceTest$RegisterChannel | 7 | 0 |
| EntityDefaultValueTest | 9 | 0 |
| CompositeKeyEqualityTest | 7 | 0 |
| (나머지 41개 클래스) | — | 모두 0 |

## 핵심 검증 근거

**WI-001 Notice 소유권** — `NoticeService.java:90-97` validateNoticeOwnership() 존재 확인
**WI-001 TestController 삭제** — controller 디렉토리 16개 파일 중 TestController.java 미존재
**WI-002 URL 검증** — WhitelistChannelServiceTest: spoofed domain, malformed URL 거부 7건 PASS
**WI-002 RefreshToken 만료** — `AuthService.java:73-76` EXPIRED 분기, refresh_expiredToken PASS
**WI-003 @EntityGraph** — `TrackRepository.java:18-24` findAll/findByIdWithTags @EntityGraph 확인

## 재현 방법
```
cd C:\Users\jm991\Desktop\project\ATStudio && gradlew.bat test --rerun-tasks
# 기대: BUILD SUCCESSFUL, 494 tests, 0 failures
```
XML: `build/test-results/test/TEST-*.xml`
HTML: `build/reports/tests/test/index.html`

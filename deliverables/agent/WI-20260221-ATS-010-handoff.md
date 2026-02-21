[WI HEADER]
WI ID: WI-20260221-ATS-010
REQ: REQ-20260221-ATS-002
Agent: re
Depends On: WI-20260221-ATS-006, WI-20260221-ATS-007, WI-20260221-ATS-008, WI-20260221-ATS-009
Blocks: WI-20260221-ATS-012

[WI SUMMARY]
Why: Phase 1에서 구현된 신규 서비스들에 대한 Mockito 기반 단위 테스트 작성
Scope (in/out):
  In:
    - TagServiceTest: updateTag(성공, 중복이름), deleteTag(성공, 없는id)
    - UserServiceTest 추가: getUsers(키워드검색, userType필터), getUser(성공, 없는id), updateUserByAdmin(성공)
    - UtilServiceTest: getSubscriptionStatus(구독있음, 없음), getDownloadCount(구독있음, 없음), getUserType
    - PlayHistoryServiceTest: savePlayHistory(성공, track없음, user없음), getMyHistory, deleteHistory(선택, 전체)
    - LikeServiceTest: addLike(성공, 중복→409), getMyLikes, removeLike(성공, 없는것→404)
    - DownloadQueueServiceTest: addToQueue(성공, 중복→409), getMyQueue, removeFromQueue
    - NoticeServiceTest: createNotice, getNotices, getNotice, updateNotice, deleteNotice(없는것→404)
  Out:
    - 컨트롤러 테스트 (WI-011 담당)
    - 통합 테스트

DoD:
  - 모든 신규 서비스에 최소 3케이스 (정상, 에러1, 에러2)
  - @ExtendWith(MockitoExtension.class) 기반 순수 단위 테스트
  - ./gradlew test 전체 통과

[ACCEPTANCE CRITERIA]
Functional:
  - [ ] TagServiceTest: 4케이스 이상 (기존 4개 + update/delete 각 2개)
  - [ ] UserServiceTest: 기존 테스트 유지 + admin 3케이스 추가
  - [ ] UtilServiceTest: 6케이스 이상
  - [ ] PlayHistoryServiceTest: 6케이스 이상
  - [ ] LikeServiceTest: 5케이스 이상
  - [ ] DownloadQueueServiceTest: 5케이스 이상
  - [ ] NoticeServiceTest: 6케이스 이상
Quality:
  - [ ] ./gradlew test 전체 통과 (기존 209개 + 신규)
  - [ ] 테스트 클래스당 @DisplayName 명시

[INPUT POINTERS]
Tier 0:
  - docs/standards/core-principles.md
  - docs/standards/development-standards.md

REQ/Context:
  - deliverables/user/REQ-20260221-ATS-002.md

서비스 소스 (테스트 대상):
  - src/main/java/com/atstudio/atstudio/service/TagService.java
  - src/main/java/com/atstudio/atstudio/service/UserService.java
  - src/main/java/com/atstudio/atstudio/service/UtilService.java
  - src/main/java/com/atstudio/atstudio/service/PlayHistoryService.java
  - src/main/java/com/atstudio/atstudio/service/LikeService.java
  - src/main/java/com/atstudio/atstudio/service/DownloadQueueService.java
  - src/main/java/com/atstudio/atstudio/service/NoticeService.java

기존 테스트 패턴 참조:
  - src/test/java/com/atstudio/atstudio/service/TagServiceTest.java
  - src/test/java/com/atstudio/atstudio/service/DownloadServiceTest.java
  - src/test/java/com/atstudio/atstudio/service/LicenseServiceTest.java
  - src/test/java/com/atstudio/atstudio/service/UserServiceTest.java

[OUTPUT CONTRACT]
Agent-facing -> deliverables/agent/WI-20260221-ATS-010-evidence-pack.md
  - 작성한 테스트 파일 목록, 테스트 케이스 수, 실행 결과

[TRACEABILITY REQUIREMENTS]
Evidence: 생성한 테스트 파일 경로 + ./gradlew test 결과 (총 테스트 수, 실패 수)

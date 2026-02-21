[WI HEADER]
WI ID: WI-20260221-ATS-005
REQ: REQ-20260221-ATS-001
Agent: re
Depends On: WI-20260221-ATS-004 (Track Core CRUD 구현 완료)
Blocks: WI-20260221-ATS-008, WI-20260221-ATS-009 (cr 리뷰)

---

[WI SUMMARY]
Why: WI-003/004에서 Tag/Track CRUD 구현이 완료됐으나 테스트 케이스가 작성되지 않았다.
     Auth 시스템과 동일한 수준의 테스트 커버리지를 확보하여 REQ G3 품질 게이트를 통과한다.

Scope (in):
- TrackService 단위 테스트 (Mockito): createTrack, getTracks, getTrack, getStreamResource, updateTrack, deleteTrack
- TagService 단위 테스트 (Mockito): createTag (중복 검증 포함), getAllTags (type=null / type=GENRE 등)
- TrackRepository @DataJpaTest: JpaSpecificationExecutor 기반 동적 필터링 쿼리 검증
- TrackTagRepository @DataJpaTest: findAllWithTagByTrack, findAllWithTagByTrackIdIn, deleteAllByTrack

Scope (out):
- TrackController MockMvc 테스트 (SecurityFilterChain 통합 테스트 범위, 별도 WI)
- WI-005 Stream/Download 로직 테스트 (WI-005 미완료)
- 성능/부하 테스트

DoD:
- 신규 테스트 전체 통과 (`gradlew test`)
- 기존 163개 테스트 회귀 없음
- TrackService 핵심 6개 메서드 커버
- TagService 핵심 2개 메서드 커버 (type 필터 포함)
- TrackTagRepository 3개 쿼리 메서드 커버

Constraints/Forbidden:
- @DataJpaTest에는 반드시 @Import(JpaConfig.class) 추가 (JPA Auditing 누락 방지)
- H2 테스트: src/test/resources/application.yml에 spring.sql.init.mode: never 이미 설정됨 — 변경 금지
- @SpringBootTest 사용 금지 (Mockito 단위 테스트 원칙)
- StorageService는 Mock 처리 (실제 파일시스템 접근 금지)
- 기존 테스트 파일 수정 금지 (회귀 방지)

---

[ACCEPTANCE CRITERIA]
Functional:
- [ ] TrackServiceTest: createTrack 성공 (audioFile only / audioFile+thumbnail+tags 케이스)
- [ ] TrackServiceTest: createTrack 실패 — user not found (RESOURCE_NOT_FOUND)
- [ ] TrackServiceTest: getTracks — 빈 목록 반환
- [ ] TrackServiceTest: getTrack 성공 / TRACK_NOT_FOUND / is_active=false 시 TRACK_NOT_FOUND
- [ ] TrackServiceTest: updateTrack — 메타데이터만 / 파일 교체 / tagIds 교체
- [ ] TrackServiceTest: deleteTrack — isActive=false 로 변경
- [ ] TagServiceTest: createTag 성공
- [ ] TagServiceTest: createTag 실패 — TAG_NAME_DUPLICATED
- [ ] TagServiceTest: getAllTags(null) — 전체 반환
- [ ] TagServiceTest: getAllTags(TagType.GENRE) — type 필터 반환
- [ ] TrackTagRepositoryTest: findAllWithTagByTrack — JOIN FETCH 결과 검증
- [ ] TrackTagRepositoryTest: findAllWithTagByTrackIdIn — 복수 Track 배치 로딩 검증
- [ ] TrackTagRepositoryTest: deleteAllByTrack — 해당 Track 태그만 삭제

Quality:
- [ ] `gradlew test` 전체 통과
- [ ] 신규 케이스 포함 총 테스트 수 > 163
- [ ] 각 테스트 클래스에 @DisplayName 한국어 서술 적용

---

[INPUT POINTERS]

Tier 0 (Constitution):
- docs/standards/core-principles.md

Tier 1 (Quality Gate — testing-qa 키워드 매칭):
- docs/policies/quality-gates.md

REQ/Context Docs:
- deliverables/user/REQ-20260221-ATS-001.md
- deliverables/agent/WI-20260221-ATS-002-evidence-pack.md  ← sa 설계 결정 (TrackTag 전략 등)

Implementation Files (테스트 대상):
- src/main/java/com/atstudio/atstudio/service/TagService.java
- src/main/java/com/atstudio/atstudio/service/TrackService.java
- src/main/java/com/atstudio/atstudio/repository/TagRepository.java
- src/main/java/com/atstudio/atstudio/repository/TrackRepository.java
- src/main/java/com/atstudio/atstudio/repository/TrackTagRepository.java
- src/main/java/com/atstudio/atstudio/repository/spec/TrackSpecification.java
- src/main/java/com/atstudio/atstudio/service/storage/StorageService.java
- src/main/java/com/atstudio/atstudio/entity/Track.java
- src/main/java/com/atstudio/atstudio/entity/Tag.java
- src/main/java/com/atstudio/atstudio/entity/TrackTag.java
- src/main/java/com/atstudio/atstudio/entity/key/TrackTagId.java
- src/main/java/com/atstudio/atstudio/common/exception/BUSINESS_ERROR.java

Reference Tests (패턴 참고):
- src/test/java/com/atstudio/atstudio/service/auth/AuthServiceTest.java
- src/test/java/com/atstudio/atstudio/service/UserServiceTest.java
- src/test/java/com/atstudio/atstudio/repository/TrackTagRepositoryTest.java  ← @DataJpaTest + @Import(JpaConfig.class) 패턴
- src/test/java/com/atstudio/atstudio/repository/LikeRepositoryTest.java

Config:
- src/main/java/com/atstudio/atstudio/config/JpaConfig.java  ← @DataJpaTest에 @Import 필요
- src/test/resources/application.yml  ← sql.init.mode: never

Repro/Logs:
- gradlew.bat test (Windows: powershell .\gradlew.bat test)

---

[OUTPUT CONTRACT]
User-facing  -> deliverables/user/WI-20260221-ATS-005-summary.md
  - 작성된 테스트 케이스 목록, 커버리지 요약, 발견된 버그(있을 경우)

Agent-facing -> deliverables/agent/WI-20260221-ATS-005-evidence-pack.md
  - 생성한 테스트 파일 경로
  - 테스트 실행 결과 (gradlew test 출력 요약)
  - 신규 테스트 케이스 수, 전체 통과 여부
  - 발견한 버그 및 수정 여부

Handoff Packet -> deliverables/agent/WI-20260221-ATS-005-handoff.md (이 파일)

---

[TRACEABILITY REQUIREMENTS]
Evidence pointers: 생성한 테스트 파일 경로 + 라인 수 기록
Tests: gradlew test 출력 마지막 BUILD SUCCESSFUL/FAILED 라인 포함
Rollback: 테스트 파일만 신규 추가 (기존 파일 미수정) → git checkout으로 제거 가능

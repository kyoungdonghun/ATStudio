[WI HEADER]
WI ID: WI-20260221-ATS-011
REQ: REQ-20260221-ATS-002
Agent: re
Depends On: WI-20260221-ATS-006, WI-20260221-ATS-007, WI-20260221-ATS-008, WI-20260221-ATS-009
Blocks: WI-20260221-ATS-012

[WI SUMMARY]
Why: Phase 1에서 추가된 신규 컨트롤러 엔드포인트에 대한 보안 권한 테스트 작성
Scope (in/out):
  In:
    - TagControllerTest 추가: PUT(ADMIN→200, USER→403, 비인증→401), DELETE(동일)
    - UserControllerTest: GET /api/users(ADMIN→200, USER→403, 비인증→401), GET /{id}(동일), PUT /{id}(동일)
    - PlayHistoryControllerTest: POST(인증→201, 비인증→401), GET(인증→200, 비인증→401), DELETE(인증→204, 비인증→401)
    - LikeControllerTest: POST(인증→201, 비인증→401), GET(인증→200, 비인증→401), DELETE(인증→204, 비인증→401)
    - DownloadQueueControllerTest: POST(인증→201, 비인증→401), GET(인증→200, 비인증→401), DELETE(인증→204, 비인증→401)
    - NoticeControllerTest: POST(ADMIN→201, USER→403, 비인증→401), GET목록(비인증→200 PUBLIC), GET상세(비인증→200 PUBLIC), PUT(ADMIN→200, USER→403), DELETE(ADMIN→204, USER→403)
  Out:
    - 서비스 단위 테스트 (WI-010 담당)
    - 비즈니스 로직 검증

DoD:
  - @SpringBootTest + @AutoConfigureMockMvc 패턴 (기존과 동일)
  - @WithMockUser(roles="USER"/"ADMIN") 활용
  - PUBLIC 엔드포인트: 비인증 접근 → 200 확인
  - ADMIN 전용: USER 역할 → 403 확인
  - 인증 필요: 비인증 → 401 확인
  - ./gradlew test 전체 통과

[ACCEPTANCE CRITERIA]
Functional:
  - [ ] TagControllerTest: PUT/DELETE ADMIN(200/204), USER(403), 비인증(401) 각 케이스
  - [ ] UserControllerTest: GET목록/GET상세/PUT 각각 ADMIN(200), USER(403), 비인증(401)
  - [ ] PlayHistoryControllerTest: POST/GET/DELETE 비인증(401), 인증(201/200/204)
  - [ ] LikeControllerTest: POST/GET/DELETE 비인증(401), 인증(201/200/204)
  - [ ] DownloadQueueControllerTest: POST/GET/DELETE 비인증(401), 인증(201/200/204)
  - [ ] NoticeControllerTest: POST/PUT/DELETE ADMIN(201/200/204), USER(403); GET목록/상세 비인증(200)
Quality:
  - [ ] ./gradlew test 전체 통과
  - [ ] 각 테스트: @DisplayName 한국어로 명시

[INPUT POINTERS]
Tier 0:
  - docs/standards/core-principles.md

REQ/Context:
  - deliverables/user/REQ-20260221-ATS-002.md

컨트롤러 소스 (테스트 대상):
  - src/main/java/com/atstudio/atstudio/controller/TagController.java
  - src/main/java/com/atstudio/atstudio/controller/UserController.java
  - src/main/java/com/atstudio/atstudio/controller/PlayHistoryController.java
  - src/main/java/com/atstudio/atstudio/controller/LikeController.java
  - src/main/java/com/atstudio/atstudio/controller/DownloadQueueController.java
  - src/main/java/com/atstudio/atstudio/controller/NoticeController.java

기존 컨트롤러 테스트 패턴 참조:
  - src/test/java/com/atstudio/atstudio/controller/TagControllerTest.java
  - src/test/java/com/atstudio/atstudio/controller/LicenseControllerTest.java
  - src/test/java/com/atstudio/atstudio/controller/TrackControllerTest.java

주의사항:
  - 패키지: org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc (Spring Boot 4.x)
  - @MockitoBean CustomUserDetailsService 항상 포함
  - 인증 필요 엔드포인트의 성공 케이스: Service mock given() 설정 필수
  - GlobalExceptionHandler가 AccessDeniedException을 올바르게 403으로 처리함 (이미 검증됨)

[OUTPUT CONTRACT]
Agent-facing -> deliverables/agent/WI-20260221-ATS-011-evidence-pack.md
  - 작성한 테스트 파일 목록, 테스트 케이스 수, 실행 결과

[TRACEABILITY REQUIREMENTS]
Evidence: 생성한 테스트 파일 경로 + ./gradlew test 결과 (총 테스트 수, 실패 수)

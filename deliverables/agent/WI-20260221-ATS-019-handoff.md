[WI HEADER]
WI ID: WI-20260221-ATS-019
REQ: REQ-20260221-ATS-004
Agent: re
Depends On: WI-20260221-ATS-018
Blocks: WI-20260221-ATS-020

[WI SUMMARY]
Why: WI-018에서 구현한 Playlist 8개 API에 대한 서비스 단위 테스트 + 컨트롤러 권한 테스트 작성.
Scope (in/out):
  In:
    - PlaylistServiceTest.java — Mockito 기반 서비스 단위 테스트
    - PlaylistControllerTest.java — Spring Boot 통합 컨트롤러 권한 테스트
  Out:
    - 소스 코드 수정 금지
    - Repository 테스트 (별도 범위)
DoD:
  - PlaylistServiceTest: 8개 API 핵심 경로 + 예외 경로 테스트 (총 12개 이상)
  - PlaylistControllerTest: 각 엔드포인트 비인증→401, 인증→200/201/204 (총 10개 이상)
  - 모든 작성된 테스트 통과
Constraints/Forbidden:
  - 소스 코드 수정 금지
  - 테스트 파일 2개만 신규 작성

[ACCEPTANCE CRITERIA]
Functional:
  - [ ] PlaylistServiceTest — createPlaylist(): 성공, 비구독자 예외
  - [ ] PlaylistServiceTest — getMyPlaylists(): 성공
  - [ ] PlaylistServiceTest — getPlaylist(): 성공, 소유자 아닌 경우 예외, 없는 경우 예외
  - [ ] PlaylistServiceTest — addTrack(): 성공, 중복 예외
  - [ ] PlaylistServiceTest — updatePlaylist(): 성공
  - [ ] PlaylistServiceTest — reorderTracks(): 성공
  - [ ] PlaylistServiceTest — removeTrack(): 성공
  - [ ] PlaylistServiceTest — deletePlaylist(): 성공
  - [ ] PlaylistControllerTest — POST /api/playlists: 비인증→401, 인증→201
  - [ ] PlaylistControllerTest — GET /api/playlists: 비인증→401, 인증→200
  - [ ] PlaylistControllerTest — GET /api/playlists/{id}: 비인증→401, 인증→200
  - [ ] PlaylistControllerTest — POST /api/playlists/{id}/tracks: 비인증→401, 인증→201
  - [ ] PlaylistControllerTest — PUT /api/playlists/{id}: 비인증→401, 인증→200
  - [ ] PlaylistControllerTest — PUT /api/playlists/{id}/tracks: 비인증→401, 인증→200
  - [ ] PlaylistControllerTest — DELETE /api/playlists/{id}/tracks/{trackId}: 비인증→401, 인증→204
  - [ ] PlaylistControllerTest — DELETE /api/playlists/{id}: 비인증→401, 인증→204
Quality:
  - [ ] 컴파일 오류 없음
  - [ ] 모든 테스트 통과

[INPUT POINTERS]
Tier 0:
  - docs/standards/core-principles.md

REQ/Context:
  - deliverables/user/REQ-20260221-ATS-004.md
  - deliverables/agent/WI-20260221-ATS-018-evidence-pack.md

Implementation (read before writing tests):
  - src/main/java/com/atstudio/atstudio/service/PlaylistService.java
  - src/main/java/com/atstudio/atstudio/controller/PlaylistController.java
  - src/main/java/com/atstudio/atstudio/entity/Playlist.java
  - src/main/java/com/atstudio/atstudio/entity/PlaylistTrack.java
  - src/main/java/com/atstudio/atstudio/entity/key/PlaylistTrackId.java
  - src/main/java/com/atstudio/atstudio/repository/PlaylistRepository.java
  - src/main/java/com/atstudio/atstudio/repository/PlaylistTrackRepository.java
  - src/main/java/com/atstudio/atstudio/dto/playlist/ (all DTOs)

Pattern References (similar test files):
  - src/test/java/com/atstudio/atstudio/service/LikeServiceTest.java
  - src/test/java/com/atstudio/atstudio/service/NoticeServiceTest.java
  - src/test/java/com/atstudio/atstudio/controller/LikeControllerTest.java
  - src/test/java/com/atstudio/atstudio/controller/NoticeControllerTest.java

Infrastructure:
  - src/main/java/com/atstudio/atstudio/security/CustomUserDetails.java
  - src/main/java/com/atstudio/atstudio/exception/ErrorCode.java

[TESTING PATTERNS]

### ServiceTest 기본 구조
```java
@ExtendWith(MockitoExtension.class)
@DisplayName("PlaylistService 단위 테스트")
class PlaylistServiceTest {
    @Mock UserRepository userRepository;
    @Mock UserSubscriptionRepository userSubscriptionRepository;
    @Mock PlaylistRepository playlistRepository;
    @Mock PlaylistTrackRepository playlistTrackRepository;
    @Mock TrackRepository trackRepository;
    @Mock StorageService storageService;
    @InjectMocks PlaylistService playlistService;
}
```

### ControllerTest 기본 구조
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc  // import: org.springframework.boot.webmvc.test.autoconfigure
@DisplayName("PlaylistController 권한 테스트")
class PlaylistControllerTest {
    @Autowired MockMvc mockMvc;
    @MockitoBean PlaylistService playlistService;
    @MockitoBean CustomUserDetailsService customUserDetailsService;
}
```

### 비구독자 예외 테스트 주의
- PlaylistService 내부에서 UserRepository.findById() + UserSubscriptionRepository.findActiveByUser()를 호출하므로
  두 mock 모두 설정해야 함

### multipart 컨트롤러 테스트
```java
// POST /api/playlists — multipart
mockMvc.perform(multipart("/api/playlists")
        .param("title", "Test Playlist"))
        .andExpect(status().isCreated());
```

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260221-ATS-019-summary.md
Agent-facing -> deliverables/agent/WI-20260221-ATS-019-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260221-ATS-019-handoff.md

[TRACEABILITY REQUIREMENTS]
Evidence: 작성된 테스트 파일 경로 + 테스트 메서드 목록
Tests: 모든 테스트 통과 확인

[EVIDENCE PACK]
WI ID: WI-20260221-ATS-019
REQ: REQ-20260221-ATS-004
Agent: re (MA-assisted write)
Status: ✅ Completed

[CREATED FILES]

1. src/test/java/com/atstudio/atstudio/service/PlaylistServiceTest.java
   - @ExtendWith(MockitoExtension.class) — Mockito unit test
   - Mocks: UserRepository, UserSubscriptionRepository, PlaylistRepository,
     PlaylistTrackRepository, TrackRepository, StorageService
   - 12 test methods covering all 8 API methods + exception paths

2. src/test/java/com/atstudio/atstudio/controller/PlaylistControllerTest.java
   - @SpringBootTest + @AutoConfigureMockMvc — Spring Boot 통합 권한 테스트
   - @MockitoBean: PlaylistService, CustomUserDetailsService
   - 16 test methods (각 엔드포인트 비인증→401, 인증→200/201/204)

[TEST METHOD INDEX]

PlaylistServiceTest:
  L51  createPlaylist_success
  L67  createPlaylist_notSubscribed → NO_ACTIVE_SUBSCRIPTION
  L84  getMyPlaylists_success
  L100 getPlaylistDetail_success
  L116 getPlaylistDetail_notOwner → RESOURCE_NOT_ACCESS
  L128 getPlaylistDetail_notFound → RESOURCE_NOT_FOUND
  L138 addTrack_success
  L158 addTrack_duplicate → DATA_INTEGRITY_VIOLATION
  L178 updatePlaylist_success
  L196 reorderTracks_success
  L212 removeTrack_success
  L232 deletePlaylist_success

PlaylistControllerTest:
  L39  createPlaylist_unauthenticated_returns401
  L47  createPlaylist_authenticated_returns201
  L59  getMyPlaylists_unauthenticated_returns401
  L67  getMyPlaylists_authenticated_returns200
  L79  getPlaylistDetail_unauthenticated_returns401
  L87  getPlaylistDetail_authenticated_returns200
  L99  addTrack_unauthenticated_returns401
  L109 addTrack_authenticated_returns201
  L122 updatePlaylist_unauthenticated_returns401
  L131 updatePlaylist_authenticated_returns200
  L145 reorderTracks_unauthenticated_returns401
  L155 reorderTracks_authenticated_returns200
  L168 removeTrack_unauthenticated_returns401
  L176 removeTrack_authenticated_returns204
  L187 deletePlaylist_unauthenticated_returns401
  L195 deletePlaylist_authenticated_returns204

[IMPLEMENTATION NOTES]

1. PlaylistReorderRequest @NotEmpty 제약:
   - 컨트롤러 테스트에서 {"tracks":[]} 사용 시 400 발생
   - {"tracks":[{"trackId":1,"trackOrder":0}]} 로 수정하여 통과

2. UserSubscription.status 필드:
   - @Builder.Default = SubscriptionStatus.ACTIVE → 빌더에서 명시 불필요
   - buildSubscription() 헬퍼는 startedAt/expiresAt 만으로 충분

3. PlaylistCreateRequest/UpdateRequest:
   - @Getter @Setter @NoArgsConstructor Lombok 클래스
   - new PlaylistCreateRequest() + setTitle() 패턴 사용

4. PlaylistAddTrackRequest/ReorderRequest/TrackOrderItem:
   - Java record → 생성자로 직접 초기화

5. deletePlaylist() 검증:
   - mock 대신 실제 Playlist 객체 반환 → playlist.deactivate() 실행 확인
   - assertThat(playlist.isActive()).isFalse() 로 검증

[TEST RUN RESULTS]
Command: ./gradlew.bat test
PlaylistServiceTest: 12/12 passed
PlaylistControllerTest: 16/16 passed
Total regression: 323/323 passed (0 failures)
BUILD SUCCESSFUL

[WI HEADER]
WI ID: WI-20260226-ATS-023
REQ: REQ-20260226-ATS-006
Agent: se
Depends On: -
Blocks: WI-20260226-ATS-024

[WI SUMMARY]
Why: Whitelist Channels 도메인 4개 API (12.1~12.4) 전체 구현. 구독자가 음원 사용 예정인 YouTube 채널 URL을 등록/수정/삭제/조회할 수 있게 한다.
Scope (in/out):
  In:
    - WhitelistChannelRepository (신규)
    - WhitelistChannelService (신규: 4개 메서드 + 비즈니스 검증)
    - WhitelistChannelController (신규: POST/GET/PUT/DELETE)
    - dto/whitelist/ DTO 신규 (Request/Response)
    - WhitelistChannelServiceTest, WhitelistChannelControllerTest 신규 작성
  Out:
    - 관리자/아티스트 전체 채널 목록 조회 (미구현)
    - YouTube OAuth 채널 소유 인증 (미구현)
    - 기존 파일 수정 (entity는 이미 완비 — 수정 금지)
DoD:
  - 4개 엔드포인트 정상 동작
  - channelUrl에 "youtube.com" 미포함 시 400 INVALID_ARGUMENT
  - 비구독자 등록 시도 시 403 NO_ACTIVE_SUBSCRIPTION
  - max_whitelist_channels 초과 시 403 WHITELIST_CHANNEL_LIMIT_EXCEEDED
  - 타인 채널 수정/삭제 시 403 RESOURCE_NOT_ACCESS
  - 전체 테스트 통과 (0 failures)
Constraints/Forbidden:
  - Entity 직접 반환 금지 — DTO 변환 필수
  - @Transactional(readOnly=true) 클래스 레벨 표준 준수
  - WhitelistChannel 엔티티 직접 수정 금지 (이미 완비, is_active 없음)
  - 수정 불가 메서드 추가 금지 (WhitelistChannel은 setter 없음 — 수정 시 신규 엔티티 대체 방식 or update 전용 메서드 추가 고려)
  - GlobalExceptionHandler로 예외 통일 (직접 ResponseEntity 에러 반환 금지)

[ACCEPTANCE CRITERIA]
Functional:
  - [ ] POST /api/whitelist-channels: 201 Created + WhitelistChannelResponse 반환
  - [ ] POST 시 channelUrl에 "youtube.com" 미포함 → 400 INVALID_ARGUMENT
  - [ ] POST 시 ACTIVE 구독 없음 → 403 NO_ACTIVE_SUBSCRIPTION
  - [ ] POST 시 현재 채널 수 >= max_whitelist_channels → 403 WHITELIST_CHANNEL_LIMIT_EXCEEDED
  - [ ] GET /api/whitelist-channels: 200 OK + List<WhitelistChannelResponse> (본인 채널만)
  - [ ] PUT /api/whitelist-channels/{channelId}: 200 OK + 수정된 WhitelistChannelResponse
  - [ ] PUT 시 타인 채널 수정 → 403 RESOURCE_NOT_ACCESS
  - [ ] DELETE /api/whitelist-channels/{channelId}: 204 No Content
  - [ ] DELETE 시 타인 채널 삭제 → 403 RESOURCE_NOT_ACCESS
Performance:
  - [ ] 별도 성능 요구사항 없음
Quality:
  - [ ] 컴파일 오류 없음
  - [ ] WhitelistChannelServiceTest: 주요 흐름 + 예외 케이스 커버
  - [ ] WhitelistChannelControllerTest: 4개 엔드포인트 각 성공/실패 케이스 커버
  - [ ] 전체 테스트 0 failures 유지 (현재 362개 통과 상태 유지)

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md

Tier 0 (Standards - se agent):
- docs/standards/development-standards.md

Tier 1 (Policies - 구독 상태 체크, 인증/권한 로직 포함):
- docs/policies/security-policy.md

REQ/Context Docs:
- deliverables/user/REQ-20260226-ATS-006.md
- docs/design/usecase/whitelist.md
- docs/design/api-spec.md  ← Section 12 (Whitelist Channels: 12.1~12.4)
- docs/design/db-schema.md ← Section 9 (whitelist_channels table)

Key Entity/Repository Files:
- src/main/java/com/atstudio/atstudio/entity/WhitelistChannel.java
  (id, user(ManyToOne), channelUrl, channelName; is_active 없음; update 메서드 없음 → 서비스에서 처리 방식 결정 필요)
- src/main/java/com/atstudio/atstudio/entity/UserSubscription.java
  (user(OneToOne), subscription(ManyToOne), status, expiresAt)
- src/main/java/com/atstudio/atstudio/entity/Subscription.java
  (maxWhitelistChannels: int)
- src/main/java/com/atstudio/atstudio/repository/UserSubscriptionRepository.java
  (findActiveByUser(user, status, today))
- src/main/java/com/atstudio/atstudio/common/exception/BUSINESS_ERROR.java
  (INVALID_ARGUMENT, NO_ACTIVE_SUBSCRIPTION, WHITELIST_CHANNEL_LIMIT_EXCEEDED, RESOURCE_NOT_ACCESS, RESOURCE_NOT_FOUND 모두 존재)

Reference Patterns (기존 구현 패턴 참조):
- src/main/java/com/atstudio/atstudio/service/UtilService.java
  (구독 조회 패턴: userSubscriptionRepository.findActiveByUser(user, ACTIVE, LocalDate.now()))
- src/main/java/com/atstudio/atstudio/service/QuestionService.java
  (@Transactional(readOnly=true) 클래스, mutating 메서드만 @Transactional override 패턴)
- src/main/java/com/atstudio/atstudio/controller/QuestionController.java
  (Controller 얇게 유지, @AuthenticationPrincipal 사용 패턴)
- src/test/java/com/atstudio/atstudio/service/QuestionServiceTest.java
  (Service 테스트 패턴)
- src/test/java/com/atstudio/atstudio/controller/QuestionControllerTest.java
  (Controller 테스트 패턴: @WithMockUser + CustomUserDetails mock 필수)

[IMPLEMENTATION NOTES]
1. WhitelistChannel.update() 처리:
   - WhitelistChannel 엔티티에 update(channelUrl, channelName) 메서드가 없음
   - 옵션 A: 엔티티에 update() 메서드 추가 (권장 — JPA dirty checking)
   - 옵션 B: delete + create 방식
   - → 옵션 A 권장. 엔티티에 update() 추가는 도메인 로직이므로 허용.

2. 구독 상태 체크 방식:
   - UserSubscriptionRepository.findActiveByUser(user, SubscriptionStatus.ACTIVE, LocalDate.now())
   - Optional이 empty → NO_ACTIVE_SUBSCRIPTION (403)
   - 존재 시 subscription.getMaxWhitelistChannels() 로 한도 확인

3. 채널 수 COUNT:
   - WhitelistChannelRepository에 countByUser(User user) 추가
   - 현재 count >= maxWhitelistChannels → WHITELIST_CHANNEL_LIMIT_EXCEEDED (403)

4. youtube.com 검증:
   - channelUrl.contains("youtube.com") → false 이면 INVALID_ARGUMENT (400)

5. 소유자 체크:
   - channel.getUser().getId().equals(userDetails.getId()) → false 이면 RESOURCE_NOT_ACCESS (403)

6. DTO 반환 형식 (api-spec.md Section 12 기준):
   Response: { id, channelUrl, channelName, createdAt }

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260226-ATS-023-summary.md :
  - 구현 완료 API 목록, 생성 파일 목록, 주요 결정사항
Agent-facing -> deliverables/agent/WI-20260226-ATS-023-evidence-pack.md :
  - 생성/수정 파일 목록 + 라인 수
  - 테스트 케이스 목록 (ServiceTest N개, ControllerTest M개)
  - 주요 비즈니스 로직 구현 포인트
  - 다음 WI: WI-20260226-ATS-024 (빌드 + 전체 테스트 회귀)
Handoff Packet -> deliverables/agent/WI-20260226-ATS-023-handoff.md :
  - 이 파일

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines): 생성된 각 파일의 경로 및 핵심 메서드 라인 번호 기록
Tests: ServiceTest, ControllerTest 각 테스트 메서드명 목록 제공
Rollback: 신규 파일만 생성 (기존 파일 수정 최소화) → 롤백 시 신규 파일 삭제로 완전 원복 가능

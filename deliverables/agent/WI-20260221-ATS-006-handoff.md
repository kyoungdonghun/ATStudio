[WI HEADER]
WI ID: WI-20260221-ATS-006
REQ: REQ-20260221-ATS-002
Agent: se
Depends On: -
Blocks: WI-20260221-ATS-010, WI-20260221-ATS-011, WI-20260221-ATS-013

[WI SUMMARY]
Why: Tag 수정/삭제, User Admin CRUD, Util 상태조회 3개 — 같은 섹션 내 누락 엔드포인트 완성
Scope (in/out):
  In:
    - TagService.updateTag() + TagController PUT /api/tags/{id}
    - TagService.deleteTag() + TagController DELETE /api/tags/{id}
    - UserService.getUsers() + UserController GET /api/users (검색·페이지)
    - UserService.getUser() + UserController GET /api/users/{id}
    - UserService.updateUserByAdmin() + UserController PUT /api/users/{id}
    - UserListItemResponse, UserDetailResponse, UserAdminUpdateRequest DTO
    - UtilService.getSubscriptionStatus() + UtilController GET /api/utils/subscription-status
    - UtilService.getDownloadCount() + UtilController GET /api/utils/download-count
    - UtilService.getUserType() + UtilController GET /api/utils/user-type
    - SubscriptionStatusResponse, DownloadCountResponse, UserTypeResponse DTO
  Out:
    - 테스트 코드 (WI-010, WI-011 담당)
    - 다른 서비스 수정

DoD:
  - 8개 신규 엔드포인트가 api-spec.md v5 명세와 일치
  - Tag 삭제 시 track_tags 연관 데이터도 함께 처리 (Cascade or DB FK)
  - User Admin 엔드포인트: @PreAuthorize("hasRole('ADMIN')")
  - ./gradlew build -x test 성공

Constraints/Forbidden:
  - Entity 수정 금지 (User, Tag, Subscription, UserSubscription, TrackDownload)
  - DB 스키마 변경 금지
  - 기존 TagController.createTag(), TagController.getAllTags() 수정 금지
  - 기존 UserController 메서드 시그니처 변경 금지
  - 새 DTO는 record 타입, @JsonInclude(NON_NULL) 적용

[ACCEPTANCE CRITERIA]
Functional:
  - [ ] PUT /api/tags/{id}: 태그 name/type 수정 → 200 + TagResponse 반환, 존재하지 않는 tagId → 404
  - [ ] DELETE /api/tags/{id}: 204 No Content, 존재하지 않는 tagId → 404
  - [ ] GET /api/users?page=1&size=20&keyword=test&userType=INDIVIDUAL: ADMIN 전용, 페이지 결과 반환
  - [ ] GET /api/users/{id}: ADMIN 전용, 유저 상세 반환, 없으면 404
  - [ ] PUT /api/users/{id}: ADMIN이 role/isVerified 수정 → 200
  - [ ] GET /api/utils/subscription-status: 현재 ACTIVE 구독 정보 반환 (없으면 hasSubscription:false)
  - [ ] GET /api/utils/download-count: 오늘 다운로드 횟수 + 한도 + 잔여 반환
  - [ ] GET /api/utils/user-type: 유저타입 + 직업 반환
Quality:
  - [ ] ./gradlew build -x test 성공
  - [ ] 기존 209개 테스트 영향 없음

[INPUT POINTERS]
Tier 0 (Constitution):
  - docs/standards/core-principles.md
Tier 0 (Standards):
  - docs/standards/development-standards.md
  - docs/standards/dto-standards.md
  - docs/standards/exception-handling.md

REQ/Context Docs:
  - deliverables/user/REQ-20260221-ATS-002.md
  - docs/design/api-spec.md (Section 2.3, 2.4, 5.5, 5.6, 5.8, 14.4, 14.5, 14.6)
  - docs/design/db-schema.md (Section 1.1 users, 4.2 tags)

Files (기존 코드 참조):
  - src/main/java/com/atstudio/atstudio/controller/TagController.java
  - src/main/java/com/atstudio/atstudio/service/TagService.java
  - src/main/java/com/atstudio/atstudio/controller/UserController.java
  - src/main/java/com/atstudio/atstudio/service/UserService.java
  - src/main/java/com/atstudio/atstudio/controller/UtilController.java
  - src/main/java/com/atstudio/atstudio/entity/Tag.java
  - src/main/java/com/atstudio/atstudio/entity/User.java
  - src/main/java/com/atstudio/atstudio/entity/UserSubscription.java
  - src/main/java/com/atstudio/atstudio/entity/TrackDownload.java
  - src/main/java/com/atstudio/atstudio/repository/TagRepository.java
  - src/main/java/com/atstudio/atstudio/repository/UserRepository.java
  - src/main/java/com/atstudio/atstudio/repository/UserSubscriptionRepository.java
  - src/main/java/com/atstudio/atstudio/repository/TrackDownloadRepository.java
  - src/main/java/com/atstudio/atstudio/common/exception/BUSINESS_ERROR.java
  - src/main/java/com/atstudio/atstudio/dto/tag/TagResponse.java
  - src/main/java/com/atstudio/atstudio/dto/user/ (기존 DTO 참조)

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260221-ATS-006-summary.md :
  - 구현된 엔드포인트 목록, 주요 결정사항, 주의 필요 사항
Agent-facing -> deliverables/agent/WI-20260221-ATS-006-evidence-pack.md :
  - 생성/수정된 파일 목록 + 경로, 빌드 결과, 후속 WI 참고사항
Handoff Packet -> deliverables/agent/WI-20260221-ATS-006-handoff.md :
  - 이 파일

[TRACEABILITY REQUIREMENTS]
Evidence pointers: 생성/수정한 모든 파일 경로 명시
Tests: 빌드 명령어 및 결과 기록 (./gradlew build -x test)
Rollback: 기존 TagController/UserController/UtilController는 수정 전 메서드 목록 명시

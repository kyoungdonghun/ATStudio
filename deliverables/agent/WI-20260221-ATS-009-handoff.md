[WI HEADER]
WI ID: WI-20260221-ATS-009
REQ: REQ-20260221-ATS-002
Agent: se
Depends On: -
Blocks: WI-20260221-ATS-010, WI-20260221-ATS-011, WI-20260221-ATS-013

[WI SUMMARY]
Why: 공지사항 CRUD — ADMIN 생성/수정/삭제, PUBLIC 목록/상세 조회 (고정 공지 우선 정렬)
Scope (in/out):
  In:
    - NoticeService (createNotice, getNotices, getNotice, updateNotice, deleteNotice)
    - NoticeController (POST /api/notices, GET /api/notices, GET /{id}, PUT /{id}, DELETE /{id})
    - NoticeCreateRequest, NoticeUpdateRequest, NoticeResponse, NoticeListItemResponse DTO
  Out:
    - 테스트 코드 (WI-010, WI-011 담당)
    - 다른 서비스 수정

DoD:
  - 5개 엔드포인트가 api-spec.md v5 Section 9 명세와 일치
  - 목록 조회: isPinned=true 우선 정렬 → 최신순 (ORDER BY is_pinned DESC, created_at DESC)
  - 생성/수정/삭제: @PreAuthorize("hasRole('ADMIN')")
  - 조회(목록/상세): PUBLIC (비인증 접근 가능) → SecurityConfig permitAll 추가
  - ./gradlew build -x test 성공

Constraints/Forbidden:
  - Entity 수정 금지 (Notice)
  - 삭제: 물리 삭제 (소프트 삭제 아님 — Notice 엔티티에 is_active 없음)
  - 새 DTO는 record 타입, @JsonInclude(NON_NULL)
  - 목록 응답: 페이지네이션 (PageInfo + dataList) — api-spec 9.2
  - 상세 응답: content 필드 포함 — api-spec 9.3

[ACCEPTANCE CRITERIA]
Functional:
  - [ ] POST /api/notices: ADMIN → 201, 비인증 → 401, USER → 403
  - [ ] GET /api/notices?page=1&size=20: 비인증 접근 가능 → 200, isPinned=true 먼저
  - [ ] GET /api/notices/{id}: 비인증 접근 가능 → 200, 없으면 404
  - [ ] PUT /api/notices/{id}: ADMIN → 200, 없으면 404
  - [ ] DELETE /api/notices/{id}: ADMIN → 204, 없으면 404
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
  - docs/design/api-spec.md (Section 9.1~9.5)
  - docs/design/db-schema.md (Section 12.1 notices)

Files (기존 코드 참조):
  - src/main/java/com/atstudio/atstudio/entity/Notice.java
  - src/main/java/com/atstudio/atstudio/repository/NoticeRepository.java
  - src/main/java/com/atstudio/atstudio/config/SecurityConfig.java (permitAll 패턴 참조)
  - src/main/java/com/atstudio/atstudio/common/exception/BUSINESS_ERROR.java
  - src/main/java/com/atstudio/atstudio/service/TagService.java (단순 CRUD 패턴 참조)
  - src/main/java/com/atstudio/atstudio/controller/TagController.java (ADMIN 권한 패턴 참조)
  - src/main/java/com/atstudio/atstudio/common/dto/PageInfo.java
  - src/main/java/com/atstudio/atstudio/common/dto/ResponseDTO.java

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260221-ATS-009-summary.md :
  - 구현된 엔드포인트, 정렬 로직 결정사항
Agent-facing -> deliverables/agent/WI-20260221-ATS-009-evidence-pack.md :
  - 생성/수정 파일 목록, 빌드 결과
Handoff Packet -> deliverables/agent/WI-20260221-ATS-009-handoff.md :
  - 이 파일

[TRACEABILITY REQUIREMENTS]
Evidence pointers: 생성/수정한 모든 파일 경로 명시
Tests: ./gradlew build -x test 결과
Rollback: SecurityConfig에 추가한 permitAll 경로 명시

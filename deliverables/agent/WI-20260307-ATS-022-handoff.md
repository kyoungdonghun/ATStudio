[WI HEADER]
WI ID: WI-20260307-ATS-022
REQ: REQ-20260307-ATS-008
Agent: se
Depends On: WI-017 (Phase 4 체크 완료 — BD-2 CRITICAL 확인)
Blocks: -

---

[WI SUMMARY]
Why: BD-2 — GET /api/tracks/admin 신규 엔드포인트 구현 (관리자 전용, 비활성 포함 전체 목록)
Scope (in):
  - TrackController.java: GET /admin 엔드포인트 추가
  - TrackService.java: getTracksForAdmin() 메서드 추가
  - TrackRepository.java: is_active 필터 쿼리 추가 (또는 Specification 활용)
  - DTO: AdminTrackListItemResponse.java 신규 (또는 기존 TrackListItemResponse에 isActive 필드 추가)
  - SecurityConfig.java: /api/tracks/admin 권한 설정 확인 및 추가
Scope (out):
  - 기존 GET /api/tracks (public) 로직 변경 금지
  - 다른 도메인 파일 수정 금지

DoD:
  - GET /api/tracks/admin [ADMIN only] 작동
  - is_active 파라미터 없으면 전체(활성+비활성) 반환
  - is_active=true이면 활성 트랙만, is_active=false이면 비활성 트랙만
  - 응답에 isActive 필드 포함
  - ADMIN 아닌 사용자 접근 시 403 반환

Constraints/Forbidden:
  - 기존 public track API 변경 금지
  - SecurityConfig에서 ADMIN 권한 설정 추가 시 기존 규칙 순서 유의

---

[ACCEPTANCE CRITERIA]

Functional:
- [ ] `GET /api/tracks/admin` 엔드포인트 추가 (Auth=[ADMIN])
      Query Params: page(default=1), size(default=20), isActive(optional Boolean)
- [ ] isActive 파라미터 없을 때: 활성+비활성 전체 반환
- [ ] isActive=true: is_active=true 트랙만 반환
- [ ] isActive=false: is_active=false 트랙만 반환
- [ ] 응답 DTO에 isActive 필드 포함 (기존 TrackListItemResponse 확장 또는 별도 DTO)
- [ ] 응답 구조: { dataList: [...], pageInfo: {...} } (기존 §1.2와 동일 구조)
- [ ] ADMIN 아닌 사용자 → 403 Forbidden
- [ ] 미인증 사용자 → 401 Unauthorized

Quality:
- [ ] SecurityConfig에서 `/api/tracks/admin` ADMIN 권한 규칙 위치 확인 (more specific rule first)
- [ ] 기존 GET /api/tracks 테스트 영향 없음
- [ ] 신규 엔드포인트 테스트 추가 (TrackControllerTest 또는 TrackServiceTest)
- [ ] `./gradlew test` 전체 통과

---

[INPUT POINTERS]

Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md

REQ:
- deliverables/user/REQ-20260307-ATS-008.md

Phase 4 체크 결과 (발견 근거):
- deliverables/user/WI-20260307-ATS-017-summary.md

API 스펙 (수정 기준):
- docs/design/api-spec.md  ← §1.8 (GET /api/tracks/admin)
- docs/check/atstudio-front-list.md  ← K-7 (트랙 관리 화면)

Files (수정 대상):
- src/main/java/com/atstudio/atstudio/controller/TrackController.java
- src/main/java/com/atstudio/atstudio/service/TrackService.java
- src/main/java/com/atstudio/atstudio/repository/TrackRepository.java
- src/main/java/com/atstudio/atstudio/config/SecurityConfig.java  (ADMIN 권한 규칙 확인)
- src/main/java/com/atstudio/atstudio/dto/track/  (isActive 필드 추가 또는 신규 DTO)

Test Files:
- src/test/java/com/atstudio/atstudio/service/TrackServiceTest.java  (있으면)
- src/test/java/com/atstudio/atstudio/controller/TrackControllerTest.java  (있으면)

---

[OUTPUT CONTRACT]

User-facing -> deliverables/user/WI-20260307-ATS-022-summary.md
Agent-facing -> deliverables/agent/WI-20260307-ATS-022-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260307-ATS-022-handoff.md

---

[TRACEABILITY REQUIREMENTS]
Evidence: 수정/추가 파일:라인 포인터 포함
Tests: ./gradlew test 실행 결과 포함 (테스트 수, failures=0 확인)

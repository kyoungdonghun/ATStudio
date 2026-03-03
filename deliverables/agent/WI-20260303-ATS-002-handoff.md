[WI HEADER]
WI ID: WI-20260303-ATS-002
REQ: REQ-20260303-ATS-001
Agent: cr
Depends On: -
Blocks: -

[WI SUMMARY]
Why: api-spec.md에 정의된 79개 API와 실제 Controller/Service 구현의 최종 정합성 검증. 누락/추가/불일치 항목을 발견하여 보고
Scope (in):
  - docs/design/api-spec.md — 79개 API (HTTP 메서드, URL, 요청/응답 구조, 상태코드, 권한)
  - src/main/java/.../controller/ — 모든 Controller 클래스 (@RequestMapping, @GetMapping 등)
  - src/main/java/.../service/ — 비즈니스 로직 (spec 기준 동작 구현 여부 확인)
  - src/main/java/.../dto/ — 요청/응답 DTO (spec의 필드명/타입 일치 여부)
  - src/main/java/.../entity/BUSINESS_ERROR.java — 에러 코드/상태코드 일치 여부
Scope (out):
  - 테스트 코드 리뷰 (WI-001 담당)
  - 새로운 기능 추가
  - 구현 코드 직접 수정 (발견만, 수정 금지)
DoD:
  - 79개 API 전체 검증 완료
  - 불일치 목록 또는 "전체 일치" 결론 명시
  - evidence-pack에 섹션별 체크 결과 기록
Constraints/Forbidden:
  - 코드 직접 수정 금지 (읽기/분석/보고만)
  - 발견된 불일치는 심각도(CRITICAL/MAJOR/MINOR) 분류하여 보고

[ACCEPTANCE CRITERIA]
Functional:
- [ ] 79개 API 모두 검증됨 (빠진 API 없음)
- [ ] HTTP 메서드 일치 여부 확인 (spec vs Controller)
- [ ] URL 패턴 일치 여부 확인 (path variable, query param 포함)
- [ ] 응답 HTTP 상태코드 일치 여부 (200/201/204/400/401/403/404/409 등)
- [ ] 요청 DTO 필드 일치 여부 (필드명, 필수/선택 여부)
- [ ] 응답 DTO 필드 일치 여부
- [ ] 인증/권한 요구사항 일치 여부 (PUBLIC/USER/ADMIN/BUSINESS)
- [ ] 불일치 항목 심각도 분류 (CRITICAL/MAJOR/MINOR)
Quality:
- [ ] evidence-pack에 섹션별(Track/License/User/etc.) 체크 결과 표 형식으로 기록
- [ ] 전체 불일치 건수 요약 명시

[INPUT POINTERS]
Tier 0 (Constitution):
- docs/standards/core-principles.md

Tier 0 (Standards):
- docs/standards/development-standards.md

Tier 1 (Policies):
- docs/policies/security-policy.md
- docs/policies/quality-gates.md

REQ/Context Docs:
- deliverables/user/REQ-20260303-ATS-001.md

API Spec (검증 기준 — Source of Truth):
- docs/design/api-spec.md

Controller Files (구현 — 비교 대상):
- src/main/java/com/atstudio/atstudio/controller/TrackController.java
- src/main/java/com/atstudio/atstudio/controller/LicenseController.java
- src/main/java/com/atstudio/atstudio/controller/TagController.java
- src/main/java/com/atstudio/atstudio/controller/UserController.java
- src/main/java/com/atstudio/atstudio/controller/PlayHistoryController.java
- src/main/java/com/atstudio/atstudio/controller/LikeController.java
- src/main/java/com/atstudio/atstudio/controller/DownloadQueueController.java
- src/main/java/com/atstudio/atstudio/controller/NoticeController.java
- src/main/java/com/atstudio/atstudio/controller/QuestionController.java
- src/main/java/com/atstudio/atstudio/controller/PlaylistController.java
- src/main/java/com/atstudio/atstudio/controller/WhitelistChannelController.java
- src/main/java/com/atstudio/atstudio/controller/CompanyCertificationController.java
- src/main/java/com/atstudio/atstudio/controller/SubscriptionController.java
- src/main/java/com/atstudio/atstudio/controller/UserSubscriptionController.java
- src/main/java/com/atstudio/atstudio/controller/DownloadController.java
- src/main/java/com/atstudio/atstudio/controller/auth/AuthController.java

DTO Directory (필드 검증):
- src/main/java/com/atstudio/atstudio/dto/

Error Codes (상태코드 검증):
- src/main/java/com/atstudio/atstudio/exception/BUSINESS_ERROR.java

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260303-ATS-002-summary.md :
- 검증 결과 요약 (전체 일치 또는 불일치 건수/목록), 권고사항
Agent-facing -> deliverables/agent/WI-20260303-ATS-002-evidence-pack.md :
- 섹션별 체크 결과 표, 불일치 항목 상세 (파일:라인, 심각도, 내용)
Handoff Packet -> deliverables/agent/WI-20260303-ATS-002-handoff.md :
- 이 파일 (추적용)

[TRACEABILITY REQUIREMENTS]
Evidence pointers: 불일치 항목마다 spec 위치(api-spec.md 라인) + 구현 위치(파일:라인) 명시
Tests: N/A (정합성 검증은 정적 분석)
Rollback: N/A (읽기 전용 작업)

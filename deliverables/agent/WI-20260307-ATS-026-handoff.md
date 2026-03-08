[WI HEADER]
WI ID: WI-20260307-ATS-026
REQ: REQ-20260307-ATS-009
Agent: cr
Depends On: -
Blocks: -

---

[WI SUMMARY]
Why: Track 2-B — api-spec §5~9, §12~15 ↔ 백엔드 코드 정합성 검증 (read-only)
Scope (in):
  - api-spec §5(User/Auth), §6(Subscription), §7(License), §8(Inquiry),
    §9(Notice), §12(Whitelist), §13(CompanyCert), §14(Util), §15(Album)
  - 대상 Controller: UserController, AuthController, UserSubscriptionController,
    SubscriptionController, LicenseController, QuestionController, NoticeController,
    WhitelistChannelController, CompanyCertificationController, UtilController, AlbumController
  - 검증 항목:
    1. URL, HTTP Method, 경로 파라미터 일치
    2. 응답 HTTP 상태코드
    3. 권한 설정 (@PreAuthorize, SecurityConfig 규칙)
    4. 요청 DTO 필드명 (api-spec 요청 body ↔ @RequestBody DTO)
    5. 응답 DTO 필드명 (api-spec 응답 fields ↔ record/class 필드)
    6. BD-1 구독 취소 유예기간: findActiveByUser() 적용 범위 정상화 확인
Scope (out):
  - 파일 수정 금지 (발견·보고만)
  - §1~4, §10~11 (WI-025 담당)
  - 문서↔문서 검증 (WI-024 담당)

DoD:
  - 11개 Controller 각각 불일치 항목 목록 산출
  - CRITICAL/MAJOR/MINOR/SUGGESTION 분류 명시
  - 발견 없으면 "PASS" 명시

Constraints/Forbidden:
  - 절대 파일 수정 금지
  - 판단 근거(api-spec 섹션# + 파일:라인) 증거로 명시

---

[ACCEPTANCE CRITERIA]

Functional:
- [ ] UserController + AuthController ↔ api-spec §5 (5.1~5.11) 검증
- [ ] UserSubscriptionController + UserSubscriptionService ↔ api-spec §6 (6.1~6.10) 검증
      포함: BD-1 취소 유예기간 — 취소 후 expiresAt까지 getMySubscription 정상 반환 확인
- [ ] LicenseController ↔ api-spec §7 (7.1~7.4) 검증
- [ ] QuestionController ↔ api-spec §8 (8.1~8.7) 검증
- [ ] NoticeController ↔ api-spec §9 (9.1~9.5) 검증
- [ ] WhitelistChannelController ↔ api-spec §12 (12.1~12.4) 검증
- [ ] CompanyCertificationController ↔ api-spec §13 (13.1~13.5) 검증
- [ ] UtilController + UtilService ↔ api-spec §14 (14.1~14.8) 검증
      포함: §14.5 nextResetAt, §14.8 subscription-change-preview
- [ ] AlbumController + AlbumService ↔ api-spec §15 (8개 Album API) 검증

Quality:
- [ ] 이슈별 api-spec 섹션# + Controller/Service 파일:라인 포인터 포함
- [ ] CRITICAL: 구현 없는 API, 상태코드 오류, 권한 누락
- [ ] MAJOR: 필드명/타입 불일치, 경로 파라미터 불일치
- [ ] MINOR: 응답 메시지 불일치, 주석 오류

---

[INPUT POINTERS]

Tier 0 (Standards):
- docs/standards/core-principles.md
- docs/standards/development-standards.md

REQ:
- deliverables/user/REQ-20260307-ATS-009.md

API Spec (검증 기준):
- docs/design/api-spec.md  ← §5, §6, §7, §8, §9, §12, §13, §14, §15

Backend Files (검증 대상):
- src/main/java/com/atstudio/atstudio/controller/UserController.java
- src/main/java/com/atstudio/atstudio/controller/AuthController.java
- src/main/java/com/atstudio/atstudio/controller/UserSubscriptionController.java
- src/main/java/com/atstudio/atstudio/controller/SubscriptionController.java
- src/main/java/com/atstudio/atstudio/controller/LicenseController.java
- src/main/java/com/atstudio/atstudio/controller/QuestionController.java
- src/main/java/com/atstudio/atstudio/controller/NoticeController.java
- src/main/java/com/atstudio/atstudio/controller/WhitelistChannelController.java
- src/main/java/com/atstudio/atstudio/controller/CompanyCertificationController.java
- src/main/java/com/atstudio/atstudio/controller/UtilController.java
- src/main/java/com/atstudio/atstudio/controller/AlbumController.java
- src/main/java/com/atstudio/atstudio/service/UserSubscriptionService.java
- src/main/java/com/atstudio/atstudio/service/UtilService.java
- src/main/java/com/atstudio/atstudio/service/AlbumService.java
- src/main/java/com/atstudio/atstudio/config/SecurityConfig.java
- src/main/java/com/atstudio/atstudio/dto/  (관련 DTO 파일)

---

[OUTPUT CONTRACT]

User-facing -> deliverables/user/WI-20260307-ATS-026-summary.md :
- Controller별 발견 이슈 요약
Agent-facing -> deliverables/agent/WI-20260307-ATS-026-evidence-pack.md :
- 이슈별 상세 근거 포인터
Handoff Packet -> deliverables/agent/WI-20260307-ATS-026-handoff.md

---

[TRACEABILITY REQUIREMENTS]
Evidence: 이슈별 api-spec 섹션# + 파일:라인 포인터 필수
Tests: 해당 없음 (read-only 검증)

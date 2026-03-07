[WI HEADER]
WI ID: WI-20260307-ATS-021
REQ: REQ-20260307-ATS-008
Agent: se
Depends On: WI-016 (Phase 4 체크 완료 — BD-1 CRITICAL 확인)
Blocks: -

---

[WI SUMMARY]
Why: BD-1 — 구독 취소 후 유예 기간 구현 (expiresAt까지 서비스 이용 가능)
Scope (in):
  - UserSubscriptionRepository.java: findActiveByUser() 쿼리 수정
  - UserSubscription.java: 필요 시 cancel() 또는 canAccess() 메서드 보완
  - UserSubscriptionService.java: 영향 범위 확인 (기존 로직 유지)
  - 이 3개 파일 및 관련 테스트 파일만
Scope (out):
  - 다른 도메인 파일 수정 금지
  - API 응답 스펙 변경 금지

DoD:
  - selfCancel() 호출 후 status=CANCELLED이나 expiresAt까지 서비스 이용 가능
  - 구독 취소 후 다운로드, 재생목록 등 혜택 유지 (expiresAt 이후 자동 차단)
  - 기존 ACTIVE 구독 조회 정상 동작
  - 관련 테스트 통과

Constraints/Forbidden:
  - 기존 subscribe(), changeSubscription() 로직 변경 금지
  - CANCELLED 상태인 구독이 expiresAt 이후에도 유지되면 안 됨 (expiresAt >= today 조건 반드시 유지)

---

[ACCEPTANCE CRITERIA]

Functional:
- [ ] `findActiveByUser()` 수정: ACTIVE + CANCELLED 모두 반환하되 expiresAt >= today 조건 유지
      BEFORE: `WHERE us.status = :status AND us.expiresAt >= :today` (status=ACTIVE 파라미터)
      AFTER: `WHERE us.status IN ('ACTIVE', 'CANCELLED') AND us.expiresAt >= :today`
- [ ] `selfCancel()` 후 `getMySubscription()` 호출 시 구독 정보 반환됨 (status=CANCELLED, expiresAt 유지)
- [ ] `selfCancel()` 후 DownloadService 등에서 구독 혜택 이용 가능 (expiresAt 이내)
- [ ] expiresAt 이후에는 정상적으로 NO_ACTIVE_SUBSCRIPTION 반환

Quality:
- [ ] `findActiveByUser` 호출하는 모든 서비스에서 영향 범위 확인
      (UserSubscriptionService, UtilService, DownloadService, PlaylistService 등)
- [ ] 기존 테스트 모두 통과
- [ ] 취소 후 expiresAt 이내 서비스 이용 가능 테스트 추가 (UserSubscriptionServiceTest)

---

[INPUT POINTERS]

Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md

REQ:
- deliverables/user/REQ-20260307-ATS-008.md

Phase 4 체크 결과 (발견 근거):
- deliverables/user/WI-20260307-ATS-016-summary.md

API 스펙 (수정 기준):
- docs/design/api-spec.md  ← §6.10 (DELETE /api/user-subscriptions/me)
- docs/design/usecase/user-subscription.md  ← PAYMENT-010

Files (수정 대상):
- src/main/java/com/atstudio/atstudio/repository/UserSubscriptionRepository.java
- src/main/java/com/atstudio/atstudio/entity/UserSubscription.java  (cancel() 메서드 — 현재: status=CANCELLED만)
- src/main/java/com/atstudio/atstudio/service/UserSubscriptionService.java  (영향 확인)

Files (영향 범위 확인):
- src/main/java/com/atstudio/atstudio/service/UtilService.java  (findActiveByUser 호출)
- src/main/java/com/atstudio/atstudio/service/DownloadService.java  (구독 체크)
- src/main/java/com/atstudio/atstudio/service/PlaylistService.java  (구독 체크)

Test Files:
- src/test/java/com/atstudio/atstudio/service/UserSubscriptionServiceTest.java

---

[OUTPUT CONTRACT]

User-facing -> deliverables/user/WI-20260307-ATS-021-summary.md
Agent-facing -> deliverables/agent/WI-20260307-ATS-021-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260307-ATS-021-handoff.md

---

[TRACEABILITY REQUIREMENTS]
Evidence: 수정 파일:라인 포인터 포함
Tests: ./gradlew test 실행 결과 포함

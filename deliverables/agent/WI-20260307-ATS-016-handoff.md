[WI HEADER]
WI ID: WI-20260307-ATS-016
REQ: REQ-20260307-ATS-008
Agent: cr
Depends On: WI-013/014/015 (Phase 3 완료)
Blocks: -

---

[WI SUMMARY]
Why: Phase 4 — 구독/다운로드/Utils 도메인 코드 정합성 체크 (Phase 3 문서 기준)
Scope (in):
  - UserSubscriptionService.java + UserSubscriptionController.java 코드 체크
  - UtilService.java + UtilController.java 코드 체크
  - DownloadQueueService.java + DownloadQueueController.java 코드 체크
  - 이 6개 파일(+관련 DTO) 만
Scope (out):
  - 코드 수정 금지
  - 다른 도메인 파일 탐색 금지
  - 문서 수정 금지

Constraints/Forbidden:
  - 발견 보고만. 코드 수정 절대 금지.
  - 아래 명시된 파일 외 탐색 금지

---

[ACCEPTANCE CRITERIA]

Functional:
- [ ] UserSubscriptionService: changeSubscription() / updateSubscription() 메서드에 UPGRADE/DOWNGRADE 분기 존재 여부 확인
       UPGRADE: 즉시 적용 + proratedAmount 결제
       DOWNGRADE: pendingSubscriptionId + pendingBillingCycle 저장, 현재 기간 만료 후 적용
- [ ] UserSubscriptionService: cancelSubscription() — status=CANCELLED 처리 후 혜택 즉시 중단 vs expiresAt까지 유지 여부
       기대: CANCELLED 상태이나 expiresAt까지 서비스 이용 가능해야 함 (BD-1)
       실제 코드: 혜택 즉시 차단 로직 존재 여부 확인
- [ ] UserSubscriptionController: changeSubscription 응답에 changeType 필드 포함 여부
- [ ] UtilController: GET /api/utils/download-count 응답에 nextResetAt 필드 포함 여부
       UtilService.getDownloadCount() 반환 DTO에 nextResetAt(LocalDateTime) 존재 확인
- [ ] UtilController: GET /api/utils/subscription-change-preview 엔드포인트 존재 여부 (UTIL-013)
       존재하지 않으면 [GAP] CRITICAL 보고
- [ ] DownloadQueueController: DELETE /api/download-queue/{trackId} — path param 이름이 {trackId}인지 확인
       {id}인 경우 [CONFLICT] MINOR 보고

Quality:
- [ ] 발견 항목별 파일:라인 포인터 포함
- [ ] CONFLICT/GAP/OMISSION/SUGGESTION 형식 준수

---

[INPUT POINTERS]

Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 1 (cr 필수):
- docs/policies/security-policy.md

REQ:
- deliverables/user/REQ-20260307-ATS-008.md

참조 문서 (코드와 대조 기준):
- docs/design/api-spec.md  ← §6.7 (PUT /api/user-subscriptions/me), §6.10 (DELETE /api/user-subscriptions/me), §11 (DownloadQueue), §14.5 (download-count), §14.8 (subscription-change-preview)
- docs/design/usecase/user-subscription.md  ← PAYMENT-007 (플랜 변경), PAYMENT-010 (구독 취소)
- docs/design/usecase/util.md  ← UTIL-006 (download-count), UTIL-013 (subscription-change-preview)

Files (검사 대상):
- src/main/java/com/atstudio/atstudio/service/UserSubscriptionService.java
- src/main/java/com/atstudio/atstudio/controller/UserSubscriptionController.java
- src/main/java/com/atstudio/atstudio/service/UtilService.java
- src/main/java/com/atstudio/atstudio/controller/UtilController.java
- src/main/java/com/atstudio/atstudio/service/DownloadQueueService.java
- src/main/java/com/atstudio/atstudio/controller/DownloadQueueController.java
- src/main/java/com/atstudio/atstudio/dto/  (관련 DTO 파일 — UtilResponse, UserSubscriptionResponse 등)

---

[OUTPUT CONTRACT]

User-facing -> deliverables/user/WI-20260307-ATS-016-summary.md
Agent-facing -> deliverables/agent/WI-20260307-ATS-016-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260307-ATS-016-handoff.md

---

[TRACEABILITY REQUIREMENTS]
Evidence: 발견 항목별 파일:라인 포인터 포함
Format:
  [CONFLICT] 상충: (코드 파일:라인) vs (문서:섹션) — 설명
  [GAP]      누락: 문서에는 있으나 코드에 없음 — 설명
  [OMISSION] 미흡: 부분 구현 — 설명
  [SUGGESTION] 제안: 개선 가능 — 설명
심각도: CRITICAL / MAJOR / MINOR / SUGGESTION

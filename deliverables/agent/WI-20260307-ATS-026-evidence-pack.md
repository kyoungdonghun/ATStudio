[EVIDENCE PACK]
WI ID: WI-20260307-ATS-026
REQ: REQ-20260307-ATS-009
Agent: cr
Completed: 2026-03-08

---

## BD-1 구독 취소 유예기간 검증 (PASS)
- `src/main/java/com/atstudio/atstudio/repository/UserSubscriptionRepository.java:21-23`
  JPQL: `status IN ('ACTIVE', 'CANCELLED') AND expiresAt >= :today` ✅
- `src/main/java/com/atstudio/atstudio/service/UserSubscriptionService.java:230-240`
  `selfCancel()` → `cancel()` (status=CANCELLED, expiresAt 유지) ✅
- `src/main/java/com/atstudio/atstudio/service/UserSubscriptionService.java:95-104`
  `getMySubscription()` → `findActiveByUser()` → CANCELLED 구독 정상 반환 ✅

## §14.5 nextResetAt PASS
- `src/main/java/com/atstudio/atstudio/dto/util/DownloadCountResponse.java:8-13`
  `LocalDateTime nextResetAt` 필드 포함 ✅
- `src/main/java/com/atstudio/atstudio/service/UtilService.java:70` — nextResetAt 계산 및 반환 ✅

## §14.8 subscription-change-preview PASS
- `src/main/java/com/atstudio/atstudio/controller/UtilController.java:76-84` — 엔드포인트 구현 ✅
- `src/main/java/com/atstudio/atstudio/dto/util/SubscriptionChangePreviewResponse.java:6-12`
  fields: `changeType, proratedAmount, effectiveDate, newPlanName, newBillingCycle` — api-spec 완전 일치 ✅

---

## MINOR-001: UserSubscriptionResponse superset
- `src/main/java/com/atstudio/atstudio/dto/subscription/UserSubscriptionResponse.java:8-19`
  extra fields: `userId, userNickname, full SubscriptionResponse(9 fields), pendingSubscriptionId, pendingBillingCycle, createdAt`
- api-spec §6.3 응답: `subscriptionId, planName, billingCycle, status, startedAt, expiresAt`
- 수정 제안: api-spec §6.3/§6.4 응답 섹션에 실제 반환 필드 명시

## MINOR-002: UserResponse superset
- `src/main/java/com/atstudio/atstudio/dto/user/UserResponse.java:5-17`
  extra fields: `phonePersonal, phoneCompany, role`
- api-spec §5.1 응답: `id, nickname, email, job, userType, isVerified, createdAt`
- 수정 제안: api-spec §5.1/§5.4 응답 섹션에 실제 반환 필드 명시

## SUGGESTION-001: UserSubscriptionController @PreAuthorize 없음
- `src/main/java/com/atstudio/atstudio/controller/UserSubscriptionController.java`
  - line 49: `listAll()` — @PreAuthorize 없음, SecurityConfig:107에서만 보호
  - line 58: `getDetail()` — @PreAuthorize 없음, SecurityConfig:108에서만 보호
  - line 84: `adminUpdate()` — @PreAuthorize 없음, SecurityConfig:109에서만 보호
  - line 97: `adminCancel()` — @PreAuthorize 없음, SecurityConfig:110에서만 보호
- 비교: NoticeController(line 26, 55, 68) — @PreAuthorize + SecurityConfig 이중 보호

## SUGGESTION-002: CompanyCertificationController.processReview @PreAuthorize 없음
- `src/main/java/com/atstudio/atstudio/controller/CompanyCertificationController.java:77-87`
  @PreAuthorize 없음, SecurityConfig:101에서만 보호

---

## 전체 엔드포인트 검증 현황

53개 엔드포인트 검증 완료. PASS 49건 / Issues 4건(MINOR 2, SUGGESTION 2).

| Controller | 검증 엔드포인트 | 결과 |
|---|---|---|
| AuthController | 3 | ALL PASS |
| UserController | 9 | MINOR-002(UserResponse superset) |
| SubscriptionController | 2 | ALL PASS |
| UserSubscriptionController | 8 | MINOR-001(UserSubscriptionResponse superset), SUGGESTION-001 |
| LicenseController | 4 | ALL PASS |
| QuestionController | 7 | ALL PASS |
| NoticeController | 5 | ALL PASS |
| WhitelistChannelController | 4 | ALL PASS |
| CompanyCertificationController | 5 | SUGGESTION-002 |
| UtilController | 7 | ALL PASS |
| AlbumController | 8 | ALL PASS |

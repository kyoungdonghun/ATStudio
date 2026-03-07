# WI-20260307-ATS-001 Summary

## REQ
REQ-20260307-ATS-007

## 변경 요약

### T-1: DownloadCountResponse에 nextResetAt 필드 추가
- `GET /api/utils/download-count` 응답에 `nextResetAt` (LocalDateTime) 필드 추가
- 값: 내일 00:00 (`LocalDate.now().plusDays(1).atStartOfDay()`)
- 구독 유무와 관계없이 항상 포함

### T-2: subscription-change-preview API 신규
- 엔드포인트: `GET /api/utils/subscription-change-preview?subscriptionId={id}&billingCycle={MONTHLY|YEARLY}`
- 응답 DTO: `SubscriptionChangePreviewResponse` (changeType, proratedAmount, effectiveDate, newPlanName, newBillingCycle)
- UPGRADE 판정: newPlan.priceMonthly >= currentPlan.priceMonthly
  - proratedAmount = newPrice - (currentPrice x remainingDays / totalDays)
  - effectiveDate = today
- DOWNGRADE 판정: newPlan.priceMonthly < currentPlan.priceMonthly
  - proratedAmount = 0
  - effectiveDate = current.expiresAt
- 에러 처리: 활성 구독 없음 (403), 잘못된 billingCycle (400), 구독 플랜 미존재 (404)

## 변경 파일

| 파일 | 변경 유형 |
|------|----------|
| `src/main/java/com/atstudio/atstudio/dto/util/DownloadCountResponse.java` | 수정 (nextResetAt 필드 추가) |
| `src/main/java/com/atstudio/atstudio/dto/util/SubscriptionChangePreviewResponse.java` | 신규 생성 |
| `src/main/java/com/atstudio/atstudio/service/UtilService.java` | 수정 (nextResetAt 계산 + previewSubscriptionChange 메서드 추가) |
| `src/main/java/com/atstudio/atstudio/controller/UtilController.java` | 수정 (subscription-change-preview 엔드포인트 추가) |
| `src/test/java/com/atstudio/atstudio/service/UtilServiceTest.java` | 수정 (기존 3건 nextResetAt 검증 추가 + 신규 4건) |

## 테스트 결과

- UtilServiceTest: **11건 전체 PASS** (기존 7건 + 신규 4건), 0 failures, 0 errors
- 신규 테스트 케이스:
  - `previewSubscriptionChange_upgrade` - UPGRADE 시 proratedAmount > 0, effectiveDate = today
  - `previewSubscriptionChange_downgrade` - DOWNGRADE 시 proratedAmount = 0, effectiveDate = expiresAt
  - `previewSubscriptionChange_noSubscription` - 활성 구독 없을 때 BusinessException
  - `previewSubscriptionChange_invalidBillingCycle` - 잘못된 billingCycle 시 BusinessException

## 위험 요소
- 없음. UserSubscriptionService 수정 없음, PaymentService 호출 없음.
- 전체 빌드 테스트 시 Gradle 9.x 환경 이슈 (NoSuchFileException on binary result) 발생하나 코드 변경과 무관한 인프라 이슈.

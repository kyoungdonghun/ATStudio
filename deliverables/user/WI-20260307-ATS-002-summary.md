# WI-20260307-ATS-002 요약

## 변경 요약

구독 다운그레이드 즉시 적용 버그를 수정했습니다. 기존에는 플랜 변경 시 업/다운그레이드 구분 없이 모두 즉시 적용되었으나, 이제 **다운그레이드는 예약(pending) 방식**으로 동작합니다.

### 핵심 변경

| 구분 | 변경 전 | 변경 후 |
|------|---------|---------|
| UPGRADE (고가 플랜) | 즉시 적용 + 결제 | 동일 (변경 없음) |
| DOWNGRADE (저가 플랜) | 즉시 적용 + 환불 결제 | **예약 저장, 현재 구독 유지, 결제 없음** |

### 변경 파일

| 파일 | 변경 내용 |
|------|----------|
| `UserSubscription.java` | `pendingSubscription`, `pendingBillingCycle` 필드 + `schedulePendingChange()` 메서드 추가 |
| `UserSubscriptionService.java` | `changeSubscription()` UPGRADE/DOWNGRADE 분기 로직 |
| `UserSubscriptionResponse.java` | `pendingSubscriptionId`, `pendingBillingCycle` 필드 추가 |
| `ChangeSubscriptionResponse.java` | `changeType` (UPGRADE/DOWNGRADE) 필드 추가 |
| `UserSubscriptionControllerTest.java` | 신규 record 생성자에 맞게 mock 데이터 업데이트 |
| `UserSubscriptionServiceTest.java` | UPGRADE/DOWNGRADE 분기 테스트 2건 추가 |
| `db-schema.md` | `user_subscriptions` 테이블에 `pending_subscription_id`, `pending_billing_cycle` 컬럼 추가 |

### 테스트 결과

- **전체 테스트**: BUILD SUCCESSFUL
- **changeSubscription 테스트**: 3건 (UPGRADE 즉시적용, DOWNGRADE pending, 활성구독없음) 모두 PASS
- **UserSubscriptionControllerTest**: 전체 PASS

### 리스크

- **낮음**: DB 스키마에 nullable 컬럼 2개 추가 (기존 데이터 영향 없음, ddl-auto로 자동 반영)
- **후속 작업 필요**: 다운그레이드 예약을 실제 적용하는 스케줄러(scheduled job)는 별도 REQ에서 구현 필요

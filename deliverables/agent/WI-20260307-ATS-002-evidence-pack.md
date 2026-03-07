# WI-20260307-ATS-002 Evidence Pack

## 변경 파일 및 핵심 코드

### 1. UserSubscription.java (엔티티)
**파일**: `src/main/java/com/atstudio/atstudio/entity/UserSubscription.java`
**라인**: 46-56 (신규 필드 + 메서드)

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "pending_subscription_id")
private Subscription pendingSubscription;

@Enumerated(EnumType.STRING)
@Column(name = "pending_billing_cycle", length = 10)
private BillingCycle pendingBillingCycle;

public void schedulePendingChange(Subscription pendingSub, BillingCycle cycle) {
    this.pendingSubscription = pendingSub;
    this.pendingBillingCycle = cycle;
}
```

### 2. UserSubscriptionService.java (서비스 분기)
**파일**: `src/main/java/com/atstudio/atstudio/service/UserSubscriptionService.java`
**라인**: 150-205 (changeSubscription 메서드 전체 교체)

핵심 판정 로직:
```java
boolean isUpgrade = newPlan.getPriceMonthly().compareTo(
        current.getSubscription().getPriceMonthly()) >= 0;
```

- UPGRADE: 기존 `current.upgrade()` + `paymentService.processPayment()` 유지
- DOWNGRADE: `current.schedulePendingChange(newPlan, billingCycle)` 호출, payment 없음, proratedAmount = ZERO

### 3. ChangeSubscriptionResponse.java (DTO)
**파일**: `src/main/java/com/atstudio/atstudio/dto/subscription/ChangeSubscriptionResponse.java`
**변경**: `changeType` (String) 필드 추가 (위치: `status` 다음, `proratedAmount` 이전)

### 4. UserSubscriptionResponse.java (DTO)
**파일**: `src/main/java/com/atstudio/atstudio/dto/subscription/UserSubscriptionResponse.java`
**변경**: `pendingSubscriptionId` (Long, nullable), `pendingBillingCycle` (String, nullable) 필드 추가
**from() 팩토리**: null-safe 매핑 (`getPendingSubscription() != null ? ... : null`)

### 5. UserSubscriptionControllerTest.java (테스트 수정)
**파일**: `src/test/java/com/atstudio/atstudio/controller/UserSubscriptionControllerTest.java`
**변경**: MOCK_RESPONSE에 `null, null` (pending 필드), MOCK_CHANGE_RESPONSE에 `"UPGRADE"` (changeType) 추가

### 6. db-schema.md (스키마 문서)
**파일**: `docs/design/db-schema.md`
**라인**: 156-157 (신규 컬럼 2행 추가)
**변경 전**: `expires_at` 바로 다음이 `created_at`
**변경 후**: `expires_at` -> `pending_subscription_id` -> `pending_billing_cycle` -> `created_at`

업그레이드 핸들링 설명도 UPGRADE/DOWNGRADE 분기 설명으로 업데이트.

---

## 신규 테스트 케이스

**파일**: `src/test/java/com/atstudio/atstudio/service/UserSubscriptionServiceTest.java`

| 테스트 | 검증 항목 |
|--------|----------|
| `changeSubscription_upgrade_immediate` | changeType="UPGRADE", proratedAmount=14950, paymentService 호출됨, subscription 즉시 변경 |
| `changeSubscription_downgrade_pending` | changeType="DOWNGRADE", proratedAmount=ZERO, pendingSubscription 설정됨, 현재 구독 유지, paymentService 미호출 |
| `change_noActiveSubscription` | 기존 테스트 유지 (활성 구독 없음 에러) |

### 테스트 실행 결과

```
Command: gradlew.bat test --rerun
Result: BUILD SUCCESSFUL
ChangeSubscription: 3 tests, 0 failures, 0 errors
UserSubscriptionControllerTest: 전체 PASS
```

---

## 롤백 방법

다음 파일을 git revert/checkout 대상으로 지정:
1. `src/main/java/com/atstudio/atstudio/entity/UserSubscription.java`
2. `src/main/java/com/atstudio/atstudio/service/UserSubscriptionService.java`
3. `src/main/java/com/atstudio/atstudio/dto/subscription/UserSubscriptionResponse.java`
4. `src/main/java/com/atstudio/atstudio/dto/subscription/ChangeSubscriptionResponse.java`
5. `src/test/java/com/atstudio/atstudio/service/UserSubscriptionServiceTest.java`
6. `src/test/java/com/atstudio/atstudio/controller/UserSubscriptionControllerTest.java`
7. `docs/design/db-schema.md`

---

## 후속 WI

- **WI-20260307-ATS-003** (Blocks 해제): 다운그레이드 예약 적용 스케줄러 또는 관련 후속 작업

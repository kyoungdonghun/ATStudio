[WI HEADER]
WI ID: WI-20260227-ATS-026
REQ: REQ-20260227-ATS-008
Agent: se
Depends On: -
Blocks: WI-20260227-ATS-027 (re — 테스트 실행 + 검증)

[WI SUMMARY]
Why: 6.x Subscription 10개 API 구현 — 마지막 미착수 섹션. 완료 시 79/79 (100%) 달성
Scope (in):
  - SubscriptionController (6.1, 6.2) — 플랜 조회
  - UserSubscriptionController (6.3~6.10) — 신청/조회/수정/취소
  - SubscriptionService, UserSubscriptionService
  - PaymentService 인터페이스 + MockPaymentServiceImpl (Mock 결제)
  - 관련 DTO 전체 (Request/Response records)
  - SUBSCRIPTION_ALREADY_EXISTS 에러코드 추가 (BUSINESS_ERROR.java)
  - UserSubscription에 update/cancel 메서드 추가 (업/다운그레이드, 취소용)
  - SubscriptionRepository에 필터 쿼리 추가
  - JUnit5 서비스 단위 테스트 + MockMvc 컨트롤러 테스트 작성
Scope (out):
  - 실제 PG 연동 (카카오페이, 토스 등) — PaymentService 인터페이스만 구현
  - 구독 자동 갱신 배치/스케줄러
  - 구독 플랜 자체 CRUD 관리 (플랜 생성/수정/삭제)
  - 결제 환불 처리
DoD:
  - 10개 API 정상 동작 (정상 + 예외 케이스)
  - 6.3: BUSINESS 유저 기업 인증 미완료 → COMPANY_CERTIFICATION_REQUIRED 403 반환
  - 6.3: 이미 활성 구독 존재 → SUBSCRIPTION_ALREADY_EXISTS 409 반환
  - 6.7: proratedAmount 계산값이 응답에 포함
  - Mock 결제: SubscriptionPayment 레코드 DONE 상태로 기록
  - gradlew build -x test 성공
  - 신규 서비스/컨트롤러 테스트 작성 완료
Constraints/Forbidden:
  - 기존 엔티티(Subscription, UserSubscription, SubscriptionPayment, CompanyCertification) 구조 변경 금지
    (update/cancel 메서드 추가는 허용)
  - Entity를 Controller에서 직접 반환 금지 (DTO 분리 필수)
  - @Transactional 표준: 클래스 레벨 readOnly=true, 변경 메서드만 @Transactional override
  - Java record 사용 (DTO)
  - Lombok @RequiredArgsConstructor (Service/Controller)
  - 실제 PG API 호출 코드 작성 금지

[ACCEPTANCE CRITERIA]
Functional:
  - [ ] GET /api/subscriptions → 200, 플랜 목록 반환 (userType 파라미터 필터 동작)
  - [ ] GET /api/subscriptions/{id} → 200, 플랜 상세 반환 / 없으면 404
  - [ ] POST /api/user-subscriptions (INDIVIDUAL) → 201, UserSubscription ACTIVE 생성, SubscriptionPayment DONE 기록
  - [ ] POST /api/user-subscriptions (BUSINESS + 인증 미완료) → 403 COMPANY_CERTIFICATION_REQUIRED
  - [ ] POST /api/user-subscriptions (이미 활성 구독 존재) → 409 SUBSCRIPTION_ALREADY_EXISTS
  - [ ] GET /api/user-subscriptions/me → 200, 내 활성 구독 반환 / 없으면 404
  - [ ] GET /api/user-subscriptions (ADMIN) → 200, 페이지네이션 목록
  - [ ] GET /api/user-subscriptions/{id} (ADMIN) → 200, 상세 반환 / 없으면 404
  - [ ] PUT /api/user-subscriptions/me → 200, 업/다운그레이드 + proratedAmount 포함
  - [ ] PUT /api/user-subscriptions/{id} (ADMIN) → 200, 구독 정보 수정
  - [ ] DELETE /api/user-subscriptions/{id} (ADMIN) → 204
  - [ ] DELETE /api/user-subscriptions/me → 204, status=CANCELLED
Performance:
  - N/A (현재 단계 성능 요건 없음)
Quality:
  - [ ] gradlew build -x test 성공
  - [ ] 서비스 단위 테스트 작성 (SubscriptionService, UserSubscriptionService)
  - [ ] MockMvc 컨트롤러 테스트 작성 (SubscriptionController, UserSubscriptionController)
  - [ ] @WithMockUser + CustomUserDetails mock 패턴 준수

[INPUT POINTERS]
Tier 0 (Constitution):
  - docs/standards/core-principles.md
  - docs/standards/development-standards.md

REQ/Context:
  - deliverables/user/REQ-20260227-ATS-008.md

Existing Entities (수정 대상 없음, 참조용):
  - src/main/java/com/atstudio/atstudio/entity/Subscription.java
  - src/main/java/com/atstudio/atstudio/entity/UserSubscription.java
  - src/main/java/com/atstudio/atstudio/entity/SubscriptionPayment.java
  - src/main/java/com/atstudio/atstudio/entity/CompanyCertification.java
  - src/main/java/com/atstudio/atstudio/entity/enums/SubscriptionStatus.java
  - src/main/java/com/atstudio/atstudio/entity/enums/BillingCycle.java
  - src/main/java/com/atstudio/atstudio/entity/enums/PaymentStatus.java
  - src/main/java/com/atstudio/atstudio/entity/enums/CompanyCertificationStatus.java
  - src/main/java/com/atstudio/atstudio/entity/enums/UserType.java

Existing Repositories (메서드 추가 허용):
  - src/main/java/com/atstudio/atstudio/repository/SubscriptionRepository.java
    → findAllByIsActive(boolean) 추가 필요
    → findAllByUserTypeAndIsActive(UserType, boolean) 추가 필요
  - src/main/java/com/atstudio/atstudio/repository/UserSubscriptionRepository.java
    → findActiveByUser() 이미 존재 ✅
    → Page<UserSubscription> findAll(Pageable) — JpaRepository 기본 제공 ✅
  - src/main/java/com/atstudio/atstudio/repository/SubscriptionPaymentRepository.java
  - src/main/java/com/atstudio/atstudio/repository/CompanyCertificationRepository.java
    → existsByUserAndStatusIn() 이미 존재 ✅

Error Codes (수정 대상):
  - src/main/java/com/atstudio/atstudio/common/exception/BUSINESS_ERROR.java
    → SUBSCRIPTION_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 활성 구독이 존재합니다.", "중복 구독 시도.") 추가

Global Exception Handler (참조용):
  - src/main/java/com/atstudio/atstudio/common/exception/GlobalExceptionHandler.java

기존 컨트롤러 패턴 참조:
  - src/main/java/com/atstudio/atstudio/controller/InquiryController.java
  - src/main/java/com/atstudio/atstudio/controller/CompanyCertificationController.java

기존 서비스 패턴 참조:
  - src/main/java/com/atstudio/atstudio/service/InquiryService.java
  - src/main/java/com/atstudio/atstudio/service/CompanyCertificationService.java

기존 테스트 패턴 참조:
  - src/test/java/com/atstudio/atstudio/service/InquiryServiceTest.java
  - src/test/java/com/atstudio/atstudio/controller/InquiryControllerTest.java

[BUSINESS LOGIC SPEC]

## 신규 생성 파일 목록
```
src/main/java/com/atstudio/atstudio/
├── service/
│   ├── SubscriptionService.java
│   └── UserSubscriptionService.java
├── service/payment/
│   ├── PaymentService.java           (인터페이스)
│   └── MockPaymentServiceImpl.java   (구현체)
├── controller/
│   ├── SubscriptionController.java
│   └── UserSubscriptionController.java
└── dto/
    ├── subscription/
    │   ├── SubscriptionResponse.java
    │   ├── UserSubscriptionRequest.java
    │   ├── UserSubscriptionResponse.java
    │   ├── ChangeSubscriptionRequest.java
    │   ├── ChangeSubscriptionResponse.java
    │   └── AdminUpdateSubscriptionRequest.java
    └── (필요 시 추가)

src/test/java/com/atstudio/atstudio/
├── service/
│   ├── SubscriptionServiceTest.java
│   └── UserSubscriptionServiceTest.java
└── controller/
    ├── SubscriptionControllerTest.java
    └── UserSubscriptionControllerTest.java
```

## API 별 핵심 로직

### 6.1 GET /api/subscriptions (PUBLIC)
- Query param: userType (optional)
- `subscriptionRepository.findAllByIsActive(true)` → userType 있으면 `findAllByUserTypeAndIsActive(userType, true)`
- 응답: List<SubscriptionResponse>

### 6.2 GET /api/subscriptions/{subscriptionId} (PUBLIC)
- `subscriptionRepository.findById(id)` → 없으면 `ResourceNotFoundException("SUBSCRIPTION_PLAN_NOT_FOUND")`
  (or 기존 에러코드 체계에 맞게 — BUSINESS_ERROR에 없으면 추가 또는 적절한 기존 에러 사용)
- 응답: SubscriptionResponse

### 6.3 POST /api/user-subscriptions (AUTH)
```
1. BUSINESS 유저 체크:
   if (user.getUserType() == BUSINESS) {
       boolean approved = certificationRepo.existsByUserAndStatusIn(user, List.of(APPROVED));
       if (!approved) throw new BusinessException(COMPANY_CERTIFICATION_REQUIRED);
   }
2. 중복 구독 체크:
   userSubscriptionRepo.findActiveByUser(user, ACTIVE, LocalDate.now())
   → present → throw BusinessException(SUBSCRIPTION_ALREADY_EXISTS)
3. Subscription 플랜 조회 (없으면 404)
4. Mock 결제 처리: paymentService.processPayment(user, subscription, billingCycle) → DONE
5. UserSubscription 생성 (startedAt=today, expiresAt=today+1M or +1Y)
6. SubscriptionPayment 생성 (status=DONE, amount=플랜 금액, pgTransactionId="MOCK-{UUID}")
7. 응답: 201 Created, UserSubscriptionResponse
```

### 6.4 GET /api/user-subscriptions/me (AUTH)
- `userSubscriptionRepo.findActiveByUser(user, ACTIVE, today)` → 없으면 SUBSCRIPTION_NOT_FOUND 404
- 응답: UserSubscriptionResponse

### 6.5 GET /api/user-subscriptions (ADMIN)
- `userSubscriptionRepo.findAll(pageable)` → Page<UserSubscription>
- 응답: Page<UserSubscriptionResponse>

### 6.6 GET /api/user-subscriptions/{id} (ADMIN)
- `userSubscriptionRepo.findById(id)` → 없으면 SUBSCRIPTION_NOT_FOUND 404
- 응답: UserSubscriptionResponse

### 6.7 PUT /api/user-subscriptions/me (AUTH)
```
1. 현재 활성 구독 조회 (없으면 SUBSCRIPTION_NOT_FOUND)
2. 새 플랜 조회 (없으면 404)
3. proratedAmount 계산:
   remainingDays = ChronoUnit.DAYS.between(today, current.getExpiresAt())
   totalDays = ChronoUnit.DAYS.between(current.getStartedAt(), current.getExpiresAt())
   currentRate = current.getBillingCycle() == MONTHLY ? current.getSubscription().getPriceMonthly() : getPriceYearly()
   refundAmount = currentRate.multiply(BigDecimal.valueOf(remainingDays)).divide(BigDecimal.valueOf(totalDays), 2, HALF_UP)
   newPlanPrice = billingCycle == MONTHLY ? newPlan.getPriceMonthly() : newPlan.getPriceYearly()
   proratedAmount = newPlanPrice.subtract(refundAmount) — 음수 가능 (다운그레이드 시 환불)
4. current.upgrade(newSubscription, billingCycle, newExpiresAt)  (UserSubscription에 update 메서드 추가)
5. Mock 결제 기록 (proratedAmount)
6. 응답: ChangeSubscriptionResponse (subscription, billingCycle, status, proratedAmount, startedAt, expiresAt)
```

### 6.8 PUT /api/user-subscriptions/{id} (ADMIN)
- ID로 UserSubscription 조회 (없으면 SUBSCRIPTION_NOT_FOUND)
- AdminUpdateSubscriptionRequest의 status, billingCycle, expiresAt 등 업데이트
- UserSubscription에 adminUpdate 메서드 추가

### 6.9 DELETE /api/user-subscriptions/{id} (ADMIN)
- ID로 UserSubscription 조회 (없으면 SUBSCRIPTION_NOT_FOUND)
- us.cancel() → status = CANCELLED
- 204 No Content

### 6.10 DELETE /api/user-subscriptions/me (AUTH)
- `findActiveByUser(user, ACTIVE, today)` → 없으면 SUBSCRIPTION_NOT_FOUND 404
- us.cancel() → status = CANCELLED
- 204 No Content

## UserSubscription 추가 메서드 (엔티티 수정)
```java
// 업그레이드/다운그레이드
public void upgrade(Subscription newSubscription, BillingCycle newBillingCycle, LocalDate newExpiresAt) {
    this.subscription = newSubscription;
    this.billingCycle = newBillingCycle;
    this.expiresAt = newExpiresAt;
}

// 취소
public void cancel() {
    this.status = SubscriptionStatus.CANCELLED;
}

// 관리자 수정
public void adminUpdate(SubscriptionStatus newStatus, BillingCycle newBillingCycle, LocalDate newExpiresAt) {
    if (newStatus != null) this.status = newStatus;
    if (newBillingCycle != null) this.billingCycle = newBillingCycle;
    if (newExpiresAt != null) this.expiresAt = newExpiresAt;
}
```

## PaymentService 설계
```java
// PaymentService.java (interface)
public interface PaymentService {
    SubscriptionPayment processPayment(User user, UserSubscription userSubscription,
                                       Subscription subscription, BillingCycle billingCycle, BigDecimal amount);
}

// MockPaymentServiceImpl.java
@Service
@Primary
public class MockPaymentServiceImpl implements PaymentService {
    private final SubscriptionPaymentRepository paymentRepository;

    @Override
    public SubscriptionPayment processPayment(User user, UserSubscription userSubscription,
                                               Subscription subscription, BillingCycle billingCycle, BigDecimal amount) {
        return paymentRepository.save(SubscriptionPayment.builder()
                .user(user)
                .userSubscription(userSubscription)
                .subscription(subscription)
                .billingCycle(billingCycle)
                .amount(amount)
                .paymentStatus(PaymentStatus.DONE)
                .pgTransactionId("MOCK-" + UUID.randomUUID())
                .build());
    }
}
```

[OUTPUT CONTRACT]
User-facing  → deliverables/user/WI-20260227-ATS-026-summary.md
Agent-facing → deliverables/agent/WI-20260227-ATS-026-evidence-pack.md
Handoff      → deliverables/agent/WI-20260227-ATS-026-handoff.md (this file)

[TRACEABILITY REQUIREMENTS]
Evidence pointers:
  - 생성/수정된 파일 목록 및 라인 범위
  - 각 API 엔드포인트에 대한 테스트 케이스 목록
  - `gradlew build -x test` 결과 스니펫
  - 추가된 에러코드 (SUBSCRIPTION_ALREADY_EXISTS) 정의 라인

Tests:
  - 명령어: ./gradlew test --tests "com.atstudio.atstudio.service.SubscriptionServiceTest"
  - 명령어: ./gradlew test --tests "com.atstudio.atstudio.service.UserSubscriptionServiceTest"
  - 명령어: ./gradlew test --tests "com.atstudio.atstudio.controller.SubscriptionControllerTest"
  - 명령어: ./gradlew test --tests "com.atstudio.atstudio.controller.UserSubscriptionControllerTest"

Rollback:
  - 신규 파일 삭제로 완전 롤백 가능 (기존 파일은 BUSINESS_ERROR.java, UserSubscription.java, SubscriptionRepository.java만 수정)
  - 위 3개 파일은 git diff로 추적 가능

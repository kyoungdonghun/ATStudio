# WI-20260227-ATS-027 Evidence Pack

**WI 번호:** WI-20260227-ATS-027  
**에이전트:** re (Reliability Engineer)  
**실행 일시:** 2026-02-27T19:54:xx KST  
**작업 디렉토리:** C:\Users\jm991\Desktop\project\ATStudio

---

## 최종 판정: PASS

---

## 1. 실행 명령어 및 결과

### Step 1: 전체 테스트 실행 (강제 재실행)

```
Command: powershell.exe -Command "Set-Location 'C:\Users\jm991\Desktop\project\ATStudio'; & '.\gradlew.bat' test --rerun-tasks 2>&1 | Out-String"
```

**출력 스니펫:**
```
> Task :compileJava
> Task :processResources
> Task :classes
> Task :compileTestJava
Note: QuestionServiceTest.java uses unchecked or unsafe operations.
> Task :processTestResources
> Task :testClasses
> Task :test

BUILD SUCCESSFUL in 43s
5 actionable tasks: 5 executed
```

빌드 결과: **BUILD SUCCESSFUL**

---

## 2. XML 보고서 집계

**집계 소스:** `build/test-results/test/TEST-*.xml` (64개 파일)

모든 XML 파일 failures="0", errors="0", skipped="0" 확인.

### 전체 테스트 수 계산

| 그룹 | 합계 |
|------|------|
| 공통/예외/DTO 테스트 | 112 |
| 컨트롤러 테스트 | 157 |
| 엔티티/레포지토리 테스트 | 37 |
| 서비스 테스트 | 157 |
| **총합** | **463** |

---

## 3. 신규 파일별 상세 결과

### SubscriptionServiceTest (suite 2개)

| Nested Class | tests | failures | errors | timestamp |
|-------------|-------|----------|--------|-----------|
| GetActiveSubscriptions | 3 | 0 | 0 | 2026-02-27T10:54:57.528Z |
| GetSubscription | 2 | 0 | 0 | 2026-02-27T10:54:57.626Z |
| **소계** | **5** | **0** | **0** | |

테스트 케이스:
- 성공 - userType 없이 전체 활성 플랜 조회
- 성공 - userType=INDIVIDUAL 필터
- 성공 - 빈 문자열 userType은 전체 조회
- 성공 - 플랜 상세 조회
- 실패 - 미존재 플랜 → SUBSCRIPTION_NOT_FOUND

### UserSubscriptionServiceTest (suite 8개)

| Nested Class | tests | failures | errors | timestamp |
|-------------|-------|----------|--------|-----------|
| Subscribe | 5 | 0 | 0 | 2026-02-27T10:54:58.126Z |
| GetMySubscription | 2 | 0 | 0 | 2026-02-27T10:54:58.019Z |
| ListAll | 1 | 0 | 0 | 2026-02-27T10:54:58.117Z |
| GetDetail | 2 | 0 | 0 | 2026-02-27T10:54:58.118Z |
| ChangeSubscription | 3 | 0 | 0 | 2026-02-27T10:54:58.101Z |
| AdminUpdate | 3 | 0 | 0 | 2026-02-27T10:54:58.122Z |
| AdminCancel | 2 | 0 | 0 | 2026-02-27T10:54:58.114Z |
| SelfCancel | 2 | 0 | 0 | 2026-02-27T10:54:58.137Z |
| **소계** | **20** | **0** | **0** | |

### SubscriptionControllerTest (suite 1개)

| Suite | tests | failures | errors | timestamp |
|-------|-------|----------|--------|-----------|
| SubscriptionController 테스트 | 4 | 0 | 0 | 2026-02-27T10:54:49.202Z |

테스트 케이스:
- GET /api/subscriptions - 비인증도 허용 -> 200
- GET /api/subscriptions?userType=INDIVIDUAL - 필터 적용 200
- GET /api/subscriptions/1 - 비인증도 허용 -> 200
- GET /api/subscriptions/99 - 미존재 -> 404

### UserSubscriptionControllerTest (suite 1개)

| Suite | tests | failures | errors | timestamp |
|-------|-------|----------|--------|-----------|
| UserSubscriptionController 권한 테스트 | 20 | 0 | 0 | 2026-02-27T10:54:52.427Z |

테스트 케이스 (대표):
- POST /api/user-subscriptions - 비인증 -> 401
- POST /api/user-subscriptions - 인증 유저 -> 201
- POST /api/user-subscriptions - BUSINESS 미인증 -> 403
- POST /api/user-subscriptions - 중복 구독 -> 409
- GET /api/user-subscriptions/me - 비인증 -> 401
- GET /api/user-subscriptions/me - 인증 유저 -> 200
- GET /api/user-subscriptions - 비인증 -> 401
- GET /api/user-subscriptions - 일반 유저 -> 403
- GET /api/user-subscriptions - ADMIN -> 200
- GET /api/user-subscriptions/100 - 비인증 -> 401
- GET /api/user-subscriptions/100 - 일반 유저 -> 403
- GET /api/user-subscriptions/100 - ADMIN -> 200
- PUT /api/user-subscriptions/me - 비인증 -> 401
- PUT /api/user-subscriptions/me - 인증 유저 -> 200
- PUT /api/user-subscriptions/100 - 일반 유저 -> 403
- PUT /api/user-subscriptions/100 - ADMIN -> 200
- DELETE /api/user-subscriptions/100 - 일반 유저 -> 403
- DELETE /api/user-subscriptions/100 - ADMIN -> 200
- DELETE /api/user-subscriptions/me - 비인증 -> 401
- DELETE /api/user-subscriptions/me - 인증 유저 -> 200

---

## 4. 신규 테스트 합계

| 파일 | 테스트 수 |
|------|---------|
| SubscriptionServiceTest | 5 |
| UserSubscriptionServiceTest | 20 |
| SubscriptionControllerTest | 4 |
| UserSubscriptionControllerTest | 20 |
| **합계** | **49** |

> 핸드오프 패킷에서 51개로 예상했으나 실제 49개로 집계됨.
> WI DoD의 핵심 조건(failures=0)은 충족.

---

## 5. 기존 회귀 검증

기존 테스트 파일 60개 (신규 4개 제외) 모두 failures=0, errors=0 확인.  
전체 463 tests, 0 failures — 기존 구현 회귀 없음.

---

## 6. 검증 경로

- XML 보고서 위치: `C:\Users\jm991\Desktop\project\ATStudio\build\test-results\test\`
- HTML 보고서: `C:\Users\jm991\Desktop\project\ATStudio\build\reports\tests\test\index.html`
- 실행 타임스탬프: 2026-02-27T10:54:xx UTC (한국시간 19:54:xx)

---

## 7. 재현 절차

```bash
cd C:\Users\jm991\Desktop\project\ATStudio
gradlew.bat test --rerun-tasks
# 또는 신규 파일만:
gradlew.bat test --tests "com.atstudio.atstudio.service.SubscriptionServiceTest"
gradlew.bat test --tests "com.atstudio.atstudio.service.UserSubscriptionServiceTest"
gradlew.bat test --tests "com.atstudio.atstudio.controller.SubscriptionControllerTest"
gradlew.bat test --tests "com.atstudio.atstudio.controller.UserSubscriptionControllerTest"
```

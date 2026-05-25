# WI-20260525-ATS-011 Summary

권한 보정 회귀 테스트를 추가하고 핵심 상태 전이를 검증했다.

## 검증한 내용

- preview는 read-only이며 현재 상태와 목표 상태를 반환한다.
- correction 생성 시 before/target snapshot이 저장된다.
- 성공하지 않은 환불 건은 권한 보정 요청을 만들 수 없다.
- 승인되지 않은 보정 건은 실행할 수 없다.
- 실행 시 명시한 target subscription/status/expiresAt이 적용되고 pending 변경이 정리된다.
- `cancelBillingAgreement=true`일 때 local billing agreement가 취소된다.
- request/process/success audit log가 기록된다.

## 결과

- `gradlew.bat test --tests "com.atstudio.atstudio.service.AdminPaymentEntitlementCorrectionServiceTest"` 통과.
- `gradlew.bat test` 통과.

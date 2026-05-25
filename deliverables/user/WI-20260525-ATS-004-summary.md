# WI-20260525-ATS-004 Summary

환불 기능의 설계 경계를 확정했다. 핵심 방향은 "환불은 사용자 구독 취소와 다르며, 관리자 승인 기반의 별도 금전 작업"이라는 점이다.

## 완료 사항

- 환불 요청, 승인, 실행, 조회, 미리보기 API 경계를 정했다.
- 전액/부분 환불은 모두 `payment_refunds` 원장에 누적 기록하고, 이미 요청/승인/처리/성공/확인대기 중인 금액은 남은 환불 가능액에서 차감하도록 정했다.
- Toss cancel 실행은 provider idempotency key를 먼저 저장한 뒤 수행하도록 확정했다.
- 환불 성공은 구독 권한을 자동 수정하지 않으며, entitlement correction은 별도 후속 작업으로 분리했다.
- raw card, billing key, authKey, customerKey, Toss secret, raw provider payload 비노출 원칙을 유지했다.

## 판단

이번 WI는 구현 전 설계 경계 확정 단계였고, 이후 WI-005 구현 범위의 기준이 되었다.

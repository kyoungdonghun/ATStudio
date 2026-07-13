# WI-20260713-ATS-010 결제 트랜잭션 검토 요약

## 판정

- 탈퇴 트랜잭션은 Toss 호출 전에 로컬 agreement와 ACTIVE subscription을 취소하고, 트랜잭션 커밋 뒤에만 빌링키 삭제를 시도한다.
- Toss 실패나 예외는 사용자의 탈퇴와 로컬 갱신 차단을 되돌리지 않는다.
- 실패한 빌링키 정리는 삭제 사용자·CANCELLED agreement·남은 암호문 조건으로만 매일 재시도된다.
- Toss가 `ALREADY_REMOVED_BILLING_KEY`를 반환하면 외부 삭제가 완료된 것으로 보아 로컬 키를 지우고 Incident를 해결한다.
- due renewal 쿼리와 서비스 내부 가드가 모두 탈퇴 사용자를 차단하므로 Provider 과금 호출은 0회다.

## 검증

- 결제·탈퇴 집중 테스트는 통합 스위트에 포함되어 모두 통과했다.
- 새 테이블·컬럼·ENUM은 없고 기존 `LOCAL_DONE_PROVIDER_NOT_DONE` Incident를 재사용한다.
- 자동 환불과 live Toss 호출은 수행하지 않았다.

## 잔여 위험

- 현재 운영 전제와 동일하게 단일 서버 스케줄러다.
- DB 자체 장애로 Incident 기록도 실패한 경우에는 다음 일일 후보 조회가 재시도를 담당한다.

# WI-20260519-ATS-002 Summary

- 백엔드 플랜 업그레이드를 단건 결제 대신 `TOSS_BILLING` recurring charge 기반으로 변경했다.
- 업그레이드 차액은 현재 결제 주기의 남은 기간 기준으로 계산하고, 결제 성공 후에만 새 플랜을 즉시 적용한다.
- 업그레이드 성공 시 기존 다음 결제일은 유지하며, 사용자가 선택한 billingCycle은 다음 갱신 결제에 반영되도록 했다.
- 활성 billing agreement가 없거나 recurring charge가 실패하면 구독 상태가 바뀌지 않도록 테스트를 보강했다.

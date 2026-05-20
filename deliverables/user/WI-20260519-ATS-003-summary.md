# WI-20260519-ATS-003 Summary

- 프론트엔드 플랜 변경 흐름에서 업그레이드 단건 결제 페이지 이동을 제거했다.
- 플랜 변경은 preview 확인 후 `PUT /api/user-subscriptions/me` 호출로 확정되며, 업그레이드는 등록된 결제수단으로 즉시 차액 결제되는 메시지를 보여준다.
- `/subscriptions/payment?purpose=UPGRADE` 직접 진입은 단건 결제를 준비하지 않고 내 구독 화면에서 진행하라는 안내로 막았다.
- 관련 Vitest 테스트를 갱신했다.

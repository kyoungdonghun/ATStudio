# WI-20260517-ATS-002 Summary

Frontend 결제 화면에 Toss 단건 결제 흐름을 붙였습니다.

- Mock 결제는 기존처럼 버튼으로 성공/실패/취소를 확인합니다.
- Toss provider일 때는 Toss V2 widget을 로드하고 `토스 결제창 열기`로 결제를 시작합니다.
- Toss success redirect는 `paymentKey/orderId/amount`로 backend confirm을 호출합니다.
- Toss fail redirect는 주문을 실패 처리하고 재시도 경로를 보여줍니다.
- 업그레이드는 중앙 결제 페이지로 이동해 Mock/Toss를 같은 방식으로 처리합니다.

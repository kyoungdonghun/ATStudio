# WI-20260517-ATS-001 Summary

Backend Toss 단건 결제 provider를 추가했습니다.

- 기본 결제 provider는 계속 `MOCK`입니다.
- `APP_PAYMENT_PROVIDER=TOSS`로 바꾸면 Toss checkout metadata를 반환합니다.
- Toss secret key는 서버 환경변수로만 사용합니다.
- Toss provider를 켰는데 client/secret key가 없으면 결제 주문을 만들지 않고 설정 오류로 막습니다.
- 서버 confirm은 저장된 주문 금액을 기준으로 Toss confirm API를 호출합니다.
- Toss provider 단위 테스트와 전체 Gradle 테스트를 통과했습니다.

# WI-20260714-ATS-015 완료 요약

## 작업 결과

- `acceptance` 프로필을 추가하고 Spring을 `127.0.0.1`에 바인딩했습니다.
- acceptance 환경의 DB/JWT/bootstrap/Toss 비밀 값은 환경변수 placeholder로만 받도록 구성했습니다.
- `APP_PUBLIC_BASE_URL` 하나에서 메일, 소셜 로그인, Toss 일회성 결제, Toss 빌링 인증 callback을 파생하도록 구성했습니다.
- 가장 먼저 실행되는 startup guard가 profile, 외부 설정, public base URL, callback 경로, Toss 설정을 검증하며 거부 메시지에는 설정 값을 포함하지 않습니다.
- bootstrap 기본 비밀번호와 `PaymentProperties`의 localhost callback 기본값을 제거했습니다.
- 로컬 bootstrap 예시는 명시적 `local` 프로필과 외부 bootstrap 비밀번호를 요구하도록 변경했습니다.
- Vite/proxy 파일, 서버·터널, live Toss/SMTP/DB는 건드리거나 실행하지 않았습니다.

## 파생 Callback

`APP_PUBLIC_BASE_URL=https://<acceptance-host>` 기준입니다.

| 용도 | 파생 값 |
|---|---|
| 메일 링크 base | `https://<acceptance-host>` |
| Google 소셜 로그인 | `https://<acceptance-host>/social-login/google` |
| Kakao 소셜 로그인 | `https://<acceptance-host>/social-login/kakao` |
| Naver 소셜 로그인 | `https://<acceptance-host>/social-login/naver` |
| Toss 일회성 결제 성공 | `https://<acceptance-host>/subscriptions/payment/success` |
| Toss 일회성 결제 실패 | `https://<acceptance-host>/subscriptions/payment/fail` |
| Toss 빌링 인증 성공 | `https://<acceptance-host>/subscriptions/checkout/success` |
| Toss 빌링 인증 실패 | `https://<acceptance-host>/subscriptions/checkout/fail` |

## 시작 거부 조건

- production 계열 프로필에서 acceptance 또는 test-user bootstrap이 활성화된 경우
- `acceptance` 프로필 없이 acceptance 플래그만 활성화하거나, acceptance 프로필에서 플래그를 끈 경우
- 명시적 non-production 프로필 없이 test-user bootstrap을 활성화한 경우
- DB URL/사용자/비밀번호 또는 JWT secret이 외부에서 제공되지 않은 경우
- bootstrap 활성화 시 외부 bootstrap 비밀번호가 비어 있는 경우
- public base URL이 HTTPS 절대 origin이 아니거나 userinfo, trailing slash/path, query, fragment를 포함한 경우
- 메일, 소셜 로그인, Toss callback이 public base URL의 승인 경로와 다른 경우
- Toss 모드에서 client key, secret key, billing-key 암호화 secret이 비어 있는 경우

## 검증 결과

- `compileJava`: 통과
- `compileTestJava`: 통과
- 집중 테스트: 3개 클래스, 17개 테스트 통과, config assertion 실패 0건
- secret scan: acceptance secret-bearing 필드는 placeholder만 사용하며 기존 bootstrap fallback literal은 제거됐습니다.
- `git diff --check`: 최종 실행 결과는 Evidence Pack에 기록했습니다.

## 체인 트리거

- `WI-20260714-ATS-016`: WI-015의 `APP_PUBLIC_BASE_URL`/public-host 파생 계약을 소비하도록 다음 작업으로 트리거합니다.
- `WI-20260714-ATS-017`: WI-016 완료 직후 WI-015/016 Evidence Pack을 입력으로 lifecycle automation 작업을 트리거합니다.

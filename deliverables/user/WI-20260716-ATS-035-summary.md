# WI-20260716-ATS-035 프론트엔드 잔여 코드 전수조사 요약

## 결론

프론트엔드 V1 정리 후보를 22개 판정 단위로 조사했습니다.

| 판정 | 개수 | 의미 |
|---|---:|---|
| KEEP | 8 | 현재 기능 또는 보안·장애 대응에 필요하므로 유지 |
| REMOVE | 7 | 현재 실행 경로와 호출자가 없어 제거 가능 |
| REPLACE | 6 | 단독 삭제하면 계약이나 사용자 경로가 깨지므로 현재 방식으로 치환 |
| ARCHIVE | 0 | 프론트엔드 실행 자산 중 별도 보관할 대상 없음 |
| REVIEW | 1 | V1 버전 표기 정책 결정 필요 |

이번 WI는 읽기 전용 조사입니다. 제품 코드, 의존성, Git 상태, DB, 실행 서버는 변경하지 않았습니다.

## 우선 제거 가능한 항목

다음 항목은 현재 프론트엔드에서 실제 호출되지 않는다는 근거가 확인됐습니다.

1. 구형 다운로드 큐 API 모듈과 타입
   - `frontend/src/api/downloadQueue.ts`
   - `frontend/src/types/index.ts`의 `DownloadQueueItem`
   - 현재 화면은 `GET /api/downloads/history` 계열만 사용합니다.
2. 사용하지 않는 서버 재생기록 API 모듈과 타입
   - `frontend/src/api/playHistory.ts`
   - `frontend/src/types/index.ts`의 `PlayHistory`
   - 현재 재생기록은 브라우저 `localStorage`가 기준입니다.
3. 사용되지 않는 공용 `DataTable` 컴포넌트와 CSS
4. 호출자가 없는 API 함수 5개와 사용되지 않는 `PaymentProvider` 타입
   - `fetchUser`
   - `fetchSubscriptionPlanDetail`
   - `fetchAdminUserSubscriptionDetail`
   - `addTracksToPlaylistBatch`
   - `cancelMyBillingAgreement`
5. 내부에서 생성하는 링크가 없는 `/playlists/new` 호환 라우트와 `PlaylistCreatePage`
6. 빈 디렉터리 유지용 `.gitkeep` 3개 (`features`, `hooks`, `public`)
7. 생성된 Vite 로그 2개

## 함께 바꿔야 하는 항목

다음은 파일 하나만 지우면 안 됩니다.

1. **구형 결제 라우트 5개**
   - `/subscriptions/payment`
   - `/subscriptions/payment/success`
   - `/subscriptions/payment/fail`
   - `/subscriptions/billing/success`
   - `/subscriptions/billing/fail`
   - 현재 내부 링크는 `/subscriptions/checkout`만 생성하지만, 백엔드 acceptance callback 설정이 구형 payment 성공·실패 URL을 아직 참조합니다. 백엔드 제거와 동시에 라우트·분기·테스트·문서를 정리해야 합니다.
2. **다운로드 기록 이름 통일**
   - 화면 기능은 이미 다운로드 기록인데 URL과 파일명이 `/download-queue`, `DownloadQueuePage`로 남아 있습니다.
   - `/downloads`, `DownloadHistoryPage`로 바꾸려면 Header, Profile, 라우터, 테스트, 문서를 한 번에 수정해야 합니다.
3. **관리자 결제 화면 이름 통일**
   - `PaymentReadOnlyPage`는 현재 환불, 정산 import, 권한 보정 등 변경 작업도 수행합니다.
   - 실제 역할에 맞게 `PaymentOperationsPage` 계열 이름으로 바꾸는 것이 정확합니다.
4. **기본 브라우저 확인창 교체**
   - 운영 결제 화면 7곳, 관리자 화이트리스트 2곳, 사용자 화이트리스트 1곳에서 `window.confirm`을 사용합니다.
   - 프로젝트 표준과 기존 `ConfirmDialog` 패턴에 맞춰 교체해야 합니다.
5. **결제 화면의 `mock*` CSS 이름 정리**
   - 실제 화면은 Toss 자동결제인데 CSS 이름과 주석이 과거 Mock 결제 명칭을 유지합니다.
6. **`tsconfig.tsbuildinfo` Git 추적 종료**
   - 이미 `.gitignore`에는 `*.tsbuildinfo`가 있으나 해당 파일은 과거에 추적된 상태라 빌드마다 변경으로 나타납니다.
   - 승인된 정리 단계에서 Git 추적만 종료하고 로컬 생성 파일은 빌드 캐시로 두는 것이 맞습니다.

## 삭제하면 안 되는 것으로 확인한 항목

- Cloudflare 공개 접속용 Vite `acceptanceMode` 설정은 임시 인증 우회가 아닙니다. 허용 Host를 제한하고 외부 forwarding header를 제거한 뒤 검증한 IP 하나만 백엔드에 전달하는 보안 경계입니다.
- `oauthAttempt.ts`는 이름 검색에서 `temp`와 비슷하게 잡힐 수 있지만 OAuth state, PKCE, 안전한 내부 복귀 경로를 검증하는 현재 보안 코드입니다.
- `safeStorage`, API 오류 분류, 요청 취소·generation fence, 회사 인증 안내 fallback, 파형 데이터가 없을 때의 평면 파형은 실제 브라우저·네트워크 실패를 처리하는 방어 코드입니다.
- 현재 `/subscriptions/checkout` 정기결제, 결제수단 재등록, 인증·구독 라우트 가드, 화이트리스트·기업인증 상태 전이는 모두 실제 호출 경로가 확인됐습니다.
- `frontend/src/test/setup.ts`는 production import graph에서는 도달하지 않지만 Vitest `setupFiles`로 사용됩니다.
- `ErrorPage.module.css`는 같은 이름의 TSX가 없어도 404와 서버 오류 화면 두 곳에서 공유합니다.

## 승인 시 권장 순서

1. 호출이 끊긴 모듈·컴포넌트·함수·placeholder·생성 로그를 먼저 제거합니다.
2. 백엔드 잔여 코드 판정과 맞춰 구형 결제 callback 및 서버 재생기록·다운로드 큐 계약을 함께 제거합니다.
3. 다운로드 기록과 관리자 결제 운영 화면의 이름을 현재 역할에 맞게 바꿉니다.
4. `window.confirm`과 Mock 명칭을 현재 UI 패턴으로 교체합니다.
5. `tsconfig.tsbuildinfo` 추적을 종료하고 Vite 로그 ignore 규칙을 추가합니다.
6. 프론트 전체 typecheck, ESLint, Prettier, Vitest, build와 역할별 화면 스모크 테스트를 수행합니다.

## 별도 결정 필요

`frontend/package.json`은 아직 `version: 0.1.0`입니다. V1 공식 기준선 확정과 동시에 `1.0.0`으로 올릴지, 내부 private 패키지라 버전을 유지할지는 릴리스 정책 결정이 필요합니다. 실행 기능에는 영향을 주지 않습니다.

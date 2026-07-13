# WI-20260711-ATS-003 Frontend QA Summary

## TL;DR

- 판정: **FAIL - P1 2건을 수정하고 회귀 검증하기 전에는 프론트엔드 감사 게이트를 통과할 수 없습니다.**
- 우선순위: P0 0건 / P1 2건 / P2 10건 / P3 2건
- 가장 큰 위험은 (1) 소셜 로그인 콜백의 토큰 누락, (2) ADMIN 계정의 회원용 구독·정기결제 경로 진입입니다.
- `npm run typecheck`와 `npm run lint`는 통과했습니다. `npm run format`은 143개 파일에서 실패했습니다.
- 이번 WI는 정적·읽기 전용 감사입니다. 프론트엔드 코드, 기존 사용자 파일, 결제/관리자 상태는 변경하지 않았습니다.

## Audit Coverage

- 활성 React SPA: `frontend/src/`, `frontend/package.json`, `frontend/vite.config.ts`
- 라우트: `frontend/src/router/index.tsx`의 `path:` 선언 62개(부모 레이아웃, 오류·별칭 경로 포함)
- 화면: page component 54개, 테스트 파일 14개, page test 파일 9개
- 역할: GUEST / USER / ADMIN 및 INDIVIDUAL / BUSINESS 사용자 유형
- 기능: 인증, 음원/검색, 앨범, 재생목록, 다운로드, 라이선스, 프로필, 구독/결제, 화이트리스트, 기업 인증, 문의/공지, 관리자 전 화면

## Top Findings

| ID     | 우선순위 | 발견사항                                                                               | 사용자 영향 및 근거                                                                                                                                                                                                                                                                                                                                                                                 |
| ------ | -------- | -------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| FE-001 | P1       | 소셜 로그인 콜백이 새 access token을 저장하기 전에 `/users/me`를 토큰 없이 호출합니다. | 활성화된 OAuth 공급자의 신규 로그인 완료가 401로 끝날 수 있습니다. `SocialLoginPage.tsx:44-47`, `auth.ts:99-103`, `authStore.ts:36-46`; 일반 로그인은 `LoginPage.tsx:127-142`에서 토큰을 명시적으로 전달합니다.                                                                                                                                                                                     |
| FE-002 | P1       | ADMIN의 회원용 구독/결제 격리가 URL, 화면, 백엔드에서 완결되지 않았습니다.             | `/subscriptions`는 공개이고 checkout은 ADMIN도 통과하는 `authRequired`입니다. 플랜 선택 후 정기결제 주문 준비까지 진행됩니다. `router/index.tsx:130,153-161`, `ProtectedRoute.tsx:7-10`, `SubscriptionPlanPage.tsx:162-178`, `SubscriptionPaymentPage.tsx:122-150`, `BillingAgreementApplicationService.java:109-120`. 이는 ADMIN 직접 접근 차단을 요구한 `docs/SR/SR-28.md:13,25-27`과 충돌합니다. |
| FE-003 | P2       | 구독 조회의 404/도메인 오류와 네트워크·5xx 오류를 같은 “비구독” 상태로 처리합니다.     | 일시 장애 때 구독자가 플랜 화면으로 밀리거나 구독 CTA를 볼 수 있습니다. `SubscriberRoute.tsx:25-33,52-57`, `PlayerBar.tsx:80-88`, `SubscriptionPlanPage.tsx:141-147`, `WhitelistChannelPage.tsx:78-84`. `SubscriptionManagePage.tsx:235-242`의 error-code 분리가 재사용 가능한 올바른 패턴입니다.                                                                                                   |
| FE-004 | P2       | INDIVIDUAL 회원이 기업 인증 신청 폼까지 진입한 뒤 제출 시 서버에서 거절됩니다.         | 404를 “신청 없음”으로 보고 폼을 노출하지만 서버는 BUSINESS가 아니면 차단합니다. `CompanyCertApplyPage.tsx:53-87,133-149`, `CompanyCertificationService.java:60-66`. 클라이언트 기준 `docs/client/2-full-feature-checklist.md:193-194`도 명확한 차단/안내를 요구합니다.                                                                                                                              |
| FE-005 | P2       | 검색·필터와 관리자 결제 탭 요청에 취소 또는 최신 요청 식별자가 없습니다.               | 느린 이전 응답이 새 검색 조건이나 새 탭의 loading/page 상태를 덮을 수 있습니다. `TrackListPage.tsx:151-208`, `PaymentReadOnlyPage.tsx:179-248`.                                                                                                                                                                                                                                                     |
| FE-006 | P2       | 보호 화면에서 로그인 후 원래 경로로 복귀하지 않습니다.                                 | 가드는 `/login`만 전달하고 로그인 성공은 항상 `/`로 이동합니다. `ProtectedRoute.tsx:37-42`, `SubscriberRoute.tsx:36-57`, `LoginPage.tsx:77-79,127-145`. `screen-flow.md:72,402`의 returnUrl 계약과 불일치합니다.                                                                                                                                                                                    |
| FE-007 | P2       | 프로필 저장 후 전역 인증 사용자와 localStorage가 갱신되지 않습니다.                    | 닉네임 변경 성공 후 Header와 새로고침 결과가 이전 값을 계속 표시합니다. `ProfilePage.tsx:216-224`, `Header.tsx:80-83,162-166`, `authStore.ts:22-29,36-46`.                                                                                                                                                                                                                                          |
| FE-008 | P2       | 공용 UI의 키보드/스크린리더 지원이 불완전합니다.                                       | Toast는 클릭 가능한 `div`이고 live region이 없으며, Pagination 화살표에는 접근 가능한 이름과 `aria-current`가 없습니다. Header 검색도 label이 없습니다. `ToastContainer.tsx:18-27`, `Pagination.tsx:30-62`, `Header.tsx:123-132,233-242`, `PlayerBar.tsx:498-516`. Pagination은 16개 화면에서 재사용됩니다.                                                                                         |
| FE-009 | P2       | Add-to-playlist 모달의 비동기 요청과 성공 타이머가 정리되지 않습니다.                  | 빠르게 닫고 다시 열면 이전 요청이 새 모달에 stale 목록을 표시하거나 이전 800ms 타이머가 새 모달을 닫을 수 있습니다. `AddToPlaylistModal.tsx:28-53,55-75`.                                                                                                                                                                                                                                           |
| FE-010 | P2       | 오디오 재생 실패를 삼킨 뒤 `isPlaying=true`로 표시합니다.                              | 스트림 오류나 브라우저 재생 거절 때 소리는 없지만 재생 아이콘/상태는 재생 중으로 남습니다. `playerStore.ts:173-200`.                                                                                                                                                                                                                                                                                |
| FE-011 | P2       | `/playlists/new`는 생성 UI를 열지 않고 목록으로만 되돌립니다.                          | 라우트의 생성 화면 계약이 사실상 no-op입니다. `router/index.tsx:145`, `PlaylistCreatePage.tsx:4-9`, `PlaylistListPage.tsx:25,84`; 화면 목록은 생성 화면을 명시합니다(`atstudio-front-list.md:54`).                                                                                                                                                                                                  |
| FE-012 | P2       | 주요 조회 실패 화면에 재시도 명령이 없고 기술적/영문 문구가 섞여 있습니다.             | 일시 오류에서 새로고침 외 복구가 어렵습니다. `TrackListPage.tsx:173-180,491-497`, `DashboardPage.tsx:12-34,37-78`, `NoticeListPage.tsx:33-58`.                                                                                                                                                                                                                                                      |
| FE-013 | P3       | 현재 코드와 화면/API 문서가 일부 어긋납니다.                                           | 재생 기록은 localStorage 기반인데 API 화면으로 기록되어 있고, 관리자 통계 API는 구현됐지만 “미정의”로 남아 있습니다. `PlayHistoryPage.tsx:1,31-37,61-77`, `atstudio-front-list.md:65,137`, `AdminStatsController.java:13-27`. 라우트 카운트도 `router/index.tsx:117`, `frontend-standards.md:305`, `atstudio-front-list.md:161`이 서로 다릅니다.                                                    |
| FE-014 | P3       | 포맷·구조·확인 UI의 유지보수 기준이 깨져 있습니다.                                     | Prettier 143개 파일 실패, `window.confirm/prompt` 사용, 대형 단일 컴포넌트가 확인됐습니다. `PaymentReadOnlyPage.tsx` 1,945줄, `SubscriptionManagePage.tsx` 878줄, `PlayerBar.tsx` 620줄이며 `frontend-standards.md:147`은 native confirm을 금지합니다.                                                                                                                                              |

## Role And Screen Result

| 역할       | 현재 가드                                                                      | 결과                                                                                   |
| ---------- | ------------------------------------------------------------------------------ | -------------------------------------------------------------------------------------- |
| GUEST      | public/auth 화면만 직접 접근, 보호 화면은 `/login` 이동                        | 기본 차단은 동작하지만 원래 경로가 보존되지 않습니다.                                  |
| USER       | `authRequired` 화면 접근, 재생목록·다운로드 기록은 `SubscriberRoute` 추가 확인 | 구독 상태 조회 실패 분류와 중복 조회가 문제입니다.                                     |
| ADMIN      | `/admin/*`는 `adminOnly`; 동시에 role hierarchy상 모든 `authRequired`도 통과   | 회원용 구독·결제와 일부 subscriber CTA가 노출/실행될 수 있어 역할 격리가 실패했습니다. |
| INDIVIDUAL | 기업 인증 링크는 Profile에서 숨김                                              | URL 직접 접근 시 신청 폼이 노출되므로 사용자 유형 가드가 불완전합니다.                 |
| BUSINESS   | 기업 인증→승인→기업 플랜 흐름 제공                                             | 기본 상태 분기는 존재하나 오류 시 재시도와 계약별 메시지가 부족합니다.                 |

## State And Performance Result

| 영역              | Loading | Empty | Success | Error | Retry / Cancel / Stale                                       |
| ----------------- | ------- | ----- | ------- | ----- | ------------------------------------------------------------ |
| 음원·앨범·공지    | 있음    | 있음  | 있음    | 있음  | 명시적 retry 없음; 목록 요청 최신성 보장 없음                |
| 재생목록·다운로드 | 있음    | 있음  | 있음    | 있음  | 가드/PlayerBar/page의 구독 조회 중복, 모달 요청 cleanup 누락 |
| 구독·결제         | 있음    | N/A   | 있음    | 있음  | callback 복귀는 있으나 상태 조회 오류 분류 실패              |
| 화이트리스트      | 있음    | 있음  | 있음    | 있음  | 채널→구독 순차 조회, 구독 조회 실패를 비구독으로 처리        |
| 기업 인증         | 있음    | 있음  | 있음    | 있음  | 사용자 유형 가드와 retry 없음                                |
| 프로필            | 있음    | N/A   | 있음    | 있음  | 성공 후 전역 사용자 stale                                    |
| 관리자            | 있음    | 있음  | 있음    | 있음  | 결제 탭 요청 최신성 보장과 공통 retry 없음                   |

라우트 단위 `React.lazy()`는 적용되어 있습니다(`router/index.tsx:24-32,38-101`). 기존 `dist` 스냅샷을 읽기만 한 결과 main shell은 105,049 bytes gzip, 가장 큰 lazy page chunk는 결제 운영 8,915 bytes gzip이어서 번들 크기 자체를 P1/P2로 판정할 근거는 없었습니다. 대형 source component는 유지보수·테스트 분리 위험으로 분류했습니다.

## Verification

| 명령                     | 결과                                                                                      |
| ------------------------ | ----------------------------------------------------------------------------------------- |
| `npm run typecheck`      | PASS, TypeScript errors 0                                                                 |
| `npm run lint`           | PASS, ESLint errors/warnings 0 (`--max-warnings 0`)                                       |
| `npm run format`         | FAIL, 143 files require formatting                                                        |
| Vitest / build / browser | 이번 WI에서는 미실행: handoff의 static inspection 범위와 두 산출물 외 read-only 제약 준수 |

기존 테스트는 14개 파일뿐이며 54개 page component 중 page test가 있는 파일은 9개입니다. 특히 `SocialLoginPage`, `TrackListPage`, `WhitelistChannelPage`, 기업 인증 3개 화면, Header/PlayerBar, Pagination/Toast/Modal, 모든 관리자 화면의 직접 테스트가 없습니다.

## Recommended Order

1. FE-001 소셜 로그인 토큰 전달을 수정하고 공급자별 callback 회귀 테스트를 추가합니다.
2. FE-002 ADMIN 차단을 프론트 전용 가드와 서버 권한 검사 양쪽에 적용합니다.
3. FE-003 구독 오류 분류와 조회 deduplication을 공통화합니다.
4. 기업 인증 사용자 유형, returnUrl, 프로필 전역 동기화, 요청 최신성을 수정합니다.
5. 공용 접근성 컴포넌트와 retry 상태를 먼저 고쳐 재사용 화면 전체에 반영합니다.
6. 계약 문서·라우트 카운트·Prettier baseline·대형 컴포넌트 분리는 후속 WI로 처리합니다.

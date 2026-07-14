---
version: 1.0
last_updated: 2026-07-15
project: ATS
category: work-summary
status: stable
related_wi: WI-20260714-ATS-020
---

# WI-20260714-ATS-020 완료 요약

## 핵심 결과

- 프론트엔드 QA 관점에서 social callback, logout, refresh, Vite Host/proxy 경계를 다시 점검했고, 계약과 어긋나 있던 `/auth/logout`의 refresh 재진입 가능성을 프론트 코드에서만 바로잡았습니다.
- `AUTH_REFRESH_EXCLUDED_PATHS`에 `/auth/logout`을 추가해, 이미 무효화된 세션의 401이 refresh 시도로 이어지지 않도록 수정했습니다.
- focused Vitest를 5개 파일로 재정렬해 state transition, callback route, logout 정리, refresh 실패 처리, Host/proxy 경계를 총 23개 테스트로 검증했습니다.
- `/api`와 `/uploads`가 모두 `127.0.0.1:8080` loopback 경계와 동일한 header-sanitizing proxy 계약을 쓰는지도 테스트로 고정했습니다.
- backend production code는 수정하지 않았고, 실제 OAuth/Toss/터널 실행도 하지 않았습니다.

## 상태 전이 확인

1. social callback 성공:
   `exchange -> stageTokens -> fetchMe(accessToken) -> login -> navigate('/')`
2. profile 미완성 callback:
   `exchange -> stageTokens -> fetchMe(accessToken) -> login -> navigate('/complete-profile')`
3. callback 중 profile 조회 실패:
   staged token 존재 시 `logoutSession()`을 먼저 시도하고, 성공 여부와 무관하게 `clearSession()`으로 auth/player/like 상태를 비웁니다.
4. 보호 API의 refresh 실패:
   access/refresh token 제거 -> `clearSession()` -> toast 표시 -> `/login` 이동
5. logout 401:
   refresh interceptor 대상에서 제외되어 재갱신 시도로 되감기지 않습니다.

## route / proxy 결과

- social callback route `/social-login/:provider`는 Strict Mode에서도 교환 1회만 수행합니다.
- callback 결과에 따라 `/` 또는 `/complete-profile`로만 이동합니다.
- Vite `allowedHosts`는 `localhost`, `127.0.0.1`, 그리고 `APP_PUBLIC_BASE_URL`에서 파생된 정확한 host 1개만 허용합니다.
- `/api`, `/uploads` 모두 `http://127.0.0.1:8080`으로 프록시되며 `xfwd: false`를 유지합니다.
- inbound `Forwarded`, `X-Forwarded-*`, `X-ATStudio-Client-IP`, `CF-Connecting-IP`는 제거 후 내부 신뢰 header 하나만 다시 씁니다.

## 검증 결과

- Focused Vitest: 5 files, 23 tests 통과
  - `SocialLoginPage.test.tsx`: 3
  - `authStore.test.ts`: 3
  - `auth.test.ts`: 3
  - `client.test.ts`: 4
  - `vite.config.test.ts`: 10
- `npm run typecheck`: 통과
- `npm run lint`: 통과
- `npm run build`: 통과
- scoped `git diff --check`: 공백 오류 없음 (working-copy CRLF warning만 표시)

## 계약 mismatch 및 범위 메모

- 발견한 mismatch:
  logout 401이 refresh interceptor 제외 목록에 없어서, 세션 정리 도중 refresh 재시도가 발생할 수 있었습니다. 이번 WI에서 프론트엔드만 수정해 DoD와 맞췄습니다.
- 범위 밖으로 남긴 항목:
  실제 public Cloudflare 런타임에서의 header overwrite, 외부 클라이언트 간 rate-limit 분리, live OAuth/Toss 동작은 WI-022 등 운영 검증 단계에서 확인해야 합니다.

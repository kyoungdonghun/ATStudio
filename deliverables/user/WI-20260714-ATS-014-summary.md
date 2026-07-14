# WI-20260714-ATS-014 완료 요약

## 작업 결과

- 소셜 로그인 코드 교환 직후 access/refresh token을 인증 저장소와 Zustand 상태에 먼저 스테이징하도록 변경했습니다.
- 스테이징이 끝난 뒤 새 access token을 명시해 `/users/me`를 조회하고, 조회 성공 후에만 user/role을 함께 확정합니다.
- 프로필 미완성 응답은 사용자 상태를 확정한 뒤 기존 `/complete-profile` 경로로 이동합니다.
- 프로필 조회 또는 최종 처리 실패 시 WI-011의 서버 우선 `logout()`을 호출하고, 서버 폐기 성공 여부와 무관하게 `clearSession()`으로 토큰, 사용자, 역할, 플레이어/좋아요 상태를 정리합니다.
- React Strict Mode에서도 동일 callback code 교환이 한 번만 실행되는 회귀 테스트를 추가했습니다.

## 요청 순서

1. `socialLogin(provider, code, codeVerifier)`로 callback code를 교환합니다.
2. 발급된 access/refresh token을 `stageTokens()`로 저장하고 access token 상태를 노출합니다.
3. `fetchMe(accessToken)`으로 첫 사용자 조회 요청에 새 토큰을 명시합니다.
4. 조회된 프로필과 토큰을 `login()`으로 일관되게 확정합니다.
5. 프로필 완성 여부에 따라 `/` 또는 `/complete-profile`로 이동합니다.

## 실패 정리 동작

- 토큰 스테이징 전 실패: 서버 세션이 발급되지 않았으므로 로컬 `clearSession()`만 수행합니다.
- 토큰 스테이징 후 실패: 새 access token이 남아 있는 동안 서버 로그아웃을 우선 시도한 뒤 로컬 세션과 사용자 의존 상태를 항상 비웁니다.
- 서버 로그아웃이 네트워크 오류로 확인되지 않아도 로컬에는 부분 로그인 상태를 남기지 않습니다.
- WI-011의 bodyless logout, confirmed-401 처리, 서버 우선 정리, `clearSession()` 의미는 변경하지 않았습니다.

## 검증 결과

- Focused Vitest: 3 files, 9 tests 통과
- TypeScript typecheck: 통과
- Owned-path ESLint: 오류/경고 없이 통과
- Owned-path `git diff --check`: 통과
- 라이브 OAuth/provider 호출: 수행하지 않음

## 위험 및 후속 체인

- 서버 로그아웃 네트워크 실패 시 서버 refresh session 폐기는 확인할 수 없지만, 로컬 토큰과 사용자 상태는 항상 제거됩니다.
- 이번 검증은 mock 기반 callback 흐름이며 실제 provider 연동은 범위 밖입니다.
- WI-014는 `WI-020`, `WI-024`, `WI-025`를 차단하던 자신의 의존 간선을 해제합니다. `WI-020`은 Phase 3의 WI-015~017 완료 후, `WI-024/025`는 Phase 4 완료 후 체인 트리거 대상입니다.

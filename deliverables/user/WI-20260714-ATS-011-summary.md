# WI-20260714-ATS-011 완료 요약

## 결과

- 단일 refresh session 모델을 유지하면서 refresh, logout, 비밀번호 변경/재설정, 회원 탈퇴가 같은 사용자 행의 비관적 쓰기 잠금을 사용하도록 통일했습니다.
- `POST /api/auth/logout`을 인증 필수, 요청 body 없음, 반복 호출 가능한 `204 No Content` API로 추가했습니다.
- 오래된 refresh token의 hash 불일치가 더 최신 session hash를 삭제하지 않도록 수정했습니다.
- 프론트 logout은 서버 폐기를 먼저 요청하고 이후 로컬 인증 및 사용자 의존 store를 항상 정리합니다. 서버 확인 여부를 boolean으로 반환하며 transient 실패를 성공으로 표시하지 않습니다.
- token 값 또는 hash를 기록하는 로그는 추가하지 않았습니다.

## 변경 파일

- Backend: `UserRepository.java`, `AuthService.java`, `AuthController.java`, `SecurityConfig.java`, `UserService.java`, `EmailService.java`
- Backend tests: `UserRepositoryTest.java`, `AuthServiceTest.java`, `AuthControllerTest.java`, `UserServiceTest.java`, `EmailServiceTest.java`
- Frontend: `frontend/src/api/auth.ts`, `frontend/src/api/client.ts`, `frontend/src/store/authStore.ts`
- Frontend tests: `frontend/src/api/auth.test.ts`, `frontend/src/api/client.test.ts`, `frontend/src/store/authStore.test.ts`

## 검증

- Backend focused tests: 5 classes, 50 tests passed.
- Frontend focused tests: 3 files, 9 tests passed.
- Backend compile: `compileJava compileTestJava` passed.
- Frontend typecheck: `tsc --noEmit` passed.
- Owned-file whitespace gate: `git diff --check` passed.

## 잔여 위험 및 후속 체인

- Access token은 기존 만료 시점까지 유효합니다. Access-token denylist와 multi-session ledger는 승인 범위 밖입니다.
- 실제 refresh/termination 경쟁과 종료 이벤트별 end-to-end replay는 후속 보안 통합 테스트 `WI-20260714-ATS-019`에서 검증해야 합니다.
- 즉시 체인 트리거: 기존 handoff가 있는 `WI-20260714-ATS-014`를 진행할 수 있습니다.
- 후속 차단 해제 대상: `WI-20260714-ATS-019`, `WI-20260714-ATS-024`, `WI-20260714-ATS-025`.

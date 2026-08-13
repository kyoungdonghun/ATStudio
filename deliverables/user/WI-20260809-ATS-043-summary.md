# WI-20260809-ATS-043 완료 요약

## 변경 내용

- 로그인 복귀 경로를 하나의 검증 모듈로 통합했습니다. `ProtectedRoute`, `SubscriberRoute`, `PlayerBar`, 비밀번호 로그인, 소셜 로그인이 모두 같은 규칙을 사용합니다.
- 현재 정책대로 pathname과 query만 유지하고 hash는 제외했습니다. 외부 URL, 프로토콜 생략 URL, 손상된 값, 로그인 반복 경로, API/업로드 경로, 사용자 권한에 맞지 않는 ADMIN/결제/사업자 경로는 안전하게 홈으로 대체됩니다.
- `SubscriberRoute` 토스트 변경을 render 밖의 effect로 옮기고 StrictMode/상태 전환 중 중복 표시를 막았습니다.
- React Router의 지원되는 future 동작을 운영과 테스트 라우터에 일관되게 적용했고, 의존성 버전은 변경하지 않았습니다.
- lazy import가 실패하면 현재 내부 URL을 유지한 한국어 복구 화면을 보여 줍니다. 새 import를 단 한 번만 재시도하며, 홈/이전 화면 이동을 제공하고 원본 오류나 파일 경로를 노출하지 않습니다.
- 기존 `/error` 서버 오류 경로, public wildcard 404, ADMIN/구독자 라우트 정책은 유지했고, WI-057의 셸 키보드/포커스 범위는 변경하지 않았습니다.

## 독립 PG 검토 후속

- percent-encoded ADMIN 경로가 raw pathname 권한 분류를 우회할 수 있던 P2를 수정했습니다. 구조 및 권한 검사는 같은 decode+lowercase canonical pathname을 사용하며, 권한이 맞으면 원래 검증된 target 문자열을 그대로 이동에 사용합니다.
- OAuth 프로필 continuation이 계정에 묶이지 않았던 P3를 수정했습니다. continuation은 인증 사용자 ID에 바인딩되고, 새 저장 전 기존 값이 삭제됩니다. 저장 실패 시 복귀 경로 없이 프로필 완성으로 진행하며, 소비 시 계정 일치 여부와 관계없이 레코드를 먼저 삭제합니다. 토큰이나 secret은 저장하지 않습니다.
- PG 재현 테스트는 수정 전 9건 실패했고, 수정 후 집중 69개 및 인접 190개 테스트가 통과했습니다.
- 독립 PG 정적 재검토는 PASS입니다. 기존 P2/P3는 종료됐으며, 새 open redirect, encoding 우회, 계정 간 replay, token/secret 저장, fail-open 문제는 발견되지 않았습니다.

## `PlaylistListPage` 운영 변경 근거

`v7_startTransition`을 적용하면 재생목록 생성 후 route-state 정리 navigation이 transition으로 예약됩니다. 그 사이 재조회가 먼저 완료되면 이전 `openCreate: true` 요청을 effect가 다시 읽어 닫힌 모달을 재오픈하는 실제 운영 경쟁 조건이 재현됐습니다. `handledCreateRequestKeyRef`는 같은 location 요청을 한 번만 소비하고 새 navigation key는 정상적으로 처리합니다. 집중 테스트는 초기/생성 후 재조회 2회가 완료된 후에도 route state가 비워지고 모달이 닫힌 상태를 검증합니다.

## 검증

- PG 수정 후 focused 69개 및 adjacent 190개 테스트 통과
- MA 최종 frontend coverage: 80개 파일, 930개 테스트, 실패 0; statements 88.8% (`7929/8929`), branches 80.33% (`5012/6239`), functions 88.43% (`1981/2240`), lines 90.98% (`7291/8013`)
- TypeScript typecheck, ESLint 경고 0, Prettier, Vite 6.4.3 production build 통과; 280개 모듈 변환
- MA 최종 backend: 지정 Gradle 명령 통과; 184개 suite에서 1568개 테스트, 실패/오류 0, skipped 19; instruction 86.957%, branch 72.251%, line 87.228%, method 84.730%, class 94.824%; assemble 통과
- 문서 검증 통과: traceability ID 579개, broken link/orphan 없음
- 최종 산출물 편집 후 `git diff --check` 통과
- 실제 OAuth/provider/payment/mail/download/export, DB 변경, 무시된 secret 설정 조회는 수행하지 않았습니다.

## 잔여 경계

- 사용자 ID가 안정적이며 재사용되지 않고, `attemptId`가 token이나 PKCE secret이 아닌 상관관계 메타데이터라는 가정을 유지합니다.
- 독립 PG 재검토는 정적 검토였습니다. MA 최종 frontend, backend, 문서 및 diff 게이트는 모두 통과했습니다.

상세 재현 명령과 변경 포인터는 `deliverables/agent/WI-20260809-ATS-043-evidence-pack.md`에 기록했습니다.

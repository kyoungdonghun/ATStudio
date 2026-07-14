# WI-20260714-ATS-040 Summary

## 결과

- non-dry-run acceptance 시작에 저장소 외부의 backend 환경 JSON 경로를 필수화했습니다.
- JSON은 일반 파일, 저장소 외부 경로, flat object, 정확한 allowlist 이름, 비어 있지 않은 문자열 값 조건을 모두 만족해야 합니다.
- 필수 6개 변수는 `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, `JWT_SECRET`, `APP_BOOTSTRAP_TEST_USERS_ENABLED`, `APP_BOOTSTRAP_TEST_USERS_DEFAULT_PASSWORD`이며 bootstrap enabled 값은 정확히 `true`여야 합니다.
- tunnel을 먼저 시작한 뒤에만 bundle을 읽고, 검증된 값은 backend spawn 동안에만 주입합니다. launcher 환경은 `finally`에서 즉시 복원되며 그 다음 frontend를 시작합니다.
- tunnel과 frontend spawn에서는 42개 backend allowlist 이름을 부모 환경에서도 명시적으로 제거하므로, 기존 부모 환경에 값이 있어도 상속되지 않습니다.
- manifest, status, dry-run, 오류 메시지에는 bundle 경로, 본문, 값, JDBC URL, bootstrap password 또는 token을 저장하거나 출력하지 않습니다.

## 변경 파일

- `scripts/acceptance/start.ps1`
- `scripts/acceptance/AcceptanceLifecycle.psm1`
- `scripts/acceptance/test-dry-run.ps1`
- `scripts/acceptance/test-backend-environment.ps1`
- `deliverables/user/WI-20260714-ATS-040-summary.md`
- `deliverables/agent/WI-20260714-ATS-040-evidence-pack.md`

## 검증

- `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\acceptance\test-dry-run.ps1`: PASS
- `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\acceptance\test-backend-environment.ps1`: PASS
- required 6개, allowlisted 42개, unique 42개를 확인했습니다.
- scoped `git diff --check`: PASS
- 소유 PowerShell 파일 trailing whitespace 검사: 0건
- 테스트 종료 후 WI-040 임시 fixture 디렉터리: 0개
- PSScriptAnalyzer는 설치되어 있지 않아 해당 검사만 실행되지 않았습니다.

실제 backend, frontend, Cloudflare tunnel, DB, Toss/provider, email은 시작하거나 호출하지 않았습니다. 실제 secret을 생성·출력·저장하지 않았고 staging 또는 commit도 수행하지 않았습니다.

## 롤백

- 위 4개 acceptance PowerShell 파일에서 WI-040 변경만 되돌리고 WI-040 요약/Evidence Pack을 제거합니다.
- 공유 작업 트리의 다른 WI 변경이나 기존 runtime log는 건드리지 않습니다.

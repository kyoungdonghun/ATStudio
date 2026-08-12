---
version: 1.2
last_updated: 2026-08-13
project: ATS
owner: se
category: work-summary
status: complete
dependencies:
  - path: ../agent/WI-20260809-ATS-040-evidence-pack.md
    reason: 상세 구현, 검증 결과, 롤백 및 잔여 위험 근거
---

# WI-20260809-ATS-040 구현 요약

## 구현 결과

- CSV 확인 문구가 현재 적용된 상태와 검색어를 그대로 표시하도록 수정했습니다. 입력 중이지만 아직 검색에 적용하지 않은 검색어는 확인 문구와 요청에 포함되지 않습니다.
- `전체 + 검색어` 범위는 모든 일치 상태를 포함하고, 그중 `PENDING`만 `EXPORTED`로 전환되며 다른 상태는 유지된다고 명시합니다. 명시적 `PENDING`과 그 외 상태의 문구도 실제 저장 결과에 맞췄습니다.
- 최신 채널 목록 요청이 실패하면 기존 행, 페이지 정보, 편집값을 모두 비워 더 이상 오래된 행을 조작할 수 없습니다. 이전 요청의 성공·실패·완료가 최신 상태를 덮어쓰지 못하는 기존 요청 세대 보호도 유지했습니다.
- `GET /api/admin/whitelist-channels/exports/recent`를 추가했습니다. 인증된 ADMIN 본인의 배치만, 정규화된 상태/검색어 범위가 정확히 같은 경우에 한해 `createdAt`, ID 최신순으로 최대 10건 조회합니다.
- 최근 이력 검색어는 공백 제거 후 export 요청과 같은 최대 100자 제한을 서비스에서 적용합니다. 101자는 기존 `INVALID_ARGUMENT`로 거절하며 batch/item 저장소를 호출하지 않습니다.
- 최근 이력은 batch ID, 파일명, 건수, 기록된 상태/검색어, 생성 시각만 반환합니다. item snapshot과 CSV bytes는 조회하거나 반환하지 않습니다.
- POST 응답이 모호하면 export POST를 다시 보내지 않습니다. 공통 401 인증 재생도 비활성화했고, 같은 범위의 최근 이력을 한 번 조회한 뒤 운영자가 선택한 batch만 기존 재다운로드 API로 명시적으로 받습니다.
- 확정적인 4xx는 일반 실패로 표시하며 batch가 저장됐다고 주장하지 않습니다.

## 검증 결과

- Backend focused: 3개 클래스, 22개 테스트 모두 통과했습니다. H2에서 다른 ADMIN/상태/검색어 제외, ALL null 상태 범위, 대소문자 정규화, 생성 시각·ID 정렬, 최대 10건 제한을 확인했습니다.
- 서비스 회귀 테스트는 공백 제거 후 101자 검색어가 두 export 저장소 접근 전에 거절되는지 확인합니다. 또한 `전체 + 검색어` 혼합 후보에서 일치하는 `PENDING`만 `EXPORTED`가 되고 일치하는 비-`PENDING`은 유지되며, export item snapshot에는 전환 전 상태가 각각 기록되는지 검증합니다. 운영 상태 전이 로직은 변경하지 않았습니다.
- 독립 QA-INTEG 검토는 P1/P2 없이 `PASS`였습니다. 제시된 두 P3인 최근 조회 검색어 100자 제한 정합성과 `전체 + 검색어` 혼합 상태 전이 회귀 검증은 모두 보완했고, 집중 백엔드 결과는 22/22가 되었습니다.
- Frontend focused/adjacent: 3개 파일, 45개 테스트 모두 통과했습니다. 적용/초안 검색어 분리, ALL 확인 문구, 목록 실패 격리, 응답 유실 복구, 두 번째 POST 없음, 명시적 replay를 포함합니다.
- 변경 파일 대상 TypeScript typecheck, ESLint, Prettier가 통과했습니다.
- 첫 Main 전체 백엔드 재실행은 테스트 실패가 아니라 `build/test-results` 테스트 바이너리 임시 파일에 대한 Gradle `NoSuchFileException`으로만 종료되었습니다. 에이전트를 닫고 Gradle daemon을 중지한 뒤 단일 worker 격리 명령 `.\gradlew.bat test jacocoTestReport jacocoTestCoverageVerification assemble --rerun-tasks --no-daemon --max-workers=1 --console=plain`으로 재실행해 `2m29s`에 통과했습니다.
- 백엔드 최종 결과는 1,568개 테스트, 실패 0개, skip 19개입니다. JaCoCo 기준을 통과했으며 instruction 86.957%, branch 72.251%, line 87.228%, method 84.730%, class 94.824%입니다.
- 프론트엔드 최종 결과는 74개 파일, 843개 테스트 통과입니다. coverage는 statements 88.64%, branches 79.88%, functions 88.18%, lines 90.87%이며 typecheck, ESLint, Prettier, build도 모두 통과했습니다. 출력된 JSDOM navigation 메시지는 기존 안내이며 실패가 아닙니다.
- 문서 검증은 578개 traceability ID 기준으로 통과했고 매핑 수 150건이 현재 소스와 일치합니다. `git diff --check`도 whitespace 오류 없이 통과했습니다.

## 유지한 경계와 잔여 위험

- 스키마, DDL, 데이터, 의존성, CSV 형식, 상태 전이, export 최대값을 변경하지 않았습니다. 보고된 게이트는 자동화 코드 테스트와 정적 검사이며 live export, 외부 전달, Provider 작업, persistent/live DB 검증은 수행하지 않았습니다.
- 최근 이력은 operation key가 아니므로 같은 범위의 batch가 여러 개일 수 있습니다. 화면은 결과를 계속 `unknown`으로 표시하며 어떤 batch가 응답 유실 요청의 결과인지 자동 판정하지 않습니다.
- 독립 검토와 전체 backend/frontend 게이트는 완료되었습니다. 이 최종 문서 갱신 뒤 Main이 수행할 commit과 push만 `pending`입니다.

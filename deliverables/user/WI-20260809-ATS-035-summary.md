---
version: 1.0
last_updated: 2026-08-12
project: ATS
owner: docops
category: work-summary
status: stable
related_wi: WI-20260809-ATS-035
dependencies:
  - path: ../agent/WI-20260809-ATS-035-evidence-pack.md
    reason: 상세 변경, 재현, 검증 및 롤백 근거
  - path: ../agent/WI-20260809-ATS-035-re-review.md
    reason: 최종 독립 신뢰성 검토 근거
---

# WI-20260809-ATS-035 사용자 요약

## 결과

ADMIN 환불 실행과 환불 연계 권한 보정 실행에서 응답 유실이나 새로고침
실패가 발생해도 같은 작업을 자동 반복하지 않도록 복구 흐름을
완성했습니다. PG, QA-INTEG, RE의 최종 판단은 모두 `APPROVE`입니다.

## 실행과 복구 흐름

1. 운영자가 기존 확인 문구를 입력해 실행을 명시적으로 시작합니다.
2. 화면은 실행 전에 해당 환불 또는 권한 보정의 정확한 상세 GET을 먼저
   호출합니다.
3. 같은 ID의 최신 상태가 `APPROVED`일 때만 execute POST를 한 번
   호출합니다.
4. execute 응답이 유실되거나 거절되면 같은 상세 GET을 한 번만 추가로
   호출해 저장된 상태를 확인합니다. execute POST는 반복하지 않습니다.
5. 두 execute POST는 인증 토큰 갱신 재전송 대상에서 제외되어, `401`
   응답도 두 번째 mutation으로 재생되지 않습니다.

## 네 가지 결과

- `COMMITTED`: 정확한 환불 또는 권한 보정 상세 상태가 `SUCCEEDED`입니다.
- `FAILED`: 정확한 execute 또는 상세 상태가 `FAILED` 또는
  `CANCELLED`입니다.
- `RELOAD_FAILED`: execute는 `SUCCEEDED`를 반환했지만 필수 상세 또는
  목록 새로고침이 실패했습니다. 실행 성공을 실패로 바꾸지 않고
  새로고침 실패를 별도로 안내합니다.
- `UNKNOWN`: 처리 중이거나 상세 조회가 불가능해 성공과 최종 실패 중
  어느 쪽도 증명되지 않은 상태입니다.

## 운영자 화면 보호

- 환불 `PROCESSING`/`PENDING_PROVIDER_CONFIRMATION` 및 권한 보정
  `PROCESSING` 행은 브라우저나 목록을 새로고침해도 `UNKNOWN`으로
  복원됩니다.
- `상태 다시 확인`은 상세 GET만 수행하는 읽기 전용 기능입니다. 승인,
  execute, Provider 호출, 로컬 상태 변경을 하지 않습니다.
- `UNKNOWN`에서 상세 상태가 `REQUESTED` 또는 `APPROVED`로 확인될 때만
  실행 전 잠금이 해제됩니다. `REQUESTED`는 승인만 가능하고,
  `APPROVED`도 이후 별도 확인 문구 입력과 새 preflight를 다시 거쳐야
  execute할 수 있습니다.
- 환불 결과가 모호하면 연결된 권한 보정 mutation도 잠기고, 권한 보정
  결과가 모호하면 연결된 환불과 같은 환불의 다른 보정 mutation도
  잠깁니다.
- 실행, 상태 조회, 현재 탭/페이지에 세대 번호를 부여해 오래된 상세나
  목록 응답이 최신 결과를 덮어쓰지 못하게 했습니다.
- 자동 환불 execute 재시도 0회, 자동 권한 보정 execute 재시도 0회,
  복구 조회의 mutation 및 Provider 호출 0회입니다.

## 검증

- 백엔드 전체 명령:
  `./gradlew.bat test jacocoTestReport jacocoTestCoverageVerification build --console=plain`
  -> `BUILD SUCCESSFUL`, 1,459 tests, 실패 0, skip 16.
- 백엔드 커버리지: instruction 86.394%, line 86.637%, method 84.100%,
  branch 71.290%.
- 프런트엔드 전체 테스트: 72 files, 794 tests PASS.
- 프런트엔드 커버리지: statements 88.2%, branches 79.39%, functions
  87.73%, lines 90.44%.
- typecheck, ESLint, `npm run format`, build 모두 PASS.
- `npm run format:check`는 저장소에 정의되지 않은 스크립트여서 실제
  저장소 명령인 `npm run format`으로 바로잡았습니다. 제품 실패가
  아닙니다.
- 최종 RE focused/adjacent 검증: 4 files, 132 tests PASS, 실패 0.
- 실제 저장소 validator:
  `python .claude/skills/validate-docs/scripts/validate_docs.py` -> exit 0,
  Tier 0, 내부 링크, traceability ID 554개, 문서 index 모두 PASS.
- `git diff --check` -> exit 0, whitespace 오류 없음. 기존 공유 작업 트리
  파일의 CRLF→LF 변환 경고만 있었으며 이번 WI에서 줄바꿈을 변경하지
  않았습니다.

## 남은 비차단 부채와 한계

- 기존 ADMIN 환불 DTO의 raw idempotency key, actor email, failure message와
  권한 보정 DTO의 actor email, failure message는 최소화 후속 과제입니다.
  WI-035가 추가한 필드가 아니며 이번 작업에서 해결된 것으로 표시하지
  않았습니다. 복구 UI도 이 필드를 표시하지 않습니다.
- 검증은 React 자동화, H2/Test-Provider, 백엔드 controller/service
  테스트 근거입니다. 실제 Toss, 배포 브라우저, 운영 DB 검증을 의미하지
  않습니다.
- 이번 DocOps 종료 작업은 지정된 5개 문서와 두 산출물만 수정했습니다.
  제품/테스트 코드, 외부 서비스, stage/commit/push, ZIP/output, 비밀값은
  건드리지 않았습니다.

## 관련 문서

- [상세 Evidence Pack](../agent/WI-20260809-ATS-035-evidence-pack.md)
- [PG Review](../agent/WI-20260809-ATS-035-pg-review.md)
- [QA-INTEG Review](../agent/WI-20260809-ATS-035-qa-integ-review.md)
- [RE Review](../agent/WI-20260809-ATS-035-re-review.md)
- [Payment Operations Runbook](../../docs/design/payment-operations-runbook.md)
- [ADMIN Operations Guide](../../docs/payment/admin-operations-guide.md)
- [API Specification](../../docs/design/api-spec.md)
- [Payment Integration Design](../../docs/design/payment-integration-design.md)
- [Screen Flows](../../docs/ui/screen-flow.md)

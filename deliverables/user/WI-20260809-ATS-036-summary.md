---
version: 1.0
last_updated: 2026-08-13
project: ATS
owner: se
category: work-summary
status: active
related_wi: WI-20260809-ATS-036
dependencies:
  - path: ../agent/WI-20260809-ATS-036-evidence-pack.md
    reason: 상세 변경, 재현, 검증, 롤백 근거
  - path: ../agent/WI-20260809-ATS-036-handoff.md
    reason: 승인된 범위와 acceptance 기준
---

# WI-20260809-ATS-036 사용자 요약

## 결과

동시에 여러 보호 API가 `401`을 반환할 때, refresh를 직접 시작하는 요청과
대기열에 들어가는 요청 모두 refresh 또는 queue 진입 전에 내부 재시도 표시를
갖도록 수정했습니다. 따라서 모든 요청은 새 access token으로 최대 한 번만
재생되며, 재생된 요청이 다시 `401`을 받으면 그 두 번째 실패를 그대로 반환하고
추가 refresh나 재생을 하지 않습니다.

## 유지된 동작

- 동시에 발생한 첫 `401`들은 refresh API 한 번만 공유합니다.
- `skipAuthReplay` 요청과 login/logout/refresh/social 인증 경로는 refresh와
  queue에 들어가지 않습니다.
- refresh token 없음, token 저장 실패, token rotation, session 정리와 login
  이동 동작은 기존 테스트를 그대로 통과했습니다.
- ADMIN `403` 역할 동기화는 요청을 재시도하지 않고 원래 `403`을 유지합니다.
- 인증 정책, session lifetime, token 저장 구조, redirect 정책은 변경하지
  않았습니다.

## 검증

- 결함 재현: `npm test -- --run src/api/client.test.ts` -> 구현 전 23개 중
  2개 실패. queued marker 누락과 두 번째 refresh 발생을 각각 포착했습니다.
- 집중 테스트: 같은 명령 -> 1 file, 23 tests 모두 PASS.
- 전체 테스트: `npm test` -> 73 files, 829 tests 모두 PASS.
- coverage: statements 88.36%, branches 79.58%, functions 88.02%, lines
  90.58%. `client.ts`는 98.94%, 93.15%, 100%, 98.88%입니다.
- `npm run typecheck`, `npm run lint`, `npm run format`, `npm run build` 모두
  PASS했고 production build는 274 modules를 처리했습니다.
- 문서 validator는 Tier 0, 내부 링크, 562개 traceability ID, 문서 index를
  모두 통과했고, WI 범위 `git diff --check`도 exit 0입니다.
- 현재 frontend/security/API 문서에 exactly-once replay와 두 번째 `401`
  fail-closed 계약을 반영했습니다.
- 독립 PG가 같은 집중 테스트를 다시 실행해 1 file, 23 tests, exit 0을
  확인했고, exactly-once replay와 fail-closed 두 번째 `401`을 `PASS`로
  판정했습니다.

## 잔여 위험과 승인 지점

- 테스트는 결정적 adapter promise로 race를 검증했으며 실제 배포 브라우저나
  외부 backend는 호출하지 않았습니다.
- `client.ts` 전체 coverage는 기존 인접 분기 때문에 100%는 아니지만, 이번에
  바뀐 marker와 leader/queued/second-`401` 경로는 직접 검증됩니다.
- 독립 PG 검토는 정확히 한 번의 replay, 동시 요청의 단일 refresh 소유권,
  두 번째 `401` 원본 실패 보존, 인증 경로/`skipAuthReplay` 제외, token/storage
  실패 처리, bounded queue, credential 비노출을 확인해 `PASS`했습니다.
- 이 판정으로 handoff의 마지막 보안 검토 acceptance 항목이 닫혔습니다.
  실제 배포 브라우저와 외부 backend를 사용하지 않은 제한은 잔여 검증 위험으로
  유지되지만, 이 WI에서 요구하거나 허용한 실행 범위는 아닙니다.

## 다음 WI

다음 후속 WI는 `WI-20260809-ATS-042`입니다. 그 밖에
`WI-20260809-ATS-043`, `WI-20260809-ATS-053`, `WI-20260809-ATS-057`,
`WI-20260809-ATS-060`도 WI-036 acceptance 완료로 진행 가능해졌습니다.
이번 독립 PG 검토 범위에서는 후속 WI를 생성하거나 위임하지 않았습니다.

## 관련 문서

- [상세 Evidence Pack](../agent/WI-20260809-ATS-036-evidence-pack.md)
- [WI-036 Handoff](../agent/WI-20260809-ATS-036-handoff.md)
- [Frontend Standards](../../docs/standards/frontend-standards.md)
- [Security Policy](../../docs/policies/security-policy.md)
- [API Specification](../../docs/design/api-spec.md)

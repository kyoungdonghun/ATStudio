---
version: 1.0
last_updated: 2026-07-15
project: ATS
owner: se
category: agent
status: stable
dependencies:
  - path: ../agent/WI-20260714-ATS-038-handoff.md
    reason: Approved WI scope and acceptance criteria
---

# WI-20260714-ATS-038 구현 결과

## 결과

- acceptance 전용 구독 플랜 bootstrap runner를 추가했습니다.
- runner는 `acceptance` 프로필, `app.acceptance.enabled=true`, `app.bootstrap.test-users.enabled=true`가 모두 충족될 때만 활성화됩니다.
- 빈 acceptance 데이터에는 `INDIVIDUAL`/`BUSINESS`별 `STANDARD`, `DELUXE`, `PREMIUM` 총 6개 플랜을 설계 문서의 가격과 한도로 생성합니다.
- 기존 활성 canonical 플랜이 일치하면 수정하거나 중복 생성하지 않습니다.
- 기존 canonical 플랜이 비활성, 속성 불일치, 중복 상태이면 민감하지 않은 사유 코드로 시작을 거부합니다.
- 새 runner는 기존 QA 사용자 bootstrap보다 먼저 실행되어 `qa_subscriber`, `qa_grace`, `qa_business`가 필요한 플랜을 조회할 수 있습니다.

## 검증

- 새 runner 단위 테스트 6개, 활성화 구성 테스트 1개, 기존 QA bootstrap 회귀 테스트 3개를 실행했습니다.
- 총 10개 focused test가 통과했습니다.
- 실제 DB, 스키마, 결제/provider, 이메일, 런타임 로그, 터널은 건드리지 않았습니다.

## 제한 사항

- 이번 WI는 mock 기반 단위/구성 검증만 수행했으며 disposable MySQL 또는 공개 acceptance 환경을 시작하지 않았습니다.
- 실제 fresh-schema subscriber 경로와 외부 클라이언트 검증은 다음 `WI-20260714-ATS-039`에서 수행해야 합니다.

## Related Documents

- [WI-038 Handoff](../agent/WI-20260714-ATS-038-handoff.md): 승인 범위와 완료 조건
- [WI-038 Evidence Pack](../agent/WI-20260714-ATS-038-evidence-pack.md): 구현 및 검증 근거

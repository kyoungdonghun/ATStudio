---
version: 1.0
last_updated: 2025-12-30
project: system
owner: SA
category: adr
status: accepted
related_work_item: WI-20251230-001
dependencies:
  - path: ../guides/operation-process.md
    reason: Operation process reference
  - path: ../guides/traceability.md
    reason: Traceability system reference
---
# ADR-20251230-001: Reuse-first + Registry + ADR로 운영 추적성 확보

- **Date**: 2025-12-30
- **Status**: Accepted
- **Related Work Item**: WI-20251230-001

## Context

에이전트/프로젝트가 성장할수록, 기능 추가는 쉬워도 운영 단계에서 아래 문제가 누적된다.

- 소스를 재사용하지 못해 중복/드리프트가 발생
- 변경 시 영향도(consumer/계약/회귀)가 파악되지 않음
- “왜 이렇게 했는지” 근거가 사라져 리팩터링/확장 비용이 급증

## Decision

운영 가능 수준의 최소 체계로 아래 3가지를 채택한다.

1. **Reuse-first 프로세스**: 새로 만들기 전에 검색/재사용/승격 검토를 강제
2. **Registry(주소록)**: Capability/Asset을 한 곳에서 찾아 재사용 결정을 빠르게 함
3. **ADR(결정 기록)**: MEDIUM/HIGH 변경의 결정/대안/리스크/롤백을 남겨 추적성 확보

## Options Considered

- **Option A: 문서 없이 “규칙은 머리로” 운영**
  - Pros: 초기 속도
  - Cons: 운영 불가(지식 증발/중복 폭발/온보딩 불가)
- **Option B: 모든 것을 코드로 자동화부터 시작**
  - Pros: 이상적
  - Cons: 초기 투자 과대, MVP 지연
- **Option C: 최소 문서/템플릿/레지스트리로 규율을 먼저 고정(선택)**
  - Pros: 낮은 비용으로 운영 기반 확보, 이후 자동화로 확장 가능
  - Cons: 초기에는 “작성/갱신”의 규율이 필요

## Consequences

- **Positive**:
  - 재사용 후보 탐색이 빨라지고 중복이 줄어듦
  - 변경 영향도/롤백/검증이 표준화됨
  - 의사결정 근거가 남아 장기 유지보수/확장 비용이 감소
- **Negative / Risks**:
  - 문서/레지스트리 갱신이 누락되면 체계가 무너질 수 있음
- **Mitigations**:
  - PR 템플릿/체크리스트로 “갱신 누락”을 방지(추후 자동화 가능)



[WI HEADER]
WI ID: WI-20260227-ATS-028
REQ: REQ-20260227-ATS-009
Agent: sa
Depends On: -
Blocks: WI-20260227-ATS-029, WI-20260227-ATS-030, WI-20260227-ATS-031, WI-20260227-ATS-032

[WI SUMMARY]
Why: 백엔드 전수 감사(REQ-009)의 Phase 1. 설계 문서(API 명세 79개, DB 스키마 21테이블)와 코딩 표준을 파악하여, Phase 2~3 코드 검토의 기준이 되는 "구현 검증 체크리스트"를 작성한다. 이 체크리스트가 없으면 이후 cr/pg 에이전트가 검토 기준 없이 주관적 판단에만 의존하게 됨.

Scope (in):
  - docs/design/api-spec.md — 79개 API 전체 파악 (URL/Auth/Request/Response/ErrorCode)
  - docs/design/db-schema.md — 21테이블 파악 (PK/FK/인덱스/Enum/Constraint/관계)
  - docs/standards/development-standards.md — 적용 가능한 규칙 목록 추출
  - 비즈니스 규칙 추출: 각 도메인의 상태 전환·한도·제약 조건 정리
  - 산출물: "구현 검증 체크리스트" (Phase 2~3 에이전트가 코드 검토 시 참조)

Scope (out):
  - 실제 코드 파일 열람 — Phase 2~3 역할 (이번 WI는 문서만)
  - 코드 수정 — 이번 REQ 전체 범위 외
  - 아키텍처 설계 변경 제안 — 발견 보고만

DoD:
  - api-spec.md 79개 API를 도메인별로 정리한 체크리스트 항목 존재
  - db-schema.md 21테이블 각각의 검증 포인트 정리
  - development-standards.md에서 추출한 "코드 검토 규칙 목록" 존재
  - 각 도메인의 핵심 비즈니스 규칙 목록 존재 (상태 전환, 제약 조건 등)
  - 체크리스트가 Phase 2 cr 에이전트가 즉시 사용할 수 있는 형식으로 작성됨

Constraints/Forbidden:
  - 코드 파일(src/) 열람 금지 — 문서만 읽을 것
  - 코드 수정 절대 금지
  - 체크리스트 작성 시 주관적 판단 최소화 — 문서에서 명시된 항목 기반

[ACCEPTANCE CRITERIA]
Functional:
  - [ ] 79개 API가 14개 도메인 섹션으로 분류되어 체크리스트에 나열됨
  - [ ] 각 API 항목에 검증 포인트 포함: URL·Auth·Request·Response·ErrorCode
  - [ ] 21개 DB 테이블 각각 검증 포인트 포함: PK/FK/인덱스/Enum 정합성
  - [ ] development-standards.md 규칙 중 코드 검토 시 확인 가능한 항목 목록화
  - [ ] 도메인별 비즈니스 규칙 목록 존재 (예: 구독 상태 전환 ACTIVE→CANCELLED→재구독 가능?)
  - [ ] Phase 2 도메인 그룹 분할 기준 확인 (cr-A/B/C 각 검토 범위 명시)
Performance:
  - N/A (Read-only 작업)
Quality:
  - [ ] 체크리스트 항목 수 ≥ 100 (79 API + 21 테이블 + 규칙)
  - [ ] 각 항목에 "확인 방법" 또는 "확인 파일 경로" 힌트 포함

[INPUT POINTERS]
Tier 0 (Constitution):
  - docs/standards/core-principles.md

Tier 0 (Standards):
  - docs/standards/development-standards.md

REQ/Context:
  - deliverables/user/REQ-20260227-ATS-009.md

설계 문서 (주요 읽기 대상):
  - docs/design/api-spec.md          ← API 명세 v5 (79개)
  - docs/design/db-schema.md         ← DB 스키마 v4 (21테이블)

추가 설계 문서 (존재 시 읽을 것):
  - docs/design/use-cases.md         ← 유스케이스 (있는 경우)
  - docs/architecture/system-design.md ← 시스템 설계 원칙

[OUTPUT CONTRACT]
User-facing  → deliverables/user/WI-20260227-ATS-028-summary.md
  - 문서 파악 요약: 발견된 주요 설계 의도·규칙·제약 요약
  - Phase 2~3 검토 시 특히 주의할 포인트 (최대 10개)

Agent-facing → deliverables/agent/WI-20260227-ATS-028-evidence-pack.md
  - **구현 검증 체크리스트** (Phase 2~3 에이전트 입력 자료):
    ```
    ## API 검증 체크리스트
    ### 1.x Track (7개 API)
    | # | Method | URL | Auth | 확인 포인트 |
    |---|--------|-----|------|------------|
    | 1.1 | POST | /api/tracks | AUTH | 단건/복수 업로드 가능한지, 파일 형식 검증 |
    ...

    ## DB 검증 체크리스트
    ### tracks 테이블
    | 항목 | 기대값 | 확인 방법 |
    |------|--------|----------|
    | PK | id BIGINT AUTO_INCREMENT | Track.java @Id |
    ...

    ## 코딩 표준 규칙 체크리스트
    | 규칙 | 기준 | 확인 방법 |
    |------|------|----------|
    | @Transactional | 클래스 readOnly=true, 변경 메서드만 override | Service 클래스 어노테이션 |
    ...

    ## 도메인별 비즈니스 규칙
    ### Subscription
    - 1인 1구독 제약: user_id UNIQUE
    - 상태 전환: ACTIVE → CANCELLED (취소 후 재구독 허용?)
    ...
    ```

Handoff Packet → deliverables/agent/WI-20260227-ATS-028-handoff.md (this file)

[TRACEABILITY REQUIREMENTS]
Evidence pointers:
  - 체크리스트 항목 수 (총 N개)
  - 문서 읽은 파일 목록 + 라인 범위
  - 특이사항: 문서에서 모호하거나 누락된 부분
Rollback:
  - Read-only 작업 → 롤백 불필요

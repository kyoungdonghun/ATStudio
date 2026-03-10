# Claude 에이전트 가이드 — AI 에이전트 시스템 심층 설명

> **이 문서의 목표:** ATStudio에서 Claude AI가 어떻게 동작하는지, 에이전트와 스킬이 무엇인지, 실제로 어떻게 활용하는지를 심층적으로 설명합니다.

---

## 1. 이 시스템이 뭔가요?

ATStudio는 단순한 Claude 채팅이 아닙니다. Claude Code를 **MA(메인 오케스트레이터)**로 활용하는 **멀티에이전트 AI 개발 시스템**입니다.

### 핵심 개념 4가지

| 개념 | 설명 |
|------|------|
| **MA** | 사용자와 대화하는 유일한 접점. 요구사항을 받아 분석하고 전문 에이전트에게 위임 |
| **Subagent** | 특정 역할을 담당하는 전문 AI. 11개가 있으며 각자 독립된 컨텍스트에서 실행 |
| **REQ** | 요구사항 정의 문서. 사용자가 승인해야 작업이 시작됨 |
| **WI** | Work Item. 실제 작업 단위. 에이전트에게 위임되는 최소 실행 단위 |

### 왜 이 방식을 쓰나요?

```
일반 AI 채팅:
  사용자 → Claude → 코드 작성 (모든 것을 혼자 처리)

ATStudio 시스템:
  사용자 → MA → sa(설계) + se(구현) + pg(보안) + re(테스트) + cr(리뷰)
                → 전문가별 독립 검증 → 결과 취합 → 보고
```

**장점:**
- 대규모 개발 작업을 역할별 전문 에이전트가 병렬 처리
- 모든 변경사항이 REQ → WI → 증거팩으로 추적 가능
- 에이전트마다 전문 표준 문서가 주입되어 품질 일관성 확보
- 승인 게이트를 통해 의도치 않은 파괴적 변경 방지

---

## 2. 에이전트 역할 매트릭스 (11개 전체)

### 핵심 에이전트 (빈번하게 호출)

| 코드명 | 풀네임 | 주요 역할 | 실제로 하는 일 | 언제 호출되나 |
|-------|-------|---------|-------------|------------|
| **ps** | Product Strategist | 요구사항 정리 | 사용자 발화를 분석해 REQ 초안 작성. 목표/범위/성공기준/제약 명문화 | 모든 새 기능 요청 시 |
| **sa** | Software Architect | 아키텍처/설계 | DB 스키마 설계, API 명세 작성, ADR(아키텍처 결정 기록) 문서화, JPA 설계 | 새 엔티티/API 설계 시 |
| **se** | Software Engineer | Java 구현 | Controller, Service, Entity, Repository, DTO 코드 작성. 리팩토링 | 코드 생성/수정이 필요한 모든 WI |
| **re** | Reliability Engineer | 테스트/검증 | JUnit5 단위 테스트, 통합 테스트 작성. 회귀 검증 | 구현 완료 후 테스트 작성 |

### 품질/보안 에이전트

| 코드명 | 풀네임 | 주요 역할 | 실제로 하는 일 | 언제 호출되나 |
|-------|-------|---------|-------------|------------|
| **pg** | Privacy Guardian | 보안 전문 | Spring Security 설정, JWT 정책 검토, 비밀번호/시크릿 관리, 취약점 분석 | 인증/보안 관련 WI, 보안 리뷰 요청 시 |
| **cr** | Code Reviewer | 코드 리뷰 | Java 베스트 프랙티스 검토, 코드 품질 개선 제안, 버그 패턴 탐지 | 구현 완료 후 리뷰 단계 |
| **qa** | Quality Assurance | 빌드/품질 | Gradle 빌드 검증, 통합 품질 테스트, 빌드 에러 분석 | 배포 전, 빌드 검증 시 |

### 보조 에이전트

| 코드명 | 풀네임 | 주요 역할 | 실제로 하는 일 | 언제 호출되나 |
|-------|-------|---------|-------------|------------|
| **eo** | Ensemble Overseer | 거버넌스 | 라우팅 결정, 정책 위반 감시, 에이전트 간 충돌 중재 | 정책 위반 의심, 라우팅이 모호할 때 |
| **tr** | Technology Researcher | 기술 조사 | 라이브러리 비교 분석, 최신 기술 트렌드 조사, 마이그레이션 옵션 평가 | "뭘 써야 해?" 기술 선택 시 |
| **uv** | UX/UI Virtuoso | API 명세/UI | Swagger 문서 작성, API 응답 형식 설계, UI 컴포넌트 설계 | API 문서화, 프론트엔드 UI 설계 시 |
| **docops** | Documentation Ops | 문서 관리 | 문서 동기화, 인덱스 관리, 드리프트 탐지, 가이드 문서 작성 | 문서 정합성 문제, 가이드 작성 요청 시 |

### 에이전트 우선순위

| 우선순위 | 에이전트 |
|---------|---------|
| HIGH | ps, eo, sa, pg, cr |
| MEDIUM | se, re, uv, docops, qa |
| LOW | tr |

> 우선순위는 라우팅 결정 시 참고 기준이며, 실제 호출 여부는 작업 내용에 따라 결정됩니다.

---

## 3. REQ → WI → 위임 워크플로우 (핵심)

모든 기능 개발, 버그 수정, 문서 작업은 이 흐름을 따릅니다.

```
[사용자 요구사항 발화]
        |
        v
[1단계] MA가 /create-req 실행
        - 발화를 분석해 REQ 초안 작성
        - 목표 / 범위(포함/제외) / 성공기준 / 제약 / WI 계획 명시
        - 사용자에게 검토 및 승인 요청
        |
        v (사용자 승인)
        |
[2단계] MA가 /create-wi-handoff-packet 실행
        - WI별 담당 에이전트, 입력파일, 완료조건 명시
        - 의존성 없는 WI는 병렬 처리 계획
        |
        v
[3단계] MA가 Agent Tool로 에이전트 호출
        - 각 에이전트는 독립 컨텍스트에서 WI 실행
        - 의존성 없는 에이전트는 병렬 실행 가능
        |
        v
[4단계] 결과 수집
        - 에이전트가 Evidence Pack(증거 패킷) 작성
        - 필요 시 re(테스트), cr(리뷰) 추가 검증
        |
        v
[5단계] WI 완료 → 다음 WI 트리거 (WI 체인)
        - 현재 WI가 완료되면 블로킹하던 다음 WI 자동 시작
        - 모든 WI 완료 시 REQ 종료 및 사용자 보고
```

### 승인이 필요한 시점

| 시점 | 이유 |
|------|------|
| REQ 검토 | 작업 범위와 방향이 올바른지 확인 |
| 파일 삭제 요청 | 되돌릴 수 없는 변경 |
| DB 스키마 변경 | 기존 데이터에 영향 |
| 아키텍처 선택 | 장기적 기술 부채 영향 |
| 라이브러리 선정 | 의존성 추가 |
| 보안/민감 데이터 변경 | JWT, DB 비밀번호 등 |

### 자동 진행되는 것 (REQ 승인 후)

- Git 작업 (커밋, 브랜치)
- 빌드/테스트 실행
- WI에 명시된 파일 생성 및 수정
- 문서 생성 및 업데이트

---

## 4. 스킬 목록 (사용자가 직접 호출하는 /명령어)

스킬은 자주 반복되는 작업을 표준화한 자동화 절차입니다.

### 핵심 워크플로우 스킬

| 스킬 | 설명 | 누가 주로 사용 |
|------|------|-------------|
| `/create-req` | 사용자 발화를 표준 REQ 문서로 변환 | MA (사용자 요청 후) |
| `/create-wi-handoff-packet` | 에이전트 호출 전 WI 핸드오프 패킷 생성 (**에이전트 호출 전 필수**) | MA |
| `/create-wi-evidence-pack` | 작업 완료 후 증거 패킷 표준화 | 각 에이전트 |
| `/ce` | Context Engineering — 에이전트별 최소 주입 번들 설계 | MA |
| `/pe` | Prompt Engineering — 에이전트 지시 강화 | MA |

### 빌드/품질 검증 스킬

| 스킬 | 설명 | 실행 명령 |
|------|------|---------|
| `/build-check` | `gradlew.bat build` 빌드 검증 | 사용자 직접 호출 가능 |
| `/test` | `gradlew.bat test` 전체 테스트 실행 | 사용자 직접 호출 가능 |
| `/test-coverage` | 테스트 커버리지 분석 | 사용자 직접 호출 가능 |
| `/typecheck` | TypeScript 컴파일 검증 (`tsc --noEmit`) | Phase 2 활성화 |
| `/lint` | 코드/문서 품질 검증 | 사용자 직접 호출 가능 |
| `/eslint` | JavaScript/TypeScript ESLint 검사 | Phase 2 활성화 |
| `/prettier` | 코드 포맷팅 검증 | Phase 2 활성화 |

### 문서/시스템 관리 스킬

| 스킬 | 설명 |
|------|------|
| `/validate-docs` | 문서 링크, 참조 무결성 확인 |
| `/sync-docs-index` | docs/index.md 및 카테고리 인덱스 동기화 |
| `/create-agent` | 새 Subagent 정의 파일 생성 |
| `/skill-creator` | 새 Skill 정의 생성 가이드 |
| `/manage-hooks` | Git 훅 관리 |
| `/react-best-practices` | React/Next.js 성능 최적화 가이드라인 (Phase 2) |

> **Phase 2 전용 스킬** (`/eslint`, `/prettier`, `/typecheck`, `/react-best-practices`)은 React 프론트엔드 전환 시 활성화됩니다. 현재는 백엔드 중심 개발 단계입니다.

---

## 5. 실제 사용 예시

### 예시 1: 새 기능 추가 — 알림 API 구현

**배경:** 사용자가 구독 중인 아티스트의 새 음원 업로드 시 알림을 받고 싶다는 요구사항이 생겼다.

```
사용자: "사용자에게 새 음원 업로드 알림을 보내는 API가 필요해"

MA: REQ 초안 제시
  - 목표: 알림 API 구현 (업로드 이벤트 → 구독자 알림)
  - 범위: Notification 엔티티, 알림 생성 API, 알림 조회 API
  - 제외: 이메일/푸시 발송 (1단계는 DB 저장만)
  - WI 계획:
      WI-A: sa → DB 스키마 + API 명세 설계
      WI-B: se → 구현 (A 완료 후)
      WI-C: re → JUnit5 테스트 (B 완료 후)
      WI-D: cr → 코드 리뷰 (B 완료 후, C와 병렬)

사용자: "좋아. 근데 알림 읽음 처리도 포함해줘"

MA: REQ 업데이트 후 "승인하시겠습니까?"

사용자: "승인"

MA: /create-wi-handoff-packet 실행
    → sa 호출 (WI-A)
    → sa 완료 후 se 호출 (WI-B)
    → B 완료 후 re, cr 병렬 호출 (WI-C, WI-D)
    → 전체 완료 후 결과 보고
```

**실제 생성 파일 예시:**
- `src/main/java/.../entity/Notification.java`
- `src/main/java/.../service/NotificationService.java`
- `src/main/java/.../controller/NotificationController.java`
- `src/test/java/.../service/NotificationServiceTest.java`
- `deliverables/agent/WI-XXX-evidence-pack.md`

---

### 예시 2: 버그 수정 — 프론트엔드 좋아요 버튼 깜빡임

**배경:** 음원 상세 페이지에서 좋아요 버튼 클릭 시 화면 전체가 리렌더링되는 문제.

```
사용자: "음원 상세 페이지에서 좋아요 버튼 클릭 시 화면이 깜빡여"

MA: REQ 초안 제시
  - 목표: TrackDetailPage 리렌더링 최적화
  - 예상 원인: 상위 컴포넌트 state 업데이트로 전체 리렌더링 발생
  - 범위: TrackDetailPage.tsx, LikeButton.tsx
  - WI 계획:
      WI-A: se → 원인 분석 + useCallback/memo 최적화

사용자: "승인"

MA: se 호출
    → 원인 분석: 좋아요 상태가 상위 컴포넌트 state에 있어 전체 리렌더링
    → 수정: LikeButton을 독립 state로 분리 + React.memo 적용
    → re가 동작 검증

MA: "수정 완료. 변경 파일: frontend/src/pages/TrackDetailPage.tsx,
     frontend/src/components/LikeButton.tsx"
```

---

### 예시 3: 문서 작업 — 온보딩 가이드 (현재 이 작업)

**배경:** 새 팀원이 프로젝트를 빠르게 이해할 수 있도록 사용자 친화적 가이드 문서가 필요했다.

```
사용자: "신규 팀원 온보딩 문서 만들어줘"

MA: REQ-20260310-ATS-013 작성 → 사용자 승인
  - 목표: docs/forYou/ 한국어 가이드 문서 세트 작성
  - WI 계획 (병렬):
      WI-001: docops → 07-기술스택-심층-분석.md
      WI-002: docops → 08-데이터베이스-스키마.md
      WI-003: docops → 09-로컬-환경-셋업.md  ← 이 문서!
      WI-004: docops → 10-Claude-에이전트-가이드.md  ← 지금 당신이 읽는 이 문서!
      WI-005: docops → 전체 검토 및 인덱스 업데이트

현재: WI-004 실행 중 (docops 에이전트가 이 파일을 작성 중)
```

이처럼 문서 작성, 코드 구현, 테스트, 리뷰 모두 동일한 REQ → WI → 에이전트 흐름을 따릅니다.

---

## 6. 산출물 추적 시스템

모든 코드 변경은 아래 체계로 추적됩니다.

```
deliverables/
├── user/                          ← 사용자가 보는 문서
│   ├── REQ-YYYYMMDD-ATS-###.md   ← 요구사항 정의 (사용자 승인 대상)
│   └── WI-*-summary.md           ← WI 완료 요약 보고서
│
└── agent/                         ← 에이전트 내부 문서 (감사/추적용)
    ├── WI-*-handoff.md            ← 에이전트에게 전달하는 작업 지시서
    └── WI-*-evidence-pack.md      ← 완료 증거 (생성 파일 목록, 테스트 결과)
```

### 추적 흐름

```
사용자 요구사항
    |
    v
REQ-20260310-ATS-013 (사용자 승인 완료)
    |
    v
WI-20260310-ATS-001 ~ 005 (에이전트별 실행)
    |
    v
deliverables/agent/WI-*-evidence-pack.md (무엇을 어떻게 했는지 기록)
    |
    v
Git 커밋 (feat: REQ-20260310-ATS-013 온보딩 가이드 작성)
```

**모든 코드 변경 ↔ WI ↔ REQ ↔ 사용자 요구사항**으로 추적 가능합니다.

---

## 7. 주의사항 (Do's and Don'ts)

### 해야 할 것

- 요구사항이 생기면 Claude에게 자연어로 말하기 (한국어 OK)
- REQ 검토 시 범위/성공기준을 꼼꼼히 확인하고 승인하기
- 중요한 아키텍처 결정은 sa 에이전트를 통해 ADR로 문서화 요청
- 작업 종료 시 "여기까지 커밋해줘"로 진행 내용 보존
- 세션 시작 시 "오늘은 [목표]를 하려고 해"로 컨텍스트 빠르게 세팅

### 하지 말아야 할 것

| 금지 행동 | 이유 |
|---------|------|
| REQ 승인 없이 파일 수정 요청 | MA가 거부함. REQ 없이는 범위 보장 불가 |
| 보안/DB 스키마 변경을 사전 승인 없이 진행 | 데이터 손실, 보안 취약점 위험 |
| WI 밖에서 직접 코드 수정 요청 | 추적 불가, 다음 세션에서 컨텍스트 충돌 가능 |
| `.claude/` 폴더 직접 수정 | 에이전트 시스템 내부 설정. 요청으로 변경 |
| 비밀번호/API키를 대화에 직접 입력 | 대화 로그에 남음 |

---

## 8. 비용 관리 팁 (토큰 절약)

### 토큰이란?

AI가 처리하는 텍스트의 최소 단위입니다. 한국어 1글자 ≈ 2~3 토큰, 영어 1단어 ≈ 1~2 토큰.
`/cost` 명령어로 현재 세션 사용량을 확인할 수 있습니다.

### 즉시 실천 가능한 절약 전략

| 전략 | 절약 효과 |
|------|---------|
| 구체적으로 1회에 완성 요청 (3번 왕복 대신) | 높음 |
| 파일 경로 직접 명시 ("MusicService.java") | 중간 |
| 범위 한정 ("signup 메서드만 리뷰") | 높음 |
| 하나의 REQ 완료 후 다음 세션 시작 | 중간 |

### 파일 경로(포인터) 방식 활용

```
❌ 비효율: "서비스 파일 전체 읽어줘"
   → 전체 파일 내용이 컨텍스트에 적재됨

✅ 효율적: "MusicService.java의 getMusic 메서드만 확인해줘"
   → 필요한 부분만 로드
```

### 서브에이전트 활용으로 메인 컨텍스트 보호

각 Subagent는 별도 컨텍스트에서 실행되어 메인 대화의 컨텍스트 오염을 방지합니다. 큰 탐색 작업은 Subagent에게 맡기고 결과 요약만 받는 것이 전체적으로 효율적입니다.

### 세션 관리

```
✅ 세션 1: "회원가입 API 만들어줘" → 완료 → 커밋
✅ 세션 2: "로그인 API 만들어줘" → 완료 → 커밋

❌ 하나의 세션에서 5개 기능 연속 개발
   → 후반부에 컨텍스트가 커져서 초반 내용 압축/손실
```

---

## 9. 에이전트 주입 규칙 (내부 참고)

각 에이전트는 MA가 전달하는 문서만 참조합니다. 스스로 파일을 읽지 않습니다.

| 에이전트 | 필수 주입 문서 |
|---------|-------------|
| ps, eo, tr, re | `core-principles.md` |
| sa, se, qa | `core-principles.md`, `development-standards.md` |
| pg | `core-principles.md`, `security-policy.md` |
| uv | `core-principles.md` + API 명세 |
| docops | `core-principles.md`, `documentation-standards.md`, `glossary.md` |
| cr | `core-principles.md`, `development-standards.md`, ADR |

이 규칙 덕분에 각 에이전트는 역할에 필요한 최소 컨텍스트만 받아 효율적으로 동작합니다.

---

## 원본 참조 문서

| 문서 | 경로 |
|------|------|
| 프로젝트 전체 지침 (에이전트 헌법) | `CLAUDE.md` |
| 에이전트 라우팅 매트릭스 | `CLAUDE.md` — Subagent Routing 섹션 |
| 스킬 전체 목록 | `CLAUDE.md` — Skills Reference 섹션 |
| 워크스페이스 설정 | `.claude/config/workspace.json` |
| 에이전트 역할 상세 | `.claude/agents/` 폴더 내 각 정의 파일 |
| 시작하기 | `docs/guides/ko/01-시작하기.md` |
| 효과적인 대화법 | `docs/guides/ko/02-효과적인-대화법.md` |
| 시스템 한눈에 보기 | `docs/guides/ko/03-시스템-한눈에-보기.md` |
| 워크플로우 실전 | `docs/guides/ko/04-워크플로우-실전.md` |
| 실전 팁과 주의사항 | `docs/guides/ko/05-실전-팁과-주의사항.md` |
| 토큰과 비용 이해하기 | `docs/guides/ko/06-토큰과-비용-이해하기.md` |
| 표준 개발 워크플로우 | `docs/guides/development-workflow.md` |

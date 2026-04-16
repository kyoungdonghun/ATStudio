# AGENTS.md

This file provides guidance to Codex (Codex.ai/code) when working with code in this repository.

## Project Overview

**ATStudio** - 쇼츠 음악 판매 웹 애플리케이션
- 음악 크리에이터가 쇼츠용 음악을 업로드하고 판매
- 구매자가 쇼츠 영상에 사용할 음악을 검색/구매
- Tag: `ATS`

## Tech Stack

### Backend (현재)

| Category | Technology |
|----------|-----------|
| Language | Java 17 |
| Framework | Spring Boot 4.x |
| Build | Gradle 9.3.0 |
| ORM | Spring Data JPA / Hibernate |
| Security | Spring Security 6 + JWT (JJWT 0.12.5) |
| Database | MySQL 8.x |
| Test | JUnit5 + Mockito |
| Utility | Lombok, ModelMapper |
| API Docs | Springdoc OpenAPI / Swagger |
| Template | Thymeleaf (legacy SSR / compatibility) |

### Frontend (활성)

| Category | Technology |
|----------|-----------|
| Framework | React 18 |
| Language | TypeScript |
| Build | Vite 6.x |
| State | Zustand |
| HTTP | Axios |
| Lint/Format | ESLint, Prettier |

> **현재 상태:** Phase 2 React SPA가 `frontend/`에서 활성 운영 중이며, 기존 Thymeleaf UI는 레거시/호환 맥락으로만 간주한다.
> React/TypeScript/ESLint/Prettier 관련 스킬은 현재 활성 상태다.

## Package Structure

```
com.atstudio.atstudio
├── config/         ← Spring 설정 (SecurityConfig, WebConfig 등)
├── controller/     ← REST API 엔드포인트 (얇게 유지)
├── dto/            ← Request/Response DTO
├── entity/         ← JPA 엔티티
├── repository/     ← JpaRepository 인터페이스
└── service/        ← 비즈니스 로직 (핵심)
```

## Build & Test Commands

```bash
# 빌드
./gradlew build          # Linux/Mac
gradlew.bat build        # Windows

# 프론트엔드 설치/검증
cd frontend
npm install
npm run build
npm run lint
npm run typecheck

# 테스트
./gradlew test

# 실행
./gradlew bootRun

# 특정 테스트만 실행
./gradlew test --tests "com.atstudio.atstudio.SomeTest"

# 빌드 (테스트 제외)
./gradlew build -x test
```

**주의:** 현재 저장소는 Spring Boot 백엔드 + React/Vite 프론트엔드가 함께 운영된다. 프론트엔드 작업 시 TypeScript/npm/ESLint/Prettier 스킬을 실제 기준으로 사용한다.

## Core Principles (Tier 0)

### Language Policy (Three-Track)

| Context | Language | Examples |
|---------|----------|----------|
| **Conversation** | 한국어 | 응답, 설명, 커밋 메시지, 생각 |
| **Documentation** | English | 시스템 문서, 가이드, 표준, 템플릿 |
| **REQ (Exception)** | 한국어 | `deliverables/user/REQ-*.md` |

### Approval Before Execution (REQ-Based Single Gate)

**승인 필요:**
- REQ 승인만 → 범위, 방향, 작업 분할 확정

**예외 (항상 물어봐야 함):**
- 파괴적 작업: 파일 삭제, DB 스키마 변경, 데이터 삭제
- 요구사항 변경 필요한 경우
- 중요 결정: 아키텍처 선택, 라이브러리 선정
- 예상치 못한 블로커
- 보안/민감 데이터 (JWT Secret, DB 패스워드 등)

**자동 진행 가능 (REQ 승인 후):**
- Git 작업, 빌드, 테스트, 품질 검사
- WI에 명시된 파일 생성/수정
- REQ에 계획된 Subagent 위임

### Two-Set Deliverables

| Set | 목적 | 위치 |
|-----|------|------|
| User-facing | 사용자 승인/보고 | `deliverables/user/` |
| Agent-facing | 추적/감사/재현 | `deliverables/agent/` |

### Work Tracking
- 작업 단위: WI (Work Item)
- 모든 변경사항은 WI로 추적 가능해야 함

## Orchestration Gates (Enforced Rules)

### Basic Principles
- MA는 **오케스트레이션만** 수행. 직접 파일 수정 기본 금지.
- 파일 변경 시 반드시: REQ 승인 → WI 생성 → Subagent 위임

### REQ→WI→Delegation Flow

**파일 수정 금지 게이트:**
- **REQ 승인 없음**: REQ 초안/질문/승인 포인트만 출력. 파일 수정 금지.
- **WI 없음**: `/create-wi-handoff-packet`으로 WI 먼저 생성. 파일 수정 금지.
- **위임 없음**: 적절한 Subagent(Task)에게 먼저 위임. 직접 수정 금지.

### WI Chain Rule (Enforced)

**WI 완료 직후, 반드시 아래 체크를 수행:**

1. 완료된 WI가 REQ PARALLEL WORK PLAN에서 어떤 WI를 **Blocks** 하는가?
2. 해당 다음 WI가 존재하면 → **즉시 `/create-wi-handoff-packet` 생성 → Subagent 위임**
3. 모든 WI가 완료된 경우에만 REQ를 닫고 다음 작업으로 넘어감

**위반 패턴 (NEVER):**
- WI 완료 후 deliverable 작성, 커밋, 메모리 업데이트를 하면서 다음 WI 트리거를 빠뜨리는 것
- 비정상 흐름(MA 직접 개입 등) 처리 후 복귀 시 체인 확인 생략

### Skill Gates (Enforced)

- **REQ 승인 전**: `/create-req` 스킬로 REQ 초안 작성
- **Subagent 호출 전 (필수)**: 반드시 `/create-wi-handoff-packet` 스킬 사용
  - **절대 수동으로 WI 패킷 작성 금지**
- **작업 완료 후**: `/create-wi-evidence-pack`으로 Evidence Pack 표준화

## Subagent Routing

### Routing Matrix & Roles

| 에이전트 | 역할 | 모델 | 우선순위 |
|---------|------|------|---------|
| `ps` | Product Strategist - REQ 초안, 의도 명확화 | sonnet | HIGH |
| `eo` | Ensemble Overseer - 라우팅, 거버넌스 | opus | HIGH |
| `sa` | Software Architect - 아키텍처, ADR, JPA/API 설계 | opus | HIGH |
| `se` | Software Engineer - Java 구현, 리팩토링 | opus | MEDIUM |
| `re` | Reliability Engineer - JUnit5 테스트, 회귀 검증 | sonnet | MEDIUM |
| `pg` | Privacy Guardian - Spring Security, JWT, 보안 | opus | HIGH |
| `tr` | Technology Researcher - 기술 조사, 라이브러리 비교 | sonnet | LOW |
| `uv` | UX/UI Virtuoso - API 명세, Swagger | sonnet | MEDIUM |
| `docops` | Documentation Ops - 문서 관리 | sonnet | MEDIUM |
| `qa` | Quality Assurance - 백엔드 빌드/테스트 품질 검증 | sonnet | MEDIUM |
| `qa-fe` | QA Frontend - React/TS 타입체크, 역할×화면, 컴포넌트 패턴 | sonnet | MEDIUM |
| `qa-integ` | QA Integration - API 계약, 3-way 검증, 교차 레이어 정합성 | opus | HIGH |
| `cr` | Code Reviewer - 코드 리뷰, OWASP 매핑, 베스트 프랙티스 | opus | HIGH |

### Delegation Process

1. **REQ 확인**: 사용자 승인된 REQ 존재 확인
2. **WI 생성 (필수)**: `/create-wi-handoff-packet` 스킬 사용 (수동 작성 절대 금지)
3. **Subagent 선택**: 위 매트릭스 참고
4. **위임**: Task 도구로 Subagent 호출
5. **결과 수집**: Evidence Pack 형식으로 수집
6. **검증**: 필요 시 `re`에게 독립 검증 위임

### Multi-Subagent 협업 예시

**신규 기능 구현 (예: 음악 업로드 API):**
`ps` → `sa` → `pg` → `se` → `re` → `docops`

**보안 이슈 대응 (예: JWT 취약점):**
`pg` → `sa` → `se` → `re`

**Escalation**: 정책 위반, 라우팅 모호, 거버넌스 게이트 통과 필요 시 → `eo`

## ATStudio Coding Standards

### Java/Spring Boot 규칙

```java
// ✅ Controller - 얇게 유지
@RestController
@RequestMapping("/api/music")
@RequiredArgsConstructor
public class MusicController {
    private final MusicService musicService;

    @GetMapping("/{id}")
    public ResponseEntity<MusicResponse> getMusic(@PathVariable Long id) {
        return ResponseEntity.ok(musicService.getMusic(id));
    }
}

// ✅ Service - 비즈니스 로직
@Service
@Transactional
@RequiredArgsConstructor
public class MusicService {
    private final MusicRepository musicRepository;
}

// ✅ Entity - JPA 표준
@Entity
@Table(name = "music")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Music {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}
```

### 필수 규칙

- Java 17 문법 적극 활용 (record for DTO, sealed class 등)
- Lombok 사용 허용 (`@Getter`, `@RequiredArgsConstructor`, `@Builder` 등)
- `@Transactional` Service 레이어에 적용
- DTO/Entity 분리 철저히 (Entity를 Controller에서 직접 반환 금지)
- 예외처리는 `GlobalExceptionHandler`로 통일
- `application.yml` 민감정보는 환경변수로 처리

### MySQL 관련

- 엔티티 네이밍: snake_case (테이블명, 컬럼명)
- PK: `BIGINT AUTO_INCREMENT`
- 날짜: `created_at`, `updated_at` (BaseEntity 상속)
- 인덱스: 검색 빈도 높은 컬럼에 적용

## Tier 0 Required Documents

모든 Subagent 실행 시 반드시 로드되는 최상위 문서들.

| 순서 | 문서 경로 | 설명 |
|------|----------|------|
| 1 | `docs/standards/core-principles.md` | 시스템 헌법 (STD-001) |
| 2 | `docs/standards/documentation-standards.md` | 문서 표준 (STD-004) |
| 3 | `docs/standards/development-standards.md` | 개발 표준 (STD-002) |
| 4 | `docs/standards/glossary.md` | 용어 사전 (STD-005) |

주입 순서: `[Tier 0: 헌법] → [Tier 1: 에이전트 역할] → [Tier 2: 작업 컨텍스트] → [스냅샷]`

## Subagent Context Injection Rules

**Subagent는 스스로 문서를 읽지 않는다. MA가 주입한다.**

| 에이전트 | 필수 주입 | 선택 주입 |
|---------|----------|----------|
| `ps` | `core-principles.md` | - |
| `eo` | `core-principles.md` | 관련 정책 |
| `sa` | `core-principles.md`, `development-standards.md` | ADR |
| `se` | `core-principles.md`, `development-standards.md` | - |
| `re` | `core-principles.md` | 테스트 가이드 |
| `pg` | `core-principles.md`, `security-policy.md` | - |
| `tr` | `core-principles.md` | - |
| `uv` | `core-principles.md` | API 명세 |
| `docops` | `core-principles.md`, `documentation-standards.md`, `glossary.md` | - |
| `qa` | `core-principles.md`, `development-standards.md` | - |
| `qa-fe` | `core-principles.md`, `development-standards.md` | react-best-practices |
| `qa-integ` | `core-principles.md`, `development-standards.md` | `api-spec.md`, 보안 정책 |
| `cr` | `core-principles.md`, `development-standards.md` | 보안 정책, ADR |

## Key Directory Layout

```
ATStudio/
├── .claude/                     ← 프로젝트 실자산 (agents/config/scripts/legacy skills)
│   ├── agents/                  ← 13개 에이전트 정의 (ps, eo, sa, se, re, pg, tr, uv, docops, qa, qa-fe, qa-integ, cr)
│   ├── skills/                  ← Claude 세션용 스킬/참조 자산
│   ├── config/                  ← workspace.json, context-injection-rules.json
│   └── scripts/                 ← 시스템 자동화 스크립트 (Python)
├── .agents/
│   └── skills/                  ← Codex에서 직접 노출되는 프로젝트 스킬
├── AGENTS.md                    ← 이 파일 (프로젝트 지침서)
├── CLAUDE.md                    ← Claude 세션의 1차 진입점
├── frontend/                    ← React/Vite SPA
├── src/
│   ├── main/java/com/atstudio/atstudio/   ← Spring Boot 소스
│   ├── main/resources/          ← application.yml, templates/
│   └── test/java/               ← JUnit5 테스트
├── deliverables/
│   ├── user/                    ← REQ-*.md, WI-*-summary.md
│   └── agent/                   ← WI-*-handoff.md, WI-*-evidence-pack.md
├── docs/
│   ├── standards/               ← Tier 0: 헌법, 개발표준, 용어사전
│   ├── policies/                ← Tier 1: 보안, 실행, 품질 정책
│   ├── architecture/            ← Tier 1: 시스템 설계
│   ├── guides/                  ← Tier 2: 워크플로우, 운영 가이드
│   ├── templates/               ← Tier 2: ADR, WI 등 템플릿
│   └── registry/                ← Tier 2: 프로젝트, 자산 레지스트리
├── build.gradle                 ← Gradle 빌드 설정
└── gradlew.bat                  ← Gradle 래퍼 (Windows)
```

## Architecture

```
사용자 → MA (단일 접점) → Subagents (병렬 전문화)
              ↓
    REQ 초안 → 사용자 승인 → WI 분할 → 위임 → 결과 수집 → 보고
```

## Skills Reference

### 핵심 워크플로우 스킬

| 스킬 | 설명 |
|------|------|
| `/create-req` | 사용자 발화 → REQ 정규화 |
| `/create-wi-handoff-packet` | 표준 WI 핸드오프 패킷 생성 (Subagent 호출 전 필수) |
| `/create-wi-evidence-pack` | Evidence Pack 표준화 (작업 완료 후) |
| `/ce` | Context Engineering - 최소 주입 번들 설계 |
| `/pe` | Prompt Engineering - Subagent 지시 강화 |

### 빌드/품질 검증 스킬

| 스킬 | 설명 |
|------|------|
| `/build-check` | `gradlew.bat build` 빌드 검증 |
| `/test` | `gradlew.bat test` 테스트 실행 |
| `/test-coverage` | 테스트 커버리지 분석 |
| `/typecheck` | Java 컴파일 검증 |
| `/lint` | 코드/문서 품질 검증 |

### 문서/시스템 관리 스킬

| 스킬 | 설명 |
|------|------|
| `/validate-docs` | 문서 무결성 검증 |
| `/sync-docs-index` | 문서 인덱스 동기화 |
| `/create-agent` | 새 Subagent 정의 생성 |
| `/skill-creator` | 새 Skill 생성 가이드 |
| `/manage-hooks` | Git 훅 관리 |

### Phase 2 활성 (React SPA 운영 중)

| 스킬 | 설명 |
|------|------|
| `/eslint` | JavaScript/TypeScript 코드 품질 검사 |
| `/prettier` | 코드 포맷팅 검증 |
| `/react-best-practices` | React/Next.js 성능 최적화 가이드라인 |

## Deliverables Naming Convention

**프로젝트 태그: `ATS`**

| 산출물 | 형식 | 예시 |
|--------|------|------|
| REQ | `REQ-YYYYMMDD-ATS-###.md` | `REQ-20260211-ATS-001.md` |
| WI 요약 | `WI-YYYYMMDD-ATS-###-summary.md` | `WI-20260211-ATS-001-summary.md` |
| WI 증거 | `WI-YYYYMMDD-ATS-###-evidence-pack.md` | `WI-20260211-ATS-001-evidence-pack.md` |
| WI 핸드오프 | `WI-YYYYMMDD-ATS-###-handoff.md` | `WI-20260211-ATS-001-handoff.md` |

## Memory Management Rules

- MEMORY.md는 200줄 제한. 초과 시 정리 필요.
- **정리 기준**: 완료된 REQ 상세 내역, 오래된 이슈 로그 등 현재 세션과 무관한 내용 제거.
- **제거 전 체크**: 다음 프로젝트에 재사용 가능한 꿀팁(기술 패턴, 함정, 프로세스 교훈)이면 제거 전에 `docs/retrospective/kick.md`에 먼저 기록.
- MEMORY.md Key Paths 섹션은 항상 상단 유지 (압축 시 소실 방지).

## Document Reading Policy

- substantial work 시작 전에는 저장소 전체를 한꺼번에 읽지 말고, 현재 작업과 직접 관련된 최소 문맥부터 로드한다.
- 항상 우선 확인: `AGENTS.md`, `docs/standards/core-principles.md`, `docs/standards/development-standards.md`, `docs/standards/documentation-standards.md`, `docs/standards/glossary.md`
- SR/문서 작업 시 추가 확인: `docs/SR/`, `docs/templates/`, `docs/design/`, `docs/ui/`, 관련 `deliverables/user/`, `deliverables/agent/`
- UI/프론트엔드 작업 시 추가 확인: `docs/ui/`, `docs/design/`, 필요한 `docs/templates/`, 대응되는 `frontend/` 구현 파일
- 아키텍처/오케스트레이션/정책 작업 시 추가 확인: `docs/architecture/`, `docs/policies/`, `docs/registry/`, 관련 설정 파일
- 프로젝트 문서가 `CLAUDE.md` 또는 `.claude/` 경로를 참조하는 경우, 이를 현재도 유효한 Claude 운영 컨텍스트로 간주한다.
- 어떤 폴더도 "자동으로 이미 다 읽었다"고 가정하지 말고, 현재 작업의 근거가 되는 문서만 단계적으로 확장해서 읽는다.

## Documentation Entry Points

| 문서 | 설명 | Tier |
|------|------|------|
| `docs/standards/core-principles.md` | 시스템 헌법 + ATStudio 도메인 원칙 | 0 |
| `docs/standards/development-standards.md` | Java/Spring Boot 코딩 표준 (Section 2A) | 0 |
| `docs/standards/glossary.md` | 용어 사전 + ATStudio 도메인 용어 | 0 |
| `docs/policies/security-policy.md` | JWT/MySQL 시크릿 관리 정책 | 1 |
| `docs/templates/ma-session-kickoff-prompt.md` | 새 세션 킥오프/REQ 시작 템플릿 | 2 |
| `docs/architecture/system-design.md` | 멀티에이전트 시스템 설계 원칙 | 1 |
| `docs/index.md` | 전체 문서 인덱스 | - |

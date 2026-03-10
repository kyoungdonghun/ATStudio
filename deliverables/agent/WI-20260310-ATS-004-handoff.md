[WI HEADER]
WI ID: WI-20260310-ATS-004 (REQ logical: WI-049)
REQ: REQ-20260310-ATS-013
Agent: docops
Depends On: -
Blocks: WI-20260310-ATS-005 (WI-050 검토)

[WI SUMMARY]
Why: 신규 팀원이 첫날 바로 개발을 시작할 수 있도록 로컬 환경 셋업 가이드(09)를 제공하고,
     Claude 에이전트 시스템을 이해·활용할 수 있도록 심층 가이드(10)를 작성.
     특히 에이전트 가이드는 라우팅 매트릭스, REQ→WI→위임 워크플로우 전체를 포함해야 함.
Scope (in/out):
  In:
    - docs/forYou/09-로컬-환경-셋업.md    — 백엔드+프론트엔드 로컬 실행 완전 가이드
    - docs/forYou/10-Claude-에이전트-가이드.md — AI 에이전트 시스템 심층 설명 (부록)
  Out:
    - 다른 docs/forYou/ 문서 생성 (WI-046~048 담당)
    - 기존 docs/ 문서 수정 금지
    - 코드 변경 금지
DoD:
  - 2개 파일 모두 docs/forYou/ 에 생성됨
  - 한국어로 작성됨
  - 09번 문서: 아무것도 모르는 상태에서 따라하면 서버가 뜨도록 단계별 안내
  - 10번 문서: 에이전트 라우팅 매트릭스 전체, REQ→WI→위임 흐름, 실제 사용 예시 포함
Constraints/Forbidden:
  - docs/ 내 기존 파일 수정 금지
  - 코드 변경 금지
  - 영어로 내용 작성 금지 (헤더는 한/영 혼용 허용)
  - 에이전트 가이드는 개요만 쓰지 말고 라우팅 매트릭스 및 실제 명령어 예시 필수 포함

[ACCEPTANCE CRITERIA]
Functional:
  - [ ] docs/forYou/09-로컬-환경-셋업.md 생성됨
        (사전조건 → 백엔드 실행 → 프론트엔드 실행 → 확인 URL 목록 포함)
  - [ ] docs/forYou/10-Claude-에이전트-가이드.md 생성됨
        (에이전트 역할표, REQ→WI→위임 흐름도, 스킬 목록, 실제 사용 예시 포함)
Quality:
  - [ ] 모든 문서 한국어 작성
  - [ ] 09번: 실제 명령어 코드블록으로 제시
  - [ ] 10번: 에이전트 11개 전체 역할 설명 포함
  - [ ] 10번: 실제 사용 예시 최소 2개 (신기능 구현 / 버그 수정)

[INPUT POINTERS]
Tier 0 (Constitution):
  - docs/standards/core-principles.md

Tier 0 (Documentation Standards - docops):
  - docs/standards/documentation-standards.md
  - docs/standards/glossary.md

Tier 2 (REQ & Context):
  - deliverables/user/REQ-20260310-ATS-013.md

Source Files (읽고 합성할 파일들):
  로컬 환경 셋업 소스:
  - CLAUDE.md (Build & Test Commands 섹션, 로컬 개발 환경 섹션)
  - src/main/resources/application.yml          ← DB/서버 설정 확인
  - frontend/package.json                       ← npm 스크립트 확인
  - frontend/vite.config.ts                     ← 프록시 설정 확인

  Claude 에이전트 가이드 소스:
  - CLAUDE.md (전체 — 특히 Subagent Routing, Skills Reference 섹션)
  - .claude/config/workspace.json               ← 에이전트/스킬 목록
  - docs/guides/ko/01-시작하기.md               ← Claude Code 기초
  - docs/guides/ko/02-효과적인-대화법.md        ← 대화 팁
  - docs/guides/ko/03-시스템-한눈에-보기.md     ← 시스템 개요
  - docs/guides/ko/04-워크플로우-실전.md        ← REQ→WI 워크플로우
  - docs/guides/ko/05-실전-팁과-주의사항.md     ← 주의사항
  - docs/guides/ko/06-토큰과-비용-이해하기.md   ← 비용 개념
  - docs/guides/development-workflow.md         ← 개발 워크플로우

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260310-ATS-004-summary.md:
  - 생성된 2개 문서 목록 및 간략 설명
  - 품질 셀프 체크 결과
Agent-facing -> deliverables/agent/WI-20260310-ATS-004-evidence-pack.md:
  - 생성 파일 경로 목록
  - 에이전트 가이드 커버리지 (에이전트 몇 개 설명했는지, 스킬 몇 개 포함됐는지)
  - 다음 WI (검토 단계)를 위한 인수인계 노트
Handoff Packet -> deliverables/agent/WI-20260310-ATS-004-handoff.md:
  - 이 파일 (추적용)

[TRACEABILITY REQUIREMENTS]
Evidence pointers: 생성된 파일 경로 명시 필수
Rollback: docs/forYou/09~10 파일 삭제로 롤백 가능

[DETAILED INSTRUCTIONS]

### 09-로컬-환경-셋업.md 작성 지침

구성:
1. 사전 조건 (설치 필요 도구)
   - Java 17 (JDK)
   - Gradle (gradlew.bat 내장이라 별도 설치 불필요)
   - Node.js 18+ / npm
   - MySQL 8.x
   - Git
   - IDE 추천 (IntelliJ IDEA, VS Code)

2. 저장소 클론
   ```
   git clone <repo-url>
   cd ATStudio
   ```

3. MySQL 설정
   - DB 생성: atstudio
   - 사용자/비밀번호 설정
   - application.yml에서 기본값: 비밀번호 1234 (개발용)

4. 백엔드 실행
   ```bash
   # Windows
   gradlew.bat bootRun

   # 또는 빌드 후 실행
   gradlew.bat build -x test
   java -jar build/libs/atstudio-*.jar
   ```
   - 실행 확인: http://localhost:8080
   - Swagger UI: http://localhost:8080/swagger-ui/index.html

5. 프론트엔드 실행
   ```bash
   cd frontend
   npm install
   npm run dev
   ```
   - 실행 확인: http://localhost:5173
   - 백엔드 프록시: /api/* → localhost:8080 자동 포워딩

6. 자주 발생하는 문제 해결
   - MySQL 연결 오류: DB 생성 여부, 비밀번호 확인
   - 포트 충돌: 8080, 5173 포트 사용 중인 프로세스 확인
   - npm PATH 오류 (Windows): export PATH="$PATH:/c/Program Files/nodejs"
   - Gradle 빌드 실패: Java 버전 확인 (java -version)

7. 빠른 참조
   | 서비스 | URL |
   |--------|-----|
   | 프론트엔드 | http://localhost:5173 |
   | 백엔드 API | http://localhost:8080 |
   | Swagger UI | http://localhost:8080/swagger-ui/index.html |

### 10-Claude-에이전트-가이드.md 작성 지침

구성:
1. 이 시스템이 뭔가요?
   - ATStudio는 Claude AI를 오케스트레이터(MA)로 활용하는 AI-assisted 개발 프로젝트
   - 개발자가 Claude Code에 요구사항을 말하면 → Claude가 REQ 작성, WI 분할, 전문 에이전트에 위임 → 결과 수집
   - 왜 이 방식을 쓰는가 (장점: 반복 작업 자동화, 표준 강제, 추적 가능성)

2. 에이전트 역할 매트릭스 (11개 전체)
   | 에이전트 | 이름 | 역할 | 주로 하는 일 |
   |---------|------|------|------------|
   | ps | Product Strategist | REQ 초안 | 요구사항 명확화, 의도 파악 |
   | eo | Ensemble Overseer | 오케스트레이션 | 라우팅, 거버넌스, 정책 준수 |
   | sa | Software Architect | 아키텍처 | ADR 작성, JPA/API 설계 |
   | se | Software Engineer | 구현 | Java 코드 작성, 리팩토링 |
   | re | Reliability Engineer | 테스트 | JUnit5, 회귀 검증 |
   | pg | Privacy Guardian | 보안 | Spring Security, JWT 검토 |
   | tr | Technology Researcher | 기술 조사 | 라이브러리 비교, 기술 검증 |
   | uv | UX/UI Virtuoso | UI/API 명세 | Swagger, 화면 설계 |
   | docops | Documentation Ops | 문서 관리 | 문서 생성/업데이트/색인 |
   | qa | Quality Assurance | 품질 | 빌드/테스트/린트 검증 |
   | cr | Code Reviewer | 코드 리뷰 | 베스트 프랙티스, 보안 |

3. REQ → WI → 위임 워크플로우 (핵심!)
   Step 1: 요구사항 말하기
     → Claude가 /create-req로 REQ 초안 작성
     → 사용자가 검토 후 승인
   Step 2: WI 생성
     → Claude가 /create-wi-handoff-packet으로 작업 단위 분할
     → 각 WI마다 담당 에이전트, 입력 파일, 완료 조건 명시
   Step 3: 에이전트 위임
     → Claude가 Agent Tool로 해당 에이전트 호출
     → 에이전트가 독립적으로 작업 수행
   Step 4: 결과 수집
     → Evidence Pack 형태로 결과 보고
     → 필요 시 re(테스트), cr(리뷰) 에이전트 추가 검증

4. 스킬 목록 (사용자가 직접 호출 가능한 /명령어)
   핵심 워크플로우:
   - /create-req: 요구사항을 REQ 문서로 정규화
   - /create-wi-handoff-packet: WI 핸드오프 패킷 생성 (에이전트 호출 전 필수)
   - /create-wi-evidence-pack: 작업 완료 후 증거 패킷 생성

   빌드/품질:
   - /build-check: Gradle 빌드 검증
   - /test: 테스트 실행
   - /typecheck: 타입 검사
   - /lint: 코드/문서 품질 검사
   - /eslint: TypeScript 린트
   - /prettier: 코드 포맷 검증

   문서 관리:
   - /validate-docs: 문서 무결성 검증
   - /sync-docs-index: 문서 인덱스 동기화

5. 실제 사용 예시

   예시 1: 새 기능 추가 (음원 좋아요 API)
   ---
   사용자: "음원 좋아요 토글 API를 추가하고 싶어"
   Claude: /create-req 실행 → REQ 초안 제시
   사용자: "승인"
   Claude: /create-wi-handoff-packet → WI-001(sa: API 설계) + WI-002(se: 구현) + WI-003(re: 테스트) 생성
           → 각 에이전트 병렬 위임
   결과: 구현 완료 + 테스트 통과 + Evidence Pack 생성

   예시 2: 버그 수정 (JWT 토큰 만료 처리)
   ---
   사용자: "로그인 후 1시간 지나면 API 호출이 401 에러 나"
   Claude: /create-req → REQ 초안 (보안 이슈 분류)
   사용자: "승인"
   Claude: pg(보안 분석) → se(수정) → re(검증) 순차 위임
   결과: 토큰 리프레시 로직 수정 + 회귀 테스트 통과

6. 산출물 추적 시스템
   - deliverables/user/: 사용자가 보는 REQ, WI 요약
   - deliverables/agent/: 에이전트가 보는 핸드오프 패킷, 증거 팩
   - 모든 변경사항은 WI로 추적 가능 (코드 변경 ↔ WI ↔ REQ ↔ 요구사항)

7. 주의사항 (Don'ts)
   - REQ 승인 없이 파일 수정 요청 금지 → Claude가 거부함
   - 에이전트 없이 직접 수정 → 추적 불가능 (비권장)
   - 보안/DB 스키마 변경은 반드시 사전 승인 필요

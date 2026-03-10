[WI HEADER]
WI ID: WI-20260310-ATS-001 (REQ logical: WI-046)
REQ: REQ-20260310-ATS-013
Agent: docops
Depends On: -
Blocks: WI-20260310-ATS-005 (WI-050 검토)

[WI SUMMARY]
Why: 신규 팀원이 git clone 직후 docs/forYou/를 펼쳤을 때 가장 먼저 만나는 색인(00-README)과,
     프로젝트가 "무엇인가"(01-개요) + "어떻게 생겼나"(02-구조)를 깊이 이해할 수 있는 3개 문서 작성.
Scope (in/out):
  In:
    - docs/forYou/00-README.md   — 전체 문서 지도, 읽는 순서 안내, 빠른 참조 링크
    - docs/forYou/01-프로젝트-개요.md — 서비스 목적/비즈니스 모델/주요 사용자/핵심 기능
    - docs/forYou/02-프로젝트-구조.md — 디렉터리 트리 + 각 폴더 역할 심층 설명
  Out:
    - 다른 docs/forYou/ 문서 생성 (WI-047~049 담당)
    - 기존 docs/ 문서 수정 금지
    - 코드 변경 금지
DoD:
  - 3개 파일 모두 docs/forYou/ 에 생성됨
  - 한국어로 작성됨
  - 00-README.md가 WI-047~049 생성 예정 문서까지 자리확보(placeholder) 포함
  - 신규 팀원 관점에서 배경 지식 없이도 이해 가능한 수준
  - 각 문서에 "원본 문서 경로" 섹션 포함
Constraints/Forbidden:
  - docs/ 내 기존 파일 수정 금지
  - 코드 변경 금지
  - 영어로 내용 작성 금지 (헤더는 한/영 혼용 허용)

[ACCEPTANCE CRITERIA]
Functional:
  - [ ] docs/forYou/00-README.md 생성됨 (WI-046~050 전체 문서 색인 포함, 읽는 순서 안내)
  - [ ] docs/forYou/01-프로젝트-개요.md 생성됨 (서비스 목적, 비즈니스 모델, 사용자 유형, 핵심 기능)
  - [ ] docs/forYou/02-프로젝트-구조.md 생성됨 (디렉터리 트리 + 각 폴더 역할 심층 설명 포함)
Quality:
  - [ ] 모든 문서 한국어 작성
  - [ ] 원본 참조 문서 경로 링크 포함
  - [ ] 신규 팀원 관점 검증: 기술 용어 첫 등장 시 간단 설명 포함

[INPUT POINTERS]
Tier 0 (Constitution):
  - docs/standards/core-principles.md

Tier 0 (Documentation Standards - docops):
  - docs/standards/documentation-standards.md
  - docs/standards/glossary.md

Tier 2 (REQ & Context):
  - deliverables/user/REQ-20260310-ATS-013.md

Source Files (읽고 합성할 파일들):
  - CLAUDE.md                                  ← 프로젝트 전체 개요, 기술스택, 아키텍처
  - docs/architecture/system-design.md         ← 시스템 설계 원칙
  - docs/standards/glossary.md                 ← 용어 사전 (도메인 용어 설명에 활용)
  - build.gradle                               ← 백엔드 의존성/버전 확인
  - frontend/package.json                      ← 프론트엔드 의존성/버전 확인
  - frontend/src/router/index.tsx              ← 라우터 구조 파악 (화면 구성 힌트)
  - frontend/src/App.tsx                       ← 앱 진입점
  - src/main/java/com/atstudio/atstudio/       ← 백엔드 패키지 구조 파악

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260310-ATS-001-summary.md:
  - 생성된 3개 문서 목록 및 간략 설명
  - 품질 셀프 체크 결과
Agent-facing -> deliverables/agent/WI-20260310-ATS-001-evidence-pack.md:
  - 생성 파일 경로 목록
  - 각 파일 주요 섹션 구성
  - 다음 WI를 위한 인수인계 노트
Handoff Packet -> deliverables/agent/WI-20260310-ATS-001-handoff.md:
  - 이 파일 (추적용)

[TRACEABILITY REQUIREMENTS]
Evidence pointers: 생성된 파일 경로 명시 필수
Rollback: docs/forYou/ 폴더 전체 삭제로 롤백 가능 (신규 생성 파일만)

[DETAILED INSTRUCTIONS]

### 00-README.md 작성 지침

구성:
1. 제목: "ATStudio 신규 팀원 온보딩 가이드"
2. 이 문서 패키지의 목적 (1~2문장)
3. 추천 읽는 순서 (번호 매긴 목록 + 각 문서가 답하는 질문)
4. 빠른 참조 표 (문서명 | 한 줄 설명 | 관련 원본 문서)
5. 로컬 환경 빠른 시작 (→ 09번 문서 링크)
6. 프로젝트 현재 상태 (백엔드 초벌 완성, 프론트 구현 진행중, 브랜치: master)

### 01-프로젝트-개요.md 작성 지침

구성:
1. 한 줄 요약: "ATStudio는 쇼츠 크리에이터가 배경음악을 구매하고, 음악 아티스트가 수익을 창출하는 마켓플레이스"
2. 왜 만드는가 (배경/문제/솔루션)
3. 주요 사용자 유형
   - 비회원: 음원 목록 검색, 미리듣기
   - 구독자(SUBSCRIBER): 음원 다운로드, 라이선스 발급, 재생목록
   - 크리에이터(CREATOR): 음원 업로드/관리, 앨범 관리, 수익
   - 관리자(ADMIN): 시스템 전반 관리
4. 핵심 기능 목록 (기능 → 담당 사용자 매핑 표)
5. 비즈니스 모델 (구독 기반, 플랜 종류)
6. 현재 개발 상태
   - 백엔드: Spring Boot 4.x, 87개 API 완성
   - 프론트엔드: React+TS, 48개 화면 구현됨

### 02-프로젝트-구조.md 작성 지침

구성:
1. 전체 디렉터리 트리 (2~3레벨, 핵심 파일 포함)
2. 최상위 폴더별 역할 설명 (한 문단씩)
   - src/: Spring Boot 백엔드
   - frontend/: React 프론트엔드
   - docs/: 프로젝트 문서 (표준, 설계, 가이드 등)
   - deliverables/: REQ/WI 산출물 추적 시스템
   - .claude/: AI 에이전트 시스템 설정
3. 백엔드 패키지 구조 심층 설명
   - com.atstudio.atstudio.config/controller/dto/entity/repository/service 각 역할
   - 의존 방향 다이어그램 (텍스트 형식)
4. 프론트엔드 src/ 구조 심층 설명
   - pages/api/components/layouts/router/store/styles/types 각 역할
   - 데이터 흐름: 페이지 → API 클라이언트 → 백엔드
5. 핵심 설정 파일 안내
   - src/main/resources/application.yml
   - frontend/vite.config.ts
   - build.gradle

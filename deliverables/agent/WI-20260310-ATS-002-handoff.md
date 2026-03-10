[WI HEADER]
WI ID: WI-20260310-ATS-002 (REQ logical: WI-047)
REQ: REQ-20260310-ATS-013
Agent: docops
Depends On: -
Blocks: WI-20260310-ATS-005 (WI-050 검토)

[WI SUMMARY]
Why: 신규 팀원이 실제 코드를 작성/수정할 때 필요한 3가지 핵심 지식을 제공.
     개발 스탠다드(03)로 코딩 규칙을 이해하고, API 명세(04)로 엔드포인트 전체를 파악하고,
     유스케이스(05)로 비즈니스 흐름을 이해할 수 있도록 깊이 있게 작성.
Scope (in/out):
  In:
    - docs/forYou/03-개발-스탠다드.md  — Java/Spring Boot + React/TS + CSS 코딩 규칙 한글 요약
    - docs/forYou/04-API-명세-요약.md  — 87개 API 그룹별 상세 설명 (딥 다이브)
    - docs/forYou/05-유스케이스-요약.md — 핵심 도메인 비즈니스 흐름 설명
  Out:
    - 다른 docs/forYou/ 문서 생성 (WI-046, WI-048~049 담당)
    - 기존 docs/ 문서 수정 금지
    - 코드 변경 금지
DoD:
  - 3개 파일 모두 docs/forYou/ 에 생성됨
  - 한국어로 작성됨
  - API 명세는 87개 전체 엔드포인트를 그룹별로 망라 (요약이 아닌 실제 상세 수준)
  - 유스케이스는 시퀀스 흐름이 텍스트로 표현됨
Constraints/Forbidden:
  - docs/ 내 기존 파일 수정 금지
  - 코드 변경 금지
  - 영어로 내용 작성 금지 (헤더는 한/영 혼용 허용)
  - API 명세 원본 파일(docs/design/api-spec.md)을 단순 복사 금지 → 신규 팀원 관점 설명 추가 필수

[ACCEPTANCE CRITERIA]
Functional:
  - [ ] docs/forYou/03-개발-스탠다드.md 생성됨
        (Java 레이어별 규칙, Lombok 사용법, 트랜잭션, 예외처리, React+TS 규칙, CSS Modules 패턴)
  - [ ] docs/forYou/04-API-명세-요약.md 생성됨
        (Auth/Track/Album/Playlist/License/User/Admin/Subscription 그룹별, 각 엔드포인트 설명)
  - [ ] docs/forYou/05-유스케이스-요약.md 생성됨
        (음원 업로드→구매→라이선스 핵심 흐름 포함)
Quality:
  - [ ] 모든 문서 한국어 작성
  - [ ] 원본 참조 문서 경로 링크 포함
  - [ ] API 명세: HTTP 메서드, 경로, 인증여부, 주요 파라미터, 응답 구조 포함

[INPUT POINTERS]
Tier 0 (Constitution):
  - docs/standards/core-principles.md

Tier 0 (Documentation Standards - docops):
  - docs/standards/documentation-standards.md
  - docs/standards/glossary.md

Tier 2 (REQ & Context):
  - deliverables/user/REQ-20260310-ATS-013.md

Source Files (읽고 합성할 파일들):
  개발 스탠다드 소스:
  - docs/standards/development-standards.md    ← Java/Spring Boot 코딩 표준
  - docs/standards/frontend-standards.md       ← React/TS/CSS 프론트엔드 표준
  - docs/standards/dto-standards.md            ← DTO 설계 규칙
  - docs/standards/exception-handling.md       ← 예외처리 패턴
  - CLAUDE.md (ATStudio Coding Standards 섹션) ← 코드 예시

  API 명세 소스:
  - docs/design/api-spec.md                    ← 87개 API 전체 명세 (핵심 소스)

  유스케이스 소스:
  - docs/design/usecase/sound-track.md         ← 음원 업로드/조회
  - docs/design/usecase/sound-album.md         ← 앨범 관리
  - docs/design/usecase/sound-playlist.md      ← 재생목록
  - docs/design/usecase/user-subscription.md   ← 구독 관리
  - docs/design/usecase/user-license.md        ← 라이선스
  - docs/design/usecase/likes.md               ← 좋아요
  - docs/design/usecase/download-queue.md      ← 다운로드 큐
  - docs/design/usecase/company-certification.md ← 기업 인증
  - docs/design/usecase/user-notice.md         ← 공지
  - docs/design/usecase/user-question.md       ← 문의
  - docs/design/usecase/whitelist.md           ← 화이트리스트
  - docs/design/usecase/sound-tag.md           ← 태그
  - docs/design/usecase/business-license.md    ← 사업자 라이선스
  - docs/design/usecase/sound-playhistory.md   ← 재생 이력
  - docs/design/usecase/util.md                ← 유틸리티

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260310-ATS-002-summary.md:
  - 생성된 3개 문서 목록 및 간략 설명
  - 품질 셀프 체크 결과
Agent-facing -> deliverables/agent/WI-20260310-ATS-002-evidence-pack.md:
  - 생성 파일 경로 목록
  - API 명세 커버리지 (몇 개 엔드포인트 문서화됐는지)
  - 다음 WI를 위한 인수인계 노트
Handoff Packet -> deliverables/agent/WI-20260310-ATS-002-handoff.md:
  - 이 파일 (추적용)

[TRACEABILITY REQUIREMENTS]
Evidence pointers: 생성된 파일 경로 명시 필수
Rollback: docs/forYou/03~05 파일 삭제로 롤백 가능

[DETAILED INSTRUCTIONS]

### 03-개발-스탠다드.md 작성 지침

구성:
1. 백엔드 (Java/Spring Boot)
   a. 레이어 구조 원칙 (Controller 얇게, Service 비즈니스 로직)
   b. 어노테이션 사용 가이드 (@RestController, @Service, @Transactional, @RequiredArgsConstructor)
   c. Lombok 사용 패턴 (@Getter, @Builder, @NoArgsConstructor 등)
   d. Entity 작성 규칙 (JPA 표준, BaseEntity 상속, AccessLevel.PROTECTED)
   e. DTO 규칙 (record 활용, Entity 직접 반환 금지)
   f. 예외처리 패턴 (GlobalExceptionHandler, 커스텀 예외 계층)
   g. 보안 규칙 (Spring Security 필터 순서, JWT 처리)
   h. MySQL 네이밍 규칙 (snake_case, PK 패턴, 날짜 컬럼)
2. 프론트엔드 (React/TypeScript)
   a. CSS Modules 사용 규칙 (Tailwind/styled-components 금지)
   b. CSS Variables (tokens.css 기반 디자인 토큰 시스템)
   c. Zustand 스토어 패턴
   d. Axios 인터셉터 및 JWT 자동 갱신
   e. React Router v6 패턴 (ProtectedRoute 활용)
   f. TypeScript 타입 정의 위치 (frontend/src/types/index.ts)
3. 공통 규칙
   a. 커밋 메시지 컨벤션 (한국어)
   b. 브랜치 전략

### 04-API-명세-요약.md 작성 지침

구성: API 그룹별로 각 엔드포인트를 상세히 설명.
각 엔드포인트마다: HTTP메서드 + 경로 + 인증여부 + 요청파라미터 + 응답 형태 + 한 줄 설명

그룹 목록:
1. 인증 (Auth) — 로그인, 로그아웃, 회원가입, 이메일 인증, 비밀번호 재설정, 소셜로그인
2. 사용자 (User) — 내 정보 조회/수정, 회원탈퇴
3. 음원 (Track) — 목록, 상세, 업로드, 수정, 삭제, 좋아요, 재생이력
4. 앨범 (Album) — 목록, 상세, 생성, 수정, 삭제
5. 재생목록 (Playlist) — CRUD, 음원 추가/제거
6. 라이선스 (License) — 발급, 목록, 상세
7. 구독 (Subscription) — 플랜 목록, 구독, 변경, 취소
8. 다운로드 큐 (DownloadQueue) — 큐 등록, 조회
9. 기업인증 (CompanyCertification) — 신청, 조회, 승인/거부
10. 화이트리스트 (Whitelist) — 채널 등록/조회
11. 공지 (Notice) — 목록, 상세, 생성, 수정, 삭제
12. 문의 (Question) — 등록, 목록, 상세, 답변
13. 태그 (Tag) — 목록, 생성, 삭제
14. 관리자 (Admin) — 대시보드, 유저관리, 음원관리, 라이선스관리

### 05-유스케이스-요약.md 작성 지침

구성:
1. 핵심 비즈니스 흐름 (각 흐름을 단계별 텍스트 시퀀스로 표현)
   a. 음원 업로드 흐름 (크리에이터)
      크리에이터 로그인 → 앨범 선택/생성 → 음원 파일 업로드 → 태그 설정 → 가격 설정 → 발행
   b. 음원 구독/다운로드 흐름 (구독자)
      구독 플랜 선택 → 결제 → 음원 검색 → 미리듣기 → 다운로드 → 라이선스 발급
   c. 기업 인증 흐름
      기업 정보 입력 → 사업자등록증 업로드 → 관리자 심사 → 승인/반려
   d. 재생목록 관리
      목록 생성(최대 3개) → 음원 추가/제거 → 공개/비공개 설정
2. 사용자 권한 매트릭스 (기능별 접근 가능 역할 표)
3. 구독 상태 머신 (NONE → ACTIVE → CANCELLED, 업그레이드/다운그레이드 규칙)
   - 업그레이드: 즉시 적용 + 잔여일수 일할 계산 추가 결제
   - 다운그레이드: 현재 기간 유지, 다음 결제일부터 적용
   - 취소: CANCELLED + expires_at까지 서비스 유지
4. 핵심 도메인 용어 빠른 참조

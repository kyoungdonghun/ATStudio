[WI HEADER]
WI ID: WI-20260310-ATS-003 (REQ logical: WI-048)
REQ: REQ-20260310-ATS-013
Agent: docops
Depends On: -
Blocks: WI-20260310-ATS-005 (WI-050 검토)

[WI SUMMARY]
Why: 신규 팀원이 UI 전체 그림을 한눈에 파악할 수 있도록 48개 화면 목록(06),
     화면 간 이동 흐름(07), 그리고 데이터 모델의 전체 관계(08)를 상세하게 작성.
     "화면을 보면 서비스가 보인다" — 프론트엔드 협업의 핵심 지식 패키지.
Scope (in/out):
  In:
    - docs/forYou/06-화면-목록.md     — 48개 화면 전체, 각 화면 역할/접근권한/주요UI요소 상세 설명
    - docs/forYou/07-화면-흐름도.md   — 화면 간 이동 경로, 조건별 분기 해설
    - docs/forYou/08-DB-스키마-요약.md — 23개 테이블 구조, 관계, 핵심 컬럼 설명
  Out:
    - 다른 docs/forYou/ 문서 생성 (WI-046, WI-047, WI-049 담당)
    - 기존 docs/ 문서 수정 금지
    - 코드 변경 금지
DoD:
  - 3개 파일 모두 docs/forYou/ 에 생성됨
  - 한국어로 작성됨
  - 48개 화면 전체 커버 (누락 없이)
  - DB 23개 테이블 전체 커버
  - 각 화면마다 접근 역할(비회원/구독자/크리에이터/관리자) 명시
Constraints/Forbidden:
  - docs/ 내 기존 파일 수정 금지
  - 코드 변경 금지
  - 영어로 내용 작성 금지 (헤더는 한/영 혼용 허용)
  - 단순 목록 나열 금지 → 각 화면/테이블에 "왜 존재하는가" 설명 필수

[ACCEPTANCE CRITERIA]
Functional:
  - [ ] docs/forYou/06-화면-목록.md 생성됨 (48개 화면 전체, 역할별 그룹화, 접근권한 명시)
  - [ ] docs/forYou/07-화면-흐름도.md 생성됨 (주요 흐름 최소 5개 이상, 분기 조건 포함)
  - [ ] docs/forYou/08-DB-스키마-요약.md 생성됨 (23개 테이블 전체, 관계도 텍스트 표현)
Quality:
  - [ ] 모든 문서 한국어 작성
  - [ ] 원본 참조 문서 경로 링크 포함
  - [ ] 화면별 React 파일 경로 포함 (frontend/src/pages/...)
  - [ ] DB 테이블별 핵심 컬럼 타입 및 제약 설명 포함

[INPUT POINTERS]
Tier 0 (Constitution):
  - docs/standards/core-principles.md

Tier 0 (Documentation Standards - docops):
  - docs/standards/documentation-standards.md
  - docs/standards/glossary.md

Tier 2 (REQ & Context):
  - deliverables/user/REQ-20260310-ATS-013.md

Source Files (읽고 합성할 파일들):
  화면 목록 소스:
  - docs/ui/atstudio-front-list.md          ← 48개 화면 목록 v4 (핵심 소스)
  - frontend/src/router/index.tsx               ← 실제 라우터 경로 확인
  - frontend/src/pages/ (폴더 구조)             ← 실제 구현된 페이지 파일 경로

  화면 흐름 소스:
  - docs/ui/screen-flow.md                  ← 화면 흐름도 v1.2 (핵심 소스)
  - frontend/src/router/ProtectedRoute.tsx      ← 인증 분기 로직

  DB 스키마 소스:
  - docs/design/db-schema.md                   ← DB 스키마 v5 (23개 테이블, 핵심 소스)

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260310-ATS-003-summary.md:
  - 생성된 3개 문서 목록 및 간략 설명
  - 화면 커버리지 수치 (X/48개 화면)
  - DB 테이블 커버리지 수치 (X/23개 테이블)
Agent-facing -> deliverables/agent/WI-20260310-ATS-003-evidence-pack.md:
  - 생성 파일 경로 목록
  - 화면 목록 그룹 구성
  - 다음 WI를 위한 인수인계 노트
Handoff Packet -> deliverables/agent/WI-20260310-ATS-003-handoff.md:
  - 이 파일 (추적용)

[TRACEABILITY REQUIREMENTS]
Evidence pointers: 생성된 파일 경로 명시 필수
Rollback: docs/forYou/06~08 파일 삭제로 롤백 가능

[DETAILED INSTRUCTIONS]

### 06-화면-목록.md 작성 지침

구성: 역할별 그룹 → 각 화면 상세

역할 그룹:
1. 공개 화면 (비회원 포함 누구나)
   - 홈 (HomePage)
   - 음원 목록 (TrackListPage)
   - 앨범 목록 리스트형 (AlbumListPage)
   - 앨범 목록 이미지형 (AlbumListImagePage)
   - 앨범 상세 (AlbumDetailPage)
   - 음원 상세 (TrackDetailPage)
   - 구독 플랜 (SubscriptionPlanPage)
   - 공지 목록 (NoticeListPage)
   - 공지 상세 (NoticeDetailPage)

2. 인증 화면
   - 로그인 (LoginPage)
   - 회원가입 (SignupPage)
   - 이메일 인증 (EmailVerifyPage)
   - 비밀번호 재설정 (PasswordResetPage)
   - 소셜 로그인 (SocialLoginPage)
   - 소셜 프로필 완성 (SocialCompleteProfilePage)

3. 구독자 전용 화면
   - 재생목록 목록 (PlaylistListPage)
   - 재생목록 상세 (PlaylistDetailPage)
   - 재생목록 이미지 목록 (PlaylistImageListPage)
   - 재생목록 생성 (PlaylistCreatePage)
   - 재생목록 편집 (PlaylistEditPage)
   - 좋아요 목록 (LikeListPage)
   - 재생 이력 (PlayHistoryPage)
   - 다운로드 큐 (DownloadQueuePage)
   - 구독 결제 (SubscriptionPaymentPage)
   - 구독 관리 (SubscriptionManagePage)
   - 화이트리스트 채널 (WhitelistChannelPage)
   - 기업인증 신청 (CompanyCertApplyPage)
   - 기업인증 현황 (CompanyCertStatusPage)
   - 라이선스 목록 (LicenseListPage)
   - 라이선스 상세 (LicenseDetailPage)
   - 프로필 (ProfilePage)
   - 1:1 문의 목록 (QuestionListPage)
   - 1:1 문의 생성 (QuestionCreatePage)
   - 1:1 문의 상세 (QuestionDetailPage)

4. 크리에이터 전용 화면
   - 음원 업로드 (TrackUploadPage)
   - 음원 수정 (TrackEditPage)
   - 앨범 관리 (AlbumManagePage)
   - 앨범 생성 (AlbumCreatePage)
   - 앨범 수정 (AlbumEditPage)

5. 관리자 전용 화면
   - 대시보드 (DashboardPage)
   - 유저 관리 (UserManagePage)
   - 음원 관리 (TrackManagePage)
   - 기업인증 관리 (CompanyCertManagePage)
   - 태그 관리 (TagManagePage)
   - 공지 생성 (NoticeCreatePage)
   - 공지 수정 (NoticeEditPage)
   - 구독 관리 (SubscriptionManagePage)
   - 라이선스 관리 (LicenseManagePage)
   - 문의 관리 (QuestionManagePage)

6. 에러 화면
   - 404 (NotFoundPage)
   - 500 (ServerErrorPage)

각 화면마다 포함할 정보:
  - 화면 이름 (한국어)
  - React 파일 경로
  - URL 경로 (라우터 기준)
  - 접근 가능 역할
  - 주요 기능 (2~4개 bullet)
  - 연관 API 그룹

### 07-화면-흐름도.md 작성 지침

구성: 주요 사용자 여정별 흐름 (텍스트 + ASCII 화살표 표현)

커버할 주요 흐름:
1. 신규 사용자 가입 흐름
   홈 → 회원가입 → 이메일인증 → 로그인 → 홈(구독자 메뉴 노출)
2. 구독 후 음원 다운로드 흐름
   음원상세 → [미구독이면] 구독플랜 → 결제 → 음원상세 → 다운로드 → 라이선스 발급 확인
3. 크리에이터 음원 업로드 흐름
   앨범관리 → 앨범생성(없으면) → 음원업로드 → 태그/가격 설정 → 발행
4. 재생목록 관리 흐름
   재생목록 목록(최대 3개 제한) → 생성/상세 → 음원 추가 → 이미지 목록 보기
5. 관리자 기업인증 심사 흐름
   기업인증관리 → 신청 목록 → 상세확인 → 승인/반려 → 신청자 상태 업데이트
6. 인증 분기 흐름 (ProtectedRoute)
   보호된 라우트 접근 → [토큰 없으면] 로그인 리다이렉트 → [권한 없으면] 403

### 08-DB-스키마-요약.md 작성 지침

구성:
1. 전체 테이블 목록 (23개) — 표 형태 (테이블명 | 역할 | 핵심 관계)
2. 테이블 그룹별 상세 설명

그룹:
  A. 사용자 관련: users, user_subscriptions, subscription_plans
  B. 음원 관련: tracks, albums, tags, track_tags
  C. 재생목록: playlists, playlist_tracks
  D. 라이선스: licenses, business_licenses
  E. 활동 기록: likes, play_history, download_queue
  F. 인증/허가: company_certifications, whitelists
  G. 커뮤니티: notices, questions, question_answers
  H. 결제: payments (있다면)

각 테이블마다:
  - 테이블명
  - 목적 (1~2문장)
  - 핵심 컬럼 (이름, 타입, 역할)
  - 외래키 관계 (→ 표기)
  - 주요 인덱스

3. 관계도 텍스트 표현
   users ←1:N→ tracks (creator)
   tracks ←N:M→ tags (track_tags)
   users ←1:N→ playlists ←N:M→ tracks (playlist_tracks)
   등

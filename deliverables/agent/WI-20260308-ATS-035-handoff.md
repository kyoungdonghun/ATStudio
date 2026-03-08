[WI HEADER]
WI ID: WI-20260308-ATS-035
REQ: REQ-20260308-ATS-012
Agent: se
Depends On: -
Blocks: WI-20260308-ATS-036

---

[WI SUMMARY]
Why: React 프론트엔드 전체(WI-036~045)의 기반 인프라 구축. 이 WI가 완료되어야 모든 후속 WI가 착수 가능.

Scope (in):
  - frontend/ 디렉토리 신규 생성 (ATStudio 루트 하위)
  - Vite + React 18 + TypeScript 설정 (vite.config.ts, tsconfig.json)
  - ESLint + Prettier 설정 (.eslintrc.cjs, .prettierrc)
  - CSS 디자인 토큰 (src/styles/tokens.css) — docs/check/mockup/main.html :root 변수 그대로
  - 폴더 구조 scaffolding (components/, layouts/, pages/, features/, api/, hooks/, store/, styles/, types/, router/)
  - React Router v6 — 48개 라우트 stub + Protected Route (GUEST/USER/CREATOR/ADMIN 4단계 guard)
  - Axios 인스턴스 (src/api/client.ts) + JWT 인터셉터 (401 자동 refresh 처리)
  - Zustand 스토어 2개: playerStore (현재 재생 트랙/상태), authStore (user/token/role)
  - 공통 TypeScript 타입 (src/types/index.ts) — User, Track, Album, Playlist, Subscription 등
  - Vite proxy 설정 → http://localhost:8080/api
  - .claude/config/context-injection-rules.json react "enabled": false → true 변경

Scope (out):
  - 실제 페이지 컴포넌트 구현 금지 — 라우트 stub (빈 <div>/<h1>) 만 생성
  - 공통 UI 컴포넌트 구현 금지 (Header, PlayerBar, Button 등 → WI-036 담당)
  - API 실제 호출 로직 금지 (api/client.ts 인스턴스만, 엔드포인트 모듈은 WI-037+ 담당)
  - 백엔드 Spring Boot 코드 수정 금지

DoD:
  - frontend/ 존재, npm install + npm run dev 실행 가능
  - http://localhost:5173 에서 빈 앱 접근 가능
  - ESLint 0 errors (npm run lint)
  - TypeScript 컴파일 오류 0 (npm run typecheck 또는 tsc --noEmit)
  - 48개 라우트 stub 등록 완료 (front-list.md 기준)
  - src/api/client.ts — Axios 인스턴스 + request/response 인터셉터 완료
  - src/store/playerStore.ts + authStore.ts 구조 완료
  - context-injection-rules.json react enabled: true 확인

Constraints/Forbidden:
  - CSS-in-JS, Tailwind, styled-components 사용 금지 (CSS Modules + CSS Variables만)
  - 디자인 토큰 변수명 임의 변경 금지 (--bg0, --accent 등 main.html 기준 그대로)
  - npm run dev 실패 상태로 완료 처리 금지

---

[ACCEPTANCE CRITERIA]

Functional:
- [ ] frontend/ 디렉토리 존재 및 package.json 확인
- [ ] npm run dev 실행 후 http://localhost:5173 접근 가능
- [ ] src/styles/tokens.css — --bg0:#0F172A, --accent:#C6A75E 등 main.html 토큰 전체 포함
- [ ] src/router/index.tsx — 48개 라우트 stub 등록 (front-list.md 기준 URL 경로)
- [ ] Protected Route: GUEST / USER / CREATOR / ADMIN 4단계 guard 구현
- [ ] src/api/client.ts — baseURL: /api, JWT Authorization 헤더 자동 첨부, 401 refresh 처리
- [ ] src/store/playerStore.ts — currentTrack, isPlaying, queue, play/pause/next/prev actions
- [ ] src/store/authStore.ts — user, accessToken, role, login/logout actions
- [ ] src/types/index.ts — User, Track, Album, Playlist, Subscription, ApiResponse<T> 타입 정의
- [ ] .claude/config/context-injection-rules.json — react.enabled: true 변경 확인

Quality:
- [ ] npm run lint — 0 errors
- [ ] npm run typecheck (tsc --noEmit) — 0 errors
- [ ] npm run build — dist/ 생성 성공

---

[INPUT POINTERS]

Tier 0 (Required — se):
- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 2 (React — 이 WI에서 enabled: true로 전환하므로 수동 포함):
- .claude/skills/react-best-practices/AGENTS.md

REQ:
- deliverables/user/REQ-20260308-ATS-012.md

참조 (수정 금지):
- docs/check/mockup/main.html                ← 디자인 토큰 :root 변수 그대로 복사
- docs/check/atstudio-front-list.md          ← 48개 화면 URL 경로 (라우트 stub 기준)
- docs/design/api-spec.md                    ← TypeScript 타입 정의 참고 (§1~15 응답 구조)

수정 대상:
- .claude/config/context-injection-rules.json   ← react.enabled: false → true (1줄)
- frontend/ (신규 생성)

---

[FOLDER STRUCTURE (목표)]

```
ATStudio/
└── frontend/
    ├── public/
    ├── src/
    │   ├── api/
    │   │   └── client.ts          ← Axios 인스턴스 + 인터셉터
    │   ├── components/            ← (빈 폴더, WI-036 담당)
    │   ├── layouts/               ← (빈 폴더, WI-036 담당)
    │   ├── pages/                 ← 48개 stub 파일
    │   │   ├── public/            ← Home, TrackList, AlbumList, AlbumDetail, TrackDetail, Subscription, Notice
    │   │   ├── auth/              ← Login, Signup, EmailVerify, PasswordReset
    │   │   ├── subscriber/        ← Playlist, Purchase, License, MyAccount, SubscriptionMgmt
    │   │   ├── creator/           ← TrackUpload, TrackEdit, AlbumMgmt, Revenue
    │   │   └── admin/             ← UserMgmt, CompanyCert, TagMgmt, NoticeMgmt, Stats
    │   ├── router/
    │   │   └── index.tsx          ← createBrowserRouter + Protected Routes
    │   ├── store/
    │   │   ├── playerStore.ts
    │   │   └── authStore.ts
    │   ├── styles/
    │   │   └── tokens.css         ← :root CSS 변수 (main.html 기준)
    │   ├── types/
    │   │   └── index.ts           ← 공통 타입 정의
    │   ├── App.tsx
    │   └── main.tsx
    ├── .eslintrc.cjs
    ├── .prettierrc
    ├── index.html
    ├── package.json
    ├── tsconfig.json
    └── vite.config.ts
```

---

[KEY IMPLEMENTATION NOTES]

CSS 토큰 (src/styles/tokens.css) — main.html :root 그대로:
```css
:root {
  --bg0: #0F172A; --bg1: #111827; --bg2: #1F2937; --bg3: #253244;
  --text0: #F9FAFB; --text1: #9CA3AF; --text2: #6B7280;
  --accent: #C6A75E; --accent-dim: rgba(198,167,94,0.12); --accent-border: rgba(198,167,94,0.3);
  --border: #1E293B; --border2: #2D3748;
}
```

Vite proxy (vite.config.ts):
```ts
server: {
  proxy: { '/api': { target: 'http://localhost:8080', changeOrigin: true } }
}
```

playerStore 최소 구조:
```ts
interface PlayerState {
  currentTrack: Track | null
  isPlaying: boolean
  queue: Track[]
  play: (track: Track) => void
  pause: () => void
  next: () => void
  prev: () => void
  addToQueue: (track: Track) => void
}
```

authStore 최소 구조:
```ts
interface AuthState {
  user: User | null
  accessToken: string | null
  role: 'GUEST' | 'USER' | 'CREATOR' | 'ADMIN'
  login: (token: string, user: User) => void
  logout: () => void
  isAuthenticated: () => boolean
}
```

---

[OUTPUT CONTRACT]

User-facing  -> deliverables/user/WI-20260308-ATS-035-summary.md
Agent-facing -> deliverables/agent/WI-20260308-ATS-035-evidence-pack.md
Handoff      -> deliverables/agent/WI-20260308-ATS-035-handoff.md

---

[TRACEABILITY REQUIREMENTS]
Evidence: 생성된 주요 파일 목록 + npm run dev/lint/typecheck 실행 결과
Tests: npm run lint (0 errors), npm run typecheck (0 errors), npm run build (성공)
Rollback: frontend/ 디렉토리 삭제로 완전 복구 가능 (기존 파일 수정 없음)

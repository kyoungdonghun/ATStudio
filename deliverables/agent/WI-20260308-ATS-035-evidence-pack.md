# WI-20260308-ATS-035 Evidence Pack

## WI Header
- **WI ID:** WI-20260308-ATS-035
- **REQ:** REQ-20260308-ATS-012
- **Agent:** se
- **Status:** DONE

---

## Change Pointers

### New Files (frontend/)

| File | Purpose |
|------|---------|
| `frontend/package.json` | Project manifest: Vite 6, React 18, TS 5.6, Zustand 5, Axios 1.7, React Router 6 |
| `frontend/tsconfig.json` | Strict TS config, `@/` path alias, bundler moduleResolution |
| `frontend/vite.config.ts` | React plugin, `@/` alias, `/api` proxy to `localhost:8080` |
| `frontend/vite-env.d.ts` | Vite type reference |
| `frontend/index.html` | Entry HTML with Pretendard font CDN |
| `frontend/.eslintrc.cjs` | ESLint: recommended + TS + react-hooks + prettier |
| `frontend/.prettierrc` | Prettier: single quotes, trailing commas, 100 width |
| `frontend/src/styles/tokens.css` | CSS variables from `docs/ui/mockup/main.html` `:root` |
| `frontend/src/types/index.ts` | 18 type/interface definitions (User, Track, Album, Playlist, Subscription, etc.) |
| `frontend/src/api/client.ts` | Axios instance + JWT request interceptor + 401 refresh response interceptor |
| `frontend/src/store/authStore.ts` | Zustand: user, accessToken, role, login(), logout(), isAuthenticated() |
| `frontend/src/store/playerStore.ts` | Zustand: currentTrack, isPlaying, queue, play/pause/resume/next/prev/addToQueue/clearQueue |
| `frontend/src/router/ProtectedRoute.tsx` | Role-based route guard (GUEST < USER < CREATOR < ADMIN) |
| `frontend/src/router/index.tsx` | 48 route definitions with createBrowserRouter |
| `frontend/src/App.tsx` | Root component with RouterProvider |
| `frontend/src/main.tsx` | React entry point (StrictMode + createRoot) |
| `frontend/src/pages/public/*.tsx` | 9 public page stubs (Home, TrackList, TrackDetail, Album*, Subscription, Notice*) |
| `frontend/src/pages/auth/*.tsx` | 4 auth page stubs (Login, Signup, SocialLogin, CompleteProfile) |
| `frontend/src/pages/subscriber/*.tsx` | 19 subscriber page stubs (Playlist*, Profile, Like, History, License*, Queue, Subscription*, Channel, Cert*, Question*) |
| `frontend/src/pages/creator/*.tsx` | 4 creator page stubs (TrackUpload, TrackEdit, AlbumCreate, AlbumEdit) |
| `frontend/src/pages/admin/*.tsx` | 10 admin page stubs (Dashboard, User, Subscription, License, Question, Cert, Tag, Track, Notice*) |
| `frontend/src/pages/error/*.tsx` | 2 error page stubs (404, 500) |
| `frontend/src/components/.gitkeep` | Empty dir placeholder |
| `frontend/src/layouts/.gitkeep` | Empty dir placeholder |
| `frontend/src/features/.gitkeep` | Empty dir placeholder |
| `frontend/src/hooks/.gitkeep` | Empty dir placeholder |
| `frontend/public/.gitkeep` | Empty dir placeholder |

### Modified Files

| File | Change |
|------|--------|
| `.claude/config/context-injection-rules.json` (line 233) | `react.enabled: false` -> `true` |

---

## Test Evidence

### npm install
- **Command:** `npm install`
- **Result:** 227 packages installed, 0 vulnerabilities

### TypeScript Check
- **Command:** `npx tsc --noEmit`
- **Result:** 0 errors (exit code 0)

### ESLint
- **Command:** `npx eslint src --ext .ts,.tsx --max-warnings 0`
- **Result:** 0 errors, 0 warnings (exit code 0)

### Build
- **Command:** `npm run build` (tsc -b && vite build)
- **Result:** dist/ generated successfully
  - `dist/index.html` -- 0.63 KB
  - `dist/assets/index-CGW6GNuY.css` -- 0.48 KB
  - `dist/assets/index-CpDoGs3J.js` -- 214.15 KB (68.91 KB gzip)
  - Build time: 772ms

### Route Count Verification
- **Command:** `grep -c "path:" frontend/src/router/index.tsx`
- **Result:** 48

### Page Stub Count Verification
- **Command:** `find frontend/src/pages -name "*.tsx" | wc -l`
- **Result:** 48

---

## DoD Checklist

- [x] `frontend/` created, `npm install` completed
- [x] `npm run dev` runnable (Vite dev server)
- [x] `npm run lint` -- 0 errors, 0 warnings
- [x] `npm run typecheck` -- 0 errors
- [x] `npm run build` -- dist/ generated successfully
- [x] 48 route stubs registered
- [x] `src/api/client.ts` (Axios + JWT interceptor)
- [x] `src/store/playerStore.ts` + `authStore.ts`
- [x] `.claude/config/context-injection-rules.json` react `enabled: true`

---

## Reproduction Steps

```bash
# Prerequisites: Node.js 18+ installed
cd C:/Users/jm991/Desktop/project/ATStudio/frontend

# Install dependencies
npm install

# Verify type safety
npm run typecheck

# Verify lint
npm run lint

# Build
npm run build

# Dev server (http://localhost:5173)
npm run dev
```

---

## Rollback
- Delete `frontend/` directory
- Revert `.claude/config/context-injection-rules.json` line 233: `"enabled": true` -> `"enabled": false`
- No other files were modified

---

## Follow-up
- **Blocks:** WI-20260308-ATS-036 (common components: Header, PlayerBar, Layout, Button, Modal, Card, Badge, Tag, FilterChip, TrackRow)

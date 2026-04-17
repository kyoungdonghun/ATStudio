---
version: 2.1
last_updated: 2026-04-15
project: ATS
owner: SA
category: standard
status: active
dependencies:
  - path: development-standards.md
    reason: Overall development standards
  - path: dto-standards.md
    reason: API request/response format
  - path: exception-handling.md
    reason: Error response format
tier: 1
target_agents:
  - se
  - uv
  - sa
  - cr
task_types:
  - implementation
  - review
  - design
---

# Frontend Standards (React + TypeScript)

> **Purpose:** Define the frontend architecture, coding standards, and patterns for ATStudio's React SPA. This document reflects the **actual implemented** state as of 2026-04-15.

---

## 1. Technology Stack

| Category | Technology | Version | Notes |
|----------|-----------|---------|-------|
| Language | TypeScript | ~5.6.3 | Strict mode via tsconfig |
| Framework | React | ^18.3.1 | |
| Routing | React Router v6 | ^6.28.0 | `createBrowserRouter` API |
| State | Zustand | ^5.0.2 | All global state; no React Context for state |
| HTTP Client | Axios | ^1.7.9 | Single `client.ts` instance with interceptors |
| Build | Vite | ^6.0.5 | |
| Styling | CSS Modules + CSS Variables | — | Design tokens in `tokens.css` |
| Lint/Format | ESLint + Prettier | active | `npm run lint`, `npm run format` |

**Not in use:** TanStack Query, Zod, React Context for state management, `httpOnly` cookies.

---

## 2. Directory Structure

```
frontend/src/
├── api/                  # Axios client + domain API functions
│   ├── client.ts         # Single Axios instance (JWT interceptor, refresh logic, toUploadUrl)
│   ├── likes.ts
│   ├── playHistory.ts
│   ├── userSubscriptions.ts
│   └── ...               # One file per API domain
├── components/           # Shared UI components (DataTable, Pagination, Toast, TrackRow, etc.)
├── features/             # Reserved for feature-local composition (currently placeholder only)
├── hooks/                # Reserved for shared hooks (currently placeholder only)
├── layouts/              # MainLayout, AdminLayout
├── pages/                # Route-level page components, grouped by role
│   ├── public/           # Unauthenticated pages (HomePage, TrackListPage, etc.)
│   ├── auth/             # Auth pages (LoginPage, SignupPage, etc.)
│   ├── subscriber/       # Subscriber-only pages (PlaylistListPage, etc.)
│   ├── creator/          # Creator/admin upload and management pages
│   ├── admin/            # Admin-only pages (DashboardPage, UserManagePage, etc.)
│   └── error/            # Error pages (NotFoundPage, ServerErrorPage)
├── router/               # Router definition and route guards
│   ├── index.tsx         # createBrowserRouter with current app route table
│   ├── ProtectedRoute.tsx
│   └── SubscriberRoute.tsx
├── store/                # Zustand stores
│   ├── authStore.ts
│   ├── playerStore.ts
│   ├── likeStore.ts
│   ├── albumLikeStore.ts
│   ├── toastStore.ts
│   └── themeStore.ts
├── styles/               # Global CSS
│   └── tokens.css        # CSS Variables (design tokens, dark/light theme)
├── types/                # TypeScript type definitions
│   └── index.ts          # All shared types (ApiResponse, PagedResponse, User, Track, etc.)
├── utils/                # Shared helpers (validation, formatting, safeStorage)
├── App.tsx               # Root: <RouterProvider router={router} />
└── main.tsx              # Entry point
```

**No `contexts/` or `schemas/` directories.** `features/` and `hooks/` currently exist as placeholders, while reusable frontend helpers live in `store/`, `api/`, and `utils/`.

---

## 3. State Management

### 3.1 Decision Matrix

| State Type | Solution | Location |
|-----------|----------|----------|
| Authentication (user, token, role) | `useAuthStore` (Zustand) | `store/authStore.ts` |
| Global player (queue, playback) | `usePlayerStore` (Zustand) | `store/playerStore.ts` |
| Track likes | `useLikeStore` (Zustand) | `store/likeStore.ts` |
| Album likes | `useAlbumLikeStore` (Zustand) | `store/albumLikeStore.ts` |
| Toast notifications | `useToastStore` (Zustand) | `store/toastStore.ts` |
| Theme (dark/light) | `useThemeStore` (Zustand) | `store/themeStore.ts` |
| Server data (lists, search results) | Component local state (`useState` + `useEffect`) | Inside each page component |
| Form input | Component local state | Inside each form component |

**Rule:** Do not create new React Context for state. Use Zustand for any global or cross-component state.

### 3.2 authStore

```typescript
interface AuthState {
  user: User | null;
  accessToken: string | null;
  role: UserRole;  // 'GUEST' | 'USER' | 'ADMIN'
  login: (accessToken: string, user: User, refreshToken?: string | null) => void;
  logout: () => void;
  isAuthenticated: () => boolean;
}
```

- `accessToken` and `user` are stored in **localStorage** (persisted across page refreshes).
- `refreshToken` is also stored in `localStorage` when the login flow provides one.
- `role` defaults to `'GUEST'` when unauthenticated.
- Login pages only commit tokens to the store **after** `fetchMe()` succeeds, to avoid half-authenticated state.
- On `logout()`, the store also resets `playerStore`, `likeStore`, and `albumLikeStore`.

### 3.3 toastStore (Replaces alert())

```typescript
interface ToastState {
  toasts: Toast[];                                        // Toast = { id, type, message }
  show: (type: 'success' | 'error' | 'warning' | 'info', message: string) => void;
  dismiss: (id: number) => void;
}

// Usage
const { show } = useToastStore();
show('success', 'Music uploaded successfully');
show('error', '오류가 발생했습니다.');
show('warning', '로그인이 필요한 기능입니다.');
```

**Rule:** Never use `window.alert()` or `window.confirm()` in production code. Use toast for passive feedback and a dedicated confirm dialog component for destructive or bulk-confirm actions.

### 3.4 playerStore

Manages a singleton `Audio` element. Key state:

- `currentTrack`, `isPlaying`, `currentTime`, `duration`, `volume`, `muted`
- `queue: Track[]` — ordered play queue
- `shuffle: boolean`, `repeat: 'off' | 'all' | 'one'`

Key methods: `play(track)`, `pause()`, `resume()`, `next()`, `prev()`, `addToQueue(track)`, `reorderQueue(from, to)`, `clearQueue()`.

- `play()` auto-records play history (fire-and-forget) if the user is logged in.
- `volume` is persisted in `localStorage` under key `'playerVolume'`.

---

## 4. API Client

### 4.1 Single Axios Instance (`api/client.ts`)

```typescript
const client = axios.create({
  baseURL: '/api',
  timeout: 15_000,
  headers: { 'Content-Type': 'application/json' },
});
```

- Base URL is `/api` (no environment variable) — proxied by Vite to `http://localhost:8080` in development.
- No `withCredentials: true` — cookies are not used.

### 4.2 Request Interceptor

Reads `accessToken` from `localStorage` and attaches it as `Authorization: Bearer <token>`.

### 4.3 Token Storage (localStorage)

```
Login → Server returns { accessToken, refreshToken } in response body
      → Both stored in localStorage

API Request → Read accessToken from localStorage → Authorization header

401 Response → Read refreshToken from localStorage
             → POST /api/auth/refresh { refreshToken }
             → Receive new tokens → update localStorage → retry original request
             → If refresh fails → clear localStorage → redirect to /login
```

**No httpOnly cookies.** Both tokens are stored in `localStorage`.
Refresh is skipped for login / social-login / refresh endpoints themselves, so credential errors are not misclassified as session expiry.

### 4.4 Concurrent 401 Race Condition Prevention

Uses a queue pattern (`failedQueue`) — all concurrent 401 responses wait for a single refresh call to complete, then replay with the new token.

### 4.5 FormData Handling

Pages that upload files send `FormData` directly to `client` (Axios). The `Content-Type` header must not be set manually — Axios sets the multipart boundary automatically when the body is `FormData`.

### 4.6 Upload URL Helper

```typescript
export function toUploadUrl(path: string | null | undefined): string | null
```

Converts a relative backend storage path to a full frontend URL by prepending `/uploads/`. E.g., `"playlists/thumbnails/abc.jpg"` → `"/uploads/playlists/thumbnails/abc.jpg"`.

### 4.7 Direct API Calls (No Wrapper Functions)

Pages call `client.get(...)`, `client.post(...)`, etc. directly, or via thin domain API files (e.g., `api/likes.ts`, `api/playHistory.ts`). There are no generic `apiGet`/`apiPost`/`apiPatch` wrapper functions with `HandleConfig`.

---

## 5. Input Handling

### 5.1 Validation Approach

Validation is done with inline checks in component code — no Zod schemas or external validation library.

```typescript
// Example: inline validation
if (!title.trim()) {
  setError('제목을 입력해주세요.');
  return;
}
if (title.length > 100) {
  setError('제목은 100자 이하여야 합니다.');
  return;
}
```

### 5.2 Error Display

Inline error messages below each field.

```tsx
<input value={title} onChange={(e) => setTitle(e.target.value)} />
{error && <span className={styles.fieldError}>{error}</span>}
```

**Rule:** Use CSS Modules classes for error styles, not global class names.

---

## 6. Routing

### 6.1 Router Setup (React Router v6)

```tsx
import { createBrowserRouter } from 'react-router-dom';

export const router = createBrowserRouter(routes);

// App.tsx
export default function App() {
  return <RouterProvider router={router} />;
}
```

No `AuthProvider` or `ToastProvider` wrappers — all state is Zustand-based and available globally without React Context.
Route pages are loaded with `React.lazy()` + `Suspense` at the route level; layouts remain eagerly loaded.

### 6.2 Route Guard: ProtectedRoute

```typescript
interface ProtectedRouteProps {
  children: ReactNode;
  minRole: UserRole;  // 'USER' | 'ADMIN'
}
```

Role hierarchy: `GUEST (0) < USER (1) < ADMIN (2)`. Redirects to `/login` if not authenticated, or to `/` if role level is insufficient.

Usage in router:
```tsx
// Requires USER or higher
function authRequired(element: ReactNode): ReactNode {
  return <ProtectedRoute minRole="USER">{element}</ProtectedRoute>;
}

// Requires ADMIN
function adminOnly(element: ReactNode): ReactNode {
  return <ProtectedRoute minRole="ADMIN">{element}</ProtectedRoute>;
}
```

### 6.3 Route Guard: SubscriberRoute

Checks for an active subscription via `fetchMySubscription()`. Redirects unauthenticated users to `/login`; redirects users without an active subscription to `/subscriptions`.

```tsx
function subscriberOnly(element: ReactNode): ReactNode {
  return <SubscriberRoute>{element}</SubscriberRoute>;
}
```

### 6.4 Route Categories (49 screens)

| Guard | Count | Example paths |
|-------|-------|---------------|
| Public (no guard) | 9 | `/`, `/tracks`, `/albums`, `/notices` |
| Auth (no guard, page handles redirect) | 6 | `/login`, `/signup` |
| `authRequired` (`minRole="USER"`) | ~13 | `/profile`, `/likes`, `/licenses` |
| `subscriberOnly` | ~6 | `/playlists`, `/download-queue` |
| `adminOnly` (`minRole="ADMIN"`) | Admin layout + children | `/admin/*` |

### 6.5 Layouts

- `MainLayout` — public+subscriber routes: includes Header, PlayerBar
- `AdminLayout` — admin routes: sidebar + topbar, no PlayerBar

---

## 7. TypeScript Types

All shared types live in `frontend/src/types/index.ts`. Key types:

| Type | Purpose |
|------|---------|
| `ApiResponse<T>` | Generic wrapper: `{ message: string; data: T }` |
| `PagedResponse<T>` | Paginated wrapper: `{ dataList: T[]; pageInfo: PageInfo }` |
| `UserRole` | `'GUEST' \| 'USER' \| 'ADMIN'` (GUEST is frontend-only) |
| `User` | Auth user object stored in localStorage |
| `Track` | Track detail response |
| `TrackListItem` | Track list item (lighter than Track) |
| `Album` | Album with `likeCount` |
| `AlbumLikeItem` | Album like list item |
| `LikeItem` | Track like list item |
| `UserSubscription` | User subscription with `status: 'ACTIVE' \| 'CANCELLED' \| 'EXPIRED'` |
| `Notice` | Notice with `viewCount` and optional `attachments` |

**Rule:** Do not duplicate type definitions. All API response shapes are defined once in `types/index.ts`.

---

## 8. Styling

### 8.1 CSS Modules

Every component has a co-located `.module.css` file. No global class names in component files.

```tsx
import styles from './TrackCard.module.css';
<div className={styles.card}>...</div>
```

### 8.2 CSS Variables (Design Tokens)

All color, spacing, and layout values come from `styles/tokens.css`. Use CSS variables in `.module.css` files.

```css
/* tokens.css — dark (default) */
:root {
  --bg0: #121826;
  --text0: #F9FAFB;
  --accent: #19B981;
  --header-h: 58px;
  --player-h: 72px;
  --page-px: 48px;
  --page-max-w: 1280px;
}

/* Light theme override */
[data-theme="light"] {
  --bg0: #FFFFFF;
  --text0: #111827;
  --accent: #16A34A;
}
```

Theme is toggled by `useThemeStore.toggle()` which sets/removes the `data-theme="light"` attribute on `document.documentElement`.

### 8.3 Breakpoints

```css
/* Mobile:  max-width: 767px */
/* Tablet:  min-width: 768px  AND  max-width: 1023px */
/* Desktop: min-width: 1024px (default) */
```

**Rule:** No Tailwind CSS or styled-components. CSS Modules only.

---

## 9. Component Design Principles

### 9.1 Page Components

Page components manage their own data fetching via `useState` + `useEffect`.

```tsx
const TrackListPage = () => {
  const [tracks, setTracks] = useState<TrackListItem[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    client.get('/tracks').then((res) => {
      setTracks(res.data.dataList);
    }).finally(() => setLoading(false));
  }, []);
  // ...
};
```

### 9.2 Naming Conventions

| Element | Convention | Example |
|---------|-----------|---------|
| Component | PascalCase | `TrackCard`, `LoginPage` |
| Hook | camelCase with `use` prefix | `useAuthStore`, `usePlayerStore` |
| Store file | camelCase + `Store` suffix | `authStore.ts`, `playerStore.ts` |
| Utility | camelCase | `toUploadUrl` |
| Type/Interface | PascalCase | `Track`, `ApiResponse`, `UserRole` |
| File (component) | PascalCase.tsx | `TrackCard.tsx` |
| CSS Module | PascalCase.module.css | `TrackCard.module.css` |
| File (utility/api) | camelCase.ts | `client.ts`, `likes.ts` |

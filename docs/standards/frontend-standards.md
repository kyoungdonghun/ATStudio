---
version: 2.9
last_updated: 2026-08-14
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

> **Purpose:** Define the frontend architecture, coding standards, and patterns for ATStudio's React SPA. This document reflects the **actual implemented** state as of 2026-08-14.

---

## 1. Technology Stack

| Category | Technology | Version | Notes |
|----------|-----------|---------|-------|
| Language | TypeScript | ~5.6.3 | Strict mode via tsconfig |
| Framework | React | ^18.3.1 | |
| Routing | React Router v6 | ^6.30.4 | `createBrowserRouter` API |
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
│   ├── downloads.ts
│   ├── userSubscriptions.ts
│   └── ...               # One file per API domain
├── components/           # Shared UI components (Pagination, Modal, Toast, TrackRow, etc.)
├── hooks/                # Shared hooks (for example, usePublicCapabilities)
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

**No `contexts/`, `schemas/`, or `features/` directories.** Reusable frontend
helpers live in `hooks/`, `store/`, `api/`, and `utils/`.

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

- `play()` records browser-local play history in `localStorage`; this is not a server API call.
- `volume` is persisted in `localStorage` under key `'playerVolume'`.
- `setTrackListContext(tracks)` returns an owner cleanup. Cleanup removes only
  that owner's visible-list context and does not stop the current Track or
  alter the durable queue, shuffle, or repeat state.
- Restored and seeked current time is finite, non-negative, and clamped to the
  current media duration once a positive duration is known. Player time,
  waveform progress, and persisted progress use the same bound.

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

Every eligible protected request receives the internal retry marker before it
starts refresh or joins the in-flight refresh queue. A successful refresh
replays each marked request at most once. If a replay receives another `401`,
the interceptor rejects that second failure without another refresh, queue
entry, or replay. `skipAuthReplay` requests and authentication endpoint
exclusions remain outside refresh and queue processing.

### 4.4.1 ADMIN 403 Role Synchronization Ownership

The response interceptor coalesces a current-user refresh for generic `403`
responses received while the auth store still holds ADMIN, then rejects with
the original error and never replays the request. Requests to current-user and
authentication endpoints remain excluded.

`updateUserAdmin` sets `skipAdminRoleSync` because `UserManagePage` owns the
refresh for the exact `403 ADMIN_ROLE_REQUIRED` response and must report its
success or failure. No other status or error code activates that page-owned
refresh. The option is read only by the centralized ADMIN `403` branch; it does
not skip eligible `401` refresh/replay processing or change `skipAuthReplay`.

### 4.5 FormData Handling

Pages that upload files send `FormData` directly to `client` (Axios). The `Content-Type` header must not be set manually — Axios sets the multipart boundary automatically when the body is `FormData`.

### 4.6 Upload URL Helper

```typescript
export function toUploadUrl(path: string | null | undefined): string | null
```

Converts a relative backend storage path to a full frontend URL by prepending `/uploads/`. E.g., `"playlists/thumbnails/abc.jpg"` → `"/uploads/playlists/thumbnails/abc.jpg"`.

### 4.7 Direct API Calls (No Wrapper Functions)

Pages call `client.get(...)`, `client.post(...)`, etc. directly, or via thin domain API files (e.g., `api/likes.ts`, `api/downloads.ts`). There are no generic `apiGet`/`apiPost`/`apiPatch` wrapper functions with `HandleConfig`.

### 4.8 Public Authentication Capabilities

`usePublicCapabilities()` exposes explicit `loading`, `ready`, and `error`
states plus an explicit manual `retry()` action after every failed attempt. It
does not retry automatically. Login, signup, verification-mail, reset,
social-provider, and QA-bootstrap availability is rendered only from a `ready`
response. A missing or failed response is never interpreted as an enabled
capability. Each manual retry owns the latest request so an older response
cannot restore stale capability state.

### 4.9 Public Catalog Requests

Track and Album catalogs use a 1-based URL page and a shared page size of 20.
Malformed, non-integer, zero, negative, and beyond-last-page values replace the
URL with a bounded page before issuing the corresponding request. Album grid
and list links preserve compatible query state and therefore request the same
projection.

Every Album list/detail request has an abort signal plus a generation owner.
Only the latest mounted owner may commit data, error, empty, or loading state.
Catalog recovery copy is fixed and localized; raw Axios or server text is not
rendered.

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

Authentication and account mutations use fixed frontend messages selected by
HTTP class and allowlisted `errorCode`. Components must not render arbitrary
backend `message`, exception text, stack detail, Provider payload text, or
account-existence signals. Accepted forgot-password requests use the same
generic receipt state whether or not the submitted address belongs to an
account.

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
Route pages are loaded through the shared `createLazyPage()` wrapper, which
retains `React.lazy()` + `Suspense` at the route level; layouts remain eagerly
loaded. A rejected page import renders fixed Korean recovery UI at the current
internal URL. One explicit retry creates one fresh loader attempt. A second
failure removes retry and retains safe Home/Back actions without exposing the
raw error, chunk URL, stack, token, or local path. No polling or hard reload is
used. The `/error` server-error route and public wildcard 404 semantics remain
separate.

`RouterProvider` and test routers opt into the supported React Router v7
transition behavior. Memory routers also opt into relative-splat behavior so
tests do not preserve migration warnings. The installed dependency is not
upgraded by this configuration.

### 6.2 Route Guard: ProtectedRoute

```typescript
interface ProtectedRouteProps {
  children: ReactNode;
  minRole: UserRole;  // 'USER' | 'ADMIN'
}
```

Role hierarchy: `GUEST (0) < USER (1) < ADMIN (2)`. Redirects to Login if not
authenticated, or to the configured denial route if role level, maximum role,
or required user type is not satisfied. Login return navigation is constructed
through one shared helper from the current pathname and query; hashes are
excluded. Login revalidates the consumed target after identity loads. Absolute,
protocol-relative, malformed, auth-loop, API/upload, ADMIN-for-USER,
USER-payment-for-ADMIN, and BUSINESS-for-inappropriate-user targets fall back
to Home.

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

Checks for an active subscription via `fetchMySubscription()`. Redirects
unauthenticated users to Login with the same validated pathname-and-query
return target; redirects users without an active subscription to
`/subscriptions`. Login/subscription warnings run from an effect and are
emitted at most once per redirect reason for the mounted guard.

Access-aware return classification uses the same percent-decoded, lowercase
canonical pathname as structural route classification. Authorized navigation
preserves the original validated pathname and query. A social profile
continuation is session-scoped, one-time, and bound to the authenticated user
ID. Any previous continuation is removed before replacement; storage failure
continues profile completion without a return target; consumption removes the
record before validating the current refreshed user ID.

```tsx
function subscriberOnly(element: ReactNode): ReactNode {
  return <SubscriberRoute>{element}</SubscriberRoute>;
}
```

### 6.4 Route Categories and Counting Contract

The authoritative current count and its definitions live in
`docs/ui/atstudio-front-list.md`. Route objects, lazy page components, redirects,
modal adapters, and distinct visual page UIs are different units and must not be
collapsed into one fixed number in source comments. The table below is a routing
category guide, not a screen-count source of truth.

| Guard | Count | Example paths |
|-------|-------|---------------|
| Public (no guard) | 9 | `/`, `/tracks`, `/albums`, `/notices` |
| Auth (no guard, page handles redirect) | 6 | `/login`, `/signup` |
| `authRequired` (`minRole="USER"`) | ~13 | `/profile`, `/likes`, `/licenses` |
| `subscriberOnly` | ~4 | `/playlists`, `/downloads` |
| `adminOnly` (`minRole="ADMIN"`) | Admin layout + children | `/admin/*` |

### 6.5 Layouts

- `MainLayout` — public+subscriber routes: includes Header, PlayerBar
- `AdminLayout` — admin routes: sidebar + topbar, no PlayerBar

### 6.6 Account Route State

- `/complete-profile` retains `ProtectedRoute` and revalidates `/users/me`
  before rendering mutation controls. The frontend applies the same completion
  predicate as the backend response fields: personal phone plus `job` for an
  individual or `companyName` for a business. Complete profiles redirect to
  `/profile?tab=account`; unresolved or failed identity shows only bounded
  loading/error/retry UI. After mutation, `refreshCurrentUser()` may persist the
  refreshed identity only for the initiating session generation and user ID;
  logout or user change prevents stale persistence, and component unmount
  prevents late navigation.
- Profile query panels are limited to `account`, `edit`, `password`, and
  `subscription`. Legacy activity query keys replace-navigate to their canonical
  activity routes, while all other unsupported values render the account panel
  immediately and replace the query with `tab=account`, including browser
  history traversal.
- Profile subscription loading, success, authoritative
  `NO_ACTIVE_SUBSCRIPTION`, and retryable failure are separate states. A new
  request clears prior subscription data and only its latest result may render.

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
When route or query changes can overlap, the effect must abort the retired
request and fence commits with a constant-time generation check. Cleanup must
also release page-owned player context.

Playlist, Like, License, Question, and Download History reads key projections
by authenticated owner (user ID and token identity) plus each view's route,
page, filter, tab, or drawer session. A key change clears prior-owner state
and retires old work; only the current generation may render or commit data,
error, empty, loading, dialogs, controls, or page-owned player context. Keys
and token material must never be displayed or logged.

Handlers revalidate the current owner and projection. Multi-step Download
History operations carry one initiating key and abort signal through ID
preparation, loop iterations, browser effects, feedback, follow-up count reads,
and cleanup.

Playlist, License, and Question detail routes accept only canonical positive
ASCII decimal safe-integer IDs; invalid IDs render fixed Korean list recovery
and issue no request. Playlist capacity has independent `loading`, positive
`known`, and retryable `error` states. Create remains fail-closed until current
playlist and server capacity reads agree; no client default is substituted.

Playlist Drawer destructive mutations snapshot the current detail target and
require explicit confirmation before the request. A synchronous pending owner
fences duplicate confirms; stale owner/detail lifecycles cannot start or commit
the operation. Failure uses fixed Korean copy and retains a same-target manual
retry. Success reloads the authoritative list or detail without changing the
zero-based reorder contract.

Add-to-Playlist list loading is always visible. List failure exposes a bounded
manual retry, and subscription-required outcomes remain explicit when no parent
callback is supplied. Open and Track lifecycle generations retire list/add
responses and delayed success timers.

Components that call `URL.createObjectURL()` retain only their locally created
URL in an ownership ref. Replacement, removal, close, route/owner retirement,
and unmount revoke that URL once and clear the ref immediately. Backend/upload
URLs are never stored in that ref or passed to `URL.revokeObjectURL()`.

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

### 9.3 Shared Shell And Modal Accessibility

- `MainLayout` playback shortcuts must return before reading player state when
  the keyboard event is default-prevented, uses Alt/Ctrl/Meta/Shift, or
  originates in an interactive, editable, contenteditable, dialog, slider,
  tabbable, or ARIA composite-control target or ancestor. The existing
  play/pause and previous/next meanings remain available only from ordinary
  non-interactive document targets.
- The Header mobile menu and overlay are conditionally rendered only while the
  menu is open, so a closed menu contributes no active controls to the DOM or
  accessibility tree. Escape and overlay dismissal close the menu and restore
  the exact opener when it remains connected, enabled, visible, and outside an
  inert subtree. The theme command exposes the state-correct Korean accessible
  names `라이트 모드로 전환` and `다크 모드로 전환`. Each desktop account,
  Login, and subscription route command is one styled interactive `Link`, with
  no nested `Button`.
- A normally accepted mobile navigation command closes its source disclosure
  immediately without restoring the opener and requests one module-level
  destination-focus intent. The destination layout consumes the intent
  synchronously for same-layout and cross-layout navigation, including React
  StrictMode effect replay. Focus targets the first available H1 in an active
  main region, then the first available main region. A temporary
  `tabindex="-1"` is removed after the focus attempt; there is no `body`
  fallback, and a missing destination still consumes the one-shot intent.
- `AdminLayout` keeps its desktop sidebar mounted and renders a separate mobile
  `dialog` only while the drawer is open at the mobile breakpoint. Opening the
  drawer focuses its first command and traps Tab/Shift+Tab. The topbar content
  outside the opener, main content, and Toast boundary receive `inert` and
  `aria-hidden` isolation while open; the overlay and Toast boundary also
  prevent background pointer interaction. Escape and overlay dismissal restore
  the exact valid opener. A `matchMedia` transition out of mobile closes without
  opener restoration, removes the mobile dialog, overlay, and trap, releases
  all isolation, and preserves the desktop sidebar and active route. Accepted
  mobile ADMIN routes use the shared one-shot destination-focus contract.
- `PlaylistDrawer` keeps its established fixed layout but exposes a named modal
  dialog while open. It receives focus on entry, traps Tab/Shift+Tab within the
  drawer, closes on Escape, and returns focus to a connected enabled opener.
  Its panel controls use state-correct `aria-pressed`; keyboard-only Escape and
  focus transitions do not invoke playlist create, delete, remove, or reorder
  requests. List/detail/like read failures remain visible and offer only their
  existing scoped read retry.
- Collapsed mobile `PlayerBar` detail markup is conditionally absent. Escape is
  handled only for an expanded detail event originating inside that PlayerBar
  mobile surface, collapses the detail, and returns focus to its expander.
  Dialog-owned targets are ignored, and no document-level listener is
  installed, so Header and shared Modal Escape ownership takes precedence.
- When a shared `Modal` closes, focus restoration tries the exact valid opener,
  an explicit valid fallback, the highest surviving Modal in the stack, then
  the first available page-main H1 and page main. It never silently targets
  `body`. Only the topmost Modal owns Escape and focus trapping; nested stack
  order remains intact, and `busy` continues to block Escape, backdrop, and
  close-button dismissal while retaining focus containment.

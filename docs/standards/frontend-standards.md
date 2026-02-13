---
version: 1.0
last_updated: 2026-02-13
project: ATS
owner: SA
category: standard
status: draft
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

# Frontend Standards (Phase 2: React + TypeScript)

> **Purpose:** Define the frontend architecture, coding standards, and patterns for ATStudio's React SPA. This document applies when the project transitions from Thymeleaf SSR to React SPA (Phase 2).
>
> **Status:** Draft — will be finalized when Phase 2 begins.

---

## 1. Technology Stack

| Category | Technology | Purpose |
|----------|-----------|---------|
| Language | TypeScript | Type safety from day one |
| Framework | React 19+ | UI rendering |
| Routing | React Router 7 | Client-side routing |
| State (Auth/UI) | Context API + Custom Hooks | Low-frequency global state |
| State (Server Data) | TanStack Query (React Query) | Server data caching, loading, error states |
| Validation | Zod | Schema-based form validation + type inference |
| HTTP Client | Axios (custom wrapper) | API communication |
| Build | Vite | Fast dev server and build |
| Lint/Format | ESLint + Prettier | Code quality and formatting |

---

## 2. Directory Structure

Feature-based organization groups related code by domain.

```
src/
├── api/                  # Axios instance, React Query setup
│   ├── axiosInstance.ts
│   ├── apiClient.ts      # HTTP method wrappers (axiosGet, axiosPost, etc.)
│   └── queryClient.ts    # TanStack Query configuration
├── components/           # Shared UI components (Button, Modal, Toast, etc.)
├── contexts/             # Global contexts (AuthContext, ToastContext)
├── features/             # Domain-based feature modules
│   ├── auth/             # Login, signup (pages + components + hooks)
│   ├── music/            # Upload, search, playback
│   ├── payment/          # Purchase, settlement
│   └── admin/            # Admin dashboard
├── hooks/                # Shared custom hooks
├── layouts/              # Layout, Header, Sidebar, Footer
├── routes/               # Router definition, ProtectedRoute
├── schemas/              # Zod validation schemas
├── types/                # TypeScript type definitions
└── utils/                # Pure utility functions
    ├── inputProcessing.ts
    └── object.ts         # flattenObject, unflattenObject
```

---

## 3. State Management

### 3.1 Decision Matrix

| State Type | Solution | Rationale |
|-----------|----------|-----------|
| Authentication (user, token) | `AuthContext` | Changes infrequently, needed everywhere |
| Global UI (toast, modal) | `ToastContext` | Cross-cutting UI concerns |
| Server data (music list, search) | TanStack Query | Automatic caching, refetch, loading/error |
| Form input | Component local state | Scoped to single form |

### 3.2 AuthContext

```tsx
interface AuthContextValue {
    user: UserInfo | null;
    login: (email: string, password: string) => Promise<void>;
    logout: () => void;
    isAuthenticated: boolean;
    isAdmin: boolean;
    initializing: boolean;
}
```

- AccessToken stored **in memory only** (not localStorage — XSS protection).
- RefreshToken stored as **httpOnly + Secure + SameSite cookie** (JS cannot access).

### 3.3 ToastContext (Replaces alert())

```tsx
interface ToastContextValue {
    showToast: (message: string, type: "success" | "error" | "info") => void;
}

// Usage
const { showToast } = useToast();
showToast("Music uploaded successfully", "success");
```

**Rule:** Never override `window.alert`. Never use `alert()` in production code.

---

## 4. API Client

### 4.1 Axios Instance

```typescript
const axiosInstance = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL,
    headers: { "Content-Type": "application/json" },
    withCredentials: true, // Send httpOnly cookies
});
```

- Base URL from environment variable (no hardcoding).
- `withCredentials: true` for cookie-based refresh token.

### 4.2 HTTP Method Wrappers

```typescript
interface RequestOptions<T = unknown> {
    endpoint: string;
    body?: T;
    params?: Record<string, string | number>;
    handle?: HandleConfig;
}

interface HandleConfig {
    success?: { message?: string; navigate?: string };
    failure?: { message?: string; navigate?: string };
}

export const apiGet = <T>(opts: RequestOptions): Promise<T> =>
    fetchData({ ...opts, method: "GET" });

export const apiPost = <T>(opts: RequestOptions): Promise<T> =>
    fetchData({ ...opts, method: "POST" });

export const apiPatch = <T>(opts: RequestOptions): Promise<T> =>
    fetchData({ ...opts, method: "PATCH" });

export const apiDelete = <T>(opts: RequestOptions): Promise<T> =>
    fetchData({ ...opts, method: "DELETE" });
```

### 4.3 Response Handling (Range-Based)

```typescript
const handleResponse = (status: number, data: any, handle?: HandleConfig) => {
    if (status >= 200 && status < 300) {
        // Success
        if (handle?.success?.message) showToast(handle.success.message, "success");
        if (handle?.success?.navigate) router.navigate(handle.success.navigate);
        return data;
    } else {
        // Failure (4xx, 5xx)
        const message = handle?.failure?.message ?? data.message ?? "오류가 발생했습니다.";
        showToast(message, "error");
        if (handle?.failure?.navigate) router.navigate(handle.failure.navigate);
        return null;
    }
};
```

**Key decisions:**
- Range-based (`>= 200 && < 300`) instead of individual `case` statements.
- Toast notifications instead of `alert()`.
- No hardcoded default navigation path.

### 4.4 Token Management

```
Login → Server sets RefreshToken as httpOnly cookie
      → Server returns AccessToken in response body → stored in memory variable

API Request → Attach AccessToken from memory to Authorization header

401 Response → POST /api/auth/refresh (cookie sent automatically)
            → Receive new AccessToken → update memory → retry original request
```

**Race Condition Prevention:**

```typescript
let refreshPromise: Promise<string> | null = null;

const refreshToken = async (): Promise<string> => {
    if (!refreshPromise) {
        refreshPromise = callRefresh().finally(() => {
            refreshPromise = null;
        });
    }
    return refreshPromise;
};
```

All concurrent 401 responses share the same refresh Promise — prevents multiple refresh calls.

### 4.5 FormData Handling

When uploading files, the `Content-Type` header is removed to let the browser set the multipart boundary automatically.

```typescript
if (body instanceof FormData) {
    delete options.headers["Content-Type"];
}
```

---

## 5. Input Handling

### 5.1 Pipeline

```
User Input (Controlled Component state)
    ↓
Processing (trim, normalize — inputProcessing.ts)
    ↓
Validation (Zod schema — type-safe)
    ↓ Fail → Inline error display per field
    ↓ Pass → API submission
```

### 5.2 Zod Schema Validation

Schemas define rules, types, and error messages in one place.

```typescript
import { z } from "zod";

export const musicCreateSchema = z.object({
    title: z.string()
        .min(2, "제목은 2자 이상이어야 합니다.")
        .max(100, "제목은 100자 이하여야 합니다.")
        .regex(/^[a-zA-Z0-9가-힣\s]+$/, "제목에 특수문자를 사용할 수 없습니다."),
    description: z.string()
        .max(500, "설명은 500자 이하여야 합니다.")
        .optional(),
    genre: z.array(z.string()).min(1, "장르를 하나 이상 선택해주세요."),
    bpm: z.number().min(40).max(300).optional(),
});

// Auto-inferred TypeScript type
type MusicCreateForm = z.infer<typeof musicCreateSchema>;
```

**Advantages over manual validation:**
- Rules + validation logic + TypeScript type = one source of truth.
- Native nested object support (no flatten/unflatten needed for validation).
- Korean error messages specified directly in schema.

### 5.3 Input Processing (Pre-validation)

Processing rules remain as a custom utility since Zod handles validation only.

```typescript
type ProcessingRule = "trim" | "trimMiddle" | "toLowerCase";

const processors: Record<ProcessingRule, (v: string) => string> = {
    trim: (v) => v.trim(),
    trimMiddle: (v) => v.replace(/\s+/g, " "),
    toLowerCase: (v) => v.toLowerCase(),
};

export function processInput<T extends Record<string, unknown>>(
    data: T,
    rules: Partial<Record<keyof T, ProcessingRule[]>>
): T {
    const result = { ...data };
    for (const [key, ruleList] of Object.entries(rules)) {
        const value = result[key as keyof T];
        if (typeof value === "string" && ruleList) {
            let processed = value;
            for (const rule of ruleList as ProcessingRule[]) {
                processed = processors[rule](processed);
            }
            (result as Record<string, unknown>)[key] = processed;
        }
    }
    return result;
}
```

### 5.4 Error Display

Inline error messages below each field, not `alert()`.

```tsx
interface FieldProps {
    name: string;
    value: string;
    error?: string;
    onChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
}

const FormField = ({ name, value, error, onChange }: FieldProps) => (
    <div>
        <input name={name} value={value} onChange={onChange} />
        {error && <span className="field-error">{error}</span>}
    </div>
);
```

### 5.5 Flatten / Unflatten Utilities

For nested DTO structures that need field-level processing.

```typescript
// src/utils/object.ts
export function flattenObject(
    obj: Record<string, unknown>,
    parentKey = "",
    result: Record<string, unknown> = {}
): Record<string, unknown> {
    for (const key in obj) {
        const newKey = parentKey ? `${parentKey}.${key}` : key;
        const value = obj[key];
        if (typeof value === "object" && value !== null && !Array.isArray(value)) {
            flattenObject(value as Record<string, unknown>, newKey, result);
        } else {
            result[newKey] = value;
        }
    }
    return result;
}

export function unflattenObject(
    flatObj: Record<string, unknown>
): Record<string, unknown> {
    const result: Record<string, unknown> = {};
    for (const flatKey in flatObj) {
        const keys = flatKey.split(".");
        let current = result;
        keys.forEach((k, index) => {
            if (index === keys.length - 1) {
                current[k] = flatObj[flatKey];
            } else {
                current[k] = current[k] || {};
                current = current[k] as Record<string, unknown>;
            }
        });
    }
    return result;
}
```

---

## 6. Routing

### 6.1 Router Setup (React Router 7)

```tsx
import { createBrowserRouter, RouterProvider } from "react-router-dom";

const router = createBrowserRouter([
    {
        path: "/",
        element: <Layout />,
        children: [
            { index: true, element: <Navigate to="/music" replace /> },
            // Public routes
            { path: "music/*", element: <MusicRoutes /> },
            // Authenticated routes
            { path: "me/*", element: <ProtectedRoute><UserRoutes /></ProtectedRoute> },
            // Admin routes
            { path: "admin/*", element: <ProtectedRoute roles={["ADMIN"]}><AdminRoutes /></ProtectedRoute> },
            // Catch-all
            { path: "*", element: <Navigate to="/" replace /> },
        ],
    },
]);

function App() {
    return (
        <AuthProvider>
            <ToastProvider>
                <RouterProvider router={router} />
            </ToastProvider>
        </AuthProvider>
    );
}
```

### 6.2 ProtectedRoute

```tsx
interface ProtectedRouteProps {
    children: React.ReactNode;
    roles?: string[];
}

const ProtectedRoute = ({ children, roles }: ProtectedRouteProps) => {
    const { user, isAuthenticated, initializing } = useAuth();

    if (initializing) return <LoadingSpinner />;
    if (!isAuthenticated) return <Navigate to="/login" replace />;
    if (roles && !roles.some(role => user?.roles?.includes(role))) {
        return <Navigate to="/" replace />;
    }

    return <>{children}</>;
};
```

---

## 7. Component Design Principles

### 7.1 Single Responsibility

Each component has one purpose. Separate state logic from UI rendering.

```tsx
// State logic — hook
const useMusicSearch = (query: string) => {
    return useQuery({
        queryKey: ["music", "search", query],
        queryFn: () => apiGet({ endpoint: `/api/music/search`, params: { q: query } }),
        enabled: query.length > 0,
    });
};

// UI rendering — component
const MusicSearchResults = ({ query }: { query: string }) => {
    const { data, isLoading, error } = useMusicSearch(query);

    if (isLoading) return <LoadingSpinner />;
    if (error) return <ErrorMessage error={error} />;
    return <MusicList items={data?.dataList ?? []} />;
};
```

### 7.2 Global Components at Root

Toast and loading components are placed at the application root.

```tsx
function App() {
    return (
        <AuthProvider>
            <ToastProvider>
                <GlobalToast />
                <RouterProvider router={router} />
            </ToastProvider>
        </AuthProvider>
    );
}
```

### 7.3 Naming Conventions

| Element | Convention | Example |
|---------|-----------|---------|
| Component | PascalCase | `MusicCard`, `LoginForm` |
| Hook | camelCase with `use` prefix | `useAuth`, `useMusicSearch` |
| Utility | camelCase | `formatDate`, `processInput` |
| Type/Interface | PascalCase | `MusicResponse`, `HandleConfig` |
| File (component) | PascalCase.tsx | `MusicCard.tsx` |
| File (utility) | camelCase.ts | `inputProcessing.ts` |
| Schema | camelCase + `Schema` suffix | `musicCreateSchema` |

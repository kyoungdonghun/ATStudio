import axios, { type InternalAxiosRequestConfig, type AxiosResponse } from 'axios';
import { router } from '@/router';
import { useToastStore } from '@/store/toastStore';
import { useAuthStore } from '@/store/authStore';
import { safeStorage } from '@/utils/safeStorage';

declare module 'axios' {
  interface AxiosRequestConfig {
    skipAuthReplay?: boolean;
  }
}

const client = axios.create({
  baseURL: '/api',
  timeout: 15_000,
  headers: { 'Content-Type': 'application/json' },
});

/* ── Request Interceptor: attach JWT + fix FormData Content-Type ── */
client.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = safeStorage.getItem('accessToken');
    if (token && config.headers && !config.headers.Authorization) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    // Let Axios auto-set Content-Type with boundary for FormData
    if (config.data instanceof FormData && config.headers) {
      delete config.headers['Content-Type'];
    }
    return config;
  },
  (error) => Promise.reject(error),
);

/* ── Response Interceptor: 401 auto-refresh ── */
let isRefreshing = false;
let adminRoleSyncPromise: Promise<void> | null = null;
let failedQueue: Array<{
  resolve: (token: string) => void;
  reject: (err: unknown) => void;
}> = [];

const AUTH_REFRESH_EXCLUDED_PATHS = [
  '/auth/login',
  '/auth/logout',
  '/auth/refresh',
  '/auth/social/',
];

export function shouldSkipRefresh(config?: { url?: string | undefined }): boolean {
  const url = config?.url ?? '';
  return AUTH_REFRESH_EXCLUDED_PATHS.some((path) => url === path || url.startsWith(path));
}

export function shouldSkipAdminRoleSync(config?: { url?: string | undefined }): boolean {
  const url = config?.url ?? '';
  const path = url.split('?')[0];
  return path === '/users/me' || path === '/auth' || path.startsWith('/auth/');
}

function synchronizeAdminRole(): Promise<void> {
  if (adminRoleSyncPromise) return adminRoleSyncPromise;

  adminRoleSyncPromise = Promise.resolve()
    .then(() => useAuthStore.getState().refreshCurrentUser())
    .then(() => undefined)
    .catch(() => undefined)
    .finally(() => {
      adminRoleSyncPromise = null;
    });
  return adminRoleSyncPromise;
}

function processQueue(error: unknown, token: string | null) {
  failedQueue.forEach((prom) => {
    if (error) {
      prom.reject(error);
    } else {
      prom.resolve(token!);
    }
  });
  failedQueue = [];
}

client.interceptors.response.use(
  (response: AxiosResponse) => response,
  async (error) => {
    const originalRequest = error.config as
      | (InternalAxiosRequestConfig & { _retry?: boolean })
      | undefined;

    if (
      originalRequest &&
      error.response?.status === 403 &&
      useAuthStore.getState().role === 'ADMIN' &&
      !shouldSkipAdminRoleSync(originalRequest)
    ) {
      await synchronizeAdminRole();
      return Promise.reject(error);
    }

    if (
      !originalRequest ||
      error.response?.status !== 401 ||
      originalRequest._retry ||
      originalRequest.skipAuthReplay ||
      shouldSkipRefresh(originalRequest)
    ) {
      return Promise.reject(error);
    }

    const refreshToken = safeStorage.getItem('refreshToken');
    if (!refreshToken) {
      useAuthStore.getState().clearSession();
      return Promise.reject(error);
    }

    originalRequest._retry = true;

    if (isRefreshing) {
      return new Promise<string>((resolve, reject) => {
        failedQueue.push({ resolve, reject });
      }).then((token) => {
        if (originalRequest.headers) {
          originalRequest.headers.Authorization = `Bearer ${token}`;
        }
        return client(originalRequest);
      });
    }

    isRefreshing = true;

    try {
      const { data } = await axios.post('/api/auth/refresh', { refreshToken });
      const newAccessToken: string = data.data.accessToken;

      if (!safeStorage.setItem('accessToken', newAccessToken)) {
        throw new Error('Failed to persist refreshed authentication session');
      }
      if (data.data.refreshToken && !safeStorage.setItem('refreshToken', data.data.refreshToken)) {
        throw new Error('Failed to persist refreshed authentication session');
      }
      useAuthStore.setState({ accessToken: newAccessToken });

      processQueue(null, newAccessToken);
      if (originalRequest.headers) {
        originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
      }
      return client(originalRequest);
    } catch (refreshError) {
      processQueue(refreshError, null);
      safeStorage.removeItem('accessToken');
      safeStorage.removeItem('refreshToken');
      useAuthStore.getState().clearSession();
      useToastStore.getState().show('error', '세션이 만료되었습니다. 다시 로그인해주세요.');
      router.navigate('/login');
      return Promise.reject(refreshError);
    } finally {
      isRefreshing = false;
    }
  },
);

export default client;

/**
 * Check if an API error is a "subscription required" error (JSON responses only).
 * For blob responses (download API), use getApiErrorCode() instead.
 */
export function isSubscriptionRequired(err: unknown): boolean {
  const axErr = err as { response?: { data?: { errorCode?: string } } };
  const data = axErr?.response?.data;
  if (data && typeof data === 'object' && !(data instanceof Blob) && 'errorCode' in data) {
    return data.errorCode === 'NO_ACTIVE_SUBSCRIPTION';
  }
  return false;
}

/**
 * Extract errorCode from an API error, handling both JSON and blob responses.
 */
export async function getApiErrorCode(err: unknown): Promise<string | null> {
  const axErr = err as { response?: { data?: unknown } };
  const data = axErr?.response?.data;
  if (!data) return null;

  // Blob response (e.g. download API with responseType: 'blob')
  if (data instanceof Blob) {
    try {
      const text = await data.text();
      const json = JSON.parse(text);
      return json.errorCode ?? null;
    } catch {
      return null;
    }
  }

  // JSON response
  if (typeof data === 'object' && data !== null && 'errorCode' in data) {
    return (data as { errorCode: string }).errorCode;
  }

  return null;
}

/**
 * Convert a relative upload path from the backend to a full URL.
 * e.g. "playlists/thumbnails/abc.jpg" → "/uploads/playlists/thumbnails/abc.jpg"
 */
export function toUploadUrl(path: string | null | undefined): string | null {
  if (!path) return null;
  if (path.startsWith('/') || path.startsWith('http') || path.startsWith('blob:')) return path;
  return `/uploads/${path}`;
}

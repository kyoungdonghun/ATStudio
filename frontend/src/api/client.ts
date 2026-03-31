import axios, { type InternalAxiosRequestConfig, type AxiosResponse } from 'axios';
import { router } from '@/router';
import { useToastStore } from '@/store/toastStore';

const client = axios.create({
  baseURL: '/api',
  timeout: 15_000,
  headers: { 'Content-Type': 'application/json' },
});

/* ── Request Interceptor: attach JWT ── */
client.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem('accessToken');
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error),
);

/* ── Response Interceptor: 401 auto-refresh ── */
let isRefreshing = false;
let failedQueue: Array<{
  resolve: (token: string) => void;
  reject: (err: unknown) => void;
}> = [];

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
    const originalRequest = error.config;

    if (error.response?.status !== 401 || originalRequest._retry) {
      return Promise.reject(error);
    }

    if (isRefreshing) {
      return new Promise<string>((resolve, reject) => {
        failedQueue.push({ resolve, reject });
      }).then((token) => {
        originalRequest.headers.Authorization = `Bearer ${token}`;
        return client(originalRequest);
      });
    }

    originalRequest._retry = true;
    isRefreshing = true;

    try {
      const refreshToken = localStorage.getItem('refreshToken');
      const { data } = await axios.post('/api/auth/refresh', { refreshToken });
      const newAccessToken: string = data.data.accessToken;

      localStorage.setItem('accessToken', newAccessToken);
      if (data.data.refreshToken) {
        localStorage.setItem('refreshToken', data.data.refreshToken);
      }

      processQueue(null, newAccessToken);
      originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
      return client(originalRequest);
    } catch (refreshError) {
      processQueue(refreshError, null);
      localStorage.removeItem('accessToken');
      localStorage.removeItem('refreshToken');
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

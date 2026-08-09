import axios from 'axios';
import { beforeEach, describe, expect, it, vi } from 'vitest';

const {
  authState,
  clearSessionMock,
  navigateMock,
  refreshCurrentUserMock,
  setAuthStateMock,
  showToastMock,
} = vi.hoisted(() => ({
  authState: { role: 'USER' },
  clearSessionMock: vi.fn(),
  navigateMock: vi.fn(),
  refreshCurrentUserMock: vi.fn(),
  setAuthStateMock: vi.fn(),
  showToastMock: vi.fn(),
}));

vi.mock('@/router', () => ({
  router: { navigate: navigateMock },
}));

vi.mock('@/store/toastStore', () => ({
  useToastStore: {
    getState: () => ({ show: showToastMock }),
  },
}));

vi.mock('@/store/authStore', () => ({
  useAuthStore: {
    getState: () => ({
      clearSession: clearSessionMock,
      refreshCurrentUser: refreshCurrentUserMock,
      role: authState.role,
    }),
    setState: setAuthStateMock,
  },
}));

import client, {
  getApiErrorCode,
  isSubscriptionRequired,
  shouldSkipAdminRoleSync,
  shouldSkipRefresh,
  toUploadUrl,
} from '@/api/client';
import { safeStorage } from '@/utils/safeStorage';

function getRequestInterceptors() {
  const requestInterceptors = client.interceptors.request as unknown as {
    handlers: Array<{
      fulfilled?: (config: Record<string, unknown>) => Record<string, unknown>;
      rejected?: (error: unknown) => Promise<never>;
    }>;
  };
  const handler = requestInterceptors.handlers[requestInterceptors.handlers.length - 1];
  if (!handler?.fulfilled || !handler.rejected) {
    throw new Error('Expected the axios request interceptor to be registered.');
  }
  return handler as Required<typeof handler>;
}

function getRejectedResponseInterceptor() {
  const responseInterceptors = client.interceptors.response as unknown as {
    handlers: Array<{
      rejected?: (error: unknown) => Promise<unknown>;
    }>;
  };
  const lastHandler = responseInterceptors.handlers[responseInterceptors.handlers.length - 1];
  const rejected = lastHandler?.rejected;
  if (!rejected) {
    throw new Error('Expected the axios response interceptor to be registered.');
  }
  return rejected;
}

describe('client auth refresh exclusions', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
    clearSessionMock.mockReset();
    navigateMock.mockReset();
    refreshCurrentUserMock.mockReset();
    setAuthStateMock.mockReset();
    showToastMock.mockReset();
    authState.role = 'USER';
    localStorage.clear();
    sessionStorage.clear();
  });

  it('skips refresh logic for login and refresh endpoints', () => {
    expect(shouldSkipRefresh({ url: '/auth/login' })).toBe(true);
    expect(shouldSkipRefresh({ url: '/auth/logout' })).toBe(true);
    expect(shouldSkipRefresh({ url: '/auth/refresh' })).toBe(true);
  });

  it('skips refresh logic for social login callbacks', () => {
    expect(shouldSkipRefresh({ url: '/auth/social/google' })).toBe(true);
    expect(shouldSkipRefresh({ url: '/auth/social/kakao' })).toBe(true);
  });

  it('does not skip refresh logic for normal protected endpoints', () => {
    expect(shouldSkipRefresh({ url: '/tracks' })).toBe(false);
    expect(shouldSkipRefresh({ url: '/users/me' })).toBe(false);
    expect(shouldSkipRefresh({ url: undefined })).toBe(false);
  });

  it('excludes /users/me and auth paths from centralized admin-role sync', () => {
    expect(shouldSkipAdminRoleSync({ url: '/users/me' })).toBe(true);
    expect(shouldSkipAdminRoleSync({ url: '/users/me?fresh=true' })).toBe(true);
    expect(shouldSkipAdminRoleSync({ url: '/auth/logout' })).toBe(true);
    expect(shouldSkipAdminRoleSync({ url: '/auth/social/google' })).toBe(true);
    expect(shouldSkipAdminRoleSync({ url: '/admin/payments' })).toBe(false);
  });

  it('attaches a stored token without replacing explicit authorization', () => {
    localStorage.setItem('accessToken', 'stored-token');
    const { fulfilled } = getRequestInterceptors();
    const first = fulfilled({ headers: {} }) as { headers: Record<string, string> };
    expect(first.headers.Authorization).toBe('Bearer stored-token');
    const explicit = fulfilled({ headers: { Authorization: 'Bearer explicit' } }) as {
      headers: Record<string, string>;
    };
    expect(explicit.headers.Authorization).toBe('Bearer explicit');
  });

  it('lets Axios set multipart boundaries and propagates request setup errors', async () => {
    const { fulfilled, rejected } = getRequestInterceptors();
    const config = fulfilled({
      headers: { 'Content-Type': 'application/json' },
      data: new FormData(),
    }) as { headers: Record<string, string> };
    expect(config.headers['Content-Type']).toBeUndefined();
    const error = new Error('request setup failed');
    await expect(rejected(error)).rejects.toBe(error);
  });

  it('rejects 401s without a refresh token after clearing local auth state', async () => {
    const error = {
      config: { url: '/users/me', headers: {} },
      response: { status: 401 },
    };
    await expect(getRejectedResponseInterceptor()(error)).rejects.toBe(error);
    expect(clearSessionMock).toHaveBeenCalledOnce();
    expect(navigateMock).not.toHaveBeenCalled();
  });

  it('does not refresh excluded, retried, non-401, or configless failures', async () => {
    const rejected = getRejectedResponseInterceptor();
    const cases = [
      { response: { status: 401 } },
      { config: { url: '/users/me' }, response: { status: 500 } },
      { config: { url: '/users/me', _retry: true }, response: { status: 401 } },
      { config: { url: '/auth/login' }, response: { status: 401 } },
    ];
    for (const error of cases) {
      await expect(rejected(error)).rejects.toBe(error);
    }
    expect(clearSessionMock).not.toHaveBeenCalled();
  });

  it('refreshes local ADMIN role on 403 without retrying and preserves the original error', async () => {
    authState.role = 'ADMIN';
    refreshCurrentUserMock.mockResolvedValue({ role: 'USER' });
    const adapter = vi.fn();
    const error = {
      config: { url: '/admin/payments', method: 'get', headers: {}, adapter },
      response: { status: 403 },
    };

    await expect(getRejectedResponseInterceptor()(error)).rejects.toBe(error);

    expect(refreshCurrentUserMock).toHaveBeenCalledTimes(1);
    expect(adapter).not.toHaveBeenCalled();
  });

  it('skips centralized 403 role sync for non-admin, /users/me, and auth requests', async () => {
    const rejected = getRejectedResponseInterceptor();
    const errors = [
      { config: { url: '/admin/users' }, response: { status: 403 } },
      { config: { url: '/users/me' }, response: { status: 403 } },
      { config: { url: '/auth/logout' }, response: { status: 403 } },
    ];

    await expect(rejected(errors[0])).rejects.toBe(errors[0]);
    authState.role = 'ADMIN';
    await expect(rejected(errors[1])).rejects.toBe(errors[1]);
    await expect(rejected(errors[2])).rejects.toBe(errors[2]);

    expect(refreshCurrentUserMock).not.toHaveBeenCalled();
  });

  it('coalesces concurrent admin 403 role sync and preserves each original rejection', async () => {
    authState.role = 'ADMIN';
    let resolveRefresh!: () => void;
    refreshCurrentUserMock.mockReturnValue(
      new Promise<void>((resolve) => {
        resolveRefresh = resolve;
      }),
    );
    const rejected = getRejectedResponseInterceptor();
    const firstError = { config: { url: '/admin/users' }, response: { status: 403 } };
    const secondError = { config: { url: '/admin/payments' }, response: { status: 403 } };

    const firstResult = rejected(firstError);
    const secondResult = rejected(secondError);
    await vi.waitFor(() => expect(refreshCurrentUserMock).toHaveBeenCalledTimes(1));
    resolveRefresh();

    await expect(firstResult).rejects.toBe(firstError);
    await expect(secondResult).rejects.toBe(secondError);
  });

  it('preserves the original 403 when current-user resync fails', async () => {
    authState.role = 'ADMIN';
    refreshCurrentUserMock.mockRejectedValue(new Error('sync failed'));
    const error = { config: { url: '/admin/users' }, response: { status: 403 } };

    await expect(getRejectedResponseInterceptor()(error)).rejects.toBe(error);
    expect(refreshCurrentUserMock).toHaveBeenCalledTimes(1);
  });

  it('stores rotated tokens and retries the protected request after refresh', async () => {
    localStorage.setItem('refreshToken', 'old-refresh');
    const postSpy = vi.spyOn(axios, 'post').mockResolvedValue({
      data: { data: { accessToken: 'new-access', refreshToken: 'new-refresh' } },
    });
    const adapter = vi.fn().mockResolvedValue({
      data: { ok: true },
      status: 200,
      statusText: 'OK',
      headers: {},
      config: {},
    });
    const result = await getRejectedResponseInterceptor()({
      config: { url: '/users/me', method: 'get', headers: {}, adapter },
      response: { status: 401 },
    });
    expect(postSpy).toHaveBeenCalledWith('/api/auth/refresh', { refreshToken: 'old-refresh' });
    expect(localStorage.getItem('accessToken')).toBe('new-access');
    expect(localStorage.getItem('refreshToken')).toBe('new-refresh');
    expect(setAuthStateMock).toHaveBeenCalledWith({ accessToken: 'new-access' });
    expect(adapter).toHaveBeenCalledOnce();
    expect(result).toMatchObject({ data: { ok: true } });
  });

  it('fails closed without retrying when the refreshed access token cannot be stored', async () => {
    localStorage.setItem('accessToken', 'expired-access');
    localStorage.setItem('refreshToken', 'old-refresh');
    const postSpy = vi.spyOn(axios, 'post').mockResolvedValue({
      data: { data: { accessToken: 'new-access', refreshToken: 'new-refresh' } },
    });
    vi.spyOn(safeStorage, 'setItem').mockImplementation((key, value) => {
      if (key === 'accessToken') return false;
      localStorage.setItem(key, value);
      return true;
    });
    const adapter = vi.fn();

    await expect(
      getRejectedResponseInterceptor()({
        config: { url: '/users/me', method: 'get', headers: {}, adapter },
        response: { status: 401 },
      }),
    ).rejects.toThrow('Failed to persist refreshed authentication session');

    expect(postSpy).toHaveBeenCalledWith('/api/auth/refresh', { refreshToken: 'old-refresh' });
    expect(adapter).not.toHaveBeenCalled();
    expect(localStorage.getItem('accessToken')).toBeNull();
    expect(localStorage.getItem('refreshToken')).toBeNull();
    expect(setAuthStateMock).not.toHaveBeenCalled();
    expect(clearSessionMock).toHaveBeenCalledOnce();
    expect(showToastMock).toHaveBeenCalledOnce();
    expect(navigateMock).toHaveBeenCalledWith('/login');
  });

  it('fails closed without retrying when the rotated refresh token cannot be stored', async () => {
    localStorage.setItem('accessToken', 'expired-access');
    localStorage.setItem('refreshToken', 'old-refresh');
    const postSpy = vi.spyOn(axios, 'post').mockResolvedValue({
      data: { data: { accessToken: 'new-access', refreshToken: 'new-refresh' } },
    });
    vi.spyOn(safeStorage, 'setItem').mockImplementation((key, value) => {
      if (key === 'refreshToken' && value === 'new-refresh') return false;
      localStorage.setItem(key, value);
      return true;
    });
    const adapter = vi.fn();

    await expect(
      getRejectedResponseInterceptor()({
        config: { url: '/users/me', method: 'get', headers: {}, adapter },
        response: { status: 401 },
      }),
    ).rejects.toThrow('Failed to persist refreshed authentication session');

    expect(postSpy).toHaveBeenCalledWith('/api/auth/refresh', { refreshToken: 'old-refresh' });
    expect(adapter).not.toHaveBeenCalled();
    expect(localStorage.getItem('accessToken')).toBeNull();
    expect(localStorage.getItem('refreshToken')).toBeNull();
    expect(setAuthStateMock).not.toHaveBeenCalled();
    expect(clearSessionMock).toHaveBeenCalledOnce();
    expect(showToastMock).toHaveBeenCalledOnce();
    expect(navigateMock).toHaveBeenCalledWith('/login');
  });

  it('clears the session and redirects when refresh fails on a protected request', async () => {
    const refreshError = new Error('refresh failed');
    const postSpy = vi.spyOn(axios, 'post').mockRejectedValue(refreshError);
    localStorage.setItem('accessToken', 'expired-access-token');
    localStorage.setItem('refreshToken', 'refresh-token');
    localStorage.setItem('user', '{"id":1,"role":"USER"}');

    await expect(
      getRejectedResponseInterceptor()({
        config: {
          url: '/users/me',
          headers: { Authorization: 'Bearer expired-access-token' },
        },
        response: { status: 401 },
      }),
    ).rejects.toBe(refreshError);

    expect(postSpy).toHaveBeenCalledWith('/api/auth/refresh', {
      refreshToken: 'refresh-token',
    });
    expect(localStorage.getItem('accessToken')).toBeNull();
    expect(localStorage.getItem('refreshToken')).toBeNull();
    expect(clearSessionMock).toHaveBeenCalledTimes(1);
    expect(showToastMock).toHaveBeenCalledTimes(1);
    expect(navigateMock).toHaveBeenCalledWith('/login');
  });
});

describe('client response helpers', () => {
  it('recognizes subscription-required JSON errors but not blobs or unrelated codes', () => {
    expect(
      isSubscriptionRequired({ response: { data: { errorCode: 'NO_ACTIVE_SUBSCRIPTION' } } }),
    ).toBe(true);
    expect(isSubscriptionRequired({ response: { data: { errorCode: 'FORBIDDEN' } } })).toBe(false);
    expect(isSubscriptionRequired({ response: { data: new Blob(['error']) } })).toBe(false);
    expect(isSubscriptionRequired(null)).toBe(false);
  });

  it('extracts error codes from JSON and blob responses', async () => {
    await expect(
      getApiErrorCode({ response: { data: { errorCode: 'NO_ACTIVE_SUBSCRIPTION' } } }),
    ).resolves.toBe('NO_ACTIVE_SUBSCRIPTION');
    await expect(
      getApiErrorCode({
        response: { data: new Blob([JSON.stringify({ errorCode: 'DOWNLOAD_LIMIT_EXCEEDED' })]) },
      }),
    ).resolves.toBe('DOWNLOAD_LIMIT_EXCEEDED');
    await expect(
      getApiErrorCode({ response: { data: new Blob(['not-json']) } }),
    ).resolves.toBeNull();
    await expect(
      getApiErrorCode({ response: { data: { message: 'no code' } } }),
    ).resolves.toBeNull();
    await expect(getApiErrorCode(new Error('offline'))).resolves.toBeNull();
  });

  it('normalizes only relative upload paths', () => {
    expect(toUploadUrl(null)).toBeNull();
    expect(toUploadUrl('')).toBeNull();
    expect(toUploadUrl('/uploads/cover.png')).toBe('/uploads/cover.png');
    expect(toUploadUrl('https://cdn.example.com/cover.png')).toBe(
      'https://cdn.example.com/cover.png',
    );
    expect(toUploadUrl('blob:preview')).toBe('blob:preview');
    expect(toUploadUrl('albums/cover.png')).toBe('/uploads/albums/cover.png');
  });
});

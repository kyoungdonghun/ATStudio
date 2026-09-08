import axios, { type AxiosResponse, type InternalAxiosRequestConfig } from 'axios';
import { beforeEach, describe, expect, it, vi } from 'vitest';

const { clearSessionMock, navigateMock, refreshCurrentUserMock, showToastMock } = vi.hoisted(
  () => ({
    clearSessionMock: vi.fn(),
    navigateMock: vi.fn(),
    refreshCurrentUserMock: vi.fn(),
    showToastMock: vi.fn(),
  }),
);

vi.mock('@/router', () => ({
  router: { navigate: navigateMock },
}));

vi.mock('@/store/toastStore', () => ({
  useToastStore: {
    getState: () => ({ show: showToastMock }),
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
import { getAuthSessionGeneration, useAuthStore } from '@/store/authStore';
import type { User } from '@/types';
import * as authApi from '@/api/auth';

const clearSession = useAuthStore.getState().clearSession;
const refreshCurrentUser = useAuthStore.getState().refreshCurrentUser;

function getRequestInterceptors() {
  const requestInterceptors = client.interceptors.request as unknown as {
    handlers: Array<{
      fulfilled?: (config: Record<string, unknown>) => Record<string, unknown>;
      rejected?: (error: unknown) => never;
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
  // Unit fixtures simulate dispatch before delivering a synthetic response.
  return (error: unknown) => {
    const config = (error as { config?: Record<string, unknown> }).config;
    if (config) getRequestInterceptors().fulfilled(config);
    return rejected(error);
  };
}

describe('client auth refresh exclusions', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
    clearSession();
    clearSessionMock.mockReset().mockImplementation(clearSession);
    navigateMock.mockReset();
    refreshCurrentUserMock.mockReset();
    showToastMock.mockReset();
    useAuthStore.setState({
      role: 'USER',
      clearSession: clearSessionMock,
      refreshCurrentUser: refreshCurrentUserMock,
    });
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

  it('lets Axios set multipart boundaries and propagates request setup errors', () => {
    const { fulfilled, rejected } = getRequestInterceptors();
    const config = fulfilled({
      headers: { 'Content-Type': 'application/json' },
      data: new FormData(),
    }) as { headers: Record<string, string> };
    expect(config.headers['Content-Type']).toBeUndefined();
    const error = new Error('request setup failed');
    expect(() => rejected(error)).toThrow(error);
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

  it('rejects an auth-replay opt-out 401 without refresh, queueing, or request replay', async () => {
    localStorage.setItem('refreshToken', 'refresh-token');
    const postSpy = vi.spyOn(axios, 'post');
    const adapter = vi.fn();
    const error = {
      config: {
        url: '/admin/payments/refunds/51/execute',
        method: 'post',
        headers: {},
        adapter,
        skipAuthReplay: true,
      },
      response: { status: 401 },
    };

    await expect(getRejectedResponseInterceptor()(error)).rejects.toBe(error);

    expect(postSpy).not.toHaveBeenCalled();
    expect(adapter).not.toHaveBeenCalled();
    expect(clearSessionMock).not.toHaveBeenCalled();
  });

  it('keeps an opted-out execute outside an in-progress protected refresh', async () => {
    localStorage.setItem('refreshToken', 'old-refresh');
    const refreshResponse = {
      data: { data: { accessToken: 'new-access', refreshToken: 'new-refresh' } },
    };
    let resolveRefresh!: (value: typeof refreshResponse) => void;
    const refreshPromise = new Promise<typeof refreshResponse>((resolve) => {
      resolveRefresh = resolve;
    });
    const postSpy = vi.spyOn(axios, 'post').mockReturnValueOnce(refreshPromise);
    const normalAdapter = vi.fn().mockResolvedValue({
      data: { ok: true },
      status: 200,
      statusText: 'OK',
      headers: {},
      config: {},
    });
    const executeAdapter = vi.fn();
    const rejected = getRejectedResponseInterceptor();

    const normalResult = rejected({
      config: { url: '/users/me', method: 'get', headers: {}, adapter: normalAdapter },
      response: { status: 401 },
    });
    await vi.waitFor(() => expect(postSpy).toHaveBeenCalledTimes(1));

    const executeError = {
      config: {
        url: '/admin/payments/refunds/51/execute',
        method: 'post',
        headers: {},
        adapter: executeAdapter,
        skipAuthReplay: true,
      },
      response: { status: 401 },
    };
    await expect(rejected(executeError)).rejects.toBe(executeError);
    expect(postSpy).toHaveBeenCalledTimes(1);
    expect(executeAdapter).not.toHaveBeenCalled();
    expect(clearSessionMock).not.toHaveBeenCalled();

    resolveRefresh(refreshResponse);
    await expect(normalResult).resolves.toMatchObject({ data: { ok: true } });
    expect(normalAdapter).toHaveBeenCalledTimes(1);
    expect(localStorage.getItem('accessToken')).toBe('new-access');
    expect(localStorage.getItem('refreshToken')).toBe('new-refresh');
    expect(clearSessionMock).not.toHaveBeenCalled();
  });

  it('refreshes local ADMIN role on 403 without retrying and preserves the original error', async () => {
    useAuthStore.setState({ role: 'ADMIN' });
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

  it('honors the admin-role sync opt-out only for centralized 403 synchronization', async () => {
    useAuthStore.setState({ role: 'ADMIN' });
    const adapter = vi.fn();
    const error = {
      config: {
        url: '/users/2',
        method: 'put',
        headers: {},
        adapter,
        skipAdminRoleSync: true,
      },
      response: { status: 403 },
    };

    await expect(getRejectedResponseInterceptor()(error)).rejects.toBe(error);

    expect(refreshCurrentUserMock).not.toHaveBeenCalled();
    expect(adapter).not.toHaveBeenCalled();
  });

  it('still refreshes and replays a 401 when only admin-role synchronization opts out', async () => {
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

    await expect(
      getRejectedResponseInterceptor()({
        config: {
          url: '/users/2',
          method: 'put',
          headers: {},
          adapter,
          skipAdminRoleSync: true,
        },
        response: { status: 401 },
      }),
    ).resolves.toMatchObject({ data: { ok: true } });

    expect(postSpy).toHaveBeenCalledTimes(1);
    expect(adapter).toHaveBeenCalledTimes(1);
    expect(clearSessionMock).not.toHaveBeenCalled();
  });

  it('skips centralized 403 role sync for non-admin, /users/me, and auth requests', async () => {
    const rejected = getRejectedResponseInterceptor();
    const errors = [
      { config: { url: '/admin/users' }, response: { status: 403 } },
      { config: { url: '/users/me' }, response: { status: 403 } },
      { config: { url: '/auth/logout' }, response: { status: 403 } },
    ];

    await expect(rejected(errors[0])).rejects.toBe(errors[0]);
    useAuthStore.setState({ role: 'ADMIN' });
    await expect(rejected(errors[1])).rejects.toBe(errors[1]);
    await expect(rejected(errors[2])).rejects.toBe(errors[2]);

    expect(refreshCurrentUserMock).not.toHaveBeenCalled();
  });

  it('coalesces concurrent admin 403 role sync and preserves each original rejection', async () => {
    useAuthStore.setState({ role: 'ADMIN' });
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
    useAuthStore.setState({ role: 'ADMIN' });
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
    expect(useAuthStore.getState().accessToken).toBe('new-access');
    expect(adapter).toHaveBeenCalledOnce();
    expect(result).toMatchObject({ data: { ok: true } });
  });

  it('marks concurrent protected requests before one refresh replays each request once', async () => {
    localStorage.setItem('refreshToken', 'old-refresh');
    const refreshResponse = {
      data: { data: { accessToken: 'new-access', refreshToken: 'new-refresh' } },
    };
    let resolveRefresh!: (value: typeof refreshResponse) => void;
    const postSpy = vi.spyOn(axios, 'post').mockReturnValueOnce(
      new Promise<typeof refreshResponse>((resolve) => {
        resolveRefresh = resolve;
      }),
    );
    const createAdapter = (request: string) =>
      vi.fn().mockResolvedValue({
        data: { request },
        status: 200,
        statusText: 'OK',
        headers: {},
        config: {},
      });
    const leaderAdapter = createAdapter('leader');
    const firstQueuedAdapter = createAdapter('first-queued');
    const secondQueuedAdapter = createAdapter('second-queued');
    const leaderConfig: Record<string, unknown> & { _retry?: boolean } = {
      url: '/users/me',
      method: 'get',
      headers: {},
      adapter: leaderAdapter,
    };
    const firstQueuedConfig: Record<string, unknown> & { _retry?: boolean } = {
      url: '/licenses',
      method: 'get',
      headers: {},
      adapter: firstQueuedAdapter,
    };
    const secondQueuedConfig: Record<string, unknown> & { _retry?: boolean } = {
      url: '/downloads/history',
      method: 'get',
      headers: {},
      adapter: secondQueuedAdapter,
    };
    const rejected = getRejectedResponseInterceptor();

    const leaderResult = rejected({ config: leaderConfig, response: { status: 401 } });
    await vi.waitFor(() => expect(postSpy).toHaveBeenCalledTimes(1));
    const leaderMarkedBeforeRefreshCompleted = leaderConfig._retry;

    const firstQueuedResult = rejected({
      config: firstQueuedConfig,
      response: { status: 401 },
    });
    const secondQueuedResult = rejected({
      config: secondQueuedConfig,
      response: { status: 401 },
    });
    const queuedMarkersBeforeReplay = [firstQueuedConfig._retry, secondQueuedConfig._retry];
    expect(leaderAdapter).not.toHaveBeenCalled();
    expect(firstQueuedAdapter).not.toHaveBeenCalled();
    expect(secondQueuedAdapter).not.toHaveBeenCalled();

    resolveRefresh(refreshResponse);
    await expect(
      Promise.all([leaderResult, firstQueuedResult, secondQueuedResult]),
    ).resolves.toHaveLength(3);

    expect(leaderMarkedBeforeRefreshCompleted).toBe(true);
    expect(queuedMarkersBeforeReplay).toEqual([true, true]);
    expect(postSpy).toHaveBeenCalledTimes(1);
    expect(leaderAdapter).toHaveBeenCalledTimes(1);
    expect(firstQueuedAdapter).toHaveBeenCalledTimes(1);
    expect(secondQueuedAdapter).toHaveBeenCalledTimes(1);
    expect(leaderAdapter.mock.calls[0]?.[0]).toMatchObject({ _retry: true });
    expect(firstQueuedAdapter.mock.calls[0]?.[0]).toMatchObject({ _retry: true });
    expect(secondQueuedAdapter.mock.calls[0]?.[0]).toMatchObject({ _retry: true });
  });

  it('rejects a queued replay second 401 without another refresh or replay', async () => {
    localStorage.setItem('refreshToken', 'old-refresh');
    const refreshResponse = {
      data: { data: { accessToken: 'new-access', refreshToken: 'new-refresh' } },
    };
    let resolveRefresh!: (value: typeof refreshResponse) => void;
    const unexpectedRefreshError = new Error('unexpected second refresh');
    const postSpy = vi
      .spyOn(axios, 'post')
      .mockReturnValueOnce(
        new Promise<typeof refreshResponse>((resolve) => {
          resolveRefresh = resolve;
        }),
      )
      .mockRejectedValue(unexpectedRefreshError);
    const leaderAdapter = vi.fn().mockResolvedValue({
      data: { ok: true },
      status: 200,
      statusText: 'OK',
      headers: {},
      config: {},
    });
    const queuedReplayErrors: unknown[] = [];
    const queuedAdapter = vi.fn().mockImplementation((config) => {
      const replayError = { config, response: { status: 401 } };
      queuedReplayErrors.push(replayError);
      return Promise.reject(replayError);
    });
    const rejected = getRejectedResponseInterceptor();

    const leaderResult = rejected({
      config: { url: '/users/me', method: 'get', headers: {}, adapter: leaderAdapter },
      response: { status: 401 },
    });
    await vi.waitFor(() => expect(postSpy).toHaveBeenCalledTimes(1));
    const queuedResult = rejected({
      config: { url: '/licenses', method: 'get', headers: {}, adapter: queuedAdapter },
      response: { status: 401 },
    });
    const outcomes = Promise.allSettled([leaderResult, queuedResult]);

    resolveRefresh(refreshResponse);
    await vi.waitFor(() => expect(queuedAdapter).toHaveBeenCalledTimes(1));
    const second401 = queuedReplayErrors[0];
    const [leaderOutcome, queuedOutcome] = await outcomes;

    expect(leaderOutcome).toMatchObject({ status: 'fulfilled' });
    expect(queuedOutcome).toEqual({ status: 'rejected', reason: second401 });
    expect(postSpy).toHaveBeenCalledTimes(1);
    expect(leaderAdapter).toHaveBeenCalledTimes(1);
    expect(queuedAdapter).toHaveBeenCalledTimes(1);
    expect(clearSessionMock).not.toHaveBeenCalled();
    expect(navigateMock).not.toHaveBeenCalled();
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
    expect(useAuthStore.getState().accessToken).toBeNull();
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
    expect(useAuthStore.getState().accessToken).toBeNull();
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

function deferred<T>() {
  let resolve!: (value: T) => void;
  let reject!: (reason: unknown) => void;
  const promise = new Promise<T>((resolvePromise, rejectPromise) => {
    resolve = resolvePromise;
    reject = rejectPromise;
  });
  return { promise, resolve, reject };
}

const sessionUser: User = {
  id: 1,
  email: 'session@example.test',
  nickname: 'session-user',
  role: 'USER',
  phonePersonal: null,
  phoneCompany: null,
  job: null,
  companyName: null,
  userType: 'INDIVIDUAL',
  isVerified: true,
  createdAt: '2026-09-09T00:00:00Z',
};

function refreshResponse(accessToken = 'rotated-access', refreshToken = 'rotated-refresh') {
  return { data: { data: { accessToken, refreshToken } } };
}

function protectedWrite(url = '/likes/10') {
  const dispatched = deferred<InternalAxiosRequestConfig>();
  const response = deferred<AxiosResponse>();
  const adapter = vi
    .fn<(config: InternalAxiosRequestConfig) => Promise<AxiosResponse>>()
    .mockImplementationOnce((config) => {
      dispatched.resolve(config);
      return response.promise;
    })
    .mockImplementation(async (config) => ({
      data: { ok: true },
      status: 200,
      statusText: 'OK',
      headers: {},
      config,
    }));
  const result = client.post(url, { fixture: true }, { adapter });
  const outcome = Promise.allSettled([result]);
  return {
    adapter,
    outcome,
    result,
    dispatched: dispatched.promise,
    reject401: async () => {
      const config = await dispatched.promise;
      const error = { config, response: { status: 401 } };
      response.reject(error);
      return error;
    },
  };
}

describe('client session ownership (SEC-04)', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
    useAuthStore.setState({ clearSession, refreshCurrentUser });
    clearSession();
    localStorage.clear();
    sessionStorage.clear();
    navigateMock.mockReset();
    showToastMock.mockReset();
    useAuthStore.getState().login('session-access', sessionUser, 'session-refresh');
  });

  it.each([
    ['logout', 'success'],
    ['logout', 'failure'],
    ['replacement', 'success'],
    ['replacement', 'failure'],
    ['same-user', 'success'],
    ['same-user', 'failure'],
  ])('rejects leader and queue after %s followed by stale refresh %s', async (change, result) => {
    const refresh = deferred<ReturnType<typeof refreshResponse>>();
    const postSpy = vi
      .spyOn(axios, 'post')
      .mockRejectedValue(new Error('unexpected refresh'))
      .mockReturnValueOnce(refresh.promise);
    const leader = protectedWrite();
    await leader.reject401();
    await vi.waitFor(() => expect(postSpy).toHaveBeenCalledTimes(1));
    const queued = protectedWrite('/likes/11');
    const queuedError = await queued.reject401();
    await vi.waitFor(() => expect(queuedError.config).toMatchObject({ _retry: true }));

    const oldGeneration = getAuthSessionGeneration();
    if (change === 'logout') {
      useAuthStore.getState().clearSession();
    } else {
      // Identical tokens and, for same-user, identical identity still start a new session.
      useAuthStore
        .getState()
        .login(
          'session-access',
          change === 'same-user' ? sessionUser : { ...sessionUser, id: 2 },
          'session-refresh',
        );
    }
    expect(getAuthSessionGeneration()).not.toBe(oldGeneration);
    const expectedState = useAuthStore.getState();
    const expectedStorage = { ...localStorage };

    if (result === 'success') refresh.resolve(refreshResponse());
    else refresh.reject(new Error('old refresh failed'));

    expect(await leader.outcome).toMatchObject([{ status: 'rejected' }]);
    expect(await queued.outcome).toMatchObject([{ status: 'rejected' }]);
    expect(leader.adapter).toHaveBeenCalledTimes(1);
    expect(queued.adapter).toHaveBeenCalledTimes(1);
    expect(postSpy).toHaveBeenCalledTimes(1);
    expect(useAuthStore.getState()).toBe(expectedState);
    expect({ ...localStorage }).toEqual(expectedStorage);
    expect(showToastMock).not.toHaveBeenCalled();
    expect(navigateMock).not.toHaveBeenCalled();
  });

  it.each([false, true])(
    'rejects a late A 401 before reading replacement refresh credentials (same user: %s)',
    async (sameUser) => {
      const postSpy = vi.spyOn(axios, 'post').mockRejectedValue(new Error('unexpected refresh'));
      const oldRequest = protectedWrite();
      await oldRequest.dispatched;
      useAuthStore
        .getState()
        .login(
          'session-access',
          sameUser ? sessionUser : { ...sessionUser, id: 2 },
          'session-refresh',
        );
      const error = await oldRequest.reject401();

      expect(await oldRequest.outcome).toEqual([{ status: 'rejected', reason: error }]);
      expect(postSpy).not.toHaveBeenCalled();
      expect(oldRequest.adapter).toHaveBeenCalledTimes(1);
      expect(useAuthStore.getState().accessToken).toBe('session-access');
      expect(navigateMock).not.toHaveBeenCalled();
    },
  );

  it('does not clear a replacement session without refresh credentials on a late A 401', async () => {
    const postSpy = vi.spyOn(axios, 'post').mockRejectedValue(new Error('unexpected refresh'));
    const oldRequest = protectedWrite();
    await oldRequest.dispatched;
    useAuthStore.getState().login('replacement-access', { ...sessionUser, id: 2 }, null);
    const error = await oldRequest.reject401();

    expect(await oldRequest.outcome).toEqual([{ status: 'rejected', reason: error }]);
    expect(postSpy).not.toHaveBeenCalled();
    expect(useAuthStore.getState().accessToken).toBe('replacement-access');
  });

  it.each(['success', 'failure'])('keeps the B flight after A %s', async (result) => {
    const oldRefresh = deferred<ReturnType<typeof refreshResponse>>();
    const newRefresh = deferred<ReturnType<typeof refreshResponse>>();
    const postSpy = vi
      .spyOn(axios, 'post')
      .mockRejectedValue(new Error('unexpected refresh'))
      .mockReturnValueOnce(oldRefresh.promise)
      .mockReturnValueOnce(newRefresh.promise);
    const oldLeader = protectedWrite();
    const lateOldRequest = protectedWrite('/likes/11');
    await oldLeader.reject401();
    await vi.waitFor(() => expect(postSpy).toHaveBeenCalledTimes(1));
    useAuthStore.getState().login('new-access', sessionUser, 'new-refresh');
    const newLeader = protectedWrite('/likes/12');
    await newLeader.reject401();
    await vi.waitFor(() => expect(postSpy).toHaveBeenCalledTimes(2));

    const lateError = await lateOldRequest.reject401();
    expect(await lateOldRequest.outcome).toEqual([{ status: 'rejected', reason: lateError }]);
    if (result === 'success') oldRefresh.resolve(refreshResponse('stale-access', 'stale-refresh'));
    else oldRefresh.reject(new Error('old flight failed'));
    expect(await oldLeader.outcome).toMatchObject([{ status: 'rejected' }]);

    const newQueued = protectedWrite('/likes/13');
    const queuedError = await newQueued.reject401();
    await vi.waitFor(() => expect(queuedError.config).toMatchObject({ _retry: true }));
    expect(postSpy).toHaveBeenCalledTimes(2);
    expect(newLeader.adapter).toHaveBeenCalledTimes(1);
    expect(newQueued.adapter).toHaveBeenCalledTimes(1);
    newRefresh.resolve(refreshResponse());

    await expect(Promise.all([newLeader.result, newQueued.result])).resolves.toHaveLength(2);
    expect(newLeader.adapter).toHaveBeenCalledTimes(2);
    expect(newQueued.adapter).toHaveBeenCalledTimes(2);
    expect(newQueued.adapter.mock.calls[1]?.[0].headers.Authorization).toBe(
      'Bearer rotated-access',
    );
    expect(lateOldRequest.adapter).toHaveBeenCalledTimes(1);
    expect(useAuthStore.getState().accessToken).toBe('rotated-access');
    expect(localStorage.getItem('refreshToken')).toBe('rotated-refresh');
    expect(showToastMock).not.toHaveBeenCalled();
    expect(navigateMock).not.toHaveBeenCalled();
  });

  it('rejects resolved waiters if the session changes before their replay continuation', async () => {
    const refresh = deferred<ReturnType<typeof refreshResponse>>();
    const postSpy = vi
      .spyOn(axios, 'post')
      .mockRejectedValue(new Error('unexpected refresh'))
      .mockReturnValueOnce(refresh.promise);
    const leader = protectedWrite();
    await leader.reject401();
    await vi.waitFor(() => expect(postSpy).toHaveBeenCalledTimes(1));
    const queued = protectedWrite('/likes/11');
    const queuedError = await queued.reject401();
    await vi.waitFor(() => expect(queuedError.config).toMatchObject({ _retry: true }));
    const unsubscribe = useAuthStore.subscribe((state) => {
      if (state.accessToken === 'rotated-access') {
        // Run after persistence succeeds but before fulfilled refresh waiters resume.
        queueMicrotask(() => {
          useAuthStore.getState().login('new-access', { ...sessionUser, id: 2 }, 'new-refresh');
        });
      }
    });
    try {
      refresh.resolve(refreshResponse());
      expect(await leader.outcome).toMatchObject([{ status: 'rejected' }]);
      expect(await queued.outcome).toMatchObject([{ status: 'rejected' }]);
    } finally {
      unsubscribe();
    }
    expect(leader.adapter).toHaveBeenCalledTimes(1);
    expect(queued.adapter).toHaveBeenCalledTimes(1);
    expect(useAuthStore.getState().accessToken).toBe('new-access');
    expect(showToastMock).not.toHaveBeenCalled();
  });

  it('checks ownership again in the request interceptor for a cloned replay config', async () => {
    const postSpy = vi.spyOn(axios, 'post').mockRejectedValue(new Error('unexpected refresh'));
    const oldRequest = protectedWrite();
    const config = await oldRequest.dispatched;
    useAuthStore.getState().login('new-access', sessionUser, 'new-refresh');
    const error = await oldRequest.reject401();
    expect(await oldRequest.outcome).toEqual([{ status: 'rejected', reason: error }]);

    await expect(client({ ...config, _retry: true } as typeof config)).rejects.toThrow(
      'Stale authentication session',
    );
    expect(oldRequest.adapter).toHaveBeenCalledTimes(1);
    expect(postSpy).not.toHaveBeenCalled();
  });

  it('rejects an old refresh during pending server logout without restoring or replaying', async () => {
    const refresh = deferred<ReturnType<typeof refreshResponse>>();
    const postSpy = vi
      .spyOn(axios, 'post')
      .mockRejectedValue(new Error('unexpected refresh'))
      .mockReturnValueOnce(refresh.promise);
    const logoutResponse = deferred<'confirmed'>();
    vi.spyOn(authApi, 'logoutSession').mockReturnValueOnce(logoutResponse.promise);
    const oldRequest = protectedWrite();
    await oldRequest.reject401();
    await vi.waitFor(() => expect(postSpy).toHaveBeenCalledTimes(1));

    const logout = useAuthStore.getState().logout();
    await vi.waitFor(() => expect(authApi.logoutSession).toHaveBeenCalledTimes(1));
    refresh.resolve(refreshResponse());
    expect(await oldRequest.outcome).toMatchObject([{ status: 'rejected' }]);
    expect(oldRequest.adapter).toHaveBeenCalledTimes(1);
    expect(localStorage.getItem('accessToken')).toBe('session-access');
    expect(localStorage.getItem('refreshToken')).toBe('session-refresh');
    expect(showToastMock).not.toHaveBeenCalled();

    const duringLogout = protectedWrite('/likes/11');
    const error = await duringLogout.reject401();
    expect(await duringLogout.outcome).toEqual([{ status: 'rejected', reason: error }]);
    expect(postSpy).toHaveBeenCalledTimes(1);
    logoutResponse.resolve('confirmed');
    await expect(logout).resolves.toEqual({ serverConfirmed: true });
    expect(useAuthStore.getState().accessToken).toBeNull();
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

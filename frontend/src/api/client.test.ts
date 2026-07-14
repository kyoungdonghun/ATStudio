import axios from 'axios';
import { beforeEach, describe, expect, it, vi } from 'vitest';

const { clearSessionMock, navigateMock, showToastMock } = vi.hoisted(() => ({
  clearSessionMock: vi.fn(),
  navigateMock: vi.fn(),
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
    getState: () => ({ clearSession: clearSessionMock }),
  },
}));

import client, { shouldSkipRefresh } from '@/api/client';

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
    clearSessionMock.mockReset();
    navigateMock.mockReset();
    showToastMock.mockReset();
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

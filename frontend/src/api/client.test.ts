import { describe, expect, it, vi } from 'vitest';

vi.mock('@/router', () => ({
  router: { navigate: vi.fn() },
}));

vi.mock('@/store/toastStore', () => ({
  useToastStore: {
    getState: () => ({ show: vi.fn() }),
  },
}));

vi.mock('@/store/authStore', () => ({
  useAuthStore: {
    getState: () => ({ logout: vi.fn() }),
  },
}));

import { shouldSkipRefresh } from '@/api/client';

describe('client auth refresh exclusions', () => {
  it('skips refresh logic for login and refresh endpoints', () => {
    expect(shouldSkipRefresh({ url: '/auth/login' })).toBe(true);
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
});

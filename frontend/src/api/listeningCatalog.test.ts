import axios, { AxiosError, type InternalAxiosRequestConfig } from 'axios';
import { expect, it, vi } from 'vitest';
vi.mock('@/router', () => ({ router: { navigate: vi.fn() } }));
import client from '@/api/client';
import { changeListeningCatalog } from '@/api/tracks';
import { useAuthStore } from '@/store/authStore';

it('catalog PUT 401 cannot enter automatic auth refresh or request replay', async () => {
  localStorage.setItem('accessToken', 'fixture-stale');
  localStorage.setItem('refreshToken', 'fixture-refresh');
  useAuthStore.setState({ accessToken: 'fixture-stale' });
  const original = client.defaults.adapter;
  const refresh = vi.spyOn(axios, 'post');
  const adapter = vi.fn(async (config: InternalAxiosRequestConfig) => {
    throw new AxiosError('unauthorized', 'ERR_BAD_REQUEST', config, undefined, {
      data: {},
      status: 401,
      statusText: 'Unauthorized',
      headers: {},
      config,
    });
  });
  client.defaults.adapter = adapter;
  try {
    await expect(
      changeListeningCatalog({
        attemptId: '00000000-0000-4000-8000-000000000001',
        expectedRevision: 1,
        trackIds: [11],
        reason: 'fixture',
        creditsConfirmed: true,
      }),
    ).rejects.toBeInstanceOf(AxiosError);
    expect(adapter).toHaveBeenCalledTimes(1);
    expect(adapter.mock.calls[0][0].skipAuthReplay).toBe(true);
    expect(refresh).not.toHaveBeenCalled();
  } finally {
    client.defaults.adapter = original;
    refresh.mockRestore();
    useAuthStore.getState().clearSession();
  }
});

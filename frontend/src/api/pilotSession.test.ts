import axios, { AxiosError, type InternalAxiosRequestConfig } from 'axios';
import { afterEach, expect, it, vi } from 'vitest';
import client from './client';
import { fetchMe } from './auth';
import { getAuthSessionGeneration, useAuthStore } from '@/store/authStore';
import {
  readPilotSession,
  removePilotSession,
  savePilotSession,
  startPilotSession,
} from '@/store/pilotSession';
import type { User } from '@/types';

vi.mock('@/config/pilotMode', () => ({ isPilotMode: true }));
vi.mock('@/router', () => ({ router: { navigate: vi.fn() } }));

const first = { id: 11, role: 'USER' } as User;
const second = { id: 22, role: 'USER' } as User;
const originalAdapter = client.defaults.adapter;
afterEach(() => {
  client.defaults.adapter = originalAdapter;
  useAuthStore.getState().clearSession();
  vi.restoreAllMocks();
});

function expired(config: InternalAxiosRequestConfig) {
  return new AxiosError('Expired', undefined, config, undefined, {
    config,
    status: 401,
    statusText: 'Unauthorized',
    headers: {},
    data: {},
  });
}

it('does not dispatch a private action with the new tab owner before receiving a storage event', async () => {
  useAuthStore.getState().login('A', first, 'R-A');
  startPilotSession(second, 'B', 'R-B');
  const adapter = vi.fn(async (config: InternalAxiosRequestConfig) => ({
    config,
    status: 200,
    statusText: 'OK',
    headers: {},
    data: {},
  }));
  client.defaults.adapter = adapter;
  await expect(client.get('/tracks/1/download')).rejects.toThrow('Stale');
  expect(adapter).not.toHaveBeenCalled();
  expect(useAuthStore.getState().user?.id).toBe(22);
  await client.get('/licenses/me');
  expect(adapter.mock.calls[0][0].headers.get('Authorization')).toBe('Bearer B');
});

it('does not overwrite the later owner with a late token refresh or replay a download', async () => {
  useAuthStore.getState().login('A', first, 'R-A');
  const adapter = vi.fn(async (config: InternalAxiosRequestConfig) => {
    throw expired(config);
  });
  client.defaults.adapter = adapter;
  let finish!: (value: unknown) => void;
  const refresh = vi.spyOn(axios, 'post').mockImplementation(
    () =>
      new Promise((resolve) => {
        finish = resolve;
      }),
  );
  const result = expect(client.get('/tracks/2/download')).rejects.toThrow('Stale');
  await vi.waitFor(() => expect(refresh).toHaveBeenCalledOnce());
  const replacement = startPilotSession(second, 'B', 'R-B');
  finish({ data: { data: { accessToken: 'A-new', refreshToken: 'R-A-new' } } });
  await result;
  expect(adapter).toHaveBeenCalledTimes(1);
  expect(readPilotSession()).toEqual(replacement);
});

it('rejects a stale logout without revoking or clearing the replacing session', async () => {
  useAuthStore.getState().login('A', first, 'R-A');
  let finish!: () => void;
  const pending = new Promise<void>((resolve) => {
    finish = resolve;
  });
  const calls: unknown[] = [];
  client.defaults.adapter = async (config) => {
    calls.push(config.headers.get('Authorization'));
    await pending;
    return { config, status: 204, statusText: 'No Content', headers: {}, data: '' };
  };
  const outcome = useAuthStore.getState().logout();
  await vi.waitFor(() => expect(calls).toEqual(['Bearer A']));
  const replacement = startPilotSession(second, 'B', 'R-B');
  finish();
  await outcome;
  expect(readPilotSession()).toEqual(replacement);
  expect(useAuthStore.getState().user?.id).toBe(22);
});

it('rotates and retries for the same participant without changing the login generation', async () => {
  useAuthStore.getState().login('A', first, 'R-A');
  const generation = getAuthSessionGeneration();
  const calls: unknown[] = [];
  client.defaults.adapter = async (config) => {
    const token = config.headers.get('Authorization');
    calls.push(token);
    if (token === 'Bearer A') throw expired(config);
    return { config, status: 200, statusText: 'OK', headers: {}, data: {} };
  };
  vi.spyOn(axios, 'post').mockResolvedValue({
    data: { data: { accessToken: 'A-new', refreshToken: 'R-A-new' } },
  });
  await client.get('/licenses/me');
  expect(calls).toEqual(['Bearer A', 'Bearer A-new']);
  expect(readPilotSession()).toMatchObject({
    accessToken: 'A-new',
    refreshToken: 'R-A-new',
    user: { id: 11 },
  });
  expect(getAuthSessionGeneration()).toBe(generation);
});

it('never refreshes a login bootstrap token using another stored account', async () => {
  useAuthStore.getState().login('A', first, 'R-A');
  const refresh = vi.spyOn(axios, 'post');
  client.defaults.adapter = async (config) => {
    expect(config.headers.get('Authorization')).toBe('Bearer B');
    throw expired(config);
  };
  await expect(fetchMe('B')).rejects.toThrow('Expired');
  expect(refresh).not.toHaveBeenCalled();
  expect(readPilotSession()?.user.id).toBe(11);
});

it('removes the old account on remote logout and cannot revive it via delayed writes', () => {
  useAuthStore.getState().login('A', first, 'R-A');
  const session = readPilotSession()!;
  removePilotSession(session.id);
  savePilotSession({ ...session, accessToken: 'A-late' });
  window.dispatchEvent(new StorageEvent('storage', { key: null }));
  expect(readPilotSession()).toBeNull();
  expect(useAuthStore.getState().role).toBe('GUEST');
});

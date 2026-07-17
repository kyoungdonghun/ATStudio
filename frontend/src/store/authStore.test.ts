import { beforeEach, describe, expect, it, vi } from 'vitest';

const { logoutSessionMock } = vi.hoisted(() => ({
  logoutSessionMock: vi.fn(),
}));

vi.mock('@/api/auth', () => ({
  logoutSession: logoutSessionMock,
}));

import { useAlbumLikeStore } from '@/store/albumLikeStore';
import { useAuthStore } from '@/store/authStore';
import { useLikeStore } from '@/store/likeStore';
import { usePlayerStore } from '@/store/playerStore';
import type { Track, User } from '@/types';

const user: User = {
  id: 1,
  email: 'user@test.com',
  nickname: 'tester',
  role: 'USER',
  phonePersonal: '010-1234-5678',
  phoneCompany: null,
  job: 'EDITOR',
  companyName: null,
  userType: 'INDIVIDUAL',
  isVerified: true,
  createdAt: '2026-04-16T00:00:00Z',
};

const track: Track = {
  id: 10,
  title: 'Track',
  artistName: 'Artist',
  duration: 120,
  bpm: 120,
  tonality: 'C',
  description: null,
  audioFile: '/api/tracks/10/stream',
  thumbnail: null,
  waveformData: null,
  tags: [],
  isActive: true,
  playCount: 0,
  likeCount: 0,
  downloadCount: 0,
  createdAt: '2026-04-16T00:00:00Z',
  updatedAt: '2026-04-16T00:00:00Z',
};

describe('authStore', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
    logoutSessionMock.mockReset();
    logoutSessionMock.mockResolvedValue(undefined);
    localStorage.clear();
    sessionStorage.clear();
    useAuthStore.setState({ user: null, accessToken: null, role: 'GUEST' });
    useLikeStore.setState({ likedIds: new Set(), loaded: false });
    useAlbumLikeStore.setState({ likedAlbumIds: new Set(), loaded: false });
    usePlayerStore.setState({
      currentTrack: null,
      isPlaying: false,
      currentTime: 0,
      duration: 0,
      queue: [],
    });
  });

  it('stores access token, refresh token, and user on login', () => {
    useAuthStore.getState().login('access-token', user, 'refresh-token');

    expect(localStorage.getItem('accessToken')).toBe('access-token');
    expect(localStorage.getItem('refreshToken')).toBe('refresh-token');
    expect(localStorage.getItem('user')).toBeTruthy();
    expect(useAuthStore.getState().user?.email).toBe('user@test.com');
    expect(useAuthStore.getState().role).toBe('USER');
  });

  it('fails atomically when durable login persistence is unavailable', () => {
    const setItem = vi.spyOn(Storage.prototype, 'setItem').mockImplementation(() => {
      throw new Error('storage unavailable');
    });

    expect(() => useAuthStore.getState().login('access-token', user, 'refresh-token')).toThrow(
      'Failed to persist authentication session',
    );

    setItem.mockRestore();
    expect(localStorage.getItem('accessToken')).toBeNull();
    expect(localStorage.getItem('refreshToken')).toBeNull();
    expect(localStorage.getItem('user')).toBeNull();
    expect(useAuthStore.getState()).toMatchObject({
      accessToken: null,
      user: null,
      role: 'GUEST',
    });
  });

  it('updates the active and persisted user together after a profile save', () => {
    localStorage.setItem('accessToken', 'access-token');
    localStorage.setItem('user', JSON.stringify(user));
    useAuthStore.setState({ user, accessToken: 'access-token', role: 'USER' });
    const updatedUser = { ...user, nickname: 'updated-name' };

    expect(useAuthStore.getState().updateUser(updatedUser)).toBe(true);

    expect(useAuthStore.getState().user).toEqual(updatedUser);
    expect(JSON.parse(localStorage.getItem('user') ?? 'null')).toEqual(updatedUser);
  });

  it('keeps the active and persisted user unchanged when profile persistence fails', () => {
    localStorage.setItem('accessToken', 'access-token');
    localStorage.setItem('user', JSON.stringify(user));
    useAuthStore.setState({ user, accessToken: 'access-token', role: 'USER' });
    const persistedBefore = localStorage.getItem('user');
    vi.spyOn(Storage.prototype, 'setItem').mockImplementation(() => {
      throw new Error('storage unavailable');
    });

    expect(useAuthStore.getState().updateUser({ ...user, nickname: 'not-committed' })).toBe(false);

    expect(useAuthStore.getState().user).toEqual(user);
    expect(localStorage.getItem('user')).toBe(persistedBefore);
  });

  it('calls server logout before clearing persisted auth and dependent stores', async () => {
    let resolveLogout: (() => void) | undefined;
    logoutSessionMock.mockImplementation(
      () =>
        new Promise<void>((resolve) => {
          resolveLogout = resolve;
        }),
    );
    localStorage.setItem('accessToken', 'access-token');
    localStorage.setItem('refreshToken', 'refresh-token');
    localStorage.setItem('user', JSON.stringify(user));
    useAuthStore.setState({ user, accessToken: 'access-token', role: 'USER' });
    useLikeStore.setState({ likedIds: new Set([1, 2]), loaded: true });
    useAlbumLikeStore.setState({ likedAlbumIds: new Set([3]), loaded: true });
    usePlayerStore.setState({
      currentTrack: track,
      isPlaying: true,
      currentTime: 30,
      duration: 120,
      queue: [track],
    });

    const logoutResult = useAuthStore.getState().logout();

    await vi.waitFor(() => expect(logoutSessionMock).toHaveBeenCalledTimes(1));
    expect(localStorage.getItem('accessToken')).toBe('access-token');
    expect(useAuthStore.getState().role).toBe('USER');

    resolveLogout?.();
    await expect(logoutResult).resolves.toBe(true);

    expect(localStorage.getItem('accessToken')).toBeNull();
    expect(localStorage.getItem('refreshToken')).toBeNull();
    expect(localStorage.getItem('user')).toBeNull();
    expect(useAuthStore.getState().role).toBe('GUEST');
    expect(useAuthStore.getState().user).toBeNull();
    expect(useLikeStore.getState().likedIds.size).toBe(0);
    expect(useLikeStore.getState().loaded).toBe(false);
    expect(useAlbumLikeStore.getState().likedAlbumIds.size).toBe(0);
    expect(useAlbumLikeStore.getState().loaded).toBe(false);
    expect(usePlayerStore.getState().currentTrack).toBeNull();
    expect(usePlayerStore.getState().queue).toEqual([]);
    expect(usePlayerStore.getState().isPlaying).toBe(false);
  });

  it('clears local auth but reports an unconfirmed server logout on network failure', async () => {
    logoutSessionMock.mockRejectedValue(new Error('network unavailable'));
    localStorage.setItem('accessToken', 'access-token');
    localStorage.setItem('refreshToken', 'refresh-token');
    localStorage.setItem('user', JSON.stringify(user));
    useAuthStore.setState({ user, accessToken: 'access-token', role: 'USER' });

    await expect(useAuthStore.getState().logout()).resolves.toBe(false);

    expect(localStorage.getItem('accessToken')).toBeNull();
    expect(localStorage.getItem('refreshToken')).toBeNull();
    expect(localStorage.getItem('user')).toBeNull();
    expect(useAuthStore.getState().role).toBe('GUEST');
  });
});

import { beforeEach, describe, expect, it, vi } from 'vitest';

const { fetchMeMock, logoutSessionMock } = vi.hoisted(() => ({
  fetchMeMock: vi.fn(),
  logoutSessionMock: vi.fn(),
}));

vi.mock('@/api/auth', () => ({
  fetchMe: fetchMeMock,
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
    fetchMeMock.mockReset();
    logoutSessionMock.mockReset();
    logoutSessionMock.mockResolvedValue('confirmed');
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

  it('refreshes the current user and persisted role from /users/me', async () => {
    const adminUser: User = { ...user, role: 'ADMIN' };
    const refreshedUser: User = { ...adminUser, role: 'USER' };
    localStorage.setItem('accessToken', 'access-token');
    localStorage.setItem('user', JSON.stringify(adminUser));
    useAuthStore.setState({ user: adminUser, accessToken: 'access-token', role: 'ADMIN' });
    fetchMeMock.mockResolvedValue(refreshedUser);

    await expect(useAuthStore.getState().refreshCurrentUser()).resolves.toEqual(refreshedUser);

    expect(fetchMeMock).toHaveBeenCalledTimes(1);
    expect(useAuthStore.getState().user).toEqual(refreshedUser);
    expect(useAuthStore.getState().role).toBe('USER');
    expect(JSON.parse(localStorage.getItem('user') ?? 'null')).toEqual(refreshedUser);
  });

  it('coalesces concurrent current-user refreshes into one request', async () => {
    const adminUser: User = { ...user, role: 'ADMIN' };
    let resolveFetch!: (refreshedUser: User) => void;
    fetchMeMock.mockReturnValue(
      new Promise<User>((resolve) => {
        resolveFetch = resolve;
      }),
    );
    localStorage.setItem('accessToken', 'access-token');
    localStorage.setItem('user', JSON.stringify(adminUser));
    useAuthStore.setState({ user: adminUser, accessToken: 'access-token', role: 'ADMIN' });

    const firstRefresh = useAuthStore.getState().refreshCurrentUser();
    const secondRefresh = useAuthStore.getState().refreshCurrentUser();

    expect(firstRefresh).toBe(secondRefresh);
    await vi.waitFor(() => expect(fetchMeMock).toHaveBeenCalledTimes(1));
    resolveFetch(adminUser);
    await expect(Promise.all([firstRefresh, secondRefresh])).resolves.toEqual([
      adminUser,
      adminUser,
    ]);
  });

  it('accepts an in-flight refresh after same-session access-token rotation', async () => {
    const refreshedUser: User = { ...user, nickname: 'refreshed-user' };
    let resolveFetch!: (refreshedUser: User) => void;
    fetchMeMock.mockReturnValue(
      new Promise<User>((resolve) => {
        resolveFetch = resolve;
      }),
    );
    localStorage.setItem('accessToken', 'old-access-token');
    localStorage.setItem('user', JSON.stringify(user));
    useAuthStore.setState({ user, accessToken: 'old-access-token', role: 'USER' });

    const refresh = useAuthStore.getState().refreshCurrentUser();
    await vi.waitFor(() => expect(fetchMeMock).toHaveBeenCalledTimes(1));
    localStorage.setItem('accessToken', 'rotated-access-token');
    useAuthStore.setState({ accessToken: 'rotated-access-token' });
    resolveFetch(refreshedUser);

    await expect(refresh).resolves.toEqual(refreshedUser);
    expect(useAuthStore.getState()).toMatchObject({
      accessToken: 'rotated-access-token',
      user: refreshedUser,
      role: 'USER',
    });
    expect(localStorage.getItem('accessToken')).toBe('rotated-access-token');
    expect(JSON.parse(localStorage.getItem('user') ?? 'null')).toEqual(refreshedUser);
  });

  it('rejects an in-flight refresh without restoring a logged-out session', async () => {
    const adminUser: User = { ...user, role: 'ADMIN' };
    let resolveFetch!: (refreshedUser: User) => void;
    fetchMeMock.mockReturnValue(
      new Promise<User>((resolve) => {
        resolveFetch = resolve;
      }),
    );
    localStorage.setItem('accessToken', 'old-access-token');
    localStorage.setItem('user', JSON.stringify(adminUser));
    useAuthStore.setState({ user: adminUser, accessToken: 'old-access-token', role: 'ADMIN' });

    const refresh = useAuthStore.getState().refreshCurrentUser();
    await vi.waitFor(() => expect(fetchMeMock).toHaveBeenCalledTimes(1));
    await expect(useAuthStore.getState().logout()).resolves.toEqual({ serverConfirmed: true });
    resolveFetch({ ...adminUser, nickname: 'stale-admin' });

    await expect(refresh).rejects.toThrow('Stale current-user refresh result');
    expect(useAuthStore.getState()).toMatchObject({
      accessToken: null,
      user: null,
      role: 'GUEST',
    });
    expect(localStorage.getItem('accessToken')).toBeNull();
    expect(localStorage.getItem('user')).toBeNull();
  });

  it('rejects an in-flight refresh without overwriting a replacement account', async () => {
    const originalUser: User = { ...user, role: 'ADMIN' };
    const replacementUser: User = {
      ...user,
      id: 2,
      email: 'replacement@test.com',
      nickname: 'replacement',
    };
    let resolveFetch!: (refreshedUser: User) => void;
    fetchMeMock.mockReturnValue(
      new Promise<User>((resolve) => {
        resolveFetch = resolve;
      }),
    );
    localStorage.setItem('accessToken', 'old-access-token');
    localStorage.setItem('user', JSON.stringify(originalUser));
    useAuthStore.setState({ user: originalUser, accessToken: 'old-access-token', role: 'ADMIN' });

    const refresh = useAuthStore.getState().refreshCurrentUser();
    await vi.waitFor(() => expect(fetchMeMock).toHaveBeenCalledTimes(1));
    useAuthStore
      .getState()
      .login('replacement-access-token', replacementUser, 'replacement-refresh-token');
    resolveFetch({ ...originalUser, nickname: 'stale-admin' });

    await expect(refresh).rejects.toThrow('Stale current-user refresh result');
    expect(useAuthStore.getState()).toMatchObject({
      accessToken: 'replacement-access-token',
      user: replacementUser,
      role: 'USER',
    });
    expect(localStorage.getItem('accessToken')).toBe('replacement-access-token');
    expect(JSON.parse(localStorage.getItem('user') ?? 'null')).toEqual(replacementUser);
  });

  it('rejects an old refresh and starts a new one after same-user re-login', async () => {
    const currentUser: User = { ...user, nickname: 'current-user' };
    let resolveOldFetch!: (refreshedUser: User) => void;
    fetchMeMock
      .mockReturnValueOnce(
        new Promise<User>((resolve) => {
          resolveOldFetch = resolve;
        }),
      )
      .mockResolvedValueOnce(currentUser);
    localStorage.setItem('accessToken', 'old-access-token');
    localStorage.setItem('user', JSON.stringify(user));
    useAuthStore.setState({ user, accessToken: 'old-access-token', role: 'USER' });

    const oldRefresh = useAuthStore.getState().refreshCurrentUser();
    await vi.waitFor(() => expect(fetchMeMock).toHaveBeenCalledTimes(1));
    useAuthStore.getState().login('replacement-access-token', user, 'replacement-refresh-token');
    const newRefresh = useAuthStore.getState().refreshCurrentUser();

    expect(newRefresh).not.toBe(oldRefresh);
    await expect(newRefresh).resolves.toEqual(currentUser);
    expect(fetchMeMock).toHaveBeenCalledTimes(2);
    resolveOldFetch({ ...user, nickname: 'stale-user' });
    await expect(oldRefresh).rejects.toThrow('Stale current-user refresh result');
    expect(useAuthStore.getState()).toMatchObject({
      accessToken: 'replacement-access-token',
      user: currentUser,
      role: 'USER',
    });
    expect(localStorage.getItem('accessToken')).toBe('replacement-access-token');
    expect(JSON.parse(localStorage.getItem('user') ?? 'null')).toEqual(currentUser);
  });

  it('rejects a current-user response for a different user id without persisting it', async () => {
    const persistedBefore = JSON.stringify(user);
    localStorage.setItem('accessToken', 'access-token');
    localStorage.setItem('user', persistedBefore);
    useAuthStore.setState({ user, accessToken: 'access-token', role: 'USER' });
    fetchMeMock.mockResolvedValue({ ...user, id: 2, email: 'other@test.com' });

    await expect(useAuthStore.getState().refreshCurrentUser()).rejects.toThrow(
      'Stale current-user refresh result',
    );
    expect(useAuthStore.getState()).toMatchObject({
      accessToken: 'access-token',
      user,
      role: 'USER',
    });
    expect(localStorage.getItem('accessToken')).toBe('access-token');
    expect(localStorage.getItem('user')).toBe(persistedBefore);
  });

  it('calls server logout before clearing persisted auth and dependent stores', async () => {
    let resolveLogout: ((outcome: 'confirmed') => void) | undefined;
    logoutSessionMock.mockImplementation(
      () =>
        new Promise<'confirmed'>((resolve) => {
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

    resolveLogout?.('confirmed');
    await expect(logoutResult).resolves.toEqual({ serverConfirmed: true });

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

  it('clears local auth but reports an unconfirmed server logout', async () => {
    logoutSessionMock.mockResolvedValue('unconfirmed');
    localStorage.setItem('accessToken', 'access-token');
    localStorage.setItem('refreshToken', 'refresh-token');
    localStorage.setItem('user', JSON.stringify(user));
    useAuthStore.setState({ user, accessToken: 'access-token', role: 'USER' });

    await expect(useAuthStore.getState().logout()).resolves.toEqual({ serverConfirmed: false });

    expect(localStorage.getItem('accessToken')).toBeNull();
    expect(localStorage.getItem('refreshToken')).toBeNull();
    expect(localStorage.getItem('user')).toBeNull();
    expect(useAuthStore.getState().role).toBe('GUEST');
  });

  it('coalesces concurrent logout attempts and returns the shared safe outcome', async () => {
    let resolveLogout!: (outcome: 'confirmed' | 'unconfirmed') => void;
    logoutSessionMock.mockImplementation(
      () =>
        new Promise<'confirmed' | 'unconfirmed'>((resolve) => {
          resolveLogout = resolve;
        }),
    );
    localStorage.setItem('accessToken', 'access-token');
    localStorage.setItem('refreshToken', 'refresh-token');
    localStorage.setItem('user', JSON.stringify(user));
    useAuthStore.setState({ user, accessToken: 'access-token', role: 'USER' });

    const firstLogout = useAuthStore.getState().logout();
    const secondLogout = useAuthStore.getState().logout();

    expect(secondLogout).toBe(firstLogout);
    await vi.waitFor(() => expect(logoutSessionMock).toHaveBeenCalledTimes(1));
    resolveLogout('unconfirmed');
    await expect(Promise.all([firstLogout, secondLogout])).resolves.toEqual([
      { serverConfirmed: false },
      { serverConfirmed: false },
    ]);
    expect(localStorage.getItem('accessToken')).toBeNull();
    expect(localStorage.getItem('refreshToken')).toBeNull();
    expect(localStorage.getItem('user')).toBeNull();
  });
});

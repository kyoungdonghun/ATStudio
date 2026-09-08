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
import { getAuthSessionGeneration, isAuthSessionCurrent, useAuthStore } from '@/store/authStore';
import { useLikeStore } from '@/store/likeStore';
import { usePlayerStore } from '@/store/playerStore';
import type { Track, User } from '@/types';
import { safeStorage } from '@/utils/safeStorage';

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
    useAuthStore.getState().clearSession();
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

  it('calls server logout before clearing auth and user-specific stores without stopping playback', async () => {
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
    expect(usePlayerStore.getState()).toMatchObject({
      currentTrack: track,
      queue: [track],
      currentTime: 30,
      duration: 120,
      isPlaying: true,
    });
  });

  it('keeps public playback when an expired session is cleared', () => {
    localStorage.setItem('accessToken', 'expired-access-token');
    localStorage.setItem('refreshToken', 'refresh-token');
    localStorage.setItem('user', JSON.stringify(user));
    useAuthStore.setState({ user, accessToken: 'expired-access-token', role: 'USER' });
    useLikeStore.setState({ likedIds: new Set([1]), loaded: true });
    useAlbumLikeStore.setState({ likedAlbumIds: new Set([2]), loaded: true });
    usePlayerStore.setState({
      currentTrack: track,
      isPlaying: false,
      currentTime: 30,
      duration: 120,
      queue: [track],
    });

    useAuthStore.getState().clearSession();

    expect(useAuthStore.getState()).toMatchObject({ accessToken: null, user: null, role: 'GUEST' });
    expect(useLikeStore.getState()).toMatchObject({ likedIds: new Set(), loaded: false });
    expect(useAlbumLikeStore.getState()).toMatchObject({ likedAlbumIds: new Set(), loaded: false });
    expect(usePlayerStore.getState()).toMatchObject({
      currentTrack: track,
      queue: [track],
      currentTime: 30,
      duration: 120,
      isPlaying: false,
    });
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

  it('advances generation before login persistence even for identical credentials (SEC-04)', () => {
    useAuthStore.getState().login('access-token', user, 'refresh-token');
    const previousGeneration = getAuthSessionGeneration();
    const generationsDuringPersistence: number[] = [];
    const setItem = safeStorage.setItem;
    vi.spyOn(safeStorage, 'setItem').mockImplementation((key, value) => {
      generationsDuringPersistence.push(getAuthSessionGeneration());
      return setItem(key, value);
    });

    useAuthStore.getState().login('access-token', user, 'refresh-token');

    expect(getAuthSessionGeneration()).toBe(previousGeneration + 1);
    expect(generationsDuringPersistence).toEqual(Array(3).fill(previousGeneration + 1));
    expect(isAuthSessionCurrent(previousGeneration)).toBe(false);
  });

  it('invalidates the prior session when staging tokens and when staging fails (SEC-04)', () => {
    useAuthStore.getState().login('access-token', user, 'refresh-token');
    const previousGeneration = getAuthSessionGeneration();
    useAuthStore.getState().stageTokens('staged-access', 'staged-refresh');
    expect(isAuthSessionCurrent(previousGeneration)).toBe(false);
    expect(useAuthStore.getState()).toMatchObject({ accessToken: 'staged-access', user: null });

    const stagedGeneration = getAuthSessionGeneration();
    vi.spyOn(safeStorage, 'setItem').mockReturnValue(false);
    expect(() => useAuthStore.getState().stageTokens('next-access', 'next-refresh')).toThrow(
      'Failed to stage authentication tokens',
    );
    expect(isAuthSessionCurrent(stagedGeneration)).toBe(false);
    expect(useAuthStore.getState().accessToken).toBeNull();
  });

  it.each(['accessToken', 'refreshToken'])(
    'preserves a reentrant login before stale staging can fail %s persistence (WI010-F1-R1)',
    (failedKey) => {
      useAuthStore.getState().login('initial-access', user, 'initial-refresh');
      const replacementUser = { ...user, id: 2 };
      const setItem = safeStorage.setItem;
      const persist = vi.spyOn(safeStorage, 'setItem').mockImplementation((key, value) => {
        if (key === failedKey && value.startsWith('staged-')) return false;
        return setItem(key, value);
      });
      let armed = true;
      const unsubscribe = useAuthStore.subscribe((state) => {
        if (!armed || state.accessToken !== null || state.user !== null) return;
        armed = false;
        useAuthStore.getState().login('replacement-access', replacementUser, 'replacement-refresh');
      });
      try {
        expect(() =>
          useAuthStore.getState().stageTokens('staged-access', 'staged-refresh'),
        ).not.toThrow();
        expect(armed).toBe(false);
        expect(useAuthStore.getState()).toMatchObject({
          accessToken: 'replacement-access',
          user: replacementUser,
          role: replacementUser.role,
        });
        expect(localStorage.getItem('accessToken')).toBe('replacement-access');
        expect(localStorage.getItem('refreshToken')).toBe('replacement-refresh');
        expect(JSON.parse(localStorage.getItem('user') ?? 'null')).toEqual(replacementUser);
        expect(persist).not.toHaveBeenCalledWith('accessToken', 'staged-access');
        expect(persist).not.toHaveBeenCalledWith('refreshToken', 'staged-refresh');
      } finally {
        unsubscribe();
      }
    },
  );

  it('invalidates refresh ownership immediately while server logout is pending (SEC-04)', async () => {
    let resolveLogout!: (outcome: 'confirmed') => void;
    logoutSessionMock.mockReturnValue(
      new Promise((resolve) => {
        resolveLogout = resolve;
      }),
    );
    let resolveFetch!: (value: User) => void;
    fetchMeMock.mockReturnValue(
      new Promise((resolve) => {
        resolveFetch = resolve;
      }),
    );
    useAuthStore.getState().login('access-token', user, 'refresh-token');
    const generation = getAuthSessionGeneration();
    const refresh = useAuthStore.getState().refreshCurrentUser();
    const refreshOutcome = Promise.allSettled([refresh]);
    await vi.waitFor(() => expect(fetchMeMock).toHaveBeenCalledTimes(1));

    const logout = useAuthStore.getState().logout();
    expect(isAuthSessionCurrent(generation)).toBe(false);
    expect(isAuthSessionCurrent(getAuthSessionGeneration())).toBe(false);
    expect(localStorage.getItem('accessToken')).toBe('access-token');
    await expect(useAuthStore.getState().refreshCurrentUser()).rejects.toThrow(
      'Cannot refresh current user without an authenticated session',
    );
    resolveFetch({ ...user, nickname: 'stale-user' });
    expect(await refreshOutcome).toMatchObject([{ status: 'rejected' }]);
    expect(useAuthStore.getState().user).toEqual(user);
    await vi.waitFor(() => expect(logoutSessionMock).toHaveBeenCalledTimes(1));
    resolveLogout('confirmed');
    await expect(logout).resolves.toEqual({ serverConfirmed: true });
    expect(useAuthStore.getState().accessToken).toBeNull();
  });

  it.each(['confirmed', 'unconfirmed', 'failure'])(
    'preserves identical same-user re-login when an old logout returns %s (SEC-04)',
    async (outcome) => {
      let resolveLogout!: (value: string) => void;
      let rejectLogout!: (reason: unknown) => void;
      logoutSessionMock.mockReturnValue(
        new Promise((resolve, reject) => {
          resolveLogout = resolve;
          rejectLogout = reject;
        }),
      );
      useAuthStore.getState().login('access-token', user, 'refresh-token');
      const logout = useAuthStore.getState().logout();
      await vi.waitFor(() => expect(logoutSessionMock).toHaveBeenCalledTimes(1));
      useAuthStore.getState().login('access-token', user, 'refresh-token');
      const newGeneration = getAuthSessionGeneration();
      useLikeStore.setState({ likedIds: new Set([9]), loaded: true });
      if (outcome === 'failure') rejectLogout(new Error('old logout failed'));
      else resolveLogout(outcome);

      await expect(logout).resolves.toEqual({ serverConfirmed: outcome === 'confirmed' });
      expect(getAuthSessionGeneration()).toBe(newGeneration);
      expect(isAuthSessionCurrent(newGeneration)).toBe(true);
      expect(useAuthStore.getState()).toMatchObject({ accessToken: 'access-token', user });
      expect(localStorage.getItem('refreshToken')).toBe('refresh-token');
      expect(useLikeStore.getState().likedIds).toEqual(new Set([9]));
    },
  );

  it('keeps the replacement logout flight when the old logout finishes (SEC-04)', async () => {
    const resolvers: Array<(value: 'confirmed') => void> = [];
    logoutSessionMock.mockImplementation(
      () =>
        new Promise((resolve) => {
          resolvers.push(resolve);
        }),
    );
    useAuthStore.getState().login('old-access', user, 'old-refresh');
    const oldLogout = useAuthStore.getState().logout();
    await vi.waitFor(() => expect(logoutSessionMock).toHaveBeenCalledTimes(1));
    useAuthStore.getState().login('new-access', { ...user, id: 2 }, 'new-refresh');
    const newLogout = useAuthStore.getState().logout();
    expect(newLogout).not.toBe(oldLogout);
    await vi.waitFor(() => expect(logoutSessionMock).toHaveBeenCalledTimes(2));

    resolvers[0]?.('confirmed');
    await oldLogout;
    expect(useAuthStore.getState().accessToken).toBe('new-access');
    expect(useAuthStore.getState().logout()).toBe(newLogout);
    expect(logoutSessionMock).toHaveBeenCalledTimes(2);
    resolvers[1]?.('confirmed');
    await newLogout;
    expect(useAuthStore.getState().accessToken).toBeNull();
  });

  it('does not send old logout through a replacement session after lazy import (SEC-04)', async () => {
    useAuthStore.getState().login('old-access', user, 'old-refresh');
    const logout = useAuthStore.getState().logout();
    useAuthStore.getState().login('new-access', { ...user, id: 2 }, 'new-refresh');

    await expect(logout).resolves.toEqual({ serverConfirmed: false });
    expect(logoutSessionMock).not.toHaveBeenCalled();
    expect(useAuthStore.getState().accessToken).toBe('new-access');
  });

  it('does not dispatch an old current-user refresh after lazy import (SEC-04)', async () => {
    useAuthStore.getState().login('old-access', user, 'old-refresh');
    const refresh = useAuthStore.getState().refreshCurrentUser();
    useAuthStore.getState().login('new-access', user, 'new-refresh');

    await expect(refresh).rejects.toThrow('Stale current-user refresh result');
    expect(fetchMeMock).not.toHaveBeenCalled();
    expect(useAuthStore.getState().accessToken).toBe('new-access');
  });
});

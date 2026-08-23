import { create } from 'zustand';
import type { User, UserRole } from '@/types';
import { useLikeStore } from '@/store/likeStore';
import { useAlbumLikeStore } from '@/store/albumLikeStore';
import { safeStorage } from '@/utils/safeStorage';

function loadUser(): User | null {
  try {
    const raw = safeStorage.getItem('user');
    return raw ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
}

function loadRole(): UserRole {
  const user = loadUser();
  return user?.role ?? 'GUEST';
}

interface CurrentUserRefresh {
  sessionGeneration: number;
  userID: number;
  promise: Promise<User>;
}

let sessionGeneration = 0;
let currentUserRefresh: CurrentUserRefresh | null = null;
let logoutInFlight: Promise<LogoutOutcome> | null = null;

export const UNCONFIRMED_LOGOUT_WARNING =
  '서버 로그아웃 확인에 실패했습니다. 이 기기에서는 로그아웃되었습니다.';

export interface LogoutOutcome {
  serverConfirmed: boolean;
}

function advanceSessionGeneration(): void {
  sessionGeneration += 1;
}

interface AuthState {
  user: User | null;
  accessToken: string | null;
  role: UserRole;
  stageTokens: (accessToken: string, refreshToken: string) => void;
  login: (accessToken: string, user: User, refreshToken?: string | null) => void;
  updateUser: (user: User) => boolean;
  refreshCurrentUser: () => Promise<User>;
  logout: () => Promise<LogoutOutcome>;
  clearSession: () => void;
  isAuthenticated: () => boolean;
}

export const useAuthStore = create<AuthState>((set, get) => ({
  user: loadUser(),
  accessToken: safeStorage.getItem('accessToken'),
  role: loadRole(),

  stageTokens: (accessToken: string, refreshToken: string) => {
    get().clearSession();

    const accessTokenStored = safeStorage.setItem('accessToken', accessToken);
    const refreshTokenStored = safeStorage.setItem('refreshToken', refreshToken);
    if (!accessTokenStored || !refreshTokenStored) {
      get().clearSession();
      throw new Error('Failed to stage authentication tokens');
    }

    set({ accessToken });
  },

  login: (accessToken: string, user: User, refreshToken) => {
    const accessTokenStored = safeStorage.setItem('accessToken', accessToken);
    let refreshTokenStored = true;
    if (refreshToken !== undefined) {
      if (refreshToken) {
        refreshTokenStored = safeStorage.setItem('refreshToken', refreshToken);
      } else {
        safeStorage.removeItem('refreshToken');
      }
    }
    const userStored = safeStorage.setItem('user', JSON.stringify(user));
    if (!accessTokenStored || !refreshTokenStored || !userStored) {
      get().clearSession();
      throw new Error('Failed to persist authentication session');
    }
    advanceSessionGeneration();
    set({ accessToken, user, role: user.role });
  },

  updateUser: (user: User) => {
    if (!get().accessToken || !safeStorage.setItem('user', JSON.stringify(user))) {
      return false;
    }

    set({ user, role: user.role });
    return true;
  },

  refreshCurrentUser: () => {
    const sessionAccessToken = get().accessToken;
    const sessionUserID = get().user?.id;
    const initiatingSessionGeneration = sessionGeneration;
    if (!sessionAccessToken || sessionUserID === undefined) {
      return Promise.reject(
        new Error('Cannot refresh current user without an authenticated session'),
      );
    }
    if (
      currentUserRefresh?.sessionGeneration === initiatingSessionGeneration &&
      currentUserRefresh.userID === sessionUserID
    ) {
      return currentUserRefresh.promise;
    }

    const promise = (async () => {
      const { fetchMe } = await import('@/api/auth');
      const refreshedUser = await fetchMe();
      const currentSession = get();
      if (
        sessionGeneration !== initiatingSessionGeneration ||
        currentSession.user?.id !== sessionUserID ||
        refreshedUser.id !== sessionUserID
      ) {
        throw new Error('Stale current-user refresh result');
      }
      if (!get().updateUser(refreshedUser)) {
        throw new Error('Failed to persist refreshed user');
      }
      return refreshedUser;
    })();

    const refresh = {
      sessionGeneration: initiatingSessionGeneration,
      userID: sessionUserID,
      promise,
    };
    currentUserRefresh = refresh;
    const clearRefresh = () => {
      if (currentUserRefresh === refresh) currentUserRefresh = null;
    };
    void promise.then(clearRefresh, clearRefresh);

    return promise;
  },

  logout: () => {
    if (logoutInFlight) return logoutInFlight;

    const promise = (async (): Promise<LogoutOutcome> => {
      let serverConfirmed = false;
      try {
        const { logoutSession } = await import('@/api/auth');
        serverConfirmed = (await logoutSession()) === 'confirmed';
      } catch {
        serverConfirmed = false;
      } finally {
        get().clearSession();
      }
      return { serverConfirmed };
    })();

    logoutInFlight = promise;
    const clearLogoutInFlight = () => {
      if (logoutInFlight === promise) logoutInFlight = null;
    };
    void promise.then(clearLogoutInFlight, clearLogoutInFlight);
    return promise;
  },

  clearSession: () => {
    advanceSessionGeneration();
    safeStorage.removeItem('accessToken');
    safeStorage.removeItem('refreshToken');
    safeStorage.removeItem('user');
    set({ accessToken: null, user: null, role: 'GUEST' });

    // Reset only user-dependent stores. Public playback survives session changes.
    useLikeStore.setState({ likedIds: new Set(), loaded: false });
    useAlbumLikeStore.setState({ likedAlbumIds: new Set(), loaded: false });
  },

  isAuthenticated: () => get().accessToken !== null,
}));

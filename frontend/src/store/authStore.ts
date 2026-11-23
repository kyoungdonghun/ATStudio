import { create } from 'zustand';
import type { User, UserRole } from '@/types';
import { useLikeStore } from '@/store/likeStore';
import { useAlbumLikeStore } from '@/store/albumLikeStore';
import { safeStorage } from '@/utils/safeStorage';
import { isPilotMode } from '@/config/pilotMode';
import {
  isPilotSessionStorageKey,
  PILOT_SESSION_CHANGED,
  readPilotSession,
  removePilotSession,
  savePilotSession,
  startPilotSession,
} from './pilotSession';

let pilotSession = isPilotMode ? readPilotSession() : null;

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
let logoutInFlight: {
  sessionGeneration: number;
  promise: Promise<LogoutOutcome>;
} | null = null;

export function getAuthSessionGeneration(): number {
  return sessionGeneration;
}

export function isAuthSessionCurrent(generation: number): boolean {
  return isSameAuthSession(generation) && logoutInFlight?.sessionGeneration !== generation;
}

export function isSameAuthSession(generation: number): boolean {
  if (isPilotMode) synchronizePilotSession();
  return sessionGeneration === generation;
}

export function getSessionRefreshToken(): string | null {
  return isPilotMode ? (pilotSession?.refreshToken ?? null) : safeStorage.getItem('refreshToken');
}

export function replaceSessionTokens(accessToken: string, refreshToken?: string): void {
  if (isPilotMode) {
    if (!pilotSession) throw new Error('Missing pilot session');
    const next = {
      ...pilotSession,
      accessToken,
      refreshToken: refreshToken ?? pilotSession.refreshToken,
    };
    savePilotSession(next);
    pilotSession = next;
  } else if (
    !safeStorage.setItem('accessToken', accessToken) ||
    (refreshToken && !safeStorage.setItem('refreshToken', refreshToken))
  ) {
    throw new Error('Failed to persist refreshed authentication session');
  }
  useAuthStore.setState({ accessToken });
}

export const UNCONFIRMED_LOGOUT_WARNING =
  '서버 로그아웃 확인에 실패했습니다. 이 기기에서는 로그아웃되었습니다.';

export interface LogoutOutcome {
  serverConfirmed: boolean;
}

function advanceSessionGeneration(): void {
  sessionGeneration += 1;
  if (isPilotMode) window.dispatchEvent(new Event(PILOT_SESSION_CHANGED));
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
  user: isPilotMode ? (pilotSession?.user ?? null) : loadUser(),
  accessToken: isPilotMode
    ? (pilotSession?.accessToken ?? null)
    : safeStorage.getItem('accessToken'),
  role: isPilotMode ? (pilotSession?.user.role ?? 'GUEST') : loadRole(),

  stageTokens: (accessToken: string, refreshToken: string) => {
    const stagedGeneration = sessionGeneration + 1;
    get().clearSession();
    // Clearing notifies subscribers synchronously; a newer session owns any subsequent writes.
    if (!isAuthSessionCurrent(stagedGeneration)) return;

    const accessTokenStored = safeStorage.setItem('accessToken', accessToken);
    const refreshTokenStored = safeStorage.setItem('refreshToken', refreshToken);
    if (!accessTokenStored || !refreshTokenStored) {
      get().clearSession();
      throw new Error('Failed to stage authentication tokens');
    }

    set({ accessToken });
  },

  login: (accessToken: string, user: User, refreshToken) => {
    if (isPilotMode) {
      pilotSession = startPilotSession(user, accessToken, refreshToken ?? null);
      advanceSessionGeneration();
      useLikeStore.setState({ likedIds: new Set(), loaded: false });
      set({ accessToken, user, role: user.role });
      return;
    }
    advanceSessionGeneration();
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
    set({ accessToken, user, role: user.role });
  },

  updateUser: (user: User) => {
    if (isPilotMode) {
      if (!pilotSession || user.id !== pilotSession.user.id) return false;
      const next = { ...pilotSession, user };
      savePilotSession(next);
      pilotSession = next;
    } else if (!get().accessToken || !safeStorage.setItem('user', JSON.stringify(user))) {
      return false;
    }

    set({ user, role: user.role });
    return true;
  },

  refreshCurrentUser: () => {
    const sessionAccessToken = get().accessToken;
    const sessionUserID = get().user?.id;
    const initiatingSessionGeneration = sessionGeneration;
    if (
      !sessionAccessToken ||
      sessionUserID === undefined ||
      !isAuthSessionCurrent(initiatingSessionGeneration)
    ) {
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
      if (!isAuthSessionCurrent(initiatingSessionGeneration)) {
        throw new Error('Stale current-user refresh result');
      }
      const refreshedUser = await fetchMe();
      const currentSession = get();
      if (
        !isAuthSessionCurrent(initiatingSessionGeneration) ||
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
    if (!isSameAuthSession(sessionGeneration)) return Promise.resolve({ serverConfirmed: false });
    if (logoutInFlight?.sessionGeneration === sessionGeneration) return logoutInFlight.promise;

    // Invalidate pending refreshes before awaiting server revocation.
    advanceSessionGeneration();
    const initiatingSessionGeneration = sessionGeneration;

    const promise = (async (): Promise<LogoutOutcome> => {
      let serverConfirmed = false;
      try {
        const { logoutSession } = await import('@/api/auth');
        if (isSameAuthSession(initiatingSessionGeneration)) {
          serverConfirmed = (await logoutSession()) === 'confirmed';
        }
      } catch {
        serverConfirmed = false;
      } finally {
        if (isSameAuthSession(initiatingSessionGeneration)) get().clearSession();
      }
      return { serverConfirmed };
    })();

    const logout = { sessionGeneration: initiatingSessionGeneration, promise };
    logoutInFlight = logout;
    const clearLogoutInFlight = () => {
      if (logoutInFlight === logout) logoutInFlight = null;
    };
    void promise.then(clearLogoutInFlight, clearLogoutInFlight);
    return promise;
  },

  clearSession: () => {
    advanceSessionGeneration();
    if (isPilotMode) {
      if (pilotSession) removePilotSession(pilotSession.id);
      pilotSession = null;
    } else {
      safeStorage.removeItem('accessToken');
      safeStorage.removeItem('refreshToken');
      safeStorage.removeItem('user');
    }
    set({ accessToken: null, user: null, role: 'GUEST' });

    // Reset only user-dependent stores. Public playback survives session changes.
    useLikeStore.setState({ likedIds: new Set(), loaded: false });
    useAlbumLikeStore.setState({ likedAlbumIds: new Set(), loaded: false });
  },

  isAuthenticated: () => get().accessToken !== null,
}));

// Check on storage notification and at private request/response boundaries;
// suspended tabs may receive the browser's storage event after resuming work.
function synchronizePilotSession(): void {
  const next = readPilotSession();
  if (
    next?.id === pilotSession?.id &&
    next?.accessToken === pilotSession?.accessToken &&
    next?.refreshToken === pilotSession?.refreshToken &&
    next?.user.role === pilotSession?.user.role &&
    next?.user.nickname === pilotSession?.user.nickname
  )
    return;
  if (next?.id !== pilotSession?.id || next?.user.role !== pilotSession?.user.role) {
    advanceSessionGeneration();
    useLikeStore.setState({ likedIds: new Set(), loaded: false });
  }
  pilotSession = next;
  useAuthStore.setState({
    user: next?.user ?? null,
    accessToken: next?.accessToken ?? null,
    role: next?.user.role ?? 'GUEST',
  });
}

if (isPilotMode && typeof window !== 'undefined') {
  window.addEventListener('storage', (event) => {
    if (isPilotSessionStorageKey(event.key)) synchronizePilotSession();
  });
}

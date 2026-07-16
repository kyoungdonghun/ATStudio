import { create } from 'zustand';
import type { User, UserRole } from '@/types';
import { usePlayerStore } from '@/store/playerStore';
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

interface AuthState {
  user: User | null;
  accessToken: string | null;
  role: UserRole;
  stageTokens: (accessToken: string, refreshToken: string) => void;
  login: (accessToken: string, user: User, refreshToken?: string | null) => void;
  updateUser: (user: User) => boolean;
  logout: () => Promise<boolean>;
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
    safeStorage.setItem('accessToken', accessToken);
    if (refreshToken !== undefined) {
      if (refreshToken) {
        safeStorage.setItem('refreshToken', refreshToken);
      } else {
        safeStorage.removeItem('refreshToken');
      }
    }
    safeStorage.setItem('user', JSON.stringify(user));
    set({ accessToken, user, role: user.role });
  },

  updateUser: (user: User) => {
    if (!get().accessToken || !safeStorage.setItem('user', JSON.stringify(user))) {
      return false;
    }

    set({ user, role: user.role });
    return true;
  },

  logout: async () => {
    let serverConfirmed = true;
    try {
      const { logoutSession } = await import('@/api/auth');
      await logoutSession();
    } catch {
      serverConfirmed = false;
    } finally {
      get().clearSession();
    }
    return serverConfirmed;
  },

  clearSession: () => {
    safeStorage.removeItem('accessToken');
    safeStorage.removeItem('refreshToken');
    safeStorage.removeItem('user');
    set({ accessToken: null, user: null, role: 'GUEST' });

    // Reset all user-dependent stores on session change
    usePlayerStore.getState().clearQueue();
    useLikeStore.setState({ likedIds: new Set(), loaded: false });
    useAlbumLikeStore.setState({ likedAlbumIds: new Set(), loaded: false });
  },

  isAuthenticated: () => get().accessToken !== null,
}));

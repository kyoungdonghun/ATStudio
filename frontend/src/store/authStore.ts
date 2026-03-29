import { create } from 'zustand';
import type { User, UserRole } from '@/types';
import { usePlayerStore } from '@/store/playerStore';
import { useLikeStore } from '@/store/likeStore';
import { useAlbumLikeStore } from '@/store/albumLikeStore';

function loadUser(): User | null {
  try {
    const raw = localStorage.getItem('user');
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
  login: (token: string, user: User) => void;
  logout: () => void;
  isAuthenticated: () => boolean;
}

export const useAuthStore = create<AuthState>((set, get) => ({
  user: loadUser(),
  accessToken: localStorage.getItem('accessToken'),
  role: loadRole(),

  login: (token: string, user: User) => {
    localStorage.setItem('accessToken', token);
    localStorage.setItem('user', JSON.stringify(user));
    set({ accessToken: token, user, role: user.role });
  },

  logout: () => {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('user');
    set({ accessToken: null, user: null, role: 'GUEST' });

    // Reset all user-dependent stores on session change
    usePlayerStore.getState().clearQueue();
    useLikeStore.setState({ likedIds: new Set(), loaded: false });
    useAlbumLikeStore.setState({ likedAlbumIds: new Set(), loaded: false });
  },

  isAuthenticated: () => get().accessToken !== null,
}));

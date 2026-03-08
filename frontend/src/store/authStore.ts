import { create } from 'zustand';
import type { User, UserRole } from '@/types';

interface AuthState {
  user: User | null;
  accessToken: string | null;
  role: UserRole;
  login: (token: string, user: User) => void;
  logout: () => void;
  isAuthenticated: () => boolean;
}

export const useAuthStore = create<AuthState>((set, get) => ({
  user: null,
  accessToken: localStorage.getItem('accessToken'),
  role: 'GUEST',

  login: (token: string, user: User) => {
    localStorage.setItem('accessToken', token);
    set({ accessToken: token, user, role: user.role });
  },

  logout: () => {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    set({ accessToken: null, user: null, role: 'GUEST' });
  },

  isAuthenticated: () => get().accessToken !== null,
}));

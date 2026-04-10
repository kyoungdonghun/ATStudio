import { create } from 'zustand';
import { safeStorage } from '@/utils/safeStorage';

type Theme = 'dark' | 'light';

interface ThemeState {
  theme: Theme;
  toggle: () => void;
}

function getInitialTheme(): Theme {
  const stored = safeStorage.getItem('theme');
  if (stored === 'light' || stored === 'dark') return stored;
  return 'dark';
}

function applyTheme(theme: Theme) {
  if (theme === 'light') {
    document.documentElement.setAttribute('data-theme', 'light');
  } else {
    document.documentElement.removeAttribute('data-theme');
  }
}

// Apply on load (before React renders)
applyTheme(getInitialTheme());

export const useThemeStore = create<ThemeState>((set, get) => ({
  theme: getInitialTheme(),

  toggle: () => {
    const next = get().theme === 'dark' ? 'light' : 'dark';
    safeStorage.setItem('theme', next);
    applyTheme(next);
    set({ theme: next });
  },
}));

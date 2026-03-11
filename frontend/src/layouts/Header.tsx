import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuthStore } from '@/store/authStore';
import { useThemeStore } from '@/store/themeStore';
import Button from '@/components/ui/Button';
import styles from './Header.module.css';

interface NavItem {
  label: string;
  path: string;
}

const NAV_ITEMS: NavItem[] = [
  { label: '\uD648', path: '/' },
  { label: '\uC74C\uC6D0', path: '/tracks' },
  { label: '\uC568\uBC94', path: '/albums' },
  { label: '\uAD6C\uB3C5', path: '/subscriptions' },
  { label: '\uACF5\uC9C0', path: '/notices' },
];

const AUTH_NAV_ITEMS: NavItem[] = [
  { label: '\uC88B\uC544\uC694', path: '/likes' },
  { label: '\uC7AC\uC0DD\uBAA9\uB85D', path: '/playlists' },
];

function SearchIcon() {
  return (
    <svg
      className={styles.searchIcon}
      width="14"
      height="14"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      viewBox="0 0 24 24"
    >
      <circle cx="11" cy="11" r="8" />
      <path d="m21 21-4.35-4.35" />
    </svg>
  );
}

function ThemeToggle() {
  const theme = useThemeStore((s) => s.theme);
  const toggle = useThemeStore((s) => s.toggle);

  return (
    <button
      className={styles.themeToggle}
      onClick={toggle}
      aria-label={theme === 'dark' ? 'Switch to light mode' : 'Switch to dark mode'}
      title={theme === 'dark' ? '라이트 모드' : '다크 모드'}
    >
      {theme === 'dark' ? (
        <svg width="16" height="16" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
          <circle cx="12" cy="12" r="5" />
          <path d="M12 1v2M12 21v2M4.22 4.22l1.42 1.42M18.36 18.36l1.42 1.42M1 12h2M21 12h2M4.22 19.78l1.42-1.42M18.36 5.64l1.42-1.42" />
        </svg>
      ) : (
        <svg width="16" height="16" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
          <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z" />
        </svg>
      )}
    </button>
  );
}

export default function Header() {
  const location = useLocation();
  const navigate = useNavigate();
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated());
  const user = useAuthStore((s) => s.user);
  const logout = useAuthStore((s) => s.logout);

  function isActive(path: string): boolean {
    if (path === '/') return location.pathname === '/';
    return location.pathname.startsWith(path);
  }

  return (
    <header className={styles.header}>
      <Link to="/" className={styles.logo}>
        ATStudio
      </Link>

      <div className={styles.search}>
        <SearchIcon />
        <span>{'\uC74C\uC6D0, \uC568\uBC94 \uAC80\uC0C9'}</span>
      </div>

      <nav className={styles.navTabs}>
        {NAV_ITEMS.map((item) => (
          <Link
            key={item.path}
            to={item.path}
            className={`${styles.tab} ${isActive(item.path) ? styles.tabActive : ''}`}
          >
            {item.label}
          </Link>
        ))}
        {isAuthenticated && AUTH_NAV_ITEMS.map((item) => (
          <Link
            key={item.path}
            to={item.path}
            className={`${styles.tab} ${isActive(item.path) ? styles.tabActive : ''}`}
          >
            {item.label}
          </Link>
        ))}
      </nav>

      <div className={styles.navRight}>
        <ThemeToggle />
        {isAuthenticated ? (
          <>
            {user && (
              <span className={styles.greeting}>
                {'\uC548\uB155\uD558\uC138\uC694, '}
                <strong className={styles.greetingName}>{user.nickname}</strong>
              </span>
            )}
            <Link to="/profile">
              <Button variant="ghost" size="md">
                {'\uB0B4 \uACC4\uC815'}
              </Button>
            </Link>
            <Button
              variant="ghost"
              size="md"
              onClick={() => {
                logout();
                navigate('/', { replace: true });
              }}
            >
              {'\uB85C\uADF8\uC544\uC6C3'}
            </Button>
          </>
        ) : (
          <>
            <Link to="/login">
              <Button variant="ghost" size="md">
                {'\uB85C\uADF8\uC778'}
              </Button>
            </Link>
            <Link to="/subscriptions">
              <Button variant="primary" size="md">
                {'\uAD6C\uB3C5 \uC2DC\uC791\uD558\uAE30'}
              </Button>
            </Link>
          </>
        )}
      </div>
    </header>
  );
}

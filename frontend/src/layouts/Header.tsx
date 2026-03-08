import { Link, useLocation } from 'react-router-dom';
import { useAuthStore } from '@/store/authStore';
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

export default function Header() {
  const location = useLocation();
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated());
  const user = useAuthStore((s) => s.user);

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
      </nav>

      <div className={styles.navRight}>
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

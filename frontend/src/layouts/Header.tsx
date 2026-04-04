import { useState, useEffect } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuthStore } from '@/store/authStore';
import { useThemeStore } from '@/store/themeStore';
import { SEARCH_KEYWORD_MAX } from '@/utils/validation';
import Button from '@/components/ui/Button';
import styles from './Header.module.css';

interface NavItem {
  label: string;
  path: string;
}

const BASE_NAV_ITEMS: NavItem[] = [
  { label: '홈', path: '/' },
  { label: '음원', path: '/tracks' },
  { label: '앨범', path: '/albums' },
  { label: '구독', path: '/subscriptions' },
  { label: '공지', path: '/notices' },
  { label: '문의', path: '/questions' },
];

const SUBSCRIBER_NAV_ITEMS: NavItem[] = [
  { label: '좋아요', path: '/likes' },
  { label: '재생목록', path: '/playlists' },
  { label: '다운로드', path: '/download-queue' },
  { label: '라이선스', path: '/licenses' },
];

const ADMIN_NAV_ITEMS: NavItem[] = [
  { label: '관리자', path: '/admin/dashboard' },
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
  const role = useAuthStore((s) => s.role);
  const logout = useAuthStore((s) => s.logout);
  const isAdmin = role === 'ADMIN';
  const navItems = isAdmin
    ? BASE_NAV_ITEMS.filter((item) => item.path !== '/subscriptions')
    : BASE_NAV_ITEMS;
  const roleNavItems = isAdmin ? ADMIN_NAV_ITEMS : SUBSCRIBER_NAV_ITEMS;
  const [menuOpen, setMenuOpen] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');

  // Close mobile menu on route change
  useEffect(() => {
    setMenuOpen(false);
  }, [location.pathname]);

  function handleSearch(e: React.FormEvent) {
    e.preventDefault();
    const q = searchQuery.trim();
    if (!q) return;
    navigate(`/tracks?keyword=${encodeURIComponent(q)}&page=1`);
    setSearchQuery('');
    setMenuOpen(false);
  }

  function isActive(path: string): boolean {
    if (path === '/') return location.pathname === '/';
    return location.pathname.startsWith(path);
  }

  return (
    <>
      <header className={styles.header}>
        <Link to="/" className={styles.logo}>
          ATStudio
        </Link>

        {/* Desktop/Tablet: inline search */}
        <form className={styles.search} onSubmit={handleSearch}>
          <SearchIcon />
          <input
            className={styles.searchInput}
            type="text"
            placeholder="음원, 앨범 검색"
            maxLength={SEARCH_KEYWORD_MAX}
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
          />
        </form>

        {/* Desktop/Tablet: inline nav */}
        <nav className={styles.navTabs}>
          {navItems.map((item) => (
            <Link
              key={item.path}
              to={item.path}
              className={`${styles.tab} ${isActive(item.path) ? styles.tabActive : ''}`}
            >
              {item.label}
            </Link>
          ))}
          {isAuthenticated && roleNavItems.map((item) => (
            <Link
              key={item.path}
              to={item.path}
              className={`${styles.tab} ${isActive(item.path) ? styles.tabActive : ''}`}
            >
              {item.label}
            </Link>
          ))}
        </nav>

        {/* Desktop/Tablet: inline right section */}
        <div className={styles.navRight}>
          <ThemeToggle />
          {isAuthenticated ? (
            <>
              {user && (
                <span className={styles.greeting}>
                  {'안녕하세요, '}
                  <strong className={styles.greetingName}>{user.nickname}</strong>
                </span>
              )}
              <Link to="/profile">
                <Button variant="ghost" size="md">
                  {'내 계정'}
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
                {'로그아웃'}
              </Button>
            </>
          ) : (
            <>
              <Link to="/login">
                <Button variant="ghost" size="md">
                  {'로그인'}
                </Button>
              </Link>
              <Link to="/subscriptions">
                <Button variant="primary" size="md">
                  {'구독 시작하기'}
                </Button>
              </Link>
            </>
          )}
        </div>

        {/* Mobile: hamburger button */}
        <div className={styles.mobileRight}>
          <ThemeToggle />
          <button
            className={styles.hamburger}
            onClick={() => setMenuOpen((v) => !v)}
            aria-label={menuOpen ? 'Close menu' : 'Open menu'}
          >
            {menuOpen ? '\u2715' : '\u2630'}
          </button>
        </div>
      </header>

      {/* Mobile: slide-down menu */}
      {menuOpen && (
        <div className={styles.mobileOverlay} onClick={() => setMenuOpen(false)} />
      )}
      <div className={`${styles.mobileMenu} ${menuOpen ? styles.mobileMenuOpen : ''}`}>
        {/* Search */}
        <form className={styles.mobileSearch} onSubmit={handleSearch}>
          <SearchIcon />
          <input
            className={styles.searchInput}
            type="text"
            placeholder="음원, 앨범 검색"
            maxLength={SEARCH_KEYWORD_MAX}
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
          />
        </form>

        {/* Nav links */}
        <nav className={styles.mobileNav}>
          {navItems.map((item) => (
            <Link
              key={item.path}
              to={item.path}
              className={`${styles.mobileNavItem} ${isActive(item.path) ? styles.mobileNavItemActive : ''}`}
            >
              {item.label}
            </Link>
          ))}
          {isAuthenticated && roleNavItems.map((item) => (
            <Link
              key={item.path}
              to={item.path}
              className={`${styles.mobileNavItem} ${isActive(item.path) ? styles.mobileNavItemActive : ''}`}
            >
              {item.label}
            </Link>
          ))}
        </nav>

        {/* Auth actions */}
        <div className={styles.mobileAuth}>
          {isAuthenticated ? (
            <>
              {user && (
                <span className={styles.mobileGreeting}>
                  {'안녕하세요, '}
                  <strong>{user.nickname}</strong>
                </span>
              )}
              <Link to="/profile" className={styles.mobileAuthLink}>
                {'내 계정'}
              </Link>
              <button
                className={styles.mobileAuthLink}
                onClick={() => {
                  logout();
                  navigate('/', { replace: true });
                }}
              >
                {'로그아웃'}
              </button>
            </>
          ) : (
            <>
              <Link to="/login" className={styles.mobileAuthLink}>
                {'로그인'}
              </Link>
              <Link to="/subscriptions" className={styles.mobileAuthLinkPrimary}>
                {'구독 시작하기'}
              </Link>
            </>
          )}
        </div>
      </div>
    </>
  );
}

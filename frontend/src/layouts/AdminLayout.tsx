import { useState, useEffect } from 'react';
import { Outlet, Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuthStore } from '@/store/authStore';
import styles from './AdminLayout.module.css';

interface MenuItem {
  label: string;
  path: string;
}

const MENU_ITEMS: MenuItem[] = [
  { label: '대시보드', path: '/admin/dashboard' },
  { label: '음원 관리', path: '/admin/track-manage' },
  { label: '음원 등록', path: '/admin/tracks/upload' },
  { label: '앨범 관리', path: '/admin/albums' },
  { label: '사용자 관리', path: '/admin/users' },
  { label: '구독 플랜', path: '/admin/subscriptions' },
  { label: '사용자 구독', path: '/admin/user-subscriptions' },
  { label: '라이선스 관리', path: '/admin/licenses' },
  { label: '문의 관리', path: '/admin/questions' },
  { label: '기업 인증', path: '/admin/company-certifications' },
  { label: '태그 관리', path: '/admin/tags' },
  { label: '공지사항', path: '/admin/notices/new' },
];

function isActive(currentPath: string, menuPath: string): boolean {
  // Exact match for specific paths to avoid false positives
  if (menuPath === '/admin/albums') {
    return currentPath === '/admin/albums' ||
      currentPath.startsWith('/admin/albums/');
  }
  if (menuPath === '/admin/tracks/upload') {
    return currentPath === '/admin/tracks/upload';
  }
  if (menuPath === '/admin/notices/new') {
    return currentPath === '/admin/notices/new' ||
      currentPath.startsWith('/admin/notices/');
  }
  return currentPath === menuPath || currentPath.startsWith(menuPath + '/');
}

export default function AdminLayout() {
  const location = useLocation();
  const navigate = useNavigate();
  const user = useAuthStore((s) => s.user);
  const logout = useAuthStore((s) => s.logout);
  const [sidebarOpen, setSidebarOpen] = useState(false);

  // Close mobile sidebar on route change
  useEffect(() => {
    setSidebarOpen(false);
  }, [location.pathname]);

  function handleLogout() {
    logout();
    navigate('/', { replace: true });
  }

  return (
    <div className={styles.layout}>
      {/* Sidebar */}
      <aside
        className={`${styles.sidebar} ${sidebarOpen ? styles.sidebarOpen : ''}`}
      >
        <div className={styles.sidebarHeader}>
          <Link to="/admin/dashboard" className={styles.logo}>
            ATStudio
          </Link>
          <span className={styles.logoSub}>Admin</span>
        </div>

        <nav className={styles.sidebarNav}>
          {MENU_ITEMS.map((item) => (
            <Link
              key={item.path}
              to={item.path}
              className={`${styles.navItem} ${
                isActive(location.pathname, item.path)
                  ? styles.navItemActive
                  : ''
              }`}
            >
              {item.label}
            </Link>
          ))}
        </nav>

        <div className={styles.sidebarFooter}>
          <div className={styles.navDivider} />
          <Link to="/" className={styles.backLink}>
            {'< '}
            {'사이트로 돌아가기'}
          </Link>
        </div>
      </aside>

      {/* Mobile overlay */}
      {sidebarOpen && (
        <div
          className={styles.overlay}
          onClick={() => setSidebarOpen(false)}
        />
      )}

      {/* Top bar */}
      <header className={styles.topbar}>
        {/* Mobile hamburger (hidden on desktop via CSS) */}
        <button
          className={styles.menuToggle}
          onClick={() => setSidebarOpen((v) => !v)}
          aria-label={sidebarOpen ? 'Close menu' : 'Open menu'}
        >
          {sidebarOpen ? '\u2715' : '\u2630'}
        </button>

        <div className={styles.userInfo}>
          {user && (
            <span>
              <span className={styles.userName}>{user.nickname}</span>
              {' (Admin)'}
            </span>
          )}
        </div>
        <button className={styles.logoutBtn} onClick={handleLogout}>
          {'로그아웃'}
        </button>
      </header>

      {/* Main content */}
      <main className={styles.content}>
        <Outlet />
      </main>
    </div>
  );
}

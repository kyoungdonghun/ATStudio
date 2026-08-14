import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { Outlet, Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuthStore } from '@/store/authStore';
import ToastContainer from '@/components/ui/ToastContainer';
import {
  consumeNavigationDestinationFocus,
  requestNavigationDestinationFocus,
} from '@/utils/navigationFocus';
import { AdminMutationBoundaryContext, type AdminMutationBoundary } from './AdminMutationBoundary';
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
  { label: '결제 운영', path: '/admin/payments' },
  { label: '화이트리스트', path: '/admin/whitelist-channels' },
  { label: '라이선스 관리', path: '/admin/licenses' },
  { label: '문의 관리', path: '/admin/questions' },
  { label: '기업 인증', path: '/admin/company-certifications' },
  { label: '태그 관리', path: '/admin/tags' },
  { label: '공지사항', path: '/admin/notices/new' },
  { label: '사이트 설정', path: '/admin/settings' },
];

const MOBILE_SIDEBAR_ID = 'admin-mobile-sidebar';
const MOBILE_SIDEBAR_TOGGLE_ID = 'admin-mobile-sidebar-toggle';
const MOBILE_SIDEBAR_MEDIA_QUERY = '(max-width: 767px)';
const DRAWER_FOCUSABLE_SELECTOR = [
  'a[href]',
  'button:not([disabled])',
  'input:not([disabled])',
  'select:not([disabled])',
  'textarea:not([disabled])',
  '[tabindex]:not([tabindex="-1"])',
].join(',');

function isActive(currentPath: string, menuPath: string): boolean {
  // Exact match for specific paths to avoid false positives
  if (menuPath === '/admin/albums') {
    return currentPath === '/admin/albums' || currentPath.startsWith('/admin/albums/');
  }
  if (menuPath === '/admin/tracks/upload') {
    return currentPath === '/admin/tracks/upload';
  }
  if (menuPath === '/admin/notices/new') {
    return currentPath === '/admin/notices/new' || currentPath.startsWith('/admin/notices/');
  }
  return currentPath === menuPath || currentPath.startsWith(menuPath + '/');
}

function SidebarContent({
  currentPath,
  onNavigate,
}: {
  currentPath: string;
  onNavigate?: (event: React.MouseEvent<HTMLAnchorElement>) => void;
}) {
  return (
    <>
      <div className={styles.sidebarHeader}>
        <Link to="/admin/dashboard" className={styles.logo} onClick={onNavigate}>
          AT.M
        </Link>
        <span className={styles.logoSub}>Admin</span>
      </div>

      <nav className={styles.sidebarNav}>
        {MENU_ITEMS.map((item) => (
          <Link
            key={item.path}
            to={item.path}
            onClick={onNavigate}
            className={`${styles.navItem} ${
              isActive(currentPath, item.path) ? styles.navItemActive : ''
            }`}
          >
            {item.label}
          </Link>
        ))}
      </nav>

      <div className={styles.sidebarFooter}>
        <div className={styles.navDivider} />
        <Link to="/" className={styles.backLink} onClick={onNavigate}>
          {'< '}
          {'사이트로 돌아가기'}
        </Link>
      </div>
    </>
  );
}

export default function AdminLayout() {
  const location = useLocation();
  const navigate = useNavigate();
  const user = useAuthStore((s) => s.user);
  const logout = useAuthStore((s) => s.logout);
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [isMobileViewport, setIsMobileViewport] = useState<boolean | null>(() => {
    if (typeof window === 'undefined' || typeof window.matchMedia !== 'function') return null;
    return window.matchMedia(MOBILE_SIDEBAR_MEDIA_QUERY).matches;
  });
  const mobileSidebarRef = useRef<HTMLElement>(null);
  const menuToggleRef = useRef<HTMLButtonElement>(null);
  const mutationOwnersRef = useRef(new Set<object>());
  const [activeMutationOwnerCount, setActiveMutationOwnerCount] = useState(0);
  const mountedRef = useRef(true);
  const mutationBoundary = useMemo<AdminMutationBoundary>(
    () => ({
      acquire(owner) {
        const owners = mutationOwnersRef.current;
        if (owners.has(owner)) return;
        owners.add(owner);
        if (mountedRef.current) setActiveMutationOwnerCount(owners.size);
      },
      release(owner) {
        const owners = mutationOwnersRef.current;
        if (!owners.delete(owner)) return;
        if (mountedRef.current) setActiveMutationOwnerCount(owners.size);
      },
      hasActiveOwner() {
        return mutationOwnersRef.current.size > 0;
      },
    }),
    [],
  );

  const closeSidebar = useCallback((restoreFocus: boolean) => {
    setSidebarOpen(false);
    if (!restoreFocus) return;

    const opener = menuToggleRef.current;
    if (
      opener?.isConnected &&
      !opener.disabled &&
      opener.getAttribute('aria-disabled') !== 'true' &&
      !opener.closest('[hidden], [aria-hidden="true"], [inert]')
    ) {
      opener.focus();
    }
  }, []);

  const mobileDrawerOpen = sidebarOpen && isMobileViewport !== false;

  useEffect(() => {
    mountedRef.current = true;
    return () => {
      mountedRef.current = false;
    };
  }, []);

  // Close mobile sidebar on route change
  useEffect(() => {
    setSidebarOpen(false);
  }, [location.pathname]);

  useEffect(() => {
    consumeNavigationDestinationFocus();
  }, [location.key]);

  useEffect(() => {
    if (typeof window === 'undefined' || typeof window.matchMedia !== 'function') return;

    const mediaQueryList = window.matchMedia(MOBILE_SIDEBAR_MEDIA_QUERY);
    const handleMediaChange = (event: MediaQueryListEvent) => {
      setIsMobileViewport(event.matches);
      if (!event.matches) closeSidebar(false);
    };

    setIsMobileViewport(mediaQueryList.matches);
    if (typeof mediaQueryList.addEventListener === 'function') {
      mediaQueryList.addEventListener('change', handleMediaChange);
      return () => mediaQueryList.removeEventListener('change', handleMediaChange);
    }

    mediaQueryList.addListener(handleMediaChange);
    return () => mediaQueryList.removeListener(handleMediaChange);
  }, [closeSidebar]);

  useEffect(() => {
    if (!mobileDrawerOpen) return;

    const drawer = mobileSidebarRef.current;
    if (!drawer) return;

    const getFocusableElements = (container: HTMLElement) =>
      Array.from(container.querySelectorAll<HTMLElement>(DRAWER_FOCUSABLE_SELECTOR));
    getFocusableElements(drawer)[0]?.focus();

    function handleDrawerKeyDown(event: KeyboardEvent) {
      if (event.key === 'Escape') {
        event.preventDefault();
        closeSidebar(true);
        return;
      }

      if (event.key !== 'Tab') return;

      const currentDrawer = mobileSidebarRef.current;
      if (!currentDrawer) return;

      const focusableElements = getFocusableElements(currentDrawer);
      const firstElement = focusableElements[0];
      const lastElement = focusableElements[focusableElements.length - 1];
      if (!firstElement || !lastElement) return;

      const activeElement = document.activeElement;
      if (
        event.shiftKey &&
        (activeElement === firstElement || !currentDrawer.contains(activeElement))
      ) {
        event.preventDefault();
        lastElement.focus();
      } else if (
        !event.shiftKey &&
        (activeElement === lastElement || !currentDrawer.contains(activeElement))
      ) {
        event.preventDefault();
        firstElement.focus();
      }
    }

    document.addEventListener('keydown', handleDrawerKeyDown);
    return () => document.removeEventListener('keydown', handleDrawerKeyDown);
  }, [closeSidebar, mobileDrawerOpen]);

  function beginNavigationFocus() {
    requestNavigationDestinationFocus();
    closeSidebar(false);
  }

  function handleMobileNavigation(event: React.MouseEvent<HTMLAnchorElement>) {
    if (
      event.defaultPrevented ||
      event.button !== 0 ||
      event.metaKey ||
      event.ctrlKey ||
      event.shiftKey ||
      event.altKey
    ) {
      return;
    }
    beginNavigationFocus();
  }

  function handleLogout() {
    if (mutationBoundary.hasActiveOwner()) return;
    logout();
    navigate('/', { replace: true });
  }

  const backgroundIsolationProps = mobileDrawerOpen ? { 'aria-hidden': true, inert: '' } : {};

  return (
    <AdminMutationBoundaryContext.Provider value={mutationBoundary}>
      <div className={styles.layout}>
        <aside className={`${styles.sidebar} ${styles.desktopSidebar}`}>
          <SidebarContent currentPath={location.pathname} />
        </aside>

        {mobileDrawerOpen && (
          <>
            <div className={styles.overlay} aria-hidden="true" onClick={() => closeSidebar(true)} />
            <aside
              ref={mobileSidebarRef}
              id={MOBILE_SIDEBAR_ID}
              className={`${styles.sidebar} ${styles.mobileSidebar}`}
              role="dialog"
              aria-modal="true"
              aria-label="Admin navigation"
            >
              <SidebarContent currentPath={location.pathname} onNavigate={handleMobileNavigation} />
            </aside>
          </>
        )}

        {/* Top bar */}
        <header className={styles.topbar}>
          {/* Mobile hamburger (hidden on desktop via CSS) */}
          <button
            ref={menuToggleRef}
            id={MOBILE_SIDEBAR_TOGGLE_ID}
            className={styles.menuToggle}
            onClick={() =>
              setSidebarOpen((isOpen) => (isMobileViewport === false ? false : !isOpen))
            }
            aria-label={mobileDrawerOpen ? 'Close menu' : 'Open menu'}
            aria-controls={MOBILE_SIDEBAR_ID}
            aria-expanded={mobileDrawerOpen}
          >
            {mobileDrawerOpen ? '\u2715' : '\u2630'}
          </button>

          <div className={styles.topbarContent} {...backgroundIsolationProps}>
            <div className={styles.userInfo}>
              {user && (
                <span>
                  <span className={styles.userName}>{user.nickname}</span>
                  {' (Admin)'}
                </span>
              )}
            </div>
            <button
              className={styles.logoutBtn}
              onClick={handleLogout}
              disabled={activeMutationOwnerCount > 0}
            >
              {'로그아웃'}
            </button>
          </div>
        </header>

        {/* Main content */}
        <main className={styles.content} {...backgroundIsolationProps}>
          <Outlet />
        </main>

        <div className={styles.toastBoundary} {...backgroundIsolationProps}>
          <ToastContainer />
        </div>
      </div>
    </AdminMutationBoundaryContext.Provider>
  );
}

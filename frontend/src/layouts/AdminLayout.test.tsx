import { act, fireEvent, render, screen, waitFor } from '@testing-library/react';
import { type ReactNode, useContext, useRef } from 'react';
import { MemoryRouter, Route, Routes, useLocation } from 'react-router-dom';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

import AdminLayout from './AdminLayout';
import { AdminMutationBoundaryContext } from './AdminMutationBoundary';
import { useToastStore } from '@/store/toastStore';
import styles from './AdminLayout.module.css';

const auth = vi.hoisted(() => ({
  user: { nickname: 'Operator' },
  logout: vi.fn(),
}));

vi.mock('@/store/authStore', () => ({
  useAuthStore: (selector: (state: typeof auth) => unknown) => selector(auth),
  UNCONFIRMED_LOGOUT_WARNING:
    '서버 로그아웃 확인에 실패했습니다. 이 기기에서는 로그아웃되었습니다.',
}));

const originalMatchMedia = window.matchMedia;
const originalRequestAnimationFrame = window.requestAnimationFrame;

function installMatchMedia(initialMatches: boolean) {
  const listeners = new Set<(event: MediaQueryListEvent) => void>();
  let matches = initialMatches;
  const media = '(max-width: 767px)';
  const mediaQueryList = {
    get matches() {
      return matches;
    },
    media,
    onchange: null,
    addEventListener: vi.fn((_type: string, listener: (event: MediaQueryListEvent) => void) => {
      listeners.add(listener);
    }),
    removeEventListener: vi.fn((_type: string, listener: (event: MediaQueryListEvent) => void) => {
      listeners.delete(listener);
    }),
    addListener: vi.fn((listener: (event: MediaQueryListEvent) => void) => listeners.add(listener)),
    removeListener: vi.fn((listener: (event: MediaQueryListEvent) => void) =>
      listeners.delete(listener),
    ),
    dispatchEvent: vi.fn(),
  } as unknown as MediaQueryList;

  Object.defineProperty(window, 'matchMedia', {
    configurable: true,
    value: vi.fn(() => mediaQueryList),
  });

  return {
    setMatches(nextMatches: boolean) {
      matches = nextMatches;
      const event = { matches, media } as MediaQueryListEvent;
      listeners.forEach((listener) => listener(event));
    },
  };
}

function AdminDestinationHeading() {
  const location = useLocation();
  const name = location.pathname === '/admin/users' ? '사용자 관리' : '앨범 관리';

  return <h1>{name}</h1>;
}

function MutationOwnerHarness() {
  const mutationBoundary = useContext(AdminMutationBoundaryContext);
  const ownerRef = useRef({});

  if (!mutationBoundary) throw new Error('Admin mutation boundary is required.');

  return (
    <>
      <div>admin-page</div>
      <button type="button" onClick={() => mutationBoundary.acquire(ownerRef.current)}>
        Start mutation
      </button>
      <button type="button" onClick={() => mutationBoundary.release(ownerRef.current)}>
        Finish mutation
      </button>
    </>
  );
}

function renderAdmin(content: ReactNode = <AdminDestinationHeading />) {
  return render(
    <MemoryRouter initialEntries={['/admin/albums']}>
      <Routes>
        <Route element={<AdminLayout />}>
          <Route path="/admin/*" element={content} />
        </Route>
        <Route path="/" element={<div>home-page</div>} />
      </Routes>
    </MemoryRouter>,
  );
}

function openDrawer() {
  const opener = document.querySelector<HTMLButtonElement>('button[aria-label="Open menu"]')!;
  fireEvent.click(opener);
  return { opener, drawer: document.querySelector<HTMLElement>('aside[role="dialog"]')! };
}

describe('AdminLayout mobile drawer', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    auth.logout.mockResolvedValue({ serverConfirmed: true });
    useToastStore.setState({ toasts: [] });
    Object.defineProperty(window, 'requestAnimationFrame', {
      configurable: true,
      value: vi.fn((callback: FrameRequestCallback) => {
        callback(0);
        return 1;
      }),
    });
  });

  afterEach(() => {
    Object.defineProperty(window, 'matchMedia', {
      configurable: true,
      value: originalMatchMedia,
    });
    Object.defineProperty(window, 'requestAnimationFrame', {
      configurable: true,
      value: originalRequestAnimationFrame,
    });
  });

  it('keeps the desktop sidebar mounted and omits the closed mobile drawer', () => {
    const { container } = renderAdmin();
    const desktopSidebar = container.querySelector('aside:not([role="dialog"])');
    const opener = container.querySelector<HTMLButtonElement>('button[aria-label="Open menu"]')!;

    expect(desktopSidebar).toBeInTheDocument();
    expect(desktopSidebar?.className).toContain('desktopSidebar');
    expect(container.querySelector('aside[role="dialog"]')).not.toBeInTheDocument();
    expect(opener).toHaveAttribute('id', 'admin-mobile-sidebar-toggle');
    expect(opener).toHaveAttribute('aria-controls', 'admin-mobile-sidebar');
    expect(opener).toHaveAttribute('aria-expanded', 'false');
  });

  it('relates and focuses the drawer while isolating background content', async () => {
    renderAdmin();
    const { opener, drawer } = openDrawer();
    const drawerLinks = Array.from(drawer.querySelectorAll<HTMLAnchorElement>('a[href]'));
    const main = screen.getByRole('main', { hidden: true });
    const topbarContent = screen.getByText('Operator').closest('div')?.parentElement;
    const overlay = drawer.previousElementSibling;

    expect(drawer).toHaveAttribute('id', 'admin-mobile-sidebar');
    expect(drawer).toHaveAttribute('aria-modal', 'true');
    expect(opener).toHaveAttribute('aria-expanded', 'true');
    await waitFor(() => expect(drawerLinks[0]).toHaveFocus());
    expect(main).toHaveAttribute('inert');
    expect(main).toHaveAttribute('aria-hidden', 'true');
    expect(topbarContent).toHaveAttribute('inert');
    expect(topbarContent).toHaveAttribute('aria-hidden', 'true');
    expect(opener.closest('[inert]')).toBeNull();
    expect(opener.closest('[aria-hidden="true"]')).toBeNull();
    expect(overlay).toHaveAttribute('aria-hidden', 'true');
  });

  it('isolates the real toast dismiss control while keeping the drawer opener usable', async () => {
    useToastStore.setState({
      toasts: [{ id: 57, type: 'info', message: '운영 알림' }],
    });
    renderAdmin();
    const dismiss = screen.getByRole('button', { name: '운영 알림 알림 닫기' });
    const { opener, drawer } = openDrawer();

    await waitFor(() => expect(drawer.querySelector('a[href]')).toHaveFocus());
    expect(dismiss.closest('[inert]')).toHaveAttribute('aria-hidden', 'true');
    expect(opener.closest('[inert]')).toBeNull();
    expect(opener).toBeEnabled();

    fireEvent.click(drawer.previousElementSibling!);
    expect(dismiss.closest('[inert]')).toBeNull();
    fireEvent.click(dismiss);
    expect(screen.queryByRole('button', { name: '운영 알림 알림 닫기' })).not.toBeInTheDocument();
  });

  it('traps forward and reverse Tab within the drawer', async () => {
    renderAdmin();
    const { opener, drawer } = openDrawer();
    const drawerLinks = Array.from(drawer.querySelectorAll<HTMLAnchorElement>('a[href]'));
    const firstLink = drawerLinks[0];
    const lastLink = drawerLinks[drawerLinks.length - 1];

    await waitFor(() => expect(firstLink).toHaveFocus());
    fireEvent.keyDown(document, { key: 'Tab', shiftKey: true });
    expect(lastLink).toHaveFocus();

    fireEvent.keyDown(document, { key: 'Tab' });
    expect(firstLink).toHaveFocus();

    opener.focus();
    fireEvent.keyDown(document, { key: 'Tab' });
    expect(firstLink).toHaveFocus();
  });

  it('fully releases the mobile drawer when the viewport transitions to desktop', async () => {
    const media = installMatchMedia(true);
    useToastStore.setState({
      toasts: [{ id: 58, type: 'info', message: '반응형 알림' }],
    });
    const { container } = renderAdmin();
    const desktopSidebar = container.querySelector<HTMLElement>(`aside.${styles.desktopSidebar}`);
    const desktopActiveLink =
      desktopSidebar?.querySelector<HTMLAnchorElement>('a[href="/admin/albums"]');
    const activeClassName = desktopActiveLink?.className;
    const { opener, drawer } = openDrawer();

    await waitFor(() => expect(drawer.querySelector('a[href]')).toHaveFocus());
    expect(screen.getByRole('main', { hidden: true })).toHaveAttribute('inert');

    act(() => media.setMatches(false));

    expect(container.querySelector('aside[role="dialog"]')).not.toBeInTheDocument();
    expect(container.querySelector(`.${styles.overlay}`)).not.toBeInTheDocument();
    expect(screen.getByRole('main')).not.toHaveAttribute('inert');
    expect(screen.getByRole('main')).not.toHaveAttribute('aria-hidden');
    const topbarContent = screen.getByText('Operator').closest(`.${styles.topbarContent}`);
    const toastBoundary = container.querySelector(`.${styles.toastBoundary}`);
    expect(topbarContent).not.toHaveAttribute('inert');
    expect(topbarContent).not.toHaveAttribute('aria-hidden');
    expect(toastBoundary).not.toHaveAttribute('inert');
    expect(toastBoundary).not.toHaveAttribute('aria-hidden');
    expect(opener).not.toHaveFocus();

    const tabEvent = new KeyboardEvent('keydown', {
      key: 'Tab',
      bubbles: true,
      cancelable: true,
    });
    document.dispatchEvent(tabEvent);
    expect(tabEvent.defaultPrevented).toBe(false);
    expect(container.querySelector(`aside.${styles.desktopSidebar}`)).toBe(desktopSidebar);
    expect(desktopActiveLink).toHaveAttribute('href', '/admin/albums');
    expect(desktopActiveLink?.className).toBe(activeClassName);
  });

  it('closes on Escape and restores the exact opener', async () => {
    renderAdmin();
    const { opener, drawer } = openDrawer();
    const drawerLinks = Array.from(drawer.querySelectorAll<HTMLAnchorElement>('a[href]'));

    await waitFor(() => expect(drawerLinks[0]).toHaveFocus());
    drawerLinks[2].focus();
    fireEvent.keyDown(document, { key: 'Escape' });

    expect(document.querySelector('aside[role="dialog"]')).not.toBeInTheDocument();
    expect(opener).toHaveAttribute('aria-expanded', 'false');
    expect(opener).toHaveFocus();
  });

  it('focuses a different-path destination and restores focus after overlay activation', async () => {
    renderAdmin();
    const firstOpen = openDrawer();
    const usersLink = Array.from(
      firstOpen.drawer.querySelectorAll<HTMLAnchorElement>('a[href]'),
    ).find((link) => link.getAttribute('href') === '/admin/users');

    expect(usersLink).toBeDefined();
    usersLink!.focus();
    fireEvent.click(usersLink!);
    await waitFor(() =>
      expect(document.querySelector('aside[role="dialog"]')).not.toBeInTheDocument(),
    );
    expect(firstOpen.opener).toHaveAttribute('aria-expanded', 'false');
    expect(firstOpen.opener).not.toHaveFocus();
    const destinationHeading = screen.getByRole('heading', { level: 1, name: '사용자 관리' });
    await waitFor(() => expect(destinationHeading).toHaveFocus());
    expect(destinationHeading).not.toHaveAttribute('tabindex');

    const secondOpen = openDrawer();
    const overlay = secondOpen.drawer.previousElementSibling;
    const drawerLink = secondOpen.drawer.querySelector<HTMLAnchorElement>('a[href]')!;
    await waitFor(() => expect(drawerLink).toHaveFocus());
    expect(overlay).toHaveAttribute('aria-hidden', 'true');
    fireEvent.click(overlay!);

    expect(document.querySelector('aside[role="dialog"]')).not.toBeInTheDocument();
    expect(secondOpen.opener).toHaveAttribute('aria-expanded', 'false');
    expect(secondOpen.opener).toHaveFocus();
  });

  it('closes and focuses the destination after an exact same-path navigation command', async () => {
    renderAdmin();
    const { drawer } = openDrawer();
    const albumsLink = drawer.querySelector<HTMLAnchorElement>('a[href="/admin/albums"]')!;

    albumsLink.focus();
    fireEvent.click(albumsLink);

    expect(document.querySelector('aside[role="dialog"]')).not.toBeInTheDocument();
    const destinationHeading = screen.getByRole('heading', { level: 1, name: '앨범 관리' });
    await waitFor(() => expect(destinationHeading).toHaveFocus());
    expect(destinationHeading).not.toHaveAttribute('tabindex');
  });

  it('preserves mutation-owned logout blocking and releases normal logout', async () => {
    renderAdmin(<MutationOwnerHarness />);
    fireEvent.click(screen.getByRole('button', { name: 'Start mutation' }));

    const logoutButton = screen.getByRole('button', { name: '로그아웃' });
    expect(logoutButton).toBeDisabled();
    logoutButton.removeAttribute('disabled');
    fireEvent.click(logoutButton);
    expect(auth.logout).not.toHaveBeenCalled();
    expect(screen.queryByText('home-page')).not.toBeInTheDocument();

    fireEvent.click(screen.getByRole('button', { name: 'Finish mutation' }));
    expect(screen.getByRole('button', { name: '로그아웃' })).toBeEnabled();
    fireEvent.click(screen.getByRole('button', { name: '로그아웃' }));

    expect(auth.logout).toHaveBeenCalledTimes(1);
    expect(await screen.findByText('home-page')).toBeInTheDocument();
  });

  it('awaits logout settlement, blocks duplicates, and warns on an unconfirmed server result', async () => {
    let resolveLogout!: (outcome: { serverConfirmed: boolean }) => void;
    auth.logout.mockImplementation(
      () =>
        new Promise<{ serverConfirmed: boolean }>((resolve) => {
          resolveLogout = resolve;
        }),
    );
    renderAdmin();

    const logoutButton = screen.getByRole('button', { name: '로그아웃' });
    fireEvent.click(logoutButton);

    expect(auth.logout).toHaveBeenCalledTimes(1);
    expect(logoutButton).toBeDisabled();
    fireEvent.click(logoutButton);
    expect(auth.logout).toHaveBeenCalledTimes(1);
    expect(screen.queryByText('home-page')).not.toBeInTheDocument();

    resolveLogout({ serverConfirmed: false });

    expect(await screen.findByRole('status')).toHaveTextContent(
      '서버 로그아웃 확인에 실패했습니다. 이 기기에서는 로그아웃되었습니다.',
    );
    expect(await screen.findByText('home-page')).toBeInTheDocument();
  });
});

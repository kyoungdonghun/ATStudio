import { fireEvent, render, screen, waitFor, within } from '@testing-library/react';
import { lazy, StrictMode, Suspense } from 'react';
import { Link, MemoryRouter, Route, Routes } from 'react-router-dom';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

import { useAuthStore } from '@/store/authStore';
import AdminLayout from './AdminLayout';
import MainLayout from './MainLayout';

vi.mock('@/layouts/PlayerBar', () => ({ default: () => null }));

const originalMatchMedia = window.matchMedia;
const originalRequestAnimationFrame = window.requestAnimationFrame;
const LazyPublicHome = lazy(
  () => new Promise<{ default: () => React.ReactElement }>(() => undefined),
);

function renderLayoutBranches(initialEntry: string) {
  return render(
    <StrictMode>
      <MemoryRouter initialEntries={[initialEntry]}>
        <Link to="/albums">Unrelated route</Link>
        <Routes>
          <Route element={<MainLayout />}>
            <Route
              path="/"
              element={
                <Suspense fallback={<div>Loading public home</div>}>
                  <LazyPublicHome />
                </Suspense>
              }
            />
            <Route path="/tracks" element={<h1>Tracks</h1>} />
            <Route path="/albums" element={<h1>Albums</h1>} />
          </Route>
          <Route path="/admin" element={<AdminLayout />}>
            <Route path="dashboard" element={<h1>Admin dashboard</h1>} />
          </Route>
        </Routes>
      </MemoryRouter>
    </StrictMode>,
  );
}

function openHeaderMenu() {
  const opener = screen.getByLabelText('메뉴 열기');
  fireEvent.click(opener);
  const menu = document.getElementById(opener.getAttribute('aria-controls') ?? '');

  expect(menu).toBeInTheDocument();
  return { opener, menu: menu as HTMLElement };
}

function openAdminDrawer() {
  const opener = document.querySelector<HTMLButtonElement>('button[aria-label="Open menu"]')!;
  fireEvent.click(opener);
  const drawer = document.querySelector<HTMLElement>('aside[role="dialog"]')!;

  return { opener, drawer };
}

describe('cross-layout navigation focus', () => {
  beforeEach(() => {
    useAuthStore.setState({
      accessToken: 'test-access-token',
      role: 'ADMIN',
      user: { nickname: 'Operator' } as NonNullable<
        ReturnType<typeof useAuthStore.getState>['user']
      >,
    });
    Object.defineProperty(window, 'matchMedia', {
      configurable: true,
      value: vi.fn(() => ({
        matches: true,
        media: '(max-width: 767px)',
        onchange: null,
        addEventListener: vi.fn(),
        removeEventListener: vi.fn(),
        addListener: vi.fn(),
        removeListener: vi.fn(),
        dispatchEvent: vi.fn(),
      })),
    });
    Object.defineProperty(window, 'requestAnimationFrame', {
      configurable: true,
      value: vi.fn(() => 1),
    });
  });

  afterEach(() => {
    useAuthStore.setState({ accessToken: null, user: null, role: 'GUEST' });
    Object.defineProperty(window, 'matchMedia', {
      configurable: true,
      value: originalMatchMedia,
    });
    Object.defineProperty(window, 'requestAnimationFrame', {
      configurable: true,
      value: originalRequestAnimationFrame,
    });
  });

  it('keeps Header navigation focus intent through the MainLayout to AdminLayout switch', async () => {
    renderLayoutBranches('/');
    const { opener, menu } = openHeaderMenu();
    const adminLink = within(menu).getByText('관리자');

    adminLink.focus();
    fireEvent.click(adminLink);

    expect(document.getElementById('header-mobile-menu')).not.toBeInTheDocument();
    expect(opener).not.toBe(document.activeElement);
    const heading = await screen.findByRole('heading', { level: 1, name: 'Admin dashboard' });
    await waitFor(() => expect(heading).toHaveFocus());
    expect(heading).not.toHaveAttribute('tabindex');
    expect(window.requestAnimationFrame).not.toHaveBeenCalled();
  });

  it('keeps Admin navigation focus intent through the AdminLayout to MainLayout switch', async () => {
    renderLayoutBranches('/admin/dashboard');
    const { opener, drawer } = openAdminDrawer();
    const publicSiteLink = drawer.querySelector<HTMLAnchorElement>('a[href="/"]')!;

    publicSiteLink.focus();
    fireEvent.click(publicSiteLink);

    expect(document.querySelector('aside[role="dialog"]')).not.toBeInTheDocument();
    expect(opener).not.toBe(document.activeElement);
    expect(await screen.findByText('Loading public home')).toBeInTheDocument();
    const main = screen.getByRole('main');
    await waitFor(() => expect(main).toHaveFocus());
    expect(main).not.toHaveAttribute('tabindex');
    expect(window.requestAnimationFrame).not.toHaveBeenCalled();
  });

  it('does not reuse a consumed intent for a later unrelated route change', async () => {
    renderLayoutBranches('/');
    const { menu } = openHeaderMenu();

    fireEvent.click(within(menu).getByText('음원'));
    const tracksHeading = await screen.findByRole('heading', { level: 1, name: 'Tracks' });
    await waitFor(() => expect(tracksHeading).toHaveFocus());

    const unrelatedRoute = screen.getByRole('link', { name: 'Unrelated route' });
    unrelatedRoute.focus();
    fireEvent.click(unrelatedRoute);

    const albumsHeading = await screen.findByRole('heading', { level: 1, name: 'Albums' });
    expect(unrelatedRoute).toHaveFocus();
    expect(albumsHeading).not.toHaveFocus();
    expect(albumsHeading).not.toHaveAttribute('tabindex');
    expect(window.requestAnimationFrame).not.toHaveBeenCalled();
  });
});

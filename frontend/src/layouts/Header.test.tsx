import { fireEvent, render, screen, waitFor, within } from '@testing-library/react';
import { MemoryRouter, useLocation } from 'react-router-dom';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import Header from '@/layouts/Header';
import buttonStyles from '@/components/ui/Button.module.css';
import ToastContainer from '@/components/ui/ToastContainer';
import { useAuthStore } from '@/store/authStore';
import { useThemeStore } from '@/store/themeStore';
import { useToastStore } from '@/store/toastStore';
import styles from './Header.module.css';

const originalRequestAnimationFrame = window.requestAnimationFrame;
const defaultLogout = useAuthStore.getState().logout;

function DestinationHeading() {
  const location = useLocation();
  const name = location.pathname === '/tracks' ? '음원 페이지' : '홈 페이지';

  return (
    <main>
      <h1>{name}</h1>
    </main>
  );
}

function renderHeader(initialEntry = '/') {
  return render(
    <MemoryRouter initialEntries={[initialEntry]}>
      <Header />
      <DestinationHeading />
      <ToastContainer />
    </MemoryRouter>,
  );
}

describe('Header accessibility', () => {
  beforeEach(() => {
    useAuthStore.setState({ accessToken: null, user: null, role: 'GUEST', logout: defaultLogout });
    useThemeStore.setState({ theme: 'dark' });
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
    Object.defineProperty(window, 'requestAnimationFrame', {
      configurable: true,
      value: originalRequestAnimationFrame,
    });
  });

  it('provides explicit labels for desktop and mobile search', () => {
    renderHeader();

    const desktopSearch = screen.getByRole('search', { name: '곡 검색' });
    fireEvent.click(screen.getByLabelText('메뉴 열기'));
    const mobileSearchInput = screen.getByLabelText('모바일 곡 제목 및 용도 검색');

    const desktopSearchInput = within(desktopSearch).getByRole('textbox', {
      name: '곡 제목 및 용도 검색',
    });

    expect(desktopSearchInput).toBe(screen.getByLabelText('곡 제목 및 용도 검색'));
    expect(desktopSearchInput).toHaveAttribute('placeholder', '곡 제목, 용도 검색');
    expect(mobileSearchInput).toHaveAttribute('type', 'text');
    expect(mobileSearchInput).toHaveAttribute('placeholder', '곡 제목, 용도 검색');
    expect(mobileSearchInput.closest('form')).toHaveAttribute('aria-label', '모바일 곡 검색');
  });

  it('keeps closed mobile controls out of the tree and restores the hamburger after Escape', () => {
    renderHeader();
    const opener = screen.getByLabelText('메뉴 열기');
    const menuId = opener.getAttribute('aria-controls');

    expect(menuId).toBeTruthy();
    expect(opener).toHaveAttribute('aria-expanded', 'false');
    expect(screen.queryByRole('search', { name: '모바일 곡 검색' })).not.toBeInTheDocument();
    expect(menuId && document.getElementById(menuId)).not.toBeInTheDocument();

    fireEvent.click(opener);

    expect(opener).toHaveAttribute('aria-expanded', 'true');
    expect(menuId && document.getElementById(menuId)).toContainElement(
      screen.getByLabelText('모바일 곡 검색'),
    );

    fireEvent.keyDown(window, { key: 'Escape' });

    expect(screen.queryByRole('search', { name: '모바일 곡 검색' })).not.toBeInTheDocument();
    expect(menuId && document.getElementById(menuId)).not.toBeInTheDocument();
    expect(opener).toHaveFocus();
  });

  it('restores the exact account opener after Escape', () => {
    useAuthStore.setState({
      accessToken: 'test-access-token',
      role: 'USER',
      user: { nickname: '테스터' } as NonNullable<ReturnType<typeof useAuthStore.getState>['user']>,
    });
    renderHeader();
    const accountOpener = screen.getByLabelText('계정 메뉴');
    const hamburger = screen.getByLabelText('메뉴 열기');

    expect(accountOpener).toHaveAttribute('aria-controls', hamburger.getAttribute('aria-controls'));
    expect(accountOpener).toHaveAttribute('aria-expanded', 'false');

    fireEvent.click(accountOpener);
    fireEvent.keyDown(window, { key: 'Escape' });

    expect(accountOpener).toHaveFocus();
    expect(hamburger).not.toHaveFocus();
  });

  it('restores the opener after overlay close and focuses a different-path destination', async () => {
    const { container } = renderHeader();
    const openMenu = () => fireEvent.click(screen.getByLabelText('메뉴 열기'));

    openMenu();
    const opener = screen.getByLabelText('메뉴 닫기');
    screen.getByLabelText('모바일 곡 제목 및 용도 검색').focus();
    const overlay = container.querySelector(`.${styles.mobileOverlay}`);
    expect(overlay).toBeInTheDocument();
    fireEvent.click(overlay as Element);
    expect(screen.queryByRole('search', { name: '모바일 곡 검색' })).not.toBeInTheDocument();
    expect(opener).toHaveFocus();

    openMenu();
    const mobileMenu = document.getElementById(
      screen.getByLabelText('메뉴 닫기').getAttribute('aria-controls') ?? '',
    );
    const routeLink = within(mobileMenu as HTMLElement).getByText('음원');
    routeLink.focus();
    fireEvent.click(routeLink);
    expect(screen.queryByRole('search', { name: '모바일 곡 검색' })).not.toBeInTheDocument();
    expect(opener).not.toHaveFocus();
    const destinationHeading = await screen.findByRole('heading', {
      level: 1,
      name: '음원 페이지',
    });
    await waitFor(() => expect(destinationHeading).toHaveFocus());
    expect(destinationHeading).not.toHaveAttribute('tabindex');
  });

  it('closes and focuses the destination after an exact same-path navigation command', async () => {
    renderHeader('/tracks');
    fireEvent.click(screen.getByLabelText('메뉴 열기'));
    const mobileMenu = document.getElementById(
      screen.getByLabelText('메뉴 닫기').getAttribute('aria-controls') ?? '',
    );
    const routeLink = within(mobileMenu as HTMLElement).getByText('음원');

    routeLink.focus();
    fireEvent.click(routeLink);

    expect(screen.queryByRole('search', { name: '모바일 곡 검색' })).not.toBeInTheDocument();
    const destinationHeading = screen.getByRole('heading', {
      level: 1,
      name: '음원 페이지',
    });
    await waitFor(() => expect(destinationHeading).toHaveFocus());
    expect(destinationHeading).not.toHaveAttribute('tabindex');
  });

  it('focuses the destination after mobile search navigation', async () => {
    renderHeader();
    fireEvent.click(screen.getByLabelText('메뉴 열기'));
    const searchInput = screen.getByLabelText('모바일 곡 제목 및 용도 검색');

    fireEvent.change(searchInput, { target: { value: 'focus contract' } });
    fireEvent.submit(searchInput.closest('form')!);

    expect(screen.queryByRole('search', { name: '모바일 곡 검색' })).not.toBeInTheDocument();
    const destinationHeading = await screen.findByRole('heading', {
      level: 1,
      name: '음원 페이지',
    });
    await waitFor(() => expect(destinationHeading).toHaveFocus());
  });

  it('uses Korean state-correct accessible names for theme toggles', () => {
    renderHeader();

    const lightModeToggles = screen.getAllByLabelText('라이트 모드로 전환');
    expect(lightModeToggles).toHaveLength(2);
    lightModeToggles.forEach((toggle) => expect(toggle).toHaveAttribute('title', '라이트 모드'));

    fireEvent.click(lightModeToggles[0]);

    const darkModeToggles = screen.getAllByLabelText('다크 모드로 전환');
    expect(darkModeToggles).toHaveLength(2);
    darkModeToggles.forEach((toggle) => expect(toggle).toHaveAttribute('title', '다크 모드'));
  });

  it('renders anonymous desktop route commands as single styled links', () => {
    renderHeader();
    const loginLink = screen.getAllByRole('link', { name: '로그인' })[0];
    const subscriptionLink = screen.getByRole('link', { name: '구독 시작하기' });

    expect(loginLink).toHaveClass(buttonStyles.button, buttonStyles.ghost, buttonStyles.md);
    expect(subscriptionLink).toHaveClass(
      buttonStyles.button,
      buttonStyles.primary,
      buttonStyles.md,
    );
    expect(within(loginLink).queryByRole('button')).not.toBeInTheDocument();
    expect(within(subscriptionLink).queryByRole('button')).not.toBeInTheDocument();
  });

  it('renders the desktop account command as one styled link', () => {
    useAuthStore.setState({
      accessToken: 'test-access-token',
      role: 'USER',
      user: { nickname: '테스터' } as NonNullable<ReturnType<typeof useAuthStore.getState>['user']>,
    });
    renderHeader();
    const accountLink = screen.getByRole('link', { name: '내 계정' });

    expect(accountLink).toHaveClass(buttonStyles.button, buttonStyles.ghost, buttonStyles.md);
    expect(within(accountLink).queryByRole('button')).not.toBeInTheDocument();
  });

  it('awaits logout, blocks duplicate interaction, and warns when server revocation is unconfirmed', async () => {
    let resolveLogout!: (outcome: { serverConfirmed: boolean }) => void;
    const logout = vi.fn(
      () =>
        new Promise<{ serverConfirmed: boolean }>((resolve) => {
          resolveLogout = resolve;
        }),
    );
    useAuthStore.setState({
      accessToken: 'test-access-token',
      role: 'USER',
      user: { nickname: '테스터' } as NonNullable<ReturnType<typeof useAuthStore.getState>['user']>,
      logout,
    });
    renderHeader('/tracks');

    const logoutButton = screen.getByRole('button', { name: '로그아웃' });
    fireEvent.click(logoutButton);

    expect(logout).toHaveBeenCalledTimes(1);
    expect(logoutButton).toBeDisabled();
    fireEvent.click(logoutButton);
    expect(logout).toHaveBeenCalledTimes(1);
    expect(screen.getByRole('heading', { name: '음원 페이지' })).toBeInTheDocument();

    resolveLogout({ serverConfirmed: false });

    expect(await screen.findByRole('status')).toHaveTextContent(
      '서버 로그아웃 확인에 실패했습니다. 이 기기에서는 로그아웃되었습니다.',
    );
    expect(await screen.findByRole('heading', { name: '홈 페이지' })).toBeInTheDocument();
  });
});

import { fireEvent, render, screen, within } from '@testing-library/react';
import { MemoryRouter, useLocation } from 'react-router-dom';
import { afterEach, expect, it, vi } from 'vitest';
import { useAuthStore } from '@/store/authStore';
import Header from './Header';

vi.mock('@/config/pilotMode', () => ({ isPilotMode: true }));

function LocationHeading() {
  const { pathname, search } = useLocation();
  return (
    <main>
      <h1>{pathname + search}</h1>
    </main>
  );
}

const initialRoute = '/tracks?genre=pop&page=2';

function renderPilot() {
  return render(
    <MemoryRouter initialEntries={[initialRoute]}>
      <Header />
      <LocationHeading />
    </MemoryRouter>,
  );
}

afterEach(() => useAuthStore.getState().clearSession());

it('starts a guest keyword search from the desktop header and resets to page one', () => {
  renderPilot();
  const form = screen.getByRole('search', { name: '곡 검색' });
  const input = within(form).getByRole('textbox', { name: '곡 제목 및 용도 검색' });
  fireEvent.change(input, { target: { value: '  ' } });
  fireEvent.submit(form);
  expect(screen.getByRole('heading')).toHaveTextContent(initialRoute);
  fireEvent.change(input, { target: { value: '  BGM & piano  ' } });
  fireEvent.submit(form);
  expect(screen.getByRole('heading')).toHaveTextContent('/tracks?keyword=BGM%20%26%20piano&page=1');
  expect(input).toHaveValue('');
  expect(screen.getByRole('link', { name: '로그인' })).toBeInTheDocument();
  expect(screen.queryByRole('link', { name: '구독' })).not.toBeInTheDocument();
});

it('opens mobile search and personal navigation, restores focus on Escape and submits for a participant', () => {
  useAuthStore.setState({ role: 'USER', accessToken: 'pilot-test-access' });
  renderPilot();
  const opener = screen.getByLabelText('메뉴 열기');
  fireEvent.click(opener);
  expect(screen.getByLabelText('모바일 곡 제목 및 용도 검색')).toHaveFocus();
  fireEvent.keyDown(window, { key: 'Escape' });
  expect(opener).toHaveFocus();
  fireEvent.click(opener);
  const form = screen.getByLabelText('모바일 곡 검색');
  fireEvent.change(screen.getByLabelText('모바일 곡 제목 및 용도 검색'), {
    target: { value: '브이로그' },
  });
  const nav = within(screen.getByLabelText('내 음악 메뉴'));
  expect(nav.getByText('내 라이선스')).toHaveAttribute('href', '/licenses');
  expect(nav.getByText('좋아요')).toHaveAttribute('href', '/likes');
  expect(nav.getByText('내 재생목록')).toHaveAttribute('href', '/playlists');
  expect(screen.queryByRole('link', { name: '내 계정' })).not.toBeInTheDocument();
  expect(screen.queryByRole('link', { name: '구독 시작하기' })).not.toBeInTheDocument();
  fireEvent.submit(form);
  expect(screen.getByRole('heading')).toHaveTextContent(
    '/tracks?keyword=' + encodeURIComponent('브이로그') + '&page=1',
  );
  expect(screen.queryByRole('search', { name: '모바일 곡 검색' })).not.toBeInTheDocument();
  expect(screen.getByRole('heading')).toHaveFocus();
});

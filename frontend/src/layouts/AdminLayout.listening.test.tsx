import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { expect, it, vi } from 'vitest';

vi.mock('@/config/appMode', async (importOriginal) => ({
  ...(await importOriginal<typeof import('@/config/appMode')>()),
  isListeningMode: true,
}));
vi.mock('@/store/authStore', () => ({
  useAuthStore: (selector: (state: unknown) => unknown) =>
    selector({ user: { nickname: 'QA' }, logout: vi.fn() }),
  UNCONFIRMED_LOGOUT_WARNING: 'fixture',
}));

import AdminLayout from './AdminLayout';

it('shows tag management beside track actions without exposing the ordinary administrator menus', () => {
  render(
    <MemoryRouter initialEntries={['/admin/tags']}>
      <AdminLayout />
    </MemoryRouter>,
  );
  expect(screen.getByRole('link', { name: '태그 관리' })).toHaveAttribute('href', '/admin/tags');
  expect(screen.getByRole('link', { name: '음원 관리' })).toBeInTheDocument();
  expect(screen.getByRole('link', { name: '음원 등록' })).toBeInTheDocument();
  expect(screen.queryByRole('link', { name: '사용자 관리' })).not.toBeInTheDocument();
  expect(screen.queryByRole('link', { name: '결제 운영' })).not.toBeInTheDocument();
  expect(screen.queryByRole('link', { name: '사이트 설정' })).not.toBeInTheDocument();
});

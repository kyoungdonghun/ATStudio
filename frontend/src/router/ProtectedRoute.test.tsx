import { render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import type { UserRole } from '@/types';
import ProtectedRoute from '@/router/ProtectedRoute';

const authState: {
  role: UserRole;
  isAuthenticated: () => boolean;
} = {
  role: 'GUEST',
  isAuthenticated: () => false,
};

const showToast = vi.fn();

vi.mock('@/store/authStore', () => ({
  useAuthStore: (selector: (state: typeof authState) => unknown) => selector(authState),
}));

vi.mock('@/store/toastStore', () => ({
  useToastStore: {
    getState: () => ({ show: showToast }),
  },
}));

function renderProtected(minRole: UserRole) {
  return render(
    <MemoryRouter initialEntries={['/protected']}>
      <Routes>
        <Route
          path="/protected"
          element={
            <ProtectedRoute minRole={minRole}>
              <div>Protected Content</div>
            </ProtectedRoute>
          }
        />
        <Route path="/login" element={<div>Login Page</div>} />
        <Route path="/" element={<div>Home Page</div>} />
      </Routes>
    </MemoryRouter>,
  );
}

describe('ProtectedRoute', () => {
  beforeEach(() => {
    authState.role = 'GUEST';
    authState.isAuthenticated = () => false;
    showToast.mockReset();
  });

  it('redirects unauthenticated users to login and shows a warning toast', async () => {
    renderProtected('USER');

    expect(screen.getByText('Login Page')).toBeInTheDocument();
    await waitFor(() => {
      expect(showToast).toHaveBeenCalledWith('warning', '로그인이 필요한 기능입니다.');
    });
  });

  it('redirects insufficient-role users to home and shows an access warning', async () => {
    authState.role = 'USER';
    authState.isAuthenticated = () => true;

    renderProtected('ADMIN');

    expect(screen.getByText('Home Page')).toBeInTheDocument();
    await waitFor(() => {
      expect(showToast).toHaveBeenCalledWith('warning', '접근 권한이 없습니다.');
    });
  });

  it('renders children for authorized users', () => {
    authState.role = 'ADMIN';
    authState.isAuthenticated = () => true;

    renderProtected('ADMIN');

    expect(screen.getByText('Protected Content')).toBeInTheDocument();
    expect(showToast).not.toHaveBeenCalled();
  });
});

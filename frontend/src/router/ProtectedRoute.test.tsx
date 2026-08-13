import { render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter, Route, Routes, useLocation } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import type { UserRole, UserType } from '@/types';
import ProtectedRoute from '@/router/ProtectedRoute';

const authState: {
  role: UserRole;
  user: { userType: UserType } | null;
  isAuthenticated: () => boolean;
} = {
  role: 'GUEST',
  user: null,
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

function renderProtected(
  minRole: UserRole,
  maxRole?: UserRole,
  deniedRedirect?: string,
  requiredUserType?: UserType,
  initialEntry = '/protected',
) {
  return render(
    <MemoryRouter
      initialEntries={[initialEntry]}
      future={{ v7_startTransition: true, v7_relativeSplatPath: true }}
    >
      <Routes>
        <Route
          path="/protected"
          element={
            <ProtectedRoute
              minRole={minRole}
              maxRole={maxRole}
              deniedRedirect={deniedRedirect}
              requiredUserType={requiredUserType}
            >
              <div>Protected Content</div>
            </ProtectedRoute>
          }
        />
        <Route path="/login" element={<LocationProbe />} />
        <Route path="/" element={<div>Home Page</div>} />
        <Route path="/admin/payments" element={<div>Admin Payments Page</div>} />
      </Routes>
    </MemoryRouter>,
  );
}

function LocationProbe() {
  const location = useLocation();
  return (
    <div>
      <span>Login Page</span>
      <span>{location.search}</span>
    </div>
  );
}

describe('ProtectedRoute', () => {
  beforeEach(() => {
    authState.role = 'GUEST';
    authState.user = null;
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

  it('preserves only the protected pathname and query in the login return target', () => {
    renderProtected('USER', undefined, undefined, undefined, '/protected?tab=edit&page=2#ignored');

    expect(screen.getByText('?returnTo=%2Fprotected%3Ftab%3Dedit%26page%3D2')).toBeInTheDocument();
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

  it('redirects administrators away from user-only payment routes', async () => {
    authState.role = 'ADMIN';
    authState.isAuthenticated = () => true;

    renderProtected('USER', 'USER', '/admin/payments');

    expect(screen.getByText('Admin Payments Page')).toBeInTheDocument();
    await waitFor(() => {
      expect(showToast).toHaveBeenCalledWith('warning', '접근 권한이 없습니다.');
    });
  });

  it('renders user-only payment routes for USER', () => {
    authState.role = 'USER';
    authState.isAuthenticated = () => true;

    renderProtected('USER', 'USER', '/admin/payments');

    expect(screen.getByText('Protected Content')).toBeInTheDocument();
    expect(showToast).not.toHaveBeenCalled();
  });

  it('redirects an individual user away from a BUSINESS-only route', async () => {
    authState.role = 'USER';
    authState.user = { userType: 'INDIVIDUAL' };
    authState.isAuthenticated = () => true;

    renderProtected('USER', 'USER', '/', 'BUSINESS');

    expect(screen.getByText('Home Page')).toBeInTheDocument();
    await waitFor(() => {
      expect(showToast).toHaveBeenCalledWith('warning', '접근 권한이 없습니다.');
    });
  });

  it('renders a BUSINESS-only route for a BUSINESS user', () => {
    authState.role = 'USER';
    authState.user = { userType: 'BUSINESS' };
    authState.isAuthenticated = () => true;

    renderProtected('USER', 'USER', '/', 'BUSINESS');

    expect(screen.getByText('Protected Content')).toBeInTheDocument();
    expect(showToast).not.toHaveBeenCalled();
  });
});

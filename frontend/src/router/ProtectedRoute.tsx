import { Navigate } from 'react-router-dom';
import { type ReactNode } from 'react';
import { useAuthStore } from '@/store/authStore';
import type { UserRole } from '@/types';

const ROLE_LEVEL: Record<UserRole, number> = {
  GUEST: 0,
  USER: 1,
  ADMIN: 2,
};

interface ProtectedRouteProps {
  children: ReactNode;
  minRole: UserRole;
}

export default function ProtectedRoute({ children, minRole }: ProtectedRouteProps) {
  const role = useAuthStore((s) => s.role);
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated);

  if (!isAuthenticated()) {
    return <Navigate to="/login" replace />;
  }

  if (ROLE_LEVEL[role] < ROLE_LEVEL[minRole]) {
    return <Navigate to="/" replace />;
  }

  return <>{children}</>;
}

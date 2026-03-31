import { Navigate } from 'react-router-dom';
import { type ReactNode, useEffect, useRef } from 'react';
import { useAuthStore } from '@/store/authStore';
import { useToastStore } from '@/store/toastStore';
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
  const toastShown = useRef(false);

  const needsLogin = !isAuthenticated();
  const needsHigherRole = !needsLogin && ROLE_LEVEL[role] < ROLE_LEVEL[minRole];

  useEffect(() => {
    if (toastShown.current) return;
    if (needsLogin) {
      toastShown.current = true;
      useToastStore.getState().show('warning', '로그인이 필요한 기능입니다.');
    } else if (needsHigherRole) {
      toastShown.current = true;
      useToastStore.getState().show('warning', '접근 권한이 없습니다.');
    }
  }, [needsLogin, needsHigherRole]);

  if (needsLogin) {
    return <Navigate to="/login" replace />;
  }

  if (needsHigherRole) {
    return <Navigate to="/" replace />;
  }

  return <>{children}</>;
}

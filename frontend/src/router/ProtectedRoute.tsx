import { Navigate, useLocation } from 'react-router-dom';
import { type ReactNode, useEffect, useRef } from 'react';
import { useAuthStore } from '@/store/authStore';
import { useToastStore } from '@/store/toastStore';
import type { UserRole, UserType } from '@/types';
import { createLoginPath } from '@/utils/loginReturn';

const ROLE_LEVEL: Record<UserRole, number> = {
  GUEST: 0,
  USER: 1,
  ADMIN: 2,
};

export interface ProtectedRouteProps {
  children: ReactNode;
  minRole: UserRole;
  maxRole?: UserRole;
  requiredUserType?: UserType;
  deniedRedirect?: string;
}

export default function ProtectedRoute({
  children,
  minRole,
  maxRole,
  requiredUserType,
  deniedRedirect = '/',
}: ProtectedRouteProps) {
  const role = useAuthStore((s) => s.role);
  const userType = useAuthStore((s) => s.user?.userType);
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated);
  const location = useLocation();
  const toastShown = useRef(false);

  const needsLogin = !isAuthenticated();
  const needsHigherRole = !needsLogin && ROLE_LEVEL[role] < ROLE_LEVEL[minRole];
  const exceedsMaximumRole =
    !needsLogin && maxRole !== undefined && ROLE_LEVEL[role] > ROLE_LEVEL[maxRole];
  const hasWrongUserType =
    !needsLogin && requiredUserType !== undefined && userType !== requiredUserType;
  const accessDenied = needsHigherRole || exceedsMaximumRole || hasWrongUserType;

  useEffect(() => {
    if (toastShown.current) return;
    if (needsLogin) {
      toastShown.current = true;
      useToastStore.getState().show('warning', '로그인이 필요한 기능입니다.');
    } else if (accessDenied) {
      toastShown.current = true;
      useToastStore.getState().show('warning', '접근 권한이 없습니다.');
    }
  }, [needsLogin, accessDenied]);

  if (needsLogin) {
    return <Navigate to={createLoginPath(location)} replace />;
  }

  if (accessDenied) {
    return <Navigate to={deniedRedirect} replace />;
  }

  return <>{children}</>;
}

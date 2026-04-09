import { Navigate } from 'react-router-dom';
import { type ReactNode, useState, useEffect, useRef } from 'react';
import { useAuthStore } from '@/store/authStore';
import { useToastStore } from '@/store/toastStore';
import { fetchMySubscription } from '@/api/userSubscriptions';

interface SubscriberRouteProps {
  children: ReactNode;
}

/**
 * Route guard that requires an active subscription.
 * Redirects non-subscribers to /subscriptions with a toast notification.
 */
export default function SubscriberRoute({ children }: SubscriberRouteProps) {
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated());
  const [status, setStatus] = useState<'loading' | 'active' | 'inactive'>('loading');
  const toastShown = useRef(false);

  useEffect(() => {
    if (!isAuthenticated) {
      setStatus('inactive');
      return;
    }
    fetchMySubscription()
      .then(() => {
        // API returns subscription only if status IN ('ACTIVE','CANCELLED') AND expiresAt >= today
        // So any successful response means the user has access (including grace period)
        setStatus('active');
      })
      .catch(() => {
        setStatus('inactive');
      });
  }, [isAuthenticated]);

  if (!isAuthenticated) {
    if (!toastShown.current) {
      toastShown.current = true;
      useToastStore.getState().show('warning', '로그인이 필요한 기능입니다.');
    }
    return <Navigate to="/login" replace />;
  }

  if (status === 'loading') {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '200px', color: 'var(--text1)' }}>
        구독 상태 확인 중...
      </div>
    );
  }

  if (status === 'inactive') {
    if (!toastShown.current) {
      toastShown.current = true;
      useToastStore.getState().show('warning', '구독이 필요한 기능입니다.');
    }
    return <Navigate to="/subscriptions" replace />;
  }

  return <>{children}</>;
}

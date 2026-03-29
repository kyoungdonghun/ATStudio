import { Navigate } from 'react-router-dom';
import { type ReactNode, useState, useEffect } from 'react';
import { useAuthStore } from '@/store/authStore';
import { fetchMySubscription } from '@/api/userSubscriptions';

interface SubscriberRouteProps {
  children: ReactNode;
}

/**
 * Route guard that requires an active subscription.
 * Redirects non-subscribers to /subscriptions.
 */
export default function SubscriberRoute({ children }: SubscriberRouteProps) {
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated());
  const [status, setStatus] = useState<'loading' | 'active' | 'inactive'>('loading');

  useEffect(() => {
    if (!isAuthenticated) {
      setStatus('inactive');
      return;
    }
    fetchMySubscription()
      .then((sub) => {
        setStatus(sub.status === 'ACTIVE' ? 'active' : 'inactive');
      })
      .catch(() => {
        setStatus('inactive');
      });
  }, [isAuthenticated]);

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (status === 'loading') {
    return null;
  }

  if (status === 'inactive') {
    return <Navigate to="/subscriptions" replace />;
  }

  return <>{children}</>;
}

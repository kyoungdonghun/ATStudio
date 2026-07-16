import { Navigate } from 'react-router-dom';
import { type ReactNode, useState, useEffect, useRef } from 'react';
import { useAuthStore } from '@/store/authStore';
import { useToastStore } from '@/store/toastStore';
import { fetchMySubscription, isNoActiveSubscriptionError } from '@/api/userSubscriptions';
import { classifyLoadError, getLoadErrorMessage } from '@/api/loadError';

interface SubscriberRouteProps {
  children: ReactNode;
}

/**
 * Route guard that requires a service-enabled subscription: ACTIVE, or CANCELLED before expiry.
 * Redirects users without service-enabled access to /subscriptions with a toast notification.
 */
export default function SubscriberRoute({ children }: SubscriberRouteProps) {
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated());
  const [status, setStatus] = useState<'loading' | 'active' | 'inactive' | 'error'>('loading');
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [retryKey, setRetryKey] = useState(0);
  const toastShown = useRef(false);
  const retryBlocked = useRef(false);
  const requestGenerationRef = useRef(0);

  useEffect(() => {
    const requestGeneration = ++requestGenerationRef.current;

    if (!isAuthenticated) {
      retryBlocked.current = false;
      setErrorMessage(null);
      setStatus('inactive');
      return () => {
        if (requestGenerationRef.current === requestGeneration) {
          requestGenerationRef.current += 1;
        }
      };
    }

    const controller = new AbortController();
    const isCurrentRequest = () =>
      requestGenerationRef.current === requestGeneration && !controller.signal.aborted;
    retryBlocked.current = true;
    setStatus('loading');

    void fetchMySubscription(controller.signal)
      .then(() => {
        if (!isCurrentRequest()) return;
        setErrorMessage(null);
        setStatus('active');
      })
      .catch((error: unknown) => {
        if (!isCurrentRequest()) return;
        if (classifyLoadError(error) === 'cancelled') return;

        if (isNoActiveSubscriptionError(error)) {
          setErrorMessage(null);
          setStatus('inactive');
          return;
        }

        setErrorMessage(getLoadErrorMessage(error, '구독 상태'));
        setStatus('error');
      })
      .finally(() => {
        if (isCurrentRequest()) {
          retryBlocked.current = false;
        }
      });

    return () => {
      if (requestGenerationRef.current === requestGeneration) {
        requestGenerationRef.current += 1;
      }
      controller.abort();
    };
  }, [isAuthenticated, retryKey]);

  function retry() {
    if (retryBlocked.current || status === 'loading') return;
    retryBlocked.current = true;
    setStatus('loading');
    setRetryKey((current) => current + 1);
  }

  if (!isAuthenticated) {
    if (!toastShown.current) {
      toastShown.current = true;
      useToastStore.getState().show('warning', '로그인이 필요한 기능입니다.');
    }
    return <Navigate to="/login" replace />;
  }

  if (status === 'loading' && !errorMessage) {
    return (
      <div
        style={{
          display: 'flex',
          justifyContent: 'center',
          alignItems: 'center',
          minHeight: '200px',
          color: 'var(--text1)',
        }}
      >
        구독 상태 확인 중...
      </div>
    );
  }

  if (status === 'error' || (status === 'loading' && errorMessage)) {
    return (
      <div
        role="alert"
        style={{
          display: 'flex',
          flexDirection: 'column',
          justifyContent: 'center',
          alignItems: 'center',
          gap: '12px',
          minHeight: '200px',
          color: 'var(--text1)',
          textAlign: 'center',
        }}
      >
        <p>{errorMessage}</p>
        <button type="button" onClick={retry} disabled={status === 'loading'}>
          다시 시도
        </button>
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

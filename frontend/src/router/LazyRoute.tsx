import { lazy, Suspense, useCallback, useMemo, useState, type ComponentType } from 'react';
import LazyRouteRecovery from '@/router/LazyRouteRecovery';
import styles from './LazyRoute.module.css';

type PageModule = { default: ComponentType };

const routeFallback = (
  <div className={styles.loading} role="status">
    로딩 중...
  </div>
);

export function createLazyPage(loader: () => Promise<PageModule>): ComponentType {
  return function LazyPage() {
    const [attempt, setAttempt] = useState(0);

    const retry = useCallback(() => {
      setAttempt((current) => (current < 1 ? current + 1 : current));
    }, []);

    const Page = useMemo(
      () =>
        lazy(async () => {
          try {
            return await loader();
          } catch {
            return {
              default: () => <LazyRouteRecovery canRetry={attempt < 1} onRetry={retry} />,
            };
          }
        }),
      [attempt, retry],
    );

    return (
      <Suspense fallback={routeFallback}>
        <Page />
      </Suspense>
    );
  };
}

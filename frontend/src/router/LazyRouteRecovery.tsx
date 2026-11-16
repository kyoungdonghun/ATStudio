import { useRef, useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { reloadCurrentDocument } from '@/router/reloadCurrentDocument';
import styles from './LazyRoute.module.css';

interface LazyRouteRecoveryProps {
  canRetry: boolean;
  onRetry: () => void;
}

export default function LazyRouteRecovery({ canRetry, onRetry }: LazyRouteRecoveryProps) {
  const location = useLocation();
  const navigate = useNavigate();
  const canReturnInternally = location.key !== 'default';
  const reloadRequested = useRef(false);
  const [isReloading, setIsReloading] = useState(false);

  function loadLatestPage() {
    if (reloadRequested.current) return;
    reloadRequested.current = true;
    setIsReloading(true);
    reloadCurrentDocument();
  }

  function goBack() {
    if (canReturnInternally) {
      navigate(-1);
      return;
    }
    navigate('/', { replace: true });
  }

  return (
    <div className={styles.recovery} role="alert">
      <h1 className={styles.title}>페이지를 불러오지 못했습니다</h1>
      <p className={styles.message}>
        화면이 업데이트되었거나 연결이 일시적으로 끊겼을 수 있습니다. 연결을 확인한 뒤 최신 화면을
        불러와 주세요.
      </p>
      <p className={styles.message}>
        최신 화면을 불러오면 저장하지 않은 입력은 사라질 수 있습니다.
      </p>
      <div className={styles.actions}>
        <button
          type="button"
          className={styles.primaryAction}
          onClick={loadLatestPage}
          disabled={isReloading}
        >
          {isReloading ? '화면 불러오는 중...' : '최신 화면 불러오기'}
        </button>
        {canRetry ? (
          <button
            type="button"
            className={styles.secondaryAction}
            onClick={onRetry}
            disabled={isReloading}
          >
            다시 시도
          </button>
        ) : null}
        <button type="button" className={styles.secondaryAction} onClick={goBack}>
          이전 화면
        </button>
        <Link to="/" replace className={styles.homeLink}>
          홈으로
        </Link>
      </div>
    </div>
  );
}

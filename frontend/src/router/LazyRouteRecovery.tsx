import { Link, useLocation, useNavigate } from 'react-router-dom';
import styles from './LazyRoute.module.css';

interface LazyRouteRecoveryProps {
  canRetry: boolean;
  onRetry: () => void;
}

export default function LazyRouteRecovery({ canRetry, onRetry }: LazyRouteRecoveryProps) {
  const location = useLocation();
  const navigate = useNavigate();
  const canReturnInternally = location.key !== 'default';

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
        {canRetry ? '연결을 확인한 뒤 다시 시도해 주세요.' : '잠시 후 홈에서 다시 접근해 주세요.'}
      </p>
      <div className={styles.actions}>
        {canRetry ? (
          <button type="button" className={styles.primaryAction} onClick={onRetry}>
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

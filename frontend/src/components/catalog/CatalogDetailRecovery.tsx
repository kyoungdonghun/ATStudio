import { Link, useLocation, useNavigate } from 'react-router-dom';
import styles from './CatalogDetailRecovery.module.css';

interface CatalogDetailRecoveryProps {
  title: string;
  message: string;
  onRetry: () => void;
}

export default function CatalogDetailRecovery({
  title,
  message,
  onRetry,
}: CatalogDetailRecoveryProps) {
  const location = useLocation();
  const navigate = useNavigate();

  function goBack() {
    if (location.key !== 'default') {
      navigate(-1);
      return;
    }
    navigate('/', { replace: true });
  }

  return (
    <section className={styles.recovery} role="alert">
      <h1 className={styles.title}>{title}</h1>
      <p className={styles.message}>{message}</p>
      <div className={styles.actions}>
        <button type="button" className={styles.primaryAction} onClick={onRetry}>
          {'다시 시도'}
        </button>
        <button type="button" className={styles.secondaryAction} onClick={goBack}>
          {'이전 화면'}
        </button>
        <Link to="/" replace className={styles.homeLink}>
          {'홈으로'}
        </Link>
      </div>
    </section>
  );
}

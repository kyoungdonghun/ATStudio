import { Link } from 'react-router-dom';
import styles from './ErrorPage.module.css';

export default function NotFoundPage() {
  return (
    <div className={styles.page}>
      <h1 className={styles.code}>404</h1>
      <p className={styles.message}>{'페이지를 찾을 수 없습니다'}</p>
      <Link to="/" className={styles.homeLink}>
        {'홈으로 돌아가기'}
      </Link>
    </div>
  );
}

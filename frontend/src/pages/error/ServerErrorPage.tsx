import { Link } from 'react-router-dom';
import styles from './ErrorPage.module.css';

export default function ServerErrorPage() {
  return (
    <div className={styles.page}>
      <h1 className={styles.code}>500</h1>
      <p className={styles.message}>{'서버 오류가 발생했습니다'}</p>
      <Link to="/" className={styles.homeLink}>
        {'홈으로 돌아가기'}
      </Link>
    </div>
  );
}

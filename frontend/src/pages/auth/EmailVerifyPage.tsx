import { useEffect, useRef, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { verifyEmail } from '@/api/auth';
import { getEmailVerificationErrorMessage } from '@/api/authError';
import styles from './EmailVerifyPage.module.css';

export default function EmailVerifyPage() {
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token');

  const [status, setStatus] = useState<'loading' | 'success' | 'error' | 'no-token'>(
    token ? 'loading' : 'no-token',
  );
  const [errorMessage, setErrorMessage] = useState('');
  const verificationStartedRef = useRef(false);

  useEffect(() => {
    if (!token || verificationStartedRef.current) return;

    verificationStartedRef.current = true;
    verifyEmail(token)
      .then(() => setStatus('success'))
      .catch((verificationError: unknown) => {
        setStatus('error');
        setErrorMessage(getEmailVerificationErrorMessage(verificationError));
      });
  }, [token]);

  if (status === 'loading') {
    return (
      <div className={styles.page}>
        <div className={styles.card}>
          <div className={styles.icon} aria-hidden="true">
            &#9993;
          </div>
          <h1 className={styles.title}>이메일 인증 중...</h1>
          <p className={styles.description}>잠시만 기다려주세요.</p>
        </div>
      </div>
    );
  }

  if (status === 'success') {
    return (
      <div className={styles.page}>
        <div className={styles.card}>
          <div className={styles.icon} aria-hidden="true">
            &#10003;
          </div>
          <h1 className={styles.title}>인증 완료</h1>
          <p className={styles.successText}>이메일 인증이 완료되었습니다.</p>
          <div className={styles.links}>
            <Link to="/login" className={styles.link}>
              로그인하러 가기
            </Link>
          </div>
        </div>
      </div>
    );
  }

  if (status === 'error') {
    return (
      <div className={styles.page}>
        <div className={styles.card}>
          <div className={styles.icon} aria-hidden="true">
            &#10007;
          </div>
          <h1 className={styles.title}>인증 실패</h1>
          <p className={styles.errorText}>{errorMessage}</p>
          <div className={styles.links}>
            <Link to="/login" className={styles.link}>
              로그인으로 돌아가기
            </Link>
          </div>
        </div>
      </div>
    );
  }

  // no-token
  return (
    <div className={styles.page}>
      <div className={styles.card}>
        <div className={styles.icon} aria-hidden="true">
          &#9993;
        </div>
        <h1 className={styles.title}>이메일 인증</h1>
        <p className={styles.description}>이메일에서 인증 링크를 클릭해주세요.</p>
        <div className={styles.links}>
          <Link to="/login" className={styles.link}>
            로그인으로 돌아가기
          </Link>
        </div>
      </div>
    </div>
  );
}

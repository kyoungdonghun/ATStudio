import { type FormEvent, useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { verifyEmail } from '@/api/auth';
import Button from '@/components/ui/Button';
import styles from './EmailVerifyPage.module.css';

/**
 * Screen: Email Verification
 * Shown after successful signup to prompt the user to verify their email.
 * Backend verify-email endpoint is not yet implemented;
 * this page serves as the planned frontend flow.
 */
export default function EmailVerifyPage() {
  const location = useLocation();
  const emailFromState = (location.state as { email?: string } | null)?.email ?? '';

  const [token, setToken] = useState('');
  const [error, setError] = useState('');
  const [verified, setVerified] = useState(false);
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setError('');

    if (!token.trim()) {
      setError('인증 코드를 입력해주세요.');
      return;
    }

    setLoading(true);
    try {
      await verifyEmail({ token: token.trim() });
      setVerified(true);
    } catch {
      setError('인증에 실패했습니다. 코드를 다시 확인해주세요.');
    } finally {
      setLoading(false);
    }
  }

  if (verified) {
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

  return (
    <div className={styles.page}>
      <div className={styles.card}>
        <div className={styles.icon} aria-hidden="true">
          &#9993;
        </div>
        <h1 className={styles.title}>이메일 인증</h1>
        <p className={styles.description}>
          가입하신 이메일로 인증 코드를 발송했습니다.
        </p>
        {emailFromState && <p className={styles.email}>{emailFromState}</p>}

        <form className={styles.form} onSubmit={handleSubmit} noValidate>
          <input
            className={styles.input}
            type="text"
            placeholder="인증 코드 입력"
            value={token}
            onChange={(e) => setToken(e.target.value)}
            autoComplete="one-time-code"
            maxLength={8}
          />

          <p className={styles.errorText}>{error}</p>

          <Button
            type="submit"
            variant="primary"
            size="lg"
            loading={loading}
            className={styles.submitButton}
          >
            인증하기
          </Button>
        </form>

        <div className={styles.links}>
          <Link to="/login" className={styles.link}>
            로그인으로 돌아가기
          </Link>
        </div>
      </div>
    </div>
  );
}

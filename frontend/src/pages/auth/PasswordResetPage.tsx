import { type FormEvent, useState } from 'react';
import { Link } from 'react-router-dom';
import { requestPasswordReset } from '@/api/auth';
import Button from '@/components/ui/Button';
import styles from './PasswordResetPage.module.css';

/**
 * Screen: Password Reset Request
 * Allows the user to enter their email address to receive a password reset link.
 * Backend password-reset endpoint is not yet implemented;
 * this page serves as the planned frontend flow.
 */
export default function PasswordResetPage() {
  const [email, setEmail] = useState('');
  const [error, setError] = useState('');
  const [sent, setSent] = useState(false);
  const [loading, setLoading] = useState(false);

  function validate(): boolean {
    if (!email.trim()) {
      setError('이메일을 입력해주세요.');
      return false;
    }
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
      setError('올바른 이메일 형식을 입력해주세요.');
      return false;
    }
    return true;
  }

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setError('');

    if (!validate()) return;

    setLoading(true);
    try {
      await requestPasswordReset({ email: email.trim() });
      setSent(true);
    } catch {
      setError('요청에 실패했습니다. 잠시 후 다시 시도해주세요.');
    } finally {
      setLoading(false);
    }
  }

  if (sent) {
    return (
      <div className={styles.page}>
        <div className={`${styles.card} ${styles.successCard}`}>
          <div className={styles.successIcon} aria-hidden="true">
            &#9993;
          </div>
          <h1 className={styles.title}>메일 발송 완료</h1>
          <p className={styles.successText}>
            비밀번호 재설정 링크를 발송했습니다.
          </p>
          <p className={styles.successDetail}>
            입력하신 이메일({email})로 비밀번호 재설정 링크를 보냈습니다.
            <br />
            메일이 도착하지 않으면 스팸 폴더를 확인해주세요.
          </p>
          <div className={styles.links}>
            <Link to="/login" className={styles.link}>
              로그인으로 돌아가기
            </Link>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className={styles.page}>
      <div className={styles.card}>
        <h1 className={styles.title}>비밀번호 찾기</h1>
        <p className={styles.description}>
          가입하신 이메일 주소를 입력하시면
          <br />
          비밀번호 재설정 링크를 보내드립니다.
        </p>

        <form className={styles.form} onSubmit={handleSubmit} noValidate>
          <div className={styles.fieldGroup}>
            <label className={styles.label} htmlFor="reset-email">
              이메일
            </label>
            <input
              id="reset-email"
              className={styles.input}
              type="email"
              placeholder="your@email.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              autoComplete="email"
            />
          </div>

          <p className={styles.errorText}>{error}</p>

          <Button
            type="submit"
            variant="primary"
            size="lg"
            loading={loading}
            className={styles.submitButton}
          >
            재설정 링크 발송
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

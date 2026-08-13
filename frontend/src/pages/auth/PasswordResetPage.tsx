import { type FormEvent, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { requestPasswordReset, resetPassword } from '@/api/auth';
import { getForgotPasswordErrorMessage, getPasswordResetErrorMessage } from '@/api/authError';
import { usePublicCapabilities } from '@/hooks/usePublicCapabilities';
import { isValidEmail, PASSWORD_MIN } from '@/utils/validation';
import Button from '@/components/ui/Button';
import styles from './PasswordResetPage.module.css';

export default function PasswordResetPage() {
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token');
  const {
    capabilities,
    loading: capabilitiesLoading,
    error: capabilitiesError,
    retry: retryCapabilities,
  } = usePublicCapabilities();

  if (capabilitiesLoading) {
    return (
      <div className={styles.page}>
        <div className={styles.card}>
          <h1 className={styles.title}>비밀번호 재설정</h1>
          <p className={styles.description}>비밀번호 재설정 환경을 확인하는 중...</p>
        </div>
      </div>
    );
  }

  if (capabilitiesError || !capabilities) {
    return (
      <div className={styles.page}>
        <div className={`${styles.card} ${styles.noticeCard}`}>
          <h1 className={styles.title}>비밀번호 재설정</h1>
          <p className={styles.noticeText}>
            {capabilitiesError || '현재 비밀번호 재설정 가능 여부를 확인하지 못했습니다.'}
          </p>
          <Button
            type="button"
            variant="primary"
            size="lg"
            className={styles.submitButton}
            onClick={() => void retryCapabilities()}
          >
            다시 시도
          </Button>
          <div className={styles.links}>
            <Link to="/login" className={styles.link}>
              로그인으로 돌아가기
            </Link>
          </div>
        </div>
      </div>
    );
  }

  const isPasswordResetAvailable =
    capabilities.passwordLoginEnabled && capabilities.passwordReset.enabled;

  if (!isPasswordResetAvailable) {
    return (
      <div className={styles.page}>
        <div className={`${styles.card} ${styles.noticeCard}`}>
          <h1 className={styles.title}>비밀번호 재설정</h1>
          <p className={styles.description}>
            현재 이 환경에서는 비밀번호 재설정 기능이 비활성화되어 있습니다.
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

  if (token) {
    return <ResetForm token={token} />;
  }
  return <ForgotForm isLocalMailMode={capabilities.passwordReset.deliveryMode === 'LOCAL_SMTP'} />;
}

/* ── Step 1: 이메일 입력 (비밀번호 찾기) ── */
function ForgotForm({ isLocalMailMode }: { isLocalMailMode: boolean }) {
  const [email, setEmail] = useState('');
  const [error, setError] = useState('');
  const [sent, setSent] = useState(false);
  const [loading, setLoading] = useState(false);

  function validate(): boolean {
    if (!email.trim()) {
      setError('이메일을 입력해주세요.');
      return false;
    }
    if (!isValidEmail(email)) {
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
    } catch (requestError: unknown) {
      setError(getForgotPasswordErrorMessage(requestError));
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
          <h1 className={styles.title}>요청 접수 완료</h1>
          <p className={styles.successText}>비밀번호 재설정 요청을 접수했습니다.</p>
          <p className={styles.successDetail}>
            입력하신 이메일({email})로 가입된 계정이 있다면 재설정 링크를 안내합니다.
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
        {isLocalMailMode ? (
          <p className={styles.noticeText}>
            현재 이 환경에서는 로컬 메일 수신 환경(MailHog 등)에서만 재설정 링크를 확인할 수
            있습니다.
          </p>
        ) : null}
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

/* ── Step 2: 새 비밀번호 입력 (토큰 기반) ── */
function ResetForm({ token }: { token: string }) {
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [error, setError] = useState('');
  const [done, setDone] = useState(false);
  const [loading, setLoading] = useState(false);

  function validate(): boolean {
    if (!password) {
      setError('새 비밀번호를 입력해주세요.');
      return false;
    }
    if (password.length < PASSWORD_MIN) {
      setError(`비밀번호는 ${PASSWORD_MIN}자 이상이어야 합니다.`);
      return false;
    }
    if (password !== confirmPassword) {
      setError('비밀번호가 일치하지 않습니다.');
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
      await resetPassword(token, password);
      setDone(true);
    } catch (resetError: unknown) {
      setError(getPasswordResetErrorMessage(resetError));
    } finally {
      setLoading(false);
    }
  }

  if (done) {
    return (
      <div className={styles.page}>
        <div className={`${styles.card} ${styles.successCard}`}>
          <div className={styles.successIcon} aria-hidden="true">
            &#10003;
          </div>
          <h1 className={styles.title}>비밀번호 변경 완료</h1>
          <p className={styles.successText}>비밀번호가 성공적으로 변경되었습니다.</p>
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
        <h1 className={styles.title}>비밀번호 재설정</h1>
        <p className={styles.description}>새로운 비밀번호를 입력해주세요.</p>
        <form className={styles.form} onSubmit={handleSubmit} noValidate>
          <div className={styles.fieldGroup}>
            <label className={styles.label} htmlFor="new-password">
              새 비밀번호
            </label>
            <input
              id="new-password"
              className={styles.input}
              type="password"
              placeholder="8자 이상"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              autoComplete="new-password"
            />
          </div>
          <div className={styles.fieldGroup}>
            <label className={styles.label} htmlFor="confirm-password">
              비밀번호 확인
            </label>
            <input
              id="confirm-password"
              className={styles.input}
              type="password"
              placeholder="비밀번호 재입력"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              autoComplete="new-password"
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
            비밀번호 변경
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

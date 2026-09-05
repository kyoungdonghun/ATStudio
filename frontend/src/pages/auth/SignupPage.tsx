import { type FormEvent, useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuthStore } from '@/store/authStore';
import {
  register,
  checkEmailAvailability,
  checkNicknameAvailability,
  checkPhoneAvailability,
} from '@/api/auth';
import { usePublicCapabilities } from '@/hooks/usePublicCapabilities';
import { getSignupErrorMessage } from '@/api/authError';
import {
  formatPhone,
  isValidEmail,
  isValidNickname,
  isValidPhone,
  normalizeNickname,
  PASSWORD_MIN,
  NICKNAME_MAX,
} from '@/utils/validation';
import Button from '@/components/ui/Button';
import styles from './SignupPage.module.css';

type UserType = 'INDIVIDUAL' | 'BUSINESS';

const JOB_OPTIONS = [
  { value: '', label: '직업을 선택하세요' },
  { value: 'EDITOR', label: '편집자' },
  { value: 'ARTIST', label: '아티스트' },
  { value: 'FREELANCER', label: '프리랜서' },
];

/** Screen A-2: Signup */
export default function SignupPage() {
  const navigate = useNavigate();
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated());
  const {
    capabilities,
    loading: capabilitiesLoading,
    error: capabilitiesError,
    retry: retryCapabilities,
  } = usePublicCapabilities();

  useEffect(() => {
    if (isAuthenticated) navigate('/', { replace: true });
  }, [isAuthenticated, navigate]);

  const [userType, setUserType] = useState<UserType>('INDIVIDUAL');
  const [nickname, setNickname] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [passwordConfirm, setPasswordConfirm] = useState('');
  const [phonePersonal, setPhonePersonal] = useState('');
  const [phoneCompany, setPhoneCompany] = useState('');
  const [job, setJob] = useState('');
  const [companyName, setCompanyName] = useState('');
  const [termsAgreed, setTermsAgreed] = useState(false);
  const [privacyAgreed, setPrivacyAgreed] = useState(false);
  const [marketingAgreed, setMarketingAgreed] = useState(false);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const isPasswordLoginEnabled = capabilities?.passwordLoginEnabled === true;
  const isEmailVerificationAvailable = capabilities?.emailVerification.enabled === true;
  const isSignupEnabled = isPasswordLoginEnabled && isEmailVerificationAvailable;
  const isLocalMailMode = capabilities?.emailVerification.deliveryMode === 'LOCAL_SMTP';

  function validate(nicknameToValidate: string): boolean {
    if (!nicknameToValidate) {
      setError('닉네임을 입력해주세요.');
      return false;
    }
    if (!isValidNickname(nicknameToValidate)) {
      setError('닉네임은 2~20자의 한글, 영문, 숫자, 밑줄(_), 공백만 사용할 수 있습니다.');
      return false;
    }
    if (!email.trim()) {
      setError('이메일을 입력해주세요.');
      return false;
    }
    if (!isValidEmail(email)) {
      setError('올바른 이메일 형식을 입력해주세요.');
      return false;
    }
    if (!password) {
      setError('비밀번호를 입력해주세요.');
      return false;
    }
    if (password.length < PASSWORD_MIN) {
      setError(`비밀번호는 ${PASSWORD_MIN}자 이상이어야 합니다.`);
      return false;
    }
    if (password !== passwordConfirm) {
      setError('비밀번호가 일치하지 않습니다.');
      return false;
    }
    if (!phonePersonal.trim()) {
      setError('연락처를 입력해주세요.');
      return false;
    }
    if (!isValidPhone(phonePersonal)) {
      setError('올바른 전화번호 형식을 입력해주세요.');
      return false;
    }
    if (userType === 'BUSINESS') {
      if (!companyName.trim()) {
        setError('회사명 또는 업종을 입력해주세요.');
        return false;
      }
    } else {
      if (!job) {
        setError('직업을 선택해주세요.');
        return false;
      }
    }
    if (!termsAgreed) {
      setError('이용약관에 동의해주세요.');
      return false;
    }
    if (!privacyAgreed) {
      setError('개인정보 처리방침에 동의해주세요.');
      return false;
    }
    return true;
  }

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setError('');

    if (!isPasswordLoginEnabled) {
      setError('현재 이 환경에서는 이메일 회원가입이 비활성화되어 있습니다.');
      return;
    }

    if (!isEmailVerificationAvailable) {
      setError('현재 이 환경에서는 이메일 인증 메일이 비활성화되어 가입을 진행할 수 없습니다.');
      return;
    }

    const normalizedNickname = normalizeNickname(nickname);
    setNickname(normalizedNickname);
    if (!validate(normalizedNickname)) return;

    setLoading(true);
    try {
      /* Parallel availability checks */
      const [emailCheck, nicknameCheck, phoneCheck] = await Promise.all([
        checkEmailAvailability(email),
        checkNicknameAvailability(normalizedNickname),
        checkPhoneAvailability(phonePersonal),
      ]);

      if (!emailCheck.available) {
        setError('이미 사용 중인 이메일입니다.');
        return;
      }
      if (!nicknameCheck.available) {
        setError('이미 사용 중인 닉네임입니다.');
        return;
      }
      if (!phoneCheck.available) {
        setError('이미 등록된 전화번호입니다.');
        return;
      }

      await register({
        nickname: normalizedNickname,
        email,
        password,
        phonePersonal,
        phoneCompany: phoneCompany.trim() || null,
        job: userType === 'BUSINESS' ? null : job,
        companyName: userType === 'BUSINESS' ? companyName.trim() : undefined,
        userType,
        termsAgreed,
        privacyAgreed,
        marketingAgreed,
      });

      navigate('/email-verify', { replace: true });
    } catch (err: unknown) {
      setError(getSignupErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }

  if (capabilitiesLoading) {
    return (
      <div className={styles.page}>
        <div className={styles.card}>
          <h1 className={styles.title}>회원가입</h1>
          <p className={styles.subtitle}>가입 환경을 확인하는 중...</p>
        </div>
      </div>
    );
  }

  if (capabilitiesError || !capabilities) {
    return (
      <div className={styles.page}>
        <div className={styles.card}>
          <h1 className={styles.title}>회원가입</h1>
          <p className={styles.noticeText}>
            {capabilitiesError || '현재 회원가입 가능 여부를 확인하지 못했습니다.'}
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

  return (
    <div className={styles.page}>
      <div className={styles.card}>
        <h1 className={styles.title}>회원가입</h1>
        <p className={styles.subtitle}>AT.M에 가입하고 음악을 시작하세요</p>

        <form className={styles.form} onSubmit={handleSubmit} noValidate>
          {/* User Type Toggle */}
          <div className={styles.fieldGroup}>
            <span className={styles.label} id="signup-user-type-label">
              회원 유형
            </span>
            <div
              aria-labelledby="signup-user-type-label"
              className={styles.roleToggle}
              role="group"
            >
              <button
                type="button"
                className={userType === 'INDIVIDUAL' ? styles.roleOptionActive : styles.roleOption}
                onClick={() => setUserType('INDIVIDUAL')}
                aria-pressed={userType === 'INDIVIDUAL'}
              >
                개인
              </button>
              <button
                type="button"
                className={userType === 'BUSINESS' ? styles.roleOptionActive : styles.roleOption}
                onClick={() => setUserType('BUSINESS')}
                aria-pressed={userType === 'BUSINESS'}
              >
                기업
              </button>
            </div>
          </div>

          {/* Nickname */}
          <div className={styles.fieldGroup}>
            <label className={styles.label} htmlFor="signup-nickname">
              닉네임
            </label>
            <input
              id="signup-nickname"
              className={styles.input}
              type="text"
              placeholder="닉네임"
              value={nickname}
              onChange={(e) => setNickname(e.target.value)}
              onBlur={() => setNickname(normalizeNickname(nickname))}
              maxLength={NICKNAME_MAX}
              autoComplete="nickname"
            />
          </div>

          {/* Email */}
          <div className={styles.fieldGroup}>
            <label className={styles.label} htmlFor="signup-email">
              이메일
            </label>
            <input
              id="signup-email"
              className={styles.input}
              type="email"
              placeholder="your@email.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              autoComplete="email"
            />
          </div>

          {/* Password */}
          <div className={styles.fieldGroup}>
            <label className={styles.label} htmlFor="signup-password">
              비밀번호
            </label>
            <input
              id="signup-password"
              className={styles.input}
              type="password"
              placeholder="8자 이상"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              autoComplete="new-password"
            />
            {password.length > 0 && (
              <div className={styles.pwHints}>
                <span
                  className={password.length >= PASSWORD_MIN ? styles.pwValid : styles.pwInvalid}
                >
                  {`${PASSWORD_MIN}자 이상`}
                </span>
              </div>
            )}
          </div>

          {/* Password Confirm */}
          <div className={styles.fieldGroup}>
            <label className={styles.label} htmlFor="signup-password-confirm">
              비밀번호 확인
            </label>
            <input
              id="signup-password-confirm"
              className={styles.input}
              type="password"
              placeholder="비밀번호 재입력"
              value={passwordConfirm}
              onChange={(e) => setPasswordConfirm(e.target.value)}
              autoComplete="new-password"
            />
          </div>

          {/* Phone */}
          <div className={styles.fieldGroup}>
            <label className={styles.label} htmlFor="signup-phone">
              연락처
            </label>
            <input
              id="signup-phone"
              className={styles.input}
              type="tel"
              placeholder="010-1234-5678"
              value={phonePersonal}
              onChange={(e) => setPhonePersonal(formatPhone(e.target.value))}
              autoComplete="tel"
            />
          </div>

          {/* Company phone (business only) */}
          {userType === 'BUSINESS' && (
            <div className={styles.fieldGroup}>
              <label className={styles.label} htmlFor="signup-company-phone">
                회사 연락처 (선택)
              </label>
              <input
                id="signup-company-phone"
                className={styles.input}
                type="tel"
                placeholder="02-0000-0000"
                value={phoneCompany}
                onChange={(e) => setPhoneCompany(formatPhone(e.target.value))}
              />
            </div>
          )}

          {/* Job (INDIVIDUAL) / Company name or industry (BUSINESS) */}
          {userType === 'BUSINESS' ? (
            <div className={styles.fieldGroup}>
              <label className={styles.label} htmlFor="signup-company-name">
                회사명 또는 업종
              </label>
              <input
                id="signup-company-name"
                className={styles.input}
                type="text"
                placeholder="회사명 또는 업종을 입력하세요"
                value={companyName}
                onChange={(e) => setCompanyName(e.target.value)}
                maxLength={100}
              />
            </div>
          ) : (
            <div className={styles.fieldGroup}>
              <label className={styles.label} htmlFor="signup-job">
                직업
              </label>
              <select
                id="signup-job"
                className={styles.select}
                value={job}
                onChange={(e) => setJob(e.target.value)}
              >
                {JOB_OPTIONS.map((opt) => (
                  <option key={opt.value} value={opt.value}>
                    {opt.label}
                  </option>
                ))}
              </select>
            </div>
          )}

          <fieldset className={styles.consentGroup}>
            <legend className={styles.consentLegend}>약관 동의</legend>
            <label className={styles.consentOption} htmlFor="signup-terms-agreed">
              <input
                id="signup-terms-agreed"
                type="checkbox"
                checked={termsAgreed}
                onChange={(event) => setTermsAgreed(event.target.checked)}
                required
              />
              <span>이용약관 동의 (필수)</span>
            </label>
            <label className={styles.consentOption} htmlFor="signup-privacy-agreed">
              <input
                id="signup-privacy-agreed"
                type="checkbox"
                checked={privacyAgreed}
                onChange={(event) => setPrivacyAgreed(event.target.checked)}
                required
              />
              <span>개인정보 처리방침 동의 (필수)</span>
            </label>
            <label className={styles.consentOption} htmlFor="signup-marketing-agreed">
              <input
                id="signup-marketing-agreed"
                type="checkbox"
                checked={marketingAgreed}
                onChange={(event) => setMarketingAgreed(event.target.checked)}
              />
              <span>마케팅 정보 수신 동의 (선택)</span>
            </label>
          </fieldset>

          <p className={styles.errorText} role="alert">
            {error}
          </p>

          {!capabilitiesLoading && capabilities && !isPasswordLoginEnabled ? (
            <p className={styles.noticeText}>
              현재 이 환경에서는 이메일 로그인과 회원가입이 비활성화되어 있습니다. 운영자에게 소셜
              로그인 또는 테스트 계정을 확인해주세요.
            </p>
          ) : null}

          {!capabilitiesLoading &&
          capabilities &&
          isPasswordLoginEnabled &&
          !isEmailVerificationAvailable ? (
            <p className={styles.noticeText}>
              현재 이 환경에서는 이메일 인증 메일이 비활성화되어 있습니다. 가입 전에 운영자에게 메일
              설정을 요청해주세요.
            </p>
          ) : null}

          {isPasswordLoginEnabled && isLocalMailMode ? (
            <p className={styles.noticeText}>
              현재 이 환경에서는 로컬 메일 수신 환경(MailHog 등)에서만 인증 링크를 확인할 수
              있습니다.
            </p>
          ) : null}

          <Button
            type="submit"
            variant="primary"
            size="lg"
            disabled={!isSignupEnabled}
            loading={loading}
            className={styles.submitButton}
          >
            가입하기
          </Button>
        </form>

        <div className={styles.links}>
          <Link to="/login" className={styles.link}>
            이미 계정이 있으신가요? 로그인
          </Link>
        </div>
      </div>
    </div>
  );
}

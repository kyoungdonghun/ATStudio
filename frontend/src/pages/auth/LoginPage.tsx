import { type FormEvent, useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuthStore } from '@/store/authStore';
import { login, fetchMe } from '@/api/auth';
import type { MeResponse, PublicCapabilitiesResponse } from '@/api/auth';
import { usePublicCapabilities } from '@/hooks/usePublicCapabilities';
import { isValidEmail, PASSWORD_MIN } from '@/utils/validation';
import { safeSessionStorage } from '@/utils/safeStorage';
import Button from '@/components/ui/Button';
import styles from './LoginPage.module.css';

/* ── PKCE helpers ── */

function generateRandomString(length: number): string {
  const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-._~';
  const arr = new Uint8Array(length);
  crypto.getRandomValues(arr);
  return Array.from(arr, (v) => chars[v % chars.length]).join('');
}

async function generateCodeChallenge(verifier: string): Promise<string> {
  const data = new TextEncoder().encode(verifier);
  const digest = await crypto.subtle.digest('SHA-256', data);
  return btoa(String.fromCharCode(...new Uint8Array(digest)))
    .replace(/\+/g, '-')
    .replace(/\//g, '_')
    .replace(/=+$/, '');
}

/* ── Provider config ── */

const PROVIDER_CONFIG = {
  GOOGLE: {
    authUrl: 'https://accounts.google.com/o/oauth2/v2/auth',
    scope: 'email profile',
  },
  KAKAO: {
    authUrl: 'https://kauth.kakao.com/oauth/authorize',
    scope: 'profile_nickname account_email',
  },
  NAVER: {
    authUrl: 'https://nid.naver.com/oauth2.0/authorize',
    scope: '',
  },
} as const;

const PROVIDER_LABELS = {
  GOOGLE: 'Google',
  KAKAO: 'Kakao',
  NAVER: 'Naver',
} as const;

const PROVIDER_KEYS = {
  GOOGLE: 'google',
  KAKAO: 'kakao',
  NAVER: 'naver',
} as const;

type SocialProvider = keyof typeof PROVIDER_CONFIG;

function getProviderCapability(
  capabilities: PublicCapabilitiesResponse | null,
  provider: SocialProvider,
) {
  if (!capabilities) return null;
  return capabilities.socialLogin[PROVIDER_KEYS[provider]];
}

/** Screen A-1: Login */
export default function LoginPage() {
  const navigate = useNavigate();
  const authLogin = useAuthStore((s) => s.login);
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated());
  const { capabilities, loading: capabilitiesLoading, error: capabilitiesError } =
    usePublicCapabilities();

  useEffect(() => {
    if (isAuthenticated) navigate('/', { replace: true });
  }, [isAuthenticated, navigate]);

  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const enabledSocialProviders = (Object.keys(PROVIDER_CONFIG) as SocialProvider[]).filter(
    (provider) => getProviderCapability(capabilities, provider)?.enabled,
  );
  const isPasswordLoginEnabled = capabilities?.passwordLoginEnabled ?? true;
  const isQaBootstrapEnabled = capabilities?.testUsersEnabled ?? false;
  const isPasswordResetAvailable = capabilities?.passwordReset.enabled ?? true;
  const isLocalMailMode = capabilities?.passwordReset.deliveryMode === 'LOCAL_SMTP';

  function validate(): boolean {
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
    return true;
  }

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setError('');

    if (!isPasswordLoginEnabled) {
      setError('현재 이 환경에서는 이메일 로그인이 비활성화되어 있습니다.');
      return;
    }

    if (!validate()) return;

    setLoading(true);
    try {
      const tokens = await login({ email, password });
      const me: MeResponse = await fetchMe();

      authLogin(tokens.accessToken, {
        id: me.id,
        email: me.email,
        nickname: me.nickname,
        role: me.role,
        phonePersonal: me.phonePersonal,
        phoneCompany: me.phoneCompany,
        job: me.job as import('@/types').UserJob | null,
        companyName: me.companyName,
        userType: me.userType as import('@/types').UserType,
        isVerified: me.isVerified,
        createdAt: me.createdAt,
      }, tokens.refreshToken);

      navigate('/', { replace: true });
    } catch (err: unknown) {
      const axiosErr = err as { response?: { status?: number } };
      if (axiosErr.response?.status === 401) {
        setError('이메일 또는 비밀번호가 일치하지 않습니다.');
      } else {
        setError('로그인에 실패했습니다. 잠시 후 다시 시도해주세요.');
      }
    } finally {
      setLoading(false);
    }
  }

  async function handleSocialLogin(provider: SocialProvider) {
    const config = PROVIDER_CONFIG[provider];
    const providerCapability = getProviderCapability(capabilities, provider);

    if (!config) return;

    if (!providerCapability?.enabled || !providerCapability.clientId || !providerCapability.redirectUri) {
      setError(`${PROVIDER_LABELS[provider]} 로그인이 현재 이 환경에서 비활성화되어 있습니다.`);
      return;
    }

    // CSRF: generate state
    const state = generateRandomString(32);
    safeSessionStorage.setItem('oauth_state', state);

    // PKCE: generate code_verifier + code_challenge
    const codeVerifier = generateRandomString(64);
    safeSessionStorage.setItem('oauth_code_verifier', codeVerifier);
    const codeChallenge = await generateCodeChallenge(codeVerifier);

    const params = new URLSearchParams({
      response_type: 'code',
      client_id: providerCapability.clientId,
      redirect_uri: providerCapability.redirectUri,
      scope: config.scope,
      state,
      code_challenge: codeChallenge,
      code_challenge_method: 'S256',
    });

    window.location.href = `${config.authUrl}?${params.toString()}`;
  }

  return (
    <div className={styles.page}>
      <div className={styles.card}>
        <h1 className={styles.title}>ATStudio</h1>
        <p className={styles.subtitle}>계정에 로그인하세요</p>

        <form className={styles.form} onSubmit={handleSubmit} noValidate>
          <div className={styles.fieldGroup}>
            <label className={styles.label} htmlFor="login-email">
              이메일
            </label>
            <input
              id="login-email"
              className={styles.input}
              type="email"
              placeholder="your@email.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              autoComplete="email"
              disabled={!isPasswordLoginEnabled}
            />
          </div>

          <div className={styles.fieldGroup}>
            <label className={styles.label} htmlFor="login-password">
              비밀번호
            </label>
            <input
              id="login-password"
              className={styles.input}
              type="password"
              placeholder="8자 이상"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              autoComplete="current-password"
              disabled={!isPasswordLoginEnabled}
            />
          </div>

          <p className={styles.errorText}>{error}</p>

          <Button
            type="submit"
            variant="primary"
            size="lg"
            disabled={!isPasswordLoginEnabled}
            loading={loading}
            className={styles.submitButton}
          >
            로그인
          </Button>
        </form>

        <div className={styles.divider}>
          <span className={styles.dividerText}>또는</span>
        </div>

        {enabledSocialProviders.length > 0 ? (
          <div className={styles.socialButtons}>
            {enabledSocialProviders.map((provider) => (
              <button
                key={provider}
                type="button"
                className={styles.socialBtn}
                onClick={() => handleSocialLogin(provider)}
              >
                {PROVIDER_LABELS[provider]} 로그인
              </button>
            ))}
          </div>
        ) : !capabilitiesLoading ? (
          <p className={styles.helperText}>이 환경에서는 소셜 로그인이 비활성화되어 있습니다.</p>
        ) : null}

        {capabilitiesError ? (
          <p className={styles.helperText}>
            {capabilitiesError} 이메일 로그인만 사용할 수 있습니다.
          </p>
        ) : null}

        {!isPasswordLoginEnabled && !capabilitiesLoading ? (
          <p className={styles.helperText}>
            현재 이 환경에서는 이메일 로그인과 회원가입이 비활성화되어 있습니다.
          </p>
        ) : null}

        {isQaBootstrapEnabled && !capabilitiesLoading ? (
          <p className={styles.helperText}>
            이 환경에서는 QA 테스트 계정이 활성화되어 있습니다. 계정은 운영자가 별도로 제공합니다.
          </p>
        ) : null}

        <div className={styles.links}>
          {isPasswordLoginEnabled ? (
            <Link to="/signup" className={styles.link}>
            회원가입
          </Link>
          ) : (
            <span className={`${styles.link} ${styles.linkDisabled}`}>회원가입 비활성화</span>
          )}
          {isPasswordResetAvailable ? (
            <Link to="/password-reset" className={styles.link}>
              비밀번호 찾기
            </Link>
          ) : (
            <span className={`${styles.link} ${styles.linkDisabled}`}>
              비밀번호 찾기 비활성화
            </span>
          )}
        </div>

        {isPasswordLoginEnabled && isLocalMailMode ? (
          <p className={styles.helperText}>
            비밀번호 재설정 메일은 현재 로컬 메일 환경에서만 확인할 수 있습니다.
          </p>
        ) : null}
      </div>
    </div>
  );
}

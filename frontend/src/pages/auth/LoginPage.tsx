import { type FormEvent, useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuthStore } from '@/store/authStore';
import { login, fetchMe } from '@/api/auth';
import type { MeResponse } from '@/api/auth';
import Button from '@/components/ui/Button';
import styles from './LoginPage.module.css';

/** Screen A-1: Login */
export default function LoginPage() {
  const navigate = useNavigate();
  const authLogin = useAuthStore((s) => s.login);
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated());

  useEffect(() => {
    if (isAuthenticated) navigate('/', { replace: true });
  }, [isAuthenticated, navigate]);

  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
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
    if (!password) {
      setError('비밀번호를 입력해주세요.');
      return false;
    }
    if (password.length < 8) {
      setError('비밀번호는 8자 이상이어야 합니다.');
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
      const tokens = await login({ email, password });

      localStorage.setItem('accessToken', tokens.accessToken);
      localStorage.setItem('refreshToken', tokens.refreshToken);

      const me: MeResponse = await fetchMe();

      authLogin(tokens.accessToken, {
        id: me.id,
        email: me.email,
        nickname: me.nickname,
        role: me.role,
        phonePersonal: me.phonePersonal,
        phoneCompany: me.phoneCompany,
        job: me.job as import('@/types').UserJob | null,
        userType: me.userType as import('@/types').UserType,
        isVerified: me.isVerified,
        createdAt: me.createdAt,
      });

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
            로그인
          </Button>
        </form>

        <div className={styles.divider}>
          <span className={styles.dividerText}>또는</span>
        </div>

        <div className={styles.socialButtons}>
          <button
            type="button"
            className={styles.socialBtn}
            onClick={() => handleSocialLogin('GOOGLE')}
          >
            Google 로그인
          </button>
          <button
            type="button"
            className={styles.socialBtn}
            onClick={() => handleSocialLogin('KAKAO')}
          >
            Kakao 로그인
          </button>
          <button
            type="button"
            className={styles.socialBtn}
            onClick={() => handleSocialLogin('NAVER')}
          >
            Naver 로그인
          </button>
        </div>

        <div className={styles.links}>
          <Link to="/signup" className={styles.link}>
            회원가입
          </Link>
          <Link to="/password-reset" className={styles.link}>
            비밀번호 찾기
          </Link>
        </div>
      </div>
    </div>
  );

  function handleSocialLogin(provider: string) {
    const providerConfig: Record<string, { authUrl: string; clientIdKey: string }> = {
      GOOGLE: {
        authUrl: 'https://accounts.google.com/o/oauth2/v2/auth',
        clientIdKey: 'google',
      },
      KAKAO: {
        authUrl: 'https://kauth.kakao.com/oauth/authorize',
        clientIdKey: 'kakao',
      },
      NAVER: {
        authUrl: 'https://nid.naver.com/oauth2.0/authorize',
        clientIdKey: 'naver',
      },
    };

    const config = providerConfig[provider];
    if (!config) return;

    const redirectUri = `${window.location.origin}/social-login/${provider.toLowerCase()}`;
    const params = new URLSearchParams({
      response_type: 'code',
      redirect_uri: redirectUri,
      scope: provider === 'GOOGLE' ? 'email profile' : '',
    });

    // Client IDs should be injected via environment — placeholder alert for unconfigured
    window.location.href = `${config.authUrl}?${params.toString()}`;
  }
}

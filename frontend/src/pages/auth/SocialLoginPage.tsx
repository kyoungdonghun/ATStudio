/** Screen A-3: Social login callback — processes OAuth authorization code */
import { useEffect, useState } from 'react';
import { useNavigate, useParams, useSearchParams } from 'react-router-dom';
import { socialLogin, fetchMe, type MeResponse } from '@/api/auth';
import { useAuthStore } from '@/store/authStore';
import type { UserJob, UserType } from '@/types';
import styles from './LoginPage.module.css';

export default function SocialLoginPage() {
  const navigate = useNavigate();
  const { provider } = useParams<{ provider: string }>();
  const [searchParams] = useSearchParams();
  const code = searchParams.get('code');
  const returnedState = searchParams.get('state');
  const authLogin = useAuthStore((s) => s.login);

  const [error, setError] = useState('');

  useEffect(() => {
    if (!provider || !code) {
      setError('잘못된 접근입니다.');
      return;
    }

    // CSRF: verify state parameter
    const savedState = sessionStorage.getItem('oauth_state');
    sessionStorage.removeItem('oauth_state');
    if (!savedState || savedState !== returnedState) {
      setError('보안 검증에 실패했습니다. 다시 로그인해주세요.');
      return;
    }

    // PKCE: retrieve code_verifier
    const codeVerifier = sessionStorage.getItem('oauth_code_verifier');
    sessionStorage.removeItem('oauth_code_verifier');

    let cancelled = false;

    (async () => {
      try {
        const res = await socialLogin(provider, code, codeVerifier);

        localStorage.setItem('accessToken', res.accessToken);
        localStorage.setItem('refreshToken', res.refreshToken);

        if (!res.isProfileComplete) {
          navigate('/complete-profile', { replace: true });
          return;
        }

        const me: MeResponse = await fetchMe();
        if (cancelled) return;

        authLogin(res.accessToken, {
          id: me.id,
          email: me.email,
          nickname: me.nickname,
          role: me.role,
          phonePersonal: me.phonePersonal,
          phoneCompany: me.phoneCompany,
          job: me.job as UserJob | null,
          userType: me.userType as UserType,
          isVerified: me.isVerified,
          createdAt: me.createdAt,
        });

        navigate('/', { replace: true });
      } catch (err: unknown) {
        if (cancelled) return;
        const msg =
          (err as { response?: { data?: { message?: string } } })?.response
            ?.data?.message ?? '소셜 로그인에 실패했습니다.';
        setError(msg);
      }
    })();

    return () => {
      cancelled = true;
    };
  }, [provider, code, returnedState, navigate, authLogin]);

  return (
    <div className={styles.page}>
      <div className={styles.card}>
        <h1 className={styles.title}>ATStudio</h1>
        {error ? (
          <>
            <p className={styles.errorText}>{error}</p>
            <div className={styles.links}>
              <button className={styles.link} onClick={() => navigate('/login')}>
                로그인으로 돌아가기
              </button>
            </div>
          </>
        ) : (
          <p className={styles.subtitle}>소셜 로그인 처리 중...</p>
        )}
      </div>
    </div>
  );
}

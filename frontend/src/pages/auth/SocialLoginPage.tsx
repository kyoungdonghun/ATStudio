/** Screen A-3: Social login callback — processes OAuth authorization code */
import { useEffect, useRef, useState } from 'react';
import { useNavigate, useParams, useSearchParams } from 'react-router-dom';
import { socialLogin, fetchMe, type MeResponse } from '@/api/auth';
import { getSocialLoginErrorMessage } from '@/api/authError';
import { useAuthStore } from '@/store/authStore';
import { consumeOAuthCallbackAttempt, storeOAuthProfileReturnTarget } from '@/utils/oauthAttempt';
import type { UserJob, UserType } from '@/types';
import { getAccessibleLoginReturnTarget } from '@/utils/loginReturn';
import styles from './LoginPage.module.css';

export default function SocialLoginPage() {
  const navigate = useNavigate();
  const { provider } = useParams<{ provider: string }>();
  const [searchParams] = useSearchParams();
  const code = searchParams.get('code');
  const returnedState = searchParams.get('state');
  const stageTokens = useAuthStore((s) => s.stageTokens);
  const authLogin = useAuthStore((s) => s.login);
  const authLogout = useAuthStore((s) => s.logout);
  const clearSession = useAuthStore((s) => s.clearSession);

  const [error, setError] = useState('');
  const processed = useRef(false);

  useEffect(() => {
    if (processed.current) return;
    processed.current = true;

    if (!provider || !code) {
      setError('잘못된 접근입니다.');
      return;
    }

    const attempt = consumeOAuthCallbackAttempt(returnedState);
    if (!attempt) {
      setError('보안 검증에 실패했습니다. 다시 로그인해주세요.');
      return;
    }

    (async () => {
      let tokensStaged = false;
      try {
        const res = await socialLogin(provider, code, attempt.codeVerifier);
        stageTokens(res.accessToken, res.refreshToken);
        tokensStaged = true;

        const me: MeResponse = await fetchMe(res.accessToken);

        authLogin(
          res.accessToken,
          {
            id: me.id,
            email: me.email,
            nickname: me.nickname,
            role: me.role,
            phonePersonal: me.phonePersonal,
            phoneCompany: me.phoneCompany,
            job: me.job as UserJob | null,
            companyName: me.companyName,
            userType: me.userType as UserType,
            isVerified: me.isVerified,
            createdAt: me.createdAt,
          },
          res.refreshToken,
        );

        if (!res.isProfileComplete) {
          const continuationStored = storeOAuthProfileReturnTarget(
            attempt.attemptId,
            attempt.returnTarget,
            me.id,
          );
          if (!continuationStored) {
            navigate('/complete-profile', { replace: true });
            return;
          }
          navigate('/complete-profile', { replace: true });
          return;
        }

        navigate(getAccessibleLoginReturnTarget(attempt.returnTarget, me) ?? '/', {
          replace: true,
        });
      } catch (err: unknown) {
        if (tokensStaged) {
          await authLogout();
        } else {
          clearSession();
        }

        setError(getSocialLoginErrorMessage(err));
      }
    })();
  }, [provider, code, returnedState, navigate, stageTokens, authLogin, authLogout, clearSession]);

  return (
    <div className={styles.page}>
      <div className={styles.card}>
        <h1 className={styles.title}>AT.M</h1>
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

/** Screen A-4: Social user complete profile */
import { type FormEvent, useCallback, useEffect, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '@/store/authStore';
import { fetchMe, checkNicknameAvailability, checkPhoneAvailability } from '@/api/auth';
import type { MeResponse } from '@/api/auth';
import type { UserJob, UserType } from '@/types';
import client from '@/api/client';
import { getCompleteProfileErrorMessage } from '@/api/authError';
import { getApiErrorCode, getLoadErrorMessage } from '@/api/loadError';
import {
  COMPANY_NAME_MAX,
  formatPhone,
  isValidNickname,
  isValidPhone,
  NICKNAME_MAX,
} from '@/utils/validation';
import Button from '@/components/ui/Button';
import { consumeOAuthProfileReturnTarget } from '@/utils/oauthAttempt';
import { getAccessibleLoginReturnTarget } from '@/utils/loginReturn';
import styles from './SignupPage.module.css';

const JOB_OPTIONS = [
  { value: '', label: '직업을 선택하세요' },
  { value: 'EDITOR', label: '편집자' },
  { value: 'ARTIST', label: '아티스트' },
  { value: 'FREELANCER', label: '프리랜서' },
];

interface CompleteProfileRequest {
  nickname: string;
  phonePersonal: string;
  phoneCompany: string | null;
  job: UserJob | null;
  companyName: string | null;
  userType: UserType;
}

type IdentityState = 'checking' | 'incomplete' | 'error';

function isProfileComplete(profile: MeResponse): boolean {
  if (!profile.phonePersonal?.trim()) return false;
  if (profile.userType === 'BUSINESS') return Boolean(profile.companyName?.trim());
  return profile.job !== null;
}

export default function SocialCompleteProfilePage() {
  const navigate = useNavigate();
  const refreshCurrentUser = useAuthStore((s) => s.refreshCurrentUser);
  const accessToken = useAuthStore((s) => s.accessToken);

  const [userType, setUserType] = useState<UserType>('INDIVIDUAL');
  const [nickname, setNickname] = useState('');
  const [phonePersonal, setPhonePersonal] = useState('');
  const [phoneCompany, setPhoneCompany] = useState('');
  const [job, setJob] = useState<UserJob | ''>('');
  const [companyName, setCompanyName] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [identityState, setIdentityState] = useState<IdentityState>('checking');
  const [identityError, setIdentityError] = useState('');
  const identityRequestIdRef = useRef(0);
  const submitPendingRef = useRef(false);
  const mountedRef = useRef(true);
  const normalizedNickname = nickname.trim();
  const normalizedCompanyName = companyName.trim();
  const isBusinessUser = userType === 'BUSINESS';

  const checkIdentity = useCallback(async () => {
    const requestId = ++identityRequestIdRef.current;
    setIdentityState('checking');
    setIdentityError('');

    try {
      const me = await fetchMe();
      if (requestId !== identityRequestIdRef.current) return;

      if (isProfileComplete(me)) {
        navigate('/profile?tab=account', { replace: true });
        return;
      }

      setIdentityState('incomplete');
    } catch (identityLoadError: unknown) {
      if (requestId !== identityRequestIdRef.current) return;

      setIdentityError(getLoadErrorMessage(identityLoadError, '프로필 상태'));
      setIdentityState('error');
    }
  }, [navigate]);

  useEffect(() => {
    mountedRef.current = true;
    void checkIdentity();
    return () => {
      mountedRef.current = false;
      identityRequestIdRef.current += 1;
    };
  }, [checkIdentity]);

  async function validate(): Promise<boolean> {
    if (!normalizedNickname) {
      setError('닉네임을 입력해주세요.');
      return false;
    }
    if (!isValidNickname(normalizedNickname)) {
      setError('닉네임은 2~20자의 한글, 영문, 숫자, 밑줄(_)만 사용할 수 있습니다.');
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
    if (isBusinessUser) {
      if (!normalizedCompanyName) {
        setError('기업 회원은 회사명을 입력해주세요.');
        return false;
      }
    } else if (!job) {
      setError('개인 회원은 직업을 선택해주세요.');
      return false;
    }

    try {
      const nicknameRes = await checkNicknameAvailability(normalizedNickname);
      if (!nicknameRes.available) {
        setError('이미 사용 중인 닉네임입니다.');
        return false;
      }
    } catch {
      setError('닉네임 확인 중 오류가 발생했습니다.');
      return false;
    }

    try {
      const phoneRes = await checkPhoneAvailability(phonePersonal);
      if (!phoneRes.available) {
        setError('이미 등록된 전화번호입니다.');
        return false;
      }
    } catch {
      setError('전화번호 확인 중 오류가 발생했습니다.');
      return false;
    }

    return true;
  }

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    if (submitPendingRef.current || identityState !== 'incomplete') return;

    submitPendingRef.current = true;
    setLoading(true);
    setError('');

    try {
      const valid = await validate();
      if (!valid) return;
      if (!accessToken) {
        setError('로그인 상태를 확인하지 못했습니다. 다시 로그인해주세요.');
        return;
      }

      const submitJob: UserJob | null = isBusinessUser ? null : job || null;
      const body: CompleteProfileRequest = {
        nickname: normalizedNickname,
        phonePersonal,
        phoneCompany: phoneCompany.trim() || null,
        job: submitJob,
        companyName: isBusinessUser ? normalizedCompanyName : null,
        userType,
      };

      await client.put('/users/me/complete-profile', body);

      const refreshedUser = await refreshCurrentUser();
      const currentSession = useAuthStore.getState();
      if (
        !mountedRef.current ||
        currentSession.accessToken === null ||
        currentSession.user?.id !== refreshedUser.id
      ) {
        return;
      }

      navigate(
        getAccessibleLoginReturnTarget(
          consumeOAuthProfileReturnTarget(refreshedUser.id),
          refreshedUser,
        ) ?? '/',
        { replace: true },
      );
    } catch (submitError: unknown) {
      if (!mountedRef.current) return;
      if (getApiErrorCode(submitError) === 'PROFILE_ALREADY_COMPLETE') {
        navigate('/profile?tab=account', { replace: true });
        return;
      }
      setError(getCompleteProfileErrorMessage(submitError));
    } finally {
      submitPendingRef.current = false;
      if (mountedRef.current) setLoading(false);
    }
  }

  if (identityState === 'checking') {
    return (
      <div className={styles.page}>
        <div className={styles.card}>
          <h1 className={styles.title}>프로필 완성</h1>
          <p className={styles.subtitle}>프로필 상태를 확인하는 중...</p>
        </div>
      </div>
    );
  }

  if (identityState === 'error') {
    return (
      <div className={styles.page}>
        <div className={styles.card}>
          <h1 className={styles.title}>프로필 완성</h1>
          <p className={styles.errorText} role="alert">
            {identityError}
          </p>
          <Button
            type="button"
            variant="primary"
            size="lg"
            className={styles.submitButton}
            onClick={() => void checkIdentity()}
          >
            다시 시도
          </Button>
        </div>
      </div>
    );
  }

  return (
    <div className={styles.page}>
      <div className={styles.card}>
        <h1 className={styles.title}>프로필 완성</h1>
        <p className={styles.subtitle}>서비스 이용을 위해 추가 정보를 입력해주세요</p>

        <form className={styles.form} onSubmit={handleSubmit} noValidate>
          {/* User type */}
          <div className={styles.fieldGroup}>
            <span className={styles.label} id="complete-profile-user-type-label">
              회원 유형
            </span>
            <div
              aria-labelledby="complete-profile-user-type-label"
              className={styles.roleToggle}
              role="group"
            >
              <button
                type="button"
                className={userType === 'INDIVIDUAL' ? styles.roleOptionActive : styles.roleOption}
                onClick={() => setUserType('INDIVIDUAL')}
                disabled={loading}
                aria-pressed={userType === 'INDIVIDUAL'}
              >
                개인
              </button>
              <button
                type="button"
                className={userType === 'BUSINESS' ? styles.roleOptionActive : styles.roleOption}
                onClick={() => setUserType('BUSINESS')}
                disabled={loading}
                aria-pressed={userType === 'BUSINESS'}
              >
                기업
              </button>
            </div>
          </div>

          {/* Nickname */}
          <div className={styles.fieldGroup}>
            <label className={styles.label} htmlFor="cp-nickname">
              닉네임
            </label>
            <input
              id="cp-nickname"
              className={styles.input}
              type="text"
              placeholder="닉네임"
              value={nickname}
              onChange={(e) => setNickname(e.target.value)}
              maxLength={NICKNAME_MAX}
              disabled={loading}
            />
          </div>

          {/* Phone */}
          <div className={styles.fieldGroup}>
            <label className={styles.label} htmlFor="cp-phone">
              연락처
            </label>
            <input
              id="cp-phone"
              className={styles.input}
              type="tel"
              placeholder="010-0000-0000"
              value={phonePersonal}
              onChange={(e) => setPhonePersonal(formatPhone(e.target.value))}
              disabled={loading}
            />
          </div>

          {/* Company phone (business only) */}
          {userType === 'BUSINESS' && (
            <div className={styles.fieldGroup}>
              <label className={styles.label} htmlFor="cp-company-phone">
                회사 연락처 (선택)
              </label>
              <input
                id="cp-company-phone"
                className={styles.input}
                type="tel"
                placeholder="02-0000-0000"
                value={phoneCompany}
                onChange={(e) => setPhoneCompany(formatPhone(e.target.value))}
                disabled={loading}
              />
            </div>
          )}

          {isBusinessUser ? (
            <div className={styles.fieldGroup}>
              <label className={styles.label} htmlFor="cp-company-name">
                회사명
              </label>
              <input
                id="cp-company-name"
                className={styles.input}
                type="text"
                placeholder="회사명을 입력하세요"
                value={companyName}
                onChange={(e) => setCompanyName(e.target.value)}
                maxLength={COMPANY_NAME_MAX}
                disabled={loading}
              />
            </div>
          ) : (
            <div className={styles.fieldGroup}>
              <label className={styles.label} htmlFor="cp-job">
                직업
              </label>
              <select
                id="cp-job"
                className={styles.input}
                value={job}
                onChange={(e) => setJob(e.target.value as UserJob | '')}
                disabled={loading}
              >
                {JOB_OPTIONS.map((opt) => (
                  <option key={opt.value} value={opt.value}>
                    {opt.label}
                  </option>
                ))}
              </select>
            </div>
          )}

          <p className={styles.errorText} role="alert">
            {error}
          </p>

          <Button
            type="submit"
            variant="primary"
            size="lg"
            loading={loading}
            className={styles.submitButton}
          >
            완료
          </Button>
        </form>
      </div>
    </div>
  );
}

/** Screen A-4: Social user complete profile */
import { type FormEvent, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '@/store/authStore';
import { fetchMe, checkNicknameAvailability, checkPhoneAvailability } from '@/api/auth';
import type { MeResponse } from '@/api/auth';
import type { UserJob, UserType } from '@/types';
import client from '@/api/client';
import { formatPhone, isValidNickname, isValidPhone, NICKNAME_MAX } from '@/utils/validation';
import Button from '@/components/ui/Button';
import styles from './SignupPage.module.css';

type UserTypeOption = 'INDIVIDUAL' | 'BUSINESS';

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
  job: string;
  userType: UserTypeOption;
}

export default function SocialCompleteProfilePage() {
  const navigate = useNavigate();
  const authLogin = useAuthStore((s) => s.login);
  const accessToken = useAuthStore((s) => s.accessToken);

  const [userType, setUserType] = useState<UserTypeOption>('INDIVIDUAL');
  const [nickname, setNickname] = useState('');
  const [phonePersonal, setPhonePersonal] = useState('');
  const [phoneCompany, setPhoneCompany] = useState('');
  const [job, setJob] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  async function validate(): Promise<boolean> {
    if (!nickname.trim()) {
      setError('닉네임을 입력해주세요.');
      return false;
    }
    if (!isValidNickname(nickname)) {
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
    if (!job) {
      setError('직업을 선택해주세요.');
      return false;
    }

    try {
      const nicknameRes = await checkNicknameAvailability(nickname);
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
    setError('');

    const valid = await validate();
    if (!valid) return;

    setLoading(true);
    try {
      const body: CompleteProfileRequest = {
        nickname,
        phonePersonal,
        phoneCompany: phoneCompany.trim() || null,
        job,
        userType,
      };

      await client.put('/users/me/complete-profile', body);

      const me: MeResponse = await fetchMe();
      authLogin(accessToken!, {
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
      const msg =
        (err as { response?: { data?: { message?: string } } })?.response?.data
          ?.message ?? '프로필 완성에 실패했습니다.';
      setError(msg);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className={styles.page}>
      <div className={styles.card}>
        <h1 className={styles.title}>프로필 완성</h1>
        <p className={styles.subtitle}>서비스 이용을 위해 추가 정보를 입력해주세요</p>

        <form className={styles.form} onSubmit={handleSubmit} noValidate>
          {/* User type */}
          <div className={styles.fieldGroup}>
            <label className={styles.label}>회원 유형</label>
            <div className={styles.roleToggle}>
              <button
                type="button"
                className={userType === 'INDIVIDUAL' ? styles.roleOptionActive : styles.roleOption}
                onClick={() => setUserType('INDIVIDUAL')}
              >
                개인
              </button>
              <button
                type="button"
                className={userType === 'BUSINESS' ? styles.roleOptionActive : styles.roleOption}
                onClick={() => setUserType('BUSINESS')}
              >
                기업
              </button>
            </div>
          </div>

          {/* Nickname */}
          <div className={styles.fieldGroup}>
            <label className={styles.label} htmlFor="cp-nickname">닉네임</label>
            <input
              id="cp-nickname"
              className={styles.input}
              type="text"
              placeholder="닉네임"
              value={nickname}
              onChange={(e) => setNickname(e.target.value)}
              maxLength={NICKNAME_MAX}
            />
          </div>

          {/* Phone */}
          <div className={styles.fieldGroup}>
            <label className={styles.label} htmlFor="cp-phone">연락처</label>
            <input
              id="cp-phone"
              className={styles.input}
              type="tel"
              placeholder="010-0000-0000"
              value={phonePersonal}
              onChange={(e) => setPhonePersonal(formatPhone(e.target.value))}
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
              />
            </div>
          )}

          {/* Job */}
          <div className={styles.fieldGroup}>
            <label className={styles.label} htmlFor="cp-job">직업</label>
            <select
              id="cp-job"
              className={styles.input}
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

          <p className={styles.errorText}>{error}</p>

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

import { useState, useEffect } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import {
  checkNicknameAvailability,
  checkPhoneAvailability,
  fetchMe,
  type MeResponse,
} from '@/api/auth';
import { fetchMySubscription, type MySubscription } from '@/api/userSubscriptions';
import client from '@/api/client';
import { formatDate } from '@/utils/format';
import type { ApiResponse, UserJob } from '@/types';
import {
  COMPANY_NAME_MAX,
  formatPhone,
  isValidNickname,
  isValidPhone,
  NICKNAME_MAX,
  PASSWORD_MIN,
} from '@/utils/validation';
import Button from '@/components/ui/Button';
import styles from './ProfilePage.module.css';

const SUB_STATUS_LABELS: Record<string, string> = {
  ACTIVE: '활성',
  CANCELLED: '취소됨',
  EXPIRED: '만료됨',
};

const PLAN_NAME_LABELS: Record<string, string> = {
  STANDARD: '스탠다드',
  DELUXE: '디럭스',
  PREMIUM: '프리미엄',
};

const USER_TYPE_LABELS: Record<string, string> = {
  INDIVIDUAL: '개인',
  BUSINESS: '기업',
};

const JOB_LABELS: Record<string, string> = {
  EDITOR: '편집자',
  ARTIST: '아티스트',
  FREELANCER: '프리랜서',
};

const JOB_OPTIONS: Array<{ value: UserJob; label: string }> = [
  { value: 'EDITOR', label: '편집자' },
  { value: 'ARTIST', label: '아티스트' },
  { value: 'FREELANCER', label: '프리랜서' },
];

type TabKey = 'account' | 'subscription' | 'edit' | 'password' | 'likes' | 'downloads' | 'playlists' | 'history' | 'licenses';

interface MenuItem {
  key: TabKey;
  label: string;
  group: string;
}

const MENU_ITEMS: MenuItem[] = [
  { key: 'account', label: '계정 정보', group: '계정 관리' },
  { key: 'edit', label: '프로필 수정', group: '계정 관리' },
  { key: 'password', label: '비밀번호 변경', group: '계정 관리' },
  { key: 'subscription', label: '구독 관리', group: '구독' },
  { key: 'likes', label: '좋아요', group: '내 활동' },
  { key: 'downloads', label: '다운로드 기록', group: '내 활동' },
  { key: 'playlists', label: '재생목록', group: '내 활동' },
  { key: 'history', label: '재생기록', group: '내 활동' },
  { key: 'licenses', label: '라이선스', group: '내 활동' },
];

export default function ProfilePage() {
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();
  const activeTab = (searchParams.get('tab') as TabKey) || 'account';

  /* ── Profile State ── */
  const [profile, setProfile] = useState<MeResponse | null>(null);
  const [mySub, setMySub] = useState<MySubscription | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  /* ── Edit Profile ── */
  const [nickname, setNickname] = useState('');
  const [phonePersonal, setPhonePersonal] = useState('');
  const [phoneCompany, setPhoneCompany] = useState('');
  const [job, setJob] = useState<UserJob | ''>('');
  const [companyName, setCompanyName] = useState('');
  const [savingProfile, setSavingProfile] = useState(false);
  const [profileMsg, setProfileMsg] = useState<string | null>(null);
  const [profileError, setProfileError] = useState<string | null>(null);

  /* ── Change Password ── */
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [savingPassword, setSavingPassword] = useState(false);
  const [passwordMsg, setPasswordMsg] = useState<string | null>(null);
  const [passwordError, setPasswordError] = useState<string | null>(null);

  /* ── Load profile ── */
  useEffect(() => {
    let cancelled = false;

    async function load() {
      try {
        setLoading(true);
        setError(null);
        const me = await fetchMe();
        if (!cancelled) {
          setProfile(me);
          setNickname(me.nickname);
        }
        try {
          const sub = await fetchMySubscription();
          if (!cancelled) setMySub(sub);
        } catch {
          /* no subscription */
        }
      } catch (err) {
        if (!cancelled) {
          setError(
            err instanceof Error ? err.message : '프로필을 불러오지 못했습니다.',
          );
        }
      } finally {
        if (!cancelled) setLoading(false);
      }
    }

    load();
    return () => {
      cancelled = true;
    };
  }, []);

  useEffect(() => {
    if (!profile) return;
    setNickname(profile.nickname);
    setPhonePersonal(profile.phonePersonal ?? '');
    setPhoneCompany(profile.phoneCompany ?? '');
    setJob(profile.job ?? '');
    setCompanyName(profile.companyName ?? '');
  }, [profile]);

  const isBusinessUser = profile?.userType === 'BUSINESS';
  const normalizedNickname = nickname.trim();
  const normalizedPhoneCompany = phoneCompany.trim();
  const normalizedCompanyName = companyName.trim();
  const originalPhonePersonal = profile?.phonePersonal ?? '';
  const originalPhoneCompany = profile?.phoneCompany ?? '';
  const originalCompanyName = profile?.companyName ?? '';
  const originalJob = profile?.job ?? '';
  const isProfileDirty = profile
    ? normalizedNickname !== profile.nickname
      || phonePersonal !== originalPhonePersonal
      || normalizedPhoneCompany !== originalPhoneCompany
      || job !== originalJob
      || normalizedCompanyName !== originalCompanyName
    : false;

  /* ── Save profile ── */
  async function handleSaveProfile() {
    if (!profile) {
      setProfileError('프로필을 다시 불러온 뒤 시도해주세요.');
      return;
    }

    setProfileMsg(null);
    setProfileError(null);

    if (!normalizedNickname) {
      setProfileError('닉네임을 입력해주세요.');
      return;
    }
    if (!isValidNickname(normalizedNickname)) {
      setProfileError('닉네임은 2~20자의 한글, 영문, 숫자, 밑줄(_)만 사용할 수 있습니다.');
      return;
    }
    if (!phonePersonal.trim()) {
      setProfileError('연락처를 입력해주세요.');
      return;
    }
    if (!isValidPhone(phonePersonal)) {
      setProfileError('올바른 전화번호 형식을 입력해주세요.');
      return;
    }
    if (isBusinessUser) {
      if (!normalizedCompanyName) {
        setProfileError('기업 회원은 회사명을 입력해주세요.');
        return;
      }
    } else if (!job) {
      setProfileError('개인 회원은 직업을 선택해주세요.');
      return;
    }

    try {
      setSavingProfile(true);
      if (normalizedNickname !== profile.nickname) {
        const nicknameCheck = await checkNicknameAvailability(normalizedNickname);
        if (!nicknameCheck.available) {
          setProfileError('이미 사용 중인 닉네임입니다.');
          return;
        }
      }
      if (phonePersonal !== originalPhonePersonal) {
        const phoneCheck = await checkPhoneAvailability(phonePersonal);
        if (!phoneCheck.available) {
          setProfileError('이미 등록된 전화번호입니다.');
          return;
        }
      }

      const response = await client.put<ApiResponse<MeResponse>>('/users/me', {
        nickname: normalizedNickname,
        phonePersonal,
        phoneCompany: isBusinessUser ? (normalizedPhoneCompany || null) : null,
        job: isBusinessUser ? null : job,
        companyName: isBusinessUser ? normalizedCompanyName : null,
      });
      setProfile(response.data.data);
      setProfileMsg('프로필이 저장되었습니다.');
    } catch (err) {
      const msg =
        (err as { response?: { data?: { message?: string } } })?.response?.data?.message
        ?? (err instanceof Error ? err.message : '프로필 저장에 실패했습니다.');
      setProfileError(msg);
    } finally {
      setSavingProfile(false);
    }
  }

  /* ── Change password ── */
  async function handleChangePassword() {
    setPasswordError(null);
    setPasswordMsg(null);

    if (!currentPassword || !newPassword) {
      setPasswordError('모든 필드를 입력해주세요.');
      return;
    }
    if (newPassword !== confirmPassword) {
      setPasswordError('새 비밀번호가 일치하지 않습니다.');
      return;
    }
    if (newPassword.length < PASSWORD_MIN) {
      setPasswordError(`비밀번호는 최소 ${PASSWORD_MIN}자 이상이어야 합니다.`);
      return;
    }

    try {
      setSavingPassword(true);
      await client.put('/users/me/password', { currentPassword, newPassword });
      setPasswordMsg('비밀번호가 변경되었습니다.');
      setCurrentPassword('');
      setNewPassword('');
      setConfirmPassword('');
    } catch (err) {
      setPasswordError(
        err instanceof Error
          ? err.message
          : '비밀번호 변경에 실패했습니다.',
      );
    } finally {
      setSavingPassword(false);
    }
  }

  function switchTab(key: TabKey) {
    setSearchParams({ tab: key }, { replace: true });
  }

  /* ── Navigation shortcuts for activity tabs ── */
  const ACTIVITY_ROUTES: Record<string, string> = {
    likes: '/likes',
    downloads: '/download-queue',
    playlists: '/playlists',
    history: '/play-history',
    licenses: '/licenses',
  };

  /* ── Render ── */

  if (loading) {
    return (
      <div className={styles.page}>
        <div className={styles.loading}>{'프로필을 불러오는 중...'}</div>
      </div>
    );
  }

  if (error || !profile) {
    return (
      <div className={styles.page}>
        <div className={styles.error}>{error ?? '프로필을 찾을 수 없습니다.'}</div>
      </div>
    );
  }

  /* ── Sidebar menu grouped ── */
  const groups = MENU_ITEMS.reduce<Record<string, MenuItem[]>>((acc, item) => {
    (acc[item.group] ??= []).push(item);
    return acc;
  }, {});

  return (
    <div className={styles.page}>
      <h1 className={styles.pageTitle}>{'내 계정'}</h1>

      <div className={styles.layout}>
        {/* ── Sidebar ── */}
        <nav className={styles.sidebar}>
          {Object.entries(groups).map(([group, items]) => (
            <div key={group} className={styles.menuGroup}>
              <div className={styles.menuGroupTitle}>{group}</div>
              {items.map((item) => (
                <button
                  key={item.key}
                  className={`${styles.menuItem} ${activeTab === item.key ? styles.menuItemActive : ''}`}
                  onClick={() => {
                    if (ACTIVITY_ROUTES[item.key]) {
                      navigate(ACTIVITY_ROUTES[item.key]);
                    } else {
                      switchTab(item.key);
                    }
                  }}
                >
                  {item.label}
                </button>
              ))}
            </div>
          ))}
        </nav>

        {/* ── Content ── */}
        <div className={styles.content}>
          {activeTab === 'account' && (
            <div className={styles.section}>
              <div className={styles.sectionTitle}>{'계정 정보'}</div>
              <div className={styles.infoRow}>
                <span className={styles.infoLabel}>{'이메일'}</span>
                <span className={styles.infoValue}>{profile.email}</span>
              </div>
              <div className={styles.infoRow}>
                <span className={styles.infoLabel}>{'유형'}</span>
                <span className={styles.infoValue}>
                  {USER_TYPE_LABELS[profile.userType] ?? profile.userType}
                </span>
              </div>
              <div className={styles.infoRow}>
                <span className={styles.infoLabel}>{'연락처'}</span>
                <span className={styles.infoValue}>{profile.phonePersonal ?? '-'}</span>
              </div>
              {profile.userType === 'BUSINESS' ? (
                <>
                  <div className={styles.infoRow}>
                    <span className={styles.infoLabel}>{'회사명'}</span>
                    <span className={styles.infoValue}>{profile.companyName ?? '-'}</span>
                  </div>
                  <div className={styles.infoRow}>
                    <span className={styles.infoLabel}>{'회사 연락처'}</span>
                    <span className={styles.infoValue}>{profile.phoneCompany ?? '-'}</span>
                  </div>
                </>
              ) : (
                <div className={styles.infoRow}>
                  <span className={styles.infoLabel}>{'직업'}</span>
                  <span className={styles.infoValue}>
                    {profile.job ? (JOB_LABELS[profile.job] ?? profile.job) : '-'}
                  </span>
                </div>
              )}
              <div className={styles.infoRow}>
                <span className={styles.infoLabel}>{'권한'}</span>
                <span className={styles.infoValue}>{profile.role}</span>
              </div>
              <div className={styles.infoRow}>
                <span className={styles.infoLabel}>{'가입일'}</span>
                <span className={styles.infoValue}>
                  {formatDate(profile.createdAt)}
                </span>
              </div>
              <div className={styles.infoRow}>
                <span className={styles.infoLabel}>{'YouTube 채널'}</span>
                <span className={styles.infoValue}>
                  <Link to="/whitelist-channels">
                    <Button variant="ghost" size="sm">
                      {'화이트리스트 채널 관리'}
                    </Button>
                  </Link>
                </span>
              </div>

              {/* 기업 회원 전용: 기업 인증 */}
              {profile.userType === 'BUSINESS' && (
                <div className={styles.infoRow}>
                  <span className={styles.infoLabel}>{'기업 인증'}</span>
                  <span className={styles.infoValue}>
                    <Link to="/company-certification/status">
                      <Button variant="ghost" size="sm">
                        {'기업 인증 관리'}
                      </Button>
                    </Link>
                  </span>
                </div>
              )}
            </div>
          )}

          {activeTab === 'subscription' && (
            <div className={styles.section}>
              <div className={styles.sectionTitle}>{'구독 정보'}</div>
              {mySub ? (
                <>
                  <div className={styles.infoRow}>
                    <span className={styles.infoLabel}>{'현재 플랜'}</span>
                    <span className={styles.infoValue}>
                      {PLAN_NAME_LABELS[mySub.subscription.name] ?? mySub.subscription.name}
                    </span>
                  </div>
                  <div className={styles.infoRow}>
                    <span className={styles.infoLabel}>{'결제 주기'}</span>
                    <span className={styles.infoValue}>
                      {mySub.billingCycle === 'YEARLY' ? '연간' : '월간'}
                    </span>
                  </div>
                  <div className={styles.infoRow}>
                    <span className={styles.infoLabel}>{'상태'}</span>
                    <span className={styles.infoValue}>
                      <span className={(mySub.status === 'ACTIVE' || mySub.status === 'CANCELLED') ? styles.statusActive : styles.statusInactive}>
                        {SUB_STATUS_LABELS[mySub.status] ?? mySub.status}
                      </span>
                    </span>
                  </div>
                  <div className={styles.infoRow}>
                    <span className={styles.infoLabel}>{'만료일'}</span>
                    <span className={styles.infoValue}>{formatDate(mySub.expiresAt)}</span>
                  </div>
                  <div className={styles.buttonRow}>
                    <Button variant="ghost" size="sm" onClick={() => navigate('/subscriptions/manage')}>
                      {'구독 관리'}
                    </Button>
                  </div>
                </>
              ) : (
                <div className={styles.noSub}>
                  <p>{'현재 구독 중인 플랜이 없습니다.'}</p>
                  {profile.role !== 'ADMIN' && (
                    <Button variant="primary" size="sm" onClick={() => navigate('/subscriptions')}>
                      {'구독 시작하기'}
                    </Button>
                  )}
                </div>
              )}
            </div>
          )}

          {activeTab === 'edit' && (
            <div className={styles.section}>
              <div className={styles.sectionTitle}>{'프로필 수정'}</div>
              <div className={styles.formGrid}>
                <div className={styles.formGroup}>
                  <label className={styles.formLabel} htmlFor="profile-user-type">
                    {'회원 유형'}
                  </label>
                  <input
                    id="profile-user-type"
                    className={styles.formInput}
                    type="text"
                    value={USER_TYPE_LABELS[profile.userType] ?? profile.userType}
                    disabled
                  />
                  <div className={styles.formHint}>
                    {'회원 유형은 변경할 수 없습니다.'}
                  </div>
                </div>
              </div>
              <div className={styles.formGroup}>
                <label className={styles.formLabel} htmlFor="profile-nickname">
                  {'닉네임'}
                </label>
                <input
                  id="profile-nickname"
                  className={styles.formInput}
                  type="text"
                  value={nickname}
                  onChange={(e) => setNickname(e.target.value)}
                  maxLength={NICKNAME_MAX}
                />
                <div className={styles.formHint}>
                  {'닉네임을 변경하면 저장 전에 중복 확인이 수행됩니다.'}
                </div>
              </div>
              <div className={styles.formGrid}>
                <div className={styles.formGroup}>
                  <label className={styles.formLabel} htmlFor="profile-phone-personal">
                    {'연락처'}
                  </label>
                  <input
                    id="profile-phone-personal"
                    className={styles.formInput}
                    type="tel"
                    value={phonePersonal}
                    onChange={(e) => setPhonePersonal(formatPhone(e.target.value))}
                    placeholder="010-0000-0000"
                  />
                </div>
                {isBusinessUser && (
                  <div className={styles.formGroup}>
                    <label className={styles.formLabel} htmlFor="profile-phone-company">
                      {'회사 연락처'}
                    </label>
                    <input
                      id="profile-phone-company"
                      className={styles.formInput}
                      type="tel"
                      value={phoneCompany}
                      onChange={(e) => setPhoneCompany(formatPhone(e.target.value))}
                      placeholder="02-0000-0000"
                    />
                  </div>
                )}
              </div>
              {isBusinessUser ? (
                <div className={styles.formGroup}>
                  <label className={styles.formLabel} htmlFor="profile-company-name">
                    {'회사명'}
                  </label>
                  <input
                    id="profile-company-name"
                    className={styles.formInput}
                    type="text"
                    value={companyName}
                    onChange={(e) => setCompanyName(e.target.value)}
                    maxLength={COMPANY_NAME_MAX}
                    placeholder="회사명"
                  />
                  <div className={styles.formHint}>
                    {'기업 회원은 회사명이 비어 있으면 저장할 수 없습니다.'}
                  </div>
                </div>
              ) : (
                <div className={styles.formGroup}>
                  <label className={styles.formLabel} htmlFor="profile-job">
                    {'직업'}
                  </label>
                  <select
                    id="profile-job"
                    className={styles.formInput}
                    value={job}
                    onChange={(e) => setJob(e.target.value as UserJob | '')}
                  >
                    <option value="">{'직업을 선택하세요'}</option>
                    {JOB_OPTIONS.map((option) => (
                      <option key={option.value} value={option.value}>
                        {option.label}
                      </option>
                    ))}
                  </select>
                </div>
              )}
              <div className={styles.buttonRow}>
                <Button
                  variant="primary"
                  onClick={handleSaveProfile}
                  loading={savingProfile}
                  disabled={!isProfileDirty}
                >
                  {'저장'}
                </Button>
              </div>
              <div className={styles.formHint}>
                {'YouTube 채널 정보는 화이트리스트 채널 관리 화면에서 언제든지 추가하거나 수정할 수 있습니다.'}
                {' '}
                <Link to="/whitelist-channels">{'채널 관리로 이동'}</Link>
              </div>
              {profileMsg && <div className={styles.successMsg}>{profileMsg}</div>}
              {profileError && <div className={styles.errorMsg}>{profileError}</div>}
            </div>
          )}

          {activeTab === 'password' && (
            <div className={styles.section}>
              <div className={styles.sectionTitle}>{'비밀번호 변경'}</div>
              <div className={styles.formGroup}>
                <label className={styles.formLabel}>{'현재 비밀번호'}</label>
                <input
                  className={styles.formInput}
                  type="password"
                  value={currentPassword}
                  onChange={(e) => setCurrentPassword(e.target.value)}
                />
              </div>
              <div className={styles.formGroup}>
                <label className={styles.formLabel}>{'새 비밀번호'}</label>
                <input
                  className={styles.formInput}
                  type="password"
                  value={newPassword}
                  onChange={(e) => setNewPassword(e.target.value)}
                />
              </div>
              <div className={styles.formGroup}>
                <label className={styles.formLabel}>{'새 비밀번호 확인'}</label>
                <input
                  className={styles.formInput}
                  type="password"
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                />
              </div>
              <div className={styles.buttonRow}>
                <Button
                  variant="primary"
                  onClick={handleChangePassword}
                  loading={savingPassword}
                  disabled={!currentPassword || !newPassword}
                >
                  {'비밀번호 변경'}
                </Button>
              </div>
              {passwordMsg && (
                <div className={styles.successMsg}>{passwordMsg}</div>
              )}
              {passwordError && (
                <div className={styles.errorMsg}>{passwordError}</div>
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

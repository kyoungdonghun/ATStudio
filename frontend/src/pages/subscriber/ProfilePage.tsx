import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { fetchMe, type MeResponse } from '@/api/auth';
import { fetchMySubscription, type MySubscription } from '@/api/userSubscriptions';
import client from '@/api/client';
import { formatDate } from '@/utils/format';
import Button from '@/components/ui/Button';
import styles from './ProfilePage.module.css';

const SUB_STATUS_LABELS: Record<string, string> = {
  ACTIVE: '활성',
  CANCELLED: '취소됨',
  EXPIRED: '만료됨',
};

const PLAN_NAME_LABELS: Record<string, string> = {
  STANDARD: 'Starter',
  PRO: 'Pro',
  PREMIUM: 'Business',
};

export default function ProfilePage() {
  const navigate = useNavigate();

  /* ── Profile State ── */
  const [profile, setProfile] = useState<MeResponse | null>(null);
  const [mySub, setMySub] = useState<MySubscription | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  /* ── Edit Nickname ── */
  const [nickname, setNickname] = useState('');
  const [savingProfile, setSavingProfile] = useState(false);
  const [profileMsg, setProfileMsg] = useState<string | null>(null);

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

  /* ── Save profile (nickname) ── */
  async function handleSaveProfile() {
    if (!nickname.trim()) return;
    try {
      setSavingProfile(true);
      setProfileMsg(null);
      await client.put('/users/me', {
        nickname: nickname.trim(),
        phonePersonal: profile?.phonePersonal,
        phoneCompany: profile?.phoneCompany,
        job: profile?.job,
      });
      setProfileMsg('프로필이 저장되었습니다.');
    } catch (err) {
      setProfileMsg(
        err instanceof Error ? err.message : '프로필 저장에 실패했습니다.',
      );
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
    if (newPassword.length < 8) {
      setPasswordError('비밀번호는 최소 8자 이상이어야 합니다.');
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

  return (
    <div className={styles.page}>
      <h1 className={styles.pageTitle}>{'내 계정'}</h1>

      {/* ── Account Info (read-only) ── */}
      <div className={styles.section}>
        <div className={styles.sectionTitle}>{'계정 정보'}</div>
        <div className={styles.infoRow}>
          <span className={styles.infoLabel}>{'이메일'}</span>
          <span className={styles.infoValue}>{profile.email}</span>
        </div>
        <div className={styles.infoRow}>
          <span className={styles.infoLabel}>{'유형'}</span>
          <span className={styles.infoValue}>{profile.userType}</span>
        </div>
        <div className={styles.infoRow}>
          <span className={styles.infoLabel}>{'직업'}</span>
          <span className={styles.infoValue}>{profile.job}</span>
        </div>
        <div className={styles.infoRow}>
          <span className={styles.infoLabel}>{'가입일'}</span>
          <span className={styles.infoValue}>
            {formatDate(profile.createdAt)}
          </span>
        </div>
      </div>

      {/* ── Subscription Info ── */}
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
                <span className={mySub.status === 'ACTIVE' ? styles.statusActive : styles.statusInactive}>
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
            <Button variant="primary" size="sm" onClick={() => navigate('/subscriptions')}>
              {'구독 시작하기'}
            </Button>
          </div>
        )}
      </div>

      {/* ── Edit Nickname ── */}
      <div className={styles.section}>
        <div className={styles.sectionTitle}>{'프로필 수정'}</div>
        <div className={styles.formGroup}>
          <label className={styles.formLabel}>{'닉네임'}</label>
          <input
            className={styles.formInput}
            type="text"
            value={nickname}
            onChange={(e) => setNickname(e.target.value)}
            maxLength={30}
          />
        </div>
        <div className={styles.buttonRow}>
          <Button
            variant="primary"
            onClick={handleSaveProfile}
            loading={savingProfile}
            disabled={!nickname.trim() || nickname === profile.nickname}
          >
            {'저장'}
          </Button>
        </div>
        {profileMsg && (
          <div className={styles.successMsg}>{profileMsg}</div>
        )}
      </div>

      {/* ── Change Password ── */}
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
    </div>
  );
}

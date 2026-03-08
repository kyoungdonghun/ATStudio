import { useState, useEffect } from 'react';
import { fetchMe, type MeResponse } from '@/api/auth';
import client from '@/api/client';
import Button from '@/components/ui/Button';
import styles from './ProfilePage.module.css';

export default function ProfilePage() {
  /* ── Profile State ── */
  const [profile, setProfile] = useState<MeResponse | null>(null);
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
        const { data } = await fetchMe();
        if (!cancelled) {
          setProfile(data);
          setNickname(data.nickname);
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
            {profile.createdAt.slice(0, 10)}
          </span>
        </div>
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

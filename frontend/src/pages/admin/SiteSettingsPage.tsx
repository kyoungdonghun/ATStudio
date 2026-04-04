/** Screen K-10: Site settings (admin) */
import { useEffect, useState, useCallback } from 'react';
import { getSetting, updateSetting } from '@/api/settings';
import { useToastStore } from '@/store/toastStore';
import Button from '@/components/ui/Button';
import styles from './SiteSettingsPage.module.css';

const SETTING_KEY = 'COMPANY_CERT_GUIDE';

export default function SiteSettingsPage() {
  const [value, setValue] = useState('');
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const showToast = useToastStore((s) => s.show);

  const loadSetting = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const text = await getSetting(SETTING_KEY);
      setValue(text);
    } catch {
      setError('설정을 불러오는 데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadSetting();
  }, [loadSetting]);

  async function handleSave() {
    setSaving(true);
    try {
      await updateSetting(SETTING_KEY, value);
      showToast('success', '설정이 저장되었습니다.');
    } catch {
      showToast('error', '설정 저장에 실패했습니다.');
    } finally {
      setSaving(false);
    }
  }

  if (loading) {
    return (
      <div className={styles.page}>
        <div className={styles.loading}>{'Loading...'}</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className={styles.page}>
        <div className={styles.error}>{error}</div>
      </div>
    );
  }

  return (
    <div className={styles.page}>
      <div className={styles.pageHeader}>
        <h1 className={styles.title}>{'사이트 설정'}</h1>
      </div>

      <div className={styles.card}>
        <h2 className={styles.cardTitle}>{'기업 인증 가이드'}</h2>
        <p className={styles.cardDesc}>
          {'기업 인증 신청 페이지에 표시되는 안내 문구입니다. 줄바꿈으로 항목을 구분합니다.'}
        </p>
        <textarea
          className={styles.textarea}
          value={value}
          onChange={(e) => setValue(e.target.value)}
          placeholder={'예: 필요 서류 안내\n1. 사업자등록증 사본\n2. 법인인감증명서'}
        />
        <div className={styles.actions}>
          <Button variant="ghost" onClick={loadSetting} disabled={saving}>
            {'초기화'}
          </Button>
          <Button onClick={handleSave} loading={saving}>
            {'저장'}
          </Button>
        </div>
      </div>
    </div>
  );
}

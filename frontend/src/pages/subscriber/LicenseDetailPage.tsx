import { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { fetchLicenseDetail, type LicenseDetail } from '@/api/licenses';
import { formatDate } from '@/utils/format';
import styles from './LicenseDetailPage.module.css';

export default function LicenseDetailPage() {
  const { licenseId } = useParams<{ licenseId: string }>();
  const id = Number(licenseId);

  /* ── State ── */
  const [detail, setDetail] = useState<LicenseDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  /* ── Fetch ── */
  useEffect(() => {
    if (!id || isNaN(id)) return;
    let cancelled = false;

    async function load() {
      try {
        setLoading(true);
        setError(null);
        const data = await fetchLicenseDetail(id);
        if (!cancelled) setDetail(data);
      } catch (err) {
        if (!cancelled) {
          setError(
            err instanceof Error
              ? err.message
              : '라이선스 정보를 불러오지 못했습니다.',
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
  }, [id]);

  /* ── Render ── */

  if (loading) {
    return (
      <div className={styles.page}>
        <div className={styles.loading}>{'라이선스를 불러오는 중...'}</div>
      </div>
    );
  }

  if (error || !detail) {
    return (
      <div className={styles.page}>
        <div className={styles.error}>
          {error ?? '라이선스를 찾을 수 없습니다.'}
        </div>
      </div>
    );
  }

  return (
    <div className={styles.page}>
      <Link to="/licenses" className={styles.backLink}>
        {'\u2190 라이선스 목록으로'}
      </Link>

      <h1 className={styles.pageTitle}>{'라이선스 상세'}</h1>

      <div className={styles.card}>
        <div className={styles.infoRow}>
          <span className={styles.infoLabel}>{'곡명'}</span>
          <span className={styles.infoValue}>{detail.track.title}</span>
        </div>
        {detail.track.bpm !== undefined && (
          <div className={styles.infoRow}>
            <span className={styles.infoLabel}>BPM</span>
            <span className={styles.infoValue}>{detail.track.bpm}</span>
          </div>
        )}
        {detail.track.tonality && (
          <div className={styles.infoRow}>
            <span className={styles.infoLabel}>Key</span>
            <span className={styles.infoValue}>{detail.track.tonality}</span>
          </div>
        )}
        <div className={styles.infoRow}>
          <span className={styles.infoLabel}>{'라이선스 코드'}</span>
          <span className={styles.licenseCode}>{detail.licenseCode}</span>
        </div>
        <div className={styles.infoRow}>
          <span className={styles.infoLabel}>{'발급일'}</span>
          <span className={styles.infoValue}>
            {formatDate(detail.issuedAt)}
          </span>
        </div>
        <div className={styles.infoRow}>
          <span className={styles.infoLabel}>{'소유자'}</span>
          <span className={styles.infoValue}>{detail.user.nickname}</span>
        </div>
      </div>
    </div>
  );
}

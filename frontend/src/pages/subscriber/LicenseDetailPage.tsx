import { useState, useEffect, useRef } from 'react';
import { useParams, Link } from 'react-router-dom';
import { fetchLicenseDetail, type LicenseDetail } from '@/api/licenses';
import { classifyLoadError } from '@/api/loadError';
import { useAuthStore } from '@/store/authStore';
import { formatDate } from '@/utils/format';
import { parsePositiveDecimalRouteID } from '@/utils/routeId';
import { createOwnerKey, createReadKey, getCurrentOwnerKey } from '@/utils/ownerProjection';
import styles from './LicenseDetailPage.module.css';

export default function LicenseDetailPage() {
  const { licenseId } = useParams<{ licenseId: string }>();
  const id = parsePositiveDecimalRouteID(licenseId);
  const userID = useAuthStore((s) => s.user?.id ?? null);
  const accessToken = useAuthStore((s) => s.accessToken);

  /* ── State ── */
  const [detail, setDetail] = useState<LicenseDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const requestGeneration = useRef(0);
  const ownerKey = createOwnerKey(userID, accessToken);
  const readKey = createReadKey(ownerKey, 'license-detail', id);
  const currentReadKeyRef = useRef(readKey);
  const [projectionKey, setProjectionKey] = useState<string | null>(null);
  const [errorKey, setErrorKey] = useState<string | null>(null);
  currentReadKeyRef.current = readKey;
  const currentDetail = projectionKey === readKey ? detail : null;
  const currentError = errorKey === readKey ? error : null;
  const currentLoading = loading || (currentDetail === null && currentError === null);

  /* ── Fetch ── */
  useEffect(() => {
    if (id === null) {
      requestGeneration.current += 1;
      setDetail(null);
      setLoading(false);
      setError(null);
      return;
    }
    const licenseID = id;
    const requestKey = readKey;
    const requestOwnerKey = ownerKey;
    const generation = ++requestGeneration.current;
    const controller = new AbortController();
    const isCurrent = () =>
      requestKey !== null &&
      generation === requestGeneration.current &&
      currentReadKeyRef.current === requestKey &&
      getCurrentOwnerKey(requestOwnerKey) === requestOwnerKey;

    async function load() {
      try {
        setLoading(true);
        setError(null);
        setDetail(null);
        const data = await fetchLicenseDetail(licenseID, controller.signal);
        if (isCurrent()) {
          setDetail(data);
          setProjectionKey(requestKey);
        }
      } catch (loadError) {
        if (isCurrent() && classifyLoadError(loadError) !== 'cancelled') {
          setError('라이선스 정보를 불러오지 못했습니다.');
          setErrorKey(requestKey);
        }
      } finally {
        if (isCurrent()) setLoading(false);
      }
    }

    void load();
    return () => {
      controller.abort();
      if (requestGeneration.current === generation) requestGeneration.current += 1;
    };
  }, [id, ownerKey, readKey]);

  /* ── Render ── */

  if (id === null) {
    return (
      <div className={styles.page}>
        <div className={styles.error}>{'라이선스 주소가 올바르지 않습니다.'}</div>
        <Link to="/licenses" className={styles.backLink}>
          {'라이선스 목록으로'}
        </Link>
      </div>
    );
  }

  if (currentLoading) {
    return (
      <div className={styles.page}>
        <div className={styles.loading}>{'라이선스를 불러오는 중...'}</div>
      </div>
    );
  }

  if (currentError || !currentDetail) {
    return (
      <div className={styles.page}>
        <div className={styles.error}>{currentError ?? '라이선스를 찾을 수 없습니다.'}</div>
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
          <span className={styles.infoValue}>{currentDetail.track.title}</span>
        </div>
        {currentDetail.track.bpm !== undefined && (
          <div className={styles.infoRow}>
            <span className={styles.infoLabel}>BPM</span>
            <span className={styles.infoValue}>{currentDetail.track.bpm}</span>
          </div>
        )}
        {currentDetail.track.tonality && (
          <div className={styles.infoRow}>
            <span className={styles.infoLabel}>Key</span>
            <span className={styles.infoValue}>{currentDetail.track.tonality}</span>
          </div>
        )}
        <div className={styles.infoRow}>
          <span className={styles.infoLabel}>{'라이선스 코드'}</span>
          <span className={styles.licenseCode}>{currentDetail.licenseCode}</span>
        </div>
        <div className={styles.infoRow}>
          <span className={styles.infoLabel}>{'발급일'}</span>
          <span className={styles.infoValue}>{formatDate(currentDetail.issuedAt)}</span>
        </div>
        <div className={styles.infoRow}>
          <span className={styles.infoLabel}>{'소유자'}</span>
          <span className={styles.infoValue}>{currentDetail.user.nickname}</span>
        </div>
      </div>
    </div>
  );
}

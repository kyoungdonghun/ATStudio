import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { fetchMyCompanyCert, type CompanyCertification } from '@/api/companyCerts';
import { formatDateTime } from '@/utils/format';
import Button from '@/components/ui/Button';
import styles from './CompanyCertStatusPage.module.css';

/** Map status code to label + CSS class */
function getStatusBadge(status: string) {
  switch (status) {
    case 'PENDING':
      return { label: '심사중', className: styles.badgePending };
    case 'APPROVED':
      return { label: '승인', className: styles.badgeApproved };
    case 'REJECTED':
      return { label: '반려', className: styles.badgeRejected };
    default:
      return { label: status, className: styles.badgePending };
  }
}

/** Screen I-2: Company certification status */
export default function CompanyCertStatusPage() {
  const [cert, setCert] = useState<CompanyCertification | null>(null);
  const [loading, setLoading] = useState(true);
  const [notFound, setNotFound] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;

    async function loadCert() {
      try {
        const data = await fetchMyCompanyCert();
        if (!cancelled) setCert(data);
      } catch (err: unknown) {
        if (cancelled) return;
        const status =
          err && typeof err === 'object' && 'response' in err
            ? (err as { response?: { status?: number } }).response?.status
            : undefined;
        if (status === 404) {
          setNotFound(true);
        } else {
          setError(
            err instanceof Error ? err.message : '인증 현황을 불러올 수 없습니다.',
          );
        }
      } finally {
        if (!cancelled) setLoading(false);
      }
    }

    loadCert();
    return () => { cancelled = true; };
  }, []);

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
        <h1 className={styles.pageTitle}>{'기업 인증 현황'}</h1>
        <div className={styles.error}>{error}</div>
      </div>
    );
  }

  if (notFound || !cert) {
    return (
      <div className={styles.page}>
        <h1 className={styles.pageTitle}>{'기업 인증 현황'}</h1>
        <div className={styles.empty}>
          <p className={styles.emptyText}>{'신청 내역이 없습니다.'}</p>
          <Link to="/company-cert/apply">
            <Button>{'인증 신청하기'}</Button>
          </Link>
        </div>
      </div>
    );
  }

  const badge = getStatusBadge(cert.status);

  return (
    <div className={styles.page}>
      <h1 className={styles.pageTitle}>{'기업 인증 현황'}</h1>

      <div className={styles.card}>
        {/* Status */}
        <div className={styles.statusRow}>
          <span className={styles.statusLabel}>{'상태'}</span>
          <span className={`${styles.badge} ${badge.className}`}>
            {badge.label}
          </span>
        </div>

        {/* Created at */}
        <div className={styles.infoRow}>
          <span className={styles.infoLabel}>{'신청일'}</span>
          <span className={styles.infoValue}>{formatDateTime(cert.createdAt)}</span>
        </div>

        {/* Approved at */}
        {cert.status === 'APPROVED' && cert.approvedAt && (
          <div className={styles.infoRow}>
            <span className={styles.infoLabel}>{'승인일'}</span>
            <span className={styles.infoValue}>{formatDateTime(cert.approvedAt)}</span>
          </div>
        )}

        {/* Certification code */}
        {cert.status === 'APPROVED' && cert.certificationCode && (
          <div className={styles.infoRow}>
            <span className={styles.infoLabel}>{'인증 코드'}</span>
            <span className={styles.certCode}>{cert.certificationCode}</span>
          </div>
        )}

        {/* Admin note */}
        {cert.adminNote && (
          <div className={styles.infoRow}>
            <span className={styles.infoLabel}>{'관리자 메모'}</span>
            <span className={styles.adminNote}>{cert.adminNote}</span>
          </div>
        )}
      </div>
    </div>
  );
}

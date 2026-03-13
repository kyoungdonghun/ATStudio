/** Screen K-5: Company certification review */
import { useEffect, useState, useCallback } from 'react';
import { fetchCompanyCerts, processCompanyCert } from '@/api/admin';
import type { CompanyCertificationSummary, CertificationStatus, PageInfo } from '@/types';
import { formatDate } from '@/utils/format';
import Modal from '@/components/ui/Modal';
import Button from '@/components/ui/Button';
import Pagination from '@/components/ui/Pagination';
import styles from './CompanyCertManagePage.module.css';

const STATUS_OPTIONS: Array<{ label: string; value: CertificationStatus | '' }> = [
  { label: 'All', value: '' },
  { label: 'Pending', value: 'PENDING' },
  { label: 'Approved', value: 'APPROVED' },
  { label: 'Rejected', value: 'REJECTED' },
  { label: 'Revision Requested', value: 'REVISION_REQUESTED' },
];

function statusClass(status: CertificationStatus): string {
  const map: Record<CertificationStatus, string> = {
    PENDING: styles.statusPENDING,
    APPROVED: styles.statusAPPROVED,
    REJECTED: styles.statusREJECTED,
    REVISION_REQUESTED: styles.statusREVISION_REQUESTED,
  };
  return `${styles.statusBadge} ${map[status] ?? ''}`;
}

export default function CompanyCertManagePage() {
  const [certs, setCerts] = useState<CompanyCertificationSummary[]>([]);
  const [pageInfo, setPageInfo] = useState<PageInfo | null>(null);
  const [page, setPage] = useState(1);
  const [statusFilter, setStatusFilter] = useState<CertificationStatus | ''>('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  /* Review modal */
  const [reviewTarget, setReviewTarget] = useState<{
    cert: CompanyCertificationSummary;
    action: 'APPROVED' | 'REJECTED';
  } | null>(null);
  const [adminNote, setAdminNote] = useState('');
  const [reviewLoading, setReviewLoading] = useState(false);

  const loadCerts = useCallback(() => {
    setLoading(true);
    setError(null);
    const params: Record<string, unknown> = { page, size: 20 };
    if (statusFilter) params.status = statusFilter;
    fetchCompanyCerts(params as Parameters<typeof fetchCompanyCerts>[0])
      .then((result) => {
        setCerts(result.dataList);
        setPageInfo(result.pageInfo);
      })
      .catch(() => setError('Failed to load certifications'))
      .finally(() => setLoading(false));
  }, [page, statusFilter]);

  useEffect(() => {
    loadCerts();
  }, [loadCerts]);

  const openReview = (
    cert: CompanyCertificationSummary,
    action: 'APPROVED' | 'REJECTED',
  ) => {
    setReviewTarget({ cert, action });
    setAdminNote('');
  };

  const confirmReview = async () => {
    if (!reviewTarget) return;
    setReviewLoading(true);
    try {
      await processCompanyCert(reviewTarget.cert.id, {
        status: reviewTarget.action,
        adminNote: adminNote || undefined,
      });
      setReviewTarget(null);
      loadCerts();
    } catch {
      setError('Failed to process certification');
    } finally {
      setReviewLoading(false);
    }
  };

  if (loading) {
    return (
      <div className={styles.page}>
        <div className={styles.loading}>Loading...</div>
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
      <h1 className={styles.title}>Company Certification Review</h1>

      {/* Status Filter */}
      <div className={styles.filterBar}>
        <span className={styles.filterLabel}>Status:</span>
        <select
          className={styles.filterSelect}
          value={statusFilter}
          onChange={(e) => {
            setStatusFilter(e.target.value as CertificationStatus | '');
            setPage(1);
          }}
        >
          {STATUS_OPTIONS.map((opt) => (
            <option key={opt.value} value={opt.value}>
              {opt.label}
            </option>
          ))}
        </select>
      </div>

      {/* Table */}
      <table className={styles.table}>
        <thead>
          <tr>
            <th>ID</th>
            <th>User</th>
            <th>Status</th>
            <th>Applied</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {certs.length === 0 && (
            <tr>
              <td colSpan={5} className={styles.empty}>
                No certification applications found.
              </td>
            </tr>
          )}
          {certs.map((cert) => (
            <tr key={cert.id} className={styles.row}>
              <td>{cert.id}</td>
              <td>{cert.userNickname}</td>
              <td>
                <span className={statusClass(cert.status)}>
                  {cert.status}
                </span>
              </td>
              <td>{formatDate(cert.createdAt)}</td>
              <td>
                {cert.status === 'PENDING' && (
                  <div className={styles.actionBtns}>
                    <Button
                      size="sm"
                      onClick={() => openReview(cert, 'APPROVED')}
                    >
                      Approve
                    </Button>
                    <Button
                      variant="danger"
                      size="sm"
                      onClick={() => openReview(cert, 'REJECTED')}
                    >
                      Reject
                    </Button>
                  </div>
                )}
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      {pageInfo && pageInfo.total > pageInfo.size && (
        <Pagination pageInfo={pageInfo} currentPage={page} onPageChange={setPage} />
      )}

      {/* Review confirm modal */}
      <Modal
        open={reviewTarget !== null}
        onClose={() => setReviewTarget(null)}
        title={
          reviewTarget?.action === 'APPROVED'
            ? 'Approve Certification'
            : 'Reject Certification'
        }
      >
        <div className={styles.modalBody}>
          <div>
            {reviewTarget?.action === 'APPROVED'
              ? 'Approve'
              : 'Reject'}{' '}
            certification for{' '}
            <strong>{reviewTarget?.cert.userNickname}</strong>?
          </div>
          <textarea
            className={styles.noteInput}
            placeholder="Admin note (optional)"
            value={adminNote}
            onChange={(e) => setAdminNote(e.target.value)}
          />
        </div>
        <div className={styles.modalActions}>
          <Button
            variant="ghost"
            size="sm"
            onClick={() => setReviewTarget(null)}
          >
            Cancel
          </Button>
          <Button
            variant={
              reviewTarget?.action === 'APPROVED' ? 'primary' : 'danger'
            }
            size="sm"
            loading={reviewLoading}
            onClick={confirmReview}
          >
            {reviewTarget?.action === 'APPROVED' ? 'Approve' : 'Reject'}
          </Button>
        </div>
      </Modal>
    </div>
  );
}

/** Screen K-5: Company certification review */
import { useEffect, useState, useCallback, useRef } from 'react';
import {
  downloadCompanyCertDocument,
  fetchCompanyCert,
  fetchCompanyCerts,
  processCompanyCert,
} from '@/api/admin';
import { createDownloadFallbackFileName, triggerBlobDownload } from '@/api/downloads';
import type {
  CompanyCertification,
  CompanyCertificationDocument,
  CompanyCertificationSummary,
  CertificationStatus,
  PageInfo,
} from '@/types';
import { formatDate, formatDateTime } from '@/utils/format';
import { CERT_REVIEW_NOTE_MAX } from '@/utils/validation';
import Modal from '@/components/ui/Modal';
import Button from '@/components/ui/Button';
import Pagination from '@/components/ui/Pagination';
import styles from './CompanyCertManagePage.module.css';

const STATUS_OPTIONS: Array<{ label: string; value: CertificationStatus | '' }> = [
  { label: '전체', value: '' },
  { label: '심사중', value: 'PENDING' },
  { label: '승인', value: 'APPROVED' },
  { label: '반려', value: 'REJECTED' },
  { label: '보완 요청', value: 'REVISION_REQUESTED' },
];

const STATUS_LABELS: Record<CertificationStatus, string> = {
  PENDING: '심사중',
  APPROVED: '승인',
  REJECTED: '반려',
  REVISION_REQUESTED: '보완 요청',
};

const REVIEW_ACTION_LABELS: Record<Exclude<CertificationStatus, 'PENDING'>, string> = {
  APPROVED: '승인',
  REVISION_REQUESTED: '보완 요청',
  REJECTED: '반려',
};

function statusClass(status: CertificationStatus): string {
  const map: Record<CertificationStatus, string> = {
    PENDING: styles.statusPENDING,
    APPROVED: styles.statusAPPROVED,
    REJECTED: styles.statusREJECTED,
    REVISION_REQUESTED: styles.statusREVISION_REQUESTED,
  };
  return `${styles.statusBadge} ${map[status] ?? ''}`;
}

function formatBytes(sizeBytes: number): string {
  if (sizeBytes < 1024) return `${sizeBytes} B`;
  if (sizeBytes < 1024 * 1024) return `${Math.round(sizeBytes / 1024)} KB`;
  return `${(sizeBytes / 1024 / 1024).toFixed(1)} MB`;
}

function getAdminReviewErrorMessage(error: unknown): string {
  const response =
    error && typeof error === 'object' && 'response' in error
      ? (error as { response?: { status?: number; data?: { message?: unknown } } }).response
      : undefined;
  if (response?.status === 403) return '기업 인증 심사 권한이 없습니다.';
  if (response?.status === 409) {
    return '다른 처리로 신청 상태가 변경되었습니다. 상세를 닫고 다시 확인해주세요.';
  }
  const message = response?.data?.message;
  if (typeof message === 'string' && message.trim()) return message.trim();
  if (response?.status === 400 || response?.status === 422) {
    return '심사 상태와 처리 사유를 확인해주세요.';
  }
  return '기업 인증 심사 처리에 실패했습니다.';
}

export default function CompanyCertManagePage() {
  const [certs, setCerts] = useState<CompanyCertificationSummary[]>([]);
  const [pageInfo, setPageInfo] = useState<PageInfo | null>(null);
  const [page, setPage] = useState(1);
  const [statusFilter, setStatusFilter] = useState<CertificationStatus | ''>('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const listRequestId = useRef(0);

  const [detailOpen, setDetailOpen] = useState(false);
  const [detail, setDetail] = useState<CompanyCertification | null>(null);
  const [detailLoading, setDetailLoading] = useState(false);
  const [detailError, setDetailError] = useState<string | null>(null);
  const [selectedDetailId, setSelectedDetailId] = useState<number | null>(null);
  const [downloadId, setDownloadId] = useState<number | null>(null);
  const detailRequestId = useRef(0);
  const detailOwnerId = useRef(0);
  const selectedDetailIdRef = useRef<number | null>(null);

  const [reviewAction, setReviewAction] = useState<Exclude<CertificationStatus, 'PENDING'> | null>(
    null,
  );
  const [adminNote, setAdminNote] = useState('');
  const [reviewError, setReviewError] = useState<string | null>(null);
  const [reviewLoading, setReviewLoading] = useState(false);
  const reviewPendingRef = useRef(false);

  const loadCerts = useCallback(
    async (showLoading = true) => {
      const currentRequestId = ++listRequestId.current;
      if (showLoading) setLoading(true);
      setError(null);
      const params: Record<string, unknown> = { page, size: 20 };
      if (statusFilter) params.status = statusFilter;
      try {
        const result = await fetchCompanyCerts(params as Parameters<typeof fetchCompanyCerts>[0]);
        if (currentRequestId !== listRequestId.current) return;
        setCerts(result.dataList);
        setPageInfo(result.pageInfo);
      } catch {
        if (currentRequestId === listRequestId.current) {
          setCerts([]);
          setPageInfo(null);
          setError('기업 인증 신청 목록을 불러오지 못했습니다.');
        }
      } finally {
        if (currentRequestId === listRequestId.current && showLoading) {
          setLoading(false);
        }
      }
    },
    [page, statusFilter],
  );

  useEffect(() => {
    void loadCerts();
    return () => {
      listRequestId.current += 1;
    };
  }, [loadCerts]);

  useEffect(
    () => () => {
      detailRequestId.current += 1;
      detailOwnerId.current += 1;
      selectedDetailIdRef.current = null;
    },
    [],
  );

  async function loadDetail(certId: number, clearCurrent = true) {
    const currentRequestId = ++detailRequestId.current;
    if (clearCurrent) setDetail(null);
    setDetailError(null);
    setDetailLoading(true);
    try {
      const data = await fetchCompanyCert(certId);
      if (currentRequestId !== detailRequestId.current || selectedDetailIdRef.current !== certId) {
        return;
      }
      setDetail(data);
    } catch {
      if (currentRequestId !== detailRequestId.current || selectedDetailIdRef.current !== certId) {
        return;
      }
      setDetailError('기업 인증 신청 상세를 불러오지 못했습니다.');
    } finally {
      if (currentRequestId === detailRequestId.current && selectedDetailIdRef.current === certId) {
        setDetailLoading(false);
      }
    }
  }

  function openDetail(certId: number) {
    if (reviewPendingRef.current) return;
    detailOwnerId.current += 1;
    selectedDetailIdRef.current = certId;
    setSelectedDetailId(certId);
    setDetailOpen(true);
    setReviewAction(null);
    setAdminNote('');
    setReviewError(null);
    void loadDetail(certId);
  }

  function closeDetail() {
    if (reviewPendingRef.current) return;
    detailRequestId.current += 1;
    detailOwnerId.current += 1;
    selectedDetailIdRef.current = null;
    setDetailOpen(false);
    setDetail(null);
    setDetailLoading(false);
    setDetailError(null);
    setSelectedDetailId(null);
    setReviewAction(null);
    setAdminNote('');
    setReviewError(null);
  }

  function openReview(action: Exclude<CertificationStatus, 'PENDING'>) {
    if (reviewPendingRef.current) return;
    setReviewAction(action);
    setAdminNote('');
    setReviewError(null);
  }

  async function confirmReview() {
    if (!detail || !reviewAction || reviewPendingRef.current) return;
    const reviewTargetId = detail.id;
    const reviewTargetOwnerId = detailOwnerId.current;
    const action = reviewAction;
    const normalizedNote = adminNote.trim();
    const requiresReason = action === 'REVISION_REQUESTED' || action === 'REJECTED';
    if (requiresReason && !normalizedNote) {
      setReviewError(`${REVIEW_ACTION_LABELS[action]} 사유를 입력해주세요.`);
      return;
    }
    if (normalizedNote.length > CERT_REVIEW_NOTE_MAX) {
      setReviewError(`처리 사유는 최대 ${CERT_REVIEW_NOTE_MAX}자까지 입력할 수 있습니다.`);
      return;
    }

    setReviewError(null);
    reviewPendingRef.current = true;
    setReviewLoading(true);
    const ownsReviewTarget = () =>
      detailOwnerId.current === reviewTargetOwnerId &&
      selectedDetailIdRef.current === reviewTargetId;
    try {
      await processCompanyCert(reviewTargetId, {
        status: action,
        adminNote: normalizedNote || undefined,
      });
      if (!ownsReviewTarget()) return;
      setReviewAction(null);
      setAdminNote('');
      await loadDetail(reviewTargetId, false);
      await loadCerts(false);
    } catch (error) {
      if (!ownsReviewTarget()) return;
      setReviewError(getAdminReviewErrorMessage(error));
    } finally {
      reviewPendingRef.current = false;
      setReviewLoading(false);
    }
  }

  async function handleDownload(document: CompanyCertificationDocument) {
    if (!detail) return;
    setDownloadId(document.id);
    try {
      const download = await downloadCompanyCertDocument(
        detail.id,
        document.id,
        createDownloadFallbackFileName(
          'company-certification-document',
          document.id,
          document.originalFilename,
        ),
      );
      triggerBlobDownload(download);
    } catch {
      setDetailError('서류 다운로드에 실패했습니다.');
    } finally {
      setDownloadId(null);
    }
  }

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
        <div className={styles.error} role="alert">
          <p>{error}</p>
          <Button type="button" onClick={() => void loadCerts()}>
            다시 시도
          </Button>
        </div>
      </div>
    );
  }

  return (
    <div className={styles.page}>
      <h1 className={styles.title}>기업 인증 심사</h1>

      <div className={styles.filterBar}>
        <span className={styles.filterLabel}>상태</span>
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

      <div className={styles.tableWrap}>
        <table className={styles.table}>
          <thead>
            <tr>
              <th>ID</th>
              <th>신청자</th>
              <th>회사명</th>
              <th>상태</th>
              <th>신청일</th>
              <th>작업</th>
            </tr>
          </thead>
          <tbody>
            {certs.length === 0 && (
              <tr>
                <td colSpan={6} className={styles.empty}>
                  기업 인증 신청 내역이 없습니다.
                </td>
              </tr>
            )}
            {certs.map((cert) => (
              <tr key={cert.id} className={styles.row}>
                <td>{cert.id}</td>
                <td>
                  <div className={styles.userCell}>
                    <strong>{cert.userNickname}</strong>
                    <span>{cert.userEmail}</span>
                  </div>
                </td>
                <td>{cert.companyName ?? '-'}</td>
                <td>
                  <span className={statusClass(cert.status)}>{STATUS_LABELS[cert.status]}</span>
                </td>
                <td>{formatDate(cert.createdAt)}</td>
                <td>
                  <Button
                    variant="outline"
                    size="sm"
                    disabled={reviewLoading}
                    onClick={() => openDetail(cert.id)}
                  >
                    상세
                  </Button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {pageInfo && pageInfo.total > pageInfo.size && (
        <Pagination pageInfo={pageInfo} currentPage={page} onPageChange={setPage} />
      )}

      <Modal open={detailOpen} onClose={closeDetail} title="기업 인증 상세" busy={reviewLoading}>
        <div className={styles.modalBody}>
          {detailLoading && <div className={styles.loadingInline}>상세를 불러오는 중...</div>}
          {detailError && (
            <div className={styles.modalError} role="alert">
              <p>{detailError}</p>
              <Button
                type="button"
                size="sm"
                onClick={() => {
                  if (selectedDetailId !== null) void loadDetail(selectedDetailId);
                }}
              >
                다시 시도
              </Button>
            </div>
          )}
          {detail && (
            <>
              <div className={styles.detailGrid}>
                <div>
                  <span className={styles.detailLabel}>신청자</span>
                  <strong>{detail.userNickname}</strong>
                  <span>{detail.userEmail}</span>
                </div>
                <div>
                  <span className={styles.detailLabel}>회사명</span>
                  <strong>{detail.companyName ?? '-'}</strong>
                  <span>{detail.phoneCompany ?? '-'}</span>
                </div>
                <div>
                  <span className={styles.detailLabel}>상태</span>
                  <span className={statusClass(detail.status)}>{STATUS_LABELS[detail.status]}</span>
                </div>
                <div>
                  <span className={styles.detailLabel}>신청일</span>
                  <strong>{formatDateTime(detail.createdAt)}</strong>
                </div>
              </div>

              {detail.adminNote && (
                <div className={styles.noteBox}>
                  <span className={styles.detailLabel}>관리자 메모</span>
                  <p>{detail.adminNote}</p>
                </div>
              )}

              <div className={styles.documentsBlock}>
                <h3 className={styles.sectionTitle}>제출 서류</h3>
                {detail.documents.length === 0 ? (
                  <p className={styles.emptyDocuments}>
                    개별 서류 메타데이터가 없습니다. 이전 방식으로 저장된 신청일 수 있습니다.
                  </p>
                ) : (
                  <div className={styles.documentList}>
                    {detail.documents.map((document) => (
                      <div key={document.id} className={styles.documentItem}>
                        <div className={styles.documentMeta}>
                          <strong>{document.originalFilename}</strong>
                          <span>
                            {`${formatBytes(document.sizeBytes)} · ${document.contentType ?? 'unknown'}`}
                          </span>
                        </div>
                        <Button
                          variant="ghost"
                          size="sm"
                          loading={downloadId === document.id}
                          onClick={() => handleDownload(document)}
                        >
                          다운로드
                        </Button>
                      </div>
                    ))}
                  </div>
                )}
              </div>

              {detail.status === 'PENDING' && (
                <div className={styles.reviewActions}>
                  <Button size="sm" onClick={() => openReview('APPROVED')}>
                    승인
                  </Button>
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => openReview('REVISION_REQUESTED')}
                  >
                    보완 요청
                  </Button>
                  <Button variant="danger" size="sm" onClick={() => openReview('REJECTED')}>
                    반려
                  </Button>
                </div>
              )}
            </>
          )}
        </div>
      </Modal>

      <Modal
        open={reviewAction !== null}
        onClose={() => {
          if (reviewPendingRef.current) return;
          setReviewAction(null);
          setReviewError(null);
        }}
        title={reviewAction ? `${REVIEW_ACTION_LABELS[reviewAction]} 처리` : '심사 처리'}
        busy={reviewLoading}
      >
        <div className={styles.modalBody}>
          <p className={styles.confirmText}>
            {reviewAction
              ? `${detail?.userNickname ?? '신청자'}의 기업 인증을 ${REVIEW_ACTION_LABELS[reviewAction]} 처리합니다.`
              : ''}
          </p>
          <label className={styles.noteLabel} htmlFor="company-cert-review-note">
            {reviewAction === 'REVISION_REQUESTED' || reviewAction === 'REJECTED'
              ? '처리 사유 (필수)'
              : '관리자 메모 (선택)'}
          </label>
          <textarea
            id="company-cert-review-note"
            className={styles.noteInput}
            placeholder="신청자에게 전달할 처리 사유를 입력해주세요."
            value={adminNote}
            maxLength={CERT_REVIEW_NOTE_MAX}
            aria-invalid={reviewError !== null}
            aria-describedby="company-cert-review-note-help"
            onChange={(e) => {
              setAdminNote(e.target.value);
              setReviewError(null);
            }}
          />
          <div id="company-cert-review-note-help" className={styles.noteMeta}>
            <span>
              {reviewAction === 'REVISION_REQUESTED' || reviewAction === 'REJECTED'
                ? '보완 요청과 반려는 사유가 반드시 필요합니다.'
                : '승인 메모는 입력하지 않아도 됩니다.'}
            </span>
            <span>{`${adminNote.length}/${CERT_REVIEW_NOTE_MAX}`}</span>
          </div>
          {reviewError && (
            <div className={styles.modalError} role="alert">
              {reviewError}
            </div>
          )}
        </div>
        <div className={styles.modalActions}>
          <Button
            variant="ghost"
            size="sm"
            disabled={reviewLoading}
            onClick={() => {
              if (reviewPendingRef.current) return;
              setReviewAction(null);
              setReviewError(null);
            }}
          >
            취소
          </Button>
          <Button
            variant={reviewAction === 'REJECTED' ? 'danger' : 'primary'}
            size="sm"
            loading={reviewLoading}
            onClick={confirmReview}
          >
            {reviewAction ? REVIEW_ACTION_LABELS[reviewAction] : '확인'}
          </Button>
        </div>
      </Modal>
    </div>
  );
}

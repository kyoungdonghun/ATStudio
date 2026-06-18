import { useState, useEffect, type ChangeEvent, type FormEvent } from 'react';
import { Link } from 'react-router-dom';
import { fetchMyCompanyCert, resubmitCompanyCert } from '@/api/companyCerts';
import type { CompanyCertification } from '@/types';
import {
  CERT_DOC_ACCEPT,
  CERT_DOC_EXTENSIONS,
  CERT_DOC_MAX_COUNT,
  CERT_DOC_MAX_SIZE_MB,
  CERT_DOC_LABEL,
  isFileSizeOk,
} from '@/utils/validation';
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
    case 'REVISION_REQUESTED':
      return { label: '보완 요청', className: styles.badgeRevision };
    default:
      return { label: status, className: styles.badgePending };
  }
}

/** Screen I-2: Company certification status */
export default function CompanyCertStatusPage() {
  const [cert, setCert] = useState<CompanyCertification | null>(null);
  const [files, setFiles] = useState<File[]>([]);
  const [loading, setLoading] = useState(true);
  const [notFound, setNotFound] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [formError, setFormError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

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
          setError(err instanceof Error ? err.message : '인증 현황을 불러올 수 없습니다.');
        }
      } finally {
        if (!cancelled) setLoading(false);
      }
    }

    loadCert();
    return () => {
      cancelled = true;
    };
  }, []);

  function handleFileChange(e: ChangeEvent<HTMLInputElement>) {
    const selected = e.target.files;
    if (!selected) return;

    const newFiles = Array.from(selected);

    for (const file of newFiles) {
      const ext = file.name.split('.').pop()?.toLowerCase() ?? '';
      if (!CERT_DOC_EXTENSIONS.includes(ext)) {
        setFormError(`허용되지 않는 파일 형식입니다: ${file.name} (${CERT_DOC_LABEL} 만 가능)`);
        e.target.value = '';
        return;
      }
    }

    for (const file of newFiles) {
      if (!isFileSizeOk(file, CERT_DOC_MAX_SIZE_MB)) {
        setFormError(`파일 크기가 ${CERT_DOC_MAX_SIZE_MB}MB를 초과합니다: ${file.name}`);
        e.target.value = '';
        return;
      }
    }

    if (files.length + newFiles.length > CERT_DOC_MAX_COUNT) {
      setFormError(`첨부파일은 최대 ${CERT_DOC_MAX_COUNT}개까지 가능합니다.`);
      e.target.value = '';
      return;
    }

    setFormError(null);
    setFiles((prev) => [...prev, ...newFiles]);
    e.target.value = '';
  }

  function removeFile(index: number) {
    setFiles((prev) => prev.filter((_, i) => i !== index));
  }

  async function handleResubmit(e: FormEvent) {
    e.preventDefault();
    setFormError(null);

    if (files.length === 0) {
      setFormError('보완 서류 파일을 하나 이상 첨부해주세요.');
      return;
    }

    setSubmitting(true);
    try {
      const updated = await resubmitCompanyCert(files);
      setCert(updated);
      setFiles([]);
    } catch (err) {
      setFormError(err instanceof Error ? err.message : '보완 서류 제출에 실패했습니다.');
    } finally {
      setSubmitting(false);
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
          <Link to="/company-certification/apply">
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
        <div className={styles.statusRow}>
          <span className={styles.statusLabel}>{'상태'}</span>
          <span className={`${styles.badge} ${badge.className}`}>{badge.label}</span>
        </div>

        <div className={styles.infoRow}>
          <span className={styles.infoLabel}>{'신청일'}</span>
          <span className={styles.infoValue}>{formatDateTime(cert.createdAt)}</span>
        </div>

        {cert.status === 'APPROVED' && cert.approvedAt && (
          <div className={styles.infoRow}>
            <span className={styles.infoLabel}>{'승인일'}</span>
            <span className={styles.infoValue}>{formatDateTime(cert.approvedAt)}</span>
          </div>
        )}

        {cert.status === 'APPROVED' && cert.certificationCode && (
          <div className={styles.infoRow}>
            <span className={styles.infoLabel}>{'인증 코드'}</span>
            <span className={styles.certCode}>{cert.certificationCode}</span>
          </div>
        )}

        {cert.documents.length > 0 && (
          <div className={styles.infoRow}>
            <span className={styles.infoLabel}>{'제출 서류'}</span>
            <span className={styles.infoValue}>{`${cert.documents.length}개 제출됨`}</span>
          </div>
        )}

        {cert.adminNote && (
          <div className={styles.infoRow}>
            <span className={styles.infoLabel}>{'관리자 메모'}</span>
            <span className={styles.adminNote}>{cert.adminNote}</span>
          </div>
        )}
      </div>

      {cert.status === 'REVISION_REQUESTED' && (
        <form className={styles.resubmitPanel} onSubmit={handleResubmit}>
          <h2 className={styles.panelTitle}>{'보완 서류 제출'}</h2>
          <p className={styles.panelDesc}>
            {
              '관리자 메모를 확인한 뒤 수정된 서류를 다시 제출해주세요. 제출 후 상태는 심사중으로 돌아갑니다.'
            }
          </p>
          {formError && <div className={styles.error}>{formError}</div>}
          <label className={styles.dropZone}>
            <input
              type="file"
              multiple
              accept={CERT_DOC_ACCEPT}
              className={styles.fileHidden}
              onChange={handleFileChange}
            />
            {'파일 선택 (여러 파일 가능)'}
          </label>
          <p className={styles.fileHint}>
            {`허용 형식: ${CERT_DOC_LABEL} / 최대 ${CERT_DOC_MAX_SIZE_MB}MB, ${CERT_DOC_MAX_COUNT}개`}
          </p>
          {files.length > 0 && (
            <div className={styles.fileList}>
              {files.map((file, idx) => (
                <div key={`${file.name}-${idx}`} className={styles.fileItem}>
                  <span className={styles.fileItemName}>{file.name}</span>
                  <button
                    type="button"
                    className={styles.fileRemoveBtn}
                    onClick={() => removeFile(idx)}
                    aria-label={`${file.name} 제거`}
                  >
                    {'X'}
                  </button>
                </div>
              ))}
            </div>
          )}
          <div className={styles.actions}>
            <Button type="submit" loading={submitting}>
              {'보완 서류 제출'}
            </Button>
          </div>
        </form>
      )}

      {cert.status === 'REJECTED' && (
        <div className={styles.actionPanel}>
          <p className={styles.panelDesc}>
            {'반려된 신청은 기록으로 보존됩니다. 보완한 서류로 새 인증 신청을 진행할 수 있습니다.'}
          </p>
          <Link to="/company-certification/apply">
            <Button>{'새 인증 신청'}</Button>
          </Link>
        </div>
      )}

      {cert.status === 'APPROVED' && (
        <div className={styles.actionPanel}>
          <p className={styles.panelDesc}>
            {'기업 인증이 승인되었습니다. 기업용 구독 플랜을 선택할 수 있습니다.'}
          </p>
          <Link to="/subscriptions">
            <Button>{'구독 플랜 보기'}</Button>
          </Link>
        </div>
      )}
    </div>
  );
}

import { useState, useEffect, useRef, type FormEvent, type ChangeEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { applyCompanyCert, fetchMyCompanyCert } from '@/api/companyCerts';
import { getSetting } from '@/api/settings';
import {
  CERT_DOC_ACCEPT,
  CERT_DOC_EXTENSIONS,
  CERT_DOC_MAX_SIZE_MB,
  CERT_DOC_MAX_COUNT,
  CERT_DOC_LABEL,
  isFileSizeOk,
} from '@/utils/validation';
import Button from '@/components/ui/Button';
import styles from './CompanyCertApplyPage.module.css';

/** Screen I-1: Company certification application */
export default function CompanyCertApplyPage() {
  const navigate = useNavigate();
  const fileInputRef = useRef<HTMLInputElement>(null);

  /* ── State ── */
  const [files, setFiles] = useState<File[]>([]);
  const [checking, setChecking] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [existingRejected, setExistingRejected] = useState(false);
  const [guideText, setGuideText] = useState<string | null>(null);
  const [guideLoading, setGuideLoading] = useState(true);

  /* ── Load guide text ── */
  useEffect(() => {
    let cancelled = false;

    async function loadGuide() {
      try {
        const text = await getSetting('COMPANY_CERT_GUIDE');
        if (!cancelled) setGuideText(text);
      } catch {
        // Fallback: show default guide on API failure
        if (!cancelled) setGuideText(null);
      } finally {
        if (!cancelled) setGuideLoading(false);
      }
    }

    loadGuide();
    return () => {
      cancelled = true;
    };
  }, []);

  /* ── Check existing certification ── */
  useEffect(() => {
    let cancelled = false;

    async function checkExisting() {
      try {
        const cert = await fetchMyCompanyCert();
        // Rejected applications are kept for history, but the user may start a new one.
        if (!cancelled && cert.status === 'REJECTED') {
          setExistingRejected(true);
          return;
        }
        if (!cancelled) {
          navigate('/company-certification/status', { replace: true });
        }
      } catch (err: unknown) {
        // 404 means no existing cert → show form
        const status =
          err && typeof err === 'object' && 'response' in err
            ? (err as { response?: { status?: number } }).response?.status
            : undefined;
        if (status !== 404) {
          if (!cancelled) {
            setError('인증 상태를 확인할 수 없습니다.');
          }
        }
      } finally {
        if (!cancelled) setChecking(false);
      }
    }

    checkExisting();
    return () => {
      cancelled = true;
    };
  }, [navigate]);

  /* ── File handlers ── */
  function handleFileChange(e: ChangeEvent<HTMLInputElement>) {
    const selected = e.target.files;
    if (!selected) return;

    const newFiles = Array.from(selected);

    // 확장자 검증
    for (const file of newFiles) {
      const ext = file.name.split('.').pop()?.toLowerCase() ?? '';
      if (!CERT_DOC_EXTENSIONS.includes(ext)) {
        setError(`허용되지 않는 파일 형식입니다: ${file.name} (${CERT_DOC_LABEL} 만 가능)`);
        e.target.value = '';
        return;
      }
    }

    // 크기 검증
    for (const file of newFiles) {
      if (!isFileSizeOk(file, CERT_DOC_MAX_SIZE_MB)) {
        setError(`파일 크기가 ${CERT_DOC_MAX_SIZE_MB}MB를 초과합니다: ${file.name}`);
        e.target.value = '';
        return;
      }
    }

    // 개수 검증
    if (files.length + newFiles.length > CERT_DOC_MAX_COUNT) {
      setError(`첨부파일은 최대 ${CERT_DOC_MAX_COUNT}개까지 가능합니다.`);
      e.target.value = '';
      return;
    }

    setError(null);
    setFiles((prev) => [...prev, ...newFiles]);
    // Reset input so the same file can be re-selected
    e.target.value = '';
  }

  function removeFile(index: number) {
    setFiles((prev) => prev.filter((_, i) => i !== index));
  }

  /* ── Submit ── */
  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setError(null);

    if (files.length === 0) {
      setError('서류 파일을 하나 이상 첨부해주세요.');
      return;
    }

    setSubmitting(true);
    try {
      await applyCompanyCert(files);
      navigate('/company-certification/status');
    } catch (err) {
      const msg = err instanceof Error ? err.message : '인증 신청에 실패했습니다.';
      setError(msg);
    } finally {
      setSubmitting(false);
    }
  }

  if (checking) {
    return (
      <div className={styles.page}>
        <div className={styles.loading}>{'Loading...'}</div>
      </div>
    );
  }

  return (
    <div className={styles.page}>
      <h1 className={styles.pageTitle}>{'기업 인증 신청'}</h1>
      <p className={styles.description}>
        {existingRejected
          ? '이전 신청은 반려되었습니다. 보완한 서류로 새 인증 신청을 제출해주세요.'
          : '사업자등록증 등 기업 서류를 제출하면 관리자 심사 후 승인됩니다.'}
      </p>

      {/* Guide text from settings */}
      <div className={styles.guideBox}>
        {guideLoading ? (
          <p className={styles.guideLoading}>{'가이드를 불러오는 중...'}</p>
        ) : guideText ? (
          <ul className={styles.guideList}>
            {guideText
              .split('\n')
              .filter(Boolean)
              .map((line, idx) => (
                <li key={idx} className={styles.guideItem}>
                  {line}
                </li>
              ))}
          </ul>
        ) : (
          <ul className={styles.guideList}>
            <li className={styles.guideItem}>{'1. 사업자등록증 사본'}</li>
            <li className={styles.guideItem}>{'2. 법인인감증명서 또는 사용인감계'}</li>
            <li className={styles.guideItem}>{'3. 대표자 신분증 사본'}</li>
          </ul>
        )}
      </div>

      <form className={styles.form} onSubmit={handleSubmit}>
        {error && <div className={styles.error}>{error}</div>}

        {/* File upload */}
        <div className={styles.field}>
          <span className={`${styles.label} ${styles.required}`}>{'서류 첨부'}</span>

          <label className={styles.dropZone}>
            <input
              ref={fileInputRef}
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
        </div>

        {/* Actions */}
        <div className={styles.actions}>
          <Button variant="ghost" type="button" onClick={() => navigate(-1)} disabled={submitting}>
            {'취소'}
          </Button>
          <Button type="submit" loading={submitting}>
            {'신청하기'}
          </Button>
        </div>
      </form>
    </div>
  );
}

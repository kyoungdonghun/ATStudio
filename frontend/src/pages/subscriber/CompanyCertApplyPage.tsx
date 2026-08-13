import { useState, useEffect, useRef, useCallback, type FormEvent, type ChangeEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  applyCompanyCert,
  fetchMyCompanyCert,
  getCompanyCertErrorMessage,
  getCompanyCertErrorStatus,
} from '@/api/companyCerts';
import { getSetting } from '@/api/settings';
import {
  CERT_DOC_ACCEPT,
  CERT_DOC_MAX_SIZE_MB,
  CERT_DOC_MAX_COUNT,
  CERT_DOC_MAX_TOTAL_SIZE_MB,
  CERT_DOC_LABEL,
  validateCompanyCertFileSelection,
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
  const [accessDenied, setAccessDenied] = useState(false);
  const [existingRejected, setExistingRejected] = useState(false);
  const [lookupAllowed, setLookupAllowed] = useState(false);
  const [guideText, setGuideText] = useState<string | null>(null);
  const [guideLoading, setGuideLoading] = useState(true);
  const lookupRequestId = useRef(0);

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
  const checkExisting = useCallback(async () => {
    const currentRequestId = ++lookupRequestId.current;
    setChecking(true);
    setError(null);
    setAccessDenied(false);
    setExistingRejected(false);
    setLookupAllowed(false);
    try {
      const cert = await fetchMyCompanyCert();
      if (currentRequestId !== lookupRequestId.current) return;
      if (cert.status === 'REJECTED') {
        setExistingRejected(true);
        setLookupAllowed(true);
        return;
      }
      navigate('/company-certification/status', { replace: true });
    } catch (err: unknown) {
      if (currentRequestId !== lookupRequestId.current) return;
      const status = getCompanyCertErrorStatus(err);
      if (status === 404) {
        setLookupAllowed(true);
      } else {
        setAccessDenied(status === 403);
        setError(getCompanyCertErrorMessage(err, '인증 상태를 확인할 수 없습니다.'));
      }
    } finally {
      if (currentRequestId === lookupRequestId.current) setChecking(false);
    }
  }, [navigate]);

  useEffect(() => {
    void checkExisting();
    return () => {
      lookupRequestId.current += 1;
    };
  }, [checkExisting]);

  /* ── File handlers ── */
  function handleFileChange(e: ChangeEvent<HTMLInputElement>) {
    const selected = e.target.files;
    if (!selected) return;

    const newFiles = Array.from(selected);

    const validationError = validateCompanyCertFileSelection(files, newFiles);
    if (validationError) {
      setError(validationError);
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

    if (!lookupAllowed) {
      setError('기존 인증 신청 확인이 완료되지 않았습니다. 다시 시도해주세요.');
      return;
    }

    if (files.length === 0) {
      setError('서류 파일을 하나 이상 첨부해주세요.');
      return;
    }

    setSubmitting(true);
    try {
      await applyCompanyCert(files);
      navigate('/company-certification/status');
    } catch (err) {
      setError(getCompanyCertErrorMessage(err, '인증 신청에 실패했습니다.'));
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

  if (accessDenied) {
    return (
      <div className={styles.page}>
        <h1 className={styles.pageTitle}>{'기업 인증 신청'}</h1>
        <div className={styles.error} role="alert">
          {error}
        </div>
      </div>
    );
  }

  if (!lookupAllowed) {
    return (
      <div className={styles.page}>
        <h1 className={styles.pageTitle}>{'기업 인증 신청'}</h1>
        <div className={styles.error} role="alert">
          <p>{error ?? '인증 상태를 확인할 수 없습니다.'}</p>
          <Button type="button" onClick={() => void checkExisting()}>
            {'다시 시도'}
          </Button>
        </div>
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
        {error && (
          <div className={styles.error} role="alert">
            {error}
          </div>
        )}

        {/* File upload */}
        <div className={styles.field}>
          <span className={`${styles.label} ${styles.required}`}>{'서류 첨부'}</span>

          <label className={styles.dropZone}>
            <input
              ref={fileInputRef}
              type="file"
              multiple
              accept={CERT_DOC_ACCEPT}
              aria-label="기업 인증 서류 선택"
              className={styles.fileHidden}
              onChange={handleFileChange}
            />
            {'파일 선택 (여러 파일 가능)'}
          </label>
          <p className={styles.fileHint}>
            {`허용 형식: ${CERT_DOC_LABEL} / 파일당 ${CERT_DOC_MAX_SIZE_MB}MB, 전체 ${CERT_DOC_MAX_TOTAL_SIZE_MB}MB, 최대 ${CERT_DOC_MAX_COUNT}개`}
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

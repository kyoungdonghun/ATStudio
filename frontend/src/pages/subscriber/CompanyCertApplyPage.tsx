import { useState, useEffect, useRef, type FormEvent, type ChangeEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { applyCompanyCert, fetchMyCompanyCert } from '@/api/companyCerts';
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

  /* ── Check existing certification ── */
  useEffect(() => {
    let cancelled = false;

    async function checkExisting() {
      try {
        await fetchMyCompanyCert();
        // If we get here, user already has a certification
        if (!cancelled) {
          navigate('/company-cert/status', { replace: true });
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
    return () => { cancelled = true; };
  }, [navigate]);

  /* ── File handlers ── */
  function handleFileChange(e: ChangeEvent<HTMLInputElement>) {
    const selected = e.target.files;
    if (!selected) return;
    setFiles((prev) => [...prev, ...Array.from(selected)]);
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
      navigate('/company-cert/status');
    } catch (err) {
      const msg =
        err instanceof Error ? err.message : '인증 신청에 실패했습니다.';
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
        {'사업자등록증 등 기업 서류를 제출하면 관리자 심사 후 승인됩니다.'}
      </p>

      <form className={styles.form} onSubmit={handleSubmit}>
        {error && <div className={styles.error}>{error}</div>}

        {/* File upload */}
        <div className={styles.field}>
          <span className={`${styles.label} ${styles.required}`}>
            {'서류 첨부'}
          </span>

          <label className={styles.dropZone}>
            <input
              ref={fileInputRef}
              type="file"
              multiple
              className={styles.fileHidden}
              onChange={handleFileChange}
            />
            {'파일 선택 (여러 파일 가능)'}
          </label>

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
          <Button
            variant="ghost"
            type="button"
            onClick={() => navigate(-1)}
            disabled={submitting}
          >
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

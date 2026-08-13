/** Screen 21: Notice create (ADMIN) */
import { useEffect, useRef, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { createNotice } from '@/api/notices';
import { classifyLoadError, isAmbiguousMutationError } from '@/api/loadError';
import usePendingMutationGuard from '@/hooks/usePendingMutationGuard';
import { useAdminMutationBoundary } from '@/layouts/AdminMutationBoundary';
import {
  isNoticeCreateObservationRequired,
  requireNoticeCreateObservation,
} from '@/utils/noticeCreateObservationFence';
import {
  ATTACHMENT_MAX_COUNT,
  ATTACHMENT_MAX_SIZE_MB,
  DESCRIPTION_MAX,
  TITLE_NOTICE_MAX,
  isFileSizeOk,
} from '@/utils/validation';
import Button from '@/components/ui/Button';
import styles from './NoticeCreatePage.module.css';

function formatFileSize(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}

interface CreateOperation {
  readonly id: symbol;
}

export default function NoticeCreatePage() {
  const navigate = useNavigate();
  const adminMutationBoundary = useAdminMutationBoundary();
  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [isPinned, setIsPinned] = useState(false);
  const [files, setFiles] = useState<File[]>([]);
  const [creating, setCreating] = useState(false);
  const [outcomeUnknown, setOutcomeUnknown] = useState(isNoticeCreateObservationRequired);
  const [formError, setFormError] = useState<string | null>(null);
  const [fileError, setFileError] = useState<string | null>(null);
  const operationRef = useRef<CreateOperation | null>(null);
  const recoveryRequiredRef = useRef(outcomeUnknown);
  const mountedRef = useRef(true);
  const fileInputRef = useRef<HTMLInputElement>(null);
  const resetBlockedNavigation = usePendingMutationGuard(operationRef, creating);
  const busy = creating || outcomeUnknown;

  useEffect(() => {
    mountedRef.current = true;
    return () => {
      mountedRef.current = false;
    };
  }, []);

  function handleFileChange(event: React.ChangeEvent<HTMLInputElement>) {
    if (operationRef.current !== null || recoveryRequiredRef.current) return;
    const selected = event.target.files;
    if (!selected) return;
    const added = Array.from(selected);

    if (files.length + added.length > ATTACHMENT_MAX_COUNT) {
      setFileError(`첨부파일은 최대 ${ATTACHMENT_MAX_COUNT}개까지 선택할 수 있습니다.`);
      event.target.value = '';
      return;
    }

    const oversized = added.filter((file) => !isFileSizeOk(file, ATTACHMENT_MAX_SIZE_MB));
    if (oversized.length > 0) {
      setFileError(
        `첨부파일은 ${ATTACHMENT_MAX_SIZE_MB}MB 이하만 선택할 수 있습니다. (${oversized
          .map((file) => file.name)
          .join(', ')})`,
      );
      event.target.value = '';
      return;
    }

    setFileError(null);
    setFiles((current) => [...current, ...added]);
    event.target.value = '';
  }

  function removeFile(index: number) {
    if (operationRef.current !== null || recoveryRequiredRef.current) return;
    setFiles((current) => current.filter((_, fileIndex) => fileIndex !== index));
    setFileError(null);
  }

  async function handleSubmit(event: React.FormEvent) {
    event.preventDefault();
    if (operationRef.current !== null || recoveryRequiredRef.current) return;
    if (!title.trim() || !content.trim()) {
      setFormError('제목과 내용을 입력해주세요.');
      return;
    }

    const operation = { id: Symbol('notice-create') };
    operationRef.current = operation;
    adminMutationBoundary.acquire(operation);
    const ownsOperation = () => operationRef.current === operation;
    const isCurrent = () => mountedRef.current && ownsOperation();
    setCreating(true);
    setFormError(null);

    try {
      await createNotice({
        title: title.trim(),
        content: content.trim(),
        isPinned,
        attachments: files.length > 0 ? files : undefined,
      });
      if (isCurrent()) {
        operationRef.current = null;
        resetBlockedNavigation();
        setCreating(false);
        navigate('/notices');
      }
    } catch (error) {
      if (ownsOperation()) {
        if (isAmbiguousMutationError(error)) {
          requireNoticeCreateObservation();
          recoveryRequiredRef.current = true;
          if (mountedRef.current) setOutcomeUnknown(true);
        } else if (mountedRef.current && classifyLoadError(error) !== 'cancelled') {
          setFormError('공지사항을 등록하지 못했습니다. 입력 내용은 유지되었습니다.');
        }
      }
    } finally {
      adminMutationBoundary.release(operation);
      if (operationRef.current === operation) {
        operationRef.current = null;
        resetBlockedNavigation();
        if (mountedRef.current) setCreating(false);
      }
    }
  }

  return (
    <div className={styles.page}>
      <h1 className={styles.title}>공지사항 작성</h1>

      <form className={styles.form} onSubmit={handleSubmit}>
        <div className={styles.formGroup}>
          <label htmlFor="notice-create-title" className={styles.formLabel}>
            제목
          </label>
          <input
            id="notice-create-title"
            className={styles.formInput}
            placeholder="공지사항 제목"
            maxLength={TITLE_NOTICE_MAX}
            value={title}
            onChange={(event) => setTitle(event.target.value)}
            disabled={busy}
            required
          />
        </div>

        <div className={styles.formGroup}>
          <label htmlFor="notice-create-content" className={styles.formLabel}>
            내용
          </label>
          <textarea
            id="notice-create-content"
            className={styles.formTextarea}
            placeholder="공지사항 내용"
            maxLength={DESCRIPTION_MAX}
            value={content}
            onChange={(event) => setContent(event.target.value)}
            disabled={busy}
            required
          />
          <span className={styles.characterCount}>{`${content.length}/${DESCRIPTION_MAX}`}</span>
        </div>

        <div className={styles.formGroup}>
          <div className={styles.checkboxRow}>
            <input
              type="checkbox"
              id="notice-create-pinned"
              checked={isPinned}
              onChange={(event) => setIsPinned(event.target.checked)}
              disabled={busy}
            />
            <label htmlFor="notice-create-pinned" className={styles.checkboxLabel}>
              상단 고정
            </label>
          </div>
        </div>

        <div className={styles.formGroup}>
          <label htmlFor="notice-create-files" className={styles.formLabel}>
            첨부파일
          </label>
          <label className={`${styles.fileLabel} ${busy ? styles.fileLabelDisabled : ''}`}>
            파일 선택
            <input
              id="notice-create-files"
              ref={fileInputRef}
              type="file"
              multiple
              className={styles.fileHidden}
              onChange={handleFileChange}
              disabled={busy}
            />
          </label>
          {fileError && (
            <p className={styles.fieldError} role="alert">
              {fileError}
            </p>
          )}
          {files.length > 0 && (
            <ul className={styles.fileList}>
              {files.map((file, index) => (
                <li key={`${file.name}-${file.lastModified}-${index}`} className={styles.fileItem}>
                  <span className={styles.fileName}>{file.name}</span>
                  <span className={styles.fileSize}>{formatFileSize(file.size)}</span>
                  <button
                    type="button"
                    className={styles.fileRemove}
                    aria-label={`${file.name} 제거`}
                    title={`${file.name} 제거`}
                    onClick={() => removeFile(index)}
                    disabled={busy}
                  >
                    X
                  </button>
                </li>
              ))}
            </ul>
          )}
        </div>

        {formError && (
          <p className={styles.actionError} role="alert">
            {formError}
          </p>
        )}

        {outcomeUnknown && (
          <section className={styles.outcomeUnknown} role="alert" aria-live="assertive">
            <strong>처리 결과 확인 필요</strong>
            <p>
              요청 응답을 확인할 수 없습니다. 같은 공지사항을 다시 등록하지 말고 목록에서 처리
              결과를 확인해주세요.
            </p>
            <Link to="/notices" className={styles.observationLink}>
              공지사항 목록에서 확인
            </Link>
          </section>
        )}

        <div className={styles.formActions}>
          <Button variant="ghost" type="button" onClick={() => navigate(-1)} disabled={busy}>
            취소
          </Button>
          <Button type="submit" loading={creating} disabled={busy}>
            등록
          </Button>
        </div>
      </form>
    </div>
  );
}

/** Screen 21-2: Notice edit (ADMIN) */
import { useEffect, useRef, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import {
  deleteNotice,
  fetchAdminNotice,
  updateNotice,
  type NoticeAdminProjection,
} from '@/api/notices';
import { classifyLoadError, isAmbiguousMutationError, type LoadErrorKind } from '@/api/loadError';
import usePendingMutationGuard from '@/hooks/usePendingMutationGuard';
import { useAdminMutationBoundary } from '@/layouts/AdminMutationBoundary';
import type { NoticeAttachmentInfo, UserRole } from '@/types';
import { useAuthStore } from '@/store/authStore';
import { createReadKey } from '@/utils/ownerProjection';
import { parsePositiveDecimalRouteID } from '@/utils/routeId';
import {
  ATTACHMENT_MAX_COUNT,
  ATTACHMENT_MAX_SIZE_MB,
  DESCRIPTION_MAX,
  TITLE_NOTICE_MAX,
  isFileSizeOk,
} from '@/utils/validation';
import Button from '@/components/ui/Button';
import Modal from '@/components/ui/Modal';
import styles from './NoticeEditPage.module.css';

function formatFileSize(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}

type OperationKind = 'saving' | 'deleting';
type VisibleLoadError = Exclude<LoadErrorKind, 'cancelled'>;

interface MutationOperation {
  readonly kind: OperationKind;
  readonly readKey: string;
}

function createNoticeOwnerKey(userID: number | null, role: UserRole): string | null {
  if (userID === null || role !== 'ADMIN') return null;
  return JSON.stringify([userID, role]);
}

function getCurrentNoticeOwnerKey(fallbackOwnerKey: string | null): string | null {
  const getState = useAuthStore.getState;
  if (typeof getState !== 'function') return fallbackOwnerKey;
  const { user, role } = getState();
  return createNoticeOwnerKey(user?.id ?? null, role);
}

export default function NoticeEditPage() {
  const { noticeId } = useParams<{ noticeId: string }>();
  const navigate = useNavigate();
  const adminMutationBoundary = useAdminMutationBoundary();
  const parsedNoticeID = parsePositiveDecimalRouteID(noticeId);
  const userID = useAuthStore((state) => state.user?.id ?? null);
  const role = useAuthStore((state) => state.role);
  const ownerKey = createNoticeOwnerKey(userID, role);
  const readKey = createReadKey(ownerKey, 'notice-edit', parsedNoticeID);
  const currentReadKeyRef = useRef(readKey);
  currentReadKeyRef.current = readKey;

  const [projection, setProjection] = useState<NoticeAdminProjection | null>(null);
  const projectionKeyRef = useRef<string | null>(null);
  const [projectionKey, setProjectionKey] = useState<string | null>(null);
  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [isPinned, setIsPinned] = useState(false);
  const [existingAttachments, setExistingAttachments] = useState<NoticeAttachmentInfo[]>([]);
  const [deleteAttachmentIDs, setDeleteAttachmentIDs] = useState<Set<number>>(new Set());
  const [newFiles, setNewFiles] = useState<File[]>([]);

  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState<VisibleLoadError | null>(null);
  const [errorKey, setErrorKey] = useState<string | null>(null);
  const [retryGeneration, setRetryGeneration] = useState(0);
  const loadGenerationRef = useRef(0);
  const loadControllerRef = useRef<AbortController | null>(null);
  const loadInFlightRef = useRef(false);

  const [operation, setOperation] = useState<OperationKind | null>(null);
  const operationRef = useRef<MutationOperation | null>(null);
  const recoveryRequiredRef = useRef(false);
  const mountedRef = useRef(true);
  const [outcomeUnknown, setOutcomeUnknown] = useState<OperationKind | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);
  const [fileError, setFileError] = useState<string | null>(null);
  const [deleteOpen, setDeleteOpen] = useState(false);
  const [deleteError, setDeleteError] = useState<string | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);
  const resetBlockedNavigation = usePendingMutationGuard(operationRef, operation !== null);

  const validID = parsedNoticeID !== null;
  const projectionCurrent = readKey !== null && projectionKey === readKey;
  const currentProjection = projectionCurrent ? projection : null;
  const currentLoadError = errorKey === readKey ? loadError : null;
  const currentLoading = loading || (!projectionCurrent && currentLoadError === null);
  const busy = operation !== null || outcomeUnknown !== null;

  function isCurrentProjection(expectedReadKey = readKey): boolean {
    return (
      expectedReadKey !== null &&
      mountedRef.current &&
      currentReadKeyRef.current === expectedReadKey &&
      projectionKeyRef.current === expectedReadKey &&
      getCurrentNoticeOwnerKey(ownerKey) === ownerKey
    );
  }

  useEffect(() => {
    mountedRef.current = true;
    return () => {
      mountedRef.current = false;
    };
  }, []);

  useEffect(() => {
    loadControllerRef.current?.abort();
    loadControllerRef.current = null;
    setDeleteOpen(false);
    setDeleteError(null);
    setActionError(null);
    setFileError(null);

    if (parsedNoticeID === null || readKey === null) {
      loadGenerationRef.current += 1;
      projectionKeyRef.current = null;
      setProjectionKey(null);
      setProjection(null);
      setLoading(false);
      setLoadError(null);
      setErrorKey(null);
      loadInFlightRef.current = false;
      return;
    }

    const requestKey = readKey;
    const requestOwnerKey = ownerKey;
    const controller = new AbortController();
    loadControllerRef.current = controller;
    const generation = ++loadGenerationRef.current;
    const isCurrent = () =>
      generation === loadGenerationRef.current &&
      currentReadKeyRef.current === requestKey &&
      getCurrentNoticeOwnerKey(requestOwnerKey) === requestOwnerKey &&
      !controller.signal.aborted;

    loadInFlightRef.current = true;
    projectionKeyRef.current = null;
    setProjectionKey(null);
    setProjection(null);
    setTitle('');
    setContent('');
    setIsPinned(false);
    setExistingAttachments([]);
    setDeleteAttachmentIDs(new Set());
    setNewFiles([]);
    setLoadError(null);
    setErrorKey(null);
    setLoading(true);

    fetchAdminNotice(parsedNoticeID, controller.signal)
      .then((result) => {
        if (!isCurrent()) return;
        setProjection(result);
        setTitle(result.title);
        setContent(result.content);
        setIsPinned(result.isPinned);
        setExistingAttachments(result.attachments ?? []);
        projectionKeyRef.current = requestKey;
        setProjectionKey(requestKey);
        recoveryRequiredRef.current = false;
        setOutcomeUnknown(null);
      })
      .catch((error: unknown) => {
        if (!isCurrent()) return;
        const kind = classifyLoadError(error);
        if (kind !== 'cancelled') {
          setLoadError(kind);
          setErrorKey(requestKey);
        }
      })
      .finally(() => {
        if (isCurrent()) {
          setLoading(false);
          loadInFlightRef.current = false;
        }
      });

    return () => {
      controller.abort();
      if (generation === loadGenerationRef.current) loadGenerationRef.current += 1;
      loadInFlightRef.current = false;
    };
  }, [ownerKey, parsedNoticeID, readKey, retryGeneration]);

  function retryLoad() {
    if (!validID || readKey === null || loadInFlightRef.current) return;
    loadInFlightRef.current = true;
    setRetryGeneration((generation) => generation + 1);
  }

  function observeCurrentState() {
    if (outcomeUnknown === null || operationRef.current !== null) return;
    retryLoad();
  }

  function handleFileChange(event: React.ChangeEvent<HTMLInputElement>) {
    if (
      !isCurrentProjection() ||
      operationRef.current !== null ||
      recoveryRequiredRef.current ||
      !event.target.files
    ) {
      return;
    }
    const added = Array.from(event.target.files);
    const retainedCount = existingAttachments.length - deleteAttachmentIDs.size;
    if (retainedCount + newFiles.length + added.length > ATTACHMENT_MAX_COUNT) {
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
    setNewFiles((current) => [...current, ...added]);
    event.target.value = '';
  }

  function toggleExistingAttachment(attachmentID: number) {
    if (!isCurrentProjection() || operationRef.current !== null || recoveryRequiredRef.current) {
      return;
    }
    setDeleteAttachmentIDs((current) => {
      const next = new Set(current);
      if (next.has(attachmentID)) next.delete(attachmentID);
      else next.add(attachmentID);
      return next;
    });
    setFileError(null);
  }

  function removeNewFile(index: number) {
    if (!isCurrentProjection() || operationRef.current !== null || recoveryRequiredRef.current) {
      return;
    }
    setNewFiles((current) => current.filter((_, fileIndex) => fileIndex !== index));
    setFileError(null);
  }

  async function handleSubmit(event: React.FormEvent) {
    event.preventDefault();
    const operationKey = readKey;
    if (
      parsedNoticeID === null ||
      operationKey === null ||
      operationRef.current !== null ||
      recoveryRequiredRef.current ||
      !isCurrentProjection(operationKey)
    ) {
      return;
    }
    if (!title.trim() || !content.trim()) {
      setActionError('제목과 내용을 입력해주세요.');
      return;
    }

    const nextOperation: MutationOperation = {
      kind: 'saving',
      readKey: operationKey,
    };
    operationRef.current = nextOperation;
    adminMutationBoundary.acquire(nextOperation);
    const isCurrent = () =>
      operationRef.current === nextOperation && isCurrentProjection(operationKey);
    setOperation('saving');
    setActionError(null);

    try {
      await updateNotice(parsedNoticeID, {
        title: title.trim(),
        content: content.trim(),
        isPinned,
        deleteAttachmentIds:
          deleteAttachmentIDs.size > 0 ? Array.from(deleteAttachmentIDs) : undefined,
        newAttachments: newFiles.length > 0 ? newFiles : undefined,
      });
      if (isCurrent()) {
        operationRef.current = null;
        resetBlockedNavigation();
        setOperation(null);
        navigate(`/notices/${parsedNoticeID}`);
      }
    } catch (error) {
      if (isCurrent()) {
        if (isAmbiguousMutationError(error)) {
          recoveryRequiredRef.current = true;
          setOutcomeUnknown('saving');
        } else if (classifyLoadError(error) !== 'cancelled') {
          setActionError('공지사항을 수정하지 못했습니다. 입력 내용은 유지되었습니다.');
        }
      }
    } finally {
      adminMutationBoundary.release(nextOperation);
      if (operationRef.current === nextOperation) {
        operationRef.current = null;
        resetBlockedNavigation();
        if (mountedRef.current) setOperation(null);
      }
    }
  }

  async function confirmDelete() {
    const operationKey = readKey;
    if (
      parsedNoticeID === null ||
      operationKey === null ||
      operationRef.current !== null ||
      recoveryRequiredRef.current ||
      !isCurrentProjection(operationKey)
    ) {
      return;
    }

    const nextOperation: MutationOperation = {
      kind: 'deleting',
      readKey: operationKey,
    };
    operationRef.current = nextOperation;
    adminMutationBoundary.acquire(nextOperation);
    const isCurrent = () =>
      operationRef.current === nextOperation && isCurrentProjection(operationKey);
    setOperation('deleting');
    setDeleteError(null);

    try {
      await deleteNotice(parsedNoticeID);
      if (isCurrent()) {
        operationRef.current = null;
        resetBlockedNavigation();
        setOperation(null);
        navigate('/notices');
      }
    } catch (error) {
      if (isCurrent()) {
        if (isAmbiguousMutationError(error)) {
          recoveryRequiredRef.current = true;
          setOutcomeUnknown('deleting');
          setDeleteOpen(false);
        } else if (classifyLoadError(error) !== 'cancelled') {
          setDeleteError('공지사항을 삭제하지 못했습니다. 다시 시도해주세요.');
        }
      }
    } finally {
      adminMutationBoundary.release(nextOperation);
      if (operationRef.current === nextOperation) {
        operationRef.current = null;
        resetBlockedNavigation();
        if (mountedRef.current) setOperation(null);
      }
    }
  }

  if (!validID || readKey === null) {
    return (
      <div className={styles.page}>
        <div className={styles.recovery} role="alert">
          <p>올바르지 않은 공지사항 주소입니다.</p>
          <Link to="/notices" className={styles.backLink}>
            공지사항 목록으로
          </Link>
        </div>
      </div>
    );
  }

  if (currentLoading) {
    return (
      <div className={styles.page}>
        <div className={styles.loading}>공지사항을 불러오는 중...</div>
      </div>
    );
  }

  if (currentLoadError || !currentProjection) {
    const missing = currentLoadError === 'not-found';
    return (
      <div className={styles.page}>
        <div className={styles.recovery} role="alert">
          <p>{missing ? '공지사항을 찾을 수 없습니다.' : '공지사항을 불러오지 못했습니다.'}</p>
          <div className={styles.recoveryActions}>
            <Link to="/notices" className={styles.backLink}>
              공지사항 목록으로
            </Link>
            {!missing && (
              <Button type="button" size="sm" onClick={retryLoad}>
                다시 시도
              </Button>
            )}
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className={styles.page}>
      <h1 className={styles.title}>공지사항 수정</h1>

      <form className={styles.form} onSubmit={handleSubmit}>
        <div className={styles.formGroup}>
          <label htmlFor="notice-edit-title" className={styles.formLabel}>
            제목
          </label>
          <input
            id="notice-edit-title"
            className={styles.formInput}
            maxLength={TITLE_NOTICE_MAX}
            value={title}
            onChange={(event) => setTitle(event.target.value)}
            disabled={busy}
            required
          />
        </div>

        <div className={styles.formGroup}>
          <label htmlFor="notice-edit-content" className={styles.formLabel}>
            내용
          </label>
          <textarea
            id="notice-edit-content"
            className={styles.formTextarea}
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
              id="notice-edit-pinned"
              checked={isPinned}
              onChange={(event) => setIsPinned(event.target.checked)}
              disabled={busy}
            />
            <label htmlFor="notice-edit-pinned" className={styles.checkboxLabel}>
              상단 고정
            </label>
          </div>
        </div>

        <div className={styles.formGroup}>
          <label htmlFor="notice-edit-files" className={styles.formLabel}>
            첨부파일
          </label>
          <label className={`${styles.fileLabel} ${busy ? styles.fileLabelDisabled : ''}`}>
            파일 추가
            <input
              id="notice-edit-files"
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

          {(existingAttachments.length > 0 || newFiles.length > 0) && (
            <ul className={styles.fileList}>
              {existingAttachments.map((attachment) => {
                const markedForDeletion = deleteAttachmentIDs.has(attachment.id);
                return (
                  <li
                    key={`existing-${attachment.id}`}
                    className={`${styles.fileItem} ${markedForDeletion ? styles.fileItemDeleted : ''}`}
                  >
                    <span className={styles.existingBadge}>
                      {markedForDeletion ? '삭제 예정' : '기존 파일'}
                    </span>
                    <span className={styles.fileName}>{attachment.originalName}</span>
                    <span className={styles.fileSize}>{formatFileSize(attachment.fileSize)}</span>
                    <button
                      type="button"
                      className={styles.fileRemove}
                      aria-label={`${attachment.originalName} ${
                        markedForDeletion ? '삭제 취소' : '삭제'
                      }`}
                      title={markedForDeletion ? '삭제 취소' : '삭제'}
                      onClick={() => toggleExistingAttachment(attachment.id)}
                      disabled={busy}
                    >
                      {markedForDeletion ? '↶' : 'X'}
                    </button>
                  </li>
                );
              })}
              {newFiles.map((file, index) => (
                <li
                  key={`new-${file.name}-${file.lastModified}-${index}`}
                  className={styles.fileItem}
                >
                  <span className={styles.existingBadge}>새 파일</span>
                  <span className={styles.fileName}>{file.name}</span>
                  <span className={styles.fileSize}>{formatFileSize(file.size)}</span>
                  <button
                    type="button"
                    className={styles.fileRemove}
                    aria-label={`${file.name} 제거`}
                    title={`${file.name} 제거`}
                    onClick={() => removeNewFile(index)}
                    disabled={busy}
                  >
                    X
                  </button>
                </li>
              ))}
            </ul>
          )}
        </div>

        {actionError && (
          <p className={styles.actionError} role="alert">
            {actionError}
          </p>
        )}

        {outcomeUnknown && (
          <section className={styles.outcomeUnknown} role="alert" aria-live="assertive">
            <strong>처리 결과 확인 필요</strong>
            <p>
              요청 응답을 확인할 수 없습니다. 같은 작업을 다시 실행하기 전에 현재 공지사항 상태를
              조회해주세요.
            </p>
            <div className={styles.observationActions}>
              <Button type="button" size="sm" onClick={observeCurrentState}>
                현재 상태 다시 확인
              </Button>
              <Link to="/notices" className={styles.backLink}>
                목록에서 확인
              </Link>
            </div>
          </section>
        )}

        <div className={styles.formActions}>
          <Button
            variant="danger"
            type="button"
            onClick={() => {
              if (operationRef.current === null) {
                setDeleteError(null);
                setDeleteOpen(true);
              }
            }}
            disabled={busy}
          >
            공지사항 삭제
          </Button>
          <div className={styles.formActionsRight}>
            <Button variant="ghost" type="button" onClick={() => navigate(-1)} disabled={busy}>
              취소
            </Button>
            <Button type="submit" loading={operation === 'saving'} disabled={busy}>
              저장
            </Button>
          </div>
        </div>
      </form>

      <Modal
        open={deleteOpen}
        onClose={() => {
          if (operationRef.current === null) setDeleteOpen(false);
        }}
        title="공지사항 삭제"
        busy={operation === 'deleting'}
      >
        <p className={styles.deleteText}>
          {operation === 'deleting'
            ? '삭제 요청을 처리 중입니다. 완료될 때까지 이 창을 닫을 수 없습니다.'
            : '이 공지사항을 삭제하시겠습니까? 삭제한 공지사항은 복구할 수 없습니다.'}
        </p>
        {deleteError && (
          <p className={styles.modalError} role="alert">
            {deleteError}
          </p>
        )}
        <div className={styles.modalActions}>
          <Button variant="ghost" size="sm" onClick={() => setDeleteOpen(false)} disabled={busy}>
            취소
          </Button>
          <Button
            variant="danger"
            size="sm"
            loading={operation === 'deleting'}
            onClick={() => void confirmDelete()}
            disabled={busy}
          >
            삭제
          </Button>
        </div>
      </Modal>
    </div>
  );
}

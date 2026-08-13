/** Screen 15: Question detail */
import { useState, useEffect, useCallback, useRef } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import {
  fetchQuestionDetail,
  deleteQuestion,
  downloadAttachment,
  createAnswer,
  type QuestionDetail,
} from '@/api/questions';
import { classifyLoadError } from '@/api/loadError';
import { formatDate } from '@/utils/format';
import { parsePositiveDecimalRouteID } from '@/utils/routeId';
import { useAuthStore } from '@/store/authStore';
import { createOwnerKey, createReadKey, getCurrentOwnerKey } from '@/utils/ownerProjection';
import Button from '@/components/ui/Button';
import Modal from '@/components/ui/Modal';
import styles from './QuestionDetailPage.module.css';

/* ── Constants ── */

const CATEGORY_LABELS: Record<string, string> = {
  DOWNLOAD: '다운로드',
  PAYMENT: '결제',
  COPYRIGHT: '저작권',
  PRODUCTION: '제작',
  OTHER: '기타',
};

const STATUS_LABELS: Record<string, string> = {
  OPEN: '접수',
  IN_PROGRESS: '처리중',
  RESOLVED: '해결',
  CLOSED: '종료',
};

function formatFileSize(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}

function statusClass(status: string): string {
  const map: Record<string, string> = {
    OPEN: styles.statusOPEN,
    IN_PROGRESS: styles.statusIN_PROGRESS,
    RESOLVED: styles.statusRESOLVED,
    CLOSED: styles.statusCLOSED,
  };
  return `${styles.statusBadge} ${map[status] ?? ''}`;
}

export default function QuestionDetailPage() {
  const { questionId } = useParams<{ questionId: string }>();
  const navigate = useNavigate();
  const parsedID = parsePositiveDecimalRouteID(questionId);
  const validID = parsedID !== null;

  const currentUser = useAuthStore((s) => s.user);
  const role = useAuthStore((s) => s.role);
  const accessToken = useAuthStore((s) => s.accessToken);
  const [question, setQuestion] = useState<QuestionDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [deleteOpen, setDeleteOpen] = useState(false);
  const [deleting, setDeleting] = useState(false);

  const [answerContent, setAnswerContent] = useState('');
  const [answerSubmitting, setAnswerSubmitting] = useState(false);
  const [answerError, setAnswerError] = useState<string | null>(null);
  const requestGeneration = useRef(0);
  const requestController = useRef<AbortController | null>(null);
  const ownerKey = createOwnerKey(currentUser?.id ?? null, accessToken);
  const readKey = createReadKey(ownerKey, 'question-detail', parsedID);
  const currentReadKeyRef = useRef(readKey);
  const projectionKeyRef = useRef<string | null>(null);
  const [projectionKey, setProjectionKey] = useState<string | null>(null);
  const [errorKey, setErrorKey] = useState<string | null>(null);
  currentReadKeyRef.current = readKey;
  const projectionCurrent = readKey !== null && projectionKey === readKey;
  const currentQuestion = projectionCurrent ? question : null;
  const currentError = errorKey === readKey ? error : null;
  const currentLoading = loading || (!projectionCurrent && currentError === null);

  function isCurrentProjection(expectedReadKey = readKey): boolean {
    return (
      expectedReadKey !== null &&
      currentReadKeyRef.current === expectedReadKey &&
      projectionKeyRef.current === expectedReadKey &&
      getCurrentOwnerKey(ownerKey) === ownerKey
    );
  }

  const load = useCallback(async () => {
    const requestKey = readKey;
    const requestOwnerKey = ownerKey;
    if (parsedID === null || requestKey === null) return;
    requestController.current?.abort();
    const controller = new AbortController();
    requestController.current = controller;
    const generation = ++requestGeneration.current;
    const isCurrent = () =>
      generation === requestGeneration.current &&
      currentReadKeyRef.current === requestKey &&
      getCurrentOwnerKey(requestOwnerKey) === requestOwnerKey;
    try {
      setLoading(true);
      setError(null);
      setQuestion(null);
      const result = await fetchQuestionDetail(parsedID, controller.signal);
      if (isCurrent()) {
        setQuestion(result);
        projectionKeyRef.current = requestKey;
        setProjectionKey(requestKey);
      }
    } catch (loadError) {
      if (isCurrent() && classifyLoadError(loadError) !== 'cancelled') {
        setError('문의를 불러오지 못했습니다.');
        setErrorKey(requestKey);
      }
    } finally {
      if (isCurrent()) setLoading(false);
    }
  }, [ownerKey, parsedID, readKey]);

  useEffect(() => {
    if (!validID) {
      requestController.current?.abort();
      requestGeneration.current += 1;
      setQuestion(null);
      setLoading(false);
      setError(null);
      return;
    }
    void load();
    return () => {
      requestController.current?.abort();
      requestGeneration.current += 1;
    };
  }, [accessToken, currentUser?.id, load, validID]);

  async function handleDelete() {
    const operationKey = readKey;
    if (parsedID === null || !isCurrentProjection(operationKey)) return;
    try {
      setDeleting(true);
      await deleteQuestion(parsedID);
      if (!isCurrentProjection(operationKey)) return;
      navigate('/questions');
    } catch {
      if (!isCurrentProjection(operationKey)) return;
      setError('삭제에 실패했습니다.');
      setErrorKey(operationKey);
      setDeleteOpen(false);
    } finally {
      if (isCurrentProjection(operationKey)) setDeleting(false);
    }
  }

  async function handleAnswerSubmit(e: React.FormEvent) {
    e.preventDefault();
    const operationKey = readKey;
    if (parsedID === null || !answerContent.trim() || !isCurrentProjection(operationKey)) return;
    try {
      setAnswerSubmitting(true);
      setAnswerError(null);
      await createAnswer(parsedID, answerContent.trim());
      if (!isCurrentProjection(operationKey)) return;
      setAnswerContent('');
      await load();
    } catch {
      if (!isCurrentProjection(operationKey)) return;
      setAnswerError('답변 등록에 실패했습니다.');
    } finally {
      if (isCurrentProjection(operationKey)) setAnswerSubmitting(false);
    }
  }

  if (parsedID === null) {
    return (
      <div className={styles.page}>
        <div className={styles.error}>{'문의 주소가 올바르지 않습니다.'}</div>
        <Link to="/questions" className={styles.backLink}>
          {'문의 목록으로'}
        </Link>
      </div>
    );
  }

  if (currentLoading) {
    return (
      <div className={styles.page}>
        <div className={styles.loading}>{'불러오는 중...'}</div>
      </div>
    );
  }

  if (currentError || !currentQuestion) {
    return (
      <div className={styles.page}>
        <div className={styles.error}>{currentError ?? '문의를 찾을 수 없습니다.'}</div>
        <Link to="/questions" className={styles.backLink}>
          {'목록으로'}
        </Link>
      </div>
    );
  }

  const attachments = currentQuestion.attachments ?? [];
  const answers = currentQuestion.answers ?? [];

  return (
    <div className={styles.page}>
      {/* Back */}
      <Link to="/questions" className={styles.backLink}>
        {'< 목록으로'}
      </Link>

      {/* Header */}
      <div className={styles.header}>
        <h1 className={styles.title}>{currentQuestion.title}</h1>
        <div className={styles.meta}>
          <span className={`${styles.categoryBadge}`}>
            {CATEGORY_LABELS[currentQuestion.category] ?? currentQuestion.category}
          </span>
          <span className={statusClass(currentQuestion.status)}>
            {STATUS_LABELS[currentQuestion.status] ?? currentQuestion.status}
          </span>
          <span className={styles.date}>{formatDate(currentQuestion.createdAt)}</span>
          {!currentQuestion.isPublic && <span className={styles.privateBadge}>{'비공개'}</span>}
        </div>
      </div>

      {/* Content */}
      <div className={styles.content}>{currentQuestion.content}</div>

      {/* Attachments */}
      {attachments.length > 0 && (
        <div className={styles.section}>
          <h2 className={styles.sectionTitle}>{'첨부파일'}</h2>
          <ul className={styles.attachList}>
            {attachments.map((att) => (
              <li key={att.id} className={styles.attachItem}>
                <button
                  type="button"
                  className={styles.attachLink}
                  onClick={() => {
                    if (isCurrentProjection()) {
                      void downloadAttachment(currentQuestion.id, att.id, att.originalName);
                    }
                  }}
                >
                  {att.originalName}
                </button>
                <span className={styles.attachSize}>{formatFileSize(att.fileSize)}</span>
              </li>
            ))}
          </ul>
        </div>
      )}

      {/* Answers */}
      {answers.length > 0 && (
        <div className={styles.section}>
          <h2 className={styles.sectionTitle}>
            {'답변'} ({answers.length})
          </h2>
          <div className={styles.answerList}>
            {answers.map((answer) => (
              <div key={answer.id} className={styles.answerCard}>
                <div className={styles.answerHeader}>
                  <span className={styles.answerUser}>
                    {answer.user.nickname}
                    {answer.user.role === 'ADMIN' && (
                      <span className={styles.adminBadge}>{'관리자'}</span>
                    )}
                  </span>
                  <span className={styles.answerDate}>{formatDate(answer.createdAt)}</span>
                </div>
                <div className={styles.answerContent}>{answer.content}</div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Answer Form — ADMIN or question owner, not CLOSED */}
      {currentQuestion.status !== 'CLOSED' &&
        currentUser &&
        (role === 'ADMIN' || currentQuestion.user?.id === currentUser.id) && (
          <div className={styles.section}>
            <h2 className={styles.sectionTitle}>{'답변 작성'}</h2>
            <form className={styles.answerForm} onSubmit={handleAnswerSubmit}>
              <textarea
                className={styles.answerTextarea}
                value={answerContent}
                onChange={(e) => setAnswerContent(e.target.value)}
                placeholder="답변 내용을 입력하세요"
                rows={4}
                required
                disabled={answerSubmitting}
              />
              {answerError && <p className={styles.answerFormError}>{answerError}</p>}
              <div className={styles.answerFormActions}>
                <Button
                  type="submit"
                  size="sm"
                  loading={answerSubmitting}
                  disabled={!answerContent.trim()}
                >
                  {'답변 등록'}
                </Button>
              </div>
            </form>
          </div>
        )}

      {/* Actions — owner only */}
      {currentQuestion.user && currentUser && currentQuestion.user.id === currentUser.id && (
        <div className={styles.actions}>
          <Button
            variant="danger"
            size="sm"
            onClick={() => {
              if (isCurrentProjection()) setDeleteOpen(true);
            }}
          >
            {'삭제'}
          </Button>
        </div>
      )}

      {/* Delete confirm modal */}
      <Modal
        open={projectionCurrent && deleteOpen}
        onClose={() => setDeleteOpen(false)}
        title="문의 삭제"
      >
        <div className={styles.modalBody}>
          {'이 문의를 삭제하시겠습니까? 삭제 후 복구할 수 없습니다.'}
        </div>
        <div className={styles.modalActions}>
          <Button variant="ghost" size="sm" onClick={() => setDeleteOpen(false)}>
            {'취소'}
          </Button>
          <Button variant="danger" size="sm" loading={deleting} onClick={handleDelete}>
            {'삭제'}
          </Button>
        </div>
      </Modal>
    </div>
  );
}

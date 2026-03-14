/** Screen 15: Question detail */
import { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import {
  fetchQuestionDetail,
  deleteQuestion,
  downloadAttachment,
  type QuestionDetail,
} from '@/api/questions';
import { formatDate } from '@/utils/format';
import { useAuthStore } from '@/store/authStore';
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
  const { questionId: id } = useParams<{ questionId: string }>();
  const navigate = useNavigate();

  const currentUser = useAuthStore((s) => s.user);
  const [question, setQuestion] = useState<QuestionDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [deleteOpen, setDeleteOpen] = useState(false);
  const [deleting, setDeleting] = useState(false);

  const load = useCallback(async () => {
    if (!id) return;
    try {
      setLoading(true);
      setError(null);
      const result = await fetchQuestionDetail(Number(id));
      setQuestion(result);
    } catch {
      setError('문의를 불러오지 못했습니다.');
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    load();
  }, [load]);

  async function handleDelete() {
    if (!id) return;
    try {
      setDeleting(true);
      await deleteQuestion(Number(id));
      navigate('/questions');
    } catch {
      setError('삭제에 실패했습니다.');
      setDeleteOpen(false);
    } finally {
      setDeleting(false);
    }
  }

  if (loading) {
    return (
      <div className={styles.page}>
        <div className={styles.loading}>{'불러오는 중...'}</div>
      </div>
    );
  }

  if (error || !question) {
    return (
      <div className={styles.page}>
        <div className={styles.error}>{error ?? '문의를 찾을 수 없습니다.'}</div>
        <Link to="/questions" className={styles.backLink}>{'목록으로'}</Link>
      </div>
    );
  }

  const attachments = question.attachments ?? [];
  const answers = question.answers ?? [];

  return (
    <div className={styles.page}>
      {/* Back */}
      <Link to="/questions" className={styles.backLink}>{'< 목록으로'}</Link>

      {/* Header */}
      <div className={styles.header}>
        <h1 className={styles.title}>{question.title}</h1>
        <div className={styles.meta}>
          <span className={`${styles.categoryBadge}`}>
            {CATEGORY_LABELS[question.category] ?? question.category}
          </span>
          <span className={statusClass(question.status)}>
            {STATUS_LABELS[question.status] ?? question.status}
          </span>
          <span className={styles.date}>{formatDate(question.createdAt)}</span>
          {!question.isPublic && <span className={styles.privateBadge}>{'비공개'}</span>}
        </div>
      </div>

      {/* Content */}
      <div className={styles.content}>{question.content}</div>

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
                  onClick={() => downloadAttachment(question.id, att.id, att.originalName)}
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
          <h2 className={styles.sectionTitle}>{'답변'} ({answers.length})</h2>
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

      {/* Actions — owner only */}
      {question.user && currentUser && question.user.id === currentUser.id && (
        <div className={styles.actions}>
          <Button variant="danger" size="sm" onClick={() => setDeleteOpen(true)}>
            {'삭제'}
          </Button>
        </div>
      )}

      {/* Delete confirm modal */}
      <Modal
        open={deleteOpen}
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

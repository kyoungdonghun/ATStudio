/** Screen K-4: Question management (admin) */
import { useState, useEffect, useCallback, useRef } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { fetchQuestions, updateQuestionStatus, type QuestionListItem } from '@/api/questions';
import type { PageInfo, QuestionStatus } from '@/types';
import { formatDate } from '@/utils/format';
import Pagination from '@/components/ui/Pagination';
import styles from './QuestionManagePage.module.css';

/* ── Constants ── */

type QuestionCategory = 'DOWNLOAD' | 'PAYMENT' | 'COPYRIGHT' | 'PRODUCTION' | 'OTHER';

interface QuestionListProjection {
  key: string;
  page: number;
  category?: QuestionCategory;
  status?: QuestionStatus;
}

const CATEGORY_LABELS: Record<QuestionCategory, string> = {
  DOWNLOAD: '다운로드',
  PAYMENT: '결제',
  COPYRIGHT: '저작권',
  PRODUCTION: '제작',
  OTHER: '기타',
};

const STATUS_LABELS: Record<QuestionStatus, string> = {
  OPEN: '접수',
  IN_PROGRESS: '처리중',
  RESOLVED: '해결',
  CLOSED: '종료',
};

const CATEGORY_OPTIONS: Array<{ label: string; value: string }> = [
  { label: '전체', value: '' },
  ...Object.entries(CATEGORY_LABELS).map(([value, label]) => ({ label, value })),
];

const STATUS_OPTIONS: Array<{ label: string; value: string }> = [
  { label: '전체', value: '' },
  ...Object.entries(STATUS_LABELS).map(([value, label]) => ({ label, value })),
];

const LEGAL_STATUS_TRANSITIONS: Record<QuestionStatus, readonly QuestionStatus[]> = {
  OPEN: ['IN_PROGRESS', 'CLOSED'],
  IN_PROGRESS: ['RESOLVED', 'CLOSED'],
  RESOLVED: ['CLOSED'],
  CLOSED: [],
};

function createQuestionListProjection(
  page: number,
  category: string,
  status: string,
): QuestionListProjection {
  return {
    key: `${page}:${category}:${status}`,
    page,
    category: category ? (category as QuestionCategory) : undefined,
    status: status ? (status as QuestionStatus) : undefined,
  };
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

export default function QuestionManagePage() {
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();

  const currentPage = Number(searchParams.get('page')) || 1;
  const categoryFilter = searchParams.get('category') ?? '';
  const statusFilter = searchParams.get('status') ?? '';
  const currentProjection = createQuestionListProjection(currentPage, categoryFilter, statusFilter);

  const [items, setItems] = useState<QuestionListItem[]>([]);
  const [pageInfo, setPageInfo] = useState<PageInfo | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [mutationError, setMutationError] = useState<string | null>(null);
  const [updatingId, setUpdatingId] = useState<number | null>(null);
  const pendingMutationRef = useRef<number | null>(null);
  const currentProjectionRef = useRef(currentProjection);
  const activeListRequestRef = useRef<AbortController | null>(null);
  const listRequestGenerationRef = useRef(0);
  currentProjectionRef.current = currentProjection;

  const load = useCallback(async (projection: QuestionListProjection) => {
    if (currentProjectionRef.current.key !== projection.key) return;

    activeListRequestRef.current?.abort();
    const controller = new AbortController();
    const requestGeneration = ++listRequestGenerationRef.current;
    activeListRequestRef.current = controller;

    try {
      setLoading(true);
      setError(null);
      setMutationError(null);
      const result = await fetchQuestions(
        {
          page: projection.page,
          size: 20,
          mine: false,
          category: projection.category,
          status: projection.status,
        },
        controller.signal,
      );
      if (
        controller.signal.aborted ||
        listRequestGenerationRef.current !== requestGeneration ||
        currentProjectionRef.current.key !== projection.key
      ) {
        return;
      }
      setItems(result.dataList);
      setPageInfo(result.pageInfo);
    } catch {
      if (
        controller.signal.aborted ||
        listRequestGenerationRef.current !== requestGeneration ||
        currentProjectionRef.current.key !== projection.key
      ) {
        return;
      }
      setError('문의 목록을 불러오지 못했습니다.');
    } finally {
      if (
        listRequestGenerationRef.current === requestGeneration &&
        currentProjectionRef.current.key === projection.key
      ) {
        setLoading(false);
      }
      if (activeListRequestRef.current === controller) {
        activeListRequestRef.current = null;
      }
    }
  }, []);

  useEffect(() => {
    void load(createQuestionListProjection(currentPage, categoryFilter, statusFilter));
    return () => activeListRequestRef.current?.abort();
  }, [load, currentPage, categoryFilter, statusFilter]);

  function updateParam(key: string, value: string) {
    const next = new URLSearchParams(searchParams);
    if (value) {
      next.set(key, value);
    } else {
      next.delete(key);
    }
    if (key !== 'page') next.delete('page');
    setSearchParams(next, { replace: true });
  }

  async function handleStatusChange(questionId: number, newStatus: QuestionStatus) {
    if (pendingMutationRef.current !== null) return;
    const currentItem = items.find((item) => item.id === questionId);
    if (!currentItem || !LEGAL_STATUS_TRANSITIONS[currentItem.status].includes(newStatus)) return;
    const initiatingProjectionKey = currentProjectionRef.current.key;

    pendingMutationRef.current = questionId;
    try {
      setUpdatingId(questionId);
      setMutationError(null);
      const updatedQuestion = await updateQuestionStatus(questionId, newStatus);
      const activeProjection = currentProjectionRef.current;
      if (activeProjection.key !== initiatingProjectionKey) {
        await load(activeProjection);
      } else if (
        activeProjection.status !== undefined &&
        activeProjection.status !== updatedQuestion.status
      ) {
        await load(activeProjection);
      } else {
        setItems((prev) =>
          prev.map((item) =>
            item.id === questionId ? { ...item, status: updatedQuestion.status } : item,
          ),
        );
      }
    } catch {
      if (currentProjectionRef.current.key === initiatingProjectionKey) {
        setMutationError('상태 변경에 실패했습니다.');
      }
    } finally {
      if (pendingMutationRef.current === questionId) {
        pendingMutationRef.current = null;
        setUpdatingId(null);
      }
    }
  }

  return (
    <div className={styles.page}>
      {/* Header */}
      <div className={styles.pageHeader}>
        <h1 className={styles.pageTitle}>
          {'문의 관리'}
          {pageInfo && <span className={styles.count}>{pageInfo.total}건</span>}
        </h1>
      </div>

      {/* Filter Bar */}
      <div className={styles.filterBar}>
        <select
          className={styles.filterSelect}
          value={categoryFilter}
          onChange={(e) => updateParam('category', e.target.value)}
        >
          {CATEGORY_OPTIONS.map((opt) => (
            <option key={opt.value} value={opt.value}>
              {opt.label}
            </option>
          ))}
        </select>
        <select
          className={styles.filterSelect}
          value={statusFilter}
          onChange={(e) => updateParam('status', e.target.value)}
        >
          {STATUS_OPTIONS.map((opt) => (
            <option key={opt.value} value={opt.value}>
              {opt.label}
            </option>
          ))}
        </select>
      </div>

      {mutationError && (
        <div className={styles.mutationError} role="alert">
          {mutationError}
        </div>
      )}

      {/* Content */}
      {loading ? (
        <div className={styles.loading}>{'불러오는 중...'}</div>
      ) : error ? (
        <div className={styles.error}>{error}</div>
      ) : items.length === 0 ? (
        <div className={styles.empty}>{'등록된 문의가 없습니다.'}</div>
      ) : (
        <>
          <div className={styles.tableWrap}>
            <table className={styles.table}>
              <thead>
                <tr>
                  <th>{'제목'}</th>
                  <th className={styles.thCenter}>{'카테고리'}</th>
                  <th className={styles.thCenter}>{'상태'}</th>
                  <th className={styles.thCenter}>{'등록일'}</th>
                  <th className={`${styles.thCenter} ${styles.thAction}`}>{'상태 변경'}</th>
                </tr>
              </thead>
              <tbody>
                {items.map((item) => (
                  <tr key={item.id} className={styles.row}>
                    <td
                      className={styles.cellTitle}
                      onClick={() => navigate(`/questions/${item.id}`)}
                    >
                      {item.title}
                      {!item.isPublic && <span className={styles.privateBadge}>{'비공개'}</span>}
                    </td>
                    <td className={styles.cellCenter}>
                      {CATEGORY_LABELS[item.category] ?? item.category}
                    </td>
                    <td className={styles.cellCenter}>
                      <span className={statusClass(item.status)}>
                        {STATUS_LABELS[item.status] ?? item.status}
                      </span>
                    </td>
                    <td className={styles.cellCenter}>{formatDate(item.createdAt)}</td>
                    <td className={styles.cellCenter}>
                      <select
                        className={styles.statusSelect}
                        value={item.status}
                        disabled={
                          updatingId !== null || LEGAL_STATUS_TRANSITIONS[item.status].length === 0
                        }
                        onClick={(e) => e.stopPropagation()}
                        onChange={(e) =>
                          handleStatusChange(item.id, e.target.value as QuestionStatus)
                        }
                      >
                        {[item.status, ...LEGAL_STATUS_TRANSITIONS[item.status]].map((s) => (
                          <option key={s} value={s}>
                            {STATUS_LABELS[s]}
                          </option>
                        ))}
                      </select>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {pageInfo && pageInfo.total > pageInfo.size && (
            <Pagination
              pageInfo={pageInfo}
              currentPage={currentPage}
              onPageChange={(p) => updateParam('page', String(p))}
            />
          )}
        </>
      )}
    </div>
  );
}

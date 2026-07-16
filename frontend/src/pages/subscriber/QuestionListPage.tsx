/** Screen 13: Question list (mine) */
import { useState, useEffect, useCallback } from 'react';
import { useNavigate, useSearchParams, Link } from 'react-router-dom';
import { fetchQuestions, type QuestionListItem } from '@/api/questions';
import { formatDate } from '@/utils/format';
import type { PageInfo } from '@/types';
import Pagination from '@/components/ui/Pagination';
import Button from '@/components/ui/Button';
import styles from './QuestionListPage.module.css';

/* ── Constants ── */

type QuestionCategory = 'DOWNLOAD' | 'PAYMENT' | 'COPYRIGHT' | 'PRODUCTION' | 'OTHER';
type QuestionStatus = 'OPEN' | 'IN_PROGRESS' | 'RESOLVED' | 'CLOSED';

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

function statusClass(status: string): string {
  const map: Record<string, string> = {
    OPEN: styles.statusOPEN,
    IN_PROGRESS: styles.statusIN_PROGRESS,
    RESOLVED: styles.statusRESOLVED,
    CLOSED: styles.statusCLOSED,
  };
  return `${styles.statusBadge} ${map[status] ?? ''}`;
}

export default function QuestionListPage() {
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();

  const currentPage = Number(searchParams.get('page')) || 1;
  const categoryFilter = searchParams.get('category') ?? '';
  const statusFilter = searchParams.get('status') ?? '';
  const tab = searchParams.get('tab') === 'mine' ? 'mine' : 'all';

  const [items, setItems] = useState<QuestionListItem[]>([]);
  const [pageInfo, setPageInfo] = useState<PageInfo | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const params: Record<string, unknown> = { page: currentPage, size: 20 };
      if (tab === 'mine') params.mine = true;
      if (categoryFilter) params.category = categoryFilter;
      if (statusFilter) params.status = statusFilter;
      const result = await fetchQuestions(params as Parameters<typeof fetchQuestions>[0]);
      setItems(result.dataList);
      setPageInfo(result.pageInfo);
    } catch {
      setError('문의 목록을 불러오지 못했습니다.');
    } finally {
      setLoading(false);
    }
  }, [currentPage, categoryFilter, statusFilter, tab]);

  useEffect(() => {
    load();
  }, [load]);

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

  return (
    <div className={styles.page}>
      {/* Header */}
      <div className={styles.pageHeader}>
        <h1 className={styles.pageTitle}>
          {'문의 게시판'}
          {pageInfo && <span className={styles.count}>{pageInfo.total}건</span>}
        </h1>
      </div>

      {/* Tabs */}
      <div className={styles.tabs}>
        <button
          className={`${styles.tab} ${tab === 'all' ? styles.tabActive : ''}`}
          onClick={() => updateParam('tab', '')}
        >
          {'전체 문의'}
        </button>
        <button
          className={`${styles.tab} ${tab === 'mine' ? styles.tabActive : ''}`}
          onClick={() => updateParam('tab', 'mine')}
        >
          {'내 문의'}
        </button>
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
                </tr>
              </thead>
              <tbody>
                {items.map((item) => (
                  <tr
                    key={item.id}
                    className={styles.row}
                    onClick={() => navigate(`/questions/${item.id}`)}
                  >
                    <td className={styles.cellTitle}>
                      {item.title}
                      {!item.isPublic && <span className={styles.privateBadge}>{'비공개'}</span>}
                    </td>
                    <td className={styles.cellCenter}>
                      {CATEGORY_LABELS[item.category as QuestionCategory] ?? item.category}
                    </td>
                    <td className={styles.cellCenter}>
                      <span className={statusClass(item.status)}>
                        {STATUS_LABELS[item.status as QuestionStatus] ?? item.status}
                      </span>
                    </td>
                    <td className={styles.cellCenter}>{formatDate(item.createdAt)}</td>
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

      {/* Floating action button */}
      <Link to="/questions/new" className={styles.fabButton}>
        <Button variant="primary">{'새 문의'}</Button>
      </Link>
    </div>
  );
}

/** Screen 20: Notice list */
import { useEffect, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { fetchNotices } from '@/api/notices';
import type { Notice, PageInfo } from '@/types';
import { formatDate } from '@/utils/format';
import Pagination from '@/components/ui/Pagination';
import styles from './NoticeListPage.module.css';

export default function NoticeListPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const [notices, setNotices] = useState<Notice[]>([]);
  const [pageInfo, setPageInfo] = useState<PageInfo | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const page = Number(searchParams.get('page') ?? '1');
  const sortValue = (searchParams.get('sort') ?? 'latest') as 'latest' | 'views';

  function setPage(newPage: number) {
    const next = new URLSearchParams(searchParams);
    next.set('page', String(newPage));
    setSearchParams(next);
  }

  function handleSortChange(e: React.ChangeEvent<HTMLSelectElement>) {
    const next = new URLSearchParams(searchParams);
    next.set('sort', e.target.value);
    next.set('page', '1');
    setSearchParams(next);
  }

  useEffect(() => {
    setLoading(true);
    setError(null);
    fetchNotices({ page, size: 20, sort: sortValue })
      .then((result) => {
        setNotices(result.dataList);
        setPageInfo(result.pageInfo);
      })
      .catch(() => setError('Failed to load notices'))
      .finally(() => setLoading(false));
  }, [page, sortValue]);

  if (loading) {
    return (
      <div className={styles.page}>
        <div className={styles.loading}>Loading...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className={styles.page}>
        <div className={styles.error}>{error}</div>
      </div>
    );
  }

  return (
    <div className={styles.page}>
      <div className={styles.pageHeader}>
        <h1 className={styles.title}>공지사항</h1>
        <div className={styles.sortBar}>
          <select
            className={styles.sortSelect}
            value={sortValue}
            onChange={handleSortChange}
          >
            <option value="latest">{'최신순'}</option>
            <option value="views">{'조회순'}</option>
          </select>
        </div>
      </div>

      <div className={styles.tableWrap}>
      <table className={styles.table}>
        <thead>
          <tr>
            <th className={styles.thPin} />
            <th>제목</th>
            <th className={styles.thViews}>조회</th>
            <th className={styles.thDate}>등록일</th>
          </tr>
        </thead>
        <tbody>
          {notices.length === 0 && (
            <tr>
              <td colSpan={4} className={styles.empty}>
                등록된 공지사항이 없습니다.
              </td>
            </tr>
          )}
          {notices.map((n) => (
            <tr key={n.id} className={styles.row}>
              <td className={styles.cellPin}>
                {n.isPinned && (
                  <span className={styles.pinBadge}>고정</span>
                )}
              </td>
              <td className={styles.cellTitle}>
                <Link to={`/notices/${n.id}`} className={styles.link}>
                  {n.title}
                </Link>
              </td>
              <td className={styles.cellViews}>
                {n.viewCount.toLocaleString()}
              </td>
              <td className={styles.cellDate}>
                {formatDate(n.createdAt)}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
      </div>

      {pageInfo && pageInfo.total > pageInfo.size && (
        <Pagination pageInfo={pageInfo} currentPage={page} onPageChange={setPage} />
      )}
    </div>
  );
}

/** Screen 20: Notice list */
import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { fetchNotices } from '@/api/notices';
import type { Notice, PageInfo } from '@/types';
import styles from './NoticeListPage.module.css';

function formatDate(iso: string): string {
  const d = new Date(iso);
  return `${d.getFullYear()}.${String(d.getMonth() + 1).padStart(2, '0')}.${String(d.getDate()).padStart(2, '0')}`;
}

export default function NoticeListPage() {
  const [notices, setNotices] = useState<Notice[]>([]);
  const [pageInfo, setPageInfo] = useState<PageInfo | null>(null);
  const [page, setPage] = useState(1);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    setLoading(true);
    setError(null);
    fetchNotices({ page, size: 20 })
      .then((result) => {
        setNotices(result.dataList);
        setPageInfo(result.pageInfo);
      })
      .catch(() => setError('Failed to load notices'))
      .finally(() => setLoading(false));
  }, [page]);

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
      <h1 className={styles.title}>공지사항</h1>

      <table className={styles.table}>
        <thead>
          <tr>
            <th className={styles.thPin} />
            <th>제목</th>
            <th className={styles.thDate}>등록일</th>
            <th className={styles.thViews}>조회</th>
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
              <td className={styles.cellDate}>
                {formatDate(n.createdAt)}
              </td>
              <td className={styles.cellViews}>
                {n.viewCount.toLocaleString()}
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      {/* Pagination */}
      {pageInfo && pageInfo.total > pageInfo.size && (
        <nav className={styles.pagination}>
          <button
            className={styles.pageBtn}
            disabled={!pageInfo.prev}
            onClick={() => setPage(pageInfo.start - 1)}
          >
            &lsaquo;
          </button>
          {Array.from(
            { length: pageInfo.end - pageInfo.start + 1 },
            (_, i) => pageInfo.start + i,
          ).map((p) => (
            <button
              key={p}
              className={`${styles.pageBtn} ${p === page ? styles.pageBtnActive : ''}`}
              onClick={() => setPage(p)}
            >
              {p}
            </button>
          ))}
          <button
            className={styles.pageBtn}
            disabled={!pageInfo.next}
            onClick={() => setPage(pageInfo.end + 1)}
          >
            &rsaquo;
          </button>
        </nav>
      )}
    </div>
  );
}

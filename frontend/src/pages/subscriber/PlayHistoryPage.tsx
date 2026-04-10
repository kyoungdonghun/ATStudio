/** Screen E-1: Play history with bulk delete */
import { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import { fetchPlayHistory, deletePlayHistory } from '@/api/playHistory';
import { toUploadUrl } from '@/api/client';
import { formatDateTime } from '@/utils/format';
import Pagination from '@/components/ui/Pagination';
import type { PlayHistory, PageInfo } from '@/types';
import styles from './PlayHistoryPage.module.css';

const PAGE_SIZE = 20;

const defaultPageInfo: PageInfo = {
  page: 1,
  size: PAGE_SIZE,
  total: 0,
  start: 1,
  end: 1,
  prev: false,
  next: false,
};

export default function PlayHistoryPage() {
  const [items, setItems] = useState<PlayHistory[]>([]);
  const [pageInfo, setPageInfo] = useState<PageInfo>(defaultPageInfo);
  const [page, setPage] = useState(1);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [selected, setSelected] = useState<Set<number>>(new Set());
  const [deleting, setDeleting] = useState(false);

  const load = useCallback(async (p: number) => {
    try {
      setLoading(true);
      setError(null);
      const result = await fetchPlayHistory({ page: p, size: PAGE_SIZE });
      setItems(result.dataList ?? []);
      setPageInfo(result.pageInfo ?? defaultPageInfo);
      setSelected(new Set());
    } catch {
      setError('재생 기록을 불러오지 못했습니다.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    load(page);
  }, [page, load]);

  function handleSelectAll(checked: boolean) {
    if (checked) {
      setSelected(new Set(items.map((i) => i.id)));
    } else {
      setSelected(new Set());
    }
  }

  function handleSelect(id: number) {
    setSelected((prev) => {
      const next = new Set(prev);
      if (next.has(id)) {
        next.delete(id);
      } else {
        next.add(id);
      }
      return next;
    });
  }

  async function handleDelete() {
    if (selected.size === 0) return;
    try {
      setDeleting(true);
      await deletePlayHistory(Array.from(selected));
      await load(page);
    } catch {
      setError('삭제에 실패했습니다.');
    } finally {
      setDeleting(false);
    }
  }

  function handlePageChange(p: number) {
    setPage(p);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  const allSelected = items.length > 0 && selected.size === items.length;

  return (
    <div className={styles.page}>
      <div className={styles.pageHeader}>
        <h1 className={styles.pageTitle}>
          {'재생 기록'}
          {pageInfo.total > 0 && (
            <span className={styles.count}>{pageInfo.total}곡</span>
          )}
        </h1>
        {items.length > 0 && (
          <button
            className={styles.btnDelete}
            onClick={handleDelete}
            disabled={selected.size === 0 || deleting}
          >
            {deleting ? '삭제 중...' : `선택 삭제 (${selected.size})`}
          </button>
        )}
      </div>

      {loading ? (
        <div className={styles.loading}>{'불러오는 중...'}</div>
      ) : error ? (
        <div className={styles.error}>{error}</div>
      ) : items.length === 0 ? (
        <div className={styles.empty}>
          <p>{'재생 기록이 없습니다.'}</p>
          <Link to="/tracks" className={styles.emptyLink}>
            {'음원 둘러보기'}
          </Link>
        </div>
      ) : (
        <>
          <div className={styles.tableWrap}>
            <table className={styles.table}>
              <thead>
                <tr>
                  <th className={styles.thCenter}>
                    <input
                      type="checkbox"
                      checked={allSelected}
                      onChange={(e) => handleSelectAll(e.target.checked)}
                    />
                  </th>
                  <th className={styles.thCenter}>#</th>
                  <th>{'음원'}</th>
                  <th className={styles.thRight}>{'재생일시'}</th>
                </tr>
              </thead>
              <tbody>
                {items.map((item, idx) => (
                  <tr key={item.id} className={styles.row}>
                    <td className={styles.cellCheck}>
                      <input
                        type="checkbox"
                        checked={selected.has(item.id)}
                        onChange={() => handleSelect(item.id)}
                      />
                    </td>
                    <td className={styles.cellNum}>
                      {(page - 1) * PAGE_SIZE + idx + 1}
                    </td>
                    <td className={styles.cellInfo}>
                      <div className={styles.info}>
                        <div className={styles.thumb}>
                          {item.track.thumbnail ? (
                            <img
                              src={toUploadUrl(item.track.thumbnail)!}
                              alt={item.track.title}
                            />
                          ) : (
                            '\u266A'
                          )}
                        </div>
                        <div className={styles.infoText}>
                          <Link
                            to={`/tracks/${item.track.id}`}
                            className={styles.titleLink}
                          >
                            {item.track.title}
                          </Link>
                        </div>
                      </div>
                    </td>
                    <td className={styles.cellDate}>
                      {formatDateTime(item.playedAt)}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <div className={styles.paginationWrap}>
            <Pagination
              pageInfo={pageInfo}
              currentPage={page}
              onPageChange={handlePageChange}
            />
          </div>
        </>
      )}
    </div>
  );
}

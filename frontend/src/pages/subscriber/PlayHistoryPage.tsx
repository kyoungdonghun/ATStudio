/** Screen E-1: Play history with bulk delete (SR-89: localStorage-based) */
import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import {
  loadPlayHistory,
  hydratePlayHistory,
  removePlayHistoryEntry,
  clearPlayHistory,
  type LocalPlayEntry,
} from '@/store/playerStore';
import { toUploadUrl } from '@/api/client';
import { formatDateTime } from '@/utils/format';
import Pagination from '@/components/ui/Pagination';
import type { PageInfo } from '@/types';
import styles from './PlayHistoryPage.module.css';

const PAGE_SIZE = 20;

function buildPageInfo(total: number, page: number): PageInfo {
  const totalPages = Math.max(1, Math.ceil(total / PAGE_SIZE));
  return {
    page,
    size: PAGE_SIZE,
    total,
    start: Math.max(1, page - 2),
    end: Math.min(totalPages, page + 2),
    prev: page > 1,
    next: page < totalPages,
  };
}

export default function PlayHistoryPage() {
  const [allItems, setAllItems] = useState<LocalPlayEntry[]>([]);
  const [page, setPage] = useState(1);
  const [selected, setSelected] = useState<Set<number>>(new Set());

  useEffect(() => {
    setAllItems(loadPlayHistory());
    let active = true;
    void hydratePlayHistory().then((entries) => {
      if (active) setAllItems(entries);
    });
    return () => {
      active = false;
    };
  }, []);

  const pageInfo = buildPageInfo(allItems.length, page);
  const startIdx = (page - 1) * PAGE_SIZE;
  const items = allItems.slice(startIdx, startIdx + PAGE_SIZE);

  function handleSelectAll(checked: boolean) {
    if (checked) {
      setSelected(new Set(items.map((item) => item.track.id)));
    } else {
      setSelected(new Set());
    }
  }

  function handleSelect(trackId: number) {
    setSelected((prev) => {
      const next = new Set(prev);
      if (next.has(trackId)) next.delete(trackId);
      else next.add(trackId);
      return next;
    });
  }

  function handleDelete() {
    if (selected.size === 0) return;
    selected.forEach((trackId) => removePlayHistoryEntry(trackId));
    const updated = loadPlayHistory();
    setAllItems(updated);
    setSelected(new Set());
    // Go back a page if current page is now empty
    const newTotal = updated.length;
    const maxPage = Math.max(1, Math.ceil(newTotal / PAGE_SIZE));
    if (page > maxPage) setPage(maxPage);
  }

  function handleClearAll() {
    clearPlayHistory();
    setAllItems([]);
    setSelected(new Set());
    setPage(1);
  }

  function handlePageChange(p: number) {
    setPage(p);
    setSelected(new Set());
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  const allSelected = items.length > 0 && items.every((item) => selected.has(item.track.id));

  return (
    <div className={styles.page}>
      <div className={styles.pageHeader}>
        <h1 className={styles.pageTitle}>
          {'재생 기록'}
          {allItems.length > 0 && <span className={styles.count}>{allItems.length}곡</span>}
        </h1>
        {allItems.length > 0 && (
          <div style={{ display: 'flex', gap: '8px' }}>
            <button
              className={styles.btnDelete}
              onClick={handleDelete}
              disabled={selected.size === 0}
            >
              {`선택 삭제 (${selected.size})`}
            </button>
            <button className={styles.btnDelete} onClick={handleClearAll}>
              전체 삭제
            </button>
          </div>
        )}
      </div>

      {allItems.length === 0 ? (
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
                  <tr key={`${item.track.id}-${item.playedAt}`} className={styles.row}>
                    <td className={styles.cellCheck}>
                      <input
                        type="checkbox"
                        checked={selected.has(item.track.id)}
                        onChange={() => handleSelect(item.track.id)}
                      />
                    </td>
                    <td className={styles.cellNum}>{startIdx + idx + 1}</td>
                    <td className={styles.cellInfo}>
                      <div className={styles.info}>
                        <div className={styles.thumb}>
                          {item.track.thumbnail ? (
                            <img src={toUploadUrl(item.track.thumbnail)!} alt={item.track.title} />
                          ) : (
                            '\u266A'
                          )}
                        </div>
                        <div className={styles.infoText}>
                          <Link to={`/tracks/${item.track.id}`} className={styles.titleLink}>
                            {item.track.title}
                          </Link>
                        </div>
                      </div>
                    </td>
                    <td className={styles.cellDate}>{formatDateTime(item.playedAt)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <div className={styles.paginationWrap}>
            <Pagination pageInfo={pageInfo} currentPage={page} onPageChange={handlePageChange} />
          </div>
        </>
      )}
    </div>
  );
}

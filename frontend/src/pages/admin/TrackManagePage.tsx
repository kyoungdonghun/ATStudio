import { useState, useEffect, useCallback } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import {
  fetchAdminTracks,
  deleteTrack,
  type AdminTrackListItem,
} from '@/api/tracks';
import type { PageInfo } from '@/types';
import { toUploadUrl } from '@/api/client';
import { formatDate } from '@/utils/format';
import Button from '@/components/ui/Button';
import Modal from '@/components/ui/Modal';
import Pagination from '@/components/ui/Pagination';
import styles from './TrackManagePage.module.css';

const PAGE_SIZE = 20;

/** Screen K-7: Track management (admin) */
export default function TrackManagePage() {
  const [searchParams, setSearchParams] = useSearchParams();

  /* ── State ── */
  const [tracks, setTracks] = useState<AdminTrackListItem[]>([]);
  const [pageInfo, setPageInfo] = useState<PageInfo | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  /* ── Delete modal ── */
  const [deleteTarget, setDeleteTarget] = useState<AdminTrackListItem | null>(
    null,
  );
  const [deleting, setDeleting] = useState(false);

  /* ── Filters from URL ── */
  const currentPage = Number(searchParams.get('page') ?? '1');
  const activeFilter = searchParams.get('filter') ?? 'all';

  /* ── Load ── */
  const loadTracks = useCallback(async () => {
    setLoading(true);
    setError(null);

    const params: { page: number; size: number; is_active?: boolean } = {
      page: currentPage,
      size: PAGE_SIZE,
    };

    if (activeFilter === 'active') params.is_active = true;
    if (activeFilter === 'inactive') params.is_active = false;

    try {
      const res = await fetchAdminTracks(params);
      setTracks(res.dataList);
      setPageInfo(res.pageInfo);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load tracks');
    } finally {
      setLoading(false);
    }
  }, [currentPage, activeFilter]);

  useEffect(() => {
    loadTracks();
  }, [loadTracks]);

  /* ── Filter change ── */
  function handleFilterChange(value: string) {
    const next = new URLSearchParams(searchParams);
    if (value === 'all') {
      next.delete('filter');
    } else {
      next.set('filter', value);
    }
    next.set('page', '1');
    setSearchParams(next);
  }

  /* ── Pagination ── */
  function goToPage(page: number) {
    const next = new URLSearchParams(searchParams);
    next.set('page', String(page));
    setSearchParams(next);
  }

  /* ── Delete ── */
  async function handleDelete() {
    if (!deleteTarget) return;
    setDeleting(true);

    try {
      await deleteTrack(deleteTarget.id);
      setDeleteTarget(null);
      loadTracks();
    } catch (err) {
      setError(err instanceof Error ? err.message : '삭제에 실패했습니다.');
      setDeleteTarget(null);
    } finally {
      setDeleting(false);
    }
  }

  return (
    <div className={styles.page}>
      {/* Header */}
      <div className={styles.pageHeader}>
        <div>
          <span className={styles.pageTitle}>{'음원 관리'}</span>
          {pageInfo && (
            <span className={styles.pageTitleCount}>
              {`(${pageInfo.total.toLocaleString()})`}
            </span>
          )}
        </div>
        <div className={styles.headerActions}>
          <Link to="/admin/tracks/upload">
            <Button size="sm">{'+ 새 음원'}</Button>
          </Link>
        </div>
      </div>

      {/* Filter */}
      <div className={styles.filterBar}>
        <span className={styles.filterLabel}>{'상태'}</span>
        <select
          className={styles.filterSelect}
          value={activeFilter}
          onChange={(e) => handleFilterChange(e.target.value)}
        >
          <option value="all">{'전체'}</option>
          <option value="active">{'활성'}</option>
          <option value="inactive">{'비활성'}</option>
        </select>
      </div>

      {/* Table */}
      {loading ? (
        <div className={styles.loading}>{'Loading...'}</div>
      ) : error ? (
        <div className={styles.error}>{error}</div>
      ) : tracks.length === 0 ? (
        <div className={styles.empty}>{'등록된 음원이 없습니다.'}</div>
      ) : (
        <>
          <div className={styles.tableWrap}>
          <table className={styles.trackTable}>
            <thead>
              <tr>
                <th className={styles.thCenter}>ID</th>
                <th>{'음원'}</th>
                <th className={styles.thRight}>BPM</th>
                <th className={styles.thCenter}>{'조성'}</th>
                <th className={styles.thRight}>{'재생수'}</th>
                <th>{'상태'}</th>
                <th>{'등록일'}</th>
                <th />
              </tr>
            </thead>
            <tbody>
              {tracks.map((track) => (
                <tr key={track.id}>
                  <td className={styles.cellId}>{track.id}</td>
                  <td className={styles.cellInfo}>
                    <div className={styles.info}>
                      <div className={styles.thumb}>
                        {track.thumbnail ? (
                          <img src={toUploadUrl(track.thumbnail)!} alt={track.title} />
                        ) : (
                          '\u266A'
                        )}
                      </div>
                      <span className={styles.trackTitle}>{track.title}</span>
                    </div>
                  </td>
                  <td className={styles.cellBpm}>{track.bpm}</td>
                  <td className={styles.cellKey}>{track.tonality}</td>
                  <td className={styles.cellPlays}>
                    {track.playCount.toLocaleString()}
                  </td>
                  <td>
                    <span
                      className={`${styles.statusBadge} ${
                        track.isActive ? styles.statusActive : styles.statusInactive
                      }`}
                    >
                      {track.isActive ? '활성' : '비활성'}
                    </span>
                  </td>
                  <td className={styles.cellDate}>
                    {formatDate(track.createdAt)}
                  </td>
                  <td className={styles.cellActions}>
                    <div className={styles.actionBtns}>
                      <Link to={`/admin/tracks/${track.id}/edit`}>
                        <button className={styles.actBtn}>{'수정'}</button>
                      </Link>
                      <button
                        className={styles.actBtnDanger}
                        onClick={() => setDeleteTarget(track)}
                      >
                        {'삭제'}
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          </div>

          {pageInfo && (
            <Pagination pageInfo={pageInfo} currentPage={currentPage} onPageChange={goToPage} />
          )}
        </>
      )}

      {/* Delete Confirm Modal */}
      <Modal
        open={deleteTarget !== null}
        onClose={() => setDeleteTarget(null)}
        title="음원 삭제"
      >
        <div className={styles.modalBody}>
          <strong>{deleteTarget?.title}</strong>
          {' 음원을 삭제하시겠습니까?'}
          <br />
          {'이 작업은 비활성화(소프트 삭제)로 처리됩니다.'}
        </div>
        <div className={styles.modalActions}>
          <Button
            variant="ghost"
            size="sm"
            onClick={() => setDeleteTarget(null)}
            disabled={deleting}
          >
            {'취소'}
          </Button>
          <Button
            variant="danger"
            size="sm"
            onClick={handleDelete}
            loading={deleting}
          >
            {'삭제'}
          </Button>
        </div>
      </Modal>
    </div>
  );
}

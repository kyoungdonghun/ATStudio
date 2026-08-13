import { useState, useEffect, useCallback, useRef } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { fetchAdminTracks, deleteTrack, type AdminTrackListItem } from '@/api/tracks';
import type { PageInfo } from '@/types';
import { toUploadUrl } from '@/api/client';
import { formatDate } from '@/utils/format';
import { parsePositiveDecimalRouteID } from '@/utils/routeId';
import { SEARCH_KEYWORD_MAX } from '@/utils/validation';
import Button from '@/components/ui/Button';
import Modal from '@/components/ui/Modal';
import Pagination from '@/components/ui/Pagination';
import styles from './TrackManagePage.module.css';

const PAGE_SIZE = 20;
const MAX_BACKEND_PAGE = 2_147_483_647;
const TRACK_LOAD_ERROR = '음원 목록을 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.';
const TRACK_DELETE_ERROR = '음원을 삭제하지 못했습니다. 같은 음원에서 다시 시도해 주세요.';
const TRACK_REFRESH_ERROR =
  '삭제는 완료됐지만 최신 목록을 불러오지 못했습니다. 목록 새로고침을 다시 시도해 주세요.';

type ActiveFilter = 'all' | 'active' | 'inactive';

function parsePage(rawPage: string | null): number {
  const page = parsePositiveDecimalRouteID(rawPage ?? undefined);
  return page !== null && page <= MAX_BACKEND_PAGE ? page : 1;
}

function parseFilter(rawFilter: string | null): ActiveFilter {
  return rawFilter === 'active' || rawFilter === 'inactive' ? rawFilter : 'all';
}

function normalizeKeyword(rawKeyword: string | null): string {
  return (rawKeyword ?? '').trim().slice(0, SEARCH_KEYWORD_MAX);
}

/** Screen K-7: Track management (admin) */
export default function TrackManagePage() {
  const [searchParams, setSearchParams] = useSearchParams();

  const rawPage = searchParams.get('page');
  const rawFilter = searchParams.get('filter');
  const rawKeyword = searchParams.get('keyword');
  const currentPage = parsePage(rawPage);
  const activeFilter = parseFilter(rawFilter);
  const activeKeyword = normalizeKeyword(rawKeyword);
  const urlNeedsNormalization =
    (rawPage !== null && rawPage !== String(currentPage)) ||
    (rawFilter !== null && rawFilter !== activeFilter) ||
    rawFilter === 'all' ||
    (rawKeyword !== null && rawKeyword !== activeKeyword) ||
    (rawKeyword !== null && activeKeyword.length === 0);

  const [tracks, setTracks] = useState<AdminTrackListItem[]>([]);
  const [pageInfo, setPageInfo] = useState<PageInfo | null>(null);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [searchInput, setSearchInput] = useState(activeKeyword);
  const activeListControllerRef = useRef<AbortController | null>(null);
  const listGenerationRef = useRef(0);

  const [deleteTarget, setDeleteTarget] = useState<AdminTrackListItem | null>(null);
  const [deleting, setDeleting] = useState(false);
  const [deleteError, setDeleteError] = useState<string | null>(null);
  const [deleteCommitted, setDeleteCommitted] = useState(false);
  const deletePendingRef = useRef(false);
  const deleteCommittedRef = useRef(false);
  const deleteGenerationRef = useRef(0);

  useEffect(() => {
    if (!urlNeedsNormalization) return;

    const next = new URLSearchParams(searchParams);
    if (rawPage !== null) next.set('page', String(currentPage));
    if (activeFilter === 'all') next.delete('filter');
    else next.set('filter', activeFilter);
    if (activeKeyword) next.set('keyword', activeKeyword);
    else next.delete('keyword');
    setSearchParams(next, { replace: true });
  }, [
    activeFilter,
    activeKeyword,
    currentPage,
    rawPage,
    searchParams,
    setSearchParams,
    urlNeedsNormalization,
  ]);

  useEffect(() => {
    setSearchInput(activeKeyword);
  }, [activeKeyword]);

  const loadTracks = useCallback(async (): Promise<boolean> => {
    activeListControllerRef.current?.abort();
    const controller = new AbortController();
    const generation = ++listGenerationRef.current;
    activeListControllerRef.current = controller;
    setLoading(true);
    setLoadError(null);

    const params: { page: number; size: number; is_active?: boolean; keyword?: string } = {
      page: currentPage,
      size: PAGE_SIZE,
    };
    if (activeFilter === 'active') params.is_active = true;
    if (activeFilter === 'inactive') params.is_active = false;
    if (activeKeyword) params.keyword = activeKeyword;

    try {
      const response = await fetchAdminTracks(params, controller.signal);
      if (controller.signal.aborted || listGenerationRef.current !== generation) return false;
      const totalPages = Math.max(1, Math.ceil(response.pageInfo.total / PAGE_SIZE));
      if (currentPage > totalPages && response.dataList.length === 0) {
        const next = new URLSearchParams(searchParams);
        next.set('page', String(totalPages));
        setSearchParams(next, { replace: true });
        return false;
      }

      setTracks(response.dataList);
      setPageInfo(response.pageInfo);
      return true;
    } catch {
      if (controller.signal.aborted || listGenerationRef.current !== generation) return false;
      setTracks([]);
      setPageInfo(null);
      setLoadError(TRACK_LOAD_ERROR);
      return false;
    } finally {
      if (listGenerationRef.current === generation) setLoading(false);
      if (activeListControllerRef.current === controller) activeListControllerRef.current = null;
    }
  }, [activeFilter, activeKeyword, currentPage, searchParams, setSearchParams]);

  useEffect(() => {
    if (urlNeedsNormalization) return;
    void loadTracks();
    return () => {
      activeListControllerRef.current?.abort();
      listGenerationRef.current += 1;
    };
  }, [loadTracks, urlNeedsNormalization]);

  function handleFilterChange(value: ActiveFilter) {
    const next = new URLSearchParams(searchParams);
    if (value === 'all') next.delete('filter');
    else next.set('filter', value);
    next.set('page', '1');
    setSearchParams(next);
  }

  function applySearch() {
    const next = new URLSearchParams(searchParams);
    const keyword = searchInput.trim().slice(0, SEARCH_KEYWORD_MAX);
    if (keyword) next.set('keyword', keyword);
    else next.delete('keyword');
    next.set('page', '1');
    setSearchParams(next);
  }

  function goToPage(page: number) {
    const next = new URLSearchParams(searchParams);
    next.set('page', String(page));
    setSearchParams(next);
  }

  function openDelete(track: AdminTrackListItem) {
    if (deletePendingRef.current) return;
    deleteGenerationRef.current += 1;
    setDeleteTarget(track);
    setDeleteError(null);
    setDeleteCommitted(false);
    deleteCommittedRef.current = false;
  }

  function closeDeleteModal() {
    if (deletePendingRef.current) return;
    deleteGenerationRef.current += 1;
    setDeleteTarget(null);
    setDeleteError(null);
    setDeleteCommitted(false);
    deleteCommittedRef.current = false;
  }

  async function handleDelete() {
    if (!deleteTarget || deletePendingRef.current || deleteCommittedRef.current) return;
    const targetID = deleteTarget.id;
    const generation = deleteGenerationRef.current;
    const ownsDelete = () => deleteGenerationRef.current === generation;
    deletePendingRef.current = true;
    setDeleting(true);
    setDeleteError(null);

    try {
      await deleteTrack(targetID);
      deleteCommittedRef.current = true;
      if (ownsDelete()) setDeleteCommitted(true);
      const refreshed = await loadTracks();
      if (ownsDelete() && refreshed) {
        deleteCommittedRef.current = false;
        setDeleteCommitted(false);
        setDeleteTarget(null);
        setDeleteError(null);
      } else if (ownsDelete() && deleteCommittedRef.current) {
        setDeleteError(TRACK_REFRESH_ERROR);
      }
    } catch {
      if (ownsDelete()) setDeleteError(TRACK_DELETE_ERROR);
    } finally {
      deletePendingRef.current = false;
      if (ownsDelete()) setDeleting(false);
    }
  }

  async function retryCommittedRefresh() {
    if (deletePendingRef.current || !deleteCommittedRef.current) return;
    const generation = deleteGenerationRef.current;
    const ownsDelete = () => deleteGenerationRef.current === generation;
    deletePendingRef.current = true;
    setDeleting(true);
    setDeleteError(null);
    try {
      const refreshed = await loadTracks();
      if (ownsDelete() && refreshed) {
        deleteCommittedRef.current = false;
        setDeleteCommitted(false);
        setDeleteTarget(null);
        setDeleteError(null);
      } else if (ownsDelete() && deleteCommittedRef.current) {
        setDeleteError(TRACK_REFRESH_ERROR);
      }
    } finally {
      deletePendingRef.current = false;
      if (ownsDelete()) setDeleting(false);
    }
  }

  return (
    <div className={styles.page}>
      <div className={styles.pageHeader}>
        <div>
          <span className={styles.pageTitle}>음원 관리</span>
          {pageInfo && (
            <span className={styles.pageTitleCount}>{`(${pageInfo.total.toLocaleString()})`}</span>
          )}
        </div>
        <div className={styles.headerActions}>
          <Link to="/admin/tracks/upload">
            <Button size="sm">+ 새 음원</Button>
          </Link>
        </div>
      </div>

      <div className={styles.filterBar}>
        <label className={styles.filterLabel} htmlFor="track-status-filter">
          상태
        </label>
        <select
          id="track-status-filter"
          className={styles.filterSelect}
          value={activeFilter}
          onChange={(event) => handleFilterChange(event.target.value as ActiveFilter)}
        >
          <option value="all">전체</option>
          <option value="active">활성</option>
          <option value="inactive">비활성</option>
        </select>
        <input
          className={styles.searchInput}
          type="search"
          maxLength={SEARCH_KEYWORD_MAX}
          aria-label="곡 제목 검색"
          placeholder="곡 제목 검색"
          value={searchInput}
          onChange={(event) => setSearchInput(event.target.value)}
          onKeyDown={(event) => {
            if (event.key === 'Enter') applySearch();
          }}
        />
        <Button size="sm" variant="outline" onClick={applySearch}>
          검색
        </Button>
      </div>

      {loading ? (
        <div className={styles.loading}>Loading...</div>
      ) : loadError ? (
        <div className={styles.error} role="alert">
          <div>{loadError}</div>
          <Button size="sm" variant="outline" onClick={() => void loadTracks()}>
            목록 다시 시도
          </Button>
        </div>
      ) : tracks.length === 0 ? (
        <div className={styles.empty}>등록된 음원이 없습니다.</div>
      ) : (
        <>
          <div className={styles.tableWrap}>
            <table className={styles.trackTable}>
              <thead>
                <tr>
                  <th className={styles.thCenter}>ID</th>
                  <th>음원</th>
                  <th className={styles.thRight}>BPM</th>
                  <th className={styles.thCenter}>조성</th>
                  <th className={styles.thRight}>재생 수</th>
                  <th>상태</th>
                  <th>등록일</th>
                  <th>작업</th>
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
                    <td className={styles.cellPlays}>{track.playCount.toLocaleString()}</td>
                    <td>
                      <span
                        className={`${styles.statusBadge} ${
                          track.isActive ? styles.statusActive : styles.statusInactive
                        }`}
                      >
                        {track.isActive ? '활성' : '비활성'}
                      </span>
                    </td>
                    <td className={styles.cellDate}>{formatDate(track.createdAt)}</td>
                    <td className={styles.cellActions}>
                      <div className={styles.actionBtns}>
                        <Link to={`/admin/tracks/${track.id}/edit`}>
                          <button type="button" className={styles.actBtn}>
                            수정
                          </button>
                        </Link>
                        <button
                          type="button"
                          className={styles.actBtnDanger}
                          onClick={() => openDelete(track)}
                          disabled={deleting}
                        >
                          삭제
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

      <Modal
        open={deleteTarget !== null}
        onClose={closeDeleteModal}
        title="음원 삭제"
        busy={deleting}
      >
        <div className={styles.modalBody}>
          <strong>{deleteTarget?.title}</strong> 음원을 삭제하시겠습니까?
          <br />이 작업은 기존 정책에 따라 비활성화로 처리됩니다.
        </div>
        {deleteError && (
          <div className={styles.modalError} role="alert">
            {deleteError}
          </div>
        )}
        <div className={styles.modalActions}>
          <Button variant="ghost" size="sm" onClick={closeDeleteModal} disabled={deleting}>
            닫기
          </Button>
          {deleteCommitted ? (
            <Button size="sm" loading={deleting} onClick={() => void retryCommittedRefresh()}>
              목록 새로고침
            </Button>
          ) : (
            <Button
              variant="danger"
              size="sm"
              loading={deleting}
              onClick={() => void handleDelete()}
            >
              {deleteError ? '삭제 다시 시도' : '삭제'}
            </Button>
          )}
        </div>
      </Modal>
    </div>
  );
}

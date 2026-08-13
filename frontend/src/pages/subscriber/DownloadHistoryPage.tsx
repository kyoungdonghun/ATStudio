/** SR-79: Download history */
import { useState, useEffect, useCallback, useMemo, useRef } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import {
  downloadTrack,
  triggerBlobDownload,
  fetchDownloadCount,
  fetchDownloadHistory,
  fetchDownloadHistoryTrackIds,
  type DownloadCount,
  type DownloadHistoryItem,
} from '@/api/downloads';
import { toUploadUrl } from '@/api/client';
import { classifyLoadError } from '@/api/loadError';
import { formatDateTime } from '@/utils/format';
import { usePlayerStore } from '@/store/playerStore';
import { useAuthStore } from '@/store/authStore';
import { useToastStore } from '@/store/toastStore';
import ConfirmDialog from '@/components/ui/ConfirmDialog';
import Pagination from '@/components/ui/Pagination';
import type { PageInfo } from '@/types';
import { toPlayableTrack } from '@/utils/playableTrack';
import styles from './DownloadHistoryPage.module.css';

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

type SortMode = 'latest' | 'oldest';

export default function DownloadHistoryPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const currentPage = Number(searchParams.get('page') ?? '1') || 1;
  const urlKeyword = searchParams.get('keyword') ?? '';
  const urlSort = (searchParams.get('sort') as SortMode) || 'latest';

  const [items, setItems] = useState<DownloadHistoryItem[]>([]);
  const [pageInfo, setPageInfo] = useState<PageInfo>(defaultPageInfo);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [dlCount, setDlCount] = useState<DownloadCount | null>(null);
  const [downloading, setDownloading] = useState<number | null>(null);
  const [bulkBusy, setBulkBusy] = useState(false);
  const [selectedIds, setSelectedIds] = useState<Set<number>>(new Set());
  const [keywordInput, setKeywordInput] = useState(urlKeyword);
  const [confirmDownloadAllIds, setConfirmDownloadAllIds] = useState<number[] | null>(null);
  const loadGenerationRef = useRef(0);

  const currentTrack = usePlayerStore((s) => s.currentTrack);
  const isPlayerPlaying = usePlayerStore((s) => s.isPlaying);
  const playTrack = usePlayerStore((s) => s.play);
  const pauseTrack = usePlayerStore((s) => s.pause);
  const resumeTrack = usePlayerStore((s) => s.resume);
  const setTrackListContext = usePlayerStore((s) => s.setTrackListContext);
  const role = useAuthStore((s) => s.role);
  const isAdmin = role === 'ADMIN';
  const toast = useToastStore((s) => s.show);

  // Keep input synced if URL changes externally (back/forward nav)
  useEffect(() => {
    setKeywordInput(urlKeyword);
  }, [urlKeyword]);

  const load = useCallback(
    async (signal?: AbortSignal) => {
      const generation = ++loadGenerationRef.current;
      try {
        setLoading(true);
        setError(null);
        const [historyResult, countResult] = await Promise.all([
          fetchDownloadHistory(
            {
              page: currentPage,
              size: PAGE_SIZE,
              keyword: urlKeyword || undefined,
              sort: urlSort,
            },
            signal,
          ),
          isAdmin
            ? Promise.resolve(null)
            : fetchDownloadCount(signal).catch((loadError: unknown) => {
                if (classifyLoadError(loadError) === 'cancelled') throw loadError;
                return null;
              }),
        ]);
        if (loadGenerationRef.current === generation) {
          setItems(historyResult.dataList ?? []);
          setPageInfo(historyResult.pageInfo ?? defaultPageInfo);
          setDlCount(countResult);
          setSelectedIds(new Set());
        }
      } catch (loadError: unknown) {
        if (
          loadGenerationRef.current === generation &&
          classifyLoadError(loadError) !== 'cancelled'
        ) {
          setError('다운로드 기록을 불러오지 못했습니다.');
        }
      } finally {
        if (loadGenerationRef.current === generation) setLoading(false);
      }
    },
    [currentPage, urlKeyword, urlSort, isAdmin],
  );

  useEffect(() => {
    const controller = new AbortController();
    void load(controller.signal);
    return () => {
      controller.abort();
      loadGenerationRef.current += 1;
    };
  }, [load]);

  /* SR-83: Publish downloaded tracks as player context so Next/Prev traverses them.
     Note: download history can contain duplicates (same track downloaded multiple times),
     so we de-duplicate by trackId, keeping the first occurrence. */
  useEffect(() => {
    const seen = new Set<number>();
    const tracks = items
      .filter((item) => {
        if (seen.has(item.trackId)) return false;
        seen.add(item.trackId);
        return true;
      })
      .map((item) => toPlayableTrack(item));
    return setTrackListContext(tracks);
  }, [items, setTrackListContext]);

  function updateParams(patch: Record<string, string | null>) {
    const next = new URLSearchParams(searchParams);
    for (const [k, v] of Object.entries(patch)) {
      if (v === null || v === '') next.delete(k);
      else next.set(k, v);
    }
    setSearchParams(next);
  }

  function handleSearchSubmit(e: React.FormEvent) {
    e.preventDefault();
    updateParams({ keyword: keywordInput.trim() || null, page: '1' });
  }

  function handleSortChange(e: React.ChangeEvent<HTMLSelectElement>) {
    updateParams({ sort: e.target.value, page: '1' });
  }

  function handlePageChange(p: number) {
    updateParams({ page: String(p) });
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  async function handleDownloadOne(item: DownloadHistoryItem) {
    try {
      setDownloading(item.trackId);
      const blob = await downloadTrack(item.trackId);
      triggerBlobDownload(blob, `${item.title}.mp3`);
      toast('success', `${item.title} 다운로드 완료`);
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : '다운로드에 실패했습니다.';
      setError(msg);
    } finally {
      setDownloading(null);
    }
  }

  async function downloadByTrackIds(trackIds: number[]) {
    if (trackIds.length === 0) return;
    try {
      setBulkBusy(true);
      let ok = 0;
      let fail = 0;
      // Look up titles on-page for a nicer filename; fall back to id.
      const titleById = new Map(items.map((i) => [i.trackId, i.title] as const));
      for (const trackId of trackIds) {
        try {
          const blob = await downloadTrack(trackId);
          const title = titleById.get(trackId) ?? `track-${trackId}`;
          triggerBlobDownload(blob, `${title}.mp3`);
          ok += 1;
        } catch {
          fail += 1;
        }
      }
      if (fail === 0) {
        toast('success', `${ok}곡 다운로드 완료`);
      } else {
        toast('error', `${ok}곡 성공, ${fail}곡 실패`);
      }
      if (!isAdmin) {
        fetchDownloadCount()
          .then(setDlCount)
          .catch(() => {});
      }
    } finally {
      setBulkBusy(false);
    }
  }

  async function handleDownloadSelected() {
    const trackIds = [
      ...new Set(
        items.filter((item) => selectedIds.has(item.downloadId)).map((item) => item.trackId),
      ),
    ];
    if (trackIds.length === 0) return;
    await downloadByTrackIds(trackIds);
  }

  async function handleDownloadAll() {
    try {
      const ids = await fetchDownloadHistoryTrackIds(urlKeyword || undefined);
      if (ids.length === 0) {
        toast('error', '다운로드할 항목이 없습니다.');
        return;
      }
      setConfirmDownloadAllIds(ids);
    } catch {
      toast('error', '전체 재다운로드에 실패했습니다.');
    }
  }

  async function confirmDownloadAll() {
    if (!confirmDownloadAllIds || confirmDownloadAllIds.length === 0) return;
    const ids = confirmDownloadAllIds;
    setConfirmDownloadAllIds(null);
    await downloadByTrackIds(ids);
  }

  function toggleSelectAll(checked: boolean) {
    if (checked) {
      setSelectedIds(new Set(items.map((i) => i.downloadId)));
    } else {
      setSelectedIds(new Set());
    }
  }

  function toggleSelectOne(downloadId: number) {
    setSelectedIds((prev) => {
      const next = new Set(prev);
      if (next.has(downloadId)) next.delete(downloadId);
      else next.add(downloadId);
      return next;
    });
  }

  function handlePlay(item: DownloadHistoryItem) {
    if (currentTrack?.id === item.trackId) {
      if (isPlayerPlaying) pauseTrack();
      else resumeTrack();
      return;
    }
    playTrack(toPlayableTrack(item));
  }

  const allSelected = useMemo(
    () => items.length > 0 && items.every((i) => selectedIds.has(i.downloadId)),
    [items, selectedIds],
  );
  // Derive selected unique track count for button label.
  const selectedUniqueTrackCount = useMemo(() => {
    const set = new Set<number>();
    for (const i of items) {
      if (selectedIds.has(i.downloadId)) set.add(i.trackId);
    }
    return set.size;
  }, [items, selectedIds]);

  return (
    <div className={styles.page}>
      <div className={styles.pageHeader}>
        <h1 className={styles.pageTitle}>
          {'다운로드 기록'}
          {pageInfo.total > 0 && <span className={styles.count}>{pageInfo.total}건</span>}
        </h1>
      </div>

      {/* Daily download limit info */}
      <div className={styles.limitInfo}>
        {isAdmin ? (
          <span className={styles.limitBadge}>{'무제한 다운로드'}</span>
        ) : dlCount ? (
          <span className={styles.limitBadge}>
            {'오늘 '}
            {dlCount.todayDownloads}
            {' / '}
            {dlCount.dailyLimit === -1 ? '무제한' : `${dlCount.dailyLimit}곡`}
            {dlCount.dailyLimit !== -1 && ` (남은 횟수: ${dlCount.remaining})`}
          </span>
        ) : null}
      </div>

      {/* Filter bar */}
      <div className={styles.filterBar}>
        <form className={styles.searchForm} onSubmit={handleSearchSubmit}>
          <input
            type="text"
            className={styles.searchInput}
            aria-label="다운로드 기록 검색"
            placeholder="제목 또는 태그 검색"
            value={keywordInput}
            onChange={(e) => setKeywordInput(e.target.value)}
          />
          <button type="submit" className={styles.searchBtn}>
            {'검색'}
          </button>
        </form>
        <select
          className={styles.sortSelect}
          aria-label="다운로드 기록 정렬"
          value={urlSort}
          onChange={handleSortChange}
        >
          <option value="latest">{'최신순'}</option>
          <option value="oldest">{'오래된순'}</option>
        </select>
        <div className={styles.bulkActions}>
          <button
            type="button"
            className={styles.bulkBtn}
            disabled={selectedIds.size === 0 || bulkBusy}
            onClick={handleDownloadSelected}
          >
            {`선택 재다운로드${selectedUniqueTrackCount > 0 ? ` (${selectedUniqueTrackCount})` : ''}`}
          </button>
          <button
            type="button"
            className={styles.bulkBtnPrimary}
            disabled={bulkBusy || pageInfo.total === 0}
            onClick={handleDownloadAll}
          >
            {'전체 재다운로드'}
          </button>
        </div>
      </div>

      {loading ? (
        <div className={styles.loading}>{'불러오는 중...'}</div>
      ) : error ? (
        <div className={styles.error}>{error}</div>
      ) : items.length === 0 ? (
        <div className={styles.empty}>
          <p>{urlKeyword ? '검색 결과가 없습니다.' : '다운로드 기록이 없습니다.'}</p>
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
                  <th className={styles.thCheck}>
                    <input
                      type="checkbox"
                      aria-label="Select all download history items"
                      checked={allSelected}
                      onChange={(e) => toggleSelectAll(e.target.checked)}
                    />
                  </th>
                  <th>{'음원'}</th>
                  <th className={styles.thTags}>{'태그'}</th>
                  <th className={`${styles.thRight} ${styles.thBpm}`}>BPM</th>
                  <th className={`${styles.thCenter} ${styles.thKey}`}>Key</th>
                  <th className={`${styles.thRight} ${styles.thDate}`}>{'다운로드 시각'}</th>
                  <th className={styles.thActs}>{'관리'}</th>
                </tr>
              </thead>
              <tbody>
                {items.map((item) => {
                  const checked = selectedIds.has(item.downloadId);
                  return (
                    <tr key={item.downloadId} className={styles.row}>
                      <td className={styles.cellCheck}>
                        <input
                          type="checkbox"
                          aria-label={`Select ${item.title}`}
                          checked={checked}
                          onChange={() => toggleSelectOne(item.downloadId)}
                        />
                      </td>
                      <td className={styles.cellInfo}>
                        <div className={styles.info}>
                          <button
                            type="button"
                            className={styles.thumb}
                            aria-label={`Play ${item.title}`}
                            onClick={() => handlePlay(item)}
                          >
                            {item.thumbnail ? (
                              <img src={toUploadUrl(item.thumbnail)!} alt={item.title} />
                            ) : (
                              '\u266A'
                            )}
                          </button>
                          <div className={styles.infoText}>
                            <Link to={`/tracks/${item.trackId}`} className={styles.titleLink}>
                              {item.title}
                            </Link>
                          </div>
                        </div>
                      </td>
                      <td className={styles.cellTags}>
                        {item.tags.length > 0 ? (
                          <div className={styles.tagList}>
                            {item.tags.map((t) => (
                              <span key={t.id} className={styles.tagChip}>
                                {t.name}
                              </span>
                            ))}
                          </div>
                        ) : (
                          <span className={styles.tagEmpty}>{'-'}</span>
                        )}
                      </td>
                      <td className={styles.cellBpm}>{item.bpm ?? '-'}</td>
                      <td className={styles.cellKey}>{item.tonality ?? '-'}</td>
                      <td className={styles.cellDate}>{formatDateTime(item.downloadedAt)}</td>
                      <td className={styles.cellActs}>
                        <button
                          type="button"
                          className={styles.dlBtn}
                          aria-label={`${item.title} 재다운로드`}
                          onClick={() => handleDownloadOne(item)}
                          disabled={downloading === item.trackId || bulkBusy}
                          title="다시 다운로드 (카운트 차감 없음)"
                        >
                          {downloading === item.trackId ? '...' : '\u2B07'}
                        </button>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
          <div className={styles.paginationWrap}>
            <Pagination
              pageInfo={pageInfo}
              currentPage={currentPage}
              onPageChange={handlePageChange}
            />
          </div>
        </>
      )}

      <ConfirmDialog
        open={confirmDownloadAllIds !== null}
        title="전체 재다운로드"
        message={`${confirmDownloadAllIds?.length ?? 0}곡을 다운로드합니다. 계속하시겠습니까?`}
        confirmLabel="다운로드"
        busy={bulkBusy}
        onConfirm={() => {
          void confirmDownloadAll();
        }}
        onCancel={() => setConfirmDownloadAllIds(null)}
      />
    </div>
  );
}

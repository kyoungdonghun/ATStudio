/** SR-79: Download history */
import { useState, useEffect, useLayoutEffect, useCallback, useMemo, useRef } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import {
  downloadTrack,
  createDownloadFallbackFileName,
  triggerBlobDownload,
  fetchDownloadCount,
  fetchDownloadHistory,
  fetchDownloadHistoryTrackIds,
  type DownloadCount,
  type DownloadHistoryItem,
} from '@/api/downloads';
import { getApiErrorCode, toUploadUrl } from '@/api/client';
import { classifyLoadError } from '@/api/loadError';
import { formatDateTime } from '@/utils/format';
import { usePlayerStore } from '@/store/playerStore';
import { useAuthStore } from '@/store/authStore';
import { useToastStore } from '@/store/toastStore';
import ConfirmDialog from '@/components/ui/ConfirmDialog';
import Pagination from '@/components/ui/Pagination';
import type { PageInfo } from '@/types';
import { toPlayableTrack } from '@/utils/playableTrack';
import { createOwnerKey, createReadKey, getCurrentOwnerKey } from '@/utils/ownerProjection';
import styles from './DownloadHistoryPage.module.css';

const PAGE_SIZE = 20;
const EMPTY_HISTORY_ITEMS: DownloadHistoryItem[] = [];
const EMPTY_SELECTED_IDS = new Set<number>();

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

interface DownloadConfirmation {
  readKey: string;
  trackIDs: number[];
}

interface SingleDownloadState {
  readKey: string;
  trackID: number;
}

type DownloadClaim = SingleDownloadState;

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
  const [downloading, setDownloading] = useState<SingleDownloadState | null>(null);
  const [bulkBusyKey, setBulkBusyKey] = useState<string | null>(null);
  const [preparingKey, setPreparingKey] = useState<string | null>(null);
  const [selectedIds, setSelectedIds] = useState<Set<number>>(new Set());
  const [keywordInput, setKeywordInput] = useState(urlKeyword);
  const [downloadConfirmation, setDownloadConfirmation] = useState<DownloadConfirmation | null>(
    null,
  );
  const [projectionKey, setProjectionKey] = useState<string | null>(null);
  const [errorKey, setErrorKey] = useState<string | null>(null);
  const loadGenerationRef = useRef(0);
  const projectionKeyRef = useRef<string | null>(null);
  const singleGenerationRef = useRef(0);
  const singleControllerRef = useRef<AbortController | null>(null);
  const singleOwnershipRef = useRef<DownloadClaim | null>(null);
  const downloadClaimsRef = useRef(new Map<string, Map<number, DownloadClaim>>());
  const preparationGenerationRef = useRef(0);
  const preparationControllerRef = useRef<AbortController | null>(null);
  const bulkGenerationRef = useRef(0);
  const bulkControllerRef = useRef<AbortController | null>(null);

  const currentTrack = usePlayerStore((s) => s.currentTrack);
  const isPlayerPlaying = usePlayerStore((s) => s.isPlaying);
  const playTrack = usePlayerStore((s) => s.play);
  const pauseTrack = usePlayerStore((s) => s.pause);
  const resumeTrack = usePlayerStore((s) => s.resume);
  const setTrackListContext = usePlayerStore((s) => s.setTrackListContext);
  const userID = useAuthStore((s) => s.user?.id ?? null);
  const accessToken = useAuthStore((s) => s.accessToken);
  const role = useAuthStore((s) => s.role);
  const ownerKey = createOwnerKey(userID, accessToken);
  const isAdmin = role === 'ADMIN';
  const toast = useToastStore((s) => s.show);
  const readKey = createReadKey(
    ownerKey,
    'download-history',
    currentPage,
    urlKeyword,
    urlSort,
    isAdmin,
  );
  const currentReadKeyRef = useRef(readKey);
  currentReadKeyRef.current = readKey;

  function isCurrentRead(expectedReadKey: string | null): expectedReadKey is string {
    return (
      expectedReadKey !== null &&
      currentReadKeyRef.current === expectedReadKey &&
      getCurrentOwnerKey(ownerKey) === ownerKey
    );
  }

  function isCurrentProjection(expectedReadKey = readKey): expectedReadKey is string {
    return isCurrentRead(expectedReadKey) && projectionKeyRef.current === expectedReadKey;
  }

  function acquireDownloadClaim(operationKey: string, trackID: number): DownloadClaim | null {
    let claimsByTrack = downloadClaimsRef.current.get(operationKey);
    if (claimsByTrack?.has(trackID)) return null;

    const claim = { readKey: operationKey, trackID };
    if (!claimsByTrack) {
      claimsByTrack = new Map();
      downloadClaimsRef.current.set(operationKey, claimsByTrack);
    }
    claimsByTrack.set(trackID, claim);
    return claim;
  }

  function releaseDownloadClaim(claim: DownloadClaim) {
    const claimsByTrack = downloadClaimsRef.current.get(claim.readKey);
    if (claimsByTrack?.get(claim.trackID) !== claim) return;

    claimsByTrack.delete(claim.trackID);
    if (claimsByTrack.size === 0) downloadClaimsRef.current.delete(claim.readKey);
  }

  // Keep input synced if URL changes externally (back/forward nav)
  useEffect(() => {
    setKeywordInput(urlKeyword);
  }, [urlKeyword]);

  const load = useCallback(
    async (signal?: AbortSignal) => {
      const requestKey = readKey;
      const requestOwnerKey = ownerKey;
      const generation = ++loadGenerationRef.current;
      const isCurrent = () =>
        requestKey !== null &&
        loadGenerationRef.current === generation &&
        currentReadKeyRef.current === requestKey &&
        getCurrentOwnerKey(requestOwnerKey) === requestOwnerKey;
      try {
        setLoading(true);
        setError(null);
        setItems([]);
        setPageInfo(defaultPageInfo);
        setDlCount(null);
        setSelectedIds(new Set());
        setDownloadConfirmation(null);
        if (requestKey === null) return;
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
        if (isCurrent()) {
          setItems(historyResult.dataList ?? []);
          setPageInfo(historyResult.pageInfo ?? defaultPageInfo);
          setDlCount(countResult);
          setSelectedIds(new Set());
          projectionKeyRef.current = requestKey;
          setProjectionKey(requestKey);
        }
      } catch (loadError: unknown) {
        if (isCurrent() && classifyLoadError(loadError) !== 'cancelled') {
          setError('다운로드 기록을 불러오지 못했습니다.');
          setErrorKey(requestKey);
        }
      } finally {
        if (isCurrent()) setLoading(false);
      }
    },
    [currentPage, isAdmin, ownerKey, readKey, urlKeyword, urlSort],
  );

  useEffect(() => {
    const controller = new AbortController();
    void load(controller.signal);
    return () => {
      controller.abort();
      loadGenerationRef.current += 1;
    };
  }, [load]);

  useLayoutEffect(() => {
    const downloadClaims = downloadClaimsRef.current;
    return () => {
      singleControllerRef.current?.abort();
      singleControllerRef.current = null;
      singleOwnershipRef.current = null;
      if (readKey !== null) downloadClaims.delete(readKey);
      preparationControllerRef.current?.abort();
      bulkControllerRef.current?.abort();
      singleGenerationRef.current += 1;
      preparationGenerationRef.current += 1;
      bulkGenerationRef.current += 1;
    };
  }, [readKey]);

  const projectionCurrent = readKey !== null && projectionKey === readKey;
  const currentItems = projectionCurrent ? items : EMPTY_HISTORY_ITEMS;
  const currentPageInfo = projectionCurrent ? pageInfo : defaultPageInfo;
  const currentDlCount = projectionCurrent ? dlCount : null;
  const currentSelectedIds = projectionCurrent ? selectedIds : EMPTY_SELECTED_IDS;
  const currentError = errorKey === readKey ? error : null;
  const currentLoading = loading || (!projectionCurrent && currentError === null);
  const currentDownloading = downloading?.readKey === readKey ? downloading.trackID : null;
  const bulkBusy = bulkBusyKey === readKey;
  const bulkPreparing = preparingKey === readKey;
  const currentConfirmation =
    downloadConfirmation?.readKey === readKey ? downloadConfirmation : null;

  /* SR-83: Publish downloaded tracks as player context so Next/Prev traverses them.
     Note: download history can contain duplicates (same track downloaded multiple times),
     so we de-duplicate by trackId, keeping the first occurrence. */
  useLayoutEffect(() => {
    const seen = new Set<number>();
    const tracks = currentItems
      .filter((item) => {
        if (seen.has(item.trackId)) return false;
        seen.add(item.trackId);
        return true;
      })
      .map((item) => toPlayableTrack(item));
    return setTrackListContext(tracks);
  }, [currentItems, setTrackListContext]);

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
    const operationKey = readKey;
    if (
      !isCurrentProjection(operationKey) ||
      !currentItems.some((candidate) => candidate.downloadId === item.downloadId)
    ) {
      return;
    }
    const ownership = acquireDownloadClaim(operationKey, item.trackId);
    if (!ownership) return;
    singleControllerRef.current?.abort();
    const controller = new AbortController();
    singleControllerRef.current = controller;
    singleOwnershipRef.current = ownership;
    const generation = ++singleGenerationRef.current;
    const isCurrent = () =>
      generation === singleGenerationRef.current &&
      !controller.signal.aborted &&
      isCurrentProjection(operationKey);
    try {
      setDownloading({ readKey: operationKey, trackID: item.trackId });
      const download = await downloadTrack(
        item.trackId,
        createDownloadFallbackFileName('track', item.trackId, item.title, 'mp3'),
        controller.signal,
      );
      if (!isCurrent()) return;
      triggerBlobDownload(download);
      toast('success', `${item.title} 다운로드 완료`);
    } catch (err: unknown) {
      if (!isCurrent() || classifyLoadError(err) === 'cancelled') return;
      const code = await getApiErrorCode(err);
      const msg =
        code === 'NO_ACTIVE_SUBSCRIPTION'
          ? '구독이 필요한 기능입니다.'
          : code === 'DOWNLOAD_LIMIT_EXCEEDED'
            ? '금일 다운로드 횟수를 모두 사용했습니다.'
            : err instanceof Error
              ? err.message
              : '다운로드에 실패했습니다.';
      setError(msg);
      setErrorKey(operationKey);
    } finally {
      releaseDownloadClaim(ownership);
      if (singleOwnershipRef.current === ownership) {
        singleOwnershipRef.current = null;
        if (singleControllerRef.current === controller) singleControllerRef.current = null;
        if (isCurrent()) setDownloading(null);
      }
    }
  }

  async function downloadByTrackIds(trackIds: number[], operationKey: string) {
    if (trackIds.length === 0 || !isCurrentProjection(operationKey)) return;
    bulkControllerRef.current?.abort();
    const controller = new AbortController();
    bulkControllerRef.current = controller;
    const generation = ++bulkGenerationRef.current;
    const isCurrent = () =>
      generation === bulkGenerationRef.current &&
      !controller.signal.aborted &&
      isCurrentProjection(operationKey);
    try {
      setBulkBusyKey(operationKey);
      let ok = 0;
      let fail = 0;
      let failureCode: 'NO_ACTIVE_SUBSCRIPTION' | 'DOWNLOAD_LIMIT_EXCEEDED' | null = null;
      // Look up titles on-page for a nicer filename; fall back to id.
      const titleById = new Map(currentItems.map((i) => [i.trackId, i.title] as const));
      for (const trackId of trackIds) {
        if (!isCurrent()) return;
        const ownership = acquireDownloadClaim(operationKey, trackId);
        if (!ownership) continue;
        try {
          const download = await downloadTrack(
            trackId,
            createDownloadFallbackFileName('track', trackId, titleById.get(trackId), 'mp3'),
            controller.signal,
          );
          if (!isCurrent()) return;
          triggerBlobDownload(download);
          ok += 1;
        } catch (downloadError) {
          if (!isCurrent() || classifyLoadError(downloadError) === 'cancelled') return;
          const code = await getApiErrorCode(downloadError);
          if (code === 'NO_ACTIVE_SUBSCRIPTION' || code === 'DOWNLOAD_LIMIT_EXCEEDED') {
            failureCode = code;
          }
          fail += 1;
        } finally {
          releaseDownloadClaim(ownership);
        }
      }
      if (!isCurrent()) return;
      if (ok === 0 && fail === 0) return;
      if (failureCode === 'NO_ACTIVE_SUBSCRIPTION') {
        toast('warning', '구독이 필요한 기능입니다.');
      } else if (failureCode === 'DOWNLOAD_LIMIT_EXCEEDED') {
        toast('warning', '금일 다운로드 횟수를 모두 사용했습니다.');
      } else if (fail === 0) {
        toast('success', `${ok}곡 다운로드 완료`);
      } else {
        toast('error', `${ok}곡 성공, ${fail}곡 실패`);
      }
      if (!isAdmin) {
        try {
          const count = await fetchDownloadCount(controller.signal);
          if (isCurrent()) setDlCount(count);
        } catch (countError) {
          if (!isCurrent() || classifyLoadError(countError) === 'cancelled') return;
        }
      }
    } finally {
      if (isCurrent()) setBulkBusyKey(null);
    }
  }

  async function handleDownloadSelected() {
    const operationKey = readKey;
    if (!isCurrentProjection(operationKey)) return;
    const trackIds = [
      ...new Set(
        currentItems
          .filter((item) => currentSelectedIds.has(item.downloadId))
          .map((item) => item.trackId),
      ),
    ];
    if (trackIds.length === 0) return;
    await downloadByTrackIds(trackIds, operationKey);
  }

  async function handleDownloadAll() {
    const operationKey = readKey;
    if (!isCurrentProjection(operationKey)) return;
    preparationControllerRef.current?.abort();
    const controller = new AbortController();
    preparationControllerRef.current = controller;
    const generation = ++preparationGenerationRef.current;
    const isCurrent = () =>
      generation === preparationGenerationRef.current &&
      !controller.signal.aborted &&
      isCurrentProjection(operationKey);
    try {
      setPreparingKey(operationKey);
      const ids = await fetchDownloadHistoryTrackIds(urlKeyword || undefined, controller.signal);
      if (!isCurrent()) return;
      if (ids.length === 0) {
        toast('error', '다운로드할 항목이 없습니다.');
        return;
      }
      setDownloadConfirmation({ readKey: operationKey, trackIDs: ids });
    } catch (preparationError) {
      if (!isCurrent() || classifyLoadError(preparationError) === 'cancelled') return;
      toast('error', '전체 재다운로드에 실패했습니다.');
    } finally {
      if (isCurrent()) setPreparingKey(null);
    }
  }

  async function confirmDownloadAll() {
    if (
      !currentConfirmation ||
      currentConfirmation.trackIDs.length === 0 ||
      !isCurrentProjection(currentConfirmation.readKey)
    ) {
      return;
    }
    const confirmation = currentConfirmation;
    setDownloadConfirmation(null);
    await downloadByTrackIds(confirmation.trackIDs, confirmation.readKey);
  }

  function toggleSelectAll(checked: boolean) {
    if (!isCurrentProjection()) return;
    if (checked) {
      setSelectedIds(new Set(currentItems.map((i) => i.downloadId)));
    } else {
      setSelectedIds(new Set());
    }
  }

  function toggleSelectOne(downloadId: number) {
    if (!isCurrentProjection() || !currentItems.some((item) => item.downloadId === downloadId)) {
      return;
    }
    setSelectedIds((prev) => {
      const next = new Set(prev);
      if (next.has(downloadId)) next.delete(downloadId);
      else next.add(downloadId);
      return next;
    });
  }

  function handlePlay(item: DownloadHistoryItem) {
    if (
      !isCurrentProjection() ||
      !currentItems.some((candidate) => candidate.downloadId === item.downloadId)
    ) {
      return;
    }
    if (currentTrack?.id === item.trackId) {
      if (isPlayerPlaying) pauseTrack();
      else resumeTrack();
      return;
    }
    playTrack(toPlayableTrack(item));
  }

  const allSelected = useMemo(
    () =>
      currentItems.length > 0 && currentItems.every((i) => currentSelectedIds.has(i.downloadId)),
    [currentItems, currentSelectedIds],
  );
  // Derive selected unique track count for button label.
  const selectedUniqueTrackCount = useMemo(() => {
    const set = new Set<number>();
    for (const i of currentItems) {
      if (currentSelectedIds.has(i.downloadId)) set.add(i.trackId);
    }
    return set.size;
  }, [currentItems, currentSelectedIds]);

  return (
    <div className={styles.page}>
      <div className={styles.pageHeader}>
        <h1 className={styles.pageTitle}>
          {'다운로드 기록'}
          {currentPageInfo.total > 0 && (
            <span className={styles.count}>{currentPageInfo.total}건</span>
          )}
        </h1>
      </div>

      {/* Daily download limit info */}
      <div className={styles.limitInfo}>
        {isAdmin ? (
          <span className={styles.limitBadge}>{'무제한 다운로드'}</span>
        ) : currentDlCount ? (
          <span className={styles.limitBadge}>
            {'오늘 '}
            {currentDlCount.todayDownloads}
            {' / '}
            {currentDlCount.dailyLimit === -1 ? '무제한' : `${currentDlCount.dailyLimit}곡`}
            {currentDlCount.dailyLimit !== -1 && ` (남은 횟수: ${currentDlCount.remaining})`}
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
            disabled={currentSelectedIds.size === 0 || bulkBusy || bulkPreparing}
            onClick={handleDownloadSelected}
          >
            {`선택 재다운로드${selectedUniqueTrackCount > 0 ? ` (${selectedUniqueTrackCount})` : ''}`}
          </button>
          <button
            type="button"
            className={styles.bulkBtnPrimary}
            disabled={bulkBusy || bulkPreparing || currentPageInfo.total === 0}
            onClick={handleDownloadAll}
          >
            {'전체 재다운로드'}
          </button>
        </div>
      </div>

      {currentLoading ? (
        <div className={styles.loading}>{'불러오는 중...'}</div>
      ) : currentError ? (
        <div className={styles.error}>{currentError}</div>
      ) : currentItems.length === 0 ? (
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
                {currentItems.map((item) => {
                  const checked = currentSelectedIds.has(item.downloadId);
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
                          disabled={currentDownloading === item.trackId || bulkBusy}
                          title="다시 다운로드 (카운트 차감 없음)"
                        >
                          {currentDownloading === item.trackId ? '...' : '\u2B07'}
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
              pageInfo={currentPageInfo}
              currentPage={currentPage}
              onPageChange={handlePageChange}
            />
          </div>
        </>
      )}

      <ConfirmDialog
        open={currentConfirmation !== null}
        title="전체 재다운로드"
        message={`${currentConfirmation?.trackIDs.length ?? 0}곡을 다운로드합니다. 계속하시겠습니까?`}
        confirmLabel="다운로드"
        busy={bulkBusy}
        onConfirm={() => {
          void confirmDownloadAll();
        }}
        onCancel={() => setDownloadConfirmation(null)}
      />
    </div>
  );
}

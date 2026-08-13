import {
  useState,
  useEffect,
  useId,
  useLayoutEffect,
  useMemo,
  useRef,
  type FormEvent,
} from 'react';
import { Link, useParams, useNavigate } from 'react-router-dom';
import {
  fetchAlbumDetail,
  updateAlbum,
  addTrackToAlbum,
  removeTrackFromAlbum,
  reorderAlbumTracks,
  type AlbumTrack,
} from '@/api/albums';
import { fetchTracks } from '@/api/tracks';
import { toUploadUrl } from '@/api/client';
import { useToastStore } from '@/store/toastStore';
import type { TrackListItem } from '@/types';
import { TITLE_ALBUM_MAX, DESCRIPTION_MAX } from '@/utils/validation';
import { parsePositiveDecimalRouteID } from '@/utils/routeId';
import Button from '@/components/ui/Button';
import AlbumThumbnailField from './AlbumThumbnailField';
import { emptyAlbumThumbnailSelection, isAlbumThumbnailBlocked } from './albumThumbnail';
import styles from './AlbumEditPage.module.css';

type MembershipRefreshProvenance = 'committed' | 'unconfirmed';
type AlbumPageOwner = Readonly<{ albumId: number; generation: symbol }>;

/** Screen L-5: Album edit */
export default function AlbumEditPage() {
  const { albumId: id } = useParams<{ albumId: string }>();
  const albumId = useMemo(() => parsePositiveDecimalRouteID(id), [id]);
  const navigate = useNavigate();
  const toast = useToastStore((s) => s.show);
  const pageOwner = useMemo<AlbumPageOwner | null>(
    () => (albumId === null ? null : { albumId, generation: Symbol() }),
    [albumId],
  );
  const currentPageOwnerRef = useRef<AlbumPageOwner | null>(pageOwner);

  useLayoutEffect(() => {
    currentPageOwnerRef.current = pageOwner;
    return () => {
      if (currentPageOwnerRef.current?.generation === pageOwner?.generation) {
        currentPageOwnerRef.current = null;
      }
    };
  }, [pageOwner]);

  function isPageOwnerCurrent(owner: AlbumPageOwner | null): owner is AlbumPageOwner {
    const currentOwner = currentPageOwnerRef.current;
    return (
      owner !== null &&
      currentOwner?.albumId === owner.albumId &&
      currentOwner.generation === owner.generation
    );
  }

  /* ── Form state ── */
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [thumbnail, setThumbnail] = useState(emptyAlbumThumbnailSelection);

  /* ── Existing data ── */
  const [currentThumbUrl, setCurrentThumbUrl] = useState<string | null>(null);

  /* ── Track management state ── */
  const [tracks, setTracks] = useState<AlbumTrack[]>([]);
  const [trackBusy, setTrackBusy] = useState(false);
  const [membershipRefreshFailure, setMembershipRefreshFailure] =
    useState<MembershipRefreshProvenance | null>(null);
  const [committedMembershipFence, setCommittedMembershipFence] = useState<ReadonlySet<number>>(
    () => new Set(),
  );
  const committedMembershipFenceRef = useRef<Set<number>>(new Set());
  const membershipRequestGenerationRef = useRef(0);
  const membershipRequestControllerRef = useRef<AbortController | null>(null);

  /* ── Track search state ── */
  const [searchKeyword, setSearchKeyword] = useState('');
  const [searchResults, setSearchResults] = useState<TrackListItem[]>([]);
  const [searchOpen, setSearchOpen] = useState(false);
  const [searching, setSearching] = useState(false);
  const [searchError, setSearchError] = useState(false);
  const [activeSearchIndex, setActiveSearchIndex] = useState(-1);
  const searchRef = useRef<HTMLDivElement>(null);
  const searchListboxId = useId();
  const searchRequestGenerationRef = useRef(0);
  const searchRequestControllerRef = useRef<AbortController | null>(null);
  const lastSearchKeywordRef = useRef('');

  const availableSearchResults = useMemo(() => {
    const currentMemberIDs = new Set(tracks.map((track) => track.trackId));
    return searchResults.filter(
      (track) => !currentMemberIDs.has(track.id) && !committedMembershipFence.has(track.id),
    );
  }, [committedMembershipFence, searchResults, tracks]);
  const activeSearchResult = availableSearchResults[activeSearchIndex];
  const membershipRefreshError =
    membershipRefreshFailure === 'committed'
      ? '변경은 완료되었지만 최신 트랙 목록을 불러오지 못했습니다.'
      : membershipRefreshFailure === 'unconfirmed'
        ? '변경 결과와 최신 트랙 목록을 확인하지 못했습니다.'
        : null;

  /* ── UI state ── */
  const [pageLoading, setPageLoading] = useState(albumId !== null);
  const [pageRetryToken, setPageRetryToken] = useState(0);
  const [pageLoadError, setPageLoadError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const pageRequestGenerationRef = useRef(0);

  /* ── Load existing album ── */
  useEffect(() => {
    searchRequestControllerRef.current?.abort();
    searchRequestControllerRef.current = null;
    membershipRequestControllerRef.current?.abort();
    membershipRequestControllerRef.current = null;
    searchRequestGenerationRef.current += 1;
    membershipRequestGenerationRef.current += 1;
    setSearchKeyword('');
    setSearchResults([]);
    setSearchOpen(false);
    setSearching(false);
    setSearchError(false);
    setActiveSearchIndex(-1);
    setTrackBusy(false);

    if (albumId === null) {
      setPageLoading(false);
      setPageLoadError(null);
      return;
    }

    const ownedAlbumId = albumId;
    const controller = new AbortController();
    const generation = ++pageRequestGenerationRef.current;
    const isCurrent = () =>
      generation === pageRequestGenerationRef.current && !controller.signal.aborted;

    async function loadAlbum() {
      setPageLoading(true);
      setPageLoadError(null);
      setError(null);
      setTitle('');
      setDescription('');
      setCurrentThumbUrl(null);
      setTracks([]);
      setMembershipRefreshFailure(null);
      setThumbnail(emptyAlbumThumbnailSelection());

      try {
        const album = await fetchAlbumDetail(ownedAlbumId, controller.signal);
        if (!isCurrent()) return;

        setTitle(album.title);
        setDescription(album.description ?? '');
        setCurrentThumbUrl(toUploadUrl(album.thumbnailUrl));
        setTracks([...album.tracks].sort((a, b) => a.order - b.order));
        const authoritativeFence = new Set<number>();
        committedMembershipFenceRef.current = authoritativeFence;
        setCommittedMembershipFence(authoritativeFence);
      } catch {
        if (isCurrent()) {
          setPageLoadError('앨범 정보를 불러오지 못했습니다. 잠시 후 다시 시도해주세요.');
        }
      } finally {
        if (isCurrent()) setPageLoading(false);
      }
    }

    void loadAlbum();
    return () => {
      controller.abort();
      pageRequestGenerationRef.current += 1;
    };
  }, [albumId, pageRetryToken]);

  /* ── Close search dropdown on outside click ── */
  useEffect(() => {
    function handleClickOutside(e: MouseEvent) {
      if (searchRef.current && !searchRef.current.contains(e.target as Node)) {
        setSearchOpen(false);
        setActiveSearchIndex(-1);
      }
    }
    document.addEventListener('mousedown', handleClickOutside);
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
      searchRequestControllerRef.current?.abort();
      membershipRequestControllerRef.current?.abort();
      searchRequestGenerationRef.current += 1;
      membershipRequestGenerationRef.current += 1;
    };
  }, []);

  /* ── Refetch album tracks ── */
  async function refetchTracks(
    provenance: MembershipRefreshProvenance,
    owner: AlbumPageOwner,
  ): Promise<boolean> {
    if (!isPageOwnerCurrent(owner)) return false;
    membershipRequestControllerRef.current?.abort();
    const controller = new AbortController();
    membershipRequestControllerRef.current = controller;
    const generation = ++membershipRequestGenerationRef.current;
    const isCurrent = () =>
      generation === membershipRequestGenerationRef.current &&
      !controller.signal.aborted &&
      isPageOwnerCurrent(owner);

    try {
      const album = await fetchAlbumDetail(owner.albumId, controller.signal);
      if (!isCurrent()) return false;
      setTracks([...album.tracks].sort((a, b) => a.order - b.order));
      const authoritativeFence = new Set<number>();
      committedMembershipFenceRef.current = authoritativeFence;
      setCommittedMembershipFence(authoritativeFence);
      setMembershipRefreshFailure(null);
      return true;
    } catch {
      if (isCurrent()) {
        setMembershipRefreshFailure(provenance);
      }
      return false;
    } finally {
      if (isCurrent()) membershipRequestControllerRef.current = null;
    }
  }

  /* ── Track search handler ── */
  async function runTrackSearch(keyword: string) {
    const kw = keyword.trim();
    if (!kw) return;

    searchRequestControllerRef.current?.abort();
    const controller = new AbortController();
    searchRequestControllerRef.current = controller;
    const generation = ++searchRequestGenerationRef.current;
    const isCurrent = () =>
      generation === searchRequestGenerationRef.current && !controller.signal.aborted;
    lastSearchKeywordRef.current = kw;
    setSearching(true);
    setSearchError(false);
    setSearchOpen(true);
    setActiveSearchIndex(-1);
    try {
      const res = await fetchTracks({ keyword: kw, size: 10 }, controller.signal);
      if (!isCurrent()) return;
      setSearchResults(res.dataList);
    } catch {
      if (!isCurrent()) return;
      setSearchResults([]);
      setSearchError(true);
    } finally {
      if (isCurrent()) {
        setSearching(false);
        searchRequestControllerRef.current = null;
      }
    }
  }

  function handleTrackSearch() {
    void runTrackSearch(searchKeyword);
  }

  function handleSearchKeywordChange(value: string) {
    searchRequestControllerRef.current?.abort();
    searchRequestControllerRef.current = null;
    searchRequestGenerationRef.current += 1;
    setSearchKeyword(value);
    setSearchResults([]);
    setSearchError(false);
    setSearchOpen(false);
    setSearching(false);
    setActiveSearchIndex(-1);
  }

  function dismissSearch() {
    setSearchOpen(false);
    setActiveSearchIndex(-1);
  }

  function handleSearchFocusOut(event: React.FocusEvent<HTMLDivElement>) {
    const nextTarget = event.relatedTarget;
    if (nextTarget instanceof Node && event.currentTarget.contains(nextTarget)) return;
    dismissSearch();
  }

  function handleSearchKeyDown(event: React.KeyboardEvent<HTMLInputElement>) {
    if (event.key === 'Escape') {
      event.preventDefault();
      dismissSearch();
      return;
    }
    if (event.key === 'Tab') {
      dismissSearch();
      return;
    }
    if (
      event.key === 'ArrowDown' ||
      event.key === 'ArrowUp' ||
      event.key === 'Home' ||
      event.key === 'End'
    ) {
      if (availableSearchResults.length === 0) return;
      event.preventDefault();
      setSearchOpen(true);
      setActiveSearchIndex((current) => {
        if (event.key === 'Home') return 0;
        if (event.key === 'End') return availableSearchResults.length - 1;
        if (event.key === 'ArrowDown') {
          return current < availableSearchResults.length - 1 ? current + 1 : 0;
        }
        return current > 0 ? current - 1 : availableSearchResults.length - 1;
      });
      return;
    }
    if (event.key !== 'Enter') return;

    event.preventDefault();
    const selected = availableSearchResults[activeSearchIndex];
    if (searchOpen && selected) {
      const isUnavailable =
        trackBusy ||
        tracks.some((track) => track.trackId === selected.id) ||
        committedMembershipFenceRef.current.has(selected.id);
      if (!isUnavailable) void handleAddTrack(selected.id);
      return;
    }
    handleTrackSearch();
  }

  /* ── Add track ── */
  async function handleAddTrack(trackId: number) {
    const owner = pageOwner;
    if (!isPageOwnerCurrent(owner)) return;
    if (
      tracks.some((track) => track.trackId === trackId) ||
      committedMembershipFenceRef.current.has(trackId)
    ) {
      toast('error', '이미 앨범에 포함된 트랙입니다.');
      return;
    }
    setTrackBusy(true);
    try {
      await addTrackToAlbum(owner.albumId, trackId);
      if (!isPageOwnerCurrent(owner)) return;
      const nextMembershipFence = new Set(committedMembershipFenceRef.current);
      nextMembershipFence.add(trackId);
      committedMembershipFenceRef.current = nextMembershipFence;
      setCommittedMembershipFence(nextMembershipFence);
      const refreshed = await refetchTracks('committed', owner);
      if (!isPageOwnerCurrent(owner)) return;
      toast(
        refreshed ? 'success' : 'warning',
        refreshed ? '트랙이 추가되었습니다.' : '트랙 추가는 완료되었습니다.',
      );
      dismissSearch();
      setSearchKeyword('');
      setSearchResults([]);
    } catch (err) {
      if (!isPageOwnerCurrent(owner)) return;
      const msg = err instanceof Error ? err.message : '트랙 추가에 실패했습니다.';
      toast('error', msg);
    } finally {
      if (isPageOwnerCurrent(owner)) setTrackBusy(false);
    }
  }

  /* ── Remove track ── */
  async function handleRemoveTrack(trackId: number) {
    const owner = pageOwner;
    if (!isPageOwnerCurrent(owner)) return;
    setTrackBusy(true);
    try {
      await removeTrackFromAlbum(owner.albumId, trackId);
      if (!isPageOwnerCurrent(owner)) return;
      const refreshed = await refetchTracks('committed', owner);
      if (!isPageOwnerCurrent(owner)) return;
      toast(
        refreshed ? 'success' : 'warning',
        refreshed ? '트랙이 제거되었습니다.' : '트랙 제거는 완료되었습니다.',
      );
    } catch (err) {
      if (!isPageOwnerCurrent(owner)) return;
      const msg = err instanceof Error ? err.message : '트랙 제거에 실패했습니다.';
      toast('error', msg);
    } finally {
      if (isPageOwnerCurrent(owner)) setTrackBusy(false);
    }
  }

  /* ── Reorder tracks (move up / down) ── */
  async function handleMoveTrack(index: number, direction: 'up' | 'down') {
    const owner = pageOwner;
    if (!isPageOwnerCurrent(owner)) return;
    const swapIdx = direction === 'up' ? index - 1 : index + 1;
    if (swapIdx < 0 || swapIdx >= tracks.length) return;

    const reordered = [...tracks];
    [reordered[index], reordered[swapIdx]] = [reordered[swapIdx], reordered[index]];

    const trackOrders = reordered.map((t, i) => ({ trackId: t.trackId, order: i }));

    /* Optimistic update */
    setTracks(reordered);
    setTrackBusy(true);
    try {
      await reorderAlbumTracks(owner.albumId, trackOrders);
      if (!isPageOwnerCurrent(owner)) return;
      await refetchTracks('committed', owner);
      if (!isPageOwnerCurrent(owner)) return;
    } catch (err) {
      if (!isPageOwnerCurrent(owner)) return;
      const msg = err instanceof Error ? err.message : '순서 변경에 실패했습니다.';
      toast('error', msg);
      await refetchTracks('unconfirmed', owner);
      if (!isPageOwnerCurrent(owner)) return;
    } finally {
      if (isPageOwnerCurrent(owner)) setTrackBusy(false);
    }
  }

  /* ── Submit ── */
  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setError(null);

    if (albumId === null || isAlbumThumbnailBlocked(thumbnail)) return;

    if (!title.trim()) {
      setError('앨범 제목을 입력해주세요.');
      return;
    }

    const formData = new FormData();
    formData.append('title', title.trim());
    formData.append('description', description.trim());
    if (thumbnail.file) {
      formData.append('thumbnailFile', thumbnail.file);
    }

    setSubmitting(true);
    try {
      await updateAlbum(albumId, formData);
      navigate('/admin/albums');
    } catch (err) {
      const msg = err instanceof Error ? err.message : '앨범 수정에 실패했습니다.';
      setError(msg);
    } finally {
      setSubmitting(false);
    }
  }

  if (pageLoading) {
    return (
      <div className={styles.page}>
        <div className={styles.loading}>{'Loading...'}</div>
      </div>
    );
  }

  if (albumId === null) {
    return (
      <div className={styles.page}>
        <div className={styles.recovery}>
          <h1 className={styles.pageTitle}>앨범을 열 수 없습니다.</h1>
          <p>올바른 앨범을 다시 선택해주세요.</p>
          <div className={styles.recoveryActions}>
            <Link to="/admin/albums">앨범 관리로 이동</Link>
            <Link to="/">홈으로 이동</Link>
          </div>
        </div>
      </div>
    );
  }

  if (pageLoadError) {
    return (
      <div className={styles.page}>
        <div className={styles.recovery} role="alert" aria-label="앨범 정보 불러오기 실패">
          <h1 className={styles.pageTitle}>앨범 정보를 불러오지 못했습니다.</h1>
          <p>{pageLoadError}</p>
          <div className={styles.recoveryActions}>
            <button type="button" onClick={() => setPageRetryToken((value) => value + 1)}>
              다시 시도
            </button>
            <Link to="/admin/albums">앨범 관리로 이동</Link>
            <Link to="/">홈으로 이동</Link>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className={styles.page}>
      <h1 className={styles.pageTitle}>{'앨범 수정'}</h1>

      <form className={styles.form} onSubmit={handleSubmit}>
        {error && <div className={styles.error}>{error}</div>}

        {/* Title */}
        <div className={styles.field}>
          <label className={`${styles.label} ${styles.required}`}>{'제목'}</label>
          <input
            className={styles.input}
            type="text"
            maxLength={TITLE_ALBUM_MAX}
            value={title}
            onChange={(e) => setTitle(e.target.value)}
          />
        </div>

        {/* Description */}
        <div className={styles.field}>
          <label className={styles.label}>{'설명'}</label>
          <textarea
            className={styles.textarea}
            maxLength={DESCRIPTION_MAX}
            value={description}
            onChange={(e) => setDescription(e.target.value)}
          />
        </div>

        <AlbumThumbnailField
          value={thumbnail}
          onChange={setThumbnail}
          existingImageUrl={currentThumbUrl}
          disabled={submitting}
        />

        {/* Actions */}
        <div className={styles.actions}>
          <Button variant="ghost" type="button" onClick={() => navigate(-1)} disabled={submitting}>
            {'취소'}
          </Button>
          <Button type="submit" loading={submitting} disabled={isAlbumThumbnailBlocked(thumbnail)}>
            {'저장'}
          </Button>
        </div>
      </form>

      {/* ── Track management (separate from form) ── */}
      <section className={styles.trackSection}>
        <h2 className={styles.trackSectionTitle}>{'앨범 트랙'}</h2>

        {/* Search */}
        <div className={styles.trackSearch} ref={searchRef} onBlur={handleSearchFocusOut}>
          <div className={styles.trackSearchRow}>
            <input
              className={styles.input}
              type="text"
              role="combobox"
              aria-label="앨범에 추가할 트랙 검색"
              aria-autocomplete="list"
              aria-expanded={searchOpen}
              aria-controls={searchOpen ? searchListboxId : undefined}
              aria-activedescendant={
                searchOpen && activeSearchResult
                  ? `${searchListboxId}-option-${activeSearchResult.id}`
                  : undefined
              }
              placeholder="트랙 제목 또는 Usage 태그 검색"
              value={searchKeyword}
              onChange={(e) => handleSearchKeywordChange(e.target.value)}
              onKeyDown={handleSearchKeyDown}
            />
            <Button
              type="button"
              variant="ghost"
              onClick={handleTrackSearch}
              loading={searching}
              disabled={trackBusy}
            >
              {'검색'}
            </Button>
          </div>

          {searchOpen && searching && (
            <div className={styles.searchEmpty} role="status">
              트랙을 검색하는 중입니다.
            </div>
          )}

          {searchOpen && !searching && !searchError && availableSearchResults.length > 0 && (
            <ul
              id={searchListboxId}
              className={styles.searchDropdown}
              role="listbox"
              aria-label="트랙 검색 결과"
            >
              {availableSearchResults.map((t, index) => {
                const isActive = index === activeSearchIndex;
                return (
                  <li
                    id={`${searchListboxId}-option-${t.id}`}
                    key={t.id}
                    className={`${styles.searchItem} ${isActive ? styles.searchItemActive : ''}`}
                    role="option"
                    aria-selected={isActive}
                    aria-disabled={trackBusy}
                    onMouseDown={(event) => event.preventDefault()}
                    onMouseMove={() => setActiveSearchIndex(index)}
                    onClick={() => {
                      if (!trackBusy) void handleAddTrack(t.id);
                    }}
                  >
                    <span className={styles.searchItemInfo}>
                      <span className={styles.searchItemTitle}>{t.title}</span>
                      <span className={styles.searchItemArtist}>{t.artistName}</span>
                    </span>
                    <span
                      className={`${styles.addIndicator} ${trackBusy ? styles.addIndicatorDisabled : ''}`}
                      aria-hidden="true"
                    >
                      + 추가
                    </span>
                  </li>
                );
              })}
            </ul>
          )}

          {searchOpen && !searching && searchError && (
            <div className={styles.searchEmpty} role="alert" aria-label="트랙 검색 실패">
              <p>트랙 검색 결과를 불러오지 못했습니다.</p>
              <button
                type="button"
                onClick={() => void runTrackSearch(lastSearchKeywordRef.current)}
              >
                검색 다시 시도
              </button>
            </div>
          )}

          {searchOpen && !searching && !searchError && availableSearchResults.length === 0 && (
            <div className={styles.searchEmpty}>{'검색 결과가 없습니다.'}</div>
          )}
        </div>

        {/* Track list */}
        {membershipRefreshError && (
          <div className={styles.membershipError} role="alert" aria-label="앨범 트랙 새로고침 실패">
            <span>{membershipRefreshError}</span>
            <button
              type="button"
              disabled={trackBusy}
              onClick={() => {
                const owner = pageOwner;
                if (!isPageOwnerCurrent(owner)) return;
                setTrackBusy(true);
                void refetchTracks(membershipRefreshFailure ?? 'unconfirmed', owner).finally(() => {
                  if (isPageOwnerCurrent(owner)) setTrackBusy(false);
                });
              }}
            >
              트랙 목록 다시 불러오기
            </button>
          </div>
        )}
        {tracks.length === 0 ? (
          <p className={styles.trackEmpty}>{'앨범에 트랙이 없습니다.'}</p>
        ) : (
          <ul className={styles.trackList}>
            {tracks.map((t, idx) => (
              <li key={t.trackId} className={styles.trackRow}>
                <span className={styles.trackOrder}>{idx + 1}</span>
                <span className={styles.trackInfo}>
                  <span className={styles.trackTitle}>{t.title}</span>
                  <span className={styles.trackArtist}>{t.artistName}</span>
                </span>
                <div className={styles.trackActions}>
                  <button
                    type="button"
                    className={styles.moveBtn}
                    disabled={idx === 0 || trackBusy}
                    onClick={() => handleMoveTrack(idx, 'up')}
                    title="위로"
                  >
                    {'\u25B2'}
                  </button>
                  <button
                    type="button"
                    className={styles.moveBtn}
                    disabled={idx === tracks.length - 1 || trackBusy}
                    onClick={() => handleMoveTrack(idx, 'down')}
                    title="아래로"
                  >
                    {'\u25BC'}
                  </button>
                  <button
                    type="button"
                    className={styles.removeBtn}
                    disabled={trackBusy}
                    onClick={() => handleRemoveTrack(t.trackId)}
                    title="제거"
                  >
                    {'\u2715'}
                  </button>
                </div>
              </li>
            ))}
          </ul>
        )}
      </section>
    </div>
  );
}

import { useState, useEffect, useCallback, useMemo, useRef } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { fetchTracks, type TrackListParams } from '@/api/tracks';
import { fetchTags, fetchAvailableTags } from '@/api/tags';
import {
  createDownloadFallbackFileName,
  downloadTrack,
  fetchDownloadCount,
  triggerBlobDownload,
} from '@/api/downloads';
import { getApiErrorCode } from '@/api/client';
import { classifyLoadError, getLoadErrorMessage } from '@/api/loadError';
import type { TrackListItem, TagItem, PageInfo, TagType } from '@/types';
import TrackRow from '@/components/track/TrackRow';
import FilterChip from '@/components/ui/FilterChip';
import TagFilterModal, { type TagFilterOption } from '@/components/filter/TagFilterModal';
import AddToPlaylistModal from '@/components/playlist/AddToPlaylistModal';
import Pagination from '@/components/ui/Pagination';
import { usePlayerStore } from '@/store/playerStore';
import { useLikeStore } from '@/store/likeStore';
import { useAuthStore } from '@/store/authStore';
import { useToastStore } from '@/store/toastStore';
import { toPlayableTrack } from '@/utils/playableTrack';
import { formatTagNameForDisplay } from '@/utils/tagName';
import {
  getCatalogTotalPages,
  normalizeCatalogPage,
  PUBLIC_CATALOG_PAGE_SIZE,
} from '@/utils/catalogPagination';
import styles from './TrackListPage.module.css';

/* ── BPM filter presets ── */
const BPM_PRESETS: readonly { label: string; min: number | undefined; max: number | undefined }[] =
  [
    { label: '~ 59', min: undefined, max: 59 },
    { label: '60 \u2013 79', min: 60, max: 79 },
    { label: '80 \u2013 99', min: 80, max: 99 },
    { label: '100 \u2013 119', min: 100, max: 119 },
    { label: '120 \u2013 139', min: 120, max: 139 },
    { label: '140 ~', min: 140, max: undefined },
  ];

const TAXONOMY_TYPES: readonly TagType[] = ['GENRE', 'MOOD', 'INSTRUMENT', 'USAGE'];
const TAXONOMY_LABELS: Record<TagType, string> = {
  GENRE: '장르',
  MOOD: '분위기',
  INSTRUMENT: '악기',
  USAGE: '용도',
};

type TaxonomyStatus = 'loading' | 'ready' | 'error';

interface TaxonomyEntry {
  tags: TagItem[];
  status: TaxonomyStatus;
}

type TaxonomyState = Record<TagType, TaxonomyEntry>;

function createInitialTaxonomyState(): TaxonomyState {
  return {
    GENRE: { tags: [], status: 'loading' },
    MOOD: { tags: [], status: 'loading' },
    INSTRUMENT: { tags: [], status: 'loading' },
    USAGE: { tags: [], status: 'loading' },
  };
}

function createTaxonomyRequestGenerations(): Record<TagType, number> {
  return { GENRE: 0, MOOD: 0, INSTRUMENT: 0, USAGE: 0 };
}

function createTaxonomyRequestTokens(): Record<TagType, number | null> {
  return { GENRE: null, MOOD: null, INSTRUMENT: null, USAGE: null };
}

function mergeTaxonomyOptions(
  tags: TagItem[],
  activeValues: string[],
  type: TagType,
): TagFilterOption[] {
  const activeNames = new Set(activeValues);
  const seenNames = new Set<string>();
  const options: TagFilterOption[] = [];

  for (const tag of tags) {
    if (tag.type !== type || seenNames.has(tag.name)) continue;
    seenNames.add(tag.name);
    options.push({
      key: `tag:${type}:${tag.id}:${tag.name.length}:${tag.name}`,
      name: tag.name,
      type,
    });
  }

  options.sort(
    (first, second) => Number(!activeNames.has(first.name)) - Number(!activeNames.has(second.name)),
  );

  for (const name of activeValues) {
    if (seenNames.has(name)) continue;
    seenNames.add(name);
    options.push({ key: `url:${type}:${name.length}:${name}`, name, type });
  }

  return options;
}

function TaxonomyLoadState({
  type,
  status,
  onRetry,
}: {
  type: TagType;
  status: TaxonomyStatus;
  onRetry: (type: TagType) => void;
}) {
  const label = TAXONOMY_LABELS[type];
  if (status === 'ready') return null;
  if (status === 'loading') {
    return (
      <span className={styles.taxonomyStatus} role="status">
        {`${label} 불러오는 중`}
      </span>
    );
  }
  return (
    <span className={styles.taxonomyRequestState}>
      <span className={styles.taxonomyError} role="alert">
        {`${label} 태그를 불러오지 못했습니다.`}
      </span>
      <button
        aria-label={`${label} 태그 다시 시도`}
        className={styles.taxonomyRetryButton}
        onClick={() => onRetry(type)}
        type="button"
      >
        {'다시 시도'}
      </button>
    </span>
  );
}

function tagValuesKey(values: string[]) {
  return JSON.stringify(values);
}

function tagValuesFromKey(key: string): string[] {
  return JSON.parse(key) as string[];
}

export default function TrackListPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const latestSearchParamsRef = useRef(searchParams);
  latestSearchParamsRef.current = searchParams;

  /* ── State ── */
  const [tracks, setTracks] = useState<TrackListItem[]>([]);
  const [pageInfo, setPageInfo] = useState<PageInfo | null>(null);
  const [taxonomies, setTaxonomies] = useState<TaxonomyState>(createInitialTaxonomyState);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const trackRequestGenerationRef = useRef(0);
  const trackRequestControllerRef = useRef<AbortController | null>(null);
  const trackRetryInFlightRef = useRef(false);
  const pendingDownloadIDsRef = useRef(new Set<number>());
  const [pendingDownloadIDs, setPendingDownloadIDs] = useState<Set<number>>(new Set());
  const availableTagsRequestGenerationRef = useRef(0);
  const [availableTagsError, setAvailableTagsError] = useState(false);
  const [availableTagsRetryVersion, setAvailableTagsRetryVersion] = useState(0);
  const taxonomyRequestGenerationRef = useRef(createTaxonomyRequestGenerations());
  const taxonomyRequestTokenRef = useRef(createTaxonomyRequestTokens());

  const toast = useToastStore((s) => s.show);
  const navigate = useNavigate();

  function handleGuestAction() {
    toast('warning', '로그인이 필요한 기능입니다.');
    navigate('/login');
  }

  function claimDownload(trackId: number): boolean {
    if (pendingDownloadIDsRef.current.has(trackId)) return false;
    pendingDownloadIDsRef.current.add(trackId);
    setPendingDownloadIDs(new Set(pendingDownloadIDsRef.current));
    return true;
  }

  function releaseDownload(trackId: number) {
    pendingDownloadIDsRef.current.delete(trackId);
    setPendingDownloadIDs(new Set(pendingDownloadIDsRef.current));
  }

  /* ── Derive filters from URL ── */
  const rawPage = searchParams.get('page');
  const currentPage = normalizeCatalogPage(rawPage);
  const pageNeedsNormalization = rawPage !== null && rawPage !== String(currentPage);
  const activeKeyword = searchParams.get('keyword') ?? '';
  const activeGenres = searchParams.getAll('genre');
  const activeMoods = searchParams.getAll('mood');
  const activeInstruments = searchParams.getAll('instrument');
  const activeUsages = searchParams.getAll('usage');
  const activeGenresKey = tagValuesKey(activeGenres);
  const activeMoodsKey = tagValuesKey(activeMoods);
  const activeInstrumentsKey = tagValuesKey(activeInstruments);
  const activeUsagesKey = tagValuesKey(activeUsages);
  const activeBpmLabel = searchParams.get('bpm') ?? '';
  const sortValue = (searchParams.get('sort') ?? 'latest') as
    | 'latest'
    | 'popular'
    | 'likes'
    | 'downloads';
  const trackRequestKey = [
    currentPage,
    sortValue,
    activeKeyword,
    activeGenresKey,
    activeMoodsKey,
    activeInstrumentsKey,
    activeUsagesKey,
    activeBpmLabel,
  ].join('\u001f');
  const latestTrackRequestKeyRef = useRef(trackRequestKey);
  latestTrackRequestKeyRef.current = trackRequestKey;

  useEffect(() => {
    if (rawPage === null || rawPage === String(currentPage)) return;
    const next = new URLSearchParams(searchParams);
    next.set('page', String(currentPage));
    setSearchParams(next, { replace: true });
  }, [currentPage, rawPage, searchParams, setSearchParams]);
  const genreTags = taxonomies.GENRE.tags;
  const moodTags = taxonomies.MOOD.tags;
  const instrumentTags = taxonomies.INSTRUMENT.tags;
  const usageTags = taxonomies.USAGE.tags;
  const visibleGenreTags = useMemo(
    () => mergeTaxonomyOptions(genreTags, tagValuesFromKey(activeGenresKey), 'GENRE'),
    [activeGenresKey, genreTags],
  );
  const visibleMoodTags = useMemo(
    () => mergeTaxonomyOptions(moodTags, tagValuesFromKey(activeMoodsKey), 'MOOD'),
    [activeMoodsKey, moodTags],
  );
  const visibleInstrumentTags = useMemo(
    () =>
      mergeTaxonomyOptions(instrumentTags, tagValuesFromKey(activeInstrumentsKey), 'INSTRUMENT'),
    [activeInstrumentsKey, instrumentTags],
  );
  const visibleUsageTags = useMemo(
    () => mergeTaxonomyOptions(usageTags, tagValuesFromKey(activeUsagesKey), 'USAGE'),
    [activeUsagesKey, usageTags],
  );

  /* Player store for playing state */
  const currentTrack = usePlayerStore((s) => s.currentTrack);
  const isPlaying = usePlayerStore((s) => s.isPlaying);
  const playTrack = usePlayerStore((s) => s.play);
  const pauseTrack = usePlayerStore((s) => s.pause);
  const resumeTrack = usePlayerStore((s) => s.resume);
  const setTrackListContext = usePlayerStore((s) => s.setTrackListContext);

  /* SR-83: Publish the currently visible track list as player context.
     The Next/Prev buttons and keyboard ↓/↑ both read from this context. */
  useEffect(() => {
    return setTrackListContext(tracks.map((track) => toPlayableTrack(track)));
  }, [tracks, setTrackListContext]);

  /* Like store */
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated());
  const likeLoaded = useLikeStore((s) => s.loaded);
  const loadLikes = useLikeStore((s) => s.load);
  const likedIds = useLikeStore((s) => s.likedIds);
  const toggleLike = useLikeStore((s) => s.toggle);
  const [addToPlTrackId, setAddToPlTrackId] = useState<number | null>(null);
  const [filterModalOpen, setFilterModalOpen] = useState(false);
  const [genreExpanded, setGenreExpanded] = useState(false);
  const [moodExpanded, setMoodExpanded] = useState(false);
  const [instrumentExpanded, setInstrumentExpanded] = useState(false);
  const [usageExpanded, setUsageExpanded] = useState(false);
  const hasActiveFilters =
    activeGenres.length > 0 ||
    activeMoods.length > 0 ||
    activeInstruments.length > 0 ||
    activeUsages.length > 0 ||
    activeBpmLabel !== '';
  const [availableGenres, setAvailableGenres] = useState<Set<string>>(new Set());
  const [availableInstruments, setAvailableInstruments] = useState<Set<string>>(new Set());
  const [availableUsages, setAvailableUsages] = useState<Set<string>>(new Set());

  useEffect(() => {
    if (isAuthenticated && !likeLoaded) {
      void loadLikes();
    }
  }, [isAuthenticated, likeLoaded, loadLikes]);

  const loadTaxonomy = useCallback(async (type: TagType) => {
    const requestGeneration = ++taxonomyRequestGenerationRef.current[type];
    taxonomyRequestTokenRef.current[type] = requestGeneration;
    setTaxonomies((current) => ({
      ...current,
      [type]: { ...current[type], status: 'loading' },
    }));

    try {
      const tags = await fetchTags(type);
      if (taxonomyRequestGenerationRef.current[type] !== requestGeneration) return;
      setTaxonomies((current) => ({
        ...current,
        [type]: { tags, status: 'ready' },
      }));
    } catch {
      if (taxonomyRequestGenerationRef.current[type] !== requestGeneration) return;
      setTaxonomies((current) => ({
        ...current,
        [type]: { ...current[type], status: 'error' },
      }));
    } finally {
      if (taxonomyRequestTokenRef.current[type] === requestGeneration) {
        taxonomyRequestTokenRef.current[type] = null;
      }
    }
  }, []);

  /* ── Load each taxonomy independently ── */
  useEffect(() => {
    const requestGenerations = taxonomyRequestGenerationRef.current;
    const requestTokens = taxonomyRequestTokenRef.current;
    TAXONOMY_TYPES.forEach((type) => void loadTaxonomy(type));
    return () => {
      TAXONOMY_TYPES.forEach((type) => {
        requestGenerations[type] += 1;
        requestTokens[type] = null;
      });
    };
  }, [loadTaxonomy]);

  const retryTaxonomy = useCallback(
    (type: TagType) => {
      if (taxonomyRequestTokenRef.current[type] !== null) return;
      void loadTaxonomy(type);
    },
    [loadTaxonomy],
  );

  /* ── Load tracks when filters/page change ── */
  const loadTracks = useCallback(async () => {
    trackRequestControllerRef.current?.abort();
    const controller = new AbortController();
    trackRequestControllerRef.current = controller;
    const requestGeneration = ++trackRequestGenerationRef.current;
    const requestKey = trackRequestKey;
    const isCurrentRequest = () =>
      requestGeneration === trackRequestGenerationRef.current &&
      requestKey === latestTrackRequestKeyRef.current &&
      !controller.signal.aborted;

    setLoading(true);
    setError(null);

    const params: TrackListParams = {
      page: currentPage,
      size: PUBLIC_CATALOG_PAGE_SIZE,
      sort: sortValue,
    };

    if (activeKeyword) params.keyword = activeKeyword;
    if (activeGenresKey !== '[]') params.genre = tagValuesFromKey(activeGenresKey);
    if (activeMoodsKey !== '[]') params.mood = tagValuesFromKey(activeMoodsKey);
    if (activeInstrumentsKey !== '[]') {
      params.instrument = tagValuesFromKey(activeInstrumentsKey);
    }
    if (activeUsagesKey !== '[]') params.usage = tagValuesFromKey(activeUsagesKey);

    const bpmPreset = BPM_PRESETS.find((p) => p.label === activeBpmLabel);
    if (bpmPreset) {
      params.bpmMin = bpmPreset.min;
      params.bpmMax = bpmPreset.max;
    }

    try {
      const res = await fetchTracks(params, controller.signal);
      if (!isCurrentRequest()) {
        return;
      }
      const totalPages = getCatalogTotalPages(res.pageInfo.total, PUBLIC_CATALOG_PAGE_SIZE);
      if (currentPage > totalPages && res.dataList.length === 0) {
        const next = new URLSearchParams(latestSearchParamsRef.current);
        next.set('page', String(totalPages));
        setSearchParams(next, { replace: true });
        return;
      }
      setTracks(res.dataList);
      setPageInfo(res.pageInfo);
    } catch (err) {
      if (!isCurrentRequest()) {
        return;
      }
      if (classifyLoadError(err) === 'cancelled') {
        return;
      }
      setTracks([]);
      setPageInfo(null);
      setError(getLoadErrorMessage(err, '음원 목록'));
    } finally {
      if (isCurrentRequest()) {
        setLoading(false);
        trackRequestControllerRef.current = null;
      }
    }
  }, [
    currentPage,
    sortValue,
    activeKeyword,
    activeGenresKey,
    activeMoodsKey,
    activeInstrumentsKey,
    activeUsagesKey,
    activeBpmLabel,
    trackRequestKey,
    setSearchParams,
  ]);

  useEffect(() => {
    if (pageNeedsNormalization) return;
    void loadTracks();
    return () => {
      trackRequestControllerRef.current?.abort();
      trackRequestControllerRef.current = null;
      trackRequestGenerationRef.current += 1;
    };
  }, [loadTracks, pageNeedsNormalization]);

  const retryLoadTracks = useCallback(() => {
    if (trackRetryInFlightRef.current || loading) {
      return;
    }
    trackRetryInFlightRef.current = true;
    void loadTracks().finally(() => {
      trackRetryInFlightRef.current = false;
    });
  }, [loadTracks, loading]);

  /* ── Tag recombination: fetch available tags when filters change ── */
  useEffect(() => {
    const requestGeneration = ++availableTagsRequestGenerationRef.current;
    const showAllTags = () => {
      setAvailableGenres(new Set());
      setAvailableInstruments(new Set());
      setAvailableUsages(new Set());
    };

    showAllTags();
    setAvailableTagsError(false);
    if (!hasActiveFilters) {
      return;
    }
    const controller = new AbortController();
    const bpmPreset = BPM_PRESETS.find((p) => p.label === activeBpmLabel);
    fetchAvailableTags(
      {
        genre: activeGenresKey === '[]' ? undefined : tagValuesFromKey(activeGenresKey),
        mood: activeMoodsKey === '[]' ? undefined : tagValuesFromKey(activeMoodsKey),
        instrument:
          activeInstrumentsKey === '[]' ? undefined : tagValuesFromKey(activeInstrumentsKey),
        usage: activeUsagesKey === '[]' ? undefined : tagValuesFromKey(activeUsagesKey),
        bpmMin: bpmPreset?.min,
        bpmMax: bpmPreset?.max,
      },
      controller.signal,
    )
      .then((tags) => {
        if (
          controller.signal.aborted ||
          requestGeneration !== availableTagsRequestGenerationRef.current
        ) {
          return;
        }
        setAvailableGenres(new Set(tags.filter((t) => t.type === 'GENRE').map((t) => t.name)));
        setAvailableInstruments(
          new Set(tags.filter((t) => t.type === 'INSTRUMENT').map((t) => t.name)),
        );
        setAvailableUsages(new Set(tags.filter((t) => t.type === 'USAGE').map((t) => t.name)));
      })
      .catch((requestError: unknown) => {
        if (
          controller.signal.aborted ||
          requestGeneration !== availableTagsRequestGenerationRef.current ||
          classifyLoadError(requestError) === 'cancelled'
        ) {
          return;
        }
        showAllTags();
        setAvailableTagsError(true);
      });

    return () => {
      controller.abort();
    };
  }, [
    activeGenresKey,
    activeMoodsKey,
    activeInstrumentsKey,
    activeUsagesKey,
    activeBpmLabel,
    hasActiveFilters,
    availableTagsRetryVersion,
  ]);

  /* ── Filter helpers ── */
  function setFilter(key: string, value: string) {
    const next = new URLSearchParams(searchParams);
    if (value) {
      next.set(key, value);
    } else {
      next.delete(key);
    }
    next.set('page', '1');
    setSearchParams(next);
  }

  function toggleGenre(name: string) {
    const next = new URLSearchParams(searchParams);
    next.delete('genre');
    const updated = activeGenres.includes(name)
      ? activeGenres.filter((g) => g !== name)
      : [...activeGenres, name];
    updated.forEach((g) => next.append('genre', g));
    next.set('page', '1');
    setSearchParams(next);
  }

  function toggleMood(name: string) {
    const next = new URLSearchParams(searchParams);
    next.delete('mood');
    next.delete('page');
    const updated = activeMoods.includes(name)
      ? activeMoods.filter((m) => m !== name)
      : [...activeMoods, name];
    updated.forEach((m) => next.append('mood', m));
    next.set('page', '1');
    setSearchParams(next);
  }

  function toggleInstrument(name: string) {
    const next = new URLSearchParams(searchParams);
    next.delete('instrument');
    const updated = activeInstruments.includes(name)
      ? activeInstruments.filter((instrument) => instrument !== name)
      : [...activeInstruments, name];
    updated.forEach((instrument) => next.append('instrument', instrument));
    next.set('page', '1');
    setSearchParams(next);
  }

  function toggleUsage(name: string) {
    const next = new URLSearchParams(searchParams);
    next.delete('usage');
    const updated = activeUsages.includes(name)
      ? activeUsages.filter((u) => u !== name)
      : [...activeUsages, name];
    updated.forEach((u) => next.append('usage', u));
    next.set('page', '1');
    setSearchParams(next);
  }

  function toggleBpm(label: string) {
    setFilter('bpm', activeBpmLabel === label ? '' : label);
  }

  function handleSortChange(e: React.ChangeEvent<HTMLSelectElement>) {
    setFilter('sort', e.target.value);
  }

  function handleFilterApply(
    genres: string[],
    moods: string[],
    instruments: string[],
    usages: string[],
    bpm: string,
  ) {
    const next = new URLSearchParams(searchParams);
    next.delete('genre');
    genres.forEach((g) => next.append('genre', g));
    next.delete('mood');
    moods.forEach((m) => next.append('mood', m));
    next.delete('instrument');
    instruments.forEach((instrument) => next.append('instrument', instrument));
    next.delete('usage');
    usages.forEach((u) => next.append('usage', u));
    if (bpm) {
      next.set('bpm', bpm);
    } else {
      next.delete('bpm');
    }
    next.set('page', '1');
    setSearchParams(next);
  }

  function goToPage(page: number) {
    const next = new URLSearchParams(searchParams);
    next.set('page', String(page));
    setSearchParams(next);
  }

  /* ── Render ── */
  return (
    <div className={styles.page}>
      {/* Page Header */}
      <div className={styles.pageHeader}>
        <div className={styles.pageTitle}>
          {'음원'}
          {pageInfo && (
            <span className={styles.pageTitleCount}>
              {`총 ${pageInfo.total.toLocaleString()}곡`}
            </span>
          )}
        </div>
        <div className={styles.sortBar}>
          <span className={styles.sortLabel}>{'정렬'}</span>
          <select className={styles.sortSelect} value={sortValue} onChange={handleSortChange}>
            <option value="latest">{'최신순'}</option>
            <option value="popular">{'인기순'}</option>
            <option value="likes">{'좋아요순'}</option>
            <option value="downloads">{'다운로드순'}</option>
          </select>
        </div>
      </div>

      {/* Active keyword */}
      {activeKeyword && (
        <div className={styles.keywordBar}>
          <span>{`"${activeKeyword}" 검색 결과`}</span>
          <button
            className={styles.keywordClear}
            onClick={() => {
              const next = new URLSearchParams(searchParams);
              next.delete('keyword');
              next.set('page', '1');
              setSearchParams(next);
            }}
          >
            {'검색 해제'}
          </button>
        </div>
      )}

      {/* Filter Bar */}
      <div className={styles.filterBar}>
        {availableTagsError && (
          <div className={styles.taxonomyRequestState} role="alert">
            <span className={styles.taxonomyError}>
              선택한 필터에 따른 사용 가능 태그를 확인하지 못했습니다.
            </span>
            <button
              aria-label="사용 가능 태그 다시 확인"
              className={styles.taxonomyRetryButton}
              onClick={() => setAvailableTagsRetryVersion((version) => version + 1)}
              type="button"
            >
              다시 시도
            </button>
          </div>
        )}
        {/* Genre row */}
        <div className={`${styles.filterRow} ${genreExpanded ? styles.filterRowExpanded : ''}`}>
          <span className={styles.filterLabel}>{'장르'}</span>
          <div className={styles.filterChips}>
            <FilterChip
              label={'전체'}
              active={activeGenres.length === 0}
              onClick={() => {
                const next = new URLSearchParams(searchParams);
                next.delete('genre');
                next.set('page', '1');
                setSearchParams(next);
              }}
            />
            {visibleGenreTags
              .filter(
                (tag) =>
                  activeGenres.includes(tag.name) ||
                  availableGenres.size === 0 ||
                  availableGenres.has(tag.name),
              )
              .map((tag) => (
                <FilterChip
                  key={tag.key}
                  label={tag.name}
                  active={activeGenres.includes(tag.name)}
                  onClick={() => toggleGenre(tag.name)}
                />
              ))}
          </div>
          <TaxonomyLoadState
            type="GENRE"
            status={taxonomies.GENRE.status}
            onRetry={retryTaxonomy}
          />
          {visibleGenreTags.length > 6 && (
            <button className={styles.expandBtn} onClick={() => setGenreExpanded((v) => !v)}>
              {genreExpanded ? '\u25B2 접기' : '\u25BC 펼치기'}
            </button>
          )}
        </div>

        {/* Mood row */}
        <div className={`${styles.filterRow} ${moodExpanded ? styles.filterRowExpanded : ''}`}>
          <span className={styles.filterLabel}>{'분위기'}</span>
          <div className={styles.filterChips}>
            {visibleMoodTags.map((tag) => (
              <FilterChip
                key={tag.key}
                label={tag.name}
                active={activeMoods.includes(tag.name)}
                onClick={() => toggleMood(tag.name)}
              />
            ))}
          </div>
          <TaxonomyLoadState type="MOOD" status={taxonomies.MOOD.status} onRetry={retryTaxonomy} />
          {visibleMoodTags.length > 6 && (
            <button className={styles.expandBtn} onClick={() => setMoodExpanded((v) => !v)}>
              {moodExpanded ? '\u25B2 접기' : '\u25BC 펼치기'}
            </button>
          )}
        </div>

        {/* Instrument row */}
        <div
          className={`${styles.filterRow} ${instrumentExpanded ? styles.filterRowExpanded : ''}`}
        >
          <span className={styles.filterLabel}>{'악기'}</span>
          <div className={styles.filterChips}>
            {visibleInstrumentTags
              .filter(
                (tag) =>
                  activeInstruments.includes(tag.name) ||
                  availableInstruments.size === 0 ||
                  availableInstruments.has(tag.name),
              )
              .map((tag) => (
                <FilterChip
                  key={tag.key}
                  label={tag.name}
                  active={activeInstruments.includes(tag.name)}
                  onClick={() => toggleInstrument(tag.name)}
                />
              ))}
          </div>
          <TaxonomyLoadState
            type="INSTRUMENT"
            status={taxonomies.INSTRUMENT.status}
            onRetry={retryTaxonomy}
          />
          {visibleInstrumentTags.length > 6 && (
            <button
              className={styles.expandBtn}
              onClick={() => setInstrumentExpanded((value) => !value)}
              type="button"
            >
              {instrumentExpanded ? '\u25B2 접기' : '\u25BC 펼치기'}
            </button>
          )}
        </div>

        {/* Usage row */}
        <div className={`${styles.filterRow} ${usageExpanded ? styles.filterRowExpanded : ''}`}>
          <span className={styles.filterLabel}>{'용도'}</span>
          <div className={styles.filterChips}>
            {visibleUsageTags
              .filter(
                (tag) =>
                  activeUsages.includes(tag.name) ||
                  availableUsages.size === 0 ||
                  availableUsages.has(tag.name),
              )
              .map((tag) => (
                <FilterChip
                  key={tag.key}
                  label={formatTagNameForDisplay(tag.name, tag.type)}
                  active={activeUsages.includes(tag.name)}
                  onClick={() => toggleUsage(tag.name)}
                />
              ))}
          </div>
          <TaxonomyLoadState
            type="USAGE"
            status={taxonomies.USAGE.status}
            onRetry={retryTaxonomy}
          />
          {visibleUsageTags.length > 6 && (
            <button className={styles.expandBtn} onClick={() => setUsageExpanded((v) => !v)}>
              {usageExpanded ? '\u25B2 접기' : '\u25BC 펼치기'}
            </button>
          )}
        </div>

        {/* BPM row + reset */}
        <div className={styles.filterRow}>
          <span className={styles.filterLabel}>BPM</span>
          <div className={styles.filterChips}>
            {BPM_PRESETS.map((preset) => (
              <FilterChip
                key={preset.label}
                label={preset.label}
                active={activeBpmLabel === preset.label}
                onClick={() => toggleBpm(preset.label)}
              />
            ))}
          </div>
          <button
            className={styles.filterSearchBtn}
            onClick={() => setFilterModalOpen(true)}
            type="button"
          >
            {'전체 필터'}
          </button>
          {hasActiveFilters && (
            <button
              className={styles.resetBtn}
              onClick={() => {
                const next = new URLSearchParams(searchParams);
                next.delete('genre');
                next.delete('mood');
                next.delete('instrument');
                next.delete('usage');
                next.delete('bpm');
                next.set('page', '1');
                setSearchParams(next);
              }}
            >
              {'초기화'}
            </button>
          )}
        </div>
      </div>

      {/* Tag Filter Modal */}
      <TagFilterModal
        open={filterModalOpen}
        onClose={() => setFilterModalOpen(false)}
        genreTags={visibleGenreTags}
        moodTags={visibleMoodTags}
        instrumentTags={visibleInstrumentTags}
        usageTags={visibleUsageTags}
        activeGenres={activeGenres}
        activeMoods={activeMoods}
        activeInstruments={activeInstruments}
        activeUsages={activeUsages}
        activeBpmLabel={activeBpmLabel}
        bpmPresets={BPM_PRESETS}
        onApply={handleFilterApply}
      />

      {/* Track Table */}
      {loading ? (
        <div className={styles.loading}>{'음원 목록을 불러오는 중...'}</div>
      ) : error ? (
        <div className={styles.error} role="alert">
          <p className={styles.errorMessage}>{error}</p>
          <button
            className={styles.retryButton}
            disabled={loading}
            onClick={retryLoadTracks}
            type="button"
          >
            {'다시 시도'}
          </button>
        </div>
      ) : tracks.length === 0 ? (
        <div className={styles.empty}>{'검색 결과가 없습니다.'}</div>
      ) : (
        <>
          <div className={styles.tableWrap}>
            <table className={styles.trackTable}>
              <thead>
                <tr>
                  <th className={`${styles.thCenter} ${styles.thNum}`}>#</th>
                  <th>{'음원'}</th>
                  <th className={styles.thTag}>{'장르 / 태그'}</th>
                  <th className={`${styles.thRight} ${styles.thBpm}`}>BPM</th>
                  <th className={`${styles.thCenter} ${styles.thKey}`}>{'조성'}</th>
                  <th className={`${styles.thRight} ${styles.thDur}`}>{'길이'}</th>
                  <th className={`${styles.thRight} ${styles.thActs}`}>{'액션'}</th>
                </tr>
              </thead>
              <tbody>
                {tracks.map((track, idx) => (
                  <TrackRow
                    key={track.id}
                    index={(currentPage - 1) * PUBLIC_CATALOG_PAGE_SIZE + idx + 1}
                    track={track}
                    playing={currentTrack?.id === track.id && isPlaying}
                    liked={likedIds.has(track.id)}
                    showAuthActions={isAuthenticated}
                    onGuestAction={handleGuestAction}
                    onPlay={(t) => {
                      if (currentTrack?.id === t.id) {
                        if (isPlaying) pauseTrack();
                        else resumeTrack();
                      } else {
                        playTrack(toPlayableTrack(t));
                      }
                    }}
                    onLike={(t) => toggleLike(t.id)}
                    onAddToPlaylist={(t) => setAddToPlTrackId(t.id)}
                    onDownload={async (t) => {
                      if (!claimDownload(t.id)) return;
                      try {
                        const download = await downloadTrack(
                          t.id,
                          createDownloadFallbackFileName('track', t.id, t.title, 'mp3'),
                        );
                        triggerBlobDownload(download);
                        try {
                          const count = await fetchDownloadCount();
                          toast(
                            'success',
                            `다운로드 완료! 오늘 남은 횟수: ${count.remaining}/${count.dailyLimit}`,
                          );
                        } catch {
                          toast('success', '다운로드가 완료되었습니다.');
                        }
                      } catch (err: unknown) {
                        const code = await getApiErrorCode(err);
                        if (code === 'NO_ACTIVE_SUBSCRIPTION') {
                          toast('warning', '구독이 필요한 기능입니다.');
                          navigate('/subscriptions');
                        } else if (code === 'DOWNLOAD_LIMIT_EXCEEDED') {
                          toast('warning', '금일 다운로드 횟수를 모두 사용했습니다.');
                        } else {
                          toast('error', '다운로드에 실패했습니다.');
                        }
                      } finally {
                        releaseDownload(t.id);
                      }
                    }}
                    downloadPending={pendingDownloadIDs.has(track.id)}
                  />
                ))}
              </tbody>
            </table>
          </div>

          {pageInfo && (
            <Pagination pageInfo={pageInfo} currentPage={currentPage} onPageChange={goToPage} />
          )}
        </>
      )}

      <AddToPlaylistModal
        open={addToPlTrackId !== null}
        trackId={addToPlTrackId}
        onClose={() => setAddToPlTrackId(null)}
        onSubscriptionRequired={() => {
          setAddToPlTrackId(null);
          toast('warning', '구독이 필요한 기능입니다.');
          navigate('/subscriptions');
        }}
      />
    </div>
  );
}

import { useState, useEffect, useCallback, useRef } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { fetchTracks, type TrackListParams } from '@/api/tracks';
import { fetchTags } from '@/api/tags';
import { addToDownloadQueue } from '@/api/downloadQueue';
import { downloadTrack, triggerBlobDownload } from '@/api/downloads';
import { isSubscriptionRequired, getApiErrorCode } from '@/api/client';
import { fetchDownloadCount } from '@/api/downloads';
import type { TrackListItem, TagItem, PageInfo } from '@/types';
import TrackRow from '@/components/track/TrackRow';
import FilterChip from '@/components/ui/FilterChip';
import TagFilterModal from '@/components/filter/TagFilterModal';
import AddToPlaylistModal from '@/components/playlist/AddToPlaylistModal';
import Pagination from '@/components/ui/Pagination';
import { usePlayerStore } from '@/store/playerStore';
import { useLikeStore } from '@/store/likeStore';
import { useAuthStore } from '@/store/authStore';
import { useToastStore } from '@/store/toastStore';
import styles from './TrackListPage.module.css';

/* ── BPM filter presets ── */
const BPM_PRESETS = [
  { label: '~ 80', min: undefined, max: 80 },
  { label: '80 \u2013 120', min: 80, max: 120 },
  { label: '120 ~', min: 120, max: undefined },
] as const;

const PAGE_SIZE = 20;

export default function TrackListPage() {
  const [searchParams, setSearchParams] = useSearchParams();

  /* ── State ── */
  const [tracks, setTracks] = useState<TrackListItem[]>([]);
  const [pageInfo, setPageInfo] = useState<PageInfo | null>(null);
  const [genreTags, setGenreTags] = useState<TagItem[]>([]);
  const [moodTags, setMoodTags] = useState<TagItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const toast = useToastStore((s) => s.show);
  const navigate = useNavigate();

  function handleGuestAction() {
    toast('warning', '로그인이 필요한 기능입니다.');
    navigate('/login');
  }

  /* ── Derive filters from URL ── */
  const currentPage = Number(searchParams.get('page') ?? '1');
  const activeKeyword = searchParams.get('keyword') ?? '';
  const activeGenres = searchParams.getAll('genre');
  const activeMoods = searchParams.getAll('mood');
  const activeBpmLabel = searchParams.get('bpm') ?? '';
  const sortValue = (searchParams.get('sort') ?? 'latest') as 'latest' | 'popular' | 'likes' | 'downloads';

  /* Player store for playing state */
  const currentTrack = usePlayerStore((s) => s.currentTrack);
  const isPlaying = usePlayerStore((s) => s.isPlaying);
  const playTrack = usePlayerStore((s) => s.play);
  const pauseTrack = usePlayerStore((s) => s.pause);
  const resumeTrack = usePlayerStore((s) => s.resume);

  /* ── Keyboard ↑↓ navigation ── */
  const tracksRef = useRef(tracks);
  tracksRef.current = tracks;

  useEffect(() => {
    function handleKeyDown(e: KeyboardEvent) {
      if (e.key !== 'ArrowUp' && e.key !== 'ArrowDown') return;
      const tag = (e.target as HTMLElement)?.tagName;
      if (tag === 'INPUT' || tag === 'TEXTAREA' || tag === 'SELECT') return;

      const list = tracksRef.current;
      if (list.length === 0) return;

      const cur = usePlayerStore.getState().currentTrack;
      const curIdx = cur ? list.findIndex((t) => t.id === cur.id) : -1;

      let nextIdx: number;
      if (e.key === 'ArrowDown') {
        nextIdx = curIdx < 0 ? 0 : Math.min(curIdx + 1, list.length - 1);
      } else {
        nextIdx = curIdx <= 0 ? 0 : curIdx - 1;
      }

      if (nextIdx === curIdx && curIdx >= 0) return;
      e.preventDefault();

      const t = list[nextIdx];
      usePlayerStore.getState().play({
        id: t.id,
        title: t.title,
        artistName: t.artistName ?? '',
        duration: t.duration ?? 0,
        bpm: t.bpm,
        tonality: t.tonality,
        description: null,
        audioFile: null,
        thumbnail: t.thumbnail,
        tags: t.tags,
        isActive: true,
        playCount: t.playCount,
        likeCount: t.likeCount,
        downloadCount: t.downloadCount,
        createdAt: t.createdAt,
        updatedAt: t.createdAt,
      });
    }

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, []);

  /* Like store */
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated());
  const likeStore = useLikeStore();
  const [addToPlTrackId, setAddToPlTrackId] = useState<number | null>(null);
  const [filterModalOpen, setFilterModalOpen] = useState(false);

  useEffect(() => {
    if (isAuthenticated && !likeStore.loaded) {
      likeStore.load();
    }
  }, [isAuthenticated, likeStore.loaded]);


  /* ── Load tags once ── */
  useEffect(() => {
    let cancelled = false;

    async function loadTags() {
      try {
        const [genres, moods] = await Promise.all([
          fetchTags('GENRE'),
          fetchTags('MOOD'),
        ]);
        if (!cancelled) {
          setGenreTags(genres);
          setMoodTags(moods);
        }
      } catch {
        /* tags are supplementary, ignore errors */
      }
    }

    loadTags();
    return () => {
      cancelled = true;
    };
  }, []);

  /* ── Load tracks when filters/page change ── */
  const loadTracks = useCallback(async () => {
    setLoading(true);
    setError(null);

    const params: TrackListParams = {
      page: currentPage,
      size: PAGE_SIZE,
      sort: sortValue,
    };

    if (activeKeyword) params.keyword = activeKeyword;
    if (activeGenres.length > 0) params.genre = activeGenres.join(',');
    if (activeMoods.length > 0) params.mood = activeMoods.join(',');

    const bpmPreset = BPM_PRESETS.find((p) => p.label === activeBpmLabel);
    if (bpmPreset) {
      params.bpmMin = bpmPreset.min;
      params.bpmMax = bpmPreset.max;
    }

    try {
      const res = await fetchTracks(params);
      setTracks(res.dataList);
      setPageInfo(res.pageInfo);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load tracks');
    } finally {
      setLoading(false);
    }
  }, [currentPage, sortValue, activeKeyword, activeGenres.join(','), activeMoods.join(','), activeBpmLabel]);

  useEffect(() => {
    loadTracks();
  }, [loadTracks]);

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

  function toggleBpm(label: string) {
    setFilter('bpm', activeBpmLabel === label ? '' : label);
  }

  function handleSortChange(e: React.ChangeEvent<HTMLSelectElement>) {
    setFilter('sort', e.target.value);
  }

  function handleFilterApply(genres: string[], moods: string[], bpm: string) {
    const next = new URLSearchParams(searchParams);
    next.delete('genre');
    genres.forEach((g) => next.append('genre', g));
    next.delete('mood');
    moods.forEach((m) => next.append('mood', m));
    if (bpm) {
      next.set('bpm', bpm);
    } else {
      next.delete('bpm');
    }
    next.set('page', '1');
    setSearchParams(next);
  }

  /* Sort tags: active ones first (CSS overflow hides the rest) */
  const sortedGenreTags = [...genreTags].sort((a, b) => {
    const aActive = activeGenres.includes(a.name) ? 0 : 1;
    const bActive = activeGenres.includes(b.name) ? 0 : 1;
    return aActive - bActive;
  });

  const sortedMoodTags = [...moodTags].sort((a, b) => {
    const aActive = activeMoods.includes(a.name) ? 0 : 1;
    const bActive = activeMoods.includes(b.name) ? 0 : 1;
    return aActive - bActive;
  });

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
          <select
            className={styles.sortSelect}
            value={sortValue}
            onChange={handleSortChange}
          >
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
        {/* Genre row */}
        <div className={styles.filterRow}>
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
            {sortedGenreTags.map((tag) => (
              <FilterChip
                key={tag.id}
                label={tag.name}
                active={activeGenres.includes(tag.name)}
                onClick={() => toggleGenre(tag.name)}
              />
            ))}
          </div>
        </div>

        {/* Mood row */}
        <div className={styles.filterRow}>
          <span className={styles.filterLabel}>{'분위기'}</span>
          <div className={styles.filterChips}>
            {sortedMoodTags.map((tag) => (
              <FilterChip
                key={tag.id}
                label={tag.name}
                active={activeMoods.includes(tag.name)}
                onClick={() => toggleMood(tag.name)}
              />
            ))}
          </div>
        </div>

        {/* BPM row + search */}
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
          >
            {'추가 옵션'}
          </button>
        </div>
      </div>

      {/* Tag Filter Modal */}
      <TagFilterModal
        open={filterModalOpen}
        onClose={() => setFilterModalOpen(false)}
        genreTags={genreTags}
        moodTags={moodTags}
        activeGenres={activeGenres}
        activeMoods={activeMoods}
        activeBpmLabel={activeBpmLabel}
        bpmPresets={BPM_PRESETS}
        onApply={handleFilterApply}
      />

      {/* Track Table */}
      {loading ? (
        <div className={styles.loading}>{'Loading...'}</div>
      ) : error ? (
        <div className={styles.error}>{error}</div>
      ) : tracks.length === 0 ? (
        <div className={styles.empty}>{'검색 결과가 없습니다.'}</div>
      ) : (
        <>
          <div className={styles.tableWrap}>
          <table className={styles.trackTable}>
            <thead>
              <tr>
                <th className={styles.thCenter}>#</th>
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
                  index={(currentPage - 1) * PAGE_SIZE + idx + 1}
                  track={track}
                  playing={currentTrack?.id === track.id && isPlaying}
                  liked={likeStore.likedIds.has(track.id)}
                  showAuthActions={isAuthenticated}
                  onGuestAction={handleGuestAction}
                  onPlay={(t) => {
                    if (currentTrack?.id === t.id) {
                      if (isPlaying) pauseTrack();
                      else resumeTrack();
                    } else {
                      playTrack({
                        id: t.id,
                        title: t.title,
                        artistName: t.artistName ?? '',
                        duration: t.duration ?? 0,
                        bpm: t.bpm,
                        tonality: t.tonality,
                        description: null,
                        audioFile: null,
                        thumbnail: t.thumbnail,
                        tags: t.tags,
                        isActive: true,
                        playCount: t.playCount,
                        likeCount: t.likeCount,
                        downloadCount: t.downloadCount,
                        createdAt: t.createdAt,
                        updatedAt: t.createdAt,
                      });
                    }
                  }}
                  onLike={(t) => likeStore.toggle(t.id)}
                  onAddToPlaylist={(t) => setAddToPlTrackId(t.id)}
                  onDownload={async (t) => {
                    try {
                      const blob = await downloadTrack(t.id);
                      triggerBlobDownload(blob, `${t.title}.mp3`);
                      try {
                        const count = await fetchDownloadCount();
                        toast('success', `다운로드 완료! 오늘 남은 횟수: ${count.remaining}/${count.dailyLimit}`);
                      } catch {
                        toast('success', '다운로드가 완료되었습니다.');
                      }
                    } catch (err) {
                      const code = await getApiErrorCode(err);
                      if (code === 'NO_ACTIVE_SUBSCRIPTION') {
                        toast('warning', '구독이 필요한 기능입니다.');
                        navigate('/subscriptions');
                      } else if (code === 'DOWNLOAD_LIMIT_EXCEEDED') {
                        toast('warning', '금일 다운로드 횟수를 모두 사용했습니다.');
                      } else {
                        toast('error', '다운로드에 실패했습니다.');
                      }
                    }
                  }}
                  onBuy={async (t) => {
                    try {
                      await addToDownloadQueue(t.id);
                      toast('success', '다운로드 대기열에 추가되었습니다.');
                    } catch (err: unknown) {
                      if (isSubscriptionRequired(err)) {
                        toast('warning', '구독이 필요한 기능입니다.');
                        navigate('/subscriptions');
                      } else {
                        const axErr = err as { response?: { status?: number } };
                        if (axErr.response?.status === 409) {
                          toast('error', '이미 대기열에 있는 음원입니다.');
                        } else {
                          toast('error', '대기열 추가에 실패했습니다.');
                        }
                      }
                    }
                  }}
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

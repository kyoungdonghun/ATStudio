import { useState, useEffect, useCallback } from 'react';
import { useSearchParams } from 'react-router-dom';
import { fetchTracks, type TrackListParams } from '@/api/tracks';
import { fetchTags } from '@/api/tags';
import { addToDownloadQueue } from '@/api/downloadQueue';
import { downloadTrack, triggerBlobDownload } from '@/api/downloads';
import type { TrackListItem, TagItem, PageInfo } from '@/types';
import TrackRow from '@/components/track/TrackRow';
import FilterChip from '@/components/ui/FilterChip';
import AddToPlaylistModal from '@/components/playlist/AddToPlaylistModal';
import Pagination from '@/components/ui/Pagination';
import { usePlayerStore } from '@/store/playerStore';
import { useLikeStore } from '@/store/likeStore';
import { useAuthStore } from '@/store/authStore';
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

  /* ── Derive filters from URL ── */
  const currentPage = Number(searchParams.get('page') ?? '1');
  const activeGenres = searchParams.getAll('genre');
  const activeMood = searchParams.get('mood') ?? '';
  const activeBpmLabel = searchParams.get('bpm') ?? '';
  const sortValue = (searchParams.get('sort') ?? 'latest') as 'latest' | 'popular';

  /* Player store for playing state */
  const currentTrack = usePlayerStore((s) => s.currentTrack);
  const playTrack = usePlayerStore((s) => s.play);

  /* Like store */
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated());
  const likeStore = useLikeStore();
  const [addToPlTrackId, setAddToPlTrackId] = useState<number | null>(null);

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

    if (activeGenres.length > 0) params.genre = activeGenres.join(',');
    if (activeMood) params.mood = activeMood;

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
  }, [currentPage, sortValue, activeGenres.join(','), activeMood, activeBpmLabel]);

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
    setFilter('mood', activeMood === name ? '' : name);
  }

  function toggleBpm(label: string) {
    setFilter('bpm', activeBpmLabel === label ? '' : label);
  }

  function handleSortChange(e: React.ChangeEvent<HTMLSelectElement>) {
    setFilter('sort', e.target.value);
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
          <select
            className={styles.sortSelect}
            value={sortValue}
            onChange={handleSortChange}
          >
            <option value="latest">{'최신순'}</option>
            <option value="popular">{'인기순'}</option>
          </select>
        </div>
      </div>

      {/* Filter Bar */}
      <div className={styles.filterBar}>
        {/* Genre */}
        <div className={styles.filterGroup}>
          <span className={styles.filterLabel}>{'장르'}</span>
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
          {genreTags.map((tag) => (
            <FilterChip
              key={tag.id}
              label={tag.name}
              active={activeGenres.includes(tag.name)}
              onClick={() => toggleGenre(tag.name)}
            />
          ))}
        </div>

        <div className={styles.filterDivider} />

        {/* Mood */}
        <div className={styles.filterGroup}>
          <span className={styles.filterLabel}>{'분위기'}</span>
          {moodTags.map((tag) => (
            <FilterChip
              key={tag.id}
              label={tag.name}
              active={activeMood === tag.name}
              onClick={() => toggleMood(tag.name)}
            />
          ))}
        </div>

        <div className={styles.filterDivider} />

        {/* BPM */}
        <div className={styles.filterGroup}>
          <span className={styles.filterLabel}>BPM</span>
          {BPM_PRESETS.map((preset) => (
            <FilterChip
              key={preset.label}
              label={preset.label}
              active={activeBpmLabel === preset.label}
              onClick={() => toggleBpm(preset.label)}
            />
          ))}
        </div>
      </div>

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
                <th>{'장르 / 태그'}</th>
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
                  playing={currentTrack?.id === track.id}
                  liked={likeStore.likedIds.has(track.id)}
                  onPlay={(t) => playTrack({
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
                    createdAt: t.createdAt,
                    updatedAt: t.createdAt,
                  })}
                  onLike={(t) => likeStore.toggle(t.id)}
                  onAddToPlaylist={(t) => setAddToPlTrackId(t.id)}
                  onDownload={async (t) => {
                    try {
                      const blob = await downloadTrack(t.id);
                      triggerBlobDownload(blob, `${t.title}.mp3`);
                    } catch {
                      alert('다운로드에 실패했습니다.');
                    }
                  }}
                  onBuy={async (t) => {
                    try {
                      await addToDownloadQueue(t.id);
                      alert('다운로드 대기열에 추가되었습니다.');
                    } catch {
                      alert('대기열 추가에 실패했습니다.');
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
      />
    </div>
  );
}

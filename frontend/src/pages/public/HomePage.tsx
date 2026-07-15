import { useState, useEffect, useRef, useCallback } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { fetchAlbums } from '@/api/albums';
import { fetchTracks } from '@/api/tracks';
import { fetchTags } from '@/api/tags';
import type { Album, TagItem, TrackListItem } from '@/types';
import AlbumCard from '@/components/album/AlbumCard';
import Button from '@/components/ui/Button';
import Tag from '@/components/ui/Tag';
import styles from './HomePage.module.css';

/** Number of albums in the new releases carousel */
const CAROUSEL_SIZE = 7;
/** Number of albums in the popular grid */
const GRID_SIZE = 12;
/** Number of tracks in the new tracks section */
const NEW_TRACKS_SIZE = 6;

function SkeletonCard() {
  return (
    <div className={styles.skeletonCard}>
      <div className={styles.skeletonThumb} />
      <div className={styles.skeletonText} />
      <div className={styles.skeletonMeta} />
    </div>
  );
}

export default function HomePage() {
  const navigate = useNavigate();

  /* ── State ── */
  const [newAlbums, setNewAlbums] = useState<Album[]>([]);
  const [popularAlbums, setPopularAlbums] = useState<Album[]>([]);
  const [newTracks, setNewTracks] = useState<TrackListItem[]>([]);
  const [genreTags, setGenreTags] = useState<TagItem[]>([]);
  const [moodTags, setMoodTags] = useState<TagItem[]>([]);
  const [selectedGenres, setSelectedGenres] = useState<Set<number>>(new Set());
  const [selectedMoods, setSelectedMoods] = useState<Set<number>>(new Set());
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  /* ── Data fetching ── */
  useEffect(() => {
    let cancelled = false;

    async function load() {
      try {
        setLoading(true);
        setError(null);

        const [newRes, popRes, trackRes, genreTagsRes, moodTagsRes] = await Promise.all([
          fetchAlbums({ page: 1, size: CAROUSEL_SIZE, sort: 'latest' }),
          fetchAlbums({ page: 1, size: GRID_SIZE, sort: 'popular' }),
          fetchTracks({ page: 1, size: NEW_TRACKS_SIZE, sort: 'latest' }),
          fetchTags('GENRE'),
          fetchTags('MOOD'),
        ]);

        if (cancelled) return;

        setNewAlbums(newRes.dataList ?? []);
        setPopularAlbums(popRes.dataList ?? []);
        setNewTracks(trackRes.dataList ?? []);
        setGenreTags(Array.isArray(genreTagsRes) ? genreTagsRes : []);
        setMoodTags(Array.isArray(moodTagsRes) ? moodTagsRes : []);
      } catch (err) {
        if (!cancelled) {
          setError(
            err instanceof Error ? err.message : 'Failed to load data',
          );
        }
      } finally {
        if (!cancelled) setLoading(false);
      }
    }

    load();
    return () => {
      cancelled = true;
    };
  }, []);

  /* ── Genre tag toggle ── */
  function toggleGenre(tagId: number) {
    setSelectedGenres((prev) => {
      const next = new Set(prev);
      if (next.has(tagId)) {
        next.delete(tagId);
      } else {
        next.add(tagId);
      }
      return next;
    });
  }

  /* ── Mood tag toggle ── */
  function toggleMood(tagId: number) {
    setSelectedMoods((prev) => {
      const next = new Set(prev);
      if (next.has(tagId)) {
        next.delete(tagId);
      } else {
        next.add(tagId);
      }
      return next;
    });
  }

  function handleGenreExplore() {
    const names = genreTags
      .filter((t) => selectedGenres.has(t.id))
      .map((t) => t.name);
    if (names.length > 0) {
      const params = new URLSearchParams();
      names.forEach((name) => params.append('genre', name));
      navigate(`/tracks?${params.toString()}`);
    } else {
      navigate('/tracks');
    }
  }

  function handleMoodExplore() {
    const names = moodTags
      .filter((t) => selectedMoods.has(t.id))
      .map((t) => t.name);
    if (names.length > 0) {
      const params = new URLSearchParams();
      names.forEach((name) => params.append('mood', name));
      navigate(`/tracks?${params.toString()}`);
    } else {
      navigate('/tracks');
    }
  }

  /* ── Album click handler ── */
  function handleAlbumClick(album: Album) {
    navigate(`/albums/${album.id}`);
  }

  /* ── Carousel scroll ── */
  const carouselRef = useRef<HTMLDivElement>(null);
  const [canScrollLeft, setCanScrollLeft] = useState(false);
  const [canScrollRight, setCanScrollRight] = useState(false);

  const updateScrollState = useCallback(() => {
    const el = carouselRef.current;
    if (!el) return;
    setCanScrollLeft(el.scrollLeft > 0);
    setCanScrollRight(el.scrollLeft + el.clientWidth < el.scrollWidth - 1);
  }, []);

  useEffect(() => {
    updateScrollState();
  }, [newAlbums, loading, updateScrollState]);

  function scrollCarousel(dir: 'left' | 'right') {
    const el = carouselRef.current;
    if (!el) return;
    const amount = el.clientWidth * 0.8;
    el.scrollBy({ left: dir === 'left' ? -amount : amount, behavior: 'smooth' });
  }

  return (
    <>
      {/* ── HERO ── */}
      <section className={styles.hero}>
        <div className={styles.heroBody}>
          <div className={styles.heroBadge}>
            {'\u2726'} New Release
          </div>
          <h1 className={styles.heroTitle}>
            {'쇼츠를 위한'}
            <br />
            {'최고의 음악'}
          </h1>
          <p className={styles.heroSub}>
            {'크리에이터를 위한 고품질 라이선스 음악.'}
            <br />
            {'구독 하나로 무제한 사용하세요.'}
          </p>
          <div className={styles.heroBtns}>
            <Link to="/subscriptions">
              <Button variant="primary" size="lg">
                {'지금 시작하기'}
              </Button>
            </Link>
            <Link to="/tracks">
              <Button variant="ghost" size="lg">
                {'음원 둘러보기'}
              </Button>
            </Link>
          </div>
        </div>
        <div className={styles.heroStacks}>
          <div className={styles.heroCard}>{'\u266A'}</div>
          <div className={styles.heroCard}>{'\u266B'}</div>
          <div className={styles.heroCard}>{'\u266A'}</div>
        </div>
      </section>

      {/* ── NEW RELEASES CAROUSEL ── */}
      <section className={styles.section}>
        <div className={styles.secHead}>
          <div className={styles.secTitle}>
            {'신규 앨범 '}
            <span className={styles.secTitleSmall}>New</span>
          </div>
          <Link to="/albums" className={styles.secMore}>
            {'전체 보기 \u2192'}
          </Link>
        </div>

        {loading ? (
          <div className={styles.carousel}>
            {Array.from({ length: CAROUSEL_SIZE }).map((_, i) => (
              <SkeletonCard key={i} />
            ))}
          </div>
        ) : error ? (
          <div className={styles.error}>{error}</div>
        ) : (
          <div className={styles.carouselWrap}>
            {canScrollLeft && (
              <button
                className={`${styles.carouselBtn} ${styles.carouselBtnLeft}`}
                onClick={() => scrollCarousel('left')}
                aria-label="Previous"
              >
                {'\u2039'}
              </button>
            )}
            <div
              className={styles.carousel}
              ref={carouselRef}
              onScroll={updateScrollState}
            >
              {newAlbums.map((album) => (
                <AlbumCard
                  key={album.id}
                  album={album}
                  onClick={handleAlbumClick}
                  className={styles.carouselCard}
                />
              ))}
            </div>
            {canScrollRight && (
              <button
                className={`${styles.carouselBtn} ${styles.carouselBtnRight}`}
                onClick={() => scrollCarousel('right')}
                aria-label="Next"
              >
                {'\u203A'}
              </button>
            )}
          </div>
        )}
      </section>

      <hr className={styles.divider} />

      {/* ── POPULAR ALBUMS GRID ── */}
      <section className={styles.section}>
        <div className={styles.secHead}>
          <div className={styles.secTitle}>{'인기 앨범'}</div>
          <Link to="/albums" className={styles.secMore}>
            {'전체 보기 \u2192'}
          </Link>
        </div>

        {loading ? (
          <div className={styles.albumGrid}>
            {Array.from({ length: GRID_SIZE }).map((_, i) => (
              <div key={i}>
                <div className={styles.skeletonThumb} />
                <div className={styles.skeletonText} />
                <div className={styles.skeletonMeta} />
              </div>
            ))}
          </div>
        ) : error ? (
          <div className={styles.error}>{error}</div>
        ) : (
          <div className={styles.albumGrid}>
            {popularAlbums.map((album) => (
              <AlbumCard
                key={album.id}
                album={album}
                onClick={handleAlbumClick}
              />
            ))}
          </div>
        )}
      </section>

      <hr className={styles.divider} />

      {/* ── NEW TRACKS ── */}
      <section className={styles.section}>
        <div className={styles.secHead}>
          <div className={styles.secTitle}>
            {'신규 음원 '}
            <span className={styles.secTitleSmall}>New</span>
          </div>
          <Link to="/tracks?sort=latest" className={styles.secMore}>
            {'전체 보기 \u2192'}
          </Link>
        </div>

        {loading ? (
          <div className={styles.trackList}>
            {Array.from({ length: NEW_TRACKS_SIZE }).map((_, i) => (
              <div key={i} className={styles.trackSkeleton}>
                <div className={styles.skeletonText} />
              </div>
            ))}
          </div>
        ) : error ? (
          <div className={styles.error}>{error}</div>
        ) : (
          <div className={styles.trackList}>
            {newTracks.map((track, idx) => (
              <Link
                key={track.id}
                to={`/tracks/${track.id}`}
                className={styles.trackItem}
              >
                <span className={styles.trackNum}>{idx + 1}</span>
                <span className={styles.trackTitle}>{track.title}</span>
                <span className={styles.trackMeta}>
                  {track.tags.some((t) => t.type === 'USAGE')
                    ? track.tags.filter((t) => t.type === 'USAGE').map((t) => `#${t.name}`).join(', ')
                    : track.tags.filter((t) => t.type === 'GENRE').map((t) => t.name).join(', ')}
                </span>
                <span className={styles.trackDur}>
                  {track.duration
                    ? `${Math.floor(track.duration / 60)}:${String(track.duration % 60).padStart(2, '0')}`
                    : '-'}
                </span>
              </Link>
            ))}
          </div>
        )}
      </section>

      <hr className={styles.divider} />

      {/* ── GENRE TAGS ── */}
      <section className={styles.section}>
        <div className={styles.secHead}>
          <div className={styles.secTitle}>{'장르별 탐색'}</div>
        </div>
        <div className={styles.tags}>
          {genreTags.map((tag) => (
            <Tag
              key={tag.id}
              label={tag.name}
              active={selectedGenres.has(tag.id)}
              onClick={() => toggleGenre(tag.id)}
            />
          ))}
          {selectedGenres.size > 0 && (
            <Button variant="outline" size="sm" onClick={handleGenreExplore}>
              {'탐색 \u2192'}
            </Button>
          )}
        </div>
      </section>

      {/* ── MOOD TAGS ── */}
      <section className={styles.section}>
        <div className={styles.secHead}>
          <div className={styles.secTitle}>{'분위기별 탐색'}</div>
        </div>
        <div className={styles.tags}>
          {moodTags.map((tag) => (
            <Tag
              key={tag.id}
              label={tag.name}
              active={selectedMoods.has(tag.id)}
              onClick={() => toggleMood(tag.id)}
            />
          ))}
          {selectedMoods.size > 0 && (
            <Button variant="outline" size="sm" onClick={handleMoodExplore}>
              {'탐색 \u2192'}
            </Button>
          )}
        </div>
      </section>

      {/* ── FOOTER ── */}
      <footer className={styles.footer}>
        <div>
          <div className={styles.ftLogo}>AT.M</div>
          <div className={styles.ftDesc}>
            {'쇼츠 크리에이터를 위한'}
            <br />
            {'음악 라이선스 플랫폼'}
          </div>
        </div>
        <div>
          <div className={styles.ftHead}>{'음원'}</div>
          <Link to="/tracks" className={styles.ftLink}>{'음원 목록'}</Link>
          <Link to="/albums" className={styles.ftLink}>{'앨범'}</Link>
        </div>
        <div>
          <div className={styles.ftHead}>{'구독'}</div>
          <Link to="/subscriptions" className={styles.ftLink}>{'구독 플랜'}</Link>
        </div>
        <div>
          <div className={styles.ftHead}>{'고객지원'}</div>
          <Link to="/notices" className={styles.ftLink}>{'공지사항'}</Link>
        </div>
        <div className={styles.ftCopy}>
          {'\u00A9 2026 AT.M. All rights reserved.'}
        </div>
      </footer>
    </>
  );
}

import { useState, useEffect, useRef, useCallback, type KeyboardEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { fetchAlbums } from '@/api/albums';
import { fetchTracks } from '@/api/tracks';
import { fetchAvailableTags, fetchTags } from '@/api/tags';
import type { Album, TagItem, TagType, TrackListItem } from '@/types';
import AlbumCard from '@/components/album/AlbumCard';
import Button from '@/components/ui/Button';
import { formatTagNameForDisplay } from '@/utils/tagName';
import styles from './HomePage.module.css';

/** Number of albums in the new releases carousel */
const CAROUSEL_SIZE = 7;
/** Number of albums in the popular grid */
const GRID_SIZE = 12;
/** Number of tracks in the new tracks section */
const NEW_TRACKS_SIZE = 6;
const INITIAL_DISCOVERY_TAGS = 8;

const TAG_CATEGORIES = [
  { type: 'USAGE', label: '용도', queryKey: 'usage' },
  { type: 'GENRE', label: '장르', queryKey: 'genre' },
  { type: 'MOOD', label: '분위기', queryKey: 'mood' },
  { type: 'INSTRUMENT', label: '악기', queryKey: 'instrument' },
] as const satisfies ReadonlyArray<{
  type: TagType;
  label: string;
  queryKey: 'usage' | 'genre' | 'mood' | 'instrument';
}>;

type TagCategory = (typeof TAG_CATEGORIES)[number];

function emptyTagSelection(): Record<TagType, number[]> {
  return { USAGE: [], GENRE: [], MOOD: [], INSTRUMENT: [] };
}

function buildTrackSearchPath(category: TagCategory, tags: TagItem[], selectedIds: number[]) {
  const params = new URLSearchParams();
  tags
    .filter((tag) => selectedIds.includes(tag.id))
    .forEach((tag) => params.append(category.queryKey, tag.name));
  return `/tracks?${params.toString()}`;
}

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
  const [registeredTags, setRegisteredTags] = useState<TagItem[]>([]);
  const [availableTags, setAvailableTags] = useState<TagItem[]>([]);
  const [activeTagType, setActiveTagType] = useState<TagType>('USAGE');
  const [selectedTagIds, setSelectedTagIds] =
    useState<Record<TagType, number[]>>(emptyTagSelection);
  const [expandedTagTypes, setExpandedTagTypes] = useState<Set<TagType>>(new Set());
  const [tagStatus, setTagStatus] = useState<'loading' | 'ready' | 'error'>('loading');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const tagLoadGenerationRef = useRef(0);
  const tagTabRefs = useRef<Array<HTMLButtonElement | null>>([]);

  /* ── Data fetching ── */
  useEffect(() => {
    let cancelled = false;

    async function load() {
      try {
        setLoading(true);
        setError(null);

        const [newRes, popRes, trackRes] = await Promise.all([
          fetchAlbums({ page: 1, size: CAROUSEL_SIZE, sort: 'latest' }),
          fetchAlbums({ page: 1, size: GRID_SIZE, sort: 'popular' }),
          fetchTracks({ page: 1, size: NEW_TRACKS_SIZE, sort: 'latest' }),
        ]);

        if (cancelled) return;

        setNewAlbums(newRes.dataList ?? []);
        setPopularAlbums(popRes.dataList ?? []);
        setNewTracks(trackRes.dataList ?? []);
      } catch (err) {
        if (!cancelled) {
          setError(err instanceof Error ? err.message : 'Failed to load data');
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

  const loadTagDiscovery = useCallback(async () => {
    const generation = ++tagLoadGenerationRef.current;
    setTagStatus('loading');

    try {
      const [registered, available] = await Promise.all([fetchTags(), fetchAvailableTags({})]);
      if (generation !== tagLoadGenerationRef.current) return;

      const safeRegistered = Array.isArray(registered) ? registered : [];
      const safeAvailable = Array.isArray(available) ? available : [];
      const firstCategoryWithResults = TAG_CATEGORIES.find((category) =>
        safeAvailable.some((tag) => tag.type === category.type),
      );

      setRegisteredTags(safeRegistered);
      setAvailableTags(safeAvailable);
      setActiveTagType(firstCategoryWithResults?.type ?? 'USAGE');
      setSelectedTagIds(emptyTagSelection());
      setExpandedTagTypes(new Set());
      setTagStatus('ready');
    } catch {
      if (generation === tagLoadGenerationRef.current) {
        setRegisteredTags([]);
        setAvailableTags([]);
        setTagStatus('error');
      }
    }
  }, []);

  useEffect(() => {
    void loadTagDiscovery();
    return () => {
      tagLoadGenerationRef.current += 1;
    };
  }, [loadTagDiscovery]);

  function toggleDiscoveryTag(tagId: number) {
    setSelectedTagIds((previous) => {
      const selected = previous[activeTagType];
      const nextSelected = selected.includes(tagId)
        ? selected.filter((id) => id !== tagId)
        : [...selected, tagId];
      return { ...previous, [activeTagType]: nextSelected };
    });
  }

  function handleTagTabKeyDown(event: KeyboardEvent<HTMLButtonElement>, currentIndex: number) {
    let nextIndex: number | null = null;
    if (event.key === 'ArrowRight') nextIndex = (currentIndex + 1) % TAG_CATEGORIES.length;
    if (event.key === 'ArrowLeft') {
      nextIndex = (currentIndex - 1 + TAG_CATEGORIES.length) % TAG_CATEGORIES.length;
    }
    if (event.key === 'Home') nextIndex = 0;
    if (event.key === 'End') nextIndex = TAG_CATEGORIES.length - 1;
    if (nextIndex === null) return;

    event.preventDefault();
    setActiveTagType(TAG_CATEGORIES[nextIndex].type);
    tagTabRefs.current[nextIndex]?.focus();
  }

  function toggleTagExpansion() {
    setExpandedTagTypes((previous) => {
      const next = new Set(previous);
      if (next.has(activeTagType)) next.delete(activeTagType);
      else next.add(activeTagType);
      return next;
    });
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

  const activeCategory = TAG_CATEGORIES.find((category) => category.type === activeTagType)!;
  const activeRegisteredTags = registeredTags.filter((tag) => tag.type === activeTagType);
  const activeAvailableTags = availableTags
    .filter((tag) => tag.type === activeTagType)
    .sort((a, b) => a.name.localeCompare(b.name, 'ko'));
  const activeSelectedIds = selectedTagIds[activeTagType];
  const activeExpanded = expandedTagTypes.has(activeTagType);
  const visibleAvailableTags = activeExpanded
    ? activeAvailableTags
    : activeAvailableTags.slice(0, INITIAL_DISCOVERY_TAGS);
  const trackSearchPath = buildTrackSearchPath(
    activeCategory,
    activeAvailableTags,
    activeSelectedIds,
  );

  function renderTagPanel() {
    if (tagStatus === 'loading') {
      return (
        <div className={styles.tagState} role="status">
          {'태그를 불러오는 중...'}
        </div>
      );
    }

    if (tagStatus === 'error') {
      return (
        <div className={styles.tagError} role="alert">
          <span>{'태그 탐색 정보를 불러오지 못했습니다.'}</span>
          <button className={styles.tagRetry} type="button" onClick={() => void loadTagDiscovery()}>
            {'다시 시도'}
          </button>
        </div>
      );
    }

    if (registeredTags.length === 0 && availableTags.length === 0) {
      return <div className={styles.tagState}>{'등록된 태그가 아직 없습니다.'}</div>;
    }

    if (activeAvailableTags.length === 0) {
      const message =
        activeRegisteredTags.length === 0
          ? `등록된 ${activeCategory.label} 태그가 아직 없습니다.`
          : `활성 음원에 연결된 ${activeCategory.label} 태그가 아직 없습니다.`;
      return <div className={styles.tagState}>{message}</div>;
    }

    return (
      <>
        <div className={styles.tagOptions} aria-label={`${activeCategory.label} 태그`}>
          {visibleAvailableTags.map((tag) => {
            const selected = activeSelectedIds.includes(tag.id);
            return (
              <button
                key={tag.id}
                className={`${styles.tagOption} ${selected ? styles.tagOptionSelected : ''}`}
                type="button"
                aria-pressed={selected}
                onClick={() => toggleDiscoveryTag(tag.id)}
              >
                {formatTagNameForDisplay(tag.name, tag.type)}
              </button>
            );
          })}
        </div>
        <div className={styles.tagActions}>
          {activeAvailableTags.length > INITIAL_DISCOVERY_TAGS && (
            <button className={styles.tagMore} type="button" onClick={toggleTagExpansion}>
              {activeExpanded
                ? '접기'
                : `더보기 (${activeAvailableTags.length - INITIAL_DISCOVERY_TAGS})`}
            </button>
          )}
          {activeSelectedIds.length > 0 && (
            <Link className={styles.tagExplore} to={trackSearchPath}>
              {'선택한 태그로 탐색'}
            </Link>
          )}
        </div>
      </>
    );
  }

  return (
    <>
      {/* ── HERO ── */}
      <section className={styles.hero}>
        <div className={styles.heroBody}>
          <div className={styles.heroBadge}>{'\u2726'} New Release</div>
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
            <div className={styles.carousel} ref={carouselRef} onScroll={updateScrollState}>
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
              <AlbumCard key={album.id} album={album} onClick={handleAlbumClick} />
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
              <Link key={track.id} to={`/tracks/${track.id}`} className={styles.trackItem}>
                <span className={styles.trackNum}>{idx + 1}</span>
                <span className={styles.trackTitle}>{track.title}</span>
                <span className={styles.trackMeta}>
                  {track.tags.some((t) => t.type === 'USAGE')
                    ? track.tags
                        .filter((t) => t.type === 'USAGE')
                        .map((t) => `#${t.name}`)
                        .join(', ')
                    : track.tags
                        .filter((t) => t.type === 'GENRE')
                        .map((t) => t.name)
                        .join(', ')}
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

      {/* ── TAG DISCOVERY ── */}
      <section className={`${styles.section} ${styles.tagExplorer}`}>
        <div className={styles.secHead}>
          <div className={styles.secTitle}>{'태그별 탐색'}</div>
        </div>
        <div className={styles.tagTabs} role="tablist" aria-label="태그 탐색 범주">
          {TAG_CATEGORIES.map((category, index) => {
            const selected = category.type === activeTagType;
            return (
              <button
                key={category.type}
                ref={(element) => {
                  tagTabRefs.current[index] = element;
                }}
                id={`home-tag-tab-${category.type.toLowerCase()}`}
                className={`${styles.tagTab} ${selected ? styles.tagTabSelected : ''}`}
                type="button"
                role="tab"
                aria-selected={selected}
                aria-controls="home-tag-panel"
                tabIndex={selected ? 0 : -1}
                onClick={() => setActiveTagType(category.type)}
                onKeyDown={(event) => handleTagTabKeyDown(event, index)}
              >
                {category.label}
              </button>
            );
          })}
        </div>
        <div
          className={styles.tagPanel}
          id="home-tag-panel"
          role="tabpanel"
          aria-labelledby={`home-tag-tab-${activeTagType.toLowerCase()}`}
        >
          {renderTagPanel()}
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
          <Link to="/tracks" className={styles.ftLink}>
            {'음원 목록'}
          </Link>
          <Link to="/albums" className={styles.ftLink}>
            {'앨범'}
          </Link>
        </div>
        <div>
          <div className={styles.ftHead}>{'구독'}</div>
          <Link to="/subscriptions" className={styles.ftLink}>
            {'구독 플랜'}
          </Link>
        </div>
        <div>
          <div className={styles.ftHead}>{'고객지원'}</div>
          <Link to="/notices" className={styles.ftLink}>
            {'공지사항'}
          </Link>
        </div>
        <div className={styles.ftCopy}>{'\u00A9 2026 AT.M. All rights reserved.'}</div>
      </footer>
    </>
  );
}

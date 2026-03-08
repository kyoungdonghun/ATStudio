import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { fetchAlbums } from '@/api/albums';
import { fetchTags } from '@/api/tags';
import type { Album, TagItem } from '@/types';
import AlbumCard from '@/components/album/AlbumCard';
import Button from '@/components/ui/Button';
import Tag from '@/components/ui/Tag';
import styles from './HomePage.module.css';

/** Number of albums in the new releases carousel */
const CAROUSEL_SIZE = 7;
/** Number of albums in the popular grid */
const GRID_SIZE = 6;

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
  const [genreTags, setGenreTags] = useState<TagItem[]>([]);
  const [selectedGenres, setSelectedGenres] = useState<Set<number>>(new Set());
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  /* ── Data fetching ── */
  useEffect(() => {
    let cancelled = false;

    async function load() {
      try {
        setLoading(true);
        setError(null);

        const [newRes, popRes, tags] = await Promise.all([
          fetchAlbums({ page: 1, size: CAROUSEL_SIZE, sort: 'latest' }),
          fetchAlbums({ page: 1, size: GRID_SIZE, sort: 'popular' }),
          fetchTags('GENRE'),
        ]);

        if (cancelled) return;

        setNewAlbums(newRes.dataList);
        setPopularAlbums(popRes.dataList);
        setGenreTags(tags);
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

  function handleGenreExplore() {
    const names = genreTags
      .filter((t) => selectedGenres.has(t.id))
      .map((t) => t.name);
    if (names.length > 0) {
      navigate(`/tracks?genre=${encodeURIComponent(names[0])}`);
    } else {
      navigate('/tracks');
    }
  }

  /* ── Album click handler ── */
  function handleAlbumClick(album: Album) {
    navigate(`/albums/${album.id}`);
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
          <div className={styles.carousel}>
            {newAlbums.map((album) => (
              <AlbumCard
                key={album.id}
                album={album}
                onClick={handleAlbumClick}
                className={styles.carouselCard}
              />
            ))}
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

      {/* ── FOOTER ── */}
      <footer className={styles.footer}>
        <div>
          <div className={styles.ftLogo}>ATStudio</div>
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
          {'\u00A9 2026 ATStudio. All rights reserved.'}
        </div>
      </footer>
    </>
  );
}

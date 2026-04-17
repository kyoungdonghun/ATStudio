import { useState, useEffect, useCallback } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { fetchAlbums } from '@/api/albums';
import type { Album, PageInfo } from '@/types';
import AlbumCard from '@/components/album/AlbumCard';
import Pagination from '@/components/ui/Pagination';
import { useAuthStore } from '@/store/authStore';
import { useAlbumLikeStore } from '@/store/albumLikeStore';
import styles from './AlbumListImagePage.module.css';

const PAGE_SIZE = 24;

export default function AlbumListImagePage() {
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();

  /* ── State ── */
  const [albums, setAlbums] = useState<Album[]>([]);
  const [pageInfo, setPageInfo] = useState<PageInfo | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const isAuthenticated = useAuthStore((s) => s.isAuthenticated());
  const albumLikeLoaded = useAlbumLikeStore((s) => s.loaded);
  const loadAlbumLikes = useAlbumLikeStore((s) => s.load);
  const likedAlbumIds = useAlbumLikeStore((s) => s.likedAlbumIds);
  const toggleAlbumLike = useAlbumLikeStore((s) => s.toggle);

  const currentPage = Number(searchParams.get('page') ?? '1');
  const sortValue = (searchParams.get('sort') ?? 'latest') as 'latest' | 'trackCount';

  /* ── Data fetch ── */
  const loadAlbums = useCallback(async () => {
    setLoading(true);
    setError(null);

    try {
      const res = await fetchAlbums({ page: currentPage, size: PAGE_SIZE, sort: sortValue });
      setAlbums(res.dataList);
      setPageInfo(res.pageInfo ?? null);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load albums');
    } finally {
      setLoading(false);
    }
  }, [currentPage, sortValue]);

  useEffect(() => {
    loadAlbums();
  }, [loadAlbums]);

  useEffect(() => {
    if (isAuthenticated && !albumLikeLoaded) {
      void loadAlbumLikes();
    }
  }, [isAuthenticated, albumLikeLoaded, loadAlbumLikes]);

  /* ── Pagination ── */
  function goToPage(page: number) {
    const next = new URLSearchParams(searchParams);
    next.set('page', String(page));
    setSearchParams(next);
  }

  function handleSortChange(e: React.ChangeEvent<HTMLSelectElement>) {
    const next = new URLSearchParams(searchParams);
    next.set('sort', e.target.value);
    next.set('page', '1');
    setSearchParams(next);
  }

  function handleAlbumClick(album: Album) {
    navigate(`/albums/${album.id}`);
  }

  return (
    <div className={styles.page}>
      {/* Page Header */}
      <div className={styles.pageHeader}>
        <div className={styles.pageTitle}>
          {'앨범'}
          {pageInfo && (
            <span className={styles.pageTitleCount}>
              {`총 ${pageInfo.total.toLocaleString()}개`}
            </span>
          )}
        </div>
        <div className={styles.headerRight}>
          <div className={styles.sortBar}>
            <select
              className={styles.sortSelect}
              value={sortValue}
              onChange={handleSortChange}
            >
              <option value="latest">{'최신순'}</option>
              <option value="trackCount">{'곡 수순'}</option>
            </select>
          </div>
          <div className={styles.viewToggle}>
            <span className={`${styles.viewBtn} ${styles.viewBtnActive}`}>
              {'카드'}
            </span>
            <Link to="/albums/list" className={styles.viewBtn}>
              {'리스트'}
            </Link>
          </div>
        </div>
      </div>

      {/* Album Grid */}
      {loading ? (
        <div className={styles.albumGrid}>
          {Array.from({ length: 12 }).map((_, i) => (
            <div key={i}>
              <div className={styles.skeletonThumb} />
              <div className={styles.skeletonText} />
              <div className={styles.skeletonMeta} />
            </div>
          ))}
        </div>
      ) : error ? (
        <div className={styles.error}>{error}</div>
      ) : albums.length === 0 ? (
        <div className={styles.empty}>{'앨범이 없습니다.'}</div>
      ) : (
        <>
          <div className={styles.albumGrid}>
            {albums.map((album) => (
                <AlbumCard
                  key={album.id}
                  album={album}
                  onClick={handleAlbumClick}
                  isLiked={likedAlbumIds.has(album.id)}
                  onToggleLike={isAuthenticated ? (id) => toggleAlbumLike(id) : undefined}
                />
            ))}
          </div>

          {pageInfo && (
            <Pagination pageInfo={pageInfo} currentPage={currentPage} onPageChange={goToPage} />
          )}
        </>
      )}
    </div>
  );
}

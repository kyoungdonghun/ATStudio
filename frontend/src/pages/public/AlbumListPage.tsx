import { useState, useEffect, useCallback } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { fetchAlbums } from '@/api/albums';
import { toUploadUrl } from '@/api/client';
import { formatDate } from '@/utils/format';
import type { Album, PageInfo } from '@/types';
import Pagination from '@/components/ui/Pagination';
import { useAuthStore } from '@/store/authStore';
import { useAlbumLikeStore } from '@/store/albumLikeStore';
import styles from './AlbumListPage.module.css';

const PAGE_SIZE = 20;

export default function AlbumListPage() {
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();

  /* ── State ── */
  const [albums, setAlbums] = useState<Album[]>([]);
  const [pageInfo, setPageInfo] = useState<PageInfo | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const isAuthenticated = useAuthStore((s) => s.isAuthenticated());
  const albumLikeStore = useAlbumLikeStore();

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
    if (isAuthenticated && !albumLikeStore.loaded) {
      albumLikeStore.load();
    }
  }, [isAuthenticated, albumLikeStore.loaded]);

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
            <Link to="/albums" className={styles.viewBtn}>
              {'카드'}
            </Link>
            <span className={`${styles.viewBtn} ${styles.viewBtnActive}`}>
              {'리스트'}
            </span>
          </div>
        </div>
      </div>

      {/* Album Table */}
      {loading ? (
        <div className={styles.loading}>{'Loading...'}</div>
      ) : error ? (
        <div className={styles.error}>{error}</div>
      ) : albums.length === 0 ? (
        <div className={styles.empty}>{'앨범이 없습니다.'}</div>
      ) : (
        <>
          <table className={styles.albumTable}>
            <thead>
              <tr>
                <th>{'앨범'}</th>
                <th className={styles.thRight}>{'곡 수'}</th>
                <th className={styles.thRight}>{'등록일'}</th>
                {isAuthenticated && <th className={styles.thCenter}>{''}</th>}
              </tr>
            </thead>
            <tbody>
              {albums.map((album) => (
                <tr
                  key={album.id}
                  className={styles.albumRow}
                  onClick={() => navigate(`/albums/${album.id}`)}
                >
                  <td>
                    <div className={styles.cellInfo}>
                      <div className={styles.albumThumb}>
                        {album.thumbnailUrl ? (
                          <img src={toUploadUrl(album.thumbnailUrl)!} alt={album.title} />
                        ) : (
                          '\u266A'
                        )}
                      </div>
                      <div>
                        <div className={styles.albumTitle}>{album.title}</div>
                        {album.description && (
                          <div className={styles.albumDesc}>
                            {album.description}
                          </div>
                        )}
                      </div>
                    </div>
                  </td>
                  <td className={styles.cellCount}>
                    {`${album.trackCount}곡`}
                  </td>
                  <td className={styles.cellDate}>
                    {formatDate(album.createdAt)}
                  </td>
                  {isAuthenticated && (
                    <td className={styles.cellLike}>
                      <button
                        className={`${styles.likeBtn} ${albumLikeStore.likedAlbumIds.has(album.id) ? styles.likeBtnActive : ''}`}
                        onClick={(e) => {
                          e.stopPropagation();
                          albumLikeStore.toggle(album.id);
                        }}
                        aria-label={albumLikeStore.likedAlbumIds.has(album.id) ? '좋아요 해제' : '좋아요'}
                      >
                        {albumLikeStore.likedAlbumIds.has(album.id) ? '\u2665' : '\u2661'}
                      </button>
                    </td>
                  )}
                </tr>
              ))}
            </tbody>
          </table>

          {pageInfo && (
            <Pagination pageInfo={pageInfo} currentPage={currentPage} onPageChange={goToPage} />
          )}
        </>
      )}
    </div>
  );
}

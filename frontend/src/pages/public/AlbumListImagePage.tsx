import { useState, useEffect, useCallback } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { fetchAlbums } from '@/api/albums';
import type { Album, PageInfo } from '@/types';
import AlbumCard from '@/components/album/AlbumCard';
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

  const currentPage = Number(searchParams.get('page') ?? '1');

  /* ── Data fetch ── */
  const loadAlbums = useCallback(async () => {
    setLoading(true);
    setError(null);

    try {
      const res = await fetchAlbums({ page: currentPage, size: PAGE_SIZE });
      setAlbums(res.dataList);
      setPageInfo((res as unknown as { pageInfo?: typeof pageInfo }).pageInfo ?? null);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load albums');
    } finally {
      setLoading(false);
    }
  }, [currentPage]);

  useEffect(() => {
    loadAlbums();
  }, [loadAlbums]);

  /* ── Pagination ── */
  function goToPage(page: number) {
    const next = new URLSearchParams(searchParams);
    next.set('page', String(page));
    setSearchParams(next);
  }

  function buildPageRange(): (number | 'ellipsis')[] {
    if (!pageInfo) return [];
    const { start, end } = pageInfo;
    const totalPages = Math.ceil(pageInfo.total / PAGE_SIZE);
    const result: (number | 'ellipsis')[] = [];
    for (let i = start; i <= end; i++) result.push(i);
    if (end < totalPages) {
      result.push('ellipsis');
      result.push(totalPages);
    }
    return result;
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
        <div className={styles.viewToggle}>
          <span className={`${styles.viewBtn} ${styles.viewBtnActive}`}>
            {'카드'}
          </span>
          <Link to="/albums/list" className={styles.viewBtn}>
            {'리스트'}
          </Link>
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
              />
            ))}
          </div>

          {/* Pagination */}
          {pageInfo && (
            <div className={styles.pagination}>
              <button
                className={styles.pgBtn}
                disabled={!pageInfo.prev}
                onClick={() => goToPage(currentPage - 1)}
              >
                {'\u2039'}
              </button>

              {buildPageRange().map((item, idx) =>
                item === 'ellipsis' ? (
                  <span key={`e-${idx}`} className={styles.pgEllipsis}>
                    {'\u2026'}
                  </span>
                ) : (
                  <button
                    key={item}
                    className={`${styles.pgBtn} ${item === currentPage ? styles.pgBtnActive : ''}`}
                    onClick={() => goToPage(item)}
                  >
                    {item}
                  </button>
                ),
              )}

              <button
                className={styles.pgBtn}
                disabled={!pageInfo.next}
                onClick={() => goToPage(currentPage + 1)}
              >
                {'\u203A'}
              </button>
            </div>
          )}
        </>
      )}
    </div>
  );
}

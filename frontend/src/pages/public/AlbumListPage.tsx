import { useState, useEffect, useCallback } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { fetchAlbums } from '@/api/albums';
import type { Album, PageInfo } from '@/types';
import styles from './AlbumListPage.module.css';

const PAGE_SIZE = 20;

function formatDate(iso: string): string {
  return iso.substring(0, 10);
}

export default function AlbumListPage() {
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
          <Link to="/albums" className={styles.viewBtn}>
            {'카드'}
          </Link>
          <span className={`${styles.viewBtn} ${styles.viewBtnActive}`}>
            {'리스트'}
          </span>
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
                          <img src={album.thumbnailUrl} alt={album.title} />
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
                    {album.createdAt ? formatDate(album.createdAt) : '-'}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>

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

import { useState, useEffect, useCallback, useRef } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { fetchAlbums } from '@/api/albums';
import type { Album, PageInfo } from '@/types';
import AlbumCard from '@/components/album/AlbumCard';
import Pagination from '@/components/ui/Pagination';
import { useAuthStore } from '@/store/authStore';
import { useAlbumLikeStore } from '@/store/albumLikeStore';
import { classifyLoadError, getLoadErrorMessage } from '@/api/loadError';
import {
  getCatalogTotalPages,
  normalizeCatalogPage,
  PUBLIC_CATALOG_PAGE_SIZE,
  withCatalogQuery,
} from '@/utils/catalogPagination';
import styles from './AlbumListImagePage.module.css';

export default function AlbumListImagePage() {
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();
  const latestSearchParamsRef = useRef(searchParams);
  latestSearchParamsRef.current = searchParams;

  /* ── State ── */
  const [albums, setAlbums] = useState<Album[]>([]);
  const [pageInfo, setPageInfo] = useState<PageInfo | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const requestGenerationRef = useRef(0);
  const requestControllerRef = useRef<AbortController | null>(null);
  const retryInFlightRef = useRef(false);

  const isAuthenticated = useAuthStore((s) => s.isAuthenticated());
  const albumLikeLoaded = useAlbumLikeStore((s) => s.loaded);
  const loadAlbumLikes = useAlbumLikeStore((s) => s.load);
  const likedAlbumIds = useAlbumLikeStore((s) => s.likedAlbumIds);
  const toggleAlbumLike = useAlbumLikeStore((s) => s.toggle);

  const rawPage = searchParams.get('page');
  const currentPage = normalizeCatalogPage(rawPage);
  const pageNeedsNormalization = rawPage !== null && rawPage !== String(currentPage);
  const sortValue = (searchParams.get('sort') ?? 'latest') as 'latest' | 'trackCount';
  const requestKey = `${currentPage}:${sortValue}`;
  const latestRequestKeyRef = useRef(requestKey);
  latestRequestKeyRef.current = requestKey;

  useEffect(() => {
    if (rawPage === null || rawPage === String(currentPage)) return;
    const next = new URLSearchParams(searchParams);
    next.set('page', String(currentPage));
    setSearchParams(next, { replace: true });
  }, [currentPage, rawPage, searchParams, setSearchParams]);

  /* ── Data fetch ── */
  const loadAlbums = useCallback(async () => {
    requestControllerRef.current?.abort();
    const controller = new AbortController();
    requestControllerRef.current = controller;
    const generation = ++requestGenerationRef.current;
    const ownedRequestKey = requestKey;
    const isCurrentRequest = () =>
      generation === requestGenerationRef.current &&
      ownedRequestKey === latestRequestKeyRef.current &&
      !controller.signal.aborted;

    setLoading(true);
    setError(null);

    try {
      const res = await fetchAlbums(
        { page: currentPage, size: PUBLIC_CATALOG_PAGE_SIZE, sort: sortValue },
        controller.signal,
      );
      if (!isCurrentRequest()) return;
      const totalPages = getCatalogTotalPages(res.pageInfo.total, PUBLIC_CATALOG_PAGE_SIZE);
      if (currentPage > totalPages && res.dataList.length === 0) {
        const next = new URLSearchParams(latestSearchParamsRef.current);
        next.set('page', String(totalPages));
        setSearchParams(next, { replace: true });
        return;
      }
      setAlbums(res.dataList);
      setPageInfo(res.pageInfo ?? null);
    } catch (loadError) {
      if (!isCurrentRequest() || classifyLoadError(loadError) === 'cancelled') return;
      setAlbums([]);
      setPageInfo(null);
      setError(getLoadErrorMessage(loadError, '앨범 목록'));
    } finally {
      if (isCurrentRequest()) {
        setLoading(false);
        requestControllerRef.current = null;
      }
    }
  }, [currentPage, requestKey, setSearchParams, sortValue]);

  useEffect(() => {
    if (pageNeedsNormalization) return;
    void loadAlbums();
    return () => {
      requestControllerRef.current?.abort();
      requestControllerRef.current = null;
      requestGenerationRef.current += 1;
    };
  }, [loadAlbums, pageNeedsNormalization]);

  const retryLoadAlbums = useCallback(() => {
    if (retryInFlightRef.current || loading) return;
    retryInFlightRef.current = true;
    void loadAlbums().finally(() => {
      retryInFlightRef.current = false;
    });
  }, [loadAlbums, loading]);

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
            <select className={styles.sortSelect} value={sortValue} onChange={handleSortChange}>
              <option value="latest">{'최신순'}</option>
              <option value="trackCount">{'곡 수순'}</option>
            </select>
          </div>
          <div className={styles.viewToggle}>
            <span className={`${styles.viewBtn} ${styles.viewBtnActive}`}>{'카드'}</span>
            <Link to={withCatalogQuery('/albums/list', searchParams)} className={styles.viewBtn}>
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
        <div className={styles.error} role="alert">
          <p className={styles.errorMessage}>{error}</p>
          <button type="button" className={styles.retryButton} onClick={retryLoadAlbums}>
            {'다시 시도'}
          </button>
        </div>
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

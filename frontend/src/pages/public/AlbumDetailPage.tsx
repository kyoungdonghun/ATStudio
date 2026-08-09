/** Screen L-3: Album detail */
import { useEffect, useState } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { fetchAlbumDetail, type AlbumDetail } from '@/api/albums';
import { toUploadUrl } from '@/api/client';
import { usePlayerStore } from '@/store/playerStore';
import { useLikeStore } from '@/store/likeStore';
import { useAlbumLikeStore } from '@/store/albumLikeStore';
import { useAuthStore } from '@/store/authStore';
import { formatDate } from '@/utils/format';
import { toPlayableTrack } from '@/utils/playableTrack';
import { useToastStore } from '@/store/toastStore';
import AddToPlaylistModal from '@/components/playlist/AddToPlaylistModal';
import styles from './AlbumDetailPage.module.css';

export default function AlbumDetailPage() {
  const { albumId } = useParams<{ albumId: string }>();
  const [album, setAlbum] = useState<AlbumDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [addToPlTrackId, setAddToPlTrackId] = useState<number | null>(null);
  const toast = useToastStore((s) => s.show);
  const navigate = useNavigate();

  useEffect(() => {
    if (!albumId) return;

    setLoading(true);
    setError(null);
    fetchAlbumDetail(Number(albumId))
      .then(setAlbum)
      .catch(() => setError('Failed to load album'))
      .finally(() => setLoading(false));
  }, [albumId]);

  const currentTrack = usePlayerStore((s) => s.currentTrack);
  const isPlayerPlaying = usePlayerStore((s) => s.isPlaying);
  const playTrack = usePlayerStore((s) => s.play);
  const pauseTrack = usePlayerStore((s) => s.pause);
  const resumeTrack = usePlayerStore((s) => s.resume);
  const playAll = usePlayerStore((s) => s.playAll);
  const setTrackListContext = usePlayerStore((s) => s.setTrackListContext);
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated());
  const likeLoaded = useLikeStore((s) => s.loaded);
  const loadLikes = useLikeStore((s) => s.load);
  const likedIds = useLikeStore((s) => s.likedIds);
  const toggleLike = useLikeStore((s) => s.toggle);
  const albumLikeLoaded = useAlbumLikeStore((s) => s.loaded);
  const loadAlbumLikes = useAlbumLikeStore((s) => s.load);
  const likedAlbumIds = useAlbumLikeStore((s) => s.likedAlbumIds);
  const toggleAlbumLike = useAlbumLikeStore((s) => s.toggle);

  useEffect(() => {
    if (isAuthenticated && !likeLoaded) {
      void loadLikes();
    }
  }, [isAuthenticated, likeLoaded, loadLikes]);

  useEffect(() => {
    if (isAuthenticated && !albumLikeLoaded) {
      void loadAlbumLikes();
    }
  }, [isAuthenticated, albumLikeLoaded, loadAlbumLikes]);

  /* SR-83: Publish album tracks as player context so Next/Prev traverses them. */
  useEffect(() => {
    if (!album) return;
    const tracks = album.tracks.map((track) => toPlayableTrack(track));
    setTrackListContext(tracks);
  }, [album, setTrackListContext]);

  if (loading) {
    return (
      <div className={styles.page}>
        <div className={styles.loading}>Loading...</div>
      </div>
    );
  }

  if (error || !album) {
    return (
      <div className={styles.page}>
        <div className={styles.error}>{error ?? 'Album not found'}</div>
      </div>
    );
  }

  return (
    <div className={styles.page}>
      {/* Breadcrumb */}
      <nav className={styles.breadcrumb}>
        <Link to="/">Home</Link>
        <span>&rsaquo;</span>
        <Link to="/albums">Album</Link>
        <span>&rsaquo;</span>
        <span className={styles.breadcrumbCurrent}>{album.title}</span>
      </nav>

      {/* Hero */}
      <section className={styles.hero}>
        {/* Cover + Vinyl */}
        <div className={styles.coverWrap}>
          <div className={styles.vinyl} />
          <div className={styles.cover}>
            {album.thumbnailUrl ? (
              <img
                src={toUploadUrl(album.thumbnailUrl)!}
                alt={album.title}
                className={styles.coverImg}
              />
            ) : (
              <span className={styles.coverPlaceholder}>{'\u266A'}</span>
            )}
          </div>
        </div>

        {/* Info */}
        <div className={styles.info}>
          <div className={styles.albumType}>Album</div>
          <h1 className={styles.title}>{album.title}</h1>
          <div className={styles.meta}>
            <span className={styles.metaItem}>
              {'\uD83C\uDFB5'} {album.tracks.length}곡
            </span>
            <span className={styles.metaItem}>
              {'\u2665'} {album.likeCount ?? 0}
            </span>
            <span className={styles.metaItem}>
              {'\uD83D\uDCC5'} {formatDate(album.createdAt)}
            </span>
          </div>
          {album.description && <p className={styles.desc}>{album.description}</p>}
          <div className={styles.actions}>
            <button
              className={styles.btnPlayAll}
              onClick={() => {
                const tracks = album.tracks.map((track) => toPlayableTrack(track));
                playAll(tracks);
              }}
            >
              {'\u25B6'}&nbsp;&nbsp;전체 재생
            </button>
            {isAuthenticated && (
              <button
                className={`${styles.btnLike} ${likedAlbumIds.has(album.id) ? styles.btnLikeActive : ''}`}
                onClick={() => {
                  void toggleAlbumLike(album.id);
                  toast(
                    'success',
                    likedAlbumIds.has(album.id)
                      ? '앨범 좋아요가 해제되었습니다.'
                      : '앨범을 좋아요했습니다.',
                  );
                }}
                title={likedAlbumIds.has(album.id) ? '앨범 좋아요 해제' : '앨범 좋아요'}
              >
                {likedAlbumIds.has(album.id) ? '\u2665' : '\u2661'}&nbsp;&nbsp;좋아요
              </button>
            )}
          </div>
        </div>
      </section>

      {/* Track List */}
      <section>
        <div className={styles.sectionLabel}>수록곡</div>
        <div className={styles.tableWrap}>
          <table className={styles.trackTable}>
            <thead>
              <tr>
                <th className={`${styles.thCenter} ${styles.thNum}`}>#</th>
                <th>음원</th>
                <th className={`${styles.thRight} ${styles.thOrder}`}>순서</th>
                <th className={styles.thActions} />
              </tr>
            </thead>
            <tbody>
              {album.tracks.map((t, idx) => (
                <tr
                  key={t.trackId}
                  className={`${styles.trackRow} ${currentTrack?.id === t.trackId ? styles.trackRowActive : ''}`}
                >
                  <td className={styles.tdNum}>
                    <span className={styles.trNum}>{idx + 1}</span>
                    <button
                      className={styles.trPlayBtn}
                      onClick={(e) => {
                        e.stopPropagation();
                        if (currentTrack?.id === t.trackId) {
                          if (isPlayerPlaying) pauseTrack();
                          else resumeTrack();
                        } else {
                          playTrack(toPlayableTrack(t));
                        }
                      }}
                      aria-label={
                        currentTrack?.id === t.trackId && isPlayerPlaying ? 'Pause' : 'Play'
                      }
                    >
                      {currentTrack?.id === t.trackId && isPlayerPlaying ? '\u23F8' : '\u25B6'}
                    </button>
                  </td>
                  <td>
                    <Link to={`/tracks/${t.trackId}`} className={styles.tdInfo}>
                      <div className={styles.trThumb}>
                        {t.thumbnailUrl ? (
                          <img src={toUploadUrl(t.thumbnailUrl)!} alt={t.title} />
                        ) : (
                          '\u266A'
                        )}
                      </div>
                      <div>
                        <div className={styles.trTitle}>{t.title}</div>
                      </div>
                    </Link>
                  </td>
                  <td className={styles.tdOrder}>{t.order}</td>
                  <td className={styles.tdActions}>
                    <div className={styles.tdActionsInner}>
                      {isAuthenticated ? (
                        <>
                          <button
                            className={`${styles.trActBtn} ${likedIds.has(t.trackId) ? styles.trActBtnActive : ''}`}
                            onClick={() => {
                              void toggleLike(t.trackId);
                            }}
                            aria-label="Like"
                          >
                            {likedIds.has(t.trackId) ? '\u2665' : '\u2661'}
                          </button>
                          <button
                            className={styles.trActBtn}
                            onClick={() => setAddToPlTrackId(t.trackId)}
                            aria-label="Add to playlist"
                            title="재생목록에 추가"
                          >
                            +
                          </button>
                        </>
                      ) : (
                        <>
                          <button
                            className={styles.trActBtn}
                            onClick={() => {
                              toast('warning', '로그인이 필요한 기능입니다.');
                              navigate('/login');
                            }}
                            title="Like"
                          >
                            {'\u2661'}
                          </button>
                          <button
                            className={styles.trActBtn}
                            onClick={() => {
                              toast('warning', '로그인이 필요한 기능입니다.');
                              navigate('/login');
                            }}
                            title="Add to playlist"
                          >
                            +
                          </button>
                        </>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>

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

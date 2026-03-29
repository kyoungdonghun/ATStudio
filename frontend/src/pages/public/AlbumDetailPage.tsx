/** Screen L-3: Album detail */
import { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { fetchAlbumDetail, type AlbumDetail } from '@/api/albums';
import { toUploadUrl } from '@/api/client';
import { usePlayerStore } from '@/store/playerStore';
import { useLikeStore } from '@/store/likeStore';
import { useAlbumLikeStore } from '@/store/albumLikeStore';
import { useAuthStore } from '@/store/authStore';
import { formatDate } from '@/utils/format';
import { downloadTrack, triggerBlobDownload } from '@/api/downloads';
import { useToastStore } from '@/store/toastStore';
import { addToDownloadQueue } from '@/api/downloadQueue';
import AddToPlaylistModal from '@/components/playlist/AddToPlaylistModal';
import styles from './AlbumDetailPage.module.css';

export default function AlbumDetailPage() {
  const { albumId } = useParams<{ albumId: string }>();
  const [album, setAlbum] = useState<AlbumDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [addToPlTrackId, setAddToPlTrackId] = useState<number | null>(null);
  const toast = useToastStore((s) => s.show);

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
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated());
  const likeStore = useLikeStore();
  const albumLikeStore = useAlbumLikeStore();

  useEffect(() => {
    if (isAuthenticated && !likeStore.loaded) {
      likeStore.load();
    }
  }, [isAuthenticated, likeStore.loaded]);

  useEffect(() => {
    if (isAuthenticated && !albumLikeStore.loaded) {
      albumLikeStore.load();
    }
  }, [isAuthenticated, albumLikeStore.loaded]);

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
          {album.description && (
            <p className={styles.desc}>{album.description}</p>
          )}
          <div className={styles.actions}>
            <button
              className={styles.btnPlayAll}
              onClick={() => {
                const tracks = album.tracks.map((t) => ({
                  id: t.trackId,
                  title: t.title,
                  artistName: t.artistName ?? '',
                  duration: 0,
                  bpm: 0,
                  tonality: '',
                  description: null,
                  audioFile: null,
                  thumbnail: t.thumbnailUrl ?? null,
                  tags: [],
                  isActive: true,
                  playCount: 0,
                  likeCount: 0,
                  downloadCount: 0,
                  createdAt: '',
                  updatedAt: '',
                }));
                playAll(tracks);
              }}
            >
              {'\u25B6'}&nbsp;&nbsp;전체 재생
            </button>
            {isAuthenticated && (
              <button
                className={`${styles.btnLike} ${albumLikeStore.likedAlbumIds.has(album.id) ? styles.btnLikeActive : ''}`}
                onClick={() => {
                  albumLikeStore.toggle(album.id);
                  toast(
                    'success',
                    albumLikeStore.likedAlbumIds.has(album.id) ? '앨범 좋아요가 해제되었습니다.' : '앨범을 좋아요했습니다.',
                  );
                }}
                title={albumLikeStore.likedAlbumIds.has(album.id) ? '앨범 좋아요 해제' : '앨범 좋아요'}
              >
                {albumLikeStore.likedAlbumIds.has(album.id) ? '\u2665' : '\u2661'}&nbsp;&nbsp;좋아요
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
              <th className={styles.thCenter} style={{ width: 42 }}>
                #
              </th>
              <th>음원</th>
              <th style={{ width: 66 }} className={styles.thRight}>
                순서
              </th>
              <th style={{ width: 140 }} />
            </tr>
          </thead>
          <tbody>
            {album.tracks.map((t, idx) => (
              <tr key={t.trackId} className={styles.trackRow}>
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
                        playTrack({
                          id: t.trackId,
                          title: t.title,
                          artistName: t.artistName ?? '',
                          duration: 0,
                          bpm: 0,
                          tonality: '',
                          description: null,
                          audioFile: null,
                          thumbnail: t.thumbnailUrl ?? null,
                          tags: [],
                          isActive: true,
                          playCount: 0,
                          likeCount: 0,
                          downloadCount: 0,
                          createdAt: '',
                          updatedAt: '',
                        });
                      }
                    }}
                    aria-label={currentTrack?.id === t.trackId && isPlayerPlaying ? 'Pause' : 'Play'}
                  >
                    {currentTrack?.id === t.trackId && isPlayerPlaying ? '\u23F8' : '\u25B6'}
                  </button>
                </td>
                <td>
                  <Link
                    to={`/tracks/${t.trackId}`}
                    className={styles.tdInfo}
                  >
                    <div className={styles.trThumb}>
                      {t.thumbnailUrl ? (
                        <img src={toUploadUrl(t.thumbnailUrl)!} alt={t.title} />
                      ) : (
                        '\u266A'
                      )}
                    </div>
                    <div>
                      <div className={styles.trTitle}>{t.title}</div>
                      <div className={styles.trArtist}>{t.artistName}</div>
                    </div>
                  </Link>
                </td>
                <td className={styles.tdOrder}>{t.order}</td>
                <td className={styles.tdActions}>
                  {isAuthenticated && (
                    <>
                      <button
                        className={`${styles.trLikeBtn} ${likeStore.likedIds.has(t.trackId) ? styles.trLikeBtnActive : ''}`}
                        onClick={() => likeStore.toggle(t.trackId)}
                        aria-label="Like"
                      >
                        {likeStore.likedIds.has(t.trackId) ? '\u2665' : '\u2661'}
                      </button>
                      <button
                        className={styles.trAddPlBtn}
                        onClick={() => setAddToPlTrackId(t.trackId)}
                        aria-label="Add to playlist"
                        title="재생목록에 추가"
                      >
                        +
                      </button>
                      <button
                        className={styles.trDlBtn}
                        onClick={async (e) => {
                          e.stopPropagation();
                          try {
                            const blob = await downloadTrack(t.trackId);
                            triggerBlobDownload(blob, `${t.title}.mp3`);
                          } catch {
                            toast('error', '다운로드에 실패했습니다.');
                          }
                        }}
                        aria-label="Download"
                        title="다운로드"
                      >
                        &#8595;
                      </button>
                      <button
                        className={styles.trQueueBtn}
                        onClick={async (e) => {
                          e.stopPropagation();
                          try {
                            await addToDownloadQueue(t.trackId);
                            toast('success', '다운로드 대기열에 추가되었습니다.');
                          } catch (err: unknown) {
                            const axErr = err as { response?: { status?: number } };
                            if (axErr.response?.status === 409) {
                              toast('error', '이미 대기열에 있는 음원입니다.');
                            } else {
                              toast('error', '대기열 추가에 실패했습니다.');
                            }
                          }
                        }}
                        aria-label="Add to download queue"
                        title="대기열에 추가"
                      >
                        대기열
                      </button>
                    </>
                  )}
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
      />
    </div>
  );
}

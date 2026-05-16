/** Screen L-3: Album detail */
import { useEffect, useState } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { fetchAlbumDetail, type AlbumDetail, type AlbumTrack } from '@/api/albums';
import { getApiErrorCode, toUploadUrl } from '@/api/client';
import { downloadTrack, fetchDownloadCount, triggerBlobDownload } from '@/api/downloads';
import { usePlayerStore } from '@/store/playerStore';
import { useLikeStore } from '@/store/likeStore';
import { useAlbumLikeStore } from '@/store/albumLikeStore';
import { useAuthStore } from '@/store/authStore';
import { formatDate } from '@/utils/format';
import { useToastStore } from '@/store/toastStore';
import AddToPlaylistModal from '@/components/playlist/AddToPlaylistModal';
import TrackRow from '@/components/track/TrackRow';
import type { Track, TrackListItem } from '@/types';
import styles from './AlbumDetailPage.module.css';

function albumTrackToTrackListItem(track: AlbumTrack): TrackListItem {
  return {
    id: track.trackId,
    title: track.title,
    artistName: track.artistName ?? '',
    duration: track.duration ?? 0,
    bpm: track.bpm ?? 0,
    tonality: track.tonality ?? '',
    thumbnail: track.thumbnailUrl ?? null,
    playCount: track.playCount ?? 0,
    likeCount: track.likeCount ?? 0,
    downloadCount: track.downloadCount ?? 0,
    waveformData: track.waveformData,
    tags: track.tags ?? [],
    createdAt: track.createdAt ?? '',
  };
}

function albumTrackToPlayerTrack(track: AlbumTrack): Track {
  const item = albumTrackToTrackListItem(track);
  return {
    ...item,
    description: null,
    audioFile: null,
    isActive: true,
    updatedAt: item.createdAt,
  };
}

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
    setTrackListContext(album.tracks.map(albumTrackToPlayerTrack));
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
                playAll(album.tracks.map(albumTrackToPlayerTrack));
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
                <th>{'음원'}</th>
                <th className={styles.thTag}>{'장르'}</th>
                <th className={styles.thTag}>{'악기'}</th>
                <th className={styles.thTag}>{'분위기'}</th>
                <th className={`${styles.thRight} ${styles.thBpm}`}>BPM</th>
                <th className={`${styles.thCenter} ${styles.thKey}`}>{'조성'}</th>
                <th className={`${styles.thRight} ${styles.thDur}`}>{'길이'}</th>
                <th className={`${styles.thRight} ${styles.thActs}`}>{'액션'}</th>
              </tr>
            </thead>
            <tbody>
              {album.tracks.map((albumTrack, idx) => {
                const track = albumTrackToTrackListItem(albumTrack);
                return (
                  <TrackRow
                    key={track.id}
                    index={idx + 1}
                    track={track}
                    playing={currentTrack?.id === track.id && isPlayerPlaying}
                    liked={likedIds.has(track.id)}
                    showAuthActions={isAuthenticated}
                    onGuestAction={() => {
                      toast('warning', '로그인이 필요한 기능입니다.');
                      navigate('/login');
                    }}
                    onPlay={(selectedTrack) => {
                      if (currentTrack?.id === selectedTrack.id) {
                        if (isPlayerPlaying) pauseTrack();
                        else resumeTrack();
                      } else {
                        playTrack(albumTrackToPlayerTrack(albumTrack));
                      }
                    }}
                    onLike={(selectedTrack) => {
                      void toggleLike(selectedTrack.id);
                    }}
                    onAddToPlaylist={(selectedTrack) => setAddToPlTrackId(selectedTrack.id)}
                    onDownload={async (selectedTrack) => {
                      try {
                        const blob = await downloadTrack(selectedTrack.id);
                        triggerBlobDownload(blob, `${selectedTrack.title}.mp3`);
                        try {
                          const count = await fetchDownloadCount();
                          toast(
                            'success',
                            `다운로드 완료! 오늘 남은 횟수: ${count.remaining}/${count.dailyLimit}`,
                          );
                        } catch {
                          toast('success', '다운로드가 완료되었습니다.');
                        }
                      } catch (err) {
                        const code = await getApiErrorCode(err);
                        if (code === 'NO_ACTIVE_SUBSCRIPTION') {
                          toast('warning', '구독이 필요한 기능입니다.');
                          navigate('/subscriptions');
                        } else if (code === 'DOWNLOAD_LIMIT_EXCEEDED') {
                          toast('warning', '금일 다운로드 횟수를 모두 사용했습니다.');
                        } else {
                          toast('error', '다운로드에 실패했습니다.');
                        }
                      }
                    }}
                  />
                );
              })}
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

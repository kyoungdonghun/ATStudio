/** Screen B-1: Track detail */
import { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { fetchTrackDetail, type TrackDetail } from '@/api/tracks';
import { usePlayerStore } from '@/store/playerStore';
import { useLikeStore } from '@/store/likeStore';
import { useAuthStore } from '@/store/authStore';
import AddToPlaylistModal from '@/components/playlist/AddToPlaylistModal';
import styles from './TrackDetailPage.module.css';

function formatDate(iso: string): string {
  const d = new Date(iso);
  return `${d.getFullYear()}.${String(d.getMonth() + 1).padStart(2, '0')}.${String(d.getDate()).padStart(2, '0')}`;
}

export default function TrackDetailPage() {
  const { trackId } = useParams<{ trackId: string }>();
  const [track, setTrack] = useState<TrackDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showPlModal, setShowPlModal] = useState(false);

  const playTrack = usePlayerStore((s) => s.play);
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated());
  const likeStore = useLikeStore();

  useEffect(() => {
    if (!trackId) return;

    setLoading(true);
    setError(null);
    fetchTrackDetail(Number(trackId))
      .then(setTrack)
      .catch(() => setError('Failed to load track'))
      .finally(() => setLoading(false));
  }, [trackId]);

  useEffect(() => {
    if (isAuthenticated && !likeStore.loaded) {
      likeStore.load();
    }
  }, [isAuthenticated, likeStore.loaded]);

  if (loading) {
    return (
      <div className={styles.page}>
        <div className={styles.loading}>Loading...</div>
      </div>
    );
  }

  if (error || !track) {
    return (
      <div className={styles.page}>
        <div className={styles.error}>{error ?? 'Track not found'}</div>
      </div>
    );
  }

  const genreTags = track.tags.filter((t) => t.type === 'GENRE');
  const moodTags = track.tags.filter((t) => t.type === 'MOOD');
  const liked = likeStore.likedIds.has(track.id);

  return (
    <div className={styles.page}>
      {/* Breadcrumb */}
      <nav className={styles.breadcrumb}>
        <Link to="/">Home</Link>
        <span>&rsaquo;</span>
        <Link to="/tracks">Track</Link>
        <span>&rsaquo;</span>
        <span className={styles.breadcrumbCurrent}>{track.title}</span>
      </nav>

      <div className={styles.layout}>
        {/* Left: Cover */}
        <div className={styles.coverSection}>
          <div className={styles.cover}>
            {track.thumbnail ? (
              <img
                src={track.thumbnail}
                alt={track.title}
                className={styles.coverImg}
              />
            ) : (
              <span className={styles.coverPlaceholder}>{'\u266A'}</span>
            )}
          </div>

          {/* Actions under cover */}
          <div className={styles.coverActions}>
            <button
              className={styles.btnPlay}
              onClick={() =>
                playTrack({
                  id: track.id,
                  title: track.title,
                  artistName: track.artistName ?? '',
                  duration: track.duration ?? 0,
                  bpm: track.bpm,
                  tonality: track.tonality,
                  description: track.description,
                  audioFile: track.audioFile,
                  thumbnail: track.thumbnail,
                  tags: track.tags as unknown as import('@/types').TagItem[],
                  isActive: track.isActive,
                  playCount: track.playCount,
                  createdAt: track.createdAt,
                  updatedAt: track.updatedAt,
                })
              }
            >
              {'\u25B6'}&nbsp;&nbsp;미리 듣기
            </button>
            <button className={styles.btnBuy}>구매하기</button>
          </div>

          {/* Like + Add to Playlist */}
          <div className={styles.coverSubActions}>
            <button
              className={`${styles.btnSubAction} ${liked ? styles.btnSubActionActive : ''}`}
              onClick={() => likeStore.toggle(track.id)}
            >
              {liked ? '\u2665' : '\u2661'}&nbsp;&nbsp;좋아요
            </button>
            <button
              className={styles.btnSubAction}
              onClick={() => setShowPlModal(true)}
            >
              +&nbsp;&nbsp;재생목록에 추가
            </button>
          </div>
        </div>

        {/* Right: Info */}
        <div className={styles.infoSection}>
          <h1 className={styles.title}>{track.title}</h1>

          {/* Meta grid */}
          <dl className={styles.metaGrid}>
            <div className={styles.metaRow}>
              <dt>BPM</dt>
              <dd>{track.bpm}</dd>
            </div>
            <div className={styles.metaRow}>
              <dt>Key</dt>
              <dd>{track.tonality}</dd>
            </div>
            <div className={styles.metaRow}>
              <dt>재생수</dt>
              <dd>{track.playCount.toLocaleString()}</dd>
            </div>
            <div className={styles.metaRow}>
              <dt>등록일</dt>
              <dd>{formatDate(track.createdAt)}</dd>
            </div>
          </dl>

          {/* Tags */}
          {(genreTags.length > 0 || moodTags.length > 0) && (
            <div className={styles.tagSection}>
              {genreTags.map((t) => (
                <span key={t.id} className={styles.tagChip}>
                  {t.name}
                </span>
              ))}
              {moodTags.map((t) => (
                <span key={t.id} className={styles.tagChip}>
                  {t.name}
                </span>
              ))}
            </div>
          )}

          {/* Description */}
          {track.description && (
            <p className={styles.desc}>{track.description}</p>
          )}

          {/* License info */}
          <div className={styles.licenseSection}>
            <h2 className={styles.sectionLabel}>라이선스 안내</h2>
            <div className={styles.licenseCards}>
              <div className={styles.licenseCard}>
                <div className={styles.licenseType}>개인 라이선스</div>
                <p className={styles.licenseDesc}>
                  개인 채널에서 쇼츠, 브이로그 등 비상업적 콘텐츠에 사용 가능합니다.
                </p>
              </div>
              <div className={styles.licenseCard}>
                <div className={styles.licenseType}>상업 라이선스</div>
                <p className={styles.licenseDesc}>
                  기업/브랜드 채널의 광고, 프로모션 등 상업적 콘텐츠에 사용 가능합니다.
                  Pro 이상 구독 필요.
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <AddToPlaylistModal
        open={showPlModal}
        trackId={track.id}
        onClose={() => setShowPlModal(false)}
      />
    </div>
  );
}

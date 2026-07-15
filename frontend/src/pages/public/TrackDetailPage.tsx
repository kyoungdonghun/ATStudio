/** Screen B-1: Track detail */
import { useEffect, useState } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { fetchTrackDetail, type TrackDetail } from '@/api/tracks';
import { toUploadUrl, getApiErrorCode } from '@/api/client';
import { downloadTrack, triggerBlobDownload, fetchDownloadCount } from '@/api/downloads';
import { usePlayerStore } from '@/store/playerStore';
import { useLikeStore } from '@/store/likeStore';
import { useAuthStore } from '@/store/authStore';
import { useToastStore } from '@/store/toastStore';
import { formatDate } from '@/utils/format';
import AddToPlaylistModal from '@/components/playlist/AddToPlaylistModal';
import styles from './TrackDetailPage.module.css';

export default function TrackDetailPage() {
  const { trackId } = useParams<{ trackId: string }>();
  const [track, setTrack] = useState<TrackDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showPlModal, setShowPlModal] = useState(false);
  const [dlStatus, setDlStatus] = useState<'idle' | 'downloading'>('idle');

  const currentTrack = usePlayerStore((s) => s.currentTrack);
  const isPlayerPlaying = usePlayerStore((s) => s.isPlaying);
  const playTrack = usePlayerStore((s) => s.play);
  const pauseTrack = usePlayerStore((s) => s.pause);
  const resumeTrack = usePlayerStore((s) => s.resume);
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated());
  const likeLoaded = useLikeStore((s) => s.loaded);
  const loadLikes = useLikeStore((s) => s.load);
  const likedIds = useLikeStore((s) => s.likedIds);
  const toggleLike = useLikeStore((s) => s.toggle);
  const toast = useToastStore((s) => s.show);
  const navigate = useNavigate();

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
    if (isAuthenticated && !likeLoaded) {
      void loadLikes();
    }
  }, [isAuthenticated, likeLoaded, loadLikes]);

  async function handleDownload() {
    if (!track) return;
    try {
      setDlStatus('downloading');
      setError(null);
      const blob = await downloadTrack(track.id);
      triggerBlobDownload(blob, `${track.title}.mp3`);
      try {
        const count = await fetchDownloadCount();
        toast('success', `다운로드 완료! 오늘 남은 횟수: ${count.remaining}/${count.dailyLimit}`);
      } catch {
        toast('success', '다운로드가 완료되었습니다.');
      }
    } catch (err: unknown) {
      const code = await getApiErrorCode(err);
      if (code === 'NO_ACTIVE_SUBSCRIPTION') {
        toast('warning', '구독이 필요한 기능입니다.');
        navigate('/subscriptions');
        return;
      }
      if (code === 'DOWNLOAD_LIMIT_EXCEEDED') {
        toast('warning', '금일 다운로드 횟수를 모두 사용했습니다.');
        return;
      }
      setError('다운로드에 실패했습니다.');
    } finally {
      setDlStatus('idle');
    }
  }

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
  const usageTags = track.tags.filter((t) => t.type === 'USAGE');
  const liked = likedIds.has(track.id);
  const playerTrack = {
    id: track.id,
    title: track.title,
    artistName: track.artistName ?? '',
    duration: track.duration ?? 0,
    bpm: track.bpm,
    tonality: track.tonality,
    description: track.description,
    audioFile: track.audioFile,
    thumbnail: track.thumbnail,
    waveformData: track.waveformData,
    tags: track.tags,
    isActive: track.isActive,
    playCount: track.playCount,
    likeCount: track.likeCount,
    downloadCount: track.downloadCount,
    createdAt: track.createdAt,
    updatedAt: track.updatedAt,
  };
  const hasCurrentWaveform = (currentTrack?.waveformData ?? null) === (track.waveformData ?? null);

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
                src={toUploadUrl(track.thumbnail)!}
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
              onClick={() => {
                if (currentTrack?.id === track.id && hasCurrentWaveform) {
                  if (isPlayerPlaying) pauseTrack();
                  else resumeTrack();
                } else {
                  playTrack(playerTrack);
                }
              }}
            >
              {currentTrack?.id === track.id && isPlayerPlaying ? '\u23F8' : '\u25B6'}&nbsp;&nbsp;
              {currentTrack?.id === track.id && isPlayerPlaying ? '일시정지' : '재생'}
            </button>
            {isAuthenticated && (
              <button
                className={styles.btnBuy}
                onClick={handleDownload}
                disabled={dlStatus !== 'idle'}
              >
                {dlStatus === 'downloading' ? '다운로드 중...' : '다운로드'}
              </button>
            )}
          </div>

          {/* Like + Add to Playlist (auth only) */}
          {isAuthenticated && (
            <div className={styles.coverSubActions}>
              <button
                className={`${styles.btnSubAction} ${liked ? styles.btnSubActionActive : ''}`}
                onClick={() => {
                  void toggleLike(track.id);
                }}
              >
                {liked ? '\u2665' : '\u2661'}&nbsp;&nbsp;좋아요
              </button>
              <button className={styles.btnSubAction} onClick={() => setShowPlModal(true)}>
                +&nbsp;&nbsp;재생목록에 추가
              </button>
            </div>
          )}
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
          {(usageTags.length > 0 || genreTags.length > 0 || moodTags.length > 0) && (
            <div className={styles.tagSection}>
              {usageTags.map((t) => (
                <span key={t.id} className={`${styles.tagChip} ${styles.usageChip}`}>
                  {`#${t.name}`}
                </span>
              ))}
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
          {track.description && <p className={styles.desc}>{track.description}</p>}

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
                  기업/브랜드 채널의 광고, 프로모션 등 상업적 콘텐츠에 사용 가능합니다. Pro 이상
                  구독 필요.
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
        onSubscriptionRequired={() => {
          setShowPlModal(false);
          toast('warning', '구독이 필요한 기능입니다.');
          navigate('/subscriptions');
        }}
      />
    </div>
  );
}

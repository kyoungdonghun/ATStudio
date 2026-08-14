/** Screen B-1: Track detail */
import { useEffect, useRef, useState } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { fetchTrackDetail, type TrackDetail } from '@/api/tracks';
import { toUploadUrl, getApiErrorCode } from '@/api/client';
import { classifyLoadError, getLoadErrorMessageForKind, type LoadErrorKind } from '@/api/loadError';
import {
  createDownloadFallbackFileName,
  downloadTrack,
  triggerBlobDownload,
  fetchDownloadCount,
} from '@/api/downloads';
import { usePlayerStore } from '@/store/playerStore';
import { useLikeStore } from '@/store/likeStore';
import { useAuthStore } from '@/store/authStore';
import { useToastStore } from '@/store/toastStore';
import { formatDate } from '@/utils/format';
import AddToPlaylistModal from '@/components/playlist/AddToPlaylistModal';
import CatalogDetailRecovery from '@/components/catalog/CatalogDetailRecovery';
import styles from './TrackDetailPage.module.css';
import { toPlayableTrack } from '@/utils/playableTrack';

export default function TrackDetailPage() {
  const { trackId } = useParams<{ trackId: string }>();
  const [track, setTrack] = useState<TrackDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState<Exclude<LoadErrorKind, 'cancelled'> | null>(null);
  const [showPlModal, setShowPlModal] = useState(false);
  const [dlStatus, setDlStatus] = useState<'idle' | 'downloading'>('idle');
  const downloadOwnershipRef = useRef<{ trackID: number } | null>(null);
  const loadGenerationRef = useRef(0);
  const loadInFlightRef = useRef(false);
  const [retryGeneration, setRetryGeneration] = useState(0);

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
    const parsedTrackId = Number(trackId);
    if (!Number.isSafeInteger(parsedTrackId) || parsedTrackId <= 0) {
      setTrack(null);
      setLoadError('not-found');
      setLoading(false);
      loadInFlightRef.current = false;
      return;
    }

    const generation = ++loadGenerationRef.current;
    const controller = new AbortController();
    loadInFlightRef.current = true;
    setLoading(true);
    setLoadError(null);
    setTrack(null);
    fetchTrackDetail(parsedTrackId, controller.signal)
      .then((nextTrack) => {
        if (loadGenerationRef.current === generation) setTrack(nextTrack);
      })
      .catch((loadError: unknown) => {
        if (
          loadGenerationRef.current === generation &&
          classifyLoadError(loadError) !== 'cancelled'
        ) {
          const kind = classifyLoadError(loadError);
          if (kind !== 'cancelled') setLoadError(kind);
        }
      })
      .finally(() => {
        if (loadGenerationRef.current === generation) {
          setLoading(false);
          loadInFlightRef.current = false;
        }
      });

    return () => {
      controller.abort();
      if (loadGenerationRef.current === generation) loadGenerationRef.current += 1;
    };
  }, [retryGeneration, trackId]);

  function retryLoad() {
    if (loadInFlightRef.current || loading) return;
    loadInFlightRef.current = true;
    setRetryGeneration((generation) => generation + 1);
  }

  useEffect(() => {
    if (isAuthenticated && !likeLoaded) {
      void loadLikes();
    }
  }, [isAuthenticated, likeLoaded, loadLikes]);

  async function handleDownload() {
    if (!track || downloadOwnershipRef.current !== null) return;
    const ownership = { trackID: track.id };
    downloadOwnershipRef.current = ownership;
    try {
      setDlStatus('downloading');
      const download = await downloadTrack(
        track.id,
        createDownloadFallbackFileName('track', track.id, track.title, 'mp3'),
      );
      triggerBlobDownload(download);
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
      toast('error', '다운로드에 실패했습니다.');
    } finally {
      if (downloadOwnershipRef.current === ownership) {
        downloadOwnershipRef.current = null;
        setDlStatus('idle');
      }
    }
  }

  if (loading) {
    return (
      <div className={styles.page}>
        <div className={styles.loading}>음원 정보를 불러오는 중...</div>
      </div>
    );
  }

  if (loadError || !track) {
    const missing = loadError === 'not-found';
    return (
      <div className={styles.page}>
        <CatalogDetailRecovery
          title={missing ? '음원을 찾을 수 없습니다' : '음원 정보를 불러오지 못했습니다'}
          message={
            missing
              ? '삭제되었거나 공개되지 않은 음원입니다.'
              : getLoadErrorMessageForKind(loadError ?? 'unknown', '음원')
          }
          onRetry={retryLoad}
        />
      </div>
    );
  }

  const genreTags = track.tags.filter((t) => t.type === 'GENRE');
  const moodTags = track.tags.filter((t) => t.type === 'MOOD');
  const usageTags = track.tags.filter((t) => t.type === 'USAGE');
  const liked = likedIds.has(track.id);
  const playerTrack = toPlayableTrack(track);
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

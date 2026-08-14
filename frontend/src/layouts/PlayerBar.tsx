import {
  useState,
  useCallback,
  useRef,
  useEffect,
  useMemo,
  type KeyboardEvent as ReactKeyboardEvent,
} from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { getApiErrorCode, toUploadUrl } from '@/api/client';
import { usePlayerStore } from '@/store/playerStore';
import { useLikeStore } from '@/store/likeStore';
import { useAuthStore } from '@/store/authStore';
import { useToastStore } from '@/store/toastStore';
import { fetchMySubscription, isNoActiveSubscriptionError } from '@/api/userSubscriptions';
import {
  createDownloadFallbackFileName,
  downloadTrack,
  triggerBlobDownload,
} from '@/api/downloads';
import HistoryModal from '@/components/player/HistoryModal';
import PlaylistDrawer from '@/components/player/PlaylistDrawer';
import AddToPlaylistModal from '@/components/playlist/AddToPlaylistModal';
import WaveformCanvas from '@/components/player/WaveformCanvas';
import styles from './PlayerBar.module.css';
import { createLoginPath } from '@/utils/loginReturn';
import { clampPlaybackTime, getFiniteMediaDuration } from '@/utils/playbackProgress';

const STALLED_MESSAGE = '재생이 지연되고 있습니다. 연결을 확인한 뒤 다시 시도해 주세요.';
const SEEK_STEP_SECONDS = 5;
const MOBILE_EXPANDED_ID = 'player-mobile-expanded-controls';
type SubscriptionStatus = 'idle' | 'loading' | 'active' | 'inactive' | 'error';

function formatTime(seconds: number): string {
  if (!isFinite(seconds) || seconds < 0) return '0:00';
  const m = Math.floor(seconds / 60);
  const s = Math.floor(seconds % 60);
  return `${m}:${String(s).padStart(2, '0')}`;
}

export default function PlayerBar() {
  const currentTrack = usePlayerStore((s) => s.currentTrack);
  const isPlaying = usePlayerStore((s) => s.isPlaying);
  const isStalled = usePlayerStore((s) => s.isStalled);
  const playbackError = usePlayerStore((s) => s.playbackError);
  const currentTime = usePlayerStore((s) => s.currentTime);
  const duration = usePlayerStore((s) => s.duration);
  const pause = usePlayerStore((s) => s.pause);
  const resume = usePlayerStore((s) => s.resume);
  const next = usePlayerStore((s) => s.next);
  const prev = usePlayerStore((s) => s.prev);
  const seek = usePlayerStore((s) => s.seek);
  const hydratePersistedState = usePlayerStore((s) => s.hydratePersistedState);

  const volume = usePlayerStore((s) => s.volume);
  const muted = usePlayerStore((s) => s.muted);
  const setVolume = usePlayerStore((s) => s.setVolume);
  const toggleMute = usePlayerStore((s) => s.toggleMute);

  const isAuthenticated = useAuthStore((s) => s.isAuthenticated());
  const role = useAuthStore((s) => s.role);
  const authIdentity = useAuthStore((s) => s.user?.id ?? s.accessToken ?? null);
  const likeLoaded = useLikeStore((s) => s.loaded);
  const loadLikes = useLikeStore((s) => s.load);
  const likedIds = useLikeStore((s) => s.likedIds);
  const toggleLike = useLikeStore((s) => s.toggle);
  const toast = useToastStore((s) => s.show);

  const shuffle = usePlayerStore((s) => s.shuffle);
  const repeat = usePlayerStore((s) => s.repeat);
  const toggleShuffle = usePlayerStore((s) => s.toggleShuffle);
  const cycleRepeat = usePlayerStore((s) => s.cycleRepeat);
  const [historyOpen, setHistoryOpen] = useState(false);
  const [playlistOpen, setPlaylistOpen] = useState(false);
  const [showPlModal, setShowPlModal] = useState(false);
  const [expanded, setExpanded] = useState(false);
  const [subscriptionStatus, setSubscriptionStatus] = useState<SubscriptionStatus>('idle');
  const [subscriptionRetryKey, setSubscriptionRetryKey] = useState(0);
  const subscriptionGenerationRef = useRef(0);
  const [downloading, setDownloading] = useState(false);
  const downloadOwnershipRef = useRef<{ trackID: number } | null>(null);
  const [volumeOpen, setVolumeOpen] = useState(false);
  const mobileMiniProgressRef = useRef<HTMLDivElement>(null);
  const mobileExpandButtonRef = useRef<HTMLButtonElement>(null);
  const volumeRef = useRef<HTMLDivElement>(null);
  const navigate = useNavigate();
  const location = useLocation();
  const loginPath = createLoginPath(location);

  function handleMobileKeyDown(event: ReactKeyboardEvent<HTMLDivElement>) {
    if (event.key !== 'Escape' || event.defaultPrevented || !expanded) return;

    const target = event.target;
    if (!(target instanceof Element) || !event.currentTarget.contains(target)) return;
    if (target.closest('dialog, [role="dialog"], [aria-modal="true"]')) return;

    event.preventDefault();
    setExpanded(false);
    mobileExpandButtonRef.current?.focus();
  }

  useEffect(() => {
    void hydratePersistedState?.();
  }, [hydratePersistedState]);

  // Close volume popup when clicking outside
  useEffect(() => {
    if (!volumeOpen) return;
    function onOutside(e: MouseEvent) {
      if (volumeRef.current && !volumeRef.current.contains(e.target as Node)) {
        setVolumeOpen(false);
      }
    }
    document.addEventListener('mousedown', onOutside);
    return () => document.removeEventListener('mousedown', onOutside);
  }, [volumeOpen]);

  useEffect(() => {
    if (isAuthenticated && !likeLoaded) {
      void loadLikes();
    }
  }, [isAuthenticated, likeLoaded, loadLikes]);

  useEffect(() => {
    if (!isAuthenticated || role === 'ADMIN') {
      subscriptionGenerationRef.current += 1;
      setSubscriptionStatus('idle');
      return;
    }

    const generation = ++subscriptionGenerationRef.current;
    const controller = new AbortController();
    setSubscriptionStatus('loading');
    fetchMySubscription(controller.signal)
      .then(() => {
        if (subscriptionGenerationRef.current === generation) {
          setSubscriptionStatus('active');
        }
      })
      .catch((error: unknown) => {
        if (subscriptionGenerationRef.current !== generation || controller.signal.aborted) return;
        setSubscriptionStatus(isNoActiveSubscriptionError(error) ? 'inactive' : 'error');
      });

    return () => {
      controller.abort();
      if (subscriptionGenerationRef.current === generation) {
        subscriptionGenerationRef.current += 1;
      }
    };
  }, [authIdentity, isAuthenticated, role, subscriptionRetryKey]);

  const hasSubscription = subscriptionStatus === 'active';
  const renderSubscriptionAction = (guestTarget: '/login' | '/subscriptions') => {
    if (!isAuthenticated) {
      return (
        <button className={styles.buyBtn} onClick={() => navigate(guestTarget)}>
          {'구독하기'}
        </button>
      );
    }
    if (role !== 'USER') return null;
    if (subscriptionStatus === 'inactive') {
      return (
        <button className={styles.buyBtn} onClick={() => navigate('/subscriptions')}>
          {'구독하기'}
        </button>
      );
    }
    if (subscriptionStatus === 'error') {
      return (
        <button
          className={styles.buyBtn}
          onClick={() => setSubscriptionRetryKey((value) => value + 1)}
        >
          {'구독 상태 다시 확인'}
        </button>
      );
    }
    return null;
  };

  const handlePlayPause = useCallback(() => {
    if (isPlaying) pause();
    else resume();
  }, [isPlaying, pause, resume]);

  // Parse waveform peaks from currentTrack.waveformData
  const parsedPeaks = useMemo<number[]>(() => {
    if (!currentTrack?.waveformData) return [];
    try {
      return JSON.parse(currentTrack.waveformData) as number[];
    } catch {
      return [];
    }
  }, [currentTrack?.waveformData]);

  const trackDuration = getFiniteMediaDuration(duration, currentTrack?.duration ?? 0);
  const boundedCurrentTime = clampPlaybackTime(currentTime, trackDuration);
  const progressRatio = trackDuration > 0 ? boundedCurrentTime / trackDuration : 0;

  const handleSeek = useCallback(
    (ratio: number) => {
      seek(ratio * trackDuration);
    },
    [seek, trackDuration],
  );

  const handleDownload = useCallback(async () => {
    if (!currentTrack || downloading || downloadOwnershipRef.current !== null) return;
    const ownership = { trackID: currentTrack.id };
    downloadOwnershipRef.current = ownership;
    setDownloading(true);
    try {
      const download = await downloadTrack(
        currentTrack.id,
        createDownloadFallbackFileName('track', currentTrack.id, currentTrack.title, 'mp3'),
      );
      triggerBlobDownload(download);
      toast('success', '다운로드가 완료되었습니다.');
    } catch (err: unknown) {
      const code = await getApiErrorCode(err);
      if (code === 'NO_ACTIVE_SUBSCRIPTION') {
        toast('warning', '구독이 필요한 기능입니다.');
        navigate('/subscriptions');
      } else if (code === 'DOWNLOAD_LIMIT_EXCEEDED') {
        toast('warning', '금일 다운로드 횟수를 모두 사용했습니다.');
      } else {
        toast('error', '다운로드에 실패했습니다.');
      }
    } finally {
      if (downloadOwnershipRef.current === ownership) {
        downloadOwnershipRef.current = null;
        setDownloading(false);
      }
    }
  }, [currentTrack, downloading, navigate, toast]);

  const handleMobileMiniProgressClick = useCallback(
    (e: React.MouseEvent<HTMLElement>) => {
      if (!mobileMiniProgressRef.current || !trackDuration) return;
      const rect = mobileMiniProgressRef.current.getBoundingClientRect();
      const ratio = Math.max(0, Math.min(1, (e.clientX - rect.left) / rect.width));
      seek(ratio * trackDuration);
    },
    [trackDuration, seek],
  );

  const handleSeekKeyDown = useCallback(
    (e: React.KeyboardEvent<HTMLElement>) => {
      if (!trackDuration) return;

      let targetTime: number | null = null;
      if (e.key === 'ArrowRight' || e.key === 'ArrowUp') {
        targetTime = boundedCurrentTime + SEEK_STEP_SECONDS;
      } else if (e.key === 'ArrowLeft' || e.key === 'ArrowDown') {
        targetTime = boundedCurrentTime - SEEK_STEP_SECONDS;
      } else if (e.key === 'Home') {
        targetTime = 0;
      } else if (e.key === 'End') {
        targetTime = trackDuration;
      }

      if (targetTime === null) return;
      e.preventDefault();
      seek(Math.max(0, Math.min(trackDuration, targetTime)));
    },
    [boundedCurrentTime, seek, trackDuration],
  );

  /* ── Repeat icon SVG helper ── */
  const repeatIcon =
    repeat === 'one' ? (
      <svg
        width="16"
        height="16"
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        strokeWidth="1.3"
        strokeLinecap="round"
        strokeLinejoin="round"
      >
        <polyline points="17 1 21 5 17 9" />
        <path d="M3 11V9a4 4 0 0 1 4-4h14" />
        <polyline points="7 23 3 19 7 15" />
        <path d="M21 13v2a4 4 0 0 1-4 4H3" />
        <text
          x="12"
          y="14.5"
          textAnchor="middle"
          fontSize="8"
          fill="currentColor"
          stroke="none"
          fontWeight="bold"
        >
          1
        </text>
      </svg>
    ) : (
      <svg
        width="16"
        height="16"
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        strokeWidth="1.3"
        strokeLinecap="round"
        strokeLinejoin="round"
      >
        <polyline points="17 1 21 5 17 9" />
        <path d="M3 11V9a4 4 0 0 1 4-4h14" />
        <polyline points="7 23 3 19 7 15" />
        <path d="M21 13v2a4 4 0 0 1-4 4H3" />
      </svg>
    );

  /* ── Volume icon helper ── */
  const volumeIcon =
    muted || volume === 0 ? (
      <svg
        width="16"
        height="16"
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        strokeWidth="2"
        strokeLinecap="round"
        strokeLinejoin="round"
      >
        <polygon points="11 5 6 9 2 9 2 15 6 15 11 19 11 5" />
        <line x1="23" y1="9" x2="17" y2="15" />
        <line x1="17" y1="9" x2="23" y2="15" />
      </svg>
    ) : volume < 0.5 ? (
      <svg
        width="16"
        height="16"
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        strokeWidth="2"
        strokeLinecap="round"
        strokeLinejoin="round"
      >
        <polygon points="11 5 6 9 2 9 2 15 6 15 11 19 11 5" />
        <path d="M15.54 8.46a5 5 0 0 1 0 7.07" />
      </svg>
    ) : (
      <svg
        width="16"
        height="16"
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        strokeWidth="2"
        strokeLinecap="round"
        strokeLinejoin="round"
      >
        <polygon points="11 5 6 9 2 9 2 15 6 15 11 19 11 5" />
        <path d="M15.54 8.46a5 5 0 0 1 0 7.07" />
        <path d="M19.07 4.93a10 10 0 0 1 0 14.14" />
      </svg>
    );

  /* ── Empty state (no track loaded) ── */
  if (!currentTrack) {
    const isFullUser = role === 'ADMIN' || hasSubscription;

    /* 비로그인 / 비구독 회원: 미니멀 바 */
    if (!isFullUser) {
      return (
        <>
          {/* Desktop */}
          <div className={styles.player}>
            <div className={styles.trackInfo}>
              <div className={styles.thumb}>{'\u266B'}</div>
              <div className={styles.trackMeta}>
                <span className={styles.trackName}>재생할 곡을 선택하세요</span>
              </div>
            </div>
            <div style={{ flex: 1 }} />
            <div className={styles.rightActions}>
              <div className={styles.volumeGroup} ref={volumeRef}>
                <button
                  type="button"
                  className={`${styles.volumeBtn} ${volumeOpen ? styles.volumeBtnActive : ''}`}
                  onClick={() => setVolumeOpen((v) => !v)}
                  aria-label={volumeOpen ? '볼륨 설정 닫기' : '볼륨 설정 열기'}
                  aria-expanded={volumeOpen}
                  title={volumeOpen ? '볼륨 설정 닫기' : '볼륨 설정 열기'}
                >
                  {volumeIcon}
                </button>
                {volumeOpen && (
                  <div className={styles.volumePopup}>
                    <span className={styles.volumePercent}>
                      {Math.round((muted ? 0 : volume) * 100)}
                    </span>
                    <div className={styles.volumeSliderWrap}>
                      <input
                        type="range"
                        className={styles.volumeSliderVertical}
                        min={0}
                        max={1}
                        step={0.01}
                        value={muted ? 0 : volume}
                        onChange={(e) => setVolume(parseFloat(e.target.value))}
                        aria-label="볼륨"
                      />
                    </div>
                    <button
                      type="button"
                      className={styles.volumeMuteSmall}
                      onClick={toggleMute}
                      aria-label={muted ? '음소거 해제' : '음소거'}
                      title={muted ? '음소거 해제' : '음소거'}
                    >
                      {muted ? '\uD83D\uDD07' : '\uD83D\uDD0A'}
                    </button>
                  </div>
                )}
              </div>
              {renderSubscriptionAction('/subscriptions')}
            </div>
          </div>

          {/* Mobile */}
          <div className={styles.mobilePlayer}>
            <div className={styles.mobileBar}>
              <div className={styles.mobileInfo}>
                <div className={styles.thumb}>{'\u266B'}</div>
                <div className={styles.trackMeta}>
                  <span className={styles.trackName}>재생할 곡을 선택하세요</span>
                </div>
              </div>
              <div className={styles.mobileControls}>
                {renderSubscriptionAction('/subscriptions')}
              </div>
            </div>
          </div>
        </>
      );
    }

    /* ADMIN / 구독 회원: 풀 빈 상태 바 */
    return (
      <>
        <div className={styles.player}>
          <div className={styles.trackInfo}>
            <div className={styles.thumb}>{'\u266B'}</div>
            <div className={styles.trackMeta}>
              <span className={styles.trackName}>재생할 곡을 선택하세요</span>
            </div>
          </div>
          <div className={styles.controls}>
            <button
              type="button"
              className={styles.ctrlBtn}
              disabled
              style={{ opacity: 0.3 }}
              aria-label="이전 곡"
            >
              {'\u23EE'}
            </button>
            <button
              type="button"
              className={styles.playBtn}
              disabled
              style={{ opacity: 0.4 }}
              aria-label="재생"
            >
              {'\u25B6'}
            </button>
            <button
              type="button"
              className={styles.ctrlBtn}
              disabled
              style={{ opacity: 0.3 }}
              aria-label="다음 곡"
            >
              {'\u23ED'}
            </button>
            <div className={styles.waveformWrap}>
              <WaveformCanvas peaks={[]} progress={0} onSeek={() => {}} />
            </div>
            <div className={styles.timeDisplay}>
              <span>0:00</span>
              <span className={styles.timeSep}>/</span>
              <span>0:00</span>
            </div>
          </div>
          <div className={styles.rightActions}>
            <div className={styles.modeGroup}>
              <button
                type="button"
                className={styles.ctrlBtn}
                disabled
                style={{ opacity: 0.3 }}
                aria-label="셔플"
              >
                {'\u21CC'}
              </button>
              <button
                type="button"
                className={styles.ctrlBtn}
                disabled
                style={{ opacity: 0.3 }}
                aria-label="반복 재생"
              >
                {repeatIcon}
              </button>
            </div>
            <div className={styles.volumeGroup} ref={volumeRef}>
              <button
                type="button"
                className={`${styles.volumeBtn} ${volumeOpen ? styles.volumeBtnActive : ''}`}
                onClick={() => setVolumeOpen((v) => !v)}
                aria-label={volumeOpen ? '볼륨 설정 닫기' : '볼륨 설정 열기'}
                aria-expanded={volumeOpen}
                title={volumeOpen ? '볼륨 설정 닫기' : '볼륨 설정 열기'}
              >
                {volumeIcon}
              </button>
              {volumeOpen && (
                <div className={styles.volumePopup}>
                  <span className={styles.volumePercent}>
                    {Math.round((muted ? 0 : volume) * 100)}
                  </span>
                  <div className={styles.volumeSliderWrap}>
                    <input
                      type="range"
                      className={styles.volumeSliderVertical}
                      min={0}
                      max={1}
                      step={0.01}
                      value={muted ? 0 : volume}
                      onChange={(e) => setVolume(parseFloat(e.target.value))}
                      aria-label="볼륨"
                    />
                  </div>
                  <button
                    type="button"
                    className={styles.volumeMuteSmall}
                    onClick={toggleMute}
                    aria-label={muted ? '음소거 해제' : '음소거'}
                    title={muted ? '음소거 해제' : '음소거'}
                  >
                    {muted ? '\uD83D\uDD07' : '\uD83D\uDD0A'}
                  </button>
                </div>
              )}
            </div>
            <button
              className={`${styles.actionBtn} ${historyOpen ? styles.actionBtnActive : ''}`}
              onClick={() => {
                setHistoryOpen((v) => !v);
                setPlaylistOpen(false);
              }}
            >
              {'재생기록'}
            </button>
            <button
              className={`${styles.actionBtn} ${playlistOpen ? styles.actionBtnActive : ''}`}
              onClick={() => {
                setPlaylistOpen((v) => !v);
                setHistoryOpen(false);
              }}
            >
              {'재생목록'}
            </button>
          </div>
        </div>

        {/* Mobile empty state */}
        <div className={styles.mobilePlayer} onKeyDown={handleMobileKeyDown}>
          <div className={styles.mobileBar}>
            <div className={styles.mobileInfo}>
              <div className={styles.thumb}>{'\u266B'}</div>
              <div className={styles.trackMeta}>
                <span className={styles.trackName}>재생할 곡을 선택하세요</span>
              </div>
            </div>
            <div className={styles.mobileControls}>
              <button
                type="button"
                ref={mobileExpandButtonRef}
                className={styles.mobileExpandBtn}
                onClick={() => setExpanded((v) => !v)}
                aria-label={expanded ? '플레이어 상세 접기' : '플레이어 상세 펼치기'}
                aria-expanded={expanded}
                aria-controls={expanded ? MOBILE_EXPANDED_ID : undefined}
              >
                {expanded ? '\u25BC' : '\u25B2'}
              </button>
            </div>
          </div>
          {expanded && (
            <div
              id={MOBILE_EXPANDED_ID}
              className={`${styles.mobileExpanded} ${styles.mobileExpandedOpen}`}
            >
              <div className={styles.mobileActions}>
                <button
                  className={`${styles.actionBtn} ${historyOpen ? styles.actionBtnActive : ''}`}
                  onClick={() => {
                    setHistoryOpen((v) => !v);
                    setPlaylistOpen(false);
                  }}
                >
                  {'재생기록'}
                </button>
                <button
                  className={`${styles.actionBtn} ${playlistOpen ? styles.actionBtnActive : ''}`}
                  onClick={() => {
                    setPlaylistOpen((v) => !v);
                    setHistoryOpen(false);
                  }}
                >
                  {'재생목록'}
                </button>
              </div>
            </div>
          )}
        </div>

        {historyOpen && <HistoryModal open={historyOpen} onClose={() => setHistoryOpen(false)} />}
        {playlistOpen && (
          <PlaylistDrawer open={playlistOpen} onClose={() => setPlaylistOpen(false)} />
        )}
      </>
    );
  }

  const progressPercent = progressRatio * 100;
  const currentUsageText = (currentTrack.tags ?? [])
    .filter((tag) => tag.type === 'USAGE')
    .map((tag) => `#${tag.name}`)
    .join(' ');
  const playbackFeedback = playbackError ?? (isStalled ? STALLED_MESSAGE : null);
  const repeatLabel =
    repeat === 'one'
      ? '한 곡 반복 사용 중'
      : repeat === 'all'
        ? '전체 반복 사용 중'
        : '반복 재생 사용 안 함';

  return (
    <>
      {playbackFeedback && (
        <div
          className={`${styles.playbackFeedback} ${playbackError ? styles.playbackFeedbackError : ''}`}
          role={playbackError ? 'alert' : 'status'}
          aria-live={playbackError ? 'assertive' : 'polite'}
        >
          <span>{playbackFeedback}</span>
          <button type="button" className={styles.retryButton} onClick={resume}>
            다시 시도
          </button>
        </div>
      )}

      {/* ── Desktop / Tablet: full bar ── */}
      <div className={styles.player}>
        {/* Left: Track info */}
        <div className={styles.trackInfo}>
          <button
            type="button"
            className={`${styles.thumb} ${styles.trackThumbButton}`}
            onClick={() => navigate(`/tracks/${currentTrack.id}`)}
            aria-label={`${currentTrack.title} 상세 보기`}
            title="트랙 상세 보기"
          >
            {currentTrack.thumbnail ? (
              <img src={toUploadUrl(currentTrack.thumbnail)!} alt="" />
            ) : (
              '\u266B'
            )}
          </button>
          <div className={styles.trackMeta}>
            <button
              type="button"
              className={`${styles.trackName} ${styles.trackNameButton}`}
              onClick={() => navigate(`/tracks/${currentTrack.id}`)}
              aria-label={`${currentTrack.title} 상세 보기`}
              title={`${currentTrack.title} 상세 보기`}
            >
              {currentTrack.title}
            </button>
            {currentUsageText && <div className={styles.trackUsage}>{currentUsageText}</div>}
          </div>
          <button
            className={`${styles.heartBtn} ${likedIds.has(currentTrack.id) ? styles.heartBtnActive : ''}`}
            aria-label={likedIds.has(currentTrack.id) ? '좋아요 취소' : '좋아요'}
            aria-pressed={likedIds.has(currentTrack.id)}
            title={likedIds.has(currentTrack.id) ? '좋아요 취소' : '좋아요'}
            onClick={() => {
              if (!isAuthenticated) {
                toast('warning', '로그인 후 이용 가능합니다.');
                navigate(loginPath);
                return;
              }
              void toggleLike(currentTrack.id);
            }}
          >
            {likedIds.has(currentTrack.id) ? '\u2665' : '\u2661'}
          </button>
          <button
            className={styles.addToPlBtn}
            aria-label="재생목록에 추가"
            onClick={() => {
              if (!isAuthenticated) {
                toast('warning', '로그인 후 이용 가능합니다.');
                navigate(loginPath);
                return;
              }
              setShowPlModal(true);
            }}
            title="재생목록에 추가"
          >
            +
          </button>
        </div>

        {/* Center: Prev + Play + Next + Waveform + Time */}
        <div className={styles.controls}>
          <button
            type="button"
            className={styles.ctrlBtn}
            onClick={prev}
            aria-label="이전 곡"
            title="이전 곡"
          >
            {'\u23EE'}
          </button>
          <button
            className={styles.playBtn}
            onClick={handlePlayPause}
            aria-label={isPlaying ? '일시정지' : '재생'}
            title={isPlaying ? '일시정지' : '재생'}
          >
            {isPlaying ? '\u275A\u275A' : '\u25B6'}
          </button>
          <button
            type="button"
            className={styles.ctrlBtn}
            onClick={next}
            aria-label="다음 곡"
            title="다음 곡"
          >
            {'\u23ED'}
          </button>
          <div
            className={styles.waveformWrap}
            role="slider"
            tabIndex={0}
            aria-label="재생 위치"
            aria-valuemin={0}
            aria-valuemax={trackDuration}
            aria-valuenow={boundedCurrentTime}
            aria-valuetext={`${formatTime(boundedCurrentTime)} / ${formatTime(trackDuration)}`}
            onKeyDown={handleSeekKeyDown}
          >
            <WaveformCanvas peaks={parsedPeaks} progress={progressRatio} onSeek={handleSeek} />
          </div>
          <div className={styles.timeDisplay}>
            <span>{formatTime(boundedCurrentTime)}</span>
            <span className={styles.timeSep}>/</span>
            <span>{formatTime(trackDuration)}</span>
          </div>
        </div>

        {/* Right: Mode + Volume(popup) + Actions */}
        <div className={styles.rightActions}>
          <div className={styles.modeGroup}>
            <button
              type="button"
              className={`${styles.ctrlBtn} ${shuffle ? styles.ctrlBtnActive : ''}`}
              onClick={toggleShuffle}
              aria-label={shuffle ? '셔플 사용 중' : '셔플 사용 안 함'}
              aria-pressed={shuffle}
              title={shuffle ? '셔플 끄기' : '셔플 켜기'}
            >
              {'\u21CC'}
            </button>
            <button
              type="button"
              className={`${styles.ctrlBtn} ${repeat !== 'off' ? styles.ctrlBtnActive : ''}`}
              onClick={cycleRepeat}
              aria-label={repeatLabel}
              title="반복 모드 변경"
            >
              {repeatIcon}
            </button>
          </div>
          <div className={styles.volumeGroup} ref={volumeRef}>
            <button
              type="button"
              className={`${styles.volumeBtn} ${volumeOpen ? styles.volumeBtnActive : ''}`}
              onClick={() => setVolumeOpen((v) => !v)}
              aria-label={volumeOpen ? '볼륨 설정 닫기' : '볼륨 설정 열기'}
              aria-expanded={volumeOpen}
              title={volumeOpen ? '볼륨 설정 닫기' : '볼륨 설정 열기'}
            >
              {volumeIcon}
            </button>
            {volumeOpen && (
              <div className={styles.volumePopup}>
                <span className={styles.volumePercent}>
                  {Math.round((muted ? 0 : volume) * 100)}
                </span>
                <div className={styles.volumeSliderWrap}>
                  <input
                    type="range"
                    className={styles.volumeSliderVertical}
                    min={0}
                    max={1}
                    step={0.01}
                    value={muted ? 0 : volume}
                    onChange={(e) => setVolume(parseFloat(e.target.value))}
                    aria-label="볼륨"
                  />
                </div>
                <button
                  type="button"
                  className={styles.volumeMuteSmall}
                  onClick={toggleMute}
                  aria-label={muted ? '음소거 해제' : '음소거'}
                  title={muted ? '음소거 해제' : '음소거'}
                >
                  {muted ? '\uD83D\uDD07' : '\uD83D\uDD0A'}
                </button>
              </div>
            )}
          </div>
          <button
            className={`${styles.actionBtn} ${historyOpen ? styles.actionBtnActive : ''}`}
            onClick={() => {
              setHistoryOpen((v) => !v);
              setPlaylistOpen(false);
            }}
          >
            {'재생기록'}
          </button>
          <button
            className={`${styles.actionBtn} ${playlistOpen ? styles.actionBtnActive : ''}`}
            onClick={() => {
              setPlaylistOpen((v) => !v);
              setHistoryOpen(false);
            }}
          >
            {'재생목록'}
          </button>
          {(role === 'ADMIN' || (isAuthenticated && hasSubscription)) && (
            <button
              className={styles.downloadBtn}
              onClick={handleDownload}
              disabled={downloading}
              title="음원 다운로드"
            >
              {downloading ? '...' : '다운로드'}
            </button>
          )}
          {renderSubscriptionAction('/login')}
        </div>
      </div>

      {/* ── Mobile: mini bar ── */}
      <div className={styles.mobilePlayer} onKeyDown={handleMobileKeyDown}>
        {/* Thin progress indicator at top of mini bar */}
        <div
          className={styles.mobileProgressTrack}
          onClick={handleMobileMiniProgressClick}
          onKeyDown={handleSeekKeyDown}
          ref={mobileMiniProgressRef}
          role="slider"
          tabIndex={0}
          aria-label="모바일 재생 위치"
          aria-valuemin={0}
          aria-valuemax={trackDuration}
          aria-valuenow={boundedCurrentTime}
          aria-valuetext={`${formatTime(boundedCurrentTime)} / ${formatTime(trackDuration)}`}
        >
          <div className={styles.mobileProgressFill} style={{ width: `${progressPercent}%` }} />
        </div>

        <div className={styles.mobileBar}>
          <div className={styles.mobileInfo}>
            <button
              type="button"
              className={`${styles.thumb} ${styles.trackThumbButton}`}
              onClick={() => navigate(`/tracks/${currentTrack.id}`)}
              aria-label={`${currentTrack.title} 상세 보기`}
              title="트랙 상세 보기"
            >
              {currentTrack.thumbnail ? (
                <img src={toUploadUrl(currentTrack.thumbnail)!} alt="" />
              ) : (
                '\u266B'
              )}
            </button>
            <div className={styles.trackMeta}>
              <button
                type="button"
                className={`${styles.trackName} ${styles.trackNameButton}`}
                onClick={() => navigate(`/tracks/${currentTrack.id}`)}
                aria-label={`${currentTrack.title} 상세 보기`}
                title={`${currentTrack.title} 상세 보기`}
              >
                {currentTrack.title}
              </button>
              {currentUsageText && <div className={styles.trackUsage}>{currentUsageText}</div>}
            </div>
          </div>
          <div className={styles.mobileControls}>
            {!expanded && (
              <>
                <button
                  className={`${styles.heartBtn} ${likedIds.has(currentTrack.id) ? styles.heartBtnActive : ''}`}
                  aria-label={likedIds.has(currentTrack.id) ? '좋아요 취소' : '좋아요'}
                  aria-pressed={likedIds.has(currentTrack.id)}
                  title={likedIds.has(currentTrack.id) ? '좋아요 취소' : '좋아요'}
                  onClick={() => {
                    if (!isAuthenticated) {
                      toast('warning', '로그인 후 이용 가능합니다.');
                      navigate(loginPath);
                      return;
                    }
                    void toggleLike(currentTrack.id);
                  }}
                >
                  {likedIds.has(currentTrack.id) ? '\u2665' : '\u2661'}
                </button>
                <button
                  className={styles.playBtn}
                  onClick={handlePlayPause}
                  aria-label={isPlaying ? '일시정지' : '재생'}
                  title={isPlaying ? '일시정지' : '재생'}
                >
                  {isPlaying ? '\u275A\u275A' : '\u25B6'}
                </button>
              </>
            )}
            <button
              ref={mobileExpandButtonRef}
              className={styles.mobileExpandBtn}
              onClick={() => setExpanded((v) => !v)}
              aria-label={expanded ? '플레이어 상세 접기' : '플레이어 상세 펼치기'}
              aria-expanded={expanded}
              aria-controls={expanded ? MOBILE_EXPANDED_ID : undefined}
              title={expanded ? '플레이어 상세 접기' : '플레이어 상세 펼치기'}
            >
              {expanded ? '\u25BC' : '\u25B2'}
            </button>
          </div>
        </div>

        {/* Expanded panel */}
        {expanded && (
          <div
            id={MOBILE_EXPANDED_ID}
            className={`${styles.mobileExpanded} ${styles.mobileExpandedOpen}`}
          >
            {/* Transport controls */}
            <div className={styles.mobileFullControls}>
              <button
                type="button"
                className={`${styles.ctrlBtn} ${shuffle ? styles.ctrlBtnActive : ''}`}
                onClick={toggleShuffle}
                aria-label={shuffle ? '셔플 사용 중' : '셔플 사용 안 함'}
                aria-pressed={shuffle}
                title={shuffle ? '셔플 끄기' : '셔플 켜기'}
              >
                {'\u21CC'}
              </button>
              <button
                type="button"
                className={styles.ctrlBtn}
                onClick={prev}
                aria-label="이전 곡"
                title="이전 곡"
              >
                {'\u23EE'}
              </button>
              <button
                type="button"
                className={styles.playBtn}
                onClick={handlePlayPause}
                aria-label={isPlaying ? '일시정지' : '재생'}
                title={isPlaying ? '일시정지' : '재생'}
              >
                {isPlaying ? '\u275A\u275A' : '\u25B6'}
              </button>
              <button
                type="button"
                className={styles.ctrlBtn}
                onClick={next}
                aria-label="다음 곡"
                title="다음 곡"
              >
                {'\u23ED'}
              </button>
              <button
                type="button"
                className={`${styles.ctrlBtn} ${repeat !== 'off' ? styles.ctrlBtnActive : ''}`}
                onClick={cycleRepeat}
                aria-label={repeatLabel}
                title="반복 모드 변경"
              >
                {repeatIcon}
              </button>
            </div>

            {/* Waveform seek (replaces plain progress bar) */}
            <div className={styles.mobileWaveform}>
              <span className={styles.mobileSeekTime}>{formatTime(boundedCurrentTime)}</span>
              <div
                className={styles.mobileWaveformWrap}
                role="slider"
                tabIndex={0}
                aria-label="모바일 상세 재생 위치"
                aria-valuemin={0}
                aria-valuemax={trackDuration}
                aria-valuenow={boundedCurrentTime}
                aria-valuetext={`${formatTime(boundedCurrentTime)} / ${formatTime(trackDuration)}`}
                onKeyDown={handleSeekKeyDown}
              >
                <WaveformCanvas
                  peaks={parsedPeaks}
                  progress={progressRatio}
                  onSeek={handleSeek}
                  height={32}
                />
              </div>
              <span className={styles.mobileSeekTime}>{formatTime(trackDuration)}</span>
            </div>

            {/* Volume */}
            <div className={styles.mobileVolumeToggle}>
              <button
                className={styles.volumeBtn}
                onClick={toggleMute}
                aria-label={muted ? '음소거 해제' : '음소거'}
                title={muted ? '음소거 해제' : '음소거'}
              >
                {volumeIcon}
              </button>
              <input
                type="range"
                className={styles.mobileVolumeSlider}
                min={0}
                max={1}
                step={0.01}
                value={muted ? 0 : volume}
                onChange={(e) => setVolume(parseFloat(e.target.value))}
                aria-label="볼륨"
              />
            </div>

            {/* Action buttons */}
            <div className={styles.mobileActions}>
              <button
                className={`${styles.actionBtn} ${historyOpen ? styles.actionBtnActive : ''}`}
                onClick={() => {
                  setHistoryOpen((v) => !v);
                  setPlaylistOpen(false);
                }}
              >
                {'재생기록'}
              </button>
              <button
                className={`${styles.actionBtn} ${playlistOpen ? styles.actionBtnActive : ''}`}
                onClick={() => {
                  setPlaylistOpen((v) => !v);
                  setHistoryOpen(false);
                }}
              >
                {'재생목록'}
              </button>
              {(role === 'ADMIN' || (isAuthenticated && hasSubscription)) && (
                <button
                  className={styles.downloadBtn}
                  onClick={handleDownload}
                  disabled={downloading}
                  title="음원 다운로드"
                >
                  {downloading ? '...' : '\u2193 다운로드'}
                </button>
              )}
              {renderSubscriptionAction('/login')}
            </div>
          </div>
        )}
      </div>

      {/* Modals / Drawers */}
      <HistoryModal open={historyOpen} onClose={() => setHistoryOpen(false)} />
      <PlaylistDrawer open={playlistOpen} onClose={() => setPlaylistOpen(false)} />
      <AddToPlaylistModal
        open={showPlModal}
        trackId={currentTrack?.id ?? null}
        onClose={() => setShowPlModal(false)}
        onSubscriptionRequired={() => {
          setShowPlModal(false);
          toast('warning', '구독이 필요한 기능입니다.');
          navigate('/subscriptions');
        }}
      />
    </>
  );
}

import { useState, useCallback, useRef, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { toUploadUrl } from '@/api/client';
import { usePlayerStore } from '@/store/playerStore';
import { useLikeStore } from '@/store/likeStore';
import { useAuthStore } from '@/store/authStore';
import { useToastStore } from '@/store/toastStore';
import QueueModal from '@/components/player/QueueModal';
import PlaylistDrawer from '@/components/player/PlaylistDrawer';
import AddToPlaylistModal from '@/components/playlist/AddToPlaylistModal';
import styles from './PlayerBar.module.css';

function formatTime(seconds: number): string {
  if (!isFinite(seconds) || seconds < 0) return '0:00';
  const m = Math.floor(seconds / 60);
  const s = Math.floor(seconds % 60);
  return `${m}:${String(s).padStart(2, '0')}`;
}

export default function PlayerBar() {
  const currentTrack = usePlayerStore((s) => s.currentTrack);
  const isPlaying = usePlayerStore((s) => s.isPlaying);
  const currentTime = usePlayerStore((s) => s.currentTime);
  const duration = usePlayerStore((s) => s.duration);
  const pause = usePlayerStore((s) => s.pause);
  const resume = usePlayerStore((s) => s.resume);
  const next = usePlayerStore((s) => s.next);
  const prev = usePlayerStore((s) => s.prev);
  const seek = usePlayerStore((s) => s.seek);

  const volume = usePlayerStore((s) => s.volume);
  const muted = usePlayerStore((s) => s.muted);
  const setVolume = usePlayerStore((s) => s.setVolume);
  const toggleMute = usePlayerStore((s) => s.toggleMute);

  const isAuthenticated = useAuthStore((s) => s.isAuthenticated());
  const role = useAuthStore((s) => s.role);
  const likeStore = useLikeStore();
  const toast = useToastStore((s) => s.show);

  const shuffle = usePlayerStore((s) => s.shuffle);
  const repeat = usePlayerStore((s) => s.repeat);
  const toggleShuffle = usePlayerStore((s) => s.toggleShuffle);
  const cycleRepeat = usePlayerStore((s) => s.cycleRepeat);
  const [queueOpen, setQueueOpen] = useState(false);
  const [playlistOpen, setPlaylistOpen] = useState(false);
  const [showPlModal, setShowPlModal] = useState(false);
  const [expanded, setExpanded] = useState(false);
  const progressRef = useRef<HTMLDivElement>(null);
  const mobileMiniProgressRef = useRef<HTMLDivElement>(null);
  const mobileSeekRef = useRef<HTMLDivElement>(null);
  const navigate = useNavigate();

  useEffect(() => {
    if (isAuthenticated && !likeStore.loaded) {
      likeStore.load();
    }
  }, [isAuthenticated, likeStore.loaded]);

  const handlePlayPause = useCallback(() => {
    if (isPlaying) {
      pause();
    } else {
      resume();
    }
  }, [isPlaying, pause, resume]);

  const handleProgressClick = useCallback(
    (e: React.MouseEvent<HTMLDivElement>) => {
      if (!progressRef.current || !duration) return;
      const rect = progressRef.current.getBoundingClientRect();
      const ratio = Math.max(0, Math.min(1, (e.clientX - rect.left) / rect.width));
      seek(ratio * duration);
    },
    [duration, seek],
  );

  const handleMobileMiniProgressClick = useCallback(
    (e: React.MouseEvent<HTMLDivElement>) => {
      if (!mobileMiniProgressRef.current || !duration) return;
      const rect = mobileMiniProgressRef.current.getBoundingClientRect();
      const ratio = Math.max(0, Math.min(1, (e.clientX - rect.left) / rect.width));
      seek(ratio * duration);
    },
    [duration, seek],
  );

  const handleMobileSeekClick = useCallback(
    (e: React.MouseEvent<HTMLDivElement>) => {
      if (!mobileSeekRef.current || !duration) return;
      const rect = mobileSeekRef.current.getBoundingClientRect();
      const ratio = Math.max(0, Math.min(1, (e.clientX - rect.left) / rect.width));
      seek(ratio * duration);
    },
    [duration, seek],
  );

  if (!currentTrack) {
    return (
      <>
        <div className={styles.player}>
          <div className={styles.trackInfo}>
            <div className={styles.thumb}>{'\u266B'}</div>
            <div className={styles.trackMeta}>
              <span className={styles.trackName}>재생할 곡을 선택하세요</span>
            </div>
          </div>
        </div>
        <div className={styles.mobilePlayer}>
          <div className={styles.mobileBar}>
            <div className={styles.mobileInfo}>
              <div className={styles.thumb}>{'\u266B'}</div>
              <div className={styles.trackMeta}>
                <span className={styles.trackName}>재생할 곡을 선택하세요</span>
              </div>
            </div>
          </div>
        </div>
      </>
    );
  }

  const trackDuration = duration || currentTrack.duration || 0;
  const progressPercent = trackDuration > 0 ? (currentTime / trackDuration) * 100 : 0;

  return (
    <>
      {/* ── Desktop / Tablet: full bar ── */}
      <div className={styles.player}>
        {/* Left: Track info */}
        <div className={styles.trackInfo}>
          <div className={styles.thumb}>
            {currentTrack.thumbnail ? (
              <img src={toUploadUrl(currentTrack.thumbnail)!} alt={currentTrack.title} />
            ) : (
              '\u266B'
            )}
          </div>
          <div className={styles.trackMeta}>
            <div className={styles.trackName}>{currentTrack.title}</div>
            <div className={styles.trackArtist}>{currentTrack.artistName}</div>
          </div>
          <button
            className={`${styles.heartBtn} ${likeStore.likedIds.has(currentTrack.id) ? styles.heartBtnActive : ''}`}
            aria-label="Like"
            onClick={() => {
              if (!isAuthenticated) {
                toast('warning', '로그인 후 이용 가능합니다.');
                navigate('/login');
                return;
              }
              likeStore.toggle(currentTrack.id);
            }}
          >
            {likeStore.likedIds.has(currentTrack.id) ? '\u2665' : '\u2661'}
          </button>
          <button
            className={styles.addToPlBtn}
            aria-label="Add to playlist"
            onClick={() => {
              if (!isAuthenticated) {
                toast('warning', '로그인 후 이용 가능합니다.');
                navigate('/login');
                return;
              }
              setShowPlModal(true);
            }}
            title="재생목록에 추가"
          >
            +
          </button>
        </div>

        {/* Center: Controls */}
        <div className={styles.controls}>
          <div className={styles.buttons}>
            <button
              className={`${styles.ctrlBtn} ${shuffle ? styles.ctrlBtnActive : ''}`}
              onClick={toggleShuffle}
              title="Shuffle"
            >
              {'\u21CC'}
            </button>
            <button className={styles.ctrlBtn} onClick={prev} title="Previous">
              {'\u23EE'}
            </button>
            <button
              className={styles.playBtn}
              onClick={handlePlayPause}
              aria-label={isPlaying ? 'Pause' : 'Play'}
            >
              {isPlaying ? '\u275A\u275A' : '\u25B6'}
            </button>
            <button className={styles.ctrlBtn} onClick={next} title="Next">
              {'\u23ED'}
            </button>
            <button
              className={`${styles.ctrlBtn} ${repeat !== 'off' ? styles.ctrlBtnActive : ''}`}
              onClick={cycleRepeat}
              title={repeat === 'one' ? 'Repeat One' : repeat === 'all' ? 'Repeat All' : 'Repeat'}
            >
              {repeat === 'one' ? '\uD83D\uDD02' : '\u21BA'}
            </button>
          </div>
          <div className={styles.progressBar}>
            <span className={styles.timeLeft}>{formatTime(currentTime)}</span>
            <div
              className={styles.progressTrack}
              ref={progressRef}
              onClick={handleProgressClick}
            >
              <div
                className={styles.progressFill}
                style={{ width: `${progressPercent}%` }}
              />
            </div>
            <span className={styles.time}>{formatTime(trackDuration)}</span>
          </div>
        </div>

        {/* Right: Actions */}
        <div className={styles.rightActions}>
          <div className={styles.volumeGroup}>
            <button
              className={styles.volumeBtn}
              onClick={toggleMute}
              aria-label={muted ? 'Unmute' : 'Mute'}
              title={muted ? '음소거 해제' : '음소거'}
            >
              {muted || volume === 0 ? (
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <polygon points="11 5 6 9 2 9 2 15 6 15 11 19 11 5" />
                  <line x1="23" y1="9" x2="17" y2="15" />
                  <line x1="17" y1="9" x2="23" y2="15" />
                </svg>
              ) : volume < 0.5 ? (
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <polygon points="11 5 6 9 2 9 2 15 6 15 11 19 11 5" />
                  <path d="M15.54 8.46a5 5 0 0 1 0 7.07" />
                </svg>
              ) : (
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <polygon points="11 5 6 9 2 9 2 15 6 15 11 19 11 5" />
                  <path d="M15.54 8.46a5 5 0 0 1 0 7.07" />
                  <path d="M19.07 4.93a10 10 0 0 1 0 14.14" />
                </svg>
              )}
            </button>
            <input
              type="range"
              className={styles.volumeSlider}
              min={0}
              max={1}
              step={0.01}
              value={muted ? 0 : volume}
              onChange={(e) => setVolume(parseFloat(e.target.value))}
              aria-label="Volume"
            />
          </div>

          <button
            className={`${styles.actionBtn} ${queueOpen ? styles.actionBtnActive : ''}`}
            onClick={() => {
              setQueueOpen((v) => !v);
              setPlaylistOpen(false);
            }}
          >
            {'\uB300\uAE30\uC5F4'}
          </button>
          <button
            className={`${styles.actionBtn} ${playlistOpen ? styles.actionBtnActive : ''}`}
            onClick={() => {
              setPlaylistOpen((v) => !v);
              setQueueOpen(false);
            }}
          >
            {'\uC7AC\uC0DD\uBAA9\uB85D'}
          </button>
          {!isAuthenticated && (
            <button className={styles.buyBtn} onClick={() => navigate('/login')}>
              {'\uAD6C\uB9E4\uD558\uAE30'}
            </button>
          )}
          {isAuthenticated && role === 'USER' && (
            <button className={styles.buyBtn} onClick={() => navigate('/subscriptions')}>
              {'\uAD6C\uB9E4\uD558\uAE30'}
            </button>
          )}
        </div>
      </div>

      {/* ── Mobile: mini bar ── */}
      <div className={styles.mobilePlayer}>
        {/* Mini progress bar on top */}
        <div className={styles.mobileProgressTrack} onClick={handleMobileMiniProgressClick} ref={mobileMiniProgressRef}>
          <div className={styles.mobileProgressFill} style={{ width: `${progressPercent}%` }} />
        </div>

        <div className={styles.mobileBar}>
          <div className={styles.mobileInfo} onClick={() => setExpanded((v) => !v)}>
            <div className={styles.thumb}>
              {currentTrack.thumbnail ? (
                <img src={toUploadUrl(currentTrack.thumbnail)!} alt={currentTrack.title} />
              ) : (
                '\u266B'
              )}
            </div>
            <div className={styles.trackMeta}>
              <div className={styles.trackName}>{currentTrack.title}</div>
              <div className={styles.trackArtist}>{currentTrack.artistName}</div>
            </div>
          </div>
          <div className={styles.mobileControls}>
            {!expanded && (
              <>
                <button
                  className={`${styles.heartBtn} ${likeStore.likedIds.has(currentTrack.id) ? styles.heartBtnActive : ''}`}
                  aria-label="Like"
                  onClick={() => {
                    if (!isAuthenticated) {
                      toast('warning', '로그인 후 이용 가능합니다.');
                      navigate('/login');
                      return;
                    }
                    likeStore.toggle(currentTrack.id);
                  }}
                >
                  {likeStore.likedIds.has(currentTrack.id) ? '\u2665' : '\u2661'}
                </button>
                <button
                  className={styles.playBtn}
                  onClick={handlePlayPause}
                  aria-label={isPlaying ? 'Pause' : 'Play'}
                >
                  {isPlaying ? '\u275A\u275A' : '\u25B6'}
                </button>
              </>
            )}
            <button
              className={styles.mobileExpandBtn}
              onClick={() => setExpanded((v) => !v)}
              aria-label={expanded ? 'Collapse' : 'Expand'}
            >
              {expanded ? '\u25BC' : '\u25B2'}
            </button>
          </div>
        </div>

        {/* Expanded panel */}
        <div className={`${styles.mobileExpanded} ${expanded ? styles.mobileExpandedOpen : ''}`}>
          {/* Full controls */}
          <div className={styles.mobileFullControls}>
            <button
              className={`${styles.ctrlBtn} ${shuffle ? styles.ctrlBtnActive : ''}`}
              onClick={toggleShuffle}
            >
              {'\u21CC'}
            </button>
            <button className={styles.ctrlBtn} onClick={prev}>{'\u23EE'}</button>
            <button className={styles.playBtn} onClick={handlePlayPause}>
              {isPlaying ? '\u275A\u275A' : '\u25B6'}
            </button>
            <button className={styles.ctrlBtn} onClick={next}>{'\u23ED'}</button>
            <button
              className={`${styles.ctrlBtn} ${repeat !== 'off' ? styles.ctrlBtnActive : ''}`}
              onClick={cycleRepeat}
            >
              {repeat === 'one' ? '\uD83D\uDD02' : '\u21BA'}
            </button>
          </div>

          {/* Progress / Seek bar */}
          <div className={styles.mobileSeek}>
            <span className={styles.mobileSeekTime}>{formatTime(currentTime)}</span>
            <div
              className={styles.mobileSeekTrack}
              onClick={handleMobileSeekClick}
              ref={mobileSeekRef}
            >
              <div
                className={styles.mobileSeekFill}
                style={{ width: `${progressPercent}%` }}
              />
            </div>
            <span className={styles.mobileSeekTime}>{formatTime(trackDuration)}</span>
          </div>

          {/* Volume toggle (icon only, no slider) */}
          <div className={styles.mobileVolumeToggle}>
            <button className={styles.volumeBtn} onClick={toggleMute} aria-label={muted ? '음소거 해제' : '음소거'}>
              {muted || volume === 0 ? (
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <polygon points="11 5 6 9 2 9 2 15 6 15 11 19 11 5" />
                  <line x1="23" y1="9" x2="17" y2="15" />
                  <line x1="17" y1="9" x2="23" y2="15" />
                </svg>
              ) : volume < 0.5 ? (
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <polygon points="11 5 6 9 2 9 2 15 6 15 11 19 11 5" />
                  <path d="M15.54 8.46a5 5 0 0 1 0 7.07" />
                </svg>
              ) : (
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <polygon points="11 5 6 9 2 9 2 15 6 15 11 19 11 5" />
                  <path d="M15.54 8.46a5 5 0 0 1 0 7.07" />
                  <path d="M19.07 4.93a10 10 0 0 1 0 14.14" />
                </svg>
              )}
            </button>
          </div>

          {/* Action buttons */}
          <div className={styles.mobileActions}>
            <button className={styles.addToPlBtn} onClick={() => {
              if (!isAuthenticated) {
                toast('warning', '로그인 후 이용 가능합니다.');
                navigate('/login');
                return;
              }
              setShowPlModal(true);
            }}>+ 재생목록</button>
            <button
              className={`${styles.actionBtn} ${queueOpen ? styles.actionBtnActive : ''}`}
              onClick={() => { setQueueOpen((v) => !v); setPlaylistOpen(false); }}
            >
              {'대기열'}
            </button>
            <button
              className={`${styles.actionBtn} ${playlistOpen ? styles.actionBtnActive : ''}`}
              onClick={() => { setPlaylistOpen((v) => !v); setQueueOpen(false); }}
            >
              {'재생목록'}
            </button>
            {!isAuthenticated && (
              <button className={styles.buyBtn} onClick={() => navigate('/login')}>{'구매하기'}</button>
            )}
            {isAuthenticated && role === 'USER' && (
              <button className={styles.buyBtn} onClick={() => navigate('/subscriptions')}>{'구매하기'}</button>
            )}
          </div>
        </div>
      </div>

      {/* Modals / Drawers */}
      <QueueModal open={queueOpen} onClose={() => setQueueOpen(false)} />
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

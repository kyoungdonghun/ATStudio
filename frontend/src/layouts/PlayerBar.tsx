import { useState, useCallback, useRef, useEffect } from 'react';
import { toUploadUrl } from '@/api/client';
import { usePlayerStore } from '@/store/playerStore';
import { useLikeStore } from '@/store/likeStore';
import { useAuthStore } from '@/store/authStore';
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
  const likeStore = useLikeStore();

  const [shuffle, setShuffle] = useState(false);
  const [repeat, setRepeat] = useState(false);
  const [queueOpen, setQueueOpen] = useState(false);
  const [playlistOpen, setPlaylistOpen] = useState(false);
  const [showPlModal, setShowPlModal] = useState(false);
  const [expanded, setExpanded] = useState(false);
  const progressRef = useRef<HTMLDivElement>(null);

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

  if (!currentTrack) {
    return null;
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
            onClick={() => likeStore.toggle(currentTrack.id)}
          >
            {likeStore.likedIds.has(currentTrack.id) ? '\u2665' : '\u2661'}
          </button>
          <button
            className={styles.addToPlBtn}
            aria-label="Add to playlist"
            onClick={() => setShowPlModal(true)}
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
              onClick={() => setShuffle((v) => !v)}
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
              className={`${styles.ctrlBtn} ${repeat ? styles.ctrlBtnActive : ''}`}
              onClick={() => setRepeat((v) => !v)}
              title="Repeat"
            >
              {'\u21BA'}
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
              {muted || volume === 0 ? '\uD83D\uDD07' : volume < 0.5 ? '\uD83D\uDD09' : '\uD83D\uDD0A'}
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
          <button className={styles.buyBtn}>
            {'\uAD6C\uB9E4\uD558\uAE30'}
          </button>
        </div>
      </div>

      {/* ── Mobile: mini bar ── */}
      <div className={styles.mobilePlayer}>
        {/* Mini progress bar on top */}
        <div className={styles.mobileProgressTrack} onClick={handleProgressClick} ref={progressRef}>
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
                  onClick={() => likeStore.toggle(currentTrack.id)}
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
              onClick={() => setShuffle((v) => !v)}
            >
              {'\u21CC'}
            </button>
            <button className={styles.ctrlBtn} onClick={prev}>{'\u23EE'}</button>
            <button className={styles.playBtn} onClick={handlePlayPause}>
              {isPlaying ? '\u275A\u275A' : '\u25B6'}
            </button>
            <button className={styles.ctrlBtn} onClick={next}>{'\u23ED'}</button>
            <button
              className={`${styles.ctrlBtn} ${repeat ? styles.ctrlBtnActive : ''}`}
              onClick={() => setRepeat((v) => !v)}
            >
              {'\u21BA'}
            </button>
          </div>

          {/* Time */}
          <div className={styles.mobileTime}>
            <span>{formatTime(currentTime)}</span>
            <span>{formatTime(trackDuration)}</span>
          </div>

          {/* Volume */}
          <div className={styles.mobileVolume}>
            <button className={styles.volumeBtn} onClick={toggleMute}>
              {muted || volume === 0 ? '\uD83D\uDD07' : volume < 0.5 ? '\uD83D\uDD09' : '\uD83D\uDD0A'}
            </button>
            <input
              type="range"
              className={styles.volumeSlider}
              min={0}
              max={1}
              step={0.01}
              value={muted ? 0 : volume}
              onChange={(e) => setVolume(parseFloat(e.target.value))}
            />
          </div>

          {/* Action buttons */}
          <div className={styles.mobileActions}>
            <button className={styles.addToPlBtn} onClick={() => setShowPlModal(true)}>+ 재생목록</button>
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
            <button className={styles.buyBtn}>{'구매하기'}</button>
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
      />
    </>
  );
}

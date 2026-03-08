import { useState, useCallback } from 'react';
import { usePlayerStore } from '@/store/playerStore';
import styles from './PlayerBar.module.css';

function formatTime(seconds: number): string {
  const m = Math.floor(seconds / 60);
  const s = Math.floor(seconds % 60);
  return `${m}:${String(s).padStart(2, '0')}`;
}

export default function PlayerBar() {
  const currentTrack = usePlayerStore((s) => s.currentTrack);
  const isPlaying = usePlayerStore((s) => s.isPlaying);
  const pause = usePlayerStore((s) => s.pause);
  const resume = usePlayerStore((s) => s.resume);
  const next = usePlayerStore((s) => s.next);
  const prev = usePlayerStore((s) => s.prev);

  const [shuffle, setShuffle] = useState(false);
  const [repeat, setRepeat] = useState(false);
  const [progress] = useState(0);

  const handlePlayPause = useCallback(() => {
    if (isPlaying) {
      pause();
    } else {
      resume();
    }
  }, [isPlaying, pause, resume]);

  if (!currentTrack) {
    return null;
  }

  const duration = currentTrack.duration;
  const currentTime = duration * (progress / 100);
  const progressPercent = progress;

  return (
    <div className={styles.player}>
      {/* Left: Track info */}
      <div className={styles.trackInfo}>
        <div className={styles.thumb}>
          {currentTrack.thumbnail ? (
            <img src={currentTrack.thumbnail} alt={currentTrack.title} />
          ) : (
            '\u266B'
          )}
        </div>
        <div>
          <div className={styles.trackName}>{currentTrack.title}</div>
          <div className={styles.trackArtist}>{currentTrack.artistName}</div>
        </div>
        <button className={styles.heartBtn} aria-label="Like">
          {'\u2665'}
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
          <div className={styles.progressTrack}>
            <div
              className={styles.progressFill}
              style={{ width: `${progressPercent}%` }}
            />
          </div>
          <span className={styles.time}>{formatTime(duration)}</span>
        </div>
      </div>

      {/* Right: Actions */}
      <div className={styles.rightActions}>
        <button className={styles.actionBtn}>{'\uB300\uAE30\uC5F4'}</button>
        <button className={styles.actionBtn}>
          {'\uC7AC\uC0DD\uBAA9\uB85D'}
        </button>
        <button className={styles.buyBtn}>
          {'\uAD6C\uB9E4\uD558\uAE30'}
        </button>
      </div>
    </div>
  );
}

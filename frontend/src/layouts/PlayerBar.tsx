import { useState, useCallback, useRef, useEffect, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { toUploadUrl } from '@/api/client';
import { usePlayerStore } from '@/store/playerStore';
import { useLikeStore } from '@/store/likeStore';
import { useAuthStore } from '@/store/authStore';
import { useToastStore } from '@/store/toastStore';
import { fetchMySubscription } from '@/api/userSubscriptions';
import HistoryModal from '@/components/player/HistoryModal';
import PlaylistDrawer from '@/components/player/PlaylistDrawer';
import AddToPlaylistModal from '@/components/playlist/AddToPlaylistModal';
import WaveformCanvas from '@/components/player/WaveformCanvas';
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
  const [historyOpen, setHistoryOpen] = useState(false);
  const [playlistOpen, setPlaylistOpen] = useState(false);
  const [showPlModal, setShowPlModal] = useState(false);
  const [expanded, setExpanded] = useState(false);
  const [hasSubscription, setHasSubscription] = useState(false);
  const mobileMiniProgressRef = useRef<HTMLDivElement>(null);
  const navigate = useNavigate();

  useEffect(() => {
    if (isAuthenticated && !likeStore.loaded) {
      likeStore.load();
    }
  }, [isAuthenticated, likeStore.loaded]);

  useEffect(() => {
    if (!isAuthenticated || role === 'ADMIN') {
      setHasSubscription(false);
      return;
    }
    fetchMySubscription()
      .then(() => setHasSubscription(true))
      .catch(() => setHasSubscription(false));
  }, [isAuthenticated, role]);

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

  const trackDuration = duration || currentTrack?.duration || 0;
  const progressRatio = trackDuration > 0 ? currentTime / trackDuration : 0;

  const handleSeek = useCallback(
    (ratio: number) => {
      seek(ratio * trackDuration);
    },
    [seek, trackDuration],
  );

  const handleMobileMiniProgressClick = useCallback(
    (e: React.MouseEvent<HTMLDivElement>) => {
      if (!mobileMiniProgressRef.current || !trackDuration) return;
      const rect = mobileMiniProgressRef.current.getBoundingClientRect();
      const ratio = Math.max(0, Math.min(1, (e.clientX - rect.left) / rect.width));
      seek(ratio * trackDuration);
    },
    [trackDuration, seek],
  );

  /* ── Repeat icon SVG helper ── */
  const repeatIcon = (
    repeat === 'one' ? (
      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.3" strokeLinecap="round" strokeLinejoin="round">
        <polyline points="17 1 21 5 17 9" />
        <path d="M3 11V9a4 4 0 0 1 4-4h14" />
        <polyline points="7 23 3 19 7 15" />
        <path d="M21 13v2a4 4 0 0 1-4 4H3" />
        <text x="12" y="14.5" textAnchor="middle" fontSize="8" fill="currentColor" stroke="none" fontWeight="bold">1</text>
      </svg>
    ) : (
      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.3" strokeLinecap="round" strokeLinejoin="round">
        <polyline points="17 1 21 5 17 9" />
        <path d="M3 11V9a4 4 0 0 1 4-4h14" />
        <polyline points="7 23 3 19 7 15" />
        <path d="M21 13v2a4 4 0 0 1-4 4H3" />
      </svg>
    )
  );

  /* ── Volume icon helper ── */
  const volumeIcon = muted || volume === 0 ? (
    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <polygon points="11 5 6 9 2 9 2 15 6 15 11 19 11 5" />
      <line x1="23" y1="9" x2="17" y2="15" /><line x1="17" y1="9" x2="23" y2="15" />
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
  );

  /* ── Empty state (no track loaded) ── */
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
          {/* center: flat waveform placeholder */}
          <div className={styles.controls}>
            <button className={styles.playBtn} disabled style={{ opacity: 0.4 }}>
              {'\u25B6'}
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
            <div className={styles.navGroup}>
              <button className={styles.ctrlBtn} disabled style={{ opacity: 0.3 }}>{'\u23EE'}</button>
              <button className={styles.ctrlBtn} disabled style={{ opacity: 0.3 }}>{'\u23ED'}</button>
            </div>
            <div className={styles.modeGroup}>
              <button className={styles.ctrlBtn} disabled style={{ opacity: 0.3 }}>{'\u21CC'}</button>
              <button className={styles.ctrlBtn} disabled style={{ opacity: 0.3 }}>{repeatIcon}</button>
            </div>
            <div className={styles.volumeGroup}>
              <button className={styles.volumeBtn} onClick={toggleMute}>{volumeIcon}</button>
              <input type="range" className={styles.volumeSlider} min={0} max={1} step={0.01}
                value={muted ? 0 : volume} onChange={(e) => setVolume(parseFloat(e.target.value))} />
            </div>
            {(role === 'ADMIN' || hasSubscription) && (
              <>
                <button
                  className={`${styles.actionBtn} ${historyOpen ? styles.actionBtnActive : ''}`}
                  onClick={() => { setHistoryOpen((v) => !v); setPlaylistOpen(false); }}
                >{'재생기록'}</button>
                <button
                  className={`${styles.actionBtn} ${playlistOpen ? styles.actionBtnActive : ''}`}
                  onClick={() => { setPlaylistOpen((v) => !v); setHistoryOpen(false); }}
                >{'재생목록'}</button>
              </>
            )}
          </div>
        </div>

        {/* Mobile empty state */}
        <div className={styles.mobilePlayer}>
          <div className={styles.mobileBar}>
            <div className={styles.mobileInfo}>
              <div className={styles.thumb}>{'\u266B'}</div>
              <div className={styles.trackMeta}>
                <span className={styles.trackName}>재생할 곡을 선택하세요</span>
              </div>
            </div>
            {(role === 'ADMIN' || hasSubscription) && (
              <div className={styles.mobileControls}>
                <button className={styles.mobileExpandBtn} onClick={() => setExpanded((v) => !v)}>
                  {expanded ? '\u25BC' : '\u25B2'}
                </button>
              </div>
            )}
          </div>
          {(role === 'ADMIN' || hasSubscription) && (
            <div className={`${styles.mobileExpanded} ${expanded ? styles.mobileExpandedOpen : ''}`}>
              <div className={styles.mobileActions}>
                <button className={`${styles.actionBtn} ${historyOpen ? styles.actionBtnActive : ''}`}
                  onClick={() => { setHistoryOpen((v) => !v); setPlaylistOpen(false); }}>{'재생기록'}</button>
                <button className={`${styles.actionBtn} ${playlistOpen ? styles.actionBtnActive : ''}`}
                  onClick={() => { setPlaylistOpen((v) => !v); setHistoryOpen(false); }}>{'재생목록'}</button>
              </div>
            </div>
          )}
        </div>

        {historyOpen && <HistoryModal open={historyOpen} onClose={() => setHistoryOpen(false)} />}
        {playlistOpen && <PlaylistDrawer open={playlistOpen} onClose={() => setPlaylistOpen(false)} />}
      </>
    );
  }

  const progressPercent = progressRatio * 100;

  return (
    <>
      {/* ── Desktop / Tablet: full bar ── */}
      <div className={styles.player}>

        {/* Left: Track info */}
        <div className={styles.trackInfo}>
          <div
            className={styles.thumb}
            onClick={() => navigate(`/tracks/${currentTrack.id}`)}
            style={{ cursor: 'pointer' }}
            role="button"
            aria-label="트랙 상세 보기"
          >
            {currentTrack.thumbnail ? (
              <img src={toUploadUrl(currentTrack.thumbnail)!} alt={currentTrack.title} />
            ) : (
              '\u266B'
            )}
          </div>
          <div className={styles.trackMeta}>
            <div
              className={styles.trackName}
              onClick={() => navigate(`/tracks/${currentTrack.id}`)}
              style={{ cursor: 'pointer' }}
            >
              {currentTrack.title}
            </div>
          </div>
          <button
            className={`${styles.heartBtn} ${likeStore.likedIds.has(currentTrack.id) ? styles.heartBtnActive : ''}`}
            aria-label="Like"
            onClick={() => {
              if (!isAuthenticated) { toast('warning', '로그인 후 이용 가능합니다.'); navigate('/login'); return; }
              likeStore.toggle(currentTrack.id);
            }}
          >
            {likeStore.likedIds.has(currentTrack.id) ? '\u2665' : '\u2661'}
          </button>
          <button
            className={styles.addToPlBtn}
            aria-label="Add to playlist"
            onClick={() => {
              if (!isAuthenticated) { toast('warning', '로그인 후 이용 가능합니다.'); navigate('/login'); return; }
              setShowPlModal(true);
            }}
            title="재생목록에 추가"
          >+</button>
        </div>

        {/* Center: Play button + Waveform + Time */}
        <div className={styles.controls}>
          <button
            className={styles.playBtn}
            onClick={handlePlayPause}
            aria-label={isPlaying ? 'Pause' : 'Play'}
          >
            {isPlaying ? '\u275A\u275A' : '\u25B6'}
          </button>
          <div className={styles.waveformWrap}>
            <WaveformCanvas
              peaks={parsedPeaks}
              progress={progressRatio}
              onSeek={handleSeek}
            />
          </div>
          <div className={styles.timeDisplay}>
            <span>{formatTime(currentTime)}</span>
            <span className={styles.timeSep}>/</span>
            <span>{formatTime(trackDuration)}</span>
          </div>
        </div>

        {/* Right: Nav + Mode + Volume + Actions */}
        <div className={styles.rightActions}>
          <div className={styles.navGroup}>
            <button className={styles.ctrlBtn} onClick={prev} title="Previous">{'\u23EE'}</button>
            <button className={styles.ctrlBtn} onClick={next} title="Next">{'\u23ED'}</button>
          </div>
          <div className={styles.modeGroup}>
            <button
              className={`${styles.ctrlBtn} ${shuffle ? styles.ctrlBtnActive : ''}`}
              onClick={toggleShuffle} title="Shuffle"
            >{'\u21CC'}</button>
            <button
              className={`${styles.ctrlBtn} ${repeat !== 'off' ? styles.ctrlBtnActive : ''}`}
              onClick={cycleRepeat}
              title={repeat === 'one' ? 'Repeat One' : repeat === 'all' ? 'Repeat All' : 'Repeat'}
            >{repeatIcon}</button>
          </div>
          <div className={styles.volumeGroup}>
            <button className={styles.volumeBtn} onClick={toggleMute}
              aria-label={muted ? 'Unmute' : 'Mute'}>{volumeIcon}</button>
            <input
              type="range" className={styles.volumeSlider}
              min={0} max={1} step={0.01}
              value={muted ? 0 : volume}
              onChange={(e) => setVolume(parseFloat(e.target.value))}
              aria-label="Volume"
            />
          </div>
          <button
            className={`${styles.actionBtn} ${historyOpen ? styles.actionBtnActive : ''}`}
            onClick={() => { setHistoryOpen((v) => !v); setPlaylistOpen(false); }}
          >{'재생기록'}</button>
          <button
            className={`${styles.actionBtn} ${playlistOpen ? styles.actionBtnActive : ''}`}
            onClick={() => { setPlaylistOpen((v) => !v); setHistoryOpen(false); }}
          >{'재생목록'}</button>
          {!isAuthenticated && (
            <button className={styles.buyBtn} onClick={() => navigate('/login')}>{'구매하기'}</button>
          )}
          {isAuthenticated && role === 'USER' && !hasSubscription && (
            <button className={styles.buyBtn} onClick={() => navigate('/subscriptions')}>{'구독하기'}</button>
          )}
        </div>
      </div>

      {/* ── Mobile: mini bar ── */}
      <div className={styles.mobilePlayer}>
        {/* Thin progress indicator at top of mini bar */}
        <div className={styles.mobileProgressTrack} onClick={handleMobileMiniProgressClick} ref={mobileMiniProgressRef}>
          <div className={styles.mobileProgressFill} style={{ width: `${progressPercent}%` }} />
        </div>

        <div className={styles.mobileBar}>
          <div className={styles.mobileInfo} onClick={() => setExpanded((v) => !v)}>
            <div
              className={styles.thumb}
              onClick={(e) => { e.stopPropagation(); navigate(`/tracks/${currentTrack.id}`); }}
              style={{ cursor: 'pointer' }} role="button" aria-label="트랙 상세 보기"
            >
              {currentTrack.thumbnail ? (
                <img src={toUploadUrl(currentTrack.thumbnail)!} alt={currentTrack.title} />
              ) : '\u266B'}
            </div>
            <div className={styles.trackMeta}>
              <div className={styles.trackName}
                onClick={(e) => { e.stopPropagation(); navigate(`/tracks/${currentTrack.id}`); }}
                style={{ cursor: 'pointer' }}>{currentTrack.title}</div>
            </div>
          </div>
          <div className={styles.mobileControls}>
            {!expanded && (
              <>
                <button
                  className={`${styles.heartBtn} ${likeStore.likedIds.has(currentTrack.id) ? styles.heartBtnActive : ''}`}
                  aria-label="Like"
                  onClick={() => {
                    if (!isAuthenticated) { toast('warning', '로그인 후 이용 가능합니다.'); navigate('/login'); return; }
                    likeStore.toggle(currentTrack.id);
                  }}
                >
                  {likeStore.likedIds.has(currentTrack.id) ? '\u2665' : '\u2661'}
                </button>
                <button className={styles.playBtn} onClick={handlePlayPause} aria-label={isPlaying ? 'Pause' : 'Play'}>
                  {isPlaying ? '\u275A\u275A' : '\u25B6'}
                </button>
              </>
            )}
            <button className={styles.mobileExpandBtn} onClick={() => setExpanded((v) => !v)}
              aria-label={expanded ? 'Collapse' : 'Expand'}>
              {expanded ? '\u25BC' : '\u25B2'}
            </button>
          </div>
        </div>

        {/* Expanded panel */}
        <div className={`${styles.mobileExpanded} ${expanded ? styles.mobileExpandedOpen : ''}`}>
          {/* Transport controls */}
          <div className={styles.mobileFullControls}>
            <button className={`${styles.ctrlBtn} ${shuffle ? styles.ctrlBtnActive : ''}`} onClick={toggleShuffle}>{'\u21CC'}</button>
            <button className={styles.ctrlBtn} onClick={prev}>{'\u23EE'}</button>
            <button className={styles.playBtn} onClick={handlePlayPause}>
              {isPlaying ? '\u275A\u275A' : '\u25B6'}
            </button>
            <button className={styles.ctrlBtn} onClick={next}>{'\u23ED'}</button>
            <button className={`${styles.ctrlBtn} ${repeat !== 'off' ? styles.ctrlBtnActive : ''}`} onClick={cycleRepeat}>
              {repeatIcon}
            </button>
          </div>

          {/* Waveform seek (replaces plain progress bar) */}
          <div className={styles.mobileWaveform}>
            <span className={styles.mobileSeekTime}>{formatTime(currentTime)}</span>
            <div className={styles.mobileWaveformWrap}>
              <WaveformCanvas peaks={parsedPeaks} progress={progressRatio} onSeek={handleSeek} height={32} />
            </div>
            <span className={styles.mobileSeekTime}>{formatTime(trackDuration)}</span>
          </div>

          {/* Volume toggle */}
          <div className={styles.mobileVolumeToggle}>
            <button className={styles.volumeBtn} onClick={toggleMute} aria-label={muted ? '음소거 해제' : '음소거'}>
              {volumeIcon}
            </button>
          </div>

          {/* Action buttons */}
          <div className={styles.mobileActions}>
            <button className={styles.addToPlBtn} onClick={() => {
              if (!isAuthenticated) { toast('warning', '로그인 후 이용 가능합니다.'); navigate('/login'); return; }
              setShowPlModal(true);
            }}>+ 재생목록</button>
            <button className={`${styles.actionBtn} ${historyOpen ? styles.actionBtnActive : ''}`}
              onClick={() => { setHistoryOpen((v) => !v); setPlaylistOpen(false); }}>{'재생기록'}</button>
            <button className={`${styles.actionBtn} ${playlistOpen ? styles.actionBtnActive : ''}`}
              onClick={() => { setPlaylistOpen((v) => !v); setHistoryOpen(false); }}>{'재생목록'}</button>
            {!isAuthenticated && (
              <button className={styles.buyBtn} onClick={() => navigate('/login')}>{'구매하기'}</button>
            )}
            {isAuthenticated && role === 'USER' && !hasSubscription && (
              <button className={styles.buyBtn} onClick={() => navigate('/subscriptions')}>{'구독하기'}</button>
            )}
          </div>
        </div>
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

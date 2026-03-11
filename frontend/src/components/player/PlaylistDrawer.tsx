import { useEffect, useState, useRef, useCallback } from 'react';
import { useAuthStore } from '@/store/authStore';
import { usePlayerStore } from '@/store/playerStore';
import {
  fetchMyPlaylists,
  fetchPlaylistDetail,
  createPlaylist,
  deletePlaylist,
  removeTrackFromPlaylist,
  reorderTracks,
  type PlaylistDetail,
  type PlaylistTrack,
} from '@/api/playlists';
import { fetchPlayHistory, deletePlayHistory } from '@/api/playHistory';
import type { Playlist, PlayHistory } from '@/types';
import styles from './PlaylistDrawer.module.css';

type Tab = 'playlists' | 'history';

interface PlaylistDrawerProps {
  open: boolean;
  onClose: () => void;
}

export default function PlaylistDrawer({ open, onClose }: PlaylistDrawerProps) {
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated());
  const playTrack = usePlayerStore((s) => s.play);

  const [tab, setTab] = useState<Tab>('playlists');

  /* ── Playlist state ── */
  const [playlists, setPlaylists] = useState<Playlist[]>([]);
  const [selectedPl, setSelectedPl] = useState<PlaylistDetail | null>(null);
  const [plLoading, setPlLoading] = useState(false);

  /* ── Create form ── */
  const [showCreate, setShowCreate] = useState(false);
  const [newTitle, setNewTitle] = useState('');
  const [creating, setCreating] = useState(false);

  /* ── History state ── */
  const [history, setHistory] = useState<PlayHistory[]>([]);
  const [histLoading, setHistLoading] = useState(false);

  /* ── Drag state ── */
  const dragIdx = useRef<number | null>(null);
  const [dragOverIdx, setDragOverIdx] = useState<number | null>(null);

  /* ── Load playlists ── */
  const loadPlaylists = useCallback(async () => {
    if (!isAuthenticated) return;
    setPlLoading(true);
    try {
      const res = await fetchMyPlaylists();
      setPlaylists(res.dataList ?? []);
    } catch { /* ignore */ }
    setPlLoading(false);
  }, [isAuthenticated]);

  /* ── Load history ── */
  const loadHistory = useCallback(async () => {
    if (!isAuthenticated) return;
    setHistLoading(true);
    try {
      const res = await fetchPlayHistory({ page: 1, size: 50 });
      setHistory(res.dataList ?? []);
    } catch { /* ignore */ }
    setHistLoading(false);
  }, [isAuthenticated]);

  useEffect(() => {
    if (!open) return;
    if (tab === 'playlists') {
      loadPlaylists();
      setSelectedPl(null);
    } else {
      loadHistory();
    }
  }, [open, tab, loadPlaylists, loadHistory]);

  if (!open) return null;

  /* ── Playlist handlers ── */

  async function openPlaylist(pl: Playlist) {
    try {
      const detail = await fetchPlaylistDetail(pl.id);
      setSelectedPl(detail);
    } catch { /* ignore */ }
  }

  async function handleCreate() {
    if (!newTitle.trim()) return;
    setCreating(true);
    try {
      await createPlaylist({ title: newTitle.trim() });
      setNewTitle('');
      setShowCreate(false);
      await loadPlaylists();
    } catch { /* ignore */ }
    setCreating(false);
  }

  async function handleDeletePlaylist(id: number) {
    try {
      await deletePlaylist(id);
      setSelectedPl(null);
      await loadPlaylists();
    } catch { /* ignore */ }
  }

  async function handleRemoveTrack(trackId: number) {
    if (!selectedPl) return;
    try {
      await removeTrackFromPlaylist(selectedPl.id, trackId);
      const detail = await fetchPlaylistDetail(selectedPl.id);
      setSelectedPl(detail);
    } catch { /* ignore */ }
  }

  function handlePlayTrack(t: PlaylistTrack) {
    playTrack({
      id: t.trackId,
      title: t.title,
      artistName: '',
      duration: 0,
      bpm: t.bpm,
      tonality: t.tonality,
      description: null,
      audioFile: null,
      thumbnail: null,
      tags: [],
      isActive: true,
      playCount: 0,
      createdAt: '',
      updatedAt: '',
    });
  }

  /* ── Drag & Drop reorder ── */

  function handleDragStart(idx: number) {
    dragIdx.current = idx;
  }

  function handleDragOver(e: React.DragEvent, idx: number) {
    e.preventDefault();
    setDragOverIdx(idx);
  }

  async function handleDrop(idx: number) {
    if (dragIdx.current === null || dragIdx.current === idx || !selectedPl) {
      dragIdx.current = null;
      setDragOverIdx(null);
      return;
    }

    const tracks = [...selectedPl.tracks];
    const [moved] = tracks.splice(dragIdx.current, 1);
    tracks.splice(idx, 0, moved);

    // Optimistic update
    const reordered = tracks.map((t, i) => ({ ...t, trackOrder: i + 1 }));
    setSelectedPl({ ...selectedPl, tracks: reordered });
    dragIdx.current = null;
    setDragOverIdx(null);

    // API call
    try {
      await reorderTracks(
        selectedPl.id,
        reordered.map((t) => ({ trackId: t.trackId, trackOrder: t.trackOrder })),
      );
    } catch {
      // Revert on error
      const detail = await fetchPlaylistDetail(selectedPl.id);
      setSelectedPl(detail);
    }
  }

  function handleDragEnd() {
    dragIdx.current = null;
    setDragOverIdx(null);
  }

  /* ── History handlers ── */

  function handlePlayHistory(h: PlayHistory) {
    playTrack({
      id: h.track.id,
      title: h.track.title,
      artistName: '',
      duration: 0,
      bpm: 0,
      tonality: '',
      description: null,
      audioFile: null,
      thumbnail: h.track.thumbnail,
      tags: [],
      isActive: true,
      playCount: 0,
      createdAt: '',
      updatedAt: '',
    });
  }

  async function handleDeleteHistoryItem(id: number) {
    try {
      await deletePlayHistory([id]);
      setHistory((prev) => prev.filter((h) => h.id !== id));
    } catch { /* ignore */ }
  }

  async function handleClearHistory() {
    try {
      await deletePlayHistory([]);
      setHistory([]);
    } catch { /* ignore */ }
  }

  /* ── Render ── */

  return (
    <div className={styles.drawer}>
      {/* Header with tabs */}
      <div className={styles.header}>
        <div className={styles.tabs}>
          <button
            className={`${styles.tab} ${tab === 'playlists' ? styles.tabActive : ''}`}
            onClick={() => { setTab('playlists'); setSelectedPl(null); }}
          >
            재생목록
          </button>
          <button
            className={`${styles.tab} ${tab === 'history' ? styles.tabActive : ''}`}
            onClick={() => setTab('history')}
          >
            재생기록
          </button>
        </div>
        <button className={styles.closeBtn} onClick={onClose}>
          &times;
        </button>
      </div>

      {!isAuthenticated ? (
        <div className={styles.empty}>로그인이 필요합니다.</div>
      ) : tab === 'playlists' ? (
        /* ── Playlists Tab ── */
        selectedPl ? (
          /* Detail view */
          <div className={styles.body}>
            <div className={styles.detailHeader}>
              <button
                className={styles.backBtn}
                onClick={() => setSelectedPl(null)}
              >
                {'\u2190'}
              </button>
              <span className={styles.detailTitle}>{selectedPl.title}</span>
              <span className={styles.detailCount}>
                {selectedPl.tracks.length}곡
              </span>
              <button
                className={styles.deletePlaylistBtn}
                onClick={() => handleDeletePlaylist(selectedPl.id)}
                title="재생목록 삭제"
              >
                {'\u2715'}
              </button>
            </div>
            {selectedPl.tracks.length === 0 ? (
              <div className={styles.empty}>곡이 없습니다.</div>
            ) : (
              <ul className={styles.trackList}>
                {selectedPl.tracks.map((t, idx) => (
                  <li
                    key={t.trackId}
                    className={`${styles.trackItem} ${dragOverIdx === idx ? styles.trackItemDragOver : ''}`}
                    draggable
                    onDragStart={() => handleDragStart(idx)}
                    onDragOver={(e) => handleDragOver(e, idx)}
                    onDrop={() => handleDrop(idx)}
                    onDragEnd={handleDragEnd}
                  >
                    <span className={styles.dragHandle} title="드래그하여 순서 변경">
                      {'\u2630'}
                    </span>
                    <button
                      className={styles.trackPlayBtn}
                      onClick={() => handlePlayTrack(t)}
                    >
                      {'\u25B6'}
                    </button>
                    <div className={styles.trackInfo}>
                      <div className={styles.trackTitle}>{t.title}</div>
                      <div className={styles.trackMeta}>
                        {t.bpm ? `${t.bpm} BPM` : ''}
                        {t.bpm && t.tonality ? ' \u00B7 ' : ''}
                        {t.tonality ?? ''}
                      </div>
                    </div>
                    <button
                      className={styles.removeBtn}
                      onClick={() => handleRemoveTrack(t.trackId)}
                      title="삭제"
                    >
                      &times;
                    </button>
                  </li>
                ))}
              </ul>
            )}
          </div>
        ) : (
          /* List view */
          <div className={styles.body}>
            {plLoading ? (
              <div className={styles.empty}>Loading...</div>
            ) : (
              <>
                <ul className={styles.plList}>
                  {playlists.map((pl) => (
                    <li key={pl.id} className={styles.plItem}>
                      <button
                        className={styles.plItemBtn}
                        onClick={() => openPlaylist(pl)}
                      >
                        <span className={styles.plIcon}>{'\u266A'}</span>
                        <div className={styles.plItemInfo}>
                          <span className={styles.plItemName}>{pl.title}</span>
                          <span className={styles.plItemCount}>
                            {pl.trackCount}곡
                          </span>
                        </div>
                        <span className={styles.plArrow}>{'\u203A'}</span>
                      </button>
                    </li>
                  ))}
                </ul>

                {/* Create new */}
                {playlists.length < 3 && !showCreate && (
                  <button
                    className={styles.createBtn}
                    onClick={() => setShowCreate(true)}
                  >
                    + 새 재생목록
                  </button>
                )}

                {showCreate && (
                  <div className={styles.createForm}>
                    <input
                      className={styles.createInput}
                      type="text"
                      placeholder="재생목록 이름"
                      value={newTitle}
                      onChange={(e) => setNewTitle(e.target.value)}
                      maxLength={50}
                      autoFocus
                      onKeyDown={(e) => {
                        if (e.key === 'Enter') handleCreate();
                        if (e.key === 'Escape') setShowCreate(false);
                      }}
                    />
                    <div className={styles.createActions}>
                      <button
                        className={styles.createCancelBtn}
                        onClick={() => setShowCreate(false)}
                      >
                        취소
                      </button>
                      <button
                        className={styles.createConfirmBtn}
                        onClick={handleCreate}
                        disabled={!newTitle.trim() || creating}
                      >
                        {creating ? '...' : '만들기'}
                      </button>
                    </div>
                  </div>
                )}
              </>
            )}
          </div>
        )
      ) : (
        /* ── History Tab ── */
        <div className={styles.body}>
          {histLoading ? (
            <div className={styles.empty}>Loading...</div>
          ) : history.length === 0 ? (
            <div className={styles.empty}>재생 기록이 없습니다.</div>
          ) : (
            <>
              <div className={styles.histHeader}>
                <button
                  className={styles.clearHistBtn}
                  onClick={handleClearHistory}
                >
                  전체 삭제
                </button>
              </div>
              <ul className={styles.histList}>
                {history.map((h) => (
                  <li key={h.id} className={styles.histItem}>
                    <button
                      className={styles.histPlayBtn}
                      onClick={() => handlePlayHistory(h)}
                    >
                      {'\u25B6'}
                    </button>
                    <div className={styles.histInfo}>
                      <div className={styles.histTitle}>{h.track.title}</div>
                      <div className={styles.histTime}>
                        {new Date(h.playedAt).toLocaleString('ko-KR', {
                          month: 'short',
                          day: 'numeric',
                          hour: '2-digit',
                          minute: '2-digit',
                        })}
                      </div>
                    </div>
                    <button
                      className={styles.histRemoveBtn}
                      onClick={() => handleDeleteHistoryItem(h.id)}
                      title="삭제"
                    >
                      &times;
                    </button>
                  </li>
                ))}
              </ul>
            </>
          )}
        </div>
      )}
    </div>
  );
}

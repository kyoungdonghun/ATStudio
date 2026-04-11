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
import { fetchMySubscription } from '@/api/userSubscriptions';
import { fetchLikes } from '@/api/likes';
import { getApiErrorCode } from '@/api/client';
import { useToastStore } from '@/store/toastStore';
import type { Playlist, LikeItem } from '@/types';
import styles from './PlaylistDrawer.module.css';

const DEFAULT_MAX_PLAYLISTS = 3;

type Tab = 'playlists' | 'likes';

interface PlaylistDrawerProps {
  open: boolean;
  onClose: () => void;
}

export default function PlaylistDrawer({ open, onClose }: PlaylistDrawerProps) {
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated());
  const playTrack = usePlayerStore((s) => s.play);
  const showToast = useToastStore((s) => s.show);

  const [tab, setTab] = useState<Tab>('playlists');

  /* ── Playlist state ── */
  const [playlists, setPlaylists] = useState<Playlist[]>([]);
  const [maxPlaylists, setMaxPlaylists] = useState(DEFAULT_MAX_PLAYLISTS);
  const [selectedPl, setSelectedPl] = useState<PlaylistDetail | null>(null);
  const [plLoading, setPlLoading] = useState(false);

  /* ── Create form ── */
  const [showCreate, setShowCreate] = useState(false);
  const [newTitle, setNewTitle] = useState('');
  const [creating, setCreating] = useState(false);

  /* ── Likes state ── */
  const [likes, setLikes] = useState<LikeItem[]>([]);
  const [likesLoading, setLikesLoading] = useState(false);

  /* ── Drag state ── */
  const dragIdx = useRef<number | null>(null);
  const [dragOverIdx, setDragOverIdx] = useState<number | null>(null);

  /* ── Touch DnD refs ── */
  const touchDragIdx = useRef<number | null>(null);
  const trackListRef = useRef<HTMLUListElement>(null);

  /* ── Load playlists ── */
  const loadPlaylists = useCallback(async () => {
    if (!isAuthenticated) return;
    setPlLoading(true);
    try {
      const [plRes, subRes] = await Promise.allSettled([
        fetchMyPlaylists(),
        fetchMySubscription(),
      ]);
      if (plRes.status === 'fulfilled') {
        setPlaylists(plRes.value.dataList ?? []);
      }
      if (subRes.status === 'fulfilled') {
        const sub = subRes.value;
        if (sub.subscription?.maxPlaylists) {
          setMaxPlaylists(sub.subscription.maxPlaylists);
        }
      }
    } catch { /* ignore */ }
    setPlLoading(false);
  }, [isAuthenticated]);

  /* ── Load likes ── */
  const loadLikes = useCallback(async () => {
    if (!isAuthenticated) return;
    setLikesLoading(true);
    try {
      const res = await fetchLikes();
      setLikes(res.dataList ?? []);
    } catch { /* ignore */ }
    setLikesLoading(false);
  }, [isAuthenticated]);

  useEffect(() => {
    if (!open) return;
    if (tab === 'playlists') {
      loadPlaylists();
      setSelectedPl(null);
    } else {
      loadLikes();
    }
  }, [open, tab, loadPlaylists, loadLikes]);

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
    } catch (err) {
      const code = await getApiErrorCode(err);
      if (code === 'PLAYLIST_LIMIT_EXCEEDED') {
        showToast('error', '구독 플랜의 재생목록 한도를 초과했습니다. 플랜을 업그레이드해주세요.');
      } else {
        showToast('error', '재생목록 생성에 실패했습니다.');
      }
    }
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
      likeCount: 0,
      downloadCount: 0,
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

  /* ── Touch DnD handlers ── */

  function handleTouchStart(idx: number) {
    touchDragIdx.current = idx;
  }

  function handleTouchMove(e: React.TouchEvent) {
    if (touchDragIdx.current === null) return;
    e.preventDefault();
  }

  function handleTouchEnd(e: React.TouchEvent) {
    if (touchDragIdx.current === null || !trackListRef.current) return;
    const touch = e.changedTouches[0];
    const items = trackListRef.current.children;
    let targetIdx = touchDragIdx.current;

    for (let i = 0; i < items.length; i++) {
      const rect = items[i].getBoundingClientRect();
      if (touch.clientY >= rect.top && touch.clientY <= rect.bottom) {
        targetIdx = i;
        break;
      }
    }

    if (targetIdx !== touchDragIdx.current) {
      // handleDrop reads dragIdx.current as the source index
      dragIdx.current = touchDragIdx.current;
      handleDrop(targetIdx);
    }
    touchDragIdx.current = null;
  }

  /* ── Like handlers ── */

  function handlePlayLike(item: LikeItem) {
    playTrack({
      id: item.trackId,
      title: item.title,
      artistName: '',
      duration: 0,
      bpm: item.bpm,
      tonality: item.tonality,
      description: null,
      audioFile: null,
      thumbnail: item.thumbnail,
      tags: [],
      isActive: true,
      playCount: 0,
      likeCount: 0,
      downloadCount: 0,
      createdAt: '',
      updatedAt: '',
    });
  }

  /* ── History handlers (SR-89: localStorage) ── */

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
            className={`${styles.tab} ${tab === 'likes' ? styles.tabActive : ''}`}
            onClick={() => setTab('likes')}
          >
            좋아요
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
              <ul className={styles.trackList} ref={trackListRef} onTouchMove={handleTouchMove} onTouchEnd={handleTouchEnd}>
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
                    <span
                      className={styles.dragHandle}
                      title="드래그하여 순서 변경"
                      onTouchStart={() => handleTouchStart(idx)}
                      style={{ touchAction: 'none' }}
                    >
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
                {playlists.length < maxPlaylists && !showCreate && (
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
        /* ── Likes Tab ── */
        <div className={styles.body}>
          {likesLoading ? (
            <div className={styles.empty}>Loading...</div>
          ) : likes.length === 0 ? (
            <div className={styles.empty}>{'좋아요한 곡이 없습니다.'}</div>
          ) : (
            <ul className={styles.histList}>
              {likes.map((item) => (
                <li key={item.trackId} className={styles.histItem}>
                  <button
                    className={styles.histPlayBtn}
                    onClick={() => handlePlayLike(item)}
                  >
                    {'\u25B6'}
                  </button>
                  <div className={styles.histInfo}>
                    <div className={styles.histTitle}>{item.title}</div>
                    <div className={styles.histTime}>
                      {item.bpm ? `${item.bpm} BPM` : ''}
                      {item.bpm && item.tonality ? ' \u00B7 ' : ''}
                      {item.tonality ?? ''}
                    </div>
                  </div>
                </li>
              ))}
            </ul>
          )}
        </div>
      )}
    </div>
  );
}

import { useEffect, useLayoutEffect, useState, useRef, useCallback } from 'react';
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
import { classifyLoadError } from '@/api/loadError';
import { useToastStore } from '@/store/toastStore';
import type { Playlist, LikeItem } from '@/types';
import { toPlayableTrack } from '@/utils/playableTrack';
import {
  createOwnerKey,
  createReadKey,
  getCurrentOwnerKey,
  type OwnerProjectionKey,
} from '@/utils/ownerProjection';
import ConfirmDialog from '@/components/ui/ConfirmDialog';
import styles from './PlaylistDrawer.module.css';

type Tab = 'playlists' | 'likes';
type CapacityState = 'loading' | 'known' | 'error';
type DrawerMutationTarget =
  | {
      kind: 'delete-playlist';
      detailKey: string;
      playlistID: number;
      playlistTitle: string;
    }
  | {
      kind: 'remove-track';
      detailKey: string;
      playlistID: number;
      playlistTitle: string;
      trackID: number;
      trackTitle: string;
    };

interface DrawerMutationOperation {
  id: number;
  target: DrawerMutationTarget;
}

interface PlaylistDrawerProps {
  open: boolean;
  onClose: () => void;
}

export default function PlaylistDrawer({ open, onClose }: PlaylistDrawerProps) {
  const accessToken = useAuthStore((s) => s.accessToken);
  const userID = useAuthStore((s) => s.user?.id ?? null);
  const ownerKey = createOwnerKey(userID, accessToken);
  const [tab, setTab] = useState<Tab>('playlists');

  if (!open) return null;

  return (
    <PlaylistDrawerSession
      key={createReadKey(ownerKey, 'playlist-drawer', tab) ?? 'anonymous-drawer'}
      ownerKey={ownerKey}
      tab={tab}
      setTab={setTab}
      onClose={onClose}
    />
  );
}

interface PlaylistDrawerSessionProps {
  ownerKey: OwnerProjectionKey | null;
  tab: Tab;
  setTab: (tab: Tab) => void;
  onClose: () => void;
}

let drawerSessionSequence = 0;

function PlaylistDrawerSession({ ownerKey, tab, setTab, onClose }: PlaylistDrawerSessionProps) {
  const isAuthenticated = ownerKey !== null;
  const open = true;
  const playTrack = usePlayerStore((s) => s.play);
  const showToast = useToastStore((s) => s.show);
  const sessionID = useRef(++drawerSessionSequence).current;
  const readKey = createReadKey(ownerKey, 'playlist-drawer', sessionID, tab);

  /* ── Playlist state ── */
  const [playlists, setPlaylists] = useState<Playlist[]>([]);
  const [maxPlaylists, setMaxPlaylists] = useState<number | null>(null);
  const [capacityState, setCapacityState] = useState<CapacityState>('loading');
  const [selectedPlaylistID, setSelectedPlaylistID] = useState<number | null>(null);
  const [selectedPl, setSelectedPl] = useState<PlaylistDetail | null>(null);
  const [detailLoading, setDetailLoading] = useState(false);
  const [detailError, setDetailError] = useState<string | null>(null);
  const [plLoading, setPlLoading] = useState(false);
  const [playlistError, setPlaylistError] = useState<string | null>(null);
  const [playlistProjectionKey, setPlaylistProjectionKey] = useState<string | null>(null);
  const [capacityProjectionKey, setCapacityProjectionKey] = useState<string | null>(null);
  const [likesProjectionKey, setLikesProjectionKey] = useState<string | null>(null);
  const [detailProjectionKey, setDetailProjectionKey] = useState<string | null>(null);
  const [playlistErrorKey, setPlaylistErrorKey] = useState<string | null>(null);
  const [likesErrorKey, setLikesErrorKey] = useState<string | null>(null);
  const [detailErrorKey, setDetailErrorKey] = useState<string | null>(null);

  /* ── Create form ── */
  const [showCreate, setShowCreate] = useState(false);
  const [newTitle, setNewTitle] = useState('');
  const [creating, setCreating] = useState(false);

  /* ── Likes state ── */
  const [likes, setLikes] = useState<LikeItem[]>([]);
  const [likesLoading, setLikesLoading] = useState(false);
  const [likesError, setLikesError] = useState<string | null>(null);

  /* ── Destructive mutation state ── */
  const [mutationTarget, setMutationTarget] = useState<DrawerMutationTarget | null>(null);
  const [mutationOperation, setMutationOperation] = useState<DrawerMutationOperation | null>(null);
  const [mutationError, setMutationError] = useState<string | null>(null);
  const mutationTargetRef = useRef<DrawerMutationTarget | null>(null);
  const mutationOperationSequenceRef = useRef(0);
  const mutationOperationRef = useRef<DrawerMutationOperation | null>(null);

  const playlistGeneration = useRef(0);
  const playlistController = useRef<AbortController | null>(null);
  const capacityGeneration = useRef(0);
  const capacityController = useRef<AbortController | null>(null);
  const capacityInFlight = useRef(false);
  const likesGeneration = useRef(0);
  const likesController = useRef<AbortController | null>(null);
  const detailGeneration = useRef(0);
  const detailController = useRef<AbortController | null>(null);
  const sessionActive = useRef(false);
  const selectedPlaylistIDRef = useRef<number | null>(null);
  const currentDetailKeyRef = useRef<string | null>(null);
  const currentSelectedPlRef = useRef<PlaylistDetail | null>(null);

  const isCurrentSession = useCallback(
    () => sessionActive.current && getCurrentOwnerKey(ownerKey) === ownerKey,
    [ownerKey],
  );

  useLayoutEffect(() => {
    sessionActive.current = true;
    return () => {
      sessionActive.current = false;
      playlistController.current?.abort();
      capacityController.current?.abort();
      likesController.current?.abort();
      detailController.current?.abort();
      playlistGeneration.current += 1;
      capacityGeneration.current += 1;
      likesGeneration.current += 1;
      detailGeneration.current += 1;
      capacityInFlight.current = false;
      mutationTargetRef.current = null;
      mutationOperationRef.current = null;
    };
  }, []);

  /* ── Drag state ── */
  const dragIdx = useRef<number | null>(null);
  const [dragOverIdx, setDragOverIdx] = useState<number | null>(null);

  /* ── Touch DnD refs ── */
  const touchDragIdx = useRef<number | null>(null);
  const trackListRef = useRef<HTMLUListElement>(null);

  /* ── Load playlists ── */
  const loadPlaylists = useCallback(async () => {
    if (!open || !isAuthenticated || tab !== 'playlists' || !isCurrentSession()) return;
    playlistController.current?.abort();
    const controller = new AbortController();
    playlistController.current = controller;
    const generation = ++playlistGeneration.current;
    const isCurrent = () => generation === playlistGeneration.current && isCurrentSession();
    setPlLoading(true);
    setPlaylistError(null);
    setPlaylists([]);
    try {
      const response = await fetchMyPlaylists(controller.signal);
      if (isCurrent()) {
        setPlaylists(response.dataList ?? []);
        setPlaylistProjectionKey(readKey);
      }
    } catch (loadError) {
      if (isCurrent() && classifyLoadError(loadError) !== 'cancelled') {
        setPlaylistError('재생목록을 불러오지 못했습니다.');
        setPlaylistErrorKey(readKey);
      }
    } finally {
      if (isCurrent()) setPlLoading(false);
    }
  }, [isAuthenticated, isCurrentSession, open, readKey, tab]);

  const loadCapacity = useCallback(async () => {
    if (
      !open ||
      !isAuthenticated ||
      tab !== 'playlists' ||
      capacityInFlight.current ||
      !isCurrentSession()
    ) {
      return;
    }
    capacityInFlight.current = true;
    capacityController.current?.abort();
    const controller = new AbortController();
    capacityController.current = controller;
    const generation = ++capacityGeneration.current;
    const isCurrent = () => generation === capacityGeneration.current && isCurrentSession();
    setMaxPlaylists(null);
    setCapacityState('loading');
    try {
      const response = await fetchMySubscription(controller.signal);
      const capacity = response.subscription?.maxPlaylists;
      if (!Number.isSafeInteger(capacity) || (capacity ?? 0) <= 0) {
        throw new Error('Invalid playlist capacity');
      }
      if (isCurrent()) {
        setMaxPlaylists(capacity!);
        setCapacityState('known');
        setCapacityProjectionKey(readKey);
      }
    } catch (loadError) {
      if (isCurrent() && classifyLoadError(loadError) !== 'cancelled') {
        setCapacityState('error');
      }
    } finally {
      if (isCurrent()) capacityInFlight.current = false;
    }
  }, [isAuthenticated, isCurrentSession, open, readKey, tab]);

  /* ── Load likes ── */
  const loadLikes = useCallback(async () => {
    if (!open || !isAuthenticated || tab !== 'likes' || !isCurrentSession()) return;
    likesController.current?.abort();
    const controller = new AbortController();
    likesController.current = controller;
    const generation = ++likesGeneration.current;
    const isCurrent = () => generation === likesGeneration.current && isCurrentSession();
    setLikesLoading(true);
    setLikesError(null);
    setLikes([]);
    try {
      const res = await fetchLikes(controller.signal);
      if (isCurrent()) {
        setLikes(res.dataList ?? []);
        setLikesProjectionKey(readKey);
      }
    } catch (loadError) {
      if (isCurrent() && classifyLoadError(loadError) !== 'cancelled') {
        setLikesError('좋아요 목록을 불러오지 못했습니다.');
        setLikesErrorKey(readKey);
      }
    } finally {
      if (isCurrent()) setLikesLoading(false);
    }
  }, [isAuthenticated, isCurrentSession, open, readKey, tab]);

  const loadPlaylistDetail = useCallback(
    async (playlistID: number, preserveCurrent = false) => {
      if (!open || !isAuthenticated || tab !== 'playlists' || !isCurrentSession()) return;
      detailController.current?.abort();
      const controller = new AbortController();
      detailController.current = controller;
      const generation = ++detailGeneration.current;
      const isCurrent = () => generation === detailGeneration.current && isCurrentSession();
      const detailReadKey = createReadKey(readKey, 'playlist-detail', playlistID);
      setDetailLoading(true);
      setDetailError(null);
      if (!preserveCurrent) setSelectedPl(null);
      try {
        const detail = await fetchPlaylistDetail(playlistID, controller.signal);
        if (isCurrent()) {
          setSelectedPl(detail);
          setDetailProjectionKey(detailReadKey);
        }
      } catch (loadError) {
        if (isCurrent() && !preserveCurrent && classifyLoadError(loadError) !== 'cancelled') {
          setDetailError('재생목록 상세를 불러오지 못했습니다.');
          setDetailErrorKey(detailReadKey);
        }
      } finally {
        if (isCurrent()) setDetailLoading(false);
      }
    },
    [isAuthenticated, isCurrentSession, open, readKey, tab],
  );

  useEffect(() => {
    setShowCreate(false);
    setSelectedPlaylistID(null);
    setSelectedPl(null);
    setDetailError(null);
    setDetailLoading(false);

    if (open && isAuthenticated) {
      if (tab === 'playlists') {
        void loadPlaylists();
        void loadCapacity();
      } else {
        void loadLikes();
      }
    }

    return () => {
      playlistController.current?.abort();
      capacityController.current?.abort();
      likesController.current?.abort();
      detailController.current?.abort();
      playlistGeneration.current += 1;
      capacityGeneration.current += 1;
      likesGeneration.current += 1;
      detailGeneration.current += 1;
      capacityInFlight.current = false;
    };
  }, [isAuthenticated, loadCapacity, loadLikes, loadPlaylists, open, tab]);

  const currentPlaylists = playlistProjectionKey === readKey ? playlists : [];
  const currentLikes = likesProjectionKey === readKey ? likes : [];
  const currentPlaylistError = playlistErrorKey === readKey ? playlistError : null;
  const currentLikesError = likesErrorKey === readKey ? likesError : null;
  const capacityCurrent = capacityProjectionKey === readKey && capacityState === 'known';
  const currentSelectedDetailKey = createReadKey(readKey, 'playlist-detail', selectedPlaylistID);
  const currentSelectedPl = detailProjectionKey === currentSelectedDetailKey ? selectedPl : null;
  const currentDetailError = detailErrorKey === currentSelectedDetailKey ? detailError : null;
  selectedPlaylistIDRef.current = selectedPlaylistID;
  currentDetailKeyRef.current = currentSelectedDetailKey;
  currentSelectedPlRef.current = currentSelectedPl;
  const currentMutationTarget =
    mutationTarget !== null && isMutationTargetCurrent(mutationTarget) ? mutationTarget : null;
  const currentMutationOperation =
    mutationOperation !== null && isMutationTargetCurrent(mutationOperation.target)
      ? mutationOperation
      : null;
  const mutationPending = currentMutationOperation !== null;
  const canCreate =
    playlistProjectionKey === readKey &&
    capacityCurrent &&
    maxPlaylists !== null &&
    currentPlaylists.length < maxPlaylists;

  /* ── Playlist handlers ── */

  async function openPlaylist(pl: Playlist) {
    if (
      !isCurrentSession() ||
      playlistProjectionKey !== readKey ||
      !currentPlaylists.some((item) => item.id === pl.id)
    ) {
      return;
    }
    setSelectedPlaylistID(pl.id);
    await loadPlaylistDetail(pl.id);
  }

  function isMutationTargetCurrent(target: DrawerMutationTarget): boolean {
    const currentDetail = currentSelectedPlRef.current;
    if (
      !isCurrentSession() ||
      currentDetailKeyRef.current !== target.detailKey ||
      selectedPlaylistIDRef.current !== target.playlistID ||
      currentDetail?.id !== target.playlistID
    ) {
      return false;
    }

    return (
      target.kind === 'delete-playlist' ||
      currentDetail.tracks.some((track) => track.trackId === target.trackID)
    );
  }

  function isActiveMutationOperation(operation: DrawerMutationOperation): boolean {
    const activeOperation = mutationOperationRef.current;
    return activeOperation?.id === operation.id && isMutationTargetCurrent(operation.target);
  }

  function clearMutationConfirmation() {
    mutationTargetRef.current = null;
    mutationOperationRef.current = null;
    setMutationTarget(null);
    setMutationOperation(null);
    setMutationError(null);
  }

  function openMutationConfirmation(target: DrawerMutationTarget) {
    if (
      mutationOperationRef.current !== null ||
      mutationTargetRef.current !== null ||
      !isMutationTargetCurrent(target)
    ) {
      return;
    }
    mutationTargetRef.current = target;
    setMutationTarget(target);
    setMutationError(null);
  }

  function closePlaylistDetail() {
    clearMutationConfirmation();
    detailController.current?.abort();
    detailGeneration.current += 1;
    setSelectedPlaylistID(null);
    setSelectedPl(null);
    setDetailError(null);
    setDetailLoading(false);
  }

  async function handleCreate() {
    const operationKey = readKey;
    if (!newTitle.trim() || operationKey === null || !isCurrentSession() || !canCreate) {
      return;
    }
    setCreating(true);
    try {
      await createPlaylist({ title: newTitle.trim() });
      if (!isCurrentSession()) return;
      setNewTitle('');
      setShowCreate(false);
      await loadPlaylists();
    } catch (err) {
      if (!isCurrentSession()) return;
      const code = await getApiErrorCode(err);
      if (code === 'PLAYLIST_LIMIT_EXCEEDED') {
        showToast('error', '구독 플랜의 재생목록 한도를 초과했습니다. 플랜을 업그레이드해주세요.');
      } else {
        showToast('error', '재생목록 생성에 실패했습니다.');
      }
    }
    if (isCurrentSession()) setCreating(false);
  }

  function requestDeletePlaylist(id: number) {
    if (
      currentSelectedDetailKey === null ||
      !isCurrentSession() ||
      selectedPlaylistID !== id ||
      currentSelectedPl?.id !== id
    ) {
      return;
    }
    openMutationConfirmation({
      kind: 'delete-playlist',
      detailKey: currentSelectedDetailKey,
      playlistID: id,
      playlistTitle: currentSelectedPl.title,
    });
  }

  function requestRemoveTrack(track: PlaylistTrack) {
    if (
      currentSelectedDetailKey === null ||
      !currentSelectedPl ||
      !isCurrentSession() ||
      !currentSelectedPl.tracks.some((item) => item.trackId === track.trackId)
    ) {
      return;
    }
    openMutationConfirmation({
      kind: 'remove-track',
      detailKey: currentSelectedDetailKey,
      playlistID: currentSelectedPl.id,
      playlistTitle: currentSelectedPl.title,
      trackID: track.trackId,
      trackTitle: track.title,
    });
  }

  async function confirmMutation() {
    const target = mutationTargetRef.current;
    if (
      target === null ||
      mutationOperationRef.current !== null ||
      !isMutationTargetCurrent(target)
    ) {
      return;
    }
    const operation: DrawerMutationOperation = {
      id: ++mutationOperationSequenceRef.current,
      target,
    };
    mutationOperationRef.current = operation;
    setMutationOperation(operation);
    setMutationError(null);

    try {
      if (target.kind === 'delete-playlist') {
        await deletePlaylist(target.playlistID);
      } else {
        await removeTrackFromPlaylist(target.playlistID, target.trackID);
      }
      if (!isActiveMutationOperation(operation)) return;

      clearMutationConfirmation();
      if (target.kind === 'delete-playlist') {
        closePlaylistDetail();
        await loadPlaylists();
      } else {
        await loadPlaylistDetail(target.playlistID, true);
      }
    } catch {
      if (isActiveMutationOperation(operation)) {
        setMutationError(
          target.kind === 'delete-playlist'
            ? '재생목록을 삭제하지 못했습니다. 다시 시도해 주세요.'
            : '재생목록에서 곡을 삭제하지 못했습니다. 다시 시도해 주세요.',
        );
      }
    } finally {
      if (mutationOperationRef.current?.id === operation.id) {
        mutationOperationRef.current = null;
        if (isCurrentSession()) setMutationOperation(null);
      }
    }
  }

  function handlePlayTrack(t: PlaylistTrack) {
    if (
      !isCurrentSession() ||
      !currentSelectedPl?.tracks.some((track) => track.trackId === t.trackId)
    ) {
      return;
    }
    playTrack(toPlayableTrack(t));
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
    if (
      dragIdx.current === null ||
      dragIdx.current === idx ||
      !currentSelectedPl ||
      !isCurrentSession()
    ) {
      dragIdx.current = null;
      setDragOverIdx(null);
      return;
    }

    const previousDetail = currentSelectedPl;
    const tracks = [...currentSelectedPl.tracks];
    const [moved] = tracks.splice(dragIdx.current, 1);
    tracks.splice(idx, 0, moved);

    // Optimistic update
    const reordered = tracks.map((t, i) => ({ ...t, trackOrder: i }));
    setSelectedPl({ ...currentSelectedPl, tracks: reordered });
    dragIdx.current = null;
    setDragOverIdx(null);

    // API call
    try {
      await reorderTracks(
        currentSelectedPl.id,
        reordered.map((t) => ({ trackId: t.trackId, trackOrder: t.trackOrder })),
      );
    } catch {
      if (!isCurrentSession() || selectedPlaylistID !== previousDetail.id) {
        return;
      }
      setSelectedPl(previousDetail);
      await loadPlaylistDetail(currentSelectedPl.id, true);
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
    if (
      !isCurrentSession() ||
      likesProjectionKey !== readKey ||
      !currentLikes.some((like) => like.trackId === item.trackId)
    ) {
      return;
    }
    playTrack(toPlayableTrack(item));
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
            onClick={() => {
              setTab('playlists');
              closePlaylistDetail();
            }}
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
        <button className={styles.closeBtn} onClick={onClose} aria-label="닫기">
          &times;
        </button>
      </div>

      {!isAuthenticated ? (
        <div className={styles.empty}>로그인이 필요합니다.</div>
      ) : tab === 'playlists' ? (
        /* ── Playlists Tab ── */
        selectedPlaylistID !== null ? (
          currentSelectedPl ? (
            /* Detail view */
            <div className={styles.body}>
              <div className={styles.detailHeader}>
                <button className={styles.backBtn} onClick={closePlaylistDetail}>
                  {'\u2190'}
                </button>
                <span className={styles.detailTitle}>{currentSelectedPl.title}</span>
                <span className={styles.detailCount}>{currentSelectedPl.tracks.length}곡</span>
                <button
                  className={styles.deletePlaylistBtn}
                  onClick={() => requestDeletePlaylist(currentSelectedPl.id)}
                  disabled={mutationPending}
                  title="재생목록 삭제"
                >
                  {'\u2715'}
                </button>
              </div>
              {currentSelectedPl.tracks.length === 0 ? (
                <div className={styles.empty}>곡이 없습니다.</div>
              ) : (
                <ul
                  className={styles.trackList}
                  ref={trackListRef}
                  onTouchMove={handleTouchMove}
                  onTouchEnd={handleTouchEnd}
                >
                  {currentSelectedPl.tracks.map((t, idx) => (
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
                      <button className={styles.trackPlayBtn} onClick={() => handlePlayTrack(t)}>
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
                        onClick={() => requestRemoveTrack(t)}
                        disabled={mutationPending}
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
            <div className={styles.body}>
              <div className={styles.detailHeader}>
                <button className={styles.backBtn} onClick={closePlaylistDetail}>
                  {'\u2190'}
                </button>
              </div>
              <div className={styles.empty}>
                {detailLoading
                  ? '재생목록 상세를 불러오는 중입니다.'
                  : (currentDetailError ?? '재생목록 상세를 불러오지 못했습니다.')}
              </div>
            </div>
          )
        ) : (
          /* List view */
          <div className={styles.body}>
            {plLoading ? (
              <div className={styles.empty}>Loading...</div>
            ) : currentPlaylistError ? (
              <div className={styles.empty}>{currentPlaylistError}</div>
            ) : (
              <>
                <ul className={styles.plList}>
                  {currentPlaylists.map((pl) => (
                    <li key={pl.id} className={styles.plItem}>
                      <button className={styles.plItemBtn} onClick={() => openPlaylist(pl)}>
                        <span className={styles.plIcon}>{'\u266A'}</span>
                        <div className={styles.plItemInfo}>
                          <span className={styles.plItemName}>{pl.title}</span>
                          <span className={styles.plItemCount}>{pl.trackCount}곡</span>
                        </div>
                        <span className={styles.plArrow}>{'\u203A'}</span>
                      </button>
                    </li>
                  ))}
                </ul>

                <div className={styles.capacityStatus}>
                  {!capacityCurrent && capacityState !== 'error' ? (
                    '재생목록 생성 한도를 확인하는 중입니다.'
                  ) : capacityState === 'error' ? (
                    <>
                      <span>{'재생목록 생성 한도를 확인하지 못했습니다.'}</span>
                      <button
                        type="button"
                        className={styles.capacityRetryBtn}
                        onClick={() => void loadCapacity()}
                      >
                        {'한도 다시 확인'}
                      </button>
                    </>
                  ) : (
                    <span>{`최대 ${maxPlaylists}개`}</span>
                  )}
                </div>

                {/* Create new */}
                {canCreate && !showCreate && (
                  <button className={styles.createBtn} onClick={() => setShowCreate(true)}>
                    + 새 재생목록
                  </button>
                )}

                {showCreate && canCreate && (
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
          ) : currentLikesError ? (
            <div className={styles.empty}>{currentLikesError}</div>
          ) : currentLikes.length === 0 ? (
            <div className={styles.empty}>{'좋아요한 곡이 없습니다.'}</div>
          ) : (
            <ul className={styles.histList}>
              {currentLikes.map((item) => (
                <li key={item.trackId} className={styles.histItem}>
                  <button className={styles.histPlayBtn} onClick={() => handlePlayLike(item)}>
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
      <ConfirmDialog
        open={currentMutationTarget !== null}
        title={currentMutationTarget?.kind === 'remove-track' ? '곡 삭제' : '재생목록 삭제'}
        message={
          mutationError ??
          (currentMutationTarget?.kind === 'remove-track'
            ? `"${currentMutationTarget.trackTitle}"을(를) "${currentMutationTarget.playlistTitle}" 재생목록에서 삭제하시겠습니까?`
            : `"${currentMutationTarget?.playlistTitle ?? ''}" 재생목록을 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.`)
        }
        confirmLabel={mutationError ? '다시 시도' : '삭제'}
        confirmVariant="danger"
        busy={mutationPending}
        onConfirm={() => void confirmMutation()}
        onCancel={clearMutationConfirmation}
      />
    </div>
  );
}

import { useCallback, useEffect, useLayoutEffect, useMemo, useRef, useState } from 'react';
import { fetchMyPlaylists, addTrackToPlaylist } from '@/api/playlists';
import { isSubscriptionRequired } from '@/api/client';
import type { Playlist } from '@/types';
import Modal from '@/components/ui/Modal';
import styles from './AddToPlaylistModal.module.css';

interface AddToPlaylistModalProps {
  open: boolean;
  trackId: number | null;
  onClose: () => void;
  onSubscriptionRequired?: () => void;
}

type PlaylistLoadState = 'idle' | 'loading' | 'ready' | 'error' | 'subscription-required';

export default function AddToPlaylistModal({
  open,
  trackId,
  onClose,
  onSubscriptionRequired,
}: AddToPlaylistModalProps) {
  const lifecycleKey = useMemo(
    () => Symbol(`add-to-playlist-lifecycle:${open ? 'open' : 'closed'}:${trackId ?? 'no-track'}`),
    [open, trackId],
  );
  const [playlists, setPlaylists] = useState<Playlist[]>([]);
  const [loadState, setLoadState] = useState<PlaylistLoadState>('idle');
  const [selectedId, setSelectedId] = useState<number | null>(null);
  const [adding, setAdding] = useState(false);
  const [result, setResult] = useState<'success' | 'error' | 'duplicate' | null>(null);
  const [projectionLifecycleKey, setProjectionLifecycleKey] = useState<symbol | null>(null);
  const lifecycleGenerationRef = useRef(0);
  const lifecycleKeyRef = useRef(lifecycleKey);
  const stateLifecycleGenerationRef = useRef<number | null>(null);
  const readyLifecycleGenerationRef = useRef<number | null>(null);
  const loadGenerationRef = useRef(0);
  const loadInFlightRef = useRef<number | null>(null);
  const loadControllerRef = useRef<AbortController | null>(null);
  const addOperationSequenceRef = useRef(0);
  const addOperationRef = useRef<number | null>(null);
  const closeTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const onCloseRef = useRef(onClose);
  const onSubscriptionRequiredRef = useRef(onSubscriptionRequired);

  useEffect(() => {
    onCloseRef.current = onClose;
    onSubscriptionRequiredRef.current = onSubscriptionRequired;
  }, [onClose, onSubscriptionRequired]);

  function clearCloseTimer() {
    if (closeTimerRef.current !== null) {
      clearTimeout(closeTimerRef.current);
      closeTimerRef.current = null;
    }
  }

  const loadPlaylists = useCallback((lifecycleGeneration: number, loadLifecycleKey: symbol) => {
    if (
      lifecycleGenerationRef.current !== lifecycleGeneration ||
      lifecycleKeyRef.current !== loadLifecycleKey
    ) {
      return;
    }
    loadControllerRef.current?.abort();
    const controller = new AbortController();
    loadControllerRef.current = controller;
    const loadGeneration = ++loadGenerationRef.current;
    loadInFlightRef.current = loadGeneration;
    const isCurrent = () =>
      lifecycleGenerationRef.current === lifecycleGeneration &&
      lifecycleKeyRef.current === loadLifecycleKey &&
      loadGenerationRef.current === loadGeneration &&
      !controller.signal.aborted;

    stateLifecycleGenerationRef.current = lifecycleGeneration;
    readyLifecycleGenerationRef.current = null;
    setProjectionLifecycleKey(loadLifecycleKey);
    setPlaylists([]);
    setSelectedId(null);
    setResult(null);
    setLoadState('loading');

    void fetchMyPlaylists(controller.signal)
      .then((res) => {
        if (!isCurrent()) return;
        setPlaylists(res.dataList ?? []);
        readyLifecycleGenerationRef.current = lifecycleGeneration;
        setLoadState('ready');
      })
      .catch((err) => {
        if (!isCurrent()) return;
        if (isSubscriptionRequired(err)) {
          setLoadState('subscription-required');
          onSubscriptionRequiredRef.current?.();
          return;
        }
        setLoadState('error');
      })
      .finally(() => {
        if (loadInFlightRef.current === loadGeneration) {
          loadInFlightRef.current = null;
        }
      });
  }, []);

  useLayoutEffect(() => {
    const generation = ++lifecycleGenerationRef.current;
    lifecycleKeyRef.current = lifecycleKey;
    stateLifecycleGenerationRef.current = null;
    readyLifecycleGenerationRef.current = null;
    loadControllerRef.current?.abort();
    loadGenerationRef.current += 1;
    loadInFlightRef.current = null;
    addOperationRef.current = null;
    clearCloseTimer();

    return () => {
      if (lifecycleGenerationRef.current === generation) {
        lifecycleGenerationRef.current += 1;
      }
      stateLifecycleGenerationRef.current = null;
      readyLifecycleGenerationRef.current = null;
      loadControllerRef.current?.abort();
      loadGenerationRef.current += 1;
      loadInFlightRef.current = null;
      addOperationRef.current = null;
      clearCloseTimer();
    };
  }, [lifecycleKey]);

  useEffect(() => {
    const generation = lifecycleGenerationRef.current;
    setPlaylists([]);
    setSelectedId(null);
    setAdding(false);
    setResult(null);

    if (!open) {
      setLoadState('idle');
      return;
    }

    loadPlaylists(generation, lifecycleKey);

    return () => {
      loadControllerRef.current?.abort();
      loadGenerationRef.current += 1;
      loadInFlightRef.current = null;
      addOperationRef.current = null;
      clearCloseTimer();
    };
  }, [lifecycleKey, loadPlaylists, open]);

  useEffect(
    () => () => {
      loadControllerRef.current?.abort();
      clearCloseTimer();
    },
    [],
  );

  function handleRetryLoad() {
    const generation = lifecycleGenerationRef.current;
    if (
      !open ||
      lifecycleKeyRef.current !== lifecycleKey ||
      projectionLifecycleKey !== lifecycleKey ||
      stateLifecycleGenerationRef.current !== generation ||
      loadState === 'loading' ||
      loadInFlightRef.current !== null
    ) {
      return;
    }
    loadPlaylists(generation, lifecycleKey);
  }

  function handleSelectPlaylist(playlistID: number) {
    if (
      lifecycleKeyRef.current !== lifecycleKey ||
      projectionLifecycleKey !== lifecycleKey ||
      readyLifecycleGenerationRef.current !== lifecycleGenerationRef.current ||
      !playlists.some((playlist) => playlist.id === playlistID)
    ) {
      return;
    }
    setSelectedId(playlistID);
  }

  async function handleAdd() {
    const generation = lifecycleGenerationRef.current;
    if (
      !open ||
      lifecycleKeyRef.current !== lifecycleKey ||
      projectionLifecycleKey !== lifecycleKey ||
      readyLifecycleGenerationRef.current !== generation ||
      selectedId === null ||
      trackId === null ||
      loadState !== 'ready' ||
      addOperationRef.current !== null
    ) {
      return;
    }
    const operationID = ++addOperationSequenceRef.current;
    addOperationRef.current = operationID;
    setAdding(true);
    try {
      await addTrackToPlaylist(selectedId, trackId);
      if (
        lifecycleGenerationRef.current !== generation ||
        lifecycleKeyRef.current !== lifecycleKey
      ) {
        return;
      }
      setResult('success');
      clearCloseTimer();
      closeTimerRef.current = setTimeout(() => {
        if (
          lifecycleGenerationRef.current === generation &&
          lifecycleKeyRef.current === lifecycleKey
        ) {
          onCloseRef.current();
        }
      }, 800);
    } catch (err) {
      if (
        lifecycleGenerationRef.current !== generation ||
        lifecycleKeyRef.current !== lifecycleKey ||
        addOperationRef.current !== operationID
      ) {
        return;
      }
      if (isSubscriptionRequired(err)) {
        setPlaylists([]);
        setSelectedId(null);
        setResult(null);
        setLoadState('subscription-required');
        onSubscriptionRequiredRef.current?.();
        return;
      }
      const axErr = err as import('axios').AxiosError<{ errorCode?: string }>;
      if (axErr.response?.status === 409) {
        setResult('duplicate');
        return;
      }
      setResult('error');
    } finally {
      if (
        lifecycleGenerationRef.current === generation &&
        lifecycleKeyRef.current === lifecycleKey &&
        addOperationRef.current === operationID
      ) {
        addOperationRef.current = null;
        setAdding(false);
      }
    }
  }

  if (!open) return null;

  const projectionIsCurrent = projectionLifecycleKey === lifecycleKey;
  const currentLoadState = projectionIsCurrent ? loadState : 'loading';
  const currentPlaylists = projectionIsCurrent ? playlists : [];
  const currentSelectedID = projectionIsCurrent ? selectedId : null;
  const currentAdding = projectionIsCurrent ? adding : false;
  const currentResult = projectionIsCurrent ? result : null;

  return (
    <Modal open={open} onClose={onClose} title="재생목록에 추가">
      {currentLoadState === 'idle' || currentLoadState === 'loading' ? (
        <div className={styles.stateMessage}>재생목록을 불러오는 중입니다.</div>
      ) : currentLoadState === 'error' ? (
        <div className={styles.stateMessage}>
          <div className={styles.errorMsg}>재생목록을 불러오지 못했습니다.</div>
          <button className={styles.retryBtn} type="button" onClick={handleRetryLoad}>
            다시 시도
          </button>
        </div>
      ) : currentLoadState === 'subscription-required' ? (
        <div className={styles.stateMessage}>구독이 필요한 기능입니다.</div>
      ) : currentPlaylists.length === 0 ? (
        <div className={styles.empty}>
          재생목록이 없습니다.
          <br />
          재생목록을 먼저 만들어주세요.
        </div>
      ) : currentResult === 'success' ? (
        <div className={styles.success}>추가되었습니다!</div>
      ) : currentResult === 'duplicate' ? (
        <div className={styles.errorMsg}>이미 재생목록에 추가된 트랙입니다.</div>
      ) : (
        <>
          <ul className={styles.list}>
            {currentPlaylists.map((pl) => (
              <li key={pl.id}>
                <button
                  className={`${styles.plBtn} ${currentSelectedID === pl.id ? styles.plBtnSelected : ''}`}
                  onClick={() => handleSelectPlaylist(pl.id)}
                >
                  <span className={styles.plIcon}>{'\u266A'}</span>
                  <span className={styles.plName}>{pl.title}</span>
                  <span className={styles.plCount}>{pl.trackCount}곡</span>
                </button>
              </li>
            ))}
          </ul>
          {currentResult === 'error' && <div className={styles.errorMsg}>추가에 실패했습니다.</div>}
          <div className={styles.footer}>
            <button className={styles.cancelBtn} onClick={onClose}>
              취소
            </button>
            <button
              className={styles.addBtn}
              onClick={handleAdd}
              disabled={!currentSelectedID || currentAdding}
            >
              {currentAdding ? '...' : '추가'}
            </button>
          </div>
        </>
      )}
    </Modal>
  );
}

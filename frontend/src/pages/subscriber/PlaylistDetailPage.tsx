import { useState, useEffect, useLayoutEffect, useCallback, useRef } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import {
  fetchPlaylistDetail,
  removeTrackFromPlaylist,
  type PlaylistDetail,
  type PlaylistTrack,
} from '@/api/playlists';
import { downloadTrack, triggerBlobDownload } from '@/api/downloads';
import { classifyLoadError } from '@/api/loadError';
import { usePlayerStore } from '@/store/playerStore';
import { useLikeStore } from '@/store/likeStore';
import { useAuthStore } from '@/store/authStore';
import { useToastStore } from '@/store/toastStore';
import { toPlayableTrack } from '@/utils/playableTrack';
import { parsePositiveDecimalRouteID } from '@/utils/routeId';
import { createOwnerKey, createReadKey, getCurrentOwnerKey } from '@/utils/ownerProjection';
import AddToPlaylistModal from '@/components/playlist/AddToPlaylistModal';
import Button from '@/components/ui/Button';
import Modal from '@/components/ui/Modal';
import styles from './PlaylistDetailPage.module.css';

export default function PlaylistDetailPage() {
  const { playlistId } = useParams<{ playlistId: string }>();
  const navigate = useNavigate();
  const id = parsePositiveDecimalRouteID(playlistId);
  const validID = id !== null;

  /* ── Stores ── */
  const currentTrack = usePlayerStore((s) => s.currentTrack);
  const isPlayerPlaying = usePlayerStore((s) => s.isPlaying);
  const playTrack = usePlayerStore((s) => s.play);
  const pauseTrack = usePlayerStore((s) => s.pause);
  const resumeTrack = usePlayerStore((s) => s.resume);
  const addToPlayerQueue = usePlayerStore((s) => s.addToQueue);
  const setTrackListContext = usePlayerStore((s) => s.setTrackListContext);
  const likeStore = useLikeStore();
  const user = useAuthStore((s) => s.user);
  const accessToken = useAuthStore((s) => s.accessToken);
  const toast = useToastStore((s) => s.show);

  /* ── State ── */
  const [detail, setDetail] = useState<PlaylistDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const requestGeneration = useRef(0);
  const requestController = useRef<AbortController | null>(null);
  const ownerKey = createOwnerKey(user?.id ?? null, accessToken);
  const readKey = createReadKey(ownerKey, 'playlist-detail', id);
  const currentReadKeyRef = useRef(readKey);
  const projectionKeyRef = useRef<string | null>(null);
  const [projectionKey, setProjectionKey] = useState<string | null>(null);
  const [errorKey, setErrorKey] = useState<string | null>(null);
  currentReadKeyRef.current = readKey;

  const projectionCurrent = readKey !== null && projectionKey === readKey;
  const currentDetail = projectionCurrent ? detail : null;
  const currentError = errorKey === readKey ? error : null;
  const currentLoading = loading || (!projectionCurrent && currentError === null);

  function isCurrentProjection(expectedReadKey = readKey): boolean {
    return (
      expectedReadKey !== null &&
      currentReadKeyRef.current === expectedReadKey &&
      projectionKeyRef.current === expectedReadKey &&
      getCurrentOwnerKey(ownerKey) === ownerKey
    );
  }

  /* ── Add to playlist modal ── */
  const [addToPlTrackId, setAddToPlTrackId] = useState<number | null>(null);

  /* ── Remove track confirm ── */
  const [removeTarget, setRemoveTarget] = useState<PlaylistTrack | null>(null);
  const [removing, setRemoving] = useState(false);

  /* ── Fetch ── */
  const load = useCallback(async () => {
    const requestKey = readKey;
    const requestOwnerKey = ownerKey;
    if (id === null || requestKey === null) return;
    requestController.current?.abort();
    const controller = new AbortController();
    requestController.current = controller;
    const generation = ++requestGeneration.current;
    const isCurrent = () =>
      generation === requestGeneration.current &&
      currentReadKeyRef.current === requestKey &&
      getCurrentOwnerKey(requestOwnerKey) === requestOwnerKey;
    try {
      setLoading(true);
      setError(null);
      setDetail(null);
      const data = await fetchPlaylistDetail(id, controller.signal);
      if (isCurrent()) {
        setDetail(data);
        projectionKeyRef.current = requestKey;
        setProjectionKey(requestKey);
      }
    } catch (loadError) {
      if (isCurrent() && classifyLoadError(loadError) !== 'cancelled') {
        setError('재생목록을 불러오지 못했습니다.');
        setErrorKey(requestKey);
      }
    } finally {
      if (isCurrent()) setLoading(false);
    }
  }, [id, ownerKey, readKey]);

  useEffect(() => {
    if (!validID) {
      requestController.current?.abort();
      requestGeneration.current += 1;
      setDetail(null);
      setLoading(false);
      setError(null);
      return;
    }
    void load();
    return () => {
      requestController.current?.abort();
      requestGeneration.current += 1;
    };
  }, [accessToken, load, user?.id, validID]);

  /* ── Like toggle (fetch on mount) ── */
  useEffect(() => {
    if (user) likeStore.load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [user]);

  /* SR-83: Publish playlist tracks as player context so Next/Prev traverses them. */
  useLayoutEffect(() => {
    if (!currentDetail) return;
    const tracks = currentDetail.tracks.map((track) => toPlayableTrack(track));
    return setTrackListContext(tracks);
  }, [currentDetail, setTrackListContext]);

  /* ── Track action handlers ── */
  function handlePlay(track: PlaylistTrack) {
    if (
      !isCurrentProjection() ||
      !currentDetail?.tracks.some((item) => item.trackId === track.trackId)
    ) {
      return;
    }
    if (currentTrack?.id === track.trackId) {
      if (isPlayerPlaying) pauseTrack();
      else resumeTrack();
      return;
    }
    playTrack(toPlayableTrack(track));
  }

  function handleToggleLike(trackId: number) {
    if (!isCurrentProjection() || !currentDetail?.tracks.some((item) => item.trackId === trackId)) {
      return;
    }
    likeStore.toggle(trackId);
  }

  async function handleDownload(track: PlaylistTrack) {
    const operationKey = readKey;
    if (
      !isCurrentProjection(operationKey) ||
      !currentDetail?.tracks.some((item) => item.trackId === track.trackId)
    ) {
      return;
    }
    try {
      const blob = await downloadTrack(track.trackId);
      if (!isCurrentProjection(operationKey)) return;
      triggerBlobDownload(blob, `${track.title}.mp3`);
    } catch {
      if (!isCurrentProjection(operationKey)) return;
      toast('error', '다운로드에 실패했습니다.');
    }
  }

  /* ── Remove track handler ── */
  async function handleRemoveTrack() {
    const operationKey = readKey;
    if (
      !removeTarget ||
      id === null ||
      !isCurrentProjection(operationKey) ||
      !currentDetail?.tracks.some((item) => item.trackId === removeTarget.trackId)
    ) {
      return;
    }
    try {
      setRemoving(true);
      await removeTrackFromPlaylist(id, removeTarget.trackId);
      if (!isCurrentProjection(operationKey)) return;
      setRemoveTarget(null);
      await load();
    } catch (err) {
      if (!isCurrentProjection(operationKey)) return;
      setError(err instanceof Error ? err.message : '곡 삭제에 실패했습니다.');
      setErrorKey(operationKey);
    } finally {
      if (isCurrentProjection(operationKey)) setRemoving(false);
    }
  }

  /* ── Add all tracks to player queue ── */
  function handleAddAllToQueue() {
    if (!isCurrentProjection() || !currentDetail || currentDetail.tracks.length === 0) return;
    for (const track of currentDetail.tracks) {
      addToPlayerQueue(toPlayableTrack(track));
    }
    toast('success', '전체 곡이 대기열에 추가되었습니다.');
  }

  /* ── Render ── */

  if (id === null) {
    return (
      <div className={styles.page}>
        <div className={styles.error}>{'재생목록 주소가 올바르지 않습니다.'}</div>
        <Link to="/playlists" className={styles.backLink}>
          {'재생목록 목록으로'}
        </Link>
      </div>
    );
  }

  if (currentLoading) {
    return (
      <div className={styles.page}>
        <div className={styles.loading}>{'재생목록을 불러오는 중...'}</div>
      </div>
    );
  }

  if (currentError) {
    return (
      <div className={styles.page}>
        <div className={styles.error}>{currentError}</div>
      </div>
    );
  }

  if (!currentDetail) {
    return (
      <div className={styles.page}>
        <div className={styles.error}>{'재생목록을 찾을 수 없습니다.'}</div>
      </div>
    );
  }

  return (
    <div className={styles.page}>
      {/* Back */}
      <Link to="/playlists" className={styles.backLink}>
        {'\u2190 재생목록으로 돌아가기'}
      </Link>

      {/* Header */}
      <div className={styles.pageHeader}>
        <div className={styles.titleArea}>
          <h1 className={styles.pageTitle}>{currentDetail.title}</h1>
          <span className={styles.trackCount}>{currentDetail.tracks.length}곡</span>
        </div>
        <div className={styles.headerActions}>
          {currentDetail.tracks.length > 0 && (
            <Button variant="ghost" size="sm" onClick={handleAddAllToQueue}>
              {'전체 대기열 추가'}
            </Button>
          )}
          <Button
            variant="ghost"
            size="sm"
            onClick={() => {
              if (isCurrentProjection()) navigate(`/playlists/${id}/edit`);
            }}
          >
            {'편집'}
          </Button>
        </div>
      </div>

      {/* Track Table */}
      {currentDetail.tracks.length === 0 ? (
        <div className={styles.empty}>
          {'아직 수록곡이 없습니다. 음원 목록에서 곡을 추가해보세요.'}
        </div>
      ) : (
        <div className={styles.tableWrap}>
          <table className={styles.table}>
            <thead>
              <tr>
                <th className={styles.cellNum}>#</th>
                <th>{'곡명'}</th>
                <th className={styles.cellBpm}>BPM</th>
                <th className={styles.cellKey}>Key</th>
                <th className={styles.cellActions}>{'관리'}</th>
              </tr>
            </thead>
            <tbody>
              {currentDetail.tracks.map((track) => (
                <tr
                  key={track.trackId}
                  className={`${styles.row} ${currentTrack?.id === track.trackId && isPlayerPlaying ? styles.rowPlaying : ''}`}
                >
                  <td className={styles.cellNum}>
                    <span className={styles.num}>{track.trackOrder}</span>
                    <button
                      className={styles.playBtn}
                      onClick={() => handlePlay(track)}
                      aria-label={
                        currentTrack?.id === track.trackId && isPlayerPlaying ? 'Pause' : 'Play'
                      }
                    >
                      {currentTrack?.id === track.trackId && isPlayerPlaying ? '\u23F8' : '\u25B6'}
                    </button>
                  </td>
                  <td className={styles.cellTitle}>
                    <Link to={`/tracks/${track.trackId}`} className={styles.titleLink}>
                      {track.title}
                    </Link>
                  </td>
                  <td className={styles.cellBpm}>{track.bpm}</td>
                  <td className={styles.cellKey}>{track.tonality}</td>
                  <td className={styles.cellActions}>
                    <span className={styles.hoverActions}>
                      <button
                        className={styles.likeBtn}
                        onClick={() => handleToggleLike(track.trackId)}
                        title={likeStore.likedIds.has(track.trackId) ? '좋아요 해제' : '좋아요'}
                      >
                        {likeStore.likedIds.has(track.trackId) ? '\u2665' : '\u2661'}
                      </button>
                      <button
                        className={styles.addPlBtn}
                        onClick={() => {
                          if (isCurrentProjection()) setAddToPlTrackId(track.trackId);
                        }}
                        title="재생목록에 추가"
                      >
                        +
                      </button>
                      <button
                        className={styles.dlBtn}
                        onClick={() => handleDownload(track)}
                        title="다운로드"
                      >
                        {'\u2193'}
                      </button>
                    </span>
                    <button
                      className={styles.removeBtn}
                      onClick={() => {
                        if (isCurrentProjection()) setRemoveTarget(track);
                      }}
                      title="재생목록에서 삭제"
                    >
                      {'삭제'}
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* Add to Playlist Modal */}
      <AddToPlaylistModal
        open={projectionCurrent && addToPlTrackId !== null}
        trackId={projectionCurrent ? addToPlTrackId : null}
        onClose={() => setAddToPlTrackId(null)}
      />

      {/* Remove Track Confirm Modal */}
      <Modal
        open={projectionCurrent && removeTarget !== null}
        onClose={() => setRemoveTarget(null)}
        title="곡 삭제"
      >
        <div className={styles.modalBody}>
          <p>
            {'정말 '}
            <strong>{removeTarget?.title}</strong>
            {'을(를) 재생목록에서 삭제하시겠습니까?'}
          </p>
        </div>
        <div className={styles.modalFooter}>
          <Button variant="ghost" onClick={() => setRemoveTarget(null)}>
            {'취소'}
          </Button>
          <Button variant="danger" onClick={handleRemoveTrack} loading={removing}>
            {'삭제'}
          </Button>
        </div>
      </Modal>
    </div>
  );
}

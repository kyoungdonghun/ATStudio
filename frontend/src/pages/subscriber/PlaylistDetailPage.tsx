import { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import {
  fetchPlaylistDetail,
  removeTrackFromPlaylist,
  type PlaylistDetail,
  type PlaylistTrack,
} from '@/api/playlists';
import { downloadTrack, triggerBlobDownload } from '@/api/downloads';
import { addToDownloadQueue } from '@/api/downloadQueue';
import { usePlayerStore } from '@/store/playerStore';
import { useLikeStore } from '@/store/likeStore';
import { useAuthStore } from '@/store/authStore';
import { useToastStore } from '@/store/toastStore';
import AddToPlaylistModal from '@/components/playlist/AddToPlaylistModal';
import Button from '@/components/ui/Button';
import Modal from '@/components/ui/Modal';
import styles from './PlaylistDetailPage.module.css';

export default function PlaylistDetailPage() {
  const { playlistId } = useParams<{ playlistId: string }>();
  const navigate = useNavigate();
  const id = Number(playlistId);

  /* ── Stores ── */
  const currentTrack = usePlayerStore((s) => s.currentTrack);
  const isPlayerPlaying = usePlayerStore((s) => s.isPlaying);
  const playTrack = usePlayerStore((s) => s.play);
  const pauseTrack = usePlayerStore((s) => s.pause);
  const resumeTrack = usePlayerStore((s) => s.resume);
  const likeStore = useLikeStore();
  const user = useAuthStore((s) => s.user);
  const toast = useToastStore((s) => s.show);

  /* ── State ── */
  const [detail, setDetail] = useState<PlaylistDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  /* ── Add to playlist modal ── */
  const [addToPlTrackId, setAddToPlTrackId] = useState<number | null>(null);

  /* ── Remove track confirm ── */
  const [removeTarget, setRemoveTarget] = useState<PlaylistTrack | null>(null);
  const [removing, setRemoving] = useState(false);

  /* ── Fetch ── */
  const load = useCallback(async () => {
    if (!id || isNaN(id)) return;
    try {
      setLoading(true);
      setError(null);
      const data = await fetchPlaylistDetail(id);
      setDetail(data);
    } catch (err) {
      setError(
        err instanceof Error ? err.message : '재생목록을 불러오지 못했습니다.',
      );
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    load();
  }, [load]);

  /* ── Like toggle (fetch on mount) ── */
  useEffect(() => {
    if (user) likeStore.load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [user]);

  /* ── Track action handlers ── */
  function handlePlay(track: PlaylistTrack) {
    if (currentTrack?.id === track.trackId) {
      if (isPlayerPlaying) pauseTrack();
      else resumeTrack();
      return;
    }
    playTrack({
      id: track.trackId,
      title: track.title,
      artistName: '',
      duration: 0,
      bpm: track.bpm,
      tonality: track.tonality,
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

  function handleToggleLike(trackId: number) {
    likeStore.toggle(trackId);
  }

  async function handleDownload(track: PlaylistTrack) {
    try {
      const blob = await downloadTrack(track.trackId);
      triggerBlobDownload(blob, `${track.title}.mp3`);
    } catch {
      toast('error', '다운로드에 실패했습니다.');
    }
  }

  async function handleAddToQueue(track: PlaylistTrack) {
    try {
      await addToDownloadQueue(track.trackId);
      toast('success', '다운로드 대기열에 추가되었습니다.');
    } catch (err: unknown) {
      const axErr = err as { response?: { status?: number } };
      if (axErr.response?.status === 409) {
        toast('error', '이미 대기열에 있는 음원입니다.');
      } else {
        toast('error', '대기열 추가에 실패했습니다.');
      }
    }
  }

  /* ── Remove track handler ── */
  async function handleRemoveTrack() {
    if (!removeTarget) return;
    try {
      setRemoving(true);
      await removeTrackFromPlaylist(id, removeTarget.trackId);
      setRemoveTarget(null);
      await load();
    } catch (err) {
      setError(
        err instanceof Error ? err.message : '곡 삭제에 실패했습니다.',
      );
    } finally {
      setRemoving(false);
    }
  }

  /* ── Render ── */

  if (loading) {
    return (
      <div className={styles.page}>
        <div className={styles.loading}>{'재생목록을 불러오는 중...'}</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className={styles.page}>
        <div className={styles.error}>{error}</div>
      </div>
    );
  }

  if (!detail) {
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
          <h1 className={styles.pageTitle}>{detail.title}</h1>
          <span className={styles.trackCount}>
            {detail.tracks.length}곡
          </span>
        </div>
        <div className={styles.headerActions}>
          <Button
            variant="ghost"
            size="sm"
            onClick={() => navigate(`/playlists/${id}/edit`)}
          >
            {'편집'}
          </Button>
        </div>
      </div>

      {/* Track Table */}
      {detail.tracks.length === 0 ? (
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
            {detail.tracks.map((track) => (
              <tr
                key={track.trackId}
                className={`${styles.row} ${currentTrack?.id === track.trackId && isPlayerPlaying ? styles.rowPlaying : ''}`}
              >
                <td className={styles.cellNum}>
                  <span className={styles.num}>{track.trackOrder}</span>
                  <button
                    className={styles.playBtn}
                    onClick={() => handlePlay(track)}
                    aria-label={currentTrack?.id === track.trackId && isPlayerPlaying ? 'Pause' : 'Play'}
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
                      onClick={() => setAddToPlTrackId(track.trackId)}
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
                    <button
                      className={styles.queueBtn}
                      onClick={() => handleAddToQueue(track)}
                      title="대기열에 추가"
                    >
                      {'대기열'}
                    </button>
                  </span>
                  <button
                    className={styles.removeBtn}
                    onClick={() => setRemoveTarget(track)}
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
        open={addToPlTrackId !== null}
        trackId={addToPlTrackId}
        onClose={() => setAddToPlTrackId(null)}
      />

      {/* Remove Track Confirm Modal */}
      <Modal
        open={removeTarget !== null}
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
          <Button
            variant="danger"
            onClick={handleRemoveTrack}
            loading={removing}
          >
            {'삭제'}
          </Button>
        </div>
      </Modal>
    </div>
  );
}

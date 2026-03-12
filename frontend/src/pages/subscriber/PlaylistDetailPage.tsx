import { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import {
  fetchPlaylistDetail,
  removeTrackFromPlaylist,
  type PlaylistDetail,
  type PlaylistTrack,
} from '@/api/playlists';
import Button from '@/components/ui/Button';
import Modal from '@/components/ui/Modal';
import styles from './PlaylistDetailPage.module.css';

export default function PlaylistDetailPage() {
  const { playlistId } = useParams<{ playlistId: string }>();
  const navigate = useNavigate();
  const id = Number(playlistId);

  /* ── State ── */
  const [detail, setDetail] = useState<PlaylistDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

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
              <tr key={track.trackId}>
                <td className={styles.cellNum}>{track.trackOrder}</td>
                <td className={styles.cellTitle}>{track.title}</td>
                <td className={styles.cellBpm}>{track.bpm}</td>
                <td className={styles.cellKey}>{track.tonality}</td>
                <td className={styles.cellActions}>
                  <button
                    className={styles.removeBtn}
                    onClick={() => setRemoveTarget(track)}
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

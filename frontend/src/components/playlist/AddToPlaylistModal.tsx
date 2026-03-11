import { useEffect, useState } from 'react';
import { fetchMyPlaylists, addTrackToPlaylist } from '@/api/playlists';
import type { Playlist } from '@/types';
import Modal from '@/components/ui/Modal';
import styles from './AddToPlaylistModal.module.css';

interface AddToPlaylistModalProps {
  open: boolean;
  trackId: number | null;
  onClose: () => void;
}

export default function AddToPlaylistModal({
  open,
  trackId,
  onClose,
}: AddToPlaylistModalProps) {
  const [playlists, setPlaylists] = useState<Playlist[]>([]);
  const [loading, setLoading] = useState(false);
  const [selectedId, setSelectedId] = useState<number | null>(null);
  const [adding, setAdding] = useState(false);
  const [result, setResult] = useState<'success' | 'error' | null>(null);

  useEffect(() => {
    if (!open) {
      setSelectedId(null);
      setResult(null);
      return;
    }

    setLoading(true);
    fetchMyPlaylists()
      .then((res) => setPlaylists(res.dataList ?? []))
      .catch(() => setPlaylists([]))
      .finally(() => setLoading(false));
  }, [open]);

  async function handleAdd() {
    if (!selectedId || !trackId) return;
    setAdding(true);
    try {
      await addTrackToPlaylist(selectedId, trackId);
      setResult('success');
      setTimeout(() => onClose(), 800);
    } catch {
      setResult('error');
    }
    setAdding(false);
  }

  return (
    <Modal open={open} onClose={onClose} title="재생목록에 추가">
      {loading ? (
        <div className={styles.empty}>Loading...</div>
      ) : playlists.length === 0 ? (
        <div className={styles.empty}>
          재생목록이 없습니다.
          <br />
          재생목록을 먼저 만들어주세요.
        </div>
      ) : result === 'success' ? (
        <div className={styles.success}>추가되었습니다!</div>
      ) : (
        <>
          <ul className={styles.list}>
            {playlists.map((pl) => (
              <li key={pl.id}>
                <button
                  className={`${styles.plBtn} ${selectedId === pl.id ? styles.plBtnSelected : ''}`}
                  onClick={() => setSelectedId(pl.id)}
                >
                  <span className={styles.plIcon}>{'\u266A'}</span>
                  <span className={styles.plName}>{pl.title}</span>
                  <span className={styles.plCount}>{pl.trackCount}곡</span>
                </button>
              </li>
            ))}
          </ul>
          {result === 'error' && (
            <div className={styles.errorMsg}>추가에 실패했습니다.</div>
          )}
          <div className={styles.footer}>
            <button className={styles.cancelBtn} onClick={onClose}>
              취소
            </button>
            <button
              className={styles.addBtn}
              onClick={handleAdd}
              disabled={!selectedId || adding}
            >
              {adding ? '...' : '추가'}
            </button>
          </div>
        </>
      )}
    </Modal>
  );
}

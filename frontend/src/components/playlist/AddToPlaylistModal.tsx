import { useEffect, useState } from 'react';
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

export default function AddToPlaylistModal({
  open,
  trackId,
  onClose,
  onSubscriptionRequired,
}: AddToPlaylistModalProps) {
  const [playlists, setPlaylists] = useState<Playlist[]>([]);
  const [ready, setReady] = useState(false);
  const [selectedId, setSelectedId] = useState<number | null>(null);
  const [adding, setAdding] = useState(false);
  const [result, setResult] = useState<'success' | 'error' | 'duplicate' | null>(null);

  useEffect(() => {
    if (!open) {
      setReady(false);
      setSelectedId(null);
      setResult(null);
      return;
    }

    fetchMyPlaylists()
      .then((res) => {
        setPlaylists(res.dataList ?? []);
        setReady(true);
      })
      .catch((err) => {
        if (isSubscriptionRequired(err)) {
          onClose();
          onSubscriptionRequired?.();
          return;
        }
        setPlaylists([]);
        setReady(true);
      });
  }, [open]);

  async function handleAdd() {
    if (!selectedId || !trackId) return;
    setAdding(true);
    try {
      await addTrackToPlaylist(selectedId, trackId);
      setResult('success');
      setTimeout(() => onClose(), 800);
    } catch (err) {
      if (isSubscriptionRequired(err)) {
        onClose();
        onSubscriptionRequired?.();
        return;
      }
      const axErr = err as import('axios').AxiosError<{ errorCode?: string }>;
      if (axErr.response?.status === 409) {
        setResult('duplicate');
        return;
      }
      setResult('error');
    } finally {
      setAdding(false);
    }
  }

  // Don't render anything until subscription check passes
  if (!open || !ready) return null;

  return (
    <Modal open={open} onClose={onClose} title="재생목록에 추가">
      {playlists.length === 0 ? (
        <div className={styles.empty}>
          재생목록이 없습니다.
          <br />
          재생목록을 먼저 만들어주세요.
        </div>
      ) : result === 'success' ? (
        <div className={styles.success}>추가되었습니다!</div>
      ) : result === 'duplicate' ? (
        <div className={styles.errorMsg}>이미 재생목록에 추가된 트랙입니다.</div>
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

import { useEffect, useState, useCallback } from 'react';
import { usePlayerStore } from '@/store/playerStore';
import { useAuthStore } from '@/store/authStore';
import { fetchPlayHistory } from '@/api/playHistory';
import { toUploadUrl } from '@/api/client';
import Modal from '@/components/ui/Modal';
import type { PlayHistory } from '@/types';
import styles from './HistoryModal.module.css';

interface HistoryModalProps {
  open: boolean;
  onClose: () => void;
}

export default function HistoryModal({ open, onClose }: HistoryModalProps) {
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated());
  const play = usePlayerStore((s) => s.play);
  const currentTrack = usePlayerStore((s) => s.currentTrack);

  const [history, setHistory] = useState<PlayHistory[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(false);

  const loadHistory = useCallback(async () => {
    if (!isAuthenticated) return;
    setLoading(true);
    setError(false);
    try {
      const res = await fetchPlayHistory({ page: 1, size: 50 });
      setHistory(res.dataList ?? []);
    } catch {
      setError(true);
    }
    setLoading(false);
  }, [isAuthenticated]);

  useEffect(() => {
    if (open) loadHistory();
  }, [open, loadHistory]);

  function handlePlay(h: PlayHistory) {
    play({
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
      likeCount: 0,
      downloadCount: 0,
      createdAt: '',
      updatedAt: '',
    });
  }

  return (
    <Modal open={open} onClose={onClose} title="재생기록">
      {!isAuthenticated ? (
        <div className={styles.empty}>로그인이 필요합니다.</div>
      ) : loading ? (
        <div className={styles.empty}>불러오는 중...</div>
      ) : error ? (
        <div className={styles.empty}>재생기록을 불러오지 못했습니다.</div>
      ) : history.length === 0 ? (
        <div className={styles.empty}>재생기록이 없습니다.</div>
      ) : (
        <ul className={styles.list}>
          {history.map((h) => {
            const thumbUrl = toUploadUrl(h.track.thumbnail);
            const isActive = currentTrack?.id === h.track.id;
            return (
              <li
                key={h.id}
                className={`${styles.item} ${isActive ? styles.itemActive : ''}`}
              >
                <div className={styles.thumb}>
                  {thumbUrl ? (
                    <img src={thumbUrl} alt={h.track.title} />
                  ) : (
                    '\u266B'
                  )}
                </div>
                <div className={styles.info}>
                  <div className={styles.title}>{h.track.title}</div>
                  <div className={styles.time}>
                    {new Date(h.playedAt).toLocaleString('ko-KR', {
                      month: 'short',
                      day: 'numeric',
                      hour: '2-digit',
                      minute: '2-digit',
                    })}
                  </div>
                </div>
                <button
                  className={styles.playBtn}
                  onClick={() => handlePlay(h)}
                  aria-label="Play"
                  title="재생"
                >
                  {'\u25B6'}
                </button>
              </li>
            );
          })}
        </ul>
      )}
    </Modal>
  );
}

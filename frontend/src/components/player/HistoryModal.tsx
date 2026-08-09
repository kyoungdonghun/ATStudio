import { useEffect, useState } from 'react';
import {
  usePlayerStore,
  loadPlayHistory,
  hydratePlayHistory,
  removePlayHistoryEntry,
  clearPlayHistory,
  type LocalPlayEntry,
} from '@/store/playerStore';
import { toUploadUrl } from '@/api/client';
import Modal from '@/components/ui/Modal';
import styles from './HistoryModal.module.css';

interface HistoryModalProps {
  open: boolean;
  onClose: () => void;
}

export default function HistoryModal({ open, onClose }: HistoryModalProps) {
  const play = usePlayerStore((s) => s.play);
  const currentTrack = usePlayerStore((s) => s.currentTrack);

  const [history, setHistory] = useState<LocalPlayEntry[]>([]);

  useEffect(() => {
    if (!open) return;
    let active = true;
    setHistory(loadPlayHistory());
    void hydratePlayHistory().then((entries) => {
      if (active) setHistory(entries);
    });
    return () => {
      active = false;
    };
  }, [open]);

  function handlePlay(entry: LocalPlayEntry) {
    play(entry.track);
  }

  function handleDelete(trackId: number) {
    removePlayHistoryEntry(trackId);
    setHistory((prev) => prev.filter((entry) => entry.track.id !== trackId));
  }

  function handleClearAll() {
    clearPlayHistory();
    setHistory([]);
  }

  return (
    <Modal open={open} onClose={onClose} title="재생기록">
      {history.length === 0 ? (
        <div className={styles.empty}>재생기록이 없습니다.</div>
      ) : (
        <>
          <div className={styles.header}>
            <button className={styles.clearBtn} onClick={handleClearAll}>
              전체 삭제
            </button>
          </div>
          <ul className={styles.list}>
            {history.map((entry) => {
              const thumbUrl = toUploadUrl(entry.track.thumbnail);
              const isActive = currentTrack?.id === entry.track.id;
              return (
                <li
                  key={`${entry.track.id}-${entry.playedAt}`}
                  className={`${styles.item} ${isActive ? styles.itemActive : ''}`}
                >
                  <div className={styles.thumb}>
                    {thumbUrl ? <img src={thumbUrl} alt={entry.track.title} /> : '\u266B'}
                  </div>
                  <div className={styles.info}>
                    <div className={styles.title}>{entry.track.title}</div>
                    <div className={styles.time}>
                      {new Date(entry.playedAt).toLocaleString('ko-KR', {
                        month: 'short',
                        day: 'numeric',
                        hour: '2-digit',
                        minute: '2-digit',
                      })}
                    </div>
                  </div>
                  <button
                    className={styles.playBtn}
                    onClick={() => handlePlay(entry)}
                    aria-label="Play"
                    title="재생"
                  >
                    {'\u25B6'}
                  </button>
                  <button
                    className={styles.deleteBtn}
                    onClick={() => handleDelete(entry.track.id)}
                    aria-label="삭제"
                    title="삭제"
                  >
                    &times;
                  </button>
                </li>
              );
            })}
          </ul>
        </>
      )}
    </Modal>
  );
}

import { usePlayerStore } from '@/store/playerStore';
import Modal from '@/components/ui/Modal';
import styles from './QueueModal.module.css';

interface QueueModalProps {
  open: boolean;
  onClose: () => void;
}

export default function QueueModal({ open, onClose }: QueueModalProps) {
  const queue = usePlayerStore((s) => s.queue);
  const currentTrack = usePlayerStore((s) => s.currentTrack);
  const play = usePlayerStore((s) => s.play);
  const removeFromQueue = usePlayerStore((s) => s.removeFromQueue);

  return (
    <Modal open={open} onClose={onClose} title="대기열">
      {queue.length === 0 ? (
        <div className={styles.empty}>대기열이 비어 있습니다.</div>
      ) : (
        <ul className={styles.list}>
          {queue.map((track, idx) => (
            <li
              key={track.id}
              className={`${styles.item} ${currentTrack?.id === track.id ? styles.itemActive : ''}`}
            >
              <button
                className={styles.playBtn}
                onClick={() => play(track)}
                aria-label="Play"
              >
                {currentTrack?.id === track.id ? '\u266B' : idx + 1}
              </button>
              <div className={styles.info}>
                <div className={styles.title}>{track.title}</div>
                <div className={styles.artist}>{track.artistName}</div>
              </div>
              <button
                className={styles.removeBtn}
                onClick={() => removeFromQueue(track.id)}
                aria-label="Remove"
              >
                &times;
              </button>
            </li>
          ))}
        </ul>
      )}
    </Modal>
  );
}

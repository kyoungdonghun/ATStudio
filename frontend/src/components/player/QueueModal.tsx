import { useRef } from 'react';
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
  const reorderQueue = usePlayerStore((s) => s.reorderQueue);

  const dragIdx = useRef<number | null>(null);
  const dragOverIdx = useRef<number | null>(null);

  function handleDragStart(idx: number) {
    dragIdx.current = idx;
  }

  function handleDragOver(e: React.DragEvent, idx: number) {
    e.preventDefault();
    dragOverIdx.current = idx;
  }

  function handleDrop() {
    if (dragIdx.current !== null && dragOverIdx.current !== null && dragIdx.current !== dragOverIdx.current) {
      reorderQueue(dragIdx.current, dragOverIdx.current);
    }
    dragIdx.current = null;
    dragOverIdx.current = null;
  }

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
              draggable
              onDragStart={() => handleDragStart(idx)}
              onDragOver={(e) => handleDragOver(e, idx)}
              onDrop={handleDrop}
            >
              <span className={styles.dragHandle} title="Drag to reorder">
                {'\u2630'}
              </span>
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

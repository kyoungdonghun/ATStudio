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

  /* ── Touch DnD refs ── */
  const touchDragIdx = useRef<number | null>(null);
  const listRef = useRef<HTMLUListElement>(null);

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

  /* ── Touch DnD handlers ── */

  function handleTouchStart(idx: number) {
    touchDragIdx.current = idx;
  }

  function handleTouchMove(e: React.TouchEvent) {
    if (touchDragIdx.current === null) return;
    e.preventDefault();
  }

  function handleTouchEnd(e: React.TouchEvent) {
    if (touchDragIdx.current === null || !listRef.current) return;
    const touch = e.changedTouches[0];
    const items = listRef.current.children;
    let targetIdx = touchDragIdx.current;

    for (let i = 0; i < items.length; i++) {
      const rect = items[i].getBoundingClientRect();
      if (touch.clientY >= rect.top && touch.clientY <= rect.bottom) {
        targetIdx = i;
        break;
      }
    }

    if (targetIdx !== touchDragIdx.current) {
      reorderQueue(touchDragIdx.current, targetIdx);
    }
    touchDragIdx.current = null;
  }

  return (
    <Modal open={open} onClose={onClose} title="대기열">
      {queue.length === 0 ? (
        <div className={styles.empty}>대기열이 비어 있습니다.</div>
      ) : (
        <ul className={styles.list} ref={listRef} onTouchMove={handleTouchMove} onTouchEnd={handleTouchEnd}>
          {queue.map((track, idx) => (
            <li
              key={track.id}
              className={`${styles.item} ${currentTrack?.id === track.id ? styles.itemActive : ''}`}
              draggable
              onDragStart={() => handleDragStart(idx)}
              onDragOver={(e) => handleDragOver(e, idx)}
              onDrop={handleDrop}
            >
              <span
                className={styles.dragHandle}
                title="Drag to reorder"
                onTouchStart={() => handleTouchStart(idx)}
                style={{ touchAction: 'none' }}
              >
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

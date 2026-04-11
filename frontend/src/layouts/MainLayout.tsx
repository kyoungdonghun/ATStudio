import { useEffect } from 'react';
import { Outlet } from 'react-router-dom';
import Header from '@/layouts/Header';
import PlayerBar from '@/layouts/PlayerBar';
import ToastContainer from '@/components/ui/ToastContainer';
import { usePlayerStore } from '@/store/playerStore';
import styles from './MainLayout.module.css';

export default function MainLayout() {
  /* Global keyboard shortcuts:
     - Space  → play/pause current track
     - ↑↓     → prev/next (SR-87, delegates to trackListContext) */
  useEffect(() => {
    function handleKeyDown(e: KeyboardEvent) {
      const tag = (e.target as HTMLElement)?.tagName;
      const isInput = tag === 'INPUT' || tag === 'TEXTAREA' || tag === 'SELECT';

      // Space → play/pause
      if (e.key === ' ') {
        if (isInput) return;
        const store = usePlayerStore.getState();
        if (!store.currentTrack) return;
        e.preventDefault();
        if (store.isPlaying) store.pause();
        else store.resume();
        return;
      }

      if (e.key !== 'ArrowUp' && e.key !== 'ArrowDown') return;
      if (isInput) return;

      const store = usePlayerStore.getState();
      if (store.trackListContext.length === 0) return;

      // If nothing is playing yet, start at the first track on ↓
      if (!store.currentTrack && e.key === 'ArrowDown') {
        e.preventDefault();
        store.play(store.trackListContext[0]);
        return;
      }

      e.preventDefault();
      if (e.key === 'ArrowDown') store.next();
      else store.prev();
    }

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, []);

  return (
    <div className={styles.layout}>
      <Header />
      <main className={styles.main}>
        <Outlet />
      </main>
      <PlayerBar />
      <ToastContainer />
    </div>
  );
}

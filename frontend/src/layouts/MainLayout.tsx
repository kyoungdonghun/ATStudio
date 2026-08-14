import { useEffect } from 'react';
import { Outlet } from 'react-router-dom';
import Header from '@/layouts/Header';
import PlayerBar from '@/layouts/PlayerBar';
import ToastContainer from '@/components/ui/ToastContainer';
import { usePlayerStore } from '@/store/playerStore';
import styles from './MainLayout.module.css';

const SHORTCUT_BLOCKING_TARGETS = [
  'a[href]',
  'area[href]',
  'audio[controls]',
  'button',
  'details',
  'dialog',
  'input',
  'option',
  'select',
  'summary',
  'textarea',
  'video[controls]',
  '[contenteditable]:not([contenteditable="false"])',
  '[tabindex]:not([tabindex="-1"])',
  '[role="alertdialog"]',
  '[role="application"]',
  '[role="button"]',
  '[role="checkbox"]',
  '[role="combobox"]',
  '[role="dialog"]',
  '[role="grid"]',
  '[role="gridcell"]',
  '[role="link"]',
  '[role="listbox"]',
  '[role="menu"]',
  '[role="menubar"]',
  '[role="menuitem"]',
  '[role="menuitemcheckbox"]',
  '[role="menuitemradio"]',
  '[role="option"]',
  '[role="radio"]',
  '[role="radiogroup"]',
  '[role="scrollbar"]',
  '[role="searchbox"]',
  '[role="slider"]',
  '[role="spinbutton"]',
  '[role="switch"]',
  '[role="tab"]',
  '[role="tablist"]',
  '[role="textbox"]',
  '[role="toolbar"]',
  '[role="tree"]',
  '[role="treegrid"]',
  '[role="treeitem"]',
].join(',');

function shouldIgnorePlaybackShortcut(event: KeyboardEvent): boolean {
  if (event.defaultPrevented || event.altKey || event.ctrlKey || event.metaKey || event.shiftKey) {
    return true;
  }

  return (
    event.target instanceof Element && Boolean(event.target.closest(SHORTCUT_BLOCKING_TARGETS))
  );
}

export default function MainLayout() {
  /* Global keyboard shortcuts:
     - Space  → play/pause current track
     - ↑↓     → prev/next (SR-87, delegates to trackListContext) */
  useEffect(() => {
    function handleKeyDown(e: KeyboardEvent) {
      if (e.key !== ' ' && e.key !== 'ArrowUp' && e.key !== 'ArrowDown') return;
      if (shouldIgnorePlaybackShortcut(e)) return;

      // Space → play/pause
      if (e.key === ' ') {
        const store = usePlayerStore.getState();
        if (!store.currentTrack) return;
        e.preventDefault();
        if (store.isPlaying) store.pause();
        else store.resume();
        return;
      }

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

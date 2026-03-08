import { create } from 'zustand';
import type { Track } from '@/types';

interface PlayerState {
  currentTrack: Track | null;
  isPlaying: boolean;
  queue: Track[];
  play: (track: Track) => void;
  pause: () => void;
  resume: () => void;
  next: () => void;
  prev: () => void;
  addToQueue: (track: Track) => void;
  clearQueue: () => void;
}

export const usePlayerStore = create<PlayerState>((set, get) => ({
  currentTrack: null,
  isPlaying: false,
  queue: [],

  play: (track: Track) => {
    set({ currentTrack: track, isPlaying: true });
  },

  pause: () => {
    set({ isPlaying: false });
  },

  resume: () => {
    set({ isPlaying: true });
  },

  next: () => {
    const { queue, currentTrack } = get();
    if (queue.length === 0) return;
    const currentIndex = currentTrack
      ? queue.findIndex((t) => t.id === currentTrack.id)
      : -1;
    const nextIndex = (currentIndex + 1) % queue.length;
    set({ currentTrack: queue[nextIndex], isPlaying: true });
  },

  prev: () => {
    const { queue, currentTrack } = get();
    if (queue.length === 0) return;
    const currentIndex = currentTrack
      ? queue.findIndex((t) => t.id === currentTrack.id)
      : 0;
    const prevIndex = (currentIndex - 1 + queue.length) % queue.length;
    set({ currentTrack: queue[prevIndex], isPlaying: true });
  },

  addToQueue: (track: Track) => {
    set((state) => ({
      queue: state.queue.some((t) => t.id === track.id)
        ? state.queue
        : [...state.queue, track],
    }));
  },

  clearQueue: () => {
    set({ queue: [], currentTrack: null, isPlaying: false });
  },
}));

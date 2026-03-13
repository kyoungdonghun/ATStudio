import { create } from 'zustand';
import type { Track } from '@/types';
import { recordPlay } from '@/api/playHistory';

const audio = new Audio();
const STREAM_BASE = '/api/tracks';

type RepeatMode = 'off' | 'all' | 'one';

interface PlayerState {
  currentTrack: Track | null;
  isPlaying: boolean;
  currentTime: number;
  duration: number;
  volume: number;
  muted: boolean;
  queue: Track[];
  shuffle: boolean;
  repeat: RepeatMode;
  play: (track: Track) => void;
  pause: () => void;
  resume: () => void;
  next: () => void;
  prev: () => void;
  seek: (time: number) => void;
  setVolume: (volume: number) => void;
  toggleMute: () => void;
  toggleShuffle: () => void;
  cycleRepeat: () => void;
  playAll: (tracks: Track[]) => void;
  addToQueue: (track: Track) => void;
  removeFromQueue: (trackId: number) => void;
  clearQueue: () => void;
}

export const usePlayerStore = create<PlayerState>((set, get) => {
  // Audio event listeners
  audio.addEventListener('timeupdate', () => {
    set({ currentTime: audio.currentTime, duration: audio.duration || 0 });
  });

  audio.addEventListener('ended', () => {
    const { repeat } = get();
    if (repeat === 'one') {
      audio.currentTime = 0;
      audio.play().catch(() => {});
      return;
    }
    get().next();
  });

  audio.addEventListener('loadedmetadata', () => {
    set({ duration: audio.duration || 0 });
  });

  // Initialize volume from localStorage
  const savedVolume = parseFloat(localStorage.getItem('playerVolume') ?? '1');
  audio.volume = isNaN(savedVolume) ? 1 : savedVolume;

  return {
    currentTrack: null,
    isPlaying: false,
    currentTime: 0,
    duration: 0,
    volume: audio.volume,
    muted: false,
    queue: [],
    shuffle: false,
    repeat: 'off' as RepeatMode,

    play: (track: Track) => {
      audio.src = `${STREAM_BASE}/${track.id}/stream`;
      audio.play().catch(() => {});
      set({ currentTrack: track, isPlaying: true, currentTime: 0 });

      // Record play history (fire-and-forget, only if logged in)
      if (localStorage.getItem('accessToken')) {
        recordPlay(track.id).catch(() => {});
      }
    },

    pause: () => {
      audio.pause();
      set({ isPlaying: false });
    },

    resume: () => {
      audio.play().catch(() => {});
      set({ isPlaying: true });
    },

    next: () => {
      const { queue, currentTrack, play, shuffle, repeat } = get();
      if (queue.length === 0) {
        audio.pause();
        set({ isPlaying: false });
        return;
      }
      const currentIndex = currentTrack
        ? queue.findIndex((t) => t.id === currentTrack.id)
        : -1;

      if (shuffle) {
        const candidates = queue.filter((_, i) => i !== currentIndex);
        if (candidates.length === 0) {
          play(queue[0]);
        } else {
          play(candidates[Math.floor(Math.random() * candidates.length)]);
        }
        return;
      }

      const nextIndex = currentIndex + 1;
      if (nextIndex >= queue.length) {
        if (repeat === 'all') {
          play(queue[0]);
        } else {
          audio.pause();
          set({ isPlaying: false });
        }
        return;
      }
      play(queue[nextIndex]);
    },

    prev: () => {
      // If more than 3 seconds in, restart current track
      if (audio.currentTime > 3) {
        audio.currentTime = 0;
        return;
      }
      const { queue, currentTrack, play } = get();
      if (queue.length === 0) return;
      const currentIndex = currentTrack
        ? queue.findIndex((t) => t.id === currentTrack.id)
        : 0;
      const prevIndex = (currentIndex - 1 + queue.length) % queue.length;
      play(queue[prevIndex]);
    },

    seek: (time: number) => {
      audio.currentTime = time;
      set({ currentTime: time });
    },

    setVolume: (volume: number) => {
      const v = Math.max(0, Math.min(1, volume));
      audio.volume = v;
      audio.muted = false;
      localStorage.setItem('playerVolume', String(v));
      set({ volume: v, muted: false });
    },

    toggleMute: () => {
      const { muted, volume } = get();
      audio.muted = !muted;
      set({ muted: !muted });
      if (!muted && volume === 0) {
        // Unmuting with 0 volume → set to default
        audio.volume = 0.5;
        localStorage.setItem('playerVolume', '0.5');
        set({ volume: 0.5 });
      }
    },

    playAll: (tracks: Track[]) => {
      if (tracks.length === 0) return;
      const { play } = get();
      set({ queue: tracks });
      play(tracks[0]);
    },

    addToQueue: (track: Track) => {
      set((state) => ({
        queue: state.queue.some((t) => t.id === track.id)
          ? state.queue
          : [...state.queue, track],
      }));
    },

    toggleShuffle: () => {
      set((state) => ({ shuffle: !state.shuffle }));
    },

    cycleRepeat: () => {
      set((state) => {
        const modes: RepeatMode[] = ['off', 'all', 'one'];
        const idx = modes.indexOf(state.repeat);
        return { repeat: modes[(idx + 1) % modes.length] };
      });
    },

    removeFromQueue: (trackId: number) => {
      set((state) => ({
        queue: state.queue.filter((t) => t.id !== trackId),
      }));
    },

    clearQueue: () => {
      audio.pause();
      audio.src = '';
      set({ queue: [], currentTrack: null, isPlaying: false, currentTime: 0, duration: 0 });
    },
  };
});

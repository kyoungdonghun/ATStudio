import { create } from 'zustand';
import type { Track } from '@/types';
import { safeStorage } from '@/utils/safeStorage';

const audio = new Audio();
const STREAM_BASE = '/api/tracks';
let isSeeking = false;
let playbackAttempt = 0;

const PLAYBACK_ERROR = '재생을 시작할 수 없습니다. 잠시 후 다시 시도해 주세요.';
const MEDIA_ERROR = '오디오를 재생하는 중 오류가 발생했습니다.';

type RepeatMode = 'off' | 'all' | 'one';

/* ── localStorage persistence helpers ── */

interface PersistedPlayerState {
  version: 1;
  currentTrack: Track | null;
  queue: Track[];
  queueIndex: number;
  currentTime: number;
}

function persistState(state: { currentTrack: Track | null; queue: Track[]; currentTime: number }) {
  try {
    const idx = state.currentTrack
      ? state.queue.findIndex((t) => t.id === state.currentTrack!.id)
      : 0;
    const saved: PersistedPlayerState = {
      version: 1,
      currentTrack: state.currentTrack,
      queue: state.queue,
      queueIndex: Math.max(0, idx),
      currentTime: state.currentTime,
    };
    safeStorage.setItem('playerState', JSON.stringify(saved));
  } catch {
    /* quota exceeded — silently ignore */
  }
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null && !Array.isArray(value);
}

function isNullableString(value: unknown): value is string | null {
  return value === null || typeof value === 'string';
}

function isTrack(value: unknown): value is Track {
  if (!isRecord(value)) return false;
  const tags = value.tags;
  return (
    Number.isSafeInteger(value.id) &&
    Number(value.id) > 0 &&
    typeof value.title === 'string' &&
    typeof value.artistName === 'string' &&
    typeof value.duration === 'number' &&
    Number.isFinite(value.duration) &&
    typeof value.bpm === 'number' &&
    Number.isFinite(value.bpm) &&
    typeof value.tonality === 'string' &&
    isNullableString(value.description) &&
    isNullableString(value.audioFile) &&
    isNullableString(value.thumbnail) &&
    (value.waveformData === undefined || isNullableString(value.waveformData)) &&
    Array.isArray(tags) &&
    tags.every(
      (tag) =>
        isRecord(tag) &&
        Number.isSafeInteger(tag.id) &&
        typeof tag.name === 'string' &&
        ['GENRE', 'MOOD', 'INSTRUMENT', 'USAGE'].includes(String(tag.type)),
    ) &&
    typeof value.isActive === 'boolean' &&
    [value.playCount, value.likeCount, value.downloadCount].every(
      (count) => typeof count === 'number' && Number.isFinite(count),
    ) &&
    typeof value.createdAt === 'string' &&
    typeof value.updatedAt === 'string'
  );
}

function normalizePersistedState(value: unknown): PersistedPlayerState | null {
  if (!isRecord(value) || value.version !== 1) return null;

  const currentTrack = isTrack(value.currentTrack) ? value.currentTrack : null;
  const queue = Array.isArray(value.queue) ? value.queue.filter(isTrack) : [];
  if (currentTrack && !queue.some((track) => track.id === currentTrack.id)) {
    queue.push(currentTrack);
  }
  const currentTime =
    currentTrack &&
    typeof value.currentTime === 'number' &&
    Number.isFinite(value.currentTime) &&
    value.currentTime >= 0
      ? value.currentTime
      : 0;
  const queueIndex = currentTrack ? queue.findIndex((track) => track.id === currentTrack.id) : 0;

  return { version: 1, currentTrack, queue, queueIndex: Math.max(0, queueIndex), currentTime };
}

function loadPersistedState(): PersistedPlayerState | null {
  try {
    const raw = safeStorage.getItem('playerState');
    return raw ? normalizePersistedState(JSON.parse(raw)) : null;
  } catch {
    return null;
  }
}

const savedState = loadPersistedState();

if (savedState?.currentTrack) {
  audio.src = `${STREAM_BASE}/${savedState.currentTrack.id}/stream`;
  try {
    audio.currentTime = savedState.currentTime;
  } catch {
    audio.addEventListener(
      'loadedmetadata',
      () => {
        audio.currentTime = savedState.currentTime;
      },
      { once: true },
    );
  }
}

/* ── Play history (localStorage, SR-89) ── */

export interface LocalPlayEntry {
  trackId: number;
  title: string;
  thumbnail: string | null;
  playedAt: string;
}

function isLocalPlayEntry(value: unknown): value is LocalPlayEntry {
  return (
    isRecord(value) &&
    Number.isSafeInteger(value.trackId) &&
    Number(value.trackId) > 0 &&
    typeof value.title === 'string' &&
    isNullableString(value.thumbnail) &&
    typeof value.playedAt === 'string' &&
    Number.isFinite(Date.parse(value.playedAt))
  );
}

function parsePlayHistory(raw: string | null): LocalPlayEntry[] {
  if (!raw) return [];
  const parsed: unknown = JSON.parse(raw);
  if (!Array.isArray(parsed)) return [];
  return parsed.filter(isLocalPlayEntry).slice(0, HISTORY_MAX);
}

const HISTORY_KEY = 'playHistory';
const HISTORY_MAX = 100;

export function savePlayHistory(track: Track): void {
  try {
    const raw = safeStorage.getItem(HISTORY_KEY);
    const list = parsePlayHistory(raw);
    const entry: LocalPlayEntry = {
      trackId: track.id,
      title: track.title,
      thumbnail: track.thumbnail ?? null,
      playedAt: new Date().toISOString(),
    };
    // Upsert: remove previous entry for same track, prepend latest
    const updated = [entry, ...list.filter((e) => e.trackId !== track.id)].slice(0, HISTORY_MAX);
    safeStorage.setItem(HISTORY_KEY, JSON.stringify(updated));
  } catch {
    /* quota exceeded — ignore */
  }
}

export function loadPlayHistory(): LocalPlayEntry[] {
  try {
    const raw = safeStorage.getItem(HISTORY_KEY);
    return parsePlayHistory(raw);
  } catch {
    return [];
  }
}

export function removePlayHistoryEntry(trackId: number): void {
  try {
    const list = loadPlayHistory().filter((e) => e.trackId !== trackId);
    safeStorage.setItem(HISTORY_KEY, JSON.stringify(list));
  } catch {
    /* ignore */
  }
}

export function clearPlayHistory(): void {
  try {
    safeStorage.removeItem(HISTORY_KEY);
  } catch {
    /* ignore */
  }
}

/* ── Store interface ── */

interface PlayerState {
  currentTrack: Track | null;
  isPlaying: boolean;
  isStalled: boolean;
  playbackError: string | null;
  currentTime: number;
  duration: number;
  volume: number;
  muted: boolean;
  queue: Track[];
  shuffle: boolean;
  repeat: RepeatMode;
  /**
   * SR-83: The currently visible track list on the active page.
   * When set, next()/prev() navigates through this list instead of the queue.
   * Falls back to queue-based navigation (shuffle/repeat) when empty.
   */
  trackListContext: Track[];
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
  reorderQueue: (fromIndex: number, toIndex: number) => void;
  clearQueue: () => void;
  setTrackListContext: (tracks: Track[]) => void;
}

export const usePlayerStore = create<PlayerState>((set, get) => {
  const stopWithError = (message: string) => {
    if (!get().currentTrack) return;
    playbackAttempt += 1;
    audio.pause();
    set({ isPlaying: false, isStalled: false, playbackError: message });
  };

  const startPlayback = (onSuccess?: () => void) => {
    const attempt = ++playbackAttempt;
    set({ isPlaying: false, isStalled: false, playbackError: null });

    let playPromise: Promise<void>;
    try {
      playPromise = audio.play();
    } catch {
      if (attempt === playbackAttempt) {
        set({ isPlaying: false, isStalled: false, playbackError: PLAYBACK_ERROR });
      }
      return;
    }

    void playPromise.then(
      () => {
        if (attempt !== playbackAttempt) return;
        set({ isPlaying: true, isStalled: false, playbackError: null });
        onSuccess?.();
      },
      () => {
        if (attempt !== playbackAttempt) return;
        set({ isPlaying: false, isStalled: false, playbackError: PLAYBACK_ERROR });
      },
    );
  };

  // Audio event listeners
  audio.addEventListener('timeupdate', () => {
    set({ currentTime: audio.currentTime, duration: audio.duration || 0, isStalled: false });
  });

  audio.addEventListener('ended', () => {
    if (isSeeking) return;
    const { repeat } = get();
    if (repeat === 'one') {
      audio.currentTime = 0;
      get().resume();
      return;
    }
    get().next();
  });

  audio.addEventListener('loadedmetadata', () => {
    set({ duration: audio.duration || 0 });
  });

  const markStalled = () => {
    if (get().currentTrack) {
      set({ isStalled: true });
    }
  };

  const clearStalled = () => {
    if (get().isStalled) {
      set({ isStalled: false });
    }
  };

  audio.addEventListener('stalled', markStalled);
  audio.addEventListener('waiting', markStalled);
  audio.addEventListener('canplay', clearStalled);
  audio.addEventListener('playing', clearStalled);

  audio.addEventListener('error', () => {
    stopWithError(MEDIA_ERROR);
  });

  // Initialize volume from localStorage
  const savedVolume = parseFloat(safeStorage.getItem('playerVolume') ?? '1');
  audio.volume = isNaN(savedVolume) ? 1 : savedVolume;

  return {
    currentTrack: savedState?.currentTrack ?? null,
    isPlaying: false,
    isStalled: false,
    playbackError: null,
    currentTime: savedState?.currentTime ?? 0,
    duration: 0,
    volume: audio.volume,
    muted: false,
    queue: savedState?.queue ?? [],
    shuffle: false,
    repeat: 'off' as RepeatMode,
    trackListContext: [],

    play: (track: Track) => {
      audio.src = `${STREAM_BASE}/${track.id}/stream`;

      // Add to queue if not already present
      const { queue } = get();
      const inQueue = queue.some((t) => t.id === track.id);
      const newQueue = inQueue ? queue : [...queue, track];
      set({
        currentTrack: track,
        isPlaying: false,
        isStalled: false,
        playbackError: null,
        currentTime: 0,
        ...(inQueue ? {} : { queue: newQueue }),
      });
      persistState({ currentTrack: track, queue: newQueue, currentTime: 0 });

      startPlayback(() => {
        // SR-89: Record only playback that actually started.
        savePlayHistory(track);
      });
    },

    pause: () => {
      playbackAttempt += 1;
      audio.pause();
      set({ isPlaying: false, isStalled: false });
    },

    resume: () => {
      startPlayback();
    },

    next: () => {
      // SR-83: Prefer the currently visible track list (like keyboard ↓)
      const ctx = get().trackListContext;
      const curForCtx = get().currentTrack;
      if (ctx.length > 0 && curForCtx) {
        const idx = ctx.findIndex((t) => t.id === curForCtx.id);
        if (idx >= 0) {
          if (idx < ctx.length - 1) {
            get().play(ctx[idx + 1]);
            return;
          }
          // End of list: stop (mirrors TrackListPage keyboard behavior)
          get().pause();
          return;
        }
        // Current track not in visible list → fall through to queue logic
      }

      const { queue, currentTrack, play, shuffle, repeat } = get();
      if (queue.length === 0) {
        get().pause();
        return;
      }
      const currentIndex = currentTrack ? queue.findIndex((t) => t.id === currentTrack.id) : -1;

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
          get().pause();
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

      // SR-83: Prefer the currently visible track list (like keyboard ↑)
      const ctx = get().trackListContext;
      const curForCtx = get().currentTrack;
      if (ctx.length > 0 && curForCtx) {
        const idx = ctx.findIndex((t) => t.id === curForCtx.id);
        if (idx > 0) {
          get().play(ctx[idx - 1]);
          return;
        }
        if (idx === 0) {
          // Already at first: restart current track
          audio.currentTime = 0;
          return;
        }
        // Current track not in visible list → fall through to queue logic
      }

      const { queue, currentTrack, play } = get();
      if (queue.length === 0) return;
      const currentIndex = currentTrack ? queue.findIndex((t) => t.id === currentTrack.id) : 0;
      const prevIndex = (currentIndex - 1 + queue.length) % queue.length;
      play(queue[prevIndex]);
    },

    seek: (time: number) => {
      isSeeking = true;
      audio.currentTime = time;
      set({ currentTime: time });
      persistState({ ...get(), currentTime: time });
      setTimeout(() => {
        isSeeking = false;
      }, 100);
    },

    setVolume: (volume: number) => {
      const v = Math.max(0, Math.min(1, volume));
      audio.volume = v;
      audio.muted = false;
      safeStorage.setItem('playerVolume', String(v));
      set({ volume: v, muted: false });
    },

    toggleMute: () => {
      const { muted, volume } = get();
      audio.muted = !muted;
      set({ muted: !muted });
      if (!muted && volume === 0) {
        // Unmuting with 0 volume → set to default
        audio.volume = 0.5;
        safeStorage.setItem('playerVolume', '0.5');
        set({ volume: 0.5 });
      }
    },

    playAll: (tracks: Track[]) => {
      if (tracks.length === 0) return;
      const { play } = get();
      set({ queue: tracks });
      play(tracks[0]);
      // persistState is called inside play()
    },

    addToQueue: (track: Track) => {
      set((state) => {
        const newQueue = state.queue.some((t) => t.id === track.id)
          ? state.queue
          : [...state.queue, track];
        persistState({ ...state, queue: newQueue });
        return { queue: newQueue };
      });
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
      set((state) => {
        const newQueue = state.queue.filter((t) => t.id !== trackId);
        persistState({ ...state, queue: newQueue });
        return { queue: newQueue };
      });
    },

    reorderQueue: (fromIndex: number, toIndex: number) => {
      set((state) => {
        const next = [...state.queue];
        const [moved] = next.splice(fromIndex, 1);
        next.splice(toIndex, 0, moved);
        persistState({ ...state, queue: next });
        return { queue: next };
      });
    },

    setTrackListContext: (tracks: Track[]) => {
      set({ trackListContext: tracks });
    },

    clearQueue: () => {
      playbackAttempt += 1;
      audio.pause();
      audio.src = '';
      set({
        queue: [],
        currentTrack: null,
        isPlaying: false,
        isStalled: false,
        playbackError: null,
        currentTime: 0,
        duration: 0,
      });
      persistState({ currentTrack: null, queue: [], currentTime: 0 });
    },
  };
});

import { create } from 'zustand';
import { fetchPlayableTracks } from '@/api/tracks';
import type { PlayableTrack } from '@/types';
import { safeStorage } from '@/utils/safeStorage';
import { clampPlaybackTime, getFiniteMediaDuration } from '@/utils/playbackProgress';

const audio = new Audio();
const STREAM_BASE = '/api/tracks';
const BUFFERING_THRESHOLD_MS = 2_000;
const MAX_HYDRATION_IDS = 100;
let isSeeking = false;
let playbackAttempt = 0;
let bufferingGeneration = 0;
let bufferingTimer: ReturnType<typeof setTimeout> | null = null;

const PLAYBACK_ERROR = '재생을 시작할 수 없습니다. 잠시 후 다시 시도해 주세요.';
const MEDIA_ERROR = '오디오를 재생하는 중 오류가 발생했습니다.';

type RepeatMode = 'off' | 'all' | 'one';

/* ── localStorage persistence helpers ── */

interface PersistedPlayerState {
  version: 2;
  currentTrackId: number | null;
  queueTrackIds: number[];
  currentTime: number;
}

interface PendingPlayerState {
  currentTrackId: number | null;
  queueTrackIds: number[];
  currentTime: number;
}

function persistState(state: {
  currentTrack: PlayableTrack | null;
  queue: PlayableTrack[];
  currentTime: number;
  duration?: number;
}) {
  try {
    const queueTrackIds = Array.from(new Set(state.queue.map((track) => track.id)));
    if (state.currentTrack && !queueTrackIds.includes(state.currentTrack.id)) {
      queueTrackIds.unshift(state.currentTrack.id);
    }
    const saved: PersistedPlayerState = {
      version: 2,
      currentTrackId: state.currentTrack?.id ?? null,
      queueTrackIds: queueTrackIds.slice(0, MAX_HYDRATION_IDS),
      currentTime: clampPlaybackTime(
        state.currentTime,
        getFiniteMediaDuration(state.duration ?? 0, state.currentTrack?.duration ?? 0),
      ),
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

function isPositiveInteger(value: unknown): value is number {
  return Number.isSafeInteger(value) && Number(value) > 0;
}

function isPlayableTrack(value: unknown): value is PlayableTrack {
  if (!isRecord(value)) return false;
  return (
    isPositiveInteger(value.id) &&
    typeof value.title === 'string' &&
    typeof value.artistName === 'string' &&
    typeof value.duration === 'number' &&
    Number.isFinite(value.duration) &&
    isNullableString(value.thumbnail) &&
    isNullableString(value.waveformData) &&
    (value.bpm === undefined || (typeof value.bpm === 'number' && Number.isFinite(value.bpm))) &&
    (value.tonality === undefined || typeof value.tonality === 'string') &&
    (value.tags === undefined || Array.isArray(value.tags))
  );
}

function readTrackId(value: unknown): number | null {
  if (isPositiveInteger(value)) return value;
  if (isRecord(value) && isPositiveInteger(value.id)) return value.id;
  return null;
}

function normalizePersistedState(value: unknown): PendingPlayerState | null {
  if (!isRecord(value) || (value.version !== 1 && value.version !== 2)) return null;

  const currentTrackId =
    value.version === 2 ? readTrackId(value.currentTrackId) : readTrackId(value.currentTrack);
  const rawQueue = value.version === 2 ? value.queueTrackIds : value.queue;
  const queueTrackIds = Array.isArray(rawQueue)
    ? Array.from(
        new Set(rawQueue.map(readTrackId).filter((trackId): trackId is number => trackId !== null)),
      ).slice(0, MAX_HYDRATION_IDS)
    : [];
  if (currentTrackId && !queueTrackIds.includes(currentTrackId)) {
    queueTrackIds.unshift(currentTrackId);
    queueTrackIds.splice(MAX_HYDRATION_IDS);
  }
  const currentTime =
    currentTrackId &&
    typeof value.currentTime === 'number' &&
    Number.isFinite(value.currentTime) &&
    value.currentTime >= 0
      ? value.currentTime
      : 0;

  return { currentTrackId, queueTrackIds, currentTime };
}

function loadPersistedState(): PendingPlayerState | null {
  try {
    const raw = safeStorage.getItem('playerState');
    return raw ? normalizePersistedState(JSON.parse(raw)) : null;
  } catch {
    return null;
  }
}

let pendingPlayerState = loadPersistedState();
let playerHydrationPromise: Promise<void> | null = null;
let playerStateGeneration = 0;

function restoreAudioSource(track: PlayableTrack, currentTime: number): void {
  const boundedTime = clampPlaybackTime(currentTime, track.duration);
  audio.src = `${STREAM_BASE}/${track.id}/stream`;
  try {
    audio.currentTime = boundedTime;
  } catch {
    audio.addEventListener(
      'loadedmetadata',
      () => {
        audio.currentTime = boundedTime;
      },
      { once: true },
    );
  }
}

/* ── Play history (localStorage, SR-89) ── */

export interface LocalPlayEntry {
  track: PlayableTrack;
  playedAt: string;
}

interface PendingPlayEntry {
  trackId: number;
  track: PlayableTrack | null;
  playedAt: string;
}

function parsePlayHistory(raw: string | null): PendingPlayEntry[] {
  if (!raw) return [];
  const parsed: unknown = JSON.parse(raw);
  if (!Array.isArray(parsed)) return [];
  return parsed
    .flatMap((value): PendingPlayEntry[] => {
      if (!isRecord(value) || typeof value.playedAt !== 'string') return [];
      if (!Number.isFinite(Date.parse(value.playedAt))) return [];
      if (isPlayableTrack(value.track)) {
        return [{ trackId: value.track.id, track: value.track, playedAt: value.playedAt }];
      }
      const trackId = readTrackId(value.trackId);
      return trackId ? [{ trackId, track: null, playedAt: value.playedAt }] : [];
    })
    .slice(0, HISTORY_MAX);
}

const HISTORY_KEY = 'playHistory';
const HISTORY_MAX = 100;

function loadPendingPlayHistory(): PendingPlayEntry[] {
  try {
    return parsePlayHistory(safeStorage.getItem(HISTORY_KEY));
  } catch {
    return [];
  }
}

function persistPlayHistory(entries: LocalPlayEntry[]): void {
  safeStorage.setItem(HISTORY_KEY, JSON.stringify(entries));
}

function persistPendingPlayHistory(entries: PendingPlayEntry[]): void {
  safeStorage.setItem(
    HISTORY_KEY,
    JSON.stringify(
      entries.map((entry) =>
        entry.track
          ? { track: entry.track, playedAt: entry.playedAt }
          : { trackId: entry.trackId, playedAt: entry.playedAt },
      ),
    ),
  );
}

export function savePlayHistory(track: PlayableTrack): void {
  try {
    const list = loadPendingPlayHistory();
    const entry: PendingPlayEntry = {
      trackId: track.id,
      track,
      playedAt: new Date().toISOString(),
    };
    const updated = [
      entry,
      ...list.filter((historyEntry) => historyEntry.trackId !== track.id),
    ].slice(0, HISTORY_MAX);
    persistPendingPlayHistory(updated);
  } catch {
    /* quota exceeded — ignore */
  }
}

export function loadPlayHistory(): LocalPlayEntry[] {
  return loadPendingPlayHistory().flatMap((entry) =>
    entry.track ? [{ track: entry.track, playedAt: entry.playedAt }] : [],
  );
}

let historyHydrated = false;
let historyHydrationPromise: Promise<LocalPlayEntry[]> | null = null;

export function hydratePlayHistory(): Promise<LocalPlayEntry[]> {
  if (historyHydrated) return Promise.resolve(loadPlayHistory());
  if (historyHydrationPromise) return historyHydrationPromise;

  const pendingEntries = loadPendingPlayHistory();
  const ids = Array.from(new Set(pendingEntries.map((entry) => entry.trackId))).slice(
    0,
    MAX_HYDRATION_IDS,
  );
  if (ids.length === 0) {
    historyHydrated = true;
    return Promise.resolve([]);
  }

  historyHydrationPromise = fetchPlayableTracks(ids)
    .then((tracks) => {
      const tracksById = new Map(tracks.map((track) => [track.id, track]));
      const latestEntries = loadPendingPlayHistory();
      const historyWasCleared =
        latestEntries.length === 0 && safeStorage.getItem(HISTORY_KEY) === null;
      const hydrated = latestEntries.flatMap((entry): LocalPlayEntry[] => {
        const hydratedTrack = entry.track ?? tracksById.get(entry.trackId);
        return hydratedTrack ? [{ track: hydratedTrack, playedAt: entry.playedAt }] : [];
      });
      if (!historyWasCleared) {
        persistPlayHistory(hydrated);
      }
      historyHydrated = true;
      return hydrated;
    })
    .catch(() => loadPlayHistory())
    .finally(() => {
      historyHydrationPromise = null;
    });
  return historyHydrationPromise;
}

export function removePlayHistoryEntry(trackId: number): void {
  try {
    const list = loadPendingPlayHistory().filter((entry) => entry.trackId !== trackId);
    persistPendingPlayHistory(list);
  } catch {
    /* ignore */
  }
}

export function clearPlayHistory(): void {
  try {
    safeStorage.removeItem(HISTORY_KEY);
    historyHydrated = true;
  } catch {
    /* ignore */
  }
}

/* ── Store interface ── */

interface PlayerState {
  currentTrack: PlayableTrack | null;
  isPlaying: boolean;
  isStalled: boolean;
  playbackError: string | null;
  currentTime: number;
  duration: number;
  volume: number;
  muted: boolean;
  queue: PlayableTrack[];
  shuffle: boolean;
  repeat: RepeatMode;
  /**
   * SR-83: The currently visible track list on the active page.
   * When set, next()/prev() navigates through this list instead of the queue.
   * Falls back to queue-based navigation (shuffle/repeat) when empty.
   */
  trackListContext: PlayableTrack[];
  persistedHydration: 'idle' | 'loading' | 'ready' | 'error';
  hydratePersistedState: () => Promise<void>;
  play: (track: PlayableTrack) => void;
  pause: () => void;
  resume: () => void;
  next: () => void;
  prev: () => void;
  seek: (time: number) => void;
  setVolume: (volume: number) => void;
  toggleMute: () => void;
  toggleShuffle: () => void;
  cycleRepeat: () => void;
  playAll: (tracks: PlayableTrack[]) => void;
  addToQueue: (track: PlayableTrack) => void;
  removeFromQueue: (trackId: number) => void;
  reorderQueue: (fromIndex: number, toIndex: number) => void;
  clearQueue: () => void;
  setTrackListContext: (tracks: PlayableTrack[]) => () => void;
}

export const usePlayerStore = create<PlayerState>((set, get) => {
  let trackListContextOwner = 0;

  const supersedePersistedHydration = () => {
    playerStateGeneration += 1;
    pendingPlayerState = null;
  };

  const cancelBuffering = (updates?: Partial<PlayerState>) => {
    bufferingGeneration += 1;
    if (bufferingTimer !== null) {
      clearTimeout(bufferingTimer);
      bufferingTimer = null;
    }

    if (updates) {
      set({ ...updates, isStalled: false });
    } else if (get().isStalled) {
      set({ isStalled: false });
    }
  };

  const beginBuffering = () => {
    const { currentTrack, isStalled, playbackError } = get();
    if (!currentTrack || isStalled || playbackError || bufferingTimer !== null) return;

    const generation = ++bufferingGeneration;
    const attempt = playbackAttempt;
    const trackId = currentTrack.id;

    bufferingTimer = setTimeout(() => {
      const state = get();
      if (
        generation !== bufferingGeneration ||
        attempt !== playbackAttempt ||
        state.currentTrack?.id !== trackId ||
        state.playbackError !== null
      ) {
        return;
      }

      bufferingTimer = null;
      set({ isStalled: true });
    }, BUFFERING_THRESHOLD_MS);
  };

  const stopWithError = (message: string) => {
    playbackAttempt += 1;
    if (!get().currentTrack) {
      cancelBuffering();
      return;
    }
    audio.pause();
    cancelBuffering({ isPlaying: false, playbackError: message });
  };

  const startPlayback = (onSuccess?: () => void) => {
    const attempt = ++playbackAttempt;
    cancelBuffering({ isPlaying: false, playbackError: null });

    let playPromise: Promise<void>;
    try {
      playPromise = audio.play();
    } catch {
      if (attempt === playbackAttempt) {
        cancelBuffering({ isPlaying: false, playbackError: PLAYBACK_ERROR });
      }
      return;
    }

    void playPromise.then(
      () => {
        if (attempt !== playbackAttempt) return;
        cancelBuffering({ isPlaying: true, playbackError: null });
        onSuccess?.();
      },
      () => {
        if (attempt !== playbackAttempt) return;
        cancelBuffering({ isPlaying: false, playbackError: PLAYBACK_ERROR });
      },
    );
  };

  // Audio event listeners
  audio.addEventListener('timeupdate', () => {
    const fallbackDuration = get().currentTrack?.duration ?? 0;
    const duration = getFiniteMediaDuration(audio.duration, fallbackDuration);
    const currentTime = clampPlaybackTime(audio.currentTime, duration);
    if (audio.currentTime !== currentTime) audio.currentTime = currentTime;
    cancelBuffering({ currentTime, duration });
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
    const state = get();
    const duration = getFiniteMediaDuration(audio.duration, state.currentTrack?.duration ?? 0);
    const currentTime = clampPlaybackTime(audio.currentTime, duration);
    if (audio.currentTime !== currentTime) audio.currentTime = currentTime;
    set({ duration, currentTime });
    persistState({ ...state, duration, currentTime });
  });

  audio.addEventListener('stalled', beginBuffering);
  audio.addEventListener('waiting', beginBuffering);
  audio.addEventListener('canplay', () => cancelBuffering());
  audio.addEventListener('playing', () => cancelBuffering());

  audio.addEventListener('error', () => {
    stopWithError(MEDIA_ERROR);
  });

  // Initialize volume from localStorage
  const savedVolume = parseFloat(safeStorage.getItem('playerVolume') ?? '1');
  audio.volume = isNaN(savedVolume) ? 1 : savedVolume;

  return {
    currentTrack: null,
    isPlaying: false,
    isStalled: false,
    playbackError: null,
    currentTime: 0,
    duration: 0,
    volume: audio.volume,
    muted: false,
    queue: [],
    shuffle: false,
    repeat: 'off' as RepeatMode,
    trackListContext: [],
    persistedHydration: 'idle',

    hydratePersistedState: () => {
      if (!pendingPlayerState) {
        set({ persistedHydration: 'ready' });
        return Promise.resolve();
      }
      if (playerHydrationPromise) return playerHydrationPromise;

      const snapshot = pendingPlayerState;
      if (snapshot.queueTrackIds.length === 0) {
        pendingPlayerState = null;
        playbackAttempt += 1;
        audio.pause();
        audio.src = '';
        cancelBuffering({
          currentTrack: null,
          isPlaying: false,
          playbackError: null,
          currentTime: 0,
          duration: 0,
          queue: [],
          persistedHydration: 'ready',
        });
        return Promise.resolve();
      }

      const hydrationGeneration = playerStateGeneration;
      set({ persistedHydration: 'loading' });
      playerHydrationPromise = fetchPlayableTracks(snapshot.queueTrackIds)
        .then((tracks) => {
          if (hydrationGeneration !== playerStateGeneration || pendingPlayerState !== snapshot) {
            return;
          }
          const tracksById = new Map(tracks.map((track) => [track.id, track]));
          const queue = snapshot.queueTrackIds.flatMap((trackId) => {
            const track = tracksById.get(trackId);
            return track ? [track] : [];
          });
          const currentTrack = snapshot.currentTrackId
            ? (tracksById.get(snapshot.currentTrackId) ?? null)
            : null;
          const currentTime = currentTrack
            ? clampPlaybackTime(snapshot.currentTime, currentTrack.duration)
            : 0;
          if (currentTrack) {
            restoreAudioSource(currentTrack, currentTime);
          }
          set({
            currentTrack,
            queue,
            currentTime,
            duration: currentTrack?.duration ?? 0,
            persistedHydration: 'ready',
          });
          persistState({ currentTrack, queue, currentTime });
          pendingPlayerState = null;
        })
        .catch(() => {
          if (hydrationGeneration === playerStateGeneration && pendingPlayerState === snapshot) {
            set({ persistedHydration: 'error' });
          }
        })
        .finally(() => {
          playerHydrationPromise = null;
        });
      return playerHydrationPromise;
    },

    play: (track: PlayableTrack) => {
      supersedePersistedHydration();
      cancelBuffering();
      audio.src = `${STREAM_BASE}/${track.id}/stream`;

      // Add to queue if not already present
      const { queue } = get();
      const inQueue = queue.some((t) => t.id === track.id);
      const newQueue = inQueue
        ? queue.map((queuedTrack) => (queuedTrack.id === track.id ? track : queuedTrack))
        : [...queue, track];
      set({
        currentTrack: track,
        isPlaying: false,
        isStalled: false,
        playbackError: null,
        currentTime: 0,
        duration: track.duration,
        queue: newQueue,
        persistedHydration: 'ready',
      });
      persistState({ currentTrack: track, queue: newQueue, currentTime: 0 });

      startPlayback(() => {
        // SR-89: Record only playback that actually started.
        savePlayHistory(track);
      });
    },

    pause: () => {
      playbackAttempt += 1;
      const state = get();
      const duration = getFiniteMediaDuration(
        audio.duration,
        state.currentTrack?.duration ?? state.duration,
      );
      const currentTime = clampPlaybackTime(audio.currentTime, duration);
      if (audio.currentTime !== currentTime) audio.currentTime = currentTime;
      audio.pause();
      cancelBuffering({ isPlaying: false, currentTime, duration });
      if (state.currentTrack) {
        persistState({ ...state, currentTime, duration });
      }
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
      supersedePersistedHydration();
      isSeeking = true;
      const state = get();
      const duration = getFiniteMediaDuration(state.duration, state.currentTrack?.duration ?? 0);
      const currentTime = clampPlaybackTime(time, duration);
      audio.currentTime = currentTime;
      set({ currentTime, persistedHydration: 'ready' });
      persistState({ ...state, currentTime });
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

    playAll: (tracks: PlayableTrack[]) => {
      if (tracks.length === 0) return;
      supersedePersistedHydration();
      const { play } = get();
      set({ queue: tracks, persistedHydration: 'ready' });
      play(tracks[0]);
      // persistState is called inside play()
    },

    addToQueue: (track: PlayableTrack) => {
      supersedePersistedHydration();
      set((state) => {
        const newQueue = state.queue.some((queuedTrack) => queuedTrack.id === track.id)
          ? state.queue.map((queuedTrack) => (queuedTrack.id === track.id ? track : queuedTrack))
          : [...state.queue, track];
        persistState({ ...state, queue: newQueue });
        return { queue: newQueue, persistedHydration: 'ready' };
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
      supersedePersistedHydration();
      set((state) => {
        const newQueue = state.queue.filter((t) => t.id !== trackId);
        persistState({ ...state, queue: newQueue });
        return { queue: newQueue, persistedHydration: 'ready' };
      });
    },

    reorderQueue: (fromIndex: number, toIndex: number) => {
      supersedePersistedHydration();
      set((state) => {
        const next = [...state.queue];
        const [moved] = next.splice(fromIndex, 1);
        next.splice(toIndex, 0, moved);
        persistState({ ...state, queue: next });
        return { queue: next, persistedHydration: 'ready' };
      });
    },

    setTrackListContext: (tracks: PlayableTrack[]) => {
      const owner = ++trackListContextOwner;
      set({ trackListContext: tracks });
      return () => {
        if (owner !== trackListContextOwner) return;
        trackListContextOwner += 1;
        set({ trackListContext: [] });
      };
    },

    clearQueue: () => {
      supersedePersistedHydration();
      playbackAttempt += 1;
      audio.pause();
      audio.src = '';
      cancelBuffering({
        queue: [],
        currentTrack: null,
        isPlaying: false,
        playbackError: null,
        currentTime: 0,
        duration: 0,
        persistedHydration: 'ready',
      });
      persistState({ currentTrack: null, queue: [], currentTime: 0 });
    },
  };
});

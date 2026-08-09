import { beforeEach, describe, expect, it, vi } from 'vitest';
import type { PlayableTrack } from '@/types';
import { toPlayableTrack } from '@/utils/playableTrack';

const apiMocks = vi.hoisted(() => ({
  fetchPlayableTracks: vi.fn(),
}));

vi.mock('@/api/tracks', () => ({
  fetchPlayableTracks: apiMocks.fetchPlayableTracks,
}));

const audioInstances: TestAudio[] = [];

class TestAudio {
  src = '';
  currentTime = 0;
  duration = 0;
  volume = 1;
  muted = false;
  play = vi.fn(() => Promise.resolve());
  pause = vi.fn();

  constructor() {
    audioInstances.push(this);
  }

  addEventListener() {}

  removeEventListener() {}
}

const track: PlayableTrack = {
  id: 7,
  title: 'Persisted track',
  artistName: 'Artist',
  duration: 180,
  bpm: 120,
  tonality: 'C',
  thumbnail: null,
  waveformData: '[0.2,0.8]',
  tags: [],
};

const secondTrack: PlayableTrack = {
  ...track,
  id: 8,
  title: 'Second persisted track',
  duration: 210,
  waveformData: '[0.1,0.9]',
};

const thirdTrack: PlayableTrack = {
  ...track,
  id: 9,
  title: 'Explicit track',
  duration: 150,
  waveformData: '[0.3,0.7]',
};

const fourthTrack: PlayableTrack = {
  ...track,
  id: 10,
  title: 'Explicit queue track',
  duration: 160,
  waveformData: '[0.4,0.6]',
};

function deferred<T>() {
  let resolve!: (value: T | PromiseLike<T>) => void;
  let reject!: (reason?: unknown) => void;
  const promise = new Promise<T>((resolvePromise, rejectPromise) => {
    resolve = resolvePromise;
    reject = rejectPromise;
  });
  return { promise, resolve, reject };
}

function seedPersistedPlayerState(): void {
  localStorage.setItem(
    'playerState',
    JSON.stringify({
      version: 2,
      currentTrackId: 7,
      queueTrackIds: [7, 8],
      currentTime: 23,
    }),
  );
}

describe('player persistence', () => {
  beforeEach(() => {
    vi.resetModules();
    localStorage.clear();
    audioInstances.length = 0;
    apiMocks.fetchPlayableTracks.mockReset();
    vi.stubGlobal('Audio', TestAudio);
  });

  it('performs no import-time request and hydrates a legacy queue in one batch', async () => {
    localStorage.setItem(
      'playerState',
      JSON.stringify({
        version: 1,
        currentTrack: { ...track, duration: 0, waveformData: null },
        queue: [{ ...track, duration: 0, waveformData: null }, secondTrack, track],
        queueIndex: 0,
        currentTime: 37,
      }),
    );
    apiMocks.fetchPlayableTracks.mockResolvedValue([track, secondTrack]);

    const { usePlayerStore } = await import('@/store/playerStore');
    const audio = audioInstances[0];

    expect(apiMocks.fetchPlayableTracks).not.toHaveBeenCalled();
    expect(usePlayerStore.getState()).toMatchObject({
      currentTrack: null,
      queue: [],
      currentTime: 0,
    });
    expect(audio.src).toBe('');

    await usePlayerStore.getState().hydratePersistedState();

    expect(apiMocks.fetchPlayableTracks).toHaveBeenCalledTimes(1);
    expect(apiMocks.fetchPlayableTracks).toHaveBeenCalledWith([7, 8]);
    expect(usePlayerStore.getState()).toMatchObject({
      currentTrack: track,
      queue: [track, secondTrack],
      currentTime: 37,
      persistedHydration: 'ready',
    });
    expect(audio.src).toBe('/api/tracks/7/stream');
    expect(audio.currentTime).toBe(37);

    usePlayerStore.getState().resume();
    await vi.waitFor(() => expect(audio.play).toHaveBeenCalledTimes(1));
  });

  it.each([
    ['obsolete version', { version: 0, currentTrack: track, queue: [track], currentTime: 5 }],
    ['wrong field shapes', { version: 1, currentTrack: {}, queue: {}, currentTime: '5' }],
  ])('falls back safely for %s', async (_label, persistedState) => {
    localStorage.setItem('playerState', JSON.stringify(persistedState));

    const { usePlayerStore } = await import('@/store/playerStore');

    expect(usePlayerStore.getState()).toMatchObject({
      currentTrack: null,
      queue: [],
      currentTime: 0,
    });
    expect(audioInstances[0].src).toBe('');
  });

  it('drops a missing current ID without corrupting the remaining queue', async () => {
    localStorage.setItem(
      'playerState',
      JSON.stringify({
        version: 2,
        currentTrackId: 999,
        queueTrackIds: [999, 7, 8],
        currentTime: 12,
      }),
    );
    apiMocks.fetchPlayableTracks.mockResolvedValue([track, secondTrack]);

    const { usePlayerStore } = await import('@/store/playerStore');
    await usePlayerStore.getState().hydratePersistedState();

    expect(apiMocks.fetchPlayableTracks).toHaveBeenCalledTimes(1);
    expect(usePlayerStore.getState()).toMatchObject({
      currentTrack: null,
      queue: [track, secondTrack],
      currentTime: 0,
    });
    expect(audioInstances[0].src).toBe('');
  });

  it('marks an empty persisted state ready without making a batch request', async () => {
    localStorage.setItem(
      'playerState',
      JSON.stringify({
        version: 2,
        currentTrackId: null,
        queueTrackIds: [],
        currentTime: 0,
      }),
    );

    const { usePlayerStore } = await import('@/store/playerStore');
    await usePlayerStore.getState().hydratePersistedState();

    expect(apiMocks.fetchPlayableTracks).not.toHaveBeenCalled();
    expect(usePlayerStore.getState()).toMatchObject({
      currentTrack: null,
      queue: [],
      currentTime: 0,
      persistedHydration: 'ready',
    });
  });

  it('persists and reloads an aggregate track whose nullable wire keys were omitted', async () => {
    const normalizedTrack = toPlayableTrack({
      trackId: 17,
      title: 'Wire aggregate track',
      artistName: 'Aggregate artist',
      duration: 77,
    });
    const firstModule = await import('@/store/playerStore');

    firstModule.usePlayerStore.getState().play(normalizedTrack);

    expect(firstModule.usePlayerStore.getState()).toMatchObject({
      currentTrack: normalizedTrack,
      queue: [normalizedTrack],
      duration: 77,
    });
    expect(normalizedTrack).toMatchObject({ thumbnail: null, waveformData: null });
    expect(JSON.parse(localStorage.getItem('playerState') ?? '{}')).toMatchObject({
      version: 2,
      currentTrackId: 17,
      queueTrackIds: [17],
    });

    apiMocks.fetchPlayableTracks.mockResolvedValueOnce([normalizedTrack]);
    vi.resetModules();
    const reloadedModule = await import('@/store/playerStore');
    await reloadedModule.usePlayerStore.getState().hydratePersistedState();

    expect(apiMocks.fetchPlayableTracks).toHaveBeenCalledWith([17]);
    expect(reloadedModule.usePlayerStore.getState()).toMatchObject({
      currentTrack: normalizedTrack,
      queue: [normalizedTrack],
      duration: 77,
      persistedHydration: 'ready',
    });
  });

  it('does not let in-flight persisted hydration overwrite an explicit play', async () => {
    seedPersistedPlayerState();
    const request = deferred<PlayableTrack[]>();
    apiMocks.fetchPlayableTracks.mockReturnValueOnce(request.promise);

    const { usePlayerStore } = await import('@/store/playerStore');
    const hydration = usePlayerStore.getState().hydratePersistedState();

    usePlayerStore.getState().play(thirdTrack);
    request.resolve([track, secondTrack]);
    await hydration;

    expect(usePlayerStore.getState()).toMatchObject({
      currentTrack: thirdTrack,
      queue: [thirdTrack],
      currentTime: 0,
      persistedHydration: 'ready',
    });
    expect(audioInstances[0].src).toBe('/api/tracks/9/stream');
  });

  it('does not let a stale hydration failure mark an explicit play as failed', async () => {
    seedPersistedPlayerState();
    const request = deferred<PlayableTrack[]>();
    apiMocks.fetchPlayableTracks.mockReturnValueOnce(request.promise);

    const { usePlayerStore } = await import('@/store/playerStore');
    const hydration = usePlayerStore.getState().hydratePersistedState();

    usePlayerStore.getState().play(thirdTrack);
    request.reject(new Error('stale hydration failure'));
    await hydration;

    expect(usePlayerStore.getState()).toMatchObject({
      currentTrack: thirdTrack,
      queue: [thirdTrack],
      persistedHydration: 'ready',
    });
  });

  it('does not let in-flight persisted hydration overwrite clearQueue', async () => {
    seedPersistedPlayerState();
    const request = deferred<PlayableTrack[]>();
    apiMocks.fetchPlayableTracks.mockReturnValueOnce(request.promise);

    const { usePlayerStore } = await import('@/store/playerStore');
    const hydration = usePlayerStore.getState().hydratePersistedState();

    usePlayerStore.getState().clearQueue();
    request.resolve([track, secondTrack]);
    await hydration;

    expect(usePlayerStore.getState()).toMatchObject({
      currentTrack: null,
      queue: [],
      currentTime: 0,
      persistedHydration: 'ready',
    });
    expect(audioInstances[0].src).toBe('');
  });

  it('fences add, remove, and reorder queue mutations from in-flight hydration', async () => {
    seedPersistedPlayerState();
    const request = deferred<PlayableTrack[]>();
    apiMocks.fetchPlayableTracks.mockReturnValueOnce(request.promise);

    const { usePlayerStore } = await import('@/store/playerStore');
    usePlayerStore.setState({ queue: [track, secondTrack] });
    const hydration = usePlayerStore.getState().hydratePersistedState();

    usePlayerStore.getState().addToQueue(thirdTrack);
    usePlayerStore.getState().removeFromQueue(track.id);
    usePlayerStore.getState().reorderQueue(1, 0);
    request.resolve([track, secondTrack]);
    await hydration;

    expect(usePlayerStore.getState()).toMatchObject({
      currentTrack: null,
      queue: [thirdTrack, secondTrack],
      persistedHydration: 'ready',
    });
  });

  it('does not let in-flight persisted hydration overwrite playAll', async () => {
    seedPersistedPlayerState();
    const request = deferred<PlayableTrack[]>();
    apiMocks.fetchPlayableTracks.mockReturnValueOnce(request.promise);

    const { usePlayerStore } = await import('@/store/playerStore');
    const hydration = usePlayerStore.getState().hydratePersistedState();

    usePlayerStore.getState().playAll([thirdTrack, fourthTrack]);
    request.resolve([track, secondTrack]);
    await hydration;

    expect(usePlayerStore.getState()).toMatchObject({
      currentTrack: thirdTrack,
      queue: [thirdTrack, fourthTrack],
      currentTime: 0,
      persistedHydration: 'ready',
    });
  });

  it('retries persisted hydration after a batch failure', async () => {
    seedPersistedPlayerState();
    apiMocks.fetchPlayableTracks
      .mockRejectedValueOnce(new Error('temporary failure'))
      .mockResolvedValueOnce([track, secondTrack]);

    const { usePlayerStore } = await import('@/store/playerStore');
    await usePlayerStore.getState().hydratePersistedState();

    expect(usePlayerStore.getState().persistedHydration).toBe('error');

    await usePlayerStore.getState().hydratePersistedState();

    expect(apiMocks.fetchPlayableTracks).toHaveBeenCalledTimes(2);
    expect(usePlayerStore.getState()).toMatchObject({
      currentTrack: track,
      queue: [track, secondTrack],
      currentTime: 23,
      persistedHydration: 'ready',
    });
  });

  it('hydrates legacy history once and isolates malformed or inactive IDs', async () => {
    localStorage.setItem(
      'playHistory',
      JSON.stringify([
        {
          trackId: 7,
          title: 'Valid history',
          thumbnail: null,
          playedAt: '2026-07-17T00:00:00Z',
        },
        {
          trackId: 8,
          title: 'Second history',
          thumbnail: null,
          playedAt: '2026-07-16T00:00:00Z',
        },
        {
          trackId: 999,
          title: 'Inactive history',
          thumbnail: null,
          playedAt: '2026-07-15T00:00:00Z',
        },
        { trackId: 'bad', title: 1, thumbnail: {}, playedAt: 'not-a-date' },
      ]),
    );
    apiMocks.fetchPlayableTracks.mockResolvedValue([track, secondTrack]);

    const { hydratePlayHistory, loadPlayHistory } = await import('@/store/playerStore');

    expect(apiMocks.fetchPlayableTracks).not.toHaveBeenCalled();
    expect(loadPlayHistory()).toEqual([]);

    await expect(hydratePlayHistory()).resolves.toEqual([
      {
        track,
        playedAt: '2026-07-17T00:00:00Z',
      },
      {
        track: secondTrack,
        playedAt: '2026-07-16T00:00:00Z',
      },
    ]);
    expect(apiMocks.fetchPlayableTracks).toHaveBeenCalledTimes(1);
    expect(apiMocks.fetchPlayableTracks).toHaveBeenCalledWith([7, 8, 999]);
    await hydratePlayHistory();
    expect(apiMocks.fetchPlayableTracks).toHaveBeenCalledTimes(1);

    localStorage.setItem('playHistory', JSON.stringify({ entries: [] }));
    expect(loadPlayHistory()).toEqual([]);
  });

  it('preserves pending legacy history when a new play is saved before hydration', async () => {
    localStorage.setItem(
      'playHistory',
      JSON.stringify([
        {
          trackId: 8,
          title: 'Legacy history',
          thumbnail: null,
          playedAt: '2026-07-16T00:00:00Z',
        },
      ]),
    );
    apiMocks.fetchPlayableTracks.mockResolvedValue([track, secondTrack]);

    const { hydratePlayHistory, savePlayHistory } = await import('@/store/playerStore');
    savePlayHistory(track);

    const hydrated = await hydratePlayHistory();

    expect(apiMocks.fetchPlayableTracks).toHaveBeenCalledTimes(1);
    expect(apiMocks.fetchPlayableTracks).toHaveBeenCalledWith([7, 8]);
    expect(hydrated.map((entry) => entry.track.id)).toEqual([7, 8]);
  });

  it('merges a play saved while history hydration is in flight', async () => {
    localStorage.setItem(
      'playHistory',
      JSON.stringify([{ trackId: 8, playedAt: '2026-07-16T00:00:00Z' }]),
    );
    const request = deferred<PlayableTrack[]>();
    apiMocks.fetchPlayableTracks.mockReturnValueOnce(request.promise);

    const { hydratePlayHistory, savePlayHistory } = await import('@/store/playerStore');
    const hydration = hydratePlayHistory();

    savePlayHistory(track);
    request.resolve([secondTrack]);
    const hydrated = await hydration;

    expect(apiMocks.fetchPlayableTracks).toHaveBeenCalledTimes(1);
    expect(hydrated.map((entry) => entry.track.id)).toEqual([7, 8]);
  });

  it('preserves a deletion made while history hydration is in flight', async () => {
    localStorage.setItem(
      'playHistory',
      JSON.stringify([
        { trackId: 7, playedAt: '2026-07-17T00:00:00Z' },
        { trackId: 8, playedAt: '2026-07-16T00:00:00Z' },
      ]),
    );
    const request = deferred<PlayableTrack[]>();
    apiMocks.fetchPlayableTracks.mockReturnValueOnce(request.promise);

    const { hydratePlayHistory, removePlayHistoryEntry } = await import('@/store/playerStore');
    const hydration = hydratePlayHistory();

    removePlayHistoryEntry(track.id);
    request.resolve([track, secondTrack]);
    const hydrated = await hydration;

    expect(apiMocks.fetchPlayableTracks).toHaveBeenCalledTimes(1);
    expect(hydrated.map((entry) => entry.track.id)).toEqual([8]);
    expect(JSON.parse(localStorage.getItem('playHistory') ?? '[]')).toEqual([
      expect.objectContaining({ track: expect.objectContaining({ id: 8 }) }),
    ]);
  });

  it('preserves a clear made while history hydration is in flight', async () => {
    localStorage.setItem(
      'playHistory',
      JSON.stringify([
        { trackId: 7, playedAt: '2026-07-17T00:00:00Z' },
        { trackId: 8, playedAt: '2026-07-16T00:00:00Z' },
      ]),
    );
    const request = deferred<PlayableTrack[]>();
    apiMocks.fetchPlayableTracks.mockReturnValueOnce(request.promise);

    const { clearPlayHistory, hydratePlayHistory } = await import('@/store/playerStore');
    const hydration = hydratePlayHistory();

    clearPlayHistory();
    expect(localStorage.getItem('playHistory')).toBeNull();
    request.resolve([track, secondTrack]);
    await expect(hydration).resolves.toEqual([]);

    expect(apiMocks.fetchPlayableTracks).toHaveBeenCalledTimes(1);
    expect(localStorage.getItem('playHistory')).toBeNull();
  });

  it('keeps latest history order and the 100-entry cap after an in-flight save', async () => {
    const existingTracks = Array.from(
      { length: 100 },
      (_, index): PlayableTrack => ({
        ...track,
        id: index + 1,
        title: `History track ${index + 1}`,
      }),
    );
    localStorage.setItem(
      'playHistory',
      JSON.stringify(
        existingTracks.map((historyTrack) => ({
          trackId: historyTrack.id,
          playedAt: '2026-07-16T00:00:00Z',
        })),
      ),
    );
    const newestTrack: PlayableTrack = { ...thirdTrack, id: 101, title: 'Newest history track' };
    const request = deferred<PlayableTrack[]>();
    apiMocks.fetchPlayableTracks.mockReturnValueOnce(request.promise);

    const { hydratePlayHistory, savePlayHistory } = await import('@/store/playerStore');
    const hydration = hydratePlayHistory();

    savePlayHistory(newestTrack);
    request.resolve(existingTracks);
    const hydrated = await hydration;

    expect(apiMocks.fetchPlayableTracks).toHaveBeenCalledTimes(1);
    expect(apiMocks.fetchPlayableTracks).toHaveBeenCalledWith(existingTracks.map(({ id }) => id));
    expect(hydrated).toHaveLength(100);
    expect(hydrated.map((entry) => entry.track.id)).toEqual([
      101,
      ...existingTracks.slice(0, 99).map(({ id }) => id),
    ]);
  });
});

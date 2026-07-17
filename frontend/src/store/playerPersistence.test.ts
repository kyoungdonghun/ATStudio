import { beforeEach, describe, expect, it, vi } from 'vitest';
import type { Track } from '@/types';

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

const track: Track = {
  id: 7,
  title: 'Persisted track',
  artistName: 'Artist',
  duration: 180,
  bpm: 120,
  tonality: 'C',
  description: null,
  audioFile: '/api/tracks/7/stream',
  thumbnail: null,
  waveformData: null,
  tags: [],
  isActive: true,
  playCount: 0,
  likeCount: 0,
  downloadCount: 0,
  createdAt: '2026-07-17T00:00:00Z',
  updatedAt: '2026-07-17T00:00:00Z',
};

describe('player persistence', () => {
  beforeEach(() => {
    vi.resetModules();
    localStorage.clear();
    audioInstances.length = 0;
    vi.stubGlobal('Audio', TestAudio);
  });

  it('rehydrates the persisted source and time before the first resume', async () => {
    localStorage.setItem(
      'playerState',
      JSON.stringify({
        version: 1,
        currentTrack: track,
        queue: [track],
        queueIndex: 0,
        currentTime: 37,
      }),
    );

    const { usePlayerStore } = await import('@/store/playerStore');
    const audio = audioInstances[0];

    expect(usePlayerStore.getState()).toMatchObject({
      currentTrack: track,
      queue: [track],
      currentTime: 37,
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

  it('filters malformed play-history entries from valid JSON', async () => {
    localStorage.setItem(
      'playHistory',
      JSON.stringify([
        {
          trackId: 7,
          title: 'Valid history',
          thumbnail: null,
          playedAt: '2026-07-17T00:00:00Z',
        },
        { trackId: 'bad', title: 1, thumbnail: {}, playedAt: 'not-a-date' },
      ]),
    );

    const { loadPlayHistory } = await import('@/store/playerStore');

    expect(loadPlayHistory()).toEqual([
      {
        trackId: 7,
        title: 'Valid history',
        thumbnail: null,
        playedAt: '2026-07-17T00:00:00Z',
      },
    ]);

    localStorage.setItem('playHistory', JSON.stringify({ entries: [] }));
    expect(loadPlayHistory()).toEqual([]);
  });
});

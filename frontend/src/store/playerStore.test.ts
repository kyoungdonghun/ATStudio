import { act } from '@testing-library/react';
import { beforeAll, beforeEach, describe, expect, it, vi } from 'vitest';
import type { Track } from '@/types';

type PlayerStoreModule = typeof import('@/store/playerStore');
type AudioListener = EventListenerOrEventListenerObject;

const PLAYBACK_ERROR = '재생을 시작할 수 없습니다. 잠시 후 다시 시도해 주세요.';
const MEDIA_ERROR = '오디오를 재생하는 중 오류가 발생했습니다.';

class ControlledAudio {
  static latest: ControlledAudio;

  src = '';
  currentTime = 0;
  duration = 0;
  volume = 1;
  muted = false;
  play = vi.fn<() => Promise<void>>(() => Promise.resolve());
  pause = vi.fn();

  private listeners = new Map<string, Set<AudioListener>>();

  constructor() {
    ControlledAudio.latest = this;
  }

  addEventListener(type: string, listener: AudioListener) {
    const listenersForType = this.listeners.get(type) ?? new Set<AudioListener>();
    listenersForType.add(listener);
    this.listeners.set(type, listenersForType);
  }

  removeEventListener(type: string, listener: AudioListener) {
    this.listeners.get(type)?.delete(listener);
  }

  dispatch(type: string) {
    const event = new Event(type);
    this.listeners.get(type)?.forEach((listener) => {
      if (typeof listener === 'function') {
        listener.call(this, event);
      } else {
        listener.handleEvent(event);
      }
    });
  }
}

const firstTrack: Track = {
  id: 1,
  title: 'First track',
  artistName: 'Artist',
  duration: 120,
  bpm: 120,
  tonality: 'C',
  description: null,
  audioFile: null,
  thumbnail: null,
  waveformData: null,
  tags: [],
  isActive: true,
  playCount: 0,
  likeCount: 0,
  downloadCount: 0,
  createdAt: '2026-07-15T00:00:00Z',
  updatedAt: '2026-07-15T00:00:00Z',
};

const secondTrack: Track = {
  ...firstTrack,
  id: 2,
  title: 'Second track',
};

let usePlayerStore: PlayerStoreModule['usePlayerStore'];
let audio: ControlledAudio;

const nextStopScenarios: Array<[string, () => void]> = [
  [
    'visible list end',
    () => {
      usePlayerStore.setState({
        currentTrack: firstTrack,
        queue: [firstTrack],
        trackListContext: [firstTrack],
      });
    },
  ],
  [
    'empty queue',
    () => {
      usePlayerStore.setState({
        currentTrack: firstTrack,
        queue: [],
        trackListContext: [],
      });
    },
  ],
  [
    'repeat-off queue end',
    () => {
      usePlayerStore.setState({
        currentTrack: firstTrack,
        queue: [firstTrack],
        trackListContext: [],
        repeat: 'off',
      });
    },
  ],
];

beforeAll(async () => {
  vi.stubGlobal('Audio', ControlledAudio);
  vi.resetModules();
  ({ usePlayerStore } = await import('@/store/playerStore'));
  audio = ControlledAudio.latest;
});

beforeEach(() => {
  vi.useRealTimers();
  localStorage.clear();
  audio.src = '';
  audio.currentTime = 0;
  audio.duration = 0;
  audio.volume = 1;
  audio.muted = false;
  audio.play.mockReset();
  audio.play.mockResolvedValue(undefined);
  audio.pause.mockReset();
  usePlayerStore.setState({
    currentTrack: null,
    isPlaying: false,
    playbackError: null,
    currentTime: 0,
    duration: 0,
    volume: 1,
    muted: false,
    queue: [],
    shuffle: false,
    repeat: 'off',
    trackListContext: [],
  });
});

describe('playerStore playback lifecycle', () => {
  it('sets playing state and history only after play resolves', async () => {
    let resolvePlay: (() => void) | undefined;
    const pendingPlay = new Promise<void>((resolve) => {
      resolvePlay = resolve;
    });
    audio.play.mockReturnValueOnce(pendingPlay);

    act(() => usePlayerStore.getState().play(firstTrack));

    expect(audio.src).toBe('/api/tracks/1/stream');
    expect(usePlayerStore.getState().isPlaying).toBe(false);
    expect(usePlayerStore.getState().currentTrack).toEqual(firstTrack);
    expect(usePlayerStore.getState().queue).toEqual([firstTrack]);
    expect(localStorage.getItem('playHistory')).toBeNull();
    expect(JSON.parse(localStorage.getItem('playerState') ?? '{}')).toMatchObject({
      currentTrack: { id: 1 },
      queue: [{ id: 1 }],
      currentTime: 0,
    });

    await act(async () => {
      resolvePlay?.();
      await pendingPlay;
    });

    expect(usePlayerStore.getState().isPlaying).toBe(true);
    expect(usePlayerStore.getState().playbackError).toBeNull();
    expect(JSON.parse(localStorage.getItem('playHistory') ?? '[]')).toEqual([
      expect.objectContaining({ trackId: 1, title: 'First track' }),
    ]);
  });

  it('keeps play and resume rejections in a coherent non-playing state', async () => {
    audio.play.mockRejectedValueOnce(new Error('autoplay blocked'));

    act(() => usePlayerStore.getState().play(firstTrack));

    await vi.waitFor(() => {
      expect(usePlayerStore.getState().playbackError).toBe(PLAYBACK_ERROR);
    });
    expect(usePlayerStore.getState().isPlaying).toBe(false);
    expect(localStorage.getItem('playHistory')).toBeNull();

    audio.play.mockRejectedValueOnce(new Error('network unavailable'));
    act(() => usePlayerStore.getState().resume());

    expect(usePlayerStore.getState().playbackError).toBeNull();
    await vi.waitFor(() => {
      expect(usePlayerStore.getState().playbackError).toBe(PLAYBACK_ERROR);
    });
    expect(usePlayerStore.getState().isPlaying).toBe(false);
  });

  it('ignores a stale play resolution after playback is paused', async () => {
    let resolvePlay: (() => void) | undefined;
    const pendingPlay = new Promise<void>((resolve) => {
      resolvePlay = resolve;
    });
    audio.play.mockReturnValueOnce(pendingPlay);

    act(() => usePlayerStore.getState().play(firstTrack));
    act(() => usePlayerStore.getState().pause());

    await act(async () => {
      resolvePlay?.();
      await pendingPlay;
    });

    expect(usePlayerStore.getState().isPlaying).toBe(false);
    expect(localStorage.getItem('playHistory')).toBeNull();
  });

  it('updates metadata, time progression, and seek without changing playback state', () => {
    vi.useFakeTimers();
    audio.duration = 120;
    act(() => audio.dispatch('loadedmetadata'));
    expect(usePlayerStore.getState().duration).toBe(120);

    audio.currentTime = 42;
    audio.duration = 121;
    act(() => audio.dispatch('timeupdate'));
    expect(usePlayerStore.getState()).toMatchObject({
      currentTime: 42,
      duration: 121,
      isPlaying: false,
    });

    act(() => usePlayerStore.getState().seek(75));
    expect(audio.currentTime).toBe(75);
    expect(usePlayerStore.getState().currentTime).toBe(75);
    expect(JSON.parse(localStorage.getItem('playerState') ?? '{}')).toMatchObject({
      currentTime: 75,
    });
    act(() => vi.advanceTimersByTime(100));
    vi.useRealTimers();
  });

  it('keeps stalled transient and clears a fatal media error after retry', async () => {
    act(() => usePlayerStore.getState().play(firstTrack));
    await vi.waitFor(() => expect(usePlayerStore.getState().isPlaying).toBe(true));

    audio.pause.mockClear();
    act(() => audio.dispatch('stalled'));
    expect(audio.pause).not.toHaveBeenCalled();
    expect(usePlayerStore.getState()).toMatchObject({
      isPlaying: true,
      playbackError: null,
    });

    act(() => audio.dispatch('error'));
    expect(usePlayerStore.getState()).toMatchObject({
      isPlaying: false,
      playbackError: MEDIA_ERROR,
    });

    act(() => usePlayerStore.getState().resume());
    expect(usePlayerStore.getState().playbackError).toBeNull();
    await vi.waitFor(() => expect(usePlayerStore.getState().isPlaying).toBe(true));
  });

  it.each(nextStopScenarios)(
    'invalidates pending playback at %s',
    async (_scenario, arrangeState) => {
      let resolvePlay: (() => void) | undefined;
      const pendingPlay = new Promise<void>((resolve) => {
        resolvePlay = resolve;
      });
      audio.play.mockReturnValueOnce(pendingPlay);
      arrangeState();

      act(() => usePlayerStore.getState().resume());
      act(() => usePlayerStore.getState().next());

      await act(async () => {
        resolvePlay?.();
        await pendingPlay;
      });

      expect(audio.pause).toHaveBeenCalledTimes(1);
      expect(usePlayerStore.getState().isPlaying).toBe(false);
    },
  );

  it('keeps next and repeat-one playback compatible', async () => {
    usePlayerStore.setState({ queue: [firstTrack, secondTrack] });
    act(() => usePlayerStore.getState().play(firstTrack));
    await vi.waitFor(() => expect(usePlayerStore.getState().isPlaying).toBe(true));

    act(() => usePlayerStore.getState().next());
    await vi.waitFor(() => expect(usePlayerStore.getState().currentTrack?.id).toBe(2));
    expect(audio.src).toBe('/api/tracks/2/stream');

    usePlayerStore.setState({ repeat: 'one' });
    audio.currentTime = 120;
    audio.play.mockClear();
    act(() => audio.dispatch('ended'));

    expect(audio.currentTime).toBe(0);
    expect(audio.play).toHaveBeenCalledTimes(1);
    await vi.waitFor(() => expect(usePlayerStore.getState().isPlaying).toBe(true));
  });
});

import { act } from '@testing-library/react';
import { afterEach, beforeAll, beforeEach, describe, expect, it, vi } from 'vitest';
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
    isStalled: false,
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

afterEach(() => {
  vi.restoreAllMocks();
  act(() => usePlayerStore.getState().clearQueue());
  if (vi.isFakeTimers()) {
    vi.clearAllTimers();
  }
  vi.useRealTimers();
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
      version: 2,
      currentTrackId: 1,
      queueTrackIds: [1],
      currentTime: 0,
    });

    await act(async () => {
      resolvePlay?.();
      await pendingPlay;
    });

    expect(usePlayerStore.getState().isPlaying).toBe(true);
    expect(usePlayerStore.getState().playbackError).toBeNull();
    expect(JSON.parse(localStorage.getItem('playHistory') ?? '[]')).toEqual([
      expect.objectContaining({
        track: expect.objectContaining({ id: 1, title: 'First track' }),
      }),
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

  it('replaces the prior duration in the same transition as a new track selection', () => {
    const switchedTrack = { ...secondTrack, duration: 45 };

    act(() => usePlayerStore.getState().play(firstTrack));
    audio.currentTime = 60;
    audio.duration = 121;
    act(() => audio.dispatch('timeupdate'));
    expect(usePlayerStore.getState()).toMatchObject({ currentTime: 60, duration: 121 });

    act(() => usePlayerStore.getState().play(switchedTrack));

    expect(usePlayerStore.getState()).toMatchObject({
      currentTrack: switchedTrack,
      currentTime: 0,
      duration: 45,
    });

    audio.duration = 46;
    act(() => audio.dispatch('loadedmetadata'));
    expect(usePlayerStore.getState().duration).toBe(46);
  });

  it('keeps buffering pending and hidden at 0 ms and 1800 ms', () => {
    vi.useFakeTimers();
    usePlayerStore.setState({ currentTrack: firstTrack, isPlaying: true });

    act(() => audio.dispatch('waiting'));
    expect(usePlayerStore.getState()).toMatchObject({
      isPlaying: true,
      isStalled: false,
      playbackError: null,
    });

    act(() => vi.advanceTimersByTime(1_800));
    expect(usePlayerStore.getState()).toMatchObject({
      isPlaying: true,
      isStalled: false,
      playbackError: null,
    });
  });

  it('exposes retryable non-fatal buffering at exactly 2000 ms', () => {
    vi.useFakeTimers();
    usePlayerStore.setState({ currentTrack: firstTrack, isPlaying: true });
    audio.pause.mockClear();

    act(() => audio.dispatch('stalled'));
    act(() => vi.advanceTimersByTime(1_999));
    expect(usePlayerStore.getState().isStalled).toBe(false);

    act(() => vi.advanceTimersByTime(1));
    expect(audio.pause).not.toHaveBeenCalled();
    expect(usePlayerStore.getState()).toMatchObject({
      isPlaying: true,
      isStalled: true,
      playbackError: null,
    });
  });

  it('does not reset the threshold or create a late duplicate for repeated native events', () => {
    vi.useFakeTimers();
    usePlayerStore.setState({ currentTrack: firstTrack, isPlaying: true });

    act(() => audio.dispatch('waiting'));
    expect(vi.getTimerCount()).toBe(1);
    act(() => vi.advanceTimersByTime(1_000));
    act(() => audio.dispatch('stalled'));
    expect(vi.getTimerCount()).toBe(1);

    act(() => vi.advanceTimersByTime(1_000));
    expect(usePlayerStore.getState().isStalled).toBe(true);
    expect(vi.getTimerCount()).toBe(0);

    act(() => audio.dispatch('stalled'));
    expect(vi.getTimerCount()).toBe(0);
    act(() => audio.dispatch('canplay'));
    act(() => vi.advanceTimersByTime(1_000));
    expect(usePlayerStore.getState().isStalled).toBe(false);
  });

  it.each(['timeupdate', 'canplay', 'playing'])(
    'cancels pending buffering on %s before the threshold',
    (eventType) => {
      vi.useFakeTimers();
      usePlayerStore.setState({ currentTrack: firstTrack, isPlaying: true });
      act(() => audio.dispatch('waiting'));
      act(() => vi.advanceTimersByTime(1_800));

      act(() => audio.dispatch(eventType));
      act(() => vi.advanceTimersByTime(2_000));

      expect(vi.getTimerCount()).toBe(0);
      expect(usePlayerStore.getState().isStalled).toBe(false);
    },
  );

  it('clears sustained buffering on recovery', () => {
    vi.useFakeTimers();
    usePlayerStore.setState({ currentTrack: firstTrack, isPlaying: true });
    act(() => audio.dispatch('waiting'));
    act(() => vi.advanceTimersByTime(2_000));
    expect(usePlayerStore.getState().isStalled).toBe(true);

    act(() => audio.dispatch('playing'));
    expect(usePlayerStore.getState()).toMatchObject({
      isPlaying: true,
      isStalled: false,
      playbackError: null,
    });
  });

  it('cancels pending buffering on pause', () => {
    vi.useFakeTimers();
    usePlayerStore.setState({ currentTrack: firstTrack, isPlaying: true });
    act(() => audio.dispatch('waiting'));
    act(() => vi.advanceTimersByTime(1_800));

    act(() => usePlayerStore.getState().pause());
    act(() => vi.advanceTimersByTime(2_000));

    expect(usePlayerStore.getState()).toMatchObject({
      isPlaying: false,
      isStalled: false,
      playbackError: null,
    });
  });

  it('cancels pending buffering when resume or retry initializes a later attempt', async () => {
    vi.useFakeTimers();
    usePlayerStore.setState({ currentTrack: firstTrack, isPlaying: true });
    act(() => audio.dispatch('waiting'));
    act(() => vi.advanceTimersByTime(1_800));

    await act(async () => {
      usePlayerStore.getState().resume();
      await Promise.resolve();
    });
    act(() => vi.advanceTimersByTime(2_000));

    expect(usePlayerStore.getState()).toMatchObject({
      isPlaying: true,
      isStalled: false,
      playbackError: null,
    });
  });

  it('cancels pending buffering on track change', async () => {
    vi.useFakeTimers();
    usePlayerStore.setState({ currentTrack: firstTrack, isPlaying: true });
    act(() => audio.dispatch('waiting'));
    act(() => vi.advanceTimersByTime(1_800));

    await act(async () => {
      usePlayerStore.getState().play(secondTrack);
      await Promise.resolve();
    });
    act(() => vi.advanceTimersByTime(2_000));

    expect(usePlayerStore.getState()).toMatchObject({
      currentTrack: secondTrack,
      isPlaying: true,
      isStalled: false,
      playbackError: null,
    });
  });

  it('cancels pending buffering on media error and keeps it as a real error', () => {
    vi.useFakeTimers();
    usePlayerStore.setState({ currentTrack: firstTrack, isPlaying: true });
    act(() => audio.dispatch('waiting'));
    act(() => vi.advanceTimersByTime(1_800));

    act(() => audio.dispatch('error'));
    act(() => vi.advanceTimersByTime(2_000));

    expect(usePlayerStore.getState()).toMatchObject({
      isPlaying: false,
      isStalled: false,
      playbackError: MEDIA_ERROR,
    });
  });

  it('cancels pending buffering on stop and clears playback state', () => {
    vi.useFakeTimers();
    usePlayerStore.setState({
      currentTrack: firstTrack,
      queue: [firstTrack],
      isPlaying: true,
    });
    act(() => audio.dispatch('waiting'));
    act(() => vi.advanceTimersByTime(1_800));

    act(() => usePlayerStore.getState().clearQueue());
    act(() => vi.advanceTimersByTime(2_000));

    expect(usePlayerStore.getState()).toMatchObject({
      currentTrack: null,
      queue: [],
      isPlaying: false,
      isStalled: false,
      playbackError: null,
    });
  });

  it('cancels pending buffering when play rejects and never reports it as stalled', async () => {
    vi.useFakeTimers();
    let rejectPlay: ((reason?: unknown) => void) | undefined;
    const pendingPlay = new Promise<void>((_resolve, reject) => {
      rejectPlay = reject;
    });
    audio.play.mockReturnValueOnce(pendingPlay);

    act(() => usePlayerStore.getState().play(firstTrack));
    act(() => audio.dispatch('waiting'));
    act(() => vi.advanceTimersByTime(1_800));
    await act(async () => {
      rejectPlay?.(new Error('network unavailable'));
      await pendingPlay.catch(() => undefined);
    });
    act(() => vi.advanceTimersByTime(2_000));

    expect(usePlayerStore.getState()).toMatchObject({
      isPlaying: false,
      isStalled: false,
      playbackError: PLAYBACK_ERROR,
    });
  });

  it('fences an uncancelled timer after pause and a later playback attempt', async () => {
    vi.useFakeTimers();
    usePlayerStore.setState({ currentTrack: firstTrack, isPlaying: true });
    act(() => audio.dispatch('waiting'));
    vi.spyOn(globalThis, 'clearTimeout').mockImplementation(() => undefined);

    act(() => usePlayerStore.getState().pause());
    await act(async () => {
      usePlayerStore.getState().resume();
      await Promise.resolve();
    });
    act(() => vi.advanceTimersByTime(2_000));

    expect(usePlayerStore.getState()).toMatchObject({
      currentTrack: firstTrack,
      isPlaying: true,
      isStalled: false,
      playbackError: null,
    });
  });

  it('fences an old Track timer and stale rejection after a new Track starts', async () => {
    vi.useFakeTimers();
    let rejectFirstPlay: ((reason?: unknown) => void) | undefined;
    const firstPlay = new Promise<void>((_resolve, reject) => {
      rejectFirstPlay = reject;
    });
    audio.play.mockReturnValueOnce(firstPlay).mockResolvedValueOnce(undefined);

    act(() => usePlayerStore.getState().play(firstTrack));
    act(() => audio.dispatch('waiting'));
    vi.spyOn(globalThis, 'clearTimeout').mockImplementation(() => undefined);

    await act(async () => {
      usePlayerStore.getState().play(secondTrack);
      await Promise.resolve();
    });
    await act(async () => {
      rejectFirstPlay?.(new Error('stale rejection'));
      await firstPlay.catch(() => undefined);
    });
    act(() => vi.advanceTimersByTime(4_000));

    expect(usePlayerStore.getState()).toMatchObject({
      currentTrack: secondTrack,
      isPlaying: true,
      isStalled: false,
      playbackError: null,
    });
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

  it('preserves queue, shuffle, and repeat mode behavior', async () => {
    usePlayerStore.setState({
      currentTrack: firstTrack,
      queue: [firstTrack, secondTrack],
      trackListContext: [],
    });

    act(() => usePlayerStore.getState().toggleShuffle());
    expect(usePlayerStore.getState().shuffle).toBe(true);

    const randomSpy = vi.spyOn(Math, 'random').mockReturnValue(0);
    act(() => usePlayerStore.getState().next());
    await vi.waitFor(() => expect(usePlayerStore.getState().currentTrack?.id).toBe(2));
    randomSpy.mockRestore();

    expect(usePlayerStore.getState().repeat).toBe('off');
    act(() => usePlayerStore.getState().cycleRepeat());
    expect(usePlayerStore.getState().repeat).toBe('all');
    act(() => usePlayerStore.getState().cycleRepeat());
    expect(usePlayerStore.getState().repeat).toBe('one');
    act(() => usePlayerStore.getState().cycleRepeat());
    expect(usePlayerStore.getState().repeat).toBe('off');

    act(() => usePlayerStore.getState().reorderQueue(1, 0));
    expect(usePlayerStore.getState().queue.map((item) => item.id)).toEqual([2, 1]);
    act(() => usePlayerStore.getState().removeFromQueue(1));
    expect(usePlayerStore.getState().queue.map((item) => item.id)).toEqual([2]);
  });

  it('plays a complete list, ignores duplicates, and clears playback state', async () => {
    act(() => usePlayerStore.getState().playAll([]));
    expect(audio.play).not.toHaveBeenCalled();
    act(() => usePlayerStore.getState().playAll([firstTrack, secondTrack]));
    await vi.waitFor(() => expect(usePlayerStore.getState().currentTrack?.id).toBe(1));
    expect(usePlayerStore.getState().queue.map((track) => track.id)).toEqual([1, 2]);

    act(() => usePlayerStore.getState().addToQueue(secondTrack));
    expect(usePlayerStore.getState().queue).toHaveLength(2);
    const thirdTrack = { ...firstTrack, id: 3, title: 'Third track' };
    act(() => usePlayerStore.getState().addToQueue(thirdTrack));
    expect(usePlayerStore.getState().queue.map((track) => track.id)).toEqual([1, 2, 3]);

    act(() => usePlayerStore.getState().clearQueue());
    expect(audio.pause).toHaveBeenCalled();
    expect(audio.src).toBe('');
    expect(usePlayerStore.getState()).toMatchObject({
      currentTrack: null,
      queue: [],
      isPlaying: false,
      currentTime: 0,
      duration: 0,
    });
  });

  it('clamps volume and restores an audible level when unmuting zero volume', () => {
    act(() => usePlayerStore.getState().setVolume(2));
    expect(audio.volume).toBe(1);
    expect(localStorage.getItem('playerVolume')).toBe('1');
    act(() => usePlayerStore.getState().setVolume(-1));
    expect(audio.volume).toBe(0);
    act(() => usePlayerStore.getState().toggleMute());
    expect(audio.muted).toBe(true);
    act(() => usePlayerStore.getState().toggleMute());
    expect(audio.muted).toBe(false);
    expect(audio.volume).toBe(0.5);
    expect(usePlayerStore.getState().volume).toBe(0.5);
  });

  it('navigates backward through time, visible context, and queue wrapping', async () => {
    usePlayerStore.setState({
      currentTrack: secondTrack,
      queue: [firstTrack, secondTrack],
      trackListContext: [firstTrack, secondTrack],
    });
    audio.currentTime = 10;
    act(() => usePlayerStore.getState().prev());
    expect(audio.currentTime).toBe(0);
    expect(usePlayerStore.getState().currentTrack?.id).toBe(2);

    act(() => usePlayerStore.getState().prev());
    await vi.waitFor(() => expect(usePlayerStore.getState().currentTrack?.id).toBe(1));
    audio.currentTime = 0;
    act(() => usePlayerStore.getState().prev());
    expect(audio.currentTime).toBe(0);

    usePlayerStore.setState({
      currentTrack: firstTrack,
      queue: [firstTrack, secondTrack],
      trackListContext: [],
    });
    act(() => usePlayerStore.getState().prev());
    await vi.waitFor(() => expect(usePlayerStore.getState().currentTrack?.id).toBe(2));
    usePlayerStore.setState({ queue: [] });
    act(() => usePlayerStore.getState().prev());
    expect(usePlayerStore.getState().currentTrack?.id).toBe(2);
  });

  it('advances through visible context and wraps repeat-all queues', async () => {
    usePlayerStore.setState({
      currentTrack: firstTrack,
      queue: [firstTrack, secondTrack],
      trackListContext: [firstTrack, secondTrack],
    });
    act(() => usePlayerStore.getState().next());
    await vi.waitFor(() => expect(usePlayerStore.getState().currentTrack?.id).toBe(2));

    usePlayerStore.setState({
      currentTrack: secondTrack,
      queue: [firstTrack, secondTrack],
      trackListContext: [],
      repeat: 'all',
    });
    act(() => usePlayerStore.getState().next());
    await vi.waitFor(() => expect(usePlayerStore.getState().currentTrack?.id).toBe(1));

    usePlayerStore.setState({
      currentTrack: firstTrack,
      queue: [firstTrack],
      shuffle: true,
      repeat: 'off',
    });
    act(() => usePlayerStore.getState().next());
    await vi.waitFor(() => expect(usePlayerStore.getState().currentTrack?.id).toBe(1));
  });

  it('ignores ended events while a seek is settling', () => {
    vi.useFakeTimers();
    usePlayerStore.setState({
      currentTrack: firstTrack,
      queue: [firstTrack, secondTrack],
    });
    act(() => usePlayerStore.getState().seek(40));
    act(() => audio.dispatch('ended'));
    expect(usePlayerStore.getState().currentTrack?.id).toBe(1);
    act(() => vi.advanceTimersByTime(100));
    vi.useRealTimers();
  });

  it('reports a synchronous browser play failure', () => {
    audio.play.mockImplementationOnce(() => {
      throw new Error('synchronous failure');
    });
    act(() => usePlayerStore.getState().play(firstTrack));
    expect(usePlayerStore.getState()).toMatchObject({
      isPlaying: false,
      playbackError: PLAYBACK_ERROR,
    });
  });
});

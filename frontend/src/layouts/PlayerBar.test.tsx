import { render } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import PlayerBar from '@/layouts/PlayerBar';

const mocks = vi.hoisted(() => ({
  toastShow: vi.fn(),
  playerState: {
    currentTrack: null,
    isPlaying: false,
    playbackError: null as string | null,
    currentTime: 0,
    duration: 0,
    volume: 1,
    muted: false,
    shuffle: false,
    repeat: 'off',
    pause: vi.fn(),
    resume: vi.fn(),
    next: vi.fn(),
    prev: vi.fn(),
    seek: vi.fn(),
    setVolume: vi.fn(),
    toggleMute: vi.fn(),
    toggleShuffle: vi.fn(),
    cycleRepeat: vi.fn(),
  },
}));

vi.mock('@/store/playerStore', () => ({
  usePlayerStore: (selector: (state: typeof mocks.playerState) => unknown) =>
    selector(mocks.playerState),
}));

vi.mock('@/store/authStore', () => ({
  useAuthStore: (selector: (state: { isAuthenticated: () => boolean; role: string }) => unknown) =>
    selector({ isAuthenticated: () => false, role: 'GUEST' }),
}));

vi.mock('@/store/likeStore', () => ({
  useLikeStore: (
    selector: (state: {
      loaded: boolean;
      load: () => Promise<void>;
      likedIds: Set<number>;
      toggle: () => Promise<void>;
    }) => unknown,
  ) =>
    selector({
      loaded: false,
      load: vi.fn().mockResolvedValue(undefined),
      likedIds: new Set<number>(),
      toggle: vi.fn().mockResolvedValue(undefined),
    }),
}));

vi.mock('@/store/toastStore', () => ({
  useToastStore: (selector: (state: { show: typeof mocks.toastShow }) => unknown) =>
    selector({ show: mocks.toastShow }),
}));

vi.mock('@/api/userSubscriptions', () => ({
  fetchMySubscription: vi.fn(),
}));

describe('PlayerBar playback feedback', () => {
  beforeEach(() => {
    mocks.toastShow.mockReset();
    mocks.playerState.playbackError = null;
  });

  it('shows the Korean playback failure from the player store', () => {
    const message = '재생을 시작할 수 없습니다. 잠시 후 다시 시도해 주세요.';
    mocks.playerState.playbackError = message;

    render(
      <MemoryRouter>
        <PlayerBar />
      </MemoryRouter>,
    );

    expect(mocks.toastShow).toHaveBeenCalledWith('error', message);
  });
});

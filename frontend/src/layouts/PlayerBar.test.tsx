import { act, fireEvent, render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import PlayerBar from '@/layouts/PlayerBar';
import type { Track } from '@/types';

const track: Track = {
  id: 1,
  title: 'First track',
  artistName: 'Artist',
  duration: 100,
  bpm: 120,
  tonality: 'C',
  description: null,
  audioFile: null,
  thumbnail: null,
  waveformData: '[0.2,0.8]',
  tags: [],
  isActive: true,
  playCount: 0,
  likeCount: 0,
  downloadCount: 0,
  createdAt: '2026-07-15T00:00:00Z',
  updatedAt: '2026-07-15T00:00:00Z',
};

const mocks = vi.hoisted(() => ({
  toastShow: vi.fn(),
  fetchMySubscription: vi.fn(),
  authState: {
    authenticated: false,
    role: 'GUEST',
    accessToken: null as string | null,
    user: null as { id: number } | null,
  },
  playerState: {
    currentTrack: null as Track | null,
    isPlaying: false,
    isStalled: false,
    playbackError: null as string | null,
    currentTime: 10,
    duration: 100,
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
  useAuthStore: (
    selector: (state: {
      isAuthenticated: () => boolean;
      role: string;
      accessToken: string | null;
      user: { id: number } | null;
    }) => unknown,
  ) =>
    selector({
      isAuthenticated: () => mocks.authState.authenticated,
      role: mocks.authState.role,
      accessToken: mocks.authState.accessToken,
      user: mocks.authState.user,
    }),
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
  fetchMySubscription: (...args: unknown[]) => mocks.fetchMySubscription(...args),
  isNoActiveSubscriptionError: (error: unknown) => {
    const shaped = error as { response?: { status?: number; data?: { errorCode?: string } } };
    return (
      shaped.response?.status === 403 &&
      shaped.response?.data?.errorCode === 'NO_ACTIVE_SUBSCRIPTION'
    );
  },
}));

vi.mock('@/components/player/WaveformCanvas', () => ({
  default: () => <div data-testid="waveform" />,
}));

vi.mock('@/components/player/HistoryModal', () => ({
  default: () => null,
}));

vi.mock('@/components/player/PlaylistDrawer', () => ({
  default: () => null,
}));

vi.mock('@/components/playlist/AddToPlaylistModal', () => ({
  default: () => null,
}));

function renderPlayerBar() {
  return render(
    <MemoryRouter>
      <PlayerBar />
    </MemoryRouter>,
  );
}

function deferred<T>() {
  let resolve!: (value: T) => void;
  let reject!: (reason?: unknown) => void;
  const promise = new Promise<T>((nextResolve, nextReject) => {
    resolve = nextResolve;
    reject = nextReject;
  });
  return { promise, resolve, reject };
}

describe('PlayerBar playback feedback', () => {
  beforeEach(() => {
    mocks.toastShow.mockReset();
    mocks.fetchMySubscription.mockReset();
    mocks.authState.authenticated = false;
    mocks.authState.role = 'GUEST';
    mocks.authState.accessToken = null;
    mocks.authState.user = null;
    mocks.playerState.currentTrack = track;
    mocks.playerState.isPlaying = false;
    mocks.playerState.isStalled = false;
    mocks.playerState.playbackError = null;
    mocks.playerState.currentTime = 10;
    mocks.playerState.duration = 100;
    mocks.playerState.shuffle = false;
    mocks.playerState.repeat = 'off';
    Object.values(mocks.playerState).forEach((value) => {
      if (typeof value === 'function' && 'mockClear' in value) {
        value.mockClear();
      }
    });
  });

  it('shows the Korean playback failure from the player store', () => {
    const message = '재생을 시작할 수 없습니다. 잠시 후 다시 시도해 주세요.';
    mocks.playerState.playbackError = message;

    renderPlayerBar();

    expect(mocks.toastShow).not.toHaveBeenCalled();
    expect(screen.getByRole('alert')).toHaveTextContent(message);

    fireEvent.click(screen.getByRole('button', { name: '다시 시도' }));
    expect(mocks.playerState.resume).toHaveBeenCalledTimes(1);
  });

  it('shows stalled playback as a retryable non-fatal status', () => {
    mocks.playerState.isStalled = true;

    renderPlayerBar();

    expect(screen.getByRole('status')).toHaveTextContent('재생이 지연되고 있습니다.');
    fireEvent.click(screen.getByRole('button', { name: '다시 시도' }));
    expect(mocks.playerState.resume).toHaveBeenCalledTimes(1);
  });

  it('exposes native named controls and keyboard seeking', () => {
    renderPlayerBar();

    expect(screen.getAllByRole('button', { name: 'First track 상세 보기' })[0].tagName).toBe(
      'BUTTON',
    );
    expect(screen.getAllByRole('button', { name: '재생' }).length).toBeGreaterThan(0);
    expect(screen.getAllByRole('button', { name: '이전 곡' }).length).toBeGreaterThan(0);
    expect(screen.getAllByRole('button', { name: '다음 곡' }).length).toBeGreaterThan(0);
    expect(screen.getAllByRole('button', { name: '셔플 사용 안 함' })[0]).toHaveAttribute(
      'aria-pressed',
      'false',
    );

    const seekControl = screen.getByRole('slider', { name: '재생 위치' });
    seekControl.focus();
    fireEvent.keyDown(seekControl, { key: 'ArrowRight' });
    expect(mocks.playerState.seek).toHaveBeenCalledWith(15);
  });

  it('keeps focus on the mobile expand control while responsive controls change', () => {
    renderPlayerBar();
    const expandButton = screen.getByLabelText('플레이어 상세 펼치기');

    expandButton.focus();
    fireEvent.click(expandButton);

    const collapseButton = screen.getByLabelText('플레이어 상세 접기');
    expect(collapseButton).toHaveAttribute('aria-expanded', 'true');
    expect(collapseButton).toHaveFocus();
  });

  it('enables subscriber actions only after an active subscription response', async () => {
    mocks.authState.authenticated = true;
    mocks.authState.role = 'USER';
    mocks.authState.accessToken = 'token-a';
    mocks.authState.user = { id: 1 };
    mocks.fetchMySubscription.mockResolvedValue({ id: 1 });

    renderPlayerBar();

    expect(await screen.findAllByTitle('음원 다운로드')).not.toHaveLength(0);
    expect(screen.queryByRole('button', { name: '구독하기' })).not.toBeInTheDocument();
  });

  it('treats only the structured no-active-subscription response as inactive', async () => {
    mocks.authState.authenticated = true;
    mocks.authState.role = 'USER';
    mocks.authState.accessToken = 'token-a';
    mocks.authState.user = { id: 1 };
    mocks.fetchMySubscription.mockRejectedValue({
      response: { status: 403, data: { errorCode: 'NO_ACTIVE_SUBSCRIPTION' } },
    });

    renderPlayerBar();

    expect(await screen.findAllByRole('button', { name: '구독하기' })).not.toHaveLength(0);
    expect(screen.queryByRole('button', { name: '구독 상태 다시 확인' })).not.toBeInTheDocument();
  });

  it.each([
    ['server', { response: { status: 503 } }],
    ['offline', { code: 'ERR_NETWORK' }],
  ])('keeps %s failures distinct from an inactive subscription', async (_label, failure) => {
    mocks.authState.authenticated = true;
    mocks.authState.role = 'USER';
    mocks.authState.accessToken = 'token-a';
    mocks.authState.user = { id: 1 };
    mocks.fetchMySubscription.mockRejectedValue(failure);

    renderPlayerBar();

    expect(await screen.findAllByRole('button', { name: '구독 상태 다시 확인' })).not.toHaveLength(
      0,
    );
    expect(screen.queryByRole('button', { name: '구독하기' })).not.toBeInTheDocument();
  });

  it('retries a failed subscription check without duplicating the inactive CTA', async () => {
    mocks.authState.authenticated = true;
    mocks.authState.role = 'USER';
    mocks.authState.accessToken = 'token-a';
    mocks.authState.user = { id: 1 };
    mocks.fetchMySubscription
      .mockRejectedValueOnce({ response: { status: 503 } })
      .mockResolvedValueOnce({ id: 1 });

    renderPlayerBar();
    fireEvent.click((await screen.findAllByRole('button', { name: '구독 상태 다시 확인' }))[0]);

    await waitFor(() => expect(mocks.fetchMySubscription).toHaveBeenCalledTimes(2));
    expect(await screen.findAllByTitle('음원 다운로드')).not.toHaveLength(0);
    expect(screen.queryByRole('button', { name: '구독하기' })).not.toBeInTheDocument();
  });

  it('ignores an out-of-order subscription response after the authenticated user changes', async () => {
    const first = deferred<{ id: number }>();
    const second = deferred<never>();
    mocks.authState.authenticated = true;
    mocks.authState.role = 'USER';
    mocks.authState.accessToken = 'token-a';
    mocks.authState.user = { id: 1 };
    mocks.fetchMySubscription
      .mockReturnValueOnce(first.promise)
      .mockReturnValueOnce(second.promise);

    const view = renderPlayerBar();
    mocks.authState.accessToken = 'token-b';
    mocks.authState.user = { id: 2 };
    view.rerender(
      <MemoryRouter>
        <PlayerBar />
      </MemoryRouter>,
    );

    await act(async () => {
      second.reject({
        response: { status: 403, data: { errorCode: 'NO_ACTIVE_SUBSCRIPTION' } },
      });
    });
    expect(await screen.findAllByRole('button', { name: '구독하기' })).not.toHaveLength(0);

    await act(async () => first.resolve({ id: 1 }));
    expect(screen.queryByTitle('음원 다운로드')).not.toBeInTheDocument();
    expect(screen.getAllByRole('button', { name: '구독하기' })).not.toHaveLength(0);
  });

  it('aborts and fences a subscription request when authentication is cleared', async () => {
    const pending = deferred<{ id: number }>();
    mocks.authState.authenticated = true;
    mocks.authState.role = 'USER';
    mocks.authState.accessToken = 'token-a';
    mocks.authState.user = { id: 1 };
    mocks.fetchMySubscription.mockReturnValue(pending.promise);

    const view = renderPlayerBar();
    const signal = mocks.fetchMySubscription.mock.calls[0][0] as AbortSignal;
    mocks.authState.authenticated = false;
    mocks.authState.role = 'GUEST';
    mocks.authState.accessToken = null;
    mocks.authState.user = null;
    view.rerender(
      <MemoryRouter>
        <PlayerBar />
      </MemoryRouter>,
    );

    expect(signal.aborted).toBe(true);
    await act(async () => pending.resolve({ id: 1 }));
    expect(screen.queryByTitle('음원 다운로드')).not.toBeInTheDocument();
    expect(screen.getAllByRole('button', { name: '구독하기' })).not.toHaveLength(0);
  });
});

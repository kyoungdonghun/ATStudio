import { act, fireEvent, render, screen, waitFor, within } from '@testing-library/react';
import { MemoryRouter, Route, Routes, useLocation } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import PlayerBar from '@/layouts/PlayerBar';
import Header from '@/layouts/Header';
import type { BinaryDownload } from '@/api/downloads';
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
  downloadTrack: vi.fn(),
  triggerBlobDownload: vi.fn(),
  getApiErrorCode: vi.fn(),
  historyBusy: false,
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

vi.mock('@/api/downloads', () => ({
  createDownloadFallbackFileName: () => 'fallback-track.mp3',
  downloadTrack: (...args: unknown[]) => mocks.downloadTrack(...args),
  triggerBlobDownload: (...args: unknown[]) => mocks.triggerBlobDownload(...args),
}));

vi.mock('@/api/client', () => ({
  getApiErrorCode: (...args: unknown[]) => mocks.getApiErrorCode(...args),
  toUploadUrl: (path: string | null | undefined) => path,
}));

vi.mock('@/components/player/WaveformCanvas', () => ({
  default: ({ progress, onSeek }: { progress: number; onSeek: (ratio: number) => void }) => (
    <div data-testid="waveform" data-progress={progress} onClick={() => onSeek(0.5)} />
  ),
}));

vi.mock('@/components/player/HistoryModal', async () => {
  const { default: Modal } =
    await vi.importActual<typeof import('@/components/ui/Modal')>('@/components/ui/Modal');

  return {
    default: ({ open, onClose }: { open: boolean; onClose: () => void }) => (
      <Modal open={open} onClose={onClose} title="Player history" busy={mocks.historyBusy}>
        <button type="button">History action</button>
      </Modal>
    ),
  };
});

vi.mock('@/components/player/PlaylistDrawer', () => ({
  default: ({ open, requestedTab }: { open: boolean; requestedTab?: 'playlists' | 'likes' }) =>
    open ? <div data-testid="playlist-drawer-tab">{requestedTab}</div> : null,
}));

vi.mock('@/components/playlist/AddToPlaylistModal', () => ({
  default: () => null,
}));

function LocationProbe() {
  const location = useLocation();
  return <div data-testid="location">{`${location.pathname}${location.search}`}</div>;
}

function renderPlayerBar(initialEntry = '/') {
  return render(
    <MemoryRouter initialEntries={[initialEntry]}>
      <Routes>
        <Route
          path="*"
          element={
            <>
              <PlayerBar />
              <LocationProbe />
            </>
          }
        />
      </Routes>
    </MemoryRouter>,
  );
}

function renderPublicShell() {
  return render(
    <MemoryRouter>
      <Header />
      <PlayerBar />
    </MemoryRouter>,
  );
}

function getMobileExpander(container: HTMLElement): HTMLButtonElement {
  const expander = Array.from(container.querySelectorAll<HTMLButtonElement>('button')).find(
    (button) => button.textContent === '\u25B2' || button.textContent === '\u25BC',
  );

  expect(expander).toBeDefined();
  return expander!;
}

function getMobilePlayer(expander: HTMLButtonElement): HTMLElement {
  const mobilePlayer = expander.parentElement?.parentElement?.parentElement;

  expect(mobilePlayer).toBeInstanceOf(HTMLElement);
  return mobilePlayer!;
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

function binaryDownload(): BinaryDownload {
  return {
    blob: new Blob(['audio'], { type: 'audio/mpeg' }),
    fileName: 'server-track.mp3',
    contentType: 'audio/mpeg',
  };
}

function rapidlyActivate(button: HTMLElement) {
  act(() => {
    button.dispatchEvent(new MouseEvent('click', { bubbles: true }));
    button.dispatchEvent(new MouseEvent('click', { bubbles: true }));
  });
}

describe('PlayerBar playback feedback', () => {
  beforeEach(() => {
    mocks.toastShow.mockReset();
    mocks.fetchMySubscription.mockReset();
    mocks.downloadTrack.mockReset();
    mocks.triggerBlobDownload.mockReset();
    mocks.getApiErrorCode.mockReset();
    mocks.historyBusy = false;
    mocks.downloadTrack.mockResolvedValue(binaryDownload());
    mocks.getApiErrorCode.mockResolvedValue(undefined);
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

  it('renders no buffering feedback while the store remains pending', () => {
    renderPlayerBar();

    expect(screen.queryByText(/재생이 지연되고 있습니다/)).not.toBeInTheDocument();
    expect(screen.queryByRole('status')).not.toBeInTheDocument();
    expect(screen.queryByRole('alert')).not.toBeInTheDocument();
  });

  it('shows the Korean playback failure from the player store', () => {
    const message = '재생을 시작할 수 없습니다. 잠시 후 다시 시도해 주세요.';
    mocks.playerState.playbackError = message;

    renderPlayerBar();

    expect(mocks.toastShow).not.toHaveBeenCalled();
    expect(screen.getByRole('alert')).toHaveAttribute('aria-live', 'assertive');
    expect(screen.getByRole('alert')).toHaveTextContent(message);
    expect(screen.queryByRole('status')).not.toBeInTheDocument();

    fireEvent.click(screen.getByRole('button', { name: '다시 시도' }));
    expect(mocks.playerState.resume).toHaveBeenCalledTimes(1);
  });

  it('shows stalled playback as a retryable non-fatal status', () => {
    mocks.playerState.isStalled = true;

    renderPlayerBar();

    expect(screen.getByRole('status')).toHaveAttribute('aria-live', 'polite');
    expect(screen.getByRole('status')).toHaveTextContent('재생이 지연되고 있습니다.');
    expect(screen.queryByRole('alert')).not.toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: '다시 시도' }));
    expect(mocks.playerState.resume).toHaveBeenCalledTimes(1);
  });

  it('keeps a real playback error authoritative over stalled state', () => {
    const message = '오디오를 재생하는 중 오류가 발생했습니다.';
    mocks.playerState.isStalled = true;
    mocks.playerState.playbackError = message;

    renderPlayerBar();

    expect(screen.getByRole('alert')).toHaveTextContent(message);
    expect(screen.queryByRole('status')).not.toBeInTheDocument();
    expect(screen.queryByText(/재생이 지연되고 있습니다/)).not.toBeInTheDocument();
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

  it('drops the prior progress, ARIA maximum, and seek scale as soon as the track switches', () => {
    mocks.playerState.currentTime = 50;
    mocks.playerState.duration = 100;
    const view = renderPlayerBar();
    let seekControl = screen.getByRole('slider', { name: '재생 위치' });

    expect(within(seekControl).getByTestId('waveform')).toHaveAttribute('data-progress', '0.5');
    expect(seekControl).toHaveAttribute('aria-valuemax', '100');

    mocks.playerState.currentTrack = { ...track, id: 2, title: 'Second track', duration: 45 };
    mocks.playerState.currentTime = 0;
    mocks.playerState.duration = 45;
    view.rerender(
      <MemoryRouter>
        <PlayerBar />
      </MemoryRouter>,
    );

    seekControl = screen.getByRole('slider', { name: '재생 위치' });
    expect(screen.queryByText('1:40')).not.toBeInTheDocument();
    expect(screen.getAllByText('0:45').length).toBeGreaterThan(0);
    expect(within(seekControl).getByTestId('waveform')).toHaveAttribute('data-progress', '0');
    expect(seekControl).toHaveAttribute('aria-valuemax', '45');
    expect(seekControl).toHaveAttribute('aria-valuenow', '0');
    expect(seekControl).toHaveAttribute('aria-valuetext', '0:00 / 0:45');

    fireEvent.click(within(seekControl).getByTestId('waveform'));
    expect(mocks.playerState.seek).toHaveBeenCalledWith(22.5);
    fireEvent.keyDown(seekControl, { key: 'End' });
    expect(mocks.playerState.seek).toHaveBeenLastCalledWith(45);
  });

  it.each([
    ['negative', -20, '0'],
    ['NaN', Number.NaN, '0'],
    ['infinite', Number.POSITIVE_INFINITY, '0'],
    ['past duration', 250, '1'],
  ])('renders %s progress inside the current Track bounds', (_label, currentTime, progress) => {
    mocks.playerState.currentTime = currentTime;
    mocks.playerState.duration = 100;
    renderPlayerBar();

    const seekControl = screen.getByRole('slider', { name: '재생 위치' });
    expect(within(seekControl).getByTestId('waveform')).toHaveAttribute('data-progress', progress);
    expect(seekControl).toHaveAttribute('aria-valuenow', progress === '1' ? '100' : '0');
    expect(seekControl).toHaveAttribute(
      'aria-valuetext',
      progress === '1' ? '1:40 / 1:40' : '0:00 / 1:40',
    );
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

  it('keeps collapsed mobile detail controls and their relationship out of the DOM', () => {
    const view = renderPlayerBar();
    const expander = getMobileExpander(view.container);

    expect(expander).toHaveAttribute('aria-expanded', 'false');
    expect(expander).not.toHaveAttribute('aria-controls');
    expect(
      view.container.querySelector('#player-mobile-expanded-controls'),
    ).not.toBeInTheDocument();
  });

  it('renders the mobile detail panel and a truthful expander relationship only while open', () => {
    const view = renderPlayerBar();
    const expander = getMobileExpander(view.container);

    fireEvent.click(expander);

    const panel = view.container.querySelector('#player-mobile-expanded-controls');
    expect(panel).toBeInTheDocument();
    expect(expander).toHaveAttribute('aria-expanded', 'true');
    expect(expander).toHaveAttribute('aria-controls', 'player-mobile-expanded-controls');
    expect(
      within(panel as HTMLElement).getAllByRole('button', { hidden: true }).length,
    ).toBeGreaterThan(0);
  });

  it('collapses mobile details on Escape and restores the exact expander focus', () => {
    const view = renderPlayerBar();
    const expander = getMobileExpander(view.container);
    fireEvent.click(expander);
    const panel = view.container.querySelector('#player-mobile-expanded-controls') as HTMLElement;
    const detailButton = within(panel).getAllByRole('button', { hidden: true })[0];
    detailButton.focus();

    fireEvent.keyDown(detailButton, { key: 'Escape' });

    expect(
      view.container.querySelector('#player-mobile-expanded-controls'),
    ).not.toBeInTheDocument();
    expect(expander).toHaveAttribute('aria-expanded', 'false');
    expect(expander).not.toHaveAttribute('aria-controls');
    expect(expander).toHaveFocus();
  });

  it('leaves expanded PlayerBar details open when Header owns Escape', () => {
    const view = renderPublicShell();
    const expander = getMobileExpander(view.container);
    fireEvent.click(expander);
    fireEvent.click(screen.getByLabelText('메뉴 열기'));
    const headerSearch = screen.getByLabelText('모바일 곡 제목 및 용도 검색');
    headerSearch.focus();

    fireEvent.keyDown(headerSearch, { key: 'Escape' });

    expect(screen.queryByRole('search', { name: '모바일 곡 검색' })).not.toBeInTheDocument();
    expect(view.container.querySelector('#player-mobile-expanded-controls')).toBeInTheDocument();
    expect(expander).toHaveAttribute('aria-expanded', 'true');
    expect(screen.getByLabelText('메뉴 열기')).toHaveFocus();
  });

  it.each([
    ['non-busy', false],
    ['busy', true],
  ])('lets a %s Modal own Escape above expanded PlayerBar details', (_label, busy) => {
    mocks.historyBusy = busy;
    const view = renderPlayerBar();
    const expander = getMobileExpander(view.container);
    fireEvent.click(expander);
    const panel = view.container.querySelector('#player-mobile-expanded-controls') as HTMLElement;
    const historyOpener = within(panel).getByRole('button', { name: '재생기록', hidden: true });
    historyOpener.focus();
    fireEvent.click(historyOpener);
    const dialog = screen.getByRole('dialog', { name: 'Player history' });

    fireEvent.keyDown(within(dialog).getByRole('button', { name: 'History action' }), {
      key: 'Escape',
    });

    if (busy) {
      expect(screen.getByRole('dialog', { name: 'Player history' })).toBeInTheDocument();
    } else {
      expect(screen.queryByRole('dialog', { name: 'Player history' })).not.toBeInTheDocument();
      expect(historyOpener).toHaveFocus();
    }
    expect(view.container.querySelector('#player-mobile-expanded-controls')).toBeInTheDocument();
    expect(expander).toHaveAttribute('aria-expanded', 'true');
  });

  it('preserves mobile mini-bar and expanded playback and seek controls', () => {
    const view = renderPlayerBar();
    const expander = getMobileExpander(view.container);
    const mobilePlayer = getMobilePlayer(expander);
    const collapsedPlayButton = within(mobilePlayer)
      .getAllByRole('button', { hidden: true })
      .find((button) => button.textContent === '\u25B6');
    const collapsedSeekControl = within(mobilePlayer).getAllByRole('slider', { hidden: true })[0];

    expect(collapsedPlayButton).toBeDefined();
    expect(collapsedSeekControl).toBeDefined();
    fireEvent.click(collapsedPlayButton!);
    fireEvent.keyDown(collapsedSeekControl!, { key: 'ArrowRight' });
    expect(mocks.playerState.resume).toHaveBeenCalledTimes(1);
    expect(mocks.playerState.seek).toHaveBeenLastCalledWith(15);

    fireEvent.click(expander);
    const panel = view.container.querySelector('#player-mobile-expanded-controls') as HTMLElement;
    const expandedPlayButton = within(panel)
      .getAllByRole('button', { hidden: true })
      .find((button) => button.textContent === '\u25B6');
    const expandedSeekControl = within(panel)
      .getAllByRole('slider', { hidden: true })
      .find((control) => control.getAttribute('aria-valuemax') === '100');

    expect(expandedPlayButton).toBeDefined();
    expect(expandedSeekControl).toBeDefined();
    fireEvent.click(expandedPlayButton!);
    fireEvent.keyDown(expandedSeekControl!, { key: 'End' });
    expect(mocks.playerState.resume).toHaveBeenCalledTimes(2);
    expect(mocks.playerState.seek).toHaveBeenLastCalledWith(100);
  });

  it('opens the existing drawer at requested tabs from desktop and mobile expanded actions', () => {
    const view = renderPlayerBar();

    fireEvent.click(screen.getByRole('button', { name: '좋아요 목록 열기' }));
    expect(screen.getByTestId('playlist-drawer-tab')).toHaveTextContent('likes');

    fireEvent.click(screen.getAllByRole('button', { name: '재생목록' })[0]);
    expect(screen.getByTestId('playlist-drawer-tab')).toHaveTextContent('playlists');

    fireEvent.click(screen.getByRole('button', { name: '좋아요 목록 열기' }));
    expect(screen.getByTestId('playlist-drawer-tab')).toHaveTextContent('likes');

    fireEvent.click(screen.getByRole('button', { name: '좋아요 목록 열기' }));
    expect(screen.queryByTestId('playlist-drawer-tab')).not.toBeInTheDocument();

    fireEvent.click(getMobileExpander(view.container));
    const panel = view.container.querySelector('#player-mobile-expanded-controls') as HTMLElement;
    fireEvent.click(within(panel).getByText('좋아요'));
    expect(screen.getByTestId('playlist-drawer-tab')).toHaveTextContent('likes');
  });

  it.each([
    ['desktop Like', 'button[aria-label="좋아요"]', 0],
    ['mobile Like', 'button[aria-label="좋아요"]', 1],
    ['Add to Playlist', 'button[aria-label="재생목록에 추가"]', 0],
  ])('preserves the safe current origin for guest %s', (_label, selector, index) => {
    const view = renderPlayerBar('/tracks/1?from=player&view=compact#ignored');

    const actions = view.container.querySelectorAll<HTMLButtonElement>(selector);
    fireEvent.click(actions[index]);

    expect(screen.getByTestId('location')).toHaveTextContent(
      '/login?returnTo=%2Ftracks%2F1%3Ffrom%3Dplayer%26view%3Dcompact',
    );
    view.unmount();
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

  it('fences rapid same-Track downloads and releases ownership after failure and success', async () => {
    const failed = deferred<BinaryDownload>();
    const succeeded = deferred<BinaryDownload>();
    mocks.authState.authenticated = true;
    mocks.authState.role = 'USER';
    mocks.authState.accessToken = 'token-a';
    mocks.authState.user = { id: 1 };
    mocks.fetchMySubscription.mockResolvedValue({ id: 1 });
    mocks.downloadTrack
      .mockReturnValueOnce(failed.promise)
      .mockReturnValueOnce(succeeded.promise)
      .mockResolvedValue(binaryDownload());

    renderPlayerBar();
    const downloadButton = (await screen.findAllByTitle('음원 다운로드'))[0];

    rapidlyActivate(downloadButton);
    expect(mocks.downloadTrack).toHaveBeenCalledTimes(1);

    await act(async () => failed.reject(new Error('download failed')));
    await waitFor(() => expect(downloadButton).toBeEnabled());

    rapidlyActivate(downloadButton);
    expect(mocks.downloadTrack).toHaveBeenCalledTimes(2);

    await act(async () => succeeded.resolve(binaryDownload()));
    await waitFor(() => expect(downloadButton).toBeEnabled());
    fireEvent.click(downloadButton);
    await waitFor(() => expect(mocks.downloadTrack).toHaveBeenCalledTimes(3));
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

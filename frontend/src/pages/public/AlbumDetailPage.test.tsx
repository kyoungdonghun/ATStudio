import { act, fireEvent, render, screen, waitFor, within } from '@testing-library/react';
import { StrictMode } from 'react';
import { MemoryRouter, Route, Routes, useNavigate } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import type { AlbumDetail } from '@/api/albums';
import AlbumDetailPage from '@/pages/public/AlbumDetailPage';
import type { Track } from '@/types';

const mocks = vi.hoisted(() => ({
  fetchAlbumDetail: vi.fn(),
  contextCleanup: vi.fn(),
  playerState: {
    currentTrack: null as Track | null,
    isPlaying: false,
    queue: [] as Track[],
    shuffle: false,
    repeat: 'off' as 'off' | 'all' | 'one',
    trackListContext: [] as Track[],
    play: vi.fn(),
    pause: vi.fn(),
    resume: vi.fn(),
    playAll: vi.fn(),
    setTrackListContext: vi.fn(),
  },
}));

vi.mock('@/api/albums', () => ({
  fetchAlbumDetail: (...args: unknown[]) => mocks.fetchAlbumDetail(...args),
}));

vi.mock('@/store/playerStore', () => ({
  usePlayerStore: (selector: (state: typeof mocks.playerState) => unknown) =>
    selector(mocks.playerState),
}));

vi.mock('@/store/authStore', () => ({
  useAuthStore: (selector: (state: { isAuthenticated: () => boolean }) => unknown) =>
    selector({ isAuthenticated: () => false }),
}));

vi.mock('@/store/likeStore', () => ({
  useLikeStore: (selector: (state: Record<string, unknown>) => unknown) =>
    selector({ loaded: true, load: vi.fn(), likedIds: new Set(), toggle: vi.fn() }),
}));

vi.mock('@/store/albumLikeStore', () => ({
  useAlbumLikeStore: (selector: (state: Record<string, unknown>) => unknown) =>
    selector({ loaded: true, load: vi.fn(), likedAlbumIds: new Set(), toggle: vi.fn() }),
}));

vi.mock('@/store/toastStore', () => ({
  useToastStore: (selector: (state: { show: ReturnType<typeof vi.fn> }) => unknown) =>
    selector({ show: vi.fn() }),
}));

vi.mock('@/components/playlist/AddToPlaylistModal', () => ({ default: () => null }));

const detail: AlbumDetail = {
  id: 7,
  title: 'Owned album',
  description: null,
  thumbnailUrl: null,
  likeCount: 0,
  createdAt: '2026-08-09T00:00:00',
  tracks: [
    {
      trackId: 11,
      title: 'Full track',
      artistName: 'AT.M',
      thumbnailUrl: null,
      waveformData: null,
      duration: 446,
      order: 0,
    },
  ],
};

function deferred<T>() {
  let resolve!: (value: T) => void;
  let reject!: (reason?: unknown) => void;
  const promise = new Promise<T>((nextResolve, nextReject) => {
    resolve = nextResolve;
    reject = nextReject;
  });
  return { promise, resolve, reject };
}

function RouteControls() {
  const navigate = useNavigate();
  return (
    <>
      <button type="button" onClick={() => navigate('/albums/8')}>
        next album
      </button>
      <button type="button" onClick={() => navigate('/')}>
        leave album
      </button>
    </>
  );
}

function renderPage(strict = false) {
  const page = (
    <MemoryRouter initialEntries={['/albums/7']}>
      <RouteControls />
      <Routes>
        <Route path="/albums/:albumId" element={<AlbumDetailPage />} />
        <Route path="/" element={<div>Home destination</div>} />
      </Routes>
    </MemoryRouter>
  );
  return render(strict ? <StrictMode>{page}</StrictMode> : page);
}

describe('AlbumDetailPage ownership and recovery', () => {
  beforeEach(() => {
    mocks.fetchAlbumDetail.mockReset();
    mocks.fetchAlbumDetail.mockResolvedValue(detail);
    mocks.contextCleanup.mockReset();
    mocks.playerState.setTrackListContext.mockReset();
    mocks.playerState.setTrackListContext.mockReturnValue(mocks.contextCleanup);
    mocks.playerState.play.mockReset();
    mocks.playerState.playAll.mockReset();
    mocks.playerState.currentTrack = null;
    mocks.playerState.isPlaying = false;
    mocks.playerState.queue = [];
    mocks.playerState.shuffle = false;
    mocks.playerState.repeat = 'off';
    mocks.playerState.trackListContext = [];
  });

  it('renders one-based display order and clears only its owned context on departure', async () => {
    renderPage();
    const trackLink = await screen.findByRole('link', { name: /Full track/ });
    const row = trackLink.closest('tr');
    expect(row).not.toBeNull();
    expect(within(row!).getAllByRole('cell')[2]).toHaveTextContent('1');
    await waitFor(() => {
      expect(mocks.playerState.setTrackListContext).toHaveBeenCalledWith([
        expect.objectContaining({ id: 11, duration: 446 }),
      ]);
    });

    fireEvent.click(screen.getByRole('button', { name: 'leave album' }));
    expect(await screen.findByText('Home destination')).toBeInTheDocument();
    expect(mocks.contextCleanup).toHaveBeenCalledTimes(1);
  });

  it('aborts an old route request and ignores its late success', async () => {
    const oldRequest = deferred<AlbumDetail>();
    const currentRequest = deferred<AlbumDetail>();
    mocks.fetchAlbumDetail
      .mockReturnValueOnce(oldRequest.promise)
      .mockReturnValueOnce(currentRequest.promise);
    renderPage();
    const oldSignal = mocks.fetchAlbumDetail.mock.calls[0][1] as AbortSignal;

    fireEvent.click(screen.getByRole('button', { name: 'next album' }));
    expect(oldSignal.aborted).toBe(true);
    await act(async () => currentRequest.resolve({ ...detail, id: 8, title: 'Current album' }));
    expect(await screen.findByRole('heading', { name: 'Current album' })).toBeInTheDocument();

    await act(async () => oldRequest.resolve(detail));
    expect(screen.queryByRole('heading', { name: detail.title })).not.toBeInTheDocument();
  });

  it('keeps the latest request and context owner through StrictMode cleanup', async () => {
    const staleRequest = deferred<AlbumDetail>();
    const latestRequest = deferred<AlbumDetail>();
    const durableTrack = {
      id: 99,
      title: 'Durable playback',
      artistName: 'AT.M',
      duration: 180,
      thumbnail: null,
      waveformData: null,
    } as Track;
    const latestDetail = {
      ...detail,
      title: 'Strict latest album',
      tracks: [{ ...detail.tracks[0], trackId: 12, title: 'Strict latest track' }],
    };
    const contextCleanups: Array<ReturnType<typeof vi.fn>> = [];
    let nextOwner = 0;
    let activeOwner = 0;

    mocks.playerState.currentTrack = durableTrack;
    mocks.playerState.queue = [durableTrack];
    mocks.playerState.shuffle = true;
    mocks.playerState.repeat = 'all';
    mocks.fetchAlbumDetail
      .mockReturnValueOnce(staleRequest.promise)
      .mockReturnValueOnce(latestRequest.promise);
    mocks.playerState.setTrackListContext.mockImplementation((tracks: Track[]) => {
      const owner = ++nextOwner;
      activeOwner = owner;
      mocks.playerState.trackListContext = tracks;
      const cleanup = vi.fn(() => {
        if (activeOwner === owner) mocks.playerState.trackListContext = [];
      });
      contextCleanups.push(cleanup);
      return cleanup;
    });

    const mounted = renderPage(true);
    await waitFor(() => expect(mocks.fetchAlbumDetail).toHaveBeenCalledTimes(2));
    const staleSignal = mocks.fetchAlbumDetail.mock.calls[0][1] as AbortSignal;
    const latestSignal = mocks.fetchAlbumDetail.mock.calls[1][1] as AbortSignal;
    expect(staleSignal.aborted).toBe(true);
    expect(latestSignal.aborted).toBe(false);

    await act(async () => latestRequest.resolve(latestDetail));
    expect(await screen.findByRole('heading', { name: latestDetail.title })).toBeInTheDocument();
    expect(mocks.playerState.setTrackListContext).toHaveBeenCalledTimes(1);
    expect(mocks.playerState.trackListContext).toEqual([
      expect.objectContaining({ id: 12, title: 'Strict latest track' }),
    ]);

    await act(async () => staleRequest.resolve(detail));
    expect(screen.queryByRole('heading', { name: detail.title })).not.toBeInTheDocument();
    expect(mocks.playerState.trackListContext).toEqual([
      expect.objectContaining({ id: 12, title: 'Strict latest track' }),
    ]);

    mounted.unmount();
    expect(contextCleanups).toHaveLength(1);
    expect(contextCleanups[0]).toHaveBeenCalledTimes(1);
    expect(mocks.playerState).toMatchObject({
      currentTrack: durableTrack,
      queue: [durableTrack],
      shuffle: true,
      repeat: 'all',
      trackListContext: [],
    });
  });

  it('shows localized missing recovery and bounds duplicate retry clicks', async () => {
    const retryRequest = deferred<AlbumDetail>();
    mocks.fetchAlbumDetail
      .mockRejectedValueOnce({ response: { status: 404, data: { message: 'raw server text' } } })
      .mockReturnValueOnce(retryRequest.promise);
    renderPage();

    expect(await screen.findByRole('alert')).toHaveTextContent('앨범을 찾을 수 없습니다');
    expect(screen.queryByText('raw server text')).not.toBeInTheDocument();
    const retry = screen.getByRole('button', { name: '다시 시도' });
    fireEvent.click(retry);
    fireEvent.click(retry);
    expect(mocks.fetchAlbumDetail).toHaveBeenCalledTimes(2);

    await act(async () => retryRequest.resolve(detail));
    expect(await screen.findByRole('heading', { name: detail.title })).toBeInTheDocument();
  });

  it('passes the complete public Track duration to full playback', async () => {
    renderPage();
    await screen.findByRole('heading', { name: detail.title });
    fireEvent.click(screen.getByLabelText('Play'));

    await waitFor(() =>
      expect(mocks.playerState.play).toHaveBeenCalledWith(
        expect.objectContaining({ id: 11, duration: 446 }),
      ),
    );
  });
});

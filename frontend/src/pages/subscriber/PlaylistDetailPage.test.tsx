import { useLayoutEffect } from 'react';
import { act, fireEvent, render, screen, waitFor } from '@testing-library/react';
import { createMemoryRouter, RouterProvider, useLocation, useNavigate } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import PlaylistDetailPage from '@/pages/subscriber/PlaylistDetailPage';

const api = vi.hoisted(() => ({
  fetchPlaylistDetail: vi.fn(),
  removeTrackFromPlaylist: vi.fn(),
}));

const playerState = {
  currentTrack: null,
  isPlayerPlaying: false,
  play: vi.fn(),
  pause: vi.fn(),
  resume: vi.fn(),
  addToQueue: vi.fn(),
  setTrackListContext: vi.fn(() => vi.fn()),
};

vi.mock('@/api/playlists', () => api);
vi.mock('@/api/downloads', () => ({
  downloadTrack: vi.fn(),
  triggerBlobDownload: vi.fn(),
}));
vi.mock('@/store/playerStore', () => ({
  usePlayerStore: (selector: (state: typeof playerState) => unknown) => selector(playerState),
}));
vi.mock('@/store/likeStore', () => ({
  useLikeStore: () => ({ likedIds: new Set<number>(), load: vi.fn(), toggle: vi.fn() }),
}));
vi.mock('@/store/authStore', () => ({
  useAuthStore: (selector: (state: { user: { id: number } }) => unknown) =>
    selector({ user: { id: 1 } }),
}));
vi.mock('@/store/toastStore', () => ({
  useToastStore: (selector: (state: { show: ReturnType<typeof vi.fn> }) => unknown) =>
    selector({ show: vi.fn() }),
}));

function deferred<T>() {
  let resolve!: (value: T) => void;
  const promise = new Promise<T>((nextResolve) => {
    resolve = nextResolve;
  });
  return { promise, resolve };
}

function RouteHarness({ onLayout }: { onLayout?: (pathname: string) => void }) {
  const navigate = useNavigate();
  const location = useLocation();

  useLayoutEffect(() => {
    onLayout?.(location.pathname);
  }, [location.pathname, onLayout]);

  return (
    <>
      <button type="button" onClick={() => navigate('/playlists/2')}>
        next playlist
      </button>
      <PlaylistDetailPage />
    </>
  );
}

function renderPage(initialEntry: string, onLayout?: (pathname: string) => void) {
  const router = createMemoryRouter(
    [
      { path: '/playlists/:playlistId', element: <RouteHarness onLayout={onLayout} /> },
      { path: '/playlists', element: <div>Playlist list</div> },
    ],
    { initialEntries: [initialEntry] },
  );
  render(<RouterProvider router={router} future={{ v7_startTransition: true }} />);
}

describe('PlaylistDetailPage load ownership', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it.each(['1e3', '0x10', '+7', ' 7', '7 ', '7.5', '0', '-1', '9007199254740992', 'abc'])(
    'rejects noncanonical playlist id %s without a request',
    (id) => {
      renderPage(`/playlists/${id}`);

      expect(screen.getByText('재생목록 주소가 올바르지 않습니다.')).toBeInTheDocument();
      expect(screen.getByRole('link', { name: '재생목록 목록으로' })).toHaveAttribute(
        'href',
        '/playlists',
      );
      expect(api.fetchPlaylistDetail).not.toHaveBeenCalled();
    },
  );

  it('keeps the newest route detail after the retired request resolves', async () => {
    const oldDetail = deferred<{
      id: number;
      title: string;
      description: null;
      thumbnail: null;
      tracks: [];
      createdAt: string;
      updatedAt: string;
    }>();
    const currentDetail = deferred<{
      id: number;
      title: string;
      description: null;
      thumbnail: null;
      tracks: [];
      createdAt: string;
      updatedAt: string;
    }>();
    api.fetchPlaylistDetail
      .mockReturnValueOnce(oldDetail.promise)
      .mockReturnValueOnce(currentDetail.promise);

    renderPage('/playlists/1');
    await waitFor(() => expect(api.fetchPlaylistDetail).toHaveBeenCalledTimes(1));
    const oldSignal = api.fetchPlaylistDetail.mock.calls[0][1] as AbortSignal;
    fireEvent.click(screen.getByRole('button', { name: 'next playlist' }));
    await waitFor(() => expect(api.fetchPlaylistDetail).toHaveBeenCalledTimes(2));
    expect(oldSignal.aborted).toBe(true);

    await act(async () =>
      currentDetail.resolve({
        id: 2,
        title: '현재 재생목록',
        description: null,
        thumbnail: null,
        tracks: [],
        createdAt: '',
        updatedAt: '',
      }),
    );
    expect(await screen.findByRole('heading', { name: '현재 재생목록' })).toBeInTheDocument();

    await act(async () =>
      oldDetail.resolve({
        id: 1,
        title: '이전 재생목록',
        description: null,
        thumbnail: null,
        tracks: [],
        createdAt: '',
        updatedAt: '',
      }),
    );
    expect(screen.queryByText('이전 재생목록')).not.toBeInTheDocument();
    expect(screen.getByRole('heading', { name: '현재 재생목록' })).toBeInTheDocument();
  });

  it('hides the retired route projection before passive effects run', async () => {
    const currentDetail = deferred<{
      id: number;
      title: string;
      description: null;
      thumbnail: null;
      tracks: [];
      createdAt: string;
      updatedAt: string;
    }>();
    api.fetchPlaylistDetail
      .mockResolvedValueOnce({
        id: 1,
        title: 'Retired route playlist',
        description: null,
        thumbnail: null,
        tracks: [],
        createdAt: '',
        updatedAt: '',
      })
      .mockReturnValueOnce(currentDetail.promise);
    const observations: Array<{ pathname: string; retiredVisible: boolean }> = [];

    renderPage('/playlists/1', (pathname) => {
      observations.push({
        pathname,
        retiredVisible: document.body.textContent?.includes('Retired route playlist') ?? false,
      });
    });
    expect(
      await screen.findByRole('heading', { name: 'Retired route playlist' }),
    ).toBeInTheDocument();
    observations.length = 0;

    fireEvent.click(screen.getByRole('button', { name: 'next playlist' }));

    expect(observations).toContainEqual({ pathname: '/playlists/2', retiredVisible: false });
    expect(screen.queryByText('Retired route playlist')).not.toBeInTheDocument();
  });
});

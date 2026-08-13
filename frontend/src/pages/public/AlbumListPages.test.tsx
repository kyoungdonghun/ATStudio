import { act, fireEvent, render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter, Route, Routes, useLocation, useNavigate } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import AlbumListImagePage from '@/pages/public/AlbumListImagePage';
import AlbumListPage from '@/pages/public/AlbumListPage';
import type { Album, PagedResponse } from '@/types';

const mocks = vi.hoisted(() => ({
  fetchAlbums: vi.fn(),
}));

vi.mock('@/api/albums', () => ({
  fetchAlbums: (...args: unknown[]) => mocks.fetchAlbums(...args),
}));

vi.mock('@/store/authStore', () => ({
  useAuthStore: (selector: (state: { isAuthenticated: () => boolean }) => unknown) =>
    selector({ isAuthenticated: () => false }),
}));

vi.mock('@/store/albumLikeStore', () => ({
  useAlbumLikeStore: (
    selector: (state: {
      loaded: boolean;
      load: () => Promise<void>;
      likedAlbumIds: Set<number>;
      toggle: () => Promise<void>;
    }) => unknown,
  ) =>
    selector({
      loaded: true,
      load: vi.fn().mockResolvedValue(undefined),
      likedAlbumIds: new Set<number>(),
      toggle: vi.fn().mockResolvedValue(undefined),
    }),
}));

vi.mock('@/components/album/AlbumCard', () => ({
  default: ({ album, onClick }: { album: Album; onClick: (album: Album) => void }) => (
    <button type="button" onClick={() => onClick(album)}>
      {album.title}
    </button>
  ),
}));

vi.mock('@/components/ui/Pagination', () => ({
  default: ({ onPageChange }: { onPageChange: (page: number) => void }) => (
    <button type="button" onClick={() => onPageChange(2)}>
      2페이지
    </button>
  ),
}));

const album: Album = {
  id: 7,
  title: 'Current album',
  description: null,
  thumbnailUrl: null,
  trackCount: 1,
  likeCount: 0,
  createdAt: '2026-08-09T00:00:00',
};

function albumPage(page: number, total = 41, dataList: Album[] = [album]): PagedResponse<Album> {
  return {
    dataList,
    pageInfo: {
      page,
      size: 20,
      total,
      start: total === 0 ? 0 : 1,
      end: total === 0 ? 0 : Math.ceil(total / 20),
      prev: page > 1,
      next: page < Math.ceil(total / 20),
    },
  };
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

function LocationProbe() {
  const location = useLocation();
  return <div data-testid="location">{`${location.pathname}${location.search}`}</div>;
}

function QueryControls() {
  const navigate = useNavigate();
  return (
    <button type="button" onClick={() => navigate('/albums?sort=trackCount&page=2')}>
      newer album query
    </button>
  );
}

function renderPages(initialEntry = '/albums?sort=trackCount&page=2') {
  return render(
    <MemoryRouter
      initialEntries={[initialEntry]}
      future={{ v7_startTransition: true, v7_relativeSplatPath: true }}
    >
      <QueryControls />
      <LocationProbe />
      <Routes>
        <Route path="/albums" element={<AlbumListImagePage />} />
        <Route path="/albums/list" element={<AlbumListPage />} />
      </Routes>
    </MemoryRouter>,
  );
}

describe('Album catalog page contract', () => {
  beforeEach(() => {
    mocks.fetchAlbums.mockReset();
  });

  it('preserves sort/page query state and one page projection across both views', async () => {
    mocks.fetchAlbums.mockResolvedValue(albumPage(2));
    renderPages();

    await screen.findByRole('button', { name: album.title });
    expect(mocks.fetchAlbums).toHaveBeenLastCalledWith(
      { page: 2, size: 20, sort: 'trackCount' },
      expect.any(AbortSignal),
    );

    fireEvent.click(screen.getByRole('link', { name: '리스트' }));
    expect(await screen.findByTestId('location')).toHaveTextContent(
      '/albums/list?sort=trackCount&page=2',
    );
    await waitFor(() => expect(mocks.fetchAlbums).toHaveBeenCalledTimes(2));
    expect(mocks.fetchAlbums).toHaveBeenLastCalledWith(
      { page: 2, size: 20, sort: 'trackCount' },
      expect.any(AbortSignal),
    );

    fireEvent.click(screen.getByRole('link', { name: '카드' }));
    expect(await screen.findByTestId('location')).toHaveTextContent(
      '/albums?sort=trackCount&page=2',
    );
  });

  it.each(['abc', '-1', '0', '1.5'])(
    'replaces invalid page=%s before one valid request',
    async (page) => {
      mocks.fetchAlbums.mockResolvedValue(albumPage(1));
      renderPages(`/albums?sort=trackCount&page=${page}`);

      await waitFor(() => expect(screen.getByTestId('location')).toHaveTextContent('page=1'));
      await waitFor(() => expect(mocks.fetchAlbums).toHaveBeenCalledTimes(1));
      expect(mocks.fetchAlbums).toHaveBeenCalledWith(
        { page: 1, size: 20, sort: 'trackCount' },
        expect.any(AbortSignal),
      );
    },
  );

  it('normalizes an out-of-range page to the last page with one bounded follow-up', async () => {
    mocks.fetchAlbums
      .mockResolvedValueOnce(albumPage(999, 21, []))
      .mockResolvedValueOnce(albumPage(2, 21));
    renderPages('/albums?page=999');

    await waitFor(() => expect(screen.getByTestId('location')).toHaveTextContent('/albums?page=2'));
    expect(await screen.findByRole('button', { name: album.title })).toBeInTheDocument();
    expect(mocks.fetchAlbums).toHaveBeenCalledTimes(2);
    expect(mocks.fetchAlbums.mock.calls.map(([params]) => params.page)).toEqual([999, 2]);
  });

  it('keeps the latest list result and aborts the superseded request', async () => {
    const oldRequest = deferred<PagedResponse<Album>>();
    const currentRequest = deferred<PagedResponse<Album>>();
    mocks.fetchAlbums
      .mockReturnValueOnce(oldRequest.promise)
      .mockReturnValueOnce(currentRequest.promise);
    renderPages('/albums?page=1');
    const oldSignal = mocks.fetchAlbums.mock.calls[0][1] as AbortSignal;

    fireEvent.click(screen.getByRole('button', { name: 'newer album query' }));
    expect(oldSignal.aborted).toBe(true);
    await act(async () =>
      currentRequest.resolve(albumPage(2, 41, [{ ...album, title: 'Newest' }])),
    );
    expect(await screen.findByRole('button', { name: 'Newest' })).toBeInTheDocument();

    await act(async () => oldRequest.resolve(albumPage(1, 41, [{ ...album, title: 'Stale' }])));
    expect(screen.queryByRole('button', { name: 'Stale' })).not.toBeInTheDocument();
  });

  it('renders fixed recovery copy and sends one manual retry', async () => {
    const retryRequest = deferred<PagedResponse<Album>>();
    mocks.fetchAlbums
      .mockRejectedValueOnce(new Error('internal transport detail'))
      .mockReturnValueOnce(retryRequest.promise);
    renderPages('/albums');

    expect(await screen.findByRole('alert')).toHaveTextContent(
      '앨범 목록 정보를 불러오지 못했습니다.',
    );
    expect(screen.queryByText('internal transport detail')).not.toBeInTheDocument();
    const retry = screen.getByRole('button', { name: '다시 시도' });
    fireEvent.click(retry);
    fireEvent.click(retry);
    expect(mocks.fetchAlbums).toHaveBeenCalledTimes(2);

    await act(async () => retryRequest.resolve(albumPage(1)));
    expect(await screen.findByRole('button', { name: album.title })).toBeInTheDocument();
  });
});

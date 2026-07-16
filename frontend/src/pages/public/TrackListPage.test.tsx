import { act, fireEvent, render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter, Route, Routes, useNavigate } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import TrackListPage from '@/pages/public/TrackListPage';
import type { PagedResponse, TrackListItem } from '@/types';

const mocks = vi.hoisted(() => ({
  fetchTracks: vi.fn(),
  fetchTags: vi.fn(),
  fetchAvailableTags: vi.fn(),
  fetchDownloadCount: vi.fn(),
  downloadTrack: vi.fn(),
  triggerBlobDownload: vi.fn(),
  playerState: {
    currentTrack: null,
    isPlaying: false,
    play: vi.fn(),
    pause: vi.fn(),
    resume: vi.fn(),
    setTrackListContext: vi.fn(),
  },
  likeState: {
    loaded: true,
    load: vi.fn(),
    likedIds: new Set<number>(),
    toggle: vi.fn(),
  },
  authState: {
    isAuthenticated: vi.fn(() => false),
  },
  toastState: {
    show: vi.fn(),
  },
}));

vi.mock('@/api/tracks', () => ({
  fetchTracks: (...args: unknown[]) => mocks.fetchTracks(...args),
}));

vi.mock('@/api/tags', () => ({
  fetchTags: (...args: unknown[]) => mocks.fetchTags(...args),
  fetchAvailableTags: (...args: unknown[]) => mocks.fetchAvailableTags(...args),
}));

vi.mock('@/api/downloads', () => ({
  fetchDownloadCount: (...args: unknown[]) => mocks.fetchDownloadCount(...args),
  downloadTrack: (...args: unknown[]) => mocks.downloadTrack(...args),
  triggerBlobDownload: (...args: unknown[]) => mocks.triggerBlobDownload(...args),
}));

vi.mock('@/store/playerStore', () => ({
  usePlayerStore: (selector: (state: typeof mocks.playerState) => unknown) =>
    selector(mocks.playerState),
}));

vi.mock('@/store/likeStore', () => ({
  useLikeStore: (selector: (state: typeof mocks.likeState) => unknown) => selector(mocks.likeState),
}));

vi.mock('@/store/authStore', () => ({
  useAuthStore: (selector: (state: typeof mocks.authState) => unknown) => selector(mocks.authState),
}));

vi.mock('@/store/toastStore', () => ({
  useToastStore: (selector: (state: typeof mocks.toastState) => unknown) =>
    selector(mocks.toastState),
}));

vi.mock('@/components/track/TrackRow', () => ({
  default: ({ track }: { track: TrackListItem }) => (
    <tr>
      <td data-testid="track-row">{track.title}</td>
    </tr>
  ),
}));

vi.mock('@/components/ui/FilterChip', () => ({
  default: ({ label, onClick }: { label: string; onClick: () => void }) => (
    <button onClick={onClick} type="button">
      {label}
    </button>
  ),
}));

vi.mock('@/components/filter/TagFilterModal', () => ({ default: () => null }));
vi.mock('@/components/playlist/AddToPlaylistModal', () => ({ default: () => null }));
vi.mock('@/components/ui/Pagination', () => ({
  default: ({ pageInfo }: { pageInfo: { total: number } }) => (
    <div data-testid="pagination-total">{pageInfo.total}</div>
  ),
}));

interface Deferred<T> {
  promise: Promise<T>;
  resolve: (value: T) => void;
  reject: (reason: unknown) => void;
}

function deferred<T>(): Deferred<T> {
  let resolve!: (value: T) => void;
  let reject!: (reason: unknown) => void;
  const promise = new Promise<T>((resolvePromise, rejectPromise) => {
    resolve = resolvePromise;
    reject = rejectPromise;
  });
  return { promise, resolve, reject };
}

function trackPage(title: string, page: number, total = 1): PagedResponse<TrackListItem> {
  return {
    dataList: [
      {
        id: page,
        title,
        artistName: 'AT.M',
        duration: 120,
        bpm: 100,
        tonality: 'C',
        thumbnail: null,
        playCount: 0,
        likeCount: 0,
        downloadCount: 0,
        waveformData: null,
        tags: [],
        createdAt: '2026-07-16T00:00:00',
      },
    ],
    pageInfo: {
      page,
      size: 20,
      total,
      start: 1,
      end: 1,
      prev: page > 1,
      next: false,
    },
  };
}

function emptyTrackPage(): PagedResponse<TrackListItem> {
  return {
    dataList: [],
    pageInfo: {
      page: 1,
      size: 20,
      total: 0,
      start: 0,
      end: 0,
      prev: false,
      next: false,
    },
  };
}

function QueryControls() {
  const navigate = useNavigate();
  return (
    <>
      <button onClick={() => navigate('/tracks?keyword=new&page=2')} type="button">
        newer query
      </button>
      <button onClick={() => navigate('/tracks?genre=new&page=1')} type="button">
        newer filter
      </button>
    </>
  );
}

function renderPage(initialEntry = '/tracks?keyword=old&page=1') {
  return render(
    <MemoryRouter
      initialEntries={[initialEntry]}
      future={{ v7_relativeSplatPath: true, v7_startTransition: true }}
    >
      <QueryControls />
      <Routes>
        <Route path="/tracks" element={<TrackListPage />} />
      </Routes>
    </MemoryRouter>,
  );
}

describe('TrackListPage latest-request-wins', () => {
  beforeEach(() => {
    mocks.fetchTracks.mockReset();
    mocks.fetchTags.mockReset();
    mocks.fetchAvailableTags.mockReset();
    mocks.fetchDownloadCount.mockReset();
    mocks.downloadTrack.mockReset();
    mocks.triggerBlobDownload.mockReset();
    mocks.fetchTags.mockResolvedValue([]);
    mocks.fetchAvailableTags.mockResolvedValue([]);
  });

  it('keeps the newer keyword/page result when the older success resolves last', async () => {
    const oldRequest = deferred<PagedResponse<TrackListItem>>();
    const newRequest = deferred<PagedResponse<TrackListItem>>();
    mocks.fetchTracks
      .mockReturnValueOnce(oldRequest.promise)
      .mockReturnValueOnce(newRequest.promise);

    renderPage();
    await waitFor(() => expect(mocks.fetchTracks).toHaveBeenCalledTimes(1));

    fireEvent.click(screen.getByRole('button', { name: 'newer query' }));
    await waitFor(() => expect(mocks.fetchTracks).toHaveBeenCalledTimes(2));

    await act(async () => {
      newRequest.resolve(trackPage('new result', 2, 2));
      await newRequest.promise;
    });
    expect(await screen.findByText('new result')).toBeInTheDocument();
    expect(screen.getByTestId('pagination-total')).toHaveTextContent('2');

    await act(async () => {
      oldRequest.resolve(trackPage('old result', 1, 99));
      await oldRequest.promise;
    });

    expect(screen.getByText('new result')).toBeInTheDocument();
    expect(screen.queryByText('old result')).not.toBeInTheDocument();
    expect(screen.getByTestId('pagination-total')).toHaveTextContent('2');
    expect((mocks.fetchTracks.mock.calls[0][1] as AbortSignal).aborted).toBe(true);
  });

  it('ignores an older failure after the newer request succeeds', async () => {
    const oldRequest = deferred<PagedResponse<TrackListItem>>();
    const newRequest = deferred<PagedResponse<TrackListItem>>();
    mocks.fetchTracks
      .mockReturnValueOnce(oldRequest.promise)
      .mockReturnValueOnce(newRequest.promise);

    renderPage();
    await waitFor(() => expect(mocks.fetchTracks).toHaveBeenCalledTimes(1));
    fireEvent.click(screen.getByRole('button', { name: 'newer query' }));
    await waitFor(() => expect(mocks.fetchTracks).toHaveBeenCalledTimes(2));

    await act(async () => {
      newRequest.resolve(trackPage('current result', 2));
      await newRequest.promise;
    });
    expect(await screen.findByText('current result')).toBeInTheDocument();

    await act(async () => {
      oldRequest.reject(new Error('stale failure'));
      await oldRequest.promise.catch(() => undefined);
    });

    expect(screen.getByText('current result')).toBeInTheDocument();
    expect(screen.queryByText('stale failure')).not.toBeInTheDocument();
    expect(screen.queryByText('음원 목록을 불러오는 중...')).not.toBeInTheDocument();
  });

  it('classifies the active failure without exposing its internal message', async () => {
    mocks.fetchTracks.mockRejectedValueOnce(new Error('active failure'));

    renderPage();

    expect(await screen.findByRole('alert')).toHaveTextContent(
      '음원 목록 정보를 불러오지 못했습니다. 네트워크 연결을 확인하고 다시 시도해주세요.',
    );
    expect(screen.queryByText('active failure')).not.toBeInTheDocument();
    expect(screen.queryByText('음원 목록을 불러오는 중...')).not.toBeInTheDocument();
    expect(screen.getByRole('button', { name: '다시 시도' })).toBeEnabled();
  });

  it('sends one retry request and replaces the failure with its delayed success', async () => {
    const retryRequest = deferred<PagedResponse<TrackListItem>>();
    mocks.fetchTracks
      .mockRejectedValueOnce({ response: { status: 500 } })
      .mockReturnValueOnce(retryRequest.promise);

    renderPage();
    expect(await screen.findByRole('alert')).toHaveTextContent('서버 오류가 발생했습니다.');
    const retryButton = screen.getByRole('button', { name: '다시 시도' });

    fireEvent.click(retryButton);
    fireEvent.click(retryButton);

    expect(mocks.fetchTracks).toHaveBeenCalledTimes(2);
    expect(screen.getByText('음원 목록을 불러오는 중...')).toBeInTheDocument();

    await act(async () => {
      retryRequest.resolve(trackPage('retry result', 1));
      await retryRequest.promise;
    });

    expect(await screen.findByText('retry result')).toBeInTheDocument();
    expect(screen.queryByRole('alert')).not.toBeInTheDocument();
    expect(mocks.fetchTracks).toHaveBeenCalledTimes(2);
  });

  it('returns a classified retry error and enables retry again after a delayed failure', async () => {
    const retryRequest = deferred<PagedResponse<TrackListItem>>();
    mocks.fetchTracks
      .mockRejectedValueOnce({ response: { status: 500 } })
      .mockReturnValueOnce(retryRequest.promise);

    renderPage();
    fireEvent.click(await screen.findByRole('button', { name: '다시 시도' }));

    await act(async () => {
      retryRequest.reject({ code: 'ETIMEDOUT' });
      await retryRequest.promise.catch(() => undefined);
    });

    expect(await screen.findByRole('alert')).toHaveTextContent(
      '네트워크 연결을 확인하고 다시 시도해주세요.',
    );
    expect(screen.getByRole('button', { name: '다시 시도' })).toBeEnabled();
    expect(mocks.fetchTracks).toHaveBeenCalledTimes(2);
  });

  it('renders a successful empty result without an error or retry control', async () => {
    mocks.fetchTracks.mockResolvedValueOnce(emptyTrackPage());

    renderPage();

    expect(await screen.findByText('검색 결과가 없습니다.')).toBeInTheDocument();
    expect(screen.queryByRole('alert')).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: '다시 시도' })).not.toBeInTheDocument();
  });

  it('keeps available filter tags from the newest filter request', async () => {
    const oldAvailableRequest =
      deferred<Array<{ id: number; name: string; type: 'MOOD'; createdAt: string }>>();
    const newAvailableRequest =
      deferred<Array<{ id: number; name: string; type: 'MOOD'; createdAt: string }>>();
    mocks.fetchTracks.mockResolvedValue(emptyTrackPage());
    mocks.fetchTags
      .mockResolvedValueOnce([
        { id: 1, name: 'old', type: 'GENRE', createdAt: '2026-07-16T00:00:00' },
        { id: 2, name: 'new', type: 'GENRE', createdAt: '2026-07-16T00:00:00' },
      ])
      .mockResolvedValueOnce([
        { id: 3, name: 'stale-mood', type: 'MOOD', createdAt: '2026-07-16T00:00:00' },
        { id: 4, name: 'current-mood', type: 'MOOD', createdAt: '2026-07-16T00:00:00' },
      ])
      .mockResolvedValueOnce([]);
    mocks.fetchAvailableTags
      .mockReturnValueOnce(oldAvailableRequest.promise)
      .mockReturnValueOnce(newAvailableRequest.promise);

    renderPage('/tracks?genre=old&page=1');
    await waitFor(() => expect(mocks.fetchAvailableTags).toHaveBeenCalledTimes(1));

    fireEvent.click(screen.getByRole('button', { name: 'newer filter' }));
    await waitFor(() => expect(mocks.fetchAvailableTags).toHaveBeenCalledTimes(2));

    await act(async () => {
      newAvailableRequest.resolve([
        { id: 4, name: 'current-mood', type: 'MOOD', createdAt: '2026-07-16T00:00:00' },
      ]);
      await newAvailableRequest.promise;
    });
    expect(await screen.findByRole('button', { name: 'current-mood' })).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'stale-mood' })).not.toBeInTheDocument();

    await act(async () => {
      oldAvailableRequest.resolve([
        { id: 3, name: 'stale-mood', type: 'MOOD', createdAt: '2026-07-16T00:00:00' },
      ]);
      await oldAvailableRequest.promise;
    });

    expect(screen.getByRole('button', { name: 'current-mood' })).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'stale-mood' })).not.toBeInTheDocument();
    expect((mocks.fetchAvailableTags.mock.calls[0][1] as AbortSignal).aborted).toBe(true);
  });
});

import { act, fireEvent, render, screen, waitFor, within } from '@testing-library/react';
import { MemoryRouter, Route, Routes, useLocation, useNavigate } from 'react-router-dom';
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
  createDownloadFallbackFileName: (_resource: string, id: number, title: string) =>
    `track-${id}-${title}.mp3`,
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
  default: ({
    track,
    onDownload,
    downloadPending,
  }: {
    track: TrackListItem;
    onDownload?: (track: TrackListItem) => void;
    downloadPending?: boolean;
  }) => (
    <tr>
      <td data-testid="track-row">{track.title}</td>
      <td>
        <button disabled={downloadPending} onClick={() => onDownload?.(track)} type="button">
          Download {track.title}
        </button>
      </td>
    </tr>
  ),
}));

vi.mock('@/components/ui/FilterChip', () => ({
  default: ({
    label,
    active,
    onClick,
  }: {
    label: string;
    active?: boolean;
    onClick: () => void;
  }) => (
    <button onClick={onClick} type="button" aria-pressed={active}>
      {label}
    </button>
  ),
}));

vi.mock('@/components/playlist/AddToPlaylistModal', () => ({ default: () => null }));
vi.mock('@/components/ui/Pagination', () => ({
  default: ({
    pageInfo,
    onPageChange,
  }: {
    pageInfo: { total: number };
    onPageChange: (page: number) => void;
  }) => (
    <div>
      <div data-testid="pagination-total">{pageInfo.total}</div>
      <button type="button" onClick={() => onPageChange(4)}>
        page 4
      </button>
    </div>
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
      <button onClick={() => navigate('/tracks?genre=latest&page=1')} type="button">
        latest filter
      </button>
    </>
  );
}

function LocationProbe() {
  const location = useLocation();
  return <div data-testid="location">{`${location.pathname}${location.search}`}</div>;
}

function renderPage(initialEntry = '/tracks?keyword=old&page=1') {
  return render(
    <MemoryRouter
      initialEntries={[initialEntry]}
      future={{ v7_relativeSplatPath: true, v7_startTransition: true }}
    >
      <QueryControls />
      <LocationProbe />
      <Routes>
        <Route path="/tracks" element={<TrackListPage />} />
      </Routes>
    </MemoryRouter>,
  );
}

function latestTrackParams(): Record<string, unknown> {
  const calls = mocks.fetchTracks.mock.calls;
  return calls[calls.length - 1]?.[0] as Record<string, unknown>;
}

describe('TrackListPage latest-request-wins', () => {
  beforeEach(() => {
    mocks.fetchTracks.mockReset();
    mocks.fetchTags.mockReset();
    mocks.fetchAvailableTags.mockReset();
    mocks.fetchDownloadCount.mockReset();
    mocks.downloadTrack.mockReset();
    mocks.triggerBlobDownload.mockReset();
    mocks.playerState.setTrackListContext.mockReset();
    mocks.fetchTags.mockResolvedValue([]);
    mocks.fetchAvailableTags.mockResolvedValue([]);
  });

  it('fences repeated track downloads synchronously and releases the identity after failure', async () => {
    const firstAttempt = deferred<never>();
    mocks.fetchTracks.mockResolvedValue(trackPage('Fence Track', 1));
    mocks.downloadTrack
      .mockReturnValueOnce(firstAttempt.promise)
      .mockRejectedValueOnce(new Error('retry'));

    renderPage();
    const downloadButton = await screen.findByRole('button', { name: 'Download Fence Track' });
    fireEvent.click(downloadButton);
    fireEvent.click(downloadButton);

    expect(mocks.downloadTrack).toHaveBeenCalledTimes(1);
    expect(downloadButton).toBeDisabled();

    await act(async () => firstAttempt.reject(new Error('offline')));
    await waitFor(() => expect(downloadButton).toBeEnabled());

    fireEvent.click(downloadButton);
    await waitFor(() => expect(mocks.downloadTrack).toHaveBeenCalledTimes(2));
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

  it.each(['abc', '-1', '0', '1.5'])(
    'replaces invalid page=%s before one valid request',
    async (page) => {
      mocks.fetchTracks.mockResolvedValue(trackPage('normalized', 1));
      renderPage(`/tracks?keyword=old&page=${page}`);

      await waitFor(() => expect(screen.getByTestId('location')).toHaveTextContent('page=1'));
      await waitFor(() => expect(mocks.fetchTracks).toHaveBeenCalledTimes(1));
      expect(mocks.fetchTracks).toHaveBeenCalledWith(
        expect.objectContaining({ page: 1, size: 20 }),
        expect.any(AbortSignal),
      );
    },
  );

  it('normalizes an out-of-range page to the last page with one bounded follow-up', async () => {
    mocks.fetchTracks
      .mockResolvedValueOnce({ ...trackPage('unused', 999, 21), dataList: [] })
      .mockResolvedValueOnce(trackPage('last page', 2, 21));
    renderPage('/tracks?page=999');

    await waitFor(() => expect(screen.getByTestId('location')).toHaveTextContent('/tracks?page=2'));
    expect(await screen.findByText('last page')).toBeInTheDocument();
    expect(mocks.fetchTracks).toHaveBeenCalledTimes(2);
    expect(mocks.fetchTracks.mock.calls.map(([params]) => params.page)).toEqual([999, 2]);
  });

  it('runs the visible-list cleanup returned by the player store on unmount', async () => {
    const cleanups: Array<ReturnType<typeof vi.fn>> = [];
    mocks.playerState.setTrackListContext.mockImplementation(() => {
      const cleanup = vi.fn();
      cleanups.push(cleanup);
      return cleanup;
    });
    mocks.fetchTracks.mockResolvedValue(trackPage('owned context', 1));
    const view = renderPage();

    await screen.findByText('owned context');
    view.unmount();

    expect(cleanups).toHaveLength(2);
    expect(cleanups[0]).toHaveBeenCalledTimes(1);
    expect(cleanups[1]).toHaveBeenCalledTimes(1);
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
      .mockResolvedValueOnce([])
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

  it('shows all choices while availability refreshes and after the current request fails', async () => {
    const supersededRequest =
      deferred<Array<{ id: number; name: string; type: 'MOOD'; createdAt: string }>>();
    const currentRequest =
      deferred<Array<{ id: number; name: string; type: 'MOOD'; createdAt: string }>>();
    mocks.fetchTracks.mockResolvedValue(emptyTrackPage());
    mocks.fetchTags
      .mockResolvedValueOnce([])
      .mockResolvedValueOnce([
        { id: 1, name: 'stale-mood', type: 'MOOD', createdAt: '2026-08-09' },
        { id: 2, name: 'safe-fallback', type: 'MOOD', createdAt: '2026-08-09' },
      ])
      .mockResolvedValueOnce([])
      .mockResolvedValueOnce([]);
    mocks.fetchAvailableTags
      .mockResolvedValueOnce([{ id: 1, name: 'stale-mood', type: 'MOOD', createdAt: '2026-08-09' }])
      .mockReturnValueOnce(supersededRequest.promise)
      .mockReturnValueOnce(currentRequest.promise);

    renderPage('/tracks?genre=old&page=1');
    expect(await screen.findByRole('button', { name: 'stale-mood' })).toBeInTheDocument();
    await waitFor(() =>
      expect(screen.queryByRole('button', { name: 'safe-fallback' })).not.toBeInTheDocument(),
    );

    fireEvent.click(screen.getByRole('button', { name: 'newer filter' }));
    await waitFor(() => expect(mocks.fetchAvailableTags).toHaveBeenCalledTimes(2));
    expect(screen.getByRole('button', { name: 'safe-fallback' })).toBeInTheDocument();

    fireEvent.click(screen.getByRole('button', { name: 'latest filter' }));
    await waitFor(() => expect(mocks.fetchAvailableTags).toHaveBeenCalledTimes(3));
    await act(async () => {
      currentRequest.reject(new Error('availability unavailable'));
      try {
        await currentRequest.promise;
      } catch {
        // Expected current-request failure exercises the show-all fallback.
      }
    });
    expect(screen.getByRole('button', { name: 'safe-fallback' })).toBeInTheDocument();

    await act(async () => {
      supersededRequest.resolve([
        { id: 1, name: 'stale-mood', type: 'MOOD', createdAt: '2026-08-09' },
      ]);
      await supersededRequest.promise;
    });
    expect(screen.getByRole('button', { name: 'safe-fallback' })).toBeInTheDocument();
    expect((mocks.fetchAvailableTags.mock.calls[1][1] as AbortSignal).aborted).toBe(true);
  });

  it('opens all tag families and applies or resets modal filters without changing sort', async () => {
    mocks.fetchTracks.mockResolvedValue(trackPage('modal result', 1, 1));
    mocks.fetchTags.mockImplementation((type: string) => {
      const tags = {
        GENRE: [{ id: 1, name: 'modal-genre', type: 'GENRE', createdAt: '2026-08-09' }],
        MOOD: [{ id: 2, name: 'modal-mood', type: 'MOOD', createdAt: '2026-08-09' }],
        INSTRUMENT: [
          { id: 3, name: 'modal-instrument', type: 'INSTRUMENT', createdAt: '2026-08-09' },
        ],
        USAGE: [{ id: 4, name: 'modal-usage', type: 'USAGE', createdAt: '2026-08-09' }],
      } as const;
      return Promise.resolve(tags[type as keyof typeof tags]);
    });
    mocks.fetchAvailableTags.mockResolvedValue([]);

    renderPage('/tracks?sort=popular&page=3');
    const openButton = await screen.findByRole('button', { name: '전체 필터' });
    expect(openButton).toHaveAttribute('type', 'button');
    fireEvent.click(openButton);

    let dialog = screen.getByRole('dialog', { name: '필터 검색' });
    expect(within(dialog).getByText('장르')).toBeInTheDocument();
    expect(within(dialog).getByText('분위기')).toBeInTheDocument();
    expect(within(dialog).getByText('악기')).toBeInTheDocument();
    expect(within(dialog).getByText('용도')).toBeInTheDocument();
    fireEvent.click(within(dialog).getByRole('button', { name: 'modal-genre' }));
    fireEvent.click(within(dialog).getByRole('button', { name: 'modal-mood' }));
    fireEvent.click(within(dialog).getByRole('button', { name: 'modal-instrument' }));
    fireEvent.click(within(dialog).getByRole('button', { name: '#modal-usage' }));
    fireEvent.click(within(dialog).getByRole('button', { name: '적용' }));

    await waitFor(() =>
      expect(mocks.fetchTracks).toHaveBeenLastCalledWith(
        expect.objectContaining({
          page: 1,
          sort: 'popular',
          genre: ['modal-genre'],
          mood: ['modal-mood'],
          instrument: ['modal-instrument'],
          usage: ['modal-usage'],
        }),
        expect.any(AbortSignal),
      ),
    );

    fireEvent.click(screen.getByRole('button', { name: '전체 필터' }));
    dialog = screen.getByRole('dialog', { name: '필터 검색' });
    fireEvent.click(within(dialog).getByRole('button', { name: '초기화' }));
    fireEvent.click(within(dialog).getByRole('button', { name: '적용' }));

    await waitFor(() => {
      const calls = mocks.fetchTracks.mock.calls;
      const params = calls[calls.length - 1]?.[0] as Record<string, unknown>;
      expect(params).toMatchObject({ page: 1, sort: 'popular' });
      expect(params).not.toHaveProperty('genre');
      expect(params).not.toHaveProperty('mood');
      expect(params).not.toHaveProperty('instrument');
      expect(params).not.toHaveProperty('usage');
    });
  });

  it('keeps all omitted URL selections visible through one taxonomy failure and scoped recovery', async () => {
    mocks.fetchTracks.mockResolvedValue(emptyTrackPage());
    let moodRequests = 0;
    mocks.fetchTags.mockImplementation((type: string) => {
      if (type === 'MOOD') {
        moodRequests += 1;
        return moodRequests === 1
          ? Promise.reject(new Error('mood taxonomy unavailable'))
          : Promise.resolve([]);
      }
      const tags = {
        GENRE: [{ id: 101, name: 'catalog-genre', type: 'GENRE' }],
        INSTRUMENT: [{ id: 102, name: 'catalog-instrument', type: 'INSTRUMENT' }],
        USAGE: [{ id: 103, name: 'catalog-usage', type: 'USAGE' }],
      } as const;
      return Promise.resolve(tags[type as keyof typeof tags]);
    });
    mocks.fetchAvailableTags.mockResolvedValue([]);

    renderPage(
      '/tracks?genre=url-genre&mood=url-mood&instrument=url-instrument&usage=url-usage&sort=popular&page=2',
    );

    await waitFor(() =>
      expect(mocks.fetchTracks).toHaveBeenCalledWith(
        expect.objectContaining({
          page: 2,
          genre: ['url-genre'],
          mood: ['url-mood'],
          instrument: ['url-instrument'],
          usage: ['url-usage'],
        }),
        expect.any(AbortSignal),
      ),
    );
    for (const label of ['url-genre', 'url-mood', 'url-instrument', '#url-usage']) {
      expect(await screen.findByRole('button', { name: label })).toHaveAttribute(
        'aria-pressed',
        'true',
      );
    }
    expect(screen.getByRole('button', { name: 'catalog-genre' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'catalog-instrument' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: '#catalog-usage' })).toBeInTheDocument();
    expect(await screen.findByRole('alert')).toHaveTextContent(
      '분위기 태그를 불러오지 못했습니다.',
    );

    fireEvent.click(screen.getByRole('button', { name: '전체 필터' }));
    const dialog = screen.getByRole('dialog', { name: '필터 검색' });
    for (const label of ['url-genre', 'url-mood', 'url-instrument', '#url-usage']) {
      expect(within(dialog).getByRole('button', { name: label })).toHaveAttribute(
        'aria-pressed',
        'true',
      );
    }
    fireEvent.keyDown(document, { key: 'Escape' });

    const retryMood = screen.getByRole('button', { name: '분위기 태그 다시 시도' });
    fireEvent.click(retryMood);
    fireEvent.click(retryMood);
    await waitFor(() =>
      expect(screen.queryByText('분위기 태그를 불러오지 못했습니다.')).not.toBeInTheDocument(),
    );
    expect(mocks.fetchTags.mock.calls.filter(([type]) => type === 'MOOD')).toHaveLength(2);
    expect(mocks.fetchTags.mock.calls.filter(([type]) => type !== 'MOOD')).toHaveLength(3);
    expect(screen.getByRole('button', { name: 'url-mood' })).toHaveAttribute(
      'aria-pressed',
      'true',
    );

    fireEvent.click(screen.getByRole('button', { name: '#url-usage' }));
    await waitFor(() => {
      const params = latestTrackParams();
      expect(params).not.toHaveProperty('usage');
      expect(params).toMatchObject({
        genre: ['url-genre'],
        mood: ['url-mood'],
        instrument: ['url-instrument'],
      });
    });

    fireEvent.click(screen.getByRole('button', { name: 'url-instrument' }));
    await waitFor(() => {
      const params = latestTrackParams();
      expect(params).not.toHaveProperty('instrument');
    });
    fireEvent.click(screen.getByRole('button', { name: 'url-mood' }));
    await waitFor(() => {
      const params = latestTrackParams();
      expect(params).not.toHaveProperty('mood');
    });
    fireEvent.click(screen.getByRole('button', { name: 'url-genre' }));
    await waitFor(() => {
      const params = latestTrackParams();
      expect(params).not.toHaveProperty('genre');
    });
  });

  it('resets all active fallback chips and their API arrays when every taxonomy omits them', async () => {
    mocks.fetchTracks.mockResolvedValue(emptyTrackPage());
    mocks.fetchTags.mockResolvedValue([]);
    mocks.fetchAvailableTags.mockResolvedValue([]);

    renderPage(
      '/tracks?genre=reset-genre&mood=reset-mood&instrument=reset-instrument&usage=reset-usage&page=3',
    );

    expect(await screen.findByRole('button', { name: 'reset-genre' })).toHaveAttribute(
      'aria-pressed',
      'true',
    );
    expect(screen.getByRole('button', { name: 'reset-mood' })).toHaveAttribute(
      'aria-pressed',
      'true',
    );
    expect(screen.getByRole('button', { name: 'reset-instrument' })).toHaveAttribute(
      'aria-pressed',
      'true',
    );
    expect(screen.getByRole('button', { name: '#reset-usage' })).toHaveAttribute(
      'aria-pressed',
      'true',
    );

    fireEvent.click(screen.getByRole('button', { name: '초기화' }));

    await waitFor(() => {
      const params = latestTrackParams();
      expect(params).toMatchObject({ page: 1 });
      expect(params).not.toHaveProperty('genre');
      expect(params).not.toHaveProperty('mood');
      expect(params).not.toHaveProperty('instrument');
      expect(params).not.toHaveProperty('usage');
    });
    expect(screen.queryByRole('button', { name: 'reset-genre' })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'reset-mood' })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'reset-instrument' })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: '#reset-usage' })).not.toBeInTheDocument();
  });

  it('restores all four encoded tag types and retains them across sort and pagination until reset', async () => {
    mocks.fetchTracks.mockResolvedValue(trackPage('encoded result', 3, 80));
    mocks.fetchTags.mockImplementation((type: string) => {
      if (type === 'GENRE') {
        return Promise.resolve([
          { id: 1, name: '한글 장르', type: 'GENRE', createdAt: '2026-08-09' },
        ]);
      }
      if (type === 'MOOD') {
        return Promise.resolve([
          { id: 2, name: 'space value', type: 'MOOD', createdAt: '2026-08-09' },
        ]);
      }
      if (type === 'INSTRUMENT') {
        return Promise.resolve([
          { id: 3, name: 'comma,value', type: 'INSTRUMENT', createdAt: '2026-08-09' },
          { id: 4, name: 'hash#value', type: 'INSTRUMENT', createdAt: '2026-08-09' },
        ]);
      }
      return Promise.resolve([{ id: 5, name: '쇼츠 용', type: 'USAGE', createdAt: '2026-08-09' }]);
    });
    mocks.fetchAvailableTags.mockResolvedValue([
      { id: 1, name: '한글 장르', type: 'GENRE' },
      { id: 2, name: 'space value', type: 'MOOD' },
      { id: 3, name: 'comma,value', type: 'INSTRUMENT' },
      { id: 4, name: 'hash#value', type: 'INSTRUMENT' },
      { id: 5, name: '쇼츠 용', type: 'USAGE' },
    ]);

    renderPage(
      '/tracks?genre=%ED%95%9C%EA%B8%80+%EC%9E%A5%EB%A5%B4&mood=space+value&instrument=comma%2Cvalue&instrument=hash%23value&usage=%EC%87%BC%EC%B8%A0+%EC%9A%A9&sort=popular&page=3',
    );

    await waitFor(() =>
      expect(mocks.fetchTracks).toHaveBeenCalledWith(
        expect.objectContaining({
          page: 3,
          sort: 'popular',
          genre: ['한글 장르'],
          mood: ['space value'],
          instrument: ['comma,value', 'hash#value'],
          usage: ['쇼츠 용'],
        }),
        expect.any(AbortSignal),
      ),
    );
    expect(await screen.findByRole('button', { name: 'comma,value' })).toHaveAttribute(
      'aria-pressed',
      'true',
    );
    expect(screen.getByRole('button', { name: 'hash#value' })).toHaveAttribute(
      'aria-pressed',
      'true',
    );

    fireEvent.click(screen.getByRole('button', { name: 'page 4' }));
    await waitFor(() =>
      expect(mocks.fetchTracks).toHaveBeenLastCalledWith(
        expect.objectContaining({
          page: 4,
          sort: 'popular',
          instrument: ['comma,value', 'hash#value'],
        }),
        expect.any(AbortSignal),
      ),
    );

    fireEvent.click(screen.getByRole('button', { name: 'comma,value' }));
    await waitFor(() =>
      expect(mocks.fetchTracks).toHaveBeenLastCalledWith(
        expect.objectContaining({ page: 1, sort: 'popular', instrument: ['hash#value'] }),
        expect.any(AbortSignal),
      ),
    );

    fireEvent.click(screen.getByRole('button', { name: '초기화' }));
    await waitFor(() => {
      const calls = mocks.fetchTracks.mock.calls;
      const resetParams = calls[calls.length - 1]?.[0] as Record<string, unknown>;
      expect(resetParams).toMatchObject({ page: 1, sort: 'popular' });
      expect(resetParams).not.toHaveProperty('genre');
      expect(resetParams).not.toHaveProperty('mood');
      expect(resetParams).not.toHaveProperty('instrument');
      expect(resetParams).not.toHaveProperty('usage');
    });
  });
});

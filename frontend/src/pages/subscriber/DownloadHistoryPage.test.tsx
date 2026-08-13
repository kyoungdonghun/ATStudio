import { useLayoutEffect } from 'react';
import { act, fireEvent, render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter, useNavigate } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import DownloadHistoryPage from '@/pages/subscriber/DownloadHistoryPage';
import { useAuthStore } from '@/store/authStore';
import type { DownloadCount, DownloadHistoryItem } from '@/api/downloads';
import type { PageInfo, User } from '@/types';

const playerState = {
  currentTrack: null,
  isPlaying: false,
  play: vi.fn(),
  pause: vi.fn(),
  resume: vi.fn(),
  setTrackListContext: vi.fn(),
};

let visiblePlayerContext: Array<{ id: number; title: string }> = [];

const toastShow = vi.fn();

const fetchDownloadCountMock = vi.fn();
const fetchDownloadHistoryMock = vi.fn();
const fetchDownloadHistoryTrackIdsMock = vi.fn();
const downloadTrackMock = vi.fn();
const triggerBlobDownloadMock = vi.fn();

vi.mock('@/api/downloads', () => ({
  downloadTrack: (...args: unknown[]) => downloadTrackMock(...args),
  triggerBlobDownload: (...args: unknown[]) => triggerBlobDownloadMock(...args),
  fetchDownloadCount: (...args: unknown[]) => fetchDownloadCountMock(...args),
  fetchDownloadHistory: (...args: unknown[]) => fetchDownloadHistoryMock(...args),
  fetchDownloadHistoryTrackIds: (...args: unknown[]) => fetchDownloadHistoryTrackIdsMock(...args),
}));

vi.mock('@/api/client', () => ({
  toUploadUrl: (path: string | null) => path,
}));

vi.mock('@/store/playerStore', () => ({
  usePlayerStore: (selector: (state: typeof playerState) => unknown) => selector(playerState),
}));

vi.mock('@/store/toastStore', () => ({
  useToastStore: (selector: (state: { show: typeof toastShow }) => unknown) =>
    selector({ show: toastShow }),
}));

function buildPageInfo(total: number): PageInfo {
  return {
    page: 1,
    size: 20,
    total,
    start: 1,
    end: 1,
    prev: false,
    next: false,
  };
}

function buildDownloadCount(): DownloadCount {
  return {
    todayDownloads: 1,
    dailyLimit: 10,
    remaining: 9,
    nextResetAt: '2026-04-18T00:00:00',
  };
}

function buildHistoryItem(overrides: Partial<DownloadHistoryItem> = {}): DownloadHistoryItem {
  return {
    downloadId: 1,
    trackId: 101,
    title: '테스트 트랙',
    artistName: 'ATStudio',
    thumbnail: null,
    bpm: 120,
    tonality: 'C Major',
    duration: 180,
    waveformData: '[0.2,0.8]',
    tags: [],
    downloadedAt: '2026-04-17T10:00:00',
    ...overrides,
  };
}

function AuthLayoutProbe({
  onLayout,
}: {
  onLayout?: (snapshot: {
    accessToken: string | null;
    bodyText: string;
    playerTitles: string[];
  }) => void;
}) {
  const accessToken = useAuthStore((state) => state.accessToken);

  useLayoutEffect(() => {
    onLayout?.({
      accessToken,
      bodyText: document.body.textContent ?? '',
      playerTitles: visiblePlayerContext.map((track) => track.title),
    });
  }, [accessToken, onLayout]);

  return null;
}

function renderPage(
  onLayout?: (snapshot: {
    accessToken: string | null;
    bodyText: string;
    playerTitles: string[];
  }) => void,
) {
  return render(
    <MemoryRouter
      initialEntries={['/downloads']}
      future={{ v7_startTransition: true, v7_relativeSplatPath: true }}
    >
      <HistorySwitcher />
      <DownloadHistoryPage />
      <AuthLayoutProbe onLayout={onLayout} />
    </MemoryRouter>,
  );
}

function HistorySwitcher() {
  const navigate = useNavigate();
  return <button onClick={() => navigate('/downloads?page=2')}>next history</button>;
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

describe('DownloadHistoryPage', () => {
  beforeEach(() => {
    playerState.currentTrack = null;
    playerState.isPlaying = false;
    playerState.play.mockReset();
    playerState.pause.mockReset();
    playerState.resume.mockReset();
    playerState.setTrackListContext.mockReset();
    visiblePlayerContext = [];
    playerState.setTrackListContext.mockImplementation((tracks) => {
      const context = tracks as Array<{ id: number; title: string }>;
      visiblePlayerContext = context;
      return () => {
        if (visiblePlayerContext === context) visiblePlayerContext = [];
      };
    });
    useAuthStore.setState({
      user: { id: 1 } as User,
      accessToken: 'owner-one-token',
      role: 'USER',
    });
    toastShow.mockReset();

    fetchDownloadCountMock.mockReset();
    fetchDownloadHistoryMock.mockReset();
    fetchDownloadHistoryTrackIdsMock.mockReset();
    downloadTrackMock.mockReset();
    triggerBlobDownloadMock.mockReset();

    fetchDownloadCountMock.mockResolvedValue(buildDownloadCount());
    fetchDownloadHistoryTrackIdsMock.mockResolvedValue([]);
    downloadTrackMock.mockResolvedValue(new Blob(['audio']));
  });

  it('renders the download history empty state', async () => {
    fetchDownloadHistoryMock.mockResolvedValue({
      dataList: [],
      pageInfo: buildPageInfo(0),
    });

    renderPage();

    await waitFor(() => {
      expect(fetchDownloadHistoryMock).toHaveBeenCalled();
    });

    expect(screen.getByText('다운로드 기록')).toBeInTheDocument();
    expect(screen.getByText('다운로드 기록이 없습니다.')).toBeInTheDocument();
    expect(screen.getByRole('link', { name: '음원 둘러보기' })).toBeInTheDocument();
  });

  it('deduplicates selected track ids in the bulk re-download label', async () => {
    fetchDownloadHistoryMock.mockResolvedValue({
      dataList: [
        buildHistoryItem({ downloadId: 1, trackId: 101, title: '트랙 A' }),
        buildHistoryItem({ downloadId: 2, trackId: 101, title: '트랙 A 재다운로드' }),
        buildHistoryItem({ downloadId: 3, trackId: 202, title: '트랙 B' }),
      ],
      pageInfo: buildPageInfo(3),
    });

    renderPage();

    await waitFor(() => {
      expect(screen.getByText('트랙 A')).toBeInTheDocument();
    });

    fireEvent.click(screen.getByRole('checkbox', { name: 'Select 트랙 A' }));
    fireEvent.click(screen.getByRole('checkbox', { name: 'Select 트랙 A 재다운로드' }));

    fireEvent.click(screen.getByRole('button', { name: '선택 재다운로드 (1)' }));

    await waitFor(() => expect(downloadTrackMock).toHaveBeenCalledTimes(1));
    expect(downloadTrackMock).toHaveBeenCalledWith(101, expect.any(AbortSignal));
    expect(downloadTrackMock).not.toHaveBeenCalledWith(1, expect.anything());
    expect(downloadTrackMock).not.toHaveBeenCalledWith(2, expect.anything());
  });

  it('provides stable names for history filters and track actions', async () => {
    fetchDownloadHistoryMock.mockResolvedValue({
      dataList: [buildHistoryItem({ title: 'Accessible track' })],
      pageInfo: buildPageInfo(1),
    });

    renderPage();

    expect(
      await screen.findByRole('checkbox', { name: 'Select all download history items' }),
    ).toBeInTheDocument();
    expect(screen.getByRole('textbox', { name: '다운로드 기록 검색' })).toBeInTheDocument();
    expect(screen.getByRole('combobox', { name: '다운로드 기록 정렬' })).toBeInTheDocument();
    expect(screen.getByRole('checkbox', { name: 'Select Accessible track' })).toBeInTheDocument();

    const playButton = screen.getByRole('button', { name: 'Play Accessible track' });
    playButton.focus();
    fireEvent.keyDown(playButton, { key: 'Enter' });
    fireEvent.click(playButton);

    expect(playerState.play).toHaveBeenCalledWith(expect.objectContaining({ id: 101 }));

    const redownloadButton = screen.getByRole('button', {
      name: 'Accessible track 재다운로드',
    });
    expect(redownloadButton).toHaveAttribute('type', 'button');
  });

  it('opens a confirm dialog before full re-download', async () => {
    fetchDownloadHistoryMock.mockResolvedValue({
      dataList: [
        buildHistoryItem({ downloadId: 1, trackId: 101, title: '트랙 A' }),
        buildHistoryItem({ downloadId: 2, trackId: 202, title: '트랙 B' }),
      ],
      pageInfo: buildPageInfo(2),
    });
    fetchDownloadHistoryTrackIdsMock.mockResolvedValue([101, 202]);

    renderPage();

    await waitFor(() => {
      expect(screen.getByText('트랙 A')).toBeInTheDocument();
    });

    fireEvent.click(screen.getByRole('button', { name: '전체 재다운로드' }));

    expect(fetchDownloadHistoryTrackIdsMock).toHaveBeenCalledWith(
      undefined,
      expect.any(AbortSignal),
    );
    expect(await screen.findByText('2곡을 다운로드합니다. 계속하시겠습니까?')).toBeInTheDocument();
    expect(await screen.findByRole('button', { name: '다운로드' })).toBeInTheDocument();
  });

  it('ignores an old successful history response after the page changes', async () => {
    const first = deferred<{ dataList: DownloadHistoryItem[]; pageInfo: PageInfo }>();
    const second = deferred<{ dataList: DownloadHistoryItem[]; pageInfo: PageInfo }>();
    fetchDownloadHistoryMock.mockReturnValueOnce(first.promise).mockReturnValueOnce(second.promise);

    renderPage();
    const firstSignal = fetchDownloadHistoryMock.mock.calls[0][1] as AbortSignal;
    fireEvent.click(screen.getByRole('button', { name: 'next history' }));
    expect(firstSignal.aborted).toBe(true);

    await act(async () =>
      second.resolve({
        dataList: [buildHistoryItem({ downloadId: 2, title: 'Current history' })],
        pageInfo: { ...buildPageInfo(1), page: 2 },
      }),
    );
    expect(await screen.findByText('Current history')).toBeInTheDocument();

    await act(async () =>
      first.resolve({
        dataList: [buildHistoryItem({ downloadId: 1, title: 'Old history' })],
        pageInfo: buildPageInfo(1),
      }),
    );
    expect(screen.getByText('Current history')).toBeInTheDocument();
    expect(screen.queryByText('Old history')).not.toBeInTheDocument();
  });

  it('ignores an old failed history response after the current response succeeds', async () => {
    const first = deferred<{ dataList: DownloadHistoryItem[]; pageInfo: PageInfo }>();
    const second = deferred<{ dataList: DownloadHistoryItem[]; pageInfo: PageInfo }>();
    fetchDownloadHistoryMock.mockReturnValueOnce(first.promise).mockReturnValueOnce(second.promise);

    renderPage();
    fireEvent.click(screen.getByRole('button', { name: 'next history' }));
    await act(async () =>
      second.resolve({
        dataList: [buildHistoryItem({ downloadId: 2, title: 'Current after failure' })],
        pageInfo: { ...buildPageInfo(1), page: 2 },
      }),
    );
    expect(await screen.findByText('Current after failure')).toBeInTheDocument();

    await act(async () => first.reject(new Error('old failure')));
    expect(screen.getByText('Current after failure')).toBeInTheDocument();
    expect(screen.queryByText('다운로드 기록을 불러오지 못했습니다.')).not.toBeInTheDocument();
  });

  it('clears history projection and selection while a same-role replacement owner loads', async () => {
    const replacementHistory = deferred<{
      dataList: DownloadHistoryItem[];
      pageInfo: PageInfo;
    }>();
    const replacementCount = deferred<DownloadCount>();
    fetchDownloadHistoryMock
      .mockResolvedValueOnce({
        dataList: [buildHistoryItem({ title: '이전 사용자 기록' })],
        pageInfo: buildPageInfo(1),
      })
      .mockReturnValueOnce(replacementHistory.promise);
    fetchDownloadCountMock
      .mockResolvedValueOnce(buildDownloadCount())
      .mockReturnValueOnce(replacementCount.promise);

    renderPage();

    fireEvent.click(await screen.findByRole('checkbox', { name: 'Select 이전 사용자 기록' }));
    expect(screen.getByRole('button', { name: '선택 재다운로드 (1)' })).toBeEnabled();
    expect(screen.getByText('오늘 1 / 10곡 (남은 횟수: 9)')).toBeInTheDocument();

    act(() => {
      useAuthStore.setState({
        user: { id: 2 } as User,
        accessToken: 'owner-two-token',
        role: 'USER',
      });
    });

    await waitFor(() => expect(fetchDownloadHistoryMock).toHaveBeenCalledTimes(2));
    expect(screen.queryByText('이전 사용자 기록')).not.toBeInTheDocument();
    expect(screen.queryByText('오늘 1 / 10곡 (남은 횟수: 9)')).not.toBeInTheDocument();
    expect(screen.getByRole('button', { name: '선택 재다운로드' })).toBeDisabled();
    expect(screen.getByText('불러오는 중...')).toBeInTheDocument();

    await act(async () => {
      replacementHistory.resolve({
        dataList: [buildHistoryItem({ downloadId: 2, title: '현재 사용자 기록' })],
        pageInfo: buildPageInfo(1),
      });
      replacementCount.resolve({
        todayDownloads: 2,
        dailyLimit: 20,
        remaining: 18,
        nextResetAt: '2026-04-18T00:00:00',
      });
    });

    expect(await screen.findByText('현재 사용자 기록')).toBeInTheDocument();
  });

  it('hides token-retired rows, selection, count, dialog, and player context before effects', async () => {
    fetchDownloadHistoryMock.mockResolvedValue({
      dataList: [buildHistoryItem({ title: 'Retired token history' })],
      pageInfo: buildPageInfo(1),
    });
    fetchDownloadHistoryTrackIdsMock.mockResolvedValue([101]);
    const observations: Array<{
      accessToken: string | null;
      bodyText: string;
      playerTitles: string[];
    }> = [];
    const onLayout = (snapshot: (typeof observations)[number]) => observations.push(snapshot);

    renderPage(onLayout);
    fireEvent.click(await screen.findByRole('checkbox', { name: 'Select Retired token history' }));
    fireEvent.click(screen.getByRole('button', { name: '전체 재다운로드' }));
    expect(await screen.findByText('1곡을 다운로드합니다. 계속하시겠습니까?')).toBeInTheDocument();
    await waitFor(() =>
      expect(visiblePlayerContext.map((track) => track.title)).toEqual(['Retired token history']),
    );
    observations.length = 0;

    act(() => {
      useAuthStore.setState({ accessToken: 'owner-one-token-replaced' });
    });

    const replacementCommit = observations.find(
      ({ accessToken }) => accessToken === 'owner-one-token-replaced',
    );
    expect(replacementCommit).toBeDefined();
    expect(replacementCommit?.bodyText).not.toContain('Retired token history');
    expect(replacementCommit?.bodyText).not.toContain('오늘 1 / 10곡');
    expect(replacementCommit?.bodyText).not.toContain('1곡을 다운로드합니다.');
    expect(replacementCommit?.playerTitles).toEqual([]);
  });

  it('suppresses delayed all-target preparation after owner replacement', async () => {
    const trackIDs = deferred<number[]>();
    fetchDownloadHistoryMock.mockResolvedValue({
      dataList: [buildHistoryItem({ title: 'Prepared by old owner' })],
      pageInfo: buildPageInfo(1),
    });
    fetchDownloadHistoryTrackIdsMock.mockReturnValue(trackIDs.promise);

    renderPage();
    fireEvent.click(await screen.findByRole('button', { name: '전체 재다운로드' }));
    await waitFor(() => expect(fetchDownloadHistoryTrackIdsMock).toHaveBeenCalledTimes(1));
    const preparationSignal = fetchDownloadHistoryTrackIdsMock.mock.calls[0][1] as AbortSignal;

    act(() => {
      useAuthStore.setState({ accessToken: 'replacement-token' });
    });
    expect(preparationSignal.aborted).toBe(true);
    await act(async () => trackIDs.resolve([101, 202]));

    expect(screen.queryByText('2곡을 다운로드합니다. 계속하시겠습니까?')).not.toBeInTheDocument();
    expect(toastShow).not.toHaveBeenCalledWith('error', '전체 재다운로드에 실패했습니다.');
  });

  it('stops a delayed selected download loop and its effects at owner replacement', async () => {
    const firstDownload = deferred<Blob>();
    fetchDownloadHistoryMock.mockResolvedValue({
      dataList: [
        buildHistoryItem({ downloadId: 1, trackId: 101, title: 'Old owner A' }),
        buildHistoryItem({ downloadId: 2, trackId: 202, title: 'Old owner B' }),
      ],
      pageInfo: buildPageInfo(2),
    });
    downloadTrackMock.mockReturnValueOnce(firstDownload.promise);

    renderPage();
    fireEvent.click(
      await screen.findByRole('checkbox', { name: 'Select all download history items' }),
    );
    fireEvent.click(screen.getByRole('button', { name: '선택 재다운로드 (2)' }));
    await waitFor(() => expect(downloadTrackMock).toHaveBeenCalledTimes(1));
    const downloadSignal = downloadTrackMock.mock.calls[0][1] as AbortSignal;

    act(() => {
      useAuthStore.setState({ accessToken: 'replacement-token' });
    });
    expect(downloadSignal.aborted).toBe(true);
    await act(async () => firstDownload.resolve(new Blob(['retired-audio'])));

    expect(downloadTrackMock).toHaveBeenCalledTimes(1);
    expect(triggerBlobDownloadMock).not.toHaveBeenCalled();
    expect(toastShow).not.toHaveBeenCalledWith('success', expect.any(String));
    expect(toastShow).not.toHaveBeenCalledWith('error', expect.stringContaining('곡 성공'));
  });

  it('aborts and suppresses a delayed post-download count refresh', async () => {
    const refreshedCount = deferred<DownloadCount>();
    fetchDownloadHistoryMock.mockResolvedValue({
      dataList: [buildHistoryItem({ title: 'Count refresh owner' })],
      pageInfo: buildPageInfo(1),
    });
    fetchDownloadCountMock
      .mockResolvedValueOnce(buildDownloadCount())
      .mockReturnValueOnce(refreshedCount.promise)
      .mockReturnValueOnce(new Promise<DownloadCount>(() => {}));

    renderPage();
    fireEvent.click(await screen.findByRole('checkbox', { name: 'Select Count refresh owner' }));
    fireEvent.click(screen.getByRole('button', { name: '선택 재다운로드 (1)' }));
    await waitFor(() => expect(fetchDownloadCountMock).toHaveBeenCalledTimes(2));
    const refreshSignal = fetchDownloadCountMock.mock.calls[1][0] as AbortSignal;

    act(() => {
      useAuthStore.setState({ accessToken: 'replacement-token' });
    });
    expect(refreshSignal.aborted).toBe(true);
    await act(async () =>
      refreshedCount.resolve({
        todayDownloads: 9,
        dailyLimit: 10,
        remaining: 1,
        nextResetAt: '2026-04-18T00:00:00',
      }),
    );

    expect(screen.queryByText('오늘 9 / 10곡 (남은 횟수: 1)')).not.toBeInTheDocument();
  });

  it('aborts and ignores a same-role retired owner completion', async () => {
    const retiredHistory = deferred<{
      dataList: DownloadHistoryItem[];
      pageInfo: PageInfo;
    }>();
    const currentHistory = deferred<{
      dataList: DownloadHistoryItem[];
      pageInfo: PageInfo;
    }>();
    fetchDownloadHistoryMock
      .mockReturnValueOnce(retiredHistory.promise)
      .mockReturnValueOnce(currentHistory.promise);

    renderPage();
    await waitFor(() => expect(fetchDownloadHistoryMock).toHaveBeenCalledTimes(1));
    const retiredSignal = fetchDownloadHistoryMock.mock.calls[0][1] as AbortSignal;

    act(() => {
      useAuthStore.setState({
        user: { id: 2 } as User,
        accessToken: 'owner-two-token',
        role: 'USER',
      });
    });

    await waitFor(() => expect(fetchDownloadHistoryMock).toHaveBeenCalledTimes(2));
    expect(retiredSignal.aborted).toBe(true);

    await act(async () =>
      currentHistory.resolve({
        dataList: [buildHistoryItem({ downloadId: 2, title: '현재 소유자 기록' })],
        pageInfo: buildPageInfo(1),
      }),
    );
    expect(await screen.findByText('현재 소유자 기록')).toBeInTheDocument();

    await act(async () =>
      retiredHistory.resolve({
        dataList: [buildHistoryItem({ downloadId: 1, title: '폐기된 소유자 기록' })],
        pageInfo: buildPageInfo(1),
      }),
    );
    expect(screen.queryByText('폐기된 소유자 기록')).not.toBeInTheDocument();
    expect(screen.getByText('현재 소유자 기록')).toBeInTheDocument();
    expect(screen.queryByText('불러오는 중...')).not.toBeInTheDocument();
  });

  it('ignores a same-role retired owner failure and finally completion', async () => {
    const retiredHistory = deferred<{
      dataList: DownloadHistoryItem[];
      pageInfo: PageInfo;
    }>();
    const currentHistory = deferred<{
      dataList: DownloadHistoryItem[];
      pageInfo: PageInfo;
    }>();
    fetchDownloadHistoryMock
      .mockReturnValueOnce(retiredHistory.promise)
      .mockReturnValueOnce(currentHistory.promise);

    renderPage();
    await waitFor(() => expect(fetchDownloadHistoryMock).toHaveBeenCalledTimes(1));

    act(() => {
      useAuthStore.setState({
        user: { id: 2 } as User,
        accessToken: 'owner-two-token',
        role: 'USER',
      });
    });
    await waitFor(() => expect(fetchDownloadHistoryMock).toHaveBeenCalledTimes(2));

    await act(async () =>
      currentHistory.resolve({
        dataList: [buildHistoryItem({ downloadId: 2, title: '실패 이후 현재 기록' })],
        pageInfo: buildPageInfo(1),
      }),
    );
    expect(await screen.findByText('실패 이후 현재 기록')).toBeInTheDocument();

    await act(async () => retiredHistory.reject(new Error('retired owner failed')));
    expect(screen.getByText('실패 이후 현재 기록')).toBeInTheDocument();
    expect(screen.queryByText('다운로드 기록을 불러오지 못했습니다.')).not.toBeInTheDocument();
    expect(screen.queryByText('불러오는 중...')).not.toBeInTheDocument();
  });
});

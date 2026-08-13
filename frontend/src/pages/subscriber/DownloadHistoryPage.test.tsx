import { act, fireEvent, render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter, useNavigate } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import DownloadHistoryPage from '@/pages/subscriber/DownloadHistoryPage';
import type { DownloadCount, DownloadHistoryItem } from '@/api/downloads';
import type { PageInfo } from '@/types';

const playerState = {
  currentTrack: null,
  isPlaying: false,
  play: vi.fn(),
  pause: vi.fn(),
  resume: vi.fn(),
  setTrackListContext: vi.fn(),
};

const authState = {
  role: 'USER',
};

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

vi.mock('@/store/authStore', () => ({
  useAuthStore: (selector: (state: typeof authState) => unknown) => selector(authState),
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

function renderPage() {
  return render(
    <MemoryRouter
      initialEntries={['/downloads']}
      future={{ v7_startTransition: true, v7_relativeSplatPath: true }}
    >
      <HistorySwitcher />
      <DownloadHistoryPage />
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
    authState.role = 'USER';
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
    expect(downloadTrackMock).toHaveBeenCalledWith(101);
    expect(downloadTrackMock).not.toHaveBeenCalledWith(1);
    expect(downloadTrackMock).not.toHaveBeenCalledWith(2);
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

    expect(fetchDownloadHistoryTrackIdsMock).toHaveBeenCalledWith(undefined);
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
});

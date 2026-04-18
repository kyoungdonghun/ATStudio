import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import DownloadHistoryPage from '@/pages/subscriber/DownloadQueuePage';
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

function buildHistoryItem(
  overrides: Partial<DownloadHistoryItem> = {},
): DownloadHistoryItem {
  return {
    downloadId: 1,
    trackId: 101,
    title: '테스트 트랙',
    artistName: 'ATStudio',
    thumbnail: null,
    bpm: 120,
    tonality: 'C Major',
    duration: 180,
    tags: [],
    downloadedAt: '2026-04-17T10:00:00',
    ...overrides,
  };
}

function renderPage() {
  return render(
    <MemoryRouter initialEntries={['/download-queue']}>
      <DownloadHistoryPage />
    </MemoryRouter>,
  );
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

    const checkboxes = screen.getAllByRole('checkbox');
    fireEvent.click(checkboxes[1]);
    fireEvent.click(checkboxes[2]);

    expect(screen.getByRole('button', { name: '선택 재다운로드 (1)' })).toBeInTheDocument();
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
    expect(
      await screen.findByText('2곡을 다운로드합니다. 계속하시겠습니까?'),
    ).toBeInTheDocument();
    expect(await screen.findByRole('button', { name: '다운로드' })).toBeInTheDocument();
  });
});

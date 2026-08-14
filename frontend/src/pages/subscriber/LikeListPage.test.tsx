import { act, fireEvent, render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import LikeListPage from '@/pages/subscriber/LikeListPage';
import { useAuthStore } from '@/store/authStore';
import type { User } from '@/types';

const api = vi.hoisted(() => ({
  fetchLikes: vi.fn(),
  fetchAlbumLikes: vi.fn(),
  removeLike: vi.fn(),
  removeAlbumLike: vi.fn(),
  downloadTrack: vi.fn(),
  triggerBlobDownload: vi.fn(),
  getApiErrorCode: vi.fn(),
}));

const playerState = {
  currentTrack: null,
  isPlaying: false,
  play: vi.fn(),
  pause: vi.fn(),
  resume: vi.fn(),
  setTrackListContext: vi.fn(() => vi.fn()),
};

vi.mock('@/api/likes', () => api);
vi.mock('@/api/downloads', () => ({
  createDownloadFallbackFileName: (_resource: string, id: number, title: string) =>
    `track-${id}-${title}.mp3`,
  downloadTrack: api.downloadTrack,
  triggerBlobDownload: api.triggerBlobDownload,
}));
vi.mock('@/api/client', () => ({
  getApiErrorCode: api.getApiErrorCode,
  toUploadUrl: (path: string | null) => path,
}));
vi.mock('@/store/playerStore', () => ({
  usePlayerStore: (selector: (state: typeof playerState) => unknown) => selector(playerState),
}));
vi.mock('@/store/likeStore', () => ({
  useLikeStore: () => ({ remove: vi.fn() }),
}));
vi.mock('@/store/albumLikeStore', () => ({
  useAlbumLikeStore: () => ({ remove: vi.fn() }),
}));
vi.mock('@/store/toastStore', () => ({
  useToastStore: (selector: (state: { show: ReturnType<typeof vi.fn> }) => unknown) =>
    selector({ show: vi.fn() }),
}));

function deferred<T>() {
  let resolve!: (value: T) => void;
  let reject!: (reason?: unknown) => void;
  const promise = new Promise<T>((nextResolve, nextReject) => {
    resolve = nextResolve;
    reject = nextReject;
  });
  return { promise, resolve, reject };
}

function renderPage() {
  return render(
    <MemoryRouter
      initialEntries={['/likes']}
      future={{ v7_startTransition: true, v7_relativeSplatPath: true }}
    >
      <LikeListPage />
    </MemoryRouter>,
  );
}

describe('LikeListPage load ownership', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    useAuthStore.setState({
      user: { id: 1 } as User,
      accessToken: 'owner-token',
      role: 'USER',
    });
  });

  it('keeps the newest track-tab result after rapid tab changes and a stale failure', async () => {
    const oldTracks = deferred<{ dataList: [] }>();
    const albums = deferred<{ dataList: [] }>();
    const currentTracks = deferred<{
      dataList: Array<{
        trackId: number;
        title: string;
        createdAt: string;
        duration: number;
        thumbnail: null;
        waveformData: null;
        bpm: number;
        tonality: string;
      }>;
    }>();
    api.fetchLikes
      .mockReturnValueOnce(oldTracks.promise)
      .mockReturnValueOnce(currentTracks.promise);
    api.fetchAlbumLikes.mockReturnValueOnce(albums.promise);

    renderPage();
    await waitFor(() => expect(api.fetchLikes).toHaveBeenCalledTimes(1));
    const retiredSignal = api.fetchLikes.mock.calls[0][0] as AbortSignal;

    fireEvent.click(screen.getByRole('button', { name: '앨범' }));
    await waitFor(() => expect(api.fetchAlbumLikes).toHaveBeenCalledTimes(1));
    fireEvent.click(screen.getByRole('button', { name: '음원' }));
    await waitFor(() => expect(api.fetchLikes).toHaveBeenCalledTimes(2));
    expect(retiredSignal.aborted).toBe(true);

    await act(async () =>
      currentTracks.resolve({
        dataList: [
          {
            trackId: 2,
            title: '현재 좋아요',
            createdAt: '2026-08-13T00:00:00Z',
            duration: 120,
            thumbnail: null,
            waveformData: null,
            bpm: 120,
            tonality: 'C',
          },
        ],
      }),
    );
    expect(await screen.findByText('현재 좋아요')).toBeInTheDocument();

    await act(async () => oldTracks.reject(new Error('old failure')));
    expect(screen.getByText('현재 좋아요')).toBeInTheDocument();
    expect(screen.queryByText('좋아요 목록을 불러오지 못했습니다.')).not.toBeInTheDocument();
  });

  it('fences repeated liked-track downloads and releases the track after failure', async () => {
    const firstAttempt = deferred<never>();
    api.fetchLikes.mockResolvedValue({
      dataList: [
        {
          trackId: 7,
          title: 'Fence liked track',
          createdAt: '2026-08-13T00:00:00Z',
          duration: 120,
          thumbnail: null,
          waveformData: null,
          bpm: 120,
          tonality: 'C',
        },
      ],
    });
    api.downloadTrack
      .mockReturnValueOnce(firstAttempt.promise)
      .mockRejectedValueOnce(new Error('retry'));

    renderPage();
    await screen.findByText('Fence liked track');
    const downloadButton = screen.getByText('\u2193');
    fireEvent.click(downloadButton);
    fireEvent.click(downloadButton);

    expect(api.downloadTrack).toHaveBeenCalledTimes(1);
    expect(downloadButton).toBeDisabled();

    await act(async () => firstAttempt.reject(new Error('offline')));
    await waitFor(() => expect(downloadButton).toBeEnabled());

    fireEvent.click(downloadButton);
    await waitFor(() => expect(api.downloadTrack).toHaveBeenCalledTimes(2));
  });
});

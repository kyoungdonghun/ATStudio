import { act, fireEvent, render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter, Route, Routes, useNavigate } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import type { BinaryDownload } from '@/api/downloads';
import type { TrackDetail } from '@/api/tracks';
import type { PlayableTrack } from '@/types';
import TrackDetailPage from '@/pages/public/TrackDetailPage';

const mocks = vi.hoisted(() => ({
  fetchTrackDetail: vi.fn(),
  play: vi.fn(),
  pause: vi.fn(),
  resume: vi.fn(),
  toastShow: vi.fn(),
  downloadTrack: vi.fn(),
  triggerBlobDownload: vi.fn(),
  fetchDownloadCount: vi.fn(),
  getApiErrorCode: vi.fn(),
  authenticated: false,
  playerState: {
    currentTrack: null as PlayableTrack | null,
    isPlaying: false,
  },
}));

vi.mock('@/api/tracks', () => ({
  fetchTrackDetail: (...args: unknown[]) => mocks.fetchTrackDetail(...args),
}));

vi.mock('@/store/playerStore', () => ({
  usePlayerStore: (
    selector: (state: {
      currentTrack: PlayableTrack | null;
      isPlaying: boolean;
      play: typeof mocks.play;
      pause: typeof mocks.pause;
      resume: typeof mocks.resume;
    }) => unknown,
  ) =>
    selector({
      ...mocks.playerState,
      play: mocks.play,
      pause: mocks.pause,
      resume: mocks.resume,
    }),
}));

vi.mock('@/store/authStore', () => ({
  useAuthStore: (selector: (state: { isAuthenticated: () => boolean }) => unknown) =>
    selector({ isAuthenticated: () => mocks.authenticated }),
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
  useToastStore: (selector: (state: { show: ReturnType<typeof vi.fn> }) => unknown) =>
    selector({ show: mocks.toastShow }),
}));

vi.mock('@/api/downloads', () => ({
  createDownloadFallbackFileName: () => 'fallback-track.mp3',
  downloadTrack: (...args: unknown[]) => mocks.downloadTrack(...args),
  triggerBlobDownload: (...args: unknown[]) => mocks.triggerBlobDownload(...args),
  fetchDownloadCount: (...args: unknown[]) => mocks.fetchDownloadCount(...args),
}));

vi.mock('@/api/client', () => ({
  default: {},
  getApiErrorCode: (...args: unknown[]) => mocks.getApiErrorCode(...args),
  toUploadUrl: (path: string | null | undefined) => path,
}));

vi.mock('@/components/playlist/AddToPlaylistModal', () => ({
  default: () => null,
}));

const waveformData = '[0.15,0.7,0.35]';
const detail: TrackDetail = {
  id: 7,
  title: 'Waveform Contract',
  artistName: 'ATStudio',
  duration: 446,
  bpm: 120,
  tonality: 'C',
  description: 'Focused mapping fixture',
  audioFile: null,
  thumbnail: '/uploads/tracks/7.png',
  waveformData,
  isActive: true,
  playCount: 11,
  likeCount: 3,
  downloadCount: 2,
  tags: [{ id: 1, name: 'Shorts', type: 'USAGE' }],
  createdAt: '2026-07-16T00:00:00',
  updatedAt: '2026-07-16T01:00:00',
};

const expectedPlayerTrack: PlayableTrack = {
  id: detail.id,
  title: detail.title,
  artistName: detail.artistName,
  duration: detail.duration,
  thumbnail: detail.thumbnail,
  waveformData: detail.waveformData,
  bpm: detail.bpm,
  tonality: detail.tonality,
  tags: detail.tags,
};

function renderPage() {
  return render(
    <MemoryRouter initialEntries={['/tracks/7']}>
      <RouteSwitcher />
      <Routes>
        <Route path="/tracks/:trackId" element={<TrackDetailPage />} />
      </Routes>
    </MemoryRouter>,
  );
}

function RouteSwitcher() {
  const navigate = useNavigate();
  return <button onClick={() => navigate('/tracks/8')}>next track</button>;
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

async function clickPlay() {
  await screen.findByRole('heading', { name: detail.title });
  fireEvent.click(screen.getByRole('button', { name: /\u25b6/ }));
}

describe('TrackDetailPage player mapping', () => {
  beforeEach(() => {
    mocks.fetchTrackDetail.mockReset();
    mocks.fetchTrackDetail.mockResolvedValue(detail);
    mocks.play.mockReset();
    mocks.pause.mockReset();
    mocks.resume.mockReset();
    mocks.toastShow.mockReset();
    mocks.downloadTrack.mockReset();
    mocks.triggerBlobDownload.mockReset();
    mocks.fetchDownloadCount.mockReset();
    mocks.getApiErrorCode.mockReset();
    mocks.authenticated = false;
    mocks.downloadTrack.mockResolvedValue(binaryDownload());
    mocks.fetchDownloadCount.mockResolvedValue({ remaining: 4, dailyLimit: 5 });
    mocks.getApiErrorCode.mockResolvedValue(undefined);
    mocks.playerState.currentTrack = null;
    mocks.playerState.isPlaying = false;
  });

  it('forwards waveformData with the complete Track mapping on first play', async () => {
    renderPage();

    await clickPlay();

    expect(mocks.play).toHaveBeenCalledWith(expectedPlayerTrack);
    expect(mocks.resume).not.toHaveBeenCalled();
  });

  it('rehydrates a stale same-id Track when persisted waveformData is missing', async () => {
    mocks.playerState.currentTrack = { ...expectedPlayerTrack, waveformData: null };
    renderPage();

    await clickPlay();

    expect(mocks.play).toHaveBeenCalledWith(expectedPlayerTrack);
    expect(mocks.pause).not.toHaveBeenCalled();
    expect(mocks.resume).not.toHaveBeenCalled();
  });

  it('resumes the same Track when its waveformData already matches', async () => {
    mocks.playerState.currentTrack = expectedPlayerTrack;
    renderPage();

    await clickPlay();

    expect(mocks.resume).toHaveBeenCalledTimes(1);
    expect(mocks.play).not.toHaveBeenCalled();
  });

  it('ignores an old successful detail response after navigation', async () => {
    const first = deferred<TrackDetail>();
    const second = deferred<TrackDetail>();
    const nextDetail = { ...detail, id: 8, title: 'Current track' };
    mocks.fetchTrackDetail.mockReturnValueOnce(first.promise).mockReturnValueOnce(second.promise);

    renderPage();
    const firstSignal = mocks.fetchTrackDetail.mock.calls[0][1] as AbortSignal;
    fireEvent.click(screen.getByRole('button', { name: 'next track' }));
    expect(firstSignal.aborted).toBe(true);

    await act(async () => second.resolve(nextDetail));
    expect(await screen.findByRole('heading', { name: nextDetail.title })).toBeInTheDocument();

    await act(async () => first.resolve(detail));
    expect(screen.getByRole('heading', { name: nextDetail.title })).toBeInTheDocument();
    expect(screen.queryByRole('heading', { name: detail.title })).not.toBeInTheDocument();
  });

  it('ignores an old failed detail response after the current response succeeds', async () => {
    const first = deferred<TrackDetail>();
    const second = deferred<TrackDetail>();
    const nextDetail = { ...detail, id: 8, title: 'Current after stale failure' };
    mocks.fetchTrackDetail.mockReturnValueOnce(first.promise).mockReturnValueOnce(second.promise);

    renderPage();
    fireEvent.click(screen.getByRole('button', { name: 'next track' }));
    await act(async () => second.resolve(nextDetail));
    expect(await screen.findByRole('heading', { name: nextDetail.title })).toBeInTheDocument();

    await act(async () => first.reject(new Error('old failure')));
    expect(screen.getByRole('heading', { name: nextDetail.title })).toBeInTheDocument();
    expect(screen.queryByText('Failed to load track')).not.toBeInTheDocument();
  });

  it('shows localized missing recovery without raw server text and bounds retry clicks', async () => {
    const retry = deferred<TrackDetail>();
    mocks.fetchTrackDetail
      .mockRejectedValueOnce({ response: { status: 404, data: { message: 'raw detail' } } })
      .mockReturnValueOnce(retry.promise);
    renderPage();

    expect(await screen.findByRole('alert')).toHaveTextContent('음원을 찾을 수 없습니다');
    expect(screen.queryByText('raw detail')).not.toBeInTheDocument();
    const retryButton = screen.getByRole('button', { name: '다시 시도' });
    fireEvent.click(retryButton);
    fireEvent.click(retryButton);
    expect(mocks.fetchTrackDetail).toHaveBeenCalledTimes(2);

    await act(async () => retry.resolve(detail));
    expect(await screen.findByRole('heading', { name: detail.title })).toBeInTheDocument();
  });

  it('fences rapid same-Track downloads and releases ownership after failure and success', async () => {
    const failed = deferred<BinaryDownload>();
    const succeeded = deferred<BinaryDownload>();
    mocks.authenticated = true;
    mocks.downloadTrack
      .mockReturnValueOnce(failed.promise)
      .mockReturnValueOnce(succeeded.promise)
      .mockResolvedValue(binaryDownload());
    renderPage();

    const downloadButton = await screen.findByRole('button', { name: '다운로드' });
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
});

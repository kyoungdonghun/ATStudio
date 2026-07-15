import { fireEvent, render, screen } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import type { TrackDetail } from '@/api/tracks';
import type { Track } from '@/types';
import TrackDetailPage from '@/pages/public/TrackDetailPage';

const mocks = vi.hoisted(() => ({
  fetchTrackDetail: vi.fn(),
  play: vi.fn(),
  pause: vi.fn(),
  resume: vi.fn(),
  playerState: {
    currentTrack: null as Track | null,
    isPlaying: false,
  },
}));

vi.mock('@/api/tracks', () => ({
  fetchTrackDetail: (...args: unknown[]) => mocks.fetchTrackDetail(...args),
}));

vi.mock('@/store/playerStore', () => ({
  usePlayerStore: (
    selector: (state: {
      currentTrack: Track | null;
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
    selector({ isAuthenticated: () => false }),
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
    selector({ show: vi.fn() }),
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

const expectedPlayerTrack: Track = { ...detail };

function renderPage() {
  return render(
    <MemoryRouter initialEntries={['/tracks/7']}>
      <Routes>
        <Route path="/tracks/:trackId" element={<TrackDetailPage />} />
      </Routes>
    </MemoryRouter>,
  );
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
});

import { act, fireEvent, render, screen, waitFor, within } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import type { AlbumDetail } from '@/api/albums';
import AlbumEditPage from './AlbumEditPage';

const mocks = vi.hoisted(() => ({
  fetchAlbumDetail: vi.fn(),
  reorderAlbumTracks: vi.fn(),
  toast: vi.fn(),
}));

vi.mock('@/api/albums', () => ({
  fetchAlbumDetail: (...args: unknown[]) => mocks.fetchAlbumDetail(...args),
  updateAlbum: vi.fn(),
  addTrackToAlbum: vi.fn(),
  removeTrackFromAlbum: vi.fn(),
  reorderAlbumTracks: (...args: unknown[]) => mocks.reorderAlbumTracks(...args),
}));

vi.mock('@/api/tracks', () => ({
  fetchTracks: vi.fn(),
}));

vi.mock('@/api/client', () => ({
  toUploadUrl: (path: string | null | undefined) => path,
}));

vi.mock('@/store/toastStore', () => ({
  useToastStore: (selector: (state: { show: typeof mocks.toast }) => unknown) =>
    selector({ show: mocks.toast }),
}));

const initialAlbum: AlbumDetail = {
  id: 11,
  title: 'Night Drive',
  description: 'City music',
  thumbnailUrl: null,
  likeCount: 0,
  createdAt: '2026-08-01T00:00:00Z',
  tracks: [
    {
      trackId: 21,
      title: 'First Track',
      artistName: 'Creator',
      duration: 120,
      order: 0,
    },
    {
      trackId: 22,
      title: 'Second Track',
      artistName: 'Creator',
      duration: 130,
      order: 1,
    },
    {
      trackId: 23,
      title: 'Third Track',
      artistName: 'Creator',
      duration: 140,
      order: 2,
    },
  ],
};

function albumWithOrder(trackIds: number[]): AlbumDetail {
  const tracksById = new Map(initialAlbum.tracks.map((track) => [track.trackId, track]));
  return {
    ...initialAlbum,
    tracks: trackIds.map((trackId, order) => ({ ...tracksById.get(trackId)!, order })),
  };
}

function renderPage() {
  return render(
    <MemoryRouter
      initialEntries={['/admin/albums/11/edit']}
      future={{ v7_startTransition: true, v7_relativeSplatPath: true }}
    >
      <Routes>
        <Route path="/admin/albums/:albumId/edit" element={<AlbumEditPage />} />
      </Routes>
    </MemoryRouter>,
  );
}

function trackTitles() {
  const section = screen.getByRole('heading', { name: '앨범 트랙' }).parentElement!;
  return within(section)
    .getAllByRole('listitem')
    .map((item) => within(item).getByText(/Track$/).textContent);
}

describe('AlbumEditPage track reorder', () => {
  beforeEach(() => {
    vi.resetAllMocks();
    mocks.fetchAlbumDetail.mockResolvedValue(initialAlbum);
    mocks.reorderAlbumTracks.mockResolvedValue(undefined);
  });

  it('sends every member once with zero-based contiguous orders and adopts one authoritative refetch', async () => {
    let resolveReorder!: () => void;
    const pendingReorder = new Promise<void>((resolve) => {
      resolveReorder = resolve;
    });
    mocks.reorderAlbumTracks.mockReturnValueOnce(pendingReorder);
    mocks.fetchAlbumDetail
      .mockResolvedValueOnce(initialAlbum)
      .mockResolvedValueOnce(albumWithOrder([23, 22, 21]));

    renderPage();
    expect(await screen.findByText('First Track')).toBeInTheDocument();

    fireEvent.click(screen.getAllByTitle('아래로')[0]);

    expect(mocks.reorderAlbumTracks).toHaveBeenCalledWith(11, [
      { trackId: 22, order: 0 },
      { trackId: 21, order: 1 },
      { trackId: 23, order: 2 },
    ]);
    expect(trackTitles()).toEqual(['Second Track', 'First Track', 'Third Track']);
    expect(mocks.fetchAlbumDetail).toHaveBeenCalledTimes(1);
    expect(screen.getAllByTitle('아래로')[0]).toBeDisabled();

    fireEvent.click(screen.getAllByTitle('아래로')[0]);
    expect(mocks.reorderAlbumTracks).toHaveBeenCalledTimes(1);

    await act(async () => resolveReorder());

    await waitFor(() => expect(mocks.fetchAlbumDetail).toHaveBeenCalledTimes(2));
    await waitFor(() =>
      expect(trackTitles()).toEqual(['Third Track', 'Second Track', 'First Track']),
    );
    expect(mocks.reorderAlbumTracks).toHaveBeenCalledTimes(1);
  });

  it('refetches once and adopts the authoritative order after success', async () => {
    mocks.fetchAlbumDetail
      .mockResolvedValueOnce(initialAlbum)
      .mockResolvedValueOnce(albumWithOrder([23, 22, 21]));

    renderPage();
    expect(await screen.findByText('First Track')).toBeInTheDocument();

    const firstDown = screen
      .getAllByRole('button')
      .find((button) => button.textContent === '\u25BC');
    expect(firstDown).toBeDefined();
    fireEvent.click(firstDown!);

    expect(trackTitles()).toEqual(['Second Track', 'First Track', 'Third Track']);
    expect(mocks.reorderAlbumTracks).toHaveBeenCalledTimes(1);
    await waitFor(() => expect(mocks.fetchAlbumDetail).toHaveBeenCalledTimes(2));
    await waitFor(() =>
      expect(trackTitles()).toEqual(['Third Track', 'Second Track', 'First Track']),
    );
  });

  it('does not request a reorder for top-up or bottom-down boundaries', async () => {
    renderPage();
    expect(await screen.findByText('First Track')).toBeInTheDocument();

    const upButtons = screen.getAllByTitle('위로');
    const downButtons = screen.getAllByTitle('아래로');
    expect(upButtons[0]).toBeDisabled();
    expect(downButtons[downButtons.length - 1]).toBeDisabled();

    fireEvent.click(upButtons[0]);
    fireEvent.click(downButtons[downButtons.length - 1]);

    expect(mocks.reorderAlbumTracks).not.toHaveBeenCalled();
    expect(mocks.fetchAlbumDetail).toHaveBeenCalledTimes(1);
  });

  it('reports rejection without retry and adopts one authoritative recovery refetch', async () => {
    mocks.reorderAlbumTracks.mockRejectedValueOnce(new Error('reorder failed'));
    mocks.fetchAlbumDetail
      .mockResolvedValueOnce(initialAlbum)
      .mockResolvedValueOnce(albumWithOrder([21, 23, 22]));

    renderPage();
    expect(await screen.findByText('First Track')).toBeInTheDocument();

    fireEvent.click(screen.getAllByTitle('아래로')[0]);

    await waitFor(() => expect(mocks.toast).toHaveBeenCalledWith('error', 'reorder failed'));
    await waitFor(() => expect(mocks.fetchAlbumDetail).toHaveBeenCalledTimes(2));
    expect(trackTitles()).toEqual(['First Track', 'Third Track', 'Second Track']);
    expect(mocks.reorderAlbumTracks).toHaveBeenCalledTimes(1);
  });
});

import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { createMemoryRouter, RouterProvider } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import PlaylistEditPage from '@/pages/subscriber/PlaylistEditPage';

const mocks = vi.hoisted(() => ({
  fetchPlaylistDetail: vi.fn(),
  updatePlaylist: vi.fn(),
  deletePlaylist: vi.fn(),
  removeTrackFromPlaylist: vi.fn(),
  reorderTracks: vi.fn(),
}));

vi.mock('@/api/playlists', () => ({
  fetchPlaylistDetail: mocks.fetchPlaylistDetail,
  updatePlaylist: mocks.updatePlaylist,
  deletePlaylist: mocks.deletePlaylist,
  removeTrackFromPlaylist: mocks.removeTrackFromPlaylist,
  reorderTracks: mocks.reorderTracks,
}));

vi.mock('@/api/client', () => ({
  toUploadUrl: (path: string | null | undefined) => path,
}));

function renderPage() {
  const router = createMemoryRouter(
    [
      { path: '/playlists/:playlistId/edit', element: <PlaylistEditPage /> },
      { path: '/playlists/:playlistId', element: <div>Playlist detail</div> },
    ],
    { initialEntries: ['/playlists/41/edit'] },
  );

  render(<RouterProvider router={router} future={{ v7_startTransition: true }} />);
  return router;
}

describe('PlaylistEditPage', () => {
  beforeEach(() => {
    vi.resetAllMocks();
    mocks.fetchPlaylistDetail.mockResolvedValue({
      id: 41,
      title: 'Focus Mix',
      description: 'Work tracks',
      thumbnail: null,
      tracks: [
        {
          trackOrder: 0,
          trackId: 101,
          title: 'First Track',
          artistName: 'Artist One',
          duration: 120,
          bpm: 100,
          tonality: 'C',
        },
        {
          trackOrder: 1,
          trackId: 102,
          title: 'Second Track',
          artistName: 'Artist Two',
          duration: 130,
          bpm: 110,
          tonality: 'D',
        },
        {
          trackOrder: 2,
          trackId: 103,
          title: 'Third Track',
          artistName: 'Artist Three',
          duration: 140,
          bpm: 120,
          tonality: 'E',
        },
      ],
      createdAt: '2026-08-01T00:00:00Z',
      updatedAt: '2026-08-02T00:00:00Z',
    });
    mocks.updatePlaylist.mockResolvedValue(undefined);
    mocks.reorderTracks.mockResolvedValue(undefined);
  });

  it('submits the reordered non-empty playlist with zero-based contiguous track orders', async () => {
    const router = renderPage();

    expect(await screen.findByText('First Track')).toBeInTheDocument();
    fireEvent.click(screen.getAllByRole('button', { name: 'Move down' })[0]);
    fireEvent.click(screen.getByRole('button', { name: '저장' }));

    await waitFor(() => {
      expect(mocks.reorderTracks).toHaveBeenCalledWith(41, [
        { trackId: 102, trackOrder: 0 },
        { trackId: 101, trackOrder: 1 },
        { trackId: 103, trackOrder: 2 },
      ]);
    });
    expect(mocks.updatePlaylist).not.toHaveBeenCalled();
    await waitFor(() => expect(router.state.location.pathname).toBe('/playlists/41'));
  });
});

import { act, cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react';
import { createMemoryRouter, RouterProvider, useNavigate } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import PlaylistEditPage from '@/pages/subscriber/PlaylistEditPage';
import { useAuthStore } from '@/store/authStore';
import type { User } from '@/types';

const mocks = vi.hoisted(() => ({
  fetchPlaylistDetail: vi.fn(),
  updatePlaylist: vi.fn(),
  deletePlaylist: vi.fn(),
  removeTrackFromPlaylist: vi.fn(),
  reorderTracks: vi.fn(),
}));
const createObjectURLMock = vi.fn();
const revokeObjectURLMock = vi.fn();

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

function EditHarness() {
  const navigate = useNavigate();
  return (
    <>
      <button type="button" onClick={() => navigate('/playlists/42/edit')}>
        next edit
      </button>
      <PlaylistEditPage />
    </>
  );
}

function renderPage(initialEntry = '/playlists/41/edit') {
  const router = createMemoryRouter(
    [
      { path: '/playlists/:playlistId/edit', element: <EditHarness /> },
      { path: '/playlists/:playlistId', element: <div>Playlist detail</div> },
      { path: '/playlists', element: <div>Playlist list</div> },
    ],
    { initialEntries: [initialEntry] },
  );

  render(<RouterProvider router={router} future={{ v7_startTransition: true }} />);
  return router;
}

function deferred<T>() {
  let resolve!: (value: T) => void;
  const promise = new Promise<T>((nextResolve) => {
    resolve = nextResolve;
  });
  return { promise, resolve };
}

describe('PlaylistEditPage', () => {
  beforeEach(() => {
    vi.resetAllMocks();
    Object.defineProperty(URL, 'createObjectURL', {
      configurable: true,
      value: createObjectURLMock,
    });
    Object.defineProperty(URL, 'revokeObjectURL', {
      configurable: true,
      value: revokeObjectURLMock,
    });
    useAuthStore.setState({
      user: { id: 1 } as User,
      accessToken: 'owner-token',
      role: 'USER',
    });
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

  it.each(['1e3', '0x10', '+7', ' 7', '7 ', '7.5', '0', '-1', '9007199254740992', 'abc'])(
    'rejects noncanonical edit id %s without a request',
    (id) => {
      mocks.fetchPlaylistDetail.mockReset();
      renderPage(`/playlists/${id}/edit`);

      expect(screen.getByText('재생목록 주소가 올바르지 않습니다.')).toBeInTheDocument();
      expect(screen.getByRole('link', { name: '재생목록 목록으로' })).toHaveAttribute(
        'href',
        '/playlists',
      );
      expect(mocks.fetchPlaylistDetail).not.toHaveBeenCalled();
    },
  );

  it('ignores a stale edit load after the route owner changes', async () => {
    const oldDetail = deferred<Awaited<ReturnType<typeof mocks.fetchPlaylistDetail>>>();
    const currentDetail = deferred<Awaited<ReturnType<typeof mocks.fetchPlaylistDetail>>>();
    mocks.fetchPlaylistDetail
      .mockReset()
      .mockReturnValueOnce(oldDetail.promise)
      .mockReturnValueOnce(currentDetail.promise);

    renderPage();
    await waitFor(() => expect(mocks.fetchPlaylistDetail).toHaveBeenCalledTimes(1));
    const oldSignal = mocks.fetchPlaylistDetail.mock.calls[0][1] as AbortSignal;
    fireEvent.click(screen.getByRole('button', { name: 'next edit' }));
    await waitFor(() => expect(mocks.fetchPlaylistDetail).toHaveBeenCalledTimes(2));
    expect(oldSignal.aborted).toBe(true);

    await act(async () =>
      currentDetail.resolve({
        id: 42,
        title: 'Current Edit',
        description: null,
        thumbnail: null,
        tracks: [],
        createdAt: '',
        updatedAt: '',
      }),
    );
    expect(await screen.findByDisplayValue('Current Edit')).toBeInTheDocument();

    await act(async () =>
      oldDetail.resolve({
        id: 41,
        title: 'Old Edit',
        description: null,
        thumbnail: null,
        tracks: [],
        createdAt: '',
        updatedAt: '',
      }),
    );
    expect(screen.queryByDisplayValue('Old Edit')).not.toBeInTheDocument();
    expect(screen.getByDisplayValue('Current Edit')).toBeInTheDocument();
  });

  it('revokes only local thumbnail previews once on replace, removal, route change, and unmount', async () => {
    createObjectURLMock
      .mockReturnValueOnce('blob:edit-a')
      .mockReturnValueOnce('blob:edit-b')
      .mockReturnValueOnce('blob:edit-c')
      .mockReturnValueOnce('blob:edit-d');
    mocks.fetchPlaylistDetail.mockResolvedValue({
      id: 41,
      title: 'Focus Mix',
      description: 'Work tracks',
      thumbnail: '/backend-thumbnail.jpg',
      tracks: [],
      createdAt: '2026-08-01T00:00:00Z',
      updatedAt: '2026-08-02T00:00:00Z',
    });
    renderPage();
    expect(await screen.findByDisplayValue('Focus Mix')).toBeInTheDocument();

    let fileInput = document.querySelector('input[type="file"]') as HTMLInputElement;
    fireEvent.change(fileInput, { target: { files: [new File(['a'], 'a.png')] } });
    fileInput = document.querySelector('input[type="file"]') as HTMLInputElement;
    fireEvent.change(fileInput, { target: { files: [new File(['b'], 'b.png')] } });
    expect(revokeObjectURLMock).toHaveBeenCalledWith('blob:edit-a');

    fileInput = document.querySelector('input[type="file"]') as HTMLInputElement;
    fireEvent.change(fileInput, { target: { files: [] } });
    expect(revokeObjectURLMock).toHaveBeenCalledWith('blob:edit-b');

    fileInput = document.querySelector('input[type="file"]') as HTMLInputElement;
    fireEvent.change(fileInput, { target: { files: [new File(['c'], 'c.png')] } });
    fireEvent.click(screen.getByRole('button', { name: 'next edit' }));
    await waitFor(() => expect(mocks.fetchPlaylistDetail).toHaveBeenCalledTimes(2));
    expect(revokeObjectURLMock).toHaveBeenCalledWith('blob:edit-c');

    fileInput = document.querySelector('input[type="file"]') as HTMLInputElement;
    fireEvent.change(fileInput, { target: { files: [new File(['d'], 'd.png')] } });
    cleanup();

    expect(revokeObjectURLMock.mock.calls.map(([url]) => url)).toEqual([
      'blob:edit-a',
      'blob:edit-b',
      'blob:edit-c',
      'blob:edit-d',
    ]);
    expect(revokeObjectURLMock).not.toHaveBeenCalledWith('/backend-thumbnail.jpg');
  });
});

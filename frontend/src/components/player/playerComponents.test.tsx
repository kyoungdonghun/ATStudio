import { fireEvent, render, screen, waitFor, within } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';

const api = vi.hoisted(() => ({
  fetchMyPlaylists: vi.fn(),
  fetchPlaylistDetail: vi.fn(),
  createPlaylist: vi.fn(),
  deletePlaylist: vi.fn(),
  removeTrackFromPlaylist: vi.fn(),
  reorderTracks: vi.fn(),
  fetchMySubscription: vi.fn(),
  fetchLikes: vi.fn(),
  getApiErrorCode: vi.fn(),
}));

vi.mock('@/api/playlists', () => ({
  fetchMyPlaylists: api.fetchMyPlaylists,
  fetchPlaylistDetail: api.fetchPlaylistDetail,
  createPlaylist: api.createPlaylist,
  deletePlaylist: api.deletePlaylist,
  removeTrackFromPlaylist: api.removeTrackFromPlaylist,
  reorderTracks: api.reorderTracks,
}));
vi.mock('@/api/userSubscriptions', () => ({ fetchMySubscription: api.fetchMySubscription }));
vi.mock('@/api/likes', () => ({ fetchLikes: api.fetchLikes }));
vi.mock('@/api/client', () => ({
  getApiErrorCode: api.getApiErrorCode,
  toUploadUrl: (value: string | null) => value,
}));

import HistoryModal from '@/components/player/HistoryModal';
import PlaylistDrawer from '@/components/player/PlaylistDrawer';
import { useAuthStore } from '@/store/authStore';
import { usePlayerStore } from '@/store/playerStore';
import { useToastStore } from '@/store/toastStore';

const playlist = {
  id: 3,
  title: 'Focus List',
  description: null,
  thumbnail: null,
  trackCount: 2,
  createdAt: '2026-07-17T00:00:00Z',
};
const detail = {
  ...playlist,
  tracks: [
    {
      trackOrder: 0,
      trackId: 11,
      title: 'Song A',
      artistName: 'Artist',
      duration: 180,
      thumbnail: null,
      waveformData: '[0.2,0.8]',
      bpm: 100,
      tonality: 'C',
    },
    {
      trackOrder: 1,
      trackId: 12,
      title: 'Song B',
      artistName: 'Artist',
      duration: 210,
      thumbnail: null,
      waveformData: '[0.1,0.9]',
      bpm: 110,
      tonality: 'D',
    },
  ],
  updatedAt: '2026-07-17T00:00:00Z',
};

describe('player auxiliary components', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    useAuthStore.setState({ user: null, accessToken: 'token', role: 'USER' });
    usePlayerStore.setState({
      currentTrack: null,
      isPlaying: false,
      queue: [],
      trackListContext: [],
    });
    useToastStore.setState({ toasts: [] });
    api.fetchMyPlaylists.mockResolvedValue({ dataList: [playlist] });
    api.fetchMySubscription.mockResolvedValue({ subscription: { maxPlaylists: 5 } });
    api.fetchPlaylistDetail.mockResolvedValue(detail);
    api.fetchLikes.mockResolvedValue({
      dataList: [
        {
          trackId: 21,
          title: 'Liked Song',
          artistName: 'Artist',
          duration: 160,
          bpm: 90,
          tonality: 'A',
          thumbnail: null,
          waveformData: '[0.3,0.7]',
          createdAt: '2026-07-17T00:00:00Z',
        },
      ],
    });
    api.createPlaylist.mockResolvedValue({ id: 4 });
    api.deletePlaylist.mockResolvedValue(undefined);
    api.removeTrackFromPlaylist.mockResolvedValue(undefined);
    api.reorderTracks.mockResolvedValue(undefined);
  });

  it('loads local history, plays an entry, and deletes it', async () => {
    localStorage.setItem(
      'playHistory',
      JSON.stringify([
        {
          track: {
            id: 7,
            title: 'Yesterday',
            artistName: 'Artist',
            duration: 180,
            thumbnail: '/yesterday.png',
            waveformData: '[0.2,0.8]',
          },
          playedAt: '2026-07-17T01:00:00Z',
        },
      ]),
    );
    const play = vi.fn();
    usePlayerStore.setState({ play });
    const { container } = render(<HistoryModal open onClose={vi.fn()} />);

    expect(await screen.findByText('Yesterday')).toBeInTheDocument();
    expect(screen.getByAltText('Yesterday')).toHaveAttribute('src', '/yesterday.png');
    fireEvent.click(screen.getByRole('button', { name: 'Play' }));
    expect(play).toHaveBeenCalledWith(expect.objectContaining({ id: 7, title: 'Yesterday' }));
    const item = screen.getByText('Yesterday').closest('li')!;
    fireEvent.click(within(item).getAllByRole('button')[1]!);
    expect(screen.queryByText('Yesterday')).not.toBeInTheDocument();
    expect(localStorage.getItem('playHistory')).toBe('[]');
    expect(container).toBeInTheDocument();
  });

  it('clears all local history entries', async () => {
    localStorage.setItem(
      'playHistory',
      JSON.stringify([
        {
          track: {
            id: 8,
            title: 'Today',
            artistName: 'Artist',
            duration: 180,
            thumbnail: null,
            waveformData: '[0.2,0.8]',
          },
          playedAt: '2026-07-17T02:00:00Z',
        },
      ]),
    );
    render(<HistoryModal open onClose={vi.fn()} />);
    await screen.findByText('Today');
    const clearButton = screen
      .getAllByRole('button')
      .find((button) => button.textContent?.includes('전체'));
    fireEvent.click(clearButton!);
    expect(screen.queryByText('Today')).not.toBeInTheDocument();
    expect(localStorage.getItem('playHistory')).toBeNull();
  });

  it('hides the drawer when closed and blocks anonymous playlist data', async () => {
    const first = render(<PlaylistDrawer open={false} onClose={vi.fn()} />);
    expect(first.container).toBeEmptyDOMElement();
    first.unmount();
    useAuthStore.setState({ accessToken: null });
    render(<PlaylistDrawer open onClose={vi.fn()} />);
    expect(api.fetchMyPlaylists).not.toHaveBeenCalled();
  });

  it('loads a playlist, plays a track, removes it, and deletes the playlist', async () => {
    const play = vi.fn();
    usePlayerStore.setState({ play });
    const { container } = render(<PlaylistDrawer open onClose={vi.fn()} />);
    fireEvent.click(await screen.findByText('Focus List'));
    expect(await screen.findByText('Song A')).toBeInTheDocument();

    const firstTrack = screen.getByText('Song A').closest('li')!;
    fireEvent.click(within(firstTrack).getAllByRole('button')[0]!);
    expect(play).toHaveBeenCalledWith(expect.objectContaining({ id: 11, bpm: 100 }));
    fireEvent.click(within(firstTrack).getAllByRole('button')[1]!);
    await waitFor(() => expect(api.removeTrackFromPlaylist).toHaveBeenCalledWith(3, 11));

    const deletePlaylistButton = container.querySelector(
      '[class*="deletePlaylistBtn"]',
    ) as HTMLButtonElement;
    fireEvent.click(deletePlaylistButton);
    await waitFor(() => expect(api.deletePlaylist).toHaveBeenCalledWith(3));
  });

  it('creates a trimmed playlist and refreshes the list', async () => {
    const { container } = render(<PlaylistDrawer open onClose={vi.fn()} />);
    await screen.findByText('Focus List');
    const createButton = container.querySelector('[class*="createBtn"]') as HTMLButtonElement;
    fireEvent.click(createButton);
    const input = screen.getByRole('textbox');
    fireEvent.change(input, { target: { value: '  New Mix  ' } });
    fireEvent.keyDown(input, { key: 'Enter' });
    await waitFor(() => expect(api.createPlaylist).toHaveBeenCalledWith({ title: 'New Mix' }));
    expect(api.fetchMyPlaylists).toHaveBeenCalledTimes(2);
  });

  it('shows the playlist-limit toast when creation is rejected', async () => {
    api.createPlaylist.mockRejectedValueOnce(new Error('limit'));
    api.getApiErrorCode.mockResolvedValueOnce('PLAYLIST_LIMIT_EXCEEDED');
    const show = vi.fn();
    useToastStore.setState({ show });
    const { container } = render(<PlaylistDrawer open onClose={vi.fn()} />);
    await screen.findByText('Focus List');
    fireEvent.click(container.querySelector('[class*="createBtn"]')!);
    const input = screen.getByRole('textbox');
    fireEvent.change(input, { target: { value: 'Overflow' } });
    fireEvent.keyDown(input, { key: 'Enter' });
    await waitFor(() => expect(show).toHaveBeenCalledWith('error', expect.any(String)));
  });

  it('optimistically reorders playlist tracks and submits exact zero-based orders', async () => {
    let resolveReorder: (() => void) | undefined;
    api.reorderTracks.mockImplementationOnce(
      () =>
        new Promise<void>((resolve) => {
          resolveReorder = resolve;
        }),
    );
    const { container } = render(<PlaylistDrawer open onClose={vi.fn()} />);
    fireEvent.click(await screen.findByText('Focus List'));
    await screen.findByText('Song B');
    const firstTrack = screen.getByText('Song A').closest('li')!;
    const secondTrack = screen.getByText('Song B').closest('li')!;
    fireEvent.dragStart(firstTrack);
    fireEvent.dragOver(secondTrack);
    fireEvent.drop(secondTrack);

    expect(
      Array.from(container.querySelectorAll('[class*="trackItem"] [class*="trackTitle"]')).map(
        (node) => node.textContent,
      ),
    ).toEqual(['Song B', 'Song A']);
    await waitFor(() =>
      expect(api.reorderTracks).toHaveBeenCalledWith(3, [
        { trackId: 12, trackOrder: 0 },
        { trackId: 11, trackOrder: 1 },
      ]),
    );
    expect(api.reorderTracks).toHaveBeenCalledTimes(1);
    resolveReorder?.();
  });

  it('does not reorder without a valid drag source or when dropped in the original position', async () => {
    render(<PlaylistDrawer open onClose={vi.fn()} />);
    fireEvent.click(await screen.findByText('Focus List'));
    await screen.findByText('Song B');
    const firstTrack = screen.getByText('Song A').closest('li')!;
    const secondTrack = screen.getByText('Song B').closest('li')!;

    fireEvent.drop(secondTrack);
    fireEvent.dragStart(firstTrack);
    fireEvent.drop(firstTrack);

    expect(api.reorderTracks).not.toHaveBeenCalled();
  });

  it('reloads authoritative detail once when a reorder is rejected', async () => {
    api.reorderTracks.mockRejectedValueOnce(new Error('reorder rejected'));
    const authoritativeDetail = {
      ...detail,
      tracks: detail.tracks.map((track) => ({ ...track })),
    };
    api.fetchPlaylistDetail
      .mockResolvedValueOnce(detail)
      .mockResolvedValueOnce(authoritativeDetail);
    const { container } = render(<PlaylistDrawer open onClose={vi.fn()} />);
    fireEvent.click(await screen.findByText('Focus List'));
    await screen.findByText('Song B');
    const firstTrack = screen.getByText('Song A').closest('li')!;
    const secondTrack = screen.getByText('Song B').closest('li')!;

    fireEvent.dragStart(firstTrack);
    fireEvent.drop(secondTrack);

    await waitFor(() => {
      expect(
        Array.from(container.querySelectorAll('[class*="trackItem"] [class*="trackTitle"]')).map(
          (node) => node.textContent,
        ),
      ).toEqual(['Song A', 'Song B']);
    });
    expect(api.reorderTracks).toHaveBeenCalledTimes(1);
    expect(api.fetchPlaylistDetail).toHaveBeenCalledTimes(2);
    expect(api.fetchPlaylistDetail).toHaveBeenLastCalledWith(3);
  });

  it('keeps the last confirmed order when the rejection reload also fails', async () => {
    api.reorderTracks.mockRejectedValueOnce(new Error('reorder rejected'));
    api.fetchPlaylistDetail
      .mockResolvedValueOnce(detail)
      .mockRejectedValueOnce(new Error('reload failed'));
    const { container } = render(<PlaylistDrawer open onClose={vi.fn()} />);
    fireEvent.click(await screen.findByText('Focus List'));
    await screen.findByText('Song B');
    const firstTrack = screen.getByText('Song A').closest('li')!;
    const secondTrack = screen.getByText('Song B').closest('li')!;

    fireEvent.dragStart(firstTrack);
    fireEvent.drop(secondTrack);

    await waitFor(() => expect(api.fetchPlaylistDetail).toHaveBeenCalledTimes(2));
    expect(
      Array.from(container.querySelectorAll('[class*="trackItem"] [class*="trackTitle"]')).map(
        (node) => node.textContent,
      ),
    ).toEqual(['Song A', 'Song B']);
    expect(api.reorderTracks).toHaveBeenCalledTimes(1);
    expect(api.fetchPlaylistDetail).toHaveBeenLastCalledWith(3);
  });

  it('submits the same zero-based reorder contract through touch drag', async () => {
    render(<PlaylistDrawer open onClose={vi.fn()} />);
    fireEvent.click(await screen.findByText('Focus List'));
    await screen.findByText('Song B');
    const firstTrack = screen.getByText('Song A').closest('li')!;
    const secondTrack = screen.getByText('Song B').closest('li')!;
    const list = firstTrack.closest('ul')!;
    vi.spyOn(firstTrack, 'getBoundingClientRect').mockReturnValue({
      top: 0,
      bottom: 49,
    } as DOMRect);
    vi.spyOn(secondTrack, 'getBoundingClientRect').mockReturnValue({
      top: 50,
      bottom: 99,
    } as DOMRect);

    fireEvent.touchStart(firstTrack.querySelector('[class*="dragHandle"]')!, {
      touches: [{ clientY: 25 }],
    });
    fireEvent.touchEnd(list, { changedTouches: [{ clientY: 75 }] });

    await waitFor(() =>
      expect(api.reorderTracks).toHaveBeenCalledWith(3, [
        { trackId: 12, trackOrder: 0 },
        { trackId: 11, trackOrder: 1 },
      ]),
    );
    expect(api.reorderTracks).toHaveBeenCalledTimes(1);
  });

  it('loads liked tracks and plays the selected item', async () => {
    const play = vi.fn();
    usePlayerStore.setState({ play });
    render(<PlaylistDrawer open onClose={vi.fn()} />);
    await screen.findByText('Focus List');
    fireEvent.click(screen.getByRole('button', { name: '좋아요' }));
    expect(await screen.findByText('Liked Song')).toBeInTheDocument();
    const likedRow = screen.getByText('Liked Song').closest('li')!;
    fireEvent.click(within(likedRow).getByRole('button'));
    expect(play).toHaveBeenCalledWith(expect.objectContaining({ id: 21, title: 'Liked Song' }));
  });
});

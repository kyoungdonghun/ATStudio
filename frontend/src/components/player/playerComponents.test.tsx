import { useLayoutEffect, useState } from 'react';
import { act, fireEvent, render, screen, waitFor, within } from '@testing-library/react';
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
import type { User } from '@/types';

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

function deferred<T>() {
  let resolve!: (value: T) => void;
  let reject!: (reason?: unknown) => void;
  const promise = new Promise<T>((nextResolve, nextReject) => {
    resolve = nextResolve;
    reject = nextReject;
  });
  return { promise, resolve, reject };
}

function DrawerLifecycleHarness({ onClosedLayout }: { onClosedLayout: () => void }) {
  const [open, setOpen] = useState(true);
  useLayoutEffect(() => {
    if (!open) onClosedLayout();
  }, [onClosedLayout, open]);
  return (
    <>
      <button type="button" onClick={() => setOpen(false)}>
        close drawer lifecycle
      </button>
      <PlaylistDrawer open={open} onClose={() => setOpen(false)} />
    </>
  );
}

function DrawerFocusReturnHarness() {
  const [open, setOpen] = useState(false);
  return (
    <>
      <button type="button" onClick={() => setOpen(true)}>
        재생목록 서랍 열기
      </button>
      <PlaylistDrawer open={open} onClose={() => setOpen(false)} />
    </>
  );
}

function expectNoPlaylistMutations() {
  expect(api.createPlaylist).not.toHaveBeenCalled();
  expect(api.deletePlaylist).not.toHaveBeenCalled();
  expect(api.removeTrackFromPlaylist).not.toHaveBeenCalled();
  expect(api.reorderTracks).not.toHaveBeenCalled();
}

describe('player auxiliary components', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    useAuthStore.setState({
      user: { id: 1 } as User,
      accessToken: 'token',
      role: 'USER',
    });
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

  it('requires target-specific confirmation before removing a track or deleting a playlist', async () => {
    const play = vi.fn();
    usePlayerStore.setState({ play });
    const { container } = render(<PlaylistDrawer open onClose={vi.fn()} />);
    fireEvent.click(await screen.findByText('Focus List'));
    expect(await screen.findByText('Song A')).toBeInTheDocument();

    const firstTrack = screen.getByText('Song A').closest('li')!;
    fireEvent.click(within(firstTrack).getAllByRole('button')[0]!);
    expect(play).toHaveBeenCalledWith(expect.objectContaining({ id: 11, bpm: 100 }));
    fireEvent.click(within(firstTrack).getAllByRole('button')[1]!);
    expect(api.removeTrackFromPlaylist).not.toHaveBeenCalled();
    const removeDialog = screen.getByRole('dialog', { name: '곡 삭제' });
    expect(removeDialog).toHaveTextContent('Song A');
    expect(removeDialog).toHaveTextContent('Focus List');
    fireEvent.click(within(removeDialog).getByRole('button', { name: '삭제' }));
    await waitFor(() => expect(api.removeTrackFromPlaylist).toHaveBeenCalledWith(3, 11));

    const deletePlaylistButton = container.querySelector(
      '[class*="deletePlaylistBtn"]',
    ) as HTMLButtonElement;
    fireEvent.click(deletePlaylistButton);
    expect(api.deletePlaylist).not.toHaveBeenCalled();
    const deleteDialog = screen.getByRole('dialog', { name: '재생목록 삭제' });
    expect(deleteDialog).toHaveTextContent('Focus List');
    fireEvent.click(within(deleteDialog).getByRole('button', { name: '삭제' }));
    await waitFor(() => expect(api.deletePlaylist).toHaveBeenCalledWith(3));
  });

  it('fences duplicate track removal and retries the same current target after failure', async () => {
    const firstAttempt = deferred<void>();
    api.removeTrackFromPlaylist
      .mockReturnValueOnce(firstAttempt.promise)
      .mockResolvedValueOnce(undefined);
    render(<PlaylistDrawer open onClose={vi.fn()} />);
    fireEvent.click(await screen.findByText('Focus List'));
    const firstTrack = (await screen.findByText('Song A')).closest('li')!;

    fireEvent.click(within(firstTrack).getAllByRole('button')[1]!);
    const removeDialog = screen.getByRole('dialog', { name: '곡 삭제' });
    const confirmButton = within(removeDialog).getByRole('button', { name: '삭제' });
    fireEvent.click(confirmButton);
    fireEvent.click(confirmButton);
    expect(api.removeTrackFromPlaylist).toHaveBeenCalledTimes(1);

    await act(async () => firstAttempt.reject(new Error('remove failed')));
    expect(
      await screen.findByText('재생목록에서 곡을 삭제하지 못했습니다. 다시 시도해 주세요.'),
    ).toBeInTheDocument();

    fireEvent.click(screen.getByRole('button', { name: '다시 시도' }));
    await waitFor(() => expect(api.removeTrackFromPlaylist).toHaveBeenCalledTimes(2));
    expect(api.removeTrackFromPlaylist).toHaveBeenLastCalledWith(3, 11);
    await waitFor(() => expect(api.fetchPlaylistDetail).toHaveBeenCalledTimes(2));
  });

  it('keeps a failed playlist deletion visible and retryable for the same target', async () => {
    api.deletePlaylist.mockRejectedValueOnce(new Error('delete failed'));
    render(<PlaylistDrawer open onClose={vi.fn()} />);
    fireEvent.click(await screen.findByText('Focus List'));
    await screen.findByText('Song A');

    fireEvent.click(document.querySelector('[class*="deletePlaylistBtn"]')!);
    fireEvent.click(
      within(screen.getByRole('dialog', { name: '재생목록 삭제' })).getByRole('button', {
        name: '삭제',
      }),
    );

    expect(
      await screen.findByText('재생목록을 삭제하지 못했습니다. 다시 시도해 주세요.'),
    ).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: '다시 시도' }));

    await waitFor(() => expect(api.deletePlaylist).toHaveBeenCalledTimes(2));
    expect(api.deletePlaylist).toHaveBeenLastCalledWith(3);
    await waitFor(() => expect(api.fetchMyPlaylists).toHaveBeenCalledTimes(2));
  });

  it('retires a detached destructive confirmation when the authenticated owner changes', async () => {
    render(<PlaylistDrawer open onClose={vi.fn()} />);
    fireEvent.click(await screen.findByText('Focus List'));
    await screen.findByText('Song A');
    fireEvent.click(document.querySelector('[class*="deletePlaylistBtn"]')!);
    const detachedConfirm = within(screen.getByRole('dialog', { name: '재생목록 삭제' })).getByRole(
      'button',
      { name: '삭제' },
    );

    act(() => {
      useAuthStore.setState({
        user: { id: 2 } as User,
        accessToken: 'replacement-token',
        role: 'USER',
      });
      detachedConfirm.click();
    });

    expect(api.deletePlaylist).not.toHaveBeenCalled();
    expect(screen.queryByRole('dialog', { name: '재생목록 삭제' })).not.toBeInTheDocument();
  });

  it('ignores an in-flight track removal after back navigation selects another detail', async () => {
    const pendingRemove = deferred<void>();
    const replacementPlaylist = { ...playlist, id: 4, title: 'Replacement List' };
    const replacementDetail = {
      ...detail,
      ...replacementPlaylist,
      tracks: [{ ...detail.tracks[0], trackId: 41, title: 'Replacement Song' }],
    };
    api.fetchMyPlaylists.mockResolvedValue({
      dataList: [playlist, replacementPlaylist],
    });
    api.fetchPlaylistDetail.mockResolvedValueOnce(detail).mockResolvedValueOnce(replacementDetail);
    api.removeTrackFromPlaylist.mockReturnValue(pendingRemove.promise);
    const { container } = render(<PlaylistDrawer open onClose={vi.fn()} />);
    fireEvent.click(await screen.findByText('Focus List'));
    const firstTrack = (await screen.findByText('Song A')).closest('li')!;

    fireEvent.click(within(firstTrack).getAllByRole('button')[1]!);
    fireEvent.click(
      within(screen.getByRole('dialog', { name: '곡 삭제' })).getByRole('button', {
        name: '삭제',
      }),
    );
    expect(api.removeTrackFromPlaylist).toHaveBeenCalledTimes(1);

    fireEvent.click(container.querySelector('button[class*="backBtn"]')!);
    fireEvent.click(await screen.findByText('Replacement List'));
    expect(await screen.findByText('Replacement Song')).toBeInTheDocument();
    await act(async () => pendingRemove.resolve());

    expect(api.fetchPlaylistDetail).toHaveBeenCalledTimes(2);
    expect(screen.getByText('Replacement Song')).toBeInTheDocument();
  });

  it('lets a reopened detail own a new mutation until that exact operation settles', async () => {
    const retiredRemove = deferred<void>();
    const currentRemove = deferred<void>();
    api.removeTrackFromPlaylist
      .mockReturnValueOnce(retiredRemove.promise)
      .mockReturnValueOnce(currentRemove.promise);
    const { container } = render(<PlaylistDrawer open onClose={vi.fn()} />);
    fireEvent.click(await screen.findByText('Focus List'));
    const firstTrack = (await screen.findByText('Song A')).closest('li')!;

    fireEvent.click(within(firstTrack).getAllByRole('button')[1]!);
    fireEvent.click(
      within(screen.getByRole('dialog', { name: '곡 삭제' })).getByRole('button', {
        name: '삭제',
      }),
    );
    expect(api.removeTrackFromPlaylist).toHaveBeenCalledTimes(1);

    fireEvent.click(container.querySelector('button[class*="backBtn"]')!);
    fireEvent.click(await screen.findByText('Focus List'));
    const reopenedTrack = (await screen.findByText('Song A')).closest('li')!;
    const reopenedRemove = within(reopenedTrack).getAllByRole('button')[1]!;
    expect(reopenedRemove).not.toBeDisabled();

    fireEvent.click(reopenedRemove);
    const currentDialog = screen.getByRole('dialog', { name: '곡 삭제' });
    fireEvent.click(within(currentDialog).getByRole('button', { name: '삭제' }));
    expect(api.removeTrackFromPlaylist).toHaveBeenCalledTimes(2);

    await act(async () => retiredRemove.resolve());
    expect(currentDialog).toBeInTheDocument();
    expect(within(currentDialog).getByRole('button', { name: '삭제' })).toBeDisabled();
    expect(api.fetchPlaylistDetail).toHaveBeenCalledTimes(2);

    await act(async () => currentRemove.resolve());
    await waitFor(() => expect(api.fetchPlaylistDetail).toHaveBeenCalledTimes(3));
  });

  it('ignores an in-flight playlist deletion after switching tabs', async () => {
    const pendingDelete = deferred<void>();
    api.deletePlaylist.mockReturnValue(pendingDelete.promise);
    const { container } = render(<PlaylistDrawer open onClose={vi.fn()} />);
    fireEvent.click(await screen.findByText('Focus List'));
    await screen.findByText('Song A');

    fireEvent.click(container.querySelector('button[class*="deletePlaylistBtn"]')!);
    fireEvent.click(
      within(screen.getByRole('dialog', { name: '재생목록 삭제' })).getByRole('button', {
        name: '삭제',
      }),
    );
    expect(api.deletePlaylist).toHaveBeenCalledTimes(1);

    fireEvent.click(container.querySelectorAll('button[class*="tab"]')[1]!);
    await waitFor(() => expect(api.fetchLikes).toHaveBeenCalledTimes(1));
    await act(async () => pendingDelete.resolve());

    expect(api.fetchMyPlaylists).toHaveBeenCalledTimes(1);
    expect(api.fetchPlaylistDetail).toHaveBeenCalledTimes(1);
  });

  it('ignores an in-flight track removal after the drawer closes', async () => {
    const pendingRemove = deferred<void>();
    api.removeTrackFromPlaylist.mockReturnValue(pendingRemove.promise);
    render(<DrawerLifecycleHarness onClosedLayout={() => undefined} />);
    fireEvent.click(await screen.findByText('Focus List'));
    const firstTrack = (await screen.findByText('Song A')).closest('li')!;

    fireEvent.click(within(firstTrack).getAllByRole('button')[1]!);
    fireEvent.click(
      within(screen.getByRole('dialog', { name: '곡 삭제' })).getByRole('button', {
        name: '삭제',
      }),
    );
    expect(api.removeTrackFromPlaylist).toHaveBeenCalledTimes(1);

    fireEvent.click(screen.getByRole('button', { name: 'close drawer lifecycle' }));
    await act(async () => pendingRemove.resolve());

    expect(api.fetchPlaylistDetail).toHaveBeenCalledTimes(1);
    expect(screen.queryByText('Song A')).not.toBeInTheDocument();
  });

  it('ignores an in-flight playlist deletion after the owner session changes', async () => {
    const pendingDelete = deferred<void>();
    api.deletePlaylist.mockReturnValue(pendingDelete.promise);
    render(<PlaylistDrawer open onClose={vi.fn()} />);
    fireEvent.click(await screen.findByText('Focus List'));
    await screen.findByText('Song A');

    fireEvent.click(document.querySelector('button[class*="deletePlaylistBtn"]')!);
    fireEvent.click(
      within(screen.getByRole('dialog', { name: '재생목록 삭제' })).getByRole('button', {
        name: '삭제',
      }),
    );
    expect(api.deletePlaylist).toHaveBeenCalledTimes(1);

    act(() => {
      useAuthStore.setState({
        user: { id: 2 } as User,
        accessToken: 'replacement-token',
        role: 'USER',
      });
    });
    await waitFor(() => expect(api.fetchMyPlaylists).toHaveBeenCalledTimes(2));
    await act(async () => pendingDelete.resolve());

    expect(api.fetchMyPlaylists).toHaveBeenCalledTimes(2);
    expect(api.fetchPlaylistDetail).toHaveBeenCalledTimes(1);
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

  it('fails closed on capacity error and recovers through one in-flight retry', async () => {
    const capacityRetry = deferred<{ subscription: { maxPlaylists: number } }>();
    api.fetchMySubscription
      .mockRejectedValueOnce(new Error('capacity unavailable'))
      .mockReturnValueOnce(capacityRetry.promise);

    const { container } = render(<PlaylistDrawer open onClose={vi.fn()} />);
    expect(
      await screen.findByText('재생목록 생성 한도를 확인하지 못했습니다.'),
    ).toBeInTheDocument();
    expect(container.querySelector('[class*="createBtn"]')).not.toBeInTheDocument();

    const retryButton = screen.getByRole('button', { name: '한도 다시 확인' });
    fireEvent.click(retryButton);
    fireEvent.click(retryButton);
    expect(api.fetchMySubscription).toHaveBeenCalledTimes(2);

    await act(async () => capacityRetry.resolve({ subscription: { maxPlaylists: 5 } }));
    expect(await screen.findByText('최대 5개')).toBeInTheDocument();
    expect(container.querySelector('[class*="createBtn"]')).toBeInTheDocument();
  });

  it('prevents an earlier list or capacity response from populating a reopened drawer', async () => {
    const oldPlaylists = deferred<{ dataList: (typeof playlist)[] }>();
    const oldCapacity = deferred<{ subscription: { maxPlaylists: number } }>();
    const currentPlaylists = deferred<{ dataList: (typeof playlist)[] }>();
    const currentCapacity = deferred<{ subscription: { maxPlaylists: number } }>();
    api.fetchMyPlaylists
      .mockReturnValueOnce(oldPlaylists.promise)
      .mockReturnValueOnce(currentPlaylists.promise);
    api.fetchMySubscription
      .mockReturnValueOnce(oldCapacity.promise)
      .mockReturnValueOnce(currentCapacity.promise);

    const { rerender } = render(<PlaylistDrawer open onClose={vi.fn()} />);
    await waitFor(() => expect(api.fetchMyPlaylists).toHaveBeenCalledTimes(1));
    const oldListSignal = api.fetchMyPlaylists.mock.calls[0][0] as AbortSignal;

    rerender(<PlaylistDrawer open={false} onClose={vi.fn()} />);
    rerender(<PlaylistDrawer open onClose={vi.fn()} />);
    await waitFor(() => expect(api.fetchMyPlaylists).toHaveBeenCalledTimes(2));
    expect(oldListSignal.aborted).toBe(true);

    const currentRows = [
      { ...playlist, id: 20, title: '현재 목록 1' },
      { ...playlist, id: 21, title: '현재 목록 2' },
      { ...playlist, id: 22, title: '현재 목록 3' },
    ];
    await act(async () => {
      currentPlaylists.resolve({ dataList: currentRows });
      currentCapacity.resolve({ subscription: { maxPlaylists: 3 } });
    });
    expect(await screen.findByText('현재 목록 1')).toBeInTheDocument();

    await act(async () => {
      oldPlaylists.resolve({ dataList: [{ ...playlist, title: '이전 목록' }] });
      oldCapacity.resolve({ subscription: { maxPlaylists: 5 } });
    });
    expect(screen.queryByText('이전 목록')).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: /새 재생목록/ })).not.toBeInTheDocument();
  });

  it('prevents an earlier detail response from populating a reopened drawer', async () => {
    const oldDetail = deferred<typeof detail>();
    api.fetchPlaylistDetail.mockReturnValueOnce(oldDetail.promise);
    const { rerender } = render(<PlaylistDrawer open onClose={vi.fn()} />);
    fireEvent.click(await screen.findByText('Focus List'));
    await waitFor(() => expect(api.fetchPlaylistDetail).toHaveBeenCalledTimes(1));
    const oldDetailSignal = api.fetchPlaylistDetail.mock.calls[0][1] as AbortSignal;

    rerender(<PlaylistDrawer open={false} onClose={vi.fn()} />);
    api.fetchMyPlaylists.mockResolvedValueOnce({
      dataList: [{ ...playlist, id: 4, title: 'Reopened List' }],
    });
    rerender(<PlaylistDrawer open onClose={vi.fn()} />);
    expect(await screen.findByText('Reopened List')).toBeInTheDocument();
    expect(oldDetailSignal.aborted).toBe(true);

    await act(async () => oldDetail.resolve(detail));
    expect(screen.queryByText('Song A')).not.toBeInTheDocument();
    expect(screen.getByText('Reopened List')).toBeInTheDocument();
  });

  it('retires a detached delete control in the drawer-close layout commit', async () => {
    let detachedDelete: HTMLButtonElement | null = null;
    render(
      <DrawerLifecycleHarness
        onClosedLayout={() => {
          detachedDelete?.click();
        }}
      />,
    );
    fireEvent.click(await screen.findByText('Focus List'));
    expect(await screen.findByText('Song A')).toBeInTheDocument();
    detachedDelete = document.querySelector('[class*="deletePlaylistBtn"]');

    fireEvent.click(screen.getByRole('button', { name: 'close drawer lifecycle' }));

    expect(api.deletePlaylist).not.toHaveBeenCalled();
    expect(screen.queryByText('Song A')).not.toBeInTheDocument();
  });

  it('prevents an earlier likes response from populating a reopened drawer', async () => {
    const oldLikes = deferred<{ dataList: Array<{ trackId: number; title: string }> }>();
    const currentLikes = deferred<{ dataList: Array<{ trackId: number; title: string }> }>();
    api.fetchLikes.mockReturnValueOnce(oldLikes.promise).mockReturnValueOnce(currentLikes.promise);
    const { rerender } = render(<PlaylistDrawer open onClose={vi.fn()} />);
    await screen.findByText('Focus List');
    fireEvent.click(screen.getByRole('button', { name: '좋아요' }));
    await waitFor(() => expect(api.fetchLikes).toHaveBeenCalledTimes(1));
    const oldLikesSignal = api.fetchLikes.mock.calls[0][0] as AbortSignal;

    rerender(<PlaylistDrawer open={false} onClose={vi.fn()} />);
    rerender(<PlaylistDrawer open onClose={vi.fn()} />);
    await waitFor(() => expect(api.fetchLikes).toHaveBeenCalledTimes(2));
    expect(oldLikesSignal.aborted).toBe(true);

    await act(async () =>
      currentLikes.resolve({ dataList: [{ trackId: 31, title: 'Current Like' }] }),
    );
    expect(await screen.findByText('Current Like')).toBeInTheDocument();
    await act(async () => oldLikes.resolve({ dataList: [{ trackId: 30, title: 'Old Like' }] }));
    expect(screen.queryByText('Old Like')).not.toBeInTheDocument();
    expect(screen.getByText('Current Like')).toBeInTheDocument();
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
    expect(api.fetchPlaylistDetail).toHaveBeenLastCalledWith(3, expect.any(AbortSignal));
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
    expect(api.fetchPlaylistDetail).toHaveBeenLastCalledWith(3, expect.any(AbortSignal));
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

  it('owns dialog focus and exposes selected tabs without dispatching a playlist mutation', async () => {
    const onClose = vi.fn();
    render(<PlaylistDrawer open onClose={onClose} />);

    const dialog = screen.getByRole('dialog', { name: '재생목록 및 좋아요' });
    expect(dialog).toHaveFocus();
    expect(screen.getByRole('button', { name: '재생목록' })).toHaveAttribute(
      'aria-pressed',
      'true',
    );
    expect(screen.getByRole('button', { name: '좋아요' })).toHaveAttribute('aria-pressed', 'false');

    fireEvent.keyDown(document, { key: 'Escape' });
    expect(onClose).toHaveBeenCalledOnce();
    expectNoPlaylistMutations();
  });

  it('wraps forward Tab focus within the drawer without dispatching a playlist mutation', async () => {
    render(<PlaylistDrawer open onClose={vi.fn()} />);

    const firstFocusable = screen.getByRole('button', { name: '재생목록' });
    const lastFocusable = await screen.findByRole('button', { name: '+ 새 재생목록' });
    lastFocusable.focus();

    fireEvent.keyDown(document, { key: 'Tab' });

    expect(firstFocusable).toHaveFocus();
    expectNoPlaylistMutations();
  });

  it('wraps reverse Shift+Tab focus within the drawer without dispatching a playlist mutation', async () => {
    render(<PlaylistDrawer open onClose={vi.fn()} />);

    const firstFocusable = screen.getByRole('button', { name: '재생목록' });
    const lastFocusable = await screen.findByRole('button', { name: '+ 새 재생목록' });
    firstFocusable.focus();

    fireEvent.keyDown(document, { key: 'Tab', shiftKey: true });

    expect(lastFocusable).toHaveFocus();
    expectNoPlaylistMutations();
  });

  it('returns focus to its connected opener after close without dispatching a playlist mutation', async () => {
    render(<DrawerFocusReturnHarness />);

    const opener = screen.getByRole('button', { name: '재생목록 서랍 열기' });
    opener.focus();
    fireEvent.click(opener);
    await screen.findByRole('dialog', { name: '재생목록 및 좋아요' });

    fireEvent.click(screen.getByRole('button', { name: '재생목록 닫기' }));

    expect(opener).toBeInTheDocument();
    expect(opener).toHaveFocus();
    expectNoPlaylistMutations();
  });
});

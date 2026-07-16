import { act, fireEvent, render, screen, waitFor } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import AddToPlaylistModal from '@/components/playlist/AddToPlaylistModal';
import type { Playlist } from '@/types';

const fetchMyPlaylistsMock = vi.fn();
const addTrackToPlaylistMock = vi.fn();

vi.mock('@/api/playlists', () => ({
  fetchMyPlaylists: (...args: unknown[]) => fetchMyPlaylistsMock(...args),
  addTrackToPlaylist: (...args: unknown[]) => addTrackToPlaylistMock(...args),
}));

vi.mock('@/api/client', () => ({
  isSubscriptionRequired: (err: unknown) =>
    (err as { response?: { data?: { errorCode?: string } } })?.response?.data?.errorCode ===
    'NO_ACTIVE_SUBSCRIPTION',
}));

function deferred<T>() {
  let resolve!: (value: T) => void;
  let reject!: (reason?: unknown) => void;
  const promise = new Promise<T>((resolvePromise, rejectPromise) => {
    resolve = resolvePromise;
    reject = rejectPromise;
  });
  return { promise, resolve, reject };
}

function playlist(id: number, title: string): Playlist {
  return {
    id,
    title,
    description: null,
    thumbnail: null,
    trackCount: 0,
    createdAt: '2026-07-16T00:00:00Z',
  };
}

describe('AddToPlaylistModal', () => {
  beforeEach(() => {
    fetchMyPlaylistsMock.mockReset();
    addTrackToPlaylistMock.mockReset();
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it('shows a real load error instead of the empty-state copy on generic API failures', async () => {
    fetchMyPlaylistsMock.mockRejectedValue(new Error('server unavailable'));

    render(<AddToPlaylistModal open trackId={1} onClose={vi.fn()} />);

    await waitFor(() => {
      expect(screen.getByText('재생목록을 불러오지 못했습니다.')).toBeInTheDocument();
    });
    expect(screen.queryByText('재생목록이 없습니다.')).not.toBeInTheDocument();
  });

  it('ignores a stale playlist load after the modal closes and reopens', async () => {
    const firstLoad = deferred<{ dataList: Playlist[] }>();
    const secondLoad = deferred<{ dataList: Playlist[] }>();
    fetchMyPlaylistsMock
      .mockReturnValueOnce(firstLoad.promise)
      .mockReturnValueOnce(secondLoad.promise);
    const onClose = vi.fn();
    const { rerender } = render(<AddToPlaylistModal open trackId={1} onClose={onClose} />);

    rerender(<AddToPlaylistModal open={false} trackId={1} onClose={onClose} />);
    rerender(<AddToPlaylistModal open trackId={1} onClose={onClose} />);

    await act(async () => {
      secondLoad.resolve({ dataList: [playlist(2, 'Current playlist')] });
      await secondLoad.promise;
    });
    expect(screen.getByText('Current playlist')).toBeInTheDocument();

    await act(async () => {
      firstLoad.resolve({ dataList: [playlist(1, 'Stale playlist')] });
      await firstLoad.promise;
    });
    expect(screen.getByText('Current playlist')).toBeInTheDocument();
    expect(screen.queryByText('Stale playlist')).not.toBeInTheDocument();
  });

  it('does not restart the playlist load when parent callback identities change', async () => {
    const pendingLoad = deferred<{ dataList: Playlist[] }>();
    fetchMyPlaylistsMock.mockReturnValue(pendingLoad.promise);
    const { rerender } = render(<AddToPlaylistModal open trackId={1} onClose={() => undefined} />);

    rerender(
      <AddToPlaylistModal
        open
        trackId={1}
        onClose={() => undefined}
        onSubscriptionRequired={() => undefined}
      />,
    );

    expect(fetchMyPlaylistsMock).toHaveBeenCalledTimes(1);

    await act(async () => {
      pendingLoad.resolve({ dataList: [playlist(1, 'Stable playlist')] });
      await pendingLoad.promise;
    });

    expect(screen.getByText('Stable playlist')).toBeInTheDocument();
    expect(fetchMyPlaylistsMock).toHaveBeenCalledTimes(1);
  });

  it('ignores an add request that completes after a close and reopen', async () => {
    const pendingAdd = deferred<void>();
    fetchMyPlaylistsMock.mockResolvedValue({ dataList: [playlist(1, 'Playlist A')] });
    addTrackToPlaylistMock.mockReturnValue(pendingAdd.promise);
    const onClose = vi.fn();
    const { rerender } = render(<AddToPlaylistModal open trackId={10} onClose={onClose} />);
    expect(await screen.findByText('Playlist A')).toBeInTheDocument();

    fireEvent.click(screen.getByRole('button', { name: /Playlist A/ }));
    fireEvent.click(screen.getByRole('button', { name: '추가' }));
    rerender(<AddToPlaylistModal open={false} trackId={10} onClose={onClose} />);
    rerender(<AddToPlaylistModal open trackId={10} onClose={onClose} />);

    await act(async () => {
      pendingAdd.resolve();
      await pendingAdd.promise;
    });

    expect(screen.queryByText('추가되었습니다!')).not.toBeInTheDocument();
    expect(onClose).not.toHaveBeenCalled();
  });

  it('clears an old success timer when the modal closes and reopens', async () => {
    vi.useFakeTimers();
    fetchMyPlaylistsMock.mockResolvedValue({ dataList: [playlist(1, 'Playlist A')] });
    addTrackToPlaylistMock.mockResolvedValue(undefined);
    const onClose = vi.fn();
    const { rerender } = render(<AddToPlaylistModal open trackId={10} onClose={onClose} />);
    await act(async () => {
      await Promise.resolve();
    });

    fireEvent.click(screen.getByRole('button', { name: /Playlist A/ }));
    fireEvent.click(screen.getByRole('button', { name: '추가' }));
    await act(async () => {
      await Promise.resolve();
    });
    expect(screen.getByText('추가되었습니다!')).toBeInTheDocument();

    rerender(<AddToPlaylistModal open={false} trackId={10} onClose={onClose} />);
    rerender(<AddToPlaylistModal open trackId={10} onClose={onClose} />);
    await act(async () => {
      await Promise.resolve();
      vi.advanceTimersByTime(800);
    });

    expect(onClose).not.toHaveBeenCalled();
  });

  it('clears the delayed close timer when unmounted', async () => {
    vi.useFakeTimers();
    fetchMyPlaylistsMock.mockResolvedValue({ dataList: [playlist(1, 'Playlist A')] });
    addTrackToPlaylistMock.mockResolvedValue(undefined);
    const onClose = vi.fn();
    const { unmount } = render(<AddToPlaylistModal open trackId={10} onClose={onClose} />);
    await act(async () => {
      await Promise.resolve();
    });

    fireEvent.click(screen.getByRole('button', { name: /Playlist A/ }));
    fireEvent.click(screen.getByRole('button', { name: '추가' }));
    await act(async () => {
      await Promise.resolve();
    });
    unmount();
    act(() => vi.advanceTimersByTime(800));

    expect(onClose).not.toHaveBeenCalled();
  });
});

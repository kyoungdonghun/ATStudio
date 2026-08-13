import { useLayoutEffect, useState } from 'react';
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

function TrackReplacementHarness({ onReplacedLayout }: { onReplacedLayout: () => void }) {
  const [trackID, setTrackID] = useState(10);

  useLayoutEffect(() => {
    if (trackID === 20) onReplacedLayout();
  }, [onReplacedLayout, trackID]);

  return (
    <>
      <button type="button" onClick={() => setTrackID(20)}>
        replace track
      </button>
      <AddToPlaylistModal open trackId={trackID} onClose={vi.fn()} />
    </>
  );
}

describe('AddToPlaylistModal', () => {
  beforeEach(() => {
    fetchMyPlaylistsMock.mockReset();
    addTrackToPlaylistMock.mockReset();
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it('renders loading while the playlist list is pending', () => {
    fetchMyPlaylistsMock.mockReturnValue(deferred<{ dataList: Playlist[] }>().promise);

    render(<AddToPlaylistModal open trackId={1} onClose={vi.fn()} />);

    expect(screen.getByRole('dialog', { name: '재생목록에 추가' })).toBeInTheDocument();
    expect(screen.getByText('재생목록을 불러오는 중입니다.')).toBeInTheDocument();
  });

  it('shows a load error and fences a manual retry while its request is pending', async () => {
    const retryLoad = deferred<{ dataList: Playlist[] }>();
    fetchMyPlaylistsMock
      .mockRejectedValueOnce(new Error('server unavailable'))
      .mockReturnValueOnce(retryLoad.promise);

    render(<AddToPlaylistModal open trackId={1} onClose={vi.fn()} />);

    await waitFor(() => {
      expect(screen.getByText('재생목록을 불러오지 못했습니다.')).toBeInTheDocument();
    });
    expect(screen.queryByText('재생목록이 없습니다.')).not.toBeInTheDocument();

    const retryButton = screen.getByRole('button', { name: '다시 시도' });
    fireEvent.click(retryButton);
    fireEvent.click(retryButton);
    expect(fetchMyPlaylistsMock).toHaveBeenCalledTimes(2);
    expect(screen.getByText('재생목록을 불러오는 중입니다.')).toBeInTheDocument();

    await act(async () => retryLoad.resolve({ dataList: [playlist(2, 'Recovered playlist')] }));
    expect(await screen.findByText('Recovered playlist')).toBeInTheDocument();
  });

  it('shows an explicit subscription-required result when no expiry callback is supplied', async () => {
    fetchMyPlaylistsMock.mockRejectedValue({
      response: { data: { errorCode: 'NO_ACTIVE_SUBSCRIPTION' } },
    });
    const onClose = vi.fn();

    render(<AddToPlaylistModal open trackId={1} onClose={onClose} />);

    expect(await screen.findByText('구독이 필요한 기능입니다.')).toBeInTheDocument();
    expect(onClose).not.toHaveBeenCalled();
    expect(screen.getByRole('button', { name: '닫기' })).toBeInTheDocument();
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

  it('retires the add control in the Track replacement layout commit', async () => {
    fetchMyPlaylistsMock.mockResolvedValue({ dataList: [playlist(1, 'Playlist A')] });
    let addButton: HTMLButtonElement | null = null;
    render(
      <TrackReplacementHarness
        onReplacedLayout={() => {
          addButton?.click();
        }}
      />,
    );
    expect(await screen.findByText('Playlist A')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: /Playlist A/ }));
    addButton = document.querySelector('[class*="addBtn"]');

    fireEvent.click(screen.getByRole('button', { name: 'replace track' }));

    expect(addTrackToPlaylistMock).not.toHaveBeenCalled();
  });

  it('renders only the new Track loading projection in the replacement layout commit', async () => {
    fetchMyPlaylistsMock.mockResolvedValue({ dataList: [playlist(1, 'Playlist A')] });
    let replacementProjection:
      | { loading: boolean; stalePlaylist: boolean; staleSelection: boolean }
      | undefined;
    render(
      <TrackReplacementHarness
        onReplacedLayout={() => {
          replacementProjection = {
            loading: document.body.textContent?.includes('재생목록을 불러오는 중입니다.') ?? false,
            stalePlaylist: document.body.textContent?.includes('Playlist A') ?? false,
            staleSelection: document.querySelector('[class*="plBtnSelected"]') !== null,
          };
        }}
      />,
    );
    expect(await screen.findByText('Playlist A')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: /Playlist A/ }));
    expect(document.querySelector('[class*="plBtnSelected"]')).toBeInTheDocument();

    fireEvent.click(screen.getByRole('button', { name: 'replace track' }));

    expect(replacementProjection).toEqual({
      loading: true,
      stalePlaylist: false,
      staleSelection: false,
    });
  });

  it('ignores a stale playlist load after Track replacement', async () => {
    const oldLoad = deferred<{ dataList: Playlist[] }>();
    const currentLoad = deferred<{ dataList: Playlist[] }>();
    fetchMyPlaylistsMock
      .mockReturnValueOnce(oldLoad.promise)
      .mockReturnValueOnce(currentLoad.promise);
    const onClose = vi.fn();
    const { rerender } = render(<AddToPlaylistModal open trackId={10} onClose={onClose} />);

    rerender(<AddToPlaylistModal open trackId={20} onClose={onClose} />);
    await act(async () => currentLoad.resolve({ dataList: [playlist(2, 'Current playlist')] }));
    expect(await screen.findByText('Current playlist')).toBeInTheDocument();

    await act(async () => oldLoad.resolve({ dataList: [playlist(1, 'Stale playlist')] }));
    expect(screen.queryByText('Stale playlist')).not.toBeInTheDocument();
    expect(screen.getByText('Current playlist')).toBeInTheDocument();
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

  it('ignores an add request that completes after Track replacement', async () => {
    const pendingAdd = deferred<void>();
    fetchMyPlaylistsMock.mockResolvedValue({ dataList: [playlist(1, 'Playlist A')] });
    addTrackToPlaylistMock.mockReturnValue(pendingAdd.promise);
    const onClose = vi.fn();
    const { rerender } = render(<AddToPlaylistModal open trackId={10} onClose={onClose} />);
    expect(await screen.findByText('Playlist A')).toBeInTheDocument();

    fireEvent.click(screen.getByRole('button', { name: /Playlist A/ }));
    fireEvent.click(document.querySelector('[class*="addBtn"]')!);
    rerender(<AddToPlaylistModal open trackId={20} onClose={onClose} />);
    await act(async () => pendingAdd.resolve());

    expect(screen.queryByText('추가되었습니다!')).not.toBeInTheDocument();
    expect(onClose).not.toHaveBeenCalled();
  });

  it('fences duplicate add submits within the current modal lifecycle', async () => {
    const pendingAdd = deferred<void>();
    fetchMyPlaylistsMock.mockResolvedValue({ dataList: [playlist(1, 'Playlist A')] });
    addTrackToPlaylistMock.mockReturnValue(pendingAdd.promise);
    render(<AddToPlaylistModal open trackId={10} onClose={vi.fn()} />);
    expect(await screen.findByText('Playlist A')).toBeInTheDocument();

    fireEvent.click(screen.getByRole('button', { name: /Playlist A/ }));
    const addButton = screen.getByRole('button', { name: '추가' });
    fireEvent.click(addButton);
    fireEvent.click(addButton);

    expect(addTrackToPlaylistMock).toHaveBeenCalledTimes(1);
    await act(async () => pendingAdd.resolve());
    expect(await screen.findByText('추가되었습니다!')).toBeInTheDocument();
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

  it('clears an old success timer after Track replacement', async () => {
    vi.useFakeTimers();
    fetchMyPlaylistsMock.mockResolvedValue({ dataList: [playlist(1, 'Playlist A')] });
    addTrackToPlaylistMock.mockResolvedValue(undefined);
    const onClose = vi.fn();
    const { rerender } = render(<AddToPlaylistModal open trackId={10} onClose={onClose} />);
    await act(async () => {
      await Promise.resolve();
    });

    fireEvent.click(screen.getByRole('button', { name: /Playlist A/ }));
    fireEvent.click(document.querySelector('[class*="addBtn"]')!);
    await act(async () => {
      await Promise.resolve();
    });

    rerender(<AddToPlaylistModal open trackId={20} onClose={onClose} />);
    act(() => vi.advanceTimersByTime(800));

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

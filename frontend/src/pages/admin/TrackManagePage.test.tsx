import { act, fireEvent, render, screen, waitFor, within } from '@testing-library/react';
import { createMemoryRouter, RouterProvider } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import TrackManagePage from './TrackManagePage';

const mocks = vi.hoisted(() => ({
  fetchAdminTracks: vi.fn(),
  deleteTrack: vi.fn(),
}));

vi.mock('@/api/tracks', () => ({
  fetchAdminTracks: (...args: unknown[]) => mocks.fetchAdminTracks(...args),
  deleteTrack: (...args: unknown[]) => mocks.deleteTrack(...args),
}));

vi.mock('@/api/client', () => ({
  toUploadUrl: (path: string | null | undefined) => (path ? `/uploads/${path}` : null),
}));

const pageInfo = {
  page: 1,
  size: 20,
  total: 1,
  start: 1,
  end: 1,
  prev: false,
  next: false,
};

const track = {
  id: 15,
  title: 'Admin Track',
  artistName: 'Artist',
  duration: 180,
  bpm: 128,
  tonality: 'C#',
  thumbnail: null,
  playCount: 22,
  likeCount: 3,
  downloadCount: 4,
  isActive: true,
  tags: [],
  createdAt: '2026-08-01T00:00:00Z',
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

function renderPage(initialEntry = '/admin/tracks') {
  const router = createMemoryRouter([{ path: '/admin/tracks', element: <TrackManagePage /> }], {
    initialEntries: [initialEntry],
  });
  render(<RouterProvider router={router} future={{ v7_startTransition: true }} />);
  return router;
}

describe('TrackManagePage', () => {
  beforeEach(() => {
    mocks.fetchAdminTracks.mockReset().mockResolvedValue({ dataList: [track], pageInfo });
    mocks.deleteTrack.mockReset().mockResolvedValue(undefined);
  });

  it('normalizes malformed URL state before issuing one bounded request', async () => {
    const router = renderPage(
      '/admin/tracks?page=999999999999&filter=broken&keyword=%20%20Admin%20%20',
    );

    await waitFor(() =>
      expect(mocks.fetchAdminTracks).toHaveBeenCalledWith(
        {
          page: 1,
          size: 20,
          keyword: 'Admin',
        },
        expect.any(AbortSignal),
      ),
    );
    expect(mocks.fetchAdminTracks).toHaveBeenCalledTimes(1);
    expect(router.state.location.search).toBe('?page=1&keyword=Admin');
    expect(screen.getByRole('searchbox', { name: '곡 제목 검색' })).toHaveValue('Admin');
  });

  it('canonicalizes a beyond-last page and reloads the authoritative final page', async () => {
    mocks.fetchAdminTracks
      .mockResolvedValueOnce({ dataList: [], pageInfo: { ...pageInfo, page: 4, total: 21 } })
      .mockResolvedValueOnce({ dataList: [track], pageInfo: { ...pageInfo, page: 2, total: 21 } });
    const router = renderPage('/admin/tracks?page=4');

    await waitFor(() =>
      expect(mocks.fetchAdminTracks).toHaveBeenLastCalledWith(
        { page: 2, size: 20 },
        expect.any(AbortSignal),
      ),
    );
    expect(router.state.location.search).toBe('?page=2');
    expect(await screen.findByText('Admin Track')).toBeInTheDocument();
  });

  it('keeps draft and applied keyword synchronized across navigation', async () => {
    const router = renderPage('/admin/tracks?keyword=first');
    const search = await screen.findByRole('searchbox', { name: '곡 제목 검색' });
    expect(search).toHaveValue('first');

    await act(async () => {
      await router.navigate('/admin/tracks?keyword=second');
    });
    await waitFor(() => expect(search).toHaveValue('second'));

    await act(async () => {
      await router.navigate(-1);
    });
    await waitFor(() => expect(search).toHaveValue('first'));
    expect(mocks.fetchAdminTracks).toHaveBeenLastCalledWith(
      {
        page: 1,
        size: 20,
        keyword: 'first',
      },
      expect.any(AbortSignal),
    );
  });

  it('keeps the newest filter result when an older request resolves last', async () => {
    const activeRequest = deferred<{ dataList: (typeof track)[]; pageInfo: typeof pageInfo }>();
    const inactiveRequest = deferred<{ dataList: (typeof track)[]; pageInfo: typeof pageInfo }>();
    mocks.fetchAdminTracks
      .mockReturnValueOnce(activeRequest.promise)
      .mockReturnValueOnce(inactiveRequest.promise);
    const router = renderPage('/admin/tracks?filter=active');

    await waitFor(() => expect(mocks.fetchAdminTracks).toHaveBeenCalledTimes(1));
    const oldSignal = mocks.fetchAdminTracks.mock.calls[0][1] as AbortSignal;
    await act(async () => {
      await router.navigate('/admin/tracks?filter=inactive');
    });
    await waitFor(() => expect(mocks.fetchAdminTracks).toHaveBeenCalledTimes(2));
    expect(oldSignal.aborted).toBe(true);

    const inactiveTrack = { ...track, id: 16, title: 'Newest Inactive Track', isActive: false };
    await act(async () =>
      inactiveRequest.resolve({ dataList: [inactiveTrack], pageInfo: { ...pageInfo, total: 1 } }),
    );
    expect(await screen.findByText('Newest Inactive Track')).toBeInTheDocument();

    await act(async () => activeRequest.resolve({ dataList: [track], pageInfo }));
    expect(screen.getByText('Newest Inactive Track')).toBeInTheDocument();
    expect(screen.queryByText('Admin Track')).not.toBeInTheDocument();
  });

  it('clears failed list data and offers an explicit load retry', async () => {
    mocks.fetchAdminTracks
      .mockRejectedValueOnce(new Error('offline'))
      .mockResolvedValueOnce({ dataList: [track], pageInfo });
    renderPage();

    expect(await screen.findByRole('alert')).toHaveTextContent('음원 목록을 불러오지 못했습니다.');
    expect(screen.queryByText('Admin Track')).not.toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: '목록 다시 시도' }));

    expect(await screen.findByText('Admin Track')).toBeInTheDocument();
    expect(mocks.fetchAdminTracks).toHaveBeenCalledTimes(2);
  });

  it('keeps a failed delete attached to its target and retries through authoritative refresh', async () => {
    mocks.deleteTrack.mockRejectedValueOnce(new Error('denied')).mockResolvedValueOnce(undefined);
    mocks.fetchAdminTracks
      .mockResolvedValueOnce({ dataList: [track], pageInfo })
      .mockResolvedValueOnce({ dataList: [], pageInfo: { ...pageInfo, total: 0, end: 0 } });
    renderPage();

    expect(await screen.findByText('Admin Track')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: '삭제' }));
    const dialog = screen.getByRole('dialog', { name: '음원 삭제' });
    fireEvent.click(within(dialog).getByRole('button', { name: '삭제' }));

    expect(await within(dialog).findByRole('alert')).toHaveTextContent(
      '같은 음원에서 다시 시도해 주세요.',
    );
    expect(within(dialog).getByText('Admin Track')).toBeInTheDocument();
    fireEvent.click(within(dialog).getByRole('button', { name: '삭제 다시 시도' }));

    await waitFor(() => expect(mocks.deleteTrack).toHaveBeenCalledTimes(2));
    await waitFor(() => expect(screen.queryByRole('dialog', { name: '음원 삭제' })).toBeNull());
    expect(mocks.fetchAdminTracks).toHaveBeenCalledTimes(2);
  });

  it('blocks close, retarget, and duplicate execution while delete is pending', async () => {
    const pendingDelete = deferred<void>();
    mocks.deleteTrack.mockReturnValueOnce(pendingDelete.promise);
    mocks.fetchAdminTracks
      .mockResolvedValueOnce({ dataList: [track], pageInfo })
      .mockResolvedValueOnce({ dataList: [], pageInfo: { ...pageInfo, total: 0, end: 0 } });
    renderPage();

    expect(await screen.findByText('Admin Track')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: '삭제' }));
    const dialog = screen.getByRole('dialog', { name: '음원 삭제' });
    fireEvent.click(within(dialog).getByRole('button', { name: '삭제' }));

    expect(dialog).toHaveAttribute('aria-busy', 'true');
    expect(
      within(dialog)
        .getAllByRole('button', { name: '닫기' })
        .every((button) => button.hasAttribute('disabled')),
    ).toBe(true);
    fireEvent.keyDown(document, { key: 'Escape' });
    fireEvent.click(dialog.parentElement!);
    for (const closeButton of within(dialog).getAllByRole('button', { name: '닫기' })) {
      fireEvent.click(closeButton);
    }
    expect(
      screen
        .getAllByRole('button', { name: '삭제' })
        .every((button) => button.hasAttribute('disabled')),
    ).toBe(true);
    fireEvent.click(within(dialog).getByRole('button', { name: '삭제' }));
    expect(mocks.deleteTrack).toHaveBeenCalledTimes(1);

    await act(async () => pendingDelete.resolve());
    await waitFor(() => expect(screen.queryByRole('dialog', { name: '음원 삭제' })).toBeNull());
  });

  it('does not repeat a committed delete when authoritative refresh needs recovery', async () => {
    mocks.fetchAdminTracks
      .mockResolvedValueOnce({ dataList: [track], pageInfo })
      .mockRejectedValueOnce(new Error('refresh failed'))
      .mockResolvedValueOnce({ dataList: [], pageInfo: { ...pageInfo, total: 0, end: 0 } });
    renderPage();

    expect(await screen.findByText('Admin Track')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: '삭제' }));
    const dialog = screen.getByRole('dialog', { name: '음원 삭제' });
    fireEvent.click(within(dialog).getByRole('button', { name: '삭제' }));

    expect(await within(dialog).findByRole('alert')).toHaveTextContent(
      '삭제는 완료됐지만 최신 목록을 불러오지 못했습니다.',
    );
    fireEvent.click(within(dialog).getByRole('button', { name: '목록 새로고침' }));

    await waitFor(() => expect(screen.queryByRole('dialog', { name: '음원 삭제' })).toBeNull());
    expect(mocks.deleteTrack).toHaveBeenCalledTimes(1);
    expect(mocks.fetchAdminTracks).toHaveBeenCalledTimes(3);
  });
});

import { act, fireEvent, render, screen, waitFor, within } from '@testing-library/react';
import { createMemoryRouter, RouterProvider } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import NoticeDetailPage from '@/pages/public/NoticeDetailPage';
import type { Notice } from '@/types';

const mocks = vi.hoisted(() => ({
  fetchNotice: vi.fn(),
  downloadNoticeAttachment: vi.fn(),
  triggerBlobDownload: vi.fn(),
}));

vi.mock('@/api/notices', () => ({
  fetchNotice: mocks.fetchNotice,
  downloadNoticeAttachment: mocks.downloadNoticeAttachment,
}));

vi.mock('@/api/downloads', () => ({
  triggerBlobDownload: mocks.triggerBlobDownload,
}));

function deferred<T>() {
  let resolve!: (value: T) => void;
  let reject!: (reason?: unknown) => void;
  const promise = new Promise<T>((res, rej) => {
    resolve = res;
    reject = rej;
  });
  return { promise, resolve, reject };
}

function notice(id: number, title = `Notice ${id}`): Notice {
  return {
    id,
    title,
    content: 'First line\nSecond line',
    isPinned: false,
    viewCount: 3,
    attachments: [
      { id: 11, originalName: 'first.txt', fileSize: 100 },
      { id: 12, originalName: 'second.txt', fileSize: 200 },
    ],
    createdAt: '2026-08-13T00:00:00Z',
    updatedAt: '2026-08-13T00:00:00Z',
  };
}

function renderPage(path = '/notices/1') {
  const router = createMemoryRouter(
    [
      { path: '/notices/:noticeId', element: <NoticeDetailPage /> },
      { path: '/notices', element: <div>notice-list</div> },
    ],
    { initialEntries: [path] },
  );
  const view = render(<RouterProvider router={router} />);
  return { ...view, router };
}

describe('NoticeDetailPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders a localized missing state without a meaningless retry', async () => {
    mocks.fetchNotice.mockRejectedValueOnce({ response: { status: 404 } });

    renderPage();

    expect(
      await screen.findByRole('heading', { name: '공지사항을 찾을 수 없습니다' }),
    ).toBeVisible();
    expect(screen.getByRole('link', { name: '공지사항 목록으로' })).toHaveAttribute(
      'href',
      '/notices',
    );
    expect(screen.queryByRole('button', { name: '다시 시도' })).not.toBeInTheDocument();
  });

  it('renders a retry only for a transient failure and retries exactly once', async () => {
    mocks.fetchNotice
      .mockRejectedValueOnce(Object.assign(new Error('offline'), { code: 'ECONNABORTED' }))
      .mockResolvedValueOnce(notice(1));

    renderPage();
    fireEvent.click(await screen.findByRole('button', { name: '다시 시도' }));

    expect(await screen.findByRole('heading', { name: 'Notice 1' })).toBeVisible();
    expect(mocks.fetchNotice).toHaveBeenCalledTimes(2);
  });

  it('keeps a public 5xx distinct from not-found and performs no implicit retry', async () => {
    mocks.fetchNotice.mockRejectedValueOnce({ response: { status: 503 } });

    renderPage();

    expect(
      await screen.findByText(/공지사항 정보를 불러오는 중 서버 오류가 발생했습니다\./),
    ).toBeVisible();
    expect(screen.getByRole('button', { name: '다시 시도' })).toBeEnabled();
    expect(mocks.fetchNotice).toHaveBeenCalledTimes(1);
  });

  it('retires the previous detail request on route switch and unmount', async () => {
    const first = deferred<Notice>();
    mocks.fetchNotice.mockReturnValueOnce(first.promise).mockResolvedValueOnce(notice(2));
    const view = renderPage('/notices/1');
    const firstSignal = mocks.fetchNotice.mock.calls[0][1] as AbortSignal;

    await act(async () => view.router.navigate('/notices/2'));
    expect(await screen.findByRole('heading', { name: 'Notice 2' })).toBeVisible();
    expect(firstSignal.aborted).toBe(true);

    await act(async () => first.resolve(notice(1, 'Retired notice')));
    expect(screen.queryByText('Retired notice')).not.toBeInTheDocument();

    const secondSignal = mocks.fetchNotice.mock.calls[1][1] as AbortSignal;
    view.unmount();
    expect(secondSignal.aborted).toBe(true);
  });

  it('moves from a valid route to an invalid route without issuing another read', async () => {
    mocks.fetchNotice.mockResolvedValueOnce(notice(1));
    const view = renderPage('/notices/1');
    expect(await screen.findByRole('heading', { name: 'Notice 1' })).toBeVisible();

    await act(async () => view.router.navigate('/notices/not-a-number'));

    expect(
      await screen.findByRole('heading', { name: '공지사항을 찾을 수 없습니다' }),
    ).toBeVisible();
    expect(mocks.fetchNotice).toHaveBeenCalledTimes(1);
    expect(mocks.downloadNoticeAttachment).not.toHaveBeenCalled();
    expect(mocks.triggerBlobDownload).not.toHaveBeenCalled();
  });

  it('owns download state per attachment, fences duplicates, and permits same-file retry', async () => {
    const first = deferred<Blob>();
    const secondBlob = new Blob(['second']);
    mocks.fetchNotice.mockResolvedValueOnce(notice(1));
    mocks.downloadNoticeAttachment
      .mockReturnValueOnce(first.promise)
      .mockResolvedValueOnce(secondBlob);

    renderPage();
    const attachmentList = await screen.findByRole('list', { name: '첨부파일' });
    const firstButton = within(attachmentList).getByRole('button', { name: 'first.txt' });
    const secondButton = within(attachmentList).getByRole('button', { name: 'second.txt' });
    fireEvent.click(firstButton);
    fireEvent.click(firstButton);
    fireEvent.click(secondButton);

    expect(mocks.downloadNoticeAttachment).toHaveBeenCalledTimes(2);
    expect(firstButton).toBeDisabled();
    expect(secondButton).toBeDisabled();
    await waitFor(() =>
      expect(mocks.triggerBlobDownload).toHaveBeenCalledWith(secondBlob, 'second.txt'),
    );

    await act(async () => first.reject(new Error('offline')));
    expect(await screen.findByText('first.txt 다운로드에 실패했습니다.')).toBeVisible();
    expect(within(attachmentList).getByRole('button', { name: 'first.txt' })).toBeEnabled();

    const retryBlob = new Blob(['retry']);
    mocks.downloadNoticeAttachment.mockResolvedValueOnce(retryBlob);
    fireEvent.click(within(attachmentList).getByRole('button', { name: 'first.txt' }));
    await waitFor(() =>
      expect(mocks.triggerBlobDownload).toHaveBeenCalledWith(retryBlob, 'first.txt'),
    );
    expect(mocks.downloadNoticeAttachment).toHaveBeenCalledTimes(3);
    expect(screen.queryByText('first.txt 다운로드에 실패했습니다.')).not.toBeInTheDocument();
  });

  it('aborts an attachment request and ignores its stale bytes after target replacement', async () => {
    const oldDownload = deferred<Blob>();
    mocks.fetchNotice.mockResolvedValueOnce(notice(1)).mockResolvedValueOnce(notice(2));
    mocks.downloadNoticeAttachment.mockReturnValueOnce(oldDownload.promise);
    const view = renderPage('/notices/1');
    fireEvent.click(await screen.findByRole('button', { name: 'first.txt' }));
    const signal = mocks.downloadNoticeAttachment.mock.calls[0][2] as AbortSignal;

    await act(async () => view.router.navigate('/notices/2'));
    expect(await screen.findByRole('heading', { name: 'Notice 2' })).toBeVisible();
    expect(signal.aborted).toBe(true);
    await act(async () => oldDownload.resolve(new Blob(['stale'])));

    expect(mocks.triggerBlobDownload).not.toHaveBeenCalled();
  });

  it('retires a direct-unmount download without triggering a browser effect', async () => {
    const pendingDownload = deferred<Blob>();
    mocks.fetchNotice.mockResolvedValueOnce(notice(1));
    mocks.downloadNoticeAttachment.mockReturnValueOnce(pendingDownload.promise);
    const view = renderPage('/notices/1');
    fireEvent.click(await screen.findByRole('button', { name: 'first.txt' }));
    const signal = mocks.downloadNoticeAttachment.mock.calls[0][2] as AbortSignal;

    view.unmount();
    expect(signal.aborted).toBe(true);
    await act(async () => pendingDownload.resolve(new Blob(['retired'])));

    expect(mocks.fetchNotice).toHaveBeenCalledTimes(1);
    expect(mocks.downloadNoticeAttachment).toHaveBeenCalledTimes(1);
    expect(mocks.triggerBlobDownload).not.toHaveBeenCalled();
  });
});

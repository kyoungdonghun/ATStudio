import { act, fireEvent, render, screen, waitFor, within } from '@testing-library/react';
import { createMemoryRouter, RouterProvider } from 'react-router-dom';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import NoticeCreatePage from '@/pages/admin/NoticeCreatePage';
import NoticeEditPage from '@/pages/admin/NoticeEditPage';
import NoticeDetailPage from '@/pages/public/NoticeDetailPage';
import NoticeListPage from '@/pages/public/NoticeListPage';
import type { Notice } from '@/types';
import { clearNoticeCreateObservation } from '@/utils/noticeCreateObservationFence';

const mocks = vi.hoisted(() => ({
  createNotice: vi.fn(),
  fetchNotices: vi.fn(),
  fetchAdminNotice: vi.fn(),
  updateNotice: vi.fn(),
  deleteNotice: vi.fn(),
  fetchNotice: vi.fn(),
  downloadNoticeAttachment: vi.fn(),
  triggerBlobDownload: vi.fn(),
}));

const authState = vi.hoisted(() => ({
  user: { id: 7 },
  accessToken: 'admin-token',
  role: 'ADMIN' as const,
}));

vi.mock('@/api/notices', () => mocks);
vi.mock('@/api/downloads', () => ({
  triggerBlobDownload: mocks.triggerBlobDownload,
}));
vi.mock('@/store/authStore', () => ({
  useAuthStore: Object.assign(
    (selector: (state: typeof authState) => unknown) => selector(authState),
    { getState: () => authState },
  ),
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

function editProjection(title = '기존 공지') {
  return {
    title,
    content: '기존 내용',
    isPinned: false,
    attachments: [{ id: 4, originalName: 'old.pdf', fileSize: 100 }],
  };
}

function createdNotice(): Notice {
  return {
    id: 1,
    title: '새 공지',
    content: '내용',
    isPinned: false,
    viewCount: 0,
    attachments: [],
    createdAt: '2026-08-13T00:00:00Z',
    updatedAt: '2026-08-13T00:00:00Z',
  };
}

function notice(id: number): Notice {
  return {
    ...createdNotice(),
    id,
    title: `Notice ${id}`,
  };
}

const emptyPage = {
  dataList: [],
  pageInfo: {
    page: 1,
    size: 20,
    total: 0,
    start: 0,
    end: 0,
    prev: false,
    next: false,
  },
};

function beforeUnloadCallCount(spy: { mock: { calls: unknown[][] } }): number {
  return spy.mock.calls.filter((call) => call[0] === 'beforeunload').length;
}

function lastElement<T>(items: T[]): T {
  return items[items.length - 1];
}

function renderRoute(
  element: React.ReactNode,
  route: string,
  path: string,
  noticeDetail: React.ReactNode = <div>notice-detail</div>,
) {
  const router = createMemoryRouter(
    [
      { path: route, element },
      { path: '/notices', element: <div>notice-list</div> },
      { path: '/notices/:noticeId', element: noticeDetail },
    ],
    { initialEntries: [path] },
  );
  const view = render(<RouterProvider router={router} />);
  return { ...view, router };
}

describe('Notice ADMIN create and edit pages', () => {
  beforeEach(() => {
    Object.values(mocks).forEach((mock) => mock.mockReset());
    clearNoticeCreateObservation();
    sessionStorage.clear();
    authState.user = { id: 7 };
    authState.accessToken = 'admin-token';
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('uses associated Korean controls, the canonical content maximum, and one create fence', async () => {
    const addEventListener = vi.spyOn(window, 'addEventListener');
    const removeEventListener = vi.spyOn(window, 'removeEventListener');
    const pending = deferred<Notice>();
    mocks.createNotice.mockReturnValueOnce(pending.promise);
    const view = renderRoute(<NoticeCreatePage />, '/admin/notices/new', '/admin/notices/new');
    const file = new File(['notice'], 'notice.txt', { type: 'text/plain' });

    fireEvent.change(screen.getByLabelText('제목'), { target: { value: '  새 공지  ' } });
    fireEvent.change(screen.getByLabelText('내용'), { target: { value: '  본문  ' } });
    fireEvent.click(screen.getByLabelText('상단 고정'));
    fireEvent.change(screen.getByLabelText('첨부파일'), { target: { files: [file] } });
    const submit = screen.getByRole('button', { name: '등록' });
    fireEvent.click(submit);
    fireEvent.click(submit);

    await waitFor(() => expect(beforeUnloadCallCount(addEventListener)).toBe(1));
    expect(beforeUnloadCallCount(removeEventListener)).toBe(0);

    const beforeUnload = new Event('beforeunload', { cancelable: true });
    window.dispatchEvent(beforeUnload);
    expect(beforeUnload.defaultPrevented).toBe(true);
    await act(async () => {
      await view.router.navigate('/notices');
    });
    expect(view.router.state.location.pathname).toBe('/admin/notices/new');

    expect(mocks.createNotice).toHaveBeenCalledTimes(1);
    expect(mocks.createNotice).toHaveBeenCalledWith({
      title: '새 공지',
      content: '본문',
      isPinned: true,
      attachments: [file],
    });
    expect(screen.getByLabelText('제목')).toBeDisabled();
    expect(screen.getByLabelText('내용')).toHaveAttribute('maxLength', '1000');
    expect(screen.getByLabelText('내용')).toBeDisabled();
    expect(screen.getByLabelText('첨부파일')).toBeDisabled();
    expect(screen.getByRole('button', { name: '취소' })).toBeDisabled();

    await act(async () => pending.resolve(createdNotice()));
    await waitFor(() => expect(view.router.state.location.pathname).toBe('/notices'));
    expect(beforeUnloadCallCount(addEventListener)).toBe(1);
    expect(beforeUnloadCallCount(removeEventListener)).toBe(1);
    const releasedBeforeUnload = new Event('beforeunload', { cancelable: true });
    window.dispatchEvent(releasedBeforeUnload);
    expect(releasedBeforeUnload.defaultPrevented).toBe(false);
  });

  it('keeps create input for an authoritative bounded retry and never retries automatically', async () => {
    mocks.createNotice
      .mockRejectedValueOnce({ response: { status: 400 } })
      .mockResolvedValueOnce(createdNotice());
    const view = renderRoute(<NoticeCreatePage />, '/admin/notices/new', '/admin/notices/new');
    fireEvent.change(screen.getByLabelText('제목'), { target: { value: '재시도 공지' } });
    fireEvent.change(screen.getByLabelText('내용'), { target: { value: '재시도 내용' } });

    fireEvent.click(screen.getByRole('button', { name: '등록' }));

    expect(await screen.findByText(/공지사항을 등록하지 못했습니다\./)).toBeVisible();
    expect(screen.getByLabelText('제목')).toHaveValue('재시도 공지');
    expect(screen.getByLabelText('내용')).toHaveValue('재시도 내용');
    expect(mocks.createNotice).toHaveBeenCalledTimes(1);

    fireEvent.click(screen.getByRole('button', { name: '등록' }));
    await waitFor(() => expect(view.router.state.location.pathname).toBe('/notices'));
    expect(mocks.createNotice).toHaveBeenCalledTimes(2);
  });

  it('blocks browser history while create is pending and does not abort on direct unmount', async () => {
    const addEventListener = vi.spyOn(window, 'addEventListener');
    const removeEventListener = vi.spyOn(window, 'removeEventListener');
    const pending = deferred<Notice>();
    mocks.createNotice.mockReturnValueOnce(pending.promise);
    const router = createMemoryRouter(
      [
        { path: '/admin/notices/new', element: <NoticeCreatePage /> },
        { path: '/notices', element: <div>notice-list</div> },
      ],
      {
        initialEntries: ['/notices', '/admin/notices/new'],
        initialIndex: 1,
      },
    );
    const view = render(<RouterProvider router={router} />);
    fireEvent.change(screen.getByLabelText('제목'), { target: { value: '히스토리 공지' } });
    fireEvent.change(screen.getByLabelText('내용'), { target: { value: '히스토리 내용' } });
    fireEvent.click(screen.getByRole('button', { name: '등록' }));

    await act(async () => router.navigate(-1));
    expect(router.state.location.pathname).toBe('/admin/notices/new');
    expect(mocks.createNotice).toHaveBeenCalledTimes(1);
    expect(mocks.createNotice.mock.calls[0]).toHaveLength(1);

    view.unmount();
    expect(beforeUnloadCallCount(addEventListener)).toBe(1);
    expect(beforeUnloadCallCount(removeEventListener)).toBe(1);
    await act(async () => pending.resolve(createdNotice()));
    expect(mocks.createNotice).toHaveBeenCalledTimes(1);
  });

  it('requires observation after an ambiguous create outcome and never repeats POST', async () => {
    mocks.createNotice.mockRejectedValueOnce(new Error('response lost'));
    renderRoute(<NoticeCreatePage />, '/admin/notices/new', '/admin/notices/new');
    fireEvent.change(screen.getByLabelText('제목'), { target: { value: '결과 미상 공지' } });
    fireEvent.change(screen.getByLabelText('내용'), { target: { value: '결과 미상 내용' } });

    fireEvent.click(screen.getByRole('button', { name: '등록' }));

    expect(await screen.findByText('처리 결과 확인 필요')).toBeVisible();
    const submit = screen.getByRole('button', { name: '등록' });
    expect(submit).toBeDisabled();
    fireEvent.click(submit);
    expect(screen.getByRole('link', { name: '공지사항 목록에서 확인' })).toHaveAttribute(
      'href',
      '/notices',
    );
    expect(mocks.createNotice).toHaveBeenCalledTimes(1);
  });

  it('owns beforeunload only while pending and releases it after 4xx and ambiguity', async () => {
    const addEventListener = vi.spyOn(window, 'addEventListener');
    const removeEventListener = vi.spyOn(window, 'removeEventListener');
    const authoritative = deferred<Notice>();
    const ambiguous = deferred<Notice>();
    mocks.createNotice
      .mockReturnValueOnce(authoritative.promise)
      .mockReturnValueOnce(ambiguous.promise);
    const view = renderRoute(<NoticeCreatePage />, '/admin/notices/new', '/admin/notices/new');
    const [title, content] = screen.getAllByRole('textbox');
    fireEvent.change(title, { target: { value: 'lifecycle title' } });
    fireEvent.change(content, { target: { value: 'lifecycle content' } });
    const submit = () => lastElement(screen.getAllByRole('button'));

    expect(beforeUnloadCallCount(addEventListener)).toBe(0);
    expect(beforeUnloadCallCount(removeEventListener)).toBe(0);
    fireEvent.click(submit());
    await waitFor(() => expect(beforeUnloadCallCount(addEventListener)).toBe(1));
    await act(async () => view.router.navigate('/notices'));
    expect(view.router.state.location.pathname).toBe('/admin/notices/new');
    expect(mocks.createNotice).toHaveBeenCalledTimes(1);

    await act(async () => authoritative.reject({ response: { status: 400 } }));
    await waitFor(() => expect(beforeUnloadCallCount(removeEventListener)).toBe(1));
    fireEvent.click(submit());
    await waitFor(() => expect(beforeUnloadCallCount(addEventListener)).toBe(2));
    await act(async () => view.router.navigate('/notices'));
    expect(view.router.state.location.pathname).toBe('/admin/notices/new');
    expect(mocks.createNotice).toHaveBeenCalledTimes(2);

    await act(async () => ambiguous.reject(new Error('response lost')));
    await waitFor(() => expect(beforeUnloadCallCount(removeEventListener)).toBe(2));
    expect(submit()).toBeDisabled();
    expect(mocks.createNotice).toHaveBeenCalledTimes(2);
  });

  it('keeps an ambiguous create fenced across unrelated routes until a successful list GET', async () => {
    const ambiguous = deferred<Notice>();
    mocks.createNotice
      .mockReturnValueOnce(ambiguous.promise)
      .mockResolvedValueOnce(createdNotice());
    mocks.fetchNotices
      .mockRejectedValueOnce({ response: { status: 503 } })
      .mockRejectedValueOnce({ code: 'ERR_CANCELED' })
      .mockResolvedValue(emptyPage);
    const router = createMemoryRouter(
      [
        { path: '/admin/notices/new', element: <NoticeCreatePage /> },
        { path: '/admin/dashboard', element: <div>admin-dashboard</div> },
        { path: '/notices', element: <NoticeListPage /> },
      ],
      { initialEntries: ['/admin/notices/new'] },
    );
    render(<RouterProvider router={router} />);
    let [title, content] = screen.getAllByRole('textbox');
    const file = new File(['private notice body'], 'private-notice.txt', { type: 'text/plain' });
    fireEvent.change(title, { target: { value: 'private notice title' } });
    fireEvent.change(content, { target: { value: 'private notice content' } });
    fireEvent.change(document.querySelector('input[type="file"]')!, { target: { files: [file] } });
    fireEvent.click(lastElement(screen.getAllByRole('button')));

    await act(async () => router.navigate('/admin/dashboard'));
    expect(router.state.location.pathname).toBe('/admin/notices/new');
    expect(mocks.createNotice).toHaveBeenCalledTimes(1);
    await act(async () => ambiguous.reject(new Error('response lost')));
    expect(await screen.findByRole('alert')).toBeVisible();
    expect(sessionStorage).toHaveLength(1);
    const storageKey = sessionStorage.key(0)!;
    const storedFence = `${storageKey}:${sessionStorage.getItem(storageKey)}`;
    expect(storedFence).not.toContain('private notice title');
    expect(storedFence).not.toContain('private notice content');
    expect(storedFence).not.toContain('private-notice.txt');

    await act(async () => router.navigate('/admin/dashboard'));
    await act(async () => router.navigate('/admin/notices/new'));
    expect(lastElement(screen.getAllByRole('button'))).toBeDisabled();
    fireEvent.click(lastElement(screen.getAllByRole('button')));
    expect(mocks.createNotice).toHaveBeenCalledTimes(1);

    await act(async () => router.navigate('/notices'));
    expect(await screen.findByRole('alert')).toBeVisible();
    expect(mocks.fetchNotices).toHaveBeenCalledTimes(1);
    await act(async () => router.navigate('/admin/notices/new'));
    expect(lastElement(screen.getAllByRole('button'))).toBeDisabled();

    await act(async () => router.navigate('/notices'));
    await waitFor(() => expect(mocks.fetchNotices).toHaveBeenCalledTimes(2));
    expect(await screen.findByRole('table')).toBeVisible();
    await act(async () => router.navigate('/admin/notices/new'));
    expect(lastElement(screen.getAllByRole('button'))).toBeDisabled();

    await act(async () => router.navigate('/notices'));
    await waitFor(() => expect(mocks.fetchNotices).toHaveBeenCalledTimes(3));
    expect(await screen.findByRole('table')).toBeVisible();
    await act(async () => router.navigate('/admin/notices/new'));
    expect(lastElement(screen.getAllByRole('button'))).toBeEnabled();
    [title, content] = screen.getAllByRole('textbox');
    fireEvent.change(title, { target: { value: 'private notice title' } });
    fireEvent.change(content, { target: { value: 'private notice content' } });
    fireEvent.click(lastElement(screen.getAllByRole('button')));
    await waitFor(() => expect(router.state.location.pathname).toBe('/notices'));
    expect(mocks.createNotice).toHaveBeenCalledTimes(2);
  });

  it('keeps the create fence in memory when session storage is unavailable', async () => {
    vi.spyOn(Storage.prototype, 'getItem').mockImplementation(function (this: Storage) {
      if (this === sessionStorage) throw new Error('storage unavailable');
      return null;
    });
    vi.spyOn(Storage.prototype, 'setItem').mockImplementation(function (this: Storage) {
      if (this === sessionStorage) throw new Error('storage unavailable');
    });
    vi.spyOn(Storage.prototype, 'removeItem').mockImplementation(function (this: Storage) {
      if (this === sessionStorage) throw new Error('storage unavailable');
    });
    mocks.createNotice.mockRejectedValueOnce(new Error('response lost'));
    mocks.fetchNotices.mockResolvedValue(emptyPage);
    const router = createMemoryRouter(
      [
        { path: '/admin/notices/new', element: <NoticeCreatePage /> },
        { path: '/admin/dashboard', element: <div>admin-dashboard</div> },
        { path: '/notices', element: <NoticeListPage /> },
      ],
      { initialEntries: ['/admin/notices/new'] },
    );
    render(<RouterProvider router={router} />);
    const [title, content] = screen.getAllByRole('textbox');
    fireEvent.change(title, { target: { value: 'fallback title' } });
    fireEvent.change(content, { target: { value: 'fallback content' } });
    fireEvent.click(lastElement(screen.getAllByRole('button')));
    expect(await screen.findByRole('alert')).toBeVisible();

    await act(async () => router.navigate('/admin/dashboard'));
    await act(async () => router.navigate('/admin/notices/new'));
    expect(lastElement(screen.getAllByRole('button'))).toBeDisabled();
    expect(mocks.createNotice).toHaveBeenCalledTimes(1);

    await act(async () => router.navigate('/notices'));
    expect(await screen.findByRole('table')).toBeVisible();
    await act(async () => router.navigate('/admin/notices/new'));
    expect(lastElement(screen.getAllByRole('button'))).toBeEnabled();
  });

  it('retries fence removal on a later successful list observation after remove-only failure', async () => {
    mocks.createNotice
      .mockRejectedValueOnce(new Error('response lost'))
      .mockResolvedValueOnce(createdNotice());
    mocks.fetchNotices.mockResolvedValue(emptyPage);
    const router = createMemoryRouter(
      [
        { path: '/admin/notices/new', element: <NoticeCreatePage /> },
        { path: '/notices', element: <NoticeListPage /> },
      ],
      { initialEntries: ['/admin/notices/new'] },
    );
    render(<RouterProvider router={router} />);
    let [title, content] = screen.getAllByRole('textbox');
    fireEvent.change(title, { target: { value: 'remove retry title' } });
    fireEvent.change(content, { target: { value: 'remove retry content' } });
    fireEvent.click(lastElement(screen.getAllByRole('button')));
    expect(await screen.findByText('처리 결과 확인 필요')).toBeVisible();
    expect(mocks.createNotice).toHaveBeenCalledTimes(1);

    const nativeRemoveItem = Storage.prototype.removeItem;
    let sessionRemovalAttempts = 0;
    vi.spyOn(Storage.prototype, 'removeItem').mockImplementation(function (
      this: Storage,
      key: string,
    ) {
      if (this === sessionStorage) {
        sessionRemovalAttempts += 1;
        if (sessionRemovalAttempts === 1) throw new DOMException('remove blocked');
      }
      nativeRemoveItem.call(this, key);
    });

    await act(async () => router.navigate('/notices'));
    expect(await screen.findByRole('table')).toBeVisible();
    expect(mocks.fetchNotices).toHaveBeenCalledTimes(1);
    expect(sessionRemovalAttempts).toBe(1);

    await act(async () => router.navigate('/admin/notices/new'));
    expect(lastElement(screen.getAllByRole('button'))).toBeDisabled();
    fireEvent.click(lastElement(screen.getAllByRole('button')));
    expect(mocks.createNotice).toHaveBeenCalledTimes(1);

    await act(async () => router.navigate('/notices'));
    await waitFor(() => expect(mocks.fetchNotices).toHaveBeenCalledTimes(2));
    expect(await screen.findByRole('table')).toBeVisible();
    expect(sessionRemovalAttempts).toBe(2);

    await act(async () => router.navigate('/admin/notices/new'));
    expect(lastElement(screen.getAllByRole('button'))).toBeEnabled();
    [title, content] = screen.getAllByRole('textbox');
    fireEvent.change(title, { target: { value: 'remove retry title' } });
    fireEvent.change(content, { target: { value: 'remove retry content' } });
    fireEvent.click(lastElement(screen.getAllByRole('button')));

    await waitFor(() => expect(router.state.location.pathname).toBe('/notices'));
    await waitFor(() => expect(mocks.fetchNotices).toHaveBeenCalledTimes(3));
    expect(mocks.createNotice).toHaveBeenCalledTimes(2);
    expect(sessionRemovalAttempts).toBe(2);
  });

  it('retries only the destination list GET after a completed create', async () => {
    mocks.createNotice.mockResolvedValueOnce(createdNotice());
    mocks.fetchNotices
      .mockRejectedValueOnce({ response: { status: 503 } })
      .mockResolvedValueOnce(emptyPage);
    const router = createMemoryRouter(
      [
        { path: '/admin/notices/new', element: <NoticeCreatePage /> },
        { path: '/notices', element: <NoticeListPage /> },
      ],
      { initialEntries: ['/admin/notices/new'] },
    );
    render(<RouterProvider router={router} />);
    const [title, content] = screen.getAllByRole('textbox');
    fireEvent.change(title, { target: { value: 'destination title' } });
    fireEvent.change(content, { target: { value: 'destination content' } });
    fireEvent.click(lastElement(screen.getAllByRole('button')));

    const alert = await screen.findByRole('alert');
    expect(mocks.createNotice).toHaveBeenCalledTimes(1);
    expect(mocks.fetchNotices).toHaveBeenCalledTimes(1);
    fireEvent.click(within(alert).getByRole('button'));
    expect(await screen.findByRole('table')).toBeVisible();
    expect(mocks.fetchNotices).toHaveBeenCalledTimes(2);
    expect(mocks.createNotice).toHaveBeenCalledTimes(1);
    expect(mocks.updateNotice).not.toHaveBeenCalled();
    expect(mocks.deleteNotice).not.toHaveBeenCalled();
  });

  it.each(['abc', '0', '-1', '01', '9007199254740992'])(
    'rejects malformed edit ID %s without any Notice API invocation',
    async (rawID) => {
      renderRoute(
        <NoticeEditPage />,
        '/admin/notices/:noticeId/edit',
        `/admin/notices/${rawID}/edit`,
      );

      expect(await screen.findByText('올바르지 않은 공지사항 주소입니다.')).toBeVisible();
      expect(screen.getByRole('link', { name: '공지사항 목록으로' })).toHaveAttribute(
        'href',
        '/notices',
      );
      expect(mocks.fetchAdminNotice).not.toHaveBeenCalled();
      expect(mocks.updateNotice).not.toHaveBeenCalled();
      expect(mocks.deleteNotice).not.toHaveBeenCalled();
    },
  );

  it('loads only through the ADMIN read and retires an older route target', async () => {
    const first = deferred<ReturnType<typeof editProjection>>();
    mocks.fetchAdminNotice
      .mockReturnValueOnce(first.promise)
      .mockResolvedValueOnce(editProjection('현재 공지'));
    const view = renderRoute(
      <NoticeEditPage />,
      '/admin/notices/:noticeId/edit',
      '/admin/notices/1/edit',
    );
    const oldSignal = mocks.fetchAdminNotice.mock.calls[0][1] as AbortSignal;

    await act(async () => view.router.navigate('/admin/notices/2/edit'));
    expect(await screen.findByDisplayValue('현재 공지')).toBeVisible();
    expect(oldSignal.aborted).toBe(true);
    await act(async () => first.resolve(editProjection('이전 공지')));

    expect(screen.queryByDisplayValue('이전 공지')).not.toBeInTheDocument();
    expect(mocks.fetchAdminNotice).toHaveBeenNthCalledWith(2, 2, expect.any(AbortSignal));
  });

  it('offers one manual retry for a transient ADMIN edit load failure', async () => {
    mocks.fetchAdminNotice
      .mockRejectedValueOnce(Object.assign(new Error('offline'), { code: 'ECONNABORTED' }))
      .mockResolvedValueOnce(editProjection());
    renderRoute(<NoticeEditPage />, '/admin/notices/:noticeId/edit', '/admin/notices/9/edit');

    fireEvent.click(await screen.findByRole('button', { name: '다시 시도' }));

    expect(await screen.findByDisplayValue('기존 공지')).toBeVisible();
    expect(mocks.fetchAdminNotice).toHaveBeenCalledTimes(2);
  });

  it('coordinates save, attachment state, navigation, and duplicate submit with one owner', async () => {
    const pending = deferred<Notice>();
    mocks.fetchAdminNotice.mockResolvedValueOnce(editProjection());
    mocks.updateNotice.mockReturnValueOnce(pending.promise).mockResolvedValueOnce(createdNotice());
    const view = renderRoute(
      <NoticeEditPage />,
      '/admin/notices/:noticeId/edit',
      '/admin/notices/9/edit',
    );
    expect(await screen.findByDisplayValue('기존 공지')).toBeVisible();
    fireEvent.change(screen.getByLabelText('제목'), { target: { value: '수정 공지' } });
    fireEvent.click(screen.getByRole('button', { name: 'old.pdf 삭제' }));
    expect(screen.getByText('삭제 예정')).toBeVisible();
    const added = new File(['new'], 'new.txt', { type: 'text/plain' });
    fireEvent.change(screen.getByLabelText('첨부파일'), { target: { files: [added] } });

    const save = screen.getByRole('button', { name: '저장' });
    fireEvent.click(save);
    fireEvent.click(save);

    expect(mocks.updateNotice).toHaveBeenCalledTimes(1);
    expect(mocks.updateNotice).toHaveBeenCalledWith(9, {
      title: '수정 공지',
      content: '기존 내용',
      isPinned: false,
      deleteAttachmentIds: [4],
      newAttachments: [added],
    });
    expect(screen.getByLabelText('제목')).toBeDisabled();
    expect(screen.getByLabelText('내용')).toHaveAttribute('maxLength', '1000');
    expect(screen.getByLabelText('첨부파일')).toBeDisabled();
    expect(screen.getByRole('button', { name: '공지사항 삭제' })).toBeDisabled();
    expect(screen.getByRole('button', { name: '취소' })).toBeDisabled();

    await act(async () => pending.reject({ response: { status: 400 } }));
    expect(await screen.findByText(/공지사항을 수정하지 못했습니다\./)).toBeVisible();
    expect(screen.getByLabelText('제목')).toBeEnabled();
    expect(screen.getByRole('button', { name: 'old.pdf 삭제 취소' })).toBeEnabled();
    expect(mocks.updateNotice).toHaveBeenCalledTimes(1);

    fireEvent.click(screen.getByRole('button', { name: '저장' }));
    await waitFor(() => expect(view.router.state.location.pathname).toBe('/notices/9'));
    expect(mocks.updateNotice).toHaveBeenCalledTimes(2);
  });

  it('requires a fresh ADMIN read after an ambiguous attachment update and never repeats PUT', async () => {
    mocks.fetchAdminNotice
      .mockResolvedValueOnce(editProjection())
      .mockResolvedValueOnce(editProjection('확인된 공지'));
    mocks.updateNotice.mockRejectedValueOnce(new Error('response lost'));
    renderRoute(<NoticeEditPage />, '/admin/notices/:noticeId/edit', '/admin/notices/9/edit');
    expect(await screen.findByDisplayValue('기존 공지')).toBeVisible();
    const added = new File(['new'], 'new.txt', { type: 'text/plain' });
    fireEvent.change(screen.getByLabelText('첨부파일'), { target: { files: [added] } });

    fireEvent.click(screen.getByRole('button', { name: '저장' }));

    expect(await screen.findByText('처리 결과 확인 필요')).toBeVisible();
    const save = screen.getByRole('button', { name: '저장' });
    expect(save).toBeDisabled();
    fireEvent.click(save);
    expect(mocks.updateNotice).toHaveBeenCalledTimes(1);

    fireEvent.click(screen.getByRole('button', { name: '현재 상태 다시 확인' }));
    expect(await screen.findByDisplayValue('확인된 공지')).toBeVisible();
    expect(mocks.fetchAdminNotice).toHaveBeenCalledTimes(2);
    expect(mocks.updateNotice).toHaveBeenCalledTimes(1);
  });

  it('keeps a failed delete associated with its modal and fences close and duplicate calls', async () => {
    const pending = deferred<void>();
    mocks.fetchAdminNotice.mockResolvedValueOnce(editProjection());
    mocks.deleteNotice.mockReturnValueOnce(pending.promise);
    renderRoute(<NoticeEditPage />, '/admin/notices/:noticeId/edit', '/admin/notices/9/edit');
    expect(await screen.findByDisplayValue('기존 공지')).toBeVisible();
    fireEvent.click(screen.getByRole('button', { name: '공지사항 삭제' }));
    const dialog = screen.getByRole('dialog');
    const confirm = within(dialog).getByRole('button', { name: '삭제' });
    fireEvent.click(confirm);
    fireEvent.click(confirm);
    const close = within(dialog).getByRole('button', { name: '닫기' });

    expect(mocks.deleteNotice).toHaveBeenCalledTimes(1);
    expect(dialog).toHaveAttribute('aria-busy', 'true');
    expect(close).toBeDisabled();
    dialog.focus();
    const tab = new KeyboardEvent('keydown', { key: 'Tab', bubbles: true, cancelable: true });
    document.dispatchEvent(tab);
    expect(tab.defaultPrevented).toBe(true);
    expect(dialog).toHaveFocus();
    const shiftTab = new KeyboardEvent('keydown', {
      key: 'Tab',
      shiftKey: true,
      bubbles: true,
      cancelable: true,
    });
    document.dispatchEvent(shiftTab);
    expect(shiftTab.defaultPrevented).toBe(true);
    expect(dialog).toHaveFocus();
    fireEvent.keyDown(document, { key: 'Escape' });
    fireEvent.click(dialog.parentElement!);
    expect(screen.getByRole('dialog')).toBeVisible();
    expect(within(dialog).getByRole('button', { name: '취소' })).toBeDisabled();

    await act(async () => pending.reject({ response: { status: 409 } }));
    expect(await within(dialog).findByText(/공지사항을 삭제하지 못했습니다\./)).toBeVisible();
    expect(dialog).not.toHaveAttribute('aria-busy');
    expect(close).toBeEnabled();
    dialog.focus();
    fireEvent.keyDown(document, { key: 'Tab' });
    expect(close).toHaveFocus();
    expect(within(dialog).getByRole('button', { name: '삭제' })).toBeEnabled();
    fireEvent.click(close);
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument();

    mocks.deleteNotice.mockResolvedValueOnce(undefined);
    fireEvent.click(screen.getByRole('button', { name: '공지사항 삭제' }));
    fireEvent.click(within(screen.getByRole('dialog')).getByRole('button', { name: '삭제' }));
    await waitFor(() => expect(screen.getByText('notice-list')).toBeVisible());
    expect(mocks.deleteNotice).toHaveBeenCalledTimes(2);
  });

  it('blocks a pending save target change, preserves the PUT, and navigates after success', async () => {
    const pending = deferred<Notice>();
    mocks.fetchAdminNotice.mockResolvedValueOnce(editProjection('첫 공지'));
    mocks.updateNotice.mockReturnValueOnce(pending.promise);
    const view = renderRoute(
      <NoticeEditPage />,
      '/admin/notices/:noticeId/edit',
      '/admin/notices/9/edit',
    );
    expect(await screen.findByDisplayValue('첫 공지')).toBeVisible();
    fireEvent.click(screen.getByRole('button', { name: '저장' }));
    await act(async () => view.router.navigate('/admin/notices/10/edit'));
    expect(view.router.state.location.pathname).toBe('/admin/notices/9/edit');
    expect(screen.getByDisplayValue('첫 공지')).toBeVisible();
    await act(async () => pending.resolve(createdNotice()));

    await waitFor(() => expect(view.router.state.location.pathname).toBe('/notices/9'));
    expect(mocks.fetchAdminNotice).toHaveBeenCalledTimes(1);
    expect(mocks.updateNotice).toHaveBeenCalledTimes(1);
  });

  it('requires observation after an ambiguous delete and never repeats DELETE', async () => {
    mocks.fetchAdminNotice
      .mockResolvedValueOnce(editProjection())
      .mockRejectedValueOnce({ response: { status: 404 } });
    mocks.deleteNotice.mockRejectedValueOnce(new Error('response lost'));
    renderRoute(<NoticeEditPage />, '/admin/notices/:noticeId/edit', '/admin/notices/9/edit');
    expect(await screen.findByDisplayValue('기존 공지')).toBeVisible();
    fireEvent.click(screen.getByRole('button', { name: '공지사항 삭제' }));
    fireEvent.click(within(screen.getByRole('dialog')).getByRole('button', { name: '삭제' }));

    expect(await screen.findByText('처리 결과 확인 필요')).toBeVisible();
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
    expect(screen.getByRole('button', { name: '공지사항 삭제' })).toBeDisabled();
    expect(mocks.deleteNotice).toHaveBeenCalledTimes(1);

    fireEvent.click(screen.getByRole('button', { name: '현재 상태 다시 확인' }));
    expect(await screen.findByText('공지사항을 찾을 수 없습니다.')).toBeVisible();
    expect(mocks.fetchAdminNotice).toHaveBeenCalledTimes(2);
    expect(mocks.deleteNotice).toHaveBeenCalledTimes(1);
  });

  it('retries only the destination list GET after a completed delete', async () => {
    mocks.fetchAdminNotice.mockResolvedValueOnce(editProjection());
    mocks.deleteNotice.mockResolvedValueOnce(undefined);
    mocks.fetchNotices
      .mockRejectedValueOnce({ response: { status: 503 } })
      .mockResolvedValueOnce(emptyPage);
    const router = createMemoryRouter(
      [
        { path: '/admin/notices/:noticeId/edit', element: <NoticeEditPage /> },
        { path: '/notices', element: <NoticeListPage /> },
      ],
      { initialEntries: ['/admin/notices/9/edit'] },
    );
    render(<RouterProvider router={router} />);
    expect(await screen.findAllByRole('textbox')).toHaveLength(2);
    fireEvent.click(screen.getAllByRole('button')[1]);
    const dialog = screen.getByRole('dialog');
    fireEvent.click(lastElement(within(dialog).getAllByRole('button')));

    const alert = await screen.findByRole('alert');
    expect(mocks.fetchAdminNotice).toHaveBeenCalledTimes(1);
    expect(mocks.deleteNotice).toHaveBeenCalledTimes(1);
    expect(mocks.fetchNotices).toHaveBeenCalledTimes(1);
    fireEvent.click(within(alert).getByRole('button'));
    expect(await screen.findByRole('table')).toBeVisible();
    expect(mocks.fetchNotices).toHaveBeenCalledTimes(2);
    expect(mocks.deleteNotice).toHaveBeenCalledTimes(1);
    expect(mocks.createNotice).not.toHaveBeenCalled();
    expect(mocks.updateNotice).not.toHaveBeenCalled();
  });

  it('retries only the destination GET after a completed update reaches a read failure', async () => {
    mocks.fetchAdminNotice.mockResolvedValueOnce(editProjection());
    mocks.updateNotice.mockResolvedValueOnce(createdNotice());
    mocks.fetchNotice
      .mockRejectedValueOnce({ response: { status: 503 } })
      .mockResolvedValueOnce(notice(9));
    renderRoute(
      <NoticeEditPage />,
      '/admin/notices/:noticeId/edit',
      '/admin/notices/9/edit',
      <NoticeDetailPage />,
    );
    expect(await screen.findByDisplayValue('기존 공지')).toBeVisible();

    fireEvent.click(screen.getByRole('button', { name: '저장' }));

    expect(
      await screen.findByText(/공지사항 정보를 불러오는 중 서버 오류가 발생했습니다\./),
    ).toBeVisible();
    expect(mocks.fetchAdminNotice).toHaveBeenCalledTimes(1);
    expect(mocks.updateNotice).toHaveBeenCalledTimes(1);
    expect(mocks.fetchNotice).toHaveBeenCalledTimes(1);
    fireEvent.click(screen.getByRole('button', { name: '다시 시도' }));
    expect(await screen.findByRole('heading', { name: 'Notice 9' })).toBeVisible();
    expect(mocks.fetchNotice).toHaveBeenCalledTimes(2);
    expect(mocks.updateNotice).toHaveBeenCalledTimes(1);
    expect(mocks.deleteNotice).not.toHaveBeenCalled();
    expect(mocks.triggerBlobDownload).not.toHaveBeenCalled();
  });
});

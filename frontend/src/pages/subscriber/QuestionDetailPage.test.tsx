import { act, fireEvent, render, screen, waitFor } from '@testing-library/react';
import { createMemoryRouter, RouterProvider, useNavigate } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import QuestionDetailPage from '@/pages/subscriber/QuestionDetailPage';
import type { QuestionDetail } from '@/api/questions';

const mocks = vi.hoisted(() => ({
  fetchQuestionDetail: vi.fn(),
  deleteQuestion: vi.fn(),
  downloadAttachment: vi.fn(),
  triggerBlobDownload: vi.fn(),
  createAnswer: vi.fn(),
}));

const authState = vi.hoisted(() => ({
  user: { id: 1 },
  role: 'USER' as 'USER' | 'ADMIN',
  accessToken: 'owner-token',
}));

vi.mock('@/api/questions', () => mocks);
vi.mock('@/api/downloads', () => ({
  createDownloadFallbackFileName: (_resource: string, id: number, name: string) =>
    `question-${id}-${name}`,
  triggerBlobDownload: mocks.triggerBlobDownload,
}));
vi.mock('@/store/authStore', () => ({
  useAuthStore: (selector: (state: typeof authState) => unknown) => selector(authState),
}));

function deferred<T>() {
  let resolve!: (value: T) => void;
  let reject!: (reason?: unknown) => void;
  const promise = new Promise<T>((nextResolve, nextReject) => {
    resolve = nextResolve;
    reject = nextReject;
  });
  return { promise, resolve, reject };
}

function RouteHarness() {
  const navigate = useNavigate();
  return (
    <>
      <button type="button" onClick={() => navigate('/questions/2')}>
        next question
      </button>
      <QuestionDetailPage />
    </>
  );
}

function renderPage(initialEntry: string) {
  const router = createMemoryRouter(
    [
      { path: '/questions/:questionId', element: <RouteHarness /> },
      { path: '/questions', element: <div>Question list</div> },
    ],
    { initialEntries: [initialEntry] },
  );
  const view = render(<RouterProvider router={router} />);
  return {
    ...view,
    router,
  };
}

function detail(
  id: number,
  title: string,
  overrides: Partial<QuestionDetail> = {},
): QuestionDetail {
  return {
    id,
    title,
    content: `${title} content`,
    category: 'OTHER',
    isPublic: true,
    status: 'OPEN',
    user: null,
    attachments: null,
    answers: null,
    createdAt: '2026-08-13T00:00:00Z',
    ...overrides,
  };
}

describe('QuestionDetailPage load ownership', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    authState.user = { id: 1 };
    authState.role = 'USER';
    authState.accessToken = 'owner-token';
  });

  it.each(['1e3', '0x10', '+7', ' 7', '7 ', '7.5', '0', '-1', '9007199254740992', 'abc'])(
    'rejects noncanonical question id %s without a request',
    (id) => {
      renderPage(`/questions/${id}`);

      expect(screen.getByText('문의 주소가 올바르지 않습니다.')).toBeInTheDocument();
      expect(screen.getByRole('link', { name: '문의 목록으로' })).toHaveAttribute(
        'href',
        '/questions',
      );
      expect(mocks.fetchQuestionDetail).not.toHaveBeenCalled();
    },
  );

  it('keeps the newest question route result after a stale failure', async () => {
    const oldDetail = deferred<QuestionDetail>();
    const currentDetail = deferred<QuestionDetail>();
    mocks.fetchQuestionDetail
      .mockReturnValueOnce(oldDetail.promise)
      .mockReturnValueOnce(currentDetail.promise);

    renderPage('/questions/1');
    await waitFor(() => expect(mocks.fetchQuestionDetail).toHaveBeenCalledTimes(1));
    const oldSignal = mocks.fetchQuestionDetail.mock.calls[0][1] as AbortSignal;
    fireEvent.click(screen.getByRole('button', { name: 'next question' }));
    await waitFor(() => expect(mocks.fetchQuestionDetail).toHaveBeenCalledTimes(2));
    expect(oldSignal.aborted).toBe(true);

    await act(async () => currentDetail.resolve(detail(2, '현재 문의')));
    expect(await screen.findByText('현재 문의')).toBeInTheDocument();

    await act(async () => oldDetail.reject(new Error('old failure')));
    expect(screen.queryByText('문의를 불러오지 못했습니다.')).not.toBeInTheDocument();
    expect(screen.getByText('현재 문의')).toBeInTheDocument();
  });

  it.each(['IN_PROGRESS', 'RESOLVED', 'CLOSED'] as const)(
    'hides owner deletion while the question is %s',
    async (status) => {
      mocks.fetchQuestionDetail.mockResolvedValue(
        detail(1, `${status} 문의`, { status, user: { id: 1, nickname: 'Owner' } }),
      );

      renderPage('/questions/1');

      expect(await screen.findByText(`${status} 문의`)).toBeInTheDocument();
      expect(screen.queryByRole('button', { name: '삭제' })).not.toBeInTheDocument();
    },
  );

  it('shows deletion to a non-ADMIN owner while the question is OPEN', async () => {
    mocks.fetchQuestionDetail.mockResolvedValue(
      detail(1, '접수 문의', { status: 'OPEN', user: { id: 1, nickname: 'Owner' } }),
    );

    renderPage('/questions/1');

    expect(await screen.findByText('접수 문의')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: '삭제' })).toBeEnabled();
  });

  it('keeps the ADMIN deletion policy separate from owner OPEN gating', async () => {
    authState.role = 'ADMIN';
    mocks.fetchQuestionDetail.mockResolvedValue(
      detail(1, '관리자 문의', {
        status: 'CLOSED',
        user: { id: 99, nickname: 'Other owner' },
      }),
    );

    renderPage('/questions/1');

    expect(await screen.findByText('관리자 문의')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: '삭제' })).toBeEnabled();
  });

  it('owns one attachment request, reports failure, and permits retry', async () => {
    const firstAttempt = deferred<Blob>();
    mocks.fetchQuestionDetail.mockResolvedValue(
      detail(1, '첨부 문의', {
        attachments: [
          { id: 11, originalName: 'first.txt', fileSize: 100 },
          { id: 12, originalName: 'second.txt', fileSize: 200 },
        ],
      }),
    );
    mocks.downloadAttachment.mockReturnValueOnce(firstAttempt.promise);

    renderPage('/questions/1');
    expect(await screen.findByText('첨부 문의')).toBeInTheDocument();

    const firstButton = screen.getByRole('button', { name: 'first.txt' });
    fireEvent.click(firstButton);
    fireEvent.click(firstButton);

    expect(mocks.downloadAttachment).toHaveBeenCalledTimes(1);
    expect(mocks.downloadAttachment).toHaveBeenCalledWith(
      1,
      11,
      'question-11-first.txt',
      expect.any(AbortSignal),
    );
    expect(screen.getByRole('button', { name: /first.txt.*다운로드 중/ })).toBeDisabled();
    expect(screen.getByRole('button', { name: 'second.txt' })).toBeDisabled();

    await act(async () => firstAttempt.reject(new Error('offline')));

    expect(await screen.findByText('첨부파일 다운로드에 실패했습니다.')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'first.txt' })).toBeEnabled();

    const download = {
      blob: new Blob(['retry']),
      fileName: 'server-first.txt',
      contentType: 'application/octet-stream',
    };
    mocks.downloadAttachment.mockResolvedValueOnce(download);
    fireEvent.click(screen.getByRole('button', { name: 'first.txt' }));

    await waitFor(() => expect(mocks.triggerBlobDownload).toHaveBeenCalledWith(download));
    expect(screen.queryByText('첨부파일 다운로드에 실패했습니다.')).not.toBeInTheDocument();
  });

  it('aborts and ignores a stale attachment completion after route replacement', async () => {
    const oldDownload = deferred<Blob>();
    mocks.fetchQuestionDetail
      .mockResolvedValueOnce(
        detail(1, '이전 문의', {
          attachments: [{ id: 11, originalName: 'old.txt', fileSize: 100 }],
        }),
      )
      .mockResolvedValueOnce(detail(2, '현재 문의'));
    mocks.downloadAttachment.mockReturnValueOnce(oldDownload.promise);

    renderPage('/questions/1');
    expect(await screen.findByText('이전 문의')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: 'old.txt' }));
    const oldSignal = mocks.downloadAttachment.mock.calls[0][3] as AbortSignal;

    fireEvent.click(screen.getByRole('button', { name: 'next question' }));
    expect(await screen.findByText('현재 문의')).toBeInTheDocument();
    expect(oldSignal.aborted).toBe(true);

    await act(async () => oldDownload.resolve(new Blob(['stale'])));

    expect(mocks.triggerBlobDownload).not.toHaveBeenCalled();
    expect(screen.getByText('현재 문의')).toBeInTheDocument();
  });

  it('aborts and ignores a stale attachment completion after owner and token replacement', async () => {
    const oldDownload = deferred<Blob>();
    mocks.fetchQuestionDetail.mockResolvedValue(
      detail(1, '인증 교체 문의', {
        attachments: [{ id: 11, originalName: 'old-owner.txt', fileSize: 100 }],
      }),
    );
    mocks.downloadAttachment.mockReturnValueOnce(oldDownload.promise);

    const view = renderPage('/questions/1');
    expect(await screen.findByText('인증 교체 문의')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: 'old-owner.txt' }));
    const oldSignal = mocks.downloadAttachment.mock.calls[0][3] as AbortSignal;

    await act(async () => {
      authState.user = { id: 2 };
      authState.accessToken = 'replacement-token';
      await view.router.navigate('/questions/1?session=replaced');
    });

    await waitFor(() => expect(mocks.fetchQuestionDetail).toHaveBeenCalledTimes(2));
    expect(oldSignal.aborted).toBe(true);
    await act(async () => oldDownload.resolve(new Blob(['stale'])));

    expect(mocks.triggerBlobDownload).not.toHaveBeenCalled();
    expect(screen.queryByText('첨부파일 다운로드에 실패했습니다.')).not.toBeInTheDocument();
  });

  it('aborts and ignores a stale attachment completion after unmount', async () => {
    const oldDownload = deferred<Blob>();
    mocks.fetchQuestionDetail.mockResolvedValue(
      detail(1, '언마운트 문의', {
        attachments: [{ id: 11, originalName: 'unmount.txt', fileSize: 100 }],
      }),
    );
    mocks.downloadAttachment.mockReturnValueOnce(oldDownload.promise);

    const view = renderPage('/questions/1');
    expect(await screen.findByText('언마운트 문의')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: 'unmount.txt' }));
    const oldSignal = mocks.downloadAttachment.mock.calls[0][3] as AbortSignal;

    view.unmount();

    expect(oldSignal.aborted).toBe(true);
    await act(async () => oldDownload.resolve(new Blob(['stale'])));
    expect(mocks.triggerBlobDownload).not.toHaveBeenCalled();
  });

  it('retires an attachment request when the same detail projection refreshes', async () => {
    const oldDownload = deferred<Blob>();
    const currentDetail = detail(1, '답변 대상 문의', {
      user: { id: 1, nickname: 'Owner' },
      attachments: [{ id: 11, originalName: 'old.txt', fileSize: 100 }],
    });
    mocks.fetchQuestionDetail.mockResolvedValue(currentDetail);
    mocks.downloadAttachment.mockReturnValueOnce(oldDownload.promise);
    mocks.createAnswer.mockResolvedValue({ id: 20 });

    renderPage('/questions/1');
    expect(await screen.findByText('답변 대상 문의')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: 'old.txt' }));
    const oldSignal = mocks.downloadAttachment.mock.calls[0][3] as AbortSignal;

    fireEvent.change(screen.getByPlaceholderText('답변 내용을 입력하세요'), {
      target: { value: '새 답변' },
    });
    fireEvent.click(screen.getByRole('button', { name: '답변 등록' }));

    await waitFor(() => expect(mocks.fetchQuestionDetail).toHaveBeenCalledTimes(2));
    expect(oldSignal.aborted).toBe(true);
    await act(async () => oldDownload.resolve(new Blob(['stale'])));

    expect(mocks.triggerBlobDownload).not.toHaveBeenCalled();
    expect(screen.getByRole('button', { name: 'old.txt' })).toBeEnabled();
  });
});

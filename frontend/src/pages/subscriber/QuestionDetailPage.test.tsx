import { act, fireEvent, render, screen, waitFor } from '@testing-library/react';
import { createMemoryRouter, RouterProvider, useNavigate } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import QuestionDetailPage from '@/pages/subscriber/QuestionDetailPage';
import type { QuestionDetail } from '@/api/questions';

const mocks = vi.hoisted(() => ({
  fetchQuestionDetail: vi.fn(),
  deleteQuestion: vi.fn(),
  downloadAttachment: vi.fn(),
  createAnswer: vi.fn(),
}));

vi.mock('@/api/questions', () => mocks);
vi.mock('@/store/authStore', () => ({
  useAuthStore: (
    selector: (state: { user: { id: number }; role: 'USER'; accessToken: string }) => unknown,
  ) => selector({ user: { id: 1 }, role: 'USER', accessToken: 'owner-token' }),
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
  render(<RouterProvider router={router} future={{ v7_startTransition: true }} />);
}

function detail(id: number, title: string): QuestionDetail {
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
  };
}

describe('QuestionDetailPage load ownership', () => {
  beforeEach(() => {
    vi.clearAllMocks();
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
});

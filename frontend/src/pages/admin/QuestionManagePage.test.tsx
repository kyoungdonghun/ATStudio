import { act, fireEvent, render, screen, waitFor } from '@testing-library/react';
import { createMemoryRouter, RouterProvider } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import type { QuestionListItem } from '@/api/questions';
import type { PagedResponse } from '@/types';
import QuestionManagePage from './QuestionManagePage';

const mocks = vi.hoisted(() => ({
  fetchQuestions: vi.fn(),
  updateQuestionStatus: vi.fn(),
}));

vi.mock('@/api/questions', () => ({
  fetchQuestions: (...args: unknown[]) => mocks.fetchQuestions(...args),
  updateQuestionStatus: (...args: unknown[]) => mocks.updateQuestionStatus(...args),
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

function questionPage(id: number, title: string, status: QuestionListItem['status']) {
  return {
    dataList: [
      {
        id,
        title,
        category: 'OTHER',
        isPublic: true,
        status,
        createdAt: '2026-08-01T00:00:00Z',
      },
    ],
    pageInfo: {
      page: 1,
      size: 20,
      total: 1,
      start: 1,
      end: 1,
      prev: false,
      next: false,
    },
  } satisfies PagedResponse<QuestionListItem>;
}

describe('QuestionManagePage request ownership', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('keeps the newest filter result when an older request resolves last', async () => {
    const openRequest = deferred<PagedResponse<QuestionListItem>>();
    const closedRequest = deferred<PagedResponse<QuestionListItem>>();
    mocks.fetchQuestions
      .mockReturnValueOnce(openRequest.promise)
      .mockReturnValueOnce(closedRequest.promise);
    const router = createMemoryRouter(
      [{ path: '/admin/questions', element: <QuestionManagePage /> }],
      { initialEntries: ['/admin/questions?status=OPEN'] },
    );
    render(<RouterProvider router={router} future={{ v7_startTransition: true }} />);

    await waitFor(() => expect(mocks.fetchQuestions).toHaveBeenCalledTimes(1));
    const oldSignal = mocks.fetchQuestions.mock.calls[0][1] as AbortSignal;
    await act(async () => {
      await router.navigate('/admin/questions?status=CLOSED');
    });
    await waitFor(() => expect(mocks.fetchQuestions).toHaveBeenCalledTimes(2));
    expect(oldSignal.aborted).toBe(true);

    await act(async () =>
      closedRequest.resolve(questionPage(2, 'Newest closed question', 'CLOSED')),
    );
    expect(await screen.findByText('Newest closed question')).toBeInTheDocument();

    await act(async () => openRequest.resolve(questionPage(1, 'Stale open question', 'OPEN')));
    expect(screen.getByText('Newest closed question')).toBeInTheDocument();
    expect(screen.queryByText('Stale open question')).not.toBeInTheDocument();
  });

  it('uses a native button title command that navigates once on click', async () => {
    mocks.fetchQuestions.mockResolvedValue(questionPage(4, 'Keyboard admin question', 'OPEN'));
    const router = createMemoryRouter(
      [
        { path: '/admin/questions', element: <QuestionManagePage /> },
        { path: '/questions/:questionId', element: <div>Question detail</div> },
      ],
      { initialEntries: ['/admin/questions'] },
    );
    render(<RouterProvider router={router} future={{ v7_startTransition: true }} />);

    const titleButton = await screen.findByRole('button', { name: 'Keyboard admin question' });
    expect(titleButton).toHaveAttribute('type', 'button');
    fireEvent.click(titleButton);
    expect(router.state.location.pathname).toBe('/questions/4');
  });
});

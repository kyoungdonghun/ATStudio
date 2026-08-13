import { act, fireEvent, render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import QuestionListPage from '@/pages/subscriber/QuestionListPage';
import type { PageInfo } from '@/types';
import type { User } from '@/types';
import { useAuthStore } from '@/store/authStore';

const fetchQuestions = vi.hoisted(() => vi.fn());

vi.mock('@/api/questions', () => ({ fetchQuestions }));

function deferred<T>() {
  let resolve!: (value: T) => void;
  let reject!: (reason?: unknown) => void;
  const promise = new Promise<T>((nextResolve, nextReject) => {
    resolve = nextResolve;
    reject = nextReject;
  });
  return { promise, resolve, reject };
}

const pageInfo: PageInfo = {
  page: 1,
  size: 20,
  total: 1,
  start: 1,
  end: 1,
  prev: false,
  next: false,
};

function renderPage() {
  return render(
    <MemoryRouter
      initialEntries={['/questions']}
      future={{ v7_startTransition: true, v7_relativeSplatPath: true }}
    >
      <QuestionListPage />
    </MemoryRouter>,
  );
}

describe('QuestionListPage load ownership', () => {
  beforeEach(() => {
    fetchQuestions.mockReset();
    useAuthStore.setState({
      user: { id: 1 } as User,
      accessToken: 'owner-token',
      role: 'USER',
    });
  });

  it('keeps the newest filtered result after stale success and failure completions', async () => {
    const initial = deferred<{ dataList: []; pageInfo: PageInfo }>();
    const category = deferred<{ dataList: []; pageInfo: PageInfo }>();
    const current = deferred<{
      dataList: Array<{
        id: number;
        title: string;
        category: 'PAYMENT';
        isPublic: boolean;
        status: 'OPEN';
        createdAt: string;
      }>;
      pageInfo: PageInfo;
    }>();
    fetchQuestions
      .mockReturnValueOnce(initial.promise)
      .mockReturnValueOnce(category.promise)
      .mockReturnValueOnce(current.promise);

    renderPage();
    await waitFor(() => expect(fetchQuestions).toHaveBeenCalledTimes(1));
    const initialSignal = fetchQuestions.mock.calls[0][1] as AbortSignal;

    const [categorySelect, statusSelect] = screen.getAllByRole('combobox');
    fireEvent.change(categorySelect, { target: { value: 'PAYMENT' } });
    await waitFor(() => expect(fetchQuestions).toHaveBeenCalledTimes(2));
    fireEvent.change(statusSelect, { target: { value: 'OPEN' } });
    await waitFor(() => expect(fetchQuestions).toHaveBeenCalledTimes(3));
    expect(initialSignal.aborted).toBe(true);

    await act(async () =>
      current.resolve({
        dataList: [
          {
            id: 3,
            title: '현재 문의',
            category: 'PAYMENT',
            isPublic: true,
            status: 'OPEN',
            createdAt: '2026-08-13T00:00:00Z',
          },
        ],
        pageInfo,
      }),
    );
    expect(await screen.findByText('현재 문의')).toBeInTheDocument();

    await act(async () => initial.resolve({ dataList: [], pageInfo: { ...pageInfo, total: 0 } }));
    await act(async () => category.reject(new Error('old filter failure')));
    expect(screen.getByText('현재 문의')).toBeInTheDocument();
    expect(screen.queryByText('등록된 문의가 없습니다.')).not.toBeInTheDocument();
    expect(screen.queryByText('문의 목록을 불러오지 못했습니다.')).not.toBeInTheDocument();
  });
});

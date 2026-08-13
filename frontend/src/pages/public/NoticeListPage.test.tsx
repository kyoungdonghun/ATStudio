import { act, fireEvent, render, screen } from '@testing-library/react';
import { MemoryRouter, useNavigate } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import NoticeListPage from '@/pages/public/NoticeListPage';

const fetchNotices = vi.fn();

vi.mock('@/api/notices', () => ({
  fetchNotices: (...args: unknown[]) => fetchNotices(...args),
}));

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

function renderPage() {
  return render(
    <MemoryRouter future={{ v7_startTransition: true, v7_relativeSplatPath: true }}>
      <NoticeListPage />
    </MemoryRouter>,
  );
}

function NavigationHarness() {
  const navigate = useNavigate();
  return (
    <>
      <button type="button" onClick={() => navigate('/notices?page=2')}>
        2페이지로 이동
      </button>
      <NoticeListPage />
    </>
  );
}

describe('NoticeListPage load states', () => {
  beforeEach(() => {
    fetchNotices.mockReset();
  });

  it('retries one network failure and renders the legitimate empty state', async () => {
    fetchNotices.mockRejectedValueOnce({ code: 'ETIMEDOUT' }).mockResolvedValueOnce(emptyPage);

    renderPage();

    expect(await screen.findByRole('alert')).toHaveTextContent('네트워크 연결');
    const retryButton = screen.getByRole('button', { name: '다시 시도' });
    fireEvent.click(retryButton);
    fireEvent.click(retryButton);

    expect(retryButton).toBeDisabled();
    expect(fetchNotices).toHaveBeenCalledTimes(2);
    expect(await screen.findByText('등록된 공지사항이 없습니다.')).toBeInTheDocument();
    expect(screen.queryByRole('alert')).not.toBeInTheDocument();
  });

  it('ignores a superseded request failure after a newer page succeeds', async () => {
    let rejectFirst!: (reason: unknown) => void;
    const firstRequest = new Promise<never>((_, reject) => {
      rejectFirst = reject;
    });
    fetchNotices.mockReturnValueOnce(firstRequest).mockResolvedValueOnce({
      dataList: [
        {
          id: 2,
          title: '두 번째 페이지 공지',
          content: '내용',
          isPinned: false,
          viewCount: 0,
          createdAt: '2026-07-16T09:00:00',
          updatedAt: '2026-07-16T09:00:00',
        },
      ],
      pageInfo: { ...emptyPage.pageInfo, page: 2, total: 21, start: 21, end: 21 },
    });

    render(
      <MemoryRouter
        initialEntries={['/notices?page=1']}
        future={{ v7_startTransition: true, v7_relativeSplatPath: true }}
      >
        <NavigationHarness />
      </MemoryRouter>,
    );

    fireEvent.click(screen.getByRole('button', { name: '2페이지로 이동' }));
    expect(await screen.findByText('두 번째 페이지 공지')).toBeInTheDocument();

    await act(async () => {
      rejectFirst({ response: { status: 500 } });
    });

    expect(screen.getByText('두 번째 페이지 공지')).toBeInTheDocument();
    expect(screen.queryByRole('alert')).not.toBeInTheDocument();
  });
});

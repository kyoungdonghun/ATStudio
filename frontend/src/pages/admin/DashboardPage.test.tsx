import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import DashboardPage from '@/pages/admin/DashboardPage';

const fetchDashboardStats = vi.fn();

vi.mock('@/api/admin', () => ({
  fetchDashboardStats: (...args: unknown[]) => fetchDashboardStats(...args),
}));

const stats = {
  totalUsers: 12,
  totalTracks: 34,
  totalSubscribers: 5,
  recentUsers: [
    {
      id: 1,
      email: 'member@example.com',
      nickname: '테스트 회원',
      role: 'USER',
      createdAt: '2026-07-16T09:00:00',
    },
  ],
};

describe('DashboardPage load states', () => {
  beforeEach(() => {
    fetchDashboardStats.mockReset();
  });

  it('retries one failed load and renders data after success', async () => {
    fetchDashboardStats
      .mockRejectedValueOnce({ response: { status: 500 } })
      .mockResolvedValueOnce(stats);

    render(<DashboardPage />);

    expect(await screen.findByRole('alert')).toHaveTextContent('서버 오류');
    const retryButton = screen.getByRole('button', { name: '다시 시도' });
    fireEvent.click(retryButton);
    fireEvent.click(retryButton);

    expect(retryButton).toBeDisabled();
    expect(fetchDashboardStats).toHaveBeenCalledTimes(2);
    await waitFor(() => {
      expect(screen.getByText('member@example.com')).toBeInTheDocument();
    });
    expect(screen.queryByRole('alert')).not.toBeInTheDocument();
  });

  it('renders a legitimate empty recent-user result without an error', async () => {
    fetchDashboardStats.mockResolvedValue({ ...stats, recentUsers: [] });

    render(<DashboardPage />);

    expect(await screen.findByText('No users found.')).toBeInTheDocument();
    expect(screen.queryByRole('alert')).not.toBeInTheDocument();
  });
});

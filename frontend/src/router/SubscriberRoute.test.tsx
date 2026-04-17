import { render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import SubscriberRoute from '@/router/SubscriberRoute';

const authState = {
  isAuthenticated: () => false,
};

const showToast = vi.fn();
const fetchMySubscription = vi.fn();

vi.mock('@/store/authStore', () => ({
  useAuthStore: (selector: (state: typeof authState) => unknown) => selector(authState),
}));

vi.mock('@/store/toastStore', () => ({
  useToastStore: {
    getState: () => ({ show: showToast }),
  },
}));

vi.mock('@/api/userSubscriptions', () => ({
  fetchMySubscription: (...args: unknown[]) => fetchMySubscription(...args),
}));

function renderSubscriber() {
  return render(
    <MemoryRouter initialEntries={['/subscriber']}>
      <Routes>
        <Route
          path="/subscriber"
          element={
            <SubscriberRoute>
              <div>Subscriber Page</div>
            </SubscriberRoute>
          }
        />
        <Route path="/login" element={<div>Login Page</div>} />
        <Route path="/subscriptions" element={<div>Subscriptions Page</div>} />
      </Routes>
    </MemoryRouter>,
  );
}

describe('SubscriberRoute', () => {
  beforeEach(() => {
    authState.isAuthenticated = () => false;
    showToast.mockReset();
    fetchMySubscription.mockReset();
  });

  it('redirects unauthenticated users to login and shows a warning toast', async () => {
    renderSubscriber();

    expect(screen.getByText('Login Page')).toBeInTheDocument();
    await waitFor(() => {
      expect(showToast).toHaveBeenCalledWith('warning', '로그인이 필요한 기능입니다.');
    });
    expect(fetchMySubscription).not.toHaveBeenCalled();
  });

  it('renders children when the user has an active subscription', async () => {
    authState.isAuthenticated = () => true;
    fetchMySubscription.mockResolvedValue({ id: 1 });

    renderSubscriber();

    expect(screen.getByText('구독 상태 확인 중...')).toBeInTheDocument();
    await waitFor(() => {
      expect(screen.getByText('Subscriber Page')).toBeInTheDocument();
    });
    expect(showToast).not.toHaveBeenCalled();
  });

  it('redirects inactive users to subscriptions and shows a warning toast', async () => {
    authState.isAuthenticated = () => true;
    fetchMySubscription.mockRejectedValue(new Error('inactive'));

    renderSubscriber();

    await waitFor(() => {
      expect(screen.getByText('Subscriptions Page')).toBeInTheDocument();
    });
    expect(showToast).toHaveBeenCalledWith('warning', '구독이 필요한 기능입니다.');
  });
});

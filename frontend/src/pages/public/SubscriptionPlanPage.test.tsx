import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import SubscriptionPlanPage from '@/pages/public/SubscriptionPlanPage';

const navigateMock = vi.hoisted(() => vi.fn());
const toastShowMock = vi.hoisted(() => vi.fn());
const fetchSubscriptionPlansMock = vi.hoisted(() => vi.fn());
const fetchMySubscriptionMock = vi.hoisted(() => vi.fn());

const authState = {
  user: {
    userType: 'INDIVIDUAL',
  },
  isAuthenticated: () => true,
};

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual<typeof import('react-router-dom')>('react-router-dom');
  return {
    ...actual,
    useNavigate: () => navigateMock,
  };
});

vi.mock('@/store/authStore', () => ({
  useAuthStore: (selector: (state: typeof authState) => unknown) => selector(authState),
}));

vi.mock('@/store/toastStore', () => ({
  useToastStore: (selector: (state: { show: typeof toastShowMock }) => unknown) =>
    selector({ show: toastShowMock }),
}));

vi.mock('@/api/subscriptions', () => ({
  fetchSubscriptionPlans: (...args: unknown[]) => fetchSubscriptionPlansMock(...args),
}));

vi.mock('@/api/userSubscriptions', () => ({
  fetchMySubscription: (...args: unknown[]) => fetchMySubscriptionMock(...args),
}));

function renderPage() {
  return render(
    <MemoryRouter>
      <SubscriptionPlanPage />
    </MemoryRouter>,
  );
}

describe('SubscriptionPlanPage', () => {
  beforeEach(() => {
    navigateMock.mockReset();
    toastShowMock.mockReset();
    fetchSubscriptionPlansMock.mockReset();
    fetchMySubscriptionMock.mockReset();

    fetchSubscriptionPlansMock.mockResolvedValue([
      {
        id: 1,
        name: 'STANDARD',
        description: 'Starter',
        userType: 'INDIVIDUAL',
        priceMonthly: 9900,
        priceYearly: 99000,
        downloadPerDay: 5,
        maxWhitelistChannels: 1,
        maxPlaylists: 3,
        isActive: true,
      },
      {
        id: 2,
        name: 'DELUXE',
        description: 'Creator',
        userType: 'INDIVIDUAL',
        priceMonthly: 19900,
        priceYearly: 199000,
        downloadPerDay: 20,
        maxWhitelistChannels: 5,
        maxPlaylists: 10,
        isActive: true,
      },
    ]);
    fetchMySubscriptionMock.mockRejectedValue(new Error('No active subscription'));
  });

  it('starts new subscriptions through Toss recurring billing by default', async () => {
    renderPage();

    fireEvent.click(await screen.findByRole('button', { name: '지금 시작하기' }));

    await waitFor(() => {
      expect(navigateMock).toHaveBeenCalledWith(
        '/subscriptions/checkout?planId=2&userType=INDIVIDUAL&billingCycle=YEARLY&purpose=SUBSCRIBE',
      );
    });
  });
});

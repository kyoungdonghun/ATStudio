import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import SubscriptionManagePage from '@/pages/subscriber/SubscriptionManagePage';

const authState = {
  role: 'USER',
  user: { userType: 'INDIVIDUAL' as const },
};

const fetchMySubscriptionMock = vi.fn();
const fetchSubscriptionPlansMock = vi.fn();
const fetchSubscriptionChangePreviewMock = vi.fn();
const changeMySubscriptionMock = vi.fn();
const cancelMySubscriptionMock = vi.fn();
const prepareSubscriptionPaymentMock = vi.fn();
const confirmPaymentMock = vi.fn();

vi.mock('@/store/authStore', () => ({
  useAuthStore: Object.assign(
    (selector: (state: typeof authState) => unknown) => selector(authState),
    {
      getState: () => authState,
    },
  ),
}));

vi.mock('@/api/userSubscriptions', () => ({
  fetchMySubscription: (...args: unknown[]) => fetchMySubscriptionMock(...args),
  fetchSubscriptionChangePreview: (...args: unknown[]) =>
    fetchSubscriptionChangePreviewMock(...args),
  changeMySubscription: (...args: unknown[]) => changeMySubscriptionMock(...args),
  cancelMySubscription: (...args: unknown[]) => cancelMySubscriptionMock(...args),
}));

vi.mock('@/api/subscriptions', () => ({
  fetchSubscriptionPlans: (...args: unknown[]) => fetchSubscriptionPlansMock(...args),
}));

vi.mock('@/api/payments', () => ({
  prepareSubscriptionPayment: (...args: unknown[]) => prepareSubscriptionPaymentMock(...args),
  confirmPayment: (...args: unknown[]) => confirmPaymentMock(...args),
}));

vi.mock('@/api/client', () => ({
  isSubscriptionRequired: (err: unknown) =>
    (err as { response?: { data?: { errorCode?: string } } })?.response?.data?.errorCode ===
    'NO_ACTIVE_SUBSCRIPTION',
}));

function renderPage() {
  return render(
    <MemoryRouter initialEntries={['/subscriptions/manage']}>
      <SubscriptionManagePage />
    </MemoryRouter>,
  );
}

describe('SubscriptionManagePage', () => {
  beforeEach(() => {
    authState.role = 'USER';
    authState.user = { userType: 'INDIVIDUAL' };
    fetchMySubscriptionMock.mockReset();
    fetchSubscriptionPlansMock.mockReset();
    fetchSubscriptionChangePreviewMock.mockReset();
    changeMySubscriptionMock.mockReset();
    cancelMySubscriptionMock.mockReset();
    prepareSubscriptionPaymentMock.mockReset();
    confirmPaymentMock.mockReset();

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
    ]);
  });

  it('shows the no-subscription CTA when the API returns NO_ACTIVE_SUBSCRIPTION', async () => {
    fetchMySubscriptionMock.mockRejectedValue({
      response: {
        data: {
          errorCode: 'NO_ACTIVE_SUBSCRIPTION',
        },
      },
    });

    renderPage();

    await waitFor(() => {
      expect(screen.getByText('현재 활성 구독이 없습니다.')).toBeInTheDocument();
    });
    expect(screen.getByRole('link', { name: '구독 플랜 보기' })).toHaveAttribute(
      'href',
      '/subscriptions',
    );
  });

  it('keeps real API failures in the error state', async () => {
    fetchMySubscriptionMock.mockRejectedValue(new Error('subscription service unavailable'));

    renderPage();

    await waitFor(() => {
      expect(screen.getByText('subscription service unavailable')).toBeInTheDocument();
    });
  });

  it('uses payment confirm flow for upgrades instead of direct subscription change', async () => {
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
        description: 'More',
        userType: 'INDIVIDUAL',
        priceMonthly: 19900,
        priceYearly: 199000,
        downloadPerDay: 20,
        maxWhitelistChannels: 5,
        maxPlaylists: 10,
        isActive: true,
      },
    ]);
    fetchMySubscriptionMock.mockResolvedValue({
      id: 100,
      subscription: {
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
      billingCycle: 'MONTHLY',
      status: 'ACTIVE',
      startedAt: '2026-05-01',
      expiresAt: '2026-06-01',
      pendingSubscriptionId: null,
      pendingBillingCycle: null,
    });
    fetchSubscriptionChangePreviewMock.mockResolvedValue({
      changeType: 'UPGRADE',
      proratedAmount: 14950,
      effectiveDate: '2026-05-16',
      newPlanName: 'DELUXE',
      newBillingCycle: 'MONTHLY',
    });
    prepareSubscriptionPaymentMock.mockResolvedValue({
      orderId: 'ATS-UPGRADE-1',
      provider: 'MOCK',
      purpose: 'UPGRADE',
      amount: 14950,
      currency: 'KRW',
      expiresAt: '2026-05-16T23:10:00',
      checkout: {
        type: 'MOCK',
        confirmToken: 'mock-ATS-UPGRADE-1',
      },
    });
    confirmPaymentMock.mockResolvedValue({
      orderId: 'ATS-UPGRADE-1',
      status: 'DONE',
      purpose: 'UPGRADE',
      subscription: {
        id: 100,
        subscription: {
          id: 2,
          name: 'DELUXE',
        },
      },
    });

    renderPage();

    fireEvent.click(await screen.findByText('디럭스'));
    await screen.findByText('업그레이드');
    fireEvent.click(screen.getByRole('button', { name: '플랜 변경 확인' }));

    await waitFor(() => {
      expect(prepareSubscriptionPaymentMock).toHaveBeenCalledWith({
        purpose: 'UPGRADE',
        subscriptionId: 2,
        billingCycle: 'MONTHLY',
      });
    });
    expect(confirmPaymentMock).toHaveBeenCalledWith({
      orderId: 'ATS-UPGRADE-1',
      amount: 14950,
      provider: 'MOCK',
      providerToken: 'mock-ATS-UPGRADE-1',
    });
    expect(changeMySubscriptionMock).not.toHaveBeenCalled();
  });
});

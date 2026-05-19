import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import SubscriptionManagePage from '@/pages/subscriber/SubscriptionManagePage';

const navigateMock = vi.hoisted(() => vi.fn());

const authState = {
  role: 'USER',
  user: { userType: 'INDIVIDUAL' as const },
};

const fetchMySubscriptionMock = vi.fn();
const fetchSubscriptionPlansMock = vi.fn();
const fetchSubscriptionChangePreviewMock = vi.fn();
const changeMySubscriptionMock = vi.fn();
const cancelMySubscriptionMock = vi.fn();
const fetchMyBillingAgreementMock = vi.fn();
const cancelMyBillingAgreementMock = vi.fn();
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual<typeof import('react-router-dom')>('react-router-dom');
  return {
    ...actual,
    useNavigate: () => navigateMock,
  };
});

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
  fetchMyBillingAgreement: (...args: unknown[]) => fetchMyBillingAgreementMock(...args),
  cancelMyBillingAgreement: (...args: unknown[]) => cancelMyBillingAgreementMock(...args),
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
    fetchMyBillingAgreementMock.mockReset();
    cancelMyBillingAgreementMock.mockReset();
    navigateMock.mockReset();

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
    fetchMyBillingAgreementMock.mockRejectedValue({
      response: { data: { errorCode: 'BILLING_AGREEMENT_NOT_FOUND' } },
    });
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

  it('routes upgrades through the payment page instead of direct subscription change', async () => {
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
      proratedAmount: 5000,
      effectiveDate: '2026-05-16',
      newPlanName: 'DELUXE',
      newBillingCycle: 'MONTHLY',
    });
    changeMySubscriptionMock.mockResolvedValue({
      subscription: { id: 2, name: 'DELUXE' },
      billingCycle: 'MONTHLY',
      status: 'ACTIVE',
      changeType: 'UPGRADE',
      proratedAmount: 5000,
      startedAt: '2026-05-01',
      expiresAt: '2026-06-01',
    });
    renderPage();

    fireEvent.click(await screen.findByText('디럭스'));
    await screen.findByText('업그레이드');
    fireEvent.click(screen.getByRole('button', { name: '플랜 변경 확인' }));

    await waitFor(() => {
      expect(changeMySubscriptionMock).toHaveBeenCalledWith({
        subscriptionId: 2,
        billingCycle: 'MONTHLY',
      });
    });
    expect(navigateMock).not.toHaveBeenCalled();
  });

  it('shows billing agreement state and cancels automatic renewal', async () => {
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
    fetchMyBillingAgreementMock.mockResolvedValue({
      provider: 'TOSS_BILLING',
      status: 'ACTIVE',
      customerKey: 'ats_billing_customer_1',
      payMethod: 'CARD',
      maskedMethod: '1234',
      nextBillingAt: '2026-06-01',
      lastChargedAt: '2026-05-01T00:00:00',
      cancelledAt: null,
      subscription: null,
    });
    cancelMyBillingAgreementMock.mockResolvedValue({
      provider: 'TOSS_BILLING',
      status: 'CANCELLED',
      customerKey: 'ats_billing_customer_1',
      payMethod: 'CARD',
      maskedMethod: '1234',
      nextBillingAt: '2026-06-01',
      lastChargedAt: '2026-05-01T00:00:00',
      cancelledAt: '2026-05-16T00:00:00',
      subscription: null,
    });

    renderPage();

    await screen.findByText('자동 갱신 중');
    fireEvent.click(screen.getByRole('button', { name: '자동 갱신 해지' }));

    await waitFor(() => {
      expect(cancelMyBillingAgreementMock).toHaveBeenCalled();
    });
  });
});

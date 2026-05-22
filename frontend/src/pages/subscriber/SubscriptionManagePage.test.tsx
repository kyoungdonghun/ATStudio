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
const reactivateMySubscriptionMock = vi.fn();
const fetchMyBillingAgreementMock = vi.fn();
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
  reactivateMySubscription: (...args: unknown[]) => reactivateMySubscriptionMock(...args),
}));

vi.mock('@/api/subscriptions', () => ({
  fetchSubscriptionPlans: (...args: unknown[]) => fetchSubscriptionPlansMock(...args),
}));

vi.mock('@/api/payments', () => ({
  fetchMyBillingAgreement: (...args: unknown[]) => fetchMyBillingAgreementMock(...args),
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
    reactivateMySubscriptionMock.mockReset();
    fetchMyBillingAgreementMock.mockReset();
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

  it('changes upgrades through the subscription API after preview confirmation', async () => {
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
      nextBillingDate: '2026-06-01',
      nextBillingAmount: 19900,
      newPlanName: 'DELUXE',
      newBillingCycle: 'MONTHLY',
    });
    fetchMyBillingAgreementMock.mockResolvedValue({
      provider: 'TOSS_BILLING',
      status: 'ACTIVE',
      payMethod: 'CARD',
      maskedMethod: '1234',
      nextBillingAt: '2026-06-01',
      lastChargedAt: '2026-05-01T00:00:00',
      cancelledAt: null,
      subscription: null,
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
    await screen.findByText('다음 결제 금액');
    await screen.findByText('₩19,900');
    fireEvent.click(screen.getByRole('button', { name: '플랜 변경 확인' }));

    await waitFor(() => {
      expect(changeMySubscriptionMock).toHaveBeenCalledWith({
        subscriptionId: 2,
        billingCycle: 'MONTHLY',
      });
    });
    expect(navigateMock).not.toHaveBeenCalled();
  });

  it('keeps plan changes available while a next-renewal change is pending', async () => {
    fetchSubscriptionPlansMock.mockResolvedValue([
      {
        id: 2,
        name: 'PREMIUM',
        description: 'Premium',
        userType: 'INDIVIDUAL',
        priceMonthly: 29900,
        priceYearly: 299000,
        downloadPerDay: 50,
        maxWhitelistChannels: 10,
        maxPlaylists: 20,
        isActive: true,
      },
      {
        id: 3,
        name: 'DELUXE',
        description: 'Deluxe',
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
        id: 2,
        name: 'PREMIUM',
        description: 'Premium',
        userType: 'INDIVIDUAL',
        priceMonthly: 29900,
        priceYearly: 299000,
        downloadPerDay: 50,
        maxWhitelistChannels: 10,
        maxPlaylists: 20,
        isActive: true,
      },
      billingCycle: 'YEARLY',
      status: 'ACTIVE',
      startedAt: '2026-05-20',
      expiresAt: '2027-05-20',
      pendingSubscriptionId: 2,
      pendingBillingCycle: 'MONTHLY',
    });

    renderPage();

    await screen.findByText('다음 결제일부터 프리미엄 (월간)이 적용됩니다.');
    expect(screen.getByText('플랜 변경')).toBeInTheDocument();
    expect(screen.getByText('현재 플랜')).toBeInTheDocument();
    expect(screen.getByText('디럭스')).toBeInTheDocument();
  });

  it('shows billing agreement state without a separate automatic-renewal cancel action', async () => {
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
      payMethod: 'CARD',
      maskedMethod: '1234',
      nextBillingAt: '2026-06-01',
      lastChargedAt: '2026-05-01T00:00:00',
      cancelledAt: null,
      subscription: null,
    });

    renderPage();

    await screen.findByText('자동 갱신 중');
    expect(screen.getByText('결제 정보')).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: '자동 갱신 해지' })).not.toBeInTheDocument();
  });

  it('lets a cancelled grace-period subscription resume before expiry', async () => {
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
      status: 'CANCELLED',
      startedAt: '2026-05-01',
      expiresAt: '2026-06-01',
      pendingSubscriptionId: null,
      pendingBillingCycle: null,
    });
    fetchMyBillingAgreementMock.mockResolvedValue({
      provider: 'TOSS_BILLING',
      status: 'CANCELLED',
      payMethod: 'CARD',
      maskedMethod: '1234',
      nextBillingAt: '2026-06-01',
      lastChargedAt: '2026-05-01T00:00:00',
      cancelledAt: '2026-05-16T00:00:00',
      subscription: null,
    });
    reactivateMySubscriptionMock.mockResolvedValue({
      id: 100,
      status: 'ACTIVE',
    });

    renderPage();

    fireEvent.click(await screen.findByRole('button', { name: '구독 유지하기' }));

    await waitFor(() => {
      expect(reactivateMySubscriptionMock).toHaveBeenCalled();
    });
  });
});

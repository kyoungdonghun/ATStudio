import { act, fireEvent, render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import SubscriptionManagePage from '@/pages/subscriber/SubscriptionManagePage';

const navigateMock = vi.hoisted(() => vi.fn());

const authState: {
  role: string;
  user: { userType: 'INDIVIDUAL' | 'BUSINESS' };
} = {
  role: 'USER',
  user: { userType: 'INDIVIDUAL' },
};

const fetchMySubscriptionMock = vi.fn();
const fetchSubscriptionPlansMock = vi.fn();
const fetchSubscriptionChangePreviewMock = vi.fn();
const changeMySubscriptionMock = vi.fn();
const cancelMySubscriptionMock = vi.fn();
const reactivateMySubscriptionMock = vi.fn();
const fetchMyBillingAgreementMock = vi.fn();
const fetchSubscriptionUpgradeOutcomeMock = vi.fn();
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
  isNoActiveSubscriptionError: (error: unknown) =>
    (error as { response?: { status?: number; data?: { errorCode?: string } } })?.response
      ?.status === 403 &&
    (error as { response?: { data?: { errorCode?: string } } })?.response?.data?.errorCode ===
      'NO_ACTIVE_SUBSCRIPTION',
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
  fetchSubscriptionUpgradeOutcome: (...args: unknown[]) =>
    fetchSubscriptionUpgradeOutcomeMock(...args),
  isBillingAgreementNotFoundError: (error: unknown) =>
    (error as { response?: { status?: number; data?: { errorCode?: string } } })?.response
      ?.status === 404 &&
    (error as { response?: { data?: { errorCode?: string } } })?.response?.data?.errorCode ===
      'BILLING_AGREEMENT_NOT_FOUND',
}));

vi.mock('@/api/client', () => ({
  isSubscriptionRequired: (err: unknown) =>
    (err as { response?: { data?: { errorCode?: string } } })?.response?.data?.errorCode ===
    'NO_ACTIVE_SUBSCRIPTION',
  getApiErrorCode: (err: unknown) =>
    Promise.resolve(
      (err as { response?: { data?: { errorCode?: string } } })?.response?.data?.errorCode ?? null,
    ),
}));

function renderPage() {
  return render(managePageElement());
}

function managePageElement() {
  return (
    <MemoryRouter
      initialEntries={['/subscriptions/manage']}
      future={{ v7_startTransition: true, v7_relativeSplatPath: true }}
    >
      <SubscriptionManagePage />
    </MemoryRouter>
  );
}

function billingAgreementNotFoundError() {
  return {
    response: {
      status: 404,
      data: { errorCode: 'BILLING_AGREEMENT_NOT_FOUND' },
    },
  };
}

function deferred<T>() {
  let resolve!: (value: T) => void;
  let reject!: (reason?: unknown) => void;
  const promise = new Promise<T>((next, fail) => {
    resolve = next;
    reject = fail;
  });
  return { promise, resolve, reject };
}

async function confirmReactivation() {
  fireEvent.click(
    await screen.findByRole('button', { name: '\uAD6C\uB3C5 \uC720\uC9C0\uD558\uAE30' }),
  );
  fireEvent.click(
    await screen.findByRole('button', {
      name: '\uAD6C\uB3C5 \uC720\uC9C0 \uD655\uC815',
    }),
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
    fetchSubscriptionUpgradeOutcomeMock.mockReset();
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
    fetchMyBillingAgreementMock.mockRejectedValue(billingAgreementNotFoundError());
  });

  it('shows the no-subscription CTA when the API returns NO_ACTIVE_SUBSCRIPTION', async () => {
    fetchMySubscriptionMock.mockRejectedValue({
      response: {
        status: 403,
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
      expect(
        screen.getByText(
          '\uAD6C\uB3C5 \uC815\uBCF4\uB97C \uBD88\uB7EC\uC624\uC9C0 \uBABB\uD588\uC2B5\uB2C8\uB2E4. \uB2E4\uC2DC \uC2DC\uB3C4\uD574\uC8FC\uC138\uC694.',
        ),
      ).toBeInTheDocument();
    });
    expect(screen.queryByText('subscription service unavailable')).not.toBeInTheDocument();
    expect(screen.getByRole('button', { name: '\uB2E4\uC2DC \uC2DC\uB3C4' })).toBeEnabled();
  });

  it('renders only the documented billing-agreement 404 as absence', async () => {
    fetchMySubscriptionMock.mockResolvedValue(subscriptionState(1, 'ACTIVE'));

    renderPage();

    expect(
      await screen.findByText(
        '\uD604\uC7AC \uB4F1\uB85D\uB41C \uC815\uAE30\uACB0\uC81C \uC218\uB2E8\uC774 \uC5C6\uC2B5\uB2C8\uB2E4.',
      ),
    ).toBeInTheDocument();
    expect(
      screen.getByRole('button', {
        name: '\uACB0\uC81C\uC218\uB2E8 \uB2E4\uC2DC \uB4F1\uB85D',
      }),
    ).toBeEnabled();
  });

  it.each([
    ['unauthorized', { response: { status: 401, data: { errorCode: 'UNAUTHORIZED' } } }],
    ['forbidden', { response: { status: 403, data: { errorCode: 'FORBIDDEN' } } }],
    ['wrong 404', { response: { status: 404, data: { errorCode: 'RESOURCE_NOT_FOUND' } } }],
    ['server', { response: { status: 503, data: { errorCode: 'SERVER_ERROR' } } }],
    ['network', new Error('network unavailable')],
  ])('keeps a %s billing read failure visible and retryable', async (_label, error) => {
    fetchMySubscriptionMock.mockResolvedValue(subscriptionState(1, 'ACTIVE'));
    fetchMyBillingAgreementMock
      .mockRejectedValueOnce(error)
      .mockResolvedValueOnce(billingState('ACTIVE'));

    renderPage();

    expect(
      await screen.findByText(
        '\uC790\uB3D9\uACB0\uC81C \uC815\uBCF4\uB97C \uBD88\uB7EC\uC624\uC9C0 \uBABB\uD588\uC2B5\uB2C8\uB2E4. \uB2E4\uC2DC \uC2DC\uB3C4\uD574\uC8FC\uC138\uC694.',
      ),
    ).toBeInTheDocument();
    expect(
      screen.queryByRole('button', {
        name: '\uACB0\uC81C\uC218\uB2E8 \uB2E4\uC2DC \uB4F1\uB85D',
      }),
    ).not.toBeInTheDocument();

    fireEvent.click(
      screen.getByRole('button', {
        name: '\uC790\uB3D9\uACB0\uC81C \uC815\uBCF4 \uB2E4\uC2DC \uC2DC\uB3C4',
      }),
    );

    expect(await screen.findByText('\uC790\uB3D9 \uAC31\uC2E0 \uC911')).toBeInTheDocument();
    expect(fetchMyBillingAgreementMock).toHaveBeenCalledTimes(2);
  });

  it('ignores a late billing read from a retired authenticated audience', async () => {
    const staleBilling = deferred<ReturnType<typeof billingState>>();
    const businessSubscription = {
      ...subscriptionState(2, 'ACTIVE'),
      subscription: {
        ...plan(2, 'DELUXE', 19900),
        userType: 'BUSINESS' as const,
      },
    };
    fetchSubscriptionPlansMock
      .mockResolvedValueOnce([plan(1, 'STANDARD', 9900)])
      .mockResolvedValueOnce([businessSubscription.subscription]);
    fetchMySubscriptionMock
      .mockResolvedValueOnce(subscriptionState(1, 'ACTIVE'))
      .mockResolvedValueOnce(businessSubscription);
    fetchMyBillingAgreementMock
      .mockReturnValueOnce(staleBilling.promise)
      .mockRejectedValueOnce(billingAgreementNotFoundError());

    const view = renderPage();
    await waitFor(() => expect(fetchMyBillingAgreementMock).toHaveBeenCalledTimes(1));

    authState.user = { userType: 'BUSINESS' };
    view.rerender(managePageElement());

    expect(
      await screen.findByText(
        '\uD604\uC7AC \uB4F1\uB85D\uB41C \uC815\uAE30\uACB0\uC81C \uC218\uB2E8\uC774 \uC5C6\uC2B5\uB2C8\uB2E4.',
      ),
    ).toBeInTheDocument();

    await act(async () => {
      staleBilling.resolve(billingState('ACTIVE'));
    });

    expect(screen.queryByText('\uC790\uB3D9 \uAC31\uC2E0 \uC911')).not.toBeInTheDocument();
  });

  it.each(['success', 'failure'] as const)(
    'ignores a stale billing retry %s after cancellation reconciliation',
    async (retryOutcome) => {
      const staleBilling = deferred<ReturnType<typeof billingState>>();
      fetchMySubscriptionMock
        .mockResolvedValueOnce(subscriptionState(1, 'ACTIVE'))
        .mockResolvedValueOnce(subscriptionState(1, 'CANCELLED'));
      fetchMyBillingAgreementMock
        .mockRejectedValueOnce(new Error('initial read unavailable'))
        .mockReturnValueOnce(staleBilling.promise)
        .mockResolvedValueOnce(billingState('CANCELLED'));
      cancelMySubscriptionMock.mockResolvedValue({ status: 'CANCELLED' });

      renderPage();

      fireEvent.click(
        await screen.findByRole('button', {
          name: '\uC790\uB3D9\uACB0\uC81C \uC815\uBCF4 \uB2E4\uC2DC \uC2DC\uB3C4',
        }),
      );
      await waitFor(() => expect(fetchMyBillingAgreementMock).toHaveBeenCalledTimes(2));

      fireEvent.click(await screen.findByRole('button', { name: '\uAD6C\uB3C5 \uCDE8\uC18C' }));
      fireEvent.click(screen.getByRole('button', { name: '\uCDE8\uC18C \uD655\uC815' }));

      expect(
        await screen.findByText('\uB2E4\uC74C \uAC31\uC2E0 \uC911\uC9C0\uB428'),
      ).toBeInTheDocument();

      await act(async () => {
        if (retryOutcome === 'success') {
          staleBilling.resolve(billingState('ACTIVE'));
        } else {
          staleBilling.reject(new Error('stale retry failed'));
        }
      });

      expect(screen.getByText('\uB2E4\uC74C \uAC31\uC2E0 \uC911\uC9C0\uB428')).toBeInTheDocument();
      expect(
        screen.queryByText(
          '\uC790\uB3D9\uACB0\uC81C \uC815\uBCF4\uB97C \uBD88\uB7EC\uC624\uC9C0 \uBABB\uD588\uC2B5\uB2C8\uB2E4. \uB2E4\uC2DC \uC2DC\uB3C4\uD574\uC8FC\uC138\uC694.',
        ),
      ).not.toBeInTheDocument();
    },
  );

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
      provider: 'TOSS',
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
    await screen.findByText('오늘 변경');
    await screen.findByText('다음 결제 금액');
    await screen.findByText('₩19,900/월');
    fireEvent.click(screen.getByRole('button', { name: '차액 결제 후 변경' }));

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
    fetchSubscriptionChangePreviewMock.mockResolvedValue({
      changeType: 'NO_CHANGE',
      proratedAmount: 0,
      effectiveDate: '2026-05-23',
      nextBillingDate: '2027-05-20',
      nextBillingAmount: 299000,
      newPlanName: 'PREMIUM',
      newBillingCycle: 'YEARLY',
    });

    renderPage();

    await screen.findByText(
      '다음 결제일부터 결제 주기만 월간으로 전환됩니다. 플랜은 프리미엄으로 유지됩니다.',
    );
    expect(screen.getByText('플랜 변경')).toBeInTheDocument();
    expect(screen.getByText('현재 이용 중')).toBeInTheDocument();
    expect(screen.getByText('월간 예약')).toBeInTheDocument();
    expect(screen.getByText('디럭스')).toBeInTheDocument();

    fireEvent.click(screen.getByRole('button', { name: /프리미엄/ }));

    const cancelPendingCycleButton = await screen.findByRole('button', {
      name: '결제 주기 예약 취소',
    });
    expect(
      screen.getByText('예약된 월간 전환을 취소하고 현재 연간 결제를 유지합니다.'),
    ).toBeInTheDocument();
    expect(cancelPendingCycleButton).toBeEnabled();
  });

  it('does not let the current plan and current cycle behave like a new change', async () => {
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

    renderPage();

    const currentPlanButton = await screen.findByRole('button', { name: /현재 이용 중/ });
    expect(currentPlanButton).toBeDisabled();
    expect(screen.queryByText('변경 미리보기')).not.toBeInTheDocument();
    expect(fetchSubscriptionChangePreviewMock).not.toHaveBeenCalled();
  });

  it('keeps preview failure visible, retryable, and non-actionable', async () => {
    fetchSubscriptionPlansMock.mockResolvedValue([
      plan(1, 'STANDARD', 9900),
      plan(2, 'DELUXE', 19900),
    ]);
    fetchMySubscriptionMock.mockResolvedValue(subscriptionState(1, 'ACTIVE'));
    fetchMyBillingAgreementMock.mockResolvedValue(billingState('ACTIVE'));
    fetchSubscriptionChangePreviewMock
      .mockRejectedValueOnce(new Error('preview unavailable'))
      .mockResolvedValueOnce({
        changeType: 'UPGRADE',
        proratedAmount: 5000,
        effectiveDate: '2026-08-14',
        nextBillingDate: '2026-09-01',
        nextBillingAmount: 19900,
        newPlanName: 'DELUXE',
        newBillingCycle: 'MONTHLY',
      });

    renderPage();
    fireEvent.click(await screen.findByRole('button', { name: /\uB514\uB7ED\uC2A4/ }));

    expect(
      await screen.findByText(
        '\uD50C\uB79C \uBCC0\uACBD \uC815\uBCF4\uB97C \uBD88\uB7EC\uC624\uC9C0 \uBABB\uD588\uC2B5\uB2C8\uB2E4. \uB2E4\uC2DC \uC2DC\uB3C4\uD574\uC8FC\uC138\uC694.',
      ),
    ).toBeInTheDocument();
    expect(changeMySubscriptionMock).not.toHaveBeenCalled();
    expect(
      screen.queryByRole('button', {
        name: '\uCC28\uC561 \uACB0\uC81C \uD6C4 \uBCC0\uACBD',
      }),
    ).not.toBeInTheDocument();

    fireEvent.click(
      screen.getByRole('button', {
        name: '\uBCC0\uACBD \uC815\uBCF4 \uB2E4\uC2DC \uC2DC\uB3C4',
      }),
    );

    expect(
      await screen.findByRole('button', {
        name: '\uCC28\uC561 \uACB0\uC81C \uD6C4 \uBCC0\uACBD',
      }),
    ).toBeEnabled();
    expect(fetchSubscriptionChangePreviewMock).toHaveBeenCalledTimes(2);
  });

  it('ignores a late preview completion after the selected plan changes', async () => {
    const stalePreview = deferred<Record<string, unknown>>();
    fetchSubscriptionPlansMock.mockResolvedValue([
      plan(1, 'STANDARD', 9900),
      plan(2, 'DELUXE', 19900),
      plan(3, 'PREMIUM', 29900),
    ]);
    fetchMySubscriptionMock.mockResolvedValue(subscriptionState(1, 'ACTIVE'));
    fetchMyBillingAgreementMock.mockResolvedValue(billingState('ACTIVE'));
    fetchSubscriptionChangePreviewMock
      .mockReturnValueOnce(stalePreview.promise)
      .mockResolvedValueOnce({
        changeType: 'UPGRADE',
        proratedAmount: 10000,
        effectiveDate: '2026-08-14',
        nextBillingDate: '2026-09-01',
        nextBillingAmount: 29900,
        newPlanName: 'PREMIUM',
        newBillingCycle: 'MONTHLY',
      });

    renderPage();
    fireEvent.click(await screen.findByRole('button', { name: /\uB514\uB7ED\uC2A4/ }));
    fireEvent.click(screen.getByRole('button', { name: /\uD504\uB9AC\uBBF8\uC5C4/ }));

    expect(await screen.findByText('\u20A929,900')).toBeInTheDocument();

    await act(async () => {
      stalePreview.resolve({
        changeType: 'UPGRADE',
        proratedAmount: 5000,
        effectiveDate: '2026-08-14',
        nextBillingDate: '2026-09-01',
        nextBillingAmount: 19900,
        newPlanName: 'DELUXE',
        newBillingCycle: 'MONTHLY',
      });
    });

    expect(screen.getByText('\u20A929,900')).toBeInTheDocument();
    expect(screen.queryByText('\u20A919,900')).not.toBeInTheDocument();
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
      provider: 'TOSS',
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

  it('offers payment-method re-registration when the billing agreement is expired', async () => {
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
      provider: 'TOSS',
      status: 'EXPIRED',
      payMethod: null,
      maskedMethod: null,
      nextBillingAt: null,
      lastChargedAt: '2026-05-01T00:00:00',
      cancelledAt: null,
      subscription: null,
    });

    renderPage();

    fireEvent.click(await screen.findByRole('button', { name: '결제수단 다시 등록' }));

    expect(navigateMock).toHaveBeenCalledWith(
      '/subscriptions/checkout?planId=1&userType=INDIVIDUAL&billingCycle=MONTHLY&purpose=BILLING_AGREEMENT',
    );
  });

  it('treats an abandoned READY billing agreement as incomplete and restartable', async () => {
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
      provider: 'TOSS',
      status: 'READY',
      payMethod: null,
      maskedMethod: null,
      nextBillingAt: null,
      lastChargedAt: null,
      cancelledAt: null,
      subscription: null,
    });

    renderPage();

    await screen.findByText(
      '카드 등록이 완료되지 않았습니다. 결제수단 다시 등록을 눌러 Toss 카드 등록을 다시 시작해주세요.',
    );
    expect(screen.getAllByText('등록 미완료')).toHaveLength(2);

    fireEvent.click(screen.getByRole('button', { name: '결제수단 다시 등록' }));

    expect(navigateMock).toHaveBeenCalledWith(
      '/subscriptions/checkout?planId=1&userType=INDIVIDUAL&billingCycle=MONTHLY&purpose=BILLING_AGREEMENT',
    );
  });

  it('routes upgrade users to payment-method registration when billing auth is incomplete', async () => {
    fetchSubscriptionPlansMock.mockResolvedValue([
      {
        id: 1,
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
    ]);
    fetchMySubscriptionMock.mockResolvedValue({
      id: 100,
      subscription: {
        id: 1,
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
      billingCycle: 'YEARLY',
      status: 'ACTIVE',
      startedAt: '2026-05-22',
      expiresAt: '2027-05-22',
      pendingSubscriptionId: 1,
      pendingBillingCycle: 'MONTHLY',
    });
    fetchSubscriptionChangePreviewMock.mockResolvedValue({
      changeType: 'UPGRADE',
      proratedAmount: 99726,
      effectiveDate: '2026-05-23',
      nextBillingDate: '2027-05-22',
      nextBillingAmount: 29900,
      newPlanName: 'PREMIUM',
      newBillingCycle: 'MONTHLY',
    });
    fetchMyBillingAgreementMock.mockResolvedValue({
      provider: 'TOSS',
      status: 'READY',
      payMethod: null,
      maskedMethod: null,
      nextBillingAt: null,
      lastChargedAt: null,
      cancelledAt: null,
      subscription: null,
    });

    renderPage();

    fireEvent.click(await screen.findByText('프리미엄'));
    await screen.findByText(
      '업그레이드를 적용하려면 자동결제 수단 등록이 먼저 필요합니다. 결제수단을 다시 등록한 뒤 플랜 변경을 진행해주세요.',
    );
    expect(
      screen.queryByText(
        '업그레이드는 등록된 결제수단이 필요합니다. 기존 단건 결제창으로는 진행하지 않습니다.',
      ),
    ).not.toBeInTheDocument();

    fireEvent.click(screen.getByRole('button', { name: '결제수단 등록하기' }));

    expect(changeMySubscriptionMock).not.toHaveBeenCalled();
    expect(navigateMock).toHaveBeenCalledWith(
      '/subscriptions/checkout?planId=1&userType=INDIVIDUAL&billingCycle=YEARLY&purpose=BILLING_AGREEMENT&returnPlanId=2&returnUserType=INDIVIDUAL&returnBillingCycle=YEARLY&returnAmount=99726',
    );
  });

  it('quotes the grace-extended subscription expiry and keeps cancel at zero mutations', async () => {
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
      expiresAt: '2026-06-08',
      pendingSubscriptionId: null,
      pendingBillingCycle: null,
    });
    fetchMyBillingAgreementMock.mockResolvedValue({
      provider: 'TOSS',
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

    const reactivateButton = await screen.findByRole('button', {
      name: '\uAD6C\uB3C5 \uC720\uC9C0\uD558\uAE30',
    });
    fireEvent.click(reactivateButton);

    expect(reactivateMySubscriptionMock).not.toHaveBeenCalled();
    expect(screen.getByText(/2026\.06\.08.*9,900/)).toBeInTheDocument();
    expect(screen.queryByText(/2026\.06\.01.*9,900/)).not.toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: '\uB3CC\uC544\uAC00\uAE30' }));
    expect(reactivateMySubscriptionMock).not.toHaveBeenCalled();
    expect(
      screen.queryByRole('button', {
        name: '\uAD6C\uB3C5 \uC720\uC9C0 \uD655\uC815',
      }),
    ).not.toBeInTheDocument();

    fireEvent.click(reactivateButton);
    expect(reactivateMySubscriptionMock).not.toHaveBeenCalled();
    fireEvent.click(
      screen.getByRole('button', {
        name: '\uAD6C\uB3C5 \uC720\uC9C0 \uD655\uC815',
      }),
    );

    await waitFor(() => {
      expect(reactivateMySubscriptionMock).toHaveBeenCalledTimes(1);
    });
  });

  it('quotes an already-active billing agreement canonical next billing date', async () => {
    fetchMySubscriptionMock.mockResolvedValue({
      ...subscriptionState(1, 'CANCELLED'),
      expiresAt: '2026-09-08',
    });
    fetchMyBillingAgreementMock.mockResolvedValue({
      ...billingState('ACTIVE'),
      nextBillingAt: '2026-09-15',
    });

    renderPage();

    fireEvent.click(
      await screen.findByRole('button', { name: '\uAD6C\uB3C5 \uC720\uC9C0\uD558\uAE30' }),
    );

    expect(screen.getByText(/2026\.09\.15.*9,900/)).toBeInTheDocument();
    expect(screen.queryByText(/2026\.09\.08.*9,900/)).not.toBeInTheDocument();
    expect(reactivateMySubscriptionMock).not.toHaveBeenCalled();
  });

  it('blocks reactivation when an active billing agreement has no canonical billing date', async () => {
    fetchMySubscriptionMock.mockResolvedValue(subscriptionState(1, 'CANCELLED'));
    fetchMyBillingAgreementMock.mockResolvedValue({
      ...billingState('ACTIVE'),
      nextBillingAt: null,
    });

    renderPage();

    const reactivateButton = await screen.findByRole('button', {
      name: '\uAD6C\uB3C5 \uC720\uC9C0\uD558\uAE30',
    });
    expect(reactivateButton).toBeDisabled();
    fireEvent.click(reactivateButton);
    expect(
      screen.queryByRole('button', {
        name: '\uAD6C\uB3C5 \uC720\uC9C0 \uD655\uC815',
      }),
    ).not.toBeInTheDocument();
    expect(reactivateMySubscriptionMock).not.toHaveBeenCalled();
  });

  it.each([
    ['pending plan and cycle', 2, 'YEARLY', '199,000'],
    ['pending cycle only', 1, 'YEARLY', '99,000'],
    ['pending plan only', 2, null, '19,900'],
  ] as const)(
    'quotes the backend renewal target for %s reactivation',
    async (_label, pendingSubscriptionId, pendingBillingCycle, expectedAmount) => {
      fetchSubscriptionPlansMock.mockResolvedValue([
        plan(1, 'STANDARD', 9900),
        plan(2, 'DELUXE', 19900),
      ]);
      fetchMySubscriptionMock.mockResolvedValue({
        ...subscriptionState(1, 'CANCELLED'),
        pendingSubscriptionId,
        pendingBillingCycle,
      });
      fetchMyBillingAgreementMock.mockResolvedValue(billingState('CANCELLED'));

      renderPage();

      fireEvent.click(
        await screen.findByRole('button', { name: '\uAD6C\uB3C5 \uC720\uC9C0\uD558\uAE30' }),
      );

      expect(screen.getByText(new RegExp(`2026\\.09\\.01.*${expectedAmount}`))).toBeInTheDocument();
      expect(reactivateMySubscriptionMock).not.toHaveBeenCalled();
    },
  );

  it('quotes the embedded current plan when a cycle-only target is absent from active plans', async () => {
    fetchSubscriptionPlansMock.mockResolvedValue([plan(2, 'DELUXE', 19900)]);
    fetchMySubscriptionMock.mockResolvedValue({
      ...subscriptionState(1, 'CANCELLED'),
      pendingSubscriptionId: 1,
      pendingBillingCycle: 'YEARLY',
    });
    fetchMyBillingAgreementMock.mockResolvedValue(billingState('CANCELLED'));

    renderPage();

    const reactivateButton = await screen.findByRole('button', {
      name: '\uAD6C\uB3C5 \uC720\uC9C0\uD558\uAE30',
    });
    expect(reactivateButton).toBeEnabled();
    fireEvent.click(reactivateButton);

    expect(screen.getByText(/2026\.09\.01.*99,000/)).toBeInTheDocument();
    expect(reactivateMySubscriptionMock).not.toHaveBeenCalled();
  });

  it('blocks reactivation when the pending target plan cannot be resolved', async () => {
    fetchMySubscriptionMock.mockResolvedValue({
      ...subscriptionState(1, 'CANCELLED'),
      pendingSubscriptionId: 999,
      pendingBillingCycle: 'YEARLY',
    });
    fetchMyBillingAgreementMock.mockResolvedValue(billingState('CANCELLED'));

    renderPage();

    const reactivateButton = await screen.findByRole('button', {
      name: '\uAD6C\uB3C5 \uC720\uC9C0\uD558\uAE30',
    });
    expect(reactivateButton).toBeDisabled();
    fireEvent.click(reactivateButton);
    expect(
      screen.queryByRole('button', {
        name: '\uAD6C\uB3C5 \uC720\uC9C0 \uD655\uC815',
      }),
    ).not.toBeInTheDocument();
    expect(reactivateMySubscriptionMock).not.toHaveBeenCalled();
  });

  it('preserves a successful charged upgrade as RELOAD_FAILED and retries reads only', async () => {
    configureChargedUpgrade();
    changeMySubscriptionMock.mockResolvedValue(upgradeResponse());
    fetchSubscriptionUpgradeOutcomeMock.mockResolvedValue(doneUpgradeOutcome());
    fetchMySubscriptionMock
      .mockResolvedValueOnce(subscriptionState(1, 'ACTIVE'))
      .mockResolvedValueOnce(subscriptionState(2, 'ACTIVE'))
      .mockResolvedValueOnce(subscriptionState(2, 'ACTIVE'));
    fetchMyBillingAgreementMock
      .mockResolvedValueOnce(billingState('ACTIVE'))
      .mockRejectedValueOnce(new Error('reload unavailable'))
      .mockResolvedValueOnce(billingState('ACTIVE', 2));

    renderPage();
    fireEvent.click(await screen.findByText('\uB514\uB7ED\uC2A4'));
    fireEvent.click(
      await screen.findByRole('button', { name: '\uCC28\uC561 \uACB0\uC81C \uD6C4 \uBCC0\uACBD' }),
    );

    expect(
      await screen.findByText(
        '\uC694\uCCAD\uC740 \uC644\uB8CC\uB418\uC5C8\uC9C0\uB9CC \uCD5C\uC2E0 \uAD6C\uB3C5 \uC815\uBCF4\uB97C \uBD88\uB7EC\uC624\uC9C0 \uBABB\uD588\uC2B5\uB2C8\uB2E4.',
      ),
    ).toBeInTheDocument();
    expect(changeMySubscriptionMock).toHaveBeenCalledTimes(1);

    fireEvent.click(screen.getByRole('button', { name: '\uC0C1\uD0DC \uB2E4\uC2DC \uD655\uC778' }));

    await waitFor(() => {
      expect(
        screen.queryByText(
          '\uC694\uCCAD\uC740 \uC644\uB8CC\uB418\uC5C8\uC9C0\uB9CC \uCD5C\uC2E0 \uAD6C\uB3C5 \uC815\uBCF4\uB97C \uBD88\uB7EC\uC624\uC9C0 \uBABB\uD588\uC2B5\uB2C8\uB2E4.',
        ),
      ).not.toBeInTheDocument();
    });
    expect(changeMySubscriptionMock).toHaveBeenCalledTimes(1);
    expect(fetchSubscriptionUpgradeOutcomeMock).toHaveBeenCalledTimes(2);
  });

  it('recovers a lost charged-upgrade response from exact DONE outcome without replay', async () => {
    configureChargedUpgrade();
    changeMySubscriptionMock.mockRejectedValue(new Error('response lost'));
    fetchSubscriptionUpgradeOutcomeMock.mockResolvedValue(doneUpgradeOutcome());
    fetchMySubscriptionMock
      .mockResolvedValueOnce(subscriptionState(1, 'ACTIVE'))
      .mockResolvedValueOnce(subscriptionState(2, 'ACTIVE'));
    fetchMyBillingAgreementMock
      .mockResolvedValueOnce(billingState('ACTIVE'))
      .mockResolvedValueOnce(billingState('ACTIVE', 2));

    renderPage();
    fireEvent.click(await screen.findByText('\uB514\uB7ED\uC2A4'));
    fireEvent.click(
      await screen.findByRole('button', { name: '\uCC28\uC561 \uACB0\uC81C \uD6C4 \uBCC0\uACBD' }),
    );

    await waitFor(() => {
      expect(changeMySubscriptionMock).toHaveBeenCalledTimes(1);
      expect(fetchSubscriptionUpgradeOutcomeMock).toHaveBeenCalledWith(2, 'MONTHLY');
    });
    expect(
      await screen.findByText(
        '\uC0C1\uD0DC \uD655\uC778\uC73C\uB85C \uD50C\uB79C \uBCC0\uACBD \uC644\uB8CC\uB97C \uD655\uC778\uD588\uC2B5\uB2C8\uB2E4.',
      ),
    ).toBeInTheDocument();
    expect(changeMySubscriptionMock).toHaveBeenCalledTimes(1);
  });

  it.each([null, 999])(
    'keeps a DONE charged upgrade UNKNOWN when outcome aggregate ID is %s',
    async (userSubscriptionId) => {
      configureChargedUpgrade();
      changeMySubscriptionMock.mockRejectedValue(new Error('response lost'));
      fetchSubscriptionUpgradeOutcomeMock.mockResolvedValue({
        ...doneUpgradeOutcome(),
        userSubscriptionId,
      });
      fetchMySubscriptionMock
        .mockResolvedValueOnce(subscriptionState(1, 'ACTIVE'))
        .mockResolvedValueOnce(subscriptionState(2, 'ACTIVE'));
      fetchMyBillingAgreementMock
        .mockResolvedValueOnce(billingState('ACTIVE'))
        .mockResolvedValueOnce(billingState('ACTIVE', 2));

      renderPage();
      fireEvent.click(await screen.findByText('\uB514\uB7ED\uC2A4'));
      fireEvent.click(
        await screen.findByRole('button', {
          name: '\uCC28\uC561 \uACB0\uC81C \uD6C4 \uBCC0\uACBD',
        }),
      );

      expect(
        await screen.findByText(
          '\uCC98\uB9AC\uAC00 \uC774\uBBF8 \uC644\uB8CC\uB418\uC5C8\uC744 \uC218 \uC788\uC2B5\uB2C8\uB2E4. \uC791\uC5C5\uC744 \uB2E4\uC2DC \uC2E4\uD589\uD558\uC9C0 \uB9D0\uACE0 \uC0C1\uD0DC\uB97C \uB2E4\uC2DC \uD655\uC778\uD574\uC8FC\uC138\uC694.',
        ),
      ).toBeInTheDocument();
      expect(changeMySubscriptionMock).toHaveBeenCalledTimes(1);
    },
  );

  it('uses the successful mutation response classification instead of the preview', async () => {
    configureChargedUpgrade();
    changeMySubscriptionMock.mockResolvedValue({
      ...upgradeResponse(),
      changeType: 'SCHEDULED_CHANGE',
      proratedAmount: 0,
    });
    fetchMySubscriptionMock
      .mockResolvedValueOnce(subscriptionState(1, 'ACTIVE'))
      .mockResolvedValueOnce({
        ...subscriptionState(1, 'ACTIVE'),
        pendingSubscriptionId: 2,
        pendingBillingCycle: 'MONTHLY',
      });
    fetchMyBillingAgreementMock
      .mockResolvedValueOnce(billingState('ACTIVE'))
      .mockResolvedValueOnce(billingState('ACTIVE'));

    renderPage();
    fireEvent.click(await screen.findByText('\uB514\uB7ED\uC2A4'));
    fireEvent.click(
      await screen.findByRole('button', {
        name: '\uCC28\uC561 \uACB0\uC81C \uD6C4 \uBCC0\uACBD',
      }),
    );

    expect(
      await screen.findByText(/\uBCC0\uACBD\uC774 \uC608\uC57D\uB418\uC5C8\uC2B5\uB2C8\uB2E4/),
    ).toBeInTheDocument();
    expect(fetchSubscriptionUpgradeOutcomeMock).not.toHaveBeenCalled();
    expect(changeMySubscriptionMock).toHaveBeenCalledTimes(1);
  });

  it('recovers a lost reactivation response from canonical subscription and agreement reads', async () => {
    fetchMySubscriptionMock
      .mockResolvedValueOnce(subscriptionState(1, 'CANCELLED'))
      .mockResolvedValueOnce(subscriptionState(1, 'ACTIVE'));
    fetchMyBillingAgreementMock
      .mockResolvedValueOnce(billingState('CANCELLED'))
      .mockResolvedValueOnce(billingState('ACTIVE'));
    reactivateMySubscriptionMock.mockRejectedValue(new Error('response lost'));

    renderPage();
    await confirmReactivation();

    expect(
      await screen.findByText(
        '\uC0C1\uD0DC \uD655\uC778\uC73C\uB85C \uAD6C\uB3C5 \uC720\uC9C0 \uC644\uB8CC\uB97C \uD655\uC778\uD588\uC2B5\uB2C8\uB2E4.',
      ),
    ).toBeInTheDocument();
    expect(reactivateMySubscriptionMock).toHaveBeenCalledTimes(1);
    expect(fetchSubscriptionUpgradeOutcomeMock).not.toHaveBeenCalled();
  });

  it('recovers a lost cancellation response from canonical reads without replay', async () => {
    fetchMySubscriptionMock
      .mockResolvedValueOnce(subscriptionState(1, 'ACTIVE'))
      .mockResolvedValueOnce(subscriptionState(1, 'CANCELLED'));
    fetchMyBillingAgreementMock
      .mockResolvedValueOnce(billingState('ACTIVE'))
      .mockResolvedValueOnce(billingState('CANCELLED'));
    cancelMySubscriptionMock.mockRejectedValue(new Error('response lost'));

    renderPage();
    fireEvent.click(await screen.findByRole('button', { name: '\uAD6C\uB3C5 \uCDE8\uC18C' }));
    fireEvent.click(screen.getByRole('button', { name: '\uCDE8\uC18C \uD655\uC815' }));

    expect(
      await screen.findByText(
        '\uC0C1\uD0DC \uD655\uC778\uC73C\uB85C \uAD6C\uB3C5 \uCDE8\uC18C \uC644\uB8CC\uB97C \uD655\uC778\uD588\uC2B5\uB2C8\uB2E4.',
      ),
    ).toBeInTheDocument();
    expect(cancelMySubscriptionMock).toHaveBeenCalledTimes(1);
    expect(fetchSubscriptionUpgradeOutcomeMock).not.toHaveBeenCalled();
  });

  it('keeps a successful cancellation RELOAD_FAILED when canonical reload fails', async () => {
    fetchMySubscriptionMock
      .mockResolvedValueOnce(subscriptionState(1, 'ACTIVE'))
      .mockResolvedValueOnce(subscriptionState(1, 'CANCELLED'));
    fetchMyBillingAgreementMock
      .mockResolvedValueOnce(billingState('ACTIVE'))
      .mockRejectedValueOnce(new Error('reload unavailable'));
    cancelMySubscriptionMock.mockResolvedValue({ status: 'CANCELLED' });

    renderPage();
    fireEvent.click(await screen.findByRole('button', { name: '\uAD6C\uB3C5 \uCDE8\uC18C' }));
    fireEvent.click(screen.getByRole('button', { name: '\uCDE8\uC18C \uD655\uC815' }));

    expect(
      await screen.findByText(
        '\uC694\uCCAD\uC740 \uC644\uB8CC\uB418\uC5C8\uC9C0\uB9CC \uCD5C\uC2E0 \uAD6C\uB3C5 \uC815\uBCF4\uB97C \uBD88\uB7EC\uC624\uC9C0 \uBABB\uD588\uC2B5\uB2C8\uB2E4.',
      ),
    ).toBeInTheDocument();
    expect(cancelMySubscriptionMock).toHaveBeenCalledTimes(1);
  });

  it('keeps a successful reactivation RELOAD_FAILED when canonical reload fails', async () => {
    fetchMySubscriptionMock
      .mockResolvedValueOnce(subscriptionState(1, 'CANCELLED'))
      .mockResolvedValueOnce(subscriptionState(1, 'ACTIVE'));
    fetchMyBillingAgreementMock
      .mockResolvedValueOnce(billingState('CANCELLED'))
      .mockRejectedValueOnce(new Error('reload unavailable'));
    reactivateMySubscriptionMock.mockResolvedValue({ status: 'ACTIVE' });

    renderPage();
    await confirmReactivation();

    expect(
      await screen.findByText(
        '\uC694\uCCAD\uC740 \uC644\uB8CC\uB418\uC5C8\uC9C0\uB9CC \uCD5C\uC2E0 \uAD6C\uB3C5 \uC815\uBCF4\uB97C \uBD88\uB7EC\uC624\uC9C0 \uBABB\uD588\uC2B5\uB2C8\uB2E4.',
      ),
    ).toBeInTheDocument();
    expect(reactivateMySubscriptionMock).toHaveBeenCalledTimes(1);
  });

  it('allows only one mutation across rapid change and cancel actions', async () => {
    configureChargedUpgrade();
    let resolveChange!: (value: ReturnType<typeof upgradeResponse>) => void;
    changeMySubscriptionMock.mockReturnValueOnce(
      new Promise<ReturnType<typeof upgradeResponse>>((resolve) => {
        resolveChange = resolve;
      }),
    );
    fetchSubscriptionUpgradeOutcomeMock.mockResolvedValue(doneUpgradeOutcome());
    fetchMySubscriptionMock
      .mockResolvedValueOnce(subscriptionState(1, 'ACTIVE'))
      .mockResolvedValueOnce(subscriptionState(2, 'ACTIVE'));
    fetchMyBillingAgreementMock
      .mockResolvedValueOnce(billingState('ACTIVE'))
      .mockResolvedValueOnce(billingState('ACTIVE', 2));

    renderPage();
    fireEvent.click(await screen.findByText('\uB514\uB7ED\uC2A4'));
    const changeButton = await screen.findByRole('button', {
      name: '\uCC28\uC561 \uACB0\uC81C \uD6C4 \uBCC0\uACBD',
    });
    fireEvent.click(screen.getByRole('button', { name: '\uAD6C\uB3C5 \uCDE8\uC18C' }));
    const cancelButton = screen.getByRole('button', { name: '\uCDE8\uC18C \uD655\uC815' });

    fireEvent.click(changeButton);
    await waitFor(() => expect(cancelButton).toBeDisabled());
    fireEvent.click(cancelButton);

    expect(changeMySubscriptionMock).toHaveBeenCalledTimes(1);
    expect(cancelMySubscriptionMock).not.toHaveBeenCalled();
    expect(reactivateMySubscriptionMock).not.toHaveBeenCalled();

    await act(async () => {
      resolveChange(upgradeResponse());
    });
    await waitFor(() => expect(fetchSubscriptionUpgradeOutcomeMock).toHaveBeenCalledTimes(1));
  });

  it('blocks same and cross mutations while recovery is UNKNOWN', async () => {
    configureChargedUpgrade();
    changeMySubscriptionMock.mockRejectedValue(new Error('response lost'));
    fetchSubscriptionUpgradeOutcomeMock.mockResolvedValue({
      ...doneUpgradeOutcome(),
      orderStatus: 'PROCESSING',
      userSubscriptionId: null,
    });
    fetchMySubscriptionMock.mockResolvedValueOnce(subscriptionState(1, 'ACTIVE'));
    fetchMyBillingAgreementMock.mockResolvedValueOnce(billingState('ACTIVE'));

    renderPage();
    fireEvent.click(await screen.findByText('\uB514\uB7ED\uC2A4'));
    const changeButton = await screen.findByRole('button', {
      name: '\uCC28\uC561 \uACB0\uC81C \uD6C4 \uBCC0\uACBD',
    });
    fireEvent.click(screen.getByRole('button', { name: '\uAD6C\uB3C5 \uCDE8\uC18C' }));
    const cancelButton = screen.getByRole('button', { name: '\uCDE8\uC18C \uD655\uC815' });
    fireEvent.click(changeButton);

    await screen.findByText(
      '\uCC98\uB9AC\uAC00 \uC774\uBBF8 \uC644\uB8CC\uB418\uC5C8\uC744 \uC218 \uC788\uC2B5\uB2C8\uB2E4. \uC791\uC5C5\uC744 \uB2E4\uC2DC \uC2E4\uD589\uD558\uC9C0 \uB9D0\uACE0 \uC0C1\uD0DC\uB97C \uB2E4\uC2DC \uD655\uC778\uD574\uC8FC\uC138\uC694.',
    );
    expect(changeButton).toBeDisabled();
    expect(cancelButton).toBeDisabled();
    fireEvent.click(changeButton);
    fireEvent.click(cancelButton);

    expect(changeMySubscriptionMock).toHaveBeenCalledTimes(1);
    expect(cancelMySubscriptionMock).not.toHaveBeenCalled();
    expect(reactivateMySubscriptionMock).not.toHaveBeenCalled();
    expect(
      screen.getByRole('button', { name: '\uC0C1\uD0DC \uB2E4\uC2DC \uD655\uC778' }),
    ).toBeEnabled();
  });

  it('blocks same and cross mutations while recovery is RELOAD_FAILED', async () => {
    configureChargedUpgrade();
    cancelMySubscriptionMock.mockResolvedValue(undefined);
    fetchMySubscriptionMock
      .mockResolvedValueOnce(subscriptionState(1, 'ACTIVE'))
      .mockResolvedValueOnce(subscriptionState(1, 'CANCELLED'));
    fetchMyBillingAgreementMock
      .mockResolvedValueOnce(billingState('ACTIVE'))
      .mockRejectedValueOnce(new Error('reload unavailable'));

    renderPage();
    fireEvent.click(await screen.findByText('\uB514\uB7ED\uC2A4'));
    const changeButton = await screen.findByRole('button', {
      name: '\uCC28\uC561 \uACB0\uC81C \uD6C4 \uBCC0\uACBD',
    });
    fireEvent.click(screen.getByRole('button', { name: '\uAD6C\uB3C5 \uCDE8\uC18C' }));
    fireEvent.click(screen.getByRole('button', { name: '\uCDE8\uC18C \uD655\uC815' }));

    await screen.findByText(
      '\uC694\uCCAD\uC740 \uC644\uB8CC\uB418\uC5C8\uC9C0\uB9CC \uCD5C\uC2E0 \uAD6C\uB3C5 \uC815\uBCF4\uB97C \uBD88\uB7EC\uC624\uC9C0 \uBABB\uD588\uC2B5\uB2C8\uB2E4.',
    );
    const cancelButton = screen.getByRole('button', { name: '\uAD6C\uB3C5 \uCDE8\uC18C' });
    expect(changeButton).toBeDisabled();
    expect(cancelButton).toBeDisabled();
    fireEvent.click(changeButton);
    fireEvent.click(cancelButton);

    expect(cancelMySubscriptionMock).toHaveBeenCalledTimes(1);
    expect(changeMySubscriptionMock).not.toHaveBeenCalled();
    expect(reactivateMySubscriptionMock).not.toHaveBeenCalled();
    expect(
      screen.getByRole('button', { name: '\uC0C1\uD0DC \uB2E4\uC2DC \uD655\uC778' }),
    ).toBeEnabled();
  });

  it('routes a no-charge CHANGE business rejection through conservative canonical reads', async () => {
    fetchSubscriptionPlansMock.mockResolvedValue([
      plan(1, 'STANDARD', 9900),
      plan(2, 'DELUXE', 19900),
    ]);
    fetchSubscriptionChangePreviewMock.mockResolvedValue({
      changeType: 'SCHEDULED_CHANGE',
      proratedAmount: 0,
      effectiveDate: '2026-09-01',
      nextBillingDate: '2026-09-01',
      nextBillingAmount: 9900,
      newPlanName: 'STANDARD',
      newBillingCycle: 'MONTHLY',
    });
    changeMySubscriptionMock.mockRejectedValue({
      response: {
        status: 404,
        data: { errorCode: 'RESOURCE_NOT_FOUND', message: '선택한 플랜이 없습니다.' },
      },
    });
    fetchMySubscriptionMock
      .mockResolvedValueOnce(subscriptionState(2, 'ACTIVE'))
      .mockResolvedValueOnce(subscriptionState(2, 'ACTIVE'));
    fetchMyBillingAgreementMock
      .mockResolvedValueOnce(billingState('ACTIVE', 2))
      .mockResolvedValueOnce(billingState('ACTIVE', 2));

    renderPage();
    fireEvent.click(await screen.findByText('\uC2A4\uD0E0\uB2E4\uB4DC'));
    const changeButton = await screen.findByRole('button', {
      name: '\uB2E4\uC74C \uACB0\uC81C\uC77C\uBD80\uD130 \uBCC0\uACBD \uC608\uC57D',
    });
    fireEvent.click(changeButton);

    expect(
      await screen.findByText(
        '\uCC98\uB9AC\uAC00 \uC774\uBBF8 \uC644\uB8CC\uB418\uC5C8\uC744 \uC218 \uC788\uC2B5\uB2C8\uB2E4. \uC791\uC5C5\uC744 \uB2E4\uC2DC \uC2E4\uD589\uD558\uC9C0 \uB9D0\uACE0 \uC0C1\uD0DC\uB97C \uB2E4\uC2DC \uD655\uC778\uD574\uC8FC\uC138\uC694.',
      ),
    ).toBeInTheDocument();
    expect(changeButton).toBeDisabled();
    fireEvent.click(changeButton);
    expect(changeMySubscriptionMock).toHaveBeenCalledTimes(1);
    expect(fetchMySubscriptionMock).toHaveBeenCalledTimes(2);
    expect(fetchMyBillingAgreementMock).toHaveBeenCalledTimes(2);
    expect(fetchSubscriptionUpgradeOutcomeMock).not.toHaveBeenCalled();
    expect(screen.queryByText('선택한 플랜이 없습니다.')).not.toBeInTheDocument();
  });

  it.each(['NO_ACTIVE_SUBSCRIPTION', 'RESOURCE_NOT_FOUND'])(
    'maps allowlisted cancellation rejection %s to FAILED with the server message',
    async (errorCode) => {
      fetchMySubscriptionMock.mockResolvedValueOnce(subscriptionState(1, 'ACTIVE'));
      fetchMyBillingAgreementMock.mockResolvedValueOnce(billingState('ACTIVE'));
      cancelMySubscriptionMock.mockRejectedValue({
        response: {
          status: 403,
          data: { errorCode, message: '취소할 구독이 없습니다.' },
        },
      });

      renderPage();
      fireEvent.click(await screen.findByRole('button', { name: '\uAD6C\uB3C5 \uCDE8\uC18C' }));
      fireEvent.click(screen.getByRole('button', { name: '\uCDE8\uC18C \uD655\uC815' }));

      expect(await screen.findByText('취소할 구독이 없습니다.')).toBeInTheDocument();
      expect(cancelMySubscriptionMock).toHaveBeenCalledTimes(1);
      expect(fetchMySubscriptionMock).toHaveBeenCalledTimes(1);
      expect(
        screen.queryByRole('button', { name: '\uC0C1\uD0DC \uB2E4\uC2DC \uD655\uC778' }),
      ).not.toBeInTheDocument();
    },
  );

  it.each([
    'NO_ACTIVE_SUBSCRIPTION',
    'RESOURCE_NOT_FOUND',
    'BILLING_AGREEMENT_NOT_FOUND',
    'BILLING_AGREEMENT_INVALID_STATE',
  ])(
    'maps allowlisted reactivation rejection %s to FAILED with the server message',
    async (errorCode) => {
      fetchMySubscriptionMock.mockResolvedValueOnce(subscriptionState(1, 'CANCELLED'));
      fetchMyBillingAgreementMock.mockResolvedValueOnce(billingState('CANCELLED'));
      reactivateMySubscriptionMock.mockRejectedValue({
        response: {
          status: 404,
          data: {
            errorCode,
            message: '자동결제 등록 정보를 찾을 수 없습니다.',
          },
        },
      });

      renderPage();
      await confirmReactivation();

      expect(await screen.findByText('자동결제 등록 정보를 찾을 수 없습니다.')).toBeInTheDocument();
      expect(reactivateMySubscriptionMock).toHaveBeenCalledTimes(1);
      expect(fetchMySubscriptionMock).toHaveBeenCalledTimes(1);
      expect(
        screen.queryByRole('button', { name: '\uC0C1\uD0DC \uB2E4\uC2DC \uD655\uC778' }),
      ).not.toBeInTheDocument();
    },
  );

  it.each([
    'PAYMENT_CONFIRM_FAILED',
    'BILLING_AGREEMENT_REAUTH_REQUIRED',
    'PAYMENT_PROVIDER_NOT_CONFIGURED',
  ])('does not directly fail a change for %s', async (errorCode) => {
    configureChargedUpgrade();
    changeMySubscriptionMock.mockRejectedValue({
      response: { status: 409, data: { errorCode, message: 'provider result is not terminal' } },
    });
    fetchSubscriptionUpgradeOutcomeMock.mockResolvedValue({
      ...doneUpgradeOutcome(),
      orderStatus: 'PROCESSING',
      userSubscriptionId: null,
    });
    fetchMySubscriptionMock.mockResolvedValueOnce(subscriptionState(1, 'ACTIVE'));
    fetchMyBillingAgreementMock.mockResolvedValueOnce(billingState('ACTIVE'));

    renderPage();
    fireEvent.click(await screen.findByText('\uB514\uB7ED\uC2A4'));
    fireEvent.click(
      await screen.findByRole('button', {
        name: '\uCC28\uC561 \uACB0\uC81C \uD6C4 \uBCC0\uACBD',
      }),
    );

    expect(
      await screen.findByText(
        '\uCC98\uB9AC\uAC00 \uC774\uBBF8 \uC644\uB8CC\uB418\uC5C8\uC744 \uC218 \uC788\uC2B5\uB2C8\uB2E4. \uC791\uC5C5\uC744 \uB2E4\uC2DC \uC2E4\uD589\uD558\uC9C0 \uB9D0\uACE0 \uC0C1\uD0DC\uB97C \uB2E4\uC2DC \uD655\uC778\uD574\uC8FC\uC138\uC694.',
      ),
    ).toBeInTheDocument();
    expect(screen.queryByText('provider result is not terminal')).not.toBeInTheDocument();
    expect(changeMySubscriptionMock).toHaveBeenCalledTimes(1);
  });

  it('commits a charged CHANGE from exact DONE evidence despite NO_ACTIVE_SUBSCRIPTION', async () => {
    configureChargedUpgrade();
    changeMySubscriptionMock.mockRejectedValue({
      response: {
        status: 409,
        data: { errorCode: 'NO_ACTIVE_SUBSCRIPTION', message: 'stale precondition response' },
      },
    });
    fetchSubscriptionUpgradeOutcomeMock.mockResolvedValue(doneUpgradeOutcome());
    fetchMySubscriptionMock
      .mockResolvedValueOnce(subscriptionState(1, 'ACTIVE'))
      .mockResolvedValueOnce(subscriptionState(2, 'ACTIVE'));
    fetchMyBillingAgreementMock
      .mockResolvedValueOnce(billingState('ACTIVE'))
      .mockResolvedValueOnce(billingState('ACTIVE', 2));

    renderPage();
    fireEvent.click(await screen.findByText('\uB514\uB7ED\uC2A4'));
    fireEvent.click(
      await screen.findByRole('button', { name: '\uCC28\uC561 \uACB0\uC81C \uD6C4 \uBCC0\uACBD' }),
    );

    expect(
      await screen.findByText(
        '\uC0C1\uD0DC \uD655\uC778\uC73C\uB85C \uD50C\uB79C \uBCC0\uACBD \uC644\uB8CC\uB97C \uD655\uC778\uD588\uC2B5\uB2C8\uB2E4.',
      ),
    ).toBeInTheDocument();
    expect(fetchSubscriptionUpgradeOutcomeMock).toHaveBeenCalledWith(2, 'MONTHLY');
    expect(changeMySubscriptionMock).toHaveBeenCalledTimes(1);
  });

  it.each([
    ['BILLING_AGREEMENT_INVALID_STATE', 'PROVIDER_SUCCEEDED', 100],
    ['NO_ACTIVE_SUBSCRIPTION', 'PROCESSING', 100],
    ['BILLING_AGREEMENT_INVALID_STATE', 'DONE', null],
  ] as const)(
    'keeps charged CHANGE %s with %s and linkage %s UNKNOWN without mutation replay',
    async (errorCode, orderStatus, userSubscriptionId) => {
      configureChargedUpgrade();
      changeMySubscriptionMock.mockRejectedValue({
        response: { status: 409, data: { errorCode, message: 'ambiguous change response' } },
      });
      fetchSubscriptionUpgradeOutcomeMock.mockResolvedValue({
        ...doneUpgradeOutcome(),
        orderStatus,
        userSubscriptionId,
      });
      fetchMySubscriptionMock.mockResolvedValueOnce(subscriptionState(1, 'ACTIVE'));
      fetchMyBillingAgreementMock.mockResolvedValueOnce(billingState('ACTIVE'));

      renderPage();
      fireEvent.click(await screen.findByText('\uB514\uB7ED\uC2A4'));
      const changeButton = await screen.findByRole('button', {
        name: '\uCC28\uC561 \uACB0\uC81C \uD6C4 \uBCC0\uACBD',
      });
      fireEvent.click(changeButton);

      expect(
        await screen.findByText(
          '\uCC98\uB9AC\uAC00 \uC774\uBBF8 \uC644\uB8CC\uB418\uC5C8\uC744 \uC218 \uC788\uC2B5\uB2C8\uB2E4. \uC791\uC5C5\uC744 \uB2E4\uC2DC \uC2E4\uD589\uD558\uC9C0 \uB9D0\uACE0 \uC0C1\uD0DC\uB97C \uB2E4\uC2DC \uD655\uC778\uD574\uC8FC\uC138\uC694.',
        ),
      ).toBeInTheDocument();
      expect(fetchSubscriptionUpgradeOutcomeMock).toHaveBeenCalledWith(2, 'MONTHLY');
      expect(changeButton).toBeDisabled();
      fireEvent.click(changeButton);
      expect(changeMySubscriptionMock).toHaveBeenCalledTimes(1);
      expect(fetchMySubscriptionMock).toHaveBeenCalledTimes(1);
    },
  );

  it('does not directly fail cancellation for an arbitrary 4xx', async () => {
    fetchMySubscriptionMock
      .mockResolvedValueOnce(subscriptionState(1, 'ACTIVE'))
      .mockResolvedValueOnce(subscriptionState(1, 'ACTIVE'));
    fetchMyBillingAgreementMock
      .mockResolvedValueOnce(billingState('ACTIVE'))
      .mockResolvedValueOnce(billingState('ACTIVE'));
    cancelMySubscriptionMock.mockRejectedValue({
      response: {
        status: 409,
        data: { errorCode: 'BILLING_AGREEMENT_INVALID_STATE' },
      },
    });

    renderPage();
    fireEvent.click(await screen.findByRole('button', { name: '\uAD6C\uB3C5 \uCDE8\uC18C' }));
    fireEvent.click(screen.getByRole('button', { name: '\uCDE8\uC18C \uD655\uC815' }));

    expect(
      await screen.findByText(
        '\uCC98\uB9AC\uAC00 \uC774\uBBF8 \uC644\uB8CC\uB418\uC5C8\uC744 \uC218 \uC788\uC2B5\uB2C8\uB2E4. \uC791\uC5C5\uC744 \uB2E4\uC2DC \uC2E4\uD589\uD558\uC9C0 \uB9D0\uACE0 \uC0C1\uD0DC\uB97C \uB2E4\uC2DC \uD655\uC778\uD574\uC8FC\uC138\uC694.',
      ),
    ).toBeInTheDocument();
    expect(cancelMySubscriptionMock).toHaveBeenCalledTimes(1);
  });

  it('does not directly fail reactivation for a transport error', async () => {
    fetchMySubscriptionMock
      .mockResolvedValueOnce(subscriptionState(1, 'CANCELLED'))
      .mockResolvedValueOnce(subscriptionState(1, 'CANCELLED'));
    fetchMyBillingAgreementMock
      .mockResolvedValueOnce(billingState('CANCELLED'))
      .mockResolvedValueOnce(billingState('CANCELLED'));
    reactivateMySubscriptionMock.mockRejectedValue(new Error('response lost'));

    renderPage();
    await confirmReactivation();

    expect(
      await screen.findByText(
        '\uCC98\uB9AC\uAC00 \uC774\uBBF8 \uC644\uB8CC\uB418\uC5C8\uC744 \uC218 \uC788\uC2B5\uB2C8\uB2E4. \uC791\uC5C5\uC744 \uB2E4\uC2DC \uC2E4\uD589\uD558\uC9C0 \uB9D0\uACE0 \uC0C1\uD0DC\uB97C \uB2E4\uC2DC \uD655\uC778\uD574\uC8FC\uC138\uC694.',
      ),
    ).toBeInTheDocument();
    expect(reactivateMySubscriptionMock).toHaveBeenCalledTimes(1);
  });

  it('maps a terminal charged-upgrade command to FAILED without canonical success', async () => {
    configureChargedUpgrade();
    changeMySubscriptionMock.mockRejectedValue({ response: { status: 409 } });
    fetchSubscriptionUpgradeOutcomeMock.mockResolvedValue({
      ...doneUpgradeOutcome(),
      orderStatus: 'FAILED',
    });
    fetchMySubscriptionMock.mockResolvedValueOnce(subscriptionState(1, 'ACTIVE'));
    fetchMyBillingAgreementMock.mockResolvedValueOnce(billingState('ACTIVE'));

    renderPage();
    fireEvent.click(await screen.findByText('\uB514\uB7ED\uC2A4'));
    fireEvent.click(
      await screen.findByRole('button', { name: '\uCC28\uC561 \uACB0\uC81C \uD6C4 \uBCC0\uACBD' }),
    );

    expect(
      await screen.findByText(
        '\uC694\uCCAD\uC774 \uC644\uB8CC\uB418\uC9C0 \uC54A\uC558\uC2B5\uB2C8\uB2E4.',
      ),
    ).toBeInTheDocument();
    expect(changeMySubscriptionMock).toHaveBeenCalledTimes(1);
    expect(
      screen.queryByRole('button', { name: '\uC0C1\uD0DC \uB2E4\uC2DC \uD655\uC778' }),
    ).not.toBeInTheDocument();
  });

  it.each([401, 403, 404, 408, 409, 429])(
    'keeps an ambiguous HTTP %s charged-upgrade response UNKNOWN',
    async (status) => {
      configureChargedUpgrade();
      changeMySubscriptionMock.mockRejectedValue({ response: { status } });
      fetchSubscriptionUpgradeOutcomeMock.mockRejectedValue({ response: { status } });
      fetchMySubscriptionMock.mockResolvedValueOnce(subscriptionState(1, 'ACTIVE'));
      fetchMyBillingAgreementMock.mockResolvedValueOnce(billingState('ACTIVE'));

      renderPage();
      fireEvent.click(await screen.findByText('\uB514\uB7ED\uC2A4'));
      fireEvent.click(
        await screen.findByRole('button', {
          name: '\uCC28\uC561 \uACB0\uC81C \uD6C4 \uBCC0\uACBD',
        }),
      );

      expect(
        await screen.findByText(
          '\uCC98\uB9AC\uAC00 \uC774\uBBF8 \uC644\uB8CC\uB418\uC5C8\uC744 \uC218 \uC788\uC2B5\uB2C8\uB2E4. \uC791\uC5C5\uC744 \uB2E4\uC2DC \uC2E4\uD589\uD558\uC9C0 \uB9D0\uACE0 \uC0C1\uD0DC\uB97C \uB2E4\uC2DC \uD655\uC778\uD574\uC8FC\uC138\uC694.',
        ),
      ).toBeInTheDocument();
      expect(changeMySubscriptionMock).toHaveBeenCalledTimes(1);
      expect(
        screen.getByRole('button', { name: '\uC0C1\uD0DC \uB2E4\uC2DC \uD655\uC778' }),
      ).toBeInTheDocument();
    },
  );

  it.each(['CANCELLED', 'EXPIRED'] as const)(
    'does not commit a CHANGE when canonical subscription status is %s',
    async (status) => {
      fetchSubscriptionPlansMock.mockResolvedValue([
        plan(1, 'STANDARD', 9900),
        plan(2, 'DELUXE', 19900),
      ]);
      fetchSubscriptionChangePreviewMock.mockResolvedValue({
        changeType: 'SCHEDULED_CHANGE',
        proratedAmount: 0,
        effectiveDate: '2026-09-01',
        nextBillingDate: '2026-09-01',
        nextBillingAmount: 9900,
        newPlanName: 'STANDARD',
        newBillingCycle: 'MONTHLY',
      });
      changeMySubscriptionMock.mockRejectedValue(new Error('response lost'));
      fetchMySubscriptionMock
        .mockResolvedValueOnce(subscriptionState(2, 'ACTIVE'))
        .mockResolvedValueOnce({
          ...subscriptionState(2, 'ACTIVE'),
          status,
          pendingSubscriptionId: 1,
          pendingBillingCycle: 'MONTHLY',
        });
      fetchMyBillingAgreementMock
        .mockResolvedValueOnce(billingState('ACTIVE', 2))
        .mockResolvedValueOnce(billingState('ACTIVE', 2));

      renderPage();
      fireEvent.click(await screen.findByText('\uC2A4\uD0E0\uB2E4\uB4DC'));
      fireEvent.click(
        await screen.findByRole('button', {
          name: '\uB2E4\uC74C \uACB0\uC81C\uC77C\uBD80\uD130 \uBCC0\uACBD \uC608\uC57D',
        }),
      );

      expect(
        await screen.findByText(
          '\uCC98\uB9AC\uAC00 \uC774\uBBF8 \uC644\uB8CC\uB418\uC5C8\uC744 \uC218 \uC788\uC2B5\uB2C8\uB2E4. \uC791\uC5C5\uC744 \uB2E4\uC2DC \uC2E4\uD589\uD558\uC9C0 \uB9D0\uACE0 \uC0C1\uD0DC\uB97C \uB2E4\uC2DC \uD655\uC778\uD574\uC8FC\uC138\uC694.',
        ),
      ).toBeInTheDocument();
      expect(changeMySubscriptionMock).toHaveBeenCalledTimes(1);
      expect(
        screen.queryByText(
          '\uC0C1\uD0DC \uD655\uC778\uC73C\uB85C \uD50C\uB79C \uBCC0\uACBD \uC644\uB8CC\uB97C \uD655\uC778\uD588\uC2B5\uB2C8\uB2E4.',
        ),
      ).not.toBeInTheDocument();
    },
  );

  it.each(['aggregate', 'current plan', 'current cycle'] as const)(
    'does not commit a scheduled change when canonical %s drifts',
    async (drift) => {
      fetchSubscriptionPlansMock.mockResolvedValue([
        plan(1, 'STANDARD', 9900),
        plan(2, 'DELUXE', 19900),
      ]);
      fetchSubscriptionChangePreviewMock.mockResolvedValue({
        changeType: 'SCHEDULED_CHANGE',
        proratedAmount: 0,
        effectiveDate: '2026-09-01',
        nextBillingDate: '2026-09-01',
        nextBillingAmount: 9900,
        newPlanName: 'STANDARD',
        newBillingCycle: 'MONTHLY',
      });
      changeMySubscriptionMock.mockRejectedValue(new Error('response lost'));
      const canonical = {
        ...subscriptionState(drift === 'current plan' ? 3 : 2, 'ACTIVE'),
        id: drift === 'aggregate' ? 999 : 100,
        billingCycle: drift === 'current cycle' ? 'YEARLY' : 'MONTHLY',
        pendingSubscriptionId: 1,
        pendingBillingCycle: 'MONTHLY',
      };
      fetchMySubscriptionMock
        .mockResolvedValueOnce(subscriptionState(2, 'ACTIVE'))
        .mockResolvedValueOnce(canonical);
      fetchMyBillingAgreementMock
        .mockResolvedValueOnce(billingState('ACTIVE', 2))
        .mockResolvedValueOnce(
          billingState(
            'ACTIVE',
            canonical.subscription.id,
            canonical.billingCycle as 'MONTHLY' | 'YEARLY',
            canonical.id,
          ),
        );

      renderPage();
      fireEvent.click(await screen.findByText('\uC2A4\uD0E0\uB2E4\uB4DC'));
      fireEvent.click(
        await screen.findByRole('button', {
          name: '\uB2E4\uC74C \uACB0\uC81C\uC77C\uBD80\uD130 \uBCC0\uACBD \uC608\uC57D',
        }),
      );

      expect(
        await screen.findByText(
          '\uCC98\uB9AC\uAC00 \uC774\uBBF8 \uC644\uB8CC\uB418\uC5C8\uC744 \uC218 \uC788\uC2B5\uB2C8\uB2E4. \uC791\uC5C5\uC744 \uB2E4\uC2DC \uC2E4\uD589\uD558\uC9C0 \uB9D0\uACE0 \uC0C1\uD0DC\uB97C \uB2E4\uC2DC \uD655\uC778\uD574\uC8FC\uC138\uC694.',
        ),
      ).toBeInTheDocument();
      expect(changeMySubscriptionMock).toHaveBeenCalledTimes(1);
    },
  );

  it('does not commit when the billing agreement points to another subscription', async () => {
    fetchSubscriptionPlansMock.mockResolvedValue([
      plan(1, 'STANDARD', 9900),
      plan(2, 'DELUXE', 19900),
    ]);
    fetchSubscriptionChangePreviewMock.mockResolvedValue({
      changeType: 'SCHEDULED_CHANGE',
      proratedAmount: 0,
      effectiveDate: '2026-09-01',
      nextBillingDate: '2026-09-01',
      nextBillingAmount: 9900,
      newPlanName: 'STANDARD',
      newBillingCycle: 'MONTHLY',
    });
    changeMySubscriptionMock.mockRejectedValue(new Error('response lost'));
    const canonical = {
      ...subscriptionState(2, 'ACTIVE'),
      pendingSubscriptionId: 1,
      pendingBillingCycle: 'MONTHLY',
    };
    fetchMySubscriptionMock
      .mockResolvedValueOnce(subscriptionState(2, 'ACTIVE'))
      .mockResolvedValueOnce(canonical);
    fetchMyBillingAgreementMock
      .mockResolvedValueOnce(billingState('ACTIVE', 2))
      .mockResolvedValueOnce({
        ...billingState('ACTIVE', 2),
        subscription: {
          ...canonical,
          id: 999,
        },
      });

    renderPage();
    fireEvent.click(await screen.findByText('\uC2A4\uD0E0\uB2E4\uB4DC'));
    fireEvent.click(
      await screen.findByRole('button', {
        name: '\uB2E4\uC74C \uACB0\uC81C\uC77C\uBD80\uD130 \uBCC0\uACBD \uC608\uC57D',
      }),
    );

    expect(
      await screen.findByText(
        '\uCC98\uB9AC\uAC00 \uC774\uBBF8 \uC644\uB8CC\uB418\uC5C8\uC744 \uC218 \uC788\uC2B5\uB2C8\uB2E4. \uC791\uC5C5\uC744 \uB2E4\uC2DC \uC2E4\uD589\uD558\uC9C0 \uB9D0\uACE0 \uC0C1\uD0DC\uB97C \uB2E4\uC2DC \uD655\uC778\uD574\uC8FC\uC138\uC694.',
      ),
    ).toBeInTheDocument();
    expect(changeMySubscriptionMock).toHaveBeenCalledTimes(1);
  });

  it('does not commit when the billing agreement omits subscription identity', async () => {
    fetchSubscriptionPlansMock.mockResolvedValue([
      plan(1, 'STANDARD', 9900),
      plan(2, 'DELUXE', 19900),
    ]);
    fetchSubscriptionChangePreviewMock.mockResolvedValue({
      changeType: 'SCHEDULED_CHANGE',
      proratedAmount: 0,
      effectiveDate: '2026-09-01',
      nextBillingDate: '2026-09-01',
      nextBillingAmount: 9900,
      newPlanName: 'STANDARD',
      newBillingCycle: 'MONTHLY',
    });
    changeMySubscriptionMock.mockRejectedValue(new Error('response lost'));
    fetchMySubscriptionMock
      .mockResolvedValueOnce(subscriptionState(2, 'ACTIVE'))
      .mockResolvedValueOnce({
        ...subscriptionState(2, 'ACTIVE'),
        pendingSubscriptionId: 1,
        pendingBillingCycle: 'MONTHLY',
      });
    fetchMyBillingAgreementMock
      .mockResolvedValueOnce(billingState('ACTIVE', 2))
      .mockResolvedValueOnce({
        ...billingState('ACTIVE', 2),
        subscription: null,
      });

    renderPage();
    fireEvent.click(await screen.findByText('\uC2A4\uD0E0\uB2E4\uB4DC'));
    fireEvent.click(
      await screen.findByRole('button', {
        name: '\uB2E4\uC74C \uACB0\uC81C\uC77C\uBD80\uD130 \uBCC0\uACBD \uC608\uC57D',
      }),
    );

    expect(
      await screen.findByText(
        '\uCC98\uB9AC\uAC00 \uC774\uBBF8 \uC644\uB8CC\uB418\uC5C8\uC744 \uC218 \uC788\uC2B5\uB2C8\uB2E4. \uC791\uC5C5\uC744 \uB2E4\uC2DC \uC2E4\uD589\uD558\uC9C0 \uB9D0\uACE0 \uC0C1\uD0DC\uB97C \uB2E4\uC2DC \uD655\uC778\uD574\uC8FC\uC138\uC694.',
      ),
    ).toBeInTheDocument();
    expect(changeMySubscriptionMock).toHaveBeenCalledTimes(1);
  });

  it.each(['aggregate', 'current plan', 'current cycle'] as const)(
    'does not commit a DOWNGRADE when canonical source %s drifts',
    async (drift) => {
      fetchSubscriptionPlansMock.mockResolvedValue([
        plan(1, 'STANDARD', 9900),
        plan(2, 'DELUXE', 19900),
      ]);
      fetchSubscriptionChangePreviewMock.mockResolvedValue({
        changeType: 'DOWNGRADE',
        proratedAmount: 0,
        effectiveDate: '2026-09-01',
        nextBillingDate: '2026-09-01',
        nextBillingAmount: 9900,
        newPlanName: 'STANDARD',
        newBillingCycle: 'MONTHLY',
      });
      changeMySubscriptionMock.mockRejectedValue(new Error('response lost'));
      const canonical = {
        ...subscriptionState(drift === 'current plan' ? 3 : 2, 'ACTIVE'),
        id: drift === 'aggregate' ? 999 : 100,
        billingCycle: drift === 'current cycle' ? 'YEARLY' : 'MONTHLY',
        pendingSubscriptionId: 1,
        pendingBillingCycle: 'MONTHLY',
      };
      fetchMySubscriptionMock
        .mockResolvedValueOnce(subscriptionState(2, 'ACTIVE'))
        .mockResolvedValueOnce(canonical);
      fetchMyBillingAgreementMock
        .mockResolvedValueOnce(billingState('ACTIVE', 2))
        .mockResolvedValueOnce(
          billingState(
            'ACTIVE',
            canonical.subscription.id,
            canonical.billingCycle as 'MONTHLY' | 'YEARLY',
            canonical.id,
          ),
        );

      renderPage();
      fireEvent.click(await screen.findByText('\uC2A4\uD0E0\uB2E4\uB4DC'));
      fireEvent.click(
        await screen.findByRole('button', {
          name: '\uB2E4\uC74C \uACB0\uC81C\uC77C\uBD80\uD130 \uBCC0\uACBD \uC608\uC57D',
        }),
      );

      expect(
        await screen.findByText(
          '\uCC98\uB9AC\uAC00 \uC774\uBBF8 \uC644\uB8CC\uB418\uC5C8\uC744 \uC218 \uC788\uC2B5\uB2C8\uB2E4. \uC791\uC5C5\uC744 \uB2E4\uC2DC \uC2E4\uD589\uD558\uC9C0 \uB9D0\uACE0 \uC0C1\uD0DC\uB97C \uB2E4\uC2DC \uD655\uC778\uD574\uC8FC\uC138\uC694.',
        ),
      ).toBeInTheDocument();
      expect(changeMySubscriptionMock).toHaveBeenCalledTimes(1);
    },
  );

  it('recovers a lost no-charge scheduled change from canonical reads only', async () => {
    fetchSubscriptionPlansMock.mockResolvedValue([
      plan(1, 'STANDARD', 9900),
      plan(2, 'DELUXE', 19900),
    ]);
    fetchSubscriptionChangePreviewMock.mockResolvedValue({
      changeType: 'SCHEDULED_CHANGE',
      proratedAmount: 0,
      effectiveDate: '2026-09-01',
      nextBillingDate: '2026-09-01',
      nextBillingAmount: 9900,
      newPlanName: 'STANDARD',
      newBillingCycle: 'MONTHLY',
    });
    changeMySubscriptionMock.mockRejectedValue(new Error('response lost'));
    fetchMySubscriptionMock
      .mockResolvedValueOnce(subscriptionState(2, 'ACTIVE'))
      .mockResolvedValueOnce({
        ...subscriptionState(2, 'ACTIVE'),
        pendingSubscriptionId: 1,
        pendingBillingCycle: 'MONTHLY',
      });
    fetchMyBillingAgreementMock.mockResolvedValue(billingState('ACTIVE', 2));

    renderPage();
    fireEvent.click(await screen.findByText('\uC2A4\uD0E0\uB2E4\uB4DC'));
    fireEvent.click(
      await screen.findByRole('button', {
        name: '\uB2E4\uC74C \uACB0\uC81C\uC77C\uBD80\uD130 \uBCC0\uACBD \uC608\uC57D',
      }),
    );

    expect(
      await screen.findByText(
        '\uC0C1\uD0DC \uD655\uC778\uC73C\uB85C \uD50C\uB79C \uBCC0\uACBD \uC644\uB8CC\uB97C \uD655\uC778\uD588\uC2B5\uB2C8\uB2E4.',
      ),
    ).toBeInTheDocument();
    expect(changeMySubscriptionMock).toHaveBeenCalledTimes(1);
    expect(fetchSubscriptionUpgradeOutcomeMock).not.toHaveBeenCalled();
  });
});

function configureChargedUpgrade() {
  fetchSubscriptionPlansMock.mockResolvedValue([
    plan(1, 'STANDARD', 9900),
    plan(2, 'DELUXE', 19900),
  ]);
  fetchSubscriptionChangePreviewMock.mockResolvedValue({
    changeType: 'UPGRADE',
    proratedAmount: 5000,
    effectiveDate: '2026-08-12',
    nextBillingDate: '2026-09-01',
    nextBillingAmount: 19900,
    newPlanName: 'DELUXE',
    newBillingCycle: 'MONTHLY',
  });
}

function plan(id: number, name: string, priceMonthly: number) {
  return {
    id,
    name,
    description: name,
    userType: 'INDIVIDUAL',
    priceMonthly,
    priceYearly: priceMonthly * 10,
    downloadPerDay: 10,
    maxWhitelistChannels: 3,
    maxPlaylists: 5,
    isActive: true,
  };
}

function subscriptionState(planId: number, status: 'ACTIVE' | 'CANCELLED') {
  return {
    id: 100,
    subscription: plan(planId, planId === 1 ? 'STANDARD' : 'DELUXE', planId === 1 ? 9900 : 19900),
    billingCycle: 'MONTHLY',
    status,
    startedAt: '2026-08-01',
    expiresAt: '2026-09-01',
    pendingSubscriptionId: null,
    pendingBillingCycle: null,
  };
}

function billingState(
  status: 'ACTIVE' | 'CANCELLED',
  planId = 1,
  billingCycle: 'MONTHLY' | 'YEARLY' = 'MONTHLY',
  aggregateId = 100,
) {
  return {
    provider: 'TOSS',
    status,
    payMethod: 'CARD',
    maskedMethod: '1234',
    nextBillingAt: '2026-09-01',
    lastChargedAt: '2026-08-01T00:00:00',
    cancelledAt: status === 'CANCELLED' ? '2026-08-12T00:00:00' : null,
    subscription: {
      ...subscriptionState(planId, status),
      id: aggregateId,
      billingCycle,
    },
  };
}

function upgradeResponse() {
  return {
    subscription: { id: 2, name: 'DELUXE' },
    billingCycle: 'MONTHLY',
    status: 'ACTIVE',
    changeType: 'UPGRADE',
    proratedAmount: 5000,
    startedAt: '2026-08-01',
    expiresAt: '2026-09-01',
  };
}

function doneUpgradeOutcome() {
  return {
    purpose: 'UPGRADE',
    orderStatus: 'DONE',
    userSubscriptionId: 100,
    targetSubscriptionId: 2,
    targetBillingCycle: 'MONTHLY',
  };
}

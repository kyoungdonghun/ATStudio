import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { StrictMode } from 'react';
import { MemoryRouter } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import SubscriptionPaymentPage from '@/pages/subscriber/SubscriptionPaymentPage';

const navigateMock = vi.hoisted(() => vi.fn());
const toastShowMock = vi.hoisted(() => vi.fn());
const fetchSubscriptionPlansMock = vi.hoisted(() => vi.fn());
const prepareBillingAgreementMock = vi.hoisted(() => vi.fn());
const confirmBillingAgreementMock = vi.hoisted(() => vi.fn());
const fetchPaymentCommandOutcomeMock = vi.hoisted(() => vi.fn());
const fetchMySubscriptionMock = vi.hoisted(() => vi.fn());
const fetchMyBillingAgreementMock = vi.hoisted(() => vi.fn());
const requestBillingAuthMock = vi.hoisted(() => vi.fn());
const storageKey = 'ats.checkout-prepare-attempt.v1.SUBSCRIBE.1.INDIVIDUAL.MONTHLY';
const firstAttemptKey = '11111111-1111-4111-8111-111111111111';
const replacementAttemptKey = '22222222-2222-4222-8222-222222222222';

const authState: {
  user: { userType: 'INDIVIDUAL' | 'BUSINESS' };
} = {
  user: { userType: 'INDIVIDUAL' },
};

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual<typeof import('react-router-dom')>('react-router-dom');
  return {
    ...actual,
    useNavigate: () => navigateMock,
  };
});

vi.mock('@/store/toastStore', () => ({
  useToastStore: (selector: (state: { show: typeof toastShowMock }) => unknown) =>
    selector({ show: toastShowMock }),
}));

vi.mock('@/store/authStore', () => ({
  useAuthStore: (selector: (state: typeof authState) => unknown) => selector(authState),
}));

vi.mock('@/api/subscriptions', () => ({
  fetchSubscriptionPlans: (...args: unknown[]) => fetchSubscriptionPlansMock(...args),
}));

vi.mock('@/api/payments', () => ({
  prepareBillingAgreement: (...args: unknown[]) => prepareBillingAgreementMock(...args),
  confirmBillingAgreement: (...args: unknown[]) => confirmBillingAgreementMock(...args),
  fetchPaymentCommandOutcome: (...args: unknown[]) => fetchPaymentCommandOutcomeMock(...args),
  fetchMyBillingAgreement: (...args: unknown[]) => fetchMyBillingAgreementMock(...args),
}));

vi.mock('@/api/userSubscriptions', () => ({
  fetchMySubscription: (...args: unknown[]) => fetchMySubscriptionMock(...args),
}));

function renderPage(
  path = '/subscriptions/checkout?planId=1&userType=INDIVIDUAL&billingCycle=MONTHLY&purpose=SUBSCRIBE',
) {
  return render(
    <MemoryRouter
      initialEntries={[path]}
      future={{ v7_startTransition: true, v7_relativeSplatPath: true }}
    >
      <SubscriptionPaymentPage />
    </MemoryRouter>,
  );
}

function preparedResponse(
  overrides: Record<string, unknown> = {},
  checkoutOverrides: Record<string, unknown> = {},
): Record<string, unknown> {
  return {
    orderId: 'ATS-BILL-1',
    provider: 'TOSS',
    purpose: 'SUBSCRIBE',
    agreementStatus: 'READY',
    subscriptionId: 1,
    billingCycle: 'MONTHLY',
    amount: 9900,
    currency: 'KRW',
    expiresAt: '2026-05-16T23:10:00',
    checkout: {
      type: 'TOSS_BILLING_AUTH',
      clientKey: 'test_ck_billing',
      customerKey: 'ats_billing_customer_1',
      successUrl: 'http://localhost:5173/subscriptions/checkout/success',
      failUrl: 'http://localhost:5173/subscriptions/checkout/fail',
      method: 'CARD',
      ...checkoutOverrides,
    },
    ...overrides,
  };
}

describe('SubscriptionPaymentPage', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
    sessionStorage.clear();
    authState.user.userType = 'INDIVIDUAL';
    navigateMock.mockReset();
    toastShowMock.mockReset();
    fetchSubscriptionPlansMock.mockReset();
    prepareBillingAgreementMock.mockReset();
    confirmBillingAgreementMock.mockReset();
    fetchPaymentCommandOutcomeMock.mockReset();
    fetchMySubscriptionMock.mockReset();
    fetchMyBillingAgreementMock.mockReset();
    requestBillingAuthMock.mockReset();
    requestBillingAuthMock.mockResolvedValue(undefined);
    window.TossPayments = vi.fn(() => ({
      widgets: vi.fn(),
      payment: () => ({
        requestBillingAuth: requestBillingAuthMock,
      }),
    }));

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
    prepareBillingAgreementMock.mockResolvedValue(preparedResponse());
    confirmBillingAgreementMock.mockResolvedValue({
      orderId: 'ATS-BILL-1',
      orderStatus: 'DONE',
      provider: 'TOSS',
      agreementStatus: 'ACTIVE',
      nextBillingAt: '2026-06-16',
      subscription: null,
    });
    fetchPaymentCommandOutcomeMock.mockResolvedValue({
      purpose: 'SUBSCRIBE',
      orderStatus: 'DONE',
      userSubscriptionId: 100,
      targetSubscriptionId: 1,
      targetBillingCycle: 'MONTHLY',
    });
    fetchMySubscriptionMock.mockResolvedValue({
      id: 100,
      subscription: { id: 1, name: 'STANDARD' },
      billingCycle: 'MONTHLY',
      status: 'ACTIVE',
      startedAt: '2026-05-16',
      expiresAt: '2026-06-16',
      pendingSubscriptionId: null,
      pendingBillingCycle: null,
    });
    fetchMyBillingAgreementMock.mockResolvedValue({
      provider: 'TOSS',
      status: 'ACTIVE',
      payMethod: 'CARD',
      maskedMethod: '1234',
      nextBillingAt: '2026-06-16',
      lastChargedAt: '2026-05-16T00:00:00',
      cancelledAt: null,
      subscription: {
        id: 100,
        subscription: { id: 1, name: 'STANDARD' },
        billingCycle: 'MONTHLY',
        status: 'ACTIVE',
      },
    });
  });

  it('prepares recurring billing by default for checkout', async () => {
    renderPage();

    await waitFor(() => {
      expect(prepareBillingAgreementMock).toHaveBeenCalledWith(
        {
          subscriptionId: 1,
          billingCycle: 'MONTHLY',
          purpose: 'SUBSCRIBE',
        },
        expect.any(String),
      );
    });

    expect(screen.getByText('ATS-BILL-1')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: '카드 등록하기' })).toBeEnabled();
  });

  it('reuses one session-scoped attempt key across StrictMode remount and reload', async () => {
    const view = render(
      <StrictMode>
        <MemoryRouter
          initialEntries={[
            '/subscriptions/checkout?planId=1&userType=INDIVIDUAL&billingCycle=MONTHLY&purpose=SUBSCRIBE',
          ]}
          future={{ v7_startTransition: true, v7_relativeSplatPath: true }}
        >
          <SubscriptionPaymentPage />
        </MemoryRouter>
      </StrictMode>,
    );

    await waitFor(() => expect(prepareBillingAgreementMock).toHaveBeenCalled());
    const attemptKey = prepareBillingAgreementMock.mock.calls[0][1];
    expect(prepareBillingAgreementMock.mock.calls.every(([, key]) => key === attemptKey)).toBe(
      true,
    );
    const callsBeforeReload = prepareBillingAgreementMock.mock.calls.length;

    view.unmount();
    renderPage();

    await waitFor(() =>
      expect(prepareBillingAgreementMock.mock.calls.length).toBeGreaterThan(callsBeforeReload),
    );
    const reloadedAttemptKey =
      prepareBillingAgreementMock.mock.calls[
        prepareBillingAgreementMock.mock.calls.length - 1
      ]?.[1];
    expect(reloadedAttemptKey).toBe(attemptKey);
  });

  it('reuses the same attempt key for a network retry', async () => {
    prepareBillingAgreementMock
      .mockRejectedValueOnce({ code: 'ERR_NETWORK' })
      .mockResolvedValueOnce(preparedResponse());

    renderPage();

    await screen.findByText('결제 준비 다시 시도');
    expect(screen.queryByRole('button', { name: '새 결제 시도 시작' })).not.toBeInTheDocument();
    const firstAttemptKey = prepareBillingAgreementMock.mock.calls[0][1];
    fireEvent.click(screen.getByRole('button', { name: '결제 준비 다시 시도' }));

    await waitFor(() => expect(prepareBillingAgreementMock).toHaveBeenCalledTimes(2));
    expect(prepareBillingAgreementMock.mock.calls[1][1]).toBe(firstAttemptKey);
  });

  it.each([
    ['PAYMENT_PREPARE_IDEMPOTENCY_KEY_INVALID', 400],
    ['PAYMENT_ORDER_EXPIRED', 400],
    ['PAYMENT_ORDER_TERMINAL', 409],
  ])('offers explicit replacement for %s only after the user clicks', async (errorCode, status) => {
    vi.spyOn(globalThis.crypto, 'randomUUID')
      .mockReturnValueOnce(firstAttemptKey)
      .mockReturnValueOnce(replacementAttemptKey);
    prepareBillingAgreementMock
      .mockRejectedValueOnce({ response: { status, data: { errorCode } } })
      .mockResolvedValueOnce(preparedResponse());

    renderPage();

    await screen.findByText('새 결제 시도 시작');
    expect(prepareBillingAgreementMock).toHaveBeenCalledTimes(1);
    expect(prepareBillingAgreementMock.mock.calls[0][1]).toBe(firstAttemptKey);
    expect(sessionStorage.getItem(storageKey)).toContain(firstAttemptKey);

    fireEvent.click(screen.getByRole('button', { name: '새 결제 시도 시작' }));

    await waitFor(() => expect(prepareBillingAgreementMock).toHaveBeenCalledTimes(2));
    expect(prepareBillingAgreementMock.mock.calls[1][1]).toBe(replacementAttemptKey);
  });

  it.each([
    [
      'tuple conflict',
      { response: { status: 409, data: { errorCode: 'PAYMENT_PREPARE_ATTEMPT_CONFLICT' } } },
    ],
    [
      'invalid state',
      { response: { status: 400, data: { errorCode: 'PAYMENT_ORDER_INVALID_STATE' } } },
    ],
    ['arbitrary 409', { response: { status: 409, data: { errorCode: 'UNRELATED_CONFLICT' } } }],
    [
      'Provider error',
      { response: { status: 503, data: { errorCode: 'PAYMENT_PROVIDER_UNAVAILABLE' } } },
    ],
    ['network error', { code: 'ERR_NETWORK' }],
  ])('keeps the same attempt and hides replacement for a %s', async (_label, error) => {
    prepareBillingAgreementMock
      .mockRejectedValueOnce(error)
      .mockResolvedValueOnce(preparedResponse());

    renderPage();

    await screen.findByText('결제 준비 다시 시도');
    expect(screen.queryByRole('button', { name: '새 결제 시도 시작' })).not.toBeInTheDocument();
    const originalAttemptKey = prepareBillingAgreementMock.mock.calls[0][1];
    fireEvent.click(screen.getByRole('button', { name: '결제 준비 다시 시도' }));

    await waitFor(() => expect(prepareBillingAgreementMock).toHaveBeenCalledTimes(2));
    expect(prepareBillingAgreementMock.mock.calls[1][1]).toBe(originalAttemptKey);
  });

  it('recovers an uppercase stored UUID only after the explicit click', async () => {
    const uppercaseRecord = JSON.stringify({
      version: 1,
      context: {
        purpose: 'SUBSCRIBE',
        planId: 1,
        userType: 'INDIVIDUAL',
        billingCycle: 'MONTHLY',
      },
      idempotencyKey: 'AAAAAAAA-AAAA-4AAA-8AAA-AAAAAAAAAAAA',
    });
    sessionStorage.setItem(storageKey, uppercaseRecord);
    const randomUUID = vi
      .spyOn(globalThis.crypto, 'randomUUID')
      .mockReturnValue(replacementAttemptKey);

    renderPage();

    await screen.findByText('새 결제 시도 시작');
    expect(screen.queryByRole('button', { name: '결제 준비 다시 시도' })).not.toBeInTheDocument();
    expect(prepareBillingAgreementMock).not.toHaveBeenCalled();
    expect(randomUUID).not.toHaveBeenCalled();
    expect(sessionStorage.getItem(storageKey)).toBe(uppercaseRecord);

    fireEvent.click(screen.getByRole('button', { name: '새 결제 시도 시작' }));

    await waitFor(() => expect(prepareBillingAgreementMock).toHaveBeenCalledTimes(1));
    expect(randomUUID).toHaveBeenCalledTimes(1);
    expect(prepareBillingAgreementMock.mock.calls[0][1]).toBe(replacementAttemptKey);
    expect(sessionStorage.length).toBe(1);
    expect(JSON.parse(sessionStorage.getItem(storageKey) ?? '{}')).toMatchObject({
      version: 1,
      idempotencyKey: replacementAttemptKey,
    });
  });

  it('opens Toss billing auth without one-time payment confirm', async () => {
    renderPage();

    await screen.findByText('ATS-BILL-1');
    fireEvent.click(screen.getByRole('button', { name: '카드 등록하기' }));

    await waitFor(() => {
      expect(requestBillingAuthMock).toHaveBeenCalledWith({
        method: 'CARD',
        successUrl:
          'http://localhost:5173/subscriptions/checkout/success?orderId=ATS-BILL-1&amount=9900',
        failUrl: 'http://localhost:5173/subscriptions/checkout/fail?orderId=ATS-BILL-1&amount=9900',
      });
    });
  });

  it('prepares payment-method re-registration without an immediate charge label', async () => {
    prepareBillingAgreementMock.mockResolvedValue({
      orderId: 'ATS-BILL-REAUTH',
      provider: 'TOSS',
      purpose: 'BILLING_AGREEMENT',
      agreementStatus: 'READY',
      subscriptionId: 1,
      billingCycle: 'MONTHLY',
      amount: 0,
      currency: 'KRW',
      expiresAt: '2026-05-16T23:10:00',
      checkout: {
        type: 'TOSS_BILLING_AUTH',
        clientKey: 'test_ck_billing',
        customerKey: 'ats_billing_customer_1',
        successUrl: 'http://localhost:5173/subscriptions/checkout/success',
        failUrl: 'http://localhost:5173/subscriptions/checkout/fail',
        method: 'CARD',
      },
    });
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

    renderPage(
      '/subscriptions/checkout?planId=1&userType=INDIVIDUAL&billingCycle=MONTHLY&purpose=BILLING_AGREEMENT',
    );

    await waitFor(() => {
      expect(prepareBillingAgreementMock).toHaveBeenCalledWith(
        {
          subscriptionId: 1,
          billingCycle: 'MONTHLY',
          purpose: 'BILLING_AGREEMENT',
        },
        expect.any(String),
      );
    });

    await screen.findByText('결제수단 등록');
    expect(screen.getByText('즉시 결제 없음')).toBeInTheDocument();

    fireEvent.click(screen.getByRole('button', { name: '카드 등록하기' }));

    await waitFor(() => {
      expect(requestBillingAuthMock).toHaveBeenCalledWith({
        method: 'CARD',
        successUrl:
          'http://localhost:5173/subscriptions/checkout/success?orderId=ATS-BILL-REAUTH&amount=0&purpose=BILLING_AGREEMENT',
        failUrl:
          'http://localhost:5173/subscriptions/checkout/fail?orderId=ATS-BILL-REAUTH&amount=0&purpose=BILLING_AGREEMENT',
      });
    });
  });

  it('keeps upgrade context visible while re-registering a payment method', async () => {
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
    prepareBillingAgreementMock.mockResolvedValue({
      orderId: 'ATS-BILL-REAUTH',
      provider: 'TOSS',
      purpose: 'BILLING_AGREEMENT',
      agreementStatus: 'READY',
      subscriptionId: 1,
      billingCycle: 'MONTHLY',
      amount: 0,
      currency: 'KRW',
      expiresAt: '2026-05-16T23:10:00',
      checkout: {
        type: 'TOSS_BILLING_AUTH',
        clientKey: 'test_ck_billing',
        customerKey: 'ats_billing_customer_1',
        successUrl: 'http://localhost:5173/subscriptions/checkout/success',
        failUrl: 'http://localhost:5173/subscriptions/checkout/fail',
        method: 'CARD',
      },
    });

    renderPage(
      '/subscriptions/checkout?planId=1&userType=INDIVIDUAL&billingCycle=MONTHLY&purpose=BILLING_AGREEMENT&returnPlanId=2&returnUserType=INDIVIDUAL&returnBillingCycle=YEARLY&returnAmount=99726',
    );

    await screen.findByText('등록 후 이어갈 플랜 변경');
    expect(screen.getByText('즉시 결제 없음')).toBeInTheDocument();
    expect(screen.getByText('PREMIUM')).toBeInTheDocument();
    expect(screen.getByText('연간')).toBeInTheDocument();
    expect(screen.getByText('₩99,726')).toBeInTheDocument();

    fireEvent.click(screen.getByRole('button', { name: '카드 등록하기' }));

    await waitFor(() => {
      expect(requestBillingAuthMock).toHaveBeenCalledWith({
        method: 'CARD',
        successUrl:
          'http://localhost:5173/subscriptions/checkout/success?orderId=ATS-BILL-REAUTH&amount=0&purpose=BILLING_AGREEMENT&returnPlanId=2&returnUserType=INDIVIDUAL&returnBillingCycle=YEARLY&returnAmount=99726',
        failUrl:
          'http://localhost:5173/subscriptions/checkout/fail?orderId=ATS-BILL-REAUTH&amount=0&purpose=BILLING_AGREEMENT&returnPlanId=2&returnUserType=INDIVIDUAL&returnBillingCycle=YEARLY&returnAmount=99726',
      });
    });
  });

  it.each([
    [
      'missing plan ID',
      '/subscriptions/checkout?userType=INDIVIDUAL&billingCycle=MONTHLY&purpose=SUBSCRIBE',
    ],
    [
      'malformed plan ID',
      '/subscriptions/checkout?planId=not-a-number&userType=INDIVIDUAL&billingCycle=MONTHLY&purpose=SUBSCRIBE',
    ],
    ['missing audience', '/subscriptions/checkout?planId=1&billingCycle=MONTHLY&purpose=SUBSCRIBE'],
    [
      'unsupported audience',
      '/subscriptions/checkout?planId=1&userType=ADMIN&billingCycle=MONTHLY&purpose=SUBSCRIBE',
    ],
    [
      'missing billing cycle',
      '/subscriptions/checkout?planId=1&userType=INDIVIDUAL&purpose=SUBSCRIBE',
    ],
    [
      'unsupported billing cycle',
      '/subscriptions/checkout?planId=1&userType=INDIVIDUAL&billingCycle=WEEKLY&purpose=SUBSCRIBE',
    ],
    [
      'missing purpose',
      '/subscriptions/checkout?planId=1&userType=INDIVIDUAL&billingCycle=MONTHLY',
    ],
    [
      'unsupported purpose',
      '/subscriptions/checkout?planId=1&userType=INDIVIDUAL&billingCycle=MONTHLY&purpose=UPGRADE',
    ],
  ])('rejects a %s route before prepare', async (_label, path) => {
    renderPage(path);

    await waitFor(() => expect(toastShowMock).toHaveBeenCalled());
    expect(fetchSubscriptionPlansMock).not.toHaveBeenCalled();
    expect(prepareBillingAgreementMock).not.toHaveBeenCalled();
    expect(requestBillingAuthMock).not.toHaveBeenCalled();
  });

  it.each([
    ['INDIVIDUAL', 'BUSINESS'],
    ['BUSINESS', 'INDIVIDUAL'],
  ] as const)(
    'rejects authenticated %s against route audience %s before prepare',
    async (authenticatedUserType, routeUserType) => {
      authState.user.userType = authenticatedUserType;

      renderPage(
        `/subscriptions/checkout?planId=1&userType=${routeUserType}&billingCycle=MONTHLY&purpose=SUBSCRIBE`,
      );

      await waitFor(() => expect(toastShowMock).toHaveBeenCalled());
      expect(fetchSubscriptionPlansMock).not.toHaveBeenCalled();
      expect(prepareBillingAgreementMock).not.toHaveBeenCalled();
      expect(requestBillingAuthMock).not.toHaveBeenCalled();
    },
  );

  it('resolves an exact plan ID instead of a duplicate cross-audience name', async () => {
    fetchSubscriptionPlansMock.mockResolvedValue([
      {
        id: 2,
        name: 'STANDARD',
        description: 'Business duplicate',
        userType: 'BUSINESS',
        priceMonthly: 19900,
        priceYearly: 199000,
        downloadPerDay: 10,
        maxWhitelistChannels: 2,
        maxPlaylists: 5,
        isActive: true,
      },
      {
        id: 1,
        name: 'STANDARD',
        description: 'Individual exact plan',
        userType: 'INDIVIDUAL',
        priceMonthly: 9900,
        priceYearly: 99000,
        downloadPerDay: 5,
        maxWhitelistChannels: 1,
        maxPlaylists: 3,
        isActive: true,
      },
    ]);

    renderPage();

    await waitFor(() => {
      expect(fetchSubscriptionPlansMock).toHaveBeenCalledWith('INDIVIDUAL');
      expect(prepareBillingAgreementMock).toHaveBeenCalledWith(
        {
          subscriptionId: 1,
          billingCycle: 'MONTHLY',
          purpose: 'SUBSCRIBE',
        },
        expect.any(String),
      );
    });
  });

  it('rejects a missing exact plan ID before prepare', async () => {
    renderPage(
      '/subscriptions/checkout?planId=99&userType=INDIVIDUAL&billingCycle=MONTHLY&purpose=SUBSCRIBE',
    );

    await waitFor(() => expect(toastShowMock).toHaveBeenCalled());
    expect(fetchSubscriptionPlansMock).toHaveBeenCalledWith('INDIVIDUAL');
    expect(prepareBillingAgreementMock).not.toHaveBeenCalled();
    expect(requestBillingAuthMock).not.toHaveBeenCalled();
  });

  it('rejects an exact plan whose audience disagrees with the route', async () => {
    fetchSubscriptionPlansMock.mockResolvedValue([
      {
        id: 1,
        name: 'STANDARD',
        description: 'Business duplicate',
        userType: 'BUSINESS',
        priceMonthly: 19900,
        priceYearly: 199000,
        downloadPerDay: 10,
        maxWhitelistChannels: 2,
        maxPlaylists: 5,
        isActive: true,
      },
    ]);

    renderPage();

    await waitFor(() => expect(toastShowMock).toHaveBeenCalled());
    expect(prepareBillingAgreementMock).not.toHaveBeenCalled();
    expect(requestBillingAuthMock).not.toHaveBeenCalled();
  });

  it('rejects an invalid immutable return identity before prepare', async () => {
    renderPage(
      '/subscriptions/checkout?planId=1&userType=INDIVIDUAL&billingCycle=MONTHLY&purpose=BILLING_AGREEMENT&returnPlanId=99&returnUserType=INDIVIDUAL&returnBillingCycle=YEARLY',
    );

    await waitFor(() => expect(toastShowMock).toHaveBeenCalled());
    expect(prepareBillingAgreementMock).not.toHaveBeenCalled();
    expect(requestBillingAuthMock).not.toHaveBeenCalled();
  });

  it.each([
    ['blank order ID', () => preparedResponse({ orderId: ' ' })],
    ['plan ID', () => preparedResponse({ subscriptionId: 2 })],
    ['billing cycle', () => preparedResponse({ billingCycle: 'YEARLY' })],
    ['purpose', () => preparedResponse({ purpose: 'BILLING_AGREEMENT' })],
    ['amount', () => preparedResponse({ amount: 0 })],
    ['non-number amount', () => preparedResponse({ amount: '9900' })],
    ['fractional amount', () => preparedResponse({ amount: 9900.5 })],
    ['unsafe amount', () => preparedResponse({ amount: Number.MAX_SAFE_INTEGER + 1 })],
    ['negative amount', () => preparedResponse({ amount: -9900 })],
    ['provider', () => preparedResponse({ provider: 'OTHER' })],
    ['currency', () => preparedResponse({ currency: 'USD' })],
    [
      'missing expiry',
      () => {
        const response = preparedResponse();
        delete response.expiresAt;
        return response;
      },
    ],
    ['blank expiry', () => preparedResponse({ expiresAt: ' ' })],
    ['malformed expiry', () => preparedResponse({ expiresAt: 'not-a-timestamp' })],
    ['checkout type', () => preparedResponse({}, { type: 'OTHER' })],
    ['checkout method', () => preparedResponse({}, { method: 'TRANSFER' })],
    ['client key', () => preparedResponse({}, { clientKey: ' ' })],
    ['customer key', () => preparedResponse({}, { customerKey: '' })],
    ['success URL', () => preparedResponse({}, { successUrl: '' })],
    ['fail URL', () => preparedResponse({}, { failUrl: '' })],
    [
      'non-http success URL',
      () => preparedResponse({}, { successUrl: 'ftp://callback.test/success' }),
    ],
    ['invalid success URL', () => preparedResponse({}, { successUrl: 'not an absolute URL' })],
    ['non-http fail URL', () => preparedResponse({}, { failUrl: 'javascript:alert(1)' })],
    ['invalid fail URL', () => preparedResponse({}, { failUrl: '://invalid' })],
  ])('disables billing auth when the prepare response has invalid %s', async (_label, response) => {
    prepareBillingAgreementMock.mockResolvedValue(response());

    renderPage();

    await waitFor(() => expect(toastShowMock).toHaveBeenCalled());
    const buttons = screen.getAllByRole('button');
    const billingAuthButton = buttons[buttons.length - 1];
    expect(billingAuthButton).toBeDisabled();
    fireEvent.click(billingAuthButton);
    expect(window.TossPayments).not.toHaveBeenCalled();
    expect(requestBillingAuthMock).not.toHaveBeenCalled();
  });

  it('rejects a nonzero billing-agreement response amount before SDK auth', async () => {
    prepareBillingAgreementMock.mockResolvedValue(
      preparedResponse({
        orderId: 'ATS-BILL-REAUTH',
        purpose: 'BILLING_AGREEMENT',
        amount: 1,
      }),
    );

    renderPage(
      '/subscriptions/checkout?planId=1&userType=INDIVIDUAL&billingCycle=MONTHLY&purpose=BILLING_AGREEMENT',
    );

    await waitFor(() => expect(toastShowMock).toHaveBeenCalled());
    const buttons = screen.getAllByRole('button');
    expect(buttons[buttons.length - 1]).toBeDisabled();
    expect(requestBillingAuthMock).not.toHaveBeenCalled();
  });

  it('does not prepare checkout for upgrade route', async () => {
    renderPage(
      '/subscriptions/checkout?planId=1&userType=INDIVIDUAL&billingCycle=MONTHLY&purpose=UPGRADE',
    );

    await screen.findByText('플랜 변경은 내 구독 화면에서 변경 내역을 확인한 뒤 진행해주세요.');
    expect(prepareBillingAgreementMock).not.toHaveBeenCalled();
  });

  it('confirms Toss billing success redirect with authKey', async () => {
    renderPage(
      '/subscriptions/checkout/success?authKey=auth-key&customerKey=ats_billing_customer_1&orderId=ATS-BILL-1&amount=9900',
    );

    await waitFor(() => {
      expect(confirmBillingAgreementMock).toHaveBeenCalledWith({
        orderId: 'ATS-BILL-1',
        authKey: 'auth-key',
        customerKey: 'ats_billing_customer_1',
        amount: 9900,
      });
    });
    expect(navigateMock).toHaveBeenCalledWith('/subscriptions/manage', { replace: true });
    expect(confirmBillingAgreementMock).toHaveBeenCalledTimes(1);
    expect(fetchMySubscriptionMock).toHaveBeenCalledTimes(1);
    expect(fetchMyBillingAgreementMock).toHaveBeenCalledTimes(1);
  });

  it('keeps a successful confirm as RELOAD_FAILED until canonical reads succeed', async () => {
    fetchMySubscriptionMock.mockRejectedValueOnce(new Error('reload unavailable'));

    renderPage(
      '/subscriptions/checkout/success?authKey=auth-key&customerKey=ats_billing_customer_1&orderId=ATS-BILL-1&amount=9900',
    );

    expect(
      await screen.findByText(
        '\uC694\uCCAD\uC740 \uC644\uB8CC\uB418\uC5C8\uC9C0\uB9CC \uCD5C\uC2E0 \uAD6C\uB3C5 \uC815\uBCF4\uB97C \uBD88\uB7EC\uC624\uC9C0 \uBABB\uD588\uC2B5\uB2C8\uB2E4.',
      ),
    ).toBeInTheDocument();
    expect(navigateMock).not.toHaveBeenCalledWith('/subscriptions/manage', { replace: true });
    expect(confirmBillingAgreementMock).toHaveBeenCalledTimes(1);

    fetchMySubscriptionMock.mockResolvedValueOnce({
      id: 100,
      subscription: { id: 1, name: 'STANDARD' },
      billingCycle: 'MONTHLY',
      status: 'ACTIVE',
    });
    fireEvent.click(screen.getByRole('button', { name: '\uC0C1\uD0DC \uB2E4\uC2DC \uD655\uC778' }));

    await waitFor(() => {
      expect(navigateMock).toHaveBeenCalledWith('/subscriptions/manage', { replace: true });
    });
    expect(confirmBillingAgreementMock).toHaveBeenCalledTimes(1);
  });

  it('recovers a lost confirm response through DONE outcome and canonical reads only', async () => {
    confirmBillingAgreementMock.mockRejectedValueOnce(new Error('response lost'));

    renderPage(
      '/subscriptions/checkout/success?authKey=auth-key&customerKey=ats_billing_customer_1&orderId=ATS-BILL-1&amount=9900',
    );

    await waitFor(() => {
      expect(navigateMock).toHaveBeenCalledWith('/subscriptions/manage', { replace: true });
    });
    expect(confirmBillingAgreementMock).toHaveBeenCalledTimes(1);
    expect(fetchPaymentCommandOutcomeMock).toHaveBeenCalledTimes(1);
    expect(fetchMySubscriptionMock).toHaveBeenCalledTimes(1);
    expect(fetchMyBillingAgreementMock).toHaveBeenCalledTimes(1);
  });

  it('recovers a refreshed sanitized callback through reads without secret query keys', async () => {
    renderPage('/subscriptions/checkout/success?orderId=ATS-BILL-1&amount=9900');

    await waitFor(() => {
      expect(navigateMock).toHaveBeenCalledWith('/subscriptions/manage', { replace: true });
    });
    expect(confirmBillingAgreementMock).not.toHaveBeenCalled();
    expect(fetchPaymentCommandOutcomeMock).toHaveBeenCalledTimes(1);
    expect(fetchMySubscriptionMock).toHaveBeenCalledTimes(1);
    expect(fetchMyBillingAgreementMock).toHaveBeenCalledTimes(1);
  });

  it.each(['FAILED', 'CANCELLED', 'EXPIRED'] as const)(
    'proves a fail callback as FAILED only from terminal %s outcome',
    async (orderStatus) => {
      fetchPaymentCommandOutcomeMock.mockResolvedValueOnce({
        purpose: 'SUBSCRIBE',
        orderStatus,
        userSubscriptionId: null,
        targetSubscriptionId: 1,
        targetBillingCycle: 'MONTHLY',
      });

      renderPage(
        '/subscriptions/checkout/fail?authKey=secret-auth&customerKey=secret-customer&orderId=ATS-BILL-1&amount=9900&message=untrusted-message',
      );

      expect(
        await screen.findByText(
          '\uC694\uCCAD\uC774 \uC644\uB8CC\uB418\uC9C0 \uC54A\uC558\uC2B5\uB2C8\uB2E4.',
        ),
      ).toBeInTheDocument();
      expect(screen.queryByText('untrusted-message')).not.toBeInTheDocument();
      expect(fetchPaymentCommandOutcomeMock).toHaveBeenCalledWith('ATS-BILL-1');
      expect(confirmBillingAgreementMock).not.toHaveBeenCalled();
      expect(fetchMySubscriptionMock).not.toHaveBeenCalled();
      expect(fetchMyBillingAgreementMock).not.toHaveBeenCalled();
      expect(navigateMock).toHaveBeenCalledWith(expect.not.stringContaining('authKey'), {
        replace: true,
      });
      expect(navigateMock).toHaveBeenCalledWith(expect.not.stringContaining('customerKey'), {
        replace: true,
      });
    },
  );

  it('keeps a fail callback UNKNOWN for a nonterminal outcome', async () => {
    fetchPaymentCommandOutcomeMock.mockResolvedValueOnce({
      purpose: 'SUBSCRIBE',
      orderStatus: 'PROCESSING',
      userSubscriptionId: null,
      targetSubscriptionId: 1,
      targetBillingCycle: 'MONTHLY',
    });

    renderPage('/subscriptions/checkout/fail?orderId=ATS-BILL-1&message=untrusted-message');

    expect(
      await screen.findByText(
        '\uCC98\uB9AC\uAC00 \uC774\uBBF8 \uC644\uB8CC\uB418\uC5C8\uC744 \uC218 \uC788\uC2B5\uB2C8\uB2E4. \uC791\uC5C5\uC744 \uB2E4\uC2DC \uC2E4\uD589\uD558\uC9C0 \uB9D0\uACE0 \uC0C1\uD0DC\uB97C \uB2E4\uC2DC \uD655\uC778\uD574\uC8FC\uC138\uC694.',
      ),
    ).toBeInTheDocument();
    expect(screen.queryByText('untrusted-message')).not.toBeInTheDocument();
    expect(confirmBillingAgreementMock).not.toHaveBeenCalled();
    expect(fetchMySubscriptionMock).not.toHaveBeenCalled();
    expect(fetchMyBillingAgreementMock).not.toHaveBeenCalled();
  });

  it('commits a fail callback from DONE outcome and matching canonical reads without mutation', async () => {
    fetchPaymentCommandOutcomeMock.mockResolvedValueOnce({
      purpose: 'SUBSCRIBE',
      orderStatus: 'DONE',
      userSubscriptionId: 100,
      targetSubscriptionId: 1,
      targetBillingCycle: 'MONTHLY',
    });

    renderPage('/subscriptions/checkout/fail?orderId=ATS-BILL-1&message=untrusted-message');

    await waitFor(() => {
      expect(navigateMock).toHaveBeenCalledWith('/subscriptions/manage', { replace: true });
    });
    expect(fetchPaymentCommandOutcomeMock).toHaveBeenCalledWith('ATS-BILL-1');
    expect(fetchMySubscriptionMock).toHaveBeenCalledTimes(1);
    expect(fetchMyBillingAgreementMock).toHaveBeenCalledTimes(1);
    expect(confirmBillingAgreementMock).not.toHaveBeenCalled();
  });

  it.each([null, 999])(
    'keeps a DONE callback UNKNOWN when outcome aggregate ID is %s',
    async (userSubscriptionId) => {
      fetchPaymentCommandOutcomeMock.mockResolvedValueOnce({
        purpose: 'SUBSCRIBE',
        orderStatus: 'DONE',
        userSubscriptionId,
        targetSubscriptionId: 1,
        targetBillingCycle: 'MONTHLY',
      });

      renderPage('/subscriptions/checkout/fail?orderId=ATS-BILL-1');

      expect(
        await screen.findByText(
          '\uCC98\uB9AC\uAC00 \uC774\uBBF8 \uC644\uB8CC\uB418\uC5C8\uC744 \uC218 \uC788\uC2B5\uB2C8\uB2E4. \uC791\uC5C5\uC744 \uB2E4\uC2DC \uC2E4\uD589\uD558\uC9C0 \uB9D0\uACE0 \uC0C1\uD0DC\uB97C \uB2E4\uC2DC \uD655\uC778\uD574\uC8FC\uC138\uC694.',
        ),
      ).toBeInTheDocument();
      expect(navigateMock).not.toHaveBeenCalledWith('/subscriptions/manage', { replace: true });
      expect(confirmBillingAgreementMock).not.toHaveBeenCalled();
    },
  );

  it('keeps a DONE callback UNKNOWN when the agreement omits subscription identity', async () => {
    fetchMyBillingAgreementMock.mockResolvedValueOnce({
      provider: 'TOSS',
      status: 'ACTIVE',
      payMethod: 'CARD',
      maskedMethod: '1234',
      nextBillingAt: '2026-06-16',
      lastChargedAt: '2026-05-16T00:00:00',
      cancelledAt: null,
      subscription: null,
    });

    renderPage('/subscriptions/checkout/fail?orderId=ATS-BILL-1');

    expect(
      await screen.findByText(
        '\uCC98\uB9AC\uAC00 \uC774\uBBF8 \uC644\uB8CC\uB418\uC5C8\uC744 \uC218 \uC788\uC2B5\uB2C8\uB2E4. \uC791\uC5C5\uC744 \uB2E4\uC2DC \uC2E4\uD589\uD558\uC9C0 \uB9D0\uACE0 \uC0C1\uD0DC\uB97C \uB2E4\uC2DC \uD655\uC778\uD574\uC8FC\uC138\uC694.',
      ),
    ).toBeInTheDocument();
    expect(navigateMock).not.toHaveBeenCalledWith('/subscriptions/manage', { replace: true });
    expect(confirmBillingAgreementMock).not.toHaveBeenCalled();
  });

  it('keeps a failed outcome read UNKNOWN and retries reads without confirming', async () => {
    fetchPaymentCommandOutcomeMock
      .mockRejectedValueOnce(new Error('outcome unavailable'))
      .mockResolvedValueOnce({
        purpose: 'SUBSCRIBE',
        orderStatus: 'PROCESSING',
        userSubscriptionId: null,
        targetSubscriptionId: 1,
        targetBillingCycle: 'MONTHLY',
      });

    renderPage('/subscriptions/checkout/fail?orderId=ATS-BILL-1&message=untrusted-message');

    const retryButton = await screen.findByRole('button', {
      name: '\uC0C1\uD0DC \uB2E4\uC2DC \uD655\uC778',
    });
    fireEvent.click(retryButton);

    await waitFor(() => expect(fetchPaymentCommandOutcomeMock).toHaveBeenCalledTimes(2));
    expect(confirmBillingAgreementMock).not.toHaveBeenCalled();
    expect(fetchMySubscriptionMock).not.toHaveBeenCalled();
    expect(fetchMyBillingAgreementMock).not.toHaveBeenCalled();
  });

  it('keeps a callback UNKNOWN when the agreement points to another subscription intent', async () => {
    fetchMyBillingAgreementMock.mockResolvedValueOnce({
      provider: 'TOSS',
      status: 'ACTIVE',
      payMethod: 'CARD',
      maskedMethod: '1234',
      nextBillingAt: '2026-06-16',
      lastChargedAt: '2026-05-16T00:00:00',
      cancelledAt: null,
      subscription: {
        id: 999,
        subscription: { id: 2, name: 'DELUXE' },
        billingCycle: 'YEARLY',
        status: 'ACTIVE',
      },
    });

    renderPage('/subscriptions/checkout/success?orderId=ATS-BILL-1&amount=9900');

    expect(
      await screen.findByText(
        '\uCC98\uB9AC\uAC00 \uC774\uBBF8 \uC644\uB8CC\uB418\uC5C8\uC744 \uC218 \uC788\uC2B5\uB2C8\uB2E4. \uC791\uC5C5\uC744 \uB2E4\uC2DC \uC2E4\uD589\uD558\uC9C0 \uB9D0\uACE0 \uC0C1\uD0DC\uB97C \uB2E4\uC2DC \uD655\uC778\uD574\uC8FC\uC138\uC694.',
      ),
    ).toBeInTheDocument();
    expect(navigateMock).not.toHaveBeenCalledWith('/subscriptions/manage', { replace: true });
    expect(confirmBillingAgreementMock).not.toHaveBeenCalled();
  });

  it('shows UNKNOWN for an in-flight command and retries reads without confirming again', async () => {
    confirmBillingAgreementMock.mockRejectedValueOnce(new Error('response lost'));
    let resolveRetryOutcome!: (value: Record<string, unknown>) => void;
    const retryOutcome = new Promise<Record<string, unknown>>((resolve) => {
      resolveRetryOutcome = resolve;
    });
    fetchPaymentCommandOutcomeMock.mockResolvedValueOnce({
      purpose: 'SUBSCRIBE',
      orderStatus: 'PROCESSING',
      userSubscriptionId: null,
      targetSubscriptionId: 1,
      targetBillingCycle: 'MONTHLY',
    });
    fetchPaymentCommandOutcomeMock.mockReturnValueOnce(retryOutcome);

    renderPage(
      '/subscriptions/checkout/success?authKey=auth-key&customerKey=ats_billing_customer_1&orderId=ATS-BILL-1&amount=9900',
    );

    expect(
      await screen.findByText(
        '\uCC98\uB9AC\uAC00 \uC774\uBBF8 \uC644\uB8CC\uB418\uC5C8\uC744 \uC218 \uC788\uC2B5\uB2C8\uB2E4. \uC791\uC5C5\uC744 \uB2E4\uC2DC \uC2E4\uD589\uD558\uC9C0 \uB9D0\uACE0 \uC0C1\uD0DC\uB97C \uB2E4\uC2DC \uD655\uC778\uD574\uC8FC\uC138\uC694.',
      ),
    ).toBeInTheDocument();
    const retryButton = screen.getByRole('button', {
      name: '\uC0C1\uD0DC \uB2E4\uC2DC \uD655\uC778',
    });
    fireEvent.click(retryButton);
    fireEvent.click(retryButton);
    expect(fetchPaymentCommandOutcomeMock).toHaveBeenCalledTimes(2);
    resolveRetryOutcome({
      purpose: 'SUBSCRIBE',
      orderStatus: 'DONE',
      userSubscriptionId: 100,
      targetSubscriptionId: 1,
      targetBillingCycle: 'MONTHLY',
    });

    await waitFor(() => {
      expect(navigateMock).toHaveBeenCalledWith('/subscriptions/manage', { replace: true });
    });
    expect(confirmBillingAgreementMock).toHaveBeenCalledTimes(1);
    expect(fetchPaymentCommandOutcomeMock).toHaveBeenCalledTimes(2);
  });

  it('returns to the selected upgrade preview after payment-method registration succeeds', async () => {
    fetchPaymentCommandOutcomeMock.mockResolvedValueOnce({
      purpose: 'BILLING_AGREEMENT',
      orderStatus: 'DONE',
      userSubscriptionId: 100,
      targetSubscriptionId: 1,
      targetBillingCycle: 'MONTHLY',
    });
    renderPage(
      '/subscriptions/checkout/success?authKey=auth-key&customerKey=ats_billing_customer_1&orderId=ATS-BILL-1&amount=0&purpose=BILLING_AGREEMENT&returnPlanId=2&returnUserType=INDIVIDUAL&returnBillingCycle=YEARLY&returnAmount=99726',
    );

    await waitFor(() => {
      expect(confirmBillingAgreementMock).toHaveBeenCalledWith({
        orderId: 'ATS-BILL-1',
        authKey: 'auth-key',
        customerKey: 'ats_billing_customer_1',
        amount: 0,
      });
    });
    expect(navigateMock).toHaveBeenCalledWith(
      '/subscriptions/manage?planId=2&userType=INDIVIDUAL&billingCycle=YEARLY',
      { replace: true },
    );
  });

  it.each([
    ['missing', ''],
    ['empty', '&amount='],
    ['negative', '&amount=-1'],
    ['fractional', '&amount=1.5'],
  ])('rejects a %s callback amount before confirmation', async (_label, amountQuery) => {
    renderPage(
      `/subscriptions/checkout/success?authKey=auth-key&customerKey=customer&orderId=order${amountQuery}`,
    );

    expect(await screen.findByText('자동결제 인증 정보가 올바르지 않습니다.')).toBeInTheDocument();
    expect(confirmBillingAgreementMock).not.toHaveBeenCalled();
  });
});

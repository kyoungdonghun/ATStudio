import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import SubscriptionPaymentPage from '@/pages/subscriber/SubscriptionPaymentPage';

const navigateMock = vi.hoisted(() => vi.fn());
const toastShowMock = vi.hoisted(() => vi.fn());
const fetchSubscriptionPlansMock = vi.hoisted(() => vi.fn());
const prepareBillingAgreementMock = vi.hoisted(() => vi.fn());
const confirmBillingAgreementMock = vi.hoisted(() => vi.fn());
const requestBillingAuthMock = vi.hoisted(() => vi.fn());

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

vi.mock('@/api/subscriptions', () => ({
  fetchSubscriptionPlans: (...args: unknown[]) => fetchSubscriptionPlansMock(...args),
}));

vi.mock('@/api/payments', () => ({
  prepareBillingAgreement: (...args: unknown[]) => prepareBillingAgreementMock(...args),
  confirmBillingAgreement: (...args: unknown[]) => confirmBillingAgreementMock(...args),
}));

function renderPage(path = '/subscriptions/checkout?plan=STANDARD&cycle=MONTHLY') {
  return render(
    <MemoryRouter initialEntries={[path]}>
      <SubscriptionPaymentPage />
    </MemoryRouter>,
  );
}

describe('SubscriptionPaymentPage', () => {
  beforeEach(() => {
    navigateMock.mockReset();
    toastShowMock.mockReset();
    fetchSubscriptionPlansMock.mockReset();
    prepareBillingAgreementMock.mockReset();
    confirmBillingAgreementMock.mockReset();
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
    prepareBillingAgreementMock.mockResolvedValue({
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
      },
    });
    confirmBillingAgreementMock.mockResolvedValue({
      orderId: 'ATS-BILL-1',
      orderStatus: 'DONE',
      provider: 'TOSS',
      agreementStatus: 'ACTIVE',
      nextBillingAt: '2026-06-16',
      subscription: null,
    });
  });

  it('prepares recurring billing by default for checkout', async () => {
    renderPage();

    await waitFor(() => {
      expect(prepareBillingAgreementMock).toHaveBeenCalledWith({
        subscriptionId: 1,
        billingCycle: 'MONTHLY',
      });
    });

    expect(screen.getByText('ATS-BILL-1')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: '카드 등록하기' })).toBeEnabled();
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

    renderPage('/subscriptions/checkout?plan=STANDARD&cycle=MONTHLY&purpose=BILLING_AGREEMENT');

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
      '/subscriptions/checkout?plan=STANDARD&cycle=MONTHLY&purpose=BILLING_AGREEMENT&returnPlan=PREMIUM&returnCycle=YEARLY&returnAmount=99726',
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
          'http://localhost:5173/subscriptions/checkout/success?orderId=ATS-BILL-REAUTH&amount=0&purpose=BILLING_AGREEMENT&returnPlan=PREMIUM&returnCycle=YEARLY&returnAmount=99726',
        failUrl:
          'http://localhost:5173/subscriptions/checkout/fail?orderId=ATS-BILL-REAUTH&amount=0&purpose=BILLING_AGREEMENT&returnPlan=PREMIUM&returnCycle=YEARLY&returnAmount=99726',
      });
    });
  });

  it('does not prepare checkout for upgrade route', async () => {
    renderPage('/subscriptions/checkout?plan=STANDARD&cycle=MONTHLY&purpose=UPGRADE');

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
  });

  it('returns to the selected upgrade preview after payment-method registration succeeds', async () => {
    renderPage(
      '/subscriptions/checkout/success?authKey=auth-key&customerKey=ats_billing_customer_1&orderId=ATS-BILL-1&amount=0&purpose=BILLING_AGREEMENT&returnPlan=PREMIUM&returnCycle=YEARLY&returnAmount=99726',
    );

    await waitFor(() => {
      expect(confirmBillingAgreementMock).toHaveBeenCalledWith({
        orderId: 'ATS-BILL-1',
        authKey: 'auth-key',
        customerKey: 'ats_billing_customer_1',
        amount: 0,
      });
    });
    expect(navigateMock).toHaveBeenCalledWith('/subscriptions/manage?plan=PREMIUM&cycle=YEARLY', {
      replace: true,
    });
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

import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import SubscriptionPaymentPage from '@/pages/subscriber/SubscriptionPaymentPage';

const navigateMock = vi.hoisted(() => vi.fn());
const toastShowMock = vi.hoisted(() => vi.fn());
const fetchSubscriptionPlansMock = vi.hoisted(() => vi.fn());
const prepareSubscriptionPaymentMock = vi.hoisted(() => vi.fn());
const prepareBillingAgreementMock = vi.hoisted(() => vi.fn());
const confirmPaymentMock = vi.hoisted(() => vi.fn());
const confirmBillingAgreementMock = vi.hoisted(() => vi.fn());
const cancelPaymentMock = vi.hoisted(() => vi.fn());
const requestPaymentMock = vi.hoisted(() => vi.fn());
const requestBillingAuthMock = vi.hoisted(() => vi.fn());
const renderPaymentMethodsMock = vi.hoisted(() => vi.fn());
const renderAgreementMock = vi.hoisted(() => vi.fn());
const setAmountMock = vi.hoisted(() => vi.fn());

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
  prepareSubscriptionPayment: (...args: unknown[]) => prepareSubscriptionPaymentMock(...args),
  prepareBillingAgreement: (...args: unknown[]) => prepareBillingAgreementMock(...args),
  confirmPayment: (...args: unknown[]) => confirmPaymentMock(...args),
  confirmBillingAgreement: (...args: unknown[]) => confirmBillingAgreementMock(...args),
  cancelPayment: (...args: unknown[]) => cancelPaymentMock(...args),
}));

function renderPage() {
  return render(
    <MemoryRouter initialEntries={['/subscriptions/payment?plan=STANDARD&cycle=MONTHLY']}>
      <SubscriptionPaymentPage />
    </MemoryRouter>,
  );
}

describe('SubscriptionPaymentPage', () => {
  beforeEach(() => {
    navigateMock.mockReset();
    toastShowMock.mockReset();
    fetchSubscriptionPlansMock.mockReset();
    prepareSubscriptionPaymentMock.mockReset();
    prepareBillingAgreementMock.mockReset();
    confirmPaymentMock.mockReset();
    confirmBillingAgreementMock.mockReset();
    cancelPaymentMock.mockReset();
    requestPaymentMock.mockReset();
    requestBillingAuthMock.mockReset();
    renderPaymentMethodsMock.mockReset();
    renderAgreementMock.mockReset();
    setAmountMock.mockReset();
    renderPaymentMethodsMock.mockResolvedValue(undefined);
    renderAgreementMock.mockResolvedValue(undefined);
    setAmountMock.mockResolvedValue(undefined);
    requestPaymentMock.mockResolvedValue(undefined);
    requestBillingAuthMock.mockResolvedValue(undefined);
    window.TossPayments = vi.fn(() => ({
      widgets: () => ({
        setAmount: setAmountMock,
        renderPaymentMethods: renderPaymentMethodsMock,
        renderAgreement: renderAgreementMock,
        requestPayment: requestPaymentMock,
      }),
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
    prepareSubscriptionPaymentMock.mockResolvedValue({
      orderId: 'ATS-ORDER-1',
      provider: 'MOCK',
      purpose: 'SUBSCRIBE',
      amount: 9900,
      currency: 'KRW',
      expiresAt: '2026-05-16T23:10:00',
      checkout: {
        type: 'MOCK',
        confirmToken: 'mock-ATS-ORDER-1',
      },
    });
    prepareBillingAgreementMock.mockResolvedValue({
      orderId: 'ATS-BILL-1',
      provider: 'TOSS_BILLING',
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
        successUrl: 'http://localhost:5173/subscriptions/billing/success',
        failUrl: 'http://localhost:5173/subscriptions/billing/fail',
        method: 'CARD',
      },
    });
    confirmPaymentMock.mockResolvedValue({
      orderId: 'ATS-ORDER-1',
      status: 'DONE',
      purpose: 'SUBSCRIBE',
      subscription: null,
    });
    confirmBillingAgreementMock.mockResolvedValue({
      orderId: 'ATS-BILL-1',
      orderStatus: 'DONE',
      provider: 'TOSS_BILLING',
      agreementStatus: 'ACTIVE',
      nextBillingAt: '2026-06-16',
      subscription: null,
    });
    cancelPaymentMock.mockResolvedValue({
      orderId: 'ATS-ORDER-1',
      status: 'FAILED',
      purpose: 'SUBSCRIBE',
    });
  });

  it('prepares a mock payment order instead of directly subscribing', async () => {
    renderPage();

    await waitFor(() => {
      expect(prepareSubscriptionPaymentMock).toHaveBeenCalledWith({
        purpose: 'SUBSCRIBE',
        subscriptionId: 1,
        billingCycle: 'MONTHLY',
      });
    });

    expect(screen.getByText('ATS-ORDER-1')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: '결제 확인' })).toBeEnabled();
  });

  it('confirms the prepared mock payment and navigates to subscription manage page', async () => {
    renderPage();

    await screen.findByText('ATS-ORDER-1');
    fireEvent.click(screen.getByRole('button', { name: '결제 확인' }));

    await waitFor(() => {
      expect(confirmPaymentMock).toHaveBeenCalledWith({
        orderId: 'ATS-ORDER-1',
        amount: 9900,
        provider: 'MOCK',
        providerToken: 'mock-ATS-ORDER-1',
      });
    });
    expect(navigateMock).toHaveBeenCalledWith('/subscriptions/manage');
  });

  it('marks the prepared mock payment as failed without navigating', async () => {
    renderPage();

    await screen.findByText('ATS-ORDER-1');
    fireEvent.click(screen.getByRole('button', { name: '실패' }));

    await waitFor(() => {
      expect(cancelPaymentMock).toHaveBeenCalledWith({
        orderId: 'ATS-ORDER-1',
        status: 'FAILED',
        reason: 'Mock payment failure',
      });
    });
    expect(navigateMock).not.toHaveBeenCalled();
  });

  it('renders Toss widget checkout and requests payment without local confirm first', async () => {
    prepareSubscriptionPaymentMock.mockResolvedValue({
      orderId: 'ATS-TOSS-1',
      provider: 'TOSS',
      purpose: 'SUBSCRIBE',
      amount: 9900,
      currency: 'KRW',
      expiresAt: '2026-05-16T23:10:00',
      checkout: {
        type: 'TOSS_WIDGET',
        clientKey: 'test_ck_sample',
        customerKey: 'ats_user_1',
        orderName: 'ATStudio STANDARD Subscription',
        successUrl: 'http://localhost:5173/subscriptions/payment/success',
        failUrl: 'http://localhost:5173/subscriptions/payment/fail',
      },
    });

    renderPage();

    await screen.findByText('ATS-TOSS-1');
    await waitFor(() => {
      expect(setAmountMock).toHaveBeenCalledWith({ value: 9900, currency: 'KRW' });
    });
    fireEvent.click(screen.getByRole('button', { name: '토스 결제창 열기' }));

    await waitFor(() => {
      expect(requestPaymentMock).toHaveBeenCalledWith({
        orderId: 'ATS-TOSS-1',
        orderName: 'ATStudio STANDARD Subscription',
        successUrl: 'http://localhost:5173/subscriptions/payment/success',
        failUrl: 'http://localhost:5173/subscriptions/payment/fail',
      });
    });
    expect(confirmPaymentMock).not.toHaveBeenCalled();
  });

  it('does not prepare one-time checkout for upgrade route', async () => {
    render(
      <MemoryRouter
        initialEntries={['/subscriptions/payment?plan=STANDARD&cycle=MONTHLY&purpose=UPGRADE']}
      >
        <SubscriptionPaymentPage />
      </MemoryRouter>,
    );

    await screen.findByText('플랜 변경은 내 구독 화면에서 변경 내역을 확인한 뒤 진행해주세요.');
    expect(prepareSubscriptionPaymentMock).not.toHaveBeenCalled();
    expect(prepareBillingAgreementMock).not.toHaveBeenCalled();
  });

  it('confirms Toss success redirect with paymentKey', async () => {
    render(
      <MemoryRouter
        initialEntries={[
          '/subscriptions/payment/success?paymentKey=toss-key&orderId=ATS-TOSS-1&amount=9900',
        ]}
      >
        <SubscriptionPaymentPage />
      </MemoryRouter>,
    );

    await waitFor(() => {
      expect(confirmPaymentMock).toHaveBeenCalledWith({
        orderId: 'ATS-TOSS-1',
        amount: 9900,
        provider: 'TOSS',
        paymentKey: 'toss-key',
      });
    });
    expect(navigateMock).toHaveBeenCalledWith('/subscriptions/manage');
  });

  it('prepares recurring billing and opens Toss billing auth', async () => {
    render(
      <MemoryRouter
        initialEntries={['/subscriptions/payment?plan=STANDARD&cycle=MONTHLY&mode=recurring']}
      >
        <SubscriptionPaymentPage />
      </MemoryRouter>,
    );

    await waitFor(() => {
      expect(prepareBillingAgreementMock).toHaveBeenCalledWith({
        subscriptionId: 1,
        billingCycle: 'MONTHLY',
      });
    });

    await screen.findByText('ATS-BILL-1');
    fireEvent.click(screen.getByRole('button', { name: '카드 등록하기' }));

    await waitFor(() => {
      expect(requestBillingAuthMock).toHaveBeenCalledWith({
        method: 'CARD',
        successUrl:
          'http://localhost:5173/subscriptions/billing/success?orderId=ATS-BILL-1&amount=9900',
        failUrl:
          'http://localhost:5173/subscriptions/billing/fail?orderId=ATS-BILL-1&amount=9900',
      });
    });
    expect(confirmPaymentMock).not.toHaveBeenCalled();
  });

  it('confirms Toss billing success redirect with authKey', async () => {
    render(
      <MemoryRouter
        initialEntries={[
          '/subscriptions/billing/success?authKey=auth-key&customerKey=ats_billing_customer_1&orderId=ATS-BILL-1&amount=9900',
        ]}
      >
        <SubscriptionPaymentPage />
      </MemoryRouter>,
    );

    await waitFor(() => {
      expect(confirmBillingAgreementMock).toHaveBeenCalledWith({
        orderId: 'ATS-BILL-1',
        authKey: 'auth-key',
        customerKey: 'ats_billing_customer_1',
        amount: 9900,
      });
    });
    expect(navigateMock).toHaveBeenCalledWith('/subscriptions/manage');
  });
});

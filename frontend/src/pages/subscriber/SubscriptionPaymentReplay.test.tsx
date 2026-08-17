import { act, render, screen, waitFor } from '@testing-library/react';
import { createMemoryRouter, RouterProvider } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import SubscriptionPaymentPage from '@/pages/subscriber/SubscriptionPaymentPage';

const confirmBillingAgreementMock = vi.hoisted(() => vi.fn());
const fetchPaymentCommandOutcomeMock = vi.hoisted(() => vi.fn());
const fetchMySubscriptionMock = vi.hoisted(() => vi.fn());
const fetchMyBillingAgreementMock = vi.hoisted(() => vi.fn());
const showToastMock = vi.hoisted(() => vi.fn());

vi.mock('@/api/payments', () => ({
  confirmBillingAgreement: (...args: unknown[]) => confirmBillingAgreementMock(...args),
  fetchPaymentCommandOutcome: (...args: unknown[]) => fetchPaymentCommandOutcomeMock(...args),
  fetchMyBillingAgreement: (...args: unknown[]) => fetchMyBillingAgreementMock(...args),
  prepareBillingAgreement: vi.fn(),
}));

vi.mock('@/api/userSubscriptions', () => ({
  fetchMySubscription: (...args: unknown[]) => fetchMySubscriptionMock(...args),
}));

vi.mock('@/api/subscriptions', () => ({
  fetchSubscriptionPlans: vi.fn(),
}));

vi.mock('@/store/toastStore', () => ({
  useToastStore: (selector: (state: { show: typeof showToastMock }) => unknown) =>
    selector({ show: showToastMock }),
}));

vi.mock('@/store/authStore', () => ({
  useAuthStore: (selector: (state: { user: { userType: 'INDIVIDUAL' } }) => unknown) =>
    selector({ user: { userType: 'INDIVIDUAL' } }),
}));

describe('SubscriptionPaymentPage callback history', () => {
  beforeEach(() => {
    confirmBillingAgreementMock.mockReset();
    confirmBillingAgreementMock.mockResolvedValue({ orderStatus: 'DONE' });
    fetchPaymentCommandOutcomeMock.mockReset();
    fetchPaymentCommandOutcomeMock.mockResolvedValue({
      purpose: 'SUBSCRIBE',
      orderStatus: 'DONE',
      userSubscriptionId: 100,
      targetSubscriptionId: 1,
      targetBillingCycle: 'MONTHLY',
    });
    fetchMySubscriptionMock.mockReset();
    fetchMySubscriptionMock.mockResolvedValue({
      id: 100,
      subscription: { id: 1 },
      billingCycle: 'MONTHLY',
      status: 'ACTIVE',
    });
    fetchMyBillingAgreementMock.mockReset();
    fetchMyBillingAgreementMock.mockResolvedValue({
      status: 'ACTIVE',
      subscription: {
        id: 100,
        subscription: { id: 1 },
        billingCycle: 'MONTHLY',
        status: 'ACTIVE',
      },
    });
    showToastMock.mockReset();
  });

  it('replaces a successful callback so Back cannot replay confirmation', async () => {
    const callback =
      '/subscriptions/checkout/success?authKey=auth-key&customerKey=customer&orderId=order-1&amount=9900';
    const router = createMemoryRouter(
      [
        { path: '/origin', element: <p>origin</p> },
        { path: '/subscriptions/checkout/success', element: <SubscriptionPaymentPage /> },
        { path: '/subscriptions/manage', element: <p>manage</p> },
      ],
      { initialEntries: ['/origin', callback], initialIndex: 1 },
    );

    render(<RouterProvider router={router} />);

    expect(await screen.findByText('manage')).toBeInTheDocument();
    expect(confirmBillingAgreementMock).toHaveBeenCalledTimes(1);

    await router.navigate(-1);

    expect(await screen.findByText('origin')).toBeInTheDocument();
    expect(confirmBillingAgreementMock).toHaveBeenCalledTimes(1);
  });

  it('removes callback keys from history before the confirmation response settles', async () => {
    let resolveConfirm!: (value: { orderStatus: 'DONE' }) => void;
    confirmBillingAgreementMock.mockReturnValueOnce(
      new Promise<{ orderStatus: 'DONE' }>((resolve) => {
        resolveConfirm = resolve;
      }),
    );
    const router = createMemoryRouter(
      [
        {
          path: '/subscriptions/checkout/success',
          element: <SubscriptionPaymentPage />,
        },
        { path: '/subscriptions/manage', element: <p>manage</p> },
      ],
      {
        initialEntries: [
          '/subscriptions/checkout/success?authKey=auth-key&customerKey=customer&orderId=order-1&amount=9900',
        ],
      },
    );

    render(<RouterProvider router={router} />);

    await waitFor(() => {
      expect(router.state.location.search).toBe('?orderId=order-1&amount=9900');
    });
    expect(router.state.location.search).not.toContain('authKey');
    expect(router.state.location.search).not.toContain('customerKey');
    expect(confirmBillingAgreementMock).toHaveBeenCalledTimes(1);

    await act(async () => {
      resolveConfirm({ orderStatus: 'DONE' });
    });
    expect(await screen.findByText('manage')).toBeInTheDocument();
  });
});

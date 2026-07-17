import { render, screen } from '@testing-library/react';
import { createMemoryRouter, RouterProvider } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import SubscriptionPaymentPage from '@/pages/subscriber/SubscriptionPaymentPage';

const confirmBillingAgreementMock = vi.hoisted(() => vi.fn());
const showToastMock = vi.hoisted(() => vi.fn());

vi.mock('@/api/payments', () => ({
  confirmBillingAgreement: (...args: unknown[]) => confirmBillingAgreementMock(...args),
  prepareBillingAgreement: vi.fn(),
}));

vi.mock('@/api/subscriptions', () => ({
  fetchSubscriptionPlans: vi.fn(),
}));

vi.mock('@/store/toastStore', () => ({
  useToastStore: (selector: (state: { show: typeof showToastMock }) => unknown) =>
    selector({ show: showToastMock }),
}));

describe('SubscriptionPaymentPage callback history', () => {
  beforeEach(() => {
    confirmBillingAgreementMock.mockReset();
    confirmBillingAgreementMock.mockResolvedValue({});
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
});

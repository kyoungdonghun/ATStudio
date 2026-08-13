import { act, fireEvent, render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import SubscriptionPlanPage from '@/pages/public/SubscriptionPlanPage';

const navigateMock = vi.hoisted(() => vi.fn());
const toastShowMock = vi.hoisted(() => vi.fn());
const fetchSubscriptionPlansMock = vi.hoisted(() => vi.fn());
const fetchMySubscriptionMock = vi.hoisted(() => vi.fn());

const authState: {
  user: { userType: 'INDIVIDUAL' | 'BUSINESS' };
  isAuthenticated: () => boolean;
} = {
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
  isNoActiveSubscriptionError: (error: unknown) =>
    (error as { response?: { status?: number; data?: { errorCode?: string } } })?.response
      ?.status === 403 &&
    (error as { response?: { data?: { errorCode?: string } } })?.response?.data?.errorCode ===
      'NO_ACTIVE_SUBSCRIPTION',
}));

function renderPage() {
  return render(
    <MemoryRouter future={{ v7_startTransition: true, v7_relativeSplatPath: true }}>
      <SubscriptionPlanPage />
    </MemoryRouter>,
  );
}

function plan(id: number, name: string, userType: 'INDIVIDUAL' | 'BUSINESS') {
  return {
    id,
    name,
    description: name,
    userType,
    priceMonthly: id * 9900,
    priceYearly: id * 99000,
    downloadPerDay: 5,
    maxWhitelistChannels: 1,
    maxPlaylists: 3,
    isActive: true,
  };
}

function noActiveSubscriptionError() {
  return {
    response: {
      status: 403,
      data: { errorCode: 'NO_ACTIVE_SUBSCRIPTION' },
    },
  };
}

function deferred<T>() {
  let resolve!: (value: T) => void;
  const promise = new Promise<T>((next) => {
    resolve = next;
  });
  return { promise, resolve };
}

describe('SubscriptionPlanPage', () => {
  beforeEach(() => {
    authState.user.userType = 'INDIVIDUAL';
    navigateMock.mockReset();
    toastShowMock.mockReset();
    fetchSubscriptionPlansMock.mockReset();
    fetchMySubscriptionMock.mockReset();

    fetchSubscriptionPlansMock.mockResolvedValue([
      plan(1, 'STANDARD', 'INDIVIDUAL'),
      plan(2, 'DELUXE', 'INDIVIDUAL'),
    ]);
    fetchMySubscriptionMock.mockRejectedValue(noActiveSubscriptionError());
  });

  it('starts new subscriptions through Toss recurring billing after documented absence', async () => {
    renderPage();

    fireEvent.click(
      await screen.findByRole('button', {
        name: '\uC9C0\uAE08 \uC2DC\uC791\uD558\uAE30',
      }),
    );

    await waitFor(() => {
      expect(navigateMock).toHaveBeenCalledWith(
        '/subscriptions/checkout?planId=2&userType=INDIVIDUAL&billingCycle=YEARLY&purpose=SUBSCRIBE',
      );
    });
  });

  it('shows a localized loading state while reads are pending', async () => {
    const plans = deferred<ReturnType<typeof plan>[]>();
    fetchSubscriptionPlansMock.mockReturnValueOnce(plans.promise);

    renderPage();

    expect(
      screen.getByText(
        '\uAD6C\uB3C5 \uD50C\uB79C\uC744 \uBD88\uB7EC\uC624\uB294 \uC911\uC785\uB2C8\uB2E4.',
      ),
    ).toBeInTheDocument();

    await act(async () => {
      plans.resolve([plan(1, 'STANDARD', 'INDIVIDUAL')]);
    });

    expect(
      await screen.findByRole('button', { name: '\uC2DC\uC791\uD558\uAE30' }),
    ).toBeInTheDocument();
  });

  it('keeps the empty state distinct and retries the complete read', async () => {
    fetchSubscriptionPlansMock
      .mockResolvedValueOnce([])
      .mockResolvedValueOnce([plan(1, 'STANDARD', 'INDIVIDUAL')]);

    renderPage();

    expect(
      await screen.findByText(
        '\uD604\uC7AC \uC120\uD0DD\uD560 \uC218 \uC788\uB294 \uAD6C\uB3C5 \uD50C\uB79C\uC774 \uC5C6\uC2B5\uB2C8\uB2E4.',
      ),
    ).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: '\uB2E4\uC2DC \uC2DC\uB3C4' }));

    expect(
      await screen.findByRole('button', { name: '\uC2DC\uC791\uD558\uAE30' }),
    ).toBeInTheDocument();
    expect(fetchSubscriptionPlansMock).toHaveBeenCalledTimes(2);
    expect(fetchMySubscriptionMock).toHaveBeenCalledTimes(2);
  });

  it('shows a retryable error when plan loading fails', async () => {
    fetchSubscriptionPlansMock
      .mockRejectedValueOnce(new Error('plan service unavailable'))
      .mockResolvedValueOnce([plan(1, 'STANDARD', 'INDIVIDUAL')]);

    renderPage();

    expect(
      await screen.findByText(
        '\uD50C\uB79C \uC815\uBCF4\uB97C \uBD88\uB7EC\uC624\uC9C0 \uBABB\uD588\uC2B5\uB2C8\uB2E4. \uB2E4\uC2DC \uC2DC\uB3C4\uD574\uC8FC\uC138\uC694.',
      ),
    ).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: '\uB2E4\uC2DC \uC2DC\uB3C4' }));

    expect(
      await screen.findByRole('button', { name: '\uC2DC\uC791\uD558\uAE30' }),
    ).toBeInTheDocument();
  });

  it.each([
    ['unauthorized', { response: { status: 401, data: { errorCode: 'UNAUTHORIZED' } } }],
    ['forbidden', { response: { status: 403, data: { errorCode: 'FORBIDDEN' } } }],
    ['server', { response: { status: 503, data: { errorCode: 'SERVER_ERROR' } } }],
    ['network', new Error('network unavailable')],
  ])('does not project a %s subscription read failure as unsubscribed', async (_label, error) => {
    fetchMySubscriptionMock.mockRejectedValueOnce(error);

    renderPage();

    expect(
      await screen.findByText(
        '\uAD6C\uB3C5 \uC815\uBCF4\uB97C \uBD88\uB7EC\uC624\uC9C0 \uBABB\uD588\uC2B5\uB2C8\uB2E4. \uB2E4\uC2DC \uC2DC\uB3C4\uD574\uC8FC\uC138\uC694.',
      ),
    ).toBeInTheDocument();
    expect(
      screen.queryByRole('button', { name: '\uC2DC\uC791\uD558\uAE30' }),
    ).not.toBeInTheDocument();
    expect(navigateMock).not.toHaveBeenCalled();
  });

  it('ignores late plan and subscription completions from a retired audience request', async () => {
    renderPage();

    expect(await screen.findAllByText('\uB514\uB7ED\uC2A4')).not.toHaveLength(0);
    const businessTab = screen.getByRole('button', { name: '\uAE30\uC5C5' });
    const stalePlans = deferred<ReturnType<typeof plan>[]>();
    const staleSubscription = deferred<Record<string, unknown>>();
    fetchSubscriptionPlansMock
      .mockReturnValueOnce(stalePlans.promise)
      .mockResolvedValueOnce([plan(2, 'DELUXE', 'INDIVIDUAL')]);
    fetchMySubscriptionMock
      .mockReturnValueOnce(staleSubscription.promise)
      .mockRejectedValueOnce(noActiveSubscriptionError());

    fireEvent.click(businessTab);
    fireEvent.click(screen.getByRole('button', { name: '\uAC1C\uC778' }));

    expect(await screen.findAllByText('\uB514\uB7ED\uC2A4')).not.toHaveLength(0);
    await waitFor(() => {
      expect(
        screen.queryByText(
          '\uAD6C\uB3C5 \uD50C\uB79C\uC744 \uBD88\uB7EC\uC624\uB294 \uC911\uC785\uB2C8\uB2E4.',
        ),
      ).not.toBeInTheDocument();
    });

    await act(async () => {
      stalePlans.resolve([plan(3, 'PREMIUM', 'BUSINESS')]);
      staleSubscription.resolve({
        id: 300,
        subscription: plan(3, 'PREMIUM', 'BUSINESS'),
        billingCycle: 'MONTHLY',
        status: 'ACTIVE',
        startedAt: '2026-08-01',
        expiresAt: '2026-09-01',
        pendingSubscriptionId: null,
        pendingBillingCycle: null,
      });
    });

    expect(screen.queryByText('\uD504\uB9AC\uBBF8\uC5C4')).not.toBeInTheDocument();
    expect(
      screen.queryByText(/\uD604\uC7AC .*\uD50C\uB79C\uC744 \uAD6C\uB3C5 \uC911\uC785\uB2C8\uB2E4/),
    ).not.toBeInTheDocument();
  });
});

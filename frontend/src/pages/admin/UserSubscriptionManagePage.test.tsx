import { StrictMode } from 'react';
import { act, render, screen, waitFor } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import UserSubscriptionManagePage from '@/pages/admin/UserSubscriptionManagePage';
import type { MySubscription } from '@/api/userSubscriptions';
import type { PagedResponse } from '@/types';

const mocks = vi.hoisted(() => ({
  fetchSubscriptions: vi.fn(),
  fetchPlans: vi.fn(),
  updateSubscription: vi.fn(),
  deleteSubscription: vi.fn(),
}));

vi.mock('@/api/userSubscriptions', () => ({
  fetchAdminUserSubscriptions: (...args: unknown[]) => mocks.fetchSubscriptions(...args),
  updateAdminUserSubscription: (...args: unknown[]) => mocks.updateSubscription(...args),
  deleteAdminUserSubscription: (...args: unknown[]) => mocks.deleteSubscription(...args),
}));

vi.mock('@/api/subscriptions', () => ({
  fetchAdminSubscriptionPlans: (...args: unknown[]) => mocks.fetchPlans(...args),
}));

function deferred<T>() {
  let resolve!: (value: T) => void;
  let reject!: (reason?: unknown) => void;
  const promise = new Promise<T>((nextResolve, nextReject) => {
    resolve = nextResolve;
    reject = nextReject;
  });
  return { promise, resolve, reject };
}

function subscription(id: number, nickname: string): MySubscription {
  return {
    id,
    userId: id,
    userNickname: nickname,
    subscription: {
      id: 10,
      name: 'STANDARD',
      description: 'Standard plan',
      userType: 'INDIVIDUAL',
      priceMonthly: 9900,
      priceYearly: 99000,
      downloadPerDay: 10,
      maxWhitelistChannels: 1,
      maxPlaylists: 3,
      isActive: true,
    },
    billingCycle: 'MONTHLY',
    status: 'ACTIVE',
    startedAt: '2026-07-16T00:00:00',
    expiresAt: '2026-08-16T00:00:00',
    pendingSubscriptionId: null,
    pendingBillingCycle: null,
  };
}

function page(entry: MySubscription): PagedResponse<MySubscription> {
  return {
    dataList: [entry],
    pageInfo: { page: 1, size: 20, total: 1, start: 1, end: 1, prev: false, next: false },
  };
}

function renderStrictPage() {
  return render(
    <StrictMode>
      <UserSubscriptionManagePage />
    </StrictMode>,
  );
}

describe('UserSubscriptionManagePage request fencing', () => {
  beforeEach(() => {
    mocks.fetchSubscriptions.mockReset();
    mocks.fetchPlans.mockReset();
    mocks.updateSubscription.mockReset();
    mocks.deleteSubscription.mockReset();
    mocks.fetchPlans.mockResolvedValue([]);
  });

  it('ignores the aborted StrictMode success after the current response', async () => {
    const first = deferred<PagedResponse<MySubscription>>();
    const second = deferred<PagedResponse<MySubscription>>();
    mocks.fetchSubscriptions.mockReturnValueOnce(first.promise).mockReturnValueOnce(second.promise);

    renderStrictPage();
    await waitFor(() => expect(mocks.fetchSubscriptions).toHaveBeenCalledTimes(2));
    const firstSignal = mocks.fetchSubscriptions.mock.calls[0][2] as AbortSignal;
    expect(firstSignal.aborted).toBe(true);

    await act(async () => second.resolve(page(subscription(2, 'CurrentSubscriber'))));
    expect(await screen.findByText('CurrentSubscriber')).toBeInTheDocument();

    await act(async () => first.resolve(page(subscription(1, 'OldSubscriber'))));
    expect(screen.getByText('CurrentSubscriber')).toBeInTheDocument();
    expect(screen.queryByText('OldSubscriber')).not.toBeInTheDocument();
  });

  it('ignores the aborted StrictMode failure after the current response', async () => {
    const first = deferred<PagedResponse<MySubscription>>();
    const second = deferred<PagedResponse<MySubscription>>();
    mocks.fetchSubscriptions.mockReturnValueOnce(first.promise).mockReturnValueOnce(second.promise);

    renderStrictPage();
    await waitFor(() => expect(mocks.fetchSubscriptions).toHaveBeenCalledTimes(2));
    await act(async () => second.resolve(page(subscription(2, 'CurrentAfterFailure'))));
    expect(await screen.findByText('CurrentAfterFailure')).toBeInTheDocument();

    await act(async () => first.reject(new Error('old failure')));
    expect(screen.getByText('CurrentAfterFailure')).toBeInTheDocument();
    expect(screen.queryByText('구독 목록을 불러오지 못했습니다.')).not.toBeInTheDocument();
  });
});

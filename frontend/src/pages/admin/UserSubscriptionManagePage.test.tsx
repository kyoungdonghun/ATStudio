import { StrictMode } from 'react';
import { act, fireEvent, render, screen, waitFor, within } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import UserSubscriptionManagePage from '@/pages/admin/UserSubscriptionManagePage';
import type {
  AdminSubscriptionCorrection,
  AdminSubscriptionCorrectionPreview,
  MySubscription,
} from '@/api/userSubscriptions';
import type { SubscriptionPlan } from '@/api/subscriptions';
import type { PagedResponse } from '@/types';

const mocks = vi.hoisted(() => ({
  fetchSubscriptions: vi.fn(),
  fetchPlans: vi.fn(),
  fetchOpenCorrection: vi.fn(),
  fetchCorrection: vi.fn(),
  previewCorrection: vi.fn(),
  createCorrection: vi.fn(),
  approveCorrection: vi.fn(),
  executeCorrection: vi.fn(),
}));

vi.mock('@/api/userSubscriptions', () => ({
  fetchAdminUserSubscriptions: (...args: unknown[]) => mocks.fetchSubscriptions(...args),
  fetchOpenAdminSubscriptionCorrection: (...args: unknown[]) => mocks.fetchOpenCorrection(...args),
  fetchAdminSubscriptionCorrection: (...args: unknown[]) => mocks.fetchCorrection(...args),
  previewAdminSubscriptionCorrection: (...args: unknown[]) => mocks.previewCorrection(...args),
  createAdminSubscriptionCorrection: (...args: unknown[]) => mocks.createCorrection(...args),
  approveAdminSubscriptionCorrection: (...args: unknown[]) => mocks.approveCorrection(...args),
  executeAdminSubscriptionCorrection: (...args: unknown[]) => mocks.executeCorrection(...args),
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

function subscription(
  id: number,
  nickname: string,
  overrides: Partial<MySubscription> = {},
): MySubscription {
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
    expiresAt: '2099-08-16T00:00:00',
    pendingSubscriptionId: null,
    pendingBillingCycle: null,
    ...overrides,
  };
}

function plan(id: number, name: string): SubscriptionPlan {
  return {
    id,
    name,
    description: `${name} plan`,
    userType: 'INDIVIDUAL',
    priceMonthly: 19900,
    priceYearly: 199000,
    downloadPerDay: 20,
    maxWhitelistChannels: 2,
    maxPlaylists: 5,
    isActive: true,
  };
}

function correctionPreview(
  overrides: Partial<AdminSubscriptionCorrectionPreview> = {},
): AdminSubscriptionCorrectionPreview {
  return {
    userSubscriptionId: 1,
    userId: 1,
    userNickname: 'CurrentSubscriber',
    currentSubscriptionId: 10,
    currentPlanName: 'STANDARD',
    currentBillingCycle: 'MONTHLY',
    currentStatus: 'ACTIVE',
    currentExpiresAt: '2099-08-16',
    currentPendingSubscriptionId: 30,
    currentPendingPlanName: 'FUTURE',
    currentPendingBillingCycle: 'YEARLY',
    targetSubscriptionId: 20,
    targetPlanName: 'PREMIUM',
    targetBillingCycle: 'YEARLY',
    targetStatus: 'CANCELLED',
    targetExpiresAt: '2099-09-01',
    clearPendingChange: true,
    cancelBillingAgreement: true,
    currentBillingAgreementStatus: 'ACTIVE',
    targetBillingAgreementStatus: 'CANCELLED',
    externalPaymentExecuted: false,
    executable: true,
    reason: null,
    ...overrides,
  };
}

function correction(
  status: AdminSubscriptionCorrection['status'],
  overrides: Partial<AdminSubscriptionCorrection> = {},
): AdminSubscriptionCorrection {
  return {
    id: 501,
    userSubscriptionId: 1,
    userId: 1,
    userNickname: 'CurrentSubscriber',
    billingAgreementId: 41,
    status,
    action: 'SET_SUBSCRIPTION_STATE',
    beforeSubscriptionId: 10,
    beforePlanName: 'STANDARD',
    beforeBillingCycle: 'MONTHLY',
    beforeStatus: 'ACTIVE',
    beforeExpiresAt: '2099-08-16',
    beforePendingSubscriptionId: 30,
    beforePendingPlanName: 'FUTURE',
    beforePendingBillingCycle: 'YEARLY',
    targetSubscriptionId: 20,
    targetPlanName: 'PREMIUM',
    targetBillingCycle: 'YEARLY',
    targetStatus: 'CANCELLED',
    targetExpiresAt: '2099-09-01',
    clearPendingChange: true,
    cancelBillingAgreement: true,
    beforeBillingAgreementStatus: 'ACTIVE',
    afterBillingAgreementStatus: status === 'SUCCEEDED' ? 'CANCELLED' : 'ACTIVE',
    reasonNote: '지원 티켓 ATS-501',
    failureCode: null,
    failureMessage: null,
    requestedById: 99,
    approvedById: status === 'REQUESTED' ? null : 99,
    executedById: status === 'SUCCEEDED' ? 99 : null,
    approvalNote: status === 'REQUESTED' ? null : '승인 메모',
    executionNote: status === 'SUCCEEDED' ? '실행 메모' : null,
    approvedAt: status === 'REQUESTED' ? null : '2026-08-08T10:00:00',
    executedAt: status === 'SUCCEEDED' ? '2026-08-08T10:01:00' : null,
    createdAt: '2026-08-08T09:59:00',
    updatedAt: '2026-08-08T10:01:00',
    ...overrides,
  };
}

function page(entry: MySubscription): PagedResponse<MySubscription> {
  return {
    dataList: [entry],
    pageInfo: { page: 1, size: 20, total: 1, start: 1, end: 1, prev: false, next: false },
  };
}

function pageOf(entries: MySubscription[]): PagedResponse<MySubscription> {
  return {
    dataList: entries,
    pageInfo: {
      page: 1,
      size: 20,
      total: entries.length,
      start: entries.length ? 1 : 0,
      end: entries.length,
      prev: false,
      next: false,
    },
  };
}

async function openWorkflow(
  nickname = 'CurrentSubscriber',
  expectedState: 'new' | 'existing' = 'new',
) {
  const row = screen.getByText(nickname).closest('tr');
  expect(row).not.toBeNull();
  fireEvent.click(within(row as HTMLElement).getByRole('button', { name: '권한 보정' }));
  const dialog = screen.getByRole('dialog', { name: '사용자 구독 권한 보정' });
  if (expectedState === 'existing') {
    await within(dialog).findByText(/진행 중 요청 #\d+을 이어서 처리합니다/);
  } else {
    await waitFor(() => expect(within(dialog).getByLabelText('운영 사유 (필수)')).toBeEnabled());
  }
  return dialog;
}

function enterValidDraft(dialog: HTMLElement) {
  fireEvent.change(within(dialog).getByLabelText('목표 활성 플랜'), {
    target: { value: '20' },
  });
  fireEvent.change(within(dialog).getByLabelText('목표 결제 주기'), {
    target: { value: 'YEARLY' },
  });
  fireEvent.change(within(dialog).getByLabelText('목표 상태'), {
    target: { value: 'CANCELLED' },
  });
  fireEvent.change(within(dialog).getByLabelText('목표 만료일'), {
    target: { value: '2099-09-01' },
  });
  fireEvent.click(within(dialog).getByRole('checkbox', { name: /로컬 자동 갱신 약정 취소/ }));
  fireEvent.change(within(dialog).getByLabelText('운영 사유 (필수)'), {
    target: { value: ' 지원 티켓 ATS-501 ' },
  });
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
    mocks.fetchOpenCorrection.mockReset();
    mocks.fetchCorrection.mockReset();
    mocks.previewCorrection.mockReset();
    mocks.createCorrection.mockReset();
    mocks.approveCorrection.mockReset();
    mocks.executeCorrection.mockReset();
    mocks.fetchOpenCorrection.mockResolvedValue(null);
    mocks.fetchPlans.mockResolvedValue([
      plan(10, 'STANDARD'),
      plan(20, 'PREMIUM'),
      plan(30, 'FUTURE'),
    ]);
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
    expect(screen.getAllByText('CurrentSubscriber').length).toBeGreaterThan(0);
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

  it('blocks new work until open lookup succeeds and recovers with an explicit retry', async () => {
    const lookup = deferred<AdminSubscriptionCorrection | null>();
    mocks.fetchSubscriptions.mockResolvedValue(page(subscription(1, 'CurrentSubscriber')));
    mocks.fetchOpenCorrection.mockReturnValueOnce(lookup.promise);

    render(<UserSubscriptionManagePage />);
    expect(await screen.findByText('CurrentSubscriber')).toBeInTheDocument();
    const row = screen.getByText('CurrentSubscriber').closest('tr');
    fireEvent.click(within(row as HTMLElement).getByRole('button', { name: '권한 보정' }));
    const dialog = screen.getByRole('dialog', { name: '사용자 구독 권한 보정' });

    expect(
      within(dialog).getByText(/진행 중 권한 보정 요청을 확인하고 있습니다/),
    ).toBeInTheDocument();
    expect(within(dialog).getByLabelText('운영 사유 (필수)')).toBeDisabled();
    expect(within(dialog).getByRole('button', { name: '미리보기' })).toBeDisabled();
    expect(within(dialog).getByRole('button', { name: '요청 생성' })).toBeDisabled();

    await act(async () => lookup.reject(new Error('lookup failed')));
    expect(
      await within(dialog).findByText(
        '진행 중 권한 보정 요청을 확인하지 못했습니다. 새 요청은 차단되었습니다.',
      ),
    ).toBeInTheDocument();
    fireEvent.click(within(dialog).getByRole('button', { name: '진행 중 요청 다시 조회' }));

    await waitFor(() => expect(mocks.fetchOpenCorrection).toHaveBeenCalledTimes(2));
    await waitFor(() => expect(within(dialog).getByLabelText('운영 사유 (필수)')).toBeEnabled());
    expect(mocks.previewCorrection).not.toHaveBeenCalled();
    expect(mocks.createCorrection).not.toHaveBeenCalled();
  });

  it('resumes and approves a REQUESTED correction after the modal is closed and reopened', async () => {
    const requested = correction('REQUESTED');
    mocks.fetchSubscriptions.mockResolvedValue(page(subscription(1, 'CurrentSubscriber')));
    mocks.fetchOpenCorrection.mockResolvedValueOnce(null).mockResolvedValueOnce(requested);
    mocks.approveCorrection.mockResolvedValue(correction('APPROVED'));

    render(<UserSubscriptionManagePage />);
    expect(await screen.findByText('CurrentSubscriber')).toBeInTheDocument();
    let dialog = await openWorkflow();
    const closeButtons = within(dialog).getAllByRole('button', { name: '닫기' });
    fireEvent.click(closeButtons[closeButtons.length - 1]!);
    await waitFor(() =>
      expect(screen.queryByRole('dialog', { name: '사용자 구독 권한 보정' })).toBeNull(),
    );

    dialog = await openWorkflow('CurrentSubscriber', 'existing');
    expect(within(dialog).getByText('진행 중 요청 #501을 이어서 처리합니다.')).toBeInTheDocument();
    expect(within(dialog).getByLabelText('목표 활성 플랜')).toHaveValue('20');
    expect(within(dialog).getByLabelText('목표 결제 주기')).toHaveValue('YEARLY');
    expect(within(dialog).getByLabelText('목표 상태')).toHaveValue('CANCELLED');
    expect(within(dialog).getByLabelText('목표 만료일')).toHaveValue('2099-09-01');
    expect(within(dialog).getByLabelText('운영 사유 (필수)')).toHaveValue('지원 티켓 ATS-501');
    expect(within(dialog).getByText(/외부 결제 실행 없음/)).toBeInTheDocument();
    expect(within(dialog).getAllByText('PREMIUM').length).toBeGreaterThan(0);
    const comparison = within(dialog).getByRole('region', { name: '권한 보정 미리보기' });
    expect(within(comparison).getAllByText('취소됨').length).toBeGreaterThanOrEqual(2);

    fireEvent.change(within(dialog).getByLabelText('승인 메모 (선택)'), {
      target: { value: '재개 승인' },
    });
    fireEvent.click(within(dialog).getByRole('button', { name: '승인 단계로 이동' }));
    fireEvent.click(
      within(screen.getByRole('dialog', { name: '권한 보정 승인 확인' })).getByRole('button', {
        name: '승인 확정',
      }),
    );

    await within(dialog).findByLabelText('실행 메모 (선택)');
    expect(mocks.approveCorrection).toHaveBeenCalledWith(
      501,
      { note: '재개 승인' },
      expect.any(AbortSignal),
    );
    expect(mocks.previewCorrection).not.toHaveBeenCalled();
    expect(mocks.createCorrection).not.toHaveBeenCalled();
  });

  it('resumes and executes an APPROVED correction after the page is remounted', async () => {
    const item = subscription(1, 'CurrentSubscriber');
    mocks.fetchSubscriptions.mockResolvedValue(page(item));
    mocks.fetchOpenCorrection.mockResolvedValue(correction('APPROVED'));
    mocks.executeCorrection.mockResolvedValue(correction('SUCCEEDED'));

    const firstRender = render(<UserSubscriptionManagePage />);
    expect(await screen.findByText('CurrentSubscriber')).toBeInTheDocument();
    firstRender.unmount();

    render(<UserSubscriptionManagePage />);
    expect(await screen.findByText('CurrentSubscriber')).toBeInTheDocument();
    const dialog = await openWorkflow('CurrentSubscriber', 'existing');
    expect(within(dialog).getByText('진행 중 요청 #501을 이어서 처리합니다.')).toBeInTheDocument();
    expect(within(dialog).getByText(/외부 결제 실행 없음/)).toBeInTheDocument();
    fireEvent.change(within(dialog).getByLabelText('실행 메모 (선택)'), {
      target: { value: '재개 실행' },
    });
    fireEvent.click(within(dialog).getByRole('button', { name: '실행 확인' }));
    fireEvent.click(
      within(screen.getByRole('dialog', { name: '권한 보정 실행 확인' })).getByRole('button', {
        name: '권한 보정 실행',
      }),
    );

    expect(await within(dialog).findByText('권한 보정 실행 완료')).toBeInTheDocument();
    expect(mocks.executeCorrection).toHaveBeenCalledWith(
      501,
      { note: '재개 실행' },
      expect.any(AbortSignal),
    );
    expect(mocks.previewCorrection).not.toHaveBeenCalled();
    expect(mocks.createCorrection).not.toHaveBeenCalled();
    expect(mocks.fetchSubscriptions).toHaveBeenCalledTimes(3);
  });

  it('restores a PROCESSING correction as a read-only workflow', async () => {
    mocks.fetchSubscriptions.mockResolvedValue(page(subscription(1, 'CurrentSubscriber')));
    mocks.fetchOpenCorrection.mockResolvedValue(correction('PROCESSING'));

    render(<UserSubscriptionManagePage />);
    expect(await screen.findByText('CurrentSubscriber')).toBeInTheDocument();
    const dialog = await openWorkflow('CurrentSubscriber', 'existing');

    expect(within(dialog).getByText('진행 중 요청 #501을 이어서 처리합니다.')).toBeInTheDocument();
    expect(within(dialog).getByText(/로컬 보정을 처리하고 있습니다/)).toBeInTheDocument();
    expect(within(dialog).getByLabelText('운영 사유 (필수)')).toBeDisabled();
    expect(within(dialog).queryByRole('button', { name: '미리보기' })).toBeNull();
    expect(within(dialog).queryByRole('button', { name: '요청 생성' })).toBeNull();
    expect(within(dialog).getByText(/외부 결제 실행 없음/)).toBeInTheDocument();
  });

  it('aborts and ignores a stale open lookup from another row', async () => {
    const oldLookup = deferred<AdminSubscriptionCorrection | null>();
    const currentLookup = deferred<AdminSubscriptionCorrection | null>();
    mocks.fetchSubscriptions.mockResolvedValue(
      pageOf([subscription(1, 'OldSubscriber'), subscription(2, 'CurrentSubscriber')]),
    );
    mocks.fetchOpenCorrection
      .mockReturnValueOnce(oldLookup.promise)
      .mockReturnValueOnce(currentLookup.promise);

    render(<UserSubscriptionManagePage />);
    expect(await screen.findByText('OldSubscriber')).toBeInTheDocument();
    const oldRow = screen.getByText('OldSubscriber').closest('tr');
    fireEvent.click(within(oldRow as HTMLElement).getByRole('button', { name: '권한 보정' }));
    let dialog = screen.getByRole('dialog', { name: '사용자 구독 권한 보정' });
    await waitFor(() => expect(mocks.fetchOpenCorrection).toHaveBeenCalledTimes(1));
    const oldSignal = mocks.fetchOpenCorrection.mock.calls[0][1] as AbortSignal;
    const closeButtons = within(dialog).getAllByRole('button', { name: '닫기' });
    fireEvent.click(closeButtons[closeButtons.length - 1]!);
    expect(oldSignal.aborted).toBe(true);
    await waitFor(() =>
      expect(screen.queryByRole('dialog', { name: '사용자 구독 권한 보정' })).toBeNull(),
    );

    const currentRow = screen.getByText('CurrentSubscriber').closest('tr');
    fireEvent.click(within(currentRow as HTMLElement).getByRole('button', { name: '권한 보정' }));
    dialog = screen.getByRole('dialog', { name: '사용자 구독 권한 보정' });
    await waitFor(() => expect(mocks.fetchOpenCorrection).toHaveBeenCalledTimes(2));
    await act(async () =>
      currentLookup.resolve(
        correction('REQUESTED', {
          id: 602,
          userSubscriptionId: 2,
          userId: 2,
          userNickname: 'CurrentSubscriber',
        }),
      ),
    );
    expect(
      await within(dialog).findByText('진행 중 요청 #602을 이어서 처리합니다.'),
    ).toBeInTheDocument();

    await act(async () =>
      oldLookup.resolve(
        correction('REQUESTED', {
          id: 601,
          userSubscriptionId: 1,
          userId: 1,
          userNickname: 'OldSubscriber',
        }),
      ),
    );
    expect(within(dialog).getByText('진행 중 요청 #602을 이어서 처리합니다.')).toBeInTheDocument();
    expect(within(dialog).queryByText('진행 중 요청 #601을 이어서 처리합니다.')).toBeNull();
  });

  it('blocks request creation when the preview is not executable', async () => {
    mocks.fetchSubscriptions.mockResolvedValue(page(subscription(1, 'CurrentSubscriber')));
    mocks.previewCorrection.mockResolvedValue(
      correctionPreview({
        executable: false,
        reason: 'A payment order can still receive a provider outcome.',
      }),
    );

    render(<UserSubscriptionManagePage />);
    expect(await screen.findByText('CurrentSubscriber')).toBeInTheDocument();
    const dialog = await openWorkflow();
    enterValidDraft(dialog);
    fireEvent.click(within(dialog).getByRole('button', { name: '미리보기' }));

    expect(
      await within(dialog).findByText(
        '결제사업자 결과를 아직 받을 수 있는 진행 중 주문이 있어 보정할 수 없습니다.',
      ),
    ).toBeInTheDocument();
    expect(within(dialog).getByRole('button', { name: '요청 생성' })).toBeDisabled();
    expect(within(dialog).getByText(/외부 결제 실행 없음/)).toBeInTheDocument();
    expect(mocks.createCorrection).not.toHaveBeenCalled();
  });

  it('preserves the row when preview loading fails', async () => {
    mocks.fetchSubscriptions.mockResolvedValue(page(subscription(1, 'CurrentSubscriber')));
    mocks.previewCorrection.mockRejectedValue(new Error('preview failed'));

    render(<UserSubscriptionManagePage />);
    expect(await screen.findByText('CurrentSubscriber')).toBeInTheDocument();
    const dialog = await openWorkflow();
    enterValidDraft(dialog);
    fireEvent.click(within(dialog).getByRole('button', { name: '미리보기' }));

    expect(
      await within(dialog).findByText(
        '보정 미리보기를 불러오지 못했습니다. 목록은 변경되지 않았습니다.',
      ),
    ).toBeInTheDocument();
    expect(screen.getAllByText('CurrentSubscriber').length).toBeGreaterThan(0);
    expect(mocks.createCorrection).not.toHaveBeenCalled();
  });

  it('lets a browser/server Seoul-date disagreement reach authoritative preview', async () => {
    vi.useFakeTimers({ toFake: ['Date'] });
    vi.setSystemTime(new Date('2100-01-01T00:00:00+09:00'));
    try {
      mocks.fetchSubscriptions.mockResolvedValue(page(subscription(1, 'CurrentSubscriber')));
      mocks.previewCorrection.mockResolvedValue(correctionPreview());

      render(<UserSubscriptionManagePage />);
      expect(await screen.findByText('CurrentSubscriber')).toBeInTheDocument();
      const dialog = await openWorkflow();
      enterValidDraft(dialog);
      const expiresInput = within(dialog).getByLabelText('목표 만료일');
      expect(expiresInput).toHaveValue('2099-09-01');
      expect(expiresInput).not.toHaveAttribute('min');
      expect(expiresInput).not.toHaveAttribute('max');

      fireEvent.click(within(dialog).getByRole('button', { name: '미리보기' }));

      expect(await within(dialog).findByText(/외부 결제 실행 없음/)).toBeInTheDocument();
      expect(mocks.previewCorrection).toHaveBeenCalledWith(
        expect.objectContaining({
          targetStatus: 'CANCELLED',
          targetExpiresAt: '2099-09-01',
        }),
        expect.any(AbortSignal),
      );
    } finally {
      vi.useRealTimers();
    }
  });

  it('runs preview, request, approval, and execution with explicit cancellable confirmations', async () => {
    const item = subscription(1, 'CurrentSubscriber', {
      pendingSubscriptionId: 30,
      pendingBillingCycle: 'YEARLY',
    });
    mocks.fetchSubscriptions.mockResolvedValue(page(item));
    mocks.previewCorrection.mockResolvedValue(correctionPreview());
    mocks.createCorrection.mockResolvedValue(correction('REQUESTED'));
    mocks.approveCorrection.mockResolvedValue(correction('APPROVED'));
    mocks.executeCorrection.mockResolvedValue(correction('SUCCEEDED'));

    render(<UserSubscriptionManagePage />);
    expect(await screen.findByText('CurrentSubscriber')).toBeInTheDocument();
    const dialog = await openWorkflow();
    enterValidDraft(dialog);
    expect(
      within(dialog).getByRole('checkbox', { name: /대기 중인 플랜·주기 변경 제거/ }),
    ).toBeChecked();

    fireEvent.click(within(dialog).getByRole('button', { name: '미리보기' }));
    expect(await within(dialog).findByText(/외부 결제 실행 없음/)).toBeInTheDocument();
    expect(within(dialog).getByLabelText('운영 사유 (필수)')).toHaveValue('지원 티켓 ATS-501');
    expect(within(dialog).getByText('"지원 티켓 ATS-501"')).toBeInTheDocument();
    expect(mocks.previewCorrection).toHaveBeenCalledWith(
      {
        userSubscriptionId: 1,
        targetSubscriptionId: 20,
        targetBillingCycle: 'YEARLY',
        targetStatus: 'CANCELLED',
        targetExpiresAt: '2099-09-01',
        clearPendingChange: true,
        cancelBillingAgreement: true,
        reasonNote: '지원 티켓 ATS-501',
      },
      expect.any(AbortSignal),
    );

    fireEvent.click(within(dialog).getByRole('button', { name: '요청 생성' }));
    expect(await within(dialog).findByText(/요청 #501이 생성되었습니다/)).toBeInTheDocument();
    expect(mocks.createCorrection).toHaveBeenCalledWith(
      expect.objectContaining({ reasonNote: '지원 티켓 ATS-501' }),
      expect.any(AbortSignal),
    );

    fireEvent.change(within(dialog).getByLabelText('승인 메모 (선택)'), {
      target: { value: ' 승인 메모 ' },
    });
    fireEvent.click(within(dialog).getByRole('button', { name: '승인 단계로 이동' }));
    let confirmationDialog = screen.getByRole('dialog', { name: '권한 보정 승인 확인' });
    expect(within(confirmationDialog).getByText(/저장할 승인 메모: "승인 메모"/)).toBeVisible();
    fireEvent.click(within(confirmationDialog).getByRole('button', { name: '취소' }));
    expect(mocks.approveCorrection).not.toHaveBeenCalled();
    expect(within(dialog).getByLabelText('승인 메모 (선택)')).toHaveValue('승인 메모');

    fireEvent.click(within(dialog).getByRole('button', { name: '승인 단계로 이동' }));
    confirmationDialog = screen.getByRole('dialog', { name: '권한 보정 승인 확인' });
    fireEvent.click(within(confirmationDialog).getByRole('button', { name: '승인 확정' }));
    expect(await within(dialog).findByLabelText('실행 메모 (선택)')).toBeInTheDocument();
    expect(mocks.approveCorrection).toHaveBeenCalledWith(
      501,
      { note: '승인 메모' },
      expect.any(AbortSignal),
    );

    fireEvent.change(within(dialog).getByLabelText('실행 메모 (선택)'), {
      target: { value: ' 실행 메모 ' },
    });
    fireEvent.click(within(dialog).getByRole('button', { name: '실행 확인' }));
    confirmationDialog = screen.getByRole('dialog', { name: '권한 보정 실행 확인' });
    expect(within(confirmationDialog).getByText(/저장할 실행 메모: "실행 메모"/)).toBeVisible();
    fireEvent.click(within(confirmationDialog).getByRole('button', { name: '취소' }));
    expect(mocks.executeCorrection).not.toHaveBeenCalled();
    expect(within(dialog).getByLabelText('실행 메모 (선택)')).toHaveValue('실행 메모');

    fireEvent.click(within(dialog).getByRole('button', { name: '실행 확인' }));
    confirmationDialog = screen.getByRole('dialog', { name: '권한 보정 실행 확인' });
    fireEvent.click(within(confirmationDialog).getByRole('button', { name: '권한 보정 실행' }));

    expect(await within(dialog).findByText('권한 보정 실행 완료')).toBeInTheDocument();
    expect(mocks.executeCorrection).toHaveBeenCalledWith(
      501,
      { note: '실행 메모' },
      expect.any(AbortSignal),
    );
    expect(mocks.fetchSubscriptions).toHaveBeenCalledTimes(2);
    expect(
      await screen.findByText('권한 보정 #501 실행이 완료되어 최신 구독 목록에 반영했습니다.'),
    ).toBeInTheDocument();
  });

  it('preserves a definite 4xx mutation error without reconciliation', async () => {
    mocks.fetchSubscriptions.mockResolvedValue(page(subscription(1, 'CurrentSubscriber')));
    mocks.previewCorrection.mockResolvedValue(correctionPreview());
    mocks.createCorrection.mockRejectedValue({
      response: {
        status: 422,
        data: {
          errorCode: 'SUBSCRIPTION_CORRECTION_STALE',
          message: 'The subscription correction target changed.',
        },
      },
    });

    render(<UserSubscriptionManagePage />);
    expect(await screen.findByText('CurrentSubscriber')).toBeInTheDocument();
    const dialog = await openWorkflow();
    enterValidDraft(dialog);
    fireEvent.click(within(dialog).getByRole('button', { name: '미리보기' }));
    await within(dialog).findByText(/외부 결제 실행 없음/);
    fireEvent.click(within(dialog).getByRole('button', { name: '요청 생성' }));

    expect(
      await within(dialog).findByText('The subscription correction target changed.'),
    ).toBeInTheDocument();
    expect(within(dialog).getByLabelText('운영 사유 (필수)')).toHaveValue('지원 티켓 ATS-501');
    expect(within(dialog).getByText(/외부 결제 실행 없음/)).toBeInTheDocument();
    expect(within(dialog).queryByRole('button', { name: '상태 다시 확인' })).toBeNull();
    await waitFor(() =>
      expect(within(dialog).getByRole('button', { name: '요청 생성' })).toBeEnabled(),
    );
    expect(mocks.fetchOpenCorrection).toHaveBeenCalledTimes(1);
    expect(mocks.createCorrection).toHaveBeenCalledTimes(1);
  });

  it.each([
    [
      'no-response network',
      Object.assign(new Error('request response lost'), { code: 'ERR_NETWORK' }),
    ],
    [
      'no-response timeout',
      Object.assign(new Error('request timed out'), { code: 'ECONNABORTED' }),
    ],
  ])('reconciles a committed request after an ambiguous %s failure', async (_label, failure) => {
    const requested = correction('REQUESTED');
    mocks.fetchSubscriptions.mockResolvedValue(page(subscription(1, 'CurrentSubscriber')));
    mocks.fetchOpenCorrection.mockResolvedValueOnce(null).mockResolvedValueOnce(requested);
    mocks.previewCorrection.mockResolvedValue(correctionPreview());
    mocks.createCorrection.mockRejectedValue(failure);

    render(<UserSubscriptionManagePage />);
    expect(await screen.findByText('CurrentSubscriber')).toBeInTheDocument();
    const dialog = await openWorkflow();
    enterValidDraft(dialog);
    fireEvent.click(within(dialog).getByRole('button', { name: '미리보기' }));
    await within(dialog).findByText(/외부 결제 실행 없음/);
    fireEvent.click(within(dialog).getByRole('button', { name: '요청 생성' }));

    expect(
      await within(dialog).findByText(
        '서버 상태를 동기화했습니다. 요청 #501의 현재 단계는 요청됨입니다.',
      ),
    ).toBeInTheDocument();
    expect(within(dialog).getByText(/요청 #501이 생성되었습니다/)).toBeInTheDocument();
    expect(within(dialog).getByLabelText('운영 사유 (필수)')).toHaveValue('지원 티켓 ATS-501');
    expect(mocks.fetchOpenCorrection).toHaveBeenNthCalledWith(2, 1, expect.any(AbortSignal));
    expect(mocks.createCorrection).toHaveBeenCalledTimes(1);
    expect(screen.getAllByText('CurrentSubscriber').length).toBeGreaterThan(0);
    expect(mocks.fetchSubscriptions).toHaveBeenCalledTimes(1);
  });

  it('reconciles a committed approval after an ambiguous HTTP 5xx response', async () => {
    mocks.fetchSubscriptions.mockResolvedValue(page(subscription(1, 'CurrentSubscriber')));
    mocks.previewCorrection.mockResolvedValue(correctionPreview());
    mocks.createCorrection.mockResolvedValue(correction('REQUESTED'));
    mocks.approveCorrection.mockRejectedValue({
      response: { status: 503, data: { message: 'Service temporarily unavailable.' } },
    });
    mocks.fetchCorrection.mockResolvedValue(correction('APPROVED', { approvalNote: '승인 메모' }));

    render(<UserSubscriptionManagePage />);
    expect(await screen.findByText('CurrentSubscriber')).toBeInTheDocument();
    const dialog = await openWorkflow();
    enterValidDraft(dialog);
    fireEvent.click(within(dialog).getByRole('button', { name: '미리보기' }));
    await within(dialog).findByText(/외부 결제 실행 없음/);
    fireEvent.click(within(dialog).getByRole('button', { name: '요청 생성' }));
    await within(dialog).findByText(/요청 #501이 생성되었습니다/);
    fireEvent.change(within(dialog).getByLabelText('승인 메모 (선택)'), {
      target: { value: ' 승인 메모 ' },
    });
    fireEvent.click(within(dialog).getByRole('button', { name: '승인 단계로 이동' }));
    fireEvent.click(
      within(screen.getByRole('dialog', { name: '권한 보정 승인 확인' })).getByRole('button', {
        name: '승인 확정',
      }),
    );

    expect(
      await within(dialog).findByText(
        '서버 상태를 동기화했습니다. 요청 #501의 현재 단계는 승인됨입니다.',
      ),
    ).toBeInTheDocument();
    expect(within(dialog).getByLabelText('실행 메모 (선택)')).toBeInTheDocument();
    expect(within(dialog).getByText(/승인 메모: 승인 메모/)).toBeInTheDocument();
    expect(mocks.fetchCorrection).toHaveBeenCalledWith(501, expect.any(AbortSignal));
    expect(mocks.approveCorrection).toHaveBeenCalledTimes(1);
    expect(screen.getAllByText('CurrentSubscriber').length).toBeGreaterThan(0);
  });

  it('keeps an ambiguous request fenced after initial and repeated 204 reads', async () => {
    mocks.fetchSubscriptions.mockResolvedValue(page(subscription(1, 'CurrentSubscriber')));
    mocks.fetchOpenCorrection.mockResolvedValue(null);
    mocks.previewCorrection.mockResolvedValue(correctionPreview());
    mocks.createCorrection.mockRejectedValue(
      Object.assign(new Error('request timed out'), { code: 'ECONNABORTED' }),
    );

    render(<UserSubscriptionManagePage />);
    expect(await screen.findByText('CurrentSubscriber')).toBeInTheDocument();
    const dialog = await openWorkflow();
    enterValidDraft(dialog);
    fireEvent.click(within(dialog).getByRole('button', { name: '미리보기' }));
    await within(dialog).findByText(/외부 결제 실행 없음/);
    fireEvent.click(within(dialog).getByRole('button', { name: '요청 생성' }));

    expect(
      await within(dialog).findByText(
        '요청 생성 응답과 서버 상태를 모두 확인하지 못해 결과를 알 수 없습니다. 중복 요청 생성을 차단했습니다.',
      ),
    ).toBeInTheDocument();
    expect(within(dialog).getByLabelText('운영 사유 (필수)')).toHaveValue('지원 티켓 ATS-501');
    expect(within(dialog).getByText(/외부 결제 실행 없음/)).toBeInTheDocument();
    expect(within(dialog).getAllByRole('button', { name: '상태 다시 확인' })).toHaveLength(1);
    const createButton = within(dialog).getByRole('button', { name: '요청 생성' });
    expect(createButton).toBeDisabled();
    fireEvent.click(createButton);
    expect(mocks.createCorrection).toHaveBeenCalledTimes(1);
    expect(mocks.fetchOpenCorrection).toHaveBeenCalledTimes(2);

    fireEvent.click(within(dialog).getByRole('button', { name: '상태 다시 확인' }));

    await waitFor(() => expect(mocks.fetchOpenCorrection).toHaveBeenCalledTimes(3));
    expect(
      within(dialog).getByText(
        '요청 생성 응답과 서버 상태를 모두 확인하지 못해 결과를 알 수 없습니다. 중복 요청 생성을 차단했습니다.',
      ),
    ).toBeInTheDocument();
    expect(within(dialog).getAllByRole('button', { name: '상태 다시 확인' })).toHaveLength(1);
    expect(within(dialog).getByRole('button', { name: '요청 생성' })).toBeDisabled();
    expect(mocks.createCorrection).toHaveBeenCalledTimes(1);
  });

  it('reconciles a committed execution after its response is lost', async () => {
    mocks.fetchSubscriptions.mockResolvedValue(page(subscription(1, 'CurrentSubscriber')));
    mocks.fetchOpenCorrection.mockResolvedValue(correction('APPROVED'));
    mocks.executeCorrection.mockRejectedValue(new Error('execute response lost'));
    mocks.fetchCorrection.mockResolvedValue(
      correction('SUCCEEDED', { executionNote: '실행 메모' }),
    );

    render(<UserSubscriptionManagePage />);
    expect(await screen.findByText('CurrentSubscriber')).toBeInTheDocument();
    const dialog = await openWorkflow('CurrentSubscriber', 'existing');
    fireEvent.change(within(dialog).getByLabelText('실행 메모 (선택)'), {
      target: { value: ' 실행 메모 ' },
    });
    fireEvent.click(within(dialog).getByRole('button', { name: '실행 확인' }));
    fireEvent.click(
      within(screen.getByRole('dialog', { name: '권한 보정 실행 확인' })).getByRole('button', {
        name: '권한 보정 실행',
      }),
    );

    expect(
      await within(dialog).findByText(
        '서버 상태를 동기화했습니다. 요청 #501의 현재 단계는 성공입니다.',
      ),
    ).toBeInTheDocument();
    expect(within(dialog).getByText('권한 보정 실행 완료')).toBeInTheDocument();
    expect(mocks.fetchCorrection).toHaveBeenCalledWith(501, expect.any(AbortSignal));
    expect(mocks.executeCorrection).toHaveBeenCalledTimes(1);
    expect(mocks.fetchSubscriptions).toHaveBeenCalledTimes(2);
    expect(
      await screen.findByText('권한 보정 #501 실행이 완료되어 최신 구독 목록에 반영했습니다.'),
    ).toBeInTheDocument();
  });

  it('locks an unknown execution outcome and reconciles it through one explicit retry', async () => {
    mocks.fetchSubscriptions.mockResolvedValue(page(subscription(1, 'CurrentSubscriber')));
    mocks.fetchOpenCorrection.mockResolvedValue(correction('APPROVED'));
    mocks.executeCorrection.mockRejectedValue(new Error('execute response lost'));
    mocks.fetchCorrection
      .mockRejectedValueOnce(new Error('detail unavailable'))
      .mockResolvedValueOnce(correction('SUCCEEDED', { executionNote: '재시도 메모' }));

    render(<UserSubscriptionManagePage />);
    expect(await screen.findByText('CurrentSubscriber')).toBeInTheDocument();
    const dialog = await openWorkflow('CurrentSubscriber', 'existing');
    fireEvent.change(within(dialog).getByLabelText('실행 메모 (선택)'), {
      target: { value: ' 재시도 메모 ' },
    });
    fireEvent.click(within(dialog).getByRole('button', { name: '실행 확인' }));
    fireEvent.click(
      within(screen.getByRole('dialog', { name: '권한 보정 실행 확인' })).getByRole('button', {
        name: '권한 보정 실행',
      }),
    );

    expect(
      await within(dialog).findByText(
        '실행 응답과 서버 상태를 모두 확인하지 못해 결과를 알 수 없습니다. 중복 실행을 차단했습니다.',
      ),
    ).toBeInTheDocument();
    expect(within(dialog).getByText('진행 중 요청 #501을 이어서 처리합니다.')).toBeInTheDocument();
    expect(within(dialog).getByLabelText('운영 사유 (필수)')).toHaveValue('지원 티켓 ATS-501');
    expect(within(dialog).getByLabelText('실행 메모 (선택)')).toHaveValue('재시도 메모');
    expect(within(dialog).getByLabelText('실행 메모 (선택)')).toBeDisabled();
    expect(within(dialog).getByRole('button', { name: '실행 확인' })).toBeDisabled();
    expect(within(dialog).getAllByRole('button', { name: '상태 다시 확인' })).toHaveLength(1);
    expect(mocks.executeCorrection).toHaveBeenCalledTimes(1);

    fireEvent.click(within(dialog).getByRole('button', { name: '상태 다시 확인' }));

    expect(await within(dialog).findByText('권한 보정 실행 완료')).toBeInTheDocument();
    expect(
      within(dialog).getByText('서버 상태를 동기화했습니다. 요청 #501의 현재 단계는 성공입니다.'),
    ).toBeInTheDocument();
    expect(within(dialog).queryByRole('button', { name: '상태 다시 확인' })).toBeNull();
    expect(mocks.fetchCorrection).toHaveBeenCalledTimes(2);
    expect(mocks.executeCorrection).toHaveBeenCalledTimes(1);
  });

  it('aborts and ignores a preview response from a closed workflow', async () => {
    const oldPreview = deferred<AdminSubscriptionCorrectionPreview>();
    const currentPreview = deferred<AdminSubscriptionCorrectionPreview>();
    mocks.fetchSubscriptions.mockResolvedValue(
      pageOf([subscription(1, 'OldSubscriber'), subscription(2, 'CurrentSubscriber')]),
    );
    mocks.previewCorrection
      .mockReturnValueOnce(oldPreview.promise)
      .mockReturnValueOnce(currentPreview.promise);

    render(<UserSubscriptionManagePage />);
    expect(await screen.findByText('OldSubscriber')).toBeInTheDocument();
    let dialog = await openWorkflow('OldSubscriber');
    enterValidDraft(dialog);
    fireEvent.click(within(dialog).getByRole('button', { name: '미리보기' }));
    const oldSignal = mocks.previewCorrection.mock.calls[0][1] as AbortSignal;
    const closeButtons = within(dialog).getAllByRole('button', { name: '닫기' });
    fireEvent.click(closeButtons[closeButtons.length - 1]!);
    expect(oldSignal.aborted).toBe(true);

    dialog = await openWorkflow('CurrentSubscriber');
    enterValidDraft(dialog);
    fireEvent.click(within(dialog).getByRole('button', { name: '미리보기' }));
    await act(async () =>
      currentPreview.resolve(correctionPreview({ targetPlanName: 'CURRENT TARGET' })),
    );
    expect(await within(dialog).findByText('CURRENT TARGET')).toBeInTheDocument();

    await act(async () => oldPreview.resolve(correctionPreview({ targetPlanName: 'OLD TARGET' })));
    expect(within(dialog).getByText('CURRENT TARGET')).toBeInTheDocument();
    expect(within(dialog).queryByText('OLD TARGET')).not.toBeInTheDocument();
  });
});

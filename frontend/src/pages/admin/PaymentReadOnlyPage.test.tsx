import { act, fireEvent, render, screen, waitFor } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import type {
  AdminPaymentEntitlementCorrectionPreview,
  AdminPaymentReceipt,
  AdminPaymentReconciliationIncident,
  AdminPaymentRefundPreview,
  AdminPaymentSettlementImportResult,
} from '@/api/admin';
import PaymentReadOnlyPage from '@/pages/admin/PaymentReadOnlyPage';
import type { PageInfo, PagedResponse } from '@/types';

const mocks = vi.hoisted(() => ({
  approveAdminPaymentEntitlementCorrection: vi.fn(),
  approveAdminPaymentRefund: vi.fn(),
  createAdminPaymentEntitlementCorrection: vi.fn(),
  createAdminPaymentRefund: vi.fn(),
  executeAdminPaymentEntitlementCorrection: vi.fn(),
  executeAdminPaymentRefund: vi.fn(),
  fetchAdminBillingAgreements: vi.fn(),
  fetchAdminPaymentEntitlementCorrections: vi.fn(),
  fetchAdminPaymentOperationAuditLogs: vi.fn(),
  fetchAdminPaymentOrders: vi.fn(),
  fetchAdminPaymentReceipts: vi.fn(),
  fetchAdminPaymentReconciliationIncidents: vi.fn(),
  fetchAdminPaymentRefundPreview: vi.fn(),
  fetchAdminPaymentRefunds: vi.fn(),
  fetchAdminPaymentSettlements: vi.fn(),
  fetchAdminSubscriptionPayments: vi.fn(),
  ignoreAdminPaymentSettlement: vi.fn(),
  importAdminPaymentSettlements: vi.fn(),
  previewAdminPaymentEntitlementCorrection: vi.fn(),
  reconcileAdminPaymentSettlements: vi.fn(),
  updateAdminPaymentReconciliationIncidentStatus: vi.fn(),
  fetchAdminSubscriptionPlans: vi.fn(),
  showToast: vi.fn(),
}));

vi.mock('@/api/admin', () => ({
  approveAdminPaymentEntitlementCorrection: (...args: unknown[]) =>
    mocks.approveAdminPaymentEntitlementCorrection(...args),
  approveAdminPaymentRefund: (...args: unknown[]) => mocks.approveAdminPaymentRefund(...args),
  createAdminPaymentEntitlementCorrection: (...args: unknown[]) =>
    mocks.createAdminPaymentEntitlementCorrection(...args),
  createAdminPaymentRefund: (...args: unknown[]) => mocks.createAdminPaymentRefund(...args),
  executeAdminPaymentEntitlementCorrection: (...args: unknown[]) =>
    mocks.executeAdminPaymentEntitlementCorrection(...args),
  executeAdminPaymentRefund: (...args: unknown[]) => mocks.executeAdminPaymentRefund(...args),
  fetchAdminBillingAgreements: (...args: unknown[]) => mocks.fetchAdminBillingAgreements(...args),
  fetchAdminPaymentEntitlementCorrections: (...args: unknown[]) =>
    mocks.fetchAdminPaymentEntitlementCorrections(...args),
  fetchAdminPaymentOperationAuditLogs: (...args: unknown[]) =>
    mocks.fetchAdminPaymentOperationAuditLogs(...args),
  fetchAdminPaymentOrders: (...args: unknown[]) => mocks.fetchAdminPaymentOrders(...args),
  fetchAdminPaymentReceipts: (...args: unknown[]) => mocks.fetchAdminPaymentReceipts(...args),
  fetchAdminPaymentReconciliationIncidents: (...args: unknown[]) =>
    mocks.fetchAdminPaymentReconciliationIncidents(...args),
  fetchAdminPaymentRefundPreview: (...args: unknown[]) =>
    mocks.fetchAdminPaymentRefundPreview(...args),
  fetchAdminPaymentRefunds: (...args: unknown[]) => mocks.fetchAdminPaymentRefunds(...args),
  fetchAdminPaymentSettlements: (...args: unknown[]) => mocks.fetchAdminPaymentSettlements(...args),
  fetchAdminSubscriptionPayments: (...args: unknown[]) =>
    mocks.fetchAdminSubscriptionPayments(...args),
  ignoreAdminPaymentSettlement: (...args: unknown[]) => mocks.ignoreAdminPaymentSettlement(...args),
  importAdminPaymentSettlements: (...args: unknown[]) =>
    mocks.importAdminPaymentSettlements(...args),
  previewAdminPaymentEntitlementCorrection: (...args: unknown[]) =>
    mocks.previewAdminPaymentEntitlementCorrection(...args),
  reconcileAdminPaymentSettlements: (...args: unknown[]) =>
    mocks.reconcileAdminPaymentSettlements(...args),
  updateAdminPaymentReconciliationIncidentStatus: (...args: unknown[]) =>
    mocks.updateAdminPaymentReconciliationIncidentStatus(...args),
}));

vi.mock('@/api/subscriptions', () => ({
  fetchAdminSubscriptionPlans: (...args: unknown[]) => mocks.fetchAdminSubscriptionPlans(...args),
}));

vi.mock('@/store/toastStore', () => ({
  useToastStore: (selector: (state: { show: typeof mocks.showToast }) => unknown) =>
    selector({ show: mocks.showToast }),
}));

vi.mock('@/components/ui/Pagination', () => ({
  default: ({
    pageInfo,
    currentPage,
    onPageChange,
  }: {
    pageInfo: PageInfo;
    currentPage: number;
    onPageChange: (page: number) => void;
  }) => (
    <div>
      <span data-testid="pagination-state">{`${currentPage}:${pageInfo.total}`}</span>
      <button onClick={() => onPageChange(currentPage + 1)} type="button">
        next page
      </button>
    </div>
  ),
}));

interface Deferred<T> {
  promise: Promise<T>;
  resolve: (value: T) => void;
  reject: (reason: unknown) => void;
}

function deferred<T>(): Deferred<T> {
  let resolve!: (value: T) => void;
  let reject!: (reason: unknown) => void;
  const promise = new Promise<T>((resolvePromise, rejectPromise) => {
    resolve = resolvePromise;
    reject = rejectPromise;
  });
  return { promise, resolve, reject };
}

function emptyPage<T>(page: number, total: number): PagedResponse<T> {
  return {
    dataList: [],
    pageInfo: {
      page,
      size: 20,
      total,
      start: total > 0 ? 1 : 0,
      end: total > 0 ? Math.min(total, 20) : 0,
      prev: page > 1,
      next: total > page * 20,
    },
  };
}

const incident: AdminPaymentReconciliationIncident = {
  id: 7,
  dedupeKey: 'incident-7',
  issueType: 'STATUS_MISMATCH',
  status: 'OPEN',
  severity: 'WARNING',
  paymentOrderId: 10,
  billingAgreementId: null,
  userId: 3,
  userNickname: 'operator-test',
  orderId: 'ORDER-7',
  provider: 'TOSS',
  purpose: 'RENEWAL',
  localStatus: 'DONE',
  providerStatus: 'CANCELED',
  localAmount: 9900,
  providerAmount: 9900,
  providerReference: 'REF-4D124B1139B2',
  failureCode: null,
  failureMessage: null,
  occurrenceCount: 1,
  firstDetectedAt: '2026-07-16T00:00:00',
  lastDetectedAt: '2026-07-16T00:00:00',
  notifiedAt: null,
  resolvedAt: null,
  resolutionNote: null,
  createdAt: '2026-07-16T00:00:00',
};

const settlementResult: AdminPaymentSettlementImportResult = {
  importBatchKey: 'BATCH-1',
  totalRows: 1,
  importedRows: 1,
  skippedDuplicateRows: 0,
  failedRows: 0,
  statusCounts: { MATCHED: 1 },
  errors: [],
};

const refundPreview: AdminPaymentRefundPreview = {
  subscriptionPaymentId: 41,
  paymentOrderId: 31,
  orderId: 'ORDER-41',
  userId: 3,
  userNickname: 'refund-user',
  provider: 'TOSS',
  originalAmount: 9900,
  alreadyRefundedOrReservedAmount: 0,
  refundableAmount: 9900,
  providerReference: 'REF-4D124B1139B2',
  refundable: true,
  reason: null,
};

const correctionPreview: AdminPaymentEntitlementCorrectionPreview = {
  paymentRefundId: 51,
  refundStatus: 'SUCCEEDED',
  userId: 3,
  userNickname: 'correction-user',
  userSubscriptionId: 61,
  currentSubscriptionId: 10,
  currentPlanName: 'STANDARD',
  currentBillingCycle: 'MONTHLY',
  currentStatus: 'ACTIVE',
  currentExpiresAt: '2026-08-16',
  currentPendingSubscriptionId: null,
  currentPendingPlanName: null,
  currentPendingBillingCycle: null,
  targetSubscriptionId: 20,
  targetPlanName: 'DELUXE',
  targetBillingCycle: 'MONTHLY',
  targetStatus: 'EXPIRED',
  targetExpiresAt: '2026-07-16',
  clearPendingChange: true,
  cancelBillingAgreement: true,
  currentBillingAgreementStatus: 'ACTIVE',
  targetBillingAgreementStatus: 'CANCELLED',
  executable: true,
  reason: null,
};

function incidentPage(): PagedResponse<AdminPaymentReconciliationIncident> {
  return {
    dataList: [incident],
    pageInfo: {
      page: 1,
      size: 20,
      total: 1,
      start: 1,
      end: 1,
      prev: false,
      next: false,
    },
  };
}

function receiptPage(receipts: AdminPaymentReceipt[]): PagedResponse<AdminPaymentReceipt> {
  return {
    dataList: receipts,
    pageInfo: {
      page: 1,
      size: 20,
      total: receipts.length,
      start: receipts.length > 0 ? 1 : 0,
      end: receipts.length,
      prev: false,
      next: false,
    },
  };
}

function setDefaultReadResults() {
  mocks.fetchAdminPaymentOrders.mockResolvedValue(emptyPage(1, 0));
  mocks.fetchAdminBillingAgreements.mockResolvedValue(emptyPage(1, 0));
  mocks.fetchAdminSubscriptionPayments.mockResolvedValue(emptyPage(1, 0));
  mocks.fetchAdminPaymentReconciliationIncidents.mockResolvedValue(emptyPage(1, 0));
  mocks.fetchAdminPaymentReceipts.mockResolvedValue(emptyPage(1, 0));
  mocks.fetchAdminPaymentOperationAuditLogs.mockResolvedValue(emptyPage(1, 0));
  mocks.fetchAdminPaymentSettlements.mockResolvedValue(emptyPage(1, 0));
  mocks.fetchAdminPaymentRefunds.mockResolvedValue(emptyPage(1, 0));
  mocks.fetchAdminPaymentEntitlementCorrections.mockResolvedValue(emptyPage(1, 0));
  mocks.fetchAdminSubscriptionPlans.mockResolvedValue([]);
}

describe('PaymentReadOnlyPage latest-request-wins', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
    for (const mock of Object.values(mocks)) {
      if (typeof mock === 'function' && 'mockReset' in mock) {
        mock.mockReset();
      }
    }
    setDefaultReadResults();
  });

  it('keeps the latest tab after an older page request resolves last', async () => {
    const oldPageRequest = deferred<PagedResponse<never>>();
    const currentTabRequest = deferred<PagedResponse<never>>();
    mocks.fetchAdminPaymentOrders
      .mockResolvedValueOnce(emptyPage(1, 40))
      .mockReturnValueOnce(oldPageRequest.promise);
    mocks.fetchAdminBillingAgreements.mockReturnValueOnce(currentTabRequest.promise);

    render(<PaymentReadOnlyPage />);
    expect(await screen.findByTestId('pagination-state')).toHaveTextContent('1:40');

    fireEvent.click(screen.getByRole('button', { name: 'next page' }));
    await waitFor(() => expect(mocks.fetchAdminPaymentOrders).toHaveBeenCalledTimes(2));
    fireEvent.click(screen.getByRole('button', { name: '자동결제' }));
    await waitFor(() => expect(mocks.fetchAdminBillingAgreements).toHaveBeenCalledTimes(1));

    await act(async () => {
      currentTabRequest.resolve(emptyPage(1, 60));
      await currentTabRequest.promise;
    });
    expect(await screen.findByTestId('pagination-state')).toHaveTextContent('1:60');

    await act(async () => {
      oldPageRequest.resolve(emptyPage(2, 100));
      await oldPageRequest.promise;
    });

    expect(screen.getByTestId('pagination-state')).toHaveTextContent('1:60');
    expect(screen.queryByText('결제 정보를 불러오지 못했습니다.')).not.toBeInTheDocument();
    expect((mocks.fetchAdminPaymentOrders.mock.calls[1][2] as AbortSignal).aborted).toBe(true);
  });

  it('ignores an older filter failure after the newer filter succeeds', async () => {
    const oldFilterRequest = deferred<PagedResponse<never>>();
    const currentFilterRequest = deferred<PagedResponse<never>>();
    mocks.fetchAdminPaymentReconciliationIncidents
      .mockReturnValueOnce(oldFilterRequest.promise)
      .mockReturnValueOnce(currentFilterRequest.promise);

    render(<PaymentReadOnlyPage />);
    await waitFor(() => expect(mocks.fetchAdminPaymentOrders).toHaveBeenCalledTimes(1));
    fireEvent.click(screen.getByRole('button', { name: '대사 Incident' }));
    await waitFor(() =>
      expect(mocks.fetchAdminPaymentReconciliationIncidents).toHaveBeenCalledTimes(1),
    );

    fireEvent.change(screen.getByRole('combobox'), { target: { value: 'RESOLVED' } });
    await waitFor(() =>
      expect(mocks.fetchAdminPaymentReconciliationIncidents).toHaveBeenCalledTimes(2),
    );

    await act(async () => {
      currentFilterRequest.resolve(emptyPage(1, 40));
      await currentFilterRequest.promise;
    });
    expect(await screen.findByTestId('pagination-state')).toHaveTextContent('1:40');

    await act(async () => {
      oldFilterRequest.reject(new Error('stale filter failure'));
      await oldFilterRequest.promise.catch(() => undefined);
    });

    expect(screen.getByTestId('pagination-state')).toHaveTextContent('1:40');
    expect(screen.queryByText('결제 정보를 불러오지 못했습니다.')).not.toBeInTheDocument();
    expect(screen.queryByText('불러오는 중...')).not.toBeInTheDocument();
  });

  it('shows the active request failure and finalizes its loading state', async () => {
    mocks.fetchAdminPaymentOrders.mockRejectedValueOnce(new Error('active failure'));

    render(<PaymentReadOnlyPage />);

    expect(await screen.findByText('결제 정보를 불러오지 못했습니다.')).toBeInTheDocument();
    expect(screen.queryByText('불러오는 중...')).not.toBeInTheDocument();
  });

  it('opens valid HTTPS receipt URLs and renders unsafe retained URLs as text only', async () => {
    const baseReceipt: AdminPaymentReceipt = {
      id: 1,
      userId: 3,
      userNickname: 'receipt-user',
      paymentOrderId: 10,
      orderId: 'ORDER-RECEIPT-1',
      subscriptionPaymentId: 20,
      provider: 'TOSS_BILLING',
      type: 'PAYMENT_RECEIPT',
      status: 'ISSUED',
      providerReference: 'REF-PROVIDER',
      receiptReference: null,
      receiptUrl: 'https://receipts.example.com/r/1',
      issuedAt: '2026-07-16T10:00:00',
      cancelledAt: null,
      createdAt: '2026-07-16T10:00:00',
    };
    mocks.fetchAdminPaymentReceipts.mockResolvedValueOnce(
      receiptPage([
        baseReceipt,
        {
          ...baseReceipt,
          id: 2,
          orderId: 'ORDER-RECEIPT-2',
          receiptReference: 'REF-UNSAFE-RECEIPT',
          receiptUrl: "javascript:alert('provider-payment-key')",
        },
      ]),
    );

    render(<PaymentReadOnlyPage />);
    await waitFor(() => expect(mocks.fetchAdminPaymentOrders).toHaveBeenCalledTimes(1));
    fireEvent.click(screen.getByRole('button', { name: '영수증' }));

    const openLink = await screen.findByRole('link', { name: '열기' });
    expect(openLink).toHaveAttribute('href', 'https://receipts.example.com/r/1');
    const unsafeReference = screen.getByText('REF-UNSAFE-RECEIPT');
    expect(unsafeReference.closest('a')).toBeNull();
    expect(screen.getAllByRole('link', { name: '열기' })).toHaveLength(1);
  });

  it('reloads the current view once after an incident mutation', async () => {
    mocks.fetchAdminPaymentReconciliationIncidents
      .mockResolvedValueOnce(incidentPage())
      .mockResolvedValueOnce(incidentPage());
    mocks.updateAdminPaymentReconciliationIncidentStatus.mockResolvedValueOnce(incident);

    render(<PaymentReadOnlyPage />);
    await waitFor(() => expect(mocks.fetchAdminPaymentOrders).toHaveBeenCalledTimes(1));
    fireEvent.click(screen.getByRole('button', { name: '대사 Incident' }));

    expect(await screen.findByText('STATUS_MISMATCH')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: '저장' }));

    await waitFor(() =>
      expect(mocks.updateAdminPaymentReconciliationIncidentStatus).toHaveBeenCalledTimes(1),
    );
    await waitFor(() =>
      expect(mocks.fetchAdminPaymentReconciliationIncidents).toHaveBeenCalledTimes(2),
    );
    expect(mocks.fetchAdminPaymentReconciliationIncidents).toHaveBeenCalledTimes(2);
  });

  it('imports a settlement file once with confirmation and refreshes only the settlement view', async () => {
    const mutation = deferred<AdminPaymentSettlementImportResult>();
    const confirm = vi.spyOn(window, 'confirm').mockReturnValue(true);
    mocks.importAdminPaymentSettlements.mockReturnValueOnce(mutation.promise);

    const view = render(<PaymentReadOnlyPage />);
    await waitFor(() => expect(mocks.fetchAdminPaymentOrders).toHaveBeenCalledTimes(1));
    fireEvent.click(screen.getByRole('button', { name: '정산' }));
    await waitFor(() => expect(mocks.fetchAdminPaymentSettlements).toHaveBeenCalledTimes(1));

    const file = new File(['orderId,amount'], 'settlement.csv', { type: 'text/csv' });
    const fileInput = view.container.querySelector<HTMLInputElement>('input[type="file"]');
    expect(fileInput).not.toBeNull();
    fireEvent.change(fileInput!, { target: { files: [file] } });
    fireEvent.change(screen.getByPlaceholderText('정산 import 근거'), {
      target: { value: 'daily settlement' },
    });
    const importButton = screen.getByRole('button', { name: '정산 import' });
    fireEvent.click(importButton);
    fireEvent.click(importButton);

    expect(confirm).toHaveBeenCalledTimes(1);
    expect(mocks.importAdminPaymentSettlements).toHaveBeenCalledTimes(1);
    expect(mocks.importAdminPaymentSettlements).toHaveBeenCalledWith(file, 'daily settlement');
    expect(importButton).toBeDisabled();

    await act(async () => mutation.resolve(settlementResult));
    await waitFor(() => expect(mocks.fetchAdminPaymentSettlements).toHaveBeenCalledTimes(2));
    expect(mocks.fetchAdminPaymentSettlements).toHaveBeenCalledTimes(2);
    expect(mocks.fetchAdminPaymentOrders).toHaveBeenCalledTimes(1);
  });

  it('reports a settlement reconciliation failure without refreshing the current view', async () => {
    vi.spyOn(window, 'confirm').mockReturnValue(true);
    mocks.reconcileAdminPaymentSettlements.mockRejectedValueOnce(new Error('reconcile failed'));

    render(<PaymentReadOnlyPage />);
    await waitFor(() => expect(mocks.fetchAdminPaymentOrders).toHaveBeenCalledTimes(1));
    fireEvent.click(screen.getByRole('button', { name: '정산' }));
    await waitFor(() => expect(mocks.fetchAdminPaymentSettlements).toHaveBeenCalledTimes(1));
    fireEvent.click(screen.getByRole('button', { name: '누락 후보 확인' }));

    await waitFor(() =>
      expect(mocks.showToast).toHaveBeenCalledWith('error', '정산 누락 후보 확인에 실패했습니다.'),
    );
    expect(mocks.fetchAdminPaymentSettlements).toHaveBeenCalledTimes(1);
  });

  it('creates one confirmed refund request with the preview local payment id and refreshes once', async () => {
    const mutation = deferred<unknown>();
    vi.spyOn(window, 'confirm').mockReturnValue(true);
    mocks.fetchAdminPaymentRefundPreview.mockResolvedValueOnce(refundPreview);
    mocks.createAdminPaymentRefund.mockReturnValueOnce(mutation.promise);

    render(<PaymentReadOnlyPage />);
    await waitFor(() => expect(mocks.fetchAdminPaymentOrders).toHaveBeenCalledTimes(1));
    fireEvent.click(screen.getByRole('button', { name: '환불' }));
    await waitFor(() => expect(mocks.fetchAdminPaymentRefunds).toHaveBeenCalledTimes(1));
    fireEvent.change(screen.getByPlaceholderText('subscriptionPaymentId'), {
      target: { value: '41' },
    });
    fireEvent.click(screen.getByRole('button', { name: '환불 미리보기' }));
    await waitFor(() => expect(mocks.fetchAdminPaymentRefundPreview).toHaveBeenCalledWith(41));
    fireEvent.change(screen.getByPlaceholderText('고객 문의/incident/승인 근거'), {
      target: { value: 'customer request 123' },
    });

    const requestButton = await screen.findByRole('button', { name: '환불 요청 생성' });
    fireEvent.click(requestButton);
    fireEvent.click(requestButton);
    expect(mocks.createAdminPaymentRefund).toHaveBeenCalledTimes(1);
    expect(mocks.createAdminPaymentRefund).toHaveBeenCalledWith({
      subscriptionPaymentId: 41,
      amount: 9900,
      reasonCode: 'CUSTOMER_REQUEST',
      reasonNote: 'customer request 123',
    });
    expect(requestButton).toBeDisabled();

    await act(async () => mutation.resolve(undefined));
    await waitFor(() => expect(mocks.fetchAdminPaymentRefunds).toHaveBeenCalledTimes(2));
    expect(mocks.fetchAdminPaymentRefunds).toHaveBeenCalledTimes(2);
  });

  it('previews and creates one confirmed entitlement correction using local ids', async () => {
    const mutation = deferred<unknown>();
    vi.spyOn(window, 'confirm').mockReturnValue(true);
    mocks.fetchAdminSubscriptionPlans.mockResolvedValueOnce([
      {
        id: 20,
        name: 'DELUXE',
        description: 'Deluxe plan',
        userType: 'INDIVIDUAL',
        priceMonthly: 19900,
        priceYearly: 199000,
        downloadPerDay: 20,
        maxWhitelistChannels: 3,
        maxPlaylists: 10,
        isActive: true,
      },
    ]);
    mocks.previewAdminPaymentEntitlementCorrection.mockResolvedValueOnce(correctionPreview);
    mocks.createAdminPaymentEntitlementCorrection.mockReturnValueOnce(mutation.promise);

    render(<PaymentReadOnlyPage />);
    await waitFor(() => expect(mocks.fetchAdminPaymentOrders).toHaveBeenCalledTimes(1));
    fireEvent.click(screen.getByRole('button', { name: '권한 보정' }));
    await waitFor(() =>
      expect(mocks.fetchAdminPaymentEntitlementCorrections).toHaveBeenCalledTimes(1),
    );
    fireEvent.change(screen.getByLabelText('환불 ID'), { target: { value: '51' } });
    fireEvent.change(screen.getByLabelText('대상 플랜'), { target: { value: '20' } });
    fireEvent.change(screen.getByPlaceholderText('환불 후 권한 보정 근거'), {
      target: { value: 'refund incident 51' },
    });
    fireEvent.click(screen.getByRole('button', { name: '권한 보정 미리보기' }));

    const expectedRequest = {
      paymentRefundId: 51,
      targetSubscriptionId: 20,
      targetBillingCycle: 'MONTHLY',
      targetStatus: 'EXPIRED',
      targetExpiresAt: expect.any(String),
      clearPendingChange: true,
      cancelBillingAgreement: true,
      reasonNote: 'refund incident 51',
    };
    await waitFor(() =>
      expect(mocks.previewAdminPaymentEntitlementCorrection).toHaveBeenCalledWith(expectedRequest),
    );

    const requestButton = await screen.findByRole('button', { name: '권한 보정 요청 생성' });
    fireEvent.click(requestButton);
    fireEvent.click(requestButton);
    expect(mocks.createAdminPaymentEntitlementCorrection).toHaveBeenCalledTimes(1);
    expect(mocks.createAdminPaymentEntitlementCorrection).toHaveBeenCalledWith(expectedRequest);
    expect(requestButton).toBeDisabled();

    await act(async () => mutation.resolve(undefined));
    await waitFor(() =>
      expect(mocks.fetchAdminPaymentEntitlementCorrections).toHaveBeenCalledTimes(2),
    );
    expect(mocks.fetchAdminPaymentEntitlementCorrections).toHaveBeenCalledTimes(2);
  });
});

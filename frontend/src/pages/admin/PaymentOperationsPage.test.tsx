import { act, fireEvent, render, screen, waitFor, within } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import type {
  AdminPaymentEntitlementCorrection,
  AdminPaymentEntitlementCorrectionPreview,
  AdminPaymentReceipt,
  AdminPaymentReconciliationIncident,
  AdminPaymentRefund,
  AdminPaymentRefundPreview,
  AdminPaymentSettlement,
  AdminPaymentSettlementImportAttempt,
  AdminPaymentSettlementImportResult,
} from '@/api/admin';
import PaymentOperationsPage from '@/pages/admin/PaymentOperationsPage';
import type { PageInfo, PagedResponse } from '@/types';
import { SETTLEMENT_IMPORT_ATTEMPT_STORAGE_KEY } from '@/utils/settlementImportAttempt';

const mocks = vi.hoisted(() => ({
  approveAdminPaymentEntitlementCorrection: vi.fn(),
  approveAdminPaymentRefund: vi.fn(),
  createAdminPaymentEntitlementCorrection: vi.fn(),
  createAdminPaymentRefund: vi.fn(),
  executeAdminPaymentEntitlementCorrection: vi.fn(),
  executeAdminPaymentRefund: vi.fn(),
  fetchAdminBillingAgreements: vi.fn(),
  fetchAdminPaymentEntitlementCorrections: vi.fn(),
  fetchAdminPaymentEntitlementCorrection: vi.fn(),
  fetchAdminPaymentOperationAuditLogs: vi.fn(),
  fetchAdminPaymentOrders: vi.fn(),
  fetchAdminPaymentReceipts: vi.fn(),
  fetchAdminPaymentReconciliationIncidents: vi.fn(),
  fetchAdminPaymentRefundPreview: vi.fn(),
  fetchAdminPaymentRefund: vi.fn(),
  fetchAdminPaymentRefunds: vi.fn(),
  fetchAdminPaymentSettlements: vi.fn(),
  fetchAdminSubscriptionPayments: vi.fn(),
  ignoreAdminPaymentSettlement: vi.fn(),
  importAdminPaymentSettlements: vi.fn(),
  previewAdminPaymentEntitlementCorrection: vi.fn(),
  reconcileAdminPaymentSettlements: vi.fn(),
  recoverAdminPaymentSettlementImportAttempt: vi.fn(),
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
  fetchAdminPaymentEntitlementCorrection: (...args: unknown[]) =>
    mocks.fetchAdminPaymentEntitlementCorrection(...args),
  fetchAdminPaymentOperationAuditLogs: (...args: unknown[]) =>
    mocks.fetchAdminPaymentOperationAuditLogs(...args),
  fetchAdminPaymentOrders: (...args: unknown[]) => mocks.fetchAdminPaymentOrders(...args),
  fetchAdminPaymentReceipts: (...args: unknown[]) => mocks.fetchAdminPaymentReceipts(...args),
  fetchAdminPaymentReconciliationIncidents: (...args: unknown[]) =>
    mocks.fetchAdminPaymentReconciliationIncidents(...args),
  fetchAdminPaymentRefundPreview: (...args: unknown[]) =>
    mocks.fetchAdminPaymentRefundPreview(...args),
  fetchAdminPaymentRefund: (...args: unknown[]) => mocks.fetchAdminPaymentRefund(...args),
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
  recoverAdminPaymentSettlementImportAttempt: (...args: unknown[]) =>
    mocks.recoverAdminPaymentSettlementImportAttempt(...args),
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

function pageWith<T>(items: T[]): PagedResponse<T> {
  return {
    dataList: items,
    pageInfo: {
      page: 1,
      size: 20,
      total: items.length,
      start: items.length > 0 ? 1 : 0,
      end: items.length,
      prev: false,
      next: false,
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
  omittedErrorCount: 0,
};

const firstSettlementImportKey = '11111111-1111-4111-8111-111111111111';
const secondSettlementImportKey = '22222222-2222-4222-8222-222222222222';

const processingSettlementAttempt: AdminPaymentSettlementImportAttempt = {
  attemptId: 81,
  importBatchKey: 'ATS-SETTLE-ATTEMPT-81',
  actorUserId: 99,
  state: 'PROCESSING',
  totalRows: 0,
  importedRows: 0,
  skippedDuplicateRows: 0,
  failedRows: 0,
  operatorNote: 'retry note',
  failureCode: null,
  completedAt: null,
  createdAt: '2026-08-12T09:00:00',
  updatedAt: '2026-08-12T09:00:00',
};

const completedSettlementAttempt: AdminPaymentSettlementImportAttempt = {
  ...processingSettlementAttempt,
  state: 'COMPLETED',
  completedAt: '2026-08-12T09:05:00',
  updatedAt: '2026-08-12T09:05:00',
};

const mismatchedSettlement: AdminPaymentSettlement = {
  id: 17,
  source: 'CSV_MANUAL',
  provider: 'TOSS',
  status: 'MISMATCHED',
  orderId: 'ORDER-SETTLEMENT-17',
  providerReference: 'REF-SETTLEMENT-17',
  providerSettlementReference: null,
  paymentOrderId: 1,
  subscriptionPaymentId: 3,
  userId: 7,
  userNickname: 'settlement-user',
  grossAmount: 9900,
  refundAmount: 0,
  feeAmount: 100,
  vatAmount: 10,
  netSettlementAmount: 9790,
  currency: 'KRW',
  settlementBaseDate: '2026-08-09',
  settlementPayoutDate: null,
  providerStatus: 'DONE',
  mismatchReason: 'fee mismatch',
  sourceFileName: 'safe-synthetic.csv',
  sourceRowNumber: 2,
  operatorNote: null,
  ignoredBy: null,
  ignoredAt: null,
  reconciledAt: '2026-08-09T00:00:00',
  createdAt: '2026-08-09T00:00:00',
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

function refund(
  status: AdminPaymentRefund['status'] = 'APPROVED',
  overrides: Partial<AdminPaymentRefund> = {},
): AdminPaymentRefund {
  return {
    id: 51,
    subscriptionPaymentId: 41,
    paymentOrderId: 31,
    orderId: 'ORDER-REFUND-51',
    userId: 3,
    userNickname: 'refund-user',
    provider: 'TOSS',
    status,
    amount: 9900,
    currency: 'KRW',
    reasonCode: 'CUSTOMER_REQUEST',
    reasonNote: 'support ticket',
    idempotencyKey: 'ATS-REFUND-51',
    providerReference: 'REF-PAYMENT-51',
    providerRefundReference: status === 'SUCCEEDED' ? 'REF-REFUND-51' : null,
    failureCode: status === 'FAILED' ? 'PROVIDER_REJECTED' : null,
    failureMessage: status === 'FAILED' ? 'Provider rejected the refund.' : null,
    requestedById: 1,
    requestedByEmail: 'admin@example.com',
    approvedById: status === 'REQUESTED' ? null : 1,
    approvedByEmail: status === 'REQUESTED' ? null : 'admin@example.com',
    executedById: ['SUCCEEDED', 'FAILED', 'CANCELLED'].includes(status) ? 1 : null,
    executedByEmail: ['SUCCEEDED', 'FAILED', 'CANCELLED'].includes(status)
      ? 'admin@example.com'
      : null,
    approvedAt: status === 'REQUESTED' ? null : '2026-08-12T09:00:00',
    executedAt: ['SUCCEEDED', 'FAILED', 'CANCELLED'].includes(status)
      ? '2026-08-12T09:05:00'
      : null,
    createdAt: '2026-08-12T08:00:00',
    updatedAt: '2026-08-12T09:05:00',
    ...overrides,
  };
}

function correction(
  status: AdminPaymentEntitlementCorrection['status'] = 'APPROVED',
  overrides: Partial<AdminPaymentEntitlementCorrection> = {},
): AdminPaymentEntitlementCorrection {
  return {
    id: 61,
    paymentRefundId: 51,
    subscriptionPaymentId: 41,
    paymentOrderId: 31,
    orderId: 'ORDER-CORRECTION-61',
    userSubscriptionId: 71,
    userId: 3,
    userNickname: 'correction-user',
    provider: 'TOSS',
    status,
    action: 'APPLY_REFUND_ENTITLEMENT',
    beforeSubscriptionId: 10,
    beforePlanName: 'STANDARD',
    beforeBillingCycle: 'MONTHLY',
    beforeStatus: 'ACTIVE',
    beforeExpiresAt: '2026-09-12',
    beforePendingSubscriptionId: null,
    beforePendingPlanName: null,
    beforePendingBillingCycle: null,
    targetSubscriptionId: 20,
    targetPlanName: 'DELUXE',
    targetBillingCycle: 'MONTHLY',
    targetStatus: 'EXPIRED',
    targetExpiresAt: '2026-08-12',
    clearPendingChange: true,
    cancelBillingAgreement: true,
    beforeBillingAgreementStatus: 'ACTIVE',
    afterBillingAgreementStatus: status === 'SUCCEEDED' ? 'CANCELLED' : 'ACTIVE',
    reasonNote: 'refund correction',
    failureCode: status === 'FAILED' ? 'CORRECTION_FAILED' : null,
    failureMessage: status === 'FAILED' ? 'Correction failed.' : null,
    requestedById: 1,
    requestedByEmail: 'admin@example.com',
    approvedById: status === 'REQUESTED' ? null : 1,
    approvedByEmail: status === 'REQUESTED' ? null : 'admin@example.com',
    executedById: ['SUCCEEDED', 'FAILED', 'CANCELLED'].includes(status) ? 1 : null,
    executedByEmail: ['SUCCEEDED', 'FAILED', 'CANCELLED'].includes(status)
      ? 'admin@example.com'
      : null,
    approvedAt: status === 'REQUESTED' ? null : '2026-08-12T09:00:00',
    executedAt: ['SUCCEEDED', 'FAILED', 'CANCELLED'].includes(status)
      ? '2026-08-12T09:05:00'
      : null,
    createdAt: '2026-08-12T08:00:00',
    updatedAt: '2026-08-12T09:05:00',
    ...overrides,
  };
}

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
  mocks.fetchAdminPaymentRefund.mockResolvedValue(refund('APPROVED'));
  mocks.fetchAdminPaymentEntitlementCorrection.mockResolvedValue(correction('APPROVED'));
  mocks.fetchAdminSubscriptionPlans.mockResolvedValue([]);
}

function seedSettlementImportAttempt(idempotencyKey: string): string {
  const stored = JSON.stringify({
    version: 1,
    scope: 'ADMIN',
    operation: 'SETTLEMENT_IMPORT',
    idempotencyKey,
  });
  sessionStorage.setItem(SETTLEMENT_IMPORT_ATTEMPT_STORAGE_KEY, stored);
  return stored;
}

async function openRefunds(items: AdminPaymentRefund[]) {
  mocks.fetchAdminPaymentRefunds.mockReset().mockResolvedValue(pageWith(items));
  render(<PaymentOperationsPage />);
  await waitFor(() => expect(mocks.fetchAdminPaymentOrders).toHaveBeenCalledTimes(1));
  fireEvent.click(screen.getByRole('button', { name: '환불' }));
  await waitFor(() => expect(mocks.fetchAdminPaymentRefunds).toHaveBeenCalledTimes(1));
}

async function openCorrections(items: AdminPaymentEntitlementCorrection[]) {
  mocks.fetchAdminPaymentEntitlementCorrections.mockReset().mockResolvedValue(pageWith(items));
  render(<PaymentOperationsPage />);
  await waitFor(() => expect(mocks.fetchAdminPaymentOrders).toHaveBeenCalledTimes(1));
  fireEvent.click(screen.getByRole('button', { name: '권한 보정' }));
  await waitFor(() =>
    expect(mocks.fetchAdminPaymentEntitlementCorrections).toHaveBeenCalledTimes(1),
  );
}

async function openSettlements() {
  const view = render(<PaymentOperationsPage />);
  await waitFor(() => expect(mocks.fetchAdminPaymentOrders).toHaveBeenCalledTimes(1));
  fireEvent.click(screen.getByRole('button', { name: '정산' }));
  await waitFor(() => expect(mocks.fetchAdminPaymentSettlements).toHaveBeenCalledTimes(1));
  return view;
}

function submitSettlementImport(view: ReturnType<typeof render>, file: File, note: string) {
  const fileInput = view.container.querySelector<HTMLInputElement>('input[type="file"]');
  expect(fileInput).not.toBeNull();
  fireEvent.change(fileInput!, { target: { files: [file] } });
  fireEvent.change(screen.getByPlaceholderText('정산 import 근거'), {
    target: { value: note },
  });
  fireEvent.click(screen.getByRole('button', { name: '정산 import' }));
  const dialog = screen.getByRole('dialog', { name: '정산 파일 가져오기' });
  const confirmButton = within(dialog).getByRole('button', { name: '가져오기' });
  fireEvent.click(confirmButton);
  fireEvent.click(confirmButton);
  return fileInput!;
}

function selectSettlementFile(view: ReturnType<typeof render>, file: File) {
  const fileInput = view.container.querySelector<HTMLInputElement>('input[type="file"]');
  expect(fileInput).not.toBeNull();
  fireEvent.change(fileInput!, { target: { files: [file] } });
  return fileInput!;
}

function withDeclaredFileSize(file: File, size: number): File {
  Object.defineProperty(file, 'size', { configurable: true, value: size });
  return file;
}

function executeRefundRow(item: AdminPaymentRefund) {
  vi.spyOn(window, 'prompt').mockReturnValue('환불 실행');
  const row = screen.getByText(item.orderId).closest('tr') as HTMLElement;
  fireEvent.click(within(row).getByRole('button', { name: '실행' }));
  return row;
}

function executeCorrectionRow(item: AdminPaymentEntitlementCorrection) {
  vi.spyOn(window, 'prompt').mockReturnValue('권한 보정 실행');
  const row = screen.getByText(item.userNickname).closest('tr') as HTMLElement;
  fireEvent.click(within(row).getByRole('button', { name: '실행' }));
  return row;
}

describe('PaymentOperationsPage latest-request-wins', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
    for (const mock of Object.values(mocks)) {
      if (typeof mock === 'function' && 'mockReset' in mock) {
        mock.mockReset();
      }
    }
    setDefaultReadResults();
  });

  it('shows the settlement note security hint and keeps the optional note as plain text', async () => {
    await openSettlements();

    const hint = screen.getByText('개인정보, 인증정보, 결제 키 등 민감정보를 입력하지 마세요.');
    const note = screen.getByPlaceholderText('정산 import 근거');
    const htmlLikeNote = '<strong>operator</strong><img alt="rich note" src="x" />';

    expect(hint).toBeVisible();
    expect(note).toHaveAttribute('aria-describedby', hint.id);
    expect(note).not.toBeRequired();
    expect(note).toHaveAttribute('maxlength', '500');

    fireEvent.change(note, { target: { value: htmlLikeNote } });

    expect(note).toHaveValue(htmlLikeNote);
    expect(note.querySelector('*')).toBeNull();
    expect(screen.queryByAltText('rich note')).not.toBeInTheDocument();
  });

  it.each([
    [
      'missing filename',
      () => new File(['x'], '', { type: 'text/csv' }),
      '파일 이름이 있는 정산 CSV를 선택해주세요.',
    ],
    [
      'non-csv extension',
      () => new File(['x'], 'settlements.txt', { type: 'text/csv' }),
      '정산 CSV 파일 이름은 .csv로 끝나야 합니다.',
    ],
    [
      '256-character filename',
      () => new File(['x'], `${'a'.repeat(252)}.csv`, { type: 'text/csv' }),
      '정산 CSV 파일 이름은 255자 이하여야 합니다.',
    ],
    [
      'unsupported MIME',
      () => new File(['x'], 'settlements.csv', { type: 'application/json' }),
      '정산 CSV 파일 형식을 확인해주세요. CSV 형식 또는 빈 MIME만 허용됩니다.',
    ],
    [
      'empty file',
      () => new File([], 'settlements.csv', { type: 'text/csv' }),
      '비어 있지 않은 정산 CSV 파일을 선택해주세요.',
    ],
    [
      'one byte over 5 MiB',
      () =>
        withDeclaredFileSize(new File(['x'], 'settlements.csv', { type: 'text/csv' }), 5_242_881),
      '정산 CSV 파일은 5 MiB(5,242,880 bytes) 이하여야 합니다.',
    ],
  ])('rejects %s before creating an import operation', async (_label, createFile, message) => {
    const randomUUID = vi.spyOn(globalThis.crypto, 'randomUUID');
    const view = await openSettlements();
    const fileInput = selectSettlementFile(view, createFile());

    expect(fileInput).toHaveAttribute('accept', '.csv,text/csv');
    fireEvent.click(screen.getByRole('button', { name: '정산 import' }));

    expect(mocks.showToast).toHaveBeenCalledWith('error', message);
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
    expect(randomUUID).not.toHaveBeenCalled();
    expect(mocks.importAdminPaymentSettlements).not.toHaveBeenCalled();
    expect(sessionStorage.getItem(SETTLEMENT_IMPORT_ATTEMPT_STORAGE_KEY)).toBeNull();
  });

  it('accepts the exact filename and byte limits with a case-insensitive extension', async () => {
    mocks.importAdminPaymentSettlements.mockResolvedValueOnce(settlementResult);
    const view = await openSettlements();
    const file = withDeclaredFileSize(
      new File(['x'], `${'a'.repeat(251)}.CSV`, { type: 'application/vnd.ms-excel' }),
      5_242_880,
    );

    submitSettlementImport(view, file, 'boundary file');

    await waitFor(() => expect(mocks.importAdminPaymentSettlements).toHaveBeenCalledTimes(1));
    expect(mocks.importAdminPaymentSettlements).toHaveBeenCalledWith(
      file,
      expect.stringMatching(/^[0-9a-f-]{36}$/),
      'boundary file',
    );
  });

  it('accepts a blank CSV MIME and leaves server validation authoritative', async () => {
    mocks.importAdminPaymentSettlements.mockResolvedValueOnce(settlementResult);
    const view = await openSettlements();
    const file = new File(['x'], 'blank-mime.csv');

    submitSettlementImport(view, file, 'blank MIME');

    await waitFor(() => expect(mocks.importAdminPaymentSettlements).toHaveBeenCalledTimes(1));
    expect(mocks.importAdminPaymentSettlements).toHaveBeenCalledWith(
      file,
      expect.stringMatching(/^[0-9a-f-]{36}$/),
      'blank MIME',
    );
  });

  it('keeps the latest tab after an older page request resolves last', async () => {
    const oldPageRequest = deferred<PagedResponse<never>>();
    const currentTabRequest = deferred<PagedResponse<never>>();
    mocks.fetchAdminPaymentOrders
      .mockResolvedValueOnce(emptyPage(1, 40))
      .mockReturnValueOnce(oldPageRequest.promise);
    mocks.fetchAdminBillingAgreements.mockReturnValueOnce(currentTabRequest.promise);

    render(<PaymentOperationsPage />);
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

    render(<PaymentOperationsPage />);
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

    render(<PaymentOperationsPage />);

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
      provider: 'TOSS',
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

    render(<PaymentOperationsPage />);
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

    render(<PaymentOperationsPage />);
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

  it('blocks a new settlement import when a pending attempt is stored', async () => {
    const stored = seedSettlementImportAttempt(firstSettlementImportKey);
    const randomUUID = vi.spyOn(globalThis.crypto, 'randomUUID');
    const view = await openSettlements();
    const file = new File(['safe,synthetic,csv'], 'pending-settlement.csv', {
      type: 'text/csv',
    });

    selectSettlementFile(view, file);
    fireEvent.click(screen.getByRole('button', { name: '정산 import' }));

    expect(mocks.showToast).toHaveBeenCalledWith(
      'warning',
      '이전 정산 import 결과를 먼저 복구한 후 새 import를 시작해주세요.',
    );
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
    expect(mocks.importAdminPaymentSettlements).not.toHaveBeenCalled();
    expect(mocks.recoverAdminPaymentSettlementImportAttempt).not.toHaveBeenCalled();
    expect(randomUUID).not.toHaveBeenCalled();
    expect(sessionStorage.getItem(SETTLEMENT_IMPORT_ATTEMPT_STORAGE_KEY)).toBe(stored);
  });

  it('fails closed when a pending attempt appears after the React state snapshot', async () => {
    sessionStorage.removeItem(SETTLEMENT_IMPORT_ATTEMPT_STORAGE_KEY);
    const randomUUID = vi.spyOn(globalThis.crypto, 'randomUUID');
    const view = await openSettlements();
    const file = new File(['safe,synthetic,csv'], 'stale-state-settlement.csv', {
      type: 'text/csv',
    });

    selectSettlementFile(view, file);
    fireEvent.click(screen.getByRole('button', { name: '정산 import' }));
    const dialog = screen.getByRole('dialog', { name: '정산 파일 가져오기' });
    const stored = seedSettlementImportAttempt(firstSettlementImportKey);
    fireEvent.click(within(dialog).getByRole('button', { name: '가져오기' }));

    await waitFor(() =>
      expect(mocks.showToast).toHaveBeenCalledWith(
        'warning',
        '이전 정산 import 결과를 먼저 복구한 후 새 import를 시작해주세요.',
      ),
    );
    expect(mocks.importAdminPaymentSettlements).not.toHaveBeenCalled();
    expect(mocks.recoverAdminPaymentSettlementImportAttempt).not.toHaveBeenCalled();
    expect(randomUUID).not.toHaveBeenCalled();
    expect(sessionStorage.getItem(SETTLEMENT_IMPORT_ATTEMPT_STORAGE_KEY)).toBe(stored);
    expect(screen.getByRole('button', { name: 'import 결과 복구' })).toBeInTheDocument();
  });

  it('rejects corrupt settlement import attempt bytes without recovery or mutation', async () => {
    const corrupt = '{not-json';
    sessionStorage.setItem(SETTLEMENT_IMPORT_ATTEMPT_STORAGE_KEY, corrupt);
    const randomUUID = vi.spyOn(globalThis.crypto, 'randomUUID');
    const view = await openSettlements();
    const file = new File(['safe,synthetic,csv'], 'corrupt-settlement.csv', {
      type: 'text/csv',
    });

    selectSettlementFile(view, file);
    fireEvent.click(screen.getByRole('button', { name: '정산 import' }));

    expect(mocks.showToast).toHaveBeenCalledWith(
      'error',
      '저장된 정산 import 복구 정보가 손상되어 새 import를 시작할 수 없습니다. 브라우저 세션을 지운 후 다시 시도해주세요.',
    );
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
    expect(mocks.importAdminPaymentSettlements).not.toHaveBeenCalled();
    expect(mocks.recoverAdminPaymentSettlementImportAttempt).not.toHaveBeenCalled();
    expect(randomUUID).not.toHaveBeenCalled();
    expect(sessionStorage.getItem(SETTLEMENT_IMPORT_ATTEMPT_STORAGE_KEY)).toBe(corrupt);
  });

  it('starts a new settlement import with a fresh key after terminal recovery', async () => {
    seedSettlementImportAttempt(firstSettlementImportKey);
    mocks.recoverAdminPaymentSettlementImportAttempt.mockResolvedValueOnce(
      completedSettlementAttempt,
    );
    mocks.importAdminPaymentSettlements.mockResolvedValueOnce(settlementResult);
    const randomUUID = vi
      .spyOn(globalThis.crypto, 'randomUUID')
      .mockReturnValue(secondSettlementImportKey);

    const view = await openSettlements();
    fireEvent.click(screen.getByRole('button', { name: 'import 결과 복구' }));

    await waitFor(() =>
      expect(mocks.recoverAdminPaymentSettlementImportAttempt).toHaveBeenCalledTimes(1),
    );
    expect(mocks.recoverAdminPaymentSettlementImportAttempt).toHaveBeenCalledWith(
      firstSettlementImportKey,
    );
    await waitFor(() =>
      expect(sessionStorage.getItem(SETTLEMENT_IMPORT_ATTEMPT_STORAGE_KEY)).toBeNull(),
    );
    expect(mocks.importAdminPaymentSettlements).not.toHaveBeenCalled();

    const file = new File(['orderId,amount'], 'fresh-settlement.csv', { type: 'text/csv' });
    submitSettlementImport(view, file, 'fresh settlement');

    await waitFor(() => expect(mocks.importAdminPaymentSettlements).toHaveBeenCalledTimes(1));
    expect(mocks.importAdminPaymentSettlements).toHaveBeenCalledWith(
      file,
      secondSettlementImportKey,
      'fresh settlement',
    );
    expect(randomUUID).toHaveBeenCalledTimes(1);
    expect(mocks.importAdminPaymentSettlements.mock.calls[0][1]).not.toBe(firstSettlementImportKey);
  });

  it('clears React and DOM file state only after a fully successful import reloads once', async () => {
    const mutation = deferred<AdminPaymentSettlementImportResult>();
    mocks.importAdminPaymentSettlements.mockReturnValueOnce(mutation.promise);

    const view = await openSettlements();
    const file = new File(['orderId,amount'], 'settlement.csv', { type: 'text/csv' });
    const fileInput = submitSettlementImport(view, file, 'daily settlement');

    expect(mocks.importAdminPaymentSettlements).toHaveBeenCalledTimes(1);
    expect(mocks.importAdminPaymentSettlements).toHaveBeenCalledWith(
      file,
      expect.stringMatching(/^[0-9a-f-]{36}$/),
      'daily settlement',
    );
    expect(screen.getByRole('button', { name: 'import 중' })).toBeDisabled();

    await act(async () => mutation.resolve(settlementResult));
    await waitFor(() => expect(mocks.fetchAdminPaymentSettlements).toHaveBeenCalledTimes(2));
    expect(mocks.fetchAdminPaymentSettlements).toHaveBeenCalledTimes(2);
    expect(mocks.fetchAdminPaymentOrders).toHaveBeenCalledTimes(1);
    expect(mocks.showToast).toHaveBeenCalledWith('success', '정산 파일 import가 완료되었습니다.');
    expect(screen.getByText('선택된 파일 없음')).toBeInTheDocument();
    const clearedFileInput = view.container.querySelector<HTMLInputElement>('input[type="file"]');
    expect(clearedFileInput).not.toBe(fileInput);
    expect(clearedFileInput?.files).toHaveLength(0);
  });

  it('presents every mixed-result error and retains exact correction context after one reload', async () => {
    const errors = Array.from({ length: 7 }, (_, index) => ({
      rowNumber: index + 2,
      message: `invalid row ${index + 2}`,
    }));
    const partialResult: AdminPaymentSettlementImportResult = {
      importBatchKey: 'BATCH-PARTIAL',
      totalRows: 8,
      importedRows: 1,
      skippedDuplicateRows: 0,
      failedRows: 7,
      statusCounts: { MATCHED: 1 },
      errors,
      omittedErrorCount: 0,
    };
    mocks.importAdminPaymentSettlements.mockResolvedValueOnce(partialResult);

    const view = await openSettlements();
    const file = new File(['safe,synthetic,csv'], 'mixed-settlement.csv', { type: 'text/csv' });
    const fileInput = submitSettlementImport(view, file, '  correction note  ');

    expect(await screen.findByRole('status')).toHaveTextContent(
      '일부 처리 실패: 7개 row의 오류 상세를 확인해주세요.',
    );
    for (const error of errors) {
      expect(screen.getByText(`row ${error.rowNumber}: ${error.message}`)).toBeInTheDocument();
    }
    expect(mocks.showToast).toHaveBeenCalledWith(
      'warning',
      '정산 import가 부분 완료되었습니다. 실패 7건을 확인해주세요.',
    );
    expect(mocks.showToast).not.toHaveBeenCalledWith('success', expect.any(String));
    expect(mocks.importAdminPaymentSettlements).toHaveBeenCalledTimes(1);
    expect(mocks.importAdminPaymentSettlements).toHaveBeenCalledWith(
      file,
      expect.stringMatching(/^[0-9a-f-]{36}$/),
      'correction note',
    );
    expect(mocks.fetchAdminPaymentSettlements).toHaveBeenCalledTimes(2);
    expect(screen.getByText('mixed-settlement.csv')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('정산 import 근거')).toHaveValue('  correction note  ');
    expect(fileInput.files?.[0]).toBe(file);
  });

  it('shows the reconciliation omitted count while retaining aggregates and 200 errors', async () => {
    const errors = Array.from({ length: 200 }, (_, index) => ({
      rowNumber: index + 1,
      message: `reconciliation error ${index + 1}`,
    }));
    const reconciliationResult: AdminPaymentSettlementImportResult = {
      importBatchKey: 'RECONCILE-BOUNDED',
      totalRows: 203,
      importedRows: 0,
      skippedDuplicateRows: 0,
      failedRows: 203,
      statusCounts: {},
      errors,
      omittedErrorCount: 3,
    };
    mocks.reconcileAdminPaymentSettlements.mockResolvedValueOnce(reconciliationResult);

    await openSettlements();
    fireEvent.click(screen.getByRole('button', { name: '누락 후보 확인' }));
    const dialog = screen.getByRole('dialog', { name: '정산 누락 후보 확인' });
    fireEvent.click(within(dialog).getByRole('button', { name: '확인' }));

    expect(
      await screen.findByText('대사 오류 상세 3건이 목록에서 생략되었습니다.'),
    ).toBeInTheDocument();
    expect(screen.getByRole('status')).toHaveTextContent(
      '일부 처리 실패: 203개 row의 오류 상세를 확인해주세요.',
    );
    expect(screen.getByRole('status')).not.toHaveTextContent('다시 import');
    expect(screen.getByText('omitted reconciliation errors').parentElement).toHaveTextContent('3');
    expect(screen.getByText('rows').parentElement).toHaveTextContent('203');
    expect(screen.getByText('failed').parentElement).toHaveTextContent('203');
    expect(screen.getByText('row 1: reconciliation error 1')).toBeInTheDocument();
    expect(screen.getByText('row 200: reconciliation error 200')).toBeInTheDocument();
    expect(screen.getAllByText(/^row \d+: reconciliation error \d+$/)).toHaveLength(200);
    expect(mocks.showToast).toHaveBeenCalledWith(
      'warning',
      '정산 누락 후보 확인이 부분 완료되었습니다. 결과 패널의 실패 및 오류 상세를 확인해주세요.',
    );
    expect(mocks.showToast).not.toHaveBeenCalledWith('success', expect.any(String));
    expect(mocks.reconcileAdminPaymentSettlements).toHaveBeenCalledTimes(1);
    expect(mocks.fetchAdminPaymentSettlements).toHaveBeenCalledTimes(2);
  });

  it('does not announce omitted errors when the omitted count is zero', async () => {
    const reconciliationResult: AdminPaymentSettlementImportResult = {
      importBatchKey: 'RECONCILE-COMPLETE-DETAILS',
      totalRows: 1,
      importedRows: 0,
      skippedDuplicateRows: 0,
      failedRows: 1,
      statusCounts: {},
      errors: [{ rowNumber: 1, message: 'complete reconciliation detail' }],
      omittedErrorCount: 0,
    };
    mocks.reconcileAdminPaymentSettlements.mockResolvedValueOnce(reconciliationResult);

    await openSettlements();
    fireEvent.click(screen.getByRole('button', { name: '누락 후보 확인' }));
    const dialog = screen.getByRole('dialog', { name: '정산 누락 후보 확인' });
    fireEvent.click(within(dialog).getByRole('button', { name: '확인' }));

    expect(await screen.findByText('row 1: complete reconciliation detail')).toBeInTheDocument();
    expect(screen.queryByText('omitted reconciliation errors')).not.toBeInTheDocument();
    expect(screen.queryByText(/대사 오류 상세 .*생략/)).not.toBeInTheDocument();
    expect(mocks.showToast).toHaveBeenCalledWith(
      'warning',
      '정산 누락 후보 확인이 부분 완료되었습니다. 결과 패널의 실패 및 오류 상세를 확인해주세요.',
    );
    expect(mocks.showToast).not.toHaveBeenCalledWith('success', expect.any(String));
  });

  it('announces a partial outcome when reconciliation reports only omitted errors', async () => {
    const reconciliationResult: AdminPaymentSettlementImportResult = {
      importBatchKey: 'RECONCILE-OMITTED-ONLY',
      totalRows: 1,
      importedRows: 1,
      skippedDuplicateRows: 0,
      failedRows: 0,
      statusCounts: { MATCHED: 1 },
      errors: [],
      omittedErrorCount: 1,
    };
    mocks.reconcileAdminPaymentSettlements.mockResolvedValueOnce(reconciliationResult);

    await openSettlements();
    fireEvent.click(screen.getByRole('button', { name: '누락 후보 확인' }));
    const dialog = screen.getByRole('dialog', { name: '정산 누락 후보 확인' });
    fireEvent.click(within(dialog).getByRole('button', { name: '확인' }));

    expect(
      await screen.findByText('대사 오류 상세 1건이 목록에서 생략되었습니다.'),
    ).toBeInTheDocument();
    expect(mocks.showToast).toHaveBeenCalledWith(
      'warning',
      '정산 누락 후보 확인이 부분 완료되었습니다. 결과 패널의 실패 및 오류 상세를 확인해주세요.',
    );
    expect(mocks.showToast).not.toHaveBeenCalledWith('success', expect.any(String));
    expect(mocks.reconcileAdminPaymentSettlements).toHaveBeenCalledTimes(1);
    expect(mocks.fetchAdminPaymentSettlements).toHaveBeenCalledTimes(2);
  });

  it('announces success only when reconciliation has no failed or omitted errors', async () => {
    const reconciliationResult: AdminPaymentSettlementImportResult = {
      ...settlementResult,
      importBatchKey: 'RECONCILE-FULL-SUCCESS',
    };
    mocks.reconcileAdminPaymentSettlements.mockResolvedValueOnce(reconciliationResult);

    await openSettlements();
    fireEvent.click(screen.getByRole('button', { name: '누락 후보 확인' }));
    const dialog = screen.getByRole('dialog', { name: '정산 누락 후보 확인' });
    fireEvent.click(within(dialog).getByRole('button', { name: '확인' }));

    await waitFor(() =>
      expect(mocks.showToast).toHaveBeenCalledWith(
        'success',
        '정산 누락 후보 확인이 완료되었습니다.',
      ),
    );
    expect(mocks.showToast).not.toHaveBeenCalledWith('warning', expect.any(String));
    expect(screen.getByText('batch').parentElement).toHaveTextContent('RECONCILE-FULL-SUCCESS');
    expect(mocks.reconcileAdminPaymentSettlements).toHaveBeenCalledTimes(1);
    expect(mocks.fetchAdminPaymentSettlements).toHaveBeenCalledTimes(2);
  });

  it('uses one key for one POST and read-only recovery while retaining WI-041 context', async () => {
    mocks.importAdminPaymentSettlements.mockRejectedValueOnce(new Error('transport failed'));
    mocks.recoverAdminPaymentSettlementImportAttempt.mockResolvedValue(processingSettlementAttempt);

    const view = await openSettlements();
    const file = new File(['safe,synthetic,csv'], 'retry-settlement.csv', { type: 'text/csv' });
    const fileInput = submitSettlementImport(view, file, ' retry note ');

    await waitFor(() =>
      expect(mocks.recoverAdminPaymentSettlementImportAttempt).toHaveBeenCalledTimes(1),
    );
    const operationKey = mocks.importAdminPaymentSettlements.mock.calls[0][1] as string;
    expect(mocks.importAdminPaymentSettlements).toHaveBeenCalledTimes(1);
    expect(mocks.importAdminPaymentSettlements).toHaveBeenCalledWith(
      file,
      operationKey,
      'retry note',
    );
    expect(mocks.recoverAdminPaymentSettlementImportAttempt).toHaveBeenCalledWith(operationKey);
    expect(mocks.fetchAdminPaymentSettlements).toHaveBeenCalledTimes(1);
    expect(mocks.showToast).not.toHaveBeenCalledWith('success', expect.any(String));
    expect(screen.getByText('PROCESSING')).toBeInTheDocument();
    expect(screen.getByText('retry-settlement.csv')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('정산 import 근거')).toHaveValue(' retry note ');
    expect(fileInput.files?.[0]).toBe(file);

    fireEvent.click(screen.getByRole('button', { name: 'import 결과 복구' }));
    await waitFor(() =>
      expect(mocks.recoverAdminPaymentSettlementImportAttempt).toHaveBeenCalledTimes(2),
    );
    expect(mocks.recoverAdminPaymentSettlementImportAttempt).toHaveBeenLastCalledWith(operationKey);
    expect(mocks.importAdminPaymentSettlements).toHaveBeenCalledTimes(1);
  });

  it('retains correction context and does not announce full success when the required reload fails', async () => {
    mocks.fetchAdminPaymentSettlements
      .mockResolvedValueOnce(emptyPage(1, 0))
      .mockRejectedValueOnce(new Error('reload failed'));
    mocks.importAdminPaymentSettlements.mockResolvedValueOnce(settlementResult);

    const view = await openSettlements();
    const file = new File(['safe,synthetic,csv'], 'reload-settlement.csv', { type: 'text/csv' });
    const fileInput = submitSettlementImport(view, file, ' reload note ');

    await waitFor(() =>
      expect(mocks.showToast).toHaveBeenCalledWith(
        'error',
        '정산 import 결과를 받았지만 목록을 다시 불러오지 못했습니다.',
      ),
    );
    expect(mocks.importAdminPaymentSettlements).toHaveBeenCalledTimes(1);
    expect(mocks.fetchAdminPaymentSettlements).toHaveBeenCalledTimes(2);
    expect(mocks.showToast).not.toHaveBeenCalledWith('success', expect.any(String));
    expect(screen.getByText('reload-settlement.csv')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('정산 import 근거')).toHaveValue(' reload note ');
    expect(fileInput.files?.[0]).toBe(file);
  });

  it('requires a trimmed note and danger confirmation before one settlement ignore call', async () => {
    mocks.fetchAdminPaymentSettlements.mockResolvedValue(pageWith([mismatchedSettlement]));
    mocks.ignoreAdminPaymentSettlement.mockResolvedValue(undefined);

    await openSettlements();
    const row = screen.getByText('ORDER-SETTLEMENT-17').closest('tr') as HTMLElement;
    const ignoreButton = within(row).getByRole('button', { name: 'IGNORE' });
    fireEvent.click(ignoreButton);

    expect(mocks.ignoreAdminPaymentSettlement).not.toHaveBeenCalled();
    expect(mocks.showToast).toHaveBeenCalledWith('error', 'ignore 처리 메모를 입력해주세요.');

    fireEvent.change(within(row).getByPlaceholderText('IGNORE 메모'), {
      target: { value: '  operator note  ' },
    });
    fireEvent.click(ignoreButton);
    expect(mocks.ignoreAdminPaymentSettlement).not.toHaveBeenCalled();
    const dialog = screen.getByRole('dialog', { name: '정산 항목 제외' });
    const confirmButton = within(dialog).getByRole('button', { name: 'IGNORE' });
    fireEvent.click(confirmButton);
    fireEvent.click(confirmButton);

    await waitFor(() => expect(mocks.ignoreAdminPaymentSettlement).toHaveBeenCalledTimes(1));
    expect(mocks.ignoreAdminPaymentSettlement).toHaveBeenCalledWith(17, 'operator note');
    await waitFor(() => expect(mocks.fetchAdminPaymentSettlements).toHaveBeenCalledTimes(2));
    expect(mocks.fetchAdminPaymentSettlements).toHaveBeenCalledTimes(2);
  });

  it('reports a settlement reconciliation failure without refreshing the current view', async () => {
    mocks.reconcileAdminPaymentSettlements.mockRejectedValueOnce(new Error('reconcile failed'));

    render(<PaymentOperationsPage />);
    await waitFor(() => expect(mocks.fetchAdminPaymentOrders).toHaveBeenCalledTimes(1));
    fireEvent.click(screen.getByRole('button', { name: '정산' }));
    await waitFor(() => expect(mocks.fetchAdminPaymentSettlements).toHaveBeenCalledTimes(1));
    fireEvent.click(screen.getByRole('button', { name: '누락 후보 확인' }));
    const dialog = screen.getByRole('dialog', { name: '정산 누락 후보 확인' });
    fireEvent.click(within(dialog).getByRole('button', { name: '확인' }));

    await waitFor(() =>
      expect(mocks.showToast).toHaveBeenCalledWith('error', '정산 누락 후보 확인에 실패했습니다.'),
    );
    expect(mocks.fetchAdminPaymentSettlements).toHaveBeenCalledTimes(1);
  });

  it('creates one confirmed refund request with the preview local payment id and refreshes once', async () => {
    const mutation = deferred<unknown>();
    mocks.fetchAdminPaymentRefundPreview.mockResolvedValueOnce(refundPreview);
    mocks.createAdminPaymentRefund.mockReturnValueOnce(mutation.promise);

    render(<PaymentOperationsPage />);
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
    expect(mocks.createAdminPaymentRefund).not.toHaveBeenCalled();
    const dialog = screen.getByRole('dialog', { name: '환불 요청 생성' });
    const confirmButton = within(dialog).getByRole('button', { name: '요청 생성' });
    fireEvent.click(confirmButton);
    fireEvent.click(confirmButton);
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

  it('invalidates a refund preview when the payment id changes', async () => {
    mocks.fetchAdminPaymentRefundPreview.mockResolvedValueOnce(refundPreview);

    render(<PaymentOperationsPage />);
    await waitFor(() => expect(mocks.fetchAdminPaymentOrders).toHaveBeenCalledTimes(1));
    fireEvent.click(screen.getByRole('button', { name: '환불' }));
    await waitFor(() => expect(mocks.fetchAdminPaymentRefunds).toHaveBeenCalledTimes(1));
    const paymentIdInput = screen.getByPlaceholderText('subscriptionPaymentId');
    fireEvent.change(paymentIdInput, { target: { value: '41' } });
    fireEvent.click(screen.getByRole('button', { name: '환불 미리보기' }));
    await waitFor(() => expect(mocks.fetchAdminPaymentRefundPreview).toHaveBeenCalledWith(41));

    expect(screen.getByRole('button', { name: '환불 요청 생성' })).toBeEnabled();
    fireEvent.change(paymentIdInput, { target: { value: '42' } });

    expect(screen.getByRole('button', { name: '환불 요청 생성' })).toBeDisabled();
    fireEvent.click(screen.getByRole('button', { name: '환불 요청 생성' }));
    expect(mocks.createAdminPaymentRefund).not.toHaveBeenCalled();
  });

  it('invalidates a prior refund preview when re-preview fails', async () => {
    mocks.fetchAdminPaymentRefundPreview
      .mockResolvedValueOnce(refundPreview)
      .mockRejectedValueOnce(new Error('preview failed'));

    render(<PaymentOperationsPage />);
    await waitFor(() => expect(mocks.fetchAdminPaymentOrders).toHaveBeenCalledTimes(1));
    fireEvent.click(screen.getByRole('button', { name: '환불' }));
    await waitFor(() => expect(mocks.fetchAdminPaymentRefunds).toHaveBeenCalledTimes(1));
    fireEvent.change(screen.getByPlaceholderText('subscriptionPaymentId'), {
      target: { value: '41' },
    });
    const previewButton = screen.getByRole('button', { name: '환불 미리보기' });
    fireEvent.click(previewButton);
    await waitFor(() => expect(mocks.fetchAdminPaymentRefundPreview).toHaveBeenCalledTimes(1));
    expect(screen.getByRole('button', { name: '환불 요청 생성' })).toBeEnabled();

    fireEvent.click(previewButton);
    await waitFor(() => expect(mocks.fetchAdminPaymentRefundPreview).toHaveBeenCalledTimes(2));

    expect(screen.getByRole('button', { name: '환불 요청 생성' })).toBeDisabled();
    expect(mocks.createAdminPaymentRefund).not.toHaveBeenCalled();
  });

  it('previews and creates one confirmed entitlement correction using local ids', async () => {
    const mutation = deferred<unknown>();
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

    render(<PaymentOperationsPage />);
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
    expect(mocks.createAdminPaymentEntitlementCorrection).not.toHaveBeenCalled();
    const dialog = screen.getByRole('dialog', { name: '권한 보정 요청 생성' });
    const confirmButton = within(dialog).getByRole('button', { name: '요청 생성' });
    fireEvent.click(confirmButton);
    fireEvent.click(confirmButton);
    expect(mocks.createAdminPaymentEntitlementCorrection).toHaveBeenCalledTimes(1);
    expect(mocks.createAdminPaymentEntitlementCorrection).toHaveBeenCalledWith(expectedRequest);
    expect(requestButton).toBeDisabled();

    await act(async () => mutation.resolve(undefined));
    await waitFor(() =>
      expect(mocks.fetchAdminPaymentEntitlementCorrections).toHaveBeenCalledTimes(2),
    );
    expect(mocks.fetchAdminPaymentEntitlementCorrections).toHaveBeenCalledTimes(2);
  });

  it('hydrates a reloaded pending refund as UNKNOWN and blocks execute and linked correction', async () => {
    const item = refund('PENDING_PROVIDER_CONFIRMATION');
    const linked = correction('APPROVED');
    mocks.fetchAdminPaymentEntitlementCorrections.mockReset().mockResolvedValue(pageWith([linked]));
    await openRefunds([item]);

    expect(await screen.findByTestId('refund-recovery-51')).toHaveTextContent('UNKNOWN');
    const refundRow = screen.getByText(item.orderId).closest('tr') as HTMLElement;
    expect(within(refundRow).getByRole('button', { name: '상태 다시 확인' })).toBeEnabled();
    expect(within(refundRow).getByRole('button', { name: '실행' })).toBeDisabled();
    expect(mocks.fetchAdminPaymentRefund).not.toHaveBeenCalled();
    expect(mocks.executeAdminPaymentRefund).not.toHaveBeenCalled();

    fireEvent.click(screen.getAllByRole('button', { name: '권한 보정' })[0]);
    const correctionRow = (await screen.findByText(linked.userNickname)).closest(
      'tr',
    ) as HTMLElement;
    expect(within(correctionRow).getByRole('button', { name: '실행' })).toBeDisabled();
    expect(mocks.executeAdminPaymentEntitlementCorrection).not.toHaveBeenCalled();
  });

  it('hydrates a reloaded processing correction as UNKNOWN and blocks execute and linked refund', async () => {
    const item = correction('PROCESSING');
    const linked = refund('APPROVED');
    mocks.fetchAdminPaymentRefunds.mockReset().mockResolvedValue(pageWith([linked]));
    await openCorrections([item]);

    expect(await screen.findByTestId('correction-recovery-61')).toHaveTextContent('UNKNOWN');
    const correctionRow = screen.getByText(item.userNickname).closest('tr') as HTMLElement;
    expect(within(correctionRow).getByRole('button', { name: '상태 다시 확인' })).toBeEnabled();
    expect(within(correctionRow).getByRole('button', { name: '실행' })).toBeDisabled();
    expect(mocks.fetchAdminPaymentEntitlementCorrection).not.toHaveBeenCalled();
    expect(mocks.executeAdminPaymentEntitlementCorrection).not.toHaveBeenCalled();

    fireEvent.click(screen.getByRole('button', { name: '환불' }));
    const refundRow = (await screen.findByText(linked.orderId)).closest('tr') as HTMLElement;
    expect(within(refundRow).getByRole('button', { name: '실행' })).toBeDisabled();
    expect(mocks.executeAdminPaymentRefund).not.toHaveBeenCalled();
  });

  it('preflights a reloaded approved correction before one explicit execute POST', async () => {
    const item = correction('APPROVED');
    mocks.fetchAdminPaymentEntitlementCorrection.mockResolvedValueOnce(item);
    mocks.executeAdminPaymentEntitlementCorrection.mockResolvedValueOnce(correction('FAILED'));
    await openCorrections([item]);

    expect(mocks.fetchAdminPaymentEntitlementCorrection).not.toHaveBeenCalled();
    expect(mocks.executeAdminPaymentEntitlementCorrection).not.toHaveBeenCalled();
    executeCorrectionRow(item);

    await waitFor(() =>
      expect(mocks.executeAdminPaymentEntitlementCorrection).toHaveBeenCalledTimes(1),
    );
    expect(mocks.fetchAdminPaymentEntitlementCorrection).toHaveBeenCalledTimes(1);
    expect(mocks.fetchAdminPaymentEntitlementCorrection.mock.invocationCallOrder[0]).toBeLessThan(
      mocks.executeAdminPaymentEntitlementCorrection.mock.invocationCallOrder[0],
    );
  });

  it('unlocks an UNKNOWN refund on manual APPROVED read before a later explicit execute', async () => {
    const item = refund('PROCESSING');
    mocks.fetchAdminPaymentRefund
      .mockResolvedValueOnce(refund('APPROVED'))
      .mockResolvedValueOnce(refund('APPROVED'));
    mocks.executeAdminPaymentRefund.mockResolvedValueOnce(refund('FAILED'));
    await openRefunds([item]);
    const row = screen.getByText(item.orderId).closest('tr') as HTMLElement;
    expect(screen.getByTestId('refund-recovery-51')).toHaveTextContent('UNKNOWN');

    fireEvent.click(within(row).getByRole('button', { name: '상태 다시 확인' }));

    await waitFor(() => expect(screen.queryByTestId('refund-recovery-51')).not.toBeInTheDocument());
    expect(within(row).getByText('APPROVED')).toBeInTheDocument();
    expect(mocks.executeAdminPaymentRefund).not.toHaveBeenCalled();
    executeRefundRow(refund('APPROVED'));
    await waitFor(() => expect(mocks.executeAdminPaymentRefund).toHaveBeenCalledTimes(1));
    expect(mocks.fetchAdminPaymentRefund).toHaveBeenCalledTimes(2);
  });

  it('unlocks an UNKNOWN correction on manual APPROVED read before a later explicit execute', async () => {
    const item = correction('PROCESSING');
    mocks.fetchAdminPaymentEntitlementCorrection
      .mockResolvedValueOnce(correction('APPROVED'))
      .mockResolvedValueOnce(correction('APPROVED'));
    mocks.executeAdminPaymentEntitlementCorrection.mockResolvedValueOnce(correction('FAILED'));
    await openCorrections([item]);
    const row = screen.getByText(item.userNickname).closest('tr') as HTMLElement;
    expect(screen.getByTestId('correction-recovery-61')).toHaveTextContent('UNKNOWN');

    fireEvent.click(within(row).getByRole('button', { name: '상태 다시 확인' }));

    await waitFor(() =>
      expect(screen.queryByTestId('correction-recovery-61')).not.toBeInTheDocument(),
    );
    expect(within(row).getByText('APPROVED')).toBeInTheDocument();
    expect(mocks.executeAdminPaymentEntitlementCorrection).not.toHaveBeenCalled();
    executeCorrectionRow(correction('APPROVED'));
    await waitFor(() =>
      expect(mocks.executeAdminPaymentEntitlementCorrection).toHaveBeenCalledTimes(1),
    );
    expect(mocks.fetchAdminPaymentEntitlementCorrection).toHaveBeenCalledTimes(2);
  });

  it('unlocks manual REQUESTED reads to approve only without mutating', async () => {
    const refundItem = refund('PROCESSING');
    mocks.fetchAdminPaymentRefund.mockResolvedValueOnce(refund('REQUESTED'));
    await openRefunds([refundItem]);
    const refundRow = screen.getByText(refundItem.orderId).closest('tr') as HTMLElement;
    expect(within(refundRow).getByRole('button', { name: '승인' })).toBeDisabled();
    fireEvent.click(within(refundRow).getByRole('button', { name: '상태 다시 확인' }));

    await waitFor(() => expect(screen.queryByTestId('refund-recovery-51')).not.toBeInTheDocument());
    expect(within(refundRow).getByRole('button', { name: '승인' })).toBeEnabled();
    expect(within(refundRow).getByRole('button', { name: '실행' })).toBeDisabled();
    expect(mocks.approveAdminPaymentRefund).not.toHaveBeenCalled();
    expect(mocks.executeAdminPaymentRefund).not.toHaveBeenCalled();
  });

  it('unlocks a manual correction REQUESTED read to approve only without mutating', async () => {
    const item = correction('PROCESSING');
    mocks.fetchAdminPaymentEntitlementCorrection.mockResolvedValueOnce(correction('REQUESTED'));
    await openCorrections([item]);
    const row = screen.getByText(item.userNickname).closest('tr') as HTMLElement;
    expect(within(row).getByRole('button', { name: '승인' })).toBeDisabled();
    fireEvent.click(within(row).getByRole('button', { name: '상태 다시 확인' }));

    await waitFor(() =>
      expect(screen.queryByTestId('correction-recovery-61')).not.toBeInTheDocument(),
    );
    expect(within(row).getByRole('button', { name: '승인' })).toBeEnabled();
    expect(within(row).getByRole('button', { name: '실행' })).toBeDisabled();
    expect(mocks.approveAdminPaymentEntitlementCorrection).not.toHaveBeenCalled();
    expect(mocks.executeAdminPaymentEntitlementCorrection).not.toHaveBeenCalled();
  });

  it('keeps a manually read pending refund UNKNOWN and locked', async () => {
    const item = refund('PENDING_PROVIDER_CONFIRMATION');
    mocks.fetchAdminPaymentRefund.mockResolvedValueOnce(item);
    await openRefunds([item]);
    const row = screen.getByText(item.orderId).closest('tr') as HTMLElement;

    fireEvent.click(within(row).getByRole('button', { name: '상태 다시 확인' }));

    await waitFor(() => expect(mocks.fetchAdminPaymentRefund).toHaveBeenCalledTimes(1));
    expect(screen.getByTestId('refund-recovery-51')).toHaveTextContent('UNKNOWN');
    expect(within(row).getByRole('button', { name: '실행' })).toBeDisabled();
    expect(mocks.executeAdminPaymentRefund).not.toHaveBeenCalled();
  });

  it.each([
    ['SUCCEEDED', 'COMMITTED'],
    ['FAILED', 'FAILED'],
    ['CANCELLED', 'FAILED'],
    ['PROCESSING', 'UNKNOWN'],
  ] as const)('blocks refund execute when preflight finds %s as %s', async (status, outcome) => {
    const item = refund('APPROVED');
    mocks.fetchAdminPaymentRefund.mockResolvedValueOnce(refund(status));
    mocks.executeAdminPaymentRefund.mockResolvedValueOnce(refund('FAILED'));
    await openRefunds([item]);

    executeRefundRow(item);

    expect(await screen.findByTestId('refund-recovery-51')).toHaveTextContent(outcome);
    expect(mocks.fetchAdminPaymentRefund).toHaveBeenCalledTimes(1);
    expect(mocks.executeAdminPaymentRefund).not.toHaveBeenCalled();
  });

  it.each([
    ['SUCCEEDED', 'COMMITTED'],
    ['FAILED', 'FAILED'],
    ['CANCELLED', 'FAILED'],
    ['PROCESSING', 'UNKNOWN'],
  ] as const)(
    'blocks correction execute when preflight finds %s as %s',
    async (status, outcome) => {
      const item = correction('APPROVED');
      mocks.fetchAdminPaymentEntitlementCorrection.mockResolvedValueOnce(correction(status));
      mocks.executeAdminPaymentEntitlementCorrection.mockResolvedValueOnce(correction('FAILED'));
      await openCorrections([item]);

      executeCorrectionRow(item);

      expect(await screen.findByTestId('correction-recovery-61')).toHaveTextContent(outcome);
      expect(mocks.fetchAdminPaymentEntitlementCorrection).toHaveBeenCalledTimes(1);
      expect(mocks.executeAdminPaymentEntitlementCorrection).not.toHaveBeenCalled();
    },
  );

  it('blocks refund execute when preflight is unreadable or returns another id', async () => {
    const unreadable = refund('APPROVED');
    mocks.fetchAdminPaymentRefund.mockRejectedValueOnce(new Error('detail unavailable'));
    mocks.executeAdminPaymentRefund.mockResolvedValueOnce(refund('FAILED'));
    await openRefunds([unreadable]);
    executeRefundRow(unreadable);
    expect(await screen.findByTestId('refund-recovery-51')).toHaveTextContent('UNKNOWN');
    expect(mocks.executeAdminPaymentRefund).not.toHaveBeenCalled();

    mocks.fetchAdminPaymentRefund.mockResolvedValueOnce(refund('APPROVED', { id: 999 }));
    const statusButton = screen.getByRole('button', { name: '상태 다시 확인' });
    fireEvent.click(statusButton);
    await waitFor(() => expect(mocks.fetchAdminPaymentRefund).toHaveBeenCalledTimes(2));
    expect(screen.getByTestId('refund-recovery-51')).toHaveTextContent('UNKNOWN');
    expect(mocks.executeAdminPaymentRefund).not.toHaveBeenCalled();
  });

  it('blocks correction execute when preflight is unreadable or returns another id', async () => {
    const unreadable = correction('APPROVED');
    mocks.fetchAdminPaymentEntitlementCorrection.mockRejectedValueOnce(
      new Error('detail unavailable'),
    );
    mocks.executeAdminPaymentEntitlementCorrection.mockResolvedValueOnce(correction('FAILED'));
    await openCorrections([unreadable]);
    executeCorrectionRow(unreadable);
    expect(await screen.findByTestId('correction-recovery-61')).toHaveTextContent('UNKNOWN');
    expect(mocks.executeAdminPaymentEntitlementCorrection).not.toHaveBeenCalled();

    mocks.fetchAdminPaymentEntitlementCorrection.mockResolvedValueOnce(
      correction('APPROVED', { id: 999 }),
    );
    const statusButton = screen.getByRole('button', { name: '상태 다시 확인' });
    fireEvent.click(statusButton);
    await waitFor(() =>
      expect(mocks.fetchAdminPaymentEntitlementCorrection).toHaveBeenCalledTimes(2),
    );
    expect(screen.getByTestId('correction-recovery-61')).toHaveTextContent('UNKNOWN');
    expect(mocks.executeAdminPaymentEntitlementCorrection).not.toHaveBeenCalled();
  });

  it('blocks refund execute when preflight returns another durable id', async () => {
    const item = refund('APPROVED');
    mocks.fetchAdminPaymentRefund.mockResolvedValueOnce(refund('APPROVED', { id: 999 }));
    await openRefunds([item]);

    executeRefundRow(item);

    expect(await screen.findByTestId('refund-recovery-51')).toHaveTextContent('UNKNOWN');
    expect(mocks.fetchAdminPaymentRefund).toHaveBeenCalledTimes(1);
    expect(mocks.executeAdminPaymentRefund).not.toHaveBeenCalled();
  });

  it('blocks correction execute when preflight returns another durable id', async () => {
    const item = correction('APPROVED');
    mocks.fetchAdminPaymentEntitlementCorrection.mockResolvedValueOnce(
      correction('APPROVED', { id: 999 }),
    );
    await openCorrections([item]);

    executeCorrectionRow(item);

    expect(await screen.findByTestId('correction-recovery-61')).toHaveTextContent('UNKNOWN');
    expect(mocks.fetchAdminPaymentEntitlementCorrection).toHaveBeenCalledTimes(1);
    expect(mocks.executeAdminPaymentEntitlementCorrection).not.toHaveBeenCalled();
  });

  it.each([
    ['SUCCEEDED', 'COMMITTED'],
    ['FAILED', 'FAILED'],
    ['CANCELLED', 'FAILED'],
    ['PROCESSING', 'UNKNOWN'],
    ['PENDING_PROVIDER_CONFIRMATION', 'UNKNOWN'],
    ['REQUESTED', 'UNKNOWN'],
    ['APPROVED', 'UNKNOWN'],
  ] as const)(
    'reconciles a lost refund execute response with %s as %s without replay',
    async (status, outcome) => {
      const item = refund('APPROVED');
      mocks.executeAdminPaymentRefund.mockRejectedValueOnce(new Error('response lost'));
      mocks.fetchAdminPaymentRefund
        .mockResolvedValueOnce(item)
        .mockResolvedValueOnce(refund(status));
      await openRefunds([item]);

      executeRefundRow(item);

      expect(await screen.findByTestId('refund-recovery-51')).toHaveTextContent(outcome);
      expect(mocks.executeAdminPaymentRefund).toHaveBeenCalledTimes(1);
      expect(mocks.fetchAdminPaymentRefund).toHaveBeenCalledTimes(2);
      expect(mocks.fetchAdminPaymentRefunds).toHaveBeenCalledTimes(1);
      expect(mocks.executeAdminPaymentEntitlementCorrection).not.toHaveBeenCalled();
    },
  );

  it.each([
    ['SUCCEEDED', 'COMMITTED'],
    ['FAILED', 'FAILED'],
    ['CANCELLED', 'FAILED'],
    ['PROCESSING', 'UNKNOWN'],
    ['REQUESTED', 'UNKNOWN'],
    ['APPROVED', 'UNKNOWN'],
  ] as const)(
    'reconciles a lost correction execute response with %s as %s without replay',
    async (status, outcome) => {
      const item = correction('APPROVED');
      mocks.executeAdminPaymentEntitlementCorrection.mockRejectedValueOnce(
        new Error('response lost'),
      );
      mocks.fetchAdminPaymentEntitlementCorrection
        .mockResolvedValueOnce(item)
        .mockResolvedValueOnce(correction(status));
      await openCorrections([item]);

      executeCorrectionRow(item);

      expect(await screen.findByTestId('correction-recovery-61')).toHaveTextContent(outcome);
      expect(mocks.executeAdminPaymentEntitlementCorrection).toHaveBeenCalledTimes(1);
      expect(mocks.fetchAdminPaymentEntitlementCorrection).toHaveBeenCalledTimes(2);
      expect(mocks.fetchAdminPaymentEntitlementCorrections).toHaveBeenCalledTimes(1);
      expect(mocks.executeAdminPaymentRefund).not.toHaveBeenCalled();
    },
  );

  it('keeps refund UNKNOWN when the one response-loss detail read fails', async () => {
    const item = refund('APPROVED');
    mocks.executeAdminPaymentRefund.mockRejectedValueOnce(new Error('network'));
    mocks.fetchAdminPaymentRefund
      .mockResolvedValueOnce(item)
      .mockRejectedValueOnce(new Error('detail unavailable'));
    await openRefunds([item]);

    executeRefundRow(item);

    expect(await screen.findByTestId('refund-recovery-51')).toHaveTextContent('UNKNOWN');
    expect(mocks.executeAdminPaymentRefund).toHaveBeenCalledTimes(1);
    expect(mocks.fetchAdminPaymentRefund).toHaveBeenCalledTimes(2);
  });

  it('keeps correction UNKNOWN when the one response-loss detail read fails', async () => {
    const item = correction('APPROVED');
    mocks.executeAdminPaymentEntitlementCorrection.mockRejectedValueOnce(new Error('network'));
    mocks.fetchAdminPaymentEntitlementCorrection
      .mockResolvedValueOnce(item)
      .mockRejectedValueOnce(new Error('detail unavailable'));
    await openCorrections([item]);

    executeCorrectionRow(item);

    expect(await screen.findByTestId('correction-recovery-61')).toHaveTextContent('UNKNOWN');
    expect(mocks.executeAdminPaymentEntitlementCorrection).toHaveBeenCalledTimes(1);
    expect(mocks.fetchAdminPaymentEntitlementCorrection).toHaveBeenCalledTimes(2);
  });

  it('marks a successful refund response RELOAD_FAILED when its list refresh fails', async () => {
    const item = refund('APPROVED');
    mocks.fetchAdminPaymentRefunds
      .mockReset()
      .mockResolvedValueOnce(pageWith([item]))
      .mockRejectedValueOnce(new Error('list reload failed'));
    mocks.executeAdminPaymentRefund.mockResolvedValueOnce(refund('SUCCEEDED'));
    mocks.fetchAdminPaymentRefund
      .mockResolvedValueOnce(item)
      .mockResolvedValueOnce(refund('SUCCEEDED'));
    render(<PaymentOperationsPage />);
    await waitFor(() => expect(mocks.fetchAdminPaymentOrders).toHaveBeenCalledTimes(1));
    fireEvent.click(screen.getByRole('button', { name: '환불' }));
    await waitFor(() => expect(mocks.fetchAdminPaymentRefunds).toHaveBeenCalledTimes(1));

    executeRefundRow(item);

    expect(await screen.findByTestId('refund-recovery-51')).toHaveTextContent('RELOAD_FAILED');
    expect(mocks.executeAdminPaymentRefund).toHaveBeenCalledTimes(1);
    expect(mocks.fetchAdminPaymentRefund).toHaveBeenCalledTimes(2);
    expect(mocks.fetchAdminPaymentRefunds).toHaveBeenCalledTimes(2);
  });

  it('marks a successful correction response RELOAD_FAILED when its list refresh fails', async () => {
    const item = correction('APPROVED');
    mocks.fetchAdminPaymentEntitlementCorrections
      .mockReset()
      .mockResolvedValueOnce(pageWith([item]))
      .mockRejectedValueOnce(new Error('list reload failed'));
    mocks.executeAdminPaymentEntitlementCorrection.mockResolvedValueOnce(correction('SUCCEEDED'));
    mocks.fetchAdminPaymentEntitlementCorrection
      .mockResolvedValueOnce(item)
      .mockResolvedValueOnce(correction('SUCCEEDED'));
    render(<PaymentOperationsPage />);
    await waitFor(() => expect(mocks.fetchAdminPaymentOrders).toHaveBeenCalledTimes(1));
    fireEvent.click(screen.getByRole('button', { name: '권한 보정' }));
    await waitFor(() =>
      expect(mocks.fetchAdminPaymentEntitlementCorrections).toHaveBeenCalledTimes(1),
    );

    executeCorrectionRow(item);

    expect(await screen.findByTestId('correction-recovery-61')).toHaveTextContent('RELOAD_FAILED');
    expect(mocks.executeAdminPaymentEntitlementCorrection).toHaveBeenCalledTimes(1);
    expect(mocks.fetchAdminPaymentEntitlementCorrection).toHaveBeenCalledTimes(2);
    expect(mocks.fetchAdminPaymentEntitlementCorrections).toHaveBeenCalledTimes(2);
  });

  it('preserves refund RELOAD_FAILED when a manual detail read fails', async () => {
    const item = refund('APPROVED');
    mocks.fetchAdminPaymentRefunds
      .mockReset()
      .mockResolvedValueOnce(pageWith([item]))
      .mockRejectedValueOnce(new Error('list reload failed'));
    mocks.executeAdminPaymentRefund.mockResolvedValueOnce(refund('SUCCEEDED'));
    mocks.fetchAdminPaymentRefund
      .mockResolvedValueOnce(item)
      .mockResolvedValueOnce(refund('SUCCEEDED'))
      .mockRejectedValueOnce(new Error('detail read failed'));
    render(<PaymentOperationsPage />);
    await waitFor(() => expect(mocks.fetchAdminPaymentOrders).toHaveBeenCalledTimes(1));
    fireEvent.click(screen.getByRole('button', { name: '환불' }));
    await waitFor(() => expect(mocks.fetchAdminPaymentRefunds).toHaveBeenCalledTimes(1));
    executeRefundRow(item);
    expect(await screen.findByTestId('refund-recovery-51')).toHaveTextContent('RELOAD_FAILED');

    fireEvent.click(
      within(screen.getByTestId('refund-recovery-51').closest('td') as HTMLElement).getByRole(
        'button',
      ),
    );

    await waitFor(() => expect(mocks.fetchAdminPaymentRefund).toHaveBeenCalledTimes(3));
    expect(screen.getByTestId('refund-recovery-51')).toHaveTextContent('RELOAD_FAILED');
    expect(mocks.executeAdminPaymentRefund).toHaveBeenCalledTimes(1);
  });

  it('preserves correction RELOAD_FAILED when a manual detail read fails', async () => {
    const item = correction('APPROVED');
    mocks.fetchAdminPaymentEntitlementCorrections
      .mockReset()
      .mockResolvedValueOnce(pageWith([item]))
      .mockRejectedValueOnce(new Error('list reload failed'));
    mocks.executeAdminPaymentEntitlementCorrection.mockResolvedValueOnce(correction('SUCCEEDED'));
    mocks.fetchAdminPaymentEntitlementCorrection
      .mockResolvedValueOnce(item)
      .mockResolvedValueOnce(correction('SUCCEEDED'))
      .mockRejectedValueOnce(new Error('detail read failed'));
    render(<PaymentOperationsPage />);
    await waitFor(() => expect(mocks.fetchAdminPaymentOrders).toHaveBeenCalledTimes(1));
    fireEvent.click(screen.getByRole('button', { name: '권한 보정' }));
    await waitFor(() =>
      expect(mocks.fetchAdminPaymentEntitlementCorrections).toHaveBeenCalledTimes(1),
    );
    executeCorrectionRow(item);
    expect(await screen.findByTestId('correction-recovery-61')).toHaveTextContent('RELOAD_FAILED');

    fireEvent.click(
      within(screen.getByTestId('correction-recovery-61').closest('td') as HTMLElement).getByRole(
        'button',
      ),
    );

    await waitFor(() =>
      expect(mocks.fetchAdminPaymentEntitlementCorrection).toHaveBeenCalledTimes(3),
    );
    expect(screen.getByTestId('correction-recovery-61')).toHaveTextContent('RELOAD_FAILED');
    expect(mocks.executeAdminPaymentEntitlementCorrection).toHaveBeenCalledTimes(1);
  });

  it('maps refund RELOAD_FAILED to COMMITTED when manual detail succeeds', async () => {
    const item = refund('APPROVED');
    const fresh = refund('SUCCEEDED', { providerRefundReference: 'DETAIL-COMMITTED-REFUND' });
    mocks.fetchAdminPaymentRefunds
      .mockReset()
      .mockResolvedValueOnce(pageWith([item]))
      .mockRejectedValueOnce(new Error('list reload failed'));
    mocks.executeAdminPaymentRefund.mockResolvedValueOnce(refund('SUCCEEDED'));
    mocks.fetchAdminPaymentRefund
      .mockResolvedValueOnce(item)
      .mockResolvedValueOnce(refund('SUCCEEDED'))
      .mockResolvedValueOnce(fresh);
    render(<PaymentOperationsPage />);
    await waitFor(() => expect(mocks.fetchAdminPaymentOrders).toHaveBeenCalledTimes(1));
    fireEvent.click(screen.getByRole('button', { name: '환불' }));
    await waitFor(() => expect(mocks.fetchAdminPaymentRefunds).toHaveBeenCalledTimes(1));
    executeRefundRow(item);
    expect(await screen.findByTestId('refund-recovery-51')).toHaveTextContent('RELOAD_FAILED');

    fireEvent.click(
      within(screen.getByTestId('refund-recovery-51').closest('td') as HTMLElement).getByRole(
        'button',
        { name: '상태 다시 확인' },
      ),
    );

    expect(await screen.findByTestId('refund-recovery-51')).toHaveTextContent('COMMITTED');
    const row = screen.getByText(item.orderId).closest('tr') as HTMLElement;
    expect(within(row).getByText('DETAIL-COMMITTED-REFUND')).toBeInTheDocument();
    expect(mocks.fetchAdminPaymentRefund).toHaveBeenCalledTimes(3);
    expect(mocks.executeAdminPaymentRefund).toHaveBeenCalledTimes(1);
  });

  it('maps correction RELOAD_FAILED to COMMITTED when manual detail succeeds', async () => {
    const item = correction('APPROVED');
    const fresh = correction('SUCCEEDED', { targetPlanName: 'DETAIL-COMMITTED-CORRECTION' });
    mocks.fetchAdminPaymentEntitlementCorrections
      .mockReset()
      .mockResolvedValueOnce(pageWith([item]))
      .mockRejectedValueOnce(new Error('list reload failed'));
    mocks.executeAdminPaymentEntitlementCorrection.mockResolvedValueOnce(correction('SUCCEEDED'));
    mocks.fetchAdminPaymentEntitlementCorrection
      .mockResolvedValueOnce(item)
      .mockResolvedValueOnce(correction('SUCCEEDED'))
      .mockResolvedValueOnce(fresh);
    render(<PaymentOperationsPage />);
    await waitFor(() => expect(mocks.fetchAdminPaymentOrders).toHaveBeenCalledTimes(1));
    fireEvent.click(screen.getByRole('button', { name: '권한 보정' }));
    await waitFor(() =>
      expect(mocks.fetchAdminPaymentEntitlementCorrections).toHaveBeenCalledTimes(1),
    );
    executeCorrectionRow(item);
    expect(await screen.findByTestId('correction-recovery-61')).toHaveTextContent('RELOAD_FAILED');

    fireEvent.click(
      within(screen.getByTestId('correction-recovery-61').closest('td') as HTMLElement).getByRole(
        'button',
        { name: '상태 다시 확인' },
      ),
    );

    expect(await screen.findByTestId('correction-recovery-61')).toHaveTextContent('COMMITTED');
    const row = screen.getByText(item.userNickname).closest('tr') as HTMLElement;
    expect(within(row).getByText('DETAIL-COMMITTED-CORRECTION')).toBeInTheDocument();
    expect(mocks.fetchAdminPaymentEntitlementCorrection).toHaveBeenCalledTimes(3);
    expect(mocks.executeAdminPaymentEntitlementCorrection).toHaveBeenCalledTimes(1);
  });

  it.each(['FAILED', 'CANCELLED'] as const)(
    'maps refund RELOAD_FAILED to FAILED when manual detail succeeds with %s',
    async (status) => {
      const item = refund('APPROVED');
      const terminal = refund(status, { failureCode: `DETAIL-${status}` });
      mocks.fetchAdminPaymentRefunds
        .mockReset()
        .mockResolvedValueOnce(pageWith([item]))
        .mockRejectedValueOnce(new Error('list reload failed'));
      mocks.executeAdminPaymentRefund.mockResolvedValueOnce(refund('SUCCEEDED'));
      mocks.fetchAdminPaymentRefund
        .mockResolvedValueOnce(item)
        .mockResolvedValueOnce(refund('SUCCEEDED'))
        .mockResolvedValueOnce(terminal);
      render(<PaymentOperationsPage />);
      await waitFor(() => expect(mocks.fetchAdminPaymentOrders).toHaveBeenCalledTimes(1));
      fireEvent.click(screen.getAllByRole('button')[7]);
      await waitFor(() => expect(mocks.fetchAdminPaymentRefunds).toHaveBeenCalledTimes(1));
      executeRefundRow(item);
      expect(await screen.findByTestId('refund-recovery-51')).toHaveTextContent('RELOAD_FAILED');

      fireEvent.click(
        within(screen.getByTestId('refund-recovery-51').closest('td') as HTMLElement).getByRole(
          'button',
        ),
      );

      expect(await screen.findByTestId('refund-recovery-51')).toHaveTextContent('FAILED');
      const row = screen.getByText(item.orderId).closest('tr') as HTMLElement;
      expect(screen.getByTestId('refund-recovery-51').closest('td')).toHaveTextContent(status);
      expect(within(row).getByText(`DETAIL-${status}`)).toBeInTheDocument();
      expect(mocks.executeAdminPaymentRefund).toHaveBeenCalledTimes(1);
    },
  );

  it.each(['FAILED', 'CANCELLED'] as const)(
    'maps correction RELOAD_FAILED to FAILED when manual detail succeeds with %s',
    async (status) => {
      const item = correction('APPROVED');
      const terminal = correction(status, { failureCode: `DETAIL-${status}` });
      mocks.fetchAdminPaymentEntitlementCorrections
        .mockReset()
        .mockResolvedValueOnce(pageWith([item]))
        .mockRejectedValueOnce(new Error('list reload failed'));
      mocks.executeAdminPaymentEntitlementCorrection.mockResolvedValueOnce(correction('SUCCEEDED'));
      mocks.fetchAdminPaymentEntitlementCorrection
        .mockResolvedValueOnce(item)
        .mockResolvedValueOnce(correction('SUCCEEDED'))
        .mockResolvedValueOnce(terminal);
      render(<PaymentOperationsPage />);
      await waitFor(() => expect(mocks.fetchAdminPaymentOrders).toHaveBeenCalledTimes(1));
      fireEvent.click(screen.getAllByRole('button')[8]);
      await waitFor(() =>
        expect(mocks.fetchAdminPaymentEntitlementCorrections).toHaveBeenCalledTimes(1),
      );
      executeCorrectionRow(item);
      expect(await screen.findByTestId('correction-recovery-61')).toHaveTextContent(
        'RELOAD_FAILED',
      );

      fireEvent.click(
        within(screen.getByTestId('correction-recovery-61').closest('td') as HTMLElement).getByRole(
          'button',
        ),
      );

      expect(await screen.findByTestId('correction-recovery-61')).toHaveTextContent('FAILED');
      const row = screen.getByText(item.userNickname).closest('tr') as HTMLElement;
      const statusCell = screen.getByTestId('correction-recovery-61').closest('td') as HTMLElement;
      expect(statusCell).toHaveTextContent(status);
      expect(within(statusCell).getByText(`DETAIL-${status}`)).toBeInTheDocument();
      expect(row).toContainElement(statusCell);
      expect(mocks.executeAdminPaymentEntitlementCorrection).toHaveBeenCalledTimes(1);
    },
  );

  it.each(['PROCESSING', 'PENDING_PROVIDER_CONFIRMATION'] as const)(
    'maps refund RELOAD_FAILED to locked UNKNOWN when manual detail succeeds with %s',
    async (status) => {
      const item = refund('APPROVED');
      mocks.fetchAdminPaymentRefunds
        .mockReset()
        .mockResolvedValueOnce(pageWith([item]))
        .mockRejectedValueOnce(new Error('list reload failed'));
      mocks.executeAdminPaymentRefund.mockResolvedValueOnce(refund('SUCCEEDED'));
      mocks.fetchAdminPaymentRefund
        .mockResolvedValueOnce(item)
        .mockResolvedValueOnce(refund('SUCCEEDED'))
        .mockResolvedValueOnce(refund(status));
      render(<PaymentOperationsPage />);
      await waitFor(() => expect(mocks.fetchAdminPaymentOrders).toHaveBeenCalledTimes(1));
      fireEvent.click(screen.getAllByRole('button')[7]);
      await waitFor(() => expect(mocks.fetchAdminPaymentRefunds).toHaveBeenCalledTimes(1));
      executeRefundRow(item);
      expect(await screen.findByTestId('refund-recovery-51')).toHaveTextContent('RELOAD_FAILED');
      fireEvent.click(
        within(screen.getByTestId('refund-recovery-51').closest('td') as HTMLElement).getByRole(
          'button',
        ),
      );

      expect(await screen.findByTestId('refund-recovery-51')).toHaveTextContent('UNKNOWN');
      const row = screen.getByText(item.orderId).closest('tr') as HTMLElement;
      expect(within(row).getByText(status)).toBeInTheDocument();
      expect(within(row).getByRole('button', { name: '실행' })).toBeDisabled();
      expect(within(row).getByRole('button', { name: '상태 다시 확인' })).toBeEnabled();
      expect(mocks.executeAdminPaymentRefund).toHaveBeenCalledTimes(1);
    },
  );

  it('maps correction RELOAD_FAILED to locked UNKNOWN when manual detail succeeds in-flight', async () => {
    const item = correction('APPROVED');
    mocks.fetchAdminPaymentEntitlementCorrections
      .mockReset()
      .mockResolvedValueOnce(pageWith([item]))
      .mockRejectedValueOnce(new Error('list reload failed'));
    mocks.executeAdminPaymentEntitlementCorrection.mockResolvedValueOnce(correction('SUCCEEDED'));
    mocks.fetchAdminPaymentEntitlementCorrection
      .mockResolvedValueOnce(item)
      .mockResolvedValueOnce(correction('SUCCEEDED'))
      .mockResolvedValueOnce(correction('PROCESSING'));
    render(<PaymentOperationsPage />);
    await waitFor(() => expect(mocks.fetchAdminPaymentOrders).toHaveBeenCalledTimes(1));
    fireEvent.click(screen.getAllByRole('button')[8]);
    await waitFor(() =>
      expect(mocks.fetchAdminPaymentEntitlementCorrections).toHaveBeenCalledTimes(1),
    );
    executeCorrectionRow(item);
    expect(await screen.findByTestId('correction-recovery-61')).toHaveTextContent('RELOAD_FAILED');
    fireEvent.click(
      within(screen.getByTestId('correction-recovery-61').closest('td') as HTMLElement).getByRole(
        'button',
      ),
    );

    expect(await screen.findByTestId('correction-recovery-61')).toHaveTextContent('UNKNOWN');
    const row = screen.getByText(item.userNickname).closest('tr') as HTMLElement;
    expect(within(row).getByText('PROCESSING')).toBeInTheDocument();
    expect(within(row).getByRole('button', { name: '실행' })).toBeDisabled();
    expect(within(row).getByRole('button', { name: '상태 다시 확인' })).toBeEnabled();
    expect(mocks.executeAdminPaymentEntitlementCorrection).toHaveBeenCalledTimes(1);
  });

  it.each([
    ['FAILED', 'FAILED', '환불 실행이 최종 실패 상태로 확인되었습니다.'],
    [
      'PROCESSING',
      'UNKNOWN',
      '환불 후속 상태가 아직 확정되지 않았습니다. 상태를 다시 확인해주세요.',
    ],
    [
      'PENDING_PROVIDER_CONFIRMATION',
      'UNKNOWN',
      '환불 후속 상태가 아직 확정되지 않았습니다. 상태를 다시 확인해주세요.',
    ],
  ] as const)(
    'uses authoritative refund detail %s after a SUCCEEDED execute response',
    async (status, outcome, message) => {
      const item = refund('APPROVED');
      mocks.fetchAdminPaymentRefund
        .mockResolvedValueOnce(item)
        .mockResolvedValueOnce(refund(status));
      mocks.executeAdminPaymentRefund.mockResolvedValueOnce(refund('SUCCEEDED'));
      await openRefunds([item]);

      executeRefundRow(item);

      expect(await screen.findByTestId('refund-recovery-51')).toHaveTextContent(outcome);
      expect(mocks.showToast).toHaveBeenLastCalledWith('error', message);
      expect(mocks.executeAdminPaymentRefund).toHaveBeenCalledTimes(1);
      expect(mocks.fetchAdminPaymentRefund).toHaveBeenCalledTimes(2);
      expect(mocks.fetchAdminPaymentRefunds).toHaveBeenCalledTimes(1);
    },
  );

  it.each([
    ['FAILED', 'FAILED', '권한 보정이 최종 실패 상태로 확인되었습니다.'],
    [
      'PROCESSING',
      'UNKNOWN',
      '권한 보정 후속 상태가 아직 확정되지 않았습니다. 상태를 다시 확인해주세요.',
    ],
  ] as const)(
    'uses authoritative correction detail %s after a SUCCEEDED execute response',
    async (status, outcome, message) => {
      const item = correction('APPROVED');
      mocks.fetchAdminPaymentEntitlementCorrection
        .mockResolvedValueOnce(item)
        .mockResolvedValueOnce(correction(status));
      mocks.executeAdminPaymentEntitlementCorrection.mockResolvedValueOnce(correction('SUCCEEDED'));
      await openCorrections([item]);

      executeCorrectionRow(item);

      expect(await screen.findByTestId('correction-recovery-61')).toHaveTextContent(outcome);
      expect(mocks.showToast).toHaveBeenLastCalledWith('error', message);
      expect(mocks.executeAdminPaymentEntitlementCorrection).toHaveBeenCalledTimes(1);
      expect(mocks.fetchAdminPaymentEntitlementCorrection).toHaveBeenCalledTimes(2);
      expect(mocks.fetchAdminPaymentEntitlementCorrections).toHaveBeenCalledTimes(1);
    },
  );

  it('keeps refund RELOAD_FAILED when the required post-execute detail read fails', async () => {
    const item = refund('APPROVED');
    mocks.fetchAdminPaymentRefund
      .mockResolvedValueOnce(item)
      .mockRejectedValueOnce(new Error('detail unavailable'));
    mocks.executeAdminPaymentRefund.mockResolvedValueOnce(refund('SUCCEEDED'));
    await openRefunds([item]);

    executeRefundRow(item);

    expect(await screen.findByTestId('refund-recovery-51')).toHaveTextContent('RELOAD_FAILED');
    expect(mocks.showToast).toHaveBeenLastCalledWith(
      'error',
      '환불은 성공했지만 후속 정보를 새로고침하지 못했습니다.',
    );
    expect(mocks.executeAdminPaymentRefund).toHaveBeenCalledTimes(1);
    expect(mocks.fetchAdminPaymentRefund).toHaveBeenCalledTimes(2);
  });

  it('keeps correction RELOAD_FAILED when the required post-execute detail read fails', async () => {
    const item = correction('APPROVED');
    mocks.fetchAdminPaymentEntitlementCorrection
      .mockResolvedValueOnce(item)
      .mockRejectedValueOnce(new Error('detail unavailable'));
    mocks.executeAdminPaymentEntitlementCorrection.mockResolvedValueOnce(correction('SUCCEEDED'));
    await openCorrections([item]);

    executeCorrectionRow(item);

    expect(await screen.findByTestId('correction-recovery-61')).toHaveTextContent('RELOAD_FAILED');
    expect(mocks.showToast).toHaveBeenLastCalledWith(
      'error',
      '권한 보정은 성공했지만 후속 정보를 새로고침하지 못했습니다.',
    );
    expect(mocks.executeAdminPaymentEntitlementCorrection).toHaveBeenCalledTimes(1);
    expect(mocks.fetchAdminPaymentEntitlementCorrection).toHaveBeenCalledTimes(2);
  });

  it('ignores an old refund success list after tab switch and a newer refund load', async () => {
    const item = refund('APPROVED');
    const oldList = deferred<PagedResponse<AdminPaymentRefund>>();
    const latest = refund('SUCCEEDED', { id: 53, orderId: 'LATEST-REFUND-LIST' });
    const stale = refund('SUCCEEDED', { id: 52, orderId: 'STALE-REFUND-LIST' });
    mocks.fetchAdminPaymentRefunds
      .mockReset()
      .mockResolvedValueOnce(pageWith([item]))
      .mockReturnValueOnce(oldList.promise)
      .mockResolvedValueOnce(pageWith([refund('SUCCEEDED'), latest]));
    mocks.fetchAdminPaymentRefund
      .mockResolvedValueOnce(item)
      .mockResolvedValueOnce(refund('SUCCEEDED'));
    mocks.executeAdminPaymentRefund.mockResolvedValueOnce(refund('SUCCEEDED'));
    render(<PaymentOperationsPage />);
    await waitFor(() => expect(mocks.fetchAdminPaymentOrders).toHaveBeenCalledTimes(1));
    fireEvent.click(screen.getByRole('button', { name: '환불' }));
    await waitFor(() => expect(mocks.fetchAdminPaymentRefunds).toHaveBeenCalledTimes(1));
    executeRefundRow(item);
    await waitFor(() => expect(mocks.fetchAdminPaymentRefunds).toHaveBeenCalledTimes(2));

    fireEvent.click(screen.getAllByRole('button', { name: '권한 보정' })[0]);
    fireEvent.click(screen.getByRole('button', { name: '환불' }));
    expect(await screen.findByText('LATEST-REFUND-LIST')).toBeInTheDocument();
    await act(async () => oldList.resolve(pageWith([refund('SUCCEEDED'), stale])));

    expect(screen.getByText('LATEST-REFUND-LIST')).toBeInTheDocument();
    expect(screen.queryByText('STALE-REFUND-LIST')).not.toBeInTheDocument();
    expect(screen.getByTestId('refund-recovery-51')).toHaveTextContent('COMMITTED');
  });

  it('ignores an old correction success list after tab switch and a newer correction load', async () => {
    const item = correction('APPROVED');
    const oldList = deferred<PagedResponse<AdminPaymentEntitlementCorrection>>();
    const latest = correction('SUCCEEDED', { id: 63, userNickname: 'latest-correction-list' });
    const stale = correction('SUCCEEDED', { id: 62, userNickname: 'stale-correction-list' });
    mocks.fetchAdminPaymentEntitlementCorrections
      .mockReset()
      .mockResolvedValueOnce(pageWith([item]))
      .mockReturnValueOnce(oldList.promise)
      .mockResolvedValueOnce(pageWith([correction('SUCCEEDED'), latest]));
    mocks.fetchAdminPaymentEntitlementCorrection
      .mockResolvedValueOnce(item)
      .mockResolvedValueOnce(correction('SUCCEEDED'));
    mocks.executeAdminPaymentEntitlementCorrection.mockResolvedValueOnce(correction('SUCCEEDED'));
    render(<PaymentOperationsPage />);
    await waitFor(() => expect(mocks.fetchAdminPaymentOrders).toHaveBeenCalledTimes(1));
    fireEvent.click(screen.getByRole('button', { name: '권한 보정' }));
    await waitFor(() =>
      expect(mocks.fetchAdminPaymentEntitlementCorrections).toHaveBeenCalledTimes(1),
    );
    executeCorrectionRow(item);
    await waitFor(() =>
      expect(mocks.fetchAdminPaymentEntitlementCorrections).toHaveBeenCalledTimes(2),
    );

    fireEvent.click(screen.getByRole('button', { name: '환불' }));
    fireEvent.click(screen.getByRole('button', { name: '권한 보정' }));
    expect(await screen.findByText('latest-correction-list')).toBeInTheDocument();
    await act(async () => oldList.resolve(pageWith([correction('SUCCEEDED'), stale])));

    expect(screen.getByText('latest-correction-list')).toBeInTheDocument();
    expect(screen.queryByText('stale-correction-list')).not.toBeInTheDocument();
    expect(screen.getByTestId('correction-recovery-61')).toHaveTextContent('COMMITTED');
  });

  it('keeps refund COMMITTED when an old success list fails after ownership changes', async () => {
    const item = refund('APPROVED');
    const oldList = deferred<PagedResponse<AdminPaymentRefund>>();
    mocks.fetchAdminPaymentRefunds
      .mockReset()
      .mockResolvedValueOnce(pageWith([item]))
      .mockReturnValueOnce(oldList.promise)
      .mockResolvedValueOnce(pageWith([refund('SUCCEEDED')]));
    mocks.fetchAdminPaymentRefund
      .mockResolvedValueOnce(item)
      .mockResolvedValueOnce(refund('SUCCEEDED'));
    mocks.executeAdminPaymentRefund.mockResolvedValueOnce(refund('SUCCEEDED'));
    render(<PaymentOperationsPage />);
    await waitFor(() => expect(mocks.fetchAdminPaymentOrders).toHaveBeenCalledTimes(1));
    fireEvent.click(screen.getByRole('button', { name: '환불' }));
    await waitFor(() => expect(mocks.fetchAdminPaymentRefunds).toHaveBeenCalledTimes(1));
    executeRefundRow(item);
    await waitFor(() => expect(mocks.fetchAdminPaymentRefunds).toHaveBeenCalledTimes(2));
    fireEvent.click(screen.getAllByRole('button', { name: '권한 보정' })[0]);
    fireEvent.click(screen.getByRole('button', { name: '환불' }));
    await waitFor(() => expect(mocks.fetchAdminPaymentRefunds).toHaveBeenCalledTimes(3));

    await act(async () => oldList.reject(new Error('old list failed')));

    expect(screen.getByTestId('refund-recovery-51')).toHaveTextContent('COMMITTED');
  });

  it('keeps correction COMMITTED when an old success list fails after ownership changes', async () => {
    const item = correction('APPROVED');
    const oldList = deferred<PagedResponse<AdminPaymentEntitlementCorrection>>();
    mocks.fetchAdminPaymentEntitlementCorrections
      .mockReset()
      .mockResolvedValueOnce(pageWith([item]))
      .mockReturnValueOnce(oldList.promise)
      .mockResolvedValueOnce(pageWith([correction('SUCCEEDED')]));
    mocks.fetchAdminPaymentEntitlementCorrection
      .mockResolvedValueOnce(item)
      .mockResolvedValueOnce(correction('SUCCEEDED'));
    mocks.executeAdminPaymentEntitlementCorrection.mockResolvedValueOnce(correction('SUCCEEDED'));
    render(<PaymentOperationsPage />);
    await waitFor(() => expect(mocks.fetchAdminPaymentOrders).toHaveBeenCalledTimes(1));
    fireEvent.click(screen.getByRole('button', { name: '권한 보정' }));
    await waitFor(() =>
      expect(mocks.fetchAdminPaymentEntitlementCorrections).toHaveBeenCalledTimes(1),
    );
    executeCorrectionRow(item);
    await waitFor(() =>
      expect(mocks.fetchAdminPaymentEntitlementCorrections).toHaveBeenCalledTimes(2),
    );
    fireEvent.click(screen.getByRole('button', { name: '환불' }));
    fireEvent.click(screen.getByRole('button', { name: '권한 보정' }));
    await waitFor(() =>
      expect(mocks.fetchAdminPaymentEntitlementCorrections).toHaveBeenCalledTimes(3),
    );

    await act(async () => oldList.reject(new Error('old list failed')));

    expect(screen.getByTestId('correction-recovery-61')).toHaveTextContent('COMMITTED');
  });

  it('deduplicates refund status reads and keeps a newer detail over a later stale list', async () => {
    const item = refund('APPROVED');
    const statusRead = deferred<AdminPaymentRefund>();
    mocks.executeAdminPaymentRefund.mockRejectedValueOnce(new Error('network'));
    mocks.fetchAdminPaymentRefund
      .mockResolvedValueOnce(item)
      .mockResolvedValueOnce(refund('PROCESSING'))
      .mockReturnValueOnce(statusRead.promise);
    await openRefunds([item]);
    executeRefundRow(item);
    expect(await screen.findByTestId('refund-recovery-51')).toHaveTextContent('UNKNOWN');

    const statusButton = screen.getByRole('button', { name: '상태 다시 확인' });
    fireEvent.click(statusButton);
    fireEvent.click(statusButton);
    expect(mocks.fetchAdminPaymentRefund).toHaveBeenCalledTimes(3);
    await act(async () => statusRead.resolve(refund('SUCCEEDED')));

    expect(await screen.findByTestId('refund-recovery-51')).toHaveTextContent('COMMITTED');
    expect(screen.getByText('SUCCEEDED')).toBeInTheDocument();
    expect(mocks.executeAdminPaymentRefund).toHaveBeenCalledTimes(1);

    fireEvent.click(screen.getAllByRole('button')[8]);
    fireEvent.click(screen.getAllByRole('button')[7]);
    await waitFor(() => expect(mocks.fetchAdminPaymentRefunds).toHaveBeenCalledTimes(2));
    expect(screen.getByText('SUCCEEDED')).toBeInTheDocument();
  });

  it('deduplicates correction status reads and keeps its detail over a later stale list', async () => {
    const item = correction('APPROVED');
    const statusRead = deferred<AdminPaymentEntitlementCorrection>();
    mocks.executeAdminPaymentEntitlementCorrection.mockRejectedValueOnce(new Error('network'));
    mocks.fetchAdminPaymentEntitlementCorrection
      .mockResolvedValueOnce(item)
      .mockResolvedValueOnce(correction('PROCESSING'))
      .mockReturnValueOnce(statusRead.promise);
    await openCorrections([item]);
    executeCorrectionRow(item);
    expect(await screen.findByTestId('correction-recovery-61')).toHaveTextContent('UNKNOWN');

    const statusButton = screen.getByRole('button', { name: '상태 다시 확인' });
    fireEvent.click(statusButton);
    fireEvent.click(statusButton);
    expect(mocks.fetchAdminPaymentEntitlementCorrection).toHaveBeenCalledTimes(3);
    await act(async () => statusRead.resolve(correction('SUCCEEDED')));

    expect(await screen.findByTestId('correction-recovery-61')).toHaveTextContent('COMMITTED');
    expect(screen.queryByTestId('refund-recovery-51')).not.toBeInTheDocument();
    expect(mocks.executeAdminPaymentEntitlementCorrection).toHaveBeenCalledTimes(1);

    fireEvent.click(screen.getAllByRole('button')[7]);
    fireEvent.click(screen.getAllByRole('button')[8]);
    await waitFor(() =>
      expect(mocks.fetchAdminPaymentEntitlementCorrections).toHaveBeenCalledTimes(2),
    );
    expect(screen.getByText('SUCCEEDED')).toBeInTheDocument();
  });

  it('keeps refund preflight as the only status owner until a terminal result releases it', async () => {
    const item = refund('APPROVED');
    const linked = correction('APPROVED');
    const preflight = deferred<AdminPaymentRefund>();
    mocks.fetchAdminPaymentRefunds.mockReset().mockResolvedValue(pageWith([item]));
    mocks.fetchAdminPaymentEntitlementCorrections.mockReset().mockResolvedValue(pageWith([linked]));
    mocks.fetchAdminPaymentRefund.mockReset().mockReturnValueOnce(preflight.promise);
    render(<PaymentOperationsPage />);
    await waitFor(() => expect(mocks.fetchAdminPaymentOrders).toHaveBeenCalledTimes(1));
    fireEvent.click(screen.getByRole('button', { name: '환불' }));
    await waitFor(() => expect(mocks.fetchAdminPaymentRefunds).toHaveBeenCalledTimes(1));

    const row = executeRefundRow(item);
    expect(await screen.findByTestId('refund-recovery-51')).toHaveTextContent('UNKNOWN');
    const statusButton = within(row).getByRole('button', { name: '상태 다시 확인' });
    expect(statusButton).toBeDisabled();
    fireEvent.click(statusButton);
    statusButton.removeAttribute('disabled');
    fireEvent.click(statusButton);
    expect(mocks.fetchAdminPaymentRefund).toHaveBeenCalledTimes(1);

    fireEvent.click(screen.getAllByRole('button', { name: '권한 보정' })[0]);
    const linkedRow = (await screen.findByText(linked.userNickname)).closest('tr') as HTMLElement;
    const linkedExecute = within(linkedRow).getByRole('button', { name: '실행' });
    expect(linkedExecute).toBeDisabled();
    fireEvent.click(linkedExecute);
    expect(mocks.executeAdminPaymentEntitlementCorrection).not.toHaveBeenCalled();

    await act(async () => preflight.resolve(refund('SUCCEEDED')));
    await waitFor(() =>
      expect(mocks.showToast).toHaveBeenLastCalledWith('success', '환불이 이미 완료된 상태입니다.'),
    );
    expect(mocks.fetchAdminPaymentRefund).toHaveBeenCalledTimes(1);
    expect(mocks.executeAdminPaymentRefund).not.toHaveBeenCalled();
  });

  it('keeps correction preflight as the only status owner until a terminal result releases it', async () => {
    const item = correction('APPROVED');
    const linked = refund('APPROVED');
    const preflight = deferred<AdminPaymentEntitlementCorrection>();
    mocks.fetchAdminPaymentEntitlementCorrections.mockReset().mockResolvedValue(pageWith([item]));
    mocks.fetchAdminPaymentRefunds.mockReset().mockResolvedValue(pageWith([linked]));
    mocks.fetchAdminPaymentEntitlementCorrection.mockReset().mockReturnValueOnce(preflight.promise);
    render(<PaymentOperationsPage />);
    await waitFor(() => expect(mocks.fetchAdminPaymentOrders).toHaveBeenCalledTimes(1));
    fireEvent.click(screen.getByRole('button', { name: '권한 보정' }));
    await waitFor(() =>
      expect(mocks.fetchAdminPaymentEntitlementCorrections).toHaveBeenCalledTimes(1),
    );

    const row = executeCorrectionRow(item);
    expect(await screen.findByTestId('correction-recovery-61')).toHaveTextContent('UNKNOWN');
    const statusButton = within(row).getByRole('button', { name: '상태 다시 확인' });
    expect(statusButton).toBeDisabled();
    fireEvent.click(statusButton);
    statusButton.removeAttribute('disabled');
    fireEvent.click(statusButton);
    expect(mocks.fetchAdminPaymentEntitlementCorrection).toHaveBeenCalledTimes(1);

    fireEvent.click(screen.getByRole('button', { name: '환불' }));
    const linkedRow = (await screen.findByText(linked.orderId)).closest('tr') as HTMLElement;
    const linkedExecute = within(linkedRow).getByRole('button', { name: '실행' });
    expect(linkedExecute).toBeDisabled();
    fireEvent.click(linkedExecute);
    expect(mocks.executeAdminPaymentRefund).not.toHaveBeenCalled();

    await act(async () => preflight.resolve(correction('SUCCEEDED')));
    await waitFor(() =>
      expect(mocks.showToast).toHaveBeenLastCalledWith(
        'success',
        '권한 보정이 이미 완료된 상태입니다.',
      ),
    );
    expect(mocks.fetchAdminPaymentEntitlementCorrection).toHaveBeenCalledTimes(1);
    expect(mocks.executeAdminPaymentEntitlementCorrection).not.toHaveBeenCalled();
  });

  it('keeps refund status reads disabled from approved preflight through POST recovery', async () => {
    const item = refund('APPROVED');
    const linked = correction('APPROVED');
    const preflight = deferred<AdminPaymentRefund>();
    const execute = deferred<AdminPaymentRefund>();
    mocks.fetchAdminPaymentRefunds.mockReset().mockResolvedValue(pageWith([item]));
    mocks.fetchAdminPaymentEntitlementCorrections.mockReset().mockResolvedValue(pageWith([linked]));
    mocks.fetchAdminPaymentRefund
      .mockReset()
      .mockReturnValueOnce(preflight.promise)
      .mockResolvedValueOnce(refund('SUCCEEDED'));
    mocks.executeAdminPaymentRefund.mockReturnValueOnce(execute.promise);
    render(<PaymentOperationsPage />);
    await waitFor(() => expect(mocks.fetchAdminPaymentOrders).toHaveBeenCalledTimes(1));
    fireEvent.click(screen.getByRole('button', { name: '환불' }));
    await waitFor(() => expect(mocks.fetchAdminPaymentRefunds).toHaveBeenCalledTimes(1));

    const row = executeRefundRow(item);
    await act(async () => preflight.resolve(item));
    await waitFor(() => expect(mocks.executeAdminPaymentRefund).toHaveBeenCalledTimes(1));
    const statusButton = within(row).getByRole('button', { name: '상태 다시 확인' });
    expect(statusButton).toBeDisabled();
    fireEvent.click(statusButton);
    statusButton.removeAttribute('disabled');
    fireEvent.click(statusButton);
    expect(mocks.fetchAdminPaymentRefund).toHaveBeenCalledTimes(1);
    expect(screen.getByTestId('refund-recovery-51')).toHaveTextContent('UNKNOWN');

    fireEvent.click(screen.getAllByRole('button', { name: '권한 보정' })[0]);
    const linkedRow = (await screen.findByText(linked.userNickname)).closest('tr') as HTMLElement;
    const linkedExecute = within(linkedRow).getByRole('button', { name: '실행' });
    expect(linkedExecute).toBeDisabled();
    fireEvent.click(linkedExecute);
    expect(mocks.executeAdminPaymentEntitlementCorrection).not.toHaveBeenCalled();

    await act(async () => execute.resolve(refund('SUCCEEDED')));
    await waitFor(() => expect(mocks.fetchAdminPaymentRefund).toHaveBeenCalledTimes(2));
    expect(mocks.executeAdminPaymentRefund).toHaveBeenCalledTimes(1);
    fireEvent.click(screen.getByRole('button', { name: '환불' }));
    expect(await screen.findByTestId('refund-recovery-51')).toHaveTextContent('COMMITTED');
  });

  it('keeps correction status reads disabled from approved preflight through POST recovery', async () => {
    const item = correction('APPROVED');
    const linked = refund('APPROVED');
    const preflight = deferred<AdminPaymentEntitlementCorrection>();
    const execute = deferred<AdminPaymentEntitlementCorrection>();
    mocks.fetchAdminPaymentEntitlementCorrections.mockReset().mockResolvedValue(pageWith([item]));
    mocks.fetchAdminPaymentRefunds.mockReset().mockResolvedValue(pageWith([linked]));
    mocks.fetchAdminPaymentEntitlementCorrection
      .mockReset()
      .mockReturnValueOnce(preflight.promise)
      .mockResolvedValueOnce(correction('SUCCEEDED'));
    mocks.executeAdminPaymentEntitlementCorrection.mockReturnValueOnce(execute.promise);
    render(<PaymentOperationsPage />);
    await waitFor(() => expect(mocks.fetchAdminPaymentOrders).toHaveBeenCalledTimes(1));
    fireEvent.click(screen.getByRole('button', { name: '권한 보정' }));
    await waitFor(() =>
      expect(mocks.fetchAdminPaymentEntitlementCorrections).toHaveBeenCalledTimes(1),
    );

    const row = executeCorrectionRow(item);
    await act(async () => preflight.resolve(item));
    await waitFor(() =>
      expect(mocks.executeAdminPaymentEntitlementCorrection).toHaveBeenCalledTimes(1),
    );
    const statusButton = within(row).getByRole('button', { name: '상태 다시 확인' });
    expect(statusButton).toBeDisabled();
    fireEvent.click(statusButton);
    statusButton.removeAttribute('disabled');
    fireEvent.click(statusButton);
    expect(mocks.fetchAdminPaymentEntitlementCorrection).toHaveBeenCalledTimes(1);
    expect(screen.getByTestId('correction-recovery-61')).toHaveTextContent('UNKNOWN');

    fireEvent.click(screen.getByRole('button', { name: '환불' }));
    const linkedRow = (await screen.findByText(linked.orderId)).closest('tr') as HTMLElement;
    const linkedExecute = within(linkedRow).getByRole('button', { name: '실행' });
    expect(linkedExecute).toBeDisabled();
    fireEvent.click(linkedExecute);
    expect(mocks.executeAdminPaymentRefund).not.toHaveBeenCalled();

    await act(async () => execute.resolve(correction('SUCCEEDED')));
    await waitFor(() =>
      expect(mocks.fetchAdminPaymentEntitlementCorrection).toHaveBeenCalledTimes(2),
    );
    expect(mocks.executeAdminPaymentEntitlementCorrection).toHaveBeenCalledTimes(1);
    fireEvent.click(screen.getAllByRole('button', { name: '권한 보정' })[0]);
    expect(await screen.findByTestId('correction-recovery-61')).toHaveTextContent('COMMITTED');
  });

  it('allows only one refund execute while rapid clicks share one pending intent', async () => {
    const item = refund('APPROVED');
    const execute = deferred<AdminPaymentRefund>();
    mocks.executeAdminPaymentRefund.mockReturnValueOnce(execute.promise);
    await openRefunds([item]);

    const row = executeRefundRow(item);
    fireEvent.click(within(row).getAllByRole('button')[1]);
    await waitFor(() => expect(mocks.executeAdminPaymentRefund).toHaveBeenCalledTimes(1));

    await act(async () => execute.resolve(refund('FAILED')));
    expect(await screen.findByTestId('refund-recovery-51')).toHaveTextContent('FAILED');
    expect(mocks.fetchAdminPaymentRefund).toHaveBeenCalledTimes(1);
  });

  it('allows only one correction execute while rapid clicks share one pending intent', async () => {
    const item = correction('APPROVED');
    const execute = deferred<AdminPaymentEntitlementCorrection>();
    mocks.executeAdminPaymentEntitlementCorrection.mockReturnValueOnce(execute.promise);
    await openCorrections([item]);

    const row = executeCorrectionRow(item);
    fireEvent.click(within(row).getAllByRole('button')[1]);
    await waitFor(() =>
      expect(mocks.executeAdminPaymentEntitlementCorrection).toHaveBeenCalledTimes(1),
    );

    await act(async () => execute.resolve(correction('FAILED')));
    expect(await screen.findByTestId('correction-recovery-61')).toHaveTextContent('FAILED');
    expect(mocks.fetchAdminPaymentEntitlementCorrection).toHaveBeenCalledTimes(1);
  });

  it('blocks a linked correction mutation while the refund intent is UNKNOWN', async () => {
    const refundItem = refund('APPROVED');
    const correctionItem = correction('APPROVED');
    mocks.fetchAdminPaymentRefunds.mockReset().mockResolvedValue(pageWith([refundItem]));
    mocks.fetchAdminPaymentEntitlementCorrections
      .mockReset()
      .mockResolvedValue(pageWith([correctionItem]));
    mocks.executeAdminPaymentRefund.mockRejectedValueOnce(new Error('network'));
    mocks.fetchAdminPaymentRefund
      .mockResolvedValueOnce(refundItem)
      .mockResolvedValueOnce(refund('PROCESSING'));
    render(<PaymentOperationsPage />);
    await waitFor(() => expect(mocks.fetchAdminPaymentOrders).toHaveBeenCalledTimes(1));
    fireEvent.click(screen.getByRole('button', { name: '환불' }));
    await waitFor(() => expect(mocks.fetchAdminPaymentRefunds).toHaveBeenCalledTimes(1));
    executeRefundRow(refundItem);
    expect(await screen.findByTestId('refund-recovery-51')).toHaveTextContent('UNKNOWN');

    fireEvent.click(screen.getAllByRole('button', { name: '권한 보정' })[0]);
    const correctionRow = (await screen.findByText(correctionItem.userNickname)).closest(
      'tr',
    ) as HTMLElement;
    expect(within(correctionRow).getByRole('button', { name: '실행' })).toBeDisabled();
    expect(mocks.executeAdminPaymentEntitlementCorrection).not.toHaveBeenCalled();
  });

  it('blocks the linked refund mutation while the correction intent is UNKNOWN', async () => {
    const refundItem = refund('APPROVED');
    const correctionItem = correction('APPROVED');
    mocks.fetchAdminPaymentRefunds.mockReset().mockResolvedValue(pageWith([refundItem]));
    mocks.fetchAdminPaymentEntitlementCorrections
      .mockReset()
      .mockResolvedValue(pageWith([correctionItem]));
    mocks.executeAdminPaymentEntitlementCorrection.mockRejectedValueOnce(new Error('network'));
    mocks.fetchAdminPaymentEntitlementCorrection
      .mockResolvedValueOnce(correctionItem)
      .mockResolvedValueOnce(correction('PROCESSING'));
    render(<PaymentOperationsPage />);
    await waitFor(() => expect(mocks.fetchAdminPaymentOrders).toHaveBeenCalledTimes(1));
    fireEvent.click(screen.getAllByRole('button')[8]);
    await waitFor(() =>
      expect(mocks.fetchAdminPaymentEntitlementCorrections).toHaveBeenCalledTimes(1),
    );
    executeCorrectionRow(correctionItem);
    expect(await screen.findByTestId('correction-recovery-61')).toHaveTextContent('UNKNOWN');

    fireEvent.click(screen.getAllByRole('button')[7]);
    const refundRow = (await screen.findByText(refundItem.orderId)).closest('tr') as HTMLElement;
    expect(within(refundRow).getAllByRole('button')[1]).toBeDisabled();
    expect(mocks.executeAdminPaymentRefund).not.toHaveBeenCalled();
  });

  it('blocks other correction rows that share an ambiguous correction refund', async () => {
    const ambiguous = correction('APPROVED');
    const requested = correction('REQUESTED', {
      id: 62,
      userNickname: 'correction-requested',
    });
    const approved = correction('APPROVED', {
      id: 63,
      userNickname: 'correction-approved',
    });
    mocks.executeAdminPaymentEntitlementCorrection.mockRejectedValueOnce(new Error('network'));
    mocks.fetchAdminPaymentEntitlementCorrection
      .mockResolvedValueOnce(ambiguous)
      .mockResolvedValueOnce(correction('PROCESSING'));
    await openCorrections([ambiguous, requested, approved]);
    executeCorrectionRow(ambiguous);
    expect(await screen.findByTestId('correction-recovery-61')).toHaveTextContent('UNKNOWN');

    const requestedRow = screen.getByText(requested.userNickname).closest('tr') as HTMLElement;
    const approvedRow = screen.getByText(approved.userNickname).closest('tr') as HTMLElement;
    expect(within(requestedRow).getAllByRole('button')[0]).toBeDisabled();
    expect(within(approvedRow).getAllByRole('button')[1]).toBeDisabled();
    fireEvent.click(within(requestedRow).getAllByRole('button')[0]);
    fireEvent.click(within(approvedRow).getAllByRole('button')[1]);
    expect(mocks.approveAdminPaymentEntitlementCorrection).not.toHaveBeenCalled();
    expect(mocks.executeAdminPaymentEntitlementCorrection).toHaveBeenCalledTimes(1);
  });

  it('blocks correction creation for a refund with an ambiguous correction intent', async () => {
    const ambiguous = correction('APPROVED');
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
    mocks.executeAdminPaymentEntitlementCorrection.mockRejectedValueOnce(new Error('network'));
    mocks.fetchAdminPaymentEntitlementCorrection
      .mockResolvedValueOnce(ambiguous)
      .mockResolvedValueOnce(correction('PROCESSING'));
    mocks.previewAdminPaymentEntitlementCorrection.mockResolvedValueOnce(correctionPreview);
    await openCorrections([ambiguous]);
    executeCorrectionRow(ambiguous);
    expect(await screen.findByTestId('correction-recovery-61')).toHaveTextContent('UNKNOWN');

    fireEvent.change(screen.getByPlaceholderText('succeeded refund id'), {
      target: { value: '51' },
    });
    fireEvent.change(screen.getAllByRole('combobox')[0], { target: { value: '20' } });
    fireEvent.click(screen.getByRole('button', { name: '권한 보정 미리보기' }));
    await waitFor(() =>
      expect(mocks.previewAdminPaymentEntitlementCorrection).toHaveBeenCalledTimes(1),
    );

    const requestButton = screen.getByRole('button', { name: '권한 보정 요청 생성' });
    expect(requestButton).toBeDisabled();
    fireEvent.click(requestButton);
    expect(mocks.createAdminPaymentEntitlementCorrection).not.toHaveBeenCalled();
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
  });
});

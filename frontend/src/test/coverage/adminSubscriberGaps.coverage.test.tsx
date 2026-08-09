import { fireEvent, render, screen, waitFor, within } from '@testing-library/react';
import type { ReactElement } from 'react';
import { createMemoryRouter, RouterProvider } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import type {
  AdminBillingAgreement,
  AdminPaymentEntitlementCorrection,
  AdminPaymentOperationAuditLog,
  AdminPaymentOrder,
  AdminPaymentReceipt,
  AdminPaymentReconciliationIncident,
  AdminPaymentRefund,
  AdminPaymentSettlement,
  AdminSubscriptionPayment,
  AdminWhitelistChannel,
} from '@/api/admin';
import type { DownloadCount, DownloadHistoryItem } from '@/api/downloads';
import type { MeResponse } from '@/api/auth';
import type {
  AdminSubscriptionCorrection,
  AdminSubscriptionCorrectionPreview,
  MySubscription,
} from '@/api/userSubscriptions';
import type { PageInfo, PagedResponse, Playlist, User, WhitelistChannel } from '@/types';
import PaymentOperationsPage from '@/pages/admin/PaymentOperationsPage';
import UserManagePage from '@/pages/admin/UserManagePage';
import UserSubscriptionManagePage from '@/pages/admin/UserSubscriptionManagePage';
import WhitelistChannelManagePage from '@/pages/admin/WhitelistChannelManagePage';
import DownloadHistoryPage from '@/pages/subscriber/DownloadHistoryPage';
import PlaylistListPage from '@/pages/subscriber/PlaylistListPage';
import ProfilePage from '@/pages/subscriber/ProfilePage';
import WhitelistChannelPage from '@/pages/subscriber/WhitelistChannelPage';

const last = <T,>(items: T[]): T => items[items.length - 1]!;

const mocks = vi.hoisted(() => ({
  fetchAdminUserSubscriptions: vi.fn(),
  fetchOpenAdminSubscriptionCorrection: vi.fn(),
  previewAdminSubscriptionCorrection: vi.fn(),
  createAdminSubscriptionCorrection: vi.fn(),
  approveAdminSubscriptionCorrection: vi.fn(),
  executeAdminSubscriptionCorrection: vi.fn(),
  fetchAdminSubscriptionPlans: vi.fn(),
  fetchMyPlaylists: vi.fn(),
  createPlaylist: vi.fn(),
  deletePlaylist: vi.fn(),
  fetchMySubscription: vi.fn(),
  getApiErrorCode: vi.fn(),
  fetchAdminWhitelistChannels: vi.fn(),
  updateAdminWhitelistChannelStatus: vi.fn(),
  exportAdminWhitelistChannels: vi.fn(),
  downloadAdminWhitelistExportBatch: vi.fn(),
  fetchUsers: vi.fn(),
  updateUserAdmin: vi.fn(),
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
  fetchMe: vi.fn(),
  checkNicknameAvailability: vi.fn(),
  checkPhoneAvailability: vi.fn(),
  clientPut: vi.fn(),
  fetchWhitelistChannels: vi.fn(),
  registerChannel: vi.fn(),
  updateChannel: vi.fn(),
  requestWhitelistRegistration: vi.fn(),
  setPrimaryWhitelistChannel: vi.fn(),
  deleteChannel: vi.fn(),
  fetchDownloadCount: vi.fn(),
  fetchDownloadHistory: vi.fn(),
  fetchDownloadHistoryTrackIds: vi.fn(),
  downloadTrack: vi.fn(),
  triggerBlobDownload: vi.fn(),
  showToast: vi.fn(),
  updateAuthUser: vi.fn(),
  refreshCurrentUser: vi.fn(),
  playerPlay: vi.fn(),
  playerPause: vi.fn(),
  playerResume: vi.fn(),
  playerSetContext: vi.fn(),
}));

const storeState = vi.hoisted(() => ({
  auth: {
    role: 'USER',
    user: null as MeResponse | null,
    updateUser: mocks.updateAuthUser,
    refreshCurrentUser: mocks.refreshCurrentUser,
  },
  player: {
    currentTrack: null as { id: number } | null,
    isPlaying: false,
    play: mocks.playerPlay,
    pause: mocks.playerPause,
    resume: mocks.playerResume,
    setTrackListContext: mocks.playerSetContext,
  },
}));

vi.mock('@/api/userSubscriptions', () => ({
  fetchAdminUserSubscriptions: mocks.fetchAdminUserSubscriptions,
  fetchOpenAdminSubscriptionCorrection: mocks.fetchOpenAdminSubscriptionCorrection,
  previewAdminSubscriptionCorrection: mocks.previewAdminSubscriptionCorrection,
  createAdminSubscriptionCorrection: mocks.createAdminSubscriptionCorrection,
  approveAdminSubscriptionCorrection: mocks.approveAdminSubscriptionCorrection,
  executeAdminSubscriptionCorrection: mocks.executeAdminSubscriptionCorrection,
  fetchMySubscription: mocks.fetchMySubscription,
  isNoActiveSubscriptionError: (error: unknown) =>
    (error as { response?: { data?: { errorCode?: string } } })?.response?.data?.errorCode ===
    'NO_ACTIVE_SUBSCRIPTION',
}));

vi.mock('@/api/subscriptions', () => ({
  fetchAdminSubscriptionPlans: mocks.fetchAdminSubscriptionPlans,
}));

vi.mock('@/api/playlists', () => ({
  fetchMyPlaylists: mocks.fetchMyPlaylists,
  createPlaylist: mocks.createPlaylist,
  deletePlaylist: mocks.deletePlaylist,
}));

vi.mock('@/api/client', () => ({
  default: { put: mocks.clientPut },
  getApiErrorCode: mocks.getApiErrorCode,
  toUploadUrl: (path: string | null | undefined) => path,
}));

vi.mock('@/api/admin', () => ({
  fetchAdminWhitelistChannels: mocks.fetchAdminWhitelistChannels,
  updateAdminWhitelistChannelStatus: mocks.updateAdminWhitelistChannelStatus,
  exportAdminWhitelistChannels: mocks.exportAdminWhitelistChannels,
  downloadAdminWhitelistExportBatch: mocks.downloadAdminWhitelistExportBatch,
  fetchUsers: mocks.fetchUsers,
  updateUserAdmin: mocks.updateUserAdmin,
  approveAdminPaymentEntitlementCorrection: mocks.approveAdminPaymentEntitlementCorrection,
  approveAdminPaymentRefund: mocks.approveAdminPaymentRefund,
  createAdminPaymentEntitlementCorrection: mocks.createAdminPaymentEntitlementCorrection,
  createAdminPaymentRefund: mocks.createAdminPaymentRefund,
  executeAdminPaymentEntitlementCorrection: mocks.executeAdminPaymentEntitlementCorrection,
  executeAdminPaymentRefund: mocks.executeAdminPaymentRefund,
  fetchAdminBillingAgreements: mocks.fetchAdminBillingAgreements,
  fetchAdminPaymentEntitlementCorrections: mocks.fetchAdminPaymentEntitlementCorrections,
  fetchAdminPaymentOperationAuditLogs: mocks.fetchAdminPaymentOperationAuditLogs,
  fetchAdminPaymentOrders: mocks.fetchAdminPaymentOrders,
  fetchAdminPaymentReceipts: mocks.fetchAdminPaymentReceipts,
  fetchAdminPaymentReconciliationIncidents: mocks.fetchAdminPaymentReconciliationIncidents,
  fetchAdminPaymentRefundPreview: mocks.fetchAdminPaymentRefundPreview,
  fetchAdminPaymentRefunds: mocks.fetchAdminPaymentRefunds,
  fetchAdminPaymentSettlements: mocks.fetchAdminPaymentSettlements,
  fetchAdminSubscriptionPayments: mocks.fetchAdminSubscriptionPayments,
  ignoreAdminPaymentSettlement: mocks.ignoreAdminPaymentSettlement,
  importAdminPaymentSettlements: mocks.importAdminPaymentSettlements,
  previewAdminPaymentEntitlementCorrection: mocks.previewAdminPaymentEntitlementCorrection,
  reconcileAdminPaymentSettlements: mocks.reconcileAdminPaymentSettlements,
  updateAdminPaymentReconciliationIncidentStatus:
    mocks.updateAdminPaymentReconciliationIncidentStatus,
}));

vi.mock('@/api/auth', () => ({
  fetchMe: mocks.fetchMe,
  checkNicknameAvailability: mocks.checkNicknameAvailability,
  checkPhoneAvailability: mocks.checkPhoneAvailability,
}));

vi.mock('@/api/whitelistChannels', () => ({
  fetchWhitelistChannels: mocks.fetchWhitelistChannels,
  registerChannel: mocks.registerChannel,
  updateChannel: mocks.updateChannel,
  requestWhitelistRegistration: mocks.requestWhitelistRegistration,
  setPrimaryWhitelistChannel: mocks.setPrimaryWhitelistChannel,
  deleteChannel: mocks.deleteChannel,
}));

vi.mock('@/api/downloads', () => ({
  fetchDownloadCount: mocks.fetchDownloadCount,
  fetchDownloadHistory: mocks.fetchDownloadHistory,
  fetchDownloadHistoryTrackIds: mocks.fetchDownloadHistoryTrackIds,
  downloadTrack: mocks.downloadTrack,
  triggerBlobDownload: mocks.triggerBlobDownload,
}));

vi.mock('@/store/toastStore', () => ({
  useToastStore: (selector: (state: { show: typeof mocks.showToast }) => unknown) =>
    selector({ show: mocks.showToast }),
}));

vi.mock('@/store/authStore', () => {
  const useAuthStore = Object.assign(
    (selector: (state: typeof storeState.auth) => unknown) => selector(storeState.auth),
    { getState: () => storeState.auth },
  );
  return { useAuthStore };
});

vi.mock('@/store/playerStore', () => ({
  usePlayerStore: (selector: (state: typeof storeState.player) => unknown) =>
    selector(storeState.player),
}));

const pageInfo = (total: number, page = 1, size = 20): PageInfo => ({
  page,
  size,
  total,
  start: total === 0 ? 0 : (page - 1) * size + 1,
  end: Math.min(total, page * size),
  prev: page > 1,
  next: total > page * size,
});

function page<T>(items: T[], total = items.length): PagedResponse<T> {
  return { dataList: items, pageInfo: pageInfo(total) };
}

function renderRoute(
  element: ReactElement,
  initialEntry: string | { pathname: string; state?: unknown },
  path = '*',
) {
  const router = createMemoryRouter(
    [
      { path, element },
      { path: '*', element: <div data-testid="navigation-target">navigated</div> },
    ],
    { initialEntries: [initialEntry] },
  );
  const view = render(<RouterProvider router={router} />);
  return Object.assign(router, { unmount: view.unmount });
}

function profile(overrides: Partial<MeResponse> = {}): MeResponse {
  return {
    id: 1,
    nickname: 'coverage-user',
    email: 'coverage@example.com',
    phonePersonal: '010-1111-2222',
    phoneCompany: null,
    job: 'EDITOR',
    companyName: null,
    userType: 'INDIVIDUAL',
    role: 'USER',
    isVerified: true,
    createdAt: '2026-07-01T00:00:00',
    ...overrides,
  };
}

function subscription(overrides: Partial<MySubscription> = {}): MySubscription {
  return {
    id: 71,
    userId: 11,
    userNickname: 'subscriber-a4',
    subscription: {
      id: 10,
      name: 'STANDARD',
      description: 'Standard',
      userType: 'INDIVIDUAL',
      priceMonthly: 9900,
      priceYearly: 99000,
      downloadPerDay: 10,
      maxWhitelistChannels: 3,
      maxPlaylists: 3,
      isActive: true,
    },
    billingCycle: 'MONTHLY',
    status: 'ACTIVE',
    startedAt: '2026-07-01',
    expiresAt: '2026-08-01',
    pendingSubscriptionId: null,
    pendingBillingCycle: null,
    ...overrides,
  };
}

function subscriptionCorrectionPreview(
  overrides: Partial<AdminSubscriptionCorrectionPreview> = {},
): AdminSubscriptionCorrectionPreview {
  return {
    userSubscriptionId: 71,
    userId: 11,
    userNickname: 'subscriber-a4',
    currentSubscriptionId: 10,
    currentPlanName: 'STANDARD',
    currentBillingCycle: 'MONTHLY',
    currentStatus: 'ACTIVE',
    currentExpiresAt: '2099-08-01',
    currentPendingSubscriptionId: null,
    currentPendingPlanName: null,
    currentPendingBillingCycle: null,
    targetSubscriptionId: 10,
    targetPlanName: 'STANDARD',
    targetBillingCycle: 'YEARLY',
    targetStatus: 'CANCELLED',
    targetExpiresAt: '2099-09-01',
    clearPendingChange: false,
    cancelBillingAgreement: false,
    currentBillingAgreementStatus: 'ACTIVE',
    targetBillingAgreementStatus: 'ACTIVE',
    externalPaymentExecuted: false,
    executable: true,
    reason: null,
    ...overrides,
  };
}

function subscriptionCorrection(
  status: AdminSubscriptionCorrection['status'],
): AdminSubscriptionCorrection {
  return {
    id: 901,
    userSubscriptionId: 71,
    userId: 11,
    userNickname: 'subscriber-a4',
    billingAgreementId: 17,
    status,
    action: 'SET_SUBSCRIPTION_STATE',
    beforeSubscriptionId: 10,
    beforePlanName: 'STANDARD',
    beforeBillingCycle: 'MONTHLY',
    beforeStatus: 'ACTIVE',
    beforeExpiresAt: '2099-08-01',
    beforePendingSubscriptionId: null,
    beforePendingPlanName: null,
    beforePendingBillingCycle: null,
    targetSubscriptionId: 10,
    targetPlanName: 'STANDARD',
    targetBillingCycle: 'YEARLY',
    targetStatus: 'CANCELLED',
    targetExpiresAt: '2099-09-01',
    clearPendingChange: false,
    cancelBillingAgreement: false,
    beforeBillingAgreementStatus: 'ACTIVE',
    afterBillingAgreementStatus: 'ACTIVE',
    reasonNote: 'coverage correction',
    failureCode: null,
    failureMessage: null,
    requestedById: 1,
    approvedById: status === 'REQUESTED' ? null : 1,
    executedById: status === 'SUCCEEDED' ? 1 : null,
    approvalNote: status === 'REQUESTED' ? null : 'coverage approval',
    executionNote: status === 'SUCCEEDED' ? 'coverage execution' : null,
    approvedAt: status === 'REQUESTED' ? null : '2026-08-08T12:01:00',
    executedAt: status === 'SUCCEEDED' ? '2026-08-08T12:02:00' : null,
    createdAt: '2026-08-08T12:00:00',
    updatedAt: '2026-08-08T12:02:00',
  };
}

function playlist(overrides: Partial<Playlist> = {}): Playlist {
  return {
    id: 1,
    title: 'Coverage playlist',
    description: null,
    thumbnail: null,
    trackCount: 1,
    createdAt: '2026-07-01',
    ...overrides,
  };
}

function adminChannel(overrides: Partial<AdminWhitelistChannel> = {}): AdminWhitelistChannel {
  return {
    id: 31,
    userId: 4,
    userEmail: 'channel@example.com',
    userNickname: 'channel-admin-a4',
    channelUrl: 'https://www.youtube.com/@coverage',
    channelName: 'Coverage channel',
    youtubeHandle: '@coverage',
    youtubeChannelId: 'UC1234567890123456789012',
    status: 'PENDING',
    primary: false,
    adminNote: null,
    processedByEmail: null,
    planName: 'STANDARD',
    billingCycle: 'MONTHLY',
    requestedAt: '2026-07-01T00:00:00',
    exportedAt: null,
    processedAt: null,
    removalRequestedAt: null,
    createdAt: '2026-07-01T00:00:00',
    ...overrides,
  };
}

function user(overrides: Partial<User> = {}): User {
  return {
    id: 9,
    email: 'role@example.com',
    nickname: 'role-user-a4',
    role: 'USER',
    phonePersonal: null,
    phoneCompany: null,
    job: null,
    companyName: null,
    userType: 'INDIVIDUAL',
    isVerified: true,
    createdAt: '2026-07-01T00:00:00',
    ...overrides,
  };
}

function whitelistChannel(overrides: Partial<WhitelistChannel> = {}): WhitelistChannel {
  return {
    id: 21,
    channelUrl: 'https://www.youtube.com/@subscriber',
    channelName: 'Subscriber channel A4',
    youtubeHandle: '@subscriber',
    youtubeChannelId: 'UC1234567890123456789012',
    status: 'DRAFT',
    primary: false,
    adminNote: null,
    requestedAt: null,
    exportedAt: null,
    processedAt: null,
    removalRequestedAt: null,
    createdAt: '2026-07-01T00:00:00',
    ...overrides,
  };
}

function downloadItem(overrides: Partial<DownloadHistoryItem> = {}): DownloadHistoryItem {
  return {
    downloadId: 91,
    trackId: 191,
    title: 'Download track A4',
    artistName: 'AT.M',
    thumbnail: null,
    bpm: 120,
    tonality: 'C',
    duration: 180,
    waveformData: '[0.2,0.8]',
    tags: [{ id: 1, name: 'shorts', type: 'USAGE' }],
    downloadedAt: '2026-07-01T00:00:00',
    ...overrides,
  };
}

function refund(overrides: Partial<AdminPaymentRefund> = {}): AdminPaymentRefund {
  return {
    id: 101,
    subscriptionPaymentId: 301,
    paymentOrderId: 201,
    orderId: 'REFUND-REQUEST-A4',
    userId: 1,
    userNickname: 'payer',
    provider: 'TOSS',
    status: 'REQUESTED',
    amount: 9900,
    currency: 'KRW',
    reasonCode: 'CUSTOMER_REQUEST',
    reasonNote: null,
    idempotencyKey: 'refund-a4',
    providerReference: 'payment-ref-a4',
    providerRefundReference: null,
    failureCode: null,
    failureMessage: null,
    requestedById: 1,
    requestedByEmail: 'admin@example.com',
    approvedById: null,
    approvedByEmail: null,
    executedById: null,
    executedByEmail: null,
    approvedAt: null,
    executedAt: null,
    createdAt: '2026-07-01T00:00:00',
    updatedAt: '2026-07-01T00:00:00',
    ...overrides,
  };
}

function correction(
  overrides: Partial<AdminPaymentEntitlementCorrection> = {},
): AdminPaymentEntitlementCorrection {
  return {
    id: 201,
    paymentRefundId: 101,
    subscriptionPaymentId: 301,
    paymentOrderId: 201,
    orderId: 'CORRECTION-REQUEST-A4',
    userSubscriptionId: 401,
    userId: 1,
    userNickname: 'payer',
    provider: 'TOSS',
    status: 'REQUESTED',
    action: 'CHANGE_SUBSCRIPTION',
    beforeSubscriptionId: 10,
    beforePlanName: 'STANDARD',
    beforeBillingCycle: 'MONTHLY',
    beforeStatus: 'ACTIVE',
    beforeExpiresAt: '2026-08-01',
    beforePendingSubscriptionId: null,
    beforePendingPlanName: null,
    beforePendingBillingCycle: null,
    targetSubscriptionId: 20,
    targetPlanName: 'PREMIUM',
    targetBillingCycle: 'YEARLY',
    targetStatus: 'CANCELLED',
    targetExpiresAt: '2027-08-01',
    clearPendingChange: true,
    cancelBillingAgreement: true,
    beforeBillingAgreementStatus: 'ACTIVE',
    afterBillingAgreementStatus: 'CANCELLED',
    reasonNote: null,
    failureCode: null,
    failureMessage: null,
    requestedById: 1,
    requestedByEmail: 'admin@example.com',
    approvedById: null,
    approvedByEmail: null,
    executedById: null,
    executedByEmail: null,
    approvedAt: null,
    executedAt: null,
    createdAt: '2026-07-01T00:00:00',
    updatedAt: '2026-07-01T00:00:00',
    ...overrides,
  };
}

const downloadCount: DownloadCount = {
  todayDownloads: 1,
  dailyLimit: 10,
  remaining: 9,
  nextResetAt: '2026-07-02T00:00:00',
};

function resetPaymentReads() {
  mocks.fetchAdminPaymentOrders.mockResolvedValue(page([]));
  mocks.fetchAdminBillingAgreements.mockResolvedValue(page([]));
  mocks.fetchAdminSubscriptionPayments.mockResolvedValue(page([]));
  mocks.fetchAdminPaymentReconciliationIncidents.mockResolvedValue(page([]));
  mocks.fetchAdminPaymentReceipts.mockResolvedValue(page([]));
  mocks.fetchAdminPaymentOperationAuditLogs.mockResolvedValue(page([]));
  mocks.fetchAdminPaymentSettlements.mockResolvedValue(page([]));
  mocks.fetchAdminPaymentRefunds.mockResolvedValue(page([]));
  mocks.fetchAdminPaymentEntitlementCorrections.mockResolvedValue(page([]));
}

beforeEach(() => {
  for (const mock of Object.values(mocks)) mock.mockReset();

  storeState.auth.role = 'USER';
  storeState.auth.user = profile();
  storeState.player.currentTrack = null;
  storeState.player.isPlaying = false;

  mocks.fetchAdminUserSubscriptions.mockResolvedValue(page([]));
  mocks.fetchOpenAdminSubscriptionCorrection.mockResolvedValue(null);
  mocks.fetchAdminSubscriptionPlans.mockResolvedValue([]);
  mocks.fetchMyPlaylists.mockResolvedValue({ dataList: [] });
  mocks.fetchMySubscription.mockResolvedValue(subscription());
  mocks.getApiErrorCode.mockResolvedValue(null);
  mocks.fetchAdminWhitelistChannels.mockResolvedValue(page([]));
  mocks.fetchUsers.mockResolvedValue(page([]));
  resetPaymentReads();
  mocks.fetchMe.mockResolvedValue(profile());
  mocks.checkNicknameAvailability.mockResolvedValue({ available: true });
  mocks.checkPhoneAvailability.mockResolvedValue({ available: true });
  mocks.updateAuthUser.mockReturnValue(true);
  mocks.refreshCurrentUser.mockResolvedValue(profile());
  mocks.fetchWhitelistChannels.mockResolvedValue({ dataList: [] });
  mocks.fetchDownloadCount.mockResolvedValue(downloadCount);
  mocks.fetchDownloadHistory.mockResolvedValue(page([]));
  mocks.fetchDownloadHistoryTrackIds.mockResolvedValue([]);
  mocks.downloadTrack.mockResolvedValue(new Blob(['audio'], { type: 'audio/mpeg' }));

  vi.spyOn(URL, 'createObjectURL').mockReturnValue('blob:coverage');
  vi.spyOn(URL, 'revokeObjectURL').mockImplementation(() => undefined);
  vi.spyOn(HTMLAnchorElement.prototype, 'click').mockImplementation(() => undefined);
});

describe('admin subscription and user management gaps', () => {
  it('runs the local subscription correction workflow through preview, request, approval, and execution', async () => {
    const item = subscription({ expiresAt: '2099-08-01' });
    mocks.fetchAdminUserSubscriptions.mockResolvedValue(page([item]));
    mocks.previewAdminSubscriptionCorrection.mockResolvedValue(subscriptionCorrectionPreview());
    mocks.createAdminSubscriptionCorrection.mockResolvedValue(subscriptionCorrection('REQUESTED'));
    mocks.approveAdminSubscriptionCorrection.mockResolvedValue(subscriptionCorrection('APPROVED'));
    mocks.executeAdminSubscriptionCorrection.mockResolvedValue(subscriptionCorrection('SUCCEEDED'));

    render(<UserSubscriptionManagePage />);
    const row = (await screen.findByText('subscriber-a4')).closest('tr');
    expect(row).not.toBeNull();

    fireEvent.click(within(row as HTMLElement).getByRole('button', { name: '권한 보정' }));
    const dialog = screen.getByRole('dialog', { name: '사용자 구독 권한 보정' });
    await waitFor(() => expect(within(dialog).getByLabelText('운영 사유 (필수)')).toBeEnabled());
    expect(mocks.fetchOpenAdminSubscriptionCorrection).toHaveBeenCalledWith(
      71,
      expect.any(AbortSignal),
    );
    fireEvent.change(within(dialog).getByLabelText('목표 결제 주기'), {
      target: { value: 'YEARLY' },
    });
    fireEvent.change(within(dialog).getByLabelText('목표 상태'), {
      target: { value: 'CANCELLED' },
    });
    fireEvent.change(within(dialog).getByLabelText('목표 만료일'), {
      target: { value: '2099-09-01' },
    });
    fireEvent.change(within(dialog).getByLabelText('운영 사유 (필수)'), {
      target: { value: 'coverage correction' },
    });
    fireEvent.click(within(dialog).getByRole('button', { name: '미리보기' }));

    expect(await within(dialog).findByText(/외부 결제 실행 없음/)).toBeInTheDocument();
    fireEvent.click(within(dialog).getByRole('button', { name: '요청 생성' }));
    expect(await within(dialog).findByText(/요청 #901이 생성되었습니다/)).toBeInTheDocument();
    fireEvent.change(within(dialog).getByLabelText('승인 메모 (선택)'), {
      target: { value: 'coverage approval' },
    });
    fireEvent.click(within(dialog).getByRole('button', { name: '승인 단계로 이동' }));
    fireEvent.click(
      within(screen.getByRole('dialog', { name: '권한 보정 승인 확인' })).getByRole('button', {
        name: '승인 확정',
      }),
    );

    expect(await within(dialog).findByLabelText('실행 메모 (선택)')).toBeInTheDocument();
    fireEvent.change(within(dialog).getByLabelText('실행 메모 (선택)'), {
      target: { value: 'coverage execution' },
    });
    fireEvent.click(within(dialog).getByRole('button', { name: '실행 확인' }));
    fireEvent.click(
      within(screen.getByRole('dialog', { name: '권한 보정 실행 확인' })).getByRole('button', {
        name: '권한 보정 실행',
      }),
    );

    expect(await within(dialog).findByText('권한 보정 실행 완료')).toBeInTheDocument();
    expect(mocks.approveAdminSubscriptionCorrection).toHaveBeenCalledWith(
      901,
      { note: 'coverage approval' },
      expect.any(AbortSignal),
    );
    expect(mocks.executeAdminSubscriptionCorrection).toHaveBeenCalledWith(
      901,
      { note: 'coverage execution' },
      expect.any(AbortSignal),
    );
    expect(mocks.fetchAdminUserSubscriptions.mock.calls.length).toBeGreaterThanOrEqual(2);
  });

  it('blocks rejected previews and preserves the current row after ambiguous request outcomes', async () => {
    const item = subscription({ expiresAt: '2099-08-01' });
    mocks.fetchAdminUserSubscriptions.mockResolvedValueOnce(page([item]));
    mocks.previewAdminSubscriptionCorrection.mockResolvedValueOnce(
      subscriptionCorrectionPreview({
        executable: false,
        reason: 'The requested correction does not change local subscription state.',
      }),
    );

    render(<UserSubscriptionManagePage />);
    const row = (await screen.findByText('subscriber-a4')).closest('tr') as HTMLElement;
    fireEvent.click(within(row).getByRole('button', { name: '권한 보정' }));
    let dialog = screen.getByRole('dialog', { name: '사용자 구독 권한 보정' });
    await waitFor(() => expect(within(dialog).getByLabelText('운영 사유 (필수)')).toBeEnabled());
    fireEvent.change(within(dialog).getByLabelText('운영 사유 (필수)'), {
      target: { value: 'coverage rejection' },
    });
    fireEvent.click(within(dialog).getByRole('button', { name: '미리보기' }));
    expect(
      await within(dialog).findByText('현재 로컬 구독 상태와 달라지는 항목이 없습니다.'),
    ).toBeInTheDocument();
    expect(within(dialog).getByRole('button', { name: '요청 생성' })).toBeDisabled();
    fireEvent.click(last(within(dialog).getAllByRole('button', { name: '닫기' })));
    await waitFor(() =>
      expect(screen.queryByRole('dialog', { name: '사용자 구독 권한 보정' })).toBeNull(),
    );

    mocks.fetchOpenAdminSubscriptionCorrection.mockClear();
    mocks.previewAdminSubscriptionCorrection.mockResolvedValueOnce(subscriptionCorrectionPreview());
    mocks.createAdminSubscriptionCorrection.mockRejectedValueOnce(new Error('request failed'));
    fireEvent.click(within(row).getByRole('button', { name: '권한 보정' }));
    dialog = screen.getByRole('dialog', { name: '사용자 구독 권한 보정' });
    await waitFor(() => expect(within(dialog).getByLabelText('운영 사유 (필수)')).toBeEnabled());
    fireEvent.change(within(dialog).getByLabelText('목표 결제 주기'), {
      target: { value: 'YEARLY' },
    });
    fireEvent.change(within(dialog).getByLabelText('목표 상태'), {
      target: { value: 'CANCELLED' },
    });
    fireEvent.change(within(dialog).getByLabelText('목표 만료일'), {
      target: { value: '2099-09-01' },
    });
    fireEvent.change(within(dialog).getByLabelText('운영 사유 (필수)'), {
      target: { value: 'coverage request failure' },
    });
    fireEvent.click(within(dialog).getByRole('button', { name: '미리보기' }));
    await within(dialog).findByText(/외부 결제 실행 없음/);
    fireEvent.click(within(dialog).getByRole('button', { name: '요청 생성' }));
    expect(
      await within(dialog).findByText(
        '요청 생성 응답과 서버 상태를 모두 확인하지 못해 결과를 알 수 없습니다. 중복 요청 생성을 차단했습니다.',
      ),
    ).toBeInTheDocument();
    expect(within(dialog).getAllByRole('button', { name: '상태 다시 확인' })).toHaveLength(1);
    const createButton = within(dialog).getByRole('button', { name: '요청 생성' });
    expect(createButton).toBeDisabled();
    fireEvent.click(createButton);
    expect(mocks.createAdminSubscriptionCorrection).toHaveBeenCalledTimes(1);
    expect(mocks.fetchOpenAdminSubscriptionCorrection).toHaveBeenCalledTimes(2);
    expect(mocks.fetchOpenAdminSubscriptionCorrection).toHaveBeenNthCalledWith(
      2,
      71,
      expect.any(AbortSignal),
    );
    expect(mocks.fetchAdminUserSubscriptions).toHaveBeenCalledTimes(1);
    expect(row).toBeInTheDocument();
  });

  it('searches users, cancels one role change, persists another, and reports update failure', async () => {
    const current = user();
    mocks.fetchUsers.mockResolvedValue(page([current]));
    mocks.updateUserAdmin
      .mockResolvedValueOnce({ ...current, role: 'ADMIN' })
      .mockRejectedValueOnce(new Error('fail'));

    render(<UserManagePage />);
    const search = await screen.findByPlaceholderText('Search by email or nickname...');
    fireEvent.change(search, { target: { value: ' role-user ' } });
    fireEvent.keyDown(search, { key: 'Enter' });
    await waitFor(() =>
      expect(mocks.fetchUsers).toHaveBeenLastCalledWith(
        expect.objectContaining({ keyword: 'role-user' }),
        expect.any(AbortSignal),
      ),
    );

    const roleSelect = screen.getByRole('combobox');
    fireEvent.change(roleSelect, { target: { value: 'ADMIN' } });
    let dialog = screen.getByRole('dialog', { name: 'Confirm Role Change' });
    fireEvent.click(within(dialog).getByRole('button', { name: 'Cancel' }));
    expect(mocks.updateUserAdmin).not.toHaveBeenCalled();

    fireEvent.change(roleSelect, { target: { value: 'ADMIN' } });
    dialog = screen.getByRole('dialog', { name: 'Confirm Role Change' });
    fireEvent.change(within(dialog).getByLabelText('Operator reason'), {
      target: { value: 'Access approved' },
    });
    fireEvent.click(within(dialog).getByRole('button', { name: 'Confirm' }));
    await waitFor(() =>
      expect(mocks.updateUserAdmin).toHaveBeenCalledWith(9, {
        role: 'ADMIN',
        reason: 'Access approved',
      }),
    );

    fireEvent.change(screen.getByRole('combobox'), { target: { value: 'USER' } });
    dialog = screen.getByRole('dialog', { name: 'Confirm Role Change' });
    fireEvent.change(within(dialog).getByLabelText('Operator reason'), {
      target: { value: 'Access revoked' },
    });
    fireEvent.click(within(dialog).getByRole('button', { name: 'Confirm' }));
    expect(await screen.findAllByText('The role could not be changed. Try again.')).toHaveLength(2);
  });

  it('renders current empty and failed user-list states distinctly', async () => {
    mocks.fetchUsers.mockResolvedValueOnce(page([]));
    const first = render(<UserManagePage />);
    expect(await screen.findByText('No users found.')).toBeInTheDocument();
    first.unmount();

    mocks.fetchUsers.mockRejectedValueOnce(new Error('active failure'));
    render(<UserManagePage />);
    expect(await screen.findByText('Failed to load users')).toBeInTheDocument();
  });
});

describe('playlist management gaps', () => {
  it('creates a playlist with a removable thumbnail and opens each thumbnail layout', async () => {
    mocks.fetchMyPlaylists
      .mockResolvedValueOnce({
        dataList: [
          playlist({ id: 1, title: 'Single note', trackCount: 1 }),
          playlist({ id: 2, title: 'Grid notes', trackCount: 4 }),
          playlist({ id: 3, title: 'Image list', thumbnail: '/cover.jpg' }),
        ],
      })
      .mockResolvedValue({ dataList: [] });
    mocks.fetchMySubscription.mockResolvedValueOnce(
      subscription({ subscription: { ...subscription().subscription, maxPlaylists: 5 } }),
    );
    mocks.createPlaylist.mockResolvedValue(undefined);

    const router = renderRoute(<PlaylistListPage />, '/playlists', '/playlists');
    await screen.findByText('Single note');
    fireEvent.click(document.querySelector('[class*="btnNewPl"]')!);

    const dialog = screen.getByRole('dialog');
    const fileInput = dialog.querySelector<HTMLInputElement>('input[type="file"]');
    expect(fileInput).not.toBeNull();
    const image = new File(['image'], 'cover.png', { type: 'image/png' });
    fireEvent.change(fileInput!, { target: { files: [image] } });
    expect(within(dialog).getByAltText('Preview')).toHaveAttribute('src', 'blob:coverage');
    fireEvent.click(within(dialog).getByAltText('Preview').parentElement!.querySelector('button')!);
    expect(within(dialog).queryByAltText('Preview')).not.toBeInTheDocument();

    const titleInput = dialog.querySelector<HTMLInputElement>('input[type="text"]');
    const description = dialog.querySelector<HTMLTextAreaElement>('textarea');
    fireEvent.change(titleInput!, { target: { value: '  New mix  ' } });
    fireEvent.change(description!, { target: { value: '  Focus  ' } });
    fireEvent.click(last(within(dialog).getAllByRole('button')));

    await waitFor(() =>
      expect(mocks.createPlaylist).toHaveBeenCalledWith({
        title: 'New mix',
        description: 'Focus',
        thumbnail: undefined,
      }),
    );
    expect(router.state.location.pathname).toBe('/playlists');
  });

  it('blocks route-driven creation at the plan limit and reports provider limit failures', async () => {
    mocks.fetchMyPlaylists.mockResolvedValue({
      dataList: [playlist({ id: 1 }), playlist({ id: 2 }), playlist({ id: 3 })],
    });
    mocks.fetchMySubscription.mockResolvedValue(
      subscription({
        subscription: { ...subscription().subscription, maxPlaylists: 3 },
      }),
    );

    const limited = renderRoute(
      <PlaylistListPage />,
      {
        pathname: '/playlists',
        state: { openCreate: true },
      },
      '/playlists',
    );
    await waitFor(() => expect(mocks.fetchMyPlaylists).toHaveBeenCalled());
    expect(limited.state.location.pathname).toBe('/playlists');
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
    limited.unmount();
    limited.dispose();

    mocks.fetchMyPlaylists.mockResolvedValue({ dataList: [] });
    mocks.fetchMySubscription.mockResolvedValue(subscription());
    mocks.createPlaylist.mockRejectedValueOnce(new Error('limit'));
    mocks.getApiErrorCode.mockResolvedValueOnce('PLAYLIST_LIMIT_EXCEEDED');
    renderRoute(<PlaylistListPage />, '/playlists', '/playlists');
    await waitFor(() => expect(mocks.fetchMyPlaylists).toHaveBeenCalled());
    const addCard = document.querySelector('[class*="addNewCard"]');
    fireEvent.click(addCard!);
    const dialog = screen.getByRole('dialog');
    fireEvent.change(dialog.querySelector('input[type="text"]')!, { target: { value: 'Limit' } });
    fireEvent.click(last(within(dialog).getAllByRole('button')));
    await waitFor(() => expect(mocks.showToast).toHaveBeenCalledWith('error', expect.any(String)));
  });

  it('confirms playlist deletion and preserves the card after a failed delete', async () => {
    mocks.fetchMyPlaylists.mockResolvedValue({ dataList: [playlist()] });
    mocks.deletePlaylist.mockRejectedValueOnce(new Error('delete unavailable'));

    renderRoute(<PlaylistListPage />, '/playlists', '/playlists');
    const title = await screen.findByText('Coverage playlist');
    const card = title.closest('[class*="myCard"]') as HTMLElement;
    fireEvent.click(within(card).getByRole('button', { name: 'Delete playlist' }));
    const dialog = screen.getByRole('dialog');
    fireEvent.click(last(within(dialog).getAllByRole('button')));

    await waitFor(() => expect(mocks.deletePlaylist).toHaveBeenCalledWith(1));
    expect(await screen.findByText('delete unavailable')).toBeInTheDocument();
    expect(screen.getByText('Coverage playlist')).toBeInTheDocument();
  });
});

describe('whitelist administrator gaps', () => {
  it('exports the filtered CSV and redownloads the resulting batch', async () => {
    mocks.fetchAdminWhitelistChannels.mockResolvedValue(page([adminChannel()]));
    mocks.exportAdminWhitelistChannels.mockResolvedValue({
      batchId: 81,
      blob: new Blob(['csv']),
      fileName: 'channels.csv',
    });
    mocks.downloadAdminWhitelistExportBatch.mockResolvedValue({
      blob: new Blob(['csv']),
      fileName: 'channels-81.csv',
    });

    render(<WhitelistChannelManagePage />);
    await screen.findByText('Coverage channel');
    fireEvent.click(screen.getByRole('button', { name: /CSV/ }));
    const dialog = screen.getByRole('dialog');
    fireEvent.click(last(within(dialog).getAllByRole('button')));

    await waitFor(() =>
      expect(mocks.exportAdminWhitelistChannels).toHaveBeenCalledWith({
        status: 'PENDING',
        keyword: undefined,
      }),
    );
    expect(screen.getByDisplayValue('81')).toBeInTheDocument();
    const batchInput = screen.getByDisplayValue('81');
    const batchButton = batchInput.closest('label')!.nextElementSibling as HTMLButtonElement;
    fireEvent.click(batchButton);
    await waitFor(() => expect(mocks.downloadAdminWhitelistExportBatch).toHaveBeenCalledWith(81));
  });

  it('validates batch input and surfaces export, batch, status, and list failures', async () => {
    mocks.fetchAdminWhitelistChannels.mockResolvedValue(page([adminChannel()]));
    mocks.exportAdminWhitelistChannels.mockRejectedValueOnce(new Error('export'));
    mocks.downloadAdminWhitelistExportBatch.mockRejectedValueOnce(new Error('batch'));
    mocks.updateAdminWhitelistChannelStatus.mockRejectedValueOnce(new Error('status'));

    render(<WhitelistChannelManagePage />);
    await screen.findByText('Coverage channel');
    const inputs = screen.getAllByRole('textbox');
    const batchInput = inputs.find((input) => input.getAttribute('inputmode') === 'numeric')!;
    const batchButton = batchInput.closest('label')!.nextElementSibling as HTMLButtonElement;
    fireEvent.change(batchInput, { target: { value: 'bad' } });
    fireEvent.click(batchButton);
    expect(document.querySelector('[class*="error"]')).not.toBeNull();

    fireEvent.change(batchInput, { target: { value: '9' } });
    fireEvent.click(batchButton);
    await waitFor(() => expect(mocks.downloadAdminWhitelistExportBatch).toHaveBeenCalledWith(9));

    fireEvent.click(screen.getByRole('button', { name: /CSV/ }));
    fireEvent.click(last(within(screen.getByRole('dialog')).getAllByRole('button')));
    await waitFor(() => expect(mocks.exportAdminWhitelistChannels).toHaveBeenCalledTimes(1));

    const row = screen.getByText('Coverage channel').closest('tr') as HTMLElement;
    fireEvent.change(within(row).getByRole('combobox'), { target: { value: 'REGISTERED' } });
    fireEvent.click(within(row).getByRole('button'));
    fireEvent.click(last(within(screen.getByRole('dialog')).getAllByRole('button')));
    await waitFor(() => expect(mocks.updateAdminWhitelistChannelStatus).toHaveBeenCalledTimes(1));
  });

  it('searches by Enter and renders a current empty result after the request', async () => {
    mocks.fetchAdminWhitelistChannels.mockResolvedValue(page([]));
    render(<WhitelistChannelManagePage />);
    await waitFor(() => expect(mocks.fetchAdminWhitelistChannels).toHaveBeenCalledTimes(1));
    const searchInput = screen
      .getAllByRole('textbox')
      .find((input) => input.getAttribute('inputmode') !== 'numeric')!;
    fireEvent.change(searchInput, { target: { value: '  owner@example.com  ' } });
    fireEvent.keyDown(searchInput, { key: 'Enter' });
    await waitFor(() =>
      expect(mocks.fetchAdminWhitelistChannels).toHaveBeenLastCalledWith(
        expect.objectContaining({ keyword: 'owner@example.com' }),
      ),
    );
    expect(document.querySelector('[class*="empty"]')).not.toBeNull();
  });
});

describe('payment operation gaps', () => {
  it('loads every read-only ledger tab with representative rows and filters settlements', async () => {
    const order = {
      id: 1,
      orderId: 'ORDER-A4',
      userId: 1,
      userNickname: 'payer',
      provider: 'TOSS',
      purpose: 'INITIAL',
      status: 'DONE',
      amount: 9900,
      billingCycle: 'MONTHLY',
      createdAt: '2026-07-01',
      confirmedAt: '2026-07-01',
      failureCode: null,
    } as AdminPaymentOrder;
    const agreement = {
      id: 2,
      userId: 1,
      userNickname: 'payer',
      provider: 'TOSS',
      status: 'ACTIVE',
      payMethod: 'CARD',
      maskedMethod: 'card ****',
      nextBillingAt: '2026-08-01',
      failureCount: 0,
      lastChargedAt: null,
      createdAt: '2026-07-01',
    } as AdminBillingAgreement;
    const payment: AdminSubscriptionPayment = {
      id: 3,
      userId: 1,
      userNickname: 'payer',
      orderId: 'ORDER-A4',
      subscriptionName: 'STANDARD',
      provider: 'TOSS',
      amount: 9900,
      billingCycle: 'MONTHLY',
      paymentStatus: 'DONE',
      providerReference: 'REF-A4',
      createdAt: '2026-07-01',
    };
    const receipt = {
      id: 4,
      userId: 1,
      userNickname: 'payer',
      paymentOrderId: 1,
      orderId: 'ORDER-A4',
      subscriptionPaymentId: 3,
      provider: 'TOSS',
      type: 'PAYMENT_RECEIPT',
      status: 'ISSUED',
      providerReference: 'REF-A4',
      receiptReference: null,
      receiptUrl: null,
      issuedAt: '2026-07-01',
      cancelledAt: null,
      createdAt: '2026-07-01',
    } as AdminPaymentReceipt;
    const audit: AdminPaymentOperationAuditLog = {
      id: 5,
      action: 'REFUND_REQUEST',
      targetType: 'PAYMENT_REFUND',
      targetId: 1,
      actorUserId: 1,
      actorEmail: 'admin@example.com',
      targetUserId: 1,
      targetUserNickname: 'payer',
      paymentOrderId: 1,
      orderId: 'ORDER-A4',
      subscriptionPaymentId: 3,
      reconciliationIncidentId: null,
      provider: 'TOSS',
      providerReference: 'REF-A4',
      beforeStatus: null,
      afterStatus: 'REQUESTED',
      reasonCode: 'CUSTOMER_REQUEST',
      note: 'audit detail',
      createdAt: '2026-07-01',
    };
    mocks.fetchAdminPaymentOrders.mockResolvedValue(page([order]));
    mocks.fetchAdminBillingAgreements.mockResolvedValue(page([agreement]));
    mocks.fetchAdminSubscriptionPayments.mockResolvedValue(page([payment]));
    mocks.fetchAdminPaymentReceipts.mockResolvedValue(page([receipt]));
    mocks.fetchAdminPaymentOperationAuditLogs.mockResolvedValue(page([audit]));

    render(<PaymentOperationsPage />);
    expect(await screen.findByText('ORDER-A4')).toBeInTheDocument();
    for (const name of [/\?\?/, /receipt|\?\?/, /audit|\?\?/]) {
      const candidate = screen
        .getAllByRole('button')
        .find((button) => name.test(button.textContent ?? ''));
      if (candidate) fireEvent.click(candidate);
    }
    fireEvent.click(screen.getAllByRole('button')[1]);
    await waitFor(() => expect(mocks.fetchAdminBillingAgreements).toHaveBeenCalled());
    fireEvent.click(screen.getAllByRole('button')[2]);
    await waitFor(() => expect(mocks.fetchAdminSubscriptionPayments).toHaveBeenCalled());
    fireEvent.click(screen.getAllByRole('button')[4]);
    await waitFor(() => expect(mocks.fetchAdminPaymentReceipts).toHaveBeenCalled());
    fireEvent.click(screen.getAllByRole('button')[5]);
    await waitFor(() => expect(mocks.fetchAdminPaymentOperationAuditLogs).toHaveBeenCalled());

    fireEvent.click(screen.getAllByRole('button')[6]);
    await waitFor(() => expect(mocks.fetchAdminPaymentSettlements).toHaveBeenCalled());
    const filters = screen.getAllByRole('combobox');
    fireEvent.change(filters[0], { target: { value: 'MISMATCHED' } });
    fireEvent.change(filters[1], { target: { value: 'CSV_MANUAL' } });
    const dates = Array.from(document.querySelectorAll<HTMLInputElement>('input[type="date"]'));
    fireEvent.change(dates[0], { target: { value: '2026-06-01' } });
    fireEvent.change(dates[1], { target: { value: '2026-06-30' } });
    await waitFor(() =>
      expect(mocks.fetchAdminPaymentSettlements).toHaveBeenLastCalledWith(
        1,
        20,
        expect.objectContaining({
          status: 'MISMATCHED',
          source: 'CSV_MANUAL',
          baseDateFrom: '2026-06-01',
          baseDateTo: '2026-06-30',
        }),
        expect.any(AbortSignal),
      ),
    );
  });

  it('requires an ignore note, then confirms and reports a failed settlement ignore', async () => {
    const settlement = {
      id: 17,
      status: 'MISMATCHED',
      orderId: 'SETTLE-A4',
      providerReference: null,
      userId: 1,
      userNickname: 'payer',
      source: 'CSV_MANUAL',
      provider: 'TOSS',
      sourceFileName: 'settle.csv',
      sourceRowNumber: 2,
      grossAmount: 9900,
      refundAmount: 0,
      feeAmount: 100,
      netSettlementAmount: 9800,
      settlementBaseDate: '2026-07-01',
      settlementPayoutDate: '2026-07-03',
      paymentOrderId: 1,
      subscriptionPaymentId: 3,
      mismatchReason: 'fee',
      createdAt: '2026-07-01',
    } as AdminPaymentSettlement;
    mocks.fetchAdminPaymentSettlements.mockResolvedValue(page([settlement]));
    mocks.ignoreAdminPaymentSettlement.mockRejectedValueOnce(new Error('ignore failed'));

    render(<PaymentOperationsPage />);
    await waitFor(() => expect(mocks.fetchAdminPaymentOrders).toHaveBeenCalled());
    fireEvent.click(screen.getAllByRole('button')[6]);
    expect(await screen.findByText('SETTLE-A4')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: 'IGNORE' }));
    expect(mocks.showToast).toHaveBeenCalledWith('error', expect.any(String));
    fireEvent.change(screen.getByPlaceholderText(/IGNORE/), {
      target: { value: ' operator note ' },
    });
    fireEvent.click(screen.getByRole('button', { name: 'IGNORE' }));
    const dialog = screen.getByRole('dialog');
    fireEvent.click(within(dialog).getByRole('button', { name: 'IGNORE' }));
    await waitFor(() =>
      expect(mocks.ignoreAdminPaymentSettlement).toHaveBeenCalledWith(17, 'operator note'),
    );
    expect(mocks.showToast).toHaveBeenCalledWith('error', expect.any(String));
  });

  it('rejects invalid refund and correction inputs and reports plan-loading failure', async () => {
    mocks.fetchAdminSubscriptionPlans.mockRejectedValueOnce(new Error('plans failed'));
    render(<PaymentOperationsPage />);
    await waitFor(() => expect(mocks.fetchAdminPaymentOrders).toHaveBeenCalled());

    fireEvent.click(screen.getAllByRole('button')[7]);
    fireEvent.click(screen.getByRole('button', { name: /\ubbf8\ub9ac\ubcf4\uae30/ }));
    expect(mocks.fetchAdminPaymentRefundPreview).not.toHaveBeenCalled();
    expect(mocks.showToast).toHaveBeenCalledWith('error', expect.any(String));

    fireEvent.click(screen.getAllByRole('button')[8]);
    await waitFor(() => expect(mocks.fetchAdminSubscriptionPlans).toHaveBeenCalled());
    fireEvent.click(screen.getByRole('button', { name: /\ubbf8\ub9ac\ubcf4\uae30/ }));
    expect(mocks.previewAdminPaymentEntitlementCorrection).not.toHaveBeenCalled();
    expect(mocks.showToast).toHaveBeenCalledWith('error', expect.any(String));
  });

  it('approves and executes refunds only after both confirmation layers', async () => {
    const requested = refund();
    const approved = refund({
      id: 102,
      orderId: 'REFUND-EXECUTE-A4',
      status: 'APPROVED',
    });
    const succeeded = refund({
      id: 103,
      orderId: 'REFUND-CORRECTION-A4',
      status: 'SUCCEEDED',
      executedAt: '2026-07-02T00:00:00',
    });
    mocks.fetchAdminPaymentRefunds.mockResolvedValue(page([requested, approved, succeeded]));
    mocks.approveAdminPaymentRefund.mockResolvedValue(undefined);
    mocks.executeAdminPaymentRefund.mockResolvedValue(undefined);
    const prompt = vi.spyOn(window, 'prompt');

    render(<PaymentOperationsPage />);
    await waitFor(() => expect(mocks.fetchAdminPaymentOrders).toHaveBeenCalled());
    fireEvent.click(screen.getAllByRole('button')[7]);

    const requestedRow = (await screen.findByText('REFUND-REQUEST-A4')).closest(
      'tr',
    ) as HTMLElement;
    fireEvent.change(within(requestedRow).getByRole('textbox'), {
      target: { value: ' approval note ' },
    });
    fireEvent.click(within(requestedRow).getAllByRole('button')[0]);
    fireEvent.click(
      within(screen.getByRole('dialog')).getByRole('button', { name: /\ucde8\uc18c/ }),
    );
    expect(mocks.approveAdminPaymentRefund).not.toHaveBeenCalled();

    fireEvent.click(within(requestedRow).getAllByRole('button')[0]);
    fireEvent.click(last(within(screen.getByRole('dialog')).getAllByRole('button')));
    await waitFor(() =>
      expect(mocks.approveAdminPaymentRefund).toHaveBeenCalledWith(101, 'approval note'),
    );

    const approvedRow = screen.getByText('REFUND-EXECUTE-A4').closest('tr') as HTMLElement;
    prompt.mockReturnValueOnce('wrong');
    fireEvent.click(within(approvedRow).getAllByRole('button')[1]);
    expect(mocks.executeAdminPaymentRefund).not.toHaveBeenCalled();
    prompt.mockReturnValueOnce('\ud658\ubd88 \uc2e4\ud589');
    fireEvent.click(within(approvedRow).getAllByRole('button')[1]);
    await waitFor(() =>
      expect(mocks.executeAdminPaymentRefund).toHaveBeenCalledWith(102, undefined),
    );

    const succeededRow = screen.getByText('REFUND-CORRECTION-A4').closest('tr') as HTMLElement;
    fireEvent.click(within(succeededRow).getAllByRole('button')[2]);
    expect(screen.getByPlaceholderText('succeeded refund id')).toHaveValue('103');
    prompt.mockRestore();
  });

  it('edits, previews, requests, approves, and executes entitlement corrections', async () => {
    const requested = correction();
    const approved = correction({
      id: 202,
      orderId: 'CORRECTION-EXECUTE-A4',
      status: 'APPROVED',
      approvedAt: '2026-07-02T00:00:00',
    });
    mocks.fetchAdminPaymentEntitlementCorrections.mockResolvedValue(page([requested, approved]));
    mocks.fetchAdminSubscriptionPlans.mockResolvedValue([
      { ...subscription().subscription, id: 20, name: 'PREMIUM' },
    ]);
    mocks.approveAdminPaymentEntitlementCorrection.mockResolvedValue(undefined);
    mocks.executeAdminPaymentEntitlementCorrection.mockResolvedValue(undefined);
    mocks.previewAdminPaymentEntitlementCorrection.mockResolvedValue({
      paymentRefundId: 101,
      refundStatus: 'SUCCEEDED',
      userId: 1,
      userNickname: 'payer',
      userSubscriptionId: 401,
      currentSubscriptionId: 10,
      currentPlanName: 'STANDARD',
      currentBillingCycle: 'MONTHLY',
      currentStatus: 'ACTIVE',
      currentExpiresAt: '2026-08-01',
      currentPendingSubscriptionId: null,
      currentPendingPlanName: null,
      currentPendingBillingCycle: null,
      targetSubscriptionId: 20,
      targetPlanName: 'PREMIUM',
      targetBillingCycle: 'YEARLY',
      targetStatus: 'CANCELLED',
      targetExpiresAt: '2027-08-01',
      clearPendingChange: true,
      cancelBillingAgreement: true,
      currentBillingAgreementStatus: 'ACTIVE',
      targetBillingAgreementStatus: 'CANCELLED',
      executable: true,
      reason: null,
    });
    mocks.createAdminPaymentEntitlementCorrection.mockResolvedValue(undefined);
    const prompt = vi.spyOn(window, 'prompt');

    render(<PaymentOperationsPage />);
    await waitFor(() => expect(mocks.fetchAdminPaymentOrders).toHaveBeenCalled());
    fireEvent.click(screen.getAllByRole('button')[8]);

    const requestedRow = (await screen.findByText('REQUESTED')).closest('tr') as HTMLElement;
    fireEvent.change(within(requestedRow).getByRole('textbox'), {
      target: { value: ' correction approval ' },
    });
    fireEvent.click(within(requestedRow).getAllByRole('button')[0]);
    fireEvent.click(last(within(screen.getByRole('dialog')).getAllByRole('button')));
    await waitFor(() =>
      expect(mocks.approveAdminPaymentEntitlementCorrection).toHaveBeenCalledWith(
        201,
        'correction approval',
      ),
    );

    const approvedRow = screen.getByText('APPROVED').closest('tr') as HTMLElement;
    prompt.mockReturnValueOnce('wrong');
    fireEvent.click(within(approvedRow).getAllByRole('button')[1]);
    expect(mocks.executeAdminPaymentEntitlementCorrection).not.toHaveBeenCalled();
    prompt.mockReturnValueOnce('\uad8c\ud55c \ubcf4\uc815 \uc2e4\ud589');
    fireEvent.click(within(approvedRow).getAllByRole('button')[1]);
    await waitFor(() =>
      expect(mocks.executeAdminPaymentEntitlementCorrection).toHaveBeenCalledWith(202, undefined),
    );

    fireEvent.change(screen.getByPlaceholderText('succeeded refund id'), {
      target: { value: '101' },
    });
    const selects = screen.getAllByRole('combobox');
    fireEvent.change(selects[0], { target: { value: '20' } });
    fireEvent.change(selects[1], { target: { value: 'YEARLY' } });
    fireEvent.change(selects[2], { target: { value: 'CANCELLED' } });
    fireEvent.change(document.querySelector('input[type="date"]')!, {
      target: { value: '2027-08-01' },
    });
    const checks = screen.getAllByRole('checkbox');
    fireEvent.click(checks[0]);
    fireEvent.click(checks[0]);
    fireEvent.click(checks[1]);
    fireEvent.change(screen.getByPlaceholderText(/\ud658\ubd88 \ud6c4 \uad8c\ud55c/), {
      target: { value: ' correction request ' },
    });
    fireEvent.click(
      screen.getByRole('button', { name: /\uad8c\ud55c \ubcf4\uc815 \ubbf8\ub9ac\ubcf4\uae30/ }),
    );
    await waitFor(() => expect(mocks.previewAdminPaymentEntitlementCorrection).toHaveBeenCalled());
    fireEvent.click(
      screen.getByRole('button', { name: /\uad8c\ud55c \ubcf4\uc815 \uc694\uccad \uc0dd\uc131/ }),
    );
    fireEvent.click(last(within(screen.getByRole('dialog')).getAllByRole('button')));
    await waitFor(() => expect(mocks.createAdminPaymentEntitlementCorrection).toHaveBeenCalled());
    prompt.mockRestore();
  });

  it('edits an incident and reports a current mutation failure', async () => {
    const incident = {
      id: 7,
      dedupeKey: 'incident-a4',
      issueType: 'PROVIDER_MISMATCH',
      status: 'OPEN',
      severity: 'WARNING',
      paymentOrderId: 1,
      billingAgreementId: null,
      userId: 1,
      userNickname: 'payer',
      orderId: 'ORDER-A4',
      provider: 'TOSS',
      purpose: 'RENEWAL',
      localStatus: 'DONE',
      providerStatus: 'CANCELED',
      localAmount: 9900,
      providerAmount: 9900,
      providerReference: 'REF-A4',
      failureCode: null,
      failureMessage: null,
      occurrenceCount: 1,
      firstDetectedAt: '2026-07-01',
      lastDetectedAt: '2026-07-01',
      notifiedAt: null,
      resolvedAt: null,
      resolutionNote: null,
      createdAt: '2026-07-01',
    } as AdminPaymentReconciliationIncident;
    mocks.fetchAdminPaymentReconciliationIncidents.mockResolvedValue(page([incident]));
    mocks.updateAdminPaymentReconciliationIncidentStatus.mockRejectedValueOnce(new Error('save'));

    render(<PaymentOperationsPage />);
    await waitFor(() => expect(mocks.fetchAdminPaymentOrders).toHaveBeenCalled());
    fireEvent.click(screen.getAllByRole('button')[3]);
    expect(await screen.findByText('PROVIDER_MISMATCH')).toBeInTheDocument();
    const row = screen.getByText('PROVIDER_MISMATCH').closest('tr') as HTMLElement;
    fireEvent.change(within(row).getByRole('combobox'), { target: { value: 'ACKNOWLEDGED' } });
    fireEvent.change(within(row).getByRole('textbox'), { target: { value: ' reviewed ' } });
    fireEvent.click(within(row).getByRole('button'));
    await waitFor(() =>
      expect(mocks.updateAdminPaymentReconciliationIncidentStatus).toHaveBeenCalledWith(7, {
        status: 'ACKNOWLEDGED',
        note: 'reviewed',
      }),
    );
    expect(mocks.showToast).toHaveBeenCalledWith('error', expect.any(String));
  });
});

describe('profile behavior gaps', () => {
  it('renders load failure and navigates account, subscription, and activity destinations', async () => {
    mocks.fetchMe.mockRejectedValueOnce(new Error('profile unavailable'));
    const failed = renderRoute(<ProfilePage />, '/profile');
    expect(await screen.findByText('profile unavailable')).toBeInTheDocument();
    failed.unmount();
    failed.dispose();

    mocks.fetchMe.mockResolvedValue(profile());
    mocks.fetchMySubscription.mockRejectedValueOnce(new Error('none'));
    const router = renderRoute(<ProfilePage />, '/profile');
    await screen.findByText('coverage@example.com');
    const menuButtons = screen.getAllByRole('button');
    const subscriptionButton = menuButtons.find(
      (button) =>
        button.textContent?.includes('援') || button.textContent?.includes('subscription'),
    );
    if (subscriptionButton) fireEvent.click(subscriptionButton);
    const activityButton =
      menuButtons.find((button) => button.textContent?.includes('like')) ?? last(menuButtons);
    fireEvent.click(activityButton);
    expect(router.state.location.pathname).not.toBe('/profile');
  });

  it('validates nickname, phone, job, availability, and durable profile synchronization', async () => {
    mocks.fetchMe.mockResolvedValue(profile());
    renderRoute(<ProfilePage />, '/profile?tab=edit');
    await waitFor(() =>
      expect(document.querySelector('#profile-nickname')).toHaveValue('coverage-user'),
    );
    const nickname = document.querySelector<HTMLInputElement>('#profile-nickname')!;
    const phone = screen.getByPlaceholderText('010-0000-0000');

    fireEvent.change(nickname, { target: { value: 'x' } });
    fireEvent.click(last(screen.getAllByRole('button')));
    expect(mocks.clientPut).not.toHaveBeenCalled();

    fireEvent.change(nickname, { target: { value: 'coverage2' } });
    fireEvent.change(phone, { target: { value: '123' } });
    fireEvent.click(last(screen.getAllByRole('button')));
    expect(mocks.clientPut).not.toHaveBeenCalled();

    fireEvent.change(phone, { target: { value: '01022223333' } });
    const job = screen.getByRole('combobox');
    fireEvent.change(job, { target: { value: '' } });
    fireEvent.click(last(screen.getAllByRole('button')));
    expect(mocks.clientPut).not.toHaveBeenCalled();

    fireEvent.change(job, { target: { value: 'ARTIST' } });
    mocks.checkNicknameAvailability.mockResolvedValueOnce({ available: false });
    fireEvent.click(last(screen.getAllByRole('button')));
    await waitFor(() => expect(mocks.checkNicknameAvailability).toHaveBeenCalledWith('coverage2'));

    mocks.checkNicknameAvailability.mockResolvedValueOnce({ available: true });
    mocks.checkPhoneAvailability.mockResolvedValueOnce({ available: true });
    mocks.clientPut.mockResolvedValueOnce({
      data: {
        data: profile({ nickname: 'coverage2', phonePersonal: '010-2222-3333', job: 'ARTIST' }),
      },
    });
    mocks.updateAuthUser.mockReturnValueOnce(false);
    fireEvent.click(last(screen.getAllByRole('button')));
    await waitFor(() => expect(mocks.clientPut).toHaveBeenCalled());
    expect(document.querySelector('[class*="errorMsg"]')).not.toBeNull();
  });

  it('validates, saves, and reports failures for password changes', async () => {
    mocks.fetchMe.mockResolvedValue(profile());
    renderRoute(<ProfilePage />, '/profile?tab=password');
    await waitFor(() =>
      expect(document.querySelectorAll('input[type="password"]')).toHaveLength(3),
    );
    const passwordInputs = Array.from(
      document.querySelectorAll<HTMLInputElement>('input[type="password"]'),
    );
    expect(passwordInputs).toHaveLength(3);

    fireEvent.change(passwordInputs[0], { target: { value: 'old-password' } });
    fireEvent.change(passwordInputs[1], { target: { value: 'new-password-123' } });
    fireEvent.change(passwordInputs[2], { target: { value: 'different' } });
    fireEvent.click(last(screen.getAllByRole('button')));
    expect(mocks.clientPut).not.toHaveBeenCalled();

    fireEvent.change(passwordInputs[2], { target: { value: 'new-password-123' } });
    mocks.clientPut.mockResolvedValueOnce({});
    fireEvent.click(last(screen.getAllByRole('button')));
    await waitFor(() =>
      expect(mocks.clientPut).toHaveBeenCalledWith('/users/me/password', {
        currentPassword: 'old-password',
        newPassword: 'new-password-123',
      }),
    );
    expect(passwordInputs[0]).toHaveValue('');

    fireEvent.change(passwordInputs[0], { target: { value: 'old-password' } });
    fireEvent.change(passwordInputs[1], { target: { value: 'new-password-456' } });
    fireEvent.change(passwordInputs[2], { target: { value: 'new-password-456' } });
    mocks.clientPut.mockRejectedValueOnce(new Error('password unavailable'));
    fireEvent.click(last(screen.getAllByRole('button')));
    expect(await screen.findByText('password unavailable')).toBeInTheDocument();
  });
});

describe('subscriber whitelist gaps', () => {
  it('validates and creates a channel, then edits the refreshed channel', async () => {
    const channel = whitelistChannel();
    mocks.fetchWhitelistChannels
      .mockResolvedValueOnce({ dataList: [] })
      .mockResolvedValue({ dataList: [channel] });
    mocks.registerChannel.mockResolvedValue(undefined);
    mocks.updateChannel.mockResolvedValue(undefined);

    render(<WhitelistChannelPage />);
    await waitFor(() => expect(mocks.fetchWhitelistChannels).toHaveBeenCalled());
    const form = document.querySelector('form')!;
    fireEvent.submit(form);
    expect(mocks.registerChannel).not.toHaveBeenCalled();

    const inputs = within(form).getAllByRole('textbox');
    fireEvent.change(inputs[0], { target: { value: ' New channel ' } });
    fireEvent.change(inputs[2], { target: { value: 'not-youtube' } });
    fireEvent.submit(form);
    expect(mocks.registerChannel).not.toHaveBeenCalled();

    fireEvent.change(inputs[1], { target: { value: ' @new ' } });
    fireEvent.change(inputs[2], { target: { value: ' https://www.youtube.com/@new ' } });
    fireEvent.change(inputs[3], { target: { value: ' UCNEW ' } });
    fireEvent.submit(form);
    await waitFor(() =>
      expect(mocks.registerChannel).toHaveBeenCalledWith({
        channelName: 'New channel',
        channelUrl: 'https://www.youtube.com/@new',
        youtubeHandle: '@new',
        youtubeChannelId: 'UCNEW',
      }),
    );

    const card = (await screen.findByText('Subscriber channel A4')).closest(
      'article',
    ) as HTMLElement;
    const editButton = within(card).getAllByRole('button')[2];
    fireEvent.click(editButton);
    const editForm = document.querySelector('form')!;
    fireEvent.change(within(editForm).getAllByRole('textbox')[0], {
      target: { value: 'Updated channel' },
    });
    fireEvent.submit(editForm);
    await waitFor(() =>
      expect(mocks.updateChannel).toHaveBeenCalledWith(
        21,
        expect.objectContaining({ channelName: 'Updated channel' }),
      ),
    );
  });

  it('reports request and primary failures and uses the registered removal confirmation flow', async () => {
    const draft = whitelistChannel();
    const registered = whitelistChannel({
      id: 22,
      channelName: 'Registered channel',
      status: 'REGISTERED',
    });
    mocks.fetchWhitelistChannels.mockResolvedValue({ dataList: [draft, registered] });
    mocks.requestWhitelistRegistration.mockRejectedValueOnce(new Error('request failed'));
    mocks.setPrimaryWhitelistChannel.mockRejectedValueOnce(new Error('primary failed'));
    mocks.deleteChannel.mockRejectedValueOnce(new Error('delete failed'));

    render(<WhitelistChannelPage />);
    const draftCard = (await screen.findByText('Subscriber channel A4')).closest(
      'article',
    ) as HTMLElement;
    fireEvent.click(within(draftCard).getAllByRole('button')[1]);
    await waitFor(() => expect(mocks.requestWhitelistRegistration).toHaveBeenCalledWith(21));
    fireEvent.click(within(draftCard).getAllByRole('button')[0]);
    await waitFor(() => expect(mocks.setPrimaryWhitelistChannel).toHaveBeenCalledWith(21));

    const registeredCard = screen.getByText('Registered channel').closest('article') as HTMLElement;
    fireEvent.click(last(within(registeredCard).getAllByRole('button')));
    const dialog = screen.getByRole('dialog');
    fireEvent.click(last(within(dialog).getAllByRole('button')));
    await waitFor(() => expect(mocks.deleteChannel).toHaveBeenCalledWith(22));
    expect(document.querySelector('[class*="error"]')).not.toBeNull();
  });

  it('blocks registration requests without a subscription', async () => {
    mocks.fetchWhitelistChannels.mockResolvedValue({ dataList: [whitelistChannel()] });
    mocks.fetchMySubscription.mockRejectedValueOnce({
      response: { data: { errorCode: 'NO_ACTIVE_SUBSCRIPTION' } },
    });
    render(<WhitelistChannelPage />);
    const card = (await screen.findByText('Subscriber channel A4')).closest(
      'article',
    ) as HTMLElement;
    const requestButton = within(card).getAllByRole('button')[1];
    fireEvent.click(requestButton);
    expect(mocks.requestWhitelistRegistration).not.toHaveBeenCalled();
    expect(document.querySelector('[class*="error"]')).not.toBeNull();
  });
});

describe('download history gaps', () => {
  it('applies filters, toggles playback state, and downloads unique selected tracks with partial failure', async () => {
    const first = downloadItem({ downloadId: 1, trackId: 101, title: 'First download' });
    const second = downloadItem({
      downloadId: 2,
      trackId: 202,
      title: 'Second download',
      tags: [],
    });
    mocks.fetchDownloadHistory.mockResolvedValue(page([first, second]));
    mocks.downloadTrack
      .mockResolvedValueOnce(new Blob(['first']))
      .mockRejectedValueOnce(new Error('second failed'));

    renderRoute(<DownloadHistoryPage />, '/downloads', '/downloads');
    await screen.findByText('First download');
    const search = screen.getByPlaceholderText(
      /\uc81c\ubaa9 \ub610\ub294 \ud0dc\uadf8 \uac80\uc0c9/,
    );
    fireEvent.change(search, { target: { value: ' focus ' } });
    fireEvent.submit(search.closest('form')!);
    await waitFor(() =>
      expect(mocks.fetchDownloadHistory).toHaveBeenLastCalledWith(
        expect.objectContaining({ keyword: 'focus' }),
        expect.any(AbortSignal),
      ),
    );

    fireEvent.click(screen.getByRole('button', { name: 'Play First download' }));
    expect(mocks.playerPlay).toHaveBeenCalledWith(expect.objectContaining({ id: 101 }));
    storeState.player.currentTrack = { id: 101 };
    storeState.player.isPlaying = true;

    const checks = screen.getAllByRole('checkbox');
    fireEvent.click(checks[0]);
    fireEvent.click(screen.getByRole('button', { name: /\(2\)/ }));
    await waitFor(() => expect(mocks.downloadTrack).toHaveBeenCalledTimes(2));
    expect(mocks.showToast).toHaveBeenCalledWith('error', expect.any(String));
    expect(mocks.fetchDownloadCount).toHaveBeenCalledTimes(3);
  });

  it('confirms all-download, handles track-id lookup failure, and skips quota for admins', async () => {
    storeState.auth.role = 'ADMIN';
    mocks.fetchDownloadHistory.mockResolvedValue(page([downloadItem()]));
    mocks.fetchDownloadHistoryTrackIds
      .mockRejectedValueOnce(new Error('lookup failed'))
      .mockResolvedValueOnce([191]);

    renderRoute(<DownloadHistoryPage />, '/downloads', '/downloads');
    await screen.findByText('Download track A4');
    const allButton = screen.getByRole('button', {
      name: /\uc804\uccb4 \uc7ac\ub2e4\uc6b4\ub85c\ub4dc/,
    });
    fireEvent.click(allButton);
    await waitFor(() => expect(mocks.fetchDownloadHistoryTrackIds).toHaveBeenCalledTimes(1));
    expect(mocks.showToast).toHaveBeenCalledWith('error', expect.any(String));

    fireEvent.click(allButton);
    const dialog = await screen.findByRole('dialog');
    fireEvent.click(last(within(dialog).getAllByRole('button')));
    await waitFor(() => expect(mocks.downloadTrack).toHaveBeenCalledWith(191));
    expect(mocks.fetchDownloadCount).not.toHaveBeenCalled();
  });
});

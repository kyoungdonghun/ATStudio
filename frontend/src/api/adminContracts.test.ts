import { beforeEach, describe, expect, expectTypeOf, it, vi } from 'vitest';

vi.mock('@/api/client', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
  },
}));

import client from '@/api/client';
import {
  approveAdminPaymentEntitlementCorrection,
  approveAdminPaymentRefund,
  createAdminPaymentEntitlementCorrection,
  createAdminPaymentRefund,
  downloadCompanyCertDocument,
  executeAdminPaymentEntitlementCorrection,
  executeAdminPaymentRefund,
  fetchAdminBillingAgreements,
  fetchAdminPaymentEntitlementCorrections,
  fetchAdminPaymentOperationAuditLogs,
  fetchAdminPaymentOrders,
  fetchAdminPaymentReceipts,
  fetchAdminPaymentReconciliationIncidents,
  fetchAdminPaymentRefundPreview,
  fetchAdminPaymentRefunds,
  fetchAdminPaymentSettlements,
  fetchAdminSubscriptionPayments,
  fetchAdminWhitelistChannels,
  fetchCompanyCert,
  fetchCompanyCerts,
  fetchDashboardStats,
  fetchUsers,
  ignoreAdminPaymentSettlement,
  importAdminPaymentSettlements,
  previewAdminPaymentEntitlementCorrection,
  processCompanyCert,
  reconcileAdminPaymentSettlements,
  updateAdminPaymentReconciliationIncidentStatus,
  updateAdminWhitelistChannelStatus,
  updateUserAdmin,
  type AdminAssignableRole,
  type AdminUserDetail,
  type AdminUserListItem,
} from '@/api/admin';

const mockedClient = vi.mocked(client);
const entity = { id: 42, status: 'READY' };
const page = { dataList: [entity], pageInfo: { currentPage: 1, totalPages: 1 } };
const adminUserListItem: AdminUserListItem = {
  id: 4,
  nickname: 'AdminTarget',
  email: 'admin-target@example.com',
  userType: 'BUSINESS',
  role: 'USER',
  isVerified: true,
  createdAt: '2026-08-09T09:00:00',
};
const adminUserDetail: AdminUserDetail = {
  ...adminUserListItem,
  role: 'ADMIN',
  phonePersonal: null,
  phoneCompany: '02-0000-0000',
  job: null,
  companyName: 'ATStudio Partner',
};

function response<T>(data: T) {
  return { data: { data } };
}

describe('admin API contracts', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('uses dashboard, user, certification, and whitelist contracts', async () => {
    const controller = new AbortController();
    const userPage = {
      dataList: [adminUserListItem],
      pageInfo: { page: 2, size: 25, total: 1, start: 1, end: 1, prev: false, next: false },
    };
    mockedClient.get
      .mockResolvedValueOnce(
        response({ totalUsers: 10, totalTracks: 20, totalSubscribers: 3, recentUsers: [] }),
      )
      .mockResolvedValueOnce({ data: userPage })
      .mockResolvedValueOnce({ data: page })
      .mockResolvedValueOnce(response(entity))
      .mockResolvedValueOnce({
        data: new Blob(['pdf']),
        headers: { 'content-disposition': "attachment; filename*=UTF-8''business%20license.pdf" },
      })
      .mockResolvedValueOnce({ data: page });
    mockedClient.put
      .mockResolvedValueOnce(response(adminUserDetail))
      .mockResolvedValue(response(entity));

    await expect(fetchDashboardStats()).resolves.toEqual({
      totalUsers: 10,
      totalTracks: 20,
      totalSubscribers: 3,
      recentUsers: [],
    });
    await expect(
      fetchUsers({ page: 2, size: 25, keyword: 'user', userType: 'BUSINESS' }, controller.signal),
    ).resolves.toEqual(userPage);
    expect(mockedClient.get).toHaveBeenNthCalledWith(2, '/users', {
      params: { page: 2, size: 25, keyword: 'user', userType: 'BUSINESS' },
      signal: controller.signal,
    });
    await expect(
      updateUserAdmin(4, {
        role: 'ADMIN',
        isVerified: true,
        reason: 'Approved access change',
      }),
    ).resolves.toEqual(adminUserDetail);
    expect(mockedClient.put).toHaveBeenNthCalledWith(1, '/users/4', {
      role: 'ADMIN',
      isVerified: true,
      reason: 'Approved access change',
    });
    expectTypeOf<AdminAssignableRole>().toEqualTypeOf<'USER' | 'ADMIN'>();
    expectTypeOf<NonNullable<Parameters<typeof updateUserAdmin>[1]['role']>>().toEqualTypeOf<
      'USER' | 'ADMIN'
    >();

    await fetchCompanyCerts({ page: 3, size: 10, status: 'PENDING' });
    await fetchCompanyCert(9);
    await processCompanyCert(9, { status: 'APPROVED', adminNote: 'Verified' });
    const document = await downloadCompanyCertDocument(9, 2);
    expect(document.fileName).toBe('business license.pdf');
    expect(mockedClient.get).toHaveBeenNthCalledWith(5, '/company-certifications/9/documents/2', {
      responseType: 'blob',
    });

    await fetchAdminWhitelistChannels({
      page: 2,
      size: 50,
      status: 'PENDING',
      keyword: '@atm',
    });
    await updateAdminWhitelistChannelStatus(3, {
      status: 'REGISTERED',
      adminNote: 'Registered',
    });
    expect(mockedClient.put).toHaveBeenLastCalledWith('/admin/whitelist-channels/3/status', {
      status: 'REGISTERED',
      adminNote: 'Registered',
    });
  });

  it('uses payment read and reconciliation incident contracts', async () => {
    const controller = new AbortController();
    mockedClient.get.mockResolvedValue({ data: page });
    mockedClient.put.mockResolvedValue(response(entity));

    await fetchAdminPaymentOrders(2, 30, controller.signal);
    await fetchAdminPaymentReconciliationIncidents(3, 25, 'OPEN', controller.signal);
    expect(mockedClient.get).toHaveBeenNthCalledWith(
      2,
      '/admin/payments/reconciliation-incidents',
      { params: { page: 3, size: 25, status: 'OPEN' }, signal: controller.signal },
    );
    await fetchAdminPaymentReconciliationIncidents(1, 20, undefined, controller.signal);
    expect(mockedClient.get).toHaveBeenNthCalledWith(
      3,
      '/admin/payments/reconciliation-incidents',
      { params: { page: 1, size: 20 }, signal: controller.signal },
    );
    await updateAdminPaymentReconciliationIncidentStatus(5, {
      status: 'RESOLVED',
      note: 'Matched',
    });
    expect(mockedClient.put).toHaveBeenCalledWith(
      '/admin/payments/reconciliation-incidents/5/status',
      { status: 'RESOLVED', note: 'Matched' },
    );
    await fetchAdminBillingAgreements(2, 20, controller.signal);
    await fetchAdminSubscriptionPayments(2, 20, controller.signal);
    await fetchAdminPaymentReceipts(2, 20, controller.signal);
    await fetchAdminPaymentOperationAuditLogs(2, 20, controller.signal);
    expect(mockedClient.get).toHaveBeenCalledTimes(7);
  });

  it('uses settlement import, reconciliation, filtering, and ignore contracts', async () => {
    const controller = new AbortController();
    mockedClient.get.mockResolvedValue({ data: page });
    mockedClient.post.mockResolvedValue(response(entity));
    mockedClient.put.mockResolvedValue(response(entity));

    await fetchAdminPaymentSettlements(
      2,
      50,
      {
        status: 'MISMATCHED',
        source: 'CSV_MANUAL',
        baseDateFrom: '2026-07-01',
        baseDateTo: '2026-07-17',
      },
      controller.signal,
    );
    expect(mockedClient.get).toHaveBeenCalledWith('/admin/payments/settlements', {
      params: {
        page: 2,
        size: 50,
        status: 'MISMATCHED',
        source: 'CSV_MANUAL',
        baseDateFrom: '2026-07-01',
        baseDateTo: '2026-07-17',
      },
      signal: controller.signal,
    });

    const file = new File(['orderId,amount'], 'settlements.csv');
    await importAdminPaymentSettlements(file, 'July settlement');
    const form = mockedClient.post.mock.calls[0]?.[1] as FormData;
    expect(form.get('file')).toBe(file);
    expect(mockedClient.post).toHaveBeenNthCalledWith(
      1,
      '/admin/payments/settlements/import',
      form,
      { params: { note: 'July settlement' } },
    );
    await importAdminPaymentSettlements(file);
    expect(mockedClient.post).toHaveBeenNthCalledWith(
      2,
      '/admin/payments/settlements/import',
      expect.any(FormData),
      { params: undefined },
    );
    await reconcileAdminPaymentSettlements({
      baseDateFrom: '2026-07-01',
      baseDateTo: '2026-07-17',
    });
    await ignoreAdminPaymentSettlement(42, 'Provider correction');
    expect(mockedClient.put).toHaveBeenCalledWith('/admin/payments/settlements/42/ignore', {
      note: 'Provider correction',
    });
  });

  it('uses the refund preview, request, approval, and execution contracts', async () => {
    const controller = new AbortController();
    mockedClient.get
      .mockResolvedValueOnce(response({ subscriptionPaymentId: 8, refundableAmount: 9900 }))
      .mockResolvedValueOnce({ data: page });
    mockedClient.post.mockResolvedValue(response(entity));

    await fetchAdminPaymentRefundPreview(8);
    expect(mockedClient.get).toHaveBeenNthCalledWith(1, '/admin/payments/refund-preview/8');
    await fetchAdminPaymentRefunds(2, 20, controller.signal);
    const request = {
      subscriptionPaymentId: 8,
      amount: 9900,
      reasonCode: 'CUSTOMER_REQUEST' as const,
      reasonNote: 'Customer confirmed',
    };
    await createAdminPaymentRefund(request);
    await approveAdminPaymentRefund(12, 'Reviewed');
    await executeAdminPaymentRefund(12, 'Provider accepted');
    expect(mockedClient.post).toHaveBeenNthCalledWith(1, '/admin/payments/refunds', request);
    expect(mockedClient.post).toHaveBeenNthCalledWith(2, '/admin/payments/refunds/12/approve', {
      note: 'Reviewed',
    });
    expect(mockedClient.post).toHaveBeenNthCalledWith(3, '/admin/payments/refunds/12/execute', {
      note: 'Provider accepted',
    });
  });

  it('uses entitlement correction preview and guarded mutation contracts', async () => {
    const controller = new AbortController();
    mockedClient.get.mockResolvedValue({ data: page });
    mockedClient.post.mockResolvedValue(response(entity));
    const request = {
      paymentRefundId: 12,
      targetSubscriptionId: 3,
      targetBillingCycle: 'MONTHLY' as const,
      targetStatus: 'CANCELLED' as const,
      targetExpiresAt: '2026-08-01T00:00:00+09:00',
      clearPendingChange: true,
      cancelBillingAgreement: true,
      reasonNote: 'Full refund',
    };

    await previewAdminPaymentEntitlementCorrection(request);
    await fetchAdminPaymentEntitlementCorrections(2, 20, controller.signal);
    await createAdminPaymentEntitlementCorrection(request);
    await approveAdminPaymentEntitlementCorrection(14, 'Reviewed');
    await executeAdminPaymentEntitlementCorrection(14, 'Applied');
    expect(mockedClient.post).toHaveBeenNthCalledWith(
      1,
      '/admin/payments/entitlement-correction-preview',
      request,
    );
    expect(mockedClient.post).toHaveBeenNthCalledWith(
      2,
      '/admin/payments/entitlement-corrections',
      request,
    );
    expect(mockedClient.post).toHaveBeenNthCalledWith(
      4,
      '/admin/payments/entitlement-corrections/14/execute',
      { note: 'Applied' },
    );
  });
});

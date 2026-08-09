import client from '@/api/client';
import type {
  ApiResponse,
  PagedResponse,
  CompanyCertification,
  CompanyCertificationSummary,
  CertificationStatus,
  WhitelistChannelStatus,
} from '@/types';

/* ── Dashboard Stats ── */

export interface DashboardStats {
  totalUsers: number;
  totalTracks: number;
  totalSubscribers: number;
  recentUsers: AdminUserListItem[];
}

export async function fetchDashboardStats(): Promise<DashboardStats> {
  const { data } = await client.get<ApiResponse<DashboardStats>>('/admin/stats');
  return data.data;
}

/* ── User Management ── */

export type AdminAssignableRole = 'USER' | 'ADMIN';
export type AdminUserType = 'INDIVIDUAL' | 'BUSINESS';
export type AdminUserJob = 'EDITOR' | 'ARTIST' | 'FREELANCER';

export interface AdminUserListItem {
  id: number;
  nickname: string;
  email: string;
  userType: AdminUserType;
  role: AdminAssignableRole;
  isVerified: boolean;
  createdAt: string;
}

export interface AdminUserDetail extends AdminUserListItem {
  phonePersonal: string | null;
  phoneCompany: string | null;
  job: AdminUserJob | null;
  companyName: string | null;
}

interface UserListParams {
  page?: number;
  size?: number;
  keyword?: string;
  userType?: AdminUserType;
}

export async function fetchUsers(
  params: UserListParams = {},
  signal?: AbortSignal,
): Promise<PagedResponse<AdminUserListItem>> {
  const { data } = await client.get<PagedResponse<AdminUserListItem>>('/users', {
    params,
    signal,
  });
  return data;
}

export interface UpdateUserAdminRequest {
  role?: AdminAssignableRole;
  isVerified?: boolean;
  reason?: string;
}

export async function updateUserAdmin(
  userId: number,
  body: UpdateUserAdminRequest,
): Promise<AdminUserDetail> {
  const { data } = await client.put<ApiResponse<AdminUserDetail>>(`/users/${userId}`, body);
  return data.data;
}

/* ── Company Certification ── */

interface CertListParams {
  page?: number;
  size?: number;
  status?: CertificationStatus;
}

export async function fetchCompanyCerts(
  params: CertListParams = {},
): Promise<PagedResponse<CompanyCertificationSummary>> {
  const { data } = await client.get<PagedResponse<CompanyCertificationSummary>>(
    '/company-certifications',
    { params },
  );
  return data;
}

export async function fetchCompanyCert(certId: number): Promise<CompanyCertification> {
  const { data } = await client.get<ApiResponse<CompanyCertification>>(
    `/company-certifications/${certId}`,
  );
  return data.data;
}

interface ProcessCertRequest {
  status: CertificationStatus;
  adminNote?: string;
}

export interface ProcessCertResponse {
  id: number;
  status: CertificationStatus;
  certificationCode: string | null;
  approvedAt: string | null;
}

export async function processCompanyCert(
  certId: number,
  body: ProcessCertRequest,
): Promise<ProcessCertResponse> {
  const { data } = await client.put<ApiResponse<ProcessCertResponse>>(
    `/company-certifications/${certId}`,
    body,
  );
  return data.data;
}

export async function downloadCompanyCertDocument(
  certId: number,
  documentId: number,
): Promise<{ blob: Blob; fileName: string }> {
  const response = await client.get<Blob>(
    `/company-certifications/${certId}/documents/${documentId}`,
    { responseType: 'blob' },
  );
  const disposition = response.headers['content-disposition'];
  const fileNameMatch =
    typeof disposition === 'string'
      ? disposition.match(/filename\*=UTF-8''([^;]+)|filename="?([^"]+)"?/)
      : null;
  const encodedName = fileNameMatch?.[1] ?? fileNameMatch?.[2];
  const fileName = encodedName
    ? decodeURIComponent(encodedName)
    : `company-certification-${documentId}`;
  return { blob: response.data, fileName };
}

/* ── Whitelist Channel Operations ── */

export interface AdminWhitelistChannel {
  id: number;
  userId: number;
  userEmail: string;
  userNickname: string;
  channelUrl: string;
  channelName: string;
  youtubeHandle: string | null;
  youtubeChannelId: string | null;
  status: WhitelistChannelStatus;
  primary: boolean;
  adminNote: string | null;
  processedByEmail: string | null;
  planName: string | null;
  billingCycle: 'MONTHLY' | 'YEARLY' | null;
  requestedAt: string | null;
  exportedAt: string | null;
  processedAt: string | null;
  removalRequestedAt: string | null;
  createdAt: string;
}

interface AdminWhitelistChannelListParams {
  page?: number;
  size?: number;
  status?: WhitelistChannelStatus;
  keyword?: string;
}

export async function fetchAdminWhitelistChannels(
  params: AdminWhitelistChannelListParams = {},
): Promise<PagedResponse<AdminWhitelistChannel>> {
  const { data } = await client.get<PagedResponse<AdminWhitelistChannel>>(
    '/admin/whitelist-channels',
    { params },
  );
  return data;
}

export async function updateAdminWhitelistChannelStatus(
  channelId: number,
  body: { status: WhitelistChannelStatus; adminNote?: string },
): Promise<AdminWhitelistChannel> {
  const { data } = await client.put<ApiResponse<AdminWhitelistChannel>>(
    `/admin/whitelist-channels/${channelId}/status`,
    body,
  );
  return data.data;
}

export interface AdminWhitelistExportRequest {
  status?: WhitelistChannelStatus;
  keyword?: string;
  note?: string;
}

export async function exportAdminWhitelistChannels(
  request: AdminWhitelistExportRequest,
): Promise<{ batchId: number; blob: Blob; fileName: string }> {
  const response = await client.post<Blob>('/admin/whitelist-channels/export', request, {
    responseType: 'blob',
  });
  return whitelistExportResponse(response);
}

export async function downloadAdminWhitelistExportBatch(
  batchId: number,
): Promise<{ batchId: number; blob: Blob; fileName: string }> {
  const response = await client.get<Blob>(`/admin/whitelist-channels/exports/${batchId}`, {
    responseType: 'blob',
  });
  return whitelistExportResponse(response, batchId);
}

function whitelistExportResponse(
  response: { data: Blob; headers: Record<string, unknown> },
  expectedBatchId?: number,
): {
  batchId: number;
  blob: Blob;
  fileName: string;
} {
  const disposition = response.headers['content-disposition'];
  const fileNameMatch =
    typeof disposition === 'string'
      ? disposition.match(/filename\*=UTF-8''([^;]+)|filename="?([^"]+)"?/)
      : null;
  const encodedName = fileNameMatch?.[1] ?? fileNameMatch?.[2];
  const fileName = encodedName ? decodeURIComponent(encodedName) : 'whitelist-channels.csv';
  const responseBatchId = parseWhitelistExportBatchId(
    response.headers['x-whitelist-export-batch-id'],
  );
  const batchId = responseBatchId ?? expectedBatchId;
  if (
    batchId === undefined ||
    !Number.isSafeInteger(batchId) ||
    batchId <= 0 ||
    (responseBatchId !== null &&
      expectedBatchId !== undefined &&
      responseBatchId !== expectedBatchId)
  ) {
    throw new Error('Whitelist export response has an invalid batch ID');
  }
  return { batchId, blob: response.data, fileName };
}

function parseWhitelistExportBatchId(value: unknown): number | null {
  if (typeof value !== 'string' || !/^\d+$/.test(value)) return null;
  const batchId = Number(value);
  return Number.isSafeInteger(batchId) && batchId > 0 ? batchId : null;
}

/* ── Payment Operations ── */

export interface AdminPaymentOrder {
  id: number;
  orderId: string;
  userId: number;
  userNickname: string;
  purpose: string;
  provider: string;
  status: string;
  subscriptionName: string;
  billingCycle: string;
  amount: number;
  currency: string;
  failureCode: string | null;
  failureMessage: string | null;
  expiresAt: string;
  confirmedAt: string | null;
  createdAt: string;
}

export interface AdminBillingAgreement {
  id: number;
  userId: number;
  userNickname: string;
  provider: string;
  status: string;
  payMethod: string | null;
  maskedMethod: string | null;
  nextBillingAt: string | null;
  lastChargedAt: string | null;
  failureCount: number;
  cancelledAt: string | null;
  createdAt: string;
}

export interface AdminSubscriptionPayment {
  id: number;
  userId: number;
  userNickname: string;
  orderId: string | null;
  subscriptionName: string;
  billingCycle: string;
  provider: string | null;
  amount: number;
  paymentStatus: string;
  providerReference: string | null;
  createdAt: string;
}

export interface AdminPaymentReceipt {
  id: number;
  userId: number;
  userNickname: string;
  paymentOrderId: number;
  orderId: string;
  subscriptionPaymentId: number;
  provider: string;
  type: string;
  status: string;
  providerReference: string | null;
  receiptReference: string | null;
  receiptUrl: string | null;
  issuedAt: string | null;
  cancelledAt: string | null;
  createdAt: string;
}

export interface AdminPaymentOperationAuditLog {
  id: number;
  action: string;
  targetType: string;
  targetId: number | null;
  actorUserId: number | null;
  actorEmail: string | null;
  targetUserId: number | null;
  targetUserNickname: string | null;
  paymentOrderId: number | null;
  orderId: string | null;
  subscriptionPaymentId: number | null;
  reconciliationIncidentId: number | null;
  provider: string | null;
  providerReference: string | null;
  beforeStatus: string | null;
  afterStatus: string | null;
  reasonCode: string | null;
  note: string | null;
  createdAt: string;
}

export type AdminPaymentSettlementSource = 'CSV_MANUAL' | 'TOSS_API' | 'SYSTEM_RECONCILIATION';
export type AdminPaymentSettlementStatus =
  | 'IMPORTED'
  | 'MATCHED'
  | 'MISMATCHED'
  | 'LOCAL_PAYMENT_NOT_FOUND'
  | 'PROVIDER_SETTLEMENT_NOT_FOUND'
  | 'IGNORED';

export interface AdminPaymentSettlement {
  id: number;
  source: AdminPaymentSettlementSource;
  provider: string;
  status: AdminPaymentSettlementStatus;
  orderId: string;
  providerReference: string | null;
  providerSettlementReference: string | null;
  paymentOrderId: number | null;
  subscriptionPaymentId: number | null;
  userId: number | null;
  userNickname: string | null;
  grossAmount: number;
  refundAmount: number;
  feeAmount: number;
  vatAmount: number;
  netSettlementAmount: number;
  currency: string;
  settlementBaseDate: string;
  settlementPayoutDate: string | null;
  providerStatus: string | null;
  mismatchReason: string | null;
  sourceFileName: string | null;
  sourceRowNumber: number | null;
  operatorNote: string | null;
  ignoredBy: number | null;
  ignoredAt: string | null;
  reconciledAt: string | null;
  createdAt: string;
}

export interface AdminPaymentSettlementImportError {
  rowNumber: number;
  message: string;
}

export interface AdminPaymentSettlementImportResult {
  importBatchKey: string;
  totalRows: number;
  importedRows: number;
  skippedDuplicateRows: number;
  failedRows: number;
  statusCounts: Record<string, number>;
  errors: AdminPaymentSettlementImportError[];
}

export type AdminPaymentRefundReasonCode =
  | 'CUSTOMER_REQUEST'
  | 'DUPLICATE_PAYMENT'
  | 'PAYMENT_ERROR'
  | 'SERVICE_ISSUE'
  | 'ADMIN_ADJUSTMENT'
  | 'OTHER';

export interface AdminPaymentRefundPreview {
  subscriptionPaymentId: number;
  paymentOrderId: number | null;
  orderId: string | null;
  userId: number;
  userNickname: string;
  provider: string | null;
  originalAmount: number;
  alreadyRefundedOrReservedAmount: number;
  refundableAmount: number;
  providerReference: string | null;
  refundable: boolean;
  reason: string | null;
}

export interface AdminPaymentRefund {
  id: number;
  subscriptionPaymentId: number;
  paymentOrderId: number;
  orderId: string;
  userId: number;
  userNickname: string;
  provider: string;
  status: string;
  amount: number;
  currency: string;
  reasonCode: AdminPaymentRefundReasonCode;
  reasonNote: string | null;
  idempotencyKey: string;
  providerReference: string;
  providerRefundReference: string | null;
  failureCode: string | null;
  failureMessage: string | null;
  requestedById: number | null;
  requestedByEmail: string | null;
  approvedById: number | null;
  approvedByEmail: string | null;
  executedById: number | null;
  executedByEmail: string | null;
  approvedAt: string | null;
  executedAt: string | null;
  createdAt: string;
  updatedAt: string;
}

export type AdminPaymentEntitlementCorrectionBillingCycle = 'MONTHLY' | 'YEARLY';
export type AdminPaymentEntitlementCorrectionSubscriptionStatus =
  | 'ACTIVE'
  | 'CANCELLED'
  | 'EXPIRED';

export interface AdminPaymentEntitlementCorrectionPreview {
  paymentRefundId: number;
  refundStatus: string;
  userId: number;
  userNickname: string;
  userSubscriptionId: number;
  currentSubscriptionId: number;
  currentPlanName: string;
  currentBillingCycle: AdminPaymentEntitlementCorrectionBillingCycle;
  currentStatus: AdminPaymentEntitlementCorrectionSubscriptionStatus;
  currentExpiresAt: string;
  currentPendingSubscriptionId: number | null;
  currentPendingPlanName: string | null;
  currentPendingBillingCycle: AdminPaymentEntitlementCorrectionBillingCycle | null;
  targetSubscriptionId: number;
  targetPlanName: string;
  targetBillingCycle: AdminPaymentEntitlementCorrectionBillingCycle;
  targetStatus: AdminPaymentEntitlementCorrectionSubscriptionStatus;
  targetExpiresAt: string;
  clearPendingChange: boolean;
  cancelBillingAgreement: boolean;
  currentBillingAgreementStatus: string | null;
  targetBillingAgreementStatus: string | null;
  executable: boolean;
  reason: string | null;
}

export interface AdminPaymentEntitlementCorrection {
  id: number;
  paymentRefundId: number;
  subscriptionPaymentId: number;
  paymentOrderId: number;
  orderId: string;
  userSubscriptionId: number;
  userId: number;
  userNickname: string;
  provider: string;
  status: string;
  action: string;
  beforeSubscriptionId: number;
  beforePlanName: string;
  beforeBillingCycle: AdminPaymentEntitlementCorrectionBillingCycle;
  beforeStatus: AdminPaymentEntitlementCorrectionSubscriptionStatus;
  beforeExpiresAt: string;
  beforePendingSubscriptionId: number | null;
  beforePendingPlanName: string | null;
  beforePendingBillingCycle: AdminPaymentEntitlementCorrectionBillingCycle | null;
  targetSubscriptionId: number;
  targetPlanName: string;
  targetBillingCycle: AdminPaymentEntitlementCorrectionBillingCycle;
  targetStatus: AdminPaymentEntitlementCorrectionSubscriptionStatus;
  targetExpiresAt: string;
  clearPendingChange: boolean;
  cancelBillingAgreement: boolean;
  beforeBillingAgreementStatus: string | null;
  afterBillingAgreementStatus: string | null;
  reasonNote: string | null;
  failureCode: string | null;
  failureMessage: string | null;
  requestedById: number | null;
  requestedByEmail: string | null;
  approvedById: number | null;
  approvedByEmail: string | null;
  executedById: number | null;
  executedByEmail: string | null;
  approvedAt: string | null;
  executedAt: string | null;
  createdAt: string;
  updatedAt: string;
}

export type AdminPaymentReconciliationIncidentStatus =
  | 'OPEN'
  | 'ACKNOWLEDGED'
  | 'RESOLVED'
  | 'IGNORED';

export type AdminPaymentReconciliationIncidentSeverity = 'WARNING' | 'CRITICAL';

export interface AdminPaymentReconciliationIncident {
  id: number;
  dedupeKey: string;
  issueType: string;
  status: AdminPaymentReconciliationIncidentStatus;
  severity: AdminPaymentReconciliationIncidentSeverity;
  paymentOrderId: number | null;
  billingAgreementId: number | null;
  userId: number | null;
  userNickname: string | null;
  orderId: string | null;
  provider: string | null;
  purpose: string | null;
  localStatus: string | null;
  providerStatus: string | null;
  localAmount: number | null;
  providerAmount: number | null;
  providerReference: string | null;
  failureCode: string | null;
  failureMessage: string | null;
  occurrenceCount: number;
  firstDetectedAt: string;
  lastDetectedAt: string;
  notifiedAt: string | null;
  resolvedAt: string | null;
  resolutionNote: string | null;
  createdAt: string;
}

export async function fetchAdminPaymentOrders(
  page = 1,
  size = 20,
  signal?: AbortSignal,
): Promise<PagedResponse<AdminPaymentOrder>> {
  const { data } = await client.get<PagedResponse<AdminPaymentOrder>>('/admin/payments/orders', {
    params: { page, size },
    signal,
  });
  return data;
}

export async function fetchAdminPaymentReconciliationIncidents(
  page = 1,
  size = 20,
  status?: AdminPaymentReconciliationIncidentStatus,
  signal?: AbortSignal,
): Promise<PagedResponse<AdminPaymentReconciliationIncident>> {
  const params: { page: number; size: number; status?: AdminPaymentReconciliationIncidentStatus } =
    { page, size };
  if (status) {
    params.status = status;
  }

  const { data } = await client.get<PagedResponse<AdminPaymentReconciliationIncident>>(
    '/admin/payments/reconciliation-incidents',
    { params, signal },
  );
  return data;
}

interface UpdatePaymentReconciliationIncidentRequest {
  status: AdminPaymentReconciliationIncidentStatus;
  note?: string;
}

export async function updateAdminPaymentReconciliationIncidentStatus(
  incidentId: number,
  body: UpdatePaymentReconciliationIncidentRequest,
): Promise<AdminPaymentReconciliationIncident> {
  const { data } = await client.put<ApiResponse<AdminPaymentReconciliationIncident>>(
    `/admin/payments/reconciliation-incidents/${incidentId}/status`,
    body,
  );
  return data.data;
}

export async function fetchAdminBillingAgreements(
  page = 1,
  size = 20,
  signal?: AbortSignal,
): Promise<PagedResponse<AdminBillingAgreement>> {
  const { data } = await client.get<PagedResponse<AdminBillingAgreement>>(
    '/admin/payments/billing-agreements',
    { params: { page, size }, signal },
  );
  return data;
}

export async function fetchAdminSubscriptionPayments(
  page = 1,
  size = 20,
  signal?: AbortSignal,
): Promise<PagedResponse<AdminSubscriptionPayment>> {
  const { data } = await client.get<PagedResponse<AdminSubscriptionPayment>>(
    '/admin/payments/subscription-payments',
    { params: { page, size }, signal },
  );
  return data;
}

export async function fetchAdminPaymentReceipts(
  page = 1,
  size = 20,
  signal?: AbortSignal,
): Promise<PagedResponse<AdminPaymentReceipt>> {
  const { data } = await client.get<PagedResponse<AdminPaymentReceipt>>(
    '/admin/payments/receipts',
    { params: { page, size }, signal },
  );
  return data;
}

export async function fetchAdminPaymentOperationAuditLogs(
  page = 1,
  size = 20,
  signal?: AbortSignal,
): Promise<PagedResponse<AdminPaymentOperationAuditLog>> {
  const { data } = await client.get<PagedResponse<AdminPaymentOperationAuditLog>>(
    '/admin/payments/operation-audit-logs',
    { params: { page, size }, signal },
  );
  return data;
}

interface AdminPaymentSettlementListParams {
  status?: AdminPaymentSettlementStatus;
  source?: AdminPaymentSettlementSource;
  baseDateFrom?: string;
  baseDateTo?: string;
}

export async function fetchAdminPaymentSettlements(
  page = 1,
  size = 20,
  filters: AdminPaymentSettlementListParams = {},
  signal?: AbortSignal,
): Promise<PagedResponse<AdminPaymentSettlement>> {
  const { data } = await client.get<PagedResponse<AdminPaymentSettlement>>(
    '/admin/payments/settlements',
    { params: { page, size, ...filters }, signal },
  );
  return data;
}

export async function importAdminPaymentSettlements(
  file: File,
  note?: string,
): Promise<AdminPaymentSettlementImportResult> {
  const formData = new FormData();
  formData.append('file', file);
  const { data } = await client.post<ApiResponse<AdminPaymentSettlementImportResult>>(
    '/admin/payments/settlements/import',
    formData,
    { params: note ? { note } : undefined },
  );
  return data.data;
}

export async function reconcileAdminPaymentSettlements(body: {
  baseDateFrom?: string;
  baseDateTo?: string;
}): Promise<AdminPaymentSettlementImportResult> {
  const { data } = await client.post<ApiResponse<AdminPaymentSettlementImportResult>>(
    '/admin/payments/settlements/reconcile',
    body,
  );
  return data.data;
}

export async function ignoreAdminPaymentSettlement(
  settlementId: number,
  note?: string,
): Promise<AdminPaymentSettlement> {
  const { data } = await client.put<ApiResponse<AdminPaymentSettlement>>(
    `/admin/payments/settlements/${settlementId}/ignore`,
    { note },
  );
  return data.data;
}

export async function fetchAdminPaymentRefundPreview(
  subscriptionPaymentId: number,
): Promise<AdminPaymentRefundPreview> {
  const { data } = await client.get<ApiResponse<AdminPaymentRefundPreview>>(
    `/admin/payments/refund-preview/${subscriptionPaymentId}`,
  );
  return data.data;
}

interface CreateAdminPaymentRefundRequest {
  subscriptionPaymentId: number;
  amount: number;
  reasonCode: AdminPaymentRefundReasonCode;
  reasonNote?: string;
}

export async function fetchAdminPaymentRefunds(
  page = 1,
  size = 20,
  signal?: AbortSignal,
): Promise<PagedResponse<AdminPaymentRefund>> {
  const { data } = await client.get<PagedResponse<AdminPaymentRefund>>('/admin/payments/refunds', {
    params: { page, size },
    signal,
  });
  return data;
}

export async function createAdminPaymentRefund(
  body: CreateAdminPaymentRefundRequest,
): Promise<AdminPaymentRefund> {
  const { data } = await client.post<ApiResponse<AdminPaymentRefund>>(
    '/admin/payments/refunds',
    body,
  );
  return data.data;
}

export async function approveAdminPaymentRefund(
  refundId: number,
  note?: string,
): Promise<AdminPaymentRefund> {
  const { data } = await client.post<ApiResponse<AdminPaymentRefund>>(
    `/admin/payments/refunds/${refundId}/approve`,
    { note },
  );
  return data.data;
}

export async function executeAdminPaymentRefund(
  refundId: number,
  note?: string,
): Promise<AdminPaymentRefund> {
  const { data } = await client.post<ApiResponse<AdminPaymentRefund>>(
    `/admin/payments/refunds/${refundId}/execute`,
    { note },
  );
  return data.data;
}

interface AdminPaymentEntitlementCorrectionRequest {
  paymentRefundId: number;
  targetSubscriptionId: number;
  targetBillingCycle: AdminPaymentEntitlementCorrectionBillingCycle;
  targetStatus: AdminPaymentEntitlementCorrectionSubscriptionStatus;
  targetExpiresAt: string;
  clearPendingChange: boolean;
  cancelBillingAgreement: boolean;
  reasonNote?: string;
}

export async function previewAdminPaymentEntitlementCorrection(
  body: AdminPaymentEntitlementCorrectionRequest,
): Promise<AdminPaymentEntitlementCorrectionPreview> {
  const { data } = await client.post<ApiResponse<AdminPaymentEntitlementCorrectionPreview>>(
    '/admin/payments/entitlement-correction-preview',
    body,
  );
  return data.data;
}

export async function fetchAdminPaymentEntitlementCorrections(
  page = 1,
  size = 20,
  signal?: AbortSignal,
): Promise<PagedResponse<AdminPaymentEntitlementCorrection>> {
  const { data } = await client.get<PagedResponse<AdminPaymentEntitlementCorrection>>(
    '/admin/payments/entitlement-corrections',
    { params: { page, size }, signal },
  );
  return data;
}

export async function createAdminPaymentEntitlementCorrection(
  body: AdminPaymentEntitlementCorrectionRequest,
): Promise<AdminPaymentEntitlementCorrection> {
  const { data } = await client.post<ApiResponse<AdminPaymentEntitlementCorrection>>(
    '/admin/payments/entitlement-corrections',
    body,
  );
  return data.data;
}

export async function approveAdminPaymentEntitlementCorrection(
  correctionId: number,
  note?: string,
): Promise<AdminPaymentEntitlementCorrection> {
  const { data } = await client.post<ApiResponse<AdminPaymentEntitlementCorrection>>(
    `/admin/payments/entitlement-corrections/${correctionId}/approve`,
    { note },
  );
  return data.data;
}

export async function executeAdminPaymentEntitlementCorrection(
  correctionId: number,
  note?: string,
): Promise<AdminPaymentEntitlementCorrection> {
  const { data } = await client.post<ApiResponse<AdminPaymentEntitlementCorrection>>(
    `/admin/payments/entitlement-corrections/${correctionId}/execute`,
    { note },
  );
  return data.data;
}

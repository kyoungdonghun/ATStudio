import client from '@/api/client';
import { getApiErrorCode } from '@/api/loadError';
import type { ApiResponse, PagedResponse } from '@/types';
import type { SubscriptionPlan } from '@/api/subscriptions';

/* ── Response types ── */

export type SubscriptionChangeType = 'UPGRADE' | 'DOWNGRADE' | 'SCHEDULED_CHANGE' | 'NO_CHANGE';
export type SubscriptionBillingCycle = 'MONTHLY' | 'YEARLY';
export type UserSubscriptionStatus = 'ACTIVE' | 'CANCELLED' | 'EXPIRED';
export type BillingAgreementStatus = 'READY' | 'ACTIVE' | 'SUSPENDED' | 'CANCELLED' | 'EXPIRED';
export type AdminSubscriptionCorrectionStatus =
  | 'REQUESTED'
  | 'APPROVED'
  | 'PROCESSING'
  | 'SUCCEEDED'
  | 'FAILED'
  | 'CANCELLED';
export type AdminSubscriptionCorrectionAction = 'SET_SUBSCRIPTION_STATE';

export interface MySubscription {
  id: number;
  userId?: number;
  userNickname?: string;
  subscription: SubscriptionPlan;
  billingCycle: SubscriptionBillingCycle;
  status: UserSubscriptionStatus;
  startedAt: string;
  expiresAt: string;
  pendingSubscriptionId: number | null;
  pendingBillingCycle: SubscriptionBillingCycle | null;
}

export interface SubscriptionChangeResponse {
  subscription: Pick<SubscriptionPlan, 'id' | 'name'>;
  billingCycle: SubscriptionBillingCycle;
  status: string;
  changeType: SubscriptionChangeType;
  proratedAmount: number;
  startedAt: string;
  expiresAt: string;
}

export interface SubscriptionChangePreview {
  changeType: SubscriptionChangeType;
  proratedAmount: number;
  effectiveDate: string;
  nextBillingDate: string;
  nextBillingAmount: number;
  newPlanName: string;
  newBillingCycle: SubscriptionBillingCycle;
}

export interface ChangeSubscriptionRequest {
  subscriptionId: number;
  billingCycle: SubscriptionBillingCycle;
}

export interface AdminSubscriptionCorrectionRequest {
  userSubscriptionId: number;
  targetSubscriptionId: number;
  targetBillingCycle: SubscriptionBillingCycle;
  targetStatus: UserSubscriptionStatus;
  targetExpiresAt: string;
  clearPendingChange: boolean;
  cancelBillingAgreement: boolean;
  reasonNote: string;
}

export interface AdminSubscriptionCorrectionPreview {
  userSubscriptionId: number;
  userId: number;
  userNickname: string;
  currentSubscriptionId: number;
  currentPlanName: string;
  currentBillingCycle: SubscriptionBillingCycle;
  currentStatus: UserSubscriptionStatus;
  currentExpiresAt: string;
  currentPendingSubscriptionId: number | null;
  currentPendingPlanName: string | null;
  currentPendingBillingCycle: SubscriptionBillingCycle | null;
  targetSubscriptionId: number;
  targetPlanName: string;
  targetBillingCycle: SubscriptionBillingCycle;
  targetStatus: UserSubscriptionStatus;
  targetExpiresAt: string;
  clearPendingChange: boolean;
  cancelBillingAgreement: boolean;
  currentBillingAgreementStatus: BillingAgreementStatus | null;
  targetBillingAgreementStatus: BillingAgreementStatus | null;
  externalPaymentExecuted: boolean;
  executable: boolean;
  reason: string | null;
}

export interface AdminSubscriptionCorrection {
  id: number;
  userSubscriptionId: number;
  userId: number;
  userNickname: string;
  billingAgreementId: number | null;
  status: AdminSubscriptionCorrectionStatus;
  action: AdminSubscriptionCorrectionAction;
  beforeSubscriptionId: number;
  beforePlanName: string;
  beforeBillingCycle: SubscriptionBillingCycle;
  beforeStatus: UserSubscriptionStatus;
  beforeExpiresAt: string;
  beforePendingSubscriptionId: number | null;
  beforePendingPlanName: string | null;
  beforePendingBillingCycle: SubscriptionBillingCycle | null;
  targetSubscriptionId: number;
  targetPlanName: string;
  targetBillingCycle: SubscriptionBillingCycle;
  targetStatus: UserSubscriptionStatus;
  targetExpiresAt: string;
  clearPendingChange: boolean;
  cancelBillingAgreement: boolean;
  beforeBillingAgreementStatus: BillingAgreementStatus | null;
  afterBillingAgreementStatus: BillingAgreementStatus | null;
  reasonNote: string;
  failureCode: string | null;
  failureMessage: string | null;
  requestedById: number;
  approvedById: number | null;
  executedById: number | null;
  approvalNote: string | null;
  executionNote: string | null;
  approvedAt: string | null;
  executedAt: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface AdminSubscriptionCorrectionNoteRequest {
  note: string;
}

/* ── API functions ── */

/** GET /api/user-subscriptions/me -- my current subscription */
export async function fetchMySubscription(signal?: AbortSignal): Promise<MySubscription> {
  const { data } = await client.get<ApiResponse<MySubscription>>('/user-subscriptions/me', {
    signal,
  });
  return data.data;
}

export function isNoActiveSubscriptionError(error: unknown): boolean {
  const status = (error as { response?: { status?: number } })?.response?.status;
  return status === 403 && getApiErrorCode(error) === 'NO_ACTIVE_SUBSCRIPTION';
}

/** PUT /api/user-subscriptions/me -- change (upgrade/downgrade) subscription */
export async function changeMySubscription(
  req: ChangeSubscriptionRequest,
): Promise<SubscriptionChangeResponse> {
  const { data } = await client.put<ApiResponse<SubscriptionChangeResponse>>(
    '/user-subscriptions/me',
    req,
  );
  return data.data;
}

/** DELETE /api/user-subscriptions/me -- cancel my subscription */
export async function cancelMySubscription(): Promise<void> {
  await client.delete('/user-subscriptions/me');
}

/** POST /api/user-subscriptions/me/reactivate -- resume a cancelled subscription before expiry */
export async function reactivateMySubscription(): Promise<MySubscription> {
  const { data } = await client.post<ApiResponse<MySubscription>>(
    '/user-subscriptions/me/reactivate',
  );
  return data.data;
}

/* ── Admin API functions ── */

/** GET /api/user-subscriptions -- admin: list all user subscriptions */
export async function fetchAdminUserSubscriptions(
  page = 1,
  size = 20,
  signal?: AbortSignal,
): Promise<PagedResponse<MySubscription>> {
  const { data } = await client.get<PagedResponse<MySubscription>>('/user-subscriptions', {
    params: { page, size },
    signal,
  });
  return data;
}

/** POST /api/admin/user-subscription-corrections/preview -- admin: validate target state */
export async function previewAdminSubscriptionCorrection(
  req: AdminSubscriptionCorrectionRequest,
  signal?: AbortSignal,
): Promise<AdminSubscriptionCorrectionPreview> {
  const { data } = await client.post<ApiResponse<AdminSubscriptionCorrectionPreview>>(
    '/admin/user-subscription-corrections/preview',
    req,
    { signal },
  );
  return data.data;
}

/** GET /api/admin/user-subscription-corrections -- admin: correction history */
export async function fetchAdminSubscriptionCorrections(
  page = 1,
  size = 20,
  signal?: AbortSignal,
): Promise<PagedResponse<AdminSubscriptionCorrection>> {
  const { data } = await client.get<PagedResponse<AdminSubscriptionCorrection>>(
    '/admin/user-subscription-corrections',
    { params: { page, size }, signal },
  );
  return data;
}

/** GET /api/admin/user-subscription-corrections/{id} -- admin: correction detail */
export async function fetchAdminSubscriptionCorrection(
  id: number,
  signal?: AbortSignal,
): Promise<AdminSubscriptionCorrection> {
  const { data } = await client.get<ApiResponse<AdminSubscriptionCorrection>>(
    `/admin/user-subscription-corrections/${id}`,
    { signal },
  );
  return data.data;
}

/** GET /api/admin/user-subscription-corrections/open -- admin: current non-terminal correction */
export async function fetchOpenAdminSubscriptionCorrection(
  userSubscriptionId: number,
  signal?: AbortSignal,
): Promise<AdminSubscriptionCorrection | null> {
  const response = await client.get<ApiResponse<AdminSubscriptionCorrection>>(
    '/admin/user-subscription-corrections/open',
    { params: { userSubscriptionId }, signal },
  );
  if (response.status === 204) return null;
  return response.data.data;
}

/** POST /api/admin/user-subscription-corrections -- admin: create correction request */
export async function createAdminSubscriptionCorrection(
  req: AdminSubscriptionCorrectionRequest,
  signal?: AbortSignal,
): Promise<AdminSubscriptionCorrection> {
  const { data } = await client.post<ApiResponse<AdminSubscriptionCorrection>>(
    '/admin/user-subscription-corrections',
    req,
    { signal },
  );
  return data.data;
}

/** POST /api/admin/user-subscription-corrections/{id}/approve -- admin: approve request */
export async function approveAdminSubscriptionCorrection(
  id: number,
  req: AdminSubscriptionCorrectionNoteRequest,
  signal?: AbortSignal,
): Promise<AdminSubscriptionCorrection> {
  const { data } = await client.post<ApiResponse<AdminSubscriptionCorrection>>(
    `/admin/user-subscription-corrections/${id}/approve`,
    req,
    { signal },
  );
  return data.data;
}

/** POST /api/admin/user-subscription-corrections/{id}/execute -- admin: apply local state */
export async function executeAdminSubscriptionCorrection(
  id: number,
  req: AdminSubscriptionCorrectionNoteRequest,
  signal?: AbortSignal,
): Promise<AdminSubscriptionCorrection> {
  const { data } = await client.post<ApiResponse<AdminSubscriptionCorrection>>(
    `/admin/user-subscription-corrections/${id}/execute`,
    req,
    { signal },
  );
  return data.data;
}

/* ── Utility API functions ── */

/** GET /api/utils/subscription-change-preview -- preview change impact */
export async function fetchSubscriptionChangePreview(
  subscriptionId: number,
  billingCycle: SubscriptionBillingCycle,
): Promise<SubscriptionChangePreview> {
  const { data } = await client.get<ApiResponse<SubscriptionChangePreview>>(
    '/utils/subscription-change-preview',
    { params: { subscriptionId, billingCycle } },
  );
  return data.data;
}

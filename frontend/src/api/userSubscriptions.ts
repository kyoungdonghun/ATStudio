import client from '@/api/client';
import { getApiErrorCode } from '@/api/loadError';
import type { ApiResponse, PagedResponse } from '@/types';
import type { SubscriptionPlan } from '@/api/subscriptions';

/* ── Response types ── */

export type SubscriptionChangeType = 'UPGRADE' | 'DOWNGRADE' | 'SCHEDULED_CHANGE' | 'NO_CHANGE';

export interface MySubscription {
  id: number;
  userId?: number;
  userNickname?: string;
  subscription: SubscriptionPlan;
  billingCycle: 'MONTHLY' | 'YEARLY';
  status: 'ACTIVE' | 'CANCELLED' | 'EXPIRED';
  startedAt: string;
  expiresAt: string;
  pendingSubscriptionId: number | null;
  pendingBillingCycle: 'MONTHLY' | 'YEARLY' | null;
}

export interface SubscriptionChangeResponse {
  subscription: Pick<SubscriptionPlan, 'id' | 'name'>;
  billingCycle: 'MONTHLY' | 'YEARLY';
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
  newBillingCycle: 'MONTHLY' | 'YEARLY';
}

export interface ChangeSubscriptionRequest {
  subscriptionId: number;
  billingCycle: 'MONTHLY' | 'YEARLY';
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

/** GET /api/user-subscriptions/{id} -- admin: subscription detail */
export async function fetchAdminUserSubscriptionDetail(id: number): Promise<MySubscription> {
  const { data } = await client.get<ApiResponse<MySubscription>>(`/user-subscriptions/${id}`);
  return data.data;
}

export interface AdminUpdateSubscriptionRequest {
  status?: string;
  billingCycle?: 'MONTHLY' | 'YEARLY';
  expiresAt?: string;
}

/** PUT /api/user-subscriptions/{id} -- admin: update subscription */
export async function updateAdminUserSubscription(
  id: number,
  req: AdminUpdateSubscriptionRequest,
): Promise<MySubscription> {
  const { data } = await client.put<ApiResponse<MySubscription>>(`/user-subscriptions/${id}`, req);
  return data.data;
}

/** DELETE /api/user-subscriptions/{id} -- admin: cancel subscription */
export async function deleteAdminUserSubscription(id: number): Promise<void> {
  await client.delete(`/user-subscriptions/${id}`);
}

/* ── Utility API functions ── */

/** GET /api/utils/subscription-change-preview -- preview change impact */
export async function fetchSubscriptionChangePreview(
  subscriptionId: number,
  billingCycle: 'MONTHLY' | 'YEARLY',
): Promise<SubscriptionChangePreview> {
  const { data } = await client.get<ApiResponse<SubscriptionChangePreview>>(
    '/utils/subscription-change-preview',
    { params: { subscriptionId, billingCycle } },
  );
  return data.data;
}

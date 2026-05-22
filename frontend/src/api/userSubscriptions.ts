import client from '@/api/client';
import type { ApiResponse, PagedResponse } from '@/types';
import type { SubscriptionPlan } from '@/api/subscriptions';

/* ── Response types ── */

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
  changeType: 'UPGRADE' | 'DOWNGRADE';
  proratedAmount: number;
  startedAt: string;
  expiresAt: string;
}

export interface SubscriptionChangePreview {
  changeType: 'UPGRADE' | 'DOWNGRADE';
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
export async function fetchMySubscription(): Promise<MySubscription> {
  const { data } = await client.get<ApiResponse<MySubscription>>(
    '/user-subscriptions/me',
  );
  return data.data;
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

/* ── Admin API functions ── */

/** GET /api/user-subscriptions -- admin: list all user subscriptions */
export async function fetchAdminUserSubscriptions(
  page = 1,
  size = 20,
): Promise<PagedResponse<MySubscription>> {
  const { data } = await client.get<PagedResponse<MySubscription>>(
    '/user-subscriptions',
    { params: { page, size } },
  );
  return data;
}

/** GET /api/user-subscriptions/{id} -- admin: subscription detail */
export async function fetchAdminUserSubscriptionDetail(
  id: number,
): Promise<MySubscription> {
  const { data } = await client.get<ApiResponse<MySubscription>>(
    `/user-subscriptions/${id}`,
  );
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
  const { data } = await client.put<ApiResponse<MySubscription>>(
    `/user-subscriptions/${id}`,
    req,
  );
  return data.data;
}

/** DELETE /api/user-subscriptions/{id} -- admin: cancel subscription */
export async function deleteAdminUserSubscription(
  id: number,
): Promise<void> {
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

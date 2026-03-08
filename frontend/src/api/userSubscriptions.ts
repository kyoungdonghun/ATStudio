import client from '@/api/client';
import type { SubscriptionPlan } from '@/api/subscriptions';

/* ── Response types ── */

export interface MySubscription {
  id: number;
  subscription: Pick<SubscriptionPlan, 'id' | 'name'>;
  billingCycle: 'MONTHLY' | 'YEARLY';
  status: 'ACTIVE' | 'CANCELLED' | 'EXPIRED';
  startedAt: string;
  expiresAt: string;
  pendingSubscriptionId: number | null;
  pendingBillingCycle: string | null;
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
  const { data } = await client.get<MySubscription>(
    '/user-subscriptions/me',
  );
  return data;
}

/** PUT /api/user-subscriptions/me -- change (upgrade/downgrade) subscription */
export async function changeMySubscription(
  req: ChangeSubscriptionRequest,
): Promise<SubscriptionChangeResponse> {
  const { data } = await client.put<SubscriptionChangeResponse>(
    '/user-subscriptions/me',
    req,
  );
  return data;
}

/** DELETE /api/user-subscriptions/me -- cancel my subscription */
export async function cancelMySubscription(): Promise<void> {
  await client.delete('/user-subscriptions/me');
}

/** GET /api/utils/subscription-change-preview -- preview change impact */
export async function fetchSubscriptionChangePreview(
  subscriptionId: number,
  billingCycle: 'MONTHLY' | 'YEARLY',
): Promise<SubscriptionChangePreview> {
  const { data } = await client.get<SubscriptionChangePreview>(
    '/utils/subscription-change-preview',
    { params: { subscriptionId, billingCycle } },
  );
  return data;
}

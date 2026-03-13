import client from '@/api/client';
import type { PagedResponse } from '@/types';

export interface SubscriptionPlan {
  id: number;
  name: string;
  description: string;
  userType: string;
  priceMonthly: number;
  priceYearly: number;
  downloadPerDay: number;
  maxWhitelistChannels: number;
  isActive: boolean;
}

export async function fetchSubscriptionPlans(
  userType?: string,
): Promise<PagedResponse<SubscriptionPlan>> {
  const params = userType ? { userType } : {};
  const { data } = await client.get<PagedResponse<SubscriptionPlan>>('/subscriptions', {
    params,
  });
  return data;
}

/** 6.1-admin GET /api/subscriptions/admin — all plans (active + inactive) */
export async function fetchAdminSubscriptionPlans(): Promise<PagedResponse<SubscriptionPlan>> {
  const { data } = await client.get<PagedResponse<SubscriptionPlan>>('/subscriptions/admin');
  return data;
}

import client from '@/api/client';
import type { ApiResponse } from '@/types';

export interface SubscriptionPlan {
  id: number;
  name: string;
  description: string;
  userType: string;
  priceMonthly: number;
  priceYearly: number;
  downloadPerDay: number;
  maxWhitelistChannels: number;
  maxPlaylists: number;
  isActive: boolean;
}

interface SubscriptionListResponse {
  dataList: SubscriptionPlan[];
}

export async function fetchSubscriptionPlans(
  userType?: string,
): Promise<SubscriptionPlan[]> {
  const params = userType ? { userType } : {};
  const { data } = await client.get<SubscriptionListResponse>('/subscriptions', {
    params,
  });
  return data.dataList;
}

/** GET /api/subscriptions/{id} -- subscription plan detail */
export async function fetchSubscriptionPlanDetail(
  subscriptionId: number,
): Promise<SubscriptionPlan> {
  const { data } = await client.get<ApiResponse<SubscriptionPlan>>(
    `/subscriptions/${subscriptionId}`,
  );
  return data.data;
}

/** 6.1-admin GET /api/subscriptions/admin — all plans (active + inactive) */
export async function fetchAdminSubscriptionPlans(): Promise<SubscriptionPlan[]> {
  const { data } = await client.get<SubscriptionListResponse>('/subscriptions/admin');
  return data.dataList;
}

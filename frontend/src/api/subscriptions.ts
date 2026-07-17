import client from '@/api/client';

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

export async function fetchSubscriptionPlans(userType?: string): Promise<SubscriptionPlan[]> {
  const params = userType ? { userType } : {};
  const { data } = await client.get<SubscriptionListResponse>('/subscriptions', {
    params,
  });
  return data.dataList;
}

/** 6.1-admin GET /api/subscriptions/admin — all plans (active + inactive) */
export async function fetchAdminSubscriptionPlans(
  signal?: AbortSignal,
): Promise<SubscriptionPlan[]> {
  const { data } = await client.get<SubscriptionListResponse>('/subscriptions/admin', { signal });
  return data.dataList;
}

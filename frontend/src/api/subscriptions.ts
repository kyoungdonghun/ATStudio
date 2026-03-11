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
  isActive: boolean;
}

export async function fetchSubscriptionPlans(
  userType?: string,
): Promise<SubscriptionPlan[]> {
  const params = userType ? { userType } : {};
  const { data } = await client.get<{ dataList: SubscriptionPlan[] }>('/subscriptions', {
    params,
  });
  return data.dataList;
}

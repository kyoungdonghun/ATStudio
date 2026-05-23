import client from '@/api/client';
import type { ApiResponse } from '@/types';
import type { MySubscription } from '@/api/userSubscriptions';

export type PaymentProvider = 'MOCK' | 'TOSS' | 'TOSS_BILLING' | 'KAKAOPAY';
export type PaymentOrderStatus =
  | 'READY'
  | 'IN_PROGRESS'
  | 'DONE'
  | 'FAILED'
  | 'CANCELLED'
  | 'EXPIRED';

export interface PaymentCheckout {
  type: 'MOCK' | string;
  confirmToken?: string;
  clientKey?: string;
  customerKey?: string;
  orderName?: string;
  successUrl?: string;
  failUrl?: string;
  method?: string;
}

export interface BillingAgreementPrepareRequest {
  subscriptionId: number;
  billingCycle: 'MONTHLY' | 'YEARLY';
}

export interface BillingAgreementPrepareResponse {
  orderId: string;
  provider: 'TOSS_BILLING';
  purpose: 'SUBSCRIBE' | 'BILLING_AGREEMENT';
  agreementStatus: 'READY' | 'ACTIVE' | 'SUSPENDED' | 'CANCELLED' | 'EXPIRED';
  subscriptionId: number;
  billingCycle: 'MONTHLY' | 'YEARLY';
  amount: number;
  currency: string;
  expiresAt: string;
  checkout: PaymentCheckout;
}

export interface BillingAgreementConfirmRequest {
  orderId: string;
  authKey: string;
  customerKey: string;
  amount: number;
}

export interface BillingAgreementConfirmResponse {
  orderId: string;
  orderStatus: PaymentOrderStatus;
  provider: 'TOSS_BILLING';
  agreementStatus: 'READY' | 'ACTIVE' | 'SUSPENDED' | 'CANCELLED' | 'EXPIRED';
  nextBillingAt: string | null;
  subscription: MySubscription | null;
}

export interface BillingAgreementResponse {
  provider: 'TOSS_BILLING';
  status: 'READY' | 'ACTIVE' | 'SUSPENDED' | 'CANCELLED' | 'EXPIRED';
  payMethod: string | null;
  maskedMethod: string | null;
  nextBillingAt: string | null;
  lastChargedAt: string | null;
  cancelledAt: string | null;
  subscription: MySubscription | null;
}

export async function prepareBillingAgreement(
  req: BillingAgreementPrepareRequest,
): Promise<BillingAgreementPrepareResponse> {
  const { data } = await client.post<ApiResponse<BillingAgreementPrepareResponse>>(
    '/payments/billing-agreements/prepare',
    req,
  );
  return data.data;
}

export async function confirmBillingAgreement(
  req: BillingAgreementConfirmRequest,
): Promise<BillingAgreementConfirmResponse> {
  const { data } = await client.post<ApiResponse<BillingAgreementConfirmResponse>>(
    '/payments/billing-agreements/confirm',
    req,
  );
  return data.data;
}

export async function fetchMyBillingAgreement(): Promise<BillingAgreementResponse> {
  const { data } = await client.get<ApiResponse<BillingAgreementResponse>>(
    '/payments/billing-agreements/me',
  );
  return data.data;
}

export async function cancelMyBillingAgreement(): Promise<BillingAgreementResponse> {
  const { data } = await client.delete<ApiResponse<BillingAgreementResponse>>(
    '/payments/billing-agreements/me',
  );
  return data.data;
}

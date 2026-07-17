import client from '@/api/client';
import type { ApiResponse } from '@/types';
import type { MySubscription } from '@/api/userSubscriptions';

export const PAYMENT_ORDER_STATUSES = [
  'READY',
  'IN_PROGRESS',
  'PROCESSING',
  'PROVIDER_SUCCEEDED',
  'PENDING_PROVIDER_CONFIRMATION',
  'DONE',
  'FAILED',
  'CANCELLED',
  'EXPIRED',
] as const;

export type PaymentOrderStatus = (typeof PAYMENT_ORDER_STATUSES)[number];

export interface PaymentCheckout {
  type: 'TOSS_BILLING_AUTH';
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
  provider: 'TOSS';
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
  provider: 'TOSS';
  agreementStatus: 'READY' | 'ACTIVE' | 'SUSPENDED' | 'CANCELLED' | 'EXPIRED';
  nextBillingAt: string | null;
  subscription: MySubscription | null;
}

export interface BillingAgreementResponse {
  provider: 'TOSS';
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

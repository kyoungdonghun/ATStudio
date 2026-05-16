import client from '@/api/client';
import type { ApiResponse } from '@/types';
import type { MySubscription } from '@/api/userSubscriptions';

export type PaymentProvider = 'MOCK' | 'TOSS' | 'TOSS_BILLING' | 'KAKAOPAY';
export type PaymentPurpose = 'SUBSCRIBE' | 'UPGRADE' | 'RENEWAL' | 'BILLING_AGREEMENT';
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
}

export interface PaymentPrepareRequest {
  purpose: PaymentPurpose;
  subscriptionId: number;
  billingCycle: 'MONTHLY' | 'YEARLY';
}

export interface PaymentPrepareResponse {
  orderId: string;
  provider: PaymentProvider;
  purpose: PaymentPurpose;
  amount: number;
  currency: string;
  expiresAt: string;
  checkout: PaymentCheckout;
}

export interface PaymentConfirmRequest {
  orderId: string;
  amount: number;
  provider: PaymentProvider;
  providerToken?: string;
  paymentKey?: string;
}

export interface PaymentConfirmResponse {
  orderId: string;
  status: PaymentOrderStatus;
  purpose: PaymentPurpose;
  subscription: MySubscription | null;
}

export interface PaymentCancelRequest {
  orderId: string;
  status: 'FAILED' | 'CANCELLED';
  reason?: string;
}

export interface PaymentOrderResponse {
  orderId: string;
  status: PaymentOrderStatus;
  purpose: PaymentPurpose;
}

export async function prepareSubscriptionPayment(
  req: PaymentPrepareRequest,
): Promise<PaymentPrepareResponse> {
  const { data } = await client.post<ApiResponse<PaymentPrepareResponse>>(
    '/payments/subscriptions/prepare',
    req,
  );
  return data.data;
}

export async function confirmPayment(req: PaymentConfirmRequest): Promise<PaymentConfirmResponse> {
  const { data } = await client.post<ApiResponse<PaymentConfirmResponse>>('/payments/confirm', req);
  return data.data;
}

export async function cancelPayment(req: PaymentCancelRequest): Promise<PaymentOrderResponse> {
  const { data } = await client.post<ApiResponse<PaymentOrderResponse>>('/payments/cancel', req);
  return data.data;
}

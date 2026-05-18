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
  method?: string;
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

export interface BillingAgreementPrepareRequest {
  subscriptionId: number;
  billingCycle: 'MONTHLY' | 'YEARLY';
}

export interface BillingAgreementPrepareResponse {
  orderId: string;
  provider: 'TOSS_BILLING';
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
  customerKey: string;
  payMethod: string | null;
  maskedMethod: string | null;
  nextBillingAt: string | null;
  lastChargedAt: string | null;
  cancelledAt: string | null;
  subscription: MySubscription | null;
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

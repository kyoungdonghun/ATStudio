import { safeSessionStorage } from '@/utils/safeStorage';
import type { UserType } from '@/types';

type CheckoutPurpose = 'SUBSCRIBE' | 'BILLING_AGREEMENT';
type BillingCycle = 'MONTHLY' | 'YEARLY';

export interface CheckoutPrepareAttemptContext {
  purpose: CheckoutPurpose;
  planId: number;
  userType: UserType;
  billingCycle: BillingCycle;
}

interface StoredCheckoutPrepareAttempt {
  version: 1;
  context: CheckoutPrepareAttemptContext;
  idempotencyKey: string;
}

export class CorruptCheckoutPrepareAttemptError extends Error {
  constructor() {
    super('Invalid checkout prepare attempt record.');
    this.name = 'CorruptCheckoutPrepareAttemptError';
  }
}

const STORAGE_PREFIX = 'ats.checkout-prepare-attempt.v1';
const UUID_PATTERN = /^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/;

// Backend-alignment boundary for prepare results that require an explicit replacement attempt.
export const CHECKOUT_PREPARE_NEW_ATTEMPT_ERROR_CODES = new Set([
  'PAYMENT_PREPARE_IDEMPOTENCY_KEY_INVALID',
  'PAYMENT_ORDER_EXPIRED',
  'PAYMENT_ORDER_TERMINAL',
]);

export function getOrCreateCheckoutPrepareAttempt(context: CheckoutPrepareAttemptContext): string {
  const existing = readStoredAttempt(context);
  if (existing) return existing.idempotencyKey;

  return writeNewAttempt(context);
}

export function getCheckoutPrepareAttempt(context: CheckoutPrepareAttemptContext): string | null {
  return readStoredAttempt(context)?.idempotencyKey ?? null;
}

export function createNewCheckoutPrepareAttempt(context: CheckoutPrepareAttemptContext): string {
  return writeNewAttempt(context);
}

export function isNewCheckoutPrepareAttemptRequired(errorCode: string | null): boolean {
  return errorCode !== null && CHECKOUT_PREPARE_NEW_ATTEMPT_ERROR_CODES.has(errorCode);
}

function readStoredAttempt(
  context: CheckoutPrepareAttemptContext,
): StoredCheckoutPrepareAttempt | null {
  const raw = safeSessionStorage.getItem(storageKey(context));
  if (raw === null) return null;

  let value: unknown;
  try {
    value = JSON.parse(raw);
  } catch {
    throw new CorruptCheckoutPrepareAttemptError();
  }

  if (!isStoredAttempt(value) || !hasSameContext(value.context, context)) {
    throw new CorruptCheckoutPrepareAttemptError();
  }

  return value;
}

function writeNewAttempt(context: CheckoutPrepareAttemptContext): string {
  if (typeof crypto.randomUUID !== 'function') {
    throw new Error('Secure checkout attempt generation is unavailable.');
  }

  const idempotencyKey = crypto.randomUUID();
  if (!UUID_PATTERN.test(idempotencyKey)) {
    throw new Error('Secure checkout attempt generation returned an invalid value.');
  }

  const value: StoredCheckoutPrepareAttempt = { version: 1, context, idempotencyKey };
  if (!safeSessionStorage.setItem(storageKey(context), JSON.stringify(value))) {
    throw new Error('Checkout attempt storage is unavailable.');
  }

  return idempotencyKey;
}

function storageKey(context: CheckoutPrepareAttemptContext): string {
  return `${STORAGE_PREFIX}.${context.purpose}.${context.planId}.${context.userType}.${context.billingCycle}`;
}

function isStoredAttempt(value: unknown): value is StoredCheckoutPrepareAttempt {
  if (!value || typeof value !== 'object') return false;
  const attempt = value as Partial<StoredCheckoutPrepareAttempt>;
  return (
    attempt.version === 1 &&
    isContext(attempt.context) &&
    typeof attempt.idempotencyKey === 'string' &&
    UUID_PATTERN.test(attempt.idempotencyKey)
  );
}

function isContext(value: unknown): value is CheckoutPrepareAttemptContext {
  if (!value || typeof value !== 'object') return false;
  const context = value as Partial<CheckoutPrepareAttemptContext>;
  return (
    (context.purpose === 'SUBSCRIBE' || context.purpose === 'BILLING_AGREEMENT') &&
    typeof context.planId === 'number' &&
    Number.isSafeInteger(context.planId) &&
    context.planId > 0 &&
    (context.userType === 'INDIVIDUAL' || context.userType === 'BUSINESS') &&
    (context.billingCycle === 'MONTHLY' || context.billingCycle === 'YEARLY')
  );
}

function hasSameContext(
  left: CheckoutPrepareAttemptContext,
  right: CheckoutPrepareAttemptContext,
): boolean {
  return (
    left.purpose === right.purpose &&
    left.planId === right.planId &&
    left.userType === right.userType &&
    left.billingCycle === right.billingCycle
  );
}

import { afterEach, describe, expect, it, vi } from 'vitest';

import {
  CHECKOUT_PREPARE_NEW_ATTEMPT_ERROR_CODES,
  CorruptCheckoutPrepareAttemptError,
  createNewCheckoutPrepareAttempt,
  getCheckoutPrepareAttempt,
  getOrCreateCheckoutPrepareAttempt,
  isNewCheckoutPrepareAttemptRequired,
} from '@/utils/checkoutPrepareAttempt';

const context = {
  purpose: 'SUBSCRIBE' as const,
  planId: 1,
  userType: 'INDIVIDUAL' as const,
  billingCycle: 'MONTHLY' as const,
};
const storageKey = 'ats.checkout-prepare-attempt.v1.SUBSCRIBE.1.INDIVIDUAL.MONTHLY';
const canonicalUUID = '11111111-1111-4111-8111-111111111111';

describe('checkout prepare attempts', () => {
  afterEach(() => {
    sessionStorage.clear();
    localStorage.clear();
    vi.restoreAllMocks();
  });

  it('keeps one opaque session-scoped key per exact checkout context', () => {
    const randomUUID = vi
      .spyOn(globalThis.crypto, 'randomUUID')
      .mockReturnValueOnce('11111111-1111-4111-8111-111111111111')
      .mockReturnValueOnce('22222222-2222-4222-8222-222222222222');

    expect(getOrCreateCheckoutPrepareAttempt(context)).toBe('11111111-1111-4111-8111-111111111111');
    expect(getOrCreateCheckoutPrepareAttempt(context)).toBe('11111111-1111-4111-8111-111111111111');
    expect(getOrCreateCheckoutPrepareAttempt({ ...context, billingCycle: 'YEARLY' })).not.toBe(
      '11111111-1111-4111-8111-111111111111',
    );
    expect(randomUUID).toHaveBeenCalledTimes(2);
    expect(localStorage.length).toBe(0);
  });

  it.each([
    ['malformed JSON', '{not-json'],
    ['wrong version', JSON.stringify({ version: 2, context, idempotencyKey: canonicalUUID })],
    [
      'context mismatch',
      JSON.stringify({
        version: 1,
        context: { ...context, billingCycle: 'YEARLY' },
        idempotencyKey: canonicalUUID,
      }),
    ],
    ['malformed UUID', JSON.stringify({ version: 1, context, idempotencyKey: 'not-a-uuid' })],
    [
      'uppercase UUID',
      JSON.stringify({
        version: 1,
        context,
        idempotencyKey: 'AAAAAAAA-AAAA-4AAA-8AAA-AAAAAAAAAAAA',
      }),
    ],
  ])('rejects a %s record without generating or changing its stored bytes', (_label, raw) => {
    sessionStorage.setItem(storageKey, raw);
    const randomUUID = vi.spyOn(globalThis.crypto, 'randomUUID');

    expect(() => getOrCreateCheckoutPrepareAttempt(context)).toThrow(
      CorruptCheckoutPrepareAttemptError,
    );
    expect(randomUUID).not.toHaveBeenCalled();
    expect(sessionStorage.getItem(storageKey)).toBe(raw);
  });

  it('validates a generated UUID before replacing the exact stored record', () => {
    const stored = JSON.stringify({ version: 1, context, idempotencyKey: canonicalUUID });
    sessionStorage.setItem(storageKey, stored);
    vi.spyOn(globalThis.crypto, 'randomUUID').mockReturnValue(
      'AAAAAAAA-AAAA-4AAA-8AAA-AAAAAAAAAAAA',
    );

    expect(() => createNewCheckoutPrepareAttempt(context)).toThrow();
    expect(sessionStorage.getItem(storageKey)).toBe(stored);
  });

  it('writes nothing when a generated UUID is invalid for an absent context record', () => {
    vi.spyOn(globalThis.crypto, 'randomUUID').mockReturnValue(
      '11111111-1111-5111-8111-111111111111',
    );

    expect(() => getOrCreateCheckoutPrepareAttempt(context)).toThrow();
    expect(sessionStorage.length).toBe(0);
  });

  it('rotates only through the explicit new-attempt operation', () => {
    vi.spyOn(globalThis.crypto, 'randomUUID')
      .mockReturnValueOnce('11111111-1111-4111-8111-111111111111')
      .mockReturnValueOnce('22222222-2222-4222-8222-222222222222');

    expect(getOrCreateCheckoutPrepareAttempt(context)).toBe('11111111-1111-4111-8111-111111111111');
    expect(getCheckoutPrepareAttempt(context)).toBe('11111111-1111-4111-8111-111111111111');
    expect(createNewCheckoutPrepareAttempt(context)).toBe('22222222-2222-4222-8222-222222222222');
    expect(getCheckoutPrepareAttempt(context)).toBe('22222222-2222-4222-8222-222222222222');
  });

  it('overwrites only the exact context during explicit replacement', () => {
    const yearlyContext = { ...context, billingCycle: 'YEARLY' as const };
    const yearlyStorageKey = 'ats.checkout-prepare-attempt.v1.SUBSCRIBE.1.INDIVIDUAL.YEARLY';
    vi.spyOn(globalThis.crypto, 'randomUUID')
      .mockReturnValueOnce('11111111-1111-4111-8111-111111111111')
      .mockReturnValueOnce('22222222-2222-4222-8222-222222222222')
      .mockReturnValueOnce('33333333-3333-4333-8333-333333333333');

    getOrCreateCheckoutPrepareAttempt(context);
    getOrCreateCheckoutPrepareAttempt(yearlyContext);
    const yearlyRecord = sessionStorage.getItem(yearlyStorageKey);

    expect(createNewCheckoutPrepareAttempt(context)).toBe('33333333-3333-4333-8333-333333333333');
    expect(sessionStorage.getItem(yearlyStorageKey)).toBe(yearlyRecord);
    expect(sessionStorage.length).toBe(2);
  });

  it('uses the exact explicit-replacement error-code set', () => {
    expect([...CHECKOUT_PREPARE_NEW_ATTEMPT_ERROR_CODES]).toEqual([
      'PAYMENT_PREPARE_IDEMPOTENCY_KEY_INVALID',
      'PAYMENT_ORDER_EXPIRED',
      'PAYMENT_ORDER_TERMINAL',
    ]);
  });

  it.each([
    ['PAYMENT_PREPARE_IDEMPOTENCY_KEY_INVALID', true],
    ['PAYMENT_ORDER_EXPIRED', true],
    ['PAYMENT_ORDER_TERMINAL', true],
    ['PAYMENT_PREPARE_ATTEMPT_CONFLICT', false],
    ['PAYMENT_ORDER_INVALID_STATE', false],
    ['UNRELATED_CONFLICT', false],
    ['PAYMENT_PROVIDER_UNAVAILABLE', false],
    [null, false],
  ])('classifies %s replacement eligibility as %s', (errorCode, expected) => {
    expect(isNewCheckoutPrepareAttemptRequired(errorCode)).toBe(expected);
  });
});

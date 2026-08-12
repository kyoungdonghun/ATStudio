import { afterEach, describe, expect, it, vi } from 'vitest';

import {
  clearSettlementImportAttempt,
  CorruptSettlementImportAttemptError,
  createNewSettlementImportAttempt,
  getSettlementImportAttempt,
  PendingSettlementImportAttemptError,
  SETTLEMENT_IMPORT_ATTEMPT_STORAGE_KEY,
} from '@/utils/settlementImportAttempt';

const firstKey = '11111111-1111-4111-8111-111111111111';
const secondKey = '22222222-2222-4222-8222-222222222222';

describe('settlement import attempts', () => {
  afterEach(() => {
    sessionStorage.clear();
    vi.restoreAllMocks();
  });

  it('loads one valid ADMIN settlement-import record without mutation', () => {
    const stored = JSON.stringify({
      version: 1,
      scope: 'ADMIN',
      operation: 'SETTLEMENT_IMPORT',
      idempotencyKey: firstKey,
    });
    sessionStorage.setItem(SETTLEMENT_IMPORT_ATTEMPT_STORAGE_KEY, stored);

    expect(getSettlementImportAttempt()).toBe(firstKey);
    expect(sessionStorage.getItem(SETTLEMENT_IMPORT_ATTEMPT_STORAGE_KEY)).toBe(stored);
  });

  it('creates and stores a new key for an explicit new attempt', () => {
    vi.spyOn(globalThis.crypto, 'randomUUID').mockReturnValue(firstKey);

    expect(createNewSettlementImportAttempt()).toBe(firstKey);
    expect(getSettlementImportAttempt()).toBe(firstKey);
    expect(JSON.parse(sessionStorage.getItem(SETTLEMENT_IMPORT_ATTEMPT_STORAGE_KEY)!)).toEqual({
      version: 1,
      scope: 'ADMIN',
      operation: 'SETTLEMENT_IMPORT',
      idempotencyKey: firstKey,
    });
  });

  it('rejects corrupt storage without generating, rotating, or mutating it', () => {
    const corrupt = '{not-json';
    sessionStorage.setItem(SETTLEMENT_IMPORT_ATTEMPT_STORAGE_KEY, corrupt);
    const randomUUID = vi.spyOn(globalThis.crypto, 'randomUUID');

    expect(() => getSettlementImportAttempt()).toThrow(CorruptSettlementImportAttemptError);
    expect(() => createNewSettlementImportAttempt()).toThrow(CorruptSettlementImportAttemptError);
    expect(randomUUID).not.toHaveBeenCalled();
    expect(sessionStorage.getItem(SETTLEMENT_IMPORT_ATTEMPT_STORAGE_KEY)).toBe(corrupt);
  });

  it.each([
    ['wrong version', { version: 2 }],
    ['wrong scope', { scope: 'USER' }],
    ['wrong operation', { operation: 'REFUND' }],
    ['uppercase UUID', { idempotencyKey: 'AAAAAAAA-AAAA-4AAA-8AAA-AAAAAAAAAAAA' }],
    ['non-v4 UUID', { idempotencyKey: '11111111-1111-5111-8111-111111111111' }],
  ])('rejects a %s record as corrupt', (_label, override) => {
    const stored = JSON.stringify({
      version: 1,
      scope: 'ADMIN',
      operation: 'SETTLEMENT_IMPORT',
      idempotencyKey: firstKey,
      ...override,
    });
    sessionStorage.setItem(SETTLEMENT_IMPORT_ATTEMPT_STORAGE_KEY, stored);

    expect(() => getSettlementImportAttempt()).toThrow(CorruptSettlementImportAttemptError);
    expect(sessionStorage.getItem(SETTLEMENT_IMPORT_ATTEMPT_STORAGE_KEY)).toBe(stored);
  });

  it('rejects a valid pending attempt before generating a key and preserves exact bytes', () => {
    const stored = `{"operation":"SETTLEMENT_IMPORT", "idempotencyKey":"${firstKey}", "scope":"ADMIN", "version":1}`;
    sessionStorage.setItem(SETTLEMENT_IMPORT_ATTEMPT_STORAGE_KEY, stored);
    const randomUUID = vi.spyOn(globalThis.crypto, 'randomUUID');

    let thrown: unknown;
    try {
      createNewSettlementImportAttempt();
    } catch (error) {
      thrown = error;
    }

    expect(thrown).toBeInstanceOf(PendingSettlementImportAttemptError);
    expect((thrown as PendingSettlementImportAttemptError).idempotencyKey).toBe(firstKey);
    expect(randomUUID).not.toHaveBeenCalled();
    expect(sessionStorage.getItem(SETTLEMENT_IMPORT_ATTEMPT_STORAGE_KEY)).toBe(stored);
  });

  it('clears only when the matching key is provided', () => {
    vi.spyOn(globalThis.crypto, 'randomUUID').mockReturnValue(firstKey);
    createNewSettlementImportAttempt();

    clearSettlementImportAttempt(secondKey);
    expect(getSettlementImportAttempt()).toBe(firstKey);

    clearSettlementImportAttempt(firstKey);
    expect(getSettlementImportAttempt()).toBeNull();
  });

  it('does not clear corrupt storage', () => {
    const corrupt = JSON.stringify({ version: 1, idempotencyKey: firstKey });
    sessionStorage.setItem(SETTLEMENT_IMPORT_ATTEMPT_STORAGE_KEY, corrupt);

    expect(() => clearSettlementImportAttempt(firstKey)).toThrow(
      CorruptSettlementImportAttemptError,
    );
    expect(sessionStorage.getItem(SETTLEMENT_IMPORT_ATTEMPT_STORAGE_KEY)).toBe(corrupt);
  });
});

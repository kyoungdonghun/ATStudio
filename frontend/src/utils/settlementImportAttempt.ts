import { safeSessionStorage } from '@/utils/safeStorage';

interface StoredSettlementImportAttempt {
  version: 1;
  scope: 'ADMIN';
  operation: 'SETTLEMENT_IMPORT';
  idempotencyKey: string;
}

export class CorruptSettlementImportAttemptError extends Error {
  constructor() {
    super('Invalid settlement import attempt record.');
    this.name = 'CorruptSettlementImportAttemptError';
  }
}

export class PendingSettlementImportAttemptError extends Error {
  constructor(readonly idempotencyKey: string) {
    super('A settlement import attempt is already pending.');
    this.name = 'PendingSettlementImportAttemptError';
  }
}

export const SETTLEMENT_IMPORT_ATTEMPT_STORAGE_KEY = 'ats.admin.settlement-import-attempt.v1';

const UUID_V4_PATTERN = /^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/;

export function getSettlementImportAttempt(): string | null {
  return readStoredAttempt()?.idempotencyKey ?? null;
}

export function createNewSettlementImportAttempt(): string {
  const pendingAttempt = readStoredAttempt();
  if (pendingAttempt) {
    throw new PendingSettlementImportAttemptError(pendingAttempt.idempotencyKey);
  }

  if (typeof crypto.randomUUID !== 'function') {
    throw new Error('Secure settlement import key generation is unavailable.');
  }

  const idempotencyKey = crypto.randomUUID();
  if (!UUID_V4_PATTERN.test(idempotencyKey)) {
    throw new Error('Secure settlement import key generation returned an invalid value.');
  }

  const record: StoredSettlementImportAttempt = {
    version: 1,
    scope: 'ADMIN',
    operation: 'SETTLEMENT_IMPORT',
    idempotencyKey,
  };
  if (!safeSessionStorage.setItem(SETTLEMENT_IMPORT_ATTEMPT_STORAGE_KEY, JSON.stringify(record))) {
    throw new Error('Settlement import attempt storage is unavailable.');
  }

  return idempotencyKey;
}

export function clearSettlementImportAttempt(idempotencyKey: string): void {
  const storedKey = getSettlementImportAttempt();
  if (storedKey !== idempotencyKey) return;

  safeSessionStorage.removeItem(SETTLEMENT_IMPORT_ATTEMPT_STORAGE_KEY);
}

function readStoredAttempt(): StoredSettlementImportAttempt | null {
  const raw = safeSessionStorage.getItem(SETTLEMENT_IMPORT_ATTEMPT_STORAGE_KEY);
  if (raw === null) return null;

  let value: unknown;
  try {
    value = JSON.parse(raw);
  } catch {
    throw new CorruptSettlementImportAttemptError();
  }

  if (!isStoredSettlementImportAttempt(value)) {
    throw new CorruptSettlementImportAttemptError();
  }

  return value;
}

function isStoredSettlementImportAttempt(value: unknown): value is StoredSettlementImportAttempt {
  if (!value || typeof value !== 'object') return false;
  const record = value as Partial<StoredSettlementImportAttempt>;
  return (
    record.version === 1 &&
    record.scope === 'ADMIN' &&
    record.operation === 'SETTLEMENT_IMPORT' &&
    typeof record.idempotencyKey === 'string' &&
    UUID_V4_PATTERN.test(record.idempotencyKey)
  );
}

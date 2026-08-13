import { safeSessionStorage } from '@/utils/safeStorage';
import { getSafeLoginReturnTarget } from '@/utils/loginReturn';

export { getSafeLoginReturnTarget } from '@/utils/loginReturn';

const OAUTH_ATTEMPT_PREFIX = 'oauth_attempt:';
const OAUTH_PROFILE_RETURN_KEY = 'oauth_profile_return';
const OAUTH_ATTEMPT_MAX_AGE_MS = 10 * 60 * 1000;

interface OAuthAttemptRecord {
  state: string;
  codeVerifier: string;
  returnTarget: string;
  createdAt: number;
}

interface OAuthProfileReturnRecord {
  attemptId: string;
  userId: number;
  returnTarget: string;
  createdAt: number;
}

export interface OAuthCallbackAttempt {
  attemptId: string;
  codeVerifier: string;
  returnTarget: string;
}

export function createOAuthAttempt(
  state: string,
  codeVerifier: string,
  returnTarget: string,
): boolean {
  if (!isSafeAttemptId(state) || !codeVerifier || !getSafeLoginReturnTarget(returnTarget)) {
    return false;
  }
  const record: OAuthAttemptRecord = {
    state,
    codeVerifier,
    returnTarget,
    createdAt: Date.now(),
  };
  return safeSessionStorage.setItem(`${OAUTH_ATTEMPT_PREFIX}${state}`, JSON.stringify(record));
}

export function consumeOAuthCallbackAttempt(state: string | null): OAuthCallbackAttempt | null {
  if (!state || !isSafeAttemptId(state)) return null;
  const key = `${OAUTH_ATTEMPT_PREFIX}${state}`;
  const raw = safeSessionStorage.getItem(key);
  safeSessionStorage.removeItem(key);
  const record = parseRecord<OAuthAttemptRecord>(raw);
  if (
    !record ||
    record.state !== state ||
    !record.codeVerifier ||
    !isFresh(record.createdAt) ||
    getSafeLoginReturnTarget(record.returnTarget) !== record.returnTarget
  ) {
    return null;
  }
  return { attemptId: state, codeVerifier: record.codeVerifier, returnTarget: record.returnTarget };
}

export function storeOAuthProfileReturnTarget(
  attemptId: string,
  returnTarget: string,
  userId: number,
): boolean {
  safeSessionStorage.removeItem(OAUTH_PROFILE_RETURN_KEY);
  if (
    !isSafeAttemptId(attemptId) ||
    !isSafeUserId(userId) ||
    getSafeLoginReturnTarget(returnTarget) !== returnTarget
  ) {
    return false;
  }
  const record: OAuthProfileReturnRecord = {
    attemptId,
    userId,
    returnTarget,
    createdAt: Date.now(),
  };
  return safeSessionStorage.setItem(OAUTH_PROFILE_RETURN_KEY, JSON.stringify(record));
}

export function consumeOAuthProfileReturnTarget(currentUserId: number): string | null {
  const raw = safeSessionStorage.getItem(OAUTH_PROFILE_RETURN_KEY);
  safeSessionStorage.removeItem(OAUTH_PROFILE_RETURN_KEY);
  const record = parseRecord<OAuthProfileReturnRecord>(raw);
  if (
    !record ||
    !isSafeUserId(currentUserId) ||
    !isSafeUserId(record.userId) ||
    record.userId !== currentUserId ||
    !isSafeAttemptId(record.attemptId) ||
    !isFresh(record.createdAt) ||
    getSafeLoginReturnTarget(record.returnTarget) !== record.returnTarget
  ) {
    return null;
  }
  return record.returnTarget;
}

function isSafeAttemptId(value: string): boolean {
  return value.length >= 16 && value.length <= 256 && /^[A-Za-z0-9._~-]+$/.test(value);
}

function isSafeUserId(value: number): boolean {
  return Number.isSafeInteger(value) && value > 0;
}

function isFresh(createdAt: number): boolean {
  const age = Date.now() - createdAt;
  return Number.isFinite(createdAt) && age >= 0 && age <= OAUTH_ATTEMPT_MAX_AGE_MS;
}

function parseRecord<T>(raw: string | null): T | null {
  if (!raw) return null;
  try {
    const parsed = JSON.parse(raw) as unknown;
    return parsed && typeof parsed === 'object' ? (parsed as T) : null;
  } catch {
    return null;
  }
}

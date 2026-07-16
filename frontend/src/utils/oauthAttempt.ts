import { safeSessionStorage } from '@/utils/safeStorage';

const OAUTH_ATTEMPT_PREFIX = 'oauth_attempt:';
const OAUTH_PROFILE_RETURN_KEY = 'oauth_profile_return';
const OAUTH_ATTEMPT_MAX_AGE_MS = 10 * 60 * 1000;
const RETURN_TARGET_MAX_LENGTH = 2048;
const FORBIDDEN_RETURN_PREFIXES = [
  '/admin',
  '/api',
  '/uploads',
  '/login',
  '/signup',
  '/email-verify',
  '/password-reset',
  '/social-login',
  '/complete-profile',
] as const;

interface OAuthAttemptRecord {
  state: string;
  codeVerifier: string;
  returnTarget: string;
  createdAt: number;
}

interface OAuthProfileReturnRecord {
  attemptId: string;
  returnTarget: string;
  createdAt: number;
}

export interface OAuthCallbackAttempt {
  attemptId: string;
  codeVerifier: string;
  returnTarget: string;
}

export function getSafeLoginReturnTarget(candidate: string | null): string | null {
  if (
    !candidate ||
    candidate.length > RETURN_TARGET_MAX_LENGTH ||
    !candidate.startsWith('/') ||
    candidate.startsWith('//') ||
    candidate.includes('\\') ||
    candidate.includes('#')
  ) {
    return null;
  }

  let decodedCandidate: string;
  try {
    decodedCandidate = decodeURIComponent(candidate);
  } catch {
    return null;
  }

  if (
    decodedCandidate.startsWith('//') ||
    decodedCandidate.includes('\\') ||
    Array.from(decodedCandidate).some((character) => {
      const code = character.charCodeAt(0);
      return code <= 31 || code === 127;
    })
  ) {
    return null;
  }

  try {
    const base = new URL('https://local.invalid');
    const parsed = new URL(candidate, base);
    const normalizedTarget = `${parsed.pathname}${parsed.search}`;
    const normalizedPath = decodeURIComponent(parsed.pathname).toLowerCase();

    if (parsed.origin !== base.origin || normalizedTarget !== candidate) return null;

    const isForbidden = FORBIDDEN_RETURN_PREFIXES.some(
      (prefix) => normalizedPath === prefix || normalizedPath.startsWith(`${prefix}/`),
    );
    return isForbidden ? null : normalizedTarget;
  } catch {
    return null;
  }
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

export function storeOAuthProfileReturnTarget(attemptId: string, returnTarget: string): boolean {
  if (!isSafeAttemptId(attemptId) || getSafeLoginReturnTarget(returnTarget) !== returnTarget) {
    return false;
  }
  const record: OAuthProfileReturnRecord = {
    attemptId,
    returnTarget,
    createdAt: Date.now(),
  };
  return safeSessionStorage.setItem(OAUTH_PROFILE_RETURN_KEY, JSON.stringify(record));
}

export function consumeOAuthProfileReturnTarget(): string | null {
  const raw = safeSessionStorage.getItem(OAUTH_PROFILE_RETURN_KEY);
  safeSessionStorage.removeItem(OAUTH_PROFILE_RETURN_KEY);
  const record = parseRecord<OAuthProfileReturnRecord>(raw);
  if (
    !record ||
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

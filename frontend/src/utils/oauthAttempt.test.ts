import { beforeEach, describe, expect, it, vi } from 'vitest';
import {
  consumeOAuthCallbackAttempt,
  consumeOAuthProfileReturnTarget,
  createOAuthAttempt,
  getSafeLoginReturnTarget,
  storeOAuthProfileReturnTarget,
} from '@/utils/oauthAttempt';

const attemptId = 'oauth-state-1234567890';

describe('OAuth attempt continuity', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
    sessionStorage.clear();
  });

  it.each(['/profile?tab=edit', '/tracks/7', '/'])(
    'accepts safe internal return target %s',
    (target) => {
      expect(getSafeLoginReturnTarget(target)).toBe(target);
    },
  );

  it.each([
    null,
    'https://evil.example/steal',
    '//evil.example/steal',
    '/%2f%2fevil.example/steal',
    '/social-login/google',
  ])('rejects missing or unsafe return target %s', (target) => {
    expect(getSafeLoginReturnTarget(target)).toBeNull();
  });

  it('retains a structurally safe ADMIN target for post-login role validation', () => {
    expect(createOAuthAttempt(attemptId, 'verifier-123', '/admin/payments')).toBe(true);

    expect(consumeOAuthCallbackAttempt(attemptId)?.returnTarget).toBe('/admin/payments');
  });

  it('stores one attempt by state and consumes it exactly once', () => {
    expect(createOAuthAttempt(attemptId, 'verifier-123', '/profile?tab=edit')).toBe(true);

    expect(consumeOAuthCallbackAttempt(attemptId)).toEqual({
      attemptId,
      codeVerifier: 'verifier-123',
      returnTarget: '/profile?tab=edit',
    });
    expect(consumeOAuthCallbackAttempt(attemptId)).toBeNull();
  });

  it('rejects and removes a stale callback attempt', () => {
    vi.spyOn(Date, 'now').mockReturnValue(1_000_000);
    sessionStorage.setItem(
      `oauth_attempt:${attemptId}`,
      JSON.stringify({
        state: attemptId,
        codeVerifier: 'verifier-123',
        returnTarget: '/profile',
        createdAt: 1_000_000 - 10 * 60 * 1000 - 1,
      }),
    );

    expect(consumeOAuthCallbackAttempt(attemptId)).toBeNull();
    expect(sessionStorage.getItem(`oauth_attempt:${attemptId}`)).toBeNull();
  });

  it('revalidates a persisted target instead of trusting session storage', () => {
    sessionStorage.setItem(
      `oauth_attempt:${attemptId}`,
      JSON.stringify({
        state: attemptId,
        codeVerifier: 'verifier-123',
        returnTarget: '//evil.example/steal',
        createdAt: Date.now(),
      }),
    );

    expect(consumeOAuthCallbackAttempt(attemptId)).toBeNull();
  });

  it('carries an identity-bound incomplete-profile target exactly once', () => {
    expect(storeOAuthProfileReturnTarget(attemptId, '/tracks/7', 17)).toBe(true);
    expect(consumeOAuthProfileReturnTarget(17)).toBe('/tracks/7');
    expect(consumeOAuthProfileReturnTarget(17)).toBeNull();
  });

  it('deletes an incomplete-profile continuation when the account does not match', () => {
    expect(storeOAuthProfileReturnTarget(attemptId, '/tracks/7', 17)).toBe(true);

    expect(consumeOAuthProfileReturnTarget(18)).toBeNull();
    expect(sessionStorage.getItem('oauth_profile_return')).toBeNull();
    expect(consumeOAuthProfileReturnTarget(17)).toBeNull();
  });

  it('clears an old continuation before rejecting or replacing a new record', () => {
    expect(storeOAuthProfileReturnTarget(attemptId, '/tracks/7', 17)).toBe(true);
    expect(storeOAuthProfileReturnTarget('bad state', '/tracks/8', 18)).toBe(false);
    expect(sessionStorage.getItem('oauth_profile_return')).toBeNull();

    expect(storeOAuthProfileReturnTarget(attemptId, '/tracks/7', 17)).toBe(true);
    expect(storeOAuthProfileReturnTarget('next-state-1234567', '/tracks/8', 18)).toBe(true);
    expect(consumeOAuthProfileReturnTarget(18)).toBe('/tracks/8');
  });

  it('clears an old continuation when replacement storage fails', () => {
    expect(storeOAuthProfileReturnTarget(attemptId, '/tracks/7', 17)).toBe(true);
    const originalSetItem = Storage.prototype.setItem;
    vi.spyOn(Storage.prototype, 'setItem').mockImplementation(function (this: Storage, key, value) {
      if (this === sessionStorage && key === 'oauth_profile_return') {
        throw new Error('storage unavailable');
      }
      return originalSetItem.call(this, key, value);
    });

    expect(storeOAuthProfileReturnTarget('next-state-1234567', '/tracks/8', 18)).toBe(false);
    expect(sessionStorage.getItem('oauth_profile_return')).toBeNull();
  });

  it('rejects and removes a stale incomplete-profile continuation', () => {
    sessionStorage.setItem(
      'oauth_profile_return',
      JSON.stringify({
        attemptId,
        userId: 17,
        returnTarget: '/tracks/8',
        createdAt: Date.now() - 10 * 60 * 1000 - 1,
      }),
    );
    expect(consumeOAuthProfileReturnTarget(17)).toBeNull();
    expect(sessionStorage.getItem('oauth_profile_return')).toBeNull();
  });
});

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
    '/admin/payments',
    '/social-login/google',
  ])('rejects missing or unsafe return target %s', (target) => {
    expect(getSafeLoginReturnTarget(target)).toBeNull();
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

  it('carries an incomplete-profile target once and rejects a stale continuation', () => {
    expect(storeOAuthProfileReturnTarget(attemptId, '/tracks/7')).toBe(true);
    expect(consumeOAuthProfileReturnTarget()).toBe('/tracks/7');
    expect(consumeOAuthProfileReturnTarget()).toBeNull();

    sessionStorage.setItem(
      'oauth_profile_return',
      JSON.stringify({
        attemptId,
        returnTarget: '/tracks/8',
        createdAt: Date.now() - 10 * 60 * 1000 - 1,
      }),
    );
    expect(consumeOAuthProfileReturnTarget()).toBeNull();
  });
});

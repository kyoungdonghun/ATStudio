import { describe, expect, it } from 'vitest';
import {
  createLoginPath,
  getAccessibleLoginReturnTarget,
  getSafeLoginReturnTarget,
} from '@/utils/loginReturn';

describe('safe login return navigation', () => {
  it.each(['/profile?tab=edit', '/tracks/7', '/admin/dashboard', '/'])(
    'accepts the structurally safe internal target %s',
    (target) => {
      expect(getSafeLoginReturnTarget(target)).toBe(target);
    },
  );

  it.each([
    null,
    '',
    'https://evil.example/steal',
    '//evil.example/steal',
    '/%2f%2fevil.example/steal',
    '/profile%zz',
    '/profile\\settings',
    '/profile#private',
    '/login',
    '/login/reset',
    '/social-login/google',
    '/api/users/me',
    '/uploads/private.txt',
  ])('rejects the malformed, external, looping, or non-route target %s', (target) => {
    expect(getSafeLoginReturnTarget(target)).toBeNull();
  });

  it('constructs a login destination from pathname and query while excluding hash', () => {
    expect(
      createLoginPath({
        pathname: '/tracks/7',
        search: '?from=player&tab=details',
        hash: '#ignored',
      }),
    ).toBe('/login?returnTo=%2Ftracks%2F7%3Ffrom%3Dplayer%26tab%3Ddetails');
  });

  it('does not construct a Login loop from an unsafe origin', () => {
    expect(createLoginPath({ pathname: '/login', search: '?returnTo=%2Fprofile' })).toBe('/login');
  });

  it.each([
    ['/admin/dashboard', { role: 'ADMIN', userType: 'INDIVIDUAL' }, '/admin/dashboard'],
    ['/admin/dashboard', { role: 'USER', userType: 'INDIVIDUAL' }, null],
    [
      '/subscriptions/checkout',
      { role: 'USER', userType: 'INDIVIDUAL' },
      '/subscriptions/checkout',
    ],
    ['/subscriptions/checkout', { role: 'ADMIN', userType: 'INDIVIDUAL' }, null],
    [
      '/company-certification/status',
      { role: 'USER', userType: 'BUSINESS' },
      '/company-certification/status',
    ],
    ['/company-certification/status', { role: 'USER', userType: 'INDIVIDUAL' }, null],
    ['/company-certification/status', { role: 'ADMIN', userType: 'BUSINESS' }, null],
    ['/profile?tab=edit', { role: 'USER', userType: 'INDIVIDUAL' }, '/profile?tab=edit'],
  ] as const)(
    'applies the current role and user-type boundary to %s',
    (target, identity, expected) => {
      expect(getAccessibleLoginReturnTarget(target, identity)).toBe(expected);
    },
  );

  it.each([
    ['/%61dmin/dashboard', { role: 'USER', userType: 'INDIVIDUAL' }, null],
    ['/%61dmin/dashboard', { role: 'ADMIN', userType: 'INDIVIDUAL' }, '/%61dmin/dashboard'],
    ['/AdMiN%2Fdashboard', { role: 'USER', userType: 'INDIVIDUAL' }, null],
    ['/AdMiN%2Fdashboard', { role: 'ADMIN', userType: 'INDIVIDUAL' }, '/AdMiN%2Fdashboard'],
    ['/subscriptions%2Fcheckout', { role: 'ADMIN', userType: 'INDIVIDUAL' }, null],
    [
      '/subscriptions%2Fcheckout?plan=2',
      { role: 'USER', userType: 'INDIVIDUAL' },
      '/subscriptions%2Fcheckout?plan=2',
    ],
    ['/Company-Certification%2Fstatus', { role: 'USER', userType: 'INDIVIDUAL' }, null],
    [
      '/Company-Certification%2Fstatus',
      { role: 'USER', userType: 'BUSINESS' },
      '/Company-Certification%2Fstatus',
    ],
  ] as const)(
    'classifies the canonical decoded access boundary for %s',
    (target, identity, expected) => {
      expect(getAccessibleLoginReturnTarget(target, identity)).toBe(expected);
    },
  );
});

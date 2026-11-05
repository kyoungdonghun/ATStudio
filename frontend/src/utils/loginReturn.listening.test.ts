import { describe, expect, it, vi } from 'vitest';
vi.mock('@/config/appMode', () => ({ isListeningMode: true }));
import { getAccessibleLoginReturnTarget } from './loginReturn';

describe('listening operator return target', () => {
  const admin = { role: 'ADMIN', userType: 'INDIVIDUAL' } as const;
  it.each([
    null,
    '/admin/payments',
    '/admin/users',
    '//outside.invalid',
    '/subscriptions',
    '/admin/tracks/0/edit',
  ])('replaces excluded target %s with track management', (target) => {
    expect(getAccessibleLoginReturnTarget(target, admin)).toBe('/admin/track-manage');
  });
  it.each(['/admin/track-manage?page=2', '/admin/tracks/upload', '/admin/tracks/11/edit'])(
    'preserves operator target %s',
    (target) => {
      expect(getAccessibleLoginReturnTarget(target, admin)).toBe(target);
    },
  );
  it('never returns a non-admin to operator screens', () => {
    expect(
      getAccessibleLoginReturnTarget('/admin/tracks/upload', {
        role: 'USER',
        userType: 'INDIVIDUAL',
      }),
    ).toBe('/tracks');
  });
});

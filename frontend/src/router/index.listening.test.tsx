import { type ReactElement } from 'react';
import { describe, expect, it, vi } from 'vitest';
import ProtectedRoute, { type ProtectedRouteProps } from './ProtectedRoute';

vi.mock('@/config/appMode', async (importOriginal) => ({
  ...(await importOriginal<typeof import('@/config/appMode')>()),
  isListeningMode: true,
}));

import { routes } from '@/router';

describe('listening administrator routes', () => {
  it('adds tags within the ADMIN boundary and keeps the limited child route set', () => {
    const admin = routes.find((route) => route.path === '/admin');
    const guard = admin?.element as ReactElement<ProtectedRouteProps>;
    expect(guard.type).toBe(ProtectedRoute);
    expect(guard.props.minRole).toBe('ADMIN');
    expect(admin?.children?.filter((route) => route.path).map((route) => route.path)).toEqual([
      'track-manage',
      'tracks/upload',
      'tracks/:trackId/edit',
      'tags',
      '*',
    ]);
  });

  it('keeps payment and member routes absent from the public listening surface', () => {
    const paths = routes[0].children?.map((route) => route.path);
    expect(paths).toEqual(['/', '/tracks', '/tracks/:trackId', '/login', '*']);
  });
});

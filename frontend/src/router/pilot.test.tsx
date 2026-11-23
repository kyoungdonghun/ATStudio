import { type ReactElement } from 'react';
import { describe, expect, it, vi } from 'vitest';
import { isListeningVisitor } from '@/config/appMode';
import { getAccessibleLoginReturnTarget } from '@/utils/loginReturn';
import { routes } from '@/router';
import ProtectedRoute, { type ProtectedRouteProps } from './ProtectedRoute';

vi.mock('@/config/pilotMode', () => ({ isPilotMode: true }));

describe('friends pilot', () => {
  it('restricts the download, license and library pages to ordinary users', () => {
    const children = routes[0].children ?? [];
    expect(children.map((route) => route.path)).toEqual([
      '/',
      '/tracks',
      '/tracks/:trackId',
      '/login',
      '/downloads',
      '/likes',
      '/playlists',
      '/playlists/:playlistId',
      '/playlists/:playlistId/edit',
      '/licenses',
      '/licenses/:licenseId',
      '*',
    ]);
    for (const route of children.filter(
      ({ path }) => path && /^\/(?:likes|playlists|licenses|downloads)(?:\/|$)/.test(path),
    )) {
      const guard = route.element as ReactElement<ProtectedRouteProps>;
      expect(guard.type).toBe(ProtectedRoute);
      expect([guard.props.minRole, guard.props.maxRole]).toEqual(['USER', 'USER']);
    }
  });

  it('retains private return links and never redirects a participant to checkout', () => {
    const user = { role: 'USER', userType: 'INDIVIDUAL' } as const;
    expect(getAccessibleLoginReturnTarget('/licenses/17', user)).toBe('/licenses/17');
    expect(getAccessibleLoginReturnTarget('/playlists/3/edit', user)).toBe('/playlists/3/edit');
    expect(getAccessibleLoginReturnTarget('/likes', user)).toBe('/likes');
    expect(getAccessibleLoginReturnTarget('/subscriptions/checkout', user)).toBe('/tracks');
  });

  it('keeps catalog and stream requests anonymous and attaches credentials only to personal actions on track pages', () => {
    window.history.replaceState({}, '', '/tracks/21');
    for (const path of [
      '/tracks',
      '/tracks/21',
      '/tracks/21/stream',
      '/likes/albums',
      '/downloads/history/track-ids',
    ]) {
      expect(isListeningVisitor('get', path)).toBe(true);
    }
    expect(isListeningVisitor('post', '/tracks/batch')).toBe(true);
    for (const path of [
      '/tracks/21/download',
      '/downloads/history',
      '/utils/download-count',
      '/licenses/me',
      '/licenses/1',
      '/likes',
      '/playlists',
      '/playlists/capacity',
    ]) {
      expect(isListeningVisitor('get', path)).toBe(false);
      expect(isListeningVisitor('put', path)).toBe(true);
    }
    expect(isListeningVisitor('post', '/auth/logout')).toBe(false);
    for (const [verb, path] of [
      ['post', '/likes/21'],
      ['delete', '/likes/21'],
      ['post', '/playlists'],
      ['get', '/playlists/3'],
      ['put', '/playlists/3'],
      ['delete', '/playlists/3'],
      ['post', '/playlists/3/tracks'],
      ['put', '/playlists/3/tracks'],
      ['delete', '/playlists/3/tracks/21'],
    ])
      expect(isListeningVisitor(verb, path)).toBe(false);
  });
});

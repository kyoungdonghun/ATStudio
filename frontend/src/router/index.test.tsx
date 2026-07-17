import { isValidElement, type ReactElement } from 'react';
import { describe, expect, it } from 'vitest';
import ProtectedRoute, { type ProtectedRouteProps } from '@/router/ProtectedRoute';
import SubscriberRoute from '@/router/SubscriberRoute';
import { routes } from '@/router';

const USER_PAYMENT_ROUTES = [
  '/subscriptions/checkout',
  '/subscriptions/checkout/success',
  '/subscriptions/checkout/fail',
];

const BUSINESS_CERTIFICATION_ROUTES = [
  '/company-certification/apply',
  '/company-certification/status',
];

describe('router user payment boundaries', () => {
  it.each(USER_PAYMENT_ROUTES)(
    '%s is wired as USER-only with an admin management redirect',
    (path) => {
      const route = routes[0]?.children?.find((candidate) => candidate.path === path);

      expect(route).toBeDefined();
      expect(isValidElement(route?.element)).toBe(true);

      const element = route?.element as ReactElement<ProtectedRouteProps>;
      expect(element.type).toBe(ProtectedRoute);
      expect(element.props.minRole).toBe('USER');
      expect(element.props.maxRole).toBe('USER');
      expect(element.props.deniedRedirect).toBe('/admin/payments');
    },
  );
});

describe('router company certification boundaries', () => {
  it.each(BUSINESS_CERTIFICATION_ROUTES)('%s is wired as BUSINESS user-only', (path) => {
    const route = routes[0]?.children?.find((candidate) => candidate.path === path);

    expect(route).toBeDefined();
    expect(isValidElement(route?.element)).toBe(true);

    const element = route?.element as ReactElement<ProtectedRouteProps>;
    expect(element.type).toBe(ProtectedRoute);
    expect(element.props.minRole).toBe('USER');
    expect(element.props.maxRole).toBe('USER');
    expect(element.props.requiredUserType).toBe('BUSINESS');
    expect(element.props.deniedRedirect).toBe('/');
  });
});

describe('router download history boundary', () => {
  it('protects the canonical /downloads route as subscriber-only', () => {
    const route = routes[0]?.children?.find((candidate) => candidate.path === '/downloads');

    expect(route).toBeDefined();
    expect(isValidElement(route?.element)).toBe(true);

    const element = route?.element as ReactElement<{ children: ReactElement }>;
    expect(element.type).toBe(SubscriberRoute);
  });
});

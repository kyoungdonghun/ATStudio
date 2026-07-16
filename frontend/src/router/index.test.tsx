import { isValidElement, type ReactElement } from 'react';
import { render, screen } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import ProtectedRoute, { type ProtectedRouteProps } from '@/router/ProtectedRoute';
import SubscriberRoute from '@/router/SubscriberRoute';
import { routes } from '@/router';
import PlaylistListPage from '@/pages/subscriber/PlaylistListPage';

const fetchMyPlaylistsMock = vi.fn();
const fetchMySubscriptionMock = vi.fn();
const showToast = vi.fn();

vi.mock('@/api/playlists', () => ({
  fetchMyPlaylists: (...args: unknown[]) => fetchMyPlaylistsMock(...args),
  createPlaylist: vi.fn(),
  deletePlaylist: vi.fn(),
}));

vi.mock('@/api/userSubscriptions', () => ({
  fetchMySubscription: (...args: unknown[]) => fetchMySubscriptionMock(...args),
}));

vi.mock('@/store/toastStore', () => ({
  useToastStore: (selector: (state: { show: typeof showToast }) => unknown) =>
    selector({ show: showToast }),
}));

beforeEach(() => {
  fetchMyPlaylistsMock.mockReset();
  fetchMySubscriptionMock.mockReset();
  showToast.mockReset();
  fetchMyPlaylistsMock.mockResolvedValue({ dataList: [] });
  fetchMySubscriptionMock.mockResolvedValue({ subscription: { maxPlaylists: 3 } });
});

const USER_PAYMENT_ROUTES = [
  '/subscriptions/checkout',
  '/subscriptions/checkout/success',
  '/subscriptions/checkout/fail',
  '/subscriptions/payment',
  '/subscriptions/payment/success',
  '/subscriptions/payment/fail',
  '/subscriptions/billing/success',
  '/subscriptions/billing/fail',
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

describe('router playlist creation workflow', () => {
  it('opens the existing create modal when entering /playlists/new directly', async () => {
    const matchingRoutes = routes[0]?.children?.filter(
      (candidate) => candidate.path === '/playlists/new',
    );

    expect(matchingRoutes).toHaveLength(1);
    const element = matchingRoutes?.[0]?.element as ReactElement<{ children: ReactElement }>;
    expect(element.type).toBe(SubscriberRoute);

    render(
      <MemoryRouter initialEntries={['/playlists/new']}>
        <Routes>
          <Route path="/playlists/new" element={element.props.children} />
          <Route path="/playlists" element={<PlaylistListPage />} />
        </Routes>
      </MemoryRouter>,
    );

    expect(await screen.findByRole('dialog', { name: '새 재생목록 만들기' })).toBeInTheDocument();
  });
});

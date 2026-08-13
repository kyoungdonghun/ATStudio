import { StrictMode } from 'react';
import { act, fireEvent, render, screen, waitFor } from '@testing-library/react';
import { renderToString } from 'react-dom/server';
import { MemoryRouter, Route, Routes, useLocation } from 'react-router-dom';
import { StaticRouter } from 'react-router-dom/server';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import SubscriberRoute from '@/router/SubscriberRoute';

const authState = {
  isAuthenticated: () => false,
};

const showToast = vi.fn();
const fetchMySubscription = vi.fn();

vi.mock('@/store/authStore', () => ({
  useAuthStore: (selector: (state: typeof authState) => unknown) => selector(authState),
}));

vi.mock('@/store/toastStore', () => ({
  useToastStore: {
    getState: () => ({ show: showToast }),
  },
}));

vi.mock('@/api/userSubscriptions', async () => {
  const actual =
    await vi.importActual<typeof import('@/api/userSubscriptions')>('@/api/userSubscriptions');
  return {
    ...actual,
    fetchMySubscription: (...args: unknown[]) => fetchMySubscription(...args),
  };
});

function createDeferred<T>() {
  let resolve!: (value: T) => void;
  let reject!: (reason?: unknown) => void;
  const promise = new Promise<T>((resolvePromise, rejectPromise) => {
    resolve = resolvePromise;
    reject = rejectPromise;
  });
  return { promise, resolve, reject };
}

function LocationProbe({ label }: { label: string }) {
  const location = useLocation();
  return <div>{`${label}: ${location.pathname}${location.search}`}</div>;
}

function renderSubscriber({ strict = false, initialEntry = '/subscriber' } = {}) {
  const tree = (
    <MemoryRouter
      initialEntries={[initialEntry]}
      future={{ v7_startTransition: true, v7_relativeSplatPath: true }}
    >
      <Routes>
        <Route
          path="/subscriber"
          element={
            <SubscriberRoute>
              <div>Subscriber Page</div>
            </SubscriberRoute>
          }
        />
        <Route path="/login" element={<LocationProbe label="Login Page" />} />
        <Route path="/subscriptions" element={<div>Subscriptions Page</div>} />
      </Routes>
    </MemoryRouter>
  );

  return render(strict ? <StrictMode>{tree}</StrictMode> : tree);
}

describe('SubscriberRoute', () => {
  beforeEach(() => {
    authState.isAuthenticated = () => false;
    showToast.mockReset();
    fetchMySubscription.mockReset();
  });

  it('redirects unauthenticated users to login and shows a warning toast', async () => {
    renderSubscriber();

    expect(screen.getByText(/^Login Page:/)).toBeInTheDocument();
    await waitFor(() => {
      expect(showToast).toHaveBeenCalledWith('warning', '로그인이 필요한 기능입니다.');
    });
    expect(fetchMySubscription).not.toHaveBeenCalled();
  });

  it('preserves pathname and query, excludes hash, and warns once in StrictMode', async () => {
    renderSubscriber({
      strict: true,
      initialEntry: '/subscriber?tab=recent&page=2#ignored',
    });

    expect(
      screen.getByText('Login Page: /login?returnTo=%2Fsubscriber%3Ftab%3Drecent%26page%3D2'),
    ).toBeInTheDocument();
    await waitFor(() => expect(showToast).toHaveBeenCalledTimes(1));
  });

  it('does not mutate the toast store during render', () => {
    renderToString(
      <StaticRouter location="/subscriber?from=render">
        <Routes>
          <Route
            path="/subscriber"
            element={
              <SubscriberRoute>
                <div>Subscriber Page</div>
              </SubscriberRoute>
            }
          />
        </Routes>
      </StaticRouter>,
    );

    expect(showToast).not.toHaveBeenCalled();
  });

  it('renders children when the user has an active subscription', async () => {
    authState.isAuthenticated = () => true;
    fetchMySubscription.mockResolvedValue({ id: 1 });

    renderSubscriber();

    expect(screen.getByText('구독 상태 확인 중...')).toBeInTheDocument();
    await waitFor(() => {
      expect(screen.getByText('Subscriber Page')).toBeInTheDocument();
    });
    expect(showToast).not.toHaveBeenCalled();
  });

  it('redirects only the approved no-subscription domain outcome', async () => {
    authState.isAuthenticated = () => true;
    fetchMySubscription.mockRejectedValue({
      response: {
        status: 403,
        data: { errorCode: 'NO_ACTIVE_SUBSCRIPTION' },
      },
    });

    renderSubscriber();

    await waitFor(() => {
      expect(screen.getByText('Subscriptions Page')).toBeInTheDocument();
    });
    expect(showToast).toHaveBeenCalledWith('warning', '구독이 필요한 기능입니다.');
  });

  it('emits the inactive-subscription warning once in StrictMode', async () => {
    authState.isAuthenticated = () => true;
    fetchMySubscription.mockRejectedValue({
      response: {
        status: 403,
        data: { errorCode: 'NO_ACTIVE_SUBSCRIPTION' },
      },
    });

    renderSubscriber({ strict: true });

    await waitFor(() => expect(screen.getByText('Subscriptions Page')).toBeInTheDocument());
    expect(showToast).toHaveBeenCalledTimes(1);
  });

  it.each([
    ['401', { response: { status: 401 } }],
    ['other 403', { response: { status: 403, data: { errorCode: 'FORBIDDEN' } } }],
    ['500', { response: { status: 500 } }],
  ])('does not grant access or redirect to subscriptions for %s failures', async (_, error) => {
    authState.isAuthenticated = () => true;
    fetchMySubscription.mockRejectedValue(error);

    renderSubscriber();

    expect(await screen.findByRole('alert')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: '다시 시도' })).toBeEnabled();
    expect(screen.queryByText('Subscriber Page')).not.toBeInTheDocument();
    expect(screen.queryByText('Subscriptions Page')).not.toBeInTheDocument();
    expect(showToast).not.toHaveBeenCalled();
  });

  it('retries a timeout once, fences duplicate clicks, and renders children after success', async () => {
    authState.isAuthenticated = () => true;
    fetchMySubscription
      .mockRejectedValueOnce({ code: 'ECONNABORTED' })
      .mockResolvedValueOnce({ id: 1 });

    renderSubscriber();

    const retryButton = await screen.findByRole('button', { name: '다시 시도' });
    fireEvent.click(retryButton);
    fireEvent.click(retryButton);

    expect(retryButton).toBeDisabled();
    expect(fetchMySubscription).toHaveBeenCalledTimes(2);
    await waitFor(() => {
      expect(screen.getByText('Subscriber Page')).toBeInTheDocument();
    });
  });

  it('ignores stale success from a signal-ignoring request after a newer failure', async () => {
    authState.isAuthenticated = () => true;
    const staleRequest = createDeferred<{ id: number }>();
    const currentRequest = createDeferred<{ id: number }>();
    fetchMySubscription
      .mockImplementationOnce(() => staleRequest.promise)
      .mockImplementationOnce(() => currentRequest.promise);

    renderSubscriber({ strict: true });

    await waitFor(() => {
      expect(fetchMySubscription).toHaveBeenCalledTimes(2);
    });

    await act(async () => {
      currentRequest.reject({ response: { status: 500 } });
      await currentRequest.promise.catch(() => undefined);
    });
    expect(await screen.findByRole('alert')).toBeInTheDocument();

    await act(async () => {
      staleRequest.resolve({ id: 1 });
      await staleRequest.promise;
    });

    expect(screen.getByRole('alert')).toBeInTheDocument();
    expect(screen.queryByText('Subscriber Page')).not.toBeInTheDocument();
  });

  it('ignores stale failure from a signal-ignoring request after a newer success', async () => {
    authState.isAuthenticated = () => true;
    const staleRequest = createDeferred<{ id: number }>();
    const currentRequest = createDeferred<{ id: number }>();
    fetchMySubscription
      .mockImplementationOnce(() => staleRequest.promise)
      .mockImplementationOnce(() => currentRequest.promise);

    renderSubscriber({ strict: true });

    await waitFor(() => {
      expect(fetchMySubscription).toHaveBeenCalledTimes(2);
    });

    await act(async () => {
      currentRequest.resolve({ id: 2 });
      await currentRequest.promise;
    });
    expect(await screen.findByText('Subscriber Page')).toBeInTheDocument();

    await act(async () => {
      staleRequest.reject({ response: { status: 500 } });
      await staleRequest.promise.catch(() => undefined);
    });

    expect(screen.getByText('Subscriber Page')).toBeInTheDocument();
    expect(screen.queryByRole('alert')).not.toBeInTheDocument();
    expect(screen.queryByText('Subscriptions Page')).not.toBeInTheDocument();
  });
});

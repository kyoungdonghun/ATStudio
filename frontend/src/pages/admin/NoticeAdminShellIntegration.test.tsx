import { act, fireEvent, render, screen, waitFor, within } from '@testing-library/react';
import axios, {
  type AxiosAdapter,
  type AxiosResponse,
  type InternalAxiosRequestConfig,
} from 'axios';
import { createMemoryRouter, RouterProvider } from 'react-router-dom';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

import client from '@/api/client';
import AdminLayout from '@/layouts/AdminLayout';
import NoticeEditPage from '@/pages/admin/NoticeEditPage';
import ProtectedRoute from '@/router/ProtectedRoute';
import { useAuthStore } from '@/store/authStore';
import type { Notice, User } from '@/types';

const ADMIN_USER: User = {
  id: 7,
  email: 'admin@example.com',
  nickname: 'Admin',
  role: 'ADMIN',
  phonePersonal: null,
  phoneCompany: null,
  job: null,
  companyName: null,
  userType: 'INDIVIDUAL',
  isVerified: true,
  createdAt: '2026-01-01T00:00:00Z',
};

const EDIT_PROJECTION = {
  title: 'Existing notice',
  content: 'Existing content',
  isPinned: false,
  attachments: [],
};

const UPDATED_NOTICE: Notice = {
  id: 9,
  title: 'Existing notice',
  content: 'Existing content',
  isPinned: false,
  viewCount: 0,
  attachments: [],
  createdAt: '2026-08-13T00:00:00Z',
  updatedAt: '2026-08-13T00:00:00Z',
};

interface Deferred<T> {
  promise: Promise<T>;
  resolve: (value: T) => void;
  reject: (reason?: unknown) => void;
}

function deferred<T>(): Deferred<T> {
  let resolve!: (value: T) => void;
  let reject!: (reason?: unknown) => void;
  const promise = new Promise<T>((promiseResolve, promiseReject) => {
    resolve = promiseResolve;
    reject = promiseReject;
  });
  return { promise, resolve, reject };
}

function response<T>(config: InternalAxiosRequestConfig, data: T, status = 200): AxiosResponse<T> {
  return {
    data,
    status,
    statusText: status === 204 ? 'No Content' : 'OK',
    headers: {},
    config,
  };
}

function statusError(config: InternalAxiosRequestConfig, status: number) {
  return {
    config,
    response: response(config, {}, status),
  };
}

function networkError(config: InternalAxiosRequestConfig): Error {
  return Object.assign(new Error('response lost'), { config });
}

function beforeUnloadCallCount(spy: { mock: { calls: unknown[][] } }): number {
  return spy.mock.calls.filter((call) => call[0] === 'beforeunload').length;
}

function renderProtectedAdminEdit(keepDestinationsInAdminShell = false) {
  const adminChildren = [
    {
      path: '/admin/notices/:noticeId/edit',
      element: <NoticeEditPage />,
    },
  ];
  if (keepDestinationsInAdminShell) {
    adminChildren.push(
      { path: '/notices', element: <div>notice-list-destination</div> },
      { path: '/notices/:noticeId', element: <div>notice-detail-destination</div> },
    );
  }

  const router = createMemoryRouter(
    [
      {
        element: (
          <ProtectedRoute minRole="ADMIN">
            <AdminLayout />
          </ProtectedRoute>
        ),
        children: adminChildren,
      },
      ...(!keepDestinationsInAdminShell
        ? [
            { path: '/notices', element: <div>notice-list-destination</div> },
            { path: '/notices/:noticeId', element: <div>notice-detail-destination</div> },
          ]
        : []),
      { path: '/', element: <div>home</div> },
      { path: '/login', element: <div>login</div> },
    ],
    { initialEntries: ['/admin/notices/9/edit'] },
  );
  const navigate = vi.spyOn(router, 'navigate');
  render(<RouterProvider router={router} />);
  return { navigate, router };
}

function setAdminSession(): void {
  localStorage.setItem('accessToken', 'token-a');
  localStorage.setItem('refreshToken', 'refresh-a');
  localStorage.setItem('user', JSON.stringify(ADMIN_USER));
  useAuthStore.setState({
    accessToken: 'token-a',
    user: ADMIN_USER,
    role: 'ADMIN',
  });
}

describe('Notice ADMIN shell and auth replay integration', () => {
  const originalAdapter = client.defaults.adapter;
  const originalLogout = useAuthStore.getState().logout;

  beforeEach(() => {
    vi.restoreAllMocks();
    localStorage.clear();
    sessionStorage.clear();
    client.defaults.adapter = originalAdapter;
    useAuthStore.setState({ logout: originalLogout });
    setAdminSession();
  });

  afterEach(() => {
    client.defaults.adapter = originalAdapter;
    useAuthStore.setState({
      accessToken: null,
      user: null,
      role: 'GUEST',
      logout: originalLogout,
    });
    vi.restoreAllMocks();
  });

  it.each([
    ['success', 'success'],
    ['authoritative 4xx', 'authoritative'],
    ['ambiguous settlement', 'ambiguous'],
  ] as const)(
    'blocks same-tick Logout during a pending Notice save and releases it after %s',
    async (_label, terminal) => {
      const addEventListener = vi.spyOn(window, 'addEventListener');
      const removeEventListener = vi.spyOn(window, 'removeEventListener');
      const pendingMutation = deferred<AxiosResponse<unknown>>();
      let adminGETCount = 0;
      let mutationWireCount = 0;
      let mutationConfig: InternalAxiosRequestConfig | null = null;
      const adapter: AxiosAdapter = (config) => {
        if (config.method === 'get' && config.url === '/notices/9/admin') {
          adminGETCount += 1;
          return Promise.resolve(response(config, { data: EDIT_PROJECTION }));
        }
        if (config.method === 'put' && config.url === '/notices/9') {
          mutationWireCount += 1;
          mutationConfig = config;
          return pendingMutation.promise;
        }
        return Promise.reject(new Error(`Unexpected request: ${config.method} ${config.url}`));
      };
      client.defaults.adapter = adapter;
      const applicationPUT = vi.spyOn(client, 'put');
      const logoutPOST = vi.spyOn(client, 'post');
      const logout = vi.fn().mockResolvedValue(true);
      useAuthStore.setState({ logout });
      const { navigate, router } = renderProtectedAdminEdit(true);

      expect(await screen.findByDisplayValue('Existing notice')).toBeVisible();
      fireEvent.click(screen.getByRole('button', { name: '저장' }));

      await waitFor(() => expect(beforeUnloadCallCount(addEventListener)).toBe(1));
      expect(applicationPUT).toHaveBeenCalledTimes(1);
      expect(mutationWireCount).toBe(1);
      expect(adminGETCount).toBe(1);
      expect(beforeUnloadCallCount(removeEventListener)).toBe(0);

      const pendingBeforeUnload = new Event('beforeunload', { cancelable: true });
      window.dispatchEvent(pendingBeforeUnload);
      expect(pendingBeforeUnload.defaultPrevented).toBe(true);

      const logoutButton = screen.getByRole('button', { name: '로그아웃' });
      expect(logoutButton).toBeDisabled();
      logoutButton.removeAttribute('disabled');
      fireEvent.click(logoutButton);

      expect(logout).not.toHaveBeenCalled();
      expect(logoutPOST).not.toHaveBeenCalled();
      expect(navigate).not.toHaveBeenCalled();
      expect(router.state.location.pathname).toBe('/admin/notices/9/edit');
      expect(useAuthStore.getState()).toMatchObject({
        accessToken: 'token-a',
        user: ADMIN_USER,
        role: 'ADMIN',
      });
      expect(localStorage.getItem('accessToken')).toBe('token-a');

      const settledConfig = mutationConfig;
      if (!settledConfig) throw new Error('Expected one pending Notice mutation request.');
      await act(async () => {
        if (terminal === 'success') {
          pendingMutation.resolve(response(settledConfig, { data: UPDATED_NOTICE }));
        } else if (terminal === 'authoritative') {
          pendingMutation.reject(statusError(settledConfig, 400));
        } else {
          pendingMutation.reject(networkError(settledConfig));
        }
      });

      if (terminal === 'success') {
        await waitFor(() => expect(router.state.location.pathname).toBe('/notices/9'));
        expect(navigate).toHaveBeenCalledTimes(1);
      } else if (terminal === 'authoritative') {
        expect(await screen.findByText(/공지사항을 수정하지 못했습니다\./)).toBeVisible();
        expect(navigate).not.toHaveBeenCalled();
      } else {
        expect(await screen.findByText('처리 결과 확인 필요')).toBeVisible();
        expect(navigate).not.toHaveBeenCalled();
      }

      await waitFor(() => expect(beforeUnloadCallCount(removeEventListener)).toBe(1));
      expect(screen.getByRole('button', { name: '로그아웃' })).toBeEnabled();
      expect(applicationPUT).toHaveBeenCalledTimes(1);
      expect(mutationWireCount).toBe(1);
      expect(logout).not.toHaveBeenCalled();
      expect(logoutPOST).not.toHaveBeenCalled();

      const releasedBeforeUnload = new Event('beforeunload', { cancelable: true });
      window.dispatchEvent(releasedBeforeUnload);
      expect(releasedBeforeUnload.defaultPrevented).toBe(false);
    },
  );

  it.each([
    ['update', 'success'],
    ['update', 'response-loss'],
    ['delete', 'success'],
    ['delete', 'response-loss'],
  ] as const)(
    'keeps one %s application mutation owned across token A -> B replay %s',
    async (operation, terminal) => {
      const addEventListener = vi.spyOn(window, 'addEventListener');
      const removeEventListener = vi.spyOn(window, 'removeEventListener');
      const replay = deferred<AxiosResponse<unknown>>();
      const mutationHeaders: string[] = [];
      let adminGETCount = 0;
      let mutationWireCount = 0;
      let replayConfig: InternalAxiosRequestConfig | null = null;
      const mutationMethod = operation === 'update' ? 'put' : 'delete';
      const adapter: AxiosAdapter = (config) => {
        if (config.method === 'get' && config.url === '/notices/9/admin') {
          adminGETCount += 1;
          return Promise.resolve(response(config, { data: EDIT_PROJECTION }));
        }
        if (config.method === mutationMethod && config.url === '/notices/9') {
          mutationWireCount += 1;
          mutationHeaders.push(String(config.headers.Authorization));
          if (mutationWireCount === 1) {
            return Promise.reject(statusError(config, 401));
          }
          replayConfig = config;
          return replay.promise;
        }
        return Promise.reject(new Error(`Unexpected request: ${config.method} ${config.url}`));
      };
      client.defaults.adapter = adapter;
      const refresh = vi.spyOn(axios, 'post').mockResolvedValue({
        data: { data: { accessToken: 'token-b', refreshToken: 'refresh-b' } },
      });
      const applicationPUT = vi.spyOn(client, 'put');
      const applicationDELETE = vi.spyOn(client, 'delete');
      const { navigate, router } = renderProtectedAdminEdit();

      expect(await screen.findByDisplayValue('Existing notice')).toBeVisible();
      if (operation === 'update') {
        fireEvent.click(screen.getByRole('button', { name: '저장' }));
      } else {
        fireEvent.click(screen.getByRole('button', { name: '공지사항 삭제' }));
        fireEvent.click(within(screen.getByRole('dialog')).getByRole('button', { name: '삭제' }));
      }

      await waitFor(() => expect(mutationWireCount).toBe(2));
      expect(localStorage.getItem('accessToken')).toBe('token-b');
      expect(useAuthStore.getState().accessToken).toBe('token-b');
      expect(refresh).toHaveBeenCalledTimes(1);
      expect(refresh).toHaveBeenCalledWith('/api/auth/refresh', { refreshToken: 'refresh-a' });
      expect(applicationPUT).toHaveBeenCalledTimes(operation === 'update' ? 1 : 0);
      expect(applicationDELETE).toHaveBeenCalledTimes(operation === 'delete' ? 1 : 0);
      expect(mutationHeaders).toEqual(['Bearer token-a', 'Bearer token-b']);
      expect(adminGETCount).toBe(1);
      expect(navigate).not.toHaveBeenCalled();
      expect(beforeUnloadCallCount(addEventListener)).toBe(1);
      expect(beforeUnloadCallCount(removeEventListener)).toBe(0);
      expect(screen.getByDisplayValue('Existing notice')).toBeVisible();

      const pendingBeforeUnload = new Event('beforeunload', { cancelable: true });
      window.dispatchEvent(pendingBeforeUnload);
      expect(pendingBeforeUnload.defaultPrevented).toBe(true);

      const settledConfig = replayConfig;
      if (!settledConfig) throw new Error('Expected one replayed Notice mutation request.');
      await act(async () => {
        if (terminal === 'success') {
          replay.resolve(
            operation === 'update'
              ? response(settledConfig, { data: UPDATED_NOTICE })
              : response(settledConfig, undefined, 204),
          );
        } else {
          replay.reject(networkError(settledConfig));
        }
      });

      if (terminal === 'success') {
        const destination = operation === 'update' ? '/notices/9' : '/notices';
        await waitFor(() => expect(router.state.location.pathname).toBe(destination));
        expect(navigate).toHaveBeenCalledTimes(1);
        expect(adminGETCount).toBe(1);
      } else {
        expect(await screen.findByText('처리 결과 확인 필요')).toBeVisible();
        expect(navigate).not.toHaveBeenCalled();
        fireEvent.click(screen.getByRole('button', { name: '현재 상태 다시 확인' }));
        await waitFor(() => expect(adminGETCount).toBe(2));
        expect(await screen.findByDisplayValue('Existing notice')).toBeVisible();
      }

      await waitFor(() => expect(beforeUnloadCallCount(removeEventListener)).toBe(1));
      expect(applicationPUT).toHaveBeenCalledTimes(operation === 'update' ? 1 : 0);
      expect(applicationDELETE).toHaveBeenCalledTimes(operation === 'delete' ? 1 : 0);
      expect(mutationWireCount).toBe(2);
      expect(refresh).toHaveBeenCalledTimes(1);
      expect(useAuthStore.getState()).toMatchObject({
        accessToken: 'token-b',
        user: ADMIN_USER,
        role: 'ADMIN',
      });

      const releasedBeforeUnload = new Event('beforeunload', { cancelable: true });
      window.dispatchEvent(releasedBeforeUnload);
      expect(releasedBeforeUnload.defaultPrevented).toBe(false);
    },
  );

  it('retires a pending save result after the authenticated ADMIN user changes', async () => {
    const addEventListener = vi.spyOn(window, 'addEventListener');
    const removeEventListener = vi.spyOn(window, 'removeEventListener');
    const pendingMutation = deferred<AxiosResponse<unknown>>();
    let adminGETCount = 0;
    let mutationWireCount = 0;
    let mutationConfig: InternalAxiosRequestConfig | null = null;
    const adapter: AxiosAdapter = (config) => {
      if (config.method === 'get' && config.url === '/notices/9/admin') {
        adminGETCount += 1;
        return Promise.resolve(response(config, { data: EDIT_PROJECTION }));
      }
      if (config.method === 'put' && config.url === '/notices/9') {
        mutationWireCount += 1;
        mutationConfig = config;
        return pendingMutation.promise;
      }
      return Promise.reject(new Error(`Unexpected request: ${config.method} ${config.url}`));
    };
    client.defaults.adapter = adapter;
    const applicationPUT = vi.spyOn(client, 'put');
    const { navigate, router } = renderProtectedAdminEdit(true);

    expect(await screen.findByDisplayValue('Existing notice')).toBeVisible();
    fireEvent.click(screen.getByRole('button', { name: '저장' }));
    await waitFor(() => expect(beforeUnloadCallCount(addEventListener)).toBe(1));
    expect(applicationPUT).toHaveBeenCalledTimes(1);
    expect(mutationWireCount).toBe(1);
    expect(adminGETCount).toBe(1);

    act(() => {
      useAuthStore.setState({ user: { ...ADMIN_USER, id: 8 } });
    });
    await waitFor(() => expect(adminGETCount).toBe(2));
    expect(screen.getByRole('button', { name: '로그아웃' })).toBeDisabled();

    const settledConfig = mutationConfig;
    if (!settledConfig) throw new Error('Expected one pending Notice mutation request.');
    await act(async () => {
      pendingMutation.resolve(response(settledConfig, { data: UPDATED_NOTICE }));
    });

    await waitFor(() => expect(beforeUnloadCallCount(removeEventListener)).toBe(1));
    expect(router.state.location.pathname).toBe('/admin/notices/9/edit');
    expect(navigate).not.toHaveBeenCalled();
    expect(adminGETCount).toBe(2);
    expect(applicationPUT).toHaveBeenCalledTimes(1);
    expect(mutationWireCount).toBe(1);
    expect(screen.getByRole('button', { name: '로그아웃' })).toBeEnabled();
  });
});

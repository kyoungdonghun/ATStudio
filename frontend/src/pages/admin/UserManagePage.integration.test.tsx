import { fireEvent, render, screen, within } from '@testing-library/react';
import {
  AxiosError,
  AxiosHeaders,
  type AxiosAdapter,
  type AxiosResponse,
  type InternalAxiosRequestConfig,
} from 'axios';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import client from '@/api/client';
import type { AdminUserListItem } from '@/api/admin';
import UserManagePage from '@/pages/admin/UserManagePage';
import ProtectedRoute from '@/router/ProtectedRoute';
import { useAuthStore } from '@/store/authStore';
import type { PagedResponse, User } from '@/types';

const originalAdapter = client.defaults.adapter;

const currentAdmin: User = {
  id: 99,
  email: 'current-admin@example.com',
  nickname: 'CurrentAdmin',
  role: 'ADMIN',
  phonePersonal: null,
  phoneCompany: null,
  job: null,
  companyName: null,
  userType: 'INDIVIDUAL',
  isVerified: true,
  createdAt: '2026-08-09T09:00:00',
};

const targetAdmin: AdminUserListItem = {
  id: 2,
  email: 'target-admin@example.com',
  nickname: 'TargetAdmin',
  role: 'ADMIN',
  userType: 'INDIVIDUAL',
  isVerified: true,
  createdAt: '2026-08-09T09:00:00',
};

const serverCurrentUser: User = { ...currentAdmin, role: 'USER' };

function axiosResponse<T>(
  config: InternalAxiosRequestConfig,
  data: T,
  status = 200,
): AxiosResponse<T> {
  return {
    config,
    data,
    headers: new AxiosHeaders(),
    status,
    statusText: status === 200 ? 'OK' : 'Forbidden',
  };
}

function userPage(): PagedResponse<AdminUserListItem> {
  return {
    dataList: [targetAdmin],
    pageInfo: {
      page: 1,
      size: 20,
      total: 1,
      start: 1,
      end: 1,
      prev: false,
      next: false,
    },
  };
}

function renderProtectedPage() {
  return render(
    <MemoryRouter initialEntries={['/admin/users']}>
      <Routes>
        <Route
          path="/admin/users"
          element={
            <ProtectedRoute minRole="ADMIN">
              <UserManagePage />
            </ProtectedRoute>
          }
        />
        <Route path="/" element={<div>Home Page</div>} />
      </Routes>
    </MemoryRouter>,
  );
}

describe('UserManagePage authority synchronization integration', () => {
  beforeEach(() => {
    localStorage.clear();
    sessionStorage.clear();
    useAuthStore.getState().clearSession();
    useAuthStore.getState().login('access-token', currentAdmin, 'refresh-token');
  });

  afterEach(() => {
    client.defaults.adapter = originalAdapter;
    useAuthStore.getState().clearSession();
    localStorage.clear();
    sessionStorage.clear();
  });

  it('uses one page-owned refresh for a typed stale-ADMIN mutation and redirects', async () => {
    const roleMutationConfigs: InternalAxiosRequestConfig[] = [];
    let currentUserReads = 0;
    const adapter: AxiosAdapter = vi.fn(async (config) => {
      const method = config.method?.toLowerCase();
      if (method === 'get' && config.url === '/users') {
        return axiosResponse(config, userPage());
      }
      if (method === 'put' && config.url === '/users/2') {
        roleMutationConfigs.push(config);
        const response = axiosResponse(config, { errorCode: 'ADMIN_ROLE_REQUIRED' }, 403);
        throw new AxiosError(
          'Administrator role is stale',
          'ERR_BAD_REQUEST',
          config,
          undefined,
          response,
        );
      }
      if (method === 'get' && config.url === '/users/me') {
        currentUserReads += 1;
        return axiosResponse(config, { data: serverCurrentUser });
      }
      throw new Error(`Unexpected request: ${method ?? 'unknown'} ${config.url ?? ''}`);
    });
    client.defaults.adapter = adapter;

    renderProtectedPage();

    fireEvent.change(await screen.findByRole('combobox', { name: 'Change role for TargetAdmin' }), {
      target: { value: 'USER' },
    });
    const dialog = await screen.findByRole('dialog', { name: 'Confirm Role Change' });
    fireEvent.change(within(dialog).getByLabelText('Operator reason'), {
      target: { value: 'Stale authority integration check' },
    });
    fireEvent.click(within(dialog).getByRole('button', { name: 'Confirm' }));

    expect(await screen.findByText('Home Page')).toBeInTheDocument();
    expect(roleMutationConfigs).toHaveLength(1);
    expect(roleMutationConfigs[0]).toMatchObject({
      method: 'put',
      skipAdminRoleSync: true,
      url: '/users/2',
    });
    expect(roleMutationConfigs.filter((config) => '_retry' in config)).toHaveLength(0);
    expect(currentUserReads).toBe(1);
    expect(useAuthStore.getState().user).toEqual(serverCurrentUser);
    expect(useAuthStore.getState().role).toBe('USER');
    expect(JSON.parse(localStorage.getItem('user') ?? '{}')).toMatchObject({ role: 'USER' });
  });
});

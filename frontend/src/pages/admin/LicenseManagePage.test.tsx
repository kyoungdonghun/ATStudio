import { act, fireEvent, render, screen, waitFor } from '@testing-library/react';
import { createMemoryRouter, RouterProvider } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import type { AdminUserDetail } from '@/api/admin';
import type { LicenseListItem } from '@/api/licenses';
import type { PagedResponse } from '@/types';
import LicenseManagePage from './LicenseManagePage';

const mocks = vi.hoisted(() => ({
  fetchUserDetail: vi.fn(),
  fetchUsers: vi.fn(),
  fetchUserLicenses: vi.fn(),
}));

vi.mock('@/api/admin', () => ({
  fetchUserDetail: (...args: unknown[]) => mocks.fetchUserDetail(...args),
  fetchUsers: (...args: unknown[]) => mocks.fetchUsers(...args),
}));

vi.mock('@/api/licenses', () => ({
  fetchUserLicenses: (...args: unknown[]) => mocks.fetchUserLicenses(...args),
}));

function deferred<T>() {
  let resolve!: (value: T) => void;
  let reject!: (reason?: unknown) => void;
  const promise = new Promise<T>((nextResolve, nextReject) => {
    resolve = nextResolve;
    reject = nextReject;
  });
  return { promise, resolve, reject };
}

function user(id: number, nickname: string): AdminUserDetail {
  return {
    id,
    nickname,
    email: `${nickname.toLowerCase().replace(' ', '-')}@example.com`,
    userType: 'INDIVIDUAL',
    role: 'USER',
    isVerified: true,
    createdAt: '2026-08-01T00:00:00Z',
    phonePersonal: null,
    phoneCompany: null,
    job: null,
    companyName: null,
  };
}

function licensePage(id: number, title: string): PagedResponse<LicenseListItem> {
  return {
    dataList: [
      {
        id,
        track: { id, title },
        licenseCode: `LICENSE-${id}`,
        issuedAt: '2026-08-01T00:00:00Z',
      },
    ],
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

function userPage(...users: AdminUserDetail[]): PagedResponse<AdminUserDetail> {
  return {
    dataList: users,
    pageInfo: {
      page: 1,
      size: 10,
      total: users.length,
      start: users.length === 0 ? 0 : 1,
      end: users.length,
      prev: false,
      next: false,
    },
  };
}

function renderPage(initialEntry: string) {
  const router = createMemoryRouter([{ path: '/admin/licenses', element: <LicenseManagePage /> }], {
    initialEntries: [initialEntry],
  });
  render(<RouterProvider router={router} future={{ v7_startTransition: true }} />);
  return router;
}

describe('LicenseManagePage request ownership', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('resolves deep-linked user identity and keeps User B over late User A responses', async () => {
    const userA = deferred<AdminUserDetail>();
    const userB = deferred<AdminUserDetail>();
    const licensesA = deferred<PagedResponse<LicenseListItem>>();
    const licensesB = deferred<PagedResponse<LicenseListItem>>();
    mocks.fetchUserDetail.mockReturnValueOnce(userA.promise).mockReturnValueOnce(userB.promise);
    mocks.fetchUserLicenses
      .mockReturnValueOnce(licensesA.promise)
      .mockReturnValueOnce(licensesB.promise);

    const router = renderPage('/admin/licenses?userId=1&page=1');
    await waitFor(() => expect(mocks.fetchUserLicenses).toHaveBeenCalledTimes(1));
    const userASignal = mocks.fetchUserDetail.mock.calls[0][1] as AbortSignal;
    const licensesASignal = mocks.fetchUserLicenses.mock.calls[0][3] as AbortSignal;

    await act(async () => {
      await router.navigate('/admin/licenses?userId=2&page=1');
    });
    await waitFor(() => expect(mocks.fetchUserLicenses).toHaveBeenCalledTimes(2));
    expect(userASignal.aborted).toBe(true);
    expect(licensesASignal.aborted).toBe(true);

    await act(async () => {
      userB.resolve(user(2, 'User B'));
      licensesB.resolve(licensePage(202, 'User B Track'));
    });
    expect(await screen.findByText('User B')).toBeInTheDocument();
    expect(screen.getByText('user-b@example.com')).toBeInTheDocument();
    expect(screen.getByText('User B Track')).toBeInTheDocument();

    await act(async () => {
      userA.resolve(user(1, 'User A'));
      licensesA.resolve(licensePage(101, 'User A Track'));
    });
    expect(screen.getByText('User B')).toBeInTheDocument();
    expect(screen.getByText('User B Track')).toBeInTheDocument();
    expect(screen.queryByText('User A')).not.toBeInTheDocument();
    expect(screen.queryByText('User A Track')).not.toBeInTheDocument();
  });

  it('retires a pending search when the submitted keyword is edited', async () => {
    const search = deferred<PagedResponse<AdminUserDetail>>();
    mocks.fetchUsers.mockReturnValue(search.promise);
    renderPage('/admin/licenses');

    const input = screen.getByRole('textbox');
    fireEvent.change(input, { target: { value: 'user-a@example.com' } });
    fireEvent.keyDown(input, { key: 'Enter' });
    await waitFor(() => expect(mocks.fetchUsers).toHaveBeenCalledTimes(1));
    const signal = mocks.fetchUsers.mock.calls[0][1] as AbortSignal;

    fireEvent.change(input, { target: { value: 'user-b@example.com' } });
    expect(signal.aborted).toBe(true);

    await act(async () => search.resolve(userPage(user(1, 'User A'))));
    expect(screen.queryByRole('button', { name: /User A/ })).not.toBeInTheDocument();
    expect(screen.queryByText('user-a@example.com')).not.toBeInTheDocument();
  });

  it('retires a pending search when a visible result becomes the canonical user', async () => {
    const selectedUser = user(2, 'User B');
    const pendingSearch = deferred<PagedResponse<AdminUserDetail>>();
    mocks.fetchUsers
      .mockResolvedValueOnce(userPage(selectedUser))
      .mockReturnValueOnce(pendingSearch.promise);
    mocks.fetchUserDetail.mockResolvedValue(selectedUser);
    mocks.fetchUserLicenses.mockResolvedValue(licensePage(202, 'User B Track'));
    const router = renderPage('/admin/licenses');

    const input = screen.getByRole('textbox');
    fireEvent.change(input, { target: { value: selectedUser.email } });
    fireEvent.keyDown(input, { key: 'Enter' });
    const selection = await screen.findByRole('button', { name: /User B/ });

    fireEvent.keyDown(input, { key: 'Enter' });
    await waitFor(() => expect(mocks.fetchUsers).toHaveBeenCalledTimes(2));
    const pendingSignal = mocks.fetchUsers.mock.calls[1][1] as AbortSignal;
    fireEvent.click(selection);

    expect(pendingSignal.aborted).toBe(true);
    await waitFor(() => expect(router.state.location.search).toBe('?userId=2&page=1'));
    expect(await screen.findByText('User B Track')).toBeInTheDocument();

    await act(async () => pendingSearch.resolve(userPage(user(1, 'User A'))));
    expect(screen.getByText('User B')).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: /User A/ })).not.toBeInTheDocument();
  });
});

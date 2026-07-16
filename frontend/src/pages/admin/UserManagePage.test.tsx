import { StrictMode } from 'react';
import { act, render, screen, waitFor } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import UserManagePage from '@/pages/admin/UserManagePage';
import type { PagedResponse, User } from '@/types';

const mocks = vi.hoisted(() => ({
  fetchUsers: vi.fn(),
  updateUserAdmin: vi.fn(),
}));

vi.mock('@/api/admin', () => ({
  fetchUsers: (...args: unknown[]) => mocks.fetchUsers(...args),
  updateUserAdmin: (...args: unknown[]) => mocks.updateUserAdmin(...args),
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

function user(id: number, nickname: string): User {
  return {
    id,
    email: `${nickname.toLowerCase()}@example.com`,
    nickname,
    role: 'USER',
    phonePersonal: null,
    phoneCompany: null,
    job: null,
    companyName: null,
    userType: 'INDIVIDUAL',
    isVerified: true,
    createdAt: '2026-07-16T00:00:00',
  };
}

function page(entry: User): PagedResponse<User> {
  return {
    dataList: [entry],
    pageInfo: { page: 1, size: 20, total: 1, start: 1, end: 1, prev: false, next: false },
  };
}

function renderStrictPage() {
  return render(
    <StrictMode>
      <UserManagePage />
    </StrictMode>,
  );
}

describe('UserManagePage request fencing', () => {
  beforeEach(() => {
    mocks.fetchUsers.mockReset();
    mocks.updateUserAdmin.mockReset();
  });

  it('ignores an old successful list response after a newer search', async () => {
    const first = deferred<PagedResponse<User>>();
    const second = deferred<PagedResponse<User>>();
    mocks.fetchUsers.mockReturnValueOnce(first.promise).mockReturnValueOnce(second.promise);

    renderStrictPage();
    await waitFor(() => expect(mocks.fetchUsers).toHaveBeenCalledTimes(2));
    const firstSignal = mocks.fetchUsers.mock.calls[0][1] as AbortSignal;
    expect(firstSignal.aborted).toBe(true);

    await act(async () => second.resolve(page(user(2, 'CurrentUser'))));
    expect(await screen.findByText('CurrentUser')).toBeInTheDocument();

    await act(async () => first.resolve(page(user(1, 'OldUser'))));
    expect(screen.getByText('CurrentUser')).toBeInTheDocument();
    expect(screen.queryByText('OldUser')).not.toBeInTheDocument();
  });

  it('ignores an old failed list response after a newer search succeeds', async () => {
    const first = deferred<PagedResponse<User>>();
    const second = deferred<PagedResponse<User>>();
    mocks.fetchUsers.mockReturnValueOnce(first.promise).mockReturnValueOnce(second.promise);

    renderStrictPage();
    await waitFor(() => expect(mocks.fetchUsers).toHaveBeenCalledTimes(2));
    await act(async () => second.resolve(page(user(2, 'CurrentAfterFailure'))));
    expect(await screen.findByText('CurrentAfterFailure')).toBeInTheDocument();

    await act(async () => first.reject(new Error('old failure')));
    expect(screen.getByText('CurrentAfterFailure')).toBeInTheDocument();
    expect(screen.queryByText('Failed to load users')).not.toBeInTheDocument();
  });
});

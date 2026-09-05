import { StrictMode } from 'react';
import { act, fireEvent, render, screen, waitFor, within } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import UserManagePage from '@/pages/admin/UserManagePage';
import styles from '@/pages/admin/UserManagePage.module.css';
import ProtectedRoute from '@/router/ProtectedRoute';
import { useAuthStore } from '@/store/authStore';
import type { AdminAssignableRole, AdminUserDetail, AdminUserListItem } from '@/api/admin';
import type { PagedResponse, User } from '@/types';

const mocks = vi.hoisted(() => ({
  fetchUserDetail: vi.fn(),
  fetchUsers: vi.fn(),
  fetchMe: vi.fn(),
  updateUserAdmin: vi.fn(),
}));

vi.mock('@/api/admin', () => ({
  fetchUserDetail: (...args: unknown[]) => mocks.fetchUserDetail(...args),
  fetchUsers: (...args: unknown[]) => mocks.fetchUsers(...args),
  updateUserAdmin: (...args: unknown[]) => mocks.updateUserAdmin(...args),
}));

vi.mock('@/api/auth', () => ({
  fetchMe: (...args: unknown[]) => mocks.fetchMe(...args),
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

function adminUser(
  id: number,
  nickname: string,
  role: AdminAssignableRole = 'USER',
): AdminUserListItem {
  return {
    id,
    email: `${nickname.toLowerCase()}@example.com`,
    nickname,
    role,
    userType: 'INDIVIDUAL',
    isVerified: true,
    createdAt: '2026-07-16T00:00:00',
  };
}

function adminUserDetail(item: AdminUserListItem): AdminUserDetail {
  return {
    ...item,
    phonePersonal: null,
    phoneCompany: null,
    job: null,
    companyName: null,
  };
}

function sessionUser(item: AdminUserListItem): User {
  return adminUserDetail(item);
}

function page(...entries: AdminUserListItem[]): PagedResponse<AdminUserListItem> {
  return {
    dataList: entries,
    pageInfo: {
      page: 1,
      size: 20,
      total: entries.length,
      start: entries.length === 0 ? 0 : 1,
      end: entries.length,
      prev: false,
      next: false,
    },
  };
}

const currentAdminRow = adminUser(99, 'CurrentAdmin', 'ADMIN');
const currentAdmin = sessionUser(currentAdminRow);

function setSession(sessionUser: User) {
  localStorage.setItem('accessToken', 'access-token');
  localStorage.setItem('user', JSON.stringify(sessionUser));
  useAuthStore.setState({
    user: sessionUser,
    accessToken: 'access-token',
    role: sessionUser.role,
  });
}

function renderPage() {
  return render(<UserManagePage />);
}

function renderStrictPage() {
  return render(
    <StrictMode>
      <UserManagePage />
    </StrictMode>,
  );
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

describe('UserManagePage request fencing', () => {
  beforeEach(() => {
    localStorage.clear();
    sessionStorage.clear();
    useAuthStore.setState({ user: null, accessToken: null, role: 'GUEST' });
    mocks.fetchUserDetail.mockReset();
    mocks.fetchUsers.mockReset();
    mocks.fetchMe.mockReset();
    mocks.updateUserAdmin.mockReset();
  });

  it('ignores an old successful list response after a newer search', async () => {
    const first = deferred<PagedResponse<AdminUserListItem>>();
    const second = deferred<PagedResponse<AdminUserListItem>>();
    mocks.fetchUsers.mockReturnValueOnce(first.promise).mockReturnValueOnce(second.promise);

    renderStrictPage();
    await waitFor(() => expect(mocks.fetchUsers).toHaveBeenCalledTimes(2));
    const firstSignal = mocks.fetchUsers.mock.calls[0][1] as AbortSignal;
    expect(firstSignal.aborted).toBe(true);

    await act(async () => second.resolve(page(adminUser(2, 'CurrentUser'))));
    expect(await screen.findByText('CurrentUser')).toBeInTheDocument();

    await act(async () => first.resolve(page(adminUser(1, 'OldUser'))));
    expect(screen.getByText('CurrentUser')).toBeInTheDocument();
    expect(screen.queryByText('OldUser')).not.toBeInTheDocument();
  });

  it('ignores an old failed list response after a newer search succeeds', async () => {
    const first = deferred<PagedResponse<AdminUserListItem>>();
    const second = deferred<PagedResponse<AdminUserListItem>>();
    mocks.fetchUsers.mockReturnValueOnce(first.promise).mockReturnValueOnce(second.promise);

    renderStrictPage();
    await waitFor(() => expect(mocks.fetchUsers).toHaveBeenCalledTimes(2));
    await act(async () => second.resolve(page(adminUser(2, 'CurrentAfterFailure'))));
    expect(await screen.findByText('CurrentAfterFailure')).toBeInTheDocument();

    await act(async () => first.reject(new Error('old failure')));
    expect(screen.getByText('CurrentAfterFailure')).toBeInTheDocument();
    expect(screen.queryByText('Failed to load users')).not.toBeInTheDocument();
  });
});

describe('UserManagePage administrator role safety', () => {
  beforeEach(() => {
    localStorage.clear();
    sessionStorage.clear();
    useAuthStore.setState({ user: null, accessToken: null, role: 'GUEST' });
    mocks.fetchUserDetail.mockReset();
    mocks.fetchUsers.mockReset();
    mocks.fetchMe.mockReset();
    mocks.updateUserAdmin.mockReset();
  });

  it('disables the current administrator role control and explains why', async () => {
    setSession(currentAdmin);
    mocks.fetchUsers.mockResolvedValue(page(currentAdminRow));

    renderPage();

    expect(
      await screen.findByRole('combobox', { name: 'Change role for CurrentAdmin' }),
    ).toBeDisabled();
    expect(
      screen.getByText('Your own administrator role cannot be changed here.'),
    ).toBeInTheDocument();
    expect(screen.queryByRole('option', { name: 'GUEST' })).not.toBeInTheDocument();
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
  });

  it.each([
    ['SELF_ADMIN_DEMOTION_FORBIDDEN', 403, 'You cannot remove your own administrator role.'],
    ['LAST_ADMIN_REQUIRED', 409, 'At least one active administrator must remain.'],
    [
      'ADMIN_ROLE_REQUIRED',
      403,
      'Your administrator access has changed. Your current role is being refreshed.',
    ],
  ])(
    'keeps the list and maps %s into row and modal feedback',
    async (errorCode, status, message) => {
      const targetAdmin = adminUser(2, 'TargetAdmin', 'ADMIN');
      setSession(currentAdmin);
      mocks.fetchUsers.mockResolvedValue(page(targetAdmin));
      mocks.updateUserAdmin.mockRejectedValue({
        response: { status, data: { errorCode } },
      });
      mocks.fetchMe.mockResolvedValue(currentAdmin);
      renderPage();

      fireEvent.change(
        await screen.findByRole('combobox', { name: 'Change role for TargetAdmin' }),
        { target: { value: 'USER' } },
      );
      const dialog = await screen.findByRole('dialog', { name: 'Confirm Role Change' });
      expect(
        within(dialog).getByText(
          'Administrator access ends immediately and the target must sign in again.',
        ),
      ).toBeInTheDocument();
      fireEvent.change(within(dialog).getByLabelText('Operator reason'), {
        target: { value: '  Access review ticket 14  ' },
      });
      fireEvent.click(within(dialog).getByRole('button', { name: 'Confirm' }));

      await waitFor(() => expect(screen.getAllByText(message)).toHaveLength(2));
      expect(mocks.updateUserAdmin).toHaveBeenCalledWith(2, {
        role: 'USER',
        reason: 'Access review ticket 14',
      });
      expect(screen.getByRole('heading', { name: 'User Management' })).toBeInTheDocument();
      expect(screen.getAllByText('TargetAdmin')).not.toHaveLength(0);
      expect(screen.getByRole('dialog', { name: 'Confirm Role Change' })).toBeInTheDocument();
      expect(mocks.updateUserAdmin).toHaveBeenCalledTimes(1);
      if (errorCode === 'ADMIN_ROLE_REQUIRED') {
        await waitFor(() => expect(mocks.fetchMe).toHaveBeenCalledTimes(1));
      } else {
        expect(mocks.fetchMe).not.toHaveBeenCalled();
      }
    },
  );

  it('renders only the bounded read-only detail and ignores a retired detail response', async () => {
    const firstTarget = adminUser(2, 'FirstTarget');
    const secondTarget = adminUser(3, 'SecondTarget');
    const firstDetail = deferred<AdminUserDetail>();
    const secondDetail = deferred<AdminUserDetail>();
    setSession(currentAdmin);
    mocks.fetchUsers.mockResolvedValue(page(firstTarget, secondTarget));
    mocks.fetchUserDetail
      .mockReturnValueOnce(firstDetail.promise)
      .mockReturnValueOnce(secondDetail.promise);

    renderPage();

    fireEvent.click(await screen.findByRole('button', { name: 'View details for FirstTarget' }));
    const firstSignal = mocks.fetchUserDetail.mock.calls[0][1] as AbortSignal;
    fireEvent.click(screen.getByRole('button', { name: 'Close user details' }));
    expect(firstSignal.aborted).toBe(true);

    fireEvent.click(screen.getByRole('button', { name: 'View details for SecondTarget' }));
    await act(async () =>
      secondDetail.resolve({
        ...adminUserDetail(secondTarget),
        phonePersonal: '010-2222-3333',
        phoneCompany: '02-222-3333',
        job: null,
        companyName: 'Second Company',
        userType: 'BUSINESS',
      }),
    );

    const dialog = await screen.findByRole('dialog', { name: 'User Details' });
    expect(within(dialog).getByText('SecondTarget')).toBeInTheDocument();
    expect(within(dialog).getByText('010-2222-3333')).toBeInTheDocument();
    expect(within(dialog).getByText('Second Company')).toBeInTheDocument();
    expect(within(dialog).getByText('Company name or industry')).toBeInTheDocument();
    expect(within(dialog).queryByText('Job')).not.toBeInTheDocument();
    expect(within(dialog).queryByText(/password|token/i)).not.toBeInTheDocument();

    await act(async () => firstDetail.resolve(adminUserDetail(firstTarget)));
    expect(within(dialog).getByText('SecondTarget')).toBeInTheDocument();
    expect(within(dialog).queryByText('FirstTarget')).not.toBeInTheDocument();
    expect(mocks.fetchUserDetail).toHaveBeenCalledTimes(2);
  });

  it('keeps the mobile detail action visible and supports open, retry, and close', async () => {
    const target = adminUser(2, 'RetryTarget');
    Object.defineProperty(window, 'innerWidth', { configurable: true, value: 767 });
    setSession(currentAdmin);
    mocks.fetchUsers.mockResolvedValue(page(target));
    mocks.fetchUserDetail
      .mockRejectedValueOnce(new Error('unavailable'))
      .mockResolvedValueOnce(adminUserDetail(target));
    renderPage();

    const detailAction = await screen.findByRole('button', {
      name: 'View details for RetryTarget',
    });
    const row = detailAction.closest('tr');
    expect(row).not.toBeNull();
    expect(row!.children[0]).toHaveClass(styles.mobileHidden);
    expect(detailAction.closest('td')).not.toHaveClass(styles.mobileHidden);
    expect(row!.children[5]).toHaveClass(styles.mobileHidden);
    expect(row!.children[6]).toHaveClass(styles.mobileHidden);

    fireEvent.click(detailAction);
    const dialog = await screen.findByRole('dialog', { name: 'User Details' });
    expect(await within(dialog).findByRole('alert')).toHaveTextContent(
      'Failed to load user details. Try again.',
    );
    fireEvent.click(within(dialog).getByRole('button', { name: 'Retry' }));

    expect(await within(dialog).findByText('RetryTarget')).toBeInTheDocument();
    expect(mocks.fetchUserDetail).toHaveBeenCalledTimes(2);
    fireEvent.click(within(dialog).getByRole('button', { name: 'Close user details' }));
    expect(screen.queryByRole('dialog', { name: 'User Details' })).not.toBeInTheDocument();
  });

  it('refreshes /users/me once after a successful role mutation', async () => {
    const targetAdmin = adminUser(2, 'TargetAdmin', 'ADMIN');
    const demotedTarget = adminUserDetail({ ...targetAdmin, role: 'USER' });
    setSession(currentAdmin);
    mocks.fetchUsers.mockResolvedValue(page(targetAdmin));
    mocks.updateUserAdmin.mockResolvedValue(demotedTarget);
    mocks.fetchMe.mockResolvedValue(currentAdmin);
    renderPage();

    fireEvent.change(await screen.findByRole('combobox', { name: 'Change role for TargetAdmin' }), {
      target: { value: 'USER' },
    });
    fireEvent.change(within(await screen.findByRole('dialog')).getByLabelText('Operator reason'), {
      target: { value: 'Role ownership changed' },
    });
    fireEvent.click(
      within(await screen.findByRole('dialog')).getByRole('button', { name: 'Confirm' }),
    );

    await waitFor(() => expect(mocks.fetchMe).toHaveBeenCalledTimes(1));
    await waitFor(() => expect(screen.queryByRole('dialog')).not.toBeInTheDocument());
    expect(screen.getByRole('combobox', { name: 'Change role for TargetAdmin' })).toHaveValue(
      'USER',
    );
    expect(useAuthStore.getState().role).toBe('ADMIN');
  });

  it('keeps a pending role result owned by its immutable target and modal generation', async () => {
    const targetA = adminUser(2, 'TargetA');
    const targetB = adminUser(3, 'TargetB');
    const pendingMutation = deferred<AdminUserDetail>();
    setSession(currentAdmin);
    mocks.fetchUsers.mockResolvedValue(page(targetA, targetB));
    mocks.updateUserAdmin.mockReturnValueOnce(pendingMutation.promise);
    mocks.fetchMe.mockResolvedValue(currentAdmin);
    renderPage();

    fireEvent.change(await screen.findByRole('combobox', { name: 'Change role for TargetA' }), {
      target: { value: 'ADMIN' },
    });
    let dialog = screen.getByRole('dialog', { name: 'Confirm Role Change' });
    fireEvent.change(within(dialog).getByLabelText('Operator reason'), {
      target: { value: 'Target A change' },
    });
    fireEvent.click(within(dialog).getByRole('button', { name: 'Confirm' }));

    expect(dialog).toHaveAttribute('aria-busy', 'true');
    expect(within(dialog).getByRole('button', { name: '닫기' })).toBeDisabled();
    expect(within(dialog).getByRole('button', { name: 'Cancel' })).toBeDisabled();
    fireEvent.keyDown(document, { key: 'Escape' });
    fireEvent.click(dialog.parentElement!);
    fireEvent.click(within(dialog).getByRole('button', { name: '닫기' }));
    fireEvent.click(within(dialog).getByRole('button', { name: 'Cancel' }));
    expect(screen.getByRole('dialog', { name: 'Confirm Role Change' })).toBeInTheDocument();

    fireEvent.change(screen.getByRole('combobox', { name: 'Change role for TargetB' }), {
      target: { value: 'ADMIN' },
    });
    dialog = screen.getByRole('dialog', { name: 'Confirm Role Change' });
    expect(within(dialog).getByText('TargetB')).toBeInTheDocument();

    await act(async () => pendingMutation.resolve(adminUserDetail({ ...targetA, role: 'ADMIN' })));

    dialog = screen.getByRole('dialog', { name: 'Confirm Role Change' });
    expect(within(dialog).getByText('TargetB')).toBeInTheDocument();
    expect(within(dialog).queryByText('TargetA')).not.toBeInTheDocument();
    expect(mocks.updateUserAdmin).toHaveBeenCalledTimes(1);
    expect(screen.getByRole('combobox', { name: 'Change role for TargetA' })).toHaveValue('ADMIN');
  });

  it('refreshes a typed stale-authority rejection once and lets the guard remove ADMIN access', async () => {
    const targetAdmin = adminUser(2, 'TargetAdmin', 'ADMIN');
    const demotedCurrentUser: User = { ...currentAdmin, role: 'USER' };
    setSession(currentAdmin);
    mocks.fetchUsers.mockResolvedValue(page(targetAdmin));
    mocks.updateUserAdmin.mockRejectedValue({
      response: { status: 403, data: { errorCode: 'ADMIN_ROLE_REQUIRED' } },
    });
    mocks.fetchMe.mockResolvedValue(demotedCurrentUser);
    renderProtectedPage();

    fireEvent.change(await screen.findByRole('combobox', { name: 'Change role for TargetAdmin' }), {
      target: { value: 'USER' },
    });
    const dialog = await screen.findByRole('dialog', { name: 'Confirm Role Change' });
    fireEvent.change(within(dialog).getByLabelText('Operator reason'), {
      target: { value: 'Stale authority check' },
    });
    fireEvent.click(within(dialog).getByRole('button', { name: 'Confirm' }));

    expect(await screen.findByText('Home Page')).toBeInTheDocument();
    expect(mocks.updateUserAdmin).toHaveBeenCalledTimes(1);
    expect(mocks.fetchMe).toHaveBeenCalledTimes(1);
    expect(useAuthStore.getState().role).toBe('USER');
  });

  it('does not refresh authority when the stale-authority code is not a 403', async () => {
    const targetAdmin = adminUser(2, 'TargetAdmin', 'ADMIN');
    setSession(currentAdmin);
    mocks.fetchUsers.mockResolvedValue(page(targetAdmin));
    mocks.updateUserAdmin.mockRejectedValue({
      response: { status: 409, data: { errorCode: 'ADMIN_ROLE_REQUIRED' } },
    });
    renderPage();

    fireEvent.change(await screen.findByRole('combobox', { name: 'Change role for TargetAdmin' }), {
      target: { value: 'USER' },
    });
    const dialog = await screen.findByRole('dialog', { name: 'Confirm Role Change' });
    fireEvent.change(within(dialog).getByLabelText('Operator reason'), {
      target: { value: 'Unexpected status check' },
    });
    fireEvent.click(within(dialog).getByRole('button', { name: 'Confirm' }));

    await waitFor(() => expect(mocks.updateUserAdmin).toHaveBeenCalledTimes(1));
    expect(mocks.fetchMe).not.toHaveBeenCalled();
  });

  it('requires a trimmed operator reason before sending a role mutation', async () => {
    const targetUser = adminUser(2, 'TargetUser');
    setSession(currentAdmin);
    mocks.fetchUsers.mockResolvedValue(page(targetUser));
    renderPage();

    fireEvent.change(await screen.findByRole('combobox', { name: 'Change role for TargetUser' }), {
      target: { value: 'ADMIN' },
    });
    const dialog = await screen.findByRole('dialog', { name: 'Confirm Role Change' });
    fireEvent.change(within(dialog).getByLabelText('Operator reason'), {
      target: { value: '   ' },
    });
    fireEvent.click(within(dialog).getByRole('button', { name: 'Confirm' }));

    expect(
      within(dialog).getByText('Enter an operator reason for this role change.'),
    ).toBeInTheDocument();
    expect(mocks.updateUserAdmin).not.toHaveBeenCalled();
  });

  it('refreshes a current-user row mismatch and lets ProtectedRoute reevaluate', async () => {
    const demotedCurrentUser: AdminUserListItem = { ...currentAdminRow, role: 'USER' };
    setSession(currentAdmin);
    mocks.fetchUsers.mockResolvedValue(page(demotedCurrentUser));
    mocks.fetchMe.mockResolvedValue(demotedCurrentUser);

    renderProtectedPage();

    expect(await screen.findByText('Home Page')).toBeInTheDocument();
    expect(useAuthStore.getState().role).toBe('USER');
    expect(mocks.fetchUsers).toHaveBeenCalledTimes(1);
    expect(mocks.fetchMe).toHaveBeenCalledTimes(1);
  });
});

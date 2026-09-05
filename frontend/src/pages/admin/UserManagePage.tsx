/** Screen K-1: User management */
import { useEffect, useState, useCallback, useRef } from 'react';
import {
  fetchUserDetail,
  fetchUsers,
  updateUserAdmin,
  type AdminAssignableRole,
  type AdminUserDetail,
  type AdminUserListItem,
} from '@/api/admin';
import { classifyLoadError, getApiErrorCode } from '@/api/loadError';
import { useAuthStore } from '@/store/authStore';
import type { PageInfo } from '@/types';
import { formatDate } from '@/utils/format';
import Modal from '@/components/ui/Modal';
import Button from '@/components/ui/Button';
import Pagination from '@/components/ui/Pagination';
import styles from './UserManagePage.module.css';

const ROLES: AdminAssignableRole[] = ['USER', 'ADMIN'];
const SELF_DEMOTION_MESSAGE = 'You cannot remove your own administrator role.';
const ROLE_REASON_REQUIRED_MESSAGE = 'Enter an operator reason for this role change.';
const ROLE_SYNC_FAILURE_MESSAGE =
  'Your current role could not be refreshed. Reload the page before making another change.';

const ROLE_CHANGE_ERROR_MESSAGES: Record<string, string> = {
  SELF_ADMIN_DEMOTION_FORBIDDEN: SELF_DEMOTION_MESSAGE,
  LAST_ADMIN_REQUIRED: 'At least one active administrator must remain.',
  ADMIN_ROLE_REQUIRED:
    'Your administrator access has changed. Your current role is being refreshed.',
  ADMIN_OPERATION_REASON_REQUIRED: ROLE_REASON_REQUIRED_MESSAGE,
};

function roleChangeErrorMessage(error: unknown): string {
  const errorCode = getApiErrorCode(error);
  return errorCode
    ? (ROLE_CHANGE_ERROR_MESSAGES[errorCode] ?? 'The role could not be changed. Try again.')
    : 'The role could not be changed. Try again.';
}

export default function UserManagePage() {
  const [users, setUsers] = useState<AdminUserListItem[]>([]);
  const [pageInfo, setPageInfo] = useState<PageInfo | null>(null);
  const [page, setPage] = useState(1);
  const [keyword, setKeyword] = useState('');
  const [searchInput, setSearchInput] = useState('');
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState<string | null>(null);
  const loadGenerationRef = useRef(0);
  const currentUserID = useAuthStore((state) => state.user?.id);
  const refreshCurrentUser = useAuthStore((state) => state.refreshCurrentUser);

  /* Role change modal */
  const [roleTarget, setRoleTarget] = useState<{
    user: AdminUserListItem;
    newRole: AdminAssignableRole;
    generation: number;
  } | null>(null);
  const [roleLoading, setRoleLoading] = useState(false);
  const roleGenerationRef = useRef(0);
  const roleMutationPendingRef = useRef(false);
  const [roleReason, setRoleReason] = useState('');
  const [roleReasonError, setRoleReasonError] = useState<string | null>(null);
  const [roleFeedback, setRoleFeedback] = useState<{
    userID: number;
    message: string;
    generation: number;
  } | null>(null);

  /* Read-only User detail modal */
  const [detailTarget, setDetailTarget] = useState<AdminUserListItem | null>(null);
  const [detail, setDetail] = useState<AdminUserDetail | null>(null);
  const [detailLoading, setDetailLoading] = useState(false);
  const [detailError, setDetailError] = useState<string | null>(null);
  const detailGenerationRef = useRef(0);
  const detailControllerRef = useRef<AbortController | null>(null);

  const refreshRoleSnapshot = useCallback(async () => {
    try {
      return await refreshCurrentUser();
    } catch {
      return null;
    }
  }, [refreshCurrentUser]);

  const loadUsers = useCallback(
    (signal?: AbortSignal) => {
      const generation = ++loadGenerationRef.current;
      setLoading(true);
      setLoadError(null);
      fetchUsers({ page, size: 20, keyword: keyword || undefined }, signal)
        .then(async (result) => {
          if (loadGenerationRef.current === generation) {
            setUsers(result.dataList);
            setPageInfo(result.pageInfo);
            const sessionUser = useAuthStore.getState().user;
            const listedCurrentUser = sessionUser
              ? result.dataList.find((user) => user.id === sessionUser.id)
              : undefined;
            if (listedCurrentUser && listedCurrentUser.role !== sessionUser?.role) {
              const refreshedUser = await refreshRoleSnapshot();
              if (!refreshedUser && loadGenerationRef.current === generation) {
                setRoleFeedback({
                  userID: listedCurrentUser.id,
                  message: ROLE_SYNC_FAILURE_MESSAGE,
                  generation: roleGenerationRef.current,
                });
              }
            }
          }
        })
        .catch(async (requestError: unknown) => {
          if (loadGenerationRef.current !== generation) return;
          const errorKind = classifyLoadError(requestError);
          if (errorKind === 'cancelled') return;
          if (errorKind === 'forbidden' && useAuthStore.getState().role !== 'ADMIN') return;
          if (loadGenerationRef.current === generation) setLoadError('Failed to load users');
        })
        .finally(() => {
          if (loadGenerationRef.current === generation) setLoading(false);
        });
    },
    [page, keyword, refreshRoleSnapshot],
  );

  useEffect(() => {
    const controller = new AbortController();
    loadUsers(controller.signal);
    return () => {
      controller.abort();
      loadGenerationRef.current += 1;
    };
  }, [loadUsers]);

  useEffect(
    () => () => {
      detailControllerRef.current?.abort();
      detailGenerationRef.current += 1;
    },
    [],
  );

  const handleSearch = () => {
    setPage(1);
    setKeyword(searchInput.trim());
  };

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') handleSearch();
  };

  const handleRoleChange = (user: AdminUserListItem, newRole: AdminAssignableRole) => {
    if (newRole === user.role) return;
    if (user.id === currentUserID && user.role === 'ADMIN' && newRole === 'USER') {
      setRoleFeedback({
        userID: user.id,
        message: SELF_DEMOTION_MESSAGE,
        generation: roleGenerationRef.current,
      });
      return;
    }
    const generation = ++roleGenerationRef.current;
    setRoleFeedback(null);
    setRoleReason('');
    setRoleReasonError(null);
    setRoleTarget({ user, newRole, generation });
  };

  const closeRoleChange = () => {
    if (roleMutationPendingRef.current) return;
    roleGenerationRef.current += 1;
    setRoleTarget(null);
    setRoleReason('');
    setRoleReasonError(null);
  };

  const closeUserDetail = () => {
    detailControllerRef.current?.abort();
    detailControllerRef.current = null;
    detailGenerationRef.current += 1;
    setDetailTarget(null);
    setDetail(null);
    setDetailError(null);
    setDetailLoading(false);
  };

  const openUserDetail = (user: AdminUserListItem) => {
    detailControllerRef.current?.abort();
    const controller = new AbortController();
    const generation = ++detailGenerationRef.current;
    detailControllerRef.current = controller;
    setDetailTarget(user);
    setDetail(null);
    setDetailError(null);
    setDetailLoading(true);

    fetchUserDetail(user.id, controller.signal)
      .then((result) => {
        if (controller.signal.aborted || detailGenerationRef.current !== generation) return;
        setDetail(result);
      })
      .catch(() => {
        if (controller.signal.aborted || detailGenerationRef.current !== generation) return;
        setDetailError('Failed to load user details. Try again.');
      })
      .finally(() => {
        if (detailGenerationRef.current === generation) setDetailLoading(false);
        if (detailControllerRef.current === controller) detailControllerRef.current = null;
      });
  };

  const confirmRoleChange = async () => {
    if (!roleTarget || roleMutationPendingRef.current) return;
    const operation = roleTarget;
    const normalizedReason = roleReason.trim();
    if (!normalizedReason) {
      setRoleReasonError(ROLE_REASON_REQUIRED_MESSAGE);
      return;
    }
    roleMutationPendingRef.current = true;
    setRoleLoading(true);
    const ownsModal = () => roleGenerationRef.current === operation.generation;
    try {
      const updatedUser = await updateUserAdmin(operation.user.id, {
        role: operation.newRole,
        reason: normalizedReason,
      });
      setUsers((currentUsers) =>
        currentUsers.map((user) => (user.id === updatedUser.id ? updatedUser : user)),
      );
      setRoleFeedback((current) => (current?.userID === updatedUser.id ? null : current));
      if (ownsModal()) {
        setRoleTarget(null);
        setRoleReason('');
        setRoleReasonError(null);
      }
      const refreshedUser = await refreshRoleSnapshot();
      if (!refreshedUser) {
        setRoleFeedback({
          userID: updatedUser.id,
          message: ROLE_SYNC_FAILURE_MESSAGE,
          generation: operation.generation,
        });
      }
    } catch (roleChangeError: unknown) {
      const errorCode = getApiErrorCode(roleChangeError);
      setRoleFeedback({
        userID: operation.user.id,
        message: roleChangeErrorMessage(roleChangeError),
        generation: operation.generation,
      });
      if (
        errorCode === 'ADMIN_ROLE_REQUIRED' &&
        classifyLoadError(roleChangeError) === 'forbidden'
      ) {
        const refreshedUser = await refreshRoleSnapshot();
        if (!refreshedUser) {
          setRoleFeedback({
            userID: operation.user.id,
            message: ROLE_SYNC_FAILURE_MESSAGE,
            generation: operation.generation,
          });
        }
      }
    } finally {
      roleMutationPendingRef.current = false;
      setRoleLoading(false);
    }
  };

  if (loading) {
    return (
      <div className={styles.page}>
        <div className={styles.loading}>Loading...</div>
      </div>
    );
  }

  if (loadError) {
    return (
      <div className={styles.page}>
        <div className={styles.error}>{loadError}</div>
      </div>
    );
  }

  return (
    <div className={styles.page}>
      <h1 className={styles.title}>User Management</h1>

      {/* Search */}
      <div className={styles.searchBar}>
        <input
          className={styles.searchInput}
          placeholder="Search by email or nickname..."
          value={searchInput}
          onChange={(e) => setSearchInput(e.target.value)}
          onKeyDown={handleKeyDown}
        />
        <Button size="sm" onClick={handleSearch}>
          Search
        </Button>
      </div>

      {/* Table */}
      <div className={styles.tableWrap}>
        <table className={styles.table}>
          <thead>
            <tr>
              <th className={styles.mobileHidden}>ID</th>
              <th>Email</th>
              <th>Nickname</th>
              <th>Role</th>
              <th>Details</th>
              <th className={styles.mobileHidden}>Change Role</th>
              <th className={styles.mobileHidden}>Joined</th>
            </tr>
          </thead>
          <tbody>
            {users.length === 0 && (
              <tr>
                <td colSpan={7} className={styles.empty}>
                  No users found.
                </td>
              </tr>
            )}
            {users.map((u) => {
              const isCurrentAdmin = u.id === currentUserID && u.role === 'ADMIN';
              const feedback = roleFeedback?.userID === u.id ? roleFeedback.message : null;
              const roleControlHintID = `user-role-hint-${u.id}`;
              return (
                <tr key={u.id} className={styles.row}>
                  <td className={styles.mobileHidden}>{u.id}</td>
                  <td>{u.email}</td>
                  <td>{u.nickname}</td>
                  <td>
                    <span className={styles.roleBadge}>{u.role}</span>
                  </td>
                  <td>
                    <Button
                      variant="outline"
                      size="sm"
                      aria-label={`View details for ${u.nickname}`}
                      onClick={() => openUserDetail(u)}
                    >
                      View
                    </Button>
                  </td>
                  <td className={styles.mobileHidden}>
                    <div className={styles.roleControl}>
                      <select
                        className={styles.roleSelect}
                        value={u.role}
                        disabled={isCurrentAdmin}
                        aria-label={`Change role for ${u.nickname}`}
                        aria-describedby={isCurrentAdmin ? roleControlHintID : undefined}
                        onChange={(e) => handleRoleChange(u, e.target.value as AdminAssignableRole)}
                      >
                        {ROLES.map((r) => (
                          <option key={r} value={r}>
                            {r}
                          </option>
                        ))}
                      </select>
                      {isCurrentAdmin && (
                        <span className={styles.roleHint} id={roleControlHintID}>
                          Your own administrator role cannot be changed here.
                        </span>
                      )}
                      {feedback && (
                        <span className={styles.roleFeedback} role="alert">
                          {feedback}
                        </span>
                      )}
                    </div>
                  </td>
                  <td className={styles.mobileHidden}>{formatDate(u.createdAt)}</td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>

      {pageInfo && pageInfo.total > pageInfo.size && (
        <Pagination pageInfo={pageInfo} currentPage={page} onPageChange={setPage} />
      )}

      <Modal open={detailTarget !== null} onClose={closeUserDetail} title="User Details">
        <div className={styles.detailBody}>
          {detailLoading ? (
            <div className={styles.detailState}>Loading user details...</div>
          ) : detailError ? (
            <div className={styles.detailState} role="alert">
              <span>{detailError}</span>
              {detailTarget && (
                <Button size="sm" variant="outline" onClick={() => openUserDetail(detailTarget)}>
                  Retry
                </Button>
              )}
            </div>
          ) : detail ? (
            <dl className={styles.detailGrid}>
              <dt>ID</dt>
              <dd>{detail.id}</dd>
              <dt>Nickname</dt>
              <dd>{detail.nickname}</dd>
              <dt>Email</dt>
              <dd>{detail.email}</dd>
              <dt>Personal phone</dt>
              <dd>{detail.phonePersonal ?? 'Not provided'}</dd>
              <dt>Company phone</dt>
              <dd>{detail.phoneCompany ?? 'Not provided'}</dd>
              {detail.userType === 'BUSINESS' ? (
                <>
                  <dt>Company name or industry</dt>
                  <dd>{detail.companyName ?? 'Not provided'}</dd>
                </>
              ) : (
                <>
                  <dt>Job</dt>
                  <dd>{detail.job ?? 'Not provided'}</dd>
                </>
              )}
              <dt>User type</dt>
              <dd>{detail.userType}</dd>
              <dt>Role</dt>
              <dd>{detail.role}</dd>
              <dt>Verification</dt>
              <dd>{detail.isVerified ? 'Verified' : 'Not verified'}</dd>
              <dt>Joined</dt>
              <dd>{formatDate(detail.createdAt)}</dd>
            </dl>
          ) : null}
        </div>
        <div className={styles.modalActions}>
          <Button
            variant="ghost"
            size="sm"
            aria-label="Close user details"
            onClick={closeUserDetail}
          >
            Close
          </Button>
        </div>
      </Modal>

      {/* Role change confirm modal */}
      <Modal
        open={roleTarget !== null}
        onClose={closeRoleChange}
        title="Confirm Role Change"
        busy={roleLoading}
      >
        <div className={styles.modalBody}>
          <p>
            Change <strong>{roleTarget?.user.nickname}</strong> role from{' '}
            <strong>{roleTarget?.user.role}</strong> to <strong>{roleTarget?.newRole}</strong>?
          </p>
          {roleTarget?.user.role === 'ADMIN' && roleTarget.newRole === 'USER' && (
            <p className={styles.roleImpact}>
              Administrator access ends immediately and the target must sign in again.
            </p>
          )}
          <label className={styles.reasonLabel} htmlFor="admin-role-change-reason">
            Operator reason
          </label>
          <textarea
            id="admin-role-change-reason"
            className={styles.reasonInput}
            value={roleReason}
            rows={4}
            maxLength={500}
            required
            aria-invalid={roleReasonError ? 'true' : undefined}
            aria-describedby={roleReasonError ? 'admin-role-change-reason-error' : undefined}
            onChange={(event) => {
              setRoleReason(event.target.value);
              if (event.target.value.trim()) setRoleReasonError(null);
            }}
          />
          <div className={styles.reasonCount}>{roleReason.length}/500</div>
          {roleReasonError && (
            <div className={styles.reasonError} id="admin-role-change-reason-error" role="alert">
              {roleReasonError}
            </div>
          )}
          {roleTarget &&
            roleFeedback?.userID === roleTarget.user.id &&
            roleFeedback.generation === roleTarget.generation && (
              <div className={styles.modalError} role="alert">
                {roleFeedback.message}
              </div>
            )}
        </div>
        <div className={styles.modalActions}>
          <Button variant="ghost" size="sm" disabled={roleLoading} onClick={closeRoleChange}>
            Cancel
          </Button>
          <Button size="sm" loading={roleLoading} onClick={confirmRoleChange}>
            Confirm
          </Button>
        </div>
      </Modal>
    </div>
  );
}

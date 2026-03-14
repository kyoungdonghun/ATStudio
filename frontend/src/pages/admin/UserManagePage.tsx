/** Screen K-1: User management */
import { useEffect, useState, useCallback } from 'react';
import { fetchUsers, updateUserAdmin } from '@/api/admin';
import type { User, PageInfo, UserRole } from '@/types';
import { formatDate } from '@/utils/format';
import Modal from '@/components/ui/Modal';
import Button from '@/components/ui/Button';
import Pagination from '@/components/ui/Pagination';
import styles from './UserManagePage.module.css';

const ROLES: UserRole[] = ['USER', 'ADMIN'];

export default function UserManagePage() {
  const [users, setUsers] = useState<User[]>([]);
  const [pageInfo, setPageInfo] = useState<PageInfo | null>(null);
  const [page, setPage] = useState(1);
  const [keyword, setKeyword] = useState('');
  const [searchInput, setSearchInput] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  /* Role change modal */
  const [roleTarget, setRoleTarget] = useState<{
    user: User;
    newRole: UserRole;
  } | null>(null);
  const [roleLoading, setRoleLoading] = useState(false);

  const loadUsers = useCallback(() => {
    setLoading(true);
    setError(null);
    fetchUsers({ page, size: 20, keyword: keyword || undefined })
      .then((result) => {
        setUsers(result.dataList);
        setPageInfo(result.pageInfo);
      })
      .catch(() => setError('Failed to load users'))
      .finally(() => setLoading(false));
  }, [page, keyword]);

  useEffect(() => {
    loadUsers();
  }, [loadUsers]);

  const handleSearch = () => {
    setPage(1);
    setKeyword(searchInput.trim());
  };

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') handleSearch();
  };

  const handleRoleChange = (user: User, newRole: UserRole) => {
    if (newRole === user.role) return;
    setRoleTarget({ user, newRole });
  };

  const confirmRoleChange = async () => {
    if (!roleTarget) return;
    setRoleLoading(true);
    try {
      await updateUserAdmin(roleTarget.user.id, { role: roleTarget.newRole });
      setRoleTarget(null);
      loadUsers();
    } catch {
      setError('Failed to update role');
    } finally {
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

  if (error) {
    return (
      <div className={styles.page}>
        <div className={styles.error}>{error}</div>
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
            <th>ID</th>
            <th>Email</th>
            <th>Nickname</th>
            <th>Role</th>
            <th>Change Role</th>
            <th>Joined</th>
          </tr>
        </thead>
        <tbody>
          {users.length === 0 && (
            <tr>
              <td colSpan={6} className={styles.empty}>
                No users found.
              </td>
            </tr>
          )}
          {users.map((u) => (
            <tr key={u.id} className={styles.row}>
              <td>{u.id}</td>
              <td>{u.email}</td>
              <td>{u.nickname}</td>
              <td>
                <span className={styles.roleBadge}>{u.role}</span>
              </td>
              <td>
                <select
                  className={styles.roleSelect}
                  value={u.role}
                  onChange={(e) =>
                    handleRoleChange(u, e.target.value as UserRole)
                  }
                >
                  {ROLES.map((r) => (
                    <option key={r} value={r}>
                      {r}
                    </option>
                  ))}
                </select>
              </td>
              <td>{formatDate(u.createdAt)}</td>
            </tr>
          ))}
        </tbody>
      </table>
      </div>

      {pageInfo && pageInfo.total > pageInfo.size && (
        <Pagination pageInfo={pageInfo} currentPage={page} onPageChange={setPage} />
      )}

      {/* Role change confirm modal */}
      <Modal
        open={roleTarget !== null}
        onClose={() => setRoleTarget(null)}
        title="Confirm Role Change"
      >
        <div className={styles.modalBody}>
          Change <strong>{roleTarget?.user.nickname}</strong> role from{' '}
          <strong>{roleTarget?.user.role}</strong> to{' '}
          <strong>{roleTarget?.newRole}</strong>?
        </div>
        <div className={styles.modalActions}>
          <Button variant="ghost" size="sm" onClick={() => setRoleTarget(null)}>
            Cancel
          </Button>
          <Button
            size="sm"
            loading={roleLoading}
            onClick={confirmRoleChange}
          >
            Confirm
          </Button>
        </div>
      </Modal>
    </div>
  );
}

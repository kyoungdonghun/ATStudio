/** Screen K-1: User management */
import { useEffect, useState, useCallback } from 'react';
import { fetchUsers, updateUserAdmin } from '@/api/admin';
import type { User, PageInfo, UserRole } from '@/types';
import Modal from '@/components/ui/Modal';
import Button from '@/components/ui/Button';
import styles from './UserManagePage.module.css';

const ROLES: UserRole[] = ['USER', 'CREATOR', 'ADMIN'];

function formatDate(iso: string): string {
  const d = new Date(iso);
  return `${d.getFullYear()}.${String(d.getMonth() + 1).padStart(2, '0')}.${String(d.getDate()).padStart(2, '0')}`;
}

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
    const params: Record<string, unknown> = { page, size: 20 };
    if (keyword) params.keyword = keyword;
    fetchUsers(params as Parameters<typeof fetchUsers>[0])
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

      {/* Pagination */}
      {pageInfo && pageInfo.total > pageInfo.size && (
        <nav className={styles.pagination}>
          <button
            className={styles.pageBtn}
            disabled={!pageInfo.prev}
            onClick={() => setPage(pageInfo.start - 1)}
          >
            &lsaquo;
          </button>
          {Array.from(
            { length: pageInfo.end - pageInfo.start + 1 },
            (_, i) => pageInfo.start + i,
          ).map((p) => (
            <button
              key={p}
              className={`${styles.pageBtn} ${p === page ? styles.pageBtnActive : ''}`}
              onClick={() => setPage(p)}
            >
              {p}
            </button>
          ))}
          <button
            className={styles.pageBtn}
            disabled={!pageInfo.next}
            onClick={() => setPage(pageInfo.end + 1)}
          >
            &rsaquo;
          </button>
        </nav>
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

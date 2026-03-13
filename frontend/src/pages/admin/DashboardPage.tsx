/** Screen 18: Admin stats dashboard */
import { useEffect, useState } from 'react';
import { fetchDashboardStats, type DashboardStats } from '@/api/admin';
import { formatDate } from '@/utils/format';
import styles from './DashboardPage.module.css';

export default function DashboardPage() {
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    setLoading(true);
    setError(null);
    fetchDashboardStats()
      .then(setStats)
      .catch(() => setError('Failed to load dashboard stats'))
      .finally(() => setLoading(false));
  }, []);

  if (loading) {
    return (
      <div className={styles.page}>
        <div className={styles.loading}>Loading...</div>
      </div>
    );
  }

  if (error || !stats) {
    return (
      <div className={styles.page}>
        <div className={styles.error}>{error ?? 'Unknown error'}</div>
      </div>
    );
  }

  return (
    <div className={styles.page}>
      <h1 className={styles.title}>Admin Dashboard</h1>

      <div className={styles.statsGrid}>
        <div className={styles.statCard}>
          <div className={styles.statLabel}>Total Users</div>
          <div className={styles.statValue}>
            {stats.totalUsers.toLocaleString()}
          </div>
        </div>
        <div className={styles.statCard}>
          <div className={styles.statLabel}>Total Tracks</div>
          <div className={styles.statValue}>
            {stats.totalTracks.toLocaleString()}
          </div>
        </div>
        <div className={styles.statCard}>
          <div className={styles.statLabel}>Subscribers</div>
          <div className={styles.statValue}>
            {stats.totalSubscribers.toLocaleString()}
          </div>
        </div>
      </div>

      <h2 className={styles.sectionTitle}>Recent Users</h2>
      <div className={styles.tableWrap}>
      <table className={styles.table}>
        <thead>
          <tr>
            <th>ID</th>
            <th>Email</th>
            <th>Nickname</th>
            <th>Role</th>
            <th>Joined</th>
          </tr>
        </thead>
        <tbody>
          {stats.recentUsers.length === 0 && (
            <tr>
              <td colSpan={5} className={styles.empty}>
                No users found.
              </td>
            </tr>
          )}
          {stats.recentUsers.map((u) => (
            <tr key={u.id} className={styles.row}>
              <td>{u.id}</td>
              <td>{u.email}</td>
              <td>{u.nickname}</td>
              <td>
                <span className={styles.roleBadge}>{u.role}</span>
              </td>
              <td>{formatDate(u.createdAt)}</td>
            </tr>
          ))}
        </tbody>
      </table>
      </div>
    </div>
  );
}

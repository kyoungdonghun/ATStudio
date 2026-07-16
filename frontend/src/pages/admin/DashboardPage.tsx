/** Screen 18: Admin stats dashboard */
import { useEffect, useRef, useState } from 'react';
import { fetchDashboardStats, type DashboardStats } from '@/api/admin';
import { classifyLoadError, getLoadErrorMessage } from '@/api/loadError';
import { formatDate } from '@/utils/format';
import styles from './DashboardPage.module.css';

export default function DashboardPage() {
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [retryKey, setRetryKey] = useState(0);
  const requestId = useRef(0);
  const retryBlocked = useRef(false);

  useEffect(() => {
    const currentRequestId = ++requestId.current;
    retryBlocked.current = true;
    setLoading(true);
    fetchDashboardStats()
      .then((result) => {
        if (currentRequestId !== requestId.current) return;
        setStats(result);
        setError(null);
      })
      .catch((loadError: unknown) => {
        if (
          currentRequestId !== requestId.current ||
          classifyLoadError(loadError) === 'cancelled'
        ) {
          return;
        }
        setError(getLoadErrorMessage(loadError, '대시보드'));
      })
      .finally(() => {
        if (currentRequestId !== requestId.current) return;
        retryBlocked.current = false;
        setLoading(false);
      });

    return () => {
      if (requestId.current === currentRequestId) requestId.current += 1;
    };
  }, [retryKey]);

  function retry() {
    if (retryBlocked.current || loading) return;
    retryBlocked.current = true;
    setLoading(true);
    setRetryKey((current) => current + 1);
  }

  if (loading && !error) {
    return (
      <div className={styles.page}>
        <div className={styles.loading}>대시보드를 불러오는 중...</div>
      </div>
    );
  }

  if (error || !stats) {
    return (
      <div className={styles.page}>
        <div className={styles.error} role="alert">
          <p>{error ?? '대시보드를 불러오지 못했습니다.'}</p>
          <button type="button" className={styles.retryButton} onClick={retry} disabled={loading}>
            다시 시도
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className={styles.page}>
      <h1 className={styles.title}>Admin Dashboard</h1>

      <div className={styles.statsGrid}>
        <div className={styles.statCard}>
          <div className={styles.statLabel}>Total Users</div>
          <div className={styles.statValue}>{stats.totalUsers.toLocaleString()}</div>
        </div>
        <div className={styles.statCard}>
          <div className={styles.statLabel}>Total Tracks</div>
          <div className={styles.statValue}>{stats.totalTracks.toLocaleString()}</div>
        </div>
        <div className={styles.statCard}>
          <div className={styles.statLabel}>Subscribers</div>
          <div className={styles.statValue}>{stats.totalSubscribers.toLocaleString()}</div>
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

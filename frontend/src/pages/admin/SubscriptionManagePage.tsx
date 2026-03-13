/** Screen K-2: Subscription plan management (read-only) */
import { useEffect, useState } from 'react';
import { fetchSubscriptionPlans, type SubscriptionPlan } from '@/api/subscriptions';
import { formatNumber } from '@/utils/format';
import styles from './SubscriptionManagePage.module.css';

function formatDownloadLimit(value: number): string {
  return value === -1 ? '무제한' : String(value);
}

export default function AdminSubscriptionManagePage() {
  const [plans, setPlans] = useState<SubscriptionPlan[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    setLoading(true);
    fetchSubscriptionPlans()
      .then((res) => setPlans(res.dataList))
      .catch(() => setError('구독 플랜 목록을 불러올 수 없습니다.'))
      .finally(() => setLoading(false));
  }, []);

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
      <h1 className={styles.title}>구독 플랜 관리</h1>

      <div className={styles.tableWrap}>
        <table className={styles.table}>
          <thead>
            <tr>
              <th>이름</th>
              <th className={styles.thRight}>월 요금</th>
              <th className={styles.thRight}>연 요금</th>
              <th className={styles.thRight}>일 다운로드</th>
              <th className={styles.thRight}>채널 한도</th>
              <th>상태</th>
            </tr>
          </thead>
          <tbody>
            {plans.length === 0 ? (
              <tr>
                <td colSpan={6} className={styles.empty}>
                  등록된 플랜이 없습니다.
                </td>
              </tr>
            ) : (
              plans.map((plan) => (
                <tr key={plan.id} className={styles.row}>
                  <td className={styles.planName}>{plan.name}</td>
                  <td className={styles.tdRight}>{formatNumber(plan.priceMonthly)}원</td>
                  <td className={styles.tdRight}>{formatNumber(plan.priceYearly)}원</td>
                  <td className={styles.tdRight}>{formatDownloadLimit(plan.downloadPerDay)}</td>
                  <td className={styles.tdRight}>{plan.maxWhitelistChannels}</td>
                  <td>
                    <span
                      className={`${styles.badge} ${plan.isActive ? styles.badgeActive : styles.badgeInactive}`}
                    >
                      {plan.isActive ? '활성' : '비활성'}
                    </span>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}

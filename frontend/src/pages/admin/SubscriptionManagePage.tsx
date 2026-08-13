/** Screen K-2: Subscription plan management (read-only) */
import { useEffect, useState } from 'react';
import { fetchAdminSubscriptionPlans, type SubscriptionPlan } from '@/api/subscriptions';
import { formatNumber } from '@/utils/format';
import styles from './SubscriptionManagePage.module.css';

function formatLimit(value: number): string {
  return value === -1 ? '무제한' : String(value);
}

function formatAudience(userType: SubscriptionPlan['userType']): string {
  return userType === 'BUSINESS' ? '기업' : '개인';
}

export default function AdminSubscriptionManagePage() {
  const [plans, setPlans] = useState<SubscriptionPlan[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    setLoading(true);
    fetchAdminSubscriptionPlans()
      .then((plans) => setPlans(plans))
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
              <th>대상</th>
              <th className={styles.thRight}>월 요금</th>
              <th className={styles.thRight}>연 요금</th>
              <th className={styles.thRight}>일 다운로드</th>
              <th className={styles.thRight}>채널 한도</th>
              <th className={styles.thRight}>재생목록 한도</th>
              <th>상태</th>
            </tr>
          </thead>
          <tbody>
            {plans.length === 0 ? (
              <tr>
                <td colSpan={8} className={styles.empty}>
                  등록된 플랜이 없습니다.
                </td>
              </tr>
            ) : (
              plans.map((plan) => (
                <tr key={plan.id} className={styles.row}>
                  <td className={styles.planName}>{plan.name}</td>
                  <td>{formatAudience(plan.userType)}</td>
                  <td className={styles.tdRight}>{formatNumber(plan.priceMonthly)}원</td>
                  <td className={styles.tdRight}>{formatNumber(plan.priceYearly)}원</td>
                  <td className={styles.tdRight}>{formatLimit(plan.downloadPerDay)}</td>
                  <td className={styles.tdRight}>{plan.maxWhitelistChannels}</td>
                  <td className={styles.tdRight}>{formatLimit(plan.maxPlaylists)}</td>
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

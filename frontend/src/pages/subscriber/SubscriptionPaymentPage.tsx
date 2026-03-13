/** Screen 16-2: Subscription payment (PG stub) */
import { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { fetchSubscriptionPlans, type SubscriptionPlan } from '@/api/subscriptions';
import { formatPrice } from '@/utils/format';
import styles from './SubscriptionPaymentPage.module.css';

export default function SubscriptionPaymentPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const planKey = searchParams.get('plan') ?? '';
  const cycle = (searchParams.get('cycle') ?? 'MONTHLY') as 'MONTHLY' | 'YEARLY';

  const [plan, setPlan] = useState<SubscriptionPlan | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchSubscriptionPlans()
      .then((res) => {
        const found = res.dataList.find(
          (p) => p.name.toUpperCase() === planKey.toUpperCase(),
        );
        setPlan(found ?? null);
      })
      .catch(() => {})
      .finally(() => setLoading(false));
  }, [planKey]);

  if (loading) {
    return (
      <div className={styles.page}>
        <div className={styles.loading}>{'로딩 중...'}</div>
      </div>
    );
  }

  if (!plan) {
    return (
      <div className={styles.page}>
        <h1 className={styles.pageTitle}>{'구독 결제'}</h1>
        <div className={styles.error}>
          {'선택된 플랜이 없습니다. '}
          <button className={styles.btnBack} onClick={() => navigate('/subscriptions')}>
            {'플랜 선택으로 돌아가기'}
          </button>
        </div>
      </div>
    );
  }

  const price = cycle === 'YEARLY' ? plan.priceYearly : plan.priceMonthly;
  const monthlyEquiv =
    cycle === 'YEARLY' ? formatPrice(Math.floor(plan.priceYearly / 12)) : null;

  return (
    <div className={styles.page}>
      <h1 className={styles.pageTitle}>{'구독 결제'}</h1>

      {/* Plan summary */}
      <div className={styles.card}>
        <div className={styles.cardTitle}>{'선택한 플랜'}</div>
        <div className={styles.row}>
          <span className={styles.label}>{'플랜'}</span>
          <span className={styles.value}>{plan.name}</span>
        </div>
        <div className={styles.row}>
          <span className={styles.label}>{'결제 주기'}</span>
          <span className={styles.value}>
            {cycle === 'MONTHLY' ? '월간' : '연간'}
          </span>
        </div>
        <div className={styles.row}>
          <span className={styles.label}>{'일 다운로드 한도'}</span>
          <span className={styles.value}>
            {plan.downloadPerDay === -1 ? '무제한' : `${plan.downloadPerDay}곡`}
          </span>
        </div>
        <div className={styles.row}>
          <span className={styles.label}>{'결제 금액'}</span>
          <span className={styles.total}>
            {formatPrice(price)}
            {monthlyEquiv && ` (${monthlyEquiv}/월)`}
          </span>
        </div>
      </div>

      {/* PG integration notice */}
      <div className={styles.pgNotice}>
        <div className={styles.pgIcon}>{'\uD83D\uDCB3'}</div>
        <div className={styles.pgText}>
          {'PG(결제 게이트웨이) 연동 준비 중입니다.'}
          <br />
          {'실제 결제 기능은 곧 추가될 예정입니다.'}
        </div>
      </div>

      <div className={styles.btnGroup}>
        <button
          className={styles.btnBack}
          onClick={() => navigate('/subscriptions')}
        >
          {'돌아가기'}
        </button>
        <button className={styles.btnPay} disabled>
          {'결제하기 (준비 중)'}
        </button>
      </div>
    </div>
  );
}

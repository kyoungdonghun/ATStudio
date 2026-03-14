/** Screen 16-2: Subscription payment (Mock PG — calls subscribe API) */
import { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { fetchSubscriptionPlans, type SubscriptionPlan } from '@/api/subscriptions';
import { subscribe } from '@/api/userSubscriptions';
import { formatPrice } from '@/utils/format';
import { useToastStore } from '@/store/toastStore';
import styles from './SubscriptionPaymentPage.module.css';

export default function SubscriptionPaymentPage() {
  const navigate = useNavigate();
  const toast = useToastStore();
  const [searchParams] = useSearchParams();
  const planKey = searchParams.get('plan') ?? '';
  const cycle = (searchParams.get('cycle') ?? 'MONTHLY') as 'MONTHLY' | 'YEARLY';

  const [plan, setPlan] = useState<SubscriptionPlan | null>(null);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);

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

  const handleSubscribe = async () => {
    if (!plan || submitting) return;
    setSubmitting(true);
    try {
      await subscribe({ subscriptionId: plan.id, billingCycle: cycle });
      toast.show('success', '구독이 시작되었습니다!');
      navigate('/subscriptions/manage');
    } catch (err: unknown) {
      const msg =
        (err as { response?: { data?: { message?: string } } })?.response?.data
          ?.message ?? '구독 처리 중 오류가 발생했습니다.';
      toast.show('error', msg);
    } finally {
      setSubmitting(false);
    }
  };

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
        <div className={styles.pgText}>
          {'현재 테스트 환경에서는 결제 없이 구독이 즉시 시작됩니다.'}
          <br />
          {'실제 PG 결제 연동은 프로덕션 단계에서 추가됩니다.'}
        </div>
      </div>

      <div className={styles.btnGroup}>
        <button
          className={styles.btnBack}
          onClick={() => navigate('/subscriptions')}
        >
          {'돌아가기'}
        </button>
        <button
          className={styles.btnPay}
          onClick={handleSubscribe}
          disabled={submitting}
        >
          {submitting ? '처리 중...' : '구독 시작하기'}
        </button>
      </div>
    </div>
  );
}

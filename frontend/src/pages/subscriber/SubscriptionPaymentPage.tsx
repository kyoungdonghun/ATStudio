/** Screen 16-2: Subscription payment (Mock PG contract) */
import { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { fetchSubscriptionPlans, type SubscriptionPlan } from '@/api/subscriptions';
import {
  cancelPayment,
  confirmPayment,
  prepareSubscriptionPayment,
  type PaymentPrepareResponse,
} from '@/api/payments';
import { formatPrice } from '@/utils/format';
import { useToastStore } from '@/store/toastStore';
import styles from './SubscriptionPaymentPage.module.css';

export default function SubscriptionPaymentPage() {
  const navigate = useNavigate();
  const showToast = useToastStore((s) => s.show);
  const [searchParams] = useSearchParams();
  const planKey = searchParams.get('plan') ?? '';
  const cycle = (searchParams.get('cycle') ?? 'MONTHLY') as 'MONTHLY' | 'YEARLY';

  const [plan, setPlan] = useState<SubscriptionPlan | null>(null);
  const [paymentOrder, setPaymentOrder] = useState<PaymentPrepareResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [orderStatus, setOrderStatus] = useState<'READY' | 'FAILED' | 'CANCELLED'>('READY');
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  useEffect(() => {
    let active = true;

    async function loadAndPrepare() {
      setLoading(true);
      setErrorMessage(null);
      setPaymentOrder(null);
      setOrderStatus('READY');

      try {
        const plans = await fetchSubscriptionPlans();
        const found = plans.find((p) => p.name.toUpperCase() === planKey.toUpperCase());
        if (!active) return;
        setPlan(found ?? null);

        if (!found) return;

        const prepared = await prepareSubscriptionPayment({
          purpose: 'SUBSCRIBE',
          subscriptionId: found.id,
          billingCycle: cycle,
        });
        if (!active) return;
        setPaymentOrder(prepared);
      } catch (err: unknown) {
        if (!active) return;
        const msg =
          (err as { response?: { data?: { message?: string } } })?.response?.data?.message ??
          '결제 준비 중 오류가 발생했습니다.';
        setErrorMessage(msg);
        showToast('error', msg);
      } finally {
        if (active) setLoading(false);
      }
    }

    void loadAndPrepare();

    return () => {
      active = false;
    };
  }, [cycle, planKey, showToast]);

  const handleConfirm = async () => {
    if (!paymentOrder || submitting) return;
    setSubmitting(true);
    try {
      await confirmPayment({
        orderId: paymentOrder.orderId,
        amount: paymentOrder.amount,
        provider: paymentOrder.provider,
        providerToken: paymentOrder.checkout.confirmToken,
      });
      showToast('success', '구독이 시작되었습니다!');
      navigate('/subscriptions/manage');
    } catch (err: unknown) {
      const msg =
        (err as { response?: { data?: { message?: string } } })?.response?.data?.message ??
        '구독 처리 중 오류가 발생했습니다.';
      showToast('error', msg);
    } finally {
      setSubmitting(false);
    }
  };

  const handleClosePayment = async (status: 'FAILED' | 'CANCELLED') => {
    if (!paymentOrder || submitting) return;
    setSubmitting(true);
    try {
      await cancelPayment({
        orderId: paymentOrder.orderId,
        status,
        reason: status === 'FAILED' ? 'Mock payment failure' : 'Mock payment cancelled',
      });
      setOrderStatus(status);
      showToast(
        status === 'FAILED' ? 'error' : 'info',
        status === 'FAILED' ? '모의 결제가 실패 처리되었습니다.' : '모의 결제가 취소되었습니다.',
      );
    } catch (err: unknown) {
      const msg =
        (err as { response?: { data?: { message?: string } } })?.response?.data?.message ??
        '결제 상태 변경 중 오류가 발생했습니다.';
      showToast('error', msg);
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
  const paymentAmount = paymentOrder?.amount ?? price;
  const monthlyEquiv = cycle === 'YEARLY' ? formatPrice(Math.floor(plan.priceYearly / 12)) : null;
  const canConfirm = Boolean(paymentOrder) && orderStatus === 'READY' && !submitting;

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
          <span className={styles.value}>{cycle === 'MONTHLY' ? '월간' : '연간'}</span>
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
            {formatPrice(paymentAmount)}
            {monthlyEquiv && ` (${monthlyEquiv}/월)`}
          </span>
        </div>
      </div>

      {/* PG integration notice */}
      <div className={styles.pgNotice}>
        <div className={styles.pgText}>
          {'현재 테스트 환경에서는 Mock 결제로 실제 청구 없이 결제 흐름을 확인합니다.'}
          <br />
          {'성공 확인 후에만 구독이 시작됩니다.'}
        </div>
      </div>

      <div className={styles.mockPanel}>
        <div className={styles.mockHeader}>
          <span className={styles.mockTitle}>{'Mock 결제'}</span>
          <span className={styles.mockStatus}>{paymentOrder ? orderStatus : 'PREPARING'}</span>
        </div>
        {paymentOrder ? (
          <div className={styles.mockMeta}>
            <span>{paymentOrder.orderId}</span>
            <span>{paymentOrder.provider}</span>
          </div>
        ) : (
          <div className={styles.mockMeta}>{'결제 주문을 준비 중입니다.'}</div>
        )}
        {errorMessage && <div className={styles.mockError}>{errorMessage}</div>}
        {orderStatus === 'FAILED' && (
          <div className={styles.mockError}>
            {'실패 처리된 결제입니다. 플랜 선택부터 다시 시도해주세요.'}
          </div>
        )}
        {orderStatus === 'CANCELLED' && (
          <div className={styles.mockError}>
            {'취소된 결제입니다. 플랜 선택부터 다시 시도해주세요.'}
          </div>
        )}
      </div>

      <div className={styles.btnGroup}>
        <button className={styles.btnBack} onClick={() => navigate('/subscriptions')}>
          {'돌아가기'}
        </button>
        <button
          className={styles.btnBack}
          onClick={() => handleClosePayment('FAILED')}
          disabled={!paymentOrder || submitting || orderStatus !== 'READY'}
        >
          {'실패'}
        </button>
        <button
          className={styles.btnBack}
          onClick={() => handleClosePayment('CANCELLED')}
          disabled={!paymentOrder || submitting || orderStatus !== 'READY'}
        >
          {'취소'}
        </button>
        <button className={styles.btnPay} onClick={handleConfirm} disabled={!canConfirm}>
          {submitting ? '처리 중...' : '결제 확인'}
        </button>
      </div>
    </div>
  );
}

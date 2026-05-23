/** Screen 16-2: Recurring subscription checkout */
import { useEffect, useRef, useState } from 'react';
import { useLocation, useNavigate, useSearchParams } from 'react-router-dom';
import { fetchSubscriptionPlans, type SubscriptionPlan } from '@/api/subscriptions';
import {
  confirmBillingAgreement,
  prepareBillingAgreement,
  type BillingAgreementPrepareResponse,
} from '@/api/payments';
import { formatPrice } from '@/utils/format';
import { loadTossPaymentsSdk } from '@/utils/tossPayments';
import { useToastStore } from '@/store/toastStore';
import styles from './SubscriptionPaymentPage.module.css';

export default function SubscriptionPaymentPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const showToast = useToastStore((s) => s.show);
  const [searchParams] = useSearchParams();
  const planKey = searchParams.get('plan') ?? '';
  const cycle = (searchParams.get('cycle') ?? 'MONTHLY') as 'MONTHLY' | 'YEARLY';
  const purposeParam = searchParams.get('purpose');
  const purpose =
    purposeParam === 'UPGRADE' || purposeParam === 'BILLING_AGREEMENT' ? purposeParam : 'SUBSCRIBE';
  const isBillingAgreementOnly = purpose === 'BILLING_AGREEMENT';
  const isSupportedRedirect =
    location.pathname.includes('/subscriptions/checkout/') ||
    location.pathname.includes('/subscriptions/billing/');
  const isLegacyPaymentRedirect = location.pathname.includes('/subscriptions/payment/');
  const isSuccessRedirect = location.pathname.endsWith('/success');
  const isFailRedirect = location.pathname.endsWith('/fail');
  const isRedirect = isSuccessRedirect || isFailRedirect;
  const redirectHandledRef = useRef(false);

  const [plan, setPlan] = useState<SubscriptionPlan | null>(null);
  const [paymentOrder, setPaymentOrder] = useState<BillingAgreementPrepareResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  useEffect(() => {
    if (!isRedirect || redirectHandledRef.current) return;
    redirectHandledRef.current = true;

    async function handleRedirect() {
      setLoading(true);
      setErrorMessage(null);

      if (isLegacyPaymentRedirect || !isSupportedRedirect) {
        const message =
          '지원이 종료된 구독 결제 경로입니다. 구독 페이지에서 새 결제를 시작해주세요.';
        setErrorMessage(message);
        showToast('error', message);
        setLoading(false);
        return;
      }

      const orderId = searchParams.get('orderId');
      if (isFailRedirect) {
        const message = searchParams.get('message') ?? '카드 등록이 완료되지 않았습니다.';
        setErrorMessage(message);
        showToast('error', message);
        setLoading(false);
        return;
      }

      const authKey = searchParams.get('authKey');
      const customerKey = searchParams.get('customerKey');
      const amount = Number(searchParams.get('amount'));
      if (!orderId || !authKey || !customerKey || !Number.isFinite(amount)) {
        const message = '자동결제 인증 정보가 올바르지 않습니다.';
        setErrorMessage(message);
        showToast('error', message);
        setLoading(false);
        return;
      }

      try {
        await confirmBillingAgreement({
          orderId,
          authKey,
          customerKey,
          amount,
        });
        showToast(
          'success',
          isBillingAgreementOnly
            ? '결제수단이 다시 등록되었습니다.'
            : '자동결제가 등록되고 구독이 시작되었습니다.',
        );
        navigate('/subscriptions/manage');
      } catch (err: unknown) {
        const msg =
          (err as { response?: { data?: { message?: string } } })?.response?.data?.message ??
          '자동결제 등록 확인 중 오류가 발생했습니다.';
        setErrorMessage(msg);
        showToast('error', msg);
        setLoading(false);
      }
    }

    void handleRedirect();
  }, [
    isFailRedirect,
    isBillingAgreementOnly,
    isLegacyPaymentRedirect,
    isRedirect,
    isSupportedRedirect,
    navigate,
    searchParams,
    showToast,
  ]);

  useEffect(() => {
    if (isRedirect) return;
    let active = true;

    async function loadAndPrepare() {
      setLoading(true);
      setErrorMessage(null);
      setPaymentOrder(null);

      try {
        const plans = await fetchSubscriptionPlans();
        const found = plans.find((p) => p.name.toUpperCase() === planKey.toUpperCase());
        if (!active) return;
        setPlan(found ?? null);

        if (!found) return;

        if (purpose === 'UPGRADE') {
          setErrorMessage('플랜 변경은 내 구독 화면에서 변경 내역을 확인한 뒤 진행해주세요.');
          return;
        }

        const prepared = await prepareBillingAgreement({
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
  }, [cycle, isRedirect, planKey, purpose, showToast]);

  const handleConfirm = async () => {
    if (!paymentOrder || submitting) return;
    setSubmitting(true);
    try {
      const { clientKey, customerKey, successUrl, failUrl, method } = paymentOrder.checkout;
      if (!clientKey || !customerKey || !successUrl || !failUrl) {
        throw new Error('Toss 자동결제 설정이 아직 준비되지 않았습니다.');
      }
      const TossPayments = await loadTossPaymentsSdk();
      const payment = TossPayments(clientKey).payment({ customerKey });
      await payment.requestBillingAuth({
        method: method ?? 'CARD',
        successUrl: withBillingQuery(
          successUrl,
          paymentOrder.orderId,
          paymentOrder.amount,
          purpose,
        ),
        failUrl: withBillingQuery(failUrl, paymentOrder.orderId, paymentOrder.amount, purpose),
      });
    } catch (err: unknown) {
      const msg =
        (err as { response?: { data?: { message?: string } } })?.response?.data?.message ??
        (err instanceof Error ? err.message : null) ??
        '구독 결제 처리 중 오류가 발생했습니다.';
      showToast('error', msg);
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return (
      <div className={styles.page}>
        <div className={styles.loading}>
          {isSuccessRedirect ? 'Toss 자동결제 등록을 확인하는 중...' : '로딩 중...'}
        </div>
      </div>
    );
  }

  if (isRedirect) {
    return (
      <div className={styles.page}>
        <h1 className={styles.pageTitle}>{isFailRedirect ? '결제 실패' : '자동결제 확인'}</h1>
        <div className={styles.pgNotice}>
          <div className={styles.pgText}>
            {errorMessage ?? '결제 상태를 확인했습니다. 다시 시도할 수 있습니다.'}
          </div>
        </div>
        <div className={styles.btnGroup}>
          <button className={styles.btnBack} onClick={() => navigate('/subscriptions')}>
            {'플랜 선택으로 돌아가기'}
          </button>
        </div>
      </div>
    );
  }

  if (!plan) {
    return (
      <div className={styles.page}>
        <h1 className={styles.pageTitle}>{'구독 결제'}</h1>
        <div className={styles.error}>
          {'선택한 플랜이 없습니다. '}
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
  const canConfirm = Boolean(paymentOrder) && !submitting;

  return (
    <div className={styles.page}>
      <h1 className={styles.pageTitle}>{isBillingAgreementOnly ? '결제수단 등록' : '구독 결제'}</h1>

      <div className={styles.card}>
        <div className={styles.cardTitle}>
          {isBillingAgreementOnly ? '현재 구독 결제수단' : '선택한 플랜'}
        </div>
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
          <span className={styles.label}>
            {isBillingAgreementOnly ? '등록 시 결제 금액' : '첫 결제 금액'}
          </span>
          <span className={styles.total}>
            {isBillingAgreementOnly ? '즉시 결제 없음' : formatPrice(paymentAmount)}
            {!isBillingAgreementOnly && monthlyEquiv && ` (${monthlyEquiv}/월)`}
          </span>
        </div>
      </div>

      <div className={styles.pgNotice}>
        <div className={styles.pgText}>
          {isBillingAgreementOnly
            ? 'Toss 테스트 환경에서 결제수단만 다시 등록합니다. 현재 구독 기간과 플랜은 이 단계에서 변경되지 않습니다.'
            : 'Toss 테스트 환경에서 자동결제 수단을 등록하고, 서버가 빌링키 발급과 첫 결제를 확인한 뒤에만 구독이 시작됩니다.'}
          <br />
          {'authKey, customerKey, billingKey, 카드 원문 정보는 화면에 표시하지 않습니다.'}
        </div>
      </div>

      <div className={styles.mockPanel}>
        <div className={styles.mockHeader}>
          <span className={styles.mockTitle}>{'Toss 자동결제'}</span>
          <span className={styles.mockStatus}>{paymentOrder ? 'READY' : 'PREPARING'}</span>
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
      </div>

      <div className={styles.btnGroup}>
        <button className={styles.btnBack} onClick={() => navigate('/subscriptions')}>
          {'돌아가기'}
        </button>
        <button className={styles.btnPay} onClick={handleConfirm} disabled={!canConfirm}>
          {submitting ? '처리 중...' : '카드 등록하기'}
        </button>
      </div>
    </div>
  );
}

function withBillingQuery(url: string, orderId: string, amount: number, purpose: string): string {
  const next = new URL(url, window.location.origin);
  next.searchParams.set('orderId', orderId);
  next.searchParams.set('amount', String(amount));
  if (purpose !== 'SUBSCRIBE') {
    next.searchParams.set('purpose', purpose);
  }
  return next.toString();
}

/** Screen 16-2: Subscription payment (Mock PG contract) */
import { useEffect, useRef, useState } from 'react';
import { useLocation, useNavigate, useSearchParams } from 'react-router-dom';
import { fetchSubscriptionPlans, type SubscriptionPlan } from '@/api/subscriptions';
import {
  cancelPayment,
  confirmBillingAgreement,
  confirmPayment,
  prepareBillingAgreement,
  prepareSubscriptionPayment,
  type BillingAgreementPrepareResponse,
  type PaymentPrepareResponse,
} from '@/api/payments';
import { formatPrice } from '@/utils/format';
import { loadTossPaymentsSdk, type TossWidgets } from '@/utils/tossPayments';
import { useToastStore } from '@/store/toastStore';
import styles from './SubscriptionPaymentPage.module.css';

export default function SubscriptionPaymentPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const showToast = useToastStore((s) => s.show);
  const [searchParams] = useSearchParams();
  const planKey = searchParams.get('plan') ?? '';
  const cycle = (searchParams.get('cycle') ?? 'MONTHLY') as 'MONTHLY' | 'YEARLY';
  const purpose = searchParams.get('purpose') === 'UPGRADE' ? 'UPGRADE' : 'SUBSCRIBE';
  const isBillingMode = searchParams.get('mode') === 'recurring';
  const isBillingRedirect = location.pathname.includes('/subscriptions/billing/');
  const isTossSuccess = location.pathname.endsWith('/success');
  const isTossFail = location.pathname.endsWith('/fail');
  const isTossRedirect = isTossSuccess || isTossFail;
  const redirectHandledRef = useRef(false);
  const tossWidgetsRef = useRef<TossWidgets | null>(null);

  const [plan, setPlan] = useState<SubscriptionPlan | null>(null);
  const [paymentOrder, setPaymentOrder] = useState<
    PaymentPrepareResponse | BillingAgreementPrepareResponse | null
  >(null);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [orderStatus, setOrderStatus] = useState<'READY' | 'FAILED' | 'CANCELLED'>('READY');
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [checkoutReady, setCheckoutReady] = useState(false);

  const isBillingCheckoutType = (type?: string) =>
    type === 'TOSS_BILLING_AUTH' || type === 'TOSS_BILLING_WIDGET';

  useEffect(() => {
    if (!isTossRedirect || redirectHandledRef.current) return;
    redirectHandledRef.current = true;

    async function handleTossRedirect() {
      setLoading(true);
      setErrorMessage(null);

      const orderId = searchParams.get('orderId');
      if (isTossFail) {
        const code = searchParams.get('code') ?? 'TOSS_PAYMENT_FAILED';
        const message = searchParams.get('message') ?? '토스 결제가 완료되지 않았습니다.';
        if (orderId && !isBillingRedirect) {
          try {
            await cancelPayment({
              orderId,
              status: 'FAILED',
              reason: `${code}: ${message}`,
            });
          } catch {
            // The user-facing failure page should still render even if order closing was already handled.
          }
        }
        setErrorMessage(message);
        showToast('error', message);
        setLoading(false);
        return;
      }

      if (isBillingRedirect) {
        const authKey = searchParams.get('authKey');
        const customerKey = searchParams.get('customerKey');
        const amount = Number(searchParams.get('amount'));
        if (!orderId || !authKey || !customerKey || !Number.isFinite(amount)) {
          const message = '토스 자동결제 인증 정보가 올바르지 않습니다.';
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
          showToast('success', '자동결제가 등록되고 구독이 시작되었습니다.');
          navigate('/subscriptions/manage');
        } catch (err: unknown) {
          const msg =
            (err as { response?: { data?: { message?: string } } })?.response?.data?.message ??
            '자동결제 등록 확인 중 오류가 발생했습니다.';
          setErrorMessage(msg);
          showToast('error', msg);
          setLoading(false);
        }
        return;
      }

      const paymentKey = searchParams.get('paymentKey');
      const amount = Number(searchParams.get('amount'));
      if (!orderId || !paymentKey || !Number.isFinite(amount)) {
        const message = '토스 결제 승인 정보가 올바르지 않습니다.';
        setErrorMessage(message);
        showToast('error', message);
        setLoading(false);
        return;
      }

      try {
        const confirmed = await confirmPayment({
          orderId,
          amount,
          provider: 'TOSS',
          paymentKey,
        });
        showToast(
          'success',
          confirmed.purpose === 'UPGRADE'
            ? '구독 플랜이 변경되었습니다.'
            : '구독이 시작되었습니다!',
        );
        navigate('/subscriptions/manage');
      } catch (err: unknown) {
        const msg =
          (err as { response?: { data?: { message?: string } } })?.response?.data?.message ??
          '토스 결제 승인 중 오류가 발생했습니다.';
        setErrorMessage(msg);
        showToast('error', msg);
        setLoading(false);
      }
    }

    void handleTossRedirect();
  }, [isBillingRedirect, isTossFail, isTossRedirect, navigate, searchParams, showToast]);

  useEffect(() => {
    if (isTossRedirect) return;
    let active = true;

    async function loadAndPrepare() {
      setLoading(true);
      setErrorMessage(null);
      setPaymentOrder(null);
      setOrderStatus('READY');
      setCheckoutReady(false);
      tossWidgetsRef.current = null;

      try {
        const plans = await fetchSubscriptionPlans();
        const found = plans.find((p) => p.name.toUpperCase() === planKey.toUpperCase());
        if (!active) return;
        setPlan(found ?? null);

        if (!found) return;

        const prepared =
          isBillingMode && purpose === 'SUBSCRIBE'
            ? await prepareBillingAgreement({
                subscriptionId: found.id,
                billingCycle: cycle,
              })
            : await prepareSubscriptionPayment({
                purpose,
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
  }, [cycle, isBillingMode, isTossRedirect, planKey, purpose, showToast]);

  useEffect(() => {
    if (!paymentOrder || paymentOrder.checkout.type !== 'TOSS_WIDGET') return;
    const order = paymentOrder;
    let active = true;

    async function renderTossWidget() {
      setCheckoutReady(false);
      setErrorMessage(null);

      const { clientKey, customerKey } = order.checkout;
      if (!clientKey || !customerKey) {
        setErrorMessage('토스 결제 설정이 준비되지 않았습니다.');
        return;
      }

      try {
        const TossPayments = await loadTossPaymentsSdk();
        if (!active) return;

        const widgets = TossPayments(clientKey).widgets({ customerKey });
        tossWidgetsRef.current = widgets;
        await widgets.setAmount({
          value: order.amount,
          currency: order.currency,
        });
        await Promise.all([
          widgets.renderPaymentMethods({
            selector: '#toss-payment-methods',
            variantKey: 'DEFAULT',
          }),
          widgets.renderAgreement({
            selector: '#toss-payment-agreement',
            variantKey: 'AGREEMENT',
          }),
        ]);

        if (active) setCheckoutReady(true);
      } catch (err) {
        if (!active) return;
        const msg = err instanceof Error ? err.message : '토스 결제창을 불러오지 못했습니다.';
        setErrorMessage(msg);
        showToast('error', msg);
      }
    }

    void renderTossWidget();

    return () => {
      active = false;
      tossWidgetsRef.current = null;
    };
  }, [paymentOrder, showToast]);

  const handleConfirm = async () => {
    if (!paymentOrder || submitting) return;
    setSubmitting(true);
    try {
      if (paymentOrder.checkout.type === 'TOSS_WIDGET') {
        const { orderName, successUrl, failUrl } = paymentOrder.checkout;
        if (!tossWidgetsRef.current || !orderName || !successUrl || !failUrl) {
          throw new Error('토스 결제창이 아직 준비되지 않았습니다.');
        }
        await tossWidgetsRef.current.requestPayment({
          orderId: paymentOrder.orderId,
          orderName,
          successUrl,
          failUrl,
        });
        return;
      }

      if (isBillingCheckoutType(paymentOrder.checkout.type)) {
        const { clientKey, customerKey, successUrl, failUrl, method } = paymentOrder.checkout;
        if (!clientKey || !customerKey || !successUrl || !failUrl) {
          throw new Error('토스 자동결제 설정이 아직 준비되지 않았습니다.');
        }
        const TossPayments = await loadTossPaymentsSdk();
        const payment = TossPayments(clientKey).payment({ customerKey });
        await payment.requestBillingAuth({
          method: method ?? 'CARD',
          successUrl: withBillingQuery(successUrl, paymentOrder.orderId, paymentOrder.amount),
          failUrl: withBillingQuery(failUrl, paymentOrder.orderId, paymentOrder.amount),
        });
        return;
      }

      const confirmed = await confirmPayment({
        orderId: paymentOrder.orderId,
        amount: paymentOrder.amount,
        provider: paymentOrder.provider,
        providerToken: paymentOrder.checkout.confirmToken,
      });
      showToast(
        'success',
        confirmed.purpose === 'UPGRADE' ? '구독 플랜이 변경되었습니다.' : '구독이 시작되었습니다!',
      );
      navigate('/subscriptions/manage');
    } catch (err: unknown) {
      const msg =
        (err as { response?: { data?: { message?: string } } })?.response?.data?.message ??
        (err instanceof Error ? err.message : null) ??
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
        <div className={styles.loading}>
          {isTossSuccess
            ? isBillingRedirect
              ? '토스 자동결제 등록을 확인하는 중...'
              : '토스 결제 승인을 확인하는 중...'
            : '로딩 중...'}
        </div>
      </div>
    );
  }

  if (isTossRedirect) {
    return (
      <div className={styles.page}>
        <h1 className={styles.pageTitle}>
          {isTossFail ? '결제 실패' : isBillingRedirect ? '자동결제 확인' : '결제 확인'}
        </h1>
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
  const isTossCheckout = paymentOrder?.checkout.type === 'TOSS_WIDGET';
  const isBillingCheckout = isBillingCheckoutType(paymentOrder?.checkout.type);
  const canConfirm =
    Boolean(paymentOrder) &&
    orderStatus === 'READY' &&
    !submitting &&
    (!isTossCheckout || checkoutReady);
  const checkoutTitle = isBillingCheckout ? 'Toss 자동결제' : isTossCheckout ? 'Toss 결제' : 'Mock 결제';

  return (
    <div className={styles.page}>
      <h1 className={styles.pageTitle}>
        {purpose === 'UPGRADE' ? '업그레이드 결제' : '구독 결제'}
      </h1>

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
          {isTossCheckout
            ? '토스 테스트 키 환경에서는 실제 청구 없이 결제 승인 흐름을 확인합니다.'
            : isBillingCheckout
              ? '토스 테스트 키 환경에서는 실제 청구 없이 자동결제 등록과 최초 결제 흐름을 확인합니다.'
            : '현재 테스트 환경에서는 Mock 결제로 실제 청구 없이 결제 흐름을 확인합니다.'}
          <br />
          {'성공 확인 후에만 구독이 시작됩니다.'}
        </div>
      </div>

      <div className={styles.mockPanel}>
        <div className={styles.mockHeader}>
          <span className={styles.mockTitle}>{checkoutTitle}</span>
          <span className={styles.mockStatus}>
            {paymentOrder
              ? isTossCheckout && !checkoutReady
                ? 'LOADING'
                : orderStatus
              : 'PREPARING'}
          </span>
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
        {isTossCheckout && (
          <div className={styles.tossWidget}>
            <div id="toss-payment-methods" className={styles.tossWidgetSection} />
            <div id="toss-payment-agreement" className={styles.tossWidgetSection} />
          </div>
        )}
      </div>

      <div className={styles.btnGroup}>
        <button className={styles.btnBack} onClick={() => navigate('/subscriptions')}>
          {'돌아가기'}
        </button>
        {!isTossCheckout && !isBillingCheckout && (
          <>
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
          </>
        )}
        <button className={styles.btnPay} onClick={handleConfirm} disabled={!canConfirm}>
          {submitting
            ? '처리 중...'
            : isBillingCheckout
              ? '카드 등록하기'
              : isTossCheckout
                ? '토스 결제창 열기'
                : '결제 확인'}
        </button>
      </div>
    </div>
  );
}

function withBillingQuery(url: string, orderId: string, amount: number): string {
  const next = new URL(url, window.location.origin);
  next.searchParams.set('orderId', orderId);
  next.searchParams.set('amount', String(amount));
  return next.toString();
}

/** Screen 16-2: Recurring subscription checkout */
import { useCallback, useEffect, useLayoutEffect, useRef, useState } from 'react';
import { useLocation, useNavigate, useSearchParams } from 'react-router-dom';
import { fetchSubscriptionPlans, type SubscriptionPlan } from '@/api/subscriptions';
import { getApiErrorCode } from '@/api/client';
import {
  confirmBillingAgreement,
  fetchMyBillingAgreement,
  fetchPaymentCommandOutcome,
  prepareBillingAgreement,
  type BillingAgreementPrepareResponse,
  type PaymentCommandOutcome,
} from '@/api/payments';
import { fetchMySubscription } from '@/api/userSubscriptions';
import { formatPrice } from '@/utils/format';
import {
  CorruptCheckoutPrepareAttemptError,
  createNewCheckoutPrepareAttempt,
  getOrCreateCheckoutPrepareAttempt,
  isNewCheckoutPrepareAttemptRequired,
} from '@/utils/checkoutPrepareAttempt';
import { loadTossPaymentsSdk } from '@/utils/tossPayments';
import { useAuthStore } from '@/store/authStore';
import { useToastStore } from '@/store/toastStore';
import type { UserType } from '@/types';
import styles from './SubscriptionPaymentPage.module.css';

type BillingCycle = 'MONTHLY' | 'YEARLY';
type BillingPurpose = 'SUBSCRIBE' | 'BILLING_AGREEMENT';
type RecoveryState = 'CHECKING' | 'RELOAD_FAILED' | 'UNKNOWN' | 'FAILED';

interface BillingCallbackContext {
  orderId: string | null;
  authKey: string | null;
  customerKey: string | null;
  amount: number | null;
  hasAuthenticationContext: boolean;
  isMalformed: boolean;
}

const INVALID_CHECKOUT_CONTEXT_MESSAGE =
  '\uC120\uD0DD\uD55C \uACB0\uC81C \uC815\uBCF4\uB97C \uB2E4\uC2DC \uD655\uC778\uD574\uC8FC\uC138\uC694.';
const INVALID_PREPARE_RESPONSE_MESSAGE =
  '\uACB0\uC81C \uC900\uBE44 \uC751\uB2F5\uC744 \uD655\uC778\uD560 \uC218 \uC5C6\uC2B5\uB2C8\uB2E4. \uB2E4\uC2DC \uC2DC\uB3C4\uD574\uC8FC\uC138\uC694.';
const INVALID_CALLBACK_CONTEXT_MESSAGE =
  '\uC790\uB3D9\uACB0\uC81C \uC778\uC99D \uC815\uBCF4\uAC00 \uC62C\uBC14\uB974\uC9C0 \uC54A\uC2B5\uB2C8\uB2E4.';
const PREPARE_FAILED_MESSAGE =
  '\uACB0\uC81C \uC900\uBE44\uB97C \uC644\uB8CC\uD558\uC9C0 \uBABB\uD588\uC2B5\uB2C8\uB2E4. \uB2E4\uC2DC \uC2DC\uB3C4\uD574\uC8FC\uC138\uC694.';
const UPGRADE_ROUTE_MESSAGE =
  '\uD50C\uB79C \uBCC0\uACBD\uC740 \uB0B4 \uAD6C\uB3C5 \uD654\uBA74\uC5D0\uC11C \uBCC0\uACBD \uB0B4\uC5ED\uC744 \uD655\uC778\uD55C \uB4A4 \uC9C4\uD589\uD574\uC8FC\uC138\uC694.';
const RELOAD_FAILED_MESSAGE =
  '\uC694\uCCAD\uC740 \uC644\uB8CC\uB418\uC5C8\uC9C0\uB9CC \uCD5C\uC2E0 \uAD6C\uB3C5 \uC815\uBCF4\uB97C \uBD88\uB7EC\uC624\uC9C0 \uBABB\uD588\uC2B5\uB2C8\uB2E4.';
const UNKNOWN_MESSAGE =
  '\uCC98\uB9AC\uAC00 \uC774\uBBF8 \uC644\uB8CC\uB418\uC5C8\uC744 \uC218 \uC788\uC2B5\uB2C8\uB2E4. \uC791\uC5C5\uC744 \uB2E4\uC2DC \uC2E4\uD589\uD558\uC9C0 \uB9D0\uACE0 \uC0C1\uD0DC\uB97C \uB2E4\uC2DC \uD655\uC778\uD574\uC8FC\uC138\uC694.';
const FAILED_MESSAGE =
  '\uC694\uCCAD\uC774 \uC644\uB8CC\uB418\uC9C0 \uC54A\uC558\uC2B5\uB2C8\uB2E4.';

export default function SubscriptionPaymentPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const showToast = useToastStore((s) => s.show);
  const authenticatedUserType = useAuthStore((s) => s.user?.userType);
  const [searchParams] = useSearchParams();
  const purposeParam = getSingleSearchParam(searchParams, 'purpose');
  const isSuccessRedirect = location.pathname === '/subscriptions/checkout/success';
  const isFailRedirect = location.pathname === '/subscriptions/checkout/fail';
  const isRedirect = isSuccessRedirect || isFailRedirect;
  const routePlanId = toPositiveInteger(getSingleSearchParam(searchParams, 'planId'));
  const routeUserType = toUserType(getSingleSearchParam(searchParams, 'userType'));
  const routeCycle = toBillingCycle(getSingleSearchParam(searchParams, 'billingCycle'));
  const routePurpose = toBillingPurpose(purposeParam);
  const callbackPurpose = searchParams.has('purpose')
    ? toBillingPurpose(purposeParam)
    : 'SUBSCRIBE';
  const callbackContextRef = useRef<BillingCallbackContext | null>(null);
  if (isRedirect && callbackContextRef.current === null) {
    const orderIdParam = getSingleSearchParam(searchParams, 'orderId');
    const authKeyParam = getSingleSearchParam(searchParams, 'authKey');
    const customerKeyParam = getSingleSearchParam(searchParams, 'customerKey');
    const amountParam = getSingleSearchParam(searchParams, 'amount');
    const hasAuthenticationContext = searchParams.has('authKey') || searchParams.has('customerKey');
    callbackContextRef.current = {
      orderId: toNonBlankString(orderIdParam),
      authKey: toNonBlankString(authKeyParam),
      customerKey: toNonBlankString(customerKeyParam),
      amount: parseCallbackAmount(amountParam, callbackPurpose),
      hasAuthenticationContext,
      isMalformed:
        callbackPurpose === null ||
        ['orderId', 'authKey', 'customerKey', 'amount', 'purpose'].some(
          (key) => searchParams.getAll(key).length > 1,
        ) ||
        (searchParams.has('orderId') && !toNonBlankString(orderIdParam)) ||
        (searchParams.has('authKey') && !toNonBlankString(authKeyParam)) ||
        (searchParams.has('customerKey') && !toNonBlankString(customerKeyParam)),
    };
  }
  const returnPlanId = toPositiveInteger(getSingleSearchParam(searchParams, 'returnPlanId'));
  const returnUserType = toUserType(getSingleSearchParam(searchParams, 'returnUserType'));
  const returnCycle = toBillingCycle(getSingleSearchParam(searchParams, 'returnBillingCycle'));
  const returnAmountRaw = getSingleSearchParam(searchParams, 'returnAmount');
  const returnAmount = toOptionalNonNegativeInteger(returnAmountRaw);
  const hasReturnAmountParam = searchParams.has('returnAmount');
  const hasAnyReturnContext = [
    'returnPlanId',
    'returnUserType',
    'returnBillingCycle',
    'returnAmount',
  ].some((key) => searchParams.has(key));
  const redirectHandledRef = useRef(false);
  const mutationSucceededRef = useRef(false);
  const recoveryInFlightRef = useRef(false);
  const recoveryVersionRef = useRef(0);
  const committedRef = useRef(false);

  const [plan, setPlan] = useState<SubscriptionPlan | null>(null);
  const [returnPlan, setReturnPlan] = useState<SubscriptionPlan | null>(null);
  const [paymentOrder, setPaymentOrder] = useState<BillingAgreementPrepareResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [requiresCompanyCertification, setRequiresCompanyCertification] = useState(false);
  const [prepareRetryVersion, setPrepareRetryVersion] = useState(0);
  const [canStartNewAttempt, setCanStartNewAttempt] = useState(false);
  const [recoveryState, setRecoveryState] = useState<RecoveryState>('CHECKING');

  useLayoutEffect(() => {
    if (!isRedirect || (!searchParams.has('authKey') && !searchParams.has('customerKey'))) {
      return;
    }
    const sanitized = new URLSearchParams(location.search);
    sanitized.delete('authKey');
    sanitized.delete('customerKey');
    const query = sanitized.toString();
    navigate(`${location.pathname}${query ? `?${query}` : ''}${location.hash}`, {
      replace: true,
    });
  }, [isRedirect, location.hash, location.pathname, location.search, navigate, searchParams]);

  const completeCommitted = useCallback(() => {
    if (committedRef.current) return;
    committedRef.current = true;
    showToast(
      'success',
      callbackPurpose === 'BILLING_AGREEMENT'
        ? '\uACB0\uC81C\uC218\uB2E8\uC774 \uB2E4\uC2DC \uB4F1\uB85D\uB418\uC5C8\uC2B5\uB2C8\uB2E4.'
        : '\uC790\uB3D9\uACB0\uC81C\uAC00 \uB4F1\uB85D\uB418\uACE0 \uAD6C\uB3C5\uC774 \uC2DC\uC791\uB418\uC5C8\uC2B5\uB2C8\uB2E4.',
    );
    const returnUrl =
      returnUserType === authenticatedUserType
        ? buildReturnUrl(returnPlanId, returnUserType, returnCycle)
        : null;
    navigate(returnUrl ?? '/subscriptions/manage', { replace: true });
  }, [
    authenticatedUserType,
    callbackPurpose,
    navigate,
    returnCycle,
    returnPlanId,
    returnUserType,
    showToast,
  ]);

  const canonicalStateMatches = useCallback(async (outcome: PaymentCommandOutcome) => {
    const [subscription, agreement] = await Promise.all([
      fetchMySubscription(),
      fetchMyBillingAgreement(),
    ]);
    const subscriptionStatusMatches =
      outcome.purpose === 'SUBSCRIBE'
        ? subscription.status === 'ACTIVE'
        : subscription.status === 'ACTIVE' || subscription.status === 'CANCELLED';
    const agreementSubscriptionMatches = Boolean(
      agreement.subscription &&
      outcome.userSubscriptionId !== null &&
      outcome.userSubscriptionId === subscription.id &&
      outcome.userSubscriptionId === agreement.subscription.id &&
      agreement.subscription.id === subscription.id &&
      agreement.subscription.subscription.id === outcome.targetSubscriptionId &&
      agreement.subscription.billingCycle === outcome.targetBillingCycle,
    );
    return (
      agreement.status === 'ACTIVE' &&
      agreementSubscriptionMatches &&
      subscriptionStatusMatches &&
      subscription.subscription.id === outcome.targetSubscriptionId &&
      subscription.billingCycle === outcome.targetBillingCycle
    );
  }, []);

  const reconcileCallback = useCallback(
    async (orderId: string) => {
      if (recoveryInFlightRef.current || committedRef.current) return;
      recoveryInFlightRef.current = true;
      const version = ++recoveryVersionRef.current;
      setLoading(true);
      setRecoveryState('CHECKING');
      try {
        const outcome = await fetchPaymentCommandOutcome(orderId);
        if (version !== recoveryVersionRef.current) return;
        if (outcome.purpose !== callbackPurpose) {
          setRecoveryState('UNKNOWN');
          setErrorMessage(UNKNOWN_MESSAGE);
          return;
        }
        if (['FAILED', 'CANCELLED', 'EXPIRED'].includes(outcome.orderStatus)) {
          setRecoveryState('FAILED');
          setErrorMessage(FAILED_MESSAGE);
          return;
        }
        if (outcome.orderStatus !== 'DONE') {
          setRecoveryState('UNKNOWN');
          setErrorMessage(UNKNOWN_MESSAGE);
          return;
        }
        try {
          if (await canonicalStateMatches(outcome)) {
            if (version === recoveryVersionRef.current) completeCommitted();
            return;
          }
          setRecoveryState('UNKNOWN');
          setErrorMessage(UNKNOWN_MESSAGE);
        } catch {
          const state = mutationSucceededRef.current ? 'RELOAD_FAILED' : 'UNKNOWN';
          setRecoveryState(state);
          setErrorMessage(state === 'RELOAD_FAILED' ? RELOAD_FAILED_MESSAGE : UNKNOWN_MESSAGE);
        }
      } catch {
        const state = mutationSucceededRef.current ? 'RELOAD_FAILED' : 'UNKNOWN';
        setRecoveryState(state);
        setErrorMessage(state === 'RELOAD_FAILED' ? RELOAD_FAILED_MESSAGE : UNKNOWN_MESSAGE);
      } finally {
        if (version === recoveryVersionRef.current && !committedRef.current) setLoading(false);
        recoveryInFlightRef.current = false;
      }
    },
    [callbackPurpose, canonicalStateMatches, completeCommitted],
  );

  useEffect(() => {
    if (!isRedirect || redirectHandledRef.current) return;
    redirectHandledRef.current = true;

    async function handleRedirect() {
      setLoading(true);
      setErrorMessage(null);

      const callbackContext = callbackContextRef.current;
      const orderId = callbackContext?.orderId ?? null;
      if (!callbackContext || callbackContext.isMalformed) {
        setRecoveryState('UNKNOWN');
        setErrorMessage(INVALID_CALLBACK_CONTEXT_MESSAGE);
        showToast('error', INVALID_CALLBACK_CONTEXT_MESSAGE);
        setLoading(false);
        return;
      }
      if (isFailRedirect) {
        if (orderId) {
          await reconcileCallback(orderId);
        } else {
          setRecoveryState('UNKNOWN');
          setErrorMessage(UNKNOWN_MESSAGE);
          setLoading(false);
        }
        return;
      }

      const authKey = callbackContext?.authKey ?? null;
      const customerKey = callbackContext?.customerKey ?? null;
      const amount = callbackContext?.amount ?? null;
      if (!orderId) {
        setErrorMessage(INVALID_CALLBACK_CONTEXT_MESSAGE);
        showToast('error', INVALID_CALLBACK_CONTEXT_MESSAGE);
        setLoading(false);
        return;
      }

      if (!callbackContext.hasAuthenticationContext) {
        await reconcileCallback(orderId);
        return;
      }

      if (!authKey || !customerKey || amount === null) {
        setErrorMessage(INVALID_CALLBACK_CONTEXT_MESSAGE);
        showToast('error', INVALID_CALLBACK_CONTEXT_MESSAGE);
        setLoading(false);
        return;
      }

      try {
        const response = await confirmBillingAgreement({
          orderId,
          authKey,
          customerKey,
          amount,
        });
        mutationSucceededRef.current = response.orderStatus === 'DONE';
        await reconcileCallback(orderId);
      } catch {
        await reconcileCallback(orderId);
      }
    }

    void handleRedirect();
  }, [isFailRedirect, callbackPurpose, isRedirect, reconcileCallback, searchParams, showToast]);

  useEffect(() => {
    if (isRedirect) return;
    let active = true;

    async function loadAndPrepare() {
      setLoading(true);
      setErrorMessage(null);
      setRequiresCompanyCertification(false);
      setCanStartNewAttempt(false);
      setPaymentOrder(null);
      setPlan(null);
      setReturnPlan(null);

      try {
        const hasInvalidReturnContext =
          hasAnyReturnContext &&
          (routePurpose !== 'BILLING_AGREEMENT' ||
            !returnPlanId ||
            !returnUserType ||
            !returnCycle ||
            returnUserType !== routeUserType ||
            (hasReturnAmountParam && returnAmount === null));
        if (
          !routePlanId ||
          !routeUserType ||
          !routeCycle ||
          !routePurpose ||
          routeUserType !== authenticatedUserType ||
          hasInvalidReturnContext
        ) {
          const message =
            purposeParam === 'UPGRADE' ? UPGRADE_ROUTE_MESSAGE : INVALID_CHECKOUT_CONTEXT_MESSAGE;
          setErrorMessage(message);
          showToast('error', message);
          return;
        }

        const plans = await fetchSubscriptionPlans(routeUserType);
        const found = plans.find((candidate) => candidate.id === routePlanId);
        if (!active) return;

        const foundReturnPlan = hasAnyReturnContext
          ? plans.find((candidate) => candidate.id === returnPlanId)
          : null;
        if (
          !found ||
          found.userType !== routeUserType ||
          (hasAnyReturnContext && (!foundReturnPlan || foundReturnPlan.userType !== returnUserType))
        ) {
          setErrorMessage(INVALID_CHECKOUT_CONTEXT_MESSAGE);
          showToast('error', INVALID_CHECKOUT_CONTEXT_MESSAGE);
          return;
        }
        setPlan(found);
        setReturnPlan(foundReturnPlan ?? null);

        const idempotencyKey = getOrCreateCheckoutPrepareAttempt({
          planId: found.id,
          userType: routeUserType,
          billingCycle: routeCycle,
          purpose: routePurpose,
        });
        const prepared = await prepareBillingAgreement(
          {
            subscriptionId: found.id,
            billingCycle: routeCycle,
            purpose: routePurpose,
          },
          idempotencyKey,
        );
        if (!active) return;
        const expectedAmount =
          routePurpose === 'SUBSCRIBE'
            ? routeCycle === 'YEARLY'
              ? found.priceYearly
              : found.priceMonthly
            : 0;
        if (!isValidPreparedOrder(prepared, found.id, routeCycle, routePurpose, expectedAmount)) {
          setErrorMessage(INVALID_PREPARE_RESPONSE_MESSAGE);
          showToast('error', INVALID_PREPARE_RESPONSE_MESSAGE);
          return;
        }
        setPaymentOrder(prepared);
      } catch (err: unknown) {
        if (!active) return;
        const errorCode = await getApiErrorCode(err);
        const requiresCertification = errorCode === 'COMPANY_CERTIFICATION_REQUIRED';
        const msg = requiresCertification
          ? '기업 인증 승인 후 기업용 구독 결제를 진행할 수 있습니다.'
          : PREPARE_FAILED_MESSAGE;
        setRequiresCompanyCertification(requiresCertification);
        setCanStartNewAttempt(
          err instanceof CorruptCheckoutPrepareAttemptError ||
            isNewCheckoutPrepareAttemptRequired(errorCode),
        );
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
  }, [
    authenticatedUserType,
    hasAnyReturnContext,
    hasReturnAmountParam,
    isRedirect,
    purposeParam,
    returnAmount,
    returnAmountRaw,
    returnCycle,
    returnPlanId,
    returnUserType,
    routeCycle,
    routePlanId,
    routePurpose,
    routeUserType,
    showToast,
    prepareRetryVersion,
  ]);

  const retryPrepare = () => {
    setPrepareRetryVersion((version) => version + 1);
  };

  const startNewPrepareAttempt = () => {
    if (!plan || !routePurpose || !routeUserType || !routeCycle) return;
    try {
      createNewCheckoutPrepareAttempt({
        planId: plan.id,
        userType: routeUserType,
        billingCycle: routeCycle,
        purpose: routePurpose,
      });
      setPrepareRetryVersion((version) => version + 1);
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : '새 결제 시도를 시작할 수 없습니다.';
      setErrorMessage(message);
      showToast('error', message);
    }
  };

  const handleConfirm = async () => {
    if (!paymentOrder || submitting) return;
    setSubmitting(true);
    try {
      const { clientKey, customerKey, successUrl, failUrl, method } = paymentOrder.checkout;
      if (!clientKey || !customerKey || !successUrl || !failUrl || method !== 'CARD') {
        throw new Error('Toss 자동결제 설정이 아직 준비되지 않았습니다.');
      }
      const TossPayments = await loadTossPaymentsSdk();
      const payment = TossPayments(clientKey).payment({ customerKey });
      await payment.requestBillingAuth({
        method,
        successUrl: withBillingQuery(
          successUrl,
          paymentOrder.orderId,
          paymentOrder.amount,
          paymentOrder.purpose,
          { returnPlanId, returnUserType, returnCycle, returnAmount },
        ),
        failUrl: withBillingQuery(
          failUrl,
          paymentOrder.orderId,
          paymentOrder.amount,
          paymentOrder.purpose,
          {
            returnPlanId,
            returnUserType,
            returnCycle,
            returnAmount,
          },
        ),
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
        <div className={styles.loading} role="status">
          {isSuccessRedirect
            ? '자동결제 등록을 확인하는 중입니다.'
            : '결제 정보를 불러오는 중입니다.'}
        </div>
      </div>
    );
  }

  if (isRedirect) {
    return (
      <div className={styles.page}>
        <h1 className={styles.pageTitle}>{isFailRedirect ? '결제 상태 확인' : '자동결제 확인'}</h1>
        <div className={styles.pgNotice} role="alert">
          <div className={styles.pgText}>
            {errorMessage ?? '결제 상태를 확인했습니다. 다시 시도할 수 있습니다.'}
          </div>
        </div>
        <div className={styles.btnGroup}>
          {(recoveryState === 'RELOAD_FAILED' || recoveryState === 'UNKNOWN') && (
            <button
              className={styles.btnPay}
              onClick={() => {
                const orderId = callbackContextRef.current?.orderId;
                if (orderId) void reconcileCallback(orderId);
              }}
            >
              {'\uC0C1\uD0DC \uB2E4\uC2DC \uD655\uC778'}
            </button>
          )}
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
        <div className={styles.error} role="alert">
          {errorMessage ?? '선택한 플랜이 없습니다. '}
          <button className={styles.btnBack} onClick={() => navigate('/subscriptions')}>
            {'플랜 선택으로 돌아가기'}
          </button>
        </div>
      </div>
    );
  }

  const isBillingAgreementOnly = paymentOrder?.purpose === 'BILLING_AGREEMENT';
  const paymentAmount = paymentOrder?.amount ?? null;
  const monthlyEquiv =
    paymentOrder?.purpose === 'SUBSCRIBE' && paymentOrder.billingCycle === 'YEARLY'
      ? formatPrice(Math.floor(plan.priceYearly / 12))
      : null;
  const canConfirm = Boolean(paymentOrder) && !submitting;
  const returnCycleLabel = returnCycle ? getBillingCycleLabel(returnCycle) : '';
  const hasReturnContext =
    isBillingAgreementOnly && Boolean(returnPlan && returnUserType && returnCycle);

  return (
    <div className={styles.page}>
      <h1 className={styles.pageTitle}>
        {paymentOrder ? (isBillingAgreementOnly ? '결제수단 등록' : '구독 결제') : '결제 준비'}
      </h1>

      <div className={styles.card}>
        <div className={styles.cardTitle}>
          {paymentOrder
            ? isBillingAgreementOnly
              ? '현재 구독 결제수단'
              : '선택한 플랜'
            : '결제 대상'}
        </div>
        <div className={styles.row}>
          <span className={styles.label}>{'플랜'}</span>
          <span className={styles.value}>{plan.name}</span>
        </div>
        <div className={styles.row}>
          <span className={styles.label}>{'결제 주기'}</span>
          <span className={styles.value}>
            {paymentOrder ? (paymentOrder.billingCycle === 'MONTHLY' ? '월간' : '연간') : '-'}
          </span>
        </div>
        <div className={styles.row}>
          <span className={styles.label}>{'일 다운로드 한도'}</span>
          <span className={styles.value}>
            {plan.downloadPerDay === -1 ? '무제한' : `${plan.downloadPerDay}곡`}
          </span>
        </div>
        <div className={styles.row}>
          <span className={styles.label}>
            {paymentOrder
              ? isBillingAgreementOnly
                ? '등록 시 결제 금액'
                : '첫 결제 금액'
              : '결제 금액'}
          </span>
          <span className={styles.total}>
            {paymentOrder
              ? isBillingAgreementOnly
                ? '즉시 결제 없음'
                : formatPrice(paymentAmount ?? 0)
              : INVALID_PREPARE_RESPONSE_MESSAGE}
            {paymentOrder && !isBillingAgreementOnly && monthlyEquiv && ` (${monthlyEquiv}/월)`}
          </span>
        </div>
      </div>

      {hasReturnContext && (
        <div className={styles.card}>
          <div className={styles.cardTitle}>{'등록 후 이어갈 플랜 변경'}</div>
          <div className={styles.row}>
            <span className={styles.label}>{'변경 대상'}</span>
            <span className={styles.value}>{returnPlan?.name}</span>
          </div>
          <div className={styles.row}>
            <span className={styles.label}>{'변경 결제 주기'}</span>
            <span className={styles.value}>{returnCycleLabel}</span>
          </div>
          <div className={styles.row}>
            <span className={styles.label}>{'등록 후 결제 예정 차액'}</span>
            <span className={styles.value}>
              {returnAmount === null ? '내 구독에서 다시 확인' : formatPrice(returnAmount)}
            </span>
          </div>
        </div>
      )}

      <div className={styles.pgNotice}>
        <div className={styles.pgText}>
          {isBillingAgreementOnly
            ? 'Toss 테스트 환경에서 결제수단만 다시 등록합니다. 이 단계에서는 플랜 변경 차액이 결제되지 않으며, 등록 후 내 구독 화면에서 변경을 확정합니다.'
            : 'Toss 테스트 환경에서 자동결제 수단을 등록하고, 서버가 빌링키 발급과 첫 결제를 확인한 뒤에만 구독이 시작됩니다.'}
          <br />
          {'authKey, customerKey, billingKey, 카드 원문 정보는 화면에 표시하지 않습니다.'}
        </div>
      </div>

      <div className={styles.providerPanel}>
        <div className={styles.providerHeader}>
          <span className={styles.providerTitle}>{'Toss 자동결제'}</span>
          <span className={styles.providerStatus}>
            {paymentOrder ? '준비 완료' : errorMessage ? '오류' : '준비 중'}
          </span>
        </div>
        {paymentOrder ? (
          <div className={styles.providerMeta}>
            <span>{paymentOrder.orderId}</span>
            <span>{paymentOrder.provider}</span>
          </div>
        ) : (
          <div className={styles.providerMeta}>
            {errorMessage ? '결제 주문 준비에 실패했습니다.' : '결제 주문을 준비 중입니다.'}
          </div>
        )}
        {errorMessage && <div className={styles.providerError}>{errorMessage}</div>}
      </div>

      <div className={styles.btnGroup}>
        <button className={styles.btnBack} onClick={() => navigate('/subscriptions')}>
          {'돌아가기'}
        </button>
        {requiresCompanyCertification && (
          <button
            className={styles.btnBack}
            onClick={() => navigate('/company-certification/status')}
          >
            {'기업 인증 관리'}
          </button>
        )}
        {!paymentOrder && errorMessage && !requiresCompanyCertification && !canStartNewAttempt && (
          <button className={styles.btnBack} onClick={retryPrepare}>
            {'결제 준비 다시 시도'}
          </button>
        )}
        {!paymentOrder && canStartNewAttempt && (
          <button className={styles.btnPay} onClick={startNewPrepareAttempt}>
            {'새 결제 시도 시작'}
          </button>
        )}
        <button className={styles.btnPay} onClick={handleConfirm} disabled={!canConfirm}>
          {submitting
            ? '처리 중...'
            : isBillingAgreementOnly
              ? '카드 등록하기'
              : '결제수단 등록 및 첫 결제하기'}
        </button>
      </div>
    </div>
  );
}

function parseCallbackAmount(
  rawAmount: string | null,
  purpose: BillingPurpose | null,
): number | null {
  if (!purpose || !rawAmount || !/^\d+$/.test(rawAmount)) return null;
  const amount = Number(rawAmount);
  if (!Number.isSafeInteger(amount)) return null;
  if (purpose === 'BILLING_AGREEMENT') return amount;
  return amount > 0 ? amount : null;
}

function getSingleSearchParam(searchParams: URLSearchParams, key: string): string | null {
  const values = searchParams.getAll(key);
  return values.length === 1 ? values[0] : null;
}

function toNonBlankString(value: string | null): string | null {
  return value && value.trim().length > 0 ? value : null;
}

interface BillingReturnContext {
  returnPlanId: number | null;
  returnUserType: UserType | null;
  returnCycle: BillingCycle | null;
  returnAmount: number | null;
}

function withBillingQuery(
  url: string,
  orderId: string,
  amount: number,
  purpose: BillingPurpose,
  context: BillingReturnContext,
): string {
  const next = new URL(url, window.location.origin);
  next.searchParams.set('orderId', orderId);
  next.searchParams.set('amount', String(amount));
  if (purpose !== 'SUBSCRIBE') {
    next.searchParams.set('purpose', purpose);
  }
  if (context.returnPlanId && context.returnUserType && context.returnCycle) {
    next.searchParams.set('returnPlanId', String(context.returnPlanId));
    next.searchParams.set('returnUserType', context.returnUserType);
    next.searchParams.set('returnBillingCycle', context.returnCycle);
    if (context.returnAmount !== null) {
      next.searchParams.set('returnAmount', String(context.returnAmount));
    }
  }
  return next.toString();
}

function toBillingCycle(value: string | null): BillingCycle | null {
  return value === 'MONTHLY' || value === 'YEARLY' ? value : null;
}

function toBillingPurpose(value: string | null): BillingPurpose | null {
  return value === 'SUBSCRIBE' || value === 'BILLING_AGREEMENT' ? value : null;
}

function toUserType(value: string | null): UserType | null {
  return value === 'INDIVIDUAL' || value === 'BUSINESS' ? value : null;
}

function toPositiveInteger(value: string | null): number | null {
  if (!value || !/^\d+$/.test(value)) return null;
  const parsed = Number(value);
  return Number.isSafeInteger(parsed) && parsed > 0 ? parsed : null;
}

function toOptionalNonNegativeInteger(value: string | null): number | null {
  if (value === null) return null;
  if (!/^\d+$/.test(value)) return null;
  const parsed = Number(value);
  return Number.isSafeInteger(parsed) ? parsed : null;
}

function isValidPreparedOrder(
  response: BillingAgreementPrepareResponse,
  subscriptionId: number,
  billingCycle: BillingCycle,
  purpose: BillingPurpose,
  expectedAmount: number,
): boolean {
  const checkout = response.checkout;
  return (
    isNonBlank(response.orderId) &&
    response.provider === 'TOSS' &&
    response.purpose === purpose &&
    response.agreementStatus === 'READY' &&
    response.subscriptionId === subscriptionId &&
    response.billingCycle === billingCycle &&
    Number.isSafeInteger(response.amount) &&
    response.amount === expectedAmount &&
    response.currency === 'KRW' &&
    isUsableExpiry(response.expiresAt) &&
    checkout?.type === 'TOSS_BILLING_AUTH' &&
    checkout.method === 'CARD' &&
    isNonBlank(checkout.clientKey) &&
    isNonBlank(checkout.customerKey) &&
    isUsableCheckoutUrl(checkout.successUrl) &&
    isUsableCheckoutUrl(checkout.failUrl)
  );
}

function isNonBlank(value: unknown): value is string {
  return typeof value === 'string' && value.trim().length > 0;
}

function isUsableExpiry(value: unknown): value is string {
  return isNonBlank(value) && Number.isFinite(Date.parse(value));
}

function isUsableCheckoutUrl(value: unknown): value is string {
  if (!isNonBlank(value)) return false;
  try {
    const parsed = new URL(value);
    return parsed.protocol === 'http:' || parsed.protocol === 'https:';
  } catch {
    return false;
  }
}

function getBillingCycleLabel(cycle: 'MONTHLY' | 'YEARLY'): string {
  return cycle === 'MONTHLY' ? '월간' : '연간';
}

function buildReturnUrl(
  planId: number | null,
  userType: UserType | null,
  cycle: BillingCycle | null,
): string | null {
  if (!planId || !userType || !cycle) return null;
  const params = new URLSearchParams({
    planId: String(planId),
    userType,
    billingCycle: cycle,
  });
  return `/subscriptions/manage?${params.toString()}`;
}

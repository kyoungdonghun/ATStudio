import { useState, useEffect, useCallback } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { useAuthStore } from '@/store/authStore';
import {
  fetchMySubscription,
  cancelMySubscription,
  reactivateMySubscription,
  changeMySubscription,
  fetchSubscriptionChangePreview,
  type MySubscription,
  type SubscriptionChangePreview,
  type SubscriptionChangeType,
} from '@/api/userSubscriptions';
import { fetchSubscriptionPlans, type SubscriptionPlan } from '@/api/subscriptions';
import { fetchMyBillingAgreement, type BillingAgreementResponse } from '@/api/payments';
import { getApiErrorCode, isSubscriptionRequired } from '@/api/client';
import { formatDate } from '@/utils/format';
import Button from '@/components/ui/Button';
import Modal from '@/components/ui/Modal';
import styles from './SubscriptionManagePage.module.css';

type BillingCycle = 'MONTHLY' | 'YEARLY';

/** Format amount with comma separator */
function formatAmount(amount: number): string {
  return amount.toLocaleString('ko-KR');
}

/** Plan display name mapping */
function getDisplayName(name: string): string {
  switch (name) {
    case 'STANDARD':
      return '스탠다드';
    case 'DELUXE':
      return '\uB514\uB7ED\uC2A4';
    case 'PREMIUM':
      return '\uD504\uB9AC\uBBF8\uC5C4';
    default:
      return name;
  }
}

function getBillingCycleLabel(cycle: BillingCycle): string {
  return cycle === 'MONTHLY' ? '월간' : '연간';
}

function getChangeTypeLabel(type: SubscriptionChangeType): string {
  switch (type) {
    case 'UPGRADE':
      return '오늘 변경';
    case 'NO_CHANGE':
      return '현재 플랜 유지';
    case 'SCHEDULED_CHANGE':
    case 'DOWNGRADE':
      return '다음 결제일 변경';
    default:
      return type;
  }
}

function getConfirmButtonLabel(
  type: SubscriptionChangeType,
  hasPendingChange: boolean,
  isCycleOnlyChange: boolean,
): string {
  switch (type) {
    case 'NO_CHANGE':
      return hasPendingChange ? '예약 취소하고 현재 플랜 유지' : '변경할 항목 없음';
    case 'SCHEDULED_CHANGE':
    case 'DOWNGRADE':
      return isCycleOnlyChange ? '다음 결제일부터 주기 변경' : '다음 결제일부터 변경 예약';
    default:
      return '차액 결제 후 변경';
  }
}

function getPreviewSummary(type: SubscriptionChangeType, hasPendingChange: boolean): string {
  switch (type) {
    case 'UPGRADE':
      return '남은 기간 차액을 오늘 결제하고 플랜은 바로 변경됩니다.';
    case 'NO_CHANGE':
      return hasPendingChange
        ? '예약된 변경을 취소하고 현재 플랜과 결제 주기를 유지합니다.'
        : '현재 이용 중인 플랜과 결제 주기입니다.';
    case 'SCHEDULED_CHANGE':
    case 'DOWNGRADE':
      return '오늘 결제 없이 다음 결제일부터 변경됩니다.';
    default:
      return '변경 내용을 확인한 뒤 적용할 수 있습니다.';
  }
}

function getPlanPrice(plan: SubscriptionPlan, cycle: BillingCycle): string {
  const amount = cycle === 'MONTHLY' ? plan.priceMonthly : plan.priceYearly;
  const suffix = cycle === 'MONTHLY' ? '/월' : '/년';
  return `₩${formatAmount(amount)}${suffix}`;
}

function getPlanPriceNote(plan: SubscriptionPlan, cycle: BillingCycle): string | null {
  if (cycle === 'MONTHLY') return null;
  return `월 ₩${formatAmount(Math.floor(plan.priceYearly / 12))} 수준`;
}

function getApiErrorMessage(err: unknown, fallback: string): string {
  return (
    (err as { response?: { data?: { message?: string } } })?.response?.data?.message ??
    (err instanceof Error ? err.message : null) ??
    fallback
  );
}

function isReusableBillingAgreement(
  agreement: BillingAgreementResponse | null,
  subscriptionStatus?: string,
): boolean {
  if (!agreement) return false;
  if (agreement.status === 'ACTIVE') return true;
  return (
    subscriptionStatus === 'CANCELLED' &&
    agreement.status === 'CANCELLED' &&
    Boolean(agreement.maskedMethod)
  );
}

function requiresPaymentMethodRegistration(
  agreement: BillingAgreementResponse | null,
  hasSubscription: boolean,
): boolean {
  if (!hasSubscription) return false;
  if (!agreement) return true;
  if (agreement.status === 'READY') return true;
  if (agreement.status === 'EXPIRED' || agreement.status === 'SUSPENDED') return true;
  return agreement.status === 'CANCELLED' && !agreement.maskedMethod;
}

function getPaymentMethodLabel(agreement: BillingAgreementResponse): string {
  if (agreement.maskedMethod) {
    return `${agreement.payMethod ?? 'CARD'} ${agreement.maskedMethod}`;
  }
  if (agreement.status === 'ACTIVE') {
    return agreement.payMethod ?? '등록됨';
  }
  if (agreement.status === 'READY') {
    return '등록 미완료';
  }
  return '미등록';
}

function getPaymentRegistrationMessage(agreement: BillingAgreementResponse | null): string {
  if (!agreement) {
    return '등록된 자동결제 수단이 없습니다. 업그레이드와 다음 갱신을 진행하려면 결제수단을 등록해야 합니다.';
  }
  if (agreement.status === 'READY') {
    return '카드 등록이 완료되지 않았습니다. 결제수단 다시 등록을 눌러 Toss 카드 등록을 다시 시작해주세요.';
  }
  return '업그레이드와 다음 갱신을 진행하려면 Toss 자동결제 수단을 다시 등록해야 합니다. 현재 구독 기간과 플랜은 변경되지 않습니다.';
}

export default function SubscriptionManagePage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const role = useAuthStore((s) => s.role);

  useEffect(() => {
    if (role === 'ADMIN') navigate('/admin', { replace: true });
  }, [role, navigate]);

  /* ── State ── */
  const [sub, setSub] = useState<MySubscription | null>(null);
  const [plans, setPlans] = useState<SubscriptionPlan[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [billingAgreement, setBillingAgreement] = useState<BillingAgreementResponse | null>(null);

  /* ── Change Plan ── */
  const [selectedPlan, setSelectedPlan] = useState<SubscriptionPlan | null>(null);
  const [selectedCycle, setSelectedCycle] = useState<BillingCycle>('MONTHLY');
  const [preview, setPreview] = useState<SubscriptionChangePreview | null>(null);
  const [loadingPreview, setLoadingPreview] = useState(false);
  const [changingPlan, setChangingPlan] = useState(false);
  const [changeMsg, setChangeMsg] = useState<string | null>(null);
  const [changeError, setChangeError] = useState<string | null>(null);

  /* ── Cancel ── */
  const [showCancelModal, setShowCancelModal] = useState(false);
  const [cancelling, setCancelling] = useState(false);
  const [reactivating, setReactivating] = useState(false);
  const hasPendingChange = Boolean(sub?.pendingSubscriptionId || sub?.pendingBillingCycle);
  const needsPaymentMethodRegistration = requiresPaymentMethodRegistration(
    billingAgreement,
    Boolean(sub),
  );

  /* ── Fetch ── */
  const load = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const userType = useAuthStore.getState().user?.userType ?? 'INDIVIDUAL';
      const plansRes = await fetchSubscriptionPlans(userType);
      setPlans(plansRes);
      try {
        const subRes = await fetchMySubscription();
        setSub(subRes);
        try {
          const billingRes = await fetchMyBillingAgreement();
          setBillingAgreement(billingRes);
        } catch {
          setBillingAgreement(null);
        }
      } catch (err) {
        if (isSubscriptionRequired(err)) {
          setSub(null);
          setBillingAgreement(null);
          return;
        }
        throw err;
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : '구독 정보를 불러오지 못했습니다.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  /* ── Pre-select plan from URL params (from SubscriptionPlanPage) ── */
  useEffect(() => {
    const urlPlan = searchParams.get('plan');
    const urlCycle = searchParams.get('cycle');
    if (urlPlan && plans.length > 0 && sub && !selectedPlan) {
      const found = plans.find((p) => p.name.toUpperCase() === urlPlan.toUpperCase());
      const targetCycle =
        urlCycle === 'MONTHLY' || urlCycle === 'YEARLY' ? urlCycle : sub.billingCycle;
      const isCurrentCombination =
        found?.id === sub.subscription.id && targetCycle === sub.billingCycle && !hasPendingChange;
      if (found && !isCurrentCombination) {
        setSelectedPlan(found);
        setSelectedCycle(targetCycle);
      }
    }
  }, [plans, sub, searchParams, selectedPlan, hasPendingChange]);

  useEffect(() => {
    if (sub && !selectedPlan && !searchParams.get('plan')) {
      setSelectedCycle(sub.billingCycle);
    }
  }, [sub, selectedPlan, searchParams]);

  /* ── Preview change when plan/cycle selected ── */
  useEffect(() => {
    if (!selectedPlan || !sub) {
      setPreview(null);
      return;
    }

    let cancelled = false;

    async function loadPreview() {
      try {
        setLoadingPreview(true);
        const res = await fetchSubscriptionChangePreview(selectedPlan!.id, selectedCycle);
        if (!cancelled) setPreview(res);
      } catch {
        if (!cancelled) setPreview(null);
      } finally {
        if (!cancelled) setLoadingPreview(false);
      }
    }

    loadPreview();
    return () => {
      cancelled = true;
    };
  }, [selectedPlan, selectedCycle, sub]);

  /* ── Change subscription ── */
  function handleSelectPlan(plan: SubscriptionPlan) {
    const isCurrentPlan = sub?.subscription.id === plan.id;
    const isCurrentCombination =
      isCurrentPlan && selectedCycle === sub?.billingCycle && !hasPendingChange;
    if (isCurrentCombination) {
      return;
    }

    setChangeMsg(null);
    setChangeError(null);
    setSelectedPlan(plan);
    if (sub && isCurrentPlan && hasPendingChange) {
      setSelectedCycle(sub.billingCycle);
      return;
    }
    if (plan.id === sub?.pendingSubscriptionId && sub?.pendingBillingCycle) {
      setSelectedCycle(sub.pendingBillingCycle);
    }
  }

  function handleSelectCycle(cycle: BillingCycle) {
    setSelectedCycle(cycle);
    if (
      sub &&
      selectedPlan?.id === sub.subscription.id &&
      cycle === sub.billingCycle &&
      !hasPendingChange
    ) {
      setSelectedPlan(null);
      setPreview(null);
    }
  }

  function handleRegisterPaymentMethod() {
    if (!sub) return;
    navigate(
      `/subscriptions/checkout?plan=${encodeURIComponent(sub.subscription.name)}&cycle=${sub.billingCycle}&purpose=BILLING_AGREEMENT`,
    );
  }

  async function handleChangePlan() {
    if (!selectedPlan || !preview) return;
    const hasReusablePaymentMethod = isReusableBillingAgreement(billingAgreement, sub?.status);
    if (preview.changeType === 'UPGRADE' && !hasReusablePaymentMethod) {
      setChangeError(
        '업그레이드를 적용하려면 자동결제 수단 등록이 먼저 필요합니다. 결제수단을 다시 등록한 뒤 플랜 변경을 진행해주세요.',
      );
      return;
    }
    try {
      setChangingPlan(true);
      setChangeError(null);
      setChangeMsg(null);

      const res = await changeMySubscription({
        subscriptionId: selectedPlan.id,
        billingCycle: selectedCycle,
      });

      const reactivatedPrefix = sub?.status === 'CANCELLED' ? '구독 취소가 철회되었습니다. ' : '';

      if (preview.changeType === 'UPGRADE') {
        const chargeMessage =
          res.proratedAmount > 0
            ? `차액 ${formatAmount(res.proratedAmount)}원이 등록된 결제수단으로 결제되었고,`
            : '즉시 결제할 차액은 없고,';
        const nextCycleMessage =
          selectedCycle !== sub?.billingCycle
            ? ` 다음 결제일부터 ${getBillingCycleLabel(selectedCycle)} 결제로 전환됩니다.`
            : '';
        setChangeMsg(
          `${reactivatedPrefix}업그레이드가 적용되었습니다. ${chargeMessage} 다음 결제일(${formatDate(res.expiresAt)})은 유지됩니다.${nextCycleMessage}`,
        );
      } else if (preview.changeType === 'NO_CHANGE') {
        setChangeMsg(
          hasPendingChange
            ? `${reactivatedPrefix}예약된 플랜 변경이 해제되었습니다. 현재 플랜이 유지됩니다.`
            : `${reactivatedPrefix}현재 플랜이 유지됩니다.`,
        );
      } else {
        setChangeMsg(
          `${reactivatedPrefix}변경이 예약되었습니다. 현재 구독 만료 후 ${getDisplayName(res.subscription.name)} 플랜이 적용됩니다.`,
        );
      }
      setSelectedPlan(null);
      setPreview(null);
      await load();
    } catch (err) {
      const errorCode = await getApiErrorCode(err);
      const message = getApiErrorMessage(err, '플랜 변경에 실패했습니다.');
      if (errorCode === 'BILLING_AGREEMENT_REAUTH_REQUIRED') {
        setChangeError(`${message} 현재 구독은 그대로 유지됩니다.`);
        await load();
        return;
      }
      setChangeError(message);
    } finally {
      setChangingPlan(false);
    }
  }

  /* ── Cancel subscription ── */
  async function handleCancel() {
    try {
      setCancelling(true);
      await cancelMySubscription();
      setShowCancelModal(false);
      await load();
    } catch (err) {
      setError(err instanceof Error ? err.message : '구독 취소에 실패했습니다.');
    } finally {
      setCancelling(false);
    }
  }

  async function handleReactivate() {
    try {
      setReactivating(true);
      setChangeError(null);
      await reactivateMySubscription();
      setChangeMsg('구독 취소가 철회되었습니다. 다음 결제일부터 자동 갱신이 다시 진행됩니다.');
      await load();
    } catch (err) {
      setError(err instanceof Error ? err.message : '구독 취소 철회에 실패했습니다.');
    } finally {
      setReactivating(false);
    }
  }

  /* ── Status badge ── */
  function getStatusClass(status: string): string {
    switch (status) {
      case 'ACTIVE':
        return styles.statusActive;
      case 'CANCELLED':
        return styles.statusCancelled;
      default:
        return styles.statusExpired;
    }
  }

  function getStatusLabel(status: string): string {
    switch (status) {
      case 'ACTIVE':
        return '활성';
      case 'CANCELLED':
        return '취소됨 (유예 중)';
      default:
        return '만료';
    }
  }

  /* ── Render ── */

  if (loading) {
    return (
      <div className={styles.page}>
        <div className={styles.loading}>{'구독 정보를 불러오는 중...'}</div>
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

  if (!sub) {
    return (
      <div className={styles.page}>
        <h1 className={styles.pageTitle}>{'내 구독'}</h1>
        <div className={styles.noSub}>
          {'현재 활성 구독이 없습니다. '}
          <Link to="/subscriptions" className={styles.noSubLink}>
            {'구독 플랜 보기'}
          </Link>
        </div>
      </div>
    );
  }

  const pendingPlan =
    !sub.pendingSubscriptionId || sub.pendingSubscriptionId === sub.subscription.id
      ? sub.subscription
      : plans.find((plan) => plan.id === sub.pendingSubscriptionId);
  const pendingPlanName = pendingPlan ? getDisplayName(pendingPlan.name) : '선택한 플랜';
  const pendingCycleLabel = sub.pendingBillingCycle
    ? getBillingCycleLabel(sub.pendingBillingCycle)
    : null;
  const pendingChangeText = pendingCycleLabel
    ? `다음 결제일부터 ${pendingPlanName} (${pendingCycleLabel})이 적용됩니다.`
    : `다음 결제일부터 ${pendingPlanName}이 적용됩니다.`;
  const activePlans = [...plans]
    .filter((p) => p.isActive)
    .sort((a, b) => a.priceMonthly - b.priceMonthly);
  const isCycleOnlyChange = Boolean(
    selectedPlan && selectedPlan.id === sub.subscription.id && selectedCycle !== sub.billingCycle,
  );
  const upgradeRequiresPaymentMethodRegistration = Boolean(
    preview?.changeType === 'UPGRADE' && !isReusableBillingAgreement(billingAgreement, sub.status),
  );

  return (
    <div className={styles.page}>
      <h1 className={styles.pageTitle}>{'내 구독'}</h1>

      {/* ── Current Plan Card ── */}
      <div className={styles.planCard}>
        <div className={styles.planHeader}>
          <div className={styles.planName}>{getDisplayName(sub.subscription.name)}</div>
          <span className={getStatusClass(sub.status)}>{getStatusLabel(sub.status)}</span>
        </div>
        <div className={styles.planInfo}>
          <div className={styles.infoItem}>
            <span className={styles.infoLabel}>{'결제 주기'}</span>
            <span className={styles.infoValue}>{getBillingCycleLabel(sub.billingCycle)}</span>
          </div>
          <div className={styles.infoItem}>
            <span className={styles.infoLabel}>{'시작일'}</span>
            <span className={styles.infoValue}>{formatDate(sub.startedAt)}</span>
          </div>
          <div className={styles.infoItem}>
            <span className={styles.infoLabel}>{'만료일'}</span>
            <span className={styles.infoValue}>{formatDate(sub.expiresAt)}</span>
          </div>
        </div>

        {/* Pending change notice */}
        {hasPendingChange && (
          <div className={styles.pendingNotice}>
            <span className={styles.pendingIcon}>{'\u23F3'}</span>
            <span className={styles.pendingText}>{pendingChangeText}</span>
          </div>
        )}
      </div>

      {/* ── Payment Info ── */}
      <div className={styles.actionSection}>
        <div className={styles.actionTitle}>{'결제 정보'}</div>
        {billingAgreement ? (
          <>
            <div className={styles.planInfo}>
              <div className={styles.infoItem}>
                <span className={styles.infoLabel}>{'상태'}</span>
                <span className={styles.infoValue}>
                  {getBillingStatusLabel(billingAgreement.status)}
                </span>
              </div>
              <div className={styles.infoItem}>
                <span className={styles.infoLabel}>{'결제수단'}</span>
                <span className={styles.infoValue}>{getPaymentMethodLabel(billingAgreement)}</span>
              </div>
              <div className={styles.infoItem}>
                <span className={styles.infoLabel}>{'다음 결제일'}</span>
                <span className={styles.infoValue}>
                  {billingAgreement.nextBillingAt
                    ? formatDate(billingAgreement.nextBillingAt)
                    : '-'}
                </span>
              </div>
            </div>
            {sub.status === 'CANCELLED' && (
              <div className={styles.actionDesc}>
                {
                  '구독 취소 상태입니다. 만료일까지 이용할 수 있고, 구독을 유지하면 다음 결제가 다시 예약됩니다.'
                }
              </div>
            )}
          </>
        ) : (
          <div className={styles.actionDesc}>{'현재 등록된 정기결제 수단이 없습니다.'}</div>
        )}
        {needsPaymentMethodRegistration && (
          <>
            <div className={styles.actionDesc}>
              {getPaymentRegistrationMessage(billingAgreement)}
            </div>
            <div className={styles.actionButtons}>
              <Button variant="primary" onClick={handleRegisterPaymentMethod}>
                {'결제수단 다시 등록'}
              </Button>
            </div>
          </>
        )}
      </div>

      {/* ── Change Plan Section ── */}
      {(sub.status === 'ACTIVE' || sub.status === 'CANCELLED') && (
        <div className={styles.actionSection}>
          <div className={styles.actionTitle}>{'플랜 변경'}</div>
          <div className={styles.actionDesc}>
            {sub.status === 'CANCELLED'
              ? '플랜 변경을 확정하면 구독 취소가 철회됩니다. 업그레이드는 남은 기간 차액을 즉시 결제하고, 그 외 변경은 현재 구독 만료 후 적용됩니다.'
              : '업그레이드는 등록된 결제수단으로 남은 기간 차액을 즉시 결제한 뒤 적용됩니다. 그 외 변경은 현재 구독 만료 후 적용되며, 예약된 변경은 다시 바꿀 수 있습니다.'}
          </div>

          <div className={styles.cycleTabs} aria-label="결제 주기 선택">
            <Button
              variant={selectedCycle === 'MONTHLY' ? 'primary' : 'ghost'}
              size="sm"
              onClick={() => handleSelectCycle('MONTHLY')}
            >
              {getBillingCycleLabel('MONTHLY')}
            </Button>
            <Button
              variant={selectedCycle === 'YEARLY' ? 'primary' : 'ghost'}
              size="sm"
              onClick={() => handleSelectCycle('YEARLY')}
            >
              {getBillingCycleLabel('YEARLY')}
            </Button>
          </div>

          {/* Plan options */}
          <div className={styles.planGrid}>
            {activePlans.map((plan) => {
              const isCurrentPlan = plan.id === sub.subscription.id;
              const isPendingTarget = hasPendingChange && pendingPlan?.id === plan.id;
              const isSelected = selectedPlan?.id === plan.id;
              const isDisabled =
                isCurrentPlan && selectedCycle === sub.billingCycle && !hasPendingChange;
              const priceNote = getPlanPriceNote(plan, selectedCycle);
              const optionClass = [
                styles.planOption,
                isSelected ? styles.planOptionSelected : '',
                isCurrentPlan ? styles.planOptionCurrentCard : '',
                isPendingTarget ? styles.planOptionPendingCard : '',
              ]
                .filter(Boolean)
                .join(' ');

              return (
                <button
                  key={plan.id}
                  type="button"
                  className={optionClass}
                  onClick={() => handleSelectPlan(plan)}
                  disabled={isDisabled}
                  aria-pressed={isSelected}
                >
                  <div className={styles.planOptionTop}>
                    <div className={styles.planOptionName}>{getDisplayName(plan.name)}</div>
                    <div className={styles.planOptionBadges}>
                      {isCurrentPlan && (
                        <span className={styles.planOptionCurrent}>{'현재 이용 중'}</span>
                      )}
                      {isPendingTarget && (
                        <span className={styles.planOptionPending}>{'예약됨'}</span>
                      )}
                    </div>
                  </div>
                  <div className={styles.planOptionPrice}>{getPlanPrice(plan, selectedCycle)}</div>
                  {priceNote && <div className={styles.planOptionMeta}>{priceNote}</div>}
                  {isDisabled && (
                    <div className={styles.planOptionMeta}>{'현재 조합이 유지됩니다.'}</div>
                  )}
                </button>
              );
            })}
          </div>

          {/* Preview */}
          {loadingPreview && <div className={styles.loading}>{'변경 내역을 미리 보는 중...'}</div>}
          {preview && !loadingPreview && (
            <div className={styles.previewBox}>
              <div className={styles.previewHeader}>
                <span className={styles.previewTitle}>{'변경 미리보기'}</span>
                <span
                  className={
                    preview.changeType === 'UPGRADE'
                      ? styles.previewUpgrade
                      : preview.changeType === 'NO_CHANGE'
                        ? styles.previewNeutral
                        : styles.previewDowngrade
                  }
                >
                  {getChangeTypeLabel(preview.changeType)}
                </span>
              </div>

              <div className={styles.previewSummary}>
                {getPreviewSummary(preview.changeType, hasPendingChange)}
              </div>

              <div className={styles.previewGrid}>
                <div className={styles.previewItem}>
                  <span className={styles.previewLabel}>{'대상 플랜'}</span>
                  <span className={styles.previewValue}>{getDisplayName(preview.newPlanName)}</span>
                </div>
                <div className={styles.previewItem}>
                  <span className={styles.previewLabel}>{'결제 주기'}</span>
                  <span className={styles.previewValue}>
                    {getBillingCycleLabel(preview.newBillingCycle)}
                  </span>
                </div>
                <div className={styles.previewItem}>
                  <span className={styles.previewLabel}>{'오늘 결제'}</span>
                  <span className={styles.previewValue}>
                    {preview.changeType === 'UPGRADE'
                      ? `\u20A9${formatAmount(preview.proratedAmount)}`
                      : '없음'}
                  </span>
                </div>
                <div className={styles.previewItem}>
                  <span className={styles.previewLabel}>{'변경 적용일'}</span>
                  <span className={styles.previewValue}>{formatDate(preview.effectiveDate)}</span>
                </div>
                <div className={styles.previewItem}>
                  <span className={styles.previewLabel}>{'다음 결제일'}</span>
                  <span className={styles.previewValue}>{formatDate(preview.nextBillingDate)}</span>
                </div>
                <div className={styles.previewItem}>
                  <span className={styles.previewLabel}>{'다음 결제 금액'}</span>
                  <span className={styles.previewValue}>
                    {'\u20A9'}
                    {formatAmount(preview.nextBillingAmount)}
                  </span>
                </div>
              </div>
            </div>
          )}

          {preview?.changeType === 'UPGRADE' && upgradeRequiresPaymentMethodRegistration && (
            <div className={styles.errorMsg}>
              {
                '업그레이드를 적용하려면 자동결제 수단 등록이 먼저 필요합니다. 결제수단을 다시 등록한 뒤 플랜 변경을 진행해주세요.'
              }
            </div>
          )}

          {/* Confirm change button */}
          {selectedPlan && preview && !loadingPreview && (
            <div className={styles.actionButtons}>
              <Button
                variant="ghost"
                onClick={() => {
                  setSelectedPlan(null);
                  setPreview(null);
                }}
              >
                {'취소'}
              </Button>
              <Button
                variant="primary"
                onClick={
                  upgradeRequiresPaymentMethodRegistration
                    ? handleRegisterPaymentMethod
                    : handleChangePlan
                }
                loading={changingPlan}
              >
                {upgradeRequiresPaymentMethodRegistration
                  ? '결제수단 등록하기'
                  : getConfirmButtonLabel(preview.changeType, hasPendingChange, isCycleOnlyChange)}
              </Button>
            </div>
          )}

          {changeMsg && <div className={styles.successMsg}>{changeMsg}</div>}
          {changeError && <div className={styles.errorMsg}>{changeError}</div>}
        </div>
      )}

      {/* ── Cancel Subscription ── */}
      {sub.status === 'ACTIVE' && (
        <div className={styles.cancelSection}>
          <div className={styles.cancelTitle}>{'구독 취소'}</div>
          <div className={styles.cancelDesc}>
            {'구독을 취소하면 만료일('}
            {formatDate(sub.expiresAt)}
            {')까지는 서비스를 계속 이용할 수 있습니다. 이후 자동으로 만료됩니다.'}
          </div>
          <Button variant="danger" onClick={() => setShowCancelModal(true)}>
            {'구독 취소'}
          </Button>
        </div>
      )}

      {sub.status === 'CANCELLED' && (
        <div className={styles.actionSection}>
          <div className={styles.actionTitle}>{'구독 취소됨'}</div>
          <div className={styles.actionDesc}>
            {'구독은 '}
            {formatDate(sub.expiresAt)}
            {'까지 유지됩니다. 구독을 계속 사용하려면 취소를 철회할 수 있습니다.'}
          </div>
          <Button variant="primary" onClick={handleReactivate} loading={reactivating}>
            {'구독 유지하기'}
          </Button>
        </div>
      )}

      {/* ── Cancel Confirm Modal ── */}
      <Modal
        open={showCancelModal}
        onClose={() => setShowCancelModal(false)}
        title="구독 취소 확인"
      >
        <div className={styles.modalBody}>
          <p>{'정말 구독을 취소하시겠습니까?'}</p>
          <p style={{ marginTop: 8, fontSize: 13, color: 'var(--text2)' }}>
            {'만료일('}
            {sub ? formatDate(sub.expiresAt) : ''}
            {')까지 서비스 이용이 가능합니다.'}
          </p>
        </div>
        <div className={styles.modalFooter}>
          <Button variant="ghost" onClick={() => setShowCancelModal(false)}>
            {'돌아가기'}
          </Button>
          <Button variant="danger" onClick={handleCancel} loading={cancelling}>
            {'취소 확정'}
          </Button>
        </div>
      </Modal>
    </div>
  );
}

function getBillingStatusLabel(status: BillingAgreementResponse['status']): string {
  switch (status) {
    case 'ACTIVE':
      return '자동 갱신 중';
    case 'READY':
      return '등록 미완료';
    case 'SUSPENDED':
      return '갱신 중지';
    case 'CANCELLED':
      return '다음 갱신 중지됨';
    case 'EXPIRED':
      return '만료';
    default:
      return status;
  }
}
